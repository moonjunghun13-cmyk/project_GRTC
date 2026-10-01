package com.grtc.dashboard.complain.file;

import com.grtc.dashboard.complain.ComplainEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// 민원 첨부파일 1개의 정보를 담는 엔티티 (테이블 1줄 = 파일 1개)
//  - 파일 "내용"은 DB 가 아니라 서버 디스크의 업로드 폴더에 저장하고,
//  - 이 엔티티에는 원래 이름, 저장한 이름, 위치, 크기 같은 "정보"만 저장한다.
// ※ 테이블명을 complain_attachment 로 쓴 이유: 예전 attachment 테이블(file_id 컬럼, NOT NULL)과
//    구조가 달라서, 같은 이름을 쓰면 ddl-auto: update 로는 기존 컬럼이 남아 insert 가 실패할 수 있다.
@Entity
@Table(name = "complain_attachment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)   // JPA 용 기본 생성자 (외부에서 함부로 못 만들게 protected)
@AllArgsConstructor(access = AccessLevel.PRIVATE)    // @Builder 가 사용
@Builder
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 이 첨부파일이 속한 민원. 민원 1건에 파일 여러 개 -> 파일 쪽이 N, 민원 쪽이 1
    // LAZY: 첨부파일만 조회할 때 민원까지 매번 가져오지 않도록 (필요할 때만 조회)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "complain_id", nullable = false)
    private ComplainEntity complain;

    // 민원인이 올린 원래 이름 - 화면 표시와 다운로드 이름으로 사용
    @Column(nullable = false, length = 255)
    private String originalFileName;

    // 서버에 저장한 이름 (UUID + 확장자) - 겹치면 안 되므로 unique
    @Column(nullable = false, unique = true, length = 100)
    private String savedFileName;

    // 업로드 폴더 기준의 "상대 경로" (예: 2026/10). 컴퓨터 전체 경로를 저장하지 않는다.
    @Column(nullable = false, length = 100)
    private String filePath;

    // 파일 크기 (바이트)
    @Column(nullable = false)
    private long fileSize;

    // 파일 종류 (예: application/pdf) - 사용자가 속일 수 있는 값이라 참고용으로만
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
