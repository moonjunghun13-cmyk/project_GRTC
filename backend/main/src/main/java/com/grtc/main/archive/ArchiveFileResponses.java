package com.grtc.main.archive;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;

// 첨부파일 다운로드 응답 (회원·관리자 컨트롤러 공통). 한글 파일명도 깨지지 않게 UTF-8 로 내려준다
public final class ArchiveFileResponses {

    private ArchiveFileResponses() {
    }

    public static ResponseEntity<Resource> download(ArchiveFileEntity file, Resource resource) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(file.getOriginalFileName(), StandardCharsets.UTF_8)
                        .build().toString())
                .body(resource);
    }
}
