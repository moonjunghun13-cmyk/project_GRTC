package com.grtc.main.login.dto;

import com.grtc.main.login.entity.LoginEntity;
import com.grtc.main.login.entity.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignUpResponseDto {

    private Long id;
    private String nickname;
    private String name;
    private String email;
    private Role role;

    public static SignUpResponseDto from(LoginEntity entity){
        return SignUpResponseDto.builder()
                .id(entity.getId())
                .nickname(entity.getNickname())
                .name(entity.getName())
                .email(entity.getEmail())
                .role(entity.getRole())
                .build();
    }
}
