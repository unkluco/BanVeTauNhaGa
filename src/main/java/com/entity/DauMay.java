package com.entity;

public class DauMay {
    private String maDauMay;
    private String tenDauMay;

    public DauMay() {
        super();
    }

    public DauMay(String maDauMay) {
        this.maDauMay = maDauMay;
    }

    public DauMay(String maDauMay, String tenDauMay) {
        this.maDauMay = maDauMay;
        this.tenDauMay = tenDauMay;
    }

    public String getMaDauMay() { return maDauMay; }
    public void setMaDauMay(String maDauMay) { this.maDauMay = maDauMay; }
    public String getTenDauMay() { return tenDauMay; }
    public void setTenDauMay(String tenDauMay) { this.tenDauMay = tenDauMay; }

    @Override
    public String toString() {
        return "DauMay{" +
                "maDauMay='" + maDauMay + '\'' +
                ", tenDauMay='" + tenDauMay + '\'' +
                '}';
    }
}
