package com.grtc.main.login.dto;

// 로그인 성공 응답 (POST /api/v1/auth/login)
//   accessToken: 이후 요청의 "Authorization: Bearer {accessToken}" 헤더에 넣는 값
//   expiresIn  : Access 토큰 유효 시간(초). 30분 = 1800
//   member     : 로그인한 회원 정보 (redirectPath 로 로그인 직후 이동할 화면을 알 수 있다)
//   ※ Refresh 토큰은 응답 본문이 아니라 HttpOnly 쿠키(refreshToken)로 내려간다.
public record LoginResult(String accessToken, String tokenType, long expiresIn, LoginResponseDto member) {

    public static LoginResult bearer(String accessToken, long expiresIn, LoginResponseDto member) {
        return new LoginResult(accessToken, "Bearer", expiresIn, member);
    }
}
