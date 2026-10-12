package com.grtc.main.admin.operation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RouteRepository extends JpaRepository<RouteEntity, Long> {

    Optional<RouteEntity> findFirstByOrderByIdAsc();
}
