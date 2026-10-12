package com.grtc.main.member;

import com.grtc.main.global.common.ApiResponse;
import com.grtc.main.global.security.RefreshCookieFactory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final RefreshCookieFactory refreshCookieFactory;

    @GetMapping("/me")
    public ApiResponse<MemberDto.Detail> me(@AuthenticationPrincipal Long userId) {
        return ApiResponse.ok(memberService.get(userId));
    }

    @PatchMapping("/me")
    public ApiResponse<MemberDto.Detail> updateMe(@AuthenticationPrincipal Long userId,
                                                  @Valid @RequestBody MemberDto.UpdateRequest request) {
        return ApiResponse.ok(memberService.update(userId, request));
    }

    @PutMapping(value = "/me/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<MemberDto.ProfileImage> changeProfileImage(
            @AuthenticationPrincipal Long userId,
            @RequestPart("file") MultipartFile file) {
        return ApiResponse.ok(memberService.changeProfileImage(userId, file));
    }

    @PatchMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@AuthenticationPrincipal Long userId,
                                                            @Valid @RequestBody MemberDto.PasswordRequest request) {
        memberService.changePassword(userId, request);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookieFactory.expire().toString())
                .body(ApiResponse.ok());
    }

    @GetMapping("/options")
    public ApiResponse<MemberDto.Options> options() {
        return ApiResponse.ok(memberService.options());
    }
}
