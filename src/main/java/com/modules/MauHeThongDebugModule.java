package com.modules;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class MauHeThongDebugModule extends JPanel implements AppModule {

    private Consumer<Object> callback;

    private static final Color SURFACE = AppColors.BACKGROUND;
    private static final Color CARD_BG = AppColors.SURFACE;
    private static final Color TEXT_DARK = AppColors.TEXT_PRIMARY;
    private static final Color TEXT_MUTED = AppColors.TEXT_SECONDARY;
    private static final Color BORDER_COLOR = AppColors.BORDER;
    private static final Color PRIMARY = AppColors.PRIMARY_DARK;

    public MauHeThongDebugModule() {
        setLayout(new BorderLayout());
        setBackground(SURFACE);
        buildUI();
    }

    private void buildUI() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(SURFACE);
        content.setBorder(new EmptyBorder(32, 40, 32, 40));

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 18, 0));

        JPanel breadcrumb = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        breadcrumb.setOpaque(false);
        addLabel(breadcrumb, "Hệ thống", Font.PLAIN, 12, TEXT_MUTED);
        addLabel(breadcrumb, " › ", Font.PLAIN, 12, TEXT_MUTED);
        addLabel(breadcrumb, "Debug icon", Font.BOLD, 12, PRIMARY);
        header.add(breadcrumb);

        JLabel title = new JLabel("Debug icon hệ thống");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(TEXT_DARK);
        title.setAlignmentX(LEFT_ALIGNMENT);
        header.add(Box.createVerticalStrut(10));
        header.add(title);

        JLabel desc = new JLabel("Xem thử bộ icon Java2D đơn sắc trước khi áp dụng vào các màn thật.");
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        desc.setForeground(TEXT_MUTED);
        desc.setAlignmentX(LEFT_ALIGNMENT);
        header.add(Box.createVerticalStrut(4));
        header.add(desc);

        content.add(header, BorderLayout.NORTH);
        content.add(buildDebugContent(), BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);
    }

    private JComponent buildDebugContent() {
        JPanel list = new JPanel();
        list.setLayout(new GridLayout(0, 5, 14, 14));
        list.setBackground(SURFACE);
        list.setBorder(new EmptyBorder(0, 4, 16, 4));

        for (LineIcons.Name iconName : LineIcons.Name.values()) {
            list.add(buildIconPreviewCard(iconName));
        }

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(SURFACE);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
        // Handoff: Menu debug now previews the full monochrome Java2D LineIcons enum.
        // Risk: restore color-token debug after final icon decision if still needed.
    }

    private String labelForIcon(LineIcons.Name name) {
        if (name == LineIcons.Name.TRAIN) return "Tàu hỏa";
        String lower = name.name().toLowerCase().replace('_', ' ');
        StringBuilder out = new StringBuilder();
        for (String part : lower.split(" ")) {
            if (part.isBlank()) continue;
            if (!out.isEmpty()) out.append(' ');
            out.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return out.toString();
    }

    private JPanel buildIconPreviewCard(LineIcons.Name iconName) {
        JPanel card = createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(18, 12, 18, 12)
        ));

        JPanel sizes = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        sizes.setOpaque(false);
        sizes.setAlignmentX(CENTER_ALIGNMENT);
        sizes.add(new JLabel(LineIcons.of(iconName, 16, TEXT_DARK)));
        sizes.add(new JLabel(LineIcons.of(iconName, 24, TEXT_DARK)));
        sizes.add(new JLabel(LineIcons.of(iconName, 32, TEXT_DARK)));

        JLabel titleLabel = new JLabel(labelForIcon(iconName));
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(TEXT_DARK);
        titleLabel.setAlignmentX(CENTER_ALIGNMENT);
        JLabel noteLabel = new JLabel("16 / 24 / 32px");
        noteLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        noteLabel.setForeground(TEXT_MUTED);
        noteLabel.setAlignmentX(CENTER_ALIGNMENT);

        card.add(sizes);
        card.add(Box.createVerticalStrut(14));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(noteLabel);
        return card;
    }

    private void addDebugGroup(JPanel parent, String title, String desc, List<Field> fields) {
        if (fields.isEmpty()) return;

        JPanel card = createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(16, 16, 16, 16)
        ));
        card.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(TEXT_DARK);
        lblTitle.setAlignmentX(LEFT_ALIGNMENT);
        card.add(lblTitle);

        JLabel lblDesc = new JLabel(desc);
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDesc.setForeground(TEXT_MUTED);
        lblDesc.setAlignmentX(LEFT_ALIGNMENT);
        card.add(Box.createVerticalStrut(4));
        card.add(lblDesc);
        card.add(Box.createVerticalStrut(12));

        for (Field field : fields) {
            try {
                Color value = (Color) field.get(null);
                card.add(buildColorRow(field.getName(), value));
                card.add(Box.createVerticalStrut(8));
            } catch (IllegalAccessException ignored) {
            }
        }

        parent.add(card);
        parent.add(Box.createVerticalStrut(14));
    }

    private JPanel buildColorRow(String name, Color color) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        JPanel swatch = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        swatch.setOpaque(false);
        swatch.setPreferredSize(new Dimension(46, 32));

        JLabel lblName = new JLabel(name + "  •  " + toHex(color));
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblName.setForeground(TEXT_DARK);

        JLabel lblUse = new JLabel(describeColor(name));
        lblUse.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblUse.setForeground(TEXT_MUTED);

        JPanel textBox = new JPanel();
        textBox.setLayout(new BoxLayout(textBox, BoxLayout.Y_AXIS));
        textBox.setOpaque(false);
        textBox.add(lblName);
        textBox.add(Box.createVerticalStrut(2));
        textBox.add(lblUse);

        row.add(swatch, BorderLayout.WEST);
        row.add(textBox, BorderLayout.CENTER);
        return row;
    }

    private List<Field> colorFieldsByPrefix(String... prefixes) {
        Map<String, Field> grouped = new LinkedHashMap<>();
        for (Field f : AppColors.class.getDeclaredFields()) {
            if (!Modifier.isStatic(f.getModifiers()) || f.getType() != Color.class) continue;
            String n = f.getName();
            for (String p : prefixes) {
                if (n.startsWith(p)) {
                    grouped.put(n, f);
                    break;
                }
            }
        }
        return new ArrayList<>(grouped.values());
    }

    private JPanel createCard() {
        JPanel p = new JPanel();
        p.setBackground(CARD_BG);
        return p;
    }

    private void addLabel(JPanel parent, String text, int style, int size, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", style, size));
        lbl.setForeground(color);
        parent.add(lbl);
    }

    private String toHex(Color c) {
        return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    }

    private String describeColor(String name) {
        if (name.startsWith("PRIMARY")) return "Brand, nút chính, tab active";
        if (name.startsWith("SUCCESS")) return "Thông báo thành công / trạng thái tốt";
        if (name.startsWith("WARNING")) return "Cảnh báo / cần chú ý";
        if (name.startsWith("ERROR")) return "Lỗi / từ chối / nguy hiểm";
        if (name.startsWith("BACKGROUND") || name.startsWith("SURFACE")) return "Màu nền layout, card, vùng phụ";
        if (name.startsWith("TEXT")) return "Màu chữ theo mức độ nhấn";
        if (name.startsWith("BORDER") || name.startsWith("DIVIDER")) return "Màu viền và đường phân cách";
        if (name.startsWith("INPUT") || name.startsWith("READONLY")) return "Nền input và field chỉ đọc";
        if (name.startsWith("ROW")) return "Màu bảng: dòng thường/hover/chọn";
        if (name.startsWith("SEAT")) return "Màu nghiệp vụ sơ đồ ghế";
        if (name.startsWith("SHADOW")) return "Màu hiệu ứng bóng";
        if (name.equals("AMOUNT")) return "Số tiền / giá trị dương";
        if (name.equals("BADGE_DANGER")) return "Badge cảnh báo mạnh";
        if (name.startsWith("PROMO")) return "Vùng thông tin khuyến mãi";
        return "Màu hệ thống";
    }

    @Override public String getTitle() { return "Debug màu hệ thống"; }
    @Override public JPanel getView() { return this; }
    @Override public void setOnResult(Consumer<Object> cb) { this.callback = cb; }
    @Override public void reset() {}
}
