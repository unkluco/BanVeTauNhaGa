package com.modules;

import com.entity.DauMay;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SuaDauMayDialog extends AbstractFormDialog<DauMay> {
    private static final Color PRIMARY = AppColors.PRIMARY_DARK;
    private static final Color CARD_BG = AppColors.SURFACE;
    private static final Color ON_SURFACE = AppColors.TEXT_PRIMARY;
    private static final Color OUTLINE = AppColors.BORDER;
    private static final Color ERROR = AppColors.ERROR_DARK;

    private static final Font FONT_INPUT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_MONO = new Font("Consolas", Font.BOLD, 13);
    private static final Font FONT_ERR = new Font("Segoe UI", Font.PLAIN, 11);

    private final DauMay source;
    private final boolean createMode;
    private final String generatedMaDauMay;
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
        this(owner, dauMay, null, false, onSaved);
    }

    public SuaDauMayDialog(Window owner, String maDauMay, Consumer<DauMay> onSaved) {
        this(owner, null, maDauMay, true, onSaved);
    }

    private SuaDauMayDialog(Window owner, DauMay dauMay, String maDauMay,
                            boolean createMode, Consumer<DauMay> onSaved) {
        super(owner, createMode ? "Th?m ??u m?y" : "S?a ??u m?y");
        this.source = dauMay;
        this.createMode = createMode;
        this.generatedMaDauMay = maDauMay;
        this.onSaved = onSaved;
        installStandardLayout(owner);
    }

    @Override
    protected String dialogTitle() {
        return createMode ? "Thêm đầu máy" : "Sửa đầu máy";
    }

    @Override
    protected String dialogDescription() {
        return createMode
                ? "Nhập thông tin đầu máy mới để đưa vào khai thác."
                : "Cập nhật thông tin kỹ thuật và trạng thái đầu máy.";
    }

    @Override
    protected LineIcons.Name dialogIcon() {
        return LineIcons.Name.TRAIN;
    }

    @Override
    protected String primaryButtonText() {
        return createMode ? "Tạo đầu máy" : "Lưu thay đổi";
    }

    @Override
    protected int preferredDialogWidth() {
        return 760;
    }

    @Override
    protected AbstractFormDialog.FormSchema buildFormSchema() {
        txtMaDauMay = createReadonlyField(createMode ? generatedMaDauMay : source == null ? "" : source.getMaDauMay());
        txtTenDauMay = createInputField(source == null ? "" : source.getTenDauMay());
        lblErrTen = createErrorLabel();
        txtHangSanXuat = createInputField(valueOf(source == null ? null : source.getHangSanXuat()));
        txtNamSanXuat = createInputField(source == null || source.getNamSanXuat() == null ? "" : String.valueOf(source.getNamSanXuat()));
        lblErrNam = createErrorLabel();
        txtCongSuat = createInputField(source == null || source.getCongSuatKw() == null ? "" : String.valueOf(source.getCongSuatKw()));
        lblErrCongSuat = createErrorLabel();
        cboTrangThai = new JComboBox<>(new String[]{"Đang hoạt động", "Đang bảo trì", "Ngừng hoạt động"});
        String status = source == null ? null : source.getTrangThai();
        if (status != null && !status.isBlank()) cboTrangThai.setSelectedItem(normalizeStatus(status));
        txtMoTa = new JTextArea(source == null || source.getMoTa() == null ? "" : source.getMoTa(), 4, 30);
        txtMoTa.setLineWrap(true);
        txtMoTa.setWrapStyleWord(true);
        JScrollPane sp = new JScrollPane(txtMoTa);

        return AbstractFormDialog.FormSchema.builder()
                .columns(3)
                .gap(16, 14)
                .field(AbstractFormDialog.FieldSpec.of("maDauMay", "Mã đầu máy", txtMaDauMay).grid(0, 0).hint("* Không thể thay đổi").build())
                .field(AbstractFormDialog.FieldSpec.of("tenDauMay", "Tên đầu máy", txtTenDauMay).grid(0, 1).span(2).required(true).errorLabel(lblErrTen).build())
                .field(AbstractFormDialog.FieldSpec.of("hangSanXuat", "Hãng sản xuất", txtHangSanXuat).grid(1, 0).build())
                .field(AbstractFormDialog.FieldSpec.of("namSanXuat", "Năm sản xuất", txtNamSanXuat).grid(1, 1).errorLabel(lblErrNam).build())
                .field(AbstractFormDialog.FieldSpec.of("congSuat", "Công suất (kW)", txtCongSuat).grid(1, 2).errorLabel(lblErrCongSuat).build())
                .field(AbstractFormDialog.FieldSpec.of("trangThai", "Trạng thái", cboTrangThai).grid(2, 0).fullWidth().build())
                .field(AbstractFormDialog.FieldSpec.of("moTa", "Mô tả kỹ thuật", sp).grid(3, 0).fullWidth().preferredHeight(92).build())
                .build();
        // Handoff: dialog chrome, footer and grid are owned by AbstractFormDialog.
        // Risk: validate/collect still read component references to preserve existing behavior.
    }

    private JTextField createInputField(String value) {
        JTextField field = new JTextField(value == null ? "" : value);
        field.setFont(FONT_INPUT);
        field.setForeground(ON_SURFACE);
        field.setBackground(CARD_BG);
        field.setPreferredSize(new Dimension(0, 40));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1),
                new EmptyBorder(8, 10, 8, 10)
        ));
        return field;
    }

    private JTextField createReadonlyField(String value) {
        JTextField field = createInputField(value);
        field.setEditable(false);
        field.setFont(FONT_MONO);
        field.setForeground(PRIMARY);
        field.setBackground(AppColors.BACKGROUND);
        return field;
    }

    private JLabel createErrorLabel() {
        JLabel label = new JLabel(" ");
        label.setFont(FONT_ERR);
        label.setForeground(ERROR);
        return label;
    }

    @Override
    protected List<ValidationError> validateForm(FormValues values) {
        clearErrors();
        List<ValidationError> errors = new ArrayList<>();
        if (txtTenDauMay.getText().trim().isEmpty()) {
            lblErrTen.setText("T?n ??u m?y kh?ng ???c ?? tr?ng.");
            errors.add(new ValidationError("tenDauMay", "T?n ??u m?y kh?ng ???c ?? tr?ng."));
        }
        validateIntegerRange(txtNamSanXuat.getText(), 1950, 2100, "namSanXuat", lblErrNam,
                "N?m s?n xu?t ph?i l? s? nguy?n.", "N?m s?n xu?t ph?i trong kho?ng 1950 - 2100.", errors);
        validateIntegerRange(txtCongSuat.getText(), 1, 10000, "congSuat", lblErrCongSuat,
                "C?ng su?t ph?i l? s? nguy?n.", "C?ng su?t ph?i > 0 v? <= 10000 kW.", errors);
        return errors;
    }

    private void validateIntegerRange(String text, int min, int max, String fieldId, JLabel label,
                                      String invalidMessage, String rangeMessage, List<ValidationError> errors) {
        String trimmed = text == null ? "" : text.trim();
        if (trimmed.isEmpty()) return;
        try {
            int value = Integer.parseInt(trimmed);
            if (value < min || value > max) {
                label.setText(rangeMessage);
                errors.add(new ValidationError(fieldId, rangeMessage));
            }
        } catch (NumberFormatException ex) {
            label.setText(invalidMessage);
            errors.add(new ValidationError(fieldId, invalidMessage));
        }
    }

    private void clearErrors() {
        lblErrTen.setText(" ");
        lblErrNam.setText(" ");
        lblErrCongSuat.setText(" ");
    }

    @Override
    protected DauMay collectResult(FormValues values) {
        DauMay out = new DauMay();
        out.setMaDauMay(txtMaDauMay.getText().trim());
        out.setTenDauMay(txtTenDauMay.getText().trim());
        out.setHangSanXuat(toNullIfBlank(txtHangSanXuat.getText()));
        out.setNamSanXuat(parseIntegerOrNull(txtNamSanXuat.getText()));
        out.setCongSuatKw(parseIntegerOrNull(txtCongSuat.getText()));
        out.setTrangThai((String) cboTrangThai.getSelectedItem());
        out.setMoTa(toNullIfBlank(txtMoTa.getText()));
        return out;
    }

    @Override
    protected void onSubmit(DauMay result) {
        if (onSaved != null) onSaved.accept(result);
        dispose();
    }

    private String toNullIfBlank(String text) {
        if (text == null) return null;
        String trimmed = text.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String valueOf(String text) {
        return text == null ? "" : text;
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

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) return "Đang hoạt động";
        String lower = status.trim().toLowerCase();
        if (lower.contains("bảo trì") || lower.contains("bao tri")) return "Đang bảo trì";
        if (lower.contains("ngừng") || lower.contains("ngung")) return "Ngừng hoạt động";
        return "Đang hoạt động";
    }
}
