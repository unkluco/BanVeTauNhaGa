package com.entity;

public class DauMay {
    private String maDauMay;
    private String tenDauMay;
    private String hangSanXuat;
    private Integer namSanXuat;
    private Integer congSuatKw;
    private String trangThai;
    private String moTa;

    public DauMay() {
        super();
    }

    public DauMay(String maDauMay) {
        this.maDauMay = maDauMay;
    }

    public DauMay(String maDauMay, String tenDauMay) {
        this(maDauMay, tenDauMay, null, null, null, "Đang hoạt động", null);
    }

    public DauMay(String maDauMay, String tenDauMay, String hangSanXuat,
                  Integer namSanXuat, Integer congSuatKw,
                  String trangThai, String moTa) {
        this.maDauMay = maDauMay;
        this.tenDauMay = tenDauMay;
        this.hangSanXuat = hangSanXuat;
        this.namSanXuat = namSanXuat;
        this.congSuatKw = congSuatKw;
        this.trangThai = (trangThai == null || trangThai.isBlank()) ? "Đang hoạt động" : trangThai;
        this.moTa = moTa;
    }

    public String getMaDauMay() { return maDauMay; }
    public void setMaDauMay(String maDauMay) { this.maDauMay = maDauMay; }
    public String getTenDauMay() { return tenDauMay; }
    public void setTenDauMay(String tenDauMay) { this.tenDauMay = tenDauMay; }
    public String getHangSanXuat() { return hangSanXuat; }
    public void setHangSanXuat(String hangSanXuat) { this.hangSanXuat = hangSanXuat; }
    public Integer getNamSanXuat() { return namSanXuat; }
    public void setNamSanXuat(Integer namSanXuat) { this.namSanXuat = namSanXuat; }
    public Integer getCongSuatKw() { return congSuatKw; }
    public void setCongSuatKw(Integer congSuatKw) { this.congSuatKw = congSuatKw; }
    public String getTrangThai() { return (trangThai == null || trangThai.isBlank()) ? "Đang hoạt động" : trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    @Override
    public String toString() {
        return "DauMay{" +
                "maDauMay='" + maDauMay + '\'' +
                ", tenDauMay='" + tenDauMay + '\'' +
                ", hangSanXuat='" + hangSanXuat + '\'' +
                ", namSanXuat=" + namSanXuat +
                ", congSuatKw=" + congSuatKw +
                ", trangThai='" + trangThai + '\'' +
                ", moTa='" + moTa + '\'' +
                '}';
    }
}
