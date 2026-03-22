package com.entity;

import java.time.LocalDateTime;

public class Lich {
    private String maLich;
    private Tuyen tuyen;
    private DoanTau doanTau;
    private LocalDateTime thoiGianBatDau;
    private String thoiGianChay;

    public Lich() {
        super();
    }

    public Lich(String maLich) {
        this.maLich = maLich;
    }

    public Lich(String maLich, Tuyen tuyen, DoanTau doanTau, LocalDateTime thoiGianBatDau, String thoiGianChay) {
        this.maLich = maLich;
        this.tuyen = tuyen;
        this.doanTau = doanTau;
        this.thoiGianBatDau = thoiGianBatDau;
        this.thoiGianChay = thoiGianChay;
    }

    public String getMaLich() { return maLich; }
    public void setMaLich(String maLich) { this.maLich = maLich; }
    public Tuyen getTuyen() { return tuyen; }
    public void setTuyen(Tuyen tuyen) { this.tuyen = tuyen; }
    public DoanTau getDoanTau() { return doanTau; }
    public void setDoanTau(DoanTau doanTau) { this.doanTau = doanTau; }
    public LocalDateTime getThoiGianBatDau() { return thoiGianBatDau; }
    public void setThoiGianBatDau(LocalDateTime thoiGianBatDau) { this.thoiGianBatDau = thoiGianBatDau; }
    public String getThoiGianChay() { return thoiGianChay; }
    public void setThoiGianChay(String thoiGianChay) { this.thoiGianChay = thoiGianChay; }

    @Override
    public String toString() {
        return "Lich{" +
                "maLich='" + maLich + '\'' +
                ", tuyen=" + tuyen +
                ", doanTau=" + doanTau +
                ", thoiGianBatDau=" + thoiGianBatDau +
                ", thoiGianChay='" + thoiGianChay + '\'' +
                '}';
    }
}
