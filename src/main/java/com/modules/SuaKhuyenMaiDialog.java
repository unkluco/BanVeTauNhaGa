package com.modules;

import com.dao.DAO_KhuyenMai;
import com.entity.KhuyenMai;
import com.util.MaTuDong;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SuaKhuyenMaiDialog extends AbstractFormDialog<KhuyenMai> {
    private JTextField txtMaKM, txtTenKM, txtMoTa;
    private DatePickerField dpBatDau, dpKetThuc;
    private JSpinner spTimeBatDau, spTimeKetThuc;
    private JComboBox<String> cboTrangThai;
    private boolean saved = false;
    private final Runnable onSaved;
    private final KhuyenMai khuyenMai;

    public SuaKhuyenMaiDialog(Window owner, KhuyenMai khuyenMai, Runnable onSaved) {
        super(owner, "Ch\u1ec9nh s\u1eeda khuy\u1ebfn m\u00e3i");
        this.khuyenMai = khuyenMai;
        this.onSaved = onSaved;
        installStandardLayout(owner);
        populateFields();
    }

    @Override protected String dialogDescription() { return "C\u1eadp nh\u1eadt th\u00f4ng tin v\u00e0 th\u1eddi gian hi\u1ec7u l\u1ef1c c\u1ee7a khuy\u1ebfn m\u00e3i."; }
    @Override protected LineIcons.Name dialogIcon() { return LineIcons.Name.PROMOTION; }
    @Override protected String primaryButtonText() { return "C\u1eadp nh\u1eadt"; }
    @Override protected int preferredDialogWidth() { return 680; }

    @Override protected FormSchema buildFormSchema() {
        txtMaKM = createReadonlyField(khuyenMai.getMaKhuyenMai());
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


    private void populateFields() {
        txtTenKM.setText(khuyenMai.getTenKhuyenMai() == null ? "" : khuyenMai.getTenKhuyenMai());
        txtMoTa.setText(khuyenMai.getMoTa() == null ? "" : khuyenMai.getMoTa());
        if (khuyenMai.getThoiGianBatDau() != null) {
            LocalDateTime batDau = khuyenMai.getThoiGianBatDau();
            dpBatDau.setValue(batDau.toLocalDate());
            setSpinnerTime(spTimeBatDau, batDau.getHour(), batDau.getMinute());
        }
        if (khuyenMai.getThoiGianKetThuc() != null) {
            LocalDateTime ketThuc = khuyenMai.getThoiGianKetThuc();
            dpKetThuc.setValue(ketThuc.toLocalDate());
            setSpinnerTime(spTimeKetThuc, ketThuc.getHour(), ketThuc.getMinute());
        }
        cboTrangThai.setSelectedIndex(khuyenMai.isTrangThai() ? 0 : 1);
    }

    private void setSpinnerTime(JSpinner spinner, int hour, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        spinner.setValue(cal.getTime());
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
        return new KhuyenMai(khuyenMai.getMaKhuyenMai(), txtTenKM.getText().trim(),
                LocalDateTime.of(dpBatDau.getValue(), readTime(spTimeBatDau)),
                LocalDateTime.of(dpKetThuc.getValue(), readTime(spTimeKetThuc)),
                moTa.isEmpty() ? null : moTa,
                cboTrangThai.getSelectedIndex() == 0);
    }

    @Override protected void onSubmit(KhuyenMai km) {
        DAO_KhuyenMai dao = new DAO_KhuyenMai();
        int usageCount = dao.countAppliedUsage(km.getMaKhuyenMai());
        boolean contentChanged = hasContentChanged(km);
        boolean cloneShouldActivate = false;
        if (usageCount > 0 && contentChanged) {
            if (!confirmCloneUsedKhuyenMai(usageCount)) return;
            cloneShouldActivate = confirmActivateClonedKhuyenMai();
        }
        final boolean shouldClone = usageCount > 0 && contentChanged;
        final boolean activateClone = cloneShouldActivate;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<KhuyenMai, Void>() {
            @Override protected KhuyenMai doInBackground() {
                if (shouldClone) return dao.cloneKhuyenMaiWithDetails(km, activateClone);
                return dao.update(km) ? km : null;
            }
            @Override protected void done() { finishSave(this, shouldClone, "Kh\u00f4ng th\u1ec3 c\u1eadp nh\u1eadt khuy\u1ebfn m\u00e3i!"); }
        }.execute();
        // Handoff: KM đã áp dụng sẽ clone khi sửa nội dung; bật/tắt trạng thái update trực tiếp.
        // Risk: booking đang mở trước clone có thể vẫn giữ CTKM cũ trong context cho tới khi reload bước KM.
    }

    private boolean confirmCloneUsedKhuyenMai(int usageCount) {
        int choice = NotionMessageDialog.showConfirmDialog(this,
                "Khuyến mãi này đã được áp dụng trên " + usageCount + " hóa đơn nên không thể sửa trực tiếp.\n\n"
                        + "Bạn có muốn nhân bản khuyến mãi này với chỉnh sửa vừa nhập không?\n"
                        + "Bản cũ sẽ ngừng áp dụng, bản mới sẽ hỏi xác nhận hoạt động ở bước tiếp theo.",
                "Khuyến mãi đã khóa", JOptionPane.WARNING_MESSAGE, "Không tạo", "Tạo bản mới");
        return choice == JOptionPane.YES_OPTION;
    }

    private boolean confirmActivateClonedKhuyenMai() {
        int choice = NotionMessageDialog.showConfirmDialog(this,
                "Bạn có muốn bật hoạt động cho khuyến mãi mới vừa nhân bản không?\n"
                        + "Nếu hủy, khuyến mãi mới vẫn được tạo nhưng ở trạng thái ngừng áp dụng.",
                "Bật khuyến mãi mới", JOptionPane.QUESTION_MESSAGE, "Không bật", "Bật hoạt động");
        return choice == JOptionPane.YES_OPTION;
    }

    private boolean hasContentChanged(KhuyenMai km) {
        return !java.util.Objects.equals(khuyenMai.getTenKhuyenMai(), km.getTenKhuyenMai())
                || !java.util.Objects.equals(khuyenMai.getMoTa(), km.getMoTa())
                || !java.util.Objects.equals(normalizeMinute(khuyenMai.getThoiGianBatDau()), normalizeMinute(km.getThoiGianBatDau()))
                || !java.util.Objects.equals(normalizeMinute(khuyenMai.getThoiGianKetThuc()), normalizeMinute(km.getThoiGianKetThuc()));
    }

    private LocalDateTime normalizeMinute(LocalDateTime value) {
        return value == null ? null : value.truncatedTo(ChronoUnit.MINUTES);
    }

    private void finishSave(SwingWorker<KhuyenMai, Void> worker, boolean cloned, String failureMessage) {
        setCursor(Cursor.getDefaultCursor());
        try {
            KhuyenMai savedKm = worker.get();
            if (savedKm != null) {
                saved = true;
                if (!cloned) copyBack();
                if (onSaved != null) onSaved.run();
                if (cloned) {
                    NotionMessageDialog.showMessageDialog(this,
                            "Đã ngừng khuyến mãi cũ và tạo khuyến mãi mới: " + savedKm.getMaKhuyenMai()
                                    + (savedKm.isTrangThai() ? " (đang hoạt động)." : " (chưa hoạt động)."),
                            "Đã nhân bản khuyến mãi", JOptionPane.INFORMATION_MESSAGE);
                }
                dispose();
            }
            else NotionMessageDialog.showMessageDialog(this, failureMessage, "L\u1ed7i", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) { NotionMessageDialog.showMessageDialog(this, "L\u1ed7i: " + ex.getMessage(), "L\u1ed7i", JOptionPane.ERROR_MESSAGE); }
    }

    private void copyBack() {
        String moTa = txtMoTa.getText().trim();
        khuyenMai.setTenKhuyenMai(txtTenKM.getText().trim());
        khuyenMai.setMoTa(moTa.isEmpty() ? null : moTa);
        khuyenMai.setThoiGianBatDau(LocalDateTime.of(dpBatDau.getValue(), readTime(spTimeBatDau)));
        khuyenMai.setThoiGianKetThuc(LocalDateTime.of(dpKetThuc.getValue(), readTime(spTimeKetThuc)));
        khuyenMai.setTrangThai(cboTrangThai.getSelectedIndex() == 0);
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
