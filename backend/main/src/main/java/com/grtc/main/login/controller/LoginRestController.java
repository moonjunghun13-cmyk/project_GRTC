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

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class LoginRestController {

    public record CheckIdResponse(boolean available) {
    }

    private final LoginService loginService;
    private final TokenService tokenService;
    private final RefreshCookieFactory refreshCookieFactory;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignUpResponseDto>> signup(@Valid @RequestBody SignUpRequestDto request) {
        SignUpResponseDto response = loginService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/check-id")
    public ApiResponse<CheckIdResponse> checkId(@RequestParam String loginId) {
        return ApiResponse.ok(new CheckIdResponse(loginService.isLoginIdAvailable(loginId)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResult>> login(@Valid @RequestBody LoginRequestDto request) {
        LoginResponseDto member = loginService.login(request);

        String accessToken = tokenService.createAccessToken(member);
        String refreshToken = tokenService.issueRefreshToken(member.getId());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookieFactory.create(refreshToken).toString())
                .body(ApiResponse.ok(LoginResult.bearer(accessToken, tokenService.accessExpiresIn(), member)));
    }

    @PostMapping("/reissue")
    public ApiResponse<TokenResponse> reissue(
            @CookieValue(name = RefreshCookieFactory.COOKIE_NAME, required = false) String refreshToken) {
        return ApiResponse.ok(tokenService.reissue(refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @CookieValue(name = RefreshCookieFactory.COOKIE_NAME, required = false) String refreshToken) {
        tokenService.revoke(refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookieFactory.expire().toString())
                .body(ApiResponse.ok());
    }

    @GetMapping("/me")
    public ApiResponse<LoginResponseDto> me(@AuthenticationPrincipal Long userId) {
        return ApiResponse.ok(loginService.me(userId));
    }
}
