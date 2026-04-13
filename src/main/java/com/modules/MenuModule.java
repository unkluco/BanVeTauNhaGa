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

        JLabel lblSub = new JLabel("Hệ thống Quản lý");
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
        JPanel btnDatVe = createPrimaryButton("Đặt vé mới");
        btnDatVe.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setActive(null);
                lblPageTitle.setText("Đặt vé mới");
                BanVeModule banVeModule = new BanVeModule(currentUser);
                banVeModule.setOnResult(null);
                contentPanel.removeAll();
                contentPanel.add(banVeModule.getView(), BorderLayout.CENTER);
                contentPanel.revalidate();
                contentPanel.repaint();
            }
        });
        nav.add(btnDatVe);
        nav.add(Box.createVerticalStrut(16));

        // Menu items — grouped by domain
        addMenuGroup(nav, "Tổng quan");
        addMenuItem(nav, "Tổng quan",                                              "TONG_QUAN");
        addMenuItem(nav, "Thống kê",                                           "THONG_KE");

        addMenuGroup(nav, "Nghiệp vụ");
        addMenuItem(nav, "Quản lý giá",                                  "QL_GIA");
        addMenuItem(nav, "Quản lý khuyến mãi",                      "QL_KHUYEN_MAI");
        addMenuItem(nav, "Quản lý vé",                                    "QL_VE_HOA_DON");
        addMenuItem(nav, "Quản lý hóa đơn",                    "QL_HOA_DON");

        addMenuGroup(nav, "Dữ liệu tàu");
        addMenuItem(nav, "Quản lý đoàn tàu",                   "QL_DOAN_TAU");
        addMenuItem(nav, "Quản lý Toa và Đầu máy",        "QL_TOA");
        addMenuItem(nav, "Quản lý tuyến",                                "QL_TUYEN");
        addMenuItem(nav, "Quản lý lịch chạy",                       "QL_LICH_CHAY");

        addMenuGroup(nav, "Hệ thống");
        addMenuItem(nav, "Quản lý nhân viên",                       "QL_NHAN_VIEN");

        applyRoleRestrictions();

        nav.add(Box.createVerticalGlue());

        // Sidebar footer
        JPanel sidebarFooter = new JPanel();
        sidebarFooter.setLayout(new BoxLayout(sidebarFooter, BoxLayout.Y_AXIS));
        sidebarFooter.setBackground(SIDEBAR_BG);
        sidebarFooter.setBorder(new EmptyBorder(12, 12, 16, 12));

        NavItem btnThongTinCaNhan = createNavItem("Thông tin cá nhân");
        btnThongTinCaNhan.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openThongTinCaNhan();
            }
        });
        sidebarFooter.add(btnThongTinCaNhan);
        sidebarFooter.add(Box.createVerticalStrut(2));

        NavItem btnCaiDat = createNavItem("Cài đặt");
        sidebarFooter.add(btnCaiDat);
        sidebarFooter.add(Box.createVerticalStrut(2));

        NavItem btnDangXuat = createNavItem("Đăng xuất");
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

        lblPageTitle = new JLabel("Quản lý Bán vé");
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

        // Load Tổng quan thật ngay khi đăng nhập — không dùng placeholder
        handleMenuAction("TONG_QUAN", "Tổng quan");
        for (NavItem item : menuItems) {
            if ("TONG_QUAN".equals(item.getName())) {
                setActive(item);
                break;
            }
        }

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
        private boolean locked = false;
        JLabel lockLabel = null;

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

        void setLocked() {
            this.locked = true;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (locked) {
                g2.setColor(new Color(0xF2, 0xF1, 0xF0));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            } else if (bgColor != null) {
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                if (rightBorder) {
                    g2.setColor(PRIMARY);
                    g2.fillRoundRect(getWidth() - 3, 4, 3, getHeight() - 8, 3, 3);
                }
            }
            g2.dispose();
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

    private void addMenuGroup(JPanel nav, String label) {
        nav.add(Box.createVerticalStrut(10));

        JPanel header = new JPanel(new BorderLayout(6, 0));
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
        header.setBorder(new EmptyBorder(0, 16, 0, 16));

        JLabel lbl = new JLabel(label.toUpperCase());
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lbl.setForeground(TEXT_MUTED);
        header.add(lbl, BorderLayout.WEST);

        JPanel line = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(SIDEBAR_BORDER);
                g2.fillRect(0, getHeight() / 2, getWidth(), 1);
                g2.dispose();
            }
        };
        line.setOpaque(false);
        header.add(line, BorderLayout.CENTER);

        nav.add(header);
        nav.add(Box.createVerticalStrut(4));
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

        // Lock icon (hidden until disabled) — dùng icon từ resources
        JLabel lockIcon = new JLabel();
        try {
            java.net.URL url = getClass().getResource("/icons/bieuTuongTuChoi.png");
            if (url != null) {
                Image img = new ImageIcon(url).getImage().getScaledInstance(12, 12, Image.SCALE_SMOOTH);
                lockIcon.setIcon(new ImageIcon(img));
            }
        } catch (Exception ignored) {}
        lockIcon.setVisible(false);
        lockIcon.setBorder(new EmptyBorder(0, 2, 0, 6));
        panel.add(lockIcon, BorderLayout.EAST);
        panel.lockLabel = lockIcon;

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
                item.setLocked();
                for (Component c : item.getComponents()) {
                    c.setEnabled(false);
                    if (c instanceof JLabel lbl && lbl != item.lockLabel) {
                        lbl.setForeground(TEXT_DISABLED);
                        lbl.setFont(lbl.getFont().deriveFont(Font.ITALIC));
                    }
                }
                if (item.lockLabel != null) {
                    item.lockLabel.setForeground(TEXT_DISABLED);
                    item.lockLabel.setVisible(true);
                }
                item.setToolTipText("Bạn không có quyền truy cập chức năng này");
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
                tqModule.setOnResult(result -> {
                    if (result instanceof String[] nav && nav.length == 3) {
                        navigateWithSearch(nav[0], nav[1], nav[2]);
                    }
                });
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
                    System.err.println("[ERROR] Không thể mở ThongKeModule:");
                    ex.printStackTrace();
                    showPlaceholder("Lỗi", "Không thể tải module Thống kê: " + ex.getMessage());
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
                    System.err.println("[ERROR] Không thể mở QuanLyKhuyenMaiModule:");
                    ex.printStackTrace();
                    showPlaceholder("Lỗi", "Không thể tải module Quản lý khuyến mãi: " + ex.getMessage());
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
                    System.err.println("[ERROR] Không thể mở QuanLyHoaDonModule:");
                    ex.printStackTrace();
                    showPlaceholder("Lỗi", "Không thể tải module Quản lý hóa đơn: " + ex.getMessage());
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
                    System.err.println("[ERROR] Không thể mở QuanLyTuyenModule:");
                    ex.printStackTrace();
                    showPlaceholder("Lỗi", "Không thể tải module Quản lý tuyến: " + ex.getMessage());
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
                    System.err.println("[ERROR] Không thể mở QuanLyDoanTauModule:");
                    ex.printStackTrace();
                    showPlaceholder("Lỗi", "Không thể tải module Quản lý đoàn tàu: " + ex.getMessage());
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
                    System.err.println("[ERROR] Không thể mở QuanLyToaModule:");
                    ex.printStackTrace();
                    showPlaceholder("Lỗi", "Không thể tải module Quản lý toa: " + ex.getMessage());
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
                    System.err.println("[ERROR] Không thể mở QuanLyLichChayModule:");
                    ex.printStackTrace();
                    showPlaceholder("Lỗi", "Không thể tải module Quản lý lịch chạy: " + ex.getMessage());
                }
            }
            default -> showPlaceholder(label,
                    "Chức năng \"" + label
                            + "\" sẽ được triển khai tại đây.");
        }
    }

    private void navigateWithSearch(String actionKey, String label, String searchText) {
        lblPageTitle.setText(label);
        for (NavItem item : menuItems) {
            if (actionKey.equals(item.getName()) && item.isEnabled()) {
                setActive(item);
                break;
            }
        }
        try {
            switch (actionKey) {
                case "QL_LICH_CHAY" -> {
                    QuanLyLichChayModule module = new QuanLyLichChayModule();
                    module.setOnResult(null);
                    contentPanel.removeAll();
                    contentPanel.add(module.getView(), BorderLayout.CENTER);
                    contentPanel.revalidate();
                    contentPanel.repaint();
                    if (!searchText.isEmpty()) module.applySearch(searchText);
                }
                case "QL_HOA_DON" -> {
                    QuanLyHoaDonModule module = new QuanLyHoaDonModule();
                    module.setOnResult(null);
                    contentPanel.removeAll();
                    contentPanel.add(module.getView(), BorderLayout.CENTER);
                    contentPanel.revalidate();
                    contentPanel.repaint();
                    if (!searchText.isEmpty()) module.applySearch(searchText);
                }
                case "QL_VE_HOA_DON" -> {
                    QuanLyVeModule module = new QuanLyVeModule();
                    module.setOnResult(null);
                    contentPanel.removeAll();
                    contentPanel.add(module.getView(), BorderLayout.CENTER);
                    contentPanel.revalidate();
                    contentPanel.repaint();
                    if (!searchText.isEmpty()) module.applySearchVe(searchText);
                }
                default -> handleMenuAction(actionKey, label);
            }
        } catch (Exception ex) {
            showPlaceholder("Lỗi", "Không thể mở module: " + ex.getMessage());
        }
    }

    private void openThongTinCaNhan() {
        setActive(null);
        lblPageTitle.setText("Thông tin cá nhân");
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

    @Override public String getTitle() { return "Menu Chính"; }
    @Override public JPanel getView()  { return this; }
    @Override public void setOnResult(Consumer<Object> cb) { this.callback = cb; }
    @Override public void reset() {
        setActive(null);
        showPlaceholder("Tổng quan",
                "Chào mừng " + currentUser.getHoTen()
                        + "! Hãy chọn chức năng từ menu bên trái.");
    }
}
