package com.entity;

import java.time.LocalDateTime;

public class GiuCho {
    private String maGiuCho;
    private NhanVien nhanVien;
    private Lich lich;
    private Ghe ghe;
    private LocalDateTime thoiGianHetHan;

    public GiuCho() {
        super();
    }

    public GiuCho(String maGiuCho, NhanVien nhanVien, Lich lich, Ghe ghe, LocalDateTime thoiGianHetHan) {
        this.maGiuCho = maGiuCho;
        this.nhanVien = nhanVien;
        this.lich = lich;
        this.ghe = ghe;
        this.thoiGianHetHan = thoiGianHetHan;
    }

    public String getMaGiuCho() { return maGiuCho; }
    public void setMaGiuCho(String maGiuCho) { this.maGiuCho = maGiuCho; }
    public NhanVien getNhanVien() { return nhanVien; }
    public void setNhanVien(NhanVien nhanVien) { this.nhanVien = nhanVien; }
    public Lich getLich() { return lich; }
    public void setLich(Lich lich) { this.lich = lich; }
    public Ghe getGhe() { return ghe; }
    public void setGhe(Ghe ghe) { this.ghe = ghe; }
    public LocalDateTime getThoiGianHetHan() { return thoiGianHetHan; }
    public void setThoiGianHetHan(LocalDateTime thoiGianHetHan) { this.thoiGianHetHan = thoiGianHetHan; }

    public boolean conHieuLuc() {
        return thoiGianHetHan != null && thoiGianHetHan.isAfter(LocalDateTime.now());
    }

    @Override
    public String toString() {
        return "GiuCho{" +
                "maGiuCho='" + maGiuCho + '\'' +
                ", nhanVien=" + (nhanVien != null ? nhanVien.getMaNV() : "null") +
                ", lich=" + (lich != null ? lich.getMaLich() : "null") +
                ", ghe=" + (ghe != null ? ghe.getMaGhe() : "null") +
                ", thoiGianHetHan=" + thoiGianHetHan +
                '}';
    }
}
