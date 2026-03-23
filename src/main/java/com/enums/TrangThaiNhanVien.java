package com.enums;

public enum TrangThaiNhanVien {
    DANG_LAM,
    NGHI_PHEP,
    DA_NGHI;

    @Override
    public String toString() {
        return switch (this) {
            case DANG_LAM  -> "Đang làm";
            case NGHI_PHEP -> "Nghỉ phép";
            case DA_NGHI   -> "Đã nghỉ";
        };
    }

    public static TrangThaiNhanVien fromAny(String s) {
        if (s == null) return null;
        String x = s.trim();
        for (TrangThaiNhanVien tt : values()) {
            if (tt.name().equalsIgnoreCase(x)
                    || tt.toDbValue().equalsIgnoreCase(x)
                    || tt.toString().equalsIgnoreCase(x)) {
                return tt;
            }
        }
        return null;
    }

    public String toDbValue() {
        return this.name();
    }
}
