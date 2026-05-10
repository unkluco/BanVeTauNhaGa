package com;

import com.connectDB.ConnectDB;
import com.entity.NhanVien;
import com.modules.AppColors;
import com.modules.LoginModule;
import com.modules.MenuModule;
import com.modules.NotionMessageDialog;

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
        wrapper.setBorder(BorderFactory.createLineBorder(AppColors.BORDER, 1));

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
        String who = (userName == null || userName.isBlank()) ? "b\u1ea1n" : userName;
        int result = NotionMessageDialog.showConfirmDialog(
                owner,
                "B\u1ea1n c\u00f3 ch\u1eafc mu\u1ed1n \u0111\u0103ng xu\u1ea5t t\u00e0i kho\u1ea3n " + who + "?",
                "X\u00e1c nh\u1eadn \u0111\u0103ng xu\u1ea5t",
                JOptionPane.WARNING_MESSAGE,
                "H\u1ee7y",
                "\u0110\u0103ng xu\u1ea5t"
        );
        return result == JOptionPane.YES_OPTION;
    }

    private static JPanel buildDialogShadowWrapper(JPanel content) {
        return new ShadowWrapperPanel(16, content);
    }

    public static JButton createCloseButton() {
        JButton btn = new JButton("X");
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btn.setForeground(AppColors.TEXT_SECONDARY);
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
                b.setForeground(AppColors.ERROR_DARK);
            }
        }

        @Override
        public void mouseExited(java.awt.event.MouseEvent e) {
            if (e.getSource() instanceof JButton b) {
                b.setForeground(AppColors.TEXT_SECONDARY);
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
            setBackground(AppColors.BACKGROUND);
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
