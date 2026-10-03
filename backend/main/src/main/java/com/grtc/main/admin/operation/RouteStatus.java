package com.grtc.main.admin.operation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 노선 운영 상태 (대시보드 노선운영현황의 '정상 운행' 배지)
@Getter
@RequiredArgsConstructor
public enum RouteStatus {
    NORMAL("정상 운행"),
    DELAYED("지연 운행"),
    SUSPENDED("운행 중단");

    private final String label;
}
