package com.modules;

import com.dao.DAO_Ve;
import com.dao.DAO_ChiTietHoaDon;
import com.entity.KhachHang;
import com.entity.Ve;
import com.entity.ChiTietHoaDon;
import com.entity.Lich;
import com.entity.Tuyen;
import com.enums.TrangThaiVe;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class QuanLyVeModule extends JPanel implements AppModule {

    private Consumer<Object> callback;

    // --- UI ---
    private JTextField   txtSearchKeyword;
    private JTable       table;
    private VeTableModel tableModel;

    private JLabel lblPageInfo;

    // --- Data ---
    private List<Ve> allData      = new ArrayList<>();
    private List<Ve> filteredData = new ArrayList<>();
    private List<VeTableModel.VeRow> allRows = new ArrayList<>();
    private DAO_ChiTietHoaDon   daoChiTietHoaDon = new DAO_ChiTietHoaDon();

    // --- Stats ---
    private JLabel lblStatTongHoaDon;
    private JLabel lblStatDoanhThu;
    private JLabel lblStatTyLeHuy;

    // --- Design tokens ---
    private static final Color PRIMARY       = NotionTheme.ACCENT;
    private static final Color PRIMARY_LIGHT = NotionTheme.ACCENT_SOFT;
    private static final Color SURFACE       = NotionTheme.PAGE;
    private static final Color CARD_BG       = NotionTheme.CARD;
    private static final Color ON_SURFACE    = NotionTheme.TEXT;
    private static final Color ON_SURF_VAR   = NotionTheme.TEXT_MUTED;
    private static final Color OUTLINE       = NotionTheme.BORDER;
    private static final Color ROW_ALT       = NotionTheme.PAGE;
    private static final Color ROW_HOVER     = NotionTheme.ACCENT_SOFT;
    private static final Color ERROR         = AppColors.ERROR_DARK;
    private static final Color ERROR_BG      = AppColors.ERROR_LIGHT;
    private static final Color ERROR_FG      = AppColors.ERROR_DARK;

    private static final Color STATUS_GREEN_BG  = AppColors.SUCCESS_LIGHT;
    private static final Color STATUS_GREEN_FG  = AppColors.SUCCESS_DARK;
    private static final Color STATUS_GRAY_BG   = NotionTheme.PAGE;
    private static final Color STATUS_GRAY_FG   = NotionTheme.TEXT_MUTED;

    private static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font FONT_DESC    = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_BOLD    = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_MONO    = new Font("Consolas", Font.BOLD, 13);
    private static final Font FONT_BADGE   = new Font("Segoe UI", Font.BOLD, 11);
    private static final Font FONT_HEADER  = new Font("Segoe UI", Font.BOLD, 11);
    private static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_STAT_NUM = new Font("Segoe UI", Font.BOLD, 28);
    private static final Font FONT_STAT_LBL = new Font("Segoe UI", Font.BOLD, 10);

    private static final DateTimeFormatter FMT_DATETIME = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy");
    private static final NumberFormat FMT_MONEY = NumberFormat.getInstance(new Locale("vi", "VN"));

    // --- Hidden buttons for AppModule compliance ---
    private JButton btnSubmit = new JButton();
    private JButton btnCancel = new JButton();
    private JPanel  btnPanel  = new JPanel();

    // --- Hover ---
    private int hoveredRow = -1;

    public QuanLyVeModule() {
        setLayout(new BorderLayout());
        setBackground(SURFACE);
        setBorder(new EmptyBorder(28, 36, 28, 36));
        btnPanel.setVisible(false);
        buildUI();
        loadData();
    }

    // =================================================================
    //  BUILD UI
    // =================================================================

    private void buildUI() {
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setOpaque(false);

        JPanel header = buildHeader();
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        JPanel stats = buildStatsRow();
        stats.setAlignmentX(Component.LEFT_ALIGNMENT);
        stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        JPanel search = buildSearchSection();
        search.setAlignmentX(Component.LEFT_ALIGNMENT);
        NotionTheme.lockMaxWidthToPreferredHeight(search);
        // Handoff: search card tính chiều cao từ nội dung để chịu được font/DPI khác nhau.
        // Cảnh báo: stats phía trên vẫn giữ chiều cao cố định vì là cụm KPI đồng đều.

        mainContent.add(header);
        mainContent.add(Box.createVerticalStrut(20));
        mainContent.add(stats);
        mainContent.add(Box.createVerticalStrut(20));
        mainContent.add(search);
        mainContent.add(Box.createVerticalStrut(20));

        add(mainContent, BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(24, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, NotionTheme.NAVY, getWidth(), getHeight(), new Color(0x00, 0x75, 0xDE));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                g2.setColor(AppColors.withAlpha(NotionTheme.SKY, 118));
                g2.fillOval(getWidth() - 185, 12, 142, 142);
                g2.setColor(AppColors.withAlpha(NotionTheme.MINT, 112));
                g2.fillRoundRect(getWidth() - 330, 100, 174, 42, 20, 20);
                g2.setColor(AppColors.withAlpha(Color.WHITE, 38));
                g2.fillOval(getWidth() - 286, 36, 64, 64);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(24, 28, 24, 28));
        header.setPreferredSize(new Dimension(10, 150));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel eyebrow = new JLabel("WORKSPACE / NGHIỆP VỤ");
        eyebrow.setFont(new Font("Segoe UI", Font.BOLD, 11));
        eyebrow.setForeground(AppColors.withAlpha(Color.WHITE, 190));
        JLabel lblTitle = new JLabel("Quản lý vé");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblTitle.setForeground(Color.WHITE);
        JLabel lblDesc = new JLabel("Theo dõi giao dịch, tra cứu vé và xử lý hoàn trả khách hàng.");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblDesc.setForeground(AppColors.withAlpha(Color.WHITE, 210));

        left.add(eyebrow);
        left.add(Box.createVerticalStrut(8));
        left.add(lblTitle);
        left.add(Box.createVerticalStrut(8));
        left.add(lblDesc);

        JPanel right = new JPanel(new GridBagLayout());
        right.setOpaque(false);
        right.add(createHeroBadge("Tra cứu & hoàn vé"));

        header.add(left, BorderLayout.CENTER);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    // =================================================================
    //  STATS ROW
    // =================================================================

    private JPanel buildStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 16, 0));
        row.setOpaque(false);

        lblStatTongHoaDon = new JLabel("0");
        lblStatDoanhThu = new JLabel("0");
        lblStatTyLeHuy = new JLabel("0%");

        row.add(buildStatCard("Tổng vé", lblStatTongHoaDon, PRIMARY, NotionTheme.ACCENT_SOFT));
        row.add(buildStatCard("Đã bán", lblStatDoanhThu, new Color(0x1A, 0xAE, 0x39), NotionTheme.MINT));
        row.add(buildStatCard("Tỷ lệ hủy vé", lblStatTyLeHuy, ERROR, NotionTheme.ROSE));

        return row;
    }

    private JPanel buildStatCard(String label, JLabel valueLbl, Color accent, Color tint) {
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
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLbl.setForeground(accent);
        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblLabel.setForeground(ON_SURF_VAR);
        text.add(valueLbl);
        text.add(Box.createVerticalStrut(2));
        text.add(lblLabel);

        card.add(marker, BorderLayout.WEST);
        card.add(text, BorderLayout.CENTER);
        return card;
    }

    // =================================================================
    //  SEARCH SECTION
    // =================================================================

    private JPanel buildSearchSection() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 12)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                NotionTheme.paintCard(g2, this, CARD_BG, OUTLINE, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(18, 22, 18, 22));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Bộ lọc vé");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(ON_SURFACE);
        JLabel subtitle = new JLabel("Tra cứu theo mã vé, hóa đơn, hành khách, ga hoặc trạng thái");
        subtitle.setFont(FONT_SMALL);
        subtitle.setForeground(ON_SURF_VAR);
        header.add(title, BorderLayout.WEST);
        header.add(subtitle, BorderLayout.EAST);

        JPanel bgPanel = new JPanel(new GridBagLayout());
        bgPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 20);
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Listeners for real-time search
        javax.swing.event.DocumentListener liveSearch = new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
        };

        JPanel searchPanel = new JPanel(new BorderLayout(0, 4));
        searchPanel.setOpaque(false);
        JLabel label = new JLabel("TỪ KHÓA");
        label.setFont(new Font("Segoe UI", Font.BOLD, 10));
        label.setForeground(ON_SURF_VAR);
        txtSearchKeyword = createSearchField("Nhập mã vé, mã hóa đơn, tên khách, ga, trạng thái...");
        txtSearchKeyword.getDocument().addDocumentListener(liveSearch);
        searchPanel.add(label, BorderLayout.NORTH);
        searchPanel.add(createSearchBox(txtSearchKeyword), BorderLayout.CENTER);

        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        bgPanel.add(searchPanel, gbc);

        // Handoff: tìm kiếm vé gom về một keyword chung để đồng bộ UX với các module quản lý khác.
        // Rủi ro: lọc theo hóa đơn/khách cần gọi DAO phụ nên danh sách lớn có thể cần cache row về sau.

        wrapper.add(header, BorderLayout.NORTH);
        wrapper.add(bgPanel, BorderLayout.CENTER);
        return wrapper;
    }

    // =================================================================
    //  TABLE CARD
    // =================================================================

    private JPanel buildTableCard() {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                NotionTheme.paintCard(g2, this, CARD_BG, OUTLINE, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);

        card.add(buildTableHeader(), BorderLayout.NORTH);
        card.add(buildTableSection(), BorderLayout.CENTER);
        card.add(buildTableFooter(), BorderLayout.SOUTH);

        return card;
    }

    private JPanel buildTableHeader() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, OUTLINE),
                new EmptyBorder(16, 20, 16, 20)
        ));

        JLabel lbl = new JLabel("Danh sách vé");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lbl.setForeground(ON_SURFACE);

        // Filter tabs
        JPanel tabs = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(NotionTheme.BORDER);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
            }
        };
        tabs.setOpaque(false);
        tabs.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 3));

        String[] tabNames = {"Tất cả", "Đã bán", "Đã hủy"};
        JButton[] tabButtons = new JButton[tabNames.length];

        for (int i = 0; i < tabNames.length; i++) {
            final int idx = i;
            JButton tab = new JButton(tabNames[i]) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (getModel().isPressed() || getClientProperty("active") == Boolean.TRUE) {
                        g2.setColor(Color.WHITE);
                        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                    }
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            tab.setFont(FONT_BADGE);
            tab.setForeground(i == 0 ? PRIMARY : ON_SURF_VAR);
            tab.setContentAreaFilled(false);
            tab.setBorderPainted(false);
            tab.setFocusPainted(false);
            tab.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            tab.setPreferredSize(new Dimension(90, 28));
            if (i == 0) tab.putClientProperty("active", Boolean.TRUE);

            tab.addActionListener(e -> {
                for (JButton b : tabButtons) {
                    b.putClientProperty("active", Boolean.FALSE);
                    b.setForeground(ON_SURF_VAR);
                    b.repaint();
                }
                tab.putClientProperty("active", Boolean.TRUE);
                tab.setForeground(PRIMARY);
                tab.repaint();
                applyTabFilter(idx);
            });

            tabButtons[i] = tab;
            tabs.add(tab);
        }

        bar.add(lbl, BorderLayout.WEST);
        bar.add(tabs, BorderLayout.EAST);

        return bar;
    }

    private int activeTabIndex = 0;

    private void applyTabFilter(int tabIdx) {
        activeTabIndex = tabIdx;
        applyFilter();
    }

    private JScrollPane buildTableSection() {
        tableModel = new VeTableModel();
        table = new JTable(tableModel);
        NotionTheme.styleTable(table);
        table.setRowHeight(56);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(OUTLINE);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(NotionTheme.TABLE_SELECTION);
        table.setSelectionForeground(ON_SURFACE);
        table.setFont(FONT_BODY);
        table.setFillsViewportHeight(true);
        table.setBackground(CARD_BG);
        table.getTableHeader().setReorderingAllowed(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        // Hover
        table.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row != hoveredRow) { hoveredRow = row; table.repaint(); }
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) { hoveredRow = -1; table.repaint(); }
            @Override public void mousePressed(MouseEvent e) { showVeQuickActions(e); }
            @Override public void mouseReleased(MouseEvent e) { showVeQuickActions(e); }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e) || e.getClickCount() != 2) return;
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row < 0 || col == 6) return;
                var veRow = tableModel.getRowAt(row);
                if (veRow != null) openVeDetailDialog(veRow);
            }
        });
        // Handoff: thân dòng vé mở bằng double-click; các nút Xem/Hoàn vé vẫn single-click.
        // Cảnh báo: nếu bật sorter sau này cần convertRowIndexToModel trước getRowAt.

        // Header style
        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setFont(FONT_HEADER);
                lbl.setForeground(ON_SURF_VAR);
                lbl.setBackground(NotionTheme.PAGE);
                lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, OUTLINE),
                        new EmptyBorder(0, 20, 0, 8)
                ));
                lbl.setPreferredSize(new Dimension(lbl.getPreferredSize().width, 44));
                if (column == 6) lbl.setHorizontalAlignment(SwingConstants.CENTER);
                return lbl;
            }
        });

        // Columns: Mã vé, Mã hóa đơn, Hành khách, Tuyến, Khởi hành, Trạng thái, Thao tác
        TableColumnModel colModel = table.getColumnModel();
        int[] widths = {110, 130, 170, 220, 140, 120, 160};
        for (int i = 0; i < widths.length; i++) {
            colModel.getColumn(i).setPreferredWidth(widths[i]);
        }

        colModel.getColumn(0).setCellRenderer(new RowCellRenderer(FONT_MONO, PRIMARY));
        colModel.getColumn(1).setCellRenderer(new RowCellRenderer(FONT_MONO, ON_SURF_VAR));
        colModel.getColumn(2).setCellRenderer(new RowCellRenderer(FONT_BOLD, ON_SURFACE));
        colModel.getColumn(3).setCellRenderer(new RowCellRenderer(FONT_BODY, ON_SURFACE));
        colModel.getColumn(4).setCellRenderer(new RowCellRenderer(FONT_SMALL, ON_SURF_VAR));
        colModel.getColumn(5).setCellRenderer(new TrangThaiBadgeRenderer());
        colModel.getColumn(6).setCellRenderer(new ActionButtonRenderer());
        colModel.getColumn(6).setCellEditor(new ActionButtonEditor());


        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(CARD_BG);
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setWheelScrollingEnabled(true);

        return sp;
    }

    private JPanel buildTableFooter() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, OUTLINE),
                new EmptyBorder(12, 20, 12, 20)
        ));

        lblPageInfo = new JLabel();
        lblPageInfo.setFont(FONT_SMALL);
        lblPageInfo.setForeground(ON_SURF_VAR);

        bar.add(lblPageInfo, BorderLayout.WEST);
        return bar;
    }

    // =================================================================
    //  HELPERS
    // =================================================================

    private JLabel createHeroBadge(String text) {
        JLabel badge = new JLabel(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppColors.withAlpha(Color.WHITE, 235));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(new Font("Segoe UI", Font.BOLD, 13));
        badge.setForeground(NotionTheme.NAVY);
        badge.setBorder(new EmptyBorder(10, 16, 10, 16));
        return badge;
    }

    private JTextField createSearchField(String placeholder) {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !hasFocus()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setColor(NotionTheme.BORDER);
                    g2.setFont(FONT_BODY);
                    Insets ins = getInsets();
                    g2.drawString(placeholder, Math.max(ins.left, 2), getHeight() / 2 + 5);
                    g2.dispose();
                }
            }
        };
        field.setFont(FONT_BODY);
        field.setColumns(1);
        field.setMinimumSize(new Dimension(0, 38));
        field.setPreferredSize(new Dimension(0, 38));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1),
                new EmptyBorder(6, 12, 6, 12)
        ));
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) { field.repaint(); }
            public void focusLost(java.awt.event.FocusEvent e) { field.repaint(); }
        });
        return field;
    }

    private JPanel createSearchBox(JTextField field) {
        field.setBorder(BorderFactory.createEmptyBorder());
        field.setOpaque(false);

        JPanel searchBox = new JPanel(new BorderLayout(10, 0));
        searchBox.setOpaque(false);
        searchBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        searchBox.add(new JLabel(LineIcons.image(LineIcons.Name.SEARCH, 18, 18)), BorderLayout.WEST);
        searchBox.add(field, BorderLayout.CENTER);
        SearchFieldClearButton.install(searchBox, field, this::applyFilter);
        return searchBox;
        // Handoff: chỉ bọc field tra cứu thật để kính lúp nằm trong thanh tìm kiếm, không gắn vào filter khác.
        // Cảnh báo: placeholder tự vẽ nên x bắt đầu gần 2px trong field, không cộng lại padding wrapper.
    }

    // =================================================================
    //  DATA
    // =================================================================

    private void loadData() {
        SwingWorker<List<Ve>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Ve> doInBackground() {
                return new DAO_Ve().getAll();
            }

            @Override
            protected void done() {
                try {
                    allData = get();
                } catch (Exception e) {
                    allData = new ArrayList<>();
                }
                rebuildRowCache();
                updateStats();
                applyFilter();
            }
        };
        worker.execute();
    }

    private void updateStats() {
        long totalVe = allData.size();
        long soldVe = allData.stream().filter(v -> v.getTrangThai() == TrangThaiVe.DA_BAN).count();
        long cancelledVe = allData.stream().filter(v -> v.getTrangThai() == TrangThaiVe.DA_HUY).count();

        lblStatTongHoaDon.setText(String.valueOf(totalVe));
        lblStatDoanhThu.setText(String.valueOf(soldVe));
        double cancelRate = totalVe > 0 ? (cancelledVe * 100.0 / totalVe) : 0;
        lblStatTyLeHuy.setText(String.format("%.1f%%", cancelRate));
    }

    // =================================================================
    //  FILTER & SEARCH
    // =================================================================

    private void applyFilter() {
        String keyword = txtSearchKeyword.getText().trim().toLowerCase();

        filteredData = new ArrayList<>();
        List<VeTableModel.VeRow> rows = new ArrayList<>();
        for (VeTableModel.VeRow row : allRows) {
            Ve ve = row.ve();
            if (activeTabIndex == 1 && ve.getTrangThai() != TrangThaiVe.DA_BAN) continue;
            if (activeTabIndex == 2 && ve.getTrangThai() != TrangThaiVe.DA_HUY) continue;
            if (!keyword.isEmpty() && !matchesKeyword(row, keyword)) continue;

            filteredData.add(ve);
            rows.add(row);
        }
        refreshTable(rows);
        // Handoff: keyword chung tìm qua vé/hóa đơn/khách/ga/trạng thái để thay hai ô search cũ.
        // Rủi ro: cache row được rebuild khi loadData, nếu dữ liệu liên quan đổi ngoài module cần load lại để đồng bộ.
    }

    private boolean matchesKeyword(VeTableModel.VeRow row, String keyword) {
        Ve ve = row.ve();
        String trangThai = ve.getTrangThai() != null ? ve.getTrangThai().toString() : "";
        String haystack = String.join(" ",
                safe(ve.getMaVe()), safe(row.maHoaDon()), safe(row.tenKhachHang()), safe(row.tuyen()),
                safe(row.khoiHanh()), safe(trangThai), statusLabel(ve.getTrangThai())).toLowerCase();
        return haystack.contains(keyword);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private void refreshTable(List<VeTableModel.VeRow> rows) {
        tableModel.setData(rows);
        int totalRecords = rows.size();

        lblPageInfo.setText(totalRecords == 0
                ? "Không tìm thấy vé nào"
                : "Hiển thị " + totalRecords + " vé");
    }

    private void rebuildRowCache() {
        allRows = new ArrayList<>();
        for (Ve ve : allData) allRows.add(buildRow(ve));
    }

    private VeTableModel.VeRow buildRow(Ve ve) {
        String maHoaDon = "", tuyenText = "", khoiHanh = "", tenKhachHang = "";
        try {
            if (ve.getLich() != null && ve.getLich().getTuyen() != null) {
                Tuyen tuyen = ve.getLich().getTuyen();
                String gaDi = tuyen.getGaDi() != null ? tuyen.getGaDi().getTenGa() : "";
                String gaDen = tuyen.getGaDen() != null ? tuyen.getGaDen().getTenGa() : "";
                String maTuyen = tuyen.getMaTuyen() != null ? tuyen.getMaTuyen() : "";
                tuyenText = formatTuyen(maTuyen, gaDi, gaDen);
            }
            if (ve.getLich() != null && ve.getLich().getThoiGianBatDau() != null) {
                khoiHanh = ve.getLich().getThoiGianBatDau().format(FMT_DATETIME);
            }
            ChiTietHoaDon cthd = daoChiTietHoaDon.findByVe(ve.getMaVe());
            if (cthd != null) {
                if (cthd.getHoaDon() != null) maHoaDon = cthd.getHoaDon().getMaHoaDon();
                KhachHang kh = cthd.getKhachHang();
                if (kh != null && kh.getHoTen() != null) tenKhachHang = kh.getHoTen();
            }
        } catch (Exception ignored) {}
        return new VeTableModel.VeRow(ve, maHoaDon, tuyenText, khoiHanh, tenKhachHang);
    }

    private String formatTuyen(String maTuyen, String gaDi, String gaDen) {
        String hanhTrinh = (gaDi.isBlank() && gaDen.isBlank()) ? "" : gaDi + " → " + gaDen;
        if (!hanhTrinh.isBlank()) return hanhTrinh;
        return maTuyen == null ? "" : maTuyen;
        // Handoff: cột tuyến ưu tiên tên hành trình để bảng dễ đọc, không kèm mã tuyến gây chật cột.
        // Cảnh báo: mã tuyến chỉ là fallback khi DAO không load được tên ga đi/ga đến.
    }

    private String statusLabel(TrangThaiVe status) {
        return status == TrangThaiVe.DA_HUY ? "Đã hủy" : "Đã thanh toán";
    }

    // =================================================================
    //  DATA MODEL
    // =================================================================

    // Non-static inner class de setValueAt co the truy cap allData va DAO
    private class VeTableModel extends AbstractTableModel {
        record VeRow(Ve ve, String maHoaDon, String tuyen, String khoiHanh, String tenKhachHang) {}
        private final String[] COLUMNS = {"MÃ VÉ", "MÃ HÓA ĐƠN", "HÀNH KHÁCH", "TUYẾN", "KHỞI HÀNH", "TRẠNG THÁI", "THAO TÁC"};
        private List<VeRow> data = new ArrayList<>();

        void setData(List<VeRow> data) {
            this.data = new ArrayList<>(data);
            fireTableDataChanged();
        }

        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return COLUMNS.length; }
        @Override public String getColumnName(int c) { return COLUMNS[c]; }

        @Override
        public boolean isCellEditable(int r, int c) {
            return c == 6; // Chi cho phep nhan nut thao tac
        }

        @Override
        public Object getValueAt(int r, int c) {
            VeRow row = data.get(r);
            Ve ve = row.ve();
            return switch (c) {
                case 0 -> ve.getMaVe();
                case 1 -> row.maHoaDon();
                case 2 -> row.tenKhachHang();
                case 3 -> row.tuyen();
                case 4 -> row.khoiHanh();
                case 5 -> ve.getTrangThai() != null ? ve.getTrangThai() : TrangThaiVe.DA_BAN;
                case 6 -> ve.getTrangThai();
                default -> "";
            };
        }


        VeRow getRowAt(int r) { return (r >= 0 && r < data.size()) ? data.get(r) : null; }
    }

    // =================================================================
    //  CELL RENDERERS
    // =================================================================

    private class RowCellRenderer extends DefaultTableCellRenderer {
        private final Font font;
        private final Color fg;

        RowCellRenderer(Font font, Color fg) {
            this.font = font;
            this.fg = fg;
        }

        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object value,
                boolean isSel, boolean hasFocus, int row, int col) {
            super.getTableCellRendererComponent(tbl, value, isSel, hasFocus, row, col);
            setFont(font);
            setForeground(fg);
            setBorder(new EmptyBorder(0, 20, 0, 8));
            setBackground(getRowBg(isSel, row));
            return this;
        }
    }

    private class TrangThaiBadgeRenderer extends JPanel implements TableCellRenderer {
        private final JLabel badge = new JLabel();
        private Color badgeBg = OUTLINE;
        private static final int BADGE_HEIGHT = 24;
        private static final int BADGE_MARGIN_X = 28;

        TrangThaiBadgeRenderer() {
            setLayout(new BorderLayout());
            setOpaque(true);
            setBorder(new EmptyBorder(0, BADGE_MARGIN_X, 0, BADGE_MARGIN_X));
            badge.setFont(FONT_BADGE);
            badge.setHorizontalAlignment(SwingConstants.CENTER);
            badge.setOpaque(false);
            badge.setPreferredSize(new Dimension(10, BADGE_HEIGHT));
            add(badge, BorderLayout.CENTER);
            // Handoff: badge trạng thái vé bám gần full ô, giữ dot như trạng thái phụ bên trái.
            // Nếu cột hẹp làm text chật, giảm BADGE_MARGIN_X hoặc nới cột trạng thái.
        }

        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object value,
                boolean isSel, boolean hasFocus, int row, int col) {
            TrangThaiVe tt = (value instanceof TrangThaiVe) ? (TrangThaiVe) value : TrangThaiVe.DA_BAN;

            if (tt == TrangThaiVe.DA_BAN) {
                badgeBg = STATUS_GREEN_BG;
                badge.setForeground(STATUS_GREEN_FG);
                badge.setText("Đã thanh toán");
            } else {
                badgeBg = STATUS_GRAY_BG;
                badge.setForeground(STATUS_GRAY_FG);
                badge.setText("Đã hủy");
            }
            setBackground(getRowBg(isSel, row));
            return this;
        }

        @Override
        protected void paintChildren(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Rectangle r = badge.getBounds();
            g2.setColor(badgeBg);
            int y = r.y + Math.max(0, (r.height - BADGE_HEIGHT) / 2);
            g2.fillRoundRect(r.x, y, r.width, BADGE_HEIGHT, 20, 20);
            // dot
            Color dotColor = (badgeBg == STATUS_GREEN_BG) ? STATUS_GREEN_FG : STATUS_GRAY_FG;
            g2.setColor(dotColor);
            int dotX = r.x + 10;
            int dotY = y + BADGE_HEIGHT / 2 - 2;
            g2.fillOval(dotX, dotY, 5, 5);
            g2.dispose();
            super.paintChildren(g);
        }
    }

    /** Action buttons: Chi tiet + Hoan ve */
    private class ActionButtonRenderer extends JPanel implements TableCellRenderer {
        private final JLabel lblDetail = new JLabel("Xem");
        private final JLabel lblRefund = new JLabel("Hoàn vé");
        private boolean isCancelled = false;

        ActionButtonRenderer() {
            setLayout(new GridBagLayout());
            setOpaque(true);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(0, 5, 0, 5);
            configLabel(lblDetail, AppColors.ACTION_SOFT_FG, new Dimension(52, 28));
            configLabel(lblRefund, ERROR_FG,                 new Dimension(72, 28));
            add(lblDetail, gbc);
            add(lblRefund, gbc);
        }

        private void configLabel(JLabel lbl, Color fg, Dimension size) {
            lbl.setFont(FONT_BADGE);
            lbl.setForeground(fg);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setPreferredSize(size);
            lbl.setOpaque(false);
        }

        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object value,
                boolean isSel, boolean hasFocus, int row, int col) {
            setBackground(getRowBg(isSel, row));
            isCancelled = (value instanceof TrangThaiVe tt && tt == TrangThaiVe.DA_HUY);
            lblRefund.setForeground(isCancelled ? ON_SURF_VAR : ERROR_FG);
            return this;
        }

        @Override
        protected void paintChildren(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Rectangle rd = lblDetail.getBounds();
            g2.setColor(AppColors.ACTION_SOFT_BG);
            g2.fillRoundRect(rd.x, rd.y, rd.width, rd.height, 8, 8);
            Rectangle rr = lblRefund.getBounds();
            g2.setColor(isCancelled ? NotionTheme.PAGE : ERROR_BG);
            g2.fillRoundRect(rr.x, rr.y, rr.width, rr.height, 8, 8);
            g2.dispose();
            super.paintChildren(g);
        }
    }

    private class ActionButtonEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel  panel     = new JPanel(new GridBagLayout());
        private final JButton btnDetail = new JButton("Xem");
        private final JButton btnRefund = new JButton("Hoàn vé");
        private int editingRow;

        ActionButtonEditor() {
            panel.setOpaque(true);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(0, 5, 0, 5);

            btnDetail.setFont(FONT_BADGE);
            btnDetail.setForeground(AppColors.ACTION_SOFT_FG);
            btnDetail.setBackground(AppColors.ACTION_SOFT_BG);
            btnDetail.setBorderPainted(false);
            btnDetail.setFocusPainted(false);
            btnDetail.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnDetail.setPreferredSize(new Dimension(52, 28));
            panel.add(btnDetail, gbc);

            btnRefund.setFont(FONT_BADGE);
            btnRefund.setForeground(ERROR_FG);
            btnRefund.setBackground(ERROR_BG);
            btnRefund.setBorderPainted(false);
            btnRefund.setFocusPainted(false);
            btnRefund.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnRefund.setPreferredSize(new Dimension(72, 28));
            panel.add(btnRefund, gbc);

            btnDetail.setToolTipText("Xem chi tiết");
            btnRefund.setToolTipText("Hoàn vé");

            btnDetail.addActionListener(e -> {
                fireEditingStopped();
                var veRow = tableModel.getRowAt(editingRow);
                if (veRow != null) {
                    openVeDetailDialog(veRow);
                }
            });

            btnRefund.addActionListener(e -> {
                fireEditingStopped();
                var veRow = tableModel.getRowAt(editingRow);
                if (veRow != null) refundTicket(veRow);
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable tbl, Object value,
                boolean isSel, int row, int col) {
            editingRow = row;
            panel.setBackground(getRowBg(true, row));
            boolean isCancelled = (value instanceof TrangThaiVe tt && tt == TrangThaiVe.DA_HUY);
            btnRefund.setEnabled(!isCancelled);
            btnRefund.setForeground(isCancelled ? ON_SURF_VAR : ERROR_FG);
            btnRefund.setBackground(isCancelled ? NotionTheme.PAGE : ERROR_BG);
            return panel;
        }

        @Override
        public Object getCellEditorValue() { return null; }
    }

    private Color getRowBg(boolean isSel, int row) {
        if (isSel) return NotionTheme.TABLE_SELECTION;
        if (row == hoveredRow) return ROW_HOVER;
        return row % 2 == 0 ? CARD_BG : ROW_ALT;
    }

    // =================================================================
    private void showVeQuickActions(MouseEvent e) {
        if (!e.isPopupTrigger()) return;
        int row = table.rowAtPoint(e.getPoint());
        if (row < 0) return;
        table.setRowSelectionInterval(row, row);
        var veRow = tableModel.getRowAt(row);
        if (veRow == null) return;
        JPopupMenu menu = new JPopupMenu();
        JMenuItem view = new JMenuItem("Xem chi tiết");
        view.addActionListener(ev -> openVeDetailDialog(veRow));
        JMenuItem refund = new JMenuItem("Hoàn vé");
        refund.setEnabled(veRow.ve().getTrangThai() != TrangThaiVe.DA_HUY);
        refund.addActionListener(ev -> refundTicket(veRow));
        menu.add(view);
        menu.addSeparator();
        menu.add(refund);
        menu.show(table, e.getX(), e.getY());
    }


    private void refundTicket(VeTableModel.VeRow veRow) {
        Ve ve = veRow.ve();
        if (ve.getTrangThai() == TrangThaiVe.DA_HUY) {
            NotionMessageDialog.showMessageDialog(this,
                    "Vé này đã được hủy trước đó.",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Window owner = SwingUtilities.getWindowAncestor(this);
        HoanVeDialog dialog = new HoanVeDialog(
                owner, ve, veRow.tenKhachHang(),
                veRow.tuyen(),
                veRow.khoiHanh());
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            boolean ok = new DAO_Ve().huyVe(ve.getMaVe(), dialog.getLyDo());
            if (ok) {
                NotionMessageDialog.showMessageDialog(this,
                        "Hoàn vé thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadData();
            } else {
                NotionMessageDialog.showMessageDialog(this,
                        "Không thể hoàn vé. Vui lòng thử lại.",
                        "Hoàn vé thất bại", JOptionPane.ERROR_MESSAGE);
            }
        }
        // Handoff: nút bảng và menu chuột phải dùng chung luồng hoàn vé để giữ kiểm tra DA_HUY nhất quán.
        // Cảnh báo: nếu sau này thêm phí hoàn/hạn hoàn, cập nhật một hàm này thay vì từng entry point.
    }

    public void applySearchVe(String text) {
        txtSearchKeyword.setText(text != null ? text.trim() : "");
        applyFilter();
    }

    /**
     * Nhận keyword từ Dashboard — tìm trên maVe và maHoaDon (qua ChiTietHoaDon).
     */
    public void applySearchFromDashboard(String keyword) {
        txtSearchKeyword.setText(keyword != null ? keyword.trim() : "");
        applyFilter();
    }

    private void openVeDetailDialog(VeTableModel.VeRow veRow) {
        Ve ve = veRow.ve();
        ChiTietHoaDon cthd = daoChiTietHoaDon.findByVe(ve.getMaVe());

        String maHoaDon = "—";
        String giaVe = "—";
        if (cthd != null) {
            if (cthd.getHoaDon() != null && cthd.getHoaDon().getMaHoaDon() != null) {
                maHoaDon = cthd.getHoaDon().getMaHoaDon();
            }
            if (cthd.getGiaTien() != null) {
                giaVe = FMT_MONEY.format(cthd.getGiaTien()) + " ₫";
            }
        }

        KhachHang khachHang = cthd != null ? cthd.getKhachHang() : null;
        String hanhKhach = "—";
        String cccd = "—";
        if (khachHang != null) {
            hanhKhach = khachHang.getHoTen() == null || khachHang.getHoTen().isBlank() ? "—" : khachHang.getHoTen();
            if (khachHang.getCccd() != null && !khachHang.getCccd().isBlank()) cccd = khachHang.getCccd();
        } else if (veRow.tenKhachHang() != null && !veRow.tenKhachHang().isBlank()) {
            hanhKhach = veRow.tenKhachHang();
        }

        Lich lich = ve.getLich();
        String maLich = (lich != null && lich.getMaLich() != null) ? lich.getMaLich() : "—";
        String maDoanTau = (lich != null && lich.getDoanTau() != null && lich.getDoanTau().getMaDoanTau() != null)
                ? lich.getDoanTau().getMaDoanTau() : "—";

        String maGhe = (ve.getGhe() != null && ve.getGhe().getMaGhe() != null) ? ve.getGhe().getMaGhe() : "—";
        String toa = (ve.getGhe() != null && ve.getGhe().getToaTau() != null && ve.getGhe().getToaTau().getMaToaTau() != null)
                ? ve.getGhe().getToaTau().getMaToaTau() : "—";
        String soGhe = (ve.getGhe() != null && ve.getGhe().getSoGhe() > 0) ? String.valueOf(ve.getGhe().getSoGhe()) : "—";

        TrangThaiVe tt = ve.getTrangThai();
        String trangThai = tt == null ? "—" : switch (tt) {
            case DA_BAN -> "Đã thanh toán";
            case DA_HUY -> "Đã hủy";
        };

        String ngayHuy = (ve.getNgayHuy() != null) ? ve.getNgayHuy().format(FMT_DATETIME) : "—";
        String lyDoHuy = (ve.getLyDoHuy() != null && !ve.getLyDoHuy().isBlank()) ? ve.getLyDoHuy() : "—";

        LinkedHashMap<String, String> fields = new LinkedHashMap<>();
        fields.put("Mã vé", ve.getMaVe());
        fields.put("Mã hóa đơn", maHoaDon);
        fields.put("Hành khách", hanhKhach);
        fields.put("CCCD", cccd);
        fields.put("Tuyến", veRow.tuyen());
        fields.put("Khởi hành", veRow.khoiHanh());
        fields.put("Mã lịch", maLich);
        fields.put("Mã đoàn tàu", maDoanTau);
        fields.put("Mã ghế", maGhe);
        fields.put("Toa", toa);
        fields.put("Số ghế", soGhe);
        fields.put("Giá vé", giaVe);
        fields.put("Trạng thái", trangThai);
        fields.put("Lý do hủy", lyDoHuy);
        fields.put("Ngày hủy", ngayHuy);

        EntityDetailModule detail = new EntityDetailModule(
                "Vé", AppColors.WARNING,
                "Chi tiết vé", ve.getMaVe(), fields
        );

        Window win = SwingUtilities.getWindowAncestor(this);
        JFrame frame = (win instanceof JFrame) ? (JFrame) win : null;
        ModuleLauncher.asDialog(detail, frame, res -> {});
    }

    //  AppModule interface
    // =================================================================

    @Override public String getTitle() { return "Quản lý vé"; }
    @Override public JPanel getView() { return this; }
    @Override public void setOnResult(Consumer<Object> cb) {
        this.callback = cb;
        boolean has = (cb != null);
        btnSubmit.setVisible(has);
        btnCancel.setVisible(has);
        btnPanel.setVisible(has);
    }
    @Override public void reset() {
        txtSearchKeyword.setText("");
        activeTabIndex = 0;
        loadData();
    }
}

