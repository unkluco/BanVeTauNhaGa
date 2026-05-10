package com.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public final class MaTuDong {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final Map<String, LocalDateTime> LAST_BY_PREFIX = new HashMap<>();

    private MaTuDong() {
    }

    public static synchronized String generate(String prefix) {
        String normalizedPrefix = prefix == null ? "" : prefix.trim().toUpperCase();
        LocalDateTime now = LocalDateTime.now().withNano(0);
        LocalDateTime last = LAST_BY_PREFIX.get(normalizedPrefix);
        if (last != null && !now.isAfter(last)) {
            now = last.plusSeconds(1);
        }
        LAST_BY_PREFIX.put(normalizedPrefix, now);
        return normalizedPrefix + now.format(FORMATTER);
    }
}
