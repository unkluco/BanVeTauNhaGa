package com.modules;

import com.entity.Ve;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class HoanVeDialog extends JDialog {

    private static final Color PRIMARY       = new Color(0x00, 0x5D, 0x90);
    private static final Color DANGER        = new Color(0xB9, 0x1C, 0x1C);
    private static final Color DANGER_HOVER  = new Color(0x99, 0x12, 0x12);
    private static final Color CARD_BG       = Color.WHITE;
    private static final Color ON_SURFACE    = new Color(0x1A, 0x1D, 0x21);
    private static final Color ON_SURF_VAR   = new Color(0x5F, 0x67, 0x70);
    private static final Color OUTLINE       = new Color(0xDE, 0xE3, 0xE8);
    private static final Color HEADER_BG     = new Color(0xF1, 0xF5, 0xF9);
    private static final Color FOOTER_BG     = new Color(0xF1, 0xF5, 0xF9);
    private static final Color ERROR         = new Color(0xBA, 0x1A, 0x1A);

    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font FONT_DESC  = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font FONT_VALUE = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_BTN   = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_ERR   = new Font("Segoe UI", Font.PLAIN, 11);

    private boolean confirmed = false;
    private String lyDo;

    private JTextArea txtLyDo;
    private JLabel lblErrLyDo;

    public HoanVeDialog(Window owner, Ve ve, String hanhKhach, String hanhTrinh, String khoiHanh) {
        super(owner, "Hoàn vé", ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        initUI(ve, hanhKhach, hanhTrinh, khoiHanh);
        pack();
        setMinimumSize(new Dimension(650, getPreferredSize().height));
        setLocationRelativeTo(owner);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public String getLyDo() {
        return lyDo;
    }

    private void initUI(Ve ve, String hanhKhach, String hanhTrinh, String khoiHanh) {
        setBackground(new Color(0, 0, 0, 0));

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CARD_BG);
        root.setBorder(BorderFactory.createLineBorder(OUTLINE, 1));

        root.add(buildHeader(ve), BorderLayout.NORTH);
        root.add(buildBody(ve, hanhKhach, hanhTrinh, khoiHanh), BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);

        setContentPane(ThemNhanVienDialog.buildShadowWrapper(root));
        getRootPane().registerKeyboardAction(
            e -> dispose(),
            KeyStroke.getKeyStroke("ESCAPE"),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    private JPanel buildHeader(Ve ve) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, OUTLINE),
            new EmptyBorder(20, 28, 16, 28)
        ));

        JPanel left = new JPanel(new BorderLayout(12, 0));
        left.setOpaque(false);

        ImageIcon icon = loadScaledIcon("bieuTuongCanhBao.png", 34);
        if (icon != null) {
            JLabel lblIcon = new JLabel(icon);
            lblIcon.setVerticalAlignment(SwingConstants.TOP);
            left.add(lblIcon, BorderLayout.WEST);
        }

        JPanel textPart = new JPanel();
        textPart.setLayout(new BoxLayout(textPart, BoxLayout.Y_AXIS));
        textPart.setOpaque(false);

        JLabel lblTitle = new JLabel("Xác nhận hoàn vé " + (ve != null ? ve.getMaVe() : ""));
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(DANGER);

        JLabel lblDesc = new JLabel("Vui lòng nhập lý do. Thao tác này sẽ cập nhật trạng thái vé sang đã hủy.");
        lblDesc.setFont(FONT_DESC);
        lblDesc.setForeground(ON_SURF_VAR);

        textPart.add(lblTitle);
        textPart.add(Box.createVerticalStrut(4));
        textPart.add(lblDesc);

        left.add(textPart, BorderLayout.CENTER);
        header.add(left, BorderLayout.CENTER);
        return header;
    }

    private JPanel buildBody(Ve ve, String hanhKhach, String hanhTrinh, String khoiHanh) {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(CARD_BG);
        body.setBorder(new EmptyBorder(20, 24, 12, 24));

        JPanel summaryCard = new JPanel(new GridLayout(2, 2, 14, 8)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xF8, 0xFA, 0xFC));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(OUTLINE);
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 12, 12));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        summaryCard.setOpaque(false);
        summaryCard.setBorder(new EmptyBorder(12, 14, 12, 14));
        summaryCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));
        summaryCard.add(summaryItem("Mã vé", ve == null ? "—" : ve.getMaVe()));
        summaryCard.add(summaryItem("Hành khách", fallback(hanhKhach)));
        summaryCard.add(summaryItem("Hành trình", fallback(hanhTrinh)));
        summaryCard.add(summaryItem("Khởi hành", fallback(khoiHanh)));

        body.add(summaryCard);
        body.add(Box.createVerticalStrut(12));

        JLabel lblReason = new JLabel("Lý do hủy vé *");
        lblReason.setFont(FONT_LABEL);
        lblReason.setForeground(ON_SURF_VAR);
        lblReason.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(lblReason);
        body.add(Box.createVerticalStrut(6));

        txtLyDo = new JTextArea(4, 30);
        txtLyDo.setLineWrap(true);
        txtLyDo.setWrapStyleWord(true);
        txtLyDo.setFont(FONT_VALUE);
        txtLyDo.setForeground(ON_SURFACE);
        txtLyDo.setBorder(new EmptyBorder(8, 10, 8, 10));
        JScrollPane sp = new JScrollPane(txtLyDo);
        sp.setBorder(BorderFactory.createLineBorder(OUTLINE, 1));
        sp.setPreferredSize(new Dimension(0, 92));
        sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));
        body.add(sp);

        lblErrLyDo = new JLabel(" ");
        lblErrLyDo.setFont(FONT_ERR);
        lblErrLyDo.setForeground(ERROR);
        lblErrLyDo.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(Box.createVerticalStrut(3));
        body.add(lblErrLyDo);

        return body;
    }

    private JPanel summaryItem(String label, String value) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(ON_SURF_VAR);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel val = new JLabel(value);
        val.setFont(FONT_VALUE);
        val.setForeground(ON_SURFACE);
        val.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(lbl);
        p.add(Box.createVerticalStrut(2));
        p.add(val);
        return p;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setBackground(FOOTER_BG);
        footer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, OUTLINE),
            new EmptyBorder(14, 24, 14, 24)
        ));

        JButton btnCancel = createOutlineButton("Hủy bỏ");
        ImageIcon icoHuy = loadScaledIcon("nutThoat.png", 15);
        if (icoHuy != null) {
            btnCancel.setIcon(icoHuy);
            btnCancel.setIconTextGap(8);
        }
        btnCancel.addActionListener(e -> dispose());

        JButton btnConfirm = createDangerButton("Xác nhận hoàn vé");
        btnConfirm.addActionListener(e -> onConfirm());
        getRootPane().setDefaultButton(btnConfirm);

        footer.add(btnCancel);
        footer.add(btnConfirm);
        return footer;
    }

    private void onConfirm() {
        String reason = txtLyDo.getText() == null ? "" : txtLyDo.getText().trim();
        if (reason.isEmpty()) {
            lblErrLyDo.setText("Vui lòng nhập lý do hủy vé.");
            txtLyDo.requestFocusInWindow();
            return;
        }
        if (reason.length() < 5) {
            lblErrLyDo.setText("Lý do tối thiểu 5 ký tự.");
            txtLyDo.requestFocusInWindow();
            return;
        }

        confirmed = true;
        lyDo = reason;
        dispose();
    }

    private JButton createDangerButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? DANGER_HOVER : DANGER);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
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
        btn.setPreferredSize(new Dimension(170, 40));
        return btn;
    }

    private JButton createOutlineButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(0xF1, 0xF5, 0xF9));
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BTN);
        btn.setForeground(ON_SURF_VAR);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(110, 40));
        return btn;
    }

    private ImageIcon loadScaledIcon(String name, int size) {
        try {
            java.net.URL url = getClass().getResource("/icons/" + name);
            if (url != null) {
                Image img = new ImageIcon(url).getImage()
                    .getScaledInstance(size, size, Image.SCALE_SMOOTH);
                return new ImageIcon(img);
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String fallback(String s) {
        if (s == null || s.isBlank()) return "—";
        return s;
    }
}
