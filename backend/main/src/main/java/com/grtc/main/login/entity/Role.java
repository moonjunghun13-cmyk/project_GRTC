package com.grtc.main.login.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 회원 권한 종류(관리자, 일반 사용자)를 정의한 열거형
@Getter
@RequiredArgsConstructor
public enum Role {
    ADMIN("관리자"),
    USER("일반회원");

    private final String label;
}
