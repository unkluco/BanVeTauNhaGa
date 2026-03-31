package com.modules;

import com.connectDB.ConnectDB;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
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

    // --- Pagination ---
    private int currentPage  = 1;
    private int rowsPerPage  = 10;
    private int totalRecords = 0;
    private boolean isRefreshing = false;
    private JLabel lblPageInfo;
    private JPanel paginationPanel;

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

        JPanel search = buildSearchSection();
        search.setAlignmentX(Component.LEFT_ALIGNMENT);
        search.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        top.add(header);
        top.add(Box.createVerticalStrut(20));
        top.add(stats);
        top.add(Box.createVerticalStrut(20));
        top.add(search);
        top.add(Box.createVerticalStrut(20));

        add(top, BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);
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
        JLabel lblTitle = new JLabel("Qu\u1EA3n l\u00FD l\u1ECBch ch\u1EA1y");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(ON_SURFACE);
        titleRow.add(lblTitle);
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblDesc = new JLabel("Xem v\u00E0 qu\u1EA3n l\u00FD c\u00E1c l\u1ECBch ch\u1EA1y, \u0111o\u00E0n t\u00E0u v\u00E0 tuy\u1EBFn \u0111\u01B0\u1EDDng.");
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

        row.add(buildStatCard("T\u1ED4NG L\u1ECBCH CH\u1EA0Y", lblStatTong, PRIMARY, "bieuTuongLich.png"));
        row.add(buildStatCard("L\u1ECBCH H\u00D4M NAY", lblStatHomNay, SUCCESS_FG, "bieuTuongThoiGian.png"));
        row.add(buildStatCard("L\u01AF\u1EE3T \u0110O\u00C0N T\u00C0U", lblStatTuyen, WARN_FG, "bieuTuongTau.png"));
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

    // ---- Search section ----
    private JPanel buildSearchSection() {
        JPanel section = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(SURFACE_DIM);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                g2.dispose();
            }
        };
        section.setOpaque(false);
        section.setLayout(new BorderLayout(12, 0));
        section.setBorder(new EmptyBorder(16, 24, 16, 24));

        JPanel labelRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        labelRow.setOpaque(false);
        ImageIcon icoSearch = loadScaledIcon("nutTimKiem.png", 14);
        if (icoSearch != null) labelRow.add(new JLabel(icoSearch));
        JLabel lbl = new JLabel("T\u00CCM KI\u1EBEM L\u1ECBCH");
        lbl.setFont(FONT_STAT_LBL);
        lbl.setForeground(ON_SURF_VAR);
        labelRow.add(lbl);

        JPanel fieldRow = new JPanel(new BorderLayout(8, 0));
        fieldRow.setOpaque(false);

        txtSearch = createSearchField("Nh\u1EADp m\u00E3 l\u1ECBch, t\u00EAn tuy\u1EBFn, m\u00E3 \u0111o\u00E0n t\u00E0u...");
        txtSearch.addActionListener(e -> doSearch());

        JButton btnSearch = createSearchButton();
        btnSearch.addActionListener(e -> doSearch());

        JButton btnReset = new JButton("L\u00E0m m\u1EDBi");
        btnReset.setFont(FONT_BADGE);
        btnReset.setForeground(ON_SURF_VAR);
        btnReset.setBackground(CARD_BG);
        btnReset.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1, true),
                new EmptyBorder(4, 12, 4, 12)));
        btnReset.setFocusPainted(false);
        btnReset.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnReset.setPreferredSize(new Dimension(90, 38));
        btnReset.addActionListener(e -> { txtSearch.setText(""); applyFilter(); });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.add(btnSearch);
        btnRow.add(btnReset);

        fieldRow.add(txtSearch, BorderLayout.CENTER);
        fieldRow.add(btnRow, BorderLayout.EAST);

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);
        inner.add(labelRow);
        inner.add(Box.createVerticalStrut(8));
        inner.add(fieldRow);

        section.add(inner, BorderLayout.CENTER);
        return section;
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
        JLabel lbl = new JLabel("Danh s\u00E1ch l\u1ECBch ch\u1EA1y");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lbl.setForeground(ON_SURFACE);
        left.add(lbl);

        JButton btnThem = createPrimaryButton("\u2795  Th\u00EAm l\u1ECBch");
        ImageIcon icoThem = loadScaledIcon("nutThem.png", 15);
        if (icoThem != null) { btnThem.setIcon(icoThem); btnThem.setText("  Th\u00EAm l\u1ECBch"); }
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
        int[] widths = {110, 130, 200, 150, 130, 160};
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
        filteredData = new ArrayList<>(allData);
        totalRecords = filteredData.size();
        currentPage  = 1;
        refreshTable();
    }

    private void doSearch() {
        String kw = txtSearch.getText().trim().toLowerCase();
        if (kw.isEmpty()) { applyFilter(); return; }
        filteredData = new ArrayList<>();
        for (Lich l : allData) {
            String maLich   = l.getMaLich() != null ? l.getMaLich().toLowerCase() : "";
            String maDoan   = l.getDoanTau() != null ? l.getDoanTau().getMaDoanTau().toLowerCase() : "";
            String tenDoan  = l.getDoanTau() != null && l.getDoanTau().getTenDoanTau() != null
                            ? l.getDoanTau().getTenDoanTau().toLowerCase() : "";
            String maTuyen  = l.getTuyen() != null ? l.getTuyen().getMaTuyen().toLowerCase() : "";
            String tenTuyen = tuyenDisplayName(l.getTuyen()).toLowerCase();
            if (maLich.contains(kw) || maDoan.contains(kw) || tenDoan.contains(kw)
                    || maTuyen.contains(kw) || tenTuyen.contains(kw)) {
                filteredData.add(l);
            }
        }
        totalRecords = filteredData.size();
        currentPage  = 1;
        refreshTable();
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
                    ? "Kh\u00F4ng t\u00ECm th\u1EA5y l\u1ECBch n\u00E0o"
                    : "Hi\u1EC3n th\u1ECB " + (start + 1) + " \u2013 " + end + " / " + totalRecords + " l\u1ECBch");

            rebuildPagination(totalPages);
        } finally { isRefreshing = false; }
    }

    private void rebuildPagination(int totalPages) {
        paginationPanel.removeAll();
        addNavBtn("\u276E", currentPage > 1, () -> { currentPage--; refreshTable(); });
        for (int i = 1; i <= totalPages; i++) {
            if (totalPages > 7) {
                if (i == 1 || i == totalPages || (i >= currentPage - 1 && i <= currentPage + 1)) {
                    addPageBtn(i);
                } else if (i == currentPage - 2 || i == currentPage + 2) {
                    JLabel dots = new JLabel("\u2026");
                    dots.setFont(FONT_SMALL); dots.setForeground(ON_SURF_VAR);
                    dots.setBorder(new EmptyBorder(0, 4, 0, 4));
                    paginationPanel.add(dots);
                }
            } else { addPageBtn(i); }
        }
        addNavBtn("\u276F", currentPage < totalPages, () -> { currentPage++; refreshTable(); });
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

    private void deleteLich(Lich lich) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "X\u00E1c nh\u1EADn x\u00F3a l\u1ECBch " + lich.getMaLich() + "?",
                "X\u00F3a l\u1ECBch", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        Connection con = ConnectDB.getCon();
        if (con == null) { JOptionPane.showMessageDialog(this, "L\u1ED7i k\u1EBFt n\u1ED1i.", "L\u1ED7i", JOptionPane.ERROR_MESSAGE); return; }
        try (PreparedStatement ps = con.prepareStatement("DELETE FROM Lich WHERE maLich = ?")) {
            ps.setString(1, lich.getMaLich());
            if (ps.executeUpdate() > 0) loadData();
            else JOptionPane.showMessageDialog(this, "Kh\u00F4ng th\u1EC3 x\u00F3a l\u1ECBch n\u00E0y.", "L\u1ED7i", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "L\u1ED7i: " + ex.getMessage(), "L\u1ED7i", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =====================================================================
    //  HELPERS
    // =====================================================================

    private String tuyenDisplayName(Tuyen t) {
        if (t == null) return "";
        String gaDi  = t.getGaDi()  != null ? t.getGaDi().getTenGa()  : t.getMaTuyen();
        String gaDen = t.getGaDen() != null ? t.getGaDen().getTenGa() : "";
        return gaDi + (gaDen.isEmpty() ? "" : " \u2192 " + gaDen);
    }

    private String formatDuration(String minutes) {
        try {
            int m = Integer.parseInt(minutes);
            int h = m / 60; int rem = m % 60;
            return h > 0 ? h + " gi\u1EDD " + rem + " ph\u00FAt" : rem + " ph\u00FAt";
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
        if (icoSearch != null) { btn.setIcon(icoSearch); btn.setText("  T\u00ECm"); }
        else btn.setText("T\u00ECm");
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
            "M\u00C3 L\u1ECBCH", "\u0110O\u00C0N T\u00C0U", "TUY\u1EBCN",
            "TH\u1EDEI GIAN B\u1EEAT \u0110\u1EA6U", "TH\u1EDEI GIAN CH\u1EA0Y", "THAO T\u00C1C"
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
            setFont(font); setForeground(fg);
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
        private final JLabel lblSua  = new JLabel();
        private final JLabel lblXoa  = new JLabel();

        ActionRenderer() {
            setLayout(new GridBagLayout());
            setOpaque(true);
            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(0, 5, 0, 5);

            ImageIcon iSua = loadScaledIcon("nutSua.png", 14);
            ImageIcon iXoa = loadScaledIcon("nutXoa.png", 14);

            lblSua.setFont(FONT_BADGE); lblSua.setForeground(PRIMARY);
            lblSua.setHorizontalAlignment(SwingConstants.CENTER);
            lblSua.setPreferredSize(new Dimension(72, 28));
            if (iSua != null) lblSua.setIcon(iSua);
            lblSua.setText(iSua != null ? "  S\u1EEDa" : "S\u1EEDa");

            lblXoa.setFont(FONT_BADGE); lblXoa.setForeground(ERROR_FG);
            lblXoa.setHorizontalAlignment(SwingConstants.CENTER);
            lblXoa.setPreferredSize(new Dimension(72, 28));
            if (iXoa != null) lblXoa.setIcon(iXoa);
            lblXoa.setText(iXoa != null ? "  X\u00F3a" : "X\u00F3a");

            add(lblSua, g); add(lblXoa, g);
        }

        @Override public Component getTableCellRendererComponent(JTable t, Object v,
                boolean sel, boolean focus, int row, int col) {
            setBackground(getRowBg(sel, row));
            return this;
        }

        @Override protected void paintChildren(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Rectangle rs = lblSua.getBounds();
            g2.setColor(PRIMARY_LIGHT);
            g2.fillRoundRect(rs.x, rs.y, rs.width, rs.height, 8, 8);
            Rectangle rx = lblXoa.getBounds();
            g2.setColor(ERROR_BG);
            g2.fillRoundRect(rx.x, rx.y, rx.width, rx.height, 8, 8);
            g2.dispose(); super.paintChildren(g);
        }
    }

    private class ActionEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel   panel   = new JPanel(new GridBagLayout());
        private final JButton  btnSua  = new JButton();
        private final JButton  btnXoa  = new JButton();
        private int editingRow;

        ActionEditor() {
            panel.setOpaque(true);
            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(0, 5, 0, 5);

            ImageIcon iSua = loadScaledIcon("nutSua.png", 14);
            ImageIcon iXoa = loadScaledIcon("nutXoa.png", 14);

            styleActionBtn(btnSua, "S\u1EEDa", PRIMARY, PRIMARY_LIGHT, iSua);
            styleActionBtn(btnXoa, "X\u00F3a", ERROR_FG, ERROR_BG, iXoa);

            panel.add(btnSua, g); panel.add(btnXoa, g);

            btnSua.addActionListener(e -> {
                fireEditingStopped();
                Lich l = tableModel.getAt(editingRow);
                if (l != null) openDialog(l);
            });
            btnXoa.addActionListener(e -> {
                fireEditingStopped();
                Lich l = tableModel.getAt(editingRow);
                if (l != null) deleteLich(l);
            });
        }

        private void styleActionBtn(JButton btn, String text, Color fg, Color bg, ImageIcon ico) {
            btn.setFont(FONT_BADGE); btn.setForeground(fg); btn.setBackground(bg);
            if (ico != null) { btn.setIcon(ico); btn.setText("  " + text); }
            else btn.setText(text);
            btn.setBorderPainted(false); btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setPreferredSize(new Dimension(72, 28));
        }

        @Override public Component getTableCellEditorComponent(JTable t, Object v,
                boolean sel, int row, int col) {
            editingRow = row;
            panel.setBackground(getRowBg(true, row));
            return panel;
        }
        @Override public Object getCellEditorValue() { return null; }
    }

    // =====================================================================
    //  AppModule
    // =====================================================================

    @Override public String getTitle()  { return "Qu\u1EA3n l\u00FD l\u1ECBch ch\u1EA1y"; }
    @Override public JPanel getView()   { return this; }
    @Override public void setOnResult(Consumer<Object> cb) { this.callback = cb; }
    @Override public void reset() { txtSearch.setText(""); loadData(); }
}
