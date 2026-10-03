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

// 회원 API (명세서 3-3 ② 회원) - 로그인한 회원 본인용 (사이드바 톱니바퀴 -> 회원정보)
//   Base URL: /api/v1, 권한: 회원(로그인한 모든 사용자)
//   ※ 관리자의 회원관리(목록/상태·권한 변경)는 dashboard 서버의 /api/v1/admin/members 에 있다.
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final RefreshCookieFactory refreshCookieFactory;

    // 내 정보 조회
    @GetMapping("/me")
    public ApiResponse<MemberDto.Detail> me(@AuthenticationPrincipal Long userId) {
        return ApiResponse.ok(memberService.get(userId));
    }

    // 내 정보 수정 (보낸 항목만 변경)
    @PatchMapping("/me")
    public ApiResponse<MemberDto.Detail> updateMe(@AuthenticationPrincipal Long userId,
                                                  @Valid @RequestBody MemberDto.UpdateRequest request) {
        return ApiResponse.ok(memberService.update(userId, request));
    }

    // 프로필 이미지 변경 (multipart/form-data, 파트 이름: file) - 썸네일도 함께 만든다.
    @PutMapping(value = "/me/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<MemberDto.ProfileImage> changeProfileImage(
            @AuthenticationPrincipal Long userId,
            @RequestPart("file") MultipartFile file) {
        return ApiResponse.ok(memberService.changeProfileImage(userId, file));
    }

    // 비밀번호 변경 - 변경 후에는 Refresh 토큰이 모두 지워지므로 다시 로그인해야 한다.
    @PatchMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@AuthenticationPrincipal Long userId,
                                                            @Valid @RequestBody MemberDto.PasswordRequest request) {
        memberService.changePassword(userId, request);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookieFactory.expire().toString())
                .body(ApiResponse.ok());
    }

    // 소속 부서 / 직급 선택 목록
    @GetMapping("/options")
    public ApiResponse<MemberDto.Options> options() {
        return ApiResponse.ok(memberService.options());
    }
}
