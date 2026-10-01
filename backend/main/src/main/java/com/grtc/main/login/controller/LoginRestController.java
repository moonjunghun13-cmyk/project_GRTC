package com.grtc.main.login.controller;

import com.grtc.main.login.dto.LoginRequestDto;
import com.grtc.main.login.dto.LoginResponseDto;
import com.grtc.main.login.dto.SignUpRequestDto;
import com.grtc.main.login.dto.SignUpResponseDto;
import com.grtc.main.login.service.LoginService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// 로그인/회원가입 API 요청을 처리하는 REST 컨트롤러
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class LoginRestController {

    // 아이디 중복 확인 응답
    public record CheckIdResponse(boolean available) {
    }

    // 회원 비즈니스 로직을 처리하는 서비스
    private final LoginService loginService;
    // 인증 정보를 세션에 저장하는 저장소
    private final SecurityContextRepository securityContextRepository;

    // 아이디/비밀번호로 로그인하고 세션에 인증 정보를 저장하는 API
    // 응답의 redirectPath: 관리자 -> /dashboard, 일반 사용자 -> /complaints
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(
            @Valid
            @RequestBody
            LoginRequestDto request,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse
    )
    {
        // 서비스에서 아이디/비밀번호를 검증하고 회원 정보를 받아옴
        LoginResponseDto user = loginService.login(request);

        // 세션이 없으면 새로 생성
        httpServletRequest.getSession(true);
        // 세션 고정 공격 방지를 위해 세션 ID 재발급
        httpServletRequest.changeSessionId();

        // 회원 권한을 ROLE_ 접두사가 붙은 스프링 시큐리티 권한 목록으로 변환
        List<GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        // 회원 ID와 권한으로 인증 완료된 인증 객체 생성
        Authentication authentication =
                UsernamePasswordAuthenticationToken.authenticated(user.getId(), null, authorities);
        // 빈 시큐리티 컨텍스트 생성
        SecurityContext context =
                SecurityContextHolder.createEmptyContext();
        // 컨텍스트에 인증 객체 저장
        context.setAuthentication(authentication);
        // 컨텍스트를 세션에 저장해 로그인 상태 유지
        securityContextRepository.saveContext(context, httpServletRequest, httpServletResponse);

        // 200 OK와 회원 정보 반환
        return ResponseEntity.ok(user);
    }
   // 회원가입 요청을 받아 계정을 생성하고 201 응답을 반환하는 API
   @PostMapping("/signup")
    public ResponseEntity<SignUpResponseDto> signup(
            @Valid
            @RequestBody
            SignUpRequestDto request
   )
   {
       // 서비스에서 회원가입 처리 후 가입된 회원 정보를 받아옴
       SignUpResponseDto response = loginService.signUp(request);

       // 201 Created와 가입된 회원 정보 반환
       return ResponseEntity.status(HttpStatus.CREATED).body(response);
   }

    // 회원가입 화면에서 아이디 사용 가능 여부를 확인하는 API
    @GetMapping("/check-id")
    public ResponseEntity<CheckIdResponse> checkId(@RequestParam String loginId) {
        return ResponseEntity.ok(new CheckIdResponse(loginService.isLoginIdAvailable(loginId)));
    }

    // 로그인한 회원 본인의 정보를 조회하는 API (사이드바 하단 이름/아이디, 권한별 메뉴 표시용)
    @GetMapping("/me")
    public ResponseEntity<LoginResponseDto> me(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(loginService.me(userId));
    }

    // 로그아웃: 세션을 없애고 인증 정보를 비우는 API
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest httpServletRequest) {
        HttpSession session = httpServletRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }
}
