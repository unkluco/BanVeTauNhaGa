package com.modules;

import com.dao.DAO_NhanVien;
import com.entity.Ga;
import com.entity.NhanVien;
import com.enums.TrangThaiNhanVien;
import com.enums.VaiTro;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class NhanVienDialog extends AbstractFormDialog<NhanVien> {
    private enum Mode { CREATE, EDIT }

    private final Mode mode;
    private final NhanVien original;
    private final Runnable onSaved;
    private JTextField txtMaNV, txtHoTen, txtSoDienThoai, txtCccd, txtEmail, txtDiaChiThuongTru, txtDiaChiTamTru, txtQuocTich;
    private JPasswordField txtPassword;
    private DatePickerField dpNgaySinh;
    private JComboBox<String> cboGioiTinh, cboBoPhan, cboTrangThai, cboGaLamViec;
    private String[] gaKeys;
    private boolean saved = false;

    private NhanVienDialog(Window owner, Mode mode, NhanVien original, Runnable onSaved) {
        super(owner, mode == Mode.CREATE ? "Thêm nhân viên" : "Chỉnh sửa nhân viên");
        this.mode = mode;
        this.original = original;
        this.onSaved = onSaved;
        installStandardLayout(owner);
        if (isEditMode()) populateFields();
        // Handoff: constructor owns mode setup before fields are populated.
        // Risk: edit mode requires a non-null original employee from caller.
    }

    public static NhanVienDialog create(Window owner, Runnable onSaved) {
        return new NhanVienDialog(owner, Mode.CREATE, null, onSaved);
        // Handoff: create mode generates maNV inside buildFormSchema.
        // Risk: maNV is generated synchronously, matching the old add dialog behavior.
    }

    public static NhanVienDialog edit(Window owner, NhanVien original, Runnable onSaved) {
        if (original == null) throw new IllegalArgumentException("original employee is required for edit mode");
        return new NhanVienDialog(owner, Mode.EDIT, original, onSaved);
        // Handoff: edit mode keeps the original maNV immutable.
        // Risk: duplicate validation must continue passing original maNV as exclusion.
    }

    @Override protected String dialogDescription() {
        return isCreateMode()
                ? "Tạo hồ sơ nhân viên với thông tin làm việc và liên hệ."
                : "Cập nhật hồ sơ, bộ phận và trạng thái nhân viên.";
    }

    @Override protected LineIcons.Name dialogIcon() { return LineIcons.Name.USER; }

    @Override protected String primaryButtonText() { return isCreateMode() ? "Thêm nhân viên" : "Cập nhật"; }

    @Override protected int preferredDialogWidth() { return isCreateMode() ? 820 : 1080; }

    @Override protected FormSchema buildFormSchema() {
        txtMaNV = createReadonlyField(isCreateMode() ? new DAO_NhanVien().generateNextMaNV() : original.getMaNV());
        txtHoTen = createTextField();
        txtSoDienThoai = createTextField();
        txtCccd = createTextField();
        txtEmail = createTextField();
        txtDiaChiThuongTru = createTextField();
        txtDiaChiTamTru = createTextField();
        txtQuocTich = createTextField();
        txtQuocTich.setText("Việt Nam");
        txtPassword = new JPasswordField();
        txtPassword.setFont(NotionTheme.BODY);
        dpNgaySinh = new DatePickerField();
        cboGioiTinh = new JComboBox<>(new String[]{"-- Không chọn --", "Nam", "Nữ"});
        cboBoPhan = new JComboBox<>(new String[]{"Bán vé", "Điều phối", "Admin"});
        cboTrangThai = new JComboBox<>(new String[]{"Đang làm", "Nghỉ phép", "Đã nghỉ"});
        loadGaLamViec();
        return isCreateMode() ? buildCreateSchema() : buildEditSchema();
        // Handoff: schemas share field instances but preserve create/edit column layouts.
        // Risk: changing field ids affects AbstractFormDialog validation focus mapping.
    }

    private FormSchema buildCreateSchema() {
        return FormSchema.builder().columns(2).gap(16, 14)
                .field(FieldSpec.of("maNV", "Mã nhân viên", txtMaNV).grid(0, 0).required(true).build())
                .field(FieldSpec.of("boPhan", "Bộ phận", cboBoPhan).grid(0, 1).required(true).build())
                .field(FieldSpec.of("hoTen", "Họ tên", txtHoTen).grid(1, 0).required(true).build())
                .field(FieldSpec.of("sdt", "Số điện thoại", txtSoDienThoai).grid(1, 1).required(true).build())
                .field(FieldSpec.of("cccd", "CCCD", txtCccd).grid(2, 0).required(true).build())
                .field(FieldSpec.of("email", "Email", txtEmail).grid(2, 1).build())
                .field(FieldSpec.of("password", passwordLabel(), txtPassword).grid(3, 0).required(passwordRequired()).hint(passwordHint()).build())
                .field(FieldSpec.of("trangThai", "Trạng thái", cboTrangThai).grid(3, 1).build())
                .field(FieldSpec.of("ga", "Ga làm việc", cboGaLamViec).grid(4, 0).build())
                .field(FieldSpec.of("gioiTinh", "Giới tính", cboGioiTinh).grid(4, 1).build())
                .field(FieldSpec.of("ngaySinh", "Ngày sinh", dpNgaySinh).grid(5, 0).build())
                .field(FieldSpec.of("quocTich", "Quốc tịch", txtQuocTich).grid(5, 1).build())
                .field(FieldSpec.of("thuongTru", "Địa chỉ thường trú", txtDiaChiThuongTru).grid(6, 0).fullWidth().build())
                .field(FieldSpec.of("tamTru", "Địa chỉ tạm trú", txtDiaChiTamTru).grid(7, 0).fullWidth().build()).build();
        // Handoff: create keeps old 2-column compact layout and required password.
        // Risk: generated maNV remains read-only but can stale if dialog is held open long.
    }

    private FormSchema buildEditSchema() {
        return FormSchema.builder().columns(3).gap(16, 14)
                .field(FieldSpec.of("maNV", "Mã nhân viên", txtMaNV).grid(0, 0).required(true).build())
                .field(FieldSpec.of("boPhan", "Bộ phận", cboBoPhan).grid(0, 1).required(true).build())
                .field(FieldSpec.of("trangThai", "Trạng thái", cboTrangThai).grid(0, 2).build())
                .field(FieldSpec.of("hoTen", "Họ tên", txtHoTen).grid(1, 0).required(true).build())
                .field(FieldSpec.of("sdt", "Số điện thoại", txtSoDienThoai).grid(1, 1).required(true).build())
                .field(FieldSpec.of("cccd", "CCCD", txtCccd).grid(1, 2).required(true).build())
                .field(FieldSpec.of("email", "Email", txtEmail).grid(2, 0).build())
                .field(FieldSpec.of("password", passwordLabel(), txtPassword).grid(2, 1).required(passwordRequired()).hint(passwordHint()).build())
                .field(FieldSpec.of("ga", "Ga làm việc", cboGaLamViec).grid(2, 2).build())
                .field(FieldSpec.of("gioiTinh", "Giới tính", cboGioiTinh).grid(3, 0).build())
                .field(FieldSpec.of("ngaySinh", "Ngày sinh", dpNgaySinh).grid(3, 1).build())
                .field(FieldSpec.of("quocTich", "Quốc tịch", txtQuocTich).grid(3, 2).build())
                .field(FieldSpec.of("thuongTru", "Địa chỉ thường trú", txtDiaChiThuongTru).grid(4, 0).span(2).build())
                .field(FieldSpec.of("tamTru", "Địa chỉ tạm trú", txtDiaChiTamTru).grid(4, 2).build()).build();
        // Handoff: edit remains 3 columns with 1080px dialog width.
        // Risk: long address fields should use span/fullWidth rather than shrinking columns.
    }

    private void populateFields() {
        txtHoTen.setText(value(original.getHoTen()));
        txtSoDienThoai.setText(value(original.getSoDienThoai()));
        txtCccd.setText(value(original.getCccd()));
        txtEmail.setText(value(original.getEmail()));
        txtDiaChiThuongTru.setText(value(original.getDiaChiThuongTru()));
        txtDiaChiTamTru.setText(value(original.getDiaChiTamTru()));
        txtQuocTich.setText(value(original.getQuocTich()));
        if (original.getNgaySinh() != null) dpNgaySinh.setValue(original.getNgaySinh());
        if (original.getVaiTro() == VaiTro.DIEU_PHOI) cboBoPhan.setSelectedIndex(1);
        else if (original.getVaiTro() == VaiTro.ADMIN) cboBoPhan.setSelectedIndex(2);
        if (original.getTrangThai() == TrangThaiNhanVien.NGHI_PHEP) cboTrangThai.setSelectedIndex(1);
        else if (original.getTrangThai() == TrangThaiNhanVien.DA_NGHI) cboTrangThai.setSelectedIndex(2);
        if ("NAM".equals(original.getGioiTinh())) cboGioiTinh.setSelectedIndex(1);
        else if ("NU".equals(original.getGioiTinh())) cboGioiTinh.setSelectedIndex(2);
        String maGa = original.getMaGaLamViec();
        if (maGa != null) for (int i = 1; i < gaKeys.length; i++) if (maGa.equals(gaKeys[i])) { cboGaLamViec.setSelectedIndex(i); break; }
        // Handoff: compare station by maGa because NhanVien owns a Ga object.
        // Risk: missing maGa keeps default no-station selection.
    }

    @Override protected List<ValidationError> validateForm(FormValues v) {
        List<ValidationError> errors = new ArrayList<>();
        String hoTen = text(txtHoTen), sdt = text(txtSoDienThoai), cccd = text(txtCccd), email = text(txtEmail), password = new String(txtPassword.getPassword()).trim();
        if (hoTen.isEmpty()) errors.add(new ValidationError("hoTen", "Vui lòng nhập họ và tên"));
        if (sdt.isEmpty()) errors.add(new ValidationError("sdt", "Vui lòng nhập số điện thoại")); else if (!sdt.matches("\\d{10,11}")) errors.add(new ValidationError("sdt", "Số điện thoại phải có 10-11 chữ số"));
        if (cccd.isEmpty()) errors.add(new ValidationError("cccd", "Vui lòng nhập số CCCD")); else if (!cccd.matches("\\d{12}")) errors.add(new ValidationError("cccd", "Số CCCD phải có đúng 12 chữ số"));
        if (!email.isEmpty() && !email.matches("[\\w.+\\-]+@[\\w\\-]+(\\.[\\w\\-]+)*\\.[a-zA-Z]{2,}")) errors.add(new ValidationError("email", "Email không hợp lệ"));
        validatePassword(errors, password);
        validateDuplicates(errors, sdt, cccd, email);
        return errors;
        // Handoff: duplicate validation excludes original maNV only in edit mode.
        // Risk: DAO duplicate checks run on EDT just like the previous dialogs.
    }

    private void validateDuplicates(List<ValidationError> errors, String sdt, String cccd, String email) {
        if (!errors.isEmpty()) return;
        String excludeMaNV = isEditMode() ? original.getMaNV() : null;
        DAO_NhanVien dao = new DAO_NhanVien();
        if (dao.existsBySoDienThoai(sdt, excludeMaNV)) errors.add(new ValidationError("sdt", "Số điện thoại đã tồn tại trong hệ thống"));
        if (dao.existsByCccd(cccd, excludeMaNV)) errors.add(new ValidationError("cccd", "Số CCCD đã tồn tại trong hệ thống"));
        if (!email.isEmpty() && dao.existsByEmail(email, excludeMaNV)) errors.add(new ValidationError("email", "Email đã tồn tại trong hệ thống"));
        // Handoff: create passes null while edit passes original maNV for exclusion.
        // Risk: keep this helper aligned with DAO existsBy* method semantics.
    }

    @Override protected NhanVien collectResult(FormValues v) {
        String password = finalPassword();
        String quocTich = text(txtQuocTich);
        NhanVien nv = new NhanVien(entityId(), text(txtHoTen), password, vaiTroValue(), text(txtSoDienThoai), text(txtCccd), text(txtDiaChiTamTru).isEmpty() ? null : text(txtDiaChiTamTru), trangThaiValue());
        nv.setEmail(text(txtEmail).isEmpty() ? null : text(txtEmail));
        nv.setGaLamViec(gaValue());
        nv.setDiaChiThuongTru(text(txtDiaChiThuongTru).isEmpty() ? null : text(txtDiaChiThuongTru));
        nv.setNgaySinh(dpNgaySinh.getValue());
        nv.setGioiTinh(gioiTinhValue());
        nv.setQuocTich(quocTich.isEmpty() ? null : quocTich);
        return nv;
        // Handoff: gaLamViec carries a Ga object; DAO persists maGa only.
        // Risk: password comes from finalPassword, which differs by create/edit mode.
    }

    @Override protected void onSubmit(NhanVien nv) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() { return isCreateMode() ? new DAO_NhanVien().insert(nv) : new DAO_NhanVien().update(nv); }
            @Override protected void done() { finish(this); }
        }.execute();
        // Handoff: one submit path switches only DAO insert/update by mode.
        // Risk: UI remains disabled only by wait cursor, matching previous behavior.
    }

    private void finish(SwingWorker<Boolean, Void> worker) {
        try {
            setCursor(Cursor.getDefaultCursor());
            if (worker.get()) {
                saved = true;
                if (onSaved != null) onSaved.run();
                dispose();
            } else {
                NotionMessageDialog.showMessageDialog(this, isCreateMode() ? "Không thể lưu!" : "Không thể lưu thay đổi!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            NotionMessageDialog.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
        // Handoff: saved flag is true for both successful create and edit.
        // Risk: worker.get surfaces DAO exceptions as message text.
    }

    private void validatePassword(List<ValidationError> errors, String password) {
        if (isCreateMode() && password.isEmpty()) errors.add(new ValidationError("password", "Vui lòng nhập mật khẩu"));
        // Handoff: create requires password; edit allows empty to preserve old password.
        // Risk: no complexity validation is intentionally unchanged from previous dialogs.
    }

    private String passwordLabel() { return isCreateMode() ? "Mật khẩu" : "Mật khẩu mới"; }
    private boolean passwordRequired() { return isCreateMode(); }
    private String passwordHint() { return isCreateMode() ? "Mật khẩu đăng nhập ban đầu" : "Để trống nếu không đổi"; }
    private String finalPassword() { String p = new String(txtPassword.getPassword()).trim(); return isEditMode() && p.isEmpty() ? original.getPassword() : p; }
    private String entityId() { return isCreateMode() ? txtMaNV.getText().trim() : original.getMaNV(); }
    private boolean isCreateMode() { return mode == Mode.CREATE; }
    private boolean isEditMode() { return mode == Mode.EDIT; }

    private void loadGaLamViec() {
        List<String[]> gaList = new DAO_NhanVien().getAllGa();
        gaKeys = new String[gaList.size() + 1];
        String[] items = new String[gaList.size() + 1];
        gaKeys[0] = null;
        items[0] = "-- Không chọn --";
        for (int i = 0; i < gaList.size(); i++) { gaKeys[i + 1] = gaList.get(i)[0]; items[i + 1] = gaList.get(i)[1] + " (" + gaList.get(i)[0] + ")"; }
        cboGaLamViec = new JComboBox<>(items);
        // Handoff: combo display stores station code separately in gaKeys.
        // Risk: any sort/filter of items must keep gaKeys indices synchronized.
    }

    private JTextField createTextField() { JTextField f = new JTextField(); f.setFont(NotionTheme.BODY); f.setForeground(NotionTheme.TEXT); return f; }
    private JTextField createReadonlyField(String value) { JTextField f = createTextField(); f.setText(value == null ? "" : value); f.setEditable(false); f.setFont(NotionTheme.BODY_BOLD); return f; }
    private String text(JTextField field) { return field.getText().trim(); }
    private String value(String value) { return value == null ? "" : value; }
    private VaiTro vaiTroValue() { return switch (cboBoPhan.getSelectedIndex()) { case 0 -> VaiTro.BAN_VE; case 1 -> VaiTro.DIEU_PHOI; default -> VaiTro.ADMIN; }; }
    private TrangThaiNhanVien trangThaiValue() { return switch (cboTrangThai.getSelectedIndex()) { case 1 -> TrangThaiNhanVien.NGHI_PHEP; case 2 -> TrangThaiNhanVien.DA_NGHI; default -> TrangThaiNhanVien.DANG_LAM; }; }
    private String gioiTinhValue() { return switch (cboGioiTinh.getSelectedIndex()) { case 1 -> "NAM"; case 2 -> "NU"; default -> null; }; }
    private Ga gaValue() { int i = cboGaLamViec.getSelectedIndex(); String maGa = (i >= 0 && i < gaKeys.length) ? gaKeys[i] : null; return maGa == null ? null : new Ga(maGa); }
    public boolean isSaved() { return saved; }
}
