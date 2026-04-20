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
    private static final Color PRIMARY       = new Color(0x00, 0x5D, 0x90);
    private static final Color PRIMARY_LIGHT = new Color(0xE3, 0xF2, 0xFD);
    private static final Color SURFACE       = new Color(0xF8, 0xFA, 0xFC);
    private static final Color SURFACE_DIM   = new Color(0xF2, 0xF4, 0xF6);
    private static final Color CARD_BG       = Color.WHITE;
    private static final Color ON_SURFACE    = new Color(0x1A, 0x1D, 0x21);
    private static final Color ON_SURF_VAR   = new Color(0x5F, 0x67, 0x70);
    private static final Color OUTLINE       = new Color(0xDE, 0xE3, 0xE8);
    private static final Color ROW_ALT       = new Color(0xF8, 0xFA, 0xFC);
    private static final Color ROW_HOVER     = new Color(0xEE, 0xF5, 0xFB);
    private static final Color ERROR_FG      = new Color(0xB9, 0x1C, 0x1C);
    private static final Color ERROR_BG      = new Color(0xFF, 0xDA, 0xD6);
    private static final Color SUCCESS_BG    = new Color(0xDC, 0xFA, 0xE6);
    private static final Color SUCCESS_FG    = new Color(0x16, 0x6B, 0x3A);
    private static final Color WARN_BG       = new Color(0xFF, 0xF3, 0xCD);
    private static final Color WARN_FG       = new Color(0x92, 0x60, 0x10);

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
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JPanel stats = buildStatsRow();
        stats.setAlignmentX(Component.LEFT_ALIGNMENT);
        stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        JPanel filterArea = buildFilterBar();
        filterArea.setAlignmentX(Component.LEFT_ALIGNMENT);

        top.add(header);
        top.add(Box.createVerticalStrut(20));
        top.add(stats);
        top.add(Box.createVerticalStrut(20));
        top.add(filterArea);
        top.add(Box.createVerticalStrut(20));

        add(top, BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);
        loadGaFilters();
    }

    // ---- Header ----
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        // Title with icon
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleRow.setOpaque(false);
        ImageIcon icoTitle = loadScaledIcon("bieuTuongLich.png", 28);
        if (icoTitle != null) {
            JLabel lblIco = new JLabel(icoTitle);
            titleRow.add(lblIco);
        }
        JLabel lblTitle = new JLabel("Quản lý lịch chạy");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(ON_SURFACE);
        titleRow.add(lblTitle);
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblDesc = new JLabel("Xem và quản lý các lịch chạy, đoàn tàu và tuyến đường.");
        lblDesc.setFont(FONT_DESC);
        lblDesc.setForeground(ON_SURF_VAR);
        lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblDesc.setBorder(new EmptyBorder(4, 0, 0, 0));

        left.add(titleRow);
        left.add(lblDesc);
        header.add(left, BorderLayout.CENTER);
        return header;
    }

    // ---- Stats row ----
    private JPanel buildStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 16, 0));
        row.setOpaque(false);

        lblStatTong   = new JLabel("0");
        lblStatHomNay = new JLabel("0");
        lblStatTuyen  = new JLabel("0");

        row.add(buildStatCard("TỔNG LỊCH CHẠY", lblStatTong, PRIMARY, "bieuTuongLich.png"));
        row.add(buildStatCard("LỊCH HÔM NAY", lblStatHomNay, SUCCESS_FG, "bieuTuongThoiGian.png"));
        row.add(buildStatCard("LƯỢT ĐOÀN TÀU", lblStatTuyen, WARN_FG, "bieuTuongTau.png"));
        return row;
    }

    private JPanel buildStatCard(String label, JLabel valueLbl, Color valueColor, String iconFile) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(SURFACE_DIM);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout(12, 0));
        card.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Left: icon
        ImageIcon ico = loadScaledIcon(iconFile, 36);
        if (ico != null) {
            JLabel lblIco = new JLabel(ico);
            lblIco.setVerticalAlignment(SwingConstants.CENTER);
            card.add(lblIco, BorderLayout.WEST);
        }

        // Right: label + value
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel lblLbl = new JLabel(label);
        lblLbl.setFont(FONT_STAT_LBL);
        lblLbl.setForeground(ON_SURF_VAR);
        lblLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        valueLbl.setFont(FONT_STAT_NUM);
        valueLbl.setForeground(valueColor);
        valueLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        textPanel.add(lblLbl);
        textPanel.add(Box.createVerticalStrut(6));
        textPanel.add(valueLbl);
        card.add(textPanel, BorderLayout.CENTER);
        return card;
    }

    // ---- Filter bar ----
    private JPanel buildFilterBar() {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setOpaque(false);

        // --- Row 1: Primary Search (Full Width) ---
        JPanel searchRow = new JPanel(new BorderLayout());
        searchRow.setOpaque(false);

        JPanel bgPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Shadow
                g2.setColor(new Color(0, 0, 0, 10));
                g2.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 5, 20, 20);
                
                // Background
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 4, 20, 20);
                
                // Border
                g2.setColor(OUTLINE);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 4, 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        bgPanel.setOpaque(false);
        bgPanel.setBorder(new EmptyBorder(6, 15, 10, 15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 12);
        gbc.fill = GridBagConstraints.VERTICAL;

        // Custom search icon
        JLabel iconSearch = new JLabel();
        try {
            java.net.URL url = getClass().getResource("/icons/nutTimKiem.png");
            if (url != null) {
                Image img = new ImageIcon(url).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
                iconSearch.setIcon(new ImageIcon(img));
            }
        } catch (Exception ignored) {}
        bgPanel.add(iconSearch, gbc);

        txtSearch = new JTextField();
        txtSearch.setFont(FONT_BODY);
        txtSearch.setOpaque(true);
        txtSearch.setBackground(Color.WHITE);
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm theo mã lịch, tên tuyến, mã đoàn tàu...");
        
        // Real-time search
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
        });

        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        bgPanel.add(txtSearch, gbc);

        // Bo loc button
        JButton btnResetAll = new JButton("Bỏ lọc") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? SURFACE : Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(OUTLINE);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnResetAll.setFont(FONT_BOLD);
        btnResetAll.setForeground(ON_SURF_VAR);
        btnResetAll.setContentAreaFilled(false);
        btnResetAll.setBorderPainted(false);
        btnResetAll.setFocusPainted(false);
        btnResetAll.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnResetAll.setPreferredSize(new Dimension(100, 38));
        btnResetAll.addActionListener(e -> {
            txtSearch.setText("");
            if (filterGaDi  != null) filterGaDi.clearSelection();
            if (filterGaDen != null) filterGaDen.clearSelection();
            if (dateFrom != null) dateFrom.setValue(null);
            if (dateTo   != null) dateTo.setValue(null);
            applyFilter();
        });

        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 0, 0, 0);
        bgPanel.add(btnResetAll, gbc);

        searchRow.add(bgPanel, BorderLayout.CENTER);
        wrapper.add(searchRow);

        // --- Row 2: Entity Filters (Below Search) ---
        JPanel filterRow = new JPanel(new GridBagLayout());
        filterRow.setOpaque(false);
        filterRow.setBorder(new EmptyBorder(12, 5, 0, 5));

        GridBagConstraints fgbc = new GridBagConstraints();
        fgbc.gridy = 0;
        fgbc.fill = GridBagConstraints.HORIZONTAL;
        fgbc.weightx = 1.0;
        fgbc.insets = new Insets(0, 0, 0, 10);

        // Ga di
        filterGaDi = new SearchableComboBox<>(
                ga -> ga.getTenGa() + " (" + ga.getMaGa() + ")",
                (ga, q) -> ga.getTenGa().toLowerCase().contains(q) || ga.getMaGa().toLowerCase().contains(q));
        filterGaDi.setPlaceholder("Tất cả ga đi");
        filterGaDi.setOnChanged(this::applyFilter);
        filterRow.add(buildFilterGroupLich("GA ĐI", filterGaDi), fgbc);

        // Ga den
        filterGaDen = new SearchableComboBox<>(
                ga -> ga.getTenGa() + " (" + ga.getMaGa() + ")",
                (ga, q) -> ga.getTenGa().toLowerCase().contains(q) || ga.getMaGa().toLowerCase().contains(q));
        filterGaDen.setPlaceholder("Tất cả ga đến");
        filterGaDen.setOnChanged(this::applyFilter);
        fgbc.gridx = 1;
        filterRow.add(buildFilterGroupLich("GA ĐẾN", filterGaDen), fgbc);

        // Tu ngay
        dateFrom = new DatePickerField();
        dateFrom.setPreferredSize(new Dimension(0, 40));
        dateFrom.addPropertyChangeListener("value", e -> applyFilter());
        fgbc.gridx = 2;
        filterRow.add(buildFilterGroupLich("TỪ NGÀY", dateFrom), fgbc);

        // Den ngay
        dateTo = new DatePickerField();
        dateTo.setPreferredSize(new Dimension(0, 40));
        dateTo.addPropertyChangeListener("value", e -> applyFilter());
        fgbc.gridx = 3; fgbc.insets = new Insets(0, 0, 0, 0);
        filterRow.add(buildFilterGroupLich("ĐẾN NGÀY", dateTo), fgbc);

        wrapper.add(filterRow);

        return wrapper;
    }

    private JPanel buildFilterGroupLich(String labelText, JComponent field) {
        JPanel group = new JPanel();
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
        group.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(FONT_STAT_LBL);
        lbl.setForeground(ON_SURF_VAR);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        group.add(lbl);
        group.add(Box.createVerticalStrut(4));
        group.add(field);
        return group;
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
        card.add(buildTableHeader(), BorderLayout.NORTH);
        card.add(buildTableSection(), BorderLayout.CENTER);
        card.add(buildPaginationBar(), BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildTableHeader() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, OUTLINE),
                new EmptyBorder(16, 20, 16, 20)));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        ImageIcon icoList = loadScaledIcon("bieuTuongLich.png", 18);
        if (icoList != null) left.add(new JLabel(icoList));
        JLabel lbl = new JLabel("Danh sách lịch chạy");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lbl.setForeground(ON_SURFACE);
        left.add(lbl);

        JButton btnThem = createPrimaryButton("+  Thêm lịch");
        ImageIcon icoThem = loadScaledIcon("nutThem.png", 15);
        if (icoThem != null) { btnThem.setIcon(icoThem); btnThem.setText("  Thêm lịch"); }
        btnThem.addActionListener(e -> openDialog(null));

        bar.add(left, BorderLayout.WEST);
        bar.add(btnThem, BorderLayout.EAST);
        return bar;
    }

    private JScrollPane buildTableSection() {
        tableModel = new LichTableModel();
        table = new JTable(tableModel);
        table.setRowHeight(52);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(OUTLINE);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(PRIMARY_LIGHT);
        table.setSelectionForeground(ON_SURFACE);
        table.setFont(FONT_BODY);
        table.setFillsViewportHeight(true);
        table.setBackground(CARD_BG);
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
        });

        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tbl, Object value,
                    boolean isSel, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(tbl, value, isSel, hasFocus, row, col);
                lbl.setFont(FONT_HEADER);
                lbl.setForeground(ON_SURF_VAR);
                lbl.setBackground(SURFACE);
                lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, OUTLINE),
                        new EmptyBorder(0, 16, 0, 8)));
                lbl.setPreferredSize(new Dimension(lbl.getPreferredSize().width, 44));
                return lbl;
            }
        });

        // Columns: Mã lịch | Đoàn tàu | Tuyến | Thời gian bắt đầu | Thời gian chạy | Thao tác
        TableColumnModel cm = table.getColumnModel();
        int[] widths = {110, 130, 200, 150, 130, 180};
        for (int i = 0; i < widths.length; i++) cm.getColumn(i).setPreferredWidth(widths[i]);

        cm.getColumn(0).setCellRenderer(new RowRenderer(FONT_MONO, PRIMARY));
        cm.getColumn(1).setCellRenderer(new RowRenderer(FONT_BOLD, ON_SURFACE));
        cm.getColumn(2).setCellRenderer(new RowRenderer(FONT_BODY, ON_SURFACE));
        cm.getColumn(3).setCellRenderer(new RowRenderer(FONT_SMALL, ON_SURF_VAR));
        cm.getColumn(4).setCellRenderer(new DurationRenderer());
        cm.getColumn(5).setCellRenderer(new ActionRenderer());
        cm.getColumn(5).setCellEditor(new ActionEditor());

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
        btn.setForeground(page == currentPage ? Color.WHITE : ON_SURF_VAR);
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
        int confirm = JOptionPane.showConfirmDialog(this, msg, title,
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        boolean ok = daoLich.setHoatDong(lich.getMaLich(), !current);
        if (ok) loadData();
        else JOptionPane.showMessageDialog(this, "Không thể cập nhật trạng thái lịch.", "Lỗi", JOptionPane.ERROR_MESSAGE);
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
        if (isSel) return PRIMARY_LIGHT;
        if (row == hoveredRow) return ROW_HOVER;
        return row % 2 == 0 ? CARD_BG : ROW_ALT;
    }

    private ImageIcon loadScaledIcon(String name, int size) {
        try {
            java.net.URL url = getClass().getResource("/icons/" + name);
            if (url != null) return new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH));
        } catch (Exception ignored) {}
        return null;
    }

    private JTextField createSearchField(String placeholder) {
        JTextField f = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !hasFocus()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(new Color(0x9E, 0xA7, 0xB0));
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
        ImageIcon icoSearch = loadScaledIcon("nutTimKiem.png", 16);
        if (icoSearch != null) { btn.setIcon(icoSearch); btn.setText("  Tìm"); }
        else btn.setText("Tìm");
        btn.setFont(FONT_BTN); btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false); btn.setBorderPainted(false);
        btn.setFocusPainted(false); btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(90, 38));
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
            "THỜI GIAN BẮT ĐẦU", "THỜI GIAN CHẠY", "THAO TÁC"
        };
        private List<Lich> data = new ArrayList<>();

        void setData(List<Lich> d) { this.data = new ArrayList<>(d); fireTableDataChanged(); }
        Lich getAt(int r) { return (r >= 0 && r < data.size()) ? data.get(r) : null; }

        @Override public int getRowCount()    { return data.size(); }
        @Override public int getColumnCount() { return COLS.length; }
        @Override public String getColumnName(int c) { return COLS[c]; }
        @Override public boolean isCellEditable(int r, int c) { return c == 5; }

        @Override public Object getValueAt(int r, int c) {
            Lich l = data.get(r);
            return switch (c) {
                case 0 -> l.getMaLich();
                case 1 -> l.getDoanTau() != null ? l.getDoanTau().getMaDoanTau() : "";
                case 2 -> tuyenDisplayName(l.getTuyen());
                case 3 -> l.getThoiGianBatDau() != null
                        ? l.getThoiGianBatDau().format(FMT_DATETIME) : "";
                case 4 -> l.getThoiGianChay();
                case 5 -> l;
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

    private class ActionRenderer extends JPanel implements TableCellRenderer {
        private final JLabel lblSua    = new JLabel();
        private final JLabel lblToggle = new JLabel();
        private Color toggleBg = ERROR_BG;

        ActionRenderer() {
            setLayout(new GridBagLayout());
            setOpaque(true);
            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(0, 5, 0, 5);

            ImageIcon iSua = loadScaledIcon("nutSua.png", 14);

            lblSua.setFont(FONT_BADGE); lblSua.setForeground(PRIMARY);
            lblSua.setHorizontalAlignment(SwingConstants.CENTER);
            lblSua.setPreferredSize(new Dimension(72, 28));
            if (iSua != null) lblSua.setIcon(iSua);
            lblSua.setText(iSua != null ? "  Sửa" : "Sửa");

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

            ImageIcon iSua = loadScaledIcon("nutSua.png", 14);

            styleActionBtn(btnSua, "Sửa", PRIMARY, PRIMARY_LIGHT, iSua, 72);
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
            btn.setFont(FONT_BADGE); btn.setForeground(fg); btn.setBackground(bg);
            if (ico != null) { btn.setIcon(ico); btn.setText("  " + text); }
            else btn.setText(text);
            btn.setBorderPainted(false); btn.setFocusPainted(false);
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
    @Override public void reset() { txtSearch.setText(""); loadData(); }
}
