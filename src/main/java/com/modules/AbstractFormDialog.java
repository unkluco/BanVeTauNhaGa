package com.modules;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractFormDialog<T> extends JDialog {
    private static final int INPUT_HEIGHT = 40;
    private DynamicFormPanel activeFormPanel;

    protected AbstractFormDialog(Window owner, String title) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
    }

    /*
     * GRID GUIDE:
     * - Label always stays above its input; do not place labels beside fields.
     * - Default to 2 columns; short fields span 1, long text/notes/address fields span full row.
     * - Each field owns its hint/error space below the input so validation does not overlap neighbors.
     * - Body may scroll, footer/actions stay outside the grid.
     * - Keep DAO/business rules in subclasses; this base only owns layout and lifecycle helpers.
     */
    protected abstract FormSchema buildFormSchema();
    protected abstract List<ValidationError> validateForm(FormValues values);
    protected abstract T collectResult(FormValues values);
    protected abstract void onSubmit(T result) throws Exception;

    protected String dialogTitle() { return getTitle(); }
    protected String dialogDescription() { return ""; }
    protected LineIcons.Name dialogIcon() { return LineIcons.Name.INFO; }
    protected String primaryButtonText() { return "Lưu"; }
    protected String cancelButtonText() { return "Hủy bỏ"; }
    protected int preferredDialogWidth() { return 680; }
    protected JComponent buildExtraContent() { return null; }

    protected void onCancel() {
        dispose();
    }

    protected final void installStandardLayout(Window owner) {
        setUndecorated(true);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setBackground(new Color(0, 0, 0, 0));

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppColors.SURFACE);
        root.setBorder(BorderFactory.createLineBorder(AppColors.BORDER, 1));
        root.add(buildStandardHeader(), BorderLayout.NORTH);
        root.add(buildStandardBody(), BorderLayout.CENTER);
        root.add(buildStandardFooter(), BorderLayout.SOUTH);

        setContentPane(ModuleLauncher.buildShadowWrapper(root));
        getRootPane().registerKeyboardAction(
                e -> onCancel(),
                KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
        pack();
        setMinimumSize(new Dimension(preferredDialogWidth(), getPreferredSize().height));
        ModuleLauncher.centerDialog(this, owner);
        // Handoff: AbstractFormDialog owns the common dialog chrome, footer actions, ESC, and form grid shell.
        // Risk: subclasses should only provide schema/extra content/validation, not rebuild the chrome.
    }

    private JPanel buildStandardHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(AppColors.BACKGROUND);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AppColors.BORDER),
                new EmptyBorder(20, 28, 16, 28)
        ));

        JPanel left = new JPanel(new BorderLayout(12, 0));
        left.setOpaque(false);
        left.add(new JLabel(LineIcons.image(dialogIcon(), 30)), BorderLayout.WEST);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel title = new JLabel(dialogTitle());
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(AppColors.PRIMARY_DARK);
        textPanel.add(title);

        String description = dialogDescription();
        if (description != null && !description.isBlank()) {
            JLabel desc = new JLabel(description);
            desc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            desc.setForeground(AppColors.TEXT_SECONDARY);
            textPanel.add(Box.createVerticalStrut(4));
            textPanel.add(desc);
        }

        left.add(textPanel, BorderLayout.CENTER);
        header.add(left, BorderLayout.CENTER);
        return header;
    }

    private JComponent buildStandardBody() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(AppColors.SURFACE);

        activeFormPanel = createFormPanel(buildFormSchema());
        activeFormPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(activeFormPanel);

        JComponent extra = buildExtraContent();
        if (extra != null) {
            JPanel extraRow = new JPanel(new BorderLayout());
            extraRow.setOpaque(false);
            extraRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            extraRow.setBorder(new EmptyBorder(0, 24, 14, 24));
            extraRow.add(extra, BorderLayout.CENTER);
            body.add(extraRow);
        }
        return body;
    }

    private JPanel buildStandardFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setBackground(AppColors.BACKGROUND);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, AppColors.BORDER),
                new EmptyBorder(14, 24, 14, 24)
        ));

        JButton cancel = createSecondaryActionButton(cancelButtonText());
        cancel.setIcon(LineIcons.image(LineIcons.Name.CLOSE, 16));
        cancel.setIconTextGap(8);
        cancel.addActionListener(e -> onCancel());

        JButton primary = createPrimaryActionButton(primaryButtonText());
        primary.addActionListener(e -> handleSubmit(activeFormPanel));
        getRootPane().setDefaultButton(primary);

        footer.add(cancel);
        footer.add(primary);
        return footer;
    }

    private JButton createPrimaryActionButton(String text) {
        JButton button = new JButton(text);
        NotionTheme.stylePrimaryButton(button);
        button.setPreferredSize(new Dimension(150, 40));
        return button;
    }

    private JButton createSecondaryActionButton(String text) {
        JButton button = new JButton(text);
        NotionTheme.styleSecondaryButton(button);
        button.setPreferredSize(new Dimension(120, 40));
        return button;
    }

    protected final DynamicFormPanel createFormPanel(FormSchema schema) {
        return new DynamicFormPanel(schema);
    }

    protected final FormValues readValues(DynamicFormPanel formPanel) {
        return new FormValues(formPanel.componentsSnapshot());
    }

    protected final void showErrors(DynamicFormPanel formPanel, List<ValidationError> errors) {
        if (formPanel == null) return;
        formPanel.clearErrors();
        for (ValidationError error : errors) {
            formPanel.setError(error.fieldId(), error.message());
        }
        if (!errors.isEmpty()) {
            formPanel.focus(errors.get(0).fieldId());
        }
    }

    protected final void handleSubmit(DynamicFormPanel formPanel) {
        if (formPanel == null) formPanel = activeFormPanel;
        FormValues values = formPanel == null ? FormValues.empty() : readValues(formPanel);
        List<ValidationError> errors = validateForm(values);
        if (!errors.isEmpty()) {
            showErrors(formPanel, errors);
            return;
        }
        try {
            onSubmit(collectResult(values));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage() == null ? "Kh\u00f4ng th\u1ec3 l\u01b0u d\u1eef li\u1ec7u." : ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static DynamicFormPanel createPanel(FormSchema schema) {
        return new DynamicFormPanel(schema);
    }

    public static final class FormSchema {
        private final int columns;
        private final int hGap;
        private final int vGap;
        private final List<FieldSpec<?>> fields;

        private FormSchema(Builder builder) {
            this.columns = Math.max(1, builder.columns);
            this.hGap = builder.hGap;
            this.vGap = builder.vGap;
            this.fields = Collections.unmodifiableList(new ArrayList<>(builder.fields));
            validate();
        }

        public static Builder builder() {
            return new Builder();
        }

        public List<FieldSpec<?>> fields() {
            return fields;
        }

        private void validate() {
            Map<String, String> occupied = new LinkedHashMap<>();
            for (FieldSpec<?> field : fields) {
                if (field.id == null || field.id.isBlank()) {
                    throw new IllegalArgumentException("Field id must not be blank");
                }
                if (field.component == null) {
                    throw new IllegalArgumentException("Field component must not be null: " + field.id);
                }
                int startCol = field.fullWidth ? 0 : field.column;
                int span = field.fullWidth ? columns : Math.min(field.colSpan, columns);
                if (field.row < 0 || startCol < 0 || startCol >= columns || startCol + span > columns) {
                    throw new IllegalArgumentException("Invalid grid for field: " + field.id);
                }
                for (int col = startCol; col < startCol + span; col++) {
                    String cell = field.row + ":" + col;
                    String previous = occupied.putIfAbsent(cell, field.id);
                    if (previous != null) {
                        throw new IllegalArgumentException("Grid cell overlap: " + previous + " and " + field.id);
                    }
                }
            }
        }

        public static final class Builder {
            private int columns = 2;
            private int hGap = 16;
            private int vGap = 14;
            private final List<FieldSpec<?>> fields = new ArrayList<>();

            public Builder columns(int columns) {
                this.columns = columns;
                return this;
            }

            public Builder gap(int hGap, int vGap) {
                this.hGap = hGap;
                this.vGap = vGap;
                return this;
            }

            public Builder field(FieldSpec<?> field) {
                this.fields.add(field);
                return this;
            }

            public FormSchema build() {
                return new FormSchema(this);
            }
        }
    }

    public static final class FieldSpec<TField extends JComponent> {
        private final String id;
        private final String label;
        private final TField component;
        private final int row;
        private final int column;
        private final int colSpan;
        private final boolean required;
        private final String hint;
        private final JLabel errorLabel;
        private final boolean fullWidth;
        private final int preferredHeight;

        private FieldSpec(Builder<TField> builder) {
            this.id = builder.id;
            this.label = builder.label;
            this.component = builder.component;
            this.row = builder.row;
            this.column = builder.column;
            this.colSpan = Math.max(1, builder.colSpan);
            this.required = builder.required;
            this.hint = builder.hint;
            this.errorLabel = builder.errorLabel;
            this.fullWidth = builder.fullWidth;
            this.preferredHeight = builder.preferredHeight;
        }

        public static <TField extends JComponent> Builder<TField> of(String id, String label, TField component) {
            return new Builder<>(id, label, component);
        }

        public String id() {
            return id;
        }

        public String label() {
            return label;
        }

        public boolean required() {
            return required;
        }

        public TField component() {
            return component;
        }

        public static final class Builder<TField extends JComponent> {
            private final String id;
            private final String label;
            private final TField component;
            private int row;
            private int column;
            private int colSpan = 1;
            private boolean required;
            private String hint;
            private JLabel errorLabel;
            private boolean fullWidth;
            private int preferredHeight = INPUT_HEIGHT;

            private Builder(String id, String label, TField component) {
                this.id = id;
                this.label = label;
                this.component = component;
            }

            public Builder<TField> grid(int row, int column) {
                this.row = row;
                this.column = column;
                return this;
            }

            public Builder<TField> span(int colSpan) {
                this.colSpan = colSpan;
                return this;
            }

            public Builder<TField> fullWidth() {
                this.fullWidth = true;
                return this;
            }

            public Builder<TField> required(boolean required) {
                this.required = required;
                return this;
            }

            public Builder<TField> hint(String hint) {
                this.hint = hint;
                return this;
            }

            public Builder<TField> errorLabel(JLabel errorLabel) {
                this.errorLabel = errorLabel;
                return this;
            }

            public Builder<TField> preferredHeight(int preferredHeight) {
                this.preferredHeight = preferredHeight;
                return this;
            }

            public FieldSpec<TField> build() {
                return new FieldSpec<>(this);
            }
        }
    }

    public static final class DynamicFormPanel extends JPanel {
        private final FormSchema schema;
        private final Map<String, JComponent> components = new LinkedHashMap<>();
        private final Map<String, JLabel> errorLabels = new LinkedHashMap<>();

        private DynamicFormPanel(FormSchema schema) {
            this.schema = schema;
            setOpaque(false);
            setLayout(new GridBagLayout());
            setBorder(new EmptyBorder(0, 24, 0, 24));
            buildFields();
        }

        public JComponent getField(String id) {
            return components.get(id);
        }

        public void setError(String id, String message) {
            JLabel label = errorLabels.get(id);
            if (label != null) {
                label.setText(message == null || message.isBlank() ? " " : message);
            }
        }

        public void clearErrors() {
            for (JLabel label : errorLabels.values()) {
                label.setText(" ");
            }
        }

        public void focus(String id) {
            JComponent component = components.get(id);
            if (component != null) {
                component.requestFocusInWindow();
            }
        }

        public Map<String, JComponent> componentsSnapshot() {
            return new LinkedHashMap<>(components);
        }

        private void buildFields() {
            for (FieldSpec<?> field : schema.fields()) {
                components.put(field.id, field.component);
                styleComponent(field.component, field.preferredHeight);
                JLabel error = field.errorLabel == null ? createErrorLabel() : field.errorLabel;
                errorLabels.put(field.id, error);
                GridBagConstraints gbc = constraintsFor(field);
                add(wrapField(field, error), gbc);
            }
        }

        private GridBagConstraints constraintsFor(FieldSpec<?> field) {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = field.fullWidth ? 0 : field.column;
            gbc.gridy = field.row;
            gbc.gridwidth = field.fullWidth ? schema.columns : Math.min(field.colSpan, schema.columns);
            gbc.weightx = field.fullWidth ? schema.columns : Math.max(1, gbc.gridwidth);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.insets = new Insets(0, 0, schema.vGap, schema.hGap);
            if (gbc.gridx + gbc.gridwidth >= schema.columns) {
                gbc.insets.right = 0;
            }
            return gbc;
        }

        private JPanel wrapField(FieldSpec<?> field, JLabel errorLabel) {
            JPanel group = new JPanel();
            group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
            group.setOpaque(false);

            JLabel label = new JLabel(field.label + (field.required ? " *" : ""));
            label.setFont(NotionTheme.BODY_BOLD);
            label.setForeground(NotionTheme.TEXT_MUTED);
            label.setAlignmentX(Component.LEFT_ALIGNMENT);

            field.component.setAlignmentX(Component.LEFT_ALIGNMENT);
            group.add(label);
            group.add(Box.createVerticalStrut(6));
            group.add(field.component);

            if (field.hint != null && !field.hint.isBlank()) {
                JLabel hint = new JLabel(field.hint);
                hint.setFont(NotionTheme.CAPTION.deriveFont(Font.ITALIC, 11f));
                hint.setForeground(NotionTheme.TEXT_MUTED);
                hint.setAlignmentX(Component.LEFT_ALIGNMENT);
                group.add(Box.createVerticalStrut(4));
                group.add(hint);
            }

            errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            group.add(Box.createVerticalStrut(4));
            group.add(errorLabel);
            Dimension preferred = group.getPreferredSize();
            group.setPreferredSize(new Dimension(0, preferred.height));
            group.setMinimumSize(new Dimension(0, preferred.height));
            group.setMaximumSize(new Dimension(Integer.MAX_VALUE, preferred.height));
            return group;
        }

        private JLabel createErrorLabel() {
            JLabel label = new JLabel(" ");
            label.setFont(NotionTheme.CAPTION);
            label.setForeground(AppColors.ERROR);
            return label;
        }

        private void styleComponent(JComponent component, int preferredHeight) {
            int height = Math.max(INPUT_HEIGHT, preferredHeight);
            Font originalFont = component.getFont();
            component.setPreferredSize(new Dimension(0, height));
            component.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
            component.setFont(NotionTheme.BODY);
            if (component instanceof JTextField textField) {
                NotionTheme.styleField(textField);
                if (!textField.isEditable()) {
                    textField.setFont(originalFont == null ? NotionTheme.BODY_BOLD : originalFont);
                    textField.setForeground(NotionTheme.ACCENT);
                    textField.setBackground(NotionTheme.PAGE);
                }
            } else if (component instanceof JComboBox<?> comboBox) {
                comboBox.setBackground(NotionTheme.CARD);
                comboBox.setForeground(NotionTheme.TEXT);
                NotionTheme.applyComboBoxSelection(comboBox);
                comboBox.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(NotionTheme.BORDER, 1, true),
                        new EmptyBorder(4, 8, 4, 8)
                ));
                // Handoff: mọi combo trong form dialog dùng popup selection tím/chữ trắng từ theme chung.
                // Rủi ro: renderer custom cần được wrap qua helper trước khi tự override màu selected.
            } else if (component instanceof JTextArea textArea) {
                textArea.setBackground(NotionTheme.CARD);
                textArea.setForeground(NotionTheme.TEXT);
                textArea.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(NotionTheme.BORDER, 1, true),
                        new EmptyBorder(10, 12, 10, 12)
                ));
            } else if (component instanceof JScrollPane scrollPane) {
                scrollPane.setBorder(BorderFactory.createLineBorder(NotionTheme.BORDER, 1, true));
                scrollPane.setBackground(NotionTheme.CARD);
                scrollPane.getViewport().setBackground(NotionTheme.CARD);
                Component view = scrollPane.getViewport().getView();
                if (view instanceof JTextArea textArea) {
                    textArea.setFont(NotionTheme.BODY);
                    textArea.setForeground(NotionTheme.TEXT);
                    textArea.setBackground(NotionTheme.CARD);
                    textArea.setBorder(new EmptyBorder(10, 12, 10, 12));
                }
            }
        }
    }

    public static final class FormValues {
        private final Map<String, JComponent> components;

        private FormValues(Map<String, JComponent> components) {
            this.components = components;
        }

        public static FormValues empty() {
            return new FormValues(Map.of());
        }

        public JComponent component(String id) {
            return components.get(id);
        }

        public String text(String id) {
            JComponent component = components.get(id);
            if (component instanceof JTextField textField) return textField.getText().trim();
            if (component instanceof JTextArea textArea) return textArea.getText().trim();
            if (component instanceof JScrollPane scrollPane && scrollPane.getViewport().getView() instanceof JTextArea textArea) {
                return textArea.getText().trim();
            }
            return "";
        }

        public Object value(String id) {
            JComponent component = components.get(id);
            if (component instanceof JComboBox<?> comboBox) return comboBox.getSelectedItem();
            if (component instanceof JCheckBox checkBox) return checkBox.isSelected();
            if (component instanceof JTextField || component instanceof JTextArea || component instanceof JScrollPane) return text(id);
            return null;
        }
    }

    public record ValidationError(String fieldId, String message) {}
}
