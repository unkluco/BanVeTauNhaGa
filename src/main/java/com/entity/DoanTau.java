package com.entity;

public class DoanTau {
    private String maDoanTau;
    private String tenDoanTau;
    private DauMay dauMay;
    private String trangThai;

    public DoanTau() {
        super();
    }

    public DoanTau(String maDoanTau) {
        this.maDoanTau = maDoanTau;
    }

    public DoanTau(String maDoanTau, String tenDoanTau, DauMay dauMay) {
        this(maDoanTau, tenDoanTau, dauMay, "Đang hoạt động");
    }

    public DoanTau(String maDoanTau, String tenDoanTau, DauMay dauMay, String trangThai) {
        this.maDoanTau = maDoanTau;
        this.tenDoanTau = tenDoanTau;
        this.dauMay = dauMay;
        this.trangThai = (trangThai == null || trangThai.isBlank()) ? "Đang hoạt động" : trangThai;
    }

    public String getMaDoanTau() { return maDoanTau; }
    public void setMaDoanTau(String maDoanTau) { this.maDoanTau = maDoanTau; }
    public String getTenDoanTau() { return tenDoanTau; }
    public void setTenDoanTau(String tenDoanTau) { this.tenDoanTau = tenDoanTau; }
    public DauMay getDauMay() { return dauMay; }
    public void setDauMay(DauMay dauMay) { this.dauMay = dauMay; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    @Override
    public String toString() {
        return "DoanTau{" +
                "maDoanTau='" + maDoanTau + '\'' +
                ", tenDoanTau='" + tenDoanTau + '\'' +
                ", dauMay=" + dauMay +
                ", trangThai='" + trangThai + '\'' +
                '}';
    }
}
