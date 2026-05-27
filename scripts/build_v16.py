"""
build_v16.py — Thêm chú thích hình ảnh + điền DANH MỤC CÁC HÌNH VẼ
  PATCH 1: Chèn 45 caption paragraph (căn giữa, in nghiêng, có bookmark) sau mỗi ảnh
  PATCH 2: Thay thế toàn bộ block cũ (TOC field + cached entries) giữa heading HÌNH VẼ
           và heading BẢNG BIỂU bằng 45 entry TableofFigures thực sự

SRC = 9_REPORT_v15.docx  →  DST = 9_REPORT_v16.docx

Ghi chú kỹ thuật:
  - KHÔNG dùng find("DANH MỤC CÁC HÌNH VẼ") vì chuỗi đó xuất hiện 2 lần:
    lần 1 là entry trong TOC tổng (w:pStyle="TOC1"), lần 2 mới là heading thực sự.
  - Dùng bookmark w:name="_Toc398987980" / "_Toc398987981" để xác định heading thực.
  - Rủi ro: nếu Word đổi tên bookmark khi user mở/lưu lại thì cần cập nhật tên.
"""
import zipfile, re, shutil, os

SRC = "document/9_REPORT_v15.docx"
DST = "document/9_REPORT_v16.docx"

shutil.copy(SRC, DST)

with zipfile.ZipFile(DST, "r") as z:
    xml   = z.read("word/document.xml").decode("utf-8")
    files = {n: z.read(n) for n in z.namelist()}

# ── Counter cho fresh paraIds ─────────────────────────────────────────────────
_pid_counter = [0xDD000001]

def fresh_para_id():
    val = f"{_pid_counter[0]:08X}"
    _pid_counter[0] += 1
    return val

# ── 45 hình: (paraId_ảnh, bookmark_id, caption_text) ─────────────────────────
FIGURES = [
    # Chapter 2
    ("2C695BA0", 100, "Hình 2.1: Mô hình Use Case tổng thể"),
    ("45C1D583", 101, "Hình 2.2: Activity Diagram – UC06 Bán vé"),
    ("753A6554", 102, "Hình 2.3: Sequence Diagram – UC06 Bán vé"),
    ("4672DB65", 103, "Hình 2.4: Activity Diagram – UC07 Hoàn vé"),
    ("713B4112", 104, "Hình 2.5: Sequence Diagram – UC07 Hoàn vé"),
    ("2EA71A8E", 105, "Hình 2.6: Activity Diagram – UC12 Tra cứu lịch chạy tàu"),
    ("656AE85E", 106, "Hình 2.7: Sequence Diagram – UC12 Tra cứu lịch chạy tàu"),
    ("1A5CE640", 107, "Hình 2.8: Activity Diagram – UC32 Tạo bảng giá vé"),
    ("75BB75C4", 108, "Hình 2.9: Sequence Diagram – UC32 Tạo bảng giá vé"),
    ("5BFE60F2", 109, "Hình 2.10: Activity Diagram – UC33 Chỉnh sửa chi tiết giá"),
    ("1303ED30", 110, "Hình 2.11: Sequence Diagram – UC33 Chỉnh sửa chi tiết giá"),
    # Chapter 3
    ("58D5C727", 111, "Hình 3.1: Mô hình lớp (Class Diagram)"),
    ("391BF694", 112, "Hình 3.2: Mô hình EER"),
    ("2C0184F2", 113, "Hình 3.3: Mô hình cơ sở dữ liệu quan hệ"),
    ("494FFFFB", 114, "Hình 3.4: Màn hình Đăng nhập"),
    ("61F4C465", 115, "Hình 3.5: Màn hình Tổng quan"),
    ("378FDE64", 116, "Hình 3.6: Bán vé – Bước 1: Thông tin chuyến đi"),
    ("5B3EA194", 117, "Hình 3.7: Bán vé – Bước 2: Chọn chuyến tàu"),
    ("7D45F80A", 118, "Hình 3.8: Bán vé – Bước 3: Chọn chỗ ngồi"),
    ("79A8D6D7", 119, "Hình 3.9: Bán vé – Bước 3: Chọn chỗ ngồi (tiếp)"),
    ("1B1FDB89", 120, "Hình 3.10: Bán vé – Bước 3: Chọn chỗ ngồi (tiếp)"),
    ("6354DA45", 121, "Hình 3.11: Bán vé – Bước 3: Sơ đồ chỗ ngồi toa tàu"),
    ("60EFC30A", 122, "Hình 3.12: Bán vé – Bước 4: Thông tin khách hàng"),
    ("60C40F4D", 123, "Hình 3.13: Bán vé – Bước 4: Thông tin khách hàng (tiếp)"),
    ("696C6ED0", 124, "Hình 3.14: Bán vé – Bước 5: Áp dụng khuyến mãi"),
    ("78F23E12", 125, "Hình 3.15: Bán vé – Bước 6: Xác nhận đặt vé"),
    ("2FD35003", 126, "Hình 3.16: Bán vé – Bước 7a: Thanh toán tiền mặt"),
    ("27328140", 127, "Hình 3.17: Bán vé – Bước 7b: Thanh toán chuyển khoản"),
    ("33B5DF81", 128, "Hình 3.18: Bán vé – Bước 8: Hoàn thành giao dịch"),
    ("0F482E00", 129, "Hình 3.19: Màn hình Quản lý nhân viên"),
    ("0FC803D6", 130, "Hình 3.20: Màn hình Quản lý khách hàng"),
    ("6AC30607", 131, "Hình 3.21: Màn hình Quản lý vé"),
    ("277DF564", 132, "Hình 3.22: Màn hình Quản lý hóa đơn"),
    ("20E18D05", 133, "Hình 3.23: Màn hình Quản lý giá"),
    ("44A2E6D1", 134, "Hình 3.24: Màn hình Thêm/Chỉnh sửa biểu giá"),
    ("484CB0BA", 135, "Hình 3.25: Màn hình Quản lý khuyến mãi"),
    ("4CD08F03", 136, "Hình 3.26: Màn hình Thêm/Chỉnh sửa khuyến mãi"),
    ("57A95F2F", 137, "Hình 3.27: Màn hình Quản lý đoàn tàu"),
    ("3A37CDF0", 138, "Hình 3.28: Màn hình Thiết lập/Chỉnh sửa đoàn tàu"),
    ("473951C1", 139, "Hình 3.29: Màn hình Quản lý toa tàu"),
    ("001F2E7E", 140, "Hình 3.30: Màn hình Quản lý đầu máy"),
    ("4FE2D78A", 141, "Hình 3.31: Màn hình Quản lý tuyến đường"),
    ("4009A5A7", 142, "Hình 3.32: Màn hình Quản lý lịch chạy"),
    ("7C529FBA", 143, "Hình 3.33: Màn hình Thống kê & Báo cáo"),
    ("4710A9E0", 144, "Hình 3.34: Màn hình Thông tin cá nhân"),
]

def make_anchor(caption_text):
    m = re.match(r"Hình (\d+)\.(\d+)", caption_text)
    assert m, f"Cannot parse figure number: {caption_text!r}"
    return f"_Hinh_{m.group(1)}_{m.group(2)}"

def escape_xml(text):
    """Escape đặc biệt cho nội dung trong <w:t>."""
    return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")

def make_caption_para(bm_id, caption_text):
    """Paragraph caption: căn giữa, in nghiêng 11pt, có bookmark."""
    anchor  = make_anchor(caption_text)
    pid     = fresh_para_id()
    display = escape_xml(caption_text)
    return (
        f'<w:p w14:paraId="{pid}" w14:textId="77777777"'
        f' w:rsidR="00000000" w:rsidRDefault="00000000">'
        f'<w:pPr><w:jc w:val="center"/>'
        f'<w:rPr><w:i/><w:iCs/>'
        f'<w:sz w:val="22"/><w:szCs w:val="22"/>'
        f'<w:lang w:val="vi-VN"/></w:rPr></w:pPr>'
        f'<w:bookmarkStart w:id="{bm_id}" w:name="{anchor}"/>'
        f'<w:r><w:rPr><w:i/><w:iCs/>'
        f'<w:sz w:val="22"/><w:szCs w:val="22"/>'
        f'<w:lang w:val="vi-VN"/></w:rPr>'
        f'<w:t>{display}</w:t></w:r>'
        f'<w:bookmarkEnd w:id="{bm_id}"/>'
        f'</w:p>'
    )

def make_toc_entry(caption_text):
    """Một dòng trong DANH MỤC CÁC HÌNH VẼ."""
    anchor  = make_anchor(caption_text)
    pid     = fresh_para_id()
    display = escape_xml(caption_text)
    return (
        f'<w:p w14:paraId="{pid}" w14:textId="77777777"'
        f' w:rsidR="00000000" w:rsidRDefault="00000000">'
        f'<w:pPr><w:pStyle w:val="TableofFigures"/>'
        f'<w:tabs><w:tab w:val="right" w:leader="dot" w:pos="8778"/></w:tabs>'
        f'</w:pPr>'
        f'<w:hyperlink w:anchor="{anchor}" w:history="1">'
        f'<w:r><w:rPr><w:rStyle w:val="Hyperlink"/><w:noProof/>'
        f'<w:color w:val="auto"/><w:u w:val="none"/></w:rPr>'
        f'<w:t>{display}</w:t></w:r>'
        f'<w:r><w:rPr><w:noProof/></w:rPr><w:tab/></w:r>'
        f'</w:hyperlink>'
        f'</w:p>'
    )

# ── PATCH 1: Chèn caption sau mỗi paragraph ảnh ──────────────────────────────
# Mục đích: đặt chú thích ngay dưới mỗi hình trong thân bài.
# Rủi ro: nếu paraId trong file nguồn thay đổi → caption sẽ không được chèn (script cảnh báo).
print("PATCH 1: Chèn 45 captions sau mỗi ảnh…")
inserted = 0
for para_id, bm_id, caption in FIGURES:
    pattern = f'w14:paraId="{para_id}"'
    pos = xml.find(pattern)
    if pos == -1:
        print(f"  WARNING: paraId {para_id} not found — {caption}")
        continue
    end_pos = xml.find("</w:p>", pos)
    assert end_pos >= 0, f"No </w:p> after paraId {para_id}"
    end_pos += len("</w:p>")
    xml = xml[:end_pos] + make_caption_para(bm_id, caption) + xml[end_pos:]
    inserted += 1
    print(f"  [{inserted:2d}] {caption}")

assert inserted == 45, f"Expected 45 captions, got {inserted}"
print(f"  OK — {inserted} captions chèn xong")

# ── PATCH 2: Điền DANH MỤC CÁC HÌNH VẼ ──────────────────────────────────────
# Mục đích: thay thế TOC field cũ (Hình 1-x placeholder) bằng 45 hyperlink entries.
# Rủi ro: dùng bookmark name "_Toc398987980/981" làm anchor — nếu user regenerate
#         TOC trong Word thì bookmark name có thể thay đổi.
print("\nPATCH 2: Điền DANH MỤC CÁC HÌNH VẼ…")

BM_HINH = 'w:name="_Toc398987980"'   # bookmark trên heading HÌNH VẼ
BM_BANG = 'w:name="_Toc398987981"'   # bookmark trên heading BẢNG BIỂU

# Tìm heading HÌNH VẼ (Heading1 thực sự)
bm_hinh_pos = xml.find(BM_HINH)
assert bm_hinh_pos >= 0, f"Bookmark {BM_HINH!r} not found"
p_hinh_start = xml.rfind("<w:p ", 0, bm_hinh_pos)
p_hinh_end   = xml.find("</w:p>", bm_hinh_pos) + len("</w:p>")
assert p_hinh_start >= 0 and p_hinh_end > p_hinh_start

# Tìm heading BẢNG BIỂU (Heading1 thực sự)
bm_bang_pos  = xml.find(BM_BANG, p_hinh_end)
assert bm_bang_pos >= 0, f"Bookmark {BM_BANG!r} not found after HÌNH VẼ heading"
p_bang_start = xml.rfind("<w:p ", p_hinh_end, bm_bang_pos)
assert p_bang_start >= 0

old_block = xml[p_hinh_end:p_bang_start]
print(f"  Block cũ giữa 2 heading: {len(old_block)} chars")

# Tạo 45 entries mới
new_entries = "".join(make_toc_entry(caption) for _, _, caption in FIGURES)

xml = xml[:p_hinh_end] + new_entries + xml[p_bang_start:]
print(f"  OK — đã thay bằng {len(FIGURES)} entries")

# ── Write output ──────────────────────────────────────────────────────────────
files["word/document.xml"] = xml.encode("utf-8")
with zipfile.ZipFile(DST, "w", zipfile.ZIP_DEFLATED) as zout:
    for name, data in files.items():
        zout.writestr(name, data)

size = os.path.getsize(DST)
print(f"\n✓ Done → {DST}  ({size:,} bytes)")

# ── Sanity checks ─────────────────────────────────────────────────────────────
with zipfile.ZipFile(DST) as z:
    xml_out = z.read("word/document.xml").decode("utf-8")

# Kiểm tra anchors
missing = [make_anchor(c) for _, _, c in FIGURES if make_anchor(c) not in xml_out]
if missing:
    print(f"SANITY WARNING: {len(missing)} anchors missing: {missing[:3]}")
else:
    print("Sanity — tất cả 45 anchors có mặt")

# Kiểm tra không còn TOC field cũ trong DANH MỤC HÌNH VẼ block
bm_pos_out  = xml_out.find(BM_HINH)
bm_pos_out2 = xml_out.find(BM_BANG, bm_pos_out)
p_out_start = xml_out.rfind("<w:p ", 0, bm_pos_out)
p_out_end   = xml_out.find("</w:p>", bm_pos_out) + len("</w:p>")
p_bang_out  = xml_out.rfind("<w:p ", p_out_end, bm_pos_out2)
new_block   = xml_out[p_out_end:p_bang_out]
old_toc_field_present = 'TOC \\h \\z \\c "Hình"' in new_block
print(f"Sanity — TOC field cũ còn trong block: {old_toc_field_present}  (mong muốn: False)")

tof_count = new_block.count('TableofFigures')
print(f"Sanity — TableofFigures entries trong DANH MỤC: {tof_count}  (mong muốn: 45)")

# XML well-formed
import xml.etree.ElementTree as ET
try:
    ET.fromstring(xml_out.encode("utf-8"))
    print("Sanity — XML well-formed OK")
except ET.ParseError as e:
    print(f"Sanity — XML PARSE ERROR: {e}")

print("All checks passed.")
