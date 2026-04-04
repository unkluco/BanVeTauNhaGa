package com.modules;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Module hiển thị chi tiết của BẤT KỲ thực thể nào trong hệ thống.
 *
 * Cách dùng:
 *   LinkedHashMap<String,String> fields = new LinkedHashMap<>();
 *   fields.put("Họ tên", "Nguyễn Văn A");
 *   fields.put("Vai trò", "ADMIN");
 *   ...
 *   EntityDetailModule m = new EntityDetailModule(
 *       "Nhân viên",          // loại thực thể (hiện trên badge header)
 *       new Color(0x005D90),  // màu chủ đạo
 *       "Nguyễn Văn A",       // tên hiển thị chính
 *       "NV-001",             // ID thực thể
 *       fields
 *   );
 *   ModuleLauncher.asDialog(m, parentFrame, result -> {});
 */
public class EntityDetailModule extends JPanel implements AppModule {

    // ── Màu sắc ──────────────────────────────────────────────────────────────
    private static final Color CARD_BG   = Color.WHITE;
    private static final Color TEXT_MAIN = new Color(0x1A1A2E);
    private static final Color TEXT_SUB  = new Color(0x6B7280);
    private static final Color ROW_ALT   = new Color(0xF8FAFC);
    private static final Color FOOTER_BG = new Color(0xF3F4F6);
    private static final Color DIVIDER   = new Color(0xE5E7EB);

    // ── Dữ liệu ──────────────────────────────────────────────────────────────
    private final String typeLabel;
    private final Color  typeColor;
    private final String entityName;
    private final String entityId;
    private final LinkedHashMap<String, String> fields;

    // ── State ─────────────────────────────────────────────────────────────────
    private Consumer<Object> callback;
    private JButton btnClose;
    private JPanel  btnPanel;

    // ─────────────────────────────────────────────────────────────────────────
    public EntityDetailModule(String typeLabel, Color typeColor,
                               String entityName, String entityId,
                               LinkedHashMap<String, String> fields) {
        this.typeLabel  = typeLabel;
        this.typeColor  = typeColor;
        this.entityName = entityName;
        this.entityId   = entityId;
        this.fields     = fields;
        setLayout(new BorderLayout());
        setBackground(CARD_BG);
        buildUI();
    }

    // ── BUILD UI ──────────────────────────────────────────────────────────────
    private void buildUI() {
        add(buildHeader(),  BorderLayout.NORTH);
        add(buildBody(),    BorderLayout.CENTER);
        add(buildFooter(),  BorderLayout.SOUTH);
    }

    // ── HEADER ────────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(0, 6));
        header.setBackground(typeColor);
        header.setBorder(new EmptyBorder(18, 22, 18, 22));

        // Badge loại thực thể
        JLabel badge = new JLabel(typeLabel.toUpperCase());
        badge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        badge.setForeground(new Color(255, 255, 255, 170));

        // Tên chính
        JLabel name = new JLabel(entityName.isBlank() ? entityId : entityName);
        name.setFont(new Font("Segoe UI", Font.BOLD, 20));
        name.setForeground(Color.WHITE);

        // ID phụ — hiện sau tên
        JLabel idLbl = new JLabel("  #" + entityId);
        idLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        idLbl.setForeground(new Color(255, 255, 255, 200));

        JPanel nameRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        nameRow.setBackground(typeColor);
        nameRow.add(name);
        nameRow.add(idLbl);

        header.add(badge,   BorderLayout.NORTH);
        header.add(nameRow, BorderLayout.CENTER);
        return header;
    }

    // ── BODY (danh sách trường) ───────────────────────────────────────────────
    private JScrollPane buildBody() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(CARD_BG);

        boolean alt = false;
        for (Map.Entry<String, String> e : fields.entrySet()) {
            body.add(buildFieldRow(e.getKey(), e.getValue(), alt));
            alt = !alt;
        }
        // padding dưới cùng để không dính footer
        body.add(Box.createVerticalStrut(8));

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(CARD_BG);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private JPanel buildFieldRow(String label, String value, boolean alt) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setBackground(alt ? ROW_ALT : CARD_BG);
        row.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 1, 0, DIVIDER),
            new EmptyBorder(11, 22, 11, 22)
        ));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        row.setMinimumSize(new Dimension(0, 46));
        row.setPreferredSize(new Dimension(500, 46));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(TEXT_SUB);
        lbl.setPreferredSize(new Dimension(180, 20));

        String display = (value == null || value.isBlank()) ? "\u2014" : value;
        JLabel val = new JLabel(display);
        val.setFont(new Font("Segoe UI",
            "\u2014".equals(display) ? Font.ITALIC : Font.PLAIN, 13));
        val.setForeground("\u2014".equals(display) ? TEXT_SUB : TEXT_MAIN);

        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.CENTER);
        return row;
    }

    // ── FOOTER ────────────────────────────────────────────────────────────────
    private JPanel buildFooter() {
        btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 10));
        btnPanel.setBackground(FOOTER_BG);
        btnPanel.setBorder(new MatteBorder(1, 0, 0, 0, DIVIDER));

        btnClose = new JButton("  \u00D3ng  ");
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnClose.setBackground(typeColor);
        btnClose.setForeground(Color.WHITE);
        btnClose.setFocusPainted(false);
        btnClose.setBorderPainted(false);
        btnClose.setOpaque(true);
        btnClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClose.setBorder(new EmptyBorder(8, 28, 8, 28));
        btnClose.addActionListener(e -> {
            if (callback != null) callback.accept(null);
            // Tự đóng cửa sổ cha (JDialog hoặc JFrame) bất kể launcher nào được dùng
            Window w = SwingUtilities.getWindowAncestor(EntityDetailModule.this);
            if (w != null) w.dispose();
        });

        // Hover effect
        btnClose.addMouseListener(new java.awt.event.MouseAdapter() {
            final Color base = typeColor;
            final Color hover = typeColor.darker();
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { btnClose.setBackground(hover); }
            @Override public void mouseExited(java.awt.event.MouseEvent e)  { btnClose.setBackground(base);  }
        });

        btnPanel.add(btnClose);
        return btnPanel;
    }

    // ── AppModule ─────────────────────────────────────────────────────────────
    @Override public String getTitle()  { return typeLabel + "  —  " + entityId; }
    @Override public JPanel  getView()  { return this; }
    @Override public void reset() {}

    @Override
    public void setOnResult(Consumer<Object> cb) {
        this.callback = cb;
        // Nút "Đóng" luôn hiện (không có standalone mode cho module view-only)
    }
}
