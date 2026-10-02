package com.grtc.main.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

// 에러 코드 (명세서 3-2)
//   - code 는 응답의 error.code 로 그대로 내려간다.
//   - 명세서에 있는 코드(INVALID_INPUT, UNAUTHORIZED, TOKEN_EXPIRED, FORBIDDEN, RESOURCE_NOT_FOUND,
//     DUPLICATE_EMAIL, SCHEDULE_CONFLICT, TRAINSET_NOT_AVAILABLE, FILE_TOO_LARGE, UNSUPPORTED_FILE_TYPE,
//     INTERNAL_ERROR)는 그 이름을 그대로 쓰고, 명세서에 없는 상황은 같은 방식(영문 대문자 이름)으로 추가했다.
//   - dashboard 서버의 ErrorCode 와 같은 내용으로 맞춘다.
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ---------- 공통 ----------
    INVALID_INPUT(HttpStatus.BAD_REQUEST,
            "INVALID_INPUT", "요청값이 올바르지 않습니다."),
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST,
            "INVALID_INPUT", "요청 값의 형식이 올바르지 않습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,
            "INTERNAL_ERROR", "서버 오류가 발생했습니다."),

    // ---------- 인증 / 권한 ----------
    LOGIN_REQUIRED(HttpStatus.UNAUTHORIZED,
            "UNAUTHORIZED", "로그인이 필요합니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED,
            "TOKEN_EXPIRED", "Access 토큰이 만료되었습니다. 토큰을 재발급해주세요."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED,
            "UNAUTHORIZED", "로그인 정보가 만료되었습니다. 다시 로그인해주세요."),
    ADMIN_ONLY(HttpStatus.FORBIDDEN,
            "FORBIDDEN", "관리자 페이지이므로 열람이 불가합니다."),

    // ---------- 로그인 / 회원 ----------
    DUPLICATE_LOGIN_ID(HttpStatus.CONFLICT,
            "DUPLICATE_LOGIN_ID", "이미 사용 중인 아이디입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT,
            "DUPLICATE_EMAIL", "이미 사용 중인 이메일입니다."),
    DUPLICATE_ACCOUNT(HttpStatus.CONFLICT,
            "DUPLICATE_ACCOUNT", "이미 가입된 정보가 있습니다. 다시 시도해주세요."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED,
            "LOGIN_FAILED", "아이디 또는 비밀번호가 올바르지 않습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND,
            "RESOURCE_NOT_FOUND", "존재하지 않는 회원입니다."),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST,
            "PASSWORD_MISMATCH", "비밀번호가 일치하지 않습니다."),
    CURRENT_PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST,
            "CURRENT_PASSWORD_MISMATCH", "현재 비밀번호가 올바르지 않습니다."),
    ACCOUNT_SUSPENDED(HttpStatus.FORBIDDEN,
            "ACCOUNT_SUSPENDED", "이용이 정지된 계정입니다. 관리자에게 문의해주세요."),
    ACCOUNT_WITHDRAWN(HttpStatus.FORBIDDEN,
            "ACCOUNT_WITHDRAWN", "탈퇴한 계정입니다."),
    CANNOT_MODIFY_SELF(HttpStatus.BAD_REQUEST,
            "CANNOT_MODIFY_SELF", "본인의 권한과 상태는 변경할 수 없습니다."),

    // ---------- 차량 ----------
    VEHICLE_NOT_FOUND(HttpStatus.NOT_FOUND,
            "RESOURCE_NOT_FOUND", "존재하지 않는 차량입니다."),
    DUPLICATE_VEHICLE_NO(HttpStatus.CONFLICT,
            "DUPLICATE_VEHICLE_NO", "이미 등록된 차량번호입니다."),
    VEHICLE_IN_USE(HttpStatus.CONFLICT,
            "VEHICLE_IN_USE", "배차 또는 운행 이력이 있는 차량은 삭제할 수 없습니다."),
    VEHICLE_NOT_AVAILABLE(HttpStatus.CONFLICT,
            "TRAINSET_NOT_AVAILABLE", "정비 중이거나 운행정지된 편성은 배차할 수 없습니다."),

    // ---------- 배차 ----------
    DISPATCH_NOT_FOUND(HttpStatus.NOT_FOUND,
            "RESOURCE_NOT_FOUND", "존재하지 않는 배차입니다."),
    INVALID_DISPATCH_TIME(HttpStatus.BAD_REQUEST,
            "INVALID_DISPATCH_TIME", "도착시간은 출발시간보다 늦어야 합니다."),
    DISPATCH_TIME_CONFLICT(HttpStatus.CONFLICT,
            "SCHEDULE_CONFLICT", "해당 편성은 같은 시간대에 이미 배정되어 있습니다."),
    DISPATCH_CANCELLED(HttpStatus.BAD_REQUEST,
            "DISPATCH_CANCELLED", "취소된 배차는 수정할 수 없습니다."),

    // ---------- 노선 / 운행 ----------
    ROUTE_NOT_FOUND(HttpStatus.NOT_FOUND,
            "RESOURCE_NOT_FOUND", "노선 정보를 찾을 수 없습니다."),

    // ---------- 민원 ----------
    COMPLAIN_NOT_FOUND(HttpStatus.NOT_FOUND,
            "RESOURCE_NOT_FOUND", "존재하지 않는 민원입니다."),
    COMPLAIN_NOT_MODIFIABLE(HttpStatus.CONFLICT,
            "COMPLAIN_NOT_MODIFIABLE", "접수 대기 상태의 민원만 수정하거나 삭제할 수 있습니다."),
    INVALID_ANSWER_STATUS(HttpStatus.BAD_REQUEST,
            "INVALID_ANSWER_STATUS", "답변 처리상태는 답변중, 답변완료, 이관안내 중에서 선택해주세요."),
    COMPLAIN_NOT_OWNER(HttpStatus.FORBIDDEN,
            "COMPLAIN_NOT_OWNER", "본인이 작성한 민원만 수정하거나 삭제할 수 있습니다."),

    // ---------- 첨부파일 ----------
    FILE_TOO_MANY(HttpStatus.BAD_REQUEST,
            "FILE_TOO_MANY", "첨부파일은 최대 5개까지 올릴 수 있습니다."),
    FILE_TOO_LARGE(HttpStatus.valueOf(413),
            "FILE_TOO_LARGE", "파일 하나의 크기는 10MB 이하여야 합니다."),
    FILE_EXTENSION_NOT_ALLOWED(HttpStatus.valueOf(415),
            "UNSUPPORTED_FILE_TYPE", "이미지(jpg, png, gif) 또는 PDF 파일만 첨부할 수 있습니다."),
    UNSUPPORTED_IMAGE_TYPE(HttpStatus.valueOf(415),
            "UNSUPPORTED_FILE_TYPE", "프로필 이미지는 jpg, png, gif 파일만 올릴 수 있습니다."),
    FILE_INVALID_NAME(HttpStatus.BAD_REQUEST,
            "FILE_INVALID_NAME", "파일 이름이 올바르지 않습니다."),
    FILE_INVALID_PATH(HttpStatus.BAD_REQUEST,
            "FILE_INVALID_PATH", "잘못된 파일 경로입니다."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR,
            "INTERNAL_ERROR", "파일 저장에 실패했습니다."),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND,
            "RESOURCE_NOT_FOUND", "파일을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
