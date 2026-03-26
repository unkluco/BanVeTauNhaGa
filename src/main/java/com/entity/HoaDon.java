package com.entity;

import java.time.LocalDateTime;

public class HoaDon {
    private String maHoaDon;
    private NhanVien nhanVien;
    private KhachHang khachHang;
    private LocalDateTime ngayLap;

    public HoaDon() {
        super();
    }

    public HoaDon(String maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

    // Backward-compatible 3-param constructor
    public HoaDon(String maHoaDon, NhanVien nhanVien, LocalDateTime ngayLap) {
        this(maHoaDon, nhanVien, null, ngayLap);
    }

    public HoaDon(String maHoaDon, NhanVien nhanVien, KhachHang khachHang, LocalDateTime ngayLap) {
        this.maHoaDon = maHoaDon;
        this.nhanVien = nhanVien;
        this.khachHang = khachHang;
        this.ngayLap = ngayLap;
    }

    public String getMaHoaDon() { return maHoaDon; }
    public void setMaHoaDon(String maHoaDon) { this.maHoaDon = maHoaDon; }
    public NhanVien getNhanVien() { return nhanVien; }
    public void setNhanVien(NhanVien nhanVien) { this.nhanVien = nhanVien; }
    public KhachHang getKhachHang() { return khachHang; }
    public void setKhachHang(KhachHang khachHang) { this.khachHang = khachHang; }
    public LocalDateTime getNgayLap() { return ngayLap; }
    public void setNgayLap(LocalDateTime ngayLap) { this.ngayLap = ngayLap; }

    @Override
    public String toString() {
        return "HoaDon{" +
                "maHoaDon='" + maHoaDon + '\'' +
                ", nhanVien=" + nhanVien +
                ", khachHang=" + khachHang +
                ", ngayLap=" + ngayLap +
                '}';
    }
}
