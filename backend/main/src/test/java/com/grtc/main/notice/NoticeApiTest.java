package com.grtc.main.notice;

import com.grtc.main.login.dto.LoginResponseDto;
import com.grtc.main.login.entity.AdminType;
import com.grtc.main.login.entity.LoginEntity;
import com.grtc.main.login.entity.Role;
import com.grtc.main.login.repository.LoginRepository;
import com.grtc.main.login.service.TokenService;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

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
class NoticeApiTest {

    private static final String ADMIN_API = "/api/v1/admin/notices";
    private static final String MEMBER_API = "/api/v1/notices";

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private LoginRepository loginRepository;
    @Autowired
    private TokenService tokenService;

    private MockMvc mockMvc;
    private String tag;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        tag = "공지테스트" + System.nanoTime() % 1_000_000_000L;
    }

    private String tokenOf(Role role, AdminType type) {
        LoginEntity member = loginRepository.save(LoginEntity.builder()
                .loginId("nt" + System.nanoTime() % 1_000_000_000L).password("x").name("공지작성자")
                .role(role).adminType(type).build());
        return tokenService.createAccessToken(LoginResponseDto.from(member));
    }

    private MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder request, String token) {
        return request.header("Authorization", "Bearer " + token);
    }

    private long createNotice(String token, String title, boolean pinned) throws Exception {
        String body = """
                {"title":"%s","content":"공지 내용입니다.","contentHtml":"<p><b>공지</b><script>x</script></p>","pinned":%s}
                """.formatted(title, pinned);
        String json = mockMvc.perform(auth(post(ADMIN_API), token)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(json, "$.data.id")).longValue();
    }

    @Test
    void 관리자가_등록하면_회원이_목록과_상세를_볼_수_있고_조회수가_오른다() throws Exception {
        String admin = tokenOf(Role.ADMIN, AdminType.VEHICLE_MANAGER);
        String user = tokenOf(Role.USER, null);
        long id = createNotice(admin, tag + " 운행 안내", false);

        mockMvc.perform(auth(get(MEMBER_API).param("keyword", tag), user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].title").value(tag + " 운행 안내"))
                .andExpect(jsonPath("$.data.content[0].writerName").value("공지작성자"));

        mockMvc.perform(auth(get(MEMBER_API + "/" + id), user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.viewCount").value(1))
                .andExpect(jsonPath("$.data.contentHtml").value("<p><b>공지</b></p>"));
        mockMvc.perform(auth(get(MEMBER_API + "/" + id), user))
                .andExpect(jsonPath("$.data.viewCount").value(2));

        // 관리자 화면에서 열어 보는 것은 조회수에 넣지 않는다
        mockMvc.perform(auth(get(ADMIN_API + "/" + id), admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.viewCount").value(2));
    }

    @Test
    void 상단_고정_공지가_목록_맨_위에_나온다() throws Exception {
        String admin = tokenOf(Role.ADMIN, AdminType.SYSTEM_ADMIN);
        createNotice(admin, tag + " 고정", true);
        createNotice(admin, tag + " 일반(나중에 등록)", false);

        mockMvc.perform(auth(get(MEMBER_API).param("keyword", tag), tokenOf(Role.USER, null)))
                .andExpect(jsonPath("$.data.content[0].title").value(tag + " 고정"))
                .andExpect(jsonPath("$.data.content[0].pinned").value(true))
                .andExpect(jsonPath("$.data.content[1].pinned").value(false));
    }

    @Test
    void 수정은_보낸_항목만_바뀌고_삭제하면_사라진다() throws Exception {
        String admin = tokenOf(Role.ADMIN, AdminType.COMPLAINT_MANAGER);
        long id = createNotice(admin, tag + " 원래 제목", false);

        mockMvc.perform(auth(patch(ADMIN_API + "/" + id), admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"" + tag + " 바뀐 제목\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value(tag + " 바뀐 제목"))
                .andExpect(jsonPath("$.data.content").value("공지 내용입니다."))
                .andExpect(jsonPath("$.data.updatedAt").isNotEmpty());

        mockMvc.perform(auth(delete(ADMIN_API + "/" + id), admin)).andExpect(status().isOk());
        mockMvc.perform(auth(get(MEMBER_API + "/" + id), tokenOf(Role.USER, null)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void 일반회원은_공지를_작성_수정_삭제할_수_없다() throws Exception {
        long id = createNotice(tokenOf(Role.ADMIN, AdminType.SYSTEM_ADMIN), tag, false);
        String user = tokenOf(Role.USER, null);

        assertThat(mockMvc.perform(auth(post(ADMIN_API), user).contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"t\",\"content\":\"c\"}")).andReturn().getResponse().getStatus()).isEqualTo(403);
        assertThat(mockMvc.perform(auth(patch(ADMIN_API + "/" + id), user).contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"t\"}")).andReturn().getResponse().getStatus()).isEqualTo(403);
        assertThat(mockMvc.perform(auth(delete(ADMIN_API + "/" + id), user))
                .andReturn().getResponse().getStatus()).isEqualTo(403);
        // 회원용 주소에는 등록·수정·삭제가 아예 없다
        assertThat(mockMvc.perform(auth(post(MEMBER_API), user).contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"t\",\"content\":\"c\"}")).andReturn().getResponse().getStatus()).isEqualTo(405);
        assertThat(mockMvc.perform(auth(delete(MEMBER_API + "/" + id), user))
                .andReturn().getResponse().getStatus()).isEqualTo(405);
    }

    @Test
    void 로그인하지_않으면_공지를_볼_수_없다() throws Exception {
        mockMvc.perform(get(MEMBER_API)).andExpect(status().isUnauthorized());
    }

    @Test
    void 제목이나_내용이_비어_있으면_등록할_수_없다() throws Exception {
        mockMvc.perform(auth(post(ADMIN_API), tokenOf(Role.ADMIN, AdminType.SYSTEM_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"title\":\" \",\"content\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT"));
    }
}
