package com.grtc.dashboard.global.security;

import com.grtc.dashboard.global.exception.BusinessException;
import com.grtc.dashboard.global.exception.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// 관리자 API 요청마다 DB 의 최신 권한/상태를 한 번 더 확인하는 필터
//  - Access 토큰에는 발급 당시의 권한(ADMIN)이 들어 있어서,
//    그 뒤에 권한이 바뀌거나 정지/탈퇴된 계정도 토큰이 만료되기 전까지는 통과할 수 있기 때문
//  - 문제가 있으면 403 + 명세서 실패 응답 형식 JSON (프론트에서 '열람불가' 화면 표시)
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
                adminMemberService.getActiveAdmin(memberId);
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
