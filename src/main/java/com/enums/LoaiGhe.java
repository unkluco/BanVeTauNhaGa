package com.enums;

public enum LoaiGhe {
    GHE_CUNG,
    GHE_MEM,
    GIUONG_NAM;

    @Override
    public String toString() {
        return switch (this) {
            case GHE_CUNG -> "Ghế cứng";
            case GHE_MEM -> "Ghế mềm";
            case GIUONG_NAM -> "Giường nằm";
        };
    }

    public static LoaiGhe fromAny(String s) {
        if (s == null) return null;
        String x = s.trim();
        for (LoaiGhe lg : values()) {
            if (lg.name().equalsIgnoreCase(x)
                    || lg.toDbValue().equalsIgnoreCase(x)
                    || lg.toString().equalsIgnoreCase(x)) {
                return lg;
            }
        }
        return null;
    }

    public String toDbValue() {
        return this.name();
    }
}
