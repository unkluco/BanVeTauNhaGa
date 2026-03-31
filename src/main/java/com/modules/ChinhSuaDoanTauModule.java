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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class ChinhSuaDoanTauModule extends JPanel implements AppModule {

    private Consumer<Object> callback;

    // --- Mode ---
    private final boolean isEditMode;   // true = edit, false = new

    // --- DAOs ---
    private final DAO_DoanTau        daoDoanTau = new DAO_DoanTau();
    private final DAO_ChiTietDoanTau daoChiTiet = new DAO_ChiTietDoanTau();
    private final DAO_ToaTau         daoToaTau  = new DAO_ToaTau();
    private final DAO_DauMay         daoDauMay  = new DAO_DauMay();

    // --- Original DoanTau (null when creating new) ---
    private final DoanTau originalDoanTau;

    // --- Form fields ---
    private JTextField txtMaDoanTau;
    private JTextField txtTenDoanTau;

    // --- Selected DauMay (replaces cboDauMay combobox) ---
    private DauMay       selectedDauMay;
    private List<DauMay> allDauMay = new ArrayList<>();

    // --- Assembly list (ordered list of ToaTau) ---
    private final List<ToaTau> assemblyList = new ArrayList<>();
    private JPanel             assemblyPanel;
    private JScrollPane        assemblyScroll;

    // --- Available ToaTau (loaded from DB) ---
    private List<ToaTau> availableToaTau = new ArrayList<>();

    // --- Design tokens ---
    private static final Color PRIMARY        = new Color(0x00, 0x5D, 0x90);
    private static final Color PRIMARY_LIGHT  = new Color(0xE3, 0xF2, 0xFD);
    private static final Color SURFACE        = new Color(0xF7, 0xF9, 0xFB);
    private static final Color SURFACE_LOW    = new Color(0xF2, 0xF4, 0xF6);
    private static final Color CARD_BG        = Color.WHITE;
    private static final Color ON_SURFACE     = new Color(0x19, 0x1C, 0x1E);
    private static final Color ON_SURF_VAR    = new Color(0x40, 0x48, 0x50);
    private static final Color OUTLINE        = new Color(0xDE, 0xE3, 0xE8);
    private static final Color ERROR_FG       = new Color(0xB9, 0x1C, 0x1C);
    private static final Color GHE_CUNG_BG   = new Color(0xCD, 0xE5, 0xFF);
    private static final Color GHE_CUNG_FG   = new Color(0x00, 0x4B, 0x74);
    private static final Color GHE_MEM_BG    = new Color(0xD1, 0xFA, 0xE5);
    private static final Color GHE_MEM_FG    = new Color(0x06, 0x5F, 0x46);
    private static final Color GIUONG_NAM_BG = new Color(0xFF, 0xED, 0xD5);
    private static final Color GIUONG_NAM_FG = new Color(0x92, 0x40, 0x0E);
    private static final Color CONNECTOR_CLR  = new Color(0xBF, 0xC7, 0xD1);

    private static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font FONT_DESC   = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_LABEL  = new Font("Segoe UI", Font.BOLD, 11);
    private static final Font FONT_BODY   = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_BOLD   = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_MONO   = new Font("Consolas", Font.BOLD, 13);
    private static final Font FONT_BTN    = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD, 13);

    // AppModule buttons
    private JButton btnSubmit;
    private JButton btnCancel;
    private JPanel  btnPanel;

    // =====================================================================
    //  Constructor
    // =====================================================================

    public ChinhSuaDoanTauModule(DoanTau doanTau) {
        this.originalDoanTau = doanTau;
        this.isEditMode      = (doanTau != null);
        setLayout(new BorderLayout(0, 0));
        setBackground(SURFACE);
        allDauMay = daoDauMay.getAll();
        if (isEditMode && doanTau.getDauMay() != null) {
            selectedDauMay = doanTau.getDauMay();
        } else if (!allDauMay.isEmpty()) {
            selectedDauMay = allDauMay.get(0);
        }
        buildUI();
        loadAvailableToaTau();
        if (isEditMode) loadAssembly(doanTau.getMaDoanTau());
    }

    // =====================================================================
    //  Build UI
    // =====================================================================

    private void buildUI() {
        // ---- Scrollable main content ----
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setOpaque(false);
        mainContent.setBorder(new EmptyBorder(24, 24, 24, 24));

        // ---- Header ----
        JLabel lblTitle = new JLabel(isEditMode ? "Ch\u1EC9nh s\u1EEDa \u0111o\u00E0n t\u00E0u" : "Thi\u1EBFt l\u1EADp \u0111o\u00E0n t\u00E0u m\u1EDBi");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(PRIMARY);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSubtitle = new JLabel("C\u1EA5u h\u00ECnh th\u00F4ng s\u1ED1 k\u1EF9 thu\u1EADt v\u00E0 s\u01A1 \u0111\u1ED3 l\u1EAFp r\u00E1p toa xe.");
        lblSubtitle.setFont(FONT_DESC);
        lblSubtitle.setForeground(ON_SURF_VAR);
        lblSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        mainContent.add(lblTitle);
        mainContent.add(Box.createVerticalStrut(4));
        mainContent.add(lblSubtitle);
        mainContent.add(Box.createVerticalStrut(24));

        // ---- Section 1: Train info ----
        JPanel infoSection = buildSectionCard("Th\u00F4ng tin \u0111o\u00E0n t\u00E0u");

        JPanel formGrid = new JPanel(new GridLayout(1, 2, 16, 0));
        formGrid.setOpaque(false);

        // Mã đoàn tàu
        JPanel fldMa = buildFormField("M\u00E3 \u0111o\u00E0n t\u00E0u");
        txtMaDoanTau = new JTextField();
        txtMaDoanTau.setFont(FONT_MONO);
        styleTextField(txtMaDoanTau);
        if (isEditMode) {
            txtMaDoanTau.setText(originalDoanTau.getMaDoanTau());
            txtMaDoanTau.setEditable(false);
            txtMaDoanTau.setBackground(SURFACE_LOW);
        }
        fldMa.add(txtMaDoanTau, BorderLayout.CENTER);
        formGrid.add(fldMa);

        // Tên đoàn tàu
        JPanel fldTen = buildFormField("T\u00EAn \u0111o\u00E0n t\u00E0u");
        txtTenDoanTau = new JTextField();
        txtTenDoanTau.setFont(FONT_BODY);
        styleTextField(txtTenDoanTau);
        if (isEditMode) txtTenDoanTau.setText(originalDoanTau.getTenDoanTau());
        fldTen.add(txtTenDoanTau, BorderLayout.CENTER);
        formGrid.add(fldTen);

        // Wrap in NORTH so the fields don't stretch vertically
        JPanel infoWrap = new JPanel(new BorderLayout());
        infoWrap.setOpaque(false);
        infoWrap.add(formGrid, BorderLayout.NORTH);
        infoSection.add(infoWrap, BorderLayout.CENTER);
        infoSection.setAlignmentX(Component.LEFT_ALIGNMENT);

        mainContent.add(infoSection);
        mainContent.add(Box.createVerticalStrut(24));

        // ---- Section 2: Legend ----
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        legendPanel.setOpaque(false);
        legendPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        legendPanel.add(buildLegendBadge("Gh\u1EBF c\u1EE9ng",   GHE_CUNG_BG,    GHE_CUNG_FG));
        legendPanel.add(buildLegendBadge("Gh\u1EBF m\u1EC1m",    GHE_MEM_BG,     GHE_MEM_FG));
        legendPanel.add(buildLegendBadge("Gi\u01B0\u1EDDng n\u1EB1m", GIUONG_NAM_BG, GIUONG_NAM_FG));

        JLabel lblLegendTitle = new JLabel("Ch\u00FA th\u00EDch lo\u1EA1i toa:");
        lblLegendTitle.setFont(FONT_LABEL);
        lblLegendTitle.setForeground(ON_SURF_VAR);

        JPanel legendRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        legendRow.setOpaque(false);
        legendRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        legendRow.add(lblLegendTitle);
        legendRow.add(legendPanel);

        mainContent.add(legendRow);
        mainContent.add(Box.createVerticalStrut(16));

        // ---- Section 3: Assembly ----
        JLabel lblAssembly = new JLabel("S\u01A1 \u0111\u1ED3 t\u00E0u");
        lblAssembly.setFont(FONT_SECTION);
        lblAssembly.setForeground(ON_SURFACE);
        lblAssembly.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContent.add(lblAssembly);
        mainContent.add(Box.createVerticalStrut(10));

        assemblyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        assemblyPanel.setOpaque(false);

        assemblyScroll = new JScrollPane(assemblyPanel);
        assemblyScroll.setPreferredSize(new Dimension(0, SLOT_H + 20));
        assemblyScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, SLOT_H + 20));
        assemblyScroll.setBorder(null);
        assemblyScroll.setOpaque(false);
        assemblyScroll.getViewport().setOpaque(false);
        assemblyScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        assemblyScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        assemblyScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContent.add(assemblyScroll);
        mainContent.add(Box.createVerticalStrut(12));

        // ---- Footer actions ----
        JPanel footerOuter = new JPanel(new BorderLayout());
        footerOuter.setBackground(CARD_BG);
        footerOuter.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, OUTLINE),
                new EmptyBorder(16, 24, 16, 24)));

        JPanel footerBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        footerBtns.setOpaque(false);

        btnCancel = new JButton("H\u1EE7y b\u1ECF");
        btnCancel.setFont(FONT_BTN);
        btnCancel.setForeground(PRIMARY);
        btnCancel.setBackground(PRIMARY_LIGHT);
        btnCancel.setBorder(new EmptyBorder(10, 24, 10, 24));
        btnCancel.setFocusPainted(false);
        btnCancel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        ImageIcon icoThoat = loadScaledIcon("nutThoat.png", 16);
        if (icoThoat != null) { btnCancel.setIcon(icoThoat); btnCancel.setIconTextGap(6); }
        btnCancel.addActionListener(e -> {
            if (callback != null) callback.accept(null);
        });

        btnSubmit = new JButton(isEditMode ? "L\u01B0u thay \u0111\u1ED5i" : "T\u1EA1o \u0111o\u00E0n t\u00E0u");
        btnSubmit.setFont(FONT_BTN);
        ImageIcon icoLuu = loadScaledIcon("nutLuu.png", 16);
        if (icoLuu != null) { btnSubmit.setIcon(icoLuu); btnSubmit.setIconTextGap(6); }
        btnSubmit.addActionListener(e -> execute());
        styleSubmitButton(btnSubmit);

        footerBtns.add(btnCancel);
        footerBtns.add(btnSubmit);
        footerOuter.add(footerBtns, BorderLayout.EAST);

        // AppModule hidden btnPanel (stub — real buttons are in footer)
        btnPanel = new JPanel();
        btnPanel.setVisible(false);

        JScrollPane outerScroll = new JScrollPane(mainContent);
        outerScroll.setBorder(null);
        outerScroll.getViewport().setBackground(SURFACE);
        outerScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        add(outerScroll, BorderLayout.CENTER);
        add(footerOuter, BorderLayout.SOUTH);
        add(btnPanel,    BorderLayout.NORTH);

        refreshAssemblyPanel();
    }

    // =====================================================================
    //  Data loading
    // =====================================================================

    private void loadAvailableToaTau() {
        availableToaTau = daoToaTau.getAll();
    }

    private void loadAssembly(String maDoanTau) {
        assemblyList.clear();
        List<ChiTietDoanTau> chiTiet = daoChiTiet.findByDoanTau(maDoanTau);
        chiTiet.sort(Comparator.comparingInt(ChiTietDoanTau::getSoThuTu));
        for (ChiTietDoanTau ct : chiTiet) {
            if (ct.getToaTau() != null) assemblyList.add(ct.getToaTau());
        }
        refreshAssemblyPanel();
    }

    // =====================================================================
    //  Assembly panel  (horizontal)
    // =====================================================================

    private static final int CARD_W = 180;
    private static final int CARD_H = 195;
    private static final int CONN_W = 48;
    private static final int SLOT_H = CARD_H + 38;  // card + delete button area below

    private void refreshAssemblyPanel() {
        assemblyPanel.removeAll();

        // ---- Locomotive block ----
        assemblyPanel.add(buildLocomotiveCard());

        if (assemblyList.isEmpty()) {
            assemblyPanel.add(buildHConnector());
            assemblyPanel.add(buildInsertButton(0));
        } else {
            for (int i = 0; i < assemblyList.size(); i++) {
                final int insertBefore = i;
                final int cardIdx      = i;
                // connector → [+] → connector → [toa card]
                assemblyPanel.add(buildHConnector());
                assemblyPanel.add(buildInsertButton(insertBefore));
                assemblyPanel.add(buildHConnector());
                ToaTau toa = assemblyList.get(i);
                assemblyPanel.add(buildToaCard(cardIdx + 1, toa, () -> {
                    assemblyList.remove(cardIdx);
                    refreshAssemblyPanel();
                }, cardIdx));
            }
            // Trailing connector → [+]
            assemblyPanel.add(buildHConnector());
            assemblyPanel.add(buildInsertButton(assemblyList.size()));
        }

        assemblyPanel.revalidate();
        assemblyPanel.repaint();
    }

    /** Locomotive card — left anchor of the diagram, clickable to change DauMay.
     *  Returns a SLOT_H-tall container so it aligns with toa slots. */
    private JPanel buildLocomotiveCard() {
        // Inner card
        JPanel card = new JPanel(new BorderLayout(0, 8)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getClientProperty("hovered") == Boolean.TRUE
                        ? PRIMARY.brighter() : PRIMARY;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(CARD_W, CARD_H));
        card.setMaximumSize(new Dimension(CARD_W, CARD_H));
        card.setBorder(new EmptyBorder(14, 12, 14, 12));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.setToolTipText("Nh\u1EA5p \u0111\u1EC3 \u0111\u1ED5i \u0111\u1EA7u m\u00E1y");

        // Icon
        ImageIcon ico = loadScaledIcon("bieuTuongTau.png", 56);
        JLabel lblIcon = ico != null
                ? new JLabel(ico, SwingConstants.CENTER)
                : new JLabel("\uD83D\uDE82", SwingConstants.CENTER);
        if (ico == null) lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        lblIcon.setForeground(Color.WHITE);

        // Text
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel lblType = new JLabel("\u0110\u1EA7u m\u00E1y", SwingConstants.CENTER);
        lblType.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblType.setForeground(new Color(0xFF, 0xFF, 0xFF, 170));
        lblType.setAlignmentX(Component.CENTER_ALIGNMENT);

        String locoName = selectedDauMay != null ? selectedDauMay.getMaDauMay() : "\u2014";
        JLabel lblName = new JLabel(locoName, SwingConstants.CENTER);
        lblName.setFont(FONT_MONO);
        lblName.setForeground(Color.WHITE);
        lblName.setAlignmentX(Component.CENTER_ALIGNMENT);

        textPanel.add(lblType);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(lblName);

        card.add(lblIcon,   BorderLayout.CENTER);
        card.add(textPanel, BorderLayout.SOUTH);

        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                card.putClientProperty("hovered", Boolean.TRUE); card.repaint();
            }
            @Override public void mouseExited(MouseEvent e) {
                card.putClientProperty("hovered", Boolean.FALSE); card.repaint();
            }
            @Override public void mouseClicked(MouseEvent e) { showPickDauMayDialog(); }
        });

        // Outer SLOT_H-tall container (card at top, empty space at bottom)
        JPanel slot = new JPanel();
        slot.setLayout(new BoxLayout(slot, BoxLayout.Y_AXIS));
        slot.setOpaque(false);
        slot.setPreferredSize(new Dimension(CARD_W, SLOT_H));
        slot.add(card);
        slot.add(Box.createVerticalGlue());
        return slot;
    }

    /** Toa card — same visual style as locomotive, color-coded by LoaiGhe.
     *  Returns a SLOT_H-tall container: [card (clickable to swap)] + [delete button below]. */
    private JPanel buildToaCard(int stt, ToaTau toa, Runnable onDelete, int assemblyIndex) {
        LoaiGhe lg     = toa.getLoaiGhe();
        Color[] colors = colorForLoai(lg);
        Color cardBg   = colors[0];
        Color cardFg   = colors[1];

        // ---- Inner card (same structure as locomotive) ----
        JPanel card = new JPanel(new BorderLayout(0, 8)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getClientProperty("hovered") == Boolean.TRUE
                        ? cardBg.darker() : cardBg;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(CARD_W, CARD_H));
        card.setMaximumSize(new Dimension(CARD_W, CARD_H));
        card.setBorder(new EmptyBorder(14, 12, 14, 12));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.setToolTipText("Nh\u1EA5p \u0111\u1EC3 \u0111\u1ED5i toa");

        // Sequence number (top-left, subtle)
        JLabel lblStt = new JLabel(String.format("%02d", stt));
        lblStt.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblStt.setForeground(new Color(cardFg.getRed(), cardFg.getGreen(), cardFg.getBlue(), 180));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(lblStt, BorderLayout.WEST);

        // Icon (center)
        String iconFile = switch (lg != null ? lg : LoaiGhe.GHE_CUNG) {
            case GHE_CUNG   -> "bieuTuongGheCung.png";
            case GHE_MEM    -> "bieuTuongGheMem.png";
            case GIUONG_NAM -> "bieuTuongGiuongNam.png";
        };
        ImageIcon ico = loadScaledIcon(iconFile, 56);
        JLabel lblIcon = ico != null
                ? new JLabel(ico, SwingConstants.CENTER)
                : new JLabel("?", SwingConstants.CENTER);
        if (ico == null) lblIcon.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblIcon.setForeground(cardFg);

        // Name (bottom)
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel lblName = new JLabel(toa.getMaToaTau(), SwingConstants.CENTER);
        lblName.setFont(FONT_MONO);
        lblName.setForeground(cardFg);
        lblName.setAlignmentX(Component.CENTER_ALIGNMENT);

        textPanel.add(lblName);

        card.add(topRow,    BorderLayout.NORTH);
        card.add(lblIcon,   BorderLayout.CENTER);
        card.add(textPanel, BorderLayout.SOUTH);

        // Click card → swap toa
        if (assemblyIndex >= 0) {
            final int idx = assemblyIndex;
            card.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) {
                    card.putClientProperty("hovered", Boolean.TRUE); card.repaint();
                }
                @Override public void mouseExited(MouseEvent e) {
                    card.putClientProperty("hovered", Boolean.FALSE); card.repaint();
                }
                @Override public void mouseClicked(MouseEvent e) { showSwapToaDialog(idx); }
            });
        }

        // ---- Delete button (below card) ----
        JButton btnDel = new JButton("X\u00F3a toa");
        btnDel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnDel.setForeground(ERROR_FG);
        btnDel.setBackground(new Color(ERROR_FG.getRed(), ERROR_FG.getGreen(), ERROR_FG.getBlue(), 15));
        btnDel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(ERROR_FG.getRed(), ERROR_FG.getGreen(), ERROR_FG.getBlue(), 80), 1, true),
                new EmptyBorder(3, 8, 3, 8)));
        btnDel.setFocusPainted(false);
        btnDel.setContentAreaFilled(false);
        btnDel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        ImageIcon icoXoa = loadScaledIcon("nutXoa.png", 13);
        if (icoXoa != null) { btnDel.setIcon(icoXoa); btnDel.setIconTextGap(4); }
        btnDel.addActionListener(e -> onDelete.run());

        JPanel delRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
        delRow.setOpaque(false);
        delRow.add(btnDel);

        // ---- SLOT_H-tall outer container ----
        JPanel slot = new JPanel();
        slot.setLayout(new BoxLayout(slot, BoxLayout.Y_AXIS));
        slot.setOpaque(false);
        slot.setPreferredSize(new Dimension(CARD_W, SLOT_H));
        slot.add(card);
        slot.add(delRow);
        slot.add(Box.createVerticalGlue());
        return slot;
    }

    /** Horizontal connector — thin dashed line, SLOT_H tall, line drawn at CARD_H/2 */
    private JPanel buildHConnector() {
        JPanel conn = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(CONNECTOR_CLR);
                g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                        1f, new float[]{6, 5}, 0f));
                int cy = CARD_H / 2;
                g2.drawLine(0, cy, getWidth(), cy);
                g2.dispose();
            }
        };
        conn.setOpaque(false);
        conn.setPreferredSize(new Dimension(CONN_W, SLOT_H));
        return conn;
    }

    /** Small circular "+" button, positioned at CARD_H/2 so it aligns with card center */
    private JPanel buildInsertButton(int insertIndex) {
        int BTN = 30;
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean hov = getModel().isRollover();
                g2.setColor(hov ? PRIMARY : PRIMARY_LIGHT);
                g2.fillOval(1, 1, getWidth() - 2, getHeight() - 2);
                g2.setColor(PRIMARY);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(1, 1, getWidth() - 2, getHeight() - 2);
                g2.setColor(hov ? Color.WHITE : PRIMARY);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                FontMetrics fm = g2.getFontMetrics();
                String txt = "+";
                g2.drawString(txt, (getWidth() - fm.stringWidth(txt)) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setToolTipText("Th\u00EAm toa t\u1EA1i v\u1ECB tr\u00ED " + (insertIndex + 1));
        btn.setPreferredSize(new Dimension(BTN, BTN));
        btn.addActionListener(e -> showThemToaDialog(insertIndex));

        // Position button so its center aligns with CARD_H/2
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setOpaque(false);
        wrapper.setPreferredSize(new Dimension(BTN, SLOT_H));
        wrapper.add(Box.createRigidArea(new Dimension(BTN, CARD_H / 2 - BTN / 2)));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        wrapper.add(btn);
        wrapper.add(Box.createVerticalGlue());
        return wrapper;
    }

    // =====================================================================
    //  Picker dialogs
    // =====================================================================

    private void showThemToaDialog(int insertIndex) {
        if (availableToaTau.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Kh\u00F4ng c\u00F3 toa n\u00E0o trong c\u01A1 s\u1EDF d\u1EEF li\u1EC7u.",
                    "Th\u00F4ng b\u00E1o", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        ToaTau selected = showSearchablePickerDialog(
                "Th\u00EAm toa v\u00E0o v\u1ECB tr\u00ED " + (insertIndex + 1),
                availableToaTau,
                tt -> tt.getMaToaTau() + "  \u2014  " + (tt.getLoaiGhe() != null ? tt.getLoaiGhe().toString() : "?"),
                null);
        if (selected != null) {
            assemblyList.add(insertIndex, selected);
            refreshAssemblyPanel();
        }
    }

    private void showSwapToaDialog(int idx) {
        if (availableToaTau.isEmpty()) return;
        ToaTau current = assemblyList.get(idx);
        ToaTau selected = showSearchablePickerDialog(
                "\u0110\u1ED5i toa t\u1EA1i v\u1ECB tr\u00ED " + (idx + 1),
                availableToaTau,
                tt -> tt.getMaToaTau() + "  \u2014  " + (tt.getLoaiGhe() != null ? tt.getLoaiGhe().toString() : "?"),
                current);
        if (selected != null) {
            assemblyList.set(idx, selected);
            refreshAssemblyPanel();
        }
    }

    private void showPickDauMayDialog() {
        if (allDauMay.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Kh\u00F4ng c\u00F3 \u0111\u1EA7u m\u00E1y n\u00E0o trong c\u01A1 s\u1EDF d\u1EEF li\u1EC7u.",
                    "Th\u00F4ng b\u00E1o", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        DauMay selected = showSearchablePickerDialog(
                "\u0110\u1ED5i \u0111\u1EA7u m\u00E1y",
                allDauMay,
                dm -> dm.getTenDauMay() + "  \u2014  " + dm.getMaDauMay(),
                selectedDauMay);
        if (selected != null) {
            selectedDauMay = selected;
            refreshAssemblyPanel();
        }
    }

    /**
     * Generic searchable picker dialog.
     * Shows a search field + scrollable list. Returns the selected item, or null if cancelled.
     */
    private <T> T showSearchablePickerDialog(String title, List<T> items,
                                              Function<T, String> labelFn, T current) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner instanceof Frame ? (Frame) owner : null, title, true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // ---- Result holder ----
        @SuppressWarnings("unchecked")
        T[] result = (T[]) new Object[1];

        // ---- Search field ----
        JTextField searchField = new JTextField();
        searchField.setFont(FONT_BODY);
        searchField.setBackground(CARD_BG);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1, true),
                new EmptyBorder(6, 10, 6, 10)));
        searchField.setPreferredSize(new Dimension(0, 36));

        JLabel lblSearch = new JLabel("\uD83D\uDD0D"); // 🔍
        lblSearch.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        lblSearch.setBorder(new EmptyBorder(0, 4, 0, 0));

        JPanel searchRow = new JPanel(new BorderLayout(6, 0));
        searchRow.setBackground(CARD_BG);
        searchRow.setBorder(new EmptyBorder(14, 14, 8, 14));
        searchRow.add(lblSearch,   BorderLayout.WEST);
        searchRow.add(searchField, BorderLayout.CENTER);

        // ---- List model ----
        DefaultListModel<T> listModel = new DefaultListModel<>();
        items.forEach(listModel::addElement);

        JList<T> jList = new JList<>(listModel);
        jList.setFont(FONT_BODY);
        jList.setBackground(CARD_BG);
        jList.setSelectionBackground(PRIMARY_LIGHT);
        jList.setSelectionForeground(PRIMARY);
        jList.setBorder(new EmptyBorder(4, 8, 4, 8));
        jList.setFixedCellHeight(36);
        jList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean sel, boolean focus) {
                super.getListCellRendererComponent(list, value, index, sel, focus);
                @SuppressWarnings("unchecked") T item = (T) value;
                setText(labelFn.apply(item));
                setBorder(new EmptyBorder(6, 10, 6, 10));
                if (!sel) setBackground(index % 2 == 0 ? CARD_BG : SURFACE_LOW);
                return this;
            }
        });

        // Pre-select current item
        if (current != null) {
            for (int i = 0; i < listModel.size(); i++) {
                if (listModel.getElementAt(i).equals(current)) {
                    jList.setSelectedIndex(i);
                    jList.ensureIndexIsVisible(i);
                    break;
                }
            }
        } else if (!listModel.isEmpty()) {
            jList.setSelectedIndex(0);
        }

        // Filter on search
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            private void filter() {
                String q = searchField.getText().toLowerCase().trim();
                listModel.clear();
                items.stream()
                     .filter(item -> labelFn.apply(item).toLowerCase().contains(q))
                     .forEach(listModel::addElement);
                if (!listModel.isEmpty()) jList.setSelectedIndex(0);
            }
            @Override public void insertUpdate(DocumentEvent e)  { filter(); }
            @Override public void removeUpdate(DocumentEvent e)  { filter(); }
            @Override public void changedUpdate(DocumentEvent e) { filter(); }
        });

        JScrollPane scroll = new JScrollPane(jList);
        scroll.setBorder(BorderFactory.createLineBorder(OUTLINE, 1, true));
        scroll.setPreferredSize(new Dimension(0, 220));

        // ---- Buttons ----
        JButton btnOk = new JButton("Ch\u1ECDn");
        ImageIcon icoOk = loadScaledIcon("nutLuu.png", 15);
        if (icoOk != null) { btnOk.setIcon(icoOk); btnOk.setIconTextGap(5); }
        styleSubmitButton(btnOk);
        btnOk.setPreferredSize(new Dimension(100, 36));

        JButton btnCancel2 = new JButton("H\u1EE7y");
        ImageIcon icoHuy = loadScaledIcon("nutThoat.png", 15);
        if (icoHuy != null) { btnCancel2.setIcon(icoHuy); btnCancel2.setIconTextGap(5); }
        btnCancel2.setFont(FONT_BTN);
        btnCancel2.setForeground(PRIMARY);
        btnCancel2.setBackground(PRIMARY_LIGHT);
        btnCancel2.setBorder(new EmptyBorder(8, 20, 8, 20));
        btnCancel2.setFocusPainted(false);
        btnCancel2.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCancel2.setPreferredSize(new Dimension(100, 36));

        JPanel footerBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        footerBtns.setBackground(CARD_BG);
        footerBtns.add(btnCancel2);
        footerBtns.add(btnOk);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(CARD_BG);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, OUTLINE),
                new EmptyBorder(10, 14, 10, 14)));
        footer.add(footerBtns, BorderLayout.EAST);

        // ---- Layout ----
        JPanel body = new JPanel(new BorderLayout(0, 0));
        body.setBackground(CARD_BG);
        body.setBorder(new EmptyBorder(0, 14, 10, 14));
        body.add(scroll, BorderLayout.CENTER);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CARD_BG);
        root.add(searchRow, BorderLayout.NORTH);
        root.add(body,      BorderLayout.CENTER);
        root.add(footer,    BorderLayout.SOUTH);

        // ---- Actions ----
        Runnable confirm = () -> {
            T sel = jList.getSelectedValue();
            if (sel != null) {
                result[0] = sel;
                dialog.dispose();
            }
        };
        btnOk.addActionListener(e -> confirm.run());
        btnCancel2.addActionListener(e -> dialog.dispose());
        jList.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) confirm.run();
            }
        });

        dialog.setContentPane(root);
        dialog.setSize(400, 380);
        dialog.setMinimumSize(new Dimension(320, 300));
        dialog.setLocationRelativeTo(this);
        dialog.getRootPane().setDefaultButton(btnOk);
        dialog.setVisible(true);

        return result[0];
    }

    // =====================================================================
    //  Save (execute)
    // =====================================================================

    private void execute() {
        String ma  = txtMaDoanTau.getText().trim();
        String ten = txtTenDoanTau.getText().trim();

        if (ma.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui l\u00F2ng nh\u1EADp m\u00E3 \u0111o\u00E0n t\u00E0u.", "Thi\u1EBFu th\u00F4ng tin", JOptionPane.WARNING_MESSAGE);
            txtMaDoanTau.requestFocus();
            return;
        }
        if (ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui l\u00F2ng nh\u1EADp t\u00EAn \u0111o\u00E0n t\u00E0u.", "Thi\u1EBFu th\u00F4ng tin", JOptionPane.WARNING_MESSAGE);
            txtTenDoanTau.requestFocus();
            return;
        }
        if (selectedDauMay == null) {
            JOptionPane.showMessageDialog(this, "Vui l\u00F2ng ch\u1ECDn \u0111\u1EA7u m\u00E1y.", "Thi\u1EBFu th\u00F4ng tin", JOptionPane.WARNING_MESSAGE);
            return;
        }
        DoanTau dt = new DoanTau(ma, ten, selectedDauMay);

        boolean ok;
        if (isEditMode) {
            ok = daoDoanTau.update(dt);
        } else {
            ok = daoDoanTau.insert(dt);
        }

        if (!ok) {
            JOptionPane.showMessageDialog(this,
                    "L\u1ED7i khi l\u01B0u \u0111o\u00E0n t\u00E0u. Ki\u1EC3m tra m\u00E3 c\u00F3 b\u1ECB tr\u00F9ng kh\u00F4ng.",
                    "L\u1ED7i", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Re-insert all ChiTietDoanTau
        Connection con = ConnectDB.getCon();
        if (con == null) {
            JOptionPane.showMessageDialog(this, "Kh\u00F4ng th\u1EC3 k\u1EBFt n\u1ED1i c\u01A1 s\u1EDF d\u1EEF li\u1EC7u.", "L\u1ED7i", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Delete existing ChiTietDoanTau
            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM ChiTietDoanTau WHERE maDoanTau = ?")) {
                ps.setString(1, ma);
                ps.executeUpdate();
            }

            // Insert new list
            for (int i = 0; i < assemblyList.size(); i++) {
                ToaTau toa = assemblyList.get(i);
                String idChiTiet = "CTDT-" + ma + "-" + (i + 1);
                ChiTietDoanTau ct = new ChiTietDoanTau(idChiTiet, dt, toa, i + 1);
                daoChiTiet.insert(ct);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "L\u1ED7i khi l\u01B0u chi ti\u1EBFt toa: " + e.getMessage(), "L\u1ED7i", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this,
                isEditMode ? "\u0110\u00E3 c\u1EADp nh\u1EADt \u0111o\u00E0n t\u00E0u th\u00E0nh c\u00F4ng."
                           : "\u0110\u00E3 t\u1EA1o \u0111o\u00E0n t\u00E0u m\u1EDBi th\u00E0nh c\u00F4ng.",
                "Th\u00E0nh c\u00F4ng", JOptionPane.INFORMATION_MESSAGE);

        if (callback != null) callback.accept(dt);
    }

    // =====================================================================
    //  UI helpers
    // =====================================================================

    private JPanel buildSectionCard(String title) {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(SURFACE_LOW);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1, true),
                new EmptyBorder(16, 16, 16, 16)));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JLabel lbl = new JLabel(title.toUpperCase());
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(ON_SURF_VAR);
        card.add(lbl, BorderLayout.NORTH);

        return card;
    }

    private JPanel buildFormField(String labelText) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);
        JLabel lbl = new JLabel(labelText.toUpperCase());
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(ON_SURF_VAR);
        panel.add(lbl, BorderLayout.NORTH);
        return panel;
    }

    private void styleTextField(JTextField tf) {
        tf.setFont(FONT_BODY);
        tf.setBackground(CARD_BG);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1, true),
                new EmptyBorder(6, 10, 6, 10)));
        tf.setPreferredSize(new Dimension(0, 38));
    }

    private JPanel buildLegendBadge(String text, Color bg, Color fg) {
        JPanel badge = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
            }
        };
        badge.setOpaque(false);
        JLabel dot = new JLabel("\u25CF");
        dot.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        dot.setForeground(fg);
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(fg);
        badge.add(dot);
        badge.add(lbl);
        return badge;
    }

    private JLabel buildBadgeLabel(String text, Color bg, Color fg) {
        JLabel lbl = new JLabel(" " + text + " ") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(fg);
        lbl.setOpaque(false);
        return lbl;
    }

    private Color[] colorForLoai(LoaiGhe lg) {
        if (lg == null) return new Color[]{SURFACE_LOW, ON_SURF_VAR};
        return switch (lg) {
            case GHE_CUNG   -> new Color[]{GHE_CUNG_BG,   GHE_CUNG_FG};
            case GHE_MEM    -> new Color[]{GHE_MEM_BG,    GHE_MEM_FG};
            case GIUONG_NAM -> new Color[]{GIUONG_NAM_BG, GIUONG_NAM_FG};
        };
    }

    private void styleSubmitButton(JButton btn) {
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 24, 10, 24));
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(((JButton) c).getModel().isPressed() ? PRIMARY.darker() : PRIMARY);
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 14, 14);
                g2.dispose();
                super.paint(g, c);
            }
        });
    }

    private JButton createOutlineButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY);
        btn.setForeground(PRIMARY);
        btn.setBackground(PRIMARY_LIGHT);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xBF, 0xC7, 0xD1), 1, true),
                new EmptyBorder(7, 16, 7, 16)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton makeIconBtn(String icon, Color fg) {
        JButton btn = new JButton(icon);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setForeground(fg);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(30, 30));
        return btn;
    }

    // =====================================================================
    //  Icon helper
    // =====================================================================

    private ImageIcon loadScaledIcon(String fileName, int size) {
        try {
            java.net.URL url = getClass().getResource("/icons/" + fileName);
            if (url != null) {
                Image img = new ImageIcon(url).getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
                return new ImageIcon(img);
            }
        } catch (Exception ignored) {}
        return null;
    }

    // =====================================================================
    //  AppModule interface
    // =====================================================================

    @Override public String getTitle() { return isEditMode ? "Ch\u1EC9nh s\u1EEDa \u0111o\u00E0n t\u00E0u" : "T\u1EA1o \u0111o\u00E0n t\u00E0u m\u1EDBi"; }
    @Override public JPanel getView()  { return this; }

    @Override
    public void setOnResult(Consumer<Object> cb) {
        this.callback = cb;
        // Footer buttons (Hủy/Lưu) are always visible in this module
        // btnPanel (AppModule stub) stays hidden
    }

    @Override
    public void reset() {
        txtMaDoanTau.setText(isEditMode ? originalDoanTau.getMaDoanTau() : "");
        txtTenDoanTau.setText(isEditMode ? originalDoanTau.getTenDoanTau() : "");
        if (isEditMode && originalDoanTau.getDauMay() != null) {
            selectedDauMay = originalDoanTau.getDauMay();
        } else if (!allDauMay.isEmpty()) {
            selectedDauMay = allDauMay.get(0);
        }
        assemblyList.clear();
        if (isEditMode) loadAssembly(originalDoanTau.getMaDoanTau());
        else            refreshAssemblyPanel();
    }
}
