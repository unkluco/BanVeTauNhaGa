package com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.connectDB.ConnectDB;
import com.entity.Ghe;
import com.entity.ToaTau;

public class DAO_Ghe {

    private DAO_ToaTau daoToaTau = new DAO_ToaTau();

    public List<Ghe> getAll() {
        List<Ghe> ds = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) return ds;

        String sql = "SELECT * FROM Ghe ORDER BY maToaTau, soGhe";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách ghế: " + e.getMessage());
        }
        return ds;
    }

    public Ghe findById(String maGhe) {
        Connection con = ConnectDB.getCon();
        if (con == null) return null;

        String sql = "SELECT * FROM Ghe WHERE maGhe = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maGhe);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm ghế: " + e.getMessage());
        }
        return null;
    }

    /**
     * Lấy danh sách ghế theo toa tàu
     */
    public List<Ghe> findByToaTau(String maToaTau) {
        List<Ghe> ds = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) return ds;

        String sql = "SELECT * FROM Ghe WHERE maToaTau = ? ORDER BY soGhe";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maToaTau);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm ghế theo toa: " + e.getMessage());
        }
        return ds;
    }

    /**
     * Kiểm tra ghế trống trên một lịch cụ thể
     * Ghế trống khi: không có ChiTietVe DA_BAN và không có GiuCho còn hiệu lực
     */
    public boolean isGheTrong(String maGhe, String maLich) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = """
            SELECT CASE
                WHEN NOT EXISTS (
                    SELECT 1 FROM ChiTietVe WHERE maGhe = ? AND maLich = ? AND trangThai = 'DA_BAN'
                )
                AND NOT EXISTS (
                    SELECT 1 FROM GiuCho WHERE maGhe = ? AND maLich = ? AND thoiGianHetHan > GETDATE()
                )
                THEN 1 ELSE 0
            END AS isTrong
            """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maGhe);
            ps.setString(2, maLich);
            ps.setString(3, maGhe);
            ps.setString(4, maLich);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("isTrong") == 1;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi kiểm tra ghế trống: " + e.getMessage());
        }
        return false;
    }

    public boolean insert(Ghe ghe) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "INSERT INTO Ghe (maGhe, maToaTau, soGhe) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ghe.getMaGhe());
            ps.setString(2, ghe.getToaTau().getMaToaTau());
            ps.setInt(3, ghe.getSoGhe());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm ghế: " + e.getMessage());
        }
        return false;
    }

    private Ghe mapRow(ResultSet rs) throws SQLException {
        String maGhe = rs.getString("maGhe");
        ToaTau toaTau = daoToaTau.findById(rs.getString("maToaTau"));
        int soGhe = rs.getInt("soGhe");
        return new Ghe(maGhe, toaTau, soGhe);
    }
}
