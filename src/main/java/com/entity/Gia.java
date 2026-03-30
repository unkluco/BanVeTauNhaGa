package com.entity;

import java.time.LocalDate;

public class Gia {
    private String maGia;
    private LocalDate thoiGianBatDau;
    private LocalDate thoiGianKetThuc;
    private String moTa;
    private boolean trangThai;

    public Gia() {
        super();
    }

    public Gia(String maGia) {
        this.maGia = maGia;
    }

    public Gia(String maGia, LocalDate thoiGianBatDau, LocalDate thoiGianKetThuc, String moTa, boolean trangThai) {
        this.maGia = maGia;
        this.thoiGianBatDau = thoiGianBatDau;
        this.thoiGianKetThuc = thoiGianKetThuc;
        this.moTa = moTa;
        this.trangThai = trangThai;
    }

    public String getMaGia() { return maGia; }
    public void setMaGia(String maGia) { this.maGia = maGia; }
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
        return "Gia{" +
                "maGia='" + maGia + '\'' +
                ", thoiGianBatDau=" + thoiGianBatDau +
                ", thoiGianKetThuc=" + thoiGianKetThuc +
                ", moTa='" + moTa + '\'' +
                ", trangThai=" + trangThai +
                '}';
    }
}
