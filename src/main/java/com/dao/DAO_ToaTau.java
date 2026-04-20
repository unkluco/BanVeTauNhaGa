package com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public String generateNextMaToaTau() {
        Connection con = ConnectDB.getCon();
        if (con == null) return "TOA-001";

        String sql = "SELECT MAX(CAST(SUBSTRING(maToaTau, 5, LEN(maToaTau)-4) AS INT)) " +
                     "FROM ToaTau WHERE maToaTau LIKE 'TOA-%'";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int maxNum = rs.getInt(1);
                return String.format("TOA-%03d", maxNum + 1);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi sinh mã toa: " + e.getMessage());
        }
        return "TOA-001";
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

    public boolean insertWithAutoSeats(ToaTau toaTau) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;
        if (toaTau == null
                || toaTau.getMaToaTau() == null
                || toaTau.getMaToaTau().isBlank()
                || toaTau.getLoaiGhe() == null) {
            return false;
        }

        final String insertToaSql = "INSERT INTO ToaTau (maToaTau, loaiGhe) VALUES (?, ?)";
        final String insertGheSql = "INSERT INTO Ghe (maGhe, maToaTau, soGhe) VALUES (?, ?, ?)";
        boolean oldAutoCommit = true;
        try {
            oldAutoCommit = con.getAutoCommit();
            con.setAutoCommit(false);

            try (PreparedStatement psToa = con.prepareStatement(insertToaSql);
                 PreparedStatement psGhe = con.prepareStatement(insertGheSql)) {
                psToa.setString(1, toaTau.getMaToaTau());
                psToa.setString(2, toaTau.getLoaiGhe().toDbValue());
                int n = psToa.executeUpdate();
                if (n <= 0) {
                    con.rollback();
                    return false;
                }

                int seatCount = defaultSeatCount(toaTau.getLoaiGhe());
                String toaSuffix = extractToaSuffix(toaTau.getMaToaTau());
                for (int i = 1; i <= seatCount; i++) {
                    String maGhe = String.format("G-%s-%02d", toaSuffix, i);
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
        return new ToaTau(
                rs.getString("maToaTau"),
                LoaiGhe.fromAny(rs.getString("loaiGhe"))
        );
    }

    private int defaultSeatCount(LoaiGhe loaiGhe) {
        if (loaiGhe == null) return 48;
        return switch (loaiGhe) {
            case GIUONG_NAM -> 30;
            case GHE_CUNG, GHE_MEM -> 48;
        };
    }

    private String extractToaSuffix(String maToaTau) {
        if (maToaTau == null) return "000";
        Matcher m = Pattern.compile("(\\d+)$").matcher(maToaTau.trim());
        if (m.find()) {
            int n = Integer.parseInt(m.group(1));
            return String.format("%03d", n % 1000);
        }
        int hash = Math.abs(maToaTau.hashCode()) % 1000;
        return String.format("%03d", hash);
    }
}
