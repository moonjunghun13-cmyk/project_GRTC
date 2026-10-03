package com.grtc.main.login.controller;

import com.grtc.main.global.common.ApiResponse;
import com.grtc.main.global.security.RefreshCookieFactory;
import com.grtc.main.login.dto.LoginRequestDto;
import com.grtc.main.login.dto.LoginResponseDto;
import com.grtc.main.login.dto.LoginResult;
import com.grtc.main.login.dto.SignUpRequestDto;
import com.grtc.main.login.dto.SignUpResponseDto;
import com.grtc.main.login.dto.TokenResponse;
import com.grtc.main.login.service.LoginService;
import com.grtc.main.login.service.TokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// 인증 API (명세서 3-3 ① 인증) - 회원가입 / 로그인 / 토큰 재발급 / 로그아웃
//   Base URL: /api/v1
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class LoginRestController {

    // 아이디 중복 확인 응답
    public record CheckIdResponse(boolean available) {
    }

    // 회원 비즈니스 로직을 처리하는 서비스
    private final LoginService loginService;
    // Access / Refresh 토큰 발급·재발급·폐기
    private final TokenService tokenService;
    // Refresh 토큰 쿠키 생성
    private final RefreshCookieFactory refreshCookieFactory;

    // 회원가입 (권한: 전체) - 계정을 만들고 201 응답
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignUpResponseDto>> signup(@Valid @RequestBody SignUpRequestDto request) {
        SignUpResponseDto response = loginService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    // 회원가입 화면의 아이디 사용 가능 여부 확인 (권한: 전체)
    @GetMapping("/check-id")
    public ApiResponse<CheckIdResponse> checkId(@RequestParam String loginId) {
        return ApiResponse.ok(new CheckIdResponse(loginService.isLoginIdAvailable(loginId)));
    }

    // 로그인, 토큰 발급 (권한: 전체)
    //   - Access 토큰: 응답 본문의 data.accessToken
    //   - Refresh 토큰: HttpOnly 쿠키(refreshToken)
    //   - data.member.redirectPath: 관리자 -> /dashboard, 일반 사용자 -> /complaints
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResult>> login(@Valid @RequestBody LoginRequestDto request) {
        // 서비스에서 아이디/비밀번호/계정 상태를 검증하고 회원 정보를 받아옴
        LoginResponseDto member = loginService.login(request);

        String accessToken = tokenService.createAccessToken(member);
        String refreshToken = tokenService.issueRefreshToken(member.getId());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookieFactory.create(refreshToken).toString())
                .body(ApiResponse.ok(LoginResult.bearer(accessToken, tokenService.accessExpiresIn(), member)));
    }

    // Access 토큰 재발급 (권한: 전체) - 쿠키의 Refresh 토큰 사용
    //   Access 토큰이 만료되어 401 TOKEN_EXPIRED 를 받았을 때 호출한다.
    @PostMapping("/reissue")
    public ApiResponse<TokenResponse> reissue(
            @CookieValue(name = RefreshCookieFactory.COOKIE_NAME, required = false) String refreshToken) {
        return ApiResponse.ok(tokenService.reissue(refreshToken));
    }

    // 로그아웃 (권한: 회원) - Refresh 토큰 기록을 지우고 쿠키도 삭제
    //   Access 토큰은 프론트에서 버린다. (서버에 저장하지 않으므로 만료될 때까지는 형식상 유효)
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @CookieValue(name = RefreshCookieFactory.COOKIE_NAME, required = false) String refreshToken) {
        tokenService.revoke(refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookieFactory.expire().toString())
                .body(ApiResponse.ok());
    }

    // 로그인한 회원 본인의 요약 정보 (권한: 회원) - 사이드바 하단 이름/아이디, 권한별 메뉴 표시, 새로고침 후 로그인 확인용
    @GetMapping("/me")
    public ApiResponse<LoginResponseDto> me(@AuthenticationPrincipal Long userId) {
        return ApiResponse.ok(loginService.me(userId));
    }
}
