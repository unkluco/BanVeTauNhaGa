package com.modules;

import com.dao.DAO_NhanVien;
import com.entity.NhanVien;
import com.enums.TrangThaiNhanVien;
import com.enums.VaiTro;

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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class QuanLyNhanVienModule extends JPanel implements AppModule {

    private Consumer<Object> callback;

    // --- UI components ---
    private JTextField        txtSearch;
    private JComboBox<String> cboVaiTro;
    private JComboBox<String> cboTrangThai;
    private JButton           btnAddNew;
    private JTable            table;
    private NhanVienTableModel tableModel;

    // --- Pagination ---
    private int currentPage  = 1;
    private int rowsPerPage  = 10;
    private int totalRecords = 0;

    private JLabel  lblPageInfo;
    private JPanel  paginationPanel;

    // --- Data ---
    private List<NhanVien> allData      = new ArrayList<>();
    private List<NhanVien> filteredData = new ArrayList<>();

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

    private static final Color STATUS_GREEN_BG  = new Color(0xDC, 0xFA, 0xE6);
    private static final Color STATUS_GREEN_FG  = new Color(0x16, 0x6B, 0x3A);
    private static final Color STATUS_ORANGE_BG = new Color(0xFF, 0xED, 0xD5);
    private static final Color STATUS_ORANGE_FG = new Color(0xC2, 0x41, 0x0C);
    private static final Color STATUS_RED_BG    = new Color(0xFE, 0xE2, 0xE2);
    private static final Color STATUS_RED_FG    = new Color(0xB9, 0x1C, 0x1C);

    private static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font FONT_DESC    = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_BOLD    = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_MONO    = new Font("Consolas", Font.BOLD, 13);
    private static final Font FONT_BADGE   = new Font("Segoe UI", Font.BOLD, 11);
    private static final Font FONT_HEADER  = new Font("Segoe UI", Font.BOLD, 11);
    private static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_BTN     = new Font("Segoe UI", Font.BOLD, 13);

    // --- Hidden buttons for AppModule compliance ---
    private JButton btnSubmit;
    private JButton btnCancel;
    private JPanel  btnPanel;

    // --- Hover tracking ---
    private int hoveredRow = -1;

    public QuanLyNhanVienModule() {
        setLayout(new BorderLayout());
        setBackground(SURFACE);
        setBorder(new EmptyBorder(28, 36, 28, 36));

        btnSubmit = new JButton();
        btnCancel = new JButton();
        btnPanel = new JPanel();
        btnPanel.setVisible(false);

        buildUI();
        loadData();
    }

    // =================================================================
    //  BUILD UI
    // =================================================================

    private void buildUI() {
        add(buildHeader(), BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(0, 0));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 24, 0));

        // Left: title + description
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel lblTitle = new JLabel("Qu\u1EA3n l\u00FD nh\u00E2n vi\u00EAn");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(ON_SURFACE);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblDesc = new JLabel("Qu\u1EA3n l\u00FD th\u00F4ng tin v\u00E0 tr\u1EA1ng th\u00E1i l\u00E0m vi\u1EC7c c\u1EE7a nh\u00E2n vi\u00EAn");
        lblDesc.setFont(FONT_DESC);
        lblDesc.setForeground(ON_SURF_VAR);
        lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        left.add(lblTitle);
        left.add(Box.createVerticalStrut(4));
        left.add(lblDesc);

        // Right: add button
        btnAddNew = createPrimaryButton("+ Th\u00EAm nh\u00E2n vi\u00EAn");
        btnAddNew.setPreferredSize(new Dimension(170, 40));

        JPanel rightWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        rightWrapper.setOpaque(false);
        rightWrapper.add(btnAddNew);

        header.add(left, BorderLayout.CENTER);
        header.add(rightWrapper, BorderLayout.EAST);

        return header;
    }

    private JPanel buildTableCard() {
        // Card container with rounded border
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
        card.setBorder(new EmptyBorder(0, 0, 0, 0));

        card.add(buildFilterBar(), BorderLayout.NORTH);
        card.add(buildTableSection(), BorderLayout.CENTER);
        card.add(buildPaginationBar(), BorderLayout.SOUTH);

        return card;
    }

    private JPanel buildFilterBar() {
        JPanel bar = new JPanel();
        bar.setLayout(new BoxLayout(bar, BoxLayout.X_AXIS));
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, OUTLINE),
                new EmptyBorder(16, 20, 16, 20)
        ));

        // Search field
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
                    g2.drawString("T\u00ECm ki\u1EBFm theo t\u00EAn, m\u00E3 NV...", insets.left, getHeight() / 2 + 5);
                    g2.dispose();
                }
            }
        };
        txtSearch.setFont(FONT_BODY);
        txtSearch.setMaximumSize(new Dimension(280, 36));
        txtSearch.setPreferredSize(new Dimension(280, 36));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1),
                new EmptyBorder(6, 12, 6, 12)
        ));
        txtSearch.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) { txtSearch.repaint(); }
            public void focusLost(java.awt.event.FocusEvent e)   { txtSearch.repaint(); }
        });
        txtSearch.addActionListener(e -> applyFilter());

        // Vai tro dropdown
        cboVaiTro = createFilterCombo(new String[]{
                "T\u1EA5t c\u1EA3 b\u1ED9 ph\u1EADn",
                "Nh\u00E2n vi\u00EAn qu\u1EA7y v\u00E9",
                "\u0110i\u1EC1u ph\u1ED1i",
                "Admin"
        });
        cboVaiTro.addActionListener(e -> applyFilter());

        // Trang thai dropdown
        cboTrangThai = createFilterCombo(new String[]{
                "T\u1EA5t c\u1EA3 tr\u1EA1ng th\u00E1i",
                "\u0110ang l\u00E0m",
                "Ngh\u1EC9 ph\u00E9p",
                "\u0110\u00E3 ngh\u1EC9"
        });
        cboTrangThai.addActionListener(e -> applyFilter());

        bar.add(txtSearch);
        bar.add(Box.createHorizontalStrut(12));
        bar.add(createFilterLabel("B\u1ED9 ph\u1EADn:"));
        bar.add(Box.createHorizontalStrut(6));
        bar.add(cboVaiTro);
        bar.add(Box.createHorizontalStrut(12));
        bar.add(createFilterLabel("Tr\u1EA1ng th\u00E1i:"));
        bar.add(Box.createHorizontalStrut(6));
        bar.add(cboTrangThai);
        bar.add(Box.createHorizontalGlue());

        return bar;
    }

    private JScrollPane buildTableSection() {
        tableModel = new NhanVienTableModel();
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

        // Row hover effect
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
                return lbl;
            }
        });

        // Column widths & renderers
        TableColumnModel colModel = table.getColumnModel();
        int[] widths = {80, 200, 140, 120, 130, 90};
        for (int i = 0; i < widths.length; i++) {
            colModel.getColumn(i).setPreferredWidth(widths[i]);
        }

        // Col 0: Ma NV
        colModel.getColumn(0).setCellRenderer(new RowCellRenderer(FONT_MONO, PRIMARY, SwingConstants.LEFT));
        // Col 1: Ho ten
        colModel.getColumn(1).setCellRenderer(new RowCellRenderer(FONT_BOLD, ON_SURFACE, SwingConstants.LEFT));
        // Col 2: Bo phan
        colModel.getColumn(2).setCellRenderer(new RowCellRenderer(FONT_BODY, ON_SURF_VAR, SwingConstants.LEFT));
        // Col 3: SDT
        colModel.getColumn(3).setCellRenderer(new RowCellRenderer(FONT_BODY, ON_SURF_VAR, SwingConstants.LEFT));
        // Col 4: Trang thai (badge)
        colModel.getColumn(4).setCellRenderer(new BadgeCellRenderer());
        // Col 5: Edit button
        colModel.getColumn(5).setCellRenderer(new EditButtonRenderer());
        colModel.getColumn(5).setCellEditor(new EditButtonEditor());

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(CARD_BG);
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setWheelScrollingEnabled(false);
        return sp;
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
    //  HELPER: create styled components
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
        cbo.setMaximumSize(new Dimension(180, 36));
        cbo.setPreferredSize(new Dimension(180, 36));
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

    private void loadData() {
        SwingWorker<List<NhanVien>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<NhanVien> doInBackground() {
                return new DAO_NhanVien().getAll();
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
        String keyword = txtSearch.getText().trim().toLowerCase();
        int vaiTroIdx = cboVaiTro.getSelectedIndex();
        int trangThaiIdx = cboTrangThai.getSelectedIndex();

        filteredData = new ArrayList<>();
        for (NhanVien nv : allData) {
            boolean matchKw = keyword.isEmpty()
                    || nv.getMaNV().toLowerCase().contains(keyword)
                    || nv.getHoTen().toLowerCase().contains(keyword)
                    || (nv.getSoDienThoai() != null && nv.getSoDienThoai().contains(keyword));

            boolean matchVt = vaiTroIdx == 0 || (nv.getVaiTro() != null && switch (vaiTroIdx) {
                case 1 -> nv.getVaiTro() == VaiTro.BAN_VE;
                case 2 -> nv.getVaiTro() == VaiTro.DIEU_PHOI;
                case 3 -> nv.getVaiTro() == VaiTro.ADMIN;
                default -> true;
            });

            boolean matchTt = trangThaiIdx == 0 || (nv.getTrangThai() != null && switch (trangThaiIdx) {
                case 1 -> nv.getTrangThai() == TrangThaiNhanVien.DANG_LAM;
                case 2 -> nv.getTrangThai() == TrangThaiNhanVien.NGHI_PHEP;
                case 3 -> nv.getTrangThai() == TrangThaiNhanVien.DA_NGHI;
                default -> true;
            });

            if (matchKw && matchVt && matchTt) {
                filteredData.add(nv);
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
        int totalPages = Math.max(1, (int) Math.ceil((double) totalRecords / rowsPerPage));
        if (currentPage > totalPages) currentPage = totalPages;

        int start = (currentPage - 1) * rowsPerPage;
        int end = Math.min(start + rowsPerPage, totalRecords);

        tableModel.setData(filteredData.subList(start, end));

        lblPageInfo.setText(totalRecords == 0
                ? "Kh\u00F4ng t\u00ECm th\u1EA5y nh\u00E2n vi\u00EAn n\u00E0o"
                : "Hi\u1EC3n th\u1ECB " + (start + 1) + " \u2013 " + end + " / " + totalRecords + " nh\u00E2n vi\u00EAn");

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

    private static class NhanVienTableModel extends AbstractTableModel {
        private final String[] COLUMNS = {"M\u00E3 NV", "H\u1ECD v\u00E0 t\u00EAn", "B\u1ED9 ph\u1EADn", "S\u0110T", "Tr\u1EA1ng th\u00E1i", ""};
        private List<NhanVien> data = new ArrayList<>();

        void setData(List<NhanVien> data) {
            this.data = new ArrayList<>(data);
            fireTableDataChanged();
        }

        @Override public int getRowCount()    { return data.size(); }
        @Override public int getColumnCount() { return COLUMNS.length; }
        @Override public String getColumnName(int c) { return COLUMNS[c]; }
        @Override public boolean isCellEditable(int r, int c) { return c == 5; }

        @Override
        public Object getValueAt(int r, int c) {
            NhanVien nv = data.get(r);
            return switch (c) {
                case 0 -> nv.getMaNV();
                case 1 -> nv.getHoTen();
                case 2 -> nv.getVaiTro() != null ? nv.getVaiTro().toString() : "";
                case 3 -> nv.getSoDienThoai() != null ? nv.getSoDienThoai() : "";
                case 4 -> nv.getTrangThai() != null ? nv.getTrangThai() : TrangThaiNhanVien.DANG_LAM;
                case 5 -> "Ch\u1EC9nh s\u1EEDa";
                default -> "";
            };
        }

        NhanVien getNhanVienAt(int r) {
            return (r >= 0 && r < data.size()) ? data.get(r) : null;
        }
    }

    // =================================================================
    //  CELL RENDERERS
    // =================================================================

    /** Generic row renderer with hover + zebra stripe */
    private class RowCellRenderer extends DefaultTableCellRenderer {
        private final Font font;
        private final Color fg;

        RowCellRenderer(Font font, Color fg, int align) {
            this.font = font;
            this.fg = fg;
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

    /** Badge renderer for TrangThaiNhanVien */
    private class BadgeCellRenderer extends JPanel implements TableCellRenderer {
        private final JLabel badge = new JLabel();
        private Color badgeBg = OUTLINE;
        private Color badgeFg = ON_SURF_VAR;

        BadgeCellRenderer() {
            setLayout(new GridBagLayout()); // centers the badge vertically & horizontally
            setOpaque(true);
            badge.setFont(FONT_BADGE);
            badge.setHorizontalAlignment(SwingConstants.CENTER);
            badge.setOpaque(false);
            add(badge);
        }

        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object value,
                boolean isSel, boolean hasFocus, int row, int col) {
            TrangThaiNhanVien tt = (value instanceof TrangThaiNhanVien) ? (TrangThaiNhanVien) value : TrangThaiNhanVien.DANG_LAM;

            switch (tt) {
                case DANG_LAM  -> { badgeBg = STATUS_GREEN_BG;  badgeFg = STATUS_GREEN_FG; }
                case NGHI_PHEP -> { badgeBg = STATUS_ORANGE_BG; badgeFg = STATUS_ORANGE_FG; }
                case DA_NGHI   -> { badgeBg = STATUS_RED_BG;    badgeFg = STATUS_RED_FG; }
            }

            badge.setText(tt.toString());
            badge.setForeground(badgeFg);
            setBackground(getRowBg(tbl, isSel, row));
            return this;
        }

        @Override
        protected void paintChildren(Graphics g) {
            // Paint rounded badge background behind label text
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

    /** Edit button renderer */
    private class EditButtonRenderer extends JPanel implements TableCellRenderer {
        private final JLabel lbl = new JLabel();

        EditButtonRenderer() {
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
            lbl.setText(value != null ? value.toString() : "Ch\u1EC9nh s\u1EEDa");
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

    /** Edit button editor (handles clicks) */
    private class EditButtonEditor extends AbstractCellEditor implements TableCellEditor {
        private final JButton button;
        private int editingRow;

        EditButtonEditor() {
            button = new JButton("Ch\u1EC9nh s\u1EEDa");
            button.setFont(FONT_BADGE);
            button.setForeground(PRIMARY);
            button.setBackground(PRIMARY_LIGHT);
            button.setBorderPainted(false);
            button.setFocusPainted(false);
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            button.addActionListener(e -> {
                fireEditingStopped();
                NhanVien nv = tableModel.getNhanVienAt(editingRow);
                if (nv != null) onEditNhanVien(nv);
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable tbl, Object value,
                boolean isSel, int row, int col) {
            editingRow = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() { return "Ch\u1EC9nh s\u1EEDa"; }
    }

    /** Row background helper: hover > selected > zebra */
    private Color getRowBg(JTable tbl, boolean isSel, int row) {
        if (isSel) return PRIMARY_LIGHT;
        if (row == hoveredRow) return ROW_HOVER;
        return row % 2 == 0 ? CARD_BG : ROW_ALT;
    }

    // =================================================================
    //  EDIT ACTION (placeholder)
    // =================================================================

    private void onEditNhanVien(NhanVien nv) {
        // TODO: Implement edit employee module
        JOptionPane.showMessageDialog(this,
                "Ch\u1EC9nh s\u1EEDa nh\u00E2n vi\u00EAn: " + nv.getMaNV() + " - " + nv.getHoTen(),
                "Ch\u1EC9nh s\u1EEDa", JOptionPane.INFORMATION_MESSAGE);
    }

    // =================================================================
    //  AppModule interface
    // =================================================================

    @Override public String getTitle() { return "Qu\u1EA3n l\u00FD nh\u00E2n vi\u00EAn"; }
    @Override public JPanel getView()  { return this; }
    @Override public void setOnResult(Consumer<Object> cb) {
        this.callback = cb;
        boolean has = (cb != null);
        btnSubmit.setVisible(has);
        btnCancel.setVisible(has);
        btnPanel.setVisible(has);
    }
    @Override public void reset() {
        txtSearch.setText("");
        cboVaiTro.setSelectedIndex(0);
        cboTrangThai.setSelectedIndex(0);
        currentPage = 1;
        loadData();
    }
}
