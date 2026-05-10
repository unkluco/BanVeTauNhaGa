package com.modules;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;

final class SearchFieldClearButton {
    private SearchFieldClearButton() {
    }

    static void install(JPanel searchBox, JTextField field, Runnable afterClear) {
        JButton clearButton = new JButton("X");
        clearButton.setVisible(false);
        clearButton.setFocusable(false);
        clearButton.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        clearButton.setContentAreaFilled(false);
        clearButton.setBorderPainted(false);
        clearButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        clearButton.setPreferredSize(new Dimension(24, 24));
        clearButton.addActionListener(e -> {
            field.setText("");
            if (afterClear != null) afterClear.run();
        });
        field.getDocument().addDocumentListener(new DocumentListener() {
            private void update() { clearButton.setVisible(!field.getText().isBlank()); }
            @Override public void insertUpdate(DocumentEvent e) { update(); }
            @Override public void removeUpdate(DocumentEvent e) { update(); }
            @Override public void changedUpdate(DocumentEvent e) { update(); }
        });
        searchBox.add(clearButton, BorderLayout.EAST);
        // Handoff: nút X chỉ thuộc thanh tìm kiếm và tự hiện khi field có input.
        // Cảnh báo: afterClear không thay thế DocumentListener, chỉ dùng cho các filter cần refresh phụ trợ.
    }
}
