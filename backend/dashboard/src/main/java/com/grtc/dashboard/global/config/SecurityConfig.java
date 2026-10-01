package com.grtc.dashboard.global.config;

import com.grtc.dashboard.global.exception.ErrorCode;
import com.grtc.dashboard.global.security.AdminAccessFilter;
import com.grtc.dashboard.global.security.AdminMemberService;
import com.grtc.dashboard.global.security.JsonErrorWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

// 관리자 서버 보안 설정
//  - 로그인은 main 서버(/api/auth/login)에서 하고, 로그인 세션은 DB(Spring Session JDBC)에 저장된다.
//    이 서버도 같은 DB 의 세션을 읽기 때문에 따로 로그인하지 않아도 같은 SESSION 쿠키로 인증된다.
//  - 이 서버의 API 는 전부 /api/admin/** 이고 ROLE_ADMIN 만 호출할 수 있다.
//      로그인 안 함 -> 401,  일반 사용자 -> 403 {"code":"A001","message":"관리자 페이지이므로 열람이 불가합니다."}
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AdminMemberService adminMemberService) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                // 로그인 화면/폼 로그인/HTTP Basic 은 쓰지 않는다 (main 서버가 로그인 담당)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().denyAll()
                )
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) ->
                                JsonErrorWriter.write(res, HttpStatus.UNAUTHORIZED.value(), ErrorCode.LOGIN_REQUIRED))
                        .accessDeniedHandler((req, res, ex) ->
                                JsonErrorWriter.write(res, HttpStatus.FORBIDDEN.value(), ErrorCode.ADMIN_ONLY))
                )
                // 권한 검사를 통과한 요청에 대해 DB 최신 상태 확인
                .addFilterAfter(new AdminAccessFilter(adminMemberService), AuthorizationFilter.class);
        return http.build();
    }

    // 허용할 프론트엔드 주소는 application.yaml 의 app.cors.allowed-origins 로 관리
    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins:http://localhost:5173}") List<String> allowedOrigins) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Content-Disposition"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
