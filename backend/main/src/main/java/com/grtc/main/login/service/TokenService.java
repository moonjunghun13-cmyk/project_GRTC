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

    public String createAccessToken(LoginResponseDto member) {
        return jwtProvider.createAccessToken(member.getId(), member.getLoginId(), member.getRole().name());
    }

    public long accessExpiresIn() {
        return jwtProvider.getAccessExpirationSeconds();
    }

    @Transactional
    public String issueRefreshToken(Long memberId) {
        LocalDateTime now = LocalDateTime.now();
        refreshTokenRepository.deleteByExpiresAtBefore(now);

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

        LoginEntity member = loginService.getActiveMember(saved.getMemberId());
        String accessToken = jwtProvider.createAccessToken(
                member.getId(), member.getLoginId(), member.getRole().name());
        log.info("[token] Access 토큰 재발급 id={}", member.getId());
        return TokenResponse.bearer(accessToken, jwtProvider.getAccessExpirationSeconds());
    }

    @Transactional
    public void revoke(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        refreshTokenRepository.deleteByTokenHash(hash(refreshToken));
    }

    @Transactional
    public void revokeAll(Long memberId) {
        refreshTokenRepository.deleteByMemberId(memberId);
    }

    private static String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 을 사용할 수 없습니다.", e);
        }
    }
}
