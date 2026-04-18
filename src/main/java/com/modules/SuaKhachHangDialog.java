package com.modules;

import com.dao.DAO_KhachHang;
import com.entity.KhachHang;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.util.function.Consumer;

public class SuaKhachHangDialog extends JDialog {

    // === Design tokens ===
    private static final Color PRIMARY       = new Color(0x00, 0x5D, 0x90);
    private static final Color PRIMARY_HOVER = new Color(0x00, 0x4A, 0x73);
    private static final Color SURFACE       = new Color(0xF8, 0xFA, 0xFC);
    private static final Color CARD_BG       = Color.WHITE;
    private static final Color ON_SURFACE    = new Color(0x1A, 0x1D, 0x21);
    private static final Color ON_SURF_VAR   = new Color(0x5F, 0x67, 0x70);
    private static final Color OUTLINE       = new Color(0xDE, 0xE3, 0xE8);
    private static final Color ERROR         = new Color(0xBA, 0x1A, 0x1A);
    private static final Color HEADER_BG    = new Color(0xF1, 0xF5, 0xF9);
    private static final Color FOOTER_BG    = new Color(0xF1, 0xF5, 0xF9);
    private static final Color WRAPPER_BG   = new Color(0xEE, 0xF2, 0xF6);

    private static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font FONT_DESC   = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_LABEL  = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font FONT_INPUT  = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_MONO   = new Font("Consolas", Font.BOLD, 13);
    private static final Font FONT_BTN    = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_HINT   = new Font("Segoe UI", Font.ITALIC, 10);
    private static final Font FONT_ERR    = new Font("Segoe UI", Font.PLAIN, 11);

    // === Form fields ===
    private JTextField        txtMaKH;
    private JTextField        txtHoTen;
    private JTextField        txtSoDienThoai;
    private JTextField        txtCccd;
    private JTextField        txtEmail;
    private JTextField        txtDiaChiThuongTru;
    private JTextField        txtDiaChiTamTru;
    private DatePickerField   dpNgaySinh;
    private JComboBox<String> cboGioiTinh;
    private JTextField        txtQuocTich;

    // === Error labels ===
    private JLabel lblErrHoTen;
    private JLabel lblErrSoDienThoai;
    private JLabel lblErrCccd;

    // === Buttons ===
    private JButton btnSave;
    private JButton btnCancel;
    private JPanel  btnPanel;

    private final KhachHang original;
    private Runnable onSaved;
    private Consumer<Object> callback;

    public SuaKhachHangDialog(Window owner, KhachHang kh, Runnable onSaved) {
        super(owner, ModalityType.APPLICATION_MODAL);
        this.original = kh;
        this.onSaved = onSaved;
        setUndecorated(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        initUI();
        pack();
        setMinimumSize(new Dimension(760, getPreferredSize().height));
        setLocationRelativeTo(owner);
    }

    private void initUI() {
        setBackground(new Color(0, 0, 0, 0));

        JPanel root = new JPanel(new BorderLayout());
        root.setOpaque(true);
        root.setBackground(CARD_BG);
        root.setBorder(BorderFactory.createLineBorder(OUTLINE, 1));

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildForm(),   BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);

        setContentPane(ThemNhanVienDialog.buildShadowWrapper(root));
    }



    // ========================= HEADER =========================
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(true);
        header.setBackground(HEADER_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, OUTLINE),
                new EmptyBorder(20, 28, 16, 28)
        ));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel lblTitle = new JLabel("✎  Sửa thông tin khách hàng");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(PRIMARY);

        JLabel lblDesc = new JLabel("Chỉnh sửa thông tin khách hàng " + original.getMaKhachHang() + ".");
        lblDesc.setFont(FONT_DESC);
        lblDesc.setForeground(ON_SURF_VAR);

        left.add(lblTitle);
        left.add(Box.createVerticalStrut(4));
        left.add(lblDesc);

        header.add(left, BorderLayout.CENTER);

        // Close button (×)
        JButton btnClose = new JButton("✕") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(0xBA, 0x1A, 0x1A, 30));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnClose.setForeground(ON_SURF_VAR);
        btnClose.setContentAreaFilled(false);
        btnClose.setBorderPainted(false);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClose.setPreferredSize(new Dimension(32, 32));
        btnClose.addActionListener(e -> dispose());
        header.add(btnClose, BorderLayout.EAST);

        return header;
    }

    // ========================= FORM =========================
    private JPanel buildForm() {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(true);
        form.setBackground(CARD_BG);
        form.setBorder(new EmptyBorder(20, 24, 12, 24));

        // --- Row 1: Ma KH (readonly) | Ho va ten * ---
        JPanel row1 = new JPanel(new GridLayout(1, 2, 16, 0));
        row1.setOpaque(false);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        txtMaKH = createReadonlyField();
        txtMaKH.setText(original.getMaKhachHang());
        row1.add(buildFieldGroup("Mã khách hàng", txtMaKH, "* Không thể thay đổi", false, null));

        txtHoTen = createInputField();
        txtHoTen.setText(original.getHoTen());
        lblErrHoTen = createErrorLabel();
        row1.add(buildFieldGroup("Họ và tên", txtHoTen, null, true, lblErrHoTen));

        form.add(row1);
        form.add(Box.createVerticalStrut(10));

        // --- Row 2: So dien thoai * | So CCCD * | Email ---
        JPanel row2 = new JPanel(new GridLayout(1, 3, 16, 0));
        row2.setOpaque(false);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        txtSoDienThoai = createInputField();
        txtSoDienThoai.setText(original.getSoDienThoai() != null ? original.getSoDienThoai() : "");
        lblErrSoDienThoai = createErrorLabel();
        row2.add(buildFieldGroup("Số điện thoại", txtSoDienThoai, null, true, lblErrSoDienThoai));

        txtCccd = createInputField();
        txtCccd.setText(original.getCccd() != null ? original.getCccd() : "");
        lblErrCccd = createErrorLabel();
        row2.add(buildFieldGroup("Số CCCD", txtCccd, null, true, lblErrCccd));

        txtEmail = createInputField();
        txtEmail.setText(original.getEmail() != null ? original.getEmail() : "");
        row2.add(buildFieldGroup("Email", txtEmail, null, false, null));

        form.add(row2);
        form.add(Box.createVerticalStrut(10));

        // --- Row 3: Ngay sinh | Gioi tinh | Quoc tich ---
        JPanel row3 = new JPanel(new GridLayout(1, 3, 16, 0));
        row3.setOpaque(false);
        row3.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        dpNgaySinh = new DatePickerField();
        dpNgaySinh.setOpaque(true);
        dpNgaySinh.setPreferredSize(new Dimension(0, 40));
        dpNgaySinh.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        if (original.getNgaySinh() != null) {
            dpNgaySinh.setValue(original.getNgaySinh());
        }
        row3.add(buildFieldGroup("Ngày sinh", dpNgaySinh, null, false, null));

        cboGioiTinh = new JComboBox<>(new String[]{"-- Không chọn --", "Nam", "Nữ"});
        cboGioiTinh.setFont(FONT_INPUT);
        cboGioiTinh.setBackground(CARD_BG);
        cboGioiTinh.setOpaque(true);
        cboGioiTinh.setPreferredSize(new Dimension(0, 36));
        cboGioiTinh.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        if ("NAM".equalsIgnoreCase(original.getGioiTinh())) cboGioiTinh.setSelectedIndex(1);
        else if ("NU".equalsIgnoreCase(original.getGioiTinh())) cboGioiTinh.setSelectedIndex(2);
        row3.add(buildFieldGroup("Giới tính", cboGioiTinh, null, false, null));

        txtQuocTich = createInputField();
        txtQuocTich.setText(original.getQuocTich() != null ? original.getQuocTich() : "Việt Nam");
        row3.add(buildFieldGroup("Quốc tịch", txtQuocTich, null, false, null));

        form.add(row3);
        form.add(Box.createVerticalStrut(10));

        // --- Row 4 (full width): Dia chi thuong tru ---
        JPanel row4 = new JPanel(new GridLayout(1, 1, 0, 0));
        row4.setOpaque(false);
        row4.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        txtDiaChiThuongTru = createInputField();
        txtDiaChiThuongTru.setText(original.getDiaChiThuongTru() != null ? original.getDiaChiThuongTru() : "");
        row4.add(buildFieldGroup("Địa chỉ thường trú", txtDiaChiThuongTru, null, false, null));

        form.add(row4);
        form.add(Box.createVerticalStrut(10));

        // --- Row 5 (full width): Dia chi tam tru ---
        JPanel row5 = new JPanel(new GridLayout(1, 1, 0, 0));
        row5.setOpaque(false);
        row5.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        txtDiaChiTamTru = createInputField();
        txtDiaChiTamTru.setText(original.getDiaChiTamTru() != null ? original.getDiaChiTamTru() : "");
        row5.add(buildFieldGroup("Địa chỉ tạm trú", txtDiaChiTamTru, null, false, null));

        form.add(row5);

        return form;
    }

    // ========================= FOOTER =========================
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        footer.setOpaque(true);
        footer.setBackground(FOOTER_BG);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, OUTLINE),
                new EmptyBorder(16, 28, 16, 28)
        ));

        btnCancel = createOutlineButton("Hủy bỏ");
        btnCancel.addActionListener(e -> {
            if (callback != null) callback.accept(null);
            dispose();
        });

        btnSave = createPrimaryButton("Lưu thay đổi");
        btnSave.addActionListener(e -> doSave());

        btnPanel = new JPanel();
        btnPanel.setOpaque(false);
        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        footer.add(btnPanel);
        return footer;
    }

    // ========================= SAVE LOGIC =========================
    private void doSave() {
        clearAllErrors();

        String hoTen = txtHoTen.getText().trim();
        String sdt = txtSoDienThoai.getText().trim();
        String cccd = txtCccd.getText().trim();
        String email = txtEmail.getText().trim();
        LocalDate ngaySinh = dpNgaySinh.getValue();
        String diaChiThuongTru = txtDiaChiThuongTru.getText().trim();
        String diaChiTamTru = txtDiaChiTamTru.getText().trim();
        String quocTich = txtQuocTich.getText().trim();

        if (hoTen.isEmpty()) {
            showFieldError(txtHoTen, lblErrHoTen, "Vui lòng nhập họ và tên");
            return;
        }
        if (sdt.isEmpty()) {
            showFieldError(txtSoDienThoai, lblErrSoDienThoai, "Vui lòng nhập số điện thoại");
            return;
        }
        if (!sdt.matches("\\d{10,11}")) {
            showFieldError(txtSoDienThoai, lblErrSoDienThoai, "Số điện thoại phải có 10–11 chữ số");
            return;
        }
        if (cccd.isEmpty()) {
            showFieldError(txtCccd, lblErrCccd, "Vui lòng nhập số CCCD");
            return;
        }
        if (!cccd.matches("\\d{12}")) {
            showFieldError(txtCccd, lblErrCccd, "Số CCCD phải có đúng 12 chữ số");
            return;
        }

        String gioiTinh = switch (cboGioiTinh.getSelectedIndex()) {
            case 1 -> "NAM";
            case 2 -> "NU";
            default -> null;
        };

        original.setHoTen(hoTen);
        original.setSoDienThoai(sdt);
        original.setCccd(cccd);
        original.setEmail(email.isEmpty() ? null : email);
        original.setNgaySinh(ngaySinh);
        original.setDiaChiThuongTru(diaChiThuongTru.isEmpty() ? null : diaChiThuongTru);
        original.setDiaChiTamTru(diaChiTamTru.isEmpty() ? null : diaChiTamTru);
        original.setGioiTinh(gioiTinh);
        original.setQuocTich(quocTich.isEmpty() ? "Việt Nam" : quocTich);

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() {
                return new DAO_KhachHang().update(original);
            }
            @Override protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    if (get()) {
                        if (callback != null) callback.accept(original);
                        if (onSaved != null) onSaved.run();
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(SuaKhachHangDialog.this,
                                "Không thể lưu thay đổi!",
                                "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(SuaKhachHangDialog.this,
                            "Lỗi: " + ex.getMessage(),
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // ========================= ERROR HELPERS =========================

    private void showFieldError(JComponent field, JLabel errLabel, String msg) {
        errLabel.setText(msg);
        errLabel.setVisible(true);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ERROR, 2, true),
                new EmptyBorder(7, 11, 7, 11)
        ));
        field.requestFocusInWindow();
    }

    private void clearAllErrors() {
        JLabel[] errs     = {lblErrHoTen, lblErrSoDienThoai, lblErrCccd};
        JComponent[] flds = {txtHoTen, txtSoDienThoai, txtCccd};
        for (JLabel err : errs) { err.setText(""); err.setVisible(false); }
        for (JComponent fld : flds) {
            fld.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(OUTLINE, 1, true),
                    new EmptyBorder(8, 12, 8, 12)
            ));
        }
    }

    private JLabel createErrorLabel() {
        JLabel lbl = new JLabel();
        lbl.setFont(FONT_ERR);
        lbl.setForeground(ERROR);
        lbl.setVisible(false);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    // ========================= UI HELPERS =========================

    private JPanel buildFieldGroup(String label, JComponent input, String hint, boolean required, JLabel errLabel) {
        JPanel group = new JPanel();
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
        group.setOpaque(false);

        JPanel lblRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        lblRow.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(ON_SURF_VAR);
        lblRow.add(lbl);
        if (required) {
            JLabel star = new JLabel(" *");
            star.setFont(FONT_LABEL);
            star.setForeground(ERROR);
            lblRow.add(star);
        }
        lblRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        group.add(lblRow);
        group.add(Box.createVerticalStrut(6));

        input.setAlignmentX(Component.LEFT_ALIGNMENT);
        input.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        group.add(input);

        if (hint != null && !hint.isEmpty()) {
            group.add(Box.createVerticalStrut(4));
            JLabel lblHint = new JLabel(hint);
            lblHint.setFont(FONT_HINT);
            lblHint.setForeground(ON_SURF_VAR);
            lblHint.setAlignmentX(Component.LEFT_ALIGNMENT);
            group.add(lblHint);
        }

        if (errLabel != null) {
            group.add(Box.createVerticalStrut(3));
            group.add(errLabel);
        }

        return group;
    }

    private JTextField createReadonlyField() {
        JTextField f = new JTextField();
        f.setFont(FONT_MONO);
        f.setForeground(PRIMARY);
        f.setBackground(SURFACE);
        f.setEditable(false);
        f.setOpaque(true);
        f.setPreferredSize(new Dimension(0, 36));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        return f;
    }

    private JTextField createInputField() {
        JTextField f = new JTextField();
        f.setFont(FONT_INPUT);
        f.setForeground(ON_SURFACE);
        f.setOpaque(true);
        f.setPreferredSize(new Dimension(0, 36));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));

        f.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(PRIMARY, 2, true),
                        new EmptyBorder(7, 11, 7, 11)
                ));
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(OUTLINE, 1, true),
                        new EmptyBorder(8, 12, 8, 12)
                ));
            }
        });

        return f;
    }

    private JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isRollover() ? PRIMARY_HOVER : PRIMARY;
                g2.setColor(bg);
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
        btn.setPreferredSize(new Dimension(150, 40));
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
        btn.setPreferredSize(new Dimension(100, 40));
        return btn;
    }

    // ========================= PUBLIC =========================
    public void reset() {}
}