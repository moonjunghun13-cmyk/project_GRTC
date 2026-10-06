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

// 회원가입 요청 데이터를 담고 입력값을 검증하는 DTO (화면: 이름, 아이디, 이메일, 비밀번호, 비밀번호 확인, 14세 이상)
@Getter
@Setter
@NoArgsConstructor
public class SignUpRequestDto {

    @NotBlank(message = "필수 입력란입니다.")
    @Size(max = 30, message = "이름은 최대 30자까지 입력 가능합니다.")
    private String name; // 이름(필수, 최대 30자)

    @NotBlank(message = "필수 입력란입니다.")
    @Size(min = 4, max = 20, message = "아이디는 4자 이상 20자 이하로 입력해주세요.")
    @Pattern(regexp = "^[A-Za-z0-9_]*$", message = "아이디는 영문, 숫자, 밑줄(_)만 사용할 수 있습니다.")
    private String loginId; // 로그인 아이디(필수, 4~20자, 영문/숫자/밑줄)

    @NotBlank(message = "필수 입력란입니다.")
    @Email(message = "이메일 형식에 맞지 않습니다.")
    @Size(max = 100, message = "이메일은 최대 100자까지 입력 가능합니다.")
    private String email; // 이메일(필수, 최대 100자, 중복 불가)

    @NotBlank(message = "필수 입력란입니다.")
    @Size(min = 8, max = 64, message = "비밀번호는 8자 이상 64자 이하여야 합니다.")
    @Pattern(regexp = "^(?=.*[^A-Za-z0-9]).*$", message = "비밀번호에 특수문자를 1개 이상 포함해야 합니다.")
    private String password; // 비밀번호(필수, 8자 이상, 특수문자 포함)

    @NotBlank(message = "필수 입력란입니다.")
    private String passwordConfirm; // 비밀번호 확인(필수, password 와 같아야 함)

    @AssertTrue(message = "만 14세 이상만 가입할 수 있습니다.")
    private boolean over14; // 만 14세 이상 동의 여부(true여야 가입 가능)

    // 비밀번호를 암호화한 값과 함께 USER 권한의 LoginEntity로 변환하는 메서드
    public LoginEntity toEntity(String encodedPassword){
        // 빌더로 엔티티 생성(암호화된 비밀번호 사용, 권한은 일반 사용자로 고정)
        return LoginEntity.builder()
                .loginId(loginId)
                .password(encodedPassword)
                .name(name)
                .email(email)
                .role(Role.USER)
                .build();
    }
}
