package com.grtc.main.global.exception;

import java.util.List;

// 실패 응답의 error 부분 (명세서 3-1)
//   { "code": "INVALID_INPUT", "message": "...", "details": [ { "field": "name", "message": "필수 입력란입니다." } ] }
public record ErrorResponse(String code, String message, List<Detail> details) {

    // 입력값 검증 실패 시 어떤 항목이 왜 틀렸는지
    public record Detail(String field, String message) {
    }
}
