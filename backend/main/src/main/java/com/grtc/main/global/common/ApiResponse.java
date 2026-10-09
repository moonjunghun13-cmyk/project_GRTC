package com.grtc.main.global.common;

import com.grtc.main.global.exception.ErrorCode;
import com.grtc.main.global.exception.ErrorResponse;

import java.util.List;

// 명세서 3-1 공통 응답 형식
//   성공: { "success": true,  "data": { ... }, "error": null }
//   실패: { "success": false, "data": null,    "error": { "code", "message", "details" } }
public record ApiResponse<T>(boolean success, T data, ErrorResponse error) {

    // 성공 응답 (data 있음)
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    // 성공 응답 (내려줄 data 없음: 로그아웃, 삭제 등)
    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(true, null, null);
    }

    // 실패 응답 (ErrorCode 의 code / message 를 그대로 사용)
    public static ApiResponse<Void> fail(ErrorCode errorCode) {
        return fail(errorCode.getCode(), errorCode.getMessage(), List.of());
    }

    // 실패 응답 (메시지나 상세 내용을 직접 지정)
    public static ApiResponse<Void> fail(String code, String message, List<ErrorResponse.Detail> details) {
        return new ApiResponse<>(false, null, new ErrorResponse(code, message, details));
    }
}
