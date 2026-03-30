package com.entity;

import com.enums.LoaiGhe;

public class ChiTietKhuyenMai {
    private String maChiTietKM;
    private String tenChiTiet;
    private KhuyenMai khuyenMai;
    private Tuyen tuyen;       // null = áp dụng tất cả tuyến
    private LoaiGhe loaiGhe;   // null = áp dụng tất cả loại ghế
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
        this.khuyenMai   = khuyenMai;
        this.tuyen       = tuyen;
        this.loaiGhe     = loaiGhe;
        this.phanTramGiam = phanTramGiam;
    }

    public ChiTietKhuyenMai(String maChiTietKM, String tenChiTiet, KhuyenMai khuyenMai,
                              Tuyen tuyen, LoaiGhe loaiGhe, double phanTramGiam) {
        this.maChiTietKM  = maChiTietKM;
        this.tenChiTiet   = tenChiTiet;
        this.khuyenMai    = khuyenMai;
        this.tuyen        = tuyen;
        this.loaiGhe      = loaiGhe;
        this.phanTramGiam = phanTramGiam;
    }

    public String getMaChiTietKM() { return maChiTietKM; }
    public void setMaChiTietKM(String maChiTietKM) { this.maChiTietKM = maChiTietKM; }
    public String getTenChiTiet() { return tenChiTiet; }
    public void setTenChiTiet(String tenChiTiet) { this.tenChiTiet = tenChiTiet; }
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
