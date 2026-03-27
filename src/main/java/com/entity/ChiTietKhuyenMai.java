package com.entity;

import com.enums.LoaiGhe;

public class ChiTietKhuyenMai {
    private String maChiTietKM;
    private KhuyenMai khuyenMai;
    private Tuyen tuyen;
    private LoaiGhe loaiGhe;
    private double phanTramGiam;

    public ChiTietKhuyenMai() {
        super();
    }

    public ChiTietKhuyenMai(String maChiTietKM) {
        this.maChiTietKM = maChiTietKM;
    }

    public ChiTietKhuyenMai(String maChiTietKM, KhuyenMai khuyenMai, Tuyen tuyen,
                              LoaiGhe loaiGhe, double phanTramGiam) {
        this.maChiTietKM = maChiTietKM;
        this.khuyenMai = khuyenMai;
        this.tuyen = tuyen;
        this.loaiGhe = loaiGhe;
        this.phanTramGiam = phanTramGiam;
    }

    public String getMaChiTietKM() { return maChiTietKM; }
    public void setMaChiTietKM(String maChiTietKM) { this.maChiTietKM = maChiTietKM; }
    public KhuyenMai getKhuyenMai() { return khuyenMai; }
    public void setKhuyenMai(KhuyenMai khuyenMai) { this.khuyenMai = khuyenMai; }
    public Tuyen getTuyen() { return tuyen; }
    public void setTuyen(Tuyen tuyen) { this.tuyen = tuyen; }
    public LoaiGhe getLoaiGhe() { return loaiGhe; }
    public void setLoaiGhe(LoaiGhe loaiGhe) { this.loaiGhe = loaiGhe; }
    public double getPhanTramGiam() { return phanTramGiam; }
    public void setPhanTramGiam(double phanTramGiam) { this.phanTramGiam = phanTramGiam; }

    @Override
    public String toString() {
        return "ChiTietKhuyenMai{" +
                "maChiTietKM='" + maChiTietKM + '\'' +
                ", khuyenMai=" + (khuyenMai != null ? khuyenMai.getMaKhuyenMai() : "null") +
                ", tuyen=" + (tuyen != null ? tuyen.getMaTuyen() : "null") +
                ", loaiGhe=" + loaiGhe +
                ", phanTramGiam=" + phanTramGiam +
                '}';
    }
}
