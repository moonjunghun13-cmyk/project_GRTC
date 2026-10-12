package com.grtc.main.archive;

import com.grtc.main.global.common.ApiResponse;
import com.grtc.main.global.common.PageResponse;
import com.grtc.main.global.common.Pages;
import com.grtc.main.global.file.FileStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// 자료실 조회·다운로드 API - 로그인한 회원이면 누구나 (등록·수정·삭제는 AdminArchiveController)
@RestController
@RequestMapping(ArchiveService.MEMBER_URL)
@RequiredArgsConstructor
public class ArchiveController {

    private final ArchiveService archiveService;
    private final FileStorage fileStorage;

    @GetMapping
    public ApiResponse<PageResponse<ArchiveDto.ListItem>> list(
            @AuthenticationPrincipal Long memberId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort
    ) {
        return ApiResponse.ok(archiveService.listForMember(memberId, keyword,
                Pages.request(page, size, sort, ArchiveService.DEFAULT_SORT, ArchiveService.SORTABLE)));
    }

    @GetMapping("/{archiveId}")
    public ApiResponse<ArchiveDto.Detail> get(@AuthenticationPrincipal Long memberId, @PathVariable Long archiveId) {
        return ApiResponse.ok(archiveService.getForMember(memberId, archiveId));
    }

    @GetMapping("/{archiveId}/files/{fileId}")
    public ResponseEntity<Resource> download(@AuthenticationPrincipal Long memberId,
                                             @PathVariable Long archiveId,
                                             @PathVariable Long fileId) {
        ArchiveFileEntity file = archiveService.getFileForMember(memberId, archiveId, fileId);
        return ArchiveFileResponses.download(file, fileStorage.load(file.getFilePath(), file.getSavedFileName()));
    }
}
