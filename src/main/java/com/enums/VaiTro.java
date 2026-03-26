package com.enums;

public enum VaiTro {
    BAN_VE,
    DIEU_PHOI,
    ADMIN;

    @Override
    public String toString() {
        return switch (this) {
            case BAN_VE -> "Bán vé";
            case DIEU_PHOI -> "Điều phối";
            case ADMIN -> "Admin";
        };
    }

    public static VaiTro fromAny(String s) {
        if (s == null) return null;
        String x = s.trim();
        for (VaiTro vt : values()) {
            if (vt.name().equalsIgnoreCase(x)
                    || vt.toDbValue().equalsIgnoreCase(x)
                    || vt.toString().equalsIgnoreCase(x)) {
                return vt;
            }
        }
        return null;
    }

    public String toDbValue() {
        return this.name();
    }
}
