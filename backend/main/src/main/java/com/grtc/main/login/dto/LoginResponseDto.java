package com.grtc.main.login.dto;

import com.grtc.main.login.entity.LoginEntity;
import com.grtc.main.login.entity.Role;
import lombok.Builder;
import lombok.Getter;

// 로그인 성공 / 내 정보 조회 시 내려주는 DTO
// redirectPath 로 프론트가 로그인 직후 이동할 화면을 알 수 있다.
//   관리자(ADMIN) -> /dashboard  (대시보드)
//   일반 사용자(USER) -> /complaints  (민원관리)
@Getter
@Builder
public class LoginResponseDto {

    public static final String ADMIN_HOME = "/dashboard";
    public static final String USER_HOME = "/complaints";

    private Long id; // 회원 고유 ID
    private String loginId; // 로그인 아이디 (사이드바 하단 표시용)
    private String name; // 이름 (사이드바 하단 표시용)
    private Role role; // 회원 권한 (ADMIN / USER) - 메뉴 노출 여부 판단용
    private String roleLabel; // 권한 표시 이름 (관리자 / 일반회원)
    private String redirectPath; // 로그인 후 이동할 화면 경로

    public static LoginResponseDto from(LoginEntity entity) {
        return LoginResponseDto.builder()
                .id(entity.getId())
                .loginId(entity.getLoginId())
                .name(entity.getName())
                .role(entity.getRole())
                .roleLabel(entity.getRole().getLabel())
                .redirectPath(entity.getRole() == Role.ADMIN ? ADMIN_HOME : USER_HOME)
                .build();
    }
}
