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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
    private static final Font FONT_CARD_TAG = new Font("Segoe UI", Font.BOLD, 12);

    // --- Widgets ---
    private JTextField txtSearch;
    private JCheckBox  cbGheCung, cbGheMem, cbGiuongNam;
    private JPanel     cardsPanel;
    private JPanel     cardsHost;
    private JScrollPane scrollPane;
    private JLabel     lblStatTotal;
    private JLabel     lblStatActive;
    private JLabel     lblStatInactive;

    private int gridCols     = 3;

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
        NotionTheme.lockMaxWidthToPreferredHeight(filterCard);
        // Handoff: filter card tự khóa theo preferred height sau khi build xong để hợp nhiều DPI.
        // Cảnh báo: không đổi chiều cao card đoàn tàu trong grid vì cần đồng đều theo thiết kế.
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

        cardsPanel = new JPanel(new GridLayout(0, gridCols, 15, 15));
        cardsPanel.setOpaque(true);
        cardsPanel.setBackground(SURFACE);

        cardsHost = new JPanel(new BorderLayout());
        cardsHost.setOpaque(true);
        cardsHost.setBackground(SURFACE);
        cardsHost.add(cardsPanel, BorderLayout.NORTH);

        scrollPane = new JScrollPane(cardsHost);
        scrollPane.setOpaque(true);
        scrollPane.setBackground(SURFACE);
        scrollPane.getViewport().setOpaque(true);
        scrollPane.getViewport().setBackground(SURFACE);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        scrollPane.getViewport().addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) { updateGridColumns(); }
        });

        content.add(scrollPane, BorderLayout.CENTER);
        

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
        refreshCards();
    }

    private void refreshCards() {
        updateGridColumns();
        cardsPanel.removeAll();
        if (filteredData.isEmpty()) {
            cardsPanel.setLayout(new BorderLayout());
            cardsPanel.add(buildEmptyState());
        } else {
            cardsPanel.setLayout(new GridLayout(0, gridCols, 15, 15));
            for (DoanTauRow r : filteredData) {
                cardsPanel.add(buildDoanTauCard(r, 300));
            }
        }
        cardsPanel.revalidate();
        cardsPanel.repaint();
        cardsHost.revalidate();
        cardsHost.repaint();
    }

    private void updateGridColumns() {
        if (cardsPanel == null || scrollPane == null) return;
        if (filteredData.isEmpty()) return;
        int nextCols = 3;
        if (nextCols != gridCols || !(cardsPanel.getLayout() instanceof GridLayout)) {
            gridCols = nextCols;
            cardsPanel.setLayout(new GridLayout(0, gridCols, 15, 15));
            cardsPanel.revalidate();
        }
        // Handoff: đoàn tàu cố định 3 card mỗi hàng để đồng bộ mật độ hiển thị với yêu cầu nghiệp vụ.
        // Rủi ro: nếu container quá hẹp, cần đổi sang Math.max(1, width / 330) để tránh ép card.
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

    private void showDoanTauQuickActions(MouseEvent e, DoanTauRow row) {
        if (!e.isPopupTrigger()) return;
        JPopupMenu menu = new JPopupMenu();
        JMenuItem view = new JMenuItem("Xem chi tiết");
        view.addActionListener(ev -> openChiTiet(row));
        JMenuItem edit = new JMenuItem("Sửa");
        edit.addActionListener(ev -> openEditModule(row.doanTau));
        JMenuItem toggle = new JMenuItem("Đổi trạng thái hoạt động");
        toggle.addActionListener(ev -> toggleDoanTauTrangThai(row));
        menu.add(view);
        menu.add(edit);
        menu.addSeparator();
        menu.add(toggle);
        menu.show(e.getComponent(), e.getX(), e.getY());
    }

    private void toggleDoanTauTrangThai(DoanTauRow row) {
        String current = normalizeStatus(row.doanTau.getTrangThai());
        String next = "Ngừng hoạt động".equals(current) ? "Đang hoạt động" : "Ngừng hoạt động";
        int confirm = NotionMessageDialog.showConfirmDialog(this,
                "Chuyển đoàn tàu " + row.doanTau.getMaDoanTau() + " sang trạng thái " + next + "?",
                "Đổi trạng thái đoàn tàu", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        boolean ok = daoDoanTau.updateTrangThai(row.doanTau.getMaDoanTau(), next);
        if (ok) loadData();
        else NotionMessageDialog.showMessageDialog(this, "Không thể cập nhật trạng thái đoàn tàu.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        // Handoff: menu nhanh chỉ đổi trạng thái vận hành, không xóa thành phần toa hay lịch liên quan.
        // Cảnh báo: nếu bổ sung trạng thái bảo trì cho đoàn tàu, cần đổi menu từ toggle sang chọn trạng thái.
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
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(fg);
        badge.setBorder(new EmptyBorder(4, 10, 4, 10));
        badge.setHorizontalAlignment(SwingConstants.CENTER);
        badge.setVerticalAlignment(SwingConstants.CENTER);
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
        chip.setFont(new Font("Consolas", Font.BOLD, 14));
        chip.setForeground(TEXT_MUTED);
        chip.setBorder(new EmptyBorder(4, 8, 4, 8));
        chip.setHorizontalAlignment(SwingConstants.CENTER);
        // Handoff: chip mã đoàn tàu đã thu gọn theo mẫu card toa để header thoáng hơn khi chia 3 cột.
        // Cảnh báo: mã quá dài có thể cần tooltip hoặc ellipsis nếu đổi chuẩn mã.
        return chip;
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
        final Color stripAccent = AppColors.withAlpha(statusAccent, 170);
        final boolean configured = row.tongSoToa() > 0;
        JPanel card = new JPanel(new BorderLayout()) {
            boolean isHovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { isHovered = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { isHovered = false; repaint(); }
                    @Override public void mouseClicked(MouseEvent e) {
                        if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) openEditModule(row.doanTau);
                    }
                    @Override public void mousePressed(MouseEvent e) { showDoanTauQuickActions(e, row); }
                    @Override public void mouseReleased(MouseEvent e) { showDoanTauQuickActions(e, row); }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int w = getWidth();
                int h = getHeight();
                
                // Shadow
                g2.setColor(new Color(0, 0, 0, isHovered ? 12 : 6));
                g2.fillRoundRect(3, 4, w - 6, h - 5, 16, 16);
                
                // Card Background
                g2.setColor(CARD_BG);
                g2.fillRoundRect(2, 0, w - 6, h - 6, 16, 16);
                
                // Accent code border
                g2.setColor(isHovered ? statusAccent : OUTLINE);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(2, 0, w - 6, h - 6, 16, 16);

                RoundRectangle2D.Float clipRect = new RoundRectangle2D.Float(2, 0, w - 6, h - 6, 16, 16);
                g2.clip(clipRect);
                g2.setColor(stripAccent);
                g2.fillRect(2, 0, 6, h);

                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.setPreferredSize(new Dimension(width, 126));
        card.setMaximumSize(new Dimension(width, 126));
        card.setBorder(new EmptyBorder(12, 20, 12, 12));

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JButton btnView = createIconButton(LineIcons.Name.SEARCH, "Xem chi tiết", PRIMARY_LIGHT);
        btnView.addActionListener(e -> openChiTiet(row));
        btnView.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { e.consume(); }
        });
        JButton btnEdit = createIconButton(LineIcons.Name.EDIT, "Chỉnh sửa", PRIMARY_LIGHT);
        btnEdit.addActionListener(e -> openEditModule(row.doanTau));
        btnEdit.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { e.consume(); }
        });

        JLabel codeChip = createCodeChip(row.doanTau.getMaDoanTau());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        actions.setOpaque(false);
        actions.add(btnView);
        actions.add(btnEdit);
        header.add(codeChip, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);
        // Handoff: header card đoàn tàu theo mẫu toa: mã bên trái, action icon bên phải.
        // Nếu thêm action trạng thái, giữ cùng vùng actions thay vì đưa vào body.
        content.add(header, BorderLayout.NORTH);

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);
        JLabel lblName = new JLabel(row.doanTau.getTenDoanTau() != null ? row.doanTau.getTenDoanTau() : "Chưa đặt tên đoàn tàu");
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblName.setForeground(statusAccent);
        lblName.setAlignmentX(Component.LEFT_ALIGNMENT);
        String dauMay = row.doanTau.getDauMay() != null ? row.doanTau.getDauMay().getMaDauMay() : "Chưa gán đầu máy";
        JLabel lblMeta = new JLabel("Đầu máy: " + dauMay);
        lblMeta.setFont(FONT_SMALL);
        lblMeta.setForeground(TEXT_MUTED);
        lblMeta.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(Box.createVerticalStrut(8));
        info.add(lblName);
        info.add(Box.createVerticalStrut(4));
        info.add(lblMeta);
        JLabel lblTotal = new JLabel(row.tongSoToa() + " toa" + (configured ? " đã cấu hình" : " cần cấu hình"));
        lblTotal.setFont(FONT_SMALL);
        lblTotal.setForeground(TEXT_MUTED);
        lblTotal.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(Box.createVerticalStrut(4));
        info.add(lblTotal);
        content.add(info, BorderLayout.CENTER);

        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        chips.setOpaque(false);
        chips.add(buildStatusBadge(row.doanTau.getTrangThai()));
        if (!configured) {
            chips.add(createBadge("Chưa cấu hình toa", NotionTheme.YELLOW, WARNING_FG));
        } else {
            chips.add(buildChip(row.soToaGheCung + " cứng", C_CUNG));
            chips.add(buildChip(row.soToaGheMem + " mềm", C_MEM));
            chips.add(buildChip(row.soToaGiuongNam + " giường", C_GIUONG));
        }
        content.add(chips, BorderLayout.SOUTH);
        // Handoff: footer đoàn tàu đặt trạng thái trước các tag thành phần để người dùng quét nhanh.
        // Rủi ro: nếu thêm nhiều loại toa, cần tăng chiều cao card hoặc rút gọn label tag.

        card.add(content, BorderLayout.CENTER);
        // Handoff: vùng card đoàn tàu chỉ mở khi double-click, tránh mở nhầm khi chọn/scroll.
        // Các nút action trong header vẫn giữ single-click vì là hành động rõ ràng.
        return card;
    }

    private void openChiTiet(DoanTauRow row) {
        LinkedHashMap<String, String> fields = new LinkedHashMap<>();
        fields.put("Mã đoàn tàu", row.doanTau.getMaDoanTau());
        fields.put("Tên đoàn tàu", row.doanTau.getTenDoanTau());
        fields.put("Đầu máy", row.doanTau.getDauMay() != null ? row.doanTau.getDauMay().getMaDauMay() : null);
        fields.put("Tổng số toa", String.valueOf(row.tongSoToa()));
        fields.put("Toa ghế cứng", String.valueOf(row.soToaGheCung));
        fields.put("Toa ghế mềm", String.valueOf(row.soToaGheMem));
        fields.put("Toa giường nằm", String.valueOf(row.soToaGiuongNam));
        fields.put("Trạng thái", normalizeStatus(row.doanTau.getTrangThai()));
        EntityDetailModule detail = new EntityDetailModule("Đoàn tàu", resolveStatusAccent(row.doanTau.getTrangThai()), row.doanTau.getTenDoanTau(), row.doanTau.getMaDoanTau(), fields);
        Window owner = SwingUtilities.getWindowAncestor(this);
        ModuleLauncher.asDialog(detail, owner instanceof JFrame frame ? frame : null, ignored -> {});
        // Handoff: kính lúp đoàn tàu mở detail tổng hợp thành phần toa thay vì vào màn chỉnh sửa.
        // Rủi ro: số lượng toa lấy từ cache filteredData; gọi loadData sau chỉnh sửa để đồng bộ.
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
        p.setBorder(new EmptyBorder(4, 8, 4, 8));

        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
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
