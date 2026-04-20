package com.modules;

import com.dao.DAO_NhanVien;
import com.entity.NhanVien;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

public class ThongTinCaNhanModule extends JPanel implements AppModule {

    private Consumer<Object> callback;
    private NhanVien currentUser;
    private final DAO_NhanVien daoNV = new DAO_NhanVien();

    private static final Color PRIMARY       = new Color(0x00, 0x5D, 0x90);
    private static final Color PRIMARY_LIGHT = new Color(0xCF, 0xE6, 0xF2);
    private static final Color SURFACE       = new Color(0xF7, 0xF9, 0xFB);
    private static final Color CARD_BG       = Color.WHITE;
    private static final Color TEXT_DARK     = new Color(0x19, 0x1C, 0x1E);
    private static final Color TEXT_MUTED    = new Color(0x64, 0x74, 0x8B);
    private static final Color BORDER_COLOR  = new Color(0xE2, 0xE8, 0xF0);
    private static final Color FIELD_BG      = new Color(0xF1, 0xF5, 0xF9);
    private static final Color SUCCESS       = new Color(0x16, 0xA3, 0x4A);
    private static final Color ERROR_COLOR   = new Color(0xDC, 0x26, 0x26);

    private JTextField txtPhone, txtEmail, txtAddress;
    private JPanel passwordPanel;
    private JPasswordField txtOldPass, txtNewPass, txtConfirmPass;
    private Timer fadeTimer;
    private float passwordPanelAlpha = 0f;
    private boolean passwordPanelVisible = false;

    private static final double LEFT_RATIO = 0.32;

    public ThongTinCaNhanModule(NhanVien user) {
        this.currentUser = user;
        setLayout(new BorderLayout());
        setBackground(SURFACE);
        buildUI();
    }

    private void buildUI() {
        NhanVien fresh = daoNV.findById(currentUser.getMaNV());
        if (fresh != null) currentUser = fresh;

        // Scrollable content
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(SURFACE);
        content.setBorder(new EmptyBorder(32, 40, 32, 40));

        // Breadcrumb
        JPanel breadcrumb = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        breadcrumb.setOpaque(false);
        breadcrumb.setBorder(new EmptyBorder(0, 0, 20, 0));
        addLabel(breadcrumb, "Nhân sự", Font.PLAIN, 12, TEXT_MUTED);
        addLabel(breadcrumb, " › ", Font.PLAIN, 12, TEXT_MUTED);
        addLabel(breadcrumb, "Thông tin cá nhân", Font.BOLD, 12, PRIMARY);
        content.add(breadcrumb, BorderLayout.NORTH);

        // Two-column layout: left gets its preferred height (top-aligned), right fills
        JPanel columns = new JPanel() {
            @Override
            public void doLayout() {
                int w = getWidth();
                int h = getHeight();
                int gap = 24;
                int leftW = (int)(w * LEFT_RATIO) - gap / 2;
                int rightW = w - leftW - gap;
                Component[] cc = getComponents();
                if (cc.length >= 2) {
                    int leftPrefH = cc[0].getPreferredSize().height;
                    cc[0].setBounds(0, 0, leftW, Math.min(leftPrefH, h));
                    cc[1].setBounds(leftW + gap, 0, rightW, h);
                }
            }
            @Override
            public Dimension getPreferredSize() {
                Component[] cc = getComponents();
                if (cc.length < 2) return super.getPreferredSize();
                int leftH = cc[0].getPreferredSize().height;
                int rightH = cc[1].getPreferredSize().height;
                return new Dimension(800, Math.max(leftH, rightH));
            }
        };
        columns.setOpaque(false);

        // LEFT: profile card + separate password card
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);
        leftPanel.add(buildProfileCard());
        leftPanel.add(buildPasswordCard());

        // RIGHT: identity + contact
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);
        rightPanel.add(buildIdentitySection());
        rightPanel.add(Box.createVerticalStrut(20));
        rightPanel.add(buildContactSection());

        columns.add(leftPanel);
        columns.add(rightPanel);

        JScrollPane scroll = new JScrollPane(columns);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(SURFACE);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        content.add(scroll, BorderLayout.CENTER);

        add(content, BorderLayout.CENTER);
    }

    // =====================================================================
    //  PROFILE CARD
    // =====================================================================
    private JPanel buildProfileCard() {
        JPanel card = createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(32, 24, 24, 24)
        ));

        // Avatar — fixed-size wrapper to prevent BoxLayout stretching
        JPanel avatarWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        avatarWrapper.setOpaque(false);
        avatarWrapper.setAlignmentX(CENTER_ALIGNMENT);
        avatarWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 124));
        JPanel avatar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(PRIMARY.getRed(), PRIMARY.getGreen(), PRIMARY.getBlue(), 25));
                g2.fillOval(0, 0, 120, 120);
                g2.setColor(PRIMARY_LIGHT);
                g2.fillOval(4, 4, 112, 112);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(3));
                g2.drawOval(4, 4, 112, 112);
                g2.setColor(PRIMARY);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 40));
                String ini = getInitials(currentUser.getHoTen());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(ini, (120 - fm.stringWidth(ini)) / 2, (120 + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(120, 120));
        avatar.setMaximumSize(new Dimension(120, 120));
        avatarWrapper.add(avatar);
        card.add(avatarWrapper);
        card.add(Box.createVerticalStrut(16));

        // Name + ID
        JLabel lblName = new JLabel(currentUser.getHoTen(), SwingConstants.CENTER);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblName.setForeground(TEXT_DARK);
        lblName.setAlignmentX(CENTER_ALIGNMENT);
        lblName.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        card.add(lblName);
        card.add(Box.createVerticalStrut(4));

        JLabel lblId = new JLabel("Mã NV: " + currentUser.getMaNV(), SwingConstants.CENTER);
        lblId.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblId.setForeground(PRIMARY);
        lblId.setAlignmentX(CENTER_ALIGNMENT);
        lblId.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
        card.add(lblId);
        card.add(Box.createVerticalStrut(20));

        // Info rows
        String roleName = currentUser.getVaiTro() != null ? currentUser.getVaiTro().toString() : "";
        card.add(buildInfoRow("Bộ phận", formatRole(roleName)));
        card.add(Box.createVerticalStrut(8));
        String gaName = resolveGaName(currentUser.getGaLamViec());
        card.add(buildInfoRow("Khu vực", gaName != null ? gaName : "Chưa xác định"));
        card.add(Box.createVerticalStrut(24));

        // Change password button
        JPanel btnChangePass = createRoundedBgPanel(FIELD_BG, 16);
        btnChangePass.setLayout(new BorderLayout());
        btnChangePass.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnChangePass.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        btnChangePass.setPreferredSize(new Dimension(0, 48));
        btnChangePass.setBorder(new EmptyBorder(0, 16, 0, 16));
        btnChangePass.setAlignmentX(CENTER_ALIGNMENT);

        JLabel lblChangePass = new JLabel("Đổi mật khẩu");
        lblChangePass.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblChangePass.setForeground(TEXT_MUTED);
        lblChangePass.setIcon(createLockIcon());
        lblChangePass.setIconTextGap(8);
        lblChangePass.setHorizontalAlignment(SwingConstants.CENTER);
        btnChangePass.add(lblChangePass, BorderLayout.CENTER);

        btnChangePass.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { togglePasswordPanel(); }
            @Override public void mouseEntered(MouseEvent e) { lblChangePass.setForeground(PRIMARY); }
            @Override public void mouseExited(MouseEvent e) { lblChangePass.setForeground(TEXT_MUTED); }
        });
        card.add(btnChangePass);

        return card;
    }

    // =====================================================================
    //  PASSWORD CARD (separate white card below profile)
    // =====================================================================
    private JPanel buildPasswordCard() {
        // Outer wrapper that controls visibility + fade
        passwordPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                if (passwordPanelAlpha <= 0f) return;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, passwordPanelAlpha));
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 16, getWidth(), getHeight() - 16, 16, 16);
                // Border
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 16, getWidth() - 1, getHeight() - 17, 16, 16);
                g2.dispose();
            }
            @Override protected void paintChildren(Graphics g) {
                if (passwordPanelAlpha <= 0f) return;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, passwordPanelAlpha));
                super.paintChildren(g2);
                g2.dispose();
            }
        };
        passwordPanel.setLayout(new BoxLayout(passwordPanel, BoxLayout.Y_AXIS));
        passwordPanel.setOpaque(false);
        passwordPanel.setBorder(new EmptyBorder(16, 24, 24, 24)); // top=16 for gap from profile card
        passwordPanel.setVisible(false);

        // Title row with icon
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleRow.setOpaque(false);
        titleRow.setAlignmentX(LEFT_ALIGNMENT);
        titleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        JPanel bar = new JPanel(); bar.setBackground(PRIMARY); bar.setPreferredSize(new Dimension(4, 18));
        titleRow.add(bar);
        JLabel lblPwTitle = new JLabel("Đổi mật khẩu");
        lblPwTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblPwTitle.setForeground(TEXT_DARK);
        titleRow.add(lblPwTitle);
        passwordPanel.add(Box.createVerticalStrut(12)); // offset for the painted card bg starting at y=16
        passwordPanel.add(titleRow);
        passwordPanel.add(Box.createVerticalStrut(16));

        passwordPanel.add(createFieldLabel("Mật khẩu cũ"));
        txtOldPass = new JPasswordField(); stylePasswordField(txtOldPass);
        passwordPanel.add(txtOldPass);
        passwordPanel.add(Box.createVerticalStrut(10));

        passwordPanel.add(createFieldLabel("Mật khẩu mới"));
        txtNewPass = new JPasswordField(); stylePasswordField(txtNewPass);
        passwordPanel.add(txtNewPass);
        passwordPanel.add(Box.createVerticalStrut(10));

        passwordPanel.add(createFieldLabel("Xác nhận mật khẩu mới"));
        txtConfirmPass = new JPasswordField(); stylePasswordField(txtConfirmPass);
        passwordPanel.add(txtConfirmPass);
        passwordPanel.add(Box.createVerticalStrut(20));

        // Separator
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(BORDER_COLOR);
        sep.setAlignmentX(LEFT_ALIGNMENT);
        passwordPanel.add(sep);
        passwordPanel.add(Box.createVerticalStrut(12));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(LEFT_ALIGNMENT);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JButton btnPwCancel = createTextButton("Hủy", TEXT_MUTED);
        btnPwCancel.addActionListener(e -> togglePasswordPanel());
        JButton btnPwConfirm = createFilledButton("Xác nhận");
        btnPwConfirm.addActionListener(e -> handleChangePassword());
        btnRow.add(btnPwCancel);
        btnRow.add(btnPwConfirm);
        passwordPanel.add(btnRow);

        return passwordPanel;
    }

    // =====================================================================
    //  IDENTITY SECTION
    // =====================================================================
    private JPanel buildIdentitySection() {
        JPanel card = createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(28, 28, 28, 28)
        ));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setAlignmentX(LEFT_ALIGNMENT);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleRow.setOpaque(false);
        JPanel bar = new JPanel(); bar.setBackground(PRIMARY); bar.setPreferredSize(new Dimension(4, 20));
        titleRow.add(bar);
        addLabel(titleRow, "Thông tin định danh", Font.BOLD, 16, TEXT_DARK);
        header.add(titleRow, BorderLayout.WEST);

        JLabel lblNote = new JLabel("* Thông tin này do nhân sự quản lý");
        lblNote.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblNote.setForeground(TEXT_MUTED);
        header.add(lblNote, BorderLayout.EAST);
        card.add(header);
        card.add(Box.createVerticalStrut(20));

        JPanel grid = new JPanel(new GridLayout(2, 2, 24, 16));
        grid.setOpaque(false);
        grid.setAlignmentX(LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        String cccd = currentUser.getCccd() != null ? currentUser.getCccd() : "—";
        String ngaySinh = currentUser.getNgaySinh() != null
                ? currentUser.getNgaySinh().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "—";
        String gioiTinh = currentUser.getGioiTinh() != null
                ? ("NAM".equalsIgnoreCase(currentUser.getGioiTinh()) ? "Nam" : "Nữ") : "—";
        String quocTich = currentUser.getQuocTich() != null ? currentUser.getQuocTich() : "—";

        grid.add(buildReadOnlyField("Số CCCD / Hộ chiếu", cccd));
        grid.add(buildReadOnlyField("Ngày sinh", ngaySinh));
        grid.add(buildReadOnlyField("Giới tính", gioiTinh));
        grid.add(buildReadOnlyField("Quốc tịch", quocTich));
        card.add(grid);

        return card;
    }

    // =====================================================================
    //  CONTACT SECTION
    // =====================================================================
    private JPanel buildContactSection() {
        JPanel card = createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(28, 28, 28, 28)
        ));

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleRow.setOpaque(false);
        titleRow.setAlignmentX(LEFT_ALIGNMENT);
        titleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        JPanel bar = new JPanel(); bar.setBackground(PRIMARY); bar.setPreferredSize(new Dimension(4, 20));
        titleRow.add(bar);
        addLabel(titleRow, "Thông tin liên lạc", Font.BOLD, 16, TEXT_DARK);
        card.add(titleRow);
        card.add(Box.createVerticalStrut(20));

        // Phone + Email row
        JPanel row1 = new JPanel(new GridLayout(1, 2, 24, 0));
        row1.setOpaque(false);
        row1.setAlignmentX(LEFT_ALIGNMENT);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JPanel phoneGroup = new JPanel();
        phoneGroup.setLayout(new BoxLayout(phoneGroup, BoxLayout.Y_AXIS));
        phoneGroup.setOpaque(false);
        phoneGroup.add(createFieldLabel("Số điện thoại"));
        txtPhone = new JTextField(currentUser.getSoDienThoai() != null ? currentUser.getSoDienThoai() : "");
        styleTextField(txtPhone);
        phoneGroup.add(txtPhone);

        JPanel emailGroup = new JPanel();
        emailGroup.setLayout(new BoxLayout(emailGroup, BoxLayout.Y_AXIS));
        emailGroup.setOpaque(false);
        emailGroup.add(createFieldLabel("Email cá nhân"));
        txtEmail = new JTextField(currentUser.getEmail() != null ? currentUser.getEmail() : "");
        styleTextField(txtEmail);
        emailGroup.add(txtEmail);

        row1.add(phoneGroup);
        row1.add(emailGroup);
        card.add(row1);
        card.add(Box.createVerticalStrut(16));

        // Address
        JPanel addrGroup = new JPanel();
        addrGroup.setLayout(new BoxLayout(addrGroup, BoxLayout.Y_AXIS));
        addrGroup.setOpaque(false);
        addrGroup.setAlignmentX(LEFT_ALIGNMENT);
        addrGroup.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        addrGroup.add(createFieldLabel("Địa chỉ thường trú"));
        txtAddress = new JTextField(currentUser.getDiaChiThuongTru() != null ? currentUser.getDiaChiThuongTru() : "");
        styleTextField(txtAddress);
        addrGroup.add(txtAddress);
        card.add(addrGroup);
        card.add(Box.createVerticalStrut(24));

        // Separator + buttons
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(BORDER_COLOR);
        card.add(sep);
        card.add(Box.createVerticalStrut(16));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(LEFT_ALIGNMENT);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JButton btnCancel = createTextButton("Hủy thay đổi", TEXT_MUTED);
        btnCancel.addActionListener(e -> resetContactFields());
        JButton btnSave = createFilledButton("Lưu thông tin");
        btnSave.addActionListener(e -> handleSaveContact());
        btnRow.add(btnCancel);
        btnRow.add(btnSave);
        card.add(btnRow);

        return card;
    }

    // =====================================================================
    //  ACTIONS
    // =====================================================================
    private void togglePasswordPanel() {
        if (fadeTimer != null && fadeTimer.isRunning()) fadeTimer.stop();

        if (!passwordPanelVisible) {
            passwordPanelVisible = true;
            passwordPanelAlpha = 0f;
            passwordPanel.setVisible(true);
            txtOldPass.setText(""); txtNewPass.setText(""); txtConfirmPass.setText("");
            revalidate();

            fadeTimer = new Timer(16, e -> {
                passwordPanelAlpha += 0.08f;
                if (passwordPanelAlpha >= 1f) { passwordPanelAlpha = 1f; ((Timer)e.getSource()).stop(); }
                passwordPanel.repaint();
            });
            fadeTimer.start();
        } else {
            fadeTimer = new Timer(16, e -> {
                passwordPanelAlpha -= 0.08f;
                if (passwordPanelAlpha <= 0f) {
                    passwordPanelAlpha = 0f;
                    passwordPanelVisible = false;
                    passwordPanel.setVisible(false);
                    revalidate();
                    ((Timer)e.getSource()).stop();
                }
                passwordPanel.repaint();
            });
            fadeTimer.start();
        }
    }

    private void handleChangePassword() {
        String oldPass = new String(txtOldPass.getPassword()).trim();
        String newPass = new String(txtNewPass.getPassword()).trim();
        String confirm = new String(txtConfirmPass.getPassword()).trim();

        if (oldPass.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
            showMessage("Vui lòng nhập đầy đủ thông tin.", ERROR_COLOR); return;
        }
        // Xác thực trực tiếp với DB để tránh sai lệch dữ liệu do object currentUser cũ.
        if (!daoNV.verifyPassword(currentUser.getMaNV(), oldPass)) {
            showMessage("Mật khẩu cũ không chính xác.", ERROR_COLOR); return;
        }
        if (!newPass.equals(confirm)) {
            showMessage("Mật khẩu mới và xác nhận không khớp.", ERROR_COLOR); return;
        }
        if (newPass.length() < 4) {
            showMessage("Mật khẩu mới phải có ít nhất 4 ký tự.", ERROR_COLOR); return;
        }

        if (daoNV.updatePassword(currentUser.getMaNV(), newPass)) {
            currentUser.setPassword(newPass);
            showMessage("Đổi mật khẩu thành công!", SUCCESS);
            togglePasswordPanel();
        } else {
            showMessage("Lỗi khi đổi mật khẩu.", ERROR_COLOR);
        }
    }

    private void resetContactFields() {
        txtPhone.setText(currentUser.getSoDienThoai() != null ? currentUser.getSoDienThoai() : "");
        txtEmail.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");
        txtAddress.setText(currentUser.getDiaChiThuongTru() != null ? currentUser.getDiaChiThuongTru() : "");
    }

    private void handleSaveContact() {
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();
        String address = txtAddress.getText().trim();

        if (phone.isEmpty()) { showMessage("SĐT không được trống.", ERROR_COLOR); return; }
        if (!phone.matches("\\d{10,11}")) { showMessage("SĐT phải có 10–11 chữ số.", ERROR_COLOR); return; }
        if (!email.isEmpty() && !email.matches("[\\w.+\\-]+@[\\w\\-]+(\\.[\\w\\-]+)*\\.[a-zA-Z]{2,}")) {
            showMessage("Email không hợp lệ.", ERROR_COLOR); return;
        }
        if (daoNV.existsBySoDienThoai(phone, currentUser.getMaNV())) {
            showMessage("Số điện thoại đã tồn tại trong hệ thống.", ERROR_COLOR); return;
        }
        if (!email.isEmpty() && daoNV.existsByEmail(email, currentUser.getMaNV())) {
            showMessage("Email đã tồn tại trong hệ thống.", ERROR_COLOR); return;
        }

        if (daoNV.updateContactInfo(currentUser.getMaNV(), phone, email, address)) {
            currentUser.setSoDienThoai(phone);
            currentUser.setEmail(email);
            currentUser.setDiaChiThuongTru(address);
            showMessage("Cập nhật thành công!", SUCCESS);
        } else {
            showMessage("Lỗi khi cập nhật.", ERROR_COLOR);
        }
    }

    // =====================================================================
    //  UI HELPERS
    // =====================================================================
    private JPanel createCard() {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setAlignmentX(LEFT_ALIGNMENT);
        return card;
    }

    private JPanel createRoundedBgPanel(Color bg, int radius) {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        return p;
    }

    private JPanel buildInfoRow(String label, String value) {
        JPanel row = createRoundedBgPanel(FIELD_BG, 12);
        row.setLayout(new BorderLayout());
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        row.setAlignmentX(CENTER_ALIGNMENT);
        row.setBorder(new EmptyBorder(8, 16, 8, 16));
        JLabel l = new JLabel(label); l.setFont(new Font("Segoe UI", Font.BOLD, 10)); l.setForeground(TEXT_MUTED);
        JLabel v = new JLabel(value); v.setFont(new Font("Segoe UI", Font.BOLD, 12)); v.setForeground(TEXT_DARK);
        row.add(l, BorderLayout.WEST);
        row.add(v, BorderLayout.EAST);
        return row;
    }

    private JPanel buildReadOnlyField(String label, String value) {
        JPanel w = new JPanel();
        w.setLayout(new BoxLayout(w, BoxLayout.Y_AXIS));
        w.setOpaque(false);
        JLabel l = new JLabel(label); l.setFont(new Font("Segoe UI", Font.BOLD, 10)); l.setForeground(TEXT_MUTED);
        l.setAlignmentX(LEFT_ALIGNMENT);
        w.add(l); w.add(Box.createVerticalStrut(6));
        JPanel bg = createRoundedBgPanel(FIELD_BG, 12);
        bg.setLayout(new BorderLayout());
        bg.setBorder(new EmptyBorder(10, 14, 10, 14));
        bg.setAlignmentX(LEFT_ALIGNMENT);
        JLabel v = new JLabel(value); v.setFont(new Font("Segoe UI", Font.BOLD, 13)); v.setForeground(TEXT_DARK);
        bg.add(v, BorderLayout.CENTER);
        w.add(bg);
        return w;
    }

    private JLabel createFieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setForeground(TEXT_MUTED);
        l.setAlignmentX(LEFT_ALIGNMENT);
        l.setBorder(new EmptyBorder(0, 0, 6, 0));
        return l;
    }

    private JLabel addLabel(JPanel parent, String text, int style, int size, Color fg) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", style, size));
        l.setForeground(fg);
        parent.add(l);
        return l;
    }

    private void styleTextField(JTextField tf) {
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setForeground(TEXT_DARK);
        tf.setBackground(Color.WHITE);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1), new EmptyBorder(10, 14, 10, 14)));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        tf.setAlignmentX(LEFT_ALIGNMENT);
    }

    private void stylePasswordField(JPasswordField pf) {
        pf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pf.setForeground(TEXT_DARK);
        pf.setBackground(Color.WHITE);
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1), new EmptyBorder(10, 14, 10, 14)));
        pf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        pf.setAlignmentX(LEFT_ALIGNMENT);
    }

    private JButton createTextButton(String text, Color fg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12)); b.setForeground(fg);
        b.setBorderPainted(false); b.setContentAreaFilled(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setForeground(PRIMARY); }
            @Override public void mouseExited(MouseEvent e) { b.setForeground(fg); }
        });
        return b;
    }

    private JButton createFilledButton(String text) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? PRIMARY.darker() : PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(new Font("Segoe UI", Font.BOLD, 12)); b.setForeground(Color.WHITE);
        b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false); b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(8, 20, 8, 20));
        return b;
    }

    private Icon createLockIcon() {
        return new Icon() {
            @Override public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c.getForeground());
                g2.setStroke(new BasicStroke(1.5f));
                g2.fillRoundRect(x + 2, y + 7, 12, 9, 3, 3);
                g2.drawArc(x + 4, y + 1, 8, 10, 0, 180);
                g2.dispose();
            }
            @Override public int getIconWidth() { return 16; }
            @Override public int getIconHeight() { return 16; }
        };
    }

    private void showMessage(String msg, Color color) {
        JLabel l = new JLabel(msg); l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(Color.WHITE); l.setHorizontalAlignment(SwingConstants.CENTER);
        l.setBorder(new EmptyBorder(10, 20, 10, 20));
        JPanel toast = createRoundedBgPanel(color, 12);
        toast.setLayout(new BorderLayout());
        toast.add(l, BorderLayout.CENTER);

        Window w = SwingUtilities.getWindowAncestor(this);
        if (w instanceof JFrame frame) {
            JLayeredPane lp = frame.getLayeredPane();
            toast.setBounds((lp.getWidth() - 320) / 2, lp.getHeight() - 80, 320, 44);
            lp.add(toast, JLayeredPane.POPUP_LAYER);
            lp.revalidate(); lp.repaint();
            Timer t = new Timer(2000, e -> { lp.remove(toast); lp.revalidate(); lp.repaint(); });
            t.setRepeats(false); t.start();
        }
    }

    private String getInitials(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] p = name.trim().split("\\s+");
        return p.length == 1 ? p[0].substring(0, 1).toUpperCase()
                : (p[0].substring(0, 1) + p[p.length - 1].substring(0, 1)).toUpperCase();
    }

    private String formatRole(String role) {
        if (role == null) return "—";
        return switch (role) {
            case "ADMIN" -> "Quản trị viên";
            case "DIEU_PHOI" -> "Điều phối";
            case "BAN_VE" -> "Nhân viên quầy vé";
            default -> role;
        };
    }

    private String resolveGaName(String maGa) {
        if (maGa == null || maGa.isEmpty()) return null;
        for (String[] ga : daoNV.getAllGa()) if (maGa.equals(ga[0])) return ga[1];
        return maGa;
    }

    @Override public String getTitle() { return "Thông tin cá nhân"; }
    @Override public JPanel getView() { return this; }
    @Override public void setOnResult(Consumer<Object> cb) { this.callback = cb; }
    @Override public void reset() { }
}
