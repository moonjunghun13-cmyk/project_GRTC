package com.grtc.dashboard.complain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 민원 유형 (대시보드 민원건수 차트: 단순 / 건의 / 제보 / 불만)
@Getter
@RequiredArgsConstructor
public enum ComplainType {
    SIMPLE("단순"),
    SUGGESTION("건의"),
    REPORT("제보"),
    COMPLAINT("불만");

    private final String label;
}
