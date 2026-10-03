package com.grtc.main.admin.vehicle;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface VehicleRepository extends JpaRepository<VehicleEntity, Long>, JpaSpecificationExecutor<VehicleEntity> {

    boolean existsByVehicleNo(String vehicleNo);

    // 수정할 때 본인을 제외하고 같은 차량번호가 있는지 확인
    boolean existsByVehicleNoAndIdNot(String vehicleNo, Long id);

    long countByStatus(VehicleStatus status);
}
