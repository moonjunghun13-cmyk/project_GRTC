package com.grtc.main.archive;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ArchiveFileRepository extends JpaRepository<ArchiveFileEntity, Long> {

    List<ArchiveFileEntity> findAllByArchiveIdOrderByIdAsc(Long archiveId);

    Optional<ArchiveFileEntity> findByIdAndArchiveId(Long id, Long archiveId);

    // 목록 화면의 첨부파일 개수를 한 번에 센다. 결과: [archiveId, count]
    @Query("select f.archive.id, count(f) from ArchiveFileEntity f where f.archive.id in :archiveIds group by f.archive.id")
    List<Object[]> countByArchiveIds(@Param("archiveIds") Collection<Long> archiveIds);
}
