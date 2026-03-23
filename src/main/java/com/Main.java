package com;

import com.connectDB.ConnectDB;
import com.entity.NhanVien;
import com.modules.LoginModule;
import com.modules.MenuModule;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;

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
            System.err.println("Khong the cai dat FlatLaf: " + e.getMessage());
        }

        SwingUtilities.invokeLater(() -> showLogin());
    }

    private static void showLogin() {
        JFrame loginFrame = new JFrame("Dang nhap | Quay Ve Azure Rail");
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setResizable(false);

        LoginModule loginModule = new LoginModule();
        loginModule.setOnResult(result -> {
            if (result instanceof NhanVien nv) {
                currentUser = nv;
                loginFrame.dispose();
                showMainScreen(nv);
            }
        });

        loginFrame.setContentPane(loginModule.getView());
        loginFrame.pack();
        loginFrame.setLocationRelativeTo(null);
        loginFrame.setVisible(true);
    }

    private static void showMainScreen(NhanVien nv) {
        JFrame mainFrame = new JFrame("Quay Ve Azure Rail");
        mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        mainFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                int confirm = JOptionPane.showConfirmDialog(mainFrame,
                        "Ban co chac muon thoat?", "Xac nhan",
                        JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    ConnectDB.getInstance().disconnect();
                    System.exit(0);
                }
            }
        });

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
        mainFrame.setSize(1280, 800);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }
}
