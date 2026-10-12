package com.grtc.main.login.repository;

import com.grtc.main.login.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);

    long deleteByTokenHash(String tokenHash);

    long deleteByMemberId(Long memberId);

    long deleteByExpiresAtBefore(LocalDateTime time);
}
