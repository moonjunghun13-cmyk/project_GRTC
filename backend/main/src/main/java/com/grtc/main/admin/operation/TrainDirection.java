package com.grtc.main.admin.operation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 열차 진행 방향 (운행관리 화면 범례: 평동행 / 소태행 / 녹동행)
// terminalName 은 그 방향의 종착역 이름이다. 현재 역에서 종착역 쪽으로 한 정거장 이동한 곳이 '다음 역'이 된다.
@Getter
@RequiredArgsConstructor
public enum TrainDirection {
    PYEONGDONG("평동행", "평동"),
    SOTAE("소태행", "소태"),
    NOKDONG("녹동행", "녹동");

    private final String label;
    private final String terminalName;
}
