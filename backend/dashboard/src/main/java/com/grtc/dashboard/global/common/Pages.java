package com.grtc.dashboard.global.common;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

// 화면(1부터 시작) 기준 page/size 를 안전한 Pageable 로 바꿔주는 유틸
public final class Pages {

    private static final int MAX_SIZE = 100; // 한 번에 너무 많이 가져가지 못하도록 상한

    private Pages() {
    }

    public static Pageable of(int page, int size, Sort sort) {
        int pageIndex = Math.max(page, 1) - 1;
        int pageSize = Math.min(Math.max(size, 1), MAX_SIZE);
        return PageRequest.of(pageIndex, pageSize, sort);
    }
}
