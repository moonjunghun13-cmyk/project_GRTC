package com.grtc.main.login;

import com.grtc.main.login.entity.LoginEntity;
import com.grtc.main.login.entity.Role;
import com.grtc.main.login.repository.LoginRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 로그아웃 API 확인: Access 토큰이 없거나 못 쓰게 된 상태에서도 로그아웃이 서버에서 처리되어야 한다.
//  - 실제 보안 설정(SecurityConfig)을 그대로 거친다. 테스트가 끝나면 전부 롤백한다.
@SpringBootTest
@Transactional
class LogoutApiTest {

    private static final String PASSWORD = "Test1234!";

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private LoginRepository loginRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    // 로그인해서 Refresh 토큰 쿠키를 받아 온다.
    private Cookie loginAndGetRefreshCookie() throws Exception {
        String loginId = "lgo" + System.nanoTime() % 1_000_000_000L;
        loginRepository.save(LoginEntity.builder()
                .loginId(loginId).password(passwordEncoder.encode(PASSWORD)).name("로그아웃테스트").role(Role.USER).build());

        Cookie cookie = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"loginId\":\"" + loginId + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getCookie("refreshToken");
        assertThat(cookie).isNotNull();
        return cookie;
    }

    @Test
    void Access_토큰_없이도_로그아웃할_수_있다() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")).andExpect(status().isOk());
    }

    // 예전에는 여기서 로그아웃이 401 로 막혀 Refresh 토큰이 남았고, 로그아웃한 뒤에도 재발급이 됐다.
    @Test
    void Access_토큰을_못_쓰는_상태에서_로그아웃해도_재발급이_막힌다() throws Exception {
        Cookie refresh = loginAndGetRefreshCookie();
        mockMvc.perform(post("/api/v1/auth/reissue").cookie(refresh)).andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/logout").cookie(refresh)
                        .header("Authorization", "Bearer not-a-usable-token"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/reissue").cookie(refresh)).andExpect(status().isUnauthorized());
    }

    @Test
    void 다른_API는_여전히_로그인이_필요하다() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/complaints")).andExpect(status().isUnauthorized());
    }
}
