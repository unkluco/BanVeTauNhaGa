package com.modules;

import com.dao.DAO_ChiTietGia;
import com.dao.DAO_Gia;
import com.dao.DAO_Tuyen;
import com.entity.ChiTietGia;
import com.entity.Gia;
import com.entity.Tuyen;
import com.enums.LoaiGhe;
import com.util.MaTuDong;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChinhSuaChiTietGiaDialog extends AbstractFormDialog<ChiTietGia> {

    private final Gia gia;
    private final ChiTietGia ctg;
    private final Runnable onSaved;
    private final boolean isAddMode;
    private List<Tuyen> tuyenList;
    private boolean isFormatting = false;

    private JTextField txtMaChiTiet;
    private SearchableComboBox<Tuyen> searchTuyen;
    private JComboBox<LoaiGhe> cboLoaiGhe;
    private JTextField txtGiaNiemYet;

    public ChinhSuaChiTietGiaDialog(Window owner, Gia gia, ChiTietGia ctg, Runnable onSaved) {
        super(owner, ctg == null ? "Thêm chi tiết giá" : "Chỉnh sửa chi tiết giá");
        this.gia = gia;
        this.ctg = ctg;
        this.onSaved = onSaved;
        this.isAddMode = (ctg == null);
        loadTuyenList();
        installStandardLayout(owner);
        if (!isAddMode) populateFields();
    }

    @Override
    protected String dialogDescription() {
        return "Cấu hình tuyến, loại ghế và giá niêm yết áp dụng.";
    }

    @Override
    protected LineIcons.Name dialogIcon() {
        return LineIcons.Name.MONEY;
    }

    @Override
    protected String primaryButtonText() {
        return isAddMode ? "Thêm chi tiết" : "Cập nhật chi tiết";
    }

    @Override
    protected int preferredDialogWidth() {
        return 620;
    }

    @Override
    protected FormSchema buildFormSchema() {
        txtMaChiTiet = createReadonlyField(isAddMode ? generateMaChiTiet() : ctg.getMaChiTietGia());
        searchTuyen = createTuyenCombo();
        searchTuyen.setItems(tuyenList);
        cboLoaiGhe = createLoaiGheCombo();
        txtGiaNiemYet = createMoneyField();

        // Handoff: form chuan hoa layout, con chon tuyen/custom combo va format tien van doc qua field refs.
        // Rui ro: money listener format lai text sau khi nguoi dung go, khong nen doc qua FormValues da normalize.
        return FormSchema.builder()
                .columns(2)
                .gap(16, 14)
                .field(FieldSpec.of("maChiTiet", "Mã chi tiết", txtMaChiTiet)
                        .grid(0, 0).required(true).build())
                .field(FieldSpec.of("giaNiemYet", "Giá niêm yết", txtGiaNiemYet)
                        .grid(0, 1).required(true).hint("Nhập tối thiểu 1.000 VNĐ, hệ thống tự thêm dấu chấm.").build())
                .field(FieldSpec.of("tuyen", "Tuyến đường", searchTuyen)
                        .grid(1, 0).required(true).build())
                .field(FieldSpec.of("loaiGhe", "Loại ghế", cboLoaiGhe)
                        .grid(1, 1).required(true).build())
                .build();
    }

    private void loadTuyenList() {
        tuyenList = new DAO_Tuyen().getAll();
    }

    private void populateFields() {
        if (ctg.getTuyen() != null) {
            String maTuyen = ctg.getTuyen().getMaTuyen();
            for (Tuyen t : tuyenList) {
                if (t.getMaTuyen().equals(maTuyen)) {
                    searchTuyen.selectItem(t);
                    break;
                }
            }
        }
        if (ctg.getLoaiGhe() != null) {
            cboLoaiGhe.setSelectedItem(ctg.getLoaiGhe());
        }
        txtGiaNiemYet.setText(formatNumberWithDots(String.format("%.0f", ctg.getGiaNiemYet())));
    }

    @Override
    protected List<ValidationError> validateForm(FormValues values) {
        List<ValidationError> errors = new ArrayList<>();
        if (searchTuyen.getSelectedItem() == null) {
            errors.add(new ValidationError("tuyen", "Vui lòng chọn tuyến đường"));
        }
        if (hasDuplicateTuyenLoaiGhe()) {
            errors.add(new ValidationError("loaiGhe", "Tuyến và loại ghế này đã tồn tại trong kỳ giá"));
        }
        parseGiaNiemYet(errors);
        return errors;
    }

    private boolean hasDuplicateTuyenLoaiGhe() {
        Tuyen selectedTuyen = searchTuyen.getSelectedItem();
        LoaiGhe selectedLoaiGhe = (LoaiGhe) cboLoaiGhe.getSelectedItem();
        if (selectedTuyen == null || selectedLoaiGhe == null) return false;
        for (ChiTietGia item : new DAO_ChiTietGia().findByGia(gia.getMaGia())) {
            boolean sameRow = !isAddMode && Objects.equals(item.getMaChiTietGia(), ctg.getMaChiTietGia());
            if (!sameRow
                    && item.getTuyen() != null
                    && item.getLoaiGhe() != null
                    && Objects.equals(item.getTuyen().getMaTuyen(), selectedTuyen.getMaTuyen())
                    && item.getLoaiGhe() == selectedLoaiGhe) return true;
        }
        return false;
    }

    private double parseGiaNiemYet(List<ValidationError> errors) {
        String giaStr = txtGiaNiemYet.getText().trim();
        if (giaStr.isEmpty()) {
            errors.add(new ValidationError("giaNiemYet", "Vui lòng nhập giá niêm yết"));
            return 0;
        }
        try {
            long raw = Long.parseLong(giaStr.replaceAll("\\.", ""));
            if (raw <= 0) {
                errors.add(new ValidationError("giaNiemYet", "Giá phải lớn hơn 0"));
            } else if (raw < 1_000) {
                errors.add(new ValidationError("giaNiemYet", "Giá tối thiểu là 1.000 VNĐ"));
            } else if (raw > 100_000_000) {
                errors.add(new ValidationError("giaNiemYet", "Giá không được vượt quá 100.000.000 VNĐ"));
            }
            return raw;
        } catch (NumberFormatException ex) {
            errors.add(new ValidationError("giaNiemYet", "Giá không hợp lệ, chỉ được nhập số"));
            return 0;
        }
    }

    @Override
    protected ChiTietGia collectResult(FormValues values) {
        double giaNiemYet = parseGiaNiemYet(new ArrayList<>());
        Tuyen selectedTuyen = searchTuyen.getSelectedItem();
        LoaiGhe loaiGhe = (LoaiGhe) cboLoaiGhe.getSelectedItem();
        String maChiTiet = isAddMode ? txtMaChiTiet.getText().trim() : ctg.getMaChiTietGia();
        return new ChiTietGia(maChiTiet, gia, selectedTuyen, loaiGhe, giaNiemYet);
    }

    @Override
    protected void onSubmit(ChiTietGia result) {
        DAO_Gia daoGia = new DAO_Gia();
        int soldTicketCount = daoGia.countSoldTicketsUsingGia(gia.getMaGia());
        boolean activateClone = false;
        List<String> conflictsToDeactivate = new ArrayList<>();
        if (!isAddMode && soldTicketCount > 0) {
            if (!confirmCloneUsedGiaDetail(soldTicketCount)) return;
            activateClone = confirmActivateClonedGiaDetail();
            if (activateClone) {
                List<Gia> conflicts = daoGia.findOverlappingActive(gia.getMaGia(), gia.getThoiGianBatDau(), gia.getThoiGianKetThuc());
                if (!conflicts.isEmpty()) {
                    if (!confirmDeactivateConflicts(conflicts)) return;
                    conflictsToDeactivate = conflicts.stream().map(Gia::getMaGia).toList();
                }
            }
        }
        final boolean shouldActivateClone = activateClone;
        final List<String> deactivateIds = List.copyOf(conflictsToDeactivate);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                DAO_ChiTietGia dao = new DAO_ChiTietGia();
                if (!isAddMode && soldTicketCount > 0) {
                    return daoGia.cloneGiaWithDetailsReplacingDetail(gia, result, shouldActivateClone, deactivateIds) != null;
                }
                return isAddMode ? dao.insert(result) : dao.update(result);
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    if (get()) {
                        if (onSaved != null) onSaved.run();
                        if (!isAddMode && soldTicketCount > 0) {
                            NotionMessageDialog.showMessageDialog(ChinhSuaChiTietGiaDialog.this,
                                    "Đã ngừng kỳ giá cũ và tạo kỳ giá mới với chỉnh sửa vừa nhập"
                                            + (shouldActivateClone ? " (đang hoạt động)." : " (chưa hoạt động)."),
                                    "Đã nhân bản kỳ giá", JOptionPane.INFORMATION_MESSAGE);
                        }
                        dispose();
                    } else {
                        NotionMessageDialog.showMessageDialog(ChinhSuaChiTietGiaDialog.this,
                                isAddMode ? "Không thể thêm chi tiết giá!" : "Không thể cập nhật chi tiết giá!",
                                "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    NotionMessageDialog.showMessageDialog(ChinhSuaChiTietGiaDialog.this,
                            "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
        // Handoff: sửa chi tiết của Gia đã bán sẽ clone Gia, tắt bản cũ; bật bản mới cần xác nhận riêng.
        // Rủi ro: duplicate tuyến/loại ghế bị chặn sớm để Step2 không lấy nhầm dòng giá.
    }

    private boolean confirmCloneUsedGiaDetail(int soldTicketCount) {
        String message = "Chi tiết giá này thuộc kỳ giá đã được áp dụng trên " + soldTicketCount
                + " vé đã bán nên không thể sửa trực tiếp.\n\n"
                + "Bạn có muốn nhân bản kỳ giá này với chỉnh sửa bạn mong muốn không?\n"
                + "Kỳ giá cũ sẽ ngừng hoạt động, còn kỳ giá mới sẽ hỏi xác nhận hoạt động ở bước tiếp theo.";
        int choice = NotionMessageDialog.showConfirmDialog(this, message, "Chi tiết giá đã khóa",
                JOptionPane.WARNING_MESSAGE, "Hủy", "Đồng ý");
        return choice == JOptionPane.YES_OPTION;
    }

    private boolean confirmActivateClonedGiaDetail() {
        int choice = NotionMessageDialog.showConfirmDialog(this,
                "Bạn có muốn bật hoạt động cho kỳ giá mới vừa nhân bản không?\n"
                        + "Nếu hủy, kỳ giá mới vẫn được tạo nhưng ở trạng thái ngừng hoạt động.",
                "Bật kỳ giá mới", JOptionPane.QUESTION_MESSAGE, "Không bật", "Bật hoạt động");
        return choice == JOptionPane.YES_OPTION;
    }

    private boolean confirmDeactivateConflicts(List<Gia> conflicts) {
        String ids = conflicts.stream().map(Gia::getMaGia).collect(java.util.stream.Collectors.joining(", "));
        int choice = NotionMessageDialog.showConfirmDialog(this,
                "Kỳ giá muốn bật bị trùng thời gian với: " + ids + "\n\n"
                        + "Bạn có muốn ngừng hoạt động các kỳ giá này để bật kỳ giá mới không?",
                "Trùng kỳ giá", JOptionPane.WARNING_MESSAGE, "Hủy", "Ngừng kỳ trùng");
        return choice == JOptionPane.YES_OPTION;
    }

    private String generateMaChiTiet() {
        return MaTuDong.generate("CTG");
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

    private JTextField createMoneyField() {
        JTextField field = createTextField();
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { formatMoneyField(); }
            @Override public void removeUpdate(DocumentEvent e) { formatMoneyField(); }
            @Override public void changedUpdate(DocumentEvent e) { formatMoneyField(); }
        });
        return field;
    }

    private SearchableComboBox<Tuyen> createTuyenCombo() {
        SearchableComboBox<Tuyen> combo = new SearchableComboBox<>(
                tuyen -> tuyen.getGaDi().getTenGa() + " \u2192 " + tuyen.getGaDen().getTenGa() + " (" + tuyen.getMaTuyen() + ")",
                (tuyen, query) -> tuyen.getMaTuyen().toLowerCase().contains(query)
                        || tuyen.getGaDi().getTenGa().toLowerCase().contains(query)
                        || tuyen.getGaDen().getTenGa().toLowerCase().contains(query));
        combo.setPlaceholder("Chọn tuyến đường");
        combo.setPreferredSize(new Dimension(200, 42));
        return combo;
    }

    private JComboBox<LoaiGhe> createLoaiGheCombo() {
        JComboBox<LoaiGhe> combo = new JComboBox<>(LoaiGhe.values());
        combo.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel(value == null ? "" : value.toString());
            label.setOpaque(true);
            label.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
            label.setFont(NotionTheme.BODY);
            label.setBackground(isSelected ? NotionTheme.ACCENT_SOFT : NotionTheme.CARD);
            label.setForeground(NotionTheme.TEXT);
            return label;
        });
        return combo;
    }

    private String formatNumberWithDots(String digits) {
        if (digits == null || digits.isEmpty()) return "";
        StringBuilder sb = new StringBuilder(digits);
        for (int i = sb.length() - 3; i > 0; i -= 3) sb.insert(i, '.');
        return sb.toString();
    }

    private void formatMoneyField() {
        if (isFormatting || txtGiaNiemYet == null) return;
        isFormatting = true;
        SwingUtilities.invokeLater(() -> {
            try {
                String raw = txtGiaNiemYet.getText().replaceAll("[^0-9]", "");
                String formatted = formatNumberWithDots(raw);
                txtGiaNiemYet.setText(formatted);
                txtGiaNiemYet.setCaretPosition(formatted.length());
            } finally {
                isFormatting = false;
            }
        });
    }
}
