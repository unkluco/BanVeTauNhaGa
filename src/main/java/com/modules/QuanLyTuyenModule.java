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
    private static final Color PRIMARY       = NotionTheme.ACCENT;
    private static final Color PRIMARY_HOVER = AppColors.PRIMARY_HOVER;
    private static final Color PRIMARY_LIGHT = AppColors.PRIMARY_LIGHT;
    private static final Color SURFACE       = AppColors.BACKGROUND;  // Very light gray
    private static final Color CARD_BG       = AppColors.SURFACE;
    private static final Color TEXT_MAIN     = AppColors.TEXT_PRIMARY;
    private static final Color TEXT_MUTED    = AppColors.TEXT_SECONDARY;
    private static final Color OUTLINE       = AppColors.BORDER;
    private static final Color DANGER        = AppColors.ERROR;
    private static final Color DANGER_LIGHT  = AppColors.ERROR_LIGHT;
    private static final Color WARN_BG       = AppColors.WARNING_LIGHT;
    private static final Color WARN_FG       = AppColors.WARNING_DARK;
    private static final Color ACTIVE_GREEN  = new Color(0x1A, 0xAE, 0x39);

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

    // ── Widgets ───────────────────────────────────────────────────────────
    private SearchableComboBox<Ga> filterGaDi;
    private SearchableComboBox<Ga> filterGaDen;
    private JComboBox<String>      filterTrangThai;
    private JPanel                 cardsPanel;
    private JScrollPane            scrollPane;
    private JLabel                 lblStatTotal;
    private JLabel                 lblStatActive;
    private JLabel                 lblStatInactive;

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
        if (filterTrangThai != null) filterTrangThai.setSelectedIndex(0);
        if (cardsPanel != null) applyFilter();
    }

    // ====================================================================
    //  UI BUILD
    // ====================================================================

    private void buildUI() {
        setLayout(new BorderLayout());
        setBackground(SURFACE);
        setBorder(new EmptyBorder(28, 36, 28, 36));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);
        JPanel header = buildHeader();
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        JPanel stats = buildStatsRow();
        stats.setAlignmentX(Component.LEFT_ALIGNMENT);
        stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));
        JPanel filterCard = buildFilterBar();
        filterCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        NotionTheme.lockMaxWidthToPreferredHeight(filterCard);
        // Handoff: vùng lọc tự theo preferred height để không bị thiếu chiều cao trên máy scale khác.
        // Cảnh báo: header/stat vẫn giữ size chung của các tab quản lý cho đồng bộ thị giác.
        top.add(header);
        top.add(Box.createVerticalStrut(16));
        top.add(stats);
        top.add(Box.createVerticalStrut(16));
        top.add(filterCard);
        top.add(Box.createVerticalStrut(16));

        add(top, BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);

        loadGaFilters();
        loadData();
    }

    // ── Header ────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        // Header đổi sang hero card Notion để đồng bộ nhóm Dữ liệu tàu, giữ nguyên ticket card tuyến bên dưới.
        // Rủi ro: tuyến có title dài hơn nên không nhồi icon vào title để tránh cắt chữ khi resize.
        JPanel hdr = new JPanel(new BorderLayout(24, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, NotionTheme.NAVY,
                        getWidth(), getHeight(), new Color(0x00, 0x75, 0xDE));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                g2.setColor(AppColors.withAlpha(NotionTheme.MINT, 145));
                g2.fillRoundRect(getWidth() - 230, -36, 170, 170, 42, 42);
                g2.setColor(AppColors.withAlpha(NotionTheme.PEACH, 120));
                g2.fillOval(getWidth() - 390, 76, 160, 160);
                g2.setColor(AppColors.withAlpha(Color.WHITE, 45));
                g2.fillRoundRect(getWidth() - 325, 44, 150, 14, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        hdr.setOpaque(false);
        hdr.setBorder(new EmptyBorder(24, 28, 24, 28));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel lblEyebrow = new JLabel("WORKSPACE / DỮ LIỆU TÀU");
        lblEyebrow.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblEyebrow.setForeground(AppColors.withAlpha(Color.WHITE, 175));
        lblEyebrow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTitle = new JLabel("Quản lý tuyến đường");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblDesc = new JLabel("Quản lý và thiết lập lộ trình các ga tàu một cách trực quan.");
        lblDesc.setFont(FONT_DESC);
        lblDesc.setForeground(AppColors.withAlpha(Color.WHITE, 205));
        lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        left.add(lblEyebrow);
        left.add(Box.createVerticalStrut(8));
        left.add(lblTitle);
        left.add(Box.createVerticalStrut(8));
        left.add(lblDesc);

        JButton btnAdd = new JButton("+ Tạo tuyến mới");
        NotionTheme.stylePrimaryButton(btnAdd);
        btnAdd.setPreferredSize(new Dimension(180, 42));
        btnAdd.addActionListener(e -> openAddDialog());

        JPanel rightBox = new JPanel(new GridBagLayout());
        rightBox.setOpaque(false);
        rightBox.add(btnAdd);
        hdr.add(left, BorderLayout.CENTER);
        hdr.add(rightBox, BorderLayout.EAST);
        return hdr;
    }

    private JPanel buildStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 12, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));

        lblStatTotal = new JLabel("0");
        lblStatActive = new JLabel("0");
        lblStatInactive = new JLabel("0");

        row.add(buildStatCard("Tuyến hiện tại", lblStatTotal, PRIMARY, NotionTheme.ACCENT_SOFT));
        row.add(buildStatCard("Đang hoạt động", lblStatActive, ACTIVE_GREEN, NotionTheme.MINT));
        row.add(buildStatCard("Tạm ngưng", lblStatInactive, AppColors.ERROR_DARK, NotionTheme.ROSE));
        return row;
    }

    private JPanel buildStatCard(String label, JLabel value, Color accent, Color tint) {
        // Stat card copy pattern Khách hàng: nền pastel toàn thẻ, marker dọc và số lớn ở trên.
        // Rủi ro: giữ 3 thẻ cố định cao 92px nên label tuyến cần ngắn để không cắt chữ.
        JPanel card = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(tint);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(AppColors.withAlpha(accent, 80));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(14, 18, 14, 18));

        JPanel marker = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
            }
        };
        marker.setOpaque(false);
        marker.setPreferredSize(new Dimension(8, 48));

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setOpaque(false);
        value.setFont(new Font("Segoe UI", Font.BOLD, 24));
        value.setForeground(accent);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(TEXT_MUTED);
        text.add(value);
        text.add(Box.createVerticalStrut(2));
        text.add(lbl);

        card.add(marker, BorderLayout.WEST);
        card.add(text, BorderLayout.CENTER);
        return card;
    }

    // ── Content ──────────────────────────────────────────────────────────
    private JPanel buildContent() {
        JPanel content = new JPanel(new BorderLayout(0, 20));
        content.setOpaque(true);
        content.setBackground(SURFACE);

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

        content.add(scrollPane, BorderLayout.CENTER);
        

        return content;
    }

    // ── Filter Bar ────────────────────────────────────────────────────────
    private JPanel buildFilterBar() {
        // Filter card tuyến giữ hai combo ga trên một hàng rõ ràng, không chỉnh ticket card bên dưới.
        // Rủi ro: SearchableComboBox có popup riêng nên không ép custom renderer trong đợt restyle header/filter.
        JPanel wrapper = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                NotionTheme.paintCard(g2, this, CARD_BG, OUTLINE, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        wrapper.setLayout(new BorderLayout(0, 12));
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(18, 22, 18, 22));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Bộ lọc tuyến đường");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(TEXT_MAIN);
        JLabel subtitle = new JLabel("Chọn ga đi và ga đến để thu hẹp danh sách tuyến");
        subtitle.setFont(FONT_SMALL);
        subtitle.setForeground(TEXT_MUTED);
        header.add(title, BorderLayout.WEST);
        header.add(subtitle, BorderLayout.EAST);
        wrapper.add(header, BorderLayout.NORTH);

        JPanel searchRow = new JPanel(new BorderLayout(10, 0));
        searchRow.setOpaque(false);
        searchRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JPanel routeBox = new JPanel(new GridBagLayout());
        routeBox.setOpaque(false);
        routeBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1, true),
                new EmptyBorder(9, 14, 9, 14)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 12);

        filterGaDi = createGaCombo("Tìm ga đi...");
        filterGaDi.setPreferredSize(new Dimension(360, 38));
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        routeBox.add(filterGaDi, gbc);

        JLabel arrow = new JLabel("→", SwingConstants.CENTER);
        arrow.setFont(new Font("Segoe UI", Font.BOLD, 16));
        arrow.setForeground(TEXT_MUTED);
        arrow.setPreferredSize(new Dimension(32, 38));
        gbc.gridx = 1;
        gbc.weightx = 0;
        routeBox.add(arrow, gbc);

        filterGaDen = createGaCombo("Tìm ga đến...");
        filterGaDen.setPreferredSize(new Dimension(360, 38));
        gbc.gridx = 2;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 0, 12);
        routeBox.add(filterGaDen, gbc);

        filterTrangThai = createStatusFilterCombo();
        gbc.gridx = 3;
        gbc.weightx = 0;
        gbc.insets = new Insets(0, 0, 0, 0);
        routeBox.add(filterTrangThai, gbc);

        JButton btnClear = createSecondaryButton("Bỏ lọc", 104, 38);
        btnClear.addActionListener(e -> {
            filterGaDi.clearSelection();
            filterGaDen.clearSelection();
            filterTrangThai.setSelectedIndex(0);
            applyFilter();
        });

        searchRow.add(routeBox, BorderLayout.CENTER);
        searchRow.add(btnClear, BorderLayout.EAST);
        wrapper.add(searchRow, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildFilterGroup(String labelText, JComponent field) {
        JPanel group = new JPanel();
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
        group.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(FONT_SMALL);
        lbl.setForeground(TEXT_MUTED);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        group.add(lbl);
        group.add(Box.createVerticalStrut(4));
        group.add(field);
        return group;
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

    private JComboBox<String> createStatusFilterCombo() {
        JComboBox<String> combo = new JComboBox<>(new String[]{"Tất cả trạng thái", "Đang hoạt động", "Tạm ngưng"});
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setForeground(TEXT_MAIN);
        combo.setBackground(CARD_BG);
        combo.setPreferredSize(new Dimension(170, 38));
        combo.setFocusable(false);
        NotionTheme.applyComboBoxSelection(combo);
        combo.addActionListener(e -> applyFilter());
        return combo;
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
        String selectedStatus = filterTrangThai == null ? "Tất cả trạng thái" : (String) filterTrangThai.getSelectedItem();
        boolean filterStatus = selectedStatus != null && !"Tất cả trạng thái".equals(selectedStatus);

        filteredData = new ArrayList<>();
        for (Tuyen t : allData) {
            boolean okDi  = (selGaDi  == null) || (t.getGaDi()  != null && t.getGaDi().getMaGa().equals(selGaDi.getMaGa()));
            boolean okDen = (selGaDen == null) || (t.getGaDen() != null && t.getGaDen().getMaGa().equals(selGaDen.getMaGa()));
            boolean okStatus = !filterStatus || selectedStatus.equals(t.isHoatDong() ? "Đang hoạt động" : "Tạm ngưng");
            if (okDi && okDen && okStatus) filteredData.add(t);
        }

        if (lblStatTotal != null) {
            lblStatTotal.setText(String.valueOf(filteredData.size()));
            lblStatActive.setText(String.valueOf(filteredData.stream().filter(Tuyen::isHoatDong).count()));
            lblStatInactive.setText(String.valueOf(filteredData.stream().filter(t -> !t.isHoatDong()).count()));
        }

        refreshCards();
    }

    private void refreshCards() {
        cardsPanel.removeAll();
        if (filteredData.isEmpty()) {
            cardsPanel.add(buildEmptyState());
        } else {
            for (Tuyen t : filteredData) {
                cardsPanel.add(buildTicketCard(t));
                cardsPanel.add(Box.createVerticalStrut(15));
            }
        }
        cardsPanel.revalidate();
        cardsPanel.repaint();
    }


    // ====================================================================
    //  TICKET CARD BUILDER
    // ====================================================================

    private JPanel buildTicketCard(Tuyen tuyen) {
        final boolean isActive = tuyen.isHoatDong();
        final Color stripColor = isActive ? ACTIVE_GREEN : AppColors.ERROR_DARK;
        final Color stripAccent = AppColors.withAlpha(stripColor, 145);

        JPanel card = new JPanel(new BorderLayout()) {
            boolean isHovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { isHovered = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { isHovered = false; repaint(); }
                    @Override public void mouseClicked(MouseEvent e) {
                        if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) openEditDialog(tuyen);
                    }
                    @Override public void mousePressed(MouseEvent e) { showTuyenQuickActions(e, tuyen); }
                    @Override public void mouseReleased(MouseEvent e) { showTuyenQuickActions(e, tuyen); }
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
                g2.setColor(CARD_BG);
                g2.fillRoundRect(2, 0, w - 6, h - 6, 20, 20);

                // Border
                g2.setColor(isHovered ? AppColors.withAlpha(stripColor, 150) : OUTLINE);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(2, 0, w - 6, h - 6, 20, 20);

                // Left accent strip
                g2.clip(new RoundRectangle2D.Float(2, 0, w - 6, h - 6, 20, 20));
                g2.setColor(stripAccent);
                g2.fillRect(2, 0, 8, h);

                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, CARD_HEIGHT));
        card.setPreferredSize(new Dimension(0, CARD_HEIGHT));
        card.setBorder(new EmptyBorder(15, 30, 15, 20));

        // ── Main Content Container ─────────────────────────────────────
        JPanel content = new JPanel(new BorderLayout(24, 0));
        content.setOpaque(false);

        JPanel leftColumn = new JPanel(new BorderLayout());
        leftColumn.setOpaque(false);
        leftColumn.setPreferredSize(new Dimension(420, 0));

        JPanel badgeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        badgeRow.setOpaque(false);

        JLabel lblCode = createCodeChip(tuyen.getMaTuyen());
        badgeRow.add(lblCode);

        JLabel lblStatus = createStatusBadge(isActive ? "Đang hoạt động" : "Tạm ngưng", isActive ? NotionTheme.MINT : NotionTheme.ROSE, stripColor);
        badgeRow.add(lblStatus);

        leftColumn.add(badgeRow, BorderLayout.NORTH);
        leftColumn.add(buildGaInfo(tuyen.getGaDi(), "Ga Đi", true), BorderLayout.CENTER);

        JPanel middle = buildTrackUI(tuyen);
        middle.setPreferredSize(new Dimension(480, 90));
        middle.setMinimumSize(new Dimension(420, 90));

        JPanel rightColumn = new JPanel(new BorderLayout());
        rightColumn.setOpaque(false);
        rightColumn.setPreferredSize(new Dimension(420, 0));
        rightColumn.add(buildGaInfo(tuyen.getGaDen(), "Ga Đến", false), BorderLayout.CENTER);

        // Ticket card dùng 3 vùng cố định để track/km luôn nằm cùng trục giữa mọi dòng.
        // Rủi ro: nếu cửa sổ rất hẹp, hai cột ga co trước nhưng vùng track vẫn giữ tối thiểu để không lệch km.
        content.add(leftColumn, BorderLayout.WEST);
        content.add(middle, BorderLayout.CENTER);
        content.add(rightColumn, BorderLayout.EAST);

        card.add(content, BorderLayout.CENTER);

        // ── Actions Panel (Right edge) ──────────────────────────────────
        JPanel actions = new JPanel(new GridBagLayout());
        actions.setOpaque(false);
        actions.setBorder(new EmptyBorder(0, 15, 0, 0));

        JButton btnEdit = buildTextActionButton("Chỉnh sửa", stripColor, isActive ? NotionTheme.MINT : NotionTheme.ROSE);
        btnEdit.addActionListener(e -> openEditDialog(tuyen));
        btnEdit.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { e.consume(); }
        });
        actions.add(btnEdit);

        card.add(actions, BorderLayout.EAST);
        // Handoff: vùng card tuyến chỉ mở sửa bằng double-click, thống nhất với các tab tàu.
        // Nút sửa riêng vẫn single-click để giữ thao tác nhanh khi người dùng chọn đúng icon.

        return card;
    }

    private JLabel createCodeChip(String text) {
        JLabel chip = new JLabel(text == null || text.isBlank() ? "--" : " " + text + " ") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(NotionTheme.CARD_MUTED);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(OUTLINE);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        chip.setFont(new Font("Consolas", Font.BOLD, 14));
        chip.setForeground(TEXT_MUTED);
        chip.setBorder(new EmptyBorder(4, 8, 4, 8));
        return chip;
    }

    private JLabel createStatusBadge(String text, Color bg, Color fg) {
        JLabel badge = new JLabel(" " + text + " ") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(AppColors.withAlpha(fg, 60));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        badge.setForeground(fg);
        badge.setBorder(new EmptyBorder(4, 6, 4, 6));
        return badge;
    }

    private JPanel buildGaInfo(Ga ga, String typeLabel, boolean leftAlign) {
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
        JComponent stationIcon = new StationDotIcon(14, TEXT_MUTED);
        
        JLabel lblAddr = new JLabel(" " + diaChi + " ");
        lblAddr.setFont(FONT_SMALL);
        lblAddr.setForeground(TEXT_MUTED);
        
        if (leftAlign) {
            locPanel.add(stationIcon);
            locPanel.add(lblAddr);
        } else {
            locPanel.add(lblAddr);
            locPanel.add(stationIcon);
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

                // Draw vector train marker directly to avoid clipped/pixelated SVG edges in route cards.
                int ix = (w - 30) / 2;
                int iy = midY - 15;
                g2.setColor(CARD_BG);
                g2.fillOval(ix - 5, iy - 5, 40, 40);
                paintTrainMarker(g2, ix, iy);
                
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

    private void paintTrainMarker(Graphics2D g2, int x, int y) {
        g2 = (Graphics2D) g2.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setColor(TEXT_MAIN);
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawRoundRect(x + 4, y + 8, 22, 13, 3, 3);
        g2.drawLine(x + 8, y + 5, x + 22, y + 5);
        g2.drawLine(x + 10, y + 5, x + 14, y + 8);
        g2.drawLine(x + 18, y + 5, x + 22, y + 8);
        g2.drawLine(x + 8, y + 14, x + 22, y + 14);
        g2.drawLine(x + 3, y + 24, x + 27, y + 24);
        g2.drawLine(x + 8, y + 21, x + 5, y + 24);
        g2.drawLine(x + 22, y + 21, x + 25, y + 24);
        g2.dispose();
    }

    private static class StationDotIcon extends JComponent {
        private final int size;
        private final Color color;

        StationDotIcon(int size, Color color) {
            this.size = size;
            this.color = color;
            setPreferredSize(new Dimension(size, size));
            setMinimumSize(new Dimension(size, size));
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            int cx = size / 2;
            int cy = size / 2;
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawOval(2, 2, size - 5, size - 5);
            g2.fillOval(cx - 2, cy - 2, 4, 4);
            g2.drawLine(cx - 3, size - 2, cx + 3, size - 2);
            g2.dispose();
        }
    }

    private JButton buildTextActionButton(String text, Color fg, Color hoverBg) {
        // Nút action ngoài card tuyến đổi màu theo trạng thái để khớp badge/strip của chính card đó.
        // Rủi ro: nếu thêm action khác cần đưa vào dialog chi tiết hoặc menu, không đặt thêm icon ngoài card.
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() || getModel().isPressed() ? hoverBg : NotionTheme.CARD_MUTED);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(AppColors.withAlpha(fg, 70));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(fg);
        btn.setPreferredSize(new Dimension(96, 34));
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

    private void showTuyenQuickActions(MouseEvent e, Tuyen tuyen) {
        if (!e.isPopupTrigger()) return;
        JPopupMenu menu = new JPopupMenu();
        JMenuItem edit = new JMenuItem("Sửa");
        edit.addActionListener(ev -> openEditDialog(tuyen));
        JMenuItem toggle = new JMenuItem(tuyen.isHoatDong() ? "Ngưng hoạt động" : "Kích hoạt");
        toggle.addActionListener(ev -> toggleHoatDong(tuyen));
        menu.add(edit);
        menu.addSeparator();
        menu.add(toggle);
        menu.show(e.getComponent(), e.getX(), e.getY());
    }

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
        int choice = NotionMessageDialog.showConfirmDialog(this, msg, title,
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) return;
        boolean ok = daoTuyen.setHoatDong(tuyen.getMaTuyen(), !current);
        if (ok) loadData();
        else NotionMessageDialog.showMessageDialog(this, "Không thể cập nhật trạng thái tuyến.", "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    private JButton createSecondaryButton(String text, int width, int height) {
        // Nút phụ trong filter card dùng nền trắng và border mảnh để không cạnh tranh với CTA ở hero.
        // Rủi ro: helper chỉ tối ưu cho nhãn ngắn; nếu thêm icon/text dài cần tăng width.
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? NotionTheme.CARD_MUTED : AppColors.SURFACE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(OUTLINE);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BOLD);
        btn.setForeground(TEXT_MUTED);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(width, height));
        return btn;
    }

    private ImageIcon loadScaledIcon(LineIcons.Name iconName, int size) {
        ImageIcon icon = LineIcons.image(iconName, size);
        // Handoff: module icons now use LineIcons enum directly, avoiding legacy SVG path strings.
        // Risk: keep visual QA at small sizes when adding new icon names.
        return icon;
    }
}
