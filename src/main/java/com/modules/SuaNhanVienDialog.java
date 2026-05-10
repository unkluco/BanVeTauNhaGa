package com.modules;

import com.dao.DAO_NhanVien;
import com.entity.NhanVien;
import com.enums.TrangThaiNhanVien;
import com.enums.VaiTro;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SuaNhanVienDialog extends AbstractFormDialog<NhanVien> {
    private JTextField txtMaNV, txtHoTen, txtSoDienThoai, txtCccd, txtEmail, txtDiaChiThuongTru, txtDiaChiTamTru, txtQuocTich;
    private JPasswordField txtPassword;
    private DatePickerField dpNgaySinh;
    private JComboBox<String> cboGioiTinh, cboBoPhan, cboTrangThai, cboGaLamViec;
    private String[] gaKeys;
    private boolean saved = false;
    private final Runnable onSaved;
 private final NhanVien original;
    public SuaNhanVienDialog(Window owner,NhanVien nv,Runnable onSaved){super(owner,"Ch\u1ec9nh s\u1eeda nh\u00e2n vi\u00ean");this.original=nv;this.onSaved=onSaved;installStandardLayout(owner);populateFields();}
    @Override protected String dialogDescription(){return "C\u1eadp nh\u1eadt h\u1ed3 s\u01a1, b\u1ed9 ph\u1eadn v\u00e0 tr\u1ea1ng th\u00e1i nh\u00e2n vi\u00ean.";}
    @Override protected LineIcons.Name dialogIcon(){return LineIcons.Name.USER;} @Override protected String primaryButtonText(){return "C\u1eadp nh\u1eadt";} @Override protected int preferredDialogWidth(){return 820;}
    @Override protected FormSchema buildFormSchema(){txtMaNV=createReadonlyField(original.getMaNV());
        txtHoTen=createTextField(); txtSoDienThoai=createTextField(); txtCccd=createTextField(); txtEmail=createTextField(); txtDiaChiThuongTru=createTextField(); txtDiaChiTamTru=createTextField(); txtQuocTich=createTextField(); txtQuocTich.setText("Vi\u1ec7t Nam");
        txtPassword=new JPasswordField(); txtPassword.setFont(NotionTheme.BODY); dpNgaySinh=new DatePickerField();
        cboGioiTinh=new JComboBox<>(new String[]{"-- Kh\u00f4ng ch\u1ecdn --","Nam","N\u1eef"}); cboBoPhan=new JComboBox<>(new String[]{"B\u00e1n v\u00e9","\u0110i\u1ec1u ph\u1ed1i","Admin"}); cboTrangThai=new JComboBox<>(new String[]{"\u0110ang l\u00e0m","Ngh\u1ec9 ph\u00e9p","\u0110\u00e3 ngh\u1ec9"}); loadGaLamViec();
        return FormSchema.builder().columns(2).gap(16,14)
          .field(FieldSpec.of("maNV","M\u00e3 nh\u00e2n vi\u00ean",txtMaNV).grid(0,0).required(true).build())
          .field(FieldSpec.of("boPhan","B\u1ed9 ph\u1eadn",cboBoPhan).grid(0,1).required(true).build())
          .field(FieldSpec.of("hoTen","H\u1ecd t\u00ean",txtHoTen).grid(1,0).required(true).build())
          .field(FieldSpec.of("sdt","S\u1ed1 \u0111i\u1ec7n tho\u1ea1i",txtSoDienThoai).grid(1,1).required(true).build())
          .field(FieldSpec.of("cccd","CCCD",txtCccd).grid(2,0).required(true).build())
          .field(FieldSpec.of("email","Email",txtEmail).grid(2,1).build())
          .field(FieldSpec.of("password", passwordLabel(), txtPassword).grid(3,0).required(passwordRequired()).hint(passwordHint()).build())
          .field(FieldSpec.of("trangThai","Tr\u1ea1ng th\u00e1i",cboTrangThai).grid(3,1).build())
          .field(FieldSpec.of("ga","Ga l\u00e0m vi\u1ec7c",cboGaLamViec).grid(4,0).build())
          .field(FieldSpec.of("gioiTinh","Gi\u1edbi t\u00ednh",cboGioiTinh).grid(4,1).build())
          .field(FieldSpec.of("ngaySinh","Ng\u00e0y sinh",dpNgaySinh).grid(5,0).build())
          .field(FieldSpec.of("quocTich","Qu\u1ed1c t\u1ecbch",txtQuocTich).grid(5,1).build())
          .field(FieldSpec.of("thuongTru","\u0110\u1ecba ch\u1ec9 th\u01b0\u1eddng tr\u00fa",txtDiaChiThuongTru).grid(6,0).fullWidth().build())
          .field(FieldSpec.of("tamTru","\u0110\u1ecba ch\u1ec9 t\u1ea1m tr\u00fa",txtDiaChiTamTru).grid(7,0).fullWidth().build()).build();
}
    private void populateFields(){txtHoTen.setText(original.getHoTen()==null?"":original.getHoTen());txtSoDienThoai.setText(original.getSoDienThoai()==null?"":original.getSoDienThoai());txtCccd.setText(original.getCccd()==null?"":original.getCccd());txtEmail.setText(original.getEmail()==null?"":original.getEmail());txtDiaChiThuongTru.setText(original.getDiaChiThuongTru()==null?"":original.getDiaChiThuongTru());txtDiaChiTamTru.setText(original.getDiaChiTamTru()==null?"":original.getDiaChiTamTru());txtQuocTich.setText(original.getQuocTich()==null?"":original.getQuocTich());if(original.getNgaySinh()!=null)dpNgaySinh.setValue(original.getNgaySinh()); if(original.getVaiTro()==VaiTro.DIEU_PHOI)cboBoPhan.setSelectedIndex(1); else if(original.getVaiTro()==VaiTro.ADMIN)cboBoPhan.setSelectedIndex(2); if(original.getTrangThai()==TrangThaiNhanVien.NGHI_PHEP)cboTrangThai.setSelectedIndex(1); else if(original.getTrangThai()==TrangThaiNhanVien.DA_NGHI)cboTrangThai.setSelectedIndex(2); if("NAM".equals(original.getGioiTinh()))cboGioiTinh.setSelectedIndex(1); else if("NU".equals(original.getGioiTinh()))cboGioiTinh.setSelectedIndex(2); if(original.getGaLamViec()!=null) for(int i=1;i<gaKeys.length;i++) if(original.getGaLamViec().equals(gaKeys[i])){cboGaLamViec.setSelectedIndex(i);break;}}
    @Override protected List<ValidationError> validateForm(FormValues v){
        List<ValidationError> errors=new ArrayList<>(); String hoTen=text(txtHoTen), sdt=text(txtSoDienThoai), cccd=text(txtCccd), email=text(txtEmail), password=new String(txtPassword.getPassword()).trim();
        if(hoTen.isEmpty()) errors.add(new ValidationError("hoTen","Vui l\u00f2ng nh\u1eadp h\u1ecd v\u00e0 t\u00ean"));
        if(sdt.isEmpty()) errors.add(new ValidationError("sdt","Vui l\u00f2ng nh\u1eadp s\u1ed1 \u0111i\u1ec7n tho\u1ea1i")); else if(!sdt.matches("\\d{10,11}")) errors.add(new ValidationError("sdt","S\u1ed1 \u0111i\u1ec7n tho\u1ea1i ph\u1ea3i c\u00f3 10-11 ch\u1eef s\u1ed1"));
        if(cccd.isEmpty()) errors.add(new ValidationError("cccd","Vui l\u00f2ng nh\u1eadp s\u1ed1 CCCD")); else if(!cccd.matches("\\d{12}")) errors.add(new ValidationError("cccd","S\u1ed1 CCCD ph\u1ea3i c\u00f3 \u0111\u00fang 12 ch\u1eef s\u1ed1"));
        if(!email.isEmpty()&&!email.matches("[\\w.+\\-]+@[\\w\\-]+(\\.[\\w\\-]+)*\\.[a-zA-Z]{2,}")) errors.add(new ValidationError("email","Email kh\u00f4ng h\u1ee3p l\u1ec7"));
        validatePassword(errors,password);
DAO_NhanVien d=new DAO_NhanVien(); if(errors.isEmpty()){if(d.existsBySoDienThoai(text(txtSoDienThoai),original.getMaNV()))errors.add(new ValidationError("sdt","S\u1ed1 \u0111i\u1ec7n tho\u1ea1i \u0111\u00e3 t\u1ed3n t\u1ea1i trong h\u1ec7 th\u1ed1ng")); if(d.existsByCccd(text(txtCccd),original.getMaNV()))errors.add(new ValidationError("cccd","S\u1ed1 CCCD \u0111\u00e3 t\u1ed3n t\u1ea1i trong h\u1ec7 th\u1ed1ng")); if(!text(txtEmail).isEmpty()&&d.existsByEmail(text(txtEmail),original.getMaNV()))errors.add(new ValidationError("email","Email \u0111\u00e3 t\u1ed3n t\u1ea1i trong h\u1ec7 th\u1ed1ng"));} return errors;}
    @Override protected NhanVien collectResult(FormValues v){
        String password = finalPassword(); String quocTich=text(txtQuocTich);
        NhanVien nv=new NhanVien(entityId(), text(txtHoTen), password, vaiTroValue(), text(txtSoDienThoai), text(txtCccd), text(txtDiaChiTamTru).isEmpty()?null:text(txtDiaChiTamTru), trangThaiValue());
        nv.setEmail(text(txtEmail).isEmpty()?null:text(txtEmail)); nv.setGaLamViec(gaValue()); nv.setDiaChiThuongTru(text(txtDiaChiThuongTru).isEmpty()?null:text(txtDiaChiThuongTru)); nv.setNgaySinh(dpNgaySinh.getValue()); nv.setGioiTinh(gioiTinhValue()); nv.setQuocTich(quocTich.isEmpty()?null:quocTich); return nv;
}
    @Override protected void onSubmit(NhanVien nv){setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));new SwingWorker<Boolean,Void>(){@Override protected Boolean doInBackground(){return new DAO_NhanVien().update(nv);}@Override protected void done(){finish(this);}}.execute();/* Handoff: update preserves unchanged password rule and onSaved. Risk: duplicate checks still hit DB before worker. */}
    private void finish(SwingWorker<Boolean,Void>w){try{setCursor(Cursor.getDefaultCursor());if(w.get()){if(onSaved!=null)onSaved.run();dispose();}else NotionMessageDialog.showMessageDialog(this,"Kh\u00f4ng th\u1ec3 l\u01b0u thay \u0111\u1ed5i!","L\u1ed7i",JOptionPane.ERROR_MESSAGE);}catch(Exception ex){NotionMessageDialog.showMessageDialog(this,"L\u1ed7i: "+ex.getMessage(),"L\u1ed7i",JOptionPane.ERROR_MESSAGE);}}
    protected String passwordLabel(){return "M\u1eadt kh\u1ea9u m\u1edbi";} protected boolean passwordRequired(){return false;} protected String passwordHint(){return "\u0110\u1ec3 tr\u1ed1ng n\u1ebfu kh\u00f4ng \u0111\u1ed5i";} protected void validatePassword(List<ValidationError>e,String p){} protected String finalPassword(){String p=new String(txtPassword.getPassword()).trim();return p.isEmpty()?original.getPassword():p;} protected String entityId(){return original.getMaNV();}

    private void loadGaLamViec() { List<String[]> gaList = new DAO_NhanVien().getAllGa(); gaKeys = new String[gaList.size()+1]; String[] items = new String[gaList.size()+1]; gaKeys[0]=null; items[0]="-- Kh\u00f4ng ch\u1ecdn --"; for(int i=0;i<gaList.size();i++){gaKeys[i+1]=gaList.get(i)[0]; items[i+1]=gaList.get(i)[1]+" ("+gaList.get(i)[0]+")";} cboGaLamViec = new JComboBox<>(items); }
    private JTextField createTextField() { JTextField f = new JTextField(); f.setFont(NotionTheme.BODY); f.setForeground(NotionTheme.TEXT); return f; }
    private JTextField createReadonlyField(String value) { JTextField f=createTextField(); f.setText(value==null?"":value); f.setEditable(false); f.setFont(NotionTheme.BODY_BOLD); return f; }
    private String text(JTextField f) { return f.getText().trim(); }
    private VaiTro vaiTroValue() { return switch(cboBoPhan.getSelectedIndex()){ case 0 -> VaiTro.BAN_VE; case 1 -> VaiTro.DIEU_PHOI; default -> VaiTro.ADMIN; }; }
    private TrangThaiNhanVien trangThaiValue() { return switch(cboTrangThai.getSelectedIndex()){ case 1 -> TrangThaiNhanVien.NGHI_PHEP; case 2 -> TrangThaiNhanVien.DA_NGHI; default -> TrangThaiNhanVien.DANG_LAM; }; }
    private String gioiTinhValue() { return switch(cboGioiTinh.getSelectedIndex()){ case 1 -> "NAM"; case 2 -> "NU"; default -> null; }; }
    private String gaValue() { int i=cboGaLamViec.getSelectedIndex(); return (i>=0 && i<gaKeys.length)?gaKeys[i]:null; }
    public boolean isSaved() { return saved; }
}
