package com.graphics;

import java.awt.*;
import java.awt.geom.*;
import java.util.List;
import java.util.Random;

/**
 * Hệ thống đồ họa tàu Java2D — thiết kế modular, tái sử dụng cao.
 *
 * Cách dùng chuẩn (tự chứa trong panel):
 *   TrainGraphics.paintTrain(g2, areaX, areaY, areaW, areaH, cars, dir);
 *
 * Hệ thống tọa độ:
 *   - train chiếm toàn bộ area truyền vào
 *   - locomotive cao nhất, cars thấp hơn, rail ở dưới cùng
 *   - Mọi pixel được tính tỷ lệ theo areaH để đảm bảo vừa panel
 */
public class TrainGraphics {

    // ===================================================================
    //  ENUMS & DATA
    // ===================================================================

    public enum TrainDirection { LEFT_TO_RIGHT, RIGHT_TO_LEFT }
    public enum CarType {
        TOA_KHACH("Khách"), TOA_GHE_NGOI("Ghế ngồi"),
        TOA_GIUONG_NAM("Giường nằm"), TOA_GHE_VIP("Ghế VIP"), TOA_HANG("Hàng");
        private final String label;
        CarType(String l) { this.label = l; }
        public String label() { return label; }
    }

    public static class TrainCarData {
        private Color primaryColor;
        private CarType carType;
        private int windowCount;
        private float heightScale;

        public TrainCarData(Color c, CarType t, int w) { this(c, t, w, 1.0f); }
        public TrainCarData(Color c, CarType t, int w, float hs) {
            this.primaryColor = c != null ? c : Color.GRAY;
            this.carType = t != null ? t : CarType.TOA_KHACH;
            this.windowCount = Math.max(1, w);
            this.heightScale = Math.max(0.5f, Math.min(2.0f, hs));
        }
        public Color getPrimaryColor() { return primaryColor; }
        public void setPrimaryColor(Color c) { this.primaryColor = c; }
        public CarType getCarType() { return carType; }
        public int getWindowCount() { return windowCount; }
        public float getHeightScale() { return heightScale; }
    }

    // ===================================================================
    //  MAIN PAINT — tự chứa trong area truyền vào
    // ===================================================================

    /**
     * Vẽ train nằm ngang, tự fit trong area truyền vào.
     * @param g2 Graphics2D
     * @param areaX left edge của khu vực vẽ
     * @param areaY top edge của khu vực vẽ
     * @param areaW chiều rộng khu vực vẽ
     * @param areaH chiều cao khu vực vẽ (dùng để tính tỷ lệ)
     * @param cars danh sách toa (locomotive luôn ở đầu)
     * @param dir hướng (LEFT_TO_RIGHT = đầu máy bên trái)
     */
    public static void paintTrain(Graphics2D g2, int areaX, int areaY,
                                   int areaW, int areaH,
                                   List<TrainCarData> cars, TrainDirection dir) {
        if (g2 == null || areaW <= 0 || areaH <= 0) return;
        g2 = (Graphics2D) g2.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Scale factor: dựa trên chiều cao area
        float s = areaH / 120f;

        // Locomotive: cao ~80% areaH, rộng ~20% areaW (tối thiểu)
        int locoW = Math.max(areaW / 5, (int)(90 * s));
        int locoH = (int)(80 * s);

        // Mỗi toa: cao ~65% areaH, rộng ~15% areaW
        int carW  = Math.max(areaW / 7, (int)(80 * s));
        int carH  = (int)(65 * s);
        int gap   = Math.max(4, (int)(6 * s));

        // Tính tổng chiều rộng train
        int trainContentW = locoW + gap + cars.size() * carW + (cars.size() - 1) * gap;
        // Căng giữa area
        int totalTrainW = trainContentW;
        int availW = areaW;
        int startX;
        if (totalTrainW <= availW) {
            startX = areaX + (availW - totalTrainW) / 2;
        } else {
            startX = areaX;
        }

        // Tọa độ Y cơ sở (đáy train)
        int railH = Math.max(6, (int)(8 * s));
        int baseY = areaY + areaH - railH;  // rail nằm ở đáy

        // Vẽ rail trước
        int railX = dir == TrainDirection.LEFT_TO_RIGHT
                ? startX
                : startX + (totalTrainW - (int)(totalTrainW * 0.45));
        RailPainter.paintRail(g2, railX, baseY, (int)(totalTrainW * 0.45), s);

        // Vị trí locomotive (đầu máy)
        int locoX = dir == TrainDirection.LEFT_TO_RIGHT
                ? startX
                : startX + totalTrainW - locoW;
        LocomotivePainter.paint(g2, locoX, baseY - locoH, locoW, locoH,
                TrainColors.SUNSET_RED, dir, s);

        // Vị trí các toa
        for (int i = 0; i < cars.size(); i++) {
            TrainCarData car = cars.get(i);
            int carX;
            if (dir == TrainDirection.LEFT_TO_RIGHT) {
                carX = startX + locoW + gap + i * (carW + gap);
            } else {
                carX = startX + totalTrainW - locoW - gap - (i + 1) * carW - i * gap;
            }
            int scaledCarH = (int)(carH * car.getHeightScale());
            TrainCarPainter.paint(g2, carX, baseY - scaledCarH - railH / 2, carW, scaledCarH,
                    car.getPrimaryColor(), dir, s);

            // Coupler giữa các toa
            if (i < cars.size() - 1) {
                int nextCarX = dir == TrainDirection.LEFT_TO_RIGHT
                        ? carX + carW + gap
                        : carX - carW - gap;
                g2.setColor(new Color(0x2D, 0x34, 0x36));
                g2.setStroke(new BasicStroke(Math.max(1.5f, 2 * s)));
                int cx1 = carX + carW / 2;
                int cx2 = nextCarX + carW / 2;
                g2.drawLine(cx1, baseY - (int)(3 * s), cx2, baseY - (int)(3 * s));
            }
        }

        g2.dispose();
    }

    /**
     * Tính chiều rộng tổng cộng của train (không tính margin căn giữa).
     * Dùng cho việc đặt kích thước canvas.
     */
    public static int getTrainWidth(List<TrainCarData> cars, float scale, int areaWidth) {
        if (cars == null || cars.isEmpty()) return (int)(90 * scale);
        float s = Math.max(0.1f, scale);
        int locoW = Math.max(areaWidth / 5, (int)(90 * s));
        int carW  = Math.max(areaWidth / 7, (int)(80 * s));
        int gap   = Math.max(4, (int)(6 * s));
        return locoW + gap + cars.size() * carW + (cars.size() - 1) * gap;
    }

    /**
     * Chiều cao tối đa của train (locomotive + rail + padding).
     */
    public static int getTrainHeight(float scale) {
        return (int)(90 * Math.max(0.1f, scale));
    }

    // ===================================================================
    //  LOCOMOTIVE PAINTER
    // ===================================================================

    public static class LocomotivePainter {

        public static void paint(Graphics2D g2, int x, int y, int w, int h,
                                 Color[] colors, TrainDirection dir, float s) {
            if (g2 == null || w <= 2 || h <= 2) return;
            g2 = (Graphics2D) g2.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean flip = (dir == TrainDirection.RIGHT_TO_LEFT);

            // ── Shadow ──────────────────────────────────────────────────
            g2.setColor(new Color(0, 0, 0, 30));
            g2.fillRoundRect(x + (int)(3 * s), y + (int)(3 * s), w, h,
                    (int)(8 * s), (int)(8 * s));

            // ── Body (phần chính của đầu máy) ─────────────────────────
            int bodyX = x + (int)(w * 0.05);
            int bodyW = w - (int)(w * 0.10);
            int bodyY = y + (int)(h * 0.18);
            int bodyH = (int)(h * 0.65);

            GradientPaint bodyGrad;
            if (colors != null && colors.length >= 2) {
                bodyGrad = new GradientPaint(bodyX, bodyY, colors[0],
                        bodyX, bodyY + bodyH, colors[1]);
            } else {
                bodyGrad = new GradientPaint(bodyX, bodyY, new Color(0xC0, 0x20, 0x20),
                        bodyX, bodyY + bodyH, new Color(0x80, 0x10, 0x10));
            }
            g2.setPaint(bodyGrad);
            g2.fillRoundRect(bodyX, bodyY, bodyW, bodyH, (int)(10 * s), (int)(10 * s));
            g2.setColor(new Color(0, 0, 0, 60));
            g2.setStroke(new BasicStroke(Math.max(1f, 1.5f * s)));
            g2.drawRoundRect(bodyX, bodyY, bodyW, bodyH, (int)(10 * s), (int)(10 * s));

            // ── Vàng stripe ────────────────────────────────────────────
            int stripeY = bodyY + (int)(bodyH * 0.28);
            int stripeH2 = Math.max(2, (int)(bodyH * 0.07));
            g2.setColor(new Color(0xFF, 0xD7, 0x00, 200));
            g2.fillRect(bodyX + (int)(w * 0.04), stripeY,
                    bodyW - (int)(w * 0.08), stripeH2);

            // ── Roof dome (vòm nóc) ─────────────────────────────────────
            int roofW = (int)(w * 0.30);
            int roofH = (int)(h * 0.22);
            int roofX = flip ? bodyX + bodyW - (int)(w * 0.18) : bodyX + (int)(w * 0.12);
            int roofY = y;
            g2.setPaint(new GradientPaint(roofX, roofY, new Color(0x1A, 0x1A, 0x2E),
                    roofX, roofY + roofH, new Color(0x2D, 0x2D, 0x4A)));
            g2.fill(new Ellipse2D.Double(roofX, roofY, roofW, roofH * 1.5));
            g2.setColor(new Color(0x10, 0x10, 0x20));
            g2.setStroke(new BasicStroke(Math.max(1f, 1.5f * s)));
            g2.draw(new Ellipse2D.Double(roofX, roofY, roofW, roofH * 1.5));

            // ── Smokestack (ống khói) ───────────────────────────────────
            int chimW = (int)(w * 0.13);
            int chimH = (int)(h * 0.28);
            int chimX = flip ? roofX - (int)(w * 0.04) : roofX + roofW - (int)(w * 0.06);
            int chimY = roofY - chimH + (int)(roofH * 0.3);
            g2.setPaint(new GradientPaint(chimX, chimY, new Color(0x2D, 0x2D, 0x2D),
                    chimX + chimW, chimY, new Color(0x55, 0x55, 0x55)));
            g2.fill(new Ellipse2D.Double(chimX, chimY, chimW, chimH));
            g2.setColor(Color.DARK_GRAY);
            g2.setStroke(new BasicStroke(Math.max(1f, 1f * s)));
            g2.draw(new Ellipse2D.Double(chimX, chimY, chimW, chimH));

            // ── Windows (cửa sổ đầu máy) ───────────────────────────────
            int winW = Math.max(4, (int)(14 * s));
            int winH = Math.max(4, (int)(12 * s));
            int winX = bodyX + (int)(w * 0.25);
            int winY = bodyY + (int)(bodyH * 0.15);
            // border
            g2.setColor(new Color(0xFF, 0xD7, 0x00, 220));
            g2.fillRoundRect(winX - 1, winY - 1, winW + 2, winH + 2,
                    (int)(3 * s), (int)(3 * s));
            // glass
            g2.setPaint(new GradientPaint(winX, winY, new Color(0xA8, 0xD8, 0xF0, 220),
                    winX, winY + winH, new Color(0x60, 0xA0, 0xC0, 180)));
            g2.fillRoundRect(winX, winY, winW, winH, (int)(3 * s), (int)(3 * s));

            // ── Logo VR ────────────────────────────────────────────────
            int logoW = Math.max(8, (int)(24 * s));
            int logoH = Math.max(6, (int)(16 * s));
            g2.setColor(new Color(0xFF, 0xD7, 0x00));
            g2.setFont(new Font("Segoe UI", Font.BOLD, Math.max(8, (int)(10 * s))));
            FontMetrics fm = g2.getFontMetrics();
            int logoTextW = fm.stringWidth("VR");
            int logoX = bodyX + (bodyW - logoTextW) / 2;
            int logoY = bodyY + bodyH - (int)(bodyH * 0.20);
            g2.drawString("VR", logoX, logoY);

            // ── Driver wheels (bánh xe lớn) ────────────────────────────
            int wheelR = Math.max(6, (int)(h * 0.14));
            int wheelY = y + h - wheelR;
            // 3 driver wheels
            int dwcx1 = x + (int)(w * 0.30);
            int dwcx2 = x + (int)(w * 0.52);
            int dwcx3 = x + (int)(w * 0.74);
            WheelPainter.paintDriver(g2, dwcx1, wheelY, wheelR, new Color(0x18, 0x18, 0x18));
            WheelPainter.paintDriver(g2, dwcx2, wheelY, wheelR, new Color(0x18, 0x18, 0x18));
            WheelPainter.paintDriver(g2, dwcx3, wheelY, wheelR, new Color(0x18, 0x18, 0x18));
            // counterweight trên bánh
            for (int cx : new int[]{dwcx1, dwcx2, dwcx3}) {
                g2.setColor(new Color(0x40, 0x40, 0x40));
                g2.setStroke(new BasicStroke(Math.max(1.5f, 2 * s)));
                g2.drawLine(cx, wheelY - wheelR + (int)(wheelR * 0.3),
                        cx, wheelY - wheelR - (int)(wheelR * 0.2));
            }

            // ── Headlight (đèn pha) ────────────────────────────────────
            int hlX = flip ? bodyX : bodyX + bodyW - (int)(w * 0.06);
            int hlY = bodyY + (int)(bodyH * 0.42);
            int hlR = Math.max(3, (int)(5 * s));
            g2.setColor(new Color(0xFF, 0xD7, 0x00));
            g2.fill(new Ellipse2D.Double(hlX - hlR, hlY - hlR, hlR * 2, hlR * 2));
            g2.setColor(Color.WHITE);
            g2.fill(new Ellipse2D.Double(hlX - (int)(hlR * 0.5), hlY - (int)(hlR * 0.5),
                    (int)(hlR * 1.0), (int)(hlR * 1.0)));

            g2.dispose();
        }
    }

    // ===================================================================
    //  TRAIN CAR PAINTER
    // ===================================================================

    public static class TrainCarPainter {

        public static void paint(Graphics2D g2, int x, int y, int w, int h,
                                 Color color, TrainDirection dir, float s) {
            if (g2 == null || w <= 2 || h <= 2) return;
            g2 = (Graphics2D) g2.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // ── Shadow ──────────────────────────────────────────────────
            g2.setColor(new Color(0, 0, 0, 25));
            g2.fillRoundRect(x + (int)(2 * s), y + (int)(3 * s), w, h,
                    (int)(6 * s), (int)(6 * s));

            // ── Body ───────────────────────────────────────────────────
            int bodyX = x;
            int bodyY = y - h;  // thân cao hơn điểm anchor (anchor ở dưới cùng)
            int bodyW = w;
            int bodyH = h;

            g2.setPaint(new GradientPaint(bodyX, bodyY, darker(color, 0.78f),
                    bodyX + bodyW, bodyY, color));
            g2.fillRoundRect(bodyX, bodyY, bodyW, bodyH, (int)(6 * s), (int)(6 * s));
            g2.setColor(new Color(0, 0, 0, 40));
            g2.setStroke(new BasicStroke(Math.max(1f, 1.5f * s)));
            g2.drawRoundRect(bodyX, bodyY, bodyW, bodyH, (int)(6 * s), (int)(6 * s));

            // ── Vàng stripe ────────────────────────────────────────────
            int stripeY = bodyY + (int)(bodyH * 0.50);
            int stripeH2 = Math.max(1, (int)(bodyH * 0.05));
            g2.setColor(new Color(0xFF, 0xD7, 0x00, 170));
            g2.fillRect(bodyX + (int)(w * 0.03), stripeY,
                    bodyW - (int)(w * 0.06), stripeH2);

            // ── Windows ────────────────────────────────────────────────
            int winCount = Math.max(1, (int)(bodyW / (Math.max(1, (int)(24 * s)))));
            winCount = Math.min(winCount, 8);
            int winW = Math.max(4, (int)(18 * s));
            int winH = Math.max(4, (int)(14 * s));
            int winGap = Math.max(2, (int)(5 * s));
            int totalWinW = winCount * winW + (winCount - 1) * winGap;
            int winStartX = bodyX + (bodyW - totalWinW) / 2;
            int winY = bodyY + (int)(bodyH * 0.16);

            for (int i = 0; i < winCount; i++) {
                int wx = winStartX + i * (winW + winGap);
                g2.setColor(new Color(0xFF, 0xD7, 0x00, 200));
                g2.fillRoundRect(wx - 1, winY - 1, winW + 2, winH + 2,
                        (int)(3 * s), (int)(3 * s));
                g2.setPaint(new GradientPaint(wx, winY, new Color(0xA8, 0xD8, 0xF0, 210),
                        wx, winY + winH, new Color(0x60, 0xA8, 0xD0, 170)));
                g2.fillRoundRect(wx, winY, winW, winH, (int)(3 * s), (int)(3 * s));
            }

            // ── Dual wheels ────────────────────────────────────────────
            int wheelR = Math.max(5, (int)(h * 0.18));
            int wheelY = y;  // đáy body = đỉnh bánh xe
            int wcx1 = x + (int)(w * 0.28);
            int wcx2 = x + (int)(w * 0.72);
            WheelPainter.paintDual(g2, wcx1, wheelY, wheelR, color);
            WheelPainter.paintDual(g2, wcx2, wheelY, wheelR, color);

            // ── Undercarriage ──────────────────────────────────────────
            g2.setColor(new Color(0x30, 0x30, 0x30));
            g2.setStroke(new BasicStroke(Math.max(1f, 1.5f * s)));
            g2.drawLine(x + (int)(w * 0.1), y - (int)(2 * s),
                    x + w - (int)(w * 0.1), y - (int)(2 * s));

            g2.dispose();
        }
    }

    // ===================================================================
    //  WHEEL PAINTER
    // ===================================================================

    public static class WheelPainter {

        public static void paint(Graphics2D g2, int cx, int cy, int radius,
                                 int spokes, Color rimColor) {
            if (g2 == null || radius <= 0) return;
            g2 = (Graphics2D) g2.create();

            // Outer rim
            g2.setPaint(new GradientPaint(cx - radius, cy - radius, new Color(0x50, 0x50, 0x50),
                    cx + radius, cy + radius, new Color(0x20, 0x20, 0x20)));
            g2.fill(new Ellipse2D.Double(cx - radius, cy - radius, radius * 2, radius * 2));

            // Rim stroke
            g2.setColor(rimColor != null ? rimColor : new Color(0x70, 0x70, 0x70));
            g2.setStroke(new BasicStroke(Math.max(1f, radius / 6f)));
            g2.draw(new Ellipse2D.Double(cx - radius, cy - radius, radius * 2, radius * 2));

            // Inner hub
            int innerR = (int)(radius * 0.65);
            g2.setPaint(new GradientPaint(cx - innerR, cy - innerR, new Color(0x30, 0x30, 0x30),
                    cx + innerR, cy + innerR, new Color(0x18, 0x18, 0x18)));
            g2.fill(new Ellipse2D.Double(cx - innerR, cy - innerR, innerR * 2, innerR * 2));

            // Spokes
            if (spokes > 0) {
                g2.setColor(new Color(0x60, 0x60, 0x60));
                g2.setStroke(new BasicStroke(Math.max(1f, radius / 8f)));
                for (int i = 0; i < spokes; i++) {
                    double angle = 2 * Math.PI * i / spokes;
                    int ex = cx + (int)(radius * 0.85 * Math.cos(angle));
                    int ey = cy + (int)(radius * 0.85 * Math.sin(angle));
                    g2.drawLine(cx, cy, ex, ey);
                }
            }

            // Center hub
            int hubR = Math.max(2, (int)(radius * 0.2));
            g2.setColor(new Color(0x80, 0x80, 0x80));
            g2.fill(new Ellipse2D.Double(cx - hubR, cy - hubR, hubR * 2, hubR * 2));

            g2.dispose();
        }

        /** Bánh xe lớn của đầu máy */
        public static void paintDriver(Graphics2D g2, int cx, int cy, int radius, Color color) {
            if (g2 == null || radius <= 0) return;
            g2 = (Graphics2D) g2.create();

            // Outer tire
            g2.setPaint(new GradientPaint(cx - radius, cy - radius, new Color(0x40, 0x40, 0x40),
                    cx + radius, cy + radius, Color.BLACK));
            g2.fill(new Ellipse2D.Double(cx - radius, cy - radius, radius * 2, radius * 2));

            // Rim ring
            g2.setColor(color != null ? color : new Color(0x20, 0x20, 0x20));
            g2.setStroke(new BasicStroke(Math.max(1.5f, radius / 5f)));
            g2.draw(new Ellipse2D.Double(cx - radius, cy - radius, radius * 2, radius * 2));

            // Inner face
            int innerR = (int)(radius * 0.72);
            g2.setPaint(new GradientPaint(cx - innerR, cy - innerR, new Color(0x25, 0x25, 0x25),
                    cx + innerR, cy + innerR, new Color(0x10, 0x10, 0x10)));
            g2.fill(new Ellipse2D.Double(cx - innerR, cy - innerR, innerR * 2, innerR * 2));

            // Spokes
            int spokes = 6;
            g2.setColor(new Color(0x50, 0x50, 0x50));
            g2.setStroke(new BasicStroke(Math.max(1f, radius / 10f)));
            for (int i = 0; i < spokes; i++) {
                double angle = 2 * Math.PI * i / spokes;
                int ex = cx + (int)(radius * 0.90 * Math.cos(angle));
                int ey = cy + (int)(radius * 0.90 * Math.sin(angle));
                g2.drawLine(cx, cy, ex, ey);
            }

            // Center hub
            int hubR = Math.max(3, (int)(radius * 0.15));
            g2.setColor(new Color(0x90, 0x90, 0x90));
            g2.fill(new Ellipse2D.Double(cx - hubR, cy - hubR, hubR * 2, hubR * 2));

            g2.dispose();
        }

        /** Cặp bánh đôi của toa */
        public static void paintDual(Graphics2D g2, int cx, int cy, int radius, Color carColor) {
            if (g2 == null || radius <= 0) return;
            int offset = Math.max(2, (int)(radius * 0.45));
            paint(g2, cx - offset, cy, radius, 5, new Color(0x60, 0x60, 0x60));
            paint(g2, cx + offset, cy, radius, 5, new Color(0x60, 0x60, 0x60));
        }
    }

    // ===================================================================
    //  RAIL PAINTER
    // ===================================================================

    public static class RailPainter {

        public static void paintRail(Graphics2D g2, int x, int y, int width, float s) {
            if (g2 == null || width <= 0) return;
            g2 = (Graphics2D) g2.create();

            int railH = Math.max(4, (int)(8 * s));
            int tieH  = Math.max(2, (int)(3 * s));
            int tieW  = Math.max(2, (int)(4 * s));

            // Ground / ties (thanh nối)
            g2.setColor(new Color(0x5A, 0x40, 0x20));
            g2.fillRect(x, y, width, tieH);

            // Rail track (thanh ray)
            int railY = y - railH;
            g2.setPaint(new GradientPaint(x, railY, new Color(0x70, 0x70, 0x70),
                    x, railY + railH, new Color(0x40, 0x40, 0x40)));
            g2.fillRect(x, railY, width, railH);
            // Highlight
            g2.setColor(new Color(0x90, 0x90, 0x90, 150));
            g2.fillRect(x, railY, width, Math.max(1, railH / 3));

            g2.dispose();
        }
    }

    // ===================================================================
    //  TRAIN COLORS — bảng màu preset
    // ===================================================================

    public static class TrainColors {
        public static final Color[] AZURE_BLUE    = { new Color(0x00, 0x5D, 0x90), new Color(0x00, 0x3A, 0x60) };
        public static final Color[] FOREST_GREEN  = { new Color(0x1A, 0x7A, 0x3C), new Color(0x0D, 0x50, 0x25) };
        public static final Color[] SUNSET_RED     = { new Color(0xC0, 0x20, 0x20), new Color(0x80, 0x10, 0x10) };
        public static final Color[] PURPLE_ROYAL   = { new Color(0x6A, 0x30, 0x9A), new Color(0x40, 0x18, 0x65) };
        public static final Color[] SUNSET_ORANGE  = { new Color(0xE0, 0x60, 0x10), new Color(0xA0, 0x35, 0x00) };
        public static final Color[] SAND_YELLOW    = { new Color(0xC8, 0xA0, 0x30), new Color(0x90, 0x70, 0x10) };
        public static final Color[] CARBON_GRAY    = { new Color(0x45, 0x45, 0x50), new Color(0x28, 0x28, 0x30) };

        private static final Random rand = new Random();

        public static Color[] randomVibrant() {
            return new Color[]{
                    Color.getHSBColor(rand.nextFloat() * 0.3f + 0.55f, 0.75f, 0.65f),
                    Color.getHSBColor(rand.nextFloat() * 0.3f + 0.55f, 0.85f, 0.40f)
            };
        }
    }

    // ===================================================================
    //  UTILITY
    // ===================================================================

    private static Color darker(Color c, float factor) {
        return new Color(
                Math.max(0, (int)(c.getRed() * factor)),
                Math.max(0, (int)(c.getGreen() * factor)),
                Math.max(0, (int)(c.getBlue() * factor)),
                c.getAlpha());
    }
}