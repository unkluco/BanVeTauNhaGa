# BanVeTauNhaGa - Tóm tắt dự án

> **GHI CHÚ**: File này mô tả tổng quan dự án cho các agent/developer. **Nhớ cập nhật lại sau mỗi lần thay đổi lớn** (chỉ tóm tắt, không ghi chi tiết để tránh file phình to).

## Mục đích
Ứng dụng quản lý bán vé tàu tại nhà ga (Java Swing + MSSQL).

## Công nghệ
- Java 17+, Swing (GUI), JDBC (MSSQL)
- Maven build (pom.xml)
- Database: SQL Server - `BanVeTauNhaGa`

## Cấu trúc chính
```
src/main/java/com/
├── entity/       # 17 entity classes
├── dao/          # 17 DAO classes (JDBC)
├── enums/        # VaiTro, LoaiGhe, TrangThaiVe, TrangThaiNhanVien
├── modules/      # UI modules (MenuModule, QuanLyVeModule, ...)
├── connectDB/    # ConnectDB - kết nối MSSQL
└── Main.java     # Entry point
```

## Danh sách thực thể (18 bảng DB, 17 entity)

| Entity | Mô tả | Ghi chú |
|--------|--------|---------|
| NhanVien | Nhân viên (BAN_VE, DIEU_PHOI, ADMIN) | Có trangThai (enum TrangThaiNhanVien) |
| KhachHang | Khách hàng mua vé | **Mới** - maKhachHang, hoTen, cccd, soDienThoai |
| Ga | Nhà ga | maGa, tenGa, diaChi |
| DauMay | Đầu máy tàu | maDauMay, tenDauMay |
| ToaTau | Toa tàu | maToaTau, loaiGhe (enum LoaiGhe) |
| Tuyen | Tuyến đường | gaDi, gaDen (FK → Ga) |
| DoanTau | Đoàn tàu | maDauMay (FK → DauMay) |
| ChiTietDoanTau | Toa trong đoàn | maDoanTau, maToaTau, soThuTu |
| Ghe | Ghế/giường | maToaTau, soGhe |
| Lich | Lịch chạy tàu | maTuyen, maDoanTau, thoiGianBatDau, thoiGianChay (Duration/INT phút) |
| Gia | Kỳ giá | thoiGianBatDau/KetThuc, trangThai |
| ChiTietGia | Giá niêm yết | maGia, maTuyen, loaiGhe, giaNiemYet (BigDecimal) |
| KhuyenMai | Khuyến mãi | phanTramGiam (BigDecimal), thoiGianBatDau/KetThuc |
| Ve | Vé tàu | maLich, maGhe, trangThai (DA_BAN/DA_HUY), lyDoHuy, ngayHuy |
| HoaDon | Hóa đơn | maNV, maKhachHang, ngayLap |
| ChiTietHoaDon | Chi tiết hóa đơn | **Mới** - maHoaDon, maVe (UNIQUE 1-1), giaTien (BigDecimal) |
| ApDungKM | Áp dụng khuyến mãi | maChiTietHD (FK → ChiTietHoaDon), maKhuyenMai |
| GiuCho | Giữ chỗ tạm | maNV, maLich, maGhe, thoiGianHetHan |

## Quan hệ quan trọng
- **Ve** → Lich + Ghe (1 vé = 1 ghế trên 1 lịch)
- **ChiTietHoaDon** → HoaDon + Ve (1-1 với Ve, chứa giaTien snapshot)
- **HoaDon** → NhanVien + KhachHang
- **ApDungKM** → ChiTietHoaDon + KhuyenMai

## Kiểu dữ liệu đặc biệt
- Tiền tệ: `BigDecimal` (Java) ↔ `DECIMAL(18,2)` (SQL)
- Phần trăm KM: `BigDecimal` ↔ `DECIMAL(5,2)` (0 < x ≤ 1)
- Thời gian chạy: `Duration` (Java) ↔ `INT` phút (SQL)

## Lưu ý thiết kế UI
- Pagination: tự động tính số dòng vừa khít khung hiển thị (recalcRowsPerPage + guard flag)
- Pagination bar dính ngay dưới dòng cuối (BorderLayout.NORTH trong centerWrapper)
- Main frame: MAXIMIZED_BOTH, min size 1024x700
- Tất cả module dùng chung design token (PRIMARY=#005D90, SURFACE, OUTLINE...)

## Lưu ý thiết kế UI bổ sung
- Dialog có nút "Hủy"/"Đăng xuất" → KHÔNG thêm nút X (tránh trùng lặp).
- Validation form: hiển thị lỗi inline (JLabel đỏ dưới ô) + viền đỏ + auto-focus. KHÔNG dùng JOptionPane.
- Nút action trong bảng: bọc JButton trong JPanel(GridBagLayout) để click area chính xác.
- Canh giữa dọc: dùng GridBagLayout hoặc FlowLayout có vgap, tránh FlowLayout(..., 0).
- Icons: `src/main/resources/icons/` (placeholder, cần thay ảnh thật). Ảnh lớn: `src/main/resources/images/`.

## Cập nhật gần nhất
- **2026-03-26**: Thêm SuaNhanVienDialog (sửa nhân viên). Sửa ThemNhanVienDialog: bỏ nút X (đã có nút Hủy), thêm inline validation. Fix canh giữa dọc header/filter bar. Fix edit button click area chỉ trigger trên nút. Tạo thư mục placeholder icons/images. Tăng kích thước nút "Bỏ lọc".
- **2026-03-25 (2)**: Đổi tab "Quản lý vé và hóa đơn" → "Quản lý vé". Fix pagination auto-fit rows (recalcRowsPerPage dùng fixed height + guard flag chống loop). Thêm min size 1024x700 cho JFrame.
- **2026-03-25**: Cập nhật entity/DAO theo schema v2 (xóa ChiTietVe, thêm KhachHang + ChiTietHoaDon, sửa Ve/HoaDon/ApDungKM/Lich/ChiTietGia/KhuyenMai).
