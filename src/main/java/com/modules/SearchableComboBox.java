package com.modules;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * Generic searchable dropdown component.
 *
 * <ul>
 *   <li>Shows a styled text field for typing.</li>
 *   <li>As the user types, a popup list appears below with matching items.</li>
 *   <li>Arrow keys navigate the list; Enter / mouse-click confirm selection.</li>
 *   <li>Escape or clicking outside dismisses the popup without changing the selection.</li>
 * </ul>
 *
 * @param <T> the type of items managed by this component
 */
public class SearchableComboBox<T> extends JPanel {

    // ===== Design tokens (matches the rest of the app) =====
    private static final Color OUTLINE       = new Color(0xDE, 0xE3, 0xE8);
    private static final Color PRIMARY       = new Color(0x00, 0x5D, 0x90);
    private static final Color PRIMARY_LIGHT = new Color(0xE3, 0xF2, 0xFD);
    private static final Color ON_SURFACE    = new Color(0x1A, 0x1D, 0x21);
    private static final Color ON_SURF_VAR   = new Color(0x5F, 0x67, 0x70);
    private static final Color PLACEHOLDER   = new Color(0x9E, 0xA7, 0xB0);

    private static final Font FONT_INPUT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_ITEM  = new Font("Segoe UI", Font.PLAIN, 13);

    private static final int ROW_HEIGHT   = 38;
    private static final int MAX_VISIBLE  = 7;

    // ===== Delegates =====
    private final Function<T, String>    displayFn;
    private final BiPredicate<T, String> matchFn;

    // ===== State =====
    private List<T>  allItems     = new ArrayList<>();
    private T        selectedItem = null;
    private boolean  updating     = false;
    private String   placeholder  = "";
    private Runnable onChanged;

    // ===== Widgets =====
    private final JTextField          txtSearch;
    private final DefaultListModel<T> listModel;
    private final JList<T>            listWidget;
    private final JPopupMenu          popup;

    // ====================================================================
    //  CONSTRUCTOR
    // ====================================================================

    /**
     * @param displayFn converts an item to the string shown in the text field and popup list
     * @param matchFn   (item, lowerCaseQuery) → true if item matches the query
     */
    public SearchableComboBox(Function<T, String> displayFn,
                              BiPredicate<T, String> matchFn) {
        this.displayFn = displayFn;
        this.matchFn   = matchFn;

        setLayout(new BorderLayout());
        setOpaque(false);

        // ── Text field ──────────────────────────────────────────────────
        txtSearch = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !hasFocus() && !placeholder.isEmpty()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setColor(PLACEHOLDER);
                    g2.setFont(FONT_INPUT);
                    Insets ins = getInsets();
                    g2.drawString(placeholder, ins.left, getHeight() / 2 + 5);
                    g2.dispose();
                }
            }
        };
        txtSearch.setFont(FONT_INPUT);
        txtSearch.setBackground(Color.WHITE);
        txtSearch.setForeground(ON_SURFACE);
        applyNormalBorder();

        // ── List inside popup ────────────────────────────────────────────
        listModel  = new DefaultListModel<>();
        listWidget = new JList<>(listModel);
        listWidget.setFont(FONT_ITEM);
        listWidget.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listWidget.setFixedCellHeight(ROW_HEIGHT);
        listWidget.setCellRenderer(new ItemRenderer());
        listWidget.setFocusable(false);
        listWidget.setBackground(Color.WHITE);

        JScrollPane scroll = new JScrollPane(listWidget);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(ROW_HEIGHT);

        // ── Popup ────────────────────────────────────────────────────────
        popup = new JPopupMenu();
        popup.setLayout(new BorderLayout());
        popup.setBorder(BorderFactory.createLineBorder(OUTLINE, 1));
        popup.setFocusable(false);
        popup.add(scroll, BorderLayout.CENTER);

        // ── Wire listeners ───────────────────────────────────────────────
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { scheduleFilter(); }
            @Override public void removeUpdate(DocumentEvent e)  { scheduleFilter(); }
            @Override public void changedUpdate(DocumentEvent e) {}
        });

        txtSearch.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                applyFocusBorder();
                txtSearch.repaint();
                scheduleFilter(); // show list on focus
            }
            @Override
            public void focusLost(FocusEvent e) {
                applyNormalBorder();
                txtSearch.repaint();
                // If user tabbed away without picking → restore selected display text
                SwingUtilities.invokeLater(() -> {
                    if (!popup.isVisible()) return;
                    popup.setVisible(false);
                });
            }
        });

        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_DOWN -> {
                        if (!popup.isVisible()) { scheduleFilter(); return; }
                        int i = listWidget.getSelectedIndex();
                        if (i < listModel.size() - 1) {
                            listWidget.setSelectedIndex(i + 1);
                            listWidget.ensureIndexIsVisible(i + 1);
                        }
                    }
                    case KeyEvent.VK_UP -> {
                        int i = listWidget.getSelectedIndex();
                        if (i > 0) {
                            listWidget.setSelectedIndex(i - 1);
                            listWidget.ensureIndexIsVisible(i - 1);
                        }
                    }
                    case KeyEvent.VK_ENTER  -> confirmSelection();
                    case KeyEvent.VK_ESCAPE -> {
                        popup.setVisible(false);
                        // restore previous text
                        restoreDisplay();
                    }
                }
            }
        });

        listWidget.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { confirmSelection(); }
        });

        add(txtSearch, BorderLayout.CENTER);
    }

    // ====================================================================
    //  PUBLIC API
    // ====================================================================

    /** Replace the full item list. */
    public void setItems(List<T> items) {
        allItems = new ArrayList<>(items);
    }

    /** Programmatically select an item (e.g., pre-fill in edit mode). */
    public void selectItem(T item) {
        selectedItem = item;
        restoreDisplay();
        popup.setVisible(false);
    }

    /** Returns the currently selected item, or {@code null} if nothing is confirmed. */
    public T getSelectedItem() { return selectedItem; }

    /** Set placeholder text shown when the field is empty and unfocused. */
    public void setPlaceholder(String ph) {
        this.placeholder = ph;
        txtSearch.repaint();
    }

    /** Callback fired when the user confirms a selection. */
    public void setOnChanged(Runnable r) { this.onChanged = r; }

    /** Clears the current selection and empties the text field. */
    public void clearSelection() {
        selectedItem = null;
        updating = true;
        txtSearch.setText("");
        updating = false;
        popup.setVisible(false);
    }

    // ====================================================================
    //  INTERNAL HELPERS
    // ====================================================================

    private void scheduleFilter() {
        if (updating) return;
        SwingUtilities.invokeLater(this::doFilter);
    }

    private void doFilter() {
        if (updating) return;
        selectedItem = null; // typing invalidates confirmed selection

        String q = txtSearch.getText().trim().toLowerCase();
        listModel.clear();
        for (T item : allItems) {
            if (q.isEmpty() || matchFn.test(item, q)) listModel.addElement(item);
        }

        if (listModel.isEmpty()) {
            popup.setVisible(false);
            return;
        }

        if (!isShowing()) return;

        int rows  = Math.min(listModel.size(), MAX_VISIBLE);
        int popW  = Math.max(getWidth(), 240);
        int popH  = rows * ROW_HEIGHT + 2;

        popup.setPreferredSize(new Dimension(popW, popH));
        popup.show(SearchableComboBox.this, 0, getHeight());

        // Pre-select first entry for keyboard convenience
        if (listModel.size() > 0) listWidget.setSelectedIndex(0);
    }

    private void confirmSelection() {
        T item = listWidget.getSelectedValue();
        if (item == null) return;
        selectedItem = item;
        popup.setVisible(false);
        restoreDisplay();
        if (onChanged != null) onChanged.run();
    }

    /** Restore the text field to the display text of the current selection. */
    private void restoreDisplay() {
        updating = true;
        txtSearch.setText(selectedItem == null ? "" : displayFn.apply(selectedItem));
        txtSearch.setForeground(selectedItem == null ? ON_SURF_VAR : ON_SURFACE);
        updating = false;
        txtSearch.repaint();
    }

    private void applyNormalBorder() {
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));
    }

    private void applyFocusBorder() {
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY, 2),
                new EmptyBorder(7, 11, 7, 11)
        ));
    }

    // ====================================================================
    //  CELL RENDERER
    // ====================================================================

    private class ItemRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            @SuppressWarnings("unchecked") T item = (T) value;
            JLabel lbl = (JLabel) super.getListCellRendererComponent(
                    list, displayFn.apply(item), index, isSelected, cellHasFocus);
            lbl.setFont(FONT_ITEM);
            lbl.setBorder(new EmptyBorder(4, 14, 4, 14));
            if (isSelected) {
                lbl.setBackground(PRIMARY_LIGHT);
                lbl.setForeground(PRIMARY);
            } else {
                lbl.setBackground(Color.WHITE);
                lbl.setForeground(ON_SURFACE);
            }
            return lbl;
        }
    }
}
