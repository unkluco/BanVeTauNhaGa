package com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.connectDB.ConnectDB;
import com.entity.ApDungKM;
import com.entity.ChiTietHoaDon;
import com.entity.ChiTietKhuyenMai;

public class DAO_ApDungKM {

    private DAO_ChiTietHoaDon daoCTHD = new DAO_ChiTietHoaDon();
    private DAO_ChiTietKhuyenMai daoCTKM = new DAO_ChiTietKhuyenMai();

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
     * Lấy danh sách khuyến mãi áp dụng cho một chi tiết hóa đơn
     */
    public List<ApDungKM> findByChiTietHD(String maChiTietHD) {
        List<ApDungKM> ds = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) return ds;

        String sql = "SELECT * FROM ApDungKM WHERE maChiTietHD = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maChiTietHD);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm áp dụng KM theo chi tiết hóa đơn: " + e.getMessage());
        }
        return ds;
    }

    public boolean insert(ApDungKM adkm) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "INSERT INTO ApDungKM (maApDung, maChiTietHD, maChiTietKM) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, adkm.getMaApDung());
            ps.setString(2, adkm.getChiTietHoaDon().getMaChiTietHD());
            ps.setString(3, adkm.getChiTietKhuyenMai().getMaChiTietKM());
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
        ChiTietHoaDon cthd = daoCTHD.findById(rs.getString("maChiTietHD"));
        ChiTietKhuyenMai ctkm = daoCTKM.findById(rs.getString("maChiTietKM"));
        return new ApDungKM(maApDung, cthd, ctkm);
    }
}
