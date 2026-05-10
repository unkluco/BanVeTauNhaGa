package com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.connectDB.ConnectDB;
import com.entity.ToaTau;
import com.enums.LoaiGhe;
import com.util.MaTuDong;

public class DAO_ToaTau {
    private static final String STATUS_ACTIVE      = "Đang hoạt động";
    private static final String STATUS_MAINTENANCE = "Đang bảo trì";
    private static final String STATUS_STOPPED     = "Ngừng hoạt động";
    private Boolean hasStatusColumn;

    private boolean ensureTrangThaiColumn(Connection con) {
        if (hasStatusColumn != null) return hasStatusColumn;
        if (con == null) {
            hasStatusColumn = false;
            return false;
        }

        final String checkSql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_NAME = 'ToaTau' AND COLUMN_NAME = 'trangThai'";
        try (PreparedStatement ps = con.prepareStatement(checkSql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next() && rs.getInt(1) > 0) {
                hasStatusColumn = true;
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi kiểm tra cột trangThai của ToaTau: " + e.getMessage());
            hasStatusColumn = false;
            return false;
        }

        // Tự mở rộng schema cho DB cũ chưa có trạng thái toa.
        final String alterSql = "ALTER TABLE ToaTau ADD trangThai NVARCHAR(30) NOT NULL " +
                "CONSTRAINT DF_ToaTau_trangThai DEFAULT N'" + STATUS_ACTIVE + "'";
        try (PreparedStatement ps = con.prepareStatement(alterSql)) {
            ps.executeUpdate();
            hasStatusColumn = true;
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi khi tự thêm cột trangThai cho ToaTau: " + e.getMessage());
            hasStatusColumn = false;
            return false;
        }
    }

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

    public String generateNextMaToaTau() {
        return MaTuDong.generate("TOA");
    }

    public boolean insert(ToaTau tt) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        boolean hasStatus = ensureTrangThaiColumn(con);
        String sql = hasStatus
                ? "INSERT INTO ToaTau (maToaTau, loaiGhe, trangThai) VALUES (?, ?, ?)"
                : "INSERT INTO ToaTau (maToaTau, loaiGhe) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tt.getMaToaTau());
            ps.setString(2, tt.getLoaiGhe().toDbValue());
            if (hasStatus) {
                ps.setNString(3, toDbTrangThai(tt.getTrangThai()));
            }
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm toa tàu: " + e.getMessage());
        }
        return false;
    }

    public boolean insertWithAutoSeats(ToaTau toaTau) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;
        if (toaTau == null
                || toaTau.getMaToaTau() == null
                || toaTau.getMaToaTau().isBlank()
                || toaTau.getLoaiGhe() == null) {
            return false;
        }

        boolean hasStatus = ensureTrangThaiColumn(con);
        final String insertToaSql = hasStatus
                ? "INSERT INTO ToaTau (maToaTau, loaiGhe, trangThai) VALUES (?, ?, ?)"
                : "INSERT INTO ToaTau (maToaTau, loaiGhe) VALUES (?, ?)";
        final String insertGheSql = "INSERT INTO Ghe (maGhe, maToaTau, soGhe) VALUES (?, ?, ?)";
        boolean oldAutoCommit = true;
        try {
            oldAutoCommit = con.getAutoCommit();
            con.setAutoCommit(false);

            try (PreparedStatement psToa = con.prepareStatement(insertToaSql);
                 PreparedStatement psGhe = con.prepareStatement(insertGheSql)) {
                psToa.setString(1, toaTau.getMaToaTau());
                psToa.setString(2, toaTau.getLoaiGhe().toDbValue());
                if (hasStatus) {
                    psToa.setNString(3, toDbTrangThai(toaTau.getTrangThai()));
                }
                int n = psToa.executeUpdate();
                if (n <= 0) {
                    con.rollback();
                    return false;
                }

                int seatCount = defaultSeatCount(toaTau.getLoaiGhe());
                for (int i = 1; i <= seatCount; i++) {
                    String maGhe = MaTuDong.generate("GHE");
                    psGhe.setString(1, maGhe);
                    psGhe.setString(2, toaTau.getMaToaTau());
                    psGhe.setInt(3, i);
                    psGhe.addBatch();
                }
                psGhe.executeBatch();

                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                System.err.println("Lỗi khi thêm toa và sinh ghế: " + e.getMessage());
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi transaction thêm toa: " + e.getMessage());
            return false;
        } finally {
            try {
                con.setAutoCommit(oldAutoCommit);
            } catch (SQLException e) {
                System.err.println("Lỗi khi khôi phục autoCommit: " + e.getMessage());
            }
        }
    }

    public boolean update(ToaTau tt) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        boolean hasStatus = ensureTrangThaiColumn(con);
        String sql = hasStatus
                ? "UPDATE ToaTau SET loaiGhe = ?, trangThai = ? WHERE maToaTau = ?"
                : "UPDATE ToaTau SET loaiGhe = ? WHERE maToaTau = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tt.getLoaiGhe().toDbValue());
            if (hasStatus) {
                ps.setNString(2, toDbTrangThai(tt.getTrangThai()));
                ps.setString(3, tt.getMaToaTau());
            } else {
                ps.setString(2, tt.getMaToaTau());
            }
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật toa tàu: " + e.getMessage());
        }
        return false;
    }

    public boolean updateTrangThai(String maToaTau, String trangThai) {
        Connection con = ConnectDB.getCon();
        if (con == null || maToaTau == null || maToaTau.isBlank()) return false;
        if (!ensureTrangThaiColumn(con)) return false;

        String sql = "UPDATE ToaTau SET trangThai = ? WHERE maToaTau = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setNString(1, toDbTrangThai(trangThai));
            ps.setString(2, maToaTau);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật trạng thái toa tàu: " + e.getMessage());
        }
        return false;
    }

    public int countDoanTauReferences(String maToaTau) {
        Connection con = ConnectDB.getCon();
        if (con == null || maToaTau == null || maToaTau.isBlank()) return 0;

        String sql = "SELECT COUNT(*) FROM ChiTietDoanTau WHERE maToaTau = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maToaTau);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi kiểm tra tham chiếu ChiTietDoanTau: " + e.getMessage());
        }
        return 0;
    }

    public int countSeatUsageReferences(String maToaTau) {
        Connection con = ConnectDB.getCon();
        if (con == null || maToaTau == null || maToaTau.isBlank()) return 0;

        String sql = "SELECT " +
                "(SELECT COUNT(*) FROM Ve v INNER JOIN Ghe g ON v.maGhe = g.maGhe WHERE g.maToaTau = ?) " +
                "+ " +
                "(SELECT COUNT(*) FROM GiuCho gc INNER JOIN Ghe g ON gc.maGhe = g.maGhe WHERE g.maToaTau = ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maToaTau);
            ps.setString(2, maToaTau);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi kiểm tra tham chiếu Ve/GiuCho: " + e.getMessage());
        }
        return 0;
    }

    public boolean deleteWithSeats(String maToaTau) {
        Connection con = ConnectDB.getCon();
        if (con == null || maToaTau == null || maToaTau.isBlank()) return false;

        final String deleteGheSql = "DELETE FROM Ghe WHERE maToaTau = ?";
        final String deleteToaSql = "DELETE FROM ToaTau WHERE maToaTau = ?";
        boolean oldAutoCommit = true;
        try {
            oldAutoCommit = con.getAutoCommit();
            con.setAutoCommit(false);

            try (PreparedStatement psGhe = con.prepareStatement(deleteGheSql);
                 PreparedStatement psToa = con.prepareStatement(deleteToaSql)) {
                psGhe.setString(1, maToaTau);
                psGhe.executeUpdate();

                psToa.setString(1, maToaTau);
                int deleted = psToa.executeUpdate();
                if (deleted <= 0) {
                    con.rollback();
                    return false;
                }

                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                System.err.println("Lỗi khi xóa toa và ghế: " + e.getMessage());
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi transaction xóa toa: " + e.getMessage());
            return false;
        } finally {
            try {
                con.setAutoCommit(oldAutoCommit);
            } catch (SQLException e) {
                System.err.println("Lỗi khi khôi phục autoCommit sau xóa toa: " + e.getMessage());
            }
        }
    }

    private ToaTau mapRow(ResultSet rs) throws SQLException {
        String trangThai = hasColumn(rs, "trangThai")
                ? toDisplayTrangThai(rs.getNString("trangThai"))
                : STATUS_ACTIVE;
        return new ToaTau(
                rs.getString("maToaTau"),
                LoaiGhe.fromAny(rs.getString("loaiGhe")),
                trangThai
        );
    }

    private int defaultSeatCount(LoaiGhe loaiGhe) {
        if (loaiGhe == null) return 48;
        return switch (loaiGhe) {
            case GIUONG_NAM -> 30;
            case GHE_CUNG, GHE_MEM -> 48;
        };
    }


    private boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        for (int i = 1; i <= md.getColumnCount(); i++) {
            String label = md.getColumnLabel(i);
            String name = md.getColumnName(i);
            if (columnName.equalsIgnoreCase(label) || columnName.equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    private String toDisplayTrangThai(String trangThai) {
        if (trangThai == null || trangThai.isBlank()) return STATUS_ACTIVE;
        String normalized = trangThai.trim().toLowerCase();
        if (normalized.contains("ngừng") || normalized.contains("ngung") || normalized.contains("khai thác")) {
            return STATUS_STOPPED;
        }
        if (normalized.contains("bảo trì") || normalized.contains("bao tri")) return STATUS_MAINTENANCE;
        return STATUS_ACTIVE;
    }

    private String toDbTrangThai(String trangThai) {
        // Ưu tiên tương thích DB cũ (CHECK: Đang hoạt động | Bảo trì | Ngừng khai thác)
        if (trangThai == null || trangThai.isBlank()) return STATUS_ACTIVE;
        String normalized = trangThai.trim().toLowerCase();
        if (normalized.contains("ngừng") || normalized.contains("ngung")) return "Ngừng khai thác";
        if (normalized.contains("bảo trì") || normalized.contains("bao tri")) return "Bảo trì";
        return STATUS_ACTIVE;
    }
}
