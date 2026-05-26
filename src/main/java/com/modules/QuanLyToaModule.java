package com.modules;

import com.dao.DAO_Ghe;
import com.dao.DAO_ToaTau;
import com.entity.ToaTau;
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
import java.util.List;
import java.util.function.Consumer;

public class QuanLyToaModule extends JPanel implements AppModule {
    private static final String[] STATUS_OPTIONS = {
            "Đang hoạt động", "Đang bảo trì", "Ngừng hoạt động"
    };

    // ── Design tokens ────────────────────────────────────────────────────
    private static final Color PRIMARY       = NotionTheme.ACCENT;
    private static final Color PRIMARY_LIGHT = NotionTheme.ACCENT_SOFT;
    private static final Color SURFACE       = NotionTheme.PAGE;
    private static final Color CARD_BG       = NotionTheme.CARD;
    private static final Color TEXT_MAIN     = NotionTheme.TEXT;
    private static final Color TEXT_MUTED    = NotionTheme.TEXT_MUTED;
    private static final Color OUTLINE       = NotionTheme.BORDER;
    private static final Color ACTIVE_GREEN  = new Color(0x1A, 0xAE, 0x39);

    // Màu theo loại ghế
    private static final Color COLOR_CUNG     = new Color(0xDD, 0x5B, 0x00);
    private static final Color COLOR_MEM      = new Color(0x00, 0x75, 0xDE);
    private static final Color COLOR_GIUONG   = ACTIVE_GREEN;

    private static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD, 26);
    private static final Font FONT_DESC   = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_BOLD   = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_BODY   = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_STA    = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font FONT_SMALL  = new Font("Segoe UI", Font.PLAIN, 12);

    // ── DAOs ─────────────────────────────────────────────────────────────
    private final DAO_ToaTau daoToa = new DAO_ToaTau();
    private final DAO_Ghe    daoGhe = new DAO_Ghe();

    // ── State ─────────────────────────────────────────────────────────────
    private Consumer<Object> callback;
    private List<ToaTau> allData      = new ArrayList<>();
    private List<ToaTau> filteredData = new ArrayList<>();
    private int gridCols     = 3;

    // ── Widgets ───────────────────────────────────────────────────────────
    private JTextField txtSearch;
    private JComboBox<String> cbLoai;
    private JComboBox<String> cbStatus;
    private JLabel     lblStatTotal;
    private JLabel     lblStatSeat;
    private JLabel     lblStatBed;
    private JPanel     cardsPanel;
    private JPanel     cardsHost;
    private JScrollPane scrollPane;

    public QuanLyToaModule() {
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
        // Handoff: filter card dùng preferred height thay vì số pixel cứng để tránh che control.
        // Cảnh báo: các kích thước nút/tag/card con vẫn cố định để giữ layout dạng lưới ổn định.
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

    @Override public String getTitle() { return "Quản lý Toa Tàu"; }
    @Override public JPanel getView()  { return this; }
    @Override public void setOnResult(Consumer<Object> cb) { this.callback = cb; }

    @Override
    public void reset() {
        if (txtSearch != null) txtSearch.setText("");
        if (cbLoai != null) cbLoai.setSelectedIndex(0);
        if (cbStatus != null) cbStatus.setSelectedIndex(0);
        loadData();
    }

    // ====================================================================
    //  UI BUILD
    // ====================================================================

    private JPanel buildHeader() {
        // Header đổi sang hero card Notion để đồng bộ nhóm Dữ liệu tàu, giữ nguyên grid toa bên dưới.
        // Rủi ro: nút thêm nằm trong vùng gradient nên cần giữ foreground trắng qua NotionTheme.
        JPanel hdr = new JPanel(new BorderLayout(24, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, NotionTheme.NAVY,
                        getWidth(), getHeight(), new Color(0xE0, 0x31, 0x31));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                g2.setColor(AppColors.withAlpha(NotionTheme.ROSE, 150));
                g2.fillOval(getWidth() - 215, -76, 230, 230);
                g2.setColor(AppColors.withAlpha(NotionTheme.MINT, 120));
                g2.fillRoundRect(getWidth() - 350, 96, 190, 46, 24, 24);
                g2.setColor(AppColors.withAlpha(Color.WHITE, 42));
                g2.fillRoundRect(getWidth() - 282, 38, 108, 16, 16, 16);
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

        JLabel lblTitle = new JLabel("Quản lý toa tàu");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblDesc = new JLabel("Quản lý hệ thống toa tàu hành khách, sức chứa và thuộc tính ghế đi kèm.");
        lblDesc.setFont(FONT_DESC);
        lblDesc.setForeground(AppColors.withAlpha(Color.WHITE, 205));
        lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        left.add(lblEyebrow);
        left.add(Box.createVerticalStrut(8));
        left.add(lblTitle);
        left.add(Box.createVerticalStrut(8));
        left.add(lblDesc);

        JButton btnAddNew = createPrimaryButton("+ Thêm toa");
        btnAddNew.setPreferredSize(new Dimension(155, 42));
        btnAddNew.addActionListener(e -> openCreateToaDialog());

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
        lblStatSeat = new JLabel("0");
        lblStatBed = new JLabel("0");
        row.add(buildStatCard("Toa hiện tại", lblStatTotal, PRIMARY, NotionTheme.ACCENT_SOFT));
        row.add(buildStatCard("Toa ghế", lblStatSeat, new Color(0x00, 0x75, 0xDE), NotionTheme.SKY));
        row.add(buildStatCard("Toa giường", lblStatBed, AppColors.ERROR_DARK, NotionTheme.ROSE));
        return row;
    }

    private JPanel buildStatCard(String label, JLabel value, Color accent, Color tint) {
        // Stat card copy pattern Khách hàng để các tab dữ liệu tàu có phần top đồng bộ.
        // Rủi ro: thống kê theo filteredData nên phải cập nhật trong applyFilter sau mọi thay đổi filter.
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

    private JPanel legendDot(Color c, String text) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);
        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(c.darker());
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
            }
        };
        dot.setOpaque(false);
        dot.setPreferredSize(new Dimension(16, 16));
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_SMALL);
        lbl.setForeground(TEXT_MAIN);
        p.add(dot);
        p.add(lbl);
        return p;
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
        // Filter card tách search/filter và legend để giữ layout rõ, chưa đổi phần grid toa bên dưới.
        // Rủi ro: cbLoai vẫn là JComboBox native nên cần kiểm tra chiều cao khi đổi LookAndFeel.
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
        JLabel title = new JLabel("Bộ lọc toa tàu");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(TEXT_MAIN);
        JLabel subtitle = new JLabel("Tìm mã toa và lọc theo loại ghế");
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
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm mã toa...");
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
            cbLoai.setSelectedIndex(0);
            cbStatus.setSelectedIndex(0);
            applyFilter();
        });

        searchRow.add(searchBox, BorderLayout.CENTER);

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.add(searchRow, BorderLayout.NORTH);

        cbLoai = new JComboBox<>(new String[]{"- Tất cả loại ghế -", "Ghế cứng", "Ghế mềm", "Giường nằm"});
        cbLoai.setPreferredSize(new Dimension(180, 38));
        cbLoai.setFont(FONT_BODY);
        cbLoai.setBackground(CARD_BG);
        cbLoai.setBorder(BorderFactory.createLineBorder(OUTLINE, 1, true));
        NotionTheme.applyComboBoxSelection(cbLoai);
        cbLoai.addActionListener(e -> applyFilter());

        cbStatus = new JComboBox<>(new String[]{"Tất cả trạng thái", STATUS_OPTIONS[0], STATUS_OPTIONS[1], STATUS_OPTIONS[2]});
        cbStatus.setPreferredSize(new Dimension(190, 38));
        cbStatus.setFont(FONT_BODY);
        cbStatus.setBackground(CARD_BG);
        cbStatus.setBorder(BorderFactory.createLineBorder(OUTLINE, 1, true));
        NotionTheme.applyComboBoxSelection(cbStatus);
        cbStatus.addActionListener(e -> applyFilter());

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
        optionRow.add(createFilterGroup("Loại ghế", cbLoai), filterGbc);
        filterGbc.gridx = 1;
        optionRow.add(createFilterGroup("Trạng thái", cbStatus), filterGbc);
        filterGbc.gridx = 2;
        filterGbc.weightx = 0.0;
        filterGbc.insets = new Insets(0, 0, 0, 0);
        optionRow.add(FilterActionGroup.wrap(btnClear), filterGbc);
        // Handoff: filter row dùng grid để loại ghế/trạng thái/nút không lệch.
        // Cảnh báo: legend phía dưới giữ layout riêng, không trộn vào filter grid.

        JPanel lower = new JPanel(new BorderLayout(0, 8));
        lower.setOpaque(false);
        lower.add(optionRow, BorderLayout.NORTH);

        JPanel pLegend = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        pLegend.setOpaque(false);
        pLegend.setAlignmentX(Component.LEFT_ALIGNMENT);
        pLegend.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        pLegend.add(legendDot(COLOR_CUNG,   "Ghế cứng"));
        pLegend.add(legendDot(COLOR_MEM,    "Ghế mềm"));
        pLegend.add(legendDot(COLOR_GIUONG, "Giường"));
        lower.add(pLegend, BorderLayout.CENTER);
        body.add(lower, BorderLayout.CENTER);
        wrapper.add(body, BorderLayout.CENTER);
        return wrapper;
    }

    // ====================================================================
    //  DATA OPERATIONS
    // ====================================================================

    private void loadData() {
        allData = daoToa.getAll();
        applyFilter();
    }

    private int countGhe(String maToaTau) {
        return daoGhe.findByToaTau(maToaTau).size();
    }

    private void applyFilter() {
        String q = txtSearch.getText().trim().toLowerCase();
        String loai = (String) cbLoai.getSelectedItem();
        String status = cbStatus == null ? "Tất cả trạng thái" : (String) cbStatus.getSelectedItem();
        boolean filterLoai = cbLoai.getSelectedIndex() > 0;
        boolean filterStatus = status != null && !"Tất cả trạng thái".equals(status);

        filteredData = new ArrayList<>();
        for (ToaTau t : allData) {
            boolean matchQ = q.isEmpty()
                    || t.getMaToaTau().toLowerCase().contains(q)
                    || (t.getLoaiGhe() != null && t.getLoaiGhe().toString().toLowerCase().contains(q))
                    || normalizeStatus(t.getTrangThai()).toLowerCase().contains(q);
            boolean matchL = !filterLoai || (t.getLoaiGhe() != null && t.getLoaiGhe().toString().equals(loai));
            boolean matchStatus = !filterStatus || status.equals(normalizeStatus(t.getTrangThai()));

            if (matchQ && matchL && matchStatus) filteredData.add(t);
        }

        if (lblStatTotal != null) {
            lblStatTotal.setText(String.valueOf(filteredData.size()));
            lblStatSeat.setText(String.valueOf(filteredData.stream().filter(t -> t.getLoaiGhe() == LoaiGhe.GHE_CUNG || t.getLoaiGhe() == LoaiGhe.GHE_MEM).count()));
            lblStatBed.setText(String.valueOf(filteredData.stream().filter(t -> t.getLoaiGhe() == LoaiGhe.GIUONG_NAM).count()));
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
            for (ToaTau t : filteredData) {
                cardsPanel.add(buildToaCard(t, 300));
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
        int width = Math.max(1, scrollPane.getViewport().getWidth());
        int nextCols = Math.max(1, width / 330);
        if (nextCols != gridCols || !(cardsPanel.getLayout() instanceof GridLayout)) {
            gridCols = nextCols;
            cardsPanel.setLayout(new GridLayout(0, gridCols, 15, 15));
            cardsPanel.revalidate();
        }
        // Handoff: card toa là mẫu grid chuẩn cho nhóm đầu máy/toa/đoàn tàu.
        // Rủi ro: nếu thay đổi card width/spacing, giữ divisor đồng bộ với hai module còn lại.
    }



    private void openChiTiet(ToaTau toa) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JFrame frame = (owner instanceof JFrame) ? (JFrame) owner : null;
        ChiTietToaDialog dlg = new ChiTietToaDialog(frame, toa);
        dlg.setVisible(true);
    }

    private void openCreateToaDialog() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        String maToaTau = daoToa.generateNextMaToaTau();

        ThemToaDialog dlg = new ThemToaDialog(owner, maToaTau, created -> {
            boolean ok = daoToa.insertWithAutoSeats(created);
            if (ok) {
                loadData();
            } else {
                NotionMessageDialog.showMessageDialog(this,
                        "Không thể tạo toa mới. Vui lòng kiểm tra dữ liệu đầu vào.",
                        "Tạo toa thất bại",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        dlg.setVisible(true);
    }

    private void showToaQuickActions(MouseEvent e, ToaTau toa) {
        if (!e.isPopupTrigger()) return;
        JPopupMenu menu = new JPopupMenu();
        JMenuItem view = new JMenuItem("Xem chi tiết");
        view.addActionListener(ev -> openChiTiet(toa));
        JMenuItem item = new JMenuItem("Đổi trạng thái hoạt động");
        item.addActionListener(ev -> openChangeToaStatusDialog(toa));
        menu.add(view);
        menu.addSeparator();
        menu.add(item);
        menu.show(e.getComponent(), e.getX(), e.getY());
    }

    private void openChangeToaStatusDialog(ToaTau toa) {
        if (toa == null || toa.getMaToaTau() == null || toa.getMaToaTau().isBlank()) return;

        JComboBox<String> cbo = new JComboBox<>(STATUS_OPTIONS);
        cbo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        NotionTheme.applyComboBoxSelection(cbo);
        cbo.setSelectedItem(normalizeStatus(toa.getTrangThai()));

        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.add(new JLabel("Cập nhật trạng thái cho " + toa.getMaToaTau() + ":"), BorderLayout.NORTH);
        panel.add(cbo, BorderLayout.CENTER);

        int confirmed = NotionMessageDialog.showConfirmDialog(
                this,
                panel,
                "Đổi trạng thái toa",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (confirmed != JOptionPane.OK_OPTION) return;

        String newStatus = (String) cbo.getSelectedItem();
        if (newStatus == null || normalizeStatus(toa.getTrangThai()).equals(newStatus)) return;

        boolean ok = daoToa.updateTrangThai(toa.getMaToaTau(), newStatus);
        if (ok) {
            loadData();
        } else {
            NotionMessageDialog.showMessageDialog(this,
                    "Không thể cập nhật trạng thái toa.",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ====================================================================
    //  CARD BUILDER
    // ====================================================================

    private JPanel buildToaCard(ToaTau toa, int width) {
        LoaiGhe lg = toa.getLoaiGhe();
        Color accentColor = PRIMARY;
        String typeLabel = "KHÔNG XÁC ĐỊNH";
        
        if (lg != null) {
            accentColor = switch (lg) {
                case GHE_CUNG   -> COLOR_CUNG;
                case GHE_MEM    -> COLOR_MEM;
                case GIUONG_NAM -> COLOR_GIUONG;
            };
            typeLabel = switch (lg) {
                case GHE_CUNG   -> "GHẾ CỨNG";
                case GHE_MEM    -> "GHẾ MỀM";
                case GIUONG_NAM -> "GIƯỜNG NẰM";
            };
        }
        
        final Color finalAccent = accentColor;
        final Color stripAccent = AppColors.withAlpha(finalAccent, 170);

        JPanel card = new JPanel(new BorderLayout()) {
            boolean isHovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { isHovered = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { isHovered = false; repaint(); }
                    @Override public void mouseClicked(MouseEvent e) {
                        if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) openChiTiet(toa);
                    }
                    @Override public void mousePressed(MouseEvent e) { showToaQuickActions(e, toa); }
                    @Override public void mouseReleased(MouseEvent e) { showToaQuickActions(e, toa); }
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
                
                // Background
                g2.setColor(CARD_BG);
                g2.fillRoundRect(2, 0, w - 6, h - 6, 16, 16);
                
                // Border
                g2.setColor(isHovered ? finalAccent : OUTLINE);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(2, 0, w - 6, h - 6, 16, 16);
                
                // Left accent
                java.awt.geom.RoundRectangle2D.Float clipRect = new java.awt.geom.RoundRectangle2D.Float(2, 0, w - 6, h - 6, 16, 16);
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

        // Header (Code & Action)
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        
        JLabel lblCode = createCodeChip(toa.getMaToaTau());

        JButton btnView = createCardActionButton(LineIcons.Name.SEARCH, "Xem chi tiết", PRIMARY_LIGHT);
        btnView.addActionListener(e -> openChiTiet(toa));
        btnView.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { e.consume(); }
        });
        JButton btnStatus = createCardActionButton(LineIcons.Name.EDIT, "Đổi trạng thái", PRIMARY_LIGHT);
        btnStatus.addActionListener(e -> openChangeToaStatusDialog(toa));
        btnStatus.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { e.consume(); }
        });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        actions.setOpaque(false);
        actions.add(btnView);
        actions.add(btnStatus);

        top.add(lblCode, BorderLayout.WEST);
        top.add(actions, BorderLayout.EAST);

        content.add(top, BorderLayout.NORTH);

        // Body (Type & Count)
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        int soGhe = countGhe(toa.getMaToaTau());

        JLabel lblType = new JLabel(typeLabel);
        lblType.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblType.setForeground(finalAccent);

        JLabel lblCount = new JLabel(soGhe + " vị trí ghế");
        lblCount.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblCount.setForeground(TEXT_MUTED);

        body.add(Box.createVerticalStrut(8));
        body.add(lblType);
        body.add(Box.createVerticalStrut(4));
        body.add(lblCount);
        content.add(body, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        footer.setOpaque(false);
        footer.add(createStatusChip(normalizeStatus(toa.getTrangThai())));
        content.add(footer, BorderLayout.SOUTH);
        // Handoff: footer card toa giữ các tag riêng, thống nhất layout với đầu máy/đoàn tàu.
        // Rủi ro: count ghế vẫn lấy theo DAO hiện tại; nếu danh sách lớn nên cache theo mã toa.
        card.add(content, BorderLayout.CENTER);
        // Handoff: vùng card toa chỉ mở chi tiết khi double-click, đồng bộ với các bảng quản lý tàu.
        // Nút kính lúp/đổi trạng thái vẫn single-click để không làm chậm thao tác chủ đích.

        return card;
    }

    private JLabel createCodeChip(String text) {
        // Chip mã toa dùng nền neutral để nhường màu trạng thái/category cho strip và badge.
        // Rủi ro: mã quá dài vẫn theo preferred size label, cần kiểm tra nếu đổi format mã.
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

    private JLabel createStatusChip(String status) {
        JLabel chip = new JLabel(status) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(resolveStatusBg(getText()));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        chip.setFont(new Font("Segoe UI", Font.BOLD, 11));
        chip.setForeground(resolveStatusFg(status));
        chip.setBorder(new EmptyBorder(4, 10, 4, 10));
        return chip;
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
        if (normalized.contains("bảo trì")) return NotionTheme.YELLOW;
        if (normalized.contains("ngừng")) return NotionTheme.ROSE;
        return NotionTheme.MINT;
    }

    private Color resolveStatusFg(String status) {
        String normalized = status == null ? "" : status.trim().toLowerCase();
        if (normalized.contains("bảo trì")) return AppColors.WARNING_DARK;
        if (normalized.contains("ngừng")) return AppColors.ERROR_DARK;
        return ACTIVE_GREEN;
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

    private JButton createCardActionButton(LineIcons.Name iconName, String toolTip, Color hoverBg) {
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
        ImageIcon icon = loadScaledIcon(iconName, 16);
        if (icon != null) btn.setIcon(icon);
        btn.setPreferredSize(new Dimension(28, 28));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setToolTipText(toolTip);
        return btn;
    }

    private JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) g2.setColor(PRIMARY.darker());
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

    private JLabel createFilterLabel(String text) {
        JLabel lbl = new JLabel(text + ":");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(TEXT_MUTED);
        return lbl;
    }

    private JPanel createFilterGroup(String label, JComponent input) {
        JPanel group = new JPanel(new BorderLayout(0, 6));
        group.setOpaque(false);
        group.add(createFilterLabel(label), BorderLayout.NORTH);
        group.add(input, BorderLayout.CENTER);
        return group;
        // Handoff: label filter đặt trên control để đồng bộ nhóm nghiệp vụ.
        // Cảnh báo: giữ chiều cao control 38/40 để nút Bỏ lọc cùng baseline.
    }

    private JButton createSecondaryButton(String text, int width, int height) {
        // Nút phụ trong filter card dùng nền trắng và border mảnh để giữ phân cấp thị giác với primary button.
        // Rủi ro: helper đang tối ưu cho nhãn ngắn; nếu thêm icon/text dài cần tăng width.
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


