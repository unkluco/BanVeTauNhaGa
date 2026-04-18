package com.modules;

import com.connectDB.ConnectDB;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
    private static final Color PRIMARY       = new Color(13, 110, 253);
    private static final Color PRIMARY_HOVER = new Color(11, 94, 215);
    private static final Color PRIMARY_LIGHT = new Color(231, 241, 255);
    private static final Color SURFACE       = new Color(248, 249, 250);
    private static final Color CARD_BG       = Color.WHITE;
    private static final Color TEXT_MAIN     = new Color(33, 37, 41);
    private static final Color TEXT_MUTED    = new Color(108, 117, 125);
    private static final Color OUTLINE       = new Color(222, 226, 230);
    private static final Color ERROR_FG      = new Color(220, 53, 69); // Danger

    // Badge Colors
    private static final Color C_CUNG   = new Color(0xFF, 0x8A, 0x65); 
    private static final Color C_MEM    = new Color(0x64, 0xB5, 0xF6); 
    private static final Color C_GIUONG = new Color(0x81, 0xC7, 0x84); 

    private static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD, 26);
    private static final Font FONT_DESC   = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_BODY   = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_BOLD   = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_SMALL  = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_BTN    = new Font("Segoe UI", Font.BOLD, 14);

    // --- Widgets ---
    private JTextField txtSearch;
    private JCheckBox  cbGheCung, cbGheMem, cbGiuongNam;
    private JPanel     cardsPanel;
    private JScrollPane scrollPane;
    private JPanel     paginationPanel;

    private int currentPage  = 1;
    private int rowsPerPage  = 8; // Auto calculates based on grid
    private int gridCols     = 2;
    private boolean isRefreshing = false;

    public QuanLyDoanTauModule() {
        setLayout(new BorderLayout());
        setBackground(SURFACE);
        setBorder(new EmptyBorder(30, 40, 30, 40));

        add(buildHeader(),    BorderLayout.NORTH);
        add(buildContent(),   BorderLayout.CENTER);

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
        ImageIcon ico = loadScaledIcon("bieuTuongTau.png", 32);
        if (ico != null) iconLbl.setIcon(ico);
        JLabel lblTitle = new JLabel("Danh Mục Đội Tàu");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(TEXT_MAIN);

        titleRow.add(iconLbl);
        titleRow.add(lblTitle);

        JLabel lblDesc = new JLabel("Quản lý cấu hình chi tiết và thành phần tổ hợp toa của toàn bộ đội tàu.");
        lblDesc.setFont(FONT_DESC);
        lblDesc.setForeground(TEXT_MUTED);
        lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        left.add(titleRow);
        left.add(Box.createVerticalStrut(6));
        left.add(lblDesc);

        hdr.add(left, BorderLayout.WEST);

        // Nút thêm mới
        JButton btnAddNew = new JButton("+ Thiết lập Đội Tàu") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) g2.setColor(PRIMARY_HOVER);
                else if (getModel().isRollover()) g2.setColor(PRIMARY.brighter());
                else g2.setColor(PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnAddNew.setFont(FONT_BTN);
        btnAddNew.setForeground(Color.WHITE);
        btnAddNew.setContentAreaFilled(false);
        btnAddNew.setBorderPainted(false);
        btnAddNew.setFocusPainted(false);
        btnAddNew.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAddNew.setPreferredSize(new Dimension(190, 44));
        btnAddNew.addActionListener(e -> openNewModule());

        JPanel rightBox = new JPanel(new GridBagLayout());
        rightBox.setOpaque(false);
        rightBox.add(btnAddNew);

        hdr.add(rightBox, BorderLayout.EAST);

        return hdr;
    }

    private JPanel buildContent() {
        JPanel content = new JPanel(new BorderLayout(0, 20));
        content.setOpaque(true);
        content.setBackground(SURFACE);

        content.add(buildFilterBar(), BorderLayout.NORTH);
        
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
                int rH = 150 + 20;
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
        
        // Row 1: Search row (full width)
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
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm tên đội tàu...");
        
        // Real-time search
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });
        txtSearch.addActionListener(e -> applyFilter());
        
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        bgPanel.add(txtSearch, gbc);

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
            cbGheCung.setSelected(true);
            cbGheMem.setSelected(true);
            cbGiuongNam.setSelected(true);
            applyFilter();
        });
        
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 0, 0, 0);
        bgPanel.add(btnClear, gbc);

        searchRow.add(bgPanel, BorderLayout.CENTER);
        wrapper.add(searchRow);

        // Row 2: Checkboxes below search
        JPanel pFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        pFilters.setOpaque(false);
        pFilters.setBorder(new EmptyBorder(12, 0, 0, 0));

        cbGheCung   = new JCheckBox("Ghế cứng", true);
        cbGheMem    = new JCheckBox("Ghế mềm", true);
        cbGiuongNam = new JCheckBox("Giường nằm", true);

        for (JCheckBox cb : new JCheckBox[]{cbGheCung, cbGheMem, cbGiuongNam}) {
            cb.setFont(FONT_BODY);
            cb.setOpaque(false);
            cb.setFocusPainted(false);
            cb.addActionListener(e -> applyFilter());
            pFilters.add(cb);
        }
        wrapper.add(pFilters);

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
        int confirm = JOptionPane.showConfirmDialog(this,
                "Xóa đoàn tàu “" + row.doanTau.getTenDoanTau() + "”?\n"
                + "Tất cả chi tiết thành phần toa sẽ bị hủy bỏ.",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        Connection con = ConnectDB.getCon();
        if (con == null) {
            JOptionPane.showMessageDialog(this, "Lỗi kết nối", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM ChiTietDoanTau WHERE maDoanTau = ?")) {
                ps.setString(1, row.doanTau.getMaDoanTau());
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM DoanTau WHERE maDoanTau = ?")) {
                ps.setString(1, row.doanTau.getMaDoanTau());
                ps.executeUpdate();
            }
            loadData();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi xóa: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =====================================================================
    //  Card Builder
    // =====================================================================

    private JPanel buildDoanTauCard(DoanTauRow row, int width) {
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
                g2.setColor(new Color(0, 0, 0, isHovered ? 14 : 7));
                g2.fillRoundRect(3, 4, w - 6, h - 5, 20, 20);
                
                // Card Background
                g2.setColor(CARD_BG);
                g2.fillRoundRect(2, 0, w - 6, h - 6, 20, 20);
                
                // Accent code border
                g2.setColor(isHovered ? PRIMARY : OUTLINE);
                g2.setStroke(new BasicStroke(isHovered ? 1.5f : 1f));
                g2.drawRoundRect(2, 0, w - 6, h - 6, 20, 20);
                
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(width, 160));
        card.setMaximumSize(new Dimension(width, 160));
        card.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        // --- ROW 1: MA DOAN TAU + DAU MAY & ACTIONS ---
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel lblCodeWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        lblCodeWrap.setOpaque(false);

        JLabel lblCode = new JLabel(row.doanTau.getMaDoanTau()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(PRIMARY_LIGHT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblCode.setFont(new Font("Consolas", Font.BOLD, 14));
        lblCode.setForeground(PRIMARY.darker());
        lblCode.setBorder(new EmptyBorder(4, 8, 4, 8));
        lblCodeWrap.add(lblCode);
        
        // Looping thru DauMay representation
        if (row.doanTau.getDauMay() != null) {
            lblCodeWrap.add(Box.createHorizontalStrut(8));
            JLabel lblDau = new JLabel(row.doanTau.getDauMay().getMaDauMay());
            lblDau.setFont(FONT_SMALL);
            lblDau.setForeground(TEXT_MUTED);
            ImageIcon icoDau = loadScaledIcon("bieuTuongTau.png", 14);
            if (icoDau != null) lblDau.setIcon(icoDau);
            lblCodeWrap.add(lblDau);
        }

        top.add(lblCodeWrap, BorderLayout.WEST);

        // Edit/Delete actions
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.setOpaque(false);
        
        JButton btnEdit = createIconButton("nutSua.png", "Sửa", PRIMARY_LIGHT);
        btnEdit.addActionListener(e -> openEditModule(row.doanTau));
        JButton btnDel = createIconButton("nuXoa.png", "Xóa", new Color(254, 226, 226)); // if nuXoa.png is missing, fallback icon
        btnDel.addActionListener(e -> deleteRow(row));

        actions.add(btnEdit);
        actions.add(btnDel);
        top.add(actions, BorderLayout.EAST);

        content.add(top);
        content.add(Box.createVerticalStrut(10));

        // --- ROW 2: TÊN ĐOÀN TÀU ---
        JLabel lblName = new JLabel(row.doanTau.getTenDoanTau() != null ? "<html>" + row.doanTau.getTenDoanTau() + "</html>" : "");
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblName.setForeground(TEXT_MAIN);
        lblName.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(lblName);
        content.add(Box.createVerticalStrut(14));

        // --- ROW 3: CHIPS SỐ TOA ---
        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        chips.setOpaque(false);
        chips.setAlignmentX(Component.LEFT_ALIGNMENT);
        int t = row.tongSoToa();
        
        if (t == 0) {
            JLabel empty = new JLabel("Chưa cấu hình toa");
            empty.setFont(FONT_SMALL);
            empty.setForeground(TEXT_MUTED);
            chips.add(empty);
        } else {
            if (row.soToaGheCung > 0)   chips.add(buildChip(row.soToaGheCung + " Cứng", C_CUNG));
            if (row.soToaGheMem > 0)    chips.add(buildChip(row.soToaGheMem + " Mềm", C_MEM));
            if (row.soToaGiuongNam > 0) chips.add(buildChip(row.soToaGiuongNam + " Nằm", C_GIUONG));
            chips.add(Box.createHorizontalStrut(8));
            JLabel lblTotal = new JLabel("= " + t + " toa");
            lblTotal.setFont(FONT_BOLD);
            lblTotal.setForeground(TEXT_MAIN);
            chips.add(lblTotal);
        }
        
        // Wrap align left
        JPanel chipWrap = new JPanel(new BorderLayout());
        chipWrap.setOpaque(false);
        chipWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        chipWrap.add(chips, BorderLayout.WEST);
        
        content.add(chipWrap);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildChip(String text, Color dotColor) {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(SURFACE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(OUTLINE);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(4, 10, 4, 10));
        
        JPanel pDot = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5));
        pDot.setOpaque(false);
        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(dotColor);
                g2.fillOval(0, 0, 8, 8);
                g2.dispose();
            }
        };
        dot.setOpaque(false);
        dot.setPreferredSize(new Dimension(8, 8));
        pDot.add(dot);
        
        JLabel lbl = new JLabel(" " + text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXT_MAIN);

        p.add(pDot, BorderLayout.WEST);
        p.add(lbl, BorderLayout.CENTER);
        return p;
    }

    private JButton createIconButton(String iconName, String tip, Color hoverBg) {
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
        if (ico == null && iconName.contains("Xoa")) { 
            // Fallback since nuXoa might be nutXoa
            ico = loadScaledIcon("nutXoa.png", 16); 
        }
        if (ico != null) btn.setIcon(ico);
        else btn.setText(tip.substring(0, 1));
        
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
