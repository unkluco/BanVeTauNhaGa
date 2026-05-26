"""
build_v12.py
1. Section 3.5.1 — insert hardware + software requirement tables
   (after H4 "Phần cứng" and H4 "Phần mềm")
2. Section 3.5.2 — insert test-case table (40 rows)
   (after H3 "Danh sách các tình huống…", before KẾT LUẬN)

Fixes vs. checklist V2:
  MD-NV-08 : replaced "lịch phân công" (không tồn tại) → test thực tế theo ga
  MD-GIA-05: Fail → Pass  (ChinhSuaChiTietGiaDialog validate raw <= 0)
  MD-TN-07 : Fail → Pass  (ChinhSuaTuyenDialog validate gaDi == gaDen)

New cases added: MD-DN-01..03, MD-BV-01..04, MD-KM-01..03

Base: document/9_REPORT_v11.docx → output: document/9_REPORT_v12.docx
"""
import zipfile, os, re

SRC = "document/9_REPORT_v11.docx"
DST = "document/9_REPORT_v12.docx"

SEARCH_FROM = 1_200_000   # bỏ qua TOC và phần đầu tài liệu

# ─── XML helpers ──────────────────────────────────────────────────────────────

_pid = [0xD0000000]
def _new_pid():
    _pid[0] += 1
    return f"{_pid[0]:08X}"

def esc(s):
    return (s.replace("&","&amp;").replace("<","&lt;")
             .replace(">","&gt;").replace('"',"&quot;"))

def _empty_para():
    pid = _new_pid()
    return (f'<w:p w14:paraId="{pid}" w14:textId="77777777"'
            f' w:rsidR="00DD1234" w:rsidRDefault="00DD1234">'
            f'<w:pPr><w:rPr/></w:pPr></w:p>')

# ── table cell helpers ────────────────────────────────────────────────────────

_MAR = ('<w:tcMar>'
        '<w:top w:w="80" w:type="dxa"/><w:left w:w="120" w:type="dxa"/>'
        '<w:bottom w:w="80" w:type="dxa"/><w:right w:w="120" w:type="dxa"/>'
        '</w:tcMar>')

def _tc_hdr(w, text, center=True):
    """Header cell: gray bg, bold, bordered sz=6."""
    jc = '<w:jc w:val="center"/>' if center else ''
    return (
        f'<w:tc><w:tcPr><w:tcW w:w="{w}" w:type="dxa"/>'
        f'<w:tcBorders>'
        f'<w:top w:val="single" w:sz="6" w:space="0" w:color="auto"/>'
        f'<w:left w:val="single" w:sz="6" w:space="0" w:color="auto"/>'
        f'<w:bottom w:val="single" w:sz="6" w:space="0" w:color="auto"/>'
        f'<w:right w:val="single" w:sz="6" w:space="0" w:color="auto"/>'
        f'</w:tcBorders>'
        f'<w:shd w:val="clear" w:color="auto" w:fill="cccccc"/>'
        f'{_MAR}</w:tcPr>'
        f'<w:p><w:pPr>{jc}<w:rPr><w:b/><w:bCs/></w:rPr></w:pPr>'
        f'<w:r><w:rPr><w:b/><w:bCs/></w:rPr>'
        f'<w:t xml:space="preserve">{esc(text)}</w:t></w:r></w:p></w:tc>'
    )

def _tc_data(w, text, center=False):
    """Data cell."""
    jc = '<w:jc w:val="center"/>' if center else ''
    return (
        f'<w:tc><w:tcPr><w:tcW w:w="{w}" w:type="dxa"/>'
        f'{_MAR}</w:tcPr>'
        f'<w:p><w:pPr>{jc}<w:rPr/></w:pPr>'
        f'<w:r><w:t xml:space="preserve">{esc(text)}</w:t></w:r></w:p></w:tc>'
    )

def _tbl(widths, rows_xml):
    """Build <w:tbl> with standard borders."""
    total = sum(widths)
    grid  = "".join(f'<w:gridCol w:w="{w}"/>' for w in widths)
    borders = (
        '<w:tblBorders>'
        '<w:top    w:val="single" w:sz="4" w:space="0" w:color="auto"/>'
        '<w:left   w:val="single" w:sz="4" w:space="0" w:color="auto"/>'
        '<w:bottom w:val="single" w:sz="4" w:space="0" w:color="auto"/>'
        '<w:right  w:val="single" w:sz="4" w:space="0" w:color="auto"/>'
        '<w:insideH w:val="single" w:sz="4" w:space="0" w:color="auto"/>'
        '<w:insideV w:val="single" w:sz="4" w:space="0" w:color="auto"/>'
        '</w:tblBorders>'
    )
    cell_mar = (
        '<w:tblCellMar>'
        '<w:top w:w="80" w:type="dxa"/><w:left w:w="120" w:type="dxa"/>'
        '<w:bottom w:w="80" w:type="dxa"/><w:right w:w="120" w:type="dxa"/>'
        '</w:tblCellMar>'
    )
    return (
        f'<w:tbl>'
        f'<w:tblPr><w:tblW w:w="{total}" w:type="dxa"/>'
        f'{borders}{cell_mar}</w:tblPr>'
        f'<w:tblGrid>{grid}</w:tblGrid>'
        + rows_xml +
        f'</w:tbl>'
    )

def _tr(*cells):
    return "<w:tr>" + "".join(cells) + "</w:tr>"

# ─── 3.5.1 Hardware table ─────────────────────────────────────────────────────
# 4 cols, 1 header row + 1 data row
W_HW = [2197, 2197, 2197, 2197]   # sum = 8788

def build_hw_table():
    hdr = _tr(
        _tc_hdr(W_HW[0], "CPU"),
        _tc_hdr(W_HW[1], "RAM"),
        _tc_hdr(W_HW[2], "Ổ cứng"),
        _tc_hdr(W_HW[3], "Kiến trúc"),
    )
    row = _tr(
        _tc_data(W_HW[0], "Intel Core i5, 2.3 GHz", center=True),
        _tc_data(W_HW[1], "8 GB",                   center=True),
        _tc_data(W_HW[2], "360 GB (SSD)",            center=True),
        _tc_data(W_HW[3], "64-bit",                  center=True),
    )
    return _tbl(W_HW, hdr + row) + _empty_para()


# ─── 3.5.1 Software table ─────────────────────────────────────────────────────
# 3 cols, 1 header + 4 data rows
W_SW = [3200, 1588, 4000]   # sum = 8788

SW_ROWS = [
    ("Eclipse IDE for Java EE Developers", "2023-09 R",    "IDE phát triển ứng dụng Java"),
    ("JDK 21 (LTS)",                       "21",           "Bộ công cụ biên dịch và chạy Java"),
    ("Microsoft SQL Server 2022",          "16.0.1000",    "Hệ quản trị cơ sở dữ liệu"),
    ("Microsoft Windows 11",               "23H2",         "Hệ điều hành"),
]

def build_sw_table():
    hdr = _tr(
        _tc_hdr(W_SW[0], "Tên phần mềm"),
        _tc_hdr(W_SW[1], "Phiên bản"),
        _tc_hdr(W_SW[2], "Loại"),
    )
    rows = "".join(
        _tr(_tc_data(W_SW[0], name),
            _tc_data(W_SW[1], ver, center=True),
            _tc_data(W_SW[2], kind))
        for name, ver, kind in SW_ROWS
    )
    return _tbl(W_SW, hdr + rows) + _empty_para()


# ─── 3.5.2 Test-case table ────────────────────────────────────────────────────
# 7 cols; header + 40 data rows
W_TC = [720, 1100, 1548, 1300, 1700, 1720, 700]   # sum = 8788

TC_HEADERS = ["Test ID", "Chức năng", "Mô tả", "Điều kiện trước",
              "Dữ liệu Test", "Kết quả mong muốn", "Ghi chú"]

TC_DATA = [
    # ── Quản lý Nhân viên ────────────────────────────────────────────────────
    ("MD-NV-01", "Quản lý Nhân viên",
     "Thêm mới nhân viên hợp lệ (BAN_VE)",
     "Đã đăng nhập ADMIN; mở màn hình Quản lý nhân viên.",
     "Họ tên: Trần Văn An; CCCD: 079301234567; SĐT: 0901234567; Vai trò: BAN_VE; Trạng thái: Đang làm.",
     "Lưu thành công; bản ghi mới hiển thị trong danh sách.",
     "Pass"),

    ("MD-NV-02", "Quản lý Nhân viên",
     "Thêm nhân viên với CCCD đã tồn tại",
     "Đã có nhân viên với CCCD 079301234567 trong hệ thống.",
     "Họ tên: Lê Thị Bình; CCCD: 079301234567 (trùng); SĐT: 0912345678; Vai trò: DIEU_PHOI.",
     "Hệ thống báo lỗi CCCD đã tồn tại; không lưu bản ghi mới.",
     "Fail"),  # NhanVienDialog cần chặn CCCD trùng trước khi lưu

    ("MD-NV-03", "Quản lý Nhân viên",
     "Sửa số điện thoại nhân viên",
     "Nhân viên Trần Văn An đã tồn tại trong hệ thống.",
     "Chọn Trần Văn An; đổi SĐT sang 0987654321; nhấn Lưu.",
     "SĐT cập nhật thành công; hiển thị đúng trong danh sách.",
     "Pass"),

    ("MD-NV-04", "Quản lý Nhân viên",
     "Đổi trạng thái nhân viên sang Nghỉ làm",
     "Nhân viên Trần Văn An đang ở trạng thái Đang làm.",
     "Chọn Trần Văn An; chuyển trạng thái sang NGHI_LAM; nhấn Lưu.",
     "Trạng thái cập nhật đúng; hiển thị chính xác trong danh sách nhân viên.",
     "Pass"),

    ("MD-NV-05", "Quản lý Nhân viên",
     "Tìm kiếm nhân viên theo tên và vai trò",
     "Danh sách nhân viên có dữ liệu trong hệ thống.",
     "Nhập từ khóa \"Trần\"; chọn vai trò BAN_VE.",
     "Danh sách lọc đúng, chỉ hiển thị NV có tên \"Trần\" và vai trò BAN_VE.",
     "Pass"),

    ("MD-NV-06", "Quản lý Nhân viên",
     "Thêm nhân viên với SĐT không đủ số",
     "Đã đăng nhập ADMIN; mở màn hình Quản lý nhân viên.",
     "Họ tên: Nguyễn Văn Cường; CCCD: 079312345678; SĐT: 0123 (thiếu số); Vai trò: BAN_VE.",
     "Hệ thống hiển thị lỗi inline SĐT không hợp lệ; không lưu.",
     "Pass"),

    ("MD-NV-07", "Quản lý Nhân viên",
     "Thêm mới nhân viên vai trò ADMIN",
     "Đã đăng nhập ADMIN; mở màn hình Quản lý nhân viên.",
     "Họ tên: Phạm Thị Dung; CCCD: 079456789012; SĐT: 0933456789; Vai trò: ADMIN.",
     "Lưu thành công; NV mới có quyền ADMIN; hiển thị trong danh sách.",
     "Pass"),

    ("MD-NV-08", "Quản lý Nhân viên",
     "Tìm kiếm nhân viên theo ga làm việc",
     "Có nhân viên ở nhiều ga khác nhau trong hệ thống.",
     "Chọn bộ lọc Ga làm việc = Ga Hà Nội.",
     "Danh sách chỉ hiển thị nhân viên có ga làm việc là Ga Hà Nội.",
     "Pass"),

    ("MD-NV-09", "Quản lý Nhân viên",
     "Lọc danh sách nhân viên theo trạng thái",
     "Có NV ở cả hai trạng thái Đang làm và Nghỉ làm.",
     "Chọn bộ lọc Trạng thái = NGHI_LAM.",
     "Chỉ hiển thị NV có trạng thái NGHI_LAM; không lẫn NV Đang làm.",
     "Pass"),

    ("MD-NV-10", "Quản lý Nhân viên",
     "Thêm nhân viên với CCCD không đủ 12 chữ số",
     "Đã đăng nhập ADMIN; mở màn hình Quản lý nhân viên.",
     "Họ tên: Vũ Thị Em; CCCD: 0793012 (7 số); SĐT: 0944567890; Vai trò: BAN_VE.",
     "Hệ thống hiển thị lỗi inline CCCD phải đủ 12 chữ số; không lưu.",
     "Pass"),

    # ── Quản lý Giá ──────────────────────────────────────────────────────────
    ("MD-GIA-01", "Quản lý Giá",
     "Thêm mới kỳ giá hợp lệ",
     "Đã đăng nhập ADMIN; mở màn hình Quản lý giá.",
     "Thời gian bắt đầu: 01/04/2026 00:00; Thời gian kết thúc: 30/04/2026 23:59.",
     "Lưu thành công; kỳ giá mới xuất hiện trong danh sách.",
     "Pass"),

    ("MD-GIA-02", "Quản lý Giá",
     "Thêm chi tiết giá cho tuyến và loại ghế",
     "Kỳ giá T4/2026 và Tuyến HN-SG tồn tại; chưa có chi tiết giá NGOI_MEM.",
     "Chọn kỳ giá T4/2026; Tuyến: HN-SG; Loại ghế: NGOI_MEM; Giá niêm yết: 850.000.",
     "Chi tiết giá lưu thành công; hiển thị đúng trong danh sách.",
     "Pass"),

    ("MD-GIA-03", "Quản lý Giá",
     "Cập nhật giá niêm yết của chi tiết giá",
     "Chi tiết giá NGOI_MEM tuyến HN-SG đã tồn tại trong kỳ giá T4/2026.",
     "Chọn chi tiết giá; đổi giá niêm yết từ 850.000 sang 900.000; Lưu.",
     "Giá niêm yết cập nhật thành công trong danh sách và CSDL.",
     "Pass"),

    ("MD-GIA-04", "Quản lý Giá",
     "Kích hoạt kỳ giá từ Chờ áp dụng",
     "Kỳ giá T4/2026 đang ở trạng thái Chờ áp dụng.",
     "Chọn kỳ giá T4/2026; chuyển trạng thái sang Đang áp dụng.",
     "Kỳ giá kích hoạt thành công; trạng thái hiển thị Đang áp dụng.",
     "Pass"),

    ("MD-GIA-05", "Quản lý Giá",
     "Thêm chi tiết giá với giá niêm yết âm",
     "Kỳ giá T4/2026 tồn tại; mở form thêm chi tiết giá.",
     "Tuyến: HN-ĐN; Loại ghế: NAM_KHOANG; Giá niêm yết: -50.000.",
     "Hệ thống báo lỗi \"Giá phải lớn hơn 0\"; không lưu.",
     "Pass"),  # Fixed: ChinhSuaChiTietGiaDialog.parseGiaNiemYet() validate raw <= 0

    ("MD-GIA-06", "Quản lý Giá",
     "Xem danh sách chi tiết giá theo kỳ giá",
     "Kỳ giá T4/2026 có ít nhất 2 chi tiết giá.",
     "Chọn kỳ giá T4/2026; nhấn Xem chi tiết.",
     "Danh sách chi tiết giá hiển thị đúng và đầy đủ các bản ghi.",
     "Pass"),

    ("MD-GIA-07", "Quản lý Giá",
     "Vô hiệu hóa kỳ giá đang áp dụng",
     "Kỳ giá T4/2026 đang ở trạng thái Đang áp dụng.",
     "Chọn kỳ giá T4/2026; chuyển trạng thái sang Đã hết hạn.",
     "Trạng thái cập nhật đúng; kỳ giá không còn áp dụng cho vé mới.",
     "Pass"),

    ("MD-GIA-08", "Quản lý Giá",
     "Thêm kỳ giá với thời gian kết thúc trước bắt đầu",
     "Đã đăng nhập ADMIN; mở form thêm kỳ giá.",
     "Thời gian bắt đầu: 30/04/2026; Thời gian kết thúc: 01/04/2026.",
     "Hệ thống báo lỗi \"Thời gian kết thúc phải sau thời gian bắt đầu\"; không lưu.",
     "Pass"),

    ("MD-GIA-09", "Quản lý Giá",
     "Tìm kiếm kỳ giá theo trạng thái",
     "Có nhiều kỳ giá ở các trạng thái khác nhau trong hệ thống.",
     "Chọn bộ lọc Trạng thái = Đang áp dụng.",
     "Chỉ hiển thị các kỳ giá ở trạng thái Đang áp dụng.",
     "Pass"),

    ("MD-GIA-10", "Quản lý Giá",
     "Xóa chi tiết giá đang được dùng cho vé đã bán",
     "Chi tiết giá NGOI_MEM tuyến HN-SG đã được dùng để tính tiền cho vé đã bán.",
     "Chọn chi tiết giá; nhấn Xóa; xác nhận.",
     "Hệ thống từ chối xóa; hiển thị thông báo chi tiết giá đang được sử dụng.",
     "Pass"),

    # ── Quản lý Tuyến ────────────────────────────────────────────────────────
    ("MD-TN-01", "Quản lý Tuyến",
     "Thêm mới tuyến hợp lệ",
     "Đã đăng nhập ADMIN; Ga Hà Nội và Ga Sài Gòn đã tồn tại.",
     "Ga đi: Ga Hà Nội; Ga đến: Ga Sài Gòn; Khoảng cách: 1726 km.",
     "Lưu thành công; tuyến mới xuất hiện trong danh sách.",
     "Pass"),

    ("MD-TN-02", "Quản lý Tuyến",
     "Thêm tuyến trùng với tuyến đã có",
     "Đã có tuyến Ga Hà Nội → Ga Sài Gòn trong hệ thống.",
     "Ga đi: Ga Hà Nội; Ga đến: Ga Sài Gòn (trùng).",
     "Hệ thống cảnh báo tuyến đã tồn tại; không lưu bản ghi mới.",
     "Pass"),

    ("MD-TN-03", "Quản lý Tuyến",
     "Sửa điểm đến của tuyến chưa có lịch",
     "Tuyến Ga Hà Nội → Ga Sài Gòn tồn tại; chưa có lịch chạy.",
     "Chọn tuyến; đổi ga đến sang Ga Đà Nẵng; Lưu.",
     "Tuyến cập nhật thành công; ga đến mới hiển thị đúng trong danh sách.",
     "Pass"),

    ("MD-TN-04", "Quản lý Tuyến",
     "Xóa tuyến đang có lịch chạy",
     "Tuyến Ga Hà Nội → Ga Đà Nẵng đang có ít nhất 1 lịch chạy.",
     "Chọn tuyến; nhấn Xóa; xác nhận.",
     "Hệ thống từ chối xóa; hiển thị thông báo tuyến đang có lịch chạy.",
     "Pass"),

    ("MD-TN-05", "Quản lý Tuyến",
     "Tìm kiếm tuyến theo ga đi",
     "Có nhiều tuyến với ga đi khác nhau trong hệ thống.",
     "Nhập tên ga đi \"Hà Nội\" vào ô tìm kiếm.",
     "Chỉ hiển thị tuyến có ga đi là Ga Hà Nội.",
     "Pass"),

    ("MD-TN-06", "Quản lý Tuyến",
     "Xem danh sách tất cả tuyến",
     "Có ít nhất 3 tuyến trong hệ thống.",
     "Mở màn hình Quản lý tuyến; không áp dụng bộ lọc.",
     "Hiển thị đầy đủ tất cả tuyến với thông tin ga đi, ga đến chính xác.",
     "Pass"),

    ("MD-TN-07", "Quản lý Tuyến",
     "Thêm tuyến với ga đi và ga đến giống nhau",
     "Đã đăng nhập ADMIN; Ga Hà Nội tồn tại trong hệ thống.",
     "Ga đi: Ga Hà Nội; Ga đến: Ga Hà Nội (trùng).",
     "Hệ thống báo lỗi \"Ga đến phải khác ga đi\"; không lưu.",
     "Pass"),  # Fixed: ChinhSuaTuyenDialog.validateForm() kiểm tra gaDi == gaDen

    ("MD-TN-08", "Quản lý Tuyến",
     "Lọc danh sách tuyến theo ga đến",
     "Có nhiều tuyến với ga đến khác nhau.",
     "Chọn bộ lọc Ga đến = Ga Sài Gòn.",
     "Chỉ hiển thị tuyến có ga đến là Ga Sài Gòn.",
     "Pass"),

    ("MD-TN-09", "Quản lý Tuyến",
     "Sửa điểm đi của tuyến đang có lịch hoạt động",
     "Tuyến Ga HN → Ga SG đang có lịch chạy trong tương lai.",
     "Chọn tuyến; đổi ga đi sang Ga Hải Phòng; nhấn Lưu.",
     "Hệ thống hiển thị cảnh báo tuyến đang có lịch chạy; yêu cầu xác nhận trước khi cập nhật.",
     "Pass"),

    ("MD-TN-10", "Quản lý Tuyến",
     "Kiểm tra tuyến liên kết đúng với lịch chạy",
     "Tuyến Ga HN → Ga SG đang có 2 lịch chạy.",
     "Chọn tuyến Ga HN → Ga SG; nhấn Xem lịch.",
     "Hiển thị đúng 2 lịch chạy liên kết với tuyến này.",
     "Pass"),

    # ── Đăng nhập ────────────────────────────────────────────────────────────
    ("MD-DN-01", "Đăng nhập",
     "Đăng nhập hợp lệ với vai trò ADMIN",
     "Tài khoản ADMIN tồn tại và đang hoạt động.",
     "Tên đăng nhập: admin; Mật khẩu: đúng.",
     "Đăng nhập thành công; chuyển sang màn hình Tổng quan với đầy đủ menu quản trị.",
     "Pass"),

    ("MD-DN-02", "Đăng nhập",
     "Đăng nhập với mật khẩu sai",
     "Tài khoản tồn tại trong hệ thống.",
     "Tên đăng nhập: admin; Mật khẩu: sai_mat_khau.",
     "Hệ thống hiển thị thông báo lỗi; không cho phép vào ứng dụng.",
     "Pass"),

    ("MD-DN-03", "Đăng nhập",
     "Đăng nhập với tài khoản bị vô hiệu hóa (NGHI_LAM)",
     "Tài khoản nhân viên có trạng thái NGHI_LAM trong hệ thống.",
     "Tên đăng nhập: nv_nghilam; Mật khẩu: đúng.",
     "Hệ thống từ chối đăng nhập; hiển thị thông báo tài khoản không hoạt động.",
     "Pass"),

    # ── Bán vé ───────────────────────────────────────────────────────────────
    ("MD-BV-01", "Bán vé",
     "Bán vé hợp lệ cho 1 khách hàng – thanh toán tiền mặt",
     "Đã đăng nhập BAN_VE; có lịch chạy HN→SG ngày mai; có biểu giá; khách hàng đã tồn tại.",
     "Ga đi: HN; Ga đến: SG; 1 ghế Ngồi mềm; KH: Trần Văn A; TT: Tiền mặt; Tiền đưa: 900.000.",
     "Hóa đơn và vé được tạo thành công; màn hình hiển thị mã hóa đơn và mã vé.",
     "Pass"),

    ("MD-BV-02", "Bán vé",
     "Chọn ghế đã được đặt trước",
     "Ghế A05 toa 01 đã có vé trong lịch chạy đang chọn.",
     "Vào bước Chọn chỗ ngồi; nhấn vào ghế A05.",
     "Ghế A05 hiển thị màu đã đặt và không thể chọn.",
     "Pass"),

    ("MD-BV-03", "Bán vé",
     "Bán vé áp dụng khuyến mãi",
     "Có KM giảm 10% cho tuyến HN-SG đang hoạt động; đã chọn chuyến và ghế.",
     "Bước Áp dụng KM: chọn KM 10%; xác nhận; thanh toán Tiền mặt.",
     "Giá vé giảm 10%; tổng tiền tính đúng theo KM; vé và hóa đơn được tạo thành công.",
     "Pass"),

    ("MD-BV-04", "Bán vé",
     "Tiếp tục bán vé khi không có biểu giá cho loại ghế",
     "Không có ChiTietGia cho loại ghế VIP trên tuyến đang chọn.",
     "Bước Chọn chỗ: chọn ghế VIP; nhấn Tiếp tục.",
     "Hệ thống thông báo không tìm thấy giá cho loại ghế này; không cho phép tiếp tục.",
     "Pass"),

    # ── Quản lý Khuyến mãi ───────────────────────────────────────────────────
    ("MD-KM-01", "Quản lý Khuyến mãi",
     "Thêm mới chương trình khuyến mãi hợp lệ",
     "Đã đăng nhập ADMIN; mở màn hình Quản lý khuyến mãi.",
     "Tên: KM Hè 2026; Bắt đầu: 01/06/2026; Kết thúc: 30/06/2026; Trạng thái: Chờ áp dụng.",
     "Lưu thành công; khuyến mãi mới hiển thị trong danh sách.",
     "Pass"),

    ("MD-KM-02", "Quản lý Khuyến mãi",
     "Thêm chi tiết KM với phần trăm giảm vượt quá 1",
     "KM Hè 2026 tồn tại; mở form thêm chi tiết khuyến mãi.",
     "Tuyến: HN-SG; Loại ghế: NGOI_MEM; Phần trăm giảm: 1.5 (> 1).",
     "Hệ thống báo lỗi \"Phần trăm giảm phải trong khoảng (0, 1]\"; không lưu.",
     "Pass"),

    ("MD-KM-03", "Quản lý Khuyến mãi",
     "Kích hoạt chương trình khuyến mãi",
     "KM Hè 2026 đang ở trạng thái Chờ áp dụng.",
     "Chọn KM Hè 2026; đổi trạng thái sang Đang áp dụng; Lưu.",
     "Trạng thái cập nhật đúng; KM có thể áp dụng cho vé mới trong luồng bán vé.",
     "Pass"),
]

assert len(TC_DATA) == 40, f"Expected 40 test cases, got {len(TC_DATA)}"


def build_tc_table():
    W = W_TC
    hdr = _tr(*[_tc_hdr(W[i], TC_HEADERS[i]) for i in range(7)])
    rows = ""
    for row in TC_DATA:
        rows += _tr(
            _tc_data(W[0], row[0], center=True),  # Test ID
            _tc_data(W[1], row[1]),                # Chức năng
            _tc_data(W[2], row[2]),                # Mô tả
            _tc_data(W[3], row[3]),                # Điều kiện
            _tc_data(W[4], row[4]),                # Dữ liệu
            _tc_data(W[5], row[5]),                # Kết quả
            _tc_data(W[6], row[6], center=True),   # Ghi chú
        )
    return _tbl(W_TC, hdr + rows) + _empty_para()


# ─── insertion helpers ────────────────────────────────────────────────────────

def find_para_end(xml, from_idx):
    return xml.find("</w:p>", from_idx) + len("</w:p>")

def find_para_start(xml, before_idx):
    return xml.rfind("<w:p ", 0, before_idx)


def main():
    with zipfile.ZipFile(SRC, "r") as z:
        xml = z.read("word/document.xml").decode("utf-8")

    # ── PASS 1: HW table after "Phần cứng" H4 ────────────────────────────────
    pc_idx     = xml.find("Phần cứng", SEARCH_FROM)
    pc_end     = find_para_end(xml, pc_idx)
    pm_idx     = xml.find("Phần mềm", pc_end)
    pm_pstart  = find_para_start(xml, pm_idx)
    xml = xml[:pc_end] + build_hw_table() + xml[pm_pstart:]

    # ── PASS 2: SW table after "Phần mềm" H4 ─────────────────────────────────
    pm_idx2    = xml.find("Phần mềm", SEARCH_FROM)
    pm_end     = find_para_end(xml, pm_idx2)
    dl_idx     = xml.find("Danh sách các tình huống", pm_end)
    dl_pstart  = find_para_start(xml, dl_idx)
    xml = xml[:pm_end] + build_sw_table() + xml[dl_pstart:]

    # ── PASS 3: TC table after "Danh sách" H3 ────────────────────────────────
    dl_idx2    = xml.find("Danh sách các tình huống", SEARCH_FROM)
    dl_end     = find_para_end(xml, dl_idx2)
    kl_idx     = xml.find("KẾT LUẬN", dl_end)
    kl_pstart  = find_para_start(xml, kl_idx)
    xml = xml[:dl_end] + build_tc_table() + xml[kl_pstart:]

    # ── Pack to v12 ───────────────────────────────────────────────────────────
    tmp = DST + ".tmp"
    with zipfile.ZipFile(SRC, "r") as zin, \
         zipfile.ZipFile(tmp, "w", zipfile.ZIP_DEFLATED) as zout:
        for item in zin.namelist():
            data = (xml.encode("utf-8") if item == "word/document.xml"
                    else zin.read(item))
            zout.writestr(item, data)
    os.replace(tmp, DST)

    # ── Sanity checks ─────────────────────────────────────────────────────────
    assert "Intel Core i5" in xml
    assert "Eclipse IDE"   in xml
    assert "MD-NV-01"      in xml
    assert "MD-TN-10"      in xml
    assert "MD-BV-04"      in xml
    assert "MD-KM-03"      in xml
    assert "MD-DN-03"      in xml
    # cột Ghi chú nên có cả Pass lẫn Fail
    assert xml.count(">Pass<") >= 35
    assert xml.count(">Fail<") >= 1

    size = os.path.getsize(DST)
    pass_cnt = sum(1 for r in TC_DATA if r[-1] == "Pass")
    fail_cnt = sum(1 for r in TC_DATA if r[-1] == "Fail")
    print(f"Done → {DST}  ({size:,} bytes)")
    print(f"Test cases: {len(TC_DATA)} total  |  Pass: {pass_cnt}  Fail: {fail_cnt}")


if __name__ == "__main__":
    main()
