import re

# ── helpers ───────────────────────────────────────────────────────────────────

def esc(t):
    return t.replace('&','&amp;').replace('<','&lt;').replace('>','&gt;').replace('"','&quot;')

def para(text='', bold=False, center=False):
    jc  = '<w:jc w:val="center"/>' if center else ''
    b   = '<w:b/><w:bCs/>' if bold else ''
    rpr = b + '<w:sz w:val="24"/>'
    run = ('<w:r><w:rPr>' + rpr + '</w:rPr>'
           + '<w:t xml:space="preserve">' + esc(text) + '</w:t></w:r>') if text else ''
    return ('<w:p><w:pPr><w:ind w:firstLine="0"/>' + jc
            + '<w:rPr>' + rpr + '</w:rPr></w:pPr>' + run + '</w:p>')

TCMAR = ('<w:tcMar><w:top w:w="0" w:type="dxa"/>'
         '<w:left w:w="108" w:type="dxa"/>'
         '<w:bottom w:w="0" w:type="dxa"/>'
         '<w:right w:w="108" w:type="dxa"/></w:tcMar>')

def cell(w, text='', bold=False, center=False, fill=None, span=1):
    gs  = '<w:gridSpan w:val="' + str(span) + '"/>' if span > 1 else ''
    shd = '<w:shd w:val="clear" w:color="auto" w:fill="' + fill + '"/>' if fill else ''
    return ('<w:tc><w:tcPr><w:tcW w:w="' + str(w) + '" w:type="dxa"/>'
            + gs + shd + TCMAR + '</w:tcPr>'
            + para(text, bold=bold, center=center) + '</w:tc>')

def row(*cells, hdr=False):
    trpr = '<w:trPr><w:tblHeader/></w:trPr>' if hdr else ''
    return '<w:tr>' + trpr + ''.join(cells) + '</w:tr>'

# ── row-type builders ─────────────────────────────────────────────────────────

def r_title(title):
    return row(cell(8788, title, bold=True, fill='D9D9D9', span=3), hdr=True)

def r_info(label, val):
    return row(cell(2000, label, bold=True), cell(6788, val, span=2))

def r_section(title):
    return row(cell(8788, title, bold=True, fill='D9D9D9', span=3))

def r_flow_hdr(actor):
    return row(cell(4000, actor, bold=True, center=True, span=2),
               cell(4788, 'Hệ thống', bold=True, center=True))

def r_actor(text):
    return row(cell(4000, text, span=2), cell(4788))

def r_system(text):
    return row(cell(4000, span=2), cell(4788, text))

def r_alt(text):
    return row(cell(8788, text, span=3))

def tbl(rows_xml):
    return (
        '<w:tbl>'
        '<w:tblPr>'
          '<w:tblStyle w:val="TableGrid"/>'
          '<w:tblW w:w="8788" w:type="dxa"/>'
          '<w:tblBorders>'
            '<w:top w:val="single" w:sz="4" w:space="0" w:color="auto"/>'
            '<w:left w:val="single" w:sz="4" w:space="0" w:color="auto"/>'
            '<w:bottom w:val="single" w:sz="4" w:space="0" w:color="auto"/>'
            '<w:right w:val="single" w:sz="4" w:space="0" w:color="auto"/>'
            '<w:insideH w:val="single" w:sz="4" w:space="0" w:color="auto"/>'
            '<w:insideV w:val="single" w:sz="4" w:space="0" w:color="auto"/>'
          '</w:tblBorders>'
          '<w:tblLayout w:type="fixed"/>'
          '<w:tblLook w:val="0000"/>'
        '</w:tblPr>'
        '<w:tblGrid>'
          '<w:gridCol w:w="2000"/>'
          '<w:gridCol w:w="2000"/>'
          '<w:gridCol w:w="4788"/>'
        '</w:tblGrid>'
        + rows_xml +
        '</w:tbl>'
    )

BLANK = '<w:p><w:pPr><w:rPr><w:lang w:val="vi-VN"/></w:rPr></w:pPr></w:p>'

# ── UC1: BAN_VE ───────────────────────────────────────────────────────────────
tbl1 = tbl(
    r_title('Use case: UC_BAN_VE - Bán vé') +
    r_info('Mục đích:',
        'Cho phép nhân viên bán vé thực hiện toàn bộ quy trình bán vé tàu cho khách hàng tại quầy giao dịch.') +
    r_info('Mô tả:',
        'Nhân viên tìm kiếm chuyến tàu theo ga đi, ga đến và ngày khởi hành; chọn ghế trên sơ đồ toa tàu 2D; nhập hoặc tra cứu thông tin hành khách; áp dụng chương trình khuyến mãi (nếu có); xử lý thanh toán bằng tiền mặt hoặc chuyển khoản; hệ thống phát hành vé và lưu hóa đơn vào cơ sở dữ liệu.') +
    r_info('Tác nhân:', 'Nhân viên bán vé (vai trò BAN_VE)') +
    r_info('Điều kiện trước:',
        'Nhân viên đã đăng nhập với quyền BAN_VE. Hệ thống có dữ liệu chuyến tàu, sơ đồ ghế và bảng giá còn hiệu lực.') +
    r_info('Điều kiện sau:',
        'Vé được tạo và lưu vào hệ thống với trạng thái "Đã bán". Ghế tương ứng chuyển sang trạng thái đã đặt. Thông tin hành khách mới (được tạo lần đầu) lưu vào cơ sở dữ liệu. Hóa đơn được tạo và liên kết với các vé trong giao dịch.') +
    r_section('Luồng sự kiện chính (Basic flows):') +
    r_flow_hdr('Nhân viên bán vé') +
    r_actor('1. Nhân viên chọn ga đi, ga đến và ngày khởi hành (hoặc khoảng ngày); nhấn "Tìm kiếm".') +
    r_system('2. Hệ thống kiểm tra: ga đi ≠ ga đến, ngày ≥ hôm nay; hiển thị danh sách chuyến tàu còn ghế trống với các cột: giờ khởi hành, mã đoàn tàu, thời gian chạy và giá vé 3 loại ghế.') +
    r_actor('3. Nhân viên chọn một chuyến tàu từ danh sách kết quả.') +
    r_system('4. Hệ thống hiển thị sơ đồ đoàn tàu và sơ đồ ghế 2D theo từng toa; ghế đã bán được đánh dấu màu khác biệt với ghế trống.') +
    r_actor('5. Nhân viên click vào ghế trống để chọn (có thể chọn nhiều ghế); nhấn "Tiếp theo".') +
    r_system('6. Hệ thống ghi nhận danh sách ghế đã chọn và chuyển sang bước nhập thông tin hành khách.') +
    r_actor('7. Nhân viên tìm kiếm khách hàng theo CCCD/tên/SĐT/email hoặc tạo khách hàng mới nếu chưa có trong hệ thống.') +
    r_system('8. Hệ thống tra cứu trong cơ sở dữ liệu và hiển thị kết quả; nếu là khách mới, lưu thông tin vào hệ thống.') +
    r_actor('9. (Tùy chọn) Nhân viên chọn chương trình khuyến mãi áp dụng cho từng ghế; nhấn "Tiếp theo".') +
    r_system('10. Hệ thống tính tổng giá vé sau khi áp dụng khuyến mãi (nếu có) và hiển thị màn hình tóm tắt đơn hàng.') +
    r_actor('11. Nhân viên xác nhận thông tin và chọn phương thức thanh toán: tiền mặt hoặc chuyển khoản ngân hàng.') +
    r_system('12. Hệ thống hiển thị form thanh toán tương ứng: tiền mặt → nhập số tiền nhận và tính tiền thối real-time; chuyển khoản → hiển thị mã QR và thông tin tài khoản Vietcombank.') +
    r_actor('13. Nhân viên xác nhận thanh toán đã hoàn tất.') +
    r_system('14. Hệ thống tạo hóa đơn, lưu vé với trạng thái "Đã bán", cập nhật ghế đã đặt và hiển thị màn hình hoàn tất giao dịch.') +
    r_section('Luồng sự kiện phụ (Alternative flows):') +
    r_alt('2a. Không tìm thấy chuyến tàu phù hợp: hệ thống hiển thị danh sách rỗng; nhân viên điều chỉnh lại điều kiện tìm kiếm.') +
    r_alt('2b. Ga đi = ga đến hoặc ngày < hôm nay: hệ thống không cho phép tìm kiếm và hiển thị thông báo lỗi.') +
    r_alt('5a. Nhân viên bỏ chọn ghế bằng cách click lại → ghế trở về trạng thái trống.') +
    r_alt('12a. Thanh toán tiền mặt — số tiền nhận < tổng giá vé: hệ thống hiển thị tiền thối âm và không cho phép xác nhận thanh toán.')
)

# ── UC2: HOAN_VE ──────────────────────────────────────────────────────────────
tbl2 = tbl(
    r_title('Use case: UC14 - Hoàn vé') +
    r_info('Mục đích:',
        'Cho phép nhân viên bán vé thực hiện hoàn trả vé cho khách hàng, cập nhật trạng thái vé sang "Đã huỷ".') +
    r_info('Mô tả:',
        'Nhân viên tra cứu vé đã bán trong danh sách, chọn vé cần hoàn, xem thông tin tóm tắt (mã vé, hành khách, hành trình, giờ khởi hành), nhập lý do hoàn vé và xác nhận; hệ thống cập nhật trạng thái vé.') +
    r_info('Tác nhân:', 'Nhân viên bán vé (vai trò BAN_VE)') +
    r_info('Điều kiện trước:',
        'Nhân viên đã đăng nhập với quyền BAN_VE. Vé cần hoàn tồn tại trong hệ thống và chưa ở trạng thái "Đã huỷ".') +
    r_info('Điều kiện sau:',
        'Vé được cập nhật trạng thái sang "Đã huỷ" trong cơ sở dữ liệu. Lý do hoàn vé được ghi nhận.') +
    r_section('Luồng sự kiện chính (Basic flows):') +
    r_flow_hdr('Nhân viên bán vé') +
    r_actor('1. Nhân viên mở màn hình quản lý vé, tìm kiếm vé cần hoàn theo mã vé, tên hành khách hoặc số CCCD.') +
    r_system('2. Hệ thống hiển thị danh sách vé phù hợp với các thông tin: mã vé, hành khách, hành trình, giờ khởi hành và trạng thái.') +
    r_actor('3. Nhân viên chọn vé cần hoàn và nhấn nút "Hoàn vé".') +
    r_system('4. Hệ thống kiểm tra trạng thái vé; nếu hợp lệ, mở dialog hoàn vé hiển thị thông tin tóm tắt: mã vé, tên hành khách, hành trình (ga đi → ga đến), giờ khởi hành.') +
    r_actor('5. Nhân viên nhập lý do hoàn vé (tối thiểu 5 ký tự) và nhấn "Xác nhận hoàn vé".') +
    r_system('6. Hệ thống kiểm tra lý do hợp lệ; cập nhật trạng thái vé sang "Đã huỷ" trong cơ sở dữ liệu; đóng dialog và làm mới danh sách vé.') +
    r_section('Luồng sự kiện phụ (Alternative flows):') +
    r_alt('4a. Vé đã ở trạng thái "Đã huỷ": hệ thống hiển thị cảnh báo "Vé này đã được hủy trước đó" và không mở dialog hoàn vé.') +
    r_alt('5a. Lý do ít hơn 5 ký tự: hệ thống hiển thị thông báo "Lý do tối thiểu 5 ký tự" và không cho phép xác nhận.')
)

# ── UC3: LICH_CHAY ────────────────────────────────────────────────────────────
tbl3 = tbl(
    r_title('Use case: UC_LICH_CHAY - Tra cứu lịch chạy tàu') +
    r_info('Mục đích:',
        'Cho phép nhân viên điều phối tìm kiếm, lọc và theo dõi toàn bộ danh sách lịch chạy tàu theo nhiều tiêu chí.') +
    r_info('Mô tả:',
        'Nhân viên mở module Quản lý lịch chạy, hệ thống hiển thị thống kê tổng quan và danh sách đầy đủ các lịch chạy. Nhân viên có thể nhập từ khoá hoặc chọn bộ lọc (ga đi, ga đến, khoảng ngày) để thu hẹp kết quả; hệ thống cập nhật bảng theo thời gian thực với phân trang động.') +
    r_info('Tác nhân:', 'Nhân viên điều phối (vai trò DIEU_PHOI)') +
    r_info('Điều kiện trước:', 'Nhân viên đã đăng nhập với quyền DIEU_PHOI.') +
    r_info('Điều kiện sau:',
        'Danh sách lịch chạy được hiển thị theo điều kiện lọc. Không có thay đổi dữ liệu trong hệ thống.') +
    r_section('Luồng sự kiện chính (Basic flows):') +
    r_flow_hdr('Nhân viên điều phối') +
    r_actor('1. Nhân viên mở module Quản lý lịch chạy tàu.') +
    r_system('2. Hệ thống tải toàn bộ dữ liệu lịch chạy từ cơ sở dữ liệu; hiển thị 3 thẻ thống kê: Tổng số lịch, Lịch chạy hôm nay, Số lượt đoàn tàu phân biệt. Hiển thị bảng danh sách với các cột: Mã lịch, Đoàn tàu, Tuyến, Thời gian bắt đầu, Thời gian chạy, Trạng thái, Thao tác.') +
    r_actor('3. Nhân viên nhập từ khoá vào ô tìm kiếm (tìm theo mã lịch, tên tuyến, mã đoàn tàu) và/hoặc chọn bộ lọc: ga đi, ga đến (SearchableComboBox), từ ngày, đến ngày.') +
    r_system('4. Hệ thống áp dụng bộ lọc theo thời gian thực trên toàn bộ dữ liệu; cập nhật bảng với các lịch chạy phù hợp và phân trang kết quả. Số bản ghi tìm được hiển thị ở thanh phân trang.') +
    r_actor('5. (Tùy chọn) Nhân viên nhấn "Bỏ lọc" để xóa toàn bộ điều kiện lọc.') +
    r_system('6. Hệ thống xóa các bộ lọc đã chọn và hiển thị lại toàn bộ danh sách lịch chạy.') +
    r_section('Luồng sự kiện phụ (Alternative flows):') +
    r_alt('4a. Không tìm thấy lịch chạy phù hợp: hệ thống hiển thị bảng rỗng; nhân viên điều chỉnh lại điều kiện lọc.')
)

# ── UC4: THEM_GIA ─────────────────────────────────────────────────────────────
tbl4 = tbl(
    r_title('Use case: UC25 - Thiết lập bảng giá vé') +
    r_info('Mục đích:',
        'Cho phép nhân viên điều phối tạo mới một bảng giá vé với thông tin chung và khoảng thời gian hiệu lực.') +
    r_info('Mô tả:',
        'Nhân viên mở module Quản lý bảng giá, nhấn "Thêm giá mới". Hệ thống mở dialog với mã giá tự động sinh (tiền tố "GIA"). Nhân viên nhập mô tả, chọn trạng thái, nhập ngày bắt đầu và ngày kết thúc hiệu lực. Hệ thống validate và lưu bảng giá mới.') +
    r_info('Tác nhân:', 'Nhân viên điều phối (vai trò DIEU_PHOI)') +
    r_info('Điều kiện trước:', 'Nhân viên đã đăng nhập với quyền DIEU_PHOI.') +
    r_info('Điều kiện sau:',
        'Bảng giá mới được lưu vào cơ sở dữ liệu với mã tự động sinh. Danh sách bảng giá được làm mới.') +
    r_section('Luồng sự kiện chính (Basic flows):') +
    r_flow_hdr('Nhân viên điều phối') +
    r_actor('1. Nhân viên mở module Quản lý bảng giá và nhấn nút "Thêm giá mới".') +
    r_system('2. Hệ thống mở dialog Thêm bảng giá với mã giá tự động sinh (tiền tố "GIA", chỉ đọc); hiển thị các trường: Mô tả, Trạng thái (combobox), Thời gian áp dụng, Thời gian kết thúc.') +
    r_actor('3. Nhân viên nhập mô tả bảng giá, chọn trạng thái, nhập ngày bắt đầu và ngày kết thúc theo định dạng dd/MM/yyyy.') +
    r_system('4. Hệ thống validate dữ liệu: mô tả không được rỗng; cả hai ngày phải đúng định dạng; ngày kết thúc phải lớn hơn ngày bắt đầu.') +
    r_actor('5. Nhân viên nhấn "Lưu".') +
    r_system('6. Hệ thống lưu bảng giá mới vào cơ sở dữ liệu; đóng dialog và làm mới danh sách bảng giá.') +
    r_section('Luồng sự kiện phụ (Alternative flows):') +
    r_alt('4a. Ngày kết thúc ≤ ngày bắt đầu: hệ thống hiển thị thông báo lỗi validation, không cho phép lưu.') +
    r_alt('4b. Mô tả để trống: hệ thống yêu cầu nhập mô tả trước khi lưu.')
)

# ── UC5: CHI_TIET_GIA ─────────────────────────────────────────────────────────
tbl5 = tbl(
    r_title('Use case: UC26 - Quản lý chi tiết giá theo loại ghế') +
    r_info('Mục đích:',
        'Cho phép nhân viên điều phối thêm dòng chi tiết giá theo tuyến đường và loại ghế vào một bảng giá, với cơ chế tự động tạo bản sao nếu bảng giá đang được áp dụng.') +
    r_info('Mô tả:',
        'Nhân viên mở bảng giá cần cập nhật và nhấn "Thêm chi tiết giá". Hệ thống mở dialog với mã chi tiết tự động sinh (tiền tố "CTG"). Nhân viên chọn tuyến đường, loại ghế và nhập giá niêm yết. Hệ thống kiểm tra trùng lặp và thực hiện lưu trực tiếp hoặc tạo bản sao bảng giá nếu cần.') +
    r_info('Tác nhân:', 'Nhân viên điều phối (vai trò DIEU_PHOI)') +
    r_info('Điều kiện trước:',
        'Nhân viên đã đăng nhập với quyền DIEU_PHOI. Bảng giá cần thêm chi tiết đã tồn tại trong hệ thống.') +
    r_info('Điều kiện sau:',
        'Chi tiết giá mới được lưu vào bảng giá. Nếu bảng giá đang được áp dụng, một bảng giá bản sao mới được tạo chứa chi tiết mới.') +
    r_section('Luồng sự kiện chính (Basic flows):') +
    r_flow_hdr('Nhân viên điều phối') +
    r_actor('1. Nhân viên mở bảng giá cần cập nhật (nhấn nút chỉnh sửa) và nhấn "Thêm chi tiết giá".') +
    r_system('2. Hệ thống mở dialog Thêm chi tiết giá với mã chi tiết tự động sinh (tiền tố "CTG", chỉ đọc); hiển thị các trường: Tuyến đường (SearchableComboBox), Loại ghế (combobox: NGOI_MEM, NAM_CUNG, NAM_MEM, VIP), Giá niêm yết.') +
    r_actor('3. Nhân viên chọn tuyến đường, chọn loại ghế và nhập giá niêm yết (phạm vi: 1.000 – 100.000.000 VNĐ, phân cách nghìn bằng dấu chấm).') +
    r_system('4. Hệ thống validate: giá trong phạm vi hợp lệ; kiểm tra không tồn tại chi tiết trùng tuyến + loại ghế trong cùng bảng giá.') +
    r_actor('5. Nhân viên nhấn "Lưu".') +
    r_system('6a. (Bảng giá chưa được áp dụng) Hệ thống lưu chi tiết giá mới trực tiếp vào bảng giá hiện tại; làm mới danh sách chi tiết.') +
    r_system('6b. (Bảng giá đang được áp dụng) Hệ thống tự động tạo bản sao bảng giá mới, thêm chi tiết vào bản sao và liên kết bản sao thay thế bảng gốc; thông báo cho nhân viên về việc tạo bản sao.') +
    r_section('Luồng sự kiện phụ (Alternative flows):') +
    r_alt('4a. Tuyến + loại ghế đã tồn tại trong bảng giá: hệ thống hiển thị thông báo lỗi trùng lặp và không cho phép lưu.') +
    r_alt('4b. Giá niêm yết ngoài phạm vi (< 1.000 hoặc > 100.000.000): hệ thống hiển thị lỗi validation.')
)

# ── assemble & insert ─────────────────────────────────────────────────────────
INSERT = (
    BLANK +
    tbl1 + BLANK +
    tbl2 + BLANK +
    tbl3 + BLANK +
    tbl4 + BLANK +
    tbl5 + BLANK
)

src = 'document/9_REPORT_v6_unpacked/word/document.xml'
with open(src, encoding='utf-8') as f:
    xml = f.read()

idx = xml.find('13681C8E')
assert idx != -1, 'Heading2 paraId not found'
p_end = xml.find('</w:p>', idx) + len('</w:p>')

new_xml = xml[:p_end] + '\n    ' + INSERT + xml[p_end:]

with open(src, 'w', encoding='utf-8') as f:
    f.write(new_xml)

print('Inserted', len(INSERT), 'chars after pos', p_end)
print('New document.xml length:', len(new_xml))

# namespace sanity check
a_count   = new_xml.count(' a:')
pic_count = new_xml.count(' pic:')
print(f'Namespace check  a:{a_count}  pic:{pic_count}')
assert a_count > 0 and pic_count > 0, 'NAMESPACE BROKEN!'
print('All OK.')
