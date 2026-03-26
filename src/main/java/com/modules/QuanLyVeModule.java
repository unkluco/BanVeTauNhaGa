package com.modules;

import com.dao.DAO_Ve;
import com.dao.DAO_ChiTietHoaDon;
import com.entity.Ve;
import com.entity.ChiTietHoaDon;
import com.entity.Lich;
import com.entity.Tuyen;
import com.enums.TrangThaiVe;

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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class QuanLyVeModule extends JPanel implements AppModule {

    private Consumer<Object> callback;

    // --- UI ---
    private JTextField   txtSearchHoaDon;
    private JTextField   txtSearchVe;
    private JTable       table;
    private VeTableModel tableModel;
    private JComboBox<String> cboTrangThai;

    // --- Pagination ---
    private int currentPage = 1;
    private int rowsPerPage = 10;
    private int totalRecords = 0;
    private JLabel lblPageInfo;
    private JPanel paginationPanel;

    // --- Data ---
    private List<Ve> allData      = new ArrayList<>();
    private List<Ve> filteredData = new ArrayList<>();
    private DAO_ChiTietHoaDon daoChiTietHoaDon = new DAO_ChiTietHoaDon();

    // --- Stats ---
    private JLabel lblStatTongHoaDon;
    private JLabel lblStatDoanhThu;
    private JLabel lblStatTyLeHuy;

    // --- Design tokens ---
    private static final Color PRIMARY       = new Color(0x00, 0x5D, 0x90);
    private static final Color PRIMARY_LIGHT = new Color(0xE3, 0xF2, 0xFD);
    private static final Color SURFACE       = new Color(0xF8, 0xFA, 0xFC);
    private static final Color CARD_BG       = Color.WHITE;
    private static final Color ON_SURFACE    = new Color(0x1A, 0x1D, 0x21);
    private static final Color ON_SURF_VAR   = new Color(0x5F, 0x67, 0x70);
    private static final Color OUTLINE       = new Color(0xDE, 0xE3, 0xE8);
    private static final Color ROW_ALT       = new Color(0xF8, 0xFA, 0xFC);
    private static final Color ROW_HOVER     = new Color(0xEE, 0xF5, 0xFB);
    private static final Color ERROR         = new Color(0xBA, 0x1A, 0x1A);

    private static final Color STATUS_GREEN_BG  = new Color(0xDC, 0xFA, 0xE6);
    private static final Color STATUS_GREEN_FG  = new Color(0x16, 0x6B, 0x3A);
    private static final Color STATUS_GRAY_BG   = new Color(0xF1, 0xF5, 0xF9);
    private static final Color STATUS_GRAY_FG   = new Color(0x64, 0x74, 0x8B);

    private static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font FONT_DESC    = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_BOLD    = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_MONO    = new Font("Consolas", Font.BOLD, 13);
    private static final Font FONT_BADGE   = new Font("Segoe UI", Font.BOLD, 11);
    private static final Font FONT_HEADER  = new Font("Segoe UI", Font.BOLD, 11);
    private static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_BTN     = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_STAT_NUM = new Font("Segoe UI", Font.BOLD, 28);
    private static final Font FONT_STAT_LBL = new Font("Segoe UI", Font.BOLD, 10);

    private static final DateTimeFormatter FMT_DATETIME = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy");
    private static final NumberFormat FMT_MONEY = NumberFormat.getInstance(new Locale("vi", "VN"));

    // --- Hidden buttons for AppModule compliance ---
    private JButton btnSubmit = new JButton();
    private JButton btnCancel = new JButton();
    private JPanel  btnPanel  = new JPanel();

    // --- Hover ---
    private int hoveredRow = -1;

    public QuanLyVeModule() {
        setLayout(new BorderLayout());
        setBackground(SURFACE);
        setBorder(new EmptyBorder(28, 36, 28, 36));
        btnPanel.setVisible(false);
        buildUI();
        loadData();
    }

    // =================================================================
    //  BUILD UI
    // =================================================================

    private void buildUI() {
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setOpaque(false);

        JPanel header = buildHeader();
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JPanel stats = buildStatsRow();
        stats.setAlignmentX(Component.LEFT_ALIGNMENT);
        stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        JPanel search = buildSearchSection();
        search.setAlignmentX(Component.LEFT_ALIGNMENT);
        search.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        mainContent.add(header);
        mainContent.add(Box.createVerticalStrut(20));
        mainContent.add(stats);
        mainContent.add(Box.createVerticalStrut(20));
        mainContent.add(search);
        mainContent.add(Box.createVerticalStrut(20));

        add(mainContent, BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel lblTitle = new JLabel("Qu\u1EA3n l\u00FD v\u00E9");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(ON_SURFACE);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblDesc = new JLabel("Theo d\u00F5i giao d\u1ECBch, xu\u1EA5t h\u00F3a \u0111\u01A1n v\u00E0 x\u1EED l\u00FD ho\u00E0n tr\u1EA3 kh\u00E1ch h\u00E0ng.");
        lblDesc.setFont(FONT_DESC);
        lblDesc.setForeground(ON_SURF_VAR);
        lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        left.add(lblTitle);
        left.add(Box.createVerticalStrut(4));
        left.add(lblDesc);

        header.add(left, BorderLayout.CENTER);
        return header;
    }

    // =================================================================
    //  STATS ROW
    // =================================================================

    private JPanel buildStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 16, 0));
        row.setOpaque(false);

        lblStatTongHoaDon = new JLabel("0");
        lblStatDoanhThu = new JLabel("0");
        lblStatTyLeHuy = new JLabel("0%");

        row.add(buildStatCard("T\u1ED4NG V\u00C9", lblStatTongHoaDon, PRIMARY));
        row.add(buildStatCard("\u0110\u00C3 B\u00C1N", lblStatDoanhThu, PRIMARY));
        row.add(buildStatCard("T\u1EF6 L\u1EC6 H\u1EE6Y V\u00C9", lblStatTyLeHuy, ERROR));

        return row;
    }

    private JPanel buildStatCard(String label, JLabel valueLbl, Color valueColor) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xF2, 0xF4, 0xF6));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(FONT_STAT_LBL);
        lblLabel.setForeground(ON_SURF_VAR);
        lblLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        valueLbl.setFont(FONT_STAT_NUM);
        valueLbl.setForeground(valueColor);
        valueLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(lblLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(valueLbl);

        return card;
    }

    // =================================================================
    //  SEARCH SECTION
    // =================================================================

    private JPanel buildSearchSection() {
        JPanel section = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xF2, 0xF4, 0xF6));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                g2.dispose();
            }
        };
        section.setOpaque(false);
        section.setLayout(new GridLayout(1, 2, 24, 0));
        section.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Search by hoa don
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel lbl1 = new JLabel("T\u00CCM THEO M\u00C3 H\u00D3A \u0110\u01A0N");
        lbl1.setFont(FONT_STAT_LBL);
        lbl1.setForeground(ON_SURF_VAR);
        lbl1.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel row1 = new JPanel(new BorderLayout(8, 0));
        row1.setOpaque(false);
        row1.setAlignmentX(Component.LEFT_ALIGNMENT);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        txtSearchHoaDon = createSearchField("Nh\u1EADp m\u00E3 HD-...");
        JButton btn1 = createSearchButton();
        btn1.addActionListener(e -> searchByHoaDon());
        txtSearchHoaDon.addActionListener(e -> searchByHoaDon());

        row1.add(txtSearchHoaDon, BorderLayout.CENTER);
        row1.add(btn1, BorderLayout.EAST);

        left.add(lbl1);
        left.add(Box.createVerticalStrut(8));
        left.add(row1);

        // Search by ve
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setOpaque(false);

        JLabel lbl2 = new JLabel("T\u00CCM THEO M\u00C3 V\u00C9");
        lbl2.setFont(FONT_STAT_LBL);
        lbl2.setForeground(ON_SURF_VAR);
        lbl2.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel row2 = new JPanel(new BorderLayout(8, 0));
        row2.setOpaque(false);
        row2.setAlignmentX(Component.LEFT_ALIGNMENT);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        txtSearchVe = createSearchField("Nh\u1EADp m\u00E3 v\u00E9...");
        JButton btn2 = createSearchButton();
        btn2.addActionListener(e -> searchByMaVe());
        txtSearchVe.addActionListener(e -> searchByMaVe());

        row2.add(txtSearchVe, BorderLayout.CENTER);
        row2.add(btn2, BorderLayout.EAST);

        right.add(lbl2);
        right.add(Box.createVerticalStrut(8));
        right.add(row2);

        section.add(left);
        section.add(right);

        return section;
    }

    // =================================================================
    //  TABLE CARD
    // =================================================================

    private JPanel buildTableCard() {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(OUTLINE);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 16, 16));
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
                new EmptyBorder(16, 20, 16, 20)
        ));

        JLabel lbl = new JLabel("Danh s\u00E1ch v\u00E9");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lbl.setForeground(ON_SURFACE);

        // Filter tabs
        JPanel tabs = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xE6, 0xE8, 0xEA));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
            }
        };
        tabs.setOpaque(false);
        tabs.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 3));

        String[] tabNames = {"T\u1EA5t c\u1EA3", "\u0110\u00E3 b\u00E1n", "\u0110\u00E3 h\u1EE7y"};
        JButton[] tabButtons = new JButton[tabNames.length];

        for (int i = 0; i < tabNames.length; i++) {
            final int idx = i;
            JButton tab = new JButton(tabNames[i]) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (getModel().isPressed() || getClientProperty("active") == Boolean.TRUE) {
                        g2.setColor(Color.WHITE);
                        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                    }
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            tab.setFont(FONT_BADGE);
            tab.setForeground(i == 0 ? PRIMARY : ON_SURF_VAR);
            tab.setContentAreaFilled(false);
            tab.setBorderPainted(false);
            tab.setFocusPainted(false);
            tab.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            tab.setPreferredSize(new Dimension(90, 28));
            if (i == 0) tab.putClientProperty("active", Boolean.TRUE);

            tab.addActionListener(e -> {
                for (JButton b : tabButtons) {
                    b.putClientProperty("active", Boolean.FALSE);
                    b.setForeground(ON_SURF_VAR);
                    b.repaint();
                }
                tab.putClientProperty("active", Boolean.TRUE);
                tab.setForeground(PRIMARY);
                tab.repaint();
                applyTabFilter(idx);
            });

            tabButtons[i] = tab;
            tabs.add(tab);
        }

        bar.add(lbl, BorderLayout.WEST);
        bar.add(tabs, BorderLayout.EAST);

        return bar;
    }

    private int activeTabIndex = 0;

    private void applyTabFilter(int tabIdx) {
        activeTabIndex = tabIdx;
        applyFilter();
    }

    private JScrollPane buildTableSection() {
        tableModel = new VeTableModel();
        table = new JTable(tableModel);
        table.setRowHeight(56);
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
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        // Hover
        table.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row != hoveredRow) { hoveredRow = row; table.repaint(); }
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) { hoveredRow = -1; table.repaint(); }
        });

        // Header style
        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setFont(FONT_HEADER);
                lbl.setForeground(ON_SURF_VAR);
                lbl.setBackground(new Color(0xF8, 0xFA, 0xFC));
                lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, OUTLINE),
                        new EmptyBorder(0, 20, 0, 8)
                ));
                lbl.setPreferredSize(new Dimension(lbl.getPreferredSize().width, 44));
                if (column == 6) lbl.setHorizontalAlignment(SwingConstants.CENTER);
                return lbl;
            }
        });

        // Columns: Mã vé, Hành khách, Ga đi, Ga đến, Khởi hành, Trạng thái, Thao tác
        TableColumnModel colModel = table.getColumnModel();
        int[] widths = {110, 170, 110, 110, 140, 120, 100};
        for (int i = 0; i < widths.length; i++) {
            colModel.getColumn(i).setPreferredWidth(widths[i]);
        }

        colModel.getColumn(0).setCellRenderer(new RowCellRenderer(FONT_MONO, PRIMARY));
        colModel.getColumn(1).setCellRenderer(new RowCellRenderer(FONT_BOLD, ON_SURFACE));
        colModel.getColumn(2).setCellRenderer(new RowCellRenderer(FONT_BODY, ON_SURFACE));
        colModel.getColumn(3).setCellRenderer(new RowCellRenderer(FONT_BODY, ON_SURFACE));
        colModel.getColumn(4).setCellRenderer(new RowCellRenderer(FONT_SMALL, ON_SURF_VAR));
        colModel.getColumn(5).setCellRenderer(new TrangThaiBadgeRenderer());
        colModel.getColumn(6).setCellRenderer(new ActionButtonRenderer());
        colModel.getColumn(6).setCellEditor(new ActionButtonEditor());


        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(CARD_BG);
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setWheelScrollingEnabled(false);

        // Recalc rowsPerPage when viewport resizes (window resize, etc.)
        sp.getViewport().addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int newRows = calcRowsFromViewport();
                if (newRows > 0 && newRows != rowsPerPage) {
                    rowsPerPage = newRows;
                    if (!isRefreshing) refreshTable();
                }
            }
        });

        return sp;
    }

    private boolean isRefreshing = false;

    private int calcRowsFromViewport() {
        if (table == null || !(table.getParent() instanceof JViewport vp)) return 0;
        int viewH = vp.getHeight();
        int rh = table.getRowHeight();
        if (rh <= 0) rh = 56;
        int headerH = table.getTableHeader().getHeight();
        if (headerH <= 0) headerH = table.getTableHeader().getPreferredSize().height;
        if (headerH <= 0) headerH = 44;
        int available = viewH - headerH;
        return available > 0 ? Math.max(1, available / rh + 1) : 0;
    }

    private JPanel buildPaginationBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, OUTLINE),
                new EmptyBorder(12, 20, 12, 20)
        ));

        lblPageInfo = new JLabel();
        lblPageInfo.setFont(FONT_SMALL);
        lblPageInfo.setForeground(ON_SURF_VAR);

        paginationPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        paginationPanel.setOpaque(false);

        bar.add(lblPageInfo, BorderLayout.WEST);
        bar.add(paginationPanel, BorderLayout.EAST);
        return bar;
    }

    // =================================================================
    //  HELPERS
    // =================================================================

    private JTextField createSearchField(String placeholder) {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !hasFocus()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setColor(new Color(0x9E, 0xA7, 0xB0));
                    g2.setFont(FONT_BODY);
                    Insets ins = getInsets();
                    g2.drawString(placeholder, ins.left, getHeight() / 2 + 5);
                    g2.dispose();
                }
            }
        };
        field.setFont(FONT_BODY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1),
                new EmptyBorder(6, 12, 6, 12)
        ));
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) { field.repaint(); }
            public void focusLost(java.awt.event.FocusEvent e) { field.repaint(); }
        });
        return field;
    }

    private JButton createSearchButton() {
        JButton btn = new JButton("T\u00ECm") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? PRIMARY.darker() : getModel().isRollover() ? PRIMARY.brighter() : PRIMARY);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BTN);
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(70, 38));
        return btn;
    }

    // =================================================================
    //  DATA
    // =================================================================

    private void loadData() {
        SwingWorker<List<Ve>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Ve> doInBackground() {
                return new DAO_Ve().getAll();
            }

            @Override
            protected void done() {
                try {
                    allData = get();
                } catch (Exception e) {
                    allData = new ArrayList<>();
                }
                updateStats();
                applyFilter();
            }
        };
        worker.execute();
    }

    private void updateStats() {
        long totalVe = allData.size();
        long soldVe = allData.stream().filter(v -> v.getTrangThai() == TrangThaiVe.DA_BAN).count();
        long cancelledVe = allData.stream().filter(v -> v.getTrangThai() == TrangThaiVe.DA_HUY).count();

        lblStatTongHoaDon.setText(String.valueOf(totalVe));
        lblStatDoanhThu.setText(String.valueOf(soldVe));
        double cancelRate = totalVe > 0 ? (cancelledVe * 100.0 / totalVe) : 0;
        lblStatTyLeHuy.setText(String.format("%.1f%%", cancelRate));
    }

    // =================================================================
    //  FILTER & SEARCH
    // =================================================================

    private void applyFilter() {
        filteredData = new ArrayList<>();
        for (Ve ve : allData) {
            if (activeTabIndex == 1 && ve.getTrangThai() != TrangThaiVe.DA_BAN) continue;
            if (activeTabIndex == 2 && ve.getTrangThai() != TrangThaiVe.DA_HUY) continue;
            filteredData.add(ve);
        }
        totalRecords = filteredData.size();
        currentPage = 1;
        refreshTable();
    }

    private void searchByHoaDon() {
        String keyword = txtSearchHoaDon.getText().trim();
        if (keyword.isEmpty()) {
            applyFilter();
            return;
        }
        filteredData = new ArrayList<>();
        for (Ve ve : allData) {
            // Tim hoa don qua ChiTietHoaDon
            ChiTietHoaDon cthd = daoChiTietHoaDon.findByVe(ve.getMaVe());
            if (cthd != null && cthd.getHoaDon() != null
                    && cthd.getHoaDon().getMaHoaDon().toLowerCase().contains(keyword.toLowerCase())) {
                filteredData.add(ve);
            }
        }
        totalRecords = filteredData.size();
        currentPage = 1;
        refreshTable();
    }

    private void searchByMaVe() {
        String keyword = txtSearchVe.getText().trim();
        if (keyword.isEmpty()) {
            applyFilter();
            return;
        }
        filteredData = new ArrayList<>();
        for (Ve ve : allData) {
            if (ve.getMaVe().toLowerCase().contains(keyword.toLowerCase())) {
                filteredData.add(ve);
            }
        }
        totalRecords = filteredData.size();
        currentPage = 1;
        refreshTable();
    }

    // =================================================================
    //  PAGINATION
    // =================================================================

    private void refreshTable() {
        isRefreshing = true;
        try {
            int vpRows = calcRowsFromViewport();
            if (vpRows > 0) {
                rowsPerPage = vpRows;
            } else {
                // First load: viewport not ready
                // Overhead: border(56) + header(70) + stats(110+20) + search(90+20) + cardHeader(60) + tableHeader(44) + pagination(56) ≈ 530
                int screenH = Toolkit.getDefaultToolkit().getScreenSize().height;
                int rh = (table != null && table.getRowHeight() > 0) ? table.getRowHeight() : 56;
                rowsPerPage = Math.max(3, (screenH - 530) / rh);
            }

        int totalPages = Math.max(1, (int) Math.ceil((double) totalRecords / rowsPerPage));
        if (currentPage > totalPages) currentPage = totalPages;

        int start = (currentPage - 1) * rowsPerPage;
        int end = Math.min(start + rowsPerPage, totalRecords);

        List<Ve> pageData = filteredData.subList(start, end);

        // Load route info from Ve -> Lich -> Tuyen, customer from ChiTietHoaDon -> HoaDon -> KhachHang
        List<VeTableModel.VeRow> rows = new ArrayList<>();
        for (Ve ve : pageData) {
            String gaDi = "", gaDen = "", khoiHanh = "", tenKhachHang = "";
            try {
                if (ve.getLich() != null && ve.getLich().getTuyen() != null) {
                    Tuyen tuyen = ve.getLich().getTuyen();
                    gaDi = tuyen.getGaDi() != null ? tuyen.getGaDi().getTenGa() : "";
                    gaDen = tuyen.getGaDen() != null ? tuyen.getGaDen().getTenGa() : "";
                }
                if (ve.getLich() != null && ve.getLich().getThoiGianBatDau() != null) {
                    khoiHanh = ve.getLich().getThoiGianBatDau().format(FMT_DATETIME);
                }
                ChiTietHoaDon cthd = daoChiTietHoaDon.findByVe(ve.getMaVe());
                if (cthd != null && cthd.getHoaDon() != null
                        && cthd.getHoaDon().getKhachHang() != null) {
                    tenKhachHang = cthd.getHoaDon().getKhachHang().getHoTen();
                }
            } catch (Exception ignored) {}
            rows.add(new VeTableModel.VeRow(ve, gaDi, gaDen, khoiHanh, tenKhachHang));
        }

        tableModel.setData(rows);

        lblPageInfo.setText(totalRecords == 0
                ? "Kh\u00F4ng t\u00ECm th\u1EA5y v\u00E9 n\u00E0o"
                : "Hi\u1EC3n th\u1ECB " + (start + 1) + " \u2013 " + end + " / " + totalRecords + " v\u00E9");

        rebuildPagination(totalPages);
        } finally {
            isRefreshing = false;
        }
    }

    private void rebuildPagination(int totalPages) {
        paginationPanel.removeAll();
        addNavButton("\u276E", currentPage > 1, () -> { currentPage--; refreshTable(); });
        for (int i = 1; i <= totalPages; i++) {
            if (totalPages > 7) {
                if (i == 1 || i == totalPages || (i >= currentPage - 1 && i <= currentPage + 1)) {
                    addPageButton(i);
                } else if (i == currentPage - 2 || i == currentPage + 2) {
                    JLabel dots = new JLabel("\u2026");
                    dots.setFont(FONT_SMALL);
                    dots.setForeground(ON_SURF_VAR);
                    dots.setBorder(new EmptyBorder(0, 6, 0, 6));
                    paginationPanel.add(dots);
                }
            } else {
                addPageButton(i);
            }
        }
        addNavButton("\u276F", currentPage < totalPages, () -> { currentPage++; refreshTable(); });
        paginationPanel.revalidate();
        paginationPanel.repaint();
    }

    private void addPageButton(int page) {
        JButton btn = new JButton(String.valueOf(page)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (page == currentPage) {
                    g2.setColor(PRIMARY);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_HEADER);
        btn.setPreferredSize(new Dimension(32, 32));
        btn.setMargin(new Insets(0, 0, 0, 0));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setForeground(page == currentPage ? Color.WHITE : ON_SURF_VAR);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> { currentPage = page; refreshTable(); });
        paginationPanel.add(btn);
    }

    private void addNavButton(String symbol, boolean enabled, Runnable action) {
        JButton btn = new JButton(symbol);
        btn.setFont(FONT_BODY);
        btn.setPreferredSize(new Dimension(32, 32));
        btn.setMargin(new Insets(0, 0, 0, 0));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setForeground(enabled ? ON_SURF_VAR : OUTLINE);
        btn.setEnabled(enabled);
        btn.setCursor(enabled ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
        btn.addActionListener(e -> action.run());
        paginationPanel.add(btn);
    }

    // =================================================================
    //  DATA MODEL
    // =================================================================

    // Non-static inner class de setValueAt co the truy cap allData va DAO
    private class VeTableModel extends AbstractTableModel {
        record VeRow(Ve ve, String gaDi, String gaDen, String khoiHanh, String tenKhachHang) {}
        private final String[] COLUMNS = {"M\u00C3 V\u00C9", "H\u00C0NH KH\u00C1CH", "GA \u0110I", "GA \u0110\u1EBEN", "KH\u1EDEI H\u00C0NH", "TR\u1EA0NG TH\u00C1I", "THAO T\u00C1C"};
        private List<VeRow> data = new ArrayList<>();

        void setData(List<VeRow> data) {
            this.data = new ArrayList<>(data);
            fireTableDataChanged();
        }

        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return COLUMNS.length; }
        @Override public String getColumnName(int c) { return COLUMNS[c]; }

        @Override
        public boolean isCellEditable(int r, int c) {
            return c == 6; // Chi cho phep nhan nut thao tac
        }

        @Override
        public Object getValueAt(int r, int c) {
            VeRow row = data.get(r);
            Ve ve = row.ve();
            return switch (c) {
                case 0 -> ve.getMaVe();
                case 1 -> row.tenKhachHang();
                case 2 -> row.gaDi();
                case 3 -> row.gaDen();
                case 4 -> row.khoiHanh();
                case 5 -> ve.getTrangThai() != null ? ve.getTrangThai() : TrangThaiVe.DA_BAN;
                case 6 -> ve.getTrangThai();
                default -> "";
            };
        }


        VeRow getRowAt(int r) { return (r >= 0 && r < data.size()) ? data.get(r) : null; }
    }

    // =================================================================
    //  CELL RENDERERS
    // =================================================================

    private class RowCellRenderer extends DefaultTableCellRenderer {
        private final Font font;
        private final Color fg;

        RowCellRenderer(Font font, Color fg) {
            this.font = font;
            this.fg = fg;
        }

        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object value,
                boolean isSel, boolean hasFocus, int row, int col) {
            super.getTableCellRendererComponent(tbl, value, isSel, hasFocus, row, col);
            setFont(font);
            setForeground(fg);
            setBorder(new EmptyBorder(0, 20, 0, 8));
            setBackground(getRowBg(isSel, row));
            return this;
        }
    }

    private class TrangThaiBadgeRenderer extends JPanel implements TableCellRenderer {
        private final JLabel badge = new JLabel();
        private Color badgeBg = OUTLINE;

        TrangThaiBadgeRenderer() {
            setLayout(new GridBagLayout());
            setOpaque(true);
            badge.setFont(FONT_BADGE);
            badge.setHorizontalAlignment(SwingConstants.CENTER);
            badge.setOpaque(false);
            add(badge);
        }

        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object value,
                boolean isSel, boolean hasFocus, int row, int col) {
            TrangThaiVe tt = (value instanceof TrangThaiVe) ? (TrangThaiVe) value : TrangThaiVe.DA_BAN;

            if (tt == TrangThaiVe.DA_BAN) {
                badgeBg = STATUS_GREEN_BG;
                badge.setForeground(STATUS_GREEN_FG);
                badge.setText("\u0110\u00E3 thanh to\u00E1n");
            } else {
                badgeBg = STATUS_GRAY_BG;
                badge.setForeground(STATUS_GRAY_FG);
                badge.setText("\u0110\u00E3 h\u1EE7y");
            }
            setBackground(getRowBg(isSel, row));
            return this;
        }

        @Override
        protected void paintChildren(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Rectangle r = badge.getBounds();
            int px = 10, py = 3;
            g2.setColor(badgeBg);
            g2.fillRoundRect(r.x - px, r.y - py, r.width + 2 * px, r.height + 2 * py, 20, 20);
            // dot
            Color dotColor = (badgeBg == STATUS_GREEN_BG) ? STATUS_GREEN_FG : STATUS_GRAY_FG;
            g2.setColor(dotColor);
            int dotX = r.x - px + 8;
            int dotY = r.y + r.height / 2 - 2;
            g2.fillOval(dotX, dotY, 5, 5);
            g2.dispose();
            super.paintChildren(g);
        }
    }

    /** Action buttons: Chi tiet + Hoan ve */
    private class ActionButtonRenderer extends JPanel implements TableCellRenderer {
        private final JLabel btnDetail = new JLabel("\u2315");
        private final JLabel btnRefund = new JLabel("\u21BA");

        ActionButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 6, 0));
            setOpaque(true);
            setupBtn(btnDetail, PRIMARY);
            setupBtn(btnRefund, PRIMARY);
            add(btnDetail);
            add(btnRefund);
        }

        private void setupBtn(JLabel lbl, Color color) {
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
            lbl.setForeground(color);
            lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            lbl.setPreferredSize(new Dimension(32, 32));
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object value,
                boolean isSel, boolean hasFocus, int row, int col) {
            setBackground(getRowBg(isSel, row));
            boolean isCancelled = (value instanceof TrangThaiVe tt && tt == TrangThaiVe.DA_HUY);
            btnRefund.setForeground(isCancelled ? new Color(0xC0, 0xC0, 0xC0) : PRIMARY);
            return this;
        }

        @Override
        protected void paintChildren(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // Draw rounded rect bg for each button
            for (Component c : getComponents()) {
                Rectangle r = c.getBounds();
                g2.setColor(PRIMARY_LIGHT);
                g2.fill(new RoundRectangle2D.Float(r.x, r.y + 2, r.width, r.height - 4, 8, 8));
            }
            g2.dispose();
            super.paintChildren(g);
        }
    }

    private class ActionButtonEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        private final JButton btnDetail = new JButton("\u2315");
        private final JButton btnRefund = new JButton("\u21BA");
        private int editingRow;

        ActionButtonEditor() {
            panel.setOpaque(true);

            for (JButton btn : new JButton[]{btnDetail, btnRefund}) {
                btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
                btn.setForeground(PRIMARY);
                btn.setBackground(PRIMARY_LIGHT);
                btn.setBorderPainted(false);
                btn.setFocusPainted(false);
                btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                btn.setPreferredSize(new Dimension(32, 32));
                panel.add(btn);
            }

            btnDetail.setToolTipText("Chi ti\u1EBFt");
            btnRefund.setToolTipText("Ho\u00E0n v\u00E9");

            btnDetail.addActionListener(e -> {
                fireEditingStopped();
                var veRow = tableModel.getRowAt(editingRow);
                if (veRow != null) {
                    Ve ve = veRow.ve();
                    String info = "M\u00E3 v\u00E9: " + ve.getMaVe()
                            + "\nH\u00E0nh kh\u00E1ch: " + veRow.tenKhachHang()
                            + "\nTuy\u1EBFn: " + veRow.gaDi() + " \u2192 " + veRow.gaDen()
                            + "\nKh\u1EDFi h\u00E0nh: " + veRow.khoiHanh()
                            + "\nTr\u1EA1ng th\u00E1i: " + ve.getTrangThai();
                    JOptionPane.showMessageDialog(QuanLyVeModule.this,
                            info, "Chi ti\u1EBFt v\u00E9", JOptionPane.INFORMATION_MESSAGE);
                }
            });

            btnRefund.addActionListener(e -> {
                fireEditingStopped();
                var veRow = tableModel.getRowAt(editingRow);
                if (veRow != null) {
                    Ve ve = veRow.ve();
                    if (ve.getTrangThai() == TrangThaiVe.DA_HUY) {
                        JOptionPane.showMessageDialog(QuanLyVeModule.this,
                                "V\u00E9 n\u00E0y \u0111\u00E3 \u0111\u01B0\u1EE3c h\u1EE7y tr\u01B0\u1EDBc \u0111\u00F3.",
                                "Th\u00F4ng b\u00E1o", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    int confirm = JOptionPane.showConfirmDialog(QuanLyVeModule.this,
                            "X\u00E1c nh\u1EADn ho\u00E0n v\u00E9 " + ve.getMaVe() + "?",
                            "Ho\u00E0n v\u00E9", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        String lyDo = JOptionPane.showInputDialog(QuanLyVeModule.this,
                                "L\u00FD do h\u1EE7y v\u00E9:", "L\u00FD do", JOptionPane.QUESTION_MESSAGE);
                        if (lyDo != null) {
                            boolean ok = new DAO_Ve().huyVe(ve.getMaVe(), lyDo);
                            if (ok) {
                                JOptionPane.showMessageDialog(QuanLyVeModule.this,
                                        "Ho\u00E0n v\u00E9 th\u00E0nh c\u00F4ng!", "Th\u00E0nh c\u00F4ng", JOptionPane.INFORMATION_MESSAGE);
                                loadData();
                            }
                        }
                    }
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable tbl, Object value,
                boolean isSel, int row, int col) {
            editingRow = row;
            panel.setBackground(PRIMARY_LIGHT);
            boolean isCancelled = (value instanceof TrangThaiVe tt && tt == TrangThaiVe.DA_HUY);
            btnRefund.setEnabled(!isCancelled);
            btnRefund.setForeground(isCancelled ? new Color(0xC0, 0xC0, 0xC0) : PRIMARY);
            return panel;
        }

        @Override
        public Object getCellEditorValue() { return null; }
    }

    private Color getRowBg(boolean isSel, int row) {
        if (isSel) return PRIMARY_LIGHT;
        if (row == hoveredRow) return ROW_HOVER;
        return row % 2 == 0 ? CARD_BG : ROW_ALT;
    }

    // =================================================================
    //  AppModule interface
    // =================================================================

    @Override public String getTitle() { return "Qu\u1EA3n l\u00FD v\u00E9"; }
    @Override public JPanel getView() { return this; }
    @Override public void setOnResult(Consumer<Object> cb) {
        this.callback = cb;
        boolean has = (cb != null);
        btnSubmit.setVisible(has);
        btnCancel.setVisible(has);
        btnPanel.setVisible(has);
    }
    @Override public void reset() {
        txtSearchHoaDon.setText("");
        txtSearchVe.setText("");
        activeTabIndex = 0;
        currentPage = 1;
        loadData();
    }
}
