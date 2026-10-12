package com.grtc.main.admin.operation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StationRepository extends JpaRepository<StationEntity, Long> {

    List<StationEntity> findAllByRouteIdOrderBySeqAsc(Long routeId);
}
