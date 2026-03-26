package com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.connectDB.ConnectDB;
import com.entity.ApDungKM;
import com.entity.KhuyenMai;
import com.entity.Ve;

public class DAO_ApDungKM {

    private DAO_Ve daoVe = new DAO_Ve();
    private DAO_KhuyenMai daoKM = new DAO_KhuyenMai();

    public List<ApDungKM> getAll() {
        List<ApDungKM> ds = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) return ds;

        String sql = "SELECT * FROM ApDungKM";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách áp dụng KM: " + e.getMessage());
        }
        return ds;
    }

    /**
     * Lấy danh sách khuyến mãi áp dụng cho một vé
     */
    public List<ApDungKM> findByVe(String maVe) {
        List<ApDungKM> ds = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) return ds;

        String sql = "SELECT * FROM ApDungKM WHERE maVe = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maVe);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm áp dụng KM theo vé: " + e.getMessage());
        }
        return ds;
    }

    public boolean insert(ApDungKM adkm) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "INSERT INTO ApDungKM (maApDung, maVe, maKhuyenMai) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, adkm.getMaApDung());
            ps.setString(2, adkm.getVe().getMaVe());
            ps.setString(3, adkm.getKhuyenMai().getMaKhuyenMai());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm áp dụng KM: " + e.getMessage());
        }
        return false;
    }

    public boolean delete(String maApDung) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "DELETE FROM ApDungKM WHERE maApDung = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maApDung);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa áp dụng KM: " + e.getMessage());
        }
        return false;
    }

    private ApDungKM mapRow(ResultSet rs) throws SQLException {
        String maApDung = rs.getString("maApDung");
        Ve ve = daoVe.findById(rs.getString("maVe"));
        KhuyenMai km = daoKM.findById(rs.getString("maKhuyenMai"));
        return new ApDungKM(maApDung, ve, km);
    }
}
