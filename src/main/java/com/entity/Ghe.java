package com.entity;

public class Ghe {
    private String maGhe;
    private ToaTau toaTau;
    private int soGhe;

    public Ghe() {
        super();
    }

    public Ghe(String maGhe) {
        this.maGhe = maGhe;
    }

    public Ghe(String maGhe, ToaTau toaTau, int soGhe) {
        this.maGhe = maGhe;
        this.toaTau = toaTau;
        this.soGhe = soGhe;
    }

    public String getMaGhe() { return maGhe; }
    public void setMaGhe(String maGhe) { this.maGhe = maGhe; }
    public ToaTau getToaTau() { return toaTau; }
    public void setToaTau(ToaTau toaTau) { this.toaTau = toaTau; }
    public int getSoGhe() { return soGhe; }
    public void setSoGhe(int soGhe) { this.soGhe = soGhe; }

    @Override
    public String toString() {
        return "Ghe{" +
                "maGhe='" + maGhe + '\'' +
                ", toaTau=" + toaTau +
                ", soGhe=" + soGhe +
                '}';
    }
}
