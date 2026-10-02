package com.grtc.dashboard.member;

import com.grtc.dashboard.global.common.DateTimes;
import com.grtc.dashboard.member.entity.MemberEntity;
import com.grtc.dashboard.member.entity.MemberStatus;
import com.grtc.dashboard.member.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.List;

// 회원관리 / 회원정보 화면에서 쓰는 요청·응답 모음
public final class MemberDto {

    // 프로필 이미지 주소 앞부분. 이미지는 main 서버(8081)가 내려준다. (GET /api/v1/files/profile/{파일명})
    private static final String PROFILE_URL_PREFIX = "/api/v1/files/profile/";

    private MemberDto() {
    }

    // 회원관리 목록 한 줄 (연락처는 010-****-1234 처럼 가려서 내려준다)
    //   joinedAt: ISO-8601 (예: 2026-09-01T09:00:00+09:00)
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
            OffsetDateTime joinedAt
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
                    DateTimes.toOffset(m.getCreatedAt())
            );
        }
    }

    // 회원정보(상세) 화면 (관리자 화면이므로 연락처 전체 표시)
    //   profileImageUrl / profileThumbnailUrl: main 서버 기준 경로, 없으면 null
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
        public static Detail from(MemberEntity m) {
            return new Detail(
                    m.getId(),
                    m.getName(),
                    m.getLoginId(),
                    m.getEmail(),
                    m.getPhone(),
                    m.getDepartment(),
                    m.getPosition(),
                    profileUrl(m.getProfileImage()),
                    profileUrl(m.getProfileThumbnail()),
                    m.getRole(),
                    m.getRole().getLabel(),
                    m.getStatus(),
                    m.getStatus().getLabel(),
                    DateTimes.toOffset(m.getCreatedAt())
            );
        }
    }

    // 회원정보 수정 요청 (PATCH: 보낸 항목만 바뀐다. 보내지 않은 항목(null)은 그대로 둔다)
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

    // 저장 파일명 -> 프론트에 내려줄 주소 (없으면 null)
    private static String profileUrl(String storedName) {
        return (storedName == null || storedName.isBlank()) ? null : PROFILE_URL_PREFIX + storedName;
    }
}
