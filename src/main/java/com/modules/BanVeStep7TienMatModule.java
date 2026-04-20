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

        // Two-column layout: left = amount visual, right = input form
        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(SURFACE);
        center.setBorder(new EmptyBorder(24, 40, 24, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill    = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.insets  = new Insets(0, 0, 0, 12);

        gbc.weightx = 0.4;
        center.add(buildAmountCard(), gbc);

        gbc.insets  = new Insets(0, 0, 0, 0);
        gbc.weightx = 0.6;
        center.add(buildInputCard(), gbc);

        add(center, BorderLayout.CENTER);

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

    private JPanel buildAmountCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(OUTLINE, 1),
            new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel title = new JLabel("Số tiền cần thu");
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(ON_SURF_VAR);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel amountLbl = new JLabel(VND_FMT.format(total.longValue()) + " ₫");
        amountLbl.setFont(new Font("Segoe UI", Font.BOLD, 34));
        amountLbl.setForeground(AMOUNT_COLOR);
        amountLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel hint = new JLabel("Nhận tiền mặt từ khách hàng");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hint.setForeground(ON_SURF_VAR);
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(Box.createVerticalGlue());
        card.add(title);
        card.add(Box.createVerticalStrut(12));
        card.add(amountLbl);
        card.add(Box.createVerticalStrut(10));
        card.add(hint);
        card.add(Box.createVerticalGlue());
        return card;
    }

    private JPanel buildInputCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(OUTLINE, 1),
            new EmptyBorder(24, 28, 24, 28)
        ));

        JLabel title = new JLabel("Nhập tiền khách đưa");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(ON_SURFACE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(20));

        addInfoRow(card, "Số tiền cần thu:",
            VND_FMT.format(total.longValue()) + " ₫", AMOUNT_COLOR, true);

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(OUTLINE);
        card.add(sep);
        card.add(Box.createVerticalStrut(14));

        JLabel recvLabel = new JLabel("Khách đưa (₫):");
        recvLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        recvLabel.setForeground(ON_SURF_VAR);
        recvLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(recvLabel);
        card.add(Box.createVerticalStrut(6));

        txReceived = new JTextField();
        txReceived.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        txReceived.setHorizontalAlignment(JTextField.RIGHT);
        txReceived.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        txReceived.setAlignmentX(Component.LEFT_ALIGNMENT);
        txReceived.setToolTipText("Nhập số tiền khách đưa");
        txReceived.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(OUTLINE, 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        card.add(txReceived);
        card.add(Box.createVerticalStrut(14));

        // Change row (same style as addInfoRow but with dynamic lblChange)
        JPanel changeRow = new JPanel(new BorderLayout());
        changeRow.setBackground(CARD_BG);
        changeRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        changeRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        JLabel changeLbl = new JLabel("Tiền thối:");
        changeLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        changeLbl.setForeground(ON_SURF_VAR);
        changeLbl.setPreferredSize(new Dimension(145, 24));
        lblChange = new JLabel("—");
        lblChange.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblChange.setForeground(ON_SURF_VAR);
        changeRow.add(changeLbl, BorderLayout.WEST);
        changeRow.add(lblChange,  BorderLayout.CENTER);
        card.add(changeRow);
        card.add(Box.createVerticalStrut(20));

        JPanel warningBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        warningBox.setBackground(new Color(0xFF, 0xF8, 0xE1));
        warningBox.setBorder(BorderFactory.createLineBorder(new Color(0xFF, 0xD5, 0x4F), 1));
        warningBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        warningBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JLabel warnLbl = new JLabel("Kiểm tra tiền trước khi xác nhận.");
        warnLbl.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        warnLbl.setForeground(new Color(0x7B, 0x5D, 0x00));
        warningBox.add(warnLbl);
        card.add(warningBox);

        txReceived.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { calcChange(); }
            @Override public void removeUpdate(DocumentEvent e)  { calcChange(); }
            @Override public void changedUpdate(DocumentEvent e) { calcChange(); }
        });

        return card;
    }

    private void addInfoRow(JPanel card, String label, String value,
                            Color valueColor, boolean bold) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(CARD_BG);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        JLabel lLbl = new JLabel(label);
        lLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lLbl.setForeground(ON_SURF_VAR);
        lLbl.setPreferredSize(new Dimension(145, 24));
        JLabel vLbl = new JLabel(value);
        vLbl.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, 13));
        vLbl.setForeground(valueColor);
        row.add(lLbl, BorderLayout.WEST);
        row.add(vLbl, BorderLayout.CENTER);
        card.add(row);
        card.add(Box.createVerticalStrut(6));
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
