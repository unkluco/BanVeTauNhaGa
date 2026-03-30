package com.modules;

import com.dao.DAO_Ga;
import com.dao.DAO_Tuyen;
import com.entity.Ga;
import com.entity.Tuyen;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.Insets;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.function.Consumer;

/**
 * Undecorated dialog for adding or editing a Tuyen (Route).
 * - Add mode: maTuyen auto-generated as "TUY-XXXXX", readonly.
 * - Edit mode: maTuyen fixed, readonly.
 * - Ga đi / Ga đến: SearchableComboBox<Ga>.
 * - Validation: both fields required, ga đi ≠ ga đến, duplicate check in add mode.
 */
public class ChinhSuaTuyenDialog extends JDialog {

    // ── Design tokens ───────────────────────────────────────────────────
    private static final Color PRIMARY       = new Color(0x00, 0x5D, 0x90);
    private static final Color PRIMARY_HOVER = new Color(0x00, 0x4A, 0x73);
    private static final Color PRIMARY_LIGHT = new Color(0xE3, 0xF2, 0xFD);
    private static final Color SURFACE       = new Color(0xF8, 0xFA, 0xFC);
    private static final Color CARD_BG       = Color.WHITE;
    private static final Color ON_SURFACE    = new Color(0x1A, 0x1D, 0x21);
    private static final Color ON_SURF_VAR   = new Color(0x5F, 0x67, 0x70);
    private static final Color OUTLINE       = new Color(0xDE, 0xE3, 0xE8);
    private static final Color ERROR         = new Color(0xBA, 0x1A, 0x1A);
    private static final Color ERROR_BG      = new Color(0xFF, 0xDA, 0xD6);
    private static final Color READONLY_BG   = new Color(0xF1, 0xF5, 0xF9);
    private static final Color HEADER_BG     = new Color(0xF1, 0xF5, 0xF9);
    private static final Color FOOTER_BG     = new Color(0xF1, 0xF5, 0xF9);

    private static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font FONT_DESC   = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_LABEL  = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font FONT_INPUT  = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_MONO   = new Font("Consolas", Font.BOLD, 13);
    private static final Font FONT_BTN    = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_ERR    = new Font("Segoe UI", Font.PLAIN, 11);

    // ── DAOs ────────────────────────────────────────────────────────────
    private final DAO_Tuyen daoTuyen = new DAO_Tuyen();
    private final DAO_Ga    daoGa    = new DAO_Ga();

    // ── State ───────────────────────────────────────────────────────────
    private final boolean  isEditMode;
    private final Tuyen    editTarget;   // null when adding
    private       Consumer<Tuyen> onSaved;

    // ── Widgets ─────────────────────────────────────────────────────────
    private JTextField             txtMaTuyen;
    private JTextField             txtKm;
    private SearchableComboBox<Ga> cboGaDi;
    private SearchableComboBox<Ga> cboGaDen;

    private JLabel lblErrKm;
    private JLabel lblErrGaDi;
    private JLabel lblErrGaDen;
    private JLabel lblErrGeneral;

    // ── Constructor: ADD ─────────────────────────────────────────────────
    public ChinhSuaTuyenDialog(Frame owner) {
        this(owner, null);
    }

    // ── Constructor: EDIT ────────────────────────────────────────────────
    public ChinhSuaTuyenDialog(Frame owner, Tuyen target) {
        super(owner, true);
        this.isEditMode = (target != null);
        this.editTarget = target;

        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        initUI();
        loadGaList();
        if (isEditMode) prefillFields();

        pack();
        setLocationRelativeTo(owner);
    }

    public void setOnSaved(Consumer<Tuyen> cb) { this.onSaved = cb; }

    // ====================================================================
    //  UI BUILD
    // ====================================================================

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CARD_BG);
        root.setBorder(BorderFactory.createLineBorder(OUTLINE, 1));

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildForm(),   BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);

        setContentPane(ThemNhanVienDialog.buildShadowWrapper(root));
    }

    // ── Header ──────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setBackground(HEADER_BG);
        hdr.setBorder(new EmptyBorder(20, 24, 16, 24));

        // Icon + title row
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleRow.setOpaque(false);

        // Route icon circle
        JPanel iconCircle = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(PRIMARY_LIGHT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        iconCircle.setOpaque(false);
        iconCircle.setPreferredSize(new Dimension(44, 44));
        iconCircle.setLayout(new GridBagLayout());
        JLabel iconLbl = new JLabel("⇌");
        iconLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        iconLbl.setForeground(PRIMARY);
        iconCircle.add(iconLbl);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel(isEditMode ? "Chỉnh sửa Tuyến đường" : "Thêm Tuyến đường mới");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(ON_SURFACE);

        JLabel lblDesc = new JLabel(isEditMode
                ? "Cập nhật thông tin chi tiết hành trình"
                : "Nhập thông tin để tạo tuyến mới");
        lblDesc.setFont(FONT_DESC);
        lblDesc.setForeground(ON_SURF_VAR);

        textPanel.add(lblTitle);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(lblDesc);

        titleRow.add(iconCircle);
        titleRow.add(textPanel);
        hdr.add(titleRow, BorderLayout.CENTER);

        // Close (×) button
        JButton btnClose = new JButton("✕") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(ERROR_BG);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnClose.setForeground(ON_SURF_VAR);
        btnClose.setContentAreaFilled(false);
        btnClose.setBorderPainted(false);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClose.setPreferredSize(new Dimension(32, 32));
        btnClose.addActionListener(e -> dispose());
        hdr.add(btnClose, BorderLayout.EAST);

        return hdr;
    }

    // ── Form ─────────────────────────────────────────────────────────────
    private JPanel buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(CARD_BG);
        form.setBorder(new EmptyBorder(20, 24, 16, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.anchor  = GridBagConstraints.WEST;
        gbc.weightx = 0.5;

        // ─ Row 0: MÃ TUYẾN label (full width) ────────────────────────────
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 4, 0);
        form.add(makeLabel("Mã Tuyến"), gbc);

        // ─ Row 1: MÃ TUYẾN field (full width) ────────────────────────────
        String maTuyen = isEditMode ? editTarget.getMaTuyen() : generateMaTuyen();
        txtMaTuyen = new JTextField(maTuyen);
        txtMaTuyen.setFont(FONT_MONO);
        txtMaTuyen.setEditable(false);
        txtMaTuyen.setBackground(READONLY_BG);
        txtMaTuyen.setForeground(ON_SURF_VAR);
        txtMaTuyen.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1),
                new EmptyBorder(9, 12, 9, 12)));

        gbc.gridy  = 1;
        gbc.insets = new Insets(0, 0, 12, 0);
        form.add(txtMaTuyen, gbc);

        // ─ Row 2: SỐ KM label (full width) ───────────────────────────────
        gbc.gridy  = 2;
        gbc.insets = new Insets(0, 0, 4, 0);
        form.add(makeLabel("Số km"), gbc);

        // ─ Row 3: SỐ KM field (full width) ───────────────────────────────
        txtKm = new JTextField(isEditMode ? String.valueOf(editTarget.getKm()) : "0");
        txtKm.setFont(FONT_INPUT);
        txtKm.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1),
                new EmptyBorder(9, 12, 9, 12)));

        gbc.gridy  = 3;
        gbc.insets = new Insets(0, 0, 2, 0);
        form.add(txtKm, gbc);

        lblErrKm = makeErrLabel();
        gbc.gridy  = 4;
        gbc.insets = new Insets(0, 0, 12, 0);
        form.add(lblErrKm, gbc);

        // ─ Row 5: GA ĐI label | GA ĐẾN label ─────────────────────────────
        gbc.gridwidth = 1;
        gbc.gridy  = 5;
        gbc.gridx  = 0;
        gbc.insets = new Insets(0, 0, 4, 8);
        form.add(makeLabel("Ga đi"), gbc);

        gbc.gridx  = 1;
        gbc.insets = new Insets(0, 0, 4, 0);
        form.add(makeLabel("Ga đến"), gbc);

        // ─ Row 6: GA ĐI combo | GA ĐẾN combo ─────────────────────────────
        cboGaDi = new SearchableComboBox<>(
                ga -> ga.getTenGa() + " (" + ga.getMaGa() + ")",
                (ga, q) -> ga.getTenGa().toLowerCase().contains(q)
                        || ga.getMaGa().toLowerCase().contains(q));
        cboGaDi.setPlaceholder("Tìm ga đi...");
        cboGaDi.setPreferredSize(new Dimension(200, 42));

        gbc.gridy  = 6;
        gbc.gridx  = 0;
        gbc.insets = new Insets(0, 0, 2, 8);
        form.add(cboGaDi, gbc);

        cboGaDen = new SearchableComboBox<>(
                ga -> ga.getTenGa() + " (" + ga.getMaGa() + ")",
                (ga, q) -> ga.getTenGa().toLowerCase().contains(q)
                        || ga.getMaGa().toLowerCase().contains(q));
        cboGaDen.setPlaceholder("Tìm ga đến...");
        cboGaDen.setPreferredSize(new Dimension(200, 42));

        gbc.gridx  = 1;
        gbc.insets = new Insets(0, 0, 2, 0);
        form.add(cboGaDen, gbc);

        // ─ Row 7: error labels ────────────────────────────────────────────
        lblErrGaDi  = makeErrLabel();
        lblErrGaDen = makeErrLabel();

        gbc.gridy  = 7;
        gbc.gridx  = 0;
        gbc.insets = new Insets(0, 0, 0, 8);
        form.add(lblErrGaDi, gbc);

        gbc.gridx  = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        form.add(lblErrGaDen, gbc);

        // ─ Row 8: general error (full width) ─────────────────────────────
        lblErrGeneral = makeErrLabel();
        gbc.gridy     = 8;
        gbc.gridx     = 0;
        gbc.gridwidth = 2;
        gbc.insets    = new Insets(6, 0, 0, 0);
        form.add(lblErrGeneral, gbc);

        return form;
    }

    // ── Footer ────────────────────────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setBackground(FOOTER_BG);
        footer.setBorder(new EmptyBorder(16, 24, 20, 24));

        JButton btnCancel = makeButton("Hủy bỏ", false);
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = makeButton(isEditMode ? "Cập nhật Tuyến" : "Thêm Tuyến", true);
        btnSave.addActionListener(e -> doSave());

        footer.add(btnCancel);
        footer.add(btnSave);
        return footer;
    }

    // ====================================================================
    //  DATA
    // ====================================================================

    private void loadGaList() {
        List<Ga> gaList = daoGa.getAll();
        cboGaDi.setItems(gaList);
        cboGaDen.setItems(gaList);
    }

    private void prefillFields() {
        if (editTarget.getGaDi() != null)  cboGaDi.selectItem(editTarget.getGaDi());
        if (editTarget.getGaDen() != null) cboGaDen.selectItem(editTarget.getGaDen());
    }

    // ====================================================================
    //  SAVE LOGIC
    // ====================================================================

    private void doSave() {
        clearErrors();

        // Validate km
        int km = 0;
        String kmStr = txtKm.getText().trim();
        try {
            km = Integer.parseInt(kmStr);
            if (km < 0) {
                lblErrKm.setText("Số km không được âm");
                return;
            }
        } catch (NumberFormatException ex) {
            lblErrKm.setText("Số km phải là số nguyên");
            return;
        }

        Ga gaDi  = cboGaDi.getSelectedItem();
        Ga gaDen = cboGaDen.getSelectedItem();
        boolean valid = true;

        if (gaDi == null) {
            lblErrGaDi.setText("Vui lòng chọn ga đi");
            valid = false;
        }
        if (gaDen == null) {
            lblErrGaDen.setText("Vui lòng chọn ga đến");
            valid = false;
        }
        if (gaDi != null && gaDen != null && gaDi.getMaGa().equals(gaDen.getMaGa())) {
            lblErrGaDen.setText("Ga đến phải khác ga đi");
            valid = false;
        }

        if (!valid) return;

        // Duplicate check for add mode
        if (!isEditMode) {
            List<Tuyen> existing = daoTuyen.findByGaDiGaDen(gaDi.getMaGa(), gaDen.getMaGa());
            if (!existing.isEmpty()) {
                lblErrGeneral.setText("Tuyến " + gaDi.getTenGa() + " → " + gaDen.getTenGa() + " đã tồn tại (Mã: " + existing.get(0).getMaTuyen() + ")");
                return;
            }
        }

        String maTuyen = txtMaTuyen.getText().trim();
        Tuyen tuyen = new Tuyen(maTuyen, gaDi, gaDen, km);

        boolean ok = isEditMode ? daoTuyen.update(tuyen) : daoTuyen.insert(tuyen);
        if (ok) {
            if (onSaved != null) onSaved.accept(tuyen);
            dispose();
        } else {
            lblErrGeneral.setText("Có lỗi xảy ra khi lưu, vui lòng thử lại.");
        }
    }

    private void clearErrors() {
        lblErrKm.setText("");
        lblErrGaDi.setText("");
        lblErrGaDen.setText("");
        lblErrGeneral.setText("");
    }

    // ====================================================================
    //  HELPERS
    // ====================================================================

    private String generateMaTuyen() {
        // Use timestamp-based suffix to ensure uniqueness
        long suffix = System.currentTimeMillis() % 100000L;
        return "TUY-" + String.format("%05d", suffix);
    }

    private JLabel makeLabel(String text) {
        JLabel lbl = new JLabel(text.toUpperCase());
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(ON_SURF_VAR);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JLabel makeErrLabel() {
        JLabel lbl = new JLabel(" ");
        lbl.setFont(FONT_ERR);
        lbl.setForeground(ERROR);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JButton makeButton(String text, boolean primary) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (primary) {
                    g2.setColor(getModel().isRollover() ? PRIMARY_HOVER : PRIMARY);
                } else {
                    g2.setColor(getModel().isRollover() ? OUTLINE.darker() : OUTLINE);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BTN);
        btn.setForeground(primary ? Color.WHITE : ON_SURFACE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(primary ? 160 : 100, 40));
        return btn;
    }
}
