package com.modules;

import com.dao.DAO_KhachHang;
import com.entity.KhachHang;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;

/**
 * Bước 5 — Nhập thông tin khách hàng.
 * Số điện thoại có autocomplete popup (JWindow) hiện ngay dưới ô khi gõ ≥6 ký tự.
 * Chọn một dòng trong popup → fill txSdt, txHoTen, txCccd cùng lúc.
 * Output: KhachHang
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
    private static final Color SUCCESS_BG    = new Color(0xE8, 0xF5, 0xE9);
    private static final Color SUCCESS_FG    = new Color(0x1B, 0x5E, 0x20);

    // Form fields
    private JTextField txSdt, txHoTen, txCccd;
    private JPanel     statusCard;
    private JLabel     lblStatus;
    private JPanel     formCard;

    // Autocomplete popup
    private JWindow           suggestionWindow;
    private JList<KhachHang>  suggestionList;
    private boolean           fillingFromSuggestion = false;

    // State
    private boolean   foundInDb      = false;
    private KhachHang foundKhachHang = null;

    // AppModule buttons
    private JButton btnSubmit, btnCancel;
    private JPanel  btnPanel;

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

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(SURFACE);
        center.setBorder(new EmptyBorder(24, 80, 24, 80));

        formCard   = buildFormCard();
        statusCard = buildStatusCard();
        center.add(formCard);
        center.add(Box.createVerticalStrut(16));
        center.add(statusCard);

        JScrollPane scroll = new JScrollPane(center);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(SURFACE);
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

    private JPanel buildFormCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(OUTLINE, 1),
            new EmptyBorder(24, 32, 24, 32)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLbl = new JLabel("Nhập / Tìm thông tin khách hàng");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLbl.setForeground(ON_SURFACE);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(titleLbl);
        card.add(Box.createVerticalStrut(16));

        // Số điện thoại — gõ ≥6 ký tự → popup gợi ý hiện ngay dưới
        txSdt = makeTextField();
        card.add(makeRow("Số điện thoại *:", txSdt));
        card.add(Box.createVerticalStrut(10));

        // Họ và tên
        txHoTen = makeTextField();
        card.add(makeRow("Họ và tên *:", txHoTen));
        card.add(Box.createVerticalStrut(10));

        // CCCD
        txCccd = makeTextField();
        card.add(makeRow("CCCD *:", txCccd));

        // ---- Listeners ----
        txSdt.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { onSdtChanged(); }
            @Override public void removeUpdate(DocumentEvent e)  { onSdtChanged(); }
            @Override public void changedUpdate(DocumentEvent e) { onSdtChanged(); }
        });

        // Ẩn popup khi txSdt mất focus (sang txHoTen, txCccd, v.v.)
        // Vì suggestionWindow là non-focusable, click vào list không trigger focusLost
        txSdt.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) {
                // Delay nhỏ để mouseClicked trên list kịp xử lý trước khi ẩn
                SwingUtilities.invokeLater(() -> hideSuggestions());
            }
        });

        return card;
    }

    // =========================================================================
    //  AUTOCOMPLETE POPUP
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
                if (kh != null) fillFromCustomer(kh);
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

    private void showSuggestions(List<KhachHang> items) {
        ensureSuggestionWindow();
        suggestionList.setListData(items.toArray(new KhachHang[0]));

        try {
            Point loc = txSdt.getLocationOnScreen();
            int w = txSdt.getWidth();
            int rows = Math.min(items.size(), 5);
            int h = rows * 36 + 6;

            suggestionWindow.setSize(w, h);
            suggestionWindow.setLocation(loc.x, loc.y + txSdt.getHeight());
            suggestionWindow.setVisible(true);
        } catch (IllegalComponentStateException ex) {
            // component not yet on screen — ignore
        }
    }

    private void hideSuggestions() {
        if (suggestionWindow != null) suggestionWindow.setVisible(false);
    }

    private void fillFromCustomer(KhachHang kh) {
        fillingFromSuggestion = true;
        txSdt.setText(kh.getSoDienThoai());
        txHoTen.setText(kh.getHoTen());
        txCccd.setText(kh.getCccd() != null ? kh.getCccd() : "");
        foundKhachHang = kh;
        foundInDb      = true;
        hideSuggestions();
        fillingFromSuggestion = false;
        showStatus("Đã chọn: " + kh.getHoTen() + " — thông tin được điền sẵn, có thể chỉnh sửa.");
    }

    // =========================================================================
    //  SEARCH (triggered by DocumentListener)
    // =========================================================================

    private void onSdtChanged() {
        if (fillingFromSuggestion) return;

        // Reset khi user gõ tay
        foundKhachHang = null;
        foundInDb      = false;
        statusCard.setVisible(false);

        String sdt = txSdt.getText().trim();
        if (sdt.length() < 6) {
            hideSuggestions();
            return;
        }

        List<KhachHang> results = daoKH.findBySoDienThoaiLike(sdt);
        if (results.isEmpty()) {
            hideSuggestions();
            return;
        }
        showSuggestions(results);
    }

    // =========================================================================
    //  STATUS CARD
    // =========================================================================

    private JPanel buildStatusCard() {
        JPanel card = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        card.setVisible(false);
        card.setBackground(SUCCESS_BG);
        card.setBorder(BorderFactory.createLineBorder(new Color(0xA5, 0xD6, 0xA7), 1));
        lblStatus = new JLabel("");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblStatus.setForeground(SUCCESS_FG);
        card.add(lblStatus);
        return card;
    }

    private void showStatus(String msg) {
        lblStatus.setText(msg);
        statusCard.setVisible(true);
        statusCard.revalidate();
    }

    // =========================================================================
    //  EXECUTE
    // =========================================================================

    private void execute() {
        String sdt   = txSdt.getText().trim();
        String hoTen = txHoTen.getText().trim();
        String cccd  = txCccd.getText().trim();

        if (sdt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập số điện thoại.",
                "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (hoTen.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập họ và tên khách hàng.",
                "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (cccd.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập số CCCD.",
                "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // maKhachHang != null → khách cũ (saveTransaction sẽ update)
        // maKhachHang == null → khách mới (saveTransaction sẽ insert)
        KhachHang kh = (foundInDb && foundKhachHang != null)
            ? new KhachHang(foundKhachHang.getMaKhachHang(), hoTen, cccd, sdt)
            : new KhachHang(null, hoTen, cccd, sdt);

        if (callback != null) callback.accept(kh);
    }

    // =========================================================================
    //  HELPERS
    // =========================================================================

    private JTextField makeTextField() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setPreferredSize(new Dimension(320, 40));
        tf.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));
        return tf;
    }

    private JPanel makeRow(String labelText, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setBackground(CARD_BG);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(ON_SURFACE);
        lbl.setPreferredSize(new Dimension(160, 28));
        row.add(lbl, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        return row;
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
        txSdt.setText("");
        txHoTen.setText("");
        txCccd.setText("");
        statusCard.setVisible(false);
        foundInDb      = false;
        foundKhachHang = null;
        hideSuggestions();
    }
}
