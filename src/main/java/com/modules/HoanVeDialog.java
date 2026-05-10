package com.modules;

import com.entity.Ve;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public class HoanVeDialog extends AbstractFormDialog<String> {
    private static final Color DANGER = AppColors.ERROR_DARK;
    private static final Color OUTLINE = AppColors.BORDER;
    private static final Font FONT_VALUE = new Font("Segoe UI", Font.PLAIN, 13);

    private final Ve ve;
    private final String hanhKhach;
    private final String hanhTrinh;
    private final String khoiHanh;

    private boolean confirmed = false;
    private String lyDo;
    private JTextArea txtLyDo;

    public HoanVeDialog(Window owner, Ve ve, String hanhKhach, String hanhTrinh, String khoiHanh) {
        super(owner, "Ho\u00e0n v\u00e9");
        this.ve = ve;
        this.hanhKhach = hanhKhach;
        this.hanhTrinh = hanhTrinh;
        this.khoiHanh = khoiHanh;
        installStandardLayout(owner);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public String getLyDo() {
        return lyDo;
    }

    @Override
    protected String dialogTitle() {
        return "X\u00e1c nh\u1eadn ho\u00e0n v\u00e9 " + (ve != null ? ve.getMaVe() : "");
    }

    @Override
    protected String dialogDescription() {
        return "Nh\u1eadp l\u00fd do ho\u00e0n v\u00e9 tr\u01b0\u1edbc khi c\u1eadp nh\u1eadt tr\u1ea1ng th\u00e1i v\u00e9.";
    }

    @Override
    protected LineIcons.Name dialogIcon() {
        return LineIcons.Name.WARNING;
    }

    @Override
    protected String primaryButtonText() {
        return "X\u00e1c nh\u1eadn ho\u00e0n v\u00e9";
    }

    @Override
    protected int preferredDialogWidth() {
        return 680;
    }

    @Override
    protected FormSchema buildFormSchema() {
        txtLyDo = new JTextArea(4, 30);
        txtLyDo.setLineWrap(true);
        txtLyDo.setWrapStyleWord(true);
        txtLyDo.setFont(FONT_VALUE);
        txtLyDo.setForeground(AppColors.TEXT_PRIMARY);
        JScrollPane reasonScroll = new JScrollPane(txtLyDo);

        // Handoff: refund dialog now uses the shared dialog shell, but confirmation state remains local.
        // Risk: callers still depend on isConfirmed()/getLyDo() after the modal dialog closes.
        return FormSchema.builder()
                .columns(2)
                .gap(16, 14)
                .field(FieldSpec.of("summary", "Th\u00f4ng tin v\u00e9", buildSummaryCard())
                        .grid(0, 0).fullWidth().preferredHeight(122).build())
                .field(FieldSpec.of("lyDo", "L\u00fd do h\u1ee7y v\u00e9", reasonScroll)
                        .grid(1, 0).fullWidth().required(true).preferredHeight(96).build())
                .build();
    }

    @Override
    protected List<ValidationError> validateForm(FormValues values) {
        List<ValidationError> errors = new ArrayList<>();
        String reason = values.text("lyDo");
        if (reason.isEmpty()) {
            errors.add(new ValidationError("lyDo", "Vui l\u00f2ng nh\u1eadp l\u00fd do h\u1ee7y v\u00e9."));
        } else if (reason.length() < 5) {
            errors.add(new ValidationError("lyDo", "L\u00fd do t\u1ed1i thi\u1ec3u 5 k\u00fd t\u1ef1."));
        }
        return errors;
    }

    @Override
    protected String collectResult(FormValues values) {
        return values.text("lyDo");
    }

    @Override
    protected void onSubmit(String reason) {
        confirmed = true;
        lyDo = reason;
        dispose();
    }

    private JPanel buildSummaryCard() {
        JPanel summaryCard = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics graphics) {
                Graphics2D g2 = (Graphics2D) graphics.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppColors.BACKGROUND);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(OUTLINE);
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 12, 12));
                g2.dispose();
                super.paintComponent(graphics);
            }
        };
        summaryCard.setOpaque(false);
        summaryCard.setBorder(new EmptyBorder(12, 14, 12, 14));
        addSummaryCell(summaryCard, summaryItem("M\u00e3 v\u00e9", ve == null ? "?" : ve.getMaVe()), 0, 0);
        addSummaryCell(summaryCard, summaryItem("H\u00e0nh kh\u00e1ch", fallback(hanhKhach)), 1, 0);
        addSummaryCell(summaryCard, summaryItem("H\u00e0nh tr\u00ecnh", fallback(hanhTrinh)), 0, 1);
        addSummaryCell(summaryCard, summaryItem("Kh\u1edfi h\u00e0nh", fallback(khoiHanh)), 1, 1);
        return summaryCard;
    }

    private JPanel summaryItem(String label, String value) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(4, 6, 4, 6));

        JLabel labelView = new JLabel(label);
        labelView.setFont(new Font("Segoe UI", Font.BOLD, 11));
        labelView.setForeground(AppColors.TEXT_SECONDARY);
        labelView.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel valueView = new JLabel(value);
        valueView.setFont(FONT_VALUE);
        valueView.setForeground(AppColors.TEXT_PRIMARY);
        valueView.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(labelView);
        panel.add(Box.createVerticalStrut(2));
        panel.add(valueView);
        return panel;
    }

    private void addSummaryCell(JPanel parent, JPanel child, int gridx, int gridy) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(gridy == 0 ? 0 : 8, gridx == 0 ? 0 : 16, 0, 0);
        parent.add(child, gbc);
    }

    private String fallback(String value) {
        if (value == null || value.isBlank()) return "?";
        return value;
    }
}
