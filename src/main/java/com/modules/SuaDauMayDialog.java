package com.modules;

import com.entity.DauMay;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Consumer;

public class SuaDauMayDialog extends JDialog {

    private static final Color PRIMARY       = new Color(0x00, 0x5D, 0x90);
    private static final Color PRIMARY_HOVER = new Color(0x00, 0x4A, 0x73);
    private static final Color CARD_BG       = Color.WHITE;
    private static final Color ON_SURFACE    = new Color(0x1A, 0x1D, 0x21);
    private static final Color ON_SURF_VAR   = new Color(0x5F, 0x67, 0x70);
    private static final Color OUTLINE       = new Color(0xDE, 0xE3, 0xE8);
    private static final Color ERROR         = new Color(0xBA, 0x1A, 0x1A);
    private static final Color HEADER_BG     = new Color(0xF1, 0xF5, 0xF9);
    private static final Color FOOTER_BG     = new Color(0xF1, 0xF5, 0xF9);

    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font FONT_DESC  = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font FONT_INPUT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_MONO  = new Font("Consolas", Font.BOLD, 13);
    private static final Font FONT_BTN   = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_ERR   = new Font("Segoe UI", Font.PLAIN, 11);

    private final DauMay source;
    private final Consumer<DauMay> onSaved;

    private JTextField txtMaDauMay;
    private JTextField txtTenDauMay;
    private JTextField txtHangSanXuat;
    private JTextField txtNamSanXuat;
    private JTextField txtCongSuat;
    private JComboBox<String> cboTrangThai;
    private JTextArea txtMoTa;

    private JLabel lblErrTen;
    private JLabel lblErrNam;
    private JLabel lblErrCongSuat;

    public SuaDauMayDialog(Window owner, DauMay dauMay, Consumer<DauMay> onSaved) {
        super(owner, "Sửa đầu máy", ModalityType.APPLICATION_MODAL);
        this.source = dauMay;
        this.onSaved = onSaved;

        setUndecorated(true);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        initUI();
        pack();
        setMinimumSize(new Dimension(760, getPreferredSize().height));
        setLocationRelativeTo(owner);
    }

    private void initUI() {
        setBackground(new Color(0, 0, 0, 0));

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CARD_BG);
        root.setBorder(BorderFactory.createLineBorder(OUTLINE, 1));

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildForm(), BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);

        setContentPane(ThemNhanVienDialog.buildShadowWrapper(root));
        getRootPane().registerKeyboardAction(
            e -> dispose(),
            KeyStroke.getKeyStroke("ESCAPE"),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, OUTLINE),
            new EmptyBorder(20, 28, 16, 28)
        ));

        JPanel left = new JPanel(new BorderLayout(12, 0));
        left.setOpaque(false);

        ImageIcon icon = loadScaledIcon("bieuTuongTau.png", 34);
        if (icon != null) {
            JLabel lblIcon = new JLabel(icon);
            lblIcon.setVerticalAlignment(SwingConstants.TOP);
            left.add(lblIcon, BorderLayout.WEST);
        }

        JPanel textPart = new JPanel();
        textPart.setLayout(new BoxLayout(textPart, BoxLayout.Y_AXIS));
        textPart.setOpaque(false);

        JLabel lblTitle = new JLabel("Chỉnh sửa đầu máy");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(PRIMARY);

        JLabel lblDesc = new JLabel("Cập nhật thông số kỹ thuật và trạng thái vận hành.");
        lblDesc.setFont(FONT_DESC);
        lblDesc.setForeground(ON_SURF_VAR);

        textPart.add(lblTitle);
        textPart.add(Box.createVerticalStrut(4));
        textPart.add(lblDesc);

        left.add(textPart, BorderLayout.CENTER);
        header.add(left, BorderLayout.CENTER);
        return header;
    }

    private JPanel buildForm() {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(CARD_BG);
        form.setBorder(new EmptyBorder(20, 24, 12, 24));

        JPanel row1 = new JPanel(new GridLayout(1, 2, 16, 0));
        row1.setOpaque(false);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        txtMaDauMay = createReadonlyField(source.getMaDauMay());
        row1.add(buildFieldGroup("Mã đầu máy", txtMaDauMay, "* Không thể thay đổi", false, null));

        txtTenDauMay = createInputField(source.getTenDauMay());
        lblErrTen = createErrorLabel();
        row1.add(buildFieldGroup("Tên đầu máy", txtTenDauMay, null, true, lblErrTen));

        form.add(row1);
        form.add(Box.createVerticalStrut(10));

        JPanel row2 = new JPanel(new GridLayout(1, 3, 16, 0));
        row2.setOpaque(false);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        txtHangSanXuat = createInputField(source.getHangSanXuat());
        row2.add(buildFieldGroup("Hãng sản xuất", txtHangSanXuat, null, false, null));

        txtNamSanXuat = createInputField(source.getNamSanXuat() == null ? "" : String.valueOf(source.getNamSanXuat()));
        lblErrNam = createErrorLabel();
        row2.add(buildFieldGroup("Năm sản xuất", txtNamSanXuat, null, false, lblErrNam));

        txtCongSuat = createInputField(source.getCongSuatKw() == null ? "" : String.valueOf(source.getCongSuatKw()));
        lblErrCongSuat = createErrorLabel();
        row2.add(buildFieldGroup("Công suất (kW)", txtCongSuat, null, false, lblErrCongSuat));

        form.add(row2);
        form.add(Box.createVerticalStrut(10));

        JPanel row3 = new JPanel(new GridLayout(1, 1, 0, 0));
        row3.setOpaque(false);
        row3.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        cboTrangThai = new JComboBox<>(new String[]{"Đang hoạt động", "Bảo trì", "Ngừng khai thác"});
        cboTrangThai.setFont(FONT_INPUT);
        cboTrangThai.setBackground(CARD_BG);
        cboTrangThai.setPreferredSize(new Dimension(0, 36));
        cboTrangThai.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        String status = source.getTrangThai();
        if (status != null && !status.isBlank()) cboTrangThai.setSelectedItem(status);
        row3.add(buildFieldGroup("Trạng thái", cboTrangThai, null, false, null));

        form.add(row3);
        form.add(Box.createVerticalStrut(10));

        JPanel row4 = new JPanel(new GridLayout(1, 1, 0, 0));
        row4.setOpaque(false);
        row4.setMaximumSize(new Dimension(Integer.MAX_VALUE, 128));

        txtMoTa = new JTextArea(source.getMoTa() == null ? "" : source.getMoTa(), 4, 30);
        txtMoTa.setLineWrap(true);
        txtMoTa.setWrapStyleWord(true);
        txtMoTa.setFont(FONT_INPUT);
        txtMoTa.setForeground(ON_SURFACE);
        txtMoTa.setBorder(new EmptyBorder(8, 10, 8, 10));
        JScrollPane sp = new JScrollPane(txtMoTa);
        sp.setBorder(BorderFactory.createLineBorder(OUTLINE, 1));
        sp.setPreferredSize(new Dimension(0, 90));
        sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        row4.add(buildFieldGroup("Mô tả kỹ thuật", sp, null, false, null));

        form.add(row4);
        return form;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setBackground(FOOTER_BG);
        footer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, OUTLINE),
            new EmptyBorder(14, 24, 14, 24)
        ));

        JButton btnCancel = createOutlineButton("Hủy bỏ");
        ImageIcon icoHuy = loadScaledIcon("nutThoat.png", 15);
        if (icoHuy != null) {
            btnCancel.setIcon(icoHuy);
            btnCancel.setIconTextGap(8);
        }
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = createPrimaryButton("Lưu thay đổi");
        ImageIcon icoLuu = loadScaledIcon("nutLuu.png", 15);
        if (icoLuu != null) {
            btnSave.setIcon(icoLuu);
            btnSave.setIconTextGap(8);
        }
        btnSave.addActionListener(e -> saveAndClose());
        getRootPane().setDefaultButton(btnSave);

        footer.add(btnCancel);
        footer.add(btnSave);
        return footer;
    }

    private JPanel buildFieldGroup(String labelText, JComponent input,
                                   String hintText, boolean required, JLabel errLabel) {
        JPanel group = new JPanel();
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
        group.setOpaque(false);

        JLabel lbl = new JLabel(labelText + (required ? " *" : ""));
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(ON_SURF_VAR);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        input.setAlignmentX(Component.LEFT_ALIGNMENT);
        group.add(lbl);
        group.add(Box.createVerticalStrut(6));
        group.add(input);

        if (hintText != null && !hintText.isBlank()) {
            JLabel hint = new JLabel(hintText);
            hint.setFont(new Font("Segoe UI", Font.ITALIC, 10));
            hint.setForeground(ON_SURF_VAR);
            hint.setAlignmentX(Component.LEFT_ALIGNMENT);
            group.add(Box.createVerticalStrut(3));
            group.add(hint);
        }

        if (errLabel != null) {
            errLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            group.add(Box.createVerticalStrut(2));
            group.add(errLabel);
        }

        return group;
    }

    private JTextField createInputField(String value) {
        JTextField tf = new JTextField(value == null ? "" : value);
        tf.setFont(FONT_INPUT);
        tf.setForeground(ON_SURFACE);
        tf.setBackground(CARD_BG);
        tf.setPreferredSize(new Dimension(0, 40));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(OUTLINE, 1),
            new EmptyBorder(8, 10, 8, 10)
        ));
        return tf;
    }

    private JTextField createReadonlyField(String value) {
        JTextField tf = createInputField(value);
        tf.setEditable(false);
        tf.setFont(FONT_MONO);
        tf.setForeground(PRIMARY);
        tf.setBackground(new Color(0xF8, 0xFA, 0xFC));
        return tf;
    }

    private JLabel createErrorLabel() {
        JLabel lbl = new JLabel(" ");
        lbl.setFont(FONT_ERR);
        lbl.setForeground(ERROR);
        return lbl;
    }

    private JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? PRIMARY_HOVER : PRIMARY);
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
        btn.setPreferredSize(new Dimension(176, 40));
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
        btn.setPreferredSize(new Dimension(120, 40));
        return btn;
    }

    private void saveAndClose() {
        clearErrors();
        if (!validateForm()) return;

        DauMay out = new DauMay();
        out.setMaDauMay(txtMaDauMay.getText().trim());
        out.setTenDauMay(txtTenDauMay.getText().trim());
        out.setHangSanXuat(toNullIfBlank(txtHangSanXuat.getText()));
        out.setNamSanXuat(parseIntegerOrNull(txtNamSanXuat.getText()));
        out.setCongSuatKw(parseIntegerOrNull(txtCongSuat.getText()));
        out.setTrangThai((String) cboTrangThai.getSelectedItem());
        out.setMoTa(toNullIfBlank(txtMoTa.getText()));

        if (onSaved != null) onSaved.accept(out);
        dispose();
    }

    private boolean validateForm() {
        boolean ok = true;

        if (txtTenDauMay.getText().trim().isEmpty()) {
            lblErrTen.setText("Tên đầu máy không được để trống.");
            ok = false;
        }

        String namText = txtNamSanXuat.getText().trim();
        if (!namText.isEmpty()) {
            try {
                int nam = Integer.parseInt(namText);
                if (nam < 1950 || nam > 2100) {
                    lblErrNam.setText("Năm sản xuất phải trong khoảng 1950 - 2100.");
                    ok = false;
                }
            } catch (NumberFormatException ex) {
                lblErrNam.setText("Năm sản xuất phải là số nguyên.");
                ok = false;
            }
        }

        String csText = txtCongSuat.getText().trim();
        if (!csText.isEmpty()) {
            try {
                int cs = Integer.parseInt(csText);
                if (cs <= 0 || cs > 10000) {
                    lblErrCongSuat.setText("Công suất phải > 0 và <= 10000 kW.");
                    ok = false;
                }
            } catch (NumberFormatException ex) {
                lblErrCongSuat.setText("Công suất phải là số nguyên.");
                ok = false;
            }
        }

        return ok;
    }

    private void clearErrors() {
        lblErrTen.setText(" ");
        lblErrNam.setText(" ");
        lblErrCongSuat.setText(" ");
    }

    private String toNullIfBlank(String text) {
        if (text == null) return null;
        String trimmed = text.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Integer parseIntegerOrNull(String text) {
        String trimmed = text == null ? "" : text.trim();
        if (trimmed.isEmpty()) return null;
        try {
            return Integer.parseInt(trimmed);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private ImageIcon loadScaledIcon(String name, int size) {
        try {
            java.net.URL url = getClass().getResource("/icons/" + name);
            if (url != null) {
                Image img = new ImageIcon(url).getImage()
                    .getScaledInstance(size, size, Image.SCALE_SMOOTH);
                return new ImageIcon(img);
            }
        } catch (Exception ignored) {}
        return null;
    }
}
