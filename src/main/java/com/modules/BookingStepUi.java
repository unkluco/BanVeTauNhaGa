package com.modules;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public final class BookingStepUi {
    private static final int BUTTON_HEIGHT = 42;
    private static final int PRIMARY_MIN_WIDTH = 180;
    private static final int SECONDARY_MIN_WIDTH = 132;
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 13);
    private static final Color PRIMARY = NotionTheme.ACCENT;
    private static final Color PRIMARY_HOVER = NotionTheme.ACCENT_HOVER;
    private static final Color SURFACE = NotionTheme.CARD;
    private static final Color SECONDARY_HOVER = NotionTheme.CARD_MUTED;
    private static final Color TEXT = NotionTheme.TEXT;
    private static final Color BORDER = NotionTheme.BORDER;

    private BookingStepUi() {}

    public static JPanel createFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        footer.setBackground(SURFACE);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
                new EmptyBorder(0, 12, 0, 12)
        ));
        return footer;
        // Handoff: use this footer for every booking step action row.
        // Risk: keep callback logic in each step; this helper only standardizes visual layout.
    }

    public static void styleActionButton(JButton button, boolean primary) {
        int width = Math.max(primary ? PRIMARY_MIN_WIDTH : SECONDARY_MIN_WIDTH,
                button.getPreferredSize().width + 28);
        button.setFont(BUTTON_FONT);
        button.setForeground(primary ? Color.WHITE : TEXT);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setOpaque(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(0, 18, 0, 18));
        button.setPreferredSize(new Dimension(width, BUTTON_HEIGHT));
        button.setMinimumSize(new Dimension(primary ? PRIMARY_MIN_WIDTH : SECONDARY_MIN_WIDTH, BUTTON_HEIGHT));
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override public void paint(Graphics graphics, JComponent component) {
                AbstractButton abstractButton = (AbstractButton) component;
                Graphics2D g2 = (Graphics2D) graphics.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                ButtonModel model = abstractButton.getModel();
                Color fill;
                if (primary) {
                    fill = model.isPressed() ? PRIMARY_HOVER.darker()
                            : model.isRollover() ? PRIMARY_HOVER
                            : PRIMARY;
                } else {
                    fill = model.isRollover() ? SECONDARY_HOVER : SURFACE;
                }
                g2.setColor(fill);
                g2.fill(new RoundRectangle2D.Float(0, 0, component.getWidth(), component.getHeight(), 10, 10));
                if (!primary) {
                    g2.setColor(BORDER);
                    g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f,
                            component.getWidth() - 1, component.getHeight() - 1, 10, 10));
                }
                g2.dispose();
                super.paint(graphics, component);
            }
        });
        // Handoff: this keeps booking step buttons visually consistent across all steps.
        // Risk: do not put navigation callbacks here; each step still owns its own flow logic.
    }
}
