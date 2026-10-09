package com.grtc.main.qna.complain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 민원 처리 상태
//   label         : 상세/답변 화면과 대시보드 답변건수 (접수대기 / 답변중 / 답변완료 / 이관안내 / 철회)
//   progressLabel : 민원관리 목록 화면 (미처리 / 처리중 / 처리완료 / 철회)
//   WITHDRAWN(철회): 민원인이 접수대기 상태에서 스스로 삭제한 민원.
//     - 민원인(사용자 API)에게는 삭제된 것처럼 보이지 않는다.
//     - DB 에는 남아서 관리자 목록/상세/대시보드 집계에는 그대로 나온다.
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
