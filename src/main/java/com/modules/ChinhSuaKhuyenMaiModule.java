package com.modules;

import com.dao.DAO_ChiTietKhuyenMai;
import com.entity.ChiTietKhuyenMai;
import com.entity.KhuyenMai;
import com.enums.LoaiGhe;

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
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel chi tiết một KhuyenMai — hiển thị thông tin KM và bảng ChiTietKhuyenMai.
 */
public class ChinhSuaKhuyenMaiModule extends JPanel {

    // ── Design tokens ──────────────────────────────────────────────────
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
    private static final Color STATUS_GREEN_BG = new Color(0xDC, 0xFA, 0xE6);
    private static final Color STATUS_GREEN_FG = new Color(0x16, 0x6B, 0x3A);
    private static final Color STATUS_RED_BG   = new Color(0xFE, 0xE2, 0xE2);
    private static final Color STATUS_RED_FG   = new Color(0xB9, 0x1C, 0x1C);

    private static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font FONT_BODY   = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_BOLD   = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_MONO   = new Font("Consolas", Font.BOLD, 13);
    private static final Font FONT_BADGE  = new Font("Segoe UI", Font.BOLD, 11);
    private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 11);
    private static final Font FONT_SMALL  = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_LABEL  = new Font("Segoe UI", Font.BOLD, 10);
    private static final Font FONT_BTN    = new Font("Segoe UI", Font.BOLD, 13);

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DecimalFormat     PCT_FMT = new DecimalFormat("##.##");

    // ── State ──────────────────────────────────────────────────────────
    private final KhuyenMai khuyenMai;
    private final Runnable  onBack;

    private JTable                    table;
    private ChiTietKhuyenMaiTableModel tableModel;
    private int currentPage   = 1;
    private int rowsPerPage   = 10;
    private int totalRecords  = 0;
    private int hoveredRow    = -1;
    private boolean isRefreshing = false;

    private JLabel lblPageInfo;
    private JPanel paginationPanel;
    private List<ChiTietKhuyenMai> allData      = new ArrayList<>();
    private List<ChiTietKhuyenMai> filteredData = new ArrayList<>();

    // Filter UI
    private JTextField        txtFilterSearch;
    private JComboBox<String> cboFilterLoaiGhe;

    // Info-card live labels
    private JLabel lblInfoTen;
    private JLabel lblInfoMoTa;
    private JLabel lblInfoBatDau;
    private JLabel lblInfoKetThuc;
    private JLabel lblInfoTrangThai;

    // ── Constructor ────────────────────────────────────────────────────
    public ChinhSuaKhuyenMaiModule(KhuyenMai khuyenMai, Runnable onBack) {
        this.khuyenMai = khuyenMai;
        this.onBack    = onBack;
        setLayout(new BorderLayout());
        setBackground(SURFACE);
        setBorder(new EmptyBorder(28, 36, 28, 36));
        buildUI();
        loadData();
    }

    // ── BUILD UI ───────────────────────────────────────────────────────

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

    // ── Page header ────────────────────────────────────────────────────

    private JPanel buildPageHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel leftSection = new JPanel();
        leftSection.setLayout(new BoxLayout(leftSection, BoxLayout.Y_AXIS));
        leftSection.setOpaque(false);

        JLabel lblBack = new JLabel("\u2190 Quay lại danh sách");
        lblBack.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblBack.setForeground(PRIMARY);
        lblBack.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblBack.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblBack.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { if (onBack != null) onBack.run(); }
            @Override public void mouseEntered(MouseEvent e) {
                lblBack.setText("<html><u>\u2190 Quay lại danh sách</u></html>"); }
            @Override public void mouseExited(MouseEvent e)  {
                lblBack.setText("\u2190 Quay lại danh sách"); }
        });

        JLabel lblTitle = new JLabel("Chi tiết khuyến mãi");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(ON_SURFACE);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftSection.add(lblBack);
        leftSection.add(Box.createVerticalStrut(6));
        leftSection.add(lblTitle);

        JButton btnAddDetail = createPrimaryButton("+ Thêm chi tiết mới");
        btnAddDetail.setPreferredSize(new Dimension(200, 40));
        btnAddDetail.addActionListener(e -> openChinhSuaChiTietDialog(null));

        JPanel rightWrapper = new JPanel(new GridBagLayout());
        rightWrapper.setOpaque(false);
        rightWrapper.add(btnAddDetail);

        header.add(leftSection,  BorderLayout.CENTER);
        header.add(rightWrapper, BorderLayout.EAST);
        return header;
    }

    // ── Info card ──────────────────────────────────────────────────────

    private JPanel buildInfoCard() {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(INFO_CARD_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(OUTLINE_VAR);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, 16, 16));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        card.setBorder(new EmptyBorder(18, 22, 18, 22));

        // Card header: icon + title + edit button
        JPanel cardHeader = new JPanel(new BorderLayout());
        cardHeader.setOpaque(false);
        cardHeader.setBorder(new EmptyBorder(0, 0, 14, 0));

        JPanel iconTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        iconTitle.setOpaque(false);

        JPanel icon = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(PRIMARY_FIXED);
                g2.fillRoundRect(0, 0, 38, 38, 10, 10);
                g2.setColor(PRIMARY);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                FontMetrics fm = g2.getFontMetrics();
                String s = "%";
                g2.drawString(s, (38 - fm.stringWidth(s)) / 2, 25);
                g2.dispose();
            }
        };
        icon.setOpaque(false);
        icon.setPreferredSize(new Dimension(38, 38));

        JLabel lblCardTitle = new JLabel("Thông tin khuyến mãi");
        lblCardTitle.setFont(FONT_BOLD);
        lblCardTitle.setForeground(ON_SURFACE);

        iconTitle.add(icon);
        iconTitle.add(lblCardTitle);

        JButton btnEdit = new JButton("\u270E Chỉnh sửa");
        btnEdit.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnEdit.setForeground(PRIMARY);
        btnEdit.setContentAreaFilled(false); btnEdit.setBorderPainted(false);
        btnEdit.setFocusPainted(false);
        btnEdit.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnEdit.addActionListener(e -> openSuaKhuyenMaiDialog());

        cardHeader.add(iconTitle, BorderLayout.WEST);
        cardHeader.add(btnEdit,   BorderLayout.EAST);

        // Fields: 5 columns
        JPanel fields = new JPanel(new GridLayout(1, 5, 20, 0));
        fields.setOpaque(false);

        fields.add(buildInfoField("MÃ KM", khuyenMai.getMaKhuyenMai(), FONT_MONO, PRIMARY));

        lblInfoTen = new JLabel(khuyenMai.getTenKhuyenMai() != null ? khuyenMai.getTenKhuyenMai() : "—");
        lblInfoTen.setFont(FONT_BOLD); lblInfoTen.setForeground(ON_SURFACE);
        fields.add(buildInfoFieldWithLabel("TÊN CHƯƠNG TRÌNH", lblInfoTen));

        lblInfoMoTa = new JLabel(khuyenMai.getMoTa() != null ? khuyenMai.getMoTa() : "—");
        lblInfoMoTa.setFont(FONT_BODY); lblInfoMoTa.setForeground(ON_SURF_VAR);
        fields.add(buildInfoFieldWithLabel("MÔ TẢ", lblInfoMoTa));

        String batDauStr = khuyenMai.getThoiGianBatDau() != null
                ? khuyenMai.getThoiGianBatDau().format(DT_FMT) : "—";
        lblInfoBatDau = new JLabel(batDauStr);
        lblInfoBatDau.setFont(FONT_BOLD); lblInfoBatDau.setForeground(ON_SURFACE);
        fields.add(buildInfoFieldWithLabel("BẮT ĐẦU", lblInfoBatDau));

        String ketThucStr = khuyenMai.getThoiGianKetThuc() != null
                ? khuyenMai.getThoiGianKetThuc().format(DT_FMT) : "—";
        lblInfoKetThuc = new JLabel(ketThucStr);
        lblInfoKetThuc.setFont(FONT_BOLD); lblInfoKetThuc.setForeground(ON_SURFACE);
        fields.add(buildInfoFieldWithLabel("KẾT THÚC", lblInfoKetThuc));

        card.add(cardHeader, BorderLayout.NORTH);
        card.add(fields,     BorderLayout.CENTER);
        return card;
    }

    private JPanel buildInfoField(String labelText, String value, Font valueFont, Color valueFg) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(FONT_LABEL); lbl.setForeground(ON_SURF_VAR); lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel val = new JLabel(value);
        val.setFont(valueFont); val.setForeground(valueFg); val.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lbl); p.add(Box.createVerticalStrut(4)); p.add(val);
        return p;
    }

    private JPanel buildInfoFieldWithLabel(String labelText, JLabel valueLabel) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(FONT_LABEL); lbl.setForeground(ON_SURF_VAR); lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lbl); p.add(Box.createVerticalStrut(4)); p.add(valueLabel);
        return p;
    }

    // ── Table card ─────────────────────────────────────────────────────

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

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);
        tableHeader.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, OUTLINE),
                new EmptyBorder(14, 20, 14, 20)));

        JLabel lblTableTitle = new JLabel("Danh sách chi tiết khuyến mãi");
        lblTableTitle.setFont(FONT_BOLD); lblTableTitle.setForeground(ON_SURFACE);
        tableHeader.add(lblTableTitle, BorderLayout.WEST);

        card.add(buildKMTableFilterBar(), BorderLayout.NORTH);
        card.add(buildTableSection(), BorderLayout.CENTER);
        card.add(buildPaginationBar(),BorderLayout.SOUTH);
        return card;
    }

    private JScrollPane buildTableSection() {
        tableModel = new ChiTietKhuyenMaiTableModel();
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
            @Override public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row != hoveredRow) { hoveredRow = row; table.repaint(); }
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseExited(MouseEvent e) { hoveredRow = -1; table.repaint(); }
        });

        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tbl, Object value,
                    boolean isSel, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(tbl, value, isSel, hasFocus, row, col);
                lbl.setFont(FONT_HEADER); lbl.setForeground(ON_SURF_VAR);
                lbl.setBackground(new Color(0xF8, 0xFA, 0xFC));
                lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, OUTLINE),
                        new EmptyBorder(0, 20, 0, 8)));
                lbl.setPreferredSize(new Dimension(lbl.getPreferredSize().width, 40));
                return lbl;
            }
        });

        TableColumnModel cols = table.getColumnModel();
        int[] widths = {110, 140, 210, 130, 100, 120};
        for (int i = 0; i < widths.length; i++) cols.getColumn(i).setPreferredWidth(widths[i]);

        cols.getColumn(0).setCellRenderer(new MaCellRenderer());
        cols.getColumn(1).setCellRenderer(new TextCellRenderer(FONT_BODY, ON_SURF_VAR));
        cols.getColumn(2).setCellRenderer(new TextCellRenderer(FONT_BODY, ON_SURFACE));
        cols.getColumn(3).setCellRenderer(new LoaiGheBadgeRenderer());
        cols.getColumn(4).setCellRenderer(new PctCellRenderer());
        cols.getColumn(5).setCellRenderer(new ActionRenderer());
        cols.getColumn(5).setCellEditor(new ActionEditor());

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(CARD_BG);
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setWheelScrollingEnabled(false);

        sp.getViewport().addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                int newRows = calcRowsFromViewport();
                if (newRows > 0 && newRows != rowsPerPage && !isRefreshing) {
                    rowsPerPage = newRows; refreshTable();
                }
            }
        });
        return sp;
    }

    private int calcRowsFromViewport() {
        if (table == null || !(table.getParent() instanceof JViewport vp)) return 0;
        int viewH = vp.getHeight();
        int rh = table.getRowHeight() > 0 ? table.getRowHeight() : 52;
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
                new EmptyBorder(12, 20, 12, 20)));

        lblPageInfo = new JLabel();
        lblPageInfo.setFont(FONT_SMALL); lblPageInfo.setForeground(ON_SURF_VAR);

        paginationPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        paginationPanel.setOpaque(false);

        bar.add(lblPageInfo,    BorderLayout.WEST);
        bar.add(paginationPanel,BorderLayout.EAST);
        return bar;
    }

    // ── Filter bar ─────────────────────────────────────────────────────

    private JPanel buildKMTableFilterBar() {
        JPanel bar = new JPanel();
        bar.setLayout(new BoxLayout(bar, BoxLayout.Y_AXIS));
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, OUTLINE),
                new EmptyBorder(14, 20, 14, 20)
        ));

        // Row 1: title only
        JLabel lblTitle = new JLabel("Danh sách chi tiết khuyến mãi");
        lblTitle.setFont(FONT_BOLD);
        lblTitle.setForeground(ON_SURFACE);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Row 2: search + loai ghe + reset
        txtFilterSearch = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !hasFocus()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setColor(new Color(0x9E, 0xA7, 0xB0));
                    g2.setFont(FONT_BODY);
                    Insets ins = getInsets();
                    g2.drawString("Tìm theo mã, tên chi tiết, tuyến...", ins.left, getHeight() / 2 + 5);
                    g2.dispose();
                }
            }
        };
        txtFilterSearch.setFont(FONT_BODY);
        txtFilterSearch.setPreferredSize(new Dimension(0, 38));
        txtFilterSearch.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txtFilterSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1), new EmptyBorder(6, 12, 6, 12)));
        txtFilterSearch.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) { txtFilterSearch.repaint(); }
            public void focusLost(java.awt.event.FocusEvent e)   { txtFilterSearch.repaint(); }
        });
        txtFilterSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { applyFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { applyFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
        });

        JLabel lblLg = new JLabel("Loại ghế:");
        lblLg.setFont(FONT_HEADER);
        lblLg.setForeground(ON_SURF_VAR);

        cboFilterLoaiGhe = new JComboBox<>(new String[]{
            "Tất cả", "Ghế cứng", "Ghế mềm", "Giường nằm", "Tất cả loại ghế (không chỉ định)"
        });
        cboFilterLoaiGhe.setFont(FONT_BODY);
        cboFilterLoaiGhe.setPreferredSize(new Dimension(200, 38));
        cboFilterLoaiGhe.setMaximumSize(new Dimension(200, 38));
        cboFilterLoaiGhe.addActionListener(e -> applyFilter());

        JButton btnReset = new JButton("Bỏ lọc") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? OUTLINE_VAR : OUTLINE);
                g2.fill(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
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
        btnReset.setPreferredSize(new Dimension(72, 38));
        btnReset.setMaximumSize(new Dimension(72, 38));
        btnReset.addActionListener(e -> {
            txtFilterSearch.setText("");
            cboFilterLoaiGhe.setSelectedIndex(0);
            applyFilter();
        });

        JPanel row2 = new JPanel();
        row2.setLayout(new BoxLayout(row2, BoxLayout.X_AXIS));
        row2.setOpaque(false);
        row2.setAlignmentX(Component.LEFT_ALIGNMENT);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        row2.add(txtFilterSearch);
        row2.add(Box.createHorizontalStrut(12));
        row2.add(lblLg);
        row2.add(Box.createHorizontalStrut(6));
        row2.add(cboFilterLoaiGhe);
        row2.add(Box.createHorizontalStrut(8));
        row2.add(btnReset);

        bar.add(lblTitle);
        bar.add(Box.createVerticalStrut(10));
        bar.add(row2);
        return bar;
    }

    private void applyFilter() {
        String kw = txtFilterSearch != null ? txtFilterSearch.getText().trim().toLowerCase() : "";
        int lgIdx = cboFilterLoaiGhe != null ? cboFilterLoaiGhe.getSelectedIndex() : 0;

        filteredData = new ArrayList<>();
        for (ChiTietKhuyenMai ct : allData) {
            boolean matchKw = kw.isEmpty()
                    || ct.getMaChiTietKM().toLowerCase().contains(kw)
                    || (ct.getTenChiTiet() != null && ct.getTenChiTiet().toLowerCase().contains(kw))
                    || (ct.getTuyen() != null && ct.getTuyen().getMaTuyen().toLowerCase().contains(kw))
                    || (ct.getTuyen() != null && ct.getTuyen().getGaDi()  != null
                        && ct.getTuyen().getGaDi().getTenGa().toLowerCase().contains(kw))
                    || (ct.getTuyen() != null && ct.getTuyen().getGaDen() != null
                        && ct.getTuyen().getGaDen().getTenGa().toLowerCase().contains(kw));

            boolean matchLg = lgIdx == 0
                    || (lgIdx == 1 && ct.getLoaiGhe() != null && ct.getLoaiGhe().toString().equals("Ghế cứng"))
                    || (lgIdx == 2 && ct.getLoaiGhe() != null && ct.getLoaiGhe().toString().equals("Ghế mềm"))
                    || (lgIdx == 3 && ct.getLoaiGhe() != null && ct.getLoaiGhe().toString().equals("Giường nằm"))
                    || (lgIdx == 4 && ct.getLoaiGhe() == null);

            if (matchKw && matchLg) filteredData.add(ct);
        }
        currentPage = 1;
        refreshTable();
    }

    // ── Data ───────────────────────────────────────────────────────────

    void loadData() {
        new SwingWorker<List<ChiTietKhuyenMai>, Void>() {
            @Override protected List<ChiTietKhuyenMai> doInBackground() {
                return new DAO_ChiTietKhuyenMai().findByKhuyenMai(khuyenMai.getMaKhuyenMai());
            }
            @Override protected void done() {
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
                    ? "Không tìm thấy chi tiết nào"
                    : "Hiển thị " + (start+1) + " – " + end + " / " + totalRecords + " bản ghi");
            rebuildPagination(totalPages);
        } finally {
            isRefreshing = false;
        }
    }

    private void rebuildPagination(int totalPages) {
        paginationPanel.removeAll();
        addNavBtn("❮", currentPage > 1, () -> { currentPage--; refreshTable(); });
        for (int i = 1; i <= totalPages; i++) {
            final int pg = i;
            JButton btn = new JButton(String.valueOf(pg)) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (pg == currentPage) {
                        g2.setColor(PRIMARY);
                        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                    }
                    g2.dispose(); super.paintComponent(g);
                }
            };
            btn.setFont(FONT_HEADER);
            btn.setPreferredSize(new Dimension(32, 32));
            btn.setMargin(new java.awt.Insets(0,0,0,0));
            btn.setFocusPainted(false); btn.setBorderPainted(false); btn.setContentAreaFilled(false);
            btn.setForeground(pg == currentPage ? Color.WHITE : ON_SURF_VAR);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> { currentPage = pg; refreshTable(); });
            paginationPanel.add(btn);
        }
        addNavBtn("❯", currentPage < totalPages, () -> { currentPage++; refreshTable(); });
        paginationPanel.revalidate(); paginationPanel.repaint();
    }

    private void addNavBtn(String sym, boolean enabled, Runnable action) {
        JButton btn = new JButton(sym);
        btn.setFont(FONT_BODY);
        btn.setPreferredSize(new Dimension(32, 32));
        btn.setMargin(new java.awt.Insets(0,0,0,0));
        btn.setFocusPainted(false); btn.setBorderPainted(false); btn.setContentAreaFilled(false);
        btn.setForeground(enabled ? ON_SURF_VAR : OUTLINE);
        btn.setEnabled(enabled);
        btn.setCursor(enabled ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
        btn.addActionListener(e -> action.run());
        paginationPanel.add(btn);
    }

    // ── Actions ────────────────────────────────────────────────────────

    private void openSuaKhuyenMaiDialog() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        SuaKhuyenMaiDialog dlg = new SuaKhuyenMaiDialog(owner, khuyenMai, () -> {
            lblInfoTen.setText(khuyenMai.getTenKhuyenMai() != null ? khuyenMai.getTenKhuyenMai() : "—");
            lblInfoMoTa.setText(khuyenMai.getMoTa() != null ? khuyenMai.getMoTa() : "—");
            lblInfoBatDau.setText(khuyenMai.getThoiGianBatDau() != null
                    ? khuyenMai.getThoiGianBatDau().format(DT_FMT) : "—");
            lblInfoKetThuc.setText(khuyenMai.getThoiGianKetThuc() != null
                    ? khuyenMai.getThoiGianKetThuc().format(DT_FMT) : "—");
            repaint();
        });
        dlg.setVisible(true);
    }

    private void openChinhSuaChiTietDialog(ChiTietKhuyenMai ctkm) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        ChinhSuaChiTietKhuyenMaiDialog dlg =
                new ChinhSuaChiTietKhuyenMaiDialog(owner, khuyenMai, ctkm, this::loadData);
        dlg.setVisible(true);
    }

    private void confirmDelete(ChiTietKhuyenMai ctkm) {
        int res = JOptionPane.showConfirmDialog(this,
                "Xóa chi tiết khuyến mãi \"" + ctkm.getMaChiTietKM() + "\"?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (res != JOptionPane.YES_OPTION) return;
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() {
                return new DAO_ChiTietKhuyenMai().delete(ctkm.getMaChiTietKM());
            }
            @Override protected void done() {
                try {
                    if (get()) loadData();
                    else JOptionPane.showMessageDialog(ChinhSuaKhuyenMaiModule.this,
                            "Không thể xóa chi tiết khuyến mãi!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ChinhSuaKhuyenMaiModule.this,
                            "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // ── Helpers ────────────────────────────────────────────────────────

    private JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = getModel().isPressed() ? PRIMARY.darker()
                        : getModel().isRollover() ? PRIMARY.brighter() : PRIMARY;
                g2.setColor(c);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose(); super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BTN); btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private Color getRowBg(JTable tbl, boolean isSel, int row) {
        if (isSel) return PRIMARY_LIGHT;
        if (row == hoveredRow) return ROW_HOVER;
        return row % 2 == 0 ? CARD_BG : ROW_ALT;
    }

    // ── Table Model ────────────────────────────────────────────────────

    private class ChiTietKhuyenMaiTableModel extends AbstractTableModel {
        private final String[] COLS = {
            "Mã chi tiết", "Tên chi tiết", "Tuyến", "Loại ghế", "% Giảm", ""
        };
        private List<ChiTietKhuyenMai> data = new ArrayList<>();

        void setData(List<ChiTietKhuyenMai> d) { this.data = new ArrayList<>(d); fireTableDataChanged(); }

        @Override public int getRowCount()    { return data.size(); }
        @Override public int getColumnCount() { return COLS.length; }
        @Override public String getColumnName(int c) { return COLS[c]; }
        @Override public boolean isCellEditable(int r, int c) { return c == 5; }

        @Override public Object getValueAt(int r, int c) {
            ChiTietKhuyenMai ct = data.get(r);
            return switch (c) {
                case 0 -> ct.getMaChiTietKM();
                case 1 -> ct.getTenChiTiet() != null ? ct.getTenChiTiet() : "—";
                case 2 -> {
                    if (ct.getTuyen() == null) yield "Tất cả tuyến";
                    String gaDi  = ct.getTuyen().getGaDi()  != null ? ct.getTuyen().getGaDi().getTenGa()  : "?";
                    String gaDen = ct.getTuyen().getGaDen() != null ? ct.getTuyen().getGaDen().getTenGa() : "?";
                    yield gaDi + " → " + gaDen + " (" + ct.getTuyen().getMaTuyen() + ")";
                }
                case 3 -> ct.getLoaiGhe() != null ? ct.getLoaiGhe().toString() : null;
                case 4 -> ct.getPhanTramGiam();
                case 5 -> ct;
                default -> "";
            };
        }

        ChiTietKhuyenMai getAt(int r) { return (r >= 0 && r < data.size()) ? data.get(r) : null; }
    }

    // ── Renderers & Editors ────────────────────────────────────────────

    private class MaCellRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable tbl, Object value,
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
        @Override public Component getTableCellRendererComponent(JTable tbl, Object value,
                boolean isSel, boolean hasFocus, int row, int col) {
            super.getTableCellRendererComponent(tbl, value, isSel, hasFocus, row, col);
            setFont(f); setForeground(fg);
            setBorder(new EmptyBorder(0, 20, 0, 8));
            setBackground(getRowBg(tbl, isSel, row));
            return this;
        }
    }

    private class PctCellRenderer extends DefaultTableCellRenderer {
        PctCellRenderer() { setHorizontalAlignment(SwingConstants.LEFT); }
        @Override public Component getTableCellRendererComponent(JTable tbl, Object value,
                boolean isSel, boolean hasFocus, int row, int col) {
            String text = (value instanceof Double d)
                    ? PCT_FMT.format(d * 100) + "%" : "—";
            super.getTableCellRendererComponent(tbl, text, isSel, hasFocus, row, col);
            setFont(FONT_BOLD); setForeground(new Color(0x00, 0x7A, 0x3D));
            setBorder(new EmptyBorder(0, 20, 0, 8));
            setBackground(getRowBg(tbl, isSel, row));
            return this;
        }
    }

    // Reuse LoaiGheBadgeRenderer logic inline (copy from ChinhSuaGiaModule pattern)
    private class LoaiGheBadgeRenderer extends JPanel implements TableCellRenderer {
        private final JLabel badge = new JLabel();
        private Color badgeBg = BADGE_BG, badgeFg = ON_SURF_VAR;
        private static final Color GHE_CUNG_BG = new Color(0xDB, 0xEA, 0xFE);
        private static final Color GHE_CUNG_FG = new Color(0x1D, 0x4E, 0xD8);
        private static final Color GHE_MEM_BG  = new Color(0xDC, 0xFC, 0xE7);
        private static final Color GHE_MEM_FG  = new Color(0x15, 0x80, 0x3D);
        private static final Color GIUONG_BG   = new Color(0xED, 0xE9, 0xFE);
        private static final Color GIUONG_FG   = new Color(0x6D, 0x28, 0xD9);

        LoaiGheBadgeRenderer() {
            setLayout(new GridBagLayout()); setOpaque(true);
            badge.setFont(FONT_BADGE);
            badge.setHorizontalAlignment(SwingConstants.CENTER);
            badge.setOpaque(false);
            add(badge);
        }
        private static final Color ALL_BG = new Color(0xFF, 0xF3, 0xCD);
        private static final Color ALL_FG = new Color(0x92, 0x60, 0x0A);

        @Override public Component getTableCellRendererComponent(JTable tbl, Object value,
                boolean isSel, boolean hasFocus, int row, int col) {
            if (value == null) {
                badge.setText("Tất cả loại ghế");
                badgeBg = ALL_BG; badgeFg = ALL_FG;
            } else {
                String text = value.toString();
                badge.setText(text);
                if (text.equals("Ghế cứng"))        { badgeBg = GHE_CUNG_BG; badgeFg = GHE_CUNG_FG; }
                else if (text.equals("Ghế mềm"))    { badgeBg = GHE_MEM_BG;  badgeFg = GHE_MEM_FG;  }
                else if (text.equals("Giường nằm")) { badgeBg = GIUONG_BG;   badgeFg = GIUONG_FG;   }
                else                                { badgeBg = BADGE_BG;    badgeFg = ON_SURF_VAR;  }
            }
            badge.setForeground(badgeFg);
            setBackground(getRowBg(tbl, isSel, row));
            return this;
        }
        @Override protected void paintChildren(Graphics g) {
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
        private final JLabel editLbl = new JLabel("Sửa");
        private final JLabel delLbl  = new JLabel("Xóa");
        ActionRenderer() {
            setLayout(new GridBagLayout()); setOpaque(true);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new java.awt.Insets(0, 5, 0, 5);
            configLabel(editLbl, PRIMARY);
            configLabel(delLbl,  ERROR_FG);
            add(editLbl, gbc); add(delLbl, gbc);
        }
        private void configLabel(JLabel lbl, Color fg) {
            lbl.setFont(FONT_BADGE); lbl.setForeground(fg);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setPreferredSize(new Dimension(52, 28));
            lbl.setOpaque(false);
        }
        @Override public Component getTableCellRendererComponent(JTable tbl, Object value,
                boolean isSel, boolean hasFocus, int row, int col) {
            setBackground(getRowBg(tbl, isSel, row)); return this;
        }
        @Override protected void paintChildren(Graphics g) {
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
            gbc.insets = new java.awt.Insets(0, 5, 0, 5);

            JButton btnEdit = new JButton("Sửa");
            btnEdit.setFont(FONT_BADGE); btnEdit.setForeground(PRIMARY); btnEdit.setBackground(PRIMARY_LIGHT);
            btnEdit.setBorderPainted(false); btnEdit.setFocusPainted(false);
            btnEdit.setPreferredSize(new Dimension(52, 28));
            btnEdit.addActionListener(e -> {
                fireEditingStopped();
                ChiTietKhuyenMai ct = tableModel.getAt(editingRow);
                if (ct != null) openChinhSuaChiTietDialog(ct);
            });

            JButton btnDel = new JButton("Xóa");
            btnDel.setFont(FONT_BADGE); btnDel.setForeground(ERROR_FG); btnDel.setBackground(ERROR_BG);
            btnDel.setBorderPainted(false); btnDel.setFocusPainted(false);
            btnDel.setPreferredSize(new Dimension(52, 28));
            btnDel.addActionListener(e -> {
                fireEditingStopped();
                ChiTietKhuyenMai ct = tableModel.getAt(editingRow);
                if (ct != null) confirmDelete(ct);
            });

            panel.add(btnEdit, gbc); panel.add(btnDel, gbc);
        }

        @Override public Component getTableCellEditorComponent(JTable tbl, Object value,
                boolean isSel, int row, int col) {
            editingRow = row;
            panel.setBackground(getRowBg(tbl, true, row));
            return panel;
        }
        @Override public boolean isCellEditable(java.util.EventObject e) { return e instanceof MouseEvent; }
        @Override public Object getCellEditorValue() { return null; }
    }
}
