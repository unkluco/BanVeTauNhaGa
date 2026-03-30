package com.entity;

import com.enums.LoaiGhe;

public class ChiTietGia {
    private String maChiTietGia;
    private Gia gia;
    private Tuyen tuyen;
    private LoaiGhe loaiGhe;
    private double giaNiemYet;

    public ChiTietGia() {
        super();
    }

    public ChiTietGia(String maChiTietGia) {
        this.maChiTietGia = maChiTietGia;
    }

    public ChiTietGia(String maChiTietGia, Gia gia, Tuyen tuyen, LoaiGhe loaiGhe, double giaNiemYet) {
        this.maChiTietGia = maChiTietGia;
        this.gia = gia;
        this.tuyen = tuyen;
        this.loaiGhe = loaiGhe;
        this.giaNiemYet = giaNiemYet;
    }

    public String getMaChiTietGia() { return maChiTietGia; }
    public void setMaChiTietGia(String maChiTietGia) { this.maChiTietGia = maChiTietGia; }
    public Gia getGia() { return gia; }
    public void setGia(Gia gia) { this.gia = gia; }
    public Tuyen getTuyen() { return tuyen; }
    public void setTuyen(Tuyen tuyen) { this.tuyen = tuyen; }
    public LoaiGhe getLoaiGhe() { return loaiGhe; }
    public void setLoaiGhe(LoaiGhe loaiGhe) { this.loaiGhe = loaiGhe; }
    public double getGiaNiemYet() { return giaNiemYet; }
    public void setGiaNiemYet(double giaNiemYet) { this.giaNiemYet = giaNiemYet; }

    @Override
    public String toString() {
        return "ChiTietGia{" +
                "maChiTietGia='" + maChiTietGia + '\'' +
                ", gia=" + gia +
                ", tuyen=" + tuyen +
                ", loaiGhe=" + loaiGhe +
                ", giaNiemYet=" + giaNiemYet +
                '}';
    }
}
