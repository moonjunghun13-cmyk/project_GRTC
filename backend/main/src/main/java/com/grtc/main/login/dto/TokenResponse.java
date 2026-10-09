package com.grtc.main.login.dto;

// Access 토큰 재발급 응답 (POST /api/v1/auth/reissue)
//   accessToken: 이후 요청의 "Authorization: Bearer {accessToken}" 헤더에 넣는 값
//   expiresIn  : Access 토큰 유효 시간(초). 30분 = 1800
public record TokenResponse(String accessToken, String tokenType, long expiresIn) {

    public static TokenResponse bearer(String accessToken, long expiresIn) {
        return new TokenResponse(accessToken, "Bearer", expiresIn);
    }
}
