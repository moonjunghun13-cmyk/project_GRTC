package com.grtc.main.admin.complain;

import com.grtc.main.qna.complain.ComplainCategory;
import com.grtc.main.qna.complain.ComplainStatus;
import com.grtc.main.qna.complain.ComplainType;
import com.grtc.main.qna.file.Attachment;
import com.grtc.main.qna.file.FileService;
import com.grtc.main.global.common.PageResponse;
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

@RestController
@RequestMapping("/api/admin/complaints")
@RequiredArgsConstructor
public class AdminComplainController {

    private final AdminComplainService complainService;
    private final FileService fileService;

    @GetMapping
    public PageResponse<AdminComplainDto.ListItem> list(
            @AuthenticationPrincipal Long adminId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ComplainCategory category,
            @RequestParam(required = false) ComplainType type,
            @RequestParam(required = false) ComplainStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return complainService.list(adminId,
                new AdminComplainDto.SearchCondition(keyword, category, type, status), page, size);
    }

    @GetMapping("/options")
    public AdminComplainDto.Options options() {
        return complainService.options();
    }

    @GetMapping("/{id}")
    public AdminComplainDto.Detail get(@AuthenticationPrincipal Long adminId, @PathVariable Long id) {
        return complainService.get(adminId, id);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AdminComplainDto.Detail> create(
            @AuthenticationPrincipal Long adminId,
            @Valid @ModelAttribute AdminComplainDto.SaveRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(complainService.create(adminId, request, files));
    }

    @PutMapping("/{id}/answer")
    public AdminComplainDto.Detail answer(@AuthenticationPrincipal Long adminId,
                                     @PathVariable Long id,
                                     @Valid @RequestBody AdminComplainDto.AnswerRequest request) {
        return complainService.answer(adminId, id, request);
    }

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
