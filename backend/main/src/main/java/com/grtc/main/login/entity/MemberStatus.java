package com.grtc.main.login.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 회원 상태 (회원관리 화면의 정상 / 이용정지 / 탈퇴)
@Getter
@RequiredArgsConstructor
public enum MemberStatus {
    NORMAL("정상"),
    SUSPENDED("이용정지"),
    WITHDRAWN("탈퇴");

    private final String label;
}
