package com.modules;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;

public final class NotionMessageDialog {
    private static final int MIN_WIDTH = 500;
    private static final int MESSAGE_WIDTH = 390;
    private static final int MAX_MESSAGE_HEIGHT = 360;

    public static void showMessageDialog(Component parent, Object message) {
        showMessageDialog(parent, message, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showMessageDialog(Component parent, Object message, String title, int messageType) {
        showDialog(parent, message, title, messageType, JOptionPane.DEFAULT_OPTION, null, null, "OK", null);
    }

    public static void showMessageDialog(Component parent, Object message, String title, int messageType, Icon icon) {
        showDialog(parent, message, title, messageType, JOptionPane.DEFAULT_OPTION, null, null, "OK", icon);
    }

    public static int showConfirmDialog(Component parent, Object message, String title, int optionType) {
        return showConfirmDialog(parent, message, title, optionType, JOptionPane.QUESTION_MESSAGE);
    }

    public static int showConfirmDialog(Component parent, Object message, String title, int optionType, int messageType) {
        return showDialog(parent, message, title, messageType, optionType, null, null, null, null);
    }

    public static int showConfirmDialog(Component parent, Object message, String title, int optionType, int messageType, Icon icon) {
        return showDialog(parent, message, title, messageType, optionType, null, null, null, icon);
    }

    public static int showConfirmDialog(Component parent, Object message, String title, int messageType,
                                        String cancelText, String confirmText) {
        return showDialog(parent, message, title, messageType, JOptionPane.YES_NO_OPTION,
                cancelText, null, confirmText, null);
    }

    public static int showYesNoDialog(Component parent, Object message, String title, int messageType) {
        return showDialog(parent, message, title, messageType, JOptionPane.YES_NO_OPTION,
                "Không", null, "Có", null);
    }

    private static int showDialog(Component parent, Object message, String title, int messageType, int optionType,
                                  String cancelText, String negativeText, String confirmText, Icon customIcon) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, title == null ? "Thông báo" : title, Dialog.ModalityType.APPLICATION_MODAL);
        DialogState state = new DialogState();

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(NotionTheme.CARD);
        root.setBorder(BorderFactory.createLineBorder(NotionTheme.BORDER, 1));

        root.add(buildHeader(dialog, title, messageType), BorderLayout.NORTH);
        root.add(buildBody(message, messageType, customIcon), BorderLayout.CENTER);
        root.add(buildFooter(dialog, state, optionType, cancelText, negativeText, confirmText, messageType), BorderLayout.SOUTH);

        dialog.setUndecorated(true);
        dialog.setResizable(false);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setContentPane(ThemNhanVienDialog.buildShadowWrapper(root));
        dialog.getRootPane().registerKeyboardAction(e -> {
            state.value = JOptionPane.CLOSED_OPTION;
            dialog.dispose();
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(MIN_WIDTH, dialog.getPreferredSize().height));
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        return state.value;
    }

    // Handoff: API giữ gần JOptionPane nhưng map đầy đủ option type phổ biến để tránh sai return.
    // Handoff: Custom icon/message dài/Object[] được xử lý trong body, không chạm logic call site.
    private static JPanel buildHeader(JDialog dialog, String title, int messageType) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(NotionTheme.CARD_MUTED);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, NotionTheme.BORDER),
                new EmptyBorder(18, 24, 14, 24)
        ));

        JPanel headLeft = new JPanel();
        headLeft.setLayout(new BoxLayout(headLeft, BoxLayout.Y_AXIS));
        headLeft.setOpaque(false);

        JLabel lblTitle = new JLabel(ellipsis(title == null || title.isBlank() ? defaultTitle(messageType) : title, 64));
        lblTitle.setToolTipText(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(headerTextColor(messageType));

        JLabel lblDesc = new JLabel(shortDescription(title, messageType));
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDesc.setForeground(NotionTheme.TEXT_MUTED);

        headLeft.add(lblTitle);
        headLeft.add(Box.createVerticalStrut(3));
        headLeft.add(lblDesc);
        header.add(headLeft, BorderLayout.CENTER);

        JButton close = new JButton("X");
        close.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        close.setForeground(NotionTheme.TEXT_MUTED);
        close.setPreferredSize(new Dimension(40, 32));
        close.setContentAreaFilled(false);
        close.setBorderPainted(false);
        close.setFocusPainted(false);
        close.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        close.addActionListener(e -> dialog.dispose());
        header.add(close, BorderLayout.EAST);
        return header;
    }

    private static JPanel buildBody(Object message, int messageType, Icon customIcon) {
        JPanel body = new JPanel(new BorderLayout(14, 0));
        body.setBackground(NotionTheme.CARD);
        body.setBorder(new EmptyBorder(20, 24, 20, 24));
        body.add(wrapIcon(customIcon, messageType), BorderLayout.WEST);
        body.add(wrapMessageComponent(createMessageComponent(message)), BorderLayout.CENTER);
        return body;
    }

    private static JComponent wrapIcon(Icon customIcon, int messageType) {
        if (customIcon != null) {
            JLabel label = new JLabel(customIcon);
            label.setPreferredSize(new Dimension(Math.max(38, customIcon.getIconWidth()), Math.max(38, customIcon.getIconHeight())));
            return label;
        }
        return new IconPanel(iconBg(messageType), iconFg(messageType), messageType, 38);
    }

    private static JComponent createMessageComponent(Object message) {
        if (message instanceof Component component) return wrapAwtComponent(component);
        if (message instanceof Object[] items) return createObjectArrayMessage(items);

        String text = String.valueOf(message == null ? "" : message);
        String trimmed = text.trim().toLowerCase();
        if (trimmed.startsWith("<html>")) {
            JLabel label = new JLabel(text.replaceFirst("(?i)<html>", "<html><body style='width:" + MESSAGE_WIDTH + "px'>"));
            label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            label.setForeground(NotionTheme.TEXT);
            label.setPreferredSize(new Dimension(MESSAGE_WIDTH, label.getPreferredSize().height));
            return label;
        }

        JTextArea area = new JTextArea(text);
        area.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        area.setForeground(NotionTheme.TEXT);
        area.setOpaque(false);
        area.setEditable(false);
        area.setFocusable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setColumns(38);
        area.setBorder(new EmptyBorder(1, 0, 0, 0));
        return area;
    }


    private static JComponent wrapAwtComponent(Component component) {
        if (component instanceof JComponent jComponent) return jComponent;
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(component, BorderLayout.CENTER);
        return wrapper;
    }

    private static JPanel createObjectArrayMessage(Object[] items) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        for (Object item : items) {
            JComponent child;
            if (item instanceof Component component) child = wrapAwtComponent(component);
            else if (item instanceof Icon icon) child = new JLabel(icon);
            else child = createMessageComponent(item);
            child.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(child);
            panel.add(Box.createVerticalStrut(6));
        }
        return panel;
    }

    private static JComponent wrapMessageComponent(JComponent component) {
        int preferredHeight = preferredMessageHeight(component);
        int height = Math.min(Math.max(preferredHeight, 38), MAX_MESSAGE_HEIGHT);
        if (preferredHeight > MAX_MESSAGE_HEIGHT) {
            JScrollPane scroll = new JScrollPane(component);
            scroll.setBorder(BorderFactory.createEmptyBorder());
            scroll.setOpaque(false);
            scroll.getViewport().setOpaque(false);
            scroll.setPreferredSize(new Dimension(MESSAGE_WIDTH, height));
            scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            return scroll;
        }
        component.setPreferredSize(new Dimension(MESSAGE_WIDTH, height));
        return component;
    }

    private static int preferredMessageHeight(JComponent component) {
        component.setPreferredSize(null);
        component.setSize(new Dimension(MESSAGE_WIDTH, Short.MAX_VALUE));
        Dimension preferred = component.getPreferredSize();
        int height = preferred.height;
        if (component instanceof JTextArea area) {
            Insets insets = area.getInsets();
            FontMetrics fm = area.getFontMetrics(area.getFont());
            int usableWidth = Math.max(1, MESSAGE_WIDTH - insets.left - insets.right);
            height = estimateWrappedTextHeight(area.getText(), fm, usableWidth) + insets.top + insets.bottom + 2;
        }
        // Handoff: đo message sau khi ép width để dialog giãn theo text, chỉ scroll khi thật sự quá dài.
        // Rủi ro: Component custom tự set preferred quá lớn vẫn bị giới hạn bởi MAX_MESSAGE_HEIGHT.
        return Math.max(height, 38);
    }

    private static int estimateWrappedTextHeight(String text, FontMetrics fm, int width) {
        String[] lines = String.valueOf(text == null ? "" : text).split("\\R", -1);
        int rows = 0;
        for (String line : lines) {
            if (line.isEmpty()) {
                rows++;
                continue;
            }
            int lineWidth = fm.stringWidth(line);
            rows += Math.max(1, (int) Math.ceil(lineWidth / (double) width));
        }
        return Math.max(1, rows) * fm.getHeight();
    }

    private static JPanel buildFooter(JDialog dialog, DialogState state, int optionType,
                                      String cancelText, String negativeText, String confirmText, int messageType) {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setBackground(NotionTheme.CARD_MUTED);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, NotionTheme.BORDER),
                new EmptyBorder(14, 24, 14, 24)
        ));

        switch (optionType) {
            case JOptionPane.YES_NO_OPTION -> {
                footer.add(createSecondaryButton(cancelText == null ? "Không" : cancelText, JOptionPane.NO_OPTION, dialog, state));
                JButton yes = createPrimaryButton(confirmText == null ? "Có" : confirmText, messageType, JOptionPane.YES_OPTION, dialog, state);
                footer.add(yes);
                dialog.getRootPane().setDefaultButton(yes);
            }
            case JOptionPane.OK_CANCEL_OPTION -> {
                footer.add(createSecondaryButton(cancelText == null ? "Hủy" : cancelText, JOptionPane.CANCEL_OPTION, dialog, state));
                JButton ok = createPrimaryButton(confirmText == null ? "OK" : confirmText, messageType, JOptionPane.OK_OPTION, dialog, state);
                footer.add(ok);
                dialog.getRootPane().setDefaultButton(ok);
            }
            case JOptionPane.YES_NO_CANCEL_OPTION -> {
                footer.add(createSecondaryButton(cancelText == null ? "Hủy" : cancelText, JOptionPane.CANCEL_OPTION, dialog, state));
                footer.add(createSecondaryButton(negativeText == null ? "Không" : negativeText, JOptionPane.NO_OPTION, dialog, state));
                JButton yes = createPrimaryButton(confirmText == null ? "Có" : confirmText, messageType, JOptionPane.YES_OPTION, dialog, state);
                footer.add(yes);
                dialog.getRootPane().setDefaultButton(yes);
            }
            default -> {
                JButton ok = createPrimaryButton(confirmText == null ? "OK" : confirmText, messageType, JOptionPane.OK_OPTION, dialog, state);
                footer.add(ok);
                dialog.getRootPane().setDefaultButton(ok);
            }
        }
        return footer;
    }

    private static JButton createPrimaryButton(String text, int messageType, int returnValue, JDialog dialog, DialogState state) {
        Color base = primaryColor(messageType);
        Color hover = darken(base, 0.88f);
        JButton button = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? hover : base);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(NotionTheme.BUTTON);
        button.setForeground(Color.WHITE);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(Math.max(104, text.length() * 10 + 30), 36));
        button.addActionListener(e -> {
            state.value = returnValue;
            dialog.dispose();
        });
        return button;
    }

    private static JButton createSecondaryButton(String text, int returnValue, JDialog dialog, DialogState state) {
        JButton button = new JButton(text);
        button.setFont(NotionTheme.BUTTON);
        button.setForeground(NotionTheme.TEXT_MUTED);
        button.setBackground(NotionTheme.CARD);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(NotionTheme.BORDER, 1, true),
                new EmptyBorder(8, 14, 8, 14)
        ));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(Math.max(92, text.length() * 9 + 28), 36));
        button.addActionListener(e -> {
            state.value = returnValue;
            dialog.dispose();
        });
        return button;
    }

    private static String defaultTitle(int type) {
        return switch (type) {
            case JOptionPane.ERROR_MESSAGE -> "Đã xảy ra lỗi";
            case JOptionPane.WARNING_MESSAGE -> "Cần xác nhận";
            case JOptionPane.QUESTION_MESSAGE -> "Xác nhận thao tác";
            default -> "Thông báo";
        };
    }

    private static String shortDescription(String title, int type) {
        if (title != null && !title.isBlank()) return switch (type) {
            case JOptionPane.ERROR_MESSAGE -> "Không thể hoàn tất thao tác này.";
            case JOptionPane.WARNING_MESSAGE -> "Vui lòng kiểm tra thông tin trước khi tiếp tục.";
            case JOptionPane.QUESTION_MESSAGE -> "Chọn hành động phù hợp để tiếp tục.";
            default -> "Thao tác đã được xử lý.";
        };
        return switch (type) {
            case JOptionPane.ERROR_MESSAGE -> "Vui lòng kiểm tra lại trước khi tiếp tục.";
            case JOptionPane.WARNING_MESSAGE -> "Thao tác này cần được xác nhận.";
            case JOptionPane.QUESTION_MESSAGE -> "Chọn hành động phù hợp để tiếp tục.";
            default -> "Thông báo từ hệ thống.";
        };
    }

    private static Color headerTextColor(int type) {
        return switch (type) {
            case JOptionPane.ERROR_MESSAGE -> new Color(0xE0, 0x31, 0x31);
            case JOptionPane.WARNING_MESSAGE -> new Color(0xDD, 0x5B, 0x00);
            default -> NotionTheme.ACCENT;
        };
    }

    private static Color primaryColor(int type) {
        return switch (type) {
            case JOptionPane.ERROR_MESSAGE -> new Color(0xE0, 0x31, 0x31);
            case JOptionPane.WARNING_MESSAGE -> new Color(0xDD, 0x5B, 0x00);
            default -> NotionTheme.ACCENT;
        };
    }

    private static Color iconBg(int type) {
        return switch (type) {
            case JOptionPane.ERROR_MESSAGE -> NotionTheme.ROSE;
            case JOptionPane.WARNING_MESSAGE -> NotionTheme.YELLOW;
            case JOptionPane.QUESTION_MESSAGE -> NotionTheme.ACCENT_SOFT;
            default -> NotionTheme.SKY;
        };
    }

    private static Color iconFg(int type) {
        return switch (type) {
            case JOptionPane.ERROR_MESSAGE -> new Color(0xE0, 0x31, 0x31);
            case JOptionPane.WARNING_MESSAGE -> new Color(0xDD, 0x5B, 0x00);
            case JOptionPane.QUESTION_MESSAGE -> NotionTheme.ACCENT;
            default -> new Color(0x00, 0x75, 0xDE);
        };
    }

    private static Color darken(Color color, float factor) {
        return new Color(Math.max(0, (int)(color.getRed() * factor)),
                Math.max(0, (int)(color.getGreen() * factor)),
                Math.max(0, (int)(color.getBlue() * factor)));
    }

    private static String ellipsis(String text, int max) {
        if (text == null || text.length() <= max) return text;
        return text.substring(0, Math.max(0, max - 1)) + "…";
    }

    private static final class IconPanel extends JPanel {
        private final Color bg;
        private final Color fg;
        private final int type;
        private final int size;

        private IconPanel(Color bg, Color fg, int type, int size) {
            this.bg = bg;
            this.fg = fg;
            this.type = type;
            this.size = size;
            setOpaque(false);
            setPreferredSize(new Dimension(size, size));
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillOval(0, 0, size, size);
            g2.setColor(fg);
            if (type == JOptionPane.WARNING_MESSAGE) {
                Font font = new Font("Segoe UI Symbol", Font.BOLD, Math.round(size * 0.56f));
                g2.setFont(font);
                FontMetrics metrics = g2.getFontMetrics();
                String glyph = "!";
                int glyphX = Math.round((size - metrics.stringWidth(glyph)) / 2f);
                int glyphY = Math.round((size - metrics.getHeight()) / 2f + metrics.getAscent() - size * 0.01f);
                g2.drawString(glyph, glyphX, glyphY);
            } else if (type == JOptionPane.ERROR_MESSAGE) {
                g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(size * 34 / 100, size * 34 / 100, size * 66 / 100, size * 66 / 100);
                g2.drawLine(size * 66 / 100, size * 34 / 100, size * 34 / 100, size * 66 / 100);
            } else if (type == JOptionPane.QUESTION_MESSAGE) {
                g2.setFont(new Font("Segoe UI", Font.BOLD, 24));
                g2.drawString("?", size * 35 / 100, size * 72 / 100);
            } else {
                g2.setFont(new Font("Segoe UI", Font.BOLD, 26));
                g2.drawString("i", size * 43 / 100, size * 73 / 100);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static final class DialogState {
        private int value = JOptionPane.CLOSED_OPTION;
    }

    private NotionMessageDialog() {}
}
