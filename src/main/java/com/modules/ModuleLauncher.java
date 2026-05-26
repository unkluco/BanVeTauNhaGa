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
        content.setBorder(BorderFactory.createLineBorder(AppColors.BORDER, 1));
        content.add(module.getView(), BorderLayout.CENTER);

        dialog.setContentPane(buildShadowWrapper(content));
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

        centerDialog(dialog, parent);

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

    public static void centerDialog(Window dialog, Component owner) {
        if (dialog == null) return;
        Component anchor = resolveAnchor(owner);
        Rectangle usable = usableBounds(anchor);
        Rectangle target = targetBounds(anchor, usable);

        int x = target.x + (target.width - dialog.getWidth()) / 2;
        int y = target.y + (target.height - dialog.getHeight()) / 2;
        x = clamp(x, usable.x, usable.x + usable.width - dialog.getWidth());
        y = clamp(y, usable.y, usable.y + usable.height - dialog.getHeight());
        dialog.setLocation(x, y);
        // Handoff: căn giữa theo screen của owner, tránh lệch trên máy nhiều màn hình/DPI khác nhau.
        // Rủi ro: dialog lớn hơn vùng usable sẽ bám mép thay vì bị đẩy ra ngoài màn hình.
    }

    public static JComponent buildShadowWrapper(JPanel content) {
        JPanel wrapper = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                for (int i = 0; i < 10; i++) {
                    g2.setColor(new Color(0, 0, 0, Math.max(0, 28 - i * 3)));
                    g2.drawRoundRect(10 - i / 2, 10 - i / 2, getWidth() - 20 + i, getHeight() - 20 + i, 12, 12);
                }
                g2.dispose();
            }
        };
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        wrapper.add(content, BorderLayout.CENTER);
        return wrapper;
        // Handoff: shadow wrapper nằm chung với launcher vì nhiều dialog shell dùng cùng chrome.
        // Rủi ro: thay đổi shadow tại đây ảnh hưởng mọi dialog dùng buildShadowWrapper.
    }

    private static Component resolveAnchor(Component owner) {
        if (owner != null && owner.isShowing()) return owner;
        Window active = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
        return active != null && active.isShowing() ? active : owner;
    }

    private static Rectangle targetBounds(Component anchor, Rectangle usable) {
        if (anchor == null || !anchor.isShowing()) return usable;
        Point location = anchor.getLocationOnScreen();
        return new Rectangle(location.x, location.y, anchor.getWidth(), anchor.getHeight());
    }

    private static Rectangle usableBounds(Component anchor) {
        GraphicsConfiguration gc = anchor != null ? anchor.getGraphicsConfiguration() : null;
        if (gc == null) gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        Rectangle bounds = new Rectangle(gc.getBounds());
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
        bounds.x += insets.left;
        bounds.y += insets.top;
        bounds.width -= insets.left + insets.right;
        bounds.height -= insets.top + insets.bottom;
        return bounds;
    }

    private static int clamp(int value, int min, int max) {
        if (max < min) return min;
        return Math.max(min, Math.min(max, value));
    }
}
