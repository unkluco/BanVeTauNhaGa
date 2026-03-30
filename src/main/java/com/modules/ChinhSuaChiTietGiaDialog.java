package com.modules;

import com.dao.DAO_ChiTietGia;
import com.dao.DAO_Tuyen;
import com.entity.ChiTietGia;
import com.entity.Gia;
import com.entity.Tuyen;
import com.enums.LoaiGhe;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

public class ChinhSuaChiTietGiaDialog extends JDialog {

    // ===== Design tokens =====
    private static final Color PRIMARY      = new Color(0x00, 0x5D, 0x90);
    private static final Color PRIMARY_FIXED = new Color(0xCD, 0xE5, 0xFF);
    private static final Color CARD_BG      = Color.WHITE;
    private static final Color ON_SURFACE   = new Color(0x1A, 0x1D, 0x21);
    private static final Color ON_SURF_VAR  = new Color(0x5F, 0x67, 0x70);
    private static final Color OUTLINE      = new Color(0xDE, 0xE3, 0xE8);
    private static final Color INPUT_BG     = new Color(0xF1, 0xF5, 0xF9);
    private static final Color READONLY_BG  = new Color(0xE8, 0xED, 0xF2);
    private static final Color ERROR        = new Color(0xBA, 0x1A, 0x1A);
    private static final Color HEADER_BG    = new Color(0xF1, 0xF5, 0xF9);
    private static final Color FOOTER_BG    = new Color(0xF1, 0xF5, 0xF9);

    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font FONT_DESC  = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font FONT_INPUT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_MONO  = new Font("Consolas", Font.BOLD, 13);
    private static final Font FONT_BTN   = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_ERR   = new Font("Segoe UI", Font.PLAIN, 11);

    // ===== Data =====
    private final Gia        gia;
    private final ChiTietGia ctg;        // null = ADD mode
    private final Runnable   onSaved;
    private final boolean    isAddMode;
    private List<Tuyen>      tuyenList;

    // ===== Formatting =====
    private boolean isFormatting = false;

    // ===== Form fields =====
    private JTextField                     txtMaChiTiet;
    private SearchableComboBox<Tuyen>      searchTuyen;
    private JComboBox<String>              cboLoaiGhe;
    private JTextField                     txtGiaNiemYet;

    // ===== Error labels =====
    private JLabel lblErrTuyen;
    private JLabel lblErrGia;

    public ChinhSuaChiTietGiaDialog(Window owner, Gia gia, ChiTietGia ctg, Runnable onSaved) {
        super(owner, ctg == null ? "Th\u00EAm chi ti\u1EBFt gi\u00E1" : "Ch\u1EC9nh s\u1EEDa chi ti\u1EBFt gi\u00E1",
                ModalityType.APPLICATION_MODAL);
        this.gia       = gia;
        this.ctg       = ctg;
        this.onSaved   = onSaved;
        this.isAddMode = (ctg == null);
        setUndecorated(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        loadTuyenList();
        initUI();
        if (!isAddMode) populateFields();
        pack();
        setMinimumSize(new Dimension(560, getPreferredSize().height));
        setLocationRelativeTo(owner);
    }

    // =================================================================
    //  INIT
    // =================================================================

    private void loadTuyenList() {
        tuyenList = new DAO_Tuyen().getAll();
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
        // Pre-select tuyến
        if (ctg.getTuyen() != null) {
            String maTuyen = ctg.getTuyen().getMaTuyen();
            for (Tuyen t : tuyenList) {
                if (t.getMaTuyen().equals(maTuyen)) {
                    searchTuyen.selectItem(t);
                    break;
                }
            }
        }
        // Loại ghế
        if (ctg.getLoaiGhe() != null) {
            for (int i = 0; i < LoaiGhe.values().length; i++) {
                if (LoaiGhe.values()[i] == ctg.getLoaiGhe()) {
                    cboLoaiGhe.setSelectedIndex(i);
                    break;
                }
            }
        }
        // Giá
        String price = formatNumberWithDots(String.format("%.0f", ctg.getGiaNiemYet()));
        txtGiaNiemYet.setText(price);
        txtGiaNiemYet.setForeground(ON_SURFACE);
    }

    // =================================================================
    //  BUILD HEADER
    // =================================================================

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_BG);
        header.setBorder(new EmptyBorder(20, 28, 20, 28));

        JPanel iconTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        iconTitle.setOpaque(false);

        JPanel icon = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(PRIMARY_FIXED);
                g2.fillRoundRect(0, 0, 40, 40, 10, 10);
                g2.setColor(PRIMARY);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
                FontMetrics fm = g2.getFontMetrics();
                String s = isAddMode ? "+" : "\u270E";
                g2.drawString(s, (40 - fm.stringWidth(s)) / 2, 27);
                g2.dispose();
            }
        };
        icon.setOpaque(false);
        icon.setPreferredSize(new Dimension(40, 40));

        JPanel textArea = new JPanel();
        textArea.setLayout(new BoxLayout(textArea, BoxLayout.Y_AXIS));
        textArea.setOpaque(false);

        JLabel lblTitle = new JLabel(isAddMode
                ? "Th\u00EAm chi ti\u1EBFt bi\u1EC3u gi\u00E1"
                : "C\u1EADp nh\u1EADt chi ti\u1EBFt bi\u1EC3u gi\u00E1");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(ON_SURFACE);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblDesc = new JLabel(isAddMode
                ? "Th\u00EAm th\u00F4ng s\u1ED1 cho tuy\u1EBFn \u0111\u01B0\u1EE3ng v\u00E0 h\u1EA1ng gh\u1EBF"
                : "Ch\u1EC9nh s\u1EEDa th\u00F4ng s\u1ED1 cho tuy\u1EBFn \u0111\u01B0\u1EE3ng v\u00E0 h\u1EA1ng gh\u1EBF");
        lblDesc.setFont(FONT_DESC);
        lblDesc.setForeground(ON_SURF_VAR);
        lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        textArea.add(lblTitle);
        textArea.add(Box.createVerticalStrut(2));
        textArea.add(lblDesc);

        iconTitle.add(icon);
        iconTitle.add(textArea);

        JButton btnClose = new JButton("\u2715");
        btnClose.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btnClose.setForeground(ON_SURF_VAR);
        btnClose.setContentAreaFilled(false);
        btnClose.setBorderPainted(false);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose());

        header.add(iconTitle, BorderLayout.WEST);
        header.add(btnClose,  BorderLayout.EAST);
        return header;
    }

    // =================================================================
    //  BUILD FORM
    // =================================================================

    private JPanel buildForm() {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(CARD_BG);
        form.setBorder(new EmptyBorder(24, 28, 8, 28));

        // Mã chi tiết (readonly)
        String codeValue = isAddMode
                ? "CTG-" + gia.getMaGia() + "-" + String.format("%05d", (int)(System.currentTimeMillis() % 100000))
                : ctg.getMaChiTietGia();
        txtMaChiTiet = createReadonlyField(codeValue, FONT_MONO);
        form.add(buildFieldRow("M\u00C3 CHI TI\u1EBET (T\u1EF1 \u0111\u1ED9ng)", txtMaChiTiet, null));
        form.add(Box.createVerticalStrut(16));

        // TUYẾN — searchable (full width)
        searchTuyen = new SearchableComboBox<>(
            t -> {
                String gaDi  = t.getGaDi()  != null ? t.getGaDi().getTenGa()  : "?";
                String gaDen = t.getGaDen() != null ? t.getGaDen().getTenGa() : "?";
                return t.getMaTuyen() + "  |  " + gaDi + " \u2192 " + gaDen;
            },
            (t, q) -> {
                String gaDiName  = t.getGaDi()  != null ? t.getGaDi().getTenGa().toLowerCase()  : "";
                String gaDenName = t.getGaDen() != null ? t.getGaDen().getTenGa().toLowerCase() : "";
                return t.getMaTuyen().toLowerCase().contains(q)
                    || gaDiName.contains(q)
                    || gaDenName.contains(q);
            }
        );
        searchTuyen.setItems(tuyenList);
        searchTuyen.setPlaceholder("Nh\u1EADp m\u00E3 tuy\u1EBFn, ga \u0111i ho\u1EB7c ga \u0111\u1EBFn...");
        lblErrTuyen = createErrLabel();
        form.add(buildFieldRow("TUY\u1EBCN \u0110\u01AF\u1EDCNG", searchTuyen, lblErrTuyen));
        form.add(Box.createVerticalStrut(16));

        // Loại ghế (hàng riêng)
        cboLoaiGhe = createStyledCombo();
        for (LoaiGhe lg : LoaiGhe.values()) cboLoaiGhe.addItem(lg.toString());
        form.add(buildFieldRow("LO\u1EA0I GH\u1EBE", cboLoaiGhe, null));
        form.add(Box.createVerticalStrut(16));

        // Giá niêm yết
        txtGiaNiemYet = createInputField("VD: 250.000");
        lblErrGia = createErrLabel();
        txtGiaNiemYet.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { formatMoneyField(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { formatMoneyField(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });
        form.add(buildFieldRow("GI\u00C1 NI\u00CAM Y\u1EBET (VN\u0110)", txtGiaNiemYet, lblErrGia));
        form.add(Box.createVerticalStrut(8));

        return form;
    }

    private JPanel buildFieldRow(String labelText, JComponent input, JLabel errLabel) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, errLabel != null ? 80 : 68));

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(ON_SURF_VAR);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        input.setAlignmentX(Component.LEFT_ALIGNMENT);
        input.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        row.add(lbl);
        row.add(Box.createVerticalStrut(4));
        row.add(input);
        if (errLabel != null) {
            errLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            row.add(errLabel);
        }
        return row;
    }

    // =================================================================
    //  BUILD FOOTER
    // =================================================================

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 16));
        footer.setBackground(FOOTER_BG);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, OUTLINE));

        JButton btnCancel = new JButton("H\u1EE7y b\u1ECF");
        btnCancel.setFont(FONT_BTN);
        btnCancel.setForeground(ON_SURF_VAR);
        btnCancel.setContentAreaFilled(false);
        btnCancel.setBorderPainted(false);
        btnCancel.setFocusPainted(false);
        btnCancel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCancel.addActionListener(e -> dispose());

        String saveLabel = isAddMode ? "Th\u00EAm chi ti\u1EBFt" : "C\u1EADp nh\u1EADt chi ti\u1EBFt";
        JButton btnSave = new JButton(saveLabel) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isPressed() ? PRIMARY.darker()
                        : getModel().isRollover() ? new Color(0x00, 0x4A, 0x73) : PRIMARY;
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnSave.setFont(FONT_BTN);
        btnSave.setForeground(Color.WHITE);
        btnSave.setContentAreaFilled(false);
        btnSave.setBorderPainted(false);
        btnSave.setFocusPainted(false);
        btnSave.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSave.setPreferredSize(new Dimension(160, 40));
        btnSave.addActionListener(e -> doSave());

        footer.add(btnCancel);
        footer.add(btnSave);
        return footer;
    }

    // =================================================================
    //  SAVE LOGIC
    // =================================================================

    private void doSave() {
        clearErrors();

        // ── Validate tuyến ─────────────────────────────────────────────
        Tuyen selectedTuyen = searchTuyen.getSelectedItem();
        if (selectedTuyen == null) {
            showError(lblErrTuyen, "Vui l\u00F2ng ch\u1ECDn tuy\u1EBFn \u0111\u01B0\u1EE3ng");
            return;
        }

        // ── Validate giá ──────────────────────────────────────────────
        String giaStr = txtGiaNiemYet.getText().trim();
        if (giaStr.isEmpty()) {
            showError(lblErrGia, "Vui l\u00F2ng nh\u1EADp gi\u00E1 ni\u00EAm y\u1EBFt");
            return;
        }
        double giaNiemYet;
        try {
            long raw = Long.parseLong(giaStr.replaceAll("\\.", ""));
            if (raw <= 0) {
                showError(lblErrGia, "Gi\u00E1 ph\u1EA3i l\u1EDBn h\u01A1n 0");
                return;
            }
            if (raw < 1_000) {
                showError(lblErrGia, "Gi\u00E1 t\u1ED1i thi\u1EC3u l\u00E0 1.000 VN\u0110");
                return;
            }
            if (raw > 100_000_000) {
                showError(lblErrGia, "Gi\u00E1 kh\u00F4ng \u0111\u01B0\u1EE3c v\u01B0\u1EE3t qu\u00E1 100.000.000 VN\u0110");
                return;
            }
            giaNiemYet = (double) raw;
        } catch (NumberFormatException ex) {
            showError(lblErrGia, "Gi\u00E1 kh\u00F4ng h\u1EE3p l\u1EC7, ch\u1EC9 \u0111\u01B0\u1EE3c nh\u1EADp s\u1ED1");
            return;
        }

        LoaiGhe loaiGhe = LoaiGhe.values()[Math.max(0, cboLoaiGhe.getSelectedIndex())];

        // ── Persist ───────────────────────────────────────────────────
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                DAO_ChiTietGia dao = new DAO_ChiTietGia();
                if (isAddMode) {
                    ChiTietGia newCtg = new ChiTietGia(
                            txtMaChiTiet.getText().trim(), gia, selectedTuyen, loaiGhe, giaNiemYet);
                    return dao.insert(newCtg);
                } else {
                    ctg.setGia(gia);
                    ctg.setTuyen(selectedTuyen);
                    ctg.setLoaiGhe(loaiGhe);
                    ctg.setGiaNiemYet(giaNiemYet);
                    return dao.update(ctg);
                }
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    if (get()) {
                        if (onSaved != null) onSaved.run();
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(ChinhSuaChiTietGiaDialog.this,
                                isAddMode ? "Kh\u00F4ng th\u1EC3 th\u00EAm chi ti\u1EBFt gi\u00E1!"
                                          : "Kh\u00F4ng th\u1EC3 c\u1EADp nh\u1EADt chi ti\u1EBFt gi\u00E1!",
                                "L\u1ED7i", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ChinhSuaChiTietGiaDialog.this,
                            "L\u1ED7i: " + ex.getMessage(), "L\u1ED7i", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // =================================================================
    //  HELPERS
    // =================================================================

    // =================================================================
    //  MONEY FORMAT HELPERS
    // =================================================================

    /** Chèn dấu chấm mỗi 3 chữ số (định dạng VNĐ). */
    private String formatNumberWithDots(String digits) {
        if (digits == null || digits.isEmpty()) return "";
        StringBuilder sb = new StringBuilder(digits);
        for (int i = sb.length() - 3; i > 0; i -= 3) sb.insert(i, '.');
        return sb.toString();
    }

    /** Tự động format ô giá niêm yết khi người dùng gõ. */
    private void formatMoneyField() {
        if (isFormatting) return;
        isFormatting = true;
        SwingUtilities.invokeLater(() -> {
            try {
                String raw = txtGiaNiemYet.getText().replaceAll("[^0-9]", "");
                String formatted = formatNumberWithDots(raw);
                txtGiaNiemYet.setText(formatted);
                txtGiaNiemYet.setCaretPosition(formatted.length());
            } finally {
                isFormatting = false;
            }
        });
    }

    private void clearErrors() {
        lblErrTuyen.setText("");
        lblErrGia.setText("");
    }

    private void showError(JLabel lbl, String msg) {
        lbl.setText(msg);
        pack();
    }

    private JTextField createInputField(String placeholder) {
        JTextField f = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !hasFocus()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(new Color(0x9E, 0xA7, 0xB0));
                    g2.setFont(FONT_INPUT);
                    Insets ins = getInsets();
                    g2.drawString(placeholder, ins.left, getHeight() / 2 + 5);
                    g2.dispose();
                }
            }
        };
        f.setFont(FONT_INPUT);
        f.setBackground(Color.WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));
        f.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) { f.repaint(); }
            public void focusLost(java.awt.event.FocusEvent e)   { f.repaint(); }
        });
        return f;
    }

    private JTextField createReadonlyField(String value, Font font) {
        JTextField f = new JTextField(value);
        f.setFont(font);
        f.setEditable(false);
        f.setBackground(READONLY_BG);
        f.setForeground(ON_SURF_VAR);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));
        return f;
    }

    private JComboBox<String> createStyledCombo() {
        JComboBox<String> cbo = new JComboBox<>();
        cbo.setFont(FONT_INPUT);
        cbo.setBackground(Color.WHITE);
        cbo.setPreferredSize(new Dimension(200, 42));
        cbo.setMinimumSize(new Dimension(50, 42));
        return cbo;
    }

    private JLabel createErrLabel() {
        JLabel lbl = new JLabel("");
        lbl.setFont(FONT_ERR);
        lbl.setForeground(ERROR);
        return lbl;
    }
}
