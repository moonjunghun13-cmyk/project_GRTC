package com.grtc.main.admin.notice;

import com.grtc.main.global.common.ApiResponse;
import com.grtc.main.global.common.PageResponse;
import com.grtc.main.global.common.Pages;
import com.grtc.main.notice.NoticeDto;
import com.grtc.main.notice.NoticeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// 공지관리 (관리자) - 등록·수정·삭제는 여기서만 할 수 있다
@RestController
@RequestMapping("/api/v1/admin/notices")
@RequiredArgsConstructor
public class AdminNoticeController {

    private final NoticeService noticeService;

    @GetMapping
    public ApiResponse<PageResponse<NoticeDto.ListItem>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort
    ) {
        return ApiResponse.ok(noticeService.list(keyword,
                Pages.request(page, size, sort, NoticeService.DEFAULT_SORT, NoticeService.SORTABLE)));
    }

    @GetMapping("/{noticeId}")
    public ApiResponse<NoticeDto.Detail> get(@PathVariable Long noticeId) {
        return ApiResponse.ok(noticeService.getForAdmin(noticeId));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<NoticeDto.Detail>> create(@AuthenticationPrincipal Long adminId,
                                                                @Valid @RequestBody NoticeDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(noticeService.create(adminId, request)));
    }

    @PatchMapping("/{noticeId}")
    public ApiResponse<NoticeDto.Detail> update(@PathVariable Long noticeId,
                                                @Valid @RequestBody NoticeDto.UpdateRequest request) {
        return ApiResponse.ok(noticeService.update(noticeId, request));
    }

    @DeleteMapping("/{noticeId}")
    public ApiResponse<Void> delete(@PathVariable Long noticeId) {
        noticeService.delete(noticeId);
        return ApiResponse.ok();
    }
}
