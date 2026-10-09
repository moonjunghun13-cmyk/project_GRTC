package com.grtc.main.login.service;

import com.grtc.main.global.exception.BusinessException;
import com.grtc.main.global.exception.ErrorCode;
import com.grtc.main.global.security.JwtProvider;
import com.grtc.main.login.dto.LoginResponseDto;
import com.grtc.main.login.dto.TokenResponse;
import com.grtc.main.login.entity.LoginEntity;
import com.grtc.main.login.entity.RefreshTokenEntity;
import com.grtc.main.login.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

// Access / Refresh 토큰 발급, 재발급, 폐기 (명세서 3-1 인증)
//  - Access 토큰 : JWT, 30분. 응답 본문으로 내려주고 프론트가 Authorization 헤더에 넣어 보낸다.
//  - Refresh 토큰: 임의의 긴 문자열, 14일. HttpOnly 쿠키로 내려주고 DB(refresh_token)에 해시를 기록한다.
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;
    private final LoginService loginService;
    private final JwtProvider jwtProvider;

    @Value("${app.jwt.refresh-expiration-days:14}")
    private long refreshExpirationDays;

    // 로그인한 회원의 Access 토큰 발급
    public String createAccessToken(LoginResponseDto member) {
        return jwtProvider.createAccessToken(member.getId(), member.getLoginId(), member.getRole().name());
    }

    // Access 토큰 유효 시간(초)
    public long accessExpiresIn() {
        return jwtProvider.getAccessExpirationSeconds();
    }

    // Refresh 토큰 발급: 원문은 쿠키로 내려주고, DB 에는 해시만 저장한다.
    @Transactional
    public String issueRefreshToken(Long memberId) {
        LocalDateTime now = LocalDateTime.now();
        refreshTokenRepository.deleteByExpiresAtBefore(now); // 만료된 기록 정리

        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String refreshToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        refreshTokenRepository.save(RefreshTokenEntity.builder()
                .memberId(memberId)
                .tokenHash(hash(refreshToken))
                .expiresAt(now.plusDays(refreshExpirationDays))
                .build());
        return refreshToken;
    }

    // Access 토큰 재발급: 쿠키의 Refresh 토큰이 DB 에 있고, 만료되지 않았고, 회원이 이용 가능한 상태여야 한다.
    //  - 권한은 DB 의 최신 값으로 다시 넣는다. (로그인 후 권한이 바뀐 경우 반영)
    @Transactional(readOnly = true)
    public TokenResponse reissue(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        RefreshTokenEntity saved = refreshTokenRepository.findByTokenHash(hash(refreshToken))
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));
        if (saved.isExpired(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 정지/탈퇴 회원이면 여기서 예외 (403)
        LoginEntity member = loginService.getActiveMember(saved.getMemberId());
        String accessToken = jwtProvider.createAccessToken(
                member.getId(), member.getLoginId(), member.getRole().name());
        log.info("[token] Access 토큰 재발급 id={}", member.getId());
        return TokenResponse.bearer(accessToken, jwtProvider.getAccessExpirationSeconds());
    }

    // 로그아웃: 해당 Refresh 토큰 기록 삭제
    @Transactional
    public void revoke(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        refreshTokenRepository.deleteByTokenHash(hash(refreshToken));
    }

    // 비밀번호 변경 등: 해당 회원의 모든 Refresh 토큰 기록 삭제
    @Transactional
    public void revokeAll(Long memberId) {
        refreshTokenRepository.deleteByMemberId(memberId);
    }

    // SHA-256 해시(16진수 64자)
    private static String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 을 사용할 수 없습니다.", e);
        }
    }
}
