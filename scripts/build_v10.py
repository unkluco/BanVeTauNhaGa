"""
build_v10.py
1. Fix 3.1.2 class tables: rescale widths 9930→8788, fix data errors
2. Insert 3.2.3 "Các ràng buộc toàn vẹn trong CSDL" tables
Base: document/9_REPORT_v9.docx → output: document/9_REPORT_v10.docx
"""
import zipfile, os, re

SRC = "document/9_REPORT_v9.docx"
DST = "document/9_REPORT_v10.docx"

# ──────────────────────────────────────────────────────────────────────────────
# XML helpers (shared for both 3.1.2 and 3.2.3)
# ──────────────────────────────────────────────────────────────────────────────

def esc(s):
    return (str(s).replace("&","&amp;").replace("<","&lt;")
            .replace(">","&gt;").replace('"',"&quot;"))

def _p(text, bold=False, center=False, shd=None,
       sp_before="240", sp_after="0", line="360"):
    jc = "center" if center else "left"
    b = "<w:b w:val=\"1\"/><w:bCs w:val=\"1\"/>" if bold else ""
    shd_xml = f"<w:shd w:fill=\"{shd}\" w:val=\"clear\"/>" if shd else ""
    return (
        "<w:p><w:pPr>"
        + shd_xml
        + f"<w:spacing w:before=\"{sp_before}\" w:line=\"{line}\" w:lineRule=\"auto\"/>"
        "<w:ind w:firstLine=\"0\"/>"
        f"<w:jc w:val=\"{jc}\"/>"
        f"<w:rPr>{b}</w:rPr></w:pPr>"
        f"<w:r><w:rPr>{b}</w:rPr>"
        f"<w:t xml:space=\"preserve\">{esc(text)}</w:t></w:r></w:p>"
    )

def empty_para():
    return "<w:p><w:pPr><w:spacing w:after=\"120\"/></w:pPr></w:p>"

# ══════════════════════════════════════════════════════════════════════════════
# SECTION 3.1.2 — Đặc tả lớp  (5 cols, widths 8788)
# ══════════════════════════════════════════════════════════════════════════════

W312 = [863, 2614, 1673, 2164, 1474]   # sum = 8788

def _tc312_hdr(w, text):
    return (
        "<w:tc><w:tcPr>"
        f"<w:tcW w:w=\"{w}\" w:type=\"dxa\"/>"
        "<w:tcBorders>"
        "<w:top w:val=\"single\" w:sz=\"4\" w:color=\"000000\"/>"
        "<w:left w:val=\"single\" w:sz=\"4\" w:color=\"000000\"/>"
        "<w:bottom w:val=\"single\" w:sz=\"4\" w:color=\"000000\"/>"
        "<w:right w:val=\"single\" w:sz=\"4\" w:color=\"000000\"/>"
        "</w:tcBorders>"
        "<w:shd w:fill=\"cccccc\" w:val=\"clear\"/>"
        "<w:tcMar><w:top w:w=\"0\" w:type=\"dxa\"/><w:left w:w=\"100\" w:type=\"dxa\"/>"
        "<w:bottom w:w=\"0\" w:type=\"dxa\"/><w:right w:w=\"100\" w:type=\"dxa\"/></w:tcMar>"
        "<w:vAlign w:val=\"top\"/></w:tcPr>"
        + _p(text, bold=True, shd="cccccc") + "</w:tc>"
    )

def _tc312_data(w, text, first_col=False, bold=False):
    left = ("<w:left w:val=\"single\" w:sz=\"5\" w:color=\"000000\"/>"
            if first_col else
            "<w:left w:val=\"nil\" w:sz=\"0\" w:color=\"000000\"/>")
    return (
        "<w:tc><w:tcPr>"
        f"<w:tcW w:w=\"{w}\" w:type=\"dxa\"/>"
        "<w:tcBorders>"
        "<w:top w:val=\"nil\" w:sz=\"0\" w:color=\"000000\"/>"
        + left +
        "<w:bottom w:val=\"single\" w:sz=\"5\" w:color=\"000000\"/>"
        "<w:right w:val=\"single\" w:sz=\"5\" w:color=\"000000\"/>"
        "</w:tcBorders>"
        "<w:tcMar><w:top w:w=\"0\" w:type=\"dxa\"/><w:left w:w=\"100\" w:type=\"dxa\"/>"
        "<w:bottom w:w=\"0\" w:type=\"dxa\"/><w:right w:w=\"100\" w:type=\"dxa\"/></w:tcMar>"
        "<w:vAlign w:val=\"top\"/></w:tcPr>"
        + _p(text, bold=bold) + "</w:tc>"
    )

def make_312_table(fields):
    """fields: list of (stt, ten, kieu, rang_buoc, ghi_chu)"""
    total = sum(W312)
    grid = "".join(f"<w:gridCol w:w=\"{w}\"/>" for w in W312)
    # header row
    hdrs = ["STT", "Công việc", "Kiểu dữ liệu", "Ràng buộc", "Ghi chú"]
    hdr_row = (
        "<w:tr><w:trPr><w:cantSplit w:val=\"0\"/>"
        "<w:trHeight w:val=\"405\" w:hRule=\"atLeast\"/>"
        "<w:tblHeader w:val=\"1\"/></w:trPr>"
        + "".join(_tc312_hdr(W312[i], hdrs[i]) for i in range(5))
        + "</w:tr>"
    )
    def drow(stt, c2, c3, c4, c5, bold2=False):
        return (
            "<w:tr><w:trPr><w:cantSplit w:val=\"0\"/>"
            "<w:trHeight w:val=\"405\" w:hRule=\"atLeast\"/>"
            "<w:tblHeader w:val=\"0\"/></w:trPr>"
            + _tc312_data(W312[0], stt,  first_col=True, bold=bold2)
            + _tc312_data(W312[1], c2,   bold=bold2)
            + _tc312_data(W312[2], c3)
            + _tc312_data(W312[3], c4)
            + _tc312_data(W312[4], c5)
            + "</w:tr>"
        )
    sub_row = drow("1", "Khai báo thuộc tính:", "", "", "", bold2=True)
    data_rows = "".join(drow(s, t, k, r, n) for s, t, k, r, n in fields)
    return (
        "<w:tbl><w:tblPr>"
        "<w:tblStyle w:val=\"Table9\"/>"
        f"<w:tblW w:w=\"{total}\" w:type=\"dxa\"/>"
        "<w:jc w:val=\"left\"/>"
        "<w:tblBorders>"
        "<w:top w:val=\"nil\" w:sz=\"0\" w:color=\"000000\"/>"
        "<w:left w:val=\"nil\" w:sz=\"0\" w:color=\"000000\"/>"
        "<w:bottom w:val=\"nil\" w:sz=\"0\" w:color=\"000000\"/>"
        "<w:right w:val=\"nil\" w:sz=\"0\" w:color=\"000000\"/>"
        "<w:insideH w:val=\"nil\" w:sz=\"0\" w:color=\"000000\"/>"
        "<w:insideV w:val=\"nil\" w:sz=\"0\" w:color=\"000000\"/>"
        "</w:tblBorders>"
        "<w:tblLayout w:type=\"fixed\"/>"
        "<w:tblLook w:val=\"0600\"/>"
        "</w:tblPr>"
        f"<w:tblGrid>{grid}</w:tblGrid>"
        + hdr_row + sub_row + data_rows
        + "</w:tbl>"
    )

def make_title_312(num, name):
    label = f"3.1.2.{num:02d}. Thực thể {name}:"
    return (
        "<w:p><w:pPr>"
        "<w:spacing w:after=\"80\" w:before=\"360\" w:line=\"259\" w:lineRule=\"auto\"/>"
        "<w:ind w:firstLine=\"0\"/><w:jc w:val=\"left\"/>"
        "<w:rPr><w:b w:val=\"1\"/><w:bCs w:val=\"1\"/></w:rPr></w:pPr>"
        "<w:r><w:rPr><w:b w:val=\"1\"/><w:bCs w:val=\"1\"/></w:rPr>"
        f"<w:t>{esc(label)}</w:t></w:r></w:p>"
    )

# ─── Entity data (corrected vs v9: gaLamViec→Ga, cccd nullable, soDienThoai not null, diaChi not null, phanTramGiam constraint)
ENTITIES_312 = [
    ("Ga", [
        ("1.1", "maGa",   "String",  "Khóa chính, không rỗng, duy nhất", "Mã ga"),
        ("1.2", "tenGa",  "String",  "Không rỗng",                       "Tên ga"),
        ("1.3", "diaChi", "String",  "Không rỗng",                       "Địa chỉ ga"),
    ]),
    ("Tuyen", [
        ("1.1", "maTuyen",   "String",  "Khóa chính, không rỗng, duy nhất",     "Mã tuyến"),
        ("1.2", "gaDi",      "Ga",      "Khóa ngoại → Ga, không rỗng",          "Ga đi"),
        ("1.3", "gaDen",     "Ga",      "Khóa ngoại → Ga, không rỗng",          "Ga đến"),
        ("1.4", "km",        "int",     "Không rỗng, mặc định: 0",              "Khoảng cách (km)"),
        ("1.5", "hoatDong",  "boolean", "Không rỗng, mặc định: true",           "Trạng thái hoạt động"),
    ]),
    ("DauMay", [
        ("1.1", "maDauMay",     "String",  "Khóa chính, không rỗng, duy nhất", "Mã đầu máy"),
        ("1.2", "tenDauMay",    "String",  "Không rỗng",                       "Tên đầu máy"),
        ("1.3", "hangSanXuat",  "String",  "Có thể rỗng",                      "Hãng sản xuất"),
        ("1.4", "namSanXuat",   "Integer", "Có thể rỗng, 1950–2100",           "Năm sản xuất"),
        ("1.5", "congSuatKw",   "Integer", "Có thể rỗng, > 0",                 "Công suất (kW)"),
        ("1.6", "trangThai",    "String",  "Không rỗng, mặc định: \"Đang hoạt động\"",  "Trạng thái đầu máy"),
        ("1.7", "moTa",         "String",  "Có thể rỗng",                      "Mô tả đầu máy"),
    ]),
    ("DoanTau", [
        ("1.1", "maDoanTau",   "String",  "Khóa chính, không rỗng, duy nhất",        "Mã đoàn tàu"),
        ("1.2", "tenDoanTau",  "String",  "Không rỗng",                              "Tên đoàn tàu"),
        ("1.3", "dauMay",      "DauMay",  "Khóa ngoại → DauMay, không rỗng",         "Đầu máy của đoàn tàu"),
        ("1.4", "trangThai",   "String",  "Không rỗng, mặc định: \"Đang hoạt động\"", "Trạng thái đoàn tàu"),
    ]),
    ("ToaTau", [
        ("1.1", "maToaTau",  "String",   "Khóa chính, không rỗng, duy nhất",         "Mã toa tàu"),
        ("1.2", "loaiGhe",   "LoaiGhe",  "Không rỗng",                               "GHE_CUNG / GHE_MEM / GIUONG_NAM"),
        ("1.3", "trangThai", "String",   "Không rỗng, mặc định: \"Đang hoạt động\"",  "Trạng thái toa tàu"),
    ]),
    ("ChiTietDoanTau", [
        ("1.1", "maChiTietDT", "String",   "Khóa chính, không rỗng, duy nhất", "Mã chi tiết đoàn tàu"),
        ("1.2", "doanTau",     "DoanTau",  "Khóa ngoại → DoanTau, không rỗng", "Đoàn tàu"),
        ("1.3", "toaTau",      "ToaTau",   "Khóa ngoại → ToaTau, không rỗng",  "Toa tàu"),
        ("1.4", "soThuTu",     "int",      "Không rỗng, > 0",                  "Số thứ tự toa trong đoàn"),
    ]),
    ("Lich", [
        ("1.1", "maLich",         "String",        "Khóa chính, không rỗng, duy nhất", "Mã lịch chạy"),
        ("1.2", "tuyen",          "Tuyen",         "Khóa ngoại → Tuyen, không rỗng",   "Tuyến chạy"),
        ("1.3", "doanTau",        "DoanTau",       "Khóa ngoại → DoanTau, không rỗng", "Đoàn tàu thực hiện"),
        ("1.4", "thoiGianBatDau", "LocalDateTime", "Không rỗng",                        "Thời gian khởi hành"),
        ("1.5", "thoiGianChay",   "int",           "Không rỗng",                        "Thời gian chạy (phút)"),
        ("1.6", "hoatDong",       "boolean",       "Không rỗng, mặc định: true",        "Trạng thái hoạt động"),
    ]),
    ("Ghe", [
        ("1.1", "maGhe",   "String",  "Khóa chính, không rỗng, duy nhất", "Mã ghế"),
        ("1.2", "toaTau",  "ToaTau",  "Khóa ngoại → ToaTau, không rỗng",  "Toa tàu chứa ghế"),
        ("1.3", "soGhe",   "int",     "Không rỗng, > 0",                  "Số thứ tự ghế trong toa"),
    ]),
    ("Ve", [
        ("1.1", "maVe",      "String",        "Khóa chính, không rỗng, duy nhất", "Mã vé"),
        ("1.2", "lich",      "Lich",          "Khóa ngoại → Lich, không rỗng",    "Lịch tàu"),
        ("1.3", "ghe",       "Ghe",           "Khóa ngoại → Ghe, không rỗng",     "Ghế trên lịch tàu"),
        ("1.4", "trangThai", "TrangThaiVe",   "Không rỗng",                        "DA_BAN hoặc DA_HUY"),
        ("1.5", "lyDoHuy",   "String",        "Có thể rỗng",                       "Lý do hủy vé"),
        ("1.6", "ngayHuy",   "LocalDateTime", "Có thể rỗng",                       "Ngày giờ hủy vé"),
    ]),
    ("ChiTietVe", [
        ("1.1", "maChiTietVe", "String",      "Khóa chính, không rỗng, duy nhất", "Mã chi tiết vé"),
        ("1.2", "ve",          "Ve",          "Khóa ngoại → Ve, không rỗng",      "Vé gốc"),
        ("1.3", "lich",        "Lich",        "Khóa ngoại → Lich, không rỗng",    "Lịch tàu"),
        ("1.4", "ghe",         "Ghe",         "Khóa ngoại → Ghe, không rỗng",     "Ghế"),
        ("1.5", "trangThai",   "TrangThaiVe", "Không rỗng",                        "DA_BAN hoặc DA_HUY"),
    ]),
    ("GiuCho", [
        ("1.1", "maGiuCho",       "String",        "Khóa chính, không rỗng, duy nhất",  "Mã giữ chỗ"),
        ("1.2", "nhanVien",       "NhanVien",      "Khóa ngoại → NhanVien, không rỗng", "Nhân viên thực hiện giữ chỗ"),
        ("1.3", "lich",           "Lich",          "Khóa ngoại → Lich, không rỗng",     "Lịch tàu"),
        ("1.4", "ghe",            "Ghe",           "Khóa ngoại → Ghe, không rỗng",      "Ghế được giữ"),
        ("1.5", "thoiGianHetHan", "LocalDateTime", "Không rỗng",                         "Thời gian hết hạn giữ chỗ"),
    ]),
    ("NhanVien", [
        ("1.1",  "maNV",             "String",              "Khóa chính, không rỗng, duy nhất", "Mã nhân viên"),
        ("1.2",  "hoTen",            "String",              "Không rỗng",                        "Họ tên nhân viên"),
        ("1.3",  "password",         "String",              "Không rỗng",                        "Mật khẩu đăng nhập"),
        ("1.4",  "vaiTro",           "VaiTro",              "Không rỗng",                        "BAN_VE / DIEU_PHOI / ADMIN"),
        ("1.5",  "soDienThoai",      "String",              "Không rỗng",                        "Số điện thoại"),
        ("1.6",  "cccd",             "String",              "Có thể rỗng",                       "Số căn cước công dân"),
        ("1.7",  "diaChiTamTru",     "String",              "Có thể rỗng",                       "Địa chỉ tạm trú"),
        ("1.8",  "trangThai",        "TrangThaiNhanVien",   "Không rỗng, mặc định: DANG_LAM",    "DANG_LAM / NGHI_PHEP / DA_NGHI"),
        ("1.9",  "email",            "String",              "Có thể rỗng",                       "Địa chỉ email"),
        ("1.10", "gaLamViec",        "Ga",                  "Khóa ngoại → Ga, có thể rỗng",      "Ga làm việc của nhân viên"),
        ("1.11", "diaChiThuongTru",  "String",              "Có thể rỗng",                       "Địa chỉ thường trú"),
        ("1.12", "ngaySinh",         "LocalDate",           "Có thể rỗng",                       "Ngày sinh"),
        ("1.13", "gioiTinh",         "String",              "Có thể rỗng",                       "Giới tính"),
        ("1.14", "quocTich",         "String",              "Có thể rỗng, mặc định: \"Việt Nam\"","Quốc tịch"),
    ]),
    ("KhachHang", [
        ("1.1",  "maKhachHang",      "String",     "Khóa chính, không rỗng, duy nhất",  "Mã khách hàng"),
        ("1.2",  "hoTen",            "String",     "Không rỗng, tối đa 100 ký tự",      "Họ tên khách hàng"),
        ("1.3",  "cccd",             "String",     "Không rỗng",                         "Số căn cước công dân"),
        ("1.4",  "soDienThoai",      "String",     "Không rỗng",                         "Số điện thoại"),
        ("1.5",  "email",            "String",     "Có thể rỗng",                        "Địa chỉ email"),
        ("1.6",  "diaChiThuongTru",  "String",     "Có thể rỗng",                        "Địa chỉ thường trú"),
        ("1.7",  "diaChiTamTru",     "String",     "Có thể rỗng",                        "Địa chỉ tạm trú"),
        ("1.8",  "ngaySinh",         "LocalDate",  "Có thể rỗng",                        "Ngày sinh"),
        ("1.9",  "gioiTinh",         "String",     "Có thể rỗng",                        "Giới tính"),
        ("1.10", "quocTich",         "String",     "Có thể rỗng, mặc định: \"Việt Nam\"", "Quốc tịch"),
    ]),
    ("HoaDon", [
        ("1.1", "maHoaDon", "String",        "Khóa chính, không rỗng, duy nhất",  "Mã hóa đơn"),
        ("1.2", "nhanVien", "NhanVien",      "Khóa ngoại → NhanVien, không rỗng", "Nhân viên lập hóa đơn"),
        ("1.3", "ngayLap",  "LocalDateTime", "Không rỗng",                         "Ngày giờ lập hóa đơn"),
    ]),
    ("HoaDonKhachHang", [
        ("1.1", "maHDKH",    "String",    "Khóa chính, không rỗng, duy nhất",           "Mã hóa đơn khách hàng"),
        ("1.2", "hoaDon",    "HoaDon",    "Khóa ngoại → HoaDon, không rỗng",            "Hóa đơn"),
        ("1.3", "khachHang", "KhachHang", "Khóa ngoại → KhachHang, không rỗng",         "Khách hàng"),
    ]),
    ("ChiTietHoaDon", [
        ("1.1", "maChiTietHD", "String",     "Khóa chính, không rỗng, duy nhất",     "Mã chi tiết hóa đơn"),
        ("1.2", "hoaDon",      "HoaDon",     "Khóa ngoại → HoaDon, không rỗng",      "Hóa đơn"),
        ("1.3", "ve",          "Ve",         "Khóa ngoại → Ve, không rỗng, duy nhất","Vé được thanh toán (1-1)"),
        ("1.4", "chiTietGia",  "ChiTietGia", "Khóa ngoại → ChiTietGia, không rỗng",  "Chi tiết giá áp dụng"),
        ("1.5", "giaTien",     "BigDecimal", "Không rỗng, > 0",                       "Giá tiền thực tế"),
    ]),
    ("Gia", [
        ("1.1", "maGia",           "String",    "Khóa chính, không rỗng, duy nhất",     "Mã bảng giá"),
        ("1.2", "thoiGianBatDau",  "LocalDate", "Không rỗng",                            "Ngày bắt đầu hiệu lực"),
        ("1.3", "thoiGianKetThuc", "LocalDate", "Không rỗng, > thoiGianBatDau",          "Ngày kết thúc hiệu lực"),
        ("1.4", "moTa",            "String",    "Có thể rỗng",                           "Mô tả bảng giá"),
        ("1.5", "trangThai",       "boolean",   "Không rỗng, mặc định: false",           "Trạng thái hiệu lực"),
    ]),
    ("ChiTietGia", [
        ("1.1", "maChiTietGia", "String",   "Khóa chính, không rỗng, duy nhất", "Mã chi tiết giá"),
        ("1.2", "gia",          "Gia",      "Khóa ngoại → Gia, không rỗng",     "Bảng giá"),
        ("1.3", "tuyen",        "Tuyen",    "Khóa ngoại → Tuyen, không rỗng",   "Tuyến áp dụng"),
        ("1.4", "loaiGhe",      "LoaiGhe",  "Không rỗng",                        "GHE_CUNG / GHE_MEM / GIUONG_NAM"),
        ("1.5", "giaNiemYet",   "double",   "Không rỗng, > 0",                  "Giá niêm yết (VNĐ)"),
    ]),
    ("KhuyenMai", [
        ("1.1", "maKhuyenMai",     "String",        "Khóa chính, không rỗng, duy nhất", "Mã khuyến mãi"),
        ("1.2", "tenKhuyenMai",    "String",        "Không rỗng",                        "Tên chương trình khuyến mãi"),
        ("1.3", "thoiGianBatDau",  "LocalDateTime", "Không rỗng",                        "Thời gian bắt đầu"),
        ("1.4", "thoiGianKetThuc", "LocalDateTime", "Không rỗng, > thoiGianBatDau",      "Thời gian kết thúc"),
        ("1.5", "moTa",            "String",        "Có thể rỗng",                       "Mô tả chương trình"),
        ("1.6", "trangThai",       "boolean",       "Không rỗng, mặc định: false",       "Trạng thái hoạt động"),
    ]),
    ("ChiTietKhuyenMai", [
        ("1.1", "maChiTietKM",  "String",    "Khóa chính, không rỗng, duy nhất",                "Mã chi tiết khuyến mãi"),
        ("1.2", "tenChiTiet",   "String",    "Có thể rỗng",                                     "Tên chi tiết khuyến mãi"),
        ("1.3", "khuyenMai",    "KhuyenMai", "Khóa ngoại → KhuyenMai, không rỗng",              "Chương trình khuyến mãi"),
        ("1.4", "tuyen",        "Tuyen",     "Khóa ngoại → Tuyen, có thể rỗng",                 "Tuyến áp dụng (null = tất cả tuyến)"),
        ("1.5", "loaiGhe",      "LoaiGhe",   "Có thể rỗng",                                     "Loại ghế áp dụng (null = tất cả)"),
        ("1.6", "phanTramGiam", "double",    "Không rỗng, > 0 và ≤ 1",                          "Phần trăm giảm giá"),
    ]),
    ("ApDungKM", [
        ("1.1", "maApDung",          "String",            "Khóa chính, không rỗng, duy nhất",             "Mã áp dụng khuyến mãi"),
        ("1.2", "chiTietHoaDon",     "ChiTietHoaDon",     "Khóa ngoại → ChiTietHoaDon, không rỗng",       "Chi tiết hóa đơn"),
        ("1.3", "chiTietKhuyenMai",  "ChiTietKhuyenMai",  "Khóa ngoại → ChiTietKhuyenMai, không rỗng",    "Chi tiết khuyến mãi được áp dụng"),
    ]),
]

def build_312_block():
    parts = []
    for idx, (name, fields) in enumerate(ENTITIES_312, start=1):
        parts.append(make_title_312(idx, name))
        parts.append(make_312_table(fields))
        parts.append(empty_para())
    return "".join(parts)

# ══════════════════════════════════════════════════════════════════════════════
# SECTION 3.2.3 — Các ràng buộc toàn vẹn trong CSDL  (4 cols, widths 8788)
# ══════════════════════════════════════════════════════════════════════════════

W323 = [1509, 2017, 3926, 1336]   # sum = 8788

def _tc323_hdr(w, text):
    return (
        "<w:tc><w:tcPr>"
        f"<w:tcW w:w=\"{w}\" w:type=\"dxa\"/>"
        "<w:tcBorders>"
        "<w:top w:val=\"single\" w:sz=\"6\" w:color=\"000000\"/>"
        "<w:left w:val=\"single\" w:sz=\"6\" w:color=\"000000\"/>"
        "<w:bottom w:val=\"single\" w:sz=\"6\" w:color=\"000000\"/>"
        "<w:right w:val=\"single\" w:sz=\"6\" w:color=\"000000\"/>"
        "</w:tcBorders>"
        "<w:shd w:fill=\"cccccc\" w:val=\"clear\"/>"
        "<w:tcMar>"
        "<w:top w:w=\"80\" w:type=\"dxa\"/><w:left w:w=\"120\" w:type=\"dxa\"/>"
        "<w:bottom w:w=\"80\" w:type=\"dxa\"/><w:right w:w=\"120\" w:type=\"dxa\"/>"
        "</w:tcMar>"
        "<w:vAlign w:val=\"top\"/></w:tcPr>"
        + _p(text, bold=True, center=True, sp_before="0", sp_after="0", line="259")
        + "</w:tc>"
    )

def _tc323_data(w, text):
    return (
        "<w:tc><w:tcPr>"
        f"<w:tcW w:w=\"{w}\" w:type=\"dxa\"/>"
        "<w:tcBorders>"
        "<w:top w:val=\"single\" w:sz=\"4\" w:color=\"000000\"/>"
        "<w:left w:val=\"single\" w:sz=\"4\" w:color=\"000000\"/>"
        "<w:bottom w:val=\"single\" w:sz=\"4\" w:color=\"000000\"/>"
        "<w:right w:val=\"single\" w:sz=\"4\" w:color=\"000000\"/>"
        "</w:tcBorders>"
        "<w:tcMar>"
        "<w:top w:w=\"80\" w:type=\"dxa\"/><w:left w:w=\"120\" w:type=\"dxa\"/>"
        "<w:bottom w:w=\"80\" w:type=\"dxa\"/><w:right w:w=\"120\" w:type=\"dxa\"/>"
        "</w:tcMar>"
        "<w:vAlign w:val=\"top\"/></w:tcPr>"
        + _p(text, sp_before="0", sp_after="0", line="259")
        + "</w:tc>"
    )

def make_323_table(rows):
    """rows: list of (ten_tt, kieu_dl, rang_buoc, nullable)"""
    total = sum(W323)
    grid = "".join(f"<w:gridCol w:w=\"{w}\"/>" for w in W323)
    hdrs = ["Tên thuộc tính", "Kiểu dữ liệu", "Ràng buộc", "Cho phép NULL"]
    hdr_row = (
        "<w:tr><w:trPr><w:cantSplit w:val=\"0\"/>"
        "<w:tblHeader w:val=\"1\"/></w:trPr>"
        + "".join(_tc323_hdr(W323[i], hdrs[i]) for i in range(4))
        + "</w:tr>"
    )
    def drow(a, b, c, d):
        return (
            "<w:tr><w:trPr><w:cantSplit w:val=\"0\"/></w:trPr>"
            + _tc323_data(W323[0], a)
            + _tc323_data(W323[1], b)
            + _tc323_data(W323[2], c)
            + _tc323_data(W323[3], d)
            + "</w:tr>"
        )
    data = "".join(drow(a, b, c, d) for a, b, c, d in rows)
    return (
        "<w:tbl><w:tblPr>"
        f"<w:tblW w:w=\"{total}\" w:type=\"dxa\"/>"
        "<w:jc w:val=\"left\"/>"
        "<w:tblBorders>"
        "<w:top w:val=\"single\" w:sz=\"4\" w:color=\"000000\"/>"
        "<w:left w:val=\"single\" w:sz=\"4\" w:color=\"000000\"/>"
        "<w:bottom w:val=\"single\" w:sz=\"4\" w:color=\"000000\"/>"
        "<w:right w:val=\"single\" w:sz=\"4\" w:color=\"000000\"/>"
        "<w:insideH w:val=\"single\" w:sz=\"4\" w:color=\"000000\"/>"
        "<w:insideV w:val=\"single\" w:sz=\"4\" w:color=\"000000\"/>"
        "</w:tblBorders>"
        "<w:tblLayout w:type=\"fixed\"/>"
        "<w:tblLook w:val=\"0000\"/>"
        "</w:tblPr>"
        f"<w:tblGrid>{grid}</w:tblGrid>"
        + hdr_row + data
        + "</w:tbl>"
    )

def make_title_323(num, name):
    label = f"3.2.3.{num:02d} Bảng {name}"
    return (
        "<w:p><w:pPr>"
        "<w:spacing w:after=\"80\" w:before=\"240\" w:line=\"259\" w:lineRule=\"auto\"/>"
        "<w:ind w:firstLine=\"0\"/>"
        "<w:rPr><w:b w:val=\"1\"/><w:bCs w:val=\"1\"/></w:rPr></w:pPr>"
        "<w:r><w:rPr><w:b w:val=\"1\"/><w:bCs w:val=\"1\"/></w:rPr>"
        f"<w:t>{esc(label)}</w:t></w:r></w:p>"
    )

# ─── SQL-based constraint data: (ten_tt, kieu_dl, rang_buoc, cho_phep_null)
# "Không" = NOT NULL, "Có" = NULL
TABLES_323 = [
    ("NhanVien", [
        ("maNV",            "VARCHAR(20)",    "PRIMARY KEY",                                           "Không"),
        ("hoTen",           "NVARCHAR(100)",  "NOT NULL",                                              "Không"),
        ("password",        "VARCHAR(255)",   "NOT NULL",                                              "Không"),
        ("vaiTro",          "VARCHAR(20)",    "NOT NULL, CHECK IN ('BAN_VE', 'DIEU_PHOI', 'ADMIN')",   "Không"),
        ("soDienThoai",     "VARCHAR(15)",    "NOT NULL",                                              "Không"),
        ("cccd",            "VARCHAR(20)",    "-",                                                     "Có"),
        ("diaChiTamTru",    "NVARCHAR(255)",  "-",                                                     "Có"),
        ("trangThai",       "VARCHAR(20)",    "NOT NULL, DEFAULT 'DANG_LAM', CHECK IN ('DANG_LAM', 'NGHI_PHEP', 'DA_NGHI')", "Không"),
        ("email",           "VARCHAR(100)",   "-",                                                     "Có"),
        ("gaLamViec",       "VARCHAR(20)",    "FK → Ga(maGa)",                                         "Có"),
        ("diaChiThuongTru", "NVARCHAR(255)",  "-",                                                     "Có"),
        ("ngaySinh",        "DATE",           "-",                                                     "Có"),
        ("gioiTinh",        "VARCHAR(5)",     "CHECK IN ('NAM', 'NU')",                                "Có"),
        ("quocTich",        "NVARCHAR(50)",   "DEFAULT N'Việt Nam'",                                   "Có"),
    ]),
    ("KhachHang", [
        ("maKhachHang",     "VARCHAR(20)",    "PRIMARY KEY",                                           "Không"),
        ("hoTen",           "NVARCHAR(100)",  "NOT NULL",                                              "Không"),
        ("cccd",            "VARCHAR(20)",    "NOT NULL",                                              "Không"),
        ("soDienThoai",     "VARCHAR(15)",    "NOT NULL",                                              "Không"),
        ("email",           "VARCHAR(100)",   "-",                                                     "Có"),
        ("diaChiThuongTru", "NVARCHAR(255)",  "-",                                                     "Có"),
        ("diaChiTamTru",    "NVARCHAR(255)",  "-",                                                     "Có"),
        ("ngaySinh",        "DATE",           "-",                                                     "Có"),
        ("gioiTinh",        "VARCHAR(5)",     "CHECK IN ('NAM', 'NU')",                                "Có"),
        ("quocTich",        "NVARCHAR(50)",   "DEFAULT N'Việt Nam'",                                   "Có"),
    ]),
    ("Ga", [
        ("maGa",   "VARCHAR(20)",    "PRIMARY KEY",  "Không"),
        ("tenGa",  "NVARCHAR(100)",  "NOT NULL",     "Không"),
        ("diaChi", "NVARCHAR(255)",  "NOT NULL",     "Không"),
    ]),
    ("DauMay", [
        ("maDauMay",    "VARCHAR(20)",    "PRIMARY KEY",                                              "Không"),
        ("tenDauMay",   "NVARCHAR(100)",  "NOT NULL",                                                 "Không"),
        ("hangSanXuat", "NVARCHAR(100)",  "-",                                                        "Có"),
        ("namSanXuat",  "INT",            "CHECK (BETWEEN 1950 AND 2100)",                            "Có"),
        ("congSuatKw",  "INT",            "CHECK (> 0)",                                              "Có"),
        ("trangThai",   "NVARCHAR(30)",   "NOT NULL, DEFAULT N'Đang hoạt động', CHECK IN ('Đang hoạt động', 'Bảo trì', 'Ngừng khai thác')", "Không"),
        ("moTa",        "NVARCHAR(255)",  "-",                                                        "Có"),
    ]),
    ("ToaTau", [
        ("maToaTau",  "VARCHAR(20)",   "PRIMARY KEY",                                              "Không"),
        ("loaiGhe",   "VARCHAR(20)",   "NOT NULL, CHECK IN ('GHE_CUNG', 'GHE_MEM', 'GIUONG_NAM')", "Không"),
        ("trangThai", "NVARCHAR(30)",  "NOT NULL, DEFAULT N'Đang hoạt động', CHECK IN ('Đang hoạt động', 'Bảo trì', 'Ngừng khai thác')", "Không"),
    ]),
    ("Gia", [
        ("maGia",           "VARCHAR(20)",   "PRIMARY KEY",                             "Không"),
        ("thoiGianBatDau",  "DATE",          "NOT NULL",                                "Không"),
        ("thoiGianKetThuc", "DATE",          "NOT NULL, CHECK (> thoiGianBatDau)",      "Không"),
        ("moTa",            "NVARCHAR(255)", "-",                                        "Có"),
        ("trangThai",       "BIT",           "NOT NULL, DEFAULT 0",                     "Không"),
    ]),
    ("KhuyenMai", [
        ("maKhuyenMai",     "VARCHAR(20)",    "PRIMARY KEY",                             "Không"),
        ("tenKhuyenMai",    "NVARCHAR(100)",  "NOT NULL",                                "Không"),
        ("thoiGianBatDau",  "DATETIME",       "NOT NULL",                                "Không"),
        ("thoiGianKetThuc", "DATETIME",       "NOT NULL, CHECK (> thoiGianBatDau)",      "Không"),
        ("moTa",            "NVARCHAR(255)",  "-",                                        "Có"),
        ("trangThai",       "BIT",            "NOT NULL, DEFAULT 0",                     "Không"),
    ]),
    ("Tuyen", [
        ("maTuyen",  "VARCHAR(20)", "PRIMARY KEY",                              "Không"),
        ("gaDi",     "VARCHAR(20)", "NOT NULL, FK → Ga(maGa), CHECK (gaDi ≠ gaDen)", "Không"),
        ("gaDen",    "VARCHAR(20)", "NOT NULL, FK → Ga(maGa), CHECK (gaDi ≠ gaDen)", "Không"),
        ("km",       "INT",         "NOT NULL, DEFAULT 0",                      "Không"),
        ("hoatDong", "BIT",         "NOT NULL, DEFAULT 1",                      "Không"),
    ]),
    ("DoanTau", [
        ("maDoanTau",  "VARCHAR(20)",   "PRIMARY KEY",                                              "Không"),
        ("tenDoanTau", "NVARCHAR(100)", "NOT NULL",                                                  "Không"),
        ("maDauMay",   "VARCHAR(20)",   "NOT NULL, FK → DauMay(maDauMay)",                           "Không"),
        ("trangThai",  "NVARCHAR(30)",  "NOT NULL, DEFAULT N'Đang hoạt động', CHECK IN ('Đang hoạt động', 'Ngừng khai thác')", "Không"),
    ]),
    ("ChiTietDoanTau", [
        ("maChiTietDT", "VARCHAR(20)", "PRIMARY KEY",                       "Không"),
        ("maDoanTau",   "VARCHAR(20)", "NOT NULL, FK → DoanTau(maDoanTau)", "Không"),
        ("maToaTau",    "VARCHAR(20)", "NOT NULL, FK → ToaTau(maToaTau)",   "Không"),
        ("soThuTu",     "INT",         "NOT NULL, CHECK (> 0)",             "Không"),
    ]),
    ("Ghe", [
        ("maGhe",    "VARCHAR(20)", "PRIMARY KEY",                     "Không"),
        ("maToaTau", "VARCHAR(20)", "NOT NULL, FK → ToaTau(maToaTau)", "Không"),
        ("soGhe",    "INT",         "NOT NULL, CHECK (> 0)",           "Không"),
    ]),
    ("Lich", [
        ("maLich",         "VARCHAR(20)", "PRIMARY KEY",                       "Không"),
        ("maTuyen",        "VARCHAR(20)", "NOT NULL, FK → Tuyen(maTuyen)",     "Không"),
        ("maDoanTau",      "VARCHAR(20)", "NOT NULL, FK → DoanTau(maDoanTau)", "Không"),
        ("thoiGianBatDau", "DATETIME",    "NOT NULL",                           "Không"),
        ("thoiGianChay",   "INT",         "NOT NULL (đơn vị: phút)",            "Không"),
        ("hoatDong",       "BIT",         "NOT NULL, DEFAULT 1",               "Không"),
    ]),
    ("ChiTietGia", [
        ("maChiTietGia", "VARCHAR(20)",    "PRIMARY KEY",                              "Không"),
        ("maGia",        "VARCHAR(20)",    "NOT NULL, FK → Gia(maGia)",               "Không"),
        ("maTuyen",      "VARCHAR(20)",    "NOT NULL, FK → Tuyen(maTuyen)",            "Không"),
        ("loaiGhe",      "VARCHAR(20)",    "NOT NULL, CHECK IN ('GHE_CUNG', 'GHE_MEM', 'GIUONG_NAM')", "Không"),
        ("giaNiemYet",   "DECIMAL(18,2)",  "NOT NULL, CHECK (> 0)",                   "Không"),
    ]),
    ("ChiTietKhuyenMai", [
        ("maChiTietKM",  "VARCHAR(20)",   "PRIMARY KEY",                                           "Không"),
        ("maKhuyenMai",  "VARCHAR(20)",   "NOT NULL, FK → KhuyenMai(maKhuyenMai)",                 "Không"),
        ("maTuyen",      "VARCHAR(20)",   "FK → Tuyen(maTuyen) (NULL = tất cả tuyến)",             "Có"),
        ("loaiGhe",      "VARCHAR(20)",   "CHECK (IS NULL OR IN ('GHE_CUNG', 'GHE_MEM', 'GIUONG_NAM'))", "Có"),
        ("tenChiTiet",   "NVARCHAR(200)", "-",                                                      "Có"),
        ("phanTramGiam", "DECIMAL(5,2)",  "NOT NULL, CHECK (> 0 AND <= 1)",                         "Không"),
    ]),
    ("Ve", [
        ("maVe",      "VARCHAR(20)",    "PRIMARY KEY",                              "Không"),
        ("maLich",    "VARCHAR(20)",    "NOT NULL, FK → Lich(maLich)",              "Không"),
        ("maGhe",     "VARCHAR(20)",    "NOT NULL, FK → Ghe(maGhe)",               "Không"),
        ("trangThai", "VARCHAR(10)",    "NOT NULL, CHECK IN ('DA_BAN', 'DA_HUY')", "Không"),
        ("lyDoHuy",   "NVARCHAR(255)",  "-",                                        "Có"),
        ("ngayHuy",   "DATETIME",       "-",                                        "Có"),
    ]),
    ("HoaDon", [
        ("maHoaDon", "VARCHAR(30)", "PRIMARY KEY",                    "Không"),
        ("maNV",     "VARCHAR(20)", "NOT NULL, FK → NhanVien(maNV)",  "Không"),
        ("ngayLap",  "DATETIME",    "NOT NULL",                        "Không"),
    ]),
    ("HoaDonKhachHang", [
        ("maHDKH",      "VARCHAR(20)", "PRIMARY KEY",                                          "Không"),
        ("maHoaDon",    "VARCHAR(30)", "NOT NULL, FK → HoaDon(maHoaDon)",                     "Không"),
        ("maKhachHang", "VARCHAR(20)", "NOT NULL, FK → KhachHang(maKhachHang), UNIQUE (maHoaDon, maKhachHang)", "Không"),
    ]),
    ("ChiTietHoaDon", [
        ("maChiTietHD",  "VARCHAR(20)",   "PRIMARY KEY",                                "Không"),
        ("maHoaDon",     "VARCHAR(30)",   "NOT NULL, FK → HoaDon(maHoaDon)",            "Không"),
        ("maVe",         "VARCHAR(20)",   "NOT NULL, UNIQUE, FK → Ve(maVe)",            "Không"),
        ("maChiTietGia", "VARCHAR(20)",   "NOT NULL, FK → ChiTietGia(maChiTietGia)",    "Không"),
        ("giaTien",      "DECIMAL(18,2)", "NOT NULL, CHECK (> 0)",                      "Không"),
    ]),
    ("ApDungKM", [
        ("maApDung",    "VARCHAR(20)", "PRIMARY KEY",                                             "Không"),
        ("maChiTietHD", "VARCHAR(20)", "NOT NULL, FK → ChiTietHoaDon(maChiTietHD)",               "Không"),
        ("maChiTietKM", "VARCHAR(20)", "NOT NULL, FK → ChiTietKhuyenMai(maChiTietKM)",            "Không"),
    ]),
    ("GiuCho", [
        ("maGiuCho",       "VARCHAR(20)", "PRIMARY KEY",                      "Không"),
        ("maNV",           "VARCHAR(20)", "NOT NULL, FK → NhanVien(maNV)",    "Không"),
        ("maLich",         "VARCHAR(20)", "NOT NULL, FK → Lich(maLich)",      "Không"),
        ("maGhe",          "VARCHAR(20)", "NOT NULL, FK → Ghe(maGhe)",        "Không"),
        ("thoiGianHetHan", "DATETIME",    "NOT NULL",                          "Không"),
    ]),
]

def build_323_block():
    parts = []
    for idx, (name, rows) in enumerate(TABLES_323, start=1):
        parts.append(make_title_323(idx, name))
        parts.append(make_323_table(rows))
        parts.append(empty_para())
    return "".join(parts)

# ══════════════════════════════════════════════════════════════════════════════
# Main: read v9, apply both fixes, write v10
# ══════════════════════════════════════════════════════════════════════════════

def main():
    with zipfile.ZipFile(SRC, "r") as z:
        xml = z.read("word/document.xml").decode("utf-8")

    # ── Locate 3.1.2 replacement bounds ──────────────────────────────────────
    # Start: just after the empty <w:p> that follows the "Đặc tả lớp" Heading3
    # End: start of the "Cơ sở dữ liệu" Heading2 paragraph
    dtl_idx = xml.find("Đặc tả lớp", 290000)
    assert dtl_idx > 0, "Cannot find 'Đặc tả lớp'"
    dtl_p_end = xml.find("</w:p>", dtl_idx) + len("</w:p>")
    empty_p_end = xml.find("</w:p>", dtl_p_end) + len("</w:p>")

    csld_idx = xml.find("Cơ sở dữ liệu", 500000)
    csld_p_start = xml.rfind("<w:p ", 0, csld_idx)

    block_312 = build_312_block()
    xml = xml[:empty_p_end] + block_312 + xml[csld_p_start:]

    # ── Locate 3.2.3 insertion point ─────────────────────────────────────────
    # After the empty <w:p> that follows the "Các ràng buộc toàn vẹn" Heading3
    rbc_idx = xml.find("ràng buộc toàn vẹn", 800000)
    assert rbc_idx > 0, "Cannot find 'ràng buộc toàn vẹn' heading in body"
    rbc_p_end = xml.find("</w:p>", rbc_idx) + len("</w:p>")
    empty_p2_end = xml.find("</w:p>", rbc_p_end) + len("</w:p>")
    # Find next top-level element after empty para (for insertion anchor)
    next_element = xml[empty_p2_end:empty_p2_end+40]

    block_323 = build_323_block()
    xml = xml[:empty_p2_end] + block_323 + xml[empty_p2_end:]

    # ── Pack to v10 ──────────────────────────────────────────────────────────
    tmp = DST + ".tmp"
    with zipfile.ZipFile(SRC, "r") as zin, \
         zipfile.ZipFile(tmp, "w", zipfile.ZIP_DEFLATED) as zout:
        for item in zin.namelist():
            data = xml.encode("utf-8") if item == "word/document.xml" else zin.read(item)
            zout.writestr(item, data)
    os.replace(tmp, DST)

    sz = os.path.getsize(DST)
    print(f"Done → {DST}  ({sz:,} bytes)")

    # ── Sanity checks ─────────────────────────────────────────────────────────
    with zipfile.ZipFile(DST, "r") as z:
        out = z.read("word/document.xml").decode("utf-8")
    assert "3.1.2.01. Thực thể Ga:" in out,        "Missing 3.1.2.01"
    assert "3.1.2.21. Thực thể ApDungKM:" in out,  "Missing 3.1.2.21"
    assert "gaLamViec" in out and ">Ga<" in out,   "gaLamViec fix missing"
    assert "3.2.3.01 Bảng NhanVien" in out,        "Missing 3.2.3.01"
    assert "3.2.3.20 Bảng GiuCho" in out,          "Missing 3.2.3.20"
    # Verify widths
    import re
    w312_found = re.findall(r'tblW w:w="(\d+)"', out)
    w8788 = [w for w in w312_found if w == "8788"]
    print(f"Tables at 8788 dxa: {len(w8788)}")
    print(f"Namespace declarations: {out.count('xmlns:')} (expect ≥ 30)")
    print("All checks passed.")

if __name__ == "__main__":
    main()
