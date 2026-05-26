# Ghi chú Kiến trúc UI — Azure Rail (BanVeTauNhaGa)

> File này ghi lại các quy ước UI đang được code áp dụng. Khi chỉnh UI, đối chiếu với `DESIGN.md`, `NotionTheme` và `AppColors` trước khi sửa.

## Quy tắc cửa sổ

### 1. Frame/Dialog không dùng title bar mặc định
- `Main` dùng `setUndecorated(true)` cho login frame và main frame.
- Dialog/module mở dạng dialog nên dùng cùng phong cách không title bar mặc định nếu đã có top bar custom.
- Nếu cửa sổ đã có nút **Hủy**, **Đóng** hoặc **Đăng xuất**, không thêm nút X thứ hai để tránh trùng hành vi.
- Nếu cần nút X custom, dùng `Main.createCloseButton()` để đồng bộ style.

### 2. Main frame
- Sau đăng nhập, `Main` tạo `MenuModule` và hiển thị nội dung chính.
- Kích thước mặc định: 1280×800.
- Kích thước tối thiểu: 1024×680.
- Trạng thái mở: `JFrame.MAXIMIZED_BOTH`.
- Main frame không có nút X riêng vì sidebar đã có **Đăng xuất**.

## Điều hướng chính

### 3. Sidebar `MenuModule`
- Nút nổi bật đầu sidebar: **Đặt vé** mở `BanVeModule`.
- Nhóm **Tổng quan**: `TongQuatModule`, `ThongKeModule`.
- Nhóm **Nghiệp vụ**: `QuanLyGiaModule`, `QuanLyKhuyenMaiModule`, `QuanLyVeModule`, `QuanLyHoaDonModule`.
- Nhóm **Dữ liệu tàu**: `QuanLyDoanTauModule`, `QuanLyToaModule`, `QuanLyDauMayModule`, `QuanLyTuyenModule`, `QuanLyLichChayModule`.
- Nhóm **Hệ thống**: `QuanLyNhanVienModule`, `QuanLyKhachHangModule`.
- Cuối sidebar có **Thông tin cá nhân** và **Đăng xuất**.

### 4. Điều hướng từ dashboard
- `TongQuatModule` có callback điều hướng sang module khác kèm criteria/từ khóa tìm kiếm.
- `MenuModule.navigateWithSearch(...)` nhận cả chuỗi tìm kiếm cũ và object criteria mới.
- Các module nhận criteria nên reset/filter lại dữ liệu mà không tạo luồng điều hướng phụ.

## Quy tắc bảng và phân trang

### 5. Tính số hàng vừa khung
- Các bảng quản lý dùng tính toán rows-per-page theo chiều cao viewport thực tế khi có thể.
- Khi viewport chưa sẵn sàng, dùng fallback dựa trên kích thước màn hình hoặc chiều cao cố định hợp lý.
- Khi resize, cập nhật lại số dòng qua listener nhưng phải có guard như `isRefreshing` để tránh vòng lặp `refreshTable()`.
- Pagination bar nên nằm sát ngay dưới bảng, không tạo khoảng trắng lớn giữa dòng cuối và điều hướng trang.

### 6. Action trong bảng
- Cột action chỉ editable ở đúng cột thao tác, không cho sửa inline dữ liệu nghiệp vụ nếu module không thiết kế cho việc đó.
- Nút trong cell nên bọc bằng panel căn giữa như `GridBagLayout` để vùng click đúng vào nút, không phải toàn bộ ô.
- Các hành động nguy hiểm như hủy vé/xóa/ngừng khai thác phải có xác nhận hoặc dialog nghiệp vụ rõ ràng.

## Form và validation

### 7. Validation inline
- Lỗi nhập liệu hiển thị bằng label đỏ dưới field, đổi viền field sang màu lỗi và focus vào lỗi đầu tiên.
- Không dùng `JOptionPane` cho lỗi validation thường gặp trong form.
- Dialog có nút **Hủy** thì không thêm nút X riêng.
- Field mã định danh khi sửa thường readonly; mật khẩu trong form sửa nhân viên để trống nghĩa là giữ nguyên nếu module đang áp dụng quy tắc đó.

### 8. Căn chỉnh layout
- Header, toolbar và filter bar nên dùng `GridBagLayout`, `BorderLayout` hoặc `FlowLayout` có `vgap` hợp lý để căn giữa dọc.
- Tránh `FlowLayout(..., 0)` trong vùng toolbar vì dễ làm component đụng trần.
- Search field trong filter bar nên có khả năng giãn theo chiều ngang; các combobox/nút lọc đặt bên phải.

## Design system

### 9. Token màu
- `AppColors` là bộ màu ứng dụng kiểu Azure Rail: `PRIMARY #006494`, `BACKGROUND #F8FAFC`, `SURFACE #FFFFFF`, `BORDER #DEE3E8`, `ERROR #DC2626`.
- `NotionTheme` là lớp theme Notion/getdesign: `PAGE #FAFAF9`, `SIDEBAR #F6F5F4`, `CARD`, `BORDER`, `TEXT`, `TEXT_MUTED`, `ACCENT #5645D4`.
- Khi làm UI mới theo yêu cầu hiện tại, ưu tiên `NotionTheme`; chỉ dùng `AppColors` khi module cũ hoặc component nghiệp vụ đã phụ thuộc token đó.

### 10. Icon và ảnh
- Icon nhỏ trong nút/nav dùng `LineIcons` Java2D, không thêm SVG mới nếu chưa cần.
- Ảnh trang trí lớn dùng `src/main/resources/images/`, ví dụ logo/nền đăng nhập.
- Load icon qua `LineIcons.of(...)`, `LineIcons.image(...)` hoặc `LineIcons.contained(...)` để đồng bộ kích thước/màu.

## Ghi chú nghiệp vụ UI

### 11. `QuanLyVeModule`
- Tiêu đề hiện tại là **Quản lý vé**, không dùng lại nhãn cũ **Quản lý vé và hóa đơn**.
- Vé không sửa inline dữ liệu chính; thao tác nghiệp vụ đi qua action/dialog.
- Thông tin tuyến lấy từ `ve.getLich().getTuyen()`.
- Thông tin hóa đơn/khách hàng phải đi qua quan hệ `ChiTietHoaDon`, `HoaDon` và `HoaDonKhachHang` theo schema mới.

### 12. Bán vé nhiều bước
- `BanVeModule` là container luồng đặt vé.
- Các bước hiện có: `BanVeStep1Module`, `BanVeStep2Module`, `BanVeStep3Module`, `BanVeStep5Module`, `BanVeStep5bModule`, `BanVeStep6Module`, `BanVeStep7TienMatModule`, `BanVeStep7ChuyenKhoanModule`, `BanVeStep8Module`.
- Step khách hàng và khuyến mãi phải khớp schema mới: hóa đơn có thể liên kết khách hàng qua `HoaDonKhachHang`, khuyến mãi áp dụng qua `ChiTietKhuyenMai`.

## Schema cần nhớ khi làm UI
- `Ve(maVe, maLich, maGhe, trangThai, lyDoHuy, ngayHuy)` không chứa trực tiếp hành khách, hóa đơn hoặc giá tiền.
- `HoaDon(maHoaDon, maNV, ngayLap)` không chứa trực tiếp `maKhachHang`.
- `HoaDonKhachHang(maHDKH, maHoaDon, maKhachHang)` là bảng nối hóa đơn-khách hàng.
- `ChiTietHoaDon(maChiTietHD, maHoaDon, maVe, maChiTietGia, giaTien)` là snapshot vé trong hóa đơn và unique theo `maVe`.
- `ApDungKM(maApDung, maChiTietHD, maChiTietKM)` trỏ tới chi tiết khuyến mãi, không trỏ thẳng tới kỳ khuyến mãi.
- `Lich.thoiGianChay` trong SQL là `INT` phút; entity hiện vẫn giữ dạng `String`, nên DAO/UI phải chuyển đổi cẩn thận khi đọc/ghi.
