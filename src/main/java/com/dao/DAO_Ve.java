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
import com.entity.HoaDon;
import com.entity.Ve;
import com.enums.TrangThaiVe;

public class DAO_Ve {

    private DAO_HoaDon daoHoaDon = new DAO_HoaDon();

    public List<Ve> getAll() {
        List<Ve> ds = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) return ds;

        String sql = "SELECT * FROM Ve";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách vé: " + e.getMessage());
        }
        return ds;
    }

    public Ve findById(String maVe) {
        Connection con = ConnectDB.getCon();
        if (con == null) return null;

        String sql = "SELECT * FROM Ve WHERE maVe = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maVe);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm vé: " + e.getMessage());
        }
        return null;
    }

    /**
     * Lấy danh sách vé theo hóa đơn
     */
    public List<Ve> findByHoaDon(String maHoaDon) {
        List<Ve> ds = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) return ds;

        String sql = "SELECT * FROM Ve WHERE maHoaDon = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHoaDon);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm vé theo hóa đơn: " + e.getMessage());
        }
        return ds;
    }

    /**
     * Tìm vé theo CCCD hành khách
     */
    public List<Ve> findByCCCD(String cccd) {
        List<Ve> ds = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) return ds;

        String sql = "SELECT * FROM Ve WHERE cccd = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, cccd);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm vé theo CCCD: " + e.getMessage());
        }
        return ds;
    }

    public boolean insert(Ve ve) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "INSERT INTO Ve (maVe, maHoaDon, tenHanhKhach, cccd, giaTien, trangThai, lyDoHuy, ngayHuy) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ve.getMaVe());
            ps.setString(2, ve.getHoaDon().getMaHoaDon());
            ps.setNString(3, ve.getTenHanhKhach());
            ps.setString(4, ve.getCccd());
            ps.setDouble(5, ve.getGiaTien());
            ps.setString(6, ve.getTrangThai().toDbValue());
            ps.setNString(7, ve.getLyDoHuy());
            if (ve.getNgayHuy() != null) {
                ps.setTimestamp(8, Timestamp.valueOf(ve.getNgayHuy()));
            } else {
                ps.setNull(8, java.sql.Types.TIMESTAMP);
            }
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm vé: " + e.getMessage());
        }
        return false;
    }

    /**
     * Hủy vé: cập nhật trạng thái, lý do hủy và ngày hủy
     */
    public boolean huyVe(String maVe, String lyDoHuy) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "UPDATE Ve SET trangThai = 'DA_HUY', lyDoHuy = ?, ngayHuy = GETDATE() WHERE maVe = ? AND trangThai = 'DA_BAN'";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setNString(1, lyDoHuy);
            ps.setString(2, maVe);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi hủy vé: " + e.getMessage());
        }
        return false;
    }

    public boolean update(Ve ve) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "UPDATE Ve SET maHoaDon = ?, tenHanhKhach = ?, cccd = ?, giaTien = ?, trangThai = ?, lyDoHuy = ?, ngayHuy = ? WHERE maVe = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ve.getHoaDon().getMaHoaDon());
            ps.setNString(2, ve.getTenHanhKhach());
            ps.setString(3, ve.getCccd());
            ps.setDouble(4, ve.getGiaTien());
            ps.setString(5, ve.getTrangThai().toDbValue());
            ps.setNString(6, ve.getLyDoHuy());
            if (ve.getNgayHuy() != null) {
                ps.setTimestamp(7, Timestamp.valueOf(ve.getNgayHuy()));
            } else {
                ps.setNull(7, java.sql.Types.TIMESTAMP);
            }
            ps.setString(8, ve.getMaVe());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật vé: " + e.getMessage());
        }
        return false;
    }

    private Ve mapRow(ResultSet rs) throws SQLException {
        String maVe = rs.getString("maVe");
        HoaDon hd = daoHoaDon.findById(rs.getString("maHoaDon"));
        String tenHanhKhach = rs.getNString("tenHanhKhach");
        String cccd = rs.getString("cccd");
        double giaTien = rs.getDouble("giaTien");
        TrangThaiVe trangThai = TrangThaiVe.fromAny(rs.getString("trangThai"));
        String lyDoHuy = rs.getNString("lyDoHuy");
        Timestamp tsHuy = rs.getTimestamp("ngayHuy");
        LocalDateTime ngayHuy = tsHuy != null ? tsHuy.toLocalDateTime() : null;
        return new Ve(maVe, hd, tenHanhKhach, cccd, giaTien, trangThai, lyDoHuy, ngayHuy);
    }
}
