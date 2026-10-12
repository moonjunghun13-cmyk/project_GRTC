package com.grtc.main.global.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

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

    public ResponseCookie create(String refreshToken) {
        return build(refreshToken, maxAge);
    }

    public ResponseCookie expire() {
        return build("", Duration.ZERO);
    }

    private ResponseCookie build(String value, Duration age) {
        return ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path(COOKIE_PATH)
                .maxAge(age)
                .build();
    }
}
