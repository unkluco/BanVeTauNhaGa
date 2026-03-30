package com.modules;

import com.dao.DAO_ChiTietGia;
import com.entity.ChiTietGia;
import com.entity.Gia;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChinhSuaGiaModule extends JPanel {

    // ===== Design tokens =====
    private static final Color PRIMARY       = new Color(0x00, 0x5D, 0x90);
    private static final Color PRIMARY_LIGHT = new Color(0xE3, 0xF2, 0xFD);
    private static final Color PRIMARY_FIXED = new Color(0xCD, 0xE5, 0xFF);
    private static final Color SURFACE       = new Color(0xF8, 0xFA, 0xFC);
    private static final Color CARD_BG       = Color.WHITE;
    private static final Color INFO_CARD_BG  = new Color(0xF2, 0xF4, 0xF6);
    private static final Color ON_SURFACE    = new Color(0x19, 0x1C, 0x1E);
    private static final Color ON_SURF_VAR   = new Color(0x40, 0x48, 0x50);
    private static final Color OUTLINE       = new Color(0xDE, 0xE3, 0xE8);
    private static final Color OUTLINE_VAR   = new Color(0xBF, 0xC7, 0xD1);
    private static final Color ROW_ALT       = new Color(0xF8, 0xFA, 0xFC);
    private static final Color ROW_HOVER     = new Color(0xEE, 0xF5, 0xFB);
    private static final Color ERROR_BG      = new Color(0xFF, 0xDA, 0xD6);
    private static final Color ERROR_FG      = new Color(0xB9, 0x1C, 0x1C);
    private static final Color BADGE_BG      = new Color(0xEC, 0xEE, 0xF0);

    private static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font FONT_BODY   = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_BOLD   = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_MONO   = new Font("Consolas", Font.BOLD, 13);
    private static final Font FONT_BADGE  = new Font("Segoe UI", Font.BOLD, 11);
    private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 11);
    private static final Font FONT_SMALL  = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_LABEL  = new Font("Segoe UI", Font.BOLD, 10);
    private static final Font FONT_BTN    = new Font("Segoe UI", Font.BOLD, 13);

    private static final DateTimeFormatter DT_FMT        = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat      CURRENCY_FMT  = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    // ===== State =====
    private final Gia      gia;
    private final Runnable onBack;

    private JTable               table;
    private ChiTietGiaTableModel tableModel;
    private int currentPage  = 1;
    private int rowsPerPage  = 10;
    private int totalRecords = 0;
    private int hoveredRow   = -1;
    private boolean isRefreshing = false;

    private JLabel lblPageInfo;
    private JPanel paginationPanel;
    private List<ChiTietGia> allData      = new ArrayList<>();
    private List<ChiTietGia> filteredData = new ArrayList<>();

    // Filter UI
    private JTextField        txtFilterSearch;
    private JComboBox<String> cboFilterLoaiGhe;

    // Info-card live labels (refreshed after editing Gia)
    private JLabel lblInfoMoTa;
    private JLabel lblInfoBatDau;
    private JLabel lblInfoKetThuc;

    // ===========================
    public ChinhSuaGiaModule(Gia gia, Runnable onBack) {
        this.gia    = gia;
        this.onBack = onBack;
        setLayout(new BorderLayout());
        setBackground(SURFACE);
        setBorder(new EmptyBorder(28, 36, 28, 36));
        buildUI();
        loadData();
    }

    // =================================================================
    //  BUILD UI
    // =================================================================

    private void buildUI() {
        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setOpaque(false);
        topSection.add(buildPageHeader());
        topSection.add(Box.createVerticalStrut(20));
        topSection.add(buildInfoCard());
        topSection.add(Box.createVerticalStrut(20));

        add(topSection, BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);
    }

    // ---------- Page header ----------

    private JPanel buildPageHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.setBorder(new EmptyBorder(0, 0, 4, 0));

        JPanel leftSection = new JPanel();
        leftSection.setLayout(new BoxLayout(leftSection, BoxLayout.Y_AXIS));
        leftSection.setOpaque(false);

        // Back link
        JLabel lblBack = new JLabel("\u2190 Quay l\u1EA1i danh s\u00E1ch");
        lblBack.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblBack.setForeground(PRIMARY);
        lblBack.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblBack.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblBack.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { if (onBack != null) onBack.run(); }
            @Override public void mouseEntered(MouseEvent e) {
                lblBack.setText("<html><u>\u2190 Quay l\u1EA1i danh s\u00E1ch</u></html>");
            }
            @Override public void mouseExited(MouseEvent e)  {
                lblBack.setText("\u2190 Quay l\u1EA1i danh s\u00E1ch");
            }
        });

        JLabel lblTitle = new JLabel("Chi ti\u1EBFt bi\u1EC3u gi\u00E1");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(ON_SURFACE);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftSection.add(lblBack);
        leftSection.add(Box.createVerticalStrut(6));
        leftSection.add(lblTitle);

        JButton btnAddDetail = createPrimaryButton("+ Th\u00EAm chi ti\u1EBFt gi\u00E1 m\u1EDBi");
        btnAddDetail.setPreferredSize(new Dimension(210, 40));
        btnAddDetail.addActionListener(e -> openChinhSuaChiTietDialog(null));

        JPanel rightWrapper = new JPanel(new GridBagLayout());
        rightWrapper.setOpaque(false);
        rightWrapper.add(btnAddDetail);

        header.add(leftSection, BorderLayout.CENTER);
        header.add(rightWrapper, BorderLayout.EAST);

        return header;
    }

    // ---------- Info card ----------

    private JPanel buildInfoCard() {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(INFO_CARD_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(OUTLINE_VAR);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 16, 16));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        card.setBorder(new EmptyBorder(18, 22, 18, 22));

        // Header row: icon + title + edit button
        JPanel cardHeader = new JPanel(new BorderLayout());
        cardHeader.setOpaque(false);
        cardHeader.setBorder(new EmptyBorder(0, 0, 14, 0));

        JPanel iconTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        iconTitle.setOpaque(false);

        JPanel icon = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(PRIMARY_FIXED);
                g2.fillRoundRect(0, 0, 38, 38, 10, 10);
                g2.setColor(PRIMARY);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("i", (38 - fm.stringWidth("i")) / 2, 26);
                g2.dispose();
            }
        };
        icon.setOpaque(false);
        icon.setPreferredSize(new Dimension(38, 38));

        JLabel lblCardTitle = new JLabel("Th\u00F4ng tin bi\u1EC3u gi\u00E1");
        lblCardTitle.setFont(FONT_BOLD);
        lblCardTitle.setForeground(ON_SURFACE);

        iconTitle.add(icon);
        iconTitle.add(lblCardTitle);

        JButton btnEdit = new JButton("\u270E Ch\u1EC9nh s\u1EEDa");
        btnEdit.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnEdit.setForeground(PRIMARY);
        btnEdit.setContentAreaFilled(false);
        btnEdit.setBorderPainted(false);
        btnEdit.setFocusPainted(false);
        btnEdit.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnEdit.addActionListener(e -> openSuaThongTinGia());

        cardHeader.add(iconTitle, BorderLayout.WEST);
        cardHeader.add(btnEdit, BorderLayout.EAST);

        // Fields row: 4 columns
        JPanel fields = new JPanel(new GridLayout(1, 4, 24, 0));
        fields.setOpaque(false);

        fields.add(buildInfoField("M\u00C3 GI\u00C1", gia.getMaGia(), FONT_MONO, PRIMARY));

        lblInfoMoTa = new JLabel(gia.getMoTa() != null ? gia.getMoTa() : "\u2014");
        lblInfoMoTa.setFont(FONT_BOLD);
        lblInfoMoTa.setForeground(ON_SURFACE);
        fields.add(buildInfoFieldWithLabel("M\u00D4 T\u1EA2", lblInfoMoTa));

        String batDauStr = gia.getThoiGianBatDau() != null ? gia.getThoiGianBatDau().format(DT_FMT) : "\u2014";
        lblInfoBatDau = new JLabel(batDauStr);
        lblInfoBatDau.setFont(FONT_BOLD);
        lblInfoBatDau.setForeground(ON_SURFACE);
        fields.add(buildInfoFieldWithLabel("NG\u00C0Y \u00C1P D\u1EE4NG", lblInfoBatDau));

        String ketThucStr = gia.getThoiGianKetThuc() != null ? gia.getThoiGianKetThuc().format(DT_FMT) : "\u2014";
        lblInfoKetThuc = new JLabel(ketThucStr);
        lblInfoKetThuc.setFont(FONT_BOLD);
        lblInfoKetThuc.setForeground(ON_SURFACE);
        fields.add(buildInfoFieldWithLabel("NG\u00C0Y K\u1EBCT TH\u00DAC", lblInfoKetThuc));

        card.add(cardHeader, BorderLayout.NORTH);
        card.add(fields, BorderLayout.CENTER);

        return card;
    }

    private JPanel buildInfoField(String labelText, String value, Font valueFont, Color valueFg) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(ON_SURF_VAR);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel val = new JLabel(value);
        val.setFont(valueFont);
        val.setForeground(valueFg);
        val.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(lbl);
        p.add(Box.createVerticalStrut(4));
        p.add(val);
        return p;
    }

    private JPanel buildInfoFieldWithLabel(String labelText, JLabel valueLabel) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(ON_SURF_VAR);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(lbl);
        p.add(Box.createVerticalStrut(4));
        p.add(valueLabel);
        return p;
    }

    // ---------- Table card ----------

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

        card.add(buildTableFilterBar(), BorderLayout.NORTH);
        card.add(buildTableSection(), BorderLayout.CENTER);
        card.add(buildPaginationBar(), BorderLayout.SOUTH);

        return card;
    }

    private JPanel buildTableFilterBar() {
        JPanel bar = new JPanel();
        bar.setLayout(new BoxLayout(bar, BoxLayout.Y_AXIS));
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, OUTLINE),
                new EmptyBorder(14, 20, 14, 20)
        ));

        // Row 1: title + search
        JPanel row1 = new JPanel(new BorderLayout(12, 0));
        row1.setOpaque(false);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JLabel lblTableTitle = new JLabel("Danh s\u00E1ch chi ti\u1EBFt gi\u00E1");
        lblTableTitle.setFont(FONT_BOLD);
        lblTableTitle.setForeground(ON_SURFACE);

        txtFilterSearch = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !hasFocus()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setColor(new Color(0x9E, 0xA7, 0xB0));
                    g2.setFont(FONT_BODY);
                    Insets ins = getInsets();
                    g2.drawString("T\u00ECm theo m\u00E3 chi ti\u1EBFt, tuy\u1EBFn...", ins.left, getHeight() / 2 + 5);
                    g2.dispose();
                }
            }
        };
        txtFilterSearch.setFont(FONT_BODY);
        txtFilterSearch.setPreferredSize(new Dimension(260, 34));
        txtFilterSearch.setMaximumSize(new Dimension(260, 34));
        txtFilterSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1),
                new EmptyBorder(4, 10, 4, 10)
        ));
        txtFilterSearch.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) { txtFilterSearch.repaint(); }
            public void focusLost(java.awt.event.FocusEvent e)   { txtFilterSearch.repaint(); }
        });
        txtFilterSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { applyFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { applyFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
        });

        row1.add(lblTableTitle, BorderLayout.WEST);
        row1.add(txtFilterSearch, BorderLayout.EAST);

        // Row 2: loai ghe filter
        JPanel row2 = new JPanel();
        row2.setLayout(new BoxLayout(row2, BoxLayout.X_AXIS));
        row2.setOpaque(false);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        JLabel lblLg = new JLabel("Lo\u1EA1i gh\u1EBF:");
        lblLg.setFont(FONT_HEADER);
        lblLg.setForeground(ON_SURF_VAR);

        cboFilterLoaiGhe = new JComboBox<>(new String[]{
            "T\u1EA5t c\u1EA3", "Gh\u1EBF c\u1EE9ng", "Gh\u1EBF m\u1EC1m", "Gi\u01B0\u1EDDng n\u1EB1m"
        });
        cboFilterLoaiGhe.setFont(FONT_BODY);
        cboFilterLoaiGhe.setPreferredSize(new Dimension(160, 34));
        cboFilterLoaiGhe.setMaximumSize(new Dimension(160, 34));
        cboFilterLoaiGhe.addActionListener(e -> applyFilter());

        row2.add(lblLg);
        row2.add(Box.createHorizontalStrut(8));
        row2.add(cboFilterLoaiGhe);
        row2.add(Box.createHorizontalGlue());

        bar.add(row1);
        bar.add(Box.createVerticalStrut(10));
        bar.add(row2);
        return bar;
    }

    private void applyFilter() {
        String kw  = txtFilterSearch != null ? txtFilterSearch.getText().trim().toLowerCase() : "";
        int lgIdx  = cboFilterLoaiGhe != null ? cboFilterLoaiGhe.getSelectedIndex() : 0;

        filteredData = new ArrayList<>();
        for (ChiTietGia ct : allData) {
            boolean matchKw = kw.isEmpty()
                    || ct.getMaChiTietGia().toLowerCase().contains(kw)
                    || (ct.getTuyen() != null && ct.getTuyen().getMaTuyen().toLowerCase().contains(kw))
                    || (ct.getTuyen() != null && ct.getTuyen().getGaDi()  != null
                        && ct.getTuyen().getGaDi().getTenGa().toLowerCase().contains(kw))
                    || (ct.getTuyen() != null && ct.getTuyen().getGaDen() != null
                        && ct.getTuyen().getGaDen().getTenGa().toLowerCase().contains(kw));

            boolean matchLg = lgIdx == 0
                    || (lgIdx == 1 && ct.getLoaiGhe() != null && ct.getLoaiGhe().toString().equals("Gh\u1EBF c\u1EE9ng"))
                    || (lgIdx == 2 && ct.getLoaiGhe() != null && ct.getLoaiGhe().toString().equals("Gh\u1EBF m\u1EC1m"))
                    || (lgIdx == 3 && ct.getLoaiGhe() != null && ct.getLoaiGhe().toString().equals("Gi\u01B0\u1EDDng n\u1EB1m"));

            if (matchKw && matchLg) filteredData.add(ct);
        }
        currentPage = 1;
        refreshTable();
    }

    private JScrollPane buildTableSection() {
        tableModel = new ChiTietGiaTableModel();
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
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

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

        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                    boolean isSel, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(tbl, value, isSel, hasFocus, row, col);
                lbl.setFont(FONT_HEADER);
                lbl.setForeground(ON_SURF_VAR);
                lbl.setBackground(new Color(0xF8, 0xFA, 0xFC));
                lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, OUTLINE),
                        new EmptyBorder(0, 20, 0, 8)
                ));
                lbl.setPreferredSize(new Dimension(lbl.getPreferredSize().width, 40));
                return lbl;
            }
        });

        TableColumnModel cols = table.getColumnModel();
        int[] widths = {120, 260, 140, 170, 120};
        for (int i = 0; i < widths.length; i++) cols.getColumn(i).setPreferredWidth(widths[i]);

        cols.getColumn(0).setCellRenderer(new MaCellRenderer());
        cols.getColumn(1).setCellRenderer(new TextCellRenderer(FONT_BODY, ON_SURFACE));
        cols.getColumn(2).setCellRenderer(new LoaiGheBadgeRenderer());
        cols.getColumn(3).setCellRenderer(new PriceCellRenderer());
        cols.getColumn(4).setCellRenderer(new ActionRenderer());
        cols.getColumn(4).setCellEditor(new ActionEditor());

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
                if (newRows > 0 && newRows != rowsPerPage && !isRefreshing) {
                    rowsPerPage = newRows;
                    refreshTable();
                }
            }
        });

        return sp;
    }

    private int calcRowsFromViewport() {
        if (table == null || !(table.getParent() instanceof JViewport vp)) return 0;
        int viewH   = vp.getHeight();
        int rh      = table.getRowHeight() > 0 ? table.getRowHeight() : 52;
        int headerH = table.getTableHeader().getHeight();
        if (headerH <= 0) headerH = 40;
        int avail = viewH - headerH;
        return avail > 0 ? Math.max(1, avail / rh + 1) : 0;
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
    //  DATA
    // =================================================================

    void loadData() {
        new SwingWorker<List<ChiTietGia>, Void>() {
            @Override
            protected List<ChiTietGia> doInBackground() {
                return new DAO_ChiTietGia().findByGia(gia.getMaGia());
            }
            @Override
            protected void done() {
                try { allData = get(); } catch (Exception ex) { allData = new ArrayList<>(); }
                applyFilter();
            }
        }.execute();
    }

    private void refreshTable() {
        isRefreshing = true;
        try {
            int vpRows = calcRowsFromViewport();
            if (vpRows > 0) rowsPerPage = vpRows;
            totalRecords = filteredData.size();
            int totalPages = Math.max(1, (int) Math.ceil((double) totalRecords / rowsPerPage));
            if (currentPage > totalPages) currentPage = totalPages;
            int start = (currentPage - 1) * rowsPerPage;
            int end   = Math.min(start + rowsPerPage, totalRecords);
            tableModel.setData(filteredData.subList(start, end));
            lblPageInfo.setText(totalRecords == 0
                    ? "Kh\u00F4ng t\u00ECm th\u1EA5y chi ti\u1EBFt n\u00E0o"
                    : "Hi\u1EC3n th\u1ECB " + (start + 1) + " \u2013 " + end + " / " + totalRecords + " b\u1EA3n ghi");
            rebuildPagination(totalPages);
        } finally {
            isRefreshing = false;
        }
    }

    private void rebuildPagination(int totalPages) {
        paginationPanel.removeAll();
        addNavBtn("\u276E", currentPage > 1, () -> { currentPage--; refreshTable(); });
        for (int i = 1; i <= totalPages; i++) {
            final int pg = i;
            JButton btn = new JButton(String.valueOf(pg)) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (pg == currentPage) {
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
            btn.setForeground(pg == currentPage ? Color.WHITE : ON_SURF_VAR);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> { currentPage = pg; refreshTable(); });
            paginationPanel.add(btn);
        }
        addNavBtn("\u276F", currentPage < totalPages, () -> { currentPage++; refreshTable(); });
        paginationPanel.revalidate();
        paginationPanel.repaint();
    }

    private void addNavBtn(String sym, boolean enabled, Runnable action) {
        JButton btn = new JButton(sym);
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
    //  ACTIONS
    // =================================================================

    private void openSuaThongTinGia() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        SuaGiaDialog dlg = new SuaGiaDialog(owner, gia, () -> {
            lblInfoMoTa.setText(gia.getMoTa() != null ? gia.getMoTa() : "\u2014");
            lblInfoBatDau.setText(gia.getThoiGianBatDau() != null ? gia.getThoiGianBatDau().format(DT_FMT) : "\u2014");
            lblInfoKetThuc.setText(gia.getThoiGianKetThuc() != null ? gia.getThoiGianKetThuc().format(DT_FMT) : "\u2014");
            repaint();
        });
        dlg.setVisible(true);
    }

    private void openChinhSuaChiTietDialog(ChiTietGia ctg) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        ChinhSuaChiTietGiaDialog dlg = new ChinhSuaChiTietGiaDialog(owner, gia, ctg, this::loadData);
        dlg.setVisible(true);
    }

    private void confirmDelete(ChiTietGia ctg) {
        int res = JOptionPane.showConfirmDialog(this,
                "X\u00F3a chi ti\u1EBFt gi\u00E1 \"" + ctg.getMaChiTietGia() + "\"?",
                "X\u00E1c nh\u1EADn x\u00F3a",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (res != JOptionPane.YES_OPTION) return;
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() {
                return new DAO_ChiTietGia().delete(ctg.getMaChiTietGia());
            }
            @Override protected void done() {
                try {
                    if (get()) loadData();
                    else JOptionPane.showMessageDialog(ChinhSuaGiaModule.this,
                            "Kh\u00F4ng th\u1EC3 x\u00F3a chi ti\u1EBFt gi\u00E1!", "L\u1ED7i", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ChinhSuaGiaModule.this,
                            "L\u1ED7i: " + ex.getMessage(), "L\u1ED7i", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // =================================================================
    //  HELPERS
    // =================================================================

    private JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = getModel().isPressed() ? PRIMARY.darker()
                        : getModel().isRollover() ? PRIMARY.brighter() : PRIMARY;
                g2.setColor(c);
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

    private Color getRowBg(JTable tbl, boolean isSel, int row) {
        if (isSel) return PRIMARY_LIGHT;
        if (row == hoveredRow) return ROW_HOVER;
        return row % 2 == 0 ? CARD_BG : ROW_ALT;
    }

    // =================================================================
    //  TABLE MODEL
    // =================================================================

    private class ChiTietGiaTableModel extends AbstractTableModel {
        private final String[] COLS = {
            "M\u00E3 chi ti\u1EBFt", "Tuy\u1EBFn",
            "Lo\u1EA1i gh\u1EBF", "Gi\u00E1 ni\u00EAm y\u1EBFt (VN\u0110)", ""
        };
        private List<ChiTietGia> data = new ArrayList<>();

        void setData(List<ChiTietGia> d) { this.data = new ArrayList<>(d); fireTableDataChanged(); }

        @Override public int getRowCount()    { return data.size(); }
        @Override public int getColumnCount() { return COLS.length; }
        @Override public String getColumnName(int c) { return COLS[c]; }
        @Override public boolean isCellEditable(int r, int c) { return c == 4; }

        @Override
        public Object getValueAt(int r, int c) {
            ChiTietGia ct = data.get(r);
            return switch (c) {
                case 0 -> ct.getMaChiTietGia();
                case 1 -> {
                    if (ct.getTuyen() == null) yield "\u2014";
                    String gaDi  = ct.getTuyen().getGaDi()  != null ? ct.getTuyen().getGaDi().getTenGa()  : "?";
                    String gaDen = ct.getTuyen().getGaDen() != null ? ct.getTuyen().getGaDen().getTenGa() : "?";
                    yield gaDi + " \u2192 " + gaDen + " (" + ct.getTuyen().getMaTuyen() + ")";
                }
                case 2 -> ct.getLoaiGhe() != null ? ct.getLoaiGhe().toString() : "\u2014";
                case 3 -> ct.getGiaNiemYet();
                case 4 -> ct;
                default -> "";
            };
        }

        ChiTietGia getAt(int r) { return (r >= 0 && r < data.size()) ? data.get(r) : null; }
    }

    // =================================================================
    //  RENDERERS & EDITORS
    // =================================================================

    private class MaCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object value,
                boolean isSel, boolean hasFocus, int row, int col) {
            super.getTableCellRendererComponent(tbl, value, isSel, hasFocus, row, col);
            setFont(FONT_MONO); setForeground(PRIMARY);
            setBorder(new EmptyBorder(0, 20, 0, 8));
            setBackground(getRowBg(tbl, isSel, row));
            return this;
        }
    }

    private class TextCellRenderer extends DefaultTableCellRenderer {
        private final Font f; private final Color fg;
        TextCellRenderer(Font f, Color fg) { this.f = f; this.fg = fg; }
        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object value,
                boolean isSel, boolean hasFocus, int row, int col) {
            super.getTableCellRendererComponent(tbl, value, isSel, hasFocus, row, col);
            setFont(f); setForeground(fg);
            setBorder(new EmptyBorder(0, 20, 0, 8));
            setBackground(getRowBg(tbl, isSel, row));
            return this;
        }
    }

    private class PriceCellRenderer extends DefaultTableCellRenderer {
        PriceCellRenderer() { setHorizontalAlignment(SwingConstants.LEFT); }
        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object value,
                boolean isSel, boolean hasFocus, int row, int col) {
            String text = (value instanceof Double d) ? CURRENCY_FMT.format(d) + " \u0111" : "\u2014";
            super.getTableCellRendererComponent(tbl, text, isSel, hasFocus, row, col);
            setFont(FONT_BOLD); setForeground(ON_SURFACE);
            setBorder(new EmptyBorder(0, 20, 0, 8));
            setBackground(getRowBg(tbl, isSel, row));
            return this;
        }
    }

    private class LoaiGheBadgeRenderer extends JPanel implements TableCellRenderer {
        private final JLabel badge = new JLabel();
        private Color badgeBg = BADGE_BG;
        private Color badgeFg = ON_SURF_VAR;

        // Color palettes per seat type
        private static final Color GHE_CUNG_BG  = new Color(0xDB, 0xEA, 0xFE);
        private static final Color GHE_CUNG_FG  = new Color(0x1D, 0x4E, 0xD8);
        private static final Color GHE_MEM_BG   = new Color(0xDC, 0xFC, 0xE7);
        private static final Color GHE_MEM_FG   = new Color(0x15, 0x80, 0x3D);
        private static final Color GIUONG_BG    = new Color(0xED, 0xE9, 0xFE);
        private static final Color GIUONG_FG    = new Color(0x6D, 0x28, 0xD9);

        LoaiGheBadgeRenderer() {
            setLayout(new GridBagLayout()); setOpaque(true);
            badge.setFont(FONT_BADGE);
            badge.setHorizontalAlignment(SwingConstants.CENTER);
            badge.setOpaque(false);
            add(badge);
        }
        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object value,
                boolean isSel, boolean hasFocus, int row, int col) {
            String text = value != null ? value.toString() : "\u2014";
            badge.setText(text);
            if (text.equals("Gh\u1EBF c\u1EE9ng")) {
                badgeBg = GHE_CUNG_BG; badgeFg = GHE_CUNG_FG;
            } else if (text.equals("Gh\u1EBF m\u1EC1m")) {
                badgeBg = GHE_MEM_BG;  badgeFg = GHE_MEM_FG;
            } else if (text.equals("Gi\u01B0\u1EDDng n\u1EB1m")) {
                badgeBg = GIUONG_BG;   badgeFg = GIUONG_FG;
            } else {
                badgeBg = BADGE_BG;    badgeFg = ON_SURF_VAR;
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
            g2.setColor(badgeBg);
            g2.fillRoundRect(r.x - 10, r.y - 3, r.width + 20, r.height + 6, 14, 14);
            g2.dispose();
            super.paintChildren(g);
        }
    }

    private class ActionRenderer extends JPanel implements TableCellRenderer {
        private final JLabel editLbl = new JLabel("S\u1EEDa");
        private final JLabel delLbl  = new JLabel("X\u00F3a");
        ActionRenderer() {
            setLayout(new GridBagLayout()); setOpaque(true);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(0, 5, 0, 5);
            configLabel(editLbl, PRIMARY);
            configLabel(delLbl,  ERROR_FG);
            add(editLbl, gbc);
            add(delLbl,  gbc);
        }
        private void configLabel(JLabel lbl, Color fg) {
            lbl.setFont(FONT_BADGE);
            lbl.setForeground(fg);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setPreferredSize(new Dimension(52, 28));
            lbl.setOpaque(false);
        }
        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object value,
                boolean isSel, boolean hasFocus, int row, int col) {
            setBackground(getRowBg(tbl, isSel, row)); return this;
        }
        @Override
        protected void paintChildren(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Rectangle re = editLbl.getBounds();
            g2.setColor(PRIMARY_LIGHT);
            g2.fillRoundRect(re.x, re.y, re.width, re.height, 8, 8);
            Rectangle rd = delLbl.getBounds();
            g2.setColor(ERROR_BG);
            g2.fillRoundRect(rd.x, rd.y, rd.width, rd.height, 8, 8);
            g2.dispose();
            super.paintChildren(g);
        }
    }

    private class ActionEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel;
        private int editingRow;

        ActionEditor() {
            panel = new JPanel(new GridBagLayout());
            panel.setOpaque(true);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(0, 5, 0, 5);

            JButton btnEdit = new JButton("S\u1EEDa");
            btnEdit.setFont(FONT_BADGE); btnEdit.setForeground(PRIMARY); btnEdit.setBackground(PRIMARY_LIGHT);
            btnEdit.setBorderPainted(false); btnEdit.setFocusPainted(false);
            btnEdit.setPreferredSize(new Dimension(52, 28));
            btnEdit.addActionListener(e -> {
                fireEditingStopped();
                ChiTietGia ct = tableModel.getAt(editingRow);
                if (ct != null) openChinhSuaChiTietDialog(ct);
            });

            JButton btnDel = new JButton("X\u00F3a");
            btnDel.setFont(FONT_BADGE); btnDel.setForeground(ERROR_FG); btnDel.setBackground(ERROR_BG);
            btnDel.setBorderPainted(false); btnDel.setFocusPainted(false);
            btnDel.setPreferredSize(new Dimension(52, 28));
            btnDel.addActionListener(e -> {
                fireEditingStopped();
                ChiTietGia ct = tableModel.getAt(editingRow);
                if (ct != null) confirmDelete(ct);
            });

            panel.add(btnEdit, gbc);
            panel.add(btnDel, gbc);
        }

        @Override
        public Component getTableCellEditorComponent(JTable tbl, Object value,
                boolean isSel, int row, int col) {
            editingRow = row;
            panel.setBackground(getRowBg(tbl, true, row));
            return panel;
        }

        @Override public boolean isCellEditable(java.util.EventObject e) { return e instanceof MouseEvent; }
        @Override public Object getCellEditorValue() { return null; }
    }
}
