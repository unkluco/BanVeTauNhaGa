package com.modules;

/**
 * Structured search criteria record — dùng chung giữa Dashboard (TongQuatModule)
 * và các module đích (QuanLyLichChayModule).
 *
 * @param gaDiCode   mã ga đi  (maGa từ DB, ví dụ "GA-001") hoặc null
 * @param gaDenCode  mã ga đến (maGa từ DB) hoặc null
 * @param ngayYmd    ngày đi   ("yyyy-MM-dd") hoặc null
 * @param loaiGhe    loại ghế  ("GHE_CUNG"|"GHE_MEM"|"GIUONG_NAM") hoặc null cho "Tất cả"
 */
public record LichSearchCriteria(
    String gaDiCode,
    String gaDenCode,
    String ngayYmd,
    String loaiGhe
) {}
