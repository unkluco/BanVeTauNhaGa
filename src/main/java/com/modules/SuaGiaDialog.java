package com.modules;

import com.dao.DAO_Gia;
import com.entity.Gia;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class SuaGiaDialog extends JDialog {

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
    private static final Font FONT_ERR    = new Font("Segoe UI", Font.PLAIN, 11);

    // === Form fields ===
    private JTextField        txtMaGia;
    private JTextField        txtMoTa;
    private DatePickerField   dpBatDau;
    private DatePickerField   dpKetThuc;
    private JComboBox<String> cboTrangThai;

    // === Error labels ===
    private JLabel lblErrMoTa;
    private JLabel lblErrBatDau;
    private JLabel lblErrKetThuc;

    private final Gia gia;
    private boolean saved = false;
    private Runnable onSaved;

    public SuaGiaDialog(Window owner, Gia gia, Runnable onSaved) {
        super(owner, "Chỉnh sửa kỳ giá", ModalityType.APPLICATION_MODAL);
        this.gia = gia;
        this.onSaved = onSaved;
        setUndecorated(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        initUI();
        populateFields();
        pack();
        setMinimumSize(new Dimension(600, getPreferredSize().height));
        setLocationRelativeTo(owner);
    }

    private void initUI() {
        setBackground(new Color(0, 0, 0, 0));

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CARD_BG);
        root.setBorder(BorderFactory.createLineBorder(OUTLINE, 1));

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildForm(),   BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);

        setContentPane(ThemNhanVienDialog.buildShadowWrapper(root));
    }

    private void populateFields() {
        txtMaGia.setText(gia.getMaGia());
        if (gia.getMoTa() != null) {
            txtMoTa.setText(gia.getMoTa());
            txtMoTa.setForeground(ON_SURFACE);
        }
        if (gia.getThoiGianBatDau() != null) {
            dpBatDau.setValue(gia.getThoiGianBatDau());
        }
        if (gia.getThoiGianKetThuc() != null) {
            dpKetThuc.setValue(gia.getThoiGianKetThuc());
        }
        cboTrangThai.setSelectedIndex(gia.isTrangThai() ? 0 : 1);
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

        JLabel lblTitle = new JLabel("Chỉnh sửa kỳ giá");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(PRIMARY);

        JLabel lblDesc = new JLabel("Cập nhật thông tin kỳ giá: " + gia.getMaGia());
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

        // Row 1: Ma gia (readonly) | Trang thai
        JPanel row1 = new JPanel(new GridLayout(1, 2, 16, 0));
        row1.setOpaque(false);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        txtMaGia = createReadonlyField();
        row1.add(buildFieldGroup("Mã giá", txtMaGia, "* Mã giá không thể thay đổi", false, null));

        cboTrangThai = new JComboBox<>(new String[]{"Đang áp dụng", "Ngừng áp dụng"});
        cboTrangThai.setFont(FONT_INPUT);
        cboTrangThai.setBackground(CARD_BG);
        cboTrangThai.setPreferredSize(new Dimension(0, 36));
        cboTrangThai.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        row1.add(buildFieldGroup("Trạng thái", cboTrangThai, null, false, null));

        form.add(row1);
        form.add(Box.createVerticalStrut(10));

        // Row 2: Mo ta (full width)
        JPanel row2 = new JPanel(new GridLayout(1, 1, 0, 0));
        row2.setOpaque(false);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        txtMoTa = createInputField("VD: Hà Nội - Sài Gòn Economy");
        lblErrMoTa = createErrorLabel();
        row2.add(buildFieldGroup("Mô tả / Tên kỳ giá", txtMoTa, null, true, lblErrMoTa));

        form.add(row2);
        form.add(Box.createVerticalStrut(10));

        // Row 3: Thoi gian bat dau | Thoi gian ket thuc
        JPanel row3 = new JPanel(new GridLayout(1, 2, 16, 0));
        row3.setOpaque(false);
        row3.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        dpBatDau = new DatePickerField();
        dpBatDau.setPreferredSize(new Dimension(0, 40));
        dpBatDau.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        lblErrBatDau = createErrorLabel();
        row3.add(buildFieldGroup("Ngày áp dụng", dpBatDau, null, true, lblErrBatDau));

        dpKetThuc = new DatePickerField();
        dpKetThuc.setPreferredSize(new Dimension(0, 40));
        dpKetThuc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        lblErrKetThuc = createErrorLabel();
        row3.add(buildFieldGroup("Ngày kết thúc", dpKetThuc, null, true, lblErrKetThuc));

        form.add(row3);

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

        JButton btnCancel = createOutlineButton("Hủy bỏ");
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = createPrimaryButton("Cập nhật");
        btnSave.addActionListener(e -> doSave());

        footer.add(btnCancel);
        footer.add(btnSave);
        return footer;
    }

    // ========================= SAVE LOGIC =========================
    private void doSave() {
        clearAllErrors();

        String moTa = getFieldText(txtMoTa, "VD: Hà Nội - Sài Gòn Economy");
        LocalDate batDau   = dpBatDau.getValue();
        LocalDate ketThuc  = dpKetThuc.getValue();

        if (moTa.isEmpty()) {
            showFieldError(txtMoTa, lblErrMoTa, "Vui lòng nhập mô tả");
            return;
        }
        if (batDau == null) {
            lblErrBatDau.setText("Vui lòng chọn ngày áp dụng");
            lblErrBatDau.setVisible(true);
            return;
        }
        if (ketThuc == null) {
            lblErrKetThuc.setText("Vui lòng chọn ngày kết thúc");
            lblErrKetThuc.setVisible(true);
            return;
        }
        if (!ketThuc.isAfter(batDau)) {
            lblErrKetThuc.setText("Thời gian kết thúc phải sau thời gian bắt đầu");
            lblErrKetThuc.setVisible(true);
            return;
        }

        boolean trangThai = cboTrangThai.getSelectedIndex() == 0;

        if (trangThai) {
            List<Gia> conflicts = new DAO_Gia().findOverlappingActive(gia.getMaGia(), batDau, ketThuc);
            if (!conflicts.isEmpty()) {
                String ids = conflicts.stream().map(Gia::getMaGia).collect(Collectors.joining(", "));
                showOverlapWarningDialog(ids);
                return;
            }
        }

        gia.setMoTa(moTa);
        gia.setThoiGianBatDau(batDau);
        gia.setThoiGianKetThuc(ketThuc);
        gia.setTrangThai(trangThai);

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() {
                return new DAO_Gia().update(gia);
            }
            @Override protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    if (get()) {
                        saved = true;
                        if (onSaved != null) onSaved.run();
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(SuaGiaDialog.this,
                                "Không thể cập nhật kỳ giá!",
                                "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(SuaGiaDialog.this,
                            "Lỗi: " + ex.getMessage(),
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
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
        lblErrMoTa.setText(""); lblErrMoTa.setVisible(false);
        lblErrBatDau.setText(""); lblErrBatDau.setVisible(false);
        lblErrKetThuc.setText(""); lblErrKetThuc.setVisible(false);
        txtMoTa.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
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
        input.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        group.add(input);

        if (hint != null && !hint.isEmpty()) {
            group.add(Box.createVerticalStrut(4));
            JLabel lblHint = new JLabel(hint);
            lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 10));
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
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(PRIMARY, 2, true),
                        new EmptyBorder(7, 11, 7, 11)
                ));
            }
            @Override public void focusLost(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(OUTLINE, 1, true),
                        new EmptyBorder(8, 12, 8, 12)
                ));
            }
        });
        return f;
    }

    private String getFieldText(JTextField f, String placeholder) {
        String t = f.getText();
        return t.equals(placeholder) ? "" : t.trim();
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

    private void showOverlapWarningDialog(String conflictIds) {
        JDialog dialog = new JDialog(this, "Trùng kỳ giá", ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setResizable(false);
        dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CARD_BG);
        root.setBorder(BorderFactory.createLineBorder(OUTLINE, 1));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, OUTLINE),
                new EmptyBorder(16, 20, 14, 20)
        ));

        JLabel lblTitle = new JLabel("Trùng kỳ giá");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitle.setForeground(ERROR);
        header.add(lblTitle, BorderLayout.WEST);

        JPanel body = new JPanel(new BorderLayout(14, 0));
        body.setBackground(CARD_BG);
        body.setBorder(new EmptyBorder(18, 20, 16, 20));

        JLabel icon = new JLabel();
        ImageIcon warnIco = loadScaledIcon("bieuTuongCanhBao.png", 36);
        if (warnIco != null) {
            icon.setIcon(warnIco);
        } else {
            icon.setText("!");
            icon.setFont(new Font("Segoe UI", Font.BOLD, 24));
            icon.setForeground(ERROR);
        }
        icon.setVerticalAlignment(SwingConstants.TOP);

        JPanel textCol = new JPanel();
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        textCol.setOpaque(false);

        JLabel lblMsg = new JLabel("Không thể kích hoạt kỳ giá này vì trùng khoảng thời gian với:");
        lblMsg.setFont(FONT_INPUT);
        lblMsg.setForeground(ON_SURFACE);
        lblMsg.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblIds = new JLabel(conflictIds);
        lblIds.setFont(new Font("Consolas", Font.BOLD, 14));
        lblIds.setForeground(new Color(0x8B, 0x1E, 0x1E));
        lblIds.setAlignmentX(Component.LEFT_ALIGNMENT);

        textCol.add(lblMsg);
        textCol.add(Box.createVerticalStrut(8));
        textCol.add(lblIds);

        body.add(icon, BorderLayout.WEST);
        body.add(textCol, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setBackground(FOOTER_BG);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, OUTLINE),
                new EmptyBorder(12, 20, 12, 20)
        ));

        JButton btnOk = createPrimaryButton("OK");
        btnOk.setPreferredSize(new Dimension(96, 38));
        btnOk.addActionListener(e -> dialog.dispose());
        footer.add(btnOk);

        root.add(header, BorderLayout.NORTH);
        root.add(body, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        dialog.setContentPane(ThemNhanVienDialog.buildShadowWrapper(root));
        dialog.pack();
        dialog.setMinimumSize(new Dimension(560, dialog.getPreferredSize().height));
        dialog.setLocationRelativeTo(this);
        dialog.getRootPane().setDefaultButton(btnOk);
        dialog.getRootPane().registerKeyboardAction(
                e -> dialog.dispose(),
                KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
        dialog.setVisible(true);
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
