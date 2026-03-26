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
import com.entity.Gia;

public class DAO_Gia {

    public List<Gia> getAll() {
        List<Gia> ds = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) return ds;

        String sql = "SELECT * FROM Gia";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách giá: " + e.getMessage());
        }
        return ds;
    }

    public Gia findById(String maGia) {
        Connection con = ConnectDB.getCon();
        if (con == null) return null;

        String sql = "SELECT * FROM Gia WHERE maGia = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maGia);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm giá: " + e.getMessage());
        }
        return null;
    }

    /**
     * Lấy kỳ giá đang áp dụng (trangThai = true)
     */
    public Gia getGiaHienHanh() {
        Connection con = ConnectDB.getCon();
        if (con == null) return null;

        String sql = "SELECT * FROM Gia WHERE trangThai = 1";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy giá hiện hành: " + e.getMessage());
        }
        return null;
    }

    public boolean insert(Gia gia) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "INSERT INTO Gia (maGia, thoiGianBatDau, thoiGianKetThuc, moTa, trangThai) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, gia.getMaGia());
            ps.setTimestamp(2, Timestamp.valueOf(gia.getThoiGianBatDau()));
            ps.setTimestamp(3, Timestamp.valueOf(gia.getThoiGianKetThuc()));
            ps.setNString(4, gia.getMoTa());
            ps.setBoolean(5, gia.isTrangThai());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm giá: " + e.getMessage());
        }
        return false;
    }

    public boolean update(Gia gia) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "UPDATE Gia SET thoiGianBatDau = ?, thoiGianKetThuc = ?, moTa = ?, trangThai = ? WHERE maGia = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(gia.getThoiGianBatDau()));
            ps.setTimestamp(2, Timestamp.valueOf(gia.getThoiGianKetThuc()));
            ps.setNString(3, gia.getMoTa());
            ps.setBoolean(4, gia.isTrangThai());
            ps.setString(5, gia.getMaGia());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật giá: " + e.getMessage());
        }
        return false;
    }

    private Gia mapRow(ResultSet rs) throws SQLException {
        String maGia = rs.getString("maGia");
        Timestamp tsBD = rs.getTimestamp("thoiGianBatDau");
        Timestamp tsKT = rs.getTimestamp("thoiGianKetThuc");
        LocalDateTime thoiGianBatDau = tsBD != null ? tsBD.toLocalDateTime() : null;
        LocalDateTime thoiGianKetThuc = tsKT != null ? tsKT.toLocalDateTime() : null;
        String moTa = rs.getNString("moTa");
        boolean trangThai = rs.getBoolean("trangThai");
        return new Gia(maGia, thoiGianBatDau, thoiGianKetThuc, moTa, trangThai);
    }
}
