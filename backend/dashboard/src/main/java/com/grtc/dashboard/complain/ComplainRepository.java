package com.grtc.dashboard.complain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;

public interface ComplainRepository extends JpaRepository<ComplainEntity, Long>, JpaSpecificationExecutor<ComplainEntity> {

    // 민원번호 일련번호 계산용 (접수일 접두사 CM260930- 로 시작하는 민원 수)
    long countByComplainNoStartingWith(String prefix);

    // ---- 대시보드 통계: 기준 시각(from) 이후에 접수된 민원 ----
    long countByCreatedAtGreaterThanEqual(LocalDateTime from);

    long countByTypeAndCreatedAtGreaterThanEqual(ComplainType type, LocalDateTime from);

    long countByStatusAndCreatedAtGreaterThanEqual(ComplainStatus status, LocalDateTime from);
}
