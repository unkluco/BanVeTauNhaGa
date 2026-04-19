package com.modules;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Consumer;

public class ModuleLauncher {

    public static void asDialog(AppModule module, JFrame parent, Consumer<Object> onResult) {
        module.reset();
        module.setOnResult(onResult);

        final boolean dismissOnOutside = module instanceof EntityDetailModule;
        final boolean[] closed = {false};

        JDialog dialog = new JDialog(
            parent,
            module.getTitle(),
            dismissOnOutside ? Dialog.ModalityType.MODELESS : Dialog.ModalityType.APPLICATION_MODAL
        );

        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(BorderFactory.createLineBorder(new Color(0xDE, 0xE3, 0xE8), 1));
        content.add(module.getView(), BorderLayout.CENTER);

        dialog.setContentPane(ThemNhanVienDialog.buildShadowWrapper(content));
        dialog.pack();

        Runnable closeAction = () -> {
            if (closed[0]) return;
            closed[0] = true;
            if (onResult != null) onResult.accept(null);
            dialog.dispose();
        };

        dialog.getRootPane().registerKeyboardAction(
            e -> closeAction.run(),
            KeyStroke.getKeyStroke("ESCAPE"),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        dialog.setLocationRelativeTo(parent);

        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                closeAction.run();
            }
        });

        if (dismissOnOutside) {
            dialog.addWindowFocusListener(new WindowAdapter() {
                @Override public void windowLostFocus(WindowEvent e) {
                    SwingUtilities.invokeLater(() -> {
                        if (dialog.isShowing() && !dialog.isFocused()) closeAction.run();
                    });
                }
            });
        }

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
