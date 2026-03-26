package com.entity;

public class DoanTau {
    private String maDoanTau;
    private String tenDoanTau;
    private DauMay dauMay;

    public DoanTau() {
        super();
    }

    public DoanTau(String maDoanTau) {
        this.maDoanTau = maDoanTau;
    }

    public DoanTau(String maDoanTau, String tenDoanTau, DauMay dauMay) {
        this.maDoanTau = maDoanTau;
        this.tenDoanTau = tenDoanTau;
        this.dauMay = dauMay;
    }

    public String getMaDoanTau() { return maDoanTau; }
    public void setMaDoanTau(String maDoanTau) { this.maDoanTau = maDoanTau; }
    public String getTenDoanTau() { return tenDoanTau; }
    public void setTenDoanTau(String tenDoanTau) { this.tenDoanTau = tenDoanTau; }
    public DauMay getDauMay() { return dauMay; }
    public void setDauMay(DauMay dauMay) { this.dauMay = dauMay; }

    @Override
    public String toString() {
        return "DoanTau{" +
                "maDoanTau='" + maDoanTau + '\'' +
                ", tenDoanTau='" + tenDoanTau + '\'' +
                ", dauMay=" + dauMay +
                '}';
    }
}
