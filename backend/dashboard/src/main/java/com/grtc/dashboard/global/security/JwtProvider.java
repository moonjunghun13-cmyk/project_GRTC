package com.grtc.dashboard.global.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

// Access 토큰(JWT) 검증 (명세서 3-1)
//   - 토큰 발급은 main 서버(/api/v1/auth/login, /api/v1/auth/reissue)가 담당하고, 이 서버는 검증만 한다.
//   - app.jwt.secret 은 main 서버와 같은 값이어야 한다.
//   - 토큰 안에는 회원 ID(sub), 아이디(loginId), 권한(role)이 들어 있다.
@Component
public class JwtProvider {

    public static final String CLAIM_LOGIN_ID = "loginId";
    public static final String CLAIM_ROLE = "role";

    private final SecretKey key;

    public JwtProvider(@Value("${app.jwt.secret}") String secret) {
        // HMAC 서명 키: 32바이트(영문 32자) 이상이어야 한다. (64자 이상이면 HS512 로 서명된다)
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // 토큰 검증 후 내용(Claims) 반환
    //   만료: ExpiredJwtException, 위조/형식 오류: JwtException
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
