package com.modules;

import com.dao.DAO_Gia;
import com.entity.Gia;
import com.util.MaTuDong;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class GiaDialog extends AbstractFormDialog<Gia> {
    private static final String MO_TA_PLACEHOLDER = "VD: Hà Nội - Sài Gòn Economy";

    private JTextField txtMaGia;
    private JTextField txtMoTa;
    private DatePickerField dpBatDau;
    private DatePickerField dpKetThuc;
    private JComboBox<String> cboTrangThai;

    private final Gia gia;
    private final boolean editMode;
    private boolean saved = false;
    private final Runnable onSaved;

    public GiaDialog(Window owner, Runnable onSaved) {
        this(owner, null, onSaved);
    }

    public GiaDialog(Window owner, Gia gia, Runnable onSaved) {
        super(owner, gia == null ? "Thêm kỳ giá mới" : "Chỉnh sửa kỳ giá");
        this.gia = gia;
        this.editMode = gia != null;
        this.onSaved = onSaved;
        installStandardLayout(owner);
        if (editMode) populateFields();
    }

    @Override
    protected String dialogDescription() {
        return editMode
                ? "Điều chỉnh thông tin kỳ giá và trạng thái áp dụng."
                : "Tạo kỳ giá mới với khoảng thời gian áp dụng rõ ràng.";
    }

    @Override protected LineIcons.Name dialogIcon() { return LineIcons.Name.MONEY; }
    @Override protected String primaryButtonText() { return editMode ? "Cập nhật kỳ giá" : "Thêm kỳ giá"; }
    @Override protected int preferredDialogWidth() { return 640; }

    @Override
    protected FormSchema buildFormSchema() {
        txtMaGia = createReadonlyField(editMode ? "" : generateMaGia());
        txtMoTa = createTextField();
        dpBatDau = new DatePickerField();
        dpKetThuc = new DatePickerField();
        cboTrangThai = new JComboBox<>(new String[]{"Đang áp dụng", "Ngừng áp dụng"});

        // Handoff: DatePickerField giữ nguyên hành vi chọn/nhập ngày của form thêm và sửa cũ.
        // Rủi ro: FormValues không đọc DatePickerField nên validate/collect đọc trực tiếp field refs.
        FieldSpec.Builder maGiaField = FieldSpec.of("maGia", "Mã giá", txtMaGia).grid(0, 0).required(true);
        if (editMode) maGiaField.hint("Mã giá không thể thay đổi.");
        FieldSpec.Builder moTaField = FieldSpec.of("moTa", "Mô tả / Tên kỳ giá", txtMoTa)
                .grid(1, 0).fullWidth().required(true);
        if (!editMode) moTaField.hint(MO_TA_PLACEHOLDER);
        FieldSpec.Builder batDauField = FieldSpec.of("batDau", editMode ? "Ngày áp dụng" : "Thời gian áp dụng", dpBatDau)
                .grid(2, 0).required(true);
        FieldSpec.Builder ketThucField = FieldSpec.of("ketThuc", editMode ? "Ngày kết thúc" : "Thời gian kết thúc", dpKetThuc)
                .grid(2, 1).required(true);
        if (!editMode) {
            batDauField.hint("Chọn ngày từ lịch hoặc nhập theo định dạng dd/MM/yyyy.");
            ketThucField.hint("Chọn ngày từ lịch hoặc nhập theo định dạng dd/MM/yyyy.");
        }

        return FormSchema.builder()
                .columns(2)
                .gap(16, 14)
                .field(maGiaField.build())
                .field(FieldSpec.of("trangThai", "Trạng thái", cboTrangThai).grid(0, 1).build())
                .field(moTaField.build())
                .field(batDauField.build())
                .field(ketThucField.build())
                .build();
    }

    private void populateFields() {
        txtMaGia.setText(gia.getMaGia());
        txtMoTa.setText(gia.getMoTa() == null ? "" : gia.getMoTa());
        if (gia.getThoiGianBatDau() != null) dpBatDau.setValue(gia.getThoiGianBatDau());
        if (gia.getThoiGianKetThuc() != null) dpKetThuc.setValue(gia.getThoiGianKetThuc());
        cboTrangThai.setSelectedIndex(gia.isTrangThai() ? 0 : 1);
    }

    @Override
    protected List<ValidationError> validateForm(FormValues values) {
        List<ValidationError> errors = new ArrayList<>();
        String moTa = editMode ? txtMoTa.getText().trim() : values.text("moTa");
        LocalDate batDau = dpBatDau.getValue();
        LocalDate ketThuc = dpKetThuc.getValue();
        if (moTa.isEmpty()) errors.add(new ValidationError("moTa", "Vui lòng nhập mô tả"));
        if (batDau == null) errors.add(new ValidationError("batDau", "Vui lòng chọn ngày áp dụng"));
        if (ketThuc == null) errors.add(new ValidationError("ketThuc", "Vui lòng chọn ngày kết thúc"));
        if (batDau != null && ketThuc != null && !ketThuc.isAfter(batDau)) {
            errors.add(new ValidationError("ketThuc", "Thời gian kết thúc phải sau thời gian bắt đầu"));
        }
        return errors;
        // Handoff: null từ DatePickerField nghĩa là chưa chọn hoặc nhập sai định dạng.
        // Rủi ro: đổi API DatePickerField cần cập nhật cả validate và collect.
    }

    @Override
    protected Gia collectResult(FormValues values) {
        String maGia = editMode ? gia.getMaGia() : txtMaGia.getText().trim();
        String moTa = editMode ? txtMoTa.getText().trim() : values.text("moTa");
        return new Gia(maGia, dpBatDau.getValue(), dpKetThuc.getValue(), moTa, cboTrangThai.getSelectedIndex() == 0);
        // Handoff: collect chỉ chạy sau validate nên ngày null đã bị chặn trước đó.
        // Rủi ro: gọi trực tiếp không qua validate có thể tạo Gia thiếu ngày.
    }

    @Override
    protected void onSubmit(Gia result) {
        if (editMode) updateGia(result); else insertGia(result);
    }

    private void insertGia(Gia giaMoi) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() { return new DAO_Gia().insert(giaMoi); }
            @Override protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    if (get()) {
                        saved = true;
                        if (onSaved != null) onSaved.run();
                        dispose();
                    } else {
                        NotionMessageDialog.showMessageDialog(GiaDialog.this,
                                "Không thể lưu! Mã giá có thể đã tồn tại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    NotionMessageDialog.showMessageDialog(GiaDialog.this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
        // Handoff: thêm mới vẫn chạy SwingWorker như dialog thêm cũ để tránh khóa UI.
        // Rủi ro: base chưa disable nút lưu khi worker chạy nên tránh double-click liên tục.
    }

    private void updateGia(Gia result) {
        DAO_Gia daoGia = new DAO_Gia();
        int soldTicketCount = daoGia.countSoldTicketsUsingGia(result.getMaGia());
        boolean contentChanged = hasContentChanged(result);
        List<String> conflictsToDeactivate = new ArrayList<>();
        boolean cloneShouldActivate = false;
        if (soldTicketCount > 0 && contentChanged && !confirmCloneUsedGia(result, soldTicketCount)) return;
        String cloneMoTa = null;
        if (soldTicketCount > 0 && contentChanged) {
            cloneMoTa = promptCloneMoTa(gia.getMoTa());
            if (cloneMoTa == null) return;
            cloneShouldActivate = confirmActivateClonedGia();
            if (cloneShouldActivate) {
                List<Gia> conflicts = daoGia.findOverlappingActive(result.getMaGia(), result.getThoiGianBatDau(), result.getThoiGianKetThuc());
                if (!conflicts.isEmpty()) {
                    if (!confirmDeactivateConflicts(conflicts)) return;
                    conflictsToDeactivate = conflicts.stream().map(Gia::getMaGia).toList();
                }
            }
        } else if (result.isTrangThai()) {
            List<Gia> conflicts = daoGia.findOverlappingActive(result.getMaGia(), result.getThoiGianBatDau(), result.getThoiGianKetThuc());
            if (!conflicts.isEmpty()) {
                if (!confirmDeactivateConflicts(conflicts)) return;
                conflictsToDeactivate = conflicts.stream().map(Gia::getMaGia).toList();
            }
        }
        final boolean activateClone = cloneShouldActivate;
        final String newCloneMoTa = cloneMoTa;
        final List<String> deactivateIds = List.copyOf(conflictsToDeactivate);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<Gia, Void>() {
            @Override protected Gia doInBackground() {
                if (soldTicketCount > 0 && contentChanged) {
                    return daoGia.cloneGiaWithDetailsReplacingDetail(result, null, activateClone, deactivateIds, newCloneMoTa);
                }
                return daoGia.updateWithDeactivatedConflicts(result, deactivateIds) ? result : null;
            }
            @Override protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    Gia savedGia = get();
                    if (savedGia != null) {
                        saved = true;
                        if (savedGia.getMaGia().equals(gia.getMaGia())) copyBack(savedGia);
                        if (onSaved != null) onSaved.run();
                        if (!savedGia.getMaGia().equals(gia.getMaGia())) {
                            NotionMessageDialog.showMessageDialog(GiaDialog.this,
                                    "Kỳ giá đã được áp dụng trên vé nên hệ thống đã ngừng bản cũ và tạo bản mới: "
                                            + savedGia.getMaGia()
                                            + (savedGia.isTrangThai() ? " (đang hoạt động)." : " (chưa hoạt động)."),
                                    "Đã nhân bản kỳ giá", JOptionPane.INFORMATION_MESSAGE);
                        }
                        dispose();
                    } else {
                        NotionMessageDialog.showMessageDialog(GiaDialog.this, "Không thể lưu kỳ giá!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    NotionMessageDialog.showMessageDialog(GiaDialog.this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
        // Handoff: chỉ sửa nội dung của Gia đã bán mới clone; bật/tắt trạng thái update trực tiếp.
        // Rủi ro: data cũ chưa backfill maChiTietGia sẽ không được count chính xác cho usage lịch sử.
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

    private boolean confirmCloneUsedGia(Gia result, int soldTicketCount) {
        String message = "Kỳ giá " + result.getMaGia() + " đã được áp dụng trên " + soldTicketCount
                + " vé đã bán nên không thể sửa trực tiếp.\n\n"
                + "Bạn có muốn nhân bản kỳ giá này với chỉnh sửa bạn mong muốn không?\n"
                + "Kỳ giá cũ sẽ ngừng hoạt động, còn kỳ giá mới sẽ hỏi xác nhận hoạt động ở bước tiếp theo.";
        int choice = NotionMessageDialog.showConfirmDialog(this, message, "Kỳ giá đã được sử dụng",
                JOptionPane.WARNING_MESSAGE, "Không tạo", "Tạo bản mới");
        return choice == JOptionPane.YES_OPTION;
    }

    private String promptCloneMoTa(String defaultMoTa) {
        JTextField field = createTextField();
        field.setText(defaultMoTa == null ? "" : defaultMoTa);
        field.selectAll();
        while (true) {
            int choice = NotionMessageDialog.showConfirmDialog(this,
                    new Object[]{"Nhập mô tả / tên cho kỳ giá mới:", field},
                    "Tên kỳ giá mới", JOptionPane.QUESTION_MESSAGE, "Hủy", "Tiếp tục");
            if (choice != JOptionPane.YES_OPTION) return null;
            String value = field.getText().trim();
            if (!value.isEmpty()) return value;
            NotionMessageDialog.showMessageDialog(this,
                    "Vui lòng nhập mô tả / tên kỳ giá mới.", "Thiếu tên kỳ giá", JOptionPane.WARNING_MESSAGE);
        }
        // Handoff: tên clone được hỏi sau xác nhận nhân bản để tránh tạo bản mới trùng mô tả bản cũ.
        // Rủi ro: trả null nghĩa là user hủy toàn bộ thao tác clone, không lưu thay đổi đã nhập.
    }

    private boolean confirmActivateClonedGia() {
        int choice = NotionMessageDialog.showConfirmDialog(this,
                "Bạn có muốn bật hoạt động cho kỳ giá mới vừa nhân bản không?\n"
                        + "Nếu hủy, kỳ giá mới vẫn được tạo nhưng ở trạng thái ngừng hoạt động.",
                "Bật kỳ giá mới", JOptionPane.QUESTION_MESSAGE, "Không bật", "Bật hoạt động");
        return choice == JOptionPane.YES_OPTION;
    }

    private boolean confirmDeactivateConflicts(List<Gia> conflicts) {
        String ids = conflicts.stream().map(Gia::getMaGia).collect(Collectors.joining(", "));
        int choice = NotionMessageDialog.showConfirmDialog(this,
                "Kỳ giá muốn bật bị trùng thời gian với: " + ids + "\n\n"
                        + "Bạn có muốn ngừng hoạt động các kỳ giá này để bật kỳ giá hiện tại không?",
                "Trùng kỳ giá", JOptionPane.WARNING_MESSAGE, "Hủy", "Ngừng kỳ trùng");
        return choice == JOptionPane.YES_OPTION;
    }

    private boolean hasContentChanged(Gia result) {
        return !Objects.equals(gia.getMoTa(), result.getMoTa());
    }

    private void copyBack(Gia source) {
        gia.setMoTa(source.getMoTa());
        gia.setThoiGianBatDau(source.getThoiGianBatDau());
        gia.setThoiGianKetThuc(source.getThoiGianKetThuc());
        gia.setTrangThai(source.isTrangThai());
    }
}
