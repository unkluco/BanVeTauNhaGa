package com.modules;

import com.dao.DAO_KhuyenMai;
import com.entity.KhuyenMai;
import com.util.MaTuDong;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class KhuyenMaiDialog extends AbstractFormDialog<KhuyenMai> {
    private JTextField txtMaKM, txtTenKM, txtMoTa;
    private DatePickerField dpBatDau, dpKetThuc;
    private JComboBox<String> cboTrangThai;
    private boolean saved = false;
    private final Runnable onSaved;
    private final KhuyenMai khuyenMai;
    private final boolean editMode;

    public KhuyenMaiDialog(Window owner, Runnable onSaved) {
        this(owner, null, onSaved);
    }

    public KhuyenMaiDialog(Window owner, KhuyenMai khuyenMai, Runnable onSaved) {
        super(owner, khuyenMai == null ? "Thêm khuyến mãi mới" : "Chỉnh sửa khuyến mãi");
        this.khuyenMai = khuyenMai;
        this.editMode = khuyenMai != null;
        this.onSaved = onSaved;
        installStandardLayout(owner);
        if (editMode) populateFields();
    }

    @Override protected String dialogDescription() {
        return editMode
                ? "Cập nhật thông tin và thời gian hiệu lực của khuyến mãi."
                : "Tạo chương trình khuyến mãi với thời gian hiệu lực rõ ràng.";
    }

    @Override protected LineIcons.Name dialogIcon() { return LineIcons.Name.PROMOTION; }
    @Override protected String primaryButtonText() { return editMode ? "Cập nhật" : "Thêm khuyến mãi"; }
    @Override protected int preferredDialogWidth() { return 680; }

    @Override protected FormSchema buildFormSchema() {
        txtMaKM = createReadonlyField(editMode ? khuyenMai.getMaKhuyenMai() : MaTuDong.generate("KM"));
        txtTenKM = createTextField();
        txtMoTa = createTextField();
        dpBatDau = new DatePickerField();
        dpKetThuc = new DatePickerField();
        cboTrangThai = new JComboBox<>(new String[]{"Đang áp dụng", "Ngừng áp dụng"});
        // Handoff: form dùng chung cho thêm/sửa, ngày vẫn đọc trực tiếp từ DatePickerField.
        // Risk: FormValues không đọc DatePickerField nên validate/collect không được đổi sang values.
        return FormSchema.builder().columns(2).gap(16,14)
            .field(FieldSpec.of("maKM", "Mã khuyến mãi", txtMaKM).grid(0,0).required(true).build())
            .field(FieldSpec.of("trangThai", "Trạng thái", cboTrangThai).grid(0,1).build())
            .field(FieldSpec.of("tenKM", "Tên chương trình", txtTenKM).grid(1,0).fullWidth().required(true).hint("VD: Giảm giá dịp Tết 2025").build())
            .field(FieldSpec.of("moTa", "Mô tả", txtMoTa).grid(2,0).fullWidth().hint("VD: Giảm giá vé tàu các tuyến").build())
            .field(FieldSpec.of("batDau", "Bắt đầu", dpBatDau).grid(3,0).required(true).build())
            .field(FieldSpec.of("ketThuc", "Kết thúc", dpKetThuc).grid(3,1).required(true).build())
            .build();
    }

    private void populateFields() {
        txtTenKM.setText(khuyenMai.getTenKhuyenMai() == null ? "" : khuyenMai.getTenKhuyenMai());
        txtMoTa.setText(khuyenMai.getMoTa() == null ? "" : khuyenMai.getMoTa());
        if (khuyenMai.getThoiGianBatDau() != null) dpBatDau.setValue(khuyenMai.getThoiGianBatDau());
        if (khuyenMai.getThoiGianKetThuc() != null) dpKetThuc.setValue(khuyenMai.getThoiGianKetThuc());
        cboTrangThai.setSelectedIndex(khuyenMai.isTrangThai() ? 0 : 1);
        // Handoff: edit mode mutates the passed KhuyenMai only after DAO update succeeds.
        // Risk: clone path intentionally leaves current object unchanged because it represents the old KM.
    }

    @Override protected List<ValidationError> validateForm(FormValues values) {
        List<ValidationError> errors = new ArrayList<>();
        if (txtTenKM.getText().trim().isEmpty()) errors.add(new ValidationError("tenKM", "Vui lòng nhập tên chương trình"));
        LocalDate dateBatDau = dpBatDau.getValue();
        LocalDate dateKetThuc = dpKetThuc.getValue();
        if (dateBatDau == null) errors.add(new ValidationError("batDau", "Vui lòng chọn ngày bắt đầu"));
        if (dateKetThuc == null) errors.add(new ValidationError("ketThuc", "Vui lòng chọn ngày kết thúc"));
        if (dateBatDau != null && dateKetThuc != null && dateKetThuc.isBefore(dateBatDau)) {
            errors.add(new ValidationError("ketThuc", "Ngày kết thúc phải sau hoặc bằng ngày bắt đầu"));
        }
        return errors;
    }

    @Override protected KhuyenMai collectResult(FormValues values) {
        String moTa = txtMoTa.getText().trim();
        return new KhuyenMai(txtMaKM.getText().trim(), txtTenKM.getText().trim(),
                dpBatDau.getValue(), dpKetThuc.getValue(),
                moTa.isEmpty() ? null : moTa, cboTrangThai.getSelectedIndex() == 0);
        // Handoff: KhuyenMai lưu LocalDate trực tiếp, không quy đổi giờ/phút ẩn.
        // Risk: nếu DB cũ còn DATETIME thì DAO getDate sẽ cắt phần giờ khi đọc.
    }

    @Override protected void onSubmit(KhuyenMai km) {
        if (editMode) submitEdit(km);
        else submitCreate(km);
    }

    private void submitCreate(KhuyenMai km) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<KhuyenMai, Void>() {
            @Override protected KhuyenMai doInBackground() { return new DAO_KhuyenMai().insert(km) ? km : null; }
            @Override protected void done() { finishSave(this, false, "Không thể lưu! Mã khuyến mãi có thể đã tồn tại."); }
        }.execute();
        // Handoff: create mode preserves insert DAO and saved/onSaved callback behavior.
        // Risk: base has no submit lock, avoid repeated save clicks while worker is running.
    }

    private void submitEdit(KhuyenMai km) {
        DAO_KhuyenMai dao = new DAO_KhuyenMai();
        int usageCount = dao.countAppliedUsage(km.getMaKhuyenMai());
        boolean contentChanged = hasContentChanged(km);
        boolean cloneShouldActivate = false;
        String cloneTenKhuyenMai = null;
        if (usageCount > 0 && contentChanged) {
            if (!confirmCloneUsedKhuyenMai(usageCount)) return;
            cloneTenKhuyenMai = promptCloneTenKhuyenMai(khuyenMai.getTenKhuyenMai());
            if (cloneTenKhuyenMai == null) return;
            cloneShouldActivate = confirmActivateClonedKhuyenMai();
        }
        final boolean shouldClone = usageCount > 0 && contentChanged;
        final boolean activateClone = cloneShouldActivate;
        final String newCloneTenKhuyenMai = cloneTenKhuyenMai;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<KhuyenMai, Void>() {
            @Override protected KhuyenMai doInBackground() {
                if (shouldClone) return dao.cloneKhuyenMaiWithDetails(km, activateClone, newCloneTenKhuyenMai);
                return dao.update(km) ? km : null;
            }
            @Override protected void done() { finishSave(this, shouldClone, "Không thể cập nhật khuyến mãi!"); }
        }.execute();
        // Handoff: used KM clones on content changes; status-only edits still update old row directly.
        // Risk: booking open before clone can retain old CTKM context until its KM step reloads.
    }

    private boolean confirmCloneUsedKhuyenMai(int usageCount) {
        int choice = NotionMessageDialog.showConfirmDialog(this,
                "Khuyến mãi này đã được áp dụng trên " + usageCount + " hóa đơn nên không thể sửa trực tiếp.\n\n"
                        + "Bạn có muốn nhân bản khuyến mãi này với chỉnh sửa vừa nhập không?\n"
                        + "Bản cũ sẽ ngừng áp dụng, bản mới sẽ hỏi xác nhận hoạt động ở bước tiếp theo.",
                "Khuyến mãi đã khóa", JOptionPane.WARNING_MESSAGE, "Không tạo", "Tạo bản mới");
        return choice == JOptionPane.YES_OPTION;
    }

    private String promptCloneTenKhuyenMai(String defaultTenKhuyenMai) {
        JTextField field = createTextField();
        field.setText(defaultTenKhuyenMai == null ? "" : defaultTenKhuyenMai);
        field.selectAll();
        while (true) {
            int choice = NotionMessageDialog.showConfirmDialog(this,
                    new Object[]{"Nhập tên cho khuyến mãi mới:", field},
                    "Tên khuyến mãi mới", JOptionPane.QUESTION_MESSAGE, "Hủy", "Tiếp tục");
            if (choice != JOptionPane.YES_OPTION) return null;
            String value = field.getText().trim();
            if (!value.isEmpty()) return value;
            NotionMessageDialog.showMessageDialog(this,
                    "Vui lòng nhập tên khuyến mãi mới.", "Thiếu tên khuyến mãi", JOptionPane.WARNING_MESSAGE);
        }
        // Handoff: hỏi tên ngay sau xác nhận clone để bản mới không bị trùng tên bản gốc.
        // Risk: hủy tại bước này sẽ dừng toàn bộ thao tác clone, không lưu thay đổi đang nhập.
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
                || !java.util.Objects.equals(khuyenMai.getThoiGianBatDau(), km.getThoiGianBatDau())
                || !java.util.Objects.equals(khuyenMai.getThoiGianKetThuc(), km.getThoiGianKetThuc());
    }

    private void finishSave(SwingWorker<KhuyenMai, Void> worker, boolean cloned, String failureMessage) {
        setCursor(Cursor.getDefaultCursor());
        try {
            KhuyenMai savedKm = worker.get();
            if (savedKm != null) {
                saved = true;
                if (editMode && !cloned) copyBack();
                if (onSaved != null) onSaved.run();
                if (cloned) {
                    NotionMessageDialog.showMessageDialog(this,
                            "Đã ngừng khuyến mãi cũ và tạo khuyến mãi mới: " + savedKm.getMaKhuyenMai()
                                    + (savedKm.isTrangThai() ? " (đang hoạt động)." : " (chưa hoạt động)."),
                            "Đã nhân bản khuyến mãi", JOptionPane.INFORMATION_MESSAGE);
                }
                dispose();
            }
            else NotionMessageDialog.showMessageDialog(this, failureMessage, "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) { NotionMessageDialog.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE); }
    }

    private void copyBack() {
        String moTa = txtMoTa.getText().trim();
        khuyenMai.setTenKhuyenMai(txtTenKM.getText().trim());
        khuyenMai.setMoTa(moTa.isEmpty() ? null : moTa);
        khuyenMai.setThoiGianBatDau(dpBatDau.getValue());
        khuyenMai.setThoiGianKetThuc(dpKetThuc.getValue());
        khuyenMai.setTrangThai(cboTrangThai.getSelectedIndex() == 0);
    }

    public boolean isSaved() { return saved; }

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
