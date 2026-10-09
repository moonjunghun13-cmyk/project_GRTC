package com.grtc.main.login.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// 발급한 Refresh 토큰을 기록하는 테이블 (명세서 3-1: Refresh 토큰 14일)
//  - 로그아웃하면 이 기록을 지운다. 기록이 없는 Refresh 토큰으로는 Access 토큰을 재발급받을 수 없다.
//  - 토큰 원문 대신 SHA-256 해시를 저장한다. (DB 가 노출돼도 토큰을 그대로 쓸 수 없도록)
@Entity
@Table(name = "refresh_token", indexes = @Index(name = "idx_refresh_token_member", columnList = "member_id"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class RefreshTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId; // 토큰 주인(회원 ID)

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash; // Refresh 토큰의 SHA-256 해시(16진수 64자)

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt; // 만료 일시

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 발급 일시

    @PrePersist
    protected void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public boolean isExpired(LocalDateTime now) {
        return expiresAt.isBefore(now);
    }
}
