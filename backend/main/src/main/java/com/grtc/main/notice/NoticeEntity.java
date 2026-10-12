package com.grtc.main.notice;

import com.grtc.main.login.entity.LoginEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// 공지사항 (테이블 notice). 작성·수정·삭제는 관리자만, 조회는 로그인한 회원 누구나
@Entity
@Table(name = "notice")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class NoticeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    // 평문 내용 (목록 검색·에디터가 없는 화면용)
    @Column(nullable = false, columnDefinition = "text")
    private String content;

    // 에디터 서식이 들어간 내용. 저장 전에 HtmlSanitizer 로 허용 태그만 남긴다
    @Column(name = "content_html", columnDefinition = "text")
    private String contentHtml;

    // 상단 고정 여부. 목록에서 항상 맨 위에 나온다
    @Column(nullable = false)
    @Builder.Default
    private boolean pinned = false;

    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private long viewCount = 0;

    // 작성한 관리자
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "writer_id", nullable = false)
    private LoginEntity writer;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void update(String title, String content, String contentHtml, boolean pinned) {
        this.title = title;
        this.content = content;
        this.contentHtml = contentHtml;
        this.pinned = pinned;
    }
}
