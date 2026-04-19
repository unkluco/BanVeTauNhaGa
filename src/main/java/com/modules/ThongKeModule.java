package com.modules;

import com.connectDB.ConnectDB;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.*;
import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.labels.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.*;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.category.*;
import org.jfree.data.general.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.awt.geom.Ellipse2D;
import java.math.*;
import java.sql.*;
import java.text.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.*;

public class ThongKeModule extends JPanel implements AppModule {

    private Consumer<Object> callback;

    // Controls
    private JComboBox<Integer> cboYear;
    private JButton btnExportReport;
    private volatile boolean loadingData = false;

    // Chart panels
    private ChartPanel chartDoanhThuThang;
    private ChartPanel chartVeThang;
    private ChartPanel chartTrangThaiVe;
    private ChartPanel chartDoanhThuTuyen;
    private ChartPanel chartTopNhanVien;
    private ChartPanel chartHoaDonNgay;

    // Summary labels
    private JLabel lblTongDoanhThu;
    private JLabel lblTongVe;
    private JLabel lblTongHoaDon;
    private JLabel lblVeHuy;

    // ── Colors ────────────────────────────────────────────────────────────
    private static final Color PRIMARY       = new Color(0x00, 0x5D, 0x90);
    private static final Color ACCENT1       = new Color(0x00, 0x96, 0x88);
    private static final Color ACCENT2       = new Color(0xFF, 0x65, 0x00);
    private static final Color ACCENT3       = new Color(0x9C, 0x27, 0xB0);
    private static final Color ACCENT4       = new Color(0xF4, 0x43, 0x36);
    private static final Color SURFACE       = new Color(0xF7, 0xF9, 0xFB);
    private static final Color CARD_BG       = Color.WHITE;
    private static final Color BORDER_COLOR  = new Color(0xE2, 0xE8, 0xF0);
    private static final Color TEXT_DARK     = new Color(0x19, 0x1C, 0x1E);
    private static final Color TEXT_MUTED    = new Color(0x64, 0x74, 0x8B);
    private static final Color GRID_COLOR    = new Color(0xEE, 0xF2, 0xF7);
    private static final Color EXPORT_START  = new Color(0x00, 0x6A, 0xA8);
    private static final Color EXPORT_END    = new Color(0x00, 0x85, 0x7E);
    private static final int REPORT_CHART_TOP_ROW = 5;
    private static final int REPORT_CHART_HEIGHT = 15;
    private static final int REPORT_TABLE_GAP_ROWS = 2;

    private static final Color[] PALETTE = {
        new Color(0x00, 0x5D, 0x90),
        new Color(0x00, 0x96, 0x88),
        new Color(0xFF, 0x65, 0x00),
        new Color(0x9C, 0x27, 0xB0),
        new Color(0xF4, 0x43, 0x36),
        new Color(0xFF, 0xC1, 0x07),
        new Color(0x4C, 0xAF, 0x50),
        new Color(0x2B, 0x96, 0xCC),
    };

    // ── Constructor ───────────────────────────────────────────────────────
    public ThongKeModule() {
        setLayout(new BorderLayout());
        setBackground(SURFACE);
        buildUI();
    }

    // ── UI Construction ───────────────────────────────────────────────────

    private void buildUI() {
        add(buildHeaderPanel(), BorderLayout.NORTH);

        JPanel mainArea = new JPanel(new BorderLayout());
        mainArea.setOpaque(false);
        mainArea.add(buildSummaryRow(), BorderLayout.NORTH);

        JPanel chartsGrid = buildChartsGrid();
        JScrollPane scroll = new JScrollPane(chartsGrid);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        mainArea.add(scroll, BorderLayout.CENTER);

        add(mainArea, BorderLayout.CENTER);

        SwingUtilities.invokeLater(this::loadAllData);
    }

    private JPanel buildHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            new EmptyBorder(14, 24, 14, 24)
        ));

        JLabel lblTitle = new JLabel("Thống kê & Báo cáo");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(PRIMARY);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setOpaque(false);

        JLabel lblYear = new JLabel("Năm:");
        lblYear.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblYear.setForeground(TEXT_MUTED);

        int cur = LocalDate.now().getYear();
        Integer[] years = new Integer[6];
        for (int i = 0; i < 6; i++) years[i] = cur - i;
        cboYear = new JComboBox<>(years);
        cboYear.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cboYear.setPreferredSize(new Dimension(90, 32));
        cboYear.addActionListener(e -> loadAllData());

        btnExportReport = createExportButton("Xuất báo cáo");
        btnExportReport.addActionListener(e -> openExportReportDialog());

        controls.add(lblYear);
        controls.add(cboYear);
        controls.add(Box.createHorizontalStrut(4));
        controls.add(btnExportReport);

        header.add(lblTitle, BorderLayout.WEST);
        header.add(controls, BorderLayout.EAST);
        return header;
    }

    private JPanel buildSummaryRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(16, 24, 8, 24));

        lblTongDoanhThu = new JLabel("...");
        lblTongVe       = new JLabel("...");
        lblTongHoaDon   = new JLabel("...");
        lblVeHuy        = new JLabel("...");

        row.add(buildKpiCard("Tổng doanh thu (năm)", lblTongDoanhThu, PRIMARY));
        row.add(buildKpiCard("Vé đã bán (năm)",      lblTongVe,       ACCENT1));
        row.add(buildKpiCard("Hóa đơn (năm)",             lblTongHoaDon,   ACCENT2));
        row.add(buildKpiCard("Vé đã hủy (năm)",       lblVeHuy,        ACCENT4));
        return row;
    }

    private JPanel buildKpiCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = createRoundCard();
        card.setLayout(new BorderLayout());

        JPanel accent = new JPanel();
        accent.setBackground(accentColor);
        accent.setPreferredSize(new Dimension(5, 0));
        card.add(accent, BorderLayout.WEST);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel lTitle = new JLabel(title);
        lTitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lTitle.setForeground(TEXT_MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        valueLabel.setForeground(accentColor);

        content.add(lTitle);
        content.add(Box.createVerticalStrut(6));
        content.add(valueLabel);
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildChartsGrid() {
        JPanel grid = new JPanel(new GridLayout(3, 2, 14, 14));
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(8, 24, 24, 24));

        // 1 ─ Monthly revenue bar chart
        chartDoanhThuThang = emptyChartPanel(emptyBarChart("Triệu ₫", "Tháng"));
        grid.add(buildChartCard("Doanh thu theo tháng", chartDoanhThuThang));

        // 2 ─ Monthly tickets grouped bar
        chartVeThang = emptyChartPanel(emptyBarChart("Số vé", "Tháng"));
        grid.add(buildChartCard("Số vé bán / hủy theo tháng", chartVeThang));

        // 3 ─ Ticket status ring chart
        chartTrangThaiVe = emptyChartPanel(emptyRingChart());
        grid.add(buildChartCard("Tỷ lệ trạng thái vé (toàn thời gian)", chartTrangThaiVe));

        // 4 ─ Revenue by route horizontal bar
        chartDoanhThuTuyen = emptyChartPanel(emptyHorizontalBarChart("Triệu ₫", "Tuyến"));
        grid.add(buildChartCard("Top tuyến theo doanh thu", chartDoanhThuTuyen));

        // 5 ─ Top employees bar chart
        chartTopNhanVien = emptyChartPanel(emptyBarChart("Triệu ₫", "Nhân viên"));
        grid.add(buildChartCard("Top 5 nhân viên (doanh thu năm)", chartTopNhanVien));

        // 6 ─ Daily invoices current month line chart
        chartHoaDonNgay = emptyChartPanel(emptyLineChart("Số hóa đơn", "Ngày"));
        grid.add(buildChartCard("Hóa đơn theo ngày (tháng hiện tại)", chartHoaDonNgay));

        return grid;
    }

    private JPanel buildChartCard(String title, ChartPanel chartPanel) {
        JPanel card = createRoundCard();
        card.setLayout(new BorderLayout());
        card.setPreferredSize(new Dimension(420, 320));

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(TEXT_DARK);
        lbl.setBorder(new EmptyBorder(14, 16, 8, 16));
        card.add(lbl, BorderLayout.NORTH);
        card.add(chartPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createRoundCard() {
        return new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
            }
        };
    }

    private JButton createExportButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                ButtonModel model = getModel();
                boolean pressed = model.isPressed();
                boolean hover = model.isRollover();
                Color c1;
                Color c2;
                if (!isEnabled()) {
                    c1 = new Color(0xA8, 0xB6, 0xC4);
                    c2 = new Color(0x93, 0xA2, 0xB2);
                } else if (pressed) {
                    c1 = EXPORT_END.darker();
                    c2 = EXPORT_START.darker();
                } else if (hover) {
                    c1 = EXPORT_START.brighter();
                    c2 = EXPORT_END.brighter();
                } else {
                    c1 = EXPORT_START;
                    c2 = EXPORT_END;
                }

                g2.setPaint(new GradientPaint(0, 0, c1, getWidth(), getHeight(), c2));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(255, 255, 255, 95));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setText(text);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorder(new EmptyBorder(8, 18, 8, 18));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setRolloverEnabled(true);
        button.setPreferredSize(new Dimension(146, 34));
        return button;
    }

    // ── Empty chart factories ─────────────────────────────────────────────

    private JFreeChart emptyBarChart(String valueLabel, String catLabel) {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        JFreeChart c = ChartFactory.createBarChart(
            "", catLabel, valueLabel, ds, PlotOrientation.VERTICAL, false, false, false);
        c.setBackgroundPaint(null);
        return c;
    }

    private JFreeChart emptyHorizontalBarChart(String valueLabel, String catLabel) {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        JFreeChart c = ChartFactory.createBarChart(
            "", catLabel, valueLabel, ds, PlotOrientation.HORIZONTAL, false, false, false);
        c.setBackgroundPaint(null);
        return c;
    }

    private JFreeChart emptyLineChart(String valueLabel, String catLabel) {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        JFreeChart c = ChartFactory.createLineChart(
            "", catLabel, valueLabel, ds, PlotOrientation.VERTICAL, false, false, false);
        c.setBackgroundPaint(null);
        return c;
    }

    private JFreeChart emptyRingChart() {
        DefaultPieDataset<String> ds = new DefaultPieDataset<>();
        JFreeChart c = ChartFactory.createRingChart("", ds, true, false, false);
        c.setBackgroundPaint(null);
        return c;
    }

    private ChartPanel emptyChartPanel(JFreeChart chart) {
        ChartPanel cp = new ChartPanel(chart);
        cp.setOpaque(false);
        cp.setBorder(new EmptyBorder(0, 8, 10, 10));
        cp.setBackground(CARD_BG);
        return cp;
    }

    // ── Data loading ──────────────────────────────────────────────────────

    private void loadAllData() {
        if (cboYear == null || cboYear.getSelectedItem() == null) return;
        int year = (Integer) cboYear.getSelectedItem();
        setLoadingState(true);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                loadDoanhThuThang(year);
                loadVeThang(year);
                loadTrangThaiVe();
                loadDoanhThuTuyen();
                loadTopNhanVien(year);
                loadHoaDonNgay();
                loadSummaryStats(year);
                return null;
            }

            @Override
            protected void done() {
                setLoadingState(false);
            }
        };
        worker.execute();
    }

    private void setLoadingState(boolean loading) {
        loadingData = loading;
        if (cboYear != null) cboYear.setEnabled(!loading);
        if (btnExportReport != null) btnExportReport.setEnabled(!loading);
    }

    // Chart 1: Monthly revenue
    private void loadDoanhThuThang(int year) {
        Connection con = ConnectDB.getCon();
        if (con == null) return;

        double[] vals = new double[12];
        String sql =
            "SELECT MONTH(hd.ngayLap) AS thang, COALESCE(SUM(ct.giaTien),0) AS tong " +
            "FROM HoaDon hd JOIN ChiTietHoaDon ct ON hd.maHoaDon = ct.maHoaDon " +
            "WHERE YEAR(hd.ngayLap) = ? " +
            "GROUP BY MONTH(hd.ngayLap)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, year);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int m = rs.getInt("thang");
                double t = rs.getBigDecimal("tong").doubleValue() / 1_000_000.0;
                if (m >= 1 && m <= 12) vals[m - 1] = t;
            }
        } catch (SQLException e) {
            System.err.println("[ThongKe] loadDoanhThuThang: " + e.getMessage());
        }

        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        String[] months = {"T1","T2","T3","T4","T5","T6","T7","T8","T9","T10","T11","T12"};
        for (int i = 0; i < 12; i++) ds.addValue(vals[i], "Doanh thu", months[i]);

        JFreeChart chart = ChartFactory.createBarChart(
            "", "Tháng", "Triệu ₫", ds,
            PlotOrientation.VERTICAL, false, true, false);
        applyBarStyle(chart, false, new Color[]{PALETTE[0]});
        chart.getPlot().setBackgroundPaint(CARD_BG);
        SwingUtilities.invokeLater(() -> chartDoanhThuThang.setChart(chart));
    }

    // Chart 2: Tickets sold/cancelled per month
    private void loadVeThang(int year) {
        Connection con = ConnectDB.getCon();
        if (con == null) return;

        double[] ban = new double[12];
        double[] huy = new double[12];
        String sql =
            "SELECT MONTH(hd.ngayLap) AS thang, v.trangThai, COUNT(v.maVe) AS soVe " +
            "FROM Ve v " +
            "JOIN ChiTietHoaDon ct ON v.maVe = ct.maVe " +
            "JOIN HoaDon hd ON ct.maHoaDon = hd.maHoaDon " +
            "WHERE YEAR(hd.ngayLap) = ? " +
            "GROUP BY MONTH(hd.ngayLap), v.trangThai";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, year);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int m = rs.getInt("thang");
                String tt = rs.getString("trangThai");
                int cnt = rs.getInt("soVe");
                if (m >= 1 && m <= 12) {
                    if ("DA_BAN".equalsIgnoreCase(tt))  ban[m - 1] = cnt;
                    else if ("DA_HUY".equalsIgnoreCase(tt)) huy[m - 1] = cnt;
                }
            }
        } catch (SQLException e) {
            System.err.println("[ThongKe] loadVeThang: " + e.getMessage());
        }

        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        String[] months = {"T1","T2","T3","T4","T5","T6","T7","T8","T9","T10","T11","T12"};
        for (int i = 0; i < 12; i++) {
            ds.addValue(ban[i], "Đã bán", months[i]);
            ds.addValue(huy[i], "Đã hủy", months[i]);
        }

        JFreeChart chart = ChartFactory.createBarChart(
            "", "Tháng", "Số vé", ds,
            PlotOrientation.VERTICAL, true, true, false);
        applyBarStyle(chart, true, new Color[]{PALETTE[0], PALETTE[4]});
        chart.getPlot().setBackgroundPaint(CARD_BG);
        if (chart.getLegend() != null) {
            chart.getLegend().setBackgroundPaint(CARD_BG);
            chart.getLegend().setItemFont(new Font("Segoe UI", Font.PLAIN, 11));
        }
        SwingUtilities.invokeLater(() -> chartVeThang.setChart(chart));
    }

    // Chart 3: Ticket status distribution (ring chart)
    private void loadTrangThaiVe() {
        Connection con = ConnectDB.getCon();
        if (con == null) return;

        DefaultPieDataset<String> ds = new DefaultPieDataset<>();
        String sql = "SELECT trangThai, COUNT(*) AS soLuong FROM Ve GROUP BY trangThai";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String tt = rs.getString("trangThai");
                int cnt = rs.getInt("soLuong");
                String label = "DA_BAN".equalsIgnoreCase(tt) ? "Đã bán" : "Đã hủy";
                ds.setValue(label, cnt);
            }
        } catch (SQLException e) {
            System.err.println("[ThongKe] loadTrangThaiVe: " + e.getMessage());
        }

        JFreeChart chart = ChartFactory.createRingChart("", ds, true, true, false);
        chart.setBackgroundPaint(null);
        if (chart.getLegend() != null) {
            chart.getLegend().setBackgroundPaint(CARD_BG);
            chart.getLegend().setItemFont(new Font("Segoe UI", Font.PLAIN, 11));
        }
        RingPlot plot = (RingPlot) chart.getPlot();
        plot.setBackgroundPaint(CARD_BG);
        plot.setOutlinePaint(null);
        plot.setSectionPaint("Đã bán", PALETTE[0]);
        plot.setSectionPaint("Đã hủy", PALETTE[4]);
        plot.setDefaultSectionOutlinePaint(CARD_BG);
        plot.setDefaultSectionOutlineStroke(new BasicStroke(3f));
        plot.setSectionDepth(0.40);
        plot.setSeparatorsVisible(false);
        plot.setLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
        plot.setLabelPaint(TEXT_DARK);
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator(
            "{0}\n{1} vé ({2})",
            NumberFormat.getIntegerInstance(),
            new DecimalFormat("0.0%")
        ));
        SwingUtilities.invokeLater(() -> chartTrangThaiVe.setChart(chart));
    }

    // Chart 4: Revenue by route (horizontal)
    private void loadDoanhThuTuyen() {
        Connection con = ConnectDB.getCon();
        if (con == null) return;

        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        String sql =
            "SELECT TOP 7 " +
            "    g1.tenGa + N' → ' + g2.tenGa AS tenTuyen, " +
            "    SUM(ct.giaTien) AS tong " +
            "FROM ChiTietHoaDon ct " +
            "JOIN Ve v ON ct.maVe = v.maVe " +
            "JOIN Lich l ON v.maLich = l.maLich " +
            "JOIN Tuyen t ON l.maTuyen = t.maTuyen " +
            "JOIN Ga g1 ON t.gaDi = g1.maGa " +
            "JOIN Ga g2 ON t.gaDen = g2.maGa " +
            "GROUP BY g1.tenGa, g2.tenGa " +
            "ORDER BY tong DESC";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String tuyen = rs.getString("tenTuyen");
                double tong = rs.getBigDecimal("tong").doubleValue() / 1_000_000.0;
                ds.addValue(tong, "Doanh thu", tuyen);
            }
        } catch (SQLException e) {
            System.err.println("[ThongKe] loadDoanhThuTuyen: " + e.getMessage());
        }

        JFreeChart chart = ChartFactory.createBarChart(
            "", "Tuyến", "Triệu ₫", ds,
            PlotOrientation.HORIZONTAL, false, true, false);
        applyBarStyle(chart, false, new Color[]{PALETTE[1]});
        chart.getPlot().setBackgroundPaint(CARD_BG);
        SwingUtilities.invokeLater(() -> chartDoanhThuTuyen.setChart(chart));
    }

    // Chart 5: Top employees by revenue
    private void loadTopNhanVien(int year) {
        Connection con = ConnectDB.getCon();
        if (con == null) return;

        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        String sql =
            "SELECT TOP 5 nv.hoTen, SUM(ct.giaTien) AS tong " +
            "FROM HoaDon hd " +
            "JOIN NhanVien nv ON hd.maNV = nv.maNV " +
            "JOIN ChiTietHoaDon ct ON hd.maHoaDon = ct.maHoaDon " +
            "WHERE YEAR(hd.ngayLap) = ? " +
            "GROUP BY nv.hoTen " +
            "ORDER BY tong DESC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, year);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String hoTen = rs.getString("hoTen");
                double tong = rs.getBigDecimal("tong").doubleValue() / 1_000_000.0;
                ds.addValue(tong, "Doanh thu", hoTen);
            }
        } catch (SQLException e) {
            System.err.println("[ThongKe] loadTopNhanVien: " + e.getMessage());
        }

        JFreeChart chart = ChartFactory.createBarChart(
            "", "Nhân viên", "Triệu ₫", ds,
            PlotOrientation.VERTICAL, false, true, false);
        applyBarStyle(chart, false, new Color[]{PALETTE[2]});
        chart.getPlot().setBackgroundPaint(CARD_BG);

        // Rotate category labels for long names
        CategoryPlot plot = chart.getCategoryPlot();
        plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_45);

        SwingUtilities.invokeLater(() -> chartTopNhanVien.setChart(chart));
    }

    // Chart 6: Daily invoice count for current month (line chart)
    private void loadHoaDonNgay() {
        Connection con = ConnectDB.getCon();
        if (con == null) return;

        int daysInMonth = LocalDate.now().lengthOfMonth();
        int[] vals = new int[daysInMonth];
        String sql =
            "SELECT DAY(ngayLap) AS ngay, COUNT(*) AS soHD " +
            "FROM HoaDon " +
            "WHERE MONTH(ngayLap) = MONTH(GETDATE()) AND YEAR(ngayLap) = YEAR(GETDATE()) " +
            "GROUP BY DAY(ngayLap)";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int d = rs.getInt("ngay");
                if (d >= 1 && d <= daysInMonth) vals[d - 1] = rs.getInt("soHD");
            }
        } catch (SQLException e) {
            System.err.println("[ThongKe] loadHoaDonNgay: " + e.getMessage());
        }

        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        for (int i = 0; i < daysInMonth; i++) {
            ds.addValue(vals[i], "Hóa đơn", String.valueOf(i + 1));
        }

        JFreeChart chart = ChartFactory.createLineChart(
            "", "Ngày", "Số hóa đơn", ds,
            PlotOrientation.VERTICAL, false, true, false);
        applyLineStyle(chart, PALETTE[3]);
        chart.getPlot().setBackgroundPaint(CARD_BG);
        SwingUtilities.invokeLater(() -> chartHoaDonNgay.setChart(chart));
    }

    // Summary KPI stats
    private void loadSummaryStats(int year) {
        Connection con = ConnectDB.getCon();
        if (con == null) {
            SwingUtilities.invokeLater(() -> {
                lblTongDoanhThu.setText("Không có dữ liệu");
                lblTongVe.setText("-");
                lblTongHoaDon.setText("-");
                lblVeHuy.setText("-");
            });
            return;
        }

        BigDecimal doanhThu = BigDecimal.ZERO;
        int tongVe = 0, tongHD = 0, veHuy = 0;

        String sqlDT =
            "SELECT COALESCE(SUM(ct.giaTien),0) AS tong " +
            "FROM HoaDon hd JOIN ChiTietHoaDon ct ON hd.maHoaDon = ct.maHoaDon " +
            "WHERE YEAR(hd.ngayLap) = ?";
        try (PreparedStatement ps = con.prepareStatement(sqlDT)) {
            ps.setInt(1, year);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) doanhThu = rs.getBigDecimal("tong");
        } catch (SQLException e) {
            System.err.println("[ThongKe] summary DT: " + e.getMessage());
        }

        String sqlVe =
            "SELECT v.trangThai, COUNT(*) AS cnt " +
            "FROM Ve v JOIN ChiTietHoaDon ct ON v.maVe = ct.maVe " +
            "JOIN HoaDon hd ON ct.maHoaDon = hd.maHoaDon " +
            "WHERE YEAR(hd.ngayLap) = ? GROUP BY v.trangThai";
        try (PreparedStatement ps = con.prepareStatement(sqlVe)) {
            ps.setInt(1, year);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int c = rs.getInt("cnt");
                tongVe += c;
                if ("DA_HUY".equalsIgnoreCase(rs.getString("trangThai"))) veHuy = c;
            }
        } catch (SQLException e) {
            System.err.println("[ThongKe] summary Ve: " + e.getMessage());
        }

        String sqlHD = "SELECT COUNT(*) AS cnt FROM HoaDon WHERE YEAR(ngayLap) = ?";
        try (PreparedStatement ps = con.prepareStatement(sqlHD)) {
            ps.setInt(1, year);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) tongHD = rs.getInt("cnt");
        } catch (SQLException e) {
            System.err.println("[ThongKe] summary HD: " + e.getMessage());
        }

        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        String sDT  = nf.format(doanhThu) + " ₫";
        String sVe  = nf.format(tongVe)   + " vé";
        String sHD  = nf.format(tongHD)   + " đơn";
        String sHuy = nf.format(veHuy)    + " vé";

        SwingUtilities.invokeLater(() -> {
            lblTongDoanhThu.setText(sDT);
            lblTongVe.setText(sVe);
            lblTongHoaDon.setText(sHD);
            lblVeHuy.setText(sHuy);
        });
    }

    // ── Export report ─────────────────────────────────────────────────────

    private void openExportReportDialog() {
        if (loadingData) {
            JOptionPane.showMessageDialog(this,
                "Dữ liệu đang được cập nhật. Vui lòng đợi vài giây rồi thử lại.",
                "Đang tải dữ liệu",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Integer yearValue = (Integer) cboYear.getSelectedItem();
        if (yearValue == null) return;
        int year = yearValue;

        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, "Xuất báo cáo thống kê", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CARD_BG);
        root.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(16, 18, 14, 14));

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);

        JLabel lblTitle = new JLabel("Xuất báo cáo thống kê");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(PRIMARY);

        JLabel lblDesc = new JLabel("Tạo file Excel (.xlsx) có bảng dữ liệu và biểu đồ trực quan cho năm " + year + ".");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDesc.setForeground(TEXT_MUTED);

        titleBox.add(lblTitle);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(lblDesc);

        JButton btnClose = createOutlineButton("Đóng");
        btnClose.addActionListener(e -> dialog.dispose());

        header.add(titleBox, BorderLayout.CENTER);
        header.add(btnClose, BorderLayout.EAST);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(0, 18, 6, 18));

        JLabel lblFormat = new JLabel("Định dạng xuất");
        lblFormat.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblFormat.setForeground(TEXT_MUTED);

        JComboBox<String> cboFormat = new JComboBox<>(new String[]{
            "Excel (.xlsx) kèm biểu đồ tự động"
        });
        cboFormat.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cboFormat.setPreferredSize(new Dimension(0, 36));
        cboFormat.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        content.add(lblFormat);
        content.add(Box.createVerticalStrut(6));
        content.add(cboFormat);
        content.add(Box.createVerticalStrut(14));

        JCheckBox chkSummary = createExportCheckBox(true);
        JCheckBox chkMonthlyRevenue = createExportCheckBox(true);
        JCheckBox chkMonthlyTickets = createExportCheckBox(true);
        JCheckBox chkRoutes = createExportCheckBox(true);
        JCheckBox chkEmployees = createExportCheckBox(true);
        JCheckBox chkDailyInvoices = createExportCheckBox(true);

        JPanel optionGrid = new JPanel(new GridLayout(3, 2, 10, 10));
        optionGrid.setOpaque(false);
        optionGrid.add(createExportOptionCard(chkSummary, "Tổng quan KPI", "Doanh thu, vé, hóa đơn, vé hủy trong năm."));
        optionGrid.add(createExportOptionCard(chkMonthlyRevenue, "Doanh thu theo tháng", "Theo dõi biến động doanh thu toàn năm."));
        optionGrid.add(createExportOptionCard(chkMonthlyTickets, "Vé bán / hủy theo tháng", "So sánh số lượng bán và hủy từng tháng."));
        optionGrid.add(createExportOptionCard(chkRoutes, "Top tuyến doanh thu", "Xếp hạng tuyến có doanh thu cao nhất."));
        optionGrid.add(createExportOptionCard(chkEmployees, "Top nhân viên", "Xếp hạng nhân viên theo doanh thu năm."));
        optionGrid.add(createExportOptionCard(chkDailyInvoices, "Hóa đơn theo ngày", "Số hóa đơn từng ngày trong tháng hiện tại."));

        content.add(optionGrid);
        content.add(Box.createVerticalStrut(12));

        JLabel lblHint = new JLabel("Mẹo: Có thể bỏ chọn các mục không cần để báo cáo gọn hơn.");
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblHint.setForeground(TEXT_MUTED);
        content.add(lblHint);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(4, 12, 10, 16));

        JButton btnCancel = createOutlineButton("Hủy");
        JButton btnExport = createExportButton("Xuất file");
        btnExport.setPreferredSize(new Dimension(154, 36));

        btnCancel.addActionListener(e -> dialog.dispose());
        btnExport.addActionListener(e -> {
            ReportExportOptions options = new ReportExportOptions(
                chkSummary.isSelected(),
                chkMonthlyRevenue.isSelected(),
                chkMonthlyTickets.isSelected(),
                chkRoutes.isSelected(),
                chkEmployees.isSelected(),
                chkDailyInvoices.isSelected()
            );
            if (!options.hasAnySection()) {
                JOptionPane.showMessageDialog(dialog,
                    "Vui lòng chọn ít nhất một nội dung để xuất báo cáo.",
                    "Thiếu nội dung",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (cboFormat.getSelectedIndex() != 0) {
                JOptionPane.showMessageDialog(dialog,
                    "Định dạng hiện tại chưa được hỗ trợ.",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            File outputFile = chooseReportOutputFile(year);
            if (outputFile == null) return;

            startExportReport(dialog, btnExport, btnCancel, outputFile, year, options);
        });

        footer.add(btnCancel);
        footer.add(btnExport);

        root.add(header, BorderLayout.NORTH);
        root.add(content, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        dialog.setContentPane(ThemNhanVienDialog.buildShadowWrapper(root));
        dialog.setSize(770, 520);
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }

    private JCheckBox createExportCheckBox(boolean selected) {
        JCheckBox cb = new JCheckBox();
        cb.setSelected(selected);
        cb.setOpaque(false);
        cb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return cb;
    }

    private JPanel createExportOptionCard(JCheckBox checkBox, String title, String description) {
        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setBackground(new Color(0xF8, 0xFA, 0xFD));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(10, 10, 10, 10)
        ));

        JPanel textBox = new JPanel();
        textBox.setLayout(new BoxLayout(textBox, BoxLayout.Y_AXIS));
        textBox.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(TEXT_DARK);

        JLabel lblDesc = new JLabel("<html><div style='width: 250px;'>" + description + "</div></html>");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDesc.setForeground(TEXT_MUTED);

        textBox.add(lblTitle);
        textBox.add(Box.createVerticalStrut(3));
        textBox.add(lblDesc);

        card.add(checkBox, BorderLayout.WEST);
        card.add(textBox, BorderLayout.CENTER);
        return card;
    }

    private JButton createOutlineButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(PRIMARY);
        button.setBackground(Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xC9, 0xD4, 0xE2), 1),
            new EmptyBorder(8, 16, 8, 16)
        ));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private File chooseReportOutputFile(int year) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn nơi lưu báo cáo thống kê");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileNameExtensionFilter("Excel Workbook (*.xlsx)", "xlsx"));

        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm");
        String fileName = "bao-cao-thong-ke-" + year + "-" + LocalDateTime.now().format(f) + ".xlsx";
        chooser.setSelectedFile(new File(fileName));

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return null;

        File selected = chooser.getSelectedFile();
        if (selected == null) return null;
        String name = selected.getName().toLowerCase(Locale.ROOT);
        if (!name.endsWith(".xlsx")) {
            File parent = selected.getParentFile();
            selected = parent == null
                ? new File(selected.getName() + ".xlsx")
                : new File(parent, selected.getName() + ".xlsx");
        }

        if (selected.exists()) {
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Tệp đã tồn tại. Bạn có muốn ghi đè không?",
                "Xác nhận ghi đè",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            if (confirm != JOptionPane.YES_OPTION) return null;
        }

        return selected;
    }

    private void startExportReport(
        JDialog dialog,
        JButton btnExport,
        JButton btnCancel,
        File outputFile,
        int year,
        ReportExportOptions options
    ) {
        btnExport.setEnabled(false);
        btnCancel.setEnabled(false);
        btnExport.setText("Đang xuất...");

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private Exception exportError;

            @Override
            protected Void doInBackground() {
                try {
                    writeXlsxReport(outputFile, year, options);
                } catch (Exception e) {
                    exportError = e;
                }
                return null;
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                btnExport.setEnabled(true);
                btnCancel.setEnabled(true);
                btnExport.setText("Xuất file");

                if (exportError != null) {
                    JOptionPane.showMessageDialog(dialog,
                        "Không thể xuất báo cáo:\n" + exportError.getMessage(),
                        "Xuất báo cáo thất bại",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }

                dialog.dispose();
                JOptionPane.showMessageDialog(
                    ThongKeModule.this,
                    "<html><b>Xuất báo cáo thành công.</b><br>Đã lưu tại:<br>"
                        + outputFile.getAbsolutePath() + "</html>",
                    "Hoàn tất xuất báo cáo",
                    JOptionPane.INFORMATION_MESSAGE
                );
            }
        };
        worker.execute();
    }

    private void writeXlsxReport(File outputFile, int year, ReportExportOptions options) throws Exception {
        Connection con = ConnectDB.getCon();
        if (con == null) {
            throw new IllegalStateException("Không có kết nối cơ sở dữ liệu.");
        }

        SummarySnapshot summary = options.includeSummary() ? fetchSummarySnapshot(year) : null;
        double[] monthlyRevenue = options.includeMonthlyRevenue() ? fetchMonthlyRevenue(year) : null;
        int[][] monthlyTickets = options.includeMonthlyTickets() ? fetchMonthlyTicketStats(year) : null;
        java.util.List<RouteRevenue> routes =
            options.includeTopRoutes() ? fetchTopRouteRevenue() : Collections.emptyList();
        java.util.List<EmployeeRevenue> employees =
            options.includeTopEmployees() ? fetchTopEmployeeRevenue(year) : Collections.emptyList();
        int[] invoicesDaily = options.includeDailyInvoices() ? fetchDailyInvoicesInCurrentMonth() : null;

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            ExcelStyles styles = createExcelStyles(workbook);

            if (options.includeSummary() && summary != null) {
                createSummarySheet(workbook, styles, year, summary);
            }
            if (options.includeMonthlyRevenue() && monthlyRevenue != null) {
                createMonthlyRevenueSheet(workbook, styles, year, monthlyRevenue);
            }
            if (options.includeMonthlyTickets() && monthlyTickets != null) {
                createMonthlyTicketSheet(workbook, styles, year, monthlyTickets[0], monthlyTickets[1]);
            }
            if (options.includeTopRoutes()) {
                createTopRoutesSheet(workbook, styles, year, routes);
            }
            if (options.includeTopEmployees()) {
                createTopEmployeesSheet(workbook, styles, year, employees);
            }
            if (options.includeDailyInvoices() && invoicesDaily != null) {
                createDailyInvoicesSheet(workbook, styles, invoicesDaily);
            }

            if (workbook.getNumberOfSheets() == 0) {
                XSSFSheet sheet = workbook.createSheet("BaoCao");
                Row row = sheet.createRow(0);
                Cell cell = row.createCell(0);
                cell.setCellValue("Không có dữ liệu để xuất.");
                cell.setCellStyle(styles.text());
                sheet.autoSizeColumn(0);
            }

            try (FileOutputStream out = new FileOutputStream(outputFile)) {
                workbook.write(out);
            }
        }
    }

    private SummarySnapshot fetchSummarySnapshot(int year) {
        Connection con = ConnectDB.getCon();
        if (con == null) return new SummarySnapshot(BigDecimal.ZERO, 0, 0, 0);

        BigDecimal doanhThu = BigDecimal.ZERO;
        int tongVe = 0;
        int tongHoaDon = 0;
        int veHuy = 0;

        String sqlDT =
            "SELECT COALESCE(SUM(ct.giaTien),0) AS tong " +
            "FROM HoaDon hd JOIN ChiTietHoaDon ct ON hd.maHoaDon = ct.maHoaDon " +
            "WHERE YEAR(hd.ngayLap) = ?";
        try (PreparedStatement ps = con.prepareStatement(sqlDT)) {
            ps.setInt(1, year);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                BigDecimal value = rs.getBigDecimal("tong");
                if (value != null) doanhThu = value;
            }
        } catch (SQLException e) {
            System.err.println("[ThongKe] export summary DT: " + e.getMessage());
        }

        String sqlVe =
            "SELECT v.trangThai, COUNT(*) AS cnt " +
            "FROM Ve v JOIN ChiTietHoaDon ct ON v.maVe = ct.maVe " +
            "JOIN HoaDon hd ON ct.maHoaDon = hd.maHoaDon " +
            "WHERE YEAR(hd.ngayLap) = ? GROUP BY v.trangThai";
        try (PreparedStatement ps = con.prepareStatement(sqlVe)) {
            ps.setInt(1, year);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int cnt = rs.getInt("cnt");
                tongVe += cnt;
                if ("DA_HUY".equalsIgnoreCase(rs.getString("trangThai"))) veHuy += cnt;
            }
        } catch (SQLException e) {
            System.err.println("[ThongKe] export summary Ve: " + e.getMessage());
        }

        String sqlHD = "SELECT COUNT(*) AS cnt FROM HoaDon WHERE YEAR(ngayLap) = ?";
        try (PreparedStatement ps = con.prepareStatement(sqlHD)) {
            ps.setInt(1, year);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) tongHoaDon = rs.getInt("cnt");
        } catch (SQLException e) {
            System.err.println("[ThongKe] export summary HD: " + e.getMessage());
        }

        return new SummarySnapshot(doanhThu, tongVe, tongHoaDon, veHuy);
    }

    private double[] fetchMonthlyRevenue(int year) {
        double[] vals = new double[12];
        Connection con = ConnectDB.getCon();
        if (con == null) return vals;

        String sql =
            "SELECT MONTH(hd.ngayLap) AS thang, COALESCE(SUM(ct.giaTien),0) AS tong " +
            "FROM HoaDon hd JOIN ChiTietHoaDon ct ON hd.maHoaDon = ct.maHoaDon " +
            "WHERE YEAR(hd.ngayLap) = ? " +
            "GROUP BY MONTH(hd.ngayLap)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, year);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int month = rs.getInt("thang");
                BigDecimal value = rs.getBigDecimal("tong");
                if (month >= 1 && month <= 12 && value != null) {
                    vals[month - 1] = value.doubleValue() / 1_000_000.0;
                }
            }
        } catch (SQLException e) {
            System.err.println("[ThongKe] export monthly revenue: " + e.getMessage());
        }
        return vals;
    }

    private int[][] fetchMonthlyTicketStats(int year) {
        int[] sold = new int[12];
        int[] cancelled = new int[12];
        Connection con = ConnectDB.getCon();
        if (con == null) return new int[][] {sold, cancelled};

        String sql =
            "SELECT MONTH(hd.ngayLap) AS thang, v.trangThai, COUNT(v.maVe) AS soVe " +
            "FROM Ve v " +
            "JOIN ChiTietHoaDon ct ON v.maVe = ct.maVe " +
            "JOIN HoaDon hd ON ct.maHoaDon = hd.maHoaDon " +
            "WHERE YEAR(hd.ngayLap) = ? " +
            "GROUP BY MONTH(hd.ngayLap), v.trangThai";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, year);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int month = rs.getInt("thang");
                if (month < 1 || month > 12) continue;
                int count = rs.getInt("soVe");
                String status = rs.getString("trangThai");
                if ("DA_BAN".equalsIgnoreCase(status)) sold[month - 1] += count;
                if ("DA_HUY".equalsIgnoreCase(status)) cancelled[month - 1] += count;
            }
        } catch (SQLException e) {
            System.err.println("[ThongKe] export monthly tickets: " + e.getMessage());
        }
        return new int[][] {sold, cancelled};
    }

    private java.util.List<RouteRevenue> fetchTopRouteRevenue() {
        java.util.List<RouteRevenue> rows = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) return rows;

        String sql =
            "SELECT TOP 7 " +
            "    g1.tenGa + N' → ' + g2.tenGa AS tenTuyen, " +
            "    SUM(ct.giaTien) AS tong " +
            "FROM ChiTietHoaDon ct " +
            "JOIN Ve v ON ct.maVe = v.maVe " +
            "JOIN Lich l ON v.maLich = l.maLich " +
            "JOIN Tuyen t ON l.maTuyen = t.maTuyen " +
            "JOIN Ga g1 ON t.gaDi = g1.maGa " +
            "JOIN Ga g2 ON t.gaDen = g2.maGa " +
            "GROUP BY g1.tenGa, g2.tenGa " +
            "ORDER BY tong DESC";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String route = rs.getString("tenTuyen");
                BigDecimal total = rs.getBigDecimal("tong");
                double revenue = total == null ? 0.0 : total.doubleValue() / 1_000_000.0;
                rows.add(new RouteRevenue(route, revenue));
            }
        } catch (SQLException e) {
            System.err.println("[ThongKe] export top route: " + e.getMessage());
        }
        return rows;
    }

    private java.util.List<EmployeeRevenue> fetchTopEmployeeRevenue(int year) {
        java.util.List<EmployeeRevenue> rows = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) return rows;

        String sql =
            "SELECT TOP 5 nv.hoTen, SUM(ct.giaTien) AS tong " +
            "FROM HoaDon hd " +
            "JOIN NhanVien nv ON hd.maNV = nv.maNV " +
            "JOIN ChiTietHoaDon ct ON hd.maHoaDon = ct.maHoaDon " +
            "WHERE YEAR(hd.ngayLap) = ? " +
            "GROUP BY nv.hoTen " +
            "ORDER BY tong DESC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, year);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String employee = rs.getString("hoTen");
                BigDecimal total = rs.getBigDecimal("tong");
                double revenue = total == null ? 0.0 : total.doubleValue() / 1_000_000.0;
                rows.add(new EmployeeRevenue(employee, revenue));
            }
        } catch (SQLException e) {
            System.err.println("[ThongKe] export top employee: " + e.getMessage());
        }
        return rows;
    }

    private int[] fetchDailyInvoicesInCurrentMonth() {
        YearMonth currentMonth = YearMonth.now();
        int[] values = new int[currentMonth.lengthOfMonth()];

        Connection con = ConnectDB.getCon();
        if (con == null) return values;

        String sql =
            "SELECT DAY(ngayLap) AS ngay, COUNT(*) AS soHD " +
            "FROM HoaDon " +
            "WHERE MONTH(ngayLap) = ? AND YEAR(ngayLap) = ? " +
            "GROUP BY DAY(ngayLap)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, currentMonth.getMonthValue());
            ps.setInt(2, currentMonth.getYear());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int day = rs.getInt("ngay");
                if (day >= 1 && day <= values.length) values[day - 1] = rs.getInt("soHD");
            }
        } catch (SQLException e) {
            System.err.println("[ThongKe] export daily invoices: " + e.getMessage());
        }
        return values;
    }

    private ExcelStyles createExcelStyles(XSSFWorkbook workbook) {
        DataFormat fmt = workbook.createDataFormat();

        XSSFFont titleFont = workbook.createFont();
        titleFont.setFontName("Segoe UI");
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);
        titleFont.setColor(IndexedColors.WHITE.getIndex());

        XSSFCellStyle title = workbook.createCellStyle();
        title.setFont(titleFont);
        title.setFillForegroundColor(IndexedColors.TEAL.getIndex());
        title.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        title.setVerticalAlignment(VerticalAlignment.CENTER);
        title.setAlignment(HorizontalAlignment.LEFT);

        XSSFFont subtitleFont = workbook.createFont();
        subtitleFont.setFontName("Segoe UI");
        subtitleFont.setBold(false);
        subtitleFont.setFontHeightInPoints((short) 11);
        subtitleFont.setColor(IndexedColors.GREY_80_PERCENT.getIndex());

        XSSFCellStyle subtitle = workbook.createCellStyle();
        subtitle.setFont(subtitleFont);
        subtitle.setVerticalAlignment(VerticalAlignment.CENTER);
        subtitle.setAlignment(HorizontalAlignment.LEFT);

        XSSFFont metaLabelFont = workbook.createFont();
        metaLabelFont.setFontName("Segoe UI");
        metaLabelFont.setBold(true);
        metaLabelFont.setFontHeightInPoints((short) 10);
        metaLabelFont.setColor(IndexedColors.GREY_80_PERCENT.getIndex());

        XSSFCellStyle metaLabel = workbook.createCellStyle();
        metaLabel.setFont(metaLabelFont);
        metaLabel.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        metaLabel.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        metaLabel.setAlignment(HorizontalAlignment.LEFT);
        metaLabel.setVerticalAlignment(VerticalAlignment.CENTER);
        applyThinBorder(metaLabel);

        XSSFFont metaValueFont = workbook.createFont();
        metaValueFont.setFontName("Segoe UI");
        metaValueFont.setFontHeightInPoints((short) 10);
        metaValueFont.setColor(IndexedColors.BLACK.getIndex());

        XSSFCellStyle metaValue = workbook.createCellStyle();
        metaValue.setFont(metaValueFont);
        metaValue.setAlignment(HorizontalAlignment.LEFT);
        metaValue.setVerticalAlignment(VerticalAlignment.CENTER);
        applyThinBorder(metaValue);

        XSSFFont sectionFont = workbook.createFont();
        sectionFont.setFontName("Segoe UI");
        sectionFont.setBold(true);
        sectionFont.setFontHeightInPoints((short) 11);
        sectionFont.setColor(IndexedColors.DARK_BLUE.getIndex());

        XSSFCellStyle section = workbook.createCellStyle();
        section.setFont(sectionFont);
        section.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
        section.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        section.setAlignment(HorizontalAlignment.LEFT);
        section.setVerticalAlignment(VerticalAlignment.CENTER);
        applyThinBorder(section);

        XSSFFont tableHeaderFont = workbook.createFont();
        tableHeaderFont.setFontName("Segoe UI");
        tableHeaderFont.setBold(true);
        tableHeaderFont.setColor(IndexedColors.WHITE.getIndex());
        tableHeaderFont.setFontHeightInPoints((short) 10);

        XSSFCellStyle tableHeader = workbook.createCellStyle();
        tableHeader.setFont(tableHeaderFont);
        tableHeader.setFillForegroundColor(IndexedColors.BLUE_GREY.getIndex());
        tableHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        tableHeader.setAlignment(HorizontalAlignment.CENTER);
        tableHeader.setVerticalAlignment(VerticalAlignment.CENTER);
        tableHeader.setWrapText(true);
        applyThinBorder(tableHeader);

        XSSFFont textFont = workbook.createFont();
        textFont.setFontName("Segoe UI");
        textFont.setFontHeightInPoints((short) 10);

        XSSFCellStyle text = workbook.createCellStyle();
        text.setFont(textFont);
        text.setAlignment(HorizontalAlignment.LEFT);
        text.setVerticalAlignment(VerticalAlignment.CENTER);
        applyThinBorder(text);

        XSSFCellStyle number = workbook.createCellStyle();
        number.cloneStyleFrom(text);
        number.setAlignment(HorizontalAlignment.RIGHT);
        number.setDataFormat(fmt.getFormat("#,##0"));

        XSSFCellStyle decimal = workbook.createCellStyle();
        decimal.cloneStyleFrom(text);
        decimal.setAlignment(HorizontalAlignment.RIGHT);
        decimal.setDataFormat(fmt.getFormat("#,##0.00"));

        XSSFCellStyle currency = workbook.createCellStyle();
        currency.cloneStyleFrom(text);
        currency.setAlignment(HorizontalAlignment.RIGHT);
        currency.setDataFormat(fmt.getFormat("#,##0 [$₫-vi-VN]"));

        return new ExcelStyles(title, subtitle, metaLabel, metaValue, section, tableHeader, text, number, decimal, currency);
    }

    private void applyThinBorder(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setBottomBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setLeftBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setRightBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
    }

    private int writeSheetHeader(XSSFSheet sheet, ExcelStyles styles, String title, String subtitle, int year) {
        sheet.setDisplayGridlines(false);
        sheet.setColumnWidth(0, 5500);
        sheet.setColumnWidth(1, 4200);
        sheet.setColumnWidth(2, 3600);
        sheet.setColumnWidth(3, 2800);
        sheet.setColumnWidth(4, 6000);
        sheet.setColumnWidth(5, 3400);
        sheet.setColumnWidth(6, 3400);
        sheet.setColumnWidth(7, 3400);
        sheet.setColumnWidth(8, 3400);

        Row row0 = sheet.createRow(0);
        row0.setHeightInPoints(30f);
        for (int c = 0; c <= 8; c++) {
            Cell cell = row0.createCell(c);
            cell.setCellStyle(styles.title());
            if (c == 0) cell.setCellValue(title);
        }
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));

        Row row1 = sheet.createRow(1);
        row1.setHeightInPoints(22f);
        for (int c = 0; c <= 8; c++) {
            Cell cell = row1.createCell(c);
            cell.setCellStyle(styles.subtitle());
            if (c == 0) cell.setCellValue(subtitle);
        }
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 8));

        Row meta = sheet.createRow(3);
        meta.setHeightInPoints(20f);
        createTextCell(meta, 0, "Năm báo cáo", styles.metaLabel());
        createNumberCell(meta, 1, year, styles.number());
        createTextCell(meta, 3, "Xuất lúc", styles.metaLabel());
        createTextCell(meta, 4, LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), styles.metaValue());

        return 5;
    }

    private int writeSectionTitle(XSSFSheet sheet, ExcelStyles styles, int rowIndex, String title) {
        Row row = sheet.createRow(rowIndex++);
        row.setHeightInPoints(22f);
        for (int c = 0; c <= 4; c++) {
            Cell cell = row.createCell(c);
            cell.setCellStyle(styles.section());
            if (c == 0) cell.setCellValue(title);
        }
        sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 0, 4));
        return rowIndex;
    }

    private Cell createTextCell(Row row, int columnIndex, String value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);
        cell.setCellValue(value);
        cell.setCellStyle(style);
        return cell;
    }

    private Cell createNumberCell(Row row, int columnIndex, double value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);
        cell.setCellValue(value);
        cell.setCellStyle(style);
        return cell;
    }

    private void createSummarySheet(
        XSSFWorkbook workbook,
        ExcelStyles styles,
        int year,
        SummarySnapshot summary
    ) {
        XSSFSheet sheet = workbook.createSheet("TongQuan");
        int rowIdx = writeSheetHeader(
            sheet,
            styles,
            "BÁO CÁO THỐNG KÊ TỔNG QUAN",
            "Tổng hợp chỉ số vận hành và doanh thu quan trọng trong năm.",
            year
        );

        rowIdx = writeSectionTitle(sheet, styles, rowIdx, "TỔNG QUAN KPI");

        Row header = sheet.createRow(rowIdx++);
        createTextCell(header, 0, "Chỉ số", styles.tableHeader());
        createTextCell(header, 1, "Giá trị", styles.tableHeader());
        createTextCell(header, 2, "Đơn vị", styles.tableHeader());

        Row r1 = sheet.createRow(rowIdx++);
        createTextCell(r1, 0, "Tổng doanh thu", styles.text());
        createNumberCell(r1, 1, summary.doanhThu().doubleValue(), styles.currency());
        createTextCell(r1, 2, "VND", styles.text());

        Row r2 = sheet.createRow(rowIdx++);
        createTextCell(r2, 0, "Vé đã bán", styles.text());
        createNumberCell(r2, 1, summary.tongVe(), styles.number());
        createTextCell(r2, 2, "vé", styles.text());

        Row r3 = sheet.createRow(rowIdx++);
        createTextCell(r3, 0, "Hóa đơn", styles.text());
        createNumberCell(r3, 1, summary.tongHoaDon(), styles.number());
        createTextCell(r3, 2, "đơn", styles.text());

        Row r4 = sheet.createRow(rowIdx++);
        createTextCell(r4, 0, "Vé đã hủy", styles.text());
        createNumberCell(r4, 1, summary.veHuy(), styles.number());
        createTextCell(r4, 2, "vé", styles.text());

        rowIdx++;
        Row note = sheet.createRow(rowIdx);
        createTextCell(
            note,
            0,
            "Ghi chú: Tổng doanh thu hiển thị theo tiền Việt Nam và cập nhật tại thời điểm xuất.",
            styles.metaValue()
        );
        sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 0, 4));

        for (int c = 0; c <= 4; c++) sheet.autoSizeColumn(c);
    }

    private void createMonthlyRevenueSheet(XSSFWorkbook workbook, ExcelStyles styles, int year, double[] values) {
        XSSFSheet sheet = workbook.createSheet("DoanhThuThang");
        int rowIdx = writeSheetHeader(
            sheet,
            styles,
            "DOANH THU THEO THÁNG",
            "Đơn vị: triệu đồng",
            year
        );

        int chartTopRow = Math.max(REPORT_CHART_TOP_ROW, rowIdx);
        int chartBottomRow = chartTopRow + REPORT_CHART_HEIGHT;
        rowIdx = chartBottomRow + REPORT_TABLE_GAP_ROWS;

        rowIdx = writeSectionTitle(sheet, styles, rowIdx, "BẢNG DOANH THU THÁNG");

        Row header = sheet.createRow(rowIdx++);
        createTextCell(header, 0, "Tháng", styles.tableHeader());
        createTextCell(header, 1, "Doanh thu (triệu ₫)", styles.tableHeader());

        int firstDataRow = rowIdx;
        for (int i = 0; i < values.length; i++) {
            Row row = sheet.createRow(rowIdx++);
            createTextCell(row, 0, "T" + (i + 1), styles.text());
            createNumberCell(row, 1, values[i], styles.decimal());
        }
        int lastDataRow = rowIdx - 1;

        createSingleSeriesBarChart(
            sheet,
            "Doanh thu theo tháng",
            "Doanh thu",
            "Triệu ₫",
            firstDataRow,
            lastDataRow,
            0,
            1,
            BarDirection.COL,
            0,
            chartTopRow,
            8,
            chartBottomRow
        );

        sheet.createFreezePane(0, firstDataRow);
        for (int c = 0; c <= 2; c++) sheet.autoSizeColumn(c);
    }

    private void createMonthlyTicketSheet(
        XSSFWorkbook workbook,
        ExcelStyles styles,
        int year,
        int[] sold,
        int[] cancelled
    ) {
        XSSFSheet sheet = workbook.createSheet("VeTheoThang");
        int rowIdx = writeSheetHeader(
            sheet,
            styles,
            "VÉ BÁN / HỦY THEO THÁNG",
            "So sánh số lượng vé bán và vé hủy trong năm.",
            year
        );

        int chartTopRow = Math.max(REPORT_CHART_TOP_ROW, rowIdx);
        int chartBottomRow = chartTopRow + REPORT_CHART_HEIGHT;
        rowIdx = chartBottomRow + REPORT_TABLE_GAP_ROWS;

        rowIdx = writeSectionTitle(sheet, styles, rowIdx, "BẢNG SỐ LIỆU VÉ");

        Row header = sheet.createRow(rowIdx++);
        createTextCell(header, 0, "Tháng", styles.tableHeader());
        createTextCell(header, 1, "Đã bán", styles.tableHeader());
        createTextCell(header, 2, "Đã hủy", styles.tableHeader());
        createTextCell(header, 3, "Tổng", styles.tableHeader());

        int firstDataRow = rowIdx;
        for (int i = 0; i < 12; i++) {
            int total = sold[i] + cancelled[i];
            Row row = sheet.createRow(rowIdx++);
            createTextCell(row, 0, "T" + (i + 1), styles.text());
            createNumberCell(row, 1, sold[i], styles.number());
            createNumberCell(row, 2, cancelled[i], styles.number());
            createNumberCell(row, 3, total, styles.number());
        }
        int lastDataRow = rowIdx - 1;

        createMultiSeriesColumnChart(
            sheet,
            "Số vé bán và hủy theo tháng",
            "Số vé",
            firstDataRow,
            lastDataRow,
            0,
            new int[]{1, 2},
            new String[]{"Đã bán", "Đã hủy"},
            0,
            chartTopRow,
            8,
            chartBottomRow
        );

        sheet.createFreezePane(0, firstDataRow);
        for (int c = 0; c <= 4; c++) sheet.autoSizeColumn(c);
    }

    private void createTopRoutesSheet(
        XSSFWorkbook workbook,
        ExcelStyles styles,
        int year,
        java.util.List<RouteRevenue> routes
    ) {
        XSSFSheet sheet = workbook.createSheet("TopTuyen");
        int rowIdx = writeSheetHeader(
            sheet,
            styles,
            "TOP TUYẾN DOANH THU",
            "Xếp hạng tuyến theo doanh thu tích lũy toàn hệ thống.",
            year
        );

        int chartTopRow = Math.max(REPORT_CHART_TOP_ROW, rowIdx);
        int chartBottomRow = chartTopRow + REPORT_CHART_HEIGHT;
        rowIdx = chartBottomRow + REPORT_TABLE_GAP_ROWS;

        rowIdx = writeSectionTitle(sheet, styles, rowIdx, "BẢNG DOANH THU TUYẾN");

        Row header = sheet.createRow(rowIdx++);
        createTextCell(header, 0, "Tuyến", styles.tableHeader());
        createTextCell(header, 1, "Doanh thu (triệu ₫)", styles.tableHeader());

        int firstDataRow = rowIdx;
        if (routes.isEmpty()) {
            Row row = sheet.createRow(rowIdx++);
            createTextCell(row, 0, "Không có dữ liệu", styles.text());
            createNumberCell(row, 1, 0, styles.decimal());
        } else {
            for (RouteRevenue route : routes) {
                Row row = sheet.createRow(rowIdx++);
                createTextCell(row, 0, route.routeName(), styles.text());
                createNumberCell(row, 1, route.revenueInMillion(), styles.decimal());
            }
        }
        int lastDataRow = rowIdx - 1;

        createSingleSeriesBarChart(
            sheet,
            "Top tuyến theo doanh thu",
            "Doanh thu",
            "Triệu ₫",
            firstDataRow,
            lastDataRow,
            0,
            1,
            BarDirection.BAR,
            0,
            chartTopRow,
            8,
            chartBottomRow
        );

        sheet.createFreezePane(0, firstDataRow);
        for (int c = 0; c <= 2; c++) sheet.autoSizeColumn(c);
    }

    private void createTopEmployeesSheet(
        XSSFWorkbook workbook,
        ExcelStyles styles,
        int year,
        java.util.List<EmployeeRevenue> employees
    ) {
        XSSFSheet sheet = workbook.createSheet("TopNhanVien");
        int rowIdx = writeSheetHeader(
            sheet,
            styles,
            "TOP NHÂN VIÊN THEO DOANH THU",
            "Hiệu suất nhân sự bán vé trong năm " + year + ".",
            year
        );

        int chartTopRow = Math.max(REPORT_CHART_TOP_ROW, rowIdx);
        int chartBottomRow = chartTopRow + REPORT_CHART_HEIGHT;
        rowIdx = chartBottomRow + REPORT_TABLE_GAP_ROWS;

        rowIdx = writeSectionTitle(sheet, styles, rowIdx, "BẢNG XẾP HẠNG NHÂN VIÊN");

        Row header = sheet.createRow(rowIdx++);
        createTextCell(header, 0, "Nhân viên", styles.tableHeader());
        createTextCell(header, 1, "Doanh thu (triệu ₫)", styles.tableHeader());

        int firstDataRow = rowIdx;
        if (employees.isEmpty()) {
            Row row = sheet.createRow(rowIdx++);
            createTextCell(row, 0, "Không có dữ liệu", styles.text());
            createNumberCell(row, 1, 0, styles.decimal());
        } else {
            for (EmployeeRevenue employee : employees) {
                Row row = sheet.createRow(rowIdx++);
                createTextCell(row, 0, employee.employeeName(), styles.text());
                createNumberCell(row, 1, employee.revenueInMillion(), styles.decimal());
            }
        }
        int lastDataRow = rowIdx - 1;

        createSingleSeriesBarChart(
            sheet,
            "Top nhân viên theo doanh thu",
            "Doanh thu",
            "Triệu ₫",
            firstDataRow,
            lastDataRow,
            0,
            1,
            BarDirection.COL,
            0,
            chartTopRow,
            8,
            chartBottomRow
        );

        sheet.createFreezePane(0, firstDataRow);
        for (int c = 0; c <= 2; c++) sheet.autoSizeColumn(c);
    }

    private void createDailyInvoicesSheet(XSSFWorkbook workbook, ExcelStyles styles, int[] invoicesDaily) {
        YearMonth month = YearMonth.now();
        XSSFSheet sheet = workbook.createSheet("HoaDonNgay");
        int rowIdx = writeSheetHeader(
            sheet,
            styles,
            "HÓA ĐƠN THEO NGÀY",
            "Theo dõi số lượng hóa đơn trong tháng " + month.getMonthValue() + "/" + month.getYear() + ".",
            month.getYear()
        );

        int chartTopRow = Math.max(REPORT_CHART_TOP_ROW, rowIdx);
        int chartBottomRow = chartTopRow + REPORT_CHART_HEIGHT;
        rowIdx = chartBottomRow + REPORT_TABLE_GAP_ROWS;

        rowIdx = writeSectionTitle(sheet, styles, rowIdx, "BẢNG SỐ LIỆU HÓA ĐƠN");

        Row header = sheet.createRow(rowIdx++);
        createTextCell(header, 0, "Ngày", styles.tableHeader());
        createTextCell(header, 1, "Số hóa đơn", styles.tableHeader());

        int firstDataRow = rowIdx;
        for (int i = 0; i < invoicesDaily.length; i++) {
            Row row = sheet.createRow(rowIdx++);
            createTextCell(row, 0, String.valueOf(i + 1), styles.text());
            createNumberCell(row, 1, invoicesDaily[i], styles.number());
        }
        int lastDataRow = rowIdx - 1;

        createLineChart(
            sheet,
            "Hóa đơn theo ngày",
            "Số hóa đơn",
            firstDataRow,
            lastDataRow,
            0,
            1,
            0,
            chartTopRow,
            8,
            chartBottomRow
        );

        sheet.createFreezePane(0, firstDataRow);
        for (int c = 0; c <= 2; c++) sheet.autoSizeColumn(c);
    }

    private void createSingleSeriesBarChart(
        XSSFSheet sheet,
        String chartTitle,
        String seriesTitle,
        String valueAxisTitle,
        int firstRow,
        int lastRow,
        int categoryColumn,
        int valueColumn,
        BarDirection direction,
        int anchorCol1,
        int anchorRow1,
        int anchorCol2,
        int anchorRow2
    ) {
        if (lastRow < firstRow) return;
        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, anchorCol1, anchorRow1, anchorCol2, anchorRow2);
        anchor.setAnchorType(ClientAnchor.AnchorType.DONT_MOVE_AND_RESIZE);
        XSSFChart chart = drawing.createChart(anchor);
        chart.setTitleText(chartTitle);
        chart.setTitleOverlay(false);
        chart.getOrAddLegend().setPosition(LegendPosition.RIGHT);

        XDDFCategoryAxis categoryAxis;
        XDDFValueAxis valueAxis;
        if (direction == BarDirection.BAR) {
            categoryAxis = chart.createCategoryAxis(AxisPosition.LEFT);
            valueAxis = chart.createValueAxis(AxisPosition.BOTTOM);
        } else {
            categoryAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
            valueAxis = chart.createValueAxis(AxisPosition.LEFT);
        }
        valueAxis.setTitle(valueAxisTitle);
        valueAxis.setCrosses(AxisCrosses.AUTO_ZERO);

        XDDFDataSource<String> categories = XDDFDataSourcesFactory.fromStringCellRange(
            sheet, new CellRangeAddress(firstRow, lastRow, categoryColumn, categoryColumn));
        XDDFNumericalDataSource<Double> values = XDDFDataSourcesFactory.fromNumericCellRange(
            sheet, new CellRangeAddress(firstRow, lastRow, valueColumn, valueColumn));

        XDDFBarChartData data = (XDDFBarChartData) chart.createData(ChartTypes.BAR, categoryAxis, valueAxis);
        data.setBarDirection(direction);
        data.setVaryColors(false);
        XDDFBarChartData.Series series = (XDDFBarChartData.Series) data.addSeries(categories, values);
        series.setTitle(seriesTitle, null);
        chart.plot(data);
    }

    private void createMultiSeriesColumnChart(
        XSSFSheet sheet,
        String chartTitle,
        String valueAxisTitle,
        int firstRow,
        int lastRow,
        int categoryColumn,
        int[] valueColumns,
        String[] seriesTitles,
        int anchorCol1,
        int anchorRow1,
        int anchorCol2,
        int anchorRow2
    ) {
        if (lastRow < firstRow || valueColumns.length != seriesTitles.length) return;
        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, anchorCol1, anchorRow1, anchorCol2, anchorRow2);
        anchor.setAnchorType(ClientAnchor.AnchorType.DONT_MOVE_AND_RESIZE);
        XSSFChart chart = drawing.createChart(anchor);
        chart.setTitleText(chartTitle);
        chart.setTitleOverlay(false);
        chart.getOrAddLegend().setPosition(LegendPosition.RIGHT);

        XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
        leftAxis.setTitle(valueAxisTitle);
        leftAxis.setCrosses(AxisCrosses.AUTO_ZERO);

        XDDFDataSource<String> categories = XDDFDataSourcesFactory.fromStringCellRange(
            sheet, new CellRangeAddress(firstRow, lastRow, categoryColumn, categoryColumn));

        XDDFBarChartData data = (XDDFBarChartData) chart.createData(ChartTypes.BAR, bottomAxis, leftAxis);
        data.setBarDirection(BarDirection.COL);
        data.setVaryColors(false);

        for (int i = 0; i < valueColumns.length; i++) {
            XDDFNumericalDataSource<Double> values = XDDFDataSourcesFactory.fromNumericCellRange(
                sheet, new CellRangeAddress(firstRow, lastRow, valueColumns[i], valueColumns[i]));
            XDDFChartData.Series series = data.addSeries(categories, values);
            series.setTitle(seriesTitles[i], null);
        }
        chart.plot(data);
    }

    private void createLineChart(
        XSSFSheet sheet,
        String chartTitle,
        String valueAxisTitle,
        int firstRow,
        int lastRow,
        int categoryColumn,
        int valueColumn,
        int anchorCol1,
        int anchorRow1,
        int anchorCol2,
        int anchorRow2
    ) {
        if (lastRow < firstRow) return;
        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, anchorCol1, anchorRow1, anchorCol2, anchorRow2);
        anchor.setAnchorType(ClientAnchor.AnchorType.DONT_MOVE_AND_RESIZE);
        XSSFChart chart = drawing.createChart(anchor);
        chart.setTitleText(chartTitle);
        chart.setTitleOverlay(false);
        chart.getOrAddLegend().setPosition(LegendPosition.RIGHT);

        XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
        leftAxis.setTitle(valueAxisTitle);
        leftAxis.setCrosses(AxisCrosses.AUTO_ZERO);

        XDDFDataSource<String> categories = XDDFDataSourcesFactory.fromStringCellRange(
            sheet, new CellRangeAddress(firstRow, lastRow, categoryColumn, categoryColumn));
        XDDFNumericalDataSource<Double> values = XDDFDataSourcesFactory.fromNumericCellRange(
            sheet, new CellRangeAddress(firstRow, lastRow, valueColumn, valueColumn));

        XDDFLineChartData data = (XDDFLineChartData) chart.createData(ChartTypes.LINE, bottomAxis, leftAxis);
        XDDFLineChartData.Series series = (XDDFLineChartData.Series) data.addSeries(categories, values);
        series.setTitle("Hóa đơn", null);
        series.setSmooth(false);
        series.setMarkerStyle(MarkerStyle.CIRCLE);
        chart.plot(data);
    }

    private record ExcelStyles(
        CellStyle title,
        CellStyle subtitle,
        CellStyle metaLabel,
        CellStyle metaValue,
        CellStyle section,
        CellStyle tableHeader,
        CellStyle text,
        CellStyle number,
        CellStyle decimal,
        CellStyle currency
    ) { }

    private record ReportExportOptions(
        boolean includeSummary,
        boolean includeMonthlyRevenue,
        boolean includeMonthlyTickets,
        boolean includeTopRoutes,
        boolean includeTopEmployees,
        boolean includeDailyInvoices
    ) {
        boolean hasAnySection() {
            return includeSummary ||
                includeMonthlyRevenue ||
                includeMonthlyTickets ||
                includeTopRoutes ||
                includeTopEmployees ||
                includeDailyInvoices;
        }
    }

    private record SummarySnapshot(
        BigDecimal doanhThu,
        int tongVe,
        int tongHoaDon,
        int veHuy
    ) { }

    private record RouteRevenue(String routeName, double revenueInMillion) { }
    private record EmployeeRevenue(String employeeName, double revenueInMillion) { }

    // ── Chart styling helpers ─────────────────────────────────────────────

    private void applyBarStyle(JFreeChart chart, boolean hasLegend, Color[] seriesColors) {
        chart.setBackgroundPaint(null);
        if (!hasLegend) chart.removeLegend();

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setOutlinePaint(null);
        plot.setRangeGridlinePaint(GRID_COLOR);
        plot.setRangeGridlineStroke(new BasicStroke(0.8f));
        plot.setDomainGridlinesVisible(false);
        plot.setAxisOffset(new RectangleInsets(4, 4, 4, 4));

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setShadowVisible(false);
        renderer.setMaximumBarWidth(0.18);
        renderer.setItemMargin(0.06);
        for (int i = 0; i < seriesColors.length; i++) {
            renderer.setSeriesPaint(i, seriesColors[i]);
        }
        renderer.setSeriesItemLabelGenerator(0, new StandardCategoryItemLabelGenerator(
            "{2}", new DecimalFormat("0.0")));
        renderer.setSeriesItemLabelsVisible(0, false);

        CategoryAxis domAxis = plot.getDomainAxis();
        domAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        domAxis.setTickLabelPaint(TEXT_MUTED);
        domAxis.setAxisLinePaint(BORDER_COLOR);
        domAxis.setTickMarksVisible(false);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        rangeAxis.setTickLabelPaint(TEXT_MUTED);
        rangeAxis.setAxisLinePaint(BORDER_COLOR);
        rangeAxis.setAutoRangeIncludesZero(true);
    }

    private void applyLineStyle(JFreeChart chart, Color color) {
        chart.setBackgroundPaint(null);
        chart.removeLegend();

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setOutlinePaint(null);
        plot.setRangeGridlinePaint(GRID_COLOR);
        plot.setRangeGridlineStroke(new BasicStroke(0.8f));
        plot.setDomainGridlinesVisible(false);
        plot.setAxisOffset(new RectangleInsets(4, 4, 4, 4));

        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, color);
        renderer.setSeriesStroke(0, new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesShape(0, new Ellipse2D.Double(-3.5, -3.5, 7, 7));
        renderer.setSeriesShapesFilled(0, true);

        CategoryAxis domAxis = plot.getDomainAxis();
        domAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 10));
        domAxis.setTickLabelPaint(TEXT_MUTED);
        domAxis.setAxisLinePaint(BORDER_COLOR);
        domAxis.setTickMarksVisible(false);
        domAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        rangeAxis.setTickLabelPaint(TEXT_MUTED);
        rangeAxis.setAxisLinePaint(BORDER_COLOR);
        rangeAxis.setAutoRangeIncludesZero(true);
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
    }

    // ── AppModule interface ───────────────────────────────────────────────

    @Override public String getTitle() { return "Thống kê"; }
    @Override public JPanel getView()  { return this; }
    @Override public void setOnResult(Consumer<Object> cb) { this.callback = cb; }
    @Override public void reset() { loadAllData(); }
}
