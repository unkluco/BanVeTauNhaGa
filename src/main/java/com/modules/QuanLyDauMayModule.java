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

    // ── Design tokens ────────────────────────────────────────────────────
    private static final Color PRIMARY       = new Color(13, 110, 253);
    private static final Color PRIMARY_HOVER = new Color(11, 94, 215);
    private static final Color PRIMARY_LIGHT = new Color(231, 241, 255);
    private static final Color SURFACE       = new Color(248, 249, 250);
    private static final Color CARD_BG       = Color.WHITE;
    private static final Color TEXT_MAIN     = new Color(33, 37, 41);
    private static final Color TEXT_MUTED    = new Color(108, 117, 125);
    private static final Color OUTLINE       = new Color(222, 226, 230);

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
    private JPanel     cardsPanel;
    private JScrollPane scrollPane;
    private JPanel     paginationPanel;

    public QuanLyDauMayModule() {
        setLayout(new BorderLayout());
        setBackground(SURFACE);
        setBorder(new EmptyBorder(30, 40, 30, 40));

        add(buildHeader(),    BorderLayout.NORTH);
        add(buildContent(),   BorderLayout.CENTER);

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
        JLabel lblTitle = new JLabel("Quản lý Đầu máy");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(TEXT_MAIN);

        titleRow.add(iconLbl);
        titleRow.add(lblTitle);

        JLabel lblDesc = new JLabel("Duyệt và kiểm soát các đầu máy kéo tàu hệ thống.");
        lblDesc.setFont(FONT_DESC);
        lblDesc.setForeground(TEXT_MUTED);
        lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        left.add(titleRow);
        left.add(Box.createVerticalStrut(6));
        left.add(lblDesc);

        hdr.add(left, BorderLayout.CENTER);
        return hdr;
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
                int cardW = 280;
                int cols = Math.max(1, (w - 15) / (cardW + 15));
                int rH = 130 + 15;
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
        gbc.insets = new Insets(0, 0, 0, 15);
        gbc.fill = GridBagConstraints.VERTICAL;

        JLabel iconSearch = new JLabel();
        ImageIcon icoSearch = loadScaledIcon("nutTimKiem.png", 22);
        if (icoSearch!=null) iconSearch.setIcon(icoSearch);
        bgPanel.add(iconSearch, gbc);

        txtSearch = new JTextField(30); 
        txtSearch.setOpaque(true);
        txtSearch.setBackground(Color.WHITE);
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm theo mã hoặc tên đầu máy...");
        
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

        JButton btnClear = new JButton("Tìm/Lọc") {
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
        btnClear.addActionListener(e -> applyFilter());
        
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 0, 0, 0);
        bgPanel.add(btnClear, gbc);

        bar.add(bgPanel, BorderLayout.CENTER);
        return bar;
    }

    // ====================================================================
    //  DATA OPERATIONS
    // ====================================================================

    private void loadData() {
        allData = daoDauMay.getAll();
        applyFilter();
    }

    private void applyFilter() {
        String q = txtSearch.getText().trim().toLowerCase();

        filteredData = new ArrayList<>();
        for (DauMay dm : allData) {
            boolean ok = q.isEmpty() || 
                         dm.getMaDauMay().toLowerCase().contains(q) || 
                         dm.getTenDauMay().toLowerCase().contains(q);
            if (ok) filteredData.add(dm);
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
            List<DauMay> pageData = filteredData.subList(start, end);

            // Calculate card width
            int w = scrollPane.getWidth();
            int cardW = 280;
            if (gridCols > 0) {
                // Adjust width to fill container better (optional)
                cardW = (w - (gridCols + 1) * 15) / gridCols;
                cardW = Math.max(cardW, 250);
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
                g2.setColor(new Color(0, 0, 0, isHovered ? 12 : 6));
                g2.fillRoundRect(3, 4, w - 6, h - 5, 16, 16);
                
                // Card Background
                g2.setColor(CARD_BG);
                g2.fillRoundRect(2, 0, w - 6, h - 6, 16, 16);
                
                // Border
                g2.setColor(isHovered ? PRIMARY.brighter() : OUTLINE);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(2, 0, w - 6, h - 6, 16, 16);
                
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(width, 144));
        card.setMaximumSize(new Dimension(width, 144));
        card.setBorder(new EmptyBorder(10, 12, 10, 12));

        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setOpaque(false);

        // Header
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        
        JLabel lblCode = new JLabel("#" + dm.getMaDauMay()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(PRIMARY_LIGHT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblCode.setFont(new Font("Consolas", Font.BOLD, 12));
        lblCode.setForeground(PRIMARY);
        lblCode.setBorder(new EmptyBorder(4, 10, 4, 10));

        // Icon left
        JLabel iconLabel = new JLabel();
        ImageIcon ico = loadScaledIcon("bieuTuongTau.png", 24);
        if (ico != null) iconLabel.setIcon(ico);

        JPanel topLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topLeft.setOpaque(false);
        topLeft.add(iconLabel);
        topLeft.add(lblCode);

        // Edit button
        JButton btnEdit = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover() || getModel().isPressed()) {
                    g2.setColor(PRIMARY_LIGHT);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        ImageIcon iEdit = loadScaledIcon("nutSua.png", 16);
        if (iEdit != null) btnEdit.setIcon(iEdit);
        btnEdit.setPreferredSize(new Dimension(28, 28));
        btnEdit.setContentAreaFilled(false);
        btnEdit.setBorderPainted(false);
        btnEdit.setFocusPainted(false);
        btnEdit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEdit.setToolTipText("Sửa");
        btnEdit.addActionListener(e -> updateTenDauMay(dm));

        top.add(topLeft, BorderLayout.WEST);
        top.add(btnEdit, BorderLayout.EAST);

        // Body
        JPanel infoBlock = new JPanel();
        infoBlock.setLayout(new BoxLayout(infoBlock, BoxLayout.Y_AXIS));
        infoBlock.setOpaque(false);

        JLabel lblName = new JLabel(ellipsize(dm.getTenDauMay(), 24));
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblName.setForeground(TEXT_MAIN);
        lblName.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblType = new JLabel("TRẠM ĐẦU KÉO");
        lblType.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblType.setForeground(TEXT_MUTED);
        lblType.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoBlock.add(lblType);
        infoBlock.add(Box.createVerticalStrut(6));
        infoBlock.add(lblName);

        content.add(top, BorderLayout.NORTH);
        content.add(infoBlock, BorderLayout.CENTER);

        card.add(content, BorderLayout.CENTER);

        return card;
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

    private void updateTenDauMay(DauMay dm) {
        String newName = JOptionPane.showInputDialog(this, 
                "Nhập tên đầu máy mới (Mã: " + dm.getMaDauMay() + "):",
                dm.getTenDauMay());
        if (newName != null && !newName.trim().isEmpty()) {
            dm.setTenDauMay(newName.trim());
            if (daoDauMay.update(dm)) {
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể cập nhật tên đầu máy!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
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
