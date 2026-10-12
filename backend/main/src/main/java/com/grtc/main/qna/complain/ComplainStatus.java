package com.grtc.main.qna.complain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ComplainStatus {
    WAITING("접수대기", "미처리"),
    RECEIVED("답변중", "처리중"),
    ANSWERED("답변완료", "처리완료"),
    TRANSFERRED("이관안내", "처리중"),
    WITHDRAWN("철회", "철회");

    private final String label;
    private final String progressLabel;
}
