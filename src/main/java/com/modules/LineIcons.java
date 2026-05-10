package com.modules;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

public final class LineIcons {
    public enum Name {
        DASHBOARD, TRAIN, ROUTE, CALENDAR, TICKET, INVOICE,
        SEARCH, FILTER, ADD, EDIT, DELETE, SAVE, CLOSE,
        WARNING, SUCCESS, INFO, USER, MONEY, PROMOTION, PRINT, EXPORT, LOGOUT,
        CHEVRON_RIGHT, CHEVRON_DOWN
    }

    public static Icon of(Name name) {
        return of(name, 24, NotionTheme.TEXT);
    }

    public static Icon of(Name name, int size) {
        return of(name, size, NotionTheme.TEXT);
    }

    public static Icon of(Name name, int size, Color color) {
        return of(name, size, color, Math.max(1.6f, size / 14f));
    }

    public static Icon of(Name name, int size, Color color, float strokeWidth) {
        int safeSize = Math.max(8, size);
        float safeStroke = Math.max(1f, strokeWidth);
        return new LineIcon(name, safeSize, color == null ? NotionTheme.TEXT : color, safeStroke);
        // Handoff: LineIcons is intentionally a curated set, not a full SVG replacement.
        // Risk: add new icons only after checking the Debug matrix at 16/24/32px.
    }

    public static ImageIcon image(Name name, int size) {
        return image(name, size, size, NotionTheme.TEXT);
    }

    public static ImageIcon image(Name name, int width, int height) {
        return image(name, width, height, NotionTheme.TEXT);
    }

    public static ImageIcon image(Name name, int width, int height, Color color) {
        int safeWidth = Math.max(8, width);
        int safeHeight = Math.max(8, height);
        Icon icon = of(name, Math.min(safeWidth, safeHeight), color);
        BufferedImage image = new BufferedImage(safeWidth, safeHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        int x = Math.max(0, (safeWidth - icon.getIconWidth()) / 2);
        int y = Math.max(0, (safeHeight - icon.getIconHeight()) / 2);
        icon.paintIcon(null, g2, x, y);
        g2.dispose();
        return new ImageIcon((Image) image);
        // Handoff: use this for Swing APIs that still require ImageIcon instead of Icon.
        // Risk: prefer LineIcons.of(...) for labels/buttons that accept Icon directly.
    }

    public static Icon contained(Name name, int boxSize, int iconSize) {
        return new BoxIcon(of(name, iconSize), boxSize, boxSize);
    }

    public static Icon contained(Name name, int boxSize, int iconSize, Color color) {
        return new BoxIcon(of(name, iconSize, color), boxSize, boxSize);
    }

    private static final class BoxIcon implements Icon {
        private final Icon inner;
        private final int width;
        private final int height;

        private BoxIcon(Icon inner, int width, int height) {
            this.inner = inner;
            this.width = Math.max(8, width);
            this.height = Math.max(8, height);
        }

        @Override public void paintIcon(Component component, Graphics graphics, int x, int y) {
            int iconX = x + Math.max(0, (width - inner.getIconWidth()) / 2);
            int iconY = y + Math.max(0, (height - inner.getIconHeight()) / 2);
            inner.paintIcon(component, graphics, iconX, iconY);
        }

        @Override public int getIconWidth() { return width; }
        @Override public int getIconHeight() { return height; }
    }

    private static final class LineIcon implements Icon {
        private final Name name;
        private final int size;
        private final Color color;
        private final float strokeWidth;

        private LineIcon(Name name, int size, Color color, float strokeWidth) {
            this.name = name == null ? Name.INFO : name;
            this.size = size;
            this.color = color;
            this.strokeWidth = strokeWidth;
        }

        @Override public int getIconWidth() { return size; }
        @Override public int getIconHeight() { return size; }

        @Override public void paintIcon(Component c, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.translate(x, y);
            double scale = size / 32.0;
            g.scale(scale, scale);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g.setColor(color);
            g.setStroke(stroke(strokeWidth / (float) scale));
            draw(name, g);
            g.dispose();
        }

        private Stroke stroke(float width) {
            return new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        }
    }

    private static void draw(Name name, Graphics2D g) {
        switch (name) {
            case DASHBOARD -> drawDashboard(g);
            case TRAIN -> drawTrain(g);
            case ROUTE -> drawRoute(g);
            case CALENDAR -> drawCalendar(g);
            case TICKET -> drawTicket(g);
            case INVOICE -> drawReceipt(g);
            case SEARCH -> drawSearch(g);
            case FILTER -> drawFilter(g);
            case ADD -> drawPlus(g);
            case EDIT -> drawEdit(g);
            case DELETE -> drawTrash(g);
            case SAVE -> drawSave(g);
            case CLOSE -> drawX(g);
            case WARNING -> drawWarning(g);
            case SUCCESS -> drawCheck(g);
            case INFO -> drawInfo(g);
            case USER -> drawUser(g);
            case MONEY -> drawMoney(g);
            case PROMOTION -> drawPromotion(g);
            case PRINT -> drawPrint(g);
            case EXPORT -> drawExport(g);
            case LOGOUT -> drawLogout(g);
            case CHEVRON_RIGHT -> drawChevronRight(g);
            case CHEVRON_DOWN -> drawChevronDown(g);
        }
    }

    private static void drawDashboard(Graphics2D g) {
        g.drawRoundRect(6, 7, 8, 8, 2, 2);
        g.drawRoundRect(18, 7, 8, 8, 2, 2);
        g.drawRoundRect(6, 19, 8, 6, 2, 2);
        g.drawRoundRect(18, 19, 8, 6, 2, 2);
    }

    private static void drawTrain(Graphics2D g) {
        g.drawRoundRect(8, 7, 16, 17, 5, 5);
        g.drawLine(8, 14, 24, 14);
        g.drawLine(12, 7, 12, 14);
        g.drawLine(20, 7, 20, 14);
        g.drawLine(12, 24, 9, 28);
        g.drawLine(20, 24, 23, 28);
        g.drawLine(10, 28, 22, 28);
        g.draw(new Ellipse2D.Double(10, 17, 3, 3));
        g.draw(new Ellipse2D.Double(19, 17, 3, 3));
    }

    private static void drawRoute(Graphics2D g) {
        g.draw(new Ellipse2D.Double(6, 7, 5, 5));
        g.draw(new Ellipse2D.Double(21, 20, 5, 5));
        Path2D path = new Path2D.Double();
        path.moveTo(11, 10); path.curveTo(22, 9, 10, 23, 21, 23);
        g.draw(path);
    }

    private static void drawCalendar(Graphics2D g) {
        g.drawRoundRect(6, 8, 20, 18, 3, 3); g.drawLine(6, 13, 26, 13);
        g.drawLine(11, 5, 11, 10); g.drawLine(21, 5, 21, 10);
        g.drawLine(11, 18, 13, 18); g.drawLine(16, 18, 18, 18); g.drawLine(21, 18, 23, 18);
    }

    private static void drawTicket(Graphics2D g) {
        Path2D p = new Path2D.Double();
        p.moveTo(8, 10); p.lineTo(24, 10); p.quadTo(26, 10, 26, 12); p.lineTo(26, 14);
        p.curveTo(23, 14, 23, 18, 26, 18); p.lineTo(26, 20); p.quadTo(26, 22, 24, 22);
        p.lineTo(8, 22); p.quadTo(6, 22, 6, 20); p.lineTo(6, 18); p.curveTo(9, 18, 9, 14, 6, 14);
        p.lineTo(6, 12); p.quadTo(6, 10, 8, 10); p.closePath();
        g.draw(p); g.drawLine(15, 12, 15, 14); g.drawLine(15, 17, 15, 20);
    }

    private static void drawReceipt(Graphics2D g) {
        Path2D p = new Path2D.Double();
        p.moveTo(9, 6); p.lineTo(23, 6); p.quadTo(25, 6, 25, 8); p.lineTo(25, 26);
        p.lineTo(21, 23); p.lineTo(18, 26); p.lineTo(15, 23); p.lineTo(12, 26); p.lineTo(8, 23); p.lineTo(8, 8); p.quadTo(8, 6, 9, 6);
        g.draw(p); g.drawLine(12, 12, 21, 12); g.drawLine(12, 17, 21, 17);
    }

    private static void drawSearch(Graphics2D g) { g.draw(new Ellipse2D.Double(7, 7, 13, 13)); g.drawLine(18, 18, 25, 25); }
    private static void drawFilter(Graphics2D g) { Path2D p = new Path2D.Double(); p.moveTo(6, 8); p.lineTo(26, 8); p.lineTo(19, 16); p.lineTo(19, 24); p.lineTo(13, 27); p.lineTo(13, 16); p.closePath(); g.draw(p); }
    private static void drawPlus(Graphics2D g) { g.drawLine(16, 7, 16, 25); g.drawLine(7, 16, 25, 16); }
    private static void drawEdit(Graphics2D g) {
        Path2D pencil = new Path2D.Double();
        pencil.moveTo(9, 22); pencil.lineTo(11, 17); pencil.lineTo(22, 6); pencil.lineTo(26, 10);
        pencil.lineTo(15, 21); pencil.lineTo(9, 22); pencil.closePath();
        g.draw(pencil); g.drawLine(20, 8, 24, 12); g.drawLine(8, 26, 24, 26);
    }
    private static void drawTrash(Graphics2D g) { g.drawLine(8, 10, 24, 10); g.drawLine(13, 6, 19, 6); g.drawRoundRect(10, 10, 12, 17, 2, 2); g.drawLine(14, 14, 14, 23); g.drawLine(18, 14, 18, 23); }
    private static void drawSave(Graphics2D g) { g.drawRoundRect(7, 6, 18, 20, 3, 3); g.drawLine(11, 6, 11, 13); g.drawLine(11, 13, 21, 13); g.drawLine(21, 6, 21, 13); g.drawRoundRect(11, 18, 10, 8, 2, 2); }
    private static void drawX(Graphics2D g) { g.drawLine(9, 9, 23, 23); g.drawLine(23, 9, 9, 23); }
    private static void drawWarning(Graphics2D g) { Path2D p = new Path2D.Double(); p.moveTo(16, 6); p.lineTo(27, 26); p.lineTo(5, 26); p.closePath(); g.draw(p); g.drawLine(16, 13, 16, 19); g.fillOval(15, 22, 2, 2); }
    private static void drawCheck(Graphics2D g) { g.drawLine(7, 17, 13, 23); g.drawLine(13, 23, 25, 9); }
    private static void drawInfo(Graphics2D g) { g.draw(new Ellipse2D.Double(6, 6, 20, 20)); g.drawLine(16, 14, 16, 22); g.fillOval(15, 9, 2, 2); }
    private static void drawUser(Graphics2D g) { g.draw(new Ellipse2D.Double(11, 7, 10, 10)); g.draw(new Ellipse2D.Double(7, 20, 18, 8)); }
    private static void drawMoney(Graphics2D g) {
        g.drawRoundRect(6, 9, 20, 14, 4, 4);
        g.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 16));
        g.drawString("$", 12, 21);
    }
    private static void drawPromotion(Graphics2D g) { g.drawRoundRect(6, 8, 20, 16, 4, 4); g.drawLine(11, 21, 21, 11); g.draw(new Ellipse2D.Double(10, 11, 3, 3)); g.draw(new Ellipse2D.Double(19, 18, 3, 3)); }
    private static void drawPrint(Graphics2D g) { g.drawRoundRect(8, 14, 16, 9, 2, 2); g.drawRoundRect(10, 6, 12, 8, 2, 2); g.drawRoundRect(11, 21, 10, 6, 1, 1); }
    private static void drawExport(Graphics2D g) { g.drawRoundRect(7, 9, 18, 17, 3, 3); g.drawLine(16, 5, 16, 17); g.drawLine(11, 10, 16, 5); g.drawLine(21, 10, 16, 5); }
    private static void drawLogout(Graphics2D g) { g.drawRoundRect(6, 7, 12, 18, 3, 3); g.drawLine(16, 16, 27, 16); g.drawLine(23, 12, 27, 16); g.drawLine(23, 20, 27, 16); }
    private static void drawChevronRight(Graphics2D g) { g.drawLine(12, 9, 20, 16); g.drawLine(20, 16, 12, 23); }
    private static void drawChevronDown(Graphics2D g) { g.drawLine(9, 12, 16, 20); g.drawLine(16, 20, 23, 12); }

    private LineIcons() {}
}
