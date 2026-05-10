package com.modules;

import com.dao.DAO_KhachHang;
import com.entity.Ghe;
import com.entity.KhachHang;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class BanVeStep5Module extends JPanel implements AppModule {

    private enum InputMode { REPRESENTATIVE, PER_SEAT }

    private Consumer<Object> callback;
    private final DAO_KhachHang daoKH = new DAO_KhachHang();
    private final List<Ghe> selectedSeats;

    private static final Color PRIMARY       = NotionTheme.ACCENT;
    private static final Color SURFACE       = NotionTheme.PAGE;
    private static final Color CARD_BG       = AppColors.SURFACE;
    private static final Color ON_SURFACE    = NotionTheme.TEXT;
    private static final Color ON_SURF_VAR   = NotionTheme.TEXT_MUTED;
    private static final Color OUTLINE       = NotionTheme.BORDER;
    private static final Color PRIMARY_LIGHT = NotionTheme.ACCENT_SOFT;
    private static final Color ERROR_FG      = AppColors.ERROR_DARK;
    private static final Color SUCCESS       = AppColors.SUCCESS_DARK;

    private JPanel contentPanel;
    private CardLayout contentLayout;
    private JButton btnRepresentativeMode;
    private JButton btnPerSeatMode;
    private JButton btnSubmit, btnCancel;
    private JPanel btnPanel;

    private CustomerPicker representativePicker;
    private JPanel seatFormHolder;
    private CardLayout seatFormLayout;
    private JList<SeatEntry> seatList;
    private DefaultListModel<SeatEntry> seatListModel;
    private JLabel seatTitleLabel;
    private JButton btnSaveSeatNext;

    private final List<SeatEntry> seatEntries = new ArrayList<>();
    private InputMode currentMode;
    private int currentSeatIndex = 0;

    public BanVeStep5Module() {
        this(Collections.emptyList());
    }

    public BanVeStep5Module(List<Ghe> selectedSeats) {
        this.selectedSeats = selectedSeats == null ? Collections.emptyList() : new ArrayList<>(selectedSeats);
        setLayout(new BorderLayout());
        setBackground(SURFACE);
        buildUI();
    }

    private void buildUI() {
        add(buildHeader(), BorderLayout.NORTH);

        contentLayout = new CardLayout();
        contentPanel = new JPanel(contentLayout);
        contentPanel.setBackground(SURFACE);
        contentPanel.add(buildRepresentativeView(), InputMode.REPRESENTATIVE.name());
        contentPanel.add(buildPerSeatView(), InputMode.PER_SEAT.name());
        add(contentPanel, BorderLayout.CENTER);

        btnSubmit = new JButton("Tiếp theo →");
        styleBtn(btnSubmit, true);
        btnSubmit.addActionListener(e -> execute());

        btnCancel = new JButton("← Quay lại");
        styleBtn(btnCancel, false);
        btnCancel.addActionListener(e -> { if (callback != null) callback.accept(null); });

        btnPanel = BookingStepUi.createFooter();
        btnPanel.add(btnCancel);
        btnPanel.add(btnSubmit);
        btnPanel.setVisible(false);
        add(btnPanel, BorderLayout.SOUTH);

        switchMode(selectedSeats.size() > 1 ? InputMode.PER_SEAT : InputMode.REPRESENTATIVE);
    }

    private JPanel buildHeader() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(SURFACE);

        JPanel titleBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 12));
        titleBar.setBackground(PRIMARY_LIGHT);
        titleBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, OUTLINE));
        JLabel lbl = new JLabel("Thông tin khách hàng");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl.setForeground(PRIMARY);
        titleBar.add(lbl);
        wrapper.add(titleBar, BorderLayout.NORTH);

        JPanel modeBar = new JPanel(new GridLayout(1, 2, 12, 0));
        modeBar.setBackground(SURFACE);
        modeBar.setBorder(new EmptyBorder(16, 80, 8, 80));
        btnRepresentativeMode = makeModeButton("Người đại diện", "Một người mua đại diện cho toàn bộ hóa đơn");
        btnPerSeatMode = makeModeButton("Nhiều người theo ghế", "Chọn riêng người ngồi từng ghế đã đặt");
        btnRepresentativeMode.addActionListener(e -> switchMode(InputMode.REPRESENTATIVE));
        btnPerSeatMode.addActionListener(e -> switchMode(InputMode.PER_SEAT));
        modeBar.add(btnRepresentativeMode);
        modeBar.add(btnPerSeatMode);
        wrapper.add(modeBar, BorderLayout.CENTER);
        return wrapper;
    }

    private JButton makeModeButton(String title, String subtitle) {
        JButton btn = new JButton("<html><div style='text-align:left'><b>" + title + "</b><br><span style='font-size:10px'>" + subtitle + "</span></div></html>");
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel buildRepresentativeView() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(SURFACE);
        outer.setBorder(new EmptyBorder(16, 80, 24, 80));
        representativePicker = new CustomerPicker("Người đại diện mua vé", null);
        outer.add(representativePicker, BorderLayout.NORTH);
        return outer;
    }

    private JPanel buildPerSeatView() {
        JPanel outer = new JPanel(new BorderLayout(16, 0));
        outer.setBackground(SURFACE);
        outer.setBorder(new EmptyBorder(16, 80, 24, 80));

        seatFormLayout = new CardLayout();
        seatFormHolder = new JPanel(seatFormLayout);
        seatFormHolder.setBackground(SURFACE);

        seatListModel = new DefaultListModel<>();
        seatEntries.clear();
        List<Ghe> seats = selectedSeats.isEmpty() ? Collections.singletonList(null) : selectedSeats;
        for (int i = 0; i < seats.size(); i++) {
            SeatEntry entry = new SeatEntry(seats.get(i), i);
            seatEntries.add(entry);
            seatListModel.addElement(entry);
            seatFormHolder.add(entry.picker, entry.cardKey());
        }

        JPanel left = new JPanel(new BorderLayout(0, 12));
        left.setBackground(SURFACE);
        seatTitleLabel = new JLabel();
        seatTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        seatTitleLabel.setForeground(ON_SURFACE);
        left.add(seatTitleLabel, BorderLayout.NORTH);
        left.add(seatFormHolder, BorderLayout.CENTER);

        btnSaveSeatNext = new JButton("Xác nhận khách & ghế kế tiếp");
        styleBtn(btnSaveSeatNext, true);
        btnSaveSeatNext.addActionListener(e -> saveCurrentSeatAndNext());
        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        leftActions.setBackground(SURFACE);
        leftActions.add(btnSaveSeatNext);
        left.add(leftActions, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, buildSeatListPanel());
        split.setResizeWeight(0.67);
        split.setDividerSize(8);
        split.setBorder(BorderFactory.createEmptyBorder());
        split.setBackground(SURFACE);
        outer.add(split, BorderLayout.CENTER);

        selectSeat(0);
        return outer;
    }

    private JPanel buildSeatListPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1),
                new EmptyBorder(16, 16, 16, 16)));
        panel.setPreferredSize(new Dimension(300, 400));

        JLabel title = new JLabel("Ghế đã chọn");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(PRIMARY);
        panel.add(title, BorderLayout.NORTH);

        seatList = new JList<>(seatListModel);
        seatList.setCellRenderer(new SeatEntryRenderer());
        seatList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        seatList.setFixedCellHeight(82);
        seatList.setBackground(CARD_BG);
        seatList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && seatList.getSelectedIndex() >= 0) selectSeat(seatList.getSelectedIndex());
        });
        JScrollPane scroll = new JScrollPane(seatList);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void switchMode(InputMode mode) {
        currentMode = mode;
        contentLayout.show(contentPanel, mode.name());
        styleModeButton(btnRepresentativeMode, mode == InputMode.REPRESENTATIVE);
        styleModeButton(btnPerSeatMode, mode == InputMode.PER_SEAT);
        if (mode == InputMode.PER_SEAT) selectSeat(Math.min(currentSeatIndex, Math.max(0, seatEntries.size() - 1)));
    }

    private void styleModeButton(JButton btn, boolean active) {
        btn.setOpaque(true);
        btn.setBackground(active ? PRIMARY : CARD_BG);
        btn.setForeground(active ? AppColors.SURFACE : ON_SURF_VAR);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(active ? PRIMARY : OUTLINE, active ? 2 : 1),
                new EmptyBorder(12, 16, 12, 16)));
    }

    private void selectSeat(int index) {
        if (seatEntries.isEmpty()) return;
        currentSeatIndex = Math.max(0, Math.min(index, seatEntries.size() - 1));
        SeatEntry entry = seatEntries.get(currentSeatIndex);
        seatFormLayout.show(seatFormHolder, entry.cardKey());
        seatTitleLabel.setText("Thông tin người ngồi " + entry.fullSeatLabel());
        if (seatList != null && seatList.getSelectedIndex() != currentSeatIndex) seatList.setSelectedIndex(currentSeatIndex);
    }

    private void saveCurrentSeatAndNext() {
        if (seatEntries.isEmpty()) return;
        SeatEntry entry = seatEntries.get(currentSeatIndex);
        if (!entry.picker.hasCustomer()) {
            entry.picker.showError("Vui lòng chọn hoặc tạo khách hàng cho " + entry.fullSeatLabel());
            return;
        }
        refreshSeatList();
        int next = findNextUnfilledSeat(currentSeatIndex + 1);
        if (next < 0) next = findNextUnfilledSeat(0);
        if (next >= 0) selectSeat(next);
    }

    private int findNextUnfilledSeat(int fromIndex) {
        for (int i = fromIndex; i < seatEntries.size(); i++) {
            if (!seatEntries.get(i).picker.hasCustomer()) return i;
        }
        return -1;
    }

    private void execute() {
        if (currentMode == InputMode.REPRESENTATIVE) {
            if (!representativePicker.hasCustomer()) {
                representativePicker.showError("Vui lòng chọn hoặc tạo khách hàng đại diện");
                return;
            }
            List<KhachHang> result = new ArrayList<>();
            result.add(representativePicker.getCustomer());
            if (callback != null) callback.accept(result);
            return;
        }

        int badIndex = -1;
        for (int i = 0; i < seatEntries.size(); i++) {
            if (!seatEntries.get(i).picker.hasCustomer()) {
                badIndex = i;
                break;
            }
        }
        if (badIndex >= 0) {
            selectSeat(badIndex);
            seatEntries.get(badIndex).picker.showError("Vui lòng chọn hoặc tạo khách hàng cho " + seatEntries.get(badIndex).fullSeatLabel());
            return;
        }

        DuplicateCustomer duplicate = findDuplicateCustomers();
        if (duplicate != null) {
            selectSeat(duplicate.index);
            seatEntries.get(duplicate.index).picker.showError(duplicate.message);
            return;
        }

        List<KhachHang> result = new ArrayList<>();
        for (SeatEntry entry : seatEntries) result.add(entry.picker.getCustomer());
        if (callback != null) callback.accept(result);
    }

    private DuplicateCustomer findDuplicateCustomers() {
        for (int i = 0; i < seatEntries.size(); i++) {
            KhachHang a = seatEntries.get(i).picker.getCustomer();
            for (int j = i + 1; j < seatEntries.size(); j++) {
                KhachHang b = seatEntries.get(j).picker.getCustomer();
                if (sameNonBlank(a.getMaKhachHang(), b.getMaKhachHang())) {
                    return new DuplicateCustomer(j, "Khách hàng này đã được chọn cho ghế khác");
                }
                if (sameNonBlank(a.getSoDienThoai(), b.getSoDienThoai())) {
                    return new DuplicateCustomer(j, "Trùng SĐT với khách ở ghế khác");
                }
                if (sameNonBlank(a.getCccd(), b.getCccd())) {
                    return new DuplicateCustomer(j, "Trùng CCCD với khách ở ghế khác");
                }
                if (sameNonBlankIgnoreCase(a.getEmail(), b.getEmail())) {
                    return new DuplicateCustomer(j, "Trùng email với khách ở ghế khác");
                }
            }
        }
        return null;
    }

    private boolean sameNonBlank(String a, String b) {
        return a != null && b != null && !a.isBlank() && a.equals(b);
    }

    private boolean sameNonBlankIgnoreCase(String a, String b) {
        return a != null && b != null && !a.isBlank() && a.equalsIgnoreCase(b);
    }

    private void refreshSeatList() {
        if (seatList != null) {
            seatList.repaint();
            seatList.revalidate();
        }
    }

    private Window ownerWindow() {
        return SwingUtilities.getWindowAncestor(this);
    }

    private void styleBtn(JButton btn, boolean primary) {
        BookingStepUi.styleActionButton(btn, primary);
    }

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
        representativePicker.resetPicker();
        for (SeatEntry entry : seatEntries) entry.picker.resetPicker();
        currentSeatIndex = 0;
        switchMode(selectedSeats.size() > 1 ? InputMode.PER_SEAT : InputMode.REPRESENTATIVE);
        refreshSeatList();
    }

    private class CustomerPicker extends JPanel {
        private final String title;
        private final SeatEntry seatEntry;
        private final CardLayout layout = new CardLayout();
        private final JPanel body = new JPanel(layout);
        private final JTextField searchField = new JTextField();
        private final DefaultListModel<KhachHang> searchModel = new DefaultListModel<>();
        private final JList<KhachHang> searchList = new JList<>(searchModel);
        private final JLabel errorLabel = new JLabel(" ");
        private KhachHang selectedCustomer;

        CustomerPicker(String title, SeatEntry seatEntry) {
            this.title = title;
            this.seatEntry = seatEntry;
            setLayout(new BorderLayout(0, 12));
            setBackground(SURFACE);
            body.setBackground(SURFACE);
            body.add(buildChooseView(), "choose");
            body.add(buildSelectedView(), "selected");
            add(body, BorderLayout.CENTER);
            layout.show(body, "choose");
        }

        private JPanel buildChooseView() {
            JPanel wrapper = new JPanel();
            wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
            wrapper.setBackground(SURFACE);
            wrapper.add(buildSearchBlock());
            wrapper.add(Box.createVerticalStrut(10));
            wrapper.add(buildCreateBlock());
            return wrapper;
        }

        private JPanel buildSearchBlock() {
            JPanel panel = cardPanel();
            panel.setLayout(new BorderLayout(0, 10));
            JLabel lbl = sectionTitle(title);
            panel.add(lbl, BorderLayout.NORTH);

            JPanel center = new JPanel(new BorderLayout(0, 8));
            center.setOpaque(false);
            searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            searchField.setPreferredSize(new Dimension(200, 38));
            searchField.setToolTipText("Tìm theo tên, SĐT, CCCD, email hoặc mã khách hàng");
            searchField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(OUTLINE, 1),
                    new EmptyBorder(0, 12, 0, 12)));
            searchField.getDocument().addDocumentListener(new DocumentListener() {
                @Override public void insertUpdate(DocumentEvent e) { searchCustomers(); }
                @Override public void removeUpdate(DocumentEvent e) { searchCustomers(); }
                @Override public void changedUpdate(DocumentEvent e) { searchCustomers(); }
            });
            center.add(searchField, BorderLayout.NORTH);

            searchList.setCellRenderer(new CustomerResultRenderer());
            searchList.setFixedCellHeight(48);
            searchList.setVisibleRowCount(5);
            searchList.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() >= 1) selectCustomer(searchList.getSelectedValue());
                }
            });
            JScrollPane scroll = new JScrollPane(searchList);
            scroll.setBorder(BorderFactory.createLineBorder(OUTLINE, 1));
            center.add(scroll, BorderLayout.CENTER);

            errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            errorLabel.setForeground(ERROR_FG);
            center.add(errorLabel, BorderLayout.SOUTH);
            panel.add(center, BorderLayout.CENTER);
            searchCustomers();
            return panel;
        }

        private JPanel buildCreateBlock() {
            JPanel panel = compactCardPanel();
            panel.setLayout(new BorderLayout(14, 0));

            JPanel textBox = new JPanel();
            textBox.setOpaque(false);
            textBox.setLayout(new BoxLayout(textBox, BoxLayout.Y_AXIS));
            JLabel title = new JLabel("Chưa có hồ sơ khách hàng?");
            title.setFont(new Font("Segoe UI", Font.BOLD, 13));
            title.setForeground(ON_SURFACE);
            JLabel desc = new JLabel("Tạo nhanh hồ sơ mới rồi tự động chọn khách vừa tạo.");
            desc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            desc.setForeground(ON_SURF_VAR);
            textBox.add(title);
            textBox.add(Box.createVerticalStrut(3));
            textBox.add(desc);

            JButton btnCreate = new JButton("+ Tạo mới");
            styleSmallPrimaryButton(btnCreate);
            btnCreate.addActionListener(e -> openCreateCustomerDialog());
            panel.add(textBox, BorderLayout.CENTER);
            panel.add(btnCreate, BorderLayout.EAST);
            return panel;
        }

        private JPanel buildSelectedView() {
            JPanel panel = cardPanel();
            panel.setLayout(new BorderLayout(0, 18));

            JPanel header = new JPanel(new BorderLayout(14, 0));
            header.setOpaque(false);
            JPanel avatar = new JPanel(new GridBagLayout());
            avatar.setPreferredSize(new Dimension(54, 54));
            avatar.setBackground(PRIMARY_LIGHT);
            avatar.setBorder(BorderFactory.createLineBorder(NotionTheme.ACCENT_SOFT, 1));
            JLabel initials = new JLabel("KH");
            initials.setFont(new Font("Segoe UI", Font.BOLD, 16));
            initials.setForeground(PRIMARY);
            avatar.add(initials);
            JPanel titleBox = new JPanel();
            titleBox.setOpaque(false);
            titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
            JLabel title = sectionTitle("Khách hàng đã chọn");
            JLabel hint = new JLabel("Thông tin bên dưới chỉ dùng để kiểm tra, chỉnh sửa bằng nút Sửa thông tin.");
            hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            hint.setForeground(ON_SURF_VAR);
            titleBox.add(title);
            titleBox.add(Box.createVerticalStrut(4));
            titleBox.add(hint);
            header.add(avatar, BorderLayout.WEST);
            header.add(titleBox, BorderLayout.CENTER);
            panel.add(header, BorderLayout.NORTH);

            JPanel info = new JPanel(new GridBagLayout());
            info.setOpaque(false);
            panel.add(info, BorderLayout.CENTER);

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            actions.setOpaque(false);
            JButton btnEdit = new JButton("Sửa thông tin");
            JButton btnChange = new JButton("Đổi khách hàng khác");
            styleSmallPrimaryButton(btnEdit);
            styleSmallSecondaryButton(btnChange);
            btnEdit.addActionListener(e -> openEditCustomerDialog());
            btnChange.addActionListener(e -> resetPicker());
            actions.add(btnChange);
            actions.add(btnEdit);
            panel.add(actions, BorderLayout.SOUTH);
            panel.putClientProperty("infoPanel", info);
            panel.putClientProperty("initialsLabel", initials);
            return panel;
        }

        private void searchCustomers() {
            searchModel.clear();
            String kw = searchField.getText().trim();
            List<KhachHang> results = kw.isEmpty() ? daoKH.getAll() : daoKH.search(kw);
            int limit = Math.min(results.size(), 30);
            for (int i = 0; i < limit; i++) searchModel.addElement(results.get(i));
        }

        private void selectCustomer(KhachHang kh) {
            if (kh == null) return;
            selectedCustomer = kh;
            refreshSelectedView();
            errorLabel.setText(" ");
            layout.show(body, "selected");
            refreshSeatList();
        }

        private void refreshSelectedView() {
            JPanel selectedView = (JPanel) body.getComponent(1);
            JPanel info = (JPanel) selectedView.getClientProperty("infoPanel");
            JLabel initials = (JLabel) selectedView.getClientProperty("initialsLabel");
            initials.setText(initialsOf(selectedCustomer.getHoTen()));
            info.removeAll();

            GridBagConstraints gc = new GridBagConstraints();
            gc.fill = GridBagConstraints.HORIZONTAL;
            gc.weightx = 1.0;
            gc.insets = new Insets(0, 0, 10, 10);
            addInfo(info, gc, 0, 0, "Mã khách hàng", selectedCustomer.getMaKhachHang());
            addInfo(info, gc, 1, 0, "Họ và tên", selectedCustomer.getHoTen());
            addInfo(info, gc, 0, 1, "Số điện thoại", selectedCustomer.getSoDienThoai());
            addInfo(info, gc, 1, 1, "CCCD", selectedCustomer.getCccd());
            addInfo(info, gc, 0, 2, "Email", selectedCustomer.getEmail());
            addInfo(info, gc, 1, 2, "Ngày sinh", selectedCustomer.getNgaySinh() == null ? "" : selectedCustomer.getNgaySinh().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            addInfo(info, gc, 0, 3, "Giới tính", formatGender(selectedCustomer.getGioiTinh()));
            addInfo(info, gc, 1, 3, "Quốc tịch", selectedCustomer.getQuocTich());
            gc.gridwidth = 2;
            addInfo(info, gc, 0, 4, "Địa chỉ thường trú", selectedCustomer.getDiaChiThuongTru());
            addInfo(info, gc, 0, 5, "Địa chỉ tạm trú", selectedCustomer.getDiaChiTamTru());
            info.revalidate();
            info.repaint();
        }

        private void addInfo(JPanel panel, GridBagConstraints base, int x, int y, String label, String value) {
            GridBagConstraints gc = (GridBagConstraints) base.clone();
            gc.gridx = x;
            gc.gridy = y;
            JPanel row = new JPanel(new BorderLayout(6, 4));
            row.setBackground(NotionTheme.PAGE);
            row.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(NotionTheme.BORDER, 1),
                    new EmptyBorder(10, 12, 10, 12)));
            JLabel l = new JLabel(label.toUpperCase());
            l.setFont(new Font("Segoe UI", Font.BOLD, 10));
            l.setForeground(ON_SURF_VAR);
            JLabel v = new JLabel(value == null || value.isBlank() ? "—" : value);
            v.setFont(new Font("Segoe UI", Font.BOLD, 13));
            v.setForeground(ON_SURFACE);
            row.add(l, BorderLayout.NORTH);
            row.add(v, BorderLayout.CENTER);
            panel.add(row, gc);
        }

        private void openCreateCustomerDialog() {
            ThemKhachHangDialog dlg = new ThemKhachHangDialog(ownerWindow(), () -> {});
            dlg.setOnResult(result -> {
                if (result instanceof KhachHang kh) selectCustomer(kh);
            });
            dlg.setVisible(true);
        }

        private void openEditCustomerDialog() {
            if (selectedCustomer == null) return;
            SuaKhachHangDialog dlg = new SuaKhachHangDialog(ownerWindow(), selectedCustomer, () -> {});
            dlg.setOnResult(result -> {
                if (result instanceof KhachHang kh) selectCustomer(kh);
            });
            dlg.setVisible(true);
        }

        boolean hasCustomer() { return selectedCustomer != null; }
        KhachHang getCustomer() { return selectedCustomer; }

        void showError(String msg) {
            errorLabel.setText(msg);
            layout.show(body, "choose");
        }

        void resetPicker() {
            selectedCustomer = null;
            searchField.setText("");
            errorLabel.setText(" ");
            searchCustomers();
            layout.show(body, "choose");
            refreshSeatList();
        }

        String customerSummary() {
            if (selectedCustomer == null) return "Chưa chọn khách hàng";
            String phone = selectedCustomer.getSoDienThoai();
            return selectedCustomer.getHoTen() + (phone == null || phone.isBlank() ? "" : " • " + phone);
        }
    }

    private JPanel cardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1),
                new EmptyBorder(18, 22, 18, 22)));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        return panel;
    }

    private JPanel compactCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1),
                new EmptyBorder(12, 18, 12, 18)));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));
        panel.setPreferredSize(new Dimension(200, 76));
        return panel;
    }

    private void styleSmallPrimaryButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBackground(PRIMARY);
        btn.setForeground(AppColors.SURFACE);
        btn.setOpaque(true);
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));
    }

    private void styleSmallSecondaryButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBackground(AppColors.SURFACE);
        btn.setForeground(ON_SURF_VAR);
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1),
                new EmptyBorder(7, 13, 7, 13)));
    }

    private JLabel sectionTitle(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl.setForeground(PRIMARY);
        return lbl;
    }

    private class SeatEntry {
        private final Ghe ghe;
        private final int index;
        private final CustomerPicker picker;

        SeatEntry(Ghe ghe, int index) {
            this.ghe = ghe;
            this.index = index;
            this.picker = new CustomerPicker("Chọn khách hàng đã đăng ký", this);
        }

        String cardKey() { return "seat-" + index; }

        String seatLabel() {
            if (ghe == null) return "Ghế đã chọn";
            String soGhe = ghe.getSoGhe() > 0 ? "Ghế " + ghe.getSoGhe() : ghe.getMaGhe();
            return soGhe == null || soGhe.isBlank() ? "Ghế #" + (index + 1) : soGhe;
        }

        String coachLabel() {
            if (ghe == null || ghe.getToaTau() == null) return "Chưa có toa";
            String maToa = ghe.getToaTau().getMaToaTau();
            return maToa == null || maToa.isBlank() ? "Toa chưa rõ" : "Toa " + maToa;
        }

        String detailLabel() {
            if (ghe == null || ghe.getToaTau() == null) return "Thông tin ghế";
            Object loai = ghe.getToaTau().getLoaiGhe();
            return coachLabel() + (loai == null ? "" : " • " + loai);
        }

        String fullSeatLabel() {
            return seatLabel() + " - " + coachLabel();
        }
    }

    private class SeatEntryRenderer extends JPanel implements ListCellRenderer<SeatEntry> {
        private final JLabel title = new JLabel();
        private final JLabel detail = new JLabel();
        private final JLabel status = new JLabel();

        SeatEntryRenderer() {
            setLayout(new BorderLayout(6, 3));
            setBorder(new EmptyBorder(8, 10, 8, 10));
            JPanel text = new JPanel();
            text.setOpaque(false);
            text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
            title.setFont(new Font("Segoe UI", Font.BOLD, 13));
            detail.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            status.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            text.add(title);
            text.add(detail);
            text.add(status);
            add(text, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends SeatEntry> list, SeatEntry value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            title.setText(value.fullSeatLabel());
            detail.setText(value.detailLabel());
            if (value.picker.hasCustomer()) {
                status.setText(value.picker.customerSummary());
                status.setForeground(SUCCESS);
            } else {
                status.setText("Chưa chọn khách hàng");
                status.setForeground(ERROR_FG);
            }
            title.setForeground(isSelected ? AppColors.SURFACE : ON_SURFACE);
            detail.setForeground(isSelected ? NotionTheme.ACCENT_SOFT : ON_SURF_VAR);
            setBackground(isSelected ? PRIMARY : CARD_BG);
            return this;
        }
    }

    private class CustomerResultRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof KhachHang kh) {
                setText("<html><b>" + safe(kh.getHoTen()) + "</b> &nbsp; " + safe(kh.getSoDienThoai())
                        + "<br><span style='font-size:10px;color:#5F6770'>CCCD: " + safe(kh.getCccd())
                        + " • " + safe(kh.getEmail()) + "</span></html>");
            }
            setBorder(new EmptyBorder(5, 10, 5, 10));
            return this;
        }
    }

    private static class DuplicateCustomer {
        private final int index;
        private final String message;
        DuplicateCustomer(int index, String message) {
            this.index = index;
            this.message = message;
        }
    }

    private String initialsOf(String name) {
        if (name == null || name.isBlank()) return "KH";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    private String formatGender(String gender) {
        if ("NAM".equalsIgnoreCase(gender)) return "Nam";
        if ("NU".equalsIgnoreCase(gender)) return "Nữ";
        return gender;
    }

    private String safe(String s) { return s == null ? "" : s; }
}
