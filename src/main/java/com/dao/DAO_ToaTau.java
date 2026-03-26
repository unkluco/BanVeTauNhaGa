package com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.connectDB.ConnectDB;
import com.entity.ToaTau;
import com.enums.LoaiGhe;

public class DAO_ToaTau {

    public List<ToaTau> getAll() {
        List<ToaTau> ds = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) return ds;

        String sql = "SELECT * FROM ToaTau";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách toa tàu: " + e.getMessage());
        }
        return ds;
    }

    public ToaTau findById(String maToaTau) {
        Connection con = ConnectDB.getCon();
        if (con == null) return null;

        String sql = "SELECT * FROM ToaTau WHERE maToaTau = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maToaTau);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm toa tàu: " + e.getMessage());
        }
        return null;
    }

    public boolean insert(ToaTau tt) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "INSERT INTO ToaTau (maToaTau, loaiGhe) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tt.getMaToaTau());
            ps.setString(2, tt.getLoaiGhe().toDbValue());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm toa tàu: " + e.getMessage());
        }
        return false;
    }

    public boolean update(ToaTau tt) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "UPDATE ToaTau SET loaiGhe = ? WHERE maToaTau = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tt.getLoaiGhe().toDbValue());
            ps.setString(2, tt.getMaToaTau());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật toa tàu: " + e.getMessage());
        }
        return false;
    }

    private ToaTau mapRow(ResultSet rs) throws SQLException {
        return new ToaTau(
                rs.getString("maToaTau"),
                LoaiGhe.fromAny(rs.getString("loaiGhe"))
        );
    }
}
