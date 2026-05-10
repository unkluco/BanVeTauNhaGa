package com.modules;

import com.entity.ToaTau;
import com.enums.LoaiGhe;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;

public class ThemToaDialog extends AbstractFormDialog<ToaTau> {
    private static final Color PRIMARY = AppColors.PRIMARY_DARK;
    private static final Color SURFACE = AppColors.BACKGROUND;
    private static final Color ON_SURFACE = AppColors.TEXT_PRIMARY;
    private static final Color ON_SURF_VAR = AppColors.TEXT_SECONDARY;
    private static final Color OUTLINE = AppColors.BORDER;
    private static final Color INFO_BG = AppColors.PRIMARY_SUBTLE;

    private static final Font FONT_MONO = new Font("Consolas", Font.BOLD, 13);
    private static final Font FONT_NOTE = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_VALUE = new Font("Segoe UI", Font.BOLD, 14);

    private final Consumer<ToaTau> onSaved;
    private final String maToaTau;

    private JTextField txtMaToaTau;
    private JComboBox<LoaiGhe> cboLoaiGhe;
    private JComboBox<String> cboTrangThai;
    private JLabel lblSoLuongGhe;

    public ThemToaDialog(Window owner, String maToaTau, Consumer<ToaTau> onSaved) {
        super(owner, "Thêm toa tàu");
        this.maToaTau = maToaTau;
        this.onSaved = onSaved;
        installStandardLayout(owner);
    }

    @Override
    protected String dialogTitle() {
        return "Thêm toa tàu mới";
    }

    @Override
    protected String dialogDescription() {
        return "Chọn loại ghế, hệ thống sẽ tự sinh đầy đủ vị trí ghế tương ứng.";
    }

    @Override
    protected LineIcons.Name dialogIcon() {
        return LineIcons.Name.TRAIN;
    }

    @Override
    protected String primaryButtonText() {
        return "Tạo toa";
    }

    @Override
    protected int preferredDialogWidth() {
        return 680;
    }

    @Override
    protected AbstractFormDialog.FormSchema buildFormSchema() {
        txtMaToaTau = createReadonlyField(maToaTau == null ? "" : maToaTau);
        cboLoaiGhe = new JComboBox<>(LoaiGhe.values());
        cboLoaiGhe.addActionListener(e -> updateSeatCountHint());
        cboTrangThai = new JComboBox<>(new String[]{"Đang hoạt động", "Đang bảo trì", "Ngừng hoạt động"});

        return AbstractFormDialog.FormSchema.builder()
                .columns(2)
                .gap(16, 14)
                .field(AbstractFormDialog.FieldSpec.of("maToaTau", "Mã toa tàu", txtMaToaTau)
                        .grid(0, 0)
                        .hint("* Mã được sinh tự động")
                        .build())
                .field(AbstractFormDialog.FieldSpec.of("loaiGhe", "Loại ghế", cboLoaiGhe)
                        .grid(0, 1)
                        .required(true)
                        .build())
                .field(AbstractFormDialog.FieldSpec.of("trangThai", "Trạng thái", cboTrangThai)
                        .grid(1, 0)
                        .fullWidth()
                        .build())
                .build();
        // Handoff: dialog chrome, footer and grid are owned by AbstractFormDialog.
        // Risk: keep component references because seat hint and submit mapping depend on them.
    }

    @Override
    protected JComponent buildExtraContent() {
        return buildSeatInfoCard();
    }

    private JPanel buildSeatInfoCard() {
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBackground(INFO_BG);
        info.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.PRIMARY_LIGHT, 1),
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

    private JTextField createReadonlyField(String value) {
        JTextField field = new JTextField(value == null ? "" : value);
        field.setEditable(false);
        field.setFont(FONT_MONO);
        field.setForeground(PRIMARY);
        field.setBackground(SURFACE);
        field.setPreferredSize(new Dimension(0, 40));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1),
                new EmptyBorder(8, 10, 8, 10)
        ));
        return field;
    }

    private void updateSeatCountHint() {
        LoaiGhe selected = (LoaiGhe) cboLoaiGhe.getSelectedItem();
        int count = selected == LoaiGhe.GIUONG_NAM ? 30 : 48;
        String loai = selected == null ? "Kh?ng x?c ??nh" : selected.toString();
        lblSoLuongGhe.setText(loai + " -> t? sinh " + count + " gh?.");
    }

    @Override
    protected java.util.List<ValidationError> validateForm(FormValues values) {
        String ma = txtMaToaTau.getText().trim();
        LoaiGhe loaiGhe = (LoaiGhe) cboLoaiGhe.getSelectedItem();
        if (ma.isEmpty() || loaiGhe == null) {
            NotionMessageDialog.showMessageDialog(this,
                    "Vui l?ng ki?m tra l?i m? toa v? lo?i gh?.",
                    "Thiếu thông tin",
                    JOptionPane.WARNING_MESSAGE);
            return java.util.List.of(new ValidationError("loaiGhe", "Vui l?ng ki?m tra l?i m? toa v? lo?i gh?."));
        }
        return java.util.List.of();
    }

    @Override
    protected ToaTau collectResult(FormValues values) {
        ToaTau out = new ToaTau(txtMaToaTau.getText().trim(), (LoaiGhe) cboLoaiGhe.getSelectedItem());
        out.setTrangThai((String) cboTrangThai.getSelectedItem());
        return out;
    }

    @Override
    protected void onSubmit(ToaTau result) {
        if (onSaved != null) onSaved.accept(result);
        dispose();
    }
}
