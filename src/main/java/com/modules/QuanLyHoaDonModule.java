package com.modules;

import com.dao.DAO_ChiTietHoaDon;
import com.dao.DAO_HoaDon;
import com.entity.HoaDon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class QuanLyHoaDonModule extends JPanel implements AppModule {

    private Consumer<Object> callback;

    // DAOs
    private final DAO_HoaDon        daoHD   = new DAO_HoaDon();
    private final DAO_ChiTietHoaDon daoCTHD = new DAO_ChiTietHoaDon();

    // Filter
    private JTextField txtSearch;

    // Table
    private JTable          table;
    private HoaDonTableModel tableModel;
    private int             hoveredRow = -1;

    // Pagination
    private int currentPage = 1;
    private int rowsPerPage = 11; // rowsPerPage + 1 per project convention
    private JLabel lblPageInfo;
    private JPanel paginationPanel;

    // Data
    private final List<HoaDonRow> allData      = new ArrayList<>();
    private final List<HoaDonRow> filteredData = new ArrayList<>();

    // Formatters
    private static final NumberFormat   VND_FMT  = NumberFormat.getInstance(new Locale("vi", "VN"));
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Design tokens
    private static final Color PRIMARY       = new Color(0x00, 0x5D, 0x90);
    private static final Color PRIMARY_LIGHT = new Color(0xE3, 0xF2, 0xFD);
    private static final Color SURFACE       = new Color(0xF8, 0xFA, 0xFC);
    private static final Color CARD_BG       = Color.WHITE;
    private static final Color ON_SURFACE    = new Color(0x1A, 0x1D, 0x21);
    private static final Color ON_SURF_VAR   = new Color(0x5F, 0x67, 0x70);
    private static final Color OUTLINE       = new Color(0xDE, 0xE3, 0xE8);
    private static final Color ROW_ALT       = new Color(0xF8, 0xFA, 0xFC);
    private static final Color ROW_HOVER     = new Color(0xEE, 0xF5, 0xFB);
    private static final Color AMOUNT_COLOR  = new Color(0x1A, 0x7A, 0x3C);

    private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD,  13);
    private static final Font FONT_BODY   = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_MONO   = new Font("Consolas",  Font.BOLD,  13);
    private static final Font FONT_AMOUNT = new Font("Segoe UI", Font.BOLD,  13);

    // AppModule buttons
    private JButton btnSubmit;
    private JButton btnCancel;
    private JPanel  btnPanel;

    // DTO row
    record HoaDonRow(HoaDon hoaDon, int soVe, BigDecimal tongTien) {}

    public QuanLyHoaDonModule() {
        setLayout(new BorderLayout());
        setBackground(SURFACE);
        buildUI();
    }

    // =========================================================================
    //  BUILD UI
    // =========================================================================

    private void buildUI() {
        add(buildHeader(),  BorderLayout.NORTH);
        add(buildBody(),    BorderLayout.CENTER);

        btnSubmit = new JButton("Xác nhận");
        btnCancel = new JButton("Hủy");
        btnCancel.addActionListener(e -> { if (callback != null) callback.accept(null); });
        btnPanel  = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btnPanel.setBackground(SURFACE);
        btnPanel.add(btnCancel); btnPanel.add(btnSubmit);
        btnPanel.setVisible(false);
        add(btnPanel, BorderLayout.SOUTH);

        loadData();
    }

    // ---- Header ----
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, OUTLINE),
                new EmptyBorder(18, 24, 18, 24)));

        // Icon + title
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setBackground(CARD_BG);

        JLabel iconLbl = loadIcon("/icons/bieuTuongHoaDon.png", 36);
        left.add(iconLbl);

        JPanel titles = new JPanel();
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setBackground(CARD_BG);
        JLabel t1 = new JLabel("Quản lý Hóa đơn");
        t1.setFont(new Font("Segoe UI", Font.BOLD, 20));
        t1.setForeground(ON_SURFACE);
        JLabel t2 = new JLabel("Danh sách hóa đơn bán vé trong hệ thống");
        t2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t2.setForeground(ON_SURF_VAR);
        titles.add(t1);
        titles.add(Box.createVerticalStrut(3));
        titles.add(t2);
        left.add(titles);

        p.add(left, BorderLayout.WEST);
        return p;
    }

    // ---- Body: filter + table + pagination ----
    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setBackground(SURFACE);
        body.setBorder(new EmptyBorder(16, 20, 16, 20));

        body.add(buildFilterBar(), BorderLayout.NORTH);
        body.add(buildTableCard(), BorderLayout.CENTER);
        body.add(buildPaginationBar(), BorderLayout.SOUTH);
        return body;
    }

    private JPanel buildFilterBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bar.setBackground(SURFACE);

        // Search field
        txtSearch = new JTextField(22);
        txtSearch.setFont(FONT_BODY);
        txtSearch.setPreferredSize(new Dimension(260, 34));
        txtSearch.putClientProperty("JTextField.placeholderText",
                "Tìm theo mã HĐ, khách hàng, nhân viên…");
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1),
                new EmptyBorder(4, 10, 4, 10)));
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
        });

        JLabel searchIcon = loadIcon("/icons/nutTimKiem.png", 18);
        JPanel searchWrap = new JPanel(new BorderLayout(4, 0));
        searchWrap.setBackground(SURFACE);
        searchWrap.add(searchIcon, BorderLayout.WEST);
        searchWrap.add(txtSearch, BorderLayout.CENTER);

        // Refresh button
        JButton btnRefresh = iconButton("/icons/nutBoLoc.png", "Làm mới", 18);
        btnRefresh.addActionListener(e -> { txtSearch.setText(""); loadData(); });

        bar.add(searchWrap);
        bar.add(btnRefresh);
        return bar;
    }

    private JPanel buildTableCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));

        String[] cols = {"", "Mã Hóa đơn", "Ngày lập", "Khách hàng", "SĐT", "Nhân viên", "Số vé", "Tổng tiền", "Chi tiết"};
        tableModel = new HoaDonTableModel(cols);
        table = new JTable(tableModel);
        table.setFont(FONT_BODY);
        table.setRowHeight(46);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(PRIMARY_LIGHT);
        table.setSelectionForeground(ON_SURFACE);
        table.setFillsViewportHeight(true);
        table.setBackground(CARD_BG);

        JTableHeader th = table.getTableHeader();
        th.setFont(FONT_HEADER);
        th.setBackground(new Color(0xF0, 0xF4, 0xF8));
        th.setForeground(ON_SURFACE);
        th.setReorderingAllowed(false);
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, OUTLINE));
        ((DefaultTableCellRenderer) th.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);

        // Column widths
        int[] widths = {40, 170, 140, 160, 120, 150, 60, 130, 100};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Renderers
        table.setDefaultRenderer(Object.class, new HoaDonRowRenderer());
        table.getColumnModel().getColumn(0).setCellRenderer(new IconCellRenderer());
        table.getColumnModel().getColumn(7).setCellRenderer(new AmountRenderer());
        table.getColumnModel().getColumn(8).setCellRenderer(new DetailBtnRenderer());
        table.getColumnModel().getColumn(8).setCellEditor(new DetailBtnEditor(new JCheckBox()));

        // Hover
        table.addMouseMotionListener(new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                int r = table.rowAtPoint(e.getPoint());
                if (r != hoveredRow) { hoveredRow = r; table.repaint(); }
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseExited(MouseEvent e) { hoveredRow = -1; table.repaint(); }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(CARD_BG);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildPaginationBar() {
        paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        paginationPanel.setBackground(SURFACE);
        lblPageInfo = new JLabel();
        lblPageInfo.setFont(FONT_BODY);
        lblPageInfo.setForeground(ON_SURF_VAR);
        return paginationPanel;
    }

    // =========================================================================
    //  DATA & FILTER
    // =========================================================================

    private void loadData() {
        allData.clear();
        for (HoaDon hd : daoHD.getAll()) {
            int soVe      = daoCTHD.getSoVeByHoaDon(hd.getMaHoaDon());
            BigDecimal tong = daoCTHD.getTongTienByHoaDon(hd.getMaHoaDon());
            allData.add(new HoaDonRow(hd, soVe, tong));
        }
        applyFilter();
    }

    private void applyFilter() {
        String kw = txtSearch.getText().trim().toLowerCase();
        filteredData.clear();
        filteredData.addAll(allData.stream()
                .filter(r -> {
                    if (kw.isEmpty()) return true;
                    String maHD  = r.hoaDon().getMaHoaDon().toLowerCase();
                    String kh    = r.hoaDon().getKhachHang() != null
                            ? r.hoaDon().getKhachHang().getHoTen().toLowerCase() : "";
                    String nv    = r.hoaDon().getNhanVien()  != null
                            ? r.hoaDon().getNhanVien().getHoTen().toLowerCase()  : "";
                    return maHD.contains(kw) || kh.contains(kw) || nv.contains(kw);
                })
                .collect(Collectors.toList()));
        currentPage = 1;
        refreshTable();
    }

    private void refreshTable() {
        int total = filteredData.size();
        int pages = Math.max(1, (int) Math.ceil((double) total / rowsPerPage));
        if (currentPage > pages) currentPage = pages;

        int from = (currentPage - 1) * rowsPerPage;
        int to   = Math.min(from + rowsPerPage, total);
        List<HoaDonRow> page = filteredData.subList(from, to);

        tableModel.setData(page, from);
        buildPaginationControls(pages);
    }

    private void buildPaginationControls(int totalPages) {
        paginationPanel.removeAll();

        lblPageInfo.setText("Trang " + currentPage + " / " + totalPages
                + "  (" + filteredData.size() + " hóa đơn)");
        paginationPanel.add(lblPageInfo);

        if (totalPages > 1) {
            JButton prev = pageBtn("<");
            prev.setEnabled(currentPage > 1);
            prev.addActionListener(e -> { currentPage--; refreshTable(); });

            JButton next = pageBtn(">");
            next.setEnabled(currentPage < totalPages);
            next.addActionListener(e -> { currentPage++; refreshTable(); });

            paginationPanel.add(Box.createHorizontalStrut(10));
            paginationPanel.add(prev);
            for (int i = 1; i <= totalPages; i++) {
                final int p = i;
                JButton pb = pageBtn(String.valueOf(i));
                if (i == currentPage) {
                    pb.setBackground(PRIMARY);
                    pb.setForeground(Color.WHITE);
                }
                pb.addActionListener(e -> { currentPage = p; refreshTable(); });
                paginationPanel.add(pb);
            }
            paginationPanel.add(next);
        }

        paginationPanel.revalidate();
        paginationPanel.repaint();
    }

    private JButton pageBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(FONT_BODY);
        b.setPreferredSize(new Dimension(34, 28));
        b.setBackground(CARD_BG);
        b.setForeground(ON_SURFACE);
        b.setBorder(BorderFactory.createLineBorder(OUTLINE, 1));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void openChiTiet(int row) {
        if (row < 0) return;
        List<HoaDonRow> page = tableModel.getCurrentPage();
        if (row >= page.size()) return;
        HoaDon hd = page.get(row).hoaDon();
        Window owner = SwingUtilities.getWindowAncestor(this);
        JFrame frame = owner instanceof JFrame ? (JFrame) owner : null;
        new ChiTietHoaDonDialog(frame, hd).setVisible(true);
    }

    // =========================================================================
    //  TABLE MODEL
    // =========================================================================

    class HoaDonTableModel extends AbstractTableModel {
        private final String[]        cols;
        private final List<HoaDonRow> data = new ArrayList<>();
        private int                   rowOffset = 0;

        HoaDonTableModel(String[] cols) { this.cols = cols; }

        void setData(List<HoaDonRow> list, int offset) {
            data.clear(); data.addAll(list);
            this.rowOffset = offset;
            fireTableDataChanged();
        }

        List<HoaDonRow> getCurrentPage() { return data; }

        @Override public int getRowCount()    { return data.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }
        @Override public boolean isCellEditable(int r, int c) { return c == 8; }

        @Override public Object getValueAt(int r, int c) {
            HoaDonRow row = data.get(r);
            HoaDon hd = row.hoaDon();
            return switch (c) {
                case 0 -> "icon";
                case 1 -> hd.getMaHoaDon();
                case 2 -> hd.getNgayLap() != null ? hd.getNgayLap().format(DT_FMT) : "";
                case 3 -> hd.getKhachHang() != null ? hd.getKhachHang().getHoTen() : "";
                case 4 -> hd.getKhachHang() != null ? hd.getKhachHang().getSoDienThoai() : "";
                case 5 -> hd.getNhanVien()  != null ? hd.getNhanVien().getHoTen()  : "";
                case 6 -> row.soVe();
                case 7 -> row.tongTien();
                case 8 -> "Chi tiết";
                default -> "";
            };
        }
    }

    // =========================================================================
    //  RENDERERS & EDITORS
    // =========================================================================

    class HoaDonRowRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
            lbl.setFont(FONT_BODY);
            lbl.setBorder(new EmptyBorder(0, 12, 0, 12));
            lbl.setForeground(ON_SURFACE);
            if (!sel) lbl.setBackground(r == hoveredRow ? ROW_HOVER : (r % 2 == 0 ? CARD_BG : ROW_ALT));
            return lbl;
        }
    }

    class IconCellRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
            lbl.setText("");
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setIcon(loadIcon("/icons/bieuTuongHoaDon.png", 22).getIcon());
            if (!sel) lbl.setBackground(r == hoveredRow ? ROW_HOVER : (r % 2 == 0 ? CARD_BG : ROW_ALT));
            return lbl;
        }
    }

    class AmountRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            BigDecimal amt = v instanceof BigDecimal ? (BigDecimal) v : BigDecimal.ZERO;
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(t,
                    VND_FMT.format(amt) + " ₫", sel, foc, r, c);
            lbl.setFont(FONT_AMOUNT);
            lbl.setForeground(sel ? ON_SURFACE : AMOUNT_COLOR);
            lbl.setHorizontalAlignment(SwingConstants.RIGHT);
            lbl.setBorder(new EmptyBorder(0, 8, 0, 16));
            if (!sel) lbl.setBackground(r == hoveredRow ? ROW_HOVER : (r % 2 == 0 ? CARD_BG : ROW_ALT));
            return lbl;
        }
    }

    class DetailBtnRenderer extends JPanel implements TableCellRenderer {
        private final JButton btn = new JButton("Chi tiết");
        DetailBtnRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 0, 7));
            setOpaque(true);
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            btn.setForeground(PRIMARY);
            btn.setBackground(PRIMARY_LIGHT);
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0xBB, 0xDE, 0xFB), 1),
                    new EmptyBorder(4, 12, 4, 12)));
            btn.setFocusPainted(false);
            btn.setOpaque(true);
            add(btn);
        }
        @Override public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            setBackground(sel ? PRIMARY_LIGHT : (r == hoveredRow ? ROW_HOVER : (r % 2 == 0 ? CARD_BG : ROW_ALT)));
            return this;
        }
    }

    class DetailBtnEditor extends DefaultCellEditor {
        private final JButton btn;
        private int editingRow = -1;
        DetailBtnEditor(JCheckBox cb) {
            super(cb);
            btn = new JButton("Chi tiết");
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            btn.setForeground(PRIMARY);
            btn.setBackground(PRIMARY_LIGHT);
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0xBB, 0xDE, 0xFB), 1),
                    new EmptyBorder(4, 12, 4, 12)));
            btn.setFocusPainted(false);
            btn.addActionListener(e -> { fireEditingStopped(); openChiTiet(editingRow); });
        }
        @Override public Component getTableCellEditorComponent(
                JTable t, Object v, boolean sel, int r, int c) { editingRow = r; return btn; }
        @Override public Object getCellEditorValue() { return "Chi tiết"; }
    }

    // =========================================================================
    //  HELPERS
    // =========================================================================

    private JLabel loadIcon(String path, int size) {
        JLabel lbl = new JLabel();
        try {
            ImageIcon raw = new ImageIcon(getClass().getResource(path));
            lbl.setIcon(new ImageIcon(raw.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH)));
        } catch (Exception ignored) {}
        return lbl;
    }

    private JButton iconButton(String iconPath, String tooltip, int iconSize) {
        JButton b = new JButton();
        b.setToolTipText(tooltip);
        b.setFocusPainted(false);
        b.setBackground(CARD_BG);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1),
                new EmptyBorder(4, 10, 4, 10)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        try {
            ImageIcon ic = new ImageIcon(getClass().getResource(iconPath));
            b.setIcon(new ImageIcon(ic.getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH)));
        } catch (Exception ignored) { b.setText(tooltip); }
        return b;
    }

    // =========================================================================
    //  AppModule
    // =========================================================================

    @Override public String getTitle() { return "Quản lý Hóa đơn"; }
    @Override public JPanel getView()  { return this; }
    @Override public void setOnResult(Consumer<Object> cb) {
        this.callback = cb;
        boolean show = cb != null;
        btnSubmit.setVisible(show); btnCancel.setVisible(show); btnPanel.setVisible(show);
    }
    @Override public void reset() { txtSearch.setText(""); loadData(); }
}
