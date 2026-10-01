package com.grtc.dashboard.complain.file;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// 첨부파일(Attachment) 데이터 접근. 기본 CRUD 는 JpaRepository 가 제공한다.
public interface FileRepository extends JpaRepository<Attachment, Long> {

    // 민원 한 건에 붙은 첨부파일 목록
    // 메서드 이름 해석:  findAllBy + Complain(필드) + Id(그 안의 id)  ->  where complain.id = ?
    // 사용처: 민원 상세 화면의 첨부 목록, 민원 삭제 시 딸린 파일 정리
    List<Attachment> findAllByComplainId(Long complainId);

    // 민원 한 건에 붙은 첨부파일 개수
    long countByComplainId(Long complainId);
}
