package com.grtc.main.admin.member;

import com.grtc.main.global.common.ApiResponse;
import com.grtc.main.global.common.PageResponse;
import com.grtc.main.global.common.Pages;
import com.grtc.main.login.entity.MemberStatus;
import com.grtc.main.login.entity.Role;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// 회원관리 / 회원정보(관리자) API (명세서 3-3 ② 회원 중 관리자용) - 권한: 관리자
//   Base URL: /api/v1
//   /admin/members/**   : 회원관리 목록, 회원 상세, 수정, 상태·권한 변경
//   /admin/me           : 사이드바 하단(관리자 / admin) 표시, 톱니바퀴 -> 내 회원정보 수정
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminMemberController {

    private final AdminMemberManageService memberService;

    // 회원 목록 (검색어: 이름/아이디/이메일, 권한·상태 필터, 페이징: page 0부터)
    //   예) GET /api/v1/admin/members?keyword=kim&role=USER&status=NORMAL&page=0&size=10&sort=createdAt,desc
    @GetMapping("/members")
    public ApiResponse<PageResponse<AdminMemberDto.ListItem>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) MemberStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort
    ) {
        return ApiResponse.ok(memberService.list(keyword, role, status,
                Pages.request(page, size, sort, AdminMemberManageService.DEFAULT_SORT, AdminMemberManageService.SORTABLE)));
    }

    // 소속 부서 / 직급 선택 목록
    @GetMapping("/members/options")
    public ApiResponse<AdminMemberDto.Options> options() {
        return ApiResponse.ok(memberService.options());
    }

    // 회원 상세(회원정보 화면)
    @GetMapping("/members/{memberId}")
    public ApiResponse<AdminMemberDto.Detail> get(@PathVariable Long memberId) {
        return ApiResponse.ok(memberService.get(memberId));
    }

    // 회원정보 수정(저장하기) - 보낸 항목만 변경
    @PatchMapping("/members/{memberId}")
    public ApiResponse<AdminMemberDto.Detail> update(@PathVariable Long memberId,
                                                @Valid @RequestBody AdminMemberDto.UpdateRequest request) {
        return ApiResponse.ok(memberService.update(memberId, request));
    }

    // 역할 변경 (관리자 / 일반회원)
    @PatchMapping("/members/{memberId}/role")
    public ApiResponse<AdminMemberDto.Detail> changeRole(@AuthenticationPrincipal Long adminId,
                                                    @PathVariable Long memberId,
                                                    @Valid @RequestBody AdminMemberDto.RoleRequest request) {
        return ApiResponse.ok(memberService.changeRole(adminId, memberId, request.role()));
    }

    // 계정 상태 변경 (정상 / 이용정지 / 탈퇴)
    @PatchMapping("/members/{memberId}/status")
    public ApiResponse<AdminMemberDto.Detail> changeStatus(@AuthenticationPrincipal Long adminId,
                                                      @PathVariable Long memberId,
                                                      @Valid @RequestBody AdminMemberDto.StatusRequest request) {
        return ApiResponse.ok(memberService.changeStatus(adminId, memberId, request.status()));
    }

    // 로그인한 관리자 본인 정보 (사이드바 하단 이름/아이디, 회원정보 화면)
    @GetMapping("/me")
    public ApiResponse<AdminMemberDto.Detail> me(@AuthenticationPrincipal Long adminId) {
        return ApiResponse.ok(memberService.get(adminId));
    }

    // 관리자 본인 정보 수정 - 보낸 항목만 변경
    @PatchMapping("/me")
    public ApiResponse<AdminMemberDto.Detail> updateMe(@AuthenticationPrincipal Long adminId,
                                                  @Valid @RequestBody AdminMemberDto.UpdateRequest request) {
        return ApiResponse.ok(memberService.update(adminId, request));
    }
}
