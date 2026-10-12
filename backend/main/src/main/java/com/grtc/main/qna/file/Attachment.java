package com.grtc.main.qna.file;

import com.grtc.main.qna.complain.ComplainEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// 민원 첨부파일 1개의 정보를 담는 엔티티 (테이블 1줄 = 파일 1개)
@Entity
@Table(name = "complain_attachment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "complain_id", nullable = false)
    private ComplainEntity complain;

    // 민원인이 올린 원래 이름 - 화면 표시와 다운로드 이름으로 사용
    @Column(nullable = false, length = 255)
    private String originalFileName;

    @Column(nullable = false, unique = true, length = 100)
    private String savedFileName;

    @Column(nullable = false, length = 100)
    private String filePath;

    // 파일 크기 (바이트)
    @Column(nullable = false)
    private long fileSize;

    @Column(length = 100)
    private String contentType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 저장 직전에 자동 실행 - 직접 넣어주지 않아도 시각이 채워진다.
    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
