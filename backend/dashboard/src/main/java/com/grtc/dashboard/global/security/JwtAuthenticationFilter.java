package com.grtc.dashboard.global.security;

import com.grtc.dashboard.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// 요청 헤더의 "Authorization: Bearer {accessToken}" 을 읽어 로그인 상태를 만들어 주는 필터 (명세서 3-1)
//   - 토큰이 정상이면 회원 ID 를 principal 로 넣는다. -> 컨트롤러에서 @AuthenticationPrincipal Long userId 로 받는다.
//   - 토큰이 없거나 잘못됐으면 로그인하지 않은 상태로 그대로 넘긴다. (로그인이 필요한 주소면 SecurityConfig 가 401 응답)
//   - 만료된 토큰이면 401 TOKEN_EXPIRED 가 내려가도록 표시해 둔다. (프론트는 /auth/reissue 호출)
// ※ @Component 로 등록하지 않는다. (서블릿 필터로 한 번 더 등록되어 시큐리티 필터 체인 밖에서 실행되는 것 방지)
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ERROR_ATTRIBUTE = JwtAuthenticationFilter.class.getName() + ".error";

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            String token = header.substring(BEARER_PREFIX.length()).trim();
            try {
                Claims claims = jwtProvider.parse(token);
                Long memberId = Long.valueOf(claims.getSubject());
                String role = claims.get(JwtProvider.CLAIM_ROLE, String.class);

                Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
                        memberId, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);
            } catch (ExpiredJwtException e) {
                request.setAttribute(ERROR_ATTRIBUTE, ErrorCode.TOKEN_EXPIRED);
            } catch (JwtException | IllegalArgumentException e) {
                request.setAttribute(ERROR_ATTRIBUTE, ErrorCode.LOGIN_REQUIRED);
            }
        }
        chain.doFilter(request, response);
    }

    // 401 응답을 만들 때 쓸 에러 코드 (토큰 만료면 TOKEN_EXPIRED, 그 외에는 UNAUTHORIZED)
    public static ErrorCode errorOf(HttpServletRequest request) {
        return request.getAttribute(ERROR_ATTRIBUTE) instanceof ErrorCode errorCode
                ? errorCode
                : ErrorCode.LOGIN_REQUIRED;
    }
}
