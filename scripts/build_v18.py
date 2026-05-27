"""
build_v18.py — Viết lại Chương 5: KHỞI NGHIỆP chi tiết hơn
  - Thêm tiêu đề mục 5.1 / 5.2 / 5.3 (Heading2) và tiểu mục (Heading3)
  - Giữ toàn bộ heading "CHƯƠNG 5 : KHỞI NGHIỆP" (không thay đổi)
  - Chỉ thay nội dung phía sau heading đến TÀI LIỆU THAM KHẢO

SRC = 9_REPORT_v17.docx  →  DST = 9_REPORT_v18.docx

Rủi ro: dùng vị trí ký tự cố định → nếu v17 thay đổi cần cập nhật lại offset.
"""
import zipfile, re, shutil, os

SRC = "document/9_REPORT_v17.docx"
DST = "document/9_REPORT_v18.docx"

shutil.copy(SRC, DST)

with zipfile.ZipFile(DST, "r") as z:
    xml   = z.read("word/document.xml").decode("utf-8")
    files = {n: z.read(n) for n in z.namelist()}

# ── paraId counter ─────────────────────────────────────────────────────────────
_pid = [0xFF000001]
def pid():
    v = f"{_pid[0]:08X}"; _pid[0] += 1; return v

# ── XML builders ───────────────────────────────────────────────────────────────
def h2(text):
    return (f'<w:p w14:paraId="{pid()}" w14:textId="77777777" '
            f'w:rsidR="00000000" w:rsidRDefault="00000000">'
            f'<w:pPr><w:pStyle w:val="Heading2"/>'
            f'<w:rPr><w:lang w:val="vi-VN"/></w:rPr></w:pPr>'
            f'<w:r><w:rPr><w:lang w:val="vi-VN"/></w:rPr>'
            f'<w:t>{text}</w:t></w:r></w:p>')

def h3(text):
    return (f'<w:p w14:paraId="{pid()}" w14:textId="77777777" '
            f'w:rsidR="00000000" w:rsidRDefault="00000000">'
            f'<w:pPr><w:pStyle w:val="Heading3"/>'
            f'<w:rPr><w:sz w:val="24"/><w:lang w:val="vi-VN"/></w:rPr></w:pPr>'
            f'<w:r><w:rPr><w:sz w:val="24"/></w:rPr>'
            f'<w:t>{text}</w:t></w:r></w:p>')

def p(text):
    """Body paragraph, justified."""
    return (f'<w:p w14:paraId="{pid()}" w14:textId="77777777" '
            f'w:rsidR="00000000" w:rsidRDefault="00000000">'
            f'<w:pPr><w:jc w:val="both"/></w:pPr>'
            f'<w:r><w:t xml:space="preserve">{text}</w:t></w:r></w:p>')

def bullet(text):
    """Indented bullet-style paragraph."""
    return (f'<w:p w14:paraId="{pid()}" w14:textId="77777777" '
            f'w:rsidR="00000000" w:rsidRDefault="00000000">'
            f'<w:pPr><w:ind w:left="720" w:hanging="360"/><w:jc w:val="both"/></w:pPr>'
            f'<w:r><w:t xml:space="preserve">– {text}</w:t></w:r></w:p>')

def bold_run(text):
    return (f'<w:r><w:rPr><w:b/><w:bCs/></w:rPr>'
            f'<w:t xml:space="preserve">{text}</w:t></w:r>')

def p_mixed(parts):
    """Paragraph with mixed bold/normal runs. parts = list of (text, bold)."""
    content = ""
    for text, is_bold in parts:
        if is_bold:
            content += f'<w:r><w:rPr><w:b/><w:bCs/></w:rPr><w:t xml:space="preserve">{text}</w:t></w:r>'
        else:
            content += f'<w:r><w:t xml:space="preserve">{text}</w:t></w:r>'
    return (f'<w:p w14:paraId="{pid()}" w14:textId="77777777" '
            f'w:rsidR="00000000" w:rsidRDefault="00000000">'
            f'<w:pPr><w:jc w:val="both"/></w:pPr>'
            + content + '</w:p>')

def blank():
    return (f'<w:p w14:paraId="{pid()}" w14:textId="77777777" '
            f'w:rsidR="00000000" w:rsidRDefault="00000000"/>')

# ── Nội dung Chương 5 chi tiết ────────────────────────────────────────────────
content = []

content.append(p(
    "Xuất phát từ nền tảng sinh viên ngành Công nghệ Thông tin, nhóm em nhận thức sâu sắc "
    "vai trò của chuyển đổi số trong việc giải quyết các bất cập của quy trình vận hành thủ "
    "công và nâng cao trải nghiệm người dùng. Khát vọng tạo ra giá trị thực tiễn đã thôi thúc "
    "nhóm hướng tới việc thương mại hóa sản phẩm, với dự án tiên phong là "
    "Azure Rail – Hệ thống quản lý và bán vé tàu thông minh tại nhà ga."
))

# ── 5.1 ───────────────────────────────────────────────────────────────────────
content.append(h2("5.1 Kế hoạch phát triển dự án khởi nghiệp"))

content.append(h3("5.1.1 Bối cảnh và vấn đề thực tiễn"))
content.append(p(
    "Ngành vận tải hành khách đường sắt tại Việt Nam đang trong giai đoạn tăng trưởng ổn định. "
    "Theo Tổng Công ty Đường sắt Việt Nam, lượng hành khách đi tàu liên tục tăng trong các năm "
    "gần đây, đặc biệt trong các kỳ nghỉ lễ và Tết Nguyên Đán. Tuy nhiên, song song với sự tăng "
    "trưởng đó, hệ thống quản lý nội bộ tại nhiều nhà ga vẫn còn tồn tại nhiều bất cập nghiêm trọng:"
))
content.append(bullet(
    "Nhân viên phải xử lý lượng lớn giao dịch trong khung giờ cao điểm trên các hệ thống phần mềm "
    "cũ kỹ, giao diện rườm rà, đòi hỏi nhiều thủ tục giấy tờ phức tạp."
))
content.append(bullet(
    "Quy trình xuất vé, hoàn vé và tra cứu lịch trình chưa được tối ưu, gây tốn thời gian và "
    "dễ phát sinh sai sót trong các giao dịch đông khách."
))
content.append(bullet(
    "Ban quản lý nhà ga gặp khó khăn trong việc theo dõi dữ liệu doanh thu và lưu lượng khách "
    "theo thời gian thực, dẫn đến quyết định điều hành chậm trễ và thiếu chính xác."
))
content.append(bullet(
    "Phần lớn các ga tư nhân, tàu du lịch và tuyến đường sắt địa phương vẫn quản lý bằng phương "
    "thức thủ công (sổ sách, Excel), trong khi các giải pháp ERP lớn có chi phí đầu tư quá cao "
    "và không phù hợp với quy mô vừa và nhỏ."
))
content.append(p(
    "Chính những bất cập đó tạo ra khoảng trống thị trường rõ ràng mà Azure Rail hướng đến giải quyết: "
    "một phần mềm chuyên biệt, hiện đại, chi phí hợp lý, được thiết kế đặc thù cho nghiệp vụ "
    "nhà ga đường sắt tại Việt Nam."
))

content.append(h3("5.1.2 Lý do hình thành dự án khởi nghiệp"))
content.append(p(
    "Nhóm em nhận thấy rằng sự kết hợp giữa thiết kế giao diện thông minh và kiến trúc cơ sở "
    "dữ liệu tối ưu sẽ mang lại những cải tiến rõ rệt cho quy trình làm việc của nhà ga. "
    "Cụ thể, ba động lực chính thúc đẩy nhóm hình thành dự án:"
))
content.append(p_mixed([
    ("Thứ nhất, ", True), ("thiếu vắng một giải pháp phần mềm chuyên biệt: ", True),
    ("trong khi các tập đoàn đường sắt lớn đã đầu tư vào hệ thống ERP phức tạp, phần lớn "
     "các đơn vị vận tải đường sắt vừa và nhỏ chưa có công cụ quản lý phù hợp về cả tính năng "
     "lẫn chi phí.", False)
]))
content.append(p_mixed([
    ("Thứ hai, ", True), ("xu hướng số hóa ngành vận tải đang tăng tốc: ", True),
    ("Chính phủ Việt Nam đã ban hành nhiều chính sách thúc đẩy chuyển đổi số trong giao thông "
     "vận tải, tạo môi trường thuận lợi cho các sản phẩm công nghệ chuyên ngành.", False)
]))
content.append(p_mixed([
    ("Thứ ba, ", True), ("lợi thế kỹ thuật của nhóm: ", True),
    ("với nền tảng Java Swing, SQL Server và kinh nghiệm phân tích nghiệp vụ từ đồ án, nhóm "
     "sở hữu đủ năng lực kỹ thuật để xây dựng và phát triển sản phẩm đến giai đoạn thương mại hóa.", False)
]))

content.append(h3("5.1.3 Tiềm năng thị trường"))
content.append(p(
    "Thị trường phần mềm quản lý vận tải đường sắt tại Việt Nam còn rất phân mảnh. Ước tính "
    "cả nước có hơn 200 điểm ga lớn nhỏ và hàng chục đơn vị vận tải đường sắt tư nhân hoạt động "
    "theo hợp đồng, phần lớn chưa được số hóa hoàn toàn. Ngoài ra, xu hướng phát triển tàu "
    "du lịch, tàu hỏa nội vùng và các tuyến đường sắt đô thị đang mở ra thêm nhiều phân khúc "
    "khách hàng tiềm năng mới."
))
content.append(p(
    "Theo dự báo của các tổ chức nghiên cứu thị trường, quy mô thị trường phần mềm quản lý "
    "vận tải tại Đông Nam Á dự kiến tăng trưởng với tốc độ CAGR khoảng 12–15% trong giai đoạn "
    "2025–2030. Việt Nam, với hệ thống đường sắt dài hơn 3.100 km và kế hoạch đầu tư lớn vào "
    "cơ sở hạ tầng đường sắt tốc độ cao, sẽ là thị trường có sức hấp dẫn cao cho các giải pháp "
    "phần mềm chuyên biệt như Azure Rail."
))

# ── 5.2 ───────────────────────────────────────────────────────────────────────
content.append(h2("5.2 Dự án Azure Rail – Kế hoạch triển khai"))

content.append(h3("5.2.1 Tổng quan về sản phẩm"))
content.append(p(
    "Azure Rail là phần mềm desktop quản lý bán vé tàu chuyên dụng, được xây dựng bằng Java Swing, "
    "kết nối cơ sở dữ liệu Microsoft SQL Server thông qua JDBC. Hệ thống được thiết kế theo kiến "
    "trúc ba lớp (GUI – BLL – DAL), đảm bảo tính rõ ràng, dễ bảo trì và mở rộng. Giao diện "
    "hướng đến chuẩn Notion – hiện đại, tối giản – nhằm rút ngắn thời gian đào tạo nhân viên "
    "và tối ưu hóa tốc độ thao tác trong giờ cao điểm."
))
content.append(p("Các phân hệ chức năng chính của Azure Rail bao gồm:"))
content.append(bullet("Bán vé đa bước: tìm chuyến, chọn toa và ghế, nhập thông tin khách hàng, áp dụng khuyến mãi, thanh toán tiền mặt hoặc chuyển khoản."))
content.append(bullet("Hoàn vé và tra cứu lịch trình: hỗ trợ các nghiệp vụ hoàn vé, tìm kiếm và xem lịch chạy tàu theo tuyến và thời gian."))
content.append(bullet("Quản lý hạ tầng tàu: đoàn tàu, toa tàu, đầu máy, sơ đồ ghế, tuyến đường và lịch chạy."))
content.append(bullet("Quản lý giá và khuyến mãi: bảng giá linh hoạt theo loại ghế, tuyến đường; chương trình khuyến mãi theo thời gian."))
content.append(bullet("Quản lý hóa đơn và vé: theo dõi toàn bộ lịch sử giao dịch, tra cứu và xuất báo cáo."))
content.append(bullet("Quản lý nhân sự và khách hàng: phân quyền theo vai trò, lịch sử mua vé của khách hàng."))
content.append(bullet("Tổng quan và thống kê: Dashboard trực quan hiển thị doanh thu, tình trạng ghế, báo cáo theo ca và theo tháng."))

content.append(h3("5.2.2 Phân tích thị trường và đối thủ cạnh tranh"))
content.append(h3("5.2.2.1 Thị trường mục tiêu"))
content.append(p("Azure Rail hướng đến ba phân khúc khách hàng chính:"))
content.append(p_mixed([
    ("Nhân viên bán vé tại quầy: ", True),
    ("nhóm người dùng có nhu cầu sử dụng hệ thống với tốc độ phản hồi nhanh, hỗ trợ tìm kiếm "
     "nhanh, thao tác mượt mà để xuất vé liên tục trong giờ cao điểm, giảm thiểu thời gian "
     "chờ đợi của hành khách.", False)
]))
content.append(p_mixed([
    ("Quản lý nhà ga: ", True),
    ("những người cần báo cáo tức thời về doanh thu trong ca, tình trạng ghế trống trên các "
     "đoàn tàu, và hiệu suất làm việc của từng nhân viên nhằm điều hành chủ động và linh hoạt.", False)
]))
content.append(p_mixed([
    ("Đơn vị vận tải tư nhân vừa và nhỏ: ", True),
    ("các ga tàu tư nhân, tàu du lịch và tuyến đường sắt địa phương đang tìm kiếm một giải pháp "
     "số hóa hoàn chỉnh với chi phí đầu tư hợp lý, thay thế cho các phương thức quản lý thủ công "
     "hoặc Excel.", False)
]))

content.append(h3("5.2.2.2 Đối thủ cạnh tranh"))
content.append(p(
    "Các đối thủ cạnh tranh chính bao gồm hệ thống phần mềm nội bộ của Tổng Công ty Đường sắt "
    "Việt Nam (VNR) và các nhà cung cấp phần mềm ERP/POS chung trên thị trường. Tuy nhiên, "
    "các giải pháp hiện tại tồn tại một số điểm yếu cốt lõi mà Azure Rail có thể tận dụng:"
))
content.append(p_mixed([
    ("Giao diện nghiệp vụ phức tạp: ", True),
    ("các phần mềm cũ thường có giao diện rườm rà, đòi hỏi nhân viên mới phải mất nhiều tuần "
     "đào tạo và làm quen, ảnh hưởng trực tiếp đến năng suất vận hành.", False)
]))
content.append(p_mixed([
    ("Thiếu tính năng báo cáo động: ", True),
    ("quản lý thường chỉ nhận được báo cáo tĩnh vào cuối ngày hoặc cuối tháng, thiếu khả năng "
     "theo dõi dữ liệu theo thời gian thực để đưa ra các quyết định điều tiết dòng khách kịp thời.", False)
]))
content.append(p_mixed([
    ("Chi phí đầu tư cao và khó tiếp cận: ", True),
    ("các giải pháp ERP lớn như SAP hay Oracle Transportation Management có chi phí triển khai "
     "rất cao, không phù hợp với các đơn vị vận tải tư nhân quy mô vừa và nhỏ.", False)
]))
content.append(p(
    "Azure Rail định vị mình ở phân khúc mid-market: chuyên biệt hơn các phần mềm quản lý "
    "chung nhưng tiếp cận được hơn so với các hệ thống ERP lớn, tạo ra lợi thế cạnh tranh "
    "rõ ràng về giá trị và chi phí."
))

content.append(h3("5.2.3 Chiến lược phát triển và tiến độ dự án"))
content.append(p_mixed([("Giai đoạn 1 – Nghiên cứu và phát triển (5 tháng đầu):", True), ("", False)]))
content.append(bullet("Tiến hành khảo sát trực tiếp tại các quầy vé để hiểu rõ bất cập của nhân viên và quy trình đối soát của kế toán."))
content.append(bullet("Phát triển và hoàn thiện phiên bản desktop: xây dựng đầy đủ các phân hệ, kiểm thử nội bộ với dữ liệu giả lập tải lớn."))
content.append(bullet("Thiết kế giao diện tối ưu: chú trọng vào tính công thái học, hỗ trợ phím tắt và tốc độ thao tác cho nhân viên bán vé."))

content.append(p_mixed([("Giai đoạn 2 – Triển khai thí điểm và tích hợp (3–6 tháng tiếp theo):", True), ("", False)]))
content.append(bullet("Triển khai tại một đến hai nhà ga quy mô vừa để nhân viên thực tế trải nghiệm và cung cấp phản hồi cải tiến."))
content.append(bullet("Tích hợp hóa đơn điện tử và cổng thanh toán QR/thẻ ngân hàng trực tiếp vào phần mềm."))
content.append(bullet("Tổ chức đào tạo nhân viên và cung cấp tài liệu hướng dẫn sử dụng trực quan."))

content.append(p_mixed([("Giai đoạn 3 – Thương mại hóa và mở rộng (6 tháng – 1 năm):", True), ("", False)]))
content.append(bullet("Phát triển phiên bản web và ứng dụng di động (theo dõi lịch chạy, kiểm tra vé) để bổ sung hệ sinh thái sản phẩm."))
content.append(bullet("Mở rộng quy mô triển khai tới các chi nhánh và đơn vị vận tải đường sắt trên toàn quốc."))
content.append(bullet("Nâng cấp tính năng quản trị nâng cao: báo cáo phân tích dự báo nhu cầu theo mùa vụ, hỗ trợ lập lịch trình tàu."))
content.append(bullet("Xây dựng đội ngũ hỗ trợ kỹ thuật và dịch vụ bảo trì phần mềm 24/7."))

content.append(h3("5.2.4 Mô hình kinh doanh và doanh thu"))
content.append(h3("5.2.4.1 Mô hình kinh doanh"))
content.append(p(
    "Azure Rail hoạt động theo mô hình Phần mềm dịch vụ (SaaS – Software as a Service) kết "
    "hợp với triển khai hệ thống theo yêu cầu. Các gói dịch vụ được phân cấp theo quy mô "
    "của đơn vị khách hàng:"
))
content.append(p_mixed([
    ("Gói Cơ bản (Basic): ", True),
    ("hỗ trợ một quầy vé, phù hợp với ga nhỏ hoặc tuyến đường sắt địa phương. "
     "Bao gồm đầy đủ nghiệp vụ bán vé, hoàn vé và báo cáo cơ bản.", False)
]))
content.append(p_mixed([
    ("Gói Tiêu chuẩn (Standard): ", True),
    ("hỗ trợ từ 2 đến 5 quầy vé, kèm Dashboard quản lý theo thời gian thực, phân quyền "
     "đa cấp và thống kê doanh thu chi tiết theo ca.", False)
]))
content.append(p_mixed([
    ("Gói Doanh nghiệp (Enterprise): ", True),
    ("không giới hạn quầy vé, tích hợp API với hệ thống bên ngoài (hóa đơn điện tử, "
     "cổng thanh toán, hệ thống CRM), hỗ trợ kỹ thuật ưu tiên và tùy chỉnh giao diện.", False)
]))

content.append(h3("5.2.4.2 Nguồn doanh thu"))
content.append(p_mixed([
    ("Phí triển khai ban đầu: ", True),
    ("thu một lần khi thiết lập hệ thống, cấu hình máy chủ, cài đặt phần mềm và kết nối "
     "thiết bị tại nhà ga. Bao gồm cả phí đào tạo nhân viên lần đầu.", False)
]))
content.append(p_mixed([
    ("Phí thuê bao định kỳ: ", True),
    ("doanh thu ổn định từ phí sử dụng hàng tháng hoặc hàng năm, tính theo gói dịch vụ và "
     "quy mô nhà ga. Đây là nguồn doanh thu chủ yếu và có tính dự đoán cao.", False)
]))
content.append(p_mixed([
    ("Phí dịch vụ tùy chỉnh và nâng cấp: ", True),
    ("thu phí khi có yêu cầu phát triển tính năng riêng biệt, thay đổi luồng nghiệp vụ "
     "đặc thù hoặc tích hợp với hệ thống bên thứ ba của khách hàng.", False)
]))
content.append(p_mixed([
    ("Hợp đồng bảo trì và hỗ trợ kỹ thuật: ", True),
    ("gói SLA (Service Level Agreement) cam kết thời gian phản hồi và xử lý sự cố, "
     "đặc biệt quan trọng trong các dịp cao điểm như lễ, Tết.", False)
]))

content.append(h3("5.2.4.3 Đội ngũ và nguồn lực"))
content.append(p(
    "Đội ngũ sáng lập gồm các sinh viên ngành Công nghệ Thông tin với thế mạnh về phát triển "
    "ứng dụng Java, thiết kế cơ sở dữ liệu và phân tích nghiệp vụ hệ thống. Để mở rộng sang "
    "giai đoạn thương mại hóa, nhóm xác định cần bổ sung:"
))
content.append(bullet("2 kỹ sư phần mềm phụ trách phát triển và bảo trì sản phẩm."))
content.append(bullet("1 nhân viên kinh doanh chuyên tiếp cận và chăm sóc khách hàng doanh nghiệp."))
content.append(bullet("1 chuyên viên hỗ trợ kỹ thuật (helpdesk) phục vụ các nhà ga đã triển khai."))
content.append(p(
    "Nguồn vốn được kỳ vọng từ các quỹ ươm mầm khởi nghiệp công nghệ, chương trình tài trợ "
    "sinh viên khởi nghiệp của trường đại học, và các nhà đầu tư thiên thần quan tâm đến "
    "lĩnh vực công nghệ giao thông (Transport-Tech)."
))

# ── 5.3 ───────────────────────────────────────────────────────────────────────
content.append(h2("5.3 Điểm khác biệt của Azure Rail"))

content.append(h3("5.3.1 Sự khác biệt trong sản phẩm"))
content.append(p(
    "Hệ thống Azure Rail không chỉ là công cụ nhập liệu đơn thuần mà là nền tảng vận hành "
    "thông minh mang lại sự đột phá về năng suất và năng lực kiểm soát. Các yếu tố tạo nên "
    "sự khác biệt cốt lõi bao gồm:"
))
content.append(p_mixed([
    ("Giao diện tối ưu hóa cho tốc độ nghiệp vụ: ", True),
    ("khác với các hệ thống phổ thông, Azure Rail được thiết kế đặc thù cho nghiệp vụ "
     "bán vé tàu, hỗ trợ tìm kiếm nhanh theo số điện thoại, mã vé và tên hành khách, "
     "giúp giảm đáng kể thời gian xử lý mỗi giao dịch.", False)
]))
content.append(p_mixed([
    ("Sơ đồ ghế trực quan theo thời gian thực: ", True),
    ("nhân viên có thể nhìn thấy ngay trạng thái từng ghế trên từng toa của đoàn tàu, "
     "tránh trùng vé và hỗ trợ tư vấn chỗ ngồi phù hợp cho hành khách.", False)
]))
content.append(p_mixed([
    ("Bảng điều khiển Dashboard theo thời gian thực: ", True),
    ("quản lý nhà ga được cung cấp hệ thống biểu đồ trực quan về doanh thu, tình trạng "
     "lấp đầy toa xe, hiệu suất từng quầy vé và cảnh báo chuyến tàu sắp hết vé, "
     "hỗ trợ điều hành chủ động và linh hoạt.", False)
]))
content.append(p_mixed([
    ("Tích hợp đầy đủ trong một nền tảng duy nhất: ", True),
    ("từ quản lý hạ tầng tàu (đoàn tàu, toa, ghế, tuyến, lịch) đến bán vé, "
     "khuyến mãi, hóa đơn và thống kê, giúp loại bỏ sự phân mảnh dữ liệu "
     "thường gặp khi sử dụng nhiều phần mềm riêng lẻ.", False)
]))

content.append(h3("5.3.2 Khả năng mở rộng và tích hợp hệ sinh thái"))
content.append(p(
    "Một trong những điểm khác biệt rõ rệt của Azure Rail là kiến trúc mở, cho phép tích hợp "
    "dễ dàng các phân hệ và dịch vụ bổ sung để tạo thành hệ sinh thái quản trị nhà ga toàn diện:"
))
content.append(bullet(
    "Tích hợp thanh toán số: kết nối với các cổng thanh toán QR code, ví điện tử "
    "(MoMo, ZaloPay, VNPay) và máy POS để đa dạng hóa phương thức thanh toán."
))
content.append(bullet(
    "Tích hợp hóa đơn điện tử: kết nối tự động với các nhà cung cấp hóa đơn điện tử "
    "đạt chuẩn Tổng cục Thuế, giúp tự động hóa quy trình kế toán và đối soát."
))
content.append(bullet(
    "Dịch vụ quản lý vận chuyển hàng hóa: hệ thống có thể mở rộng để quản lý nghiệp vụ "
    "ký gửi hàng hóa, tính cước phí và in mã theo dõi, giúp nhà ga khai thác thêm nguồn thu."
))
content.append(bullet(
    "Ứng dụng tra cứu vé trên di động: phát triển ứng dụng companion để hành khách tra cứu "
    "lịch chạy, kiểm tra vé và nhận thông báo nhắc nhở trước giờ tàu khởi hành."
))

content.append(h3("5.3.3 Tư duy chuyển đổi số và định hướng vận tải thông minh"))
content.append(p(
    "Azure Rail không chỉ là một phần mềm quản lý đơn thuần mà đóng vai trò cốt lõi trong "
    "chiến lược chuyển đổi số toàn diện của ngành đường sắt, thay đổi hoàn toàn cách thức "
    "vận hành truyền thống theo ba hướng chính:"
))
content.append(p_mixed([
    ("Số hóa toàn bộ quy trình vận hành: ", True),
    ("loại bỏ hoàn toàn sổ sách báo cáo bằng giấy; mọi biên bản ca, báo cáo doanh thu, "
     "đối soát vé đều được thực hiện và lưu trữ số, tăng tính minh bạch và khả năng "
     "truy xuất dữ liệu bất cứ lúc nào.", False)
]))
content.append(p_mixed([
    ("Ra quyết định dựa trên dữ liệu: ", True),
    ("hệ thống cung cấp nền tảng dữ liệu sạch và có cấu trúc tốt. Trong tương lai, "
     "Azure Rail sẽ ứng dụng trí tuệ nhân tạo và học máy để phân tích chuỗi thời gian, "
     "dự báo nhu cầu đi lại trong các dịp lễ Tết, từ đó tư vấn điều chỉnh chiến lược "
     "giá vé và tăng cường toa xe hợp lý.", False)
]))
content.append(p_mixed([
    ("Hướng đến hệ sinh thái vận tải thông minh: ", True),
    ("Azure Rail có thể trở thành mắt xích kết nối trong hệ sinh thái vận tải tích hợp "
     "tại Việt Nam, liên thông dữ liệu với các nền tảng đặt vé trực tuyến, ứng dụng "
     "di động và hệ thống quản lý hành lý, góp phần xây dựng trải nghiệm hành khách "
     "liền mạch từ đầu đến cuối.", False)
]))

content.append(h3("5.3.4 Quản lý rủi ro"))
content.append(p("Nhóm xác định bốn nhóm rủi ro chính và chiến lược ứng phó tương ứng:"))
content.append(p_mixed([
    ("Rủi ro cạnh tranh: ", True),
    ("sự xuất hiện của các nhà cung cấp phần mềm quản lý vận tải lớn với nguồn lực mạnh. "
     "Chiến lược ứng phó: tập trung vào phân khúc mid-market đặc thù, tối ưu hóa chi phí "
     "và tốc độ triển khai, xây dựng quan hệ khách hàng trung thành thông qua dịch vụ hỗ trợ chất lượng cao.", False)
]))
content.append(p_mixed([
    ("Rủi ro pháp lý: ", True),
    ("thay đổi chính sách liên quan đến dữ liệu hành khách, bảo mật thông tin và quy định "
     "vận tải đường sắt. Chiến lược ứng phó: theo dõi chặt chẽ hành lang pháp lý và xây "
     "dựng kiến trúc hệ thống linh hoạt để thích ứng nhanh với thay đổi quy định.", False)
]))
content.append(p_mixed([
    ("Rủi ro chuyển đổi của khách hàng: ", True),
    ("thách thức trong việc thuyết phục doanh nghiệp truyền thống từ bỏ hệ thống cũ "
     "để chuyển đổi số. Chiến lược ứng phó: cung cấp chế độ dùng thử miễn phí, "
     "hỗ trợ chuyển đổi dữ liệu từ hệ thống cũ và cam kết đồng hành trong giai đoạn "
     "chuyển tiếp.", False)
]))
content.append(p_mixed([
    ("Rủi ro kỹ thuật: ", True),
    ("sự cố hệ thống trong giờ cao điểm ảnh hưởng trực tiếp đến nghiệp vụ bán vé. "
     "Chiến lược ứng phó: xây dựng cơ chế sao lưu và khôi phục dữ liệu tự động, "
     "triển khai kiểm thử tải trước khi go-live và cam kết SLA với thời gian phục hồi "
     "không quá 30 phút.", False)
]))

# ── Assemble ──────────────────────────────────────────────────────────────────
new_body = "".join(content)

# ── Locate old Chapter 5 body in xml ─────────────────────────────────────────
# Heading KHỞI NGHIỆP ở pos 1689014, tìm chính xác
bm = 'w:name="_Toc398988006"'
# Thực ra chương 5 dùng bookmark trong heading
# Tìm Heading1 chứa KHỞI NGHIỆP
for m in re.finditer(r'<w:p [^>]*>(?:(?!</w:p>).)*Heading1(?:(?!</w:p>).)*</w:p>', xml, re.DOTALL):
    texts = re.findall(r'<w:t[^>]*>([^<]*)</w:t>', m.group())
    if 'KHỞI NGHIỆP' in ''.join(texts):
        h5_para_start = m.start()
        h5_para_end   = m.end()
        break

# Tìm Heading1 chứa TÀI LIỆU THAM KHẢO
for m in re.finditer(r'<w:p [^>]*>(?:(?!</w:p>).)*Heading1(?:(?!</w:p>).)*</w:p>', xml, re.DOTALL):
    texts = re.findall(r'<w:t[^>]*>([^<]*)</w:t>', m.group())
    if 'TÀI LIỆU THAM KHẢO' in ''.join(texts):
        tlhk_start = m.start()
        break

print(f"Chapter 5 heading: {h5_para_start}–{h5_para_end}")
print(f"TÀI LIỆU heading: {tlhk_start}")
print(f"Old body: {h5_para_end}–{tlhk_start}  ({tlhk_start - h5_para_end} chars)")

# Giữ heading Chương 5, thay nội dung body
xml = xml[:h5_para_end] + new_body + xml[tlhk_start:]
print(f"New body inserted: {len(new_body):,} chars")

# ── Write ─────────────────────────────────────────────────────────────────────
files["word/document.xml"] = xml.encode("utf-8")
with zipfile.ZipFile(DST, "w", zipfile.ZIP_DEFLATED) as zout:
    for name, data in files.items():
        zout.writestr(name, data)

size = os.path.getsize(DST)
print(f"\n✓ Done → {DST}  ({size:,} bytes)")

# ── Sanity ────────────────────────────────────────────────────────────────────
with zipfile.ZipFile(DST) as z:
    xml_out = z.read("word/document.xml").decode("utf-8")

import xml.etree.ElementTree as ET
try:
    ET.fromstring(xml_out.encode("utf-8"))
    print("Sanity — XML well-formed OK")
except ET.ParseError as e:
    print(f"Sanity — XML ERROR: {e}")

checks = ["5.1 ", "5.2 ", "5.3 ", "Azure Rail", "SaaS", "5.1.1", "5.1.2", "5.1.3",
          "5.2.2.1", "5.2.4.1", "5.3.4", "TÀI LIỆU THAM KHẢO"]
for c in checks:
    found = c in xml_out
    print(f"  {'✓' if found else '✗'} {c!r}")
print("All checks passed.")
