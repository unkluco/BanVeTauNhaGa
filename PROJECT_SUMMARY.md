# BanVeTauNhaGa - Tóm tắt dự án

> **GHI CHÚ**: File này mô tả tổng quan dự án cho agent/developer. Khi code, schema hoặc UI thay đổi lớn, cập nhật lại ngắn gọn để tránh lỗi thời.

## Mục đích
Ứng dụng quản lý bán vé tàu tại nhà ga, xây dựng bằng Java Swing và SQL Server. Luồng chính gồm đăng nhập, tổng quan vận hành, bán vé nhiều bước, quản lý nghiệp vụ, dữ liệu tàu và hệ thống người dùng/khách hàng.

## Công nghệ
- Java 21, Swing/AWT cho giao diện desktop.
- Maven build qua `pom.xml`.
- JDBC SQL Server qua Microsoft SQL Server JDBC Driver.
- SQL Server database mặc định: `BanVeTauNhaGa`.
- Kết nối hiện được hard-code trong `ConnectDB` tới `localhost:1433`, user `sa`, password `sapassword`; comment trong file vẫn nhắc `.env` nhưng code chưa dùng `Dotenv`.
- Tài nguyên SQL mẫu nằm ở `src/main/resources/data/BanVeTauNhaGa_MSSQL.sql`.

## Cấu trúc chính
```text
src/main/java/com/
├── connectDB/    # ConnectDB - singleton kết nối SQL Server
├── dao/          # 21 DAO JDBC
├── entity/       # 21 entity/model class
├── enums/        # LoaiGhe, TrangThaiNhanVien, TrangThaiVe, VaiTro
├── graphics/     # TrainGraphics
├── modules/      # 63 UI module/dialog/helper cho Swing
├── util/         # MaTuDong
└── Main.java     # Entry point: login frame + main frame
```

## Schema và entity hiện tại
Schema SQL hiện tạo **18 bảng nghiệp vụ**. Code Java có **21 entity/DAO** vì còn giữ một số model/DAO phục vụ tương thích hoặc view nghiệp vụ như `ChiTietVe`, `HoaDonKhachHang`, `ChiTietKhuyenMai`.

| Nhóm | Bảng/entity chính | Vai trò |
|---|---|---|
| Người dùng | `NhanVien`, `KhachHang` | Hồ sơ nhân viên/khách hàng, có thông tin liên hệ, địa chỉ, ngày sinh, giới tính, quốc tịch. |
| Hạ tầng ga tàu | `Ga`, `DauMay`, `ToaTau`, `Ghe`, `Tuyen`, `DoanTau`, `ChiTietDoanTau`, `Lich` | Quản lý ga, tuyến, đầu máy, toa, ghế, đoàn tàu và lịch chạy. |
| Giá/khuyến mãi | `Gia`, `ChiTietGia`, `KhuyenMai`, `ChiTietKhuyenMai` | Kỳ giá, giá theo tuyến/loại ghế, kỳ khuyến mãi và điều kiện áp dụng theo tuyến/loại ghế hoặc toàn bộ. |
| Vé/hóa đơn | `Ve`, `HoaDon`, `HoaDonKhachHang`, `ChiTietHoaDon`, `ApDungKM`, `GiuCho` | Vé theo lịch + ghế, hóa đơn, liên kết nhiều khách hàng trong một hóa đơn, chi tiết giá snapshot, khuyến mãi đã áp dụng, giữ chỗ tạm. |
| Tương thích | `ChiTietVe` | Vẫn còn class/DAO trong code, nhưng schema mới dùng `ChiTietHoaDon` thay thế quan hệ hóa đơn-vé. |

## Quan hệ quan trọng
- `Ve` đại diện cho 1 ghế trên 1 lịch chạy: `Ve -> Lich + Ghe`.
- `HoaDon` chỉ giữ nhân viên lập và ngày lập; khách hàng tách qua bảng nối `HoaDonKhachHang`.
- `HoaDonKhachHang` cho phép một hóa đơn liên kết nhiều khách hàng, unique theo cặp `maHoaDon + maKhachHang`.
- `ChiTietHoaDon` liên kết `HoaDon -> Ve`, có `maVe UNIQUE`, `maChiTietGia` và `giaTien` snapshot.
- `ApDungKM` liên kết `ChiTietHoaDon` với `ChiTietKhuyenMai`, không trỏ trực tiếp `KhuyenMai` nữa.
- `ChiTietKhuyenMai` có thể áp dụng theo tuyến, theo loại ghế hoặc toàn bộ nếu `maTuyen`/`loaiGhe` là `NULL`.

## Kiểu dữ liệu và enum đáng chú ý
- Tiền tệ trong SQL dùng `DECIMAL(18,2)`; code hiện dùng `double` cho `ChiTietGia.giaNiemYet` và `BigDecimal` cho `ChiTietHoaDon.giaTien`.
- Phần trăm khuyến mãi trong SQL dùng `DECIMAL(5,2)`; code hiện dùng `double` cho `ChiTietKhuyenMai.phanTramGiam`, miền giá trị SQL `(0, 1]`.
- Thời gian chạy tàu trong SQL là `INT` phút; entity `Lich` hiện vẫn lưu `thoiGianChay` dạng `String`.
- `LoaiGhe`: `GHE_CUNG`, `GHE_MEM`, `GIUONG_NAM`.
- `TrangThaiVe`: `DA_BAN`, `DA_HUY`.
- `VaiTro`: `BAN_VE`, `DIEU_PHOI`, `ADMIN`.
- `TrangThaiNhanVien`: `DANG_LAM`, `NGHI_PHEP`, `DA_NGHI`.

## UI hiện tại
- `Main` mở `LoginModule` trước, sau đăng nhập tạo main frame undecorated, maximized, size mặc định 1280×800, min size 1024×680.
- Sidebar `MenuModule` có nút **Đặt vé** và các nhóm accordion: Tổng quan, Nghiệp vụ, Dữ liệu tàu, Hệ thống.
- Các module chính: `TongQuatModule`, `ThongKeModule`, `BanVeModule`, `QuanLyGiaModule`, `QuanLyKhuyenMaiModule`, `QuanLyVeModule`, `QuanLyHoaDonModule`, `QuanLyDoanTauModule`, `QuanLyToaModule`, `QuanLyDauMayModule`, `QuanLyTuyenModule`, `QuanLyLichChayModule`, `QuanLyNhanVienModule`, `QuanLyKhachHangModule`, `ThongTinCaNhanModule`.
- Luồng bán vé chia nhiều bước: thông tin, chọn chuyến, chọn chỗ, khách hàng, khuyến mãi, xác nhận, thanh toán tiền mặt/chuyển khoản, hoàn thành.
- Giao diện dùng chung `AppColors`, `NotionTheme`, `LineIcons`; ảnh lớn nằm trong `src/main/resources/images/`.
- Dialog/form ưu tiên validation inline, viền lỗi và auto-focus thay vì `JOptionPane` cho lỗi nhập liệu.

## Lưu ý bảo trì
- Không cập nhật tài liệu dựa trên giả định cũ về schema; đối chiếu `BanVeTauNhaGa_MSSQL.sql`, entity và DAO trước.
- Nếu chỉnh UI, đọc `DESIGN.md` và dùng token từ `NotionTheme`/`AppColors`.
- Nếu tạo/chỉnh `*Module.java`, áp dụng pattern module UI độc lập của project.
- Thư mục `document/` là khu vực tài liệu riêng, không chỉnh khi chỉ cập nhật Markdown gốc của repo.

## Cập nhật gần nhất
- **2026-05-21**: Đồng bộ tóm tắt với code hiện tại: Java 21, 21 entity/DAO, 18 bảng SQL, thêm `HoaDonKhachHang` và `ChiTietKhuyenMai`, cập nhật luồng UI/menu và ghi chú kết nối DB thực tế.
