package com.grtc.main.qna.complain.dto;

import com.grtc.main.qna.complain.ComplainCategory;
import com.grtc.main.qna.complain.ComplainEntity;
import com.grtc.main.qna.complain.ComplainStatus;
import com.grtc.main.qna.complain.ComplainType;
import com.grtc.main.qna.file.Attachment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public final class ComplainDto {

    private ComplainDto() {
    }

    public record SaveRequest(
            @NotNull(message = "필수 선택 항목입니다.")
            ComplainType type,

            ComplainCategory category,

            @NotBlank(message = "필수 입력란입니다.")
            @Size(max = 200, message = "제목은 최대 200자까지 입력 가능합니다.")
            String title,

            @NotBlank(message = "필수 입력란입니다.")
            @Size(max = 5000, message = "내용은 최대 5,000자까지 입력 가능합니다.")
            String content,

            @Size(max = 20000, message = "서식이 포함된 내용이 너무 깁니다.")
            String contentHtml,

            List<Long> deleteFileIds
    ) {
    }

    public record SearchCondition(
            String keyword,
            ComplainCategory category,
            ComplainType type,
            ComplainStatus status
    ) {
    }

    public record ListItem(
            Long id,
            String complainNo,
            LocalDateTime createdAt,
            ComplainCategory category,
            String categoryLabel,
            ComplainType type,
            String typeLabel,
            String title,
            String writerName,
            ComplainStatus status,
            String statusLabel,
            String progressLabel,
            boolean mine
    ) {
        public static ListItem from(ComplainEntity c, Long viewerId, boolean maskOthers) {
            boolean mine = c.getWriter().getId().equals(viewerId);
            boolean hideName = maskOthers && !mine;
            return new ListItem(
                    c.getId(),
                    c.getComplainNo(),
                    c.getCreatedAt(),
                    c.getCategory(),
                    c.getCategory().getLabel(),
                    c.getType(),
                    c.getType().getLabel(),
                    c.getTitle(),
                    hideName ? maskName(c.getWriter().getName()) : c.getWriter().getName(),
                    c.getStatus(),
                    c.getStatus().getLabel(),
                    c.getStatus().getProgressLabel(),
                    mine
            );
        }

        private static String maskName(String name) {
            if (name == null || name.length() < 2) {
                return name;
            }
            if (name.length() == 2) {
                return name.charAt(0) + "*";
            }
            return name.charAt(0) + "*".repeat(name.length() - 2) + name.charAt(name.length() - 1);
        }
    }

    public record AttachmentInfo(
            Long id,
            String originalFileName,
            long fileSize,
            String contentType,
            String downloadUrl
    ) {
        public static AttachmentInfo of(Long complainId, Attachment a) {
            return new AttachmentInfo(
                    a.getId(),
                    a.getOriginalFileName(),
                    a.getFileSize(),
                    a.getContentType(),
                    "/api/complaints/" + complainId + "/attachments/" + a.getId()
            );
        }
    }

    public record AnswerInfo(
            String content,
            LocalDateTime answeredAt,
            String answeredByName
    ) {
    }

    public record Detail(
            Long id,
            String complainNo,
            ComplainType type,
            String typeLabel,
            ComplainCategory category,
            String categoryLabel,
            String title,
            String content,
            String contentHtml,
            Long writerId,
            String writerName,
            ComplainStatus status,
            String statusLabel,
            String progressLabel,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            List<AttachmentInfo> attachments,
            AnswerInfo answer,
            boolean editable
    ) {
    }

    public record LabelValue(String code, String label) {
    }

    public record Options(
            List<LabelValue> types,
            List<LabelValue> categories,
            List<LabelValue> statuses
    ) {
    }
}
