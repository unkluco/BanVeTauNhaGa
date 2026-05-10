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
import com.entity.ChiTietGia;
import com.entity.Gia;
import com.util.MaTuDong;

public class DAO_Gia {

    public List<Gia> getAll() {
        List<Gia> ds = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) return ds;

        String sql = "SELECT * FROM Gia";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách giá: " + e.getMessage());
        }
        return ds;
    }

    public Gia findById(String maGia) {
        Connection con = ConnectDB.getCon();
        if (con == null) return null;

        String sql = "SELECT * FROM Gia WHERE maGia = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maGia);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm giá: " + e.getMessage());
        }
        return null;
    }

    /**
     * Kiểm tra có kỳ giá nào đang bật (trangThai=1) trùng khoảng thời gian không.
     * Dùng trước khi bật một kỳ giá để ngăn trùng.
     * @param excludeId  mã kỳ giá đang sửa (loại khỏi kết quả)
     * @param batDau     ngày bắt đầu kỳ giá cần kiểm tra
     * @param ketThuc    ngày kết thúc kỳ giá cần kiểm tra
     * @return danh sách kỳ giá bị trùng (rỗng = không trùng)
     */
    public List<Gia> findOverlappingActive(String excludeId, LocalDate batDau, LocalDate ketThuc) {
        List<Gia> ds = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) return ds;

        String sql = "SELECT * FROM Gia WHERE trangThai = 1 AND maGia != ? "
                   + "AND thoiGianBatDau <= ? AND thoiGianKetThuc >= ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, excludeId);
            ps.setDate(2, Date.valueOf(ketThuc));
            ps.setDate(3, Date.valueOf(batDau));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ds.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi kiểm tra trùng kỳ giá: " + e.getMessage());
        }
        return ds;
    }

    /**
     * Lấy kỳ giá đang áp dụng (trangThai = true)
     */
    public Gia getGiaHienHanh() {
        Connection con = ConnectDB.getCon();
        if (con == null) return null;

        String sql = "SELECT * FROM Gia WHERE trangThai = 1";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy giá hiện hành: " + e.getMessage());
        }
        return null;
    }

    public boolean insert(Gia gia) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "INSERT INTO Gia (maGia, thoiGianBatDau, thoiGianKetThuc, moTa, trangThai) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, gia.getMaGia());
            ps.setDate(2, Date.valueOf(gia.getThoiGianBatDau()));
            ps.setDate(3, Date.valueOf(gia.getThoiGianKetThuc()));
            ps.setNString(4, gia.getMoTa());
            ps.setBoolean(5, gia.isTrangThai());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm giá: " + e.getMessage());
        }
        return false;
    }

    public boolean update(Gia gia) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        String sql = "UPDATE Gia SET thoiGianBatDau = ?, thoiGianKetThuc = ?, moTa = ?, trangThai = ? WHERE maGia = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(gia.getThoiGianBatDau()));
            ps.setDate(2, Date.valueOf(gia.getThoiGianKetThuc()));
            ps.setNString(3, gia.getMoTa());
            ps.setBoolean(4, gia.isTrangThai());
            ps.setString(5, gia.getMaGia());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật giá: " + e.getMessage());
        }
        return false;
    }

    public boolean updateWithDeactivatedConflicts(Gia gia, List<String> conflictIds) {
        Connection con = ConnectDB.getCon();
        if (con == null) return false;
        boolean oldAutoCommit;
        try {
            oldAutoCommit = con.getAutoCommit();
            con.setAutoCommit(false);
            deactivateGiaIds(con, conflictIds);
            updateGia(con, gia);
            con.commit();
            con.setAutoCommit(oldAutoCommit);
            return true;
        } catch (SQLException e) {
            try { con.rollback(); } catch (SQLException rollbackEx) { System.err.println("Lỗi rollback cập nhật giá: " + rollbackEx.getMessage()); }
            try { con.setAutoCommit(true); } catch (SQLException restoreEx) { System.err.println("Lỗi khôi phục transaction giá: " + restoreEx.getMessage()); }
            System.err.println("Lỗi khi cập nhật giá kèm tắt kỳ trùng: " + e.getMessage());
        }
        return false;
    }

    public int countSoldTicketsUsingGia(String maGia) {
        Connection con = ConnectDB.getCon();
        if (con == null) return 0;

        String sql = "SELECT COUNT(DISTINCT cthd.maChiTietHD) AS total "
                + "FROM ChiTietHoaDon cthd "
                + "JOIN ChiTietGia ctg ON ctg.maChiTietGia = cthd.maChiTietGia "
                + "WHERE ctg.maGia = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maGia);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("total") : 0;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi kiểm tra giá đã áp dụng trên vé: " + e.getMessage());
        }
        return 0;
    }

    public Gia cloneGiaWithDetails(Gia editedGia) {
        return cloneGiaWithDetailsReplacingDetail(editedGia, null, false, List.of());
    }

    public Gia cloneGiaWithDetailsReplacingDetail(Gia editedGia, ChiTietGia editedDetail) {
        return cloneGiaWithDetailsReplacingDetail(editedGia, editedDetail, false, List.of());
    }

    public Gia cloneGiaWithDetailsReplacingDetail(Gia editedGia, ChiTietGia editedDetail,
                                                  boolean activateNew, List<String> conflictIdsToDeactivate) {
        Connection con = ConnectDB.getCon();
        if (con == null || editedGia == null) return null;

        boolean oldAutoCommit;
        try {
            oldAutoCommit = con.getAutoCommit();
            con.setAutoCommit(false);
        } catch (SQLException e) {
            System.err.println("Lỗi khi bắt đầu nhân bản giá: " + e.getMessage());
            return null;
        }

        String oldMaGia = editedGia.getMaGia();
        String newMaGia = MaTuDong.generate("GIA");
        Gia cloned = new Gia(newMaGia, editedGia.getThoiGianBatDau(), editedGia.getThoiGianKetThuc(),
                editedGia.getMoTa(), activateNew);
        try {
            updateTrangThaiGia(con, oldMaGia, false);
            deactivateGiaIds(con, conflictIdsToDeactivate);
            insertGia(con, cloned);
            cloneChiTietGia(con, oldMaGia, newMaGia, editedDetail);
            con.commit();
            con.setAutoCommit(oldAutoCommit);
            return cloned;
        } catch (SQLException e) {
            try { con.rollback(); } catch (SQLException rollbackEx) { System.err.println("Lỗi rollback nhân bản giá: " + rollbackEx.getMessage()); }
            try { con.setAutoCommit(oldAutoCommit); } catch (SQLException restoreEx) { System.err.println("Lỗi khôi phục transaction giá: " + restoreEx.getMessage()); }
            System.err.println("Lỗi khi nhân bản giá đã áp dụng: " + e.getMessage());
        }
        return null;
    }

    private void updateGia(Connection con, Gia gia) throws SQLException {
        String sql = "UPDATE Gia SET thoiGianBatDau = ?, thoiGianKetThuc = ?, moTa = ?, trangThai = ? WHERE maGia = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(gia.getThoiGianBatDau()));
            ps.setDate(2, Date.valueOf(gia.getThoiGianKetThuc()));
            ps.setNString(3, gia.getMoTa());
            ps.setBoolean(4, gia.isTrangThai());
            ps.setString(5, gia.getMaGia());
            if (ps.executeUpdate() == 0) throw new SQLException("Không cập nhật được kỳ giá");
        }
    }

    private void deactivateGiaIds(Connection con, List<String> maGiaList) throws SQLException {
        if (maGiaList == null || maGiaList.isEmpty()) return;
        for (String maGia : maGiaList) {
            updateTrangThaiGia(con, maGia, false);
        }
    }

    private void updateTrangThaiGia(Connection con, String maGia, boolean trangThai) throws SQLException {
        String sql = "UPDATE Gia SET trangThai = ? WHERE maGia = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBoolean(1, trangThai);
            ps.setString(2, maGia);
            if (ps.executeUpdate() == 0) throw new SQLException("Không cập nhật được trạng thái giá cũ");
        }
    }

    private void insertGia(Connection con, Gia gia) throws SQLException {
        String sql = "INSERT INTO Gia (maGia, thoiGianBatDau, thoiGianKetThuc, moTa, trangThai) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, gia.getMaGia());
            ps.setDate(2, Date.valueOf(gia.getThoiGianBatDau()));
            ps.setDate(3, Date.valueOf(gia.getThoiGianKetThuc()));
            ps.setNString(4, gia.getMoTa());
            ps.setBoolean(5, gia.isTrangThai());
            if (ps.executeUpdate() == 0) throw new SQLException("Không thêm được bản giá mới");
        }
    }

    private void cloneChiTietGia(Connection con, String oldMaGia, String newMaGia, ChiTietGia editedDetail) throws SQLException {
        String selectSql = "SELECT maChiTietGia, maTuyen, loaiGhe, giaNiemYet FROM ChiTietGia WHERE maGia = ?";
        String insertSql = "INSERT INTO ChiTietGia (maChiTietGia, maGia, maTuyen, loaiGhe, giaNiemYet) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement select = con.prepareStatement(selectSql);
             PreparedStatement insert = con.prepareStatement(insertSql)) {
            select.setString(1, oldMaGia);
            boolean replaced = editedDetail == null;
            Set<String> uniqueKeys = new HashSet<>();
            try (ResultSet rs = select.executeQuery()) {
                while (rs.next()) {
                    boolean replace = editedDetail != null
                            && editedDetail.getMaChiTietGia().equals(rs.getString("maChiTietGia"));
                    if (replace) replaced = true;
                    String maTuyen = replace ? editedDetail.getTuyen().getMaTuyen() : rs.getString("maTuyen");
                    String loaiGhe = replace ? editedDetail.getLoaiGhe().toDbValue() : rs.getString("loaiGhe");
                    String uniqueKey = maTuyen + "|" + loaiGhe;
                    if (!uniqueKeys.add(uniqueKey)) throw new SQLException("Trùng tuyến và loại ghế trong bản giá mới: " + uniqueKey);
                    insert.setString(1, MaTuDong.generate("CTG"));
                    insert.setString(2, newMaGia);
                    insert.setString(3, maTuyen);
                    insert.setString(4, loaiGhe);
                    insert.setDouble(5, replace ? editedDetail.getGiaNiemYet() : rs.getDouble("giaNiemYet"));
                    insert.addBatch();
                }
            }
            if (!replaced) throw new SQLException("Không tìm thấy chi tiết giá cần thay trong kỳ giá cũ");
            insert.executeBatch();
        }
    }

    private Gia mapRow(ResultSet rs) throws SQLException {
        String maGia = rs.getString("maGia");
        Date dBD = rs.getDate("thoiGianBatDau");
        Date dKT = rs.getDate("thoiGianKetThuc");
        LocalDate thoiGianBatDau = dBD != null ? dBD.toLocalDate() : null;
        LocalDate thoiGianKetThuc = dKT != null ? dKT.toLocalDate() : null;
        String moTa = rs.getNString("moTa");
        boolean trangThai = rs.getBoolean("trangThai");
        return new Gia(maGia, thoiGianBatDau, thoiGianKetThuc, moTa, trangThai);
    }
}
