package com.modules;

import com.dao.DAO_Ga;
import com.entity.Ga;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

/**
 * Bước 1 — Nhập thông tin hành trình.
 * Input: ga đi, ga đến, ngày đi.
 * Output: Object[]{ Ga gaDi, Ga gaDen, LocalDate ngayDi }
 */
public class BanVeStep1Module extends JPanel implements AppModule {

    private Consumer<Object> callback;

    private final DAO_Ga daoGa = new DAO_Ga();

    // UI fields
    private JComboBox<Ga> cbGaDi;
    private JComboBox<Ga> cbGaDen;
    private JSpinner      spNgayDi;

    // Design tokens
    private static final Color PRIMARY       = new Color(0x00, 0x5D, 0x90);
    private static final Color PRIMARY_LIGHT = new Color(0xE3, 0xF2, 0xFD);
    private static final Color SURFACE       = new Color(0xF8, 0xFA, 0xFC);
    private static final Color CARD_BG       = Color.WHITE;
    private static final Color ON_SURFACE    = new Color(0x1A, 0x1D, 0x21);
    private static final Color ON_SURF_VAR   = new Color(0x5F, 0x67, 0x70);
    private static final Color OUTLINE       = new Color(0xDE, 0xE3, 0xE8);

    // AppModule buttons
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

        // ---- Main content ----
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(CARD_BG);
        content.setBorder(new EmptyBorder(48, 80, 32, 80));

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
        content.add(Box.createVerticalStrut(6));
        content.add(sub);
        content.add(Box.createVerticalStrut(36));

        // ---- Form ----
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

        // Ngày đi — default = today
        SpinnerDateModel dateModel = new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH);
        spNgayDi = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(spNgayDi, "dd/MM/yyyy");
        spNgayDi.setEditor(dateEditor);
        spNgayDi.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        spNgayDi.setPreferredSize(new Dimension(320, 42));

        addRow(form, gbc, 0, "Ga đi:", cbGaDi);
        addRow(form, gbc, 1, "Ga đến:", cbGaDen);
        addRow(form, gbc, 2, "Ngày đi:", spNgayDi);

        content.add(form);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(CARD_BG);
        add(scroll, BorderLayout.CENTER);

        // ---- Buttons ----
        btnSubmit = new JButton("Tìm chuyến →");
        styleBtn(btnSubmit, true);
        btnSubmit.addActionListener(e -> execute());

        btnCancel = new JButton("Hủy");
        styleBtn(btnCancel, false);
        btnCancel.addActionListener(e -> { if (callback != null) callback.accept(null); });

        btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        btnPanel.setBackground(CARD_BG);
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, OUTLINE));
        btnPanel.add(btnCancel);
        btnPanel.add(btnSubmit);
        btnPanel.setVisible(false);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void addRow(JPanel form, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx   = 0; gbc.gridy  = row;
        gbc.weightx = 0; gbc.fill   = GridBagConstraints.NONE;
        gbc.insets  = new Insets(12, 0, 0, 24);

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(ON_SURFACE);
        lbl.setPreferredSize(new Dimension(110, 28));
        form.add(lbl, gbc);

        gbc.gridx   = 1;
        gbc.weightx = 1;
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        form.add(field, gbc);
        gbc.fill = GridBagConstraints.NONE;
    }

    private void styleCombo(JComboBox<?> cb) {
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cb.setBackground(CARD_BG);
        cb.setPreferredSize(new Dimension(320, 42));
    }

    private void styleBtn(JButton btn, boolean primary) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 28, 10, 28));
        if (primary) {
            btn.setBackground(PRIMARY);
            btn.setForeground(Color.WHITE);
            btn.setOpaque(true);
        } else {
            btn.setBackground(CARD_BG);
            btn.setForeground(ON_SURF_VAR);
            btn.setOpaque(true);
        }
    }

    // =========================================================================
    //  EXECUTE
    // =========================================================================

    private void execute() {
        Ga gaDi  = (Ga) cbGaDi.getSelectedItem();
        Ga gaDen = (Ga) cbGaDen.getSelectedItem();

        if (gaDi == null || gaDen == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ga đi và ga đến.");
            return;
        }
        if (gaDi.getMaGa().equals(gaDen.getMaGa())) {
            JOptionPane.showMessageDialog(this, "Ga đi và ga đến không được trùng nhau.");
            return;
        }

        Date     dateVal = (Date) spNgayDi.getValue();
        LocalDate ngayDi = dateVal.toInstant()
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate();

        if (ngayDi.isBefore(LocalDate.now())) {
            JOptionPane.showMessageDialog(this, "Ngày đi phải từ hôm nay trở đi.");
            return;
        }

        if (callback != null) callback.accept(new Object[]{ gaDi, gaDen, ngayDi });
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
        spNgayDi.setValue(new Date());
    }
}
