package com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.connectDB.ConnectDB;
import com.entity.DoanTau;
import com.entity.Lich;
import com.entity.Tuyen;

public class DAO_Lich {

    private DAO_Tuyen daoTuyen = new DAO_Tuyen();
    private DAO_DoanTau daoDoanTau = new DAO_DoanTau();

    public List<Lich> getAll() {
        List<Lich> ds = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) return ds;

        String sql = "SELECT * FROM Lich ORDER BY thoiGianBatDau";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách lịch: " + e.getMessage());
        }
        return ds;
    }

    public Lich findById(String maLich) {
        Connection con = ConnectDB.getCon();
        if (con == null) return null;

        String sql = "SELECT * FROM Lich WHERE maLich = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maLich);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm lịch: " + e.getMessage());
        }
        return null;
    }

    /**
     * Tìm lịch theo tuyến và ngày khởi hành
     */
    public List<Lich> findByTuyenAndDate(String maTuyen, LocalDateTime fromDate, LocalDateTime toDate) {
        List<Lich> ds = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) return ds;

        String sql = "SELECT * FROM Lich WHERE maTuyen = ? AND thoiGianBatDau >= ? AND thoiGianBatDau <= ? ORDER BY thoiGianBatDau";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maTuyen);
            ps.setTimestamp(2, Timestamp.valueOf(fromDate));
            ps.setTimestamp(3, Timestamp.valueOf(toDate));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm lịch theo tuyến: " + e.getMessage());
        }
        return ds;
    }

    public boolean insert(Lich lich) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, lich.getMaLich());
            ps.setString(2, lich.getTuyen().getMaTuyen());
            ps.setString(3, lich.getDoanTau().getMaDoanTau());
            ps.setTimestamp(4, Timestamp.valueOf(lich.getThoiGianBatDau()));
            ps.setNString(5, lich.getThoiGianChay());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm lịch: " + e.getMessage());
        }
        return false;
    }

    public boolean update(Lich lich) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "UPDATE Lich SET maTuyen = ?, maDoanTau = ?, thoiGianBatDau = ?, thoiGianChay = ? WHERE maLich = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, lich.getTuyen().getMaTuyen());
            ps.setString(2, lich.getDoanTau().getMaDoanTau());
            ps.setTimestamp(3, Timestamp.valueOf(lich.getThoiGianBatDau()));
            ps.setNString(4, lich.getThoiGianChay());
            ps.setString(5, lich.getMaLich());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật lịch: " + e.getMessage());
        }
        return false;
    }

    private Lich mapRow(ResultSet rs) throws SQLException {
        String maLich = rs.getString("maLich");
        Tuyen tuyen = daoTuyen.findById(rs.getString("maTuyen"));
        DoanTau doanTau = daoDoanTau.findById(rs.getString("maDoanTau"));
        Timestamp ts = rs.getTimestamp("thoiGianBatDau");
        LocalDateTime thoiGianBatDau = ts != null ? ts.toLocalDateTime() : null;
        String thoiGianChay = rs.getNString("thoiGianChay");
        return new Lich(maLich, tuyen, doanTau, thoiGianBatDau, thoiGianChay);
    }
}
