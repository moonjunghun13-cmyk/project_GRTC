package com.grtc.main.admin.vehicle;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 차량 상태 (차량관리 화면의 운행중 / 대기 / 정비 / 운행정지)
@Getter
@RequiredArgsConstructor
public enum VehicleStatus {
    RUNNING("운행중"),
    STANDBY("대기"),
    MAINTENANCE("정비"),
    STOPPED("운행정지");

    private final String label;
}
