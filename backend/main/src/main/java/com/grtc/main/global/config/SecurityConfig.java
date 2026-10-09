package com.grtc.main.global.config;

import com.grtc.main.global.exception.ErrorCode;
import com.grtc.main.global.security.AdminAccessFilter;
import com.grtc.main.global.security.AdminMemberService;
import com.grtc.main.global.security.JsonErrorWriter;
import com.grtc.main.global.security.JwtAuthenticationFilter;
import com.grtc.main.global.security.JwtProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

// 통합 서버 보안 설정 - JWT 방식 (명세서 3-1)
//  - 예전 main(공통·일반 사용자) + dashboard(관리자) 두 서버를 하나로 합쳤다. 포트 8081 하나만 쓴다.
//  - 로그인하면 Access 토큰(30분)을 응답 본문으로, Refresh 토큰(14일)을 HttpOnly 쿠키로 내려준다.
//  - 이후 요청은 "Authorization: Bearer {accessToken}" 헤더로 인증한다. 서버는 세션을 만들지 않는다.
//  - 관리자 API(/api/v1/admin/**, /api/admin/**)는 ADMIN 권한 + AdminAccessFilter(DB 최신 권한·상태 재확인)로 이중 확인한다.
@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtProvider jwtProvider,
                                                   AdminMemberService adminMemberService) throws Exception{
        http
                .csrf(AbstractHttpConfigurer::disable)
                // Vue(다른 포트)에서 호출할 수 있도록 CORS 허용
                .cors(Customizer.withDefaults())
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                // 토큰으로 인증하므로 세션을 만들지 않는다.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 권한 "전체": 로그인 전에 호출하는 API
                        .requestMatchers("/api/v1/auth/signup", "/api/v1/auth/login",
                                "/api/v1/auth/reissue", "/api/v1/auth/check-id").permitAll()
                        // 로그아웃은 Access 토큰이 만료된 뒤에도 되어야 한다. (쿠키의 Refresh 토큰만으로 처리)
                        //  - 여기서 401 로 막으면 Refresh 토큰이 지워지지 않아, 로그아웃한 뒤에도 재발급으로 다시 로그인된다.
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/logout").permitAll()
                        // 프로필 이미지는 <img src> 로 불러오므로 토큰 없이 조회 가능 (파일명은 추측할 수 없는 임의 값)
                        .requestMatchers(HttpMethod.GET, "/api/v1/files/profile/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        // 권한 "관리자": 관리자 화면 API (예전 dashboard 서버)
                        .requestMatchers("/api/v1/admin/**", "/api/admin/**").hasRole("ADMIN")
                        // 권한 "회원": 그 밖의 모든 API 는 로그인 필요
                        .anyRequest().authenticated()
                )
                .exceptionHandling(e -> e
                        // 로그인하지 않았거나 토큰이 잘못됨 -> 401 UNAUTHORIZED, 토큰 만료 -> 401 TOKEN_EXPIRED
                        .authenticationEntryPoint((request, response, ex) ->
                                JsonErrorWriter.write(response, JwtAuthenticationFilter.errorOf(request)))
                        // 일반 사용자가 관리자 API 를 호출 -> 403 FORBIDDEN (프론트에서 '열람불가' 화면 표시)
                        .accessDeniedHandler((request, response, ex) ->
                                JsonErrorWriter.write(response, ErrorCode.ADMIN_ONLY))
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class)
                // 권한 검사를 통과한 관리자 API 요청에 대해 DB 최신 권한·상태 확인 (정지·탈퇴·권한 변경 즉시 반영)
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
        config.setExposedHeaders(List.of("Content-Disposition")); // 첨부파일 다운로드 파일명 읽기용
        config.setAllowCredentials(true);                          // Refresh 토큰 쿠키 전달 허용

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
