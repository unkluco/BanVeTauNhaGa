package com.modules;

import com.dao.DAO_Ga;
import com.entity.Ga;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

/**
 * Bước 1 — Nhập thông tin hành trình.
 * Output: Object[]{ Ga gaDi, Ga gaDen, LocalDate ngayDiFrom, LocalDate ngayDiTo }
 *   Chế độ "Ngày cụ thể" → ngayDiFrom == ngayDiTo
 *   Chế độ "Khoảng ngày"  → ngayDiFrom &lt; ngayDiTo
 */
public class BanVeStep1Module extends JPanel implements AppModule {

    private Consumer<Object> callback;

    private final DAO_Ga daoGa = new DAO_Ga();

    // UI fields
    private JComboBox<Ga>   cbGaDi;
    private JComboBox<Ga>   cbGaDen;
    private DatePickerField dpExact;        // chế độ ngày cụ thể
    private DatePickerField dpRangeFrom;   // chế độ khoảng — từ
    private DatePickerField dpRangeTo;     // chế độ khoảng — đến

    // Date mode
    private static final int MODE_EXACT = 0;
    private static final int MODE_RANGE = 1;
    private int dateMode = MODE_EXACT;

    // Mode cards (for border styling)
    private JPanel cardExact;
    private JPanel cardRange;
    private JPanel dotExact;   // radio dot indicators
    private JPanel dotRange;
    private JLabel lblModeExact; // header labels (for dynamic font/color update)
    private JLabel lblModeRange;

    // Design tokens
    private static final Color PRIMARY       = NotionTheme.ACCENT;
    private static final Color PRIMARY_LIGHT = NotionTheme.ACCENT_SOFT;
    private static final Color SURFACE       = NotionTheme.PAGE;
    private static final Color CARD_BG       = NotionTheme.CARD;
    private static final Color ON_SURFACE    = NotionTheme.TEXT;
    private static final Color ON_SURF_VAR   = NotionTheme.TEXT_MUTED;
    private static final Color OUTLINE       = NotionTheme.BORDER;

    private JButton btnSubmit;
    private JButton btnCancel;
    private JPanel  btnPanel;

    // =========================================================================
    //  CONSTRUCTOR
    // =========================================================================

    public BanVeStep1Module() {
        setLayout(new BorderLayout());
        setBackground(CARD_BG);
        buildUI();
    }

    // =========================================================================
    //  BUILD UI
    // =========================================================================

    private void buildUI() {
        List<Ga> gaList = daoGa.getAll();

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(CARD_BG);
        content.setBorder(new EmptyBorder(42, 72, 30, 72));

        // Title
        JLabel title = new JLabel("Thông tin hành trình");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(ON_SURFACE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Chọn điểm đi, điểm đến và ngày khởi hành");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(ON_SURF_VAR);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(title);
        content.add(Box.createVerticalStrut(4));
        content.add(sub);
        content.add(Box.createVerticalStrut(30));

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(CARD_BG);
        form.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;

        // Ga đi
        cbGaDi = new JComboBox<>();
        cbGaDi.setRenderer(new GaRenderer());
        for (Ga ga : gaList) cbGaDi.addItem(ga);
        styleCombo(cbGaDi);

        // Ga đến
        cbGaDen = new JComboBox<>();
        cbGaDen.setRenderer(new GaRenderer());
        for (Ga ga : gaList) cbGaDen.addItem(ga);
        styleCombo(cbGaDen);
        if (gaList.size() > 1) cbGaDen.setSelectedIndex(1);

        // Ngày đi — two-mode selector
        JPanel dateModePanel = buildDateModePanel();

        addFormRow(form, gbc, 0, "Ga đi:",    cbGaDi);
        addFormRow(form, gbc, 1, "Ga đến:",   cbGaDen);
        addFormRowWide(form, gbc, 2, "Ngày đi:", dateModePanel);

        content.add(form);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(CARD_BG);
        add(scroll, BorderLayout.CENTER);

        // Buttons
        btnSubmit = new JButton("Tìm chuyến →");
        styleBtn(btnSubmit, true);
        btnSubmit.addActionListener(e -> execute());

        btnCancel = new JButton("Hủy");
        styleBtn(btnCancel, false);
        btnCancel.addActionListener(e -> { if (callback != null) callback.accept(null); });

        btnPanel = BookingStepUi.createFooter();
        btnPanel.add(btnCancel);
        btnPanel.add(btnSubmit);
        btnPanel.setVisible(false);
        add(btnPanel, BorderLayout.SOUTH);
    }

    // =========================================================================
    //  DATE MODE PANEL
    // =========================================================================

    private JPanel buildDateModePanel() {
        JPanel outer = new JPanel(new GridLayout(1, 2, 10, 0));
        outer.setBackground(CARD_BG);
        outer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        // ── Card: Ngày cụ thể ──────────────────────────────────────────────
        cardExact = new JPanel(new BorderLayout(0, 8));
        cardExact.setBackground(CARD_BG);
        cardExact.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY, 1, true),
            new EmptyBorder(12, 14, 12, 14)
        ));
        cardExact.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cardExact.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { selectMode(MODE_EXACT); }
        });

        JPanel headerExact = buildModeHeader("Ngày cụ thể", MODE_EXACT);
        addModeListener(headerExact, MODE_EXACT);
        dpExact = new DatePickerField(LocalDate.now(), LocalDate.now());
        dpExact.setPreferredSize(new Dimension(200, 38));

        cardExact.add(headerExact, BorderLayout.NORTH);
        cardExact.add(dpExact,     BorderLayout.CENTER);

        // ── Card: Khoảng ngày ─────────────────────────────────────────────
        cardRange = new JPanel(new BorderLayout(0, 8));
        cardRange.setBackground(CARD_BG);
        cardRange.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(OUTLINE, 1, true),
            new EmptyBorder(12, 14, 12, 14)
        ));
        cardRange.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cardRange.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { selectMode(MODE_RANGE); }
        });

        JPanel headerRange = buildModeHeader("Khoảng ngày", MODE_RANGE);
        JPanel rangeInputs = buildRangeInputs();

        // Thêm listener cho header + children vì Swing không bubble events lên parent
        addModeListener(headerRange, MODE_RANGE);

        cardRange.add(headerRange, BorderLayout.NORTH);
        cardRange.add(rangeInputs, BorderLayout.CENTER);

        outer.add(cardExact);
        outer.add(cardRange);
        return outer;
    }

    /** Thêm selectMode listener cho panel và tất cả child components */
    private void addModeListener(JPanel header, int mode) {
        MouseAdapter ma = new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { selectMode(mode); }
        };
        header.addMouseListener(ma);
        header.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        for (Component c : header.getComponents()) {
            c.addMouseListener(ma);
            c.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }

    private JPanel buildModeHeader(String title, int targetMode) {
        JPanel hdr = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        hdr.setBackground(CARD_BG);
        hdr.setOpaque(false);

        // Radio dot — đọc dateMode động mỗi lần repaint
        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                boolean sel = (dateMode == targetMode);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(sel ? PRIMARY : OUTLINE);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(1, 1, getWidth()-3, getHeight()-3);
                if (sel) {
                    g2.setColor(PRIMARY);
                    g2.fillOval(4, 4, getWidth()-9, getHeight()-9);
                }
                g2.dispose();
            }
        };
        dot.setPreferredSize(new Dimension(16, 16));
        dot.setOpaque(false);

        if (targetMode == MODE_EXACT) dotExact = dot;
        else dotRange = dot;

        boolean initialSel = (dateMode == targetMode);
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", initialSel ? Font.BOLD : Font.PLAIN, 13));
        lbl.setForeground(initialSel ? PRIMARY : ON_SURF_VAR);

        if (targetMode == MODE_EXACT) lblModeExact = lbl;
        else lblModeRange = lbl;

        hdr.add(dot);
        hdr.add(lbl);
        return hdr;
    }

    private JPanel buildRangeInputs() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        row.setBackground(CARD_BG);
        row.setOpaque(false);

        dpRangeFrom = new DatePickerField(LocalDate.now(), LocalDate.now());
        dpRangeFrom.setPreferredSize(new Dimension(145, 38));

        dpRangeTo = new DatePickerField(LocalDate.now().plusDays(7), LocalDate.now());
        dpRangeTo.setPreferredSize(new Dimension(145, 38));

        // Keep to-date >= from-date
        dpRangeFrom.addPropertyChangeListener("value", e -> {
            LocalDate from = dpRangeFrom.getValue();
            LocalDate to   = dpRangeTo.getValue();
            if (from != null && to != null && to.isBefore(from)) {
                dpRangeTo.setValue(from);
            }
            dpRangeTo.setMinDate(from);
        });

        JLabel arrow = new JLabel("→");
        arrow.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        arrow.setForeground(ON_SURF_VAR);

        row.add(dpRangeFrom);
        row.add(arrow);
        row.add(dpRangeTo);
        return row;
    }

    private void selectMode(int mode) {
        this.dateMode = mode;
        boolean exact = (mode == MODE_EXACT);

        // Update card borders
        cardExact.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(exact ? PRIMARY : OUTLINE, 1, true),
            new EmptyBorder(12, 14, 12, 14)
        ));
        cardRange.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(!exact ? PRIMARY : OUTLINE, 1, true),
            new EmptyBorder(12, 14, 12, 14)
        ));

        // Repaint radio dots (chúng đọc dateMode động nên repaint là đủ)
        if (dotExact != null) dotExact.repaint();
        if (dotRange != null) dotRange.repaint();

        // Update header labels
        if (lblModeExact != null) {
            lblModeExact.setFont(new Font("Segoe UI", exact ? Font.BOLD : Font.PLAIN, 13));
            lblModeExact.setForeground(exact ? PRIMARY : ON_SURF_VAR);
        }
        if (lblModeRange != null) {
            lblModeRange.setFont(new Font("Segoe UI", !exact ? Font.BOLD : Font.PLAIN, 13));
            lblModeRange.setForeground(!exact ? PRIMARY : ON_SURF_VAR);
        }
    }

    // =========================================================================
    //  FORM HELPERS
    // =========================================================================

    private void addFormRow(JPanel form, GridBagConstraints gbc, int row,
                            String label, JComponent field) {
        gbc.gridx   = 0; gbc.gridy  = row;
        gbc.weightx = 0; gbc.fill   = GridBagConstraints.NONE;
        gbc.insets  = new Insets(12, 0, 0, 24);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(ON_SURFACE);
        lbl.setPreferredSize(new Dimension(90, 28));
        form.add(lbl, gbc);

        gbc.gridx   = 1;
        gbc.weightx = 1;
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        form.add(field, gbc);
        gbc.fill = GridBagConstraints.NONE;
    }

    private void addFormRowWide(JPanel form, GridBagConstraints gbc, int row,
                                String label, JComponent field) {
        gbc.gridx   = 0; gbc.gridy  = row;
        gbc.weightx = 0; gbc.fill   = GridBagConstraints.NONE;
        gbc.insets  = new Insets(16, 0, 0, 24);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(ON_SURFACE);
        lbl.setPreferredSize(new Dimension(90, 28));
        lbl.setVerticalAlignment(SwingConstants.TOP);
        form.add(lbl, gbc);

        gbc.gridx   = 1;
        gbc.weightx = 1;
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.insets  = new Insets(16, 0, 0, 0);
        form.add(field, gbc);
        gbc.fill   = GridBagConstraints.NONE;
        gbc.insets = new Insets(12, 0, 0, 24);
    }

    private void styleCombo(JComboBox<?> cb) {
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cb.setBackground(CARD_BG);
        cb.setBorder(BorderFactory.createLineBorder(OUTLINE, 1, true));
        cb.setPreferredSize(new Dimension(320, 42));
    }

    private void styleBtn(JButton btn, boolean primary) {
        BookingStepUi.styleActionButton(btn, primary);
    }

    // =========================================================================
    //  EXECUTE
    // =========================================================================

    private void execute() {
        Ga gaDi  = (Ga) cbGaDi.getSelectedItem();
        Ga gaDen = (Ga) cbGaDen.getSelectedItem();

        if (gaDi == null || gaDen == null) {
            NotionMessageDialog.showMessageDialog(this, "Vui lòng chọn ga đi và ga đến.");
            return;
        }
        if (gaDi.getMaGa().equals(gaDen.getMaGa())) {
            NotionMessageDialog.showMessageDialog(this, "Ga đi và ga đến không được trùng nhau.");
            return;
        }

        LocalDate from, to;

        if (dateMode == MODE_EXACT) {
            from = dpExact.getValue();
            if (from == null) {
                NotionMessageDialog.showMessageDialog(this, "Vui lòng chọn ngày đi.");
                return;
            }
            to = from;
        } else {
            from = dpRangeFrom.getValue();
            to   = dpRangeTo.getValue();
            if (from == null || to == null) {
                NotionMessageDialog.showMessageDialog(this, "Vui lòng chọn khoảng ngày.");
                return;
            }
            if (to.isBefore(from)) {
                NotionMessageDialog.showMessageDialog(this, "Ngày kết thúc phải sau ngày bắt đầu.");
                return;
            }
        }

        if (from.isBefore(LocalDate.now())) {
            NotionMessageDialog.showMessageDialog(this, "Ngày đi phải từ hôm nay trở đi.");
            return;
        }

        if (callback != null) callback.accept(new Object[]{ gaDi, gaDen, from, to });
    }

    // =========================================================================
    //  RENDERER
    // =========================================================================

    static class GaRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Ga ga) setText(ga.getTenGa());
            return this;
        }
    }

    // =========================================================================
    //  AppModule
    // =========================================================================

    @Override public String getTitle() { return "Bước 1 – Thông tin"; }
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
        if (cbGaDi.getItemCount()  > 0) cbGaDi.setSelectedIndex(0);
        if (cbGaDen.getItemCount() > 1) cbGaDen.setSelectedIndex(1);
        dpExact.setValue(LocalDate.now());
        dpRangeFrom.setValue(LocalDate.now());
        dpRangeTo.setValue(LocalDate.now().plusDays(7));
        selectMode(MODE_EXACT);
    }
}
