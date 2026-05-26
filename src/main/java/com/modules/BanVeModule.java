package com.modules;

import com.dao.*;
import com.entity.*;
import com.enums.LoaiGhe;
import com.enums.TrangThaiVe;
import com.entity.ChiTietKhuyenMai;
import com.entity.ApDungKM;
import com.util.MaTuDong;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
    private LocalDate ctxNgayDiFrom, ctxNgayDiTo;
    private Lich      ctxLich;
    private final Map<LoaiGhe, Double> ctxPriceMap = new EnumMap<>(LoaiGhe.class);
    private final Map<LoaiGhe, ChiTietGia> ctxPriceDetailMap = new EnumMap<>(LoaiGhe.class);
    private final List<Ghe> ctxGhes = new ArrayList<>();
    private final Map<String, KhachHang> ctxKhachHangBySeat = new LinkedHashMap<>();
    private final Map<String, List<ChiTietKhuyenMai>> ctxChiTietKMsBySeat = new LinkedHashMap<>();
    private String    ctxPaymentType;
    private HoaDon    ctxSavedHoaDon;
    private final DAO_GiuCho daoGiuCho = new DAO_GiuCho();

    // --- Navigation stack ---
    private final Deque<String> stepHistory = new ArrayDeque<>();
    private final Map<String, JPanel> stepCache = new HashMap<>();
    private String currentStep = "";
    private int maxUnlockedStepIndex = 0;

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
    private static final int HOLD_TTL_SECONDS = 5 * 60;

    private static final String[] STEP_LABELS = {
        "Thông tin", "Chọn chuyến", "Chọn chỗ",
        "Khách hàng", "Khuyến mãi", "Xác nhận", "Thanh toán", "Hoàn thành"
    };

    // --- Design tokens ---
    private static final Color PRIMARY       = NotionTheme.ACCENT;
    private static final Color PRIMARY_LIGHT = NotionTheme.ACCENT_SOFT;
    private static final Color SURFACE       = NotionTheme.PAGE;
    private static final Color CARD_BG       = NotionTheme.CARD;
    private static final Color ON_SURFACE    = NotionTheme.TEXT;
    private static final Color ON_SURF_VAR   = NotionTheme.TEXT_MUTED;
    private static final Color OUTLINE       = NotionTheme.BORDER;
    private static final Color STEP_DONE     = AppColors.SUCCESS_DARK;
    private static final Color STEP_DONE_BG  = AppColors.SUCCESS_LIGHT;
    private static final Color STEP_INACTIVE = NotionTheme.TEXT_FAINT;

    // --- DAOs for final save ---
    private final DAO_Ve               daoVe    = new DAO_Ve();
    private final DAO_HoaDon           daoHD    = new DAO_HoaDon();
    private final DAO_ChiTietHoaDon    daoCTHD  = new DAO_ChiTietHoaDon();
    private final DAO_KhachHang        daoKH    = new DAO_KhachHang();
    private final DAO_ApDungKM         daoADKM  = new DAO_ApDungKM();

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
        centerWrap.setBorder(new EmptyBorder(22, 24, 24, 24));

        stagePanel = NotionTheme.cardPanel(new BorderLayout());
        stagePanel.setBackground(CARD_BG);
        stagePanel.setBorder(BorderFactory.createEmptyBorder());

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
        NotionTheme.styleSecondaryButton(btnBack);
        btnBack.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnBack.setVisible(false);
        btnBack.addActionListener(e -> goBack());

        lblPageTitle = new JLabel("Bán vé mới");
        lblPageTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblPageTitle.setForeground(ON_SURFACE);

        left.add(btnBack);
        left.add(lblPageTitle);

        // Center: step progress blocks
        stepBarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        stepBarPanel.setBackground(CARD_BG);

        header.add(left, BorderLayout.WEST);
        header.add(stepBarPanel, BorderLayout.CENTER);
        return header;
    }

    private void refreshStepBar() {
        stepBarPanel.removeAll();
        int activeIdx = stepToIndex(currentStep);

        for (int i = 0; i < STEP_LABELS.length; i++) {
            final boolean done   = i < activeIdx;
            final boolean active = i == activeIdx;
            final boolean reachable = i <= maxUnlockedStepIndex && stepCache.containsKey(stepFromIndex(i));
            final Color bgClr = active ? PRIMARY : (done || reachable ? STEP_DONE_BG : NotionTheme.CARD_MUTED);
            final Color borderClr = active ? PRIMARY : (done || reachable ? AppColors.SUCCESS_LIGHT : OUTLINE);
            final Color textClr = active ? Color.WHITE : (done || reachable ? STEP_DONE : ON_SURF_VAR);

            JPanel chip = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0)) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(bgClr);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 12, 12));
                    g2.setColor(borderClr);
                    g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 2, getHeight() - 2, 12, 12));
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            chip.setOpaque(false);
            chip.setBorder(new EmptyBorder(6, 10, 6, 10));
            chip.setPreferredSize(new Dimension(116, 32));
            chip.setCursor(reachable ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
            if (reachable && !active) {
                final String targetStep = stepFromIndex(i);
                chip.addMouseListener(new MouseAdapter() {
                    @Override public void mouseClicked(MouseEvent e) { navigateToVisitedStep(targetStep); }
                });
            }

            JLabel stepLbl = new JLabel((i + 1) + ". " + STEP_LABELS[i]);
            stepLbl.setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 11));
            stepLbl.setForeground(textClr);
            stepLbl.setHorizontalAlignment(SwingConstants.CENTER);

            chip.add(stepLbl);
            stepBarPanel.add(chip);
        }

        stepBarPanel.revalidate();
        stepBarPanel.repaint();
        // Handoff: stepper dùng block có chữ để đọc nhanh trong menu full window.
        // Risk: nếu thêm bước mới, kiểm tra lại chiều rộng 116px để không bị cắt chữ.
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

    private String stepFromIndex(int index) {
        return switch (index) {
            case 0 -> STEP_1;
            case 1 -> STEP_2;
            case 2 -> STEP_3;
            case 3 -> STEP_5;
            case 4 -> STEP_5B;
            case 5 -> STEP_6;
            case 6 -> STEP_7A;
            case 7 -> STEP_8;
            default -> STEP_1;
        };
    }

    // =========================================================================
    //  NAVIGATION
    // =========================================================================

    private void navigateTo(String step, boolean pushHistory) {
        if (pushHistory && !validateBeforeForward(step)) return;
        if (pushHistory && !currentStep.isEmpty()) {
            stepHistory.push(currentStep);
            // Moving forward: clear the next step from cache to ensure it's fresh
            stepCache.remove(step);
        }
        currentStep = step;
        maxUnlockedStepIndex = Math.max(maxUnlockedStepIndex, stepToIndex(step));
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

    private void navigateToVisitedStep(String step) {
        if (!stepCache.containsKey(step)) return;
        currentStep = step;
        rebuildHistoryBefore(step);
        renderStep();
        btnBack.setVisible(!stepHistory.isEmpty());
        refreshStepBar();
    }

    private void rebuildHistoryBefore(String step) {
        stepHistory.clear();
        int targetIndex = stepToIndex(step);
        for (int i = 0; i < targetIndex; i++) stepHistory.push(stepFromIndex(i));
    }

    private void clearAfterStep(String step) {
        int index = stepToIndex(step);
        for (int i = index + 1; i < STEP_LABELS.length; i++) stepCache.remove(stepFromIndex(i));
        if (index < stepToIndex(STEP_2)) { ctxLich = null; ctxPriceMap.clear(); ctxPriceDetailMap.clear(); }
        if (index < stepToIndex(STEP_3)) { releaseCurrentHolds(); ctxGhes.clear(); }
        if (index < stepToIndex(STEP_5)) ctxKhachHangBySeat.clear();
        if (index < stepToIndex(STEP_5B)) ctxChiTietKMsBySeat.clear();
        if (index < stepToIndex(STEP_6)) ctxPaymentType = null;
        if (index < stepToIndex(STEP_8)) ctxSavedHoaDon = null;
        maxUnlockedStepIndex = Math.min(maxUnlockedStepIndex, index);
        // Handoff: sửa dữ liệu ở bước trước sẽ cắt cache/context các bước phụ thuộc phía sau.
        // Cảnh báo: nếu thêm bước mới phải cập nhật stepFromIndex và dependency clear tại đây.
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
                        Ga newGaDi = (Ga) arr[0];
                        Ga newGaDen = (Ga) arr[1];
                        LocalDate newFrom = (LocalDate) arr[2];
                        LocalDate newTo = (LocalDate) arr[3];
                        boolean changed = !Objects.equals(stationId(ctxGaDi), stationId(newGaDi))
                                || !Objects.equals(stationId(ctxGaDen), stationId(newGaDen))
                                || !Objects.equals(ctxNgayDiFrom, newFrom)
                                || !Objects.equals(ctxNgayDiTo, newTo);
                        if (changed) clearAfterStep(STEP_1);
                        ctxGaDi = newGaDi;
                        ctxGaDen = newGaDen;
                        ctxNgayDiFrom = newFrom;
                        ctxNgayDiTo = newTo;
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
                        Lich newLich = (Lich) arr[0];
                        Map<LoaiGhe, Double> newPriceMap = new EnumMap<>((Map<LoaiGhe, Double>) arr[1]);
                        Map<LoaiGhe, ChiTietGia> newPriceDetailMap = new EnumMap<>(LoaiGhe.class);
                        if (arr.length > 2) newPriceDetailMap.putAll((Map<LoaiGhe, ChiTietGia>) arr[2]);
                        boolean changed = !Objects.equals(lichId(ctxLich), lichId(newLich))
                                || !Objects.equals(ctxPriceMap, newPriceMap)
                                || !samePriceDetailMap(ctxPriceDetailMap, newPriceDetailMap);
                        if (changed) clearAfterStep(STEP_2);
                        ctxLich = newLich;
                        ctxPriceMap.clear();
                        ctxPriceMap.putAll(newPriceMap);
                        ctxPriceDetailMap.clear();
                        ctxPriceDetailMap.putAll(newPriceDetailMap);
                        navigateTo(STEP_3, true);
                    }
                });
                yield m.getView();
            }

            case STEP_3 -> {
                BanVeStep3Module m = new BanVeStep3Module(ctxLich, ctxPriceMap);
                m.setHeldByOtherSeats(heldSeatsByOther());
                m.setHeldByMeSeats(heldSeatsByCurrentUser());
                m.setSeatHoldHandlers(ghe -> {
                    boolean held = holdSeatForCurrentUser(ghe);
                    if (held) m.markHeldByMe(ghe, LocalDateTime.now().plusSeconds(HOLD_TTL_SECONDS));
                    if (!held) m.setHeldByOtherSeats(heldSeatsByOther());
                    return held;
                }, ghe -> {
                    boolean released = releaseSeatForCurrentUser(ghe);
                    if (released) m.unmarkHeldByMe(ghe);
                    return released;
                });
                m.setOnResult(result -> {
                    if (result == null) { goBack(); return; }
                    if (result instanceof List<?> ghes) {
                        List<Ghe> newSeats = new ArrayList<>();
                        for (Object o : ghes) if (o instanceof Ghe g) newSeats.add(g);
                        if (!sameSeatList(ctxGhes, newSeats)) clearAfterStep(STEP_3);
                        ctxGhes.clear();
                        ctxGhes.addAll(newSeats);
                        navigateTo(STEP_5, true);
                    }
                });
                yield m.getView();
            }

            case STEP_5 -> {
                BanVeStep5Module m = new BanVeStep5Module(Collections.unmodifiableList(ctxGhes));
                m.setOnResult(result -> {
                    if (result == null) { goBack(); return; }
                    if (result instanceof List<?> list && !list.isEmpty()) {
                        Map<String, KhachHang> newCustomersBySeat = new LinkedHashMap<>();
                        for (int i = 0; i < Math.min(ctxGhes.size(), list.size()); i++) {
                            Object o = list.get(i);
                            if (o instanceof KhachHang kh) newCustomersBySeat.put(seatKey(ctxGhes.get(i)), kh);
                        }
                        if (newCustomersBySeat.size() != ctxGhes.size()) return;
                        if (!sameCustomerMap(ctxKhachHangBySeat, newCustomersBySeat)) clearAfterStep(STEP_5);
                        ctxKhachHangBySeat.clear();
                        ctxKhachHangBySeat.putAll(newCustomersBySeat);
                        navigateTo(STEP_5B, true);
                    }
                });
                yield m.getView();
            }

            case STEP_5B -> {
                Tuyen   tuyen      = (ctxLich != null) ? ctxLich.getTuyen() : null;
                LocalDateTime departureTime = (ctxLich != null) ? ctxLich.getThoiGianBatDau() : null;
                BanVeStep5bModule m5b = new BanVeStep5bModule(
                    tuyen, ctxGhes, ctxPriceMap, departureTime);
                m5b.setOnResult(result -> {
                    if (result == null) { goBack(); return; }
                    Map<String, List<ChiTietKhuyenMai>> newPromosBySeat = new LinkedHashMap<>();
                    if (result instanceof Map<?, ?> mapResult) {
                        for (Map.Entry<?, ?> entry : mapResult.entrySet()) {
                            if (!(entry.getKey() instanceof String maGhe)) continue;
                            List<ChiTietKhuyenMai> seatPromos = new ArrayList<>();
                            if (entry.getValue() instanceof List<?> list) {
                                for (Object o : list) {
                                    if (o instanceof ChiTietKhuyenMai ctkm) seatPromos.add(ctkm);
                                }
                            }
                            newPromosBySeat.put(maGhe, seatPromos);
                        }
                    }
                    if (!samePromotionMap(ctxChiTietKMsBySeat, newPromosBySeat)) clearAfterStep(STEP_5B);
                    ctxChiTietKMsBySeat.clear();
                    ctxChiTietKMsBySeat.putAll(newPromosBySeat);
                    navigateTo(STEP_6, true);
                });
                yield m5b.getView();
            }

            case STEP_6 -> {
                // Toa đại diện = toa của ghế đầu tiên (Step6 sẽ hiển thị chi tiết từng ghế nếu mixed)
                ToaTau repToa = (!ctxGhes.isEmpty()) ? ctxGhes.get(0).getToaTau() : null;
                BanVeStep6Module m = new BanVeStep6Module(
                    ctxLich, repToa, Collections.unmodifiableList(ctxGhes),
                    ctxPriceMap, orderedSeatCustomers(),
                    freezeSeatPromoMap(ctxChiTietKMsBySeat));
                m.setOnResult(result -> {
                    if (result == null) { goBack(); return; }
                    if (result instanceof String paymentType) {
                        if (!Objects.equals(ctxPaymentType, paymentType)) clearAfterStep(STEP_6);
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
                    if ("CONFIRMED".equals(result) && saveTransaction()) {
                        navigateTo(STEP_8, true);
                    }
                });
                yield m.getView();
            }

            case STEP_7B -> {
                BanVeStep7ChuyenKhoanModule m = new BanVeStep7ChuyenKhoanModule(calcTotal());
                m.setOnResult(result -> {
                    if (result == null) { goBack(); return; }
                    if ("CONFIRMED".equals(result) && saveTransaction()) {
                        navigateTo(STEP_8, true);
                    }
                });
                yield m.getView();
            }

            case STEP_8 -> {
                BanVeStep8Module m = new BanVeStep8Module(ctxSavedHoaDon);
                m.setOnResult(result -> reset());
                yield m.getView();
            }

            default -> new JPanel();
        };
    }

    private String stationId(Ga ga) {
        return ga == null ? null : ga.getMaGa();
    }

    private String lichId(Lich lich) {
        return lich == null ? null : lich.getMaLich();
    }

    private boolean samePriceDetailMap(Map<LoaiGhe, ChiTietGia> left, Map<LoaiGhe, ChiTietGia> right) {
        if (!left.keySet().equals(right.keySet())) return false;
        for (LoaiGhe loai : left.keySet()) {
            if (!Objects.equals(priceDetailId(left.get(loai)), priceDetailId(right.get(loai)))) return false;
        }
        return true;
    }

    private String priceDetailId(ChiTietGia chiTietGia) {
        return chiTietGia == null ? null : chiTietGia.getMaChiTietGia();
    }

    private boolean sameSeatList(List<Ghe> left, List<Ghe> right) {
        if (left.size() != right.size()) return false;
        for (int i = 0; i < left.size(); i++) {
            if (!Objects.equals(seatKey(left.get(i)), seatKey(right.get(i)))) return false;
        }
        return true;
    }

    private boolean sameCustomerMap(Map<String, KhachHang> left, Map<String, KhachHang> right) {
        if (!left.keySet().equals(right.keySet())) return false;
        for (String key : left.keySet()) {
            if (!Objects.equals(customerSignature(left.get(key)), customerSignature(right.get(key)))) return false;
        }
        return true;
    }

    private String customerSignature(KhachHang khachHang) {
        if (khachHang == null) return "";
        return String.join("|",
                Objects.toString(khachHang.getMaKhachHang(), ""),
                Objects.toString(khachHang.getHoTen(), ""),
                Objects.toString(khachHang.getCccd(), ""),
                Objects.toString(khachHang.getSoDienThoai(), ""),
                Objects.toString(khachHang.getEmail(), ""));
    }

    private boolean samePromotionMap(Map<String, List<ChiTietKhuyenMai>> left, Map<String, List<ChiTietKhuyenMai>> right) {
        if (!left.keySet().equals(right.keySet())) return false;
        for (String key : left.keySet()) {
            if (!promotionIds(left.get(key)).equals(promotionIds(right.get(key)))) return false;
        }
        return true;
    }

    private List<String> promotionIds(List<ChiTietKhuyenMai> promotions) {
        List<String> ids = new ArrayList<>();
        if (promotions != null) {
            for (ChiTietKhuyenMai km : promotions) ids.add(km == null ? "" : Objects.toString(km.getMaChiTietKM(), ""));
        }
        return ids;
    }

    private boolean validateBeforeForward(String targetStep) {
        return switch (targetStep) {
            case STEP_2 -> validateSearchCriteria();
            case STEP_3 -> validateScheduleAndPrices();
            case STEP_5 -> validateScheduleAndPrices() && validateSelectedSeatsAvailable();
            case STEP_5B -> validateScheduleAndPrices() && validateSelectedSeatsAvailable() && validateSeatCustomers();
            case STEP_6, STEP_7A, STEP_7B -> validateReadyForPayment();
            default -> true;
        };
    }

    private boolean validateSearchCriteria() {
        if (ctxGaDi == null || ctxGaDen == null || ctxNgayDiFrom == null || ctxNgayDiTo == null) {
            showValidationError("Thiếu thông tin tìm chuyến. Vui lòng kiểm tra ga đi, ga đến và ngày đi.");
            return false;
        }
        return true;
    }

    private boolean validateScheduleAndPrices() {
        if (ctxLich == null || ctxLich.getMaLich() == null) {
            showValidationError("Chưa chọn lịch chạy hoặc lịch chạy không hợp lệ.");
            return false;
        }
        Lich current = new DAO_Lich().findById(ctxLich.getMaLich());
        if (current == null || !current.isHoatDong()) {
            showValidationError("Lịch chạy đã bị ngừng hoặc không còn tồn tại. Vui lòng chọn lại chuyến.");
            return false;
        }
        ctxLich = current;
        if (ctxPriceMap.isEmpty() || ctxPriceDetailMap.isEmpty()) {
            showValidationError("Thông tin giá không còn hợp lệ. Vui lòng quay lại bước chọn chuyến.");
            return false;
        }
        for (Ghe ghe : ctxGhes) {
            if (unitPriceFor(ghe) <= 0.0 || priceDetailForSeat(ghe) == null) {
                showValidationError("Giá của ghế " + seatDisplay(ghe) + " không còn hợp lệ. Vui lòng chọn lại chuyến hoặc ghế.");
                return false;
            }
        }
        return true;
    }

    private boolean validateSelectedSeatsAvailable() {
        if (ctxGhes.isEmpty()) {
            showValidationError("Chưa chọn ghế để đặt vé.");
            return false;
        }
        if (ctxLich == null || ctxLich.getMaLich() == null) return false;
        if (hasExpiredCurrentHolds()) {
            handleExpiredCurrentHolds();
            return false;
        }
        for (Ghe ghe : ctxGhes) {
            String maGhe = ghe != null ? ghe.getMaGhe() : null;
            if (daoVe.existsSoldSeat(ctxLich.getMaLich(), maGhe)) {
                showValidationError("Ghế " + seatDisplay(ghe) + " vừa được bán hoặc không còn trống. Vui lòng chọn lại ghế.");
                return false;
            }
            if (currentUser != null && !daoGiuCho.hasActiveHold(currentUser.getMaNV(), ctxLich.getMaLich(), maGhe)) {
                handleExpiredCurrentHolds();
                return false;
            }
        }
        return true;
    }

    private boolean validateSeatCustomers() {
        if (ctxKhachHangBySeat.size() != ctxGhes.size()) {
            showValidationError("Mỗi ghế/vé phải có đúng một khách hàng. Vui lòng kiểm tra lại bước khách hàng.");
            return false;
        }
        for (Ghe ghe : ctxGhes) {
            KhachHang kh = ctxKhachHangBySeat.get(seatKey(ghe));
            if (kh == null || isBlank(kh.getHoTen())) {
                showValidationError("Thiếu khách hàng cho ghế " + seatDisplay(ghe) + ".");
                return false;
            }
        }
        return true;
    }

    private boolean validateSelectedPromotions() {
        LocalDate travelDate = ctxLich != null && ctxLich.getThoiGianBatDau() != null
                ? ctxLich.getThoiGianBatDau().toLocalDate() : LocalDate.now();
        for (Ghe ghe : ctxGhes) {
            String key = seatKey(ghe);
            for (ChiTietKhuyenMai km : ctxChiTietKMsBySeat.getOrDefault(key, Collections.emptyList())) {
                if (!isPromotionStillApplicable(km, ghe, travelDate)) {
                    showValidationError("Khuyến mãi cho ghế " + seatDisplay(ghe) + " không còn hợp lệ. Vui lòng kiểm tra lại bước khuyến mãi.");
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isPromotionStillApplicable(ChiTietKhuyenMai km, Ghe ghe, LocalDate travelDate) {
        if (km == null || km.getKhuyenMai() == null || !km.getKhuyenMai().isTrangThai()) return false;
        if (km.getKhuyenMai().getThoiGianBatDau() != null && travelDate.isBefore(km.getKhuyenMai().getThoiGianBatDau())) return false;
        if (km.getKhuyenMai().getThoiGianKetThuc() != null && travelDate.isAfter(km.getKhuyenMai().getThoiGianKetThuc())) return false;
        if (ctxLich != null && ctxLich.getTuyen() != null && km.getTuyen() != null
                && !Objects.equals(km.getTuyen().getMaTuyen(), ctxLich.getTuyen().getMaTuyen())) return false;
        LoaiGhe loai = ghe != null && ghe.getToaTau() != null ? ghe.getToaTau().getLoaiGhe() : null;
        return km.getLoaiGhe() == null || km.getLoaiGhe() == loai;
    }

    private boolean validateReadyForPayment() {
        return validateScheduleAndPrices()
                && validateSelectedSeatsAvailable()
                && validateSeatCustomers()
                && validateSelectedPromotions();
        // Handoff: mọi bước tiến trong wizard chạy qua validator tập trung để giữ draft nhưng vẫn bắt lỗi nền kịp thời.
        // Cảnh báo: thanh toán vẫn phải gọi validateReadyForPayment vì ghế/giá có thể đổi sau bước xác nhận.
    }

    private void showValidationError(String message) {
        NotionMessageDialog.showMessageDialog(this, message, "Dữ liệu đặt vé đã thay đổi", JOptionPane.WARNING_MESSAGE);
    }

    private String seatDisplay(Ghe ghe) {
        if (ghe == null) return "?";
        String toa = ghe.getToaTau() != null && ghe.getToaTau().getMaToaTau() != null ? ghe.getToaTau().getMaToaTau() : "?";
        return toa + " - ghế " + ghe.getSoGhe();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public boolean isCompleted() {
        return STEP_8.equals(currentStep);
    }

    public void beginNewBooking() {
        reset();
    }

    private Map<String, LocalDateTime> heldSeatsByOther() {
        String maNV = currentUser == null ? null : currentUser.getMaNV();
        String maLich = ctxLich == null ? null : ctxLich.getMaLich();
        daoGiuCho.deleteExpired();
        return daoGiuCho.findActiveHeldSeatsByOther(maNV, maLich);
    }

    private Map<String, LocalDateTime> heldSeatsByCurrentUser() {
        String maNV = currentUser == null ? null : currentUser.getMaNV();
        String maLich = ctxLich == null ? null : ctxLich.getMaLich();
        daoGiuCho.deleteExpired();
        return daoGiuCho.findActiveHeldSeatsByNhanVien(maNV, maLich);
    }

    private boolean holdSeatForCurrentUser(Ghe ghe) {
        if (ctxLich == null || currentUser == null || ghe == null) return false;
        String maGhe = ghe.getMaGhe();
        if (daoVe.existsSoldSeat(ctxLich.getMaLich(), maGhe)) {
            showValidationError("Ghế " + seatDisplay(ghe) + " vừa được bán. Vui lòng chọn ghế khác.");
            return false;
        }
        if (!daoGiuCho.tryHoldSeat(currentUser.getMaNV(), ctxLich.getMaLich(), maGhe, HOLD_TTL_SECONDS)) {
            showValidationError("Ghế " + seatDisplay(ghe) + " đã được nhân viên khác giữ chỗ. Vui lòng chọn ghế khác.");
            stepCache.remove(STEP_3);
            return false;
        }
        return true;
        // Handoff: giữ chỗ ghi DB ngay lúc chọn ghế để nhân viên khác thấy tức thì.
        // Risk: TTL hiện là 5 phút; nếu đổi test nhanh chỉ cần đổi HOLD_TTL_SECONDS.
    }

    private boolean releaseSeatForCurrentUser(Ghe ghe) {
        if (ctxLich == null || currentUser == null || ghe == null) return false;
        daoGiuCho.releaseSeat(currentUser.getMaNV(), ctxLich.getMaLich(), ghe.getMaGhe());
        return true;
    }

    private boolean hasExpiredCurrentHolds() {
        return currentUser != null && ctxLich != null
                && daoGiuCho.hasExpiredHold(currentUser.getMaNV(), ctxLich.getMaLich());
    }

    private void handleExpiredCurrentHolds() {
        releaseCurrentHolds();
        ctxGhes.clear();
        clearAfterStep(STEP_3);
        stepCache.remove(STEP_3);
        showValidationError("Giữ chỗ đã hết hạn. Hệ thống đã xóa giữ chỗ cũ, vui lòng chọn ghế lại.");
        SwingUtilities.invokeLater(() -> navigateTo(STEP_3, false));
        // Handoff: kiểm tra hết hạn khi qua bước để cache sau chọn ghế không còn dữ liệu cũ.
        // Risk: nếu user đứng yên ở Step 3 quá TTL, lỗi được chặn ở lần đi tiếp kế tiếp.
    }

    private void releaseCurrentHolds() {
        if (currentUser == null || ctxLich == null) return;
        daoGiuCho.deleteByNhanVienAndLich(currentUser.getMaNV(), ctxLich.getMaLich());
    }

    // =========================================================================
    //  BUSINESS LOGIC
    // =========================================================================

    /** Đơn giá gốc của một ghế, dựa theo LoaiGhe của toa chứa ghế đó. */
    private double unitPriceFor(Ghe g) {
        if (g == null || g.getToaTau() == null) return 0.0;
        return ctxPriceMap.getOrDefault(g.getToaTau().getLoaiGhe(), 0.0);
    }

    private ChiTietGia priceDetailForSeat(Ghe ghe) {
        if (ghe == null || ghe.getToaTau() == null) return null;
        return ctxPriceDetailMap.get(ghe.getToaTau().getLoaiGhe());
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

    private List<KhachHang> orderedSeatCustomers() {
        List<KhachHang> customers = new ArrayList<>();
        for (Ghe ghe : ctxGhes) {
            KhachHang kh = ctxKhachHangBySeat.get(seatKey(ghe));
            if (kh != null) customers.add(kh);
        }
        return Collections.unmodifiableList(customers);
        // Handoff: thứ tự khách luôn đi theo thứ tự ghế để các bước xác nhận không tách rời vé/khách.
        // Cảnh báo: nếu đổi cách sắp xếp ghế ở Step 5 thì phải giữ đồng bộ key seatKey tại đây.
    }

    private BigDecimal calcTotal() {
        if (ctxGhes.isEmpty()) return BigDecimal.ZERO;
        double total = 0.0;
        for (Ghe g : ctxGhes) total += finalPriceForSeat(g);
        return BigDecimal.valueOf(total);
    }

    private boolean saveTransaction() {
        java.sql.Connection txCon = null;
        boolean oldAutoCommit = true;
        try {
            // 0. Kiểm tra giá hợp lệ trước khi ghi DB — DB có CHECK giaTien > 0.
            for (Ghe g : ctxGhes) {
                double finalPrice = finalPriceForSeat(g);
                ChiTietGia priceDetail = priceDetailForSeat(g);
                if (finalPrice <= 0.0 || priceDetail == null) {
                    NotionMessageDialog.showMessageDialog(this,
                        "Không tìm được giá hợp lệ cho ghế " + (g != null ? g.getMaGhe() : "?") +
                        ".\nVui lòng kiểm tra bảng giá tuyến cho loại ghế này trước khi bán vé.",
                        "Lỗi dữ liệu giá", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
            txCon = com.connectDB.ConnectDB.getCon();
            if (txCon == null) throw new IllegalStateException("Chưa kết nối cơ sở dữ liệu");
            oldAutoCommit = txCon.getAutoCommit();
            txCon.setAutoCommit(false);
            if (!validateReadyForPayment()) {
                txCon.rollback();
                txCon.setAutoCommit(oldAutoCommit);
                return false;
            }

            // 1. Lưu / cập nhật từng KhachHang theo từng ghế đã chọn
            Map<String, KhachHang> savedKhachHangBySeat = new LinkedHashMap<>();
            for (Ghe ghe : ctxGhes) {
                String key = seatKey(ghe);
                KhachHang kh = ctxKhachHangBySeat.get(key);
                if (kh == null) {
                    throw new IllegalStateException("Thiếu khách hàng cho ghế " + key);
                }
                boolean khOk;
                if (kh.getMaKhachHang() != null) {
                    khOk = daoKH.update(kh);
                } else {
                    kh.setMaKhachHang(genId("KH"));
                    khOk = daoKH.insert(kh);
                }
                if (!khOk) throw new IllegalStateException("Không thể lưu khách hàng " + key);
                savedKhachHangBySeat.put(key, kh);
            }

            // 2. Tạo HoaDon; khách hàng của từng vé được lưu ở ChiTietHoaDon
            String maHD = daoHD.phatSinhMaHoaDon();
            HoaDon hd   = new HoaDon(maHD, currentUser, LocalDateTime.now());
            if (!daoHD.insert(hd)) throw new IllegalStateException("Không thể tạo hóa đơn " + maHD);
            ctxSavedHoaDon = hd;

            // 3. Tạo Ve + ChiTietHoaDon (+ ApDungKM) — mỗi chi tiết hóa đơn gắn đúng khách của ghế
            for (Ghe ghe : ctxGhes) {
                double finalPrice = finalPriceForSeat(ghe);
                ChiTietGia priceDetail = priceDetailForSeat(ghe);
                List<ChiTietKhuyenMai> appliedPromos = selectedPromotionsForSeat(ghe);

                if (daoVe.existsSoldSeat(ctxLich.getMaLich(), ghe != null ? ghe.getMaGhe() : null)) {
                    throw new IllegalStateException("Ghế " + seatDisplay(ghe) + " vừa được bán, vui lòng chọn lại ghế");
                }
                if (currentUser != null && !daoGiuCho.hasActiveHold(currentUser.getMaNV(), ctxLich.getMaLich(), ghe.getMaGhe())) {
                    throw new IllegalStateException("Giữ chỗ của ghế " + seatDisplay(ghe) + " đã hết hạn, vui lòng chọn lại ghế");
                }
                Ve ve = new Ve(genId("VE"), ctxLich, ghe, TrangThaiVe.DA_BAN, null, null);
                if (!daoVe.insert(ve)) throw new IllegalStateException("Không thể tạo vé cho ghế " + seatKey(ghe));

                KhachHang khachHang = savedKhachHangBySeat.get(seatKey(ghe));
                ChiTietHoaDon cthd = new ChiTietHoaDon(
                    genId("CTHD"), hd, ve, khachHang, priceDetail, BigDecimal.valueOf(finalPrice));
                if (!daoCTHD.insert(cthd)) throw new IllegalStateException("Không thể tạo chi tiết hóa đơn cho vé " + ve.getMaVe());

                for (ChiTietKhuyenMai km : appliedPromos) {
                    ApDungKM adkm = new ApDungKM(genId("ADKM"), cthd, km);
                    if (!daoADKM.insert(adkm)) throw new IllegalStateException("Không thể lưu khuyến mãi cho vé " + ve.getMaVe());
                }
            }
            txCon.commit();
            txCon.setAutoCommit(oldAutoCommit);
            releaseCurrentHolds();
            return true;

        } catch (Exception ex) {
            try {
                if (txCon != null) {
                    txCon.rollback();
                    txCon.setAutoCommit(oldAutoCommit);
                }
            } catch (Exception rollbackEx) {
                rollbackEx.printStackTrace();
            }
            ex.printStackTrace();
            NotionMessageDialog.showMessageDialog(this,
                "Lỗi khi lưu giao dịch: " + ex.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private String genId(String prefix) {
        return MaTuDong.generate(prefix);
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
        releaseCurrentHolds();
        ctxGaDi   = null; ctxGaDen = null; ctxNgayDiFrom = null; ctxNgayDiTo = null;
        ctxLich   = null; ctxPriceMap.clear(); ctxPriceDetailMap.clear();
        ctxGhes.clear();
        ctxKhachHangBySeat.clear();
        ctxChiTietKMsBySeat.clear();
        ctxPaymentType = null;
        ctxSavedHoaDon = null;
        stepHistory.clear();
        stepCache.clear();
        navigateTo(STEP_1, false);
    }
}
