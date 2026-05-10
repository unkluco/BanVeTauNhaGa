package com.modules;

import com.dao.DAO_ApDungKM;
import com.dao.DAO_ChiTietHoaDon;
import com.dao.DAO_HoaDonKhachHang;
import com.entity.*;
import com.enums.TrangThaiVe;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
        setLocationRelativeTo(owner);
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
        setContentPane(ThemNhanVienDialog.buildShadowWrapper(buildInvoicePanel(true)));
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
                    if (isShowing() && !isFocused()) dispose();
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

        // 11-column table
        String[] cols = {
            "STT", "Mã vé", "Ga đi", "Ga đến",
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
        int[] widths = {40, 80, 130, 130, 120, 110, 110, 70, 100, 70, 110};
        for (int i = 0; i < widths.length; i++)
            tbl.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Default renderer (handles alternating row colors)
        tbl.setDefaultRenderer(Object.class, new ItemRowRenderer());

        // Column-specific renderers
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        tbl.getColumnModel().getColumn(0).setCellRenderer(center);  // STT
        tbl.getColumnModel().getColumn(7).setCellRenderer(center);  // Ghế số
        tbl.getColumnModel().getColumn(8).setCellRenderer(new TrangThaiRenderer());
        tbl.getColumnModel().getColumn(9).setCellRenderer(new KmRenderer());
        tbl.getColumnModel().getColumn(10).setCellRenderer(new ItemAmountRenderer());

        JScrollPane scroll = new JScrollPane(tbl);
        scroll.setBorder(BorderFactory.createLineBorder(OUTLINE, 1));
        scroll.getViewport().setBackground(CARD_BG);
        scroll.setPreferredSize(new Dimension(0, Math.min(300, 42 + chiTietList.size() * 40 + 2)));

        section.add(top,    BorderLayout.NORTH);
        section.add(scroll, BorderLayout.CENTER);
        return section;
    }

    private Object[][] buildTableData() {
        Object[][] data = new Object[chiTietList.size()][11];
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
            data[i][2]  = gaDi;
            data[i][3]  = gaDen;
            data[i][4]  = doanTau;
            data[i][5]  = ngayDi;
            data[i][6]  = loaiGhe;
            data[i][7]  = soGhe;
            data[i][8]  = trangThai;
            data[i][9]  = kmText;
            data[i][10] = ct.getGiaTien();
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

        JButton btnClose = styledBtn("Đóng", PRIMARY, AppColors.SURFACE);
        btnClose.addActionListener(e -> dispose());

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

