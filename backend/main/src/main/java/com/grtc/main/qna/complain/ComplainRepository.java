package com.grtc.main.qna.complain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ComplainRepository extends JpaRepository<ComplainEntity, Long>, JpaSpecificationExecutor<ComplainEntity> {

    // 민원번호 일련번호 계산용: 접수일 접두사(CM260930-)로 시작하는 민원번호 중 가장 큰 일련번호. 없으면 0
    //  - start: 일련번호가 시작하는 위치(1부터 센다) = 접두사 길이 + 1
    //  - 문자열이 아니라 숫자로 비교한다. (문자열로 비교하면 1000 이 999 보다 작다고 나온다)
    @Query("select coalesce(max(cast(substring(c.complainNo, :start) as integer)), 0) "
            + "from ComplainEntity c where c.complainNo like concat(:prefix, '%')")
    int findMaxSequence(@Param("prefix") String prefix, @Param("start") int start);

    // ---- 대시보드 통계(관리자): 기준 시각(from) 이후에 접수된 민원 ----
    long countByCreatedAtGreaterThanEqual(LocalDateTime from);

    long countByTypeAndCreatedAtGreaterThanEqual(ComplainType type, LocalDateTime from);

    long countByStatusAndCreatedAtGreaterThanEqual(ComplainStatus status, LocalDateTime from);
}
