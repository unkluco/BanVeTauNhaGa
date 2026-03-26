package com.entity;

import com.enums.TrangThaiVe;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Ve {
    private String maVe;
    private Lich lich;
    private Ghe ghe;
    private TrangThaiVe trangThai;
    private String lyDoHuy;
    private LocalDateTime ngayHuy;

    private List<ChiTietHoaDon> danhSachChiTietHoaDon = new ArrayList<>();

    public Ve() {
        super();
    }

    public Ve(String maVe) {
        this.maVe = maVe;
    }

    public Ve(String maVe, Lich lich, Ghe ghe, TrangThaiVe trangThai, String lyDoHuy, LocalDateTime ngayHuy) {
        this.maVe = maVe;
        this.lich = lich;
        this.ghe = ghe;
        this.trangThai = trangThai;
        this.lyDoHuy = lyDoHuy;
        this.ngayHuy = ngayHuy;
    }

    public String getMaVe() { return maVe; }
    public void setMaVe(String maVe) { this.maVe = maVe; }
    public Lich getLich() { return lich; }
    public void setLich(Lich lich) { this.lich = lich; }
    public Ghe getGhe() { return ghe; }
    public void setGhe(Ghe ghe) { this.ghe = ghe; }
    public TrangThaiVe getTrangThai() { return trangThai; }
    public void setTrangThai(TrangThaiVe trangThai) { this.trangThai = trangThai; }
    public String getLyDoHuy() { return lyDoHuy; }
    public void setLyDoHuy(String lyDoHuy) { this.lyDoHuy = lyDoHuy; }
    public LocalDateTime getNgayHuy() { return ngayHuy; }
    public void setNgayHuy(LocalDateTime ngayHuy) { this.ngayHuy = ngayHuy; }
    public List<ChiTietHoaDon> getDanhSachChiTietHoaDon() { return danhSachChiTietHoaDon; }
    public void setDanhSachChiTietHoaDon(List<ChiTietHoaDon> danhSachChiTietHoaDon) { this.danhSachChiTietHoaDon = danhSachChiTietHoaDon; }

    @Override
    public String toString() {
        return "Ve{" +
                "maVe='" + maVe + '\'' +
                ", lich=" + (lich != null ? lich.getMaLich() : "null") +
                ", ghe=" + (ghe != null ? ghe.getMaGhe() : "null") +
                ", trangThai=" + trangThai +
                ", lyDoHuy='" + lyDoHuy + '\'' +
                ", ngayHuy=" + ngayHuy +
                '}';
    }
}
