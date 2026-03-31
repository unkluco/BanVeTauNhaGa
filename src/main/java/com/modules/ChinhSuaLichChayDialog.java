package com.modules;

import com.dao.DAO_DoanTau;
import com.dao.DAO_Lich;
import com.dao.DAO_Tuyen;
import com.entity.DoanTau;
import com.entity.Lich;
import com.entity.Tuyen;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class ChinhSuaLichChayDialog extends JDialog {

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

    private static final DateTimeFormatter FMT_IN  = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // === Mode ===
    private final boolean isEditMode;
    private final Lich    original;
    private final Runnable onSaved;

    // === DAOs ===
    private final DAO_Lich    daoLich   = new DAO_Lich();
    private final DAO_Tuyen   daoTuyen  = new DAO_Tuyen();
    private final DAO_DoanTau daoDoanTau = new DAO_DoanTau();

    // === Form fields ===
    private JTextField         txtMaLich;
    private JComboBox<Tuyen>   cboTuyen;
    private JComboBox<DoanTau> cboDoanTau;
    private JTextField         txtThoiGianBatDau;
    private JTextField         txtThoiGianChay;

    // === Error labels ===
    private JLabel lblErrMaLich;
    private JLabel lblErrTuyen;
    private JLabel lblErrDoanTau;
    private JLabel lblErrBatDau;
    private JLabel lblErrChay;

    // =====================================================================

    public ChinhSuaLichChayDialog(Window owner, Lich lich, Runnable onSaved) {
        super(owner, lich == null ? "Th\u00EAm l\u1ECBch ch\u1EA1y" : "Ch\u1EC9nh s\u1EEDa l\u1ECBch ch\u1EA1y",
                ModalityType.APPLICATION_MODAL);
        this.original    = lich;
        this.isEditMode  = (lich != null);
        this.onSaved     = onSaved;

        setUndecorated(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        initUI();
        pack();
        setMinimumSize(new Dimension(680, getPreferredSize().height));
        setLocationRelativeTo(owner);
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CARD_BG);
        root.setBorder(BorderFactory.createLineBorder(OUTLINE, 1));

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildForm(),   BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);

        setContentPane(buildShadowWrapper(root));
    }

    // ========================= SHADOW =========================

    private JPanel buildShadowWrapper(JPanel inner) {
        JPanel wrapper = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int pad = 8;
                for (int i = pad; i > 0; i--) {
                    float alpha = 0.04f * (pad - i + 1);
                    g2.setColor(new Color(0, 0, 0, (int)(alpha * 255)));
                    g2.fill(new RoundRectangle2D.Float(i, i, getWidth()-2*i, getHeight()-2*i, 12, 12));
                }
                g2.dispose();
            }
        };
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(8, 8, 8, 8));
        wrapper.add(inner, BorderLayout.CENTER);
        return wrapper;
    }

    // ========================= HEADER =========================

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, OUTLINE),
                new EmptyBorder(20, 28, 16, 28)));

        JPanel left = new JPanel(new BorderLayout(14, 0));
        left.setOpaque(false);

        // Icon
        ImageIcon ico = loadScaledIcon("bieuTuongLich.png", 36);
        if (ico != null) {
            JLabel lblIco = new JLabel(ico);
            lblIco.setVerticalAlignment(SwingConstants.TOP);
            left.add(lblIco, BorderLayout.WEST);
        }

        JPanel textPart = new JPanel();
        textPart.setLayout(new BoxLayout(textPart, BoxLayout.Y_AXIS));
        textPart.setOpaque(false);

        String titleStr = isEditMode
                ? "\u270E  Ch\u1EC9nh s\u1EEDa l\u1ECBch ch\u1EA1y"
                : "\u2795  Th\u00EAm l\u1ECBch ch\u1EA1y m\u1EDBi";
        JLabel lblTitle = new JLabel(titleStr);
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(PRIMARY);

        String descStr = isEditMode
                ? "C\u1EADp nh\u1EADt th\u00F4ng tin l\u1ECBch ch\u1EA1y " + original.getMaLich() + "."
                : "Nh\u1EADp th\u00F4ng tin \u0111\u1EC3 t\u1EA1o l\u1ECBch ch\u1EA1y m\u1EDBi.";
        JLabel lblDesc = new JLabel(descStr);
        lblDesc.setFont(FONT_DESC);
        lblDesc.setForeground(ON_SURF_VAR);

        textPart.add(lblTitle);
        textPart.add(Box.createVerticalStrut(4));
        textPart.add(lblDesc);
        left.add(textPart, BorderLayout.CENTER);

        // Close button
        JButton btnClose = new JButton("\u2715");
        btnClose.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btnClose.setForeground(ON_SURF_VAR);
        btnClose.setContentAreaFilled(false);
        btnClose.setBorderPainted(false);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose());

        header.add(left, BorderLayout.CENTER);
        header.add(btnClose, BorderLayout.EAST);
        return header;
    }

    // ========================= FORM =========================

    private JPanel buildForm() {
        // Load combo data
        List<Tuyen>   dsTuyen   = daoTuyen.getAll();
        List<DoanTau> dsDoanTau = daoDoanTau.getAll();

        cboTuyen = new JComboBox<>();
        cboTuyen.setFont(FONT_INPUT); cboTuyen.setBackground(CARD_BG);
        cboTuyen.setPreferredSize(new Dimension(0, 36));
        cboTuyen.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cboTuyen.setRenderer(buildTuyenRenderer());
        for (Tuyen t : dsTuyen) cboTuyen.addItem(t);

        cboDoanTau = new JComboBox<>();
        cboDoanTau.setFont(FONT_INPUT); cboDoanTau.setBackground(CARD_BG);
        cboDoanTau.setPreferredSize(new Dimension(0, 36));
        cboDoanTau.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cboDoanTau.setRenderer(buildDoanTauRenderer());
        for (DoanTau d : dsDoanTau) cboDoanTau.addItem(d);

        // Pre-fill if editing
        if (isEditMode) {
            if (original.getTuyen() != null) {
                for (int i = 0; i < cboTuyen.getItemCount(); i++) {
                    if (cboTuyen.getItemAt(i).getMaTuyen().equals(original.getTuyen().getMaTuyen())) {
                        cboTuyen.setSelectedIndex(i); break;
                    }
                }
            }
            if (original.getDoanTau() != null) {
                for (int i = 0; i < cboDoanTau.getItemCount(); i++) {
                    if (cboDoanTau.getItemAt(i).getMaDoanTau().equals(original.getDoanTau().getMaDoanTau())) {
                        cboDoanTau.setSelectedIndex(i); break;
                    }
                }
            }
        }

        // Build form
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(CARD_BG);
        form.setBorder(new EmptyBorder(24, 28, 16, 28));

        // Row 1: Mã lịch (full width)
        txtMaLich = createTextField();
        txtMaLich.setFont(FONT_MONO);
        lblErrMaLich = createErrorLabel();
        if (isEditMode) {
            txtMaLich.setText(original.getMaLich());
            txtMaLich.setEditable(false);
            txtMaLich.setBackground(new Color(0xF8, 0xFA, 0xFC));
            txtMaLich.setForeground(PRIMARY);
        }
        JPanel row1 = buildRow(1, 1, Integer.MAX_VALUE, 80);
        row1.add(buildFieldGroup("M\u00E3 l\u1ECBch ch\u1EA1y", txtMaLich,
                isEditMode ? "* Kh\u00F4ng th\u1EC3 thay \u0111\u1ED5i" : null, !isEditMode, lblErrMaLich));
        form.add(row1); form.add(Box.createVerticalStrut(12));

        // Row 2: Tuyến | Đoàn tàu
        lblErrTuyen   = createErrorLabel();
        lblErrDoanTau = createErrorLabel();
        JPanel row2 = buildRow(1, 2, Integer.MAX_VALUE, 90);
        row2.add(buildFieldGroup("Tuy\u1EBFn \u0111\u01B0\u1EDDng", cboTuyen, null, true, lblErrTuyen));
        row2.add(buildFieldGroup("\u0110o\u00E0n t\u00E0u", cboDoanTau, null, true, lblErrDoanTau));
        form.add(row2); form.add(Box.createVerticalStrut(12));

        // Row 3: Thời gian bắt đầu | Thời gian chạy
        txtThoiGianBatDau = createTextField();
        txtThoiGianBatDau.setPreferredSize(new Dimension(0, 36));
        txtThoiGianBatDau.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        lblErrBatDau = createErrorLabel();

        txtThoiGianChay = createTextField();
        txtThoiGianChay.setPreferredSize(new Dimension(0, 36));
        txtThoiGianChay.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        lblErrChay = createErrorLabel();

        if (isEditMode) {
            if (original.getThoiGianBatDau() != null)
                txtThoiGianBatDau.setText(original.getThoiGianBatDau().format(FMT_IN));
            if (original.getThoiGianChay() != null)
                txtThoiGianChay.setText(original.getThoiGianChay());
        }

        JPanel row3 = buildRow(1, 2, Integer.MAX_VALUE, 90);
        row3.add(buildFieldGroup("Th\u1EDDi gian b\u1EAFt \u0111\u1EA7u",
                txtThoiGianBatDau, "Nh\u1EADp theo d\u1EA1ng dd/MM/yyyy HH:mm", true, lblErrBatDau));
        row3.add(buildFieldGroup("Th\u1EDDi gian ch\u1EA1y (ph\u00FAt)",
                txtThoiGianChay, "S\u1ED1 ph\u00FAt to\u00E0n h\u00E0nh tr\u00ECnh", true, lblErrChay));
        form.add(row3);

        return form;
    }

    private JPanel buildRow(int rows, int cols, int maxW, int maxH) {
        JPanel row = new JPanel(new GridLayout(rows, cols, 16, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(maxW, maxH));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        return row;
    }

    // ========================= FOOTER =========================

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        footer.setBackground(FOOTER_BG);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, OUTLINE),
                new EmptyBorder(16, 28, 16, 28)));

        JButton btnCancel = createOutlineButton("H\u1EE7y b\u1ECF");
        ImageIcon icoHuy = loadScaledIcon("nutThoat.png", 15);
        if (icoHuy != null) { btnCancel.setIcon(icoHuy); btnCancel.setIconTextGap(6); }
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = createPrimaryButton(isEditMode ? "L\u01B0u thay \u0111\u1ED5i" : "T\u1EA1o l\u1ECBch");
        ImageIcon icoLuu = loadScaledIcon("nutLuu.png", 15);
        if (icoLuu != null) { btnSave.setIcon(icoLuu); btnSave.setIconTextGap(6); }
        btnSave.addActionListener(e -> doSave());

        footer.add(btnCancel);
        footer.add(btnSave);
        return footer;
    }

    // ========================= SAVE LOGIC =========================

    private void doSave() {
        clearAllErrors();

        String maLich = txtMaLich.getText().trim();
        String batDauStr = txtThoiGianBatDau.getText().trim();
        String chayStr   = txtThoiGianChay.getText().trim();

        // Validate
        if (!isEditMode && maLich.isEmpty()) {
            showFieldError(txtMaLich, lblErrMaLich, "Vui l\u00F2ng nh\u1EADp m\u00E3 l\u1ECBch"); return;
        }
        if (cboTuyen.getSelectedItem() == null) {
            lblErrTuyen.setText("Vui l\u00F2ng ch\u1ECDn tuy\u1EBFn"); lblErrTuyen.setVisible(true); return;
        }
        if (cboDoanTau.getSelectedItem() == null) {
            lblErrDoanTau.setText("Vui l\u00F2ng ch\u1ECDn \u0111o\u00E0n t\u00E0u"); lblErrDoanTau.setVisible(true); return;
        }
        if (batDauStr.isEmpty()) {
            showFieldError(txtThoiGianBatDau, lblErrBatDau, "Vui l\u00F2ng nh\u1EADp th\u1EDDi gian b\u1EAFt \u0111\u1EA7u"); return;
        }

        LocalDateTime batDau;
        try {
            batDau = LocalDateTime.parse(batDauStr, FMT_IN);
        } catch (DateTimeParseException ex) {
            showFieldError(txtThoiGianBatDau, lblErrBatDau, "Sai \u0111\u1ECBnh d\u1EA1ng (dd/MM/yyyy HH:mm)"); return;
        }

        if (chayStr.isEmpty()) {
            showFieldError(txtThoiGianChay, lblErrChay, "Vui l\u00F2ng nh\u1EADp th\u1EDDi gian ch\u1EA1y"); return;
        }
        try {
            int mins = Integer.parseInt(chayStr);
            if (mins <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            showFieldError(txtThoiGianChay, lblErrChay, "Vui l\u00F2ng nh\u1EADp s\u1ED1 ph\u00FAt h\u1EE3p l\u1EC7 (> 0)"); return;
        }

        Tuyen   tuyen   = (Tuyen)   cboTuyen.getSelectedItem();
        DoanTau doanTau = (DoanTau) cboDoanTau.getSelectedItem();
        Lich lich = new Lich(isEditMode ? original.getMaLich() : maLich,
                tuyen, doanTau, batDau, chayStr);

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() {
                return isEditMode ? daoLich.update(lich) : daoLich.insert(lich);
            }
            @Override protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    if (get()) {
                        if (onSaved != null) onSaved.run();
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(ChinhSuaLichChayDialog.this,
                                "Kh\u00F4ng th\u1EC3 l\u01B0u l\u1ECBch. Ki\u1EC3m tra m\u00E3 c\u00F3 b\u1ECB tr\u00F9ng kh\u00F4ng.",
                                "L\u1ED7i", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ChinhSuaLichChayDialog.this,
                            "L\u1ED7i: " + ex.getMessage(), "L\u1ED7i", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // ========================= COMBO RENDERERS =========================

    private DefaultListCellRenderer buildTuyenRenderer() {
        return new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object val,
                    int idx, boolean sel, boolean focus) {
                super.getListCellRendererComponent(list, val, idx, sel, focus);
                if (val instanceof Tuyen t) {
                    String gaDi  = t.getGaDi()  != null ? t.getGaDi().getTenGa()  : t.getMaTuyen();
                    String gaDen = t.getGaDen() != null ? t.getGaDen().getTenGa() : "";
                    setText(t.getMaTuyen() + "  \u2014  " + gaDi
                            + (gaDen.isEmpty() ? "" : " \u2192 " + gaDen));
                }
                return this;
            }
        };
    }

    private DefaultListCellRenderer buildDoanTauRenderer() {
        return new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object val,
                    int idx, boolean sel, boolean focus) {
                super.getListCellRendererComponent(list, val, idx, sel, focus);
                if (val instanceof DoanTau d) {
                    String ten = d.getTenDoanTau() != null ? d.getTenDoanTau() : "";
                    setText(d.getMaDoanTau() + (ten.isEmpty() ? "" : "  \u2014  " + ten));
                }
                return this;
            }
        };
    }

    // ========================= INLINE ERROR =========================

    private void showFieldError(JComponent field, JLabel errLbl, String msg) {
        errLbl.setText(msg); errLbl.setVisible(true);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ERROR, 2, true),
                new EmptyBorder(7, 11, 7, 11)));
        field.requestFocusInWindow();
    }

    private void clearAllErrors() {
        JLabel[]     errs = {lblErrMaLich, lblErrTuyen, lblErrDoanTau, lblErrBatDau, lblErrChay};
        JComponent[] flds = {txtMaLich, cboTuyen, cboDoanTau, txtThoiGianBatDau, txtThoiGianChay};
        for (int i = 0; i < errs.length; i++) {
            if (errs[i] != null) { errs[i].setText(""); errs[i].setVisible(false); }
            if (flds[i] instanceof JTextField tf && tf.isEditable()) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(OUTLINE, 1, true), new EmptyBorder(8, 12, 8, 12)));
            }
        }
    }

    // ========================= UI HELPERS =========================

    private JPanel buildFieldGroup(String label, JComponent input, String hint,
                                   boolean required, JLabel errLabel) {
        JPanel group = new JPanel();
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
        group.setOpaque(false);

        // Label row
        JPanel lblRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        lblRow.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_LABEL); lbl.setForeground(ON_SURF_VAR);
        lblRow.add(lbl);
        if (required) {
            JLabel star = new JLabel(" *");
            star.setFont(FONT_LABEL); star.setForeground(ERROR);
            lblRow.add(star);
        }
        lblRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        group.add(lblRow);
        group.add(Box.createVerticalStrut(6));

        input.setAlignmentX(Component.LEFT_ALIGNMENT);
        input.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        group.add(input);

        if (hint != null) {
            group.add(Box.createVerticalStrut(3));
            JLabel lblHint = new JLabel(hint);
            lblHint.setFont(FONT_HINT); lblHint.setForeground(ON_SURF_VAR);
            lblHint.setAlignmentX(Component.LEFT_ALIGNMENT);
            group.add(lblHint);
        }
        if (errLabel != null) {
            group.add(Box.createVerticalStrut(3));
            group.add(errLabel);
        }
        return group;
    }

    private JLabel createErrorLabel() {
        JLabel lbl = new JLabel();
        lbl.setFont(FONT_ERR); lbl.setForeground(ERROR);
        lbl.setVisible(false); lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JTextField createTextField() {
        JTextField f = new JTextField();
        f.setFont(FONT_INPUT); f.setForeground(ON_SURFACE);
        f.setPreferredSize(new Dimension(0, 36));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1, true), new EmptyBorder(8, 12, 8, 12)));
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(PRIMARY, 2, true), new EmptyBorder(7, 11, 7, 11)));
            }
            @Override public void focusLost(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(OUTLINE, 1, true), new EmptyBorder(8, 12, 8, 12)));
            }
        });
        return f;
    }

    private JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? PRIMARY_HOVER : PRIMARY);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose(); super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BTN); btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false); btn.setBorderPainted(false);
        btn.setFocusPainted(false); btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
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
                g2.dispose(); super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BTN); btn.setForeground(ON_SURF_VAR);
        btn.setContentAreaFilled(false); btn.setBorderPainted(false);
        btn.setFocusPainted(false); btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(120, 40));
        return btn;
    }

    private ImageIcon loadScaledIcon(String name, int size) {
        try {
            java.net.URL url = getClass().getResource("/icons/" + name);
            if (url != null) return new ImageIcon(new ImageIcon(url).getImage()
                    .getScaledInstance(size, size, Image.SCALE_SMOOTH));
        } catch (Exception ignored) {}
        return null;
    }
}
