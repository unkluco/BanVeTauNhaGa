package com.entity;

import java.math.BigDecimal;
import java.util.Objects;

public class ChiTietHoaDon {
    private String maChiTietHD;
    private HoaDon hoaDon;
    private Ve ve;
    private BigDecimal giaTien;

    public ChiTietHoaDon() {
        super();
    }

    public ChiTietHoaDon(String maChiTietHD) {
        this.maChiTietHD = maChiTietHD;
    }

    public ChiTietHoaDon(String maChiTietHD, HoaDon hoaDon, Ve ve, BigDecimal giaTien) {
        this.maChiTietHD = maChiTietHD;
        this.hoaDon = hoaDon;
        this.ve = ve;
        this.giaTien = giaTien;
    }

    public String getMaChiTietHD() { return maChiTietHD; }
    public void setMaChiTietHD(String maChiTietHD) { this.maChiTietHD = maChiTietHD; }

    public HoaDon getHoaDon() { return hoaDon; }
    public void setHoaDon(HoaDon hoaDon) { this.hoaDon = hoaDon; }

    public Ve getVe() { return ve; }
    public void setVe(Ve ve) { this.ve = ve; }

    public BigDecimal getGiaTien() { return giaTien; }
    public void setGiaTien(BigDecimal giaTien) {
        if (giaTien != null && giaTien.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Gia tien phai lon hon 0");
        this.giaTien = giaTien;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChiTietHoaDon that = (ChiTietHoaDon) o;
        return Objects.equals(maChiTietHD, that.maChiTietHD);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maChiTietHD);
    }

    @Override
    public String toString() {
        return "ChiTietHoaDon{" +
                "maChiTietHD='" + maChiTietHD + '\'' +
                ", hoaDon=" + (hoaDon != null ? hoaDon.getMaHoaDon() : "null") +
                ", ve=" + (ve != null ? ve.getMaVe() : "null") +
                ", giaTien=" + giaTien +
                '}';
    }
}
