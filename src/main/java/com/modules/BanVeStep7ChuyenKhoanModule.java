package com.modules;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Bước 7 (Chuyển khoản) — Thanh toán chuyển khoản / QR.
 * Hiển thị QR code mẫu và thông tin chuyển khoản.
 * Output: "CONFIRMED" khi xác nhận đã nhận tiền.
 */
public class BanVeStep7ChuyenKhoanModule extends JPanel implements AppModule {

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

    private static final String BANK_NAME    = "Vietcombank";
    private static final String ACCOUNT_NUM  = "1234567890";
    private static final String ACCOUNT_NAME = "CONG TY BAN VE TAU NHA GA";

    private JButton btnSubmit, btnCancel;
    private JPanel  btnPanel;

    // =========================================================================
    //  CONSTRUCTOR
    // =========================================================================

    public BanVeStep7ChuyenKhoanModule(BigDecimal total) {
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
        JLabel hdr = new JLabel("Thanh toán — Chuyển khoản / QR");
        hdr.setFont(new Font("Segoe UI", Font.BOLD, 15));
        hdr.setForeground(PRIMARY);
        header.add(hdr);
        add(header, BorderLayout.NORTH);

        // Center: two-column layout (QR left, bank info right)
        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(SURFACE);
        center.setBorder(new EmptyBorder(24, 40, 24, 40));

        JPanel qrCard   = buildQrCard();
        JPanel infoCard = buildInfoCard();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill    = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.insets  = new Insets(0, 0, 0, 12);

        gbc.weightx = 0.4;
        center.add(qrCard, gbc);

        gbc.insets  = new Insets(0, 0, 0, 0);
        gbc.weightx = 0.6;
        center.add(infoCard, gbc);

        add(center, BorderLayout.CENTER);

        // Buttons
        btnSubmit = new JButton("Xác nhận đã nhận tiền →");
        styleBtn(btnSubmit, true);
        btnSubmit.addActionListener(e -> { if (callback != null) callback.accept("CONFIRMED"); });

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

    private JPanel buildQrCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(OUTLINE, 1),
            new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel title = new JLabel("Mã QR thanh toán");
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(ON_SURF_VAR);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        BufferedImage qrImg = generateQrImage(220);
        JLabel qrLabel = new JLabel(new ImageIcon(qrImg));
        qrLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        qrLabel.setBorder(BorderFactory.createLineBorder(OUTLINE, 1));

        JLabel hint = new JLabel("Quét QR bằng ứng dụng ngân hàng");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hint.setForeground(ON_SURF_VAR);
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(title);
        card.add(Box.createVerticalStrut(12));
        card.add(qrLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(hint);
        return card;
    }

    private JPanel buildInfoCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(OUTLINE, 1),
            new EmptyBorder(24, 28, 24, 28)
        ));

        JLabel title = new JLabel("Thông tin chuyển khoản");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(ON_SURFACE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(title);
        card.add(Box.createVerticalStrut(20));

        addInfoRow(card, "Ngân hàng:",    BANK_NAME,    ON_SURFACE, false);
        addInfoRow(card, "Số tài khoản:", ACCOUNT_NUM,  PRIMARY,    true);
        addInfoRow(card, "Chủ tài khoản:", ACCOUNT_NAME, ON_SURFACE, false);

        // Divider
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(OUTLINE);
        card.add(sep);
        card.add(Box.createVerticalStrut(12));

        addInfoRow(card, "Số tiền:",     VND_FMT.format(total.longValue()) + " ₫",
            AMOUNT_COLOR, true);
        addInfoRow(card, "Nội dung CK:", "DAT VE TAU",  ON_SURFACE, false);

        card.add(Box.createVerticalStrut(20));

        JPanel warningBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        warningBox.setBackground(new Color(0xFF, 0xF8, 0xE1));
        warningBox.setBorder(BorderFactory.createLineBorder(new Color(0xFF, 0xD5, 0x4F), 1));
        warningBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        warningBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel warnLbl = new JLabel("Vui lòng kiểm tra đã nhận tiền trước khi xác nhận.");
        warnLbl.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        warnLbl.setForeground(new Color(0x7B, 0x5D, 0x00));
        warningBox.add(warnLbl);
        card.add(warningBox);

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
    //  QR GENERATION
    // =========================================================================

    /**
     * Tạo ảnh QR giả với pattern ngẫu nhiên + finder patterns chuẩn.
     */
    private BufferedImage generateQrImage(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, size, size);

        int cells = 25;
        int cell  = size / cells;
        Random rng = new Random(total.longValue() ^ ACCOUNT_NUM.hashCode());

        // Random data cells (excluding finder pattern areas)
        g2.setColor(Color.BLACK);
        for (int r = 0; r < cells; r++) {
            for (int c = 0; c < cells; c++) {
                if (isFinderZone(r, c, cells)) continue;
                if (rng.nextBoolean()) {
                    g2.fillRect(c * cell, r * cell, cell, cell);
                }
            }
        }

        // Finder patterns (3 corners)
        drawFinder(g2, 0, 0, cell);
        drawFinder(g2, 0, (cells - 7) * cell, cell);
        drawFinder(g2, (cells - 7) * cell, 0, cell);

        // Quiet zone border
        g2.setColor(Color.WHITE);
        g2.drawRect(0, 0, size - 1, size - 1);

        g2.dispose();
        return img;
    }

    private boolean isFinderZone(int r, int c, int cells) {
        return (r < 9 && c < 9)
            || (r < 9 && c >= cells - 8)
            || (r >= cells - 8 && c < 9);
    }

    private void drawFinder(Graphics2D g2, int x, int y, int cell) {
        // Outer 7×7 black square
        g2.setColor(Color.BLACK);
        g2.fillRect(x, y, cell * 7, cell * 7);
        // Inner 5×5 white
        g2.setColor(Color.WHITE);
        g2.fillRect(x + cell, y + cell, cell * 5, cell * 5);
        // Center 3×3 black
        g2.setColor(Color.BLACK);
        g2.fillRect(x + cell * 2, y + cell * 2, cell * 3, cell * 3);
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

    @Override public String getTitle() { return "Bước 7 – Chuyển khoản"; }
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
