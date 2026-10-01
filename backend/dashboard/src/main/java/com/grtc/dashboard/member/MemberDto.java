package com.grtc.dashboard.member;

import com.grtc.dashboard.member.entity.MemberEntity;
import com.grtc.dashboard.member.entity.MemberStatus;
import com.grtc.dashboard.member.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

// 회원관리 / 회원정보 화면에서 쓰는 요청·응답 모음
public final class MemberDto {

    private MemberDto() {
    }

    // 회원관리 목록 한 줄 (연락처는 010-****-1234 처럼 가려서 내려준다)
    public record ListItem(
            int no,
            Long id,
            String name,
            String loginId,
            String email,
            String phone,
            Role role,
            String roleLabel,
            MemberStatus status,
            String statusLabel,
            LocalDateTime joinedAt
    ) {
        public static ListItem of(int no, MemberEntity m) {
            return new ListItem(
                    no,
                    m.getId(),
                    m.getName(),
                    m.getLoginId(),
                    m.getEmail(),
                    maskPhone(m.getPhone()),
                    m.getRole(),
                    m.getRole().getLabel(),
                    m.getStatus(),
                    m.getStatus().getLabel(),
                    m.getCreatedAt()
            );
        }
    }

    // 회원정보(상세) 화면 (관리자 화면이므로 연락처 전체 표시)
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
        public static Detail from(MemberEntity m) {
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

    // 회원 상태 변경 요청 (정상 / 이용정지 / 탈퇴)
    public record StatusRequest(
            @NotNull(message = "필수 선택 항목입니다.") MemberStatus status
    ) {
    }

    // 회원 권한 변경 요청 (관리자 / 일반회원)
    public record RoleRequest(
            @NotNull(message = "필수 선택 항목입니다.") Role role
    ) {
    }

    // 소속 부서 / 직급 선택 목록
    public record Options(List<String> departments, List<String> positions) {
    }

    // 연락처 가리기: 010-1234-5678 -> 010-****-5678
    static String maskPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return phone;
        }
        if (phone.matches("^\\d{2,3}-\\d{3,4}-\\d{4}$")) {
            return phone.replaceAll("^(\\d{2,3})-\\d{3,4}-(\\d{4})$", "$1-****-$2");
        }
        // 예상 못한 형식이면 뒤 4자리만 남기고 가린다.
        return phone.length() > 4 ? "****" + phone.substring(phone.length() - 4) : "****";
    }
}
