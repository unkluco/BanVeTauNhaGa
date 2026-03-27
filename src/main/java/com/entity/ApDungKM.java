package com.entity;

public class ApDungKM {
    private String maApDung;
    private ChiTietHoaDon chiTietHoaDon;
    private ChiTietKhuyenMai chiTietKhuyenMai;

    public ApDungKM() {
        super();
    }

    public ApDungKM(String maApDung, ChiTietHoaDon chiTietHoaDon, ChiTietKhuyenMai chiTietKhuyenMai) {
        this.maApDung = maApDung;
        this.chiTietHoaDon = chiTietHoaDon;
        this.chiTietKhuyenMai = chiTietKhuyenMai;
    }

    public String getMaApDung() { return maApDung; }
    public void setMaApDung(String maApDung) { this.maApDung = maApDung; }
    public ChiTietHoaDon getChiTietHoaDon() { return chiTietHoaDon; }
    public void setChiTietHoaDon(ChiTietHoaDon chiTietHoaDon) { this.chiTietHoaDon = chiTietHoaDon; }
    public ChiTietKhuyenMai getChiTietKhuyenMai() { return chiTietKhuyenMai; }
    public void setChiTietKhuyenMai(ChiTietKhuyenMai chiTietKhuyenMai) { this.chiTietKhuyenMai = chiTietKhuyenMai; }

    @Override
    public String toString() {
        return "ApDungKM{" +
                "maApDung='" + maApDung + '\'' +
                ", chiTietHoaDon=" + (chiTietHoaDon != null ? chiTietHoaDon.getMaChiTietHD() : "null") +
                ", chiTietKhuyenMai=" + (chiTietKhuyenMai != null ? chiTietKhuyenMai.getMaChiTietKM() : "null") +
                '}';
    }
}
