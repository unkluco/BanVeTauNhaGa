package com.modules;

import com.dao.DAO_ChiTietKhuyenMai;
import com.dao.DAO_KhuyenMai;
import com.entity.ChiTietKhuyenMai;
import com.entity.Ghe;
import com.entity.KhuyenMai;
import com.entity.Tuyen;
import com.enums.LoaiGhe;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Bước 5b — Chọn khuyến mãi theo từng ghế.
 * Cột trái: danh sách ghế đã chọn.
 * Cột phải: khuyến mãi khả dụng cho ghế đang chọn (đã chọn/chưa chọn).
 * Output:
 *   - Map<String, List<ChiTietKhuyenMai>>: maGhe -> danh sách KM đã tick cho ghế đó
 *   - null: quay lại
 */
public class BanVeStep5bModule extends JPanel implements AppModule {

    private Consumer<Object> callback;

    private final Tuyen tuyen;
    private final List<Ghe> ghes;
    private final Map<LoaiGhe, Double> priceMap;
    private final LocalDateTime departureTime;

    private final DAO_KhuyenMai daoKM = new DAO_KhuyenMai();
    private final DAO_ChiTietKhuyenMai daoCTKM = new DAO_ChiTietKhuyenMai();

    // Design tokens
    private static final Color PRIMARY       = NotionTheme.ACCENT;
    private static final Color SURFACE       = NotionTheme.PAGE;
    private static final Color CARD_BG       = AppColors.SURFACE;
    private static final Color ON_SURFACE    = NotionTheme.TEXT;
    private static final Color ON_SURF_VAR   = NotionTheme.TEXT_MUTED;
    private static final Color OUTLINE       = NotionTheme.BORDER;
    private static final Color PRIMARY_LIGHT = NotionTheme.ACCENT_SOFT;
    private static final Color PROMO_BG      = AppColors.WARNING_LIGHT;
    private static final Color PROMO_BORDER  = AppColors.WARNING;
    private static final Color PROMO_FG      = AppColors.WARNING_DARK;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat VND_FMT = NumberFormat.getInstance(new Locale("vi", "VN"));

    // Promotions valid by route/time
    private final List<ChiTietKhuyenMai> validPromos = new ArrayList<>();
    // Promotions allowed for each seat
    private final Map<String, List<ChiTietKhuyenMai>> promosBySeatKey = new LinkedHashMap<>();
    // Selected promotions for each seat (store by maChiTietKM for stable toggle state)
    private final Map<String, Set<String>> selectedPromoIdsBySeat = new LinkedHashMap<>();
    // Keep first seen promo by id for output mapping
    private final Map<String, ChiTietKhuyenMai> promoById = new LinkedHashMap<>();

    private Ghe selectedSeat;
    private JList<Ghe> seatList;
    private JPanel promoListPanel;
    private JLabel promoPanelTitle;
    private JLabel promoPanelHint;

    private JButton btnSubmit, btnCancel;
    private JPanel btnPanel;

    public BanVeStep5bModule(Tuyen tuyen, List<Ghe> ghes, Map<LoaiGhe, Double> priceMap,
                             LocalDateTime departureTime) {
        this.tuyen = tuyen;
        this.ghes = (ghes == null) ? Collections.emptyList() : new ArrayList<>(ghes);
        this.priceMap = (priceMap == null) ? Collections.emptyMap() : priceMap;
        this.departureTime = (departureTime == null) ? LocalDateTime.now() : departureTime;
        setLayout(new BorderLayout());
        setBackground(SURFACE);
        loadPromos();
        buildUI();
    }

    private void loadPromos() {
        validPromos.clear();
        promoById.clear();

        String maTuyen = (tuyen != null) ? tuyen.getMaTuyen() : null;
        LocalDate refDate = departureTime.toLocalDate();

        for (KhuyenMai km : daoKM.getAll()) {
            if (km == null || !km.isTrangThai()) continue;
            if (km.getThoiGianBatDau() != null && refDate.isBefore(km.getThoiGianBatDau())) continue;
            if (km.getThoiGianKetThuc() != null && refDate.isAfter(km.getThoiGianKetThuc())) continue;

            for (ChiTietKhuyenMai ctg : daoCTKM.findByKhuyenMai(km.getMaKhuyenMai())) {
                if (ctg == null) continue;
                boolean tuyenOk = ctg.getTuyen() == null
                    || (maTuyen != null && ctg.getTuyen().getMaTuyen() != null
                    && ctg.getTuyen().getMaTuyen().equals(maTuyen));
                if (!tuyenOk) continue;

                String kmId = promoId(ctg);
                if (!promoById.containsKey(kmId)) {
                    promoById.put(kmId, ctg);
                    validPromos.add(ctg);
                }
            }
        }

        promosBySeatKey.clear();
        selectedPromoIdsBySeat.clear();
        for (Ghe ghe : ghes) {
            String seatKey = seatKey(ghe);
            LoaiGhe seatType = (ghe != null && ghe.getToaTau() != null) ? ghe.getToaTau().getLoaiGhe() : null;
            List<ChiTietKhuyenMai> seatPromos = new ArrayList<>();
            for (ChiTietKhuyenMai promo : validPromos) {
                if (promo.getLoaiGhe() == null || promo.getLoaiGhe() == seatType) {
                    seatPromos.add(promo);
                }
            }
            promosBySeatKey.put(seatKey, seatPromos);
            selectedPromoIdsBySeat.put(seatKey, new LinkedHashSet<>());
        }
    }

    private void buildUI() {
        add(buildHeaderBar(), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(16, 0));
        body.setBackground(SURFACE);
        body.setBorder(new EmptyBorder(20, 24, 20, 24));

        body.add(buildSeatPanel(), BorderLayout.WEST);
        body.add(buildPromoPanel(), BorderLayout.CENTER);

        add(body, BorderLayout.CENTER);

        btnSubmit = new JButton("Tiếp theo →");
        styleBtn(btnSubmit, true);
        btnSubmit.addActionListener(e -> execute());

        btnCancel = new JButton("← Quay lại");
        styleBtn(btnCancel, false);
        btnCancel.addActionListener(e -> { if (callback != null) callback.accept(null); });

        btnPanel = BookingStepUi.createFooter();
        btnPanel.add(btnCancel);
        btnPanel.add(btnSubmit);
        btnPanel.setVisible(false);
        add(btnPanel, BorderLayout.SOUTH);

        if (!ghes.isEmpty()) {
            seatList.setSelectedIndex(0);
        } else {
            refreshPromoPanel();
        }
    }

    private JPanel buildHeaderBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 12));
        bar.setBackground(PRIMARY_LIGHT);
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, OUTLINE));
        JLabel lbl = new JLabel("Chọn khuyến mãi theo từng vé");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl.setForeground(PRIMARY);
        bar.add(lbl);
        return bar;
    }

    private JPanel buildSeatPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(SURFACE);
        panel.setPreferredSize(new Dimension(320, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(OUTLINE, 1),
            new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel title = new JLabel("Ghế đã chọn (" + ghes.size() + ")");
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(ON_SURFACE);
        panel.add(title, BorderLayout.NORTH);

        DefaultListModel<Ghe> model = new DefaultListModel<>();
        for (Ghe ghe : ghes) model.addElement(ghe);

        seatList = new JList<>(model);
        seatList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        seatList.setBackground(CARD_BG);
        seatList.setFixedCellHeight(64);
        seatList.setBorder(BorderFactory.createLineBorder(OUTLINE, 1));
        seatList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> buildSeatCell(value, isSelected));
        seatList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectedSeat = seatList.getSelectedValue();
                refreshPromoPanel();
            }
        });

        JScrollPane seatScroll = new JScrollPane(seatList);
        seatScroll.setBorder(BorderFactory.createEmptyBorder());
        seatScroll.getViewport().setBackground(CARD_BG);
        panel.add(seatScroll, BorderLayout.CENTER);
        return panel;
    }

    private Component buildSeatCell(Ghe ghe, boolean selected) {
        JPanel cell = new JPanel(new BorderLayout());
        cell.setBorder(new EmptyBorder(8, 10, 8, 10));
        cell.setBackground(selected ? PRIMARY_LIGHT : CARD_BG);

        JLabel lblTitle = new JLabel(seatLabel(ghe));
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(selected ? PRIMARY : ON_SURFACE);

        String type = (ghe != null && ghe.getToaTau() != null && ghe.getToaTau().getLoaiGhe() != null)
            ? ghe.getToaTau().getLoaiGhe().toString() : "Không rõ loại";
        double listPrice = unitPriceFor(ghe);
        double discountedPrice = finalPriceForSeat(ghe);
        String priceMeta = type + "  •  " + fmt(listPrice);
        if (discountedPrice < listPrice - 0.5) {
            priceMeta += "  →  Sau giảm " + fmt(discountedPrice);
        }
        JLabel lblMeta = new JLabel(priceMeta);
        lblMeta.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblMeta.setForeground(ON_SURF_VAR);

        cell.add(lblTitle, BorderLayout.NORTH);
        cell.add(lblMeta, BorderLayout.SOUTH);
        return cell;
    }

    private JPanel buildPromoPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(OUTLINE, 1),
            new EmptyBorder(12, 14, 12, 14)
        ));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBackground(SURFACE);

        promoPanelTitle = new JLabel("Khuyến mãi cho từng ghế");
        promoPanelTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        promoPanelTitle.setForeground(ON_SURFACE);
        promoPanelTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(promoPanelTitle);
        top.add(Box.createVerticalStrut(4));

        promoPanelHint = new JLabel("Chọn ghế bên trái để tick khuyến mãi tương ứng.");
        promoPanelHint.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        promoPanelHint.setForeground(ON_SURF_VAR);
        promoPanelHint.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(promoPanelHint);

        panel.add(top, BorderLayout.NORTH);

        promoListPanel = new JPanel();
        promoListPanel.setLayout(new BoxLayout(promoListPanel, BoxLayout.Y_AXIS));
        promoListPanel.setBackground(SURFACE);
        promoListPanel.setBorder(new EmptyBorder(6, 2, 6, 2));

        JScrollPane promoScroll = new JScrollPane(promoListPanel);
        promoScroll.setBorder(BorderFactory.createEmptyBorder());
        promoScroll.getViewport().setBackground(SURFACE);
        panel.add(promoScroll, BorderLayout.CENTER);

        return panel;
    }

    private void refreshPromoPanel() {
        promoListPanel.removeAll();

        if (selectedSeat == null) {
            promoPanelTitle.setText("Khuyến mãi theo ghế");
            promoPanelHint.setText("Chưa có ghế nào được chọn.");
            promoListPanel.add(emptyState("Danh sách ghế đang trống."));
            repaintPromoPanel();
            return;
        }

        String seatKey = seatKey(selectedSeat);
        String seatTitle = seatLabel(selectedSeat);
        List<ChiTietKhuyenMai> seatPromos = promosBySeatKey.getOrDefault(seatKey, Collections.emptyList());
        Set<String> selectedIds = selectedPromoIdsBySeat.computeIfAbsent(seatKey, k -> new LinkedHashSet<>());

        promoPanelTitle.setText("Khuyến mãi cho " + seatTitle);
        promoPanelHint.setText("Tick để áp dụng cho vé này. Bỏ tick = không áp dụng.");

        if (seatPromos.isEmpty()) {
            promoListPanel.add(emptyState("Ghế này hiện không có khuyến mãi phù hợp."));
            repaintPromoPanel();
            return;
        }

        for (ChiTietKhuyenMai promo : seatPromos) {
            promoListPanel.add(buildPromoCard(promo, selectedIds));
            promoListPanel.add(Box.createVerticalStrut(8));
        }

        repaintPromoPanel();
    }

    private JPanel buildPromoCard(ChiTietKhuyenMai promo, Set<String> selectedIds) {
        String id = promoId(promo);
        JCheckBox cb = new JCheckBox();
        cb.setBackground(PROMO_BG);
        cb.setFocusPainted(false);
        cb.setSelected(selectedIds.contains(id));
        cb.addActionListener(e -> {
            if (cb.isSelected()) selectedIds.add(id);
            else selectedIds.remove(id);
            refreshPromoPanel();
            if (seatList != null) seatList.repaint();
        });

        JPanel card = new JPanel(new BorderLayout(14, 0));
        card.setBackground(PROMO_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PROMO_BORDER, 1),
            new EmptyBorder(10, 14, 10, 14)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 128));
        card.add(cb, BorderLayout.WEST);

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBackground(PROMO_BG);

        int pct = (int) Math.round(clampDiscount(promo.getPhanTramGiam()) * 100.0);
        JLabel lblName = new JLabel(promoNameFor(promo) + "  —  Giảm " + pct + "%");
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblName.setForeground(PROMO_FG);
        info.add(lblName);

        if (selectedSeat != null) {
            double saving = savingForPromoWithCurrentStack(selectedSeat, promo, selectedIds);
            JLabel lblSaving = new JLabel("Áp dụng cho vé: −" + VND_FMT.format((long) saving) + " ₫");
            lblSaving.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblSaving.setForeground(PROMO_FG);
            info.add(lblSaving);
        }

        if (promo.getKhuyenMai() != null) {
            KhuyenMai km = promo.getKhuyenMai();
            if (km.getThoiGianBatDau() != null && km.getThoiGianKetThuc() != null) {
                JLabel lblPeriod = new JLabel("Áp dụng: "
                    + km.getThoiGianBatDau().format(DT_FMT)
                    + " → " + km.getThoiGianKetThuc().format(DT_FMT));
                lblPeriod.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                lblPeriod.setForeground(ON_SURF_VAR);
                info.add(lblPeriod);
            }
        }

        String scope = "Phạm vi: "
            + (promo.getTuyen() != null ? promo.getTuyen().getMaTuyen() : "Tất cả tuyến")
            + "  •  "
            + (promo.getLoaiGhe() != null ? promo.getLoaiGhe().toString() : "Tất cả loại ghế");
        JLabel lblScope = new JLabel(scope);
        lblScope.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblScope.setForeground(ON_SURF_VAR);
        info.add(lblScope);

        card.add(info, BorderLayout.CENTER);
        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { cb.doClick(); }
        });
        return card;
    }

    private JPanel emptyState(String text) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(OUTLINE, 1),
            new EmptyBorder(18, 16, 18, 16)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(ON_SURF_VAR);
        panel.add(lbl, BorderLayout.CENTER);
        return panel;
    }

    private void repaintPromoPanel() {
        promoListPanel.revalidate();
        promoListPanel.repaint();
    }

    private void execute() {
        Map<String, List<ChiTietKhuyenMai>> result = new LinkedHashMap<>();
        for (Ghe ghe : ghes) {
            String seatKey = seatKey(ghe);
            List<ChiTietKhuyenMai> available = promosBySeatKey.getOrDefault(seatKey, Collections.emptyList());
            Set<String> selectedIds = selectedPromoIdsBySeat.getOrDefault(seatKey, Collections.emptySet());
            List<ChiTietKhuyenMai> chosen = new ArrayList<>();
            for (ChiTietKhuyenMai promo : available) {
                String id = promoId(promo);
                if (selectedIds.contains(id)) chosen.add(promoById.getOrDefault(id, promo));
            }
            result.put(seatKey, chosen);
        }
        if (callback != null) callback.accept(result);
    }

    private String promoNameFor(ChiTietKhuyenMai km) {
        if (km == null) return "Khuyến mãi";
        if (km.getTenChiTiet() != null && !km.getTenChiTiet().isBlank()) return km.getTenChiTiet();
        if (km.getKhuyenMai() != null && km.getKhuyenMai().getTenKhuyenMai() != null
            && !km.getKhuyenMai().getTenKhuyenMai().isBlank()) return km.getKhuyenMai().getTenKhuyenMai();
        return "Khuyến mãi";
    }

    private double clampDiscount(double discount) {
        if (Double.isNaN(discount)) return 0.0;
        return Math.max(0.0, Math.min(1.0, discount));
    }

    private double unitPriceFor(Ghe ghe) {
        if (ghe == null || ghe.getToaTau() == null || ghe.getToaTau().getLoaiGhe() == null) return 0.0;
        return priceMap.getOrDefault(ghe.getToaTau().getLoaiGhe(), 0.0);
    }

    private double finalPriceForSeat(Ghe ghe) {
        double base = unitPriceFor(ghe);
        String seatKey = seatKey(ghe);
        Set<String> selectedIds = selectedPromoIdsBySeat.getOrDefault(seatKey, Collections.emptySet());
        if (selectedIds.isEmpty()) return base;

        List<ChiTietKhuyenMai> availablePromos = promosBySeatKey.getOrDefault(seatKey, Collections.emptyList());
        double finalPrice = base;
        for (ChiTietKhuyenMai promo : availablePromos) {
            if (promo == null) continue;
            if (!selectedIds.contains(promoId(promo))) continue;
            finalPrice *= (1.0 - clampDiscount(promo.getPhanTramGiam()));
        }
        return finalPrice;
    }

    /**
     * Giá trị giảm của 1 khuyến mãi được tính trên mức giá sau các KM đã chọn trước nó.
     * Công thức là nhân dồn, không cộng dồn.
     */
    private double savingForPromoWithCurrentStack(Ghe ghe, ChiTietKhuyenMai targetPromo, Set<String> selectedIds) {
        if (ghe == null || targetPromo == null) return 0.0;
        String seatKey = seatKey(ghe);
        List<ChiTietKhuyenMai> orderedPromos = promosBySeatKey.getOrDefault(seatKey, Collections.emptyList());
        if (orderedPromos.isEmpty()) return 0.0;

        String targetId = promoId(targetPromo);
        double runningPrice = unitPriceFor(ghe);
        for (ChiTietKhuyenMai promo : orderedPromos) {
            if (promo == null) continue;
            String promoId = promoId(promo);
            if (targetId.equals(promoId)) {
                return runningPrice * clampDiscount(promo.getPhanTramGiam());
            }
            if (selectedIds.contains(promoId)) {
                runningPrice *= (1.0 - clampDiscount(promo.getPhanTramGiam()));
            }
        }
        return 0.0;
    }

    private String seatLabel(Ghe ghe) {
        if (ghe == null) return "Ghế ?";
        String toa = (ghe.getToaTau() != null && ghe.getToaTau().getMaToaTau() != null)
            ? ghe.getToaTau().getMaToaTau() : "Toa ?";
        return toa + " - Ghế " + ghe.getSoGhe();
    }

    private String seatKey(Ghe ghe) {
        if (ghe == null) return "";
        if (ghe.getMaGhe() != null && !ghe.getMaGhe().isBlank()) return ghe.getMaGhe();
        String toa = (ghe.getToaTau() != null && ghe.getToaTau().getMaToaTau() != null)
            ? ghe.getToaTau().getMaToaTau() : "TOA";
        return toa + "-SEAT-" + ghe.getSoGhe();
    }

    private String promoId(ChiTietKhuyenMai promo) {
        if (promo == null) return "";
        if (promo.getMaChiTietKM() != null && !promo.getMaChiTietKM().isBlank()) return promo.getMaChiTietKM();
        return String.valueOf(System.identityHashCode(promo));
    }

    private String fmt(double amount) {
        return VND_FMT.format((long) amount) + " ₫";
    }

    private void styleBtn(JButton btn, boolean primary) {
        BookingStepUi.styleActionButton(btn, primary);
    }

    @Override public String getTitle() { return "Bước 5b – Khuyến mãi"; }
    @Override public JPanel getView() { return this; }

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
        for (Set<String> selected : selectedPromoIdsBySeat.values()) selected.clear();
        if (seatList != null && seatList.getModel().getSize() > 0) seatList.setSelectedIndex(0);
        refreshPromoPanel();
    }
}
