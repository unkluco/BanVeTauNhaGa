package com.modules;

import com.connectDB.ConnectDB;
import com.dao.DAO_ChiTietDoanTau;
import com.dao.DAO_DauMay;
import com.dao.DAO_DoanTau;
import com.dao.DAO_ToaTau;
import com.entity.ChiTietDoanTau;
import com.entity.DauMay;
import com.entity.DoanTau;
import com.entity.ToaTau;
import com.enums.LoaiGhe;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ChinhSuaDoanTauModule extends JPanel implements AppModule {

    private Consumer<Object> callback;
    private final DoanTau doanTau;
    private final boolean isEditMode;

    // Design tokens
    private static final Color PRIMARY       = new Color(13, 110, 253);
    private static final Color PRIMARY_HOVER = new Color(11, 94, 215);
    private static final Color SURFACE       = new Color(248, 249, 250);
    private static final Color CARD_BG       = Color.WHITE;
    private static final Color TEXT_MAIN     = new Color(33, 37, 41);
    private static final Color TEXT_MUTED    = new Color(108, 117, 125);
    private static final Color OUTLINE       = new Color(222, 226, 230);
    private static final Color READONLY_BG   = new Color(233, 236, 239);
    private static final Color ERROR         = new Color(220, 53, 69);
    
    // Badge Colors
    private static final Color C_CUNG   = new Color(0xFF, 0x8A, 0x65); 
    private static final Color C_MEM    = new Color(0x64, 0xB5, 0xF6); 
    private static final Color C_GIUONG = new Color(0x81, 0xC7, 0x84);

    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 26);
    private static final Font FONT_DESC  = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_INPUT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_MONO  = new Font("Consolas", Font.BOLD, 14);
    private static final Font FONT_BTN   = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_ERR   = new Font("Segoe UI", Font.PLAIN, 12);

    // Form fields
    private JTextField        txtMaDoanTau;
    private JTextField        txtTenDoanTau;
    private JComboBox<DauMay> cboDauMay;
    private JLabel            lblErrTen;
    private JLabel            lblErrDauMay;

    // Actions
    private JButton btnSubmit;
    private JButton btnCancel;
    private JPanel  btnPanel;

    // Visualizer State
    private JPanel visualContainer;
    private List<ToaTau> currentWagons = new ArrayList<>();
    private List<ToaTau> allToaTau     = new ArrayList<>();

    public ChinhSuaDoanTauModule(DoanTau doanTau) {
        this.doanTau    = doanTau;
        this.isEditMode = (doanTau != null);
        setLayout(new BorderLayout());
        setBackground(SURFACE);

        btnSubmit = new JButton();
        btnCancel = new JButton();
        btnPanel  = new JPanel();
        btnPanel.setVisible(false);
        add(btnPanel, BorderLayout.SOUTH);

        buildUI();
        loadData();
    }

    private void buildUI() {
        add(buildHeader(),  BorderLayout.NORTH);
        
        JPanel body = new JPanel(new BorderLayout(20, 0));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(10, 36, 20, 36));
        
        body.add(buildLeftForm(), BorderLayout.WEST);
        body.add(buildVisualizerPanel(), BorderLayout.CENTER);
        
        add(body, BorderLayout.CENTER);

        // Action buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        btnRow.setOpaque(false);
        btnRow.setBorder(new EmptyBorder(0, 36, 24, 36));

        JButton btnHuy = createOutlineButton("Hủy bỏ");
        btnHuy.addActionListener(e -> { if (callback != null) callback.accept(null); });

        JButton btnLuu = createPrimaryButton(isEditMode ? "Lưu thay đổi" : "Xuất bản đội tàu");
        btnLuu.addActionListener(e -> doSave());

        btnRow.add(btnHuy);
        btnRow.add(btnLuu);
        add(btnRow, BorderLayout.SOUTH);
    }

    // ── Header ────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setBackground(SURFACE);
        header.setBorder(new EmptyBorder(24, 36, 16, 36));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        String titleText = isEditMode ? "Chỉnh Sửa Đội Tàu" : "Thiết Lập Đội Tàu Mới";
        JLabel lblTitle  = new JLabel(titleText);
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(TEXT_MAIN);

        String descText = isEditMode
                ? "Cập nhật, thay đổi đầu kéo hoặc cấu trúc các toa nối."
                : "Thiết lập cấu hình tàu hỏa mới bằng cách minh họa phân đoạn trực quan.";
        JLabel lblDesc = new JLabel(descText);
        lblDesc.setFont(FONT_DESC);
        lblDesc.setForeground(TEXT_MUTED);

        titlePanel.add(lblTitle);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(lblDesc);

        header.add(titlePanel, BorderLayout.CENTER);
        return header;
    }

    // ── Left Form (Basic info) ──────────────────────────────────────────
    private JPanel buildLeftForm() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(380, 0));
        card.setBorder(new EmptyBorder(10, 0, 10, 0));

        // Background painter
        JPanel bg = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                g2.setColor(OUTLINE);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 24, 24);
                g2.dispose();
            }
        };
        bg.setOpaque(false);
        bg.setBorder(new EmptyBorder(30, 24, 30, 24));
        
        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);

        JLabel lblSecTitle = new JLabel("Thông Tin Cơ Bản");
        lblSecTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblSecTitle.setForeground(TEXT_MAIN);
        lblSecTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.add(lblSecTitle);
        inner.add(Box.createVerticalStrut(24));

        // Ma doan tau (read-only)
        String maValue = isEditMode ? doanTau.getMaDoanTau() : generateMaDoanTau();
        txtMaDoanTau = new JTextField(maValue);
        txtMaDoanTau.setFont(FONT_MONO);
        txtMaDoanTau.setForeground(TEXT_MAIN);
        txtMaDoanTau.setEditable(false);
        txtMaDoanTau.setBackground(READONLY_BG);
        styleField(txtMaDoanTau);

        inner.add(buildFieldGroup("Mã đoàn tàu", txtMaDoanTau, "Hệ thống tự động khởi tạo", false, null));
        inner.add(Box.createVerticalStrut(18));

        // Ten doan tau
        txtTenDoanTau = createInputField("Nhập tên, VD: Tàu SE1");
        if (isEditMode && doanTau.getTenDoanTau() != null) {
            txtTenDoanTau.setText(doanTau.getTenDoanTau());
            txtTenDoanTau.setForeground(TEXT_MAIN);
        }
        lblErrTen = createErrorLabel();
        inner.add(buildFieldGroup("Tên đoàn tàu", txtTenDoanTau, null, true, lblErrTen));
        inner.add(Box.createVerticalStrut(18));

        // Dau may
        cboDauMay = new JComboBox<>();
        cboDauMay.setFont(FONT_INPUT);
        cboDauMay.setEditable(false);
        cboDauMay.setBackground(Color.WHITE);
        cboDauMay.setPreferredSize(new Dimension(0, 42));
        Color bColor = new Color(180, 185, 190);
        cboDauMay.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bColor, 1, true),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        cboDauMay.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean sel, boolean foc) {
                super.getListCellRendererComponent(list, value, index, sel, foc);
                if (value instanceof DauMay dm) setText(dm.getMaDauMay() + " - " + dm.getTenDauMay());
                setBorder(new EmptyBorder(4, 8, 4, 8));
                return this;
            }
        });
        cboDauMay.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) updateVisualizer();
        });
        lblErrDauMay = createErrorLabel();
        inner.add(buildFieldGroup("Lắp Đầu Kéo", cboDauMay, "Đầu máy chi phối sức kéo của cả chuyến tàu", true, lblErrDauMay));
        
        inner.add(Box.createVerticalGlue()); // Push to top
        bg.add(inner, BorderLayout.CENTER);
        card.add(bg);
        return card;
    }

    // ── Visualizer (Right Pane) ─────────────────────────────────────────
    private JPanel buildVisualizerPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(10, 0, 10, 0));

        JPanel bg = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                g2.setColor(OUTLINE);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 24, 24);
                g2.dispose();
            }
        };
        bg.setOpaque(false);
        bg.setBorder(new EmptyBorder(30, 24, 30, 24));
        
        // Title area
        JPanel tPanel = new JPanel(new BorderLayout());
        tPanel.setOpaque(false);
        JLabel lblSec = new JLabel("Minh Hoạ Tuyến Toa Tàu");
        lblSec.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblSec.setForeground(TEXT_MAIN);
        tPanel.add(lblSec, BorderLayout.WEST);
        
        JButton btnClear = createOutlineButton("Xóa toàn bộ");
        btnClear.setPreferredSize(new Dimension(120, 34));
        btnClear.addActionListener(e -> {
            if(JOptionPane.showConfirmDialog(this,"Xác nhận tháo tất cả toa tàu?","Cảnh báo", JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION){
                currentWagons.clear();
                updateVisualizer();
            }
        });
        tPanel.add(btnClear, BorderLayout.EAST);
        
        bg.add(tPanel, BorderLayout.NORTH);

        // Visual Scroll
        visualContainer = new JPanel();
        visualContainer.setLayout(new BoxLayout(visualContainer, BoxLayout.X_AXIS));
        visualContainer.setOpaque(false);
        visualContainer.setBorder(new EmptyBorder(40, 10, 40, 10));

        JScrollPane sp = new JScrollPane(visualContainer);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        bg.add(sp, BorderLayout.CENTER);
        wrapper.add(bg, BorderLayout.CENTER);
        return wrapper;
    }

    private void updateVisualizer() {
        visualContainer.removeAll();
        
        // Đầu máy (Locomotive)
        DauMay dm = (DauMay) cboDauMay.getSelectedItem();
        visualContainer.add(buildLocoNode(dm));
        
        visualContainer.add(buildConnector());
        visualContainer.add(buildAddNode(0));
        visualContainer.add(buildConnector());

        // Các toa
        for (int i = 0; i < currentWagons.size(); i++) {
            visualContainer.add(buildWagonNode(currentWagons.get(i), i));
            visualContainer.add(buildConnector());
            visualContainer.add(buildAddNode(i + 1));
            visualContainer.add(buildConnector());
        }
        
        visualContainer.revalidate();
        visualContainer.repaint();
    }

    private JPanel buildLocoNode(DauMay dm) {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(140, 150));
        p.setMaximumSize(new Dimension(140, 150));
        
        JLabel lblIco = new JLabel(loadScaledIcon("bieuTuongTau.png", 42));
        lblIco.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(lblIco, BorderLayout.CENTER);
        
        String txt = dm == null ? "KHÔNG RÕ" : dm.getMaDauMay();
        JLabel lblName = new JLabel("ĐẦU MÁY: " + txt, SwingConstants.CENTER);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblName.setForeground(Color.WHITE);
        lblName.setBorder(new EmptyBorder(10, 5, 15, 5));
        p.add(lblName, BorderLayout.SOUTH);
        
        return p;
    }

    private JPanel buildWagonNode(ToaTau toa, int index) {
        Color cVal = OUTLINE;
        String typeName = "N/A";
        if(toa.getLoaiGhe() != null) {
            switch(toa.getLoaiGhe()) {
                case GHE_CUNG: cVal = C_CUNG; typeName = "Ghế Cứng"; break;
                case GHE_MEM: cVal = C_MEM; typeName = "Ghế Mềm"; break;
                case GIUONG_NAM: cVal = C_GIUONG; typeName = "Giường Nằm"; break;
            }
        }
        final Color finalCol = cVal;
        
        JPanel p = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(finalCol);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 16, 16);
                
                // Header ribbon
                g2.fillRoundRect(0, 0, getWidth(), 35, 16, 16);
                g2.fillRect(0, 16, getWidth(), 19); 
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(140, 150));
        p.setMaximumSize(new Dimension(140, 150));
        
        // Header
        JLabel lblThuTu = new JLabel("TOA SỐ " + (index+1), SwingConstants.CENTER);
        lblThuTu.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblThuTu.setForeground(Color.WHITE);
        lblThuTu.setPreferredSize(new Dimension(140, 35));
        p.add(lblThuTu, BorderLayout.NORTH);
        
        // Core
        JPanel core = new JPanel();
        core.setLayout(new BoxLayout(core, BoxLayout.Y_AXIS));
        core.setOpaque(false);
        core.add(Box.createVerticalStrut(10));
        
        JLabel lblMa = new JLabel(toa.getMaToaTau(), SwingConstants.CENTER);
        lblMa.setFont(FONT_MONO);
        lblMa.setAlignmentX(Component.CENTER_ALIGNMENT);
        core.add(lblMa);
        
        JLabel lblLoai = new JLabel(typeName, SwingConstants.CENTER);
        lblLoai.setFont(FONT_ERR);
        lblLoai.setForeground(TEXT_MUTED);
        lblLoai.setAlignmentX(Component.CENTER_ALIGNMENT);
        core.add(Box.createVerticalStrut(6));
        core.add(lblLoai);
        p.add(core, BorderLayout.CENTER);
        
        // Bottom controls (Left, Trash, Right)
        JPanel ctrl = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 10));
        ctrl.setOpaque(false);
        
        JButton btnL = createMiniBtn("<", e -> {
            if (index > 0) {
                ToaTau tmp = currentWagons.get(index);
                currentWagons.set(index, currentWagons.get(index - 1));
                currentWagons.set(index - 1, tmp);
                updateVisualizer();
            }
        });
        btnL.setEnabled(index > 0);
        
        JButton btnR = createMiniBtn(">", e -> {
            if (index < currentWagons.size() - 1) {
                ToaTau tmp = currentWagons.get(index);
                currentWagons.set(index, currentWagons.get(index + 1));
                currentWagons.set(index + 1, tmp);
                updateVisualizer();
            }
        });
        btnR.setEnabled(index < currentWagons.size() - 1);
        
        JButton btnDel = createMiniBtn("Xóa", e -> {
            currentWagons.remove(index);
            updateVisualizer();
        });
        btnDel.setForeground(ERROR);
        
        ctrl.add(btnL);
        ctrl.add(btnDel);
        ctrl.add(btnR);
        
        p.add(ctrl, BorderLayout.SOUTH);
        
        return p;
    }

    private JPanel buildAddNode(int insertIdx) {
        JPanel wrapper = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(200, 200, 200));
                g2.setStroke(new BasicStroke(3f));
                g2.drawLine(0, getHeight()/2, getWidth(), getHeight()/2);
                g2.dispose();
            }
        };
        wrapper.setOpaque(false);
        wrapper.setPreferredSize(new Dimension(50, 150));
        wrapper.setMaximumSize(new Dimension(50, 150));
        
        JButton btn = new JButton("+") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) g2.setColor(PRIMARY_HOVER);
                else if (getModel().isRollover()) g2.setColor(new Color(100, 160, 255));
                else g2.setColor(new Color(160, 160, 160));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setPreferredSize(new Dimension(32, 32));
        btn.setFont(new Font("Arial", Font.BOLD, 18));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setToolTipText("Chèn toa tàu vào vị trí này");
        btn.addActionListener(e -> openAddWagonDialog(insertIdx));
        
        wrapper.add(btn);
        return wrapper;
    }
    
    private JPanel buildConnector() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(200, 200, 200));
                g2.setStroke(new BasicStroke(3f));
                g2.drawLine(0, getHeight()/2, getWidth(), getHeight()/2);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(20, 150));
        p.setMaximumSize(new Dimension(20, 150));
        return p;
    }

    private JButton createMiniBtn(String txt, ActionListener a) {
        JButton b = new JButton(txt);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(30, 30));
        b.setMargin(new Insets(0,0,0,0));
        b.setForeground(TEXT_MAIN);
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.addActionListener(a);
        return b;
    }

    // ── Dialog Chọn Toa Tàu ────────────────────────────────────────────────
    private void openAddWagonDialog(int insertIdx) {
        if (allToaTau.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có toa tàu nào khả dụng trong hệ thống.");
            return;
        }

        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow, "Thêm Toa Tàu", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setResizable(false);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setBackground(new Color(0, 0, 0, 0));
        
        JPanel cpan = new JPanel(new BorderLayout(0, 10));
        cpan.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1),
                new EmptyBorder(15, 15, 15, 15)));
        cpan.setBackground(SURFACE);
        
        JTextField txtSearch = createInputField("Tìm mã toa...");
        cpan.add(txtSearch, BorderLayout.NORTH);
        
        DefaultListModel<ToaTau> model = new DefaultListModel<>();
        for (ToaTau tt : allToaTau) {
            // Optional: check if already in list to avoid duplicates
            boolean inTrain = currentWagons.stream().anyMatch(w -> w.getMaToaTau().equals(tt.getMaToaTau()));
            if(!inTrain) model.addElement(tt);
        }
        
        JList<ToaTau> list = new JList<>(model);
        list.setFont(FONT_INPUT);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                super.getListCellRendererComponent(l, v, i, s, f);
                if (v instanceof ToaTau tt) {
                    setText(tt.getMaToaTau() + " - " + (tt.getLoaiGhe() != null ? tt.getLoaiGhe().toDbValue() : ""));
                }
                setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                return this;
            }
        });
        
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            void filter() {
                String kw = txtSearch.getText().trim().toLowerCase();
                if(kw.equals("tìm mã toa...")) kw = "";
                model.clear();
                for (ToaTau tt : allToaTau) {
                    boolean inTrain = currentWagons.stream().anyMatch(w -> w.getMaToaTau().equals(tt.getMaToaTau()));
                    if (!inTrain && tt.getMaToaTau().toLowerCase().contains(kw)) {
                        model.addElement(tt);
                    }
                }
            }
            @Override public void insertUpdate(DocumentEvent e) { filter(); }
            @Override public void removeUpdate(DocumentEvent e) { filter(); }
            @Override public void changedUpdate(DocumentEvent e) { filter(); }
        });
        
        JScrollPane rscroll = new JScrollPane(list);
        cpan.add(rscroll, BorderLayout.CENTER);
        
        JButton btnAdd = createPrimaryButton("Chọn Thêm");
        btnAdd.addActionListener(e -> {
            ToaTau picked = list.getSelectedValue();
            if(picked != null) {
                currentWagons.add(insertIdx, picked);
                updateVisualizer();
                dialog.dispose();
            }
        });
        JButton btnCancel = createOutlineButton("Hủy");
        btnCancel.addActionListener(e -> dialog.dispose());
        JPanel bot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bot.setOpaque(false);
        bot.add(btnCancel);
        bot.add(btnAdd);
        cpan.add(bot, BorderLayout.SOUTH);
        
        dialog.setContentPane(ThemNhanVienDialog.buildShadowWrapper(cpan));
        dialog.setSize(470, 540);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // ── Data & Saving ─────────────────────────────────────────────────────
    private void loadData() {
        new SwingWorker<Void, Void>() {
            List<DauMay> dList;
            List<ToaTau> tList;
            List<ChiTietDoanTau> detailList;

            @Override protected Void doInBackground() {
                dList = new DAO_DauMay().getAll();
                tList = new DAO_ToaTau().getAll();
                if (isEditMode) {
                    detailList = new DAO_ChiTietDoanTau().findByDoanTau(doanTau.getMaDoanTau());
                }
                return null;
            }

            @Override protected void done() {
                allToaTau = tList != null ? tList : new ArrayList<>();
                cboDauMay.removeAllItems();
                if (dList != null) {
                    for (DauMay dm : dList) cboDauMay.addItem(dm);
                }

                if (isEditMode) {
                    if (doanTau.getDauMay() != null) {
                        for (int i = 0; i < cboDauMay.getItemCount(); i++) {
                            if (cboDauMay.getItemAt(i).getMaDauMay().equals(doanTau.getDauMay().getMaDauMay())) {
                                cboDauMay.setSelectedIndex(i);
                                break;
                            }
                        }
                    }
                    if (detailList != null) {
                        for (ChiTietDoanTau ct : detailList) {
                            if (ct.getToaTau() != null) currentWagons.add(ct.getToaTau());
                        }
                    }
                }
                updateVisualizer();
            }
        }.execute();
    }

    private void doSave() {
        lblErrTen.setText(""); lblErrTen.setVisible(false);
        lblErrDauMay.setText(""); lblErrDauMay.setVisible(false);
        txtTenDoanTau.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1, true),
                new EmptyBorder(10, 14, 10, 14)));

        String ten = getFieldText(txtTenDoanTau, "Nhập tên, VD: Tàu SE1");
        if (ten.isEmpty()) {
            lblErrTen.setText("Tên Không Được Rỗng");
            lblErrTen.setVisible(true);
            txtTenDoanTau.requestFocus();
            return;
        }

        Object sel = cboDauMay.getSelectedItem();
        if (!(sel instanceof DauMay dauMay)) {
            lblErrDauMay.setText("Chưa lắp đầu kéo");
            lblErrDauMay.setVisible(true);
            return;
        }

        String ma = txtMaDoanTau.getText().trim();
        DoanTau dt = new DoanTau(ma, ten, dauMay);

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() {
                Connection con = ConnectDB.getCon();
                try {
                    // Start manually wrapping transaction
                    con.setAutoCommit(false);
                    
                    DAO_DoanTau dao = new DAO_DoanTau();
                    boolean ok = isEditMode ? dao.update(dt) : dao.insert(dt);
                    if (!ok) {
                        con.rollback();
                        return false;
                    }

                    // Delete old configurations
                    try (PreparedStatement ps = con.prepareStatement("DELETE FROM ChiTietDoanTau WHERE maDoanTau = ?")) {
                        ps.setString(1, dt.getMaDoanTau());
                        ps.executeUpdate();
                    }

                    // Insert new flow structure
                    DAO_ChiTietDoanTau daoCt = new DAO_ChiTietDoanTau();
                    for (int i = 0; i < currentWagons.size(); i++) {
                        String newId = "CTDT-" + System.nanoTime() % 1000000L;
                        ChiTietDoanTau ctdt = new ChiTietDoanTau(newId, dt, currentWagons.get(i), i + 1);
                        if (!daoCt.insert(ctdt)) {
                            con.rollback();
                            return false;
                        }
                    }
                    con.commit();
                    return true;
                } catch (Exception ex) {
                    try { if (ConnectDB.getCon() != null) ConnectDB.getCon().rollback(); } catch (SQLException e) { }
                    ex.printStackTrace();
                    return false;
                } finally {
                    try { if (ConnectDB.getCon() != null) ConnectDB.getCon().setAutoCommit(true); } catch (SQLException e) { }
                }
            }

            @Override protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(ChinhSuaDoanTauModule.this, "Lưu cấu hình tàu xuất sắc!");
                        if (callback != null) callback.accept(dt);
                    } else {
                        JOptionPane.showMessageDialog(ChinhSuaDoanTauModule.this, "Thất bại, có thể lỗi logic khóa ngoại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) { }
            }
        }.execute();
    }

    // ── Utils ─────────────────────────────────────────────────────────────
    private String generateMaDoanTau() {
        return "DT-" + String.format("%05d", System.currentTimeMillis() % 100000L);
    }

    private JPanel buildFieldGroup(String label, JComponent input, String hint, boolean req, JLabel errLabel) {
        JPanel group = new JPanel();
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
        group.setOpaque(false);
        group.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel lblRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        lblRow.setOpaque(false);
        lblRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(TEXT_MAIN);
        lblRow.add(lbl);
        if (req) {
            JLabel star = new JLabel(" *");
            star.setFont(FONT_LABEL);
            star.setForeground(ERROR);
            lblRow.add(star);
        }
        group.add(lblRow);
        group.add(Box.createVerticalStrut(8));

        input.setAlignmentX(Component.LEFT_ALIGNMENT);
        input.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        group.add(input);

        if (hint != null) {
            group.add(Box.createVerticalStrut(6));
            JLabel lblHint = new JLabel(hint);
            lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            lblHint.setForeground(TEXT_MUTED);
            lblHint.setAlignmentX(Component.LEFT_ALIGNMENT);
            lblHint.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
            group.add(lblHint);
        }
        if (errLabel != null) {
            group.add(Box.createVerticalStrut(4));
            group.add(errLabel);
        }
        group.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        return group;
    }

    private JTextField createInputField(String ph) {
        JTextField f = new JTextField();
        f.setFont(FONT_INPUT);
        f.setForeground(TEXT_MUTED);
        styleField(f);
        f.setText(ph);
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (f.getText().equals(ph)) { f.setText(""); f.setForeground(TEXT_MAIN); }
                f.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(PRIMARY, 2, true),
                        new EmptyBorder(9, 13, 9, 13)));
            }
            @Override public void focusLost(FocusEvent e) {
                if (f.getText().trim().isEmpty()) { f.setForeground(TEXT_MUTED); f.setText(ph); }
                styleField(f);
            }
        });
        return f;
    }
    
    private void styleField(JTextField f) {
        f.setPreferredSize(new Dimension(0, 42));
        Color bColor = new Color(180, 185, 190);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bColor, 1, true),
                new EmptyBorder(10, 14, 10, 14)));
    }

    private String getFieldText(JTextField f, String ph) {
        String t = f.getText();
        return t.equals(ph) ? "" : t.trim();
    }

    private JLabel createErrorLabel() {
        JLabel lbl = new JLabel();
        lbl.setFont(FONT_ERR);
        lbl.setForeground(ERROR);
        lbl.setVisible(false);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JButton createPrimaryButton(String txt) {
        JButton btn = new JButton(txt) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? PRIMARY_HOVER : PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BTN);
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(170, 42));
        return btn;
    }

    private JButton createOutlineButton(String txt) {
        JButton btn = new JButton(txt) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? OUTLINE.darker() : OUTLINE);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BTN);
        btn.setForeground(TEXT_MAIN);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(110, 42));
        return btn;
    }
    
    private ImageIcon loadScaledIcon(String fileName, int size) {
        try {
            java.net.URL url = getClass().getResource("/icons/" + fileName);
            if (url == null) return null;
            Image img = new ImageIcon(url).getImage()
                    .getScaledInstance(size, size, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } catch (Exception e) {
            return null;
        }
    }

    // ── AppModule interface ───────────────────────────────────────────────
    @Override public String getTitle() { return isEditMode ? "Chỉnh Sửa Đội Tàu" : "Thiết Lập Đội Tàu Mới"; }
    @Override public JPanel  getView() { return this; }
    @Override public void setOnResult(Consumer<Object> cb) { this.callback = cb; }
    @Override public void reset() { }
}
