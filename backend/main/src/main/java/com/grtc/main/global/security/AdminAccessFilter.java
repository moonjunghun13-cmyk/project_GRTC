package com.grtc.main.global.security;

import com.grtc.main.global.exception.BusinessException;
import com.grtc.main.global.exception.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class AdminAccessFilter extends OncePerRequestFilter {

    private final AdminMemberService adminMemberService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return !(uri.startsWith("/api/v1/admin/") || uri.startsWith("/api/admin/"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long memberId) {
            try {
                // 관리자인지 + 이 관리자 유형이 볼 수 있는 페이지의 API 인지
                adminMemberService.getActiveAdminFor(memberId, request.getRequestURI());
            } catch (BusinessException e) {
                ErrorCode code = e.getErrorCode();
                // 회원이 없어진 경우도 '열람 불가'로 통일 (403)
                ErrorCode shown = code == ErrorCode.USER_NOT_FOUND ? ErrorCode.ADMIN_ONLY : code;
                JsonErrorWriter.write(response, shown);
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
