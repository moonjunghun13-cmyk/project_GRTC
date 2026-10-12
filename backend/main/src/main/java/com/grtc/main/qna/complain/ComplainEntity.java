package com.grtc.main.qna.complain;

import com.grtc.main.login.entity.LoginEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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

    @Column(name = "content_html", columnDefinition = "text")
    private String contentHtml;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "writer_id", nullable = false)
    private LoginEntity writer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ComplainStatus status = ComplainStatus.WAITING;

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

    public void update(ComplainType type, ComplainCategory category, String title, String content,
                       String contentHtml) {
        this.type = type;
        this.category = category;
        this.title = title;
        this.content = content;
        this.contentHtml = contentHtml;
        this.updatedAt = LocalDateTime.now();
    }

    public void answer(ComplainStatus status, String answerContent, LoginEntity admin) {
        this.status = status;
        this.answerContent = answerContent;
        this.answeredBy = admin;
        this.answeredAt = LocalDateTime.now();
    }

    public void withdraw() {
        this.status = ComplainStatus.WITHDRAWN;
        this.withdrawnAt = LocalDateTime.now();
    }

    public boolean isWithdrawn() {
        return status == ComplainStatus.WITHDRAWN;
    }
}
