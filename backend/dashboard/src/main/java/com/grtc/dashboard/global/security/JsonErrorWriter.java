package com.grtc.dashboard.global.security;

import com.grtc.dashboard.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

// 필터/시큐리티 단계(컨트롤러 밖)에서 ErrorResponse 와 같은 JSON 을 내려줄 때 사용
public final class JsonErrorWriter {

    private JsonErrorWriter() {
    }

    public static void write(HttpServletResponse response, int status, ErrorCode errorCode) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":\"" + errorCode.getCode()
                + "\",\"message\":\"" + errorCode.getMessage() + "\"}");
    }
}
