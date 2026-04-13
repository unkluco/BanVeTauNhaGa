package com.modules;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Bước 8 — Hoàn thành đặt vé.
 * Hiển thị thông báo thành công và cho phép quay lại màn hình ban đầu.
 * Output: bất kỳ giá trị nào → trigger reset() ở BanVeModule.
 */
public class BanVeStep8Module extends JPanel implements AppModule {

    private Consumer<Object> callback;

    // Design tokens
    private static final Color PRIMARY       = new Color(0x00, 0x5D, 0x90);
    private static final Color SURFACE       = new Color(0xF8, 0xFA, 0xFC);
    private static final Color CARD_BG       = Color.WHITE;
    private static final Color ON_SURFACE    = new Color(0x1A, 0x1D, 0x21);
    private static final Color ON_SURF_VAR   = new Color(0x5F, 0x67, 0x70);
    private static final Color OUTLINE       = new Color(0xDE, 0xE3, 0xE8);
    private static final Color SUCCESS_BG    = new Color(0xE8, 0xF5, 0xE9);
    private static final Color SUCCESS_FG    = new Color(0x1B, 0x5E, 0x20);
    private static final Color SUCCESS_DARK  = new Color(0x2E, 0x7D, 0x32);

    private JButton btnSubmit, btnCancel;
    private JPanel  btnPanel;

    // =========================================================================
    //  CONSTRUCTOR
    // =========================================================================

    public BanVeStep8Module() {
        setLayout(new BorderLayout());
        setBackground(SURFACE);
        buildUI();
    }

    // =========================================================================
    //  BUILD UI
    // =========================================================================

    private void buildUI() {
        // Full-center layout
        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(SURFACE);

        JPanel card = buildSuccessCard();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(32, 80, 32, 80);
        center.add(card, gbc);
        add(center, BorderLayout.CENTER);

        // AppModule buttons
        btnSubmit = new JButton("Đặt vé mới");
        styleBtn(btnSubmit, true);
        btnSubmit.addActionListener(e -> { if (callback != null) callback.accept("DONE"); });

        btnCancel = new JButton("Về menu chính");
        styleBtn(btnCancel, false);
        // TODO: Implement navigate-back-to-menu when parent supports it
        btnCancel.addActionListener(e -> { if (callback != null) callback.accept("DONE"); });

        JButton btnPrint = new JButton("In vé");
        styleBtn(btnPrint, false);
        // TODO: Implement print ticket
        btnPrint.addActionListener(e ->
            JOptionPane.showMessageDialog(this,
                "Chức năng in vé đang được phát triển.",
                "Thông báo", JOptionPane.INFORMATION_MESSAGE));

        btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        btnPanel.setBackground(SURFACE);
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, OUTLINE));
        btnPanel.add(btnCancel);
        btnPanel.add(btnPrint);
        btnPanel.add(btnSubmit);
        btnPanel.setVisible(false);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private JPanel buildSuccessCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xA5, 0xD6, 0xA7), 2),
            new EmptyBorder(48, 64, 48, 64)
        ));

        // Success icon (drawn)
        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

                int s = Math.min(getWidth(), getHeight());
                int cx = getWidth()  / 2;
                int cy = getHeight() / 2;
                int r  = s / 2 - 2;

                // Circle background
                g2.setColor(SUCCESS_DARK);
                g2.fillOval(cx - r, cy - r, r * 2, r * 2);

                // Checkmark
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int[] xPts = { cx - r / 3, cx, cx + r / 2 };
                int[] yPts = { cy, cy + r / 3, cy - r / 3 };
                g2.drawPolyline(xPts, yPts, 3);

                g2.dispose();
            }
        };
        iconPanel.setOpaque(false);
        iconPanel.setPreferredSize(new Dimension(72, 72));
        iconPanel.setMaximumSize(new Dimension(72, 72));
        iconPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLbl = new JLabel("Đặt vé thành công!");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLbl.setForeground(SUCCESS_DARK);
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLbl = new JLabel("Vé đã được lưu vào hệ thống.");
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subLbl.setForeground(ON_SURF_VAR);
        subLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(340, 1));
        sep.setForeground(OUTLINE);

        JLabel hint1 = new JLabel("Bạn có thể:");
        hint1.setFont(new Font("Segoe UI", Font.BOLD, 13));
        hint1.setForeground(ON_SURFACE);
        hint1.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel hint2 = new JLabel("• Nhấn \"In vé\" để in vé cho khách");
        hint2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        hint2.setForeground(ON_SURF_VAR);
        hint2.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel hint3 = new JLabel("• Nhấn \"Đặt vé mới\" để tiếp tục bán vé");
        hint3.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        hint3.setForeground(ON_SURF_VAR);
        hint3.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(iconPanel);
        card.add(Box.createVerticalStrut(20));
        card.add(titleLbl);
        card.add(Box.createVerticalStrut(8));
        card.add(subLbl);
        card.add(Box.createVerticalStrut(24));
        card.add(sep);
        card.add(Box.createVerticalStrut(16));
        card.add(hint1);
        card.add(Box.createVerticalStrut(8));
        card.add(hint2);
        card.add(Box.createVerticalStrut(4));
        card.add(hint3);

        return card;
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

    @Override public String getTitle() { return "Bước 8 – Hoàn thành"; }
    @Override public JPanel getView()  { return this; }

    @Override
    public void setOnResult(Consumer<Object> cb) {
        this.callback = cb;
        boolean show = cb != null;
        btnSubmit.setVisible(show);
        btnCancel.setVisible(show);
        btnPanel.setVisible(show);
    }

    @Override public void reset() {}
}
