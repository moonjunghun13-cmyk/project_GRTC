package com.grtc.main.admin.operation;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OperationRepository extends JpaRepository<OperationEntity, Long> {

    boolean existsByVehicleId(Long vehicleId);
}
