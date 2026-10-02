package com.grtc.main.member;

import com.grtc.main.global.common.DateTimes;
import com.grtc.main.login.entity.LoginEntity;
import com.grtc.main.login.entity.MemberStatus;
import com.grtc.main.login.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.List;

// 내 정보(회원정보) 화면에서 쓰는 요청·응답 모음
public final class MemberDto {

    private MemberDto() {
    }

    // 회원정보(상세) 화면 (본인 정보이므로 연락처 전체 표시)
    //   profileImageUrl / profileThumbnailUrl: main 서버 기준 경로 (예: /api/v1/files/profile/xxxx.png), 없으면 null
    //   joinedAt: ISO-8601 (예: 2026-10-01T14:40:00+09:00)
    public record Detail(
            Long id,
            String name,
            String loginId,
            String email,
            String phone,
            String department,
            String position,
            String profileImageUrl,
            String profileThumbnailUrl,
            Role role,
            String roleLabel,
            MemberStatus status,
            String statusLabel,
            OffsetDateTime joinedAt
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
                    ProfileImageService.urlOf(m.getProfileImage()),
                    ProfileImageService.urlOf(m.getProfileThumbnail()),
                    m.getRole(),
                    m.getRole().getLabel(),
                    m.getStatus(),
                    m.getStatus().getLabel(),
                    DateTimes.toOffset(m.getCreatedAt())
            );
        }
    }

    // 내 정보 수정 요청 (PATCH: 보낸 항목만 바뀐다. 보내지 않은 항목(null)은 그대로 둔다)
    //   - name, email 은 보낼 경우 비워둘 수 없다.
    //   - phone, department, position 은 빈 문자열("")로 보내면 값이 지워진다.
    public record UpdateRequest(
            @Pattern(regexp = "(?s).*\\S.*", message = "필수 입력란입니다.")
            @Size(max = 30, message = "이름은 최대 30자까지 입력 가능합니다.")
            String name,

            @Pattern(regexp = "(?s).*\\S.*", message = "필수 입력란입니다.")
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

    // 비밀번호 변경 요청 (새 비밀번호 규칙은 회원가입과 같다: 8~64자, 특수문자 1개 이상)
    public record PasswordRequest(
            @NotBlank(message = "필수 입력란입니다.")
            String currentPassword,

            @NotBlank(message = "필수 입력란입니다.")
            @Size(min = 8, max = 64, message = "비밀번호는 8자 이상 64자 이하여야 합니다.")
            @Pattern(regexp = "^(?=.*[^A-Za-z0-9]).*$", message = "비밀번호에 특수문자를 1개 이상 포함해야 합니다.")
            String newPassword,

            @NotBlank(message = "필수 입력란입니다.")
            String newPasswordConfirm
    ) {
    }

    // 프로필 이미지 변경 응답
    public record ProfileImage(String profileImageUrl, String profileThumbnailUrl) {
    }

    // 소속 부서 / 직급 선택 목록
    public record Options(List<String> departments, List<String> positions) {
    }
}
