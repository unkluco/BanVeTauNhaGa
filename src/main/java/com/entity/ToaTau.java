package com.entity;

import com.enums.LoaiGhe;

public class ToaTau {
    private String maToaTau;
    private LoaiGhe loaiGhe;

    public ToaTau() {
        super();
    }

    public ToaTau(String maToaTau) {
        this.maToaTau = maToaTau;
    }

    public ToaTau(String maToaTau, LoaiGhe loaiGhe) {
        this.maToaTau = maToaTau;
        this.loaiGhe = loaiGhe;
    }

    public String getMaToaTau() { return maToaTau; }
    public void setMaToaTau(String maToaTau) { this.maToaTau = maToaTau; }
    public LoaiGhe getLoaiGhe() { return loaiGhe; }
    public void setLoaiGhe(LoaiGhe loaiGhe) { this.loaiGhe = loaiGhe; }

    @Override
    public String toString() {
        return "ToaTau{" +
                "maToaTau='" + maToaTau + '\'' +
                ", loaiGhe=" + loaiGhe +
                '}';
    }
}
