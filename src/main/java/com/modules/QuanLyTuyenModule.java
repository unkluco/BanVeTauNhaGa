package com.modules;

import com.dao.DAO_Ga;
import com.dao.DAO_Tuyen;
import com.entity.Ga;
import com.entity.Tuyen;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * AppModule: Quản lý Tuyến đường.
 *
 * Layout:
 *   Header  ─ title + "Thêm Tuyến mới" button
 *   Filter  ─ SearchableComboBox<Ga> (ga đi) + sync icon + SearchableComboBox<Ga> (ga đến) + "Bỏ lọc"
 *   Cards   ─ scrollable list of TuyenCardPanel (route track visualization)
 *   Footer  ─ pagination
 */
public class QuanLyTuyenModule extends JPanel implements AppModule {

    // ── Design tokens ────────────────────────────────────────────────────
    private static final Color PRIMARY       = new Color(0x00, 0x5D, 0x90);
    private static final Color PRIMARY_HOVER = new Color(0x00, 0x4A, 0x73);
    private static final Color PRIMARY_LIGHT = new Color(0xE3, 0xF2, 0xFD);
    private static final Color SURFACE       = new Color(0xF7, 0xF9, 0xFB);
    private static final Color CARD_BG       = Color.WHITE;
    private static final Color ON_SURFACE    = new Color(0x19, 0x1C, 0x1E);
    private static final Color ON_SURF_VAR   = new Color(0x40, 0x48, 0x50);
    private static final Color OUTLINE       = new Color(0xBF, 0xC7, 0xD1);
    private static final Color FILTER_BG     = new Color(0xF2, 0xF4, 0xF6);
    private static final Color ERROR_BG      = new Color(0xFF, 0xDA, 0xD6);
    private static final Color ERROR_FG      = new Color(0xB9, 0x1C, 0x1C);
    private static final Color TRACK_START   = new Color(0x00, 0x5D, 0x90);
    private static final Color TRACK_END     = new Color(0x00, 0x77, 0xB6);
    private static final Color BADGE_BLUE_BG = new Color(0xCD, 0xE5, 0xFF);
    private static final Color BADGE_BLUE_FG = new Color(0x00, 0x4B, 0x74);

    private static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font FONT_DESC   = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_BODY   = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_BOLD   = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_MONO   = new Font("Consolas", Font.BOLD, 12);
    private static final Font FONT_BADGE  = new Font("Segoe UI", Font.BOLD, 11);
    private static final Font FONT_BTN    = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_STA    = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font FONT_SMALL  = new Font("Segoe UI", Font.PLAIN, 11);

    private static final int CARD_HEIGHT = 155;  // px per card
    private static final int CARD_GAP    = 10;

    // ── DAOs ─────────────────────────────────────────────────────────────
    private final DAO_Tuyen daoTuyen = new DAO_Tuyen();
    private final DAO_Ga    daoGa    = new DAO_Ga();

    // ── State ─────────────────────────────────────────────────────────────
    private Consumer<Object> callback;
    private List<Tuyen> allData      = new ArrayList<>();
    private List<Tuyen> filteredData = new ArrayList<>();
    private int currentPage  = 1;
    private int rowsPerPage  = 4;
    private boolean isRefreshing = false;

    // ── Widgets ───────────────────────────────────────────────────────────
    private SearchableComboBox<Ga> filterGaDi;
    private SearchableComboBox<Ga> filterGaDen;
    private JPanel                 cardsPanel;
    private JScrollPane            scrollPane;
    private JLabel                 lblPageInfo;
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
        loadData();
    }

    // ====================================================================
    //  UI BUILD
    // ====================================================================

    private void buildUI() {
        setLayout(new BorderLayout());
        setBackground(SURFACE);
        setBorder(new EmptyBorder(32, 40, 32, 40));

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

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleRow.setOpaque(false);
        ImageIcon icoTitle = loadScaledIcon("bieuTuongTuyen.png", 28);
        if (icoTitle != null) titleRow.add(new JLabel(icoTitle));
        JLabel lblTitle = new JLabel("Qu\u1EA3n l\u00FD Tuy\u1EBFn \u0111\u01B0\u1EDDng");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(PRIMARY);
        titleRow.add(lblTitle);
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblDesc = new JLabel("C\u1EA5u h\u00ECnh v\u00E0 t\u1ED1i \u01B0u h\u00F3a c\u00E1c l\u1ED9 tr\u00ECnh v\u1EADn t\u1EA3i \u0111\u01B0\u1EDDng s\u1EAFt trong h\u1EC7 th\u1ED1ng.");
        lblDesc.setFont(FONT_DESC);
        lblDesc.setForeground(ON_SURF_VAR);
        lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        left.add(titleRow);
        left.add(Box.createVerticalStrut(4));
        left.add(lblDesc);

        JButton btnAdd = new JButton("+ Thêm Tuyến mới") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? PRIMARY_HOVER : PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnAdd.setFont(FONT_BTN);
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setContentAreaFilled(false);
        btnAdd.setBorderPainted(false);
        btnAdd.setFocusPainted(false);
        btnAdd.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAdd.setPreferredSize(new Dimension(190, 42));
        btnAdd.addActionListener(e -> openAddDialog());
        ImageIcon icoAdd = loadScaledIcon("nutThem.png", 16);
        if (icoAdd != null) { btnAdd.setIcon(icoAdd); btnAdd.setText("  Th\u00EAm Tuy\u1EBFn m\u1EDBi"); }

        hdr.add(left,   BorderLayout.CENTER);
        hdr.add(btnAdd, BorderLayout.EAST);
        return hdr;
    }

    // ── Content (filter + cards + pagination) ────────────────────────────
    private JPanel buildContent() {
        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setOpaque(false);

        content.add(buildFilterBar(), BorderLayout.NORTH);
        content.add(buildCardsArea(), BorderLayout.CENTER);
        content.add(buildPagination(), BorderLayout.SOUTH);

        return content;
    }

    // ── Filter Bar ────────────────────────────────────────────────────────
    private JPanel buildFilterBar() {
        JPanel bar = new JPanel(new GridBagLayout());
        bar.setBackground(FILTER_BG);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1),
                new EmptyBorder(14, 18, 14, 18)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy   = 0;
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.anchor  = GridBagConstraints.CENTER;
        gbc.weighty = 0;

        // Ga đi
        filterGaDi = new SearchableComboBox<>(
                ga -> ga.getTenGa() + " (" + ga.getMaGa() + ")",
                (ga, q) -> ga.getTenGa().toLowerCase().contains(q) || ga.getMaGa().toLowerCase().contains(q));
        filterGaDi.setPlaceholder("T\u1EA5t c\u1EA3 c\u00E1c ga");
        filterGaDi.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        filterGaDi.setOnChanged(this::applyFilter);
        gbc.gridx = 0; gbc.weightx = 1.0; gbc.insets = new Insets(0, 0, 0, 8);
        bar.add(buildFilterGroup("Ga \u0111i", filterGaDi), gbc);

        // sync icon ⇄ — wrapped to align at field level
        JPanel syncWrap = new JPanel(new BorderLayout());
        syncWrap.setOpaque(false);
        JLabel syncSpacer = new JLabel(" ");
        syncSpacer.setFont(FONT_SMALL);
        syncWrap.add(syncSpacer, BorderLayout.NORTH);
        JLabel syncLbl = new JLabel("\u21C4");
        syncLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        syncLbl.setForeground(OUTLINE);
        syncLbl.setHorizontalAlignment(SwingConstants.CENTER);
        syncWrap.add(syncLbl, BorderLayout.CENTER);
        gbc.gridx = 1; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 0, 0, 8);
        bar.add(syncWrap, gbc);

        // Ga đến
        filterGaDen = new SearchableComboBox<>(
                ga -> ga.getTenGa() + " (" + ga.getMaGa() + ")",
                (ga, q) -> ga.getTenGa().toLowerCase().contains(q) || ga.getMaGa().toLowerCase().contains(q));
        filterGaDen.setPlaceholder("T\u1EA5t c\u1EA3 c\u00E1c ga");
        filterGaDen.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        filterGaDen.setOnChanged(this::applyFilter);
        gbc.gridx = 2; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 8);
        bar.add(buildFilterGroup("Ga \u0111\u1EBFn", filterGaDen), gbc);

        // Bỏ lọc button
        JButton btnClear = new JButton("B\u1ECF l\u1ECDc") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? OUTLINE.darker() : OUTLINE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnClear.setFont(FONT_BOLD);
        btnClear.setForeground(PRIMARY);
        btnClear.setContentAreaFilled(false);
        btnClear.setBorderPainted(false);
        btnClear.setFocusPainted(false);
        btnClear.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClear.setPreferredSize(new Dimension(100, 40));
        btnClear.addActionListener(e -> {
            filterGaDi.clearSelection();
            filterGaDen.clearSelection();
            applyFilter();
        });
        ImageIcon icoClear = loadScaledIcon("nutBoLoc.png", 14);
        if (icoClear != null) btnClear.setIcon(icoClear);

        JPanel clearWrapper = new JPanel(new BorderLayout());
        clearWrapper.setOpaque(false);
        JLabel clearSpacer = new JLabel(" ");
        clearSpacer.setFont(FONT_SMALL);
        clearWrapper.add(clearSpacer, BorderLayout.NORTH);
        clearWrapper.add(btnClear, BorderLayout.CENTER);
        gbc.gridx = 3; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 0, 0, 0);
        bar.add(clearWrapper, gbc);

        return bar;
    }

    private JPanel buildFilterGroup(String labelText, JComponent field) {
        JPanel group = new JPanel();
        group.setOpaque(false);
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));

        JLabel lbl = new JLabel(labelText.toUpperCase());
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(ON_SURF_VAR);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        group.add(lbl);
        group.add(Box.createVerticalStrut(4));
        group.add(field);
        return group;
    }

    // ── Cards area ────────────────────────────────────────────────────────
    private JScrollPane buildCardsArea() {
        cardsPanel = new JPanel();
        cardsPanel.setOpaque(false);
        cardsPanel.setLayout(new BoxLayout(cardsPanel, BoxLayout.Y_AXIS));

        scrollPane = new JScrollPane(cardsPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);

        scrollPane.getViewport().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int newRows = calcRowsFromViewport();
                if (newRows > 0 && newRows != rowsPerPage) {
                    rowsPerPage = newRows;
                    if (!isRefreshing) refreshCards();
                }
            }
        });

        return scrollPane;
    }

    private int calcRowsFromViewport() {
        return 4;
    }

    // ── Pagination ────────────────────────────────────────────────────────
    private JPanel buildPagination() {
        paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        paginationPanel.setOpaque(false);
        paginationPanel.setBorder(new EmptyBorder(12, 0, 0, 0));

        lblPageInfo = new JLabel();
        lblPageInfo.setFont(FONT_SMALL);
        lblPageInfo.setForeground(ON_SURF_VAR);

        return paginationPanel;
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
        Ga selGaDi  = (filterGaDi  != null) ? filterGaDi.getSelectedItem()  : null;
        Ga selGaDen = (filterGaDen != null) ? filterGaDen.getSelectedItem() : null;

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
            int vpRows = calcRowsFromViewport();
            if (vpRows > 0) rowsPerPage = vpRows;

            int total = filteredData.size();
            int totalPages = (total == 0) ? 1 : (int) Math.ceil((double) total / rowsPerPage);
            if (currentPage > totalPages) currentPage = totalPages;

            int start = (currentPage - 1) * rowsPerPage;
            int end   = Math.min(start + rowsPerPage, total);
            List<Tuyen> pageData = filteredData.subList(start, end);

            // Rebuild cards
            cardsPanel.removeAll();
            if (pageData.isEmpty()) {
                cardsPanel.add(buildEmptyState());
            } else {
                for (Tuyen t : pageData) {
                    cardsPanel.add(buildTuyenCard(t));
                    cardsPanel.add(Box.createVerticalStrut(CARD_GAP));
                }
            }
            cardsPanel.revalidate();
            cardsPanel.repaint();

            // Rebuild pagination
            rebuildPagination(totalPages, total);
        } finally {
            isRefreshing = false;
        }
    }

    // ====================================================================
    //  CARD BUILDER
    // ====================================================================

    private JPanel buildTuyenCard(Tuyen tuyen) {
        JPanel card = new JPanel(new BorderLayout(16, 0)) {
            @Override
            public Dimension getPreferredSize() {
                // Fix width to parent's width; fix height to CARD_HEIGHT
                return new Dimension(100, CARD_HEIGHT);
            }
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
        card.setBorder(new EmptyBorder(14, 20, 14, 20));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, CARD_HEIGHT));
        card.setMinimumSize(new Dimension(100, CARD_HEIGHT));

        // ─ Left: maTuyen badge + route track ────────────────────────────
        JPanel leftPanel = new JPanel();
        leftPanel.setOpaque(false);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

        // Badge row: maTuyen on left, no updated-time shown
        JPanel badgeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        badgeRow.setOpaque(false);
        badgeRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel badge = new JLabel(tuyen.getMaTuyen()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BADGE_BLUE_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(FONT_MONO);
        badge.setForeground(BADGE_BLUE_FG);
        badge.setOpaque(false);
        badge.setBorder(new EmptyBorder(3, 10, 3, 10));

        badgeRow.add(badge);
        leftPanel.add(badgeRow);
        leftPanel.add(Box.createVerticalStrut(8));

        // Route track: [circle] gaDi ─────track─────── gaDen [flag]
        JPanel trackPanel = buildTrackPanel(tuyen);
        trackPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.add(trackPanel);

        card.add(leftPanel, BorderLayout.CENTER);

        // ─ Right: edit + delete buttons ─────────────────────────────────
        JPanel actionPanel = new JPanel();
        actionPanel.setOpaque(false);
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
        actionPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(0xEC, 0xEE, 0xF0)),
                new EmptyBorder(0, 16, 0, 0)));

        JButton btnEdit   = buildActionBtn("✎", PRIMARY_LIGHT, PRIMARY, false);
        JButton btnDelete = buildActionBtn("✕", ERROR_BG,      ERROR_FG, false);

        btnEdit.setToolTipText("Chỉnh sửa");
        btnDelete.setToolTipText("Xóa tuyến");

        btnEdit.addActionListener(e -> openEditDialog(tuyen));
        btnDelete.addActionListener(e -> confirmDelete(tuyen));

        actionPanel.add(Box.createVerticalGlue());
        actionPanel.add(btnEdit);
        actionPanel.add(Box.createVerticalStrut(8));
        actionPanel.add(btnDelete);
        actionPanel.add(Box.createVerticalGlue());

        card.add(actionPanel, BorderLayout.EAST);

        return card;
    }

    private JPanel buildTrackPanel(Tuyen tuyen) {
        final String gaDiName  = tuyen.getGaDi()  != null ? tuyen.getGaDi().getTenGa()  : "(?)";
        final String gaDenName = tuyen.getGaDen() != null ? tuyen.getGaDen().getTenGa() : "(?)";
        final String kmText    = tuyen.getKm() > 0 ? tuyen.getKm() + " km" : "";

        // Everything drawn in paintComponent — no child widgets, no null layout trouble.
        JPanel track = new JPanel() {
            private static final int LINE_Y   = 30;  // Y of the track line (from top)
            private static final int CIRCLE_R = 8;
            private static final int MARGIN   = 16;  // left/right margin from card border

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,     RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int w      = getWidth();
                int leftX  = MARGIN + CIRCLE_R;          // node centre X (left)
                int rightX = w - MARGIN - CIRCLE_R;      // node centre X (right)
                if (rightX < leftX + 20) rightX = leftX + 20;

                // ── Gradient track line ───────────────────────────────────
                GradientPaint gp = new GradientPaint(leftX, LINE_Y, TRACK_START, rightX, LINE_Y, TRACK_END);
                g2.setPaint(gp);
                g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(leftX + CIRCLE_R, LINE_Y, rightX - CIRCLE_R, LINE_Y);

                // ── Left node ────────────────────────────────────────────
                g2.setPaint(null);
                g2.setColor(TRACK_START);
                g2.fill(new Ellipse2D.Float(leftX - CIRCLE_R, LINE_Y - CIRCLE_R, CIRCLE_R * 2, CIRCLE_R * 2));
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f));
                g2.draw(new Ellipse2D.Float(leftX - CIRCLE_R + 1, LINE_Y - CIRCLE_R + 1, CIRCLE_R * 2 - 2, CIRCLE_R * 2 - 2));

                // ── Right node ───────────────────────────────────────────
                g2.setColor(TRACK_END);
                g2.fill(new Ellipse2D.Float(rightX - CIRCLE_R, LINE_Y - CIRCLE_R, CIRCLE_R * 2, CIRCLE_R * 2));
                g2.setColor(Color.WHITE);
                g2.draw(new Ellipse2D.Float(rightX - CIRCLE_R + 1, LINE_Y - CIRCLE_R + 1, CIRCLE_R * 2 - 2, CIRCLE_R * 2 - 2));

                // ── km label (above the track line, centered) ────────────
                if (!kmText.isEmpty()) {
                    Font kmFont = new Font("Segoe UI", Font.BOLD, 11);
                    g2.setFont(kmFont);
                    FontMetrics kmFm = g2.getFontMetrics();
                    int midX = (leftX + rightX) / 2;
                    int kmW  = kmFm.stringWidth(kmText);
                    int kmX  = midX - kmW / 2;
                    int kmY  = LINE_Y - CIRCLE_R - 4;
                    g2.setColor(ON_SURF_VAR);
                    g2.drawString(kmText, kmX, kmY);
                }

                // ── Station name labels (below nodes) ────────────────────
                g2.setFont(FONT_STA);
                g2.setColor(ON_SURFACE);
                FontMetrics fm = g2.getFontMetrics();
                int textY = LINE_Y + CIRCLE_R + fm.getAscent() + 6;

                // Left: centered under left node, but clamped so it doesn't go off-screen
                int leftNameW  = fm.stringWidth(gaDiName);
                int leftTextX  = Math.max(0, leftX - leftNameW / 2);
                g2.drawString(gaDiName, leftTextX, textY);

                // Right: centered under right node, but clamped so it doesn't go off-screen
                int rightNameW = fm.stringWidth(gaDenName);
                int rightTextX = Math.min(w - rightNameW, rightX - rightNameW / 2);
                g2.drawString(gaDenName, rightTextX, textY);

                g2.dispose();
            }
        };
        track.setOpaque(false);
        // Fixed height; width is determined by the parent (BorderLayout CENTER)
        track.setPreferredSize(new Dimension(100, 80));
        track.setMinimumSize(new Dimension(100, 80));
        track.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        return track;
    }

    private JButton buildActionBtn(String text, Color bgColor, Color fgColor, boolean small) {
        JButton btn = new JButton(text) {
            boolean hovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hovered ? bgColor.darker() : bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setForeground(fgColor);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(44, 44));
        btn.setMaximumSize(new Dimension(44, 44));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return btn;
    }

    // ── Empty state ───────────────────────────────────────────────────────
    private JPanel buildEmptyState() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(0, 300));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));

        JLabel lbl = new JLabel("Không có tuyến đường nào phù hợp");
        lbl.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        lbl.setForeground(ON_SURF_VAR);
        panel.add(lbl);
        return panel;
    }

    // ====================================================================
    //  PAGINATION
    // ====================================================================

    private void rebuildPagination(int totalPages, int total) {
        paginationPanel.removeAll();

        if (total > 0) {
            JLabel info = new JLabel("Hiển thị trang " + currentPage + " / " + totalPages
                    + "  (" + total + " tuyến)");
            info.setFont(FONT_SMALL);
            info.setForeground(ON_SURF_VAR);
            paginationPanel.add(info);
        }

        if (totalPages > 1) {
            paginationPanel.add(Box.createHorizontalStrut(12));

            // Prev
            JButton btnPrev = makePaginBtn("←");
            btnPrev.setEnabled(currentPage > 1);
            btnPrev.addActionListener(e -> { currentPage--; refreshCards(); });
            paginationPanel.add(btnPrev);

            // Page number buttons (show up to 5)
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

            // Next
            JButton btnNext = makePaginBtn("→");
            btnNext.setEnabled(currentPage < totalPages);
            btnNext.addActionListener(e -> { currentPage++; refreshCards(); });
            paginationPanel.add(btnNext);
        }

        paginationPanel.revalidate();
        paginationPanel.repaint();
    }

    private JButton makePaginBtn(String label) {
        boolean isActive = Boolean.TRUE.equals(null); // placeholder
        JButton btn = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Object active = getClientProperty("active");
                if (Boolean.TRUE.equals(active)) {
                    g2.setColor(PRIMARY);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                } else if (getModel().isRollover()) {
                    g2.setColor(PRIMARY_LIGHT);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BOLD);
        btn.setForeground(Boolean.TRUE.equals(btn.getClientProperty("active")) ? Color.WHITE : ON_SURFACE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
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

    private void confirmDelete(Tuyen tuyen) {
        String gaDiName  = tuyen.getGaDi()  != null ? tuyen.getGaDi().getTenGa()  : "?";
        String gaDenName = tuyen.getGaDen() != null ? tuyen.getGaDen().getTenGa() : "?";

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn xóa tuyến\n"
                        + tuyen.getMaTuyen() + "  (" + gaDiName + " → " + gaDenName + ")?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            boolean ok = daoTuyen.delete(tuyen.getMaTuyen());
            if (ok) {
                loadData();
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Kh\u00F4ng th\u1EC3 x\u00F3a tuy\u1EBFn n\u00E0y. Tuy\u1EBFn c\u00F3 th\u1EC3 \u0111ang \u0111\u01B0\u1EE3c s\u1EED d\u1EE5ng trong l\u1ECBch ch\u1EA1y.",
                        "L\u1ED7i x\u00F3a",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
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
