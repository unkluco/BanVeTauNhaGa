package com.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.connectDB.ConnectDB;
import com.entity.ChiTietKhuyenMai;
import com.entity.KhuyenMai;
import com.util.MaTuDong;

public class DAO_KhuyenMai {

    public List<KhuyenMai> getAll() {
        List<KhuyenMai> ds = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) return ds;

        String sql = "SELECT * FROM KhuyenMai";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách khuyến mãi: " + e.getMessage());
        }
        return ds;
    }

    public KhuyenMai findById(String maKhuyenMai) {
        Connection con = ConnectDB.getCon();
        if (con == null) return null;

        String sql = "SELECT * FROM KhuyenMai WHERE maKhuyenMai = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maKhuyenMai);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm khuyến mãi: " + e.getMessage());
        }
        return null;
    }

    /**
     * Lấy kỳ khuyến mãi đang áp dụng (trangThai = true)
     */
    public KhuyenMai getKhuyenMaiHienHanh() {
        Connection con = ConnectDB.getCon();
        if (con == null) return null;

        String sql = "SELECT * FROM KhuyenMai WHERE trangThai = 1";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy khuyến mãi hiện hành: " + e.getMessage());
        }
        return null;
    }

    public boolean insert(KhuyenMai km) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "INSERT INTO KhuyenMai (maKhuyenMai, tenKhuyenMai, thoiGianBatDau, thoiGianKetThuc, moTa, trangThai) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, km.getMaKhuyenMai());
            ps.setNString(2, km.getTenKhuyenMai());
            ps.setDate(3, Date.valueOf(km.getThoiGianBatDau()));
            ps.setDate(4, Date.valueOf(km.getThoiGianKetThuc()));
            ps.setNString(5, km.getMoTa());
            ps.setBoolean(6, km.isTrangThai());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm khuyến mãi: " + e.getMessage());
        }
        return false;
    }

    public boolean update(KhuyenMai km) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "UPDATE KhuyenMai SET tenKhuyenMai = ?, thoiGianBatDau = ?, thoiGianKetThuc = ?, moTa = ?, trangThai = ? WHERE maKhuyenMai = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setNString(1, km.getTenKhuyenMai());
            ps.setDate(2, Date.valueOf(km.getThoiGianBatDau()));
            ps.setDate(3, Date.valueOf(km.getThoiGianKetThuc()));
            ps.setNString(4, km.getMoTa());
            ps.setBoolean(5, km.isTrangThai());
            ps.setString(6, km.getMaKhuyenMai());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật khuyến mãi: " + e.getMessage());
        }
        return false;
    }

    public int countAppliedUsage(String maKhuyenMai) {
        Connection con = ConnectDB.getCon();
        if (con == null) return 0;
        String sql = "SELECT COUNT(DISTINCT ad.maApDung) AS total "
                + "FROM ApDungKM ad "
                + "JOIN ChiTietKhuyenMai ctkm ON ctkm.maChiTietKM = ad.maChiTietKM "
                + "WHERE ctkm.maKhuyenMai = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maKhuyenMai);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("total") : 0;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi kiểm tra khuyến mãi đã áp dụng: " + e.getMessage());
        }
        return 0;
    }

    public KhuyenMai cloneKhuyenMaiWithDetails(KhuyenMai editedKm, boolean activateNew) {
        return cloneKhuyenMaiWithDetails(editedKm, activateNew, null);
    }

    public KhuyenMai cloneKhuyenMaiWithDetails(KhuyenMai editedKm, boolean activateNew, String cloneTenKhuyenMai) {
        return cloneKhuyenMaiWithDetails(editedKm, null, false, false, activateNew, cloneTenKhuyenMai);
    }

    public KhuyenMai cloneKhuyenMaiWithDetailsReplacingDetail(KhuyenMai sourceKm, ChiTietKhuyenMai editedDetail,
                                                              boolean activateNew) {
        return cloneKhuyenMaiWithDetailsReplacingDetail(sourceKm, editedDetail, activateNew, null);
    }

    public KhuyenMai cloneKhuyenMaiWithDetailsReplacingDetail(KhuyenMai sourceKm, ChiTietKhuyenMai editedDetail,
                                                              boolean activateNew, String cloneTenKhuyenMai) {
        return cloneKhuyenMaiWithDetails(sourceKm, editedDetail, true, false, activateNew, cloneTenKhuyenMai);
    }

    public KhuyenMai cloneKhuyenMaiWithDetailsRemovingDetail(KhuyenMai sourceKm, ChiTietKhuyenMai removedDetail,
                                                             boolean activateNew) {
        return cloneKhuyenMaiWithDetailsRemovingDetail(sourceKm, removedDetail, activateNew, null);
    }

    public KhuyenMai cloneKhuyenMaiWithDetailsRemovingDetail(KhuyenMai sourceKm, ChiTietKhuyenMai removedDetail,
                                                             boolean activateNew, String cloneTenKhuyenMai) {
        return cloneKhuyenMaiWithDetails(sourceKm, removedDetail, false, true, activateNew, cloneTenKhuyenMai);
    }

    public KhuyenMai cloneKhuyenMaiWithDetailsAddingDetail(KhuyenMai sourceKm, ChiTietKhuyenMai addedDetail,
                                                           boolean activateNew) {
        return cloneKhuyenMaiWithDetailsAddingDetail(sourceKm, addedDetail, activateNew, null);
    }

    public KhuyenMai cloneKhuyenMaiWithDetailsAddingDetail(KhuyenMai sourceKm, ChiTietKhuyenMai addedDetail,
                                                           boolean activateNew, String cloneTenKhuyenMai) {
        return cloneKhuyenMaiWithDetails(sourceKm, addedDetail, false, false, activateNew, cloneTenKhuyenMai);
    }

    private KhuyenMai cloneKhuyenMaiWithDetails(KhuyenMai sourceKm, ChiTietKhuyenMai changedDetail,
                                                boolean replaceDetail, boolean removeDetail,
                                                boolean activateNew, String cloneTenKhuyenMai) {
        Connection con = ConnectDB.getCon();
        if (con == null || sourceKm == null) return null;
        boolean oldAutoCommit;
        try {
            oldAutoCommit = con.getAutoCommit();
            con.setAutoCommit(false);
        } catch (SQLException e) {
            System.err.println("Lỗi khi bắt đầu nhân bản khuyến mãi: " + e.getMessage());
            return null;
        }
        String oldMaKm = sourceKm.getMaKhuyenMai();
        String newMaKm = MaTuDong.generate("KM");
        String newTenKhuyenMai = (cloneTenKhuyenMai != null && !cloneTenKhuyenMai.trim().isEmpty())
                ? cloneTenKhuyenMai.trim() : sourceKm.getTenKhuyenMai();
        KhuyenMai cloned = new KhuyenMai(newMaKm, newTenKhuyenMai, sourceKm.getThoiGianBatDau(),
                sourceKm.getThoiGianKetThuc(), sourceKm.getMoTa(), activateNew);
        try {
            updateTrangThaiKhuyenMai(con, oldMaKm, false);
            insertKhuyenMai(con, cloned);
            cloneChiTietKhuyenMai(con, oldMaKm, newMaKm, changedDetail, replaceDetail, removeDetail);
            con.commit();
            con.setAutoCommit(oldAutoCommit);
            return cloned;
        } catch (SQLException e) {
            try { con.rollback(); } catch (SQLException rollbackEx) { System.err.println("Lỗi rollback nhân bản khuyến mãi: " + rollbackEx.getMessage()); }
            try { con.setAutoCommit(oldAutoCommit); } catch (SQLException restoreEx) { System.err.println("Lỗi khôi phục transaction khuyến mãi: " + restoreEx.getMessage()); }
            System.err.println("Lỗi khi nhân bản khuyến mãi đã áp dụng: " + e.getMessage());
        }
        // Handoff: tên clone được override từ UI để tránh bản khuyến mãi mới trùng tên bản cũ.
        // Rủi ro: các overload cũ truyền null vẫn giữ hành vi cũ cho caller chưa cập nhật.
        return null;
    }

    private void insertKhuyenMai(Connection con, KhuyenMai km) throws SQLException {
        String sql = "INSERT INTO KhuyenMai (maKhuyenMai, tenKhuyenMai, thoiGianBatDau, thoiGianKetThuc, moTa, trangThai) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, km.getMaKhuyenMai());
            ps.setNString(2, km.getTenKhuyenMai());
            ps.setDate(3, Date.valueOf(km.getThoiGianBatDau()));
            ps.setDate(4, Date.valueOf(km.getThoiGianKetThuc()));
            ps.setNString(5, km.getMoTa());
            ps.setBoolean(6, km.isTrangThai());
            if (ps.executeUpdate() == 0) throw new SQLException("Không thêm được khuyến mãi mới");
        }
    }

    private void updateKhuyenMai(Connection con, KhuyenMai km) throws SQLException {
        String sql = "UPDATE KhuyenMai SET tenKhuyenMai = ?, thoiGianBatDau = ?, thoiGianKetThuc = ?, moTa = ?, trangThai = ? WHERE maKhuyenMai = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setNString(1, km.getTenKhuyenMai());
            ps.setDate(2, Date.valueOf(km.getThoiGianBatDau()));
            ps.setDate(3, Date.valueOf(km.getThoiGianKetThuc()));
            ps.setNString(4, km.getMoTa());
            ps.setBoolean(5, km.isTrangThai());
            ps.setString(6, km.getMaKhuyenMai());
            if (ps.executeUpdate() == 0) throw new SQLException("Không cập nhật được khuyến mãi");
        }
    }

    private void updateTrangThaiKhuyenMai(Connection con, String maKhuyenMai, boolean trangThai) throws SQLException {
        String sql = "UPDATE KhuyenMai SET trangThai = ? WHERE maKhuyenMai = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBoolean(1, trangThai);
            ps.setString(2, maKhuyenMai);
            if (ps.executeUpdate() == 0) throw new SQLException("Không cập nhật được trạng thái khuyến mãi cũ");
        }
    }

    private void cloneChiTietKhuyenMai(Connection con, String oldMaKm, String newMaKm, ChiTietKhuyenMai changedDetail,
                                       boolean replaceDetail, boolean removeDetail) throws SQLException {
        String selectSql = "SELECT maChiTietKM, maTuyen, loaiGhe, tenChiTiet, phanTramGiam FROM ChiTietKhuyenMai WHERE maKhuyenMai = ?";
        String insertSql = "INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement select = con.prepareStatement(selectSql);
             PreparedStatement insert = con.prepareStatement(insertSql)) {
            select.setString(1, oldMaKm);
            boolean appendDetail = changedDetail != null && !replaceDetail && !removeDetail;
            boolean touched = changedDetail == null || appendDetail;
            Set<String> uniqueKeys = new HashSet<>();
            try (ResultSet rs = select.executeQuery()) {
                while (rs.next()) {
                    boolean same = changedDetail != null && changedDetail.getMaChiTietKM().equals(rs.getString("maChiTietKM"));
                    if (same) touched = true;
                    if (same && removeDetail) continue;
                    String maTuyen = same && replaceDetail ? changedDetail.getTuyen() == null ? null : changedDetail.getTuyen().getMaTuyen() : rs.getString("maTuyen");
                    String loaiGhe = same && replaceDetail ? changedDetail.getLoaiGhe() == null ? null : changedDetail.getLoaiGhe().toDbValue() : rs.getString("loaiGhe");
                    String tenChiTiet = same && replaceDetail ? changedDetail.getTenChiTiet() : rs.getNString("tenChiTiet");
                    double phanTramGiam = same && replaceDetail ? changedDetail.getPhanTramGiam() : rs.getDouble("phanTramGiam");
                    String uniqueKey = String.valueOf(maTuyen) + "|" + String.valueOf(loaiGhe);
                    if (!uniqueKeys.add(uniqueKey)) throw new SQLException("Trùng tuyến và loại ghế trong khuyến mãi mới: " + uniqueKey);
                    insert.setString(1, MaTuDong.generate("CTKM"));
                    insert.setString(2, newMaKm);
                    insert.setString(3, maTuyen);
                    insert.setString(4, loaiGhe);
                    insert.setNString(5, tenChiTiet);
                    insert.setDouble(6, phanTramGiam);
                    insert.addBatch();
                }
            }
            if (!touched) throw new SQLException("Không tìm thấy chi tiết khuyến mãi cần thay trong bản cũ");
            if (appendDetail) addChiTietKhuyenMaiBatch(insert, newMaKm, changedDetail, uniqueKeys);
            insert.executeBatch();
        }
    }

    private void addChiTietKhuyenMaiBatch(PreparedStatement insert, String newMaKm, ChiTietKhuyenMai detail,
                                          Set<String> uniqueKeys) throws SQLException {
        String maTuyen = detail.getTuyen() == null ? null : detail.getTuyen().getMaTuyen();
        String loaiGhe = detail.getLoaiGhe() == null ? null : detail.getLoaiGhe().toDbValue();
        String uniqueKey = String.valueOf(maTuyen) + "|" + String.valueOf(loaiGhe);
        if (!uniqueKeys.add(uniqueKey)) throw new SQLException("Trùng tuyến và loại ghế trong khuyến mãi mới: " + uniqueKey);
        insert.setString(1, MaTuDong.generate("CTKM"));
        insert.setString(2, newMaKm);
        insert.setString(3, maTuyen);
        insert.setString(4, loaiGhe);
        insert.setNString(5, detail.getTenChiTiet());
        insert.setDouble(6, detail.getPhanTramGiam());
        insert.addBatch();
    }

    public boolean delete(String maKhuyenMai) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;
        String sql = "DELETE FROM KhuyenMai WHERE maKhuyenMai = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maKhuyenMai);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa khuyến mãi: " + e.getMessage());
        }
        return false;
    }

    private KhuyenMai mapRow(ResultSet rs) throws SQLException {
        String maKM = rs.getString("maKhuyenMai");
        String tenKM = rs.getNString("tenKhuyenMai");
        Date dBD = rs.getDate("thoiGianBatDau");
        Date dKT = rs.getDate("thoiGianKetThuc");
        LocalDate bd = dBD != null ? dBD.toLocalDate() : null;
        LocalDate kt = dKT != null ? dKT.toLocalDate() : null;
        String moTa = rs.getNString("moTa");
        boolean trangThai = rs.getBoolean("trangThai");
        return new KhuyenMai(maKM, tenKM, bd, kt, moTa, trangThai);
    }
}
