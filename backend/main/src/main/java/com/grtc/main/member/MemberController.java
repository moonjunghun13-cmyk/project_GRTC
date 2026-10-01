package com.grtc.main.member;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 내 정보 API (로그인한 회원 본인용, 사이드바 톱니바퀴 -> 회원정보)
//   ※ 관리자의 회원관리(목록/상태·권한 변경)는 dashboard 서버의 /api/admin/members 에 있다.
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // 내 정보 조회
    @GetMapping("/me")
    public MemberDto.Detail me(@AuthenticationPrincipal Long userId) {
        return memberService.get(userId);
    }

    // 내 정보 수정
    @PutMapping("/me")
    public MemberDto.Detail updateMe(@AuthenticationPrincipal Long userId,
                                     @Valid @RequestBody MemberDto.UpdateRequest request) {
        return memberService.update(userId, request);
    }

    // 소속 부서 / 직급 선택 목록
    @GetMapping("/options")
    public MemberDto.Options options() {
        return memberService.options();
    }
}
