package com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.connectDB.ConnectDB;
import com.entity.DauMay;
import com.entity.DoanTau;

public class DAO_DoanTau {

    private DAO_DauMay daoDauMay = new DAO_DauMay();

    public List<DoanTau> getAll() {
        List<DoanTau> ds = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) return ds;

        String sql = "SELECT * FROM DoanTau";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách đoàn tàu: " + e.getMessage());
        }
        return ds;
    }

    public DoanTau findById(String maDoanTau) {
        Connection con = ConnectDB.getCon();
        if (con == null) return null;

        String sql = "SELECT * FROM DoanTau WHERE maDoanTau = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maDoanTau);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm đoàn tàu: " + e.getMessage());
        }
        return null;
    }

    public boolean insert(DoanTau dt) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "INSERT INTO DoanTau (maDoanTau, tenDoanTau, maDauMay) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, dt.getMaDoanTau());
            ps.setNString(2, dt.getTenDoanTau());
            ps.setString(3, dt.getDauMay().getMaDauMay());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm đoàn tàu: " + e.getMessage());
        }
        return false;
    }

    public boolean update(DoanTau dt) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "UPDATE DoanTau SET tenDoanTau = ?, maDauMay = ? WHERE maDoanTau = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setNString(1, dt.getTenDoanTau());
            ps.setString(2, dt.getDauMay().getMaDauMay());
            ps.setString(3, dt.getMaDoanTau());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật đoàn tàu: " + e.getMessage());
        }
        return false;
    }

    private DoanTau mapRow(ResultSet rs) throws SQLException {
        String maDoanTau = rs.getString("maDoanTau");
        String tenDoanTau = rs.getNString("tenDoanTau");
        DauMay dauMay = daoDauMay.findById(rs.getString("maDauMay"));
        return new DoanTau(maDoanTau, tenDoanTau, dauMay);
    }
}
