package com.modules;

import com.dao.DAO_ChiTietDoanTau;
import com.dao.DAO_Ghe;
import com.entity.DoanTau;
import com.entity.Ghe;
import com.entity.ToaTau;
import com.enums.LoaiGhe;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Dialog chi tiết toa tàu.
 * Phần trên: sơ đồ chỗ ngồi (vẽ tay bằng Custom JPanel).
 * Phần dưới: danh sách đoàn tàu đang dùng toa này.
 */
public class ChiTietToaDialog extends JDialog {

    // --- DAOs ---
    private final DAO_Ghe              daoGhe    = new DAO_Ghe();
    private final DAO_ChiTietDoanTau   daoCTDT   = new DAO_ChiTietDoanTau();

    private Point dragStart;

    // --- Dữ liệu ---
    private final ToaTau        toa;
    private final List<Ghe>     gheList;
    private final List<DoanTau> doanTauList;

    // --- Design tokens ---
    private static final Color SURFACE        = new Color(0xF8, 0xFA, 0xFC);
    private static final Color CARD_BG        = Color.WHITE;
    private static final Color ON_SURFACE     = new Color(0x1A, 0x1D, 0x21);
    private static final Color ON_SURF_VAR    = new Color(0x5F, 0x67, 0x70);
    private static final Color OUTLINE        = new Color(0xDE, 0xE3, 0xE8);
    private static final Color PRIMARY        = new Color(0x00, 0x5D, 0x90);
    private static final Color PRIMARY_LIGHT  = new Color(0xE3, 0xF2, 0xFD);

    // Màu ghế theo loại
    private static final Color CLR_CUNG_FILL   = new Color(0xFF, 0xCC, 0x80);
    private static final Color CLR_CUNG_BORDER = new Color(0xE6, 0x91, 0x00);
    private static final Color CLR_MEM_FILL    = new Color(0x81, 0xD4, 0xFA);
    private static final Color CLR_MEM_BORDER  = new Color(0x02, 0x88, 0xD1);
    private static final Color CLR_GIUONG_FILL = new Color(0xA5, 0xD6, 0xA7);
    private static final Color CLR_GIUONG_BORDER = new Color(0x2E, 0x7D, 0x32);
    private static final Color CLR_KHOANG_LINE = new Color(0xBD, 0xBD, 0xBD);

    public ChiTietToaDialog(JFrame owner, ToaTau toa) {
        super(owner, "Chi tiết toa: " + toa.getMaToaTau(), true);
        this.toa        = toa;
        this.gheList    = daoGhe.findByToaTau(toa.getMaToaTau());
        this.doanTauList = daoCTDT.findDoanTauByToaTau(toa.getMaToaTau());

        setUndecorated(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUI();
        pack();
        setMinimumSize(new Dimension(820, 560));
        setLocationRelativeTo(owner);
    }

    // =========================================================================
    //  BUILD UI
    // =========================================================================

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(SURFACE);

        // ---- Header ----
        root.add(buildHeader(), BorderLayout.NORTH);

        // ---- Split: top = sơ đồ, bottom = đoàn tàu ----
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setBorder(BorderFactory.createEmptyBorder());
        split.setBackground(SURFACE);
        split.setResizeWeight(0.6);
        split.setDividerSize(6);

        split.setTopComponent(buildDiagramPanel());
        split.setBottomComponent(buildTrainListPanel());

        root.add(split, BorderLayout.CENTER);

        // ---- Footer button ----
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 10));
        footer.setBackground(CARD_BG);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, OUTLINE));

        JButton btnClose = new JButton("Đóng");
        btnClose.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnClose.setBackground(PRIMARY);
        btnClose.setForeground(Color.WHITE);
        btnClose.setFocusPainted(false);
        btnClose.setBorder(new EmptyBorder(8, 24, 8, 24));
        btnClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose());
        footer.add(btnClose);

        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, OUTLINE),
                new EmptyBorder(16, 24, 16, 24)));

        // Window drag via header
        p.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                dragStart = SwingUtilities.convertPoint(p, e.getPoint(), ChiTietToaDialog.this);
            }
        });
        p.addMouseMotionListener(new MouseAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                if (dragStart == null) return;
                Point cur = SwingUtilities.convertPoint(p, e.getPoint(), ChiTietToaDialog.this);
                Point loc = getLocation();
                setLocation(loc.x + cur.x - dragStart.x, loc.y + cur.y - dragStart.y);
                dragStart = cur;
            }
        });

        String loaiText = toa.getLoaiGhe() != null ? toa.getLoaiGhe().toString() : "Không rõ";
        int soGhe = gheList.size();

        JLabel title = new JLabel(toa.getMaToaTau() + "  —  " + loaiText);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(ON_SURFACE);

        JLabel sub = new JLabel(soGhe + " chỗ  •  " + doanTauList.size() + " đoàn tàu đang sử dụng");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(ON_SURF_VAR);

        // Badge loại ghế
        JLabel badge = new JLabel(loaiText);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        badge.setForeground(Color.WHITE);
        badge.setOpaque(true);
        badge.setBackground(badgeColor());
        badge.setBorder(new EmptyBorder(4, 12, 4, 12));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBackground(CARD_BG);
        left.add(title);
        left.add(Box.createVerticalStrut(4));
        left.add(sub);

        p.add(left,  BorderLayout.WEST);
        p.add(badge, BorderLayout.EAST);
        return p;
    }

    // =========================================================================
    //  SƠ ĐỒ TOA
    // =========================================================================

    private JPanel buildDiagramPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 10));
        wrapper.setBackground(SURFACE);
        wrapper.setBorder(new EmptyBorder(16, 20, 12, 20));

        // Section title
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setBackground(SURFACE);
        JLabel lbl = new JLabel("Sơ đồ toa tàu");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(ON_SURFACE);
        titleRow.add(lbl, BorderLayout.WEST);
        titleRow.add(buildDiagramLegend(), BorderLayout.EAST);

        // Custom seat canvas
        SeatDiagramCanvas canvas = new SeatDiagramCanvas();
        canvas.setBackground(CARD_BG);

        JScrollPane scroll = new JScrollPane(canvas);
        scroll.setBorder(BorderFactory.createLineBorder(OUTLINE, 1));
        scroll.getViewport().setBackground(CARD_BG);

        wrapper.add(titleRow, BorderLayout.NORTH);
        wrapper.add(scroll,   BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildDiagramLegend() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        p.setBackground(SURFACE);

        LoaiGhe lg = toa.getLoaiGhe();
        if (lg == LoaiGhe.GIUONG_NAM) {
            p.add(legendItem(CLR_GIUONG_FILL, CLR_GIUONG_BORDER, "Giường nằm", true));
        } else if (lg == LoaiGhe.GHE_MEM) {
            p.add(legendItem(CLR_MEM_FILL, CLR_MEM_BORDER, "Ghế mềm", false));
        } else {
            p.add(legendItem(CLR_CUNG_FILL, CLR_CUNG_BORDER, "Ghế cứng", false));
        }
        p.add(khoangLegend());
        return p;
    }

    private JPanel legendItem(Color fill, Color border, String text, boolean wide) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        p.setBackground(SURFACE);
        JPanel box = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 5, 5);
                g2.setColor(border);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 5, 5);
                g2.dispose();
            }
        };
        box.setOpaque(false);
        box.setPreferredSize(wide ? new Dimension(26, 14) : new Dimension(14, 14));
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(ON_SURF_VAR);
        p.add(box); p.add(lbl);
        return p;
    }

    private JPanel khoangLegend() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        p.setBackground(SURFACE);
        JPanel line = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(CLR_KHOANG_LINE);
                ((Graphics2D)g).setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_MITER, 1f, new float[]{4, 3}, 0));
                g.drawLine(getWidth()/2, 0, getWidth()/2, getHeight());
            }
        };
        line.setOpaque(false);
        line.setPreferredSize(new Dimension(10, 14));
        JLabel lbl = new JLabel("Ranh khoang");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(ON_SURF_VAR);
        p.add(line); p.add(lbl);
        return p;
    }

    // =========================================================================
    //  CANVAS VẼ SƠ ĐỒ GHẾ
    // =========================================================================

    class SeatDiagramCanvas extends JPanel {

        // Layout constants
        private static final int PADDING    = 28;
        private static final int SEAT_GAP   = 6;
        private static final int KHOANG_GAP = 18; // khoảng cách thêm giữa các khoang
        private static final int LABEL_H    = 22; // chiều cao dòng nhãn khoang

        // Giường: 3 hàng x 10 cột — mỗi ghế hình chữ nhật nằm ngang (wide > tall)
        private static final int BED_W      = 52;
        private static final int BED_H      = 28;
        // Ghế: 4 hàng x 12 cột — hình vuông
        private static final int SEAT_W     = 42;
        private static final int SEAT_H     = 42;

        SeatDiagramCanvas() {
            setOpaque(true);
            setBackground(CARD_BG);
            addMouseMotionListener(new MouseAdapter() {
                @Override public void mouseMoved(MouseEvent e) { repaint(); }
            });
        }

        @Override
        public Dimension getPreferredSize() {
            LoaiGhe lg = toa.getLoaiGhe();
            if (lg == LoaiGhe.GIUONG_NAM) {
                // 3 rows, 10 cols, 5 khoang (every 2 cols)
                int cols = 10, khoang = 5, colsPerKhoang = 2;
                int totalW = PADDING * 2
                        + cols * BED_W
                        + (cols - 1) * SEAT_GAP
                        + (khoang - 1) * KHOANG_GAP;
                int totalH = PADDING * 2 + LABEL_H + 3 * BED_H + 2 * SEAT_GAP;
                return new Dimension(totalW, totalH);
            } else {
                // 4 rows, 12 cols, 2 khoang (every 6 cols)
                int cols = 12, khoang = 2;
                int totalW = PADDING * 2
                        + cols * SEAT_W
                        + (cols - 1) * SEAT_GAP
                        + (khoang - 1) * KHOANG_GAP;
                int totalH = PADDING * 2 + LABEL_H + 4 * SEAT_H + 3 * SEAT_GAP;
                return new Dimension(totalW, totalH);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            LoaiGhe lg = toa.getLoaiGhe();
            if (lg == LoaiGhe.GIUONG_NAM) {
                paintGiuong(g2);
            } else {
                paintGhe(g2, lg);
            }
            g2.dispose();
        }

        // ---- Vẽ toa giường: 3x10, khoang mỗi 2 cột ----
        private void paintGiuong(Graphics2D g2) {
            int rows = 3, cols = 10, colsPerKhoang = 2, khoang = cols / colsPerKhoang; // 5 khoang

            Point mouse = getMousePosition();
            Font fontNum = new Font("Segoe UI", Font.BOLD, 9);
            Font fontKhoang = new Font("Segoe UI", Font.BOLD, 11);

            int x0 = PADDING;
            int y0 = PADDING + LABEL_H;

            for (int kh = 0; kh < khoang; kh++) {
                // X bắt đầu khoang này (tính thêm khoảng cách khoang trước)
                int khoangX = x0
                        + kh * colsPerKhoang * (BED_W + SEAT_GAP)
                        + kh * KHOANG_GAP;

                // Nhãn "Khoang N"
                g2.setFont(fontKhoang);
                g2.setColor(ON_SURF_VAR);
                String kLabel = "Khoang " + (kh + 1);
                FontMetrics fm = g2.getFontMetrics();
                int khoangW = colsPerKhoang * BED_W + (colsPerKhoang - 1) * SEAT_GAP;
                g2.drawString(kLabel, khoangX + (khoangW - fm.stringWidth(kLabel)) / 2,
                        PADDING + LABEL_H - 5);

                // Vẽ ghế trong khoang
                for (int col = 0; col < colsPerKhoang; col++) {
                    int globalCol = kh * colsPerKhoang + col;
                    int cx = khoangX + col * (BED_W + SEAT_GAP);

                    for (int row = 0; row < rows; row++) {
                        int gy = y0 + row * (BED_H + SEAT_GAP);
                        int seatNo = row * cols + globalCol + 1;
                        Ghe ghe = seatNo <= gheList.size() ? gheList.get(seatNo - 1) : null;

                        boolean hover = mouse != null &&
                                new Rectangle(cx, gy, BED_W, BED_H).contains(mouse);

                        drawBed(g2, cx, gy, BED_W, BED_H, seatNo, ghe, hover);
                    }
                }

                // Vẽ đường kẻ dọc phân khoang (sau mỗi khoang trừ khoang cuối)
                if (kh < khoang - 1) {
                    int lineX = khoangX + colsPerKhoang * BED_W
                            + (colsPerKhoang - 1) * SEAT_GAP + KHOANG_GAP / 2;
                    drawKhoangDivider(g2, lineX,
                            y0 - 4,
                            y0 + rows * BED_H + (rows - 1) * SEAT_GAP + 4);
                }
            }
        }

        // ---- Vẽ toa mềm/cứng: 4x12, khoang mỗi 6 cột ----
        private void paintGhe(Graphics2D g2, LoaiGhe lg) {
            int rows = 4, cols = 12, colsPerKhoang = 6, khoang = cols / colsPerKhoang; // 2 khoang

            Point mouse = getMousePosition();
            Font fontNum = new Font("Segoe UI", Font.BOLD, 10);
            Font fontKhoang = new Font("Segoe UI", Font.BOLD, 11);

            int x0 = PADDING;
            int y0 = PADDING + LABEL_H;

            for (int kh = 0; kh < khoang; kh++) {
                int khoangX = x0
                        + kh * colsPerKhoang * (SEAT_W + SEAT_GAP)
                        + kh * KHOANG_GAP;

                // Nhãn khoang
                g2.setFont(fontKhoang);
                g2.setColor(ON_SURF_VAR);
                String kLabel = "Khoang " + (kh + 1);
                FontMetrics fm = g2.getFontMetrics();
                int khoangW = colsPerKhoang * SEAT_W + (colsPerKhoang - 1) * SEAT_GAP;
                g2.drawString(kLabel, khoangX + (khoangW - fm.stringWidth(kLabel)) / 2,
                        PADDING + LABEL_H - 5);

                for (int col = 0; col < colsPerKhoang; col++) {
                    int globalCol = kh * colsPerKhoang + col;
                    int cx = khoangX + col * (SEAT_W + SEAT_GAP);

                    for (int row = 0; row < rows; row++) {
                        int gy = y0 + row * (SEAT_H + SEAT_GAP);
                        int seatNo = row * cols + globalCol + 1;
                        Ghe ghe = seatNo <= gheList.size() ? gheList.get(seatNo - 1) : null;

                        boolean hover = mouse != null &&
                                new Rectangle(cx, gy, SEAT_W, SEAT_H).contains(mouse);

                        drawSeat(g2, cx, gy, SEAT_W, SEAT_H, seatNo, ghe, hover, lg);
                    }
                }

                if (kh < khoang - 1) {
                    int lineX = khoangX + colsPerKhoang * SEAT_W
                            + (colsPerKhoang - 1) * SEAT_GAP + KHOANG_GAP / 2;
                    drawKhoangDivider(g2, lineX,
                            y0 - 4,
                            y0 + rows * SEAT_H + (rows - 1) * SEAT_GAP + 4);
                }
            }
        }

        private void drawBed(Graphics2D g2, int x, int y, int w, int h,
                int seatNo, Ghe ghe, boolean hover) {
            Color fill   = hover ? CLR_GIUONG_FILL.darker() : CLR_GIUONG_FILL;
            Color border = CLR_GIUONG_BORDER;
            drawSeatShape(g2, x, y, w, h, fill, border, seatNo, ghe, hover, 5);
        }

        private void drawSeat(Graphics2D g2, int x, int y, int w, int h,
                int seatNo, Ghe ghe, boolean hover, LoaiGhe lg) {
            Color fill   = lg == LoaiGhe.GHE_MEM
                    ? (hover ? CLR_MEM_FILL.darker()  : CLR_MEM_FILL)
                    : (hover ? CLR_CUNG_FILL.darker() : CLR_CUNG_FILL);
            Color border = lg == LoaiGhe.GHE_MEM ? CLR_MEM_BORDER : CLR_CUNG_BORDER;
            drawSeatShape(g2, x, y, w, h, fill, border, seatNo, ghe, hover, 6);
        }

        private void drawSeatShape(Graphics2D g2, int x, int y, int w, int h,
                Color fill, Color border,
                int seatNo, Ghe ghe, boolean hover, int arc) {
            // Shadow
            g2.setColor(new Color(0, 0, 0, 18));
            g2.fillRoundRect(x + 2, y + 2, w, h, arc, arc);

            // Fill
            g2.setColor(fill);
            g2.fillRoundRect(x, y, w, h, arc, arc);

            // Border
            g2.setColor(border);
            g2.setStroke(new BasicStroke(hover ? 2f : 1.5f));
            g2.drawRoundRect(x, y, w, h, arc, arc);

            // Số thứ tự
            g2.setFont(new Font("Segoe UI", Font.BOLD, w > 40 ? 10 : 9));
            g2.setColor(new Color(0x33, 0x33, 0x33));
            String numStr = String.valueOf(seatNo);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(numStr,
                    x + (w - fm.stringWidth(numStr)) / 2,
                    y + (h + fm.getAscent() - fm.getDescent()) / 2);

            // Tooltip được xử lý qua setToolTipText nhưng do là canvas thì ta ghi mã ghế nhỏ
            if (ghe != null && hover) {
                String tip = ghe.getMaGhe();
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                g2.setColor(new Color(0x00, 0x5D, 0x90));
                FontMetrics fm2 = g2.getFontMetrics();
                // Hiển thị mã ghế nhỏ bên dưới số (chỉ khi hover)
                if (w >= 36) {
                    g2.drawString(tip,
                            x + (w - fm2.stringWidth(tip)) / 2,
                            y + h - 3);
                }
            }
        }

        private void drawKhoangDivider(Graphics2D g2, int x, int y1, int y2) {
            g2.setColor(CLR_KHOANG_LINE);
            float[] dash = {5f, 4f};
            g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER, 1f, dash, 0f));
            g2.drawLine(x, y1, x, y2);
            g2.setStroke(new BasicStroke(1));
        }

        // Tooltip theo chuẩn Swing — khi di chuột qua ghế
        @Override
        public String getToolTipText(MouseEvent e) {
            LoaiGhe lg = toa.getLoaiGhe();
            if (lg == null) return null;

            int seatNo = getSeatAtPoint(e.getPoint(), lg);
            if (seatNo < 1 || seatNo > gheList.size()) return null;
            Ghe ghe = gheList.get(seatNo - 1);
            return "<html><b>" + ghe.getMaGhe() + "</b><br/>Ghế số " + seatNo + "</html>";
        }

        private int getSeatAtPoint(Point p, LoaiGhe lg) {
            if (lg == LoaiGhe.GIUONG_NAM) {
                return hitTestGrid(p, 3, 10, 2, BED_W, BED_H);
            } else {
                return hitTestGrid(p, 4, 12, 6, SEAT_W, SEAT_H);
            }
        }

        /**
         * Xác định số ghế tại điểm p.
         * rows, cols: kích thước lưới; colsPerKhoang: số cột mỗi khoang.
         * w, h: kích thước mỗi ô.
         */
        private int hitTestGrid(Point p, int rows, int cols, int colsPerKhoang, int w, int h) {
            int khoang = cols / colsPerKhoang;
            int x0 = PADDING;
            int y0 = PADDING + LABEL_H;

            for (int kh = 0; kh < khoang; kh++) {
                int khoangX = x0
                        + kh * colsPerKhoang * (w + SEAT_GAP)
                        + kh * KHOANG_GAP;

                for (int col = 0; col < colsPerKhoang; col++) {
                    int globalCol = kh * colsPerKhoang + col;
                    int cx = khoangX + col * (w + SEAT_GAP);

                    for (int row = 0; row < rows; row++) {
                        int gy = y0 + row * (h + SEAT_GAP);
                        if (new Rectangle(cx, gy, w, h).contains(p)) {
                            return row * cols + globalCol + 1;
                        }
                    }
                }
            }
            return -1;
        }
    }

    // =========================================================================
    //  DANH SÁCH ĐOÀN TÀU
    // =========================================================================

    private JPanel buildTrainListPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 10));
        wrapper.setBackground(SURFACE);
        wrapper.setBorder(new EmptyBorder(10, 20, 16, 20));

        JLabel lbl = new JLabel("Đoàn tàu đang sử dụng toa này");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(ON_SURFACE);

        JPanel listPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        listPanel.setBackground(CARD_BG);
        listPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

        if (doanTauList.isEmpty()) {
            JLabel empty = new JLabel("Chưa có đoàn tàu nào dùng toa này");
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            empty.setForeground(new Color(0xAA, 0xAA, 0xAA));
            listPanel.add(empty);
        } else {
            for (DoanTau dt : doanTauList) {
                listPanel.add(buildTrainCard(dt));
            }
        }

        JScrollPane scroll = new JScrollPane(listPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setBorder(BorderFactory.createLineBorder(OUTLINE, 1));
        scroll.getViewport().setBackground(CARD_BG);

        wrapper.add(lbl,    BorderLayout.NORTH);
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildTrainCard(DoanTau dt) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Shadow
                g2.setColor(new Color(0, 0, 0, 20));
                g2.fillRoundRect(3, 3, getWidth() - 3, getHeight() - 3, 12, 12);
                // Fill
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth() - 3, getHeight() - 3, 12, 12);
                // Border
                g2.setColor(OUTLINE);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 4, getHeight() - 4, 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(90, 100));
        card.setBorder(new EmptyBorder(10, 8, 8, 8));

        // Icon tàu
        JLabel iconLbl = new JLabel();
        iconLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        try {
            ImageIcon raw = new ImageIcon(
                    getClass().getResource("/icons/bieuTuongTau.png"));
            Image scaled = raw.getImage().getScaledInstance(38, 38, Image.SCALE_SMOOTH);
            iconLbl.setIcon(new ImageIcon(scaled));
        } catch (Exception ex) {
            iconLbl.setText("🚂");
            iconLbl.setFont(new Font("Segoe UI", Font.PLAIN, 26));
        }

        // Mã đoàn tàu
        JLabel idLbl = new JLabel(dt.getMaDoanTau());
        idLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        idLbl.setForeground(PRIMARY);
        idLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Tên (rút gọn)
        String ten = dt.getTenDoanTau();
        if (ten != null && ten.length() > 10) ten = ten.substring(0, 9) + "…";
        JLabel nameLbl = new JLabel(ten != null ? ten : "");
        nameLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        nameLbl.setForeground(ON_SURF_VAR);
        nameLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(iconLbl);
        card.add(Box.createVerticalStrut(5));
        card.add(idLbl);
        card.add(Box.createVerticalStrut(2));
        card.add(nameLbl);

        // Hover effect
        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(PRIMARY, 1),
                        new EmptyBorder(10, 8, 8, 8)));
                card.repaint();
            }
            @Override public void mouseExited(MouseEvent e) {
                card.setBorder(new EmptyBorder(10, 8, 8, 8));
                card.repaint();
            }
        });

        return card;
    }

    // =========================================================================
    //  Helpers
    // =========================================================================

    private Color badgeColor() {
        if (toa.getLoaiGhe() == null) return Color.GRAY;
        return switch (toa.getLoaiGhe()) {
            case GHE_CUNG   -> new Color(0xE6, 0x91, 0x00);
            case GHE_MEM    -> new Color(0x02, 0x88, 0xD1);
            case GIUONG_NAM -> new Color(0x2E, 0x7D, 0x32);
        };
    }
}
