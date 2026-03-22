package com.entity;

import com.enums.VaiTro;

public class NhanVien {
    private String maNV;
    private String hoTen;
    private String password;
    private VaiTro vaiTro;
    private String soDienThoai;

    public NhanVien() {
        super();
    }

    public NhanVien(String maNV) {
        this.maNV = maNV;
    }

    public NhanVien(String maNV, String hoTen, String password, VaiTro vaiTro, String soDienThoai) {
        this.maNV = maNV;
        this.hoTen = hoTen;
        this.password = password;
        this.vaiTro = vaiTro;
        this.soDienThoai = soDienThoai;
    }

    public String getMaNV() { return maNV; }
    public void setMaNV(String maNV) { this.maNV = maNV; }
    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public VaiTro getVaiTro() { return vaiTro; }
    public void setVaiTro(VaiTro vaiTro) { this.vaiTro = vaiTro; }
    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }

    @Override
    public String toString() {
        return "NhanVien{" +
                "maNV='" + maNV + '\'' +
                ", hoTen='" + hoTen + '\'' +
                ", vaiTro=" + vaiTro +
                ", soDienThoai='" + soDienThoai + '\'' +
                '}';
    }
}
