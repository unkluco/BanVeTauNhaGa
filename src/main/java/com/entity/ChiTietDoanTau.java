package com.entity;

public class ChiTietDoanTau {
    private String maChiTietDT;
    private DoanTau doanTau;
    private ToaTau toaTau;
    private int soThuTu;

    public ChiTietDoanTau() {
        super();
    }

    public ChiTietDoanTau(String maChiTietDT, DoanTau doanTau, ToaTau toaTau, int soThuTu) {
        this.maChiTietDT = maChiTietDT;
        this.doanTau = doanTau;
        this.toaTau = toaTau;
        this.soThuTu = soThuTu;
    }

    public String getMaChiTietDT() { return maChiTietDT; }
    public void setMaChiTietDT(String maChiTietDT) { this.maChiTietDT = maChiTietDT; }
    public DoanTau getDoanTau() { return doanTau; }
    public void setDoanTau(DoanTau doanTau) { this.doanTau = doanTau; }
    public ToaTau getToaTau() { return toaTau; }
    public void setToaTau(ToaTau toaTau) { this.toaTau = toaTau; }
    public int getSoThuTu() { return soThuTu; }
    public void setSoThuTu(int soThuTu) { this.soThuTu = soThuTu; }

    @Override
    public String toString() {
        return "ChiTietDoanTau{" +
                "maChiTietDT='" + maChiTietDT + '\'' +
                ", doanTau=" + doanTau +
                ", toaTau=" + toaTau +
                ", soThuTu=" + soThuTu +
                '}';
    }
}
