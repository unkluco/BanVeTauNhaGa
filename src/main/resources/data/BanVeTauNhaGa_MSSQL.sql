-- ============================================================
-- CO SO DU LIEU: UNG DUNG BAN VE TAU TAI NHA GA
-- Tuong thich: SQL Server (MSSQL)
-- Phien ban: v2 - Da sua theo tai lieu thiet ke OOAD
-- Reset DB: chay lai file nay se xoa sach va tao lai tu dau
-- ============================================================

USE master;
GO

IF EXISTS (SELECT name FROM sys.databases WHERE name = 'BanVeTauNhaGa')
BEGIN
    ALTER DATABASE BanVeTauNhaGa SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE BanVeTauNhaGa;
END
GO

CREATE DATABASE BanVeTauNhaGa;
GO

USE BanVeTauNhaGa;
GO

-- ============================================================
-- TAO BANG (thu tu theo dependency)
-- ============================================================

-- 1. NhanVien
CREATE TABLE NhanVien (
    maNV VARCHAR(20) PRIMARY KEY,
    hoTen NVARCHAR(100) NOT NULL,
    [password] VARCHAR(255) NOT NULL,
    vaiTro VARCHAR(20) NOT NULL CHECK (vaiTro IN ('BAN_VE', 'DIEU_PHOI', 'ADMIN')),
    soDienThoai VARCHAR(15) NOT NULL,
    cccd VARCHAR(20) NULL,
    diaChiTamTru NVARCHAR(255) NULL,
    trangThai VARCHAR(20) NOT NULL DEFAULT 'DANG_LAM' CHECK (trangThai IN ('DANG_LAM', 'NGHI_PHEP', 'DA_NGHI')),
    email VARCHAR(100) NULL,
    gaLamViec VARCHAR(20) NULL,
    diaChiThuongTru NVARCHAR(255) NULL,
    ngaySinh DATE NULL,
    gioiTinh VARCHAR(5) NULL CHECK (gioiTinh IN ('NAM', 'NU')),
    quocTich NVARCHAR(50) NULL DEFAULT N'Việt Nam'
);

-- 2. KhachHang (DA SUA: cau truc giong NhanVien, tru cac thuoc tinh lien quan nghiep vu ban hang)
CREATE TABLE KhachHang (
    maKhachHang VARCHAR(20) PRIMARY KEY,
    hoTen NVARCHAR(100) NOT NULL,
    cccd VARCHAR(20) NOT NULL,
    soDienThoai VARCHAR(15) NOT NULL,
    email VARCHAR(100) NULL,
    diaChiThuongTru NVARCHAR(255) NULL,
    diaChiTamTru NVARCHAR(255) NULL,
    ngaySinh DATE NULL,
    gioiTinh VARCHAR(5) NULL CHECK (gioiTinh IN ('NAM', 'NU')),
    quocTich NVARCHAR(50) NULL DEFAULT N'Việt Nam'
);

-- 3. Ga
CREATE TABLE Ga (
    maGa VARCHAR(20) PRIMARY KEY,
    tenGa NVARCHAR(100) NOT NULL,
    diaChi NVARCHAR(255) NOT NULL
);	

-- 4. DauMay
CREATE TABLE DauMay (
    maDauMay VARCHAR(20) PRIMARY KEY,
    tenDauMay NVARCHAR(100) NOT NULL,
    hangSanXuat NVARCHAR(100) NULL,
    namSanXuat INT NULL CHECK (namSanXuat BETWEEN 1950 AND 2100),
    congSuatKw INT NULL CHECK (congSuatKw > 0),
    trangThai NVARCHAR(30) NOT NULL DEFAULT N'Đang hoạt động'
        CHECK (trangThai IN (N'Đang hoạt động', N'Bảo trì', N'Ngừng khai thác')),
    moTa NVARCHAR(255) NULL
);

-- 5. ToaTau
CREATE TABLE ToaTau (
    maToaTau VARCHAR(20) PRIMARY KEY,
    loaiGhe VARCHAR(20) NOT NULL CHECK (loaiGhe IN ('GHE_CUNG', 'GHE_MEM', 'GIUONG_NAM')),
    trangThai NVARCHAR(30) NOT NULL DEFAULT N'Đang hoạt động'
        CHECK (trangThai IN (N'Đang hoạt động', N'Bảo trì', N'Ngừng khai thác'))
);

-- 6. Gia
CREATE TABLE Gia (
    maGia VARCHAR(20) PRIMARY KEY,
    thoiGianBatDau DATE NOT NULL,
    thoiGianKetThuc DATE NOT NULL,
    moTa NVARCHAR(255),
    trangThai BIT NOT NULL DEFAULT 0,
    CHECK (thoiGianKetThuc > thoiGianBatDau)
);

-- 7. KhuyenMai (cau truc giong Gia: ky khuyen mai voi thoi gian, mo ta, trang thai)
CREATE TABLE KhuyenMai (
    maKhuyenMai VARCHAR(20) PRIMARY KEY,
    tenKhuyenMai NVARCHAR(100) NOT NULL,
    thoiGianBatDau DATETIME NOT NULL,
    thoiGianKetThuc DATETIME NOT NULL,
    moTa NVARCHAR(255),
    trangThai BIT NOT NULL DEFAULT 0,
    CHECK (thoiGianKetThuc > thoiGianBatDau)
);

-- 8. Tuyen (FK -> Ga x2)
CREATE TABLE Tuyen (
    maTuyen VARCHAR(20) PRIMARY KEY,
    gaDi VARCHAR(20) NOT NULL,
    gaDen VARCHAR(20) NOT NULL,
    km INT NOT NULL DEFAULT 0,
    hoatDong BIT NOT NULL DEFAULT 1,
    FOREIGN KEY (gaDi) REFERENCES Ga(maGa),
    FOREIGN KEY (gaDen) REFERENCES Ga(maGa),
    CHECK (gaDi != gaDen)
);

-- 9. DoanTau (FK -> DauMay)
CREATE TABLE DoanTau (
    maDoanTau VARCHAR(20) PRIMARY KEY,
    tenDoanTau NVARCHAR(100) NOT NULL,
    maDauMay VARCHAR(20) NOT NULL,
    FOREIGN KEY (maDauMay) REFERENCES DauMay(maDauMay)
);

-- 10. ChiTietDoanTau (FK -> DoanTau, ToaTau)
CREATE TABLE ChiTietDoanTau (
    maChiTietDT VARCHAR(20) PRIMARY KEY,
    maDoanTau VARCHAR(20) NOT NULL,
    maToaTau VARCHAR(20) NOT NULL,
    soThuTu INT NOT NULL CHECK (soThuTu > 0),
    FOREIGN KEY (maDoanTau) REFERENCES DoanTau(maDoanTau),
    FOREIGN KEY (maToaTau) REFERENCES ToaTau(maToaTau)
);

-- 11. Ghe (FK -> ToaTau)
CREATE TABLE Ghe (
    maGhe VARCHAR(20) PRIMARY KEY,
    maToaTau VARCHAR(20) NOT NULL,
    soGhe INT NOT NULL CHECK (soGhe > 0),
    FOREIGN KEY (maToaTau) REFERENCES ToaTau(maToaTau)
);

-- 12. Lich (FK -> Tuyen, DoanTau) - da doi thoiGianChay: NVARCHAR -> INT (phut)
CREATE TABLE Lich (
    maLich VARCHAR(20) PRIMARY KEY,
    maTuyen VARCHAR(20) NOT NULL,
    maDoanTau VARCHAR(20) NOT NULL,
    thoiGianBatDau DATETIME NOT NULL,
    thoiGianChay INT NOT NULL,
    hoatDong BIT NOT NULL DEFAULT 1,
    FOREIGN KEY (maTuyen) REFERENCES Tuyen(maTuyen),
    FOREIGN KEY (maDoanTau) REFERENCES DoanTau(maDoanTau)
);

-- 13. ChiTietGia (FK -> Gia, Tuyen) - da doi giaNiemYet: FLOAT -> DECIMAL
CREATE TABLE ChiTietGia (
    maChiTietGia VARCHAR(20) PRIMARY KEY,
    maGia VARCHAR(20) NOT NULL,
    maTuyen VARCHAR(20) NOT NULL,
    loaiGhe VARCHAR(20) NOT NULL CHECK (loaiGhe IN ('GHE_CUNG', 'GHE_MEM', 'GIUONG_NAM')),
    giaNiemYet DECIMAL(18,2) NOT NULL CHECK (giaNiemYet > 0),
    FOREIGN KEY (maGia) REFERENCES Gia(maGia),
    FOREIGN KEY (maTuyen) REFERENCES Tuyen(maTuyen)
);

-- 13b. ChiTietKhuyenMai (FK -> KhuyenMai, Tuyen) - cau truc giong ChiTietGia
--   maTuyen  NULL = ap dung tat ca tuyen
--   loaiGhe  NULL = ap dung tat ca loai ghe
CREATE TABLE ChiTietKhuyenMai (
    maChiTietKM VARCHAR(20) PRIMARY KEY,
    maKhuyenMai VARCHAR(20) NOT NULL,
    maTuyen     VARCHAR(20) NULL,
    loaiGhe     VARCHAR(20) NULL CHECK (loaiGhe IS NULL OR loaiGhe IN ('GHE_CUNG', 'GHE_MEM', 'GIUONG_NAM')),
    tenChiTiet  NVARCHAR(200) NULL,
    phanTramGiam DECIMAL(5,2) NOT NULL CHECK (phanTramGiam > 0 AND phanTramGiam <= 1),
    FOREIGN KEY (maKhuyenMai) REFERENCES KhuyenMai(maKhuyenMai),
    FOREIGN KEY (maTuyen)     REFERENCES Tuyen(maTuyen)
);

-- 14. Ve (THAY DOI LON: xoa maHoaDon/tenHanhKhach/cccd/giaTien, them maLich+maGhe)
--     Ve bay gio dai dien cho 1 ghe tren 1 lich cu the
CREATE TABLE Ve (
    maVe VARCHAR(20) PRIMARY KEY,
    maLich VARCHAR(20) NOT NULL,
    maGhe VARCHAR(20) NOT NULL,
    trangThai VARCHAR(10) NOT NULL CHECK (trangThai IN ('DA_BAN', 'DA_HUY')),
    lyDoHuy NVARCHAR(255),
    ngayHuy DATETIME,
    FOREIGN KEY (maLich) REFERENCES Lich(maLich),
    FOREIGN KEY (maGhe) REFERENCES Ghe(maGhe)
);

-- 15. HoaDon (FK -> NhanVien) - KhachHang tach ra bang junction HoaDonKhachHang
CREATE TABLE HoaDon (
    maHoaDon VARCHAR(30) PRIMARY KEY,
    maNV     VARCHAR(20) NOT NULL,
    ngayLap  DATETIME    NOT NULL,
    FOREIGN KEY (maNV) REFERENCES NhanVien(maNV)
);

-- 15b. HoaDonKhachHang (junction 1-N: mot HoaDon co the co nhieu KhachHang)
CREATE TABLE HoaDonKhachHang (
    maHDKH      VARCHAR(20) PRIMARY KEY,
    maHoaDon    VARCHAR(30) NOT NULL,
    maKhachHang VARCHAR(20) NOT NULL,
    CONSTRAINT UQ_HDKH UNIQUE (maHoaDon, maKhachHang),
    FOREIGN KEY (maHoaDon)    REFERENCES HoaDon(maHoaDon),
    FOREIGN KEY (maKhachHang) REFERENCES KhachHang(maKhachHang)
);

-- 16. ChiTietHoaDon (BANG MOI - thay the ChiTietVe, lien ket HoaDon voi Ve)
--     Quan he 1-1 voi Ve (UNIQUE constraint tren maVe)
CREATE TABLE ChiTietHoaDon (
    maChiTietHD VARCHAR(20) PRIMARY KEY,
    maHoaDon VARCHAR(30) NOT NULL,
    maVe VARCHAR(20) NOT NULL UNIQUE,
    giaTien DECIMAL(18,2) NOT NULL CHECK (giaTien > 0),
    FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHoaDon),
    FOREIGN KEY (maVe) REFERENCES Ve(maVe)
);

-- 17. ApDungKM (DA SUA: FK -> ChiTietHoaDon + ChiTietKhuyenMai)
CREATE TABLE ApDungKM (
    maApDung VARCHAR(20) PRIMARY KEY,
    maChiTietHD VARCHAR(20) NOT NULL,
    maChiTietKM VARCHAR(20) NOT NULL,
    FOREIGN KEY (maChiTietHD) REFERENCES ChiTietHoaDon(maChiTietHD),
    FOREIGN KEY (maChiTietKM) REFERENCES ChiTietKhuyenMai(maChiTietKM)
);

-- 18. GiuCho (FK -> NhanVien, Lich, Ghe) - khong doi
CREATE TABLE GiuCho (
    maGiuCho VARCHAR(20) PRIMARY KEY,
    maNV VARCHAR(20) NOT NULL,
    maLich VARCHAR(20) NOT NULL,
    maGhe VARCHAR(20) NOT NULL,
    thoiGianHetHan DATETIME NOT NULL,
    FOREIGN KEY (maNV) REFERENCES NhanVien(maNV),
    FOREIGN KEY (maLich) REFERENCES Lich(maLich),
    FOREIGN KEY (maGhe) REFERENCES Ghe(maGhe)
);

-- ============================================================
-- DU LIEU MAU
-- ============================================================

-- ==================== 1. NhanVien ====================
INSERT INTO NhanVien (maNV, hoTen, [password], vaiTro, soDienThoai, cccd, diaChiTamTru, trangThai, email, gaLamViec, diaChiThuongTru, ngaySinh, gioiTinh, quocTich) VALUES
('ad', N'Quản trị viên', 'ad', 'ADMIN', '0900000000', '001080000001', N'120 Lê Duẩn, Hoàn Kiếm, Hà Nội', 'DANG_LAM', 'admin@azurerail.vn', 'GA-001', N'120 Lê Duẩn, Hoàn Kiếm, Hà Nội', '1980-01-01', 'NAM', N'Việt Nam');

INSERT INTO NhanVien (maNV, hoTen, [password], vaiTro, soDienThoai, cccd, diaChiTamTru, trangThai, email, gaLamViec, diaChiThuongTru, ngaySinh, gioiTinh, quocTich) VALUES
('NV-0001', N'Nguyễn Văn An', 'Pass@123', 'BAN_VE', '0901234567', '001085001234', N'15 Phố Huế, Hai Bà Trưng, Hà Nội', 'DANG_LAM', 'an.nguyenvan@azurerail.vn', 'GA-001', N'45 Nguyễn Trãi, Thanh Xuân, Hà Nội', '1995-03-12', 'NAM', N'Việt Nam');

INSERT INTO NhanVien (maNV, hoTen, [password], vaiTro, soDienThoai, cccd, diaChiTamTru, trangThai, email, gaLamViec, diaChiThuongTru, ngaySinh, gioiTinh, quocTich) VALUES
('NV-0002', N'Trần Thị Bình', 'Pass@456', 'BAN_VE', '0912345678', '001085002345', N'23 Trần Hưng Đạo, Hoàn Kiếm, Hà Nội', 'DANG_LAM', 'binh.tranthithi@azurerail.vn', 'GA-001', N'67 Bạch Mai, Hai Bà Trưng, Hà Nội', '1997-07-25', 'NU', N'Việt Nam');

INSERT INTO NhanVien (maNV, hoTen, [password], vaiTro, soDienThoai, cccd, diaChiTamTru, trangThai, email, gaLamViec, diaChiThuongTru, ngaySinh, gioiTinh, quocTich) VALUES
('NV-0003', N'Lê Hoàng Cường', 'Pass@789', 'DIEU_PHOI', '0923456789', '038085003456', N'10 Đường Phan Bội Châu, TP Vinh, Nghệ An', 'DANG_LAM', 'cuong.lehoang@azurerail.vn', 'GA-002', N'88 Lê Lợi, TP Vinh, Nghệ An', '1990-11-08', 'NAM', N'Việt Nam');

INSERT INTO NhanVien (maNV, hoTen, [password], vaiTro, soDienThoai, cccd, diaChiTamTru, trangThai, email, gaLamViec, diaChiThuongTru, ngaySinh, gioiTinh, quocTich) VALUES
('NV-0004', N'Phạm Minh Đức', 'Pass@101', 'BAN_VE', '0934567890', '038085004567', N'34 Nguyễn Sỹ Sách, TP Vinh, Nghệ An', 'DANG_LAM', 'duc.phamminhh@azurerail.vn', 'GA-002', N'12 Quang Trung, TP Vinh, Nghệ An', '1993-05-17', 'NAM', N'Việt Nam');

INSERT INTO NhanVien (maNV, hoTen, [password], vaiTro, soDienThoai, cccd, diaChiTamTru, trangThai, email, gaLamViec, diaChiThuongTru, ngaySinh, gioiTinh, quocTich) VALUES
('NV-0005', N'Hoàng Thị Elysa', 'Pass@102', 'BAN_VE', '0945678901', '046085005678', N'5 Bùi Thị Xuân, TP Huế, Thừa Thiên Huế', 'NGHI_PHEP', 'elysa.hoangthit@azurerail.vn', 'GA-003', N'22 Hùng Vương, TP Huế, Thừa Thiên Huế', '1998-09-30', 'NU', N'Việt Nam');

INSERT INTO NhanVien (maNV, hoTen, [password], vaiTro, soDienThoai, cccd, diaChiTamTru, trangThai, email, gaLamViec, diaChiThuongTru, ngaySinh, gioiTinh, quocTich) VALUES
('NV-0006', N'Võ Văn Phúc', 'Pass@103', 'DIEU_PHOI', '0956789012', '046085006789', N'18 Điện Biên Phủ, TP Huế, Thừa Thiên Huế', 'DANG_LAM', 'phuc.vovan@azurerail.vn', 'GA-003', N'99 Lê Thánh Tôn, TP Huế, Thừa Thiên Huế', '1988-02-14', 'NAM', N'Việt Nam');

INSERT INTO NhanVien (maNV, hoTen, [password], vaiTro, soDienThoai, cccd, diaChiTamTru, trangThai, email, gaLamViec, diaChiThuongTru, ngaySinh, gioiTinh, quocTich) VALUES
('NV-0007', N'Đặng Thùy Giang', 'Pass@104', 'BAN_VE', '0967890123', '048085007890', N'56 Hải Phòng, Thanh Khê, Đà Nẵng', 'DANG_LAM', 'giang.dangthuy@azurerail.vn', 'GA-004', N'30 Nguyễn Văn Linh, Hải Châu, Đà Nẵng', '1996-12-03', 'NU', N'Việt Nam');

INSERT INTO NhanVien (maNV, hoTen, [password], vaiTro, soDienThoai, cccd, diaChiTamTru, trangThai, email, gaLamViec, diaChiThuongTru, ngaySinh, gioiTinh, quocTich) VALUES
('NV-0008', N'Bùi Quốc Huy', 'Pass@105', 'BAN_VE', '0978901234', '048085008901', N'72 Trần Phú, Hải Châu, Đà Nẵng', 'DANG_LAM', 'huy.buiquoc@azurerail.vn', 'GA-004', N'14 Lê Duẩn, Hải Châu, Đà Nẵng', '1994-06-20', 'NAM', N'Việt Nam');

INSERT INTO NhanVien (maNV, hoTen, [password], vaiTro, soDienThoai, cccd, diaChiTamTru, trangThai, email, gaLamViec, diaChiThuongTru, ngaySinh, gioiTinh, quocTich) VALUES
('NV-0009', N'Ngô Thanh Inh', 'Pass@106', 'DIEU_PHOI', '0989012345', '079085009012', N'20 Nguyễn Thông, Quận 3, TP.HCM', 'DANG_LAM', 'inh.ngothanh@azurerail.vn', 'GA-005', N'55 Võ Thị Sáu, Quận 3, TP.HCM', '1987-04-11', 'NAM', N'Việt Nam');

INSERT INTO NhanVien (maNV, hoTen, [password], vaiTro, soDienThoai, cccd, diaChiTamTru, trangThai, email, gaLamViec, diaChiThuongTru, ngaySinh, gioiTinh, quocTich) VALUES
('NV-0010', N'Lý Thị Kim', 'Pass@107', 'BAN_VE', '0990123456', '079085010123', N'8 Nam Kỳ Khởi Nghĩa, Quận 1, TP.HCM', 'NGHI_PHEP', 'kim.lythi@azurerail.vn', 'GA-005', N'101 Cách Mạng Tháng 8, Quận 3, TP.HCM', '1999-08-16', 'NU', N'Việt Nam');

INSERT INTO NhanVien (maNV, hoTen, [password], vaiTro, soDienThoai, cccd, diaChiTamTru, trangThai, email, gaLamViec, diaChiThuongTru, ngaySinh, gioiTinh, quocTich) VALUES
('NV-0011', N'Trương Đình Lâm', 'Pass@108', 'BAN_VE', '0901122334', '001085011234', N'37 Kim Liên, Đống Đa, Hà Nội', 'DANG_LAM', 'lam.truongdinh@azurerail.vn', 'GA-001', N'9 Phạm Ngọc Thạch, Đống Đa, Hà Nội', '1992-01-28', 'NAM', N'Việt Nam');

INSERT INTO NhanVien (maNV, hoTen, [password], vaiTro, soDienThoai, cccd, diaChiTamTru, trangThai, email, gaLamViec, diaChiThuongTru, ngaySinh, gioiTinh, quocTich) VALUES
('NV-0012', N'Phan Thị Mai', 'Pass@109', 'DIEU_PHOI', '0912233445', '001085012345', N'44 Hàng Bông, Hoàn Kiếm, Hà Nội', 'DANG_LAM', 'mai.phanthi@azurerail.vn', 'GA-001', N'26 Tây Sơn, Đống Đa, Hà Nội', '1991-10-05', 'NU', N'Việt Nam');

INSERT INTO NhanVien (maNV, hoTen, [password], vaiTro, soDienThoai, cccd, diaChiTamTru, trangThai, email, gaLamViec, diaChiThuongTru, ngaySinh, gioiTinh, quocTich) VALUES
('NV-0013', N'Hồ Trọng Nam', 'Pass@110', 'BAN_VE', '0923344556', '038085013456', N'3 Lê Hồng Phong, TP Vinh, Nghệ An', 'DANG_LAM', 'nam.hotrong@azurerail.vn', 'GA-002', N'77 Đinh Công Tráng, TP Vinh, Nghệ An', '1989-07-19', 'NAM', N'Việt Nam');

INSERT INTO NhanVien (maNV, hoTen, [password], vaiTro, soDienThoai, cccd, diaChiTamTru, trangThai, email, gaLamViec, diaChiThuongTru, ngaySinh, gioiTinh, quocTich) VALUES
('NV-0014', N'Dương Thị Oanh', 'Pass@111', 'BAN_VE', '0934455667', '046085014567', N'11 Chu Văn An, TP Huế, Thừa Thiên Huế', 'DANG_LAM', 'oanh.duongthit@azurerail.vn', 'GA-003', N'50 Trần Cao Vân, TP Huế, Thừa Thiên Huế', '1996-03-22', 'NU', N'Việt Nam');

INSERT INTO NhanVien (maNV, hoTen, [password], vaiTro, soDienThoai, cccd, diaChiTamTru, trangThai, email, gaLamViec, diaChiThuongTru, ngaySinh, gioiTinh, quocTich) VALUES
('NV-0015', N'Tạ Minh Phong', 'Pass@112', 'DIEU_PHOI', '0945566778', '048085015678', N'29 Phan Châu Trinh, Hải Châu, Đà Nẵng', 'NGHI_PHEP', 'phong.taminh@azurerail.vn', 'GA-004', N'63 Ông Ích Khiêm, Thanh Khê, Đà Nẵng', '1985-11-14', 'NAM', N'Việt Nam');

INSERT INTO NhanVien (maNV, hoTen, [password], vaiTro, soDienThoai, cccd, diaChiTamTru, trangThai, email, gaLamViec, diaChiThuongTru, ngaySinh, gioiTinh, quocTich) VALUES
('NV-0016', N'Vũ Thị Quỳnh', 'Pass@113', 'BAN_VE', '0956677889', '048085016789', N'6 Trần Quý Cáp, Hải Châu, Đà Nẵng', 'DANG_LAM', 'quynh.vuthi@azurerail.vn', 'GA-004', N'18 Lê Văn Hiến, Ngũ Hành Sơn, Đà Nẵng', '1997-05-09', 'NU', N'Việt Nam');

INSERT INTO NhanVien (maNV, hoTen, [password], vaiTro, soDienThoai, cccd, diaChiTamTru, trangThai, email, gaLamViec, diaChiThuongTru, ngaySinh, gioiTinh, quocTich) VALUES
('NV-0017', N'Đinh Công Sơn', 'Pass@114', 'BAN_VE', '0967788990', '079085017890', N'40 Đinh Tiên Hoàng, Bình Thạnh, TP.HCM', 'DANG_LAM', 'son.dinhcong@azurerail.vn', 'GA-005', N'82 Phan Đình Giót, Bình Thạnh, TP.HCM', '1993-09-27', 'NAM', N'Việt Nam');

INSERT INTO NhanVien (maNV, hoTen, [password], vaiTro, soDienThoai, cccd, diaChiTamTru, trangThai, email, gaLamViec, diaChiThuongTru, ngaySinh, gioiTinh, quocTich) VALUES
('NV-0018', N'Mai Thị Tâm', 'Pass@115', 'BAN_VE', '0978899001', '079085018901', N'13 Lý Tự Trọng, Quận 1, TP.HCM', 'DANG_LAM', 'tam.maithi@azurerail.vn', 'GA-005', N'35 Trương Định, Quận 3, TP.HCM', '1998-02-06', 'NU', N'Việt Nam');

INSERT INTO NhanVien (maNV, hoTen, [password], vaiTro, soDienThoai, cccd, diaChiTamTru, trangThai, email, gaLamViec, diaChiThuongTru, ngaySinh, gioiTinh, quocTich) VALUES
('NV-0019', N'Lương Văn Uy', 'Pass@116', 'DIEU_PHOI', '0989900112', '001085019012', N'58 Giải Phóng, Hoàng Mai, Hà Nội', 'DANG_LAM', 'uy.luongvan@azurerail.vn', 'GA-001', N'24 Trương Định, Hoàng Mai, Hà Nội', '1986-06-13', 'NAM', N'Việt Nam');

INSERT INTO NhanVien (maNV, hoTen, [password], vaiTro, soDienThoai, cccd, diaChiTamTru, trangThai, email, gaLamViec, diaChiThuongTru, ngaySinh, gioiTinh, quocTich) VALUES
('NV-0020', N'Cao Thị Vân', 'Pass@117', 'BAN_VE', '0990011223', '001085020123', N'19 Xã Đàn, Đống Đa, Hà Nội', 'DA_NGHI', 'van.caothi@azurerail.vn', 'GA-001', N'7 La Thành, Đống Đa, Hà Nội', '1990-12-31', 'NU', N'Việt Nam');

INSERT INTO NhanVien (maNV, hoTen, [password], vaiTro, soDienThoai, cccd, diaChiTamTru, trangThai, email, gaLamViec, diaChiThuongTru, ngaySinh, gioiTinh, quocTich) VALUES
('NV-0021', N'Châu Quốc Xuân', 'Pass@118', 'BAN_VE', '0901233210', '079085021234', N'25 Bà Huyện Thanh Quan, Quận 3, TP.HCM', 'NGHI_PHEP', 'xuan.chauquoc@azurerail.vn', 'GA-005', N'16 Đinh Tiên Hoàng, Quận 1, TP.HCM', '1995-08-21', 'NAM', N'Việt Nam');

INSERT INTO NhanVien (maNV, hoTen, [password], vaiTro, soDienThoai, cccd, diaChiTamTru, trangThai, email, gaLamViec, diaChiThuongTru, ngaySinh, gioiTinh, quocTich) VALUES
('NV-0022', N'Kiều Thị Yến', 'Pass@119', 'DIEU_PHOI', '0912344321', '046085022345', N'31 Trần Thị Lý, Hải Châu, Đà Nẵng', 'DA_NGHI', 'yen.kieuthit@azurerail.vn', 'GA-003', N'48 Lê Lợi, TP Huế, Thừa Thiên Huế', '1988-04-17', 'NU', N'Việt Nam');

INSERT INTO NhanVien (maNV, hoTen, [password], vaiTro, soDienThoai, cccd, diaChiTamTru, trangThai, email, gaLamViec, diaChiThuongTru, ngaySinh, gioiTinh, quocTich) VALUES
('NV-0023', N'Trịnh Đức Zũng', 'Pass@120', 'BAN_VE', '0923455432', '038085023456', N'62 Nguyễn Viết Xuân, TP Vinh, Nghệ An', 'DA_NGHI', 'zung.trinhduc@azurerail.vn', 'GA-002', N'33 Nguyễn Du, TP Vinh, Nghệ An', '1991-10-10', 'NAM', N'Việt Nam');

-- ==================== 2. KhachHang (BANG MOI) ====================
INSERT INTO KhachHang (maKhachHang, hoTen, cccd, soDienThoai, email, diaChiThuongTru, diaChiTamTru, ngaySinh, gioiTinh, quocTich) VALUES
('KH-0001', N'Phạm Minh Tuấn', '012345678901', '0371234567', 'tuan.phamminh@email.com', N'12 Lê Lợi, Hải Châu, Đà Nẵng', NULL, '1992-03-15', 'NAM', N'Việt Nam'),
('KH-0002', N'Hoàng Đức Mạnh', '034567890123', '0382345678', 'manh.hoangduc@email.com', N'55 Nguyễn Du, TP Vinh, Nghệ An', NULL, '1988-08-22', 'NAM', N'Việt Nam'),
('KH-0003', N'Nguyễn Thị Hoa', '056789012345', '0393456789', 'hoa.nguyenthi@email.com', N'88 Lê Thánh Tôn, TP Huế, Thừa Thiên Huế', NULL, '1995-12-03', 'NU', N'Việt Nam'),
('KH-0004', N'Trần Văn Đức', '078901234567', '0354567890', 'duc.tranvan@email.com', N'21 Phạm Văn Đồng, Cầu Giấy, Hà Nội', NULL, '1990-05-18', 'NAM', N'Việt Nam'),
('KH-0005', N'Lý Văn Hùng', '090123456789', '0365678901', 'hung.lyvan@email.com', N'40 Trần Phú, Hải Châu, Đà Nẵng', N'5 Lê Lợi, Quận 1, TP.HCM', '1993-11-27', 'NAM', N'Việt Nam');

-- ==================== 3. Ga ====================
-- Tuyen duong sat Bac-Nam (Thong Nhat), 17 ga chinh theo dia ly tu Bac xuong Nam
-- Nguon: Tong cong ty Duong sat Viet Nam (VNR) - dia chi chinh thuc
INSERT INTO Ga VALUES ('GA-001', N'Ga Hà Nội',               N'120 Lê Duẩn, Hoàn Kiếm, Hà Nội');
INSERT INTO Ga VALUES ('GA-002', N'Ga Vinh',                  N'Đường Phan Bội Châu, TP Vinh, Nghệ An');
INSERT INTO Ga VALUES ('GA-003', N'Ga Huế',                   N'2 Bùi Thị Xuân, TP Huế, Thừa Thiên Huế');
INSERT INTO Ga VALUES ('GA-004', N'Ga Đà Nẵng',               N'791 Hải Phòng, Thanh Khê, Đà Nẵng');
INSERT INTO Ga VALUES ('GA-005', N'Ga Sài Gòn',               N'1 Nguyễn Thông, Quận 3, TP.HCM');
-- Ga trung gian (giu nguyen ID 001-005 de tuong thich du lieu hien co)
INSERT INTO Ga VALUES ('GA-006', N'Ga Nam Định',              N'9 Trần Quý Cáp, TP Nam Định, Nam Định');
INSERT INTO Ga VALUES ('GA-007', N'Ga Ninh Bình',             N'Đường Lý Thái Tổ, TP Ninh Bình, Ninh Bình');
INSERT INTO Ga VALUES ('GA-008', N'Ga Thanh Hóa',             N'Đường Trần Phú, TP Thanh Hóa, Thanh Hóa');
INSERT INTO Ga VALUES ('GA-009', N'Ga Đồng Hới',              N'Đường Trần Hưng Đạo, TP Đồng Hới, Quảng Bình');
INSERT INTO Ga VALUES ('GA-010', N'Ga Đông Hà',               N'Đường Lê Duẩn, TP Đông Hà, Quảng Trị');
INSERT INTO Ga VALUES ('GA-011', N'Ga Tam Kỳ',                N'191 Phan Chu Trinh, TP Tam Kỳ, Quảng Nam');
INSERT INTO Ga VALUES ('GA-012', N'Ga Quảng Ngãi',            N'Đường Nguyễn Bỉnh Khiêm, TP Quảng Ngãi, Quảng Ngãi');
INSERT INTO Ga VALUES ('GA-013', N'Ga Diêu Trì',              N'Thị trấn Diêu Trì, huyện Tuy Phước, Bình Định');
INSERT INTO Ga VALUES ('GA-014', N'Ga Tuy Hòa',               N'Đường Lê Duẩn, TP Tuy Hòa, Phú Yên');
INSERT INTO Ga VALUES ('GA-015', N'Ga Nha Trang',             N'17 Thái Nguyên, TP Nha Trang, Khánh Hòa');
INSERT INTO Ga VALUES ('GA-016', N'Ga Phan Rang-Tháp Chàm',  N'Đường Thống Nhất, TP Phan Rang-Tháp Chàm, Ninh Thuận');
INSERT INTO Ga VALUES ('GA-017', N'Ga Biên Hòa',              N'1 Hà Huy Giáp, TP Biên Hòa, Đồng Nai');

-- ==================== 4. DauMay ====================
-- Nguon: Tong cong ty Duong sat Viet Nam (VNR)
-- D19E: diesel-dien Dong Phong DF7G (Trung Quoc), 60 don vi (901-960), nhap 2006-2014
--        cong suat 1500kW, toc do toi da 100km/h, su dung tren tuyen Bac-Nam chinh
INSERT INTO DauMay (maDauMay, tenDauMay, hangSanXuat, namSanXuat, congSuatKw, trangThai, moTa) VALUES
('DM-001', N'Đầu máy D19E-901', N'Dongfang (Trung Quốc)', 2007, 1500, N'Đang hoạt động', N'Đầu kéo chính tuyến Bắc - Nam, vận hành ổn định.');
INSERT INTO DauMay (maDauMay, tenDauMay, hangSanXuat, namSanXuat, congSuatKw, trangThai, moTa) VALUES
('DM-002', N'Đầu máy D19E-902', N'Dongfang (Trung Quốc)', 2008, 1500, N'Đang hoạt động', N'Đầu kéo chính cho tàu khách nhanh.');
INSERT INTO DauMay (maDauMay, tenDauMay, hangSanXuat, namSanXuat, congSuatKw, trangThai, moTa) VALUES
('DM-003', N'Đầu máy D19E-903', N'Dongfang (Trung Quốc)', 2009, 1500, N'Đang hoạt động', N'Vận hành trên hành trình dài liên tỉnh.');
INSERT INTO DauMay (maDauMay, tenDauMay, hangSanXuat, namSanXuat, congSuatKw, trangThai, moTa) VALUES
('DM-004', N'Đầu máy D19E-904', N'Dongfang (Trung Quốc)', 2010, 1500, N'Bảo trì', N'Đang bảo trì định kỳ hệ thống truyền động.');
INSERT INTO DauMay (maDauMay, tenDauMay, hangSanXuat, namSanXuat, congSuatKw, trangThai, moTa) VALUES
('DM-005', N'Đầu máy D19E-905', N'Dongfang (Trung Quốc)', 2011, 1500, N'Đang hoạt động', N'Đầu kéo dự phòng cho các chuyến tăng cường.');
-- D14E: diesel-dien EMD (My), 7 don vi (001-007), nhap 1997
--        cong suat 1490kW, su dung tuyen chinh Bac-Nam
INSERT INTO DauMay (maDauMay, tenDauMay, hangSanXuat, namSanXuat, congSuatKw, trangThai, moTa) VALUES
('DM-006', N'Đầu máy D14E-001', N'EMD (Mỹ)', 1997, 1490, N'Đang hoạt động', N'Đầu máy diesel điện thế hệ cũ, hiệu suất ổn định.');
INSERT INTO DauMay (maDauMay, tenDauMay, hangSanXuat, namSanXuat, congSuatKw, trangThai, moTa) VALUES
('DM-007', N'Đầu máy D14E-002', N'EMD (Mỹ)', 1997, 1490, N'Đang hoạt động', N'Thường khai thác trên tuyến đường dài nhiều chặng.');
-- D13E: diesel-thuy luc Mitsubishi/Toshiba (Nhat), 68 don vi, nhap 1992-1996
--        cong suat 900kW, toc do toi da 90km/h, su dung tuyen chinh va nhanh
INSERT INTO DauMay (maDauMay, tenDauMay, hangSanXuat, namSanXuat, congSuatKw, trangThai, moTa) VALUES
('DM-008', N'Đầu máy D13E-006', N'Mitsubishi/Toshiba (Nhật Bản)', 1993, 900, N'Đang hoạt động', N'Phục vụ tuyến ngắn và trung bình, tiết kiệm nhiên liệu.');
INSERT INTO DauMay (maDauMay, tenDauMay, hangSanXuat, namSanXuat, congSuatKw, trangThai, moTa) VALUES
('DM-009', N'Đầu máy D13E-012', N'Mitsubishi/Toshiba (Nhật Bản)', 1994, 900, N'Đang hoạt động', N'Đầu kéo linh hoạt cho nhiều loại đoàn tàu.');
INSERT INTO DauMay (maDauMay, tenDauMay, hangSanXuat, namSanXuat, congSuatKw, trangThai, moTa) VALUES
('DM-010', N'Đầu máy D13E-018', N'Mitsubishi/Toshiba (Nhật Bản)', 1995, 900, N'Ngừng khai thác', N'Tạm ngừng khai thác để chờ nâng cấp lớn.');
-- D12E: diesel Krupp (Duc), 25 don vi, nhap 1988, hien dung tuyen nhanh va hang hoa
INSERT INTO DauMay (maDauMay, tenDauMay, hangSanXuat, namSanXuat, congSuatKw, trangThai, moTa) VALUES
('DM-011', N'Đầu máy D12E-001', N'Krupp (Đức)', 1988, 1200, N'Đang hoạt động', N'Đầu máy lâu năm, thường dùng cho tàu hàng và tuyến phụ.');

-- ==================== 5. ToaTau ====================
-- 3 mau toa chuan: moi loai 1 toa, so ghe theo quy dinh
-- TOA-001: Giuong nam (3x10 = 30 giuong)
-- TOA-002: Ghe mem    (4x12 = 48 ghe)
-- TOA-003: Ghe cung   (4x12 = 48 ghe)
INSERT INTO ToaTau (maToaTau, loaiGhe) VALUES ('TOA-001', 'GIUONG_NAM');
INSERT INTO ToaTau (maToaTau, loaiGhe) VALUES ('TOA-002', 'GHE_MEM');
INSERT INTO ToaTau (maToaTau, loaiGhe) VALUES ('TOA-003', 'GHE_CUNG');

-- ==================== 6. Tuyen ====================
-- Ghi chu: Tuyen trong DB la tuyen dich vu (ga di - ga den), khong phai chi doan vat ly.
-- TUY-001..010: Tuyen express giua cac ga chinh (giu nguyen de tuong thich du lieu mau)
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY-001', 'GA-001', 'GA-002', 319);   -- Ha Noi -> Vinh
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY-002', 'GA-002', 'GA-003', 368);   -- Vinh -> Hue
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY-003', 'GA-003', 'GA-004', 100);   -- Hue -> Da Nang
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY-004', 'GA-004', 'GA-005', 935);   -- Da Nang -> Sai Gon
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY-005', 'GA-005', 'GA-004', 935);   -- Sai Gon -> Da Nang
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY-006', 'GA-004', 'GA-003', 100);   -- Da Nang -> Hue
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY-007', 'GA-003', 'GA-002', 368);   -- Hue -> Vinh
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY-008', 'GA-002', 'GA-001', 319);   -- Vinh -> Ha Noi
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY-009', 'GA-001', 'GA-005', 1726);  -- Ha Noi -> Sai Gon (xuyen Viet)
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY-010', 'GA-005', 'GA-001', 1726);  -- Sai Gon -> Ha Noi (xuyen Viet)
-- TUY-011..025: Doan trung gian Bac->Nam (theo thu tu dia ly)
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY-011', 'GA-001', 'GA-006', 87);    -- Ha Noi -> Nam Dinh     (~87 km)
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY-012', 'GA-006', 'GA-007', 30);    -- Nam Dinh -> Ninh Binh  (~30 km)
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY-013', 'GA-007', 'GA-008', 57);    -- Ninh Binh -> Thanh Hoa (~57 km)
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY-014', 'GA-008', 'GA-002', 73);    -- Thanh Hoa -> Vinh      (~73 km)
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY-015', 'GA-002', 'GA-009', 166);   -- Vinh -> Dong Hoi       (~166 km)
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY-016', 'GA-009', 'GA-010', 72);    -- Dong Hoi -> Dong Ha    (~72 km)
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY-017', 'GA-010', 'GA-003', 70);    -- Dong Ha -> Hue         (~70 km)
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY-018', 'GA-003', 'GA-004', 100);   -- Hue -> Da Nang (qua Hai Van, ~100 km)
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY-019', 'GA-004', 'GA-011', 72);    -- Da Nang -> Tam Ky      (~72 km)
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY-020', 'GA-011', 'GA-012', 40);    -- Tam Ky -> Quang Ngai   (~40 km)
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY-021', 'GA-012', 'GA-013', 107);   -- Quang Ngai -> Dieu Tri (~107 km)
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY-022', 'GA-013', 'GA-014', 85);    -- Dieu Tri -> Tuy Hoa    (~85 km)
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY-023', 'GA-014', 'GA-015', 104);   -- Tuy Hoa -> Nha Trang   (~104 km)
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY-024', 'GA-015', 'GA-016', 101);   -- Nha Trang -> Phan Rang (~101 km)
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY-025', 'GA-016', 'GA-017', 185);   -- Phan Rang -> Bien Hoa  (~185 km)
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY-026', 'GA-017', 'GA-005', 32);    -- Bien Hoa -> Sai Gon    (~32 km)
-- TUY-027..040: Doan trung gian Nam->Bac (nguoc chieu)
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY-027', 'GA-006', 'GA-001', 87);    -- Nam Dinh -> Ha Noi
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY-028', 'GA-007', 'GA-006', 30);    -- Ninh Binh -> Nam Dinh
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY-029', 'GA-008', 'GA-007', 57);    -- Thanh Hoa -> Ninh Binh
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY-030', 'GA-002', 'GA-008', 73);    -- Vinh -> Thanh Hoa
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY-031', 'GA-009', 'GA-002', 166);   -- Dong Hoi -> Vinh
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY-032', 'GA-010', 'GA-009', 72);    -- Dong Ha -> Dong Hoi
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY-033', 'GA-003', 'GA-010', 70);    -- Hue -> Dong Ha
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY-034', 'GA-011', 'GA-004', 72);    -- Tam Ky -> Da Nang
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY-035', 'GA-012', 'GA-011', 40);    -- Quang Ngai -> Tam Ky
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY-036', 'GA-013', 'GA-012', 107);   -- Dieu Tri -> Quang Ngai
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY-037', 'GA-014', 'GA-013', 85);    -- Tuy Hoa -> Dieu Tri
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY-038', 'GA-015', 'GA-014', 104);   -- Nha Trang -> Tuy Hoa
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY-039', 'GA-016', 'GA-015', 101);   -- Phan Rang -> Nha Trang
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY-040', 'GA-017', 'GA-016', 185);   -- Bien Hoa -> Phan Rang
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY-041', 'GA-005', 'GA-017', 32);    -- Sai Gon -> Bien Hoa

-- ==================== 7. DoanTau ====================
-- Ten tau theo quy uoc cua VNR (SE = Speed Express, TN = Thong Nhat)
-- Tau SE1/SE2 chay hang ngay tren tuyen Thong Nhat (Sai Gon <-> Ha Noi, ~1726km)
-- Tau SE3/SE4: tuyen chinh xuat phat 06:00; SE7/SE8: Ha Noi <-> Da Nang
-- TN1/TN2: tau Thong Nhat phu (cham hon SE)
INSERT INTO DoanTau VALUES ('DT-001', N'SE1 (Sài Gòn → Hà Nội)', 'DM-001');
INSERT INTO DoanTau VALUES ('DT-002', N'SE2 (Hà Nội → Sài Gòn)', 'DM-002');
INSERT INTO DoanTau VALUES ('DT-003', N'SE7 (Hà Nội → Đà Nẵng)', 'DM-003');

-- ==================== 8. ChiTietDoanTau ====================
-- DT-001 (SE1): TOA-003(cung thu 1), TOA-002(mem thu 2), TOA-001(giuong thu 3)
INSERT INTO ChiTietDoanTau VALUES ('CTDT-001', 'DT-001', 'TOA-003', 1);
INSERT INTO ChiTietDoanTau VALUES ('CTDT-002', 'DT-001', 'TOA-002', 2);
INSERT INTO ChiTietDoanTau VALUES ('CTDT-003', 'DT-001', 'TOA-001', 3);
-- DT-002 (SE2): TOA-003(cung thu 1), TOA-002(mem thu 2), TOA-001(giuong thu 3)
INSERT INTO ChiTietDoanTau VALUES ('CTDT-004', 'DT-002', 'TOA-003', 1);
INSERT INTO ChiTietDoanTau VALUES ('CTDT-005', 'DT-002', 'TOA-002', 2);
INSERT INTO ChiTietDoanTau VALUES ('CTDT-006', 'DT-002', 'TOA-001', 3);
-- DT-003 (SE7): TOA-003(cung thu 1), TOA-002(mem thu 2)
INSERT INTO ChiTietDoanTau VALUES ('CTDT-007', 'DT-003', 'TOA-003', 1);
INSERT INTO ChiTietDoanTau VALUES ('CTDT-008', 'DT-003', 'TOA-002', 2);

-- ==================== 9. Ghe ====================
-- TOA-001 (giuong nam): 3x10 = 30 giuong, danh so 1..30 theo hang trai->phai, tren->duoi
INSERT INTO Ghe VALUES ('G-001-01', 'TOA-001',  1); INSERT INTO Ghe VALUES ('G-001-02', 'TOA-001',  2); INSERT INTO Ghe VALUES ('G-001-03', 'TOA-001',  3);
INSERT INTO Ghe VALUES ('G-001-04', 'TOA-001',  4); INSERT INTO Ghe VALUES ('G-001-05', 'TOA-001',  5); INSERT INTO Ghe VALUES ('G-001-06', 'TOA-001',  6);
INSERT INTO Ghe VALUES ('G-001-07', 'TOA-001',  7); INSERT INTO Ghe VALUES ('G-001-08', 'TOA-001',  8); INSERT INTO Ghe VALUES ('G-001-09', 'TOA-001',  9);
INSERT INTO Ghe VALUES ('G-001-10', 'TOA-001', 10); INSERT INTO Ghe VALUES ('G-001-11', 'TOA-001', 11); INSERT INTO Ghe VALUES ('G-001-12', 'TOA-001', 12);
INSERT INTO Ghe VALUES ('G-001-13', 'TOA-001', 13); INSERT INTO Ghe VALUES ('G-001-14', 'TOA-001', 14); INSERT INTO Ghe VALUES ('G-001-15', 'TOA-001', 15);
INSERT INTO Ghe VALUES ('G-001-16', 'TOA-001', 16); INSERT INTO Ghe VALUES ('G-001-17', 'TOA-001', 17); INSERT INTO Ghe VALUES ('G-001-18', 'TOA-001', 18);
INSERT INTO Ghe VALUES ('G-001-19', 'TOA-001', 19); INSERT INTO Ghe VALUES ('G-001-20', 'TOA-001', 20); INSERT INTO Ghe VALUES ('G-001-21', 'TOA-001', 21);
INSERT INTO Ghe VALUES ('G-001-22', 'TOA-001', 22); INSERT INTO Ghe VALUES ('G-001-23', 'TOA-001', 23); INSERT INTO Ghe VALUES ('G-001-24', 'TOA-001', 24);
INSERT INTO Ghe VALUES ('G-001-25', 'TOA-001', 25); INSERT INTO Ghe VALUES ('G-001-26', 'TOA-001', 26); INSERT INTO Ghe VALUES ('G-001-27', 'TOA-001', 27);
INSERT INTO Ghe VALUES ('G-001-28', 'TOA-001', 28); INSERT INTO Ghe VALUES ('G-001-29', 'TOA-001', 29); INSERT INTO Ghe VALUES ('G-001-30', 'TOA-001', 30);
-- TOA-002 (ghe mem): 4x12 = 48 ghe
INSERT INTO Ghe VALUES ('G-002-01', 'TOA-002',  1); INSERT INTO Ghe VALUES ('G-002-02', 'TOA-002',  2); INSERT INTO Ghe VALUES ('G-002-03', 'TOA-002',  3);
INSERT INTO Ghe VALUES ('G-002-04', 'TOA-002',  4); INSERT INTO Ghe VALUES ('G-002-05', 'TOA-002',  5); INSERT INTO Ghe VALUES ('G-002-06', 'TOA-002',  6);
INSERT INTO Ghe VALUES ('G-002-07', 'TOA-002',  7); INSERT INTO Ghe VALUES ('G-002-08', 'TOA-002',  8); INSERT INTO Ghe VALUES ('G-002-09', 'TOA-002',  9);
INSERT INTO Ghe VALUES ('G-002-10', 'TOA-002', 10); INSERT INTO Ghe VALUES ('G-002-11', 'TOA-002', 11); INSERT INTO Ghe VALUES ('G-002-12', 'TOA-002', 12);
INSERT INTO Ghe VALUES ('G-002-13', 'TOA-002', 13); INSERT INTO Ghe VALUES ('G-002-14', 'TOA-002', 14); INSERT INTO Ghe VALUES ('G-002-15', 'TOA-002', 15);
INSERT INTO Ghe VALUES ('G-002-16', 'TOA-002', 16); INSERT INTO Ghe VALUES ('G-002-17', 'TOA-002', 17); INSERT INTO Ghe VALUES ('G-002-18', 'TOA-002', 18);
INSERT INTO Ghe VALUES ('G-002-19', 'TOA-002', 19); INSERT INTO Ghe VALUES ('G-002-20', 'TOA-002', 20); INSERT INTO Ghe VALUES ('G-002-21', 'TOA-002', 21);
INSERT INTO Ghe VALUES ('G-002-22', 'TOA-002', 22); INSERT INTO Ghe VALUES ('G-002-23', 'TOA-002', 23); INSERT INTO Ghe VALUES ('G-002-24', 'TOA-002', 24);
INSERT INTO Ghe VALUES ('G-002-25', 'TOA-002', 25); INSERT INTO Ghe VALUES ('G-002-26', 'TOA-002', 26); INSERT INTO Ghe VALUES ('G-002-27', 'TOA-002', 27);
INSERT INTO Ghe VALUES ('G-002-28', 'TOA-002', 28); INSERT INTO Ghe VALUES ('G-002-29', 'TOA-002', 29); INSERT INTO Ghe VALUES ('G-002-30', 'TOA-002', 30);
INSERT INTO Ghe VALUES ('G-002-31', 'TOA-002', 31); INSERT INTO Ghe VALUES ('G-002-32', 'TOA-002', 32); INSERT INTO Ghe VALUES ('G-002-33', 'TOA-002', 33);
INSERT INTO Ghe VALUES ('G-002-34', 'TOA-002', 34); INSERT INTO Ghe VALUES ('G-002-35', 'TOA-002', 35); INSERT INTO Ghe VALUES ('G-002-36', 'TOA-002', 36);
INSERT INTO Ghe VALUES ('G-002-37', 'TOA-002', 37); INSERT INTO Ghe VALUES ('G-002-38', 'TOA-002', 38); INSERT INTO Ghe VALUES ('G-002-39', 'TOA-002', 39);
INSERT INTO Ghe VALUES ('G-002-40', 'TOA-002', 40); INSERT INTO Ghe VALUES ('G-002-41', 'TOA-002', 41); INSERT INTO Ghe VALUES ('G-002-42', 'TOA-002', 42);
INSERT INTO Ghe VALUES ('G-002-43', 'TOA-002', 43); INSERT INTO Ghe VALUES ('G-002-44', 'TOA-002', 44); INSERT INTO Ghe VALUES ('G-002-45', 'TOA-002', 45);
INSERT INTO Ghe VALUES ('G-002-46', 'TOA-002', 46); INSERT INTO Ghe VALUES ('G-002-47', 'TOA-002', 47); INSERT INTO Ghe VALUES ('G-002-48', 'TOA-002', 48);
-- TOA-003 (ghe cung): 4x12 = 48 ghe
INSERT INTO Ghe VALUES ('G-003-01', 'TOA-003',  1); INSERT INTO Ghe VALUES ('G-003-02', 'TOA-003',  2); INSERT INTO Ghe VALUES ('G-003-03', 'TOA-003',  3);
INSERT INTO Ghe VALUES ('G-003-04', 'TOA-003',  4); INSERT INTO Ghe VALUES ('G-003-05', 'TOA-003',  5); INSERT INTO Ghe VALUES ('G-003-06', 'TOA-003',  6);
INSERT INTO Ghe VALUES ('G-003-07', 'TOA-003',  7); INSERT INTO Ghe VALUES ('G-003-08', 'TOA-003',  8); INSERT INTO Ghe VALUES ('G-003-09', 'TOA-003',  9);
INSERT INTO Ghe VALUES ('G-003-10', 'TOA-003', 10); INSERT INTO Ghe VALUES ('G-003-11', 'TOA-003', 11); INSERT INTO Ghe VALUES ('G-003-12', 'TOA-003', 12);
INSERT INTO Ghe VALUES ('G-003-13', 'TOA-003', 13); INSERT INTO Ghe VALUES ('G-003-14', 'TOA-003', 14); INSERT INTO Ghe VALUES ('G-003-15', 'TOA-003', 15);
INSERT INTO Ghe VALUES ('G-003-16', 'TOA-003', 16); INSERT INTO Ghe VALUES ('G-003-17', 'TOA-003', 17); INSERT INTO Ghe VALUES ('G-003-18', 'TOA-003', 18);
INSERT INTO Ghe VALUES ('G-003-19', 'TOA-003', 19); INSERT INTO Ghe VALUES ('G-003-20', 'TOA-003', 20); INSERT INTO Ghe VALUES ('G-003-21', 'TOA-003', 21);
INSERT INTO Ghe VALUES ('G-003-22', 'TOA-003', 22); INSERT INTO Ghe VALUES ('G-003-23', 'TOA-003', 23); INSERT INTO Ghe VALUES ('G-003-24', 'TOA-003', 24);
INSERT INTO Ghe VALUES ('G-003-25', 'TOA-003', 25); INSERT INTO Ghe VALUES ('G-003-26', 'TOA-003', 26); INSERT INTO Ghe VALUES ('G-003-27', 'TOA-003', 27);
INSERT INTO Ghe VALUES ('G-003-28', 'TOA-003', 28); INSERT INTO Ghe VALUES ('G-003-29', 'TOA-003', 29); INSERT INTO Ghe VALUES ('G-003-30', 'TOA-003', 30);
INSERT INTO Ghe VALUES ('G-003-31', 'TOA-003', 31); INSERT INTO Ghe VALUES ('G-003-32', 'TOA-003', 32); INSERT INTO Ghe VALUES ('G-003-33', 'TOA-003', 33);
INSERT INTO Ghe VALUES ('G-003-34', 'TOA-003', 34); INSERT INTO Ghe VALUES ('G-003-35', 'TOA-003', 35); INSERT INTO Ghe VALUES ('G-003-36', 'TOA-003', 36);
INSERT INTO Ghe VALUES ('G-003-37', 'TOA-003', 37); INSERT INTO Ghe VALUES ('G-003-38', 'TOA-003', 38); INSERT INTO Ghe VALUES ('G-003-39', 'TOA-003', 39);
INSERT INTO Ghe VALUES ('G-003-40', 'TOA-003', 40); INSERT INTO Ghe VALUES ('G-003-41', 'TOA-003', 41); INSERT INTO Ghe VALUES ('G-003-42', 'TOA-003', 42);
INSERT INTO Ghe VALUES ('G-003-43', 'TOA-003', 43); INSERT INTO Ghe VALUES ('G-003-44', 'TOA-003', 44); INSERT INTO Ghe VALUES ('G-003-45', 'TOA-003', 45);
INSERT INTO Ghe VALUES ('G-003-46', 'TOA-003', 46); INSERT INTO Ghe VALUES ('G-003-47', 'TOA-003', 47); INSERT INTO Ghe VALUES ('G-003-48', 'TOA-003', 48);

-- ==================== 10. Lich (thoiGianChay da doi sang INT = so phut) ====================
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH-001', 'TUY-001', 'DT-001', '2026-04-10 06:00:00', 330);   -- 5h30p
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH-002', 'TUY-001', 'DT-001', '2026-04-11 06:00:00', 330);   -- 5h30p
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH-003', 'TUY-002', 'DT-002', '2026-04-10 14:00:00', 360);   -- 6h
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH-004', 'TUY-002', 'DT-002', '2026-04-11 14:00:00', 360);   -- 6h
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH-005', 'TUY-003', 'DT-003', '2026-04-10 08:00:00', 150);   -- 2h30p
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH-006', 'TUY-004', 'DT-001', '2026-04-12 19:00:00', 1020);  -- 17h
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH-007', 'TUY-005', 'DT-002', '2026-04-13 07:00:00', 1020);  -- 17h
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH-008', 'TUY-009', 'DT-003', '2026-04-15 19:00:00', 1980);  -- 33h

-- ==================== 11. Gia ====================
INSERT INTO Gia VALUES ('GIA-001', '2026-01-01', '2026-12-31', N'Bảng giá thường 2026', 0);
INSERT INTO Gia VALUES ('GIA-002', '2026-01-25', '2026-02-10', N'Bảng giá Tết Nguyên Đán 2026', 0);
INSERT INTO Gia VALUES ('GIA-003', '2026-06-01', '2026-08-31', N'Bảng giá mùa hè 2026', 0);

-- ==================== 12. ChiTietGia (giaNiemYet da doi sang DECIMAL) ====================
-- Bang gia thuong (GIA-001)
INSERT INTO ChiTietGia VALUES ('CTG-001', 'GIA-001', 'TUY-001', 'GHE_CUNG', 180000.00);
INSERT INTO ChiTietGia VALUES ('CTG-002', 'GIA-001', 'TUY-001', 'GHE_MEM', 250000.00);
INSERT INTO ChiTietGia VALUES ('CTG-003', 'GIA-001', 'TUY-001', 'GIUONG_NAM', 400000.00);
INSERT INTO ChiTietGia VALUES ('CTG-004', 'GIA-001', 'TUY-002', 'GHE_CUNG', 200000.00);
INSERT INTO ChiTietGia VALUES ('CTG-005', 'GIA-001', 'TUY-002', 'GHE_MEM', 280000.00);
INSERT INTO ChiTietGia VALUES ('CTG-006', 'GIA-001', 'TUY-002', 'GIUONG_NAM', 450000.00);
INSERT INTO ChiTietGia VALUES ('CTG-007', 'GIA-001', 'TUY-003', 'GHE_CUNG', 80000.00);
INSERT INTO ChiTietGia VALUES ('CTG-008', 'GIA-001', 'TUY-003', 'GHE_MEM', 120000.00);
INSERT INTO ChiTietGia VALUES ('CTG-009', 'GIA-001', 'TUY-003', 'GIUONG_NAM', 200000.00);
INSERT INTO ChiTietGia VALUES ('CTG-010', 'GIA-001', 'TUY-004', 'GHE_CUNG', 450000.00);
INSERT INTO ChiTietGia VALUES ('CTG-011', 'GIA-001', 'TUY-004', 'GHE_MEM', 600000.00);
INSERT INTO ChiTietGia VALUES ('CTG-012', 'GIA-001', 'TUY-004', 'GIUONG_NAM', 900000.00);
INSERT INTO ChiTietGia VALUES ('CTG-013', 'GIA-001', 'TUY-005', 'GHE_CUNG', 450000.00);
INSERT INTO ChiTietGia VALUES ('CTG-014', 'GIA-001', 'TUY-005', 'GHE_MEM', 600000.00);
INSERT INTO ChiTietGia VALUES ('CTG-015', 'GIA-001', 'TUY-005', 'GIUONG_NAM', 900000.00);
INSERT INTO ChiTietGia VALUES ('CTG-016', 'GIA-001', 'TUY-009', 'GHE_CUNG', 800000.00);
INSERT INTO ChiTietGia VALUES ('CTG-017', 'GIA-001', 'TUY-009', 'GHE_MEM', 1100000.00);
INSERT INTO ChiTietGia VALUES ('CTG-018', 'GIA-001', 'TUY-009', 'GIUONG_NAM', 1600000.00);
-- Bang gia thuong (GIA-001) cho cac tuyen trung gian moi (gia xap xi thuc te VNR 2024)
-- Ghe cung(B) / Ghe mem(C) / Giuong nam(A) - ty le ~1 : 1.45 : 2.2
-- TUY-011: Ha Noi -> Nam Dinh (~87km)
INSERT INTO ChiTietGia VALUES ('CTG-025', 'GIA-001', 'TUY-011', 'GHE_CUNG',  55000.00);
INSERT INTO ChiTietGia VALUES ('CTG-026', 'GIA-001', 'TUY-011', 'GHE_MEM',   80000.00);
INSERT INTO ChiTietGia VALUES ('CTG-027', 'GIA-001', 'TUY-011', 'GIUONG_NAM',125000.00);
-- TUY-012: Nam Dinh -> Ninh Binh (~30km)
INSERT INTO ChiTietGia VALUES ('CTG-028', 'GIA-001', 'TUY-012', 'GHE_CUNG',  25000.00);
INSERT INTO ChiTietGia VALUES ('CTG-029', 'GIA-001', 'TUY-012', 'GHE_MEM',   36000.00);
INSERT INTO ChiTietGia VALUES ('CTG-030', 'GIA-001', 'TUY-012', 'GIUONG_NAM', 56000.00);
-- TUY-013: Ninh Binh -> Thanh Hoa (~57km)
INSERT INTO ChiTietGia VALUES ('CTG-031', 'GIA-001', 'TUY-013', 'GHE_CUNG',  40000.00);
INSERT INTO ChiTietGia VALUES ('CTG-032', 'GIA-001', 'TUY-013', 'GHE_MEM',   58000.00);
INSERT INTO ChiTietGia VALUES ('CTG-033', 'GIA-001', 'TUY-013', 'GIUONG_NAM', 90000.00);
-- TUY-014: Thanh Hoa -> Vinh (~73km)
INSERT INTO ChiTietGia VALUES ('CTG-034', 'GIA-001', 'TUY-014', 'GHE_CUNG',  55000.00);
INSERT INTO ChiTietGia VALUES ('CTG-035', 'GIA-001', 'TUY-014', 'GHE_MEM',   80000.00);
INSERT INTO ChiTietGia VALUES ('CTG-036', 'GIA-001', 'TUY-014', 'GIUONG_NAM',125000.00);
-- TUY-015: Vinh -> Dong Hoi (~166km)
INSERT INTO ChiTietGia VALUES ('CTG-037', 'GIA-001', 'TUY-015', 'GHE_CUNG', 100000.00);
INSERT INTO ChiTietGia VALUES ('CTG-038', 'GIA-001', 'TUY-015', 'GHE_MEM',  145000.00);
INSERT INTO ChiTietGia VALUES ('CTG-039', 'GIA-001', 'TUY-015', 'GIUONG_NAM',225000.00);
-- TUY-016: Dong Hoi -> Dong Ha (~72km)
INSERT INTO ChiTietGia VALUES ('CTG-040', 'GIA-001', 'TUY-016', 'GHE_CUNG',  50000.00);
INSERT INTO ChiTietGia VALUES ('CTG-041', 'GIA-001', 'TUY-016', 'GHE_MEM',   73000.00);
INSERT INTO ChiTietGia VALUES ('CTG-042', 'GIA-001', 'TUY-016', 'GIUONG_NAM',115000.00);
-- TUY-017: Dong Ha -> Hue (~70km)
INSERT INTO ChiTietGia VALUES ('CTG-043', 'GIA-001', 'TUY-017', 'GHE_CUNG',  50000.00);
INSERT INTO ChiTietGia VALUES ('CTG-044', 'GIA-001', 'TUY-017', 'GHE_MEM',   73000.00);
INSERT INTO ChiTietGia VALUES ('CTG-045', 'GIA-001', 'TUY-017', 'GIUONG_NAM',115000.00);
-- TUY-019: Da Nang -> Tam Ky (~72km)
INSERT INTO ChiTietGia VALUES ('CTG-046', 'GIA-001', 'TUY-019', 'GHE_CUNG',  50000.00);
INSERT INTO ChiTietGia VALUES ('CTG-047', 'GIA-001', 'TUY-019', 'GHE_MEM',   73000.00);
INSERT INTO ChiTietGia VALUES ('CTG-048', 'GIA-001', 'TUY-019', 'GIUONG_NAM',115000.00);
-- TUY-020: Tam Ky -> Quang Ngai (~40km)
INSERT INTO ChiTietGia VALUES ('CTG-049', 'GIA-001', 'TUY-020', 'GHE_CUNG',  30000.00);
INSERT INTO ChiTietGia VALUES ('CTG-050', 'GIA-001', 'TUY-020', 'GHE_MEM',   44000.00);
INSERT INTO ChiTietGia VALUES ('CTG-051', 'GIA-001', 'TUY-020', 'GIUONG_NAM', 68000.00);
-- TUY-021: Quang Ngai -> Dieu Tri (~107km)
INSERT INTO ChiTietGia VALUES ('CTG-052', 'GIA-001', 'TUY-021', 'GHE_CUNG',  70000.00);
INSERT INTO ChiTietGia VALUES ('CTG-053', 'GIA-001', 'TUY-021', 'GHE_MEM',  102000.00);
INSERT INTO ChiTietGia VALUES ('CTG-054', 'GIA-001', 'TUY-021', 'GIUONG_NAM',158000.00);
-- TUY-022: Dieu Tri -> Tuy Hoa (~85km)
INSERT INTO ChiTietGia VALUES ('CTG-055', 'GIA-001', 'TUY-022', 'GHE_CUNG',  60000.00);
INSERT INTO ChiTietGia VALUES ('CTG-056', 'GIA-001', 'TUY-022', 'GHE_MEM',   87000.00);
INSERT INTO ChiTietGia VALUES ('CTG-057', 'GIA-001', 'TUY-022', 'GIUONG_NAM',135000.00);
-- TUY-023: Tuy Hoa -> Nha Trang (~104km)
INSERT INTO ChiTietGia VALUES ('CTG-058', 'GIA-001', 'TUY-023', 'GHE_CUNG',  70000.00);
INSERT INTO ChiTietGia VALUES ('CTG-059', 'GIA-001', 'TUY-023', 'GHE_MEM',  102000.00);
INSERT INTO ChiTietGia VALUES ('CTG-060', 'GIA-001', 'TUY-023', 'GIUONG_NAM',158000.00);
-- TUY-024: Nha Trang -> Phan Rang-Thap Cham (~101km)
INSERT INTO ChiTietGia VALUES ('CTG-061', 'GIA-001', 'TUY-024', 'GHE_CUNG',  70000.00);
INSERT INTO ChiTietGia VALUES ('CTG-062', 'GIA-001', 'TUY-024', 'GHE_MEM',  102000.00);
INSERT INTO ChiTietGia VALUES ('CTG-063', 'GIA-001', 'TUY-024', 'GIUONG_NAM',158000.00);
-- TUY-025: Phan Rang -> Bien Hoa (~185km)
INSERT INTO ChiTietGia VALUES ('CTG-064', 'GIA-001', 'TUY-025', 'GHE_CUNG', 115000.00);
INSERT INTO ChiTietGia VALUES ('CTG-065', 'GIA-001', 'TUY-025', 'GHE_MEM',  167000.00);
INSERT INTO ChiTietGia VALUES ('CTG-066', 'GIA-001', 'TUY-025', 'GIUONG_NAM',260000.00);
-- TUY-026: Bien Hoa -> Sai Gon (~32km)
INSERT INTO ChiTietGia VALUES ('CTG-067', 'GIA-001', 'TUY-026', 'GHE_CUNG',  25000.00);
INSERT INTO ChiTietGia VALUES ('CTG-068', 'GIA-001', 'TUY-026', 'GHE_MEM',   36000.00);
INSERT INTO ChiTietGia VALUES ('CTG-069', 'GIA-001', 'TUY-026', 'GIUONG_NAM', 56000.00);
-- Nguoc chieu (TUY-027..041): gia tuong tu chieu di
INSERT INTO ChiTietGia VALUES ('CTG-070', 'GIA-001', 'TUY-027', 'GHE_CUNG',  55000.00);
INSERT INTO ChiTietGia VALUES ('CTG-071', 'GIA-001', 'TUY-027', 'GHE_MEM',   80000.00);
INSERT INTO ChiTietGia VALUES ('CTG-072', 'GIA-001', 'TUY-027', 'GIUONG_NAM',125000.00);
INSERT INTO ChiTietGia VALUES ('CTG-073', 'GIA-001', 'TUY-028', 'GHE_CUNG',  25000.00);
INSERT INTO ChiTietGia VALUES ('CTG-074', 'GIA-001', 'TUY-028', 'GHE_MEM',   36000.00);
INSERT INTO ChiTietGia VALUES ('CTG-075', 'GIA-001', 'TUY-028', 'GIUONG_NAM', 56000.00);
INSERT INTO ChiTietGia VALUES ('CTG-076', 'GIA-001', 'TUY-029', 'GHE_CUNG',  40000.00);
INSERT INTO ChiTietGia VALUES ('CTG-077', 'GIA-001', 'TUY-029', 'GHE_MEM',   58000.00);
INSERT INTO ChiTietGia VALUES ('CTG-078', 'GIA-001', 'TUY-029', 'GIUONG_NAM', 90000.00);
INSERT INTO ChiTietGia VALUES ('CTG-079', 'GIA-001', 'TUY-030', 'GHE_CUNG',  55000.00);
INSERT INTO ChiTietGia VALUES ('CTG-080', 'GIA-001', 'TUY-030', 'GHE_MEM',   80000.00);
INSERT INTO ChiTietGia VALUES ('CTG-081', 'GIA-001', 'TUY-030', 'GIUONG_NAM',125000.00);
INSERT INTO ChiTietGia VALUES ('CTG-082', 'GIA-001', 'TUY-031', 'GHE_CUNG', 100000.00);
INSERT INTO ChiTietGia VALUES ('CTG-083', 'GIA-001', 'TUY-031', 'GHE_MEM',  145000.00);
INSERT INTO ChiTietGia VALUES ('CTG-084', 'GIA-001', 'TUY-031', 'GIUONG_NAM',225000.00);
INSERT INTO ChiTietGia VALUES ('CTG-085', 'GIA-001', 'TUY-032', 'GHE_CUNG',  50000.00);
INSERT INTO ChiTietGia VALUES ('CTG-086', 'GIA-001', 'TUY-032', 'GHE_MEM',   73000.00);
INSERT INTO ChiTietGia VALUES ('CTG-087', 'GIA-001', 'TUY-032', 'GIUONG_NAM',115000.00);
INSERT INTO ChiTietGia VALUES ('CTG-088', 'GIA-001', 'TUY-033', 'GHE_CUNG',  50000.00);
INSERT INTO ChiTietGia VALUES ('CTG-089', 'GIA-001', 'TUY-033', 'GHE_MEM',   73000.00);
INSERT INTO ChiTietGia VALUES ('CTG-090', 'GIA-001', 'TUY-033', 'GIUONG_NAM',115000.00);
INSERT INTO ChiTietGia VALUES ('CTG-091', 'GIA-001', 'TUY-034', 'GHE_CUNG',  50000.00);
INSERT INTO ChiTietGia VALUES ('CTG-092', 'GIA-001', 'TUY-034', 'GHE_MEM',   73000.00);
INSERT INTO ChiTietGia VALUES ('CTG-093', 'GIA-001', 'TUY-034', 'GIUONG_NAM',115000.00);
INSERT INTO ChiTietGia VALUES ('CTG-094', 'GIA-001', 'TUY-035', 'GHE_CUNG',  30000.00);
INSERT INTO ChiTietGia VALUES ('CTG-095', 'GIA-001', 'TUY-035', 'GHE_MEM',   44000.00);
INSERT INTO ChiTietGia VALUES ('CTG-096', 'GIA-001', 'TUY-035', 'GIUONG_NAM', 68000.00);
INSERT INTO ChiTietGia VALUES ('CTG-097', 'GIA-001', 'TUY-036', 'GHE_CUNG',  70000.00);
INSERT INTO ChiTietGia VALUES ('CTG-098', 'GIA-001', 'TUY-036', 'GHE_MEM',  102000.00);
INSERT INTO ChiTietGia VALUES ('CTG-099', 'GIA-001', 'TUY-036', 'GIUONG_NAM',158000.00);
INSERT INTO ChiTietGia VALUES ('CTG-100', 'GIA-001', 'TUY-037', 'GHE_CUNG',  60000.00);
INSERT INTO ChiTietGia VALUES ('CTG-101', 'GIA-001', 'TUY-037', 'GHE_MEM',   87000.00);
INSERT INTO ChiTietGia VALUES ('CTG-102', 'GIA-001', 'TUY-037', 'GIUONG_NAM',135000.00);
INSERT INTO ChiTietGia VALUES ('CTG-103', 'GIA-001', 'TUY-038', 'GHE_CUNG',  70000.00);
INSERT INTO ChiTietGia VALUES ('CTG-104', 'GIA-001', 'TUY-038', 'GHE_MEM',  102000.00);
INSERT INTO ChiTietGia VALUES ('CTG-105', 'GIA-001', 'TUY-038', 'GIUONG_NAM',158000.00);
INSERT INTO ChiTietGia VALUES ('CTG-106', 'GIA-001', 'TUY-039', 'GHE_CUNG',  70000.00);
INSERT INTO ChiTietGia VALUES ('CTG-107', 'GIA-001', 'TUY-039', 'GHE_MEM',  102000.00);
INSERT INTO ChiTietGia VALUES ('CTG-108', 'GIA-001', 'TUY-039', 'GIUONG_NAM',158000.00);
INSERT INTO ChiTietGia VALUES ('CTG-109', 'GIA-001', 'TUY-040', 'GHE_CUNG', 115000.00);
INSERT INTO ChiTietGia VALUES ('CTG-110', 'GIA-001', 'TUY-040', 'GHE_MEM',  167000.00);
INSERT INTO ChiTietGia VALUES ('CTG-111', 'GIA-001', 'TUY-040', 'GIUONG_NAM',260000.00);
INSERT INTO ChiTietGia VALUES ('CTG-112', 'GIA-001', 'TUY-041', 'GHE_CUNG',  25000.00);
INSERT INTO ChiTietGia VALUES ('CTG-113', 'GIA-001', 'TUY-041', 'GHE_MEM',   36000.00);
INSERT INTO ChiTietGia VALUES ('CTG-114', 'GIA-001', 'TUY-041', 'GIUONG_NAM', 56000.00);

-- Bang gia Tet (GIA-002): tang 30%
INSERT INTO ChiTietGia VALUES ('CTG-019', 'GIA-002', 'TUY-001', 'GHE_CUNG', 234000.00);
INSERT INTO ChiTietGia VALUES ('CTG-020', 'GIA-002', 'TUY-001', 'GHE_MEM', 325000.00);
INSERT INTO ChiTietGia VALUES ('CTG-021', 'GIA-002', 'TUY-001', 'GIUONG_NAM', 520000.00);
INSERT INTO ChiTietGia VALUES ('CTG-022', 'GIA-002', 'TUY-009', 'GHE_CUNG', 1040000.00);
INSERT INTO ChiTietGia VALUES ('CTG-023', 'GIA-002', 'TUY-009', 'GHE_MEM', 1430000.00);
INSERT INTO ChiTietGia VALUES ('CTG-024', 'GIA-002', 'TUY-009', 'GIUONG_NAM', 2080000.00);

-- ==================== 13. KhuyenMai (cau truc giong Gia: ky khuyen mai) ====================
INSERT INTO KhuyenMai VALUES ('KM-001', N'Khuyến mãi đối tượng ưu tiên 2026', '2026-01-01 00:00:00', '2026-12-31 23:59:59', N'Giảm giá cho trẻ em, sinh viên, người cao tuổi cả năm 2026', 1);
INSERT INTO KhuyenMai VALUES ('KM-002', N'Khuyến mãi lễ 30/4 - 1/5', '2026-04-28 00:00:00', '2026-05-02 23:59:59', N'Chương trình giảm giá nhân dịp lễ 30/4 - 1/5', 0);
INSERT INTO KhuyenMai VALUES ('KM-003', N'Khuyến mãi mùa hè 2026', '2026-06-01 00:00:00', '2026-08-31 23:59:59', N'Giảm giá mùa hè khuyến khích du lịch bằng tàu hỏa', 0);
INSERT INTO KhuyenMai VALUES ('KM-004', N'Khuyến mãi Tết Nguyên Đán 2027', '2027-01-20 00:00:00', '2027-02-05 23:59:59', N'Giảm giá dịp Tết Nguyên Đán cho hành khách về quê', 0);
INSERT INTO KhuyenMai VALUES ('KM-005', N'Khuyến mãi Quốc khánh 2/9', '2026-08-30 00:00:00', '2026-09-03 23:59:59', N'Chương trình giảm giá nhân dịp Quốc khánh 2/9', 0);

-- ==================== 13b. ChiTietKhuyenMai ====================
-- KM-001: Uu tien - giam tuy loai ghe (ghe cung giam it, giuong nam giam nhieu hon)
-- Tre em: giam 20-25%, Sinh vien: giam 10-15%, Nguoi cao tuoi: giam 15-20%
-- Ap dung cho 10 tuyen express chinh (TUY-001 den TUY-010)
-- Uu tien - ghe cung 20%
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-001', 'KM-001', 'TUY-001', 'GHE_CUNG', N'Ưu tiên - Ghế cứng TUY-001', 0.20);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-002', 'KM-001', 'TUY-002', 'GHE_CUNG', N'Ưu tiên - Ghế cứng TUY-002', 0.20);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-003', 'KM-001', 'TUY-003', 'GHE_CUNG', N'Ưu tiên - Ghế cứng TUY-003', 0.20);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-004', 'KM-001', 'TUY-004', 'GHE_CUNG', N'Ưu tiên - Ghế cứng TUY-004', 0.20);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-005', 'KM-001', 'TUY-005', 'GHE_CUNG', N'Ưu tiên - Ghế cứng TUY-005', 0.20);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-006', 'KM-001', 'TUY-006', 'GHE_CUNG', N'Ưu tiên - Ghế cứng TUY-006', 0.20);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-007', 'KM-001', 'TUY-007', 'GHE_CUNG', N'Ưu tiên - Ghế cứng TUY-007', 0.20);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-008', 'KM-001', 'TUY-008', 'GHE_CUNG', N'Ưu tiên - Ghế cứng TUY-008', 0.20);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-009', 'KM-001', 'TUY-009', 'GHE_CUNG', N'Ưu tiên - Ghế cứng TUY-009', 0.20);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-010', 'KM-001', 'TUY-010', 'GHE_CUNG', N'Ưu tiên - Ghế cứng TUY-010', 0.20);
-- Uu tien - ghe mem 22%
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-011', 'KM-001', 'TUY-001', 'GHE_MEM', N'Ưu tiên - Ghế mềm TUY-001', 0.22);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-012', 'KM-001', 'TUY-002', 'GHE_MEM', N'Ưu tiên - Ghế mềm TUY-002', 0.22);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-013', 'KM-001', 'TUY-003', 'GHE_MEM', N'Ưu tiên - Ghế mềm TUY-003', 0.22);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-014', 'KM-001', 'TUY-004', 'GHE_MEM', N'Ưu tiên - Ghế mềm TUY-004', 0.22);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-015', 'KM-001', 'TUY-005', 'GHE_MEM', N'Ưu tiên - Ghế mềm TUY-005', 0.22);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-016', 'KM-001', 'TUY-006', 'GHE_MEM', N'Ưu tiên - Ghế mềm TUY-006', 0.22);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-017', 'KM-001', 'TUY-007', 'GHE_MEM', N'Ưu tiên - Ghế mềm TUY-007', 0.22);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-018', 'KM-001', 'TUY-008', 'GHE_MEM', N'Ưu tiên - Ghế mềm TUY-008', 0.22);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-019', 'KM-001', 'TUY-009', 'GHE_MEM', N'Ưu tiên - Ghế mềm TUY-009', 0.22);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-020', 'KM-001', 'TUY-010', 'GHE_MEM', N'Ưu tiên - Ghế mềm TUY-010', 0.22);
-- Uu tien - giuong nam 25%
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-021', 'KM-001', 'TUY-001', 'GIUONG_NAM', N'Ưu tiên - Giường nằm TUY-001', 0.25);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-022', 'KM-001', 'TUY-002', 'GIUONG_NAM', N'Ưu tiên - Giường nằm TUY-002', 0.25);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-023', 'KM-001', 'TUY-003', 'GIUONG_NAM', N'Ưu tiên - Giường nằm TUY-003', 0.25);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-024', 'KM-001', 'TUY-004', 'GIUONG_NAM', N'Ưu tiên - Giường nằm TUY-004', 0.25);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-025', 'KM-001', 'TUY-005', 'GIUONG_NAM', N'Ưu tiên - Giường nằm TUY-005', 0.25);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-026', 'KM-001', 'TUY-006', 'GIUONG_NAM', N'Ưu tiên - Giường nằm TUY-006', 0.25);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-027', 'KM-001', 'TUY-007', 'GIUONG_NAM', N'Ưu tiên - Giường nằm TUY-007', 0.25);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-028', 'KM-001', 'TUY-008', 'GIUONG_NAM', N'Ưu tiên - Giường nằm TUY-008', 0.25);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-029', 'KM-001', 'TUY-009', 'GIUONG_NAM', N'Ưu tiên - Giường nằm TUY-009', 0.25);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-030', 'KM-001', 'TUY-010', 'GIUONG_NAM', N'Ưu tiên - Giường nằm TUY-010', 0.25);

-- KM-002: Le 30/4 - giam 10% dong deu cho cac tuyen chinh
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-031', 'KM-002', 'TUY-001', 'GHE_CUNG', N'Lễ 30/4 - Ghế cứng TUY-001', 0.10);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-032', 'KM-002', 'TUY-001', 'GHE_MEM', N'Lễ 30/4 - Ghế mềm TUY-001', 0.10);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-033', 'KM-002', 'TUY-001', 'GIUONG_NAM', N'Lễ 30/4 - Giường nằm TUY-001', 0.10);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-034', 'KM-002', 'TUY-002', 'GHE_CUNG', N'Lễ 30/4 - Ghế cứng TUY-002', 0.10);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-035', 'KM-002', 'TUY-002', 'GHE_MEM', N'Lễ 30/4 - Ghế mềm TUY-002', 0.10);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-036', 'KM-002', 'TUY-002', 'GIUONG_NAM', N'Lễ 30/4 - Giường nằm TUY-002', 0.10);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-037', 'KM-002', 'TUY-003', 'GHE_CUNG', N'Lễ 30/4 - Ghế cứng TUY-003', 0.10);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-038', 'KM-002', 'TUY-003', 'GHE_MEM', N'Lễ 30/4 - Ghế mềm TUY-003', 0.10);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-039', 'KM-002', 'TUY-003', 'GIUONG_NAM', N'Lễ 30/4 - Giường nằm TUY-003', 0.10);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-040', 'KM-002', 'TUY-004', 'GHE_CUNG', N'Lễ 30/4 - Ghế cứng TUY-004', 0.10);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-041', 'KM-002', 'TUY-004', 'GHE_MEM', N'Lễ 30/4 - Ghế mềm TUY-004', 0.10);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-042', 'KM-002', 'TUY-004', 'GIUONG_NAM', N'Lễ 30/4 - Giường nằm TUY-004', 0.10);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-043', 'KM-002', 'TUY-009', 'GHE_CUNG', N'Lễ 30/4 - Ghế cứng TUY-009', 0.12);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-044', 'KM-002', 'TUY-009', 'GHE_MEM', N'Lễ 30/4 - Ghế mềm TUY-009', 0.12);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-045', 'KM-002', 'TUY-009', 'GIUONG_NAM', N'Lễ 30/4 - Giường nằm TUY-009', 0.12);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-046', 'KM-002', 'TUY-010', 'GHE_CUNG', N'Lễ 30/4 - Ghế cứng TUY-010', 0.12);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-047', 'KM-002', 'TUY-010', 'GHE_MEM', N'Lễ 30/4 - Ghế mềm TUY-010', 0.12);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-048', 'KM-002', 'TUY-010', 'GIUONG_NAM', N'Lễ 30/4 - Giường nằm TUY-010', 0.12);

-- KM-003: Mua he - giam nhieu hon cho giuong nam de khuyen khich du lich
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-049', 'KM-003', 'TUY-001', 'GHE_CUNG', N'Mùa hè - Ghế cứng TUY-001', 0.05);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-050', 'KM-003', 'TUY-001', 'GHE_MEM', N'Mùa hè - Ghế mềm TUY-001', 0.07);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-051', 'KM-003', 'TUY-001', 'GIUONG_NAM', N'Mùa hè - Giường nằm TUY-001', 0.10);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-052', 'KM-003', 'TUY-002', 'GHE_CUNG', N'Mùa hè - Ghế cứng TUY-002', 0.05);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-053', 'KM-003', 'TUY-002', 'GHE_MEM', N'Mùa hè - Ghế mềm TUY-002', 0.07);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-054', 'KM-003', 'TUY-002', 'GIUONG_NAM', N'Mùa hè - Giường nằm TUY-002', 0.10);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-055', 'KM-003', 'TUY-003', 'GHE_CUNG', N'Mùa hè - Ghế cứng TUY-003', 0.08);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-056', 'KM-003', 'TUY-003', 'GHE_MEM', N'Mùa hè - Ghế mềm TUY-003', 0.10);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-057', 'KM-003', 'TUY-003', 'GIUONG_NAM', N'Mùa hè - Giường nằm TUY-003', 0.15);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-058', 'KM-003', 'TUY-004', 'GHE_CUNG', N'Mùa hè - Ghế cứng TUY-004', 0.05);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-059', 'KM-003', 'TUY-004', 'GHE_MEM', N'Mùa hè - Ghế mềm TUY-004', 0.07);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-060', 'KM-003', 'TUY-004', 'GIUONG_NAM', N'Mùa hè - Giường nằm TUY-004', 0.10);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-061', 'KM-003', 'TUY-005', 'GHE_CUNG', N'Mùa hè - Ghế cứng TUY-005', 0.05);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-062', 'KM-003', 'TUY-005', 'GHE_MEM', N'Mùa hè - Ghế mềm TUY-005', 0.07);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-063', 'KM-003', 'TUY-005', 'GIUONG_NAM', N'Mùa hè - Giường nằm TUY-005', 0.10);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-064', 'KM-003', 'TUY-009', 'GHE_CUNG', N'Mùa hè - Ghế cứng TUY-009', 0.08);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-065', 'KM-003', 'TUY-009', 'GHE_MEM', N'Mùa hè - Ghế mềm TUY-009', 0.10);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-066', 'KM-003', 'TUY-009', 'GIUONG_NAM', N'Mùa hè - Giường nằm TUY-009', 0.15);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-067', 'KM-003', 'TUY-010', 'GHE_CUNG', N'Mùa hè - Ghế cứng TUY-010', 0.08);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-068', 'KM-003', 'TUY-010', 'GHE_MEM', N'Mùa hè - Ghế mềm TUY-010', 0.10);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-069', 'KM-003', 'TUY-010', 'GIUONG_NAM', N'Mùa hè - Giường nằm TUY-010', 0.15);

-- KM-004: Tet 2027 - giam manh cho tuyen xuyen Viet
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-070', 'KM-004', 'TUY-009', 'GHE_CUNG', N'Tết 2027 - Ghế cứng TUY-009', 0.15);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-071', 'KM-004', 'TUY-009', 'GHE_MEM', N'Tết 2027 - Ghế mềm TUY-009', 0.15);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-072', 'KM-004', 'TUY-009', 'GIUONG_NAM', N'Tết 2027 - Giường nằm TUY-009', 0.20);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-073', 'KM-004', 'TUY-010', 'GHE_CUNG', N'Tết 2027 - Ghế cứng TUY-010', 0.15);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-074', 'KM-004', 'TUY-010', 'GHE_MEM', N'Tết 2027 - Ghế mềm TUY-010', 0.15);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-075', 'KM-004', 'TUY-010', 'GIUONG_NAM', N'Tết 2027 - Giường nằm TUY-010', 0.20);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-076', 'KM-004', 'TUY-001', 'GHE_CUNG', N'Tết 2027 - Ghế cứng TUY-001', 0.10);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-077', 'KM-004', 'TUY-001', 'GHE_MEM', N'Tết 2027 - Ghế mềm TUY-001', 0.10);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-078', 'KM-004', 'TUY-001', 'GIUONG_NAM', N'Tết 2027 - Giường nằm TUY-001', 0.12);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-079', 'KM-004', 'TUY-004', 'GHE_CUNG', N'Tết 2027 - Ghế cứng TUY-004', 0.10);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-080', 'KM-004', 'TUY-004', 'GHE_MEM', N'Tết 2027 - Ghế mềm TUY-004', 0.10);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-081', 'KM-004', 'TUY-004', 'GIUONG_NAM', N'Tết 2027 - Giường nằm TUY-004', 0.12);

-- KM-005: Quoc khanh 2/9 - giam 8% cho tat ca tuyen chinh
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-082', 'KM-005', 'TUY-001', 'GHE_CUNG', N'Quốc khánh - Ghế cứng TUY-001', 0.08);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-083', 'KM-005', 'TUY-001', 'GHE_MEM', N'Quốc khánh - Ghế mềm TUY-001', 0.08);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-084', 'KM-005', 'TUY-001', 'GIUONG_NAM', N'Quốc khánh - Giường nằm TUY-001', 0.08);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-085', 'KM-005', 'TUY-002', 'GHE_CUNG', N'Quốc khánh - Ghế cứng TUY-002', 0.08);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-086', 'KM-005', 'TUY-002', 'GHE_MEM', N'Quốc khánh - Ghế mềm TUY-002', 0.08);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-087', 'KM-005', 'TUY-002', 'GIUONG_NAM', N'Quốc khánh - Giường nằm TUY-002', 0.08);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-088', 'KM-005', 'TUY-003', 'GHE_CUNG', N'Quốc khánh - Ghế cứng TUY-003', 0.08);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-089', 'KM-005', 'TUY-003', 'GHE_MEM', N'Quốc khánh - Ghế mềm TUY-003', 0.08);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-090', 'KM-005', 'TUY-003', 'GIUONG_NAM', N'Quốc khánh - Giường nằm TUY-003', 0.08);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-091', 'KM-005', 'TUY-009', 'GHE_CUNG', N'Quốc khánh - Ghế cứng TUY-009', 0.10);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-092', 'KM-005', 'TUY-009', 'GHE_MEM', N'Quốc khánh - Ghế mềm TUY-009', 0.10);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-093', 'KM-005', 'TUY-009', 'GIUONG_NAM', N'Quốc khánh - Giường nằm TUY-009', 0.12);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-094', 'KM-005', 'TUY-010', 'GHE_CUNG', N'Quốc khánh - Ghế cứng TUY-010', 0.10);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-095', 'KM-005', 'TUY-010', 'GHE_MEM', N'Quốc khánh - Ghế mềm TUY-010', 0.10);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM-096', 'KM-005', 'TUY-010', 'GIUONG_NAM', N'Quốc khánh - Giường nằm TUY-010', 0.12);

-- ==================== 14. Ve (CAU TRUC MOI: maLich + maGhe thay vi maHoaDon) ====================
-- HD-10042026-001: Pham Minh Tuan mua 2 ve ghe mem LCH-001
INSERT INTO Ve VALUES ('VE-001', 'LCH-001', 'G-003-01', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE-002', 'LCH-001', 'G-003-02', 'DA_BAN', NULL, NULL);
-- HD-10042026-002: Hoang Duc Manh mua 1 ve giuong nam LCH-003
INSERT INTO Ve VALUES ('VE-003', 'LCH-003', 'G-001-05', 'DA_BAN', NULL, NULL);
-- HD-10042026-003: Nguyen Thi Hoa mua 1 ve ghe cung LCH-005 (sinh vien)
INSERT INTO Ve VALUES ('VE-004', 'LCH-005', 'G-003-05', 'DA_BAN', NULL, NULL);
-- HD-11042026-001: Tran Van Duc mua 2 cap ve noi chuyen HN->Vinh->Hue
--   Cap 1: cho Tran Van Duc
INSERT INTO Ve VALUES ('VE-005', 'LCH-002', 'G-001-01', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE-006', 'LCH-004', 'G-002-01', 'DA_BAN', NULL, NULL);
--   Cap 2: cho Tran Thi Mai
INSERT INTO Ve VALUES ('VE-007', 'LCH-002', 'G-001-02', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE-008', 'LCH-004', 'G-002-02', 'DA_BAN', NULL, NULL);
-- HD-12042026-001: Ly Van Hung mua 1 ve ghe mem LCH-006, sau do huy
INSERT INTO Ve VALUES ('VE-009', 'LCH-006', 'G-003-01', 'DA_HUY', N'Hành khách thay đổi kế hoạch', '2026-04-10 10:30:00');

-- ==================== 15. HoaDon (tach KhachHang sang HoaDonKhachHang) ====================
INSERT INTO HoaDon VALUES ('HD-10042026-001', 'NV-0001', '2026-04-08 09:15:00');
INSERT INTO HoaDon VALUES ('HD-10042026-002', 'NV-0002', '2026-04-08 10:30:00');
INSERT INTO HoaDon VALUES ('HD-10042026-003', 'NV-0001', '2026-04-09 14:00:00');
INSERT INTO HoaDon VALUES ('HD-11042026-001', 'NV-0002', '2026-04-09 16:00:00');
INSERT INTO HoaDon VALUES ('HD-12042026-001', 'NV-0001', '2026-04-10 08:00:00');

-- ==================== 15b. HoaDonKhachHang (junction) ====================
INSERT INTO HoaDonKhachHang VALUES ('HDKH-001', 'HD-10042026-001', 'KH-0001');
INSERT INTO HoaDonKhachHang VALUES ('HDKH-002', 'HD-10042026-002', 'KH-0002');
INSERT INTO HoaDonKhachHang VALUES ('HDKH-003', 'HD-10042026-003', 'KH-0003');
INSERT INTO HoaDonKhachHang VALUES ('HDKH-004', 'HD-11042026-001', 'KH-0004');
-- HD-11042026-001 demo da-khach (Tran Van Duc + Tran Thi Mai)
INSERT INTO HoaDonKhachHang VALUES ('HDKH-005', 'HD-11042026-001', 'KH-0003');
INSERT INTO HoaDonKhachHang VALUES ('HDKH-006', 'HD-12042026-001', 'KH-0005');

-- ==================== 16. ChiTietHoaDon (BANG MOI thay the ChiTietVe) ====================
-- HD-10042026-001: 2 ve ghe mem TUY-001 (250k/ve)
INSERT INTO ChiTietHoaDon VALUES ('CTHD-001', 'HD-10042026-001', 'VE-001', 250000.00);
INSERT INTO ChiTietHoaDon VALUES ('CTHD-002', 'HD-10042026-001', 'VE-002', 250000.00);
-- HD-10042026-002: 1 ve giuong nam TUY-002 (450k)
INSERT INTO ChiTietHoaDon VALUES ('CTHD-003', 'HD-10042026-002', 'VE-003', 450000.00);
-- HD-10042026-003: 1 ve ghe cung TUY-003, sinh vien giam 15% (80k * 0.85 = 68k)
INSERT INTO ChiTietHoaDon VALUES ('CTHD-004', 'HD-10042026-003', 'VE-004', 68000.00);
-- HD-11042026-001: 4 ve noi chuyen HN->Vinh (180k ghe cung) + Vinh->Hue (200k ghe cung)
INSERT INTO ChiTietHoaDon VALUES ('CTHD-005', 'HD-11042026-001', 'VE-005', 180000.00);
INSERT INTO ChiTietHoaDon VALUES ('CTHD-006', 'HD-11042026-001', 'VE-006', 200000.00);
INSERT INTO ChiTietHoaDon VALUES ('CTHD-007', 'HD-11042026-001', 'VE-007', 180000.00);
INSERT INTO ChiTietHoaDon VALUES ('CTHD-008', 'HD-11042026-001', 'VE-008', 200000.00);
-- HD-12042026-001: 1 ve ghe mem TUY-004 (600k), da huy
INSERT INTO ChiTietHoaDon VALUES ('CTHD-009', 'HD-12042026-001', 'VE-009', 600000.00);

-- ==================== 17. ApDungKM (FK -> ChiTietHoaDon + ChiTietKhuyenMai) ====================
-- CTHD-004: ve ghe cung TUY-003 (Hue->Da Nang), sinh vien -> ap dung CTKM-003 (KM-001, TUY-003, GHE_CUNG, 20%)
INSERT INTO ApDungKM VALUES ('ADKM-001', 'CTHD-004', 'CTKM-003');

-- ==================== 18. GiuCho ====================
INSERT INTO GiuCho VALUES ('GC-001', 'NV-0001', 'LCH-008', 'G-003-06', '2026-04-14 15:05:00');
INSERT INTO GiuCho VALUES ('GC-002', 'NV-0001', 'LCH-008', 'G-003-07', '2026-04-14 15:05:00');

-- ============================================================
-- DU LIEU BO SUNG - TANG PHONG PHU CHO CAC BANG CON IT DATA
-- ============================================================

-- ==================== KhachHang (them 15 khach hang) ====================
INSERT INTO KhachHang (maKhachHang, hoTen, cccd, soDienThoai, email, diaChiThuongTru, diaChiTamTru, ngaySinh, gioiTinh, quocTich) VALUES
('KH-0006', N'Nguyễn Thị Lan', '001086006789', '0901112223', 'lan.nguyenthi@email.com', N'33 Điện Biên Phủ, TP Huế, Thừa Thiên Huế', NULL, '1997-06-20', 'NU', N'Việt Nam');

INSERT INTO KhachHang (maKhachHang, hoTen, cccd, soDienThoai, email, diaChiThuongTru, diaChiTamTru, ngaySinh, gioiTinh, quocTich) VALUES
('KH-0007', N'Trần Minh Khoa', '079086007890', '0912223334', 'khoa.tranminh@email.com', N'72 Nguyễn Văn Linh, Quận 7, TP.HCM', NULL, '1991-01-14', 'NAM', N'Việt Nam');

INSERT INTO KhachHang (maKhachHang, hoTen, cccd, soDienThoai, email, diaChiThuongTru, diaChiTamTru, ngaySinh, gioiTinh, quocTich) VALUES
('KH-0008', N'Lê Thị Thu Hà', '046086008901', '0923334445', 'ha.lethithu@email.com', N'19 Lê Thánh Tôn, TP Huế, Thừa Thiên Huế', NULL, '1999-09-28', 'NU', N'Việt Nam');

INSERT INTO KhachHang (maKhachHang, hoTen, cccd, soDienThoai, email, diaChiThuongTru, diaChiTamTru, ngaySinh, gioiTinh, quocTich) VALUES
('KH-0009', N'Phạm Văn Bình', '038086009012', '0934445556', 'binh.phamvan@email.com', N'60 Nguyễn Du, TP Vinh, Nghệ An', NULL, '1985-04-05', 'NAM', N'Việt Nam');

INSERT INTO KhachHang (maKhachHang, hoTen, cccd, soDienThoai, email, diaChiThuongTru, diaChiTamTru, ngaySinh, gioiTinh, quocTich) VALUES
('KH-0010', N'Hoàng Thị Nga', '048086010123', '0945556667', 'nga.hoangthi@email.com', N'45 Ông Ích Khiêm, Thanh Khê, Đà Nẵng', NULL, '1994-07-11', 'NU', N'Việt Nam');

INSERT INTO KhachHang (maKhachHang, hoTen, cccd, soDienThoai, email, diaChiThuongTru, diaChiTamTru, ngaySinh, gioiTinh, quocTich) VALUES
('KH-0011', N'Vũ Đức Mạnh', '001086011234', '0956667778', 'manh.vuduc@email.com', N'8 Phạm Ngọc Thạch, Đống Đa, Hà Nội', N'55 Võ Thị Sáu, Quận 1, TP.HCM', '1993-02-19', 'NAM', N'Việt Nam');

INSERT INTO KhachHang (maKhachHang, hoTen, cccd, soDienThoai, email, diaChiThuongTru, diaChiTamTru, ngaySinh, gioiTinh, quocTich) VALUES
('KH-0012', N'Đặng Thị Thúy', '079086012345', '0967778889', 'thuy.dangthi@email.com', N'16 Đinh Tiên Hoàng, Quận 1, TP.HCM', NULL, '1996-10-30', 'NU', N'Việt Nam');

INSERT INTO KhachHang (maKhachHang, hoTen, cccd, soDienThoai, email, diaChiThuongTru, diaChiTamTru, ngaySinh, gioiTinh, quocTich) VALUES
('KH-0013', N'Bùi Văn Thành', '048086013456', '0978889990', 'thanh.buivan@email.com', N'30 Lê Lợi, Hải Châu, Đà Nẵng', NULL, '1990-08-08', 'NAM', N'Việt Nam');

INSERT INTO KhachHang (maKhachHang, hoTen, cccd, soDienThoai, email, diaChiThuongTru, diaChiTamTru, ngaySinh, gioiTinh, quocTich) VALUES
('KH-0014', N'Ngô Thị Hương', '079086014567', '0989990001', 'huong.ngothi@email.com', N'88 Cách Mạng Tháng 8, Quận 3, TP.HCM', NULL, '1998-03-25', 'NU', N'Việt Nam');

INSERT INTO KhachHang (maKhachHang, hoTen, cccd, soDienThoai, email, diaChiThuongTru, diaChiTamTru, ngaySinh, gioiTinh, quocTich) VALUES
('KH-0015', N'Đinh Minh Hiếu', '001086015678', '0990001112', 'hieu.dinhminh@email.com', N'14 Kim Liên, Đống Đa, Hà Nội', NULL, '1992-12-01', 'NAM', N'Việt Nam');

INSERT INTO KhachHang (maKhachHang, hoTen, cccd, soDienThoai, email, diaChiThuongTru, diaChiTamTru, ngaySinh, gioiTinh, quocTich) VALUES
('KH-0016', N'Lương Thị Ánh', '038086016789', '0901221332', 'anh.luongthi@email.com', N'77 Đinh Công Tráng, TP Vinh, Nghệ An', NULL, '1995-05-17', 'NU', N'Việt Nam');

INSERT INTO KhachHang (maKhachHang, hoTen, cccd, soDienThoai, email, diaChiThuongTru, diaChiTamTru, ngaySinh, gioiTinh, quocTich) VALUES
('KH-0017', N'Tạ Văn Phúc', '046086017890', '0912332443', 'phuc.tavan@email.com', N'22 Hùng Vương, TP Huế, Thừa Thiên Huế', NULL, '1987-07-09', 'NAM', N'Việt Nam');

INSERT INTO KhachHang (maKhachHang, hoTen, cccd, soDienThoai, email, diaChiThuongTru, diaChiTamTru, ngaySinh, gioiTinh, quocTich) VALUES
('KH-0018', N'Kiều Thị Bích', '048086018901', '0923443554', 'bich.kieuthi@email.com', N'63 Trần Quý Cáp, Hải Châu, Đà Nẵng', NULL, '1999-11-12', 'NU', N'Việt Nam');

INSERT INTO KhachHang (maKhachHang, hoTen, cccd, soDienThoai, email, diaChiThuongTru, diaChiTamTru, ngaySinh, gioiTinh, quocTich) VALUES
('KH-0019', N'Châu Minh Tuấn', '079086019012', '0934554665', 'tuan.chau@email.com', N'40 Đinh Tiên Hoàng, Bình Thạnh, TP.HCM', NULL, '1991-06-22', 'NAM', N'Việt Nam');

INSERT INTO KhachHang (maKhachHang, hoTen, cccd, soDienThoai, email, diaChiThuongTru, diaChiTamTru, ngaySinh, gioiTinh, quocTich) VALUES
('KH-0020', N'Dương Văn Long', '038086020123', '0945665776', 'long.duongvan@email.com', N'25 Nguyễn Sỹ Sách, TP Vinh, Nghệ An', N'10 Lê Duẩn, Hoàn Kiếm, Hà Nội', '1994-01-03', 'NAM', N'Việt Nam');

-- ==================== DoanTau (them 5 doan tau) ====================
-- SE3/SE4: song hanh voi SE1/SE2 tren tuyen xuyen Viet
-- SE5/SE8: Hanoi <-> Da Nang (ngan hon)
-- TN1: Thong Nhat 1 - chay cham hon SE, dung nhieu ga hon
INSERT INTO DoanTau VALUES ('DT-004', N'SE3 (Hà Nội → Sài Gòn)', 'DM-004');
INSERT INTO DoanTau VALUES ('DT-005', N'SE4 (Sài Gòn → Hà Nội)', 'DM-005');
INSERT INTO DoanTau VALUES ('DT-006', N'SE5 (Hà Nội → Đà Nẵng)', 'DM-006');
INSERT INTO DoanTau VALUES ('DT-007', N'SE8 (Đà Nẵng → Hà Nội)', 'DM-007');
INSERT INTO DoanTau VALUES ('DT-008', N'TN1 (Hà Nội → Sài Gòn)', 'DM-008');

-- ==================== ChiTietDoanTau (thanh phan toa cho doan tau moi) ====================
-- DT-004 (SE3): toa cung(1) + toa mem(2) + giuong nam(3)
INSERT INTO ChiTietDoanTau VALUES ('CTDT-009', 'DT-004', 'TOA-003', 1);
INSERT INTO ChiTietDoanTau VALUES ('CTDT-010', 'DT-004', 'TOA-002', 2);
INSERT INTO ChiTietDoanTau VALUES ('CTDT-011', 'DT-004', 'TOA-001', 3);
-- DT-005 (SE4): toa cung(1) + toa mem(2) + giuong nam(3)
INSERT INTO ChiTietDoanTau VALUES ('CTDT-012', 'DT-005', 'TOA-003', 1);
INSERT INTO ChiTietDoanTau VALUES ('CTDT-013', 'DT-005', 'TOA-002', 2);
INSERT INTO ChiTietDoanTau VALUES ('CTDT-014', 'DT-005', 'TOA-001', 3);
-- DT-006 (SE5): tau ngan, chi co toa cung + toa mem (khong co giuong nam)
INSERT INTO ChiTietDoanTau VALUES ('CTDT-015', 'DT-006', 'TOA-003', 1);
INSERT INTO ChiTietDoanTau VALUES ('CTDT-016', 'DT-006', 'TOA-002', 2);
-- DT-007 (SE8): tuong tu SE5, tau ngan
INSERT INTO ChiTietDoanTau VALUES ('CTDT-017', 'DT-007', 'TOA-003', 1);
INSERT INTO ChiTietDoanTau VALUES ('CTDT-018', 'DT-007', 'TOA-002', 2);
-- DT-008 (TN1): day du 3 loai toa
INSERT INTO ChiTietDoanTau VALUES ('CTDT-019', 'DT-008', 'TOA-003', 1);
INSERT INTO ChiTietDoanTau VALUES ('CTDT-020', 'DT-008', 'TOA-002', 2);
INSERT INTO ChiTietDoanTau VALUES ('CTDT-021', 'DT-008', 'TOA-001', 3);

-- ==================== ChiTietGia (bo sung gia cho cac tuyen chua co) ====================
-- TUY-006: Da Nang -> Hue (~100km) - cung cu ly TUY-003
INSERT INTO ChiTietGia VALUES ('CTG-115', 'GIA-001', 'TUY-006', 'GHE_CUNG',    80000.00);
INSERT INTO ChiTietGia VALUES ('CTG-116', 'GIA-001', 'TUY-006', 'GHE_MEM',    120000.00);
INSERT INTO ChiTietGia VALUES ('CTG-117', 'GIA-001', 'TUY-006', 'GIUONG_NAM', 200000.00);
-- TUY-007: Hue -> Vinh (~368km) - cung cu ly TUY-002
INSERT INTO ChiTietGia VALUES ('CTG-118', 'GIA-001', 'TUY-007', 'GHE_CUNG',  200000.00);
INSERT INTO ChiTietGia VALUES ('CTG-119', 'GIA-001', 'TUY-007', 'GHE_MEM',   280000.00);
INSERT INTO ChiTietGia VALUES ('CTG-120', 'GIA-001', 'TUY-007', 'GIUONG_NAM',450000.00);
-- TUY-008: Vinh -> Ha Noi (~319km) - cung cu ly TUY-001
INSERT INTO ChiTietGia VALUES ('CTG-121', 'GIA-001', 'TUY-008', 'GHE_CUNG',  180000.00);
INSERT INTO ChiTietGia VALUES ('CTG-122', 'GIA-001', 'TUY-008', 'GHE_MEM',   250000.00);
INSERT INTO ChiTietGia VALUES ('CTG-123', 'GIA-001', 'TUY-008', 'GIUONG_NAM',400000.00);
-- TUY-010: Sai Gon -> Ha Noi (~1726km) - cung cu ly TUY-009
INSERT INTO ChiTietGia VALUES ('CTG-124', 'GIA-001', 'TUY-010', 'GHE_CUNG',   800000.00);
INSERT INTO ChiTietGia VALUES ('CTG-125', 'GIA-001', 'TUY-010', 'GHE_MEM',   1100000.00);
INSERT INTO ChiTietGia VALUES ('CTG-126', 'GIA-001', 'TUY-010', 'GIUONG_NAM',1600000.00);

-- ==================== Lich (them 22 lich chay, LCH-009 den LCH-030) ====================
-- Lich thuong ngay (16-25/4/2026)
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH-009', 'TUY-001', 'DT-004', '2026-04-16 06:00:00',  330);  -- SE3 HN->Vinh
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH-010', 'TUY-008', 'DT-005', '2026-04-16 14:00:00',  330);  -- SE4 Vinh->HN
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH-011', 'TUY-002', 'DT-004', '2026-04-16 12:00:00',  360);  -- SE3 Vinh->Hue
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH-012', 'TUY-003', 'DT-006', '2026-04-16 09:00:00',  150);  -- SE5 Hue->DN
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH-013', 'TUY-006', 'DT-007', '2026-04-16 08:00:00',  150);  -- SE8 DN->Hue
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH-014', 'TUY-009', 'DT-004', '2026-04-17 19:00:00', 1980);  -- SE3 HN->SG xuyen Viet
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH-015', 'TUY-010', 'DT-005', '2026-04-18 07:00:00', 1980);  -- SE4 SG->HN xuyen Viet
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH-016', 'TUY-009', 'DT-008', '2026-04-19 20:00:00', 2160);  -- TN1 HN->SG (cham hon SE)
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH-017', 'TUY-001', 'DT-002', '2026-04-20 06:00:00',  330);  -- SE2 HN->Vinh
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH-018', 'TUY-004', 'DT-001', '2026-04-20 19:00:00', 1020);  -- SE1 DN->SG
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH-019', 'TUY-005', 'DT-002', '2026-04-21 07:00:00', 1020);  -- SE2 SG->DN
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH-020', 'TUY-002', 'DT-001', '2026-04-22 08:00:00',  360);  -- SE1 Vinh->Hue
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH-021', 'TUY-007', 'DT-002', '2026-04-22 10:00:00',  360);  -- SE2 Hue->Vinh
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH-022', 'TUY-003', 'DT-003', '2026-04-23 08:00:00',  150);  -- SE7 Hue->DN
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH-023', 'TUY-001', 'DT-004', '2026-04-25 06:00:00',  330);  -- SE3 HN->Vinh
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH-024', 'TUY-002', 'DT-004', '2026-04-25 12:00:00',  360);  -- SE3 Vinh->Hue
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH-025', 'TUY-003', 'DT-006', '2026-04-25 19:00:00',  150);  -- SE5 Hue->DN
-- Lich dip Le 30/4 - 1/5 (nhu cau cao, gia ap dung KM-002)
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH-026', 'TUY-001', 'DT-004', '2026-04-28 06:00:00',  330);  -- SE3 HN->Vinh (le)
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH-027', 'TUY-009', 'DT-004', '2026-04-28 19:00:00', 1980);  -- SE3 HN->SG (le)
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH-028', 'TUY-010', 'DT-005', '2026-04-30 07:00:00', 1980);  -- SE4 SG->HN (le)
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH-029', 'TUY-004', 'DT-005', '2026-04-29 20:00:00', 1020);  -- SE4 DN->SG
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH-030', 'TUY-005', 'DT-004', '2026-05-01 09:00:00', 1020);  -- SE3 SG->DN (sau le)

-- ==================== Ve (them 52 ve moi, VE-010 den VE-061) ====================
-- LCH-009 (SE3 HN->Vinh 16/4, DT-004 co TOA-001+TOA-002+TOA-003)
INSERT INTO Ve VALUES ('VE-010', 'LCH-009', 'G-003-10', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE-011', 'LCH-009', 'G-003-11', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE-012', 'LCH-009', 'G-001-10', 'DA_BAN', NULL, NULL);
-- LCH-011 (SE3 Vinh->Hue 16/4, DT-004)
INSERT INTO Ve VALUES ('VE-013', 'LCH-011', 'G-002-10', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE-014', 'LCH-011', 'G-002-11', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE-015', 'LCH-011', 'G-002-12', 'DA_BAN', NULL, NULL);
-- LCH-012 (SE5 Hue->DN 16/4, DT-006 chi co TOA-002+TOA-003)
INSERT INTO Ve VALUES ('VE-016', 'LCH-012', 'G-003-20', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE-017', 'LCH-012', 'G-003-21', 'DA_BAN', NULL, NULL);
-- LCH-013 (SE8 DN->Hue 16/4, DT-007 chi co TOA-002+TOA-003)
INSERT INTO Ve VALUES ('VE-018', 'LCH-013', 'G-002-15', 'DA_BAN', NULL, NULL);
-- LCH-014 (SE3 HN->SG xuyen Viet 17/4, DT-004)
INSERT INTO Ve VALUES ('VE-019', 'LCH-014', 'G-001-05', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE-020', 'LCH-014', 'G-001-06', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE-021', 'LCH-014', 'G-001-07', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE-022', 'LCH-014', 'G-001-08', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE-023', 'LCH-014', 'G-002-20', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE-024', 'LCH-014', 'G-002-21', 'DA_BAN', NULL, NULL);
-- LCH-015 (SE4 SG->HN 18/4, DT-005)
INSERT INTO Ve VALUES ('VE-025', 'LCH-015', 'G-003-10', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE-026', 'LCH-015', 'G-003-11', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE-027', 'LCH-015', 'G-003-12', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE-028', 'LCH-015', 'G-001-15', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE-029', 'LCH-015', 'G-001-16', 'DA_BAN', NULL, NULL);
-- LCH-016 (TN1 HN->SG 19/4, DT-008)
INSERT INTO Ve VALUES ('VE-030', 'LCH-016', 'G-003-05', 'DA_BAN', NULL, NULL);
-- LCH-017 (SE2 HN->Vinh 20/4, DT-002)
INSERT INTO Ve VALUES ('VE-031', 'LCH-017', 'G-002-05', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE-032', 'LCH-017', 'G-002-06', 'DA_BAN', NULL, NULL);
-- LCH-018 (SE1 DN->SG 20/4, DT-001)
INSERT INTO Ve VALUES ('VE-033', 'LCH-018', 'G-003-15', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE-034', 'LCH-018', 'G-003-16', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE-035', 'LCH-018', 'G-003-17', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE-036', 'LCH-018', 'G-003-18', 'DA_BAN', NULL, NULL);
-- LCH-019 (SE2 SG->DN 21/4, DT-002) - 1 ve bi huy
INSERT INTO Ve VALUES ('VE-037', 'LCH-019', 'G-001-20', 'DA_HUY', N'Khách thay đổi kế hoạch du lịch', '2026-04-20 14:00:00');
-- LCH-020 (SE1 Vinh->Hue 22/4, DT-001)
INSERT INTO Ve VALUES ('VE-038', 'LCH-020', 'G-003-22', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE-039', 'LCH-020', 'G-003-23', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE-040', 'LCH-020', 'G-003-24', 'DA_BAN', NULL, NULL);
-- LCH-021 (SE2 Hue->Vinh 22/4, DT-002)
INSERT INTO Ve VALUES ('VE-041', 'LCH-021', 'G-002-30', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE-042', 'LCH-021', 'G-002-31', 'DA_BAN', NULL, NULL);
-- LCH-022 (SE7 Hue->DN 23/4, DT-003)
INSERT INTO Ve VALUES ('VE-043', 'LCH-022', 'G-003-30', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE-044', 'LCH-022', 'G-003-31', 'DA_BAN', NULL, NULL);
-- LCH-023 (SE3 HN->Vinh 25/4, DT-004)
INSERT INTO Ve VALUES ('VE-045', 'LCH-023', 'G-002-35', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE-046', 'LCH-023', 'G-002-36', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE-047', 'LCH-023', 'G-002-37', 'DA_BAN', NULL, NULL);
-- LCH-024 (SE3 Vinh->Hue 25/4, DT-004)
INSERT INTO Ve VALUES ('VE-048', 'LCH-024', 'G-001-20', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE-049', 'LCH-024', 'G-001-21', 'DA_BAN', NULL, NULL);
-- LCH-026 (SE3 HN->Vinh 28/4 le 30/4, DT-004): 5 ghe cung - ap dung KM-002 giam 10%
INSERT INTO Ve VALUES ('VE-050', 'LCH-026', 'G-003-01', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE-051', 'LCH-026', 'G-003-02', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE-052', 'LCH-026', 'G-003-03', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE-053', 'LCH-026', 'G-003-04', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE-054', 'LCH-026', 'G-003-05', 'DA_BAN', NULL, NULL);
-- LCH-027 (SE3 HN->SG 28/4 le 30/4, DT-004): 3 giuong nam - ap dung KM-002 giam 12%
INSERT INTO Ve VALUES ('VE-055', 'LCH-027', 'G-001-01', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE-056', 'LCH-027', 'G-001-02', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE-057', 'LCH-027', 'G-001-03', 'DA_BAN', NULL, NULL);
-- LCH-028 (SE4 SG->HN 30/4, DT-005)
INSERT INTO Ve VALUES ('VE-058', 'LCH-028', 'G-002-10', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE-059', 'LCH-028', 'G-002-11', 'DA_BAN', NULL, NULL);
-- LCH-029 (SE4 DN->SG 29/4, DT-005)
INSERT INTO Ve VALUES ('VE-060', 'LCH-029', 'G-003-15', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE-061', 'LCH-029', 'G-003-16', 'DA_BAN', NULL, NULL);

-- ==================== HoaDon (them 22 hoa don, ngay lap truoc ngay di 1-2 ngay) ====================
INSERT INTO HoaDon VALUES ('HD-16042026-001', 'NV-0001', '2026-04-14 09:00:00');
INSERT INTO HoaDon VALUES ('HD-16042026-002', 'NV-0002', '2026-04-14 10:30:00');
INSERT INTO HoaDon VALUES ('HD-16042026-003', 'NV-0004', '2026-04-14 13:00:00');
INSERT INTO HoaDon VALUES ('HD-16042026-004', 'NV-0007', '2026-04-15 08:30:00');
INSERT INTO HoaDon VALUES ('HD-17042026-001', 'NV-0008', '2026-04-15 11:00:00');
INSERT INTO HoaDon VALUES ('HD-17042026-002', 'NV-0001', '2026-04-15 14:00:00');
INSERT INTO HoaDon VALUES ('HD-17042026-003', 'NV-0002', '2026-04-15 16:30:00');
INSERT INTO HoaDon VALUES ('HD-18042026-001', 'NV-0010', '2026-04-16 09:00:00');
INSERT INTO HoaDon VALUES ('HD-18042026-002', 'NV-0017', '2026-04-16 11:00:00');
INSERT INTO HoaDon VALUES ('HD-19042026-001', 'NV-0001', '2026-04-17 08:00:00');
INSERT INTO HoaDon VALUES ('HD-20042026-001', 'NV-0002', '2026-04-18 09:30:00');
INSERT INTO HoaDon VALUES ('HD-20042026-002', 'NV-0004', '2026-04-18 13:00:00');
INSERT INTO HoaDon VALUES ('HD-21042026-001', 'NV-0007', '2026-04-19 10:00:00');
INSERT INTO HoaDon VALUES ('HD-21042026-002', 'NV-0008', '2026-04-19 14:30:00');
INSERT INTO HoaDon VALUES ('HD-22042026-001', 'NV-0013', '2026-04-20 09:00:00');
INSERT INTO HoaDon VALUES ('HD-22042026-002', 'NV-0001', '2026-04-20 11:00:00');
INSERT INTO HoaDon VALUES ('HD-25042026-001', 'NV-0002', '2026-04-23 08:30:00');
INSERT INTO HoaDon VALUES ('HD-25042026-002', 'NV-0016', '2026-04-23 10:00:00');
INSERT INTO HoaDon VALUES ('HD-28042026-001', 'NV-0001', '2026-04-26 09:00:00');
INSERT INTO HoaDon VALUES ('HD-28042026-002', 'NV-0002', '2026-04-26 10:30:00');
INSERT INTO HoaDon VALUES ('HD-29042026-001', 'NV-0017', '2026-04-27 11:00:00');
INSERT INTO HoaDon VALUES ('HD-01052026-001', 'NV-0018', '2026-04-29 09:00:00');

-- HoaDonKhachHang junction cho 22 HD moi them o tren
INSERT INTO HoaDonKhachHang VALUES ('HDKH-007', 'HD-16042026-001', 'KH-0006');
INSERT INTO HoaDonKhachHang VALUES ('HDKH-008', 'HD-16042026-002', 'KH-0007');
INSERT INTO HoaDonKhachHang VALUES ('HDKH-009', 'HD-16042026-003', 'KH-0008');
INSERT INTO HoaDonKhachHang VALUES ('HDKH-010', 'HD-16042026-004', 'KH-0009');
INSERT INTO HoaDonKhachHang VALUES ('HDKH-011', 'HD-17042026-001', 'KH-0010');
INSERT INTO HoaDonKhachHang VALUES ('HDKH-012', 'HD-17042026-002', 'KH-0011');
INSERT INTO HoaDonKhachHang VALUES ('HDKH-013', 'HD-17042026-003', 'KH-0012');
INSERT INTO HoaDonKhachHang VALUES ('HDKH-014', 'HD-18042026-001', 'KH-0013');
INSERT INTO HoaDonKhachHang VALUES ('HDKH-015', 'HD-18042026-002', 'KH-0014');
INSERT INTO HoaDonKhachHang VALUES ('HDKH-016', 'HD-19042026-001', 'KH-0015');
INSERT INTO HoaDonKhachHang VALUES ('HDKH-017', 'HD-20042026-001', 'KH-0016');
INSERT INTO HoaDonKhachHang VALUES ('HDKH-018', 'HD-20042026-002', 'KH-0017');
INSERT INTO HoaDonKhachHang VALUES ('HDKH-019', 'HD-21042026-001', 'KH-0018');
INSERT INTO HoaDonKhachHang VALUES ('HDKH-020', 'HD-21042026-002', 'KH-0019');
INSERT INTO HoaDonKhachHang VALUES ('HDKH-021', 'HD-22042026-001', 'KH-0020');
INSERT INTO HoaDonKhachHang VALUES ('HDKH-022', 'HD-22042026-002', 'KH-0006');
INSERT INTO HoaDonKhachHang VALUES ('HDKH-023', 'HD-25042026-001', 'KH-0007');
INSERT INTO HoaDonKhachHang VALUES ('HDKH-024', 'HD-25042026-002', 'KH-0008');
INSERT INTO HoaDonKhachHang VALUES ('HDKH-025', 'HD-28042026-001', 'KH-0011');
INSERT INTO HoaDonKhachHang VALUES ('HDKH-026', 'HD-28042026-002', 'KH-0012');
INSERT INTO HoaDonKhachHang VALUES ('HDKH-027', 'HD-29042026-001', 'KH-0013');
INSERT INTO HoaDonKhachHang VALUES ('HDKH-028', 'HD-01052026-001', 'KH-0014');

-- ==================== ChiTietHoaDon (them 52 chi tiet, CTHD-010 den CTHD-061) ====================
-- HD-16042026-001: 2 ve ghe cung TUY-001 HN->Vinh (180k/ve)
INSERT INTO ChiTietHoaDon VALUES ('CTHD-010', 'HD-16042026-001', 'VE-010', 180000.00);
INSERT INTO ChiTietHoaDon VALUES ('CTHD-011', 'HD-16042026-001', 'VE-011', 180000.00);
-- HD-16042026-002: 1 ve giuong nam TUY-001 HN->Vinh (400k)
INSERT INTO ChiTietHoaDon VALUES ('CTHD-012', 'HD-16042026-002', 'VE-012', 400000.00);
-- HD-16042026-003: 3 ve ghe mem TUY-002 Vinh->Hue (280k/ve)
INSERT INTO ChiTietHoaDon VALUES ('CTHD-013', 'HD-16042026-003', 'VE-013', 280000.00);
INSERT INTO ChiTietHoaDon VALUES ('CTHD-014', 'HD-16042026-003', 'VE-014', 280000.00);
INSERT INTO ChiTietHoaDon VALUES ('CTHD-015', 'HD-16042026-003', 'VE-015', 280000.00);
-- HD-16042026-004: 2 ve ghe cung TUY-003 Hue->DN (80k/ve)
INSERT INTO ChiTietHoaDon VALUES ('CTHD-016', 'HD-16042026-004', 'VE-016',  80000.00);
INSERT INTO ChiTietHoaDon VALUES ('CTHD-017', 'HD-16042026-004', 'VE-017',  80000.00);
-- HD-17042026-001: 1 ve ghe mem TUY-006 DN->Hue (120k)
INSERT INTO ChiTietHoaDon VALUES ('CTHD-018', 'HD-17042026-001', 'VE-018', 120000.00);
-- HD-17042026-002: 4 ve giuong nam TUY-009 HN->SG (1,600k/ve)
INSERT INTO ChiTietHoaDon VALUES ('CTHD-019', 'HD-17042026-002', 'VE-019', 1600000.00);
INSERT INTO ChiTietHoaDon VALUES ('CTHD-020', 'HD-17042026-002', 'VE-020', 1600000.00);
INSERT INTO ChiTietHoaDon VALUES ('CTHD-021', 'HD-17042026-002', 'VE-021', 1600000.00);
INSERT INTO ChiTietHoaDon VALUES ('CTHD-022', 'HD-17042026-002', 'VE-022', 1600000.00);
-- HD-17042026-003: 2 ve ghe mem TUY-009 HN->SG (1,100k/ve)
INSERT INTO ChiTietHoaDon VALUES ('CTHD-023', 'HD-17042026-003', 'VE-023', 1100000.00);
INSERT INTO ChiTietHoaDon VALUES ('CTHD-024', 'HD-17042026-003', 'VE-024', 1100000.00);
-- HD-18042026-001: 3 ve ghe cung TUY-010 SG->HN (800k/ve)
INSERT INTO ChiTietHoaDon VALUES ('CTHD-025', 'HD-18042026-001', 'VE-025',  800000.00);
INSERT INTO ChiTietHoaDon VALUES ('CTHD-026', 'HD-18042026-001', 'VE-026',  800000.00);
INSERT INTO ChiTietHoaDon VALUES ('CTHD-027', 'HD-18042026-001', 'VE-027',  800000.00);
-- HD-18042026-002: 2 ve giuong nam TUY-010 SG->HN (1,600k/ve)
INSERT INTO ChiTietHoaDon VALUES ('CTHD-028', 'HD-18042026-002', 'VE-028', 1600000.00);
INSERT INTO ChiTietHoaDon VALUES ('CTHD-029', 'HD-18042026-002', 'VE-029', 1600000.00);
-- HD-19042026-001: 1 ve ghe cung TUY-009 HN->SG, sinh vien ap dung KM-001 giam 20% (800k*0.8=640k)
INSERT INTO ChiTietHoaDon VALUES ('CTHD-030', 'HD-19042026-001', 'VE-030',  640000.00);
-- HD-20042026-001: 2 ve ghe mem TUY-001 HN->Vinh (250k/ve)
INSERT INTO ChiTietHoaDon VALUES ('CTHD-031', 'HD-20042026-001', 'VE-031',  250000.00);
INSERT INTO ChiTietHoaDon VALUES ('CTHD-032', 'HD-20042026-001', 'VE-032',  250000.00);
-- HD-20042026-002: 4 ve ghe cung TUY-004 DN->SG (450k/ve)
INSERT INTO ChiTietHoaDon VALUES ('CTHD-033', 'HD-20042026-002', 'VE-033',  450000.00);
INSERT INTO ChiTietHoaDon VALUES ('CTHD-034', 'HD-20042026-002', 'VE-034',  450000.00);
INSERT INTO ChiTietHoaDon VALUES ('CTHD-035', 'HD-20042026-002', 'VE-035',  450000.00);
INSERT INTO ChiTietHoaDon VALUES ('CTHD-036', 'HD-20042026-002', 'VE-036',  450000.00);
-- HD-21042026-001: 1 ve giuong nam TUY-005 SG->DN (900k) - ve bi huy
INSERT INTO ChiTietHoaDon VALUES ('CTHD-037', 'HD-21042026-001', 'VE-037',  900000.00);
-- HD-21042026-002: 3 ve ghe cung TUY-002 Vinh->Hue (200k/ve)
INSERT INTO ChiTietHoaDon VALUES ('CTHD-038', 'HD-21042026-002', 'VE-038',  200000.00);
INSERT INTO ChiTietHoaDon VALUES ('CTHD-039', 'HD-21042026-002', 'VE-039',  200000.00);
INSERT INTO ChiTietHoaDon VALUES ('CTHD-040', 'HD-21042026-002', 'VE-040',  200000.00);
-- HD-22042026-001: 2 ve ghe mem TUY-007 Hue->Vinh (280k/ve)
INSERT INTO ChiTietHoaDon VALUES ('CTHD-041', 'HD-22042026-001', 'VE-041',  280000.00);
INSERT INTO ChiTietHoaDon VALUES ('CTHD-042', 'HD-22042026-001', 'VE-042',  280000.00);
-- HD-22042026-002: 2 ve ghe cung TUY-003 Hue->DN (80k/ve)
INSERT INTO ChiTietHoaDon VALUES ('CTHD-043', 'HD-22042026-002', 'VE-043',   80000.00);
INSERT INTO ChiTietHoaDon VALUES ('CTHD-044', 'HD-22042026-002', 'VE-044',   80000.00);
-- HD-25042026-001: 3 ve ghe mem TUY-001 HN->Vinh (250k/ve)
INSERT INTO ChiTietHoaDon VALUES ('CTHD-045', 'HD-25042026-001', 'VE-045',  250000.00);
INSERT INTO ChiTietHoaDon VALUES ('CTHD-046', 'HD-25042026-001', 'VE-046',  250000.00);
INSERT INTO ChiTietHoaDon VALUES ('CTHD-047', 'HD-25042026-001', 'VE-047',  250000.00);
-- HD-25042026-002: 2 ve giuong nam TUY-002 Vinh->Hue (450k/ve)
INSERT INTO ChiTietHoaDon VALUES ('CTHD-048', 'HD-25042026-002', 'VE-048',  450000.00);
INSERT INTO ChiTietHoaDon VALUES ('CTHD-049', 'HD-25042026-002', 'VE-049',  450000.00);
-- HD-28042026-001: 5 ve ghe cung TUY-001 dip le 30/4, KM-002 giam 10% (180k*0.9=162k)
INSERT INTO ChiTietHoaDon VALUES ('CTHD-050', 'HD-28042026-001', 'VE-050',  162000.00);
INSERT INTO ChiTietHoaDon VALUES ('CTHD-051', 'HD-28042026-001', 'VE-051',  162000.00);
INSERT INTO ChiTietHoaDon VALUES ('CTHD-052', 'HD-28042026-001', 'VE-052',  162000.00);
INSERT INTO ChiTietHoaDon VALUES ('CTHD-053', 'HD-28042026-001', 'VE-053',  162000.00);
INSERT INTO ChiTietHoaDon VALUES ('CTHD-054', 'HD-28042026-001', 'VE-054',  162000.00);
-- HD-28042026-002: 3 ve giuong nam TUY-009 dip le 30/4, KM-002 giam 12% (1600k*0.88=1408k)
INSERT INTO ChiTietHoaDon VALUES ('CTHD-055', 'HD-28042026-002', 'VE-055', 1408000.00);
INSERT INTO ChiTietHoaDon VALUES ('CTHD-056', 'HD-28042026-002', 'VE-056', 1408000.00);
INSERT INTO ChiTietHoaDon VALUES ('CTHD-057', 'HD-28042026-002', 'VE-057', 1408000.00);
-- HD-29042026-001: 2 ve ghe mem TUY-010 SG->HN (1,100k/ve)
INSERT INTO ChiTietHoaDon VALUES ('CTHD-058', 'HD-29042026-001', 'VE-058', 1100000.00);
INSERT INTO ChiTietHoaDon VALUES ('CTHD-059', 'HD-29042026-001', 'VE-059', 1100000.00);
-- HD-01052026-001: 2 ve ghe cung TUY-004 DN->SG (450k/ve)
INSERT INTO ChiTietHoaDon VALUES ('CTHD-060', 'HD-01052026-001', 'VE-060',  450000.00);
INSERT INTO ChiTietHoaDon VALUES ('CTHD-061', 'HD-01052026-001', 'VE-061',  450000.00);

-- ==================== ApDungKM (them 9 ap dung khuyen mai) ====================
-- CTHD-030: sinh vien mua ghe cung TUY-009, ap dung KM-001 uu tien giam 20%
INSERT INTO ApDungKM VALUES ('ADKM-002', 'CTHD-030', 'CTKM-009');
-- CTHD-050..054: ghe cung TUY-001 dip Le 30/4, ap dung KM-002 giam 10%
INSERT INTO ApDungKM VALUES ('ADKM-003', 'CTHD-050', 'CTKM-031');
INSERT INTO ApDungKM VALUES ('ADKM-004', 'CTHD-051', 'CTKM-031');
INSERT INTO ApDungKM VALUES ('ADKM-005', 'CTHD-052', 'CTKM-031');
INSERT INTO ApDungKM VALUES ('ADKM-006', 'CTHD-053', 'CTKM-031');
INSERT INTO ApDungKM VALUES ('ADKM-007', 'CTHD-054', 'CTKM-031');
-- CTHD-055..057: giuong nam TUY-009 dip Le 30/4, ap dung KM-002 giam 12%
INSERT INTO ApDungKM VALUES ('ADKM-008', 'CTHD-055', 'CTKM-045');
INSERT INTO ApDungKM VALUES ('ADKM-009', 'CTHD-056', 'CTKM-045');
INSERT INTO ApDungKM VALUES ('ADKM-010', 'CTHD-057', 'CTKM-045');

-- ==================== GiuCho (them 6 giu cho) ====================
INSERT INTO GiuCho VALUES ('GC-003', 'NV-0002', 'LCH-014', 'G-002-22', '2026-04-16 15:00:00');
INSERT INTO GiuCho VALUES ('GC-004', 'NV-0002', 'LCH-014', 'G-002-23', '2026-04-16 15:00:00');
INSERT INTO GiuCho VALUES ('GC-005', 'NV-0004', 'LCH-015', 'G-003-15', '2026-04-17 12:00:00');
INSERT INTO GiuCho VALUES ('GC-006', 'NV-0007', 'LCH-027', 'G-001-10', '2026-04-27 16:00:00');
INSERT INTO GiuCho VALUES ('GC-007', 'NV-0007', 'LCH-027', 'G-001-11', '2026-04-27 16:00:00');
INSERT INTO GiuCho VALUES ('GC-008', 'NV-0017', 'LCH-028', 'G-002-20', '2026-04-28 09:00:00');

-- FK: NhanVien.gaLamViec -> Ga (them sau khi tat ca data da duoc insert)
ALTER TABLE NhanVien ADD CONSTRAINT FK_NhanVien_Ga
    FOREIGN KEY (gaLamViec) REFERENCES Ga(maGa);
GO

-- ============================================================
-- KIEM TRA DU LIEU
-- ============================================================
SELECT 'NhanVien' AS bang, COUNT(*) AS so_ban_ghi FROM NhanVien
UNION ALL SELECT 'KhachHang', COUNT(*) FROM KhachHang
UNION ALL SELECT 'Ga', COUNT(*) FROM Ga
UNION ALL SELECT 'DauMay', COUNT(*) FROM DauMay
UNION ALL SELECT 'ToaTau', COUNT(*) FROM ToaTau
UNION ALL SELECT 'Tuyen', COUNT(*) FROM Tuyen
UNION ALL SELECT 'DoanTau', COUNT(*) FROM DoanTau
UNION ALL SELECT 'ChiTietDoanTau', COUNT(*) FROM ChiTietDoanTau
UNION ALL SELECT 'Ghe', COUNT(*) FROM Ghe
UNION ALL SELECT 'Lich', COUNT(*) FROM Lich
UNION ALL SELECT 'Gia', COUNT(*) FROM Gia
UNION ALL SELECT 'ChiTietGia', COUNT(*) FROM ChiTietGia
UNION ALL SELECT 'KhuyenMai', COUNT(*) FROM KhuyenMai
UNION ALL SELECT 'ChiTietKhuyenMai', COUNT(*) FROM ChiTietKhuyenMai
UNION ALL SELECT 'Ve', COUNT(*) FROM Ve
UNION ALL SELECT 'HoaDon', COUNT(*) FROM HoaDon
UNION ALL SELECT 'ChiTietHoaDon', COUNT(*) FROM ChiTietHoaDon
UNION ALL SELECT 'ApDungKM', COUNT(*) FROM ApDungKM
UNION ALL SELECT 'GiuCho', COUNT(*) FROM GiuCho;
