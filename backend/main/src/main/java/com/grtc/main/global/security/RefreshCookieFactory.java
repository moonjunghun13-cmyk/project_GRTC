package com.grtc.main.global.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

// Refresh 토큰을 담는 HttpOnly 쿠키 생성 (명세서 3-1: Refresh 토큰은 14일, HttpOnly 쿠키로 전달)
//   - HttpOnly 라서 자바스크립트로는 읽을 수 없고, 브라우저가 /api/v1/auth/** 요청에만 자동으로 실어 보낸다.
//   - 프론트(axios)는 withCredentials: true 로 호출해야 쿠키가 오간다.
@Component
public class RefreshCookieFactory {

    public static final String COOKIE_NAME = "refreshToken";
    private static final String COOKIE_PATH = "/api/v1/auth";

    private final boolean secure;
    private final Duration maxAge;

    public RefreshCookieFactory(@Value("${app.jwt.refresh-cookie-secure:false}") boolean secure,
                                @Value("${app.jwt.refresh-expiration-days:14}") long refreshExpirationDays) {
        this.secure = secure;
        this.maxAge = Duration.ofDays(refreshExpirationDays);
    }

    // 로그인 시 내려주는 쿠키
    public ResponseCookie create(String refreshToken) {
        return build(refreshToken, maxAge);
    }

    // 로그아웃 시 쿠키 삭제 (같은 이름/경로로 만료 시간 0)
    public ResponseCookie expire() {
        return build("", Duration.ZERO);
    }

    private ResponseCookie build(String value, Duration age) {
        return ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(secure)      // 운영(HTTPS)에서는 app.jwt.refresh-cookie-secure=true
                .sameSite("Lax")
                .path(COOKIE_PATH)
                .maxAge(age)
                .build();
    }
}
