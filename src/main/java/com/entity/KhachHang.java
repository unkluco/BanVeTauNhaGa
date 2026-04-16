package com.entity;

import java.time.LocalDate;
import java.util.Objects;

public class KhachHang {
    private String maKhachHang;
    private String hoTen;
    private String cccd;
    private String soDienThoai;
    // 6 new fields — giống NhanVien, trừ nghiệp vụ bán hàng
    private String email;
    private String diaChiThuongTru;
    private String diaChiTamTru;
    private LocalDate ngaySinh;
    private String gioiTinh;     // "NAM" or "NU"
    private String quocTich;

    public KhachHang() {
        super();
    }

    public KhachHang(String maKhachHang) {
        this.maKhachHang = maKhachHang;
    }

    // Backward-compatible 4-field constructor
    public KhachHang(String maKhachHang, String hoTen, String cccd, String soDienThoai) {
        this.maKhachHang = maKhachHang;
        setHoTen(hoTen);
        setCccd(cccd);
        setSoDienThoai(soDienThoai);
    }

    // Full constructor
    public KhachHang(String maKhachHang, String hoTen, String cccd, String soDienThoai,
                     String email, String diaChiThuongTru, String diaChiTamTru,
                     LocalDate ngaySinh, String gioiTinh, String quocTich) {
        this.maKhachHang = maKhachHang;
        setHoTen(hoTen);
        setCccd(cccd);
        setSoDienThoai(soDienThoai);
        this.email = email;
        this.diaChiThuongTru = diaChiThuongTru;
        this.diaChiTamTru = diaChiTamTru;
        this.ngaySinh = ngaySinh;
        this.gioiTinh = gioiTinh;
        this.quocTich = quocTich != null ? quocTich : "Việt Nam";
    }

    // --- Getters / Setters ---

    public String getMaKhachHang() { return maKhachHang; }
    public void setMaKhachHang(String maKhachHang) { this.maKhachHang = maKhachHang; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) {
        if (hoTen == null || hoTen.trim().isEmpty())
            throw new IllegalArgumentException("Ho ten khong duoc de trong");
        if (hoTen.length() > 100)
            throw new IllegalArgumentException("Ho ten toi da 100 ky tu");
        this.hoTen = hoTen;
    }

    public String getCccd() { return cccd; }
    public void setCccd(String cccd) {
        if (cccd == null || cccd.trim().isEmpty())
            throw new IllegalArgumentException("CCCD khong duoc de trong");
        this.cccd = cccd;
    }

    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) {
        if (soDienThoai == null || soDienThoai.trim().isEmpty())
            throw new IllegalArgumentException("So dien thoai khong duoc de trong");
        this.soDienThoai = soDienThoai;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDiaChiThuongTru() { return diaChiThuongTru; }
    public void setDiaChiThuongTru(String diaChiThuongTru) { this.diaChiThuongTru = diaChiThuongTru; }

    public String getDiaChiTamTru() { return diaChiTamTru; }
    public void setDiaChiTamTru(String diaChiTamTru) { this.diaChiTamTru = diaChiTamTru; }

    public LocalDate getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(LocalDate ngaySinh) { this.ngaySinh = ngaySinh; }

    public String getGioiTinh() { return gioiTinh; }
    public void setGioiTinh(String gioiTinh) { this.gioiTinh = gioiTinh; }

    public String getQuocTich() { return quocTich != null ? quocTich : "Việt Nam"; }
    public void setQuocTich(String quocTich) { this.quocTich = quocTich; }

    // --- equals / hashCode / toString ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KhachHang that = (KhachHang) o;
        return Objects.equals(maKhachHang, that.maKhachHang);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maKhachHang);
    }

    @Override
    public String toString() {
        return "KhachHang{" +
                "maKhachHang='" + maKhachHang + '\'' +
                ", hoTen='" + hoTen + '\'' +
                ", cccd='" + cccd + '\'' +
                ", soDienThoai='" + soDienThoai + '\'' +
                ", email='" + email + '\'' +
                ", diaChiThuongTru='" + diaChiThuongTru + '\'' +
                ", ngaySinh=" + ngaySinh +
                ", gioiTinh='" + gioiTinh + '\'' +
                ", quocTich='" + quocTich + '\'' +
                '}';
    }
}