package com.modules;

import com.dao.DAO_Ga;
import com.dao.DAO_Tuyen;
import com.entity.Ga;
import com.entity.Tuyen;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class QuanLyTuyenModule extends JPanel implements AppModule {

    // ── Design tokens ────────────────────────────────────────────────────
    private static final Color PRIMARY       = new Color(13, 110, 253);   // Modern Blue
    private static final Color PRIMARY_HOVER = new Color(11, 94, 215);
    private static final Color PRIMARY_LIGHT = new Color(231, 241, 255);
    private static final Color SURFACE       = new Color(248, 249, 250);  // Very light gray
    private static final Color CARD_BG       = Color.WHITE;
    private static final Color TEXT_MAIN     = new Color(33, 37, 41);
    private static final Color TEXT_MUTED    = new Color(108, 117, 125);
    private static final Color OUTLINE       = new Color(222, 226, 230);
    private static final Color DANGER        = new Color(220, 53, 69);
    private static final Color DANGER_LIGHT  = new Color(254, 226, 226);
    private static final Color WARN_BG       = new Color(0xFF, 0xF3, 0xCD);
    private static final Color WARN_FG       = new Color(0x92, 0x60, 0x10);

    private static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD, 26);
    private static final Font FONT_DESC   = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_BOLD   = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_STA    = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font FONT_SMALL  = new Font("Segoe UI", Font.PLAIN, 12);

    private static final int CARD_HEIGHT = 160;

    // ── DAOs ─────────────────────────────────────────────────────────────
    private final DAO_Tuyen daoTuyen = new DAO_Tuyen();
    private final DAO_Ga    daoGa    = new DAO_Ga();

    // ── State ─────────────────────────────────────────────────────────────
    private Consumer<Object> callback;
    private List<Tuyen> allData      = new ArrayList<>();
    private List<Tuyen> filteredData = new ArrayList<>();
    private int currentPage  = 1;
    private int rowsPerPage  = 5;
    private boolean isRefreshing = false;

    // ── Widgets ───────────────────────────────────────────────────────────
    private SearchableComboBox<Ga> filterGaDi;
    private SearchableComboBox<Ga> filterGaDen;
    private JPanel                 cardsPanel;
    private JScrollPane            scrollPane;
    private JPanel                 paginationPanel;

    // ====================================================================
    //  AppModule interface
    // ====================================================================

    @Override public String getTitle() { return "Quản lý Tuyến đường"; }

    @Override
    public JPanel getView() {
        if (getComponentCount() == 0) buildUI();
        return this;
    }

    @Override public void setOnResult(Consumer<Object> cb) { this.callback = cb; }

    @Override
    public void reset() {
        if (filterGaDi  != null) filterGaDi.clearSelection();
        if (filterGaDen != null) filterGaDen.clearSelection();
        if (cardsPanel != null) applyFilter();
    }

    // ====================================================================
    //  UI BUILD
    // ====================================================================

    private void buildUI() {
        setLayout(new BorderLayout());
        setBackground(SURFACE);
        setBorder(new EmptyBorder(30, 40, 30, 40));

        add(buildHeader(),    BorderLayout.NORTH);
        add(buildContent(),   BorderLayout.CENTER);

        loadGaFilters();
        loadData();
    }

    // ── Header ────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);
        hdr.setBorder(new EmptyBorder(0, 0, 24, 0));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        titleRow.setOpaque(false);
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Icon header
        JLabel iconLbl = new JLabel();
        ImageIcon ico = loadScaledIcon("bieuTuongTuyen.png", 32);
        if (ico != null) iconLbl.setIcon(ico);
        JLabel lblTitle = new JLabel("Quản lý Tuyến đường");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(TEXT_MAIN);

        titleRow.add(iconLbl);
        titleRow.add(lblTitle);

        JLabel lblDesc = new JLabel("Quản lý và thiết lập lộ trình các ga tàu một cách trực quan.");
        lblDesc.setFont(FONT_DESC);
        lblDesc.setForeground(TEXT_MUTED);
        lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        left.add(titleRow);
        left.add(Box.createVerticalStrut(6));
        left.add(lblDesc);

        // Add Button
        JButton btnAdd = new JButton("  Tạo tuyến mới") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) g2.setColor(PRIMARY_HOVER.darker());
                else if (getModel().isRollover()) g2.setColor(PRIMARY_HOVER);
                else g2.setColor(PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        ImageIcon icoAdd = loadScaledIcon("nutThem.png", 18);
        if (icoAdd != null) btnAdd.setIcon(icoAdd);
        btnAdd.setFont(FONT_BOLD);
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFocusPainted(false);
        btnAdd.setBorderPainted(false);
        btnAdd.setContentAreaFilled(false);
        btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAdd.setPreferredSize(new Dimension(200, 45));
        btnAdd.addActionListener(e -> openAddDialog());

        hdr.add(left,   BorderLayout.CENTER);
        hdr.add(btnAdd, BorderLayout.EAST);
        return hdr;
    }

    // ── Content ──────────────────────────────────────────────────────────
    private JPanel buildContent() {
        JPanel content = new JPanel(new BorderLayout(0, 20));
        content.setOpaque(true);
        content.setBackground(SURFACE);

        content.add(buildFilterBar(), BorderLayout.NORTH);
        
        cardsPanel = new JPanel();
        cardsPanel.setOpaque(true);
        cardsPanel.setBackground(SURFACE);
        cardsPanel.setLayout(new BoxLayout(cardsPanel, BoxLayout.Y_AXIS));

        scrollPane = new JScrollPane(cardsPanel);
        scrollPane.setOpaque(true);
        scrollPane.setBackground(SURFACE);
        scrollPane.getViewport().setOpaque(true);
        scrollPane.getViewport().setBackground(SURFACE);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);

        scrollPane.getViewport().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int h = scrollPane.getViewport().getHeight();
                int newRows = Math.max(3, h / (CARD_HEIGHT + 15));
                if (newRows != rowsPerPage) {
                    rowsPerPage = newRows;
                    if (!isRefreshing) refreshCards();
                }
            }
        });

        content.add(scrollPane, BorderLayout.CENTER);
        
        paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        paginationPanel.setOpaque(true);
        paginationPanel.setBackground(SURFACE);
        content.add(paginationPanel, BorderLayout.SOUTH);

        return content;
    }

    // ── Filter Bar ────────────────────────────────────────────────────────
    private JPanel buildFilterBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        
        JPanel bgPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Shadow
                g2.setColor(new Color(0, 0, 0, 10));
                g2.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 5, 24, 24);
                
                // Background
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 4, 24, 24);
                
                // Border
                g2.setColor(OUTLINE);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 4, 24, 24);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        bgPanel.setOpaque(false);
        bgPanel.setBorder(new EmptyBorder(8, 20, 12, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 15);

        // Icon
        JLabel iconSearch = new JLabel();
        ImageIcon icoSearch = loadScaledIcon("nutTimKiem.png", 22);
        if (icoSearch!=null) iconSearch.setIcon(icoSearch);
        bgPanel.add(iconSearch, gbc);

        // Station Di
        filterGaDi = createGaCombo("Tìm ga đi...");
        gbc.weightx = 0.45;
        bgPanel.add(filterGaDi, gbc);

        // Arrow
        JLabel arrow = new JLabel(" → ");
        arrow.setFont(new Font("Segoe UI", Font.BOLD, 18));
        arrow.setForeground(TEXT_MUTED);
        gbc.weightx = 0;
        bgPanel.add(arrow, gbc);

        // Station Den
        filterGaDen = createGaCombo("Tìm ga đến...");
        gbc.weightx = 0.45;
        bgPanel.add(filterGaDen, gbc);

        // Bo loc button
        JButton btnClear = new JButton("Bỏ lọc") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? SURFACE : Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(OUTLINE);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnClear.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnClear.setForeground(TEXT_MUTED);
        btnClear.setContentAreaFilled(false);
        btnClear.setBorderPainted(false);
        btnClear.setFocusPainted(false);
        btnClear.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClear.setPreferredSize(new Dimension(100, 42));
        btnClear.addActionListener(e -> {
            filterGaDi.clearSelection();
            filterGaDen.clearSelection();
            applyFilter();
        });
        
        gbc.weightx = 0;
        gbc.insets = new Insets(0, 10, 0, 0);
        bgPanel.add(btnClear, gbc);

        bar.add(bgPanel, BorderLayout.CENTER);
        return bar;
    }

    private SearchableComboBox<Ga> createGaCombo(String placeholder) {
        SearchableComboBox<Ga> cb = new SearchableComboBox<>(
                ga -> ga.getTenGa() + " (" + ga.getMaGa() + ")",
                (ga, q) -> ga.getTenGa().toLowerCase().contains(q.toLowerCase()) || 
                           ga.getMaGa().toLowerCase().contains(q.toLowerCase()));
        cb.setPlaceholder(placeholder);
        cb.setPreferredSize(new Dimension(250, 40));
        cb.setOnChanged(this::applyFilter);
        return cb;
    }

    // ====================================================================
    //  DATA OPERATIONS
    // ====================================================================

    private void loadGaFilters() {
        List<Ga> gaList = daoGa.getAll();
        filterGaDi.setItems(gaList);
        filterGaDen.setItems(gaList);
    }

    private void loadData() {
        allData = daoTuyen.getAll();
        applyFilter();
    }

    private void applyFilter() {
        Ga selGaDi  = filterGaDi.getSelectedItem();
        Ga selGaDen = filterGaDen.getSelectedItem();

        filteredData = new ArrayList<>();
        for (Tuyen t : allData) {
            boolean okDi  = (selGaDi  == null) || (t.getGaDi()  != null && t.getGaDi().getMaGa().equals(selGaDi.getMaGa()));
            boolean okDen = (selGaDen == null) || (t.getGaDen() != null && t.getGaDen().getMaGa().equals(selGaDen.getMaGa()));
            if (okDi && okDen) filteredData.add(t);
        }

        currentPage = 1;
        refreshCards();
    }

    private void refreshCards() {
        isRefreshing = true;
        try {
            int total = filteredData.size();
            int totalPages = (total == 0) ? 1 : (int) Math.ceil((double) total / rowsPerPage);
            if (currentPage > totalPages) currentPage = totalPages;

            int start = (currentPage - 1) * rowsPerPage;
            int end   = Math.min(start + rowsPerPage, total);
            List<Tuyen> pageData = filteredData.subList(start, end);

            cardsPanel.removeAll();
            if (pageData.isEmpty()) {
                cardsPanel.add(buildEmptyState());
            } else {
                for (Tuyen t : pageData) {
                    cardsPanel.add(buildTicketCard(t));
                    cardsPanel.add(Box.createVerticalStrut(15));
                }
            }
            cardsPanel.revalidate();
            cardsPanel.repaint();

            rebuildPagination(totalPages, total);
        } finally {
            isRefreshing = false;
        }
    }

    // ====================================================================
    //  TICKET CARD BUILDER
    // ====================================================================

    private JPanel buildTicketCard(Tuyen tuyen) {
        final boolean isActive = tuyen.isHoatDong();
        final Color stripColor = isActive ? PRIMARY : WARN_FG;

        JPanel card = new JPanel(new BorderLayout()) {
            boolean isHovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { isHovered = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { isHovered = false; repaint(); }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Shadow
                g2.setColor(new Color(0, 0, 0, isHovered ? 15 : 8));
                g2.fillRoundRect(3, 4, w - 6, h - 5, 20, 20);

                // Card Background
                g2.setColor(isActive ? CARD_BG : new Color(0xFF, 0xFD, 0xF5));
                g2.fillRoundRect(2, 0, w - 6, h - 6, 20, 20);

                // Border
                g2.setColor(isHovered ? stripColor.brighter() : OUTLINE);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(2, 0, w - 6, h - 6, 20, 20);

                // Left accent strip
                g2.clip(new RoundRectangle2D.Float(2, 0, w - 6, h - 6, 20, 20));
                g2.setColor(stripColor);
                g2.fillRect(2, 0, 8, h);

                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, CARD_HEIGHT));
        card.setPreferredSize(new Dimension(0, CARD_HEIGHT));
        card.setBorder(new EmptyBorder(15, 30, 15, 20));

        // ── Main Content Container ─────────────────────────────────────
        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();

        // ── Row 0: Badge 
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3; 
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weighty = 0;
        gbc.insets = new Insets(0, 0, 5, 0);

        JPanel badgeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        badgeRow.setOpaque(false);

        JLabel lblCode = new JLabel(" #" + tuyen.getMaTuyen() + " ") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(PRIMARY_LIGHT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblCode.setFont(new Font("Consolas", Font.BOLD, 13));
        lblCode.setForeground(PRIMARY);
        lblCode.setBorder(new EmptyBorder(4, 6, 4, 6));
        badgeRow.add(lblCode);

        if (!isActive) {
            JLabel lblInactive = new JLabel(" Ngưng hoạt động ") {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(WARN_BG);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            lblInactive.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblInactive.setForeground(WARN_FG);
            lblInactive.setBorder(new EmptyBorder(4, 6, 4, 6));
            badgeRow.add(lblInactive);
        }

        content.add(badgeRow, gbc);

        // ── Row 1: Stations & Track
        gbc.gridy = 1; gbc.gridwidth = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 0);

        // ── Left: Ga Di ────────────────────────────────────────────────
        JPanel leftGa = buildGaInfo(tuyen.getGaDi(), "Ga Đi", "bieuTuongGaDi.png", true);
        gbc.gridx = 0; gbc.weightx = 0.35; gbc.anchor = GridBagConstraints.WEST;
        content.add(leftGa, gbc);

        // ── Middle: Track graphic ──────────────────────────────────────
        JPanel middle = buildTrackUI(tuyen);
        gbc.gridx = 1; gbc.weightx = 0.3; gbc.anchor = GridBagConstraints.CENTER;
        content.add(middle, gbc);

        // ── Right: Ga Den ──────────────────────────────────────────────
        JPanel rightGa = buildGaInfo(tuyen.getGaDen(), "Ga Đến", "bieuTuongGaDen.png", false);
        gbc.gridx = 2; gbc.weightx = 0.35; gbc.anchor = GridBagConstraints.EAST;
        content.add(rightGa, gbc);

        card.add(content, BorderLayout.CENTER);

        // ── Actions Panel (Right edge) ──────────────────────────────────
        JPanel actions = new JPanel();
        actions.setOpaque(false);
        actions.setLayout(new BoxLayout(actions, BoxLayout.Y_AXIS));
        actions.setBorder(new EmptyBorder(0, 15, 0, 0));

        JButton btnEdit = buildIconButton("nutSua.png", PRIMARY_LIGHT, PRIMARY);
        btnEdit.setToolTipText("Chỉnh sửa tuyến");
        btnEdit.addActionListener(e -> openEditDialog(tuyen));

        JButton btnToggle = buildIconButton(isActive ? "nutXoa.png" : "nutSua.png",
                isActive ? DANGER_LIGHT : WARN_BG, isActive ? DANGER : WARN_FG);
        btnToggle.setToolTipText(isActive ? "Ngưng hoạt động" : "Kích hoạt lại");
        btnToggle.addActionListener(e -> toggleHoatDong(tuyen));

        actions.add(Box.createVerticalGlue());
        actions.add(btnEdit);
        actions.add(Box.createVerticalStrut(15));
        actions.add(btnToggle);
        actions.add(Box.createVerticalGlue());

        card.add(actions, BorderLayout.EAST);

        return card;
    }

    private JPanel buildGaInfo(Ga ga, String typeLabel, String iconName, boolean leftAlign) {
        JPanel pnl = new JPanel();
        pnl.setOpaque(false);
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));

        String gaName = ga != null ? ga.getTenGa() : "(Chưa có)";
        String maGa   = ga != null ? ga.getMaGa() : "N/A";
        String diaChi = (ga != null && ga.getDiaChi() != null) ? ga.getDiaChi() : "Không có địa chỉ";

        float alignX = leftAlign ? Component.LEFT_ALIGNMENT : Component.RIGHT_ALIGNMENT;

        JLabel lblType = new JLabel(typeLabel.toUpperCase() + " • " + maGa);
        lblType.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblType.setForeground(TEXT_MUTED);
        lblType.setAlignmentX(alignX);

        JLabel lblName = new JLabel(gaName);
        lblName.setFont(FONT_STA);
        lblName.setForeground(TEXT_MAIN);
        lblName.setAlignmentX(alignX);

        // Icon + Address
        JPanel locPanel = new JPanel(new FlowLayout(leftAlign ? FlowLayout.LEFT : FlowLayout.RIGHT, 0, 0));
        locPanel.setOpaque(false);
        locPanel.setAlignmentX(alignX);
        JLabel lblIcon = new JLabel();
        ImageIcon idx = loadScaledIcon(iconName, 14);
        if(idx!=null) lblIcon.setIcon(idx);
        
        JLabel lblAddr = new JLabel(" " + diaChi + " ");
        lblAddr.setFont(FONT_SMALL);
        lblAddr.setForeground(TEXT_MUTED);
        
        if (leftAlign) {
            locPanel.add(lblIcon);
            locPanel.add(lblAddr);
        } else {
            locPanel.add(lblAddr);
            locPanel.add(lblIcon);
        }

        pnl.add(Box.createVerticalGlue());
        pnl.add(lblType);
        pnl.add(Box.createVerticalStrut(4));
        pnl.add(lblName);
        pnl.add(Box.createVerticalStrut(6));
        pnl.add(locPanel);
        pnl.add(Box.createVerticalGlue());
        
        return pnl;
    }

    private JPanel buildTrackUI(Tuyen tuyen) {
        JPanel track = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                int midY = h / 2 + 5;
                
                // Draw dots line
                Stroke dashed = new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{6, 6}, 0);
                g2.setStroke(dashed);
                g2.setColor(OUTLINE.darker());
                g2.drawLine(20, midY, w - 20, midY);

                // Draw Train Icon in middle
                ImageIcon trainIcon = loadScaledIcon("bieuTuongTau.png", 24);
                if (trainIcon != null) {
                    int ix = (w - 24) / 2;
                    int iy = midY - 12;
                    
                    // Clear background behind icon
                    g2.setColor(CARD_BG);
                    g2.fillOval(ix-4, iy-4, 32, 32);
                    
                    trainIcon.paintIcon(this, g2, ix, iy);
                }
                
                // Draw KM text
                String km = tuyen.getKm() > 0 ? tuyen.getKm() + " km" : "---";
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                FontMetrics fm = g2.getFontMetrics();
                int tw = fm.stringWidth(km);
                g2.setColor(PRIMARY);
                g2.drawString(km, (w - tw)/2, midY - 20);

                // End dots
                g2.setColor(PRIMARY);
                g2.fillOval(15, midY-4, 8, 8);
                g2.setColor(DANGER);
                g2.fillOval(w - 23, midY-4, 8, 8);
                
                g2.dispose();
            }
        };
        track.setOpaque(false);
        track.setPreferredSize(new Dimension(150, 80));
        return track;
    }

    private JButton buildIconButton(String iconFile, Color bgHover, Color iconColor) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover() || getModel().isPressed()) {
                    g2.setColor(bgHover);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        ImageIcon ico = loadScaledIcon(iconFile, 20);
        if (ico != null) btn.setIcon(ico);
        btn.setPreferredSize(new Dimension(38, 38));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel buildEmptyState() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        JLabel lbl = new JLabel("Không tìm thấy tuyến đường nào!");
        lbl.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        lbl.setForeground(TEXT_MUTED);
        p.add(lbl);
        return p;
    }

    // ====================================================================
    //  PAGINATION
    // ====================================================================

    private void rebuildPagination(int totalPages, int total) {
        paginationPanel.removeAll();

        if (total > 0) {
            JLabel info = new JLabel("Hiển thị trang " + currentPage + " / " + totalPages + " (" + total + " tuyến)");
            info.setFont(FONT_SMALL);
            info.setForeground(TEXT_MUTED);
            paginationPanel.add(info);
            paginationPanel.add(Box.createHorizontalStrut(15));
        }

        if (totalPages > 1) {
            JButton btnPrev = makePaginBtn("<");
            btnPrev.setEnabled(currentPage > 1);
            btnPrev.addActionListener(e -> { currentPage--; refreshCards(); });
            paginationPanel.add(btnPrev);

            int startP = Math.max(1, currentPage - 2);
            int endP   = Math.min(totalPages, startP + 4);
            for (int p = startP; p <= endP; p++) {
                final int page = p;
                JButton btn = makePaginBtn(String.valueOf(p));
                if (p == currentPage) {
                    btn.setForeground(Color.WHITE);
                    btn.putClientProperty("active", true);
                }
                btn.addActionListener(e -> { currentPage = page; refreshCards(); });
                paginationPanel.add(btn);
            }

            JButton btnNext = makePaginBtn(">");
            btnNext.setEnabled(currentPage < totalPages);
            btnNext.addActionListener(e -> { currentPage++; refreshCards(); });
            paginationPanel.add(btnNext);
        }

        paginationPanel.revalidate();
        paginationPanel.repaint();
    }

    private JButton makePaginBtn(String label) {
        JButton btn = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Object active = getClientProperty("active");
                if (Boolean.TRUE.equals(active)) {
                    g2.setColor(PRIMARY);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                } else if (getModel().isRollover()) {
                    g2.setColor(OUTLINE);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BOLD);
        btn.setForeground(TEXT_MAIN);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(0, 0, 0, 0));
        btn.setPreferredSize(new Dimension(36, 36));
        return btn;
    }

    // ====================================================================
    //  ACTIONS
    // ====================================================================

    private void openAddDialog() {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        ChinhSuaTuyenDialog dlg = new ChinhSuaTuyenDialog(owner);
        dlg.setOnSaved(saved -> loadData());
        dlg.setVisible(true);
    }

    private void openEditDialog(Tuyen tuyen) {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        ChinhSuaTuyenDialog dlg = new ChinhSuaTuyenDialog(owner, tuyen);
        dlg.setOnSaved(saved -> loadData());
        dlg.setVisible(true);
    }

    private void toggleHoatDong(Tuyen tuyen) {
        boolean current = tuyen.isHoatDong();
        String msg = current
                ? "Ngưng hoạt động tuyến " + tuyen.getMaTuyen() + "?\nTuyến sẽ không hiển thị khi tìm kiếm và bán vé."
                : "Kích hoạt lại tuyến " + tuyen.getMaTuyen() + "?";
        String title = current ? "Xác nhận ngưng hoạt động" : "Xác nhận kích hoạt";
        int choice = JOptionPane.showConfirmDialog(this, msg, title,
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) return;
        boolean ok = daoTuyen.setHoatDong(tuyen.getMaTuyen(), !current);
        if (ok) loadData();
        else JOptionPane.showMessageDialog(this, "Không thể cập nhật trạng thái tuyến.", "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    private ImageIcon loadScaledIcon(String fileName, int size) {
        try {
            java.net.URL url = getClass().getResource("/icons/" + fileName);
            if (url == null) return null;
            Image img = new ImageIcon(url).getImage()
                    .getScaledInstance(size, size, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } catch (Exception e) {
            return null;
        }
    }
}
