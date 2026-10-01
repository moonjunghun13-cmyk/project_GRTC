package com.grtc.dashboard.operation;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OperationRepository extends JpaRepository<OperationEntity, Long> {

    // 차량 삭제 가능 여부 확인용 (운행 중 기록이 있는 차량은 삭제 불가)
    boolean existsByVehicleId(Long vehicleId);
}
