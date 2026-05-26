package com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import com.connectDB.ConnectDB;
import com.entity.KhachHang;
import com.util.MaTuDong;

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

    public List<KhachHang> findBySoDienThoaiLike(String sdt) {
        List<KhachHang> ds = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) return ds;

        String sql = "SELECT * FROM KhachHang WHERE soDienThoai LIKE ? ORDER BY hoTen";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, sdt + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ds.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Loi khi tim khach hang theo SDT: " + e.getMessage());
        }
        return ds;
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

        String sql = "INSERT INTO KhachHang (maKhachHang, hoTen, cccd, soDienThoai, email, diaChiThuongTru, diaChiTamTru, ngaySinh, gioiTinh, quocTich) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, kh.getMaKhachHang());
            ps.setNString(2, kh.getHoTen());
            ps.setString(3, kh.getCccd());
            ps.setString(4, kh.getSoDienThoai());
            ps.setString(5, kh.getEmail());
            ps.setNString(6, kh.getDiaChiThuongTru());
            ps.setNString(7, kh.getDiaChiTamTru());
            if (kh.getNgaySinh() != null) {
                ps.setDate(8, Date.valueOf(kh.getNgaySinh()));
            } else {
                ps.setDate(8, null);
            }
            ps.setString(9, kh.getGioiTinh());
            ps.setNString(10, kh.getQuocTich());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Loi khi them khach hang: " + e.getMessage());
        }
        return false;
    }

    public boolean update(KhachHang kh) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "UPDATE KhachHang SET hoTen = ?, cccd = ?, soDienThoai = ?, email = ?, "
                   + "diaChiThuongTru = ?, diaChiTamTru = ?, ngaySinh = ?, gioiTinh = ?, quocTich = ? "
                   + "WHERE maKhachHang = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setNString(1, kh.getHoTen());
            ps.setString(2, kh.getCccd());
            ps.setString(3, kh.getSoDienThoai());
            ps.setString(4, kh.getEmail());
            ps.setNString(5, kh.getDiaChiThuongTru());
            ps.setNString(6, kh.getDiaChiTamTru());
            if (kh.getNgaySinh() != null) {
                ps.setDate(7, Date.valueOf(kh.getNgaySinh()));
            } else {
                ps.setDate(7, null);
            }
            ps.setString(8, kh.getGioiTinh());
            ps.setNString(9, kh.getQuocTich());
            ps.setString(10, kh.getMaKhachHang());
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

        String sql = "SELECT * FROM KhachHang WHERE maKhachHang LIKE ? OR hoTen COLLATE Latin1_General_CI_AI LIKE ? OR cccd LIKE ? OR soDienThoai LIKE ? OR email LIKE ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            String kw = "%" + keyword.trim() + "%";
            ps.setString(1, kw);
            ps.setNString(2, kw);
            ps.setString(3, kw);
            ps.setString(4, kw);
            ps.setString(5, kw);
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

    // ========================= KIỂM TRA TRÙNG LẶP =========================
    public boolean existsBySoDienThoai(String sdt, String excludeMaKH) {
        Connection con = ConnectDB.getCon();
        if (con == null || sdt == null || sdt.isBlank()) return false;
        String sql = "SELECT 1 FROM KhachHang WHERE soDienThoai = ? AND (? IS NULL OR maKhachHang <> ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, sdt);
            ps.setString(2, excludeMaKH);
            ps.setString(3, excludeMaKH);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) {
            System.err.println("Loi khi kiem tra trung SDT khach hang: " + e.getMessage());
        }
        return false;
    }

    public boolean existsByCccd(String cccd, String excludeMaKH) {
        Connection con = ConnectDB.getCon();
        if (con == null || cccd == null || cccd.isBlank()) return false;
        String sql = "SELECT 1 FROM KhachHang WHERE cccd = ? AND (? IS NULL OR maKhachHang <> ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, cccd);
            ps.setString(2, excludeMaKH);
            ps.setString(3, excludeMaKH);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) {
            System.err.println("Loi khi kiem tra trung CCCD khach hang: " + e.getMessage());
        }
        return false;
    }

    public boolean existsByEmail(String email, String excludeMaKH) {
        Connection con = ConnectDB.getCon();
        if (con == null || email == null || email.isBlank()) return false;
        String sql = "SELECT 1 FROM KhachHang WHERE email = ? AND (? IS NULL OR maKhachHang <> ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, excludeMaKH);
            ps.setString(3, excludeMaKH);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) {
            System.err.println("Loi khi kiem tra trung email khach hang: " + e.getMessage());
        }
        return false;
    }

    // ========================= SINH MA KH MOI =========================
    public String generateNextMaKH() {
        return MaTuDong.generate("KH");
    }

    private KhachHang mapRow(ResultSet rs) throws SQLException {
        KhachHang kh = new KhachHang();
        kh.setMaKhachHang(rs.getString("maKhachHang"));
        kh.setHoTen(rs.getNString("hoTen"));
        kh.setCccd(rs.getString("cccd"));
        kh.setSoDienThoai(rs.getString("soDienThoai"));
        kh.setEmail(rs.getString("email"));
        kh.setDiaChiThuongTru(rs.getNString("diaChiThuongTru"));
        kh.setDiaChiTamTru(rs.getNString("diaChiTamTru"));
        Date ngaySinh = rs.getDate("ngaySinh");
        kh.setNgaySinh(ngaySinh != null ? ngaySinh.toLocalDate() : null);
        kh.setGioiTinh(rs.getString("gioiTinh"));
        kh.setQuocTich(rs.getNString("quocTich"));
        return kh;
    }
}
