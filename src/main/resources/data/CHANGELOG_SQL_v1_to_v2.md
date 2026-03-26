# CHANGELOG: SQL Database Migration v1 → v2

> **Mục đích**: Tài liệu này mô tả toàn bộ thay đổi từ file SQL cũ sang file SQL mới, đã được chỉnh sửa cho khớp với tài liệu thiết kế OOAD (`mo_ta_so_do_lop_v2.docx` + `N01_4_ApplicationDevelopment_OOAD_V3.docx`).
>
> **Đối tượng sử dụng**: Các agent/developer cần cập nhật Entity classes và DAO classes trong dự án Java để đồng bộ với schema mới.

---

## 1. BẢNG BỊ XÓA

### 1.1. `ChiTietVe` — XÓA HOÀN TOÀN

- **Lý do**: Thiết kế mới không có bảng `ChiTietVe`. Chức năng liên kết Vé với Lịch + Ghế đã được chuyển trực tiếp vào bảng `Ve`.
- **Hành động cần làm**:
  - Xóa class `ChiTietVe.java` (entity).
  - Xóa `ChiTietVe_DAO.java`.
  - Xóa mọi import/reference đến `ChiTietVe` trong project.

---

## 2. BẢNG MỚI THÊM

### 2.1. `KhachHang` — BẢNG MỚI HOÀN TOÀN

- **Lý do**: Thiết kế yêu cầu thực thể KhachHang riêng biệt, đại diện cho người mua vé tại quầy. Không có tài khoản đăng nhập.
- **Cấu trúc**:

| Cột | Kiểu SQL | Ràng buộc | Ghi chú |
|-----|----------|-----------|---------|
| `maKhachHang` | VARCHAR(20) | PK | Mã tự động: KH-XXXX |
| `hoTen` | NVARCHAR(100) | NOT NULL | Tối đa 100 ký tự |
| `cccd` | VARCHAR(20) | NOT NULL | CCCD (12 số) hoặc Hộ chiếu |
| `soDienThoai` | VARCHAR(15) | NOT NULL | Format VN (10 số) |

- **Hành động cần làm**:
  - Tạo class `KhachHang.java` với đầy đủ thuộc tính, getter/setter (có validate), 3 constructors, `toString()`, `equals()`/`hashCode()` theo `maKhachHang`.
  - Tạo `KhachHang_DAO.java`.

### 2.2. `ChiTietHoaDon` — BẢNG MỚI (thay thế ChiTietVe)

- **Lý do**: Theo thiết kế, `ChiTietHoaDon` là thực thể trung gian giữa `HoaDon` và `Ve`, bọc lấy một Vé và bổ sung thông tin tài chính (giá thực trả). Quan hệ 1-1 với `Ve`.
- **Cấu trúc**:

| Cột | Kiểu SQL | Ràng buộc | Ghi chú |
|-----|----------|-----------|---------|
| `maChiTietHD` | VARCHAR(20) | PK | Mã tự động |
| `maHoaDon` | VARCHAR(30) | FK → HoaDon, NOT NULL | Thuộc hóa đơn nào |
| `maVe` | VARCHAR(20) | FK → Ve, NOT NULL, **UNIQUE** | Quan hệ 1-1 với Ve |
| `giaTien` | DECIMAL(18,2) | > 0 | Giá thực trả (snapshot tại thời điểm thanh toán) |

- **Hành động cần làm**:
  - Tạo class `ChiTietHoaDon.java` với thuộc tính: `maChiTietHD`, `hoaDon` (HoaDon), `ve` (Ve), `giaTien` (BigDecimal).
  - Phương thức đặc biệt: `tinhGiaTien()` — lấy giá từ ChiTietGia (tuyến + loại ghế + kỳ giá hiện hành) rồi trừ khuyến mãi qua ApDungKM.
  - Setter `setVe()` phải kiểm tra Ve chưa thuộc ChiTietHoaDon khác (do UNIQUE constraint).
  - Tạo `ChiTietHoaDon_DAO.java`.

---

## 3. BẢNG BỊ THAY ĐỔI CẤU TRÚC

### 3.1. `NhanVien` — GIỮ NGUYÊN CỘT `trangThai`

| Thuộc tính | Chi tiết |
|------------|----------|
| **Cột** | `trangThai VARCHAR(20) NOT NULL DEFAULT 'DANG_LAM'` |
| **Giá trị hợp lệ** | `DANG_LAM`, `NGHI_PHEP`, `DA_NGHI` |
| **Lý do** | Nghiệp vụ yêu cầu theo dõi trạng thái công tác của nhân viên |

- **Hành động cần làm**:
  - Đảm bảo `NhanVien.java` có thuộc tính `trangThai` (String hoặc enum `TrangThaiNhanVien`).
  - Có getter/setter: `getTrangThai()`, `setTrangThai()`.
  - Constructors bao gồm param `trangThai`.
  - `NhanVien_DAO.java`: thêm `trangThai` vào mọi câu INSERT/UPDATE/SELECT.
  - Tạo enum `TrangThaiNhanVien` với 3 giá trị: `DANG_LAM`, `NGHI_PHEP`, `DA_NGHI` (nếu chưa có).

### 3.2. `Ve` — THAY ĐỔI LỚN (tái cấu trúc hoàn toàn)

**Cấu trúc CŨ** (sai):

| Cột | Ghi chú |
|-----|---------|
| maVe | PK |
| maHoaDon | FK → HoaDon |
| tenHanhKhach | Tên hành khách |
| cccd | CCCD hành khách |
| giaTien | Giá vé |
| trangThai | DA_BAN / DA_HUY |
| lyDoHuy | Lý do hủy |
| ngayHuy | Ngày hủy |

**Cấu trúc MỚI** (đúng thiết kế):

| Cột | Kiểu SQL | Ràng buộc | Ghi chú |
|-----|----------|-----------|---------|
| `maVe` | VARCHAR(20) | PK | Mã tự động: VE-XXX |
| `maLich` | VARCHAR(20) | FK → Lich, NOT NULL | **MỚI** - Lịch chạy tàu |
| `maGhe` | VARCHAR(20) | FK → Ghe, NOT NULL | **MỚI** - Ghế được chọn |
| `trangThai` | VARCHAR(10) | DA_BAN / DA_HUY | Giữ nguyên |
| `lyDoHuy` | NVARCHAR(255) | Nullable | Giữ nguyên |
| `ngayHuy` | DATETIME | Nullable | Giữ nguyên |

**Tóm tắt thay đổi**:
- **XÓA**: `maHoaDon`, `tenHanhKhach`, `cccd`, `giaTien`
- **THÊM**: `maLich` (FK → Lich), `maGhe` (FK → Ghe)

- **Hành động cần làm**:
  - Thay đổi `Ve.java`: xóa các thuộc tính `hoaDon`, `tenHanhKhach`, `cccd`, `giaTien`. Thêm `lich` (Lich), `ghe` (Ghe).
  - Setter `setGhe()` phải validate: ghế phải thuộc DoanTau của Lich (qua ChiTietDoanTau).
  - Phương thức `huyVe(String lyDo)`: cập nhật trangThai = DA_HUY, ghi lyDoHuy và ngayHuy.
  - Cập nhật constructors.
  - Viết lại `Ve_DAO.java` hoàn toàn.

### 3.3. `HoaDon` — THÊM CỘT `maKhachHang`

| Thay đổi | Chi tiết |
|----------|----------|
| **Thêm** | Cột `maKhachHang VARCHAR(20) NOT NULL, FK → KhachHang` |
| **Lý do** | Thiết kế yêu cầu HoaDon gắn với KhachHang đứng ra mua |

- **Hành động cần làm**:
  - Thêm thuộc tính `khachHang` (KhachHang) vào `HoaDon.java`.
  - Thêm getter/setter: `getKhachHang()`, `setKhachHang()` (NOT NULL).
  - Cập nhật constructors (thêm param `khachHang`).
  - Phương thức `tinhTongTien()`: tổng `giaTien` của các ChiTietHoaDon.
  - Cập nhật `HoaDon_DAO.java`: thêm `maKhachHang` vào INSERT/UPDATE/SELECT, thêm JOIN với KhachHang.

### 3.4. `ApDungKM` — ĐỔI FK TỪ `Ve` SANG `ChiTietHoaDon`

**Cấu trúc CŨ**:

| Cột | FK |
|-----|----|
| maVe | FK → Ve |
| maKhuyenMai | FK → KhuyenMai |

**Cấu trúc MỚI**:

| Cột | Kiểu SQL | Ràng buộc | Ghi chú |
|-----|----------|-----------|---------|
| `maApDung` | VARCHAR(20) | PK | Mã tự động |
| `maChiTietHD` | VARCHAR(20) | FK → ChiTietHoaDon, NOT NULL | **ĐỔI** từ maVe |
| `maKhuyenMai` | VARCHAR(20) | FK → KhuyenMai, NOT NULL | Giữ nguyên |

- **Hành động cần làm**:
  - Đổi thuộc tính `ve` → `chiTietHoaDon` (ChiTietHoaDon) trong `ApDungKM.java`.
  - Setter `setKhuyenMai()` phải kiểm tra `km.conHieuLuc() == true`.
  - Cập nhật `ApDungKM_DAO.java`.

---

## 4. THAY ĐỔI KIỂU DỮ LIỆU

### 4.1. Tiền tệ: `FLOAT` → `DECIMAL(18,2)`

| Bảng | Cột | Cũ | Mới | Java type |
|------|-----|----|-----|-----------|
| `ChiTietGia` | `giaNiemYet` | FLOAT | DECIMAL(18,2) | BigDecimal |
| `ChiTietHoaDon` | `giaTien` | *(mới)* | DECIMAL(18,2) | BigDecimal |

- **Lý do**: BigDecimal trong thiết kế Java → DECIMAL trong SQL để tránh lỗi làm tròn tiền tệ.
- **Hành động**: Đảm bảo DAO dùng `getBigDecimal()` / `setBigDecimal()` thay vì `getFloat()`.

### 4.2. Phần trăm: `FLOAT` → `DECIMAL(5,2)`

| Bảng | Cột | Cũ | Mới |
|------|-----|----|-----|
| `KhuyenMai` | `phanTramGiam` | FLOAT | DECIMAL(5,2) |

### 4.3. Thời gian chạy: `NVARCHAR(50)` → `INT`

| Bảng | Cột | Cũ | Mới | Ghi chú |
|------|-----|----|-----|---------|
| `Lich` | `thoiGianChay` | NVARCHAR(50) | INT | Đơn vị: **phút**. VD: 330 = 5h30p |

- **Lý do**: Kiểu Duration trong Java map tự nhiên hơn sang INT (phút) thay vì chuỗi text.
- **Hành động**:
  - Trong `Lich.java`, thuộc tính `thoiGianChay` kiểu `Duration`. 
  - DAO đọc INT từ DB rồi convert: `Duration.ofMinutes(rs.getInt("thoiGianChay"))`.
  - DAO ghi: `ps.setInt(idx, (int) lich.getThoiGianChay().toMinutes())`.

---

## 5. THAY ĐỔI DỮ LIỆU MẪU

### 5.1. NhanVien — Giữ nguyên cột `trangThai`

Tất cả INSERT bao gồm cột `trangThai` (cột thứ 6). Phân bổ dữ liệu mẫu:

| Trạng thái | Số lượng NV | Ghi chú |
|------------|-------------|---------|
| `DANG_LAM` | 16 | Đang làm việc bình thường |
| `NGHI_PHEP` | 4 | NV-0005, NV-0010, NV-0015, NV-0021 |
| `DA_NGHI` | 3 | NV-0020, NV-0022, NV-0023 |

### 5.2. KhachHang — Dữ liệu mới

5 khách hàng được tạo từ thông tin hành khách cũ trong bảng `Ve`:

| Mã | Họ tên | CCCD | SĐT |
|----|--------|------|-----|
| KH-0001 | Phạm Minh Tuấn | 012345678901 | 0371234567 |
| KH-0002 | Hoàng Đức Mạnh | 034567890123 | 0382345678 |
| KH-0003 | Nguyễn Thị Hoa | 056789012345 | 0393456789 |
| KH-0004 | Trần Văn Đức | 078901234567 | 0354567890 |
| KH-0005 | Lý Văn Hùng | 090123456789 | 0365678901 |

### 5.3. Ve — Tái cấu trúc hoàn toàn

**Mapping dữ liệu cũ → mới**:

Vé cũ có thể có nhiều ChiTietVe (multi-leg). Vé mới mỗi vé = 1 ghế trên 1 lịch. Vé multi-leg cũ được tách thành nhiều vé mới.

| Vé cũ | ChiTietVe cũ | → Vé mới | Lịch | Ghế |
|-------|-------------|----------|------|-----|
| VE-001 | CTV-001 | VE-001 | LCH-001 | G-003-01 |
| VE-002 | CTV-002 | VE-002 | LCH-001 | G-003-02 |
| VE-003 | CTV-003 | VE-003 | LCH-003 | G-006-01 |
| VE-004 | CTV-004 | VE-004 | LCH-005 | G-007-01 |
| VE-005 | CTV-005 | VE-005 | LCH-002 | G-001-01 |
| VE-005 | CTV-006 | **VE-006** | LCH-004 | G-002-01 |
| VE-006 | CTV-007 | **VE-007** | LCH-002 | G-001-02 |
| VE-006 | CTV-008 | **VE-008** | LCH-004 | G-002-02 |
| VE-007 | CTV-009 | **VE-009** | LCH-006 | G-003-01 |

> Lưu ý: VE-005 và VE-006 cũ (multi-leg HN→Vinh→Huế) đã tách thành 4 vé riêng biệt.

### 5.4. HoaDon — Thêm `maKhachHang`

| Mã HoaDon | KhachHang |
|-----------|-----------|
| HD-10042026-001 | KH-0001 |
| HD-10042026-002 | KH-0002 |
| HD-10042026-003 | KH-0003 |
| HD-11042026-001 | KH-0004 |
| HD-12042026-001 | KH-0005 |

### 5.5. ChiTietHoaDon — Dữ liệu mới (thay ChiTietVe)

| Mã CTHD | HoaDon | Vé | Giá (VNĐ) | Ghi chú |
|---------|--------|-----|-----------|---------|
| CTHD-001 | HD-10042026-001 | VE-001 | 250,000 | Ghế mềm TUY-001 |
| CTHD-002 | HD-10042026-001 | VE-002 | 250,000 | Ghế mềm TUY-001 |
| CTHD-003 | HD-10042026-002 | VE-003 | 450,000 | Giường nằm TUY-002 |
| CTHD-004 | HD-10042026-003 | VE-004 | 68,000 | Ghế cứng TUY-003, SV giảm 15% |
| CTHD-005 | HD-11042026-001 | VE-005 | 180,000 | Ghế cứng TUY-001 (chặng 1) |
| CTHD-006 | HD-11042026-001 | VE-006 | 200,000 | Ghế cứng TUY-002 (chặng 2) |
| CTHD-007 | HD-11042026-001 | VE-007 | 180,000 | Ghế cứng TUY-001 (chặng 1) |
| CTHD-008 | HD-11042026-001 | VE-008 | 200,000 | Ghế cứng TUY-002 (chặng 2) |
| CTHD-009 | HD-12042026-001 | VE-009 | 600,000 | Ghế mềm TUY-004, đã hủy |

### 5.6. ApDungKM — Đổi FK

| Cũ | Mới |
|----|-----|
| `('ADKM-001', 'VE-004', 'KM-002')` | `('ADKM-001', 'CTHD-004', 'KM-002')` |

### 5.7. Lich — `thoiGianChay` đổi sang số phút

| Mã Lịch | Cũ (NVARCHAR) | Mới (INT phút) |
|---------|---------------|-----------------|
| LCH-001 | '5 giờ 30 phút' | 330 |
| LCH-002 | '5 giờ 30 phút' | 330 |
| LCH-003 | '6 giờ' | 360 |
| LCH-004 | '6 giờ' | 360 |
| LCH-005 | '2 giờ 30 phút' | 150 |
| LCH-006 | '17 giờ' | 1020 |
| LCH-007 | '17 giờ' | 1020 |
| LCH-008 | '33 giờ' | 1980 |

---

## 6. BẢNG KHÔNG THAY ĐỔI

Các bảng sau giữ nguyên cấu trúc và dữ liệu:

- `Ga`
- `DauMay`
- `ToaTau`
- `Tuyen`
- `DoanTau`
- `ChiTietDoanTau`
- `Ghe`
- `Gia`
- `GiuCho`

---

## 7. TỔNG KẾT SỐ LƯỢNG BẢNG

| | Cũ | Mới |
|--|---:|----:|
| Tổng số bảng | 17 | **18** |
| Bảng xóa | — | 1 (`ChiTietVe`) |
| Bảng thêm | — | 2 (`KhachHang`, `ChiTietHoaDon`) |
| Bảng sửa cấu trúc | — | 4 (`NhanVien`, `Ve`, `HoaDon`, `ApDungKM`) |
| Bảng đổi kiểu dữ liệu | — | 3 (`ChiTietGia`, `KhuyenMai`, `Lich`) |

---

## 8. CHECKLIST CHO AGENT CẬP NHẬT CODE

- [ ] Xóa `ChiTietVe.java` + `ChiTietVe_DAO.java`
- [ ] Tạo `KhachHang.java` + `KhachHang_DAO.java`
- [ ] Tạo `ChiTietHoaDon.java` + `ChiTietHoaDon_DAO.java`
- [ ] Đảm bảo `NhanVien.java` có thuộc tính `trangThai` (enum `TrangThaiNhanVien`: `DANG_LAM`, `NGHI_PHEP`, `DA_NGHI`)
- [ ] Đảm bảo `NhanVien_DAO.java` ánh xạ cột `trangThai` trong INSERT/UPDATE/SELECT
- [ ] Sửa `Ve.java`: xóa `hoaDon/tenHanhKhach/cccd/giaTien`, thêm `lich/ghe`
- [ ] Viết lại `Ve_DAO.java` hoàn toàn
- [ ] Sửa `HoaDon.java`: thêm thuộc tính `khachHang`
- [ ] Sửa `HoaDon_DAO.java`: thêm `maKhachHang` vào SQL + JOIN
- [ ] Sửa `ApDungKM.java`: đổi `ve` → `chiTietHoaDon`
- [ ] Sửa `ApDungKM_DAO.java`: đổi FK column
- [ ] Sửa `Lich_DAO.java`: `thoiGianChay` đọc/ghi INT (phút) ↔ Duration
- [ ] Sửa `ChiTietGia_DAO.java`: `giaNiemYet` dùng `getBigDecimal()`
- [ ] Sửa `KhuyenMai_DAO.java`: `phanTramGiam` dùng `getBigDecimal()`
- [ ] Cập nhật tất cả UI/Service classes reference đến các entity đã thay đổi
