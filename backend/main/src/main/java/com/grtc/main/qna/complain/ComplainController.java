package com.grtc.main.qna.complain;

import com.grtc.main.global.common.PageResponse;
import com.grtc.main.qna.complain.dto.ComplainDto;
import com.grtc.main.qna.file.Attachment;
import com.grtc.main.qna.file.FileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
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

import java.nio.charset.StandardCharsets;
import java.util.List;

// 민원관리(사용자) API - 로그인한 회원이면 누구나 호출 가능
//   목록(민원관리) / 글쓰기(민원 양식) / 글확인(민원 상세) / 수정 / 삭제 / 첨부파일 다운로드
//   ※ 관리자 답변·전체 관리는 dashboard 서버의 /api/admin/complaints 에 있다.
@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
public class ComplainController {

    private final ComplainService complainService;
    private final FileService fileService;

    // 민원관리 목록 (검색어: 제목/내용/민원인/민원번호, 분류·유형·처리상태 필터, page 는 1부터)
    @GetMapping
    public PageResponse<ComplainDto.ListItem> list(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ComplainCategory category,
            @RequestParam(required = false) ComplainType type,
            @RequestParam(required = false) ComplainStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return complainService.listForUser(userId,
                new ComplainDto.SearchCondition(keyword, category, type, status), page, size);
    }

    // 민원 양식/검색 필터의 선택 목록 (유형, 분류, 처리상태)
    @GetMapping("/options")
    public ComplainDto.Options options() {
        return complainService.options();
    }

    // 민원 상세 (본인 민원만, 남의 민원은 404)
    @GetMapping("/{id}")
    public ComplainDto.Detail get(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        return complainService.get(userId, id);
    }

    // 민원 등록 (multipart/form-data: type, category, title, content, files)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ComplainDto.Detail> create(
            @AuthenticationPrincipal Long userId,
            @Valid @ModelAttribute ComplainDto.SaveRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(complainService.create(userId, request, files));
    }

    // 민원 수정 (본인 + 접수대기 상태만, deleteFileIds 로 기존 첨부 삭제, files 로 새 첨부 추가)
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ComplainDto.Detail update(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @Valid @ModelAttribute ComplainDto.SaveRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        return complainService.update(userId, id, request, files);
    }

    // 민원 삭제 (본인 + 접수대기 상태만)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        complainService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }

    // 첨부파일 다운로드 (민원을 볼 수 있는 사람만)
    @GetMapping("/{id}/attachments/{attachmentId}")
    public ResponseEntity<Resource> download(@AuthenticationPrincipal Long userId,
                                             @PathVariable Long id,
                                             @PathVariable Long attachmentId) {
        Attachment attachment = complainService.getAttachmentForDownload(userId, id, attachmentId);
        Resource resource = fileService.loadAsResource(attachment);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(attachment.getOriginalFileName(), StandardCharsets.UTF_8)
                        .build().toString())
                .body(resource);
    }
}
