package com.entity;

import java.util.Objects;

public class HoaDonKhachHang {
    private String    maHDKH;
    private HoaDon    hoaDon;
    private KhachHang khachHang;

    public HoaDonKhachHang() {
        super();
    }

    public HoaDonKhachHang(String maHDKH) {
        this.maHDKH = maHDKH;
    }

    public HoaDonKhachHang(String maHDKH, HoaDon hoaDon, KhachHang khachHang) {
        this.maHDKH    = maHDKH;
        this.hoaDon    = hoaDon;
        this.khachHang = khachHang;
    }

    public String getMaHDKH() { return maHDKH; }
    public void   setMaHDKH(String maHDKH) { this.maHDKH = maHDKH; }

    public HoaDon getHoaDon() { return hoaDon; }
    public void   setHoaDon(HoaDon hoaDon) { this.hoaDon = hoaDon; }

    public KhachHang getKhachHang() { return khachHang; }
    public void      setKhachHang(KhachHang khachHang) { this.khachHang = khachHang; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HoaDonKhachHang)) return false;
        HoaDonKhachHang that = (HoaDonKhachHang) o;
        return Objects.equals(maHDKH, that.maHDKH);
    }

    @Override
    public int hashCode() { return Objects.hash(maHDKH); }

    @Override
    public String toString() {
        return "HoaDonKhachHang{maHDKH='" + maHDKH + "', hoaDon=" + hoaDon + ", khachHang=" + khachHang + "}";
    }
}
