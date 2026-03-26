package com.enums;

public enum TrangThaiVe {
    DA_BAN,
    DA_HUY;

    @Override
    public String toString() {
        return switch (this) {
            case DA_BAN -> "Đã bán";
            case DA_HUY -> "Đã hủy";
        };
    }

    public static TrangThaiVe fromAny(String s) {
        if (s == null) return null;
        String x = s.trim();
        for (TrangThaiVe tt : values()) {
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
