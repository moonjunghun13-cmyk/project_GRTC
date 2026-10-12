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
    void 부서장은_대시보드와_공지_자료실만_볼_수_있다() throws Exception {
        String token = tokenOf(Role.ADMIN, AdminType.DEPARTMENT_HEAD);
        assertOnlyThesePagesOpen(token, EnumSet.of(AdminPage.DASHBOARD, AdminPage.NOTICES, AdminPage.ARCHIVES));
        assertThat(statusOf(token, get(AdminPage.DASHBOARD.getApiPrefix()))).isEqualTo(200);
    }

    @Test
    void 민원_담당자는_대시보드_민원관리_공지_자료실만_볼_수_있다() throws Exception {
        String token = tokenOf(Role.ADMIN, AdminType.COMPLAINT_MANAGER);
        assertOnlyThesePagesOpen(token, EnumSet.of(AdminPage.DASHBOARD, AdminPage.COMPLAINTS,
                AdminPage.NOTICES, AdminPage.ARCHIVES));
        assertThat(statusOf(token, get(AdminPage.COMPLAINTS.getApiPrefix()))).isEqualTo(200);
    }

    @Test
    void 차량_담당자는_대시보드_차량관리_배차관리_공지_자료실만_볼_수_있다() throws Exception {
        String token = tokenOf(Role.ADMIN, AdminType.VEHICLE_MANAGER);
        assertOnlyThesePagesOpen(token, EnumSet.of(AdminPage.DASHBOARD, AdminPage.VEHICLES, AdminPage.DISPATCHES,
                AdminPage.NOTICES, AdminPage.ARCHIVES));
        assertThat(statusOf(token, get(AdminPage.VEHICLES.getApiPrefix()))).isEqualTo(200);
        assertThat(statusOf(token, get(AdminPage.DISPATCHES.getApiPrefix()))).isEqualTo(200);
        assertThat(statusOf(token, get("/api/v1/admin/dispatches/options"))).isEqualTo(200);
    }

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

    @Test
    void 볼_수_없는_페이지는_하위_주소와_변경_요청도_막힌다() throws Exception {
        String complaintManager = tokenOf(Role.ADMIN, AdminType.COMPLAINT_MANAGER);
        assertThat(statusOf(complaintManager, get("/api/v1/admin/vehicles/summary"))).isEqualTo(403);
        assertThat(statusOf(complaintManager, delete("/api/v1/admin/vehicles/1"))).isEqualTo(403);
        assertThat(statusOf(complaintManager, patch("/api/v1/admin/members/1/role"))).isEqualTo(403);

        String vehicleManager = tokenOf(Role.ADMIN, AdminType.VEHICLE_MANAGER);
        assertThat(statusOf(vehicleManager, get("/api/admin/complaints/1"))).isEqualTo(403);
        assertThat(statusOf(vehicleManager, post("/api/admin/complaints"))).isEqualTo(403);
        assertThat(statusOf(vehicleManager, get("/api/v1/admin/operations"))).isEqualTo(403);
        assertThat(statusOf(vehicleManager, patch("/api/v1/admin/members/1/role"))).isEqualTo(403);

        String departmentHead = tokenOf(Role.ADMIN, AdminType.DEPARTMENT_HEAD);
        assertThat(statusOf(departmentHead, get("/api/v1/admin/dashboard/periods"))).isEqualTo(200);
        assertThat(statusOf(departmentHead, get("/api/v1/admin/operations"))).isEqualTo(403);
    }

    @Test
    void 내_정보_응답에_관리자_유형과_열람_가능_페이지가_내려간다() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + tokenOf(Role.ADMIN, AdminType.COMPLAINT_MANAGER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("ADMIN"))
                .andExpect(jsonPath("$.data.adminType").value("COMPLAINT_MANAGER"))
                .andExpect(jsonPath("$.data.adminTypeLabel").value("민원 담당자"))
                .andExpect(jsonPath("$.data.pages.length()").value(4))
                .andExpect(jsonPath("$.data.pages[0].code").value("DASHBOARD"))
                .andExpect(jsonPath("$.data.pages[1].code").value("COMPLAINTS"))
                .andExpect(jsonPath("$.data.pages[1].label").value("민원관리"))
                .andExpect(jsonPath("$.data.pages[1].path").value("/dashboard/complaints"))
                .andExpect(jsonPath("$.data.pages[2].code").value("NOTICES"))
                .andExpect(jsonPath("$.data.pages[3].code").value("ARCHIVES"));

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

        assertThat(AdminType.DEPARTMENT_HEAD.canCallApi("/api/v1/admin/dashboardX")).isFalse();
        assertThat(AdminType.DEPARTMENT_HEAD.canCallApi("/api/v1/admin/dashboard/periods")).isTrue();
        assertThat(AdminType.VEHICLE_MANAGER.canCallApi("/api/v1/admin/vehicles/3")).isTrue();
        assertThat(AdminType.VEHICLE_MANAGER.canCallApi("/api/v1/admin/dispatches/3/cancel")).isTrue();
        assertThat(AdminType.VEHICLE_MANAGER.canCallApi("/api/v1/admin/operations")).isFalse();
        for (AdminType type : AdminType.values()) {
            assertThat(type.canCallApi("/api/v1/admin/notices/1")).as(type.getLabel()).isTrue();
            assertThat(type.canCallApi("/api/v1/admin/archives/1/files/2")).as(type.getLabel()).isTrue();
        }
        assertThat(AdminType.VEHICLE_MANAGER.canCallApi("/api/v1/admin/meX")).isFalse();
        assertThat(AdminType.VEHICLE_MANAGER.canCallApi("/api/v1/admin/me")).isTrue();
    }
}
