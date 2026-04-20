package com.modules;

import com.connectDB.ConnectDB;
import com.dao.*;
import com.entity.*;
import com.enums.LoaiGhe;
import com.enums.TrangThaiVe;
import com.entity.ChiTietKhuyenMai;
import com.entity.ApDungKM;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Tab Bán vé mới — wizard 7 bước.
 * Mỗi bước là một module độc lập hiển thị trong stagePanel ở giữa.
 * BanVeModule đóng vai trò coordinator: điều phối navigation, lưu context, ghi DB.
 */
public class BanVeModule extends JPanel implements AppModule {

    private Consumer<Object> callback;
    private final NhanVien currentUser;

    // --- UI ---
    private JPanel  stagePanel;
    private JButton btnBack;
    private JLabel  lblPageTitle;
    private JPanel  stepBarPanel;

    // --- Wizard context ---
    private Ga        ctxGaDi, ctxGaDen;
    private LocalDate ctxNgayDiFrom, ctxNgayDiTo;
    private Lich      ctxLich;
    private final Map<LoaiGhe, Double> ctxPriceMap = new EnumMap<>(LoaiGhe.class);
    private final List<Ghe> ctxGhes = new ArrayList<>();
    private final List<KhachHang> ctxKhachHangs = new ArrayList<>();
    private final Map<String, List<ChiTietKhuyenMai>> ctxChiTietKMsBySeat = new LinkedHashMap<>();
    private String    ctxPaymentType;

    // --- Navigation stack ---
    private final Deque<String> stepHistory = new ArrayDeque<>();
    private final Map<String, JPanel> stepCache = new HashMap<>();
    private String currentStep = "";

    private static final String STEP_1  = "1";
    private static final String STEP_2  = "2";
    private static final String STEP_3  = "3";
    private static final String STEP_4  = "4";
    private static final String STEP_5  = "5";   // Khách hàng
    private static final String STEP_5B = "5B";  // Khuyến mãi
    private static final String STEP_6  = "6";   // Xác nhận & thanh toán
    private static final String STEP_7A = "7A";  // Tiền mặt
    private static final String STEP_7B = "7B";  // Chuyển khoản
    private static final String STEP_8  = "8";   // Hoàn thành

    private static final String[] STEP_LABELS = {
        "Thông tin", "Chọn chuyến", "Chọn chỗ",
        "Khách hàng", "Khuyến mãi", "Xác nhận", "Thanh toán", "Hoàn thành"
    };

    // --- Design tokens ---
    private static final Color PRIMARY       = new Color(0x00, 0x5D, 0x90);
    private static final Color PRIMARY_LIGHT = new Color(0xE3, 0xF2, 0xFD);
    private static final Color SURFACE       = new Color(0xF8, 0xFA, 0xFC);
    private static final Color CARD_BG       = Color.WHITE;
    private static final Color ON_SURFACE    = new Color(0x1A, 0x1D, 0x21);
    private static final Color ON_SURF_VAR   = new Color(0x5F, 0x67, 0x70);
    private static final Color OUTLINE       = new Color(0xDE, 0xE3, 0xE8);
    private static final Color STEP_DONE     = new Color(0x2E, 0x7D, 0x32);
    private static final Color STEP_INACTIVE = new Color(0xB0, 0xBE, 0xC5);

    // --- DAOs for final save ---
    private final DAO_Ve               daoVe    = new DAO_Ve();
    private final DAO_HoaDon           daoHD    = new DAO_HoaDon();
    private final DAO_ChiTietHoaDon    daoCTHD  = new DAO_ChiTietHoaDon();
    private final DAO_KhachHang        daoKH    = new DAO_KhachHang();
    private final DAO_ApDungKM         daoADKM  = new DAO_ApDungKM();
    private final DAO_HoaDonKhachHang  daoHDKH  = new DAO_HoaDonKhachHang();

    // --- AppModule buttons ---
    private JButton btnSubmit, btnCancel;
    private JPanel  btnPanel;

    // =========================================================================
    //  CONSTRUCTOR
    // =========================================================================

    public BanVeModule(NhanVien currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout());
        setBackground(SURFACE);
        buildUI();
    }

    // =========================================================================
    //  BUILD UI
    // =========================================================================

    private void buildUI() {
        add(buildHeader(), BorderLayout.NORTH);

        // Center: gray surface with stage card inside
        JPanel centerWrap = new JPanel(new GridBagLayout());
        centerWrap.setBackground(SURFACE);
        centerWrap.setBorder(new EmptyBorder(16, 16, 16, 16));

        stagePanel = new JPanel(new BorderLayout());
        stagePanel.setBackground(CARD_BG);
        stagePanel.setBorder(BorderFactory.createLineBorder(OUTLINE, 1));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        centerWrap.add(stagePanel, gbc);
        add(centerWrap, BorderLayout.CENTER);

        // AppModule buttons (only shown when used as dialog)
        btnSubmit = new JButton("Xác nhận");
        btnCancel = new JButton("Hủy");
        btnCancel.addActionListener(e -> { if (callback != null) callback.accept(null); });
        btnPanel  = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btnPanel.setBackground(SURFACE);
        btnPanel.add(btnCancel);
        btnPanel.add(btnSubmit);
        btnPanel.setVisible(false);
        add(btnPanel, BorderLayout.SOUTH);

        navigateTo(STEP_1, false);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(CARD_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, OUTLINE),
            new EmptyBorder(14, 24, 14, 24)
        ));

        // Left: back button + title
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setBackground(CARD_BG);

        btnBack = new JButton("← Quay lại");
        btnBack.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnBack.setForeground(PRIMARY);
        btnBack.setBackground(PRIMARY_LIGHT);
        btnBack.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xBB, 0xDE, 0xFB), 1),
            new EmptyBorder(5, 14, 5, 14)
        ));
        btnBack.setFocusPainted(false);
        btnBack.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBack.setVisible(false);
        btnBack.addActionListener(e -> goBack());

        lblPageTitle = new JLabel("Bán vé mới");
        lblPageTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblPageTitle.setForeground(ON_SURFACE);

        left.add(btnBack);
        left.add(lblPageTitle);

        // Center: step progress bar
        stepBarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        stepBarPanel.setBackground(CARD_BG);

        header.add(left, BorderLayout.WEST);
        header.add(stepBarPanel, BorderLayout.CENTER);
        return header;
    }

    private void refreshStepBar() {
        stepBarPanel.removeAll();
        int activeIdx = stepToIndex(currentStep);

        for (int i = 0; i < STEP_LABELS.length; i++) {
            // Connector
            if (i > 0) {
                JLabel conn = new JLabel("—");
                conn.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                conn.setForeground(i <= activeIdx ? STEP_DONE : STEP_INACTIVE);
                stepBarPanel.add(conn);
            }

            final boolean done   = i < activeIdx;
            final boolean active = i == activeIdx;
            final Color   bgClr  = done ? STEP_DONE : (active ? PRIMARY : STEP_INACTIVE);

            JPanel chip = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 1));
            chip.setOpaque(false);

            String badgeStr = done ? "V" : String.valueOf(i + 1);
            JLabel badge = new JLabel(badgeStr) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(bgClr);
                    g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            badge.setOpaque(false);
            badge.setForeground(Color.WHITE);
            badge.setFont(new Font("Segoe UI", Font.BOLD, 10));
            badge.setHorizontalAlignment(SwingConstants.CENTER);
            badge.setPreferredSize(new Dimension(20, 20));

            JLabel stepLbl = new JLabel(STEP_LABELS[i]);
            stepLbl.setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 11));
            stepLbl.setForeground(active ? PRIMARY : (done ? STEP_DONE : STEP_INACTIVE));

            chip.add(badge);
            chip.add(stepLbl);
            stepBarPanel.add(chip);
        }

        stepBarPanel.revalidate();
        stepBarPanel.repaint();
    }

    private int stepToIndex(String step) {
        return switch (step) {
            case STEP_1             -> 0;
            case STEP_2             -> 1;
            case STEP_3             -> 2;
            case STEP_5             -> 3;
            case STEP_5B            -> 4;
            case STEP_6             -> 5;
            case STEP_7A, STEP_7B   -> 6;
            case STEP_8             -> 7;
            default                 -> 0;
        };
    }

    // =========================================================================
    //  NAVIGATION
    // =========================================================================

    private void navigateTo(String step, boolean pushHistory) {
        if (pushHistory && !currentStep.isEmpty()) {
            stepHistory.push(currentStep);
            // Moving forward: clear the next step from cache to ensure it's fresh
            stepCache.remove(step);
        }
        currentStep = step;
        renderStep();
        btnBack.setVisible(!stepHistory.isEmpty());
        refreshStepBar();
    }

    private void goBack() {
        if (stepHistory.isEmpty()) return;
        currentStep = stepHistory.pop();
        renderStep();
        btnBack.setVisible(!stepHistory.isEmpty());
        refreshStepBar();
    }

    private void renderStep() {
        JPanel view = stepCache.get(currentStep);
        if (view == null) {
            view = buildStepView(currentStep);
            stepCache.put(currentStep, view);
        }
        stagePanel.removeAll();
        stagePanel.add(view, BorderLayout.CENTER);
        stagePanel.revalidate();
        stagePanel.repaint();
    }

    @SuppressWarnings("unchecked")
    private JPanel buildStepView(String step) {
        return switch (step) {

            case STEP_1 -> {
                BanVeStep1Module m = new BanVeStep1Module();
                m.setOnResult(result -> {
                    if (result instanceof Object[] arr) {
                        ctxGaDi       = (Ga)        arr[0];
                        ctxGaDen      = (Ga)        arr[1];
                        ctxNgayDiFrom = (LocalDate) arr[2];
                        ctxNgayDiTo   = (LocalDate) arr[3];
                        navigateTo(STEP_2, true);
                    }
                });
                yield m.getView();
            }

            case STEP_2 -> {
                BanVeStep2Module m = new BanVeStep2Module(ctxGaDi, ctxGaDen, ctxNgayDiFrom, ctxNgayDiTo);
                m.setOnResult(result -> {
                    if (result == null) { goBack(); return; }
                    if (result instanceof Object[] arr) {
                        ctxLich = (Lich) arr[0];
                        ctxPriceMap.clear();
                        ctxPriceMap.putAll((Map<LoaiGhe, Double>) arr[1]);
                        navigateTo(STEP_3, true);
                    }
                });
                yield m.getView();
            }

            case STEP_3 -> {
                BanVeStep3Module m = new BanVeStep3Module(ctxLich);
                m.setOnResult(result -> {
                    if (result == null) { goBack(); return; }
                    if (result instanceof List<?> ghes) {
                        ctxGhes.clear();
                        for (Object o : ghes) if (o instanceof Ghe g) ctxGhes.add(g);
                        navigateTo(STEP_5, true);
                    }
                });
                yield m.getView();
            }

            case STEP_5 -> {
                BanVeStep5Module m = new BanVeStep5Module();
                m.setOnResult(result -> {
                    if (result == null) { goBack(); return; }
                    if (result instanceof List<?> list && !list.isEmpty()) {
                        ctxKhachHangs.clear();
                        for (Object o : list) if (o instanceof KhachHang kh) ctxKhachHangs.add(kh);
                        if (ctxKhachHangs.isEmpty()) return;
                        navigateTo(STEP_5B, true);
                    }
                });
                yield m.getView();
            }

            case STEP_5B -> {
                Tuyen   tuyen      = (ctxLich != null) ? ctxLich.getTuyen() : null;
                BanVeStep5bModule m5b = new BanVeStep5bModule(tuyen, ctxGhes, ctxPriceMap);
                m5b.setOnResult(result -> {
                    if (result == null) { goBack(); return; }
                    ctxChiTietKMsBySeat.clear();
                    if (result instanceof Map<?, ?> mapResult) {
                        for (Map.Entry<?, ?> entry : mapResult.entrySet()) {
                            if (!(entry.getKey() instanceof String maGhe)) continue;
                            List<ChiTietKhuyenMai> seatPromos = new ArrayList<>();
                            if (entry.getValue() instanceof List<?> list) {
                                for (Object o : list) {
                                    if (o instanceof ChiTietKhuyenMai ctkm) seatPromos.add(ctkm);
                                }
                            }
                            ctxChiTietKMsBySeat.put(maGhe, seatPromos);
                        }
                    }
                    navigateTo(STEP_6, true);
                });
                yield m5b.getView();
            }

            case STEP_6 -> {
                // Toa đại diện = toa của ghế đầu tiên (Step6 sẽ hiển thị chi tiết từng ghế nếu mixed)
                ToaTau repToa = (!ctxGhes.isEmpty()) ? ctxGhes.get(0).getToaTau() : null;
                BanVeStep6Module m = new BanVeStep6Module(
                    ctxLich, repToa, Collections.unmodifiableList(ctxGhes),
                    ctxPriceMap, Collections.unmodifiableList(ctxKhachHangs),
                    freezeSeatPromoMap(ctxChiTietKMsBySeat));
                m.setOnResult(result -> {
                    if (result == null) { goBack(); return; }
                    if (result instanceof String paymentType) {
                        ctxPaymentType = paymentType;
                        navigateTo("TIEN_MAT".equals(ctxPaymentType) ? STEP_7A : STEP_7B, true);
                    }
                });
                yield m.getView();
            }

            case STEP_7A -> {
                BanVeStep7TienMatModule m = new BanVeStep7TienMatModule(calcTotal());
                m.setOnResult(result -> {
                    if (result == null) { goBack(); return; }
                    if ("CONFIRMED".equals(result)) {
                        saveTransaction();
                        navigateTo(STEP_8, true);
                    }
                });
                yield m.getView();
            }

            case STEP_7B -> {
                BanVeStep7ChuyenKhoanModule m = new BanVeStep7ChuyenKhoanModule(calcTotal());
                m.setOnResult(result -> {
                    if (result == null) { goBack(); return; }
                    if ("CONFIRMED".equals(result)) {
                        saveTransaction();
                        navigateTo(STEP_8, true);
                    }
                });
                yield m.getView();
            }

            case STEP_8 -> {
                BanVeStep8Module m = new BanVeStep8Module();
                m.setOnResult(result -> reset());
                yield m.getView();
            }

            default -> new JPanel();
        };
    }

    // =========================================================================
    //  BUSINESS LOGIC
    // =========================================================================

    /** Đơn giá gốc của một ghế, dựa theo LoaiGhe của toa chứa ghế đó. */
    private double unitPriceFor(Ghe g) {
        if (g == null || g.getToaTau() == null) return 0.0;
        return ctxPriceMap.getOrDefault(g.getToaTau().getLoaiGhe(), 0.0);
    }

    /** Tổng đơn giá gốc (chưa giảm) của tất cả ghế đã chọn. */
    private double sumBasePrice() {
        double sum = 0.0;
        for (Ghe g : ctxGhes) sum += unitPriceFor(g);
        return sum;
    }

    private List<ChiTietKhuyenMai> selectedPromotionsForSeat(Ghe ghe) {
        List<ChiTietKhuyenMai> applicable = new ArrayList<>();
        if (ghe == null || ctxLich == null || ctxLich.getTuyen() == null) return applicable;

        String seatKey = seatKey(ghe);
        List<ChiTietKhuyenMai> chosenPromos = ctxChiTietKMsBySeat.getOrDefault(seatKey, Collections.emptyList());
        if (chosenPromos.isEmpty()) return applicable;

        String maTuyen = ctxLich.getTuyen().getMaTuyen();
        LoaiGhe loai = ghe.getToaTau() != null ? ghe.getToaTau().getLoaiGhe() : null;
        for (ChiTietKhuyenMai km : chosenPromos) {
            if (km == null) continue;
            boolean tuyenOk = km.getTuyen() == null
                    || (km.getTuyen().getMaTuyen() != null && km.getTuyen().getMaTuyen().equals(maTuyen));
            boolean loaiOk = km.getLoaiGhe() == null || km.getLoaiGhe() == loai;
            if (tuyenOk && loaiOk) applicable.add(km);
        }
        return applicable;
    }

    private double clampDiscount(double discount) {
        if (Double.isNaN(discount)) return 0.0;
        return Math.max(0.0, Math.min(1.0, discount));
    }

    private double finalPriceForSeat(Ghe ghe) {
        double price = unitPriceFor(ghe);
        for (ChiTietKhuyenMai km : selectedPromotionsForSeat(ghe)) {
            price *= (1.0 - clampDiscount(km.getPhanTramGiam()));
        }
        return price;
    }

    private BigDecimal calcTotal() {
        if (ctxGhes.isEmpty()) return BigDecimal.ZERO;
        double total = 0.0;
        for (Ghe g : ctxGhes) total += finalPriceForSeat(g);
        return BigDecimal.valueOf(total);
    }

    private void saveTransaction() {
        try {
            // 0. Kiểm tra giá hợp lệ trước khi ghi DB — DB có CHECK giaTien > 0.
            for (Ghe g : ctxGhes) {
                double finalPrice = finalPriceForSeat(g);
                if (finalPrice <= 0.0) {
                    JOptionPane.showMessageDialog(this,
                        "Không tìm được giá hợp lệ cho ghế " + (g != null ? g.getMaGhe() : "?") +
                        ".\nVui lòng kiểm tra bảng giá tuyến cho loại ghế này trước khi bán vé.",
                        "Lỗi dữ liệu giá", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            // 1. Lưu / cập nhật từng KhachHang trong ctxKhachHangs
            List<KhachHang> savedKHs = new ArrayList<>();
            for (KhachHang kh : ctxKhachHangs) {
                if (kh.getMaKhachHang() != null) {
                    daoKH.update(kh);
                } else {
                    kh.setMaKhachHang(genId("KH"));
                    daoKH.insert(kh);
                }
                savedKHs.add(kh);
            }

            // 2. Tạo HoaDon (không còn maKhachHang — tách sang HoaDonKhachHang)
            String maHD = daoHD.phatSinhMaHoaDon();
            HoaDon hd   = new HoaDon(maHD, currentUser, LocalDateTime.now());
            daoHD.insert(hd);

            // 3. Link HoaDon ↔ từng KhachHang qua bảng junction
            for (KhachHang kh : savedKHs) {
                HoaDonKhachHang link = new HoaDonKhachHang(
                    daoHDKH.phatSinhMaHDKH(), hd, kh);
                daoHDKH.insert(link);
            }

            // 4. Tạo Ve + ChiTietHoaDon (+ ApDungKM) — giá tính riêng cho từng ghế
            for (Ghe ghe : ctxGhes) {
                double finalPrice = finalPriceForSeat(ghe);
                List<ChiTietKhuyenMai> appliedPromos = selectedPromotionsForSeat(ghe);

                Ve ve = new Ve(genId("VE"), ctxLich, ghe, TrangThaiVe.DA_BAN, null, null);
                daoVe.insert(ve);

                ChiTietHoaDon cthd = new ChiTietHoaDon(
                    genId("CTHD"), hd, ve, BigDecimal.valueOf(finalPrice));
                daoCTHD.insert(cthd);

                for (ChiTietKhuyenMai km : appliedPromos) {
                    ApDungKM adkm = new ApDungKM(genId("ADKM"), cthd, km);
                    daoADKM.insert(adkm);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Lỗi khi lưu giao dịch: " + ex.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Tạo ID an toàn cho đa luồng bằng cách đếm số bản ghi hiện có trong DB.
     * Giữ nguyên format: PREFIX-ddMMyyyy-NNN
     * Không dùng UUID — tránh birthday paradox khi nhiều nhân viên bán vé cùng lúc.
     */
    private String genId(String prefix) {
        LocalDate now = LocalDate.now();
        String datePrefix = String.format("%s-%02d%02d%04d",
            prefix, now.getDayOfMonth(), now.getMonthValue(), now.getYear());

        Connection con = ConnectDB.getCon();
        if (con == null) return datePrefix + "-001";

        String tableColumn = switch (prefix) {
            case "VE"   -> "maVe";
            case "KH"   -> "maKhachHang";
            case "CTHD" -> "maChiTietHD";
            case "ADKM" -> "maApDung";
            default     -> "maVe";
        };

        String sql = "SELECT COUNT(*) FROM " + getTableName(prefix) + " WHERE " + tableColumn + " LIKE ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, datePrefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1) + 1;
                    return datePrefix + "-" + String.format("%03d", count);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi phát sinh ID: " + e.getMessage());
        }
        return datePrefix + "-001";
    }

    private String getTableName(String prefix) {
        return switch (prefix) {
            case "VE"   -> "Ve";
            case "KH"   -> "KhachHang";
            case "CTHD" -> "ChiTietHoaDon";
            case "ADKM" -> "ApDungKM";
            default     -> "Ve";
        };
    }

    private String seatKey(Ghe ghe) {
        if (ghe == null) return "";
        if (ghe.getMaGhe() != null && !ghe.getMaGhe().isBlank()) return ghe.getMaGhe();
        String toa = (ghe.getToaTau() != null && ghe.getToaTau().getMaToaTau() != null)
            ? ghe.getToaTau().getMaToaTau() : "TOA";
        return toa + "-SEAT-" + ghe.getSoGhe();
    }

    private Map<String, List<ChiTietKhuyenMai>> freezeSeatPromoMap(
            Map<String, List<ChiTietKhuyenMai>> source) {
        Map<String, List<ChiTietKhuyenMai>> frozen = new LinkedHashMap<>();
        for (Map.Entry<String, List<ChiTietKhuyenMai>> e : source.entrySet()) {
            String key = e.getKey();
            List<ChiTietKhuyenMai> value = e.getValue();
            frozen.put(key, Collections.unmodifiableList(
                value == null ? Collections.emptyList() : new ArrayList<>(value)));
        }
        return Collections.unmodifiableMap(frozen);
    }

    // =========================================================================
    //  AppModule
    // =========================================================================

    @Override public String getTitle() { return "Bán vé mới"; }
    @Override public JPanel getView()  { return this; }

    @Override
    public void setOnResult(Consumer<Object> cb) {
        this.callback = cb;
        boolean show = cb != null;
        btnSubmit.setVisible(show);
        btnCancel.setVisible(show);
        btnPanel.setVisible(show);
    }

    @Override
    public void reset() {
        ctxGaDi   = null; ctxGaDen = null; ctxNgayDiFrom = null; ctxNgayDiTo = null;
        ctxLich   = null; ctxPriceMap.clear();
        ctxGhes.clear();
        ctxKhachHangs.clear();
        ctxChiTietKMsBySeat.clear();
        ctxPaymentType = null;
        stepHistory.clear();
        stepCache.clear();
        navigateTo(STEP_1, false);
    }
}
