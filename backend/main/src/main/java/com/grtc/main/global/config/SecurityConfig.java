package com.grtc.main.global.config;

import com.grtc.main.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.List;

@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
                .csrf(AbstractHttpConfigurer::disable)
                // Vue(다른 포트)에서 세션 쿠키를 들고 호출할 수 있도록 CORS 허용
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        // 로그인 전에 호출하는 API만 공개 (/api/auth/me 는 로그인 필요)
                        .requestMatchers("/api/auth/login", "/api/auth/signup",
                                "/api/auth/check-id", "/api/auth/logout").permitAll()
                        .requestMatchers("/error").permitAll()
                        // 관리자 전용 API 는 dashboard 서버에 있지만, 혹시 이 서버에 추가되더라도 막아 둔다.
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(SecurityConfig::writeLoginRequired)
                        // 일반 사용자가 관리자 API 를 호출하면 403 + JSON (프론트에서 '열람불가' 화면 표시)
                        .accessDeniedHandler(SecurityConfig::writeAdminOnly)
                );
        return http.build();
    }
    @Bean
    public SecurityContextRepository securityContextRepository(){
        return new HttpSessionSecurityContextRepository();
    }

    // 허용할 프론트엔드 주소는 application.yaml 의 app.cors.allowed-origins 로 관리
    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins:http://localhost:5173}") List<String> allowedOrigins) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Content-Disposition")); // 첨부파일 다운로드 파일명 읽기용
        config.setAllowCredentials(true);                          // 세션 쿠키 전달 허용

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    // 로그인하지 않은 요청(401)도 ErrorResponse 와 같은 JSON 형태로 내려준다.
    private static void writeLoginRequired(HttpServletRequest request,
                                           HttpServletResponse response,
                                           AuthenticationException e) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":\"" + ErrorCode.LOGIN_REQUIRED.getCode()
                + "\",\"message\":\"" + ErrorCode.LOGIN_REQUIRED.getMessage() + "\"}");
    }

    // 권한 부족(403) 응답을 ErrorResponse 와 같은 JSON 형태로 내려준다.
    private static void writeAdminOnly(HttpServletRequest request,
                                       HttpServletResponse response,
                                       AccessDeniedException e) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":\"" + ErrorCode.ADMIN_ONLY.getCode()
                + "\",\"message\":\"" + ErrorCode.ADMIN_ONLY.getMessage() + "\"}");
    }
}
