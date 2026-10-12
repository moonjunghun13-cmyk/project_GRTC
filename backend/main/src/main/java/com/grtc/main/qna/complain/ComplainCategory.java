package com.grtc.main.qna.complain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

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
