package com.grtc.main.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    DUPLICATE_NICKNAME(
            HttpStatus.CONFLICT, "L001", "이미 사용 중인 닉네임입니다."
    ),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT,
            "L002", "이미 사용 중인 이메일입니다."),
    DUPLICATE_ACCOUNT(HttpStatus.CONFLICT,
            "L003", "이미 가입된 정보가 있습니다. 다시 시도해주세요."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED,
            "L004", "닉네임 또는 비밀번호가 올바르지 않습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
