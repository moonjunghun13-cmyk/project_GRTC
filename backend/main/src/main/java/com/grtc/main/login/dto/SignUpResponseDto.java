package com.grtc.main.login.dto;

import com.grtc.main.login.entity.LoginEntity;
import com.grtc.main.login.entity.Role;
import lombok.Builder;
import lombok.Getter;

// 회원가입 완료 시 내려주는 회원 정보(비밀번호 제외) DTO
@Getter
@Builder
public class SignUpResponseDto {

    private Long id; // 회원 고유 ID
    private String loginId; // 로그인 아이디
    private String name; // 이름
    private String email; // 이메일
    private Role role; // 회원 권한

    // LoginEntity를 응답 DTO로 변환하는 정적 팩토리 메서드
    public static SignUpResponseDto from(LoginEntity entity){
        // 엔티티의 값을 응답 DTO에 복사(비밀번호는 제외)
        return SignUpResponseDto.builder()
                .id(entity.getId())
                .loginId(entity.getLoginId())
                .name(entity.getName())
                .email(entity.getEmail())
                .role(entity.getRole())
                .build();
    }
}
