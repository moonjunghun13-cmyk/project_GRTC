package com.grtc.main.qna.file;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/*
 * FileRepository 답안
 *
 * - 이 파일은 src 밖(study 폴더)에 있어서 빌드에 포함되지 않습니다.
 * - 실제로 쓰려면 인터페이스명을 FileRepository 로 바꿔서 file 폴더의 파일에 붙여넣으세요.
 * - 전제: Attachment 에 "complain" 필드(ComplainEntity)가 있고,
 *         ComplainEntity 에 "id" 필드가 있어야 아래 메서드 이름이 해석됩니다.
 */
public interface FileRepositoryAnswer extends JpaRepository<Attachment, Long> {

    // 민원 한 건에 붙은 첨부파일 목록
    // 메서드 이름 해석:  findAllBy + Complain(필드) + Id(그 안의 id)  ->  where complain.id = ?
    // 사용처: 민원 상세 화면의 첨부 목록, 민원 삭제 시 딸린 파일 정리
    List<Attachment> findAllByComplainId(Long complainId);

    // 민원 한 건에 붙은 첨부파일 개수 (파일 개수 제한을 "추가 업로드" 때도 지키고 싶을 때)
    long countByComplainId(Long complainId);
}
