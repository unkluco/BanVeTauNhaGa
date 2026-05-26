package com.modules;

import com.connectDB.ConnectDB;
import com.dao.DAO_DoanTau;
import com.dao.DAO_Ga;
import com.dao.DAO_HoaDon;
import com.dao.DAO_Lich;
import com.entity.DoanTau;
import com.entity.Ga;
import com.entity.HoaDon;
import com.entity.Lich;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

public class TongQuatModule extends JPanel implements AppModule {

    // ── Colors ────────────────────────────────────────────────────────────────
    private static final Color PRIMARY       = NotionTheme.ACCENT;
    private static final Color PRIMARY_LIGHT = NotionTheme.ACCENT_SOFT;
    private static final Color SURFACE       = NotionTheme.PAGE;
    private static final Color SURF_CONT     = NotionTheme.BORDER;
    private static final Color CARD_BG       = NotionTheme.CARD;
    private static final Color TEXT_MAIN     = NotionTheme.TEXT;
    private static final Color TEXT_SUB      = NotionTheme.TEXT_MUTED;
    private static final Color DIVIDER       = NotionTheme.BORDER;
    private static final Color GREEN         = new Color(0x1A, 0xAE, 0x39);
    private static final Color ORANGE        = new Color(0xDD, 0x5B, 0x00);
    private static final Color PURPLE        = NotionTheme.ACCENT;
    private static final Color SKY           = new Color(0x00, 0x75, 0xDE);

    // ── State ─────────────────────────────────────────────────────────────────
    private Consumer<Object>   callback;           // AppModule callback
    private NavigationCallback navCallback;         // Dashboard → MenuModule navigation
    private final DAO_DoanTau daoDoanTau = new DAO_DoanTau();

    @FunctionalInterface
    public interface NavigationCallback {
        void navigate(String actionKey, String label, Object searchCriteria);
    }
    private Timer   debounceTimer;
    private Timer   clockTimer;
    private JWindow searchPopup;

    // ── UI refs ───────────────────────────────────────────────────────────────
    private JTextField txtSearch;
    private JPanel     searchCard;
    private DefaultListModel<SearchResult> searchModel;
    private JList<SearchResult>            lstResults;

    private JLabel lblNhanVien, lblKhachHang, lblVeBan, lblHoaDon;
    private JLabel lblClock, lblDate;
    private JPanel departurePanel;

    private DefaultTableModel recentTableModel;
    private JTable            recentTable;
    private final DAO_HoaDon  daoHoaDon = new DAO_HoaDon();
    private final DAO_Lich    daoLich = new DAO_Lich();

    // AppModule buttons
    private JButton btnSubmit, btnCancel;
    private JPanel  btnPanel;

    // ─────────────────────────────────────────────────────────────────────────
    public TongQuatModule() {
        setLayout(new BorderLayout());
        setBackground(SURFACE);
        buildUI();
        loadKpis();
        loadRecent();
        loadDepartures();
        startClock();
    }

    // ── BUILD UI ──────────────────────────────────────────────────────────────
    private void buildUI() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(SURFACE);
        content.setBorder(new EmptyBorder(20, 24, 20, 24));

        content.add(buildSearchBar());
        content.add(Box.createVerticalStrut(14));
        content.add(buildTicketSearchCard());
        content.add(Box.createVerticalStrut(14));
        content.add(buildScheduleSearchCard());
        content.add(Box.createVerticalStrut(18));
        content.add(buildMainGrid());
        content.add(Box.createVerticalStrut(16));

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(SURFACE);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(scroll,          BorderLayout.CENTER);
        add(buildBtnPanel(), BorderLayout.SOUTH);

        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) == 0) return;
            if (!isShowing()) {
                hidePopup();
                if (clockTimer != null) clockTimer.stop();
            } else {
                startClock();
            }
        });
    }

    // ── SEARCH BAR ────────────────────────────────────────────────────────────
    private JPanel buildSearchBar() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(SURFACE);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        searchCard = new JPanel(new BorderLayout(8, 0));
        searchCard.setBackground(CARD_BG);
        searchCard.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(PRIMARY, 2, true),
            new EmptyBorder(8, 14, 8, 14)
        ));

        JLabel icon = new JLabel(LineIcons.image(LineIcons.Name.SEARCH, 16, 16));
        // Handoff: overview search icon is generated by LineIcons, not loaded from SVG resources.
        // Risk: keep this icon only in the search bar so filter semantics stay clear.

        txtSearch = new JTextField();
        txtSearch.setBorder(BorderFactory.createEmptyBorder());
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtSearch.setBackground(CARD_BG);
        txtSearch.setForeground(TEXT_MAIN);
        stabilizeLookupInput(txtSearch, 40);
        txtSearch.putClientProperty("JTextField.placeholderText",
            "Tìm kiếm nhân viên, khách hàng, vé, hóa đơn, ga, đoàn tàu...");

        searchCard.add(icon,      BorderLayout.WEST);
        searchCard.add(txtSearch, BorderLayout.CENTER);
        wrapper.add(searchCard, BorderLayout.CENTER);

        // ── Dropdown list ────────────────────────────────────────────────────
        searchModel = new DefaultListModel<>();
        lstResults  = new JList<>(searchModel);
        lstResults.setCellRenderer(new SearchResultRenderer());
        lstResults.setBackground(CARD_BG);
        lstResults.setFixedCellHeight(58);
        lstResults.setSelectionBackground(NotionTheme.TABLE_SELECTION);
        lstResults.setSelectionForeground(TEXT_MAIN);
        lstResults.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int idx = lstResults.locationToIndex(e.getPoint());
                if (idx >= 0) {
                    SearchResult sr = searchModel.getElementAt(idx);
                    hidePopup();
                    if (!"INFO".equals(sr.type)) openDetail(sr);
                }
            }
        });
        lstResults.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    hidePopup();
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    SearchResult sr = lstResults.getSelectedValue();
                    hidePopup();
                    if (sr != null && !"INFO".equals(sr.type)) openDetail(sr);
                }
            }
        });

        // ── Debounce 280 ms ──────────────────────────────────────────────────
        debounceTimer = new Timer(280, e -> doSearch(txtSearch.getText().trim()));
        debounceTimer.setRepeats(false);

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            void fire() {
                if (txtSearch.getText().trim().isEmpty()) { hidePopup(); return; }
                debounceTimer.restart();
            }
            @Override public void insertUpdate(DocumentEvent e)  { fire(); }
            @Override public void removeUpdate(DocumentEvent e)  { fire(); }
            @Override public void changedUpdate(DocumentEvent e) {}
        });

        txtSearch.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) { hidePopup(); }
            }
        });

        // Popup không focusable → txtSearch không bao giờ mất focus khi click vào list
        // Chỉ ẩn popup khi focus thực sự rời khỏi txtSearch (click chỗ khác)
        txtSearch.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) {
                hidePopup();
            }
        });

        NotionTheme.lockMaxWidthToPreferredHeight(wrapper);
        // Handoff: search bar khóa theo preferred sau khi gắn input để tránh hụt chiều cao ở DPI khác.
        // Cảnh báo: không đổi preferred của text field để placeholder/nút clear vẫn ổn định.

        return wrapper;
    }

    // ── TRA CỨU LỊCH CHẠY ───────────────────────────────────────────────────
    private JPanel buildTicketSearchCard() {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(CARD_BG);
        card.setBorder(cardBorder());
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(makeSectionLabel("TRA CỨU LỊCH CHẠY"), BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setBackground(CARD_BG);
        GridBagConstraints lookupGbc = new GridBagConstraints();
        lookupGbc.gridy = 0;
        lookupGbc.fill = GridBagConstraints.BOTH;
        lookupGbc.insets = new Insets(0, 0, 0, 10);

        // Load ga list for SearchableComboBox
        List<Ga> gaList = new DAO_Ga().getAll();

        // GA ĐI — SearchableComboBox<Ga>
        SearchableComboBox<Ga> cbGaDi = new SearchableComboBox<>(
                ga -> ga.getTenGa() + " (" + ga.getMaGa() + ")",
                (ga, q) -> ga.getTenGa().toLowerCase().contains(q)
                        || ga.getMaGa().toLowerCase().contains(q));
        cbGaDi.setItems(gaList);
        cbGaDi.setPlaceholder("Chọn ga đi…");
        cbGaDi.setPreferredSize(new Dimension(0, 40));
        cbGaDi.setMinimumSize(new Dimension(0, 40));

        // GA ĐẾN — SearchableComboBox<Ga>
        SearchableComboBox<Ga> cbGaDen = new SearchableComboBox<>(
                ga -> ga.getTenGa() + " (" + ga.getMaGa() + ")",
                (ga, q) -> ga.getTenGa().toLowerCase().contains(q)
                        || ga.getMaGa().toLowerCase().contains(q));
        cbGaDen.setItems(gaList);
        cbGaDen.setPlaceholder("Chọn ga đến…");
        cbGaDen.setPreferredSize(new Dimension(0, 40));
        cbGaDen.setMinimumSize(new Dimension(0, 40));

        // TỪ NGÀY / ĐẾN NGÀY — đồng bộ với bộ lọc Quản lý lịch chạy
        DatePickerField dateFrom = new DatePickerField();
        dateFrom.setPreferredSize(new Dimension(0, 40));

        DatePickerField dateTo = new DatePickerField();
        dateTo.setPreferredSize(new Dimension(0, 40));

        // Nút TÌM LỊCH
        JButton btn = makeSearchActionBtn("Tìm lịch");
        btn.addActionListener(e -> {
            Ga gaDi  = cbGaDi.getSelectedItem();
            Ga gaDen = cbGaDen.getSelectedItem();
            LocalDate tuNgay = dateFrom.getValue();
            LocalDate denNgay = dateTo.getValue();

            if (tuNgay != null && denNgay != null && tuNgay.isAfter(denNgay)) {
                NotionMessageDialog.showMessageDialog(this,
                        "Từ ngày phải trước hoặc bằng Đến ngày.",
                        "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (gaDi == null && gaDen == null && tuNgay == null && denNgay == null) {
                NotionMessageDialog.showMessageDialog(this,
                        "Vui lòng nhập ít nhất một điều kiện tìm kiếm.",
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String tuNgayYmd = tuNgay != null ? tuNgay.format(DateTimeFormatter.ISO_LOCAL_DATE) : null;
            String denNgayYmd = denNgay != null ? denNgay.format(DateTimeFormatter.ISO_LOCAL_DATE) : null;

            LichSearchCriteria criteria = new LichSearchCriteria(
                    gaDi  != null ? gaDi.getMaGa()  : null,
                    gaDen != null ? gaDen.getMaGa() : null,
                    tuNgayYmd,
                    denNgayYmd,
                    null,
                    null
            );

            if (navCallback != null) {
                navCallback.navigate("QL_LICH_CHAY", "Quản lý lịch chạy", criteria);
            } else if (callback != null) {
                callback.accept(new Object[]{"QL_LICH_CHAY", "Quản lý lịch chạy", criteria});
            } else {
                NotionMessageDialog.showMessageDialog(this,
                        "Màn hình hiện tại chưa hỗ trợ điều hướng lịch chạy.",
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
            // Handoff: tra lịch chỉ gửi điều kiện user thật sự chọn, không tự lọc hôm nay ngầm.
            // Cảnh báo: standalone mode trả criteria qua callback thay vì im lặng như trước.
        });

        addLookupField(fields, lookupGbc, 0, 1.0, wrapFieldGroup("GA ĐI", cbGaDi));
        addLookupField(fields, lookupGbc, 1, 1.0, wrapFieldGroup("GA ĐẾN", cbGaDen));
        addLookupField(fields, lookupGbc, 2, 0.9, wrapFieldGroup("TỪ NGÀY", dateFrom));
        addLookupField(fields, lookupGbc, 3, 0.9, wrapFieldGroup("ĐẾN NGÀY", dateTo));
        addLookupField(fields, lookupGbc, 4, 0, wrapBtnGroup(btn));

        card.add(fields, BorderLayout.CENTER);
        NotionTheme.lockMaxWidthToPreferredHeight(card);
        // Handoff: card tra cứu lịch chạy tự lấy chiều cao từ các field ngày/ga hiện có.
        // Cảnh báo: chỉ nới card ngoài, không thay grid weight để tránh lệch hàng nút Tìm.
        return card;
    }

    // ── TRA CỨU HÓA ĐƠN & VÉ ────────────────────────────────────────────────
    private JPanel buildScheduleSearchCard() {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(CARD_BG);
        card.setBorder(cardBorder());
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(makeSectionLabel("TRA CỨU HÓA ĐƠN & VÉ NHANH"), BorderLayout.NORTH);

        JPanel row = new JPanel(new GridBagLayout());
        row.setBackground(CARD_BG);
        GridBagConstraints rowGbc = new GridBagConstraints();
        rowGbc.gridy = 0;
        rowGbc.fill = GridBagConstraints.BOTH;

        // 1 field tìm kiếm đa năng: mã HĐ, mã vé, tên KH, tên NV, CCCD…
        JTextField txtSearchHD = makeInput("Mã HĐ, mã vé, tên KH, tên NV, CCCD…");
        JButton btnTim = makeSearchActionBtn("Tìm");
        btnTim.addActionListener(e -> {
            String kw = txtSearchHD.getText().trim();
            if (kw.isEmpty()) {
                NotionMessageDialog.showMessageDialog(this,
                        "Vui lòng nhập mã hóa đơn, mã vé hoặc thông tin khách hàng cần tìm.",
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String actionKey = looksLikeTicketKeyword(kw) ? "QL_VE_HOA_DON" : "QL_HOA_DON";
            String label = looksLikeTicketKeyword(kw) ? "Quản lý vé" : "Quản lý hóa đơn";
            if (navCallback != null) {
                navCallback.navigate(actionKey, label, kw);
            } else if (callback != null) {
                callback.accept(new String[]{actionKey, label, kw});
            }
            // Handoff: keyword giống mã vé sẽ mở quản lý vé, còn lại ưu tiên hóa đơn/khách hàng.
            // Cảnh báo: nếu DB đổi format mã vé, cập nhật looksLikeTicketKeyword().
        });
        txtSearchHD.addActionListener(e -> btnTim.doClick());

        addLookupField(row, rowGbc, 0, 1.0, wrapFieldGroup("TỪ KHÓA", txtSearchHD));
        addLookupField(row, rowGbc, 1, 0, wrapBtnGroup(btnTim));
        // Handoff: hai khối tra cứu dùng chung grid/button wrapper để nút Tìm đồng đều ở cuối hàng.
        // Cảnh báo: nếu đổi kích thước nút tìm, cập nhật makeSearchActionBtn() để cả hai card đổi cùng lúc.

        card.add(row, BorderLayout.CENTER);
        NotionTheme.lockMaxWidthToPreferredHeight(card);
        // Handoff: card tra cứu hóa đơn/vé tự theo preferred height để không cắt field khi font đổi.
        // Cảnh báo: logic điều hướng theo keyword không đổi, chỉ chỉnh giới hạn layout card ngoài.
        return card;
    }

    // ── MAIN GRID (2/3 left + 1/3 right) ─────────────────────────────────────
    private JPanel buildMainGrid() {
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setBackground(SURFACE);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints gL = new GridBagConstraints();
        gL.gridx = 0; gL.gridy = 0;
        gL.weightx = 0.65; gL.weighty = 1.0;
        gL.fill = GridBagConstraints.BOTH;
        gL.insets = new Insets(0, 0, 0, 14);

        GridBagConstraints gR = new GridBagConstraints();
        gR.gridx = 1; gR.gridy = 0;
        gR.weightx = 0.35; gR.weighty = 1.0;
        gR.fill = GridBagConstraints.BOTH;

        grid.add(buildLeftColumn(),  gL);
        grid.add(buildRightColumn(), gR);
        return grid;
    }

    // ── LEFT COLUMN ───────────────────────────────────────────────────────────
    private JPanel buildLeftColumn() {
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setBackground(SURFACE);

        col.add(buildKpiRow());
        col.add(Box.createVerticalStrut(14));
        col.add(buildRecentCard());
        col.add(Box.createVerticalStrut(14));
        col.add(buildClockCard());
        return col;
    }

    // ── KPI ROW ───────────────────────────────────────────────────────────────
    private JPanel buildKpiRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 10, 0));
        row.setBackground(SURFACE);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 96));

        lblNhanVien  = new JLabel("--");
        lblKhachHang = new JLabel("--");
        lblVeBan     = new JLabel("--");
        lblHoaDon    = new JLabel("--");

        row.add(kpiCard("Nhân viên",                    lblNhanVien,  PURPLE, NotionTheme.ACCENT_SOFT));
        row.add(kpiCard("Khách hàng",                   lblKhachHang, GREEN, NotionTheme.MINT));
        row.add(kpiCard("Vé đã bán",          lblVeBan,     ORANGE, NotionTheme.PEACH));
        row.add(kpiCard("Hóa đơn hôm nay",    lblHoaDon,    SKY, NotionTheme.SKY));
        return row;
    }

    private JPanel kpiCard(String label, JLabel valueLbl, Color accent, Color tint) {
        JPanel card = new JPanel(new BorderLayout(10, 4)) {
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
        card.setBorder(new EmptyBorder(12, 16, 12, 16));

        JPanel bar = new JPanel();
        bar.setBackground(accent);
        bar.setPreferredSize(new Dimension(4, 1));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(TEXT_SUB);

        valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valueLbl.setForeground(accent);

        JPanel txt = new JPanel(new BorderLayout(0, 3));
        txt.setOpaque(false);
        txt.setBorder(new EmptyBorder(0, 2, 0, 0));
        txt.add(lbl,      BorderLayout.NORTH);
        txt.add(valueLbl, BorderLayout.CENTER);

        card.add(bar, BorderLayout.WEST);
        card.add(txt, BorderLayout.CENTER);
        return card;
    }

    // ── GIAO DỊCH GẦN ĐÂY ────────────────────────────────────────────────────
    private JPanel buildRecentCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(new LineBorder(DIVIDER, 1, true));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Header
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setBackground(CARD_BG);
        hdr.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 1, 0, SURF_CONT),
            new EmptyBorder(14, 20, 14, 20)
        ));
        JLabel title = new JLabel("GIAO DỊCH GẦN ĐÂY");
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(TEXT_MAIN);
        JButton btnExport = makeLinkBtn("Xuất nhật ký ngày");
        hdr.add(title,     BorderLayout.WEST);
        hdr.add(btnExport, BorderLayout.EAST);
        card.add(hdr, BorderLayout.NORTH);

        // Table
        String[] cols = {
            "MÃ HÓA ĐƠN",
            "KHÁCH HÀNG",
            "NGÀY LẬP",
            "SỐ VÉ",
            "TỔNG TIỀN"
        };
        recentTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        recentTable = new JTable(recentTableModel);
        NotionTheme.styleTable(recentTable);
        recentTable.setRowHeight(40);
        recentTable.setShowGrid(false);
        recentTable.setIntercellSpacing(new Dimension(0, 0));
        recentTable.setSelectionBackground(NotionTheme.TABLE_SELECTION);
        recentTable.setSelectionForeground(TEXT_MAIN);
        recentTable.setFillsViewportHeight(true);
        recentTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e) || e.getClickCount() != 2) return;
                openRecentInvoiceDetail();
            }
        });

        JTableHeader header = recentTable.getTableHeader();
        header.setDefaultRenderer((tbl, val, sel, foc, r, c) -> {
            JLabel lbl = new JLabel(val == null ? "" : val.toString());
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
            lbl.setForeground(TEXT_SUB);
            lbl.setBackground(SURFACE);
            lbl.setOpaque(true);
            lbl.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, SURF_CONT),
                new EmptyBorder(0, 16, 0, 4)
            ));
            lbl.setPreferredSize(new Dimension(0, 44));
            return lbl;
        });

        recentTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setBorder(new EmptyBorder(0, 16, 0, 8));
                setOpaque(true);
                if (sel) {
                    setBackground(PRIMARY_LIGHT);
                    setForeground(TEXT_MAIN);
                    setFont(new Font("Segoe UI", Font.PLAIN, 13));
                } else {
                    setBackground(row % 2 == 0 ? CARD_BG : NotionTheme.PAGE);
                    switch (col) {
                        case 0 -> {
                            setForeground(PRIMARY);
                            setFont(new Font("Segoe UI", Font.BOLD, 12));
                        }
                        case 2 -> {
                            setForeground(TEXT_SUB);
                            setFont(new Font("Segoe UI", Font.PLAIN, 12));
                        }
                        case 4 -> {
                            setForeground(GREEN);
                            setFont(new Font("Segoe UI", Font.PLAIN, 13));
                        }
                        default -> {
                            setForeground(TEXT_MAIN);
                            setFont(new Font("Segoe UI", Font.PLAIN, 13));
                        }
                    }
                }
                return this;
            }
        });

        int[] widths = {140, 180, 145, 70, 150};
        for (int i = 0; i < widths.length; i++)
            recentTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JScrollPane scroll = new JScrollPane(recentTable);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(CARD_BG);
        scroll.setPreferredSize(new Dimension(0, 280));
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }


    private void openRecentInvoiceDetail() {
        int viewRow = recentTable.getSelectedRow();
        if (viewRow < 0) return;
        int modelRow = recentTable.convertRowIndexToModel(viewRow);
        Object value = recentTableModel.getValueAt(modelRow, 0);
        String maHoaDon = value == null ? "" : value.toString().trim();
        if (maHoaDon.isEmpty()) return;

        HoaDon hoaDon = daoHoaDon.findById(maHoaDon);
        if (hoaDon == null) {
            NotionMessageDialog.showMessageDialog(this,
                    "Không tìm thấy hóa đơn " + maHoaDon + ".",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Window owner = SwingUtilities.getWindowAncestor(this);
        JFrame frame = owner instanceof JFrame ? (JFrame) owner : null;
        new ChiTietHoaDonDialog(frame, hoaDon).setVisible(true);
        // Handoff: double-click giao dịch gần đây mở đúng dialog chi tiết hóa đơn hiện có.
        // Cảnh báo: cột 0 phải luôn là mã hóa đơn gốc, nếu đổi thứ tự cột cần cập nhật index này.
    }
    // ── ĐỒNG HỒ SỐ ───────────────────────────────────────────────────────────
    private JPanel buildClockCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(DIVIDER, 1, true),
            new EmptyBorder(20, 20, 20, 20)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        lblDate = new JLabel("...", SwingConstants.CENTER);
        lblDate.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblDate.setForeground(TEXT_SUB);
        lblDate.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblClock = new JLabel("00:00:00", SwingConstants.CENTER);
        lblClock.setFont(new Font("Segoe UI", Font.BOLD, 54));
        lblClock.setForeground(PRIMARY);
        lblClock.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel syncLbl = new JLabel("●  Thời gian hệ thống đồng bộ");
        syncLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        syncLbl.setForeground(AppColors.SUCCESS);
        syncLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(Box.createVerticalGlue());
        card.add(lblDate);
        card.add(Box.createVerticalStrut(4));
        card.add(lblClock);
        card.add(Box.createVerticalStrut(8));
        card.add(syncLbl);
        card.add(Box.createVerticalGlue());
        return card;
    }

    // ── RIGHT COLUMN ──────────────────────────────────────────────────────────
    private JPanel buildRightColumn() {
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setBackground(SURFACE);
        col.add(buildDeparturesCard());
        return col;
    }

    // ── BẢNG KHỞI HÀNH ───────────────────────────────────────────────────────
    private JPanel buildDeparturesCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(CARD_BG);
        card.setBorder(cardBorder());
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Header
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setBackground(CARD_BG);
        JLabel title = new JLabel("BẢNG KHởI HÀNH");
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(TEXT_MAIN);
        JLabel liveLbl = new JLabel("● Live");
        liveLbl.setFont(new Font("Segoe UI", Font.BOLD, 9));
        liveLbl.setForeground(AppColors.SUCCESS);
        hdr.add(title,   BorderLayout.WEST);
        hdr.add(liveLbl, BorderLayout.EAST);
        card.add(hdr, BorderLayout.NORTH);

        // Departure items
        departurePanel = new JPanel();
        departurePanel.setLayout(new BoxLayout(departurePanel, BoxLayout.Y_AXIS));
        departurePanel.setBackground(CARD_BG);

        JLabel loading = new JLabel("Đang tải lịch trình...");
        loading.setForeground(TEXT_SUB);
        loading.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        loading.setAlignmentX(Component.LEFT_ALIGNMENT);
        departurePanel.add(loading);
        card.add(departurePanel, BorderLayout.CENTER);

        // View all button
        JButton btnAll = new JButton("XEM TOÀN BỘ LỊCH TRÌNH");
        btnAll.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnAll.setForeground(TEXT_SUB);
        btnAll.setBackground(CARD_BG);
        btnAll.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(DIVIDER, 1, true),
            new EmptyBorder(8, 12, 8, 12)
        ));
        btnAll.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAll.setFocusPainted(false);
        btnAll.addActionListener(e -> {
            LocalDateTime now = LocalDateTime.now();
            LichSearchCriteria criteria = new LichSearchCriteria(
                    null, null,
                    now.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                    null,
                    now.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                    null);
            if (navCallback != null) {
                navCallback.navigate("QL_LICH_CHAY", "Quản lý lịch chạy", criteria);
            } else if (callback != null) {
                callback.accept(new Object[]{"QL_LICH_CHAY", "Quản lý lịch chạy", criteria});
            }
            // Handoff: Xem toàn bộ lịch trình mở quản lý lịch với mốc hiện tại trở đi.
            // Cảnh báo: chỉ set từ ngày/từ giờ, không set đến ngày để giữ nghĩa "trở đi".
        });
        card.add(btnAll, BorderLayout.SOUTH);

        return card;
    }

    // ── BUTTON PANEL ──────────────────────────────────────────────────────────
    private JPanel buildBtnPanel() {
        btnSubmit = new JButton("Xác nhận");
        btnCancel = new JButton("Hủy");
        btnSubmit.addActionListener(e -> { if (callback != null) callback.accept("ok"); });
        btnCancel.addActionListener(e -> { if (callback != null) callback.accept(null); });
        btnPanel = new JPanel();
        btnPanel.add(btnSubmit);
        btnPanel.add(btnCancel);
        return btnPanel;
    }

    // ── CLOCK TIMER ───────────────────────────────────────────────────────────
    private void startClock() {
        if (clockTimer != null) clockTimer.stop();
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss");
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String[] days = {
            "Chủ Nhật", "Thứ Hai", "Thứ Ba",
            "Thứ Tư",   "Thứ Năm", "Thứ Sáu", "Thứ Bảy"
        };
        clockTimer = new Timer(1000, e -> {
            LocalDateTime now = LocalDateTime.now();
            if (lblClock != null) lblClock.setText(now.format(timeFmt));
            if (lblDate  != null) {
                int dow = now.getDayOfWeek().getValue(); // Mon=1..Sun=7
                lblDate.setText(days[dow % 7] + ", " + now.format(dateFmt));
            }
        });
        clockTimer.setInitialDelay(0);
        clockTimer.start();
    }

    // ── SEARCH LOGIC ──────────────────────────────────────────────────────────
    private void doSearch(String keyword) {
        if (keyword.isEmpty()) { hidePopup(); return; }
        new SwingWorker<List<SearchResult>, Void>() {
            @Override protected List<SearchResult> doInBackground() { return queryAll(keyword); }
            @Override protected void done() {
                try {
                    List<SearchResult> list = get();
                    searchModel.clear();
                    if (list.isEmpty())
                        searchModel.addElement(new SearchResult(
                            "INFO", "Không tìm thấy kết quả", "", ""));
                    else
                        list.forEach(searchModel::addElement);
                    showPopup();
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }.execute();
    }

    private List<SearchResult> queryAll(String kw) {
        List<SearchResult> out = new ArrayList<>();
        String p = "%" + kw + "%";
        Connection con = ConnectDB.getCon();
        if (con == null) return out;

        search(con, out,
            "SELECT TOP 4 maNV, hoTen, vaiTro, soDienThoai " +
            "FROM NhanVien WHERE maNV LIKE ? OR hoTen LIKE ? OR soDienThoai LIKE ?",
            rs -> new SearchResult("NV", rs.getString("maNV"), rs.getString("hoTen"),
                rs.getString("vaiTro") + "  ·  " + rs.getString("soDienThoai")),
            p, p, p);

        search(con, out,
            "SELECT TOP 4 maKhachHang, hoTen, soDienThoai " +
            "FROM KhachHang WHERE maKhachHang LIKE ? OR hoTen LIKE ? OR soDienThoai LIKE ?",
            rs -> new SearchResult("KH", rs.getString("maKhachHang"), rs.getString("hoTen"),
                "SDT: " + rs.getString("soDienThoai")),
            p, p, p);

        search(con, out,
            "SELECT TOP 4 hd.maHoaDon, hd.maNV, CONVERT(varchar,hd.ngayLap,120) AS nd, " +
            "       MIN(ISNULL(kh.hoTen, '')) AS tenKhach, MIN(ISNULL(kh.soDienThoai, '')) AS sdtKhach " +
            "FROM HoaDon hd " +
            "LEFT JOIN ChiTietHoaDon ct ON hd.maHoaDon = ct.maHoaDon " +
            "LEFT JOIN KhachHang kh ON ct.maKhachHang = kh.maKhachHang " +
            "LEFT JOIN NhanVien nv ON hd.maNV = nv.maNV " +
            "WHERE hd.maHoaDon LIKE ? OR kh.hoTen LIKE ? OR kh.soDienThoai LIKE ? OR kh.cccd LIKE ? OR nv.hoTen LIKE ? " +
            "GROUP BY hd.maHoaDon, hd.maNV, hd.ngayLap",
            rs -> new SearchResult("HD", rs.getString("maHoaDon"),
                "Hóa đơn " + rs.getString("maHoaDon"),
                "KH: " + fallback(rs.getString("tenKhach"), "-") + "  ·  " + clip(rs.getString("nd"), 10)),
            p, p, p, p, p);

        search(con, out,
            "SELECT TOP 4 v.maVe, v.trangThai, ISNULL(hd.maHoaDon, '') AS maHoaDon, " +
            "       ISNULL(kh.hoTen, '') AS tenKhach " +
            "FROM Ve v " +
            "LEFT JOIN ChiTietHoaDon ct ON v.maVe = ct.maVe " +
            "LEFT JOIN HoaDon hd ON ct.maHoaDon = hd.maHoaDon " +
            "LEFT JOIN KhachHang kh ON ct.maKhachHang = kh.maKhachHang " +
            "WHERE v.maVe LIKE ? OR hd.maHoaDon LIKE ? OR kh.hoTen LIKE ? OR kh.soDienThoai LIKE ? OR kh.cccd LIKE ?",
            rs -> new SearchResult("VE", rs.getString("maVe"),
                "Vé " + rs.getString("maVe"),
                "HĐ: " + fallback(rs.getString("maHoaDon"), "-") + "  ·  " + fallback(rs.getString("tenKhach"), "-")),
            p, p, p, p, p);

        search(con, out,
            "SELECT TOP 4 maGa, tenGa, diaChi FROM Ga WHERE maGa LIKE ? OR tenGa LIKE ?",
            rs -> new SearchResult("GA", rs.getString("maGa"), rs.getString("tenGa"),
                rs.getString("diaChi")),
            p, p);

        int dtCount = 0;
        for (DoanTau dt : daoDoanTau.getAllActive()) {
            String ma = dt.getMaDoanTau() == null ? "" : dt.getMaDoanTau();
            String ten = dt.getTenDoanTau() == null ? "" : dt.getTenDoanTau();
            String haystack = (ma + " " + ten).toLowerCase();
            if (!haystack.contains(kw.toLowerCase())) continue;

            out.add(new SearchResult("DT", ma, ten, "Mã: " + ma));
            dtCount++;
            if (dtCount >= 4) break;
        }

        search(con, out,
            "SELECT TOP 4 maKhuyenMai, tenKhuyenMai, trangThai " +
            "FROM KhuyenMai WHERE maKhuyenMai LIKE ? OR tenKhuyenMai LIKE ?",
            rs -> new SearchResult("KM", rs.getString("maKhuyenMai"),
                rs.getString("tenKhuyenMai"),
                rs.getBoolean("trangThai") ? "Hoạt động" : "Dừng"),
            p, p);

        return out;
    }

    @FunctionalInterface
    interface RowMapper { SearchResult map(ResultSet rs) throws SQLException; }

    private void search(Connection con, List<SearchResult> out,
                        String sql, RowMapper mapper, String... params) {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setString(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapper.map(rs));
            }
        } catch (SQLException e) {
            System.err.println("[TQ search] " + e.getMessage());
        }
    }

    private static String clip(String s, int len) {
        if (s == null) return "";
        return s.length() > len ? s.substring(0, len) : s;
    }

    private static String fallback(String value, String replacement) {
        return value == null || value.isBlank() ? replacement : value;
    }

    // ── POPUP ─────────────────────────────────────────────────────────────────
    private void showPopup() {
        Window win = SwingUtilities.getWindowAncestor(this);
        if (win == null) return;
        if (searchPopup == null || searchPopup.getOwner() != win) {
            if (searchPopup != null) searchPopup.dispose();
            searchPopup = new JWindow(win);
            searchPopup.setFocusableWindowState(false); // không tham gia hệ thống focus
            searchPopup.setAlwaysOnTop(true);
            JScrollPane sp = new JScrollPane(lstResults);
            sp.setBorder(new LineBorder(PRIMARY, 1));
            sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            searchPopup.setContentPane(sp);
        }
        // Không hiện nếu txtSearch không còn focus (ví dụ: dialog đang mở)
        if (!txtSearch.isFocusOwner()) return;
        try {
            Point loc  = searchCard.getLocationOnScreen();
            int   rows = Math.min(searchModel.size(), 7);
            searchPopup.setSize(searchCard.getWidth(), rows * 58 + 2);
            searchPopup.setLocation(loc.x, loc.y + searchCard.getHeight());
            searchPopup.setVisible(true);
        } catch (IllegalComponentStateException ignored) {}
    }

    private void hidePopup() {
        if (searchPopup != null) searchPopup.setVisible(false);
    }

    // ── OPEN DETAIL ───────────────────────────────────────────────────────────
    private void openDetail(SearchResult sr) {
        new SwingWorker<LinkedHashMap<String, String>, Void>() {
            @Override protected LinkedHashMap<String, String> doInBackground() {
                return fetchEntityFields(sr);
            }
            @Override protected void done() {
                try {
                    LinkedHashMap<String, String> fields = get();
                    if (fields == null) return;
                    Color  color = SearchResultRenderer.COLORS.getOrDefault(sr.type, PRIMARY);
                    String label = SearchResultRenderer.LABELS.getOrDefault(sr.type, sr.type);
                    EntityDetailModule detail = new EntityDetailModule(
                        label, color, sr.title, sr.id, fields);
                    Window win = SwingUtilities.getWindowAncestor(TongQuatModule.this);
                    JFrame frame = (win instanceof JFrame) ? (JFrame) win : null;
                    ModuleLauncher.asDialog(detail, frame, res -> {});
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }.execute();
    }

    private LinkedHashMap<String, String> fetchEntityFields(SearchResult sr) {
        String sql = getDetailSql(sr.type);
        if (sql == null) return null;
        Connection con = ConnectDB.getCon();
        if (con == null) return null;
        LinkedHashMap<String, String> fields = new LinkedHashMap<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, sr.id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return fields;
                ResultSetMetaData meta = rs.getMetaData();
                int cols = meta.getColumnCount();
                for (int i = 1; i <= cols; i++) {
                    String colLabel = meta.getColumnLabel(i);
                    String val      = rs.getString(i);
                    fields.put(colLabel, val);
                }
            }
        } catch (SQLException e) {
            System.err.println("[Detail] " + e.getMessage());
        }
        return fields;
    }

    /** SQL dùng alias Tiếng Việt — getColumnLabel() sẽ trả về tên alias làm nhãn hiển thị */
    private String getDetailSql(String type) {
        switch (type) {
            case "NV":
                return "SELECT maNV AS [Mã NV], hoTen AS [Họ tên]," +
                    " vaiTro AS [Vai trò], soDienThoai AS [Điện thoại]," +
                    " cccd AS [CCCD], email AS [Email]," +
                    " maGaLamViec AS [Ga làm việc]," +
                    " CONVERT(varchar,ngaySinh,103) AS [Ngày sinh]," +
                    " gioiTinh AS [Giới tính], quocTich AS [Quốc tịch]," +
                    " diaChiThuongTru AS [Địa chỉ thường trú]," +
                    " diaChiTamTru AS [Địa chỉ tạm trú]," +
                    " trangThai AS [Trạng thái]" +
                    " FROM NhanVien WHERE maNV=?";
            case "KH":
                return "SELECT maKhachHang AS [Mã KH], hoTen AS [Họ tên]," +
                    " cccd AS [CCCD], soDienThoai AS [Điện thoại]" +
                    " FROM KhachHang WHERE maKhachHang=?";
            case "HD":
                return "SELECT hd.maHoaDon AS [Mã HĐ]," +
                    " hd.maNV AS [Nhân viên (mã)]," +
                    " nv.hoTen AS [Tên nhân viên]," +
                    " MIN(kh.maKhachHang) AS [Khách hàng đầu tiên (mã)]," +
                    " MIN(kh.hoTen) AS [Tên khách hàng đầu tiên]," +
                    " COUNT(DISTINCT ct.maKhachHang) AS [Số khách]," +
                    " CONVERT(varchar,hd.ngayLap,120) AS [Ngày lập]," +
                    " COUNT(ct.maChiTietHD) AS [Số vé]," +
                    " ISNULL(SUM(ct.giaTien),0) AS [Tổng tiền (VNĐ)]" +
                    " FROM HoaDon hd" +
                    " LEFT JOIN NhanVien nv ON hd.maNV=nv.maNV" +
                    " LEFT JOIN ChiTietHoaDon ct ON hd.maHoaDon=ct.maHoaDon" +
                    " LEFT JOIN KhachHang kh ON kh.maKhachHang=ct.maKhachHang" +
                    " WHERE hd.maHoaDon=?" +
                    " GROUP BY hd.maHoaDon,hd.maNV,nv.hoTen,hd.ngayLap";
            case "VE":
                return "SELECT v.maVe AS [Mã vé]," +
                    " v.maLich AS [Lịch chạy (mã)]," +
                    " v.maGhe AS [Ghế (mã)]," +
                    " toa.loaiGhe AS [Loại ghế]," +
                    " v.trangThai AS [Trạng thái]," +
                    " v.lyDoHuy AS [Lý do hủy]," +
                    " CONVERT(varchar,v.ngayHuy,120) AS [Ngày hủy]" +
                    " FROM Ve v" +
                    " LEFT JOIN Ghe g ON v.maGhe=g.maGhe" +
                    " LEFT JOIN ToaTau toa ON g.maToaTau=toa.maToaTau" +
                    " WHERE v.maVe=?";
                // Handoff: loại ghế của vé lấy qua Ghe -> ToaTau vì bảng Ghe chỉ lưu maToaTau/soGhe.
                // Rủi ro: nếu đổi schema ghế có loaiGhe riêng thì cần đồng bộ lại query detail này.
            case "GA":
                return "SELECT maGa AS [Mã ga], tenGa AS [Tên ga], diaChi AS [Địa chỉ]" +
                    " FROM Ga WHERE maGa=?";
            case "DT":
                return "SELECT maDoanTau AS [Mã đoàn tàu], tenDoanTau AS [Tên đoàn tàu]" +
                    " FROM DoanTau WHERE maDoanTau=?";
            case "KM":
                return "SELECT maKhuyenMai AS [Mã KM], tenKhuyenMai AS [Tên KM]," +
                    " CONVERT(varchar,thoiGianBatDau,103) AS [Thời gian bắt đầu]," +
                    " CONVERT(varchar,thoiGianKetThuc,103) AS [Thời gian kết thúc]," +
                    " moTa AS [Mô tả]," +
                    " CASE WHEN trangThai=1 THEN N'Hoạt động' ELSE N'Dừng' END AS [Trạng thái]" +
                    " FROM KhuyenMai WHERE maKhuyenMai=?";
            default:
                return null;
        }
    }

    // ── LOAD DATA ─────────────────────────────────────────────────────────────
    private void loadKpis() {
        new SwingWorker<int[], Void>() {
            @Override protected int[] doInBackground() {
                int[] v = new int[4];
                Connection con = ConnectDB.getCon();
                if (con == null) return v;
                try {
                    try (PreparedStatement ps = con.prepareStatement(
                             "SELECT COUNT(*) FROM NhanVien");
                         ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) v[0] = rs.getInt(1);
                    }
                    try (PreparedStatement ps = con.prepareStatement(
                             "SELECT COUNT(*) FROM KhachHang");
                         ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) v[1] = rs.getInt(1);
                    }
                    try (PreparedStatement ps = con.prepareStatement(
                             "SELECT COUNT(*) FROM Ve WHERE trangThai='DA_BAN'");
                         ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) v[2] = rs.getInt(1);
                    }
                    try (PreparedStatement ps = con.prepareStatement(
                             "SELECT COUNT(*) FROM HoaDon " +
                             "WHERE CAST(ngayLap AS DATE)=CAST(GETDATE() AS DATE)");
                         ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) v[3] = rs.getInt(1);
                    }
                } catch (SQLException e) {
                    System.err.println("[KPI] " + e.getMessage());
                }
                return v;
            }
            @Override protected void done() {
                try {
                    int[] v = get();
                    lblNhanVien .setText(String.valueOf(v[0]));
                    lblKhachHang.setText(String.valueOf(v[1]));
                    lblVeBan    .setText(String.valueOf(v[2]));
                    lblHoaDon   .setText(String.valueOf(v[3]));
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void loadRecent() {
        new SwingWorker<List<Object[]>, Void>() {
            @Override protected List<Object[]> doInBackground() {
                List<Object[]> rows = new ArrayList<>();
                Connection con = ConnectDB.getCon();
                if (con == null) return rows;
                String sql =
                    "SELECT TOP 15 hd.maHoaDon, MIN(kh.hoTen) AS tenKH, " +
                    "  CONVERT(varchar,hd.ngayLap,120) AS nd, " +
                    "  COUNT(ct.maChiTietHD) AS soVe, " +
                    "  ISNULL(SUM(ct.giaTien),0) AS tong, " +
                    "  COUNT(DISTINCT ct.maKhachHang) AS soKH " +
                    "FROM HoaDon hd " +
                    "LEFT JOIN ChiTietHoaDon ct ON hd.maHoaDon=ct.maHoaDon " +
                    "LEFT JOIN KhachHang kh ON kh.maKhachHang=ct.maKhachHang " +
                    "GROUP BY hd.maHoaDon, hd.ngayLap " +
                    "ORDER BY hd.ngayLap DESC";
                try (PreparedStatement ps = con.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
                    while (rs.next()) {
                        String tenKH = rs.getString("tenKH");
                        if (tenKH == null) tenKH = "Khách vãng lai";
                        int soKH = rs.getInt("soKH");
                        if (soKH > 1) tenKH = tenKH + " (+" + (soKH - 1) + ")";
                        java.math.BigDecimal tong = rs.getBigDecimal("tong");
                        rows.add(new Object[]{
                            rs.getString("maHoaDon"),
                            tenKH,
                            clip(rs.getString("nd"), 16),
                            rs.getInt("soVe") + " vé",
                            (tong != null ? nf.format(tong) : "0") + " ₫"
                        });
                    }
                } catch (SQLException e) {
                    System.err.println("[Recent] " + e.getMessage());
                }
                return rows;
            }
            @Override protected void done() {
                try {
                    List<Object[]> rows = get();
                    recentTableModel.setRowCount(0);
                    rows.forEach(recentTableModel::addRow);
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void loadDepartures() {
        new SwingWorker<List<String[]>, Void>() {
            @Override protected List<String[]> doInBackground() {
                List<String[]> results = new ArrayList<>();
                Connection con = ConnectDB.getCon();
                if (con == null) return results;
                String sql =
                    "SELECT TOP 5 l.maLich, l.maDoanTau," +
                    "  ISNULL(dt.tenDoanTau, l.maDoanTau) AS tenDoan," +
                    "  ISNULL(g1.tenGa, ISNULL(t.gaDi,'?')) AS gaDi," +
                    "  ISNULL(g2.tenGa, ISNULL(t.gaDen,'?')) AS gaDen," +
                    "  CONVERT(varchar, l.thoiGianBatDau, 108) AS gio," +
                    "  CONVERT(varchar, l.thoiGianBatDau, 120) AS batDau," +
                    "  CONVERT(varchar, DATEADD(minute, ISNULL(l.thoiGianChay, 0), l.thoiGianBatDau), 120) AS ketThuc," +
                    "  ISNULL(CONVERT(varchar, l.hoatDong), '1') AS trangThai" +
                    " FROM Lich l" +
                    " LEFT JOIN Tuyen t    ON l.maTuyen    = t.maTuyen" +
                    " LEFT JOIN Ga g1      ON t.gaDi       = g1.maGa" +
                    " LEFT JOIN Ga g2      ON t.gaDen      = g2.maGa" +
                    " LEFT JOIN DoanTau dt ON l.maDoanTau  = dt.maDoanTau" +
                    " WHERE DATEADD(minute, ISNULL(l.thoiGianChay, 0), l.thoiGianBatDau) >= DATEADD(hour, -1, GETDATE())" +
                    " ORDER BY l.thoiGianBatDau ASC";
                try (PreparedStatement ps = con.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        results.add(new String[]{
                            rs.getString("maLich"),
                            rs.getString("maDoanTau"),
                            rs.getString("tenDoan"),
                            rs.getString("gaDi"),
                            rs.getString("gaDen"),
                            rs.getString("gio"),
                            rs.getString("batDau"),
                            rs.getString("ketThuc"),
                            rs.getString("trangThai")
                        });
                    }
                } catch (SQLException e) {
                    System.err.println("[Departures] " + e.getMessage());
                }
                return results;
            }
            @Override protected void done() {
                try {
                    List<String[]> results = get();
                    departurePanel.removeAll();
                    if (results.isEmpty()) {
                        JLabel empty = new JLabel("Không có lịch trình nào.");
                        empty.setForeground(TEXT_SUB);
                        empty.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                        empty.setAlignmentX(Component.LEFT_ALIGNMENT);
                        departurePanel.add(empty);
                    } else {
                        for (int i = 0; i < results.size(); i++) {
                            if (i > 0) departurePanel.add(Box.createVerticalStrut(8));
                            departurePanel.add(buildDepartureItem(results.get(i)));
                        }
                    }
                    departurePanel.revalidate();
                    departurePanel.repaint();
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    // ── DEPARTURE ITEM ────────────────────────────────────────────────────────
    private JPanel buildDepartureItem(String[] d) {
        // d: [maLich, maDoanTau, tenDoanTau, gaDi, gaDen, gio, batDau, ketThuc, trangThai]
        String maLich = d[0] != null ? d[0] : "";
        String code  = d[1] != null ? d[1] : "?";
        String tenDoan = d[2] != null ? d[2] : "";
        String gaDi  = d[3] != null ? d[3] : "?";
        String gaDen = d[4] != null ? d[4] : "?";
        String gio   = d[5] != null ? d[5].substring(0, Math.min(5, d[5].length())) : "--:--";
        String status = departureStatus(d.length > 6 ? d[6] : null, d.length > 7 ? d[7] : null, d.length > 8 ? d[8] : null);
        String badge = code.length() > 6 ? code.substring(0, 6) : code;

        JPanel item = new JPanel(new BorderLayout(12, 0));
        item.setBackground(SURF_CONT);
        item.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(NotionTheme.BORDER, 1, true),
            new EmptyBorder(10, 12, 10, 12)
        ));
        item.setAlignmentX(Component.LEFT_ALIGNMENT);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        item.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e) || e.getClickCount() != 2) return;
                openDepartureDetail(maLich);
            }
        });

        // Badge
        JLabel badgeLbl = new JLabel(badge, SwingConstants.CENTER);
        badgeLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        badgeLbl.setForeground(PRIMARY);
        badgeLbl.setBackground(PRIMARY_LIGHT);
        badgeLbl.setOpaque(true);
        badgeLbl.setBorder(new LineBorder(NotionTheme.ACCENT_SOFT, 1));
        badgeLbl.setPreferredSize(new Dimension(52, 52));

        // Center
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(SURF_CONT);

        JLabel routeLbl = new JLabel(gaDi + " → " + gaDen);
        routeLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        routeLbl.setForeground(TEXT_MAIN);

        JLabel infoLbl = new JLabel("Tàu: " + code + (tenDoan.isBlank() || tenDoan.equals(code) ? "" : " · " + tenDoan));
        infoLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        infoLbl.setForeground(TEXT_SUB);

        center.add(routeLbl);
        center.add(Box.createVerticalStrut(3));
        center.add(infoLbl);

        // Right
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBackground(SURF_CONT);

        JLabel timeLbl = new JLabel(gio, SwingConstants.RIGHT);
        timeLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        timeLbl.setForeground(TEXT_MAIN);
        timeLbl.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JLabel statusLbl = new JLabel(status, SwingConstants.RIGHT);
        statusLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        statusLbl.setForeground(GREEN);
        statusLbl.setAlignmentX(Component.RIGHT_ALIGNMENT);

        right.add(timeLbl);
        right.add(Box.createVerticalStrut(2));
        right.add(statusLbl);

        item.add(badgeLbl, BorderLayout.WEST);
        item.add(center,   BorderLayout.CENTER);
        item.add(right,    BorderLayout.EAST);
        return item;
        // Handoff: chuyến sắp tới dùng tên đoàn tàu và trạng thái tính từ thời gian/trạng thái DB.
        // Cảnh báo: nếu DB đổi kiểu trangThai, cập nhật departureStatus().
    }


    private void openDepartureDetail(String maLich) {
        if (maLich == null || maLich.isBlank()) return;
        Lich lich = daoLich.findById(maLich.trim());
        if (lich == null) {
            NotionMessageDialog.showMessageDialog(this,
                    "Không tìm thấy lịch chạy " + maLich + ".",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Window owner = SwingUtilities.getWindowAncestor(this);
        ChinhSuaLichChayDialog dialog = new ChinhSuaLichChayDialog(owner, lich, this::loadDepartures);
        dialog.setVisible(true);
        // Handoff: double-click bảng khởi hành mở dialog lịch chạy hiện có và reload lại dashboard sau khi lưu.
        // Cảnh báo: dữ liệu item phải giữ maLich ở index 0 vì mã đoàn tàu hiển thị không định danh lịch.
    }
    private String departureStatus(String startText, String endText, String rawStatus) {
        if (rawStatus != null) {
            String normalized = rawStatus.trim().toLowerCase(Locale.ROOT);
            if (normalized.equals("0") || normalized.equals("false") || normalized.contains("ngung") || normalized.contains("ngừng")) {
                return "Tạm ngưng";
            }
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = parseDateTime(startText);
        LocalDateTime end = parseDateTime(endText);
        if (end != null && now.isAfter(end)) return "Đã chạy";
        if (start != null && now.isAfter(start.minusMinutes(15)) && (end == null || now.isBefore(end))) return "Đang lên tàu";
        return "Sắp khởi hành";
        // Handoff: trạng thái vận hành không còn hard-code một nhãn tĩnh.
        // Cảnh báo: parse thất bại sẽ fallback Sắp khởi hành để không làm rỗng dashboard.
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDateTime.parse(value.trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception ignored) {
            return null;
        }
    }

    // ── UI HELPERS ────────────────────────────────────────────────────────────
    private Border cardBorder() {
        return BorderFactory.createCompoundBorder(
            new LineBorder(DIVIDER, 1, true),
            new EmptyBorder(16, 20, 16, 20)
        );
    }

    private JLabel makeSectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(TEXT_SUB);
        return lbl;
    }

    private JTextField makeInput(String placeholder) {
        JTextField f = new JTextField();
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(NotionTheme.BORDER, 1, true),
            new EmptyBorder(7, 10, 7, 10)
        ));
        stabilizeLookupInput(f, 40);
        f.putClientProperty("JTextField.placeholderText", placeholder);
        return f;
    }

    private void stabilizeLookupInput(JTextField field, int height) {
        field.setColumns(1);
        field.setMinimumSize(new Dimension(0, height));
        field.setPreferredSize(new Dimension(0, height));
        // Handoff: Tổng quan dùng grid ngang cố định; input không tự nới theo nội dung khi nhập.
        // Cảnh báo: chỉ áp dụng cho field nằm trong vùng fill ngang, không dùng cho form nhập liệu tự do.
    }

    private void styleCombo(JComboBox<?> cbo) {
        cbo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbo.setBackground(Color.WHITE);
        NotionTheme.applyComboBoxSelection(cbo);
        // Handoff: combo bộ lọc tổng quan giữ LAF gọn nhưng popup selection theo theme tím chung.
        // Rủi ro: nếu đổi renderer sau styleCombo thì cần gọi lại applyComboBoxSelection.
    }

    private JButton makePrimaryBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(PRIMARY);
        btn.setBorder(new EmptyBorder(10, 16, 10, 16));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        return btn;
    }

    private JButton makeSearchActionBtn(String text) {
        JButton btn = makePrimaryBtn(text);
        btn.setPreferredSize(new Dimension(180, 40));
        btn.setMinimumSize(new Dimension(180, 40));
        btn.setMaximumSize(new Dimension(180, 40));
        return btn;
    }

    private JButton makeLinkBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setForeground(PRIMARY);
        btn.setBackground(CARD_BG);
        btn.setBorder(BorderFactory.createEmptyBorder());
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(false);
        return btn;
    }

    private JPanel wrapFieldGroup(String label, Component comp) {
        JPanel g = new JPanel(new BorderLayout(0, 4));
        g.setBackground(CARD_BG);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lbl.setForeground(TEXT_SUB);
        lbl.setBorder(new EmptyBorder(0, 2, 0, 0));
        g.add(lbl,  BorderLayout.NORTH);
        g.add(comp, BorderLayout.CENTER);
        return g;
    }

    private JPanel wrapBtnGroup(JButton btn) {
        JPanel g = new JPanel(new BorderLayout());
        g.setBackground(CARD_BG);
        JLabel spacer = new JLabel(" ");
        spacer.setFont(new Font("Segoe UI", Font.BOLD, 9));
        g.add(spacer, BorderLayout.NORTH);
        g.add(btn,    BorderLayout.CENTER);
        return g;
    }

    private void addLookupField(JPanel row, GridBagConstraints base, int gridx, double weightx, JComponent component) {
        GridBagConstraints gbc = (GridBagConstraints) base.clone();
        gbc.gridx = gridx;
        gbc.weightx = weightx;
        gbc.insets = new Insets(0, 0, 0, weightx == 0 ? 0 : 10);
        if (weightx == 0) {
            gbc.fill = GridBagConstraints.VERTICAL;
            gbc.weightx = 0;
        }
        row.add(component, gbc);
        // Handoff: helper giữ field và nút tìm cùng baseline/kích thước giữa các card tra cứu.
        // Cảnh báo: chỉ dùng cho hàng lookup ngang, không dùng cho grid/card khác.
    }

    private JPanel wrapSearchGroup(String label, JTextField input, JButton btn) {
        JPanel g = new JPanel(new BorderLayout(0, 4));
        g.setBackground(CARD_BG);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lbl.setForeground(TEXT_SUB);
        lbl.setBorder(new EmptyBorder(0, 2, 0, 0));
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setBackground(CARD_BG);
        row.add(input, BorderLayout.CENTER);
        row.add(btn,   BorderLayout.EAST);
        g.add(lbl, BorderLayout.NORTH);
        g.add(row, BorderLayout.CENTER);
        return g;
    }

    private boolean looksLikeTicketKeyword(String keyword) {
        String normalized = keyword == null ? "" : keyword.trim().toUpperCase(Locale.ROOT);
        return normalized.matches("^(VE|V)[A-Z0-9_-]*\\d.*") || normalized.startsWith("TICKET");
        // Handoff: định tuyến nhanh keyword dạng mã vé sang module quản lý vé thay vì hóa đơn.
        // Cảnh báo: heuristic này không query DB; nếu mã vé thực tế khác prefix V/VE thì cần cập nhật.
    }

    // ── INNER: SearchResult ───────────────────────────────────────────────────
    static class SearchResult {
        final String type, id, title, subtitle;
        SearchResult(String t, String id, String title, String sub) {
            this.type = t; this.id = id; this.title = title; this.subtitle = sub;
        }
    }

    // ── INNER: SearchResultRenderer ───────────────────────────────────────────
    static class SearchResultRenderer extends DefaultListCellRenderer {
        private static final Map<String, Color>  COLORS = new LinkedHashMap<>();
        private static final Map<String, String> LABELS = new LinkedHashMap<>();
        static {
            COLORS.put("NV", NotionTheme.ACCENT); LABELS.put("NV", "Nhân viên");
            COLORS.put("KH", AppColors.SUCCESS); LABELS.put("KH", "Khách hàng");
            COLORS.put("HD", AppColors.PRIMARY); LABELS.put("HD", "Hóa đơn");
            COLORS.put("VE", AppColors.WARNING); LABELS.put("VE", "Vé");
            COLORS.put("GA", AppColors.PRIMARY); LABELS.put("GA", "Ga");
            COLORS.put("DT", AppColors.WARNING_DARK); LABELS.put("DT", "Đoàn tàu");
            COLORS.put("KM", AppColors.ERROR); LABELS.put("KM", "Khuyến mãi");
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int idx, boolean sel, boolean foc) {
            SearchResult sr = (SearchResult) value;
            Color bg = sel ? NotionTheme.POPUP_SELECTION : Color.WHITE;

            JPanel panel = new JPanel(new BorderLayout(10, 0));
            panel.setBackground(bg);
            panel.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, NotionTheme.PAGE),
                new EmptyBorder(8, 12, 8, 12)
            ));

            if ("INFO".equals(sr.type)) {
                JLabel lbl = new JLabel(sr.title);
                lbl.setForeground(sel ? NotionTheme.POPUP_SELECTION_TEXT : NotionTheme.TEXT_MUTED);
                lbl.setFont(new Font("Segoe UI", Font.ITALIC, 13));
                panel.add(lbl, BorderLayout.CENTER);
                return panel;
            }

            Color  col  = COLORS.getOrDefault(sr.type, NotionTheme.TEXT_MUTED);
            String name = LABELS.getOrDefault(sr.type, sr.type);

            JLabel badge = new JLabel(name, SwingConstants.CENTER);
            badge.setFont(new Font("Segoe UI", Font.BOLD, 10));
            badge.setForeground(Color.WHITE);
            badge.setBackground(col);
            badge.setOpaque(true);
            badge.setBorder(new EmptyBorder(3, 8, 3, 8));
            badge.setPreferredSize(new Dimension(86, 22));

            JPanel texts = new JPanel(new BorderLayout(0, 2));
            texts.setBackground(bg);

            JLabel lTitle = new JLabel(sr.title + "  (" + sr.id + ")");
            lTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lTitle.setForeground(sel ? NotionTheme.POPUP_SELECTION_TEXT : NotionTheme.TEXT);

            JLabel lSub = new JLabel(sr.subtitle);
            lSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lSub.setForeground(sel ? NotionTheme.POPUP_SELECTION_TEXT : NotionTheme.TEXT_MUTED);

            texts.add(lTitle, BorderLayout.NORTH);
            texts.add(lSub,   BorderLayout.SOUTH);

            panel.add(badge, BorderLayout.WEST);
            panel.add(texts, BorderLayout.CENTER);
            return panel;
        }
    }

    // ── AppModule ─────────────────────────────────────────────────────────────
    @Override public String getTitle() { return "Tổng quan"; }
    @Override public JPanel getView()  { return this; }

    @Override public void setOnResult(Consumer<Object> cb) {
        callback = cb;
        boolean show = cb != null;
        btnSubmit.setVisible(show);
        btnCancel.setVisible(show);
        btnPanel .setVisible(show);
        // Nếu có callback → đây là standalone mode, không cần navCallback
    }

    /** Set callback cho navigation từ Dashboard → MenuModule. */
    public void setNavigationCallback(NavigationCallback cb) {
        this.navCallback = cb;
    }

    @Override public void reset() {
        txtSearch.setText("");
        hidePopup();
        loadKpis();
        loadRecent();
        loadDepartures();
    }
}


