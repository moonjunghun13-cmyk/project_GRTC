package com.grtc.main.login.repository;

import com.grtc.main.login.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

// Refresh 토큰 기록 조회/저장/삭제
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

    // 쿠키로 받은 Refresh 토큰(의 해시)으로 기록 조회 (재발급용)
    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);

    // 로그아웃: 해당 토큰 기록 삭제
    long deleteByTokenHash(String tokenHash);

    // 비밀번호 변경: 해당 회원의 모든 토큰 기록 삭제 (다른 기기에서도 다시 로그인하도록)
    long deleteByMemberId(Long memberId);

    // 만료된 기록 정리
    long deleteByExpiresAtBefore(LocalDateTime time);
}
