package com.grtc.main.global.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

// Access 토큰(JWT) 발급 / 검증 (명세서 3-1: Access 토큰 30분)
//   - 토큰 안에는 회원 ID(sub), 아이디(loginId), 권한(role)을 담는다.
@Component
public class JwtProvider {

    public static final String CLAIM_LOGIN_ID = "loginId";
    public static final String CLAIM_ROLE = "role";

    private final SecretKey key;

    @Getter
    private final long accessExpirationSeconds;

    public JwtProvider(@Value("${app.jwt.secret}") String secret,
                       @Value("${app.jwt.access-expiration-minutes:30}") long accessExpirationMinutes) {
        // HMAC 서명 키: 32바이트(영문 32자) 이상이어야 한다. (64자 이상이면 HS512 로 서명된다)
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpirationSeconds = accessExpirationMinutes * 60;
    }

    // Access 토큰 발급
    public String createAccessToken(Long memberId, String loginId, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .claim(CLAIM_LOGIN_ID, loginId)
                .claim(CLAIM_ROLE, role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessExpirationSeconds)))
                .signWith(key)
                .compact();
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
