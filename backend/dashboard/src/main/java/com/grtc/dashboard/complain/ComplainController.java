package com.grtc.dashboard.complain;

import com.grtc.dashboard.complain.dto.ComplainDto;
import com.grtc.dashboard.complain.file.Attachment;
import com.grtc.dashboard.complain.file.FileService;
import com.grtc.dashboard.global.common.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

// 민원관리(관리자) API (관리자 전용: /api/admin/** 은 ROLE_ADMIN 만 허용)
@RestController
@RequestMapping("/api/admin/complaints")
@RequiredArgsConstructor
public class ComplainController {

    private final ComplainService complainService;
    private final FileService fileService;

    // 민원관리 목록 (검색어, 분류/유형/처리상태 필터, page 는 1부터)
    @GetMapping
    public PageResponse<ComplainDto.ListItem> list(
            @AuthenticationPrincipal Long adminId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ComplainCategory category,
            @RequestParam(required = false) ComplainType type,
            @RequestParam(required = false) ComplainStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return complainService.list(adminId,
                new ComplainDto.SearchCondition(keyword, category, type, status), page, size);
    }

    // 검색 필터 / 답변 작성 화면의 선택 목록 (유형, 분류, 처리상태)
    @GetMapping("/options")
    public ComplainDto.Options options() {
        return complainService.options();
    }

    // 민원 상세 (관리자 민원 상세 + 답변 작성 화면)
    @GetMapping("/{id}")
    public ComplainDto.Detail get(@AuthenticationPrincipal Long adminId, @PathVariable Long id) {
        return complainService.get(adminId, id);
    }

    // 관리자 민원 등록 (multipart/form-data: type, category, title, content, files)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ComplainDto.Detail> create(
            @AuthenticationPrincipal Long adminId,
            @Valid @ModelAttribute ComplainDto.SaveRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(complainService.create(adminId, request, files));
    }

    // 답변 등록 (처리상태 + 답변 내용)
    @PutMapping("/{id}/answer")
    public ComplainDto.Detail answer(@AuthenticationPrincipal Long adminId,
                                     @PathVariable Long id,
                                     @Valid @RequestBody ComplainDto.AnswerRequest request) {
        return complainService.answer(adminId, id, request);
    }

    // 첨부파일 다운로드
    @GetMapping("/{id}/attachments/{attachmentId}")
    public ResponseEntity<Resource> download(@PathVariable Long id, @PathVariable Long attachmentId) {
        Attachment attachment = complainService.getAttachmentForDownload(id, attachmentId);
        Resource resource = fileService.loadAsResource(attachment);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(attachment.getOriginalFileName(), StandardCharsets.UTF_8)
                        .build().toString())
                .body(resource);
    }
}
