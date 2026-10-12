package com.grtc.main.global.common;

import java.util.Locale;

// 목록 검색(LIKE)에서 공통으로 쓰는 유틸
public final class SearchUtil {

    public static final char ESCAPE = '\\';

    private SearchUtil() {
    }

    public static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public static String contains(String keyword) {
        String escaped = keyword.trim()
                .toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
    }
}
