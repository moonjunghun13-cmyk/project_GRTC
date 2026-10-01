package com.grtc.dashboard.complain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 민원 분류 (민원관리 목록의 색깔 태그 / 검색 필터: 운행관련, 시설물, 노선/시간, 요금/결제, 기타)
@Getter
@RequiredArgsConstructor
public enum ComplainCategory {
    OPERATION("운행관련"),
    FACILITY("시설물"),
    ROUTE_TIME("노선/시간"),
    FARE_PAYMENT("요금/결제"),
    ETC("기타");

    private final String label;
}
