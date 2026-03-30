package com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.connectDB.ConnectDB;
import com.entity.KhuyenMai;

public class DAO_KhuyenMai {

    public List<KhuyenMai> getAll() {
        List<KhuyenMai> ds = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) return ds;

        String sql = "SELECT * FROM KhuyenMai";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách khuyến mãi: " + e.getMessage());
        }
        return ds;
    }

    public KhuyenMai findById(String maKhuyenMai) {
        Connection con = ConnectDB.getCon();
        if (con == null) return null;

        String sql = "SELECT * FROM KhuyenMai WHERE maKhuyenMai = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maKhuyenMai);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm khuyến mãi: " + e.getMessage());
        }
        return null;
    }

    /**
     * Lấy kỳ khuyến mãi đang áp dụng (trangThai = true)
     */
    public KhuyenMai getKhuyenMaiHienHanh() {
        Connection con = ConnectDB.getCon();
        if (con == null) return null;

        String sql = "SELECT * FROM KhuyenMai WHERE trangThai = 1";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy khuyến mãi hiện hành: " + e.getMessage());
        }
        return null;
    }

    public boolean insert(KhuyenMai km) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "INSERT INTO KhuyenMai (maKhuyenMai, tenKhuyenMai, thoiGianBatDau, thoiGianKetThuc, moTa, trangThai) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, km.getMaKhuyenMai());
            ps.setNString(2, km.getTenKhuyenMai());
            ps.setTimestamp(3, Timestamp.valueOf(km.getThoiGianBatDau()));
            ps.setTimestamp(4, Timestamp.valueOf(km.getThoiGianKetThuc()));
            ps.setNString(5, km.getMoTa());
            ps.setBoolean(6, km.isTrangThai());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm khuyến mãi: " + e.getMessage());
        }
        return false;
    }

    public boolean update(KhuyenMai km) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "UPDATE KhuyenMai SET tenKhuyenMai = ?, thoiGianBatDau = ?, thoiGianKetThuc = ?, moTa = ?, trangThai = ? WHERE maKhuyenMai = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setNString(1, km.getTenKhuyenMai());
            ps.setTimestamp(2, Timestamp.valueOf(km.getThoiGianBatDau()));
            ps.setTimestamp(3, Timestamp.valueOf(km.getThoiGianKetThuc()));
            ps.setNString(4, km.getMoTa());
            ps.setBoolean(5, km.isTrangThai());
            ps.setString(6, km.getMaKhuyenMai());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật khuyến mãi: " + e.getMessage());
        }
        return false;
    }

    public boolean delete(String maKhuyenMai) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;
        String sql = "DELETE FROM KhuyenMai WHERE maKhuyenMai = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maKhuyenMai);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa khuyến mãi: " + e.getMessage());
        }
        return false;
    }

    private KhuyenMai mapRow(ResultSet rs) throws SQLException {
        String maKM = rs.getString("maKhuyenMai");
        String tenKM = rs.getNString("tenKhuyenMai");
        Timestamp tsBD = rs.getTimestamp("thoiGianBatDau");
        Timestamp tsKT = rs.getTimestamp("thoiGianKetThuc");
        LocalDateTime bd = tsBD != null ? tsBD.toLocalDateTime() : null;
        LocalDateTime kt = tsKT != null ? tsKT.toLocalDateTime() : null;
        String moTa = rs.getNString("moTa");
        boolean trangThai = rs.getBoolean("trangThai");
        return new KhuyenMai(maKM, tenKM, bd, kt, moTa, trangThai);
    }
}
