package com.modules;

import com.dao.*;
import com.entity.*;
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
 * Bước 3 Hợp nhất — Chọn Chỗ (Toa & Ghế).
 * Tầng trên: Sơ đồ đoàn tàu trực quan.
 * Tầng dưới: Sơ đồ ghế chi tiết của toa đang chọn.
 */
public class BanVeStep3Module extends JPanel implements AppModule {

    private Consumer<Object> callback;
    private final Lich lich;

    // DAOs
    private final DAO_ChiTietDoanTau daoChiTietDT = new DAO_ChiTietDoanTau();
    private final DAO_Ghe            daoGhe        = new DAO_Ghe();
    private final DAO_Ve             daoVe         = new DAO_Ve();

    // Data State
    private List<ChiTietDoanTau> ctdtList = new ArrayList<>();
    private final Map<String, List<Ghe>> wagonGheMap = new HashMap<>();
    private final Set<String> soldMaGhes = new HashSet<>();
    private final Set<Ghe> selectedGhes = new LinkedHashSet<>();
    private ChiTietDoanTau activeCTDT = null;

    // UI Components
    private JPanel trainContainer;
    private SeatMapCanvas seatCanvas;
    private JLabel lblStatus;
    private JButton btnSubmit, btnCancel;
    private JPanel btnPanel;

    // Design tokens
    private static final Color PRIMARY       = new Color(13, 110, 253);
    private static final Color PRIMARY_LIGHT = new Color(245, 248, 255);
    private static final Color SURFACE       = new Color(248, 249, 250);
    private static final Color CARD_BG       = Color.WHITE;
    private static final Color TEXT_MAIN     = new Color(33, 37, 41);
    private static final Color TEXT_MUTED    = new Color(108, 117, 125);
    private static final Color OUTLINE       = new Color(222, 226, 230);
    
    // Seat Colors
    private static final Color SEAT_AVAIL_BG     = Color.WHITE;
    private static final Color SEAT_AVAIL_BORDER  = new Color(180, 185, 190);
    private static final Color SEAT_SOLD_BG       = new Color(230, 232, 235);
    private static final Color SEAT_SOLD_BORDER   = new Color(200, 202, 205);
    private static final Color SEAT_SEL_BG        = new Color(13, 110, 253);
    private static final Color SEAT_SEL_BORDER    = new Color(10, 88, 202);

    public BanVeStep3Module(Lich lich) {
        this.lich = lich;
        setLayout(new BorderLayout());
        setBackground(SURFACE);
        buildUI();
        loadData();
    }

    private void buildUI() {
        // --- 1. Top Section: Train Schematic ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(15, 24, 10, 24));

        JLabel lblTitle = new JLabel("Lựa chọn chỗ ngồi — Nhấn vào toa để xem sơ đồ");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(TEXT_MAIN);
        topPanel.add(lblTitle, BorderLayout.NORTH);

        trainContainer = new JPanel();
        trainContainer.setLayout(new BoxLayout(trainContainer, BoxLayout.X_AXIS));
        trainContainer.setOpaque(false);
        trainContainer.setBorder(new EmptyBorder(20, 10, 20, 10));

        JScrollPane trainScroll = new JScrollPane(trainContainer);
        trainScroll.setOpaque(false);
        trainScroll.getViewport().setOpaque(false);
        trainScroll.setBorder(null);
        trainScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        trainScroll.setPreferredSize(new Dimension(0, 140));
        topPanel.add(trainScroll, BorderLayout.CENTER);

        // --- 2. Middle Section: Legend & Active Wagon Info ---
        JPanel midPanel = new JPanel(new BorderLayout());
        midPanel.setBackground(CARD_BG);
        midPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, OUTLINE));
        midPanel.setPreferredSize(new Dimension(0, 50));
        
        lblStatus = new JLabel("Đang xem: Chưa chọn toa");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblStatus.setBorder(new EmptyBorder(0, 24, 0, 0));
        midPanel.add(lblStatus, BorderLayout.WEST);

        JPanel legend = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 13));
        legend.setOpaque(false);
        legend.add(createLegendItem(SEAT_AVAIL_BG, SEAT_AVAIL_BORDER, "Còn trống"));
        legend.add(createLegendItem(SEAT_SOLD_BG,  SEAT_SOLD_BORDER,  "Đã bán / Giữ chỗ"));
        legend.add(createLegendItem(SEAT_SEL_BG,   SEAT_SEL_BORDER,   "Đang chọn"));
        midPanel.add(legend, BorderLayout.EAST);

        // --- 3. Bottom Section: Seat Map Canvas ---
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(10, 24, 10, 24));

        seatCanvas = new SeatMapCanvas();
        JScrollPane seatScroll = new JScrollPane(seatCanvas);
        seatScroll.setBorder(BorderFactory.createLineBorder(OUTLINE, 1));
        seatScroll.getViewport().setBackground(CARD_BG);
        bottomPanel.add(seatScroll, BorderLayout.CENTER);

        // Assembler
        JPanel centerContent = new JPanel(new BorderLayout());
        centerContent.setOpaque(false);
        centerContent.add(topPanel, BorderLayout.NORTH);
        centerContent.add(midPanel, BorderLayout.CENTER);
        centerContent.add(bottomPanel, BorderLayout.SOUTH); // Actually the rest of center
        
        // Re-adjust to avoid NORTH/CENTER sizing issues with ScrollPanes
        add(topPanel, BorderLayout.NORTH);
        add(new JPanel(new BorderLayout()){{
            setOpaque(false);
            add(midPanel, BorderLayout.NORTH);
            add(bottomPanel, BorderLayout.CENTER);
        }}, BorderLayout.CENTER);

        // --- 4. Navigation Buttons ---
        btnSubmit = new JButton("Xác nhận & Tiếp tục →");
        styleBtn(btnSubmit, true);
        btnSubmit.setEnabled(false);
        btnSubmit.addActionListener(e -> {
            if (callback != null) callback.accept(new ArrayList<>(selectedGhes));
        });

        btnCancel = new JButton("← Trở lại bước 2");
        styleBtn(btnCancel, false);
        btnCancel.addActionListener(e -> { if (callback != null) callback.accept(null); });

        btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        btnPanel.setBackground(SURFACE);
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, OUTLINE));
        btnPanel.add(btnCancel);
        btnPanel.add(btnSubmit);
        btnPanel.setVisible(false);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private JPanel createLegendItem(Color bg, Color border, String text) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);
        JPanel box = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                g2.setColor(border);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 4, 4);
                g2.dispose();
            }
        };
        box.setPreferredSize(new Dimension(14, 14));
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        p.add(box); p.add(lbl);
        return p;
    }

    private void styleBtn(JButton btn, boolean primary) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(200, 42));
        if (primary) {
            btn.setBackground(PRIMARY); btn.setForeground(Color.WHITE); btn.setOpaque(true);
        } else {
            btn.setBackground(Color.WHITE); btn.setForeground(TEXT_MAIN); btn.setOpaque(true);
        }
    }

    private void loadData() {
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() {
                // 1. Load sold seats for this trip
                daoVe.findByLich(lich.getMaLich()).forEach(v -> {
                    if (v.getGhe() != null) soldMaGhes.add(v.getGhe().getMaGhe());
                });

                // 2. Load wagons
                if (lich.getDoanTau() != null) {
                    ctdtList = daoChiTietDT.findByDoanTau(lich.getDoanTau().getMaDoanTau());
                    for (ChiTietDoanTau ct : ctdtList) {
                        wagonGheMap.put(ct.getToaTau().getMaToaTau(), daoGhe.findByToaTau(ct.getToaTau().getMaToaTau()));
                    }
                }
                return null;
            }

            @Override protected void done() {
                updateTrainSchematic();
                if (!ctdtList.isEmpty()) {
                    setActiveWagon(ctdtList.get(0));
                }
            }
        }.execute();
    }

    private void updateTrainSchematic() {
        trainContainer.removeAll();
        
        // Locomotive
        trainContainer.add(createLocoIcon());
        trainContainer.add(createConnector());
        
        // Wagons
        for (int i = 0; i < ctdtList.size(); i++) {
            ChiTietDoanTau ct = ctdtList.get(i);
            trainContainer.add(createWagonCard(ct, i));
            if (i < ctdtList.size() - 1) trainContainer.add(createConnector());
        }
        
        trainContainer.revalidate();
        trainContainer.repaint();
    }

    private JPanel createLocoIcon() {
        JPanel p = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
            }
        };
        p.setPreferredSize(new Dimension(60, 60));
        p.setMaximumSize(new Dimension(60, 60));
        JLabel lbl = new JLabel();
        ImageIcon raw = new ImageIcon(getClass().getResource("/icons/bieuTuongTau.png"));
        if (raw.getImage() != null) {
            lbl.setIcon(new ImageIcon(raw.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH)));
        }
        p.add(lbl);
        return p;
    }

    private JPanel createWagonCard(ChiTietDoanTau ct, int index) {
        boolean isActive = activeCTDT != null && activeCTDT.getMaChiTietDT().equals(ct.getMaChiTietDT());
        long selCount = selectedGhes.stream().filter(g -> g.getToaTau().getMaToaTau().equals(ct.getToaTau().getMaToaTau())).count();
        
        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isActive ? PRIMARY : CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(isActive ? PRIMARY : OUTLINE);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.dispose();
            }
        };
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.setPreferredSize(new Dimension(80, 70));
        card.setMaximumSize(new Dimension(80, 70));
        
        JLabel lblNum = new JLabel("Toa " + (index + 1), SwingConstants.CENTER);
        lblNum.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblNum.setForeground(isActive ? Color.WHITE : TEXT_MAIN);
        card.add(lblNum, BorderLayout.NORTH);
        
        String typeIcon = switch(ct.getToaTau().getLoaiGhe()){
            case GHE_CUNG -> "C";
            case GHE_MEM -> "M";
            case GIUONG_NAM -> "G";
        };
        JLabel lblType = new JLabel(typeIcon, SwingConstants.CENTER);
        lblType.setFont(new Font("Consolas", Font.BOLD, 18));
        lblType.setForeground(isActive ? Color.WHITE : TEXT_MUTED);
        card.add(lblType, BorderLayout.CENTER);
        
        if (selCount > 0) {
            JLabel lblBadge = new JLabel(String.valueOf(selCount), SwingConstants.CENTER);
            lblBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
            lblBadge.setForeground(Color.WHITE);
            lblBadge.setOpaque(true);
            lblBadge.setBackground(new Color(255, 107, 107));
            lblBadge.setPreferredSize(new Dimension(16, 16));
            JPanel badgeWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
            badgeWrap.setOpaque(false);
            badgeWrap.add(lblBadge);
            card.add(badgeWrap, BorderLayout.SOUTH);
        }

        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                setActiveWagon(ct);
            }
        });
        
        return card;
    }

    private JPanel createConnector() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(OUTLINE);
                g.fillRect(0, getHeight()/2 - 2, getWidth(), 4);
            }
        };
        p.setPreferredSize(new Dimension(20, 70));
        p.setMaximumSize(new Dimension(20, 70));
        p.setOpaque(false);
        return p;
    }

    private void setActiveWagon(ChiTietDoanTau ct) {
        this.activeCTDT = ct;
        lblStatus.setText("Đang xem: Toa " + ct.getSoThuTu() + " (" + ct.getToaTau().getLoaiGhe().toString() + ")");
        seatCanvas.loadWagon(ct.getToaTau(), wagonGheMap.get(ct.getToaTau().getMaToaTau()));
        updateTrainSchematic();
    }

    private void updateGlobalSelection() {
        btnSubmit.setEnabled(!selectedGhes.isEmpty());
        updateTrainSchematic();
    }

    // --- Inner Class: Seat Map Canvas ---
    class SeatMapCanvas extends JPanel {
        private ToaTau activeToa;
        private List<Ghe> activeGhes = new ArrayList<>();
        private final Map<String, Rectangle> rects = new HashMap<>();
        private String hoveredMaGhe = null;

        // Visual Constants
        private static final int SEAT_W = 48, SEAT_H = 48, GAP = 8, KHOANG_GAP = 24, PADDING = 30;

        SeatMapCanvas() {
            setBackground(CARD_BG);
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    String hit = hitTest(e.getPoint());
                    if (hit != null && !soldMaGhes.contains(hit)) {
                        Ghe target = activeGhes.stream().filter(g -> g.getMaGhe().equals(hit)).findFirst().orElse(null);
                        if (target != null) {
                            if (selectedGhes.contains(target)) selectedGhes.remove(target);
                            else selectedGhes.add(target);
                            updateGlobalSelection();
                            repaint();
                        }
                    }
                }
            });
            addMouseMotionListener(new MouseAdapter() {
                @Override public void mouseMoved(MouseEvent e) {
                    String prev = hoveredMaGhe;
                    hoveredMaGhe = hitTest(e.getPoint());
                    if (!Objects.equals(prev, hoveredMaGhe)) repaint();
                    setCursor(hoveredMaGhe != null && !soldMaGhes.contains(hoveredMaGhe) ? new Cursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
                }
            });
        }

        void loadWagon(ToaTau toa, List<Ghe> ghes) {
            this.activeToa = toa;
            this.activeGhes = ghes != null ? ghes.stream().sorted(Comparator.comparingInt(Ghe::getSoGhe)).collect(Collectors.toList()) : new ArrayList<>();
            computeLayout();
            repaint();
        }

        private void computeLayout() {
            rects.clear();
            if (activeToa == null) return;
            
            int cols = (activeToa.getLoaiGhe() == LoaiGhe.GIUONG_NAM) ? 10 : 12;
            int colsPerKhoang = (activeToa.getLoaiGhe() == LoaiGhe.GIUONG_NAM) ? 2 : 6;
            
            for (int i = 0; i < activeGhes.size(); i++) {
                int row = i / cols;
                int col = i % cols;
                int khoangIdx = col / colsPerKhoang;
                int x = PADDING + col * (SEAT_W + GAP) + khoangIdx * (KHOANG_GAP - GAP);
                int y = PADDING + row * (SEAT_H + GAP);
                rects.put(activeGhes.get(i).getMaGhe(), new Rectangle(x, y, SEAT_W, SEAT_H));
            }
            
            int numRows = activeGhes.isEmpty() ? 0 : (activeGhes.size() + cols - 1) / cols;
            int totalW = PADDING * 2 + cols * (SEAT_W + GAP) + (cols/colsPerKhoang - 1) * (KHOANG_GAP - GAP);
            int totalH = PADDING * 2 + numRows * (SEAT_H + GAP);
            setPreferredSize(new Dimension(totalW, totalH));
            revalidate();
        }

        private String hitTest(Point p) {
            for (Map.Entry<String, Rectangle> e : rects.entrySet()) {
                if (e.getValue().contains(p)) return e.getKey();
            }
            return null;
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (activeToa == null) return;
            
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            for (Ghe ghe : activeGhes) {
                Rectangle r = rects.get(ghe.getMaGhe());
                boolean isSold = soldMaGhes.contains(ghe.getMaGhe());
                boolean isSel = selectedGhes.contains(ghe);
                boolean isHover = ghe.getMaGhe().equals(hoveredMaGhe) && !isSold;
                
                Color bg = isSold ? SEAT_SOLD_BG : isSel ? SEAT_SEL_BG : isHover ? PRIMARY_LIGHT : SEAT_AVAIL_BG;
                Color border = isSold ? SEAT_SOLD_BORDER : isSel ? SEAT_SEL_BORDER : SEAT_AVAIL_BORDER;
                
                g2.setColor(bg);
                g2.fillRoundRect(r.x, r.y, r.width, r.height, 8, 8);
                g2.setColor(border);
                g2.setStroke(new BasicStroke(isSel ? 2f : 1f));
                g2.drawRoundRect(r.x, r.y, r.width, r.height, 8, 8);
                
                g2.setColor(isSel ? Color.WHITE : TEXT_MAIN);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                String sn = String.valueOf(ghe.getSoGhe());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(sn, r.x + (r.width - fm.stringWidth(sn))/2, r.y + (r.height + fm.getAscent() - fm.getDescent())/2);
            }

            // Draw Khoang Dividers
            g2.setColor(OUTLINE);
            g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float[]{5, 4}, 0f));
            
            int cols = (activeToa.getLoaiGhe() == LoaiGhe.GIUONG_NAM) ? 10 : 12;
            int colsPerKhoang = (activeToa.getLoaiGhe() == LoaiGhe.GIUONG_NAM) ? 2 : 6;
            int numKhoang = cols / colsPerKhoang;
            int numRows = activeGhes.isEmpty() ? 0 : (activeGhes.size() + cols - 1) / cols;
            
            for (int k = 1; k < numKhoang; k++) {
                int lineX = PADDING + k * colsPerKhoang * (SEAT_W + GAP) - GAP + (KHOANG_GAP - GAP)/2 + (k-1)*(KHOANG_GAP - GAP);
                g2.drawLine(lineX, PADDING - 10, lineX, PADDING + numRows*(SEAT_H + GAP) - GAP + 10);
            }

            g2.dispose();
        }
    }

    // AppModule interface
    @Override public String getTitle() { return "Bước 3 – Chọn chỗ"; }
    @Override public JPanel getView()  { return this; }
    @Override public void setOnResult(Consumer<Object> cb) {
        this.callback = cb;
        btnPanel.setVisible(cb != null);
    }
    @Override public void reset() {
        selectedGhes.clear();
        updateGlobalSelection();
    }
}
