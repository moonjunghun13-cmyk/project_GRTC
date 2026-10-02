package com.grtc.main.global.security;

import com.grtc.main.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

// 필터/시큐리티 단계(컨트롤러 밖)에서 명세서 3-1 의 실패 응답과 같은 JSON 을 내려줄 때 사용
//   { "success": false, "data": null, "error": { "code": "...", "message": "...", "details": [] } }
public final class JsonErrorWriter {

    private JsonErrorWriter() {
    }

    public static void write(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getStatus().value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"success\":false,\"data\":null,\"error\":{\"code\":\""
                + escape(errorCode.getCode()) + "\",\"message\":\""
                + escape(errorCode.getMessage()) + "\",\"details\":[]}}");
    }

    // JSON 문자열 안에 들어가면 안 되는 문자(역슬래시, 큰따옴표) 처리
    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
