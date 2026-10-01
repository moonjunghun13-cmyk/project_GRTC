package com.grtc.dashboard.dispatch;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface DispatchRepository extends JpaRepository<DispatchEntity, Long>, JpaSpecificationExecutor<DispatchEntity> {

    // 차량 삭제 가능 여부 확인용 (배차 이력이 있는 차량은 삭제 불가)
    boolean existsByVehicleId(Long vehicleId);

    // 배차번호 일련번호 계산용 (같은 배차일의 배차 수)
    long countByDispatchDate(LocalDate dispatchDate);

    // 같은 차량, 같은 날에 시간이 겹치는 배차(취소 제외, 자기 자신 제외)가 몇 건인지
    // 겹침 조건: 기존 출발 < 새 도착  AND  기존 도착 > 새 출발
    @Query("""
            select count(d) from DispatchEntity d
            where d.vehicle.id = :vehicleId
              and d.dispatchDate = :date
              and d.status <> :excluded
              and d.id <> :excludeId
              and d.departureTime < :arrival
              and d.arrivalTime > :departure
            """)
    long countOverlap(@Param("vehicleId") Long vehicleId,
                      @Param("date") LocalDate date,
                      @Param("departure") LocalTime departure,
                      @Param("arrival") LocalTime arrival,
                      @Param("excluded") DispatchStatus excluded,
                      @Param("excludeId") Long excludeId);

    // 배차 검색 필터의 운전자 선택 목록
    @Query("select distinct d.driverName from DispatchEntity d order by d.driverName")
    List<String> findDistinctDriverNames();
}
