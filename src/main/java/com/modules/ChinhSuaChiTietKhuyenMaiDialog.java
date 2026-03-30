package com.modules;

import com.dao.DAO_ChiTietKhuyenMai;
import com.dao.DAO_Tuyen;
import com.entity.ChiTietKhuyenMai;
import com.entity.KhuyenMai;
import com.entity.Tuyen;
import com.enums.LoaiGhe;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * Dialog thêm/sửa ChiTietKhuyenMai.
 * - phanTramGiam: người dùng nhập 0‒100, lưu vào DB dưới dạng 0.0‒1.0.
 */
public class ChinhSuaChiTietKhuyenMaiDialog extends JDialog {

    private static final Color PRIMARY       = new Color(0x00, 0x5D, 0x90);
    private static final Color PRIMARY_FIXED = new Color(0xCD, 0xE5, 0xFF);
    private static final Color CARD_BG       = Color.WHITE;
    private static final Color ON_SURFACE    = new Color(0x1A, 0x1D, 0x21);
    private static final Color ON_SURF_VAR   = new Color(0x5F, 0x67, 0x70);
    private static final Color OUTLINE       = new Color(0xDE, 0xE3, 0xE8);
    private static final Color READONLY_BG   = new Color(0xE8, 0xED, 0xF2);
    private static final Color ERROR         = new Color(0xBA, 0x1A, 0x1A);
    private static final Color HEADER_BG     = new Color(0xF1, 0xF5, 0xF9);
    private static final Color FOOTER_BG     = new Color(0xF1, 0xF5, 0xF9);

    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font FONT_DESC  = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font FONT_INPUT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_MONO  = new Font("Consolas", Font.BOLD, 13);
    private static final Font FONT_BTN   = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_ERR   = new Font("Segoe UI", Font.PLAIN, 11);

    private final KhuyenMai      khuyenMai;
    private final ChiTietKhuyenMai ctkm;   // null = ADD mode
    private final Runnable         onSaved;
    private final boolean          isAddMode;
    private List<Tuyen>            tuyenList;

    private JTextField                txtMaChiTiet;
    private JTextField                txtTenChiTiet;
    private SearchableComboBox<Tuyen> searchTuyen;
    private JComboBox<Object>         cboLoaiGhe;
    private JTextField                txtPhanTram;

    private JLabel lblErrPhanTram;

    public ChinhSuaChiTietKhuyenMaiDialog(Window owner, KhuyenMai khuyenMai,
                                           ChiTietKhuyenMai ctkm, Runnable onSaved) {
        super(owner, ctkm == null ? "Thêm chi tiết khuyến mãi" : "Chỉnh sửa chi tiết khuyến mãi",
                ModalityType.APPLICATION_MODAL);
        this.khuyenMai = khuyenMai;
        this.ctkm      = ctkm;
        this.onSaved   = onSaved;
        this.isAddMode = (ctkm == null);
        setUndecorated(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        loadTuyenList();
        initUI();
        if (!isAddMode) populateFields();
        pack();
        setMinimumSize(new Dimension(560, getPreferredSize().height));
        setLocationRelativeTo(owner);
    }

    private void loadTuyenList() {
        tuyenList = new DAO_Tuyen().getAll();
    }

    private void initUI() {
        setBackground(new Color(0, 0, 0, 0));
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CARD_BG);
        root.setBorder(BorderFactory.createLineBorder(OUTLINE, 1));
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildForm(),   BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);
        setContentPane(ThemNhanVienDialog.buildShadowWrapper(root));
    }

    private void populateFields() {
        txtTenChiTiet.setText(ctkm.getTenChiTiet() != null ? ctkm.getTenChiTiet() : "");
        if (ctkm.getTuyen() != null) {
            String maTuyen = ctkm.getTuyen().getMaTuyen();
            for (Tuyen t : tuyenList) {
                if (t.getMaTuyen().equals(maTuyen)) { searchTuyen.selectItem(t); break; }
            }
        }
        // cboLoaiGhe: index 0 = null (Tất cả), 1..3 = LoaiGhe values
        if (ctkm.getLoaiGhe() == null) {
            cboLoaiGhe.setSelectedIndex(0);
        } else {
            cboLoaiGhe.setSelectedItem(ctkm.getLoaiGhe());
        }
        double pct = ctkm.getPhanTramGiam() * 100.0;
        txtPhanTram.setText(String.format("%.2f", pct));
        txtPhanTram.setForeground(ON_SURFACE);
    }

    // ── Header ──────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_BG);
        header.setBorder(new EmptyBorder(20, 28, 20, 28));

        JPanel iconTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        iconTitle.setOpaque(false);

        JPanel icon = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(PRIMARY_FIXED);
                g2.fillRoundRect(0, 0, 40, 40, 10, 10);
                g2.setColor(PRIMARY);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
                FontMetrics fm = g2.getFontMetrics();
                String s = isAddMode ? "+" : "\u270E";
                g2.drawString(s, (40 - fm.stringWidth(s)) / 2, 27);
                g2.dispose();
            }
        };
        icon.setOpaque(false);
        icon.setPreferredSize(new Dimension(40, 40));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel lblTitle = new JLabel(isAddMode ? "Thêm chi tiết khuyến mãi" : "Chỉnh sửa chi tiết khuyến mãi");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(ON_SURFACE);

        JLabel lblDesc = new JLabel("Khuyến mãi: " + khuyenMai.getMaKhuyenMai()
                + " — " + (khuyenMai.getTenKhuyenMai() != null ? khuyenMai.getTenKhuyenMai() : ""));
        lblDesc.setFont(FONT_DESC);
        lblDesc.setForeground(ON_SURF_VAR);

        textPanel.add(lblTitle);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(lblDesc);

        iconTitle.add(icon);
        iconTitle.add(textPanel);
        header.add(iconTitle, BorderLayout.CENTER);
        return header;
    }

    // ── Form ────────────────────────────────────────────────────────────

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(CARD_BG);
        form.setBorder(new EmptyBorder(20, 28, 16, 28));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.anchor  = GridBagConstraints.WEST;
        gbc.weightx = 1.0;

        // Row 0–1: Mã chi tiết (readonly)
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 4, 0);
        form.add(makeLabel("Mã chi tiết"), gbc);

        String maCT = isAddMode ? generateMaChiTiet() : ctkm.getMaChiTietKM();
        txtMaChiTiet = new JTextField(maCT);
        txtMaChiTiet.setFont(FONT_MONO);
        txtMaChiTiet.setEditable(false);
        txtMaChiTiet.setBackground(READONLY_BG);
        txtMaChiTiet.setForeground(ON_SURF_VAR);
        txtMaChiTiet.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1), new EmptyBorder(9, 12, 9, 12)));
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 14, 0);
        form.add(txtMaChiTiet, gbc);

        // Row 2–3: Tên chi tiết (full width)
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 4, 0);
        form.add(makeLabel("Tên chi tiết (tùy chọn)"), gbc);

        txtTenChiTiet = new JTextField();
        txtTenChiTiet.setFont(FONT_INPUT);
        txtTenChiTiet.setForeground(ON_SURFACE);
        txtTenChiTiet.setPreferredSize(new Dimension(500, 42));
        txtTenChiTiet.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1), new EmptyBorder(9, 12, 9, 12)));
        txtTenChiTiet.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                txtTenChiTiet.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(PRIMARY, 2), new EmptyBorder(8, 11, 8, 11)));
            }
            @Override public void focusLost(FocusEvent e) {
                txtTenChiTiet.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(OUTLINE, 1), new EmptyBorder(9, 12, 9, 12)));
            }
        });
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 14, 0);
        form.add(txtTenChiTiet, gbc);

        // Row 4–5: Tuyến (optional — null = tất cả tuyến)
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 4, 0);
        form.add(makeLabel("Tuyến (bỏ trống = áp dụng tất cả tuyến)"), gbc);

        searchTuyen = new SearchableComboBox<>(
                t -> {
                    String gaDi  = t.getGaDi()  != null ? t.getGaDi().getTenGa()  : "?";
                    String gaDen = t.getGaDen() != null ? t.getGaDen().getTenGa() : "?";
                    return gaDi + " → " + gaDen + " (" + t.getMaTuyen() + ")";
                },
                (t, q) -> {
                    String gaDi  = t.getGaDi()  != null ? t.getGaDi().getTenGa().toLowerCase()  : "";
                    String gaDen = t.getGaDen() != null ? t.getGaDen().getTenGa().toLowerCase() : "";
                    return gaDi.contains(q) || gaDen.contains(q) || t.getMaTuyen().toLowerCase().contains(q);
                });
        searchTuyen.setPlaceholder("Tìm tuyến (để trống = tất cả tuyến)...");
        searchTuyen.setItems(tuyenList);
        searchTuyen.setPreferredSize(new Dimension(500, 42));

        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 14, 0);
        form.add(searchTuyen, gbc);

        // Row 6: Loại ghế | % Giảm labels
        gbc.gridwidth = 1;
        gbc.gridy = 6; gbc.gridx = 0;
        gbc.insets = new Insets(0, 0, 4, 8);
        form.add(makeLabel("Loại ghế"), gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 0, 4, 0);
        form.add(makeLabel("% Giảm (0–100)"), gbc);

        // Row 7: Loại ghế combo | % Giảm field
        // null item = "Tất cả loại ghế"
        javax.swing.DefaultComboBoxModel<Object> lgModel = new javax.swing.DefaultComboBoxModel<>();
        lgModel.addElement(null); // index 0 = tất cả
        for (LoaiGhe lg : LoaiGhe.values()) lgModel.addElement(lg);
        cboLoaiGhe = new JComboBox<>(lgModel);
        cboLoaiGhe.setFont(FONT_INPUT);
        cboLoaiGhe.setBackground(CARD_BG);
        cboLoaiGhe.setPreferredSize(new Dimension(200, 42));
        cboLoaiGhe.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(
                    javax.swing.JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(value == null ? "Tất cả loại ghế" : value.toString());
                return this;
            }
        });

        gbc.gridy = 7; gbc.gridx = 0;
        gbc.insets = new Insets(0, 0, 2, 8);
        form.add(cboLoaiGhe, gbc);

        txtPhanTram = new JTextField("0");
        txtPhanTram.setFont(FONT_INPUT);
        txtPhanTram.setForeground(ON_SURFACE);
        txtPhanTram.setPreferredSize(new Dimension(200, 42));
        txtPhanTram.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1), new EmptyBorder(9, 12, 9, 12)));
        txtPhanTram.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                txtPhanTram.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(PRIMARY, 2), new EmptyBorder(8, 11, 8, 11)));
            }
            @Override public void focusLost(FocusEvent e) {
                txtPhanTram.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(OUTLINE, 1), new EmptyBorder(9, 12, 9, 12)));
            }
        });

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 0, 2, 0);
        form.add(txtPhanTram, gbc);

        // Row 8: error for % giảm
        lblErrPhanTram = makeErrLabel();
        gbc.gridy = 8; gbc.gridx = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        form.add(lblErrPhanTram, gbc);

        return form;
    }

    // ── Footer ───────────────────────────────────────────────────────────

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setBackground(FOOTER_BG);
        footer.setBorder(new EmptyBorder(16, 24, 20, 24));

        JButton btnCancel = makeButton("Hủy bỏ", false);
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = makeButton(isAddMode ? "Thêm chi tiết" : "Cập nhật", true);
        btnSave.addActionListener(e -> doSave());

        footer.add(btnCancel);
        footer.add(btnSave);
        return footer;
    }

    // ── Save ─────────────────────────────────────────────────────────────

    private void doSave() {
        lblErrPhanTram.setText("");

        String pctStr = txtPhanTram.getText().trim();
        double pct;
        try {
            pct = Double.parseDouble(pctStr);
        } catch (NumberFormatException ex) {
            lblErrPhanTram.setText("% giảm phải là số");
            return;
        }
        if (pct <= 0 || pct > 100) {
            lblErrPhanTram.setText("% giảm phải từ 0 đến 100 (không bao gồm 0)");
            return;
        }

        Tuyen tuyen   = searchTuyen.getSelectedItem(); // null = tất cả tuyến
        Object lgSel  = cboLoaiGhe.getSelectedItem();
        LoaiGhe loaiGhe = (lgSel instanceof LoaiGhe lg) ? lg : null; // null = tất cả loại ghế
        double phanTramGiam = pct / 100.0;
        String tenChiTiet = txtTenChiTiet.getText().trim();
        if (tenChiTiet.isEmpty()) tenChiTiet = null;

        String maCT = txtMaChiTiet.getText().trim();
        ChiTietKhuyenMai record = new ChiTietKhuyenMai(
                maCT, tenChiTiet, khuyenMai, tuyen, loaiGhe, phanTramGiam);

        boolean ok = isAddMode
                ? new DAO_ChiTietKhuyenMai().insert(record)
                : new DAO_ChiTietKhuyenMai().update(record);

        if (ok) {
            if (onSaved != null) onSaved.run();
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Có lỗi khi lưu. Vui lòng kiểm tra dữ liệu.",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private String generateMaChiTiet() {
        long suffix = System.currentTimeMillis() % 100000L;
        return "CTKM-" + String.format("%05d", suffix);
    }

    private JLabel makeLabel(String text) {
        JLabel lbl = new JLabel(text.toUpperCase());
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(ON_SURF_VAR);
        return lbl;
    }

    private JLabel makeErrLabel() {
        JLabel lbl = new JLabel(" ");
        lbl.setFont(FONT_ERR);
        lbl.setForeground(ERROR);
        return lbl;
    }

    private JButton makeButton(String text, boolean primary) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (primary) {
                    g2.setColor(getModel().isRollover() ? new Color(0x00, 0x4A, 0x73) : PRIMARY);
                } else {
                    g2.setColor(getModel().isRollover() ? OUTLINE.darker() : OUTLINE);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BTN);
        btn.setForeground(primary ? Color.WHITE : ON_SURFACE);
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(primary ? 160 : 100, 40));
        return btn;
    }
}
