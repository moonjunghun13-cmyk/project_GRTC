package com.grtc.main.admin.timetable;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 입·출고 구분 (출고: 차량기지에서 본선으로 나감 / 입고: 운행을 마치고 차량기지로 들어옴)
@Getter
@RequiredArgsConstructor
public enum MoveType {
    DEPART("출고"),
    RETURN("입고");

    private final String label;
}
