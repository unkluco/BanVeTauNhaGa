package com.entity;

public class Ga {
    private String maGa;
    private String tenGa;
    private String diaChi;

    public Ga() {
        super();
    }

    public Ga(String maGa) {
        this.maGa = maGa;
    }

    public Ga(String maGa, String tenGa, String diaChi) {
        this.maGa = maGa;
        this.tenGa = tenGa;
        this.diaChi = diaChi;
    }

    public String getMaGa() { return maGa; }
    public void setMaGa(String maGa) { this.maGa = maGa; }
    public String getTenGa() { return tenGa; }
    public void setTenGa(String tenGa) { this.tenGa = tenGa; }
    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    @Override
    public String toString() {
        return "Ga{" +
                "maGa='" + maGa + '\'' +
                ", tenGa='" + tenGa + '\'' +
                ", diaChi='" + diaChi + '\'' +
                '}';
    }
}
