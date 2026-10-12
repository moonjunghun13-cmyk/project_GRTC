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

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminMemberController {

    private final AdminMemberManageService memberService;

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

    @GetMapping("/members/options")
    public ApiResponse<AdminMemberDto.Options> options() {
        return ApiResponse.ok(memberService.options());
    }

    @GetMapping("/members/{memberId}")
    public ApiResponse<AdminMemberDto.Detail> get(@PathVariable Long memberId) {
        return ApiResponse.ok(memberService.get(memberId));
    }

    @PatchMapping("/members/{memberId}")
    public ApiResponse<AdminMemberDto.Detail> update(@PathVariable Long memberId,
                                                @Valid @RequestBody AdminMemberDto.UpdateRequest request) {
        return ApiResponse.ok(memberService.update(memberId, request));
    }

    @PatchMapping("/members/{memberId}/role")
    public ApiResponse<AdminMemberDto.Detail> changeRole(@AuthenticationPrincipal Long adminId,
                                                    @PathVariable Long memberId,
                                                    @Valid @RequestBody AdminMemberDto.RoleRequest request) {
        return ApiResponse.ok(memberService.changeRole(adminId, memberId, request.role()));
    }

    @PatchMapping("/members/{memberId}/status")
    public ApiResponse<AdminMemberDto.Detail> changeStatus(@AuthenticationPrincipal Long adminId,
                                                      @PathVariable Long memberId,
                                                      @Valid @RequestBody AdminMemberDto.StatusRequest request) {
        return ApiResponse.ok(memberService.changeStatus(adminId, memberId, request.status()));
    }

    @GetMapping("/me")
    public ApiResponse<AdminMemberDto.Detail> me(@AuthenticationPrincipal Long adminId) {
        return ApiResponse.ok(memberService.get(adminId));
    }

    @PatchMapping("/me")
    public ApiResponse<AdminMemberDto.Detail> updateMe(@AuthenticationPrincipal Long adminId,
                                                  @Valid @RequestBody AdminMemberDto.UpdateRequest request) {
        return ApiResponse.ok(memberService.update(adminId, request));
    }
}
