package com.modules;

import com.dao.DAO_KhachHang;
import com.entity.KhachHang;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

/**
 * Bước 5 — Nhập thông tin khách hàng (hỗ trợ nhiều khách cho cùng một hóa đơn).
 *
 * Mỗi khách hàng là một card với đầy đủ 9 trường của entity KhachHang.
 * Nút "+ Thêm khách hàng" thêm card mới. Tối thiểu 1 card.
 * SĐT có autocomplete popup — chọn một gợi ý sẽ điền sạch toàn bộ field
 * của card đó (không nối chuỗi).
 *
 * Output: List<KhachHang>
 *   - maKhachHang != null → khách cũ (saveTransaction sẽ update)
 *   - maKhachHang == null → khách mới (saveTransaction sẽ insert)
 */
public class BanVeStep5Module extends JPanel implements AppModule {

    private Consumer<Object> callback;
    private final DAO_KhachHang daoKH = new DAO_KhachHang();

    // Design tokens
    private static final Color PRIMARY       = new Color(0x00, 0x5D, 0x90);
    private static final Color SURFACE       = new Color(0xF8, 0xFA, 0xFC);
    private static final Color CARD_BG       = Color.WHITE;
    private static final Color ON_SURFACE    = new Color(0x1A, 0x1D, 0x21);
    private static final Color ON_SURF_VAR   = new Color(0x5F, 0x67, 0x70);
    private static final Color OUTLINE       = new Color(0xDE, 0xE3, 0xE8);
    private static final Color PRIMARY_LIGHT = new Color(0xE3, 0xF2, 0xFD);
    private static final Color ERROR_FG      = new Color(0xBA, 0x1A, 0x1A);

    // Layout holders
    private JPanel customersHolder;
    private JButton btnAddCustomer;

    // Autocomplete popup (dùng chung cho tất cả card — bám theo card đang focus)
    private JWindow           suggestionWindow;
    private JList<KhachHang>  suggestionList;
    private CustomerCard      activeCard;  // card đang mở popup

    // AppModule buttons
    private JButton btnSubmit, btnCancel;
    private JPanel  btnPanel;

    // Danh sách card hiện có
    private final List<CustomerCard> cards = new ArrayList<>();

    // =========================================================================
    //  CONSTRUCTOR
    // =========================================================================

    public BanVeStep5Module() {
        setLayout(new BorderLayout());
        setBackground(SURFACE);
        buildUI();
    }

    // =========================================================================
    //  BUILD UI
    // =========================================================================

    private void buildUI() {
        add(buildHeaderBar(), BorderLayout.NORTH);

        customersHolder = new JPanel();
        customersHolder.setLayout(new BoxLayout(customersHolder, BoxLayout.Y_AXIS));
        customersHolder.setBackground(SURFACE);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(SURFACE);
        center.setBorder(new EmptyBorder(24, 80, 24, 80));

        center.add(customersHolder);
        center.add(Box.createVerticalStrut(12));

        btnAddCustomer = new JButton("+ Thêm khách hàng");
        btnAddCustomer.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAddCustomer.setForeground(PRIMARY);
        btnAddCustomer.setBackground(CARD_BG);
        btnAddCustomer.setFocusPainted(false);
        btnAddCustomer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAddCustomer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createDashedBorder(PRIMARY, 1.5f, 4, 3, true),
                new EmptyBorder(10, 16, 10, 16)));
        btnAddCustomer.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnAddCustomer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btnAddCustomer.addActionListener(e -> addNewCard());
        center.add(btnAddCustomer);

        JScrollPane scroll = new JScrollPane(center);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(SURFACE);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        add(scroll, BorderLayout.CENTER);

        btnSubmit = new JButton("Tiếp theo →");
        styleBtn(btnSubmit, true);
        btnSubmit.addActionListener(e -> execute());

        btnCancel = new JButton("← Quay lại");
        styleBtn(btnCancel, false);
        btnCancel.addActionListener(e -> { if (callback != null) callback.accept(null); });

        btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        btnPanel.setBackground(SURFACE);
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, OUTLINE));
        btnPanel.add(btnCancel);
        btnPanel.add(btnSubmit);
        btnPanel.setVisible(false);
        add(btnPanel, BorderLayout.SOUTH);

        // Seed: 1 card mặc định
        addNewCard();
    }

    private JPanel buildHeaderBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 12));
        bar.setBackground(PRIMARY_LIGHT);
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, OUTLINE));
        JLabel lbl = new JLabel("Thông tin khách hàng");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl.setForeground(PRIMARY);
        bar.add(lbl);
        return bar;
    }

    // =========================================================================
    //  CUSTOMER CARD MANAGEMENT
    // =========================================================================

    private void addNewCard() {
        CustomerCard card = new CustomerCard(cards.size() + 1);
        cards.add(card);
        customersHolder.add(card);
        customersHolder.add(Box.createVerticalStrut(12));
        updateCardHeaders();
        customersHolder.revalidate();
        customersHolder.repaint();
    }

    private void removeCard(CustomerCard card) {
        if (cards.size() <= 1) return; // min 1
        int idx = customersHolder.getComponentZOrder(card);
        cards.remove(card);
        customersHolder.remove(card);
        // remove strut ngay dưới card (nếu có)
        if (idx < customersHolder.getComponentCount()) {
            customersHolder.remove(idx);
        }
        updateCardHeaders();
        customersHolder.revalidate();
        customersHolder.repaint();
    }

    private void updateCardHeaders() {
        for (int i = 0; i < cards.size(); i++) {
            cards.get(i).setIndex(i + 1, cards.size() > 1);
        }
    }

    // =========================================================================
    //  AUTOCOMPLETE POPUP (shared)
    // =========================================================================

    private void ensureSuggestionWindow() {
        if (suggestionWindow != null) return;

        suggestionList = new JList<>();
        suggestionList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        suggestionList.setCellRenderer(new KhachHangRenderer());
        suggestionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        suggestionList.setFixedCellHeight(36);
        suggestionList.setBackground(CARD_BG);

        suggestionList.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                KhachHang kh = suggestionList.getSelectedValue();
                if (kh != null && activeCard != null) {
                    activeCard.fillFromCustomer(kh);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(suggestionList);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xB0, 0xC4, 0xDE), 1));
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        Window topLevel = SwingUtilities.getWindowAncestor(this);
        suggestionWindow = new JWindow(topLevel);
        suggestionWindow.setFocusableWindowState(false);
        suggestionWindow.setContentPane(scroll);
    }

    private void showSuggestions(CustomerCard card, List<KhachHang> items) {
        ensureSuggestionWindow();
        activeCard = card;
        suggestionList.setListData(items.toArray(new KhachHang[0]));

        try {
            Point loc = card.txSdt.getLocationOnScreen();
            int w = card.txSdt.getWidth();
            int rows = Math.min(items.size(), 5);
            int h = rows * 36 + 6;

            suggestionWindow.setSize(w, h);
            suggestionWindow.setLocation(loc.x, loc.y + card.txSdt.getHeight());
            suggestionWindow.setVisible(true);
        } catch (IllegalComponentStateException ex) {
            // component not yet on screen
        }
    }

    private void hideSuggestions() {
        if (suggestionWindow != null) suggestionWindow.setVisible(false);
    }

    // =========================================================================
    //  EXECUTE — VALIDATE & OUTPUT
    // =========================================================================

    private void execute() {
        CustomerCard firstBadCard = null;
        JComponent firstBadField = null;

        // Reset tất cả lỗi
        for (CustomerCard c : cards) c.clearAllErrors();

        // Validate per-card
        for (CustomerCard c : cards) {
            JComponent bad = c.validateFields();
            if (bad != null && firstBadField == null) {
                firstBadCard  = c;
                firstBadField = bad;
            }
        }

        // Chặn trùng SĐT / CCCD giữa các card
        if (firstBadField == null) {
            for (int i = 0; i < cards.size(); i++) {
                String sdtI  = cards.get(i).txSdt.getText().trim();
                String cccdI = cards.get(i).txCccd.getText().trim();
                for (int j = i + 1; j < cards.size(); j++) {
                    if (sdtI.equals(cards.get(j).txSdt.getText().trim())) {
                        cards.get(j).showFieldError(cards.get(j).txSdt, cards.get(j).errSdt,
                                "Trùng SĐT với khách hàng khác trong hóa đơn");
                        if (firstBadField == null) {
                            firstBadCard  = cards.get(j);
                            firstBadField = cards.get(j).txSdt;
                        }
                    }
                    if (cccdI.equals(cards.get(j).txCccd.getText().trim())) {
                        cards.get(j).showFieldError(cards.get(j).txCccd, cards.get(j).errCccd,
                                "Trùng CCCD với khách hàng khác trong hóa đơn");
                        if (firstBadField == null) {
                            firstBadCard  = cards.get(j);
                            firstBadField = cards.get(j).txCccd;
                        }
                    }
                }
            }
        }

        if (firstBadField != null) {
            firstBadField.requestFocusInWindow();
            if (firstBadCard != null) firstBadCard.scrollRectToVisible(firstBadCard.getBounds());
            return;
        }

        // Tạo list KhachHang output
        List<KhachHang> result = new ArrayList<>();
        for (CustomerCard c : cards) result.add(c.toKhachHang());

        if (callback != null) callback.accept(result);
    }

    // =========================================================================
    //  HELPERS
    // =========================================================================

    private void styleBtn(JButton btn, boolean primary) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(9, 22, 9, 22));
        if (primary) {
            btn.setBackground(PRIMARY); btn.setForeground(Color.WHITE); btn.setOpaque(true);
        } else {
            btn.setBackground(CARD_BG); btn.setForeground(ON_SURF_VAR); btn.setOpaque(true);
        }
    }

    class KhachHangRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof KhachHang kh)
                setText(kh.getSoDienThoai() + "  —  " + kh.getHoTen());
            setBorder(new EmptyBorder(4, 10, 4, 10));
            return this;
        }
    }

    // =========================================================================
    //  AppModule
    // =========================================================================

    @Override public String getTitle() { return "Bước 5 – Khách hàng"; }
    @Override public JPanel getView()  { return this; }

    @Override
    public void setOnResult(Consumer<Object> cb) {
        this.callback = cb;
        boolean show = cb != null;
        btnSubmit.setVisible(show);
        btnCancel.setVisible(show);
        btnPanel.setVisible(show);
    }

    @Override
    public void reset() {
        cards.clear();
        customersHolder.removeAll();
        hideSuggestions();
        addNewCard();
    }

    // =========================================================================
    //  INNER CLASS — CustomerCard
    // =========================================================================

    private class CustomerCard extends JPanel {
        JTextField txSdt, txHoTen, txCccd, txEmail, txQuocTich, txThuongTru, txTamTru;
        JSpinner   spNgaySinh;
        JComboBox<String> cbGioiTinh;
        JLabel     errSdt, errHoTen, errCccd;
        JLabel     headerLbl;
        JButton    btnRemove;
        Border     defaultFieldBorder;

        KhachHang foundKhachHang = null;
        boolean   fillingFromSuggestion = false;

        CustomerCard(int index) {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBackground(CARD_BG);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(OUTLINE, 1),
                    new EmptyBorder(18, 24, 18, 24)));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
            buildCard(index);
        }

        void setIndex(int index, boolean canRemove) {
            headerLbl.setText("Khách hàng #" + index);
            btnRemove.setVisible(canRemove);
        }

        private void buildCard(int index) {
            // Header: "Khách hàng #N" + nút Xóa (ẩn với card #1 khi chỉ có 1 card)
            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(CARD_BG);
            header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            header.setAlignmentX(Component.LEFT_ALIGNMENT);

            headerLbl = new JLabel("Khách hàng #" + index);
            headerLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
            headerLbl.setForeground(PRIMARY);
            header.add(headerLbl, BorderLayout.WEST);

            btnRemove = new JButton("✕ Xóa khách");
            btnRemove.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            btnRemove.setForeground(ERROR_FG);
            btnRemove.setBackground(CARD_BG);
            btnRemove.setFocusPainted(false);
            btnRemove.setBorder(new EmptyBorder(6, 12, 6, 12));
            btnRemove.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnRemove.setVisible(false);
            btnRemove.addActionListener(e -> removeCard(this));
            header.add(btnRemove, BorderLayout.EAST);

            add(header);
            add(Box.createVerticalStrut(14));

            // --- Row 1: SĐT + Họ tên ---
            txSdt   = makeTextField();
            txHoTen = makeTextField();
            errSdt   = makeErrLabel();
            errHoTen = makeErrLabel();
            defaultFieldBorder = txSdt.getBorder();

            add(makeRowPair("Số điện thoại *", txSdt, errSdt, "Họ và tên *", txHoTen, errHoTen));
            add(Box.createVerticalStrut(10));

            // --- Row 2: CCCD + Email ---
            txCccd  = makeTextField();
            txEmail = makeTextField();
            errCccd = makeErrLabel();
            add(makeRowPair("CCCD *", txCccd, errCccd, "Email", txEmail, null));
            add(Box.createVerticalStrut(10));

            // --- Row 3: Ngày sinh + Giới tính + Quốc tịch ---
            spNgaySinh = new JSpinner(new SpinnerDateModel());
            JSpinner.DateEditor ed = new JSpinner.DateEditor(spNgaySinh, "dd/MM/yyyy");
            spNgaySinh.setEditor(ed);
            spNgaySinh.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            spNgaySinh.setPreferredSize(new Dimension(160, 40));
            spNgaySinh.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));
            spNgaySinh.setValue(java.sql.Date.valueOf(LocalDate.of(2000, 1, 1)));

            cbGioiTinh = new JComboBox<>(new String[]{"Nam", "Nữ"});
            cbGioiTinh.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            cbGioiTinh.setPreferredSize(new Dimension(120, 40));
            cbGioiTinh.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));

            txQuocTich = makeTextField();
            txQuocTich.setText("Việt Nam");

            add(makeRowTriple("Ngày sinh", spNgaySinh, "Giới tính", cbGioiTinh, "Quốc tịch", txQuocTich));
            add(Box.createVerticalStrut(10));

            // --- Row 4: Địa chỉ thường trú ---
            txThuongTru = makeTextField();
            add(makeSingleRow("Địa chỉ thường trú", txThuongTru));
            add(Box.createVerticalStrut(10));

            // --- Row 5: Địa chỉ tạm trú ---
            txTamTru = makeTextField();
            add(makeSingleRow("Địa chỉ tạm trú", txTamTru));

            // --- Listeners ---
            txSdt.getDocument().addDocumentListener(new DocumentListener() {
                @Override public void insertUpdate(DocumentEvent e)  { onSdtChanged(); clearFieldError(txSdt, errSdt); }
                @Override public void removeUpdate(DocumentEvent e)  { onSdtChanged(); clearFieldError(txSdt, errSdt); }
                @Override public void changedUpdate(DocumentEvent e) { onSdtChanged(); clearFieldError(txSdt, errSdt); }
            });
            txHoTen.getDocument().addDocumentListener(new DocumentListener() {
                @Override public void insertUpdate(DocumentEvent e)  { clearFieldError(txHoTen, errHoTen); }
                @Override public void removeUpdate(DocumentEvent e)  { clearFieldError(txHoTen, errHoTen); }
                @Override public void changedUpdate(DocumentEvent e) { clearFieldError(txHoTen, errHoTen); }
            });
            txCccd.getDocument().addDocumentListener(new DocumentListener() {
                @Override public void insertUpdate(DocumentEvent e)  { clearFieldError(txCccd, errCccd); }
                @Override public void removeUpdate(DocumentEvent e)  { clearFieldError(txCccd, errCccd); }
                @Override public void changedUpdate(DocumentEvent e) { clearFieldError(txCccd, errCccd); }
            });
            txSdt.addFocusListener(new FocusAdapter() {
                @Override public void focusLost(FocusEvent e) {
                    SwingUtilities.invokeLater(() -> {
                        if (activeCard == CustomerCard.this) hideSuggestions();
                    });
                }
            });
        }

        private void onSdtChanged() {
            if (fillingFromSuggestion) return;

            // Reset state khi user gõ tay
            foundKhachHang = null;

            String sdt = txSdt.getText().trim();
            if (sdt.length() < 6) {
                if (activeCard == this) hideSuggestions();
                return;
            }

            List<KhachHang> results = daoKH.findBySoDienThoaiLike(sdt);
            if (results.isEmpty()) {
                if (activeCard == this) hideSuggestions();
                return;
            }
            showSuggestions(this, results);
        }

        /**
         * Điền thông tin khách hàng từ gợi ý.
         * Đảm bảo XÓA TRẮNG từng field trước khi set để tránh cảm giác "nối chuỗi".
         */
        void fillFromCustomer(KhachHang kh) {
            fillingFromSuggestion = true;
            SwingUtilities.invokeLater(() -> {
                try {
                    // Xóa trắng rồi set giá trị mới cho mỗi ô, đặt caret về đầu
                    setFieldText(txSdt,   safe(kh.getSoDienThoai()));
                    setFieldText(txHoTen, safe(kh.getHoTen()));
                    setFieldText(txCccd,  safe(kh.getCccd()));
                    setFieldText(txEmail, safe(kh.getEmail()));
                    setFieldText(txQuocTich, kh.getQuocTich() != null ? kh.getQuocTich() : "Việt Nam");
                    setFieldText(txThuongTru, safe(kh.getDiaChiThuongTru()));
                    setFieldText(txTamTru,    safe(kh.getDiaChiTamTru()));

                    if (kh.getNgaySinh() != null) {
                        spNgaySinh.setValue(java.sql.Date.valueOf(kh.getNgaySinh()));
                    }
                    if ("NAM".equalsIgnoreCase(kh.getGioiTinh()) || "Nam".equalsIgnoreCase(kh.getGioiTinh())) {
                        cbGioiTinh.setSelectedIndex(0);
                    } else if ("NU".equalsIgnoreCase(kh.getGioiTinh()) || "Nữ".equalsIgnoreCase(kh.getGioiTinh())) {
                        cbGioiTinh.setSelectedIndex(1);
                    }

                    foundKhachHang = kh;
                    hideSuggestions();
                    clearAllErrors();
                } finally {
                    fillingFromSuggestion = false;
                }
            });
        }

        private void setFieldText(JTextField f, String v) {
            f.setText("");
            f.setText(v);
            f.setCaretPosition(0);
        }

        private String safe(String s) { return s == null ? "" : s; }

        /**
         * Validate các trường bắt buộc. Trả về component đầu tiên có lỗi (để focus),
         * hoặc null nếu OK.
         */
        JComponent validateFields() {
            JComponent firstBad = null;
            String sdt   = txSdt.getText().trim();
            String hoTen = txHoTen.getText().trim();
            String cccd  = txCccd.getText().trim();

            if (sdt.isEmpty()) {
                showFieldError(txSdt, errSdt, "Vui lòng nhập số điện thoại");
                firstBad = txSdt;
            } else if (!sdt.matches("0\\d{9}")) {
                showFieldError(txSdt, errSdt, "Số điện thoại phải gồm 10 chữ số, bắt đầu bằng 0");
                if (firstBad == null) firstBad = txSdt;
            }

            if (hoTen.isEmpty()) {
                showFieldError(txHoTen, errHoTen, "Vui lòng nhập họ và tên");
                if (firstBad == null) firstBad = txHoTen;
            } else if (hoTen.length() > 100) {
                showFieldError(txHoTen, errHoTen, "Họ tên tối đa 100 ký tự");
                if (firstBad == null) firstBad = txHoTen;
            }

            if (cccd.isEmpty()) {
                showFieldError(txCccd, errCccd, "Vui lòng nhập số CCCD");
                if (firstBad == null) firstBad = txCccd;
            } else if (!cccd.matches("\\d{9}|\\d{12}")) {
                showFieldError(txCccd, errCccd, "CCCD phải gồm 9 hoặc 12 chữ số");
                if (firstBad == null) firstBad = txCccd;
            }
            return firstBad;
        }

        /**
         * Map các field trong card về đối tượng KhachHang để caller dùng.
         */
        KhachHang toKhachHang() {
            String sdt   = txSdt.getText().trim();
            String hoTen = txHoTen.getText().trim();
            String cccd  = txCccd.getText().trim();
            String email = txEmail.getText().trim();
            String thuongTru = txThuongTru.getText().trim();
            String tamTru    = txTamTru.getText().trim();
            String quocTich  = txQuocTich.getText().trim();

            LocalDate ngaySinh = null;
            Object v = spNgaySinh.getValue();
            if (v instanceof Date d) {
                ngaySinh = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            }

            String gioiTinh = cbGioiTinh.getSelectedIndex() == 0 ? "NAM" : "NU";

            // Reuse nếu đã chọn từ DB: cập nhật setter trên object cũ (path update)
            if (foundKhachHang != null) {
                foundKhachHang.setHoTen(hoTen);
                foundKhachHang.setCccd(cccd);
                foundKhachHang.setSoDienThoai(sdt);
                foundKhachHang.setEmail(email.isEmpty() ? null : email);
                foundKhachHang.setDiaChiThuongTru(thuongTru.isEmpty() ? null : thuongTru);
                foundKhachHang.setDiaChiTamTru(tamTru.isEmpty() ? null : tamTru);
                foundKhachHang.setNgaySinh(ngaySinh);
                foundKhachHang.setGioiTinh(gioiTinh);
                foundKhachHang.setQuocTich(quocTich.isEmpty() ? "Việt Nam" : quocTich);
                return foundKhachHang;
            }

            return new KhachHang(
                    null, hoTen, cccd, sdt,
                    email.isEmpty() ? null : email,
                    thuongTru.isEmpty() ? null : thuongTru,
                    tamTru.isEmpty() ? null : tamTru,
                    ngaySinh, gioiTinh,
                    quocTich.isEmpty() ? "Việt Nam" : quocTich);
        }

        void clearAllErrors() {
            clearFieldError(txSdt, errSdt);
            clearFieldError(txHoTen, errHoTen);
            clearFieldError(txCccd, errCccd);
        }

        void showFieldError(JTextField field, JLabel errLbl, String msg) {
            errLbl.setText(msg);
            errLbl.setVisible(true);
            field.setBorder(BorderFactory.createLineBorder(ERROR_FG, 2));
        }

        void clearFieldError(JTextField field, JLabel errLbl) {
            if (errLbl == null) return;
            if (errLbl.isVisible()) {
                errLbl.setVisible(false);
                errLbl.setText(" ");
                if (defaultFieldBorder != null) field.setBorder(defaultFieldBorder);
            }
        }

        // --- row builders ---

        private JPanel makeRowPair(String label1, JComponent f1, JLabel err1,
                                   String label2, JComponent f2, JLabel err2) {
            JPanel row = new JPanel(new GridBagLayout());
            row.setBackground(CARD_BG);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 96));

            GridBagConstraints gc = new GridBagConstraints();
            gc.fill = GridBagConstraints.HORIZONTAL;
            gc.insets = new Insets(0, 0, 0, 8);

            gc.gridx = 0; gc.gridy = 0; gc.weightx = 1.0;
            row.add(makeFieldBlock(label1, f1, err1), gc);
            gc.gridx = 1; gc.gridy = 0;
            gc.insets = new Insets(0, 8, 0, 0);
            row.add(makeFieldBlock(label2, f2, err2), gc);
            return row;
        }

        private JPanel makeRowTriple(String l1, JComponent f1, String l2, JComponent f2, String l3, JComponent f3) {
            JPanel row = new JPanel(new GridBagLayout());
            row.setBackground(CARD_BG);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));

            GridBagConstraints gc = new GridBagConstraints();
            gc.fill = GridBagConstraints.HORIZONTAL;
            gc.weightx = 1.0;

            gc.gridx = 0; gc.insets = new Insets(0, 0, 0, 8);
            row.add(makeFieldBlock(l1, f1, null), gc);
            gc.gridx = 1; gc.insets = new Insets(0, 8, 0, 8);
            row.add(makeFieldBlock(l2, f2, null), gc);
            gc.gridx = 2; gc.insets = new Insets(0, 8, 0, 0);
            row.add(makeFieldBlock(l3, f3, null), gc);
            return row;
        }

        private JPanel makeSingleRow(String label, JComponent field) {
            JPanel row = new JPanel(new BorderLayout());
            row.setBackground(CARD_BG);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));
            row.add(makeFieldBlock(label, field, null), BorderLayout.CENTER);
            return row;
        }

        private JPanel makeFieldBlock(String label, JComponent field, JLabel errLbl) {
            JPanel block = new JPanel();
            block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));
            block.setBackground(CARD_BG);

            JLabel lbl = new JLabel(label);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lbl.setForeground(ON_SURF_VAR);
            lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            block.add(lbl);
            block.add(Box.createVerticalStrut(4));

            if (field instanceof JComponent jc) jc.setAlignmentX(Component.LEFT_ALIGNMENT);
            block.add(field);

            if (errLbl != null) {
                errLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
                block.add(errLbl);
            }
            return block;
        }
    }

    // ---- shared helpers used by CustomerCard ----

    private JTextField makeTextField() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setPreferredSize(new Dimension(200, 40));
        tf.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));
        return tf;
    }

    private JLabel makeErrLabel() {
        JLabel l = new JLabel(" ");
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(ERROR_FG);
        l.setVisible(false);
        l.setBorder(new EmptyBorder(2, 0, 0, 0));
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
        return l;
    }
}
