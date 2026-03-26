package com.entity;

import com.enums.TrangThaiNhanVien;
import com.enums.VaiTro;
import java.time.LocalDate;

public class NhanVien {
    private String maNV;
    private String hoTen;
    private String password;
    private VaiTro vaiTro;
    private String soDienThoai;
    private String cccd;
    private String diaChiTamTru;
    private TrangThaiNhanVien trangThai;

    // 6 new fields
    private String email;
    private String gaLamViec;        // FK to Ga.maGa e.g. "GA-001"
    private String diaChiThuongTru;
    private LocalDate ngaySinh;
    private String gioiTinh;         // "NAM" or "NU"
    private String quocTich;

    public NhanVien() {
        super();
    }

    public NhanVien(String maNV) {
        this.maNV = maNV;
    }

    /** Backward-compatible constructor (without cccd, diaChiTamTru) */
    public NhanVien(String maNV, String hoTen, String password, VaiTro vaiTro,
                    String soDienThoai, TrangThaiNhanVien trangThai) {
        this(maNV, hoTen, password, vaiTro, soDienThoai, null, null, trangThai);
    }

    /** Full 8-field constructor (original) */
    public NhanVien(String maNV, String hoTen, String password, VaiTro vaiTro,
                    String soDienThoai, String cccd, String diaChiTamTru,
                    TrangThaiNhanVien trangThai) {
        this.maNV = maNV;
        this.hoTen = hoTen;
        this.password = password;
        this.vaiTro = vaiTro;
        this.soDienThoai = soDienThoai;
        this.cccd = cccd;
        this.diaChiTamTru = diaChiTamTru;
        this.trangThai = trangThai;
    }

    // --- existing getters/setters ---
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

    public String getCccd() { return cccd; }
    public void setCccd(String cccd) { this.cccd = cccd; }

    public String getDiaChiTamTru() { return diaChiTamTru; }
    public void setDiaChiTamTru(String diaChiTamTru) { this.diaChiTamTru = diaChiTamTru; }

    public TrangThaiNhanVien getTrangThai() { return trangThai; }
    public void setTrangThai(TrangThaiNhanVien trangThai) { this.trangThai = trangThai; }

    // --- new getters/setters ---
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getGaLamViec() { return gaLamViec; }
    public void setGaLamViec(String gaLamViec) { this.gaLamViec = gaLamViec; }

    public String getDiaChiThuongTru() { return diaChiThuongTru; }
    public void setDiaChiThuongTru(String diaChiThuongTru) { this.diaChiThuongTru = diaChiThuongTru; }

    public LocalDate getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(LocalDate ngaySinh) { this.ngaySinh = ngaySinh; }

    public String getGioiTinh() { return gioiTinh; }
    public void setGioiTinh(String gioiTinh) { this.gioiTinh = gioiTinh; }

    public String getQuocTich() { return quocTich; }
    public void setQuocTich(String quocTich) { this.quocTich = quocTich; }

    @Override
    public String toString() {
        return "NhanVien{" +
                "maNV='" + maNV + '\'' +
                ", hoTen='" + hoTen + '\'' +
                ", vaiTro=" + vaiTro +
                ", soDienThoai='" + soDienThoai + '\'' +
                ", cccd='" + cccd + '\'' +
                ", trangThai=" + trangThai +
                ", email='" + email + '\'' +
                ", gaLamViec='" + gaLamViec + '\'' +
                ", ngaySinh=" + ngaySinh +
                ", gioiTinh='" + gioiTinh + '\'' +
                '}';
    }
}
