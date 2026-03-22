package com.entity;

import com.enums.TrangThaiVe;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Ve {
    private String maVe;
    private HoaDon hoaDon;
    private String tenHanhKhach;
    private String cccd;
    private double giaTien;
    private TrangThaiVe trangThai;
    private String lyDoHuy;
    private LocalDateTime ngayHuy;

    private List<ChiTietVe> danhSachChiTietVe = new ArrayList<>();
    private List<ApDungKM> danhSachApDungKM = new ArrayList<>();

    public Ve() {
        super();
    }

    public Ve(String maVe) {
        this.maVe = maVe;
    }

    public Ve(String maVe, HoaDon hoaDon, String tenHanhKhach, String cccd, double giaTien,
              TrangThaiVe trangThai, String lyDoHuy, LocalDateTime ngayHuy) {
        this.maVe = maVe;
        this.hoaDon = hoaDon;
        this.tenHanhKhach = tenHanhKhach;
        this.cccd = cccd;
        this.giaTien = giaTien;
        this.trangThai = trangThai;
        this.lyDoHuy = lyDoHuy;
        this.ngayHuy = ngayHuy;
    }

    public String getMaVe() { return maVe; }
    public void setMaVe(String maVe) { this.maVe = maVe; }
    public HoaDon getHoaDon() { return hoaDon; }
    public void setHoaDon(HoaDon hoaDon) { this.hoaDon = hoaDon; }
    public String getTenHanhKhach() { return tenHanhKhach; }
    public void setTenHanhKhach(String tenHanhKhach) { this.tenHanhKhach = tenHanhKhach; }
    public String getCccd() { return cccd; }
    public void setCccd(String cccd) { this.cccd = cccd; }
    public double getGiaTien() { return giaTien; }
    public void setGiaTien(double giaTien) { this.giaTien = giaTien; }
    public TrangThaiVe getTrangThai() { return trangThai; }
    public void setTrangThai(TrangThaiVe trangThai) { this.trangThai = trangThai; }
    public String getLyDoHuy() { return lyDoHuy; }
    public void setLyDoHuy(String lyDoHuy) { this.lyDoHuy = lyDoHuy; }
    public LocalDateTime getNgayHuy() { return ngayHuy; }
    public void setNgayHuy(LocalDateTime ngayHuy) { this.ngayHuy = ngayHuy; }
    public List<ChiTietVe> getDanhSachChiTietVe() { return danhSachChiTietVe; }
    public void setDanhSachChiTietVe(List<ChiTietVe> danhSachChiTietVe) { this.danhSachChiTietVe = danhSachChiTietVe; }
    public List<ApDungKM> getDanhSachApDungKM() { return danhSachApDungKM; }
    public void setDanhSachApDungKM(List<ApDungKM> danhSachApDungKM) { this.danhSachApDungKM = danhSachApDungKM; }

    @Override
    public String toString() {
        return "Ve{" +
                "maVe='" + maVe + '\'' +
                ", tenHanhKhach='" + tenHanhKhach + '\'' +
                ", cccd='" + cccd + '\'' +
                ", giaTien=" + giaTien +
                ", trangThai=" + trangThai +
                '}';
    }
}
