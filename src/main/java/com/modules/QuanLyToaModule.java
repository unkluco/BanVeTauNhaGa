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
    private static final Color PRIMARY       = new Color(13, 110, 253);
    private static final Color PRIMARY_LIGHT = new Color(231, 241, 255);
    private static final Color SURFACE       = new Color(248, 249, 250);
    private static final Color CARD_BG       = Color.WHITE;
    private static final Color TEXT_MAIN     = new Color(33, 37, 41);
    private static final Color TEXT_MUTED    = new Color(108, 117, 125);
    private static final Color OUTLINE       = new Color(222, 226, 230);

    // Màu theo loại ghế
    private static final Color COLOR_CUNG     = new Color(0xFF, 0xE0, 0xB2); // cam nhạt
    private static final Color COLOR_MEM      = new Color(0xB3, 0xE5, 0xFC); // xanh dương nhạt
    private static final Color COLOR_GIUONG   = new Color(0xC8, 0xE6, 0xC9); // xanh lá nhạt

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
    private int currentPage  = 1;
    private int rowsPerPage  = 12; // Adjusted dynamically
    private int gridCols     = 3;
    private boolean isRefreshing = false;

    // ── Widgets ───────────────────────────────────────────────────────────
    private JTextField txtSearch;
    private JComboBox<String> cbLoai;
    private JPanel     cardsPanel;
    private JScrollPane scrollPane;
    private JPanel     paginationPanel;

    public QuanLyToaModule() {
        setLayout(new BorderLayout());
        setBackground(SURFACE);
        setBorder(new EmptyBorder(30, 40, 30, 40));

        add(buildHeader(),    BorderLayout.NORTH);
        add(buildContent(),   BorderLayout.CENTER);

        loadData();
    }

    @Override public String getTitle() { return "Quản lý Toa Tàu"; }
    @Override public JPanel getView()  { return this; }
    @Override public void setOnResult(Consumer<Object> cb) { this.callback = cb; }

    @Override
    public void reset() {
        if (txtSearch != null) txtSearch.setText("");
        if (cbLoai != null) cbLoai.setSelectedIndex(0);
        loadData();
    }

    // ====================================================================
    //  UI BUILD
    // ====================================================================

    private JPanel buildHeader() {
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(true);
        hdr.setBackground(SURFACE);
        hdr.setBorder(new EmptyBorder(0, 0, 24, 0));

        JPanel left = new JPanel();
        left.setOpaque(true);
        left.setBackground(SURFACE);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        titleRow.setOpaque(true);
        titleRow.setBackground(SURFACE);
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel iconLbl = new JLabel();
        ImageIcon ico = loadScaledIcon("bieuTuongToa.png", 32); 
        // Fallback to train ticket icon if bieuTuongToa not explicitly present
        if (ico == null) ico = loadScaledIcon("nutHanhKhach.png", 32);
        if (ico != null) iconLbl.setIcon(ico);
        JLabel lblTitle = new JLabel("Quản lý Toa Tàu");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(TEXT_MAIN);

        titleRow.add(iconLbl);
        titleRow.add(lblTitle);

        JLabel lblDesc = new JLabel("Quản lý hệ thống toa tàu hành khách, sức chứa và thuộc tính ghế đi kèm.");
        lblDesc.setFont(FONT_DESC);
        lblDesc.setForeground(TEXT_MUTED);
        lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        left.add(titleRow);
        left.add(Box.createVerticalStrut(6));
        left.add(lblDesc);

        hdr.add(left, BorderLayout.WEST);
        
        JButton btnAddNew = createPrimaryButton("+ Thêm toa");
        btnAddNew.setPreferredSize(new Dimension(155, 44));
        btnAddNew.addActionListener(e -> openCreateToaDialog());

        JPanel rightBox = new JPanel(new GridBagLayout());
        rightBox.setOpaque(false);
        rightBox.add(btnAddNew);
        hdr.add(rightBox, BorderLayout.EAST);

        return hdr;
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

        content.add(buildFilterBar(), BorderLayout.NORTH);
        
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
                int cardW = 260; // Slightly narrower for wagons
                int cols = Math.max(1, (w - 15) / (cardW + 15));
                int rH = 110 + 15;
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
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setOpaque(false);
        
        // Row 1: Search and Filter fields (full width)
        JPanel searchRow = new JPanel(new BorderLayout());
        searchRow.setOpaque(false);

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
        gbc.insets = new Insets(0, 0, 0, 15);
        gbc.fill = GridBagConstraints.VERTICAL;

        JLabel iconSearch = new JLabel();
        ImageIcon icoSearch = loadScaledIcon("nutTimKiem.png", 22);
        if (icoSearch!=null) iconSearch.setIcon(icoSearch);
        bgPanel.add(iconSearch, gbc);

        txtSearch = new JTextField(20); 
        txtSearch.setOpaque(true);
        txtSearch.setBackground(Color.WHITE);
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm mã toa...");
        
        // Real-time search
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });
        txtSearch.addActionListener(e -> applyFilter());
        
        gbc.weightx = 1.0; // Expand to fill width
        gbc.fill = GridBagConstraints.HORIZONTAL;
        bgPanel.add(txtSearch, gbc);
        
        cbLoai = new JComboBox<>(new String[]{"- Tất cả loại ghế -", "Ghế cứng", "Ghế mềm", "Giường nằm"});
        cbLoai.setPreferredSize(new Dimension(180, 42));
        cbLoai.setFont(FONT_BODY);
        cbLoai.setBackground(Color.WHITE);
        cbLoai.setBorder(BorderFactory.createLineBorder(OUTLINE, 1, true));
        cbLoai.addActionListener(e -> applyFilter());

        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        bgPanel.add(cbLoai, gbc);

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
            txtSearch.setText("");
            cbLoai.setSelectedIndex(0);
            applyFilter();
        });
        
        gbc.insets = new Insets(0, 0, 0, 0);
        bgPanel.add(btnClear, gbc);

        searchRow.add(bgPanel, BorderLayout.CENTER);
        wrapper.add(searchRow);

        // Row 2: Legend below search
        JPanel pLegend = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        pLegend.setOpaque(false);
        pLegend.setBorder(new EmptyBorder(12, 0, 0, 0));
        pLegend.add(legendDot(COLOR_CUNG,   "Ghế cứng"));
        pLegend.add(legendDot(COLOR_MEM,    "Ghế mềm"));
        pLegend.add(legendDot(COLOR_GIUONG, "Giường"));
        wrapper.add(pLegend);

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
        boolean filterLoai = cbLoai.getSelectedIndex() > 0;

        filteredData = new ArrayList<>();
        for (ToaTau t : allData) {
            boolean matchQ = q.isEmpty()
                    || t.getMaToaTau().toLowerCase().contains(q)
                    || (t.getLoaiGhe() != null && t.getLoaiGhe().toString().toLowerCase().contains(q))
                    || normalizeStatus(t.getTrangThai()).toLowerCase().contains(q);
            boolean matchL = !filterLoai || (t.getLoaiGhe() != null && t.getLoaiGhe().toString().equals(loai));
            
            if (matchQ && matchL) filteredData.add(t);
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
            List<ToaTau> pageData = filteredData.subList(start, end);

            int w = scrollPane.getWidth();
            int cardW = 260;
            if (gridCols > 0) {
                cardW = (w - (gridCols + 1) * 15) / gridCols;
                cardW = Math.max(cardW, 230);
            }

            cardsPanel.removeAll();
            cardsPanel.setPreferredSize(new Dimension(w - 20, 0));
            if (pageData.isEmpty()) {
                cardsPanel.add(buildEmptyState());
            } else {
                for (ToaTau t : pageData) {
                    cardsPanel.add(buildToaCard(t, cardW));
                }
            }
            cardsPanel.revalidate();
            cardsPanel.repaint();

            rebuildPagination(totalPages, total);
        } finally {
            isRefreshing = false;
        }
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
                JOptionPane.showMessageDialog(this,
                        "Không thể tạo toa mới. Vui lòng kiểm tra dữ liệu đầu vào.",
                        "Tạo toa thất bại",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        dlg.setVisible(true);
    }

    private void openChangeToaStatusDialog(ToaTau toa) {
        if (toa == null || toa.getMaToaTau() == null || toa.getMaToaTau().isBlank()) return;

        JComboBox<String> cbo = new JComboBox<>(STATUS_OPTIONS);
        cbo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbo.setSelectedItem(normalizeStatus(toa.getTrangThai()));

        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.add(new JLabel("Cập nhật trạng thái cho " + toa.getMaToaTau() + ":"), BorderLayout.NORTH);
        panel.add(cbo, BorderLayout.CENTER);

        int confirmed = JOptionPane.showConfirmDialog(
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
            JOptionPane.showMessageDialog(this,
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
        
        final Color finalAccent = scaleBrightness(accentColor, 0.85f); // slightly darker for text/accent

        JPanel card = new JPanel(new BorderLayout()) {
            boolean isHovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { isHovered = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { isHovered = false; repaint(); }
                    @Override public void mouseClicked(MouseEvent e) { openChiTiet(toa); }
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
                g2.setColor(finalAccent);
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
        
        JLabel lblCode = new JLabel(toa.getMaToaTau());
        lblCode.setFont(new Font("Consolas", Font.BOLD, 16));
        lblCode.setForeground(TEXT_MAIN);

        JButton btnView = createCardActionButton("nutTimKiem.png", "Xem chi tiết", PRIMARY_LIGHT);
        btnView.addActionListener(e -> openChiTiet(toa));
        JButton btnStatus = createCardActionButton("nutSua.png", "Đổi trạng thái", PRIMARY_LIGHT);
        btnStatus.addActionListener(e -> openChangeToaStatusDialog(toa));

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

        JLabel lblStatus = new JLabel(normalizeStatus(toa.getTrangThai())) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(resolveStatusBg(getText()));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblStatus.setForeground(resolveStatusFg(lblStatus.getText()));
        lblStatus.setBorder(new EmptyBorder(4, 10, 4, 10));

        body.add(Box.createVerticalStrut(8));
        body.add(lblType);
        body.add(Box.createVerticalStrut(4));
        body.add(lblCount);
        body.add(Box.createVerticalStrut(6));
        body.add(lblStatus);

        content.add(body, BorderLayout.CENTER);
        card.add(content, BorderLayout.CENTER);

        return card;
    }

    private static Color scaleBrightness(Color c, float f) {
        return new Color(
                Math.min(255, Math.max(0, (int)(c.getRed()   * f))),
                Math.min(255, Math.max(0, (int)(c.getGreen() * f))),
                Math.min(255, Math.max(0, (int)(c.getBlue()  * f))));
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
        if (normalized.contains("bảo trì")) return new Color(0xFE, 0xF3, 0xC7);
        if (normalized.contains("ngừng")) return new Color(0xFE, 0xE2, 0xE2);
        return new Color(0xDC, 0xFA, 0xE6);
    }

    private Color resolveStatusFg(String status) {
        String normalized = status == null ? "" : status.trim().toLowerCase();
        if (normalized.contains("bảo trì")) return new Color(0x92, 0x60, 0x10);
        if (normalized.contains("ngừng")) return new Color(0xB9, 0x1C, 0x1C);
        return new Color(0x16, 0x6B, 0x3A);
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
            JLabel info = new JLabel("Hiển thị trang " + currentPage + " / " + totalPages + " (" + total + " toa)");
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

    private JButton createCardActionButton(String iconFile, String toolTip, Color hoverBg) {
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
        ImageIcon icon = loadScaledIcon(iconFile, 16);
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
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
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
