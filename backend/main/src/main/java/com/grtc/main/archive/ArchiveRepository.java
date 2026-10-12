package com.grtc.main.archive;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArchiveRepository extends JpaRepository<ArchiveEntity, Long>, JpaSpecificationExecutor<ArchiveEntity> {

    @Override
    @EntityGraph(attributePaths = "writer")
    Page<ArchiveEntity> findAll(Specification<ArchiveEntity> spec, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update ArchiveEntity a set a.viewCount = a.viewCount + 1 where a.id = :id")
    int increaseViewCount(@Param("id") Long id);
}
