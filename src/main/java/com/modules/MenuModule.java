package com.modules;

import com.entity.NhanVien;
import com.enums.VaiTro;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MenuModule extends JPanel implements AppModule {

    private Consumer<Object> callback;

    private final NhanVien currentUser;
    private final VaiTro   vaiTro;

    private JPanel contentPanel;
    private JLabel lblPageTitle;

    private final List<NavItem> menuItems = new ArrayList<>();
    private NavItem activeMenuItem = null;

    // Colors
    private static final Color PRIMARY        = new Color(0x00, 0x5D, 0x90);
    private static final Color PRIMARY_LIGHT  = new Color(0xCF, 0xE6, 0xF2);
    private static final Color SIDEBAR_BG     = new Color(0xF8, 0xFA, 0xFC);
    private static final Color SIDEBAR_BORDER = new Color(0xE2, 0xE8, 0xF0);
    private static final Color TEXT_DARK      = new Color(0x19, 0x1C, 0x1E);
    private static final Color TEXT_MUTED     = new Color(0x64, 0x74, 0x8B);
    private static final Color TEXT_DISABLED  = new Color(0xA0, 0xAE, 0xBA);
    private static final Color SURFACE        = new Color(0xF7, 0xF9, 0xFB);
    private static final Color HOVER_BG       = new Color(0xE4, 0xF0, 0xF8);
    private static final Color ACTIVE_BG      = new Color(0xE8, 0xF2, 0xFA);

    public MenuModule(NhanVien user) {
        this.currentUser = user;
        this.vaiTro = user.getVaiTro();
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        // ============ SIDEBAR ============
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(270, 0));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, SIDEBAR_BORDER));

        // Sidebar header
        JPanel sidebarHeader = new JPanel();
        sidebarHeader.setLayout(new BoxLayout(sidebarHeader, BoxLayout.Y_AXIS));
        sidebarHeader.setBackground(SIDEBAR_BG);
        sidebarHeader.setBorder(new EmptyBorder(24, 24, 24, 24));

        JLabel lblBrand = new JLabel("Azure Rail");
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblBrand.setForeground(PRIMARY);
        lblBrand.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSub = new JLabel("H\u1EC7 th\u1ED1ng Qu\u1EA3n l\u00FD");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(TEXT_MUTED);
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebarHeader.add(lblBrand);
        sidebarHeader.add(Box.createVerticalStrut(2));
        sidebarHeader.add(lblSub);

        // Sidebar nav
        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBackground(SIDEBAR_BG);
        nav.setBorder(new EmptyBorder(0, 12, 0, 12));

        // Primary action button
        JPanel btnDatVe = createPrimaryButton("\u0110\u1EB7t v\u00E9 m\u1EDBi");
        btnDatVe.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setActive(null);
                showPlaceholder("\u0110\u1EB7t v\u00E9 m\u1EDBi",
                        "Ch\u1EE9c n\u0103ng \u0111\u1EB7t v\u00E9 m\u1EDBi s\u1EBD \u0111\u01B0\u1EE3c tri\u1EC3n khai t\u1EA1i \u0111\u00E2y.");
            }
        });
        nav.add(btnDatVe);
        nav.add(Box.createVerticalStrut(16));

        // Menu items (only Vietnamese labels)
        addMenuItem(nav, "T\u1ED5ng quan",                                              "TONG_QUAN");
        addMenuItem(nav, "Th\u1ED1ng k\u00EA",                                           "THONG_KE");
        addMenuItem(nav, "Qu\u1EA3n l\u00FD gi\u00E1",                                  "QL_GIA");
        addMenuItem(nav, "Qu\u1EA3n l\u00FD khuy\u1EBFn m\u00E3i",                      "QL_KHUYEN_MAI");
        addMenuItem(nav, "Qu\u1EA3n l\u00FD v\u00E9",                                    "QL_VE_HOA_DON");
        addMenuItem(nav, "Qu\u1EA3n l\u00FD h\u00F3a \u0111\u01A1n",                    "QL_HOA_DON");
        addMenuItem(nav, "Qu\u1EA3n l\u00FD \u0111o\u00E0n t\u00E0u",                   "QL_DOAN_TAU");
        addMenuItem(nav, "Qu\u1EA3n l\u00FD Toa v\u00E0 \u0110\u1EA7u m\u00E1y",        "QL_TOA");
        addMenuItem(nav, "Qu\u1EA3n l\u00FD tuy\u1EBFn",                                "QL_TUYEN");
        addMenuItem(nav, "Qu\u1EA3n l\u00FD l\u1ECBch ch\u1EA1y",                       "QL_LICH_CHAY");
        addMenuItem(nav, "Qu\u1EA3n l\u00FD nh\u00E2n vi\u00EAn",                       "QL_NHAN_VIEN");

        applyRoleRestrictions();

        nav.add(Box.createVerticalGlue());

        // Sidebar footer
        JPanel sidebarFooter = new JPanel();
        sidebarFooter.setLayout(new BoxLayout(sidebarFooter, BoxLayout.Y_AXIS));
        sidebarFooter.setBackground(SIDEBAR_BG);
        sidebarFooter.setBorder(new EmptyBorder(12, 12, 16, 12));

        NavItem btnThongTinCaNhan = createNavItem("Th\u00F4ng tin c\u00E1 nh\u00E2n");
        btnThongTinCaNhan.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openThongTinCaNhan();
            }
        });
        sidebarFooter.add(btnThongTinCaNhan);
        sidebarFooter.add(Box.createVerticalStrut(2));

        NavItem btnCaiDat = createNavItem("C\u00E0i \u0111\u1EB7t");
        sidebarFooter.add(btnCaiDat);
        sidebarFooter.add(Box.createVerticalStrut(2));

        NavItem btnDangXuat = createNavItem("\u0110\u0103ng xu\u1EA5t");
        btnDangXuat.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (callback != null) callback.accept("LOGOUT");
            }
        });
        sidebarFooter.add(btnDangXuat);

        // Assemble sidebar
        JPanel sidebarContent = new JPanel(new BorderLayout());
        sidebarContent.setBackground(SIDEBAR_BG);
        sidebarContent.add(sidebarHeader, BorderLayout.NORTH);

        JScrollPane navScroll = new JScrollPane(nav);
        navScroll.setBorder(null);
        navScroll.setBackground(SIDEBAR_BG);
        navScroll.getViewport().setBackground(SIDEBAR_BG);
        navScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sidebarContent.add(navScroll, BorderLayout.CENTER);

        JPanel footerWrapper = new JPanel(new BorderLayout());
        footerWrapper.setBackground(SIDEBAR_BG);
        footerWrapper.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, SIDEBAR_BORDER));
        footerWrapper.add(sidebarFooter, BorderLayout.CENTER);
        sidebarContent.add(footerWrapper, BorderLayout.SOUTH);

        sidebar.add(sidebarContent, BorderLayout.CENTER);

        // ============ HEADER ============
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 60));
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xF1, 0xF5, 0xF9)),
                new EmptyBorder(0, 32, 0, 32)
        ));

        lblPageTitle = new JLabel("Qu\u1EA3n l\u00FD B\u00E1n v\u00E9");
        lblPageTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblPageTitle.setForeground(PRIMARY);

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        userPanel.setOpaque(false);

        JPanel userInfo = new JPanel();
        userInfo.setLayout(new BoxLayout(userInfo, BoxLayout.Y_AXIS));
        userInfo.setOpaque(false);

        JLabel lblUserName = new JLabel(currentUser.getHoTen());
        lblUserName.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUserName.setForeground(TEXT_DARK);
        lblUserName.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JLabel lblUserRole = new JLabel(vaiTro.toString().toUpperCase());
        lblUserRole.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblUserRole.setForeground(TEXT_MUTED);
        lblUserRole.setAlignmentX(Component.RIGHT_ALIGNMENT);

        userInfo.add(lblUserName);
        userInfo.add(lblUserRole);

        // Avatar
        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(PRIMARY);
                g2.fillOval(0, 0, 36, 36);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                String initials = getInitials(currentUser.getHoTen());
                FontMetrics fm = g2.getFontMetrics();
                int x = (36 - fm.stringWidth(initials)) / 2;
                int y = (36 + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(initials, x, y);
                g2.dispose();
            }
        };
        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(36, 36));

        userPanel.add(userInfo);
        userPanel.add(avatar);
        userPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        userPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openThongTinCaNhan();
            }
        });

        header.add(lblPageTitle, BorderLayout.WEST);
        header.add(userPanel, BorderLayout.EAST);

        // ============ CONTENT ============
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(SURFACE);

        showPlaceholder("T\u1ED5ng quan",
                "Ch\u00E0o m\u1EEBng " + currentUser.getHoTen()
                        + "! H\u00E3y ch\u1ECDn ch\u1EE9c n\u0103ng t\u1EEB menu b\u00EAn tr\u00E1i.");

        // ============ MAIN (header + content) ============
        JPanel mainArea = new JPanel(new BorderLayout());
        mainArea.add(header, BorderLayout.NORTH);
        mainArea.add(contentPanel, BorderLayout.CENTER);

        add(sidebar, BorderLayout.WEST);
        add(mainArea, BorderLayout.CENTER);
    }

    // =====================================================================
    //  NavItem — custom JPanel that paints its own background (no alpha glitch)
    // =====================================================================

    private static class NavItem extends JPanel {
        private Color bgColor = null;    // null = transparent
        private boolean rightBorder = false;

        NavItem() {
            super(new BorderLayout());
            setOpaque(false);            // always false — we draw bg ourselves
        }

        void setHighlight(Color bg, boolean border) {
            this.bgColor = bg;
            this.rightBorder = border;
            repaint();
        }

        void clearHighlight() {
            this.bgColor = null;
            this.rightBorder = false;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (bgColor != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                if (rightBorder) {
                    g2.setColor(PRIMARY);
                    g2.fillRoundRect(getWidth() - 3, 4, 3, getHeight() - 8, 3, 3);
                }
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }

    // =====================================================================
    //  MENU ITEMS
    // =====================================================================

    private void addMenuItem(JPanel nav, String label, String actionKey) {
        NavItem item = createNavItem(label);
        item.setName(actionKey);

        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!item.isEnabled()) return;
                setActive(item);
                handleMenuAction(actionKey, label);
            }
        });

        menuItems.add(item);
        nav.add(item);
        nav.add(Box.createVerticalStrut(2));
    }

    private NavItem createNavItem(String label) {
        NavItem panel = new NavItem();
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        panel.setPreferredSize(new Dimension(240, 44));
        panel.setBorder(new EmptyBorder(0, 16, 0, 8));

        JLabel lblText = new JLabel(label);
        lblText.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblText.setForeground(TEXT_MUTED);
        panel.add(lblText, BorderLayout.CENTER);

        // Hover effect
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!panel.isEnabled() || panel == activeMenuItem) return;
                panel.setHighlight(HOVER_BG, false);
                lblText.setForeground(PRIMARY);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (panel == activeMenuItem) return;
                panel.clearHighlight();
                lblText.setForeground(panel.isEnabled() ? TEXT_MUTED : TEXT_DISABLED);
            }
        });

        return panel;
    }

    private JPanel createPrimaryButton(String label) {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        panel.setPreferredSize(new Dimension(240, 48));
        panel.setBorder(new EmptyBorder(0, 20, 0, 8));

        JLabel lblText = new JLabel(label);
        lblText.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblText.setForeground(Color.WHITE);
        panel.add(lblText, BorderLayout.CENTER);

        return panel;
    }

    private void setActive(NavItem item) {
        // Reset previous
        if (activeMenuItem != null) {
            activeMenuItem.clearHighlight();
            for (Component c : activeMenuItem.getComponents()) {
                if (c instanceof JLabel lbl) {
                    lbl.setForeground(TEXT_MUTED);
                    lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN));
                }
            }
        }

        activeMenuItem = item;

        if (item != null) {
            item.setHighlight(ACTIVE_BG, true);
            for (Component c : item.getComponents()) {
                if (c instanceof JLabel lbl) {
                    lbl.setForeground(PRIMARY);
                    lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
                }
            }
        }
    }

    // =====================================================================
    //  ROLE RESTRICTIONS
    // =====================================================================

    private void applyRoleRestrictions() {
        if (vaiTro == VaiTro.ADMIN) return;

        if (vaiTro == VaiTro.DIEU_PHOI) {
            disableMenuItem("QL_NHAN_VIEN");
            return;
        }

        if (vaiTro == VaiTro.BAN_VE) {
            disableMenuItem("QL_NHAN_VIEN");
            disableMenuItem("QL_TUYEN");
            disableMenuItem("QL_LICH_CHAY");
            disableMenuItem("QL_DOAN_TAU");
            disableMenuItem("QL_TOA");
            disableMenuItem("QL_KHUYEN_MAI");
            disableMenuItem("QL_GIA");
        }
    }

    private void disableMenuItem(String actionKey) {
        for (NavItem item : menuItems) {
            if (actionKey.equals(item.getName())) {
                item.setEnabled(false);
                item.setCursor(Cursor.getDefaultCursor());
                for (Component c : item.getComponents()) {
                    c.setEnabled(false);
                    if (c instanceof JLabel lbl) {
                        lbl.setForeground(TEXT_DISABLED);
                    }
                }
                item.setToolTipText("B\u1EA1n kh\u00F4ng c\u00F3 quy\u1EC1n truy c\u1EADp ch\u1EE9c n\u0103ng n\u00E0y");
                break;
            }
        }
    }

    // =====================================================================
    //  MENU ACTION HANDLER
    // =====================================================================

    private void handleMenuAction(String actionKey, String label) {
        lblPageTitle.setText(label);

        switch (actionKey) {
            case "TONG_QUAN" -> {
                TongQuatModule tqModule = new TongQuatModule();
                tqModule.setOnResult(null);
                contentPanel.removeAll();
                contentPanel.add(tqModule.getView(), BorderLayout.CENTER);
                contentPanel.revalidate();
                contentPanel.repaint();
            }
            case "THONG_KE" -> {
                try {
                    ThongKeModule module = new ThongKeModule();
                    module.setOnResult(null);
                    contentPanel.removeAll();
                    contentPanel.add(module.getView(), BorderLayout.CENTER);
                    contentPanel.revalidate();
                    contentPanel.repaint();
                } catch (Exception ex) {
                    System.err.println("[ERROR] Kh\u00F4ng th\u1EC3 m\u1EDF ThongKeModule:");
                    ex.printStackTrace();
                    showPlaceholder("L\u1ED7i", "Kh\u00F4ng th\u1EC3 t\u1EA3i module Th\u1ED1ng k\u00EA: " + ex.getMessage());
                }
            }
            case "QL_NHAN_VIEN" -> {
                QuanLyNhanVienModule module = new QuanLyNhanVienModule();
                module.setOnResult(null);
                contentPanel.removeAll();
                contentPanel.add(module.getView(), BorderLayout.CENTER);
                contentPanel.revalidate();
                contentPanel.repaint();
            }
            case "QL_GIA" -> {
                QuanLyGiaModule giaModule = new QuanLyGiaModule();
                giaModule.setOnResult(null);
                contentPanel.removeAll();
                contentPanel.add(giaModule.getView(), BorderLayout.CENTER);
                contentPanel.revalidate();
                contentPanel.repaint();
            }
            case "QL_KHUYEN_MAI" -> {
                try {
                    QuanLyKhuyenMaiModule kmModule = new QuanLyKhuyenMaiModule();
                    kmModule.setOnResult(null);
                    contentPanel.removeAll();
                    contentPanel.add(kmModule.getView(), BorderLayout.CENTER);
                    contentPanel.revalidate();
                    contentPanel.repaint();
                } catch (Exception ex) {
                    System.err.println("[ERROR] Kh\u00F4ng th\u1EC3 m\u1EDF QuanLyKhuyenMaiModule:");
                    ex.printStackTrace();
                    showPlaceholder("L\u1ED7i", "Kh\u00F4ng th\u1EC3 t\u1EA3i module Qu\u1EA3n l\u00FD khuy\u1EBFn m\u00E3i: " + ex.getMessage());
                }
            }
            case "QL_VE_HOA_DON" -> {
                try {
                    QuanLyVeModule module = new QuanLyVeModule();
                    module.setOnResult(null);
                    contentPanel.removeAll();
                    contentPanel.add(module.getView(), BorderLayout.CENTER);
                    contentPanel.revalidate();
                    contentPanel.repaint();
                } catch (Exception ex) {
                    System.err.println("[ERROR] Không thể mở QuanLyVeModule:");
                    ex.printStackTrace();
                    showPlaceholder("Lỗi", "Không thể tải module Quản lý vé: " + ex.getMessage());
                }
            }
            case "QL_HOA_DON" -> {
                try {
                    QuanLyHoaDonModule module = new QuanLyHoaDonModule();
                    module.setOnResult(null);
                    contentPanel.removeAll();
                    contentPanel.add(module.getView(), BorderLayout.CENTER);
                    contentPanel.revalidate();
                    contentPanel.repaint();
                } catch (Exception ex) {
                    System.err.println("[ERROR] Kh\u00F4ng th\u1EC3 m\u1EDF QuanLyHoaDonModule:");
                    ex.printStackTrace();
                    showPlaceholder("L\u1ED7i", "Kh\u00F4ng th\u1EC3 t\u1EA3i module Qu\u1EA3n l\u00FD h\u00F3a \u0111\u01A1n: " + ex.getMessage());
                }
            }
            case "QL_TUYEN" -> {
                try {
                    QuanLyTuyenModule module = new QuanLyTuyenModule();
                    module.setOnResult(null);
                    contentPanel.removeAll();
                    contentPanel.add(module.getView(), BorderLayout.CENTER);
                    contentPanel.revalidate();
                    contentPanel.repaint();
                } catch (Exception ex) {
                    System.err.println("[ERROR] Kh\u00F4ng th\u1EC3 m\u1EDF QuanLyTuyenModule:");
                    ex.printStackTrace();
                    showPlaceholder("L\u1ED7i", "Kh\u00F4ng th\u1EC3 t\u1EA3i module Qu\u1EA3n l\u00FD tuy\u1EBFn: " + ex.getMessage());
                }
            }
            case "QL_DOAN_TAU" -> {
                try {
                    QuanLyDoanTauModule module = new QuanLyDoanTauModule();
                    module.setOnResult(null);
                    contentPanel.removeAll();
                    contentPanel.add(module.getView(), BorderLayout.CENTER);
                    contentPanel.revalidate();
                    contentPanel.repaint();
                } catch (Exception ex) {
                    System.err.println("[ERROR] Kh\u00F4ng th\u1EC3 m\u1EDF QuanLyDoanTauModule:");
                    ex.printStackTrace();
                    showPlaceholder("L\u1ED7i", "Kh\u00F4ng th\u1EC3 t\u1EA3i module Qu\u1EA3n l\u00FD \u0111o\u00E0n t\u00E0u: " + ex.getMessage());
                }
            }
            case "QL_TOA" -> {
                try {
                    QuanLyToaModule module = new QuanLyToaModule();
                    module.setOnResult(null);
                    contentPanel.removeAll();
                    contentPanel.add(module.getView(), BorderLayout.CENTER);
                    contentPanel.revalidate();
                    contentPanel.repaint();
                } catch (Exception ex) {
                    System.err.println("[ERROR] Kh\u00F4ng th\u1EC3 m\u1EDF QuanLyToaModule:");
                    ex.printStackTrace();
                    showPlaceholder("L\u1ED7i", "Kh\u00F4ng th\u1EC3 t\u1EA3i module Qu\u1EA3n l\u00FD toa: " + ex.getMessage());
                }
            }
            case "QL_LICH_CHAY" -> {
                try {
                    QuanLyLichChayModule module = new QuanLyLichChayModule();
                    module.setOnResult(null);
                    contentPanel.removeAll();
                    contentPanel.add(module.getView(), BorderLayout.CENTER);
                    contentPanel.revalidate();
                    contentPanel.repaint();
                } catch (Exception ex) {
                    System.err.println("[ERROR] Kh\u00F4ng th\u1EC3 m\u1EDF QuanLyLichChayModule:");
                    ex.printStackTrace();
                    showPlaceholder("L\u1ED7i", "Kh\u00F4ng th\u1EC3 t\u1EA3i module Qu\u1EA3n l\u00FD l\u1ECBch ch\u1EA1y: " + ex.getMessage());
                }
            }
            default -> showPlaceholder(label,
                    "Ch\u1EE9c n\u0103ng \"" + label
                            + "\" s\u1EBD \u0111\u01B0\u1EE3c tri\u1EC3n khai t\u1EA1i \u0111\u00E2y.");
        }
    }

    private void openThongTinCaNhan() {
        setActive(null);
        lblPageTitle.setText("Th\u00F4ng tin c\u00E1 nh\u00E2n");
        ThongTinCaNhanModule module = new ThongTinCaNhanModule(currentUser);
        module.setOnResult(null);
        contentPanel.removeAll();
        contentPanel.add(module.getView(), BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showPlaceholder(String title, String message) {
        lblPageTitle.setText(title);
        contentPanel.removeAll();

        JPanel placeholder = new JPanel(new GridBagLayout());
        placeholder.setOpaque(false);

        JLabel lbl = new JLabel("<html><div style='text-align:center;'>"
                + "<div style='font-size:16px; font-weight:bold; color:#191C1E;'>" + title + "</div>"
                + "<div style='font-size:12px; color:#707881; margin-top:8px;'>" + message + "</div>"
                + "</div></html>");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        placeholder.add(lbl);

        contentPanel.add(placeholder, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private String getInitials(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    // =====================================================================
    //  AppModule interface
    // =====================================================================

    @Override public String getTitle() { return "Menu Ch\u00EDnh"; }
    @Override public JPanel getView()  { return this; }
    @Override public void setOnResult(Consumer<Object> cb) { this.callback = cb; }
    @Override public void reset() {
        setActive(null);
        showPlaceholder("T\u1ED5ng quan",
                "Ch\u00E0o m\u1EEBng " + currentUser.getHoTen()
                        + "! H\u00E3y ch\u1ECDn ch\u1EE9c n\u0103ng t\u1EEB menu b\u00EAn tr\u00E1i.");
    }
}
