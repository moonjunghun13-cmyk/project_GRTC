package com.grtc.main.login.dto;

import com.grtc.main.login.entity.LoginEntity;
import com.grtc.main.login.entity.Role;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SignUpRequestDto {

    @NotBlank(message = "필수 입력란입니다.")
    @Size(max = 30, message = "닉네임은 최대 30자까지 입력 가능합니다.")
    private String nickname;

    @NotBlank(message = "필수 입력란입니다.")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    @Pattern(regexp = "^(?=.*[^A-Za-z0-9]).*$", message = "비밀번호에 특수문자를 1개 이상 포함해야 합니다.")
    private String password;

    @NotBlank(message = "필수 입력란입니다.")
    private String name;

    @NotBlank(message = "필수 입력란입니다.")
    @Email(message = "이메일 형식에 맞지 않습니다.")
    private String email;

    @AssertTrue(message = "만 14세 이상만 가입할 수 있습니다.")
    private boolean over14;

    public LoginEntity toEntity(String encodedPassword){
        return LoginEntity.builder()
                .nickname(nickname)
                .password(encodedPassword)
                .name(name)
                .email(email)
                .role(Role.USER)
                .build();
    }
}
