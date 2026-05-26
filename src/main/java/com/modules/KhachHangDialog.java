package com.modules;

import com.dao.DAO_KhachHang;
import com.entity.KhachHang;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class KhachHangDialog extends AbstractFormDialog<KhachHang> {
    private final Mode mode;
    private final KhachHang original;
    private final Runnable onSaved;

    private JTextField txtMaKH;
    private JTextField txtHoTen;
    private JTextField txtSoDienThoai;
    private JTextField txtCccd;
    private JTextField txtEmail;
    private JTextField txtDiaChiThuongTru;
    private JTextField txtDiaChiTamTru;
    private JTextField txtQuocTich;
    private DatePickerField dpNgaySinh;
    private JComboBox<String> cboGioiTinh;
    private boolean saved = false;
    private Consumer<Object> callback;

    private enum Mode { CREATE, EDIT }

    public static KhachHangDialog create(Window owner, Runnable onSaved) {
        return new KhachHangDialog(owner, Mode.CREATE, null, onSaved);
    }

    public static KhachHangDialog edit(Window owner, KhachHang kh, Runnable onSaved) {
        return new KhachHangDialog(owner, Mode.EDIT, kh, onSaved);
    }

    private KhachHangDialog(Window owner, Mode mode, KhachHang original, Runnable onSaved) {
        super(owner, mode == Mode.CREATE ? "Thêm khách hàng" : "Chỉnh sửa khách hàng");
        this.mode = mode;
        this.original = original;
        this.onSaved = onSaved;
        installStandardLayout(owner);
        if (isEditMode()) populateFields();
    }

    @Override protected String dialogDescription() {
        return isCreateMode()
                ? "Tạo hồ sơ khách hàng với thông tin liên hệ cơ bản."
                : "Cập nhật thông tin liên hệ và hồ sơ khách hàng.";
    }

    @Override protected LineIcons.Name dialogIcon() { return LineIcons.Name.USER; }
    @Override protected String primaryButtonText() { return isCreateMode() ? "Thêm khách hàng" : "Cập nhật"; }
    @Override protected int preferredDialogWidth() { return 760; }

    @Override protected FormSchema buildFormSchema() {
        txtMaKH = createReadonlyField(isCreateMode() ? com.util.MaTuDong.generate("KH") : original.getMaKhachHang());
        txtHoTen = createTextField();
        txtSoDienThoai = createTextField();
        txtCccd = createTextField();
        txtEmail = createTextField();
        txtDiaChiThuongTru = createTextField();
        txtDiaChiTamTru = createTextField();
        txtQuocTich = createTextField();
        txtQuocTich.setText("Việt Nam");
        dpNgaySinh = new DatePickerField();
        cboGioiTinh = new JComboBox<>(new String[]{"-- Không chọn --", "Nam", "Nữ"});

        return FormSchema.builder().columns(2).gap(16, 14)
                .field(FieldSpec.of("maKH", "Mã khách hàng", txtMaKH).grid(0, 0).required(true).build())
                .field(FieldSpec.of("gioiTinh", "Giới tính", cboGioiTinh).grid(0, 1).build())
                .field(FieldSpec.of("hoTen", "Họ tên", txtHoTen).grid(1, 0).required(true).build())
                .field(FieldSpec.of("sdt", "Số điện thoại", txtSoDienThoai).grid(1, 1).required(true).build())
                .field(FieldSpec.of("cccd", "CCCD", txtCccd).grid(2, 0).required(true).build())
                .field(FieldSpec.of("email", "Email", txtEmail).grid(2, 1).build())
                .field(FieldSpec.of("ngaySinh", "Ngày sinh", dpNgaySinh).grid(3, 0).build())
                .field(FieldSpec.of("quocTich", "Quốc tịch", txtQuocTich).grid(3, 1).build())
                .field(FieldSpec.of("thuongTru", "Địa chỉ thường trú", txtDiaChiThuongTru).grid(4, 0).fullWidth().build())
                .field(FieldSpec.of("tamTru", "Địa chỉ tạm trú", txtDiaChiTamTru).grid(5, 0).fullWidth().build())
                .build();
        // Handoff: one schema supports both create and edit, with only mã KH source changing by mode.
        // Risk: create mode still generates KH during dialog construction, not at save time.
    }

    private void populateFields() {
        txtHoTen.setText(valueOrEmpty(original.getHoTen()));
        txtSoDienThoai.setText(valueOrEmpty(original.getSoDienThoai()));
        txtCccd.setText(valueOrEmpty(original.getCccd()));
        txtEmail.setText(valueOrEmpty(original.getEmail()));
        txtDiaChiThuongTru.setText(valueOrEmpty(original.getDiaChiThuongTru()));
        txtDiaChiTamTru.setText(valueOrEmpty(original.getDiaChiTamTru()));
        txtQuocTich.setText(valueOrEmpty(original.getQuocTich()));
        if (original.getNgaySinh() != null) dpNgaySinh.setValue(original.getNgaySinh());
        if ("NAM".equals(original.getGioiTinh())) cboGioiTinh.setSelectedIndex(1);
        else if ("NU".equals(original.getGioiTinh())) cboGioiTinh.setSelectedIndex(2);
        // Handoff: edit mode mutates the original KhachHang instance to preserve caller references.
        // Risk: fields are populated after AbstractFormDialog builds components; keep constructor order intact.
    }

    @Override protected List<ValidationError> validateForm(FormValues values) {
        List<ValidationError> errors = new ArrayList<>();
        String hoTen = text(txtHoTen);
        String sdt = text(txtSoDienThoai);
        String cccd = text(txtCccd);
        String email = text(txtEmail);

        if (hoTen.isEmpty()) errors.add(new ValidationError("hoTen", "Vui lòng nhập họ và tên"));
        if (sdt.isEmpty()) errors.add(new ValidationError("sdt", "Vui lòng nhập số điện thoại"));
        else if (!sdt.matches("\\d{10,11}")) errors.add(new ValidationError("sdt", "Số điện thoại phải có 10-11 chữ số"));
        if (cccd.isEmpty()) errors.add(new ValidationError("cccd", "Vui lòng nhập số CCCD"));
        else if (!cccd.matches("\\d{12}")) errors.add(new ValidationError("cccd", "Số CCCD phải có đúng 12 chữ số"));
        if (!email.isEmpty() && !email.matches("[\\w.+\\-]+@[\\w\\-]+(\\.[\\w\\-]+)*\\.[a-zA-Z]{2,}")) errors.add(new ValidationError("email", "Email không hợp lệ"));

        if (errors.isEmpty()) validateDuplicates(errors, sdt, cccd, email);
        return errors;
        // Handoff: duplicate validation excludes original mã KH only in edit mode.
        // Risk: DAO duplicate helpers must keep null excludeId semantics for create mode.
    }

    private void validateDuplicates(List<ValidationError> errors, String sdt, String cccd, String email) {
        DAO_KhachHang daoCheck = new DAO_KhachHang();
        String excludeId = isEditMode() ? original.getMaKhachHang() : null;
        if (daoCheck.existsBySoDienThoai(sdt, excludeId)) errors.add(new ValidationError("sdt", "Số điện thoại đã tồn tại trong hệ thống"));
        if (daoCheck.existsByCccd(cccd, excludeId)) errors.add(new ValidationError("cccd", "Số CCCD đã tồn tại trong hệ thống"));
        if (!email.isEmpty() && daoCheck.existsByEmail(email, excludeId)) errors.add(new ValidationError("email", "Email đã tồn tại trong hệ thống"));
    }

    @Override protected KhachHang collectResult(FormValues values) {
        if (isEditMode()) return updateOriginal();
        String quocTich = text(txtQuocTich);
        return new KhachHang(txtMaKH.getText().trim(), text(txtHoTen), text(txtCccd), text(txtSoDienThoai),
                text(txtEmail).isEmpty() ? null : text(txtEmail),
                text(txtDiaChiThuongTru).isEmpty() ? null : text(txtDiaChiThuongTru),
                text(txtDiaChiTamTru).isEmpty() ? null : text(txtDiaChiTamTru),
                dpNgaySinh.getValue(), gioiTinhValue(), quocTich.isEmpty() ? "Việt Nam" : quocTich);
    }

    private KhachHang updateOriginal() {
        String quocTich = text(txtQuocTich);
        original.setHoTen(text(txtHoTen));
        original.setSoDienThoai(text(txtSoDienThoai));
        original.setCccd(text(txtCccd));
        original.setEmail(text(txtEmail).isEmpty() ? null : text(txtEmail));
        original.setDiaChiThuongTru(text(txtDiaChiThuongTru).isEmpty() ? null : text(txtDiaChiThuongTru));
        original.setDiaChiTamTru(text(txtDiaChiTamTru).isEmpty() ? null : text(txtDiaChiTamTru));
        original.setNgaySinh(dpNgaySinh.getValue());
        original.setGioiTinh(gioiTinhValue());
        original.setQuocTich(quocTich.isEmpty() ? "Việt Nam" : quocTich);
        return original;
    }

    @Override protected void onSubmit(KhachHang kh) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() {
                DAO_KhachHang dao = new DAO_KhachHang();
                return isCreateMode() ? dao.insert(kh) : dao.update(kh);
            }

            @Override protected void done() { finishSave(this, kh); }
        }.execute();
        // Handoff: save mode chooses insert/update but keeps callback and onSaved order unchanged.
        // Risk: failure message is mode-specific; callers still rely on setOnResult for selection refresh.
    }

    private void finishSave(SwingWorker<Boolean, Void> worker, KhachHang kh) {
        try {
            setCursor(Cursor.getDefaultCursor());
            if (worker.get()) {
                saved = true;
                if (callback != null) callback.accept(kh);
                if (onSaved != null) onSaved.run();
                dispose();
                return;
            }
            NotionMessageDialog.showMessageDialog(this, failureMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            setCursor(Cursor.getDefaultCursor());
            NotionMessageDialog.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String failureMessage() {
        return isCreateMode() ? "Không thể lưu! Mã KH có thể đã tồn tại." : "Không thể lưu thay đổi!";
    }

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

    private boolean isCreateMode() { return mode == Mode.CREATE; }
    private boolean isEditMode() { return mode == Mode.EDIT; }
    private String text(JTextField field) { return field.getText().trim(); }
    private String valueOrEmpty(String value) { return value == null ? "" : value; }
    private String gioiTinhValue() { return switch (cboGioiTinh.getSelectedIndex()) { case 1 -> "NAM"; case 2 -> "NU"; default -> null; }; }
    public boolean isSaved() { return saved; }
    public void setOnResult(Consumer<Object> callback) { this.callback = callback; }
}
