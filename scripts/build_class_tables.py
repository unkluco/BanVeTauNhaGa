"""
build_class_tables.py
Tạo bảng "3.1.2 Đặc tả lớp" cho 21 thực thể và chèn vào v8.docx → v9.docx
Format tham khảo: Table9, 5 cột (975|2955|1890|2445|1665), header fill=cccccc,
viền ô sz=5 single, tiêu đề lớp in đậm trước mỗi bảng.
"""
import zipfile, shutil, os, re

SRC  = "document/9_REPORT_v8.docx"
DST  = "document/9_REPORT_v9.docx"
WIDTHS = [975, 2955, 1890, 2445, 1665]   # STT | Công việc | Kiểu dữ liệu | Ràng buộc | Ghi chú

# ──────────────────────────────────────────────────────────────────────────────
# XML helpers
# ──────────────────────────────────────────────────────────────────────────────

def esc(s):
    """XML-escape a plain string."""
    return (str(s)
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace('"', "&quot;"))

def _p(text, bold=False, center=False, shd="ffffff",
       sp_before="240", sp_after="0", line="360"):
    """Simple paragraph with one text run (no rFonts, no sz overrides)."""
    jc = "center" if center else "left"
    b_open  = "<w:b w:val=\"1\"/><w:bCs w:val=\"1\"/>" if bold else ""
    return (
        "<w:p>"
        "<w:pPr>"
        f"<w:shd w:fill=\"{shd}\" w:val=\"clear\"/>"
        f"<w:spacing w:before=\"{sp_before}\" w:line=\"{line}\" w:lineRule=\"auto\"/>"
        "<w:ind w:firstLine=\"0\"/>"
        f"<w:jc w:val=\"{jc}\"/>"
        f"<w:rPr>{b_open}</w:rPr>"
        "</w:pPr>"
        "<w:r>"
        f"<w:rPr>{b_open}<w:rtl w:val=\"0\"/></w:rPr>"
        f"<w:t xml:space=\"preserve\">{esc(text)}</w:t>"
        "</w:r>"
        "</w:p>"
    )

def _tc_hdr(w, text):
    """Header cell: fill cccccc, all borders sz=4 single, text bold centered."""
    return (
        "<w:tc>"
        "<w:tcPr>"
        f"<w:tcW w:w=\"{w}\" w:type=\"dxa\"/>"
        "<w:tcBorders>"
        "<w:top w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"000000\"/>"
        "<w:left w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"000000\"/>"
        "<w:bottom w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"000000\"/>"
        "<w:right w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"000000\"/>"
        "</w:tcBorders>"
        "<w:shd w:fill=\"cccccc\" w:val=\"clear\"/>"
        "<w:tcMar>"
        "<w:top w:w=\"0\" w:type=\"dxa\"/>"
        "<w:left w:w=\"100\" w:type=\"dxa\"/>"
        "<w:bottom w:w=\"0\" w:type=\"dxa\"/>"
        "<w:right w:w=\"100\" w:type=\"dxa\"/>"
        "</w:tcMar>"
        "<w:vAlign w:val=\"top\"/>"
        "</w:tcPr>"
        + _p(text, bold=True, center=False, shd="cccccc") +
        "</w:tc>"
    )

def _tc_data(w, text, is_first_col=False, bold=False):
    """Data cell: top=nil, left=sz5 if first col else nil, bottom=sz5, right=sz5."""
    left_border = (
        "<w:left w:val=\"single\" w:sz=\"5\" w:space=\"0\" w:color=\"000000\"/>"
        if is_first_col else
        "<w:left w:val=\"nil\" w:sz=\"0\" w:space=\"0\" w:color=\"000000\"/>"
    )
    return (
        "<w:tc>"
        "<w:tcPr>"
        f"<w:tcW w:w=\"{w}\" w:type=\"dxa\"/>"
        "<w:tcBorders>"
        "<w:top w:val=\"nil\" w:sz=\"0\" w:space=\"0\" w:color=\"000000\"/>"
        + left_border +
        "<w:bottom w:val=\"single\" w:sz=\"5\" w:space=\"0\" w:color=\"000000\"/>"
        "<w:right w:val=\"single\" w:sz=\"5\" w:space=\"0\" w:color=\"000000\"/>"
        "</w:tcBorders>"
        "<w:tcMar>"
        "<w:top w:w=\"0\" w:type=\"dxa\"/>"
        "<w:left w:w=\"100\" w:type=\"dxa\"/>"
        "<w:bottom w:w=\"0\" w:type=\"dxa\"/>"
        "<w:right w:w=\"100\" w:type=\"dxa\"/>"
        "</w:tcMar>"
        "<w:vAlign w:val=\"top\"/>"
        "</w:tcPr>"
        + _p(text, bold=bold) +
        "</w:tc>"
    )

def make_header_row():
    headers = ["STT", "Công việc", "Kiểu dữ liệu", "Ràng buộc", "Ghi chú"]
    tcs = "".join(_tc_hdr(WIDTHS[i], headers[i]) for i in range(5))
    return (
        "<w:tr>"
        "<w:trPr>"
        "<w:cantSplit w:val=\"0\"/>"
        "<w:trHeight w:val=\"405\" w:hRule=\"atLeast\"/>"
        "<w:tblHeader w:val=\"1\"/>"
        "</w:trPr>"
        + tcs +
        "</w:tr>"
    )

def make_subsection_row(num, label):
    """Row with bold STT number and bold label in col2, empty cols 3-5."""
    tr = (
        "<w:tr>"
        "<w:trPr>"
        "<w:cantSplit w:val=\"0\"/>"
        "<w:trHeight w:val=\"405\" w:hRule=\"atLeast\"/>"
        "<w:tblHeader w:val=\"0\"/>"
        "</w:trPr>"
        + _tc_data(WIDTHS[0], str(num), is_first_col=True, bold=True)
        + _tc_data(WIDTHS[1], label, is_first_col=False, bold=True)
        + _tc_data(WIDTHS[2], "", is_first_col=False)
        + _tc_data(WIDTHS[3], "", is_first_col=False)
        + _tc_data(WIDTHS[4], "", is_first_col=False)
        + "</w:tr>"
    )
    return tr

def make_data_row(stt, ten, kieu, rang_buoc, ghi_chu=""):
    return (
        "<w:tr>"
        "<w:trPr>"
        "<w:cantSplit w:val=\"0\"/>"
        "<w:trHeight w:val=\"405\" w:hRule=\"atLeast\"/>"
        "<w:tblHeader w:val=\"0\"/>"
        "</w:trPr>"
        + _tc_data(WIDTHS[0], stt,       is_first_col=True)
        + _tc_data(WIDTHS[1], ten,       is_first_col=False)
        + _tc_data(WIDTHS[2], kieu,      is_first_col=False)
        + _tc_data(WIDTHS[3], rang_buoc, is_first_col=False)
        + _tc_data(WIDTHS[4], ghi_chu,   is_first_col=False)
        + "</w:tr>"
    )

def make_table(rows_xml):
    total_w = sum(WIDTHS)
    grid = "".join(f"<w:gridCol w:w=\"{w}\"/>" for w in WIDTHS)
    return (
        "<w:tbl>"
        "<w:tblPr>"
        "<w:tblStyle w:val=\"Table9\"/>"
        f"<w:tblW w:w=\"{total_w}\" w:type=\"dxa\"/>"
        "<w:jc w:val=\"left\"/>"
        "<w:tblBorders>"
        "<w:top w:val=\"nil\" w:sz=\"0\" w:space=\"0\" w:color=\"000000\"/>"
        "<w:left w:val=\"nil\" w:sz=\"0\" w:space=\"0\" w:color=\"000000\"/>"
        "<w:bottom w:val=\"nil\" w:sz=\"0\" w:space=\"0\" w:color=\"000000\"/>"
        "<w:right w:val=\"nil\" w:sz=\"0\" w:space=\"0\" w:color=\"000000\"/>"
        "<w:insideH w:val=\"nil\" w:sz=\"0\" w:space=\"0\" w:color=\"000000\"/>"
        "<w:insideV w:val=\"nil\" w:sz=\"0\" w:space=\"0\" w:color=\"000000\"/>"
        "</w:tblBorders>"
        "<w:tblLayout w:type=\"fixed\"/>"
        "<w:tblLook w:val=\"0600\"/>"
        "</w:tblPr>"
        f"<w:tblGrid>{grid}</w:tblGrid>"
        + rows_xml +
        "</w:tbl>"
    )

def make_title_para(num, name):
    """Bold paragraph: '3.1.2.XX. Thực thể Name:'"""
    label = f"3.1.2.{num:02d}. Thực thể {name}:"
    return (
        "<w:p>"
        "<w:pPr>"
        "<w:spacing w:after=\"80\" w:before=\"360\" w:line=\"259\" w:lineRule=\"auto\"/>"
        "<w:ind w:firstLine=\"0\"/>"
        "<w:jc w:val=\"left\"/>"
        "<w:rPr><w:b w:val=\"1\"/><w:bCs w:val=\"1\"/></w:rPr>"
        "</w:pPr>"
        "<w:r>"
        "<w:rPr><w:b w:val=\"1\"/><w:bCs w:val=\"1\"/></w:rPr>"
        f"<w:t>{esc(label)}</w:t>"
        "</w:r>"
        "</w:p>"
    )

def empty_para():
    return "<w:p><w:pPr><w:spacing w:after=\"120\"/></w:pPr></w:p>"

# ──────────────────────────────────────────────────────────────────────────────
# Entity data  (stt_prefix, field, type, constraint, note)
# ──────────────────────────────────────────────────────────────────────────────

ENTITIES = [
    ("Ga", [
        ("1.1", "maGa",   "String",  "Khóa chính, không rỗng, duy nhất", "Mã ga"),
        ("1.2", "tenGa",  "String",  "Không rỗng",                       "Tên ga"),
        ("1.3", "diaChi", "String",  "Có thể rỗng",                      "Địa chỉ ga"),
    ]),
    ("Tuyen", [
        ("1.1", "maTuyen",   "String",  "Khóa chính, không rỗng, duy nhất",     "Mã tuyến"),
        ("1.2", "gaDi",      "Ga",      "Khóa ngoại → Ga, không rỗng",          "Ga đi"),
        ("1.3", "gaDen",     "Ga",      "Khóa ngoại → Ga, không rỗng",          "Ga đến"),
        ("1.4", "km",        "int",     "Số nguyên dương, không rỗng",          "Khoảng cách (km)"),
        ("1.5", "hoatDong",  "boolean", "Không rỗng, mặc định: true",           "Trạng thái hoạt động"),
    ]),
    ("DauMay", [
        ("1.1", "maDauMay",     "String",  "Khóa chính, không rỗng, duy nhất", "Mã đầu máy"),
        ("1.2", "tenDauMay",    "String",  "Không rỗng",                       "Tên đầu máy"),
        ("1.3", "hangSanXuat",  "String",  "Có thể rỗng",                      "Hãng sản xuất"),
        ("1.4", "namSanXuat",   "Integer", "Có thể rỗng",                      "Năm sản xuất"),
        ("1.5", "congSuatKw",   "Integer", "Có thể rỗng",                      "Công suất (kW)"),
        ("1.6", "trangThai",    "String",  "Mặc định: \"Đang hoạt động\"",      "Trạng thái đầu máy"),
        ("1.7", "moTa",         "String",  "Có thể rỗng",                      "Mô tả đầu máy"),
    ]),
    ("DoanTau", [
        ("1.1", "maDoanTau",   "String",  "Khóa chính, không rỗng, duy nhất",  "Mã đoàn tàu"),
        ("1.2", "tenDoanTau",  "String",  "Không rỗng",                        "Tên đoàn tàu"),
        ("1.3", "dauMay",      "DauMay",  "Khóa ngoại → DauMay, không rỗng",   "Đầu máy của đoàn tàu"),
        ("1.4", "trangThai",   "String",  "Không rỗng",                        "Trạng thái đoàn tàu"),
    ]),
    ("ToaTau", [
        ("1.1", "maToaTau",  "String",   "Khóa chính, không rỗng, duy nhất",         "Mã toa tàu"),
        ("1.2", "loaiGhe",   "LoaiGhe",  "Không rỗng",                               "GHE_CUNG / GHE_MEM / GIUONG_NAM"),
        ("1.3", "trangThai", "String",   "Không rỗng",                               "Trạng thái toa tàu"),
    ]),
    ("ChiTietDoanTau", [
        ("1.1", "maChiTietDT", "String",   "Khóa chính, không rỗng, duy nhất",      "Mã chi tiết đoàn tàu"),
        ("1.2", "doanTau",     "DoanTau",  "Khóa ngoại → DoanTau, không rỗng",      "Đoàn tàu"),
        ("1.3", "toaTau",      "ToaTau",   "Khóa ngoại → ToaTau, không rỗng",       "Toa tàu"),
        ("1.4", "soThuTu",     "int",      "Số nguyên dương, không rỗng",           "Số thứ tự toa trong đoàn"),
    ]),
    ("Lich", [
        ("1.1", "maLich",          "String",        "Khóa chính, không rỗng, duy nhất",    "Mã lịch chạy"),
        ("1.2", "tuyen",           "Tuyen",         "Khóa ngoại → Tuyen, không rỗng",      "Tuyến chạy"),
        ("1.3", "doanTau",         "DoanTau",       "Khóa ngoại → DoanTau, không rỗng",    "Đoàn tàu thực hiện"),
        ("1.4", "thoiGianBatDau",  "LocalDateTime", "Không rỗng",                          "Thời gian khởi hành"),
        ("1.5", "thoiGianChay",    "String",        "Có thể rỗng",                         "Thời gian di chuyển ước tính"),
        ("1.6", "hoatDong",        "boolean",       "Không rỗng",                          "Trạng thái hoạt động của lịch"),
    ]),
    ("Ghe", [
        ("1.1", "maGhe",   "String",  "Khóa chính, không rỗng, duy nhất",     "Mã ghế"),
        ("1.2", "toaTau",  "ToaTau",  "Khóa ngoại → ToaTau, không rỗng",      "Toa tàu chứa ghế"),
        ("1.3", "soGhe",   "int",     "Số nguyên dương, không rỗng",          "Số thứ tự ghế trong toa"),
    ]),
    ("Ve", [
        ("1.1", "maVe",      "String",        "Khóa chính, không rỗng, duy nhất",   "Mã vé"),
        ("1.2", "lich",      "Lich",          "Khóa ngoại → Lich, không rỗng",      "Lịch tàu"),
        ("1.3", "ghe",       "Ghe",           "Khóa ngoại → Ghe, không rỗng",       "Ghế trên lịch tàu"),
        ("1.4", "trangThai", "TrangThaiVe",   "Không rỗng",                         "DA_BAN hoặc DA_HUY"),
        ("1.5", "lyDoHuy",   "String",        "Có thể rỗng",                        "Lý do hủy vé"),
        ("1.6", "ngayHuy",   "LocalDateTime", "Có thể rỗng",                        "Ngày giờ hủy vé"),
    ]),
    ("ChiTietVe", [
        ("1.1", "maChiTietVe", "String",      "Khóa chính, không rỗng, duy nhất",   "Mã chi tiết vé"),
        ("1.2", "ve",          "Ve",          "Khóa ngoại → Ve, không rỗng",        "Vé gốc"),
        ("1.3", "lich",        "Lich",        "Khóa ngoại → Lich, không rỗng",      "Lịch tàu"),
        ("1.4", "ghe",         "Ghe",         "Khóa ngoại → Ghe, không rỗng",       "Ghế"),
        ("1.5", "trangThai",   "TrangThaiVe", "Không rỗng",                         "DA_BAN hoặc DA_HUY"),
    ]),
    ("GiuCho", [
        ("1.1", "maGiuCho",        "String",        "Khóa chính, không rỗng, duy nhất",    "Mã giữ chỗ"),
        ("1.2", "nhanVien",        "NhanVien",      "Khóa ngoại → NhanVien, không rỗng",   "Nhân viên thực hiện giữ chỗ"),
        ("1.3", "lich",            "Lich",          "Khóa ngoại → Lich, không rỗng",       "Lịch tàu"),
        ("1.4", "ghe",             "Ghe",           "Khóa ngoại → Ghe, không rỗng",        "Ghế được giữ"),
        ("1.5", "thoiGianHetHan",  "LocalDateTime", "Không rỗng",                          "Thời gian hết hạn giữ chỗ"),
    ]),
    ("NhanVien", [
        ("1.1",  "maNV",             "String",                "Khóa chính, không rỗng, duy nhất",   "Mã nhân viên"),
        ("1.2",  "hoTen",            "String",                "Không rỗng",                          "Họ tên nhân viên"),
        ("1.3",  "password",         "String",                "Không rỗng",                          "Mật khẩu đăng nhập"),
        ("1.4",  "vaiTro",           "VaiTro",                "Không rỗng",                          "BAN_VE / DIEU_PHOI / ADMIN"),
        ("1.5",  "soDienThoai",      "String",                "Có thể rỗng",                         "Số điện thoại"),
        ("1.6",  "cccd",             "String",                "Không rỗng",                          "Số căn cước công dân"),
        ("1.7",  "diaChiTamTru",     "String",                "Có thể rỗng",                         "Địa chỉ tạm trú"),
        ("1.8",  "trangThai",        "TrangThaiNhanVien",     "Mặc định: DANG_LAM",                  "DANG_LAM / NGHI_PHEP / DA_NGHI"),
        ("1.9",  "email",            "String",                "Có thể rỗng",                         "Địa chỉ email"),
        ("1.10", "gaLamViec",        "String",                "Khóa ngoại → Ga, không rỗng",         "Mã ga làm việc của nhân viên"),
        ("1.11", "diaChiThuongTru",  "String",                "Có thể rỗng",                         "Địa chỉ thường trú"),
        ("1.12", "ngaySinh",         "LocalDate",             "Có thể rỗng",                         "Ngày sinh"),
        ("1.13", "gioiTinh",         "String",                "Có thể rỗng",                         "Giới tính"),
        ("1.14", "quocTich",         "String",                "Có thể rỗng",                         "Quốc tịch"),
    ]),
    ("KhachHang", [
        ("1.1",  "maKhachHang",      "String",     "Khóa chính, không rỗng, duy nhất",       "Mã khách hàng"),
        ("1.2",  "hoTen",            "String",     "Không rỗng, tối đa 100 ký tự",           "Họ tên khách hàng"),
        ("1.3",  "cccd",             "String",     "Không rỗng",                             "Số căn cước công dân"),
        ("1.4",  "soDienThoai",      "String",     "Không rỗng",                             "Số điện thoại"),
        ("1.5",  "email",            "String",     "Có thể rỗng",                            "Địa chỉ email"),
        ("1.6",  "diaChiThuongTru",  "String",     "Có thể rỗng",                            "Địa chỉ thường trú"),
        ("1.7",  "diaChiTamTru",     "String",     "Có thể rỗng",                            "Địa chỉ tạm trú"),
        ("1.8",  "ngaySinh",         "LocalDate",  "Có thể rỗng",                            "Ngày sinh"),
        ("1.9",  "gioiTinh",         "String",     "Có thể rỗng",                            "Giới tính"),
        ("1.10", "quocTich",         "String",     "Mặc định: \"Việt Nam\"",                  "Quốc tịch"),
    ]),
    ("HoaDon", [
        ("1.1", "maHoaDon",  "String",        "Khóa chính, không rỗng, duy nhất",    "Mã hóa đơn"),
        ("1.2", "nhanVien",  "NhanVien",      "Khóa ngoại → NhanVien, không rỗng",   "Nhân viên lập hóa đơn"),
        ("1.3", "ngayLap",   "LocalDateTime", "Không rỗng",                          "Ngày giờ lập hóa đơn"),
    ]),
    ("HoaDonKhachHang", [
        ("1.1", "maHDKH",      "String",      "Khóa chính, không rỗng, duy nhất",      "Mã hóa đơn khách hàng"),
        ("1.2", "hoaDon",      "HoaDon",      "Khóa ngoại → HoaDon, không rỗng",       "Hóa đơn"),
        ("1.3", "khachHang",   "KhachHang",   "Khóa ngoại → KhachHang, không rỗng",    "Khách hàng"),
    ]),
    ("ChiTietHoaDon", [
        ("1.1", "maChiTietHD",  "String",       "Khóa chính, không rỗng, duy nhất",         "Mã chi tiết hóa đơn"),
        ("1.2", "hoaDon",       "HoaDon",       "Khóa ngoại → HoaDon, không rỗng",          "Hóa đơn"),
        ("1.3", "ve",           "Ve",           "Khóa ngoại → Ve, không rỗng",              "Vé được thanh toán"),
        ("1.4", "chiTietGia",   "ChiTietGia",   "Khóa ngoại → ChiTietGia, không rỗng",      "Chi tiết giá áp dụng"),
        ("1.5", "giaTien",      "BigDecimal",   "Không rỗng, > 0",                          "Giá tiền thực tế"),
    ]),
    ("Gia", [
        ("1.1", "maGia",            "String",    "Khóa chính, không rỗng, duy nhất",   "Mã bảng giá"),
        ("1.2", "thoiGianBatDau",   "LocalDate", "Không rỗng",                         "Ngày bắt đầu hiệu lực"),
        ("1.3", "thoiGianKetThuc",  "LocalDate", "Không rỗng",                         "Ngày kết thúc hiệu lực"),
        ("1.4", "moTa",             "String",    "Có thể rỗng",                        "Mô tả bảng giá"),
        ("1.5", "trangThai",        "boolean",   "Không rỗng",                         "Trạng thái hiệu lực"),
    ]),
    ("ChiTietGia", [
        ("1.1", "maChiTietGia",  "String",    "Khóa chính, không rỗng, duy nhất",    "Mã chi tiết giá"),
        ("1.2", "gia",           "Gia",       "Khóa ngoại → Gia, không rỗng",        "Bảng giá"),
        ("1.3", "tuyen",         "Tuyen",     "Khóa ngoại → Tuyen, không rỗng",      "Tuyến áp dụng"),
        ("1.4", "loaiGhe",       "LoaiGhe",   "Không rỗng",                          "GHE_CUNG / GHE_MEM / GIUONG_NAM"),
        ("1.5", "giaNiemYet",    "double",    "Không rỗng, > 0",                     "Giá niêm yết (VNĐ)"),
    ]),
    ("KhuyenMai", [
        ("1.1", "maKhuyenMai",      "String",        "Khóa chính, không rỗng, duy nhất",   "Mã khuyến mãi"),
        ("1.2", "tenKhuyenMai",     "String",        "Không rỗng",                         "Tên chương trình khuyến mãi"),
        ("1.3", "thoiGianBatDau",   "LocalDateTime", "Không rỗng",                         "Thời gian bắt đầu"),
        ("1.4", "thoiGianKetThuc",  "LocalDateTime", "Không rỗng",                         "Thời gian kết thúc"),
        ("1.5", "moTa",             "String",        "Có thể rỗng",                        "Mô tả chương trình"),
        ("1.6", "trangThai",        "boolean",       "Không rỗng",                         "Trạng thái hoạt động"),
    ]),
    ("ChiTietKhuyenMai", [
        ("1.1", "maChiTietKM",  "String",      "Khóa chính, không rỗng, duy nhất",              "Mã chi tiết khuyến mãi"),
        ("1.2", "tenChiTiet",   "String",      "Không rỗng",                                    "Tên chi tiết khuyến mãi"),
        ("1.3", "khuyenMai",    "KhuyenMai",   "Khóa ngoại → KhuyenMai, không rỗng",            "Chương trình khuyến mãi"),
        ("1.4", "tuyen",        "Tuyen",       "Khóa ngoại → Tuyen, có thể rỗng",               "Tuyến áp dụng (null = tất cả tuyến)"),
        ("1.5", "loaiGhe",      "LoaiGhe",     "Có thể rỗng",                                   "Loại ghế áp dụng (null = tất cả)"),
        ("1.6", "phanTramGiam", "double",      "Không rỗng, ≥ 0",                               "Phần trăm giảm giá"),
    ]),
    ("ApDungKM", [
        ("1.1", "maApDung",           "String",             "Khóa chính, không rỗng, duy nhất",              "Mã áp dụng khuyến mãi"),
        ("1.2", "chiTietHoaDon",      "ChiTietHoaDon",      "Khóa ngoại → ChiTietHoaDon, không rỗng",        "Chi tiết hóa đơn"),
        ("1.3", "chiTietKhuyenMai",   "ChiTietKhuyenMai",   "Khóa ngoại → ChiTietKhuyenMai, không rỗng",     "Chi tiết khuyến mãi được áp dụng"),
    ]),
]

# ──────────────────────────────────────────────────────────────────────────────
# Build XML block for all entities
# ──────────────────────────────────────────────────────────────────────────────

def build_all_blocks():
    parts = []
    for idx, (entity_name, fields) in enumerate(ENTITIES, start=1):
        # Title paragraph
        parts.append(make_title_para(idx, entity_name))
        # Table: header + subsection + data rows
        rows = make_header_row()
        rows += make_subsection_row(1, "Khai báo thuộc tính:")
        for (stt, field, dtype, rang, note) in fields:
            rows += make_data_row(stt, field, dtype, rang, note)
        parts.append(make_table(rows))
        parts.append(empty_para())
    return "".join(parts)

# ──────────────────────────────────────────────────────────────────────────────
# Unpack / patch / repack
# ──────────────────────────────────────────────────────────────────────────────

def main():
    # Read v8 xml
    with zipfile.ZipFile(SRC, "r") as z:
        xml = z.read("word/document.xml").decode("utf-8")
        names = z.namelist()

    # Find insertion anchor: just before the "Cơ sở dữ liệu" Heading2 paragraph
    # which follows the "Đặc tả lớp" heading
    anchor = '<w:p w14:paraId="3C571EF2"'
    if anchor not in xml:
        # Fallback: search for "Cơ sở dữ liệu" Heading2 near position 302700
        m = re.search(r'<w:p [^>]*><w:pPr><w:pStyle w:val="Heading2"/>[^<]*</w:pPr><w:r>[^<]*</w:r><w:t>Cơ sở dữ liệu</w:t>', xml)
        if m:
            anchor = xml[m.start():m.start()+40]
        else:
            raise RuntimeError("Cannot find insertion anchor in document.xml")

    insert_xml = build_all_blocks()
    new_xml = xml.replace(anchor, insert_xml + anchor, 1)
    assert new_xml != xml, "Replacement did not happen!"

    # Repack: rebuild docx from scratch (avoids duplicate-name warning)
    import tempfile
    tmp = DST + ".tmp"
    with zipfile.ZipFile(SRC, "r") as zin, zipfile.ZipFile(tmp, "w", zipfile.ZIP_DEFLATED) as zout:
        for item in zin.namelist():
            if item == "word/document.xml":
                zout.writestr(item, new_xml.encode("utf-8"))
            else:
                zout.writestr(item, zin.read(item))
    os.replace(tmp, DST)

    print(f"Done. Output: {DST}")
    sz = os.path.getsize(DST)
    print(f"File size: {sz} bytes")

    # Quick sanity check
    with zipfile.ZipFile(DST, "r") as z:
        out_xml = z.read("word/document.xml").decode("utf-8")
    assert "Thực thể Ga:" in out_xml
    assert "Thực thể ApDungKM:" in out_xml
    assert "Khai báo thuộc tính:" in out_xml
    assert "3.1.2.01" in out_xml
    assert "3.1.2.21" in out_xml
    # Namespace safety check
    ns_count = out_xml.count("xmlns:")
    print(f"Namespace declarations: {ns_count} (expect ≥ 30)")
    print("Sanity checks passed.")

if __name__ == "__main__":
    main()
