package com.modules;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class ModuleLauncher {

    public static void asDialog(AppModule module, JFrame parent, Consumer<Object> onResult) {
        module.reset();
        module.setOnResult(onResult);

        JDialog dialog = new JDialog(parent, module.getTitle(), true);
        dialog.setContentPane(module.getView());
        dialog.pack();
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
