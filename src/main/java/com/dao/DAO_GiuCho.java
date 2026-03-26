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
import com.entity.Ghe;
import com.entity.GiuCho;
import com.entity.Lich;
import com.entity.NhanVien;

public class DAO_GiuCho {

    private DAO_NhanVien daoNV = new DAO_NhanVien();
    private DAO_Lich daoLich = new DAO_Lich();
    private DAO_Ghe daoGhe = new DAO_Ghe();

    public List<GiuCho> getAll() {
        List<GiuCho> ds = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) return ds;

        String sql = "SELECT * FROM GiuCho";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách giữ chỗ: " + e.getMessage());
        }
        return ds;
    }

    /**
     * Lấy danh sách giữ chỗ còn hiệu lực trên một lịch
     */
    public List<GiuCho> findConHieuLucByLich(String maLich) {
        List<GiuCho> ds = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) return ds;

        String sql = "SELECT * FROM GiuCho WHERE maLich = ? AND thoiGianHetHan > GETDATE()";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maLich);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm giữ chỗ còn hiệu lực: " + e.getMessage());
        }
        return ds;
    }

    /**
     * Tạo giữ chỗ mới (5 phút)
     */
    public boolean insert(GiuCho gc) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "INSERT INTO GiuCho (maGiuCho, maNV, maLich, maGhe, thoiGianHetHan) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, gc.getMaGiuCho());
            ps.setString(2, gc.getNhanVien().getMaNV());
            ps.setString(3, gc.getLich().getMaLich());
            ps.setString(4, gc.getGhe().getMaGhe());
            ps.setTimestamp(5, Timestamp.valueOf(gc.getThoiGianHetHan()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm giữ chỗ: " + e.getMessage());
        }
        return false;
    }

    /**
     * Xóa giữ chỗ theo mã
     */
    public boolean delete(String maGiuCho) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "DELETE FROM GiuCho WHERE maGiuCho = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maGiuCho);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa giữ chỗ: " + e.getMessage());
        }
        return false;
    }

    /**
     * Xóa tất cả giữ chỗ của một nhân viên (khi xuất hóa đơn hoặc hủy thao tác)
     */
    public boolean deleteByNhanVien(String maNV) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "DELETE FROM GiuCho WHERE maNV = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNV);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa giữ chỗ theo nhân viên: " + e.getMessage());
        }
        return false;
    }

    /**
     * Xóa các giữ chỗ đã hết hạn (dọn dẹp)
     */
    public int deleteExpired() {
        Connection con = ConnectDB.getCon();
        if (con == null) return 0;

        String sql = "DELETE FROM GiuCho WHERE thoiGianHetHan <= GETDATE()";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            return ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa giữ chỗ hết hạn: " + e.getMessage());
        }
        return 0;
    }

    private GiuCho mapRow(ResultSet rs) throws SQLException {
        String maGiuCho = rs.getString("maGiuCho");
        NhanVien nv = daoNV.findById(rs.getString("maNV"));
        Lich lich = daoLich.findById(rs.getString("maLich"));
        Ghe ghe = daoGhe.findById(rs.getString("maGhe"));
        Timestamp ts = rs.getTimestamp("thoiGianHetHan");
        LocalDateTime thoiGianHetHan = ts != null ? ts.toLocalDateTime() : null;
        return new GiuCho(maGiuCho, nv, lich, ghe, thoiGianHetHan);
    }
}
