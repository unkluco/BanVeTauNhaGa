package com.modules;

import com.dao.DAO_NhanVien;
import com.entity.Ga;
import com.entity.NhanVien;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ThongTinCaNhanModule extends JPanel implements AppModule {

    private Consumer<Object> callback;
    private NhanVien currentUser;
    private final DAO_NhanVien daoNV = new DAO_NhanVien();

    private static final Color PRIMARY       = NotionTheme.ACCENT;
    private static final Color PRIMARY_LIGHT = NotionTheme.ACCENT_SOFT;
    private static final Color SURFACE       = NotionTheme.PAGE;
    private static final Color CARD_BG       = NotionTheme.CARD;
    private static final Color TEXT_DARK     = NotionTheme.TEXT;
    private static final Color TEXT_MUTED    = NotionTheme.TEXT_MUTED;
    private static final Color BORDER_COLOR  = NotionTheme.BORDER;
    private static final Color FIELD_BG      = NotionTheme.CARD_MUTED;
    private static final Color SUCCESS       = AppColors.SUCCESS;
    private static final Color ERROR_COLOR   = AppColors.ERROR;
    private static final Color HERO_START    = NotionTheme.NAVY;
    private static final Color HERO_END      = NotionTheme.ACCENT;

    private JTextField txtPhone, txtEmail, txtAddress;
    private JPasswordField txtOldPass, txtNewPass, txtConfirmPass;
    private Timer fadeTimer;

    private static final double LEFT_RATIO = 0.34;

    public ThongTinCaNhanModule(NhanVien user) {
        this.currentUser = user;
        setLayout(new BorderLayout());
        setBackground(SURFACE);
        buildUI();
    }

    private void buildUI() {
        NhanVien fresh = daoNV.findById(currentUser.getMaNV());
        if (fresh != null) currentUser = fresh;

        JPanel content = new JPanel(new BorderLayout(0, 24));
        content.setBackground(SURFACE);
        content.setBorder(new EmptyBorder(32, 40, 32, 40));

        content.add(buildHeroPanel(), BorderLayout.NORTH);

        content.add(buildInfoTab(), BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);
    }

    private JPanel buildHeroPanel() {
        JPanel hero = new JPanel(new BorderLayout(24, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, HERO_START, w, h, HERO_END);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, w, h, 28, 28);
                g2.setColor(AppColors.withAlpha(Color.WHITE, 42));
                g2.fillRoundRect(w - 245, 22, 138, 92, 28, 28);
                g2.setColor(AppColors.withAlpha(Color.WHITE, 70));
                g2.fillOval(w - 156, -18, 126, 126);
                g2.setColor(AppColors.withAlpha(NotionTheme.YELLOW, 95));
                g2.fillRoundRect(w - 300, h - 38, 164, 16, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        hero.setOpaque(false);
        hero.setPreferredSize(new Dimension(10, 150));
        hero.setBorder(new EmptyBorder(26, 30, 26, 30));

        JPanel copy = new JPanel();
        copy.setOpaque(false);
        copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));

        JLabel eyebrow = new JLabel("WORKSPACE / NHÂN SỰ");
        eyebrow.setFont(new Font("Segoe UI", Font.BOLD, 12));
        eyebrow.setForeground(AppColors.withAlpha(Color.WHITE, 175));
        JLabel title = new JLabel("Hồ sơ cá nhân");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(Color.WHITE);
        JLabel desc = new JLabel("Quản lý thông tin liên hệ, định danh và bảo mật tài khoản nội bộ.");
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        desc.setForeground(AppColors.withAlpha(Color.WHITE, 210));
        copy.add(eyebrow);
        copy.add(Box.createVerticalStrut(14));
        copy.add(title);
        copy.add(Box.createVerticalStrut(8));
        copy.add(desc);
        hero.add(copy, BorderLayout.CENTER);

        hero.add(new ProfileHeroGlyph(), BorderLayout.EAST);
        // Handoff: hero chỉ trang trí và không chứa logic; giữ khác bảng quản lý bằng glyph hồ sơ riêng.
        // Nếu đổi theme màu, cập nhật HERO_START/HERO_END để không ảnh hưởng các form bên dưới.
        return hero;
    }

    private JComponent buildInfoTab() {
        JPanel columns = new JPanel() {
            @Override
            public void doLayout() {
                int w = getWidth();
                int h = getHeight();
                int gap = 24;
                int leftW = Math.max(320, (int) (w * LEFT_RATIO) - gap / 2);
                leftW = Math.min(leftW, Math.max(300, w - 520));
                int rightW = w - leftW - gap;
                Component[] cc = getComponents();
                if (cc.length >= 2) {
                    int leftPrefH = cc[0].getPreferredSize().height;
                    cc[0].setBounds(0, 0, leftW, leftPrefH);
                    cc[1].setBounds(leftW + gap, 0, rightW, h);
                }
            }

            @Override
            public Dimension getPreferredSize() {
                Component[] cc = getComponents();
                if (cc.length < 2) return super.getPreferredSize();
                int leftH = cc[0].getPreferredSize().height;
                int rightH = cc[1].getPreferredSize().height;
                return new Dimension(960, Math.max(leftH, rightH));
            }
        };
        columns.setOpaque(false);

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);
        leftPanel.add(buildProfileCard());

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);
        rightPanel.add(buildQuickInsightStrip());
        rightPanel.add(Box.createVerticalStrut(20));
        rightPanel.add(buildIdentitySection());
        rightPanel.add(Box.createVerticalStrut(20));
        rightPanel.add(buildContactSection());

        columns.add(leftPanel);
        columns.add(rightPanel);

        JScrollPane scroll = new JScrollPane(columns);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(SURFACE);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private JPanel buildQuickInsightStrip() {
        JPanel strip = new JPanel(new GridLayout(1, 3, 14, 0));
        strip.setOpaque(false);
        strip.setAlignmentX(LEFT_ALIGNMENT);
        strip.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));
        String roleName = currentUser.getVaiTro() != null ? currentUser.getVaiTro().toString() : "—";
        String gaName = resolveGaName(currentUser.getGaLamViec());
        strip.add(buildInsightCard("Vai trò", formatRole(roleName), NotionTheme.ACCENT_SOFT, PRIMARY));
        strip.add(buildInsightCard("Ga làm việc", gaName != null ? gaName : "Chưa xác định", NotionTheme.MINT, SUCCESS));
        strip.add(buildInsightCard("Trạng thái", currentUser.getTrangThai() != null ? currentUser.getTrangThai().toString() : "Đang làm", NotionTheme.YELLOW, AppColors.WARNING_DARK));
        return strip;
    }

    private JPanel buildInsightCard(String label, String value, Color bg, Color fg) {
        JPanel card = new JPanel(new BorderLayout(0, 6)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(AppColors.withAlpha(fg, 80));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(14, 16, 14, 16));
        JLabel l = new JLabel(label.toUpperCase());
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setForeground(TEXT_MUTED);
        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.BOLD, 14));
        v.setForeground(fg);
        card.add(l, BorderLayout.NORTH);
        card.add(v, BorderLayout.CENTER);
        return card;
    }

    private JComponent buildDebugTab() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(SURFACE);

        JPanel list = new JPanel();
        list.setLayout(new GridLayout(0, 5, 14, 14));
        list.setBackground(SURFACE);
        list.setBorder(new EmptyBorder(18, 18, 18, 18));

        list.add(buildIconPreviewCard("Hóa đơn", new ReceiptPreviewIcon()));
        list.add(buildIconPreviewCard("Tàu", new TrainPreviewIcon()));
        list.add(buildIconPreviewCard("Vé", new TicketPreviewIcon()));
        list.add(buildIconPreviewCard("Tìm kiếm", new SearchPreviewIcon()));
        list.add(buildIconPreviewCard("Lưu", new SavePreviewIcon()));

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(SURFACE);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
        // Handoff: debug tab temporarily previews the 5 monochrome Java2D icons before rollout.
        // Risk: this replaces color-token debug content only for visual review, not business logic.
    }

    private JPanel buildIconPreviewCard(String title, Icon icon) {
        JPanel card = createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(24, 12, 22, 12)
        ));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setAlignmentX(CENTER_ALIGNMENT);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(TEXT_DARK);
        titleLabel.setAlignmentX(CENTER_ALIGNMENT);
        JLabel noteLabel = new JLabel("Java2D / 32px");
        noteLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        noteLabel.setForeground(TEXT_MUTED);
        noteLabel.setAlignmentX(CENTER_ALIGNMENT);

        card.add(iconLabel);
        card.add(Box.createVerticalStrut(14));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(noteLabel);
        return card;
    }

    private abstract static class PreviewIcon implements Icon {
        @Override public int getIconWidth() { return 32; }
        @Override public int getIconHeight() { return 32; }

        protected Graphics2D setup(Graphics graphics, int x, int y) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.translate(x, y);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(NotionTheme.TEXT);
            g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            return g2;
        }
    }

    private static final class ReceiptPreviewIcon extends PreviewIcon {
        @Override public void paintIcon(Component c, Graphics graphics, int x, int y) {
            Graphics2D g2 = setup(graphics, x, y);
            java.awt.geom.Path2D p = new java.awt.geom.Path2D.Double();
            p.moveTo(9, 6); p.lineTo(23, 6); p.quadTo(25, 6, 25, 8); p.lineTo(25, 26);
            p.lineTo(21, 23); p.lineTo(18, 26); p.lineTo(15, 23); p.lineTo(12, 26); p.lineTo(8, 23); p.lineTo(8, 8); p.quadTo(8, 6, 9, 6);
            g2.draw(p); g2.drawLine(12, 12, 21, 12); g2.drawLine(12, 17, 21, 17); g2.dispose();
        }
    }

    private static final class TrainPreviewIcon extends PreviewIcon {
        @Override public void paintIcon(Component c, Graphics graphics, int x, int y) {
            Graphics2D g2 = setup(graphics, x, y);
            java.awt.geom.Path2D body = new java.awt.geom.Path2D.Double();
            body.moveTo(6, 11); body.lineTo(15, 11); body.curveTo(22, 11, 26, 14, 26, 19);
            body.quadTo(26, 22, 23, 22); body.lineTo(6, 22); body.closePath();
            g2.draw(body); g2.drawLine(6, 15, 24, 15); g2.drawLine(10, 11, 10, 15); g2.drawLine(16, 11, 17, 15); g2.drawLine(6, 27, 26, 27); g2.dispose();
        }
    }

    private static final class TicketPreviewIcon extends PreviewIcon {
        @Override public void paintIcon(Component c, Graphics graphics, int x, int y) {
            Graphics2D g2 = setup(graphics, x, y);
            java.awt.geom.Path2D p = new java.awt.geom.Path2D.Double();
            p.moveTo(7, 9); p.lineTo(25, 9); p.lineTo(25, 14); p.curveTo(22, 14, 22, 18, 25, 18); p.lineTo(25, 23); p.lineTo(7, 23); p.lineTo(7, 18); p.curveTo(10, 18, 10, 14, 7, 14); p.closePath();
            g2.draw(p); g2.drawLine(17, 11, 17, 13); g2.drawLine(17, 16, 17, 18); g2.dispose();
        }
    }

    private static final class SearchPreviewIcon extends PreviewIcon {
        @Override public void paintIcon(Component c, Graphics graphics, int x, int y) {
            Graphics2D g2 = setup(graphics, x, y);
            g2.draw(new java.awt.geom.Ellipse2D.Double(7, 7, 13, 13)); g2.drawLine(18, 18, 25, 25); g2.dispose();
        }
    }

    private static final class SavePreviewIcon extends PreviewIcon {
        @Override public void paintIcon(Component c, Graphics graphics, int x, int y) {
            Graphics2D g2 = setup(graphics, x, y);
            g2.drawRoundRect(7, 6, 18, 20, 3, 3); g2.drawLine(11, 6, 11, 13); g2.drawLine(11, 13, 21, 13); g2.drawLine(21, 6, 21, 13); g2.drawRoundRect(11, 18, 10, 8, 2, 2); g2.dispose();
        }
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

    // =====================================================================
    //  PROFILE CARD
    // =====================================================================
    private JPanel buildProfileCard() {
        JPanel card = createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(0, 0, 24, 0)
        ));

        card.add(new ProfileAccentPanel());
        card.add(Box.createVerticalStrut(18));

        // Avatar — fixed-size wrapper to prevent BoxLayout stretching
        JPanel avatarWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        avatarWrapper.setOpaque(false);
        avatarWrapper.setAlignmentX(CENTER_ALIGNMENT);
        avatarWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 106));
        JPanel avatar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppColors.withAlpha(PRIMARY, 28));
                g2.fillOval(0, 0, 104, 104);
                g2.setPaint(new GradientPaint(4, 4, NotionTheme.ACCENT_SOFT, 104, 104, NotionTheme.MINT));
                g2.fillOval(4, 4, 96, 96);
                g2.setColor(AppColors.SURFACE);
                g2.setStroke(new BasicStroke(3f));
                g2.drawOval(4, 4, 96, 96);
                g2.setColor(PRIMARY);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 34));
                String ini = getInitials(currentUser.getHoTen());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(ini, (104 - fm.stringWidth(ini)) / 2, (104 + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(104, 104));
        avatar.setMaximumSize(new Dimension(104, 104));
        avatarWrapper.add(avatar);
        card.add(avatarWrapper);
        card.add(Box.createVerticalStrut(16));

        // Name + ID
        JLabel lblName = new JLabel(currentUser.getHoTen(), SwingConstants.CENTER);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblName.setForeground(TEXT_DARK);
        lblName.setAlignmentX(CENTER_ALIGNMENT);
        lblName.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        card.add(lblName);
        card.add(Box.createVerticalStrut(4));

        JLabel lblId = new JLabel("Mã NV: " + currentUser.getMaNV(), SwingConstants.CENTER);
        lblId.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblId.setForeground(PRIMARY);
        lblId.setAlignmentX(CENTER_ALIGNMENT);
        lblId.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
        card.add(lblId);
        card.add(Box.createVerticalStrut(20));

        // Info rows
        String roleName = currentUser.getVaiTro() != null ? currentUser.getVaiTro().toString() : "";
        card.add(wrapProfileRow(buildInfoRow("Bộ phận", formatRole(roleName))));
        card.add(Box.createVerticalStrut(8));
        String gaName = resolveGaName(currentUser.getGaLamViec());
        card.add(wrapProfileRow(buildInfoRow("Khu vực", gaName != null ? gaName : "Chưa xác định")));
        card.add(Box.createVerticalStrut(8));
        // Handoff: 2 dòng info có wrapper padding ngang để không chạm mép card cha.
        // Password form đã tách luôn bên dưới, không còn nút toggle trung gian.
        card.add(Box.createVerticalStrut(18));
        card.add(buildInlinePasswordSection());

        return card;
    }

    private JPanel buildInlinePasswordSection() {
        JPanel section = new JPanel();
        section.setOpaque(false);
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBorder(new EmptyBorder(0, 24, 0, 24));
        section.setAlignmentX(CENTER_ALIGNMENT);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 360));

        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_COLOR);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        section.add(sep);
        section.add(Box.createVerticalStrut(18));

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleRow.setOpaque(false);
        titleRow.setAlignmentX(LEFT_ALIGNMENT);
        titleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        JLabel lblPwTitle = new JLabel("Đổi mật khẩu");
        lblPwTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblPwTitle.setForeground(TEXT_DARK);
        lblPwTitle.setIcon(createLockIcon());
        lblPwTitle.setIconTextGap(8);
        titleRow.add(lblPwTitle);
        section.add(titleRow);
        section.add(Box.createVerticalStrut(16));

        section.add(createFieldLabel("Mật khẩu cũ"));
        txtOldPass = new JPasswordField(); stylePasswordField(txtOldPass);
        section.add(txtOldPass);
        section.add(Box.createVerticalStrut(10));

        section.add(createFieldLabel("Mật khẩu mới"));
        txtNewPass = new JPasswordField(); stylePasswordField(txtNewPass);
        section.add(txtNewPass);
        section.add(Box.createVerticalStrut(10));

        section.add(createFieldLabel("Xác nhận mật khẩu mới"));
        txtConfirmPass = new JPasswordField(); stylePasswordField(txtConfirmPass);
        section.add(txtConfirmPass);
        section.add(Box.createVerticalStrut(20));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(LEFT_ALIGNMENT);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JButton btnPwCancel = createTextButton("Xóa nhập", TEXT_MUTED);
        btnPwCancel.addActionListener(e -> { txtOldPass.setText(""); txtNewPass.setText(""); txtConfirmPass.setText(""); });
        JButton btnPwConfirm = createFilledButton("Xác nhận");
        btnPwConfirm.addActionListener(e -> handleChangePassword());
        btnRow.add(btnPwCancel);
        btnRow.add(btnPwConfirm);
        section.add(btnRow);
        // Handoff: password nằm cùng profile card để tránh ranh giới 2 card; chỉ còn separator nhẹ.
        // Logic đổi mật khẩu vẫn dùng cùng field/handler cũ, không đổi nghiệp vụ.
        return section;
    }
    // =====================================================================
    //  IDENTITY SECTION
    // =====================================================================
    private JPanel buildIdentitySection() {
        JPanel card = createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(28, 28, 28, 28)
        ));

        JPanel header = buildSectionTitle("Thông tin định danh", "Dữ liệu do bộ phận nhân sự quản lý", PRIMARY, createLineIcon("id", PRIMARY));

        JLabel lblNote = new JLabel("* Thông tin này do nhân sự quản lý");
        lblNote.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblNote.setForeground(TEXT_MUTED);
        header.add(lblNote, BorderLayout.EAST);
        card.add(header);
        card.add(Box.createVerticalStrut(20));

        JPanel grid = new JPanel(new GridLayout(2, 2, 24, 16));
        grid.setOpaque(false);
        grid.setAlignmentX(LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        String cccd = currentUser.getCccd() != null ? currentUser.getCccd() : "—";
        String ngaySinh = currentUser.getNgaySinh() != null
                ? currentUser.getNgaySinh().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "—";
        String gioiTinh = currentUser.getGioiTinh() != null
                ? ("NAM".equalsIgnoreCase(currentUser.getGioiTinh()) ? "Nam" : "Nữ") : "—";
        String quocTich = currentUser.getQuocTich() != null ? currentUser.getQuocTich() : "—";

        grid.add(buildReadOnlyField("Số CCCD / Hộ chiếu", cccd));
        grid.add(buildReadOnlyField("Ngày sinh", ngaySinh));
        grid.add(buildReadOnlyField("Giới tính", gioiTinh));
        grid.add(buildReadOnlyField("Quốc tịch", quocTich));
        card.add(grid);

        return card;
    }

    // =====================================================================
    //  CONTACT SECTION
    // =====================================================================
    private JPanel buildContactSection() {
        JPanel card = createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(28, 28, 28, 28)
        ));

        JPanel titleRow = buildSectionTitle("Thông tin liên lạc", "Cập nhật kênh liên hệ cá nhân khi cần", SUCCESS, createLineIcon("contact", SUCCESS));
        card.add(titleRow);
        card.add(Box.createVerticalStrut(20));

        // Phone + Email row
        JPanel row1 = new JPanel(new GridLayout(1, 2, 24, 0));
        row1.setOpaque(false);
        row1.setAlignmentX(LEFT_ALIGNMENT);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JPanel phoneGroup = new JPanel();
        phoneGroup.setLayout(new BoxLayout(phoneGroup, BoxLayout.Y_AXIS));
        phoneGroup.setOpaque(false);
        phoneGroup.add(createFieldLabel("Số điện thoại"));
        txtPhone = new JTextField(currentUser.getSoDienThoai() != null ? currentUser.getSoDienThoai() : "");
        styleTextField(txtPhone);
        phoneGroup.add(txtPhone);

        JPanel emailGroup = new JPanel();
        emailGroup.setLayout(new BoxLayout(emailGroup, BoxLayout.Y_AXIS));
        emailGroup.setOpaque(false);
        emailGroup.add(createFieldLabel("Email cá nhân"));
        txtEmail = new JTextField(currentUser.getEmail() != null ? currentUser.getEmail() : "");
        styleTextField(txtEmail);
        emailGroup.add(txtEmail);

        row1.add(phoneGroup);
        row1.add(emailGroup);
        card.add(row1);
        card.add(Box.createVerticalStrut(16));

        // Address
        JPanel addrGroup = new JPanel();
        addrGroup.setLayout(new BoxLayout(addrGroup, BoxLayout.Y_AXIS));
        addrGroup.setOpaque(false);
        addrGroup.setAlignmentX(LEFT_ALIGNMENT);
        addrGroup.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        addrGroup.add(createFieldLabel("Địa chỉ thường trú"));
        txtAddress = new JTextField(currentUser.getDiaChiThuongTru() != null ? currentUser.getDiaChiThuongTru() : "");
        styleTextField(txtAddress);
        addrGroup.add(txtAddress);
        card.add(addrGroup);
        card.add(Box.createVerticalStrut(24));

        // Separator + buttons
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(BORDER_COLOR);
        card.add(sep);
        card.add(Box.createVerticalStrut(16));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(LEFT_ALIGNMENT);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JButton btnCancel = createTextButton("Hủy thay đổi", TEXT_MUTED);
        btnCancel.addActionListener(e -> resetContactFields());
        JButton btnSave = createFilledButton("Lưu thông tin");
        btnSave.addActionListener(e -> handleSaveContact());
        btnRow.add(btnCancel);
        btnRow.add(btnSave);
        card.add(btnRow);

        return card;
    }

    private JPanel buildSectionTitle(String title, String subtitle, Color accent, Icon icon) {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        header.setAlignmentX(LEFT_ALIGNMENT);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JLabel glyph = new JLabel(icon);
        glyph.setPreferredSize(new Dimension(38, 38));
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 17));
        t.setForeground(TEXT_DARK);
        JLabel s = new JLabel(subtitle);
        s.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        s.setForeground(TEXT_MUTED);
        text.add(t);
        text.add(Box.createVerticalStrut(2));
        text.add(s);
        header.add(glyph, BorderLayout.WEST);
        header.add(text, BorderLayout.CENTER);
        return header;
    }

    // =====================================================================
    //  ACTIONS
    // =====================================================================
    private void handleChangePassword() {
        String oldPass = new String(txtOldPass.getPassword()).trim();
        String newPass = new String(txtNewPass.getPassword()).trim();
        String confirm = new String(txtConfirmPass.getPassword()).trim();

        if (oldPass.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
            showMessage("Vui lòng nhập đầy đủ thông tin.", ERROR_COLOR); return;
        }
        // Xác thực trực tiếp với DB để tránh sai lệch dữ liệu do object currentUser cũ.
        if (!daoNV.verifyPassword(currentUser.getMaNV(), oldPass)) {
            showMessage("Mật khẩu cũ không chính xác.", ERROR_COLOR); return;
        }
        if (!newPass.equals(confirm)) {
            showMessage("Mật khẩu mới và xác nhận không khớp.", ERROR_COLOR); return;
        }
        if (newPass.length() < 4) {
            showMessage("Mật khẩu mới phải có ít nhất 4 ký tự.", ERROR_COLOR); return;
        }

        if (daoNV.updatePassword(currentUser.getMaNV(), newPass)) {
            currentUser.setPassword(newPass);
            showMessage("Đổi mật khẩu thành công!", SUCCESS);
            txtOldPass.setText(""); txtNewPass.setText(""); txtConfirmPass.setText("");
        } else {
            showMessage("Lỗi khi đổi mật khẩu.", ERROR_COLOR);
        }
    }

    private void resetContactFields() {
        txtPhone.setText(currentUser.getSoDienThoai() != null ? currentUser.getSoDienThoai() : "");
        txtEmail.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");
        txtAddress.setText(currentUser.getDiaChiThuongTru() != null ? currentUser.getDiaChiThuongTru() : "");
    }

    private void handleSaveContact() {
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();
        String address = txtAddress.getText().trim();

        if (phone.isEmpty()) { showMessage("SĐT không được trống.", ERROR_COLOR); return; }
        if (!phone.matches("\\d{10,11}")) { showMessage("SĐT phải có 10–11 chữ số.", ERROR_COLOR); return; }
        if (!email.isEmpty() && !email.matches("[\\w.+\\-]+@[\\w\\-]+(\\.[\\w\\-]+)*\\.[a-zA-Z]{2,}")) {
            showMessage("Email không hợp lệ.", ERROR_COLOR); return;
        }
        if (daoNV.existsBySoDienThoai(phone, currentUser.getMaNV())) {
            showMessage("Số điện thoại đã tồn tại trong hệ thống.", ERROR_COLOR); return;
        }
        if (!email.isEmpty() && daoNV.existsByEmail(email, currentUser.getMaNV())) {
            showMessage("Email đã tồn tại trong hệ thống.", ERROR_COLOR); return;
        }

        if (daoNV.updateContactInfo(currentUser.getMaNV(), phone, email, address)) {
            currentUser.setSoDienThoai(phone);
            currentUser.setEmail(email);
            currentUser.setDiaChiThuongTru(address);
            showMessage("Cập nhật thành công!", SUCCESS);
        } else {
            showMessage("Lỗi khi cập nhật.", ERROR_COLOR);
        }
    }

    // =====================================================================
    //  UI HELPERS
    // =====================================================================
    private JPanel createCard() {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setAlignmentX(LEFT_ALIGNMENT);
        return card;
    }

    private JPanel createRoundedBgPanel(Color bg, int radius) {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        return p;
    }

    private JPanel buildInfoRow(String label, String value) {
        JPanel row = createRoundedBgPanel(FIELD_BG, 12);
        row.setLayout(new BorderLayout());
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        row.setAlignmentX(CENTER_ALIGNMENT);
        row.setBorder(new EmptyBorder(8, 16, 8, 16));
        JLabel l = new JLabel(label); l.setFont(new Font("Segoe UI", Font.BOLD, 10)); l.setForeground(TEXT_MUTED);
        JLabel v = new JLabel(value); v.setFont(new Font("Segoe UI", Font.BOLD, 12)); v.setForeground(TEXT_DARK);
        row.add(l, BorderLayout.WEST);
        row.add(v, BorderLayout.EAST);
        return row;
    }

    private JPanel wrapProfileRow(JPanel row) {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(0, 24, 0, 24));
        wrap.setAlignmentX(CENTER_ALIGNMENT);
        wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        wrap.add(row, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel buildReadOnlyField(String label, String value) {
        JPanel w = new JPanel();
        w.setLayout(new BoxLayout(w, BoxLayout.Y_AXIS));
        w.setOpaque(false);
        JLabel l = new JLabel(label); l.setFont(new Font("Segoe UI", Font.BOLD, 10)); l.setForeground(TEXT_MUTED);
        l.setAlignmentX(LEFT_ALIGNMENT);
        w.add(l); w.add(Box.createVerticalStrut(6));
        JPanel bg = createRoundedBgPanel(FIELD_BG, 12);
        bg.setLayout(new BorderLayout());
        bg.setBorder(new EmptyBorder(10, 14, 10, 14));
        bg.setAlignmentX(LEFT_ALIGNMENT);
        JLabel v = new JLabel(value); v.setFont(new Font("Segoe UI", Font.BOLD, 13)); v.setForeground(TEXT_DARK);
        bg.add(v, BorderLayout.CENTER);
        w.add(bg);
        return w;
    }

    private JLabel createFieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setForeground(TEXT_MUTED);
        l.setAlignmentX(LEFT_ALIGNMENT);
        l.setBorder(new EmptyBorder(0, 0, 6, 0));
        return l;
    }

    private JLabel addLabel(JPanel parent, String text, int style, int size, Color fg) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", style, size));
        l.setForeground(fg);
        parent.add(l);
        return l;
    }

    private void styleTextField(JTextField tf) {
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setForeground(TEXT_DARK);
        tf.setBackground(CARD_BG);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true), new EmptyBorder(10, 14, 10, 14)));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        tf.setAlignmentX(LEFT_ALIGNMENT);
        tf.setCaretColor(PRIMARY);
        // Handoff: field chỉ đổi style Notion bo nhẹ, giữ nguyên JTextField để logic save/reset không đổi.
        // Nếu cần icon trong field, bọc ngoài bằng panel mới thay vì thay component này.
    }

    private void stylePasswordField(JPasswordField pf) {
        pf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pf.setForeground(TEXT_DARK);
        pf.setBackground(CARD_BG);
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true), new EmptyBorder(10, 14, 10, 14)));
        pf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        pf.setAlignmentX(LEFT_ALIGNMENT);
        pf.setCaretColor(PRIMARY);
    }

    private JButton createTextButton(String text, Color fg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12)); b.setForeground(fg);
        b.setBorderPainted(false); b.setContentAreaFilled(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setForeground(PRIMARY); }
            @Override public void mouseExited(MouseEvent e) { b.setForeground(fg); }
        });
        return b;
    }

    private JButton createFilledButton(String text) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? PRIMARY.darker() : PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(new Font("Segoe UI", Font.BOLD, 12)); b.setForeground(AppColors.SURFACE);
        b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false); b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(8, 20, 8, 20));
        return b;
    }

    private Icon createLockIcon() {
        return new Icon() {
            @Override public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c.getForeground());
                g2.setStroke(new BasicStroke(1.5f));
                g2.fillRoundRect(x + 2, y + 7, 12, 9, 3, 3);
                g2.drawArc(x + 4, y + 1, 8, 10, 0, 180);
                g2.dispose();
            }
            @Override public int getIconWidth() { return 16; }
            @Override public int getIconHeight() { return 16; }
        };
    }

    private Icon createLineIcon(String type, Color color) {
        return new Icon() {
            @Override public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppColors.withAlpha(color, 35));
                g2.fillRoundRect(x, y, 34, 34, 12, 12);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                if ("contact".equals(type)) {
                    g2.drawRoundRect(x + 8, y + 10, 18, 14, 3, 3);
                    g2.drawLine(x + 9, y + 11, x + 17, y + 17);
                    g2.drawLine(x + 25, y + 11, x + 17, y + 17);
                } else {
                    g2.drawRoundRect(x + 8, y + 8, 18, 20, 4, 4);
                    g2.drawOval(x + 13, y + 12, 8, 8);
                    g2.drawLine(x + 12, y + 23, x + 22, y + 23);
                }
                g2.dispose();
            }
            @Override public int getIconWidth() { return 34; }
            @Override public int getIconHeight() { return 34; }
        };
    }

    private static class ProfileHeroGlyph extends JComponent {
        ProfileHeroGlyph() {
            setPreferredSize(new Dimension(210, 110));
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(AppColors.withAlpha(Color.WHITE, 70));
            g2.drawRoundRect(36, 18, 126, 78, 22, 22);
            g2.setColor(AppColors.withAlpha(Color.WHITE, 155));
            g2.fillOval(78, 32, 32, 32);
            g2.setColor(AppColors.withAlpha(Color.WHITE, 125));
            g2.fillRoundRect(58, 70, 72, 12, 12, 12);
            g2.setColor(AppColors.withAlpha(NotionTheme.YELLOW, 135));
            g2.fillOval(142, 8, 34, 34);
            g2.setColor(AppColors.withAlpha(NotionTheme.MINT, 135));
            g2.fillRoundRect(16, 62, 44, 18, 18, 18);
            g2.dispose();
        }
    }

    private static class ProfileAccentPanel extends JPanel {
        ProfileAccentPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(10, 78));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 78));
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            GradientPaint gp = new GradientPaint(0, 0, NotionTheme.ACCENT_SOFT, w, getHeight(), NotionTheme.MINT);
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, w, getHeight() + 18, 16, 16);
            g2.setColor(AppColors.withAlpha(Color.WHITE, 120));
            g2.fillOval(w - 92, -28, 82, 82);
            g2.dispose();
        }
    }

    private void showMessage(String msg, Color color) {
        JLabel l = new JLabel(msg); l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(AppColors.SURFACE); l.setHorizontalAlignment(SwingConstants.CENTER);
        l.setBorder(new EmptyBorder(10, 20, 10, 20));
        JPanel toast = createRoundedBgPanel(color, 12);
        toast.setLayout(new BorderLayout());
        toast.add(l, BorderLayout.CENTER);

        Window w = SwingUtilities.getWindowAncestor(this);
        if (w instanceof JFrame frame) {
            JLayeredPane lp = frame.getLayeredPane();
            toast.setBounds((lp.getWidth() - 320) / 2, lp.getHeight() - 80, 320, 44);
            lp.add(toast, JLayeredPane.POPUP_LAYER);
            lp.revalidate(); lp.repaint();
            Timer t = new Timer(2000, e -> { lp.remove(toast); lp.revalidate(); lp.repaint(); });
            t.setRepeats(false); t.start();
        }
    }

    private String getInitials(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] p = name.trim().split("\\s+");
        return p.length == 1 ? p[0].substring(0, 1).toUpperCase()
                : (p[0].substring(0, 1) + p[p.length - 1].substring(0, 1)).toUpperCase();
    }

    private String formatRole(String role) {
        if (role == null) return "—";
        return switch (role) {
            case "ADMIN" -> "Quản trị viên";
            case "DIEU_PHOI" -> "Điều phối";
            case "BAN_VE" -> "Nhân viên quầy vé";
            default -> role;
        };
    }

    private String resolveGaName(Ga gaLamViec) {
        String maGa = gaLamViec != null ? gaLamViec.getMaGa() : null;
        if (maGa == null || maGa.isEmpty()) return null;
        for (String[] ga : daoNV.getAllGa()) if (maGa.equals(ga[0])) return ga[1];
        return maGa;
        // Handoff: resolver accepts Ga object but still tolerates code-only Ga from DAO.
        // Risk: if DAO later hydrates tenGa, this lookup can be skipped for faster render.
    }

    @Override public String getTitle() { return "Thông tin cá nhân"; }
    @Override public JPanel getView() { return this; }
    @Override public void setOnResult(Consumer<Object> cb) { this.callback = cb; }
    @Override public void reset() { }
}

