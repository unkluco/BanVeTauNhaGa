package com.modules;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public final class NotionTheme {

    public static final Color PAGE = new Color(0xFA, 0xFA, 0xF9);
    public static final Color SIDEBAR = new Color(0xF6, 0xF5, 0xF4);
    public static final Color CARD = AppColors.SURFACE;
    public static final Color CARD_MUTED = new Color(0xF0, 0xEE, 0xEC);
    public static final Color BORDER = new Color(0xE5, 0xE3, 0xDF);
    public static final Color BORDER_STRONG = new Color(0xC8, 0xC4, 0xBE);
    public static final Color TEXT = new Color(0x1A, 0x1A, 0x1A);
    public static final Color TEXT_MUTED = new Color(0x78, 0x76, 0x71);
    public static final Color TEXT_FAINT = new Color(0xA4, 0xA0, 0x97);
    public static final Color ACCENT = new Color(0x56, 0x45, 0xD4);
    public static final Color ACCENT_HOVER = new Color(0x45, 0x34, 0xB3);
    public static final Color ACCENT_SOFT = new Color(0xE6, 0xE0, 0xF5);
    public static final Color TABLE_SELECTION = new Color(0xD9, 0xD1, 0xF4);
    public static final Color NAVY = new Color(0x0A, 0x15, 0x30);
    public static final Color PEACH = new Color(0xFF, 0xE8, 0xD4);
    public static final Color ROSE = new Color(0xFD, 0xE0, 0xEC);
    public static final Color MINT = new Color(0xD9, 0xF3, 0xE1);
    public static final Color SKY = new Color(0xDC, 0xEC, 0xFA);
    public static final Color YELLOW = new Color(0xFE, 0xF7, 0xD6);
    public static final Color ROW_HOVER = new Color(0xF0, 0xEE, 0xEC);

    public static final int RADIUS = 14;
    public static final int RADIUS_SM = 10;
    public static final int GAP = 12;
    public static final int PAGE_PAD_X = 36;
    public static final int PAGE_PAD_Y = 28;

    public static final Font TITLE = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font HEADING = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font SECTION = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font CAPTION = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font TINY_BOLD = new Font("Segoe UI", Font.BOLD, 10);
    public static final Font BUTTON = new Font("Segoe UI", Font.BOLD, 13);

    public static Border pageBorder() {
        return new EmptyBorder(PAGE_PAD_Y, PAGE_PAD_X, PAGE_PAD_Y, PAGE_PAD_X);
    }

    public static Border cardPadding() {
        return new EmptyBorder(18, 20, 18, 20);
    }

    public static JPanel cardPanel(LayoutManager layout) {
        JPanel panel = new JPanel(layout) {
            @Override
            protected void paintComponent(Graphics g) {
                paintCard(g, this, CARD, BORDER, RADIUS);
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        return panel;
    }

    public static void stylePrimaryButton(AbstractButton button) {
        button.setFont(BUTTON);
        button.setForeground(Color.WHITE);
        button.setBackground(ACCENT);
        button.setBorder(new EmptyBorder(9, 16, 9, 16));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public static void styleSecondaryButton(AbstractButton button) {
        button.setFont(BUTTON);
        button.setForeground(TEXT);
        button.setBackground(CARD);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(8, 14, 8, 14)
        ));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public static void styleField(JTextField field) {
        field.setFont(BODY);
        field.setForeground(TEXT);
        field.setBackground(CARD);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));
    }

    public static void styleTable(JTable table) {
        table.setFont(BODY);
        table.setForeground(TEXT);
        table.setBackground(CARD);
        table.setSelectionBackground(TABLE_SELECTION);
        table.setSelectionForeground(TEXT);
        table.setGridColor(BORDER);
        table.setShowVerticalLines(false);
        table.setRowHeight(42);

        JTableHeader header = table.getTableHeader();
        header.setFont(TINY_BOLD);
        header.setForeground(TEXT_MUTED);
        header.setBackground(CARD_MUTED);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
    }

    public static void paintCard(Graphics g, JComponent component, Color fill, Color stroke, int radius) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(fill);
        g2.fill(new RoundRectangle2D.Float(0, 0, component.getWidth(), component.getHeight(), radius, radius));
        g2.setColor(stroke);
        g2.setStroke(new BasicStroke(1f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, component.getWidth() - 1, component.getHeight() - 1, radius, radius));
        g2.dispose();
    }

    private NotionTheme() {}
}
