package com.grtc.main.archive;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// 자료실 첨부파일 1개 (테이블 archive_file)
@Entity
@Table(name = "archive_file", indexes = @Index(name = "idx_archive_file_archive", columnList = "archive_id"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ArchiveFileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "archive_id", nullable = false)
    private ArchiveEntity archive;

    // 관리자가 올린 원래 이름 - 화면 표시와 다운로드 이름으로 사용
    @Column(name = "original_file_name", nullable = false, length = 255)
    private String originalFileName;

    // 디스크에 저장된 이름 (UUID)
    @Column(name = "saved_file_name", nullable = false, unique = true, length = 100)
    private String savedFileName;

    // 업로드 폴더 기준 경로 (예: archive/2026/10)
    @Column(name = "file_path", nullable = false, length = 100)
    private String filePath;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
