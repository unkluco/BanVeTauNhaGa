package com.modules;

import com.dao.*;
import com.entity.*;
import com.enums.LoaiGhe;
import com.enums.TrangThaiVe;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
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
    private LocalDate ctxNgayDi;
    private Lich      ctxLich;
    private final Map<LoaiGhe, Double> ctxPriceMap = new EnumMap<>(LoaiGhe.class);
    private ToaTau    ctxToa;
    private final List<Ghe> ctxGhes = new ArrayList<>();
    private KhachHang ctxKhachHang;
    private String    ctxPaymentType;

    // --- Navigation stack ---
    private final Deque<String> stepHistory = new ArrayDeque<>();
    private String currentStep = "";

    private static final String STEP_1  = "1";
    private static final String STEP_2  = "2";
    private static final String STEP_3  = "3";
    private static final String STEP_4  = "4";
    private static final String STEP_5  = "5";
    private static final String STEP_6A = "6A";
    private static final String STEP_6B = "6B";
    private static final String STEP_7  = "7";

    private static final String[] STEP_LABELS = {
        "Thông tin", "Chọn chuyến", "Chọn toa", "Chọn ghế", "Xác nhận", "Thanh toán", "Hoàn thành"
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
    private final DAO_Ve            daoVe   = new DAO_Ve();
    private final DAO_HoaDon        daoHD   = new DAO_HoaDon();
    private final DAO_ChiTietHoaDon daoCTHD = new DAO_ChiTietHoaDon();
    private final DAO_KhachHang     daoKH   = new DAO_KhachHang();

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

            String badgeStr = done ? "✓" : String.valueOf(i + 1);
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
            case STEP_1        -> 0;
            case STEP_2        -> 1;
            case STEP_3        -> 2;
            case STEP_4        -> 3;
            case STEP_5        -> 4;
            case STEP_6A, STEP_6B -> 5;
            case STEP_7        -> 6;
            default            -> 0;
        };
    }

    // =========================================================================
    //  NAVIGATION
    // =========================================================================

    private void navigateTo(String step, boolean pushHistory) {
        if (pushHistory && !currentStep.isEmpty()) {
            stepHistory.push(currentStep);
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
        JPanel view = buildStepView(currentStep);
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
                        ctxGaDi   = (Ga)        arr[0];
                        ctxGaDen  = (Ga)        arr[1];
                        ctxNgayDi = (LocalDate) arr[2];
                        navigateTo(STEP_2, true);
                    }
                });
                yield m.getView();
            }

            case STEP_2 -> {
                BanVeStep2Module m = new BanVeStep2Module(ctxGaDi, ctxGaDen, ctxNgayDi);
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
                    if (result instanceof ToaTau toa) {
                        ctxToa = toa;
                        ctxGhes.clear();
                        navigateTo(STEP_4, true);
                    }
                });
                yield m.getView();
            }

            case STEP_4 -> {
                BanVeStep4Module m = new BanVeStep4Module(ctxLich, ctxToa);
                m.setOnResult(result -> {
                    if (result == null) { goBack(); return; }
                    if (result instanceof List<?> list && !list.isEmpty()) {
                        ctxGhes.clear();
                        for (Object o : list) if (o instanceof Ghe g) ctxGhes.add(g);
                        navigateTo(STEP_5, true);
                    }
                });
                yield m.getView();
            }

            case STEP_5 -> {
                BanVeStep5Module m = new BanVeStep5Module(
                    ctxLich, ctxToa, Collections.unmodifiableList(ctxGhes), ctxPriceMap);
                m.setOnResult(result -> {
                    if (result == null) { goBack(); return; }
                    if (result instanceof Object[] arr) {
                        ctxKhachHang  = (KhachHang) arr[0];
                        ctxPaymentType = (String)   arr[1];
                        navigateTo("TIEN_MAT".equals(ctxPaymentType) ? STEP_6A : STEP_6B, true);
                    }
                });
                yield m.getView();
            }

            case STEP_6A -> {
                BanVeStep6aTienMatModule m = new BanVeStep6aTienMatModule(calcTotal());
                m.setOnResult(result -> {
                    if (result == null) { goBack(); return; }
                    if ("CONFIRMED".equals(result)) {
                        saveTransaction();
                        navigateTo(STEP_7, true);
                    }
                });
                yield m.getView();
            }

            case STEP_6B -> {
                BanVeStep6bChuyenKhoanModule m = new BanVeStep6bChuyenKhoanModule(calcTotal());
                m.setOnResult(result -> {
                    if (result == null) { goBack(); return; }
                    if ("CONFIRMED".equals(result)) {
                        saveTransaction();
                        navigateTo(STEP_7, true);
                    }
                });
                yield m.getView();
            }

            case STEP_7 -> {
                BanVeStep7Module m = new BanVeStep7Module();
                m.setOnResult(result -> reset());
                yield m.getView();
            }

            default -> new JPanel();
        };
    }

    // =========================================================================
    //  BUSINESS LOGIC
    // =========================================================================

    private BigDecimal calcTotal() {
        if (ctxToa == null || ctxGhes.isEmpty()) return BigDecimal.ZERO;
        LoaiGhe loai = ctxToa.getLoaiGhe();
        double  unit = ctxPriceMap.getOrDefault(loai, 0.0);
        return BigDecimal.valueOf(unit * ctxGhes.size());
    }

    private void saveTransaction() {
        try {
            // 1. Find or create KhachHang
            KhachHang kh = daoKH.findByCCCD(ctxKhachHang.getCccd());
            if (kh == null) {
                ctxKhachHang.setMaKhachHang(genId("KH"));
                daoKH.insert(ctxKhachHang);
                kh = ctxKhachHang;
            }

            // 2. Create HoaDon
            String  maHD = daoHD.phatSinhMaHoaDon();
            HoaDon  hd   = new HoaDon(maHD, currentUser, kh, LocalDateTime.now());
            daoHD.insert(hd);

            // 3. Create Ve + ChiTietHoaDon for each seat
            LoaiGhe loai      = ctxToa != null ? ctxToa.getLoaiGhe() : null;
            double  unitPrice = loai != null ? ctxPriceMap.getOrDefault(loai, 0.0) : 0.0;

            for (Ghe ghe : ctxGhes) {
                Ve ve = new Ve(genId("VE"), ctxLich, ghe, TrangThaiVe.DA_BAN, null, null);
                daoVe.insert(ve);

                ChiTietHoaDon cthd = new ChiTietHoaDon(
                    genId("CTHD"), hd, ve, BigDecimal.valueOf(unitPrice));
                daoCTHD.insert(cthd);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Lỗi khi lưu giao dịch: " + ex.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String genId(String prefix) {
        LocalDate n = LocalDate.now();
        return String.format("%s-%02d%02d%04d-%s",
            prefix, n.getDayOfMonth(), n.getMonthValue(), n.getYear(),
            UUID.randomUUID().toString().substring(0, 6).toUpperCase());
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
        ctxGaDi   = null; ctxGaDen = null; ctxNgayDi = null;
        ctxLich   = null; ctxPriceMap.clear();
        ctxToa    = null; ctxGhes.clear();
        ctxKhachHang = null; ctxPaymentType = null;
        stepHistory.clear();
        navigateTo(STEP_1, false);
    }
}
