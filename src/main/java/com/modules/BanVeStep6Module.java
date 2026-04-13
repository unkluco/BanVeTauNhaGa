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
    private final KhachHang             khachHang;
    private final ChiTietKhuyenMai      chiTietKM;

    private static final NumberFormat      VND_FMT = NumberFormat.getInstance(new Locale("vi", "VN"));
    private static final DateTimeFormatter DT_FMT  = DateTimeFormatter.ofPattern("HH:mm  dd/MM/yyyy");

    // Design tokens
    private static final Color PRIMARY       = new Color(0x00, 0x5D, 0x90);
    private static final Color SURFACE       = new Color(0xF8, 0xFA, 0xFC);
    private static final Color CARD_BG       = Color.WHITE;
    private static final Color ON_SURFACE    = new Color(0x1A, 0x1D, 0x21);
    private static final Color ON_SURF_VAR   = new Color(0x5F, 0x67, 0x70);
    private static final Color OUTLINE       = new Color(0xDE, 0xE3, 0xE8);
    private static final Color PRIMARY_LIGHT = new Color(0xE3, 0xF2, 0xFD);
    private static final Color AMOUNT_COLOR  = new Color(0x1A, 0x7A, 0x3C);
    private static final Color DIVIDER       = new Color(0xF0, 0xF4, 0xF8);

    private ButtonGroup   bgPayment;
    private JRadioButton  rbTienMat, rbChuyenKhoan;
    private JButton       btnSubmit, btnCancel;
    private JPanel        btnPanel;

    // =========================================================================
    //  CONSTRUCTOR
    // =========================================================================

    public BanVeStep6Module(Lich lich, ToaTau toa, List<Ghe> ghes,
                            Map<LoaiGhe, Double> priceMap, KhachHang khachHang,
                            ChiTietKhuyenMai chiTietKM) {
        this.lich      = lich;
        this.toa       = toa;
        this.ghes      = ghes;
        this.priceMap  = priceMap;
        this.khachHang = khachHang;
        this.chiTietKM = chiTietKM;
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

        btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        btnPanel.setBackground(SURFACE);
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, OUTLINE));
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

        LoaiGhe loai      = toa != null ? toa.getLoaiGhe() : null;
        double  unitPrice = loai != null ? priceMap.getOrDefault(loai, 0.0) : 0.0;
        double  total     = unitPrice * ghes.size();

        addInfoRow(card, "Toa:",      toa != null ? toa.getMaToaTau() : "—", ON_SURFACE);
        addInfoRow(card, "Loại ghế:", loai != null ? loai.toString() : "—", ON_SURFACE);

        StringBuilder gheStr = new StringBuilder();
        for (int i = 0; i < ghes.size(); i++) {
            if (i > 0) gheStr.append(", ");
            gheStr.append("Ghế ").append(ghes.get(i).getSoGhe());
        }
        addInfoRow(card, "Ghế:",          gheStr.toString(), ON_SURFACE);
        addInfoRow(card, "Đơn giá gốc:", fmt(unitPrice), ON_SURFACE);
        addInfoRow(card, "Số lượng:",    ghes.size() + " ghế", ON_SURFACE);

        // Divider
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(OUTLINE);
        card.add(sep);
        card.add(Box.createVerticalStrut(8));

        if (chiTietKM != null) {
            double discount    = chiTietKM.getPhanTramGiam();          // đã là 0–1 (vd: 0.20)
            double discountAmt = total * discount;
            double finalTotal  = total - discountAmt;
            Color  ORANGE      = new Color(0xE6, 0x52, 0x00);
            String promoLabel  = chiTietKM.getTenChiTiet() != null && !chiTietKM.getTenChiTiet().isBlank()
                ? chiTietKM.getTenChiTiet() : "Khuyến mãi";
            addInfoRow(card, "Khuyến mãi:", promoLabel + "  (−" + (int)(chiTietKM.getPhanTramGiam() * 100) + "%)", ORANGE);
            addInfoRow(card, "Giảm:",       "−" + fmt(discountAmt), ORANGE);
            addInfoRow(card, "Tổng tiền:",  fmt(finalTotal), AMOUNT_COLOR, true);
        } else {
            addInfoRow(card, "Tổng tiền:", fmt(total), AMOUNT_COLOR, true);
        }
        return card;
    }

    private JPanel buildCustomerCard() {
        JPanel card = card("Thông tin khách hàng");
        if (khachHang == null) {
            addInfoRow(card, "", "Chưa có thông tin khách hàng", ON_SURF_VAR);
            return card;
        }
        addInfoRow(card, "Họ và tên:", khachHang.getHoTen(), ON_SURFACE);
        addInfoRow(card, "CCCD:",      khachHang.getCccd(), ON_SURFACE);
        addInfoRow(card, "SĐT:",       khachHang.getSoDienThoai(), ON_SURFACE);
        return card;
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

    private String fmt(double amount) {
        return VND_FMT.format((long) amount) + " ₫";
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
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 24, 10, 24));
        if (primary) {
            btn.setBackground(PRIMARY); btn.setForeground(Color.WHITE); btn.setOpaque(true);
        } else {
            btn.setBackground(CARD_BG); btn.setForeground(ON_SURF_VAR); btn.setOpaque(true);
        }
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
