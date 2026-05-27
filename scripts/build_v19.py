"""
build_v19.py — Thêm PHỤ LỤC B: ĐÁNH GIÁ ĐÓNG GÓP CỦA THÀNH VIÊN
  - Heading1 (giống PHỤ LỤC A), chèn trước paragraph cuối cùng (trước sectPr)
  - Bảng 5 cột: STT | Họ và tên | Vai trò chính | Tỉ lệ đóng góp | Nhận xét
  - Tổng chiều rộng: 8788 dxa (khớp với lề trang A4 của doc)
  - Màu header: DEEBF6 (giống nhật ký), phần trăm dựa trên phân công thực tế

SRC = 9_REPORT_v18.docx  →  DST = 9_REPORT_v19.docx
Rủi ro: nếu người dùng đã sửa sectPr cuối (lề trang, v.v.) thì cần giữ nguyên.
"""
import zipfile, shutil, os, re

SRC = "document/9_REPORT_v18.docx"
DST = "document/9_REPORT_v19.docx"

shutil.copy(SRC, DST)

with zipfile.ZipFile(DST, "r") as z:
    xml   = z.read("word/document.xml").decode("utf-8")
    files = {n: z.read(n) for n in z.namelist()}

# ── paraId counter ────────────────────────────────────────────────────────────
_pid = [0xEE000001]
def pid():
    v = f"{_pid[0]:08X}"; _pid[0] += 1; return v

# ── XML helpers ───────────────────────────────────────────────────────────────
def h1(text):
    """Heading1 không đánh số (numId=0) — khớp với style PHỤ LỤC A."""
    return (
        f'<w:p w14:paraId="{pid()}" w14:textId="77777777" '
        f'w:rsidR="00000000" w:rsidRDefault="00000000">'
        f'<w:pPr><w:pStyle w:val="Heading1"/>'
        f'<w:numPr><w:ilvl w:val="0"/><w:numId w:val="0"/></w:numPr>'
        f'</w:pPr>'
        f'<w:r><w:t>{text}</w:t></w:r></w:p>'
    )

def para(text, bold=False, center=False):
    """Đoạn văn thông thường với tùy chọn bold/center."""
    jc = '<w:jc w:val="center"/>' if center else '<w:jc w:val="both"/>'
    rpr = '<w:rPr><w:b/><w:lang w:val="vi-VN"/></w:rPr>' if bold else '<w:rPr><w:lang w:val="vi-VN"/></w:rPr>'
    rpr_run = '<w:b/><w:lang w:val="vi-VN"/>' if bold else '<w:lang w:val="vi-VN"/>'
    return (
        f'<w:p w14:paraId="{pid()}" w14:textId="77777777" '
        f'w:rsidR="00000000" w:rsidRDefault="00000000">'
        f'<w:pPr><w:spacing w:before="60" w:after="60"/>{jc}{rpr}</w:pPr>'
        f'<w:r><w:rPr>{rpr_run}</w:rPr><w:t xml:space="preserve">{text}</w:t></w:r></w:p>'
    )

# ── Table builder ─────────────────────────────────────────────────────────────
# Tổng = 8788 dxa (= 11907 - 1985 (left) - 1134 (right))
COL_WIDTHS = [500, 1700, 2800, 1000, 2788]  # STT | Tên | Vai trò | % | Nhận xét

BORDER_H = (
    '<w:top w:val="single" w:sz="4" w:space="0" w:color="000000"/>'
    '<w:bottom w:val="single" w:sz="4" w:space="0" w:color="000000"/>'
    '<w:left w:val="single" w:sz="4" w:space="0" w:color="000000"/>'
    '<w:right w:val="single" w:sz="4" w:space="0" w:color="000000"/>'
)
BORDER_D = (
    '<w:top w:val="dotted" w:sz="4" w:space="0" w:color="000000"/>'
    '<w:bottom w:val="dotted" w:sz="4" w:space="0" w:color="000000"/>'
    '<w:left w:val="single" w:sz="4" w:space="0" w:color="000000"/>'
    '<w:right w:val="single" w:sz="4" w:space="0" w:color="000000"/>'
)

def tc(w, text, fill="FFFFFF", bold=False, center=False, borders=BORDER_D):
    """Tạo ô bảng."""
    jc = 'center' if center else 'left'
    rpr = '<w:rPr><w:b/></w:rPr>' if bold else ''
    rpr_r = '<w:rPr><w:b/></w:rPr>' if bold else ''
    return (
        f'<w:tc>'
        f'<w:tcPr><w:tcW w:w="{w}" w:type="dxa"/>'
        f'<w:tcBorders>{borders}</w:tcBorders>'
        f'<w:shd w:val="clear" w:color="auto" w:fill="{fill}"/>'
        f'<w:vAlign w:val="center"/></w:tcPr>'
        f'<w:p w14:paraId="{pid()}" w14:textId="77777777" '
        f'w:rsidR="00000000" w:rsidRDefault="00000000">'
        f'<w:pPr><w:spacing w:before="0" w:line="259" w:lineRule="auto"/>'
        f'<w:ind w:firstLine="0"/><w:jc w:val="{jc}"/>{rpr}</w:pPr>'
        f'<w:r>{rpr_r}<w:t xml:space="preserve">{text}</w:t></w:r></w:p></w:tc>'
    )

def tr_header(cells_data):
    """Hàng tiêu đề màu xanh."""
    cells = "".join(
        tc(COL_WIDTHS[i], text, fill="DEEBF6", bold=True,
           center=(i in (0, 3)), borders=BORDER_H)
        for i, text in enumerate(cells_data)
    )
    return (
        f'<w:tr w:rsidR="00000000" w14:paraId="{pid()}" w14:textId="77777777">'
        f'<w:trPr><w:trHeight w:val="420"/><w:tblHeader/></w:trPr>'
        f'{cells}</w:tr>'
    )

def tr_data(cells_data):
    """Hàng dữ liệu thông thường."""
    cells = "".join(
        tc(COL_WIDTHS[i], text, center=(i in (0, 3)))
        for i, text in enumerate(cells_data)
    )
    return (
        f'<w:tr w:rsidR="00000000" w14:paraId="{pid()}" w14:textId="77777777">'
        f'<w:trPr><w:trHeight w:val="360"/></w:trPr>'
        f'{cells}</w:tr>'
    )

def build_table(header, rows):
    """Bảng hoàn chỉnh với header + dữ liệu."""
    total_w = sum(COL_WIDTHS)
    header_row = tr_header(header)
    data_rows  = "".join(tr_data(r) for r in rows)
    return (
        f'<w:tbl>'
        f'<w:tblPr>'
        f'<w:tblStyle w:val="TableGrid"/>'
        f'<w:tblW w:w="{total_w}" w:type="dxa"/>'
        f'<w:tblBorders>'
        f'<w:top w:val="single" w:sz="4" w:space="0" w:color="000000"/>'
        f'<w:left w:val="single" w:sz="4" w:space="0" w:color="000000"/>'
        f'<w:bottom w:val="single" w:sz="4" w:space="0" w:color="000000"/>'
        f'<w:right w:val="single" w:sz="4" w:space="0" w:color="000000"/>'
        f'<w:insideH w:val="dotted" w:sz="4" w:space="0" w:color="000000"/>'
        f'<w:insideV w:val="single" w:sz="4" w:space="0" w:color="000000"/>'
        f'</w:tblBorders>'
        f'<w:tblLook w:val="04A0"/>'
        f'</w:tblPr>'
        f'<w:tblGrid>'
        + "".join(f'<w:gridCol w:w="{w}"/>' for w in COL_WIDTHS)
        + f'</w:tblGrid>'
        f'{header_row}{data_rows}</w:tbl>'
    )

# ── Dữ liệu thành viên ────────────────────────────────────────────────────────
# Phần trăm dựa trên phân công: Kiệt (nhóm trưởng + module nặng) 22%,
# Bảo/Hoàng 20%, Huy/Lộc 19% — tổng 100%.
MEMBERS = [
    (
        "1", "Kiệt",
        "Trưởng nhóm; module Bán vé (full flow), Tổng quan (Dashboard), "
        "Thống kê &amp; Báo cáo; xây dựng cơ sở hạ tầng project (DBConnection, base DAO/BLL)",
        "22%",
        "Đảm nhận vai trò điều phối chung và các module cốt lõi, "
        "phức tạp nhất của hệ thống. Hoàn thành đúng tiến độ, chất lượng tốt."
    ),
    (
        "2", "Bảo",
        "Module Quản lý giá (ChiTietGia), Quản lý khuyến mãi (KhuyenMai), "
        "Quản lý hóa đơn (HoaDon); hỗ trợ viết SQL tạo bảng và insert dữ liệu mẫu",
        "20%",
        "Xây dựng đầy đủ nghiệp vụ tài chính của hệ thống. "
        "Logic tính giá, áp dụng khuyến mãi hoạt động chính xác."
    ),
    (
        "3", "Hoàng",
        "Module Quản lý vé, Quản lý tuyến đường, Quản lý lịch chạy; "
        "hoàn thiện báo cáo phần thiết kế hệ thống và giao diện",
        "20%",
        "Đảm bảo tính nhất quán giữa dữ liệu tuyến — lịch — vé. "
        "Đóng góp tích cực vào tài liệu thiết kế."
    ),
    (
        "4", "Huy",
        "Module Quản lý đoàn tàu, Quản lý toa tàu, Quản lý đầu máy; "
        "hoàn thiện báo cáo phần Use Case và phân tích",
        "19%",
        "Xử lý tốt cấu trúc dữ liệu phân cấp phức tạp (đoàn tàu → toa → ghế). "
        "Hoàn thành đúng hạn."
    ),
    (
        "5", "Lộc",
        "Module Quản lý nhân viên, Quản lý khách hàng; "
        "hoàn thiện nhật ký, phụ lục và tài liệu tham khảo trong báo cáo",
        "19%",
        "Đảm nhận phân hệ quản lý người dùng, đồng thời đóng góp lớn "
        "vào việc hoàn thiện tài liệu báo cáo cuối kỳ."
    ),
]

HEADER = ["STT", "Họ và tên", "Vai trò &amp; Nhiệm vụ chính", "Tỉ lệ đóng góp", "Nhận xét"]

# ── Xây dựng nội dung PHỤ LỤC B ──────────────────────────────────────────────
appendix_b = ""

# Heading
appendix_b += h1("PHỤ LỤC B: ĐÁNH GIÁ ĐÓNG GÓP CỦA THÀNH VIÊN")

# Giới thiệu ngắn
appendix_b += para(
    "Bảng dưới đây tổng hợp mức độ đóng góp của từng thành viên trong nhóm "
    "dựa trên phân công nhiệm vụ thực tế trong suốt quá trình thực hiện đồ án "
    "(12 tuần, từ 31/12/2025 đến 24/03/2026). Tỉ lệ được tính trên tổng 100% "
    "công sức của cả nhóm, phản ánh khối lượng và độ phức tạp của từng nhiệm vụ."
)

# Blank line trước bảng
appendix_b += (
    f'<w:p w14:paraId="{pid()}" w14:textId="77777777" '
    f'w:rsidR="00000000" w:rsidRDefault="00000000">'
    f'<w:pPr><w:spacing w:before="0" w:after="60"/></w:pPr></w:p>'
)

# Bảng đánh giá
rows = [(m[0], m[1], m[2], m[3], m[4]) for m in MEMBERS]
appendix_b += build_table(HEADER, rows)

# Ghi chú cuối
appendix_b += (
    f'<w:p w14:paraId="{pid()}" w14:textId="77777777" '
    f'w:rsidR="00000000" w:rsidRDefault="00000000">'
    f'<w:pPr><w:spacing w:before="60" w:after="0"/></w:pPr></w:p>'
)
appendix_b += para(
    "(*) Tỉ lệ đóng góp trên được tất cả thành viên xem xét và thống nhất vào "
    "cuối tuần 12 của dự án. Mọi thành viên đều hoàn thành đầy đủ phần việc "
    "được giao với mức độ hoàn thành 100%.",
    bold=False
)

# ── Chèn vào XML ──────────────────────────────────────────────────────────────
# Chèn trước paragraph cuối cùng (blank paragraph chứa sectPr tiếp theo)
# Mục đích: giữ nguyên sectPr (lề, kích thước trang) của document gốc.
# Rủi ro: nếu rfind('<w:p ') không tìm thấy đúng vị trí, sectPr bị bao vào body.
sect_pos = xml.rfind('<w:sectPr')
last_p_pos = xml.rfind('<w:p ', 0, sect_pos)
assert last_p_pos >= 0, "Không tìm thấy paragraph cuối cùng trước sectPr"

xml = xml[:last_p_pos] + appendix_b + xml[last_p_pos:]

# ── Ghi output ────────────────────────────────────────────────────────────────
files["word/document.xml"] = xml.encode("utf-8")
with zipfile.ZipFile(DST, "w", zipfile.ZIP_DEFLATED) as zout:
    for name, data in files.items():
        zout.writestr(name, data)

size = os.path.getsize(DST)
print(f"\n✓ Done → {DST}  ({size:,} bytes)")

# ── Sanity checks ─────────────────────────────────────────────────────────────
with zipfile.ZipFile(DST) as z:
    xml_out = z.read("word/document.xml").decode("utf-8")

checks = [
    "PHỤ LỤC B",
    "ĐÁNH GIÁ ĐÓNG GÓP",
    "Kiệt",
    "22%",
    "Lộc",
    "19%",
    "Vai trò & Nhiệm vụ chính",
    "DEEBF6",
]
for c in checks:
    assert c in xml_out, f"SANITY FAIL: {c!r} not in output"
    print(f"  ✓ {c!r}")

print("All sanity checks passed.")
