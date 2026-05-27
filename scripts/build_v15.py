"""
build_v15.py — vá 4 điểm lệch doc ↔ Java/SQL
  A. 3.1.2.16 ChiTietHoaDon: thêm field khachHang (row 1.4) + renumber 1.4→1.5, 1.5→1.6
  B. 3.1.2.19 KhuyenMai: LocalDateTime → LocalDate (2 rows)
  C. 3.2.3.18 ChiTietHoaDon: thêm ràng buộc maKhachHang
  D. 3.2.3.07 KhuyenMai: DATETIME → DATE (2 rows)

SRC = 9_REPORT_v14.docx  →  DST = 9_REPORT_v15.docx
WARNING: chỉ patch những thay đổi trên, không chạy lại các PASS của v14.
"""
import zipfile, re, shutil, os

SRC = "document/9_REPORT_v14.docx"
DST = "document/9_REPORT_v15.docx"

shutil.copy(SRC, DST)

with zipfile.ZipFile(DST, "r") as z:
    xml   = z.read("word/document.xml").decode("utf-8")
    files = {n: z.read(n) for n in z.namelist()}

# ── helpers ──────────────────────────────────────────────────────────────────

def xml_to_text(block):
    return "".join(re.findall(r"<w:t[^>]*>([^<]*)</w:t>", block))

def find_tbl_after(xml, marker):
    """Return (tbl_xml, start, end) of first <w:tbl> after marker text."""
    pos = xml.find(marker)
    assert pos >= 0, f"Marker not found: {marker!r}"
    ts = xml.find("<w:tbl>", pos)
    te = xml.find("</w:tbl>", ts) + len("</w:tbl>")
    return xml[ts:te], ts, te

def get_rows(tbl):
    return re.findall(r"<w:tr[ >].*?</w:tr>", tbl, re.DOTALL)

# Counter for fresh paraIds — monotone, deterministic, no randomness
_pid_counter = [0xCC000001]

def fresh_para_id():
    val = f"{_pid_counter[0]:08X}"
    _pid_counter[0] += 1
    return val

def clone_row_with_new_ids(row_xml):
    """Replace every w14:paraId and w14:textId with fresh unique values."""
    result = row_xml
    for m in re.finditer(r'w14:(paraId|textId)="[0-9A-Fa-f]{8}"', result):
        attr = m.group()
        new_attr = re.sub(r'"[0-9A-Fa-f]{8}"', f'"{fresh_para_id()}"', attr)
        result = result.replace(attr, new_attr, 1)
    return result

# ── PATCH A — 3.1.2.16 ChiTietHoaDon: add khachHang row, renumber ───────────
# Mục đích: đồng bộ với ChiTietHoaDon.java (2026-05-26) + SQL có maKhachHang.
# Rủi ro: nếu thứ tự rows thay đổi trong file nguồn sẽ cần điều chỉnh index.

print("PATCH A: 3.1.2.16 ChiTietHoaDon — thêm field khachHang…")
tbl_A, _, _ = find_tbl_after(xml, "Thực thể ChiTietHoaDon:")
rows_A = get_rows(tbl_A)
# rows_A: [0]=header [1]=group "Khai báo" [2]=1.1 maChiTietHD [3]=1.2 hoaDon
#         [4]=1.3 ve  [5]=1.4 chiTietGia  [6]=1.5 giaTien
assert len(rows_A) == 7, f"ChiTietHoaDon 3.1.2 expected 7 rows, got {len(rows_A)}"

r_ve       = rows_A[4]   # 1.3 ve
r_cgia     = rows_A[5]   # 1.4 chiTietGia  → will become 1.5
r_gtien    = rows_A[6]   # 1.5 giaTien     → will become 1.6

# Build new khachHang row from chiTietGia template (same cell widths/borders)
r_khach = clone_row_with_new_ids(r_cgia)
# Cell 1 (STT): stays "1.4" — correct for the inserted row
# Cell 2 (field name)
r_khach = r_khach.replace("<w:t>chiTietGia</w:t>",
                           "<w:t>khachHang</w:t>")
# Cell 3 (type)
r_khach = r_khach.replace("<w:t>ChiTietGia</w:t>",
                           "<w:t>KhachHang</w:t>")
# Cell 4 (constraint)
r_khach = r_khach.replace(
    "<w:t>Khóa ngoại → ChiTietGia, không rỗng</w:t>",
    "<w:t>Khóa ngoại → KhachHang, không rỗng</w:t>")
# Cell 5 (note)
r_khach = r_khach.replace("<w:t>Chi tiết giá áp dụng</w:t>",
                           "<w:t>Khách hàng mua vé</w:t>")
assert "<w:t>khachHang</w:t>" in r_khach, "khachHang row build failed"

# Renumber chiTietGia: 1.4 → 1.5
# First cell of chiTietGia row contains exactly one <w:t>1.4</w:t>
r_cgia_new = r_cgia.replace("<w:t>1.4</w:t>", "<w:t>1.5</w:t>", 1)
assert "<w:t>1.5</w:t>" in r_cgia_new, "chiTietGia renumber failed"

# Renumber giaTien: 1.5 → 1.6
r_gtien_new = r_gtien.replace("<w:t>1.5</w:t>", "<w:t>1.6</w:t>", 1)
assert "<w:t>1.6</w:t>" in r_gtien_new, "giaTien renumber failed"

# Rebuild table
new_tbl_A = tbl_A
new_tbl_A = new_tbl_A.replace(r_ve,    r_ve + r_khach, 1)   # insert khachHang after ve
new_tbl_A = new_tbl_A.replace(r_cgia,  r_cgia_new,     1)   # 1.4→1.5
new_tbl_A = new_tbl_A.replace(r_gtien, r_gtien_new,    1)   # 1.5→1.6
xml = xml.replace(tbl_A, new_tbl_A, 1)

# Verify
tbl_A_check, _, _ = find_tbl_after(xml, "Thực thể ChiTietHoaDon:")
rows_check = get_rows(tbl_A_check)
texts_check = [xml_to_text(r) for r in rows_check]
assert any("khachHang" in t for t in texts_check), "PATCH A verify: khachHang not found"
assert any("1.5" in t and "chiTietGia" in t for t in texts_check), "PATCH A verify: 1.5 chiTietGia not found"
assert any("1.6" in t and "giaTien" in t for t in texts_check), "PATCH A verify: 1.6 giaTien not found"
print(f"  OK — table now has {len(rows_check)} rows")

# ── PATCH B — 3.1.2.19 KhuyenMai: LocalDateTime → LocalDate ─────────────────
# Mục đích: KhuyenMai.java (2026-05-25) + SQL dùng DATE, không phải DATETIME.
# Rủi ro: nếu có entity khác trong cùng file dùng LocalDateTime sẽ không ảnh hưởng
#         vì ta chỉ thay trong đoạn tbl_B (giữa entity KhuyenMai và entity tiếp theo).

print("PATCH B: 3.1.2.19 KhuyenMai — LocalDateTime → LocalDate…")
tbl_B, _, _ = find_tbl_after(xml, "Thực thể KhuyenMai:")
rows_B = get_rows(tbl_B)
ldt_count = sum(1 for r in rows_B if "LocalDateTime" in xml_to_text(r))
assert ldt_count == 2, f"Expected 2 LocalDateTime rows in KhuyenMai, got {ldt_count}"

new_tbl_B = tbl_B.replace("<w:t>LocalDateTime</w:t>", "<w:t>LocalDate</w:t>")
assert new_tbl_B.count("<w:t>LocalDate</w:t>") == 2, "PATCH B: wrong replacement count"
xml = xml.replace(tbl_B, new_tbl_B, 1)
print("  OK — 2 rows updated")

# ── PATCH C — 3.2.3.18 ChiTietHoaDon: add maKhachHang constraint row ────────
# Mục đích: đồng bộ với SQL ChiTietHoaDon (maKhachHang NOT NULL FK→KhachHang).
# Rủi ro: nếu thứ tự rows thay đổi sẽ cần điều chỉnh index.

print("PATCH C: 3.2.3.18 ChiTietHoaDon — thêm maKhachHang…")
tbl_C, _, _ = find_tbl_after(xml, "3.2.3.18 Bảng ChiTietHoaDon")
rows_C = get_rows(tbl_C)
# [0]=header [1]=maChiTietHD [2]=maHoaDon [3]=maVe [4]=maChiTietGia [5]=giaTien
assert len(rows_C) == 6, f"ChiTietHoaDon 3.2.3 expected 6 rows, got {len(rows_C)}"

r_maVe = rows_C[3]   # maVe row — insert khachHang after this

# Build maKhachHang row from maVe template
r_maKhach_323 = clone_row_with_new_ids(r_maVe)
r_maKhach_323 = r_maKhach_323.replace("<w:t>maVe</w:t>",
                                        "<w:t>maKhachHang</w:t>")
# VARCHAR(20) stays the same — no replacement needed for second cell
r_maKhach_323 = r_maKhach_323.replace(
    "<w:t>NOT NULL, UNIQUE, FK → Ve(maVe)</w:t>",
    "<w:t>NOT NULL, FK → KhachHang(maKhachHang)</w:t>")
# "Không" in last cell stays as-is
assert "<w:t>maKhachHang</w:t>" in r_maKhach_323, "maKhachHang row build failed"

new_tbl_C = tbl_C
new_tbl_C = new_tbl_C.replace(r_maVe, r_maVe + r_maKhach_323, 1)
xml = xml.replace(tbl_C, new_tbl_C, 1)

# Verify
tbl_C_check, _, _ = find_tbl_after(xml, "3.2.3.18 Bảng ChiTietHoaDon")
rows_C_check = get_rows(tbl_C_check)
texts_C = [xml_to_text(r) for r in rows_C_check]
assert any("maKhachHang" in t for t in texts_C), "PATCH C verify failed"
print(f"  OK — table now has {len(rows_C_check)} rows")

# ── PATCH D — 3.2.3.07 KhuyenMai: DATETIME → DATE ───────────────────────────
# Mục đích: SQL dùng DATE (không phải DATETIME) cho thoiGianBatDau/KetThuc.
# Rủi ro: chỉ thay trong tbl_D nên không ảnh hưởng bảng khác.

print("PATCH D: 3.2.3.07 KhuyenMai — DATETIME → DATE…")
tbl_D, _, _ = find_tbl_after(xml, "3.2.3.07 Bảng KhuyenMai")
dt_count = tbl_D.count("<w:t>DATETIME</w:t>")
assert dt_count == 2, f"Expected 2 DATETIME in KhuyenMai 3.2.3, got {dt_count}"

new_tbl_D = tbl_D.replace("<w:t>DATETIME</w:t>", "<w:t>DATE</w:t>")
xml = xml.replace(tbl_D, new_tbl_D, 1)
print("  OK — 2 rows updated")

# ── Write output ─────────────────────────────────────────────────────────────
files["word/document.xml"] = xml.encode("utf-8")
with zipfile.ZipFile(DST, "w", zipfile.ZIP_DEFLATED) as zout:
    for name, data in files.items():
        zout.writestr(name, data)

size = os.path.getsize(DST)
print(f"\n✓ Done → {DST}  ({size:,} bytes)")

# ── Sanity checks ─────────────────────────────────────────────────────────────
with zipfile.ZipFile(DST) as z:
    xml_out = z.read("word/document.xml").decode("utf-8")

# KhuyenMai table should no longer contain LocalDateTime (patched to LocalDate)
def find_tbl_after_check(xml, marker):
    pos = xml.find(marker); ts = xml.find("<w:tbl>", pos)
    te = xml.find("</w:tbl>", ts) + len("</w:tbl>")
    return xml[ts:te]

tbl_km_check = find_tbl_after_check(xml_out, "Thực thể KhuyenMai:")
assert "LocalDateTime" not in tbl_km_check, \
    "SANITY: KhuyenMai 3.1.2 still has LocalDateTime"
ldt_remaining = xml_out.count("LocalDateTime")
print(f"Sanity — LocalDateTime in doc (other entities): {ldt_remaining}  (OK, not KhuyenMai)")
print(f"Sanity — LocalDate occurrences: {xml_out.count('LocalDate')}")
print(f"Sanity — maKhachHang in doc: {xml_out.count('maKhachHang')}")
print(f"Sanity — khachHang in 3.1.2 section: {xml_out.count('khachHang')}")
print(f"Sanity — DATE in KhuyenMai 3.2.3: present")
print("All checks passed.")
