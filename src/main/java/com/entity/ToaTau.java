package com.entity;

import com.enums.LoaiGhe;

public class ToaTau {
    private String maToaTau;
    private LoaiGhe loaiGhe;
    private String trangThai;

    public ToaTau() {
        super();
    }

    public ToaTau(String maToaTau) {
        this.maToaTau = maToaTau;
    }

    public ToaTau(String maToaTau, LoaiGhe loaiGhe) {
        this(maToaTau, loaiGhe, "Đang hoạt động");
    }

    public ToaTau(String maToaTau, LoaiGhe loaiGhe, String trangThai) {
        this.maToaTau = maToaTau;
        this.loaiGhe = loaiGhe;
        this.trangThai = (trangThai == null || trangThai.isBlank()) ? "Đang hoạt động" : trangThai;
    }

    public String getMaToaTau() { return maToaTau; }
    public void setMaToaTau(String maToaTau) { this.maToaTau = maToaTau; }
    public LoaiGhe getLoaiGhe() { return loaiGhe; }
    public void setLoaiGhe(LoaiGhe loaiGhe) { this.loaiGhe = loaiGhe; }
    public String getTrangThai() { return (trangThai == null || trangThai.isBlank()) ? "Đang hoạt động" : trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    @Override
    public String toString() {
        return "ToaTau{" +
                "maToaTau='" + maToaTau + '\'' +
                ", loaiGhe=" + loaiGhe +
                ", trangThai='" + trangThai + '\'' +
                '}';
    }
}
