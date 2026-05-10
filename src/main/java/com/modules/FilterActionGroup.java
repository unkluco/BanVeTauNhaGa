package com.modules;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

final class FilterActionGroup {
    private static final int DEFAULT_WIDTH = 104;
    private static final int DEFAULT_HEIGHT = 40;

    private FilterActionGroup() {
    }

    static JPanel wrap(JButton button) {
        Dimension preferred = button.getPreferredSize();
        int width = preferred != null && preferred.width > 0 ? preferred.width : DEFAULT_WIDTH;
        int height = preferred != null && preferred.height > 0 ? preferred.height : DEFAULT_HEIGHT;
        return wrap(button, width, height);
    }

    static JPanel wrap(JButton button, int width, int height) {
        Dimension size = new Dimension(width, height);
        button.setPreferredSize(size);
        button.setMinimumSize(size);
        button.setMaximumSize(size);

        JPanel group = new JPanel(new GridBagLayout());
        group.setOpaque(false);
        JLabel spacer = new JLabel(" ");
        spacer.setFont(new Font("Segoe UI", Font.BOLD, 11));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        group.add(spacer, gbc);
        gbc.gridy = 1;
        gbc.insets = new Insets(6, 0, 0, 0);
        group.add(button, gbc);
        return group;
        // Handoff: giữ kích thước nút đã set và spacer 11px để cùng nhịp với label filter.
        // Cảnh báo: chỉ dùng trong filter row; search clear X vẫn nằm trong search box riêng.
    }
}
