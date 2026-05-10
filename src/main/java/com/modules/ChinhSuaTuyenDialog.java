package com.modules;

import com.dao.DAO_Ga;
import com.dao.DAO_Tuyen;
import com.entity.Ga;
import com.entity.Tuyen;
import com.util.MaTuDong;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ChinhSuaTuyenDialog extends AbstractFormDialog<Tuyen> {
    private static final Color PRIMARY = AppColors.PRIMARY_DARK;
    private static final Color CARD_BG = AppColors.SURFACE;
    private static final Color ON_SURFACE = AppColors.TEXT_PRIMARY;
    private static final Color OUTLINE = AppColors.BORDER;
    private static final Color ERROR = AppColors.ERROR_DARK;

    private static final Font FONT_INPUT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_MONO = new Font("Consolas", Font.BOLD, 13);
    private static final Font FONT_ERR = new Font("Segoe UI", Font.PLAIN, 11);

    private final DAO_Tuyen daoTuyen = new DAO_Tuyen();
    private final DAO_Ga daoGa = new DAO_Ga();
    private final boolean isEditMode;
    private final Tuyen editTarget;
    private Consumer<Tuyen> onSaved;

    private JTextField txtMaTuyen;
    private JTextField txtKm;
    private SearchableComboBox<Ga> cboGaDi;
    private SearchableComboBox<Ga> cboGaDen;
    private JComboBox<String> cboTrangThai;
    private JLabel lblErrKm;
    private JLabel lblErrGaDi;
    private JLabel lblErrGaDen;
    private JLabel lblErrGeneral;

    public ChinhSuaTuyenDialog(Frame owner) {
        this(owner, null);
    }

    public ChinhSuaTuyenDialog(Frame owner, Tuyen target) {
        super(owner, target == null ? "Th\u00eam tuy\u1ebfn \u0111\u01b0\u1eddng" : "Ch\u1ec9nh s\u1eeda tuy\u1ebfn \u0111\u01b0\u1eddng");
        this.isEditMode = target != null;
        this.editTarget = target;
        installStandardLayout(owner);
        loadGaList();
        if (isEditMode) prefillFields();
    }

    public void setOnSaved(Consumer<Tuyen> callback) {
        this.onSaved = callback;
    }

    @Override
    protected String dialogTitle() {
        return isEditMode ? "Ch\u1ec9nh s\u1eeda tuy\u1ebfn \u0111\u01b0\u1eddng" : "Th\u00eam tuy\u1ebfn \u0111\u01b0\u1eddng m\u1edbi";
    }

    @Override
    protected String dialogDescription() {
        return isEditMode ? "C\u1eadp nh\u1eadt th\u00f4ng tin chi ti\u1ebft h\u00e0nh tr\u00ecnh." : "Nh\u1eadp th\u00f4ng tin \u0111\u1ec3 t\u1ea1o tuy\u1ebfn m\u1edbi.";
    }

    @Override
    protected LineIcons.Name dialogIcon() {
        return LineIcons.Name.ROUTE;
    }

    @Override
    protected String primaryButtonText() {
        return isEditMode ? "C\u1eadp nh\u1eadt tuy\u1ebfn" : "Th\u00eam tuy\u1ebfn";
    }

    @Override
    protected int preferredDialogWidth() {
        return 680;
    }

    @Override
    protected AbstractFormDialog.FormSchema buildFormSchema() {
        txtMaTuyen = createReadonlyField(isEditMode ? editTarget.getMaTuyen() : generateMaTuyen());
        txtKm = createInputField(isEditMode ? String.valueOf(editTarget.getKm()) : "0");
        lblErrKm = createErrorLabel();
        cboGaDi = createGaCombo("T\u00ecm ga \u0111i...");
        lblErrGaDi = createErrorLabel();
        cboGaDen = createGaCombo("T\u00ecm ga \u0111\u1ebfn...");
        lblErrGaDen = createErrorLabel();
        cboTrangThai = new JComboBox<>(new String[]{"Ho\u1ea1t \u0111\u1ed9ng", "Ng\u1eebng ho\u1ea1t \u0111\u1ed9ng"});
        lblErrGeneral = createErrorLabel();

        return AbstractFormDialog.FormSchema.builder()
                .columns(2)
                .gap(16, 14)
                .field(AbstractFormDialog.FieldSpec.of("maTuyen", "M\u00e3 tuy\u1ebfn", txtMaTuyen)
                        .grid(0, 0)
                        .hint("* M\u00e3 tuy\u1ebfn t\u1ef1 \u0111\u1ed9ng, kh\u00f4ng th\u1ec3 thay \u0111\u1ed5i")
                        .build())
                .field(AbstractFormDialog.FieldSpec.of("km", "S\u1ed1 km", txtKm)
                        .grid(0, 1)
                        .required(true)
                        .errorLabel(lblErrKm)
                        .build())
                .field(AbstractFormDialog.FieldSpec.of("gaDi", "Ga \u0111i", cboGaDi)
                        .grid(1, 0)
                        .required(true)
                        .errorLabel(lblErrGaDi)
                        .build())
                .field(AbstractFormDialog.FieldSpec.of("gaDen", "Ga \u0111\u1ebfn", cboGaDen)
                        .grid(1, 1)
                        .required(true)
                        .errorLabel(lblErrGaDen)
                        .build())
                .field(AbstractFormDialog.FieldSpec.of("trangThai", "Tr\u1ea1ng th\u00e1i", cboTrangThai)
                        .grid(2, 0)
                        .fullWidth()
                        .build())
                .field(AbstractFormDialog.FieldSpec.of("general", " ", lblErrGeneral)
                        .grid(3, 0)
                        .fullWidth()
                        .preferredHeight(28)
                        .build())
                .build();
        // Handoff: route form now uses shared shell; DAO validation remains inside subclass.
        // Risk: keep SearchableComboBox references because FormValues cannot read custom combo values.
    }

    private SearchableComboBox<Ga> createGaCombo(String placeholder) {
        SearchableComboBox<Ga> combo = new SearchableComboBox<>(
                ga -> ga.getTenGa() + " (" + ga.getMaGa() + ")",
                (ga, query) -> ga.getTenGa().toLowerCase().contains(query)
                        || ga.getMaGa().toLowerCase().contains(query));
        combo.setPlaceholder(placeholder);
        combo.setPreferredSize(new Dimension(200, 42));
        return combo;
    }

    private JTextField createInputField(String value) {
        JTextField field = new JTextField(value == null ? "" : value);
        field.setFont(FONT_INPUT);
        field.setForeground(ON_SURFACE);
        field.setBackground(CARD_BG);
        field.setPreferredSize(new Dimension(0, 40));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1),
                new EmptyBorder(8, 10, 8, 10)
        ));
        return field;
    }

    private JTextField createReadonlyField(String value) {
        JTextField field = createInputField(value);
        field.setEditable(false);
        field.setFont(FONT_MONO);
        field.setForeground(PRIMARY);
        field.setBackground(AppColors.BACKGROUND);
        return field;
    }

    private JLabel createErrorLabel() {
        JLabel label = new JLabel(" ");
        label.setFont(FONT_ERR);
        label.setForeground(ERROR);
        return label;
    }

    private void loadGaList() {
        List<Ga> gaList = daoGa.getAll();
        cboGaDi.setItems(gaList);
        cboGaDen.setItems(gaList);
    }

    private void prefillFields() {
        if (editTarget.getGaDi() != null) cboGaDi.selectItem(editTarget.getGaDi());
        if (editTarget.getGaDen() != null) cboGaDen.selectItem(editTarget.getGaDen());
        cboTrangThai.setSelectedItem(editTarget.isHoatDong() ? "Ho\u1ea1t \u0111\u1ed9ng" : "Ng\u1eebng ho\u1ea1t \u0111\u1ed9ng");
    }

    @Override
    protected List<ValidationError> validateForm(FormValues values) {
        clearErrors();
        List<ValidationError> errors = new ArrayList<>();
        int km = parseKm(errors);
        Ga gaDi = cboGaDi.getSelectedItem();
        Ga gaDen = cboGaDen.getSelectedItem();

        if (gaDi == null) {
            lblErrGaDi.setText("Vui l\u00f2ng ch\u1ecdn ga \u0111i");
            errors.add(new ValidationError("gaDi", "Vui l\u00f2ng ch\u1ecdn ga \u0111i"));
        }
        if (gaDen == null) {
            lblErrGaDen.setText("Vui l\u00f2ng ch\u1ecdn ga \u0111\u1ebfn");
            errors.add(new ValidationError("gaDen", "Vui l\u00f2ng ch\u1ecdn ga \u0111\u1ebfn"));
        }
        if (gaDi != null && gaDen != null && gaDi.getMaGa().equals(gaDen.getMaGa())) {
            lblErrGaDen.setText("Ga \u0111\u1ebfn ph\u1ea3i kh\u00e1c ga \u0111i");
            errors.add(new ValidationError("gaDen", "Ga \u0111\u1ebfn ph\u1ea3i kh\u00e1c ga \u0111i"));
        }
        if (errors.isEmpty() && !isEditMode) {
            List<Tuyen> existing = daoTuyen.findByGaDiGaDen(gaDi.getMaGa(), gaDen.getMaGa());
            if (!existing.isEmpty()) {
                lblErrGeneral.setText("Tuy\u1ebfn " + gaDi.getTenGa() + " \u2192 " + gaDen.getTenGa()
                        + " \u0111\u00e3 t\u1ed3n t\u1ea1i (M\u00e3: " + existing.get(0).getMaTuyen() + ")");
                errors.add(new ValidationError("general", lblErrGeneral.getText()));
            }
        }
        return errors;
    }

    private int parseKm(List<ValidationError> errors) {
        try {
            int km = Integer.parseInt(txtKm.getText().trim());
            if (km < 0) {
                lblErrKm.setText("S\u1ed1 km kh\u00f4ng \u0111\u01b0\u1ee3c \u00e2m");
                errors.add(new ValidationError("km", "S\u1ed1 km kh\u00f4ng \u0111\u01b0\u1ee3c \u00e2m"));
            }
            return km;
        } catch (NumberFormatException ex) {
            lblErrKm.setText("S\u1ed1 km ph\u1ea3i l\u00e0 s\u1ed1 nguy\u00ean");
            errors.add(new ValidationError("km", "S\u1ed1 km ph\u1ea3i l\u00e0 s\u1ed1 nguy\u00ean"));
            return 0;
        }
    }

    private void clearErrors() {
        lblErrKm.setText(" ");
        lblErrGaDi.setText(" ");
        lblErrGaDen.setText(" ");
        lblErrGeneral.setText(" ");
    }

    @Override
    protected Tuyen collectResult(FormValues values) {
        Ga gaDi = cboGaDi.getSelectedItem();
        Ga gaDen = cboGaDen.getSelectedItem();
        Tuyen tuyen = new Tuyen(txtMaTuyen.getText().trim(), gaDi, gaDen, Integer.parseInt(txtKm.getText().trim()));
        tuyen.setHoatDong(cboTrangThai.getSelectedIndex() == 0);
        return tuyen;
    }

    @Override
    protected void onSubmit(Tuyen result) {
        boolean ok = isEditMode ? daoTuyen.update(result) : daoTuyen.insert(result);
        if (ok) {
            if (onSaved != null) onSaved.accept(result);
            dispose();
        } else {
            lblErrGeneral.setText("C\u00f3 l\u1ed7i x\u1ea3y ra khi l\u01b0u, vui l\u00f2ng th\u1eed l\u1ea1i.");
        }
    }

    private String generateMaTuyen() {
        return MaTuDong.generate("TUY");
    }
}
