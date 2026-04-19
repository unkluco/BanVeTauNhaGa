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
    private static final String DEFAULT_TRANG_THAI = "Đang hoạt động";
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
                ps.setNString(6, normalizeTrangThai(dm.getTrangThai()));
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
                ps.setNString(5, normalizeTrangThai(dm.getTrangThai()));
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

    private DauMay mapRow(ResultSet rs) throws SQLException {
        String ma = rs.getString("maDauMay");
        String ten = rs.getNString("tenDauMay");

        String hang = getNStringIfExists(rs, "hangSanXuat");
        Integer nam = getIntegerIfExists(rs, "namSanXuat");
        Integer congSuat = getIntegerIfExists(rs, "congSuatKw");
        String trangThai = getNStringIfExists(rs, "trangThai");
        String moTa = getNStringIfExists(rs, "moTa");

        return new DauMay(ma, ten, hang, nam, congSuat, normalizeTrangThai(trangThai), moTa);
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

    private String normalizeTrangThai(String trangThai) {
        if (trangThai == null || trangThai.isBlank()) return DEFAULT_TRANG_THAI;
        return trangThai.trim();
    }
}
