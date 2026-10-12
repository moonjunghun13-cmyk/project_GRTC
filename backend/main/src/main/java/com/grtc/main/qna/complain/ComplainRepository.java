package com.grtc.main.qna.complain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ComplainRepository extends JpaRepository<ComplainEntity, Long>, JpaSpecificationExecutor<ComplainEntity> {

    @Query("select coalesce(max(cast(substring(c.complainNo, :start) as integer)), 0) "
            + "from ComplainEntity c where c.complainNo like concat(:prefix, '%')")
    int findMaxSequence(@Param("prefix") String prefix, @Param("start") int start);

    long countByCreatedAtGreaterThanEqual(LocalDateTime from);

    long countByTypeAndCreatedAtGreaterThanEqual(ComplainType type, LocalDateTime from);

    long countByStatusAndCreatedAtGreaterThanEqual(ComplainStatus status, LocalDateTime from);
}
