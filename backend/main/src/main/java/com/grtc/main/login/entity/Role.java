package com.grtc.main.login.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 회원 권한 종류(관리자, 일반 사용자)를 정의한 열거형
@Getter
@RequiredArgsConstructor
public enum Role {
    ADMIN("관리자"), // 관리자: 로그인 시 대시보드로 이동
    USER("일반회원"); // 일반 사용자: 로그인 시 민원 화면으로 이동

    private final String label; // 화면에 보여줄 이름
}
