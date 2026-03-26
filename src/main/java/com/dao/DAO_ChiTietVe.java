package com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.connectDB.ConnectDB;
import com.entity.ChiTietVe;
import com.entity.Ghe;
import com.entity.Lich;
import com.entity.Ve;
import com.enums.TrangThaiVe;

public class DAO_ChiTietVe {

    private DAO_Ve daoVe = new DAO_Ve();
    private DAO_Lich daoLich = new DAO_Lich();
    private DAO_Ghe daoGhe = new DAO_Ghe();

    public List<ChiTietVe> getAll() {
        List<ChiTietVe> ds = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) return ds;

        String sql = "SELECT * FROM ChiTietVe";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách chi tiết vé: " + e.getMessage());
        }
        return ds;
    }

    /**
     * Lấy danh sách chi tiết vé theo mã vé
     */
    public List<ChiTietVe> findByVe(String maVe) {
        List<ChiTietVe> ds = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) return ds;

        String sql = "SELECT * FROM ChiTietVe WHERE maVe = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maVe);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm chi tiết vé: " + e.getMessage());
        }
        return ds;
    }

    /**
     * Lấy danh sách chi tiết vé theo lịch (ghế đã bán trên một lịch cụ thể)
     */
    public List<ChiTietVe> findByLich(String maLich) {
        List<ChiTietVe> ds = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) return ds;

        String sql = "SELECT * FROM ChiTietVe WHERE maLich = ? AND trangThai = 'DA_BAN'";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maLich);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm chi tiết vé theo lịch: " + e.getMessage());
        }
        return ds;
    }

    public boolean insert(ChiTietVe ctv) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "INSERT INTO ChiTietVe (maChiTietVe, maVe, maLich, maGhe, trangThai) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ctv.getMaChiTietVe());
            ps.setString(2, ctv.getVe().getMaVe());
            ps.setString(3, ctv.getLich().getMaLich());
            ps.setString(4, ctv.getGhe().getMaGhe());
            ps.setString(5, ctv.getTrangThai().toDbValue());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm chi tiết vé: " + e.getMessage());
        }
        return false;
    }

    /**
     * Hủy chi tiết vé theo mã vé (hủy tất cả chặng)
     */
    public boolean huyChiTietVeByMaVe(String maVe) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "UPDATE ChiTietVe SET trangThai = 'DA_HUY' WHERE maVe = ? AND trangThai = 'DA_BAN'";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maVe);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi hủy chi tiết vé: " + e.getMessage());
        }
        return false;
    }

    public boolean update(ChiTietVe ctv) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "UPDATE ChiTietVe SET maVe = ?, maLich = ?, maGhe = ?, trangThai = ? WHERE maChiTietVe = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ctv.getVe().getMaVe());
            ps.setString(2, ctv.getLich().getMaLich());
            ps.setString(3, ctv.getGhe().getMaGhe());
            ps.setString(4, ctv.getTrangThai().toDbValue());
            ps.setString(5, ctv.getMaChiTietVe());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật chi tiết vé: " + e.getMessage());
        }
        return false;
    }

    private ChiTietVe mapRow(ResultSet rs) throws SQLException {
        String maChiTietVe = rs.getString("maChiTietVe");
        Ve ve = daoVe.findById(rs.getString("maVe"));
        Lich lich = daoLich.findById(rs.getString("maLich"));
        Ghe ghe = daoGhe.findById(rs.getString("maGhe"));
        TrangThaiVe trangThai = TrangThaiVe.fromAny(rs.getString("trangThai"));
        return new ChiTietVe(maChiTietVe, ve, lich, ghe, trangThai);
    }
}
