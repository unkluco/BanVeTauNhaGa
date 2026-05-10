package com.modules;

import com.dao.DAO_Gia;
import com.entity.Gia;
import com.util.MaTuDong;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class ThemGiaDialog extends AbstractFormDialog<Gia> {
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String MO_TA_PLACEHOLDER = "VD: H\u00e0 N\u1ed9i - S\u00e0i G\u00f2n Economy";

    private JTextField txtMaGia;
    private JTextField txtMoTa;
    private JTextField txtThoiGianBatDau;
    private JTextField txtThoiGianKetThuc;
    private JComboBox<String> cboTrangThai;

    private boolean saved = false;
    private final Runnable onSaved;

    public ThemGiaDialog(Window owner, Runnable onSaved) {
        super(owner, "Th\u00eam k\u1ef3 gi\u00e1 m\u1edbi");
        this.onSaved = onSaved;
        installStandardLayout(owner);
    }

    @Override
    protected String dialogDescription() {
        return "T\u1ea1o k\u1ef3 gi\u00e1 m\u1edbi v\u1edbi kho\u1ea3ng th\u1eddi gian \u00e1p d\u1ee5ng r\u00f5 r\u00e0ng.";
    }

    @Override
    protected LineIcons.Name dialogIcon() {
        return LineIcons.Name.MONEY;
    }

    @Override
    protected String primaryButtonText() {
        return "Th\u00eam k\u1ef3 gi\u00e1";
    }

    @Override
    protected int preferredDialogWidth() {
        return 640;
    }

    @Override
    protected FormSchema buildFormSchema() {
        txtMaGia = createReadonlyField(generateMaGia());
        txtMoTa = createTextField();
        txtThoiGianBatDau = createTextField();
        txtThoiGianKetThuc = createTextField();
        cboTrangThai = new JComboBox<>(new String[]{"\u0110ang \u00e1p d\u1ee5ng", "Ng\u1eebng \u00e1p d\u1ee5ng"});

        // Handoff: them gia van nhap ngay bang text de giu hanh vi cu va format dd/MM/yyyy.
        // Rui ro: neu doi sang DatePicker thi can kiem lai luong nhap tay cua nguoi dung hien tai.
        return FormSchema.builder()
                .columns(2)
                .gap(16, 14)
                .field(FieldSpec.of("maGia", "M\u00e3 gi\u00e1", txtMaGia).grid(0, 0).required(true).build())
                .field(FieldSpec.of("trangThai", "Tr\u1ea1ng th\u00e1i", cboTrangThai).grid(0, 1).build())
                .field(FieldSpec.of("moTa", "M\u00f4 t\u1ea3 / T\u00ean k\u1ef3 gi\u00e1", txtMoTa)
                        .grid(1, 0).fullWidth().required(true).hint(MO_TA_PLACEHOLDER).build())
                .field(FieldSpec.of("batDau", "Th\u1eddi gian \u00e1p d\u1ee5ng", txtThoiGianBatDau)
                        .grid(2, 0).required(true).hint("\u0110\u1ecbnh d\u1ea1ng: dd/MM/yyyy").build())
                .field(FieldSpec.of("ketThuc", "Th\u1eddi gian k\u1ebft th\u00fac", txtThoiGianKetThuc)
                        .grid(2, 1).required(true).hint("\u0110\u1ecbnh d\u1ea1ng: dd/MM/yyyy").build())
                .build();
    }

    @Override
    protected List<ValidationError> validateForm(FormValues values) {
        List<ValidationError> errors = new ArrayList<>();
        String moTa = values.text("moTa");
        LocalDate batDau = parseDate(values.text("batDau"), "batDau", "ng\u00e0y \u00e1p d\u1ee5ng", errors);
        LocalDate ketThuc = parseDate(values.text("ketThuc"), "ketThuc", "ng\u00e0y k\u1ebft th\u00fac", errors);

        if (moTa.isEmpty()) {
            errors.add(new ValidationError("moTa", "Vui l\u00f2ng nh\u1eadp m\u00f4 t\u1ea3"));
        }
        if (batDau != null && ketThuc != null && !ketThuc.isAfter(batDau)) {
            errors.add(new ValidationError("ketThuc", "Th\u1eddi gian k\u1ebft th\u00fac ph\u1ea3i sau th\u1eddi gian b\u1eaft \u0111\u1ea7u"));
        }
        return errors;
    }

    private LocalDate parseDate(String value, String fieldId, String label, List<ValidationError> errors) {
        if (value == null || value.isBlank()) {
            errors.add(new ValidationError(fieldId, "Vui l\u00f2ng nh\u1eadp " + label));
            return null;
        }
        try {
            return LocalDate.parse(value.trim(), DT_FMT);
        } catch (DateTimeParseException ex) {
            errors.add(new ValidationError(fieldId, "Sai \u0111\u1ecbnh d\u1ea1ng (dd/MM/yyyy" + ")"));
            return null;
        }
    }

    @Override
    protected Gia collectResult(FormValues values) {
        String maGia = txtMaGia.getText().trim();
        String moTa = values.text("moTa");
        LocalDate batDau = LocalDate.parse(values.text("batDau"), DT_FMT);
        LocalDate ketThuc = LocalDate.parse(values.text("ketThuc"), DT_FMT);
        Object selTT = cboTrangThai.getSelectedItem();
        boolean trangThai = selTT == null || selTT.toString().contains("\u0110ang");
        return new Gia(maGia, batDau, ketThuc, moTa, trangThai);
    }

    @Override
    protected void onSubmit(Gia gia) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() {
                return new DAO_Gia().insert(gia);
            }
            @Override protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    if (get()) {
                        saved = true;
                        if (onSaved != null) onSaved.run();
                        dispose();
                    } else {
                        NotionMessageDialog.showMessageDialog(ThemGiaDialog.this,
                                "Kh\u00f4ng th\u1ec3 l\u01b0u! M\u00e3 gi\u00e1 c\u00f3 th\u1ec3 \u0111\u00e3 t\u1ed3n t\u1ea1i.", "L\u1ed7i", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    NotionMessageDialog.showMessageDialog(ThemGiaDialog.this,
                            "L\u1ed7i: " + ex.getMessage(), "L\u1ed7i", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
        // Handoff: luu van chay bang SwingWorker nhu ban cu de tranh khoa giao dien.
        // Rui ro: base chua disable nut luu khi worker chay nen tranh double-click lien tuc.
    }

    public boolean isSaved() { return saved; }

    private String generateMaGia() { return MaTuDong.generate("GIA"); }

    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(NotionTheme.BODY);
        field.setForeground(NotionTheme.TEXT);
        return field;
    }

    private JTextField createReadonlyField(String value) {
        JTextField field = createTextField();
        field.setText(value == null ? "" : value);
        field.setEditable(false);
        field.setFont(NotionTheme.BODY_BOLD);
        return field;
    }
}
