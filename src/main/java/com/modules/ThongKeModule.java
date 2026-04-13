package com.modules;

import com.connectDB.ConnectDB;
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
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.math.*;
import java.sql.*;
import java.text.*;
import java.time.*;
import java.util.*;
import java.util.function.*;

public class ThongKeModule extends JPanel implements AppModule {

    private Consumer<Object> callback;

    // Controls
    private JComboBox<Integer> cboYear;
    private JButton btnRefresh;

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

        btnRefresh = new JButton("↻ Làm mới");
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRefresh.setBackground(PRIMARY);
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setBorder(new EmptyBorder(7, 16, 7, 16));
        btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRefresh.setOpaque(true);
        btnRefresh.addActionListener(e -> loadAllData());

        controls.add(lblYear);
        controls.add(cboYear);
        controls.add(Box.createHorizontalStrut(4));
        controls.add(btnRefresh);

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
        int year = (Integer) cboYear.getSelectedItem();
        btnRefresh.setEnabled(false);
        btnRefresh.setText("⏳ Đang tải...");

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
                btnRefresh.setEnabled(true);
                btnRefresh.setText("↻ Làm mới");
            }
        };
        worker.execute();
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
