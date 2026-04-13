package com.modules;

import com.dao.DAO_Ghe;
import com.dao.DAO_Ve;
import com.entity.Ghe;
import com.entity.Lich;
import com.entity.ToaTau;
import com.entity.Ve;
import com.enums.LoaiGhe;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Bước 4 — Chọn ghế.
 * Input: Lich, ToaTau.
 * Output: List&lt;Ghe&gt; các ghế đã chọn.
 */
public class BanVeStep4Module extends JPanel implements AppModule {

    private Consumer<Object> callback;

    private final Lich   lich;
    private final ToaTau toa;

    private final DAO_Ghe daoGhe = new DAO_Ghe();
    private final DAO_Ve  daoVe  = new DAO_Ve();

    private List<Ghe>    allGhes    = new ArrayList<>();
    private Set<String>  soldMaGhe  = new HashSet<>();
    private final Set<String> selectedMaGhe = new LinkedHashSet<>();

    // Design tokens
    private static final Color PRIMARY       = new Color(0x00, 0x5D, 0x90);
    private static final Color SURFACE       = new Color(0xF8, 0xFA, 0xFC);
    private static final Color CARD_BG       = Color.WHITE;
    private static final Color ON_SURFACE    = new Color(0x1A, 0x1D, 0x21);
    private static final Color ON_SURF_VAR   = new Color(0x5F, 0x67, 0x70);
    private static final Color OUTLINE       = new Color(0xDE, 0xE3, 0xE8);
    private static final Color PRIMARY_LIGHT = new Color(0xE3, 0xF2, 0xFD);

    // Seat colors
    private static final Color SEAT_AVAIL_BG     = Color.WHITE;
    private static final Color SEAT_AVAIL_BORDER  = new Color(0xB0, 0xBE, 0xC5);
    private static final Color SEAT_SOLD_BG       = new Color(0xEE, 0xEE, 0xEE);
    private static final Color SEAT_SOLD_BORDER   = new Color(0xBD, 0xBD, 0xBD);
    private static final Color SEAT_SEL_BG        = new Color(0x00, 0x5D, 0x90);
    private static final Color SEAT_SEL_BORDER    = new Color(0x00, 0x3D, 0x66);
    private static final Color SEAT_HOVER_BG      = new Color(0xD0, 0xEA, 0xFF);

    // Canvas layout
    private static final int SEAT_W     = 44;
    private static final int SEAT_H     = 44;
    private static final int SEAT_GAP   = 6;
    private static final int KHOANG_GAP = 20;
    private static final int PADDING    = 28;

    private SeatMapCanvas canvas;
    private JLabel        lblCounter;
    private JButton       btnSubmit, btnCancel;
    private JPanel        btnPanel;

    // =========================================================================
    //  CONSTRUCTOR
    // =========================================================================

    public BanVeStep4Module(Lich lich, ToaTau toa) {
        this.lich = lich;
        this.toa  = toa;
        setLayout(new BorderLayout());
        setBackground(SURFACE);
        loadData();
        buildUI();
    }

    // =========================================================================
    //  BUILD UI
    // =========================================================================

    private void buildUI() {
        add(buildInfoBar(),    BorderLayout.NORTH);
        add(buildMapPanel(),   BorderLayout.CENTER);

        btnSubmit = new JButton("Chọn ghế này →");
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
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(PRIMARY_LIGHT);
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, OUTLINE));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 10));
        left.setBackground(PRIMARY_LIGHT);

        JLabel toaLbl = new JLabel("Toa: " + toa.getMaToaTau());
        toaLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        toaLbl.setForeground(PRIMARY);

        JLabel typeLbl = new JLabel("  ·  " + (toa.getLoaiGhe() != null ? toa.getLoaiGhe().toString() : ""));
        typeLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        typeLbl.setForeground(ON_SURF_VAR);

        left.add(toaLbl);
        left.add(typeLbl);

        // Legend
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 10));
        legend.setBackground(PRIMARY_LIGHT);
        legend.add(legendItem(SEAT_AVAIL_BG, SEAT_AVAIL_BORDER, "Còn trống"));
        legend.add(legendItem(SEAT_SOLD_BG,  SEAT_SOLD_BORDER,  "Đã bán"));
        legend.add(legendItem(SEAT_SEL_BG,   SEAT_SEL_BORDER,   "Đang chọn"));

        bar.add(left,   BorderLayout.WEST);
        bar.add(legend, BorderLayout.EAST);
        return bar;
    }

    private JPanel legendItem(Color bg, Color border, String text) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        item.setOpaque(false);

        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 4, 4);
                g2.setColor(border);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 4, 4);
                g2.dispose();
            }
        };
        dot.setPreferredSize(new Dimension(14, 14));
        dot.setOpaque(false);

        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(ON_SURF_VAR);

        item.add(dot);
        item.add(lbl);
        return item;
    }

    private JPanel buildMapPanel() {
        JPanel wrap = new JPanel(new BorderLayout(0, 0));
        wrap.setBackground(SURFACE);
        wrap.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Counter bar
        JPanel counterBar = new JPanel(new BorderLayout());
        counterBar.setBackground(SURFACE);
        counterBar.setBorder(new EmptyBorder(0, 0, 12, 0));

        JLabel titleLbl = new JLabel("Sơ đồ ghế — nhấp để chọn");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLbl.setForeground(ON_SURFACE);

        lblCounter = new JLabel("Đã chọn: 0 ghế");
        lblCounter.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblCounter.setForeground(PRIMARY);

        counterBar.add(titleLbl,  BorderLayout.WEST);
        counterBar.add(lblCounter, BorderLayout.EAST);

        // Canvas
        canvas = new SeatMapCanvas();

        JScrollPane scroll = new JScrollPane(canvas);
        scroll.setBorder(BorderFactory.createLineBorder(OUTLINE, 1));
        scroll.getViewport().setBackground(CARD_BG);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        wrap.add(counterBar, BorderLayout.NORTH);
        wrap.add(scroll,     BorderLayout.CENTER);
        return wrap;
    }

    // =========================================================================
    //  LOAD DATA
    // =========================================================================

    private void loadData() {
        allGhes   = daoGhe.findByToaTau(toa.getMaToaTau());
        soldMaGhe = new HashSet<>();
        List<Ve> ves = daoVe.findByLich(lich.getMaLich());
        for (Ve ve : ves) {
            if (ve.getGhe() != null &&
                toa.getMaToaTau().equals(ve.getGhe().getToaTau() != null
                    ? ve.getGhe().getToaTau().getMaToaTau() : null)) {
                soldMaGhe.add(ve.getGhe().getMaGhe());
            }
        }
    }

    // =========================================================================
    //  EXECUTE
    // =========================================================================

    private void execute() {
        List<Ghe> selected = getSelectedGhes();
        if (selected.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất một ghế.");
            return;
        }
        if (callback != null) callback.accept(selected);
    }

    private List<Ghe> getSelectedGhes() {
        Map<String, Ghe> gheMap = allGhes.stream()
            .collect(Collectors.toMap(Ghe::getMaGhe, g -> g));
        return selectedMaGhe.stream()
            .map(gheMap::get)
            .filter(Objects::nonNull)
            .sorted(Comparator.comparingInt(Ghe::getSoGhe))
            .collect(Collectors.toList());
    }

    private void updateCounter() {
        int n = selectedMaGhe.size();
        lblCounter.setText("Đã chọn: " + n + " ghế");
        if (btnSubmit != null) btnSubmit.setEnabled(n > 0);
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
    //  SEAT MAP CANVAS
    // =========================================================================

    class SeatMapCanvas extends JPanel {

        private final List<Ghe> sorted;
        private final int gridCols;
        private final int khoangCols;
        private final Map<String, Rectangle> rects = new LinkedHashMap<>();
        private String hoveredMaGhe = null;

        SeatMapCanvas() {
            LoaiGhe loai = toa.getLoaiGhe();
            this.gridCols   = (loai == LoaiGhe.GIUONG_NAM) ? 10 : 12;
            this.khoangCols = (loai == LoaiGhe.GIUONG_NAM) ?  2 :  6;
            this.sorted = allGhes.stream()
                .sorted(Comparator.comparingInt(Ghe::getSoGhe))
                .collect(Collectors.toList());

            setBackground(CARD_BG);
            computeRects();
            setPreferredSize(calcPreferredSize());

            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    String hit = hitTest(e.getPoint());
                    if (hit == null || soldMaGhe.contains(hit)) return;
                    // TODO: GiuCho mechanism — currently just toggle selection
                    if (selectedMaGhe.contains(hit)) selectedMaGhe.remove(hit);
                    else selectedMaGhe.add(hit);
                    repaint();
                    updateCounter();
                }
            });
            addMouseMotionListener(new MouseAdapter() {
                @Override public void mouseMoved(MouseEvent e) {
                    String prev = hoveredMaGhe;
                    hoveredMaGhe = hitTest(e.getPoint());
                    if (!Objects.equals(prev, hoveredMaGhe)) repaint();
                    setCursor(hoveredMaGhe != null && !soldMaGhe.contains(hoveredMaGhe)
                        ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        : Cursor.getDefaultCursor());
                }
                @Override public void mouseExited(MouseEvent e) {
                    hoveredMaGhe = null; repaint();
                }
            });
        }

        private void computeRects() {
            rects.clear();
            for (int i = 0; i < sorted.size(); i++) {
                Ghe ghe = sorted.get(i);
                int row = i / gridCols;
                int col = i % gridCols;
                int khoangOffset = (col / khoangCols) * (KHOANG_GAP - SEAT_GAP);
                int x = PADDING + col * (SEAT_W + SEAT_GAP) + khoangOffset;
                int y = PADDING + row * (SEAT_H + SEAT_GAP);
                rects.put(ghe.getMaGhe(), new Rectangle(x, y, SEAT_W, SEAT_H));
            }
        }

        private Dimension calcPreferredSize() {
            int numKhoang = (gridCols + khoangCols - 1) / khoangCols;
            int numRows   = sorted.isEmpty() ? 1 : (sorted.size() + gridCols - 1) / gridCols;
            int w = PADDING * 2
                + gridCols   * (SEAT_W + SEAT_GAP) - SEAT_GAP
                + (numKhoang - 1) * (KHOANG_GAP - SEAT_GAP);
            int h = PADDING * 2
                + numRows * (SEAT_H + SEAT_GAP) - SEAT_GAP;
            return new Dimension(w, h);
        }

        private String hitTest(Point p) {
            for (Map.Entry<String, Rectangle> e : rects.entrySet()) {
                if (e.getValue().contains(p)) return e.getKey();
            }
            return null;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            Map<String, Ghe> gheMap = sorted.stream()
                .collect(Collectors.toMap(Ghe::getMaGhe, ge -> ge));

            Font seatFont = new Font("Segoe UI", Font.BOLD, 11);
            g2.setFont(seatFont);
            FontMetrics fm = g2.getFontMetrics(seatFont);

            for (Map.Entry<String, Rectangle> entry : rects.entrySet()) {
                String    maGhe = entry.getKey();
                Rectangle rect  = entry.getValue();
                Ghe       ghe   = gheMap.get(maGhe);

                boolean sold     = soldMaGhe.contains(maGhe);
                boolean selected = selectedMaGhe.contains(maGhe);
                boolean hovered  = maGhe.equals(hoveredMaGhe) && !sold;

                Color bg     = sold ? SEAT_SOLD_BG
                    : selected ? SEAT_SEL_BG
                    : hovered  ? SEAT_HOVER_BG
                    : SEAT_AVAIL_BG;
                Color border = sold ? SEAT_SOLD_BORDER
                    : selected ? SEAT_SEL_BORDER
                    : SEAT_AVAIL_BORDER;
                Color fg     = sold ? new Color(0x9E, 0x9E, 0x9E)
                    : selected ? Color.WHITE
                    : ON_SURFACE;

                g2.setColor(bg);
                g2.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 8, 8);

                g2.setColor(border);
                g2.setStroke(selected
                    ? new BasicStroke(2f) : new BasicStroke(1f));
                g2.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 8, 8);
                g2.setStroke(new BasicStroke(1f));

                if (ghe != null) {
                    String label = String.valueOf(ghe.getSoGhe());
                    int tx = rect.x + (rect.width  - fm.stringWidth(label)) / 2;
                    int ty = rect.y + (rect.height + fm.getAscent() - fm.getDescent()) / 2;
                    g2.setColor(fg);
                    g2.drawString(label, tx, ty);
                }
            }

            // Khoang divider lines
            g2.setColor(OUTLINE);
            g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 1f, new float[]{4, 4}, 0f));

            int numKhoang = gridCols / khoangCols;
            int numRows   = sorted.isEmpty() ? 0
                : (sorted.size() + gridCols - 1) / gridCols;

            for (int k = 1; k < numKhoang; k++) {
                int col = k * khoangCols;
                int khoangOffset = (col / khoangCols - 1) * (KHOANG_GAP - SEAT_GAP);
                int lineX = PADDING + col * (SEAT_W + SEAT_GAP) + khoangOffset
                    - SEAT_GAP / 2 - (KHOANG_GAP - SEAT_GAP) / 2;
                int y1 = PADDING - 4;
                int y2 = PADDING + numRows * (SEAT_H + SEAT_GAP) - SEAT_GAP + 4;
                g2.drawLine(lineX, y1, lineX, y2);
            }

            g2.dispose();
        }
    }

    // =========================================================================
    //  AppModule
    // =========================================================================

    @Override public String getTitle() { return "Bước 4 – Chọn ghế"; }
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
        selectedMaGhe.clear();
        updateCounter();
        if (canvas != null) canvas.repaint();
    }
}
