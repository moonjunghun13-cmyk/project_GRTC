package com.grtc.main.login.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginRequestDto {

    @NotBlank(message = "필수 입력란입니다.")
    private String nickname;

    @NotBlank(message = "필수 입력란입니다.")
    private String password;
}
