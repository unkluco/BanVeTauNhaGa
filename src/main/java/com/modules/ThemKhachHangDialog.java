package com.modules;

import com.dao.DAO_KhachHang;
import com.entity.KhachHang;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ThemKhachHangDialog extends AbstractFormDialog<KhachHang> {
    private JTextField txtMaKH, txtHoTen, txtSoDienThoai, txtCccd, txtEmail, txtDiaChiThuongTru, txtDiaChiTamTru, txtQuocTich;
    private DatePickerField dpNgaySinh;
    private JComboBox<String> cboGioiTinh;
    private boolean saved = false;
    private final Runnable onSaved;
    private Consumer<Object> callback;

    public ThemKhachHangDialog(Window owner, Runnable onSaved) { super(owner, "Th\u00eam kh\u00e1ch h\u00e0ng"); this.onSaved = onSaved; installStandardLayout(owner); }
    @Override protected String dialogDescription() { return "T\u1ea1o h\u1ed3 s\u01a1 kh\u00e1ch h\u00e0ng v\u1edbi th\u00f4ng tin li\u00ean h\u1ec7 c\u01a1 b\u1ea3n."; }
    @Override protected LineIcons.Name dialogIcon() { return LineIcons.Name.USER; }
    @Override protected String primaryButtonText() { return "Th\u00eam kh\u00e1ch h\u00e0ng"; }
    @Override protected int preferredDialogWidth() { return 760; }
    @Override protected FormSchema buildFormSchema() { txtMaKH = createReadonlyField(com.util.MaTuDong.generate("KH")); 
        txtHoTen = createTextField(); txtSoDienThoai = createTextField(); txtCccd = createTextField(); txtEmail = createTextField();
        txtDiaChiThuongTru = createTextField(); txtDiaChiTamTru = createTextField(); txtQuocTich = createTextField(); txtQuocTich.setText("Vi\u1ec7t Nam");
        dpNgaySinh = new DatePickerField();
        cboGioiTinh = new JComboBox<>(new String[]{"-- Kh\u00f4ng ch\u1ecdn --", "Nam", "N\u1eef"});
        return FormSchema.builder().columns(2).gap(16,14)
            .field(FieldSpec.of("maKH", "M\u00e3 kh\u00e1ch h\u00e0ng", txtMaKH).grid(0,0).required(true).build())
            .field(FieldSpec.of("gioiTinh", "Gi\u1edbi t\u00ednh", cboGioiTinh).grid(0,1).build())
            .field(FieldSpec.of("hoTen", "H\u1ecd t\u00ean", txtHoTen).grid(1,0).required(true).build())
            .field(FieldSpec.of("sdt", "S\u1ed1 \u0111i\u1ec7n tho\u1ea1i", txtSoDienThoai).grid(1,1).required(true).build())
            .field(FieldSpec.of("cccd", "CCCD", txtCccd).grid(2,0).required(true).build())
            .field(FieldSpec.of("email", "Email", txtEmail).grid(2,1).build())
            .field(FieldSpec.of("ngaySinh", "Ng\u00e0y sinh", dpNgaySinh).grid(3,0).build())
            .field(FieldSpec.of("quocTich", "Qu\u1ed1c t\u1ecbch", txtQuocTich).grid(3,1).build())
            .field(FieldSpec.of("thuongTru", "\u0110\u1ecba ch\u1ec9 th\u01b0\u1eddng tr\u00fa", txtDiaChiThuongTru).grid(4,0).fullWidth().build())
            .field(FieldSpec.of("tamTru", "\u0110\u1ecba ch\u1ec9 t\u1ea1m tr\u00fa", txtDiaChiTamTru).grid(5,0).fullWidth().build())
            .build();
 }
    @Override protected List<ValidationError> validateForm(FormValues values) { 
        List<ValidationError> errors = new ArrayList<>();
        String hoTen = text(txtHoTen), sdt = text(txtSoDienThoai), cccd = text(txtCccd), email = text(txtEmail);
        if (hoTen.isEmpty()) errors.add(new ValidationError("hoTen", "Vui l\u00f2ng nh\u1eadp h\u1ecd v\u00e0 t\u00ean"));
        if (sdt.isEmpty()) errors.add(new ValidationError("sdt", "Vui l\u00f2ng nh\u1eadp s\u1ed1 \u0111i\u1ec7n tho\u1ea1i"));
        else if (!sdt.matches("\\d{10,11}")) errors.add(new ValidationError("sdt", "S\u1ed1 \u0111i\u1ec7n tho\u1ea1i ph\u1ea3i c\u00f3 10-11 ch\u1eef s\u1ed1"));
        if (cccd.isEmpty()) errors.add(new ValidationError("cccd", "Vui l\u00f2ng nh\u1eadp s\u1ed1 CCCD"));
        else if (!cccd.matches("\\d{12}")) errors.add(new ValidationError("cccd", "S\u1ed1 CCCD ph\u1ea3i c\u00f3 \u0111\u00fang 12 ch\u1eef s\u1ed1"));
        if (!email.isEmpty() && !email.matches("[\\w.+\\-]+@[\\w\\-]+(\\.[\\w\\-]+)*\\.[a-zA-Z]{2,}")) errors.add(new ValidationError("email", "Email kh\u00f4ng h\u1ee3p l\u1ec7"));
 return errors; }
    @Override protected KhachHang collectResult(FormValues values) { 
        String quocTich = text(txtQuocTich);
        return new KhachHang(txtMaKH.getText().trim(), text(txtHoTen), text(txtCccd), text(txtSoDienThoai),
                text(txtEmail).isEmpty() ? null : text(txtEmail),
                text(txtDiaChiThuongTru).isEmpty() ? null : text(txtDiaChiThuongTru),
                text(txtDiaChiTamTru).isEmpty() ? null : text(txtDiaChiTamTru),
                dpNgaySinh.getValue(), gioiTinhValue(), quocTich.isEmpty() ? "Vi\u1ec7t Nam" : quocTich);
 }
    @Override protected void onSubmit(KhachHang kh) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() { return new DAO_KhachHang().insert(kh); }
            @Override protected void done() { finishSave(this, kh, "Kh\u00f4ng th\u1ec3 l\u01b0u! M\u00e3 KH c\u00f3 th\u1ec3 \u0111\u00e3 t\u1ed3n t\u1ea1i."); }
        }.execute();
        // Handoff: insert flow keeps saved flag, callback and onSaved behavior.
        // Risk: uniqueness validation remains DAO-side for add mode, matching prior behavior.
    }
    private void finishSave(SwingWorker<Boolean, Void> worker, KhachHang kh, String failure) { try { setCursor(Cursor.getDefaultCursor()); if(worker.get()){saved=true;if(callback!=null)callback.accept(kh);if(onSaved!=null)onSaved.run();dispose();} else NotionMessageDialog.showMessageDialog(this,failure,"L\u1ed7i",JOptionPane.ERROR_MESSAGE); } catch(Exception ex){ NotionMessageDialog.showMessageDialog(this,"L\u1ed7i: "+ex.getMessage(),"L\u1ed7i",JOptionPane.ERROR_MESSAGE); } }

    private JTextField createTextField() { JTextField f = new JTextField(); f.setFont(NotionTheme.BODY); f.setForeground(NotionTheme.TEXT); return f; }
    private JTextField createReadonlyField(String value) { JTextField f = createTextField(); f.setText(value == null ? "" : value); f.setEditable(false); f.setFont(NotionTheme.BODY_BOLD); return f; }
    private String text(JTextField f) { return f.getText().trim(); }
    private String gioiTinhValue() { return switch (cboGioiTinh.getSelectedIndex()) { case 1 -> "NAM"; case 2 -> "NU"; default -> null; }; }
    public boolean isSaved() { return saved; }
    public void setOnResult(Consumer<Object> callback) { this.callback = callback; }
}
