package com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.connectDB.ConnectDB;
import com.entity.HoaDon;
import com.entity.NhanVien;

public class DAO_HoaDon {

    private DAO_NhanVien daoNV = new DAO_NhanVien();

    public List<HoaDon> getAll() {
        List<HoaDon> ds = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) return ds;

        String sql = "SELECT * FROM HoaDon ORDER BY ngayLap DESC";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách hóa đơn: " + e.getMessage());
        }
        return ds;
    }

    public HoaDon findById(String maHoaDon) {
        Connection con = ConnectDB.getCon();
        if (con == null) return null;

        String sql = "SELECT * FROM HoaDon WHERE maHoaDon = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHoaDon);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm hóa đơn: " + e.getMessage());
        }
        return null;
    }

    /**
     * Phát sinh mã hóa đơn theo định dạng HD-ddMMyyyy-xxx
     */
    public String phatSinhMaHoaDon() {
        Connection con = ConnectDB.getCon();
        if (con == null) return "HD-00000000-001";

        LocalDate now = LocalDate.now();
        String datePrefix = String.format("HD-%02d%02d%04d", now.getDayOfMonth(), now.getMonthValue(), now.getYear());

        String sql = "SELECT COUNT(*) FROM HoaDon WHERE maHoaDon LIKE ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, datePrefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1) + 1;
                    return datePrefix + "-" + String.format("%03d", count);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi phát sinh mã hóa đơn: " + e.getMessage());
        }
        return datePrefix + "-001";
    }

    /**
     * Lấy hóa đơn trong ngày (lọc theo nhân viên nếu maNV != null)
     */
    public List<HoaDon> getHoaDonTrongNgay(LocalDate ngay, String maNV) {
        List<HoaDon> ds = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) return ds;

        StringBuilder sql = new StringBuilder("SELECT * FROM HoaDon WHERE CAST(ngayLap AS date) = ?");
        if (maNV != null) sql.append(" AND maNV = ?");
        sql.append(" ORDER BY ngayLap DESC");

        try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
            ps.setDate(1, java.sql.Date.valueOf(ngay));
            if (maNV != null) ps.setString(2, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy hóa đơn trong ngày: " + e.getMessage());
        }
        return ds;
    }

    public boolean insert(HoaDon hd) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "INSERT INTO HoaDon (maHoaDon, maNV, ngayLap) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, hd.getMaHoaDon());
            ps.setString(2, hd.getNhanVien().getMaNV());
            ps.setTimestamp(3, Timestamp.valueOf(hd.getNgayLap()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm hóa đơn: " + e.getMessage());
        }
        return false;
    }

    private HoaDon mapRow(ResultSet rs) throws SQLException {
        String maHoaDon = rs.getString("maHoaDon");
        NhanVien nv = daoNV.findById(rs.getString("maNV"));
        Timestamp ts = rs.getTimestamp("ngayLap");
        LocalDateTime ngayLap = ts != null ? ts.toLocalDateTime() : null;
        return new HoaDon(maHoaDon, nv, ngayLap);
    }
}
