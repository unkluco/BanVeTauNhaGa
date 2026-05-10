package com.modules;

import com.dao.DAO_DauMay;
import com.entity.DauMay;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class QuanLyDauMayModule extends JPanel implements AppModule {
    private static final String[] STATUS_OPTIONS = {
            "Đang hoạt động", "Đang bảo trì", "Ngừng hoạt động"
    };

    // ── Design tokens ────────────────────────────────────────────────────
    private static final Color PRIMARY       = NotionTheme.ACCENT;
    private static final Color PRIMARY_HOVER = NotionTheme.ACCENT_HOVER;
    private static final Color PRIMARY_LIGHT = NotionTheme.ACCENT_SOFT;
    private static final Color SURFACE       = NotionTheme.PAGE;
    private static final Color CARD_BG       = NotionTheme.CARD;
    private static final Color TEXT_MAIN     = NotionTheme.TEXT;
    private static final Color TEXT_MUTED    = NotionTheme.TEXT_MUTED;
    private static final Color OUTLINE       = NotionTheme.BORDER;
    private static final Color ACTIVE_GREEN  = new Color(0x1A, 0xAE, 0x39);

    private static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD, 26);
    private static final Font FONT_DESC   = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_BOLD   = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_STA    = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font FONT_SMALL  = new Font("Segoe UI", Font.PLAIN, 12);

    // ── DAOs ─────────────────────────────────────────────────────────────
    private final DAO_DauMay daoDauMay = new DAO_DauMay();

    // ── State ─────────────────────────────────────────────────────────────
    private Consumer<Object> callback;
    private List<DauMay> allData      = new ArrayList<>();
    private List<DauMay> filteredData = new ArrayList<>();
    private int currentPage  = 1;
    private int rowsPerPage  = 12; // Grid items
    private int gridCols     = 3;
    private boolean isRefreshing = false;

    // ── Widgets ───────────────────────────────────────────────────────────
    private JTextField txtSearch;
    private JComboBox<String> cboHangSanXuat;
    private JComboBox<String> cboTrangThai;
    private JLabel     lblStatTotal;
    private JLabel     lblStatActive;
    private JLabel     lblStatMaintenance;
    private JLabel     lblStatInactive;
    private JPanel     cardsPanel;
    private JScrollPane scrollPane;
    private JPanel     paginationPanel;

    public QuanLyDauMayModule() {
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
        filterCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 172));
        top.add(header);
        top.add(Box.createVerticalStrut(16));
        top.add(stats);
        top.add(Box.createVerticalStrut(16));
        top.add(filterCard);
        top.add(Box.createVerticalStrut(16));

        add(top, BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);

        loadData();
    }

    @Override public String getTitle() { return "Quản lý Đầu máy"; }
    @Override public JPanel getView()  { return this; }
    @Override public void setOnResult(Consumer<Object> cb) { this.callback = cb; }

    @Override
    public void reset() {
        if (txtSearch != null) txtSearch.setText("");
        loadData();
    }

    // ====================================================================
    //  UI BUILD
    // ====================================================================

    private JPanel buildHeader() {
        // Header đổi sang hero card Notion để đồng bộ nhóm Dữ liệu tàu, giữ nguyên grid đầu máy bên dưới.
        // Rủi ro: vùng trang trí phụ thuộc width; khi panel quá hẹp vẫn phải ưu tiên title và action button.
        JPanel hdr = new JPanel(new BorderLayout(24, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, NotionTheme.NAVY,
                        getWidth(), getHeight(), new Color(0xDD, 0x5B, 0x00));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                g2.setColor(AppColors.withAlpha(NotionTheme.YELLOW, 150));
                g2.fillOval(getWidth() - 150, -82, 210, 210);
                g2.setColor(AppColors.withAlpha(NotionTheme.PEACH, 132));
                g2.fillRoundRect(getWidth() - 340, 70, 210, 58, 16, 16);
                g2.setColor(AppColors.withAlpha(Color.WHITE, 45));
                g2.fillRoundRect(getWidth() - 285, 28, 140, 16, 16, 16);
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

        JLabel lblTitle = new JLabel("Quản lý đầu máy");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblDesc = new JLabel("Duyệt và kiểm soát các đầu máy kéo tàu trong hệ thống.");
        lblDesc.setFont(FONT_DESC);
        lblDesc.setForeground(AppColors.withAlpha(Color.WHITE, 205));
        lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        left.add(lblEyebrow);
        left.add(Box.createVerticalStrut(8));
        left.add(lblTitle);
        left.add(Box.createVerticalStrut(8));
        left.add(lblDesc);

        JButton btnAddNew = createPrimaryButton("+ Thêm đầu máy");
        btnAddNew.setPreferredSize(new Dimension(180, 42));
        btnAddNew.addActionListener(e -> openCreateDauMayDialog());

        JPanel rightBox = new JPanel(new GridBagLayout());
        rightBox.setOpaque(false);
        rightBox.add(btnAddNew);
        hdr.add(left, BorderLayout.CENTER);
        hdr.add(rightBox, BorderLayout.EAST);
        return hdr;
    }

    private JPanel buildStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));
        lblStatTotal = new JLabel("0");
        lblStatActive = new JLabel("0");
        lblStatMaintenance = new JLabel("0");
        lblStatInactive = new JLabel("0");
        row.add(buildStatCard("Đầu máy hiện tại", lblStatTotal, PRIMARY, NotionTheme.ACCENT_SOFT));
        row.add(buildStatCard("Đang hoạt động", lblStatActive, ACTIVE_GREEN, NotionTheme.MINT));
        row.add(buildStatCard("Đang bảo trì", lblStatMaintenance, AppColors.WARNING_DARK, NotionTheme.YELLOW));
        row.add(buildStatCard("Ngừng hoạt động", lblStatInactive, AppColors.ERROR_DARK, NotionTheme.ROSE));
        return row;
    }

    private JPanel buildStatCard(String label, JLabel value, Color accent, Color tint) {
        // Stat card copy pattern Khách hàng để top area không lệch giữa các tab quản lý.
        // Rủi ro: trạng thái là String tự do nên thống kê phải normalize trước khi so sánh.
        JPanel card = new JPanel(new BorderLayout(10, 0)) {
            @Override protected void paintComponent(Graphics g) {
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
            @Override protected void paintComponent(Graphics g) {
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

    private JPanel buildContent() {
        JPanel content = new JPanel(new BorderLayout(0, 20));
        content.setOpaque(true);
        content.setBackground(SURFACE);

        cardsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        cardsPanel.setOpaque(true);
        cardsPanel.setBackground(SURFACE);

        scrollPane = new JScrollPane(cardsPanel);
        scrollPane.setOpaque(true);
        scrollPane.setBackground(SURFACE);
        scrollPane.getViewport().setOpaque(true);
        scrollPane.getViewport().setBackground(SURFACE);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        scrollPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int w = scrollPane.getWidth();
                int h = scrollPane.getHeight();
                int cardW = 300;
                int cols = Math.max(1, (w - 15) / (cardW + 15));
                int rH = 214 + 15;
                int rows = Math.max(2, h / rH);
                int newItems = cols * rows;
                
                if (gridCols != cols) {
                    gridCols = cols;
                }
                
                if (newItems != rowsPerPage) {
                    rowsPerPage = newItems;
                    if (!isRefreshing) refreshCards();
                } else if (!isRefreshing) {
                    // Just update layout
                    refreshCards();
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

    private JPanel buildFilterBar() {
        // Filter card chỉ chỉnh vùng điều khiển phía trên, không can thiệp luồng render card đầu máy.
        // Rủi ro: search realtime đang bao phủ nhiều field, nên placeholder cần nói rõ phạm vi tìm kiếm.
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
        JLabel title = new JLabel("Bộ lọc đầu máy");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(TEXT_MAIN);
        JLabel subtitle = new JLabel("Tìm theo mã, tên, hãng sản xuất, trạng thái hoặc công suất");
        subtitle.setFont(FONT_SMALL);
        subtitle.setForeground(TEXT_MUTED);
        header.add(title, BorderLayout.WEST);
        header.add(subtitle, BorderLayout.EAST);
        wrapper.add(header, BorderLayout.NORTH);

        JPanel searchRow = new JPanel(new BorderLayout(10, 0));
        searchRow.setOpaque(false);
        searchRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JPanel searchBox = new JPanel(new BorderLayout(10, 0));
        searchBox.setOpaque(false);
        searchBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1, true),
                new EmptyBorder(9, 14, 9, 14)
        ));

        JLabel iconSearch = new JLabel();
        ImageIcon icoSearch = loadScaledIcon(LineIcons.Name.SEARCH, 18);
        if (icoSearch != null) iconSearch.setIcon(icoSearch);
        searchBox.add(iconSearch, BorderLayout.WEST);

        txtSearch = new JTextField();
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtSearch.setOpaque(false);
        txtSearch.setBorder(BorderFactory.createEmptyBorder());
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm theo mã hoặc tên đầu máy...");
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });
        txtSearch.addActionListener(e -> applyFilter());
        searchBox.add(txtSearch, BorderLayout.CENTER);
        SearchFieldClearButton.install(searchBox, txtSearch, this::applyFilter);

        JButton btnClear = createSecondaryButton("Bỏ lọc", 104, 40);
        btnClear.addActionListener(e -> {
            if (cboHangSanXuat != null) cboHangSanXuat.setSelectedIndex(0);
            if (cboTrangThai != null) cboTrangThai.setSelectedIndex(0);
            applyFilter();
        });

        searchRow.add(searchBox, BorderLayout.CENTER);

        cboHangSanXuat = createFilterCombo(new String[]{"Tất cả hãng"});
        cboHangSanXuat.addActionListener(e -> applyFilter());
        cboTrangThai = createFilterCombo(new String[]{"Tất cả trạng thái", STATUS_OPTIONS[0], STATUS_OPTIONS[1], STATUS_OPTIONS[2]});
        cboTrangThai.addActionListener(e -> applyFilter());

        JPanel optionRow = new JPanel(new GridBagLayout());
        optionRow.setOpaque(false);
        optionRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        optionRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        GridBagConstraints filterGbc = new GridBagConstraints();
        filterGbc.gridy = 0;
        filterGbc.fill = GridBagConstraints.HORIZONTAL;
        filterGbc.anchor = GridBagConstraints.NORTHWEST;
        filterGbc.weightx = 1.0;
        filterGbc.insets = new Insets(0, 0, 0, 12);
        filterGbc.gridx = 0;
        optionRow.add(createFilterGroup("Hãng sản xuất", cboHangSanXuat), filterGbc);
        filterGbc.gridx = 1;
        optionRow.add(createFilterGroup("Trạng thái", cboTrangThai), filterGbc);
        filterGbc.gridx = 2;
        filterGbc.weightx = 0.0;
        filterGbc.insets = new Insets(0, 0, 0, 0);
        optionRow.add(FilterActionGroup.wrap(btnClear), filterGbc);
        // Handoff: filter row dùng grid để các control cùng baseline/chiều cao.
        // Cảnh báo: thanh tìm kiếm vẫn nằm riêng, nút Bỏ lọc không clear txtSearch.

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.add(searchRow, BorderLayout.NORTH);
        body.add(optionRow, BorderLayout.CENTER);
        wrapper.add(body, BorderLayout.CENTER);
        return wrapper;
    }

    // ====================================================================
    //  DATA OPERATIONS
    // ====================================================================

    private void loadData() {
        allData = daoDauMay.getAll();
        refreshHangSanXuatFilter();
        applyFilter();
    }

    private void applyFilter() {
        String q = txtSearch.getText().trim().toLowerCase();
        String selectedMaker = cboHangSanXuat == null ? "Tất cả hãng" : (String) cboHangSanXuat.getSelectedItem();
        String selectedStatus = cboTrangThai == null ? "Tất cả trạng thái" : (String) cboTrangThai.getSelectedItem();
        boolean filterMaker = selectedMaker != null && !"Tất cả hãng".equals(selectedMaker);
        boolean filterStatus = selectedStatus != null && !"Tất cả trạng thái".equals(selectedStatus);

        filteredData = new ArrayList<>();
        for (DauMay dm : allData) {
            boolean matchSearch = q.isEmpty()
                    || safeContains(dm.getMaDauMay(), q)
                    || safeContains(dm.getTenDauMay(), q)
                    || safeContains(dm.getHangSanXuat(), q)
                    || safeContains(dm.getTrangThai(), q)
                    || safeContains(dm.getMoTa(), q)
                    || (dm.getNamSanXuat() != null && String.valueOf(dm.getNamSanXuat()).contains(q))
                    || (dm.getCongSuatKw() != null && String.valueOf(dm.getCongSuatKw()).contains(q));
            boolean matchMaker = !filterMaker || selectedMaker.equals(normalizeMaker(dm.getHangSanXuat()));
            boolean matchStatus = !filterStatus || selectedStatus.equals(normalizeStatus(dm.getTrangThai()));
            if (matchSearch && matchMaker && matchStatus) filteredData.add(dm);
        }

        if (lblStatTotal != null) {
            lblStatTotal.setText(String.valueOf(filteredData.size()));
            lblStatActive.setText(String.valueOf(filteredData.stream().filter(dm -> STATUS_OPTIONS[0].equals(normalizeStatus(dm.getTrangThai()))).count()));
            lblStatMaintenance.setText(String.valueOf(filteredData.stream().filter(dm -> STATUS_OPTIONS[1].equals(normalizeStatus(dm.getTrangThai()))).count()));
            lblStatInactive.setText(String.valueOf(filteredData.stream().filter(dm -> STATUS_OPTIONS[2].equals(normalizeStatus(dm.getTrangThai()))).count()));
        }
        currentPage = 1;
        refreshCards();
    }

    private void refreshHangSanXuatFilter() {
        // Combo hãng lấy từ dữ liệu hiện tại để lọc card mà không làm lệch search/pagination.
        // Rủi ro: hãng trống được gom vào "Chưa cập nhật", nên cần normalize thống nhất với applyFilter.
        if (cboHangSanXuat == null) return;
        Object current = cboHangSanXuat.getSelectedItem();
        java.util.TreeSet<String> makers = new java.util.TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (DauMay dm : allData) {
            makers.add(normalizeMaker(dm.getHangSanXuat()));
        }

        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        model.addElement("Tất cả hãng");
        for (String maker : makers) model.addElement(maker);
        cboHangSanXuat.setModel(model);
        if (current != null) cboHangSanXuat.setSelectedItem(current);
        if (cboHangSanXuat.getSelectedIndex() < 0) cboHangSanXuat.setSelectedIndex(0);
    }

    private String normalizeMaker(String maker) {
        return maker == null || maker.isBlank() ? "Chưa cập nhật" : maker.trim();
    }

    private void refreshCards() {
        isRefreshing = true;
        try {
            int total = filteredData.size();
            int totalPages = (total == 0) ? 1 : (int) Math.ceil((double) total / rowsPerPage);
            if (currentPage > totalPages) currentPage = totalPages;

            int start = (currentPage - 1) * rowsPerPage;
            int end   = Math.min(start + rowsPerPage, total);
            List<DauMay> pageData = filteredData.subList(start, end);

            // Calculate card width
            int w = scrollPane.getWidth();
            int cardW = 280;
            if (gridCols > 0) {
                // Adjust width to fill container better (optional)
                cardW = (w - (gridCols + 1) * 15) / gridCols;
                cardW = Math.max(cardW, 270);
            }

            cardsPanel.removeAll();
            cardsPanel.setPreferredSize(new Dimension(w - 20, 0)); // force flow layout wrap
            if (pageData.isEmpty()) {
                cardsPanel.add(buildEmptyState());
            } else {
                for (DauMay m : pageData) {
                    cardsPanel.add(buildDauMayCard(m, cardW));
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
    //  CARD BUILDER
    // ====================================================================

    private JPanel buildDauMayCard(DauMay dm, int width) {
        String status = normalizeStatus(dm.getTrangThai());
        Color accent = resolveStatusAccent(status);
        Color stripAccent = AppColors.withAlpha(accent, 150);
        JPanel card = new JPanel(new BorderLayout()) {
            boolean isHovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { isHovered = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { isHovered = false; repaint(); }
                    @Override public void mouseClicked(MouseEvent e) {
                        if (SwingUtilities.isLeftMouseButton(e)) openEditDauMayDialog(dm);
                    }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                g2.setColor(new Color(0, 0, 0, isHovered ? 16 : 7));
                g2.fillRoundRect(4, 6, w - 8, h - 8, 18, 18);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(2, 0, w - 6, h - 8, 18, 18);
                RoundRectangle2D.Float clipRect = new RoundRectangle2D.Float(2, 0, w - 6, h - 8, 18, 18);
                g2.clip(clipRect);
                g2.setColor(stripAccent);
                g2.fillRect(2, 0, 6, h);
                g2.setColor(isHovered ? AppColors.withAlpha(accent, 150) : OUTLINE);
                g2.setStroke(new BasicStroke(isHovered ? 1.4f : 1f));
                g2.drawRoundRect(2, 0, w - 6, h - 8, 18, 18);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(width, 214));
        card.setMaximumSize(new Dimension(width, 214));
        card.setBorder(new EmptyBorder(18, 22, 18, 18));

        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setOpaque(false);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel lblCode = createCodeChip(dm.getMaDauMay());

        JPanel topLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topLeft.setOpaque(false);
        topLeft.add(lblCode);

        JButton btnEdit = createCardTextActionButton("Chỉnh sửa");
        btnEdit.addActionListener(e -> openEditDauMayDialog(dm));
        btnEdit.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { e.consume(); }
        });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        actions.setOpaque(false);
        actions.add(btnEdit);
        top.add(topLeft, BorderLayout.WEST);
        top.add(actions, BorderLayout.EAST);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        JLabel lblMaker = new JLabel(ellipsize(
                dm.getHangSanXuat() == null ? "CHƯA CẬP NHẬT HÃNG SẢN XUẤT" : dm.getHangSanXuat().toUpperCase(),
                36));
        lblMaker.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblMaker.setForeground(TEXT_MUTED);
        lblMaker.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblName = new JLabel(ellipsize(dm.getTenDauMay(), 30));
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblName.setForeground(TEXT_MAIN);
        lblName.setAlignmentX(Component.LEFT_ALIGNMENT);

        String desc = dm.getMoTa() == null || dm.getMoTa().isBlank()
                ? "Thông tin vận hành đang được cập nhật"
                : dm.getMoTa();
        JLabel lblDesc = new JLabel(ellipsize(desc, 46));
        lblDesc.setFont(FONT_SMALL);
        lblDesc.setForeground(TEXT_MUTED);
        lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        body.add(lblMaker);
        body.add(Box.createVerticalStrut(8));
        body.add(lblName);
        body.add(Box.createVerticalStrut(8));
        body.add(lblDesc);

        JPanel footer = new JPanel(new BorderLayout(10, 0));
        footer.setOpaque(false);
        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        chips.setOpaque(false);
        chips.add(createInfoChip(dm.getNamSanXuat() != null ? String.valueOf(dm.getNamSanXuat()) : "N/A", NotionTheme.CARD_MUTED, TEXT_MUTED));
        chips.add(createInfoChip(dm.getCongSuatKw() != null ? dm.getCongSuatKw() + " kW" : "Chưa rõ kW", NotionTheme.SKY, new Color(0x00, 0x75, 0xDE)));
        footer.add(chips, BorderLayout.WEST);
        footer.add(createStatusBadge(status), BorderLayout.EAST);

        content.add(top, BorderLayout.NORTH);
        content.add(body, BorderLayout.CENTER);
        content.add(footer, BorderLayout.SOUTH);
        card.add(content, BorderLayout.CENTER);
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

    private JLabel createInfoChip(String text, Color bg, Color fg) {
        JLabel chip = new JLabel(text == null || text.isBlank() ? "--" : " " + text + " ") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 9, 9);
                g2.setColor(AppColors.withAlpha(fg, 60));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 9, 9);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        chip.setFont(new Font("Segoe UI", Font.BOLD, 11));
        chip.setForeground(fg);
        chip.setBorder(new EmptyBorder(4, 8, 4, 8));
        return chip;
    }

    private JLabel createStatusBadge(String status) {
        JLabel badge = createInfoChip(status, resolveStatusBg(status), resolveStatusFg(status));
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        return badge;
    }

    private Color resolveStatusAccent(String status) {
        // Card đầu máy dùng accent theo trạng thái để scan nhanh mà vẫn giữ palette pastel Notion.
        // Rủi ro: trạng thái mới sẽ về MINT như mặc định hoạt động, cần cập nhật map nếu thêm trạng thái.
        String normalized = status == null ? "" : status.trim().toLowerCase();
        if (normalized.contains("bảo trì") || normalized.contains("bao tri")) return AppColors.WARNING_DARK;
        if (normalized.contains("ngừng") || normalized.contains("ngung")) return AppColors.ERROR_DARK;
        return ACTIVE_GREEN;
    }

    private String ellipsize(String text, int maxChars) {
        if (text == null) return "";
        if (text.length() <= maxChars) return text;
        if (maxChars <= 1) return text.substring(0, 1);
        if (maxChars <= 3) return text.substring(0, maxChars);
        return text.substring(0, maxChars - 3) + "...";
    }

    private JPanel buildEmptyState() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        JLabel lbl = new JLabel("Không tìm thấy kết quả nào!");
        lbl.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        lbl.setForeground(TEXT_MUTED);
        p.add(lbl);
        return p;
    }

    // ====================================================================
    //  ACTIONS
    // ====================================================================

    private void openEditDauMayDialog(DauMay dm) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        DauMay editable = new DauMay(
                dm.getMaDauMay(),
                dm.getTenDauMay(),
                dm.getHangSanXuat(),
                dm.getNamSanXuat(),
                dm.getCongSuatKw(),
                dm.getTrangThai(),
                dm.getMoTa()
        );

        SuaDauMayDialog dialog = new SuaDauMayDialog(owner, editable, updated -> {
            boolean ok = daoDauMay.update(updated);
            if (ok) {
                loadData();
            } else {
                NotionMessageDialog.showMessageDialog(this,
                        "Không thể cập nhật đầu máy. Vui lòng kiểm tra schema dữ liệu.",
                        "Cập nhật thất bại", JOptionPane.ERROR_MESSAGE);
            }
        });
        dialog.setVisible(true);
    }

    private void openCreateDauMayDialog() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        String maDauMay = daoDauMay.generateNextMaDauMay();

        SuaDauMayDialog dialog = new SuaDauMayDialog(owner, maDauMay, created -> {
            boolean ok = daoDauMay.insert(created);
            if (ok) {
                loadData();
            } else {
                NotionMessageDialog.showMessageDialog(this,
                        "Không thể thêm đầu máy mới. Vui lòng kiểm tra dữ liệu nhập.",
                        "Tạo đầu máy thất bại", JOptionPane.ERROR_MESSAGE);
            }
        });
        dialog.setVisible(true);
    }

    private void openChangeDauMayStatusDialog(DauMay dm) {
        if (dm == null || dm.getMaDauMay() == null || dm.getMaDauMay().isBlank()) return;

        JComboBox<String> cbo = new JComboBox<>(STATUS_OPTIONS);
        cbo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbo.setSelectedItem(normalizeStatus(dm.getTrangThai()));

        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.add(new JLabel("Cập nhật trạng thái cho " + dm.getMaDauMay() + ":"), BorderLayout.NORTH);
        panel.add(cbo, BorderLayout.CENTER);

        int confirmed = NotionMessageDialog.showConfirmDialog(
                this,
                panel,
                "Đổi trạng thái đầu máy",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (confirmed != JOptionPane.OK_OPTION) return;

        String newStatus = (String) cbo.getSelectedItem();
        if (newStatus == null || normalizeStatus(dm.getTrangThai()).equals(newStatus)) {
            return;
        }

        boolean ok = daoDauMay.updateTrangThai(dm.getMaDauMay(), newStatus);
        if (ok) {
            loadData();
        } else {
            NotionMessageDialog.showMessageDialog(this,
                    "Không thể cập nhật trạng thái đầu máy.",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean safeContains(String src, String keyword) {
        return src != null && src.toLowerCase().contains(keyword);
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) return STATUS_OPTIONS[0];
        String normalized = status.trim().toLowerCase();
        if (normalized.contains("ngừng") || normalized.contains("ngung")) return STATUS_OPTIONS[2];
        if (normalized.contains("bảo trì") || normalized.contains("bao tri")) return STATUS_OPTIONS[1];
        return STATUS_OPTIONS[0];
    }

    private Color resolveStatusBg(String status) {
        String normalized = status == null ? "" : status.trim().toLowerCase();
        if (normalized.contains("bảo trì") || normalized.contains("bao tri")) return NotionTheme.YELLOW;
        if (normalized.contains("ngừng") || normalized.contains("ngung")) return NotionTheme.ROSE;
        return NotionTheme.MINT;
    }

    private Color resolveStatusFg(String status) {
        String normalized = status == null ? "" : status.trim().toLowerCase();
        if (normalized.contains("bảo trì") || normalized.contains("bao tri")) return AppColors.WARNING_DARK;
        if (normalized.contains("ngừng") || normalized.contains("ngung")) return AppColors.ERROR_DARK;
        return ACTIVE_GREEN;
    }

    // ====================================================================
    //  PAGINATION
    // ====================================================================

    private void rebuildPagination(int totalPages, int total) {
        paginationPanel.removeAll();

        if (total > 0) {
            JLabel info = new JLabel("Hiển thị trang " + currentPage + " / " + totalPages + " (" + total + " đầu máy)");
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
                    btn.setForeground(AppColors.SURFACE);
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

    private JButton createCardTextActionButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() || getModel().isPressed() ? PRIMARY_LIGHT : NotionTheme.CARD_MUTED);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 9, 9);
                g2.setColor(AppColors.withAlpha(PRIMARY, 70));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 9, 9);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setForeground(PRIMARY);
        btn.setPreferredSize(new Dimension(92, 30));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) g2.setColor(PRIMARY_HOVER);
                else if (getModel().isRollover()) g2.setColor(PRIMARY.brighter());
                else g2.setColor(PRIMARY);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BOLD);
        btn.setForeground(AppColors.SURFACE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel createFilterGroup(String label, JComponent input) {
        // Nhóm filter giữ label + input cùng nhịp với search bar, tránh combo bị trôi giữa card.
        // Rủi ro: width cố định tối ưu cho filter ngắn; thêm nhiều filter cần chuyển sang GridBag để responsive hơn.
        JPanel group = new JPanel(new BorderLayout(0, 6));
        group.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXT_MUTED);
        group.add(lbl, BorderLayout.NORTH);
        group.add(input, BorderLayout.CENTER);
        group.setPreferredSize(new Dimension(320, 60));
        return group;
        // Handoff: chuyển label lên trên để filter card cùng phong cách các module nghiệp vụ.
        // Cảnh báo: width cố định vẫn phục vụ layout card đầu máy; chỉ đổi trục label/control.
    }

    private JComboBox<String> createFilterCombo(String[] values) {
        JComboBox<String> combo = new JComboBox<>(values);
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setForeground(TEXT_MAIN);
        combo.setBackground(CARD_BG);
        combo.setPreferredSize(new Dimension(210, 38));
        combo.setFocusable(false);
        return combo;
    }

    private JButton createSecondaryButton(String text, int width, int height) {
        // Nút phụ của filter card dùng nền trắng và border mảnh để giữ đúng phân cấp với primary action.
        // Rủi ro: helper chỉ tối ưu cho nhãn ngắn; text dài hoặc thêm icon cần tăng width truyền vào.
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


