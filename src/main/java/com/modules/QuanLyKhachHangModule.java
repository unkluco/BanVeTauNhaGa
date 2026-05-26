package com.modules;

import com.dao.DAO_KhachHang;
import com.entity.KhachHang;

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

public class QuanLyKhachHangModule extends JPanel implements AppModule {

    private Consumer<Object> callback;

    // --- UI ---
    private JButton          btnAddNew;
    private JTextField        txtSearch;
    private JComboBox<String> cboGioiTinh;
    private JTable            table;
    private KhachHangTableModel tableModel;

    // --- Pagination ---
    private int totalRecords = 0;

    private JLabel  lblPageInfo;
    private JLabel  lblTotalCount;
    private JLabel  lblMaleCount;
    private JLabel  lblFemaleCount;

    // --- Data ---
    private List<KhachHang> allData      = new ArrayList<>();
    private List<KhachHang> filteredData = new ArrayList<>();

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
    private static final Color TEXT_MUTED    = NotionTheme.TEXT_FAINT;

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

    public QuanLyKhachHangModule() {
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
                        getWidth(), getHeight(), new Color(0x00, 0x75, 0xDE));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                g2.setColor(AppColors.withAlpha(NotionTheme.SKY, 150));
                g2.fillOval(getWidth() - 255, -92, 260, 260);
                g2.setColor(AppColors.withAlpha(NotionTheme.ROSE, 120));
                g2.fillRoundRect(getWidth() - 420, 86, 180, 64, 22, 22);
                g2.setColor(AppColors.withAlpha(Color.WHITE, 48));
                g2.fillOval(getWidth() - 315, 34, 74, 74);
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

        JLabel lblEyebrow = new JLabel("WORKSPACE / KHÁCH HÀNG");
        lblEyebrow.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblEyebrow.setForeground(AppColors.withAlpha(Color.WHITE, 175));
        lblEyebrow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTitle = new JLabel("Quản lý khách hàng");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblDesc = new JLabel("Theo dõi hồ sơ hành khách, liên hệ và lịch sử đặt vé trong một workspace.");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDesc.setForeground(AppColors.withAlpha(Color.WHITE, 205));
        lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        left.add(lblEyebrow);
        left.add(Box.createVerticalStrut(8));
        left.add(lblTitle);
        left.add(Box.createVerticalStrut(8));
        left.add(lblDesc);

        btnAddNew = createPrimaryButton("+ Thêm khách hàng");
        btnAddNew.setPreferredSize(new Dimension(178, 42));
        btnAddNew.addActionListener(e -> openCreateKhachHangDialog());

        JPanel rightWrapper = new JPanel(new GridBagLayout());
        rightWrapper.setOpaque(false);
        rightWrapper.add(btnAddNew);

        header.add(left, BorderLayout.CENTER);
        header.add(rightWrapper, BorderLayout.EAST);
        return header;
    }

    private JPanel buildStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 12, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));

        lblTotalCount = new JLabel("0");
        lblMaleCount = new JLabel("0");
        lblFemaleCount = new JLabel("0");

        row.add(buildStatCard("Kết quả hiện tại", lblTotalCount, NotionTheme.ACCENT, NotionTheme.ACCENT_SOFT));
        row.add(buildStatCard("Khách nam", lblMaleCount, new Color(0x00, 0x75, 0xDE), NotionTheme.SKY));
        row.add(buildStatCard("Khách nữ", lblFemaleCount, AppColors.ERROR_DARK, NotionTheme.ROSE));
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
        // Handoff: card filter khách hàng lấy chiều cao tự nhiên sau khi add đủ nội dung, không hard-code.
        // Cảnh báo: nếu filter thêm/bớt dòng runtime, cần revalidate và khóa lại max height theo preferred.
        return card;
    }

    private JPanel buildTableCard() {
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
        JLabel title = new JLabel("Danh sách khách hàng");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(ON_SURFACE);
        JLabel hint = new JLabel("Nhấp đúp vào dòng hoặc nút Sửa để cập nhật hồ sơ");
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
        JLabel title = new JLabel("Bộ lọc khách hàng");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(ON_SURFACE);
        JLabel subtitle = new JLabel("Thống kê phía trên thay đổi theo kết quả tìm kiếm bên dưới");
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
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm theo tên, mã KH, CCCD, SĐT hoặc email...");
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
            cboGioiTinh.setSelectedIndex(0);
            applyFilter();
        });

        searchRow.add(searchBox, BorderLayout.CENTER);
        wrapper.add(searchRow);
        wrapper.add(Box.createVerticalStrut(12));

        cboGioiTinh = createFilterCombo(new String[]{"Tất cả giới tính", "Nam", "Nữ"});
        cboGioiTinh.addActionListener(e -> applyFilter());
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
        optionRow.add(createFilterGroup("Giới tính", cboGioiTinh), filterGbc);
        filterGbc.gridx = 1;
        filterGbc.weightx = 0.0;
        filterGbc.insets = new Insets(0, 0, 0, 0);
        optionRow.add(FilterActionGroup.wrap(btnReset), filterGbc);
        // Handoff: filter row dùng grid để control và Bỏ lọc đều cao/cùng nhịp.
        // Cảnh báo: reset filter không tác động search box vì search có nút X riêng.
        wrapper.add(optionRow);

        return wrapper;
    }

    private JScrollPane buildTableSection() {
        tableModel = new KhachHangTableModel();
        table = new JTable(tableModel);
        NotionTheme.styleTable(table);
        table.setRowHeight(56);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(OUTLINE);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(NotionTheme.TABLE_SELECTION);
        table.setSelectionForeground(ON_SURFACE);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        // Row hover
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
                lbl.setBackground(NotionTheme.CARD_MUTED);
                lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, OUTLINE),
                        new EmptyBorder(0, 20, 0, 8)
                ));
                lbl.setPreferredSize(new Dimension(lbl.getPreferredSize().width, 44));
                return lbl;
            }
        });

        // Column widths
        TableColumnModel colModel = table.getColumnModel();
        int[] widths = {100, 160, 120, 100, 80, 60, 60};
        for (int i = 0; i < widths.length; i++) {
            colModel.getColumn(i).setPreferredWidth(widths[i]);
        }

        colModel.getColumn(0).setCellRenderer(new RowCellRenderer(FONT_MONO, PRIMARY, SwingConstants.LEFT));
        colModel.getColumn(1).setCellRenderer(new RowCellRenderer(FONT_BOLD, ON_SURFACE, SwingConstants.LEFT));
        colModel.getColumn(2).setCellRenderer(new RowCellRenderer(FONT_BODY, ON_SURF_VAR, SwingConstants.LEFT));
        colModel.getColumn(3).setCellRenderer(new RowCellRenderer(FONT_BODY, ON_SURF_VAR, SwingConstants.LEFT));
        colModel.getColumn(4).setCellRenderer(new RowCellRenderer(FONT_BODY, ON_SURF_VAR, SwingConstants.LEFT));
        colModel.getColumn(5).setCellRenderer(new GenderBadgeCellRenderer());
        colModel.getColumn(6).setCellRenderer(new EditButtonRenderer());
        colModel.getColumn(6).setCellEditor(new EditButtonEditor());

        // Row click → open edit dialog
        table.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { showKhachHangQuickActions(e); }
            @Override public void mouseReleased(MouseEvent e) { showKhachHangQuickActions(e); }

            @Override
            public void mouseClicked(MouseEvent e) {
                int viewRow = table.rowAtPoint(e.getPoint());
                if (viewRow < 0) return;
                int col = table.columnAtPoint(e.getPoint());
                int modelRow = table.convertRowIndexToModel(viewRow);
                KhachHang kh = tableModel.getKhachHangAt(modelRow);
                if (kh != null && (col == 6 || e.getClickCount() == 2)) {
                    onEditKhachHang(kh);
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
    //  DATA
    // =================================================================

    private void loadData() {
        SwingWorker<List<KhachHang>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<KhachHang> doInBackground() {
                return new DAO_KhachHang().getAll();
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
        String keyword = txtSearch.getText().trim().toLowerCase();
        String genderFilter = cboGioiTinh == null ? "" : switch (cboGioiTinh.getSelectedIndex()) {
            case 1 -> "NAM";
            case 2 -> "NU";
            default -> "";
        };

        filteredData = new ArrayList<>();
        for (KhachHang kh : allData) {
            String gender = kh.getGioiTinh() == null ? "" : kh.getGioiTinh().trim().toUpperCase();
            boolean matchText = keyword.isEmpty()
                    || (kh.getMaKhachHang() != null && kh.getMaKhachHang().toLowerCase().contains(keyword))
                    || (kh.getHoTen() != null && kh.getHoTen().toLowerCase().contains(keyword))
                    || (kh.getCccd() != null && kh.getCccd().toLowerCase().contains(keyword))
                    || (kh.getSoDienThoai() != null && kh.getSoDienThoai().toLowerCase().contains(keyword));
            boolean matchGender = genderFilter.isEmpty() || genderFilter.equals(gender);
            if (matchText && matchGender) filteredData.add(kh);
        }

        // Thống kê luôn đi theo filteredData để số liệu phản ánh đúng search + lọc giới tính hiện tại.
        // Rủi ro: giới tính trong DB đang là String thô NAM/NU, nên cần normalize trước khi so sánh.
        totalRecords = filteredData.size();
        updateStats(filteredData);
        refreshTable();
    }

    private void updateStats(List<KhachHang> source) {
        if (lblTotalCount == null) return;
        long male = source.stream().filter(kh -> "NAM".equalsIgnoreCase(kh.getGioiTinh() == null ? "" : kh.getGioiTinh().trim())).count();
        long female = source.stream().filter(kh -> "NU".equalsIgnoreCase(kh.getGioiTinh() == null ? "" : kh.getGioiTinh().trim())).count();
        lblTotalCount.setText(String.valueOf(source.size()));
        lblMaleCount.setText(String.valueOf(male));
        lblFemaleCount.setText(String.valueOf(female));
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

    private class KhachHangTableModel extends AbstractTableModel {
        private final String[] COLUMNS = {"Mã KH", "Họ và tên", "CCCD", "SĐT", "Email", "GT", ""};
        private List<KhachHang> data = new ArrayList<>();

        void setData(List<KhachHang> data) {
            this.data = new ArrayList<>(data);
            fireTableDataChanged();
        }

        @Override public int getRowCount()    { return data.size(); }
        @Override public int getColumnCount() { return COLUMNS.length; }
        @Override public String getColumnName(int c) { return COLUMNS[c]; }
        @Override
        public Object getValueAt(int r, int c) {
            if (r < 0 || r >= data.size()) return "";
            KhachHang kh = data.get(r);
            return switch (c) {
                case 0 -> kh.getMaKhachHang();
                case 1 -> kh.getHoTen();
                case 2 -> kh.getCccd() != null ? kh.getCccd() : "";
                case 3 -> kh.getSoDienThoai() != null ? kh.getSoDienThoai() : "";
                case 4 -> kh.getEmail() != null ? kh.getEmail() : "";
                case 5 -> kh.getGioiTinh() != null ? kh.getGioiTinh() : "";
                case 6 -> "Chỉnh sửa";
                default -> "";
            };
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            return false; // all edits handled via KhachHangDialog
        }

        @Override
        public void setValueAt(Object value, int r, int c) {
            // no-op
        }

        KhachHang getKhachHangAt(int r) {
            return (r >= 0 && r < data.size()) ? data.get(r) : null;
        }
    }

    // =================================================================
    //  CELL RENDERERS
    // =================================================================

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

    private class GenderBadgeCellRenderer extends JPanel implements TableCellRenderer {
        private final JLabel badge = new JLabel();
        private Color badgeBg = NotionTheme.CARD_MUTED;
        private Color badgeFg = ON_SURF_VAR;
        private static final int BADGE_HEIGHT = 24;
        private static final int BADGE_MARGIN_X = 28;

        GenderBadgeCellRenderer() {
            setLayout(new BorderLayout());
            setOpaque(true);
            setBorder(new EmptyBorder(0, BADGE_MARGIN_X, 0, BADGE_MARGIN_X));
            badge.setFont(FONT_BADGE);
            badge.setHorizontalAlignment(SwingConstants.CENTER);
            badge.setOpaque(false);
            badge.setPreferredSize(new Dimension(10, BADGE_HEIGHT));
            add(badge, BorderLayout.CENTER);
            // Handoff: badge giới tính bám gần full ô để đồng bộ với các property tag trong table.
            // Nếu cột giới tính bị hẹp, giảm BADGE_MARGIN_X trước khi giảm font/padding.
        }

        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object value,
                boolean isSel, boolean hasFocus, int row, int col) {
            String gender = value == null ? "" : value.toString().trim().toUpperCase();
            switch (gender) {
                case "NAM" -> { badge.setText("Nam"); badgeBg = NotionTheme.SKY; badgeFg = new Color(0x00, 0x75, 0xDE); }
                case "NU"  -> { badge.setText("Nữ");  badgeBg = NotionTheme.ROSE; badgeFg = AppColors.ERROR_DARK; }
                default    -> { badge.setText("--");  badgeBg = NotionTheme.CARD_MUTED; badgeFg = ON_SURF_VAR; }
            }
            badge.setForeground(badgeFg);
            setBackground(getRowBg(tbl, isSel, row));
            return this;
        }

        @Override
        protected void paintChildren(Graphics g) {
            // Gender badge map raw NAM/NU sang nhãn tiếng Việt có dấu để UI không lộ mã DB.
            // Rủi ro: nếu DB phát sinh giá trị mới, renderer sẽ rơi về "--" để tránh hiển thị sai.
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Rectangle r = badge.getBounds();
            int x = r.x;
            int y = r.y + Math.max(0, (r.height - BADGE_HEIGHT) / 2);
            int w = r.width;
            int h = BADGE_HEIGHT;
            g2.setColor(badgeBg);
            g2.fillRoundRect(x, y, w, h, 8, 8);
            g2.setColor(AppColors.withAlpha(badgeFg, 65));
            g2.drawRoundRect(x, y, w - 1, h - 1, 8, 8);
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
            button.setPreferredSize(new Dimension(96, 28));

            button.addActionListener(e -> {
                fireEditingStopped();
                KhachHang kh = tableModel.getKhachHangAt(editingRow);
                if (kh != null) onEditKhachHang(kh);
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

    private void showKhachHangQuickActions(MouseEvent e) {
        if (!e.isPopupTrigger()) return;
        int viewRow = table.rowAtPoint(e.getPoint());
        if (viewRow < 0) return;
        table.setRowSelectionInterval(viewRow, viewRow);
        KhachHang kh = tableModel.getKhachHangAt(table.convertRowIndexToModel(viewRow));
        if (kh == null) return;
        JPopupMenu menu = new JPopupMenu();
        JMenuItem edit = new JMenuItem("Sửa");
        edit.addActionListener(ev -> onEditKhachHang(kh));
        menu.add(edit);
        menu.show(table, e.getX(), e.getY());
    }

    private void onEditKhachHang(KhachHang kh) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        KhachHangDialog dlg = KhachHangDialog.edit(owner, kh, this::loadData);
        dlg.setVisible(true);
    }

    // =================================================================
    //  AppModule interface
    // =================================================================

    @Override public String getTitle() { return "Quản lý khách hàng"; }
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
        if (cboGioiTinh != null) cboGioiTinh.setSelectedIndex(0);
        loadData();
    }

    private JComboBox<String> createFilterCombo(String[] items) {
        JComboBox<String> cbo = new JComboBox<>(items);
        cbo.setFont(FONT_BODY);
        cbo.setPreferredSize(new Dimension(180, 38));
        cbo.setBackground(NotionTheme.CARD);
        cbo.setBorder(BorderFactory.createLineBorder(OUTLINE, 1, true));
        NotionTheme.applyComboBoxSelection(cbo);
        return cbo;
    }

    private JLabel createFilterLabel(String text) {
        JLabel lbl = new JLabel(text + ":");
        lbl.setFont(FONT_HEADER);
        lbl.setForeground(ON_SURF_VAR);
        return lbl;
    }

    private JPanel createFilterGroup(String label, JComponent input) {
        JPanel group = new JPanel(new BorderLayout(0, 6));
        group.setOpaque(false);
        group.add(createFilterLabel(label), BorderLayout.NORTH);
        group.add(input, BorderLayout.CENTER);
        return group;
        // Handoff: label đặt trên field để filter grid không bị cắt khi nhúng trong MenuModule.
        // Cảnh báo: nếu thêm filter mới, giữ cấu trúc vertical này để cùng baseline với Bỏ lọc.
    }

    private void openCreateKhachHangDialog() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        KhachHangDialog dlg = KhachHangDialog.create(owner, this::loadData);
        dlg.setVisible(true);
    }

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
}



