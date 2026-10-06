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

// 민원관리 / 민원 양식 / 민원 상세 화면에서 쓰는 요청·응답 모음
public final class ComplainDto {

    private ComplainDto() {
    }

    // ---------- 요청 ----------

    // 민원 등록 / 수정 요청 (파일이 같이 오므로 multipart/form-data 의 일반 필드로 받는다)
    //  - category: 비워 두면 '기타'
    //  - contentHtml: 서식(굵게, 목록, 링크)이 들어간 내용(선택). 서버가 허용 태그만 남겨 저장한다.
    //                 content(일반 텍스트)는 검색·글자 수 기준이라 그대로 필수다.
    //  - deleteFileIds: 수정할 때 지울 기존 첨부파일 번호들
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

    // 목록 검색 조건
    public record SearchCondition(
            String keyword,              // 제목, 내용, 민원인 이름, 민원번호
            ComplainCategory category,   // 분류 필터
            ComplainType type,           // 유형 필터
            ComplainStatus status        // 처리상태 필터
    ) {
    }

    // ---------- 응답 ----------

    // 민원관리 목록 한 줄
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
            String statusLabel,       // 접수대기 / 답변중 / 답변완료 / 이관안내
            String progressLabel,     // 미처리 / 처리중 / 처리완료 (목록 화면 표시용)
            boolean mine              // 보는 사람이 쓴 민원인지 (false 면 상세보기가 열리지 않으므로 버튼 비활성화용)
    ) {
        // maskOthers=true 이면(일반 사용자 목록) 남의 민원은 민원인 이름만 가려서 내려준다. (제목은 그대로)
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

        // 정여원 -> 정*원, 김수 -> 김*, 홍길동이 -> 홍**이
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

    // 첨부파일 정보 (downloadUrl 로 GET 하면 내려받기)
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

    // 관리자 답변 (아직 답변 전이면 상세 응답의 answer 가 null)
    public record AnswerInfo(
            String content,
            LocalDateTime answeredAt,
            String answeredByName
    ) {
    }

    // 민원 상세 (사용자 상세 화면 / 관리자 상세+답변 화면 공통)
    //  - editable: 지금 보는 사람이 작성자 본인이고 접수대기 상태일 때만 true (수정/삭제 버튼 노출 기준)
    public record Detail(
            Long id,
            String complainNo,
            ComplainType type,
            String typeLabel,
            ComplainCategory category,
            String categoryLabel,
            String title,
            String content,
            String contentHtml,   // 서식이 들어간 내용(허용 태그만 남긴 HTML). 없으면 null -> content 를 그대로 보여준다
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

    // 선택 목록 항목 (code 를 서버로 보내고, label 을 화면에 보여준다)
    public record LabelValue(String code, String label) {
    }

    // 민원 양식/검색 필터의 선택 목록
    public record Options(
            List<LabelValue> types,
            List<LabelValue> categories,
            List<LabelValue> statuses
    ) {
    }
}
