package com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.connectDB.ConnectDB;
import com.entity.HoaDon;
import com.entity.HoaDonKhachHang;
import com.entity.KhachHang;
import com.util.MaTuDong;

public class DAO_HoaDonKhachHang {

    private final DAO_KhachHang daoKH = new DAO_KhachHang();

    public boolean insert(HoaDonKhachHang link) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "INSERT INTO HoaDonKhachHang (maHDKH, maHoaDon, maKhachHang) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, link.getMaHDKH());
            ps.setString(2, link.getHoaDon().getMaHoaDon());
            ps.setString(3, link.getKhachHang().getMaKhachHang());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Loi khi them HoaDonKhachHang: " + e.getMessage());
        }
        return false;
    }

    /**
     * Lay danh sach KhachHang cua mot HoaDon theo thu tu chen (maHDKH tang dan).
     */
    public List<KhachHang> findKhachHangByHoaDon(String maHoaDon) {
        List<KhachHang> ds = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) return ds;

        String sql = "SELECT maKhachHang FROM HoaDonKhachHang WHERE maHoaDon = ? ORDER BY maHDKH";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHoaDon);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    KhachHang kh = daoKH.findById(rs.getString("maKhachHang"));
                    if (kh != null) ds.add(kh);
                }
            }
        } catch (SQLException e) {
            System.err.println("Loi khi lay KhachHang theo HoaDon: " + e.getMessage());
        }
        return ds;
    }

    public List<HoaDon> findHoaDonByKhachHang(String maKhachHang) {
        List<HoaDon> ds = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) return ds;

        DAO_HoaDon daoHD = new DAO_HoaDon();
        String sql = "SELECT maHoaDon FROM HoaDonKhachHang WHERE maKhachHang = ? ORDER BY maHDKH";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maKhachHang);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HoaDon hd = daoHD.findById(rs.getString("maHoaDon"));
                    if (hd != null) ds.add(hd);
                }
            }
        } catch (SQLException e) {
            System.err.println("Loi khi lay HoaDon theo KhachHang: " + e.getMessage());
        }
        return ds;
    }

    public String phatSinhMaHDKH() {
        return MaTuDong.generate("HDKH");
    }
}
