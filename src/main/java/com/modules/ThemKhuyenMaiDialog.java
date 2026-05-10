package com.modules;

import com.dao.DAO_KhuyenMai;
import com.entity.KhuyenMai;
import com.util.MaTuDong;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ThemKhuyenMaiDialog extends AbstractFormDialog<KhuyenMai> {
    private JTextField txtMaKM, txtTenKM, txtMoTa;
    private DatePickerField dpBatDau, dpKetThuc;
    private JSpinner spTimeBatDau, spTimeKetThuc;
    private JComboBox<String> cboTrangThai;
    private boolean saved = false;
    private final Runnable onSaved;

    public ThemKhuyenMaiDialog(Window owner, Runnable onSaved) {
        super(owner, "Th\u00eam khuy\u1ebfn m\u00e3i m\u1edbi");
        this.onSaved = onSaved;
        installStandardLayout(owner);
    }

    @Override protected String dialogDescription() { return "T\u1ea1o ch\u01b0\u01a1ng tr\u00ecnh khuy\u1ebfn m\u00e3i v\u1edbi th\u1eddi gian hi\u1ec7u l\u1ef1c r\u00f5 r\u00e0ng."; }
    @Override protected LineIcons.Name dialogIcon() { return LineIcons.Name.PROMOTION; }
    @Override protected String primaryButtonText() { return "Th\u00eam khuy\u1ebfn m\u00e3i"; }
    @Override protected int preferredDialogWidth() { return 680; }

    @Override protected FormSchema buildFormSchema() {
        txtMaKM = createReadonlyField(MaTuDong.generate("KM"));
        txtTenKM = createTextField();
        txtMoTa = createTextField();
        dpBatDau = new DatePickerField();
        dpKetThuc = new DatePickerField();
        spTimeBatDau = createTimeSpinner();
        spTimeKetThuc = createTimeSpinner();
        cboTrangThai = new JComboBox<>(new String[]{"\u0110ang \u00e1p d\u1ee5ng", "Ng\u1eebng \u00e1p d\u1ee5ng"});
        // Handoff: date/time kept as DatePickerField + JSpinner, only dialog shell/layout changed.
        // Risk: FormValues cannot read these custom components, so validation and collect read refs directly.
        return FormSchema.builder().columns(2).gap(16,14)
            .field(FieldSpec.of("maKM", "M\u00e3 khuy\u1ebfn m\u00e3i", txtMaKM).grid(0,0).required(true).build())
            .field(FieldSpec.of("trangThai", "Tr\u1ea1ng th\u00e1i", cboTrangThai).grid(0,1).build())
            .field(FieldSpec.of("tenKM", "T\u00ean ch\u01b0\u01a1ng tr\u00ecnh", txtTenKM).grid(1,0).fullWidth().required(true).hint("VD: Gi\u1ea3m gi\u00e1 d\u1ecbp T\u1ebft 2025").build())
            .field(FieldSpec.of("moTa", "M\u00f4 t\u1ea3", txtMoTa).grid(2,0).fullWidth().hint("VD: Gi\u1ea3m gi\u00e1 v\u00e9 t\u00e0u c\u00e1c tuy\u1ebfn").build())
            .field(FieldSpec.of("batDau", "B\u1eaft \u0111\u1ea7u", buildDateTimePanel(dpBatDau, spTimeBatDau)).grid(3,0).required(true).build())
            .field(FieldSpec.of("ketThuc", "K\u1ebft th\u00fac", buildDateTimePanel(dpKetThuc, spTimeKetThuc)).grid(3,1).required(true).build())
            .build();
    }

    @Override protected List<ValidationError> validateForm(FormValues values) {
        List<ValidationError> errors = new ArrayList<>();
        if (txtTenKM.getText().trim().isEmpty()) errors.add(new ValidationError("tenKM", "Vui l\u00f2ng nh\u1eadp t\u00ean ch\u01b0\u01a1ng tr\u00ecnh"));
        LocalDate dateBatDau = dpBatDau.getValue();
        LocalDate dateKetThuc = dpKetThuc.getValue();
        if (dateBatDau == null) errors.add(new ValidationError("batDau", "Vui l\u00f2ng ch\u1ecdn ng\u00e0y b\u1eaft \u0111\u1ea7u"));
        if (dateKetThuc == null) errors.add(new ValidationError("ketThuc", "Vui l\u00f2ng ch\u1ecdn ng\u00e0y k\u1ebft th\u00fac"));
        if (dateBatDau != null && dateKetThuc != null) {
            LocalDateTime batDau = LocalDateTime.of(dateBatDau, readTime(spTimeBatDau));
            LocalDateTime ketThuc = LocalDateTime.of(dateKetThuc, readTime(spTimeKetThuc));
            if (!ketThuc.isAfter(batDau)) errors.add(new ValidationError("ketThuc", "Th\u1eddi gian k\u1ebft th\u00fac ph\u1ea3i sau th\u1eddi gian b\u1eaft \u0111\u1ea7u"));
        }
        return errors;
    }

    @Override protected KhuyenMai collectResult(FormValues values) {
        String moTa = txtMoTa.getText().trim();
        return new KhuyenMai(txtMaKM.getText().trim(), txtTenKM.getText().trim(),
            LocalDateTime.of(dpBatDau.getValue(), readTime(spTimeBatDau)),
            LocalDateTime.of(dpKetThuc.getValue(), readTime(spTimeKetThuc)),
            moTa.isEmpty() ? null : moTa, cboTrangThai.getSelectedIndex() == 0);
    }

    @Override protected void onSubmit(KhuyenMai km) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() { return new DAO_KhuyenMai().insert(km); }
            @Override protected void done() { finishSave(this, "Kh\u00f4ng th\u1ec3 l\u01b0u! M\u00e3 khuy\u1ebfn m\u00e3i c\u00f3 th\u1ec3 \u0111\u00e3 t\u1ed3n t\u1ea1i."); }
        }.execute();
        // Handoff: insert DAO and saved/onSaved behavior are preserved.
        // Risk: base has no submit lock, avoid repeated save clicks while worker is running.
    }

    private void finishSave(SwingWorker<Boolean, Void> worker, String failureMessage) {
        setCursor(Cursor.getDefaultCursor());
        try {
            if (worker.get()) { saved = true; if (onSaved != null) onSaved.run(); dispose(); }
            else NotionMessageDialog.showMessageDialog(this, failureMessage, "L\u1ed7i", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) { NotionMessageDialog.showMessageDialog(this, "L\u1ed7i: " + ex.getMessage(), "L\u1ed7i", JOptionPane.ERROR_MESSAGE); }
    }
    public boolean isSaved() { return saved; }

    protected JSpinner createTimeSpinner() {
        SpinnerDateModel model = new SpinnerDateModel();
        JSpinner spinner = new JSpinner(model);
        spinner.setEditor(new JSpinner.DateEditor(spinner, "HH:mm"));
        spinner.setFont(NotionTheme.BODY);
        spinner.setPreferredSize(new Dimension(82, 40));
        return spinner;
    }

    protected JPanel buildDateTimePanel(DatePickerField picker, JSpinner spinner) {
        JPanel panel = new JPanel(new BorderLayout(6, 0));
        panel.setOpaque(false);
        panel.add(picker, BorderLayout.CENTER);
        panel.add(spinner, BorderLayout.EAST);
        return panel;
    }

    protected LocalTime readTime(JSpinner spinner) {
        try { spinner.commitEdit(); } catch (java.text.ParseException ignored) {}
        Calendar cal = Calendar.getInstance();
        cal.setTime((java.util.Date) spinner.getValue());
        return LocalTime.of(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
    }

    protected JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(NotionTheme.BODY);
        field.setForeground(NotionTheme.TEXT);
        return field;
    }

    protected JTextField createReadonlyField(String value) {
        JTextField field = createTextField();
        field.setText(value == null ? "" : value);
        field.setEditable(false);
        field.setFont(NotionTheme.BODY_BOLD);
        return field;
    }
}
