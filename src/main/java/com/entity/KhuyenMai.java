package com.entity;

import java.time.LocalDate;

public class KhuyenMai {
    private String maKhuyenMai;
    private String tenKhuyenMai;
    private LocalDate thoiGianBatDau;
    private LocalDate thoiGianKetThuc;
    private String moTa;
    private boolean trangThai;

    public KhuyenMai() {
        super();
    }

    public KhuyenMai(String maKhuyenMai) {
        this.maKhuyenMai = maKhuyenMai;
    }

    public KhuyenMai(String maKhuyenMai, String tenKhuyenMai, LocalDate thoiGianBatDau,
                      LocalDate thoiGianKetThuc, String moTa, boolean trangThai) {
        this.maKhuyenMai = maKhuyenMai;
        this.tenKhuyenMai = tenKhuyenMai;
        this.thoiGianBatDau = thoiGianBatDau;
        this.thoiGianKetThuc = thoiGianKetThuc;
        this.moTa = moTa;
        this.trangThai = trangThai;
    }

    public String getMaKhuyenMai() { return maKhuyenMai; }
    public void setMaKhuyenMai(String maKhuyenMai) { this.maKhuyenMai = maKhuyenMai; }
    public String getTenKhuyenMai() { return tenKhuyenMai; }
    public void setTenKhuyenMai(String tenKhuyenMai) { this.tenKhuyenMai = tenKhuyenMai; }
    public LocalDate getThoiGianBatDau() { return thoiGianBatDau; }
    public void setThoiGianBatDau(LocalDate thoiGianBatDau) { this.thoiGianBatDau = thoiGianBatDau; }
    public LocalDate getThoiGianKetThuc() { return thoiGianKetThuc; }
    public void setThoiGianKetThuc(LocalDate thoiGianKetThuc) { this.thoiGianKetThuc = thoiGianKetThuc; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public boolean isTrangThai() { return trangThai; }
    public void setTrangThai(boolean trangThai) { this.trangThai = trangThai; }

    @Override
    public String toString() {
        return "KhuyenMai{" +
                "maKhuyenMai='" + maKhuyenMai + '\'' +
                ", tenKhuyenMai='" + tenKhuyenMai + '\'' +
                ", thoiGianBatDau=" + thoiGianBatDau +
                ", thoiGianKetThuc=" + thoiGianKetThuc +
                ", moTa='" + moTa + '\'' +
                ", trangThai=" + trangThai +
                '}';
    }
}
