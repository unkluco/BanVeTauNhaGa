package com.modules;

import java.awt.Window;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class BasicFormDialog extends AbstractFormDialog<Map<String, Object>> {
    private final String title;
    private final FormSchema schema;
    private final Consumer<Map<String, Object>> onSubmit;

    public BasicFormDialog(Window owner, String title, FormSchema schema, Consumer<Map<String, Object>> onSubmit) {
        super(owner, title);
        this.title = title;
        this.schema = schema;
        this.onSubmit = onSubmit;
        installStandardLayout(owner);
    }

    @Override
    protected String dialogTitle() {
        return title;
    }

    @Override
    protected LineIcons.Name dialogIcon() {
        return LineIcons.Name.INFO;
    }

    @Override
    protected FormSchema buildFormSchema() {
        return schema;
    }

    @Override
    protected List<ValidationError> validateForm(FormValues values) {
        List<ValidationError> errors = new ArrayList<>();
        for (FieldSpec<?> field : schema.fields()) {
            if (field.required() && values.text(field.id()).isBlank()) {
                errors.add(new ValidationError(field.id(), field.label() + " kh?ng ???c ?? tr?ng"));
            }
        }
        return errors;
    }

    @Override
    protected Map<String, Object> collectResult(FormValues values) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (FieldSpec<?> field : schema.fields()) {
            result.put(field.id(), values.value(field.id()));
        }
        return result;
    }

    @Override
    protected void onSubmit(Map<String, Object> result) {
        if (onSubmit != null) onSubmit.accept(result);
        dispose();
        // Handoff: BasicFormDialog is for simple forms without field dependencies or custom save flow.
        // Risk: use a dedicated AbstractFormDialog subclass when validation or domain mapping grows.
    }
}
