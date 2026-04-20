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

    // --- Colors theo design login.html ---
    private static final Color PRIMARY           = new Color(0x00, 0x5D, 0x90);
    private static final Color PRIMARY_CONTAINER = new Color(0x00, 0x77, 0xB6);
    private static final Color ON_PRIMARY        = Color.WHITE;
    private static final Color SURFACE           = new Color(0xF7, 0xF9, 0xFB);
    private static final Color SURFACE_HIGH      = new Color(0xE0, 0xE3, 0xE5);
    private static final Color ON_SURFACE        = new Color(0x19, 0x1C, 0x1E);
    private static final Color ON_SURFACE_VAR    = new Color(0x40, 0x48, 0x50);
    private static final Color OUTLINE           = new Color(0x70, 0x78, 0x81);
    private static final Color ERROR             = new Color(0xBA, 0x1A, 0x1A);

    public LoginModule() {
        setLayout(new BorderLayout());
        setBackground(SURFACE);
        buildUI();
    }

    private void buildUI() {
        JPanel mainPanel = new JPanel(new GridLayout(1, 2));
        mainPanel.setPreferredSize(new Dimension(1000, 620));

        // -------- LEFT: Hero panel --------
        JPanel heroPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(0x00, 0x4B, 0x74),
                        getWidth(), getHeight(), PRIMARY_CONTAINER);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(255, 255, 255, 25));
                g2.fillOval(getWidth() - 200, getHeight() - 200, 400, 400);
                g2.dispose();
            }
        };
        heroPanel.setLayout(new BoxLayout(heroPanel, BoxLayout.Y_AXIS));
        heroPanel.setBorder(new EmptyBorder(50, 40, 50, 40));

        JPanel logoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        logoRow.setOpaque(false);
        logoRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lblBrand = new JLabel("Quầy Vé Azure Rail");
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblBrand.setForeground(ON_PRIMARY);
        logoRow.add(lblBrand);

        heroPanel.add(logoRow);
        heroPanel.add(Box.createVerticalStrut(40));

        JLabel lblHeroTitle = new JLabel("<html><div style='width:280px'>"
                + "Hệ thống Quản lý Bán vé tại Quầy</div></html>");
        lblHeroTitle.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblHeroTitle.setForeground(ON_PRIMARY);
        lblHeroTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        heroPanel.add(lblHeroTitle);
        heroPanel.add(Box.createVerticalStrut(20));

        JLabel lblHeroDesc = new JLabel("<html><div style='width:300px'>"
                + "Công cụ nghiệp vụ chuyên nghiệp cho việc xuất vé, "
                + "quản lý ca làm việc và điều phối hành trình "
                + "tại nhà ga.</div></html>");
        lblHeroDesc.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblHeroDesc.setForeground(new Color(0xF3, 0xF7, 0xFF));
        lblHeroDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        heroPanel.add(lblHeroDesc);

        heroPanel.add(Box.createVerticalGlue());

        JLabel lblFooterHero = new JLabel("Hơn 500 nhân viên đang làm việc");
        lblFooterHero.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblFooterHero.setForeground(new Color(0xF3, 0xF7, 0xFF, 200));
        lblFooterHero.setAlignmentX(Component.LEFT_ALIGNMENT);
        heroPanel.add(lblFooterHero);

        // -------- RIGHT: Login form --------
        JPanel formWrapper = new JPanel(new GridBagLayout());
        formWrapper.setBackground(Color.WHITE);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        formPanel.setPreferredSize(new Dimension(360, 480));

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

        JLabel lblUser = new JLabel("Mã nhân viên");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblUser.setForeground(ON_SURFACE_VAR);
        lblUser.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(lblUser);
        formPanel.add(Box.createVerticalStrut(6));

        txtUsername = createStyledTextField("Ví dụ: NV-0001");
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
        chkShowPassword.setBackground(Color.WHITE);
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
                    g2.setColor(PRIMARY.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(PRIMARY_CONTAINER);
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
                    g2.setColor(new Color(0x9E, 0xA7, 0xB0));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(0xB0, 0xB8, 0xC1));
                } else {
                    g2.setColor(new Color(0xC4, 0xCB, 0xD1));
                }
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnExit.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnExit.setForeground(new Color(0x40, 0x48, 0x50));
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
        JLabel lblFooter = new JLabel("© 2024 The Fluid Terminal Corporate");
        lblFooter.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblFooter.setForeground(OUTLINE);
        lblFooter.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(lblFooter);

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
        field.setBackground(SURFACE_HIGH);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SURFACE_HIGH, 1),
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
        field.setBackground(SURFACE_HIGH);
        field.setEchoChar('•');
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SURFACE_HIGH, 1),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        field.setPreferredSize(new Dimension(360, 48));
        return field;
    }

    private void execute() {
        lblError.setText(" ");
        String maNV = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (maNV.isEmpty()) {
            lblError.setText("Vui lòng nhập mã nhân viên.");
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
                return dao.checkLogin(maNV, password);
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
