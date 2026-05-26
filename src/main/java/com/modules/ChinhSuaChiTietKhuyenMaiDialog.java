package com.modules;

import com.dao.DAO_ChiTietKhuyenMai;
import com.dao.DAO_KhuyenMai;
import com.dao.DAO_Tuyen;
import com.entity.ChiTietKhuyenMai;
import com.entity.KhuyenMai;
import com.entity.Tuyen;
import com.enums.LoaiGhe;
import com.util.MaTuDong;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog them/sua ChiTietKhuyenMai.
 * - phanTramGiam: nguoi dung nhap 0-100, luu vao DB duoi dang 0.0-1.0.
 */
public class ChinhSuaChiTietKhuyenMaiDialog extends AbstractFormDialog<ChiTietKhuyenMai> {

    private final KhuyenMai khuyenMai;
    private final ChiTietKhuyenMai ctkm;
    private final Runnable onSaved;
    private final boolean isAddMode;
    private List<Tuyen> tuyenList;

    private JTextField txtMaChiTiet;
    private JTextField txtTenChiTiet;
    private SearchableComboBox<Tuyen> searchTuyen;
    private JComboBox<Object> cboLoaiGhe;
    private JTextField txtPhanTram;

    public ChinhSuaChiTietKhuyenMaiDialog(Window owner, KhuyenMai khuyenMai,
                                           ChiTietKhuyenMai ctkm, Runnable onSaved) {
        super(owner, ctkm == null ? "Thêm chi tiết khuyến mãi" : "Chỉnh sửa chi tiết khuyến mãi");
        this.khuyenMai = khuyenMai;
        this.ctkm = ctkm;
        this.onSaved = onSaved;
        this.isAddMode = (ctkm == null);
        loadTuyenList();
        installStandardLayout(owner);
        if (!isAddMode) populateFields();
    }

    @Override
    protected String dialogDescription() {
        return "Thiết lập tuyến, loại ghế và phần trăm giảm cho chương trình.";
    }

    @Override
    protected LineIcons.Name dialogIcon() {
        return LineIcons.Name.PROMOTION;
    }

    @Override
    protected String primaryButtonText() {
        return isAddMode ? "Thêm chi tiết" : "Cập nhật";
    }

    @Override
    protected int preferredDialogWidth() {
        return 620;
    }

    @Override
    protected FormSchema buildFormSchema() {
        txtMaChiTiet = createReadonlyField(isAddMode ? generateMaChiTiet() : ctkm.getMaChiTietKM());
        txtTenChiTiet = createTextField();
        searchTuyen = createTuyenCombo();
        searchTuyen.setItems(tuyenList);
        cboLoaiGhe = createLoaiGheCombo();
        txtPhanTram = createTextField();
        txtPhanTram.setText("0");

        // Handoff: schema chi doi vo UI sang form chuan; field id giu on dinh de loi hien dung o.
        // Rui ro: SearchableComboBox la custom component nen validate/collect doc truc tiep tu reference.
        return FormSchema.builder()
                .columns(2)
                .gap(16, 14)
                .field(FieldSpec.of("maChiTiet", "Mã chi tiết", txtMaChiTiet)
                        .grid(0, 0).required(true).build())
                .field(FieldSpec.of("phanTram", "% giảm", txtPhanTram)
                        .grid(0, 1).required(true).hint("Nhập giá trị lớn hơn 0 và không quá 100.").build())
                .field(FieldSpec.of("tenChiTiet", "Tên chi tiết", txtTenChiTiet)
                        .grid(1, 0).fullWidth().hint("Có thể để trống nếu không cần tên riêng cho dòng khuyến mãi.").build())
                .field(FieldSpec.of("tuyen", "Tuyến áp dụng", searchTuyen)
                        .grid(2, 0).build())
                .field(FieldSpec.of("loaiGhe", "Loại ghế", cboLoaiGhe)
                        .grid(2, 1).build())
                .build();
    }

    private void loadTuyenList() {
        tuyenList = new DAO_Tuyen().getAll();
    }

    private void populateFields() {
        txtTenChiTiet.setText(ctkm.getTenChiTiet() != null ? ctkm.getTenChiTiet() : "");
        if (ctkm.getTuyen() != null) {
            String maTuyen = ctkm.getTuyen().getMaTuyen();
            for (Tuyen t : tuyenList) {
                if (t.getMaTuyen().equals(maTuyen)) {
                    searchTuyen.selectItem(t);
                    break;
                }
            }
        }
        cboLoaiGhe.setSelectedItem(ctkm.getLoaiGhe() == null ? "Tất cả loại ghế" : ctkm.getLoaiGhe());
        txtPhanTram.setText(String.format("%.2f", ctkm.getPhanTramGiam() * 100.0));
    }

    @Override
    protected List<ValidationError> validateForm(FormValues values) {
        List<ValidationError> errors = new ArrayList<>();
        String pctStr = txtPhanTram.getText().trim().replace(',', '.');
        try {
            double pct = Double.parseDouble(pctStr);
            if (pct <= 0 || pct > 100) {
                errors.add(new ValidationError("phanTram", "% giảm phải từ 0 đến 100 (không bao gồm 0)"));
            }
        } catch (NumberFormatException ex) {
            errors.add(new ValidationError("phanTram", "% giảm phải là số"));
        }
        return errors;
    }

    @Override
    protected ChiTietKhuyenMai collectResult(FormValues values) {
        Tuyen tuyen = searchTuyen.getSelectedItem();
        Object lgSel = cboLoaiGhe.getSelectedItem();
        LoaiGhe loaiGhe = (lgSel instanceof LoaiGhe selectedLoaiGhe) ? selectedLoaiGhe : null;
        double phanTramGiam = Double.parseDouble(txtPhanTram.getText().trim().replace(',', '.')) / 100.0;
        String tenChiTiet = txtTenChiTiet.getText().trim();
        if (tenChiTiet.isEmpty()) tenChiTiet = null;
        return new ChiTietKhuyenMai(txtMaChiTiet.getText().trim(), tenChiTiet, khuyenMai, tuyen, loaiGhe, phanTramGiam);
    }

    @Override
    protected void onSubmit(ChiTietKhuyenMai record) {
        DAO_KhuyenMai daoKm = new DAO_KhuyenMai();
        int kmUsage = daoKm.countAppliedUsage(khuyenMai.getMaKhuyenMai());
        boolean shouldClone = kmUsage > 0;
        boolean activateClone = false;
        String cloneTenKhuyenMai = null;
        if (shouldClone) {
            if (!confirmCloneUsedKhuyenMaiDetail(kmUsage)) return;
            cloneTenKhuyenMai = promptCloneTenKhuyenMai(khuyenMai.getTenKhuyenMai());
            if (cloneTenKhuyenMai == null) return;
            activateClone = confirmActivateClonedKhuyenMai();
        }
        boolean ok = shouldClone
                ? cloneFromDetailChange(daoKm, record, activateClone, cloneTenKhuyenMai)
                : isAddMode ? new DAO_ChiTietKhuyenMai().insert(record) : new DAO_ChiTietKhuyenMai().update(record);

        if (ok) {
            if (onSaved != null) onSaved.run();
            if (shouldClone) {
                NotionMessageDialog.showMessageDialog(this,
                        "Đã ngừng khuyến mãi cũ và tạo khuyến mãi mới với chi tiết vừa nhập"
                                + (activateClone ? " (đang hoạt động)." : " (chưa hoạt động)."),
                        "Đã nhân bản khuyến mãi", JOptionPane.INFORMATION_MESSAGE);
            }
            dispose();
        } else {
            NotionMessageDialog.showMessageDialog(this,
                    "Có lỗi khi lưu. Vui lòng kiểm tra dữ liệu.",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
        // Handoff: KM đã áp dụng sẽ clone khi thêm/sửa chi tiết, không đổi detail lịch sử.
        // Rủi ro: booking đang mở trước clone cần reload bước KM để thấy bản mới.
    }

    private boolean cloneFromDetailChange(DAO_KhuyenMai daoKm, ChiTietKhuyenMai record,
                                          boolean activateClone, String cloneTenKhuyenMai) {
        KhuyenMai cloned = isAddMode
                ? daoKm.cloneKhuyenMaiWithDetailsAddingDetail(khuyenMai, record, activateClone, cloneTenKhuyenMai)
                : daoKm.cloneKhuyenMaiWithDetailsReplacingDetail(khuyenMai, record, activateClone, cloneTenKhuyenMai);
        return cloned != null;
    }

    private boolean confirmCloneUsedKhuyenMaiDetail(int usageCount) {
        int choice = NotionMessageDialog.showConfirmDialog(this,
                "Khuyến mãi này đã được áp dụng trên " + usageCount + " hóa đơn nên không thể thay đổi chi tiết trực tiếp.\n\n"
                        + "Bạn có muốn nhân bản khuyến mãi này với chi tiết vừa nhập không?",
                "Chi tiết khuyến mãi đã khóa", JOptionPane.WARNING_MESSAGE, "Hủy", "Đồng ý");
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
        // Handoff: clone detail KM hỏi tên mới trước khi bật hoạt động để người dùng phân biệt bản clone.
        // Rủi ro: hủy ở đây bỏ toàn bộ thay đổi detail đang nhập.
    }

    private boolean confirmActivateClonedKhuyenMai() {
        int choice = NotionMessageDialog.showConfirmDialog(this,
                "Bạn có muốn bật hoạt động cho khuyến mãi mới vừa nhân bản không?\n"
                        + "Nếu hủy, khuyến mãi mới vẫn được tạo nhưng ở trạng thái ngừng áp dụng.",
                "Bật khuyến mãi mới", JOptionPane.QUESTION_MESSAGE, "Không bật", "Bật hoạt động");
        return choice == JOptionPane.YES_OPTION;
    }

    private String generateMaChiTiet() {
        return MaTuDong.generate("CTKM");
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

    private SearchableComboBox<Tuyen> createTuyenCombo() {
        SearchableComboBox<Tuyen> combo = new SearchableComboBox<>(
                tuyen -> tuyen.getGaDi().getTenGa() + " \u2192 " + tuyen.getGaDen().getTenGa() + " (" + tuyen.getMaTuyen() + ")",
                (tuyen, query) -> tuyen.getMaTuyen().toLowerCase().contains(query)
                        || tuyen.getGaDi().getTenGa().toLowerCase().contains(query)
                        || tuyen.getGaDen().getTenGa().toLowerCase().contains(query));
        combo.setPlaceholder("Tất cả tuyến");
        combo.setPreferredSize(new Dimension(200, 42));
        return combo;
    }

    private JComboBox<Object> createLoaiGheCombo() {
        JComboBox<Object> combo = new JComboBox<>();
        combo.addItem("Tất cả loại ghế");
        for (LoaiGhe loaiGhe : LoaiGhe.values()) {
            combo.addItem(loaiGhe);
        }
        combo.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel(value instanceof LoaiGhe loaiGhe ? loaiGhe.toString() : String.valueOf(value));
            label.setOpaque(true);
            label.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
            label.setFont(NotionTheme.BODY);
            label.setBackground(isSelected ? NotionTheme.POPUP_SELECTION : NotionTheme.CARD);
            label.setForeground(isSelected ? NotionTheme.POPUP_SELECTION_TEXT : NotionTheme.TEXT);
            return label;
        });
        return combo;
    }
}

