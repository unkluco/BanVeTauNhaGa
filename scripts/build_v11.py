"""
build_v11.py
1. Section 3.4 — insert 27 screen subsections (Heading3 + body)
2. Chapter 4  — fill 4.1 / 4.2 / 4.3 body content
3. Chapter 5  — fill Khởi nghiệp body content
Base: document/9_REPORT_v10.docx → output: document/9_REPORT_v11.docx
"""
import zipfile, os, re

SRC = "document/9_REPORT_v10.docx"
DST = "document/9_REPORT_v11.docx"

# ─── helpers ──────────────────────────────────────────────────────────────────

_pid = [0xC0000000]

def _new_pid():
    _pid[0] += 1
    return f"{_pid[0]:08X}"

def esc(s):
    return (s.replace("&", "&amp;")
             .replace("<", "&lt;")
             .replace(">", "&gt;")
             .replace('"', "&quot;"))

def _h3(text):
    """Heading3 paragraph — dùng cho tiêu đề 3.4.X"""
    pid = _new_pid()
    return (f'<w:p w14:paraId="{pid}" w14:textId="77777777"'
            f' w:rsidR="00CC1234" w:rsidRDefault="00CC1234">'
            f'<w:pPr><w:pStyle w:val="Heading3"/></w:pPr>'
            f'<w:r><w:t>{esc(text)}</w:t></w:r></w:p>')

def _body(text):
    """Normal body paragraph"""
    pid = _new_pid()
    return (f'<w:p w14:paraId="{pid}" w14:textId="77777777"'
            f' w:rsidR="00CC1234" w:rsidRDefault="00CC1234">'
            f'<w:r><w:t xml:space="preserve">{esc(text)}</w:t></w:r></w:p>')

def _bullet(text):
    """Indented bullet paragraph dùng cho list (dấu –)"""
    pid = _new_pid()
    return (f'<w:p w14:paraId="{pid}" w14:textId="77777777"'
            f' w:rsidR="00CC1234" w:rsidRDefault="00CC1234">'
            f'<w:pPr><w:ind w:left="360"/></w:pPr>'
            f'<w:r><w:t xml:space="preserve">– {esc(text)}</w:t></w:r></w:p>')

# ─── 3.4 screen list ──────────────────────────────────────────────────────────

SCREENS = [
    ("3.4.1. Màn hình Đăng nhập",
     "Màn hình khởi động của ứng dụng, cho phép nhân viên đăng nhập bằng tên đăng nhập"
     " và mật khẩu. Sau khi xác thực thành công, hệ thống kiểm tra vai trò tài khoản"
     " (Quản trị, Điều phối, Bán vé) và điều hướng vào giao diện chính với quyền truy cập"
     " tương ứng."),

    ("3.4.2. Màn hình Tổng quan",
     "Màn hình chính sau đăng nhập, hiển thị tổng hợp các chỉ số hoạt động qua các thẻ KPI"
     " (doanh thu, số vé bán, số hóa đơn trong ngày/tuần/tháng). Giao diện tích hợp ô tìm"
     " kiếm toàn cục cho phép điều hướng nhanh đến bất kỳ lịch trình, hóa đơn hay vé cụ thể."),

    ("3.4.3. Bán vé – Bước 1: Thông tin chuyến đi",
     "Bước khởi đầu trong luồng bán vé, nhân viên nhập các tiêu chí tìm chuyến gồm ga đi,"
     " ga đến và ngày khởi hành. Hệ thống kiểm tra tính hợp lệ (ga đi phải khác ga đến, ngày"
     " không được ở quá khứ) trước khi cho phép tiếp tục sang bước chọn chuyến tàu."),

    ("3.4.4. Bán vé – Bước 2: Chọn chuyến tàu",
     "Hiển thị danh sách các lịch trình phù hợp với tiêu chí đã nhập, sắp xếp theo giờ"
     " khởi hành. Mỗi dòng thể hiện mã đoàn tàu, giờ đi, giờ đến dự kiến, thời gian di"
     " chuyển, số chỗ còn trống và giá vé. Nhân viên chọn một lịch trình để tiếp tục."),

    ("3.4.5. Bán vé – Bước 3: Chọn chỗ ngồi",
     "Hiển thị sơ đồ toa tàu với từng ghế theo dạng lưới. Ghế còn trống và đã được đặt"
     " được phân biệt bằng màu sắc; hệ thống hiển thị tooltip thông tin giá và loại ghế khi"
     " di chuột vào. Nhân viên chọn số lượng ghế phù hợp theo yêu cầu hành khách."),

    ("3.4.6. Bán vé – Bước 4: Thông tin khách hàng",
     "Nhân viên tra cứu và chọn khách hàng đã có trong hệ thống hoặc thêm mới nhanh. Với"
     " mỗi ghế đã chọn, nhân viên gán tương ứng một khách hàng và xác nhận thông tin bắt buộc"
     " (họ tên, số điện thoại) trước khi chuyển bước tiếp theo."),

    ("3.4.7. Bán vé – Bước 5: Áp dụng khuyến mãi",
     "Liệt kê các chương trình khuyến mãi đang hoạt động và phù hợp với tuyến hoặc loại ghế"
     " đã chọn. Nhân viên có thể áp dụng khuyến mãi cho từng ghế; hệ thống tự động tính toán"
     " và cập nhật giá sau giảm theo thời gian thực."),

    ("3.4.8. Bán vé – Bước 6: Xác nhận đặt vé",
     "Trình bày bảng tóm tắt toàn bộ đơn hàng bao gồm thông tin chuyến tàu, danh sách ghế"
     " cùng tên khách hàng, khuyến mãi áp dụng, đơn giá, chiết khấu và tổng tiền thanh toán."
     " Nhân viên chọn phương thức thanh toán (Tiền mặt hoặc Chuyển khoản) rồi xác nhận."),

    ("3.4.9. Bán vé – Bước 7a: Thanh toán tiền mặt",
     "Giao diện thanh toán tiền mặt với vùng hiển thị nổi bật số tiền cần thu và ô nhập số"
     " tiền khách đưa. Hệ thống tự tính và hiển thị ngay số tiền thối lại; nhân viên xác nhận"
     " để hoàn tất giao dịch và tạo hóa đơn."),

    ("3.4.10. Bán vé – Bước 7b: Thanh toán chuyển khoản",
     "Hiển thị mã QR và thông tin tài khoản ngân hàng nhận chuyển khoản gồm số tài khoản,"
     " tên chủ tài khoản, ngân hàng và nội dung chuyển khoản đề xuất. Nhân viên xác nhận đã"
     " nhận tiền để hệ thống hoàn tất giao dịch và phát hành vé."),

    ("3.4.11. Bán vé – Bước 8: Hoàn thành",
     "Thông báo giao dịch thành công với mã hóa đơn và danh sách mã vé vừa phát hành. Màn"
     " hình cung cấp nút in vé hoặc in hóa đơn và tùy chọn bắt đầu giao dịch bán vé mới."),

    ("3.4.12. Màn hình Quản lý nhân viên",
     "Hiển thị danh sách toàn bộ nhân viên với bộ lọc theo tên, vai trò, ga làm việc và"
     " trạng thái hoạt động. Quản trị viên có thể thêm nhân viên mới, chỉnh sửa thông tin và"
     " kích hoạt hoặc vô hiệu hóa tài khoản. Chỉ vai trò Quản trị viên mới có quyền truy"
     " cập màn hình này."),

    ("3.4.13. Màn hình Quản lý khách hàng",
     "Danh sách khách hàng với thông tin họ tên, số điện thoại, số CCCD, email và trạng thái"
     " tài khoản. Hỗ trợ tìm kiếm theo nhiều tiêu chí; nhân viên có thể thêm mới hoặc cập"
     " nhật thông tin khách hàng khi có yêu cầu."),

    ("3.4.14. Màn hình Quản lý vé",
     "Tra cứu và quản lý tất cả vé đã phát hành với bộ lọc đa tiêu chí (mã vé, mã hóa đơn,"
     " tuyến, ngày đi, trạng thái). Hỗ trợ xem chi tiết từng vé và thực hiện hoàn vé khi"
     " khách hàng có yêu cầu hợp lệ."),

    ("3.4.15. Màn hình Quản lý hóa đơn",
     "Danh sách hóa đơn toàn hệ thống có thể lọc theo mã hóa đơn, tên khách hàng, tên nhân"
     " viên lập và khoảng thời gian. Cho phép mở xem chi tiết từng hóa đơn bao gồm danh sách"
     " vé kèm và tổng tiền giao dịch."),

    ("3.4.16. Màn hình Quản lý giá",
     "Liệt kê các biểu giá được thiết lập cho từng tuyến đường. Mỗi biểu giá hiển thị tuyến"
     " áp dụng, khoảng thời gian hiệu lực, đơn giá theo từng loại ghế và trạng thái hiện tại"
     " (đang áp dụng hoặc hết hạn). Cho phép tạo mới hoặc điều hướng sang màn hình chỉnh sửa"
     " chi tiết."),

    ("3.4.17. Màn hình Thêm/Chỉnh sửa biểu giá",
     "Màn hình chi tiết để tạo mới hoặc chỉnh sửa một biểu giá: chọn tuyến đường áp dụng,"
     " thiết lập khoảng thời gian hiệu lực và nhập đơn giá riêng cho từng loại ghế (Ngồi"
     " cứng, Ngồi mềm, Nằm cứng, Nằm mềm, VIP). Hệ thống kiểm tra tính hợp lệ và không cho"
     " phép trùng khoảng thời gian trên cùng một tuyến."),

    ("3.4.18. Màn hình Quản lý khuyến mãi",
     "Danh sách các chương trình khuyến mãi với thông tin tên, mô tả, thời gian áp dụng và"
     " trạng thái hoạt động. Hỗ trợ thêm mới, chỉnh sửa và bật/tắt từng chương trình khuyến"
     " mãi."),

    ("3.4.19. Màn hình Thêm/Chỉnh sửa khuyến mãi",
     "Màn hình chi tiết để cấu hình chương trình khuyến mãi: nhập tên, mô tả, thiết lập"
     " khoảng thời gian hiệu lực và cấu hình điều kiện áp dụng theo tuyến hoặc loại ghế kèm"
     " mức phần trăm giảm giá tương ứng."),

    ("3.4.20. Màn hình Quản lý đoàn tàu",
     "Danh sách đoàn tàu với thông tin mã đoàn tàu, số lượng toa hiện tại, tổng sức chứa và"
     " trạng thái hoạt động. Hỗ trợ thêm đoàn tàu mới và điều hướng sang màn hình thiết lập"
     " chi tiết để cấu hình toa và đầu máy."),

    ("3.4.21. Màn hình Thiết lập/Chỉnh sửa đoàn tàu",
     "Màn hình chi tiết để cấu hình thành phần đoàn tàu: thêm, xóa và sắp xếp lại thứ tự"
     " các toa tàu, đồng thời gán đầu máy kéo tương ứng. Sơ đồ đoàn tàu được hiển thị trực"
     " quan theo chiều ngang; thông tin tổng sức chứa cập nhật tự động khi thay đổi cấu hình."),

    ("3.4.22. Màn hình Quản lý toa tàu",
     "Danh sách toa tàu với thông tin loại toa (Ngồi cứng, Ngồi mềm, Nằm cứng, Nằm mềm,"
     " VIP), số lượng ghế và trạng thái. Hỗ trợ thêm toa mới kèm sơ đồ ghế và xem chi tiết"
     " danh sách ghế của từng toa."),

    ("3.4.23. Màn hình Quản lý đầu máy",
     "Danh sách đầu máy với các thông số kỹ thuật: mã đầu máy, loại đầu máy, năm sản xuất,"
     " công suất và trạng thái hoạt động. Hỗ trợ thêm mới và cập nhật thông tin đầu máy trong"
     " hệ thống."),

    ("3.4.24. Màn hình Quản lý tuyến đường",
     "Danh sách tuyến tàu với thông tin ga đi, ga đến, khoảng cách (km) và trạng thái khai"
     " thác. Quản trị viên có thể thêm tuyến mới, chỉnh sửa thông tin hoặc vô hiệu hóa tuyến"
     " không còn hoạt động."),

    ("3.4.25. Màn hình Quản lý lịch chạy",
     "Danh sách lịch trình tàu với bộ lọc đa tiêu chí theo tuyến, đoàn tàu và khoảng ngày"
     " giờ. Hỗ trợ thêm lịch chạy mới, chỉnh sửa giờ khởi hành và giờ đến dự kiến, cùng"
     " theo dõi trạng thái từng chuyến tàu."),

    ("3.4.26. Màn hình Thống kê & Báo cáo",
     "Màn hình phân tích dữ liệu kinh doanh với biểu đồ doanh thu theo tháng, số lượng vé"
     " bán theo tuyến và loại ghế, tỷ lệ hoàn vé cùng xu hướng tăng trưởng theo thời gian."
     " Hỗ trợ lọc linh hoạt theo khoảng thời gian và xuất báo cáo ra file."),

    ("3.4.27. Màn hình Thông tin cá nhân",
     "Hiển thị hồ sơ của nhân viên đang đăng nhập bao gồm thông tin cá nhân, vai trò và ga"
     " làm việc. Nhân viên có thể cập nhật thông tin liên hệ và thay đổi mật khẩu tài khoản"
     " tại đây."),
]


def build_34_block():
    parts = []
    for title, desc in SCREENS:
        parts.append(_h3(title))
        parts.append(_body(desc))
    return "".join(parts)


# ─── Chapter 4 ────────────────────────────────────────────────────────────────

def build_41_block():
    parts = [_body(
        "Sau quá trình phân tích, thiết kế và hiện thực, đề tài đã xây dựng thành công"
        " ứng dụng desktop quản lý bán vé tàu Azure Rail, đáp ứng các yêu cầu chức năng"
        " và phi chức năng đã đặt ra. Ứng dụng được phát triển trên nền tảng Java Swing"
        " với cơ sở dữ liệu Microsoft SQL Server, áp dụng kiến trúc phân lớp rõ ràng và"
        " giao diện được thiết kế theo phong cách Notion hiện đại. Cụ thể, đề tài đã đạt"
        " được các kết quả sau:"
    )]
    bullets = [
        "Hệ thống đăng nhập và phân quyền ba cấp (Quản trị, Điều phối, Bán vé) kiểm soát"
        " truy cập chặt chẽ cho từng nhóm chức năng.",
        "Luồng bán vé đa bước hoàn chỉnh gồm 8 bước: tìm kiếm chuyến tàu, chọn chỗ ngồi,"
        " nhập thông tin khách hàng, áp dụng khuyến mãi, xác nhận và thanh toán (tiền mặt"
        " hoặc chuyển khoản).",
        "Quản lý toàn diện hạ tầng tàu bao gồm đầu máy, toa tàu, đoàn tàu, tuyến đường"
        " và lịch chạy với sơ đồ ghế trực quan.",
        "Hệ thống biểu giá và chương trình khuyến mãi linh hoạt với cấu hình chi tiết theo"
        " tuyến đường và loại ghế.",
        "Quản lý nhân viên và khách hàng tích hợp trong cùng ứng dụng.",
        "Tra cứu vé và hóa đơn với bộ lọc đa chiều và khả năng thực hiện hoàn vé.",
        "Màn hình thống kê và báo cáo với biểu đồ phân tích doanh thu, xu hướng bán vé"
        " và hiệu suất hoạt động.",
    ]
    for b in bullets:
        parts.append(_bullet(b))
    return "".join(parts)


def build_42_block():
    parts = [_body(
        "Bên cạnh các kết quả đạt được, đồ án vẫn còn một số hạn chế cần được ghi nhận"
        " và khắc phục trong các phiên bản tiếp theo:"
    )]
    bullets = [
        "Ứng dụng chỉ chạy trên môi trường desktop, chưa có phiên bản web hoặc di động"
        " để hỗ trợ nhân viên làm việc từ xa hoặc quản lý đa chi nhánh.",
        "Chưa tích hợp cổng thanh toán điện tử; hệ thống chỉ ghi nhận thủ công các giao"
        " dịch tiền mặt và chuyển khoản.",
        "Dữ liệu chưa được đồng bộ thời gian thực giữa nhiều quầy vé hoạt động đồng thời,"
        " có thể dẫn đến xung đột đặt ghế trong môi trường nhiều nhân viên.",
        "Chưa có chức năng gửi thông báo tự động (SMS, email) xác nhận đặt vé hoặc nhắc"
        " nhở lịch tàu đến khách hàng.",
        "Chức năng xuất báo cáo còn hạn chế về định dạng và mức độ chi tiết của dữ liệu"
        " phân tích.",
        "Chưa hỗ trợ quét mã vạch hoặc mã QR để xác thực và tra cứu vé nhanh tại quầy.",
    ]
    for b in bullets:
        parts.append(_bullet(b))
    return "".join(parts)


def build_43_block():
    parts = [_body(
        "Từ những hạn chế hiện tại, nhóm đề xuất các hướng phát triển ưu tiên cho phiên"
        " bản tiếp theo của ứng dụng:"
    )]
    bullets = [
        "Phát triển phiên bản web (Spring Boot + ReactJS) và ứng dụng di động (Flutter)"
        " để nhân viên có thể xử lý nghiệp vụ từ mọi thiết bị và mọi địa điểm.",
        "Tích hợp cổng thanh toán điện tử (VNPay, MoMo, ZaloPay) nhằm hỗ trợ khách hàng"
        " thanh toán trực tuyến và giảm thiểu giao dịch tiền mặt.",
        "Xây dựng module đồng bộ dữ liệu thời gian thực sử dụng WebSocket giữa nhiều quầy"
        " bán vé, đảm bảo tính nhất quán và tránh đặt trùng ghế.",
        "Tích hợp dịch vụ gửi thông báo tự động qua SMS và email sau mỗi giao dịch và"
        " nhắc nhở trước giờ tàu chạy.",
        "Mở rộng chức năng thống kê với khả năng dự báo nhu cầu theo tuyến và mùa vụ"
        " bằng các thuật toán học máy.",
        "Triển khai ứng dụng trên nền tảng đám mây để hỗ trợ nhiều ga đồng thời, sao lưu"
        " dữ liệu tự động và tăng khả năng mở rộng theo quy mô.",
    ]
    for b in bullets:
        parts.append(_bullet(b))
    return "".join(parts)


# ─── Chapter 5 ────────────────────────────────────────────────────────────────

def build_ch5_block():
    parts = []
    parts.append(_body(
        "Quản lý bán vé tàu là nghiệp vụ quan trọng nhưng còn thiếu giải pháp phần mềm"
        " chuyên biệt phù hợp với quy mô vừa và nhỏ tại Việt Nam. Trong khi các tập đoàn"
        " đường sắt lớn đã đầu tư vào hệ thống ERP phức tạp và chi phí cao, phần lớn các"
        " ga tư nhân, tàu du lịch và tuyến đường sắt địa phương vẫn quản lý bằng phương"
        " thức thủ công. Đây là khoảng trống thị trường mà sản phẩm Azure Rail hướng đến."
    ))
    parts.append(_body(
        "Đối tượng khách hàng mục tiêu gồm: các ga tàu tư nhân và tàu du lịch có quy mô"
        " vừa và nhỏ, các công ty điều hành tuyến tàu nội địa theo hợp đồng, và các trung"
        " tâm vé tập trung quản lý nhiều tuyến đường sắt địa phương. Ước tính tại Việt Nam"
        " có hơn 200 điểm ga và hàng chục đơn vị vận tải đường sắt tư nhân chưa được số"
        " hóa hoàn toàn, tạo ra thị trường tiềm năng lớn."
    ))
    parts.append(_body(
        "Mô hình kinh doanh đề xuất là Phần mềm dịch vụ (SaaS) theo gói thuê bao hàng"
        " tháng hoặc hàng năm. Các gói được phân cấp theo quy mô: gói Cơ bản cho một quầy"
        " vé, gói Tiêu chuẩn cho từ 2–5 quầy và gói Doanh nghiệp hỗ trợ không giới hạn"
        " quầy kèm tích hợp API. Ngoài phí thuê bao, doanh thu còn đến từ phí triển khai"
        " ban đầu, đào tạo nhân viên và hỗ trợ kỹ thuật theo hợp đồng bảo trì."
    ))
    parts.append(_body(
        "Điểm khác biệt của Azure Rail so với các giải pháp hiện có bao gồm: giao diện"
        " hiện đại theo chuẩn Notion giúp giảm thời gian đào tạo nhân viên; tích hợp hoàn"
        " chỉnh từ quản lý hạ tầng tàu đến bán vé và thống kê trong một nền tảng duy nhất;"
        " chi phí đầu tư thấp hơn các giải pháp ERP truyền thống; và khả năng triển khai"
        " nhanh với tùy biến theo đặc thù từng đơn vị vận hành."
    ))
    parts.append(_body(
        "Kế hoạch triển khai giai đoạn đầu gồm ba bước: (1) Hoàn thiện và kiểm thử thực"
        " tế phiên bản desktop tại một đơn vị khách hàng thí điểm trong quý đầu; (2) Phát"
        " triển phiên bản web và ứng dụng di động trong sáu tháng tiếp theo; (3) Mở rộng"
        " thị trường với đội ngũ kinh doanh và chăm sóc khách hàng chuyên biệt. Nhân lực"
        " yêu cầu giai đoạn đầu tối thiểu gồm hai kỹ sư phần mềm, một nhân viên kinh doanh"
        " và một chuyên viên hỗ trợ khách hàng."
    ))
    parts.append(_body(
        "Các rủi ro chính cần quản lý bao gồm: cạnh tranh từ các nhà cung cấp phần mềm"
        " quản lý vận tải lớn có nguồn lực mạnh; thay đổi chính sách pháp lý liên quan đến"
        " dữ liệu hành khách và vận tải đường sắt; và thách thức trong việc thuyết phục"
        " doanh nghiệp truyền thống chuyển đổi số. Để giảm thiểu rủi ro, nhóm dự kiến ưu"
        " tiên xây dựng quan hệ đối tác với các nhà tích hợp hệ thống địa phương và tham"
        " gia các chương trình hỗ trợ khởi nghiệp trong lĩnh vực công nghệ giao thông."
    ))
    return "".join(parts)


# ─── main ─────────────────────────────────────────────────────────────────────

def find_para_end(xml, from_idx):
    """Trả về vị trí ngay SAU </w:p> đầu tiên kể từ from_idx."""
    return xml.find("</w:p>", from_idx) + len("</w:p>")

def find_para_start(xml, before_idx):
    """Trả về vị trí của <w:p cuối cùng trước before_idx."""
    return xml.rfind("<w:p ", 0, before_idx)


def main():
    with zipfile.ZipFile(SRC, "r") as z:
        xml = z.read("word/document.xml").decode("utf-8")

    # ── PASS 1: Section 3.4 ──────────────────────────────────────────────────
    # Tìm heading "Màn hình của ứng dụng" (chỉ có 1 lần xuất hiện)
    mh_idx   = xml.find("Màn hình của ứng dụng")
    mh_end   = find_para_end(xml, mh_idx)          # kết thúc para heading
    empty_end = find_para_end(xml, mh_end)          # kết thúc para rỗng sau heading
    # Tìm heading "Kiểm thử chức năng" (tìm lần đầu SAU vị trí hiện tại)
    kt_idx       = xml.find("Kiểm thử chức năng", mh_end)
    kt_p_start   = find_para_start(xml, kt_idx)
    # Thay thế vùng rỗng bằng 27 sub-sections
    xml = xml[:empty_end] + build_34_block() + xml[kt_p_start:]

    # ── PASS 2: Chapter 4 — tìm từ offset 1_000_000 để bỏ qua TOC ───────────
    SEARCH_FROM = 1_000_000

    # 4.1
    kq_idx      = xml.find("4.1 Kết quả đạt được", SEARCH_FROM)
    kq_end      = find_para_end(xml, kq_idx)
    hc_idx      = xml.find("4.2 Hạn chế", kq_end)
    hc_p_start  = find_para_start(xml, hc_idx)
    xml = xml[:kq_end] + build_41_block() + xml[hc_p_start:]

    # 4.2
    hc_idx2     = xml.find("4.2 Hạn chế", SEARCH_FROM)
    hc_end      = find_para_end(xml, hc_idx2)
    hpt_idx     = xml.find("4.3 Hướng phát triển", hc_end)
    hpt_p_start = find_para_start(xml, hpt_idx)
    xml = xml[:hc_end] + build_42_block() + xml[hpt_p_start:]

    # 4.3
    hpt_idx2    = xml.find("4.3 Hướng phát triển", SEARCH_FROM)
    hpt_end     = find_para_end(xml, hpt_idx2)
    kn_idx      = xml.find("KHỞI NGHIỆP", hpt_end)
    kn_p_start  = find_para_start(xml, kn_idx)
    xml = xml[:hpt_end] + build_43_block() + xml[kn_p_start:]

    # ── PASS 3: Chapter 5 ────────────────────────────────────────────────────
    kn_idx2     = xml.find("KHỞI NGHIỆP", SEARCH_FROM)
    kn_end      = find_para_end(xml, kn_idx2)
    tl_idx      = xml.find("TÀI LIỆU THAM KHẢO", kn_end)
    tl_p_start  = find_para_start(xml, tl_idx)
    xml = xml[:kn_end] + build_ch5_block() + xml[tl_p_start:]

    # ── Pack to v11 ──────────────────────────────────────────────────────────
    tmp = DST + ".tmp"
    with zipfile.ZipFile(SRC, "r") as zin, \
         zipfile.ZipFile(tmp, "w", zipfile.ZIP_DEFLATED) as zout:
        for item in zin.namelist():
            data = (xml.encode("utf-8") if item == "word/document.xml"
                    else zin.read(item))
            zout.writestr(item, data)
    os.replace(tmp, DST)

    # ── Sanity checks ────────────────────────────────────────────────────────
    assert "3.4.1. Màn hình Đăng nhập"         in xml
    assert "3.4.27. Màn hình Thông tin cá nhân" in xml
    assert "3.4.14. Màn hình Quản lý vé"        in xml
    # ch4
    assert "Hệ thống đăng nhập và phân quyền"   in xml
    assert "Chưa tích hợp cổng thanh toán"       in xml
    assert "WebSocket"                            in xml
    # ch5
    assert "Azure Rail"                           in xml
    assert "SaaS"                                 in xml

    size = os.path.getsize(DST)
    print(f"Done → {DST}  ({size:,} bytes)")
    print(f"Screens inserted: {len(SCREENS)}")


if __name__ == "__main__":
    main()
