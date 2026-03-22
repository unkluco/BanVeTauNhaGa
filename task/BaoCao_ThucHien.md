# BÁO CÁO THỰC HIỆN DỰ ÁN BAN VÉ TÀU NHÀ GA

## 1. Tổng quan

Dự án xây dựng hệ thống bán vé tàu tại nhà ga bằng Java, sử dụng kiến trúc phân tầng (entity - DAO - connectDB) với cơ sở dữ liệu SQL Server (MSSQL).

## 2. Kết quả thực hiện

### 2.1. Cấu trúc thư mục đã tạo

```
BanVeTauNhaGa/
├── data/
│   └── BanVeTauNhaGa_MSSQL.sql          (có sẵn)
├── document/
│   ├── mo_ta_so_do_lop.docx             (có sẵn)
│   └── N01_4_ApplicationDevelopment_OOAD_V2.docx (có sẵn)
├── task/
│   ├── task_ai_agent.txt                 (có sẵn)
│   └── BaoCao_ThucHien.md               (mới tạo)
└── src/main/java/com/
    ├── connectDB/
    │   └── ConnectDB.java
    ├── enums/
    │   ├── VaiTro.java         (BAN_VE, DIEU_PHOI, ADMIN)
    │   ├── LoaiGhe.java        (GHE_CUNG, GHE_MEM, GIUONG_NAM)
    │   └── TrangThaiVe.java    (DA_BAN, DA_HUY)
    ├── entity/   (17 class)
    │   ├── NhanVien.java
    │   ├── Ga.java
    │   ├── DauMay.java
    │   ├── ToaTau.java
    │   ├── Gia.java
    │   ├── KhuyenMai.java
    │   ├── Tuyen.java
    │   ├── DoanTau.java
    │   ├── ChiTietDoanTau.java
    │   ├── Ghe.java
    │   ├── Lich.java
    │   ├── ChiTietGia.java
    │   ├── HoaDon.java
    │   ├── Ve.java
    │   ├── ChiTietVe.java
    │   ├── ApDungKM.java
    │   └── GiuCho.java
    ├── dao/   (17 class)
    │   ├── DAO_NhanVien.java
    │   ├── DAO_Ga.java
    │   ├── DAO_DauMay.java
    │   ├── DAO_ToaTau.java
    │   ├── DAO_Gia.java
    │   ├── DAO_KhuyenMai.java
    │   ├── DAO_Tuyen.java
    │   ├── DAO_DoanTau.java
    │   ├── DAO_ChiTietDoanTau.java
    │   ├── DAO_Ghe.java
    │   ├── DAO_Lich.java
    │   ├── DAO_ChiTietGia.java
    │   ├── DAO_HoaDon.java
    │   ├── DAO_Ve.java
    │   ├── DAO_ChiTietVe.java
    │   ├── DAO_ApDungKM.java
    │   └── DAO_GiuCho.java
    └── Main.java
```

### 2.2. Chi tiết các class Entity (Bước 3)

Mỗi entity class được tạo với đầy đủ:
- Thuộc tính private theo đúng kiểu dữ liệu từ SQL schema và tài liệu mô tả
- Constructor mặc định (no-arg)
- Constructor với tham số mã (dùng cho FK đơn giản)
- Constructor đầy đủ tham số
- Getter/Setter cho tất cả thuộc tính
- Phương thức toString() để hỗ trợ debug

Các quan hệ FK được thể hiện bằng tham chiếu đối tượng (ví dụ: Tuyen chứa Ga gaDi, Ga gaDen thay vì chỉ String maGa).

Đặc biệt:
- HoaDon có thuộc tính dẫn xuất getTongTien() tính từ danh sách Vé
- GiuCho có phương thức conHieuLuc() kiểm tra thời gian hết hạn
- Ve chứa danh sách ChiTietVe và ApDungKM

### 2.3. Chi tiết các class DAO (Bước 4)

Mỗi DAO class được tạo với các phương thức chuẩn:
- getAll(): Lấy tất cả bản ghi
- findById(): Tìm theo mã (khóa chính)
- insert(): Thêm bản ghi mới
- update(): Cập nhật bản ghi

Ngoài ra, các DAO có phương thức nghiệp vụ riêng:
- DAO_NhanVien: checkLogin(), search()
- DAO_Ga: (CRUD cơ bản)
- DAO_Tuyen: findByGaDiGaDen()
- DAO_Lich: findByTuyenAndDate()
- DAO_Ghe: findByToaTau(), isGheTrong() (kiểm tra ghế trống theo logic tài liệu)
- DAO_Gia: getGiaHienHanh()
- DAO_KhuyenMai: getKhuyenMaiHienHanh()
- DAO_ChiTietGia: findByGiaTuyenLoaiGhe(), findByGia()
- DAO_ChiTietDoanTau: findByDoanTau()
- DAO_HoaDon: phatSinhMaHoaDon(), getHoaDonTrongNgay()
- DAO_Ve: findByHoaDon(), findByCCCD(), huyVe()
- DAO_ChiTietVe: findByVe(), findByLich(), huyChiTietVeByMaVe()
- DAO_ApDungKM: findByVe(), delete()
- DAO_GiuCho: findConHieuLucByLich(), deleteByNhanVien(), deleteExpired()

Tất cả DAO sử dụng PreparedStatement để tránh SQL injection, và xử lý NVARCHAR bằng setNString/getNString cho dữ liệu tiếng Việt.

### 2.4. Class Main (Bước 5)

Class Main thực hiện:
1. Kết nối database thông qua ConnectDB
2. Test tạo đối tượng entity (không cần DB) - kiểm tra constructor và toString()
3. Test từng DAO với dữ liệu mẫu có sẵn trong database
4. In kết quả chi tiết ra console để dễ kiểm tra
5. Đóng kết nối database khi hoàn tất

### 2.5. Tham khảo từ dự án KVStore

Các điểm đã tham khảo từ KVStore:
- Cấu trúc package: connectDB, entity, enums, dao (giống KVStore)
- Pattern ConnectDB Singleton: giữ nguyên cách kết nối SQL Server với JDBC
- Mẫu DAO: PreparedStatement, try-with-resources, mapRow pattern
- Enum pattern: fromAny(), toDbValue(), toString() override

Các điểm khác biệt so với KVStore:
- Database name: BanVeTauNhaGa (thay vì n3_qlCuaHangTienLoi)
- Entity phong phú hơn: 17 entity thay vì 7
- Xử lý DateTime: dùng LocalDateTime + Timestamp thay vì chỉ LocalDate
- Logic nghiệp vụ phức tạp hơn: giữ chỗ tạm, hủy vé, kiểm tra ghế trống

## 3. Khó khăn gặp phải và cách giải quyết

1. **Xử lý NVARCHAR tiếng Việt**: Sử dụng setNString/getNString thay vì setString/getString cho các trường NVARCHAR.

2. **Cột password là từ khóa SQL Server**: Bọc trong dấu ngoặc vuông [password] trong câu SQL.

3. **Quan hệ đối tượng phức tạp**: Nhiều entity có FK lồng nhau (ví dụ: ChiTietVe → Ve → HoaDon → NhanVien). Giải quyết bằng cách mỗi DAO tự inject DAO phụ thuộc và gọi findById để lấy đối tượng liên quan.

4. **Logic kiểm tra ghế trống**: Cần kiểm tra đồng thời cả ChiTietVe (DA_BAN) và GiuCho (còn hiệu lực). Giải quyết bằng câu query CASE WHEN kết hợp NOT EXISTS.

5. **Thuộc tính thoiGianChay kiểu NVARCHAR**: Trong SQL, thoiGianChay được lưu dạng text (ví dụ: "5 giờ 30 phút"). Entity dùng String thay vì Duration để khớp với DB.

## 4. Hướng dẫn chạy thử

1. Chạy file `data/BanVeTauNhaGa_MSSQL.sql` trên SQL Server để tạo database và dữ liệu mẫu
2. Cập nhật thông tin kết nối trong `ConnectDB.java` (url, user, password) nếu cần
3. Thêm JDBC driver `mssql-jdbc` vào classpath (có thể dùng Maven hoặc thêm JAR trực tiếp)
4. Chạy class `com.Main` để kiểm tra tất cả entity và DAO

## 5. Kết luận

Đã hoàn thành đầy đủ các bước theo yêu cầu:
- Bước 1: Đọc và hiểu yêu cầu ✅
- Bước 2: Tham khảo KVStore ✅
- Bước 3: Tạo 17 entity classes ✅
- Bước 4: Tạo 17 DAO classes ✅
- Bước 5: Tạo class Main để test ✅
- Bước 6: Kiểm tra cú pháp ✅
- Bước 7: Viết báo cáo ✅
