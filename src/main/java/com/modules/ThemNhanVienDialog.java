package com.modules;

import com.dao.DAO_NhanVien;
import com.entity.NhanVien;
import com.enums.TrangThaiNhanVien;
import com.enums.VaiTro;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class ThemNhanVienDialog extends JDialog {

    // === Design tokens ===
    private static final Color PRIMARY       = new Color(0x00, 0x5D, 0x90);
    private static final Color PRIMARY_HOVER = new Color(0x00, 0x4A, 0x73);
    private static final Color SURFACE       = new Color(0xF8, 0xFA, 0xFC);
    private static final Color CARD_BG       = Color.WHITE;
    private static final Color ON_SURFACE    = new Color(0x1A, 0x1D, 0x21);
    private static final Color ON_SURF_VAR   = new Color(0x5F, 0x67, 0x70);
    private static final Color OUTLINE       = new Color(0xDE, 0xE3, 0xE8);
    private static final Color ERROR         = new Color(0xBA, 0x1A, 0x1A);
    private static final Color HEADER_BG     = new Color(0xF1, 0xF5, 0xF9);
    private static final Color FOOTER_BG     = new Color(0xF1, 0xF5, 0xF9);

    private static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font FONT_DESC   = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_LABEL  = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font FONT_INPUT  = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_MONO   = new Font("Consolas", Font.BOLD, 13);
    private static final Font FONT_BTN    = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_HINT   = new Font("Segoe UI", Font.ITALIC, 10);
    private static final Font FONT_ERR    = new Font("Segoe UI", Font.PLAIN, 11);

    // === Form fields ===
    private JTextField        txtMaNV;
    private JTextField        txtHoTen;
    private JTextField        txtSoDienThoai;
    private JPasswordField    txtPassword;
    private JTextField        txtCccd;
    private JTextField        txtDiaChiTamTru;
    private JComboBox<String> cboBoPhan;
    private JComboBox<String> cboTrangThai;

    // new fields
    private JTextField        txtEmail;
    private JComboBox<String> cboGaLamViec;
    private JTextField        txtDiaChiThuongTru;
    private JTextField        txtNgaySinh;
    private JComboBox<String> cboGioiTinh;
    private JTextField        txtQuocTich;
    private String[]          gaKeys;   // maGa values corresponding to cboGaLamViec items (index 0 = null)

    // === Error labels ===
    private JLabel lblErrHoTen;
    private JLabel lblErrSoDienThoai;
    private JLabel lblErrCccd;
    private JLabel lblErrPassword;
    private JLabel lblErrNgaySinh;

    private boolean saved = false;
    private Runnable onSaved;

    public ThemNhanVienDialog(Window owner, Runnable onSaved) {
        super(owner, "Th\u00EAm nh\u00E2n vi\u00EAn m\u1EDBi", ModalityType.APPLICATION_MODAL);
        this.onSaved = onSaved;
        // Load Ga options synchronously before building UI
        loadGaOptions();
        setUndecorated(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        initUI();
        pack();
        setMinimumSize(new Dimension(760, getPreferredSize().height));
        setLocationRelativeTo(owner);
    }

    private void loadGaOptions() {
        List<String[]> gaList = new DAO_NhanVien().getAllGa();
        // index 0 = "-- Chua phan cong --" (null), then each ga
        gaKeys = new String[gaList.size() + 1];
        gaKeys[0] = null;
        String[] gaDisplayItems = new String[gaList.size() + 1];
        gaDisplayItems[0] = "-- Ch\u01B0a ph\u00E2n c\u00F4ng --";
        for (int i = 0; i < gaList.size(); i++) {
            gaKeys[i + 1] = gaList.get(i)[0];
            gaDisplayItems[i + 1] = gaList.get(i)[1];
        }
        cboGaLamViec = new JComboBox<>(gaDisplayItems);
        cboGaLamViec.setFont(FONT_INPUT);
        cboGaLamViec.setBackground(CARD_BG);
        cboGaLamViec.setPreferredSize(new Dimension(0, 36));
        cboGaLamViec.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
    }

    private void initUI() {
        setBackground(new Color(0, 0, 0, 0));

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CARD_BG);
        root.setBorder(BorderFactory.createLineBorder(OUTLINE, 1));

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildForm(),   BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);

        setContentPane(buildShadowWrapper(root));
    }

    static JPanel buildShadowWrapper(JPanel content) {
        final int PAD = 16;
        JPanel wrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                for (int i = 1; i <= PAD; i++) {
                    int alpha = Math.round(10f * i / PAD);
                    g2.setColor(new Color(0, 0, 0, alpha));
                    int offset = PAD - i;
                    g2.fillRoundRect(offset, offset + 3, getWidth() - offset * 2,
                                     getHeight() - offset * 2, 8, 8);
                }
                g2.dispose();
            }
        };
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(PAD, PAD, PAD + 4, PAD));
        wrapper.add(content, BorderLayout.CENTER);
        return wrapper;
    }

    // ========================= HEADER =========================
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, OUTLINE),
                new EmptyBorder(20, 28, 16, 28)
        ));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel lblTitle = new JLabel("\u2795  Th\u00EAm nh\u00E2n vi\u00EAn m\u1EDBi");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(PRIMARY);

        JLabel lblDesc = new JLabel("Vui l\u00F2ng \u0111i\u1EC1n \u0111\u1EA7y \u0111\u1EE7 th\u00F4ng tin b\u00EAn d\u01B0\u1EDBi \u0111\u1EC3 t\u1EA1o t\u00E0i kho\u1EA3n m\u1EDBi.");
        lblDesc.setFont(FONT_DESC);
        lblDesc.setForeground(ON_SURF_VAR);

        left.add(lblTitle);
        left.add(Box.createVerticalStrut(4));
        left.add(lblDesc);

        header.add(left, BorderLayout.CENTER);
        return header;
    }

    // ========================= FORM =========================
    private JPanel buildForm() {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(CARD_BG);
        form.setBorder(new EmptyBorder(20, 24, 12, 24));

        // --- Row 1 (1x2): Ma NV (readonly) | Ho va ten * ---
        JPanel row1 = new JPanel(new GridLayout(1, 2, 16, 0));
        row1.setOpaque(false);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        txtMaNV = createReadonlyField();
        new SwingWorker<String, Void>() {
            @Override protected String doInBackground() { return new DAO_NhanVien().generateNextMaNV(); }
            @Override protected void done() {
                try { txtMaNV.setText(get()); } catch (Exception e) { txtMaNV.setText("NV-????"); }
            }
        }.execute();
        row1.add(buildFieldGroup("M\u00E3 nh\u00E2n vi\u00EAn", txtMaNV, "* M\u00E3 s\u1ED1 n\u00E0y \u0111\u01B0\u1EE3c h\u1EC7 th\u1ED1ng t\u1EA1o t\u1EF1 \u0111\u1ED9ng", false, null));

        txtHoTen = createInputField("Nh\u1EADp h\u1ECD v\u00E0 t\u00EAn \u0111\u1EA7y \u0111\u1EE7");
        lblErrHoTen = createErrorLabel();
        row1.add(buildFieldGroup("H\u1ECD v\u00E0 t\u00EAn", txtHoTen, null, true, lblErrHoTen));

        form.add(row1);
        form.add(Box.createVerticalStrut(10));

        // --- Row 2 (1x3): So dien thoai * | So CCCD * | Email ---
        JPanel row2 = new JPanel(new GridLayout(1, 3, 16, 0));
        row2.setOpaque(false);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        txtSoDienThoai = createInputField("09xx xxx xxx");
        lblErrSoDienThoai = createErrorLabel();
        row2.add(buildFieldGroup("S\u1ED1 \u0111i\u1EC7n tho\u1EA1i", txtSoDienThoai, null, true, lblErrSoDienThoai));

        txtCccd = createInputField("Nh\u1EADp s\u1ED1 CCCD");
        lblErrCccd = createErrorLabel();
        row2.add(buildFieldGroup("S\u1ED1 CCCD", txtCccd, null, true, lblErrCccd));

        txtEmail = createInputField("example@email.com");
        row2.add(buildFieldGroup("Email", txtEmail, null, false, null));

        form.add(row2);
        form.add(Box.createVerticalStrut(10));

        // --- Row 3 (1x3): Ngay sinh | Gioi tinh | Quoc tich ---
        JPanel row3 = new JPanel(new GridLayout(1, 3, 16, 0));
        row3.setOpaque(false);
        row3.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        txtNgaySinh = createInputField("dd/MM/yyyy");
        lblErrNgaySinh = createErrorLabel();
        row3.add(buildFieldGroup("Ng\u00E0y sinh", txtNgaySinh, null, false, lblErrNgaySinh));

        cboGioiTinh = new JComboBox<>(new String[]{"-- Kh\u00F4ng ch\u1ECDn --", "Nam", "N\u1EEF"});
        cboGioiTinh.setFont(FONT_INPUT);
        cboGioiTinh.setBackground(CARD_BG);
        cboGioiTinh.setPreferredSize(new Dimension(0, 36));
        cboGioiTinh.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        row3.add(buildFieldGroup("Gi\u1EDBi t\u00EDnh", cboGioiTinh, null, false, null));

        txtQuocTich = createInputField("Vi\u1EC7t Nam");
        txtQuocTich.setText("Vi\u1EC7t Nam");
        txtQuocTich.setForeground(ON_SURFACE);
        row3.add(buildFieldGroup("Qu\u1ED1c t\u1ECBch", txtQuocTich, null, false, null));

        form.add(row3);
        form.add(Box.createVerticalStrut(10));

        // --- Row 4 (1x3): Bo phan | Trang thai | Ga lam viec ---
        JPanel row4 = new JPanel(new GridLayout(1, 3, 16, 0));
        row4.setOpaque(false);
        row4.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        cboBoPhan = new JComboBox<>(new String[]{"B\u00E1n v\u00E9", "\u0110i\u1EC1u ph\u1ED1i", "Admin"});
        cboBoPhan.setFont(FONT_INPUT);
        cboBoPhan.setBackground(CARD_BG);
        cboBoPhan.setPreferredSize(new Dimension(0, 36));
        cboBoPhan.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        row4.add(buildFieldGroup("B\u1ED9 ph\u1EADn", cboBoPhan, null, false, null));

        cboTrangThai = new JComboBox<>(new String[]{"\u0110ang l\u00E0m", "Ngh\u1EC9 ph\u00E9p", "\u0110\u00E3 ngh\u1EC9"});
        cboTrangThai.setFont(FONT_INPUT);
        cboTrangThai.setBackground(CARD_BG);
        cboTrangThai.setPreferredSize(new Dimension(0, 36));
        cboTrangThai.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        row4.add(buildFieldGroup("Tr\u1EA1ng th\u00E1i", cboTrangThai, null, false, null));

        row4.add(buildFieldGroup("Ga l\u00E0m vi\u1EC7c", cboGaLamViec, null, false, null));

        form.add(row4);
        form.add(Box.createVerticalStrut(10));

        // --- Row 5 (full width): Mat khau * ---
        JPanel row5 = new JPanel(new GridLayout(1, 1, 0, 0));
        row5.setOpaque(false);
        row5.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        txtPassword = new JPasswordField();
        txtPassword.setFont(FONT_INPUT);
        txtPassword.setPreferredSize(new Dimension(0, 36));
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        addFocusBorderEffect(txtPassword);
        lblErrPassword = createErrorLabel();
        row5.add(buildFieldGroup("M\u1EADt kh\u1EA9u", txtPassword, "* M\u1EADt kh\u1EA9u \u0111\u0103ng nh\u1EADp ban \u0111\u1EA7u", true, lblErrPassword));

        form.add(row5);
        form.add(Box.createVerticalStrut(10));

        // --- Row 6 (full width): Dia chi tam tru ---
        JPanel row6 = new JPanel(new GridLayout(1, 1, 0, 0));
        row6.setOpaque(false);
        row6.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        txtDiaChiTamTru = createInputField("Nh\u1EADp \u0111\u1ECBa ch\u1EC9 t\u1EA1m tr\u00FA hi\u1EC7n t\u1EA1i");
        row6.add(buildFieldGroup("\u0110\u1ECBa ch\u1EC9 t\u1EA1m tr\u00FA", txtDiaChiTamTru, null, false, null));

        form.add(row6);
        form.add(Box.createVerticalStrut(10));

        // --- Row 7 (full width): Dia chi thuong tru ---
        JPanel row7 = new JPanel(new GridLayout(1, 1, 0, 0));
        row7.setOpaque(false);
        row7.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        txtDiaChiThuongTru = createInputField("Nh\u1EADp \u0111\u1ECBa ch\u1EC9 th\u01B0\u1EDDng tr\u00FA");
        row7.add(buildFieldGroup("\u0110\u1ECBa ch\u1EC9 th\u01B0\u1EDDng tr\u00FA", txtDiaChiThuongTru, null, false, null));

        form.add(row7);

        return form;
    }

    // ========================= FOOTER =========================
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        footer.setBackground(FOOTER_BG);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, OUTLINE),
                new EmptyBorder(16, 28, 16, 28)
        ));

        JButton btnCancel = createOutlineButton("H\u1EE7y b\u1ECF");
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = createPrimaryButton("L\u01B0u nh\u00E2n vi\u00EAn");
        btnSave.addActionListener(e -> doSave());

        footer.add(btnCancel);
        footer.add(btnSave);
        return footer;
    }

    // ========================= SAVE LOGIC =========================
    private void doSave() {
        clearAllErrors();

        String hoTen = getFieldText(txtHoTen, "Nh\u1EADp h\u1ECD v\u00E0 t\u00EAn \u0111\u1EA7y \u0111\u1EE7");
        String sdt = getFieldText(txtSoDienThoai, "09xx xxx xxx");
        String cccd = getFieldText(txtCccd, "Nh\u1EADp s\u1ED1 CCCD");
        String email = getFieldText(txtEmail, "example@email.com");
        String ngaySinhStr = getFieldText(txtNgaySinh, "dd/MM/yyyy");
        String diaChiThuongTru = getFieldText(txtDiaChiThuongTru, "Nh\u1EADp \u0111\u1ECBa ch\u1EC9 th\u01B0\u1EDDng tr\u00FA");
        String diaChiTamTru = getFieldText(txtDiaChiTamTru, "Nh\u1EADp \u0111\u1ECBa ch\u1EC9 t\u1EA1m tr\u00FA hi\u1EC7n t\u1EA1i");
        String password = new String(txtPassword.getPassword()).trim();
        String maNV = txtMaNV.getText().trim();
        String quocTich = txtQuocTich.getText().trim();

        // Validation
        if (hoTen.isEmpty()) {
            showFieldError(txtHoTen, lblErrHoTen, "Vui l\u00F2ng nh\u1EADp h\u1ECD v\u00E0 t\u00EAn");
            return;
        }
        if (sdt.isEmpty()) {
            showFieldError(txtSoDienThoai, lblErrSoDienThoai, "Vui l\u00F2ng nh\u1EADp s\u1ED1 \u0111i\u1EC7n tho\u1EA1i");
            return;
        }
        if (!sdt.matches("\\d{10,11}")) {
            showFieldError(txtSoDienThoai, lblErrSoDienThoai, "S\u1ED1 \u0111i\u1EC7n tho\u1EA1i ph\u1EA3i c\u00F3 10\u201311 ch\u1EEF s\u1ED1");
            return;
        }
        if (cccd.isEmpty()) {
            showFieldError(txtCccd, lblErrCccd, "Vui l\u00F2ng nh\u1EADp s\u1ED1 CCCD");
            return;
        }
        if (!cccd.matches("\\d{12}")) {
            showFieldError(txtCccd, lblErrCccd, "S\u1ED1 CCCD ph\u1EA3i c\u00F3 \u0111\u00FAng 12 ch\u1EEF s\u1ED1");
            return;
        }
        if (password.isEmpty()) {
            showFieldError(txtPassword, lblErrPassword, "Vui l\u00F2ng nh\u1EADp m\u1EADt kh\u1EA9u");
            return;
        }

        // Parse ngaySinh
        LocalDate ngaySinh = null;
        if (!ngaySinhStr.isEmpty()) {
            try {
                ngaySinh = LocalDate.parse(ngaySinhStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } catch (DateTimeParseException ex) {
                showFieldError(txtNgaySinh, lblErrNgaySinh, "Ng\u00E0y sinh kh\u00F4ng h\u1EE3p l\u1EC7 (dd/MM/yyyy)");
                return;
            }
        }

        // Resolve vaiTro
        VaiTro vaiTro = switch (cboBoPhan.getSelectedIndex()) {
            case 0 -> VaiTro.BAN_VE;
            case 1 -> VaiTro.DIEU_PHOI;
            default -> VaiTro.ADMIN;
        };
        // Resolve trangThai
        TrangThaiNhanVien trangThai = switch (cboTrangThai.getSelectedIndex()) {
            case 1 -> TrangThaiNhanVien.NGHI_PHEP;
            case 2 -> TrangThaiNhanVien.DA_NGHI;
            default -> TrangThaiNhanVien.DANG_LAM;
        };
        // Resolve gaLamViec
        int gaIdx = cboGaLamViec.getSelectedIndex();
        String gaLamViec = (gaIdx >= 0 && gaIdx < gaKeys.length) ? gaKeys[gaIdx] : null;

        // Resolve gioiTinh
        String gioiTinh = switch (cboGioiTinh.getSelectedIndex()) {
            case 1 -> "NAM";
            case 2 -> "NU";
            default -> null;
        };

        NhanVien nv = new NhanVien(maNV, hoTen, password, vaiTro, sdt,
                cccd, diaChiTamTru.isEmpty() ? null : diaChiTamTru, trangThai);
        nv.setEmail(email.isEmpty() ? null : email);
        nv.setGaLamViec(gaLamViec);
        nv.setDiaChiThuongTru(diaChiThuongTru.isEmpty() ? null : diaChiThuongTru);
        nv.setNgaySinh(ngaySinh);
        nv.setGioiTinh(gioiTinh);
        nv.setQuocTich(quocTich.isEmpty() ? null : quocTich);

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() {
                return new DAO_NhanVien().insert(nv);
            }
            @Override protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    if (get()) {
                        saved = true;
                        if (onSaved != null) onSaved.run();
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(ThemNhanVienDialog.this,
                                "Kh\u00F4ng th\u1EC3 l\u01B0u! M\u00E3 NV c\u00F3 th\u1EC3 \u0111\u00E3 t\u1ED3n t\u1EA1i.",
                                "L\u1ED7i", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ThemNhanVienDialog.this,
                            "L\u1ED7i: " + ex.getMessage(),
                            "L\u1ED7i", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    public boolean isSaved() { return saved; }

    // ========================= INLINE ERROR HELPERS =========================

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
        JLabel[] errs     = {lblErrHoTen, lblErrSoDienThoai, lblErrCccd, lblErrPassword, lblErrNgaySinh};
        JComponent[] flds = {txtHoTen, txtSoDienThoai, txtCccd, txtPassword, txtNgaySinh};
        for (int i = 0; i < errs.length; i++) {
            errs[i].setText("");
            errs[i].setVisible(false);
            flds[i].setBorder(BorderFactory.createCompoundBorder(
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
        f.setPreferredSize(new Dimension(0, 36));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        return f;
    }

    private JTextField createInputField(String placeholder) {
        JTextField f = new JTextField();
        f.setFont(FONT_INPUT);
        f.setForeground(ON_SURF_VAR);
        f.setPreferredSize(new Dimension(0, 36));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        f.setText(placeholder);
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (f.getText().equals(placeholder)) {
                    f.setText("");
                    f.setForeground(ON_SURFACE);
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (f.getText().trim().isEmpty()) {
                    f.setForeground(ON_SURF_VAR);
                    f.setText(placeholder);
                }
            }
        });
        addFocusBorderEffect(f);
        return f;
    }

    private String getFieldText(JTextField f, String placeholder) {
        String t = f.getText();
        return t.equals(placeholder) ? "" : t.trim();
    }

    private void addFocusBorderEffect(JComponent comp) {
        comp.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                comp.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(PRIMARY, 2, true),
                        new EmptyBorder(7, 11, 7, 11)
                ));
            }
            @Override public void focusLost(FocusEvent e) {
                comp.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(OUTLINE, 1, true),
                        new EmptyBorder(8, 12, 8, 12)
                ));
            }
        });
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
}
