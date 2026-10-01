package com.grtc.dashboard.operation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StationRepository extends JpaRepository<StationEntity, Long> {

    // 노선의 역을 순서대로 조회
    List<StationEntity> findAllByRouteIdOrderBySeqAsc(Long routeId);
}
