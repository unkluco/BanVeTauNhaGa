package com.modules;

import com.dao.DAO_DauMay;
import com.dao.DAO_DoanTau;
import com.entity.DauMay;
import com.entity.DoanTau;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.function.Consumer;

public class ChinhSuaDoanTauModule extends JPanel implements AppModule {

    private Consumer<Object> callback;
    private final DoanTau doanTau;
    private final boolean isEditMode;

    // Design tokens
    private static final Color PRIMARY       = new Color(0x00, 0x5D, 0x90);
    private static final Color PRIMARY_HOVER = new Color(0x00, 0x4A, 0x73);
    private static final Color SURFACE       = new Color(0xF8, 0xFA, 0xFC);
    private static final Color CARD_BG       = Color.WHITE;
    private static final Color ON_SURFACE    = new Color(0x1A, 0x1D, 0x21);
    private static final Color ON_SURF_VAR   = new Color(0x5F, 0x67, 0x70);
    private static final Color OUTLINE       = new Color(0xDE, 0xE3, 0xE8);
    private static final Color READONLY_BG   = new Color(0xF1, 0xF5, 0xF9);
    private static final Color ERROR         = new Color(0xBA, 0x1A, 0x1A);

    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font FONT_DESC  = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font FONT_INPUT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_MONO  = new Font("Consolas", Font.BOLD, 13);
    private static final Font FONT_BTN   = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_ERR   = new Font("Segoe UI", Font.PLAIN, 11);

    // Form fields
    private JTextField        txtMaDoanTau;
    private JTextField        txtTenDoanTau;
    private JComboBox<DauMay> cboDauMay;
    private JLabel            lblErrTen;
    private JLabel            lblErrDauMay;

    // AppModule hidden buttons
    private JButton btnSubmit;
    private JButton btnCancel;
    private JPanel  btnPanel;

    public ChinhSuaDoanTauModule(DoanTau doanTau) {
        this.doanTau    = doanTau;
        this.isEditMode = (doanTau != null);
        setLayout(new BorderLayout());
        setBackground(SURFACE);

        btnSubmit = new JButton();
        btnCancel = new JButton();
        btnPanel  = new JPanel();
        btnPanel.setVisible(false);
        add(btnPanel, BorderLayout.SOUTH);

        buildUI();
    }

    private void buildUI() {
        add(buildHeader(),  BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
    }

    // ── Header ────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setBackground(CARD_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, OUTLINE),
                new EmptyBorder(20, 36, 16, 36)
        ));

        JButton btnBack = new JButton("← Quay lại");
        btnBack.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnBack.setForeground(ON_SURF_VAR);
        btnBack.setContentAreaFilled(false);
        btnBack.setBorderPainted(false);
        btnBack.setFocusPainted(false);
        btnBack.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> { if (callback != null) callback.accept(null); });

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        String titleText = isEditMode ? "Chỉnh sửa đoàn tàu" : "Thêm đoàn tàu mới";
        JLabel lblTitle  = new JLabel(titleText);
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(PRIMARY);

        String descText = isEditMode
                ? "Cập nhật thông tin đoàn tàu."
                : "Điền đầy đủ thông tin để tạo đoàn tàu mới.";
        JLabel lblDesc = new JLabel(descText);
        lblDesc.setFont(FONT_DESC);
        lblDesc.setForeground(ON_SURF_VAR);

        titlePanel.add(lblTitle);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(lblDesc);

        header.add(btnBack,    BorderLayout.WEST);
        header.add(titlePanel, BorderLayout.CENTER);
        return header;
    }

    // ── Content (form card + buttons) ─────────────────────────────────────

    private JPanel buildContent() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(SURFACE);
        wrapper.setBorder(new EmptyBorder(28, 36, 28, 36));

        // Form card
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1, true),
                new EmptyBorder(24, 28, 24, 28)
        ));

        // Ma doan tau (read-only)
        String maValue = isEditMode ? doanTau.getMaDoanTau() : generateMaDoanTau();
        txtMaDoanTau = new JTextField(maValue);
        txtMaDoanTau.setFont(FONT_MONO);
        txtMaDoanTau.setForeground(ON_SURF_VAR);
        txtMaDoanTau.setEditable(false);
        txtMaDoanTau.setBackground(READONLY_BG);
        txtMaDoanTau.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        txtMaDoanTau.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));

        JPanel row1 = rowPanel();
        row1.add(buildFieldGroup("Mã đoàn tàu", txtMaDoanTau, null, false, null));
        card.add(row1);
        card.add(Box.createVerticalStrut(14));

        // Ten doan tau
        txtTenDoanTau = createInputField("VD: Tàu SE1");
        if (isEditMode && doanTau.getTenDoanTau() != null) {
            txtTenDoanTau.setText(doanTau.getTenDoanTau());
            txtTenDoanTau.setForeground(ON_SURFACE);
        }
        lblErrTen = createErrorLabel();

        JPanel row2 = rowPanel();
        row2.add(buildFieldGroup("Tên đoàn tàu", txtTenDoanTau, null, true, lblErrTen));
        card.add(row2);
        card.add(Box.createVerticalStrut(14));

        // Dau may
        cboDauMay = new JComboBox<>();
        cboDauMay.setFont(FONT_INPUT);
        cboDauMay.setEditable(false);
        cboDauMay.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof DauMay dm) {
                    setText(dm.getTenDauMay() + "  (" + dm.getMaDauMay() + ")");
                }
                return this;
            }
        });
        cboDauMay.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cboDauMay.setPreferredSize(new Dimension(0, 36));
        lblErrDauMay = createErrorLabel();

        JPanel row3 = rowPanel();
        row3.add(buildFieldGroup("Đầu máy", cboDauMay, null, true, lblErrDauMay));
        card.add(row3);

        wrapper.add(card, BorderLayout.CENTER);

        // Action buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        btnRow.setOpaque(false);
        btnRow.setBorder(new EmptyBorder(16, 0, 0, 0));

        JButton btnHuy = createOutlineButton("Hủy");
        btnHuy.addActionListener(e -> { if (callback != null) callback.accept(null); });

        JButton btnLuu = createPrimaryButton(isEditMode ? "Lưu thay đổi" : "Thêm đoàn tàu");
        btnLuu.addActionListener(e -> doSave());

        btnRow.add(btnHuy);
        btnRow.add(btnLuu);
        wrapper.add(btnRow, BorderLayout.SOUTH);

        // Load dau may list in background
        loadDauMayList();

        return wrapper;
    }

    // ── Data ──────────────────────────────────────────────────────────────

    private void loadDauMayList() {
        new SwingWorker<List<DauMay>, Void>() {
            @Override
            protected List<DauMay> doInBackground() {
                return new DAO_DauMay().getAll();
            }
            @Override
            protected void done() {
                try {
                    List<DauMay> list = get();
                    cboDauMay.removeAllItems();
                    for (DauMay dm : list) cboDauMay.addItem(dm);
                    if (isEditMode && doanTau.getDauMay() != null) {
                        String maDM = doanTau.getDauMay().getMaDauMay();
                        for (int i = 0; i < cboDauMay.getItemCount(); i++) {
                            if (cboDauMay.getItemAt(i).getMaDauMay().equals(maDM)) {
                                cboDauMay.setSelectedIndex(i);
                                break;
                            }
                        }
                    }
                } catch (Exception ex) {
                    System.err.println("Lỗi khi tải đầu máy: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void doSave() {
        // Clear errors
        lblErrTen.setText(""); lblErrTen.setVisible(false);
        lblErrDauMay.setText(""); lblErrDauMay.setVisible(false);
        txtTenDoanTau.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1, true),
                new EmptyBorder(8, 12, 8, 12)));

        String ten = getFieldText(txtTenDoanTau, "VD: Tàu SE1");
        if (ten.isEmpty()) {
            lblErrTen.setText("Vui lòng nhập tên đoàn tàu");
            lblErrTen.setVisible(true);
            txtTenDoanTau.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ERROR, 2, true),
                    new EmptyBorder(7, 11, 7, 11)));
            txtTenDoanTau.requestFocusInWindow();
            return;
        }

        Object sel = cboDauMay.getSelectedItem();
        if (!(sel instanceof DauMay dauMay)) {
            lblErrDauMay.setText("Vui lòng chọn đầu máy");
            lblErrDauMay.setVisible(true);
            return;
        }

        String ma = txtMaDoanTau.getText().trim();
        DoanTau dt = new DoanTau(ma, ten, dauMay);

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                DAO_DoanTau dao = new DAO_DoanTau();
                return isEditMode ? dao.update(dt) : dao.insert(dt);
            }
            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    if (get()) {
                        if (callback != null) callback.accept(dt);
                    } else {
                        JOptionPane.showMessageDialog(ChinhSuaDoanTauModule.this,
                                isEditMode
                                        ? "Không thể cập nhật đoàn tàu."
                                        : "Không thể thêm đoàn tàu. Mã có thể đã tồn tại.",
                                "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ChinhSuaDoanTauModule.this,
                            "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private String generateMaDoanTau() {
        return "DT-" + String.format("%05d", System.currentTimeMillis() % 100000L);
    }

    private JPanel rowPanel() {
        JPanel p = new JPanel(new GridLayout(1, 1));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        return p;
    }

    private JPanel buildFieldGroup(String label, JComponent input, String hint,
                                   boolean required, JLabel errLabel) {
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

        if (hint != null) {
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
                if (f.getText().equals(placeholder)) {
                    f.setText("");
                    f.setForeground(ON_SURFACE);
                }
                f.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(PRIMARY, 2, true),
                        new EmptyBorder(7, 11, 7, 11)));
            }
            @Override public void focusLost(FocusEvent e) {
                if (f.getText().trim().isEmpty()) {
                    f.setForeground(ON_SURF_VAR);
                    f.setText(placeholder);
                }
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

    private JLabel createErrorLabel() {
        JLabel lbl = new JLabel();
        lbl.setFont(FONT_ERR);
        lbl.setForeground(ERROR);
        lbl.setVisible(false);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
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
        btn.setPreferredSize(new Dimension(160, 40));
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

    // ── AppModule interface ───────────────────────────────────────────────

    @Override public String getTitle() { return isEditMode ? "Chỉnh sửa đoàn tàu" : "Thêm đoàn tàu mới"; }
    @Override public JPanel  getView() { return this; }
    @Override public void setOnResult(Consumer<Object> cb) { this.callback = cb; }
    @Override public void reset() { }
}
