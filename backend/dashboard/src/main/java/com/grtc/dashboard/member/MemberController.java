package com.grtc.dashboard.member;

import com.grtc.dashboard.global.common.PageResponse;
import com.grtc.dashboard.member.entity.MemberStatus;
import com.grtc.dashboard.member.entity.Role;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// 회원관리 / 회원정보(관리자) API - 모두 관리자 전용 (/api/admin/** 은 ROLE_ADMIN 만 허용)
//   /api/admin/members/**   : 회원관리 목록, 회원 상세, 수정, 상태·권한 변경
//   /api/admin/me           : 사이드바 하단(관리자 / admin) 표시, 톱니바퀴 -> 내 회원정보 수정
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // 회원관리 목록 (검색어: 이름/아이디/이메일, 권한, 상태 필터 / page 는 1부터)
    @GetMapping("/members")
    public PageResponse<MemberDto.ListItem> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) MemberStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return memberService.list(keyword, role, status, page, size);
    }

    // 회원 상세(회원정보 화면)
    @GetMapping("/members/{id}")
    public MemberDto.Detail get(@PathVariable Long id) {
        return memberService.get(id);
    }

    // 회원정보 수정(저장하기)
    @PutMapping("/members/{id}")
    public MemberDto.Detail update(@PathVariable Long id,
                                   @Valid @RequestBody MemberDto.UpdateRequest request) {
        return memberService.update(id, request);
    }

    // 회원 상태 변경 (정상 / 이용정지 / 탈퇴)
    @PatchMapping("/members/{id}/status")
    public MemberDto.Detail changeStatus(@AuthenticationPrincipal Long adminId,
                                         @PathVariable Long id,
                                         @Valid @RequestBody MemberDto.StatusRequest request) {
        return memberService.changeStatus(adminId, id, request.status());
    }

    // 회원 권한 변경 (관리자 / 일반회원)
    @PatchMapping("/members/{id}/role")
    public MemberDto.Detail changeRole(@AuthenticationPrincipal Long adminId,
                                       @PathVariable Long id,
                                       @Valid @RequestBody MemberDto.RoleRequest request) {
        return memberService.changeRole(adminId, id, request.role());
    }

    // 소속 부서 / 직급 선택 목록
    @GetMapping("/members/options")
    public MemberDto.Options options() {
        return memberService.options();
    }

    // 로그인한 관리자 본인 정보 (사이드바 하단 이름/아이디, 회원정보 화면)
    @GetMapping("/me")
    public MemberDto.Detail me(@AuthenticationPrincipal Long adminId) {
        return memberService.get(adminId);
    }

    // 관리자 본인 정보 수정
    @PutMapping("/me")
    public MemberDto.Detail updateMe(@AuthenticationPrincipal Long adminId,
                                     @Valid @RequestBody MemberDto.UpdateRequest request) {
        return memberService.update(adminId, request);
    }
}
