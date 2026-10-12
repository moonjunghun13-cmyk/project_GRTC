package com.grtc.main.admin.archive;

import com.grtc.main.archive.ArchiveDto;
import com.grtc.main.archive.ArchiveFileEntity;
import com.grtc.main.archive.ArchiveFileResponses;
import com.grtc.main.archive.ArchiveService;
import com.grtc.main.global.common.ApiResponse;
import com.grtc.main.global.common.PageResponse;
import com.grtc.main.global.common.Pages;
import com.grtc.main.global.file.FileStorage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

// 자료실관리 (관리자) - 등록·수정·삭제는 여기서만 할 수 있다
@RestController
@RequestMapping(ArchiveService.ADMIN_URL)
@RequiredArgsConstructor
public class AdminArchiveController {

    private final ArchiveService archiveService;
    private final FileStorage fileStorage;

    @GetMapping
    public ApiResponse<PageResponse<ArchiveDto.ListItem>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort
    ) {
        return ApiResponse.ok(archiveService.list(keyword,
                Pages.request(page, size, sort, ArchiveService.DEFAULT_SORT, ArchiveService.SORTABLE)));
    }

    @GetMapping("/{archiveId}")
    public ApiResponse<ArchiveDto.Detail> get(@PathVariable Long archiveId) {
        return ApiResponse.ok(archiveService.getForAdmin(archiveId));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ArchiveDto.Detail>> create(
            @AuthenticationPrincipal Long adminId,
            @Valid @ModelAttribute ArchiveDto.SaveRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(archiveService.create(adminId, request, files)));
    }

    @PutMapping(value = "/{archiveId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ArchiveDto.Detail> update(
            @PathVariable Long archiveId,
            @Valid @ModelAttribute ArchiveDto.SaveRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        return ApiResponse.ok(archiveService.update(archiveId, request, files));
    }

    @DeleteMapping("/{archiveId}")
    public ApiResponse<Void> delete(@PathVariable Long archiveId) {
        archiveService.delete(archiveId);
        return ApiResponse.ok();
    }

    @GetMapping("/{archiveId}/files/{fileId}")
    public ResponseEntity<Resource> download(@PathVariable Long archiveId, @PathVariable Long fileId) {
        ArchiveFileEntity file = archiveService.getFile(archiveId, fileId);
        return ArchiveFileResponses.download(file, fileStorage.load(file.getFilePath(), file.getSavedFileName()));
    }
}
