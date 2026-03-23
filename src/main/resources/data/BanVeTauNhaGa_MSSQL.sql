-- ============================================================
-- CO SO DU LIEU: UNG DUNG BAN VE TAU TAI NHA GA
-- Tuong thich: SQL Server (MSSQL)
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
    trangThai VARCHAR(20) NOT NULL DEFAULT 'DANG_LAM' CHECK (trangThai IN ('DANG_LAM', 'NGHI_PHEP', 'DA_NGHI'))
);

-- 2. Ga
CREATE TABLE Ga (
    maGa VARCHAR(20) PRIMARY KEY,
    tenGa NVARCHAR(100) NOT NULL,
    diaChi NVARCHAR(255) NOT NULL
);

-- 3. DauMay
CREATE TABLE DauMay (
    maDauMay VARCHAR(20) PRIMARY KEY,
    tenDauMay NVARCHAR(100) NOT NULL
);

-- 4. ToaTau
CREATE TABLE ToaTau (
    maToaTau VARCHAR(20) PRIMARY KEY,
    loaiGhe VARCHAR(20) NOT NULL CHECK (loaiGhe IN ('GHE_CUNG', 'GHE_MEM', 'GIUONG_NAM'))
);

-- 5. Gia
CREATE TABLE Gia (
    maGia VARCHAR(20) PRIMARY KEY,
    thoiGianBatDau DATETIME NOT NULL,
    thoiGianKetThuc DATETIME NOT NULL,
    moTa NVARCHAR(255),
    trangThai BIT NOT NULL DEFAULT 0,
    CHECK (thoiGianKetThuc > thoiGianBatDau)
);

-- 6. KhuyenMai
CREATE TABLE KhuyenMai (
    maKhuyenMai VARCHAR(20) PRIMARY KEY,
    tenKhuyenMai NVARCHAR(100) NOT NULL,
    phanTramGiam FLOAT NOT NULL CHECK (phanTramGiam > 0 AND phanTramGiam <= 1),
    dieuKien NVARCHAR(255),
    thoiGianBatDau DATETIME NOT NULL,
    thoiGianKetThuc DATETIME NOT NULL,
    CHECK (thoiGianKetThuc > thoiGianBatDau)
);

-- 7. Tuyen (FK -> Ga x2)
CREATE TABLE Tuyen (
    maTuyen VARCHAR(20) PRIMARY KEY,
    gaDi VARCHAR(20) NOT NULL,
    gaDen VARCHAR(20) NOT NULL,
    FOREIGN KEY (gaDi) REFERENCES Ga(maGa),
    FOREIGN KEY (gaDen) REFERENCES Ga(maGa),
    CHECK (gaDi != gaDen)
);

-- 8. DoanTau (FK -> DauMay)
CREATE TABLE DoanTau (
    maDoanTau VARCHAR(20) PRIMARY KEY,
    tenDoanTau NVARCHAR(100) NOT NULL,
    maDauMay VARCHAR(20) NOT NULL,
    FOREIGN KEY (maDauMay) REFERENCES DauMay(maDauMay)
);

-- 9. ChiTietDoanTau (FK -> DoanTau, ToaTau)
CREATE TABLE ChiTietDoanTau (
    maChiTietDT VARCHAR(20) PRIMARY KEY,
    maDoanTau VARCHAR(20) NOT NULL,
    maToaTau VARCHAR(20) NOT NULL,
    soThuTu INT NOT NULL CHECK (soThuTu > 0),
    FOREIGN KEY (maDoanTau) REFERENCES DoanTau(maDoanTau),
    FOREIGN KEY (maToaTau) REFERENCES ToaTau(maToaTau)
);

-- 10. Ghe (FK -> ToaTau)
CREATE TABLE Ghe (
    maGhe VARCHAR(20) PRIMARY KEY,
    maToaTau VARCHAR(20) NOT NULL,
    soGhe INT NOT NULL CHECK (soGhe > 0),
    FOREIGN KEY (maToaTau) REFERENCES ToaTau(maToaTau)
);

-- 11. Lich (FK -> Tuyen, DoanTau)
CREATE TABLE Lich (
    maLich VARCHAR(20) PRIMARY KEY,
    maTuyen VARCHAR(20) NOT NULL,
    maDoanTau VARCHAR(20) NOT NULL,
    thoiGianBatDau DATETIME NOT NULL,
    thoiGianChay NVARCHAR(50) NOT NULL,
    FOREIGN KEY (maTuyen) REFERENCES Tuyen(maTuyen),
    FOREIGN KEY (maDoanTau) REFERENCES DoanTau(maDoanTau)
);

-- 12. ChiTietGia (FK -> Gia, Tuyen)
CREATE TABLE ChiTietGia (
    maChiTietGia VARCHAR(20) PRIMARY KEY,
    maGia VARCHAR(20) NOT NULL,
    maTuyen VARCHAR(20) NOT NULL,
    loaiGhe VARCHAR(20) NOT NULL CHECK (loaiGhe IN ('GHE_CUNG', 'GHE_MEM', 'GIUONG_NAM')),
    giaNiemYet FLOAT NOT NULL CHECK (giaNiemYet > 0),
    FOREIGN KEY (maGia) REFERENCES Gia(maGia),
    FOREIGN KEY (maTuyen) REFERENCES Tuyen(maTuyen)
);

-- 13. HoaDon (FK -> NhanVien)
CREATE TABLE HoaDon (
    maHoaDon VARCHAR(30) PRIMARY KEY,
    maNV VARCHAR(20) NOT NULL,
    ngayLap DATETIME NOT NULL,
    FOREIGN KEY (maNV) REFERENCES NhanVien(maNV)
);

-- 14. Ve (FK -> HoaDon)
CREATE TABLE Ve (
    maVe VARCHAR(20) PRIMARY KEY,
    maHoaDon VARCHAR(30) NOT NULL,
    tenHanhKhach NVARCHAR(100) NOT NULL,
    cccd VARCHAR(20) NOT NULL,
    giaTien FLOAT NOT NULL CHECK (giaTien > 0),
    trangThai VARCHAR(10) NOT NULL CHECK (trangThai IN ('DA_BAN', 'DA_HUY')),
    lyDoHuy NVARCHAR(255),
    ngayHuy DATETIME,
    FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHoaDon)
);

-- 15. ChiTietVe (FK -> Ve, Lich, Ghe)
CREATE TABLE ChiTietVe (
    maChiTietVe VARCHAR(20) PRIMARY KEY,
    maVe VARCHAR(20) NOT NULL,
    maLich VARCHAR(20) NOT NULL,
    maGhe VARCHAR(20) NOT NULL,
    trangThai VARCHAR(10) NOT NULL CHECK (trangThai IN ('DA_BAN', 'DA_HUY')),
    FOREIGN KEY (maVe) REFERENCES Ve(maVe),
    FOREIGN KEY (maLich) REFERENCES Lich(maLich),
    FOREIGN KEY (maGhe) REFERENCES Ghe(maGhe)
);

-- 16. ApDungKM (FK -> Ve, KhuyenMai)
CREATE TABLE ApDungKM (
    maApDung VARCHAR(20) PRIMARY KEY,
    maVe VARCHAR(20) NOT NULL,
    maKhuyenMai VARCHAR(20) NOT NULL,
    FOREIGN KEY (maVe) REFERENCES Ve(maVe),
    FOREIGN KEY (maKhuyenMai) REFERENCES KhuyenMai(maKhuyenMai)
);

-- 17. GiuCho (FK -> NhanVien, Lich, Ghe)
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
GO

-- ============================================================
-- DU LIEU MAU
-- ============================================================

-- ==================== 1. NhanVien ====================
INSERT INTO NhanVien VALUES ('NV-0000', N'ADMIN', 'ADMIN', 'ADMIN', '0000000000', 'DANG_LAM');
INSERT INTO NhanVien VALUES ('NV-0001', N'Nguyễn Văn An', 'Pass@123', 'BAN_VE', '0901234567', 'DANG_LAM');
INSERT INTO NhanVien VALUES ('NV-0002', N'Trần Thị Bình', 'Pass@456', 'BAN_VE', '0912345678', 'NGHI_PHEP');
INSERT INTO NhanVien VALUES ('NV-0003', N'Lê Hoàng Cường', 'Pass@789', 'DIEU_PHOI', '0923456789', 'DANG_LAM');
INSERT INTO NhanVien VALUES ('NV-0004', N'Phạm Minh Đức', 'Pass@101', 'BAN_VE', '0934567890', 'DANG_LAM');
INSERT INTO NhanVien VALUES ('NV-0005', N'Hoàng Thị Elysa', 'Pass@102', 'BAN_VE', '0945678901', 'DANG_LAM');
INSERT INTO NhanVien VALUES ('NV-0006', N'Võ Văn Phúc', 'Pass@103', 'DIEU_PHOI', '0956789012', 'DANG_LAM');
INSERT INTO NhanVien VALUES ('NV-0007', N'Đặng Thùy Giang', 'Pass@104', 'BAN_VE', '0967890123', 'NGHI_PHEP');
INSERT INTO NhanVien VALUES ('NV-0008', N'Bùi Quốc Huy', 'Pass@105', 'BAN_VE', '0978901234', 'DANG_LAM');
INSERT INTO NhanVien VALUES ('NV-0009', N'Ngô Thanh Inh', 'Pass@106', 'DIEU_PHOI', '0989012345', 'DA_NGHI');
INSERT INTO NhanVien VALUES ('NV-0010', N'Lý Thị Kim', 'Pass@107', 'BAN_VE', '0990123456', 'DANG_LAM');
INSERT INTO NhanVien VALUES ('NV-0011', N'Trương Đình Lâm', 'Pass@108', 'BAN_VE', '0901122334', 'DANG_LAM');
INSERT INTO NhanVien VALUES ('NV-0012', N'Phan Thị Mai', 'Pass@109', 'DIEU_PHOI', '0912233445', 'NGHI_PHEP');
INSERT INTO NhanVien VALUES ('NV-0013', N'Hồ Trọng Nam', 'Pass@110', 'BAN_VE', '0923344556', 'DANG_LAM');
INSERT INTO NhanVien VALUES ('NV-0014', N'Dương Thị Oanh', 'Pass@111', 'BAN_VE', '0934455667', 'DA_NGHI');
INSERT INTO NhanVien VALUES ('NV-0015', N'Tạ Minh Phong', 'Pass@112', 'DIEU_PHOI', '0945566778', 'DANG_LAM');
INSERT INTO NhanVien VALUES ('NV-0016', N'Vũ Thị Quỳnh', 'Pass@113', 'BAN_VE', '0956677889', 'DANG_LAM');
INSERT INTO NhanVien VALUES ('NV-0017', N'Đinh Công Sơn', 'Pass@114', 'BAN_VE', '0967788990', 'NGHI_PHEP');
INSERT INTO NhanVien VALUES ('NV-0018', N'Mai Thị Tâm', 'Pass@115', 'BAN_VE', '0978899001', 'DANG_LAM');
INSERT INTO NhanVien VALUES ('NV-0019', N'Lương Văn Uy', 'Pass@116', 'DIEU_PHOI', '0989900112', 'DANG_LAM');
INSERT INTO NhanVien VALUES ('NV-0020', N'Cao Thị Vân', 'Pass@117', 'BAN_VE', '0990011223', 'DA_NGHI');
INSERT INTO NhanVien VALUES ('NV-0021', N'Châu Quốc Xuân', 'Pass@118', 'BAN_VE', '0901233210', 'DANG_LAM');
INSERT INTO NhanVien VALUES ('NV-0022', N'Kiều Thị Yến', 'Pass@119', 'DIEU_PHOI', '0912344321', 'DANG_LAM');
INSERT INTO NhanVien VALUES ('NV-0023', N'Trịnh Đức Zũng', 'Pass@120', 'BAN_VE', '0923455432', 'NGHI_PHEP');

-- ==================== 2. Ga ====================
INSERT INTO Ga VALUES ('GA-001', N'Ga Hà Nội', N'120 Lê Duẩn, Hoàn Kiếm, Hà Nội');
INSERT INTO Ga VALUES ('GA-002', N'Ga Vinh', N'Đường Phan Bội Châu, TP Vinh, Nghệ An');
INSERT INTO Ga VALUES ('GA-003', N'Ga Huế', N'2 Bùi Thị Xuân, TP Huế, Thừa Thiên Huế');
INSERT INTO Ga VALUES ('GA-004', N'Ga Đà Nẵng', N'791 Hải Phòng, Thanh Khê, Đà Nẵng');
INSERT INTO Ga VALUES ('GA-005', N'Ga Sài Gòn', N'1 Nguyễn Thông, Quận 3, TP.HCM');

-- ==================== 3. DauMay ====================
INSERT INTO DauMay VALUES ('DM-001', N'Đầu máy D19E-001');
INSERT INTO DauMay VALUES ('DM-002', N'Đầu máy D19E-002');
INSERT INTO DauMay VALUES ('DM-003', N'Đầu máy D13E-001');

-- ==================== 4. ToaTau ====================
INSERT INTO ToaTau VALUES ('TOA-001', 'GHE_CUNG');
INSERT INTO ToaTau VALUES ('TOA-002', 'GHE_CUNG');
INSERT INTO ToaTau VALUES ('TOA-003', 'GHE_MEM');
INSERT INTO ToaTau VALUES ('TOA-004', 'GHE_MEM');
INSERT INTO ToaTau VALUES ('TOA-005', 'GIUONG_NAM');
INSERT INTO ToaTau VALUES ('TOA-006', 'GIUONG_NAM');
INSERT INTO ToaTau VALUES ('TOA-007', 'GHE_CUNG');
INSERT INTO ToaTau VALUES ('TOA-008', 'GHE_MEM');

-- ==================== 5. Tuyen ====================
INSERT INTO Tuyen VALUES ('TUY-001', 'GA-001', 'GA-002');
INSERT INTO Tuyen VALUES ('TUY-002', 'GA-002', 'GA-003');
INSERT INTO Tuyen VALUES ('TUY-003', 'GA-003', 'GA-004');
INSERT INTO Tuyen VALUES ('TUY-004', 'GA-004', 'GA-005');
INSERT INTO Tuyen VALUES ('TUY-005', 'GA-005', 'GA-004');
INSERT INTO Tuyen VALUES ('TUY-006', 'GA-004', 'GA-003');
INSERT INTO Tuyen VALUES ('TUY-007', 'GA-003', 'GA-002');
INSERT INTO Tuyen VALUES ('TUY-008', 'GA-002', 'GA-001');
INSERT INTO Tuyen VALUES ('TUY-009', 'GA-001', 'GA-005');
INSERT INTO Tuyen VALUES ('TUY-010', 'GA-005', 'GA-001');

-- ==================== 6. DoanTau ====================
INSERT INTO DoanTau VALUES ('DT-001', N'SE1-v1', 'DM-001');
INSERT INTO DoanTau VALUES ('DT-002', N'SE2-v1', 'DM-002');
INSERT INTO DoanTau VALUES ('DT-003', N'SE3-v1', 'DM-003');

-- ==================== 7. ChiTietDoanTau ====================
-- DT-001 (SE1): TOA-001(cung), TOA-003(mem), TOA-005(giuong)
INSERT INTO ChiTietDoanTau VALUES ('CTDT-001', 'DT-001', 'TOA-001', 1);
INSERT INTO ChiTietDoanTau VALUES ('CTDT-002', 'DT-001', 'TOA-003', 2);
INSERT INTO ChiTietDoanTau VALUES ('CTDT-003', 'DT-001', 'TOA-005', 3);
-- DT-002 (SE2): TOA-002(cung), TOA-004(mem), TOA-006(giuong)
INSERT INTO ChiTietDoanTau VALUES ('CTDT-004', 'DT-002', 'TOA-002', 1);
INSERT INTO ChiTietDoanTau VALUES ('CTDT-005', 'DT-002', 'TOA-004', 2);
INSERT INTO ChiTietDoanTau VALUES ('CTDT-006', 'DT-002', 'TOA-006', 3);
-- DT-003 (SE3): TOA-007(cung), TOA-008(mem)
INSERT INTO ChiTietDoanTau VALUES ('CTDT-007', 'DT-003', 'TOA-007', 1);
INSERT INTO ChiTietDoanTau VALUES ('CTDT-008', 'DT-003', 'TOA-008', 2);

-- ==================== 8. Ghe ====================
-- TOA-001 (ghe cung): 5 ghe
INSERT INTO Ghe VALUES ('G-001-01', 'TOA-001', 1);
INSERT INTO Ghe VALUES ('G-001-02', 'TOA-001', 2);
INSERT INTO Ghe VALUES ('G-001-03', 'TOA-001', 3);
INSERT INTO Ghe VALUES ('G-001-04', 'TOA-001', 4);
INSERT INTO Ghe VALUES ('G-001-05', 'TOA-001', 5);
-- TOA-002 (ghe cung): 5 ghe
INSERT INTO Ghe VALUES ('G-002-01', 'TOA-002', 1);
INSERT INTO Ghe VALUES ('G-002-02', 'TOA-002', 2);
INSERT INTO Ghe VALUES ('G-002-03', 'TOA-002', 3);
INSERT INTO Ghe VALUES ('G-002-04', 'TOA-002', 4);
INSERT INTO Ghe VALUES ('G-002-05', 'TOA-002', 5);
-- TOA-003 (ghe mem): 4 ghe
INSERT INTO Ghe VALUES ('G-003-01', 'TOA-003', 1);
INSERT INTO Ghe VALUES ('G-003-02', 'TOA-003', 2);
INSERT INTO Ghe VALUES ('G-003-03', 'TOA-003', 3);
INSERT INTO Ghe VALUES ('G-003-04', 'TOA-003', 4);
-- TOA-004 (ghe mem): 4 ghe
INSERT INTO Ghe VALUES ('G-004-01', 'TOA-004', 1);
INSERT INTO Ghe VALUES ('G-004-02', 'TOA-004', 2);
INSERT INTO Ghe VALUES ('G-004-03', 'TOA-004', 3);
INSERT INTO Ghe VALUES ('G-004-04', 'TOA-004', 4);
-- TOA-005 (giuong nam): 3 giuong
INSERT INTO Ghe VALUES ('G-005-01', 'TOA-005', 1);
INSERT INTO Ghe VALUES ('G-005-02', 'TOA-005', 2);
INSERT INTO Ghe VALUES ('G-005-03', 'TOA-005', 3);
-- TOA-006 (giuong nam): 3 giuong
INSERT INTO Ghe VALUES ('G-006-01', 'TOA-006', 1);
INSERT INTO Ghe VALUES ('G-006-02', 'TOA-006', 2);
INSERT INTO Ghe VALUES ('G-006-03', 'TOA-006', 3);
-- TOA-007 (ghe cung): 5 ghe
INSERT INTO Ghe VALUES ('G-007-01', 'TOA-007', 1);
INSERT INTO Ghe VALUES ('G-007-02', 'TOA-007', 2);
INSERT INTO Ghe VALUES ('G-007-03', 'TOA-007', 3);
INSERT INTO Ghe VALUES ('G-007-04', 'TOA-007', 4);
INSERT INTO Ghe VALUES ('G-007-05', 'TOA-007', 5);
-- TOA-008 (ghe mem): 4 ghe
INSERT INTO Ghe VALUES ('G-008-01', 'TOA-008', 1);
INSERT INTO Ghe VALUES ('G-008-02', 'TOA-008', 2);
INSERT INTO Ghe VALUES ('G-008-03', 'TOA-008', 3);
INSERT INTO Ghe VALUES ('G-008-04', 'TOA-008', 4);

-- ==================== 9. Lich ====================
INSERT INTO Lich VALUES ('LCH-001', 'TUY-001', 'DT-001', '2026-04-10 06:00:00', N'5 giờ 30 phút');
INSERT INTO Lich VALUES ('LCH-002', 'TUY-001', 'DT-001', '2026-04-11 06:00:00', N'5 giờ 30 phút');
INSERT INTO Lich VALUES ('LCH-003', 'TUY-002', 'DT-002', '2026-04-10 14:00:00', N'6 giờ');
INSERT INTO Lich VALUES ('LCH-004', 'TUY-002', 'DT-002', '2026-04-11 14:00:00', N'6 giờ');
INSERT INTO Lich VALUES ('LCH-005', 'TUY-003', 'DT-003', '2026-04-10 08:00:00', N'2 giờ 30 phút');
INSERT INTO Lich VALUES ('LCH-006', 'TUY-004', 'DT-001', '2026-04-12 19:00:00', N'17 giờ');
INSERT INTO Lich VALUES ('LCH-007', 'TUY-005', 'DT-002', '2026-04-13 07:00:00', N'17 giờ');
INSERT INTO Lich VALUES ('LCH-008', 'TUY-009', 'DT-003', '2026-04-15 19:00:00', N'33 giờ');

-- ==================== 10. Gia ====================
INSERT INTO Gia VALUES ('GIA-001', '2026-01-01 00:00:00', '2026-12-31 23:59:59', N'Bảng giá thường 2026', 0);
INSERT INTO Gia VALUES ('GIA-002', '2026-01-25 00:00:00', '2026-02-10 23:59:59', N'Bảng giá Tết Nguyên Đán 2026', 0);
INSERT INTO Gia VALUES ('GIA-003', '2026-06-01 00:00:00', '2026-08-31 23:59:59', N'Bảng giá mùa hè 2026', 0);

-- ==================== 11. ChiTietGia ====================
-- Bang gia thuong (GIA-001)
INSERT INTO ChiTietGia VALUES ('CTG-001', 'GIA-001', 'TUY-001', 'GHE_CUNG', 180000);
INSERT INTO ChiTietGia VALUES ('CTG-002', 'GIA-001', 'TUY-001', 'GHE_MEM', 250000);
INSERT INTO ChiTietGia VALUES ('CTG-003', 'GIA-001', 'TUY-001', 'GIUONG_NAM', 400000);
INSERT INTO ChiTietGia VALUES ('CTG-004', 'GIA-001', 'TUY-002', 'GHE_CUNG', 200000);
INSERT INTO ChiTietGia VALUES ('CTG-005', 'GIA-001', 'TUY-002', 'GHE_MEM', 280000);
INSERT INTO ChiTietGia VALUES ('CTG-006', 'GIA-001', 'TUY-002', 'GIUONG_NAM', 450000);
INSERT INTO ChiTietGia VALUES ('CTG-007', 'GIA-001', 'TUY-003', 'GHE_CUNG', 80000);
INSERT INTO ChiTietGia VALUES ('CTG-008', 'GIA-001', 'TUY-003', 'GHE_MEM', 120000);
INSERT INTO ChiTietGia VALUES ('CTG-009', 'GIA-001', 'TUY-003', 'GIUONG_NAM', 200000);
INSERT INTO ChiTietGia VALUES ('CTG-010', 'GIA-001', 'TUY-004', 'GHE_CUNG', 450000);
INSERT INTO ChiTietGia VALUES ('CTG-011', 'GIA-001', 'TUY-004', 'GHE_MEM', 600000);
INSERT INTO ChiTietGia VALUES ('CTG-012', 'GIA-001', 'TUY-004', 'GIUONG_NAM', 900000);
INSERT INTO ChiTietGia VALUES ('CTG-013', 'GIA-001', 'TUY-005', 'GHE_CUNG', 450000);
INSERT INTO ChiTietGia VALUES ('CTG-014', 'GIA-001', 'TUY-005', 'GHE_MEM', 600000);
INSERT INTO ChiTietGia VALUES ('CTG-015', 'GIA-001', 'TUY-005', 'GIUONG_NAM', 900000);
INSERT INTO ChiTietGia VALUES ('CTG-016', 'GIA-001', 'TUY-009', 'GHE_CUNG', 800000);
INSERT INTO ChiTietGia VALUES ('CTG-017', 'GIA-001', 'TUY-009', 'GHE_MEM', 1100000);
INSERT INTO ChiTietGia VALUES ('CTG-018', 'GIA-001', 'TUY-009', 'GIUONG_NAM', 1600000);
-- Bang gia Tet (GIA-002): tang 30%
INSERT INTO ChiTietGia VALUES ('CTG-019', 'GIA-002', 'TUY-001', 'GHE_CUNG', 234000);
INSERT INTO ChiTietGia VALUES ('CTG-020', 'GIA-002', 'TUY-001', 'GHE_MEM', 325000);
INSERT INTO ChiTietGia VALUES ('CTG-021', 'GIA-002', 'TUY-001', 'GIUONG_NAM', 520000);
INSERT INTO ChiTietGia VALUES ('CTG-022', 'GIA-002', 'TUY-009', 'GHE_CUNG', 1040000);
INSERT INTO ChiTietGia VALUES ('CTG-023', 'GIA-002', 'TUY-009', 'GHE_MEM', 1430000);
INSERT INTO ChiTietGia VALUES ('CTG-024', 'GIA-002', 'TUY-009', 'GIUONG_NAM', 2080000);

-- ==================== 12. KhuyenMai ====================
INSERT INTO KhuyenMai VALUES ('KM-001', N'Giảm giá trẻ em', 0.25, N'Hành khách dưới 12 tuổi', '2026-01-01 00:00:00', '2026-12-31 23:59:59');
INSERT INTO KhuyenMai VALUES ('KM-002', N'Giảm giá sinh viên', 0.15, N'Có thẻ sinh viên hợp lệ', '2026-01-01 00:00:00', '2026-12-31 23:59:59');
INSERT INTO KhuyenMai VALUES ('KM-003', N'Giảm giá người cao tuổi', 0.20, N'Hành khách từ 60 tuổi trở lên', '2026-01-01 00:00:00', '2026-12-31 23:59:59');
INSERT INTO KhuyenMai VALUES ('KM-004', N'Khuyến mãi lễ 30/4', 0.10, N'Áp dụng dịp lễ 30/4 - 1/5', '2026-04-28 00:00:00', '2026-05-02 23:59:59');
INSERT INTO KhuyenMai VALUES ('KM-005', N'Khuyến mãi hè', 0.05, N'Áp dụng mùa hè', '2026-06-01 00:00:00', '2026-08-31 23:59:59');

-- ==================== 13. HoaDon ====================
INSERT INTO HoaDon VALUES ('HD-10042026-001', 'NV-0001', '2026-04-08 09:15:00');
INSERT INTO HoaDon VALUES ('HD-10042026-002', 'NV-0002', '2026-04-08 10:30:00');
INSERT INTO HoaDon VALUES ('HD-10042026-003', 'NV-0001', '2026-04-09 14:00:00');
INSERT INTO HoaDon VALUES ('HD-11042026-001', 'NV-0002', '2026-04-09 16:00:00');
INSERT INTO HoaDon VALUES ('HD-12042026-001', 'NV-0001', '2026-04-10 08:00:00');

-- ==================== 14. Ve ====================
INSERT INTO Ve VALUES ('VE-001', 'HD-10042026-001', N'Phạm Minh Tuấn', '012345678901', 250000, 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE-002', 'HD-10042026-001', N'Phạm Thị Lan', '012345678902', 250000, 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE-003', 'HD-10042026-002', N'Hoàng Đức Mạnh', '034567890123', 450000, 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE-004', 'HD-10042026-003', N'Nguyễn Thị Hoa', '056789012345', 68000, 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE-005', 'HD-11042026-001', N'Trần Văn Đức', '078901234567', 380000, 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE-006', 'HD-11042026-001', N'Trần Thị Mai', '078901234568', 380000, 'DA_BAN', NULL, NULL);
INSERT INTO Ve VALUES ('VE-007', 'HD-12042026-001', N'Lý Văn Hùng', '090123456789', 600000, 'DA_HUY', N'Hành khách thay đổi kế hoạch', '2026-04-10 10:30:00');

-- ==================== 15. ChiTietVe ====================
INSERT INTO ChiTietVe VALUES ('CTV-001', 'VE-001', 'LCH-001', 'G-003-01', 'DA_BAN');
INSERT INTO ChiTietVe VALUES ('CTV-002', 'VE-002', 'LCH-001', 'G-003-02', 'DA_BAN');
INSERT INTO ChiTietVe VALUES ('CTV-003', 'VE-003', 'LCH-003', 'G-006-01', 'DA_BAN');
INSERT INTO ChiTietVe VALUES ('CTV-004', 'VE-004', 'LCH-005', 'G-007-01', 'DA_BAN');
INSERT INTO ChiTietVe VALUES ('CTV-005', 'VE-005', 'LCH-002', 'G-001-01', 'DA_BAN');
INSERT INTO ChiTietVe VALUES ('CTV-006', 'VE-005', 'LCH-004', 'G-002-01', 'DA_BAN');
INSERT INTO ChiTietVe VALUES ('CTV-007', 'VE-006', 'LCH-002', 'G-001-02', 'DA_BAN');
INSERT INTO ChiTietVe VALUES ('CTV-008', 'VE-006', 'LCH-004', 'G-002-02', 'DA_BAN');
INSERT INTO ChiTietVe VALUES ('CTV-009', 'VE-007', 'LCH-006', 'G-003-01', 'DA_HUY');

-- ==================== 16. ApDungKM ====================
INSERT INTO ApDungKM VALUES ('ADKM-001', 'VE-004', 'KM-002');

-- ==================== 17. GiuCho ====================
INSERT INTO GiuCho VALUES ('GC-001', 'NV-0001', 'LCH-008', 'G-007-02', '2026-04-14 15:05:00');
INSERT INTO GiuCho VALUES ('GC-002', 'NV-0001', 'LCH-008', 'G-007-03', '2026-04-14 15:05:00');
GO

-- ============================================================
-- KIEM TRA DU LIEU
-- ============================================================
SELECT 'NhanVien' AS bang, COUNT(*) AS so_ban_ghi FROM NhanVien
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
UNION ALL SELECT 'HoaDon', COUNT(*) FROM HoaDon
UNION ALL SELECT 'Ve', COUNT(*) FROM Ve
UNION ALL SELECT 'ChiTietVe', COUNT(*) FROM ChiTietVe
UNION ALL SELECT 'ApDungKM', COUNT(*) FROM ApDungKM
UNION ALL SELECT 'GiuCho', COUNT(*) FROM GiuCho;
