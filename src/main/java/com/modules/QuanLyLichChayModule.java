package com.modules;

import com.dao.DAO_Ga;
import com.dao.DAO_Lich;
import com.entity.DoanTau;
import com.entity.Ga;
import com.entity.Lich;
import com.entity.Tuyen;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class QuanLyLichChayModule extends JPanel implements AppModule {

    private Consumer<Object> callback;

    // --- Data ---
    private List<Lich> allData      = new ArrayList<>();
    private List<Lich> filteredData = new ArrayList<>();
    private final DAO_Lich daoLich  = new DAO_Lich();
    private final DAO_Ga   daoGa    = new DAO_Ga();

    // --- Pagination ---
    private int currentPage  = 1;
    private int rowsPerPage  = 10;
    private int totalRecords = 0;
    private boolean isRefreshing = false;
    private JLabel lblPageInfo;
    private JPanel paginationPanel;

    // --- Filter ---
    private SearchableComboBox<Ga> filterGaDi;
    private SearchableComboBox<Ga> filterGaDen;
    private DatePickerField         dateFrom;
    private DatePickerField         dateTo;

    // --- UI ---
    private JTextField    txtSearch;
    private JTable        table;
    private LichTableModel tableModel;
    private int           hoveredRow = -1;

    // --- Stats labels ---
    private JLabel lblStatTong;
    private JLabel lblStatHomNay;
    private JLabel lblStatTuyen;

    // --- Design tokens ---
    private static final Color PRIMARY       = NotionTheme.ACCENT;
    private static final Color PRIMARY_LIGHT = NotionTheme.ACCENT_SOFT;
    private static final Color SURFACE       = NotionTheme.PAGE;
    private static final Color SURFACE_DIM   = NotionTheme.PAGE;
    private static final Color CARD_BG       = NotionTheme.CARD;
    private static final Color ON_SURFACE    = NotionTheme.TEXT;
    private static final Color ON_SURF_VAR   = NotionTheme.TEXT_MUTED;
    private static final Color OUTLINE       = NotionTheme.BORDER;
    private static final Color ROW_ALT       = NotionTheme.PAGE;
    private static final Color ROW_HOVER     = NotionTheme.ACCENT_SOFT;
    private static final Color ERROR_FG      = AppColors.ERROR_DARK;
    private static final Color ERROR_BG      = AppColors.ERROR_LIGHT;
    private static final Color SUCCESS_BG    = AppColors.SUCCESS_LIGHT;
    private static final Color SUCCESS_FG    = AppColors.SUCCESS_DARK;
    private static final Color WARN_BG       = AppColors.WARNING_LIGHT;
    private static final Color WARN_FG       = AppColors.WARNING_DARK;

    private static final Font FONT_TITLE    = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font FONT_DESC     = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_BODY     = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_BOLD     = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_MONO     = new Font("Consolas", Font.BOLD, 13);
    private static final Font FONT_BADGE    = new Font("Segoe UI", Font.BOLD, 11);
    private static final Font FONT_HEADER   = new Font("Segoe UI", Font.BOLD, 11);
    private static final Font FONT_SMALL    = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_BTN      = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_STAT_NUM = new Font("Segoe UI", Font.BOLD, 28);
    private static final Font FONT_STAT_LBL = new Font("Segoe UI", Font.BOLD, 10);

    private static final DateTimeFormatter FMT_DATETIME = DateTimeFormatter.ofPattern("HH:mm  dd/MM/yyyy");

    // --- AppModule stub buttons ---
    private final JButton btnSubmit = new JButton();
    private final JButton btnCancel = new JButton();
    private final JPanel  btnPanel  = new JPanel();

    // =====================================================================
    //  Constructor
    // =====================================================================

    public QuanLyLichChayModule() {
        setLayout(new BorderLayout());
        setBackground(SURFACE);
        setBorder(new EmptyBorder(28, 36, 28, 36));
        btnPanel.setVisible(false);
        buildUI();
        loadData();
    }

    // =====================================================================
    //  BUILD UI
    // =====================================================================

    private void buildUI() {
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);

        JPanel header = buildHeader();
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        JPanel stats = buildStatsRow();
        stats.setAlignmentX(Component.LEFT_ALIGNMENT);
        stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));

        JPanel filterArea = buildFilterBar();
        filterArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        filterArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        top.add(header);
        top.add(Box.createVerticalStrut(16));
        top.add(stats);
        top.add(Box.createVerticalStrut(16));
        top.add(filterArea);
        top.add(Box.createVerticalStrut(16));

        add(top, BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);
        loadGaFilters();
    }

    // ---- Header ----
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(24, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, NotionTheme.NAVY,
                        getWidth(), getHeight(), new Color(0x45, 0x34, 0xB3));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                g2.setColor(AppColors.withAlpha(NotionTheme.SKY, 145));
                g2.fillOval(getWidth() - 230, -82, 250, 250);
                g2.setColor(AppColors.withAlpha(NotionTheme.YELLOW, 118));
                g2.fillRoundRect(getWidth() - 365, 82, 190, 62, 18, 18);
                g2.setColor(AppColors.withAlpha(Color.WHITE, 42));
                g2.fillRoundRect(getWidth() - 300, 36, 128, 14, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(24, 28, 24, 28));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel lblEyebrow = new JLabel("WORKSPACE / DỮ LIỆU TÀU");
        lblEyebrow.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblEyebrow.setForeground(AppColors.withAlpha(Color.WHITE, 175));
        lblEyebrow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTitle = new JLabel("Quản lý lịch chạy");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblDesc = new JLabel("Theo dõi lịch khởi hành, tuyến đường và đoàn tàu trong một bảng điều phối.");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDesc.setForeground(AppColors.withAlpha(Color.WHITE, 205));
        lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        left.add(lblEyebrow);
        left.add(Box.createVerticalStrut(8));
        left.add(lblTitle);
        left.add(Box.createVerticalStrut(8));
        left.add(lblDesc);

        JButton btnThem = createPrimaryButton("+ Thêm lịch");
        ImageIcon icoThem = loadScaledIcon(LineIcons.Name.ADD, 15);
        if (icoThem != null) { btnThem.setIcon(icoThem); btnThem.setText("  Thêm lịch"); }
        btnThem.setPreferredSize(new Dimension(150, 42));
        btnThem.addActionListener(e -> openDialog(null));

        JPanel rightWrapper = new JPanel(new GridBagLayout());
        rightWrapper.setOpaque(false);
        rightWrapper.add(btnThem);

        header.add(left, BorderLayout.CENTER);
        header.add(rightWrapper, BorderLayout.EAST);
        return header;
    }

    // ---- Stats row ----
    private JPanel buildStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 12, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));

        lblStatTong   = new JLabel("0");
        lblStatHomNay = new JLabel("0");
        lblStatTuyen  = new JLabel("0");

        row.add(buildStatCard("Tổng lịch chạy", lblStatTong, PRIMARY, NotionTheme.ACCENT_SOFT));
        row.add(buildStatCard("Lịch hôm nay", lblStatHomNay, new Color(0x00, 0x75, 0xDE), NotionTheme.SKY));
        row.add(buildStatCard("Lượt đoàn tàu", lblStatTuyen, AppColors.ERROR_DARK, NotionTheme.ROSE));
        return row;
    }

    private JPanel buildStatCard(String label, JLabel valueLbl, Color valueColor, Color tint) {
        // Stat card copy pattern Khách hàng: nền pastel toàn thẻ, số ở trên, nhãn ở dưới.
        // Rủi ro: giữ chiều cao 92px; text label dài cần rút gọn để không cắt khi resize.
        JPanel card = new JPanel(new BorderLayout(10, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(tint);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(AppColors.withAlpha(valueColor, 80));
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
                g2.setColor(valueColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
            }
        };
        marker.setOpaque(false);
        marker.setPreferredSize(new Dimension(8, 48));

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setOpaque(false);
        valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLbl.setForeground(valueColor);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(ON_SURF_VAR);
        text.add(valueLbl);
        text.add(Box.createVerticalStrut(2));
        text.add(lbl);

        card.add(marker, BorderLayout.WEST);
        card.add(text, BorderLayout.CENTER);
        return card;
    }

    // ---- Filter bar ----
    private JPanel buildFilterBar() {
        JPanel wrapper = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(OUTLINE);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(18, 22, 18, 22));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel title = new JLabel("Bộ lọc lịch chạy");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(ON_SURFACE);
        JLabel subtitle = new JLabel("Tìm theo lịch, tuyến, đoàn tàu hoặc lọc theo ga và ngày chạy");
        subtitle.setFont(FONT_SMALL);
        subtitle.setForeground(ON_SURF_VAR);
        header.add(title, BorderLayout.WEST);
        header.add(subtitle, BorderLayout.EAST);
        wrapper.add(header);
        wrapper.add(Box.createVerticalStrut(12));

        // Filter card copy pattern Khách hàng: searchBox có icon trong viền, optionRow nằm dưới.
        // Rủi ro: DatePicker/SearchableComboBox rộng hơn combo thường nên phải giữ grid responsive.
        JPanel searchRow = new JPanel(new BorderLayout(10, 0));
        searchRow.setOpaque(false);
        searchRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JPanel searchBox = new JPanel(new BorderLayout(10, 0));
        searchBox.setOpaque(false);
        searchBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1, true),
                new EmptyBorder(9, 14, 9, 14)
        ));

        JLabel iconSearch = new JLabel();
        ImageIcon icoSearch = loadScaledIcon(LineIcons.Name.SEARCH, 18);
        if (icoSearch != null) iconSearch.setIcon(icoSearch);
        searchBox.add(iconSearch, BorderLayout.WEST);

        txtSearch = new JTextField();
        txtSearch.setFont(FONT_BODY);
        txtSearch.setOpaque(false);
        txtSearch.setBorder(BorderFactory.createEmptyBorder());
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm theo mã lịch, tên tuyến, mã đoàn tàu...");
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
        });
        searchBox.add(txtSearch, BorderLayout.CENTER);
        SearchFieldClearButton.install(searchBox, txtSearch, this::applyFilter);

        JButton btnResetAll = new JButton("Bỏ lọc");
        NotionTheme.styleSecondaryButton(btnResetAll);
        btnResetAll.setPreferredSize(new Dimension(104, 40));
        btnResetAll.addActionListener(e -> {
            if (filterGaDi  != null) filterGaDi.clearSelection();
            if (filterGaDen != null) filterGaDen.clearSelection();
            if (dateFrom != null) dateFrom.setValue(null);
            if (dateTo   != null) dateTo.setValue(null);
            applyFilter();
        });

        searchRow.add(searchBox, BorderLayout.CENTER);
        wrapper.add(searchRow);
        wrapper.add(Box.createVerticalStrut(12));

        JPanel filterRow = new JPanel(new GridBagLayout());
        filterRow.setOpaque(false);
        filterRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        filterRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        GridBagConstraints filterGbc = new GridBagConstraints();
        filterGbc.gridy = 0;
        filterGbc.fill = GridBagConstraints.HORIZONTAL;
        filterGbc.anchor = GridBagConstraints.NORTHWEST;
        filterGbc.weightx = 1.0;
        filterGbc.insets = new Insets(0, 0, 0, 12);

        // Ga di
        filterGaDi = new SearchableComboBox<>(
                ga -> ga.getTenGa() + " (" + ga.getMaGa() + ")",
                (ga, q) -> ga.getTenGa().toLowerCase().contains(q) || ga.getMaGa().toLowerCase().contains(q));
        filterGaDi.setPlaceholder("Tất cả ga đi");
        filterGaDi.setPreferredSize(new Dimension(220, 38));
        filterGaDi.setOnChanged(this::applyFilter);
        filterGbc.gridx = 0;
        filterRow.add(buildFilterGroupLich("Ga đi", filterGaDi), filterGbc);

        // Ga den
        filterGaDen = new SearchableComboBox<>(
                ga -> ga.getTenGa() + " (" + ga.getMaGa() + ")",
                (ga, q) -> ga.getTenGa().toLowerCase().contains(q) || ga.getMaGa().toLowerCase().contains(q));
        filterGaDen.setPlaceholder("Tất cả ga đến");
        filterGaDen.setPreferredSize(new Dimension(220, 38));
        filterGaDen.setOnChanged(this::applyFilter);
        filterGbc.gridx = 1;
        filterRow.add(buildFilterGroupLich("Ga đến", filterGaDen), filterGbc);

        // Tu ngay
        dateFrom = new DatePickerField();
        dateFrom.setPreferredSize(new Dimension(150, 38));
        dateFrom.addPropertyChangeListener("value", e -> applyFilter());
        filterGbc.gridx = 2;
        filterGbc.weightx = 0.7;
        filterRow.add(buildFilterGroupLich("Từ ngày", dateFrom), filterGbc);

        // Den ngay
        dateTo = new DatePickerField();
        dateTo.setPreferredSize(new Dimension(150, 38));
        dateTo.addPropertyChangeListener("value", e -> applyFilter());
        filterGbc.gridx = 3;
        filterRow.add(buildFilterGroupLich("Đến ngày", dateTo), filterGbc);
        filterGbc.gridx = 4;
        filterGbc.weightx = 0.0;
        filterGbc.insets = new Insets(0, 0, 0, 0);
        filterRow.add(FilterActionGroup.wrap(btnResetAll), filterGbc);
        // Handoff: Bỏ lọc chỉ reset ga/ngày, còn tìm kiếm có nút X riêng trong search box.
        // Cảnh báo: giữ nút ở filterRow để semantics không ngang hàng với thanh tìm kiếm.

        wrapper.add(filterRow);

        return wrapper;
    }

    private JPanel buildFilterGroupLich(String labelText, JComponent field) {
        JPanel group = new JPanel(new BorderLayout(0, 6));
        group.setOpaque(false);
        JLabel lbl = new JLabel(labelText + ":");
        lbl.setFont(FONT_HEADER);
        lbl.setForeground(ON_SURF_VAR);
        group.add(lbl, BorderLayout.NORTH);
        group.add(field, BorderLayout.CENTER);
        return group;
        // Handoff: label đặt trên field để đồng bộ filter grid với các module quản lý.
        // Cảnh báo: DatePicker có icon riêng nên không ép border/renderer ở helper này.
    }

    private void loadGaFilters() {
        List<Ga> gaList = daoGa.getAll();
        if (filterGaDi  != null) filterGaDi.setItems(gaList);
        if (filterGaDen != null) filterGaDen.setItems(gaList);
    }

    // ---- Table card ----
    private JPanel buildTableCard() {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(OUTLINE);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, 16, 16));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(0, 0, 0, 0));
        card.add(buildTableHeader(), BorderLayout.NORTH);
        card.add(buildTableSection(), BorderLayout.CENTER);
        card.add(buildPaginationBar(), BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildTableHeader() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(18, 20, 14, 20));

        JLabel lbl = new JLabel("Danh sách lịch chạy");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl.setForeground(ON_SURFACE);
        JLabel hint = new JLabel("Nhấp đúp vào dòng hoặc dùng thao tác để cập nhật lịch chạy");
        hint.setFont(FONT_SMALL);
        hint.setForeground(ON_SURF_VAR);

        bar.add(lbl, BorderLayout.WEST);
        bar.add(hint, BorderLayout.EAST);
        return bar;
    }

    private JScrollPane buildTableSection() {
        tableModel = new LichTableModel();
        table = new JTable(tableModel);
        NotionTheme.styleTable(table);
        table.setRowHeight(52);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(OUTLINE);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(NotionTheme.TABLE_SELECTION);
        table.setSelectionForeground(ON_SURFACE);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.addMouseMotionListener(new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row != hoveredRow) { hoveredRow = row; table.repaint(); }
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseExited(MouseEvent e) { hoveredRow = -1; table.repaint(); }

            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() != 2 || !SwingUtilities.isLeftMouseButton(e)) return;
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row < 0 || col == 6) return;
                Lich lich = tableModel.getAt(table.convertRowIndexToModel(row));
                if (lich != null) openDialog(lich);
                // Handoff: double-click dòng mở cùng dialog với nút Chỉnh sửa hiện có.
                // Cảnh báo: bỏ qua cột thao tác để không xung đột click button editor.
            }
        });

        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tbl, Object value,
                    boolean isSel, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(tbl, value, isSel, hasFocus, row, col);
                lbl.setFont(FONT_HEADER);
                lbl.setForeground(ON_SURF_VAR);
                lbl.setBackground(NotionTheme.CARD_MUTED);
                lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, OUTLINE),
                        new EmptyBorder(0, 16, 0, 8)));
                lbl.setPreferredSize(new Dimension(lbl.getPreferredSize().width, 44));
                return lbl;
            }
        });

        // Columns: Mã lịch | Đoàn tàu | Tuyến | Thời gian bắt đầu | Thời gian chạy | Trạng thái | Thao tác
        TableColumnModel cm = table.getColumnModel();
        int[] widths = {105, 120, 190, 145, 125, 110, 170};
        for (int i = 0; i < widths.length; i++) cm.getColumn(i).setPreferredWidth(widths[i]);

        cm.getColumn(0).setCellRenderer(new RowRenderer(FONT_MONO, PRIMARY));
        cm.getColumn(1).setCellRenderer(new RowRenderer(FONT_BOLD, ON_SURFACE));
        cm.getColumn(2).setCellRenderer(new RowRenderer(FONT_BODY, ON_SURFACE));
        cm.getColumn(3).setCellRenderer(new RowRenderer(FONT_SMALL, ON_SURF_VAR));
        cm.getColumn(4).setCellRenderer(new DurationRenderer());
        cm.getColumn(5).setCellRenderer(new StatusBadgeRenderer());
        cm.getColumn(6).setCellRenderer(new ActionRenderer());
        cm.getColumn(6).setCellEditor(new ActionEditor());

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(CARD_BG);
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setWheelScrollingEnabled(false);

        sp.getViewport().addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                int nr = calcRowsFromViewport();
                if (nr > 0 && nr != rowsPerPage) {
                    rowsPerPage = nr;
                    if (!isRefreshing) refreshTable();
                }
            }
        });
        return sp;
    }

    private int calcRowsFromViewport() {
        if (table == null || !(table.getParent() instanceof JViewport vp)) return 0;
        int rh = table.getRowHeight() > 0 ? table.getRowHeight() : 52;
        int hh = table.getTableHeader().getHeight();
        if (hh <= 0) hh = 44;
        int avail = vp.getHeight() - hh;
        return avail > 0 ? Math.max(1, avail / rh + 1) : 0;
    }

    private JPanel buildPaginationBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, OUTLINE),
                new EmptyBorder(12, 20, 12, 20)));

        lblPageInfo = new JLabel();
        lblPageInfo.setFont(FONT_SMALL);
        lblPageInfo.setForeground(ON_SURF_VAR);

        paginationPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        paginationPanel.setOpaque(false);

        bar.add(lblPageInfo, BorderLayout.WEST);
        bar.add(paginationPanel, BorderLayout.EAST);
        return bar;
    }

    // =====================================================================
    //  DATA
    // =====================================================================

    private void loadData() {
        new SwingWorker<List<Lich>, Void>() {
            @Override protected List<Lich> doInBackground() { return daoLich.getAll(); }
            @Override protected void done() {
                try { allData = get(); } catch (Exception e) { allData = new ArrayList<>(); }
                updateStats();
                applyFilter();
            }
        }.execute();
    }

    private void updateStats() {
        lblStatTong.setText(String.valueOf(allData.size()));

        long homNay = allData.stream().filter(l -> {
            if (l.getThoiGianBatDau() == null) return false;
            return l.getThoiGianBatDau().toLocalDate().equals(LocalDate.now());
        }).count();
        lblStatHomNay.setText(String.valueOf(homNay));

        long distinctDoan = allData.stream()
                .map(l -> l.getDoanTau() != null ? l.getDoanTau().getMaDoanTau() : "")
                .distinct().count();
        lblStatTuyen.setText(String.valueOf(distinctDoan));
    }

    // =====================================================================
    //  FILTER / SEARCH / PAGINATION
    // =====================================================================

    private void applyFilter() {
        String kw = txtSearch != null ? txtSearch.getText().trim().toLowerCase() : "";
        Ga selGaDi  = filterGaDi  != null ? filterGaDi.getSelectedItem()  : null;
        Ga selGaDen = filterGaDen != null ? filterGaDen.getSelectedItem() : null;
        java.time.LocalDate ldFrom = dateFrom != null ? dateFrom.getValue() : null;
        java.time.LocalDate ldTo   = dateTo   != null ? dateTo.getValue()   : null;

        filteredData = new ArrayList<>();
        for (Lich l : allData) {
            if (!kw.isEmpty()) {
                String maLich   = l.getMaLich() != null ? l.getMaLich().toLowerCase() : "";
                String maDoan   = l.getDoanTau() != null ? l.getDoanTau().getMaDoanTau().toLowerCase() : "";
                String tenDoan  = l.getDoanTau() != null && l.getDoanTau().getTenDoanTau() != null
                                ? l.getDoanTau().getTenDoanTau().toLowerCase() : "";
                String maTuyen  = l.getTuyen() != null ? l.getTuyen().getMaTuyen().toLowerCase() : "";
                String tenTuyen = tuyenDisplayName(l.getTuyen()).toLowerCase();
                if (!maLich.contains(kw) && !maDoan.contains(kw) && !tenDoan.contains(kw)
                        && !maTuyen.contains(kw) && !tenTuyen.contains(kw)) continue;
            }
            if (selGaDi != null) {
                if (l.getTuyen() == null || l.getTuyen().getGaDi() == null
                        || !l.getTuyen().getGaDi().getMaGa().equals(selGaDi.getMaGa())) continue;
            }
            if (selGaDen != null) {
                if (l.getTuyen() == null || l.getTuyen().getGaDen() == null
                        || !l.getTuyen().getGaDen().getMaGa().equals(selGaDen.getMaGa())) continue;
            }
            if (ldFrom != null && l.getThoiGianBatDau() != null) {
                if (l.getThoiGianBatDau().toLocalDate().isBefore(ldFrom)) continue;
            }
            if (ldTo != null && l.getThoiGianBatDau() != null) {
                if (l.getThoiGianBatDau().toLocalDate().isAfter(ldTo)) continue;
            }
            filteredData.add(l);
        }
        totalRecords = filteredData.size();
        currentPage  = 1;
        refreshTable();
    }

    private void doSearch() {
        applyFilter();
    }

    private void refreshTable() {
        isRefreshing = true;
        try {
            int vpRows = calcRowsFromViewport();
            if (vpRows > 0) {
                rowsPerPage = vpRows;
            } else {
                int screenH = Toolkit.getDefaultToolkit().getScreenSize().height;
                int rh = table != null && table.getRowHeight() > 0 ? table.getRowHeight() : 52;
                rowsPerPage = Math.max(3, (screenH - 530) / rh);
            }

            int totalPages = Math.max(1, (int) Math.ceil((double) totalRecords / rowsPerPage));
            if (currentPage > totalPages) currentPage = totalPages;

            int start = (currentPage - 1) * rowsPerPage;
            int end   = Math.min(start + rowsPerPage, totalRecords);
            tableModel.setData(filteredData.subList(start, end));

            lblPageInfo.setText(totalRecords == 0
                    ? "Không tìm thấy lịch nào"
                    : "Hiển thị " + (start + 1) + " – " + end + " / " + totalRecords + " lịch");

            rebuildPagination(totalPages);
        } finally { isRefreshing = false; }
    }

    private void rebuildPagination(int totalPages) {
        paginationPanel.removeAll();
        addNavBtn("‹", currentPage > 1, () -> { currentPage--; refreshTable(); });
        for (int i = 1; i <= totalPages; i++) {
            if (totalPages > 7) {
                if (i == 1 || i == totalPages || (i >= currentPage - 1 && i <= currentPage + 1)) {
                    addPageBtn(i);
                } else if (i == currentPage - 2 || i == currentPage + 2) {
                    JLabel dots = new JLabel("…");
                    dots.setFont(FONT_SMALL); dots.setForeground(ON_SURF_VAR);
                    dots.setBorder(new EmptyBorder(0, 4, 0, 4));
                    paginationPanel.add(dots);
                }
            } else { addPageBtn(i); }
        }
        addNavBtn("›", currentPage < totalPages, () -> { currentPage++; refreshTable(); });
        paginationPanel.revalidate(); paginationPanel.repaint();
    }

    private void addPageBtn(int page) {
        JButton btn = new JButton(String.valueOf(page)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (page == currentPage) { g2.setColor(PRIMARY); g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),8,8)); }
                g2.dispose(); super.paintComponent(g);
            }
        };
        btn.setFont(FONT_HEADER); btn.setPreferredSize(new Dimension(32,32));
        btn.setMargin(new Insets(0,0,0,0)); btn.setFocusPainted(false);
        btn.setBorderPainted(false); btn.setContentAreaFilled(false);
        btn.setForeground(page == currentPage ? NotionTheme.CARD : ON_SURF_VAR);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> { currentPage = page; refreshTable(); });
        paginationPanel.add(btn);
    }

    private void addNavBtn(String sym, boolean enabled, Runnable action) {
        JButton btn = new JButton(sym);
        btn.setFont(FONT_BODY); btn.setPreferredSize(new Dimension(32,32));
        btn.setMargin(new Insets(0,0,0,0)); btn.setFocusPainted(false);
        btn.setBorderPainted(false); btn.setContentAreaFilled(false);
        btn.setForeground(enabled ? ON_SURFACE : ON_SURF_VAR);
        btn.setEnabled(enabled); btn.setCursor(enabled
                ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
        if (enabled) btn.addActionListener(e -> action.run());
        paginationPanel.add(btn);
    }

    // =====================================================================
    //  ACTIONS
    // =====================================================================

    private void openDialog(Lich lich) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        ChinhSuaLichChayDialog dlg = new ChinhSuaLichChayDialog(owner, lich, () -> loadData());
        dlg.setVisible(true);
    }

    private void toggleHoatDong(Lich lich) {
        boolean current = lich.isHoatDong();
        String msg = current
                ? "Ngưng hoạt động lịch " + lich.getMaLich() + "?"
                : "Kích hoạt lại lịch " + lich.getMaLich() + "?";
        String title = current ? "Xác nhận ngưng hoạt động" : "Xác nhận kích hoạt";
        int confirm = NotionMessageDialog.showConfirmDialog(this, msg, title,
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        boolean ok = daoLich.setHoatDong(lich.getMaLich(), !current);
        if (ok) loadData();
        else NotionMessageDialog.showMessageDialog(this, "Không thể cập nhật trạng thái lịch.", "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    // =====================================================================
    //  HELPERS
    // =====================================================================

    private String tuyenDisplayName(Tuyen t) {
        if (t == null) return "";
        String gaDi  = t.getGaDi()  != null ? t.getGaDi().getTenGa()  : t.getMaTuyen();
        String gaDen = t.getGaDen() != null ? t.getGaDen().getTenGa() : "";
        return gaDi + (gaDen.isEmpty() ? "" : " → " + gaDen);
    }

    private String formatDuration(String minutes) {
        try {
            int m = Integer.parseInt(minutes);
            int h = m / 60; int rem = m % 60;
            return h > 0 ? h + " giờ " + rem + " phút" : rem + " phút";
        } catch (Exception e) { return minutes; }
    }

    private Color getRowBg(boolean isSel, int row) {
        if (isSel) return NotionTheme.TABLE_SELECTION;
        if (row == hoveredRow) return ROW_HOVER;
        return row % 2 == 0 ? CARD_BG : ROW_ALT;
    }

    private ImageIcon loadScaledIcon(LineIcons.Name iconName, int size) {
        return LineIcons.image(iconName, size);
    }

    private JTextField createSearchField(String placeholder) {
        JTextField f = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !hasFocus()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(NotionTheme.BORDER);
                    g2.setFont(FONT_BODY);
                    Insets ins = getInsets();
                    g2.drawString(placeholder, ins.left, getHeight() / 2 + 5);
                    g2.dispose();
                }
            }
        };
        f.setFont(FONT_BODY);
        f.setBackground(CARD_BG);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1, true), new EmptyBorder(6, 12, 6, 12)));
        f.setPreferredSize(new Dimension(0, 38));
        return f;
    }

    private JButton createSearchButton() {
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? PRIMARY.darker()
                        : getModel().isRollover() ? PRIMARY.brighter() : PRIMARY);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose(); super.paintComponent(g);
            }
        };
        ImageIcon icoSearch = loadScaledIcon(LineIcons.Name.SEARCH, 16);
        if (icoSearch != null) { btn.setIcon(icoSearch); btn.setText("  Tìm"); }
        else btn.setText("Tìm");
        btn.setFont(FONT_BTN); btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false); btn.setBorderPainted(false);
        btn.setFocusPainted(false); btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(90, 38));
        return btn;
    }

    private JButton createSecondaryButton(String text, int width, int height) {
        // Nút phụ dùng nền trắng và border mảnh để hợp card filter, tránh lẫn với primary action.
        // Rủi ro: helper này đang tối ưu cho button ngắn; text dài cần tăng width truyền vào.
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? NotionTheme.CARD_MUTED : NotionTheme.CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(OUTLINE);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BOLD);
        btn.setForeground(ON_SURF_VAR);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(width, height));
        return btn;
    }

    private JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? PRIMARY.darker() : PRIMARY);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose(); super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BTN); btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false); btn.setBorderPainted(false);
        btn.setFocusPainted(false); btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(140, 36));
        return btn;
    }

    // =====================================================================
    //  TABLE MODEL
    // =====================================================================

    private class LichTableModel extends AbstractTableModel {
        private final String[] COLS = {
            "MÃ LỊCH", "ĐOÀN TÀU", "TUYẾN",
            "THỜI GIAN BẮT ĐẦU", "THỜI GIAN CHẠY", "TRẠNG THÁI", "THAO TÁC"
        };
        private List<Lich> data = new ArrayList<>();

        void setData(List<Lich> d) { this.data = new ArrayList<>(d); fireTableDataChanged(); }
        Lich getAt(int r) { return (r >= 0 && r < data.size()) ? data.get(r) : null; }

        @Override public int getRowCount()    { return data.size(); }
        @Override public int getColumnCount() { return COLS.length; }
        @Override public String getColumnName(int c) { return COLS[c]; }
        @Override public boolean isCellEditable(int r, int c) { return c == 6; }

        @Override public Object getValueAt(int r, int c) {
            Lich l = data.get(r);
            return switch (c) {
                case 0 -> l.getMaLich();
                case 1 -> l.getDoanTau() != null ? l.getDoanTau().getMaDoanTau() : "";
                case 2 -> tuyenDisplayName(l.getTuyen());
                case 3 -> l.getThoiGianBatDau() != null
                        ? l.getThoiGianBatDau().format(FMT_DATETIME) : "";
                case 4 -> l.getThoiGianChay();
                case 5, 6 -> l;
                default -> "";
            };
        }
    }

    // =====================================================================
    //  CELL RENDERERS
    // =====================================================================

    private class RowRenderer extends DefaultTableCellRenderer {
        private final Font font; private final Color fg;
        RowRenderer(Font font, Color fg) { this.font = font; this.fg = fg; }
        @Override public Component getTableCellRendererComponent(JTable t, Object v,
                boolean sel, boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, focus, row, col);
            Lich lich = tableModel.getAt(row);
            boolean active = lich == null || lich.isHoatDong();
            setFont(font);
            setForeground(active ? fg : ON_SURF_VAR);
            setBorder(new EmptyBorder(0, 16, 0, 8));
            setBackground(getRowBg(sel, row));
            return this;
        }
    }

    private class DurationRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v,
                boolean sel, boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, focus, row, col);
            setFont(FONT_BADGE); setForeground(WARN_FG);
            setBorder(new EmptyBorder(0, 16, 0, 8));
            setBackground(getRowBg(sel, row));
            setText(v != null ? formatDuration(v.toString()) : "");
            return this;
        }
    }

    private class StatusBadgeRenderer extends JPanel implements TableCellRenderer {
        private final JLabel badge = new JLabel();
        private Color badgeBg = SUCCESS_BG;
        private Color badgeFg = SUCCESS_FG;
        private static final int BADGE_HEIGHT = 24;
        private static final int BADGE_MARGIN_X = 28;

        StatusBadgeRenderer() {
            setLayout(new BorderLayout());
            setOpaque(true);
            setBorder(new EmptyBorder(0, BADGE_MARGIN_X, 0, BADGE_MARGIN_X));
            badge.setFont(FONT_BADGE);
            badge.setHorizontalAlignment(SwingConstants.CENTER);
            badge.setPreferredSize(new Dimension(10, BADGE_HEIGHT));
            add(badge, BorderLayout.CENTER);
            // Handoff: status badge lịch chạy dùng gần full ô để đồng bộ với tag table khác.
            // Nếu cột trạng thái hẹp, giảm BADGE_MARGIN_X trước khi thay đổi row/table layout.
        }

        @Override public Component getTableCellRendererComponent(JTable t, Object v,
                boolean sel, boolean focus, int row, int col) {
            setBackground(getRowBg(sel, row));
            boolean active = !(v instanceof Lich lich) || lich.isHoatDong();
            badge.setText(active ? "Hoạt động" : "Tạm ngưng");
            badgeBg = active ? SUCCESS_BG : WARN_BG;
            badgeFg = active ? SUCCESS_FG : WARN_FG;
            badge.setForeground(badgeFg);
            return this;
        }

        @Override protected void paintChildren(Graphics g) {
            // Badge trạng thái dùng chữ nhật bo góc nhẹ để đồng bộ với property tag kiểu Notion.
            // Rủi ro: nếu đổi text dài hơn cần kiểm tra lại width cột để không cắt nhãn.
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Rectangle r = badge.getBounds();
            int x = r.x;
            int y = r.y + Math.max(0, (r.height - BADGE_HEIGHT) / 2);
            int w = r.width;
            int h = BADGE_HEIGHT;
            g2.setColor(badgeBg);
            g2.fillRoundRect(x, y, w, h, 8, 8);
            g2.setColor(AppColors.withAlpha(badgeFg, 65));
            g2.drawRoundRect(x, y, w - 1, h - 1, 8, 8);
            g2.dispose();
            super.paintChildren(g);
        }
    }

    private class ActionRenderer extends JPanel implements TableCellRenderer {
        private final JLabel lblSua    = new JLabel();
        private final JLabel lblToggle = new JLabel();
        private Color toggleBg = ERROR_BG;

        ActionRenderer() {
            setLayout(new GridBagLayout());
            setOpaque(true);
            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(0, 5, 0, 5);

            ImageIcon iSua = loadScaledIcon(LineIcons.Name.EDIT, 14);

            lblSua.setFont(FONT_BADGE); lblSua.setForeground(PRIMARY);
            lblSua.setHorizontalAlignment(SwingConstants.CENTER);
            lblSua.setPreferredSize(new Dimension(86, 28));
            if (iSua != null) lblSua.setIcon(iSua);
            lblSua.setText(iSua != null ? "  Chỉnh sửa" : "Chỉnh sửa");

            lblToggle.setFont(FONT_BADGE);
            lblToggle.setHorizontalAlignment(SwingConstants.CENTER);
            lblToggle.setPreferredSize(new Dimension(88, 28));

            add(lblSua, g); add(lblToggle, g);
        }

        @Override public Component getTableCellRendererComponent(JTable t, Object v,
                boolean sel, boolean focus, int row, int col) {
            setBackground(getRowBg(sel, row));
            if (v instanceof Lich lich) {
                boolean active = lich.isHoatDong();
                lblToggle.setForeground(active ? ERROR_FG : WARN_FG);
                lblToggle.setText(active ? "Ngưng HĐ" : "Kích hoạt");
                toggleBg = active ? ERROR_BG : WARN_BG;
            }
            return this;
        }

        @Override protected void paintChildren(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Rectangle rs = lblSua.getBounds();
            g2.setColor(PRIMARY_LIGHT);
            g2.fillRoundRect(rs.x, rs.y, rs.width, rs.height, 8, 8);
            Rectangle rx = lblToggle.getBounds();
            g2.setColor(toggleBg);
            g2.fillRoundRect(rx.x, rx.y, rx.width, rx.height, 8, 8);
            g2.dispose(); super.paintChildren(g);
        }
    }

    private class ActionEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel   panel      = new JPanel(new GridBagLayout());
        private final JButton  btnSua     = new JButton();
        private final JButton  btnToggle  = new JButton();
        private int editingRow;

        ActionEditor() {
            panel.setOpaque(true);
            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(0, 5, 0, 5);

            ImageIcon iSua = loadScaledIcon(LineIcons.Name.EDIT, 14);

            styleActionBtn(btnSua, "Chỉnh sửa", PRIMARY, PRIMARY_LIGHT, iSua, 86);
            styleActionBtn(btnToggle, "Ngưng HĐ", ERROR_FG, ERROR_BG, null, 88);

            panel.add(btnSua, g); panel.add(btnToggle, g);

            btnSua.addActionListener(e -> {
                fireEditingStopped();
                Lich l = tableModel.getAt(editingRow);
                if (l != null) openDialog(l);
            });
            btnToggle.addActionListener(e -> {
                fireEditingStopped();
                Lich l = tableModel.getAt(editingRow);
                if (l != null) toggleHoatDong(l);
            });
        }

        private void styleActionBtn(JButton btn, String text, Color fg, Color bg, ImageIcon ico, int width) {
            // Action editor mô phỏng tag/button Notion bằng nền mềm + border theo màu chữ.
            // Rủi ro: JButton vẫn do LAF paint một phần, nên luôn giữ contentAreaFilled true với background rõ.
            btn.setFont(FONT_BADGE); btn.setForeground(fg); btn.setBackground(bg);
            if (ico != null) { btn.setIcon(ico); btn.setText("  " + text); }
            else btn.setText(text);
            btn.setOpaque(true); btn.setContentAreaFilled(true);
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(AppColors.withAlpha(fg, 55), 1, true),
                    new EmptyBorder(0, 8, 0, 8)));
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setPreferredSize(new Dimension(width, 28));
        }

        @Override public Component getTableCellEditorComponent(JTable t, Object v,
                boolean sel, int row, int col) {
            editingRow = row;
            panel.setBackground(getRowBg(true, row));
            if (v instanceof Lich lich) {
                boolean active = lich.isHoatDong();
                btnToggle.setText(active ? "Ngưng HĐ" : "Kích hoạt");
                btnToggle.setForeground(active ? ERROR_FG : WARN_FG);
                btnToggle.setBackground(active ? ERROR_BG : WARN_BG);
            }
            return panel;
        }
        @Override public Object getCellEditorValue() { return null; }
    }

    // =====================================================================
    public void applySearch(String text) {
        txtSearch.setText(text);
        applyFilter();
    }

    /**
     * Nhận structured criteria từ Dashboard (TongQuatModule).
     * Điền filter fields rồi gọi applyFilter().
     */
    public void applySearchFromDashboard(LichSearchCriteria criteria) {
        if (criteria == null) return;

        try {
            // Reset filter trước khi áp dụng criteria từ Dashboard
            if (filterGaDi != null) filterGaDi.selectItem(null);
            if (filterGaDen != null) filterGaDen.selectItem(null);
            if (dateFrom != null) dateFrom.setValue(null);
            if (dateTo != null) dateTo.setValue(null);

            // Select GA ĐI — tìm Ga theo maGa rồi select
            if (criteria.gaDiCode() != null && filterGaDi != null) {
                for (Ga g : daoGa.getAll()) {
                    if (g.getMaGa().equals(criteria.gaDiCode())) {
                        filterGaDi.selectItem(g);
                        break;
                    }
                }
            }

            // Select GA ĐẾN
            if (criteria.gaDenCode() != null && filterGaDen != null) {
                for (Ga g : daoGa.getAll()) {
                    if (g.getMaGa().equals(criteria.gaDenCode())) {
                        filterGaDen.selectItem(g);
                        break;
                    }
                }
            }

            // Set khoảng ngày
            if (criteria.tuNgayYmd() != null && dateFrom != null) {
                java.time.LocalDate ld = java.time.LocalDate.parse(criteria.tuNgayYmd());
                dateFrom.setValue(ld);
            }
            if (criteria.denNgayYmd() != null && dateTo != null) {
                java.time.LocalDate ld = java.time.LocalDate.parse(criteria.denNgayYmd());
                dateTo.setValue(ld);
            }
        } catch (Exception ex) {
            System.err.println("[TongQuat] applySearchFromDashboard error: " + ex.getMessage());
        }

        applyFilter();
    }

    //  AppModule
    // =====================================================================

    @Override public String getTitle()  { return "Quản lý lịch chạy"; }
    @Override public JPanel getView()   { return this; }
    @Override public void setOnResult(Consumer<Object> cb) { this.callback = cb; }
    @Override public void reset() {
        txtSearch.setText("");
        if (dateFrom != null) dateFrom.setValue(null);
        if (dateTo != null) dateTo.setValue(null);
        loadData();
    }
}



