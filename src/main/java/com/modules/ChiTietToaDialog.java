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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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

    // --- Dữ liệu ---
    private final ToaTau        toa;
    private final List<Ghe>     gheList;
    private final List<DoanTau> doanTauList;

    // --- Design tokens ---
    private static final Color SURFACE        = AppColors.BACKGROUND;
    private static final Color CARD_BG        = AppColors.SURFACE;
    private static final Color ON_SURFACE     = AppColors.TEXT_PRIMARY;
    private static final Color ON_SURF_VAR    = AppColors.TEXT_SECONDARY;
    private static final Color OUTLINE        = AppColors.BORDER;
    private static final Color PRIMARY        = AppColors.PRIMARY_DARK;

    // Màu ghế theo loại
    private static final Color CLR_CUNG_FILL   = AppColors.SEAT_HARD_FILL;
    private static final Color CLR_CUNG_BORDER = AppColors.SEAT_HARD_BORDER;
    private static final Color CLR_MEM_FILL    = AppColors.SEAT_SOFT_FILL;
    private static final Color CLR_MEM_BORDER  = AppColors.PRIMARY_HOVER;
    private static final Color CLR_GIUONG_FILL = AppColors.SEAT_BED_FILL;
    private static final Color CLR_GIUONG_BORDER = AppColors.SUCCESS_DARK;
    private static final Color CLR_KHOANG_LINE = AppColors.BORDER;

    public ChiTietToaDialog(JFrame owner, ToaTau toa) {
        super(owner, "Chi tiết toa: " + toa.getMaToaTau(), Dialog.ModalityType.MODELESS);
        this.toa        = toa;
        this.gheList    = daoGhe.findByToaTau(toa.getMaToaTau());
        this.doanTauList = daoCTDT.findDoanTauByToaTau(toa.getMaToaTau());

        setUndecorated(true);
        setResizable(false);
        setBackground(new Color(0, 0, 0, 0));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUI();
        installDismissOnOutsideClick();
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
        root.setBorder(BorderFactory.createLineBorder(OUTLINE, 1));

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
        btnClose.setForeground(AppColors.SURFACE);
        btnClose.setFocusPainted(false);
        btnClose.setBorder(new EmptyBorder(8, 24, 8, 24));
        btnClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose());
        footer.add(btnClose);

        root.add(footer, BorderLayout.SOUTH);

        setContentPane(ThemNhanVienDialog.buildShadowWrapper(root));
    }

    private void installDismissOnOutsideClick() {
        addWindowFocusListener(new WindowAdapter() {
            @Override public void windowLostFocus(WindowEvent e) {
                SwingUtilities.invokeLater(() -> {
                    if (isShowing() && !isFocused()) dispose();
                });
            }
        });
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, OUTLINE),
                new EmptyBorder(16, 24, 16, 24)));

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
        badge.setForeground(AppColors.SURFACE);
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

        ToaSeatDiagramPanel canvas = new ToaSeatDiagramPanel();
        canvas.setData(toa, gheList);
        canvas.setSelectable(false);
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
            empty.setForeground(ON_SURF_VAR);
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
            iconLbl.setIcon(LineIcons.contained(LineIcons.Name.TRAIN, 40, 26));
        } catch (Exception ex) {
            iconLbl.setText("TAU");
        }
        // Handoff: header train SVG uses a 40px box with inner padding to avoid clipped strokes.
        // Risk: keep card layout stable by centering icon instead of changing surrounding spacing.

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
            case GHE_CUNG   -> AppColors.SEAT_HARD_BORDER;
            case GHE_MEM    -> AppColors.PRIMARY;
            case GIUONG_NAM -> AppColors.SUCCESS_DARK;
        };
    }
}
