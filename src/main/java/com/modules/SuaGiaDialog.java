package com.modules;

import com.dao.DAO_Gia;
import com.entity.Gia;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SuaGiaDialog extends AbstractFormDialog<Gia> {
    private JTextField txtMaGia;
    private JTextField txtMoTa;
    private DatePickerField dpBatDau;
    private DatePickerField dpKetThuc;
    private JComboBox<String> cboTrangThai;

    private final Gia gia;
    private boolean saved = false;
    private final Runnable onSaved;

    public SuaGiaDialog(Window owner, Gia gia, Runnable onSaved) {
        super(owner, "Ch\u1ec9nh s\u1eeda k\u1ef3 gi\u00e1");
        this.gia = gia;
        this.onSaved = onSaved;
        installStandardLayout(owner);
        populateFields();
    }

    @Override protected String dialogDescription() { return "\u0110i\u1ec1u ch\u1ec9nh th\u00f4ng tin k\u1ef3 gi\u00e1 v\u00e0 tr\u1ea1ng th\u00e1i \u00e1p d\u1ee5ng."; }
    @Override protected LineIcons.Name dialogIcon() { return LineIcons.Name.MONEY; }
    @Override protected String primaryButtonText() { return "C\u1eadp nh\u1eadt k\u1ef3 gi\u00e1"; }
    @Override protected int preferredDialogWidth() { return 640; }

    @Override
    protected FormSchema buildFormSchema() {
        txtMaGia = createReadonlyField();
        txtMoTa = createTextField();
        dpBatDau = new DatePickerField();
        dpKetThuc = new DatePickerField();
        cboTrangThai = new JComboBox<>(new String[]{"\u0110ang \u00e1p d\u1ee5ng", "Ng\u1eebng \u00e1p d\u1ee5ng"});

        // Handoff: sua gia giu DatePickerField va overlap check cu, chi thay shell/layout form.
        // Rui ro: DatePickerField khong doc qua FormValues nen validate/collect phai doc truc tiep.
        return FormSchema.builder()
                .columns(2)
                .gap(16, 14)
                .field(FieldSpec.of("maGia", "M\u00e3 gi\u00e1", txtMaGia)
                        .grid(0, 0).required(true).hint("M\u00e3 gi\u00e1 kh\u00f4ng th\u1ec3 thay \u0111\u1ed5i.").build())
                .field(FieldSpec.of("trangThai", "Tr\u1ea1ng th\u00e1i", cboTrangThai).grid(0, 1).build())
                .field(FieldSpec.of("moTa", "M\u00f4 t\u1ea3 / T\u00ean k\u1ef3 gi\u00e1", txtMoTa)
                        .grid(1, 0).fullWidth().required(true).build())
                .field(FieldSpec.of("batDau", "Ng\u00e0y \u00e1p d\u1ee5ng", dpBatDau).grid(2, 0).required(true).build())
                .field(FieldSpec.of("ketThuc", "Ng\u00e0y k\u1ebft th\u00fac", dpKetThuc).grid(2, 1).required(true).build())
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
        String moTa = txtMoTa.getText().trim();
        LocalDate batDau = dpBatDau.getValue();
        LocalDate ketThuc = dpKetThuc.getValue();
        if (moTa.isEmpty()) errors.add(new ValidationError("moTa", "Vui l\u00f2ng nh\u1eadp m\u00f4 t\u1ea3"));
        if (batDau == null) errors.add(new ValidationError("batDau", "Vui l\u00f2ng ch\u1ecdn ng\u00e0y \u00e1p d\u1ee5ng"));
        if (ketThuc == null) errors.add(new ValidationError("ketThuc", "Vui l\u00f2ng ch\u1ecdn ng\u00e0y k\u1ebft th\u00fac"));
        if (batDau != null && ketThuc != null && !ketThuc.isAfter(batDau)) {
            errors.add(new ValidationError("ketThuc", "Th\u1eddi gian k\u1ebft th\u00fac ph\u1ea3i sau th\u1eddi gian b\u1eaft \u0111\u1ea7u"));
        }
        return errors;
    }

    @Override
    protected Gia collectResult(FormValues values) {
        return new Gia(gia.getMaGia(), dpBatDau.getValue(), dpKetThuc.getValue(),
                txtMoTa.getText().trim(), cboTrangThai.getSelectedIndex() == 0);
    }

    @Override
    protected void onSubmit(Gia result) {
        DAO_Gia daoGia = new DAO_Gia();
        int soldTicketCount = daoGia.countSoldTicketsUsingGia(result.getMaGia());
        boolean contentChanged = hasContentChanged(result);
        List<String> conflictsToDeactivate = new ArrayList<>();
        boolean cloneShouldActivate = false;
        if (soldTicketCount > 0 && contentChanged && !confirmCloneUsedGia(result, soldTicketCount)) return;
        if (soldTicketCount > 0 && contentChanged) {
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
        final List<String> deactivateIds = List.copyOf(conflictsToDeactivate);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<Gia, Void>() {
            @Override protected Gia doInBackground() {
                if (soldTicketCount > 0 && contentChanged) {
                    return daoGia.cloneGiaWithDetailsReplacingDetail(result, null, activateClone, deactivateIds);
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
                            NotionMessageDialog.showMessageDialog(SuaGiaDialog.this,
                                    "Kỳ giá đã được áp dụng trên vé nên hệ thống đã ngừng bản cũ và tạo bản mới: "
                                            + savedGia.getMaGia()
                                            + (savedGia.isTrangThai() ? " (đang hoạt động)." : " (chưa hoạt động)."),
                                    "Đã nhân bản kỳ giá", JOptionPane.INFORMATION_MESSAGE);
                        }
                        dispose();
                    } else {
                        NotionMessageDialog.showMessageDialog(SuaGiaDialog.this, "Không thể lưu kỳ giá!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    NotionMessageDialog.showMessageDialog(SuaGiaDialog.this, "L\u1ed7i: " + ex.getMessage(), "L\u1ed7i", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
        // Handoff: chỉ sửa nội dung của Gia đã bán mới clone; bật/tắt trạng thái được update trực tiếp.
        // Rủi ro: data cũ chưa backfill maChiTietGia sẽ không được count chính xác cho usage lịch sử.
    }

    public boolean isSaved() { return saved; }

    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(NotionTheme.BODY);
        field.setForeground(NotionTheme.TEXT);
        return field;
    }

    private JTextField createReadonlyField() {
        JTextField field = createTextField();
        field.setEditable(false);
        field.setFont(NotionTheme.BODY_BOLD);
        return field;
    }

    private void showOverlapWarningDialog(String conflictIds) {
        JLabel lblMsg = new JLabel("Kh\u00f4ng th\u1ec3 k\u00edch ho\u1ea1t k\u1ef3 gi\u00e1 n\u00e0y v\u00ec tr\u00f9ng kho\u1ea3ng th\u1eddi gian v\u1edbi:");
        lblMsg.setFont(NotionTheme.BODY);
        lblMsg.setForeground(NotionTheme.TEXT);
        JLabel lblIds = new JLabel(conflictIds);
        lblIds.setFont(new Font("Consolas", Font.BOLD, 14));
        lblIds.setForeground(AppColors.ERROR_DARK);
        NotionMessageDialog.showMessageDialog(this, new Object[]{lblMsg, lblIds}, "Tr\u00f9ng k\u1ef3 gi\u00e1", JOptionPane.WARNING_MESSAGE);
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
