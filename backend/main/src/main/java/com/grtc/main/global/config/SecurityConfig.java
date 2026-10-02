package com.grtc.main.global.config;

import com.grtc.main.global.exception.ErrorCode;
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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

// main 서버(공통 + 일반 사용자) 보안 설정 - JWT 방식 (명세서 3-1)
//  - 로그인하면 Access 토큰(30분)을 응답 본문으로, Refresh 토큰(14일)을 HttpOnly 쿠키로 내려준다.
//  - 이후 요청은 "Authorization: Bearer {accessToken}" 헤더로 인증한다. 서버는 세션을 만들지 않는다.
//  - 같은 토큰으로 dashboard(관리자) 서버도 호출할 수 있다. (두 서버의 app.jwt.secret 이 같아야 함)
@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtProvider jwtProvider) throws Exception{
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
                        // 프로필 이미지는 <img src> 로 불러오므로 토큰 없이 조회 가능 (파일명은 추측할 수 없는 임의 값)
                        .requestMatchers(HttpMethod.GET, "/api/v1/files/profile/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        // 관리자 전용 API 는 dashboard 서버에 있지만, 혹시 이 서버에 추가되더라도 막아 둔다.
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
                .addFilterBefore(new JwtAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class);
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
