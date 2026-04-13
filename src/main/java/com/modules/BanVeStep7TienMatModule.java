package com.modules;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Bước 7 (Tiền mặt) — Thanh toán tiền mặt.
 * Input: tổng tiền cần thanh toán.
 * Output: "CONFIRMED" khi thu tiền xong.
 */
public class BanVeStep7TienMatModule extends JPanel implements AppModule {

    private Consumer<Object> callback;

    private final BigDecimal total;

    private static final NumberFormat VND_FMT = NumberFormat.getInstance(new Locale("vi", "VN"));

    // Design tokens
    private static final Color PRIMARY       = new Color(0x00, 0x5D, 0x90);
    private static final Color SURFACE       = new Color(0xF8, 0xFA, 0xFC);
    private static final Color CARD_BG       = Color.WHITE;
    private static final Color ON_SURFACE    = new Color(0x1A, 0x1D, 0x21);
    private static final Color ON_SURF_VAR   = new Color(0x5F, 0x67, 0x70);
    private static final Color OUTLINE       = new Color(0xDE, 0xE3, 0xE8);
    private static final Color PRIMARY_LIGHT = new Color(0xE3, 0xF2, 0xFD);
    private static final Color AMOUNT_COLOR  = new Color(0x1A, 0x7A, 0x3C);
    private static final Color CHANGE_COLOR  = new Color(0x1A, 0x7A, 0x3C);
    private static final Color WARN_COLOR    = new Color(0xC6, 0x28, 0x28);

    private JTextField txReceived;
    private JLabel     lblChange;
    private JLabel     lblChangePrefix;
    private JButton    btnSubmit, btnCancel;
    private JPanel     btnPanel;

    // =========================================================================
    //  CONSTRUCTOR
    // =========================================================================

    public BanVeStep7TienMatModule(BigDecimal total) {
        this.total = total;
        setLayout(new BorderLayout());
        setBackground(SURFACE);
        buildUI();
    }

    // =========================================================================
    //  BUILD UI
    // =========================================================================

    private void buildUI() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 12));
        header.setBackground(PRIMARY_LIGHT);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, OUTLINE));
        JLabel hdr = new JLabel("Thanh toán — Tiền mặt");
        hdr.setFont(new Font("Segoe UI", Font.BOLD, 15));
        hdr.setForeground(PRIMARY);
        header.add(hdr);
        add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(SURFACE);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(OUTLINE, 1),
            new EmptyBorder(36, 48, 36, 48)
        ));

        // -- Total amount display --
        JLabel totalCaption = new JLabel("Số tiền cần thu");
        totalCaption.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        totalCaption.setForeground(ON_SURF_VAR);
        totalCaption.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel totalAmount = new JLabel(VND_FMT.format(total.longValue()) + " ₫");
        totalAmount.setFont(new Font("Segoe UI", Font.BOLD, 42));
        totalAmount.setForeground(AMOUNT_COLOR);
        totalAmount.setAlignmentX(Component.CENTER_ALIGNMENT);

        // -- Received amount input --
        JLabel recvCaption = new JLabel("Khách đưa (₫)");
        recvCaption.setFont(new Font("Segoe UI", Font.BOLD, 13));
        recvCaption.setForeground(ON_SURFACE);
        recvCaption.setAlignmentX(Component.LEFT_ALIGNMENT);

        txReceived = new JTextField();
        txReceived.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        txReceived.setHorizontalAlignment(JTextField.RIGHT);
        txReceived.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        txReceived.setAlignmentX(Component.LEFT_ALIGNMENT);
        txReceived.setToolTipText("Nhập số tiền khách đưa");

        // -- Change display --
        JPanel changeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        changeRow.setBackground(CARD_BG);
        changeRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblChangePrefix = new JLabel("Tiền thối:");
        lblChangePrefix.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblChangePrefix.setForeground(ON_SURFACE);

        lblChange = new JLabel("—");
        lblChange.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblChange.setForeground(ON_SURF_VAR);

        changeRow.add(lblChangePrefix);
        changeRow.add(lblChange);

        // Auto-calc change on input
        txReceived.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { calcChange(); }
            @Override public void removeUpdate(DocumentEvent e)  { calcChange(); }
            @Override public void changedUpdate(DocumentEvent e) { calcChange(); }
        });

        card.add(totalCaption);
        card.add(Box.createVerticalStrut(6));
        card.add(totalAmount);
        card.add(Box.createVerticalStrut(28));
        card.add(recvCaption);
        card.add(Box.createVerticalStrut(8));
        card.add(txReceived);
        card.add(Box.createVerticalStrut(16));
        card.add(changeRow);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill    = GridBagConstraints.BOTH;
        gbc.weightx = 0.5;
        gbc.weighty = 0.7;
        gbc.insets  = new Insets(24, 80, 24, 80);
        center.add(card, gbc);
        add(center, BorderLayout.CENTER);

        // Buttons
        btnSubmit = new JButton("Xác nhận đã thu tiền");
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

    // =========================================================================
    //  LOGIC
    // =========================================================================

    private void calcChange() {
        try {
            String raw = txReceived.getText().replaceAll("[^\\d]", "").trim();
            if (raw.isEmpty()) {
                lblChange.setText("—");
                lblChange.setForeground(ON_SURF_VAR);
                return;
            }
            long received = Long.parseLong(raw);
            long needed   = total.longValue();
            long change   = received - needed;

            if (change < 0) {
                lblChange.setText("Thiếu " + VND_FMT.format(-change) + " ₫");
                lblChange.setForeground(WARN_COLOR);
            } else {
                lblChange.setText(VND_FMT.format(change) + " ₫");
                lblChange.setForeground(CHANGE_COLOR);
            }
        } catch (NumberFormatException ex) {
            lblChange.setText("—");
            lblChange.setForeground(ON_SURF_VAR);
        }
    }

    private void execute() {
        // Validate that received >= total
        try {
            String raw = txReceived.getText().replaceAll("[^\\d]", "").trim();
            if (raw.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Vui lòng nhập số tiền khách đưa.", "Thiếu thông tin",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            long received = Long.parseLong(raw);
            if (received < total.longValue()) {
                JOptionPane.showMessageDialog(this,
                    "Số tiền khách đưa chưa đủ.", "Không đủ tiền",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Số tiền không hợp lệ.",
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (callback != null) callback.accept("CONFIRMED");
    }

    // =========================================================================
    //  HELPERS
    // =========================================================================

    private void styleBtn(JButton btn, boolean primary) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 24, 10, 24));
        if (primary) {
            btn.setBackground(PRIMARY); btn.setForeground(Color.WHITE); btn.setOpaque(true);
        } else {
            btn.setBackground(CARD_BG); btn.setForeground(ON_SURF_VAR); btn.setOpaque(true);
        }
    }

    // =========================================================================
    //  AppModule
    // =========================================================================

    @Override public String getTitle() { return "Bước 7 – Tiền mặt"; }
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
        txReceived.setText("");
        lblChange.setText("—");
        lblChange.setForeground(ON_SURF_VAR);
    }
}
