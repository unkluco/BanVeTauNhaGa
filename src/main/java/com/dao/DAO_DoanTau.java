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

    private static final String STATUS_ACTIVE = "Đang hoạt động";
    private static final String STATUS_STOPPED = "Ngừng khai thác";

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

    public List<DoanTau> getAllActive() {
        List<DoanTau> ds = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) return ds;

        String sql = "SELECT * FROM DoanTau WHERE trangThai = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setNString(1, STATUS_ACTIVE);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ds.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách đoàn tàu hoạt động: " + e.getMessage());
        }
        return ds;
    }

    public boolean insert(DoanTau dt) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "INSERT INTO DoanTau (maDoanTau, tenDoanTau, maDauMay, trangThai) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, dt.getMaDoanTau());
            ps.setNString(2, dt.getTenDoanTau());
            ps.setString(3, dt.getDauMay().getMaDauMay());
            ps.setNString(4, toDbTrangThai(dt.getTrangThai()));
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

    public boolean updateTrangThai(String maDoanTau, String trangThai) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "UPDATE DoanTau SET trangThai = ? WHERE maDoanTau = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setNString(1, toDbTrangThai(trangThai));
            ps.setString(2, maDoanTau);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật trạng thái đoàn tàu: " + e.getMessage());
        }
        return false;
    }

    public int countLichReferences(String maDoanTau) {
        Connection con = ConnectDB.getCon();
        if (con == null) return 0;

        String sql = "SELECT COUNT(*) FROM Lich WHERE maDoanTau = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maDoanTau);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi đếm tham chiếu lịch: " + e.getMessage());
        }
        return 0;
    }

    public boolean deleteWithDetails(String maDoanTau) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        try {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM ChiTietDoanTau WHERE maDoanTau = ?")) {
                ps.setString(1, maDoanTau);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM DoanTau WHERE maDoanTau = ?")) {
                ps.setString(1, maDoanTau);
                int affected = ps.executeUpdate();
                con.commit();
                return affected > 0;
            }
        } catch (SQLException e) {
            try { con.rollback(); } catch (SQLException ignored) {}
            System.err.println("Lỗi khi xóa đoàn tàu: " + e.getMessage());
        } finally {
            try { con.setAutoCommit(true); } catch (SQLException ignored) {}
        }
        return false;
    }

    private DoanTau mapRow(ResultSet rs) throws SQLException {
        String maDoanTau = rs.getString("maDoanTau");
        String tenDoanTau = rs.getNString("tenDoanTau");
        DauMay dauMay = daoDauMay.findById(rs.getString("maDauMay"));
        String trangThai = toDisplayTrangThai(rs.getNString("trangThai"));
        return new DoanTau(maDoanTau, tenDoanTau, dauMay, trangThai);
    }

    private String toDisplayTrangThai(String dbStatus) {
        if (dbStatus == null || dbStatus.isBlank()) return STATUS_ACTIVE;
        String normalized = dbStatus.trim().toLowerCase();
        if (normalized.contains("ngừng") || normalized.contains("ngung") || normalized.contains("khai thác") || normalized.contains("khai thac")) {
            return "Ngừng hoạt động";
        }
        return STATUS_ACTIVE;
    }

    private String toDbTrangThai(String input) {
        if (input == null || input.isBlank()) return STATUS_ACTIVE;
        String normalized = input.trim().toLowerCase();
        if (normalized.contains("ngừng") || normalized.contains("ngung") || normalized.contains("khai thác") || normalized.contains("khai thac")) {
            return STATUS_STOPPED;
        }
        return STATUS_ACTIVE;
    }
}
