package com.grtc.main.archive;

import com.grtc.main.global.common.DateTimes;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.List;

// 자료실 요청·응답 모음
public final class ArchiveDto {

    private ArchiveDto() {
    }

    // 등록·수정 (관리자, multipart 의 일반 필드). 파일은 files 파트로 따로 받는다
    //   수정(PUT)은 보낸 값으로 통째로 바뀐다. 지울 첨부파일은 deleteFileIds 로 보낸다
    public record SaveRequest(
            @NotBlank(message = "필수 입력란입니다.")
            @Size(max = 200, message = "제목은 최대 200자까지 입력 가능합니다.")
            String title,

            @Size(max = 10000, message = "내용은 최대 10,000자까지 입력 가능합니다.")
            String content,

            @Size(max = 50000, message = "서식이 포함된 내용이 너무 깁니다.")
            String contentHtml,

            List<Long> deleteFileIds
    ) {
    }

    public record ListItem(
            Long id,
            String title,
            String writerName,
            long viewCount,
            long fileCount,
            OffsetDateTime createdAt
    ) {
        public static ListItem of(ArchiveEntity a, long fileCount) {
            return new ListItem(
                    a.getId(),
                    a.getTitle(),
                    a.getWriter().getName(),
                    a.getViewCount(),
                    fileCount,
                    DateTimes.toOffset(a.getCreatedAt())
            );
        }
    }

    public record FileInfo(
            Long id,
            String originalFileName,
            long fileSize,
            String contentType,
            String downloadUrl
    ) {
        public static FileInfo of(String archiveUrl, ArchiveFileEntity f) {
            return new FileInfo(
                    f.getId(),
                    f.getOriginalFileName(),
                    f.getFileSize(),
                    f.getContentType(),
                    archiveUrl + "/files/" + f.getId()
            );
        }
    }

    public record Detail(
            Long id,
            String title,
            String content,
            String contentHtml,
            String writerName,
            long viewCount,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt,
            List<FileInfo> files
    ) {
        // urlPrefix: 회원 화면 "/api/v1/archives", 관리자 화면 "/api/v1/admin/archives"
        public static Detail of(ArchiveEntity a, List<ArchiveFileEntity> files, String urlPrefix) {
            String archiveUrl = urlPrefix + "/" + a.getId();
            return new Detail(
                    a.getId(),
                    a.getTitle(),
                    a.getContent(),
                    a.getContentHtml(),
                    a.getWriter().getName(),
                    a.getViewCount(),
                    DateTimes.toOffset(a.getCreatedAt()),
                    DateTimes.toOffset(a.getUpdatedAt()),
                    files.stream().map(f -> FileInfo.of(archiveUrl, f)).toList()
            );
        }
    }
}
