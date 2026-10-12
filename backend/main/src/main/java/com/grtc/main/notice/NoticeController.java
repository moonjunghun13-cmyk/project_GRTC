package com.grtc.main.notice;

import com.grtc.main.global.common.ApiResponse;
import com.grtc.main.global.common.PageResponse;
import com.grtc.main.global.common.Pages;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// 공지사항 조회 API - 로그인한 회원이면 누구나 (작성·수정·삭제는 AdminNoticeController)
@RestController
@RequestMapping("/api/v1/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping
    public ApiResponse<PageResponse<NoticeDto.ListItem>> list(
            @AuthenticationPrincipal Long memberId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort
    ) {
        return ApiResponse.ok(noticeService.listForMember(memberId, keyword,
                Pages.request(page, size, sort, NoticeService.DEFAULT_SORT, NoticeService.SORTABLE)));
    }

    @GetMapping("/{noticeId}")
    public ApiResponse<NoticeDto.Detail> get(@AuthenticationPrincipal Long memberId, @PathVariable Long noticeId) {
        return ApiResponse.ok(noticeService.getForMember(memberId, noticeId));
    }
}
