package com.grtc.main.global.security;

import com.grtc.main.login.dto.LoginResponseDto;
import com.grtc.main.login.entity.AdminPage;
import com.grtc.main.login.entity.AdminType;
import com.grtc.main.login.entity.LoginEntity;
import com.grtc.main.login.entity.Role;
import com.grtc.main.login.repository.LoginRepository;
import com.grtc.main.login.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.EnumSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 관리자 유형별 열람 가능 페이지 확인 (실제 보안 설정을 그대로 거친다. 테스트가 끝나면 전부 롤백)
//   시스템 관리자: 모든 페이지 / 부서장: 대시보드 / 민원 담당자: 대시보드, 민원관리 / 차량 담당자: 대시보드, 차량관리
@SpringBootTest
@Transactional
class AdminPageAccessTest {

    private static final String MY_INFO_API = "/api/v1/admin/me";

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private LoginRepository loginRepository;
    @Autowired
    private TokenService tokenService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    // 해당 권한/유형의 회원을 만들고 그 회원의 Access 토큰을 돌려준다.
    private String tokenOf(Role role, AdminType type) {
        LoginEntity member = loginRepository.save(LoginEntity.builder()
                .loginId("pg" + System.nanoTime() % 1_000_000_000L).password("x").name("권한테스트")
                .role(role).adminType(type).build());
        return tokenService.createAccessToken(LoginResponseDto.from(member));
    }

    private int statusOf(String token, MockHttpServletRequestBuilder request) throws Exception {
        return mockMvc.perform(request.header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getStatus();
    }

    // 여섯 페이지의 목록 API 를 모두 불러 보고, 허용된 페이지만 통과하는지 확인한다.
    private void assertOnlyThesePagesOpen(String token, Set<AdminPage> allowed) throws Exception {
        for (AdminPage page : AdminPage.values()) {
            int status = statusOf(token, get(page.getApiPrefix()));
            if (allowed.contains(page)) {
                assertThat(status).as("%s 는 열려야 한다", page.getLabel()).isNotIn(401, 403);
            } else {
                assertThat(status).as("%s 는 막혀야 한다", page.getLabel()).isEqualTo(403);
            }
        }
    }

    @Test
    void 시스템_관리자는_모든_페이지를_볼_수_있다() throws Exception {
        String token = tokenOf(Role.ADMIN, AdminType.SYSTEM_ADMIN);
        assertOnlyThesePagesOpen(token, EnumSet.allOf(AdminPage.class));
    }

    @Test
    void 부서장은_대시보드만_볼_수_있다() throws Exception {
        String token = tokenOf(Role.ADMIN, AdminType.DEPARTMENT_HEAD);
        assertOnlyThesePagesOpen(token, EnumSet.of(AdminPage.DASHBOARD));
        assertThat(statusOf(token, get(AdminPage.DASHBOARD.getApiPrefix()))).isEqualTo(200);
    }

    @Test
    void 민원_담당자는_대시보드와_민원관리만_볼_수_있다() throws Exception {
        String token = tokenOf(Role.ADMIN, AdminType.COMPLAINT_MANAGER);
        assertOnlyThesePagesOpen(token, EnumSet.of(AdminPage.DASHBOARD, AdminPage.COMPLAINTS));
        assertThat(statusOf(token, get(AdminPage.COMPLAINTS.getApiPrefix()))).isEqualTo(200);
    }

    @Test
    void 차량_담당자는_대시보드와_차량관리만_볼_수_있다() throws Exception {
        String token = tokenOf(Role.ADMIN, AdminType.VEHICLE_MANAGER);
        assertOnlyThesePagesOpen(token, EnumSet.of(AdminPage.DASHBOARD, AdminPage.VEHICLES));
        assertThat(statusOf(token, get(AdminPage.VEHICLES.getApiPrefix()))).isEqualTo(200);
    }

    // 이 기능 전에 만들어진 관리자 계정(유형이 비어 있음)은 지금처럼 모든 페이지를 쓸 수 있어야 한다.
    @Test
    void 유형이_비어_있는_관리자는_시스템_관리자로_취급한다() throws Exception {
        String token = tokenOf(Role.ADMIN, null);
        assertOnlyThesePagesOpen(token, EnumSet.allOf(AdminPage.class));
    }

    @Test
    void 일반회원은_관리자_페이지를_하나도_볼_수_없다() throws Exception {
        String token = tokenOf(Role.USER, null);
        assertOnlyThesePagesOpen(token, EnumSet.noneOf(AdminPage.class));
        assertThat(statusOf(token, get(MY_INFO_API))).isEqualTo(403);
    }

    @Test
    void 내_정보_API는_모든_관리자가_쓸_수_있다() throws Exception {
        for (AdminType type : AdminType.values()) {
            assertThat(statusOf(tokenOf(Role.ADMIN, type), get(MY_INFO_API))).as(type.getLabel()).isEqualTo(200);
        }
    }

    // 목록 조회뿐 아니라 그 페이지의 하위 주소, 등록·수정·삭제 요청도 함께 막혀야 한다.
    @Test
    void 볼_수_없는_페이지는_하위_주소와_변경_요청도_막힌다() throws Exception {
        String complaintManager = tokenOf(Role.ADMIN, AdminType.COMPLAINT_MANAGER);
        assertThat(statusOf(complaintManager, get("/api/v1/admin/vehicles/summary"))).isEqualTo(403);
        assertThat(statusOf(complaintManager, delete("/api/v1/admin/vehicles/1"))).isEqualTo(403);
        assertThat(statusOf(complaintManager, patch("/api/v1/admin/members/1/role"))).isEqualTo(403);

        String vehicleManager = tokenOf(Role.ADMIN, AdminType.VEHICLE_MANAGER);
        assertThat(statusOf(vehicleManager, get("/api/admin/complaints/1"))).isEqualTo(403);
        assertThat(statusOf(vehicleManager, post("/api/admin/complaints"))).isEqualTo(403);
        assertThat(statusOf(vehicleManager, post("/api/v1/admin/dispatches"))).isEqualTo(403);

        String departmentHead = tokenOf(Role.ADMIN, AdminType.DEPARTMENT_HEAD);
        assertThat(statusOf(departmentHead, get("/api/v1/admin/dashboard/periods"))).isEqualTo(200);
        assertThat(statusOf(departmentHead, get("/api/v1/admin/operations"))).isEqualTo(403);
    }

    // 프론트가 메뉴를 그릴 때 쓰는 값: 로그인/내 정보 응답의 adminType, pages
    @Test
    void 내_정보_응답에_관리자_유형과_열람_가능_페이지가_내려간다() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + tokenOf(Role.ADMIN, AdminType.COMPLAINT_MANAGER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("ADMIN"))
                .andExpect(jsonPath("$.data.adminType").value("COMPLAINT_MANAGER"))
                .andExpect(jsonPath("$.data.adminTypeLabel").value("민원 담당자"))
                .andExpect(jsonPath("$.data.pages.length()").value(2))
                .andExpect(jsonPath("$.data.pages[0].code").value("DASHBOARD"))
                .andExpect(jsonPath("$.data.pages[1].code").value("COMPLAINTS"))
                .andExpect(jsonPath("$.data.pages[1].label").value("민원관리"))
                .andExpect(jsonPath("$.data.pages[1].path").value("/dashboard/complaints"));

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + tokenOf(Role.USER, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("USER"))
                .andExpect(jsonPath("$.data.adminType").doesNotExist())
                .andExpect(jsonPath("$.data.pages.length()").value(0));
    }

    // 주소 판별 규칙 (DB 없이 확인)
    @Test
    void 페이지에_속하지_않는_관리자_API는_시스템_관리자만_쓸_수_있다() {
        String unknown = "/api/v1/admin/something-new";
        assertThat(AdminType.SYSTEM_ADMIN.canCallApi(unknown)).isTrue();
        assertThat(AdminType.DEPARTMENT_HEAD.canCallApi(unknown)).isFalse();
        assertThat(AdminType.COMPLAINT_MANAGER.canCallApi(unknown)).isFalse();
        assertThat(AdminType.VEHICLE_MANAGER.canCallApi(unknown)).isFalse();

        // 이름만 비슷한 주소는 그 페이지로 보지 않는다.
        assertThat(AdminType.DEPARTMENT_HEAD.canCallApi("/api/v1/admin/dashboardX")).isFalse();
        assertThat(AdminType.DEPARTMENT_HEAD.canCallApi("/api/v1/admin/dashboard/periods")).isTrue();
        assertThat(AdminType.VEHICLE_MANAGER.canCallApi("/api/v1/admin/vehicles/3")).isTrue();
        assertThat(AdminType.VEHICLE_MANAGER.canCallApi("/api/v1/admin/meX")).isFalse();
        assertThat(AdminType.VEHICLE_MANAGER.canCallApi("/api/v1/admin/me")).isTrue();
    }
}
