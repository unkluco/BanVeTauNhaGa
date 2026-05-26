package com.modules;

import com.entity.HoaDon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;

public class BanVeStep8Module extends JPanel implements AppModule {

    private Consumer<Object> callback;
    private final HoaDon hoaDon;

    private static final Color PRIMARY       = NotionTheme.ACCENT;
    private static final Color SURFACE       = NotionTheme.PAGE;
    private static final Color CARD_BG       = AppColors.SURFACE;
    private static final Color ON_SURF_VAR   = NotionTheme.TEXT_MUTED;
    private static final Color OUTLINE       = NotionTheme.BORDER;
    private static final Color SUCCESS_DARK  = AppColors.SUCCESS_DARK;

    private JButton btnSubmit, btnCancel;
    private JPanel  btnPanel;

    public BanVeStep8Module() {
        this(null);
    }

    public BanVeStep8Module(HoaDon hoaDon) {
        this.hoaDon = hoaDon;
        setLayout(new BorderLayout());
        setBackground(SURFACE);
        buildUI();
    }

    private void buildUI() {
        JPanel center = new JPanel(new BorderLayout(0, 14));
        center.setBackground(SURFACE);
        center.setBorder(new EmptyBorder(18, 64, 18, 64));
        center.add(buildSuccessStrip(), BorderLayout.NORTH);
        center.add(buildInvoiceArea(), BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        btnSubmit = new JButton("Đặt vé mới");
        styleBtn(btnSubmit, true);
        btnSubmit.addActionListener(e -> { if (callback != null) callback.accept("DONE"); });

        btnCancel = new JButton("Về menu chính");
        styleBtn(btnCancel, false);
        btnCancel.addActionListener(e -> { if (callback != null) callback.accept("DONE"); });

        JButton btnPrint = new JButton("In vé");
        styleBtn(btnPrint, false);
        btnPrint.addActionListener(e -> exportInvoicePdf());

        btnPanel = BookingStepUi.createFooter();
        btnPanel.add(btnCancel);
        btnPanel.add(btnPrint);
        btnPanel.add(btnSubmit);
        btnPanel.setVisible(false);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private JPanel buildSuccessStrip() {
        JPanel card = new JPanel(new BorderLayout(16, 0));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.SUCCESS_LIGHT, 1),
                new EmptyBorder(14, 20, 14, 20)));
        card.setPreferredSize(new Dimension(200, 88));

        JPanel iconPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int s = Math.min(getWidth(), getHeight()) - 4;
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                int r = s / 2;
                g2.setColor(SUCCESS_DARK);
                g2.fillOval(cx - r, cy - r, r * 2, r * 2);
                g2.setColor(AppColors.SURFACE);
                g2.setStroke(new BasicStroke(3.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int[] xPts = { cx - r / 3, cx, cx + r / 2 };
                int[] yPts = { cy, cy + r / 3, cy - r / 3 };
                g2.drawPolyline(xPts, yPts, 3);
                g2.dispose();
            }
        };
        iconPanel.setOpaque(false);
        iconPanel.setPreferredSize(new Dimension(54, 54));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel titleLbl = new JLabel("Đặt vé thành công!");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLbl.setForeground(SUCCESS_DARK);
        JLabel subLbl = new JLabel(hoaDon == null
                ? "Vé đã được lưu vào hệ thống."
                : "Hóa đơn " + hoaDon.getMaHoaDon() + " đã được tạo và lưu vào hệ thống.");
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subLbl.setForeground(ON_SURF_VAR);
        text.add(titleLbl);
        text.add(Box.createVerticalStrut(4));
        text.add(subLbl);

        card.add(iconPanel, BorderLayout.WEST);
        card.add(text, BorderLayout.CENTER);
        return card;
    }

    private JComponent buildInvoiceArea() {
        if (hoaDon == null) {
            JPanel empty = new JPanel(new GridBagLayout());
            empty.setBackground(CARD_BG);
            empty.setBorder(BorderFactory.createLineBorder(OUTLINE, 1));
            JLabel lbl = new JLabel("Không tìm thấy dữ liệu hóa đơn để hiển thị.");
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            lbl.setForeground(ON_SURF_VAR);
            empty.add(lbl);
            return empty;
        }
        JPanel invoice = ChiTietHoaDonDialog.createInvoiceView(hoaDon);
        invoice.setPreferredSize(new Dimension(1000, 560));
        return invoice;
    }

    private void exportInvoicePdf() {
        if (hoaDon == null) {
            NotionMessageDialog.showMessageDialog(this,
                    "Không tìm thấy dữ liệu hóa đơn để in.",
                    "In vé", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Window owner = SwingUtilities.getWindowAncestor(this);
        JFrame frame = owner instanceof JFrame f ? f : null;
        new ChiTietHoaDonDialog(frame, hoaDon).exportInvoicePdf();
        // Handoff: nút In vé dùng lại layout PDF hóa đơn để dữ liệu khớp màn chi tiết hóa đơn.
        // Cảnh báo: nếu tách mẫu in vé riêng, đổi tại đây thay vì gọi trực tiếp ChiTietHoaDonDialog.
    }

    private void styleBtn(JButton btn, boolean primary) {
        BookingStepUi.styleActionButton(btn, primary);
    }

    @Override public String getTitle() { return "Bước 8 – Hoàn thành"; }
    @Override public JPanel getView()  { return this; }

    @Override
    public void setOnResult(Consumer<Object> cb) {
        this.callback = cb;
        boolean show = cb != null;
        btnSubmit.setVisible(show);
        btnCancel.setVisible(show);
        btnPanel.setVisible(show);
    }

    @Override public void reset() {}
}
