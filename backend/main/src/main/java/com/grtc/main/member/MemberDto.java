package com.grtc.main.member;

import com.grtc.main.login.entity.LoginEntity;
import com.grtc.main.login.entity.MemberStatus;
import com.grtc.main.login.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

// 내 정보(회원정보) 화면에서 쓰는 요청·응답 모음
public final class MemberDto {

    private MemberDto() {
    }

    // 회원정보(상세) 화면 (본인 정보이므로 연락처 전체 표시)
    public record Detail(
            Long id,
            String name,
            String loginId,
            String email,
            String phone,
            String department,
            String position,
            Role role,
            String roleLabel,
            MemberStatus status,
            String statusLabel,
            LocalDateTime joinedAt
    ) {
        public static Detail from(LoginEntity m) {
            return new Detail(
                    m.getId(),
                    m.getName(),
                    m.getLoginId(),
                    m.getEmail(),
                    m.getPhone(),
                    m.getDepartment(),
                    m.getPosition(),
                    m.getRole(),
                    m.getRole().getLabel(),
                    m.getStatus(),
                    m.getStatus().getLabel(),
                    m.getCreatedAt()
            );
        }
    }

    // 회원정보 수정 요청 (이름, 이메일, 연락처, 소속 부서, 직급)
    public record UpdateRequest(
            @NotBlank(message = "필수 입력란입니다.")
            @Size(max = 30, message = "이름은 최대 30자까지 입력 가능합니다.")
            String name,

            @NotBlank(message = "필수 입력란입니다.")
            @Email(message = "이메일 형식에 맞지 않습니다.")
            @Size(max = 100, message = "이메일은 최대 100자까지 입력 가능합니다.")
            String email,

            @Pattern(regexp = "^$|^01[016789]-\\d{3,4}-\\d{4}$",
                    message = "연락처는 010-1234-5678 형식으로 입력해주세요.")
            String phone,

            @Size(max = 50, message = "소속 부서는 최대 50자까지 입력 가능합니다.")
            String department,

            @Size(max = 50, message = "직급은 최대 50자까지 입력 가능합니다.")
            String position
    ) {
    }

    // 소속 부서 / 직급 선택 목록
    public record Options(List<String> departments, List<String> positions) {
    }
}
