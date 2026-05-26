package com.modules;

import com.dao.DAO_ApDungKM;
import com.dao.DAO_ChiTietHoaDon;
import com.dao.DAO_HoaDonKhachHang;
import com.entity.*;
import com.enums.TrangThaiVe;
import com.lowagie.text.pdf.BaseFont;
import org.xhtmlrenderer.pdf.ITextRenderer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Cửa sổ Chi tiết Hóa đơn — thiết kế theo bố cục hóa đơn thực tế.
 * Không có title bar hệ thống (setUndecorated(true)).
 * Header xanh đậm cố định, không kéo thả.
 *
 *  ┌─ HEADER (xanh đậm, cố định) ────────────────────────────────────┐
 *  │  [LOGO] HÓA ĐƠN BÁN VÉ TÀU      Số: HD-...                     │
 *  ├─ INFO CARDS ───────────────────────────────────────────────────  ┤
 *  │  [KHÁCH HÀNG]                │  [NHÂN VIÊN LẬP]                │
 *  ├─ TABLE (11 cột) ────────────────────────────────────────────────┤
 *  │  STT │ Mã vé │ Ga đi │ Ga đến │ Đoàn tàu │ Ngày đi │ Loại ghế │
 *  │      │       │       │        │          │         │ Ghế │ TT  │
 *  ├─ SUMMARY ───────────────────────────────────────────────────────┤
 *  │                                           Tổng:  500,000 ₫      │
 *  ├─ FOOTER (ký tên + nút Đóng) ───────────────────────────────────┤
 */
public class ChiTietHoaDonDialog extends JDialog {

    private final HoaDon              hoaDon;
    private final List<ChiTietHoaDon> chiTietList;
    private final List<KhachHang>     khachHangList;
    private final DAO_ChiTietHoaDon   daoCTHD = new DAO_ChiTietHoaDon();
    private final DAO_ApDungKM        daoKM   = new DAO_ApDungKM();
    private final DAO_HoaDonKhachHang daoHDKH = new DAO_HoaDonKhachHang();
    private final boolean embedded;
    private boolean suppressAutoDismiss;

    // Formatters
    private static final NumberFormat     VND_FMT = NumberFormat.getInstance(new Locale("vi", "VN"));
    private static final DateTimeFormatter DT_FMT  = DateTimeFormatter.ofPattern("dd/MM/yyyy  HH:mm");
    private static final DateTimeFormatter D_FMT   = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Design tokens
    private static final Color HDR_BG         = NotionTheme.NAVY;
    private static final Color HDR_ACCENT     = NotionTheme.ACCENT;
    private static final Color SURFACE        = NotionTheme.PAGE;
    private static final Color CARD_BG        = NotionTheme.CARD;
    private static final Color ON_SURFACE     = NotionTheme.TEXT;
    private static final Color ON_SURF_VAR    = NotionTheme.TEXT_MUTED;
    private static final Color OUTLINE        = NotionTheme.BORDER;
    private static final Color PRIMARY        = NotionTheme.ACCENT;
    private static final Color PRIMARY_LIGHT  = NotionTheme.ACCENT_SOFT;
    private static final Color TABLE_HDR_BG   = NotionTheme.ACCENT_SOFT;
    private static final Color ROW_ALT        = NotionTheme.CARD_MUTED;
    private static final Color AMOUNT_COLOR   = AppColors.SUCCESS_DARK;
    private static final Color STATUS_SOLD_BG = AppColors.SUCCESS_LIGHT;
    private static final Color STATUS_SOLD_FG = AppColors.SUCCESS_DARK;
    private static final Color STATUS_HUY_BG  = AppColors.ERROR_LIGHT;
    private static final Color STATUS_HUY_FG  = AppColors.ERROR_DARK;
    private static final Color KM_FG          = AppColors.WARNING_DARK;  // amber/orange

    public ChiTietHoaDonDialog(JFrame owner, HoaDon hoaDon) {
        super(owner, "", Dialog.ModalityType.MODELESS);
        this.hoaDon        = hoaDon;
        this.chiTietList   = daoCTHD.findByHoaDon(hoaDon.getMaHoaDon());
        this.khachHangList = daoHDKH.findKhachHangByHoaDon(hoaDon.getMaHoaDon());
        this.embedded = false;

        setUndecorated(true);
        setResizable(false);
        setBackground(new Color(0, 0, 0, 0));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUI();
        installDismissOnOutsideClick();
        pack();
        setMinimumSize(new Dimension(1150, 640));
        ModuleLauncher.centerDialog(this, owner);
    }

    private ChiTietHoaDonDialog(HoaDon hoaDon, boolean embedded) {
        super((Frame) null, "", Dialog.ModalityType.MODELESS);
        this.hoaDon        = hoaDon;
        this.chiTietList   = daoCTHD.findByHoaDon(hoaDon.getMaHoaDon());
        this.khachHangList = daoHDKH.findKhachHangByHoaDon(hoaDon.getMaHoaDon());
        this.embedded = embedded;
    }

    public static JPanel createInvoiceView(HoaDon hoaDon) {
        return new ChiTietHoaDonDialog(hoaDon, true).buildInvoicePanel(false);
    }

    // =========================================================================
    //  BUILD UI
    // =========================================================================

    private void buildUI() {
        setContentPane(ModuleLauncher.buildShadowWrapper(buildInvoicePanel(true)));
    }

    private JPanel buildInvoicePanel(boolean includeFooter) {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(SURFACE);
        root.setBorder(BorderFactory.createLineBorder(OUTLINE, 1));

        root.add(buildInvoiceHeader(), BorderLayout.NORTH);

        JPanel invoiceBody = new JPanel();
        invoiceBody.setLayout(new BoxLayout(invoiceBody, BoxLayout.Y_AXIS));
        invoiceBody.setBackground(SURFACE);
        invoiceBody.setBorder(new EmptyBorder(16, 24, 16, 24));

        invoiceBody.add(buildInfoCards());
        invoiceBody.add(Box.createVerticalStrut(14));
        invoiceBody.add(buildItemsSection());
        invoiceBody.add(Box.createVerticalStrut(14));
        invoiceBody.add(buildSummaryRow());
        invoiceBody.add(Box.createVerticalStrut(20));
        invoiceBody.add(buildSignatureSection());

        JScrollPane scroll = new JScrollPane(invoiceBody);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(SURFACE);

        root.add(scroll, BorderLayout.CENTER);
        if (includeFooter) root.add(buildFooterBar(), BorderLayout.SOUTH);
        return root;
    }

    private void installDismissOnOutsideClick() {
        addWindowFocusListener(new WindowAdapter() {
            @Override public void windowLostFocus(WindowEvent e) {
                SwingUtilities.invokeLater(() -> {
                    if (isShowing() && !isFocused() && !suppressAutoDismiss) dispose();
                });
            }
        });
    }

    // ---- Header xanh đậm — cố định, không kéo thả cửa sổ ----
    private JPanel buildInvoiceHeader() {
        JPanel outer = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, HDR_BG, getWidth(), getHeight(), HDR_ACCENT));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        outer.setOpaque(false);
        outer.setBorder(new EmptyBorder(18, 24, 18, 24));

        // Left: icon + title + company
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel iconLbl = new JLabel();
        iconLbl.setOpaque(false);
        iconLbl.setForeground(AppColors.SURFACE);
        try {
            iconLbl.setIcon(LineIcons.contained(LineIcons.Name.INVOICE, 32, 20, AppColors.SURFACE));
        } catch (Exception ignored) {}
        // Handoff: invoice header icon now loads at final 32px SVG size for crisp strokes.
        // Risk: title row spacing intentionally stays unchanged.

        JLabel lblTitle = new JLabel("  HÓA ĐƠN BÁN VÉ TÀU HỎA");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(AppColors.SURFACE);

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titleRow.setOpaque(false);
        titleRow.add(iconLbl);
        titleRow.add(lblTitle);

        JLabel lblCompany = new JLabel("Công ty Vận tải Đường sắt Việt Nam  •  Hệ thống Azure Rail");
        lblCompany.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblCompany.setForeground(PRIMARY_LIGHT);

        left.add(titleRow);
        left.add(Box.createVerticalStrut(4));
        left.add(lblCompany);

        // Right: mã HĐ + ngày
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setOpaque(false);

        JLabel lblMa = new JLabel("Số:  " + hoaDon.getMaHoaDon());
        lblMa.setFont(new Font("Consolas", Font.BOLD, 14));
        lblMa.setForeground(AppColors.SURFACE);
        lblMa.setAlignmentX(Component.RIGHT_ALIGNMENT);

        String ngayStr = hoaDon.getNgayLap() != null ? hoaDon.getNgayLap().format(DT_FMT) : "—";
        JLabel lblNgay = new JLabel("Ngày:  " + ngayStr);
        lblNgay.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblNgay.setForeground(PRIMARY_LIGHT);
        lblNgay.setAlignmentX(Component.RIGHT_ALIGNMENT);

        right.add(lblMa);
        right.add(Box.createVerticalStrut(6));
        right.add(lblNgay);

        outer.add(left,  BorderLayout.WEST);
        outer.add(right, BorderLayout.EAST);
        return outer;
    }

    // ---- Info cards: khách hàng + nhân viên ----
    private JPanel buildInfoCards() {
        JPanel row = new JPanel(new GridLayout(1, 2, 14, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        row.add(buildKhachHangCard());
        row.add(buildNhanVienCard());
        return row;
    }

    private JPanel buildKhachHangCard() {
        JPanel card = infoCard();

        String headerTitle = khachHangList.size() > 1
                ? "THÔNG TIN KHÁCH HÀNG (" + khachHangList.size() + ")"
                : "THÔNG TIN KHÁCH HÀNG";
        addCardHeader(card, LineIcons.Name.USER, headerTitle,
                PRIMARY);

        if (khachHangList.isEmpty()) {
            addInfoRow(card, "Khách hàng:", "Không rõ", false);
        } else if (khachHangList.size() == 1) {
            KhachHang kh = khachHangList.get(0);
            addInfoRow(card, "Họ và tên:",    kh.getHoTen(),        true);
            addInfoRow(card, "CCCD:",         kh.getCccd(),         false);
            addInfoRow(card, "Điện thoại:",   kh.getSoDienThoai(),  false);
            addInfoRow(card, "Mã KH:",        kh.getMaKhachHang(),  false);
        } else {
            for (int i = 0; i < khachHangList.size(); i++) {
                KhachHang kh = khachHangList.get(i);
                if (i > 0) {
                    JSeparator sep = new JSeparator();
                    sep.setForeground(OUTLINE);
                    sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
                    sep.setAlignmentX(Component.LEFT_ALIGNMENT);
                    card.add(Box.createVerticalStrut(4));
                    card.add(sep);
                    card.add(Box.createVerticalStrut(4));
                }
                addInfoRow(card, "Khách #" + (i + 1), kh.getHoTen() != null ? kh.getHoTen() : "—", true);
                addInfoRow(card, "CCCD/SĐT:",
                        (kh.getCccd() != null ? kh.getCccd() : "—")
                        + "  •  "
                        + (kh.getSoDienThoai() != null ? kh.getSoDienThoai() : "—"),
                        false);
            }
        }
        return card;
    }

    private JPanel buildNhanVienCard() {
        JPanel card = infoCard();
        NhanVien nv = hoaDon.getNhanVien();

        addCardHeader(card, LineIcons.Name.USER, "NHÂN VIÊN LẬP HÓA ĐƠN",
                PRIMARY);

        if (nv != null) {
            addInfoRow(card, "Họ và tên:", nv.getHoTen(),          true);
            addInfoRow(card, "Mã NV:",     nv.getMaNV(),           false);
            addInfoRow(card, "Vai trò:",   nv.getVaiTro() != null ? nv.getVaiTro().toString() : "—", false);
            addInfoRow(card, "Điện thoại:",nv.getSoDienThoai() != null ? nv.getSoDienThoai() : "—", false);
        } else {
            addInfoRow(card, "Nhân viên:", "Không rõ", false);
        }
        return card;
    }

    private JPanel infoCard() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1),
                new EmptyBorder(0, 0, 14, 0)));
        return p;
    }

    private void addCardHeader(JPanel card, LineIcons.Name iconName, String title, Color accentColor) {
        JPanel hdr = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        hdr.setBackground(accentColor);
        hdr.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

        try {
            JLabel iLbl = new JLabel(LineIcons.contained(iconName, 18, 10, AppColors.SURFACE));
            hdr.add(iLbl);
        } catch (Exception ignored) {}
        // Handoff: card header icons use LineIcons at final 18px size, no legacy resource path.
        // Risk: keep header max height fixed so invoice cards do not jump.

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(AppColors.SURFACE);
        hdr.add(lbl);

        card.add(hdr);
    }

    private void addInfoRow(JPanel card, String label, String value, boolean bold) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(5, 14, 2, 14));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        JLabel lLbl = new JLabel(label);
        lLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lLbl.setForeground(ON_SURF_VAR);
        lLbl.setPreferredSize(new Dimension(100, 22));

        JLabel vLbl = new JLabel(value != null ? value : "—");
        vLbl.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, 13));
        vLbl.setForeground(ON_SURFACE);

        row.add(lLbl, BorderLayout.WEST);
        row.add(vLbl, BorderLayout.CENTER);
        card.add(row);
    }

    // ---- Items table (11 columns) ----
    private JPanel buildItemsSection() {
        JPanel section = new JPanel(new BorderLayout(0, 8));
        section.setOpaque(false);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // Section title
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleRow.setOpaque(false);
        try {
            JLabel ticketIcon = new JLabel(LineIcons.contained(LineIcons.Name.TICKET, 18, 10, PRIMARY));
            titleRow.add(ticketIcon);
        } catch (Exception ignored) {}
        // Handoff: ticket section icon uses final SVG size to avoid pixelated small glyphs.
        // Risk: FlowLayout gap keeps previous baseline rhythm.
        JLabel sTitle = new JLabel("CHI TIẾT CÁC VÉ");
        sTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        sTitle.setForeground(PRIMARY);
        titleRow.add(sTitle);

        JSeparator sep = new JSeparator();
        sep.setForeground(AppColors.primaryAlpha(80));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(titleRow, BorderLayout.WEST);
        top.add(sep, BorderLayout.SOUTH);

        // 12-column table
        String[] cols = {
            "STT", "Mã vé", "Hành khách", "Ga đi", "Ga đến",
            "Đoàn tàu", "Ngày đi", "Loại ghế",
            "Ghế số", "Trạng thái", "KM", "Thành tiền"
        };
        Object[][] data = buildTableData();

        DefaultTableModel model = new DefaultTableModel(data, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable tbl = new JTable(model);
        tbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tbl.setRowHeight(40);
        tbl.setShowGrid(false);
        tbl.setIntercellSpacing(new Dimension(0, 0));
        tbl.setBackground(CARD_BG);
        tbl.setSelectionBackground(NotionTheme.TABLE_SELECTION);
        tbl.setSelectionForeground(ON_SURFACE);
        tbl.setFillsViewportHeight(true);

        JTableHeader th = tbl.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.BOLD, 12));
        th.setBackground(TABLE_HDR_BG);
        th.setForeground(PRIMARY);
        th.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, PRIMARY));
        th.setReorderingAllowed(false);

        // Column widths (total ~1045px — comfortable in 1150px dialog)
        int[] widths = {40, 80, 150, 120, 120, 110, 100, 100, 65, 90, 60, 105};
        for (int i = 0; i < widths.length; i++)
            tbl.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Default renderer (handles alternating row colors)
        tbl.setDefaultRenderer(Object.class, new ItemRowRenderer());

        // Column-specific renderers
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        tbl.getColumnModel().getColumn(0).setCellRenderer(center);  // STT
        tbl.getColumnModel().getColumn(8).setCellRenderer(center);  // Ghế số
        tbl.getColumnModel().getColumn(9).setCellRenderer(new TrangThaiRenderer());
        tbl.getColumnModel().getColumn(10).setCellRenderer(new KmRenderer());
        tbl.getColumnModel().getColumn(11).setCellRenderer(new ItemAmountRenderer());

        JScrollPane scroll = new JScrollPane(tbl);
        scroll.setBorder(BorderFactory.createLineBorder(OUTLINE, 1));
        scroll.getViewport().setBackground(CARD_BG);
        scroll.setPreferredSize(new Dimension(0, Math.min(300, 42 + chiTietList.size() * 40 + 2)));

        section.add(top,    BorderLayout.NORTH);
        section.add(scroll, BorderLayout.CENTER);
        return section;
    }

    private Object[][] buildTableData() {
        Object[][] data = new Object[chiTietList.size()][12];
        for (int i = 0; i < chiTietList.size(); i++) {
            ChiTietHoaDon ct = chiTietList.get(i);
            Ve ve = ct.getVe();

            String gaDi      = "—";
            String gaDen     = "—";
            String doanTau   = "—";
            String ngayDi    = "—";
            String loaiGhe   = "—";
            String soGhe     = "—";
            String trangThai = "—";
            String maVe      = "—";
            String hanhKhach = ct.getKhachHang() != null && ct.getKhachHang().getHoTen() != null
                    ? ct.getKhachHang().getHoTen() : "—";

            if (ve != null) {
                maVe      = ve.getMaVe();
                trangThai = ve.getTrangThai() != null ? ve.getTrangThai().toString() : "—";

                Ghe ghe = ve.getGhe();
                if (ghe != null) {
                    soGhe = String.valueOf(ghe.getSoGhe());
                    if (ghe.getToaTau() != null && ghe.getToaTau().getLoaiGhe() != null)
                        loaiGhe = ghe.getToaTau().getLoaiGhe().toString();
                }

                Lich lich = ve.getLich();
                if (lich != null) {
                    if (lich.getThoiGianBatDau() != null)
                        ngayDi = lich.getThoiGianBatDau().format(D_FMT);
                    if (lich.getDoanTau() != null)
                        doanTau = lich.getDoanTau().getMaDoanTau()
                                + (lich.getDoanTau().getTenDoanTau() != null
                                   ? "  " + abbrev(lich.getDoanTau().getTenDoanTau(), 10) : "");
                    Tuyen t = lich.getTuyen();
                    if (t != null) {
                        gaDi  = t.getGaDi()  != null ? t.getGaDi().getTenGa()  : t.getMaTuyen();
                        gaDen = t.getGaDen() != null ? t.getGaDen().getTenGa() : "—";
                    }
                }
            }

            // KM discount — giaTien in ChiTietHoaDon is already the final (discounted) price
            String kmText = "—";
            List<ApDungKM> kmList = daoKM.findByChiTietHD(ct.getMaChiTietHD());
            if (!kmList.isEmpty()) {
                double totalPct = 0;
                for (ApDungKM km : kmList) {
                    if (km.getChiTietKhuyenMai() != null)
                        totalPct += km.getChiTietKhuyenMai().getPhanTramGiam();
                }
                if (totalPct > 0) {
                    int pct = (int) Math.round(totalPct * 100);
                    kmText = "−" + pct + "%";   // − (minus sign)
                }
            }

            data[i][0]  = i + 1;
            data[i][1]  = maVe;
            data[i][2]  = hanhKhach;
            data[i][3]  = gaDi;
            data[i][4]  = gaDen;
            data[i][5]  = doanTau;
            data[i][6]  = ngayDi;
            data[i][7]  = loaiGhe;
            data[i][8]  = soGhe;
            data[i][9]  = trangThai;
            data[i][10] = kmText;
            data[i][11] = ct.getGiaTien();
        }
        return data;
    }

    // ---- Summary ----
    private JPanel buildSummaryRow() {
        BigDecimal tong = chiTietList.stream()
                .map(ChiTietHoaDon::getGiaTien)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setOpaque(false);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setForeground(OUTLINE);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(PRIMARY_LIGHT);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY, 1),
                new EmptyBorder(14, 20, 14, 20)));
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JPanel leftInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        leftInfo.setOpaque(false);
        try {
            JLabel ticketIcon = new JLabel(LineIcons.contained(LineIcons.Name.TICKET, 18, 10, PRIMARY));
            leftInfo.add(ticketIcon);
        } catch (Exception ignored) {}
        // Handoff: total ticket icon stays vector-rendered in the summary strip.
        // Risk: do not change summary text/action layout here.
        JLabel soVeLbl = new JLabel("Tổng số vé: " + chiTietList.size());
        soVeLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        soVeLbl.setForeground(ON_SURF_VAR);
        leftInfo.add(soVeLbl);

        JPanel rightInfo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        rightInfo.setOpaque(false);
        JLabel tongLabel = new JLabel("Tổng cộng:");
        tongLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tongLabel.setForeground(ON_SURF_VAR);
        JLabel tongAmt = new JLabel(VND_FMT.format(tong) + " ₫");
        tongAmt.setFont(new Font("Segoe UI", Font.BOLD, 22));
        tongAmt.setForeground(AMOUNT_COLOR);
        rightInfo.add(tongLabel);
        rightInfo.add(tongAmt);

        bar.add(leftInfo,  BorderLayout.WEST);
        bar.add(rightInfo, BorderLayout.EAST);

        wrapper.add(sep);
        wrapper.add(Box.createVerticalStrut(8));
        wrapper.add(bar);
        return wrapper;
    }

    // ---- Footer ký tên ----
    private JPanel buildSignatureSection() {
        JPanel p = new JPanel(new GridLayout(1, 2, 20, 0));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        p.add(signatureBox("Người mua hàng", "(Ký, ghi rõ họ tên)",
                khachHangNamesJoined()));
        p.add(signatureBox("Nhân viên bán hàng", "(Ký, ghi rõ họ tên)",
                hoaDon.getNhanVien() != null ? hoaDon.getNhanVien().getHoTen() : ""));
        return p;
    }

    private JPanel signatureBox(String title, String sub, String name) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1),
                new EmptyBorder(14, 20, 14, 20)));

        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 13));
        t.setForeground(ON_SURFACE);
        t.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel s = new JLabel(sub);
        s.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        s.setForeground(ON_SURF_VAR);
        s.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel blank = new JPanel();
        blank.setOpaque(false);
        blank.setPreferredSize(new Dimension(0, 28));

        JLabel n = new JLabel(name);
        n.setFont(new Font("Segoe UI", Font.BOLD, 12));
        n.setForeground(PRIMARY);
        n.setAlignmentX(Component.CENTER_ALIGNMENT);

        p.add(t); p.add(Box.createVerticalStrut(2)); p.add(s);
        p.add(blank); p.add(n);
        return p;
    }

    // ---- Footer buttons ----
    private JPanel buildFooterBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        bar.setBackground(CARD_BG);
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, OUTLINE));

        JButton btnExportPdf = styledBtn("Xuất PDF", AppColors.SUCCESS_DARK, AppColors.SURFACE);
        btnExportPdf.addActionListener(e -> exportInvoicePdf());

        JButton btnClose = styledBtn("Đóng", PRIMARY, AppColors.SURFACE);
        btnClose.addActionListener(e -> dispose());

        bar.add(btnExportPdf);
        bar.add(btnClose);
        return bar;
    }

    private JButton styledBtn(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBackground(bg); b.setForeground(fg);
        b.setBorder(new EmptyBorder(8, 28, 8, 28));
        b.setFocusPainted(false); b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }



    public void exportInvoicePdf() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Xuất hóa đơn PDF");
        chooser.setSelectedFile(new java.io.File(safeFileName(hoaDon.getMaHoaDon()) + ".pdf"));
        suppressAutoDismiss = true;
        int choice;
        try {
            choice = chooser.showSaveDialog(this);
        } finally {
            suppressAutoDismiss = false;
        }
        if (choice != JFileChooser.APPROVE_OPTION) return;

        Path output = chooser.getSelectedFile().toPath();
        if (!output.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            output = output.resolveSibling(output.getFileName() + ".pdf");
        }
        Path target = output;
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                writeInvoicePdf(target);
                return null;
            }
            @Override protected void done() {
                suppressAutoDismiss = true;
                try {
                    get();
                    NotionMessageDialog.showMessageDialog(ChiTietHoaDonDialog.this,
                            "Đã xuất hóa đơn PDF thành công:\n" + target,
                            "Xuất PDF", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    NotionMessageDialog.showMessageDialog(ChiTietHoaDonDialog.this,
                            "Xuất hóa đơn PDF thất bại: " + ex.getMessage(),
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                } finally {
                    suppressAutoDismiss = false;
                }
            }
        }.execute();
        // Handoff: xuất PDF chạy nền để dialog không đứng hình khi render nhiều vé.
        // Cảnh báo: layout PDF dùng HTML riêng, nếu đổi field hóa đơn cần cập nhật buildInvoicePdfHtml().
    }

    private void writeInvoicePdf(Path output) throws Exception {
        if (output.getParent() != null) Files.createDirectories(output.getParent());
        ITextRenderer renderer = new ITextRenderer();
        registerPdfFonts(renderer);
        renderer.setDocumentFromString(buildInvoicePdfHtml());
        renderer.layout();
        try (OutputStream out = new FileOutputStream(output.toFile())) {
            renderer.createPDF(out);
        }
    }

    private void registerPdfFonts(ITextRenderer renderer) {
        try {
            String windir = System.getenv("WINDIR") != null ? System.getenv("WINDIR") : "C:/Windows";
            Path arial = Path.of(windir, "Fonts", "arial.ttf");
            if (Files.exists(arial)) renderer.getFontResolver().addFont(arial.toString(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Path arialBold = Path.of(windir, "Fonts", "arialbd.ttf");
            if (Files.exists(arialBold)) renderer.getFontResolver().addFont(arialBold.toString(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        } catch (Exception ignored) {}
    }

    private String buildInvoicePdfHtml() {
        String ngayLap = hoaDon.getNgayLap() != null ? hoaDon.getNgayLap().format(DT_FMT) : "—";
        String ngayShort = hoaDon.getNgayLap() != null ? hoaDon.getNgayLap().format(D_FMT) : "—";
        String nhanVien = hoaDon.getNhanVien() != null ? text(hoaDon.getNhanVien().getHoTen()) : "—";
        String maNhanVien = hoaDon.getNhanVien() != null ? text(hoaDon.getNhanVien().getMaNV()) : "—";
        String khachHang = khachHangNamesJoined();
        String buyerPhone = firstCustomerPhone();
        String buyerId = firstCustomerId();
        String invoiceCode = safeFileName(hoaDon.getMaHoaDon()) + "-" + Integer.toHexString(Math.abs(hoaDon.getMaHoaDon().hashCode())).toUpperCase(Locale.ROOT);
        BigDecimal tong = chiTietList.stream().map(ChiTietHoaDon::getGiaTien).filter(v -> v != null).reduce(BigDecimal.ZERO, BigDecimal::add);

        StringBuilder rows = new StringBuilder();
        for (int i = 0; i < chiTietList.size(); i++) {
            ChiTietHoaDon ct = chiTietList.get(i);
            TicketPdfRow row = toTicketPdfRow(ct);
            rows.append("<tr")
                    .append(i % 2 == 1 ? " class=\"even\"" : "")
                    .append(">")
                    .append(td("center", String.valueOf(i + 1)))
                    .append(td("desc", row.hanhTrinh() + "<br/><span class=\"subline\">Mã vé: " + row.maVe() + "</span>"))
                    .append(td("center", "Vé"))
                    .append(td("center", "1"))
                    .append(td("right", money(ct.getGiaTien())))
                    .append(td("right", money(ct.getGiaTien())))
                    .append("</tr>");
        }

        return """
                <!DOCTYPE html>
                <html>
                <head>
                  <meta charset=\"UTF-8\" />
                  <style>
                    @page { size: A4; margin: 14mm 12mm; }
                    body { font-family: Arial, sans-serif; color: #111827; font-size: 10.5px; }
                    .paper { border: 1.4px solid #123b63; padding: 12px 14px 10px 14px; }
                    table { border-collapse: collapse; }
                    .top-note { width: 100%%; font-size: 8.5px; color: #23415f; margin-bottom: 4px; }
                    .header { width: 100%%; border-bottom: 1.4px solid #123b63; margin-bottom: 8px; }
                    .logoBox { width: 118px; height: 56px; border: 1px solid #123b63; text-align: center; vertical-align: middle; color: #123b63; font-size: 18px; font-weight: bold; }
                    .title { text-align: center; vertical-align: top; }
                    .title .vn { font-size: 18px; color: #b91c1c; font-weight: bold; margin-top: 4px; }
                    .title .en { font-size: 12px; color: #111827; font-weight: bold; margin-top: 2px; }
                    .title .date { margin-top: 8px; font-size: 10.5px; }
                    .meta { width: 138px; font-size: 10px; line-height: 1.55; vertical-align: top; }
                    .meta b { color: #b91c1c; }
                    .party { width: 100%%; margin-top: 5px; }
                    .party td { border-bottom: 1px solid #7f9db9; padding: 3px 4px; vertical-align: top; }
                    .party .label { width: 128px; color: #123b63; font-weight: bold; }
                    .party .value { font-weight: bold; }
                    .items { width: 100%%; margin-top: 8px; border: 1.2px solid #123b63; }
                    .items th { border: 1px solid #123b63; padding: 5px 4px; text-align: center; font-weight: bold; background: #f3f6f9; }
                    .items td { border: 1px solid #123b63; padding: 5px 4px; vertical-align: top; }
                    .items tr.even td { background: #fbfcfd; }
                    .center { text-align: center; } .right { text-align: right; } .desc { line-height: 1.35; }
                    .subline { color: #4b5563; font-size: 9.5px; }
                    .summary { width: 100%%; border-left: 1.2px solid #123b63; border-right: 1.2px solid #123b63; border-bottom: 1.2px solid #123b63; }
                    .summary td { border: 1px solid #123b63; padding: 5px 6px; }
                    .summary .sumLabel { text-align: right; font-weight: bold; }
                    .summary .sumAmount { text-align: right; font-weight: bold; width: 150px; }
                    .amountWords { width: 100%%; border-left: 1.2px solid #123b63; border-right: 1.2px solid #123b63; border-bottom: 1.2px solid #123b63; }
                    .amountWords td { padding: 6px; font-style: italic; }
                    .sign { width: 100%%; margin-top: 18px; }
                    .sign td { width: 33.33%%; text-align: center; vertical-align: top; font-weight: bold; }
                    .sign .hint { font-weight: normal; font-style: italic; font-size: 9.5px; margin-top: 2px; }
                    .sign .space { height: 70px; }
                    .lookup { width: 100%%; margin-top: 14px; border-top: 1px solid #123b63; color: #123b63; font-size: 9px; }
                    .lookup td { padding-top: 7px; vertical-align: middle; }
                    .qr { width: 52px; height: 52px; border: 1px solid #123b63; text-align: center; font-size: 8px; color: #123b63; }
                  </style>
                </head>
                <body>
                <div class=\"paper\">
                  <table class=\"top-note\"><tr><td>Đơn vị cung cấp dịch vụ bán vé tàu: Hệ thống Azure Rail</td><td class=\"right\">Mã tra cứu: %s</td></tr></table>
                  <table class=\"header\"><tr>
                    <td class=\"logoBox\">AZURE<br/>RAIL</td>
                    <td class=\"title\"><div class=\"vn\">HÓA ĐƠN BÁN VÉ TÀU</div><div class=\"en\">RAILWAY TICKET INVOICE</div><div class=\"date\">Ngày (Date) %s</div></td>
                    <td class=\"meta\">Mẫu số: <b>AR/01</b><br/>Ký hiệu: <b>AR%s</b><br/>Số: <b>%s</b><br/>NV lập: %s</td>
                  </tr></table>

                  <table class=\"party\">
                    <tr><td class=\"label\">Đơn vị bán hàng</td><td class=\"value\">CÔNG TY VẬN TẢI ĐƯỜNG SẮT AZURE RAIL</td></tr>
                    <tr><td class=\"label\">Mã số thuế</td><td>0100000000</td></tr>
                    <tr><td class=\"label\">Địa chỉ</td><td>Nhà ga trung tâm Azure Rail, Việt Nam</td></tr>
                    <tr><td class=\"label\">Điện thoại / Email</td><td>1900 0000  •  support@azurerail.vn</td></tr>
                    <tr><td class=\"label\">Họ tên người mua hàng</td><td class=\"value\">%s</td></tr>
                    <tr><td class=\"label\">MST/CCCD</td><td>%s</td></tr>
                    <tr><td class=\"label\">Điện thoại</td><td>%s</td></tr>
                    <tr><td class=\"label\">Hình thức thanh toán</td><td>Tiền mặt / Chuyển khoản</td></tr>
                  </table>

                  <table class=\"items\">
                    <thead><tr><th style=\"width:34px\">STT<br/>(No)</th><th>Tên hàng hóa, dịch vụ<br/>(Description)</th><th style=\"width:55px\">ĐVT<br/>(Unit)</th><th style=\"width:45px\">SL<br/>(Qty)</th><th style=\"width:86px\">Đơn giá<br/>(Unit price)</th><th style=\"width:96px\">Thành tiền<br/>(Amount)</th></tr></thead>
                    <tbody>%s</tbody>
                  </table>
                  <table class=\"summary\">
                    <tr><td class=\"sumLabel\" colspan=\"5\">Cộng tiền hàng (Total amount excl. VAT):</td><td class=\"sumAmount\">%s</td></tr>
                    <tr><td>Thuế suất GTGT (VAT rate):</td><td colspan=\"3\">Giá vé đã bao gồm thuế/phí theo quy định</td><td class=\"sumLabel\">Tiền thuế GTGT:</td><td class=\"sumAmount\">—</td></tr>
                    <tr><td class=\"sumLabel\" colspan=\"5\">Tổng cộng tiền thanh toán (Total payment):</td><td class=\"sumAmount\">%s</td></tr>
                  </table>
                  <table class=\"amountWords\"><tr><td>Số tiền viết bằng chữ (Amount in words): <b>%s</b></td></tr></table>

                  <table class=\"sign\"><tr>
                    <td>Người mua hàng<div class=\"hint\">Ký, ghi rõ họ tên</div><div class=\"space\"></div>%s</td>
                    <td>Người bán hàng<div class=\"hint\">Ký, ghi rõ họ tên</div><div class=\"space\"></div>%s</td>
                    <td>Thủ trưởng đơn vị<div class=\"hint\">Ký điện tử / đóng dấu nếu có</div><div class=\"space\"></div></td>
                  </tr></table>

                  <table class=\"lookup\"><tr><td>Tra cứu hóa đơn tại: https://azurerail.vn/tra-cuu &nbsp; | &nbsp; Mã tra cứu: <b>%s</b><br/>Hóa đơn chỉ có giá trị khi thông tin tra cứu khớp với hệ thống.</td><td class=\"qr\">QR<br/>TRA CỨU</td></tr></table>
                </div>
                </body>
                </html>
                """.formatted(
                    html(invoiceCode), html(ngayLap), html(ngayShort.replace("/", "")), html(hoaDon.getMaHoaDon()), html(maNhanVien),
                    html(khachHang), html(buyerId), html(buyerPhone.isBlank() ? "—" : buyerPhone), rows,
                    html(money(tong)), html(money(tong)), html(amountInVietnamese(tong)), html(khachHang), html(nhanVien), html(invoiceCode));
    }

    private TicketPdfRow toTicketPdfRow(ChiTietHoaDon ct) {
        Ve ve = ct.getVe();
        String maVe = ve != null ? text(ve.getMaVe()) : "—";
        String gaDi = "—", gaDen = "—", doanTau = "—", thoiGian = "—", choNgoi = "—";
        if (ve != null) {
            Ghe ghe = ve.getGhe();
            if (ghe != null) {
                String toa = ghe.getToaTau() != null ? text(ghe.getToaTau().getMaToaTau()) : "—";
                String loai = ghe.getToaTau() != null && ghe.getToaTau().getLoaiGhe() != null ? ghe.getToaTau().getLoaiGhe().toString() : "—";
                choNgoi = "Toa " + toa + " / Ghế " + ghe.getSoGhe() + "<br/>" + loai;
            }
            Lich lich = ve.getLich();
            if (lich != null) {
                if (lich.getThoiGianBatDau() != null) thoiGian = lich.getThoiGianBatDau().format(DT_FMT);
                if (lich.getDoanTau() != null) doanTau = text(lich.getDoanTau().getMaDoanTau()) + "<br/>" + text(lich.getDoanTau().getTenDoanTau());
                Tuyen tuyen = lich.getTuyen();
                if (tuyen != null) {
                    gaDi = tuyen.getGaDi() != null ? text(tuyen.getGaDi().getTenGa()) : text(tuyen.getMaTuyen());
                    gaDen = tuyen.getGaDen() != null ? text(tuyen.getGaDen().getTenGa()) : "—";
                }
            }
        }
        KhachHang kh = ct.getKhachHang();
        String hanhKhach = kh != null && kh.getHoTen() != null ? text(kh.getHoTen()) : "—";
        return new TicketPdfRow(maVe, gaDi + " → " + gaDen, doanTau, thoiGian, choNgoi + "<br/>" + hanhKhach);
    }

    private String td(String cssClass, String value) {
        return "<td" + (cssClass == null || cssClass.isBlank() ? "" : " class=\"" + cssClass + "\"") + ">" + html(value).replace("&lt;br/&gt;", "<br/>") + "</td>";
    }

    private String money(BigDecimal value) {
        return VND_FMT.format(value != null ? value : BigDecimal.ZERO) + " ₫";
    }

    private String text(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }

    private String html(Object value) {
        String raw = value == null ? "" : value.toString();
        return raw.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }


    private String firstCustomerPhone() {
        if (khachHangList.isEmpty()) return "";
        String phone = khachHangList.get(0).getSoDienThoai();
        return phone == null ? "" : phone.trim();
    }

    private String firstCustomerId() {
        if (khachHangList.isEmpty()) return "—";
        String id = khachHangList.get(0).getCccd();
        return id == null || id.isBlank() ? "—" : id.trim();
    }

    private String amountInVietnamese(BigDecimal value) {
        long amount = value == null ? 0L : value.setScale(0, java.math.RoundingMode.HALF_UP).longValue();
        if (amount == 0) return "Không đồng.";
        return capitalize(readNumber(amount)) + " đồng.";
    }

    private String readNumber(long number) {
        String[] units = {"", " nghìn", " triệu", " tỷ"};
        StringBuilder result = new StringBuilder();
        int unitIndex = 0;
        while (number > 0 && unitIndex < units.length) {
            int group = (int) (number % 1000);
            if (group > 0) {
                String groupText = readThreeDigits(group, number >= 1000);
                result.insert(0, groupText + units[unitIndex] + (result.length() > 0 ? " " : ""));
            }
            number /= 1000;
            unitIndex++;
        }
        return result.toString().trim();
    }

    private String readThreeDigits(int number, boolean full) {
        String[] digit = {"không", "một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín"};
        int hundred = number / 100;
        int ten = (number % 100) / 10;
        int one = number % 10;
        StringBuilder text = new StringBuilder();
        if (hundred > 0 || full) text.append(digit[hundred]).append(" trăm");
        if (ten > 1) {
            if (text.length() > 0) text.append(' ');
            text.append(digit[ten]).append(" mươi");
            if (one == 1) text.append(" mốt");
            else if (one == 5) text.append(" lăm");
            else if (one > 0) text.append(' ').append(digit[one]);
        } else if (ten == 1) {
            if (text.length() > 0) text.append(' ');
            text.append("mười");
            if (one == 5) text.append(" lăm");
            else if (one > 0) text.append(' ').append(digit[one]);
        } else if (one > 0) {
            if (text.length() > 0) text.append(" lẻ ");
            text.append(digit[one]);
        }
        return text.toString();
    }

    private String capitalize(String text) {
        return text == null || text.isBlank() ? "" : text.substring(0, 1).toUpperCase(Locale.ROOT) + text.substring(1);
    }

    private String safeFileName(String value) {
        String raw = value == null || value.isBlank() ? "hoa-don" : value;
        return raw.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private record TicketPdfRow(String maVe, String hanhTrinh, String doanTau, String thoiGian, String choNgoi) {}


    // =========================================================================
    //  RENDERERS
    // =========================================================================

    class ItemRowRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lbl.setBorder(new EmptyBorder(0, 10, 0, 10));
            lbl.setForeground(ON_SURFACE);
            if (!sel) lbl.setBackground(r % 2 == 0 ? CARD_BG : ROW_ALT);
            return lbl;
        }
    }

    /** Renderer cột KM — hiển thị phần trăm giảm giá, ví dụ "−20%" */
    class KmRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
            String text = v != null ? v.toString() : "—";
            lbl.setText(text);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setBorder(new EmptyBorder(0, 4, 0, 4));
            if (!sel) {
                lbl.setBackground(r % 2 == 0 ? CARD_BG : ROW_ALT);
                lbl.setForeground("—".equals(text) ? ON_SURF_VAR : KM_FG);
            }
            return lbl;
        }
    }

    class TrangThaiRenderer extends JPanel implements TableCellRenderer {
        private final JLabel badge = new JLabel();
        private static final int BADGE_HEIGHT = 24;
        private static final int BADGE_MARGIN_X = 24;

        TrangThaiRenderer() {
            setLayout(new BorderLayout());
            setOpaque(true);
            setBorder(new EmptyBorder(0, BADGE_MARGIN_X, 0, BADGE_MARGIN_X));
            badge.setOpaque(false);
            badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
            badge.setHorizontalAlignment(SwingConstants.CENTER);
            badge.setPreferredSize(new Dimension(10, BADGE_HEIGHT));
            add(badge, BorderLayout.CENTER);
            // Handoff: trạng thái vé trong chi tiết hóa đơn dùng tag gần full ô như các bảng quản lý.
            // Dialog hẹp hơn module chính nên margin nhỏ hơn; tránh tăng để không cắt text trạng thái.
        }
        @Override public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            String text = v != null ? v.toString() : "";
            badge.setText(text);
            boolean sold = TrangThaiVe.DA_BAN.toString().equals(text);
            badge.setForeground(sold ? STATUS_SOLD_FG : STATUS_HUY_FG);
            setBackground(sel ? NotionTheme.TABLE_SELECTION : (r % 2 == 0 ? CARD_BG : ROW_ALT));
            return this;
        }

        @Override protected void paintChildren(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Rectangle r = badge.getBounds();
            String text = badge.getText();
            boolean sold = TrangThaiVe.DA_BAN.toString().equals(text);
            int y = r.y + Math.max(0, (r.height - BADGE_HEIGHT) / 2);
            g2.setColor(sold ? STATUS_SOLD_BG : STATUS_HUY_BG);
            g2.fillRoundRect(r.x, y, r.width, BADGE_HEIGHT, 14, 14);
            g2.dispose();
            super.paintChildren(g);
        }
    }

    class ItemAmountRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            String text = v instanceof BigDecimal
                    ? VND_FMT.format(v) + " ₫" : (v != null ? v.toString() : "");
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, text, sel, foc, r, c);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lbl.setForeground(sel ? ON_SURFACE : AMOUNT_COLOR);
            lbl.setHorizontalAlignment(SwingConstants.RIGHT);
            lbl.setBorder(new EmptyBorder(0, 8, 0, 12));
            if (!sel) lbl.setBackground(r % 2 == 0 ? CARD_BG : ROW_ALT);
            return lbl;
        }
    }

    // =========================================================================
    //  HELPERS
    // =========================================================================

    private String abbrev(String s, int maxLen) {
        if (s == null) return "";
        return s.length() <= maxLen ? s : s.substring(0, maxLen - 1) + "…";
    }

    private String khachHangNamesJoined() {
        if (khachHangList == null || khachHangList.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < khachHangList.size(); i++) {
            if (i > 0) sb.append(", ");
            String name = khachHangList.get(i).getHoTen();
            sb.append(name != null ? name : "—");
        }
        return sb.toString();
    }
}

