package com.grtc.main.admin.vehicle;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VehicleStatus {
    RUNNING("운행중"),
    STANDBY("대기"),
    MAINTENANCE("정비"),
    STOPPED("운행정지");

    private final String label;
}
