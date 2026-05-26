package com.modules;

import com.dao.DAO_Gia;
import com.entity.Gia;
import com.toedter.calendar.JCalendar;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.CardLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

public class QuanLyGiaModule extends JPanel implements AppModule {

    private Consumer<Object> callback;

    // --- UI components ---
    private JTextField        txtSearchMaGia;
    private DatePickerField   dpTuNgay;
    private DatePickerField   dpDenNgay;
    private JComboBox<String> cboTrangThai;
    private JButton           btnAddNew;
    private JTable            table;
    private GiaTableModel     tableModel;

    // --- Pagination ---
    private int totalRecords = 0;

    private JLabel  lblPageInfo;
    private JLabel  lblTableMeta;

    // --- Data ---
    private List<Gia> allData      = new ArrayList<>();
    private List<Gia> filteredData = new ArrayList<>();

    // --- Design tokens (same as QuanLyNhanVienModule) ---
    private static final Color PRIMARY       = NotionTheme.ACCENT;
    private static final Color PRIMARY_LIGHT = NotionTheme.ACCENT_SOFT;
    private static final Color SURFACE       = NotionTheme.PAGE;
    private static final Color CARD_BG       = NotionTheme.CARD;
    private static final Color ON_SURFACE    = NotionTheme.TEXT;
    private static final Color ON_SURF_VAR   = NotionTheme.TEXT_MUTED;
    private static final Color OUTLINE       = NotionTheme.BORDER;
    private static final Color TABLE_HEAD_BG = NotionTheme.PAGE;
    private static final Color TABLE_HEAD_FG = NotionTheme.TEXT_MUTED;
    private static final Color TABLE_DIVIDER = NotionTheme.ACCENT_SOFT;
    private static final Color ROW_ALT       = NotionTheme.PAGE;
    private static final Color ROW_HOVER     = NotionTheme.ACCENT_SOFT;

    private static final Color STATUS_GREEN_BG  = AppColors.SUCCESS_LIGHT;
    private static final Color STATUS_GREEN_FG  = AppColors.SUCCESS_DARK;
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

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // --- Hidden buttons for AppModule compliance ---
    private JButton btnSubmit;
    private JButton btnCancel;
    private JPanel  btnPanel;

    // --- Hover tracking ---
    private int hoveredRow = -1;

    // --- Card layout for panel-swap ---
    private CardLayout rootCard;
    private JPanel     listView;
    private JPanel     editContainer;

    public QuanLyGiaModule() {
        rootCard = new CardLayout();
        setLayout(rootCard);
        setBackground(SURFACE);

        btnSubmit = new JButton();
        btnCancel = new JButton();
        btnPanel = new JPanel();
        btnPanel.setVisible(false);

        listView = new JPanel(new BorderLayout());
        listView.setBackground(SURFACE);
        listView.setBorder(new EmptyBorder(28, 36, 28, 36));

        editContainer = new JPanel(new BorderLayout());
        editContainer.setBackground(SURFACE);

        add(listView, "LIST");
        add(editContainer, "EDIT");

        buildUI();
        loadData();
        rootCard.show(this, "LIST");
    }

    // =================================================================
    //  BUILD UI
    // =================================================================

    private void buildUI() {
        listView.add(buildHeader(), BorderLayout.NORTH);
        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setOpaque(false);
        content.add(buildFilterCard(), BorderLayout.NORTH);
        content.add(buildTableCard(), BorderLayout.CENTER);
        listView.add(content, BorderLayout.CENTER);
    }

    private JPanel buildFilterCard() {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                NotionTheme.paintCard(g2, this, CARD_BG, OUTLINE, 16);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.add(buildFilterBar(), BorderLayout.CENTER);
        return card;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(24, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, NotionTheme.NAVY, getWidth(), getHeight(), new Color(0xDD, 0x5B, 0x00));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                g2.setColor(AppColors.withAlpha(NotionTheme.YELLOW, 135));
                g2.fillRoundRect(getWidth() - 188, 18, 132, 132, 32, 32);
                g2.setColor(AppColors.withAlpha(NotionTheme.SKY, 105));
                g2.fillOval(getWidth() - 300, 92, 132, 132);
                g2.setColor(AppColors.withAlpha(Color.WHITE, 44));
                g2.fillRoundRect(getWidth() - 260, 40, 118, 14, 14, 14);
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
        JLabel lblTitle = new JLabel("Thiết lập biểu giá");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblTitle.setForeground(Color.WHITE);
        JLabel lblDesc = new JLabel("Cấu hình và tìm kiếm thông tin giá vé trên toàn hệ thống.");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblDesc.setForeground(AppColors.withAlpha(Color.WHITE, 210));

        left.add(eyebrow);
        left.add(Box.createVerticalStrut(8));
        left.add(lblTitle);
        left.add(Box.createVerticalStrut(8));
        left.add(lblDesc);

        btnAddNew = createPrimaryButton("+ Thêm giá mới");
        btnAddNew.setPreferredSize(new Dimension(150, 42));
        btnAddNew.addActionListener(e -> openGiaDialog());

        JPanel rightWrapper = new JPanel(new GridBagLayout());
        rightWrapper.setOpaque(false);
        rightWrapper.add(btnAddNew);

        header.add(left, BorderLayout.CENTER);
        header.add(rightWrapper, BorderLayout.EAST);
        return header;
    }

    private JPanel buildTableCard() {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                NotionTheme.paintCard(g2, this, CARD_BG, OUTLINE, 16);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(0, 0, 0, 0));

        card.add(buildTableTopBar(), BorderLayout.NORTH);
        card.add(buildTableSection(), BorderLayout.CENTER);
        card.add(buildTableFooter(), BorderLayout.SOUTH);

        return card;
    }

    private JPanel buildTableTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, TABLE_DIVIDER),
                new EmptyBorder(14, 20, 12, 20)
        ));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel title = new JLabel("Danh sách biểu giá");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(ON_SURFACE);

        JLabel desc = new JLabel("Theo dõi trạng thái áp dụng và thời gian hiệu lực của từng biểu giá.");
        desc.setFont(FONT_SMALL);
        desc.setForeground(ON_SURF_VAR);

        left.add(title);
        left.add(Box.createVerticalStrut(2));
        left.add(desc);

        lblTableMeta = new JLabel("0 bản ghi");
        lblTableMeta.setFont(FONT_BADGE);
        lblTableMeta.setForeground(PRIMARY);
        lblTableMeta.setBorder(new EmptyBorder(6, 12, 6, 12));
        lblTableMeta.setOpaque(true);
        lblTableMeta.setBackground(NotionTheme.ACCENT_SOFT);

        bar.add(left, BorderLayout.WEST);
        bar.add(lblTableMeta, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildFilterBar() {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setOpaque(false);

        // --- UNIFIED Boxed Container ---
        JPanel bgPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                NotionTheme.paintCard(g2, this, CARD_BG, OUTLINE, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        bgPanel.setOpaque(false);
        bgPanel.setBorder(new EmptyBorder(16, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(0, 0, 12, 0);

        // --- ROW 1: SEARCH ---
        JPanel searchRow = new JPanel(new GridBagLayout());
        searchRow.setOpaque(false);
        GridBagConstraints sgbc = new GridBagConstraints();
        sgbc.insets = new Insets(0, 0, 0, 12);
        
        txtSearchMaGia = new JTextField();
        txtSearchMaGia.setFont(FONT_BODY);
        txtSearchMaGia.setOpaque(false);
        txtSearchMaGia.setBorder(BorderFactory.createEmptyBorder());
        txtSearchMaGia.putClientProperty("JTextField.placeholderText", "Tìm kiếm theo mã giá, mô tả...");
        JPanel searchBox = new JPanel(new BorderLayout(10, 0));
        searchBox.setOpaque(false);
        searchBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        JLabel iconSearch = new JLabel(LineIcons.image(LineIcons.Name.SEARCH, 18, 18));
        searchBox.add(iconSearch, BorderLayout.WEST);
        searchBox.add(txtSearchMaGia, BorderLayout.CENTER);
        SearchFieldClearButton.install(searchBox, txtSearchMaGia, this::applyFilter);
        txtSearchMaGia.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
        });
        sgbc.weightx = 1.0;
        sgbc.fill = GridBagConstraints.HORIZONTAL;
        searchRow.add(searchBox, sgbc);

        JButton btnReset = new JButton("Bỏ lọc");
        NotionTheme.styleSecondaryButton(btnReset);
        btnReset.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnReset.setPreferredSize(new Dimension(104, 40));
        btnReset.addActionListener(e -> {
            cboTrangThai.setSelectedIndex(0);
            dpTuNgay.setValue(null);
            dpDenNgay.setValue(null);
            applyFilter();
        });
        // Handoff: dùng style nút phụ chung để text Bỏ lọc đồng bộ với các module quản lý khác.
        // Cảnh báo: nút này chỉ clear filter ngày/trạng thái, không xóa thanh tìm kiếm.

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0;
        bgPanel.add(searchRow, gbc);

        // --- ROW 2: FILTERS ---
        JPanel filterRow = new JPanel(new GridBagLayout());
        filterRow.setOpaque(false);
        GridBagConstraints fgbc = new GridBagConstraints();
        fgbc.gridy = 0; fgbc.fill = GridBagConstraints.HORIZONTAL; fgbc.weightx = 1.0; fgbc.insets = new Insets(0, 0, 0, 12);

        dpTuNgay = new DatePickerField();
        dpTuNgay.addPropertyChangeListener("value", e -> applyFilter());
        filterRow.add(buildFilterGroupGia("TỪ NGÀY", dpTuNgay), fgbc);

        dpDenNgay = new DatePickerField();
        dpDenNgay.addPropertyChangeListener("value", e -> applyFilter());
        fgbc.gridx = 1;
        filterRow.add(buildFilterGroupGia("ĐẾN NGÀY", dpDenNgay), fgbc);

        cboTrangThai = createFilterCombo(new String[]{"Tất cả trạng thái", "Đang áp dụng", "Ngừng áp dụng"});
        cboTrangThai.addActionListener(e -> applyFilter());
        fgbc.gridx = 2; fgbc.insets = new Insets(0, 0, 0, 0);
        filterRow.add(buildFilterGroupGia("TRẠNG THÁI", cboTrangThai), fgbc);
        fgbc.gridx = 3; fgbc.weightx = 0; fgbc.fill = GridBagConstraints.NONE; fgbc.insets = new Insets(0, 12, 0, 0);
        filterRow.add(FilterActionGroup.wrap(btnReset), fgbc);

        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 0, 0);
        bgPanel.add(filterRow, gbc);

        wrapper.add(bgPanel);
        wrapper.setBorder(new EmptyBorder(16, 20, 16, 20));
        return wrapper;
    }

    private JPanel buildFilterGroupGia(String labelText, JComponent field) {
        JPanel group = new JPanel();
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
        group.setOpaque(false);

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(ON_SURF_VAR);
        lbl.setBorder(new EmptyBorder(0, 4, 4, 0));

        int h = Math.max(40, field.getPreferredSize().height);
        field.setPreferredSize(new Dimension(0, h));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        group.add(lbl);
        group.add(field);
        return group;
    }

    /** Plain text field for date input with watermark placeholder. */
    private JTextField createPlainDateField() {
        JTextField f = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !hasFocus()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setColor(NotionTheme.BORDER);
                    g2.setFont(FONT_SMALL);
                    Insets ins = getInsets();
                    g2.drawString("dd/MM/yyyy", ins.left + 2, getHeight() / 2 + 4);
                    g2.dispose();
                }
            }
        };
        f.setFont(FONT_BODY);
        f.setPreferredSize(new Dimension(120, 38));
        f.setMaximumSize(new Dimension(120, 38));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1),
                new EmptyBorder(4, 10, 4, 10)
        ));
        f.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) { f.repaint(); }
            public void focusLost(java.awt.event.FocusEvent e)   { f.repaint(); applyFilter(); }
        });
        return f;
    }

    /** Wraps a date text field + a calendar popup button into one panel. */
    private JPanel createDateInputPanel(JTextField txtField) {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(158, 38));
        panel.setPreferredSize(new Dimension(158, 38));

        JButton btnCal = new JButton("▼") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isRollover() ? PRIMARY_LIGHT : NotionTheme.PAGE;
                g2.setColor(bg);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(OUTLINE);
                g2.drawLine(0, 0, 0, getHeight() - 1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnCal.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        btnCal.setForeground(ON_SURF_VAR);
        btnCal.setContentAreaFilled(false);
        btnCal.setBorderPainted(false);
        btnCal.setFocusPainted(false);
        btnCal.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCal.setPreferredSize(new Dimension(30, 38));
        btnCal.addActionListener(e -> showDatePickerPopup(btnCal, txtField));

        panel.add(txtField, BorderLayout.CENTER);
        panel.add(btnCal, BorderLayout.EAST);
        return panel;
    }

    /** Opens a popup calendar near the anchor. */
    private void showDatePickerPopup(Component anchor, JTextField target) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog popup = new JDialog(owner, Dialog.ModalityType.MODELESS);
        popup.setUndecorated(true);
        popup.addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            @Override public void windowGainedFocus(java.awt.event.WindowEvent e) {}
            @Override public void windowLostFocus(java.awt.event.WindowEvent e)   { popup.dispose(); }
        });

        // Pre-select existing date if present
        LocalDate existing = parseDateField(target);
        Calendar cal = Calendar.getInstance();
        if (existing != null) {
            cal.set(existing.getYear(), existing.getMonthValue() - 1, existing.getDayOfMonth());
        }

        // Calendar widget
        JCalendar jCal = new JCalendar();
        jCal.setDate(cal.getTime());
        jCal.setWeekOfYearVisible(false);

        // Buttons
        JButton btnOk = new JButton("Chọn") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? PRIMARY.darker() : PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnOk.setFont(FONT_BTN);
        btnOk.setForeground(Color.WHITE);
        btnOk.setContentAreaFilled(false);
        btnOk.setBorderPainted(false);
        btnOk.setFocusPainted(false);
        btnOk.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnOk.setPreferredSize(new Dimension(80, 32));

        JButton btnClear = new JButton("Xóa") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(AppColors.ERROR_LIGHT);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnClear.setFont(FONT_BTN);
        btnClear.setForeground(AppColors.ERROR_DARK);
        btnClear.setContentAreaFilled(false);
        btnClear.setBorderPainted(false);
        btnClear.setFocusPainted(false);
        btnClear.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClear.setPreferredSize(new Dimension(60, 32));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.add(btnClear);
        btnPanel.add(btnOk);

        // Assemble popup
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(NotionTheme.BORDER, 1),
                new EmptyBorder(4, 4, 0, 4)
        ));
        root.add(jCal, BorderLayout.CENTER);
        root.add(btnPanel, BorderLayout.SOUTH);

        popup.setContentPane(root);

        // Button actions
        btnOk.addActionListener(e -> {
            Date selectedDate = jCal.getDate();
            Calendar c = Calendar.getInstance();
            c.setTime(selectedDate);
            LocalDate ld = LocalDate.of(
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
            target.setText(ld.format(DT_FMT));
            target.setForeground(ON_SURFACE);
            popup.dispose();
            applyFilter();
        });

        btnClear.addActionListener(e -> {
            target.setText("");
            popup.dispose();
            applyFilter();
        });

        // Position near anchor
        popup.pack();
        try {
            Point anchorLoc = anchor.getLocationOnScreen();
            int px = anchorLoc.x;
            int py = anchorLoc.y + anchor.getHeight() + 2;
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            if (px + popup.getWidth()  > screen.width)  px = screen.width  - popup.getWidth();
            if (py + popup.getHeight() > screen.height) py = anchorLoc.y   - popup.getHeight() - 2;
            popup.setLocation(px, py);
        } catch (IllegalComponentStateException ex) {
            ModuleLauncher.centerDialog(popup, owner);
        }

        popup.setVisible(true);
    }

    private JScrollPane buildTableSection() {
        tableModel = new GiaTableModel();
        table = new JTable(tableModel);
        NotionTheme.styleTable(table);
        table.setRowHeight(60);
        table.setShowGrid(false);
        table.setShowHorizontalLines(false);
        table.setGridColor(TABLE_DIVIDER);
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

            @Override public void mousePressed(MouseEvent e) { showGiaQuickActions(e); }
            @Override public void mouseReleased(MouseEvent e) { showGiaQuickActions(e); }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e) || e.getClickCount() != 2) return;
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row < 0 || col == 5) return;
                Gia gia = tableModel.getGiaAt(row);
                if (gia != null) onEditGia(gia);
            }
        });
        // Handoff: thân dòng bảng giá mở bằng double-click; nút Chỉnh sửa vẫn single-click.
        // Cảnh báo: nếu bật sorter sau này, cần convertRowIndexToModel trước khi lấy dữ liệu.

        // Header style
        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setFont(FONT_HEADER);
                lbl.setForeground(TABLE_HEAD_FG);
                lbl.setBackground(TABLE_HEAD_BG);
                lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, TABLE_DIVIDER),
                        new EmptyBorder(0, column <= 1 ? 18 : 8, 0, 8)
                ));
                lbl.setHorizontalAlignment(column <= 1 ? SwingConstants.LEFT : SwingConstants.CENTER);
                lbl.setPreferredSize(new Dimension(lbl.getPreferredSize().width, 46));
                return lbl;
            }
        });

        // Column widths & renderers
        TableColumnModel colModel = table.getColumnModel();
        int[] widths = {115, 330, 170, 170, 145, 120};
        for (int i = 0; i < widths.length; i++) {
            colModel.getColumn(i).setPreferredWidth(widths[i]);
        }

        // Col 0: Ma gia
        colModel.getColumn(0).setCellRenderer(new RowCellRenderer(FONT_MONO, PRIMARY, SwingConstants.LEFT));
        // Col 1: Mo ta
        colModel.getColumn(1).setCellRenderer(new RowCellRenderer(FONT_BOLD, ON_SURFACE, SwingConstants.LEFT));
        // Col 2: Thoi gian bat dau
        colModel.getColumn(2).setCellRenderer(new RowCellRenderer(FONT_BODY, ON_SURF_VAR, SwingConstants.CENTER));
        // Col 3: Thoi gian ket thuc
        colModel.getColumn(3).setCellRenderer(new RowCellRenderer(FONT_BODY, ON_SURF_VAR, SwingConstants.CENTER));
        // Col 4: Trang thai (badge)
        colModel.getColumn(4).setCellRenderer(new BadgeCellRenderer());
        // Col 5: Thao tac button
        colModel.getColumn(5).setCellRenderer(new EditButtonRenderer());
        colModel.getColumn(5).setCellEditor(new EditButtonEditor());

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

    private JComboBox<String> createFilterCombo(String[] items) {
        JComboBox<String> cbo = new JComboBox<>(items);
        cbo.setFont(FONT_BODY);
        cbo.setEditable(false);
        cbo.setOpaque(true);
        cbo.setBackground(Color.WHITE);
        cbo.setBorder(BorderFactory.createLineBorder(OUTLINE, 1, true));
        cbo.setPreferredSize(new Dimension(0, 40));
        cbo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        cbo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                lbl.setBorder(new EmptyBorder(0, 10, 0, 10));
                return lbl;
            }
        });
        NotionTheme.applyComboBoxSelection(cbo);
        return cbo;
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


    private void showGiaQuickActions(MouseEvent e) {
        if (!e.isPopupTrigger()) return;
        int row = table.rowAtPoint(e.getPoint());
        if (row < 0) return;
        table.setRowSelectionInterval(row, row);
        Gia gia = tableModel.getGiaAt(row);
        if (gia == null) return;
        JPopupMenu menu = new JPopupMenu();
        JMenuItem edit = new JMenuItem("Sửa");
        edit.addActionListener(ev -> onEditGia(gia));
        JMenuItem toggle = new JMenuItem(gia.isTrangThai() ? "Ngừng áp dụng" : "Bật áp dụng");
        toggle.addActionListener(ev -> toggleGiaTrangThai(gia));
        menu.add(edit);
        menu.addSeparator();
        menu.add(toggle);
        menu.show(table, e.getX(), e.getY());
    }

    private void toggleGiaTrangThai(Gia gia) {
        DAO_Gia daoGia = new DAO_Gia();
        Gia updated = new Gia(gia.getMaGia(), gia.getThoiGianBatDau(), gia.getThoiGianKetThuc(), gia.getMoTa(), !gia.isTrangThai());
        List<String> conflictsToDeactivate = new ArrayList<>();
        if (updated.isTrangThai()) {
            List<Gia> conflicts = daoGia.findOverlappingActive(updated.getMaGia(), updated.getThoiGianBatDau(), updated.getThoiGianKetThuc());
            if (!conflicts.isEmpty()) {
                String ids = conflicts.stream().map(Gia::getMaGia).reduce((a, b) -> a + ", " + b).orElse("");
                int choice = NotionMessageDialog.showConfirmDialog(this,
                        "Kỳ giá muốn bật bị trùng thời gian với: " + ids + "\\n\\nBạn có muốn ngừng các kỳ giá trùng rồi bật kỳ này không?",
                        "Trùng kỳ giá", JOptionPane.WARNING_MESSAGE, "Hủy", "Ngừng kỳ trùng");
                if (choice != JOptionPane.YES_OPTION) return;
                conflictsToDeactivate = conflicts.stream().map(Gia::getMaGia).toList();
            }
        }
        boolean ok = daoGia.updateWithDeactivatedConflicts(updated, conflictsToDeactivate);
        if (ok) loadData();
        else NotionMessageDialog.showMessageDialog(this, "Không thể cập nhật trạng thái kỳ giá.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        // Handoff: quick toggle giá vẫn đi qua updateWithDeactivatedConflicts để giữ rule không trùng kỳ đang áp dụng.
        // Cảnh báo: chỉ đổi trạng thái, không clone vì clone chỉ dành cho sửa nội dung giá đã dùng trên vé.
    }

    private void openGiaDialog() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        GiaDialog dlg = new GiaDialog(owner, this::loadData);
        dlg.setVisible(true);
    }

    void loadData() {
        SwingWorker<List<Gia>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Gia> doInBackground() {
                return new DAO_Gia().getAll();
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

    private void applyFilter() {
        String keyword     = txtSearchMaGia.getText().trim().toLowerCase();
        int trangThaiIdx   = cboTrangThai.getSelectedIndex();
        LocalDate tuNgay  = dpTuNgay.getValue();
        LocalDate denNgay = dpDenNgay.getValue();

        filteredData = new ArrayList<>();
        for (Gia g : allData) {
            boolean matchKw = keyword.isEmpty()
                    || g.getMaGia().toLowerCase().contains(keyword)
                    || (g.getMoTa() != null && g.getMoTa().toLowerCase().contains(keyword));

            boolean matchTt = trangThaiIdx == 0
                    || (trangThaiIdx == 1 && g.isTrangThai())
                    || (trangThaiIdx == 2 && !g.isTrangThai());

            // Date range filter:
            // - only tuNgay  -> batDau >= tuNgay
            // - only denNgay -> ketThuc <= denNgay
            // - both         -> batDau >= tuNgay AND ketThuc <= denNgay
            boolean matchDate = true;
            if (tuNgay != null && denNgay != null) {
                matchDate = g.getThoiGianBatDau() != null && g.getThoiGianKetThuc() != null
                        && !g.getThoiGianBatDau().isBefore(tuNgay)
                        && !g.getThoiGianKetThuc().isAfter(denNgay);
            } else if (tuNgay != null) {
                matchDate = g.getThoiGianBatDau() != null
                        && !g.getThoiGianBatDau().isBefore(tuNgay);
            } else if (denNgay != null) {
                matchDate = g.getThoiGianKetThuc() != null
                        && !g.getThoiGianKetThuc().isAfter(denNgay);
            }

            if (matchKw && matchTt && matchDate) {
                filteredData.add(g);
            }
        }

        totalRecords = filteredData.size();
        refreshTable();
    }

    /** Parses the content of a date text field; returns null if empty or invalid. */
    private LocalDate parseDateField(JTextField f) {
        String text = f.getText().trim();
        if (text.isEmpty()) return null;
        try {
            return LocalDate.parse(text, DT_FMT);
        } catch (DateTimeParseException ex) {
            return null;
        }
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

    private class GiaTableModel extends AbstractTableModel {
        private final String[] COLUMNS = {"Mã giá", "Mô tả", "Thời gian áp dụng", "Thời gian kết thúc", "Trạng thái", ""};
        private List<Gia> data = new ArrayList<>();

        void setData(List<Gia> data) {
            this.data = new ArrayList<>(data);
            fireTableDataChanged();
        }

        @Override public int getRowCount()    { return data.size(); }
        @Override public int getColumnCount() { return COLUMNS.length; }
        @Override public String getColumnName(int c) { return COLUMNS[c]; }

        @Override
        public boolean isCellEditable(int r, int c) {
            return c == 5; // Only the action button column
        }

        @Override
        public Object getValueAt(int r, int c) {
            Gia g = data.get(r);
            return switch (c) {
                case 0 -> g.getMaGia();
                case 1 -> g.getMoTa() != null ? g.getMoTa() : "";
                case 2 -> g.getThoiGianBatDau() != null ? g.getThoiGianBatDau().format(DT_FMT) : "";
                case 3 -> g.getThoiGianKetThuc() != null ? g.getThoiGianKetThuc().format(DT_FMT) : "";
                case 4 -> g.isTrangThai();
                case 5 -> "Chỉnh sửa";
                default -> "";
            };
        }

        Gia getGiaAt(int r) {
            return (r >= 0 && r < data.size()) ? data.get(r) : null;
        }
    }

    // =================================================================
    //  CELL RENDERERS
    // =================================================================

    private class RowCellRenderer extends DefaultTableCellRenderer {
        private final Font font;
        private final Color fg;
        private final int align;

        RowCellRenderer(Font font, Color fg, int align) {
            this.font = font;
            this.fg = fg;
            this.align = align;
        }

        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object value,
                boolean isSel, boolean hasFocus, int row, int col) {
            super.getTableCellRendererComponent(tbl, value, isSel, hasFocus, row, col);
            setFont(font);
            setForeground(fg);
            setHorizontalAlignment(align);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, TABLE_DIVIDER),
                    new EmptyBorder(0, align == SwingConstants.LEFT ? 18 : 8, 0, 8)
            ));
            setBackground(getRowBg(tbl, isSel, row));
            return this;
        }
    }

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
            // Handoff: badge trạng thái giá bám theo chiều rộng ô, không theo text.
            // Giữ margin để tag không dính vách cột; nếu cột đổi, renderer tự co giãn theo ô.
        }

        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object value,
                boolean isSel, boolean hasFocus, int row, int col) {
            boolean active = (value instanceof Boolean) ? (Boolean) value : false;

            if (active) {
                badgeBg = STATUS_GREEN_BG;
                badgeFg = STATUS_GREEN_FG;
                badge.setText("Đang áp dụng");
            } else {
                badgeBg = STATUS_RED_BG;
                badgeFg = STATUS_RED_FG;
                badge.setText("Ngừng áp dụng");
            }

            badge.setForeground(badgeFg);
            setBackground(getRowBg(tbl, isSel, row));
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, TABLE_DIVIDER));
            return this;
        }

        @Override
        protected void paintChildren(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Rectangle r = badge.getBounds();
            g2.setColor(badgeBg);
            int y = r.y + Math.max(0, (r.height - BADGE_HEIGHT) / 2);
            g2.fillRoundRect(r.x, y, r.width, BADGE_HEIGHT, 14, 14);
            g2.dispose();

            super.paintChildren(g);
        }
    }

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
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, TABLE_DIVIDER));
            return this;
        }

        @Override
        protected void paintChildren(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Rectangle r = lbl.getBounds();
            int px = 12, py = 5;
            g2.setColor(PRIMARY_LIGHT);
            g2.fillRoundRect(r.x - px, r.y - py, r.width + 2 * px, r.height + 2 * py, 10, 10);
            g2.setColor(new Color(PRIMARY.getRed(), PRIMARY.getGreen(), PRIMARY.getBlue(), 60));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(r.x - px, r.y - py, r.width + 2 * px, r.height + 2 * py, 10, 10);
            g2.dispose();

            super.paintChildren(g);
        }
    }

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
            button.setPreferredSize(new Dimension(92, 30));

            button.addActionListener(e -> {
                fireEditingStopped();
                Gia g = tableModel.getGiaAt(editingRow);
                if (g != null) onEditGia(g);
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

    private Color getRowBg(JTable tbl, boolean isSel, int row) {
        if (isSel) return NotionTheme.TABLE_SELECTION;
        if (row == hoveredRow) return ROW_HOVER;
        return row % 2 == 0 ? CARD_BG : ROW_ALT;
    }

    // =================================================================
    //  EDIT ACTION
    // =================================================================

    private void onEditGia(Gia gia) {
        editContainer.removeAll();
        ChinhSuaGiaModule editModule = new ChinhSuaGiaModule(gia, () -> {
            rootCard.show(QuanLyGiaModule.this, "LIST");
            loadData();
        });
        editContainer.add(editModule, BorderLayout.CENTER);
        editContainer.revalidate();
        editContainer.repaint();
        rootCard.show(this, "EDIT");
    }

    // =================================================================
    //  AppModule interface
    // =================================================================

    @Override public String getTitle() { return "Quản lý giá"; }
    @Override public JPanel getView()  { return this; }
    @Override public void setOnResult(Consumer<Object> cb) {
        this.callback = cb;
        boolean has = (cb != null);
        btnSubmit.setVisible(has);
        btnCancel.setVisible(has);
        btnPanel.setVisible(has);
    }
    @Override public void reset() {
        rootCard.show(this, "LIST");
        txtSearchMaGia.setText("");
        cboTrangThai.setSelectedIndex(0);
        dpTuNgay.setValue(null);
        dpDenNgay.setValue(null);
        loadData();
    }
}

