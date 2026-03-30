package com.modules;

import com.dao.DAO_Gia;
import com.entity.Gia;
import com.toedter.calendar.JCalendar;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Date;

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
    private static final Font FONT_HINT   = new Font("Segoe UI", Font.ITALIC, 10);
    private static final Font FONT_ERR    = new Font("Segoe UI", Font.PLAIN, 11);

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // === Form fields ===
    private JTextField        txtMaGia;
    private JTextField        txtMoTa;
    private JTextField        txtThoiGianBatDau;
    private JTextField        txtThoiGianKetThuc;
    private JComboBox<String> cboTrangThai;

    // === Error labels ===
    private JLabel lblErrMoTa;
    private JLabel lblErrBatDau;
    private JLabel lblErrKetThuc;

    private final Gia gia;
    private boolean saved = false;
    private Runnable onSaved;

    public SuaGiaDialog(Window owner, Gia gia, Runnable onSaved) {
        super(owner, "Ch\u1EC9nh s\u1EEDa k\u1EF3 gi\u00E1", ModalityType.APPLICATION_MODAL);
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
            txtThoiGianBatDau.setText(gia.getThoiGianBatDau().format(DT_FMT));
            txtThoiGianBatDau.setForeground(ON_SURFACE);
        }
        if (gia.getThoiGianKetThuc() != null) {
            txtThoiGianKetThuc.setText(gia.getThoiGianKetThuc().format(DT_FMT));
            txtThoiGianKetThuc.setForeground(ON_SURFACE);
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

        JLabel lblTitle = new JLabel("Ch\u1EC9nh s\u1EEDa k\u1EF3 gi\u00E1");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(PRIMARY);

        JLabel lblDesc = new JLabel("C\u1EADp nh\u1EADt th\u00F4ng tin k\u1EF3 gi\u00E1: " + gia.getMaGia());
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
        row1.add(buildFieldGroup("M\u00E3 gi\u00E1", txtMaGia, "* M\u00E3 gi\u00E1 kh\u00F4ng th\u1EC3 thay \u0111\u1ED5i", false, null));

        cboTrangThai = new JComboBox<>(new String[]{"\u0110ang \u00E1p d\u1EE5ng", "Ng\u1EEBng \u00E1p d\u1EE5ng"});
        cboTrangThai.setFont(FONT_INPUT);
        cboTrangThai.setBackground(CARD_BG);
        cboTrangThai.setPreferredSize(new Dimension(0, 36));
        cboTrangThai.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        row1.add(buildFieldGroup("Tr\u1EA1ng th\u00E1i", cboTrangThai, null, false, null));

        form.add(row1);
        form.add(Box.createVerticalStrut(10));

        // Row 2: Mo ta (full width)
        JPanel row2 = new JPanel(new GridLayout(1, 1, 0, 0));
        row2.setOpaque(false);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        txtMoTa = createInputField("VD: H\u00E0 N\u1ED9i - S\u00E0i G\u00F2n Economy");
        lblErrMoTa = createErrorLabel();
        row2.add(buildFieldGroup("M\u00F4 t\u1EA3 / T\u00EAn k\u1EF3 gi\u00E1", txtMoTa, null, true, lblErrMoTa));

        form.add(row2);
        form.add(Box.createVerticalStrut(10));

        // Row 3: Thoi gian bat dau | Thoi gian ket thuc
        JPanel row3 = new JPanel(new GridLayout(1, 2, 16, 0));
        row3.setOpaque(false);
        row3.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        txtThoiGianBatDau = createInputField("dd/MM/yyyy");
        lblErrBatDau = createErrorLabel();
        row3.add(buildFieldGroup("Ng\u00E0y \u00E1p d\u1EE5ng", createDatePanel(txtThoiGianBatDau), "* \u0110\u1ECBnh d\u1EA1ng: dd/MM/yyyy", true, lblErrBatDau));

        txtThoiGianKetThuc = createInputField("dd/MM/yyyy");
        lblErrKetThuc = createErrorLabel();
        row3.add(buildFieldGroup("Ng\u00E0y k\u1EBFt th\u00FAc", createDatePanel(txtThoiGianKetThuc), "* \u0110\u1ECBnh d\u1EA1ng: dd/MM/yyyy", true, lblErrKetThuc));

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

        JButton btnCancel = createOutlineButton("H\u1EE7y b\u1ECF");
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = createPrimaryButton("C\u1EADp nh\u1EADt");
        btnSave.addActionListener(e -> doSave());

        footer.add(btnCancel);
        footer.add(btnSave);
        return footer;
    }

    // ========================= SAVE LOGIC =========================
    private void doSave() {
        clearAllErrors();

        String moTa = getFieldText(txtMoTa, "VD: H\u00E0 N\u1ED9i - S\u00E0i G\u00F2n Economy");
        String batDauStr = getFieldText(txtThoiGianBatDau, "dd/MM/yyyy");
        String ketThucStr = getFieldText(txtThoiGianKetThuc, "dd/MM/yyyy");

        if (moTa.isEmpty()) {
            showFieldError(txtMoTa, lblErrMoTa, "Vui l\u00F2ng nh\u1EADp m\u00F4 t\u1EA3");
            return;
        }
        if (batDauStr.isEmpty()) {
            showFieldError(txtThoiGianBatDau, lblErrBatDau, "Vui l\u00F2ng nh\u1EADp th\u1EDDi gian \u00E1p d\u1EE5ng");
            return;
        }
        if (ketThucStr.isEmpty()) {
            showFieldError(txtThoiGianKetThuc, lblErrKetThuc, "Vui l\u00F2ng nh\u1EADp th\u1EDDi gian k\u1EBFt th\u00FAc");
            return;
        }

        LocalDate batDau;
        try {
            batDau = LocalDate.parse(batDauStr, DT_FMT);
        } catch (DateTimeParseException ex) {
            showFieldError(txtThoiGianBatDau, lblErrBatDau, "Sai định dạng (dd/MM/yyyy)");
            return;
        }

        LocalDate ketThuc;
        try {
            ketThuc = LocalDate.parse(ketThucStr, DT_FMT);
        } catch (DateTimeParseException ex) {
            showFieldError(txtThoiGianKetThuc, lblErrKetThuc, "Sai định dạng (dd/MM/yyyy)");
            return;
        }

        if (!ketThuc.isAfter(batDau)) {
            showFieldError(txtThoiGianKetThuc, lblErrKetThuc, "Th\u1EDDi gian k\u1EBFt th\u00FAc ph\u1EA3i sau th\u1EDDi gian b\u1EAFt \u0111\u1EA7u");
            return;
        }

        boolean trangThai = cboTrangThai.getSelectedIndex() == 0;

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
                                "Kh\u00F4ng th\u1EC3 c\u1EADp nh\u1EADt k\u1EF3 gi\u00E1!",
                                "L\u1ED7i", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(SuaGiaDialog.this,
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
        JLabel[] errs     = {lblErrMoTa, lblErrBatDau, lblErrKetThuc};
        JComponent[] flds = {txtMoTa, txtThoiGianBatDau, txtThoiGianKetThuc};
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

    // ========================= DATE PANEL =========================

    /** Wraps a date text field + calendar popup button into one panel. */
    private JPanel createDatePanel(JTextField txtField) {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(0, 36));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JButton btnCal = new JButton("\u25BC") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isRollover() ? new Color(0xE3, 0xF2, 0xFD) : new Color(0xF1, 0xF5, 0xF9);
                g2.setColor(bg);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(OUTLINE);
                g2.drawLine(0, 0, 0, getHeight() - 1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnCal.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        btnCal.setForeground(ON_SURF_VAR);
        btnCal.setContentAreaFilled(false);
        btnCal.setBorderPainted(false);
        btnCal.setFocusPainted(false);
        btnCal.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCal.setPreferredSize(new Dimension(30, 36));
        btnCal.addActionListener(e -> showDatePickerPopup(btnCal, txtField));

        panel.add(txtField, BorderLayout.CENTER);
        panel.add(btnCal, BorderLayout.EAST);
        return panel;
    }

    /** Opens a modeless calendar popup near the anchor; click outside to dismiss without change. */
    private void showDatePickerPopup(Component anchor, JTextField target) {
        JDialog popup = new JDialog(this, Dialog.ModalityType.MODELESS);
        popup.setUndecorated(true);
        popup.addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            @Override public void windowGainedFocus(java.awt.event.WindowEvent e) {}
            @Override public void windowLostFocus(java.awt.event.WindowEvent e)   { popup.dispose(); }
        });

        // Pre-select existing date
        String existing = target.getText().trim();
        Calendar cal = Calendar.getInstance();
        try {
            if (!existing.isEmpty() && !existing.equals("dd/MM/yyyy")) {
                LocalDate ld = LocalDate.parse(existing, DT_FMT);
                cal.set(ld.getYear(), ld.getMonthValue() - 1, ld.getDayOfMonth());
            }
        } catch (DateTimeParseException ignored) {}

        JCalendar jCal = new JCalendar();
        jCal.setDate(cal.getTime());
        jCal.setWeekOfYearVisible(false);

        JButton btnOk = new JButton("Ch\u1ECDn") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? PRIMARY_HOVER : PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnOk.setFont(FONT_BTN); btnOk.setForeground(Color.WHITE);
        btnOk.setContentAreaFilled(false); btnOk.setBorderPainted(false); btnOk.setFocusPainted(false);
        btnOk.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnOk.setPreferredSize(new Dimension(80, 32));

        JButton btnClear = new JButton("X\u00F3a");
        btnClear.setFont(FONT_BTN); btnClear.setForeground(new Color(0xB9, 0x1C, 0x1C));
        btnClear.setContentAreaFilled(false); btnClear.setBorderPainted(false); btnClear.setFocusPainted(false);
        btnClear.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClear.setPreferredSize(new Dimension(60, 32));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        btnRow.setBackground(Color.WHITE);
        btnRow.add(btnClear); btnRow.add(btnOk);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xC8, 0xD0, 0xDA), 1),
                new EmptyBorder(4, 4, 0, 4)
        ));
        root.add(jCal, BorderLayout.CENTER);
        root.add(btnRow, BorderLayout.SOUTH);
        popup.setContentPane(root);

        btnOk.addActionListener(e -> {
            Date d = jCal.getDate();
            Calendar c = Calendar.getInstance(); c.setTime(d);
            LocalDate ld = LocalDate.of(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
            target.setText(ld.format(DT_FMT));
            target.setForeground(ON_SURFACE);
            popup.dispose();
        });
        btnClear.addActionListener(e -> { target.setText(""); popup.dispose(); });

        popup.pack();
        try {
            Point loc = anchor.getLocationOnScreen();
            int px = loc.x, py = loc.y + anchor.getHeight() + 2;
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            if (px + popup.getWidth()  > screen.width)  px = screen.width  - popup.getWidth();
            if (py + popup.getHeight() > screen.height) py = loc.y - popup.getHeight() - 2;
            popup.setLocation(px, py);
        } catch (Exception ex) { popup.setLocationRelativeTo(this); }
        popup.setVisible(true);
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
