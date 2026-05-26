package com.modules;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * DatePickerField — text field + popup calendar với nút ngày to, giao diện hiện đại.
 *
 * <pre>
 *   DatePickerField dpf = new DatePickerField();
 *   DatePickerField dpf = new DatePickerField(LocalDate.now());         // initial date
 *   DatePickerField dpf = new DatePickerField(LocalDate.now(), null);   // with minDate
 *
 *   dpf.getValue()               // LocalDate or null
 *   dpf.setValue(date)           // set programmatically
 *   dpf.setMinDate(LocalDate.now()) // disable past dates
 *   dpf.addPropertyChangeListener("value", e -> ...)  // fires on change
 * </pre>
 */
public class DatePickerField extends JPanel {

    // ── Design tokens ────────────────────────────────────────────────────────
    private static final Color PRIMARY        = NotionTheme.ACCENT;
    private static final Color PRIMARY_LIGHT  = NotionTheme.ACCENT_SOFT;
    private static final Color SURFACE        = AppColors.BACKGROUND;
    private static final Color CARD_BG        = AppColors.SURFACE;
    private static final Color ON_SURFACE     = AppColors.TEXT_PRIMARY;
    private static final Color ON_SURF_VAR    = AppColors.TEXT_SECONDARY;
    private static final Color OUTLINE        = AppColors.BORDER;
    private static final Color CELL_TODAY_FG  = PRIMARY;
    private static final Color CELL_SEL_BG    = PRIMARY;
    private static final Color CELL_HOVER_BG  = PRIMARY_LIGHT;
    private static final Color CELL_OTHERMON  = AppColors.BORDER;
    private static final Color CELL_DISABLED  = AppColors.BORDER;

    private static final DateTimeFormatter DISPLAY_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String[] DAY_NAMES = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};

    // ── State ─────────────────────────────────────────────────────────────────
    private LocalDate value;
    private LocalDate minDate;

    // ── UI ────────────────────────────────────────────────────────────────────
    private final JTextField displayField;
    private final JButton    calBtn;
    private JDialog          popup;

    // ── Constructors ──────────────────────────────────────────────────────────

    public DatePickerField() {
        this(null, null);
    }

    public DatePickerField(LocalDate initial) {
        this(initial, null);
    }

    public DatePickerField(LocalDate initial, LocalDate minDate) {
        this.value   = initial;
        this.minDate = minDate;

        setLayout(new BorderLayout(0, 0));
        setOpaque(false);

        // ── Text field ────────────────────────────────────────────────────────
        displayField = new JTextField();
        displayField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        displayField.setEditable(false);
        displayField.setBackground(CARD_BG);
        displayField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(OUTLINE, 1),
            new EmptyBorder(0, 10, 0, 6)
        ));
        displayField.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        displayField.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { togglePopup(); }
        });

        // ── Calendar button ───────────────────────────────────────────────────
        calBtn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isRollover() ? PRIMARY_LIGHT
                    : getModel().isPressed()       ? NotionTheme.ACCENT_SOFT
                    : SURFACE;
                g2.setColor(bg);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(OUTLINE);
                g2.drawLine(0, 0, 0, getHeight());
                drawCalendarIcon(g2, getWidth(), getHeight(),
                    getModel().isRollover() ? PRIMARY : ON_SURF_VAR);
                g2.dispose();
            }
        };
        calBtn.setPreferredSize(new Dimension(40, 40));
        calBtn.setContentAreaFilled(false);
        calBtn.setBorderPainted(false);
        calBtn.setFocusPainted(false);
        calBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        calBtn.setToolTipText("Chọn ngày");
        calBtn.addActionListener(e -> togglePopup());

        add(displayField, BorderLayout.CENTER);
        add(calBtn,       BorderLayout.EAST);

        refreshDisplay();
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public LocalDate getValue() { return value; }

    public void setValue(LocalDate newVal) {
        LocalDate old = this.value;
        this.value = newVal;
        refreshDisplay();
        firePropertyChange("value", old, newVal);
    }

    public void setMinDate(LocalDate min) {
        this.minDate = min;
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private void refreshDisplay() {
        if (value == null) {
            displayField.setText("");
            displayField.setForeground(ON_SURF_VAR);
        } else {
            displayField.setText(value.format(DISPLAY_FMT));
            displayField.setForeground(ON_SURFACE);
        }
    }

    private void togglePopup() {
        if (popup != null && popup.isVisible()) {
            popup.dispose();
            popup = null;
        } else {
            showPopup();
        }
    }

    private void showPopup() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        popup = new JDialog(owner, Dialog.ModalityType.MODELESS);
        popup.setUndecorated(true);
        popup.getRootPane().setBorder(
            BorderFactory.createLineBorder(OUTLINE, 1));
        popup.addWindowFocusListener(new WindowFocusListener() {
            @Override public void windowGainedFocus(WindowEvent e) {}
            @Override public void windowLostFocus(WindowEvent e)   { if (popup != null) { popup.dispose(); popup = null; } }
        });

        YearMonth start = value != null ? YearMonth.from(value)
            : (minDate != null ? YearMonth.from(minDate) : YearMonth.now());

        CalendarPanel calPanel = new CalendarPanel(start, value, minDate, selected -> {
            LocalDate old = value;
            value = selected;
            refreshDisplay();
            firePropertyChange("value", old, selected);
            popup.dispose();
            popup = null;
        });

        popup.setContentPane(calPanel);
        popup.pack();

        // Position below the field
        try {
            Point loc = getLocationOnScreen();
            int px = loc.x;
            int py = loc.y + getHeight() + 2;

            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            if (py + popup.getHeight() > screen.height - 40)
                py = loc.y - popup.getHeight() - 2;
            if (px + popup.getWidth() > screen.width)
                px = screen.width - popup.getWidth();

            popup.setLocation(px, py);
        } catch (Exception ignored) {}

        popup.setVisible(true);
    }

    /** Draws a small calendar icon centered in the button area. */
    private void drawCalendarIcon(Graphics2D g2, int w, int h, Color color) {
        int cx = w / 2, cy = h / 2;
        int bw = 16, bh = 14;
        int x  = cx - bw / 2, y = cy - bh / 2 + 1;

        g2.setColor(color);
        g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Outer rectangle
        g2.drawRoundRect(x, y, bw, bh, 3, 3);
        // Header divider
        g2.drawLine(x, y + 4, x + bw, y + 4);
        // Grid dots 3×2
        int dotR = 1;
        int[] colX = {x + 3, x + bw / 2, x + bw - 3};
        int[] rowY = {y + 7, y + 11};
        g2.setStroke(new BasicStroke(1f));
        for (int rx : colX)
            for (int ry : rowY)
                g2.fillOval(rx - dotR, ry - dotR, dotR * 2, dotR * 2);
        // Tabs on top
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(x + 4, y - 2, x + 4, y + 2);
        g2.drawLine(x + bw - 4, y - 2, x + bw - 4, y + 2);
    }

    // =========================================================================
    //  CALENDAR PANEL
    // =========================================================================

    private static class CalendarPanel extends JPanel {

        interface DaySelectedListener {
            void onSelected(LocalDate date);
        }

        private YearMonth currentMonth;
        private final LocalDate  selectedDate;
        private final LocalDate  minDate;
        private final DaySelectedListener listener;

        private JLabel lblMonthYear;
        private GridPanel grid;

        CalendarPanel(YearMonth initial, LocalDate selected, LocalDate min,
                      DaySelectedListener listener) {
            this.currentMonth = initial;
            this.selectedDate = selected;
            this.minDate      = min;
            this.listener     = listener;

            setLayout(new BorderLayout(0, 4));
            setBackground(CARD_BG);
            setBorder(new EmptyBorder(10, 10, 10, 10));

            add(buildHeader(),   BorderLayout.NORTH);
            add(buildDayNames(), BorderLayout.CENTER);
            grid = new GridPanel();
            add(grid,            BorderLayout.SOUTH);
        }

        private JPanel buildHeader() {
            JPanel hdr = new JPanel(new BorderLayout(0, 0));
            hdr.setBackground(CARD_BG);
            hdr.setBorder(new EmptyBorder(0, 0, 6, 0));

            JButton prev = navBtn("‹");
            prev.addActionListener(e -> { currentMonth = currentMonth.minusMonths(1); refreshGrid(); });

            JButton next = navBtn("›");
            next.addActionListener(e -> { currentMonth = currentMonth.plusMonths(1); refreshGrid(); });

            lblMonthYear = new JLabel("", SwingConstants.CENTER);
            lblMonthYear.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblMonthYear.setForeground(ON_SURFACE);
            updateMonthLabel();

            hdr.add(prev,         BorderLayout.WEST);
            hdr.add(lblMonthYear, BorderLayout.CENTER);
            hdr.add(next,         BorderLayout.EAST);
            return hdr;
        }

        private JButton navBtn(String text) {
            JButton btn = new JButton(text) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (getModel().isRollover()) {
                        g2.setColor(PRIMARY_LIGHT);
                        g2.fillOval(2, 2, getWidth()-4, getHeight()-4);
                    }
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            btn.setFont(new Font("Segoe UI", Font.BOLD, 18));
            btn.setForeground(ON_SURF_VAR);
            btn.setPreferredSize(new Dimension(32, 32));
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return btn;
        }

        private JPanel buildDayNames() {
            JPanel row = new JPanel(new GridLayout(1, 7, 2, 0));
            row.setBackground(CARD_BG);
            row.setBorder(new EmptyBorder(0, 0, 4, 0));
            for (int i = 0; i < 7; i++) {
                JLabel lbl = new JLabel(DAY_NAMES[i], SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
                lbl.setForeground(i == 6 ? AppColors.ERROR_DARK : ON_SURF_VAR);
                lbl.setPreferredSize(new Dimension(36, 18));
                row.add(lbl);
            }
            return row;
        }

        private void updateMonthLabel() {
            String monthName = currentMonth.getMonth()
                .getDisplayName(TextStyle.FULL, new Locale("vi", "VN"));
            // Capitalize first letter
            monthName = monthName.substring(0, 1).toUpperCase() + monthName.substring(1);
            lblMonthYear.setText(monthName + " " + currentMonth.getYear());
        }

        private void refreshGrid() {
            updateMonthLabel();
            remove(grid);
            grid = new GridPanel();
            add(grid, BorderLayout.SOUTH);
            revalidate();
            repaint();
        }

        // ── Grid panel ────────────────────────────────────────────────────────

        class GridPanel extends JPanel {

            private final List<DayCell> cells = new ArrayList<>();

            GridPanel() {
                setLayout(new GridLayout(6, 7, 2, 2));
                setBackground(CARD_BG);

                LocalDate today = LocalDate.now();
                LocalDate first = currentMonth.atDay(1);
                // Monday=0 offset
                int offset = (first.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue() + 7) % 7;

                // Fill 42 cells
                LocalDate cursor = first.minusDays(offset);
                for (int i = 0; i < 42; i++) {
                    final LocalDate day  = cursor;
                    boolean thisMonth    = day.getMonth() == currentMonth.getMonth();
                    boolean isToday      = day.equals(today);
                    boolean isSelected   = day.equals(selectedDate);
                    boolean disabled     = minDate != null && day.isBefore(minDate);

                    DayCell cell = new DayCell(day, thisMonth, isToday, isSelected, disabled);
                    if (!disabled) {
                        cell.addMouseListener(new MouseAdapter() {
                            @Override public void mouseClicked(MouseEvent e) {
                                listener.onSelected(day);
                            }
                        });
                    }
                    cells.add(cell);
                    add(cell);
                    cursor = cursor.plusDays(1);
                }
            }
        }

        // ── Single day cell ───────────────────────────────────────────────────

        static class DayCell extends JPanel {

            private final LocalDate day;
            private final boolean   thisMonth;
            private final boolean   isToday;
            private final boolean   isSelected;
            private final boolean   disabled;
            private boolean         hovered = false;

            DayCell(LocalDate day, boolean thisMonth, boolean isToday,
                    boolean isSelected, boolean disabled) {
                this.day        = day;
                this.thisMonth  = thisMonth;
                this.isToday    = isToday;
                this.isSelected = isSelected;
                this.disabled   = disabled;

                setOpaque(false);
                setPreferredSize(new Dimension(36, 36));
                if (!disabled) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    addMouseListener(new MouseAdapter() {
                        @Override public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                        @Override public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
                    });
                }
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int w = getWidth(), h = getHeight();
                int diam = Math.min(w, h) - 4;
                int cx = (w - diam) / 2, cy = (h - diam) / 2;

                String text = String.valueOf(day.getDayOfMonth());
                Font  font;
                Color textColor;

                if (isSelected) {
                    // Handoff: ngày đang chọn dùng tím đậm để đồng bộ popup selection toàn app.
                    // Rủi ro: chữ ngày chọn luôn trắng, nên nền chọn không được đổi sang màu quá nhạt.
                    g2.setColor(CELL_SEL_BG);
                    g2.fillOval(cx, cy, diam, diam);
                    font      = new Font("Segoe UI", Font.BOLD, 12);
                    textColor = AppColors.SURFACE;
                } else if (hovered) {
                    g2.setColor(CELL_HOVER_BG);
                    g2.fillOval(cx, cy, diam, diam);
                    font      = new Font("Segoe UI", Font.PLAIN, 12);
                    textColor = PRIMARY;
                } else if (isToday) {
                    g2.setColor(PRIMARY);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawOval(cx, cy, diam, diam);
                    font      = new Font("Segoe UI", Font.BOLD, 12);
                    textColor = CELL_TODAY_FG;
                } else {
                    font = new Font("Segoe UI", Font.PLAIN, 12);
                    if (disabled)          textColor = CELL_DISABLED;
                    else if (!thisMonth)   textColor = CELL_OTHERMON;
                    else if (day.getDayOfWeek() == DayOfWeek.SUNDAY)
                        textColor = AppColors.ERROR_DARK;
                    else
                        textColor = ON_SURFACE;
                }

                g2.setFont(font);
                FontMetrics fm = g2.getFontMetrics();
                int tx = (w - fm.stringWidth(text)) / 2;
                int ty = (h + fm.getAscent() - fm.getDescent()) / 2;
                g2.setColor(textColor);
                g2.drawString(text, tx, ty);

                g2.dispose();
            }
        }
    }
}
