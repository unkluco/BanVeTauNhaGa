package com.entity;

public class Tuyen {
    private String maTuyen;
    private Ga gaDi;
    private Ga gaDen;
    private int km;
    private boolean hoatDong = true;

    public Tuyen() {
        super();
    }

    public Tuyen(String maTuyen) {
        this.maTuyen = maTuyen;
    }

    public Tuyen(String maTuyen, Ga gaDi, Ga gaDen) {
        this.maTuyen = maTuyen;
        this.gaDi = gaDi;
        this.gaDen = gaDen;
    }

    public Tuyen(String maTuyen, Ga gaDi, Ga gaDen, int km) {
        this.maTuyen = maTuyen;
        this.gaDi = gaDi;
        this.gaDen = gaDen;
        this.km = km;
    }

    public String getMaTuyen() { return maTuyen; }
    public void setMaTuyen(String maTuyen) { this.maTuyen = maTuyen; }
    public Ga getGaDi() { return gaDi; }
    public void setGaDi(Ga gaDi) { this.gaDi = gaDi; }
    public Ga getGaDen() { return gaDen; }
    public void setGaDen(Ga gaDen) { this.gaDen = gaDen; }
    public int getKm() { return km; }
    public void setKm(int km) { this.km = km; }
    public boolean isHoatDong() { return hoatDong; }
    public void setHoatDong(boolean hoatDong) { this.hoatDong = hoatDong; }

    @Override
    public String toString() {
        return "Tuyen{" +
                "maTuyen='" + maTuyen + '\'' +
                ", gaDi=" + gaDi +
                ", gaDen=" + gaDen +
                ", km=" + km +
                '}';
    }
}
