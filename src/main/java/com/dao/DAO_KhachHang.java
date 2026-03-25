package com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.connectDB.ConnectDB;
import com.entity.KhachHang;

public class DAO_KhachHang {

    public List<KhachHang> getAll() {
        List<KhachHang> ds = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) return ds;

        String sql = "SELECT * FROM KhachHang";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Loi khi lay danh sach khach hang: " + e.getMessage());
        }
        return ds;
    }

    public KhachHang findById(String maKhachHang) {
        Connection con = ConnectDB.getCon();
        if (con == null) return null;

        String sql = "SELECT * FROM KhachHang WHERE maKhachHang = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maKhachHang);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Loi khi tim khach hang: " + e.getMessage());
        }
        return null;
    }

    public KhachHang findByCCCD(String cccd) {
        Connection con = ConnectDB.getCon();
        if (con == null) return null;

        String sql = "SELECT * FROM KhachHang WHERE cccd = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, cccd);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Loi khi tim khach hang theo CCCD: " + e.getMessage());
        }
        return null;
    }

    public boolean insert(KhachHang kh) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "INSERT INTO KhachHang (maKhachHang, hoTen, cccd, soDienThoai) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, kh.getMaKhachHang());
            ps.setNString(2, kh.getHoTen());
            ps.setString(3, kh.getCccd());
            ps.setString(4, kh.getSoDienThoai());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Loi khi them khach hang: " + e.getMessage());
        }
        return false;
    }

    public boolean update(KhachHang kh) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "UPDATE KhachHang SET hoTen = ?, cccd = ?, soDienThoai = ? WHERE maKhachHang = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setNString(1, kh.getHoTen());
            ps.setString(2, kh.getCccd());
            ps.setString(3, kh.getSoDienThoai());
            ps.setString(4, kh.getMaKhachHang());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Loi khi cap nhat khach hang: " + e.getMessage());
        }
        return false;
    }

    public List<KhachHang> search(String keyword) {
        List<KhachHang> ds = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) return ds;

        String sql = "SELECT * FROM KhachHang WHERE maKhachHang LIKE ? OR hoTen COLLATE Latin1_General_CI_AI LIKE ? OR cccd LIKE ? OR soDienThoai LIKE ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            String kw = "%" + keyword.trim() + "%";
            ps.setString(1, kw);
            ps.setNString(2, kw);
            ps.setString(3, kw);
            ps.setString(4, kw);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Loi khi tim kiem khach hang: " + e.getMessage());
        }
        return ds;
    }

    private KhachHang mapRow(ResultSet rs) throws SQLException {
        String maKH = rs.getString("maKhachHang");
        String hoTen = rs.getNString("hoTen");
        String cccd = rs.getString("cccd");
        String soDienThoai = rs.getString("soDienThoai");
        return new KhachHang(maKH, hoTen, cccd, soDienThoai);
    }
}
