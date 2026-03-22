package com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.connectDB.ConnectDB;
import com.entity.DauMay;

public class DAO_DauMay {

    public List<DauMay> getAll() {
        List<DauMay> ds = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) return ds;

        String sql = "SELECT * FROM DauMay";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách đầu máy: " + e.getMessage());
        }
        return ds;
    }

    public DauMay findById(String maDauMay) {
        Connection con = ConnectDB.getCon();
        if (con == null) return null;

        String sql = "SELECT * FROM DauMay WHERE maDauMay = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maDauMay);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm đầu máy: " + e.getMessage());
        }
        return null;
    }

    public boolean insert(DauMay dm) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "INSERT INTO DauMay (maDauMay, tenDauMay) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, dm.getMaDauMay());
            ps.setNString(2, dm.getTenDauMay());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm đầu máy: " + e.getMessage());
        }
        return false;
    }

    public boolean update(DauMay dm) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "UPDATE DauMay SET tenDauMay = ? WHERE maDauMay = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setNString(1, dm.getTenDauMay());
            ps.setString(2, dm.getMaDauMay());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật đầu máy: " + e.getMessage());
        }
        return false;
    }

    private DauMay mapRow(ResultSet rs) throws SQLException {
        return new DauMay(
                rs.getString("maDauMay"),
                rs.getNString("tenDauMay")
        );
    }
}
