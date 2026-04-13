package com.modules;

import com.dao.DAO_ChiTietKhuyenMai;
import com.dao.DAO_KhuyenMai;
import com.entity.ChiTietKhuyenMai;
import com.entity.KhuyenMai;
import com.entity.Tuyen;
import com.enums.LoaiGhe;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Bước 5b — Chọn khuyến mãi.
 * Lọc các ChiTietKhuyenMai đang áp dụng phù hợp với tuyến + loại ghế đã chọn.
 * Output:
 *   - ChiTietKhuyenMai (áp dụng KM)
 *   - "SKIP"           (không áp dụng)
 *   - null             (quay lại)
 */
public class BanVeStep5bModule extends JPanel implements AppModule {

    private Consumer<Object> callback;

    private final Tuyen   tuyen;
    private final LoaiGhe loaiGhe;
    private final double  baseTotal;

    private final DAO_KhuyenMai       daoKM    = new DAO_KhuyenMai();
    private final DAO_ChiTietKhuyenMai daoCTKM = new DAO_ChiTietKhuyenMai();

    // Design tokens
    private static final Color PRIMARY       = new Color(0x00, 0x5D, 0x90);
    private static final Color SURFACE       = new Color(0xF8, 0xFA, 0xFC);
    private static final Color CARD_BG       = Color.WHITE;
    private static final Color ON_SURFACE    = new Color(0x1A, 0x1D, 0x21);
    private static final Color ON_SURF_VAR   = new Color(0x5F, 0x67, 0x70);
    private static final Color OUTLINE       = new Color(0xDE, 0xE3, 0xE8);
    private static final Color PRIMARY_LIGHT = new Color(0xE3, 0xF2, 0xFD);
    private static final Color PROMO_BG      = new Color(0xFF, 0xF8, 0xE1);
    private static final Color PROMO_BORDER  = new Color(0xFF, 0xB3, 0x00);
    private static final Color PROMO_FG      = new Color(0xE6, 0x52, 0x00);

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final List<ChiTietKhuyenMai> validPromos = new ArrayList<>();
    private final ButtonGroup            bg           = new ButtonGroup();
    private JRadioButton                 rbNone;
    private final List<JRadioButton>     rbPromos     = new ArrayList<>();

    private JButton btnSubmit, btnCancel;
    private JPanel  btnPanel;

    // =========================================================================
    //  CONSTRUCTOR
    // =========================================================================

    public BanVeStep5bModule(Tuyen tuyen, LoaiGhe loaiGhe, double baseTotal) {
        this.tuyen      = tuyen;
        this.loaiGhe    = loaiGhe;
        this.baseTotal  = baseTotal;
        setLayout(new BorderLayout());
        setBackground(SURFACE);
        loadPromos();
        buildUI();
    }

    // =========================================================================
    //  LOAD DATA
    // =========================================================================

    private void loadPromos() {
        KhuyenMai km = daoKM.getKhuyenMaiHienHanh();
        if (km == null) return;

        String maTuyen = (tuyen != null) ? tuyen.getMaTuyen() : null;

        for (ChiTietKhuyenMai ctg : daoCTKM.findByKhuyenMai(km.getMaKhuyenMai())) {
            boolean tuyenOk = ctg.getTuyen() == null
                || (maTuyen != null && ctg.getTuyen().getMaTuyen().equals(maTuyen));
            boolean loaiOk  = ctg.getLoaiGhe() == null
                || ctg.getLoaiGhe() == loaiGhe;
            if (tuyenOk && loaiOk) validPromos.add(ctg);
        }
    }

    // =========================================================================
    //  BUILD UI
    // =========================================================================

    private void buildUI() {
        add(buildHeaderBar(), BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(SURFACE);
        center.setBorder(new EmptyBorder(24, 80, 24, 80));

        if (validPromos.isEmpty()) {
            center.add(buildNoPromoCard());
        } else {
            center.add(buildPromoList());
        }

        JScrollPane scroll = new JScrollPane(center);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(SURFACE);
        add(scroll, BorderLayout.CENTER);

        btnSubmit = new JButton("Tiếp theo →");
        styleBtn(btnSubmit, true);
        btnSubmit.addActionListener(e -> execute());

        btnCancel = new JButton("← Quay lại");
        styleBtn(btnCancel, false);
        btnCancel.addActionListener(e -> { if (callback != null) callback.accept(null); });

        btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        btnPanel.setBackground(SURFACE);
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, OUTLINE));
        btnPanel.add(btnCancel);
        btnPanel.add(btnSubmit);
        btnPanel.setVisible(false);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private JPanel buildHeaderBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 12));
        bar.setBackground(PRIMARY_LIGHT);
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, OUTLINE));
        JLabel lbl = new JLabel("Chọn khuyến mãi");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl.setForeground(PRIMARY);
        bar.add(lbl);
        return bar;
    }

    private JPanel buildNoPromoCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(OUTLINE, 1),
            new EmptyBorder(24, 32, 24, 32)
        ));

        JLabel icon = new JLabel("Hiện không có chương trình khuyến mãi phù hợp với chuyến này.");
        icon.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        icon.setForeground(ON_SURF_VAR);
        icon.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(icon);

        JLabel hint = new JLabel("Nhấn Tiếp theo để tiếp tục.");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        hint.setForeground(ON_SURF_VAR);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        hint.setBorder(new EmptyBorder(6, 0, 0, 0));
        card.add(hint);

        return card;
    }

    private JPanel buildPromoList() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(SURFACE);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("Chọn một khuyến mãi áp dụng:");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(ON_SURFACE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.setBorder(new EmptyBorder(0, 0, 12, 0));
        panel.add(title);

        // Option: không áp dụng
        rbNone = new JRadioButton("Không áp dụng khuyến mãi");
        rbNone.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        rbNone.setBackground(CARD_BG);
        rbNone.setFocusPainted(false);
        rbNone.setSelected(true);
        bg.add(rbNone);

        JPanel noneCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 12));
        noneCard.setBackground(CARD_BG);
        noneCard.setBorder(BorderFactory.createLineBorder(OUTLINE, 1));
        noneCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        noneCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        noneCard.add(rbNone);
        noneCard.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { rbNone.setSelected(true); }
        });
        panel.add(noneCard);

        // Các option khuyến mãi
        for (int i = 0; i < validPromos.size(); i++) {
            JRadioButton rb = new JRadioButton();
            rb.setBackground(PROMO_BG);
            rb.setFocusPainted(false);
            bg.add(rb);
            rbPromos.add(rb);

            panel.add(Box.createVerticalStrut(8));
            panel.add(buildPromoCard(validPromos.get(i), rb));
        }

        return panel;
    }

    private JPanel buildPromoCard(ChiTietKhuyenMai ctg, JRadioButton rb) {
        JPanel card = new JPanel(new BorderLayout(14, 0));
        card.setBackground(PROMO_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PROMO_BORDER, 1),
            new EmptyBorder(12, 16, 12, 16)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        card.add(rb, BorderLayout.WEST);

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBackground(PROMO_BG);

        String name = ctg.getTenChiTiet() != null && !ctg.getTenChiTiet().isBlank()
            ? ctg.getTenChiTiet()
            : (ctg.getKhuyenMai() != null ? ctg.getKhuyenMai().getTenKhuyenMai() : "Khuyến mãi");

        int pct = (int) Math.round(ctg.getPhanTramGiam() * 100); // 0.20 → 20
        JLabel lblName = new JLabel(name + "  —  Giảm " + pct + "%");
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblName.setForeground(PROMO_FG);
        info.add(lblName);

        // Tiết kiệm bao nhiêu tiền (nếu biết tổng tiền gốc)
        if (baseTotal > 0) {
            double saving = baseTotal * ctg.getPhanTramGiam();
            NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
            JLabel lblSaving = new JLabel("Tiết kiệm: −" + nf.format((long) saving) + " VND");
            lblSaving.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblSaving.setForeground(PROMO_FG);
            info.add(lblSaving);
        }

        if (ctg.getKhuyenMai() != null) {
            KhuyenMai km = ctg.getKhuyenMai();
            if (km.getThoiGianBatDau() != null && km.getThoiGianKetThuc() != null) {
                JLabel lblPeriod = new JLabel("Áp dụng: "
                    + km.getThoiGianBatDau().format(DT_FMT)
                    + " → " + km.getThoiGianKetThuc().format(DT_FMT));
                lblPeriod.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                lblPeriod.setForeground(ON_SURF_VAR);
                info.add(lblPeriod);
            }
        }

        String scope = "Áp dụng cho: "
            + (ctg.getTuyen()   != null ? ctg.getTuyen().getMaTuyen()   : "Tất cả tuyến")
            + "  ·  "
            + (ctg.getLoaiGhe() != null ? ctg.getLoaiGhe().toString()   : "Tất cả loại ghế");
        JLabel lblScope = new JLabel(scope);
        lblScope.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblScope.setForeground(ON_SURF_VAR);
        info.add(lblScope);

        card.add(info, BorderLayout.CENTER);

        // Click card = chọn radio
        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { rb.setSelected(true); }
        });

        return card;
    }

    // =========================================================================
    //  EXECUTE
    // =========================================================================

    private void execute() {
        if (rbNone != null && rbNone.isSelected()) {
            if (callback != null) callback.accept("SKIP");
            return;
        }
        for (int i = 0; i < rbPromos.size(); i++) {
            if (rbPromos.get(i).isSelected()) {
                if (callback != null) callback.accept(validPromos.get(i));
                return;
            }
        }
        // Mặc định: không áp dụng
        if (callback != null) callback.accept("SKIP");
    }

    // =========================================================================
    //  HELPERS
    // =========================================================================

    private void styleBtn(JButton btn, boolean primary) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 24, 10, 24));
        if (primary) {
            btn.setBackground(PRIMARY); btn.setForeground(Color.WHITE); btn.setOpaque(true);
        } else {
            btn.setBackground(CARD_BG); btn.setForeground(ON_SURF_VAR); btn.setOpaque(true);
        }
    }

    // =========================================================================
    //  AppModule
    // =========================================================================

    @Override public String getTitle() { return "Bước 5b – Khuyến mãi"; }
    @Override public JPanel getView()  { return this; }

    @Override
    public void setOnResult(Consumer<Object> cb) {
        this.callback = cb;
        boolean show = cb != null;
        btnSubmit.setVisible(show);
        btnCancel.setVisible(show);
        btnPanel.setVisible(show);
    }

    @Override
    public void reset() {
        if (rbNone != null) rbNone.setSelected(true);
    }
}
