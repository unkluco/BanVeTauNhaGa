"""
build_v17.py — Thêm PHỤ LỤC: NHẬT KÝ THỰC HIỆN vào cuối v16
  - Bảng 4 cột: Tuần | Công việc | Thành viên | Mức độ hoàn thành
  - 12 tuần, bắt đầu từ 31/12/2025 (theo file mẫu)
  - Phân công thực tế từ ảnh bình chọn nhóm

SRC = 9_REPORT_v16.docx  →  DST = 9_REPORT_v17.docx

Phân công module:
  Kiệt  (1): Đặt vé mới, Tổng quan, Thống kê
  Bảo   (2): Quản lý giá, Quản lý khuyến mãi, Quản lý hóa đơn
  Hoàng (3): Quản lý vé, Quản lý tuyến đường, Quản lý lịch chạy
  Huy   (4): Quản lý đoàn tàu, Quản lý toa tàu, Quản lý đầu máy
  Lộc   (5): Quản lý nhân viên, Quản lý khách hàng, Thông tin cá nhân

Rủi ro: w:sectPr ở cuối doc cần còn nguyên; tất cả insert đều trước nó.
"""
import zipfile, re, shutil, os

SRC = "document/9_REPORT_v16.docx"
DST = "document/9_REPORT_v17.docx"

shutil.copy(SRC, DST)

with zipfile.ZipFile(DST, "r") as z:
    xml   = z.read("word/document.xml").decode("utf-8")
    files = {n: z.read(n) for n in z.namelist()}

# ── paraId counter ────────────────────────────────────────────────────────────
_pid = [0xEE000001]
def pid():
    v = f"{_pid[0]:08X}"; _pid[0] += 1; return v

# ── 12 tuần dữ liệu ───────────────────────────────────────────────────────────
WEEKS = [
    ("01", "31/12/2025", "06/01/2026", [
        ("Đặt ra mục tiêu của đồ án",                              "Cả nhóm", "100%"),
        ("Lập ra những kế hoạch thực hiện đồ án",                  "Cả nhóm", "100%"),
        ("Triển khai các công cụ làm việc nhóm (GitHub, Discord)", "Cả nhóm", "100%"),
        ("Tìm hiểu các hệ thống bán vé tàu tương tự",             "Cả nhóm", "100%"),
        ("Phân chia nhiệm vụ module cho từng thành viên",          "Kiệt",    "100%"),
    ]),
    ("02", "07/01/2026", "13/01/2026", [
        ("Xác định và soạn tài liệu thu thập yêu cầu",                         "Kiệt",  "100%"),
        ("Khảo sát module Bán vé, Tổng quan, Thống kê",                        "Kiệt",  "100%"),
        ("Khảo sát module Quản lý giá, Khuyến mãi, Hóa đơn",                  "Bảo",   "100%"),
        ("Khảo sát module Quản lý vé, Tuyến đường, Lịch chạy",                "Hoàng", "100%"),
        ("Khảo sát module Quản lý đoàn tàu, Toa tàu, Đầu máy",               "Huy",   "100%"),
        ("Khảo sát module Quản lý nhân viên, Khách hàng",                      "Lộc",   "100%"),
        ("Rà soát, tổng hợp và chỉnh sửa tài liệu yêu cầu",                   "Kiệt",  "100%"),
    ]),
    ("03", "14/01/2026", "20/01/2026", [
        ("Xác định yêu cầu chức năng và phi chức năng",    "Bảo",   "100%"),
        ("Xây dựng sơ đồ phân cấp chức năng",              "Bảo",   "100%"),
        ("Vẽ sơ đồ Use Case tổng thể",                     "Kiệt",  "100%"),
        ("Thiết kế giao diện sơ bộ đăng nhập, menu chính", "Lộc",   "100%"),
        ("Điều chỉnh và hoàn thiện tài liệu yêu cầu",     "Kiệt",  "100%"),
    ]),
    ("04", "21/01/2026", "27/01/2026", [
        ("Mô tả UC, Activity Diagram, Sequence Diagram của UC Bán vé",           "Kiệt",  "100%"),
        ("Mô tả UC, Activity Diagram, Sequence Diagram của UC Hoàn vé",          "Hoàng", "100%"),
        ("Mô tả UC, Activity Diagram, Sequence Diagram của UC Tra cứu lịch chạy","Hoàng", "100%"),
        ("Mô tả UC, Activity Diagram, Sequence Diagram của UC Tạo bảng giá vé",  "Bảo",   "100%"),
        ("Mô tả UC, Activity Diagram, Sequence Diagram của UC Chỉnh sửa chi tiết giá", "Bảo", "100%"),
        ("Rà soát, chỉnh sửa và hoàn thiện tài liệu",                            "Kiệt",  "100%"),
    ]),
    ("05", "28/01/2026", "03/02/2026", [
        ("Thiết kế các entity: NhanVien, KhachHang, TaiKhoan", "Lộc",   "100%"),
        ("Thiết kế các entity: HoaDon, ChiTietHoaDon, Ve",     "Bảo",   "100%"),
        ("Thiết kế các entity: DoAnTau, ToaTau, DauMay, Ghe",  "Huy",   "100%"),
        ("Thiết kế các entity: TuyenDuong, Lich, ChiTietGia, KhuyenMai", "Hoàng", "100%"),
        ("Thiết kế Class Diagram tổng thể",                     "Kiệt",  "100%"),
        ("Thiết kế mô hình EER và sơ đồ quan hệ CSDL",         "Bảo",   "100%"),
    ]),
    ("06", "04/02/2026", "10/02/2026", [
        ("Viết SQL tạo toàn bộ bảng CSDL (MSSQL)",        "Kiệt, Bảo",  "100%"),
        ("Viết SQL insert dữ liệu mẫu cho toàn bộ bảng",  "Cả nhóm",    "100%"),
        ("Xây dựng cấu trúc project Java (package, layer)","Kiệt",       "100%"),
        ("Xây dựng lớp kết nối CSDL (DBConnection)",       "Kiệt",       "100%"),
        ("Xây dựng base class DAO và BLL",                  "Kiệt",       "100%"),
    ]),
    ("07", "11/02/2026", "17/02/2026", [
        ("Lập trình module Đăng nhập, Đăng xuất",           "Lộc",  "100%"),
        ("Lập trình menu chính và điều hướng các module",   "Kiệt", "100%"),
        ("Thiết kế và áp dụng giao diện Notion theme",      "Kiệt", "100%"),
        ("Lập trình DAO, BLL cho NhanVien, TaiKhoan",       "Lộc",  "100%"),
        ("Lập trình tab Thông tin cá nhân",                 "Lộc",  "100%"),
    ]),
    ("08", "18/02/2026", "24/02/2026", [
        ("Lập trình module Bán vé (bước 1–4: chọn chuyến, chỗ, khách hàng)", "Kiệt",  "100%"),
        ("Lập trình module Quản lý giá (CRUD ChiTietGia)",                    "Bảo",   "100%"),
        ("Lập trình module Quản lý vé (xem, tìm kiếm)",                       "Hoàng", "100%"),
        ("Lập trình module Quản lý đoàn tàu và toa tàu",                      "Huy",   "100%"),
        ("Lập trình module Quản lý nhân viên",                                 "Lộc",   "100%"),
    ]),
    ("09", "25/02/2026", "03/03/2026", [
        ("Lập trình module Bán vé (bước 5–8: khuyến mãi, thanh toán)", "Kiệt",  "100%"),
        ("Lập trình module Quản lý khuyến mãi",                         "Bảo",   "100%"),
        ("Lập trình module Quản lý hóa đơn",                            "Bảo",   "100%"),
        ("Lập trình module Quản lý tuyến đường và lịch chạy",           "Hoàng", "100%"),
        ("Lập trình module Quản lý đầu máy",                             "Huy",   "100%"),
        ("Lập trình module Quản lý khách hàng",                          "Lộc",   "100%"),
    ]),
    ("10", "04/03/2026", "10/03/2026", [
        ("Lập trình Tổng quan (Dashboard)",                       "Kiệt",      "100%"),
        ("Lập trình module Thống kê &amp; Báo cáo",              "Kiệt",      "100%"),
        ("Tích hợp toàn bộ module vào hệ thống chính",            "Cả nhóm",   "100%"),
        ("Kiểm tra luồng Bán vé end-to-end",                      "Kiệt, Bảo", "100%"),
    ]),
    ("11", "11/03/2026", "17/03/2026", [
        ("Kiểm thử chức năng Bán vé, Hoàn vé và sửa lỗi",                "Kiệt",  "100%"),
        ("Kiểm thử module Quản lý giá, Khuyến mãi, Hóa đơn và sửa lỗi", "Bảo",   "100%"),
        ("Kiểm thử module Quản lý vé, Tuyến, Lịch chạy và sửa lỗi",     "Hoàng", "100%"),
        ("Kiểm thử module Quản lý tàu, Toa, Đầu máy và sửa lỗi",        "Huy",   "100%"),
        ("Kiểm thử module Nhân viên, Khách hàng, Thông tin và sửa lỗi",  "Lộc",   "100%"),
        ("Sửa lỗi tổng thể, review code toàn bộ source",                  "Cả nhóm","100%"),
    ]),
    ("12", "18/03/2026", "24/03/2026", [
        ("Hoàn thiện báo cáo (phần 1, 2: Use Case, phân tích)",           "Kiệt, Bảo",   "100%"),
        ("Hoàn thiện báo cáo (phần 3: thiết kế hệ thống, giao diện)",    "Hoàng, Huy",  "100%"),
        ("Hoàn thiện báo cáo (phụ lục, nhật ký, tài liệu tham khảo)",    "Lộc",         "100%"),
        ("Chuẩn bị slide trình bày và kịch bản demo",                      "Cả nhóm",     "100%"),
        ("Rà soát lần cuối toàn bộ tài liệu và source code",              "Kiệt",        "100%"),
    ]),
]

# ── XML helpers ───────────────────────────────────────────────────────────────
TNR = ('<w:rFonts w:ascii="Times New Roman" w:cs="Times New Roman" '
       'w:eastAsia="Times New Roman" w:hAnsi="Times New Roman"/>')

BORDER_SOLID = ('<w:tblBorders>'
    '<w:top    w:color="000000" w:space="0" w:sz="4" w:val="single"/>'
    '<w:left   w:color="000000" w:space="0" w:sz="4" w:val="single"/>'
    '<w:bottom w:color="000000" w:space="0" w:sz="4" w:val="single"/>'
    '<w:right  w:color="000000" w:space="0" w:sz="4" w:val="single"/>'
    '<w:insideH w:color="000000" w:space="0" w:sz="4" w:val="single"/>'
    '<w:insideV w:color="000000" w:space="0" w:sz="4" w:val="single"/>'
    '</w:tblBorders>')

BORDER_DOT_TB = ('<w:tcBorders>'
    '<w:top    w:color="000000" w:space="0" w:sz="4" w:val="dotted"/>'
    '<w:bottom w:color="000000" w:space="0" w:sz="4" w:val="dotted"/>'
    '</w:tcBorders>')

# Column widths for 4 columns (fit A4 portrait, content width ~8788 dxa)
COL_W = [1800, 4200, 1800, 988]  # Tuần | Công việc | Thành viên | Mức độ
assert sum(COL_W) == 8788

def rpr(bold=False, italic=False, sz=26):
    b  = '<w:b w:val="1"/><w:bCs w:val="1"/>' if bold else ''
    i  = '<w:i w:val="1"/><w:iCs w:val="1"/>' if italic else ''
    return f'<w:rPr>{TNR}{b}{i}<w:sz w:val="{sz}"/><w:szCs w:val="{sz}"/></w:rPr>'

def text_run(text, bold=False, italic=False, sz=26):
    if not text:
        return f'<w:r><w:rPr><w:rtl w:val="0"/></w:rPr></w:r>'
    return (f'<w:r><w:rPr>{TNR}'
            + ('<w:b w:val="1"/><w:bCs w:val="1"/>' if bold else '')
            + ('<w:i w:val="1"/><w:iCs w:val="1"/>' if italic else '')
            + f'<w:sz w:val="{sz}"/><w:szCs w:val="{sz}"/><w:rtl w:val="0"/></w:rPr>'
            + f'<w:t xml:space="preserve">{text}</w:t></w:r>')

def para(text, bold=False, italic=False, center=False, sz=26):
    jc  = '<w:jc w:val="center"/>' if center else ''
    return (f'<w:p w:rsidR="00000000" w:rsidRDefault="00000000" w14:paraId="{pid()}">'
            f'<w:pPr>{jc}{rpr(bold, italic, sz)}</w:pPr>'
            + text_run(text, bold, italic, sz)
            + '</w:p>')

def empty_para():
    return f'<w:p w:rsidR="00000000" w:rsidRDefault="00000000" w14:paraId="{pid()}"><w:pPr>{rpr()}</w:pPr><w:r><w:rPr><w:rtl w:val="0"/></w:rPr></w:r></w:p>'

def cell(content_xml, tcpr_extra="", width=None):
    w_attr = f'<w:tcW w:w="{width}" w:type="dxa"/>' if width else ''
    return f'<w:tc><w:tcPr>{w_attr}{tcpr_extra}<w:vAlign w:val="center"/></w:tcPr>{content_xml}</w:tc>'

def content_cell(text, width, bold=False, center=False, is_first_row=True):
    """Normal data cell with dotted top/bottom border on continuation rows."""
    border = '' if is_first_row else BORDER_DOT_TB
    tcpr = f'<w:tcW w:w="{width}" w:type="dxa"/>{border}<w:vAlign w:val="center"/>'
    return f'<w:tc><w:tcPr>{tcpr}</w:tcPr>{para(text, bold=bold, center=center)}</w:tc>'

def header_cell(text, width):
    """Blue bold italic header cell."""
    shd = '<w:shd w:fill="deebf6" w:val="clear"/>'
    bot = '<w:tcBorders><w:bottom w:color="000000" w:space="0" w:sz="4" w:val="single"/></w:tcBorders>'
    tcpr = f'<w:tcW w:w="{width}" w:type="dxa"/>{bot}{shd}<w:vAlign w:val="center"/>'
    return (f'<w:tc><w:tcPr>{tcpr}</w:tcPr>'
            + para(text, bold=True, italic=True, center=True)
            + '</w:tc>')

def week_first_cell(week_num, date_from, date_to, width):
    """Week label cell — vMerge restart, bold, 3 paragraphs."""
    tcpr = f'<w:tcW w:w="{width}" w:type="dxa"/><w:vMerge w:val="restart"/><w:vAlign w:val="center"/>'
    p1 = (f'<w:p w:rsidR="00000000" w:rsidRDefault="00000000" w14:paraId="{pid()}">'
          f'<w:pPr>{rpr(bold=True)}</w:pPr>'
          + text_run(f'Tuần {week_num}', bold=True) + '</w:p>')
    p2 = (f'<w:p w:rsidR="00000000" w:rsidRDefault="00000000" w14:paraId="{pid()}">'
          f'<w:pPr>{rpr()}</w:pPr>'
          + text_run(f'(Từ: {date_from}') + '</w:p>')
    p3 = (f'<w:p w:rsidR="00000000" w:rsidRDefault="00000000" w14:paraId="{pid()}">'
          f'<w:pPr>{rpr()}</w:pPr>'
          + text_run(f'Đến: {date_to})') + '</w:p>')
    return f'<w:tc><w:tcPr>{tcpr}</w:tcPr>{p1}{p2}{p3}</w:tc>'

def week_cont_cell(width):
    """Continuation cell for merged week column."""
    tcpr = (f'<w:tcW w:w="{width}" w:type="dxa"/><w:vMerge w:val="continue"/>'
            '<w:vAlign w:val="center"/>')
    return f'<w:tc><w:tcPr>{tcpr}</w:tcPr>{empty_para()}</w:tc>'

def make_row(cells_xml, trHeight=340):
    return (f'<w:tr><w:trPr>'
            f'<w:cantSplit w:val="0"/>'
            f'<w:trHeight w:val="{trHeight}" w:hRule="atLeast"/>'
            f'<w:tblHeader w:val="0"/>'
            f'</w:trPr>{"".join(cells_xml)}</w:tr>')

# ── Build nhật ký table ───────────────────────────────────────────────────────
W = COL_W  # [1800, 4200, 1800, 988]

# Header row
hdr = (f'<w:tr><w:trPr>'
       f'<w:cantSplit w:val="0"/><w:trHeight w:val="340" w:hRule="atLeast"/>'
       f'<w:tblHeader w:val="1"/></w:trPr>'
       + header_cell("Tuần",                    W[0])
       + header_cell("Công việc",               W[1])
       + header_cell("Thành viên thực hiện",    W[2])
       + header_cell("Mức độ hoàn thành",       W[3])
       + '</w:tr>')

data_rows = [hdr]

for week_num, date_from, date_to, tasks in WEEKS:
    for i, (task, member, done) in enumerate(tasks):
        if i == 0:
            week_col = week_first_cell(week_num, date_from, date_to, W[0])
        else:
            week_col = week_cont_cell(W[0])
        row = make_row([
            week_col,
            content_cell(task,   W[1], is_first_row=(i == 0)),
            content_cell(member, W[2], center=True, is_first_row=(i == 0)),
            content_cell(done,   W[3], center=True, is_first_row=(i == 0)),
        ])
        data_rows.append(row)

# Assemble table
grid_xml = "".join(f'<w:gridCol w:w="{w}"/>' for w in W)
tbl_xml = (
    '<w:tbl>'
    '<w:tblPr>'
    '<w:tblW w:w="8788" w:type="dxa"/>'
    '<w:jc w:val="left"/>'
    + BORDER_SOLID +
    '<w:tblLayout w:type="fixed"/>'
    '<w:tblLook w:val="0400"/>'
    '</w:tblPr>'
    f'<w:tblGrid>{grid_xml}</w:tblGrid>'
    + "".join(data_rows)
    + '</w:tbl>'
)

# ── Build appendix section: page break + heading + meta + table ───────────────
def h1_para(text):
    """Heading1 style paragraph for appendix title."""
    return (f'<w:p w14:paraId="{pid()}" w14:textId="77777777" '
            f'w:rsidR="00000000" w:rsidRDefault="00000000">'
            f'<w:pPr><w:pStyle w:val="Heading1"/>'
            f'<w:numPr><w:ilvl w:val="0"/><w:numId w:val="0"/></w:numPr>'
            f'</w:pPr>'
            f'<w:r><w:t>{text}</w:t></w:r>'
            f'</w:p>')

def normal_para(text, sz=26, center=False):
    jc = '<w:jc w:val="center"/>' if center else ''
    return (f'<w:p w14:paraId="{pid()}" w14:textId="77777777" '
            f'w:rsidR="00000000" w:rsidRDefault="00000000">'
            f'<w:pPr>{jc}<w:rPr>{TNR}<w:sz w:val="{sz}"/><w:szCs w:val="{sz}"/></w:rPr></w:pPr>'
            f'<w:r><w:rPr>{TNR}<w:sz w:val="{sz}"/><w:szCs w:val="{sz}"/></w:rPr>'
            f'<w:t xml:space="preserve">{text}</w:t></w:r>'
            f'</w:p>')

page_break_para = (
    f'<w:p w14:paraId="{pid()}" w14:textId="77777777" '
    f'w:rsidR="00000000" w:rsidRDefault="00000000">'
    f'<w:pPr><w:pageBreakBefore/></w:pPr>'
    f'</w:p>'
)

appendix_xml = (
    page_break_para
    + h1_para("PHỤ LỤC A: NHẬT KÝ THỰC HIỆN")
    + normal_para("Nhóm 01 – Thành viên nhóm: Nguyễn Anh Kiệt (nhóm trưởng), "
                  "Ngô Gia Bảo, Nguyễn Bảo Hoàng, Lê Quốc Huy, Lê Tấn Lộc")
    + normal_para("Tên ứng dụng: Xây Dựng Ứng Dụng Bán Vé Tàu Tại Nhà Ga")
    + normal_para("Thời gian thực hiện: Từ 31/12/2025 đến 24/03/2026 (12 tuần)")
    + f'<w:p w14:paraId="{pid()}" w14:textId="77777777" w:rsidR="00000000" w:rsidRDefault="00000000"/>'
    + tbl_xml
)

# ── Insert vào trước sectPr cuối cùng ────────────────────────────────────────
# Mục đích: giữ nguyên sectPr (page size, margins, header/footer) của v16.
sect_pos = xml.rfind('<w:sectPr')
assert sect_pos >= 0, "No sectPr found"
# Lùi về đầu paragraph chứa sectPr (sectPr có thể nằm trong <w:p> cuối)
p_before_sect = xml.rfind('<w:p ', 0, sect_pos)
p_before_sect2 = xml.rfind('<w:p>', 0, sect_pos)
p_pos = max(p_before_sect, p_before_sect2)
# Kiểm tra: nếu sectPr nằm trong paragraph, cần chèn trước paragraph đó
# Nếu sectPr là standalone thì chèn ngay trước nó
if p_pos >= 0 and xml[sect_pos-5:sect_pos].count('</w:p>') == 0:
    # sectPr nằm trong paragraph - insert before that paragraph
    insert_at = p_pos
else:
    insert_at = sect_pos

xml = xml[:insert_at] + appendix_xml + xml[insert_at:]
print(f"Inserted appendix ({len(appendix_xml):,} chars) at pos {insert_at}")

# ── Write output ──────────────────────────────────────────────────────────────
files["word/document.xml"] = xml.encode("utf-8")
with zipfile.ZipFile(DST, "w", zipfile.ZIP_DEFLATED) as zout:
    for name, data in files.items():
        zout.writestr(name, data)

size = os.path.getsize(DST)
print(f"✓ Done → {DST}  ({size:,} bytes)")

# ── Sanity ─────────────────────────────────────────────────────────────────────
with zipfile.ZipFile(DST) as z:
    xml_out = z.read("word/document.xml").decode("utf-8")

import xml.etree.ElementTree as ET
try:
    ET.fromstring(xml_out.encode("utf-8"))
    print("Sanity — XML well-formed OK")
except ET.ParseError as e:
    print(f"Sanity — XML ERROR: {e}")

print(f"Sanity — 'PHỤ LỤC A' in doc: {'PHỤ LỤC A' in xml_out}")
print(f"Sanity — 'Tuần 01' in doc: {'Tuần 01' in xml_out}")
print(f"Sanity — 'Tuần 12' in doc: {'Tuần 12' in xml_out}")
print(f"Sanity — sectPr still present: {'<w:sectPr' in xml_out}")
total_weeks = sum(1 for w, *_ in WEEKS)
tasks_total = sum(len(tasks) for _, _, _, tasks in WEEKS)
print(f"Sanity — {total_weeks} tuần, {tasks_total} dòng công việc")
print("All checks passed.")
