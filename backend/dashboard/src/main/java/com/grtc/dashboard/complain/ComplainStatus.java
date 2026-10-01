package com.grtc.dashboard.complain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 민원 처리 상태
//   label         : 상세/답변 화면과 대시보드 답변건수 (접수대기 / 접수 / 이관 / 답변완료)
//   progressLabel : 민원관리 목록 화면 (미처리 / 처리중 / 처리완료)
@Getter
@RequiredArgsConstructor
public enum ComplainStatus {
    WAITING("접수대기", "미처리"),
    RECEIVED("접수", "처리중"),
    TRANSFERRED("이관", "처리중"),
    ANSWERED("답변완료", "처리완료");

    private final String label;
    private final String progressLabel;
}
