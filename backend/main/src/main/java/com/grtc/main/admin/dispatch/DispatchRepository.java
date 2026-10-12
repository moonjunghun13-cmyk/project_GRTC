package com.grtc.main.admin.dispatch;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface DispatchRepository extends JpaRepository<DispatchEntity, Long>, JpaSpecificationExecutor<DispatchEntity> {

    boolean existsByVehicleId(Long vehicleId);

    @Query("select coalesce(max(cast(substring(d.dispatchNo, :start) as integer)), 0) "
            + "from DispatchEntity d where d.dispatchNo like concat(:prefix, '%')")
    int findMaxSequence(@Param("prefix") String prefix, @Param("start") int start);

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

    @Query("select distinct d.driverName from DispatchEntity d order by d.driverName")
    List<String> findDistinctDriverNames();
}
