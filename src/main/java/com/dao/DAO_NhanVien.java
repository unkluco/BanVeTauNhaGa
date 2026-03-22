package com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.connectDB.ConnectDB;
import com.entity.NhanVien;
import com.enums.VaiTro;

public class DAO_NhanVien {

    // ========================= LẤY TẤT CẢ =========================
    public List<NhanVien> getAll() {
        List<NhanVien> ds = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) return ds;

        String sql = "SELECT * FROM NhanVien";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách nhân viên: " + e.getMessage());
        }
        return ds;
    }

    // ========================= TÌM THEO MÃ =========================
    public NhanVien findById(String maNV) {
        Connection con = ConnectDB.getCon();
        if (con == null) return null;

        String sql = "SELECT * FROM NhanVien WHERE maNV = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm nhân viên: " + e.getMessage());
        }
        return null;
    }

    // ========================= THÊM =========================
    public boolean insert(NhanVien nv) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "INSERT INTO NhanVien (maNV, hoTen, [password], vaiTro, soDienThoai) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nv.getMaNV());
            ps.setNString(2, nv.getHoTen());
            ps.setString(3, nv.getPassword());
            ps.setString(4, nv.getVaiTro().toDbValue());
            ps.setString(5, nv.getSoDienThoai());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm nhân viên: " + e.getMessage());
        }
        return false;
    }

    // ========================= CẬP NHẬT =========================
    public boolean update(NhanVien nv) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "UPDATE NhanVien SET hoTen = ?, [password] = ?, vaiTro = ?, soDienThoai = ? WHERE maNV = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setNString(1, nv.getHoTen());
            ps.setString(2, nv.getPassword());
            ps.setString(3, nv.getVaiTro().toDbValue());
            ps.setString(4, nv.getSoDienThoai());
            ps.setString(5, nv.getMaNV());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật nhân viên: " + e.getMessage());
        }
        return false;
    }

    // ========================= ĐĂNG NHẬP =========================
    public NhanVien checkLogin(String maNV, String password) {
        Connection con = ConnectDB.getCon();
        if (con == null) return null;

        String sql = "SELECT * FROM NhanVien WHERE maNV = ? AND [password] = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNV);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi kiểm tra đăng nhập: " + e.getMessage());
        }
        return null;
    }

    // ========================= TÌM KIẾM =========================
    public List<NhanVien> search(String keyword) {
        List<NhanVien> ds = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) return ds;

        String sql = "SELECT * FROM NhanVien WHERE maNV LIKE ? OR hoTen COLLATE Latin1_General_CI_AI LIKE ? OR soDienThoai LIKE ? OR vaiTro LIKE ?";
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
            System.err.println("Lỗi khi tìm kiếm nhân viên: " + e.getMessage());
        }
        return ds;
    }

    // ========================= MAP ROW =========================
    private NhanVien mapRow(ResultSet rs) throws SQLException {
        String maNV = rs.getString("maNV");
        String hoTen = rs.getNString("hoTen");
        String password = rs.getString("password");
        VaiTro vaiTro = VaiTro.fromAny(rs.getString("vaiTro"));
        String soDienThoai = rs.getString("soDienThoai");
        return new NhanVien(maNV, hoTen, password, vaiTro, soDienThoai);
    }
}
