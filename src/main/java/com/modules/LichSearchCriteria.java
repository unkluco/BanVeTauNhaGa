package com.modules;

/**
 * Structured search criteria record — dùng chung giữa Dashboard (TongQuatModule)
 * và các module đích (QuanLyLichChayModule).
 *
 * @param gaDiCode    mã ga đi  (maGa từ DB, ví dụ "GA-001") hoặc null
 * @param gaDenCode   mã ga đến (maGa từ DB) hoặc null
 * @param tuNgayYmd   từ ngày ("yyyy-MM-dd") hoặc null
 * @param denNgayYmd  đến ngày ("yyyy-MM-dd") hoặc null
 * @param tuGioHm     từ giờ ("HH:mm") hoặc null
 * @param denGioHm    đến giờ ("HH:mm") hoặc null
 */
public record LichSearchCriteria(
    String gaDiCode,
    String gaDenCode,
    String tuNgayYmd,
    String denNgayYmd,
    String tuGioHm,
    String denGioHm
) {}
