package com.grtc.main.global.common;

import java.util.Locale;

// 목록 검색(LIKE)에서 공통으로 쓰는 유틸
public final class SearchUtil {

    // LIKE 이스케이프 문자 (cb.like(..., pattern, SearchUtil.ESCAPE) 로 함께 넘긴다)
    public static final char ESCAPE = '\\';

    private SearchUtil() {
    }

    public static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    // "%검색어%" 형태의 소문자 패턴을 만든다. 사용자가 입력한 % _ 는 글자 그대로 검색되도록 이스케이프한다.
    public static String contains(String keyword) {
        String escaped = keyword.trim()
                .toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
    }
}
