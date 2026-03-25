package com.entity;

import java.util.Objects;

public class KhachHang {
    private String maKhachHang;
    private String hoTen;
    private String cccd;
    private String soDienThoai;

    public KhachHang() {
        super();
    }

    public KhachHang(String maKhachHang) {
        this.maKhachHang = maKhachHang;
    }

    public KhachHang(String maKhachHang, String hoTen, String cccd, String soDienThoai) {
        this.maKhachHang = maKhachHang;
        setHoTen(hoTen);
        setCccd(cccd);
        setSoDienThoai(soDienThoai);
    }

    public String getMaKhachHang() { return maKhachHang; }
    public void setMaKhachHang(String maKhachHang) { this.maKhachHang = maKhachHang; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) {
        if (hoTen == null || hoTen.trim().isEmpty())
            throw new IllegalArgumentException("Ho ten khong duoc de trong");
        if (hoTen.length() > 100)
            throw new IllegalArgumentException("Ho ten toi da 100 ky tu");
        this.hoTen = hoTen;
    }

    public String getCccd() { return cccd; }
    public void setCccd(String cccd) {
        if (cccd == null || cccd.trim().isEmpty())
            throw new IllegalArgumentException("CCCD khong duoc de trong");
        this.cccd = cccd;
    }

    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) {
        if (soDienThoai == null || soDienThoai.trim().isEmpty())
            throw new IllegalArgumentException("So dien thoai khong duoc de trong");
        this.soDienThoai = soDienThoai;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KhachHang that = (KhachHang) o;
        return Objects.equals(maKhachHang, that.maKhachHang);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maKhachHang);
    }

    @Override
    public String toString() {
        return "KhachHang{" +
                "maKhachHang='" + maKhachHang + '\'' +
                ", hoTen='" + hoTen + '\'' +
                ", cccd='" + cccd + '\'' +
                ", soDienThoai='" + soDienThoai + '\'' +
                '}';
    }
}
