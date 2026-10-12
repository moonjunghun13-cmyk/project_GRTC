package com.grtc.main.archive;

import com.grtc.main.login.entity.LoginEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// 자료실 글 (테이블 archive). 첨부파일은 ArchiveFileEntity. 작성·수정·삭제는 관리자만
@Entity
@Table(name = "archive")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ArchiveEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    // 자료 설명 (선택)
    @Column(columnDefinition = "text")
    private String content;

    @Column(name = "content_html", columnDefinition = "text")
    private String contentHtml;

    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private long viewCount = 0;

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

    public void update(String title, String content, String contentHtml) {
        this.title = title;
        this.content = content;
        this.contentHtml = contentHtml;
    }

    // 첨부파일만 바뀌어도 수정일시가 바뀌도록
    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }
}
