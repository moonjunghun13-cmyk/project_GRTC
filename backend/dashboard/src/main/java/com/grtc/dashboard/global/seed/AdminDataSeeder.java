package com.grtc.dashboard.global.seed;

import com.grtc.dashboard.dispatch.DispatchEntity;
import com.grtc.dashboard.dispatch.DispatchRepository;
import com.grtc.dashboard.dispatch.DispatchStatus;
import com.grtc.dashboard.operation.OperationEntity;
import com.grtc.dashboard.operation.OperationRepository;
import com.grtc.dashboard.operation.RouteEntity;
import com.grtc.dashboard.operation.RouteRepository;
import com.grtc.dashboard.operation.RouteStatus;
import com.grtc.dashboard.operation.StationEntity;
import com.grtc.dashboard.operation.StationRepository;
import com.grtc.dashboard.operation.TrainDirection;
import com.grtc.dashboard.vehicle.VehicleEntity;
import com.grtc.dashboard.vehicle.VehicleRepository;
import com.grtc.dashboard.vehicle.VehicleStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 개발용 초기 데이터 (app.seed.enabled=true 일 때만, 테이블이 비어 있을 때 한 번만 넣는다)
//  - 노선(광주 도시철도 1호선) + 역 20개, 차량 32편성, 운행 중인 열차 위치, 오늘 배차
//  - 운영 서버에서는 app.seed.enabled 를 false 로 두면 된다.
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class AdminDataSeeder implements ApplicationRunner {

    // 평동(1번) -> 녹동(20번) 순서
    private static final List<String> STATIONS = List.of(
            "평동", "도산", "광주송정역", "송정공원", "공항", "김대중컨벤션센터(마륵)", "상무",
            "운천", "쌍촌", "화정", "농성", "돌고개", "양동시장", "금남로5가", "금남로4가",
            "문화전당(구도청)", "남광주", "학동·증심사입구", "소태", "녹동"
    );

    private final RouteRepository routeRepository;
    private final StationRepository stationRepository;
    private final VehicleRepository vehicleRepository;
    private final OperationRepository operationRepository;
    private final DispatchRepository dispatchRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Map<String, StationEntity> stations = seedRoute();
        Map<String, VehicleEntity> vehicles = seedVehicles();
        seedOperations(stations, vehicles);
        seedDispatches(vehicles);
    }

    private Map<String, StationEntity> seedRoute() {
        Map<String, StationEntity> byName = new HashMap<>();
        if (routeRepository.count() > 0) {
            RouteEntity route = routeRepository.findFirstByOrderByIdAsc().orElseThrow();
            stationRepository.findAllByRouteIdOrderBySeqAsc(route.getId())
                    .forEach(s -> byName.put(s.getName(), s));
            return byName;
        }
        RouteEntity route = routeRepository.save(RouteEntity.builder()
                .name("광주 도시철도 1호선")
                .status(RouteStatus.NORMAL)
                .dailyRunCount(240)
                .build());
        for (int i = 0; i < STATIONS.size(); i++) {
            StationEntity s = stationRepository.save(StationEntity.builder()
                    .route(route).seq(i + 1).name(STATIONS.get(i)).build());
            byName.put(s.getName(), s);
        }
        log.info("[seed] 노선 1개, 역 {}개 등록", STATIONS.size());
        return byName;
    }

    private Map<String, VehicleEntity> seedVehicles() {
        Map<String, VehicleEntity> byNo = new HashMap<>();
        if (vehicleRepository.count() > 0) {
            vehicleRepository.findAll().forEach(v -> byNo.put(v.getVehicleNo(), v));
            return byNo;
        }
        LocalDate base = LocalDate.now().minusDays(10);
        List<VehicleEntity> list = new ArrayList<>();
        for (int i = 0; i < 32; i++) {
            int no = 101 + i;
            VehicleStatus status = switch (no) {
                case 102, 106, 110, 120 -> VehicleStatus.STANDBY;
                case 103, 125 -> VehicleStatus.MAINTENANCE;
                default -> VehicleStatus.RUNNING;
            };
            list.add(VehicleEntity.builder()
                    .vehicleNo(no + "편성")
                    .status(status)
                    .lastInspectionDate(base.minusDays(i))
                    .build());
        }
        vehicleRepository.saveAll(list).forEach(v -> byNo.put(v.getVehicleNo(), v));
        log.info("[seed] 차량 {}편성 등록", list.size());
        return byNo;
    }

    private void seedOperations(Map<String, StationEntity> stations, Map<String, VehicleEntity> vehicles) {
        if (operationRepository.count() > 0) {
            return;
        }
        Object[][] trains = {
                {"101편성", TrainDirection.PYEONGDONG, "광주송정역"},
                {"104편성", TrainDirection.PYEONGDONG, "화정"},
                {"105편성", TrainDirection.SOTAE, "돌고개"},
                {"107편성", TrainDirection.NOKDONG, "남광주"},
                {"108편성", TrainDirection.NOKDONG, "학동·증심사입구"},
        };
        int count = 0;
        for (Object[] t : trains) {
            VehicleEntity vehicle = vehicles.get((String) t[0]);
            StationEntity station = stations.get((String) t[2]);
            if (vehicle == null || station == null) {
                continue;
            }
            operationRepository.save(OperationEntity.builder()
                    .vehicle(vehicle)
                    .direction((TrainDirection) t[1])
                    .currentStation(station)
                    .build());
            count++;
        }
        log.info("[seed] 운행 중인 열차 {}대 등록", count);
    }

    private void seedDispatches(Map<String, VehicleEntity> vehicles) {
        if (dispatchRepository.count() > 0) {
            return;
        }
        LocalDate today = LocalDate.now();
        String prefix = "DISP" + today.format(DateTimeFormatter.ofPattern("yyMMdd")) + "-";
        String[] drivers = {"김민수", "이지훈", "박서연", "장하늘", "최유진", "한지민", "송재민", "전수빈", "정민호", "윤지우"};
        DispatchStatus[] statuses = {
                DispatchStatus.COMPLETED, DispatchStatus.COMPLETED, DispatchStatus.WAITING, DispatchStatus.COMPLETED,
                DispatchStatus.CHANGED, DispatchStatus.COMPLETED, DispatchStatus.COMPLETED, DispatchStatus.WAITING,
                DispatchStatus.COMPLETED, DispatchStatus.CHANGED
        };
        LocalTime start = LocalTime.of(6, 30);
        int count = 0;
        for (int i = 0; i < drivers.length; i++) {
            VehicleEntity vehicle = vehicles.get((101 + i) + "편성");
            if (vehicle == null) {
                continue;
            }
            LocalTime departure = start.plusMinutes(25L * i);
            String remark = switch (statuses[i]) {
                case WAITING -> "운전자 대기";
                case CHANGED -> "도착시간 조정";
                default -> "평일 배차";
            };
            dispatchRepository.save(DispatchEntity.builder()
                    .dispatchNo(prefix + String.format("%03d", i + 1))
                    .dispatchDate(today)
                    .vehicle(vehicle)
                    .driverName(drivers[i])
                    .departureTime(departure)
                    .arrivalTime(departure.plusMinutes(55))
                    .status(statuses[i])
                    .remark(remark)
                    .build());
            count++;
        }
        log.info("[seed] 오늘 배차 {}건 등록", count);
    }
}
