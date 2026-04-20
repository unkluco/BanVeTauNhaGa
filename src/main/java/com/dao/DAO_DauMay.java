package com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.connectDB.ConnectDB;
import com.entity.DauMay;

public class DAO_DauMay {
    private static final String STATUS_ACTIVE      = "Đang hoạt động";
    private static final String STATUS_MAINTENANCE = "Đang bảo trì";
    private static final String STATUS_STOPPED     = "Ngừng hoạt động";
    private Boolean hasExtendedSchema;

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

    public String generateNextMaDauMay() {
        Connection con = ConnectDB.getCon();
        if (con == null) return "DM-001";

        String sql = "SELECT MAX(CAST(SUBSTRING(maDauMay, 4, LEN(maDauMay)-3) AS INT)) " +
                     "FROM DauMay WHERE maDauMay LIKE 'DM-%'";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int maxNum = rs.getInt(1);
                return String.format("DM-%03d", maxNum + 1);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi sinh mã đầu máy: " + e.getMessage());
        }
        return "DM-001";
    }

    public boolean insert(DauMay dm) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        if (supportsExtendedSchema(con)) {
            String sql = "INSERT INTO DauMay (maDauMay, tenDauMay, hangSanXuat, namSanXuat, congSuatKw, trangThai, moTa) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, dm.getMaDauMay());
                ps.setNString(2, dm.getTenDauMay());
                setNullableNString(ps, 3, dm.getHangSanXuat());
                setNullableInt(ps, 4, dm.getNamSanXuat());
                setNullableInt(ps, 5, dm.getCongSuatKw());
                ps.setNString(6, toDbTrangThai(dm.getTrangThai()));
                setNullableNString(ps, 7, dm.getMoTa());
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("Lỗi khi thêm đầu máy: " + e.getMessage());
            }
            return false;
        }

        String sqlLegacy = "INSERT INTO DauMay (maDauMay, tenDauMay) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sqlLegacy)) {
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

        if (supportsExtendedSchema(con)) {
            String sql = "UPDATE DauMay SET tenDauMay = ?, hangSanXuat = ?, namSanXuat = ?, " +
                         "congSuatKw = ?, trangThai = ?, moTa = ? WHERE maDauMay = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setNString(1, dm.getTenDauMay());
                setNullableNString(ps, 2, dm.getHangSanXuat());
                setNullableInt(ps, 3, dm.getNamSanXuat());
                setNullableInt(ps, 4, dm.getCongSuatKw());
                ps.setNString(5, toDbTrangThai(dm.getTrangThai()));
                setNullableNString(ps, 6, dm.getMoTa());
                ps.setString(7, dm.getMaDauMay());
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("Lỗi khi cập nhật đầu máy: " + e.getMessage());
            }
            return false;
        }

        String sqlLegacy = "UPDATE DauMay SET tenDauMay = ? WHERE maDauMay = ?";
        try (PreparedStatement ps = con.prepareStatement(sqlLegacy)) {
            ps.setNString(1, dm.getTenDauMay());
            ps.setString(2, dm.getMaDauMay());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật đầu máy: " + e.getMessage());
        }
        return false;
    }

    public boolean updateTrangThai(String maDauMay, String trangThai) {
        Connection con = ConnectDB.getCon();
        if (con == null || maDauMay == null || maDauMay.isBlank()) return false;
        if (!supportsExtendedSchema(con)) return false;

        String sql = "UPDATE DauMay SET trangThai = ? WHERE maDauMay = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setNString(1, toDbTrangThai(trangThai));
            ps.setString(2, maDauMay);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật trạng thái đầu máy: " + e.getMessage());
        }
        return false;
    }

    public int countDoanTauReferences(String maDauMay) {
        Connection con = ConnectDB.getCon();
        if (con == null || maDauMay == null || maDauMay.isBlank()) return 0;

        String sql = "SELECT COUNT(*) FROM DoanTau WHERE maDauMay = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maDauMay);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi kiểm tra tham chiếu đoàn tàu: " + e.getMessage());
        }
        return 0;
    }

    public boolean delete(String maDauMay) {
        Connection con = ConnectDB.getCon();
        if (con == null || maDauMay == null || maDauMay.isBlank()) return false;

        String sql = "DELETE FROM DauMay WHERE maDauMay = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maDauMay);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa đầu máy: " + e.getMessage());
        }
        return false;
    }

    private DauMay mapRow(ResultSet rs) throws SQLException {
        String ma = rs.getString("maDauMay");
        String ten = rs.getNString("tenDauMay");

        String hang = getNStringIfExists(rs, "hangSanXuat");
        Integer nam = getIntegerIfExists(rs, "namSanXuat");
        Integer congSuat = getIntegerIfExists(rs, "congSuatKw");
        String trangThai = getNStringIfExists(rs, "trangThai");
        String moTa = getNStringIfExists(rs, "moTa");

        return new DauMay(ma, ten, hang, nam, congSuat, toDisplayTrangThai(trangThai), moTa);
    }

    private boolean supportsExtendedSchema(Connection con) {
        if (hasExtendedSchema != null) return hasExtendedSchema;
        String sql = "SELECT COUNT(*) AS c FROM INFORMATION_SCHEMA.COLUMNS " +
                     "WHERE TABLE_NAME = 'DauMay' " +
                     "AND COLUMN_NAME IN ('hangSanXuat','namSanXuat','congSuatKw','trangThai','moTa')";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                hasExtendedSchema = (rs.getInt("c") == 5);
                return hasExtendedSchema;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi kiểm tra schema DauMay: " + e.getMessage());
        }
        hasExtendedSchema = false;
        return false;
    }

    private boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        int count = md.getColumnCount();
        for (int i = 1; i <= count; i++) {
            String label = md.getColumnLabel(i);
            String name = md.getColumnName(i);
            if (columnName.equalsIgnoreCase(label) || columnName.equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    private String getNStringIfExists(ResultSet rs, String columnName) throws SQLException {
        if (!hasColumn(rs, columnName)) return null;
        return rs.getNString(columnName);
    }

    private Integer getIntegerIfExists(ResultSet rs, String columnName) throws SQLException {
        if (!hasColumn(rs, columnName)) return null;
        int value = rs.getInt(columnName);
        return rs.wasNull() ? null : value;
    }

    private void setNullableNString(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null || value.isBlank()) {
            ps.setNull(index, java.sql.Types.NVARCHAR);
        } else {
            ps.setNString(index, value.trim());
        }
    }

    private void setNullableInt(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value == null) {
            ps.setNull(index, java.sql.Types.INTEGER);
        } else {
            ps.setInt(index, value);
        }
    }

    private String toDisplayTrangThai(String trangThai) {
        if (trangThai == null || trangThai.isBlank()) return STATUS_ACTIVE;
        String normalized = trangThai.trim().toLowerCase();
        if (normalized.contains("ngừng") || normalized.contains("ngung") || normalized.contains("khai thác")) {
            return STATUS_STOPPED;
        }
        if (normalized.contains("bảo trì") || normalized.contains("bao tri")) {
            return STATUS_MAINTENANCE;
        }
        return STATUS_ACTIVE;
    }

    private String toDbTrangThai(String trangThai) {
        // Giữ tương thích với schema cũ đang CHECK (Đang hoạt động | Bảo trì | Ngừng khai thác)
        if (trangThai == null || trangThai.isBlank()) return STATUS_ACTIVE;
        String normalized = trangThai.trim().toLowerCase();
        if (normalized.contains("ngừng") || normalized.contains("ngung")) return "Ngừng khai thác";
        if (normalized.contains("bảo trì") || normalized.contains("bao tri")) return "Bảo trì";
        return STATUS_ACTIVE;
    }
}
