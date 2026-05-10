package com.modules;

import com.dao.DAO_ChiTietDoanTau;
import com.dao.DAO_DoanTau;
import com.entity.ChiTietDoanTau;
import com.entity.DoanTau;
import com.enums.LoaiGhe;

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
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

public class QuanLyDoanTauModule extends JPanel implements AppModule {

    private Consumer<Object> callback;

    // --- DAOs ---
    private final DAO_DoanTau        daoDoanTau = new DAO_DoanTau();
    private final DAO_ChiTietDoanTau daoChiTiet = new DAO_ChiTietDoanTau();

    // --- Data ---
    static class DoanTauRow {
        final DoanTau doanTau;
        final int soToaGheCung;
        final int soToaGheMem;
        final int soToaGiuongNam;

        DoanTauRow(DoanTau dt, int gc, int gm, int gn) {
            this.doanTau        = dt;
            this.soToaGheCung   = gc;
            this.soToaGheMem    = gm;
            this.soToaGiuongNam = gn;
        }
        int tongSoToa() { return soToaGheCung + soToaGheMem + soToaGiuongNam; }
    }

    private final List<DoanTauRow> allData      = new ArrayList<>();
    private final List<DoanTauRow> filteredData = new ArrayList<>();

    // --- Design tokens ---
    private static final Color PRIMARY       = NotionTheme.ACCENT;
    private static final Color PRIMARY_HOVER = AppColors.PRIMARY_HOVER;
    private static final Color PRIMARY_LIGHT = NotionTheme.ACCENT_SOFT;
    private static final Color SURFACE       = NotionTheme.PAGE;
    private static final Color CARD_BG       = NotionTheme.CARD;
    private static final Color TEXT_MAIN     = NotionTheme.TEXT;
    private static final Color TEXT_MUTED    = NotionTheme.TEXT_MUTED;
    private static final Color OUTLINE       = NotionTheme.BORDER;
    private static final Color ERROR_FG      = AppColors.ERROR; // Danger
    private static final Color ACTIVE_GREEN  = new Color(0x1A, 0xAE, 0x39);
    private static final Color WARNING_FG    = AppColors.WARNING_DARK;

    // Badge Colors
    private static final Color C_CUNG   = new Color(0xDD, 0x5B, 0x00);
    private static final Color C_MEM    = new Color(0x00, 0x75, 0xDE);
    private static final Color C_GIUONG = ACTIVE_GREEN;

    private static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD, 26);
    private static final Font FONT_DESC   = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_BODY   = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_BOLD   = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_SMALL  = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_BTN    = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_CARD_TAG = new Font("Segoe UI", Font.BOLD, 12);

    // --- Widgets ---
    private JTextField txtSearch;
    private JCheckBox  cbGheCung, cbGheMem, cbGiuongNam;
    private JPanel     cardsPanel;
    private JScrollPane scrollPane;
    private JPanel     paginationPanel;
    private JLabel     lblStatTotal;
    private JLabel     lblStatActive;
    private JLabel     lblStatInactive;

    private int currentPage  = 1;
    private int rowsPerPage  = 8; // Auto calculates based on grid
    private int gridCols     = 2;
    private boolean isRefreshing = false;

    public QuanLyDoanTauModule() {
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
        filterCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 178));
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

    @Override public String getTitle() { return "Quản lý Đội Tàu"; }
    @Override public JPanel getView()  { return this; }
    @Override public void setOnResult(Consumer<Object> cb) { this.callback = cb; }
    @Override public void reset() {
        if (txtSearch != null) txtSearch.setText("");
        if (cbGheCung != null) {
            cbGheCung.setSelected(true);
            cbGheMem.setSelected(true);
            cbGiuongNam.setSelected(true);
        }
        loadData();
    }

    // ====================================================================
    //  UI BUILD
    // ====================================================================

    private JPanel buildHeader() {
        // Header đổi sang hero card Notion để đồng bộ nhóm Dữ liệu tàu, không chạm layout card bên dưới.
        // Rủi ro: chiều cao header cố định theo padding; nếu subtitle dài hơn cần kiểm tra khi cửa sổ hẹp.
        JPanel hdr = new JPanel(new BorderLayout(24, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, NotionTheme.NAVY,
                        getWidth(), getHeight(), new Color(0x56, 0x45, 0xD4));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                g2.setColor(AppColors.withAlpha(NotionTheme.PEACH, 140));
                g2.fillRoundRect(getWidth() - 250, -48, 210, 180, 52, 52);
                g2.setColor(AppColors.withAlpha(NotionTheme.SKY, 122));
                g2.fillOval(getWidth() - 405, 80, 170, 170);
                g2.setColor(AppColors.withAlpha(Color.WHITE, 44));
                g2.fillRoundRect(getWidth() - 330, 42, 120, 18, 18, 18);
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

        JLabel lblTitle = new JLabel("Danh mục đội tàu");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblDesc = new JLabel("Quản lý cấu hình chi tiết và thành phần tổ hợp toa của toàn bộ đội tàu.");
        lblDesc.setFont(FONT_DESC);
        lblDesc.setForeground(AppColors.withAlpha(Color.WHITE, 205));
        lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        left.add(lblEyebrow);
        left.add(Box.createVerticalStrut(8));
        left.add(lblTitle);
        left.add(Box.createVerticalStrut(8));
        left.add(lblDesc);

        JButton btnAddNew = new JButton("+ Thiết lập đội tàu");
        NotionTheme.stylePrimaryButton(btnAddNew);
        btnAddNew.setPreferredSize(new Dimension(190, 42));
        btnAddNew.addActionListener(e -> openNewModule());

        JPanel rightBox = new JPanel(new GridBagLayout());
        rightBox.setOpaque(false);
        rightBox.add(btnAddNew);

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
        row.add(buildStatCard("Đội tàu hiện tại", lblStatTotal, PRIMARY, NotionTheme.ACCENT_SOFT));
        row.add(buildStatCard("Đang hoạt động", lblStatActive, ACTIVE_GREEN, NotionTheme.MINT));
        row.add(buildStatCard("Ngừng hoạt động", lblStatInactive, AppColors.ERROR_DARK, NotionTheme.ROSE));
        return row;
    }

    private JPanel buildStatCard(String label, JLabel value, Color accent, Color tint) {
        // Stat card bê pattern Khách hàng để phần trên của tab lưới có cùng nhịp thị giác.
        // Rủi ro: số liệu tạm cập nhật sau loadData; nếu thêm filter mới phải gọi updateTopStats cùng applyFilter.
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

        cardsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
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
                int cardW = 340; // Wider cards for train makeup details
                int cols = Math.max(1, (w - 15) / (cardW + 20));
                int rH = 185 + 20;
                int rows = Math.max(2, h / rH);
                int newItems = cols * rows;
                
                if (gridCols != cols) {
                    gridCols = cols;
                }
                
                if (newItems != rowsPerPage) {
                    rowsPerPage = newItems;
                    if (!isRefreshing) refreshCards();
                } else if (!isRefreshing) {
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
        // Filter card giữ search full-width và nhóm checkbox bên dưới để tránh vỡ layout khi resize.
        // Rủi ro: checkbox là Swing native nên chỉ đồng bộ spacing/màu, chưa custom indicator trong đợt này.
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
        JLabel title = new JLabel("Bộ lọc đội tàu");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(TEXT_MAIN);
        JLabel subtitle = new JLabel("Tìm đội tàu và lọc nhanh theo nhóm loại toa đang có");
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
        txtSearch.setFont(FONT_BODY);
        txtSearch.setOpaque(false);
        txtSearch.setBorder(BorderFactory.createEmptyBorder());
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm mã hoặc tên đội tàu...");
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
            cbGheCung.setSelected(true);
            cbGheMem.setSelected(true);
            cbGiuongNam.setSelected(true);
            applyFilter();
        });

        searchRow.add(searchBox, BorderLayout.CENTER);

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.add(searchRow, BorderLayout.NORTH);

        JPanel pFilters = new JPanel(new GridBagLayout());
        pFilters.setOpaque(false);
        pFilters.setAlignmentX(Component.LEFT_ALIGNMENT);
        pFilters.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        GridBagConstraints filterGbc = new GridBagConstraints();
        filterGbc.gridy = 0;
        filterGbc.fill = GridBagConstraints.HORIZONTAL;
        filterGbc.anchor = GridBagConstraints.NORTHWEST;
        filterGbc.insets = new Insets(0, 0, 0, 12);

        cbGheCung   = new JCheckBox("Ghế cứng", true);
        cbGheMem    = new JCheckBox("Ghế mềm", true);
        cbGiuongNam = new JCheckBox("Giường nằm", true);

        int filterCol = 0;
        for (JCheckBox cb : new JCheckBox[]{cbGheCung, cbGheMem, cbGiuongNam}) {
            cb.setFont(FONT_BODY);
            cb.setForeground(TEXT_MAIN);
            cb.setOpaque(false);
            cb.setFocusPainted(false);
            cb.addActionListener(e -> applyFilter());
            filterGbc.gridx = filterCol++;
            filterGbc.weightx = 1.0;
            pFilters.add(createFilterGroup("Loại toa", cb), filterGbc);
        }
        filterGbc.gridx = filterCol;
        filterGbc.weightx = 0.0;
        filterGbc.insets = new Insets(0, 0, 0, 0);
        pFilters.add(FilterActionGroup.wrap(btnClear), filterGbc);
        // Handoff: checkbox filter chuyển sang grid có label trên field để không bị che trong MenuModule.
        // Cảnh báo: Bỏ lọc chỉ reset checkbox, không xóa thanh tìm kiếm.
        body.add(pFilters, BorderLayout.CENTER);
        wrapper.add(body, BorderLayout.CENTER);
        return wrapper;
    }

    // ====================================================================
    //  DATA OPERATIONS
    // ====================================================================

    private void loadData() {
        allData.clear();
        List<DoanTau> dsDoanTau = daoDoanTau.getAll();
        List<ChiTietDoanTau> dsChiTiet = daoChiTiet.getAll();

        Map<String, int[]> countMap = new HashMap<>();
        for (ChiTietDoanTau ct : dsChiTiet) {
            if (ct.getDoanTau() == null || ct.getToaTau() == null) continue;
            String ma = ct.getDoanTau().getMaDoanTau();
            countMap.putIfAbsent(ma, new int[3]);
            LoaiGhe lg = ct.getToaTau().getLoaiGhe();
            if (lg != null) {
                switch (lg) {
                    case GHE_CUNG   -> countMap.get(ma)[0]++;
                    case GHE_MEM    -> countMap.get(ma)[1]++;
                    case GIUONG_NAM -> countMap.get(ma)[2]++;
                }
            }
        }

        for (DoanTau dt : dsDoanTau) {
            int[] c = countMap.getOrDefault(dt.getMaDoanTau(), new int[3]);
            allData.add(new DoanTauRow(dt, c[0], c[1], c[2]));
        }

        applyFilter();
    }

    private void applyFilter() {
        String kw      = txtSearch.getText().trim().toLowerCase();
        boolean showGC = cbGheCung.isSelected();
        boolean showGM = cbGheMem.isSelected();
        boolean showGN = cbGiuongNam.isSelected();
        boolean allOn  = showGC && showGM && showGN;
        boolean noneOn = !showGC && !showGM && !showGN;
        filteredData.clear();
        for (DoanTauRow row : allData) {
            String ma  = row.doanTau.getMaDoanTau()  != null ? row.doanTau.getMaDoanTau().toLowerCase()  : "";
            String ten = row.doanTau.getTenDoanTau() != null ? row.doanTau.getTenDoanTau().toLowerCase() : "";
            if (!kw.isEmpty() && !ma.contains(kw) && !ten.contains(kw)) continue;

            if (!allOn && !noneOn) {
                boolean match = (showGC && row.soToaGheCung > 0)
                             || (showGM && row.soToaGheMem > 0)
                             || (showGN && row.soToaGiuongNam > 0);
                if (!match) continue;
            }
            filteredData.add(row);
        }

        if (lblStatTotal != null) {
            lblStatTotal.setText(String.valueOf(filteredData.size()));
            lblStatActive.setText(String.valueOf(filteredData.stream().filter(r -> "Đang hoạt động".equals(normalizeStatus(r.doanTau.getTrangThai()))).count()));
            lblStatInactive.setText(String.valueOf(filteredData.stream().filter(r -> "Ngừng hoạt động".equals(normalizeStatus(r.doanTau.getTrangThai()))).count()));
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
            List<DoanTauRow> pageData = filteredData.subList(start, end);

            int w = scrollPane.getWidth();
            int cardW = 340;
            if (gridCols > 0) {
                cardW = (w - (gridCols + 1) * 20) / gridCols;
                cardW = Math.max(cardW, 300);
            }

            cardsPanel.removeAll();
            cardsPanel.setPreferredSize(new Dimension(w - 25, 0));
            if (pageData.isEmpty()) {
                cardsPanel.add(buildEmptyState());
            } else {
                for (DoanTauRow r : pageData) {
                    cardsPanel.add(buildDoanTauCard(r, cardW));
                }
            }
            cardsPanel.revalidate();
            cardsPanel.repaint();

            rebuildPagination(totalPages, total);
        } finally {
            isRefreshing = false;
        }
    }

    // =====================================================================
    //  Actions
    // =====================================================================

    private void openEditModule(DoanTau doanTau) {
        Container parent = this.getParent();
        if (parent == null) return;

        ChinhSuaDoanTauModule editModule = new ChinhSuaDoanTauModule(doanTau);
        editModule.setOnResult(result -> {
            parent.remove(editModule);
            parent.add(this, BorderLayout.CENTER);
            loadData();
            parent.revalidate();
            parent.repaint();
        });

        parent.remove(this);
        parent.add(editModule, BorderLayout.CENTER);
        parent.revalidate();
        parent.repaint();
    }

    private void openNewModule() {
        Container parent = this.getParent();
        if (parent == null) return;

        ChinhSuaDoanTauModule newModule = new ChinhSuaDoanTauModule(null);
        newModule.setOnResult(result -> {
            parent.remove(newModule);
            parent.add(this, BorderLayout.CENTER);
            loadData();
            parent.revalidate();
            parent.repaint();
        });

        parent.remove(this);
        parent.add(newModule, BorderLayout.CENTER);
        parent.revalidate();
        parent.repaint();
    }

    private void deleteRow(DoanTauRow row) {
        int confirm = NotionMessageDialog.showConfirmDialog(this,
                "Xóa đoàn tàu “" + row.doanTau.getTenDoanTau() + "”?\n"
                + "Tất cả chi tiết thành phần toa sẽ bị hủy bỏ.",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        String maDoanTau = row.doanTau.getMaDoanTau();
        int lichRefs = daoDoanTau.countLichReferences(maDoanTau);
        if (lichRefs > 0) {
            NotionMessageDialog.showMessageDialog(this,
                    "Không thể xóa đoàn tàu đang được gán cho lịch chạy.\n"
                            + "Bạn có thể chuyển đoàn tàu sang trạng thái Ngừng hoạt động.",
                    "Không thể xóa", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!daoDoanTau.deleteWithDetails(maDoanTau)) {
            NotionMessageDialog.showMessageDialog(this,
                    "Không thể xóa đoàn tàu. Vui lòng thử lại.",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        loadData();
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) return "Đang hoạt động";
        String normalized = status.trim().toLowerCase();
        if (normalized.contains("ngừng") || normalized.contains("ngung") || normalized.contains("khai thác") || normalized.contains("khai thac")) {
            return "Ngừng hoạt động";
        }
        return "Đang hoạt động";
    }

    private JLabel buildStatusBadge(String status) {
        String normalized = normalizeStatus(status);
        boolean stopped = "Ngừng hoạt động".equals(normalized);
        return createBadge(normalized, stopped ? NotionTheme.ROSE : NotionTheme.MINT, stopped ? AppColors.ERROR_DARK : ACTIVE_GREEN);
    }

    private JLabel createBadge(String text, Color bg, Color fg) {
        JLabel badge = new JLabel(text == null || text.isBlank() ? "--" : " " + text + " ") {
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
        badge.setFont(FONT_CARD_TAG);
        badge.setForeground(fg);
        badge.setBorder(new EmptyBorder(0, 14, 0, 14));
        badge.setHorizontalAlignment(SwingConstants.CENTER);
        badge.setVerticalAlignment(SwingConstants.CENTER);
        badge.setPreferredSize(new Dimension(142, 34));
        badge.setMinimumSize(new Dimension(126, 34));
        return badge;
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
        chip.setFont(FONT_CARD_TAG);
        chip.setForeground(TEXT_MUTED);
        chip.setBorder(new EmptyBorder(0, 12, 0, 12));
        chip.setHorizontalAlignment(SwingConstants.CENTER);
        chip.setPreferredSize(new Dimension(176, 34));
        chip.setMinimumSize(new Dimension(142, 34));
        // Handoff: các tag đầu card dùng cùng font/height để mã, trạng thái và nút không lệch nhịp.
        // Nếu muốn mã nổi bật hơn, đổi màu/weight nhẹ chứ tránh quay lại monospace lớn gây thô.
        return chip;
    }

    private void addHeaderCell(JPanel header, JComponent component, int gridx, double weightx, Insets insets) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gridx;
        gbc.gridy = 0;
        gbc.weightx = weightx;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = insets;
        header.add(component, gbc);
    }

    private JButton createStatusToggleButton(DoanTauRow row) {
        String status = normalizeStatus(row.doanTau.getTrangThai());
        boolean stopped = "Ngừng hoạt động".equals(status);
        String next = stopped ? "Đang hoạt động" : "Ngừng hoạt động";

        JButton btn = createIconButton(LineIcons.Name.EDIT, "Đổi trạng thái", AppColors.WARNING_LIGHT);
        btn.setToolTipText("Chuyển sang: " + next);
        btn.addActionListener(e -> {
            int confirm = NotionMessageDialog.showConfirmDialog(this,
                    "Chuyển trạng thái đoàn tàu “" + row.doanTau.getTenDoanTau() + "” sang “" + next + "”?",
                    "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;

            if (!daoDoanTau.updateTrangThai(row.doanTau.getMaDoanTau(), next)) {
                NotionMessageDialog.showMessageDialog(this,
                        "Không thể cập nhật trạng thái đoàn tàu.",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            loadData();
        });
        return btn;
    }

    // =====================================================================
    //  Card Builder
    // =====================================================================

    private Color resolveStatusAccent(String status) {
        String normalized = normalizeStatus(status);
        return "Ngừng hoạt động".equals(normalized) ? AppColors.ERROR_DARK : ACTIVE_GREEN;
    }

    private JPanel buildDoanTauCard(DoanTauRow row, int width) {
        final Color statusAccent = resolveStatusAccent(row.doanTau.getTrangThai());
        final Color stripAccent = AppColors.withAlpha(statusAccent, 145);
        final boolean configured = row.tongSoToa() > 0;
        JPanel card = new JPanel(new BorderLayout()) {
            boolean isHovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { isHovered = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { isHovered = false; repaint(); }
                    @Override public void mouseClicked(MouseEvent e) {
                        if (SwingUtilities.isLeftMouseButton(e)) openEditModule(row.doanTau);
                    }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int w = getWidth();
                int h = getHeight();
                
                // Shadow
                g2.setColor(new Color(0, 0, 0, isHovered ? 14 : 7));
                g2.fillRoundRect(3, 4, w - 6, h - 5, 20, 20);
                
                // Card Background
                g2.setColor(CARD_BG);
                g2.fillRoundRect(2, 0, w - 6, h - 6, 20, 20);
                
                // Accent code border
                g2.setColor(isHovered ? AppColors.withAlpha(statusAccent, 150) : OUTLINE);
                g2.setStroke(new BasicStroke(isHovered ? 1.5f : 1f));
                g2.drawRoundRect(2, 0, w - 6, h - 6, 20, 20);

                RoundRectangle2D.Float clipRect = new RoundRectangle2D.Float(2, 0, w - 6, h - 6, 20, 20);
                g2.clip(clipRect);
                g2.setColor(stripAccent);
                g2.fillRect(2, 0, 6, h);

                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(width, 185));
        card.setMaximumSize(new Dimension(width, 185));
        card.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setOpaque(false);

        JPanel header = new JPanel(new GridBagLayout());
        header.setOpaque(false);

        JButton btnEdit = buildTextActionButton("Chỉnh sửa", PRIMARY, PRIMARY_LIGHT);
        btnEdit.addActionListener(e -> openEditModule(row.doanTau));
        btnEdit.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { e.consume(); }
        });

        JLabel codeChip = createCodeChip(row.doanTau.getMaDoanTau());
        JLabel statusBadge = buildStatusBadge(row.doanTau.getTrangThai());
        addHeaderCell(header, codeChip, 0, 1.0, new Insets(0, 0, 0, 10));
        addHeaderCell(header, statusBadge, 1, 0.0, new Insets(0, 0, 0, 10));
        addHeaderCell(header, btnEdit, 2, 0.0, new Insets(0, 0, 0, 0));
        // Handoff: hàng top card dùng GridBag 3 vùng để mã/trạng thái/nút không đè nhau khi card co.
        // Nếu thêm action mới, tăng chiều cao/card width hoặc chuyển action xuống hàng riêng thay vì ép vào đây.
        content.add(header, BorderLayout.NORTH);

        JPanel main = new JPanel(new BorderLayout(18, 0));
        main.setOpaque(false);

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);
        JLabel lblName = new JLabel(row.doanTau.getTenDoanTau() != null ? row.doanTau.getTenDoanTau() : "Chưa đặt tên đoàn tàu");
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 21));
        lblName.setForeground(TEXT_MAIN);
        lblName.setAlignmentX(Component.LEFT_ALIGNMENT);
        String dauMay = row.doanTau.getDauMay() != null ? row.doanTau.getDauMay().getMaDauMay() : "Chưa gán đầu máy";
        JLabel lblMeta = new JLabel("Đầu máy: " + dauMay);
        lblMeta.setFont(FONT_SMALL);
        lblMeta.setForeground(TEXT_MUTED);
        lblMeta.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(lblName);
        info.add(Box.createVerticalStrut(6));
        info.add(lblMeta);
        main.add(info, BorderLayout.CENTER);

        JPanel summary = new JPanel();
        summary.setLayout(new BoxLayout(summary, BoxLayout.Y_AXIS));
        summary.setOpaque(false);
        summary.setPreferredSize(new Dimension(150, 0));
        JLabel lblTotal = new JLabel(row.tongSoToa() + " toa");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTotal.setForeground(statusAccent);
        lblTotal.setAlignmentX(Component.RIGHT_ALIGNMENT);
        JLabel lblSummary = new JLabel(configured ? "Đã có thành phần" : "Cần cấu hình toa");
        lblSummary.setFont(FONT_SMALL);
        lblSummary.setForeground(TEXT_MUTED);
        lblSummary.setAlignmentX(Component.RIGHT_ALIGNMENT);
        summary.add(lblTotal);
        summary.add(Box.createVerticalStrut(4));
        summary.add(lblSummary);
        main.add(summary, BorderLayout.EAST);
        content.add(main, BorderLayout.CENTER);

        JPanel chips = new JPanel(new GridLayout(1, 3, 8, 0));
        chips.setOpaque(false);
        if (!configured) {
            chips.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
            chips.add(createBadge("Chưa cấu hình toa", NotionTheme.YELLOW, WARNING_FG));
        } else {
            chips.add(buildChip(row.soToaGheCung + " Ghế cứng", C_CUNG));
            chips.add(buildChip(row.soToaGheMem + " Ghế mềm", C_MEM));
            chips.add(buildChip(row.soToaGiuongNam + " Giường nằm", C_GIUONG));
        }
        content.add(chips, BorderLayout.SOUTH);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JButton buildTextActionButton(String text, Color accent, Color tint) {
        JButton button = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? tint : CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(AppColors.withAlpha(accent, getModel().isRollover() ? 150 : 90));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(FONT_CARD_TAG);
        button.setForeground(accent);
        button.setPreferredSize(new Dimension(126, 34));
        button.setMinimumSize(new Dimension(116, 34));
        button.setMaximumSize(new Dimension(126, 34));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JPanel buildChip(String text, Color accent) {
        Color tint = resolveCompositionTint(accent);
        JPanel p = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(tint);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(AppColors.withAlpha(accent, 100));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 18, 18);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(7, 10, 7, 10));
        p.setPreferredSize(new Dimension(124, 36));

        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(accent);
        p.add(lbl, BorderLayout.CENTER);
        return p;
    }

    private Color resolveCompositionTint(Color accent) {
        if (C_MEM.equals(accent)) return new Color(0xE5, 0xF2, 0xFF);
        if (C_GIUONG.equals(accent)) return NotionTheme.MINT;
        return new Color(0xFF, 0xEF, 0xDF);
    }

    private JButton createIconButton(LineIcons.Name iconName, String tip, Color hoverBg) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover() || getModel().isPressed()) {
                    g2.setColor(hoverBg);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        ImageIcon ico = loadScaledIcon(iconName, 16);
        btn.setIcon(ico);
        
        btn.setPreferredSize(new Dimension(32, 32));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setToolTipText(tip);
        return btn;
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
    //  PAGINATION
    // ====================================================================

    private void rebuildPagination(int totalPages, int total) {
        paginationPanel.removeAll();

        if (total > 0) {
            JLabel info = new JLabel("Hiển thị trang " + currentPage + " / " + totalPages + " (" + total + " đội tàu)");
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

    private JPanel createFilterGroup(String label, JComponent input) {
        JPanel group = new JPanel(new BorderLayout(0, 6));
        group.setOpaque(false);
        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(FONT_CARD_TAG);
        lbl.setForeground(TEXT_MUTED);
        input.setPreferredSize(new Dimension(Math.max(108, input.getPreferredSize().width), 40));
        input.setMinimumSize(new Dimension(108, 40));
        group.add(lbl, BorderLayout.NORTH);
        group.add(input, BorderLayout.CENTER);
        return group;
        // Handoff: label đặt trên field để các filter cùng hàng không bị ép chiều ngang.
        // Cảnh báo: checkbox vẫn dùng state native, chỉ đổi wrapper visual/layout.
    }

    private JButton createSecondaryButton(String text, int width, int height) {
        // Nút phụ dùng cho filter card: nền trắng, border mảnh, không cạnh tranh với nút primary ở hero.
        // Rủi ro: helper chỉ tối ưu cho button ngắn; nếu text dài cần truyền width lớn hơn.
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? NotionTheme.CARD_MUTED : NotionTheme.CARD);
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
