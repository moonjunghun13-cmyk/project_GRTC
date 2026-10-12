package com.grtc.main.login.dto;

// 로그인 성공 응답 (POST /api/v1/auth/login)
public record LoginResult(String accessToken, String tokenType, long expiresIn, LoginResponseDto member) {

    public static LoginResult bearer(String accessToken, long expiresIn, LoginResponseDto member) {
        return new LoginResult(accessToken, "Bearer", expiresIn, member);
    }
}
