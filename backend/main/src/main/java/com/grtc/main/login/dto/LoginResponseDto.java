package com.grtc.main.login.dto;

import com.grtc.main.login.entity.AdminPage;
import com.grtc.main.login.entity.AdminType;
import com.grtc.main.login.entity.LoginEntity;
import com.grtc.main.login.entity.Role;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

// 로그인 성공 / 내 정보 조회 시 내려주는 DTO
// redirectPath 로 프론트가 로그인 직후 이동할 화면을 알 수 있다.
//   관리자(ADMIN) -> /dashboard  (대시보드. 네 가지 관리자 유형 모두 대시보드는 볼 수 있다)
//   일반 사용자(USER) -> /complaints  (민원관리)
@Getter
@Builder
public class LoginResponseDto {

    public static final String ADMIN_HOME = "/dashboard";
    public static final String USER_HOME = "/complaints";

    private Long id; // 회원 고유 ID
    private String loginId; // 로그인 아이디 (사이드바 하단 표시용)
    private String name; // 이름 (사이드바 하단 표시용)
    private Role role; // 회원 권한 (ADMIN / USER) - 메뉴 노출 여부 판단용
    private String roleLabel; // 권한 표시 이름 (관리자 / 일반회원)
    private String redirectPath; // 로그인 후 이동할 화면 경로

    // ---- 관리자 유형별 열람 가능 페이지 (일반회원은 adminType/adminTypeLabel 이 null, pages 는 빈 목록) ----
    private AdminType adminType; // SYSTEM_ADMIN / DEPARTMENT_HEAD / COMPLAINT_MANAGER / VEHICLE_MANAGER
    private String adminTypeLabel; // 시스템 관리자 / 부서장 / 민원 담당자 / 차량 담당자
    private List<PageInfo> pages; // 이 관리자가 볼 수 있는 페이지 (메뉴 순서). 프론트는 이 목록에 있는 메뉴만 보여주면 된다.

    // 열람 가능 페이지 한 개 (code: DASHBOARD 등, label: 메뉴 이름, path: 프론트 화면 주소)
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
