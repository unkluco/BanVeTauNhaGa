package com.modules;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class ModuleLauncher {

    public static void asDialog(AppModule module, JFrame parent, Consumer<Object> onResult) {
        module.reset();
        module.setOnResult(onResult);

        final int SHADOW_PAD = 14;

        JDialog dialog = new JDialog(parent, module.getTitle(), true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(BorderFactory.createLineBorder(new Color(0xDE, 0xE3, 0xE8), 1));
        content.add(module.getView(), BorderLayout.CENTER);

        dialog.setContentPane(ThemNhanVienDialog.buildShadowWrapper(content));
        dialog.pack();

        // Overlay close button via glass pane (adjusted for shadow padding)
        JButton btnClose = com.Main.createCloseButton();
        btnClose.addActionListener(e -> { onResult.accept(null); dialog.dispose(); });
        JPanel glass = new JPanel(null) {
            @Override
            public boolean contains(int x, int y) {
                for (Component c : getComponents()) {
                    Point p = SwingUtilities.convertPoint(this, x, y, c);
                    if (c.contains(p)) return true;
                }
                return false;
            }
        };
        glass.setOpaque(false);
        glass.add(btnClose);
        btnClose.setBounds(dialog.getWidth() - SHADOW_PAD - 42, SHADOW_PAD + 2, 40, 32);
        dialog.setGlassPane(glass);
        glass.setVisible(true);

        dialog.setLocationRelativeTo(parent);

        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                onResult.accept(null);
            }
        });

        dialog.setVisible(true);
    }

    public static void asTab(AppModule module, JTabbedPane tabs, Consumer<Object> onResult) {
        module.reset();
        module.setOnResult(onResult);
        tabs.addTab(module.getTitle(), module.getView());
        tabs.setSelectedComponent(module.getView());
    }

    public static void asPanel(AppModule module, JPanel container, Consumer<Object> onResult) {
        module.reset();
        module.setOnResult(onResult);
        container.removeAll();
        container.add(module.getView(), BorderLayout.CENTER);
        container.revalidate();
        container.repaint();
    }
}
