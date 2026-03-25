package com.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.connectDB.ConnectDB;
import com.entity.ChiTietHoaDon;
import com.entity.HoaDon;
import com.entity.Ve;

public class DAO_ChiTietHoaDon {

    private DAO_HoaDon daoHoaDon = new DAO_HoaDon();
    private DAO_Ve daoVe = new DAO_Ve();

    public List<ChiTietHoaDon> getAll() {
        List<ChiTietHoaDon> ds = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) return ds;

        String sql = "SELECT * FROM ChiTietHoaDon";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Loi khi lay danh sach chi tiet hoa don: " + e.getMessage());
        }
        return ds;
    }

    public ChiTietHoaDon findById(String maChiTietHD) {
        Connection con = ConnectDB.getCon();
        if (con == null) return null;

        String sql = "SELECT * FROM ChiTietHoaDon WHERE maChiTietHD = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maChiTietHD);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Loi khi tim chi tiet hoa don: " + e.getMessage());
        }
        return null;
    }

    public List<ChiTietHoaDon> findByHoaDon(String maHoaDon) {
        List<ChiTietHoaDon> ds = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) return ds;

        String sql = "SELECT * FROM ChiTietHoaDon WHERE maHoaDon = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHoaDon);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Loi khi tim chi tiet hoa don theo hoa don: " + e.getMessage());
        }
        return ds;
    }

    public ChiTietHoaDon findByVe(String maVe) {
        Connection con = ConnectDB.getCon();
        if (con == null) return null;

        String sql = "SELECT * FROM ChiTietHoaDon WHERE maVe = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maVe);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Loi khi tim chi tiet hoa don theo ve: " + e.getMessage());
        }
        return null;
    }

    public boolean insert(ChiTietHoaDon cthd) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, giaTien) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, cthd.getMaChiTietHD());
            ps.setString(2, cthd.getHoaDon().getMaHoaDon());
            ps.setString(3, cthd.getVe().getMaVe());
            ps.setBigDecimal(4, cthd.getGiaTien());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Loi khi them chi tiet hoa don: " + e.getMessage());
        }
        return false;
    }

    public boolean update(ChiTietHoaDon cthd) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "UPDATE ChiTietHoaDon SET maHoaDon = ?, maVe = ?, giaTien = ? WHERE maChiTietHD = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, cthd.getHoaDon().getMaHoaDon());
            ps.setString(2, cthd.getVe().getMaVe());
            ps.setBigDecimal(3, cthd.getGiaTien());
            ps.setString(4, cthd.getMaChiTietHD());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Loi khi cap nhat chi tiet hoa don: " + e.getMessage());
        }
        return false;
    }

    private ChiTietHoaDon mapRow(ResultSet rs) throws SQLException {
        String maChiTietHD = rs.getString("maChiTietHD");
        HoaDon hd = daoHoaDon.findById(rs.getString("maHoaDon"));
        Ve ve = daoVe.findById(rs.getString("maVe"));
        BigDecimal giaTien = rs.getBigDecimal("giaTien");
        return new ChiTietHoaDon(maChiTietHD, hd, ve, giaTien);
    }
}
