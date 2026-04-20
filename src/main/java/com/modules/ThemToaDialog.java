package com.modules;

import com.entity.ToaTau;
import com.enums.LoaiGhe;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Consumer;

public class ThemToaDialog extends JDialog {

    private static final Color PRIMARY       = new Color(0x00, 0x5D, 0x90);
    private static final Color PRIMARY_HOVER = new Color(0x00, 0x4A, 0x73);
    private static final Color CARD_BG       = Color.WHITE;
    private static final Color SURFACE       = new Color(0xF8, 0xFA, 0xFC);
    private static final Color ON_SURFACE    = new Color(0x1A, 0x1D, 0x21);
    private static final Color ON_SURF_VAR   = new Color(0x5F, 0x67, 0x70);
    private static final Color OUTLINE       = new Color(0xDE, 0xE3, 0xE8);
    private static final Color HEADER_BG     = new Color(0xF1, 0xF5, 0xF9);
    private static final Color FOOTER_BG     = new Color(0xF1, 0xF5, 0xF9);
    private static final Color INFO_BG       = new Color(0xEE, 0xF5, 0xFB);

    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font FONT_DESC  = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font FONT_INPUT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_MONO  = new Font("Consolas", Font.BOLD, 13);
    private static final Font FONT_BTN   = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_NOTE  = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_VALUE = new Font("Segoe UI", Font.BOLD, 14);

    private final Consumer<ToaTau> onSaved;
    private final String maToaTau;

    private JTextField txtMaToaTau;
    private JComboBox<LoaiGhe> cboLoaiGhe;
    private JLabel lblSoLuongGhe;

    public ThemToaDialog(Window owner, String maToaTau, Consumer<ToaTau> onSaved) {
        super(owner, "Thêm toa tàu", ModalityType.APPLICATION_MODAL);
        this.maToaTau = maToaTau;
        this.onSaved = onSaved;

        setUndecorated(true);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        initUI();
        pack();
        setMinimumSize(new Dimension(680, getPreferredSize().height));
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

        ImageIcon icon = loadScaledIcon("bieuTuongTau.png", 30);
        if (icon != null) {
            left.add(new JLabel(icon), BorderLayout.WEST);
        }

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Thêm toa tàu mới");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(PRIMARY);

        JLabel lblDesc = new JLabel("Chọn loại ghế, hệ thống sẽ tự sinh đầy đủ vị trí ghế tương ứng.");
        lblDesc.setFont(FONT_DESC);
        lblDesc.setForeground(ON_SURF_VAR);

        textPanel.add(lblTitle);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(lblDesc);
        left.add(textPanel, BorderLayout.CENTER);

        header.add(left, BorderLayout.CENTER);
        return header;
    }

    private JPanel buildForm() {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(CARD_BG);
        form.setBorder(new EmptyBorder(20, 24, 14, 24));

        JPanel row1 = new JPanel(new GridLayout(1, 2, 16, 0));
        row1.setOpaque(false);
        row1.setAlignmentX(Component.LEFT_ALIGNMENT);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        txtMaToaTau = createReadonlyField(maToaTau == null ? "" : maToaTau);
        row1.add(buildFieldGroup("Mã toa tàu", txtMaToaTau, "* Mã được sinh tự động", false));

        cboLoaiGhe = new JComboBox<>(LoaiGhe.values());
        cboLoaiGhe.setFont(FONT_INPUT);
        cboLoaiGhe.setBackground(CARD_BG);
        cboLoaiGhe.setPreferredSize(new Dimension(0, 36));
        cboLoaiGhe.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cboLoaiGhe.addActionListener(e -> updateSeatCountHint());
        row1.add(buildFieldGroup("Loại ghế", cboLoaiGhe, null, true));

        form.add(row1);
        form.add(Box.createVerticalStrut(12));

        JPanel infoRow = new JPanel(new BorderLayout());
        infoRow.setOpaque(false);
        infoRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        infoRow.add(buildSeatInfoCard(), BorderLayout.CENTER);
        form.add(infoRow);

        return form;
    }

    private JPanel buildSeatInfoCard() {
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBackground(INFO_BG);
        info.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xC7, 0xDD, 0xEE), 1),
                new EmptyBorder(12, 14, 12, 14)
        ));
        info.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        info.setPreferredSize(new Dimension(0, 120));

        JLabel lblTitle = new JLabel("Tự động tạo ghế");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(ON_SURFACE);

        lblSoLuongGhe = new JLabel();
        lblSoLuongGhe.setFont(FONT_VALUE);
        lblSoLuongGhe.setForeground(PRIMARY);

        JLabel lblRule = new JLabel("Quy tắc: Ghế cứng/Ghế mềm = 48 ghế, Giường nằm = 30 ghế.");
        lblRule.setFont(FONT_NOTE);
        lblRule.setForeground(ON_SURF_VAR);

        info.add(lblTitle);
        info.add(Box.createVerticalStrut(6));
        info.add(lblSoLuongGhe);
        info.add(Box.createVerticalStrut(4));
        info.add(lblRule);

        updateSeatCountHint();
        return info;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setBackground(FOOTER_BG);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, OUTLINE),
                new EmptyBorder(14, 24, 14, 24)
        ));

        JButton btnCancel = createOutlineButton("Hủy bỏ");
        ImageIcon icoCancel = loadScaledIcon("nutThoat.png", 15);
        if (icoCancel != null) {
            btnCancel.setIcon(icoCancel);
            btnCancel.setIconTextGap(8);
        }
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = createPrimaryButton("Tạo toa");
        btnSave.addActionListener(e -> saveAndClose());
        getRootPane().setDefaultButton(btnSave);

        footer.add(btnCancel);
        footer.add(btnSave);
        return footer;
    }

    private JPanel buildFieldGroup(String labelText, JComponent input, String hintText, boolean required) {
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
        return group;
    }

    private JTextField createReadonlyField(String value) {
        JTextField tf = new JTextField(value == null ? "" : value);
        tf.setEditable(false);
        tf.setFont(FONT_MONO);
        tf.setForeground(PRIMARY);
        tf.setBackground(SURFACE);
        tf.setPreferredSize(new Dimension(0, 40));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1),
                new EmptyBorder(8, 10, 8, 10)
        ));
        return tf;
    }

    private JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
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
        btn.setPreferredSize(new Dimension(150, 40));
        return btn;
    }

    private JButton createOutlineButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
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

    private void updateSeatCountHint() {
        LoaiGhe selected = (LoaiGhe) cboLoaiGhe.getSelectedItem();
        int count = defaultSeatCount(selected);
        String loai = selected == null ? "Không xác định" : selected.toString();
        lblSoLuongGhe.setText(loai + " -> tự sinh " + count + " ghế.");
    }

    private int defaultSeatCount(LoaiGhe loaiGhe) {
        if (loaiGhe == LoaiGhe.GIUONG_NAM) return 30;
        return 48;
    }

    private void saveAndClose() {
        String ma = txtMaToaTau.getText().trim();
        LoaiGhe loaiGhe = (LoaiGhe) cboLoaiGhe.getSelectedItem();

        if (ma.isEmpty() || loaiGhe == null) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng kiểm tra lại mã toa và loại ghế.",
                    "Thiếu thông tin",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        ToaTau out = new ToaTau(ma, loaiGhe);
        if (onSaved != null) onSaved.accept(out);
        dispose();
    }

    private ImageIcon loadScaledIcon(String name, int size) {
        try {
            java.net.URL url = getClass().getResource("/icons/" + name);
            if (url != null) {
                Image img = new ImageIcon(url).getImage()
                        .getScaledInstance(size, size, Image.SCALE_SMOOTH);
                return new ImageIcon(img);
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
