package com.modules;

import com.dao.DAO_KhuyenMai;
import com.entity.KhuyenMai;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;

public class ThemKhuyenMaiDialog extends JDialog {

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
    private static final Font FONT_HINT  = new Font("Segoe UI", Font.ITALIC, 10);
    private static final Font FONT_ERR   = new Font("Segoe UI", Font.PLAIN, 11);

    private JTextField        txtMaKM;
    private JTextField        txtTenKM;
    private JTextField        txtMoTa;
    private DatePickerField   dpBatDau;
    private JSpinner          spTimeBatDau;
    private DatePickerField   dpKetThuc;
    private JSpinner          spTimeKetThuc;
    private JComboBox<String> cboTrangThai;

    private JLabel lblErrMaKM;
    private JLabel lblErrTenKM;
    private JLabel lblErrBatDau;
    private JLabel lblErrKetThuc;

    private boolean saved = false;
    private final Runnable onSaved;

    public ThemKhuyenMaiDialog(Window owner, Runnable onSaved) {
        super(owner, "Thêm khuyến mãi mới", ModalityType.APPLICATION_MODAL);
        this.onSaved = onSaved;
        setUndecorated(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        initUI();
        pack();
        setMinimumSize(new Dimension(640, getPreferredSize().height));
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

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, OUTLINE),
                new EmptyBorder(20, 28, 16, 28)));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel lblTitle = new JLabel("Thêm khuyến mãi mới");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(PRIMARY);

        JLabel lblDesc = new JLabel("Vui lòng điền đầy đủ thông tin để tạo chương trình khuyến mãi.");
        lblDesc.setFont(FONT_DESC);
        lblDesc.setForeground(ON_SURF_VAR);

        left.add(lblTitle);
        left.add(Box.createVerticalStrut(4));
        left.add(lblDesc);
        header.add(left, BorderLayout.CENTER);
        return header;
    }

    private JPanel buildForm() {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(CARD_BG);
        form.setBorder(new EmptyBorder(20, 24, 12, 24));

        // Row 1: Mã KM | Trạng thái
        JPanel row1 = new JPanel(new GridLayout(1, 2, 16, 0));
        row1.setOpaque(false);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        txtMaKM = createInputField("VD: KM-2025-01");
        lblErrMaKM = createErrorLabel();
        row1.add(buildFieldGroup("Mã khuyến mãi", txtMaKM, null, true, lblErrMaKM));

        cboTrangThai = new JComboBox<>(new String[]{"Đang áp dụng", "Ngừng áp dụng"});
        cboTrangThai.setFont(FONT_INPUT);
        cboTrangThai.setBackground(CARD_BG);
        cboTrangThai.setPreferredSize(new Dimension(0, 36));
        cboTrangThai.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        row1.add(buildFieldGroup("Trạng thái", cboTrangThai, null, false, null));

        form.add(row1);
        form.add(Box.createVerticalStrut(10));

        // Row 2: Tên KM (full width)
        JPanel row2 = new JPanel(new GridLayout(1, 1));
        row2.setOpaque(false);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        txtTenKM = createInputField("VD: Giảm giá dịp Tết 2025");
        lblErrTenKM = createErrorLabel();
        row2.add(buildFieldGroup("Tên chương trình", txtTenKM, null, true, lblErrTenKM));

        form.add(row2);
        form.add(Box.createVerticalStrut(10));

        // Row 3: Mô tả (full width)
        JPanel row3 = new JPanel(new GridLayout(1, 1));
        row3.setOpaque(false);
        row3.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        txtMoTa = createInputField("VD: Giảm giá vé tàu các tuyến dịp Tết Nguyên Đán");
        row3.add(buildFieldGroup("Mô tả", txtMoTa, null, false, null));

        form.add(row3);
        form.add(Box.createVerticalStrut(10));

        // Row 4: Thời gian bắt đầu | Thời gian kết thúc
        JPanel row4 = new JPanel(new GridLayout(1, 2, 16, 0));
        row4.setOpaque(false);
        row4.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        dpBatDau     = new DatePickerField();
        spTimeBatDau = createTimeSpinner();
        lblErrBatDau = createErrorLabel();
        row4.add(buildFieldGroup("Thời gian bắt đầu", buildDateTimePanel(dpBatDau, spTimeBatDau), null, true, lblErrBatDau));

        dpKetThuc     = new DatePickerField();
        spTimeKetThuc = createTimeSpinner();
        lblErrKetThuc = createErrorLabel();
        row4.add(buildFieldGroup("Thời gian kết thúc", buildDateTimePanel(dpKetThuc, spTimeKetThuc), null, true, lblErrKetThuc));

        form.add(row4);
        return form;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        footer.setBackground(FOOTER_BG);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, OUTLINE),
                new EmptyBorder(16, 28, 16, 28)));

        JButton btnCancel = createOutlineButton("Hủy bỏ");
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = createPrimaryButton("Lưu khuyến mãi");
        btnSave.addActionListener(e -> doSave());

        footer.add(btnCancel);
        footer.add(btnSave);
        return footer;
    }

    private void doSave() {
        clearAllErrors();

        String maKM  = getFieldText(txtMaKM,  "VD: KM-2025-01");
        String tenKM = getFieldText(txtTenKM, "VD: Giảm giá dịp Tết 2025");
        String moTa  = getFieldText(txtMoTa,  "VD: Giảm giá vé tàu các tuyến dịp Tết Nguyên Đán");

        LocalDate dateBatDau  = dpBatDau.getValue();
        LocalDate dateKetThuc = dpKetThuc.getValue();

        if (maKM.isEmpty()) {
            showFieldError(txtMaKM, lblErrMaKM, "Vui lòng nhập mã khuyến mãi");
            return;
        }
        if (tenKM.isEmpty()) {
            showFieldError(txtTenKM, lblErrTenKM, "Vui lòng nhập tên chương trình");
            return;
        }
        if (dateBatDau == null) {
            lblErrBatDau.setText("Vui lòng chọn ngày bắt đầu");
            lblErrBatDau.setVisible(true);
            return;
        }
        if (dateKetThuc == null) {
            lblErrKetThuc.setText("Vui lòng chọn ngày kết thúc");
            lblErrKetThuc.setVisible(true);
            return;
        }

        LocalDateTime batDau  = LocalDateTime.of(dateBatDau,  readTime(spTimeBatDau));
        LocalDateTime ketThuc = LocalDateTime.of(dateKetThuc, readTime(spTimeKetThuc));

        if (!ketThuc.isAfter(batDau)) {
            lblErrKetThuc.setText("Thời gian kết thúc phải sau thời gian bắt đầu");
            lblErrKetThuc.setVisible(true);
            return;
        }

        boolean trangThai = cboTrangThai.getSelectedIndex() == 0;
        KhuyenMai km = new KhuyenMai(maKM, tenKM, batDau, ketThuc,
                moTa.isEmpty() ? null : moTa, trangThai);

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() { return new DAO_KhuyenMai().insert(km); }
            @Override protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    if (get()) {
                        saved = true;
                        if (onSaved != null) onSaved.run();
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(ThemKhuyenMaiDialog.this,
                                "Không thể lưu! Mã khuyến mãi có thể đã tồn tại.",
                                "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ThemKhuyenMaiDialog.this,
                            "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    public boolean isSaved() { return saved; }

    // ───────────────────────── date-time helpers ─────────────────────────

    private JSpinner createTimeSpinner() {
        SpinnerDateModel m = new SpinnerDateModel();
        JSpinner sp = new JSpinner(m);
        sp.setEditor(new JSpinner.DateEditor(sp, "HH:mm"));
        sp.setFont(FONT_INPUT);
        sp.setPreferredSize(new Dimension(72, 40));
        sp.setMaximumSize(new Dimension(72, 40));
        return sp;
    }

    private JPanel buildDateTimePanel(DatePickerField dp, JSpinner timeSp) {
        JPanel p = new JPanel(new BorderLayout(6, 0));
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(0, 40));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        p.add(dp,    BorderLayout.CENTER);
        p.add(timeSp, BorderLayout.EAST);
        return p;
    }

    private LocalTime readTime(JSpinner sp) {
        Calendar cal = Calendar.getInstance();
        cal.setTime((java.util.Date) sp.getValue());
        return LocalTime.of(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
    }

    // ───────────────────────── error helpers ─────────────────────────

    private void showFieldError(JComponent field, JLabel errLabel, String msg) {
        errLabel.setText(msg);
        errLabel.setVisible(true);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ERROR, 2, true),
                new EmptyBorder(7, 11, 7, 11)));
        field.requestFocusInWindow();
    }

    private void clearAllErrors() {
        for (JLabel lbl : new JLabel[]{lblErrMaKM, lblErrTenKM, lblErrBatDau, lblErrKetThuc}) {
            lbl.setText(""); lbl.setVisible(false);
        }
        for (JTextField f : new JTextField[]{txtMaKM, txtTenKM}) {
            f.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(OUTLINE, 1, true),
                    new EmptyBorder(8, 12, 8, 12)));
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

    private JTextField createInputField(String placeholder) {
        JTextField f = new JTextField();
        f.setFont(FONT_INPUT);
        f.setForeground(ON_SURF_VAR);
        f.setPreferredSize(new Dimension(0, 36));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1, true),
                new EmptyBorder(8, 12, 8, 12)));
        f.setText(placeholder);
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (f.getText().equals(placeholder)) { f.setText(""); f.setForeground(ON_SURFACE); }
            }
            @Override public void focusLost(FocusEvent e) {
                if (f.getText().trim().isEmpty()) { f.setForeground(ON_SURF_VAR); f.setText(placeholder); }
            }
        });
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(PRIMARY, 2, true),
                        new EmptyBorder(7, 11, 7, 11)));
            }
            @Override public void focusLost(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(OUTLINE, 1, true),
                        new EmptyBorder(8, 12, 8, 12)));
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
                g2.setColor(getModel().isRollover() ? PRIMARY_HOVER : PRIMARY);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BTN); btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(170, 40));
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
        btn.setFont(FONT_BTN); btn.setForeground(ON_SURF_VAR);
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(100, 40));
        return btn;
    }
}
