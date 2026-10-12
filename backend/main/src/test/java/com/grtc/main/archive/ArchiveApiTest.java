package com.grtc.main.archive;

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
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.AbstractMockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 파일은 실제 업로드 폴더(app.file.upload-dir/archive)에 써지지만, 테스트 트랜잭션이 롤백되면서 지워진다
@SpringBootTest
@Transactional
class ArchiveApiTest {

    private static final String ADMIN_API = "/api/v1/admin/archives";
    private static final String MEMBER_API = "/api/v1/archives";
    private static final byte[] PDF = "%PDF-1.4 테스트 자료".getBytes(StandardCharsets.UTF_8);

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
        tag = "자료테스트" + System.nanoTime() % 1_000_000_000L;
    }

    private String tokenOf(Role role, AdminType type) {
        LoginEntity member = loginRepository.save(LoginEntity.builder()
                .loginId("ar" + System.nanoTime() % 1_000_000_000L).password("x").name("자료등록자")
                .role(role).adminType(type).build());
        return tokenService.createAccessToken(LoginResponseDto.from(member));
    }

    private <T extends AbstractMockHttpServletRequestBuilder<?>> T auth(T request, String token) {
        request.header("Authorization", "Bearer " + token);
        return request;
    }

    private static MockMultipartFile file(String name, String type, byte[] content) {
        return new MockMultipartFile("files", name, type, content);
    }

    private MockMultipartHttpServletRequestBuilder createRequest(String token, MockMultipartFile... files) {
        MockMultipartHttpServletRequestBuilder request = multipart(ADMIN_API);
        for (MockMultipartFile f : files) {
            request.file(f);
        }
        request.param("title", tag + " 운행 규정").param("content", "규정 파일입니다.");
        return auth(request, token);
    }

    private String createArchive(String token, MockMultipartFile... files) throws Exception {
        return mockMvc.perform(createRequest(token, files))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
    }

    private static long idOf(String json, String path) {
        return ((Number) JsonPath.read(json, path)).longValue();
    }

    @Test
    void 관리자가_파일과_함께_등록하면_회원이_보고_내려받을_수_있다() throws Exception {
        String admin = tokenOf(Role.ADMIN, AdminType.DEPARTMENT_HEAD);
        String user = tokenOf(Role.USER, null);
        String created = createArchive(admin, file("운행 규정.pdf", "application/pdf", PDF));
        long id = idOf(created, "$.data.id");
        long fileId = idOf(created, "$.data.files[0].id");

        mockMvc.perform(auth(get(MEMBER_API).param("keyword", tag), user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].fileCount").value(1));

        mockMvc.perform(auth(get(MEMBER_API + "/" + id), user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.viewCount").value(1))
                .andExpect(jsonPath("$.data.files[0].originalFileName").value("운행 규정.pdf"))
                .andExpect(jsonPath("$.data.files[0].downloadUrl")
                        .value(MEMBER_API + "/" + id + "/files/" + fileId));

        MockHttpServletResponse download = mockMvc.perform(
                        auth(get(MEMBER_API + "/" + id + "/files/" + fileId), user))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertThat(download.getContentAsByteArray()).isEqualTo(PDF);
        assertThat(download.getHeader("Content-Disposition")).contains("attachment").contains("UTF-8''");
    }

    @Test
    void 민원과_같은_파일_규칙을_쓴다() throws Exception {
        String admin = tokenOf(Role.ADMIN, AdminType.SYSTEM_ADMIN);

        mockMvc.perform(createRequest(admin, file("setup.exe", "application/octet-stream", PDF)))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.error.code").value("UNSUPPORTED_FILE_TYPE"));

        MockMultipartFile[] six = new MockMultipartFile[6];
        for (int i = 0; i < six.length; i++) {
            six[i] = file("자료" + i + ".pdf", "application/pdf", PDF);
        }
        mockMvc.perform(createRequest(admin, six))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("FILE_TOO_MANY"));
    }

    @Test
    void 수정할_때_첨부파일을_지우고_새로_올릴_수_있다() throws Exception {
        String admin = tokenOf(Role.ADMIN, AdminType.VEHICLE_MANAGER);
        String created = createArchive(admin,
                file("a.pdf", "application/pdf", PDF), file("b.png", "image/png", PDF));
        long id = idOf(created, "$.data.id");
        long firstFileId = idOf(created, "$.data.files[0].id");

        mockMvc.perform(auth(multipart(HttpMethod.PUT, ADMIN_API + "/" + id)
                                .file(file("c.gif", "image/gif", PDF)), admin)
                        .param("title", tag + " 개정")
                        .param("deleteFileIds", String.valueOf(firstFileId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value(tag + " 개정"))
                .andExpect(jsonPath("$.data.files.length()").value(2))
                .andExpect(jsonPath("$.data.files[0].originalFileName").value("b.png"))
                .andExpect(jsonPath("$.data.files[1].originalFileName").value("c.gif"))
                .andExpect(jsonPath("$.data.files[1].downloadUrl").value(
                        org.hamcrest.Matchers.startsWith(ADMIN_API + "/" + id + "/files/")));
    }

    @Test
    void 삭제하면_글과_파일이_모두_사라진다() throws Exception {
        String admin = tokenOf(Role.ADMIN, AdminType.SYSTEM_ADMIN);
        String created = createArchive(admin, file("a.pdf", "application/pdf", PDF));
        long id = idOf(created, "$.data.id");
        long fileId = idOf(created, "$.data.files[0].id");

        mockMvc.perform(auth(delete(ADMIN_API + "/" + id), admin)).andExpect(status().isOk());

        String user = tokenOf(Role.USER, null);
        mockMvc.perform(auth(get(MEMBER_API + "/" + id), user)).andExpect(status().isNotFound());
        mockMvc.perform(auth(get(MEMBER_API + "/" + id + "/files/" + fileId), user))
                .andExpect(status().isNotFound());
    }

    @Test
    void 다른_글의_파일_번호로는_내려받을_수_없다() throws Exception {
        String admin = tokenOf(Role.ADMIN, AdminType.SYSTEM_ADMIN);
        long first = idOf(createArchive(admin, file("a.pdf", "application/pdf", PDF)), "$.data.id");
        long otherFileId = idOf(createArchive(admin, file("b.pdf", "application/pdf", PDF)), "$.data.files[0].id");

        mockMvc.perform(auth(get(MEMBER_API + "/" + first + "/files/" + otherFileId), tokenOf(Role.USER, null)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void 일반회원은_자료를_등록_수정_삭제할_수_없다() throws Exception {
        long id = idOf(createArchive(tokenOf(Role.ADMIN, AdminType.SYSTEM_ADMIN),
                file("a.pdf", "application/pdf", PDF)), "$.data.id");
        String user = tokenOf(Role.USER, null);

        assertThat(mockMvc.perform(createRequest(user, file("x.pdf", "application/pdf", PDF)))
                .andReturn().getResponse().getStatus()).isEqualTo(403);
        assertThat(mockMvc.perform(auth(multipart(HttpMethod.PUT, ADMIN_API + "/" + id), user).param("title", "x"))
                .andReturn().getResponse().getStatus()).isEqualTo(403);
        assertThat(mockMvc.perform(auth(delete(ADMIN_API + "/" + id), user))
                .andReturn().getResponse().getStatus()).isEqualTo(403);
        assertThat(mockMvc.perform(auth(multipart(MEMBER_API), user).param("title", "x"))
                .andReturn().getResponse().getStatus()).isEqualTo(405);
    }

    @Test
    void 로그인하지_않으면_자료실을_볼_수_없다() throws Exception {
        mockMvc.perform(get(MEMBER_API)).andExpect(status().isUnauthorized());
    }
}
