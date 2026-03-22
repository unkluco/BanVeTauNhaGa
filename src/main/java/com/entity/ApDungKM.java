package com.entity;

public class ApDungKM {
    private String maApDung;
    private Ve ve;
    private KhuyenMai khuyenMai;

    public ApDungKM() {
        super();
    }

    public ApDungKM(String maApDung, Ve ve, KhuyenMai khuyenMai) {
        this.maApDung = maApDung;
        this.ve = ve;
        this.khuyenMai = khuyenMai;
    }

    public String getMaApDung() { return maApDung; }
    public void setMaApDung(String maApDung) { this.maApDung = maApDung; }
    public Ve getVe() { return ve; }
    public void setVe(Ve ve) { this.ve = ve; }
    public KhuyenMai getKhuyenMai() { return khuyenMai; }
    public void setKhuyenMai(KhuyenMai khuyenMai) { this.khuyenMai = khuyenMai; }

    @Override
    public String toString() {
        return "ApDungKM{" +
                "maApDung='" + maApDung + '\'' +
                ", ve=" + (ve != null ? ve.getMaVe() : "null") +
                ", khuyenMai=" + (khuyenMai != null ? khuyenMai.getMaKhuyenMai() : "null") +
                '}';
    }
}
