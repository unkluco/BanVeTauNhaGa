package com.entity;

import com.enums.TrangThaiVe;

public class ChiTietVe {
    private String maChiTietVe;
    private Ve ve;
    private Lich lich;
    private Ghe ghe;
    private TrangThaiVe trangThai;

    public ChiTietVe() {
        super();
    }

    public ChiTietVe(String maChiTietVe, Ve ve, Lich lich, Ghe ghe, TrangThaiVe trangThai) {
        this.maChiTietVe = maChiTietVe;
        this.ve = ve;
        this.lich = lich;
        this.ghe = ghe;
        this.trangThai = trangThai;
    }

    public String getMaChiTietVe() { return maChiTietVe; }
    public void setMaChiTietVe(String maChiTietVe) { this.maChiTietVe = maChiTietVe; }
    public Ve getVe() { return ve; }
    public void setVe(Ve ve) { this.ve = ve; }
    public Lich getLich() { return lich; }
    public void setLich(Lich lich) { this.lich = lich; }
    public Ghe getGhe() { return ghe; }
    public void setGhe(Ghe ghe) { this.ghe = ghe; }
    public TrangThaiVe getTrangThai() { return trangThai; }
    public void setTrangThai(TrangThaiVe trangThai) { this.trangThai = trangThai; }

    @Override
    public String toString() {
        return "ChiTietVe{" +
                "maChiTietVe='" + maChiTietVe + '\'' +
                ", ve=" + (ve != null ? ve.getMaVe() : "null") +
                ", lich=" + (lich != null ? lich.getMaLich() : "null") +
                ", ghe=" + (ghe != null ? ghe.getMaGhe() : "null") +
                ", trangThai=" + trangThai +
                '}';
    }
}
