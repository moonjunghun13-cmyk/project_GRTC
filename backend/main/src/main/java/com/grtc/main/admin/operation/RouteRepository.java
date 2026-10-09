package com.grtc.main.admin.operation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RouteRepository extends JpaRepository<RouteEntity, Long> {

    // 현재는 노선이 1호선 하나뿐이라 첫 번째 노선을 사용한다.
    Optional<RouteEntity> findFirstByOrderByIdAsc();
}
