package com.entity;

import java.time.LocalDateTime;

public class HoaDon {
    private String        maHoaDon;
    private NhanVien      nhanVien;
    private LocalDateTime ngayLap;

    public HoaDon() {
        super();
    }

    public HoaDon(String maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

    public HoaDon(String maHoaDon, NhanVien nhanVien, LocalDateTime ngayLap) {
        this.maHoaDon = maHoaDon;
        this.nhanVien = nhanVien;
        this.ngayLap  = ngayLap;
    }

    public String getMaHoaDon() { return maHoaDon; }
    public void   setMaHoaDon(String maHoaDon) { this.maHoaDon = maHoaDon; }

    public NhanVien getNhanVien() { return nhanVien; }
    public void     setNhanVien(NhanVien nhanVien) { this.nhanVien = nhanVien; }

    public LocalDateTime getNgayLap() { return ngayLap; }
    public void          setNgayLap(LocalDateTime ngayLap) { this.ngayLap = ngayLap; }

    @Override
    public String toString() {
        return "HoaDon{" +
                "maHoaDon='" + maHoaDon + '\'' +
                ", nhanVien=" + nhanVien +
                ", ngayLap=" + ngayLap +
                '}';
    }
}
