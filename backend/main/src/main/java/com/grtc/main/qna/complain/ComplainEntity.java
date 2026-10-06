package com.grtc.main.qna.complain;

import com.grtc.main.login.entity.LoginEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// 민원 엔티티 (민원 1건 = 1줄). 관리자 답변도 같은 줄에 저장한다.
@Entity
@Table(name = "complain")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ComplainEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 민원번호. 예: CM260930-001 (CM + 접수일 yyMMdd + 일련번호)
    @Column(name = "complain_no", nullable = false, unique = true, length = 20)
    private String complainNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ComplainType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ComplainCategory category;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 5000)
    private String content;

    // 서식(굵게, 목록, 링크)이 들어간 내용. 서버가 허용 태그만 남긴 HTML 이다. 서식 없이 등록했으면 null
    @Column(name = "content_html", columnDefinition = "text")
    private String contentHtml;

    // 민원인(작성자)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "writer_id", nullable = false)
    private LoginEntity writer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ComplainStatus status = ComplainStatus.WAITING;

    // ---- 관리자 답변 (답변 전에는 모두 null) ----
    @Column(name = "answer_content", length = 5000)
    private String answerContent;

    @Column(name = "answered_at")
    private LocalDateTime answeredAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answered_by")
    private LoginEntity answeredBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 민원인이 철회(사용자 화면에서는 '삭제')한 시각. 철회 전에는 null
    @Column(name = "withdrawn_at")
    private LocalDateTime withdrawnAt;

    @PrePersist
    protected void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = createdAt;
        }
    }

    // 민원인이 내용을 수정할 때 (처리 상태는 건드리지 않는다)
    public void update(ComplainType type, ComplainCategory category, String title, String content,
                       String contentHtml) {
        this.type = type;
        this.category = category;
        this.title = title;
        this.content = content;
        this.contentHtml = contentHtml;
        this.updatedAt = LocalDateTime.now();
    }

    // 관리자가 답변을 등록/수정할 때: 처리상태와 답변 내용을 함께 바꾼다.
    public void answer(ComplainStatus status, String answerContent, LoginEntity admin) {
        this.status = status;
        this.answerContent = answerContent;
        this.answeredBy = admin;
        this.answeredAt = LocalDateTime.now();
    }

    // 민원인이 민원을 철회할 때: 줄을 지우지 않고 상태만 '철회'로 바꾼다. (내용·첨부파일은 관리자용으로 남긴다)
    public void withdraw() {
        this.status = ComplainStatus.WITHDRAWN;
        this.withdrawnAt = LocalDateTime.now();
    }

    public boolean isWithdrawn() {
        return status == ComplainStatus.WITHDRAWN;
    }
}
