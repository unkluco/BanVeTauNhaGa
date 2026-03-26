package com.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.connectDB.ConnectDB;
import com.entity.NhanVien;
import com.enums.TrangThaiNhanVien;
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

        String sql = "INSERT INTO NhanVien (maNV, hoTen, [password], vaiTro, soDienThoai, cccd, diaChiTamTru, trangThai, " +
                     "email, gaLamViec, diaChiThuongTru, ngaySinh, gioiTinh, quocTich) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nv.getMaNV());
            ps.setNString(2, nv.getHoTen());
            ps.setString(3, nv.getPassword());
            ps.setString(4, nv.getVaiTro().toDbValue());
            ps.setString(5, nv.getSoDienThoai());
            ps.setString(6, nv.getCccd());
            ps.setNString(7, nv.getDiaChiTamTru());
            ps.setString(8, nv.getTrangThai() != null ? nv.getTrangThai().toDbValue() : TrangThaiNhanVien.DANG_LAM.toDbValue());
            // new fields
            ps.setString(9, nv.getEmail());
            ps.setString(10, nv.getGaLamViec());
            ps.setNString(11, nv.getDiaChiThuongTru());
            if (nv.getNgaySinh() != null) {
                ps.setDate(12, Date.valueOf(nv.getNgaySinh()));
            } else {
                ps.setNull(12, java.sql.Types.DATE);
            }
            ps.setString(13, nv.getGioiTinh());
            ps.setNString(14, nv.getQuocTich());
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

        String sql = "UPDATE NhanVien SET hoTen = ?, [password] = ?, vaiTro = ?, soDienThoai = ?, cccd = ?, " +
                     "diaChiTamTru = ?, trangThai = ?, email = ?, gaLamViec = ?, diaChiThuongTru = ?, " +
                     "ngaySinh = ?, gioiTinh = ?, quocTich = ? WHERE maNV = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setNString(1, nv.getHoTen());
            ps.setString(2, nv.getPassword());
            ps.setString(3, nv.getVaiTro().toDbValue());
            ps.setString(4, nv.getSoDienThoai());
            ps.setString(5, nv.getCccd());
            ps.setNString(6, nv.getDiaChiTamTru());
            ps.setString(7, nv.getTrangThai() != null ? nv.getTrangThai().toDbValue() : TrangThaiNhanVien.DANG_LAM.toDbValue());
            // new fields
            ps.setString(8, nv.getEmail());
            ps.setString(9, nv.getGaLamViec());
            ps.setNString(10, nv.getDiaChiThuongTru());
            if (nv.getNgaySinh() != null) {
                ps.setDate(11, Date.valueOf(nv.getNgaySinh()));
            } else {
                ps.setNull(11, java.sql.Types.DATE);
            }
            ps.setString(12, nv.getGioiTinh());
            ps.setNString(13, nv.getQuocTich());
            ps.setString(14, nv.getMaNV());
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

    // ========================= ĐỔI MẬT KHẨU =========================
    public boolean updatePassword(String maNV, String newPassword) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "UPDATE NhanVien SET [password] = ? WHERE maNV = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setString(2, maNV);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi đổi mật khẩu: " + e.getMessage());
        }
        return false;
    }

    // ========================= CẬP NHẬT THÔNG TIN LIÊN LẠC =========================
    public boolean updateContactInfo(String maNV, String soDienThoai, String email, String diaChiThuongTru) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "UPDATE NhanVien SET soDienThoai = ?, email = ?, diaChiThuongTru = ? WHERE maNV = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, soDienThoai);
            ps.setString(2, email);
            ps.setNString(3, diaChiThuongTru);
            ps.setString(4, maNV);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật thông tin liên lạc: " + e.getMessage());
        }
        return false;
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

    // ========================= SINH MA NV MOI =========================
    public String generateNextMaNV() {
        Connection con = ConnectDB.getCon();
        if (con == null) return "NV-0001";

        String sql = "SELECT MAX(CAST(SUBSTRING(maNV, 4, LEN(maNV)-3) AS INT)) FROM NhanVien WHERE maNV LIKE 'NV-%'";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int maxNum = rs.getInt(1);
                return String.format("NV-%04d", maxNum + 1);
            }
        } catch (SQLException e) {
            System.err.println("Loi khi sinh ma NV: " + e.getMessage());
        }
        return "NV-0001";
    }

    // ========================= LẤY DANH SÁCH GA =========================
    /**
     * Returns list of {maGa, tenGa} from the Ga table, ordered by maGa.
     */
    public List<String[]> getAllGa() {
        List<String[]> result = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) return result;

        String sql = "SELECT maGa, tenGa FROM Ga ORDER BY maGa";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(new String[]{ rs.getString("maGa"), rs.getNString("tenGa") });
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách ga: " + e.getMessage());
        }
        return result;
    }

    // ========================= MAP ROW =========================
    private NhanVien mapRow(ResultSet rs) throws SQLException {
        String maNV = rs.getString("maNV");
        String hoTen = rs.getNString("hoTen");
        String password = rs.getString("password");
        VaiTro vaiTro = VaiTro.fromAny(rs.getString("vaiTro"));
        String soDienThoai = rs.getString("soDienThoai");
        String cccd = rs.getString("cccd");
        String diaChiTamTru = rs.getNString("diaChiTamTru");
        TrangThaiNhanVien trangThai = TrangThaiNhanVien.fromAny(rs.getString("trangThai"));
        if (trangThai == null) trangThai = TrangThaiNhanVien.DANG_LAM;

        NhanVien nv = new NhanVien(maNV, hoTen, password, vaiTro, soDienThoai, cccd, diaChiTamTru, trangThai);

        // new fields – guard against column-not-found on older schema
        try { nv.setEmail(rs.getString("email")); } catch (SQLException ignored) {}
        try { nv.setGaLamViec(rs.getString("gaLamViec")); } catch (SQLException ignored) {}
        try { nv.setDiaChiThuongTru(rs.getNString("diaChiThuongTru")); } catch (SQLException ignored) {}
        try {
            Date d = rs.getDate("ngaySinh");
            if (d != null) nv.setNgaySinh(d.toLocalDate());
        } catch (SQLException ignored) {}
        try { nv.setGioiTinh(rs.getString("gioiTinh")); } catch (SQLException ignored) {}
        try { nv.setQuocTich(rs.getNString("quocTich")); } catch (SQLException ignored) {}

        return nv;
    }
}
