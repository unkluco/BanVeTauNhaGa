package com.modules;

import com.dao.DAO_KhuyenMai;
import com.entity.KhuyenMai;
import com.toedter.calendar.JCalendar;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.CardLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

public class QuanLyKhuyenMaiModule extends JPanel implements AppModule {

    private Consumer<Object> callback;

    // --- UI components ---
    private JTextField        txtSearch;
    private JTextField        txtTuNgay;
    private JTextField        txtDenNgay;
    private JComboBox<String> cboTrangThai;
    private JButton           btnAddNew;
    private JTable            table;
    private KhuyenMaiTableModel tableModel;

    // --- Pagination ---
    private int currentPage  = 1;
    private int rowsPerPage  = 10;
    private int totalRecords = 0;

    private JLabel  lblPageInfo;
    private JPanel  paginationPanel;

    // --- Data ---
    private List<KhuyenMai> allData      = new ArrayList<>();
    private List<KhuyenMai> filteredData = new ArrayList<>();

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

    private static final Color STATUS_GREEN_BG = new Color(0xDC, 0xFA, 0xE6);
    private static final Color STATUS_GREEN_FG = new Color(0x16, 0x6B, 0x3A);
    private static final Color STATUS_RED_BG   = new Color(0xFE, 0xE2, 0xE2);
    private static final Color STATUS_RED_FG   = new Color(0xB9, 0x1C, 0x1C);

    private static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font FONT_DESC   = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_BODY   = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_BOLD   = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_MONO   = new Font("Consolas", Font.BOLD, 13);
    private static final Font FONT_BADGE  = new Font("Segoe UI", Font.BOLD, 11);
    private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 11);
    private static final Font FONT_SMALL  = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_BTN    = new Font("Segoe UI", Font.BOLD, 13);

    private static final DateTimeFormatter DT_FMT     = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DT_FMT_DIS = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // --- Hover tracking ---
    private int hoveredRow = -1;

    // --- Card layout ---
    private CardLayout rootCard;
    private JPanel     listView;
    private JPanel     editContainer;

    // --- AppModule hidden buttons ---
    private JButton btnSubmit;
    private JButton btnCancel;
    private JPanel  btnPanel;

    public QuanLyKhuyenMaiModule() {
        rootCard = new CardLayout();
        setLayout(rootCard);
        setBackground(SURFACE);

        btnSubmit = new JButton();
        btnCancel = new JButton();
        btnPanel  = new JPanel();
        btnPanel.setVisible(false);

        listView = new JPanel(new BorderLayout());
        listView.setBackground(SURFACE);
        listView.setBorder(new EmptyBorder(28, 36, 28, 36));

        editContainer = new JPanel(new BorderLayout());
        editContainer.setBackground(SURFACE);

        add(listView, "LIST");
        add(editContainer, "EDIT");

        buildUI();
        loadData();
        rootCard.show(this, "LIST");
    }

    // =================================================================
    //  BUILD UI
    // =================================================================

    private void buildUI() {
        listView.add(buildHeader(), BorderLayout.NORTH);
        listView.add(buildTableCard(), BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(0, 0));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 24, 0));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel lblTitle = new JLabel("Qu\u1EA3n l\u00FD Khuy\u1EBFn m\u00E3i");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(ON_SURFACE);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblDesc = new JLabel("Qu\u1EA3n l\u00FD c\u00E1c ch\u01B0\u01A1ng tr\u00ECnh khuy\u1EBFn m\u00E3i v\u00E0 chi ti\u1EBFt gi\u1EA3m gi\u00E1 theo tuy\u1EBFn.");
        lblDesc.setFont(FONT_DESC);
        lblDesc.setForeground(ON_SURF_VAR);
        lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        left.add(lblTitle);
        left.add(Box.createVerticalStrut(4));
        left.add(lblDesc);

        btnAddNew = createPrimaryButton("+ Th\u00EAm KM m\u1EDBi");
        btnAddNew.setPreferredSize(new Dimension(160, 40));
        btnAddNew.addActionListener(e -> openThemKhuyenMaiDialog());

        JPanel rightWrapper = new JPanel(new GridBagLayout());
        rightWrapper.setOpaque(false);
        rightWrapper.add(btnAddNew);

        header.add(left, BorderLayout.CENTER);
        header.add(rightWrapper, BorderLayout.EAST);

        return header;
    }

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

        card.add(buildFilterBar(), BorderLayout.NORTH);
        card.add(buildTableSection(), BorderLayout.CENTER);
        card.add(buildPaginationBar(), BorderLayout.SOUTH);

        return card;
    }

    private JPanel buildFilterBar() {
        JPanel bar = new JPanel();
        bar.setLayout(new BoxLayout(bar, BoxLayout.Y_AXIS));
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, OUTLINE),
                new EmptyBorder(14, 20, 14, 20)
        ));

        // Row 1: search field
        JPanel row1 = new JPanel(new BorderLayout());
        row1.setOpaque(false);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        txtSearch = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !hasFocus()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setColor(new Color(0x9E, 0xA7, 0xB0));
                    g2.setFont(FONT_BODY);
                    Insets insets = getInsets();
                    g2.drawString("T\u00ECm ki\u1EBFm theo m\u00E3 KM, t\u00EAn, m\u00F4 t\u1EA3...", insets.left, getHeight() / 2 + 5);
                    g2.dispose();
                }
            }
        };
        txtSearch.setFont(FONT_BODY);
        txtSearch.setPreferredSize(new Dimension(0, 38));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1),
                new EmptyBorder(6, 12, 6, 12)
        ));
        txtSearch.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) { txtSearch.repaint(); }
            public void focusLost(java.awt.event.FocusEvent e)   { txtSearch.repaint(); }
        });
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { applyFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { applyFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
        });

        row1.add(txtSearch, BorderLayout.CENTER);

        // Row 2: date range + trang thai + reset
        txtTuNgay  = createPlainDateField();
        txtDenNgay = createPlainDateField();

        cboTrangThai = createFilterCombo(new String[]{
                "T\u1EA5t c\u1EA3 tr\u1EA1ng th\u00E1i",
                "\u0110ang \u00E1p d\u1EE5ng",
                "Ng\u1EEBng \u00E1p d\u1EE5ng"
        });
        cboTrangThai.setPreferredSize(new Dimension(170, 38));
        cboTrangThai.setMaximumSize(new Dimension(170, 38));
        cboTrangThai.addActionListener(e -> applyFilter());

        JButton btnReset = new JButton("B\u1ECF l\u1ECDc") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? OUTLINE.darker() : OUTLINE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnReset.setFont(FONT_BODY);
        btnReset.setForeground(ON_SURF_VAR);
        btnReset.setContentAreaFilled(false);
        btnReset.setBorderPainted(false);
        btnReset.setFocusPainted(false);
        btnReset.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnReset.setPreferredSize(new Dimension(80, 38));
        btnReset.setMaximumSize(new Dimension(80, 38));
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            cboTrangThai.setSelectedIndex(0);
            txtTuNgay.setText("");
            txtDenNgay.setText("");
            applyFilter();
        });

        JPanel row2 = new JPanel();
        row2.setLayout(new BoxLayout(row2, BoxLayout.X_AXIS));
        row2.setOpaque(false);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        row2.add(createFilterLabel("T\u1EEB ng\u00E0y:"));
        row2.add(Box.createHorizontalStrut(6));
        row2.add(createDateInputPanel(txtTuNgay));
        row2.add(Box.createHorizontalStrut(16));
        row2.add(createFilterLabel("\u0110\u1EBFn ng\u00E0y:"));
        row2.add(Box.createHorizontalStrut(6));
        row2.add(createDateInputPanel(txtDenNgay));
        row2.add(Box.createHorizontalStrut(20));
        row2.add(createFilterLabel("Tr\u1EA1ng th\u00E1i:"));
        row2.add(Box.createHorizontalStrut(6));
        row2.add(cboTrangThai);
        row2.add(Box.createHorizontalStrut(12));
        row2.add(btnReset);
        row2.add(Box.createHorizontalGlue());

        bar.add(row1);
        bar.add(Box.createVerticalStrut(10));
        bar.add(row2);

        return bar;
    }

    private JTextField createPlainDateField() {
        JTextField f = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !hasFocus()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setColor(new Color(0x9E, 0xA7, 0xB0));
                    g2.setFont(FONT_SMALL);
                    Insets ins = getInsets();
                    g2.drawString("dd/MM/yyyy", ins.left + 2, getHeight() / 2 + 4);
                    g2.dispose();
                }
            }
        };
        f.setFont(FONT_BODY);
        f.setPreferredSize(new Dimension(120, 38));
        f.setMaximumSize(new Dimension(120, 38));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1),
                new EmptyBorder(4, 10, 4, 10)
        ));
        f.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) { f.repaint(); }
            public void focusLost(java.awt.event.FocusEvent e)   { f.repaint(); applyFilter(); }
        });
        return f;
    }

    private JPanel createDateInputPanel(JTextField txtField) {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(158, 38));
        panel.setPreferredSize(new Dimension(158, 38));

        JButton btnCal = new JButton("\u25BC") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isRollover() ? PRIMARY_LIGHT : new Color(0xF1, 0xF5, 0xF9);
                g2.setColor(bg);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(OUTLINE);
                g2.drawLine(0, 0, 0, getHeight() - 1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnCal.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        btnCal.setForeground(ON_SURF_VAR);
        btnCal.setContentAreaFilled(false);
        btnCal.setBorderPainted(false);
        btnCal.setFocusPainted(false);
        btnCal.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCal.setPreferredSize(new Dimension(30, 38));
        btnCal.addActionListener(e -> showDatePickerPopup(btnCal, txtField));

        panel.add(txtField, BorderLayout.CENTER);
        panel.add(btnCal, BorderLayout.EAST);
        return panel;
    }

    private void showDatePickerPopup(Component anchor, JTextField target) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog popup = new JDialog(owner, Dialog.ModalityType.MODELESS);
        popup.setUndecorated(true);
        popup.addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            @Override public void windowGainedFocus(java.awt.event.WindowEvent e) {}
            @Override public void windowLostFocus(java.awt.event.WindowEvent e)   { popup.dispose(); }
        });

        LocalDate existing = parseDateField(target);
        Calendar cal = Calendar.getInstance();
        if (existing != null) {
            cal.set(existing.getYear(), existing.getMonthValue() - 1, existing.getDayOfMonth());
        }

        JCalendar jCal = new JCalendar();
        jCal.setDate(cal.getTime());
        jCal.setWeekOfYearVisible(false);

        JButton btnOk = new JButton("Ch\u1ECDn") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? PRIMARY.darker() : PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnOk.setFont(FONT_BTN);
        btnOk.setForeground(Color.WHITE);
        btnOk.setContentAreaFilled(false);
        btnOk.setBorderPainted(false);
        btnOk.setFocusPainted(false);
        btnOk.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnOk.setPreferredSize(new Dimension(80, 32));

        JButton btnClear = new JButton("X\u00F3a") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(0xFE, 0xE2, 0xE2));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnClear.setFont(FONT_BTN);
        btnClear.setForeground(new Color(0xB9, 0x1C, 0x1C));
        btnClear.setContentAreaFilled(false);
        btnClear.setBorderPainted(false);
        btnClear.setFocusPainted(false);
        btnClear.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClear.setPreferredSize(new Dimension(60, 32));

        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        btnP.setBackground(Color.WHITE);
        btnP.add(btnClear);
        btnP.add(btnOk);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xC8, 0xD0, 0xDA), 1),
                new EmptyBorder(4, 4, 0, 4)
        ));
        root.add(jCal, BorderLayout.CENTER);
        root.add(btnP, BorderLayout.SOUTH);
        popup.setContentPane(root);

        btnOk.addActionListener(e -> {
            Date selectedDate = jCal.getDate();
            Calendar c = Calendar.getInstance();
            c.setTime(selectedDate);
            LocalDate ld = LocalDate.of(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
            target.setText(ld.format(DT_FMT));
            target.setForeground(ON_SURFACE);
            popup.dispose();
            applyFilter();
        });

        btnClear.addActionListener(e -> {
            target.setText("");
            popup.dispose();
            applyFilter();
        });

        popup.pack();
        try {
            Point anchorLoc = anchor.getLocationOnScreen();
            int px = anchorLoc.x;
            int py = anchorLoc.y + anchor.getHeight() + 2;
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            if (px + popup.getWidth()  > screen.width)  px = screen.width  - popup.getWidth();
            if (py + popup.getHeight() > screen.height) py = anchorLoc.y   - popup.getHeight() - 2;
            popup.setLocation(px, py);
        } catch (IllegalComponentStateException ex) {
            popup.setLocationRelativeTo(owner);
        }
        popup.setVisible(true);
    }

    private JScrollPane buildTableSection() {
        tableModel = new KhuyenMaiTableModel();
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

        table.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row != hoveredRow) {
                    hoveredRow = row;
                    table.repaint();
                }
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                hoveredRow = -1;
                table.repaint();
            }
        });

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
                return lbl;
            }
        });

        TableColumnModel colModel = table.getColumnModel();
        // Mã KM | Tên KM | Bắt đầu | Kết thúc | Trạng thái | Chi tiết
        int[] widths = {110, 260, 150, 150, 130, 100};
        for (int i = 0; i < widths.length; i++) {
            colModel.getColumn(i).setPreferredWidth(widths[i]);
        }

        colModel.getColumn(0).setCellRenderer(new RowCellRenderer(FONT_MONO, PRIMARY, SwingConstants.LEFT));
        colModel.getColumn(1).setCellRenderer(new RowCellRenderer(FONT_BOLD, ON_SURFACE, SwingConstants.LEFT));
        colModel.getColumn(2).setCellRenderer(new RowCellRenderer(FONT_BODY, ON_SURF_VAR, SwingConstants.LEFT));
        colModel.getColumn(3).setCellRenderer(new RowCellRenderer(FONT_BODY, ON_SURF_VAR, SwingConstants.LEFT));
        colModel.getColumn(4).setCellRenderer(new BadgeCellRenderer());
        colModel.getColumn(5).setCellRenderer(new ChiTietButtonRenderer());
        colModel.getColumn(5).setCellEditor(new ChiTietButtonEditor());

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(CARD_BG);
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setWheelScrollingEnabled(false);

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
    //  HELPER COMPONENTS
    // =================================================================

    private JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(PRIMARY.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(PRIMARY.brighter());
                } else {
                    g2.setColor(PRIMARY);
                }
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
        return btn;
    }

    private JComboBox<String> createFilterCombo(String[] items) {
        JComboBox<String> cbo = new JComboBox<>(items);
        cbo.setFont(FONT_BODY);
        return cbo;
    }

    private JLabel createFilterLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_HEADER);
        lbl.setForeground(ON_SURF_VAR);
        return lbl;
    }

    // =================================================================
    //  DATA
    // =================================================================

    private void openThemKhuyenMaiDialog() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        ThemKhuyenMaiDialog dlg = new ThemKhuyenMaiDialog(owner, this::loadData);
        dlg.setVisible(true);
    }

    void loadData() {
        SwingWorker<List<KhuyenMai>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<KhuyenMai> doInBackground() {
                return new DAO_KhuyenMai().getAll();
            }

            @Override
            protected void done() {
                try {
                    allData = get();
                } catch (Exception e) {
                    allData = new ArrayList<>();
                }
                applyFilter();
            }
        };
        worker.execute();
    }

    // =================================================================
    //  FILTER
    // =================================================================

    private void applyFilter() {
        String keyword    = txtSearch.getText().trim().toLowerCase();
        int trangThaiIdx  = cboTrangThai.getSelectedIndex();
        LocalDate tuNgay  = parseDateField(txtTuNgay);
        LocalDate denNgay = parseDateField(txtDenNgay);

        filteredData = new ArrayList<>();
        for (KhuyenMai km : allData) {
            boolean matchKw = keyword.isEmpty()
                    || km.getMaKhuyenMai().toLowerCase().contains(keyword)
                    || (km.getTenKhuyenMai() != null && km.getTenKhuyenMai().toLowerCase().contains(keyword))
                    || (km.getMoTa() != null && km.getMoTa().toLowerCase().contains(keyword));

            boolean matchTt = trangThaiIdx == 0
                    || (trangThaiIdx == 1 && km.isTrangThai())
                    || (trangThaiIdx == 2 && !km.isTrangThai());

            boolean matchDate = true;
            LocalDate batDau  = km.getThoiGianBatDau()  != null ? km.getThoiGianBatDau().toLocalDate()  : null;
            LocalDate ketThuc = km.getThoiGianKetThuc() != null ? km.getThoiGianKetThuc().toLocalDate() : null;

            if (tuNgay != null && denNgay != null) {
                matchDate = batDau != null && ketThuc != null
                        && !batDau.isBefore(tuNgay) && !ketThuc.isAfter(denNgay);
            } else if (tuNgay != null) {
                matchDate = batDau != null && !batDau.isBefore(tuNgay);
            } else if (denNgay != null) {
                matchDate = ketThuc != null && !ketThuc.isAfter(denNgay);
            }

            if (matchKw && matchTt && matchDate) {
                filteredData.add(km);
            }
        }

        totalRecords = filteredData.size();
        currentPage = 1;
        refreshTable();
    }

    private LocalDate parseDateField(JTextField f) {
        String text = f.getText().trim();
        if (text.isEmpty()) return null;
        try {
            return LocalDate.parse(text, DT_FMT);
        } catch (DateTimeParseException ex) {
            return null;
        }
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
                int screenH = Toolkit.getDefaultToolkit().getScreenSize().height;
                int rh = (table != null && table.getRowHeight() > 0) ? table.getRowHeight() : 56;
                rowsPerPage = Math.max(5, (screenH - 320) / rh);
            }

            int totalPages = Math.max(1, (int) Math.ceil((double) totalRecords / rowsPerPage));
            if (currentPage > totalPages) currentPage = totalPages;

            int start = (currentPage - 1) * rowsPerPage;
            int end   = Math.min(start + rowsPerPage, totalRecords);

            tableModel.setData(filteredData.subList(start, end));

            lblPageInfo.setText(totalRecords == 0
                    ? "Kh\u00F4ng t\u00ECm th\u1EA5y ch\u01B0\u01A1ng tr\u00ECnh khuy\u1EBFn m\u00E3i n\u00E0o"
                    : "Hi\u1EC3n th\u1ECB " + (start + 1) + " \u2013 " + end + " / " + totalRecords + " khuy\u1EBFn m\u00E3i");

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
    //  TABLE MODEL
    // =================================================================

    private class KhuyenMaiTableModel extends AbstractTableModel {
        private final String[] COLUMNS = {
            "M\u00E3 KM", "T\u00EAn ch\u01B0\u01A1ng tr\u00ECnh",
            "B\u1EAFt \u0111\u1EA7u", "K\u1EBFt th\u00FAc", "Tr\u1EA1ng th\u00E1i", ""
        };
        private List<KhuyenMai> data = new ArrayList<>();

        void setData(List<KhuyenMai> data) {
            this.data = new ArrayList<>(data);
            fireTableDataChanged();
        }

        @Override public int getRowCount()    { return data.size(); }
        @Override public int getColumnCount() { return COLUMNS.length; }
        @Override public String getColumnName(int c) { return COLUMNS[c]; }

        @Override
        public boolean isCellEditable(int r, int c) {
            return c == 5;
        }

        @Override
        public Object getValueAt(int r, int c) {
            KhuyenMai km = data.get(r);
            return switch (c) {
                case 0 -> km.getMaKhuyenMai();
                case 1 -> km.getTenKhuyenMai() != null ? km.getTenKhuyenMai() : "";
                case 2 -> km.getThoiGianBatDau()  != null ? km.getThoiGianBatDau().format(DT_FMT_DIS)  : "";
                case 3 -> km.getThoiGianKetThuc() != null ? km.getThoiGianKetThuc().format(DT_FMT_DIS) : "";
                case 4 -> km.isTrangThai();
                case 5 -> "Chi ti\u1EBFt";
                default -> "";
            };
        }

        KhuyenMai getKMAt(int r) {
            return (r >= 0 && r < data.size()) ? data.get(r) : null;
        }
    }

    // =================================================================
    //  CELL RENDERERS
    // =================================================================

    private class RowCellRenderer extends DefaultTableCellRenderer {
        private final Font  font;
        private final Color fg;

        RowCellRenderer(Font font, Color fg, int align) {
            this.font = font;
            this.fg   = fg;
            setHorizontalAlignment(align);
        }

        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object value,
                boolean isSel, boolean hasFocus, int row, int col) {
            super.getTableCellRendererComponent(tbl, value, isSel, hasFocus, row, col);
            setFont(font);
            setForeground(fg);
            setBorder(new EmptyBorder(0, 20, 0, 8));
            setBackground(getRowBg(tbl, isSel, row));
            return this;
        }
    }

    private class BadgeCellRenderer extends JPanel implements TableCellRenderer {
        private final JLabel badge = new JLabel();
        private Color badgeBg = OUTLINE;
        private Color badgeFg = ON_SURF_VAR;

        BadgeCellRenderer() {
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
            boolean active = value instanceof Boolean b && b;
            if (active) {
                badgeBg = STATUS_GREEN_BG;
                badgeFg = STATUS_GREEN_FG;
                badge.setText("\u0110ang \u00E1p d\u1EE5ng");
            } else {
                badgeBg = STATUS_RED_BG;
                badgeFg = STATUS_RED_FG;
                badge.setText("Ng\u1EEBng \u00E1p d\u1EE5ng");
            }
            badge.setForeground(badgeFg);
            setBackground(getRowBg(tbl, isSel, row));
            return this;
        }

        @Override
        protected void paintChildren(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Rectangle r = badge.getBounds();
            int px = 10, py = 3;
            g2.setColor(badgeBg);
            g2.fillRoundRect(r.x - px, r.y - py, r.width + 2 * px, r.height + 2 * py, 14, 14);
            g2.dispose();
            super.paintChildren(g);
        }
    }

    private class ChiTietButtonRenderer extends JPanel implements TableCellRenderer {
        private final JLabel lbl = new JLabel();

        ChiTietButtonRenderer() {
            setLayout(new GridBagLayout());
            setOpaque(true);
            lbl.setFont(FONT_BADGE);
            lbl.setForeground(PRIMARY);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setOpaque(false);
            lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            add(lbl);
        }

        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object value,
                boolean isSel, boolean hasFocus, int row, int col) {
            lbl.setText(value != null ? value.toString() : "Chi ti\u1EBFt");
            setBackground(getRowBg(tbl, isSel, row));
            return this;
        }

        @Override
        protected void paintChildren(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Rectangle r = lbl.getBounds();
            int px = 12, py = 4;
            g2.setColor(PRIMARY_LIGHT);
            g2.fillRoundRect(r.x - px, r.y - py, r.width + 2 * px, r.height + 2 * py, 10, 10);
            g2.setColor(new Color(PRIMARY.getRed(), PRIMARY.getGreen(), PRIMARY.getBlue(), 60));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(r.x - px, r.y - py, r.width + 2 * px, r.height + 2 * py, 10, 10);
            g2.dispose();
            super.paintChildren(g);
        }
    }

    private class ChiTietButtonEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel  panel;
        private final JButton button;
        private int editingRow;

        ChiTietButtonEditor() {
            panel  = new JPanel(new GridBagLayout());
            panel.setOpaque(true);

            button = new JButton("Chi ti\u1EBFt");
            button.setFont(FONT_BADGE);
            button.setForeground(PRIMARY);
            button.setBackground(PRIMARY_LIGHT);
            button.setBorderPainted(false);
            button.setFocusPainted(false);
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            button.setPreferredSize(new Dimension(80, 28));

            button.addActionListener(e -> {
                fireEditingStopped();
                KhuyenMai km = tableModel.getKMAt(editingRow);
                if (km != null) onOpenChiTiet(km);
            });

            panel.add(button);
        }

        @Override
        public Component getTableCellEditorComponent(JTable tbl, Object value,
                boolean isSel, int row, int col) {
            editingRow = row;
            panel.setBackground(getRowBg(tbl, true, row));
            return panel;
        }

        @Override
        public boolean isCellEditable(java.util.EventObject e) {
            if (e instanceof MouseEvent me) {
                JTable tbl = (JTable) me.getSource();
                int col = tbl.columnAtPoint(me.getPoint());
                int row = tbl.rowAtPoint(me.getPoint());
                if (col < 0 || row < 0) return false;
                Rectangle cellRect = tbl.getCellRect(row, col, false);
                int clickX = me.getX() - cellRect.x;
                int clickY = me.getY() - cellRect.y;
                int btnW = button.getPreferredSize().width;
                int btnH = button.getPreferredSize().height;
                int btnX = (cellRect.width  - btnW) / 2;
                int btnY = (cellRect.height - btnH) / 2;
                return clickX >= btnX && clickX <= btnX + btnW
                    && clickY >= btnY && clickY <= btnY + btnH;
            }
            return false;
        }

        @Override
        public Object getCellEditorValue() { return "Chi ti\u1EBFt"; }
    }

    private Color getRowBg(JTable tbl, boolean isSel, int row) {
        if (isSel) return PRIMARY_LIGHT;
        if (row == hoveredRow) return ROW_HOVER;
        return row % 2 == 0 ? CARD_BG : ROW_ALT;
    }

    // =================================================================
    //  OPEN DETAIL
    // =================================================================

    private void onOpenChiTiet(KhuyenMai km) {
        editContainer.removeAll();
        ChinhSuaKhuyenMaiModule detailModule = new ChinhSuaKhuyenMaiModule(km, () -> {
            rootCard.show(QuanLyKhuyenMaiModule.this, "LIST");
            loadData();
        });
        editContainer.add(detailModule, BorderLayout.CENTER);
        editContainer.revalidate();
        editContainer.repaint();
        rootCard.show(this, "EDIT");
    }

    // =================================================================
    //  AppModule interface
    // =================================================================

    @Override public String getTitle() { return "Qu\u1EA3n l\u00FD khuy\u1EBFn m\u00E3i"; }
    @Override public JPanel getView()  { return this; }

    @Override
    public void setOnResult(Consumer<Object> cb) {
        this.callback = cb;
    }

    @Override
    public void reset() {
        rootCard.show(this, "LIST");
        txtSearch.setText("");
        cboTrangThai.setSelectedIndex(0);
        txtTuNgay.setText("");
        txtDenNgay.setText("");
        loadData();
    }
}
