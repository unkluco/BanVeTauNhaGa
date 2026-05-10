package com.modules;

import com.dao.DAO_DoanTau;
import com.dao.DAO_Lich;
import com.dao.DAO_Tuyen;
import com.entity.DoanTau;
import com.entity.Lich;
import com.entity.Tuyen;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.List;

public class ChinhSuaLichChayDialog extends AbstractFormDialog<Lich> {
    private final boolean isEditMode;
    private final Lich original;
    private final Runnable onSaved;

    private final DAO_Lich daoLich = new DAO_Lich();
    private final DAO_Tuyen daoTuyen = new DAO_Tuyen();
    private final DAO_DoanTau daoDoanTau = new DAO_DoanTau();

    private JTextField txtMaLich;
    private JComboBox<Tuyen> cboTuyen;
    private JComboBox<DoanTau> cboDoanTau;
    private DatePickerField dpBatDau;
    private JSpinner timePickerBatDau;
    private JTextField txtThoiGianChay;
    private JComboBox<String> cboTrangThai;

    public ChinhSuaLichChayDialog(Window owner, Lich lich, Runnable onSaved) {
        super(owner, lich == null ? "Th\u00eam l\u1ecbch ch\u1ea1y" : "Ch\u1ec9nh s\u1eeda l\u1ecbch ch\u1ea1y");
        this.original = lich;
        this.isEditMode = lich != null;
        this.onSaved = onSaved;
        installStandardLayout(owner);
    }

    @Override
    protected String dialogTitle() {
        return isEditMode ? "Ch\u1ec9nh s\u1eeda l\u1ecbch ch\u1ea1y" : "Th\u00eam l\u1ecbch ch\u1ea1y";
    }

    @Override
    protected String dialogDescription() {
        return isEditMode
                ? "C\u1eadp nh\u1eadt tuy\u1ebfn, \u0111o\u00e0n t\u00e0u v\u00e0 th\u1eddi gian kh\u1edfi h\u00e0nh."
                : "T\u1ea1o l\u1ecbch ch\u1ea1y m\u1edbi cho tuy\u1ebfn v\u00e0 \u0111o\u00e0n t\u00e0u.";
    }

    @Override
    protected LineIcons.Name dialogIcon() {
        return LineIcons.Name.CALENDAR;
    }

    @Override
    protected String primaryButtonText() {
        return isEditMode ? "C\u1eadp nh\u1eadt l\u1ecbch" : "Th\u00eam l\u1ecbch";
    }

    @Override
    protected int preferredDialogWidth() {
        return 760;
    }

    @Override
    protected FormSchema buildFormSchema() {
        txtMaLich = createTextField();
        if (isEditMode) {
            txtMaLich.setText(original.getMaLich());
            txtMaLich.setEditable(false);
            txtMaLich.setFont(NotionTheme.BODY_BOLD);
        }

        cboTuyen = createTuyenCombo();
        cboDoanTau = createDoanTauCombo();
        dpBatDau = new DatePickerField();
        timePickerBatDau = createTimeSpinner();
        txtThoiGianChay = createTextField();
        cboTrangThai = new JComboBox<>(new String[]{"Ho\u1ea1t \u0111\u1ed9ng", "Ng\u1eebng ho\u1ea1t \u0111\u1ed9ng"});

        loadComboData();
        if (isEditMode) prefillFields();

        // Handoff: schedule dialog now uses shared form chrome; route/train/date business logic remains here.
        // Risk: DatePickerField and JSpinner are custom components, so validate/collect read direct refs.
        return FormSchema.builder()
                .columns(2)
                .gap(16, 14)
                .field(FieldSpec.of("maLich", "M\u00e3 l\u1ecbch ch\u1ea1y", txtMaLich)
                        .grid(0, 0).required(!isEditMode)
                        .hint(isEditMode ? "Kh\u00f4ng th\u1ec3 thay \u0111\u1ed5i m\u00e3 l\u1ecbch." : null)
                        .build())
                .field(FieldSpec.of("trangThai", "Tr\u1ea1ng th\u00e1i", cboTrangThai)
                        .grid(0, 1).build())
                .field(FieldSpec.of("tuyen", "Tuy\u1ebfn \u0111\u01b0\u1eddng", cboTuyen)
                        .grid(1, 0).required(true).build())
                .field(FieldSpec.of("doanTau", "\u0110o\u00e0n t\u00e0u", cboDoanTau)
                        .grid(1, 1).required(true).build())
                .field(FieldSpec.of("batDau", "Th\u1eddi gian b\u1eaft \u0111\u1ea7u", buildDateTimePanel())
                        .grid(2, 0).required(true).build())
                .field(FieldSpec.of("thoiGianChay", "Th\u1eddi gian ch\u1ea1y", txtThoiGianChay)
                        .grid(2, 1).required(true).hint("Nh\u1eadp s\u1ed1 ph\u00fat ch\u1ea1y.").build())
                .build();
    }

    private void loadComboData() {
        List<Tuyen> dsTuyen = daoTuyen.getAll();
        List<DoanTau> dsDoanTau = daoDoanTau.getAllActive();
        for (Tuyen tuyen : dsTuyen) cboTuyen.addItem(tuyen);
        for (DoanTau doanTau : dsDoanTau) cboDoanTau.addItem(doanTau);
    }

    private void prefillFields() {
        if (original.getTuyen() != null) {
            boolean found = false;
            for (int i = 0; i < cboTuyen.getItemCount(); i++) {
                if (cboTuyen.getItemAt(i).getMaTuyen().equals(original.getTuyen().getMaTuyen())) {
                    cboTuyen.setSelectedIndex(i);
                    found = true;
                    break;
                }
            }
            if (!found) {
                Tuyen originalTuyen = daoTuyen.findById(original.getTuyen().getMaTuyen());
                if (originalTuyen == null) originalTuyen = original.getTuyen();
                cboTuyen.addItem(originalTuyen);
                cboTuyen.setSelectedItem(originalTuyen);
            }
        }
        if (original.getDoanTau() != null) {
            boolean found = false;
            for (int i = 0; i < cboDoanTau.getItemCount(); i++) {
                if (cboDoanTau.getItemAt(i).getMaDoanTau().equals(original.getDoanTau().getMaDoanTau())) {
                    cboDoanTau.setSelectedIndex(i);
                    found = true;
                    break;
                }
            }
            if (!found) {
                DoanTau originalDoanTau = daoDoanTau.findById(original.getDoanTau().getMaDoanTau());
                if (originalDoanTau == null) originalDoanTau = original.getDoanTau();
                cboDoanTau.addItem(originalDoanTau);
                cboDoanTau.setSelectedItem(originalDoanTau);
            }
        }
        if (original.getThoiGianBatDau() != null) {
            LocalDateTime batDau = original.getThoiGianBatDau();
            dpBatDau.setValue(batDau.toLocalDate());
            setSpinnerTime(timePickerBatDau, batDau.getHour(), batDau.getMinute());
        }
        txtThoiGianChay.setText(original.getThoiGianChay() == null ? "" : original.getThoiGianChay());
        cboTrangThai.setSelectedIndex(original.isHoatDong() ? 0 : 1);
    }

    @Override
    protected List<ValidationError> validateForm(FormValues values) {
        java.util.ArrayList<ValidationError> errors = new java.util.ArrayList<>();
        if (!isEditMode && txtMaLich.getText().trim().isEmpty()) {
            errors.add(new ValidationError("maLich", "Vui l\u00f2ng nh\u1eadp m\u00e3 l\u1ecbch"));
        }
        if (cboTuyen.getSelectedItem() == null) {
            errors.add(new ValidationError("tuyen", "Vui l\u00f2ng ch\u1ecdn tuy\u1ebfn"));
        }
        if (cboDoanTau.getSelectedItem() == null) {
            errors.add(new ValidationError("doanTau", "Vui l\u00f2ng ch\u1ecdn \u0111o\u00e0n t\u00e0u"));
        }
        if (dpBatDau.getValue() == null) {
            errors.add(new ValidationError("batDau", "Vui l\u00f2ng ch\u1ecdn ng\u00e0y b\u1eaft \u0111\u1ea7u"));
        }
        String chayStr = txtThoiGianChay.getText().trim();
        if (chayStr.isEmpty()) {
            errors.add(new ValidationError("thoiGianChay", "Vui l\u00f2ng nh\u1eadp th\u1eddi gian ch\u1ea1y"));
        } else {
            try {
                int mins = Integer.parseInt(chayStr);
                if (mins <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                errors.add(new ValidationError("thoiGianChay", "Vui l\u00f2ng nh\u1eadp s\u1ed1 ph\u00fat h\u1ee3p l\u1ec7 (> 0)"));
            }
        }
        return errors;
    }

    @Override
    protected Lich collectResult(FormValues values) {
        LocalDate dateVal = dpBatDau.getValue();
        LocalDateTime batDau = LocalDateTime.of(dateVal, readTime(timePickerBatDau));
        Lich lich = new Lich(isEditMode ? original.getMaLich() : txtMaLich.getText().trim(),
                (Tuyen) cboTuyen.getSelectedItem(),
                (DoanTau) cboDoanTau.getSelectedItem(),
                batDau,
                txtThoiGianChay.getText().trim());
        lich.setHoatDong(cboTrangThai.getSelectedIndex() == 0);
        return lich;
    }

    @Override
    protected void onSubmit(Lich lich) {
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
                        NotionMessageDialog.showMessageDialog(ChinhSuaLichChayDialog.this,
                                "Kh\u00f4ng th\u1ec3 l\u01b0u l\u1ecbch. Ki\u1ec3m tra m\u00e3 c\u00f3 b\u1ecb tr\u00f9ng kh\u00f4ng.",
                                "L\u1ed7i", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    NotionMessageDialog.showMessageDialog(ChinhSuaLichChayDialog.this,
                            "L\u1ed7i: " + ex.getMessage(), "L\u1ed7i", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
        // Handoff: insert/update still runs through DAO_Lich inside SwingWorker as before.
        // Risk: no schedule-conflict validation is added here; this preserves prior behavior exactly.
    }

    private JComboBox<Tuyen> createTuyenCombo() {
        JComboBox<Tuyen> combo = new JComboBox<>();
        combo.setRenderer(buildTuyenRenderer());
        return combo;
    }

    private JComboBox<DoanTau> createDoanTauCombo() {
        JComboBox<DoanTau> combo = new JComboBox<>();
        combo.setRenderer(buildDoanTauRenderer());
        return combo;
    }

    private JPanel buildDateTimePanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 0));
        panel.setOpaque(false);
        panel.add(dpBatDau, BorderLayout.CENTER);
        panel.add(timePickerBatDau, BorderLayout.EAST);
        return panel;
    }

    private JSpinner createTimeSpinner() {
        SpinnerDateModel model = new SpinnerDateModel();
        JSpinner spinner = new JSpinner(model);
        spinner.setEditor(new JSpinner.DateEditor(spinner, "HH:mm"));
        spinner.setFont(NotionTheme.BODY);
        spinner.setPreferredSize(new Dimension(82, 40));
        return spinner;
    }

    private void setSpinnerTime(JSpinner spinner, int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        spinner.setValue(calendar.getTime());
    }

    private LocalTime readTime(JSpinner spinner) {
        try { spinner.commitEdit(); } catch (java.text.ParseException ignored) {}
        Calendar calendar = Calendar.getInstance();
        calendar.setTime((java.util.Date) spinner.getValue());
        return LocalTime.of(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
    }

    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(NotionTheme.BODY);
        field.setForeground(NotionTheme.TEXT);
        return field;
    }

    private DefaultListCellRenderer buildTuyenRenderer() {
        return new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object val,
                    int idx, boolean sel, boolean focus) {
                super.getListCellRendererComponent(list, val, idx, sel, focus);
                if (val instanceof Tuyen t) {
                    String gaDi = t.getGaDi() != null ? t.getGaDi().getTenGa() : t.getMaTuyen();
                    String gaDen = t.getGaDen() != null ? t.getGaDen().getTenGa() : "";
                    setText(t.getMaTuyen() + "  \u2014  " + gaDi + (gaDen.isEmpty() ? "" : " \u2192 " + gaDen));
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
}
