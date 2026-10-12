package com.grtc.main.login.dto;

import com.grtc.main.login.entity.AdminPage;
import com.grtc.main.login.entity.AdminType;
import com.grtc.main.login.entity.LoginEntity;
import com.grtc.main.login.entity.Role;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class LoginResponseDto {

    public static final String ADMIN_HOME = "/dashboard";
    public static final String USER_HOME = "/complaints";

    private Long id;
    private String loginId;
    private String name;
    private Role role;
    private String roleLabel;
    private String redirectPath;

    private AdminType adminType;
    private String adminTypeLabel;
    private List<PageInfo> pages;

    public record PageInfo(String code, String label, String path) {
        static PageInfo from(AdminPage page) {
            return new PageInfo(page.name(), page.getLabel(), page.getPath());
        }
    }

    public static LoginResponseDto from(LoginEntity entity) {
        AdminType adminType = entity.resolveAdminType();
        return LoginResponseDto.builder()
                .adminType(adminType)
                .adminTypeLabel(adminType == null ? null : adminType.getLabel())
                .pages(adminType == null ? List.of() : adminType.pageList().stream().map(PageInfo::from).toList())
                .id(entity.getId())
                .loginId(entity.getLoginId())
                .name(entity.getName())
                .role(entity.getRole())
                .roleLabel(entity.getRole().getLabel())
                .redirectPath(entity.getRole() == Role.ADMIN ? ADMIN_HOME : USER_HOME)
                .build();
    }
}
