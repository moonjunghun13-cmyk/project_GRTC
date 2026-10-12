package com.grtc.main.notice;

import com.grtc.main.global.common.DateTimes;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

// 공지사항 요청·응답 모음
public final class NoticeDto {

    private NoticeDto() {
    }

    // 등록 (관리자)
    public record CreateRequest(
            @NotBlank(message = "필수 입력란입니다.")
            @Size(max = 200, message = "제목은 최대 200자까지 입력 가능합니다.")
            String title,

            @NotBlank(message = "필수 입력란입니다.")
            @Size(max = 10000, message = "내용은 최대 10,000자까지 입력 가능합니다.")
            String content,

            @Size(max = 50000, message = "서식이 포함된 내용이 너무 깁니다.")
            String contentHtml,

            Boolean pinned
    ) {
    }

    // 수정 (관리자, PATCH: 보낸 항목만 바뀐다. contentHtml 을 "" 로 보내면 서식이 지워진다)
    public record UpdateRequest(
            @Pattern(regexp = "(?s).*\\S.*", message = "필수 입력란입니다.")
            @Size(max = 200, message = "제목은 최대 200자까지 입력 가능합니다.")
            String title,

            @Pattern(regexp = "(?s).*\\S.*", message = "필수 입력란입니다.")
            @Size(max = 10000, message = "내용은 최대 10,000자까지 입력 가능합니다.")
            String content,

            @Size(max = 50000, message = "서식이 포함된 내용이 너무 깁니다.")
            String contentHtml,

            Boolean pinned
    ) {
    }

    public record ListItem(
            Long id,
            String title,
            boolean pinned,
            String writerName,
            long viewCount,
            OffsetDateTime createdAt
    ) {
        public static ListItem from(NoticeEntity n) {
            return new ListItem(
                    n.getId(),
                    n.getTitle(),
                    n.isPinned(),
                    n.getWriter().getName(),
                    n.getViewCount(),
                    DateTimes.toOffset(n.getCreatedAt())
            );
        }
    }

    public record Detail(
            Long id,
            String title,
            String content,
            String contentHtml,
            boolean pinned,
            String writerName,
            long viewCount,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        public static Detail from(NoticeEntity n) {
            return new Detail(
                    n.getId(),
                    n.getTitle(),
                    n.getContent(),
                    n.getContentHtml(),
                    n.isPinned(),
                    n.getWriter().getName(),
                    n.getViewCount(),
                    DateTimes.toOffset(n.getCreatedAt()),
                    DateTimes.toOffset(n.getUpdatedAt())
            );
        }
    }
}
