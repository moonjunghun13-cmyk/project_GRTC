package com.grtc.main.admin.operation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TrainDirection {
    PYEONGDONG("평동행", "평동"),
    SOTAE("소태행", "소태"),
    NOKDONG("녹동행", "녹동");

    private final String label;
    private final String terminalName;
}
