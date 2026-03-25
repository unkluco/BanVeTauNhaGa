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

/**
 * Dialog "Th\u00EAm nh\u00E2n vi\u00EAn m\u1EDBi" \u2014 phong c\u00E1ch Azure Rail.
 * Sau khi l\u01B0u th\u00E0nh c\u00F4ng, g\u1ECDi callback \u0111\u1EC3 module cha reload b\u1EA3ng.
 */
public class ThemNhanVienDialog extends JDialog {

    // === Design tokens (gi\u1EEF nh\u1EA5t qu\u00E1n v\u1EDBi module cha) ===
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

    // === Form fields ===
    private JTextField   txtMaNV;
    private JTextField   txtHoTen;
    private JTextField   txtSoDienThoai;
    private JPasswordField txtPassword;
    private JTextField   txtCccd;
    private JTextField   txtDiaChiTamTru;
    private JComboBox<String> cboBoPhan;

    private boolean saved = false;
    private Runnable onSaved;

    public ThemNhanVienDialog(Window owner, Runnable onSaved) {
        super(owner, "Th\u00EAm nh\u00E2n vi\u00EAn m\u1EDBi", ModalityType.APPLICATION_MODAL);
        this.onSaved = onSaved;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        initUI();
        pack();
        setMinimumSize(new Dimension(540, getPreferredSize().height));
        setLocationRelativeTo(owner);
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CARD_BG);
        root.setBorder(BorderFactory.createLineBorder(OUTLINE, 1));

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildForm(),   BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);

        setContentPane(root);
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
        form.setBorder(new EmptyBorder(24, 28, 8, 28));

        // --- Row 1: M\u00E3 NV (readonly) + H\u1ECD t\u00EAn ---
        JPanel row1 = new JPanel(new GridLayout(1, 2, 20, 0));
        row1.setOpaque(false);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        // M\u00E3 NV
        txtMaNV = createReadonlyField();
        new SwingWorker<String, Void>() {
            @Override protected String doInBackground() { return new DAO_NhanVien().generateNextMaNV(); }
            @Override protected void done() {
                try { txtMaNV.setText(get()); } catch (Exception e) { txtMaNV.setText("NV-????"); }
            }
        }.execute();
        row1.add(buildFieldGroup("M\u00E3 nh\u00E2n vi\u00EAn", txtMaNV, "* M\u00E3 s\u1ED1 n\u00E0y \u0111\u01B0\u1EE3c h\u1EC7 th\u1ED1ng t\u1EA1o t\u1EF1 \u0111\u1ED9ng", false));

        // H\u1ECD t\u00EAn
        txtHoTen = createInputField("Nh\u1EADp h\u1ECD v\u00E0 t\u00EAn \u0111\u1EA7y \u0111\u1EE7");
        row1.add(buildFieldGroup("H\u1ECD v\u00E0 t\u00EAn", txtHoTen, null, true));

        form.add(row1);
        form.add(Box.createVerticalStrut(16));

        // --- Row 2: S\u0110T + S\u1ED1 CCCD ---
        JPanel row2 = new JPanel(new GridLayout(1, 2, 20, 0));
        row2.setOpaque(false);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        txtSoDienThoai = createInputField("09xx xxx xxx");
        row2.add(buildFieldGroup("S\u1ED1 \u0111i\u1EC7n tho\u1EA1i", txtSoDienThoai, null, true));

        txtCccd = createInputField("Nh\u1EADp s\u1ED1 CCCD");
        row2.add(buildFieldGroup("S\u1ED1 CCCD", txtCccd, null, true));

        form.add(row2);
        form.add(Box.createVerticalStrut(16));

        // --- Row 3: B\u1ED9 ph\u1EADn + M\u1EADt kh\u1EA9u ---
        JPanel row3 = new JPanel(new GridLayout(1, 2, 20, 0));
        row3.setOpaque(false);
        row3.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        cboBoPhan = new JComboBox<>(new String[]{"B\u00E1n v\u00E9", "\u0110i\u1EC1u ph\u1ED1i"});
        cboBoPhan.setFont(FONT_INPUT);
        cboBoPhan.setBackground(CARD_BG);
        cboBoPhan.setPreferredSize(new Dimension(0, 40));
        row3.add(buildFieldGroup("B\u1ED9 ph\u1EADn", cboBoPhan, null, false));

        txtPassword = new JPasswordField();
        txtPassword.setFont(FONT_INPUT);
        txtPassword.setPreferredSize(new Dimension(0, 40));
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        addFocusBorderEffect(txtPassword);
        row3.add(buildFieldGroup("M\u1EADt kh\u1EA9u", txtPassword, "* M\u1EADt kh\u1EA9u \u0111\u0103ng nh\u1EADp ban \u0111\u1EA7u", true));

        form.add(row3);
        form.add(Box.createVerticalStrut(16));

        // --- Row 4: \u0110\u1ECBa ch\u1EC9 t\u1EA1m tr\u00FA (full width) ---
        JPanel row4 = new JPanel(new GridLayout(1, 1, 0, 0));
        row4.setOpaque(false);
        row4.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        txtDiaChiTamTru = createInputField("Nh\u1EADp \u0111\u1ECBa ch\u1EC9 t\u1EA1m tr\u00FA hi\u1EC7n t\u1EA1i");
        row4.add(buildFieldGroup("\u0110\u1ECBa ch\u1EC9 t\u1EA1m tr\u00FA", txtDiaChiTamTru, null, false));

        form.add(row4);

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
        // Validate (l\u1ECDc placeholder)
        String hoTen = getFieldText(txtHoTen, "Nh\u1EADp h\u1ECD v\u00E0 t\u00EAn \u0111\u1EA7y \u0111\u1EE7");
        String sdt = getFieldText(txtSoDienThoai, "09xx xxx xxx");
        String cccd = getFieldText(txtCccd, "Nh\u1EADp s\u1ED1 CCCD");
        String diaChi = getFieldText(txtDiaChiTamTru, "Nh\u1EADp \u0111\u1ECBa ch\u1EC9 t\u1EA1m tr\u00FA hi\u1EC7n t\u1EA1i");
        String password = new String(txtPassword.getPassword()).trim();
        String maNV = txtMaNV.getText().trim();

        if (hoTen.isEmpty()) {
            showValidationError("Vui l\u00F2ng nh\u1EADp h\u1ECD v\u00E0 t\u00EAn!");
            txtHoTen.requestFocus();
            return;
        }
        if (sdt.isEmpty()) {
            showValidationError("Vui l\u00F2ng nh\u1EADp s\u1ED1 \u0111i\u1EC7n tho\u1EA1i!");
            txtSoDienThoai.requestFocus();
            return;
        }
        if (!sdt.matches("\\d{10,11}")) {
            showValidationError("S\u1ED1 \u0111i\u1EC7n tho\u1EA1i ph\u1EA3i c\u00F3 10\u201311 ch\u1EEF s\u1ED1!");
            txtSoDienThoai.requestFocus();
            return;
        }
        if (cccd.isEmpty()) {
            showValidationError("Vui l\u00F2ng nh\u1EADp s\u1ED1 CCCD!");
            txtCccd.requestFocus();
            return;
        }
        if (!cccd.matches("\\d{12}")) {
            showValidationError("S\u1ED1 CCCD ph\u1EA3i c\u00F3 \u0111\u00FAng 12 ch\u1EEF s\u1ED1!");
            txtCccd.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            showValidationError("Vui l\u00F2ng nh\u1EADp m\u1EADt kh\u1EA9u!");
            txtPassword.requestFocus();
            return;
        }

        VaiTro vaiTro = cboBoPhan.getSelectedIndex() == 0 ? VaiTro.BAN_VE : VaiTro.DIEU_PHOI;
        NhanVien nv = new NhanVien(maNV, hoTen, password, vaiTro, sdt,
                cccd, diaChi.isEmpty() ? null : diaChi, TrangThaiNhanVien.DANG_LAM);

        // Save in background
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
                        showValidationError("Kh\u00F4ng th\u1EC3 l\u01B0u! M\u00E3 NV c\u00F3 th\u1EC3 \u0111\u00E3 t\u1ED3n t\u1EA1i.");
                    }
                } catch (Exception ex) {
                    showValidationError("L\u1ED7i: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void showValidationError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Th\u00F4ng b\u00E1o", JOptionPane.WARNING_MESSAGE);
    }

    public boolean isSaved() { return saved; }

    // ========================= UI HELPERS =========================

    private JPanel buildFieldGroup(String label, JComponent input, String hint, boolean required) {
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
        input.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        group.add(input);

        if (hint != null && !hint.isEmpty()) {
            group.add(Box.createVerticalStrut(4));
            JLabel lblHint = new JLabel(hint);
            lblHint.setFont(FONT_HINT);
            lblHint.setForeground(ON_SURF_VAR);
            lblHint.setAlignmentX(Component.LEFT_ALIGNMENT);
            group.add(lblHint);
        }

        return group;
    }

    private JTextField createReadonlyField() {
        JTextField f = new JTextField();
        f.setFont(FONT_MONO);
        f.setForeground(PRIMARY);
        f.setBackground(SURFACE);
        f.setEditable(false);
        f.setPreferredSize(new Dimension(0, 40));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        return f;
    }

    private JTextField createInputField(String placeholder) {
        JTextField f = new JTextField();
        f.setFont(FONT_INPUT);
        f.setForeground(ON_SURFACE);
        f.setPreferredSize(new Dimension(0, 40));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        // Placeholder
        f.setForeground(ON_SURF_VAR);
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

    /** Override getText to not return placeholder */
    private String getFieldText(JTextField f, String placeholder) {
        String t = f.getText();
        return t.equals(placeholder) ? "" : t;
    }

    private void addFocusBorderEffect(JComponent comp) {
        comp.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                comp.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(PRIMARY, 2, true),
                        new EmptyBorder(7, 11, 7, 11)  // compensate for thicker border
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
