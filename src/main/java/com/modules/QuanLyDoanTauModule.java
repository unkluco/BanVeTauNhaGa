package com.modules;

import com.connectDB.ConnectDB;
import com.dao.DAO_ChiTietDoanTau;
import com.dao.DAO_DoanTau;
import com.entity.ChiTietDoanTau;
import com.entity.DoanTau;
import com.enums.LoaiGhe;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

public class QuanLyDoanTauModule extends JPanel implements AppModule {

    private Consumer<Object> callback;

    // --- DAOs ---
    private final DAO_DoanTau        daoDoanTau = new DAO_DoanTau();
    private final DAO_ChiTietDoanTau daoChiTiet = new DAO_ChiTietDoanTau();

    // --- Filter UI ---
    private JTextField txtSearch;
    private JCheckBox  cbGheCung, cbGheMem, cbGiuongNam;

    // --- Table ---
    private JTable            table;
    private DoanTauTableModel tableModel;
    private int               hoveredRow = -1;

    // --- Pagination ---
    private int currentPage  = 1;
    private int rowsPerPage  = 10;
    private JLabel lblPageInfo;
    private JPanel paginationPanel;

    // --- Data ---
    private final List<DoanTauRow> allData      = new ArrayList<>();
    private final List<DoanTauRow> filteredData = new ArrayList<>();

    // --- Design tokens (same as QuanLyKhuyenMaiModule) ---
    private static final Color PRIMARY       = new Color(0x00, 0x5D, 0x90);
    private static final Color PRIMARY_LIGHT = new Color(0xE3, 0xF2, 0xFD);
    private static final Color SURFACE       = new Color(0xF8, 0xFA, 0xFC);
    private static final Color CARD_BG       = Color.WHITE;
    private static final Color ON_SURFACE    = new Color(0x1A, 0x1D, 0x21);
    private static final Color ON_SURF_VAR   = new Color(0x5F, 0x67, 0x70);
    private static final Color OUTLINE       = new Color(0xDE, 0xE3, 0xE8);
    private static final Color ROW_ALT       = new Color(0xF8, 0xFA, 0xFC);
    private static final Color ROW_HOVER     = new Color(0xEE, 0xF5, 0xFB);
    private static final Color ERROR_FG      = new Color(0xB9, 0x1C, 0x1C);
    private static final Color ERROR_BG      = new Color(0xFE, 0xE2, 0xE2);

    private static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font FONT_DESC   = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_BODY   = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_BOLD   = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_MONO   = new Font("Consolas", Font.BOLD, 13);
    private static final Font FONT_BADGE  = new Font("Segoe UI", Font.BOLD, 11);
    private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 11);
    private static final Font FONT_SMALL  = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_BTN    = new Font("Segoe UI", Font.BOLD, 13);

    // --- AppModule hidden buttons ---
    private JButton btnSubmit;
    private JButton btnCancel;
    private JPanel  btnPanel;

    // =====================================================================
    //  Inner data holder
    // =====================================================================

    static class DoanTauRow {
        final DoanTau doanTau;
        final int soToaGheCung;
        final int soToaGheMem;
        final int soToaGiuongNam;

        DoanTauRow(DoanTau dt, int gc, int gm, int gn) {
            this.doanTau        = dt;
            this.soToaGheCung   = gc;
            this.soToaGheMem    = gm;
            this.soToaGiuongNam = gn;
        }

        int tongSoToa() { return soToaGheCung + soToaGheMem + soToaGiuongNam; }
    }

    // =====================================================================
    //  Constructor
    // =====================================================================

    public QuanLyDoanTauModule() {
        setLayout(new BorderLayout());
        setBackground(SURFACE);

        btnSubmit = new JButton();
        btnCancel = new JButton();
        btnPanel  = new JPanel();
        btnPanel.setVisible(false);

        JPanel listView = new JPanel(new BorderLayout());
        listView.setBackground(SURFACE);
        listView.setBorder(new EmptyBorder(28, 36, 28, 36));
        listView.add(buildHeader(),    BorderLayout.NORTH);
        listView.add(buildTableCard(), BorderLayout.CENTER);

        add(listView, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        loadData();
    }

    // =====================================================================
    //  Header
    // =====================================================================

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(0, 0));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 24, 0));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel lblTitle = new JLabel("Danh m\u1EE5c \u0111\u1ED9i t\u00E0u");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(ON_SURFACE);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblDesc = new JLabel("Qu\u1EA3n l\u00FD c\u1EA5u h\u00ECnh chi ti\u1EBFt v\u00E0 tr\u1EA1ng th\u00E1i k\u1EF9 thu\u1EADt c\u1EE7a c\u00E1c \u0111o\u00E0n t\u00E0u \u0111ang v\u1EADn h\u00E0nh.");
        lblDesc.setFont(FONT_DESC);
        lblDesc.setForeground(ON_SURF_VAR);
        lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        left.add(lblTitle);
        left.add(Box.createVerticalStrut(4));
        left.add(lblDesc);

        JButton btnAddNew = createPrimaryButton("+ Thi\u1EBFt l\u1EADp \u0111o\u00E0n t\u00E0u m\u1EDBi");
        btnAddNew.setPreferredSize(new Dimension(210, 40));
        btnAddNew.addActionListener(e -> openNewModule());

        JPanel rightWrapper = new JPanel(new GridBagLayout());
        rightWrapper.setOpaque(false);
        rightWrapper.add(btnAddNew);

        header.add(left,         BorderLayout.CENTER);
        header.add(rightWrapper, BorderLayout.EAST);
        return header;
    }

    // =====================================================================
    //  Table Card (rounded card containing filter + table + pagination)
    // =====================================================================

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
        card.add(buildFilterBar(),    BorderLayout.NORTH);
        card.add(buildTableSection(), BorderLayout.CENTER);
        card.add(buildPaginationBar(), BorderLayout.SOUTH);
        return card;
    }

    // =====================================================================
    //  Filter bar
    // =====================================================================

    private JPanel buildFilterBar() {
        JPanel bar = new JPanel();
        bar.setLayout(new BoxLayout(bar, BoxLayout.Y_AXIS));
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, OUTLINE),
                new EmptyBorder(14, 20, 14, 20)));

        // Row 1: search field full width
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
                    Insets ins = getInsets();
                    g2.drawString("T\u00ECm ki\u1EBFm m\u00E3 t\u00E0u, t\u00EAn \u0111o\u00E0n t\u00E0u...", ins.left, getHeight() / 2 + 5);
                    g2.dispose();
                }
            }
        };
        txtSearch.setFont(FONT_BODY);
        txtSearch.setPreferredSize(new Dimension(0, 38));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1),
                new EmptyBorder(6, 12, 6, 12)));
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

        // Row 2: checkboxes + bỏ lọc
        cbGheCung   = new JCheckBox("Gh\u1EBF c\u1EE9ng",   true);
        cbGheMem    = new JCheckBox("Gh\u1EBF m\u1EC1m",    true);
        cbGiuongNam = new JCheckBox("Gi\u01B0\u1EDDng n\u1EB1m", true);
        for (JCheckBox cb : new JCheckBox[]{cbGheCung, cbGheMem, cbGiuongNam}) {
            cb.setFont(FONT_BODY);
            cb.setOpaque(false);
            cb.addActionListener(e -> applyFilter());
        }

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
        btnReset.setPreferredSize(new Dimension(80, 34));
        btnReset.setMaximumSize(new Dimension(80, 34));
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            cbGheCung.setSelected(true);
            cbGheMem.setSelected(true);
            cbGiuongNam.setSelected(true);
            applyFilter();
        });

        JPanel row2 = new JPanel();
        row2.setLayout(new BoxLayout(row2, BoxLayout.X_AXIS));
        row2.setOpaque(false);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        JLabel lblLoai = new JLabel("Lo\u1EA1i gh\u1EBF:");
        lblLoai.setFont(FONT_HEADER);
        lblLoai.setForeground(ON_SURF_VAR);

        row2.add(lblLoai);
        row2.add(Box.createHorizontalStrut(10));
        row2.add(cbGheCung);
        row2.add(Box.createHorizontalStrut(8));
        row2.add(cbGheMem);
        row2.add(Box.createHorizontalStrut(8));
        row2.add(cbGiuongNam);
        row2.add(Box.createHorizontalStrut(16));
        row2.add(btnReset);
        row2.add(Box.createHorizontalGlue());

        bar.add(row1);
        bar.add(Box.createVerticalStrut(10));
        bar.add(row2);
        return bar;
    }

    // =====================================================================
    //  Table section
    // =====================================================================

    private JScrollPane buildTableSection() {
        tableModel = new DoanTauTableModel();
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
                if (row != hoveredRow) { hoveredRow = row; table.repaint(); }
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) { hoveredRow = -1; table.repaint(); }
        });

        // Table header renderer
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean sel, boolean focus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                lbl.setFont(FONT_HEADER);
                lbl.setForeground(ON_SURF_VAR);
                lbl.setBackground(new Color(0xF8, 0xFA, 0xFC));
                lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, OUTLINE),
                        new EmptyBorder(0, 20, 0, 8)));
                lbl.setPreferredSize(new Dimension(lbl.getPreferredSize().width, 44));
                return lbl;
            }
        });

        TableColumnModel colModel = table.getColumnModel();
        int[] widths = {110, 200, 130, 110, 110, 130, 90, 120};
        for (int i = 0; i < widths.length; i++) colModel.getColumn(i).setPreferredWidth(widths[i]);

        // Renderers
        colModel.getColumn(0).setCellRenderer(new RowCellRenderer(FONT_MONO,  PRIMARY,    SwingConstants.LEFT));
        colModel.getColumn(1).setCellRenderer(new RowCellRenderer(FONT_BOLD,  ON_SURFACE, SwingConstants.LEFT));
        colModel.getColumn(2).setCellRenderer(new RowCellRenderer(FONT_BODY,  ON_SURF_VAR, SwingConstants.LEFT));
        colModel.getColumn(3).setCellRenderer(new RowCellRenderer(FONT_BODY,  ON_SURF_VAR, SwingConstants.CENTER));
        colModel.getColumn(4).setCellRenderer(new RowCellRenderer(FONT_BODY,  ON_SURF_VAR, SwingConstants.CENTER));
        colModel.getColumn(5).setCellRenderer(new RowCellRenderer(FONT_BODY,  ON_SURF_VAR, SwingConstants.CENTER));
        colModel.getColumn(6).setCellRenderer(new RowCellRenderer(FONT_BOLD,  ON_SURFACE,  SwingConstants.CENTER));
        colModel.getColumn(7).setCellRenderer(new ActionRenderer());
        colModel.getColumn(7).setCellEditor(new ActionEditor());

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(CARD_BG);
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setWheelScrollingEnabled(false);
        return sp;
    }

    // =====================================================================
    //  Pagination bar
    // =====================================================================

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

        bar.add(lblPageInfo,    BorderLayout.WEST);
        bar.add(paginationPanel, BorderLayout.EAST);
        return bar;
    }

    // =====================================================================
    //  Data
    // =====================================================================

    private void loadData() {
        allData.clear();
        List<DoanTau> dsDoanTau = daoDoanTau.getAll();
        List<ChiTietDoanTau> dsChiTiet = daoChiTiet.getAll();

        Map<String, int[]> countMap = new HashMap<>();
        for (ChiTietDoanTau ct : dsChiTiet) {
            if (ct.getDoanTau() == null || ct.getToaTau() == null) continue;
            String ma = ct.getDoanTau().getMaDoanTau();
            countMap.putIfAbsent(ma, new int[3]);
            LoaiGhe lg = ct.getToaTau().getLoaiGhe();
            if (lg != null) {
                switch (lg) {
                    case GHE_CUNG   -> countMap.get(ma)[0]++;
                    case GHE_MEM    -> countMap.get(ma)[1]++;
                    case GIUONG_NAM -> countMap.get(ma)[2]++;
                }
            }
        }

        for (DoanTau dt : dsDoanTau) {
            int[] c = countMap.getOrDefault(dt.getMaDoanTau(), new int[3]);
            allData.add(new DoanTauRow(dt, c[0], c[1], c[2]));
        }

        applyFilter();
    }

    private void applyFilter() {
        String kw      = txtSearch.getText().trim().toLowerCase();
        boolean showGC = cbGheCung.isSelected();
        boolean showGM = cbGheMem.isSelected();
        boolean showGN = cbGiuongNam.isSelected();
        boolean allOn  = showGC && showGM && showGN;
        boolean noneOn = !showGC && !showGM && !showGN;

        filteredData.clear();
        for (DoanTauRow row : allData) {
            String ma  = row.doanTau.getMaDoanTau()  != null ? row.doanTau.getMaDoanTau().toLowerCase()  : "";
            String ten = row.doanTau.getTenDoanTau() != null ? row.doanTau.getTenDoanTau().toLowerCase() : "";
            if (!kw.isEmpty() && !ma.contains(kw) && !ten.contains(kw)) continue;

            if (!allOn && !noneOn) {
                boolean match = (showGC && row.soToaGheCung > 0)
                             || (showGM && row.soToaGheMem > 0)
                             || (showGN && row.soToaGiuongNam > 0);
                if (!match) continue;
            }
            filteredData.add(row);
        }

        currentPage = 1;
        refreshTable();
    }

    private void refreshTable() {
        int total      = filteredData.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) total / rowsPerPage));
        if (currentPage > totalPages) currentPage = totalPages;

        int from = (currentPage - 1) * rowsPerPage;
        int to   = Math.min(from + rowsPerPage, total);

        tableModel.setData(filteredData.subList(from, to));

        lblPageInfo.setText(total == 0
                ? "Kh\u00F4ng t\u00ECm th\u1EA5y \u0111o\u00E0n t\u00E0u n\u00E0o"
                : "Hi\u1EC3n th\u1ECB " + (from + 1) + " \u2013 " + to + " / " + total + " \u0111o\u00E0n t\u00E0u");

        rebuildPagination(totalPages);
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
                    dots.setBorder(new EmptyBorder(0, 4, 0, 4));
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

    // =====================================================================
    //  Navigation (hide/show pattern)
    // =====================================================================

    private void openEditModule(DoanTau doanTau) {
        Container parent = this.getParent();
        if (parent == null) return;
        this.setVisible(false);

        ChinhSuaDoanTauModule editModule = new ChinhSuaDoanTauModule(doanTau);
        editModule.setOnResult(result -> {
            parent.remove(editModule);
            loadData();
            this.setVisible(true);
            parent.revalidate();
            parent.repaint();
        });

        parent.add(editModule, BorderLayout.CENTER);
        parent.revalidate();
        parent.repaint();
    }

    private void openNewModule() {
        Container parent = this.getParent();
        if (parent == null) return;
        this.setVisible(false);

        ChinhSuaDoanTauModule newModule = new ChinhSuaDoanTauModule(null);
        newModule.setOnResult(result -> {
            parent.remove(newModule);
            loadData();
            this.setVisible(true);
            parent.revalidate();
            parent.repaint();
        });

        parent.add(newModule, BorderLayout.CENTER);
        parent.revalidate();
        parent.repaint();
    }

    private void deleteRow(DoanTauRow row) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "X\u00F3a \u0111o\u00E0n t\u00E0u \u201C" + row.doanTau.getTenDoanTau() + "\u201D?\n"
                + "T\u1EA5t c\u1EA3 chi ti\u1EBFt toa thu\u1ED9c \u0111o\u00E0n t\u00E0u n\u00E0y c\u0169ng s\u1EBD b\u1ECB x\u00F3a.",
                "X\u00E1c nh\u1EADn x\u00F3a", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        Connection con = ConnectDB.getCon();
        if (con == null) {
            JOptionPane.showMessageDialog(this, "Kh\u00F4ng th\u1EC3 k\u1EBFt n\u1ED1i c\u01A1 s\u1EDF d\u1EEF li\u1EC7u.", "L\u1ED7i", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM ChiTietDoanTau WHERE maDoanTau = ?")) {
                ps.setString(1, row.doanTau.getMaDoanTau());
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM DoanTau WHERE maDoanTau = ?")) {
                ps.setString(1, row.doanTau.getMaDoanTau());
                ps.executeUpdate();
            }
            loadData();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "L\u1ED7i khi x\u00F3a: " + e.getMessage(), "L\u1ED7i", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =====================================================================
    //  Table model
    // =====================================================================

    private class DoanTauTableModel extends AbstractTableModel {
        private static final String[] COLS = {
            "M\u00E3 t\u00E0u", "T\u00EAn \u0111o\u00E0n t\u00E0u", "\u0110\u1EA7u m\u00E1y",
            "Toa Gh\u1EBF C\u1EE9ng", "Toa Gh\u1EBF M\u1EC1m", "Toa Gi\u01B0\u1EDDng N\u1EB1m",
            "T\u1ED5ng toa", "Thao t\u00E1c"
        };
        private List<DoanTauRow> data = new ArrayList<>();

        void setData(List<DoanTauRow> d) { this.data = new ArrayList<>(d); fireTableDataChanged(); }
        DoanTauRow getRow(int i)         { return data.get(i); }

        @Override public int getRowCount()    { return data.size(); }
        @Override public int getColumnCount() { return COLS.length; }
        @Override public String getColumnName(int c) { return COLS[c]; }
        @Override public boolean isCellEditable(int r, int c) { return c == 7; }

        @Override
        public Object getValueAt(int r, int c) {
            DoanTauRow row = data.get(r);
            return switch (c) {
                case 0 -> row.doanTau.getMaDoanTau();
                case 1 -> row.doanTau.getTenDoanTau();
                case 2 -> row.doanTau.getDauMay() != null ? row.doanTau.getDauMay().getTenDauMay() : "\u2014";
                case 3 -> row.soToaGheCung;
                case 4 -> row.soToaGheMem;
                case 5 -> row.soToaGiuongNam;
                case 6 -> row.tongSoToa();
                case 7 -> row;
                default -> "";
            };
        }
    }

    // =====================================================================
    //  Renderers & Editor
    // =====================================================================

    private Color getRowBg(JTable tbl, boolean isSel, int row) {
        if (isSel) return PRIMARY_LIGHT;
        if (row == hoveredRow) return ROW_HOVER;
        return row % 2 == 0 ? CARD_BG : ROW_ALT;
    }

    private class RowCellRenderer extends DefaultTableCellRenderer {
        private final Font  font;
        private final Color fg;

        RowCellRenderer(Font font, Color fg, int align) {
            this.font = font;
            this.fg   = fg;
            setHorizontalAlignment(align);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int r, int c) {
            super.getTableCellRendererComponent(t, v, sel, focus, r, c);
            setFont(font);
            setForeground(fg);
            setBorder(new EmptyBorder(0, 20, 0, 8));
            setBackground(getRowBg(t, sel, r));
            return this;
        }
    }

    private class ActionRenderer extends JPanel implements TableCellRenderer {
        private final JLabel btnEdit   = buildActionLabel("S\u1EEDa",  PRIMARY,   PRIMARY_LIGHT);
        private final JLabel btnDelete = buildActionLabel("X\u00F3a", ERROR_FG,   ERROR_BG);

        ActionRenderer() {
            setLayout(new GridBagLayout());
            setOpaque(true);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(0, 4, 0, 4);
            add(btnEdit, gbc);
            add(btnDelete, gbc);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int r, int c) {
            setBackground(getRowBg(t, sel, r));
            return this;
        }
    }

    private class ActionEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel = new JPanel(new GridBagLayout());
        private final JButton btnEdit   = buildActionButton("S\u1EEDa",  PRIMARY,   PRIMARY_LIGHT);
        private final JButton btnDelete = buildActionButton("X\u00F3a", ERROR_FG,   ERROR_BG);
        private DoanTauRow currentRow;

        ActionEditor() {
            panel.setOpaque(true);
            panel.setBackground(ROW_HOVER);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(0, 4, 0, 4);
            panel.add(btnEdit, gbc);
            panel.add(btnDelete, gbc);
            btnEdit.addActionListener(e -> { fireEditingStopped(); openEditModule(currentRow.doanTau); });
            btnDelete.addActionListener(e -> { fireEditingStopped(); deleteRow(currentRow); });
        }

        @Override
        public Component getTableCellEditorComponent(JTable t, Object v, boolean sel, int r, int c) {
            currentRow = (DoanTauRow) v;
            return panel;
        }

        @Override public Object getCellEditorValue() { return currentRow; }
    }

    /** Renderer badge label (non-interactive, used in ActionRenderer) */
    private JLabel buildActionLabel(String text, Color fg, Color bg) {
        JLabel lbl = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lbl.setFont(FONT_BADGE);
        lbl.setForeground(fg);
        lbl.setOpaque(false);
        lbl.setBorder(new EmptyBorder(4, 12, 4, 12));
        return lbl;
    }

    /** Editor button (interactive) */
    private JButton buildActionButton(String text, Color fg, Color bg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BADGE);
        btn.setForeground(fg);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(54, 28));
        return btn;
    }

    // =====================================================================
    //  Primary button
    // =====================================================================

    private JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed())       g2.setColor(PRIMARY.darker());
                else if (getModel().isRollover()) g2.setColor(PRIMARY.brighter());
                else                              g2.setColor(PRIMARY);
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

    // =====================================================================
    //  AppModule interface
    // =====================================================================

    @Override public String getTitle() { return "Qu\u1EA3n l\u00FD \u0111o\u00E0n t\u00E0u"; }
    @Override public JPanel getView()  { return this; }

    @Override
    public void setOnResult(Consumer<Object> cb) {
        this.callback = cb;
        // standalone mode — btnPanel hidden always in this module
    }

    @Override
    public void reset() { loadData(); }
}
