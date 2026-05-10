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

/**
 * Bước 3 Hợp nhất — Chọn Chỗ (Toa & Ghế).
 * Tầng trên: Sơ đồ đoàn tàu trực quan.
 * Tầng dưới: Sơ đồ ghế chi tiết của toa đang chọn.
 */
public class BanVeStep3Module extends JPanel implements AppModule {

    private Consumer<Object> callback;
    private final Lich lich;
    private final Map<LoaiGhe, Double> priceMap;

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
    private ToaSeatDiagramPanel seatCanvas;
    private JLabel lblStatus;
    private JButton btnSubmit, btnCancel;
    private JPanel btnPanel;

    // Design tokens
    private static final Color PRIMARY       = NotionTheme.ACCENT;
    private static final Color PRIMARY_LIGHT = NotionTheme.ACCENT_SOFT;
    private static final Color SURFACE       = NotionTheme.PAGE;
    private static final Color CARD_BG       = NotionTheme.CARD;
    private static final Color TEXT_MAIN     = NotionTheme.TEXT;
    private static final Color TEXT_MUTED    = NotionTheme.TEXT_MUTED;
    private static final Color OUTLINE       = NotionTheme.BORDER;

    // Seat Colors
    private static final Color SEAT_AVAIL_BG     = AppColors.SURFACE;
    private static final Color SEAT_AVAIL_BORDER  = NotionTheme.BORDER_STRONG;
    private static final Color SEAT_SOLD_BG       = NotionTheme.CARD_MUTED;
    private static final Color SEAT_SOLD_BORDER   = NotionTheme.TEXT_FAINT;
    private static final Color SEAT_SEL_BG        = NotionTheme.ACCENT;
    private static final Color SEAT_SEL_BORDER    = NotionTheme.ACCENT_HOVER;

    public BanVeStep3Module(Lich lich) {
        this(lich, Collections.emptyMap());
    }

    public BanVeStep3Module(Lich lich, Map<LoaiGhe, Double> priceMap) {
        this.lich = lich;
        this.priceMap = priceMap == null ? Collections.emptyMap() : new EnumMap<>(priceMap);
        setLayout(new BorderLayout());
        setBackground(SURFACE);
        buildUI();
        loadData();
        // Handoff: giữ constructor cũ để không phá nơi gọi khác; constructor mới chỉ thêm tooltip giá.
        // Risk: priceMap rỗng thì Step 3 vẫn hoạt động, tooltip báo chưa có giá.
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

        seatCanvas = new ToaSeatDiagramPanel();
        seatCanvas.setSelectable(true);
        seatCanvas.setUnavailableResolver(ghe -> soldMaGhes.contains(ghe.getMaGhe()));
        seatCanvas.setSelectedResolver(selectedGhes::contains);
        seatCanvas.setPriceResolver(this::unitPriceForTooltip);
        seatCanvas.setSeatClickHandler(ghe -> {
            if (selectedGhes.contains(ghe)) selectedGhes.remove(ghe);
            else selectedGhes.add(ghe);
            updateGlobalSelection();
            seatCanvas.repaint();
        });
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

        btnCancel = new JButton("← Quay lại");
        styleBtn(btnCancel, false);
        btnCancel.addActionListener(e -> { if (callback != null) callback.accept(null); });

        btnPanel = BookingStepUi.createFooter();
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
        BookingStepUi.styleActionButton(btn, primary);
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
        lbl.setIcon(LineIcons.contained(LineIcons.Name.TRAIN, 32, 20));
        p.add(lbl);
        return p;
        // Handoff: load SVG at final 32px size instead of raster-scaling from 24px.
        // Risk: keep the surrounding 60x60 circle fixed so step layout does not shift.
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
        lblNum.setForeground(isActive ? AppColors.SURFACE : TEXT_MAIN);
        card.add(lblNum, BorderLayout.NORTH);
        
        String typeIcon = switch(ct.getToaTau().getLoaiGhe()){
            case GHE_CUNG -> "C";
            case GHE_MEM -> "M";
            case GIUONG_NAM -> "G";
        };
        JLabel lblType = new JLabel(typeIcon, SwingConstants.CENTER);
        lblType.setFont(new Font("Consolas", Font.BOLD, 18));
        lblType.setForeground(isActive ? AppColors.SURFACE : TEXT_MUTED);
        card.add(lblType, BorderLayout.CENTER);
        
        if (selCount > 0) {
            JLabel lblBadge = new JLabel(String.valueOf(selCount), SwingConstants.CENTER);
            lblBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
            lblBadge.setForeground(AppColors.SURFACE);
            lblBadge.setOpaque(true);
            lblBadge.setBackground(AppColors.ERROR);
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
        seatCanvas.setData(ct.getToaTau(), wagonGheMap.get(ct.getToaTau().getMaToaTau()));
        updateTrainSchematic();
    }

    private Double unitPriceForTooltip(Ghe ghe) {
        if (ghe == null || ghe.getToaTau() == null || ghe.getToaTau().getLoaiGhe() == null) return null;
        return priceMap.get(ghe.getToaTau().getLoaiGhe());
        // Handoff: tooltip dùng đúng giá theo loại ghế mà Step 2 đã resolve cho lịch/tuyến.
        // Risk: không dùng giá này để tính tiền; tính tiền chính vẫn ở BanVeModule/Step6.
    }

    private void updateGlobalSelection() {
        btnSubmit.setEnabled(!selectedGhes.isEmpty());
        updateTrainSchematic();
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

