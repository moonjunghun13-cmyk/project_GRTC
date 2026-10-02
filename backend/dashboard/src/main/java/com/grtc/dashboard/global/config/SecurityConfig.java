package com.grtc.dashboard.global.config;

import com.grtc.dashboard.global.exception.ErrorCode;
import com.grtc.dashboard.global.security.AdminAccessFilter;
import com.grtc.dashboard.global.security.AdminMemberService;
import com.grtc.dashboard.global.security.JsonErrorWriter;
import com.grtc.dashboard.global.security.JwtAuthenticationFilter;
import com.grtc.dashboard.global.security.JwtProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

// 관리자 서버 보안 설정 - JWT 방식 (명세서 3-1)
//  - 로그인은 main 서버(/api/v1/auth/login)에서 하고, 거기서 받은 Access 토큰을
//    "Authorization: Bearer {accessToken}" 헤더에 넣어 이 서버를 호출한다. (두 서버의 app.jwt.secret 이 같아야 함)
//  - 이 서버의 API 는 전부 /api/v1/admin/** 이고 관리자(ADMIN)만 호출할 수 있다.
//      로그인 안 함 -> 401 UNAUTHORIZED,  토큰 만료 -> 401 TOKEN_EXPIRED,
//      일반 사용자 -> 403 FORBIDDEN ("관리자 페이지이므로 열람이 불가합니다.")
//  - /api/admin/** 은 아직 예전 주소를 쓰는 민원관리(관리자) API 용으로 같이 열어 둔다.
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtProvider jwtProvider,
                                                   AdminMemberService adminMemberService) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                // 로그인 화면/폼 로그인/HTTP Basic 은 쓰지 않는다 (main 서버가 로그인 담당)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                // 토큰으로 인증하므로 세션을 만들지 않는다.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/v1/admin/**", "/api/admin/**").hasRole("ADMIN")
                        .anyRequest().denyAll()
                )
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((request, response, ex) ->
                                JsonErrorWriter.write(response, JwtAuthenticationFilter.errorOf(request)))
                        .accessDeniedHandler((request, response, ex) ->
                                JsonErrorWriter.write(response, ErrorCode.ADMIN_ONLY))
                )
                // 토큰을 읽어 로그인 상태를 만든다.
                .addFilterBefore(new JwtAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class)
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
        config.setAllowedHeaders(List.of("*"));                    // Authorization 헤더 포함
        config.setExposedHeaders(List.of("Content-Disposition"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
