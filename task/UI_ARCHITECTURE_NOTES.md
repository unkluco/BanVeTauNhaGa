# Ghi chú Kiến trúc UI — Azure Rail (BanVeTauNhaGa)

## Quy tắc chung

### 0. Có thể sửa file quy tắc này nếu được người dùng yêu cầu
- Có thể sửa nội dung các quy tắc này nếu được người dùng yêu cầu
- Có thể sửa nội dung các quy tắc này nếu thấy người dùng thường xuyên không muốn dùng các quy tắc không đây.
- Có thể sửa nội dung các quy tắc này nếu nó thật sự cần thiết để phát triển lâu dài ( nếu thật sự cần thiết và nhớ hỏi ý kiến người dùng ).

### 1. Ẩn thanh tiêu đề (title bar) trên TẤT CẢ cửa sổ
- Tất cả JFrame và JDialog đều dùng `setUndecorated(true)`.
- **Quy tắc nút đóng**: Nếu cửa sổ đã có nút "Hủy" hoặc "Đăng xuất" thì KHÔNG thêm nút X. Ngược lại, thêm nút X dùng `Main.createCloseButton()` (public static).
- Login frame: nút X ở góc phải trên (glass pane overlay).
- Main frame: có nút "Đăng xuất" ở sidebar → KHÔNG cần nút X riêng.
- ThemNhanVienDialog / SuaNhanVienDialog: có nút "Hủy bỏ" → KHÔNG có nút X.
- ModuleLauncher.asDialog: tự thêm topBar với nút X.

### 2. Cửa sổ chính (Main frame)
- Luôn mở fullscreen: `setExtendedState(JFrame.MAXIMIZED_BOTH)`.
- Kích thước tối thiểu: `setMinimumSize(new Dimension(1024, 680))`.
- Kích thước mặc định: 1280×800.

### 3. Tính toán số hàng bảng (rowsPerPage)
- Dùng `calcRowsFromViewport()` tính từ chiều cao JViewport thực tế.
- Công thức: `available / rowHeight + 1` (cộng 1 để tránh hở khoảng trống).
- Fallback: nếu viewport chưa sẵn sàng, ước lượng từ chiều cao màn hình.
- Dùng `JViewport.addComponentListener` để tự cập nhật khi resize.
- Dùng cờ `isRefreshing` để ngăn gọi đệ quy `refreshTable()`.

### 4. Bộ lọc QuanLyNhanVienModule
- Thanh tìm kiếm tự giãn (BorderLayout.CENTER), các bộ lọc nằm bên phải (EAST).
- Tìm kiếm trực tiếp khi gõ (DocumentListener), không cần nhấn Enter.
- Nút "Bỏ lọc" để reset tất cả bộ lọc về mặc định.

### 5. ThemNhanVienDialog / SuaNhanVienDialog
- Bộ phận (vai trò): "Bán vé", "Điều phối", "Admin" — map sang VaiTro enum.
- Trạng thái: "Đang làm", "Nghỉ phép", "Đã nghỉ" — map sang TrangThaiNhanVien enum.
- Layout: Row1 (Mã NV + Họ tên), Row2 (SĐT + CCCD), Row3 (Bộ phận + Trạng thái), Row4 (Mật khẩu + Địa chỉ tạm trú).
- **Inline validation**: Lỗi hiển thị ngay dưới ô nhập (JLabel đỏ), viền ô chuyển đỏ, tự focus vào ô lỗi. KHÔNG dùng JOptionPane cho lỗi validation.
- SuaNhanVienDialog: Mã NV readonly, mật khẩu tùy chọn (để trống = giữ nguyên).

### 7. Quy tắc canh giữa dọc (vertical centering)
- Khi đặt nút/component trong header/toolbar, dùng `GridBagLayout` hoặc `FlowLayout` với vgap phù hợp để canh giữa dọc. TRÁNH dùng `FlowLayout(..., 0)` vì component sẽ đụng trần.
- Nút action trong bảng (ví dụ "Chỉnh sửa"): dùng JPanel(GridBagLayout) bọc ngoài JButton để chỉ trigger khi click đúng nút, không phải toàn ô.

### 8. Tài nguyên hình ảnh
- Icons nhỏ (nút, nav): `src/main/resources/icons/` — đặt tên tiếng Việt không dấu, ví dụ `nutThem.png`, `bieuTuongNhanVien.png`.
- Ảnh trang trí lớn (logo, nền): `src/main/resources/images/` — ví dụ `logoAzureRail.png`, `hinhNenDangNhap.png`.
- Load qua `getClass().getResource("/icons/tenFile.png")` hoặc `/images/tenFile.png`.

### 6. QuanLyVeModule
- Tiêu đề: "Quản lý vé" (KHÔNG phải "Quản lý vé và hóa đơn").
- Vé KHÔNG cho phép sửa inline (isCellEditable chỉ true cho cột action).
- Thông tin khách hàng lấy từ `DAO_ChiTietHoaDon.findByVe()`.
- Thông tin tuyến lấy từ `ve.getLich().getTuyen()`.

## Schema SQL quan trọng
- **Ve**: `(maVe, maLich, maGhe, trangThai, lyDoHuy, ngayHuy)` — KHÔNG có maHoaDon/tenHanhKhach/cccd/giaTien.
- **HoaDon**: `(maHoaDon, maNV, maKhachHang, ngayLap)` — có FK tới KhachHang.
- **ChiTietHoaDon**: `(maChiTietHD, maHoaDon, maVe, giaTien)` — liên kết Ve↔HoaDon (1:1 qua UNIQUE trên maVe).
- **KhachHang**: `(maKhachHang, hoTen, cccd, soDienThoai)`.
- **Lich**: `thoiGianChay` là `INT` (phút), KHÔNG phải NVARCHAR.

## Design tokens (màu chung)
- PRIMARY: `#005D90`
- SURFACE: `#F8FAFC` / `#F7F9FB`
- OUTLINE: `#DEE3E8`
- TEXT_MUTED: `#64748B`
- ERROR: `#BA1A1A`
