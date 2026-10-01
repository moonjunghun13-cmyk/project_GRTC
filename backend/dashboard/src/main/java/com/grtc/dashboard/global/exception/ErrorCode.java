package com.grtc.dashboard.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ---------- 로그인 / 회원 (L) ----------
    DUPLICATE_LOGIN_ID(HttpStatus.CONFLICT,
            "L001", "이미 사용 중인 아이디입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT,
            "L002", "이미 사용 중인 이메일입니다."),
    DUPLICATE_ACCOUNT(HttpStatus.CONFLICT,
            "L003", "이미 가입된 정보가 있습니다. 다시 시도해주세요."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED,
            "L004", "아이디 또는 비밀번호가 올바르지 않습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND,
            "L005", "존재하지 않는 회원입니다."),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST,
            "L006", "비밀번호가 일치하지 않습니다."),
    ACCOUNT_SUSPENDED(HttpStatus.FORBIDDEN,
            "L007", "이용이 정지된 계정입니다. 관리자에게 문의해주세요."),
    ACCOUNT_WITHDRAWN(HttpStatus.FORBIDDEN,
            "L008", "탈퇴한 계정입니다."),
    CANNOT_MODIFY_SELF(HttpStatus.BAD_REQUEST,
            "L009", "본인의 권한과 상태는 변경할 수 없습니다."),

    // ---------- 권한 (A) ----------
    ADMIN_ONLY(HttpStatus.FORBIDDEN,
            "A001", "관리자 페이지이므로 열람이 불가합니다."),
    LOGIN_REQUIRED(HttpStatus.UNAUTHORIZED,
            "A002", "로그인이 필요합니다."),

    // ---------- 공통 (C) ----------
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST,
            "C003", "요청 값의 형식이 올바르지 않습니다."),

    // ---------- 차량 (V) ----------
    VEHICLE_NOT_FOUND(HttpStatus.NOT_FOUND,
            "V001", "존재하지 않는 차량입니다."),
    DUPLICATE_VEHICLE_NO(HttpStatus.CONFLICT,
            "V002", "이미 등록된 차량번호입니다."),
    VEHICLE_IN_USE(HttpStatus.CONFLICT,
            "V003", "배차 또는 운행 이력이 있는 차량은 삭제할 수 없습니다."),

    // ---------- 배차 (D) ----------
    DISPATCH_NOT_FOUND(HttpStatus.NOT_FOUND,
            "D001", "존재하지 않는 배차입니다."),
    INVALID_DISPATCH_TIME(HttpStatus.BAD_REQUEST,
            "D002", "도착시간은 출발시간보다 늦어야 합니다."),
    DISPATCH_TIME_CONFLICT(HttpStatus.CONFLICT,
            "D003", "해당 차량에 시간이 겹치는 배차가 있습니다."),
    DISPATCH_CANCELLED(HttpStatus.BAD_REQUEST,
            "D004", "취소된 배차는 수정할 수 없습니다."),

    // ---------- 노선 / 운행 (R) ----------
    ROUTE_NOT_FOUND(HttpStatus.NOT_FOUND,
            "R001", "노선 정보를 찾을 수 없습니다."),

    // ---------- 민원 (Q) ----------
    COMPLAIN_NOT_FOUND(HttpStatus.NOT_FOUND,
            "Q001", "존재하지 않는 민원입니다."),
    COMPLAIN_NOT_MODIFIABLE(HttpStatus.CONFLICT,
            "Q002", "접수 대기 상태의 민원만 수정하거나 삭제할 수 있습니다."),
    INVALID_ANSWER_STATUS(HttpStatus.BAD_REQUEST,
            "Q003", "답변 처리상태는 접수, 이관, 답변완료 중에서 선택해주세요."),
    COMPLAIN_NOT_OWNER(HttpStatus.FORBIDDEN,
            "Q004", "본인이 작성한 민원만 수정하거나 삭제할 수 있습니다."),

    // ---------- 첨부파일 (F) ----------
    FILE_TOO_MANY(HttpStatus.BAD_REQUEST,
            "F001", "첨부파일은 최대 5개까지 올릴 수 있습니다."),
    FILE_TOO_LARGE(HttpStatus.BAD_REQUEST,
            "F002", "파일 하나의 크기는 10MB 이하여야 합니다."),
    FILE_EXTENSION_NOT_ALLOWED(HttpStatus.BAD_REQUEST,
            "F003", "이미지(jpg, png, gif) 또는 PDF 파일만 첨부할 수 있습니다."),
    FILE_INVALID_NAME(HttpStatus.BAD_REQUEST,
            "F004", "파일 이름이 올바르지 않습니다."),
    FILE_INVALID_PATH(HttpStatus.BAD_REQUEST,
            "F005", "잘못된 파일 경로입니다."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR,
            "F006", "파일 저장에 실패했습니다."),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND,
            "F007", "파일을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
