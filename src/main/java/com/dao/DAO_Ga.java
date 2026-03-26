package com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.connectDB.ConnectDB;
import com.entity.Ga;

public class DAO_Ga {

    public List<Ga> getAll() {
        List<Ga> ds = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) return ds;

        String sql = "SELECT * FROM Ga";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách ga: " + e.getMessage());
        }
        return ds;
    }

    public Ga findById(String maGa) {
        Connection con = ConnectDB.getCon();
        if (con == null) return null;

        String sql = "SELECT * FROM Ga WHERE maGa = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maGa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm ga: " + e.getMessage());
        }
        return null;
    }

    public boolean insert(Ga ga) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "INSERT INTO Ga (maGa, tenGa, diaChi) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ga.getMaGa());
            ps.setNString(2, ga.getTenGa());
            ps.setNString(3, ga.getDiaChi());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm ga: " + e.getMessage());
        }
        return false;
    }

    public boolean update(Ga ga) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "UPDATE Ga SET tenGa = ?, diaChi = ? WHERE maGa = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setNString(1, ga.getTenGa());
            ps.setNString(2, ga.getDiaChi());
            ps.setString(3, ga.getMaGa());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật ga: " + e.getMessage());
        }
        return false;
    }

    private Ga mapRow(ResultSet rs) throws SQLException {
        return new Ga(
                rs.getString("maGa"),
                rs.getNString("tenGa"),
                rs.getNString("diaChi")
        );
    }
}
