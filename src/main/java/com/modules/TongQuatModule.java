package com.modules;

import com.connectDB.ConnectDB;

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
    private static final Color PRIMARY       = new Color(0x005D90);
    private static final Color PRIMARY_LIGHT = new Color(0xE8F4FB);
    private static final Color SURFACE       = new Color(0xF7F9FB);
    private static final Color SURF_CONT     = new Color(0xECEEF0);
    private static final Color CARD_BG       = Color.WHITE;
    private static final Color TEXT_MAIN     = new Color(0x191C1E);
    private static final Color TEXT_SUB      = new Color(0x404850);
    private static final Color DIVIDER       = new Color(0xBFC7D1);
    private static final Color GREEN         = new Color(0x16A34A);
    private static final Color ORANGE        = new Color(0xEA580C);
    private static final Color PURPLE        = new Color(0x7C3AED);

    // ── State ─────────────────────────────────────────────────────────────────
    private Consumer<Object> callback;
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
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

        searchCard = new JPanel(new BorderLayout(8, 0));
        searchCard.setBackground(CARD_BG);
        searchCard.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(PRIMARY, 2, true),
            new EmptyBorder(8, 14, 8, 14)
        ));

        JLabel icon = new JLabel("\uD83D\uDD0D");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));

        txtSearch = new JTextField();
        txtSearch.setBorder(BorderFactory.createEmptyBorder());
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtSearch.setBackground(CARD_BG);
        txtSearch.setForeground(TEXT_MAIN);
        txtSearch.putClientProperty("JTextField.placeholderText",
            "T\u00ECm ki\u1EBFm nh\u00E2n vi\u00EAn, kh\u00E1ch h\u00E0ng, v\u00E9, h\u00F3a \u0111\u01A1n, ga, \u0111o\u00E0n t\u00E0u...");

        searchCard.add(icon,      BorderLayout.WEST);
        searchCard.add(txtSearch, BorderLayout.CENTER);
        wrapper.add(searchCard, BorderLayout.CENTER);

        // ── Dropdown list ────────────────────────────────────────────────────
        searchModel = new DefaultListModel<>();
        lstResults  = new JList<>(searchModel);
        lstResults.setCellRenderer(new SearchResultRenderer());
        lstResults.setBackground(CARD_BG);
        lstResults.setFixedCellHeight(58);
        lstResults.setSelectionBackground(PRIMARY_LIGHT);
        lstResults.setSelectionForeground(TEXT_MAIN);
        lstResults.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                hidePopup();
            }
        });
        lstResults.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE
                    || e.getKeyCode() == KeyEvent.VK_ENTER) {
                    hidePopup();
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

        return wrapper;
    }

    // ── TRA CỨU VÉ NHANH ─────────────────────────────────────────────────────
    private JPanel buildTicketSearchCard() {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(CARD_BG);
        card.setBorder(cardBorder());
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        card.add(makeSectionLabel("TRA C\u1EACU V\u00C9 NHANH"), BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridLayout(1, 5, 10, 0));
        fields.setBackground(CARD_BG);

        JTextField txtGaDi  = makeInput("T\u1EEB \u0111\u00E2u?");
        JTextField txtGaDen = makeInput("\u0110i\u1EC3m \u0111\u1EBFn");
        JTextField txtNgay  = makeInput("YYYY-MM-DD");
        JComboBox<String> cbo = new JComboBox<>(new String[]{
            "H\u1EA1ng Ph\u1ED5 th\u00F4ng", "H\u1EA1ng Th\u01B0\u01A1ng nh\u00E2n",
            "H\u1EA1ng Nh\u1EA5t", "Nh\u00F3m (5+)"
        });
        styleCombo(cbo);
        JButton btn = makePrimaryBtn("T\u00ECm v\u00E9");

        btn.addActionListener(e -> {
            String gaDi  = txtGaDi.getText().trim();
            String gaDen = txtGaDen.getText().trim();
            String query = (gaDi + " " + gaDen).trim();
            if (query.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Vui l\u00F2ng nh\u1EADp ga \u0111i ho\u1EB7c ga \u0111\u1EBFn.",
                    "Th\u00F4ng b\u00E1o", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            txtSearch.setText(query);
            doSearch(query);
        });

        fields.add(wrapFieldGroup("GA \u0110I",     txtGaDi));
        fields.add(wrapFieldGroup("GA \u0110\u1EAEN", txtGaDen));
        fields.add(wrapFieldGroup("NG\u00C0Y \u0110I",   txtNgay));
        fields.add(wrapFieldGroup("LO\u1EA0I GH\u1EBE", cbo));
        fields.add(wrapBtnGroup(btn));

        card.add(fields, BorderLayout.CENTER);
        return card;
    }

    // ── TRA CỨU LỊCH TRÌNH NHANH ─────────────────────────────────────────────
    private JPanel buildScheduleSearchCard() {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(CARD_BG);
        card.setBorder(cardBorder());
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        card.add(makeSectionLabel("TRA C\u1EACU L\u1ECACH TR\u00CCNH NHANH"), BorderLayout.NORTH);

        JPanel row = new JPanel(new GridLayout(1, 2, 16, 0));
        row.setBackground(CARD_BG);

        JTextField txtHD = makeInput("VD: HD-001");
        JButton btnHD = makeSecBtn("T\u00CCM");
        btnHD.addActionListener(e -> {
            String ma = txtHD.getText().trim();
            if (ma.isEmpty()) return;
            txtSearch.setText(ma);
            doSearch(ma);
        });
        row.add(wrapSearchGroup("NH\u1EAAP M\u00C3 H\u00D3A \u0110\u01A0N", txtHD, btnHD));

        JTextField txtVe = makeInput("VD: VE-001");
        JButton btnVe = makeSecBtn("T\u00CCM");
        btnVe.addActionListener(e -> {
            String ma = txtVe.getText().trim();
            if (ma.isEmpty()) return;
            txtSearch.setText(ma);
            doSearch(ma);
        });
        row.add(wrapSearchGroup("NH\u1EAAP M\u00C3 V\u00C9", txtVe, btnVe));

        card.add(row, BorderLayout.CENTER);
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

        row.add(kpiCard("Nh\u00E2n vi\u00EAn",                    lblNhanVien,  PRIMARY));
        row.add(kpiCard("Kh\u00E1ch h\u00E0ng",                   lblKhachHang, GREEN));
        row.add(kpiCard("V\u00E9 \u0111\u00E3 b\u00E1n",          lblVeBan,     ORANGE));
        row.add(kpiCard("H\u00F3a \u0111\u01A1n h\u00F4m nay",    lblHoaDon,    PURPLE));
        return row;
    }

    private JPanel kpiCard(String label, JLabel valueLbl, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(DIVIDER, 1, true),
            new EmptyBorder(12, 16, 12, 16)
        ));

        JPanel bar = new JPanel();
        bar.setBackground(accent);
        bar.setPreferredSize(new Dimension(4, 1));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(TEXT_SUB);

        valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valueLbl.setForeground(accent);

        JPanel txt = new JPanel(new BorderLayout(0, 3));
        txt.setBackground(CARD_BG);
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
        JLabel title = new JLabel("GIAO D\u1ECACH G\u1EA6N \u0110\u00C2Y");
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(TEXT_MAIN);
        JButton btnExport = makeLinkBtn("Xu\u1EA5t nh\u1EADt k\u00FD ng\u00E0y");
        hdr.add(title,     BorderLayout.WEST);
        hdr.add(btnExport, BorderLayout.EAST);
        card.add(hdr, BorderLayout.NORTH);

        // Table
        String[] cols = {
            "M\u00C3 H\u00D3A \u0110\u01A0N",
            "KH\u00C1CH H\u00C0NG",
            "NG\u00C0Y L\u1EAAP",
            "S\u1ED0 V\u00C9",
            "T\u1ED4NG TI\u1EC0N"
        };
        recentTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        recentTable = new JTable(recentTableModel);
        recentTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        recentTable.setRowHeight(40);
        recentTable.setShowGrid(false);
        recentTable.setIntercellSpacing(new Dimension(0, 0));
        recentTable.setSelectionBackground(PRIMARY_LIGHT);
        recentTable.setSelectionForeground(TEXT_MAIN);
        recentTable.setFillsViewportHeight(true);
        recentTable.setBackground(CARD_BG);

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
                    setBackground(row % 2 == 0 ? CARD_BG : new Color(0xF8FAFC));
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

        JLabel syncLbl = new JLabel("\u25CF  Th\u1EDDi gian h\u1EC7 th\u1ED1ng \u0111\u1ED3ng b\u1ED9");
        syncLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        syncLbl.setForeground(new Color(0x22C55E));
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
        JLabel title = new JLabel("B\u1EA2NG KH\u1EDFI H\u00C0NH");
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(TEXT_MAIN);
        JLabel liveLbl = new JLabel("\u25CF Live");
        liveLbl.setFont(new Font("Segoe UI", Font.BOLD, 9));
        liveLbl.setForeground(new Color(0x22C55E));
        hdr.add(title,   BorderLayout.WEST);
        hdr.add(liveLbl, BorderLayout.EAST);
        card.add(hdr, BorderLayout.NORTH);

        // Departure items
        departurePanel = new JPanel();
        departurePanel.setLayout(new BoxLayout(departurePanel, BoxLayout.Y_AXIS));
        departurePanel.setBackground(CARD_BG);

        JLabel loading = new JLabel("\u0110ang t\u1EA3i l\u1ECBch tr\u00ECnh...");
        loading.setForeground(TEXT_SUB);
        loading.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        loading.setAlignmentX(Component.LEFT_ALIGNMENT);
        departurePanel.add(loading);
        card.add(departurePanel, BorderLayout.CENTER);

        // View all button
        JButton btnAll = new JButton("XEM TO\u00C0N B\u1ED8 L\u1ECACH TR\u00CCNH");
        btnAll.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnAll.setForeground(TEXT_SUB);
        btnAll.setBackground(CARD_BG);
        btnAll.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(DIVIDER, 1, true),
            new EmptyBorder(8, 12, 8, 12)
        ));
        btnAll.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAll.setFocusPainted(false);
        card.add(btnAll, BorderLayout.SOUTH);

        return card;
    }

    // ── BUTTON PANEL ──────────────────────────────────────────────────────────
    private JPanel buildBtnPanel() {
        btnSubmit = new JButton("X\u00E1c nh\u1EADn");
        btnCancel = new JButton("H\u1EE7y");
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
            "Ch\u1EE7 Nh\u1EADt", "Th\u1EE9 Hai", "Th\u1EE9 Ba",
            "Th\u1EE9 T\u01B0",   "Th\u1EE9 N\u0103m", "Th\u1EE9 S\u00E1u", "Th\u1EE9 B\u1EA3y"
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
                            "INFO", "Kh\u00F4ng t\u00ECm th\u1EA5y k\u1EBFt qu\u1EA3", "", ""));
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
                rs.getString("vaiTro") + "  \u00B7  " + rs.getString("soDienThoai")),
            p, p, p);

        search(con, out,
            "SELECT TOP 4 maKhachHang, hoTen, soDienThoai " +
            "FROM KhachHang WHERE maKhachHang LIKE ? OR hoTen LIKE ? OR soDienThoai LIKE ?",
            rs -> new SearchResult("KH", rs.getString("maKhachHang"), rs.getString("hoTen"),
                "SDT: " + rs.getString("soDienThoai")),
            p, p, p);

        search(con, out,
            "SELECT TOP 4 maHoaDon, maNV, CONVERT(varchar,ngayLap,120) AS nd " +
            "FROM HoaDon WHERE maHoaDon LIKE ?",
            rs -> new SearchResult("HD", rs.getString("maHoaDon"),
                "H\u00F3a \u0111\u01A1n " + rs.getString("maHoaDon"),
                "NV: " + rs.getString("maNV") + "  \u00B7  " + clip(rs.getString("nd"), 10)),
            p);

        search(con, out,
            "SELECT TOP 4 maVe, trangThai FROM Ve WHERE maVe LIKE ?",
            rs -> new SearchResult("VE", rs.getString("maVe"),
                "V\u00E9 " + rs.getString("maVe"),
                "Tr\u1EA1ng th\u00E1i: " + rs.getString("trangThai")),
            p);

        search(con, out,
            "SELECT TOP 4 maGa, tenGa, diaChi FROM Ga WHERE maGa LIKE ? OR tenGa LIKE ?",
            rs -> new SearchResult("GA", rs.getString("maGa"), rs.getString("tenGa"),
                rs.getString("diaChi")),
            p, p);

        search(con, out,
            "SELECT TOP 4 maDoanTau, tenDoanTau " +
            "FROM DoanTau WHERE maDoanTau LIKE ? OR tenDoanTau LIKE ?",
            rs -> new SearchResult("DT", rs.getString("maDoanTau"),
                rs.getString("tenDoanTau"),
                "M\u00E3: " + rs.getString("maDoanTau")),
            p, p);

        search(con, out,
            "SELECT TOP 4 maKhuyenMai, tenKhuyenMai, trangThai " +
            "FROM KhuyenMai WHERE maKhuyenMai LIKE ? OR tenKhuyenMai LIKE ?",
            rs -> new SearchResult("KM", rs.getString("maKhuyenMai"),
                rs.getString("tenKhuyenMai"),
                rs.getBoolean("trangThai") ? "Ho\u1EA1t \u0111\u1ED9ng" : "D\u1EEBng"),
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
                    "SELECT TOP 15 hd.maHoaDon, kh.hoTen AS tenKH, " +
                    "  CONVERT(varchar,hd.ngayLap,120) AS nd, " +
                    "  COUNT(ct.maChiTietHD) AS soVe, " +
                    "  ISNULL(SUM(ct.giaTien),0) AS tong " +
                    "FROM HoaDon hd " +
                    "LEFT JOIN KhachHang kh ON hd.maKhachHang=kh.maKhachHang " +
                    "LEFT JOIN ChiTietHoaDon ct ON hd.maHoaDon=ct.maHoaDon " +
                    "GROUP BY hd.maHoaDon, kh.hoTen, hd.ngayLap " +
                    "ORDER BY hd.ngayLap DESC";
                try (PreparedStatement ps = con.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
                    while (rs.next()) {
                        String tenKH = rs.getString("tenKH");
                        if (tenKH == null) tenKH = "Kh\u00E1ch v\u00E3ng lai";
                        java.math.BigDecimal tong = rs.getBigDecimal("tong");
                        rows.add(new Object[]{
                            rs.getString("maHoaDon"),
                            tenKH,
                            clip(rs.getString("nd"), 16),
                            rs.getInt("soVe") + " v\u00E9",
                            (tong != null ? nf.format(tong) : "0") + " \u20AB"
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
                    "SELECT TOP 5 l.maDoanTau," +
                    "  ISNULL(dt.tenDoanTau, l.maDoanTau) AS tenDoan," +
                    "  ISNULL(g1.tenGa, ISNULL(t.gaDi,'?')) AS gaDi," +
                    "  ISNULL(g2.tenGa, ISNULL(t.gaDen,'?')) AS gaDen," +
                    "  CONVERT(varchar, l.thoiGianBatDau, 108) AS gio" +
                    " FROM Lich l" +
                    " LEFT JOIN Tuyen t    ON l.maTuyen    = t.maTuyen" +
                    " LEFT JOIN Ga g1      ON t.gaDi       = g1.maGa" +
                    " LEFT JOIN Ga g2      ON t.gaDen      = g2.maGa" +
                    " LEFT JOIN DoanTau dt ON l.maDoanTau  = dt.maDoanTau" +
                    " ORDER BY l.thoiGianBatDau ASC";
                try (PreparedStatement ps = con.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        results.add(new String[]{
                            rs.getString("maDoanTau"),
                            rs.getString("tenDoan"),
                            rs.getString("gaDi"),
                            rs.getString("gaDen"),
                            rs.getString("gio")
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
                        JLabel empty = new JLabel("Kh\u00F4ng c\u00F3 l\u1ECBch tr\u00ECnh n\u00E0o.");
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
        // d: [maDoanTau, tenDoanTau, gaDi, gaDen, gio]
        String code  = d[0] != null ? d[0] : "?";
        String gaDi  = d[2] != null ? d[2] : "?";
        String gaDen = d[3] != null ? d[3] : "?";
        String gio   = d[4] != null ? d[4].substring(0, Math.min(5, d[4].length())) : "--:--";
        String badge = code.length() > 6 ? code.substring(0, 6) : code;

        JPanel item = new JPanel(new BorderLayout(12, 0));
        item.setBackground(SURF_CONT);
        item.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(0xE0E3E5), 1, true),
            new EmptyBorder(10, 12, 10, 12)
        ));
        item.setAlignmentX(Component.LEFT_ALIGNMENT);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));

        // Badge
        JLabel badgeLbl = new JLabel(badge, SwingConstants.CENTER);
        badgeLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        badgeLbl.setForeground(PRIMARY);
        badgeLbl.setBackground(PRIMARY_LIGHT);
        badgeLbl.setOpaque(true);
        badgeLbl.setBorder(new LineBorder(new Color(0xC8E6F7), 1));
        badgeLbl.setPreferredSize(new Dimension(52, 52));

        // Center
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(SURF_CONT);

        JLabel routeLbl = new JLabel(gaDi + " \u2192 " + gaDen);
        routeLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        routeLbl.setForeground(TEXT_MAIN);

        JLabel infoLbl = new JLabel("T\u00E0u: " + code);
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

        JLabel statusLbl = new JLabel("S\u1EAFp kh\u1EF9i h\u00E0nh", SwingConstants.RIGHT);
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
            new LineBorder(new Color(0xE0E3E5), 1, true),
            new EmptyBorder(7, 10, 7, 10)
        ));
        f.putClientProperty("JTextField.placeholderText", placeholder);
        return f;
    }

    private void styleCombo(JComboBox<?> cbo) {
        cbo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbo.setBackground(Color.WHITE);
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

    private JButton makeSecBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(new Color(0x4C616C));
        btn.setBackground(new Color(0xCFE6F2));
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
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
            COLORS.put("NV", new Color(0x005D90)); LABELS.put("NV", "Nh\u00E2n vi\u00EAn");
            COLORS.put("KH", new Color(0x16A34A)); LABELS.put("KH", "Kh\u00E1ch h\u00E0ng");
            COLORS.put("HD", new Color(0x7C3AED)); LABELS.put("HD", "H\u00F3a \u0111\u01A1n");
            COLORS.put("VE", new Color(0xEA580C)); LABELS.put("VE", "V\u00E9");
            COLORS.put("GA", new Color(0x0891B2)); LABELS.put("GA", "Ga");
            COLORS.put("DT", new Color(0xB45309)); LABELS.put("DT", "\u0110o\u00E0n t\u00E0u");
            COLORS.put("KM", new Color(0xDB2777)); LABELS.put("KM", "Khuy\u1EBFn m\u00E3i");
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int idx, boolean sel, boolean foc) {
            SearchResult sr = (SearchResult) value;
            Color bg = sel ? new Color(0xE8F4FB) : Color.WHITE;

            JPanel panel = new JPanel(new BorderLayout(10, 0));
            panel.setBackground(bg);
            panel.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(0xF3F4F6)),
                new EmptyBorder(8, 12, 8, 12)
            ));

            if ("INFO".equals(sr.type)) {
                JLabel lbl = new JLabel(sr.title);
                lbl.setForeground(new Color(0x6B7280));
                lbl.setFont(new Font("Segoe UI", Font.ITALIC, 13));
                panel.add(lbl, BorderLayout.CENTER);
                return panel;
            }

            Color  col  = COLORS.getOrDefault(sr.type, new Color(0x6B7280));
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
            lTitle.setForeground(new Color(0x1A1A2E));

            JLabel lSub = new JLabel(sr.subtitle);
            lSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lSub.setForeground(new Color(0x6B7280));

            texts.add(lTitle, BorderLayout.NORTH);
            texts.add(lSub,   BorderLayout.SOUTH);

            panel.add(badge, BorderLayout.WEST);
            panel.add(texts, BorderLayout.CENTER);
            return panel;
        }
    }

    // ── AppModule ─────────────────────────────────────────────────────────────
    @Override public String getTitle() { return "T\u1ED5ng quan"; }
    @Override public JPanel getView()  { return this; }

    @Override public void setOnResult(Consumer<Object> cb) {
        callback = cb;
        boolean show = cb != null;
        btnSubmit.setVisible(show);
        btnCancel.setVisible(show);
        btnPanel .setVisible(show);
    }

    @Override public void reset() {
        txtSearch.setText("");
        hidePopup();
        loadKpis();
        loadRecent();
        loadDepartures();
    }
}
