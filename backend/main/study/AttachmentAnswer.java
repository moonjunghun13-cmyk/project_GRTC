package com.grtc.main.qna.file;

import com.grtc.main.qna.complain.ComplainEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/*
 * Attachment 답안 (민원 첨부파일 1개의 정보)
 *
 * - 이 파일은 src 밖(study 폴더)에 있어서 빌드에 포함되지 않습니다.
 * - 실제로 쓰려면 클래스명을 Attachment 로 바꿔서 file 폴더의 파일에 붙여넣으세요.
 * - 전제: ComplainEntity 가 @Entity 이고 Long 타입의 id 필드를 가지고 있어야 합니다.
 *         (지금은 빈 클래스라서 먼저 만들어야 컴파일됩니다)
 *
 * 기존 코드에서 바뀐 점
 *   - fileId(의미가 모호한 번호)   -> complain (@ManyToOne, 어느 민원의 첨부인지)
 *   - @Setter 제거                 -> 저장 후 값이 바뀔 일이 없으므로
 *   - fileSize 를 long 으로        -> 항상 있는 값이라 null 을 허용하지 않음
 *   - contentType 추가             -> 다운로드 응답을 만들 때 필요
 *   - createdAt 을 저장 직전에 자동으로 채움
 */
@Entity
@Table(name = "attachment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)   // JPA 용 기본 생성자 (외부에서 함부로 못 만들게 protected)
@AllArgsConstructor(access = AccessLevel.PRIVATE)    // @Builder 가 사용
@Builder
public class AttachmentAnswer {

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
