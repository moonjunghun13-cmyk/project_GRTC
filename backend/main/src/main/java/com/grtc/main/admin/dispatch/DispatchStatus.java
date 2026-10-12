package com.grtc.main.admin.dispatch;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DispatchStatus {
    COMPLETED("배차 완료"),
    WAITING("배차 대기"),
    CHANGED("배차 변경"),
    CANCELLED("배차 취소");

    private final String label;
}
