package com.modules;

import com.entity.Ghe;
import com.entity.ToaTau;
import com.enums.LoaiGhe;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class ToaSeatDiagramPanel extends JPanel {

    private ToaTau toa;
    private List<Ghe> gheList = new ArrayList<>();
    private Predicate<Ghe> unavailableResolver = ghe -> false;
    private Predicate<Ghe> selectedResolver = ghe -> false;
    private Function<Ghe, Double> priceResolver = ghe -> null;
    private Consumer<Ghe> seatClickHandler;
    private boolean selectable;
    private Ghe hoveredGhe;

    private static final int PADDING    = 28;
    private static final int SEAT_GAP   = 6;
    private static final int KHOANG_GAP = 18;
    private static final int LABEL_H    = 22;

    private static final int BED_W      = 52;
    private static final int BED_H      = 28;
    private static final int SEAT_W     = 42;
    private static final int SEAT_H     = 42;

    private static final Color CARD_BG        = NotionTheme.CARD;
    private static final Color ON_SURFACE     = NotionTheme.TEXT;
    private static final Color ON_SURF_VAR    = NotionTheme.TEXT_MUTED;
    private static final Color OUTLINE        = NotionTheme.BORDER;
    private static final Color PRIMARY        = NotionTheme.ACCENT;
    private static final Color PRIMARY_LIGHT  = NotionTheme.ACCENT_SOFT;

    private static final Color CLR_CUNG_FILL      = AppColors.SEAT_HARD_FILL;
    private static final Color CLR_CUNG_BORDER    = AppColors.SEAT_HARD_BORDER;
    private static final Color CLR_MEM_FILL       = AppColors.SEAT_SOFT_FILL;
    private static final Color CLR_MEM_BORDER     = NotionTheme.ACCENT;
    private static final Color CLR_GIUONG_FILL    = AppColors.SEAT_BED_FILL;
    private static final Color CLR_GIUONG_BORDER  = AppColors.SUCCESS_DARK;
    private static final Color CLR_KHOANG_LINE    = AppColors.BORDER;
    private static final Color UNAVAILABLE_FILL   = AppColors.SEAT_UNAVAILABLE_FILL;
    private static final Color UNAVAILABLE_BORDER = AppColors.SEAT_UNAVAILABLE_BORDER;
    private static final Color SELECTED_FILL      = NotionTheme.ACCENT;
    private static final Color SELECTED_BORDER    = NotionTheme.ACCENT_HOVER;

    public ToaSeatDiagramPanel() {
        setOpaque(true);
        setBackground(CARD_BG);
        ToolTipManager.sharedInstance().registerComponent(this);
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                Ghe ghe = getSeatAtPoint(e.getPoint());
                if (ghe == null || !selectable || isUnavailable(ghe)) return;
                if (seatClickHandler != null) seatClickHandler.accept(ghe);
                repaint();
            }

            @Override public void mouseExited(MouseEvent e) {
                if (hoveredGhe != null) {
                    hoveredGhe = null;
                    repaint();
                }
                setCursor(Cursor.getDefaultCursor());
            }
        });
        addMouseMotionListener(new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                Ghe hit = getSeatAtPoint(e.getPoint());
                if (!Objects.equals(hit, hoveredGhe)) {
                    hoveredGhe = hit;
                    repaint();
                }
                setCursor(hit != null && selectable && !isUnavailable(hit)
                        ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        : Cursor.getDefaultCursor());
            }
        });
    }

    public void setData(ToaTau toa, List<Ghe> ghes) {
        this.toa = toa;
        this.gheList = ghes == null
                ? new ArrayList<>()
                : ghes.stream().sorted(Comparator.comparingInt(Ghe::getSoGhe)).toList();
        this.hoveredGhe = null;
        revalidate();
        repaint();
    }

    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    public void setUnavailableResolver(Predicate<Ghe> unavailableResolver) {
        this.unavailableResolver = unavailableResolver == null ? ghe -> false : unavailableResolver;
        repaint();
    }

    public void setSelectedResolver(Predicate<Ghe> selectedResolver) {
        this.selectedResolver = selectedResolver == null ? ghe -> false : selectedResolver;
        repaint();
    }

    public void setPriceResolver(Function<Ghe, Double> priceResolver) {
        this.priceResolver = priceResolver == null ? ghe -> null : priceResolver;
        repaint();
        // Handoff: Step chọn ghế truyền giá theo lịch/tuyến để tooltip không tự truy DB.
        // Risk: resolver trả null/0 thì tooltip hiển thị trạng thái chưa có giá thay vì crash.
    }

    public void setSeatClickHandler(Consumer<Ghe> seatClickHandler) {
        this.seatClickHandler = seatClickHandler;
    }

    @Override public Dimension getPreferredSize() {
        LoaiGhe loaiGhe = toa != null ? toa.getLoaiGhe() : null;
        if (loaiGhe == LoaiGhe.GIUONG_NAM) {
            int cols = 10, khoang = 5;
            int totalW = PADDING * 2 + cols * BED_W + (cols - 1) * SEAT_GAP + (khoang - 1) * KHOANG_GAP;
            int totalH = PADDING * 2 + LABEL_H + 3 * BED_H + 2 * SEAT_GAP;
            return new Dimension(totalW, totalH);
        }
        int cols = 12, khoang = 2;
        int totalW = PADDING * 2 + cols * SEAT_W + (cols - 1) * SEAT_GAP + (khoang - 1) * KHOANG_GAP;
        int totalH = PADDING * 2 + LABEL_H + 4 * SEAT_H + 3 * SEAT_GAP;
        return new Dimension(totalW, totalH);
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (toa == null) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        LoaiGhe lg = toa.getLoaiGhe();
        if (lg == LoaiGhe.GIUONG_NAM) paintGiuong(g2);
        else paintGhe(g2, lg);
        g2.dispose();
    }

    private void paintGiuong(Graphics2D g2) {
        int rows = 3, cols = 10, colsPerKhoang = 2, khoang = cols / colsPerKhoang;
        paintGrid(g2, rows, cols, colsPerKhoang, khoang, BED_W, BED_H, toa.getLoaiGhe());
    }

    private void paintGhe(Graphics2D g2, LoaiGhe lg) {
        int rows = 4, cols = 12, colsPerKhoang = 6, khoang = cols / colsPerKhoang;
        paintGrid(g2, rows, cols, colsPerKhoang, khoang, SEAT_W, SEAT_H, lg);
    }

    private void paintGrid(Graphics2D g2, int rows, int cols, int colsPerKhoang, int khoang, int w, int h, LoaiGhe lg) {
        Font fontKhoang = new Font("Segoe UI", Font.BOLD, 11);
        int x0 = PADDING;
        int y0 = PADDING + LABEL_H;

        for (int kh = 0; kh < khoang; kh++) {
            int khoangX = x0 + kh * colsPerKhoang * (w + SEAT_GAP) + kh * KHOANG_GAP;
            String label = "Khoang " + (kh + 1);
            g2.setFont(fontKhoang);
            g2.setColor(ON_SURF_VAR);
            FontMetrics fm = g2.getFontMetrics();
            int khoangW = colsPerKhoang * w + (colsPerKhoang - 1) * SEAT_GAP;
            g2.drawString(label, khoangX + (khoangW - fm.stringWidth(label)) / 2, PADDING + LABEL_H - 5);

            for (int col = 0; col < colsPerKhoang; col++) {
                int globalCol = kh * colsPerKhoang + col;
                int cx = khoangX + col * (w + SEAT_GAP);
                for (int row = 0; row < rows; row++) {
                    int gy = y0 + row * (h + SEAT_GAP);
                    int seatNo = row * cols + globalCol + 1;
                    Ghe ghe = seatNo <= gheList.size() ? gheList.get(seatNo - 1) : null;
                    drawSeatShape(g2, cx, gy, w, h, seatNo, ghe, Objects.equals(ghe, hoveredGhe), lg);
                }
            }

            if (kh < khoang - 1) {
                int lineX = khoangX + colsPerKhoang * w + (colsPerKhoang - 1) * SEAT_GAP + KHOANG_GAP / 2;
                drawKhoangDivider(g2, lineX, y0 - 4, y0 + rows * h + (rows - 1) * SEAT_GAP + 4);
            }
        }
    }

    private void drawSeatShape(Graphics2D g2, int x, int y, int w, int h, int seatNo, Ghe ghe, boolean hover, LoaiGhe lg) {
        boolean unavailable = ghe != null && isUnavailable(ghe);
        boolean selected = ghe != null && isSelected(ghe);
        Color fill = fillColor(lg, hover && ghe != null && !unavailable, unavailable, selected);
        Color border = borderColor(lg, unavailable, selected);
        Color textColor = selected ? AppColors.SURFACE : unavailable ? ON_SURF_VAR : ON_SURFACE;
        int arc = lg == LoaiGhe.GIUONG_NAM ? 5 : 6;

        g2.setColor(new Color(0, 0, 0, unavailable ? 8 : 18));
        g2.fillRoundRect(x + 2, y + 2, w, h, arc, arc);
        g2.setColor(fill);
        g2.fillRoundRect(x, y, w, h, arc, arc);
        g2.setColor(border);
        g2.setStroke(new BasicStroke(selected || hover ? 2f : 1.5f));
        g2.drawRoundRect(x, y, w, h, arc, arc);

        String numStr = String.valueOf(ghe != null && ghe.getSoGhe() > 0 ? ghe.getSoGhe() : seatNo);
        g2.setFont(new Font("Segoe UI", Font.BOLD, w > 40 ? 10 : 9));
        g2.setColor(textColor);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(numStr, x + (w - fm.stringWidth(numStr)) / 2, y + (h + fm.getAscent() - fm.getDescent()) / 2);

        // Handoff: hover detail nằm trong tooltip; không vẽ mã ghế lên ô để tránh đè số ghế.
        // Risk: nếu cần thêm dữ liệu hover, ưu tiên tooltip thay vì nhồi chữ vào ô nhỏ.
    }

    private Color fillColor(LoaiGhe lg, boolean hover, boolean unavailable, boolean selected) {
        if (selected) return SELECTED_FILL;
        if (unavailable) return UNAVAILABLE_FILL;
        if (hover && selectable) return PRIMARY_LIGHT;
        if (lg == LoaiGhe.GIUONG_NAM) return CLR_GIUONG_FILL;
        if (lg == LoaiGhe.GHE_MEM) return CLR_MEM_FILL;
        return CLR_CUNG_FILL;
    }

    private Color borderColor(LoaiGhe lg, boolean unavailable, boolean selected) {
        if (selected) return SELECTED_BORDER;
        if (unavailable) return UNAVAILABLE_BORDER;
        if (lg == LoaiGhe.GIUONG_NAM) return CLR_GIUONG_BORDER;
        if (lg == LoaiGhe.GHE_MEM) return CLR_MEM_BORDER;
        return CLR_CUNG_BORDER;
    }

    private void drawKhoangDivider(Graphics2D g2, int x, int y1, int y2) {
        g2.setColor(CLR_KHOANG_LINE);
        g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float[]{5f, 4f}, 0f));
        g2.drawLine(x, y1, x, y2);
        g2.setStroke(new BasicStroke(1));
    }

    private Ghe getSeatAtPoint(Point p) {
        LoaiGhe lg = toa != null ? toa.getLoaiGhe() : null;
        if (lg == null) return null;
        int seatNo = lg == LoaiGhe.GIUONG_NAM
                ? hitTestGrid(p, 3, 10, 2, BED_W, BED_H)
                : hitTestGrid(p, 4, 12, 6, SEAT_W, SEAT_H);
        return seatNo >= 1 && seatNo <= gheList.size() ? gheList.get(seatNo - 1) : null;
    }

    private int hitTestGrid(Point p, int rows, int cols, int colsPerKhoang, int w, int h) {
        int khoang = cols / colsPerKhoang;
        int x0 = PADDING;
        int y0 = PADDING + LABEL_H;
        for (int kh = 0; kh < khoang; kh++) {
            int khoangX = x0 + kh * colsPerKhoang * (w + SEAT_GAP) + kh * KHOANG_GAP;
            for (int col = 0; col < colsPerKhoang; col++) {
                int globalCol = kh * colsPerKhoang + col;
                int cx = khoangX + col * (w + SEAT_GAP);
                for (int row = 0; row < rows; row++) {
                    int gy = y0 + row * (h + SEAT_GAP);
                    if (new Rectangle(cx, gy, w, h).contains(p)) return row * cols + globalCol + 1;
                }
            }
        }
        return -1;
    }

    private boolean isUnavailable(Ghe ghe) {
        return unavailableResolver != null && unavailableResolver.test(ghe);
    }

    private boolean isSelected(Ghe ghe) {
        return selectedResolver != null && selectedResolver.test(ghe);
    }

    @Override public String getToolTipText(MouseEvent e) {
        Ghe ghe = getSeatAtPoint(e.getPoint());
        if (ghe == null) return null;
        String state = isUnavailable(ghe) ? "Đã bán / giữ chỗ" : isSelected(ghe) ? "Đang chọn" : "Còn trống";
        Double price = priceResolver != null ? priceResolver.apply(ghe) : null;
        String priceText = price != null && price > 0 ? String.format("%,.0f đ", price) : "Chưa có giá";
        return "<html><div style='padding:6px 8px;'>"
                + "<b>" + ghe.getMaGhe() + "</b>"
                + "<br/>Ghế số " + ghe.getSoGhe()
                + "<br/>" + state
                + "<br/><font color='#5645D4'><b>Giá: " + priceText + "</b></font>"
                + "</div></html>";
    }
}
