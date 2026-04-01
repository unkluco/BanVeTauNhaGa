package com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.connectDB.ConnectDB;
import com.entity.ChiTietDoanTau;
import com.entity.DoanTau;
import com.entity.ToaTau;

public class DAO_ChiTietDoanTau {

    private DAO_DoanTau daoDoanTau = new DAO_DoanTau();
    private DAO_ToaTau daoToaTau = new DAO_ToaTau();

    public List<ChiTietDoanTau> getAll() {
        List<ChiTietDoanTau> ds = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) return ds;

        String sql = "SELECT * FROM ChiTietDoanTau ORDER BY maDoanTau, soThuTu";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách chi tiết đoàn tàu: " + e.getMessage());
        }
        return ds;
    }

    /**
     * Lấy danh sách chi tiết toa tàu theo mã đoàn tàu (sắp xếp theo thứ tự toa)
     */
    public List<ChiTietDoanTau> findByDoanTau(String maDoanTau) {
        List<ChiTietDoanTau> ds = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) return ds;

        String sql = "SELECT * FROM ChiTietDoanTau WHERE maDoanTau = ? ORDER BY soThuTu";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maDoanTau);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm chi tiết đoàn tàu: " + e.getMessage());
        }
        return ds;
    }

    /**
     * Lấy danh sách đoàn tàu đang dùng một toa cụ thể (qua ChiTietDoanTau)
     */
    public List<DoanTau> findDoanTauByToaTau(String maToaTau) {
        List<DoanTau> ds = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) return ds;

        String sql = "SELECT DISTINCT maDoanTau FROM ChiTietDoanTau WHERE maToaTau = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maToaTau);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DoanTau dt = daoDoanTau.findById(rs.getString("maDoanTau"));
                    if (dt != null) ds.add(dt);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm đoàn tàu theo toa: " + e.getMessage());
        }
        return ds;
    }

    public boolean insert(ChiTietDoanTau ct) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "INSERT INTO ChiTietDoanTau (maChiTietDT, maDoanTau, maToaTau, soThuTu) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ct.getMaChiTietDT());
            ps.setString(2, ct.getDoanTau().getMaDoanTau());
            ps.setString(3, ct.getToaTau().getMaToaTau());
            ps.setInt(4, ct.getSoThuTu());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm chi tiết đoàn tàu: " + e.getMessage());
        }
        return false;
    }

    public boolean update(ChiTietDoanTau ct) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "UPDATE ChiTietDoanTau SET maDoanTau = ?, maToaTau = ?, soThuTu = ? WHERE maChiTietDT = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ct.getDoanTau().getMaDoanTau());
            ps.setString(2, ct.getToaTau().getMaToaTau());
            ps.setInt(3, ct.getSoThuTu());
            ps.setString(4, ct.getMaChiTietDT());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật chi tiết đoàn tàu: " + e.getMessage());
        }
        return false;
    }

    private ChiTietDoanTau mapRow(ResultSet rs) throws SQLException {
        String maChiTietDT = rs.getString("maChiTietDT");
        DoanTau doanTau = daoDoanTau.findById(rs.getString("maDoanTau"));
        ToaTau toaTau = daoToaTau.findById(rs.getString("maToaTau"));
        int soThuTu = rs.getInt("soThuTu");
        return new ChiTietDoanTau(maChiTietDT, doanTau, toaTau, soThuTu);
    }
}
