package com.modules;

import com.connectDB.ConnectDB;
import com.dao.DAO_NhanVien;
import com.entity.NhanVien;
import com.enums.TrangThaiNhanVien;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.SQLException;
import java.util.function.Consumer;

public class LoginModule extends JPanel implements AppModule {

    // --- State ---
    private Consumer<Object> callback;

    // --- UI components ---
    private JTextField     txtUsername;
    private JPasswordField txtPassword;
    private JButton        btnSubmit;
    private JLabel         lblError;
    private JCheckBox      chkShowPassword;

    // --- Colors ---
    private static final Color PRIMARY           = NotionTheme.ACCENT;
    private static final Color PRIMARY_HOVER     = NotionTheme.ACCENT_HOVER;
    private static final Color PRIMARY_CONTAINER = NotionTheme.ACCENT;
    private static final Color ON_PRIMARY        = AppColors.SURFACE;
    private static final Color SURFACE           = NotionTheme.PAGE;
    private static final Color SURFACE_HIGH      = NotionTheme.CARD_MUTED;
    private static final Color ON_SURFACE        = NotionTheme.TEXT;
    private static final Color ON_SURFACE_VAR    = NotionTheme.TEXT_MUTED;
    private static final Color OUTLINE           = NotionTheme.TEXT_FAINT;
    private static final Color ERROR             = AppColors.ERROR_DARK;

    public LoginModule() {
        setLayout(new BorderLayout());
        setBackground(SURFACE);
        buildUI();
    }

    private void buildUI() {
        JPanel mainPanel = new JPanel(new GridLayout(1, 2));
        mainPanel.setPreferredSize(new Dimension(1000, 620));
        mainPanel.setBackground(SURFACE);

        // -------- LEFT: Hero panel --------
        JPanel heroPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint bg = new GradientPaint(0, 0, NotionTheme.YELLOW,
                        getWidth(), getHeight(), NotionTheme.SKY);
                g2.setPaint(bg);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(AppColors.withAlpha(NotionTheme.PEACH, 180));
                g2.fillOval(-90, 260, 250, 250);
                g2.setColor(AppColors.withAlpha(NotionTheme.ACCENT_SOFT, 190));
                g2.fillOval(getWidth() - 180, 48, 250, 250);
                g2.setColor(AppColors.withAlpha(NotionTheme.MINT, 190));
                g2.fillRoundRect(44, getHeight() - 188, getWidth() - 88, 130, 30, 30);
                g2.setColor(NotionTheme.BORDER);
                g2.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
                g2.dispose();
            }
        };
        heroPanel.setLayout(new BoxLayout(heroPanel, BoxLayout.Y_AXIS));
        heroPanel.setBorder(new EmptyBorder(50, 42, 50, 42));

        JPanel logoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        logoRow.setOpaque(false);
        logoRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lblBrand = new JLabel("Quầy Vé Azure Rail");
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblBrand.setForeground(ON_SURFACE);
        logoRow.add(createHeroDot(NotionTheme.ACCENT));
        logoRow.add(lblBrand);

        heroPanel.add(logoRow);
        heroPanel.add(Box.createVerticalStrut(40));

        JLabel lblHeroTitle = new JLabel("<html><div style='width:280px'>"
                + "Hệ thống Quản lý Bán vé tại Quầy</div></html>");
        lblHeroTitle.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblHeroTitle.setForeground(ON_SURFACE);
        lblHeroTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        heroPanel.add(lblHeroTitle);
        heroPanel.add(Box.createVerticalStrut(20));

        JLabel lblHeroDesc = new JLabel("<html><div style='width:300px'>"
                + "Công cụ nghiệp vụ chuyên nghiệp cho việc xuất vé, "
                + "quản lý ca làm việc và điều phối hành trình "
                + "tại nhà ga.</div></html>");
        lblHeroDesc.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblHeroDesc.setForeground(ON_SURFACE_VAR);
        lblHeroDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        heroPanel.add(lblHeroDesc);
        heroPanel.add(Box.createVerticalStrut(22));

        JPanel chipRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        chipRow.setOpaque(false);
        chipRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        chipRow.add(createHeroChip("Bán vé", NotionTheme.SKY, new Color(0x00, 0x75, 0xDE)));
        chipRow.add(createHeroChip("Điều phối", NotionTheme.PEACH, new Color(0xDD, 0x5B, 0x00)));
        chipRow.add(createHeroChip("Báo cáo", NotionTheme.MINT, new Color(0x1A, 0xAE, 0x39)));
        heroPanel.add(chipRow);

        heroPanel.add(Box.createVerticalGlue());

        JPanel insightCard = createHeroInsightCard();
        insightCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        heroPanel.add(insightCard);
        heroPanel.add(Box.createVerticalStrut(10));

        // -------- RIGHT: Login form --------
        JPanel formWrapper = new JPanel(new GridBagLayout());
        formWrapper.setBackground(SURFACE);

        JPanel formPanel = NotionTheme.cardPanel(null);
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(new EmptyBorder(30, 32, 26, 32));
        formPanel.setPreferredSize(new Dimension(424, 520));

        JLabel lblTitle = new JLabel("Đăng nhập");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(ON_SURFACE);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(lblTitle);
        formPanel.add(Box.createVerticalStrut(6));

        JLabel lblSubtitle = new JLabel("Chào mừng quay trở lại. Vui lòng nhập thông tin để truy cập.");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitle.setForeground(ON_SURFACE_VAR);
        lblSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(lblSubtitle);
        formPanel.add(Box.createVerticalStrut(30));

        JLabel lblUser = new JLabel("Mã nhân viên / SĐT / Email");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblUser.setForeground(ON_SURFACE_VAR);
        lblUser.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(lblUser);
        formPanel.add(Box.createVerticalStrut(6));

        txtUsername = createStyledTextField("Ví dụ: NV20260504123045 hoặc 0900000000");
        txtUsername.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(txtUsername);
        formPanel.add(Box.createVerticalStrut(18));

        JLabel lblPass = new JLabel("Mật khẩu");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPass.setForeground(ON_SURFACE_VAR);
        lblPass.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(lblPass);
        formPanel.add(Box.createVerticalStrut(6));

        txtPassword = createStyledPasswordField("••••••••••");
        txtPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(txtPassword);
        formPanel.add(Box.createVerticalStrut(8));

        chkShowPassword = new JCheckBox("Hiện mật khẩu");
        chkShowPassword.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkShowPassword.setForeground(ON_SURFACE_VAR);
        chkShowPassword.setBackground(NotionTheme.CARD);
        chkShowPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        chkShowPassword.addActionListener(e -> {
            if (chkShowPassword.isSelected()) {
                txtPassword.setEchoChar((char) 0);
            } else {
                txtPassword.setEchoChar('•');
            }
        });
        formPanel.add(chkShowPassword);
        formPanel.add(Box.createVerticalStrut(8));

        lblError = new JLabel(" ");
        lblError.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblError.setForeground(ERROR);
        lblError.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(lblError);
        formPanel.add(Box.createVerticalStrut(12));

        btnSubmit = new JButton("Đăng nhập") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(PRIMARY_CONTAINER);
                } else if (getModel().isRollover()) {
                    g2.setColor(PRIMARY_HOVER);
                } else {
                    g2.setColor(PRIMARY);
                }
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnSubmit.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnSubmit.setForeground(ON_PRIMARY);
        btnSubmit.setMinimumSize(new Dimension(0, 60));
        btnSubmit.setPreferredSize(new Dimension(360, 60));
        btnSubmit.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        btnSubmit.setContentAreaFilled(false);
        btnSubmit.setBorderPainted(false);
        btnSubmit.setFocusPainted(false);
        btnSubmit.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSubmit.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnSubmit.addActionListener(e -> execute());
        formPanel.add(btnSubmit);
        formPanel.add(Box.createVerticalStrut(10));

        JButton btnExit = new JButton("Thoát") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(AppColors.BORDER);
                } else if (getModel().isRollover()) {
                    g2.setColor(AppColors.BORDER);
                } else {
                    g2.setColor(AppColors.BORDER);
                }
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnExit.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnExit.setForeground(AppColors.TEXT_SECONDARY);
        btnExit.setMinimumSize(new Dimension(0, 60));
        btnExit.setPreferredSize(new Dimension(360, 60));
        btnExit.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        btnExit.setContentAreaFilled(false);
        btnExit.setBorderPainted(false);
        btnExit.setFocusPainted(false);
        btnExit.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnExit.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnExit.addActionListener(e -> System.exit(0));
        formPanel.add(btnExit);

        formPanel.add(Box.createVerticalGlue());

        formWrapper.add(formPanel);

        mainPanel.add(heroPanel);
        mainPanel.add(formWrapper);

        add(mainPanel, BorderLayout.CENTER);

        txtPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) execute();
            }
        });
        txtUsername.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) txtPassword.requestFocusInWindow();
            }
        });
    }

    private JPanel createHeroDot(Color color) {
        JPanel dot = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        dot.setOpaque(false);
        dot.setPreferredSize(new Dimension(14, 14));
        return dot;
    }

    private JLabel createHeroChip(String text, Color bg, Color fg) {
        JLabel chip = new JLabel(text);
        chip.setFont(new Font("Segoe UI", Font.BOLD, 12));
        chip.setForeground(fg);
        chip.setOpaque(true);
        chip.setBackground(bg);
        chip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.withAlpha(fg, 45), 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        return chip;
    }

    private JPanel createHeroInsightCard() {
        JPanel card = new JPanel(new BorderLayout(14, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                g2.setColor(new Color(0xE5, 0xE1, 0xD8));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 24, 24);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 118));
        card.setPreferredSize(new Dimension(360, 118));
        card.setBorder(new EmptyBorder(18, 20, 18, 20));

        JPanel icon = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(NotionTheme.ACCENT_SOFT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        icon.setOpaque(false);
        icon.setPreferredSize(new Dimension(64, 64));
        JLabel iconText = new JLabel("^^");
        iconText.setFont(new Font("Segoe UI", Font.BOLD, 18));
        iconText.setForeground(PRIMARY);
        icon.add(iconText);
        // Handoff: dùng ASCII ^^ để badge ca trực không phụ thuộc glyph đặc biệt của font máy.
        // Cảnh báo: tránh ký tự biểu tượng như mũi tên/tick vì có máy hiển thị thành ô vuông tofu.

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setOpaque(false);
        JLabel title = new JLabel("Ca trực hôm nay");
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(ON_SURFACE);
        JLabel value = new JLabel("Sẵn sàng mở quầy bán vé");
        value.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        value.setForeground(ON_SURFACE_VAR);
        JLabel meta = new JLabel("Theo dõi chuyến, ghế và thanh toán trong một luồng.");
        meta.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        meta.setForeground(OUTLINE);
        text.add(title);
        text.add(Box.createVerticalStrut(5));
        text.add(value);
        text.add(Box.createVerticalStrut(8));
        text.add(meta);

        card.add(icon, BorderLayout.WEST);
        card.add(text, BorderLayout.CENTER);
        return card;
    }

    private JTextField createStyledTextField(String placeholder) {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !hasFocus()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setColor(OUTLINE);
                    g2.setFont(getFont());
                    Insets insets = getInsets();
                    g2.drawString(placeholder, insets.left, g.getFontMetrics().getMaxAscent() + insets.top);
                    g2.dispose();
                }
            }
        };
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(ON_SURFACE);
        field.setBackground(NotionTheme.CARD);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(NotionTheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        field.setPreferredSize(new Dimension(360, 48));
        return field;
    }

    private JPasswordField createStyledPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getPassword().length == 0 && !hasFocus()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setColor(OUTLINE);
                    g2.setFont(getFont());
                    Insets insets = getInsets();
                    g2.drawString(placeholder, insets.left, g.getFontMetrics().getMaxAscent() + insets.top);
                    g2.dispose();
                }
            }
        };
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(ON_SURFACE);
        field.setBackground(NotionTheme.CARD);
        field.setEchoChar('•');
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(NotionTheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        field.setPreferredSize(new Dimension(360, 48));
        return field;
    }

    private void execute() {
        lblError.setText(" ");
        String taiKhoan = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (taiKhoan.isEmpty()) {
            lblError.setText("Vui lòng nhập mã nhân viên, SĐT hoặc email.");
            txtUsername.requestFocusInWindow();
            return;
        }
        if (password.isEmpty()) {
            lblError.setText("Vui lòng nhập mật khẩu.");
            txtPassword.requestFocusInWindow();
            return;
        }

        btnSubmit.setEnabled(false);
        btnSubmit.setText("Đang xác thực...");

        SwingWorker<NhanVien, Void> worker = new SwingWorker<>() {
            @Override
            protected NhanVien doInBackground() {
                try {
                    if (ConnectDB.getCon() == null || ConnectDB.getCon().isClosed()) {
                        ConnectDB.getInstance().connect();
                    }
                } catch (SQLException e) {
                    return null;
                }
                DAO_NhanVien dao = new DAO_NhanVien();
                return dao.checkLogin(taiKhoan, password);
            }

            @Override
            protected void done() {
                btnSubmit.setEnabled(true);
                btnSubmit.setText("Đăng nhập");
                try {
                    NhanVien nv = get();
                    if (nv != null) {
                        if (nv.getTrangThai() == TrangThaiNhanVien.DA_NGHI) {
                            lblError.setText("Nhân viên đã nghỉ việc, không được phép đăng nhập.");
                            txtPassword.setText("");
                            txtPassword.requestFocusInWindow();
                            return;
                        }
                        if (callback != null) callback.accept(nv);
                    } else {
                        lblError.setText("Sai mã nhân viên hoặc mật khẩu. Vui lòng thử lại.");
                        txtPassword.setText("");
                        txtPassword.requestFocusInWindow();
                    }
                } catch (Exception ex) {
                    lblError.setText("Lỗi kết nối database. Vui lòng thử lại.");
                }
            }
        };
        worker.execute();
    }

    @Override public String getTitle() { return "Đăng nhập | Quầy Vé Azure Rail"; }
    @Override public JPanel getView()  { return this; }
    @Override public void setOnResult(Consumer<Object> cb) { this.callback = cb; }
    @Override public void reset() {
        txtUsername.setText("");
        txtPassword.setText("");
        lblError.setText(" ");
        chkShowPassword.setSelected(false);
        txtPassword.setEchoChar('•');
        btnSubmit.setEnabled(true);
        btnSubmit.setText("Đăng nhập");
    }
}

