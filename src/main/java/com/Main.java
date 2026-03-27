package com;

import com.connectDB.ConnectDB;
import com.entity.NhanVien;
import com.modules.LoginModule;
import com.modules.MenuModule;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;

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
            System.err.println("Khong the cai dxat FlatLaf: " + e.getMessage());
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
                int confirm = JOptionPane.showConfirmDialog(mainFrame,
                        "Ban co chac muon dang xuat?", "Xac nhan",
                        JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
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

    public static JButton createCloseButton() {
        JButton btn = new JButton("\u2715");
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btn.setForeground(new Color(0x64, 0x74, 0x8B));
        btn.setPreferredSize(new Dimension(40, 32));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setForeground(new Color(0xBA, 0x1A, 0x1A));
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setForeground(new Color(0x64, 0x74, 0x8B));
            }
        });
        return btn;
    }
}
