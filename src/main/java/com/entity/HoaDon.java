package com.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HoaDon {
    private String maHoaDon;
    private NhanVien nhanVien;
    private LocalDateTime ngayLap;

    private List<Ve> danhSachVe = new ArrayList<>();

    public HoaDon() {
        super();
    }

    public HoaDon(String maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

    public HoaDon(String maHoaDon, NhanVien nhanVien, LocalDateTime ngayLap) {
        this.maHoaDon = maHoaDon;
        this.nhanVien = nhanVien;
        this.ngayLap = ngayLap;
    }

    public String getMaHoaDon() { return maHoaDon; }
    public void setMaHoaDon(String maHoaDon) { this.maHoaDon = maHoaDon; }
    public NhanVien getNhanVien() { return nhanVien; }
    public void setNhanVien(NhanVien nhanVien) { this.nhanVien = nhanVien; }
    public LocalDateTime getNgayLap() { return ngayLap; }
    public void setNgayLap(LocalDateTime ngayLap) { this.ngayLap = ngayLap; }
    public List<Ve> getDanhSachVe() { return danhSachVe; }
    public void setDanhSachVe(List<Ve> danhSachVe) { this.danhSachVe = danhSachVe; }

    public void addVe(Ve ve) {
        this.danhSachVe.add(ve);
    }

    /**
     * Thuoc tinh dan xuat: tinh tong tien tu cac ve
     */
    public double getTongTien() {
        double tong = 0;
        for (Ve ve : danhSachVe) {
            tong += ve.getGiaTien();
        }
        return tong;
    }

    @Override
    public String toString() {
        return "HoaDon{" +
                "maHoaDon='" + maHoaDon + '\'' +
                ", nhanVien=" + nhanVien +
                ", ngayLap=" + ngayLap +
                '}';
    }
}
