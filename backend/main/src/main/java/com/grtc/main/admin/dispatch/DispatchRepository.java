package com.grtc.main.admin.dispatch;

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

    // 배차번호 일련번호 계산용: 배차일 접두사(DISP260930-)로 시작하는 배차번호 중 가장 큰 일련번호. 없으면 0
    //  - start: 일련번호가 시작하는 위치(1부터 센다) = 접두사 길이 + 1
    //  - 문자열이 아니라 숫자로 비교한다. (문자열로 비교하면 1000 이 999 보다 작다고 나온다)
    //  - 배차일 칸이 아니라 배차번호로 찾는다. (배차일을 수정해도 배차번호는 처음 날짜 그대로이기 때문)
    @Query("select coalesce(max(cast(substring(d.dispatchNo, :start) as integer)), 0) "
            + "from DispatchEntity d where d.dispatchNo like concat(:prefix, '%')")
    int findMaxSequence(@Param("prefix") String prefix, @Param("start") int start);

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

    // ---- 입·출고 시간표 배차 ----

    // 그 날짜에 시간표로 만든 배차가 있는지
    boolean existsByDispatchDateAndMoveTypeIsNotNull(LocalDate dispatchDate);

    // 시간표로 만든 배차가 하나라도 있는지 (초기 데이터 교체 판단용)
    boolean existsByMoveTypeIsNotNull();

    // 대시보드: 그 날짜의 입·출고 배차 (취소 제외), 시각 순
    List<DispatchEntity> findAllByDispatchDateAndMoveTypeIsNotNullAndStatusNotOrderByDepartureTimeAsc(
            LocalDate dispatchDate, DispatchStatus excluded);

    // 예전 예시 배차(시간표 이전 초기 데이터) 정리용
    List<DispatchEntity> findAllByMoveTypeIsNullAndDriverNameIn(List<String> driverNames);
}
