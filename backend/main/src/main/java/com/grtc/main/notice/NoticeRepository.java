package com.grtc.main.notice;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NoticeRepository extends JpaRepository<NoticeEntity, Long>, JpaSpecificationExecutor<NoticeEntity> {

    // 목록에서 작성자 이름을 쓰므로 writer 를 함께 불러온다 (N+1 방지)
    @Override
    @EntityGraph(attributePaths = "writer")
    Page<NoticeEntity> findAll(Specification<NoticeEntity> spec, Pageable pageable);

    // 조회수는 엔티티에서 ++ 하지 않고 DB 에서 바로 올린다 (동시에 여러 명이 봐도 빠지지 않게)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update NoticeEntity n set n.viewCount = n.viewCount + 1 where n.id = :id")
    int increaseViewCount(@Param("id") Long id);
}
