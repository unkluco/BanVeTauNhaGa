package com.modules;

import com.entity.ChiTietKhuyenMai;
import com.entity.Ghe;
import com.entity.KhachHang;
import com.entity.Lich;
import com.entity.ToaTau;
import com.enums.LoaiGhe;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Bước 6 — Xác nhận hóa đơn & chọn phương thức thanh toán.
 * Input: lich, toa, ghes, priceMap, khachHang.
 * Output: String "TIEN_MAT" hoặc "CHUYEN_KHOAN".
 */
public class BanVeStep6Module extends JPanel implements AppModule {

    private Consumer<Object> callback;

    private final Lich                  lich;
    private final ToaTau                toa;
    private final List<Ghe>             ghes;
    private final Map<LoaiGhe, Double>  priceMap;
    private final List<KhachHang>       khachHangs;
    private final Map<String, List<ChiTietKhuyenMai>> chiTietKMsBySeat;

    private static final NumberFormat      VND_FMT = NumberFormat.getInstance(new Locale("vi", "VN"));
    private static final DateTimeFormatter DT_FMT  = DateTimeFormatter.ofPattern("HH:mm  dd/MM/yyyy");

    // Design tokens
    private static final Color PRIMARY       = NotionTheme.ACCENT;
    private static final Color SURFACE       = NotionTheme.PAGE;
    private static final Color CARD_BG       = AppColors.SURFACE;
    private static final Color ON_SURFACE    = NotionTheme.TEXT;
    private static final Color ON_SURF_VAR   = NotionTheme.TEXT_MUTED;
    private static final Color OUTLINE       = NotionTheme.BORDER;
    private static final Color PRIMARY_LIGHT = NotionTheme.ACCENT_SOFT;
    private static final Color AMOUNT_COLOR  = AppColors.SUCCESS_DARK;
    private static final Color DIVIDER       = NotionTheme.PAGE;

    private ButtonGroup   bgPayment;
    private JRadioButton  rbTienMat, rbChuyenKhoan;
    private JButton       btnSubmit, btnCancel;
    private JPanel        btnPanel;

    // =========================================================================
    //  CONSTRUCTOR
    // =========================================================================

    public BanVeStep6Module(Lich lich, ToaTau toa, List<Ghe> ghes,
                            Map<LoaiGhe, Double> priceMap, List<KhachHang> khachHangs,
                            Map<String, List<ChiTietKhuyenMai>> chiTietKMsBySeat) {
        this.lich       = lich;
        this.toa        = toa;
        this.ghes       = ghes;
        this.priceMap   = priceMap;
        this.khachHangs = khachHangs;
        this.chiTietKMsBySeat = (chiTietKMsBySeat == null)
            ? java.util.Collections.emptyMap()
            : chiTietKMsBySeat;
        setLayout(new BorderLayout());
        setBackground(SURFACE);
        buildUI();
    }

    // =========================================================================
    //  BUILD UI
    // =========================================================================

    private void buildUI() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 12));
        header.setBackground(PRIMARY_LIGHT);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, OUTLINE));
        JLabel hdr = new JLabel("Xác nhận đặt vé");
        hdr.setFont(new Font("Segoe UI", Font.BOLD, 15));
        hdr.setForeground(PRIMARY);
        header.add(hdr);
        add(header, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(SURFACE);
        body.setBorder(new EmptyBorder(20, 40, 20, 40));

        body.add(buildTripCard());
        body.add(Box.createVerticalStrut(16));
        body.add(buildSeatsCard());
        body.add(Box.createVerticalStrut(16));
        body.add(buildCustomerCard());
        body.add(Box.createVerticalStrut(16));
        body.add(buildPaymentCard());
        body.add(Box.createVerticalStrut(8));

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(SURFACE);
        add(scroll, BorderLayout.CENTER);

        btnSubmit = new JButton("Xác nhận thanh toán →");
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
    }

    private JPanel buildTripCard() {
        JPanel card = card("Thông tin chuyến");

        String gaDi  = lich.getTuyen() != null && lich.getTuyen().getGaDi()  != null
            ? lich.getTuyen().getGaDi().getTenGa()  : "—";
        String gaDen = lich.getTuyen() != null && lich.getTuyen().getGaDen() != null
            ? lich.getTuyen().getGaDen().getTenGa() : "—";
        String tgBD  = lich.getThoiGianBatDau() != null
            ? lich.getThoiGianBatDau().format(DT_FMT) : "—";
        String tgChay = lich.getThoiGianChay() != null
            ? lich.getThoiGianChay() + " phút" : "—";

        addInfoRow(card, "Tuyến:",       gaDi + "  →  " + gaDen, PRIMARY);
        addInfoRow(card, "Giờ khởi hành:", tgBD, ON_SURFACE);
        addInfoRow(card, "Thời gian đi:",   tgChay, ON_SURFACE);
        addInfoRow(card, "Mã lịch:",       lich.getMaLich(), ON_SURFACE);
        return card;
    }

    private JPanel buildSeatsCard() {
        JPanel card = card("Chi tiết vé");

        // Tính tổng giá gốc + tổng sau giảm theo từng ghế (khuyến mãi áp theo chi tiết hóa đơn)
        double total = 0.0;
        double finalTotal = 0.0;
        boolean mixed = false;
        LoaiGhe firstLoai = (!ghes.isEmpty() && ghes.get(0).getToaTau() != null)
            ? ghes.get(0).getToaTau().getLoaiGhe() : null;
        Map<String, PromoUsage> promoUsageMap = new LinkedHashMap<>();
        List<SeatPromoSummary> seatPromoSummaries = new ArrayList<>();

        for (Ghe g : ghes) {
            LoaiGhe l = g.getToaTau() != null ? g.getToaTau().getLoaiGhe() : null;
            if (l != firstLoai) mixed = true;
            double base = unitPriceFor(g);
            total += base;

            double seatFinal = base;
            List<ChiTietKhuyenMai> seatPromotions = selectedPromotionsForSeat(g);
            seatPromoSummaries.add(new SeatPromoSummary(g, seatPromotions));
            for (ChiTietKhuyenMai km : seatPromotions) {
                seatFinal *= (1.0 - clampDiscount(km.getPhanTramGiam()));
                String id = promoId(km);
                PromoUsage usage = promoUsageMap.computeIfAbsent(id, x -> new PromoUsage(km));
                usage.seatCount++;
            }
            finalTotal += seatFinal;
        }

        if (!mixed) {
            // Toàn bộ ghế cùng loại — hiển thị gọn như trước
            double unitPrice = firstLoai != null ? priceMap.getOrDefault(firstLoai, 0.0) : 0.0;
            String toaLabel = toa != null ? toa.getMaToaTau()
                : (!ghes.isEmpty() && ghes.get(0).getToaTau() != null
                    ? ghes.get(0).getToaTau().getMaToaTau() : "—");
            addInfoRow(card, "Toa:",      toaLabel, ON_SURFACE);
            addInfoRow(card, "Loại ghế:", firstLoai != null ? firstLoai.toString() : "—", ON_SURFACE);

            StringBuilder gheStr = new StringBuilder();
            for (int i = 0; i < ghes.size(); i++) {
                if (i > 0) gheStr.append(", ");
                gheStr.append("Ghế ").append(ghes.get(i).getSoGhe());
            }
            addInfoRow(card, "Ghế:",          gheStr.toString(), ON_SURFACE);
            addInfoRow(card, "Đơn giá gốc:",  fmt(unitPrice), ON_SURFACE);
            addInfoRow(card, "Số lượng:",     ghes.size() + " ghế", ON_SURFACE);
        } else {
            // Ghế thuộc nhiều loại — hiển thị từng ghế kèm giá riêng
            addInfoRow(card, "Số lượng:", ghes.size() + " ghế (nhiều loại)", ON_SURFACE);
            for (Ghe g : ghes) {
                LoaiGhe l = g.getToaTau() != null ? g.getToaTau().getLoaiGhe() : null;
                String toaMa = g.getToaTau() != null ? g.getToaTau().getMaToaTau() : "—";
                double price = l != null ? priceMap.getOrDefault(l, 0.0) : 0.0;
                String label = "Ghế " + g.getSoGhe() + " (" + toaMa + " — " + (l != null ? l : "?") + "):";
                addInfoRow(card, label, fmt(price), ON_SURFACE);
            }
        }

        // Divider
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(OUTLINE);
        card.add(sep);
        card.add(Box.createVerticalStrut(8));

        if (!promoUsageMap.isEmpty()) {
            double discountAmt = total - finalTotal;
            addInfoRow(card, "Tạm tính:",   fmt(total), ON_SURFACE);
            addSeatPromotionRows(card, seatPromoSummaries);
            addInfoRow(card, "Giảm:",       "−" + fmt(discountAmt), discountAmt > 0.0 ? AppColors.WARNING : ON_SURF_VAR);
            addInfoRow(card, "Tổng tiền:",  fmt(finalTotal), AMOUNT_COLOR, true);
        } else {
            addInfoRow(card, "Tổng tiền:", fmt(total), AMOUNT_COLOR, true);
        }
        return card;
    }

    private JPanel buildCustomerCard() {
        JPanel card = card("Khách hàng theo từng vé" + (khachHangs != null && khachHangs.size() > 1
                ? " (" + khachHangs.size() + " khách)" : ""));
        if (khachHangs == null || khachHangs.isEmpty()) {
            addInfoRow(card, "", "Chưa có thông tin khách hàng", ON_SURF_VAR);
            return card;
        }
        if (khachHangs.size() == 1) {
            KhachHang kh = khachHangs.get(0);
            addInfoRow(card, "Họ và tên:", kh.getHoTen(), ON_SURFACE);
            addInfoRow(card, "CCCD:",      kh.getCccd(), ON_SURFACE);
            addInfoRow(card, "SĐT:",       kh.getSoDienThoai(), ON_SURFACE);
            return card;
        }
        for (int i = 0; i < khachHangs.size(); i++) {
            KhachHang kh = khachHangs.get(i);
            JLabel idxLbl = new JLabel(seatCustomerLabel(i));
            idxLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            idxLbl.setForeground(PRIMARY);
            idxLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(idxLbl);
            card.add(Box.createVerticalStrut(4));
            addInfoRow(card, "Họ và tên:", kh.getHoTen(), ON_SURFACE);
            addInfoRow(card, "CCCD:",      kh.getCccd(), ON_SURFACE);
            addInfoRow(card, "SĐT:",       kh.getSoDienThoai(), ON_SURFACE);
            if (i < khachHangs.size() - 1) {
                JSeparator sep = new JSeparator();
                sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
                sep.setForeground(OUTLINE);
                card.add(sep);
                card.add(Box.createVerticalStrut(8));
            }
        }
        return card;
    }

    private String seatCustomerLabel(int index) {
        if (ghes != null && index >= 0 && index < ghes.size()) {
            Ghe ghe = ghes.get(index);
            if (ghe != null) return "Khách của " + seatPromoLabel(ghe);
        }
        return "Khách #" + (index + 1);
        // Handoff: nhãn khách ở bước xác nhận bám theo thứ tự ghế từ Step 5.
        // Cảnh báo: không sort riêng danh sách khách ở bước này để tránh lệch vé/khách.
    }

    private JPanel buildPaymentCard() {
        JPanel card = card("Phương thức thanh toán");
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        bgPayment = new ButtonGroup();

        rbTienMat = new JRadioButton("Tiền mặt");
        rbTienMat.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        rbTienMat.setBackground(CARD_BG);
        rbTienMat.setFocusPainted(false);
        rbTienMat.setSelected(true);

        rbChuyenKhoan = new JRadioButton("Chuyển khoản / QR");
        rbChuyenKhoan.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        rbChuyenKhoan.setBackground(CARD_BG);
        rbChuyenKhoan.setFocusPainted(false);

        bgPayment.add(rbTienMat);
        bgPayment.add(rbChuyenKhoan);

        card.add(Box.createVerticalStrut(4));
        card.add(rbTienMat);
        card.add(Box.createVerticalStrut(6));
        card.add(rbChuyenKhoan);
        return card;
    }

    // =========================================================================
    //  CARD HELPERS
    // =========================================================================

    private JPanel card(String sectionTitle) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(OUTLINE, 1),
            new EmptyBorder(16, 24, 16, 24)
        ));

        JLabel titleLbl = new JLabel(sectionTitle);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLbl.setForeground(ON_SURF_VAR);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(titleLbl);
        card.add(Box.createVerticalStrut(12));
        return card;
    }

    private void addInfoRow(JPanel card, String label, String value, Color valueColor) {
        addInfoRow(card, label, value, valueColor, false);
    }

    private void addInfoRow(JPanel card, String label, String value,
                            Color valueColor, boolean bold) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(CARD_BG);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lLbl = new JLabel(label);
        lLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lLbl.setForeground(ON_SURF_VAR);
        lLbl.setPreferredSize(new Dimension(160, 24));

        JLabel vLbl = new JLabel(value);
        vLbl.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, 13));
        vLbl.setForeground(valueColor);

        row.add(lLbl, BorderLayout.WEST);
        row.add(vLbl, BorderLayout.CENTER);
        card.add(row);
        card.add(Box.createVerticalStrut(4));
    }

    private void addSeatPromotionRows(JPanel card, List<SeatPromoSummary> summaries) {
        JLabel title = new JLabel("Khuyến mãi theo từng ghế");
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(Box.createVerticalStrut(4));
        card.add(title);
        card.add(Box.createVerticalStrut(8));

        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(CARD_BG);
        list.setAlignmentX(Component.LEFT_ALIGNMENT);
        list.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        for (SeatPromoSummary summary : summaries) {
            list.add(createSeatPromoRow(summary));
            list.add(Box.createVerticalStrut(6));
        }
        card.add(list);
        card.add(Box.createVerticalStrut(6));
        // Handoff: hiển thị KM ngang hàng với từng ghế để tránh hiểu nhầm KM áp cho mọi ghế.
        // Risk: nếu số KM/ghế tăng nhiều, row có thể cao hơn nhưng không đổi công thức tính tiền.
    }

    private JPanel createSeatPromoRow(SeatPromoSummary summary) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setBackground(PRIMARY_LIGHT);
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xD7, 0xCC, 0xF2), 1),
            new EmptyBorder(8, 10, 8, 10)
        ));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

        JLabel seat = new JLabel(seatPromoLabel(summary.ghe));
        seat.setFont(new Font("Segoe UI", Font.BOLD, 12));
        seat.setForeground(PRIMARY);
        seat.setPreferredSize(new Dimension(150, 24));
        row.add(seat, BorderLayout.WEST);

        JPanel promos = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        promos.setOpaque(false);
        if (summary.promotions.isEmpty()) {
            promos.add(promoChip("Không áp dụng", ON_SURF_VAR, NotionTheme.CARD_MUTED, OUTLINE));
        } else {
            for (ChiTietKhuyenMai km : summary.promotions) {
                int pct = (int) Math.round(clampDiscount(km.getPhanTramGiam()) * 100);
                promos.add(promoChip(promoNameFor(km) + "  −" + pct + "%", AppColors.WARNING_DARK, AppColors.WARNING_LIGHT, AppColors.WARNING));
            }
        }
        row.add(promos, BorderLayout.CENTER);
        return row;
        // Handoff: row dùng màu tím nhạt theo Notion, chip KM dùng vàng để nhấn giảm giá.
        // Risk: text tên KM dài sẽ chiếm ngang; ưu tiên giữ đầy đủ nội dung thay vì cắt logic.
    }

    private JLabel promoChip(String text, Color fg, Color bg, Color border) {
        JLabel chip = new JLabel(text);
        chip.setFont(new Font("Segoe UI", Font.BOLD, 12));
        chip.setForeground(fg);
        chip.setOpaque(true);
        chip.setBackground(bg);
        chip.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(border, 1),
            new EmptyBorder(4, 8, 4, 8)
        ));
        return chip;
    }

    private String seatPromoLabel(Ghe ghe) {
        if (ghe == null) return "Ghế —";
        String toaMa = ghe.getToaTau() != null ? ghe.getToaTau().getMaToaTau() : "—";
        return "Ghế " + ghe.getSoGhe() + " · " + toaMa;
    }

    private String fmt(double amount) {
        return VND_FMT.format((long) amount) + " ₫";
    }

    private double unitPriceFor(Ghe g) {
        if (g == null || g.getToaTau() == null || g.getToaTau().getLoaiGhe() == null) return 0.0;
        return priceMap.getOrDefault(g.getToaTau().getLoaiGhe(), 0.0);
    }

    private String promoNameFor(ChiTietKhuyenMai km) {
        if (km == null) return "Khuyến mãi";
        if (km.getTenChiTiet() != null && !km.getTenChiTiet().isBlank()) return km.getTenChiTiet();
        if (km.getKhuyenMai() != null && km.getKhuyenMai().getTenKhuyenMai() != null
                && !km.getKhuyenMai().getTenKhuyenMai().isBlank()) {
            return km.getKhuyenMai().getTenKhuyenMai();
        }
        return "Khuyến mãi";
    }

    private List<ChiTietKhuyenMai> selectedPromotionsForSeat(Ghe ghe) {
        List<ChiTietKhuyenMai> applicable = new ArrayList<>();
        if (ghe == null || lich == null || lich.getTuyen() == null) return applicable;
        List<ChiTietKhuyenMai> selected = chiTietKMsBySeat.getOrDefault(seatKey(ghe), java.util.Collections.emptyList());
        if (selected.isEmpty()) return applicable;
        String maTuyen = lich.getTuyen().getMaTuyen();
        LoaiGhe loai = ghe.getToaTau() != null ? ghe.getToaTau().getLoaiGhe() : null;

        for (ChiTietKhuyenMai km : selected) {
            if (km == null) continue;
            boolean tuyenOk = km.getTuyen() == null
                || (km.getTuyen().getMaTuyen() != null && km.getTuyen().getMaTuyen().equals(maTuyen));
            boolean loaiOk = km.getLoaiGhe() == null || km.getLoaiGhe() == loai;
            if (tuyenOk && loaiOk) applicable.add(km);
        }
        return applicable;
    }

    private String seatKey(Ghe ghe) {
        if (ghe == null) return "";
        if (ghe.getMaGhe() != null && !ghe.getMaGhe().isBlank()) return ghe.getMaGhe();
        String toa = (ghe.getToaTau() != null && ghe.getToaTau().getMaToaTau() != null)
            ? ghe.getToaTau().getMaToaTau() : "TOA";
        return toa + "-SEAT-" + ghe.getSoGhe();
    }

    private String promoId(ChiTietKhuyenMai km) {
        if (km == null) return "";
        if (km.getMaChiTietKM() != null && !km.getMaChiTietKM().isBlank()) return km.getMaChiTietKM();
        return String.valueOf(System.identityHashCode(km));
    }

    private double clampDiscount(double discount) {
        if (Double.isNaN(discount)) return 0.0;
        return Math.max(0.0, Math.min(1.0, discount));
    }

    private static class PromoUsage {
        private final ChiTietKhuyenMai promo;
        private int seatCount;

        private PromoUsage(ChiTietKhuyenMai promo) {
            this.promo = promo;
            this.seatCount = 0;
        }
    }

    private static class SeatPromoSummary {
        private final Ghe ghe;
        private final List<ChiTietKhuyenMai> promotions;

        private SeatPromoSummary(Ghe ghe, List<ChiTietKhuyenMai> promotions) {
            this.ghe = ghe;
            this.promotions = promotions == null ? java.util.Collections.emptyList() : promotions;
        }
    }

    // =========================================================================
    //  EXECUTE
    // =========================================================================

    private void execute() {
        String paymentType = rbTienMat.isSelected() ? "TIEN_MAT" : "CHUYEN_KHOAN";
        if (callback != null) callback.accept(paymentType);
    }

    // =========================================================================
    //  HELPERS
    // =========================================================================

    private void styleBtn(JButton btn, boolean primary) {
        BookingStepUi.styleActionButton(btn, primary);
    }

    // =========================================================================
    //  AppModule
    // =========================================================================

    @Override public String getTitle() { return "Bước 6 – Xác nhận"; }
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
        rbTienMat.setSelected(true);
    }
}
