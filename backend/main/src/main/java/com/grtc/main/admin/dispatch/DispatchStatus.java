package com.grtc.main.admin.dispatch;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 배차 상태 (배차관리 화면의 배차 완료 / 대기 / 변경 / 취소)
@Getter
@RequiredArgsConstructor
public enum DispatchStatus {
    COMPLETED("배차 완료"),
    WAITING("배차 대기"),
    CHANGED("배차 변경"),
    CANCELLED("배차 취소");

    private final String label;
}
