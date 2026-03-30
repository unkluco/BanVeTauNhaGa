package com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.connectDB.ConnectDB;
import com.entity.ChiTietGia;
import com.entity.Gia;
import com.entity.Tuyen;
import com.enums.LoaiGhe;

public class DAO_ChiTietGia {

    private DAO_Gia daoGia = new DAO_Gia();
    private DAO_Tuyen daoTuyen = new DAO_Tuyen();

    public List<ChiTietGia> getAll() {
        List<ChiTietGia> ds = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) return ds;

        String sql = "SELECT * FROM ChiTietGia";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách chi tiết giá: " + e.getMessage());
        }
        return ds;
    }

    public ChiTietGia findById(String maChiTietGia) {
        Connection con = ConnectDB.getCon();
        if (con == null) return null;

        String sql = "SELECT * FROM ChiTietGia WHERE maChiTietGia = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maChiTietGia);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm chi tiết giá: " + e.getMessage());
        }
        return null;
    }

    /**
     * Lấy giá niêm yết theo kỳ giá, tuyến và loại ghế
     */
    public ChiTietGia findByGiaTuyenLoaiGhe(String maGia, String maTuyen, String loaiGhe) {
        Connection con = ConnectDB.getCon();
        if (con == null) return null;

        String sql = "SELECT * FROM ChiTietGia WHERE maGia = ? AND maTuyen = ? AND loaiGhe = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maGia);
            ps.setString(2, maTuyen);
            ps.setString(3, loaiGhe);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm chi tiết giá theo tuyến: " + e.getMessage());
        }
        return null;
    }

    /**
     * Lấy danh sách chi tiết giá theo kỳ giá
     */
    public List<ChiTietGia> findByGia(String maGia) {
        List<ChiTietGia> ds = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) return ds;

        String sql = "SELECT * FROM ChiTietGia WHERE maGia = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maGia);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm chi tiết giá theo kỳ giá: " + e.getMessage());
        }
        return ds;
    }

    public boolean insert(ChiTietGia ctg) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "INSERT INTO ChiTietGia (maChiTietGia, maGia, maTuyen, loaiGhe, giaNiemYet) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ctg.getMaChiTietGia());
            ps.setString(2, ctg.getGia().getMaGia());
            ps.setString(3, ctg.getTuyen().getMaTuyen());
            ps.setString(4, ctg.getLoaiGhe().toDbValue());
            ps.setDouble(5, ctg.getGiaNiemYet());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm chi tiết giá: " + e.getMessage());
        }
        return false;
    }

    public boolean update(ChiTietGia ctg) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "UPDATE ChiTietGia SET maGia = ?, maTuyen = ?, loaiGhe = ?, giaNiemYet = ? WHERE maChiTietGia = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ctg.getGia().getMaGia());
            ps.setString(2, ctg.getTuyen().getMaTuyen());
            ps.setString(3, ctg.getLoaiGhe().toDbValue());
            ps.setDouble(4, ctg.getGiaNiemYet());
            ps.setString(5, ctg.getMaChiTietGia());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật chi tiết giá: " + e.getMessage());
        }
        return false;
    }

    public boolean delete(String maChiTietGia) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "DELETE FROM ChiTietGia WHERE maChiTietGia = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maChiTietGia);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa chi tiết giá: " + e.getMessage());
        }
        return false;
    }

    private ChiTietGia mapRow(ResultSet rs) throws SQLException {
        String maChiTietGia = rs.getString("maChiTietGia");
        Gia gia = daoGia.findById(rs.getString("maGia"));
        Tuyen tuyen = daoTuyen.findById(rs.getString("maTuyen"));
        LoaiGhe loaiGhe = LoaiGhe.fromAny(rs.getString("loaiGhe"));
        double giaNiemYet = rs.getDouble("giaNiemYet");
        return new ChiTietGia(maChiTietGia, gia, tuyen, loaiGhe, giaNiemYet);
    }
}
