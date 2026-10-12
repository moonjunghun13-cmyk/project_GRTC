package com.grtc.main.admin.member;

import com.grtc.main.global.common.DateTimes;
import com.grtc.main.login.entity.LoginEntity;
import com.grtc.main.login.entity.MemberStatus;
import com.grtc.main.login.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.List;

public final class AdminMemberDto {

    private static final String PROFILE_URL_PREFIX = "/api/v1/files/profile/";

    private AdminMemberDto() {
    }

    public record ListItem(
            int no,
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
            OffsetDateTime joinedAt
    ) {
        public static ListItem of(int no, LoginEntity m) {
            return new ListItem(
                    no,
                    m.getId(),
                    m.getName(),
                    m.getLoginId(),
                    m.getEmail(),
                    maskPhone(m.getPhone()),
                    m.getDepartment(),
                    m.getPosition(),
                    m.getRole(),
                    m.getRole().getLabel(),
                    m.getStatus(),
                    m.getStatus().getLabel(),
                    DateTimes.toOffset(m.getCreatedAt())
            );
        }
    }

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

    public record StatusRequest(
            @NotNull(message = "필수 선택 항목입니다.") MemberStatus status
    ) {
    }

    public record RoleRequest(
            @NotNull(message = "필수 선택 항목입니다.") Role role
    ) {
    }

    public record Options(List<String> departments, List<String> positions) {
    }

    static String maskPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return phone;
        }
        if (phone.matches("^\\d{2,3}-\\d{3,4}-\\d{4}$")) {
            return phone.replaceAll("^(\\d{2,3})-\\d{3,4}-(\\d{4})$", "$1-****-$2");
        }
        return phone.length() > 4 ? "****" + phone.substring(phone.length() - 4) : "****";
    }

    private static String profileUrl(String storedName) {
        return (storedName == null || storedName.isBlank()) ? null : PROFILE_URL_PREFIX + storedName;
    }
}
