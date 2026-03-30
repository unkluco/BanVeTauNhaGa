package com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.connectDB.ConnectDB;
import com.entity.Ga;
import com.entity.Tuyen;

public class DAO_Tuyen {

    private DAO_Ga daoGa = new DAO_Ga();

    public List<Tuyen> getAll() {
        List<Tuyen> ds = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) return ds;

        String sql = "SELECT * FROM Tuyen";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách tuyến: " + e.getMessage());
        }
        return ds;
    }

    public Tuyen findById(String maTuyen) {
        Connection con = ConnectDB.getCon();
        if (con == null) return null;

        String sql = "SELECT * FROM Tuyen WHERE maTuyen = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maTuyen);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm tuyến: " + e.getMessage());
        }
        return null;
    }

    /**
     * Tìm tuyến theo ga đi và ga đến
     */
    public List<Tuyen> findByGaDiGaDen(String maGaDi, String maGaDen) {
        List<Tuyen> ds = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) return ds;

        String sql = "SELECT * FROM Tuyen WHERE gaDi = ? AND gaDen = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maGaDi);
            ps.setString(2, maGaDen);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm tuyến theo ga: " + e.getMessage());
        }
        return ds;
    }

    public boolean insert(Tuyen tuyen) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tuyen.getMaTuyen());
            ps.setString(2, tuyen.getGaDi().getMaGa());
            ps.setString(3, tuyen.getGaDen().getMaGa());
            ps.setInt(4, tuyen.getKm());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm tuyến: " + e.getMessage());
        }
        return false;
    }

    public boolean update(Tuyen tuyen) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "UPDATE Tuyen SET gaDi = ?, gaDen = ?, km = ? WHERE maTuyen = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tuyen.getGaDi().getMaGa());
            ps.setString(2, tuyen.getGaDen().getMaGa());
            ps.setInt(3, tuyen.getKm());
            ps.setString(4, tuyen.getMaTuyen());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật tuyến: " + e.getMessage());
        }
        return false;
    }

    public boolean delete(String maTuyen) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "DELETE FROM Tuyen WHERE maTuyen = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maTuyen);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa tuyến: " + e.getMessage());
        }
        return false;
    }

    private Tuyen mapRow(ResultSet rs) throws SQLException {
        String maTuyen = rs.getString("maTuyen");
        Ga gaDi = daoGa.findById(rs.getString("gaDi"));
        Ga gaDen = daoGa.findById(rs.getString("gaDen"));
        int km = rs.getInt("km");
        return new Tuyen(maTuyen, gaDi, gaDen, km);
    }
}
