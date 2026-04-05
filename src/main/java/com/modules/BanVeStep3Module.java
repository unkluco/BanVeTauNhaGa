package com.modules;

import com.dao.*;
import com.entity.*;
import com.enums.LoaiGhe;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Bước 3 — Chọn toa tàu.
 * Input: Lich đã chọn.
 * Output: ToaTau được chọn.
 */
public class BanVeStep3Module extends JPanel implements AppModule {

    private Consumer<Object> callback;
    private final Lich lich;

    private final DAO_ChiTietDoanTau daoChiTietDT = new DAO_ChiTietDoanTau();
    private final DAO_Ghe            daoGhe        = new DAO_Ghe();
    private final DAO_Ve             daoVe         = new DAO_Ve();

    private final List<ToaRow> rows = new ArrayList<>();
    private int selectedRow = -1;
    private int hoveredRow  = -1;

    // Design tokens
    private static final Color PRIMARY       = new Color(0x00, 0x5D, 0x90);
    private static final Color SURFACE       = new Color(0xF8, 0xFA, 0xFC);
    private static final Color CARD_BG       = Color.WHITE;
    private static final Color ON_SURFACE    = new Color(0x1A, 0x1D, 0x21);
    private static final Color ON_SURF_VAR   = new Color(0x5F, 0x67, 0x70);
    private static final Color OUTLINE       = new Color(0xDE, 0xE3, 0xE8);
    private static final Color ROW_ALT       = new Color(0xF8, 0xFA, 0xFC);
    private static final Color ROW_HOVER     = new Color(0xEE, 0xF5, 0xFB);
    private static final Color ROW_SELECTED  = new Color(0xD0, 0xEA, 0xFF);
    private static final Color PRIMARY_LIGHT = new Color(0xE3, 0xF2, 0xFD);
    private static final Color WARN_COLOR    = new Color(0xE6, 0x5C, 0x00);

    private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD,  13);
    private static final Font FONT_BODY   = new Font("Segoe UI", Font.PLAIN, 13);

    private JTable          table;
    private ToaTableModel   tableModel;
    private JButton         btnSubmit, btnCancel;
    private JPanel          btnPanel;
    private JLabel          lblNoResult;

    record ToaRow(ChiTietDoanTau ctdt, int tongGhe, int conTrong) {}

    // =========================================================================
    //  CONSTRUCTOR
    // =========================================================================

    public BanVeStep3Module(Lich lich) {
        this.lich = lich;
        setLayout(new BorderLayout());
        setBackground(SURFACE);
        buildUI();
        loadData();
    }

    // =========================================================================
    //  BUILD UI
    // =========================================================================

    private void buildUI() {
        add(buildInfoBar(), BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);

        btnSubmit = new JButton("Chọn toa này →");
        styleBtn(btnSubmit, true);
        btnSubmit.setEnabled(false);
        btnSubmit.addActionListener(e -> execute());

        btnCancel = new JButton("← Quay lại");
        styleBtn(btnCancel, false);
        btnCancel.addActionListener(e -> { if (callback != null) callback.accept(null); });

        btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        btnPanel.setBackground(SURFACE);
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, OUTLINE));
        btnPanel.add(btnCancel);
        btnPanel.add(btnSubmit);
        btnPanel.setVisible(false);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private JPanel buildInfoBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 10));
        bar.setBackground(PRIMARY_LIGHT);
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, OUTLINE));

        JLabel lichLbl = new JLabel("Lịch: " + lich.getMaLich());
        lichLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lichLbl.setForeground(PRIMARY);

        String dtText = lich.getDoanTau() != null ? "  ·  Đoàn tàu: " + lich.getDoanTau().getMaDoanTau() : "";
        JLabel dtLbl = new JLabel(dtText);
        dtLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dtLbl.setForeground(ON_SURF_VAR);

        bar.add(lichLbl);
        bar.add(dtLbl);
        return bar;
    }

    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(SURFACE);
        panel.setBorder(new EmptyBorder(16, 20, 0, 20));

        JLabel title = new JLabel("Chọn toa");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(ON_SURFACE);
        title.setBorder(new EmptyBorder(0, 0, 6, 0));

        String[] cols = {"STT", "Mã toa", "Loại ghế", "Tổng ghế", "Còn trống"};
        tableModel = new ToaTableModel(cols);
        table = new JTable(tableModel);
        table.setFont(FONT_BODY);
        table.setRowHeight(50);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBackground(CARD_BG);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader th = table.getTableHeader();
        th.setFont(FONT_HEADER);
        th.setBackground(new Color(0xF0, 0xF4, 0xF8));
        th.setForeground(ON_SURFACE);
        th.setReorderingAllowed(false);
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, OUTLINE));
        ((DefaultTableCellRenderer) th.getDefaultRenderer())
            .setHorizontalAlignment(SwingConstants.LEFT);

        int[] widths = {60, 160, 200, 110, 110};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        table.setDefaultRenderer(Object.class, new RowRenderer());
        table.getColumnModel().getColumn(2).setCellRenderer(new LoaiGheRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new AvailRenderer());

        table.addMouseMotionListener(new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                int r = table.rowAtPoint(e.getPoint());
                if (r != hoveredRow) { hoveredRow = r; table.repaint(); }
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseExited(MouseEvent e) { hoveredRow = -1; table.repaint(); }
            @Override public void mouseClicked(MouseEvent e) {
                int r = table.rowAtPoint(e.getPoint());
                if (r >= 0 && r < rows.size() && rows.get(r).conTrong() > 0) {
                    selectedRow = (r == selectedRow) ? -1 : r;
                } else {
                    selectedRow = -1;
                }
                table.repaint();
                btnSubmit.setEnabled(selectedRow >= 0);
            }
        });

        lblNoResult = new JLabel("Không có toa nào trong đoàn tàu này.");
        lblNoResult.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblNoResult.setForeground(ON_SURF_VAR);
        lblNoResult.setHorizontalAlignment(SwingConstants.CENTER);
        lblNoResult.setBorder(new EmptyBorder(20, 0, 20, 0));
        lblNoResult.setVisible(false);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(OUTLINE, 1));
        scroll.getViewport().setBackground(CARD_BG);

        panel.add(title,     BorderLayout.NORTH);
        panel.add(scroll,    BorderLayout.CENTER);
        panel.add(lblNoResult, BorderLayout.SOUTH);
        return panel;
    }

    // =========================================================================
    //  LOAD DATA
    // =========================================================================

    private void loadData() {
        rows.clear();
        if (lich.getDoanTau() == null) { tableModel.setData(rows); return; }

        Set<String> soldMaGhe = new HashSet<>();
        daoVe.findByLich(lich.getMaLich()).forEach(v -> {
            if (v.getGhe() != null) soldMaGhe.add(v.getGhe().getMaGhe());
        });

        List<ChiTietDoanTau> ctdtList = daoChiTietDT.findByDoanTau(
            lich.getDoanTau().getMaDoanTau());

        for (ChiTietDoanTau ctdt : ctdtList) {
            List<Ghe> ghes = daoGhe.findByToaTau(ctdt.getToaTau().getMaToaTau());
            int tong     = ghes.size();
            int conTrong = (int) ghes.stream()
                .filter(g -> !soldMaGhe.contains(g.getMaGhe())).count();
            rows.add(new ToaRow(ctdt, tong, conTrong));
        }

        tableModel.setData(rows);
        lblNoResult.setVisible(rows.isEmpty());
    }

    // =========================================================================
    //  EXECUTE
    // =========================================================================

    private void execute() {
        if (selectedRow < 0 || selectedRow >= rows.size()) return;
        ToaTau toa = rows.get(selectedRow).ctdt().getToaTau();
        if (callback != null) callback.accept(toa);
    }

    // =========================================================================
    //  TABLE MODEL
    // =========================================================================

    class ToaTableModel extends AbstractTableModel {
        private final String[]    cols;
        private final List<ToaRow> data = new ArrayList<>();

        ToaTableModel(String[] cols) { this.cols = cols; }

        void setData(List<ToaRow> list) {
            data.clear(); data.addAll(list); fireTableDataChanged();
        }

        @Override public int     getRowCount()                { return data.size(); }
        @Override public int     getColumnCount()             { return cols.length; }
        @Override public String  getColumnName(int c)         { return cols[c]; }
        @Override public boolean isCellEditable(int r, int c) { return false; }

        @Override
        public Object getValueAt(int r, int c) {
            ToaRow row = data.get(r);
            return switch (c) {
                case 0 -> row.ctdt().getSoThuTu();
                case 1 -> row.ctdt().getToaTau().getMaToaTau();
                case 2 -> row.ctdt().getToaTau().getLoaiGhe();
                case 3 -> row.tongGhe();
                case 4 -> row.conTrong();
                default -> "";
            };
        }
    }

    // =========================================================================
    //  RENDERERS
    // =========================================================================

    private Color rowBg(int r) {
        if (r == selectedRow) return ROW_SELECTED;
        if (r == hoveredRow)  return ROW_HOVER;
        return r % 2 == 0 ? CARD_BG : ROW_ALT;
    }

    class RowRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
            lbl.setFont(FONT_BODY);
            lbl.setBorder(new EmptyBorder(0, 12, 0, 12));
            lbl.setBackground(rowBg(r));
            boolean full = r < rows.size() && rows.get(r).conTrong() == 0;
            lbl.setForeground(full ? ON_SURF_VAR : ON_SURFACE);
            return lbl;
        }
    }

    class LoaiGheRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            super.getTableCellRendererComponent(t, v, sel, foc, r, c);
            if (!(v instanceof LoaiGhe loai)) { setText(""); setBackground(rowBg(r)); return this; }

            Color badgeBg = switch (loai) {
                case GHE_CUNG   -> new Color(0x17, 0x65, 0xC0);
                case GHE_MEM    -> new Color(0x2E, 0x7D, 0x32);
                case GIUONG_NAM -> new Color(0x6A, 0x1B, 0x9A);
            };

            JPanel cell = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 9));
            cell.setBackground(rowBg(r));

            JLabel badge = new JLabel(loai.toString()) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(badgeBg);
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            badge.setOpaque(false);
            badge.setForeground(Color.WHITE);
            badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
            badge.setBorder(new EmptyBorder(3, 10, 3, 10));

            cell.add(badge);
            return cell;
        }
    }

    class AvailRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lbl.setBorder(new EmptyBorder(0, 12, 0, 12));
            lbl.setBackground(rowBg(r));
            int avail = v instanceof Integer i ? i : 0;
            lbl.setForeground(avail == 0 ? WARN_COLOR : new Color(0x1A, 0x7A, 0x3C));
            return lbl;
        }
    }

    // =========================================================================
    //  HELPERS
    // =========================================================================

    private void styleBtn(JButton btn, boolean primary) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 24, 10, 24));
        if (primary) {
            btn.setBackground(PRIMARY); btn.setForeground(Color.WHITE); btn.setOpaque(true);
        } else {
            btn.setBackground(CARD_BG); btn.setForeground(ON_SURF_VAR); btn.setOpaque(true);
        }
    }

    // =========================================================================
    //  AppModule
    // =========================================================================

    @Override public String getTitle() { return "Bước 3 – Chọn toa"; }
    @Override public JPanel getView()  { return this; }

    @Override
    public void setOnResult(Consumer<Object> cb) {
        this.callback = cb;
        boolean show = cb != null;
        btnSubmit.setVisible(show);
        btnCancel.setVisible(show);
        btnPanel.setVisible(show);
    }

    @Override
    public void reset() {
        selectedRow = -1;
        table.repaint();
        btnSubmit.setEnabled(false);
    }
}
