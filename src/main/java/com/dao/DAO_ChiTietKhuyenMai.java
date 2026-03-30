package com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.connectDB.ConnectDB;
import com.entity.ChiTietKhuyenMai;
import com.entity.KhuyenMai;
import com.entity.Tuyen;
import com.enums.LoaiGhe;

public class DAO_ChiTietKhuyenMai {

    private DAO_KhuyenMai daoKM = new DAO_KhuyenMai();
    private DAO_Tuyen daoTuyen = new DAO_Tuyen();

    public List<ChiTietKhuyenMai> getAll() {
        List<ChiTietKhuyenMai> ds = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) return ds;

        String sql = "SELECT * FROM ChiTietKhuyenMai";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách chi tiết khuyến mãi: " + e.getMessage());
        }
        return ds;
    }

    public ChiTietKhuyenMai findById(String maChiTietKM) {
        Connection con = ConnectDB.getCon();
        if (con == null) return null;

        String sql = "SELECT * FROM ChiTietKhuyenMai WHERE maChiTietKM = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maChiTietKM);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm chi tiết khuyến mãi: " + e.getMessage());
        }
        return null;
    }

    /**
     * Lấy chi tiết khuyến mãi theo kỳ KM, tuyến và loại ghế
     */
    public ChiTietKhuyenMai findByKMTuyenLoaiGhe(String maKhuyenMai, String maTuyen, String loaiGhe) {
        Connection con = ConnectDB.getCon();
        if (con == null) return null;

        String sql = "SELECT * FROM ChiTietKhuyenMai WHERE maKhuyenMai = ? AND maTuyen = ? AND loaiGhe = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maKhuyenMai);
            ps.setString(2, maTuyen);
            ps.setString(3, loaiGhe);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm chi tiết khuyến mãi theo tuyến: " + e.getMessage());
        }
        return null;
    }

    /**
     * Lấy danh sách chi tiết khuyến mãi theo kỳ KM
     */
    public List<ChiTietKhuyenMai> findByKhuyenMai(String maKhuyenMai) {
        List<ChiTietKhuyenMai> ds = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) return ds;

        String sql = "SELECT * FROM ChiTietKhuyenMai WHERE maKhuyenMai = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maKhuyenMai);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm chi tiết khuyến mãi theo kỳ KM: " + e.getMessage());
        }
        return ds;
    }

    public boolean insert(ChiTietKhuyenMai ctkm) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ctkm.getMaChiTietKM());
            ps.setString(2, ctkm.getKhuyenMai().getMaKhuyenMai());
            if (ctkm.getTuyen() != null) ps.setString(3, ctkm.getTuyen().getMaTuyen());
            else ps.setNull(3, java.sql.Types.VARCHAR);
            if (ctkm.getLoaiGhe() != null) ps.setString(4, ctkm.getLoaiGhe().toDbValue());
            else ps.setNull(4, java.sql.Types.VARCHAR);
            ps.setString(5, ctkm.getTenChiTiet());
            ps.setDouble(6, ctkm.getPhanTramGiam());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm chi tiết khuyến mãi: " + e.getMessage());
        }
        return false;
    }

    public boolean update(ChiTietKhuyenMai ctkm) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "UPDATE ChiTietKhuyenMai SET maKhuyenMai = ?, maTuyen = ?, loaiGhe = ?, tenChiTiet = ?, phanTramGiam = ? WHERE maChiTietKM = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ctkm.getKhuyenMai().getMaKhuyenMai());
            if (ctkm.getTuyen() != null) ps.setString(2, ctkm.getTuyen().getMaTuyen());
            else ps.setNull(2, java.sql.Types.VARCHAR);
            if (ctkm.getLoaiGhe() != null) ps.setString(3, ctkm.getLoaiGhe().toDbValue());
            else ps.setNull(3, java.sql.Types.VARCHAR);
            ps.setString(4, ctkm.getTenChiTiet());
            ps.setDouble(5, ctkm.getPhanTramGiam());
            ps.setString(6, ctkm.getMaChiTietKM());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật chi tiết khuyến mãi: " + e.getMessage());
        }
        return false;
    }

    public boolean delete(String maChiTietKM) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;
        String sql = "DELETE FROM ChiTietKhuyenMai WHERE maChiTietKM = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maChiTietKM);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa chi tiết khuyến mãi: " + e.getMessage());
        }
        return false;
    }

    private ChiTietKhuyenMai mapRow(ResultSet rs) throws SQLException {
        String maChiTietKM = rs.getString("maChiTietKM");
        String tenChiTiet  = rs.getString("tenChiTiet");
        KhuyenMai km = daoKM.findById(rs.getString("maKhuyenMai"));
        String maTuyenVal = rs.getString("maTuyen");
        Tuyen tuyen = (maTuyenVal != null) ? daoTuyen.findById(maTuyenVal) : null;
        String loaiGheVal = rs.getString("loaiGhe");
        LoaiGhe loaiGhe = (loaiGheVal != null) ? LoaiGhe.fromAny(loaiGheVal) : null;
        double phanTramGiam = rs.getDouble("phanTramGiam");
        return new ChiTietKhuyenMai(maChiTietKM, tenChiTiet, km, tuyen, loaiGhe, phanTramGiam);
    }
}
