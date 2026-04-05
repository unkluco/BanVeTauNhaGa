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
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Bước 2 — Chọn lịch tàu chạy.
 * Hiển thị các lịch thỏa mãn + giá từng loại ghế trên cùng hàng.
 * Output: Object[]{ Lich selectedLich, Map<LoaiGhe, Double> priceMap }
 */
public class BanVeStep2Module extends JPanel implements AppModule {

    private Consumer<Object> callback;

    // Input
    private final Ga        gaDi, gaDen;
    private final LocalDate ngayDi;

    // DAOs
    private final DAO_Tuyen      daoTuyen = new DAO_Tuyen();
    private final DAO_Lich       daoLich  = new DAO_Lich();
    private final DAO_Gia        daoGia   = new DAO_Gia();
    private final DAO_ChiTietGia daoCTGia = new DAO_ChiTietGia();

    // Data
    private final List<LichRow> rows = new ArrayList<>();
    private int selectedRow = -1;
    private int hoveredRow  = -1;

    // Formatters
    private static final NumberFormat      VND_FMT = NumberFormat.getInstance(new Locale("vi", "VN"));
    private static final DateTimeFormatter DT_FMT  = DateTimeFormatter.ofPattern("HH:mm  dd/MM");

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
    private static final Color AMOUNT_COLOR  = new Color(0x1A, 0x7A, 0x3C);
    private static final Color PRIMARY_LIGHT = new Color(0xE3, 0xF2, 0xFD);

    private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD,  13);
    private static final Font FONT_BODY   = new Font("Segoe UI", Font.PLAIN, 13);

    // Table
    private JTable         table;
    private LichTableModel tableModel;
    private JLabel         lblNoResult;

    // AppModule buttons
    private JButton btnSubmit, btnCancel;
    private JPanel  btnPanel;

    // DTO
    record LichRow(Lich lich, Map<LoaiGhe, Double> priceMap) {}

    // =========================================================================
    //  CONSTRUCTOR
    // =========================================================================

    public BanVeStep2Module(Ga gaDi, Ga gaDen, LocalDate ngayDi) {
        this.gaDi   = gaDi;
        this.gaDen  = gaDen;
        this.ngayDi = ngayDi;
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

        btnSubmit = new JButton("Chọn chuyến này →");
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

        JLabel route = new JLabel(gaDi.getTenGa() + "  →  " + gaDen.getTenGa());
        route.setFont(new Font("Segoe UI", Font.BOLD, 15));
        route.setForeground(PRIMARY);

        JLabel date = new JLabel("Ngày: " + ngayDi.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        date.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        date.setForeground(ON_SURF_VAR);

        bar.add(route);
        bar.add(new JLabel("  ·  "));
        bar.add(date);
        return bar;
    }

    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(SURFACE);
        panel.setBorder(new EmptyBorder(16, 20, 0, 20));

        JLabel sectionTitle = new JLabel("Các chuyến tàu phù hợp");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        sectionTitle.setForeground(ON_SURFACE);
        sectionTitle.setBorder(new EmptyBorder(0, 0, 6, 0));

        String[] cols = {
            "Giờ đi", "Mã lịch", "Đoàn tàu", "Thời gian",
            "Ghế cứng (₫)", "Ghế mềm (₫)", "Giường nằm (₫)"
        };
        tableModel = new LichTableModel(cols);
        table = new JTable(tableModel);
        table.setFont(FONT_BODY);
        table.setRowHeight(48);
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

        int[] widths = {110, 160, 140, 110, 130, 130, 130};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        table.setDefaultRenderer(Object.class, new RowRenderer());
        for (int i = 4; i <= 6; i++)
            table.getColumnModel().getColumn(i).setCellRenderer(new PriceRenderer());

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
                selectedRow = (r == selectedRow) ? -1 : r;
                table.repaint();
                btnSubmit.setEnabled(selectedRow >= 0);
            }
        });

        lblNoResult = new JLabel("Không tìm thấy chuyến tàu nào. Vui lòng chọn lại tuyến hoặc ngày.");
        lblNoResult.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblNoResult.setForeground(ON_SURF_VAR);
        lblNoResult.setHorizontalAlignment(SwingConstants.CENTER);
        lblNoResult.setBorder(new EmptyBorder(20, 0, 20, 0));
        lblNoResult.setVisible(false);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(OUTLINE, 1));
        scroll.getViewport().setBackground(CARD_BG);

        panel.add(sectionTitle, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(lblNoResult, BorderLayout.SOUTH);
        return panel;
    }

    // =========================================================================
    //  LOAD DATA
    // =========================================================================

    private void loadData() {
        rows.clear();
        List<Tuyen> tuyens = daoTuyen.findByGaDiGaDen(gaDi.getMaGa(), gaDen.getMaGa());
        Gia  giaHH = daoGia.getGiaHienHanh();

        LocalDateTime dayStart = ngayDi.atStartOfDay();
        LocalDateTime dayEnd   = ngayDi.atTime(23, 59, 59);

        for (Tuyen tuyen : tuyens) {
            List<Lich> lichs = daoLich.findByTuyenAndDate(tuyen.getMaTuyen(), dayStart, dayEnd);
            for (Lich lich : lichs) {
                Map<LoaiGhe, Double> priceMap = new EnumMap<>(LoaiGhe.class);
                if (giaHH != null) {
                    for (LoaiGhe loai : LoaiGhe.values()) {
                        ChiTietGia ctg = daoCTGia.findByGiaTuyenLoaiGhe(
                            giaHH.getMaGia(), tuyen.getMaTuyen(), loai.toDbValue());
                        if (ctg != null) priceMap.put(loai, ctg.getGiaNiemYet());
                    }
                }
                rows.add(new LichRow(lich, priceMap));
            }
        }

        tableModel.setData(rows);
        lblNoResult.setVisible(rows.isEmpty());
    }

    // =========================================================================
    //  EXECUTE
    // =========================================================================

    private void execute() {
        if (selectedRow < 0 || selectedRow >= rows.size()) return;
        LichRow row = rows.get(selectedRow);
        if (callback != null) callback.accept(new Object[]{ row.lich(), row.priceMap() });
    }

    // =========================================================================
    //  TABLE MODEL
    // =========================================================================

    class LichTableModel extends AbstractTableModel {
        private final String[] cols;
        private final List<LichRow> data = new ArrayList<>();

        LichTableModel(String[] cols) { this.cols = cols; }

        void setData(List<LichRow> list) {
            data.clear(); data.addAll(list); fireTableDataChanged();
        }

        @Override public int     getRowCount()              { return data.size(); }
        @Override public int     getColumnCount()           { return cols.length; }
        @Override public String  getColumnName(int c)       { return cols[c]; }
        @Override public boolean isCellEditable(int r, int c) { return false; }

        @Override
        public Object getValueAt(int r, int c) {
            LichRow row  = data.get(r);
            Lich    lich = row.lich();
            return switch (c) {
                case 0 -> lich.getThoiGianBatDau() != null
                    ? lich.getThoiGianBatDau().format(DT_FMT) : "";
                case 1 -> lich.getMaLich();
                case 2 -> lich.getDoanTau() != null ? lich.getDoanTau().getMaDoanTau() : "";
                case 3 -> lich.getThoiGianChay() != null ? lich.getThoiGianChay() + " phút" : "";
                case 4 -> row.priceMap().getOrDefault(LoaiGhe.GHE_CUNG,   0.0);
                case 5 -> row.priceMap().getOrDefault(LoaiGhe.GHE_MEM,    0.0);
                case 6 -> row.priceMap().getOrDefault(LoaiGhe.GIUONG_NAM, 0.0);
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
            lbl.setForeground(ON_SURFACE);
            lbl.setBackground(rowBg(r));
            return lbl;
        }
    }

    class PriceRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            double amt  = v instanceof Double d ? d : 0.0;
            String text = amt > 0 ? VND_FMT.format((long) amt) + " ₫" : "—";
            JLabel lbl  = (JLabel) super.getTableCellRendererComponent(t, text, sel, foc, r, c);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lbl.setForeground(amt > 0 ? AMOUNT_COLOR : ON_SURF_VAR);
            lbl.setHorizontalAlignment(SwingConstants.RIGHT);
            lbl.setBorder(new EmptyBorder(0, 8, 0, 16));
            lbl.setBackground(rowBg(r));
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

    @Override public String getTitle() { return "Bước 2 – Chọn chuyến"; }
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
