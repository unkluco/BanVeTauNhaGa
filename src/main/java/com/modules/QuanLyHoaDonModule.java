package com.modules;

import com.dao.DAO_ChiTietHoaDon;
import com.dao.DAO_HoaDon;
import com.dao.DAO_HoaDonKhachHang;
import com.entity.HoaDon;
import com.entity.KhachHang;

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
    private final DAO_HoaDon            daoHD   = new DAO_HoaDon();
    private final DAO_ChiTietHoaDon     daoCTHD = new DAO_ChiTietHoaDon();
    private final DAO_HoaDonKhachHang   daoHDKH = new DAO_HoaDonKhachHang();

    // Filter
    private JTextField txtSearch;

    // Table
    private JTable          table;
    private HoaDonTableModel tableModel;
    private int             hoveredRow = -1;

    // Pagination
    private JLabel lblPageInfo;
    private JLabel lblTableMeta;
    private JLabel lblStatTotal;
    private JLabel lblStatTickets;
    private JLabel lblStatRevenue;

    // Data
    private final List<HoaDonRow> allData      = new ArrayList<>();
    private final List<HoaDonRow> filteredData = new ArrayList<>();

    // Formatters
    private static final NumberFormat   VND_FMT  = NumberFormat.getInstance(new Locale("vi", "VN"));
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Design tokens
    private static final Color PRIMARY       = NotionTheme.ACCENT;
    private static final Color PRIMARY_LIGHT = NotionTheme.ACCENT_SOFT;
    private static final Color SURFACE       = NotionTheme.PAGE;
    private static final Color CARD_BG       = NotionTheme.CARD;
    private static final Color ON_SURFACE    = NotionTheme.TEXT;
    private static final Color ON_SURF_VAR   = NotionTheme.TEXT_MUTED;
    private static final Color OUTLINE       = NotionTheme.BORDER;
    private static final Color ROW_ALT       = NotionTheme.CARD_MUTED;
    private static final Color ROW_HOVER     = NotionTheme.ROW_HOVER;
    private static final Color AMOUNT_COLOR  = new Color(0x1A, 0xAE, 0x39);
    private static final Color ERROR_FG      = new Color(0xE0, 0x31, 0x31);

    private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD,  13);
    private static final Font FONT_BODY   = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_MONO   = new Font("Consolas",  Font.BOLD,  13);
    private static final Font FONT_AMOUNT = new Font("Segoe UI", Font.BOLD,  13);

    // AppModule buttons
    private JButton btnSubmit;
    private JButton btnCancel;
    private JPanel  btnPanel;

    // DTO row
    record HoaDonRow(HoaDon hoaDon, List<KhachHang> khachHangs, int soVe, BigDecimal tongTien) {}

    public QuanLyHoaDonModule() {
        setLayout(new BorderLayout());
        setBackground(SURFACE);
        setBorder(NotionTheme.pageBorder());
        buildUI();
    }

    // =========================================================================
    //  BUILD UI
    // =========================================================================

    private void buildUI() {
        JPanel main = new JPanel(new BorderLayout(0, 16));
        main.setOpaque(false);
        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        JPanel header = buildHeader();
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel stats = buildStatsRow();
        stats.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(header);
        top.add(Box.createVerticalStrut(16));
        top.add(stats);
        main.add(top, BorderLayout.NORTH);
        main.add(buildBody(), BorderLayout.CENTER);
        add(main, BorderLayout.CENTER);

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
        JPanel hdr = new JPanel(new BorderLayout(24, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, NotionTheme.NAVY, getWidth(), getHeight(), NotionTheme.ACCENT);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                g2.setColor(AppColors.withAlpha(NotionTheme.ACCENT_SOFT, 150));
                g2.fillRoundRect(getWidth() - 198, 12, 138, 138, 34, 34);
                g2.setColor(AppColors.withAlpha(NotionTheme.ROSE, 112));
                g2.fillOval(getWidth() - 315, 92, 150, 150);
                g2.setColor(AppColors.withAlpha(Color.WHITE, 40));
                g2.fillRoundRect(getWidth() - 270, 38, 116, 14, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        hdr.setOpaque(false);
        hdr.setBorder(new EmptyBorder(24, 28, 24, 28));
        hdr.setPreferredSize(new Dimension(10, 150));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        JLabel eyebrow = new JLabel("WORKSPACE / NGHIỆP VỤ");
        eyebrow.setFont(new Font("Segoe UI", Font.BOLD, 11));
        eyebrow.setForeground(AppColors.withAlpha(Color.WHITE, 190));
        JLabel title = new JLabel("Quản lý Hóa đơn");
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(Color.WHITE);
        JLabel desc = new JLabel("Tra cứu hóa đơn, theo dõi khách hàng và mở chi tiết giao dịch bán vé.");
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        desc.setForeground(AppColors.withAlpha(Color.WHITE, 210));
        left.add(eyebrow);
        left.add(Box.createVerticalStrut(8));
        left.add(title);
        left.add(Box.createVerticalStrut(8));
        left.add(desc);

        JPanel right = new JPanel(new GridBagLayout());
        right.setOpaque(false);
        JLabel badge = createHeroBadge("Hóa đơn bán vé");
        right.add(badge);

        hdr.add(left, BorderLayout.CENTER);
        hdr.add(right, BorderLayout.EAST);
        return hdr;
    }

    private JPanel buildStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));
        lblStatTotal = new JLabel("0");
        lblStatTickets = new JLabel("0");
        lblStatRevenue = new JLabel("0 ₫");
        row.add(buildStatCard("Hóa đơn", lblStatTotal, AppColors.PRIMARY, NotionTheme.SKY));
        row.add(buildStatCard("Số vé đã bán", lblStatTickets, AppColors.WARNING_DARK, NotionTheme.YELLOW));
        row.add(buildStatCard("Doanh thu", lblStatRevenue, AppColors.SUCCESS_DARK, NotionTheme.MINT));
        return row;
    }

    private JPanel buildStatCard(String label, JLabel value, Color accent, Color tint) {
        JPanel card = new JPanel(new BorderLayout(10, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppColors.withAlpha(tint, 190));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(AppColors.withAlpha(accent, 55));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(14, 18, 14, 18));
        JPanel marker = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
            }
        };
        marker.setOpaque(false);
        marker.setPreferredSize(new Dimension(8, 48));
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        value.setFont(new Font("Segoe UI", Font.BOLD, 24));
        value.setForeground(accent);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(ON_SURF_VAR);
        text.add(value);
        text.add(Box.createVerticalStrut(2));
        text.add(lbl);
        card.add(marker, BorderLayout.WEST);
        card.add(text, BorderLayout.CENTER);
        return card;
    }

    // ---- Body: filter + scrollable table ----
    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);
        body.add(buildFilterBar(), BorderLayout.NORTH);
        body.add(buildTableCard(), BorderLayout.CENTER);
        return body;
    }

    private JPanel buildFilterBar() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 12)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                NotionTheme.paintCard(g2, this, CARD_BG, OUTLINE, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(18, 22, 18, 22));

        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        JLabel title = new JLabel("Bộ lọc hóa đơn");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(ON_SURFACE);
        JLabel subtitle = new JLabel("Tìm theo mã hóa đơn, khách hàng hoặc nhân viên");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(ON_SURF_VAR);
        header.add(title, BorderLayout.WEST);
        header.add(subtitle, BorderLayout.EAST);

        JPanel filterGrid = new JPanel(new GridBagLayout());
        filterGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 0, 12);

        JPanel searchBox = new JPanel(new BorderLayout(10, 0));
        searchBox.setOpaque(false);
        searchBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1, true),
                new EmptyBorder(9, 14, 9, 14)));

        JLabel iconSearch = loadIcon(LineIcons.Name.SEARCH, 18);
        searchBox.add(iconSearch, BorderLayout.WEST);

        txtSearch = new JTextField();
        txtSearch.setFont(FONT_BODY);
        txtSearch.setOpaque(false);
        txtSearch.setBorder(BorderFactory.createEmptyBorder());
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm theo mã HĐ, khách hàng, nhân viên…");
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
        });
        searchBox.add(txtSearch, BorderLayout.CENTER);
        SearchFieldClearButton.install(searchBox, txtSearch, this::applyFilter);

        gbc.gridx = 0;
        gbc.insets = new Insets(0, 0, 0, 0);
        filterGrid.add(buildFilterGroup("TÌM KIẾM", searchBox), gbc);

        wrapper.add(header, BorderLayout.NORTH);
        // Handoff: hóa đơn hiện chỉ có tìm kiếm nên bỏ nút Bỏ lọc, clear bằng X trong field.
        // Cảnh báo: filterGrid giữ mỗi ô tự canh hàng; nếu thêm filter thật, thêm vào grid này.
        wrapper.add(filterGrid, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildFilterGroup(String labelText, JComponent field) {
        JPanel group = new JPanel(new BorderLayout(0, 6));
        group.setOpaque(false);
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setForeground(ON_SURF_VAR);
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 42));
        field.setMinimumSize(new Dimension(120, 42));
        group.add(label, BorderLayout.NORTH);
        group.add(field, BorderLayout.CENTER);
        return group;
        // Handoff: nhóm filter chuẩn hóa label + chiều cao control để tránh lệch/che khuất.
        // Cảnh báo: không nhét nút không phải filter vào đây nếu không có label rõ nghĩa.
    }

    private JPanel buildTableCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                NotionTheme.paintCard(g2, this, CARD_BG, OUTLINE, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        JLabel tableTitle = new JLabel("Danh sách hóa đơn");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tableTitle.setForeground(ON_SURFACE);
        lblTableMeta = createMetaBadge("0 hóa đơn");
        topBar.add(tableTitle, BorderLayout.WEST);
        topBar.add(lblTableMeta, BorderLayout.EAST);
        card.add(topBar, BorderLayout.NORTH);

        String[] cols = {"Mã Hóa đơn", "Ngày lập", "Khách hàng", "SĐT", "Nhân viên", "Số vé", "Tổng tiền", "Chi tiết"};
        tableModel = new HoaDonTableModel(cols);
        table = new JTable(tableModel);
        NotionTheme.styleTable(table);
        table.setRowHeight(56);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(OUTLINE);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(NotionTheme.TABLE_SELECTION);
        table.setSelectionForeground(ON_SURFACE);
        table.setFillsViewportHeight(true);
        table.setBackground(CARD_BG);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        JTableHeader th = table.getTableHeader();
        th.setReorderingAllowed(false);
        th.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setFont(FONT_HEADER);
                lbl.setForeground(ON_SURF_VAR);
                lbl.setBackground(NotionTheme.PAGE);
                lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, OUTLINE),
                        new EmptyBorder(0, 20, 0, 8)));
                lbl.setPreferredSize(new Dimension(lbl.getPreferredSize().width, 44));
                return lbl;
            }
        });

        TableColumnModel colModel = table.getColumnModel();
        int[] widths = {150, 140, 180, 120, 160, 80, 140, 110};
        for (int i = 0; i < widths.length; i++) colModel.getColumn(i).setPreferredWidth(widths[i]);

        colModel.getColumn(0).setCellRenderer(new HoaDonTextRenderer(FONT_MONO, PRIMARY, SwingConstants.LEFT));
        colModel.getColumn(1).setCellRenderer(new HoaDonTextRenderer(FONT_BODY, ON_SURF_VAR, SwingConstants.LEFT));
        colModel.getColumn(2).setCellRenderer(new HoaDonTextRenderer(new Font("Segoe UI", Font.BOLD, 13), ON_SURFACE, SwingConstants.LEFT));
        colModel.getColumn(3).setCellRenderer(new HoaDonTextRenderer(FONT_BODY, ON_SURF_VAR, SwingConstants.LEFT));
        colModel.getColumn(4).setCellRenderer(new HoaDonTextRenderer(FONT_BODY, ON_SURF_VAR, SwingConstants.LEFT));
        colModel.getColumn(5).setCellRenderer(new HoaDonTextRenderer(FONT_BODY, ON_SURFACE, SwingConstants.CENTER));
        colModel.getColumn(6).setCellRenderer(new AmountRenderer());
        colModel.getColumn(7).setCellRenderer(new DetailBtnRenderer());
        colModel.getColumn(7).setCellEditor(new DetailBtnEditor(new JCheckBox()));

        // Hover
        table.addMouseMotionListener(new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                int r = table.rowAtPoint(e.getPoint());
                if (r != hoveredRow) { hoveredRow = r; table.repaint(); }
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseExited(MouseEvent e) { hoveredRow = -1; table.repaint(); }
            @Override public void mousePressed(MouseEvent e) { showHoaDonQuickActions(e); }
            @Override public void mouseReleased(MouseEvent e) { showHoaDonQuickActions(e); }

            @Override public void mouseClicked(MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e) || e.getClickCount() != 2) return;
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row < 0 || col == 7) return;
                openChiTiet(row);
            }
        });
        // Handoff: thân dòng hóa đơn mở bằng double-click; nút Chi tiết vẫn single-click.
        // Cảnh báo: nếu JTable có sorter thì row view cần convert về model trước khi mở.

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setBackground(CARD_BG);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setWheelScrollingEnabled(true);
        card.add(scroll, BorderLayout.CENTER);
        card.add(buildTableFooter(), BorderLayout.SOUTH);
        return card;
    }

        private JPanel buildTableFooter() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, OUTLINE),
                new EmptyBorder(12, 20, 12, 20)
        ));

        lblPageInfo = new JLabel();
        lblPageInfo.setFont(FONT_BODY);
        lblPageInfo.setForeground(ON_SURF_VAR);


        bar.add(lblPageInfo, BorderLayout.WEST);
        // Handoff: footer chỉ hiển thị tổng số hóa đơn sau lọc, không còn điều hướng phân trang.
        // Cảnh báo: nếu thêm tổng tiền theo filter thì cập nhật cùng refreshTable để tránh lệch số liệu.
        return bar;
    }

    // =========================================================================
    //  DATA & FILTER
    // =========================================================================

    private void loadData() {
        allData.clear();
        for (HoaDon hd : daoHD.getAll()) {
            int soVe          = daoCTHD.getSoVeByHoaDon(hd.getMaHoaDon());
            BigDecimal tong   = daoCTHD.getTongTienByHoaDon(hd.getMaHoaDon());
            List<KhachHang> khs = daoHDKH.findKhachHangByHoaDon(hd.getMaHoaDon());
            allData.add(new HoaDonRow(hd, khs, soVe, tong));
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
                    boolean khMatch = r.khachHangs() != null && r.khachHangs().stream()
                            .anyMatch(kh -> kh.getHoTen() != null
                                    && kh.getHoTen().toLowerCase().contains(kw));
                    String nv    = r.hoaDon().getNhanVien()  != null
                            ? r.hoaDon().getNhanVien().getHoTen().toLowerCase()  : "";
                    return maHD.contains(kw) || khMatch || nv.contains(kw);
                })
                .collect(Collectors.toList()));
        refreshTable();
    }

    private void updateStats(List<HoaDonRow> source) {
        int totalInvoices = source.size();
        int totalTickets = source.stream().mapToInt(HoaDonRow::soVe).sum();
        BigDecimal totalRevenue = source.stream()
                .map(HoaDonRow::tongTien)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        lblStatTotal.setText(String.valueOf(totalInvoices));
        lblStatTickets.setText(String.valueOf(totalTickets));
        lblStatRevenue.setText(VND_FMT.format(totalRevenue) + " ₫");
        // Handoff: thống kê phản ánh đúng danh sách đang lọc, không chỉ toàn bộ hóa đơn.
        // Risk: tongTien lấy từ ChiTietHoaDon; nếu dữ liệu chi tiết thiếu thì doanh thu về 0 cho hóa đơn đó.
    }
    private void refreshTable() {
        tableModel.setData(filteredData);
        updateStats(filteredData);
        int total = filteredData.size();
        lblPageInfo.setText(total == 0
                ? "Không tìm thấy hóa đơn nào"
                : "Hiển thị " + total + " hóa đơn");
    }
    private void showHoaDonQuickActions(MouseEvent e) {
        if (!e.isPopupTrigger()) return;
        int row = table.rowAtPoint(e.getPoint());
        if (row < 0) return;
        table.setRowSelectionInterval(row, row);
        JPopupMenu menu = new JPopupMenu();
        JMenuItem view = new JMenuItem("Xem chi tiết");
        view.addActionListener(ev -> openChiTiet(row));
        menu.add(view);
        menu.show(table, e.getX(), e.getY());
    }

    private void openChiTiet(int row) {
        if (row < 0) return;
        HoaDonRow selectedRow = tableModel.getRowAt(row);
        if (selectedRow == null) return;
        HoaDon hd = selectedRow.hoaDon();
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

        HoaDonTableModel(String[] cols) { this.cols = cols; }

        void setData(List<HoaDonRow> list) {
            data.clear(); data.addAll(list);
            fireTableDataChanged();
        }

        HoaDonRow getRowAt(int row) {
            return row >= 0 && row < data.size() ? data.get(row) : null;
        }

        @Override public int getRowCount()    { return data.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }
        @Override public boolean isCellEditable(int r, int c) { return c == 7; }

        @Override public Object getValueAt(int r, int c) {
            HoaDonRow row = data.get(r);
            HoaDon hd = row.hoaDon();
            List<KhachHang> khs = row.khachHangs();
            KhachHang first = (khs != null && !khs.isEmpty()) ? khs.get(0) : null;
            int extra = (khs != null && khs.size() > 1) ? khs.size() - 1 : 0;
            return switch (c) {
                case 0 -> hd.getMaHoaDon();
                case 1 -> hd.getNgayLap() != null ? hd.getNgayLap().format(DT_FMT) : "";
                case 2 -> first != null
                        ? (first.getHoTen() != null ? first.getHoTen() : "")
                          + (extra > 0 ? " (+" + extra + ")" : "")
                        : "";
                case 3 -> first != null && first.getSoDienThoai() != null ? first.getSoDienThoai() : "";
                case 4 -> hd.getNhanVien()  != null ? hd.getNhanVien().getHoTen()  : "";
                case 5 -> row.soVe();
                case 6 -> row.tongTien();
                case 7 -> "Chi tiết";
                default -> "";
            };
        }
    }

    // =========================================================================
    //  RENDERERS & EDITORS
    // =========================================================================

    class HoaDonTextRenderer extends DefaultTableCellRenderer {
        private final Font font;
        private final Color fg;

        HoaDonTextRenderer(Font font, Color fg, int align) {
            this.font = font;
            this.fg = fg;
            setHorizontalAlignment(align);
        }

        @Override public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
            lbl.setFont(font);
            lbl.setBorder(new EmptyBorder(0, 20, 0, 8));
            lbl.setForeground(sel ? ON_SURFACE : fg);
            lbl.setBackground(rowBg(sel, r));
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
            lbl.setBorder(new EmptyBorder(0, 8, 0, 20));
            lbl.setBackground(rowBg(sel, r));
            return lbl;
        }
    }

    class DetailBtnRenderer extends JPanel implements TableCellRenderer {
        private final JLabel label = new JLabel("Chi tiết", SwingConstants.CENTER);
        DetailBtnRenderer() {
            setLayout(new GridBagLayout());
            setOpaque(true);
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            label.setForeground(PRIMARY);
            add(label);
        }
        @Override public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            label.setText(v != null ? v.toString() : "Chi tiết");
            setBackground(rowBg(sel, r));
            return this;
        }
        @Override protected void paintChildren(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Rectangle r = label.getBounds();
            g2.setColor(PRIMARY_LIGHT);
            g2.fillRoundRect(r.x - 12, r.y - 5, r.width + 24, r.height + 10, 10, 10);
            g2.setColor(AppColors.withAlpha(PRIMARY, 60));
            g2.drawRoundRect(r.x - 12, r.y - 5, r.width + 24, r.height + 10, 10, 10);
            g2.dispose();
            super.paintChildren(g);
        }
    }

    class DetailBtnEditor extends DefaultCellEditor {
        private final JButton btn;
        private int editingRow = -1;
        DetailBtnEditor(JCheckBox cb) {
            super(cb);
            btn = createTableActionButton("Chi tiết", PRIMARY, PRIMARY_LIGHT);
            btn.addActionListener(e -> { fireEditingStopped(); openChiTiet(editingRow); });
        }
        @Override public Component getTableCellEditorComponent(
                JTable t, Object v, boolean sel, int r, int c) {
            editingRow = r;
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBackground(rowBg(true, r));
            panel.add(btn);
            return panel;
        }
        @Override public Object getCellEditorValue() { return "Chi tiết"; }
    }

    private Color rowBg(boolean selected, int row) {
        if (selected) return NotionTheme.TABLE_SELECTION;
        if (row == hoveredRow) return ROW_HOVER;
        return row % 2 == 0 ? CARD_BG : ROW_ALT;
    }

    // =========================================================================
    //  HELPERS
    // =========================================================================

    private JLabel createHeroBadge(String text) {
        JLabel badge = new JLabel(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppColors.withAlpha(Color.WHITE, 235));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(new Font("Segoe UI", Font.BOLD, 13));
        badge.setForeground(NotionTheme.NAVY);
        badge.setBorder(new EmptyBorder(10, 16, 10, 16));
        return badge;
    }

    private JLabel createMetaBadge(String text) {
        JLabel badge = new JLabel(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(NotionTheme.ACCENT_SOFT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(AppColors.withAlpha(PRIMARY, 70));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        badge.setForeground(PRIMARY);
        badge.setBorder(new EmptyBorder(5, 10, 5, 10));
        return badge;
    }

    private JButton createSecondaryButton(String text, int width, int height) {
        JButton button = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? NotionTheme.CARD_MUTED : CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(OUTLINE);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(ON_SURF_VAR);
        button.setPreferredSize(new Dimension(width, height));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JButton createTableActionButton(String text, Color accent, Color tint) {
        JButton button = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? tint : CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(AppColors.withAlpha(accent, 95));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(accent);
        button.setPreferredSize(new Dimension(76, 30));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JLabel loadIcon(LineIcons.Name iconName, int size) {
        JLabel lbl = new JLabel(LineIcons.image(iconName, size, size));
        // Handoff: module icons use LineIcons enum directly, independent of legacy SVG resource files.
        // Risk: only use search icons for real search fields, not filter controls.
        return lbl;
    }

    private JButton iconButton(LineIcons.Name iconName, String tooltip, int iconSize) {
        JButton b = new JButton();
        b.setToolTipText(tooltip);
        b.setFocusPainted(false);
        b.setBackground(CARD_BG);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1),
                new EmptyBorder(4, 10, 4, 10)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setIcon(LineIcons.contained(iconName, iconSize, Math.max(8, iconSize - 8)));
        // Handoff: action buttons use LineIcons enum directly, so deleted SVG files are safe.
        // Risk: if a new action icon is needed, add a curated LineIcons.Name first.
        return b;
    }

    // =========================================================================
    public void applySearch(String text) {
        txtSearch.setText(text);
        applyFilter();
    }

    /**
     * Nhận keyword từ Dashboard — tìm trên maHoaDon, hoTen KH, hoTen NV (đã có trong applyFilter).
     */
    public void applySearchFromDashboard(String keyword) {
        txtSearch.setText(keyword != null ? keyword.trim() : "");
        applyFilter();
    }

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



