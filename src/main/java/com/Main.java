package com;

import com.connectDB.ConnectDB;
import com.entity.NhanVien;
import com.modules.LoginModule;
import com.modules.MenuModule;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class Main {

    private static NhanVien currentUser;

    public static NhanVien getCurrentUser() {
        return currentUser;
    }	

    public static void main(String[] args) {
        try {
            FlatLightLaf.setup();
            UIManager.put("Button.arc", 12);
            UIManager.put("TextComponent.arc", 12);
            UIManager.put("Component.arc", 12);
        } catch (Exception e) {
            System.err.println("Không thể cài đặt FlatLaf: " + e.getMessage());
        }

        SwingUtilities.invokeLater(() -> showLogin());
    }

    private static void showLogin() {
        JFrame loginFrame = new JFrame("Dang nhap | Quay Ve Azure Rail");
        loginFrame.setUndecorated(true);
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setResizable(false);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(BorderFactory.createLineBorder(new Color(0xDE, 0xE3, 0xE8), 1));

        LoginModule loginModule = new LoginModule();
        loginModule.setOnResult(result -> {
            if (result instanceof NhanVien nv) {
                currentUser = nv;
                loginFrame.dispose();
                showMainScreen(nv);
            }
        });

        wrapper.add(loginModule.getView(), BorderLayout.CENTER);

        loginFrame.setContentPane(wrapper);
        loginFrame.pack();

        loginFrame.setLocationRelativeTo(null);
        loginFrame.setVisible(true);
    }

    private static void showMainScreen(NhanVien nv) {
        JFrame mainFrame = new JFrame("Quay Ve Azure Rail");
        mainFrame.setUndecorated(true);
        mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        MenuModule menuModule = new MenuModule(nv);
        menuModule.setOnResult(result -> {
            if ("LOGOUT".equals(result)) {
                if (showLogoutConfirmDialog(mainFrame, nv.getHoTen())) {
                    currentUser = null;
                    mainFrame.dispose();
                    showLogin();
                }
            }
        });

        mainFrame.setContentPane(menuModule.getView());
        mainFrame.setMinimumSize(new Dimension(1024, 680));
        mainFrame.setSize(1280, 800);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        mainFrame.setVisible(true);
    }

    private static boolean showLogoutConfirmDialog(Window owner, String userName) {
        final boolean[] confirmed = {false};

        final Color PRIMARY     = new Color(0x00, 0x5D, 0x90);
        final Color CARD_BG     = Color.WHITE;
        final Color OUTLINE     = new Color(0xDE, 0xE3, 0xE8);
        final Color HEADER_BG   = new Color(0xF1, 0xF5, 0xF9);
        final Color FOOTER_BG   = new Color(0xF1, 0xF5, 0xF9);
        final Color ON_SURFACE  = new Color(0x1A, 0x1D, 0x21);
        final Color ON_SURF_VAR = new Color(0x5F, 0x67, 0x70);
        final Color WARN_BG     = new Color(0xFF, 0xF3, 0xCD);
        final Color WARN_FG     = new Color(0x92, 0x60, 0x10);

        JDialog dialog = new JDialog(owner, "Xác nhận đăng xuất", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setResizable(false);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CARD_BG);
        root.setBorder(BorderFactory.createLineBorder(OUTLINE, 1));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, OUTLINE),
                new EmptyBorder(18, 24, 14, 24)
        ));

        JPanel headLeft = new JPanel();
        headLeft.setLayout(new BoxLayout(headLeft, BoxLayout.Y_AXIS));
        headLeft.setOpaque(false);

        JLabel lblTitle = new JLabel("Xác nhận đăng xuất");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(PRIMARY);

        JLabel lblDesc = new JLabel("Phiên làm việc hiện tại sẽ kết thúc.");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDesc.setForeground(ON_SURF_VAR);

        headLeft.add(lblTitle);
        headLeft.add(Box.createVerticalStrut(3));
        headLeft.add(lblDesc);
        header.add(headLeft, BorderLayout.CENTER);

        JPanel body = new JPanel(new BorderLayout(14, 0));
        body.setBackground(CARD_BG);
        body.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel iconWrap = new WarningIconPanel(WARN_BG, WARN_FG, 36);

        String who = (userName == null || userName.isBlank()) ? "bạn" : userName;
        JLabel lblMsg = new JLabel("<html>Bạn có chắc muốn đăng xuất tài khoản <b>" + who + "</b>?</html>");
        lblMsg.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblMsg.setForeground(ON_SURFACE);

        body.add(iconWrap, BorderLayout.WEST);
        body.add(lblMsg, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setBackground(FOOTER_BG);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, OUTLINE),
                new EmptyBorder(14, 24, 14, 24)
        ));

        JButton btnCancel = new JButton("Hủy");
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCancel.setForeground(ON_SURF_VAR);
        btnCancel.setBackground(Color.WHITE);
        btnCancel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1, true),
                new EmptyBorder(8, 14, 8, 14)
        ));
        btnCancel.setFocusPainted(false);
        btnCancel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCancel.addActionListener(e -> dialog.dispose());

        JButton btnLogout = new PrimaryRoundButton("Đăng xuất", PRIMARY, 10);
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.setPreferredSize(new Dimension(112, 36));
        btnLogout.addActionListener(e -> {
            confirmed[0] = true;
            dialog.dispose();
        });

        footer.add(btnCancel);
        footer.add(btnLogout);

        root.add(header, BorderLayout.NORTH);
        root.add(body, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        dialog.setContentPane(buildDialogShadowWrapper(root));
        dialog.pack();
        dialog.setMinimumSize(new Dimension(480, dialog.getPreferredSize().height));
        dialog.setLocationRelativeTo(owner);

        dialog.getRootPane().setDefaultButton(btnLogout);
        dialog.getRootPane().registerKeyboardAction(
                e -> dialog.dispose(),
                KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        dialog.setVisible(true);
        return confirmed[0];
    }

    private static JPanel buildDialogShadowWrapper(JPanel content) {
        return new ShadowWrapperPanel(16, content);
    }

    public static JButton createCloseButton() {
        JButton btn = new JButton("X");
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btn.setForeground(new Color(0x64, 0x74, 0x8B));
        btn.setPreferredSize(new Dimension(40, 32));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new CloseButtonHoverListener());
        return btn;
    }

    private static class CloseButtonHoverListener extends java.awt.event.MouseAdapter {
        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
            if (e.getSource() instanceof JButton b) {
                b.setForeground(new Color(0xBA, 0x1A, 0x1A));
            }
        }

        @Override
        public void mouseExited(java.awt.event.MouseEvent e) {
            if (e.getSource() instanceof JButton b) {
                b.setForeground(new Color(0x64, 0x74, 0x8B));
            }
        }
    }

    private static class PrimaryRoundButton extends JButton {
        private final Color primary;
        private final int arc;

        PrimaryRoundButton(String text, Color primary, int arc) {
            super(text);
            this.primary = primary;
            this.arc = arc;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color bg = getModel().isPressed()
                    ? primary.darker()
                    : (getModel().isRollover() ? primary.brighter() : primary);
            g2.setColor(bg);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), arc, arc));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class WarningIconPanel extends JPanel {
        private final Color bg;
        private final Color fg;
        private final int size;

        WarningIconPanel(Color bg, Color fg, int size) {
            this.bg = bg;
            this.fg = fg;
            this.size = size;
            setOpaque(false);
            setPreferredSize(new Dimension(size, size));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillOval(0, 0, getWidth(), getHeight());
            g2.setColor(fg);
            g2.setFont(new Font("Segoe UI", Font.BOLD, Math.max(16, size / 2)));
            String text = "!";
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(text)) / 2;
            int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(text, x, y);
            g2.dispose();
        }
    }

    private static class ShadowWrapperPanel extends JPanel {
        private final int pad;

        ShadowWrapperPanel(int pad, JPanel content) {
            super(new BorderLayout());
            this.pad = pad;
            setOpaque(true);
            setBackground(new Color(0xEE, 0xF2, 0xF6));
            setBorder(BorderFactory.createEmptyBorder(pad, pad, pad + 4, pad));
            add(content, BorderLayout.CENTER);
        }

        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (int i = 1; i <= pad; i++) {
                int alpha = Math.round(18f * i / pad);
                g2.setColor(new Color(0, 0, 0, alpha));
                int offset = pad - i;
                g2.fillRoundRect(offset, offset + 3,
                        getWidth() - offset * 2,
                        getHeight() - offset * 2, 8, 8);
            }
            g2.dispose();
        }
    }
}
