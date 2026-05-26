import re

# ── XML helpers ───────────────────────────────────────────────────────────────

def esc(t):
    return t.replace('&','&amp;').replace('<','&lt;').replace('>','&gt;').replace('"','&quot;')

TCMAR = ('<w:tcMar>'
         '<w:top w:w="80" w:type="dxa"/>'
         '<w:left w:w="120" w:type="dxa"/>'
         '<w:bottom w:w="80" w:type="dxa"/>'
         '<w:right w:w="120" w:type="dxa"/>'
         '</w:tcMar>')

def p_cell(text, bold=False, center=False):
    jc  = '<w:jc w:val="center"/>' if center else '<w:jc w:val="left"/>'
    b   = '<w:b/><w:bCs/>' if bold else ''
    rpr = b + '<w:sz w:val="24"/>'
    if text:
        run = '<w:r><w:rPr>' + rpr + '</w:rPr><w:t xml:space="preserve">' + esc(text) + '</w:t></w:r>'
    else:
        run = ''
    return ('<w:p><w:pPr><w:ind w:firstLine="0"/>' + jc
            + '<w:rPr>' + rpr + '</w:rPr></w:pPr>' + run + '</w:p>')

def tc(width, text='', bold=False, center=False, no_mar=False):
    mar = '' if no_mar else TCMAR
    return ('<w:tc><w:tcPr>'
            '<w:tcW w:w="' + str(width) + '" w:type="dxa"/>'
            + mar + '</w:tcPr>'
            + p_cell(text, bold=bold, center=center)
            + '</w:tc>')

def make_data_row(uc_code, name, desc, actor, note=''):
    """Standard 5-column UC row"""
    return ('<w:tr>'
            + tc(1139, uc_code, bold=True, center=True)
            + tc(1771, name)
            + tc(3491, desc)
            + tc(1405, actor)
            + tc(1222, note, no_mar=True)
            + '</w:tr>')

def make_header_row():
    """Bold header row with tblHeader"""
    def hdr_tc(width, text, no_mar=False):
        mar = '' if no_mar else TCMAR
        rpr = '<w:b/><w:bCs/><w:sz w:val="24"/>'
        run = '<w:r><w:rPr>' + rpr + '</w:rPr><w:t xml:space="preserve">' + esc(text) + '</w:t></w:r>'
        p   = ('<w:p><w:pPr><w:ind w:firstLine="0"/><w:jc w:val="center"/>'
               '<w:rPr>' + rpr + '</w:rPr></w:pPr>' + run + '</w:p>')
        return '<w:tc><w:tcPr><w:tcW w:w="' + str(width) + '" w:type="dxa"/>' + mar + '</w:tcPr>' + p + '</w:tc>'
    return ('<w:tr><w:trPr><w:tblHeader/></w:trPr>'
            + hdr_tc(1139, 'Mã UC')
            + hdr_tc(1771, 'Tên Use case')
            + hdr_tc(3491, 'Mô tả')
            + hdr_tc(1405, 'Tác nhân')
            + hdr_tc(1222, 'Ghi chú', no_mar=True)
            + '</w:tr>')

# ── Complete UC table data (final, renumbered UC01-UC39) ──────────────────────
#   (uc_code, name, description, actor)

UC_DATA = [
    # ── Tất cả ────────────────────────────────────────────────────────────────
    ('UC01', 'Đăng nhập hệ thống',
     'Người dùng nhập tên đăng nhập và mật khẩu để xác thực và truy cập hệ thống.',
     'Tất cả'),
    ('UC02', 'Đăng xuất hệ thống',
     'Người dùng kết thúc phiên làm việc, xóa session và trở về màn hình đăng nhập.',
     'Tất cả'),
    ('UC03', 'Xem Dashboard',
     'Hiển thị bảng điều khiển tổng quan sau khi đăng nhập: thống kê nhanh, trạng thái hệ thống.',
     'Tất cả'),
    ('UC04', 'Xem thống kê và xuất báo cáo',
     'Xem biểu đồ doanh thu, số vé bán theo kỳ; xuất báo cáo ra file Excel/PDF.',
     'Tất cả'),
    ('UC05', 'Xem và cập nhật thông tin cá nhân',
     'Người dùng xem hồ sơ cá nhân (họ tên, SĐT, email) và thay đổi mật khẩu.',
     'Tất cả'),
    # ── Bán vé ────────────────────────────────────────────────────────────────
    ('UC06', 'Bán vé',
     'Nhân viên thực hiện quy trình bán vé tàu: tra cứu chuyến, chọn ghế, nhập thông tin hành khách, áp dụng khuyến mãi (nếu có), thanh toán và phát hành vé.',
     'Bán vé'),
    ('UC07', 'Hoàn vé',
     'Chọn vé cần hoàn, nhập lý do hoàn (tối thiểu 5 ký tự), xác nhận; hệ thống cập nhật trạng thái vé sang Đã huỷ.',
     'Bán vé'),
    ('UC08', 'Tra cứu chuyến tàu',
     'Nhập ga đi, ga đến và ngày khởi hành (hoặc khoảng ngày) để tra cứu danh sách chuyến tàu còn ghế trống.',
     'Bán vé'),
    ('UC09', 'Xem sơ đồ toa và chọn ghế',
     'Chọn toa tàu, xem sơ đồ ghế trực quan; click để chọn/bỏ chọn ghế trống cho hành khách.',
     'Bán vé'),
    ('UC10', 'Tra cứu thông tin hành khách',
     'Nhập CCCD/CMND, họ tên, SĐT của hành khách hoặc tra cứu khách hàng đã có trong hệ thống.',
     'Bán vé'),
    ('UC11', 'Thanh toán',
     'Xử lý thanh toán bằng tiền mặt (nhập số tiền nhận, tính tiền thối real-time) hoặc chuyển khoản ngân hàng (hiển thị mã QR và thông tin tài khoản Vietcombank).',
     'Bán vé'),
    ('UC12', 'Tra cứu lịch chạy tàu',
     'Tìm kiếm và lọc danh sách lịch chạy theo mã lịch, tên tuyến, mã đoàn tàu, ga đi/đến và khoảng ngày; xem thống kê tổng số lịch, lịch hôm nay và số lượt đoàn tàu.',
     'Bán vé'),
    ('UC13', 'Xuất vé và in vé',
     'Sau khi thanh toán thành công, hệ thống tạo vé điện tử và hỗ trợ in vé vật lý.',
     'Bán vé'),
    ('UC14', 'Tra cứu vé đã bán',
     'Tìm kiếm vé theo mã vé, tên hành khách hoặc số CCCD; xem chi tiết trạng thái vé.',
     'Bán vé'),
    ('UC15', 'Xem chi tiết hóa đơn',
     'Mở hóa đơn giao dịch, xem danh sách vé trong hóa đơn, tổng tiền và trạng thái thanh toán.',
     'Bán vé'),
    ('UC16', 'Áp dụng chương trình khuyến mãi',
     'Trong quá trình bán vé, nhân viên chọn chương trình khuyến mãi phù hợp áp dụng cho từng ghế; hệ thống tính lại giá vé sau khi áp dụng giảm giá.',
     'Bán vé'),
    ('UC17', 'Thêm khách hàng mới',
     'Nhập thông tin khách hàng mới (họ tên, CCCD, SĐT, email) vào cơ sở dữ liệu.',
     'Bán vé'),
    ('UC18', 'Cập nhật thông tin khách hàng',
     'Sửa thông tin liên lạc hoặc giấy tờ tùy thân của khách hàng đã có trong hệ thống.',
     'Bán vé'),
    ('UC19', 'Xem lịch sử giao dịch khách hàng',
     'Hiển thị danh sách vé và hóa đơn gắn với một khách hàng cụ thể theo thời gian.',
     'Bán vé'),
    # ── Điều phối ─────────────────────────────────────────────────────────────
    ('UC20', 'Thêm đoàn tàu mới',
     'Tạo hồ sơ đoàn tàu mới: mã số, tên, mô tả và cấu hình toa; lưu vào danh sách quản lý.',
     'Điều phối'),
    ('UC21', 'Chỉnh sửa thông tin đoàn tàu',
     'Sửa thông tin đoàn tàu hiện có; chuyển đổi trạng thái hoạt động/ngừng thông qua dialog xác nhận.',
     'Điều phối'),
    ('UC22', 'Thêm toa tàu',
     'Thêm toa tàu mới vào đoàn tàu; cấu hình số hàng, số cột ghế và loại ghế cho toa.',
     'Điều phối'),
    ('UC23', 'Chỉnh sửa thông tin toa tàu',
     'Sửa thông tin toa tàu hiện có; xem sơ đồ cấu hình ghế; đổi trạng thái hoạt động của toa.',
     'Điều phối'),
    ('UC24', 'Thêm đầu máy',
     'Tạo hồ sơ đầu máy mới: mã, ký hiệu, nhà sản xuất, năm đưa vào sử dụng; lưu vào danh sách đầu máy.',
     'Điều phối'),
    ('UC25', 'Chỉnh sửa thông tin đầu máy',
     'Sửa thông tin đầu máy hiện có: ký hiệu, nhà sản xuất, năm sử dụng.',
     'Điều phối'),
    ('UC26', 'Tạo tuyến đường',
     'Định nghĩa tuyến đường mới: ga đầu, ga cuối, danh sách ga trung gian và thứ tự dừng.',
     'Điều phối'),
    ('UC27', 'Chỉnh sửa tuyến đường',
     'Sửa thông tin tuyến đường; cập nhật ga trung gian và thứ tự dừng; đổi trạng thái hoạt động tuyến.',
     'Điều phối'),
    ('UC28', 'Tạo ga',
     'Thêm ga mới vào hệ thống: nhập mã ga, tên ga và các thông tin địa lý cơ bản.',
     'Điều phối'),
    ('UC29', 'Chỉnh sửa thông tin ga',
     'Sửa thông tin ga hiện có: tên, địa chỉ, trạng thái hoạt động.',
     'Điều phối'),
    ('UC30', 'Tạo lịch chạy tàu',
     'Lên lịch trình mới: gán đoàn tàu, tuyến đường, đầu máy, giờ khởi hành và giờ đến từng ga.',
     'Điều phối'),
    ('UC31', 'Chỉnh sửa lịch chạy tàu',
     'Sửa giờ chạy, thành phần đoàn tàu; ngưng hoạt động hoặc kích hoạt lại lịch chạy thông qua dialog xác nhận.',
     'Điều phối'),
    ('UC32', 'Tạo bảng giá vé',
     'Tạo bảng giá mới và cấu hình thông tin chung: mô tả, trạng thái và thời gian hiệu lực; mã giá được tự động sinh (tiền tố "GIA").',
     'Điều phối'),
    ('UC33', 'Quản lý chi tiết giá theo loại ghế',
     'Trong bảng giá: thêm, sửa hoặc xóa dòng chi tiết theo tuyến và loại ghế; xóa bị khóa nếu bảng giá đang được áp dụng; hệ thống tự động tạo bản sao khi bảng giá đang được dùng.',
     'Điều phối'),
    ('UC34', 'Tạo chương trình khuyến mãi',
     'Tạo chương trình KM mới: tên, % giảm, thời gian hiệu lực, điều kiện áp dụng.',
     'Điều phối'),
    ('UC35', 'Chỉnh sửa và quản lý chi tiết khuyến mãi',
     'Sửa thông tin chương trình KM; thêm/sửa/xóa mã chi tiết; nếu KM đã áp dụng, hệ thống tạo bản sao thay cho chỉnh sửa trực tiếp.',
     'Điều phối'),
    # ── Admin ──────────────────────────────────────────────────────────────────
    ('UC36', 'Thêm tài khoản nhân viên',
     'Tạo tài khoản mới: nhập họ tên, CCCD, SĐT, email, mật khẩu và gán vai trò (Bán vé/Điều phối/Admin).',
     'Admin'),
    ('UC37', 'Cập nhật thông tin nhân viên',
     'Sửa thông tin cá nhân hoặc thay đổi vai trò của nhân viên thông qua form chỉnh sửa.',
     'Admin'),
    ('UC38', 'Chuyển trạng thái nhân viên sang Đã nghỉ',
     'Thay đổi trạng thái tài khoản nhân viên sang "Đã nghỉ" (DA_NGHI) thông qua form chỉnh sửa; dữ liệu được giữ lại trong hệ thống.',
     'Admin'),
    ('UC39', 'Phân quyền vai trò cho nhân viên',
     'Gán hoặc thay đổi vai trò (Bán vé / Điều phối / Admin) của tài khoản nhân viên thông qua form cập nhật.',
     'Admin'),
]

# ── Build new table 2.3 ───────────────────────────────────────────────────────
TBLPR_GRID = (
    '<w:tblPr>'
      '<w:tblStyle w:val="TableGrid"/>'
      '<w:tblW w:w="9028" w:type="dxa"/>'
      '<w:tblBorders>'
        '<w:top w:val="single" w:sz="8" w:space="0" w:color="000000"/>'
        '<w:left w:val="single" w:sz="8" w:space="0" w:color="000000"/>'
        '<w:bottom w:val="single" w:sz="8" w:space="0" w:color="000000"/>'
        '<w:right w:val="single" w:sz="8" w:space="0" w:color="000000"/>'
        '<w:insideH w:val="single" w:sz="8" w:space="0" w:color="000000"/>'
        '<w:insideV w:val="single" w:sz="8" w:space="0" w:color="000000"/>'
      '</w:tblBorders>'
      '<w:tblCellMar>'
        '<w:top w:w="80" w:type="dxa"/>'
        '<w:left w:w="120" w:type="dxa"/>'
        '<w:bottom w:w="80" w:type="dxa"/>'
        '<w:right w:w="120" w:type="dxa"/>'
      '</w:tblCellMar>'
      '<w:tblLook w:val="04A0" w:firstRow="1" w:lastRow="0" w:firstColumn="1"'
      ' w:lastColumn="0" w:noHBand="0" w:noVBand="1"/>'
    '</w:tblPr>'
    '<w:tblGrid>'
      '<w:gridCol w:w="1139"/>'
      '<w:gridCol w:w="1771"/>'
      '<w:gridCol w:w="3491"/>'
      '<w:gridCol w:w="1405"/>'
      '<w:gridCol w:w="1222"/>'
    '</w:tblGrid>'
)

rows_xml = make_header_row()
for uc, name, desc, actor in UC_DATA:
    rows_xml += make_data_row(uc, name, desc, actor)

NEW_TBL23 = '<w:tbl>' + TBLPR_GRID + rows_xml + '</w:tbl>'

# ── Patch document.xml ────────────────────────────────────────────────────────
src = 'document/9_REPORT_v7_unpacked/word/document.xml'
with open(src, encoding='utf-8') as f:
    xml = f.read()

def find_tbl_end(xml, start):
    depth, i = 0, start
    while i < len(xml):
        o_m = re.search(r'<w:tbl[ >]', xml[i:])
        o = (o_m.start() + i) if o_m else len(xml)
        c_m = re.search(r'</w:tbl>', xml[i:])
        c = (c_m.start() + i) if c_m else len(xml)
        if o < c:
            depth += 1; i = o + 1
        else:
            depth -= 1
            end = c + len('</w:tbl>')
            if depth == 0: return (start, end)
            i = c + 1

# Replace table 2.3 (pos 110618)
s23, e23 = find_tbl_end(xml, 110618)
xml = xml[:s23] + NEW_TBL23 + xml[e23:]
print(f'Table 2.3 replaced: {e23-s23} → {len(NEW_TBL23)} chars')

# ── Update 2.4 UC codes ───────────────────────────────────────────────────────
# Map old header text → new header text
uc_remap = {
    'Use case: UC_BAN_VE - Bán vé':
        'Use case: UC06 - Bán vé',
    'Use case: UC14 - Hoàn vé':
        'Use case: UC07 - Hoàn vé',
    'Use case: UC_LICH_CHAY - Tra cứu lịch chạy tàu':
        'Use case: UC12 - Tra cứu lịch chạy tàu',
    'Use case: UC25 - Thiết lập bảng giá vé':
        'Use case: UC32 - Tạo bảng giá vé',
    'Use case: UC26 - Quản lý chi tiết giá theo loại ghế':
        'Use case: UC33 - Quản lý chi tiết giá theo loại ghế',
}

for old, new in uc_remap.items():
    old_esc = esc(old)
    new_esc = esc(new)
    if old_esc in xml:
        xml = xml.replace(old_esc, new_esc, 1)
        print(f'  2.4 updated: {old} → {new}')
    else:
        print(f'  WARNING: not found in 2.4: {old}')

# Also fix actor in UC12 table (from Điều phối → Bán vé)
# The info row "Tác nhân:" in UC12 table has "Nhân viên điều phối (vai trò DIEU_PHOI)"
old_actor = esc('Nhân viên điều phối (vai trò DIEU_PHOI)')
new_actor = esc('Nhân viên bán vé (vai trò BAN_VE)')
# Only replace after UC12 header appears
uc12_hdr = esc('Use case: UC12 - Tra cứu lịch chạy tàu')
idx12 = xml.find(uc12_hdr)
if idx12 != -1:
    # find old_actor after idx12 and within next 3000 chars
    seg = xml[idx12:idx12+4000]
    if old_actor in seg:
        xml = xml[:idx12] + seg.replace(old_actor, new_actor, 1) + xml[idx12+4000:]
        print('  UC12 actor fixed: Điều phối → Bán vé')

with open(src, 'w', encoding='utf-8') as f:
    f.write(xml)

print(f'Done. New document.xml length: {len(xml)}')

# ── Verify ────────────────────────────────────────────────────────────────────
tbls = [m.start() for m in re.finditer(r'<w:tbl[ >]', xml)]
print(f'Total tables: {len(tbls)}')

# Count rows in new table 2.3
# Table 2 in new xml
tbl2_pos = tbls[2]
s2, e2 = find_tbl_end(xml, tbl2_pos)
rows_new = len(re.findall(r'<w:tr[ >]', xml[s2:e2]))
print(f'Table 2.3 rows: {rows_new} (expected 40)')

# Check all UC codes present
for uc in ['UC01', 'UC06', 'UC12', 'UC32', 'UC33', 'UC39']:
    print(f'  {uc} in doc: {uc in xml}')
