package com.modules;

import com.dao.DAO_NhanVien;
import com.entity.NhanVien;
import com.enums.TrangThaiNhanVien;
import com.enums.VaiTro;

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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class QuanLyNhanVienModule extends JPanel implements AppModule {

    private Consumer<Object> callback;

    // --- UI components ---
    private JTextField        txtSearch;
    private JComboBox<String> cboVaiTro;
    private JComboBox<String> cboTrangThai;
        private JComboBox<String> cboGaLamViec;
private JButton           btnAddNew;
    private JTable            table;
    private NhanVienTableModel tableModel;

    // --- Pagination ---
    private int totalRecords = 0;

    private JLabel  lblPageInfo;
    private JLabel  lblTotalCount;
    private JLabel  lblActiveCount;
    private JLabel  lblLeaveCount;
    private JLabel  lblAdminCount;

    // --- Data ---
    private List<NhanVien> allData      = new ArrayList<>();
    private List<NhanVien> filteredData = new ArrayList<>();

        private String[] gaFilterKeys = new String[]{null};
// --- Design tokens ---
    private static final Color PRIMARY       = NotionTheme.ACCENT;
    private static final Color PRIMARY_LIGHT = AppColors.ACTION_SOFT_BG;
    private static final Color SURFACE       = NotionTheme.PAGE;
    private static final Color CARD_BG       = NotionTheme.CARD;
    private static final Color ON_SURFACE    = NotionTheme.TEXT;
    private static final Color ON_SURF_VAR   = NotionTheme.TEXT_MUTED;
    private static final Color OUTLINE       = NotionTheme.BORDER;
    private static final Color ROW_ALT       = NotionTheme.PAGE;
    private static final Color ROW_HOVER     = NotionTheme.ACCENT_SOFT;

    private static final Color STATUS_GREEN_BG  = AppColors.SUCCESS_LIGHT;
    private static final Color STATUS_GREEN_FG  = AppColors.SUCCESS_DARK;
    private static final Color STATUS_ORANGE_BG = AppColors.WARNING_LIGHT;
    private static final Color STATUS_ORANGE_FG = AppColors.WARNING;
    private static final Color STATUS_RED_BG    = AppColors.ERROR_LIGHT;
    private static final Color STATUS_RED_FG    = AppColors.ERROR_DARK;

    private static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font FONT_DESC    = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_BOLD    = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_MONO    = new Font("Consolas", Font.BOLD, 13);
    private static final Font FONT_BADGE   = new Font("Segoe UI", Font.BOLD, 11);
    private static final Font FONT_HEADER  = new Font("Segoe UI", Font.BOLD, 11);
    private static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_BTN     = new Font("Segoe UI", Font.BOLD, 13);

    // --- Hidden buttons for AppModule compliance ---
    private JButton btnSubmit;
    private JButton btnCancel;
    private JPanel  btnPanel;

    // --- Hover tracking ---
    private int hoveredRow = -1;

    public QuanLyNhanVienModule() {
        setLayout(new BorderLayout());
        setBackground(SURFACE);
        setBorder(new EmptyBorder(24, 32, 24, 32));

        btnSubmit = new JButton();
        btnCancel = new JButton();
        btnPanel = new JPanel();
        btnPanel.setVisible(false);

        buildUI();
        loadData();
    }

    // =================================================================
    //  BUILD UI
    // =================================================================

    private void buildUI() {
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);
        top.add(buildHeader());
        top.add(Box.createVerticalStrut(16));
        top.add(buildStatsRow());
        top.add(Box.createVerticalStrut(16));
        top.add(buildFilterCard());
        top.add(Box.createVerticalStrut(16));

        add(top, BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(24, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, NotionTheme.NAVY,
                        getWidth(), getHeight(), new Color(0x1A, 0xAE, 0x39));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                g2.setColor(AppColors.withAlpha(NotionTheme.MINT, 150));
                g2.fillOval(getWidth() - 205, -58, 220, 220);
                g2.setColor(AppColors.withAlpha(NotionTheme.PEACH, 125));
                g2.fillRoundRect(getWidth() - 335, 92, 170, 52, 28, 28);
                g2.setColor(AppColors.withAlpha(Color.WHITE, 52));
                g2.fillRoundRect(getWidth() - 260, 40, 92, 18, 18, 18);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(24, 28, 24, 28));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel lblEyebrow = new JLabel("WORKSPACE / NHÂN SỰ");
        lblEyebrow.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblEyebrow.setForeground(AppColors.withAlpha(Color.WHITE, 175));
        lblEyebrow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTitle = new JLabel("Quản lý nhân viên");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblDesc = new JLabel("Theo dõi hồ sơ, bộ phận và trạng thái làm việc trong một bảng điều khiển.");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDesc.setForeground(AppColors.withAlpha(Color.WHITE, 205));
        lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        left.add(lblEyebrow);
        left.add(Box.createVerticalStrut(8));
        left.add(lblTitle);
        left.add(Box.createVerticalStrut(8));
        left.add(lblDesc);

        btnAddNew = createPrimaryButton("+ Thêm nhân viên");
        btnAddNew.setPreferredSize(new Dimension(170, 42));
        btnAddNew.addActionListener(e -> openThemNhanVienDialog());

        JPanel rightWrapper = new JPanel(new GridBagLayout());
        rightWrapper.setOpaque(false);
        rightWrapper.add(btnAddNew);

        header.add(left, BorderLayout.CENTER);
        header.add(rightWrapper, BorderLayout.EAST);

        return header;
    }

    private JPanel buildStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));

        lblTotalCount = new JLabel("0");
        lblActiveCount = new JLabel("0");
        lblLeaveCount = new JLabel("0");
        lblAdminCount = new JLabel("0");

        row.add(buildStatCard("Kết quả hiện tại", lblTotalCount, NotionTheme.ACCENT, NotionTheme.ACCENT_SOFT));
        row.add(buildStatCard("Đang làm trong kết quả", lblActiveCount, AppColors.SUCCESS_DARK, NotionTheme.MINT));
        row.add(buildStatCard("Nghỉ phép trong kết quả", lblLeaveCount, AppColors.WARNING_DARK, NotionTheme.YELLOW));
        row.add(buildStatCard("Đã nghỉ trong kết quả", lblAdminCount, AppColors.ERROR_DARK, NotionTheme.ROSE));
        return row;
    }

    private JPanel buildStatCard(String label, JLabel value, Color accent, Color tint) {
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
        lbl.setForeground(ON_SURF_VAR);
        text.add(value);
        text.add(Box.createVerticalStrut(2));
        text.add(lbl);

        card.add(marker, BorderLayout.WEST);
        card.add(text, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildFilterCard() {
        JPanel card = new JPanel(new BorderLayout()) {
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
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(buildFilterBar(), BorderLayout.CENTER);
        NotionTheme.lockMaxWidthToPreferredHeight(card);
        // Handoff: card filter nhân viên lấy chiều cao tự nhiên sau khi add đủ nội dung, không hard-code.
        // Cảnh báo: nếu filter thêm/bớt dòng runtime, cần revalidate và khóa lại max height theo preferred.
        return card;
    }

    private JPanel buildTableCard() {
        // Card container with rounded border
        JPanel card = new JPanel(new BorderLayout()) {
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
        card.setBorder(new EmptyBorder(0, 0, 0, 0));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);
        tableHeader.setBorder(new EmptyBorder(18, 20, 14, 20));
        JLabel title = new JLabel("Danh sách nhân viên");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(ON_SURFACE);
        JLabel hint = new JLabel("Nhấp đúp vào dòng hoặc nút Chỉnh sửa để cập nhật hồ sơ");
        hint.setFont(FONT_SMALL);
        hint.setForeground(ON_SURF_VAR);
        tableHeader.add(title, BorderLayout.WEST);
        tableHeader.add(hint, BorderLayout.EAST);

        card.add(tableHeader, BorderLayout.NORTH);
        card.add(buildTableSection(), BorderLayout.CENTER);
        card.add(buildTableFooter(), BorderLayout.SOUTH);

        return card;
    }

    private JPanel buildFilterBar() {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(18, 22, 18, 22));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel title = new JLabel("Bộ lọc nhân viên");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(ON_SURFACE);
        JLabel subtitle = new JLabel("Thống kê phía trên thay đổi theo kết quả lọc bên dưới");
        subtitle.setFont(FONT_SMALL);
        subtitle.setForeground(ON_SURF_VAR);
        header.add(title, BorderLayout.WEST);
        header.add(subtitle, BorderLayout.EAST);
        wrapper.add(header);
        wrapper.add(Box.createVerticalStrut(12));

        JPanel searchRow = new JPanel(new BorderLayout(10, 0));
        searchRow.setOpaque(false);
        searchRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel searchBox = new JPanel(new BorderLayout(10, 0));
        searchBox.setOpaque(false);
        searchBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1, true),
                new EmptyBorder(9, 14, 9, 14)
        ));

        JLabel iconSearch = new JLabel(LineIcons.image(LineIcons.Name.SEARCH, 18, 18));
        // Handoff: search icon is generated by LineIcons directly, independent of SVG files.
        // Risk: keep search icon inside only real search fields, not filter controls.
        searchBox.add(iconSearch, BorderLayout.WEST);

        txtSearch = new JTextField();
        txtSearch.setFont(FONT_BODY);
        txtSearch.setOpaque(false);
        txtSearch.setBorder(BorderFactory.createEmptyBorder());
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm kiếm theo tên, mã nhân viên hoặc số điện thoại...");
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
        });
        searchBox.add(txtSearch, BorderLayout.CENTER);
        SearchFieldClearButton.install(searchBox, txtSearch, this::applyFilter);

        JButton btnReset = new JButton("Bỏ lọc");
        NotionTheme.styleSecondaryButton(btnReset);
        btnReset.setPreferredSize(new Dimension(104, 40));
        btnReset.addActionListener(e -> {
            cboVaiTro.setSelectedIndex(0);
            cboTrangThai.setSelectedIndex(0);
            cboGaLamViec.setSelectedIndex(0);
            applyFilter();
        });

        searchRow.add(searchBox, BorderLayout.CENTER);
        wrapper.add(searchRow);
        wrapper.add(Box.createVerticalStrut(12));

        cboVaiTro = createFilterCombo(new String[]{
                "Tất cả bộ phận",
                "Nhân viên quầy vé",
                "Điều phối",
                "Admin"
        });
        cboVaiTro.addActionListener(e -> applyFilter());

        cboTrangThai = createFilterCombo(new String[]{
                "Tất cả trạng thái",
                "Đang làm",
                "Nghỉ phép",
                "Đã nghỉ"
        });
        cboTrangThai.addActionListener(e -> applyFilter());

        cboGaLamViec = createFilterCombo(loadGaFilterItems());
        cboGaLamViec.addActionListener(e -> applyFilter());

        JPanel optionRow = new JPanel(new GridBagLayout());
        optionRow.setOpaque(false);
        optionRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        GridBagConstraints filterGbc = new GridBagConstraints();
        filterGbc.gridy = 0;
        filterGbc.fill = GridBagConstraints.HORIZONTAL;
        filterGbc.anchor = GridBagConstraints.NORTHWEST;
        filterGbc.weightx = 1.0;
        filterGbc.insets = new Insets(0, 0, 0, 12);
        filterGbc.gridx = 0;
        optionRow.add(createFilterGroup("Bộ phận", cboVaiTro), filterGbc);
        filterGbc.gridx = 1;
        optionRow.add(createFilterGroup("Trạng thái", cboTrangThai), filterGbc);
        filterGbc.gridx = 2;
        optionRow.add(createFilterGroup("Ga làm việc", cboGaLamViec), filterGbc);
        filterGbc.gridx = 3;
        filterGbc.weightx = 0.0;
        filterGbc.insets = new Insets(0, 0, 0, 0);
        optionRow.add(FilterActionGroup.wrap(btnReset), filterGbc);
        // Handoff: filter row dùng grid để bộ phận/trạng thái/ga/nút không bị lệch.
        // Cảnh báo: search clear X vẫn là cơ chế xóa riêng của thanh tìm kiếm.
        wrapper.add(optionRow);

        return wrapper;
    }

    private JPanel createFilterGroup(String label, JComponent input) {
        JPanel group = new JPanel(new BorderLayout(0, 6));
        group.setOpaque(false);
        JLabel lbl = createFilterLabel(label);
        group.add(lbl, BorderLayout.NORTH);
        group.add(input, BorderLayout.CENTER);
        return group;
        // Handoff: label đặt trên field để filter grid không bị cắt trong menu full window.
        // Cảnh báo: reset filter không clear search, search có nút X riêng trong field.
    }

    private JScrollPane buildTableSection() {
        tableModel = new NhanVienTableModel();
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

        // Row hover effect
        table.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row != hoveredRow) {
                    hoveredRow = row;
                    table.repaint();
                }
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                hoveredRow = -1;
                table.repaint();
            }
        });

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
                return lbl;
            }
        });

        // Column widths & renderers
        TableColumnModel colModel = table.getColumnModel();
        int[] widths = {80, 200, 140, 120, 130, 90};
        for (int i = 0; i < widths.length; i++) {
            colModel.getColumn(i).setPreferredWidth(widths[i]);
        }

        // Col 0: Ma NV (khong cho sua)
        colModel.getColumn(0).setCellRenderer(new RowCellRenderer(FONT_MONO, PRIMARY, SwingConstants.LEFT));
        // Col 1: Ho ten
        colModel.getColumn(1).setCellRenderer(new RowCellRenderer(FONT_BOLD, ON_SURFACE, SwingConstants.LEFT));
        // Col 2: Bo phan
        colModel.getColumn(2).setCellRenderer(new RowCellRenderer(FONT_BODY, ON_SURF_VAR, SwingConstants.LEFT));
        // Col 3: SDT
        colModel.getColumn(3).setCellRenderer(new RowCellRenderer(FONT_BODY, ON_SURF_VAR, SwingConstants.LEFT));
        // Col 4: Trang thai (badge)
        colModel.getColumn(4).setCellRenderer(new BadgeCellRenderer());
        // Col 5: Edit button
        colModel.getColumn(5).setCellRenderer(new EditButtonRenderer());
        colModel.getColumn(5).setCellEditor(new EditButtonEditor());

        // Double-click row → open edit dialog; single-click button col → same
        table.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { showNhanVienQuickActions(e); }
            @Override public void mouseReleased(MouseEvent e) { showNhanVienQuickActions(e); }

            @Override
            public void mouseClicked(MouseEvent e) {
                int viewRow = table.rowAtPoint(e.getPoint());
                if (viewRow < 0) return;
                int col = table.columnAtPoint(e.getPoint());
                int modelRow = table.convertRowIndexToModel(viewRow);
                NhanVien nv = tableModel.getNhanVienAt(modelRow);
                if (nv != null && (col == 5 || e.getClickCount() == 2)) {
                    onEditNhanVien(nv);
                }
            }
        });

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
    //  HELPER: create styled components
    // =================================================================

    private JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(PRIMARY.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(PRIMARY.brighter());
                } else {
                    g2.setColor(PRIMARY);
                }
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BTN);
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private String[] loadGaFilterItems() {
        List<String[]> gaList = new DAO_NhanVien().getAllGa();
        List<String> labels = new ArrayList<>();
        List<String> keys = new ArrayList<>();
        labels.add("Tất cả ga làm việc");
        keys.add(null);
        for (String[] ga : gaList) {
            if (ga == null || ga.length == 0 || ga[0] == null || ga[0].isBlank()) continue;
            String tenGa = ga.length > 1 && ga[1] != null && !ga[1].isBlank() ? ga[1] : ga[0];
            labels.add(tenGa);
            keys.add(ga[0]);
        }
        gaFilterKeys = keys.toArray(new String[0]);
        return labels.toArray(new String[0]);
        // Handoff: combo hiển thị tên ga nhưng filter theo maGa để không phụ thuộc text UI.
        // Risk: nếu danh sách ga thay đổi runtime, mở lại module hoặc reload filter để cập nhật labels.
    }
    private JComboBox<String> createFilterCombo(String[] items) {
        JComboBox<String> cbo = new JComboBox<>(items);
        cbo.setFont(FONT_BODY);
        cbo.setPreferredSize(new Dimension(180, 38));
        cbo.setBackground(NotionTheme.CARD);
        cbo.setBorder(BorderFactory.createLineBorder(OUTLINE, 1, true));
        NotionTheme.applyComboBoxSelection(cbo);
        return cbo;
        // Handoff: combo nhân viên đồng bộ kích thước/border với Quản lý khách hàng.
        // Cảnh báo: không setMaximumSize để GridBag có thể co giãn trong MenuModule.
    }

    private JLabel createFilterLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_HEADER);
        lbl.setForeground(ON_SURF_VAR);
        return lbl;
    }

    // =================================================================
    //  DATA
    // =================================================================

    private void openThemNhanVienDialog() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        NhanVienDialog dlg = NhanVienDialog.create(owner, this::loadData);
        dlg.setVisible(true);
    }

    private void loadData() {
        SwingWorker<List<NhanVien>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<NhanVien> doInBackground() {
                return new DAO_NhanVien().getAll();
            }

            @Override
            protected void done() {
                try {
                    allData = get();
                } catch (Exception e) {
                    allData = new ArrayList<>();
                }
                applyFilter();
            }
        };
        worker.execute();
    }

    // =================================================================
    //  FILTER
    // =================================================================

    private void updateStats(List<NhanVien> source) {
        if (lblTotalCount == null) return;
        long active = source.stream().filter(nv -> nv.getTrangThai() == TrangThaiNhanVien.DANG_LAM).count();
        long leave = source.stream().filter(nv -> nv.getTrangThai() == TrangThaiNhanVien.NGHI_PHEP).count();
        long inactive = source.stream().filter(nv -> nv.getTrangThai() == TrangThaiNhanVien.DA_NGHI).count();
        lblTotalCount.setText(String.valueOf(source.size()));
        lblActiveCount.setText(String.valueOf(active));
        lblLeaveCount.setText(String.valueOf(leave));
        lblAdminCount.setText(String.valueOf(inactive));
    }

    private String selectedGaFilterKey() {
        if (cboGaLamViec == null || gaFilterKeys == null) return null;
        int index = cboGaLamViec.getSelectedIndex();
        return index > 0 && index < gaFilterKeys.length ? gaFilterKeys[index] : null;
    }
    private void applyFilter() {
        String keyword = txtSearch.getText().trim().toLowerCase();
        int vaiTroIdx = cboVaiTro.getSelectedIndex();
        int trangThaiIdx = cboTrangThai.getSelectedIndex();
        String selectedMaGa = selectedGaFilterKey();

        filteredData = new ArrayList<>();
        for (NhanVien nv : allData) {
            boolean matchKw = keyword.isEmpty()
                    || nv.getMaNV().toLowerCase().contains(keyword)
                    || nv.getHoTen().toLowerCase().contains(keyword)
                    || (nv.getSoDienThoai() != null && nv.getSoDienThoai().contains(keyword));

            boolean matchVt = vaiTroIdx == 0 || (nv.getVaiTro() != null && switch (vaiTroIdx) {
                case 1 -> nv.getVaiTro() == VaiTro.BAN_VE;
                case 2 -> nv.getVaiTro() == VaiTro.DIEU_PHOI;
                case 3 -> nv.getVaiTro() == VaiTro.ADMIN;
                default -> true;
            });

            boolean matchTt = trangThaiIdx == 0 || (nv.getTrangThai() != null && switch (trangThaiIdx) {
                case 1 -> nv.getTrangThai() == TrangThaiNhanVien.DANG_LAM;
                case 2 -> nv.getTrangThai() == TrangThaiNhanVien.NGHI_PHEP;
                case 3 -> nv.getTrangThai() == TrangThaiNhanVien.DA_NGHI;
                default -> true;
            });

            boolean matchGa = selectedMaGa == null || selectedMaGa.equals(nv.getMaGaLamViec());

            if (matchKw && matchVt && matchTt && matchGa) {
                filteredData.add(nv);
            }
        }

        totalRecords = filteredData.size();
        updateStats(filteredData);
        refreshTable();
    }

    // =================================================================
    //  PAGINATION
    // =================================================================

    private void refreshTable() {
        tableModel.setData(filteredData);
        lblPageInfo.setText(totalRecords == 0
                ? "Không tìm thấy bản ghi nào"
                : "Hiển thị " + totalRecords + " bản ghi");
    }


                // =================================================================
    //  TABLE MODEL
    // =================================================================

    private class NhanVienTableModel extends AbstractTableModel {
        private final String[] COLUMNS = {"Mã NV", "Họ và tên", "Bộ phận", "SĐT", "Trạng thái", ""};
        private List<NhanVien> data = new ArrayList<>();

        void setData(List<NhanVien> data) {
            this.data = new ArrayList<>(data);
            fireTableDataChanged();
        }

        @Override public int getRowCount()    { return data.size(); }
        @Override public int getColumnCount() { return COLUMNS.length; }
        @Override public String getColumnName(int c) { return COLUMNS[c]; }

        @Override
        public boolean isCellEditable(int r, int c) {
            return false; // all edits go through NhanVienDialog edit mode
        }

        @Override
        public Object getValueAt(int r, int c) {
            NhanVien nv = data.get(r);
            return switch (c) {
                case 0 -> nv.getMaNV();
                case 1 -> nv.getHoTen();
                case 2 -> nv.getVaiTro() != null ? nv.getVaiTro() : VaiTro.BAN_VE;
                case 3 -> nv.getSoDienThoai() != null ? nv.getSoDienThoai() : "";
                case 4 -> nv.getTrangThai() != null ? nv.getTrangThai() : TrangThaiNhanVien.DANG_LAM;
                case 5 -> "Chỉnh sửa";
                default -> "";
            };
        }

        @Override
        public void setValueAt(Object value, int r, int c) {
            // no-op — all edits go through NhanVienDialog edit mode
        }

        NhanVien getNhanVienAt(int r) {
            return (r >= 0 && r < data.size()) ? data.get(r) : null;
        }
    }

    // =================================================================
    //  CELL RENDERERS
    // =================================================================

    /** Generic row renderer with hover + zebra stripe */
    private class RowCellRenderer extends DefaultTableCellRenderer {
        private final Font font;
        private final Color fg;

        RowCellRenderer(Font font, Color fg, int align) {
            this.font = font;
            this.fg = fg;
            setHorizontalAlignment(align);
        }

        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object value,
                boolean isSel, boolean hasFocus, int row, int col) {
            super.getTableCellRendererComponent(tbl, value, isSel, hasFocus, row, col);
            setFont(font);
            setForeground(fg);
            setBorder(new EmptyBorder(0, 20, 0, 8));
            setBackground(getRowBg(tbl, isSel, row));
            return this;
        }
    }

    /** Badge renderer for TrangThaiNhanVien */
    private class BadgeCellRenderer extends JPanel implements TableCellRenderer {
        private final JLabel badge = new JLabel();
        private Color badgeBg = OUTLINE;
        private Color badgeFg = ON_SURF_VAR;
        private static final int BADGE_HEIGHT = 24;
        private static final int BADGE_MARGIN_X = 28;

        BadgeCellRenderer() {
            setLayout(new BorderLayout());
            setOpaque(true);
            setBorder(new EmptyBorder(0, BADGE_MARGIN_X, 0, BADGE_MARGIN_X));
            badge.setFont(FONT_BADGE);
            badge.setHorizontalAlignment(SwingConstants.CENTER);
            badge.setOpaque(false);
            badge.setPreferredSize(new Dimension(10, BADGE_HEIGHT));
            add(badge, BorderLayout.CENTER);
            // Handoff: badge trạng thái nhân viên bám gần full ô để tránh tag dài/ngắn lệch nhịp.
            // Nếu thêm trạng thái dài hơn, ưu tiên nới cột hoặc giảm BADGE_MARGIN_X.
        }

        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object value,
                boolean isSel, boolean hasFocus, int row, int col) {
            TrangThaiNhanVien tt = (value instanceof TrangThaiNhanVien) ? (TrangThaiNhanVien) value : TrangThaiNhanVien.DANG_LAM;

            switch (tt) {
                case DANG_LAM  -> { badgeBg = NotionTheme.MINT;   badgeFg = AppColors.SUCCESS_DARK; }
                case NGHI_PHEP -> { badgeBg = NotionTheme.YELLOW; badgeFg = AppColors.WARNING_DARK; }
                case DA_NGHI   -> { badgeBg = NotionTheme.ROSE;   badgeFg = AppColors.ERROR_DARK; }
            }

            badge.setText(tt.toString());
            badge.setForeground(badgeFg);
            setBackground(getRowBg(tbl, isSel, row));
            return this;
        }

        @Override
        protected void paintChildren(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Rectangle r = badge.getBounds();
            int x = r.x;
            int y = r.y + Math.max(0, (r.height - BADGE_HEIGHT) / 2);
            int w = r.width;
            int h = BADGE_HEIGHT;
            // Badge là hình chữ nhật bo góc nhẹ, không dùng pill tròn để giống property tag kiểu Notion.
            // Giữ padding nhỏ để renderer không làm tăng chiều cao row; nếu font đổi cần kiểm tra lại bounds.
            g2.setColor(badgeBg);
            g2.fillRoundRect(x, y, w, h, 8, 8);
            g2.setColor(AppColors.withAlpha(badgeFg, 65));
            g2.drawRoundRect(x, y, w - 1, h - 1, 8, 8);
            g2.dispose();

            super.paintChildren(g);
        }
    }

    /** Edit button renderer */
    private class EditButtonRenderer extends JPanel implements TableCellRenderer {
        private final JLabel lbl = new JLabel();

        EditButtonRenderer() {
            setLayout(new GridBagLayout());
            setOpaque(true);
            lbl.setFont(FONT_BADGE);
            lbl.setForeground(PRIMARY);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setOpaque(false);
            lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            add(lbl);
        }

        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object value,
                boolean isSel, boolean hasFocus, int row, int col) {
            lbl.setText(value != null ? value.toString() : "Chỉnh sửa");
            setBackground(getRowBg(tbl, isSel, row));
            return this;
        }

        @Override
        protected void paintChildren(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Rectangle r = lbl.getBounds();
            int px = 12, py = 4;
            g2.setColor(PRIMARY_LIGHT);
            g2.fillRoundRect(r.x - px, r.y - py, r.width + 2 * px, r.height + 2 * py, 10, 10);
            g2.setColor(new Color(PRIMARY.getRed(), PRIMARY.getGreen(), PRIMARY.getBlue(), 60));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(r.x - px, r.y - py, r.width + 2 * px, r.height + 2 * py, 10, 10);
            g2.dispose();

            super.paintChildren(g);
        }
    }

    /** Edit button editor (handles clicks) — only triggers when clicking the button area */
    private class EditButtonEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel;
        private final JButton button;
        private int editingRow;

        EditButtonEditor() {
            panel = new JPanel(new GridBagLayout());
            panel.setOpaque(true);

            button = new JButton("Chỉnh sửa");
            button.setFont(FONT_BADGE);
            button.setForeground(AppColors.ACTION_SOFT_FG);
            button.setBackground(AppColors.ACTION_SOFT_BG);
            button.setBorderPainted(false);
            button.setFocusPainted(false);
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            button.setPreferredSize(new Dimension(80, 28));

            button.addActionListener(e -> {
                fireEditingStopped();
                NhanVien nv = tableModel.getNhanVienAt(editingRow);
                if (nv != null) onEditNhanVien(nv);
            });

            panel.add(button);
        }

        @Override
        public Component getTableCellEditorComponent(JTable tbl, Object value,
                boolean isSel, int row, int col) {
            editingRow = row;
            panel.setBackground(getRowBg(tbl, true, row));
            return panel;
        }

        @Override
        public boolean isCellEditable(java.util.EventObject e) {
            if (e instanceof MouseEvent) {
                MouseEvent me = (MouseEvent) e;
                JTable tbl = (JTable) me.getSource();
                int col = tbl.columnAtPoint(me.getPoint());
                int row = tbl.rowAtPoint(me.getPoint());
                if (col < 0 || row < 0) return false;
                Rectangle cellRect = tbl.getCellRect(row, col, false);
                int clickX = me.getX() - cellRect.x;
                int clickY = me.getY() - cellRect.y;
                int btnW = button.getPreferredSize().width;
                int btnH = button.getPreferredSize().height;
                int btnX = (cellRect.width - btnW) / 2;
                int btnY = (cellRect.height - btnH) / 2;
                return clickX >= btnX && clickX <= btnX + btnW
                    && clickY >= btnY && clickY <= btnY + btnH;
            }
            return false;
        }

        @Override
        public Object getCellEditorValue() { return "Chỉnh sửa"; }
    }

    /** Row background helper: hover > selected > zebra */
    private Color getRowBg(JTable tbl, boolean isSel, int row) {
        if (isSel) return NotionTheme.TABLE_SELECTION;
        if (row == hoveredRow) return ROW_HOVER;
        return row % 2 == 0 ? CARD_BG : ROW_ALT;
    }

    // =================================================================
    //  EDIT ACTION (placeholder)
    // =================================================================

    private void showNhanVienQuickActions(MouseEvent e) {
        if (!e.isPopupTrigger()) return;
        int viewRow = table.rowAtPoint(e.getPoint());
        if (viewRow < 0) return;
        table.setRowSelectionInterval(viewRow, viewRow);
        NhanVien nv = tableModel.getNhanVienAt(table.convertRowIndexToModel(viewRow));
        if (nv == null) return;
        JPopupMenu menu = new JPopupMenu();
        JMenuItem edit = new JMenuItem("Sửa");
        edit.addActionListener(ev -> onEditNhanVien(nv));
        menu.add(edit);
        menu.show(table, e.getX(), e.getY());
    }

    private void onEditNhanVien(NhanVien nv) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        NhanVienDialog dlg = NhanVienDialog.edit(owner, nv, this::loadData);
        dlg.setVisible(true);
    }

    // =================================================================
    //  AppModule interface
    // =================================================================

    @Override public String getTitle() { return "Quản lý nhân viên"; }
    @Override public JPanel getView()  { return this; }
    @Override public void setOnResult(Consumer<Object> cb) {
        this.callback = cb;
        boolean has = (cb != null);
        btnSubmit.setVisible(has);
        btnCancel.setVisible(has);
        btnPanel.setVisible(has);
    }
    @Override public void reset() {
        txtSearch.setText("");
        cboVaiTro.setSelectedIndex(0);
        cboTrangThai.setSelectedIndex(0);
        cboGaLamViec.setSelectedIndex(0);
        loadData();
    }
}




