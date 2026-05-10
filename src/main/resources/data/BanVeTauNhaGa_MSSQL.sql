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
    trangThai NVARCHAR(30) NOT NULL DEFAULT N'Đang hoạt động'
        CHECK (trangThai IN (N'Đang hoạt động', N'Ngừng khai thác')),
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
    maChiTietGia VARCHAR(20) NOT NULL,
    giaTien DECIMAL(18,2) NOT NULL CHECK (giaTien > 0),
    FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHoaDon),
    FOREIGN KEY (maVe) REFERENCES Ve(maVe),
    FOREIGN KEY (maChiTietGia) REFERENCES ChiTietGia(maChiTietGia)
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
('ad', N'Quản trị viên', 'ad', 'ADMIN', '0900000000', '001080000001', N'120 Lê Duẩn, Hoàn Kiếm, Hà Nội', 'DANG_LAM', 'admin@azurerail.vn', 'GA20260504100159', N'120 Lê Duẩn, Hoàn Kiếm, Hà Nội', '1980-01-01', 'NAM', N'Việt Nam');

INSERT INTO NhanVien (maNV, hoTen, [password], vaiTro, soDienThoai, cccd, diaChiTamTru, trangThai, email, gaLamViec, diaChiThuongTru, ngaySinh, gioiTinh, quocTich) VALUES
('NV20260504120358', N'Nguyễn Văn An', 'Pass@123', 'BAN_VE', '0901234567', '001085001234', N'15 Phố Huế, Hai Bà Trưng, Hà Nội', 'DANG_LAM', 'an.nguyenvan@azurerail.vn', 'GA20260504100159', N'45 Nguyễn Trãi, Thanh Xuân, Hà Nội', '1995-03-12', 'NAM', N'Việt Nam');

INSERT INTO NhanVien (maNV, hoTen, [password], vaiTro, soDienThoai, cccd, diaChiTamTru, trangThai, email, gaLamViec, diaChiThuongTru, ngaySinh, gioiTinh, quocTich) VALUES
('NV20260504140557', N'Trần Thị Bình', 'Pass@456', 'BAN_VE', '0912345678', '001085002345', N'23 Trần Hưng Đạo, Hoàn Kiếm, Hà Nội', 'DANG_LAM', 'binh.tranthithi@azurerail.vn', 'GA20260504100159', N'67 Bạch Mai, Hai Bà Trưng, Hà Nội', '1997-07-25', 'NU', N'Việt Nam');

INSERT INTO NhanVien (maNV, hoTen, [password], vaiTro, soDienThoai, cccd, diaChiTamTru, trangThai, email, gaLamViec, diaChiThuongTru, ngaySinh, gioiTinh, quocTich) VALUES
('NV20260504160756', N'Lê Hoàng Cường', 'Pass@789', 'DIEU_PHOI', '0923456789', '038085003456', N'10 Đường Phan Bội Châu, TP Vinh, Nghệ An', 'DANG_LAM', 'cuong.lehoang@azurerail.vn', 'GA20260504180955', N'88 Lê Lợi, TP Vinh, Nghệ An', '1990-11-08', 'NAM', N'Việt Nam');

INSERT INTO NhanVien (maNV, hoTen, [password], vaiTro, soDienThoai, cccd, diaChiTamTru, trangThai, email, gaLamViec, diaChiThuongTru, ngaySinh, gioiTinh, quocTich) VALUES
('NV20260504201155', N'Phạm Minh Đức', 'Pass@101', 'BAN_VE', '0934567890', '038085004567', N'34 Nguyễn Sỹ Sách, TP Vinh, Nghệ An', 'DANG_LAM', 'duc.phamminhh@azurerail.vn', 'GA20260504180955', N'12 Quang Trung, TP Vinh, Nghệ An', '1993-05-17', 'NAM', N'Việt Nam');

INSERT INTO NhanVien (maNV, hoTen, [password], vaiTro, soDienThoai, cccd, diaChiTamTru, trangThai, email, gaLamViec, diaChiThuongTru, ngaySinh, gioiTinh, quocTich) VALUES
('NV20260504221354', N'Hoàng Thị Elysa', 'Pass@102', 'BAN_VE', '0945678901', '046085005678', N'5 Bùi Thị Xuân, TP Huế, Thừa Thiên Huế', 'NGHI_PHEP', 'elysa.hoangthit@azurerail.vn', 'GA20260505001553', N'22 Hùng Vương, TP Huế, Thừa Thiên Huế', '1998-09-30', 'NU', N'Việt Nam');

INSERT INTO NhanVien (maNV, hoTen, [password], vaiTro, soDienThoai, cccd, diaChiTamTru, trangThai, email, gaLamViec, diaChiThuongTru, ngaySinh, gioiTinh, quocTich) VALUES
('NV20260505021752', N'Võ Văn Phúc', 'Pass@103', 'DIEU_PHOI', '0956789012', '046085006789', N'18 Điện Biên Phủ, TP Huế, Thừa Thiên Huế', 'DANG_LAM', 'phuc.vovan@azurerail.vn', 'GA20260505001553', N'99 Lê Thánh Tôn, TP Huế, Thừa Thiên Huế', '1988-02-14', 'NAM', N'Việt Nam');

INSERT INTO NhanVien (maNV, hoTen, [password], vaiTro, soDienThoai, cccd, diaChiTamTru, trangThai, email, gaLamViec, diaChiThuongTru, ngaySinh, gioiTinh, quocTich) VALUES
('NV20260505041951', N'Đặng Thùy Giang', 'Pass@104', 'BAN_VE', '0967890123', '048085007890', N'56 Hải Phòng, Thanh Khê, Đà Nẵng', 'DANG_LAM', 'giang.dangthuy@azurerail.vn', 'GA20260505062150', N'30 Nguyễn Văn Linh, Hải Châu, Đà Nẵng', '1996-12-03', 'NU', N'Việt Nam');

INSERT INTO NhanVien (maNV, hoTen, [password], vaiTro, soDienThoai, cccd, diaChiTamTru, trangThai, email, gaLamViec, diaChiThuongTru, ngaySinh, gioiTinh, quocTich) VALUES
('NV20260505082350', N'Bùi Quốc Huy', 'Pass@105', 'BAN_VE', '0978901234', '048085008901', N'72 Trần Phú, Hải Châu, Đà Nẵng', 'DANG_LAM', 'huy.buiquoc@azurerail.vn', 'GA20260505062150', N'14 Lê Duẩn, Hải Châu, Đà Nẵng', '1994-06-20', 'NAM', N'Việt Nam');

INSERT INTO NhanVien (maNV, hoTen, [password], vaiTro, soDienThoai, cccd, diaChiTamTru, trangThai, email, gaLamViec, diaChiThuongTru, ngaySinh, gioiTinh, quocTich) VALUES
('NV20260505102549', N'Ngô Thanh Inh', 'Pass@106', 'DIEU_PHOI', '0989012345', '079085009012', N'20 Nguyễn Thông, Quận 3, TP.HCM', 'DANG_LAM', 'inh.ngothanh@azurerail.vn', 'GA20260505122748', N'55 Võ Thị Sáu, Quận 3, TP.HCM', '1987-04-11', 'NAM', N'Việt Nam');

INSERT INTO NhanVien (maNV, hoTen, [password], vaiTro, soDienThoai, cccd, diaChiTamTru, trangThai, email, gaLamViec, diaChiThuongTru, ngaySinh, gioiTinh, quocTich) VALUES
('NV20260505142947', N'Lý Thị Kim', 'Pass@107', 'BAN_VE', '0990123456', '079085010123', N'8 Nam Kỳ Khởi Nghĩa, Quận 1, TP.HCM', 'NGHI_PHEP', 'kim.lythi@azurerail.vn', 'GA20260505122748', N'101 Cách Mạng Tháng 8, Quận 3, TP.HCM', '1999-08-16', 'NU', N'Việt Nam');

INSERT INTO NhanVien (maNV, hoTen, [password], vaiTro, soDienThoai, cccd, diaChiTamTru, trangThai, email, gaLamViec, diaChiThuongTru, ngaySinh, gioiTinh, quocTich) VALUES
('NV20260505163146', N'Trương Đình Lâm', 'Pass@108', 'BAN_VE', '0901122334', '001085011234', N'37 Kim Liên, Đống Đa, Hà Nội', 'DANG_LAM', 'lam.truongdinh@azurerail.vn', 'GA20260504100159', N'9 Phạm Ngọc Thạch, Đống Đa, Hà Nội', '1992-01-28', 'NAM', N'Việt Nam');

INSERT INTO NhanVien (maNV, hoTen, [password], vaiTro, soDienThoai, cccd, diaChiTamTru, trangThai, email, gaLamViec, diaChiThuongTru, ngaySinh, gioiTinh, quocTich) VALUES
('NV20260505183345', N'Phan Thị Mai', 'Pass@109', 'DIEU_PHOI', '0912233445', '001085012345', N'44 Hàng Bông, Hoàn Kiếm, Hà Nội', 'DANG_LAM', 'mai.phanthi@azurerail.vn', 'GA20260504100159', N'26 Tây Sơn, Đống Đa, Hà Nội', '1991-10-05', 'NU', N'Việt Nam');

INSERT INTO NhanVien (maNV, hoTen, [password], vaiTro, soDienThoai, cccd, diaChiTamTru, trangThai, email, gaLamViec, diaChiThuongTru, ngaySinh, gioiTinh, quocTich) VALUES
('NV20260505203545', N'Hồ Trọng Nam', 'Pass@110', 'BAN_VE', '0923344556', '038085013456', N'3 Lê Hồng Phong, TP Vinh, Nghệ An', 'DANG_LAM', 'nam.hotrong@azurerail.vn', 'GA20260504180955', N'77 Đinh Công Tráng, TP Vinh, Nghệ An', '1989-07-19', 'NAM', N'Việt Nam');

INSERT INTO NhanVien (maNV, hoTen, [password], vaiTro, soDienThoai, cccd, diaChiTamTru, trangThai, email, gaLamViec, diaChiThuongTru, ngaySinh, gioiTinh, quocTich) VALUES
('NV20260505223744', N'Dương Thị Oanh', 'Pass@111', 'BAN_VE', '0934455667', '046085014567', N'11 Chu Văn An, TP Huế, Thừa Thiên Huế', 'DANG_LAM', 'oanh.duongthit@azurerail.vn', 'GA20260505001553', N'50 Trần Cao Vân, TP Huế, Thừa Thiên Huế', '1996-03-22', 'NU', N'Việt Nam');

INSERT INTO NhanVien (maNV, hoTen, [password], vaiTro, soDienThoai, cccd, diaChiTamTru, trangThai, email, gaLamViec, diaChiThuongTru, ngaySinh, gioiTinh, quocTich) VALUES
('NV20260506003943', N'Tạ Minh Phong', 'Pass@112', 'DIEU_PHOI', '0945566778', '048085015678', N'29 Phan Châu Trinh, Hải Châu, Đà Nẵng', 'NGHI_PHEP', 'phong.taminh@azurerail.vn', 'GA20260505062150', N'63 Ông Ích Khiêm, Thanh Khê, Đà Nẵng', '1985-11-14', 'NAM', N'Việt Nam');

INSERT INTO NhanVien (maNV, hoTen, [password], vaiTro, soDienThoai, cccd, diaChiTamTru, trangThai, email, gaLamViec, diaChiThuongTru, ngaySinh, gioiTinh, quocTich) VALUES
('NV20260506024142', N'Vũ Thị Quỳnh', 'Pass@113', 'BAN_VE', '0956677889', '048085016789', N'6 Trần Quý Cáp, Hải Châu, Đà Nẵng', 'DANG_LAM', 'quynh.vuthi@azurerail.vn', 'GA20260505062150', N'18 Lê Văn Hiến, Ngũ Hành Sơn, Đà Nẵng', '1997-05-09', 'NU', N'Việt Nam');

INSERT INTO NhanVien (maNV, hoTen, [password], vaiTro, soDienThoai, cccd, diaChiTamTru, trangThai, email, gaLamViec, diaChiThuongTru, ngaySinh, gioiTinh, quocTich) VALUES
('NV20260506044341', N'Đinh Công Sơn', 'Pass@114', 'BAN_VE', '0967788990', '079085017890', N'40 Đinh Tiên Hoàng, Bình Thạnh, TP.HCM', 'DANG_LAM', 'son.dinhcong@azurerail.vn', 'GA20260505122748', N'82 Phan Đình Giót, Bình Thạnh, TP.HCM', '1993-09-27', 'NAM', N'Việt Nam');

INSERT INTO NhanVien (maNV, hoTen, [password], vaiTro, soDienThoai, cccd, diaChiTamTru, trangThai, email, gaLamViec, diaChiThuongTru, ngaySinh, gioiTinh, quocTich) VALUES
('NV20260506064540', N'Mai Thị Tâm', 'Pass@115', 'BAN_VE', '0978899001', '079085018901', N'13 Lý Tự Trọng, Quận 1, TP.HCM', 'DANG_LAM', 'tam.maithi@azurerail.vn', 'GA20260505122748', N'35 Trương Định, Quận 3, TP.HCM', '1998-02-06', 'NU', N'Việt Nam');

INSERT INTO NhanVien (maNV, hoTen, [password], vaiTro, soDienThoai, cccd, diaChiTamTru, trangThai, email, gaLamViec, diaChiThuongTru, ngaySinh, gioiTinh, quocTich) VALUES
('NV20260506084740', N'Lương Văn Uy', 'Pass@116', 'DIEU_PHOI', '0989900112', '001085019012', N'58 Giải Phóng, Hoàng Mai, Hà Nội', 'DANG_LAM', 'uy.luongvan@azurerail.vn', 'GA20260504100159', N'24 Trương Định, Hoàng Mai, Hà Nội', '1986-06-13', 'NAM', N'Việt Nam');

INSERT INTO NhanVien (maNV, hoTen, [password], vaiTro, soDienThoai, cccd, diaChiTamTru, trangThai, email, gaLamViec, diaChiThuongTru, ngaySinh, gioiTinh, quocTich) VALUES
('NV20260506104939', N'Cao Thị Vân', 'Pass@117', 'BAN_VE', '0990011223', '001085020123', N'19 Xã Đàn, Đống Đa, Hà Nội', 'DA_NGHI', 'van.caothi@azurerail.vn', 'GA20260504100159', N'7 La Thành, Đống Đa, Hà Nội', '1990-12-31', 'NU', N'Việt Nam');

INSERT INTO NhanVien (maNV, hoTen, [password], vaiTro, soDienThoai, cccd, diaChiTamTru, trangThai, email, gaLamViec, diaChiThuongTru, ngaySinh, gioiTinh, quocTich) VALUES
('NV20260506125138', N'Châu Quốc Xuân', 'Pass@118', 'BAN_VE', '0901233210', '079085021234', N'25 Bà Huyện Thanh Quan, Quận 3, TP.HCM', 'NGHI_PHEP', 'xuan.chauquoc@azurerail.vn', 'GA20260505122748', N'16 Đinh Tiên Hoàng, Quận 1, TP.HCM', '1995-08-21', 'NAM', N'Việt Nam');

INSERT INTO NhanVien (maNV, hoTen, [password], vaiTro, soDienThoai, cccd, diaChiTamTru, trangThai, email, gaLamViec, diaChiThuongTru, ngaySinh, gioiTinh, quocTich) VALUES
('NV20260506145337', N'Kiều Thị Yến', 'Pass@119', 'DIEU_PHOI', '0912344321', '046085022345', N'31 Trần Thị Lý, Hải Châu, Đà Nẵng', 'DA_NGHI', 'yen.kieuthit@azurerail.vn', 'GA20260505001553', N'48 Lê Lợi, TP Huế, Thừa Thiên Huế', '1988-04-17', 'NU', N'Việt Nam');

INSERT INTO NhanVien (maNV, hoTen, [password], vaiTro, soDienThoai, cccd, diaChiTamTru, trangThai, email, gaLamViec, diaChiThuongTru, ngaySinh, gioiTinh, quocTich) VALUES
('NV20260506165536', N'Trịnh Đức Zũng', 'Pass@120', 'BAN_VE', '0923455432', '038085023456', N'62 Nguyễn Viết Xuân, TP Vinh, Nghệ An', 'DA_NGHI', 'zung.trinhduc@azurerail.vn', 'GA20260504180955', N'33 Nguyễn Du, TP Vinh, Nghệ An', '1991-10-10', 'NAM', N'Việt Nam');

-- ==================== 2. KhachHang (BANG MOI) ====================
INSERT INTO KhachHang (maKhachHang, hoTen, cccd, soDienThoai, email, diaChiThuongTru, diaChiTamTru, ngaySinh, gioiTinh, quocTich) VALUES
('KH20260506185736', N'Phạm Minh Tuấn', '012345678901', '0371234567', 'tuan.phamminh@email.com', N'12 Lê Lợi, Hải Châu, Đà Nẵng', NULL, '1992-03-15', 'NAM', N'Việt Nam'),
('KH20260506205935', N'Hoàng Đức Mạnh', '034567890123', '0382345678', 'manh.hoangduc@email.com', N'55 Nguyễn Du, TP Vinh, Nghệ An', NULL, '1988-08-22', 'NAM', N'Việt Nam'),
('KH20260506230134', N'Nguyễn Thị Hoa', '056789012345', '0393456789', 'hoa.nguyenthi@email.com', N'88 Lê Thánh Tôn, TP Huế, Thừa Thiên Huế', NULL, '1995-12-03', 'NU', N'Việt Nam'),
('KH20260507010333', N'Trần Văn Đức', '078901234567', '0354567890', 'duc.tranvan@email.com', N'21 Phạm Văn Đồng, Cầu Giấy, Hà Nội', NULL, '1990-05-18', 'NAM', N'Việt Nam'),
('KH20260507030532', N'Lý Văn Hùng', '090123456789', '0365678901', 'hung.lyvan@email.com', N'40 Trần Phú, Hải Châu, Đà Nẵng', N'5 Lê Lợi, Quận 1, TP.HCM', '1993-11-27', 'NAM', N'Việt Nam');

-- ==================== 3. Ga ====================
-- Tuyen duong sat Bac-Nam (Thong Nhat), 17 ga chinh theo dia ly tu Bac xuong Nam
-- Nguon: Tong cong ty Duong sat Viet Nam (VNR) - dia chi chinh thuc
INSERT INTO Ga VALUES ('GA20260504100159', N'Ga Hà Nội',               N'120 Lê Duẩn, Hoàn Kiếm, Hà Nội');
INSERT INTO Ga VALUES ('GA20260504180955', N'Ga Vinh',                  N'Đường Phan Bội Châu, TP Vinh, Nghệ An');
INSERT INTO Ga VALUES ('GA20260505001553', N'Ga Huế',                   N'2 Bùi Thị Xuân, TP Huế, Thừa Thiên Huế');
INSERT INTO Ga VALUES ('GA20260505062150', N'Ga Đà Nẵng',               N'791 Hải Phòng, Thanh Khê, Đà Nẵng');
INSERT INTO Ga VALUES ('GA20260505122748', N'Ga Sài Gòn',               N'1 Nguyễn Thông, Quận 3, TP.HCM');
-- Ga trung gian (giu nguyen ID 001-005 de tuong thich du lieu hien co)
INSERT INTO Ga VALUES ('GA20260507050731', N'Ga Nam Định',              N'9 Trần Quý Cáp, TP Nam Định, Nam Định');
INSERT INTO Ga VALUES ('GA20260507070931', N'Ga Ninh Bình',             N'Đường Lý Thái Tổ, TP Ninh Bình, Ninh Bình');
INSERT INTO Ga VALUES ('GA20260507091130', N'Ga Thanh Hóa',             N'Đường Trần Phú, TP Thanh Hóa, Thanh Hóa');
INSERT INTO Ga VALUES ('GA20260507111329', N'Ga Đồng Hới',              N'Đường Trần Hưng Đạo, TP Đồng Hới, Quảng Bình');
INSERT INTO Ga VALUES ('GA20260507131528', N'Ga Đông Hà',               N'Đường Lê Duẩn, TP Đông Hà, Quảng Trị');
INSERT INTO Ga VALUES ('GA20260507151727', N'Ga Tam Kỳ',                N'191 Phan Chu Trinh, TP Tam Kỳ, Quảng Nam');
INSERT INTO Ga VALUES ('GA20260507171926', N'Ga Quảng Ngãi',            N'Đường Nguyễn Bỉnh Khiêm, TP Quảng Ngãi, Quảng Ngãi');
INSERT INTO Ga VALUES ('GA20260507192126', N'Ga Diêu Trì',              N'Thị trấn Diêu Trì, huyện Tuy Phước, Bình Định');
INSERT INTO Ga VALUES ('GA20260507212325', N'Ga Tuy Hòa',               N'Đường Lê Duẩn, TP Tuy Hòa, Phú Yên');
INSERT INTO Ga VALUES ('GA20260507232524', N'Ga Nha Trang',             N'17 Thái Nguyên, TP Nha Trang, Khánh Hòa');
INSERT INTO Ga VALUES ('GA20260508012723', N'Ga Phan Rang-Tháp Chàm',  N'Đường Thống Nhất, TP Phan Rang-Tháp Chàm, Ninh Thuận');
INSERT INTO Ga VALUES ('GA20260508032922', N'Ga Biên Hòa',              N'1 Hà Huy Giáp, TP Biên Hòa, Đồng Nai');

-- ==================== 4. DauMay ====================
-- Nguon: Tong cong ty Duong sat Viet Nam (VNR)
-- D19E: diesel-dien Dong Phong DF7G (Trung Quoc), 60 don vi (901-960), nhap 2006-2014
--        cong suat 1500kW, toc do toi da 100km/h, su dung tren tuyen Bac-Nam chinh
INSERT INTO DauMay (maDauMay, tenDauMay, hangSanXuat, namSanXuat, congSuatKw, trangThai, moTa) VALUES
('DM20260508053121', N'Đầu máy D19E-901', N'Dongfang (Trung Quốc)', 2007, 1500, N'Đang hoạt động', N'Đầu kéo chính tuyến Bắc - Nam, vận hành ổn định.');
INSERT INTO DauMay (maDauMay, tenDauMay, hangSanXuat, namSanXuat, congSuatKw, trangThai, moTa) VALUES
('DM20260508073321', N'Đầu máy D19E-902', N'Dongfang (Trung Quốc)', 2008, 1500, N'Đang hoạt động', N'Đầu kéo chính cho tàu khách nhanh.');
INSERT INTO DauMay (maDauMay, tenDauMay, hangSanXuat, namSanXuat, congSuatKw, trangThai, moTa) VALUES
('DM20260508093520', N'Đầu máy D19E-903', N'Dongfang (Trung Quốc)', 2009, 1500, N'Đang hoạt động', N'Vận hành trên hành trình dài liên tỉnh.');
INSERT INTO DauMay (maDauMay, tenDauMay, hangSanXuat, namSanXuat, congSuatKw, trangThai, moTa) VALUES
('DM20260508113719', N'Đầu máy D19E-904', N'Dongfang (Trung Quốc)', 2010, 1500, N'Bảo trì', N'Đang bảo trì định kỳ hệ thống truyền động.');
INSERT INTO DauMay (maDauMay, tenDauMay, hangSanXuat, namSanXuat, congSuatKw, trangThai, moTa) VALUES
('DM20260508133918', N'Đầu máy D19E-905', N'Dongfang (Trung Quốc)', 2011, 1500, N'Đang hoạt động', N'Đầu kéo dự phòng cho các chuyến tăng cường.');
-- D14E: diesel-dien EMD (My), 7 don vi (001-007), nhap 1997
--        cong suat 1490kW, su dung tuyen chinh Bac-Nam
INSERT INTO DauMay (maDauMay, tenDauMay, hangSanXuat, namSanXuat, congSuatKw, trangThai, moTa) VALUES
('DM20260508154117', N'Đầu máy D14E-001', N'EMD (Mỹ)', 1997, 1490, N'Đang hoạt động', N'Đầu máy diesel điện thế hệ cũ, hiệu suất ổn định.');
INSERT INTO DauMay (maDauMay, tenDauMay, hangSanXuat, namSanXuat, congSuatKw, trangThai, moTa) VALUES
('DM20260508174316', N'Đầu máy D14E-002', N'EMD (Mỹ)', 1997, 1490, N'Đang hoạt động', N'Thường khai thác trên tuyến đường dài nhiều chặng.');
-- D13E: diesel-thuy luc Mitsubishi/Toshiba (Nhat), 68 don vi, nhap 1992-1996
--        cong suat 900kW, toc do toi da 90km/h, su dung tuyen chinh va nhanh
INSERT INTO DauMay (maDauMay, tenDauMay, hangSanXuat, namSanXuat, congSuatKw, trangThai, moTa) VALUES
('DM20260508194516', N'Đầu máy D13E-006', N'Mitsubishi/Toshiba (Nhật Bản)', 1993, 900, N'Đang hoạt động', N'Phục vụ tuyến ngắn và trung bình, tiết kiệm nhiên liệu.');
INSERT INTO DauMay (maDauMay, tenDauMay, hangSanXuat, namSanXuat, congSuatKw, trangThai, moTa) VALUES
('DM20260508214715', N'Đầu máy D13E-012', N'Mitsubishi/Toshiba (Nhật Bản)', 1994, 900, N'Đang hoạt động', N'Đầu kéo linh hoạt cho nhiều loại đoàn tàu.');
INSERT INTO DauMay (maDauMay, tenDauMay, hangSanXuat, namSanXuat, congSuatKw, trangThai, moTa) VALUES
('DM20260508234914', N'Đầu máy D13E-018', N'Mitsubishi/Toshiba (Nhật Bản)', 1995, 900, N'Ngừng khai thác', N'Tạm ngừng khai thác để chờ nâng cấp lớn.');
-- D12E: diesel Krupp (Duc), 25 don vi, nhap 1988, hien dung tuyen nhanh va hang hoa
INSERT INTO DauMay (maDauMay, tenDauMay, hangSanXuat, namSanXuat, congSuatKw, trangThai, moTa) VALUES
('DM20260509015113', N'Đầu máy D12E-001', N'Krupp (Đức)', 1988, 1200, N'Đang hoạt động', N'Đầu máy lâu năm, thường dùng cho tàu hàng và tuyến phụ.');

-- ==================== 5. ToaTau ====================
-- 3 mau toa chuan: moi loai 1 toa, so ghe theo quy dinh
-- TOA20260509035312: Giuong nam (3x10 = 30 giuong)
-- TOA20260509055512: Ghe mem    (4x12 = 48 ghe)
-- TOA20260509075711: Ghe cung   (4x12 = 48 ghe)
INSERT INTO ToaTau (maToaTau, loaiGhe) VALUES ('TOA20260509035312', 'GIUONG_NAM');
INSERT INTO ToaTau (maToaTau, loaiGhe) VALUES ('TOA20260509055512', 'GHE_MEM');
INSERT INTO ToaTau (maToaTau, loaiGhe) VALUES ('TOA20260509075711', 'GHE_CUNG');

-- ==================== 6. Tuyen ====================
-- Ghi chu: Tuyen trong DB la tuyen dich vu (ga di - ga den), khong phai chi doan vat ly.
-- TUY20260509095910..010: Tuyen express giua cac ga chinh (giu nguyen de tuong thich du lieu mau)
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY20260509095910', 'GA20260504100159', 'GA20260504180955', 319);   -- Ha Noi -> Vinh
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY20260509120109', 'GA20260504180955', 'GA20260505001553', 368);   -- Vinh -> Hue
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY20260509140308', 'GA20260505001553', 'GA20260505062150', 100);   -- Hue -> Da Nang
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY20260509160507', 'GA20260505062150', 'GA20260505122748', 935);   -- Da Nang -> Sai Gon
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY20260509180707', 'GA20260505122748', 'GA20260505062150', 935);   -- Sai Gon -> Da Nang
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY20260509200906', 'GA20260505062150', 'GA20260505001553', 100);   -- Da Nang -> Hue
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY20260509221105', 'GA20260505001553', 'GA20260504180955', 368);   -- Hue -> Vinh
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY20260510001304', 'GA20260504180955', 'GA20260504100159', 319);   -- Vinh -> Ha Noi
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY20260510021503', 'GA20260504100159', 'GA20260505122748', 1726);  -- Ha Noi -> Sai Gon (xuyen Viet)
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY20260510041702', 'GA20260505122748', 'GA20260504100159', 1726);  -- Sai Gon -> Ha Noi (xuyen Viet)
-- TUY20260510061902..025: Doan trung gian Bac->Nam (theo thu tu dia ly)
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY20260510061902', 'GA20260504100159', 'GA20260507050731', 87);    -- Ha Noi -> Nam Dinh     (~87 km)
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY20260510082101', 'GA20260507050731', 'GA20260507070931', 30);    -- Nam Dinh -> Ninh Binh  (~30 km)
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY20260510102300', 'GA20260507070931', 'GA20260507091130', 57);    -- Ninh Binh -> Thanh Hoa (~57 km)
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY20260510122459', 'GA20260507091130', 'GA20260504180955', 73);    -- Thanh Hoa -> Vinh      (~73 km)
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY20260510142658', 'GA20260504180955', 'GA20260507111329', 166);   -- Vinh -> Dong Hoi       (~166 km)
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY20260510162857', 'GA20260507111329', 'GA20260507131528', 72);    -- Dong Hoi -> Dong Ha    (~72 km)
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY20260510183057', 'GA20260507131528', 'GA20260505001553', 70);    -- Dong Ha -> Hue         (~70 km)
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY20260510203256', 'GA20260505001553', 'GA20260505062150', 100);   -- Hue -> Da Nang (qua Hai Van, ~100 km)
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY20260510223455', 'GA20260505062150', 'GA20260507151727', 72);    -- Da Nang -> Tam Ky      (~72 km)
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY20260511003654', 'GA20260507151727', 'GA20260507171926', 40);    -- Tam Ky -> Quang Ngai   (~40 km)
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY20260511023853', 'GA20260507171926', 'GA20260507192126', 107);   -- Quang Ngai -> Dieu Tri (~107 km)
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY20260511044052', 'GA20260507192126', 'GA20260507212325', 85);    -- Dieu Tri -> Tuy Hoa    (~85 km)
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY20260511064252', 'GA20260507212325', 'GA20260507232524', 104);   -- Tuy Hoa -> Nha Trang   (~104 km)
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY20260511084451', 'GA20260507232524', 'GA20260508012723', 101);   -- Nha Trang -> Phan Rang (~101 km)
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY20260511104650', 'GA20260508012723', 'GA20260508032922', 185);   -- Phan Rang -> Bien Hoa  (~185 km)
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY20260511124849', 'GA20260508032922', 'GA20260505122748', 32);    -- Bien Hoa -> Sai Gon    (~32 km)
-- TUY20260511145048..040: Doan trung gian Nam->Bac (nguoc chieu)
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY20260511145048', 'GA20260507050731', 'GA20260504100159', 87);    -- Nam Dinh -> Ha Noi
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY20260511165248', 'GA20260507070931', 'GA20260507050731', 30);    -- Ninh Binh -> Nam Dinh
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY20260511185447', 'GA20260507091130', 'GA20260507070931', 57);    -- Thanh Hoa -> Ninh Binh
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY20260511205646', 'GA20260504180955', 'GA20260507091130', 73);    -- Vinh -> Thanh Hoa
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY20260511225845', 'GA20260507111329', 'GA20260504180955', 166);   -- Dong Hoi -> Vinh
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY20260512010044', 'GA20260507131528', 'GA20260507111329', 72);    -- Dong Ha -> Dong Hoi
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY20260512030243', 'GA20260505001553', 'GA20260507131528', 70);    -- Hue -> Dong Ha
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY20260512050443', 'GA20260507151727', 'GA20260505062150', 72);    -- Tam Ky -> Da Nang
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY20260512070642', 'GA20260507171926', 'GA20260507151727', 40);    -- Quang Ngai -> Tam Ky
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY20260512090841', 'GA20260507192126', 'GA20260507171926', 107);   -- Dieu Tri -> Quang Ngai
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY20260512111040', 'GA20260507212325', 'GA20260507192126', 85);    -- Tuy Hoa -> Dieu Tri
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY20260512131239', 'GA20260507232524', 'GA20260507212325', 104);   -- Nha Trang -> Tuy Hoa
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY20260512151438', 'GA20260508012723', 'GA20260507232524', 101);   -- Phan Rang -> Nha Trang
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY20260512171638', 'GA20260508032922', 'GA20260508012723', 185);   -- Bien Hoa -> Phan Rang
INSERT INTO Tuyen (maTuyen, gaDi, gaDen, km) VALUES ('TUY20260512191837', 'GA20260505122748', 'GA20260508032922', 32);    -- Sai Gon -> Bien Hoa

-- ==================== 7. DoanTau ====================
-- Ten tau theo quy uoc cua VNR (SE = Speed Express, TN = Thong Nhat)
-- Tau SE1/SE2 chay hang ngay tren tuyen Thong Nhat (Sai Gon <-> Ha Noi, ~1726km)
-- Tau SE3/SE4: tuyen chinh xuat phat 06:00; SE7/SE8: Ha Noi <-> Da Nang
-- TN1/TN2: tau Thong Nhat phu (cham hon SE)
INSERT INTO DoanTau (maDoanTau, tenDoanTau, maDauMay, trangThai) VALUES ('DT20260512212036', N'SE1 (Sài Gòn → Hà Nội)', 'DM20260508053121', N'Đang hoạt động');
INSERT INTO DoanTau (maDoanTau, tenDoanTau, maDauMay, trangThai) VALUES ('DT20260512232235', N'SE2 (Hà Nội → Sài Gòn)', 'DM20260508073321', N'Đang hoạt động');
INSERT INTO DoanTau (maDoanTau, tenDoanTau, maDauMay, trangThai) VALUES ('DT20260513012434', N'SE7 (Hà Nội → Đà Nẵng)', 'DM20260508093520', N'Đang hoạt động');

-- ==================== 8. ChiTietDoanTau ====================
-- DT20260512212036 (SE1): TOA20260509075711(cung thu 1), TOA20260509055512(mem thu 2), TOA20260509035312(giuong thu 3)
INSERT INTO ChiTietDoanTau VALUES ('CTDT20260513032633', 'DT20260512212036', 'TOA20260509075711', 1);
INSERT INTO ChiTietDoanTau VALUES ('CTDT20260513052833', 'DT20260512212036', 'TOA20260509055512', 2);
INSERT INTO ChiTietDoanTau VALUES ('CTDT20260513073032', 'DT20260512212036', 'TOA20260509035312', 3);
-- DT20260512232235 (SE2): TOA20260509075711(cung thu 1), TOA20260509055512(mem thu 2), TOA20260509035312(giuong thu 3)
INSERT INTO ChiTietDoanTau VALUES ('CTDT20260513093231', 'DT20260512232235', 'TOA20260509075711', 1);
INSERT INTO ChiTietDoanTau VALUES ('CTDT20260513113430', 'DT20260512232235', 'TOA20260509055512', 2);
INSERT INTO ChiTietDoanTau VALUES ('CTDT20260513133629', 'DT20260512232235', 'TOA20260509035312', 3);
-- DT20260513012434 (SE7): TOA20260509075711(cung thu 1), TOA20260509055512(mem thu 2)
INSERT INTO ChiTietDoanTau VALUES ('CTDT20260513153828', 'DT20260513012434', 'TOA20260509075711', 1);
INSERT INTO ChiTietDoanTau VALUES ('CTDT20260513174028', 'DT20260513012434', 'TOA20260509055512', 2);

-- ==================== 9. Ghe ====================
-- TOA20260509035312 (giuong nam): 3x10 = 30 giuong, danh so 1..30 theo hang trai->phai, tren->duoi
INSERT INTO Ghe VALUES ('GHE20260513194227', 'TOA20260509035312',  1); INSERT INTO Ghe VALUES ('GHE20260513214426', 'TOA20260509035312',  2); INSERT INTO Ghe VALUES ('GHE20260513234625', 'TOA20260509035312',  3);
INSERT INTO Ghe VALUES ('GHE20260514014824', 'TOA20260509035312',  4); INSERT INTO Ghe VALUES ('GHE20260514035024', 'TOA20260509035312',  5); INSERT INTO Ghe VALUES ('GHE20260514055223', 'TOA20260509035312',  6);
INSERT INTO Ghe VALUES ('GHE20260514075422', 'TOA20260509035312',  7); INSERT INTO Ghe VALUES ('GHE20260514095621', 'TOA20260509035312',  8); INSERT INTO Ghe VALUES ('GHE20260514115820', 'TOA20260509035312',  9);
INSERT INTO Ghe VALUES ('GHE20260514140019', 'TOA20260509035312', 10); INSERT INTO Ghe VALUES ('GHE20260514160219', 'TOA20260509035312', 11); INSERT INTO Ghe VALUES ('GHE20260514180418', 'TOA20260509035312', 12);
INSERT INTO Ghe VALUES ('GHE20260514200617', 'TOA20260509035312', 13); INSERT INTO Ghe VALUES ('GHE20260514220816', 'TOA20260509035312', 14); INSERT INTO Ghe VALUES ('GHE20260515001015', 'TOA20260509035312', 15);
INSERT INTO Ghe VALUES ('GHE20260515021214', 'TOA20260509035312', 16); INSERT INTO Ghe VALUES ('GHE20260515041414', 'TOA20260509035312', 17); INSERT INTO Ghe VALUES ('GHE20260515061613', 'TOA20260509035312', 18);
INSERT INTO Ghe VALUES ('GHE20260515081812', 'TOA20260509035312', 19); INSERT INTO Ghe VALUES ('GHE20260515102011', 'TOA20260509035312', 20); INSERT INTO Ghe VALUES ('GHE20260515122210', 'TOA20260509035312', 21);
INSERT INTO Ghe VALUES ('GHE20260515142409', 'TOA20260509035312', 22); INSERT INTO Ghe VALUES ('GHE20260515162609', 'TOA20260509035312', 23); INSERT INTO Ghe VALUES ('GHE20260515182808', 'TOA20260509035312', 24);
INSERT INTO Ghe VALUES ('GHE20260515203007', 'TOA20260509035312', 25); INSERT INTO Ghe VALUES ('GHE20260515223206', 'TOA20260509035312', 26); INSERT INTO Ghe VALUES ('GHE20260516003405', 'TOA20260509035312', 27);
INSERT INTO Ghe VALUES ('GHE20260516023604', 'TOA20260509035312', 28); INSERT INTO Ghe VALUES ('GHE20260516043804', 'TOA20260509035312', 29); INSERT INTO Ghe VALUES ('GHE20260516064003', 'TOA20260509035312', 30);
-- TOA20260509055512 (ghe mem): 4x12 = 48 ghe
INSERT INTO Ghe VALUES ('GHE20260516084202', 'TOA20260509055512',  1); INSERT INTO Ghe VALUES ('GHE20260516104401', 'TOA20260509055512',  2); INSERT INTO Ghe VALUES ('GHE20260516124600', 'TOA20260509055512',  3);
INSERT INTO Ghe VALUES ('GHE20260516144800', 'TOA20260509055512',  4); INSERT INTO Ghe VALUES ('GHE20260516164959', 'TOA20260509055512',  5); INSERT INTO Ghe VALUES ('GHE20260516185158', 'TOA20260509055512',  6);
INSERT INTO Ghe VALUES ('GHE20260516205357', 'TOA20260509055512',  7); INSERT INTO Ghe VALUES ('GHE20260516225556', 'TOA20260509055512',  8); INSERT INTO Ghe VALUES ('GHE20260517005755', 'TOA20260509055512',  9);
INSERT INTO Ghe VALUES ('GHE20260517025955', 'TOA20260509055512', 10); INSERT INTO Ghe VALUES ('GHE20260517050154', 'TOA20260509055512', 11); INSERT INTO Ghe VALUES ('GHE20260517070353', 'TOA20260509055512', 12);
INSERT INTO Ghe VALUES ('GHE20260517090552', 'TOA20260509055512', 13); INSERT INTO Ghe VALUES ('GHE20260517110751', 'TOA20260509055512', 14); INSERT INTO Ghe VALUES ('GHE20260517130950', 'TOA20260509055512', 15);
INSERT INTO Ghe VALUES ('GHE20260517151150', 'TOA20260509055512', 16); INSERT INTO Ghe VALUES ('GHE20260517171349', 'TOA20260509055512', 17); INSERT INTO Ghe VALUES ('GHE20260517191548', 'TOA20260509055512', 18);
INSERT INTO Ghe VALUES ('GHE20260517211747', 'TOA20260509055512', 19); INSERT INTO Ghe VALUES ('GHE20260517231946', 'TOA20260509055512', 20); INSERT INTO Ghe VALUES ('GHE20260518012145', 'TOA20260509055512', 21);
INSERT INTO Ghe VALUES ('GHE20260518032345', 'TOA20260509055512', 22); INSERT INTO Ghe VALUES ('GHE20260518052544', 'TOA20260509055512', 23); INSERT INTO Ghe VALUES ('GHE20260518072743', 'TOA20260509055512', 24);
INSERT INTO Ghe VALUES ('GHE20260518092942', 'TOA20260509055512', 25); INSERT INTO Ghe VALUES ('GHE20260518113141', 'TOA20260509055512', 26); INSERT INTO Ghe VALUES ('GHE20260518133340', 'TOA20260509055512', 27);
INSERT INTO Ghe VALUES ('GHE20260518153540', 'TOA20260509055512', 28); INSERT INTO Ghe VALUES ('GHE20260518173739', 'TOA20260509055512', 29); INSERT INTO Ghe VALUES ('GHE20260518193938', 'TOA20260509055512', 30);
INSERT INTO Ghe VALUES ('GHE20260518214137', 'TOA20260509055512', 31); INSERT INTO Ghe VALUES ('GHE20260518234336', 'TOA20260509055512', 32); INSERT INTO Ghe VALUES ('GHE20260519014536', 'TOA20260509055512', 33);
INSERT INTO Ghe VALUES ('GHE20260519034735', 'TOA20260509055512', 34); INSERT INTO Ghe VALUES ('GHE20260519054934', 'TOA20260509055512', 35); INSERT INTO Ghe VALUES ('GHE20260519075133', 'TOA20260509055512', 36);
INSERT INTO Ghe VALUES ('GHE20260519095332', 'TOA20260509055512', 37); INSERT INTO Ghe VALUES ('GHE20260519115531', 'TOA20260509055512', 38); INSERT INTO Ghe VALUES ('GHE20260519135731', 'TOA20260509055512', 39);
INSERT INTO Ghe VALUES ('GHE20260519155930', 'TOA20260509055512', 40); INSERT INTO Ghe VALUES ('GHE20260519180129', 'TOA20260509055512', 41); INSERT INTO Ghe VALUES ('GHE20260519200328', 'TOA20260509055512', 42);
INSERT INTO Ghe VALUES ('GHE20260519220527', 'TOA20260509055512', 43); INSERT INTO Ghe VALUES ('GHE20260520000726', 'TOA20260509055512', 44); INSERT INTO Ghe VALUES ('GHE20260520020926', 'TOA20260509055512', 45);
INSERT INTO Ghe VALUES ('GHE20260520041125', 'TOA20260509055512', 46); INSERT INTO Ghe VALUES ('GHE20260520061324', 'TOA20260509055512', 47); INSERT INTO Ghe VALUES ('GHE20260520081523', 'TOA20260509055512', 48);
-- TOA20260509075711 (ghe cung): 4x12 = 48 ghe
INSERT INTO Ghe VALUES ('GHE20260520101722', 'TOA20260509075711',  1); INSERT INTO Ghe VALUES ('GHE20260520121921', 'TOA20260509075711',  2); INSERT INTO Ghe VALUES ('GHE20260520142121', 'TOA20260509075711',  3);
INSERT INTO Ghe VALUES ('GHE20260520162320', 'TOA20260509075711',  4); INSERT INTO Ghe VALUES ('GHE20260520182519', 'TOA20260509075711',  5); INSERT INTO Ghe VALUES ('GHE20260520202718', 'TOA20260509075711',  6);
INSERT INTO Ghe VALUES ('GHE20260520222917', 'TOA20260509075711',  7); INSERT INTO Ghe VALUES ('GHE20260521003116', 'TOA20260509075711',  8); INSERT INTO Ghe VALUES ('GHE20260521023316', 'TOA20260509075711',  9);
INSERT INTO Ghe VALUES ('GHE20260521043515', 'TOA20260509075711', 10); INSERT INTO Ghe VALUES ('GHE20260521063714', 'TOA20260509075711', 11); INSERT INTO Ghe VALUES ('GHE20260521083913', 'TOA20260509075711', 12);
INSERT INTO Ghe VALUES ('GHE20260521104112', 'TOA20260509075711', 13); INSERT INTO Ghe VALUES ('GHE20260521124312', 'TOA20260509075711', 14); INSERT INTO Ghe VALUES ('GHE20260521144511', 'TOA20260509075711', 15);
INSERT INTO Ghe VALUES ('GHE20260521164710', 'TOA20260509075711', 16); INSERT INTO Ghe VALUES ('GHE20260521184909', 'TOA20260509075711', 17); INSERT INTO Ghe VALUES ('GHE20260521205108', 'TOA20260509075711', 18);
INSERT INTO Ghe VALUES ('GHE20260521225307', 'TOA20260509075711', 19); INSERT INTO Ghe VALUES ('GHE20260522005507', 'TOA20260509075711', 20); INSERT INTO Ghe VALUES ('GHE20260522025706', 'TOA20260509075711', 21);
INSERT INTO Ghe VALUES ('GHE20260522045905', 'TOA20260509075711', 22); INSERT INTO Ghe VALUES ('GHE20260522070104', 'TOA20260509075711', 23); INSERT INTO Ghe VALUES ('GHE20260522090303', 'TOA20260509075711', 24);
INSERT INTO Ghe VALUES ('GHE20260522110502', 'TOA20260509075711', 25); INSERT INTO Ghe VALUES ('GHE20260522130702', 'TOA20260509075711', 26); INSERT INTO Ghe VALUES ('GHE20260522150901', 'TOA20260509075711', 27);
INSERT INTO Ghe VALUES ('GHE20260522171100', 'TOA20260509075711', 28); INSERT INTO Ghe VALUES ('GHE20260522191259', 'TOA20260509075711', 29); INSERT INTO Ghe VALUES ('GHE20260522211458', 'TOA20260509075711', 30);
INSERT INTO Ghe VALUES ('GHE20260522231657', 'TOA20260509075711', 31); INSERT INTO Ghe VALUES ('GHE20260523011857', 'TOA20260509075711', 32); INSERT INTO Ghe VALUES ('GHE20260523032056', 'TOA20260509075711', 33);
INSERT INTO Ghe VALUES ('GHE20260523052255', 'TOA20260509075711', 34); INSERT INTO Ghe VALUES ('GHE20260523072454', 'TOA20260509075711', 35); INSERT INTO Ghe VALUES ('GHE20260523092653', 'TOA20260509075711', 36);
INSERT INTO Ghe VALUES ('GHE20260523112852', 'TOA20260509075711', 37); INSERT INTO Ghe VALUES ('GHE20260523133052', 'TOA20260509075711', 38); INSERT INTO Ghe VALUES ('GHE20260523153251', 'TOA20260509075711', 39);
INSERT INTO Ghe VALUES ('GHE20260523173450', 'TOA20260509075711', 40); INSERT INTO Ghe VALUES ('GHE20260523193649', 'TOA20260509075711', 41); INSERT INTO Ghe VALUES ('GHE20260523213848', 'TOA20260509075711', 42);
INSERT INTO Ghe VALUES ('GHE20260523234048', 'TOA20260509075711', 43); INSERT INTO Ghe VALUES ('GHE20260524014247', 'TOA20260509075711', 44); INSERT INTO Ghe VALUES ('GHE20260524034446', 'TOA20260509075711', 45);
INSERT INTO Ghe VALUES ('GHE20260524054645', 'TOA20260509075711', 46); INSERT INTO Ghe VALUES ('GHE20260524074844', 'TOA20260509075711', 47); INSERT INTO Ghe VALUES ('GHE20260524095043', 'TOA20260509075711', 48);

-- ==================== 10. Lich (thoiGianChay da doi sang INT = so phut) ====================
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH20260524115243', 'TUY20260509095910', 'DT20260512212036', '2026-05-10 06:00:00', 330);   -- 5h30p
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH20260524135442', 'TUY20260509095910', 'DT20260512212036', '2026-05-11 06:00:00', 330);   -- 5h30p
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH20260524155641', 'TUY20260509120109', 'DT20260512232235', '2026-05-10 14:00:00', 360);   -- 6h
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH20260524175840', 'TUY20260509120109', 'DT20260512232235', '2026-05-11 14:00:00', 360);   -- 6h
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH20260524200039', 'TUY20260509140308', 'DT20260513012434', '2026-05-10 08:00:00', 150);   -- 2h30p
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH20260524220238', 'TUY20260509160507', 'DT20260512212036', '2026-05-12 19:00:00', 1020);  -- 17h
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH20260525000438', 'TUY20260509180707', 'DT20260512232235', '2026-05-13 07:00:00', 1020);  -- 17h
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH20260525020637', 'TUY20260510021503', 'DT20260513012434', '2026-05-15 19:00:00', 1980);  -- 33h

-- ==================== 11. Gia ====================
INSERT INTO Gia VALUES ('GIA20260525040836', '2026-05-08', '2026-07-31', N'Bảng giá thường 2026', 0);
INSERT INTO Gia VALUES ('GIA20260525061035', '2026-05-20', '2026-06-05', N'Bảng giá cao điểm cuối tháng 5/2026', 0);
INSERT INTO Gia VALUES ('GIA20260525081234', '2026-06-01', '2026-07-31', N'Bảng giá mùa hè 2026', 0);

-- ==================== 12. ChiTietGia (giaNiemYet da doi sang DECIMAL) ====================
-- Bang gia thuong (GIA20260525040836)
INSERT INTO ChiTietGia VALUES ('CTG20260525101433', 'GIA20260525040836', 'TUY20260509095910', 'GHE_CUNG', 180000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260525121633', 'GIA20260525040836', 'TUY20260509095910', 'GHE_MEM', 250000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260525141832', 'GIA20260525040836', 'TUY20260509095910', 'GIUONG_NAM', 400000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260525162031', 'GIA20260525040836', 'TUY20260509120109', 'GHE_CUNG', 200000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260525182230', 'GIA20260525040836', 'TUY20260509120109', 'GHE_MEM', 280000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260525202429', 'GIA20260525040836', 'TUY20260509120109', 'GIUONG_NAM', 450000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260525222628', 'GIA20260525040836', 'TUY20260509140308', 'GHE_CUNG', 80000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260526002828', 'GIA20260525040836', 'TUY20260509140308', 'GHE_MEM', 120000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260526023027', 'GIA20260525040836', 'TUY20260509140308', 'GIUONG_NAM', 200000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260526043226', 'GIA20260525040836', 'TUY20260509160507', 'GHE_CUNG', 450000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260526063425', 'GIA20260525040836', 'TUY20260509160507', 'GHE_MEM', 600000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260526083624', 'GIA20260525040836', 'TUY20260509160507', 'GIUONG_NAM', 900000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260526103824', 'GIA20260525040836', 'TUY20260509180707', 'GHE_CUNG', 450000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260526124023', 'GIA20260525040836', 'TUY20260509180707', 'GHE_MEM', 600000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260526144222', 'GIA20260525040836', 'TUY20260509180707', 'GIUONG_NAM', 900000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260526164421', 'GIA20260525040836', 'TUY20260510021503', 'GHE_CUNG', 800000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260526184620', 'GIA20260525040836', 'TUY20260510021503', 'GHE_MEM', 1100000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260526204819', 'GIA20260525040836', 'TUY20260510021503', 'GIUONG_NAM', 1600000.00);
-- Bang gia thuong (GIA20260525040836) cho cac tuyen trung gian moi (gia xap xi thuc te VNR 2024)
-- Ghe cung(B) / Ghe mem(C) / Giuong nam(A) - ty le ~1 : 1.45 : 2.2
-- TUY20260510061902: Ha Noi -> Nam Dinh (~87km)
INSERT INTO ChiTietGia VALUES ('CTG20260526225019', 'GIA20260525040836', 'TUY20260510061902', 'GHE_CUNG',  55000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260527005218', 'GIA20260525040836', 'TUY20260510061902', 'GHE_MEM',   80000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260527025417', 'GIA20260525040836', 'TUY20260510061902', 'GIUONG_NAM',125000.00);
-- TUY20260510082101: Nam Dinh -> Ninh Binh (~30km)
INSERT INTO ChiTietGia VALUES ('CTG20260527045616', 'GIA20260525040836', 'TUY20260510082101', 'GHE_CUNG',  25000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260527065815', 'GIA20260525040836', 'TUY20260510082101', 'GHE_MEM',   36000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260527090014', 'GIA20260525040836', 'TUY20260510082101', 'GIUONG_NAM', 56000.00);
-- TUY20260510102300: Ninh Binh -> Thanh Hoa (~57km)
INSERT INTO ChiTietGia VALUES ('CTG20260527110214', 'GIA20260525040836', 'TUY20260510102300', 'GHE_CUNG',  40000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260527130413', 'GIA20260525040836', 'TUY20260510102300', 'GHE_MEM',   58000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260527150612', 'GIA20260525040836', 'TUY20260510102300', 'GIUONG_NAM', 90000.00);
-- TUY20260510122459: Thanh Hoa -> Vinh (~73km)
INSERT INTO ChiTietGia VALUES ('CTG20260527170811', 'GIA20260525040836', 'TUY20260510122459', 'GHE_CUNG',  55000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260527191010', 'GIA20260525040836', 'TUY20260510122459', 'GHE_MEM',   80000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260527211209', 'GIA20260525040836', 'TUY20260510122459', 'GIUONG_NAM',125000.00);
-- TUY20260510142658: Vinh -> Dong Hoi (~166km)
INSERT INTO ChiTietGia VALUES ('CTG20260527231409', 'GIA20260525040836', 'TUY20260510142658', 'GHE_CUNG', 100000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260528011608', 'GIA20260525040836', 'TUY20260510142658', 'GHE_MEM',  145000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260528031807', 'GIA20260525040836', 'TUY20260510142658', 'GIUONG_NAM',225000.00);
-- TUY20260510162857: Dong Hoi -> Dong Ha (~72km)
INSERT INTO ChiTietGia VALUES ('CTG20260528052006', 'GIA20260525040836', 'TUY20260510162857', 'GHE_CUNG',  50000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260528072205', 'GIA20260525040836', 'TUY20260510162857', 'GHE_MEM',   73000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260528092404', 'GIA20260525040836', 'TUY20260510162857', 'GIUONG_NAM',115000.00);
-- TUY20260510183057: Dong Ha -> Hue (~70km)
INSERT INTO ChiTietGia VALUES ('CTG20260528112604', 'GIA20260525040836', 'TUY20260510183057', 'GHE_CUNG',  50000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260528132803', 'GIA20260525040836', 'TUY20260510183057', 'GHE_MEM',   73000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260528153002', 'GIA20260525040836', 'TUY20260510183057', 'GIUONG_NAM',115000.00);
-- TUY20260510223455: Da Nang -> Tam Ky (~72km)
INSERT INTO ChiTietGia VALUES ('CTG20260528173201', 'GIA20260525040836', 'TUY20260510223455', 'GHE_CUNG',  50000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260528193400', 'GIA20260525040836', 'TUY20260510223455', 'GHE_MEM',   73000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260528213600', 'GIA20260525040836', 'TUY20260510223455', 'GIUONG_NAM',115000.00);
-- TUY20260511003654: Tam Ky -> Quang Ngai (~40km)
INSERT INTO ChiTietGia VALUES ('CTG20260528233759', 'GIA20260525040836', 'TUY20260511003654', 'GHE_CUNG',  30000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260529013958', 'GIA20260525040836', 'TUY20260511003654', 'GHE_MEM',   44000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260529034157', 'GIA20260525040836', 'TUY20260511003654', 'GIUONG_NAM', 68000.00);
-- TUY20260511023853: Quang Ngai -> Dieu Tri (~107km)
INSERT INTO ChiTietGia VALUES ('CTG20260529054356', 'GIA20260525040836', 'TUY20260511023853', 'GHE_CUNG',  70000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260529074555', 'GIA20260525040836', 'TUY20260511023853', 'GHE_MEM',  102000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260529094755', 'GIA20260525040836', 'TUY20260511023853', 'GIUONG_NAM',158000.00);
-- TUY20260511044052: Dieu Tri -> Tuy Hoa (~85km)
INSERT INTO ChiTietGia VALUES ('CTG20260529114954', 'GIA20260525040836', 'TUY20260511044052', 'GHE_CUNG',  60000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260529135153', 'GIA20260525040836', 'TUY20260511044052', 'GHE_MEM',   87000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260529155352', 'GIA20260525040836', 'TUY20260511044052', 'GIUONG_NAM',135000.00);
-- TUY20260511064252: Tuy Hoa -> Nha Trang (~104km)
INSERT INTO ChiTietGia VALUES ('CTG20260529175551', 'GIA20260525040836', 'TUY20260511064252', 'GHE_CUNG',  70000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260529195750', 'GIA20260525040836', 'TUY20260511064252', 'GHE_MEM',  102000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260529215950', 'GIA20260525040836', 'TUY20260511064252', 'GIUONG_NAM',158000.00);
-- TUY20260511084451: Nha Trang -> Phan Rang-Thap Cham (~101km)
INSERT INTO ChiTietGia VALUES ('CTG20260530000149', 'GIA20260525040836', 'TUY20260511084451', 'GHE_CUNG',  70000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260530020348', 'GIA20260525040836', 'TUY20260511084451', 'GHE_MEM',  102000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260530040547', 'GIA20260525040836', 'TUY20260511084451', 'GIUONG_NAM',158000.00);
-- TUY20260511104650: Phan Rang -> Bien Hoa (~185km)
INSERT INTO ChiTietGia VALUES ('CTG20260530060746', 'GIA20260525040836', 'TUY20260511104650', 'GHE_CUNG', 115000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260530080945', 'GIA20260525040836', 'TUY20260511104650', 'GHE_MEM',  167000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260530101145', 'GIA20260525040836', 'TUY20260511104650', 'GIUONG_NAM',260000.00);
-- TUY20260511124849: Bien Hoa -> Sai Gon (~32km)
INSERT INTO ChiTietGia VALUES ('CTG20260530121344', 'GIA20260525040836', 'TUY20260511124849', 'GHE_CUNG',  25000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260530141543', 'GIA20260525040836', 'TUY20260511124849', 'GHE_MEM',   36000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260530161742', 'GIA20260525040836', 'TUY20260511124849', 'GIUONG_NAM', 56000.00);
-- Nguoc chieu (TUY20260511145048..041): gia tuong tu chieu di
INSERT INTO ChiTietGia VALUES ('CTG20260530181941', 'GIA20260525040836', 'TUY20260511145048', 'GHE_CUNG',  55000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260530202140', 'GIA20260525040836', 'TUY20260511145048', 'GHE_MEM',   80000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260530222340', 'GIA20260525040836', 'TUY20260511145048', 'GIUONG_NAM',125000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260531002539', 'GIA20260525040836', 'TUY20260511165248', 'GHE_CUNG',  25000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260531022738', 'GIA20260525040836', 'TUY20260511165248', 'GHE_MEM',   36000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260531042937', 'GIA20260525040836', 'TUY20260511165248', 'GIUONG_NAM', 56000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260531063136', 'GIA20260525040836', 'TUY20260511185447', 'GHE_CUNG',  40000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260531083336', 'GIA20260525040836', 'TUY20260511185447', 'GHE_MEM',   58000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260531103535', 'GIA20260525040836', 'TUY20260511185447', 'GIUONG_NAM', 90000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260531123734', 'GIA20260525040836', 'TUY20260511205646', 'GHE_CUNG',  55000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260531143933', 'GIA20260525040836', 'TUY20260511205646', 'GHE_MEM',   80000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260531164132', 'GIA20260525040836', 'TUY20260511205646', 'GIUONG_NAM',125000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260531184331', 'GIA20260525040836', 'TUY20260511225845', 'GHE_CUNG', 100000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260531204531', 'GIA20260525040836', 'TUY20260511225845', 'GHE_MEM',  145000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260531224730', 'GIA20260525040836', 'TUY20260511225845', 'GIUONG_NAM',225000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260601004929', 'GIA20260525040836', 'TUY20260512010044', 'GHE_CUNG',  50000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260601025128', 'GIA20260525040836', 'TUY20260512010044', 'GHE_MEM',   73000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260601045327', 'GIA20260525040836', 'TUY20260512010044', 'GIUONG_NAM',115000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260601065526', 'GIA20260525040836', 'TUY20260512030243', 'GHE_CUNG',  50000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260601085726', 'GIA20260525040836', 'TUY20260512030243', 'GHE_MEM',   73000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260601105925', 'GIA20260525040836', 'TUY20260512030243', 'GIUONG_NAM',115000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260601130124', 'GIA20260525040836', 'TUY20260512050443', 'GHE_CUNG',  50000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260601150323', 'GIA20260525040836', 'TUY20260512050443', 'GHE_MEM',   73000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260601170522', 'GIA20260525040836', 'TUY20260512050443', 'GIUONG_NAM',115000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260601190721', 'GIA20260525040836', 'TUY20260512070642', 'GHE_CUNG',  30000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260601210921', 'GIA20260525040836', 'TUY20260512070642', 'GHE_MEM',   44000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260601231120', 'GIA20260525040836', 'TUY20260512070642', 'GIUONG_NAM', 68000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260602011319', 'GIA20260525040836', 'TUY20260512090841', 'GHE_CUNG',  70000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260602031518', 'GIA20260525040836', 'TUY20260512090841', 'GHE_MEM',  102000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260602051717', 'GIA20260525040836', 'TUY20260512090841', 'GIUONG_NAM',158000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260602071916', 'GIA20260525040836', 'TUY20260512111040', 'GHE_CUNG',  60000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260602092116', 'GIA20260525040836', 'TUY20260512111040', 'GHE_MEM',   87000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260602112315', 'GIA20260525040836', 'TUY20260512111040', 'GIUONG_NAM',135000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260602132514', 'GIA20260525040836', 'TUY20260512131239', 'GHE_CUNG',  70000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260602152713', 'GIA20260525040836', 'TUY20260512131239', 'GHE_MEM',  102000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260602172912', 'GIA20260525040836', 'TUY20260512131239', 'GIUONG_NAM',158000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260602193112', 'GIA20260525040836', 'TUY20260512151438', 'GHE_CUNG',  70000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260602213311', 'GIA20260525040836', 'TUY20260512151438', 'GHE_MEM',  102000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260602233510', 'GIA20260525040836', 'TUY20260512151438', 'GIUONG_NAM',158000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260603013709', 'GIA20260525040836', 'TUY20260512171638', 'GHE_CUNG', 115000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260603033908', 'GIA20260525040836', 'TUY20260512171638', 'GHE_MEM',  167000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260603054107', 'GIA20260525040836', 'TUY20260512171638', 'GIUONG_NAM',260000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260603074307', 'GIA20260525040836', 'TUY20260512191837', 'GHE_CUNG',  25000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260603094506', 'GIA20260525040836', 'TUY20260512191837', 'GHE_MEM',   36000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260603114705', 'GIA20260525040836', 'TUY20260512191837', 'GIUONG_NAM', 56000.00);

-- Bang gia Tet (GIA20260525061035): tang 30%
INSERT INTO ChiTietGia VALUES ('CTG20260603134904', 'GIA20260525061035', 'TUY20260509095910', 'GHE_CUNG', 234000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260603155103', 'GIA20260525061035', 'TUY20260509095910', 'GHE_MEM', 325000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260603175302', 'GIA20260525061035', 'TUY20260509095910', 'GIUONG_NAM', 520000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260603195502', 'GIA20260525061035', 'TUY20260510021503', 'GHE_CUNG', 1040000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260603215701', 'GIA20260525061035', 'TUY20260510021503', 'GHE_MEM', 1430000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260603235900', 'GIA20260525061035', 'TUY20260510021503', 'GIUONG_NAM', 2080000.00);

-- ==================== 13. KhuyenMai (cau truc giong Gia: ky khuyen mai) ====================
INSERT INTO KhuyenMai VALUES ('KM20260604020059', N'Khuyến mãi đối tượng ưu tiên hè 2026', '2026-05-08 00:00:00', '2026-07-31 23:59:59', N'Giảm giá cho trẻ em, sinh viên, người cao tuổi trong mùa hè 2026', 1);
INSERT INTO KhuyenMai VALUES ('KM20260604040258', N'Khuyến mãi cao điểm cuối tháng 5', '2026-05-20 00:00:00', '2026-05-26 23:59:59', N'Chương trình giảm giá giai đoạn cao điểm cuối tháng 5', 0);
INSERT INTO KhuyenMai VALUES ('KM20260604060457', N'Khuyến mãi mùa hè 2026', '2026-06-01 00:00:00', '2026-07-31 23:59:59', N'Giảm giá mùa hè khuyến khích du lịch bằng tàu hỏa', 0);
INSERT INTO KhuyenMai VALUES ('KM20260604080657', N'Khuyến mãi cao điểm tháng 7', '2026-07-10 00:00:00', '2026-07-25 23:59:59', N'Giảm giá cho hành khách đặt vé giai đoạn cao điểm tháng 7', 0);
INSERT INTO KhuyenMai VALUES ('KM20260604100856', N'Khuyến mãi cuối tháng 7', '2026-07-24 00:00:00', '2026-07-31 23:59:59', N'Chương trình giảm giá tuần cuối tháng 7', 0);

-- ==================== 13b. ChiTietKhuyenMai ====================
-- KM20260604020059: Uu tien - giam tuy loai ghe (ghe cung giam it, giuong nam giam nhieu hon)
-- Tre em: giam 20-25%, Sinh vien: giam 10-15%, Nguoi cao tuoi: giam 15-20%
-- Ap dung cho 10 tuyen express chinh (TUY20260509095910 den TUY20260510041702)
-- Uu tien - ghe cung 20%
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260604121055', 'KM20260604020059', 'TUY20260509095910', 'GHE_CUNG', N'Ưu tiên - Ghế cứng TUY20260509095910', 0.20);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260604141254', 'KM20260604020059', 'TUY20260509120109', 'GHE_CUNG', N'Ưu tiên - Ghế cứng TUY20260509120109', 0.20);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260604161453', 'KM20260604020059', 'TUY20260509140308', 'GHE_CUNG', N'Ưu tiên - Ghế cứng TUY20260509140308', 0.20);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260604181652', 'KM20260604020059', 'TUY20260509160507', 'GHE_CUNG', N'Ưu tiên - Ghế cứng TUY20260509160507', 0.20);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260604201852', 'KM20260604020059', 'TUY20260509180707', 'GHE_CUNG', N'Ưu tiên - Ghế cứng TUY20260509180707', 0.20);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260604222051', 'KM20260604020059', 'TUY20260509200906', 'GHE_CUNG', N'Ưu tiên - Ghế cứng TUY20260509200906', 0.20);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260605002250', 'KM20260604020059', 'TUY20260509221105', 'GHE_CUNG', N'Ưu tiên - Ghế cứng TUY20260509221105', 0.20);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260605022449', 'KM20260604020059', 'TUY20260510001304', 'GHE_CUNG', N'Ưu tiên - Ghế cứng TUY20260510001304', 0.20);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260605042648', 'KM20260604020059', 'TUY20260510021503', 'GHE_CUNG', N'Ưu tiên - Ghế cứng TUY20260510021503', 0.20);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260605062848', 'KM20260604020059', 'TUY20260510041702', 'GHE_CUNG', N'Ưu tiên - Ghế cứng TUY20260510041702', 0.20);
-- Uu tien - ghe mem 22%
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260605083047', 'KM20260604020059', 'TUY20260509095910', 'GHE_MEM', N'Ưu tiên - Ghế mềm TUY20260509095910', 0.22);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260605103246', 'KM20260604020059', 'TUY20260509120109', 'GHE_MEM', N'Ưu tiên - Ghế mềm TUY20260509120109', 0.22);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260605123445', 'KM20260604020059', 'TUY20260509140308', 'GHE_MEM', N'Ưu tiên - Ghế mềm TUY20260509140308', 0.22);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260605143644', 'KM20260604020059', 'TUY20260509160507', 'GHE_MEM', N'Ưu tiên - Ghế mềm TUY20260509160507', 0.22);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260605163843', 'KM20260604020059', 'TUY20260509180707', 'GHE_MEM', N'Ưu tiên - Ghế mềm TUY20260509180707', 0.22);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260605184043', 'KM20260604020059', 'TUY20260509200906', 'GHE_MEM', N'Ưu tiên - Ghế mềm TUY20260509200906', 0.22);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260605204242', 'KM20260604020059', 'TUY20260509221105', 'GHE_MEM', N'Ưu tiên - Ghế mềm TUY20260509221105', 0.22);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260605224441', 'KM20260604020059', 'TUY20260510001304', 'GHE_MEM', N'Ưu tiên - Ghế mềm TUY20260510001304', 0.22);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260606004640', 'KM20260604020059', 'TUY20260510021503', 'GHE_MEM', N'Ưu tiên - Ghế mềm TUY20260510021503', 0.22);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260606024839', 'KM20260604020059', 'TUY20260510041702', 'GHE_MEM', N'Ưu tiên - Ghế mềm TUY20260510041702', 0.22);
-- Uu tien - giuong nam 25%
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260606045038', 'KM20260604020059', 'TUY20260509095910', 'GIUONG_NAM', N'Ưu tiên - Giường nằm TUY20260509095910', 0.25);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260606065238', 'KM20260604020059', 'TUY20260509120109', 'GIUONG_NAM', N'Ưu tiên - Giường nằm TUY20260509120109', 0.25);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260606085437', 'KM20260604020059', 'TUY20260509140308', 'GIUONG_NAM', N'Ưu tiên - Giường nằm TUY20260509140308', 0.25);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260606105636', 'KM20260604020059', 'TUY20260509160507', 'GIUONG_NAM', N'Ưu tiên - Giường nằm TUY20260509160507', 0.25);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260606125835', 'KM20260604020059', 'TUY20260509180707', 'GIUONG_NAM', N'Ưu tiên - Giường nằm TUY20260509180707', 0.25);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260606150034', 'KM20260604020059', 'TUY20260509200906', 'GIUONG_NAM', N'Ưu tiên - Giường nằm TUY20260509200906', 0.25);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260606170233', 'KM20260604020059', 'TUY20260509221105', 'GIUONG_NAM', N'Ưu tiên - Giường nằm TUY20260509221105', 0.25);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260606190433', 'KM20260604020059', 'TUY20260510001304', 'GIUONG_NAM', N'Ưu tiên - Giường nằm TUY20260510001304', 0.25);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260606210632', 'KM20260604020059', 'TUY20260510021503', 'GIUONG_NAM', N'Ưu tiên - Giường nằm TUY20260510021503', 0.25);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260606230831', 'KM20260604020059', 'TUY20260510041702', 'GIUONG_NAM', N'Ưu tiên - Giường nằm TUY20260510041702', 0.25);

-- KM20260604040258: Le 30/4 - giam 10% dong deu cho cac tuyen chinh
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260607011030', 'KM20260604040258', 'TUY20260509095910', 'GHE_CUNG', N'Lễ 30/4 - Ghế cứng TUY20260509095910', 0.10);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260607031229', 'KM20260604040258', 'TUY20260509095910', 'GHE_MEM', N'Lễ 30/4 - Ghế mềm TUY20260509095910', 0.10);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260607051428', 'KM20260604040258', 'TUY20260509095910', 'GIUONG_NAM', N'Lễ 30/4 - Giường nằm TUY20260509095910', 0.10);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260607071628', 'KM20260604040258', 'TUY20260509120109', 'GHE_CUNG', N'Lễ 30/4 - Ghế cứng TUY20260509120109', 0.10);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260607091827', 'KM20260604040258', 'TUY20260509120109', 'GHE_MEM', N'Lễ 30/4 - Ghế mềm TUY20260509120109', 0.10);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260607112026', 'KM20260604040258', 'TUY20260509120109', 'GIUONG_NAM', N'Lễ 30/4 - Giường nằm TUY20260509120109', 0.10);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260607132225', 'KM20260604040258', 'TUY20260509140308', 'GHE_CUNG', N'Lễ 30/4 - Ghế cứng TUY20260509140308', 0.10);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260607152424', 'KM20260604040258', 'TUY20260509140308', 'GHE_MEM', N'Lễ 30/4 - Ghế mềm TUY20260509140308', 0.10);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260607172624', 'KM20260604040258', 'TUY20260509140308', 'GIUONG_NAM', N'Lễ 30/4 - Giường nằm TUY20260509140308', 0.10);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260607192823', 'KM20260604040258', 'TUY20260509160507', 'GHE_CUNG', N'Lễ 30/4 - Ghế cứng TUY20260509160507', 0.10);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260607213022', 'KM20260604040258', 'TUY20260509160507', 'GHE_MEM', N'Lễ 30/4 - Ghế mềm TUY20260509160507', 0.10);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260607233221', 'KM20260604040258', 'TUY20260509160507', 'GIUONG_NAM', N'Lễ 30/4 - Giường nằm TUY20260509160507', 0.10);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260608013420', 'KM20260604040258', 'TUY20260510021503', 'GHE_CUNG', N'Lễ 30/4 - Ghế cứng TUY20260510021503', 0.12);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260608033619', 'KM20260604040258', 'TUY20260510021503', 'GHE_MEM', N'Lễ 30/4 - Ghế mềm TUY20260510021503', 0.12);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260608053819', 'KM20260604040258', 'TUY20260510021503', 'GIUONG_NAM', N'Lễ 30/4 - Giường nằm TUY20260510021503', 0.12);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260608074018', 'KM20260604040258', 'TUY20260510041702', 'GHE_CUNG', N'Lễ 30/4 - Ghế cứng TUY20260510041702', 0.12);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260608094217', 'KM20260604040258', 'TUY20260510041702', 'GHE_MEM', N'Lễ 30/4 - Ghế mềm TUY20260510041702', 0.12);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260608114416', 'KM20260604040258', 'TUY20260510041702', 'GIUONG_NAM', N'Lễ 30/4 - Giường nằm TUY20260510041702', 0.12);

-- KM20260604060457: Mua he - giam nhieu hon cho giuong nam de khuyen khich du lich
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260608134615', 'KM20260604060457', 'TUY20260509095910', 'GHE_CUNG', N'Mùa hè - Ghế cứng TUY20260509095910', 0.05);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260608154814', 'KM20260604060457', 'TUY20260509095910', 'GHE_MEM', N'Mùa hè - Ghế mềm TUY20260509095910', 0.07);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260608175014', 'KM20260604060457', 'TUY20260509095910', 'GIUONG_NAM', N'Mùa hè - Giường nằm TUY20260509095910', 0.10);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260608195213', 'KM20260604060457', 'TUY20260509120109', 'GHE_CUNG', N'Mùa hè - Ghế cứng TUY20260509120109', 0.05);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260608215412', 'KM20260604060457', 'TUY20260509120109', 'GHE_MEM', N'Mùa hè - Ghế mềm TUY20260509120109', 0.07);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260608235611', 'KM20260604060457', 'TUY20260509120109', 'GIUONG_NAM', N'Mùa hè - Giường nằm TUY20260509120109', 0.10);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260609015810', 'KM20260604060457', 'TUY20260509140308', 'GHE_CUNG', N'Mùa hè - Ghế cứng TUY20260509140308', 0.08);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260609040009', 'KM20260604060457', 'TUY20260509140308', 'GHE_MEM', N'Mùa hè - Ghế mềm TUY20260509140308', 0.10);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260609060209', 'KM20260604060457', 'TUY20260509140308', 'GIUONG_NAM', N'Mùa hè - Giường nằm TUY20260509140308', 0.15);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260609080408', 'KM20260604060457', 'TUY20260509160507', 'GHE_CUNG', N'Mùa hè - Ghế cứng TUY20260509160507', 0.05);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260609100607', 'KM20260604060457', 'TUY20260509160507', 'GHE_MEM', N'Mùa hè - Ghế mềm TUY20260509160507', 0.07);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260609120806', 'KM20260604060457', 'TUY20260509160507', 'GIUONG_NAM', N'Mùa hè - Giường nằm TUY20260509160507', 0.10);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260609141005', 'KM20260604060457', 'TUY20260509180707', 'GHE_CUNG', N'Mùa hè - Ghế cứng TUY20260509180707', 0.05);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260609161204', 'KM20260604060457', 'TUY20260509180707', 'GHE_MEM', N'Mùa hè - Ghế mềm TUY20260509180707', 0.07);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260609181404', 'KM20260604060457', 'TUY20260509180707', 'GIUONG_NAM', N'Mùa hè - Giường nằm TUY20260509180707', 0.10);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260609201603', 'KM20260604060457', 'TUY20260510021503', 'GHE_CUNG', N'Mùa hè - Ghế cứng TUY20260510021503', 0.08);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260609221802', 'KM20260604060457', 'TUY20260510021503', 'GHE_MEM', N'Mùa hè - Ghế mềm TUY20260510021503', 0.10);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260610002001', 'KM20260604060457', 'TUY20260510021503', 'GIUONG_NAM', N'Mùa hè - Giường nằm TUY20260510021503', 0.15);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260610022200', 'KM20260604060457', 'TUY20260510041702', 'GHE_CUNG', N'Mùa hè - Ghế cứng TUY20260510041702', 0.08);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260610042400', 'KM20260604060457', 'TUY20260510041702', 'GHE_MEM', N'Mùa hè - Ghế mềm TUY20260510041702', 0.10);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260610062559', 'KM20260604060457', 'TUY20260510041702', 'GIUONG_NAM', N'Mùa hè - Giường nằm TUY20260510041702', 0.15);

-- KM20260604080657: Tet 2027 - giam manh cho tuyen xuyen Viet
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260610082758', 'KM20260604080657', 'TUY20260510021503', 'GHE_CUNG', N'Tết 2027 - Ghế cứng TUY20260510021503', 0.15);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260610102957', 'KM20260604080657', 'TUY20260510021503', 'GHE_MEM', N'Tết 2027 - Ghế mềm TUY20260510021503', 0.15);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260610123156', 'KM20260604080657', 'TUY20260510021503', 'GIUONG_NAM', N'Tết 2027 - Giường nằm TUY20260510021503', 0.20);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260610143355', 'KM20260604080657', 'TUY20260510041702', 'GHE_CUNG', N'Tết 2027 - Ghế cứng TUY20260510041702', 0.15);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260610163555', 'KM20260604080657', 'TUY20260510041702', 'GHE_MEM', N'Tết 2027 - Ghế mềm TUY20260510041702', 0.15);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260610183754', 'KM20260604080657', 'TUY20260510041702', 'GIUONG_NAM', N'Tết 2027 - Giường nằm TUY20260510041702', 0.20);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260610203953', 'KM20260604080657', 'TUY20260509095910', 'GHE_CUNG', N'Tết 2027 - Ghế cứng TUY20260509095910', 0.10);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260610224152', 'KM20260604080657', 'TUY20260509095910', 'GHE_MEM', N'Tết 2027 - Ghế mềm TUY20260509095910', 0.10);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260611004351', 'KM20260604080657', 'TUY20260509095910', 'GIUONG_NAM', N'Tết 2027 - Giường nằm TUY20260509095910', 0.12);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260611024550', 'KM20260604080657', 'TUY20260509160507', 'GHE_CUNG', N'Tết 2027 - Ghế cứng TUY20260509160507', 0.10);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260611044750', 'KM20260604080657', 'TUY20260509160507', 'GHE_MEM', N'Tết 2027 - Ghế mềm TUY20260509160507', 0.10);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260611064949', 'KM20260604080657', 'TUY20260509160507', 'GIUONG_NAM', N'Tết 2027 - Giường nằm TUY20260509160507', 0.12);

-- KM20260604100856: Quoc khanh 2/9 - giam 8% cho tat ca tuyen chinh
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260611085148', 'KM20260604100856', 'TUY20260509095910', 'GHE_CUNG', N'Quốc khánh - Ghế cứng TUY20260509095910', 0.08);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260611105347', 'KM20260604100856', 'TUY20260509095910', 'GHE_MEM', N'Quốc khánh - Ghế mềm TUY20260509095910', 0.08);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260611125546', 'KM20260604100856', 'TUY20260509095910', 'GIUONG_NAM', N'Quốc khánh - Giường nằm TUY20260509095910', 0.08);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260611145745', 'KM20260604100856', 'TUY20260509120109', 'GHE_CUNG', N'Quốc khánh - Ghế cứng TUY20260509120109', 0.08);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260611165945', 'KM20260604100856', 'TUY20260509120109', 'GHE_MEM', N'Quốc khánh - Ghế mềm TUY20260509120109', 0.08);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260611190144', 'KM20260604100856', 'TUY20260509120109', 'GIUONG_NAM', N'Quốc khánh - Giường nằm TUY20260509120109', 0.08);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260611210343', 'KM20260604100856', 'TUY20260509140308', 'GHE_CUNG', N'Quốc khánh - Ghế cứng TUY20260509140308', 0.08);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260611230542', 'KM20260604100856', 'TUY20260509140308', 'GHE_MEM', N'Quốc khánh - Ghế mềm TUY20260509140308', 0.08);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260612010741', 'KM20260604100856', 'TUY20260509140308', 'GIUONG_NAM', N'Quốc khánh - Giường nằm TUY20260509140308', 0.08);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260612030940', 'KM20260604100856', 'TUY20260510021503', 'GHE_CUNG', N'Quốc khánh - Ghế cứng TUY20260510021503', 0.10);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260612051140', 'KM20260604100856', 'TUY20260510021503', 'GHE_MEM', N'Quốc khánh - Ghế mềm TUY20260510021503', 0.10);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260612071339', 'KM20260604100856', 'TUY20260510021503', 'GIUONG_NAM', N'Quốc khánh - Giường nằm TUY20260510021503', 0.12);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260612091538', 'KM20260604100856', 'TUY20260510041702', 'GHE_CUNG', N'Quốc khánh - Ghế cứng TUY20260510041702', 0.10);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260612111737', 'KM20260604100856', 'TUY20260510041702', 'GHE_MEM', N'Quốc khánh - Ghế mềm TUY20260510041702', 0.10);
INSERT INTO ChiTietKhuyenMai (maChiTietKM, maKhuyenMai, maTuyen, loaiGhe, tenChiTiet, phanTramGiam) VALUES ('CTKM20260612131936', 'KM20260604100856', 'TUY20260510041702', 'GIUONG_NAM', N'Quốc khánh - Giường nằm TUY20260510041702', 0.12);

-- ==================== 14. Ve (CAU TRUC MOI: maLich + maGhe thay vi maHoaDon) ====================
-- HD20260612152136: Pham Minh Tuan mua 2 ve ghe mem LCH20260524115243
INSERT INTO Ve VALUES ('VE20260612172335', 'LCH20260524115243', 'GHE20260520101722', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE20260612192534', 'LCH20260524115243', 'GHE20260520121921', 'DA_BAN', NULL, NULL);
-- HD20260612212733: Hoang Duc Manh mua 1 ve giuong nam LCH20260524155641
INSERT INTO Ve VALUES ('VE20260612232932', 'LCH20260524155641', 'GHE20260514035024', 'DA_BAN', NULL, NULL);
-- HD20260613013131: Nguyen Thi Hoa mua 1 ve ghe cung LCH20260524200039 (sinh vien)
INSERT INTO Ve VALUES ('VE20260613033331', 'LCH20260524200039', 'GHE20260520182519', 'DA_BAN', NULL, NULL);
-- HD20260613053530: Tran Van Duc mua 2 cap ve noi chuyen HN->Vinh->Hue
--   Cap 1: cho Tran Van Duc
INSERT INTO Ve VALUES ('VE20260613073729', 'LCH20260524135442', 'GHE20260513194227', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE20260613093928', 'LCH20260524175840', 'GHE20260516084202', 'DA_BAN', NULL, NULL);
--   Cap 2: cho Tran Thi Mai
INSERT INTO Ve VALUES ('VE20260613114127', 'LCH20260524135442', 'GHE20260513214426', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE20260613134326', 'LCH20260524175840', 'GHE20260516104401', 'DA_BAN', NULL, NULL);
-- HD20260613154526: Ly Van Hung mua 1 ve ghe mem LCH20260524220238, sau do huy
INSERT INTO Ve VALUES ('VE20260613174725', 'LCH20260524220238', 'GHE20260520101722', 'DA_HUY', N'Hành khách thay đổi kế hoạch', '2026-05-10 10:30:00');

-- ==================== 15. HoaDon (tach KhachHang sang HoaDonKhachHang) ====================
INSERT INTO HoaDon VALUES ('HD20260612152136', 'NV20260504120358', '2026-05-08 09:15:00');
INSERT INTO HoaDon VALUES ('HD20260612212733', 'NV20260504140557', '2026-05-08 10:30:00');
INSERT INTO HoaDon VALUES ('HD20260613013131', 'NV20260504120358', '2026-05-09 14:00:00');
INSERT INTO HoaDon VALUES ('HD20260613053530', 'NV20260504140557', '2026-05-09 16:00:00');
INSERT INTO HoaDon VALUES ('HD20260613154526', 'NV20260504120358', '2026-05-10 08:00:00');

-- ==================== 15b. HoaDonKhachHang (junction) ====================
INSERT INTO HoaDonKhachHang VALUES ('HDKH20260613194924', 'HD20260612152136', 'KH20260506185736');
INSERT INTO HoaDonKhachHang VALUES ('HDKH20260613215123', 'HD20260612212733', 'KH20260506205935');
INSERT INTO HoaDonKhachHang VALUES ('HDKH20260613235322', 'HD20260613013131', 'KH20260506230134');
INSERT INTO HoaDonKhachHang VALUES ('HDKH20260614015521', 'HD20260613053530', 'KH20260507010333');
-- HD20260613053530 demo da-khach (Tran Van Duc + Tran Thi Mai)
INSERT INTO HoaDonKhachHang VALUES ('HDKH20260614035721', 'HD20260613053530', 'KH20260506230134');
INSERT INTO HoaDonKhachHang VALUES ('HDKH20260614055920', 'HD20260613154526', 'KH20260507030532');

-- ==================== 16. ChiTietHoaDon (BANG MOI thay the ChiTietVe) ====================
-- HD20260612152136: 2 ve ghe mem TUY20260509095910 (250k/ve)
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD20260614140716', 'HD20260612152136', 'VE20260612172335', ctg.maChiTietGia, 100000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260612152136'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260612172335'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD20260630223037', 'HD20260612212733', 'VE20260612192534', ctg.maChiTietGia, 125000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260612212733'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260612192534'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
-- HD20260612212733: 1 ve giuong nam TUY20260509120109 (450k)
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD20260702151020', 'HD20260613013131', 'VE20260612232932', ctg.maChiTietGia, 150000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260613013131'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260612232932'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
-- HD20260613013131: 1 ve ghe cung TUY20260509140308, sinh vien giam 15% (80k * 0.85 = 68k)
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD20260702171219', 'HD20260613053530', 'VE20260613033331', ctg.maChiTietGia, 175000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260613053530'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260613033331'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
-- HD20260613053530: 4 ve noi chuyen HN->Vinh (180k ghe cung) + Vinh->Hue (200k ghe cung)
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD20260702191419', 'HD20260613154526', 'VE20260613073729', ctg.maChiTietGia, 200000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260613154526'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260613073729'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD20260702211618', 'HD20260613053530', 'VE20260613093928', ctg.maChiTietGia, 225000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260613053530'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260613093928'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD20260702231817', 'HD20260613053530', 'VE20260613114127', ctg.maChiTietGia, 250000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260613053530'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260613114127'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD20260703012016', 'HD20260613053530', 'VE20260613134326', ctg.maChiTietGia, 275000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260613053530'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260613134326'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
-- HD20260613154526: 1 ve ghe mem TUY20260509160507 (600k), da huy
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD20260703032215', 'HD20260613154526', 'VE20260613174725', ctg.maChiTietGia, 300000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260613154526'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260613174725'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;

-- ==================== 17. ApDungKM (FK -> ChiTietHoaDon + ChiTietKhuyenMai) ====================
-- CTHD20260614140716: ve ghe cung TUY20260509140308 (Hue->Da Nang), sinh vien -> ap dung CTKM20260604161453 (KM20260604020059, TUY20260509140308, GHE_CUNG, 20%)
INSERT INTO ApDungKM VALUES ('ADKM20260615021912', 'CTHD20260614140716', 'CTKM20260604161453');

-- ==================== 18. GiuCho ====================
INSERT INTO GiuCho VALUES ('GC20260615042111', 'NV20260504120358', 'LCH20260525020637', 'GHE20260520202718', '2026-05-14 15:05:00');
INSERT INTO GiuCho VALUES ('GC20260615062310', 'NV20260504120358', 'LCH20260525020637', 'GHE20260520222917', '2026-05-14 15:05:00');

-- ============================================================
-- DU LIEU BO SUNG - TANG PHONG PHU CHO CAC BANG CON IT DATA
-- ============================================================

-- ==================== KhachHang (them 15 khach hang) ====================
INSERT INTO KhachHang (maKhachHang, hoTen, cccd, soDienThoai, email, diaChiThuongTru, diaChiTamTru, ngaySinh, gioiTinh, quocTich) VALUES
('KH20260615082509', N'Nguyễn Thị Lan', '001086006789', '0901112223', 'lan.nguyenthi@email.com', N'33 Điện Biên Phủ, TP Huế, Thừa Thiên Huế', NULL, '1997-06-20', 'NU', N'Việt Nam');

INSERT INTO KhachHang (maKhachHang, hoTen, cccd, soDienThoai, email, diaChiThuongTru, diaChiTamTru, ngaySinh, gioiTinh, quocTich) VALUES
('KH20260615102708', N'Trần Minh Khoa', '079086007890', '0912223334', 'khoa.tranminh@email.com', N'72 Nguyễn Văn Linh, Quận 7, TP.HCM', NULL, '1991-01-14', 'NAM', N'Việt Nam');

INSERT INTO KhachHang (maKhachHang, hoTen, cccd, soDienThoai, email, diaChiThuongTru, diaChiTamTru, ngaySinh, gioiTinh, quocTich) VALUES
('KH20260615122907', N'Lê Thị Thu Hà', '046086008901', '0923334445', 'ha.lethithu@email.com', N'19 Lê Thánh Tôn, TP Huế, Thừa Thiên Huế', NULL, '1999-09-28', 'NU', N'Việt Nam');

INSERT INTO KhachHang (maKhachHang, hoTen, cccd, soDienThoai, email, diaChiThuongTru, diaChiTamTru, ngaySinh, gioiTinh, quocTich) VALUES
('KH20260615143107', N'Phạm Văn Bình', '038086009012', '0934445556', 'binh.phamvan@email.com', N'60 Nguyễn Du, TP Vinh, Nghệ An', NULL, '1985-04-05', 'NAM', N'Việt Nam');

INSERT INTO KhachHang (maKhachHang, hoTen, cccd, soDienThoai, email, diaChiThuongTru, diaChiTamTru, ngaySinh, gioiTinh, quocTich) VALUES
('KH20260615163306', N'Hoàng Thị Nga', '048086010123', '0945556667', 'nga.hoangthi@email.com', N'45 Ông Ích Khiêm, Thanh Khê, Đà Nẵng', NULL, '1994-07-11', 'NU', N'Việt Nam');

INSERT INTO KhachHang (maKhachHang, hoTen, cccd, soDienThoai, email, diaChiThuongTru, diaChiTamTru, ngaySinh, gioiTinh, quocTich) VALUES
('KH20260615183505', N'Vũ Đức Mạnh', '001086011234', '0956667778', 'manh.vuduc@email.com', N'8 Phạm Ngọc Thạch, Đống Đa, Hà Nội', N'55 Võ Thị Sáu, Quận 1, TP.HCM', '1993-02-19', 'NAM', N'Việt Nam');

INSERT INTO KhachHang (maKhachHang, hoTen, cccd, soDienThoai, email, diaChiThuongTru, diaChiTamTru, ngaySinh, gioiTinh, quocTich) VALUES
('KH20260615203704', N'Đặng Thị Thúy', '079086012345', '0967778889', 'thuy.dangthi@email.com', N'16 Đinh Tiên Hoàng, Quận 1, TP.HCM', NULL, '1996-10-30', 'NU', N'Việt Nam');

INSERT INTO KhachHang (maKhachHang, hoTen, cccd, soDienThoai, email, diaChiThuongTru, diaChiTamTru, ngaySinh, gioiTinh, quocTich) VALUES
('KH20260615223903', N'Bùi Văn Thành', '048086013456', '0978889990', 'thanh.buivan@email.com', N'30 Lê Lợi, Hải Châu, Đà Nẵng', NULL, '1990-08-08', 'NAM', N'Việt Nam');

INSERT INTO KhachHang (maKhachHang, hoTen, cccd, soDienThoai, email, diaChiThuongTru, diaChiTamTru, ngaySinh, gioiTinh, quocTich) VALUES
('KH20260616004102', N'Ngô Thị Hương', '079086014567', '0989990001', 'huong.ngothi@email.com', N'88 Cách Mạng Tháng 8, Quận 3, TP.HCM', NULL, '1998-03-25', 'NU', N'Việt Nam');

INSERT INTO KhachHang (maKhachHang, hoTen, cccd, soDienThoai, email, diaChiThuongTru, diaChiTamTru, ngaySinh, gioiTinh, quocTich) VALUES
('KH20260616024302', N'Đinh Minh Hiếu', '001086015678', '0990001112', 'hieu.dinhminh@email.com', N'14 Kim Liên, Đống Đa, Hà Nội', NULL, '1992-12-01', 'NAM', N'Việt Nam');

INSERT INTO KhachHang (maKhachHang, hoTen, cccd, soDienThoai, email, diaChiThuongTru, diaChiTamTru, ngaySinh, gioiTinh, quocTich) VALUES
('KH20260616044501', N'Lương Thị Ánh', '038086016789', '0901221332', 'anh.luongthi@email.com', N'77 Đinh Công Tráng, TP Vinh, Nghệ An', NULL, '1995-05-17', 'NU', N'Việt Nam');

INSERT INTO KhachHang (maKhachHang, hoTen, cccd, soDienThoai, email, diaChiThuongTru, diaChiTamTru, ngaySinh, gioiTinh, quocTich) VALUES
('KH20260616064700', N'Tạ Văn Phúc', '046086017890', '0912332443', 'phuc.tavan@email.com', N'22 Hùng Vương, TP Huế, Thừa Thiên Huế', NULL, '1987-07-09', 'NAM', N'Việt Nam');

INSERT INTO KhachHang (maKhachHang, hoTen, cccd, soDienThoai, email, diaChiThuongTru, diaChiTamTru, ngaySinh, gioiTinh, quocTich) VALUES
('KH20260616084859', N'Kiều Thị Bích', '048086018901', '0923443554', 'bich.kieuthi@email.com', N'63 Trần Quý Cáp, Hải Châu, Đà Nẵng', NULL, '1999-11-12', 'NU', N'Việt Nam');

INSERT INTO KhachHang (maKhachHang, hoTen, cccd, soDienThoai, email, diaChiThuongTru, diaChiTamTru, ngaySinh, gioiTinh, quocTich) VALUES
('KH20260616105058', N'Châu Minh Tuấn', '079086019012', '0934554665', 'tuan.chau@email.com', N'40 Đinh Tiên Hoàng, Bình Thạnh, TP.HCM', NULL, '1991-06-22', 'NAM', N'Việt Nam');

INSERT INTO KhachHang (maKhachHang, hoTen, cccd, soDienThoai, email, diaChiThuongTru, diaChiTamTru, ngaySinh, gioiTinh, quocTich) VALUES
('KH20260616125257', N'Dương Văn Long', '038086020123', '0945665776', 'long.duongvan@email.com', N'25 Nguyễn Sỹ Sách, TP Vinh, Nghệ An', N'10 Lê Duẩn, Hoàn Kiếm, Hà Nội', '1994-01-03', 'NAM', N'Việt Nam');

-- ==================== DoanTau (them 5 doan tau) ====================
-- SE3/SE4: song hanh voi SE1/SE2 tren tuyen xuyen Viet
-- SE5/SE8: Hanoi <-> Da Nang (ngan hon)
-- TN1: Thong Nhat 1 - chay cham hon SE, dung nhieu ga hon
INSERT INTO DoanTau (maDoanTau, tenDoanTau, maDauMay, trangThai) VALUES ('DT20260616145457', N'SE3 (Hà Nội → Sài Gòn)', 'DM20260508113719', N'Đang hoạt động');
INSERT INTO DoanTau (maDoanTau, tenDoanTau, maDauMay, trangThai) VALUES ('DT20260616165656', N'SE4 (Sài Gòn → Hà Nội)', 'DM20260508133918', N'Đang hoạt động');
INSERT INTO DoanTau (maDoanTau, tenDoanTau, maDauMay, trangThai) VALUES ('DT20260616185855', N'SE5 (Hà Nội → Đà Nẵng)', 'DM20260508154117', N'Đang hoạt động');
INSERT INTO DoanTau (maDoanTau, tenDoanTau, maDauMay, trangThai) VALUES ('DT20260616210054', N'SE8 (Đà Nẵng → Hà Nội)', 'DM20260508174316', N'Đang hoạt động');
INSERT INTO DoanTau (maDoanTau, tenDoanTau, maDauMay, trangThai) VALUES ('DT20260616230253', N'TN1 (Hà Nội → Sài Gòn)', 'DM20260508194516', N'Đang hoạt động');

-- ==================== ChiTietDoanTau (thanh phan toa cho doan tau moi) ====================
-- DT20260616145457 (SE3): toa cung(1) + toa mem(2) + giuong nam(3)
INSERT INTO ChiTietDoanTau VALUES ('CTDT20260617010452', 'DT20260616145457', 'TOA20260509075711', 1);
INSERT INTO ChiTietDoanTau VALUES ('CTDT20260617030652', 'DT20260616145457', 'TOA20260509055512', 2);
INSERT INTO ChiTietDoanTau VALUES ('CTDT20260617050851', 'DT20260616145457', 'TOA20260509035312', 3);
-- DT20260616165656 (SE4): toa cung(1) + toa mem(2) + giuong nam(3)
INSERT INTO ChiTietDoanTau VALUES ('CTDT20260617071050', 'DT20260616165656', 'TOA20260509075711', 1);
INSERT INTO ChiTietDoanTau VALUES ('CTDT20260617091249', 'DT20260616165656', 'TOA20260509055512', 2);
INSERT INTO ChiTietDoanTau VALUES ('CTDT20260617111448', 'DT20260616165656', 'TOA20260509035312', 3);
-- DT20260616185855 (SE5): tau ngan, chi co toa cung + toa mem (khong co giuong nam)
INSERT INTO ChiTietDoanTau VALUES ('CTDT20260617131648', 'DT20260616185855', 'TOA20260509075711', 1);
INSERT INTO ChiTietDoanTau VALUES ('CTDT20260617151847', 'DT20260616185855', 'TOA20260509055512', 2);
-- DT20260616210054 (SE8): tuong tu SE5, tau ngan
INSERT INTO ChiTietDoanTau VALUES ('CTDT20260617172046', 'DT20260616210054', 'TOA20260509075711', 1);
INSERT INTO ChiTietDoanTau VALUES ('CTDT20260617192245', 'DT20260616210054', 'TOA20260509055512', 2);
-- DT20260616230253 (TN1): day du 3 loai toa
INSERT INTO ChiTietDoanTau VALUES ('CTDT20260617212444', 'DT20260616230253', 'TOA20260509075711', 1);
INSERT INTO ChiTietDoanTau VALUES ('CTDT20260617232643', 'DT20260616230253', 'TOA20260509055512', 2);
INSERT INTO ChiTietDoanTau VALUES ('CTDT20260618012843', 'DT20260616230253', 'TOA20260509035312', 3);

-- ==================== ChiTietGia (bo sung gia cho cac tuyen chua co) ====================
-- TUY20260509200906: Da Nang -> Hue (~100km) - cung cu ly TUY20260509140308
INSERT INTO ChiTietGia VALUES ('CTG20260618033042', 'GIA20260525040836', 'TUY20260509200906', 'GHE_CUNG',    80000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260618053241', 'GIA20260525040836', 'TUY20260509200906', 'GHE_MEM',    120000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260618073440', 'GIA20260525040836', 'TUY20260509200906', 'GIUONG_NAM', 200000.00);
-- TUY20260509221105: Hue -> Vinh (~368km) - cung cu ly TUY20260509120109
INSERT INTO ChiTietGia VALUES ('CTG20260618093639', 'GIA20260525040836', 'TUY20260509221105', 'GHE_CUNG',  200000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260618113838', 'GIA20260525040836', 'TUY20260509221105', 'GHE_MEM',   280000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260618134038', 'GIA20260525040836', 'TUY20260509221105', 'GIUONG_NAM',450000.00);
-- TUY20260510001304: Vinh -> Ha Noi (~319km) - cung cu ly TUY20260509095910
INSERT INTO ChiTietGia VALUES ('CTG20260618154237', 'GIA20260525040836', 'TUY20260510001304', 'GHE_CUNG',  180000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260618174436', 'GIA20260525040836', 'TUY20260510001304', 'GHE_MEM',   250000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260618194635', 'GIA20260525040836', 'TUY20260510001304', 'GIUONG_NAM',400000.00);
-- TUY20260510041702: Sai Gon -> Ha Noi (~1726km) - cung cu ly TUY20260510021503
INSERT INTO ChiTietGia VALUES ('CTG20260618214834', 'GIA20260525040836', 'TUY20260510041702', 'GHE_CUNG',   800000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260618235033', 'GIA20260525040836', 'TUY20260510041702', 'GHE_MEM',   1100000.00);
INSERT INTO ChiTietGia VALUES ('CTG20260619015233', 'GIA20260525040836', 'TUY20260510041702', 'GIUONG_NAM',1600000.00);

-- ==================== Lich (them 22 lich chay, LCH20260619035432 den LCH20260619055631) ====================
-- Lich thuong ngay (01-20/6/2026)
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH20260619035432', 'TUY20260509095910', 'DT20260616145457', '2026-06-01 06:00:00',  330);  -- SE3 HN->Vinh
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH20260619075830', 'TUY20260510001304', 'DT20260616165656', '2026-06-01 14:00:00',  330);  -- SE4 Vinh->HN
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH20260619100029', 'TUY20260509120109', 'DT20260616145457', '2026-06-01 12:00:00',  360);  -- SE3 Vinh->Hue
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH20260619120228', 'TUY20260509140308', 'DT20260616185855', '2026-06-01 09:00:00',  150);  -- SE5 Hue->DN
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH20260619140428', 'TUY20260509200906', 'DT20260616210054', '2026-06-01 08:00:00',  150);  -- SE8 DN->Hue
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH20260619160627', 'TUY20260510021503', 'DT20260616145457', '2026-06-03 19:00:00', 1980);  -- SE3 HN->SG xuyen Viet
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH20260619180826', 'TUY20260510041702', 'DT20260616165656', '2026-06-05 07:00:00', 1980);  -- SE4 SG->HN xuyen Viet
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH20260619201025', 'TUY20260510021503', 'DT20260616230253', '2026-06-07 20:00:00', 2160);  -- TN1 HN->SG (cham hon SE)
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH20260619221224', 'TUY20260509095910', 'DT20260512232235', '2026-06-10 06:00:00',  330);  -- SE2 HN->Vinh
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH20260620001424', 'TUY20260509160507', 'DT20260512212036', '2026-06-10 19:00:00', 1020);  -- SE1 DN->SG
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH20260620021623', 'TUY20260509180707', 'DT20260512232235', '2026-06-12 07:00:00', 1020);  -- SE2 SG->DN
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH20260620041822', 'TUY20260509120109', 'DT20260512212036', '2026-06-14 08:00:00',  360);  -- SE1 Vinh->Hue
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH20260620062021', 'TUY20260509221105', 'DT20260512232235', '2026-06-14 10:00:00',  360);  -- SE2 Hue->Vinh
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH20260620082220', 'TUY20260509140308', 'DT20260513012434', '2026-06-16 08:00:00',  150);  -- SE7 Hue->DN
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH20260620102419', 'TUY20260509095910', 'DT20260616145457', '2026-06-20 06:00:00',  330);  -- SE3 HN->Vinh
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH20260620122619', 'TUY20260509120109', 'DT20260616145457', '2026-06-20 12:00:00',  360);  -- SE3 Vinh->Hue
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH20260620142818', 'TUY20260509140308', 'DT20260616185855', '2026-06-20 19:00:00',  150);  -- SE5 Hue->DN
-- Lich cao diem thang 7 (nhu cau cao, gia ap dung cac chuong trinh KM demo)
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH20260620163017', 'TUY20260509095910', 'DT20260616145457', '2026-07-08 06:00:00',  330);  -- SE3 HN->Vinh (le)
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH20260620183216', 'TUY20260510021503', 'DT20260616145457', '2026-07-08 19:00:00', 1980);  -- SE3 HN->SG (le)
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH20260620203415', 'TUY20260510041702', 'DT20260616165656', '2026-07-24 07:00:00', 1980);  -- SE4 SG->HN (le)
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH20260620223614', 'TUY20260509160507', 'DT20260616165656', '2026-07-16 20:00:00', 1020);  -- SE4 DN->SG
INSERT INTO Lich (maLich, maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay) VALUES ('LCH20260619055631', 'TUY20260509180707', 'DT20260616145457', '2026-07-31 09:00:00', 1020);  -- SE3 SG->DN (sau le)

-- ==================== Ve (them 52 ve moi, VE20260621003814 den VE20260621024013) ====================
-- LCH20260619035432 (SE3 HN->Vinh 16/4, DT20260616145457 co TOA20260509035312+TOA20260509055512+TOA20260509075711)
INSERT INTO Ve VALUES ('VE20260621003814', 'LCH20260619035432', 'GHE20260521043515', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE20260621044212', 'LCH20260619035432', 'GHE20260521063714', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE20260621064411', 'LCH20260619035432', 'GHE20260514140019', 'DA_BAN', NULL, NULL);
-- LCH20260619100029 (SE3 Vinh->Hue 16/4, DT20260616145457)
INSERT INTO Ve VALUES ('VE20260621084610', 'LCH20260619100029', 'GHE20260517025955', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE20260621104809', 'LCH20260619100029', 'GHE20260517050154', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE20260621125009', 'LCH20260619100029', 'GHE20260517070353', 'DA_BAN', NULL, NULL);
-- LCH20260619120228 (SE5 Hue->DN 16/4, DT20260616185855 chi co TOA20260509055512+TOA20260509075711)
INSERT INTO Ve VALUES ('VE20260621145208', 'LCH20260619120228', 'GHE20260522005507', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE20260621165407', 'LCH20260619120228', 'GHE20260522025706', 'DA_BAN', NULL, NULL);
-- LCH20260619140428 (SE8 DN->Hue 16/4, DT20260616210054 chi co TOA20260509055512+TOA20260509075711)
INSERT INTO Ve VALUES ('VE20260621185606', 'LCH20260619140428', 'GHE20260517130950', 'DA_BAN', NULL, NULL);
-- LCH20260619160627 (SE3 HN->SG xuyen Viet 17/4, DT20260616145457)
INSERT INTO Ve VALUES ('VE20260621205805', 'LCH20260619160627', 'GHE20260514035024', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE20260621230004', 'LCH20260619160627', 'GHE20260514055223', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE20260622010204', 'LCH20260619160627', 'GHE20260514075422', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE20260622030403', 'LCH20260619160627', 'GHE20260514095621', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE20260622050602', 'LCH20260619160627', 'GHE20260517231946', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE20260622070801', 'LCH20260619160627', 'GHE20260518012145', 'DA_BAN', NULL, NULL);
-- LCH20260619180826 (SE4 SG->HN 18/4, DT20260616165656)
INSERT INTO Ve VALUES ('VE20260622091000', 'LCH20260619180826', 'GHE20260521043515', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE20260622111200', 'LCH20260619180826', 'GHE20260521063714', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE20260622131359', 'LCH20260619180826', 'GHE20260521083913', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE20260622151558', 'LCH20260619180826', 'GHE20260515001015', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE20260622171757', 'LCH20260619180826', 'GHE20260515021214', 'DA_BAN', NULL, NULL);
-- LCH20260619201025 (TN1 HN->SG 19/4, DT20260616230253)
INSERT INTO Ve VALUES ('VE20260622191956', 'LCH20260619201025', 'GHE20260520182519', 'DA_BAN', NULL, NULL);
-- LCH20260619221224 (SE2 HN->Vinh 20/4, DT20260512232235)
INSERT INTO Ve VALUES ('VE20260622212155', 'LCH20260619221224', 'GHE20260516164959', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE20260622232355', 'LCH20260619221224', 'GHE20260516185158', 'DA_BAN', NULL, NULL);
-- LCH20260620001424 (SE1 DN->SG 20/4, DT20260512212036)
INSERT INTO Ve VALUES ('VE20260623012554', 'LCH20260620001424', 'GHE20260521144511', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE20260623032753', 'LCH20260620001424', 'GHE20260521164710', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE20260623052952', 'LCH20260620001424', 'GHE20260521184909', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE20260623073151', 'LCH20260620001424', 'GHE20260521205108', 'DA_BAN', NULL, NULL);
-- LCH20260620021623 (SE2 SG->DN 21/4, DT20260512232235) - 1 ve bi huy
INSERT INTO Ve VALUES ('VE20260623093350', 'LCH20260620021623', 'GHE20260515102011', 'DA_HUY', N'Khách thay đổi kế hoạch du lịch', '2026-06-10 14:00:00');
-- LCH20260620041822 (SE1 Vinh->Hue 22/4, DT20260512212036)
INSERT INTO Ve VALUES ('VE20260623113550', 'LCH20260620041822', 'GHE20260522045905', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE20260623133749', 'LCH20260620041822', 'GHE20260522070104', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE20260623153948', 'LCH20260620041822', 'GHE20260522090303', 'DA_BAN', NULL, NULL);
-- LCH20260620062021 (SE2 Hue->Vinh 22/4, DT20260512232235)
INSERT INTO Ve VALUES ('VE20260623174147', 'LCH20260620062021', 'GHE20260518193938', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE20260623194346', 'LCH20260620062021', 'GHE20260518214137', 'DA_BAN', NULL, NULL);
-- LCH20260620082220 (SE7 Hue->DN 23/4, DT20260513012434)
INSERT INTO Ve VALUES ('VE20260623214545', 'LCH20260620082220', 'GHE20260522211458', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE20260623234745', 'LCH20260620082220', 'GHE20260522231657', 'DA_BAN', NULL, NULL);
-- LCH20260620102419 (SE3 HN->Vinh 25/4, DT20260616145457)
INSERT INTO Ve VALUES ('VE20260624014944', 'LCH20260620102419', 'GHE20260519054934', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE20260624035143', 'LCH20260620102419', 'GHE20260519075133', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE20260624055342', 'LCH20260620102419', 'GHE20260519095332', 'DA_BAN', NULL, NULL);
-- LCH20260620122619 (SE3 Vinh->Hue 25/4, DT20260616145457)
INSERT INTO Ve VALUES ('VE20260624075541', 'LCH20260620122619', 'GHE20260515102011', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE20260624095740', 'LCH20260620122619', 'GHE20260515122210', 'DA_BAN', NULL, NULL);
-- LCH20260620163017 (SE3 HN->Vinh 28/4 le 30/4, DT20260616145457): 5 ghe cung - ap dung KM20260604040258 giam 10%
INSERT INTO Ve VALUES ('VE20260624115940', 'LCH20260620163017', 'GHE20260520101722', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE20260624140139', 'LCH20260620163017', 'GHE20260520121921', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE20260624160338', 'LCH20260620163017', 'GHE20260520142121', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE20260624180537', 'LCH20260620163017', 'GHE20260520162320', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE20260624200736', 'LCH20260620163017', 'GHE20260520182519', 'DA_BAN', NULL, NULL);
-- LCH20260620183216 (SE3 HN->SG 28/4 le 30/4, DT20260616145457): 3 giuong nam - ap dung KM20260604040258 giam 12%
INSERT INTO Ve VALUES ('VE20260624220936', 'LCH20260620183216', 'GHE20260513194227', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE20260625001135', 'LCH20260620183216', 'GHE20260513214426', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE20260625021334', 'LCH20260620183216', 'GHE20260513234625', 'DA_BAN', NULL, NULL);
-- LCH20260620203415 (SE4 SG->HN 30/4, DT20260616165656)
INSERT INTO Ve VALUES ('VE20260625041533', 'LCH20260620203415', 'GHE20260517025955', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE20260625061732', 'LCH20260620203415', 'GHE20260517050154', 'DA_BAN', NULL, NULL);
-- LCH20260620223614 (SE4 DN->SG 29/4, DT20260616165656)
INSERT INTO Ve VALUES ('VE20260625081931', 'LCH20260620223614', 'GHE20260521144511', 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE20260621024013', 'LCH20260620223614', 'GHE20260521164710', 'DA_BAN', NULL, NULL);

-- ==================== HoaDon (them 22 hoa don, ngay lap truoc ngay di 1-2 ngay) ====================
INSERT INTO HoaDon VALUES ('HD20260625102131', 'NV20260504120358', '2026-05-14 09:00:00');
INSERT INTO HoaDon VALUES ('HD20260625122330', 'NV20260504140557', '2026-05-14 10:30:00');
INSERT INTO HoaDon VALUES ('HD20260625142529', 'NV20260504201155', '2026-05-14 13:00:00');
INSERT INTO HoaDon VALUES ('HD20260625162728', 'NV20260505041951', '2026-05-15 08:30:00');
INSERT INTO HoaDon VALUES ('HD20260625182927', 'NV20260505082350', '2026-05-15 11:00:00');
INSERT INTO HoaDon VALUES ('HD20260625203126', 'NV20260504120358', '2026-05-15 14:00:00');
INSERT INTO HoaDon VALUES ('HD20260625223326', 'NV20260504140557', '2026-05-15 16:30:00');
INSERT INTO HoaDon VALUES ('HD20260626003525', 'NV20260505142947', '2026-06-01 09:00:00');
INSERT INTO HoaDon VALUES ('HD20260626023724', 'NV20260506044341', '2026-06-01 11:00:00');
INSERT INTO HoaDon VALUES ('HD20260626043923', 'NV20260504120358', '2026-06-03 08:00:00');
INSERT INTO HoaDon VALUES ('HD20260626064122', 'NV20260504140557', '2026-06-05 09:30:00');
INSERT INTO HoaDon VALUES ('HD20260626084321', 'NV20260504201155', '2026-06-05 13:00:00');
INSERT INTO HoaDon VALUES ('HD20260626104521', 'NV20260505041951', '2026-06-07 10:00:00');
INSERT INTO HoaDon VALUES ('HD20260626124720', 'NV20260505082350', '2026-06-07 14:30:00');
INSERT INTO HoaDon VALUES ('HD20260626144919', 'NV20260505203545', '2026-06-10 09:00:00');
INSERT INTO HoaDon VALUES ('HD20260626165118', 'NV20260504120358', '2026-06-10 11:00:00');
INSERT INTO HoaDon VALUES ('HD20260626185317', 'NV20260504140557', '2026-06-16 08:30:00');
INSERT INTO HoaDon VALUES ('HD20260626205516', 'NV20260506024142', '2026-06-16 10:00:00');
INSERT INTO HoaDon VALUES ('HD20260626225716', 'NV20260504120358', '2026-06-24 09:00:00');
INSERT INTO HoaDon VALUES ('HD20260627005915', 'NV20260504140557', '2026-06-24 10:30:00');
INSERT INTO HoaDon VALUES ('HD20260627030114', 'NV20260506044341', '2026-07-01 11:00:00');
INSERT INTO HoaDon VALUES ('HD20260627050313', 'NV20260506064540', '2026-07-16 09:00:00');

-- HoaDonKhachHang junction cho 22 HD moi them o tren
INSERT INTO HoaDonKhachHang VALUES ('HDKH20260627070512', 'HD20260625102131', 'KH20260615082509');
INSERT INTO HoaDonKhachHang VALUES ('HDKH20260627090712', 'HD20260625122330', 'KH20260615102708');
INSERT INTO HoaDonKhachHang VALUES ('HDKH20260627110911', 'HD20260625142529', 'KH20260615122907');
INSERT INTO HoaDonKhachHang VALUES ('HDKH20260627131110', 'HD20260625162728', 'KH20260615143107');
INSERT INTO HoaDonKhachHang VALUES ('HDKH20260627151309', 'HD20260625182927', 'KH20260615163306');
INSERT INTO HoaDonKhachHang VALUES ('HDKH20260627171508', 'HD20260625203126', 'KH20260615183505');
INSERT INTO HoaDonKhachHang VALUES ('HDKH20260627191707', 'HD20260625223326', 'KH20260615203704');
INSERT INTO HoaDonKhachHang VALUES ('HDKH20260627211907', 'HD20260626003525', 'KH20260615223903');
INSERT INTO HoaDonKhachHang VALUES ('HDKH20260627232106', 'HD20260626023724', 'KH20260616004102');
INSERT INTO HoaDonKhachHang VALUES ('HDKH20260628012305', 'HD20260626043923', 'KH20260616024302');
INSERT INTO HoaDonKhachHang VALUES ('HDKH20260628032504', 'HD20260626064122', 'KH20260616044501');
INSERT INTO HoaDonKhachHang VALUES ('HDKH20260628052703', 'HD20260626084321', 'KH20260616064700');
INSERT INTO HoaDonKhachHang VALUES ('HDKH20260628072902', 'HD20260626104521', 'KH20260616084859');
INSERT INTO HoaDonKhachHang VALUES ('HDKH20260628093102', 'HD20260626124720', 'KH20260616105058');
INSERT INTO HoaDonKhachHang VALUES ('HDKH20260628113301', 'HD20260626144919', 'KH20260616125257');
INSERT INTO HoaDonKhachHang VALUES ('HDKH20260628133500', 'HD20260626165118', 'KH20260615082509');
INSERT INTO HoaDonKhachHang VALUES ('HDKH20260628153659', 'HD20260626185317', 'KH20260615102708');
INSERT INTO HoaDonKhachHang VALUES ('HDKH20260628173858', 'HD20260626205516', 'KH20260615122907');
INSERT INTO HoaDonKhachHang VALUES ('HDKH20260628194057', 'HD20260626225716', 'KH20260615183505');
INSERT INTO HoaDonKhachHang VALUES ('HDKH20260628214257', 'HD20260627005915', 'KH20260615203704');
INSERT INTO HoaDonKhachHang VALUES ('HDKH20260628234456', 'HD20260627030114', 'KH20260615223903');
INSERT INTO HoaDonKhachHang VALUES ('HDKH20260629014655', 'HD20260627050313', 'KH20260616004102');

-- ==================== ChiTietHoaDon (them 52 chi tiet, CTHD20260629034854 den CTHD20260629055053) ====================
-- HD20260625102131: 2 ve ghe cung TUY20260509095910 HN->Vinh (180k/ve)
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD20260703052414', 'HD20260625182927', 'VE20260621003814', ctg.maChiTietGia, 100000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260625182927'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260621003814'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD_FIX011', 'HD20260625203126', 'VE20260621044212', ctg.maChiTietGia, 125000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260625203126'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260621044212'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
-- HD20260625122330: 1 ve giuong nam TUY20260509095910 HN->Vinh (400k)
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD_FIX012', 'HD20260625223326', 'VE20260621064411', ctg.maChiTietGia, 150000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260625223326'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260621064411'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
-- HD20260625142529: 3 ve ghe mem TUY20260509120109 Vinh->Hue (280k/ve)
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD_FIX013', 'HD20260626003525', 'VE20260621084610', ctg.maChiTietGia, 175000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260626003525'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260621084610'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD_FIX014', 'HD20260626023724', 'VE20260621104809', ctg.maChiTietGia, 200000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260626023724'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260621104809'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD_FIX015', 'HD20260626043923', 'VE20260621125009', ctg.maChiTietGia, 225000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260626043923'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260621125009'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
-- HD20260625162728: 2 ve ghe cung TUY20260509140308 Hue->DN (80k/ve)
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD_FIX016', 'HD20260626064122', 'VE20260621145208', ctg.maChiTietGia, 250000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260626064122'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260621145208'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD_FIX017', 'HD20260626084321', 'VE20260621165407', ctg.maChiTietGia, 275000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260626084321'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260621165407'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
-- HD20260625182927: 1 ve ghe mem TUY20260509200906 DN->Hue (120k)
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD_FIX018', 'HD20260626104521', 'VE20260621185606', ctg.maChiTietGia, 300000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260626104521'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260621185606'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
-- HD20260625203126: 4 ve giuong nam TUY20260510021503 HN->SG (1,600k/ve)
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD_FIX019', 'HD20260626124720', 'VE20260621205805', ctg.maChiTietGia, 100000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260626124720'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260621205805'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD_FIX020', 'HD20260626144919', 'VE20260621230004', ctg.maChiTietGia, 125000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260626144919'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260621230004'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD_FIX021', 'HD20260626165118', 'VE20260622010204', ctg.maChiTietGia, 150000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260626165118'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260622010204'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD_FIX022', 'HD20260626185317', 'VE20260622030403', ctg.maChiTietGia, 175000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260626185317'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260622030403'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
-- HD20260625223326: 2 ve ghe mem TUY20260510021503 HN->SG (1,100k/ve)
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD_FIX023', 'HD20260626205516', 'VE20260622050602', ctg.maChiTietGia, 200000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260626205516'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260622050602'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD_FIX024', 'HD20260626225716', 'VE20260622070801', ctg.maChiTietGia, 225000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260626225716'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260622070801'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
-- HD20260626003525: 3 ve ghe cung TUY20260510041702 SG->HN (800k/ve)
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD_FIX025', 'HD20260627005915', 'VE20260622091000', ctg.maChiTietGia, 250000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260627005915'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260622091000'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD_FIX026', 'HD20260627030114', 'VE20260622111200', ctg.maChiTietGia, 275000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260627030114'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260622111200'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD_FIX027', 'HD20260627050313', 'VE20260622131359', ctg.maChiTietGia, 300000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260627050313'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260622131359'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
-- HD20260626023724: 2 ve giuong nam TUY20260510041702 SG->HN (1,600k/ve)
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD_FIX028', 'HD20260612152136', 'VE20260622151558', ctg.maChiTietGia, 100000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260612152136'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260622151558'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD_FIX029', 'HD20260612212733', 'VE20260622171757', ctg.maChiTietGia, 125000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260612212733'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260622171757'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
-- HD20260626043923: 1 ve ghe cung TUY20260510021503 HN->SG, sinh vien ap dung KM20260604020059 giam 20% (800k*0.8=640k)
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD_FIX030', 'HD20260613013131', 'VE20260622191956', ctg.maChiTietGia, 150000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260613013131'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260622191956'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
-- HD20260626064122: 2 ve ghe mem TUY20260509095910 HN->Vinh (250k/ve)
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD_FIX031', 'HD20260613053530', 'VE20260622212155', ctg.maChiTietGia, 175000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260613053530'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260622212155'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD_FIX032', 'HD20260613154526', 'VE20260622232355', ctg.maChiTietGia, 200000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260613154526'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260622232355'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
-- HD20260626084321: 4 ve ghe cung TUY20260509160507 DN->SG (450k/ve)
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD_FIX033', 'HD20260625102131', 'VE20260623012554', ctg.maChiTietGia, 225000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260613053530'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260623012554'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD_FIX034', 'HD20260625122330', 'VE20260623032753', ctg.maChiTietGia, 250000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260613053530'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260623032753'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD_FIX035', 'HD20260625142529', 'VE20260623052952', ctg.maChiTietGia, 275000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260613053530'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260623052952'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD_FIX036', 'HD20260625162728', 'VE20260623073151', ctg.maChiTietGia, 300000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260613154526'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260623073151'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
-- HD20260626104521: 1 ve giuong nam TUY20260509180707 SG->DN (900k) - ve bi huy
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD_FIX037', 'HD20260625182927', 'VE20260623093350', ctg.maChiTietGia, 100000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260625182927'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260623093350'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
-- HD20260626124720: 3 ve ghe cung TUY20260509120109 Vinh->Hue (200k/ve)
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD_FIX038', 'HD20260625203126', 'VE20260623113550', ctg.maChiTietGia, 125000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260625203126'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260623113550'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD_FIX039', 'HD20260625223326', 'VE20260623133749', ctg.maChiTietGia, 150000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260625223326'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260623133749'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD_FIX040', 'HD20260626003525', 'VE20260623153948', ctg.maChiTietGia, 175000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260626003525'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260623153948'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
-- HD20260626144919: 2 ve ghe mem TUY20260509221105 Hue->Vinh (280k/ve)
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD_FIX041', 'HD20260626023724', 'VE20260623174147', ctg.maChiTietGia, 200000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260626023724'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260623174147'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD_FIX042', 'HD20260626043923', 'VE20260623194346', ctg.maChiTietGia, 225000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260626043923'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260623194346'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
-- HD20260626165118: 2 ve ghe cung TUY20260509140308 Hue->DN (80k/ve)
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD_FIX043', 'HD20260626064122', 'VE20260623214545', ctg.maChiTietGia, 250000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260626064122'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260623214545'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD_FIX044', 'HD20260626084321', 'VE20260623234745', ctg.maChiTietGia, 275000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260626084321'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260623234745'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
-- HD20260626185317: 3 ve ghe mem TUY20260509095910 HN->Vinh (250k/ve)
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD_FIX045', 'HD20260626104521', 'VE20260624014944', ctg.maChiTietGia, 300000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260626104521'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260624014944'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD_FIX046', 'HD20260626124720', 'VE20260624035143', ctg.maChiTietGia, 100000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260626124720'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260624035143'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD_FIX047', 'HD20260626144919', 'VE20260624055342', ctg.maChiTietGia, 125000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260626144919'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260624055342'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
-- HD20260626205516: 2 ve giuong nam TUY20260509120109 Vinh->Hue (450k/ve)
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD_FIX048', 'HD20260626165118', 'VE20260624075541', ctg.maChiTietGia, 150000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260626165118'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260624075541'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD_FIX049', 'HD20260626185317', 'VE20260624095740', ctg.maChiTietGia, 175000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260626185317'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260624095740'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
-- HD20260626225716: 5 ve ghe cung TUY20260509095910 dip le 30/4, KM20260604040258 giam 10% (180k*0.9=162k)
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD_FIX050', 'HD20260626205516', 'VE20260624115940', ctg.maChiTietGia, 200000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260626205516'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260624115940'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD_FIX051', 'HD20260626225716', 'VE20260624140139', ctg.maChiTietGia, 225000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260626225716'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260624140139'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD_FIX052', 'HD20260627005915', 'VE20260624160338', ctg.maChiTietGia, 250000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260627005915'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260624160338'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD_FIX053', 'HD20260627030114', 'VE20260624180537', ctg.maChiTietGia, 275000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260627030114'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260624180537'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD_FIX054', 'HD20260627050313', 'VE20260624200736', ctg.maChiTietGia, 300000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260627050313'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260624200736'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
-- HD20260627005915: 3 ve giuong nam TUY20260510021503 dip le 30/4, KM20260604040258 giam 12% (1600k*0.88=1408k)
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD_FIX055', 'HD20260612152136', 'VE20260624220936', ctg.maChiTietGia, 100000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260612152136'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260624220936'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD_FIX056', 'HD20260612212733', 'VE20260625001135', ctg.maChiTietGia, 125000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260612212733'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260625001135'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD_FIX057', 'HD20260613013131', 'VE20260625021334', ctg.maChiTietGia, 150000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260613013131'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260625021334'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
-- HD20260627030114: 2 ve ghe mem TUY20260510041702 SG->HN (1,100k/ve)
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD_FIX058', 'HD20260613053530', 'VE20260625041533', ctg.maChiTietGia, 175000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260613053530'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260625041533'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD_FIX059', 'HD20260613154526', 'VE20260625061732', ctg.maChiTietGia, 200000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260613154526'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260625061732'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
-- HD20260627050313: 2 ve ghe cung TUY20260509160507 DN->SG (450k/ve)
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD_FIX060', 'HD20260625102131', 'VE20260625081931', ctg.maChiTietGia, 225000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260613053530'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260625081931'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;
INSERT INTO ChiTietHoaDon (maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)
SELECT TOP 1 'CTHD_FIX061', 'HD20260625122330', 'VE20260621024013', ctg.maChiTietGia, 250000.00
FROM Ve v
JOIN Lich l ON l.maLich = v.maLich
JOIN Ghe ghe ON ghe.maGhe = v.maGhe
JOIN ToaTau toa ON toa.maToaTau = ghe.maToaTau
JOIN HoaDon hd ON hd.maHoaDon = 'HD20260613053530'
JOIN ChiTietGia ctg ON ctg.maTuyen = l.maTuyen AND ctg.loaiGhe = toa.loaiGhe
JOIN Gia g ON g.maGia = ctg.maGia
WHERE v.maVe = 'VE20260621024013'
ORDER BY g.trangThai DESC, g.thoiGianBatDau DESC;

-- ==================== ApDungKM (them 9 ap dung khuyen mai) ====================
-- CTHD20260630223037: sinh vien mua ghe cung TUY20260510021503, ap dung KM20260604020059 uu tien giam 20%
INSERT INTO ApDungKM VALUES ('ADKM20260703133211', 'CTHD20260630223037', 'CTKM20260605042648');
-- CTHD20260702151020..054: ghe cung TUY20260509095910 dip Le 30/4, ap dung KM20260604040258 giam 10%
INSERT INTO ApDungKM VALUES ('ADKM20260703153410', 'CTHD20260702151020', 'CTKM20260607011030');
INSERT INTO ApDungKM VALUES ('ADKM20260703173609', 'CTHD20260702171219', 'CTKM20260607011030');
INSERT INTO ApDungKM VALUES ('ADKM20260703193809', 'CTHD20260702191419', 'CTKM20260607011030');
INSERT INTO ApDungKM VALUES ('ADKM20260703214008', 'CTHD20260702211618', 'CTKM20260607011030');
INSERT INTO ApDungKM VALUES ('ADKM20260703234207', 'CTHD20260702231817', 'CTKM20260607011030');
-- CTHD20260703012016..057: giuong nam TUY20260510021503 dip Le 30/4, ap dung KM20260604040258 giam 12%
INSERT INTO ApDungKM VALUES ('ADKM20260704014406', 'CTHD20260703012016', 'CTKM20260608053819');
INSERT INTO ApDungKM VALUES ('ADKM20260704034605', 'CTHD20260703032215', 'CTKM20260608053819');
INSERT INTO ApDungKM VALUES ('ADKM20260704054804', 'CTHD20260703052414', 'CTKM20260608053819');

-- ==================== GiuCho (them 6 giu cho) ====================
INSERT INTO GiuCho VALUES ('GC20260704075004', 'NV20260504140557', 'LCH20260619160627', 'GHE20260518032345', '2026-06-01 15:00:00');
INSERT INTO GiuCho VALUES ('GC20260704095203', 'NV20260504140557', 'LCH20260619160627', 'GHE20260518052544', '2026-06-01 15:00:00');
INSERT INTO GiuCho VALUES ('GC20260704115402', 'NV20260504201155', 'LCH20260619180826', 'GHE20260521144511', '2026-06-03 12:00:00');
INSERT INTO GiuCho VALUES ('GC20260704135601', 'NV20260505041951', 'LCH20260620183216', 'GHE20260514140019', '2026-07-01 16:00:00');
INSERT INTO GiuCho VALUES ('GC20260704155800', 'NV20260505041951', 'LCH20260620183216', 'GHE20260514160219', '2026-07-01 16:00:00');
INSERT INTO GiuCho VALUES ('GC20260704180000', 'NV20260506044341', 'LCH20260620203415', 'GHE20260517231946', '2026-07-08 09:00:00');

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
