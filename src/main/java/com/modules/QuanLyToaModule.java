package com.modules;

import com.dao.DAO_DauMay;
import com.dao.DAO_Ghe;
import com.dao.DAO_ToaTau;
import com.entity.DauMay;
import com.entity.Ghe;
import com.entity.ToaTau;
import com.enums.LoaiGhe;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class QuanLyToaModule extends JPanel implements AppModule {

    private Consumer<Object> callback;

    // --- DAOs ---
    private final DAO_ToaTau  daoToa   = new DAO_ToaTau();
    private final DAO_Ghe     daoGhe   = new DAO_Ghe();
    private final DAO_DauMay  daoDauMay = new DAO_DauMay();

    // --- Data ---
    private final List<ToaTau> allToa   = new ArrayList<>();
    private final List<DauMay> allDauMay = new ArrayList<>();

    // --- Toa table ---
    private JTable         toaTable;
    private ToaTableModel  toaModel;
    private int            toaHoveredRow = -1;

    // --- DauMay table ---
    private JTable          dauMayTable;
    private DauMayTableModel dauMayModel;
    private int             dauMayHoveredRow = -1;

    // --- Design tokens ---
    private static final Color PRIMARY        = new Color(0x00, 0x5D, 0x90);
    private static final Color PRIMARY_LIGHT  = new Color(0xE3, 0xF2, 0xFD);
    private static final Color SURFACE        = new Color(0xF8, 0xFA, 0xFC);
    private static final Color CARD_BG        = Color.WHITE;
    private static final Color ON_SURFACE     = new Color(0x1A, 0x1D, 0x21);
    private static final Color ON_SURF_VAR    = new Color(0x5F, 0x67, 0x70);
    private static final Color OUTLINE        = new Color(0xDE, 0xE3, 0xE8);
    private static final Color ROW_HOVER      = new Color(0xEE, 0xF5, 0xFB);
    private static final Color ROW_ALT        = new Color(0xF8, 0xFA, 0xFC);

    // Màu theo loại ghế
    private static final Color COLOR_CUNG     = new Color(0xFF, 0xE0, 0xB2); // cam nhạt
    private static final Color COLOR_MEM      = new Color(0xB3, 0xE5, 0xFC); // xanh dương nhạt
    private static final Color COLOR_GIUONG   = new Color(0xC8, 0xE6, 0xC9); // xanh lá nhạt

    private static final Font  FONT_HEADER    = new Font("Segoe UI", Font.BOLD,  13);
    private static final Font  FONT_BODY      = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font  FONT_SECTION   = new Font("Segoe UI", Font.BOLD,  14);

    // --- AppModule buttons (standalone mode) ---
    private JButton btnSubmit;
    private JButton btnCancel;
    private JPanel  btnPanel;

    public QuanLyToaModule() {
        setLayout(new BorderLayout());
        setBackground(SURFACE);
        buildUI();
    }

    // =========================================================================
    //  BUILD UI
    // =========================================================================

    private void buildUI() {
        // Header
        JPanel header = buildHeader();

        // Body: 2 sections stacked vertically
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(SURFACE);
        body.setBorder(new EmptyBorder(0, 20, 16, 20));

        body.add(buildToaSection());
        body.add(Box.createVerticalStrut(20));
        body.add(buildDauMaySection());

        // Buttons (hidden in standalone mode)
        btnSubmit = new JButton("Xác nhận");
        btnCancel = new JButton("Hủy");
        btnCancel.addActionListener(e -> { if (callback != null) callback.accept(null); });
        btnPanel  = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btnPanel.setBackground(SURFACE);
        btnPanel.add(btnCancel);
        btnPanel.add(btnSubmit);
        btnPanel.setVisible(false);

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(SURFACE);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        add(header, BorderLayout.NORTH);
        add(scroll,  BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        loadData();
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, OUTLINE),
                new EmptyBorder(18, 24, 18, 24)));

        JLabel title = new JLabel("Quản lý Toa và Đầu máy");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(ON_SURFACE);

        JLabel sub = new JLabel("Danh sách toa tàu và đầu máy trong hệ thống");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(ON_SURF_VAR);

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBackground(CARD_BG);
        left.add(title);
        left.add(Box.createVerticalStrut(3));
        left.add(sub);

        p.add(left, BorderLayout.WEST);
        return p;
    }

    // ---- Toa section ----
    private JPanel buildToaSection() {
        JPanel card = createCard();

        // Section title + legend
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setBackground(CARD_BG);

        JLabel lbl = new JLabel("Danh sách Toa tàu");
        lbl.setFont(FONT_SECTION);
        lbl.setForeground(ON_SURFACE);

        JPanel legend = buildLegend();
        titleRow.add(lbl,    BorderLayout.WEST);
        titleRow.add(legend, BorderLayout.EAST);

        // Table
        String[] cols = {"Mã Toa", "Loại ghế", "Số ghế", "Chi tiết"};
        toaModel = new ToaTableModel(cols);
        toaTable = createStyledTable(toaModel);
        toaTable.setRowHeight(44);

        // Renderer: cột loại ghế có màu badge, cột chi tiết có nút
        toaTable.getColumnModel().getColumn(1).setCellRenderer(new LoaiGheBadgeRenderer());
        toaTable.getColumnModel().getColumn(3).setCellRenderer(new ButtonRenderer());
        toaTable.getColumnModel().getColumn(3).setCellEditor(new ButtonEditor(new JCheckBox(), toaTable));

        // Column widths
        toaTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        toaTable.getColumnModel().getColumn(1).setPreferredWidth(160);
        toaTable.getColumnModel().getColumn(2).setPreferredWidth(90);
        toaTable.getColumnModel().getColumn(3).setPreferredWidth(110);

        // Hover
        toaTable.addMouseMotionListener(new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                int row = toaTable.rowAtPoint(e.getPoint());
                if (row != toaHoveredRow) { toaHoveredRow = row; toaTable.repaint(); }
            }
        });
        toaTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseExited(MouseEvent e) { toaHoveredRow = -1; toaTable.repaint(); }
        });

        // Row color renderer
        toaTable.setDefaultRenderer(Object.class, new ToaRowRenderer());

        JScrollPane scroll = new JScrollPane(toaTable);
        scroll.setBorder(BorderFactory.createLineBorder(OUTLINE, 1));
        scroll.setPreferredSize(new Dimension(0, 180));

        card.add(titleRow, BorderLayout.NORTH);
        card.add(scroll,   BorderLayout.CENTER);
        return card;
    }

    // ---- DauMay section ----
    private JPanel buildDauMaySection() {
        JPanel card = createCard();

        JLabel lbl = new JLabel("Danh sách Đầu máy");
        lbl.setFont(FONT_SECTION);
        lbl.setForeground(ON_SURFACE);
        lbl.setBorder(new EmptyBorder(0, 0, 12, 0));

        String[] cols = {"Mã Đầu máy", "Tên Đầu máy"};
        dauMayModel = new DauMayTableModel(cols);
        dauMayTable = createStyledTable(dauMayModel);
        dauMayTable.setRowHeight(40);
        dauMayTable.setDefaultRenderer(Object.class, new DauMayRowRenderer());

        dauMayTable.getColumnModel().getColumn(0).setPreferredWidth(160);
        dauMayTable.getColumnModel().getColumn(1).setPreferredWidth(400);

        dauMayTable.addMouseMotionListener(new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                int row = dauMayTable.rowAtPoint(e.getPoint());
                if (row != dauMayHoveredRow) { dauMayHoveredRow = row; dauMayTable.repaint(); }
            }
        });
        dauMayTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseExited(MouseEvent e) { dauMayHoveredRow = -1; dauMayTable.repaint(); }
        });

        JScrollPane scroll = new JScrollPane(dauMayTable);
        scroll.setBorder(BorderFactory.createLineBorder(OUTLINE, 1));
        scroll.setPreferredSize(new Dimension(0, 220));

        card.add(lbl,    BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JPanel createCard() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1),
                new EmptyBorder(18, 18, 18, 18)));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        return p;
    }

    private JPanel buildLegend() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        p.setBackground(CARD_BG);
        p.add(legendDot(COLOR_CUNG,   "Ghế cứng"));
        p.add(legendDot(COLOR_MEM,    "Ghế mềm"));
        p.add(legendDot(COLOR_GIUONG, "Giường nằm"));
        return p;
    }

    private JPanel legendDot(Color c, String text) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setBackground(Color.WHITE);
        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setColor(c.darker());
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 6, 6);
            }
        };
        dot.setOpaque(false);
        dot.setPreferredSize(new Dimension(14, 14));
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(ON_SURF_VAR);
        p.add(dot);
        p.add(lbl);
        return p;
    }

    private JTable createStyledTable(AbstractTableModel model) {
        JTable t = new JTable(model);
        t.setFont(FONT_BODY);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setSelectionBackground(PRIMARY_LIGHT);
        t.setSelectionForeground(ON_SURFACE);
        t.setFillsViewportHeight(true);
        t.setBackground(CARD_BG);

        JTableHeader th = t.getTableHeader();
        th.setFont(FONT_HEADER);
        th.setBackground(new Color(0xF0, 0xF4, 0xF8));
        th.setForeground(ON_SURFACE);
        th.setReorderingAllowed(false);
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, OUTLINE));
        ((DefaultTableCellRenderer) th.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);

        return t;
    }

    // =========================================================================
    //  LOAD DATA
    // =========================================================================

    private void loadData() {
        allToa.clear();
        allToa.addAll(daoToa.getAll());
        toaModel.setData(allToa);

        allDauMay.clear();
        allDauMay.addAll(daoDauMay.getAll());
        dauMayModel.setData(allDauMay);
    }

    private int countGhe(String maToaTau) {
        return daoGhe.findByToaTau(maToaTau).size();
    }

    private void openChiTiet(int row) {
        if (row < 0 || row >= allToa.size()) return;
        ToaTau toa = allToa.get(row);
        Window owner = SwingUtilities.getWindowAncestor(this);
        JFrame frame = (owner instanceof JFrame) ? (JFrame) owner : null;
        ChiTietToaDialog dlg = new ChiTietToaDialog(frame, toa);
        dlg.setVisible(true);
    }

    // =========================================================================
    //  TABLE MODELS
    // =========================================================================

    class ToaTableModel extends AbstractTableModel {
        private final String[] cols;
        private final List<ToaTau> data = new ArrayList<>();

        ToaTableModel(String[] cols) { this.cols = cols; }

        void setData(List<ToaTau> list) {
            data.clear(); data.addAll(list); fireTableDataChanged();
        }

        @Override public int getRowCount()    { return data.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }
        @Override public boolean isCellEditable(int r, int c) { return c == 3; }

        @Override public Object getValueAt(int r, int c) {
            ToaTau toa = data.get(r);
            return switch (c) {
                case 0 -> toa.getMaToaTau();
                case 1 -> toa.getLoaiGhe() != null ? toa.getLoaiGhe().toString() : "";
                case 2 -> countGhe(toa.getMaToaTau());
                case 3 -> "Chi tiết";
                default -> "";
            };
        }
    }

    class DauMayTableModel extends AbstractTableModel {
        private final String[] cols;
        private final List<DauMay> data = new ArrayList<>();

        DauMayTableModel(String[] cols) { this.cols = cols; }

        void setData(List<DauMay> list) {
            data.clear(); data.addAll(list); fireTableDataChanged();
        }

        @Override public int getRowCount()    { return data.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }
        @Override public boolean isCellEditable(int r, int c) { return false; }

        @Override public Object getValueAt(int r, int c) {
            DauMay dm = data.get(r);
            return switch (c) {
                case 0 -> dm.getMaDauMay();
                case 1 -> dm.getTenDauMay();
                default -> "";
            };
        }
    }

    // =========================================================================
    //  SHARED ROW COLOR — dùng chung cho tất cả renderers của bảng Toa
    // =========================================================================

    /** Màu nền chuẩn cho một hàng trong bảng Toa (solid, không dùng alpha). */
    private Color toaRowBg(int row) {
        if (row < 0 || row >= allToa.size()) return CARD_BG;
        LoaiGhe lg = allToa.get(row).getLoaiGhe();
        if (lg == null) return CARD_BG;
        Color base = switch (lg) {
            case GHE_CUNG   -> COLOR_CUNG;
            case GHE_MEM    -> COLOR_MEM;
            case GIUONG_NAM -> COLOR_GIUONG;
        };
        // Hàng chẵn: màu gốc; hàng lẻ: tối nhẹ 6%
        Color normal = (row % 2 == 0) ? base : scaleBrightness(base, 0.94f);
        return (row == toaHoveredRow) ? scaleBrightness(normal, 0.91f) : normal;
    }

    private static Color scaleBrightness(Color c, float f) {
        return new Color(
                Math.min(255, Math.max(0, (int)(c.getRed()   * f))),
                Math.min(255, Math.max(0, (int)(c.getGreen() * f))),
                Math.min(255, Math.max(0, (int)(c.getBlue()  * f))));
    }

    // =========================================================================
    //  RENDERERS & EDITORS
    // =========================================================================

    /** Renderer hàng toa — tất cả cột dùng toaRowBg() */
    class ToaRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean sel, boolean focus, int row, int col) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, sel, focus, row, col);
            lbl.setFont(FONT_BODY);
            lbl.setBorder(new EmptyBorder(0, 12, 0, 12));
            lbl.setForeground(ON_SURFACE);
            if (!sel) lbl.setBackground(toaRowBg(row));
            return lbl;
        }
    }

    /** Badge renderer cho cột loại ghế — nền khớp toaRowBg() */
    class LoaiGheBadgeRenderer extends JPanel implements TableCellRenderer {
        private final JLabel badge = new JLabel();

        LoaiGheBadgeRenderer() {
            setLayout(new FlowLayout(FlowLayout.LEFT, 12, 7));
            setOpaque(true);
            badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
            badge.setOpaque(true);
            badge.setBorder(new EmptyBorder(3, 10, 3, 10));
            add(badge);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object value,
                boolean sel, boolean focus, int row, int col) {
            badge.setText(value != null ? value.toString() : "");

            if (!sel && row >= 0 && row < allToa.size()) {
                LoaiGhe lg = allToa.get(row).getLoaiGhe();
                if (lg != null) {
                    Color badgeColor = switch (lg) {
                        case GHE_CUNG   -> scaleBrightness(COLOR_CUNG,   0.72f);
                        case GHE_MEM    -> scaleBrightness(COLOR_MEM,    0.72f);
                        case GIUONG_NAM -> scaleBrightness(COLOR_GIUONG, 0.72f);
                    };
                    badge.setBackground(badgeColor);
                    badge.setForeground(Color.WHITE);
                }
                setBackground(toaRowBg(row));  // khớp hoàn toàn với ToaRowRenderer
            } else if (sel) {
                setBackground(PRIMARY_LIGHT);
                badge.setBackground(PRIMARY);
                badge.setForeground(Color.WHITE);
            }
            return this;
        }
    }

    /** Renderer hàng đầu máy */
    class DauMayRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean sel, boolean focus, int row, int col) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, sel, focus, row, col);
            lbl.setFont(FONT_BODY);
            lbl.setBorder(new EmptyBorder(0, 12, 0, 12));
            lbl.setForeground(ON_SURFACE);
            if (!sel) lbl.setBackground(row == dauMayHoveredRow ? ROW_HOVER : (row % 2 == 0 ? CARD_BG : ROW_ALT));
            return lbl;
        }
    }

    /** Renderer nút — nền ô khớp màu hàng */
    class ButtonRenderer extends JPanel implements TableCellRenderer {
        private final JButton btn = new JButton("Chi tiết");

        ButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 0, 6));
            setOpaque(true);
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            btn.setForeground(PRIMARY);
            btn.setBackground(PRIMARY_LIGHT);
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0xBB, 0xDE, 0xFB), 1),
                    new EmptyBorder(4, 14, 4, 14)));
            btn.setFocusPainted(false);
            btn.setOpaque(true);
            add(btn);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v,
                boolean sel, boolean focus, int row, int col) {
            setBackground(sel ? PRIMARY_LIGHT : toaRowBg(row));
            return this;
        }
    }

    /** Editor nút — kích hoạt openChiTiet khi click */
    class ButtonEditor extends DefaultCellEditor {
        private final JButton btn;
        private int editingRow = -1;

        ButtonEditor(JCheckBox cb, JTable table) {
            super(cb);
            btn = new JButton("Chi tiết");
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            btn.setForeground(PRIMARY);
            btn.setBackground(PRIMARY_LIGHT);
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0xBB, 0xDE, 0xFB), 1),
                    new EmptyBorder(4, 14, 4, 14)));
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> {
                fireEditingStopped();
                openChiTiet(editingRow);
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable t, Object v,
                boolean sel, int row, int col) {
            editingRow = row;
            return btn;
        }

        @Override public Object getCellEditorValue() { return "Chi tiết"; }
    }

    // =========================================================================
    //  AppModule interface
    // =========================================================================

    @Override public String getTitle() { return "Quản lý Toa và Đầu máy"; }
    @Override public JPanel getView()  { return this; }

    @Override
    public void setOnResult(Consumer<Object> cb) {
        this.callback = cb;
        boolean show = (cb != null);
        btnSubmit.setVisible(show);
        btnCancel.setVisible(show);
        btnPanel.setVisible(show);
    }

    @Override
    public void reset() { loadData(); }
}
