package com.grtc.main.login.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 로그인 요청 데이터(아이디, 비밀번호)를 담는 DTO
@Getter
@Setter
@NoArgsConstructor
public class LoginRequestDto {

    @NotBlank(message = "필수 입력란입니다.")
    private String loginId; // 로그인 아이디(필수)

    @NotBlank(message = "필수 입력란입니다.")
    private String password; // 로그인 비밀번호(필수)
}
