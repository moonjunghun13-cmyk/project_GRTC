package com.grtc.main.global.init;

import com.grtc.main.admin.dispatch.DispatchRepository;
import com.grtc.main.admin.dispatch.DispatchTimetableGenerator;
import com.grtc.main.admin.operation.OperationEntity;
import com.grtc.main.admin.operation.OperationRepository;
import com.grtc.main.admin.operation.RouteEntity;
import com.grtc.main.admin.operation.RouteRepository;
import com.grtc.main.admin.operation.RouteStatus;
import com.grtc.main.admin.operation.StationEntity;
import com.grtc.main.admin.operation.StationRepository;
import com.grtc.main.admin.operation.TrainDirection;
import com.grtc.main.admin.timetable.ServiceDay;
import com.grtc.main.admin.timetable.TimetableService;
import com.grtc.main.admin.vehicle.VehicleEntity;
import com.grtc.main.admin.vehicle.VehicleRepository;
import com.grtc.main.admin.vehicle.VehicleStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// 개발용 초기 데이터 (app.seed.enabled=true 일 때만, 테이블이 비어 있을 때 한 번만 넣는다)
//  - 노선(광주 도시철도 1호선) + 역 20개, 차량 23편성(101~123편성, 실제 1호선 보유 편성 수), 운행 중인 열차 위치
//  - 입·출고 시간표(평일/토요일/휴일) + 시간표 기준 배차 (오늘 운행일 기준 앞뒤 7일)
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
    private final TimetableService timetableService;
    private final DispatchTimetableGenerator dispatchTimetableGenerator;

    // 시간표 기준 배차를 만들 기간 (오늘 운행일 기준 앞뒤 N일)
    private static final int DISPATCH_DAYS = 7;

    // 시간표 도입 전의 예시 배차(운전자·비고)를 알아보는 값. 이 배차들은 시간표 배차로 바꾼다.
    private static final List<String> OLD_SAMPLE_DRIVERS = List.of(
            "김민수", "이지훈", "박서연", "장하늘", "최유진", "한지민", "송재민", "전수빈", "정민호", "윤지우");
    private static final List<String> OLD_SAMPLE_REMARKS = List.of("평일 배차", "운전자 대기", "도착시간 조정");

    // 광주 도시철도 1호선 전동차: 101~123편성 (23편성, 편성당 4량)
    private static final int FIRST_TRAIN_SET = 101;
    private static final int TRAIN_SET_COUNT = 23;
    private static final int LAST_TRAIN_SET = FIRST_TRAIN_SET + TRAIN_SET_COUNT - 1;
    private static final Pattern TRAIN_SET_NO = Pattern.compile("^(\\d{3})편성$");
    // 초기 데이터로 만든 시간표 배차를 알아보는 비고 문구 (DispatchTimetableGenerator 참고)
    private static final String SEED_DISPATCH_REMARK = "예시 배정";

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Map<String, StationEntity> stations = seedRoute();
        Map<String, VehicleEntity> vehicles = seedVehicles();
        boolean trimmed = trimExtraTrainSets(vehicles);
        seedOperations(stations, vehicles);
        timetableService.loadIfEmpty();
        seedTimetableDispatches(trimmed);
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
        for (int i = 0; i < TRAIN_SET_COUNT; i++) {
            int no = FIRST_TRAIN_SET + i;
            VehicleStatus status = switch (no) {
                case 102, 106, 110, 120 -> VehicleStatus.STANDBY;
                case 103, 119 -> VehicleStatus.MAINTENANCE;
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

    // 예전 초기 데이터(32편성)로 만든 DB를 실제 편성 수(101~123편성)에 맞춘다.
    //  - 124편성 이후의 'NNN편성' 차량만 대상으로 한다. (사용자가 다른 이름으로 등록한 차량은 건드리지 않는다)
    //  - 초기 데이터 시간표 배차(비고에 '예시 배정')는 모두 지우고 23편성으로 다시 만든다.
    //  - 사용자가 직접 만든 배차가 걸린 차량은 지우지 않고 남겨 둔다.
    //  - 지웠으면 true (시간표 배차를 다시 만들어야 함)
    private boolean trimExtraTrainSets(Map<String, VehicleEntity> vehicles) {
        List<VehicleEntity> extras = vehicles.values().stream()
                .filter(v -> {
                    Matcher m = TRAIN_SET_NO.matcher(v.getVehicleNo());
                    return m.matches() && Integer.parseInt(m.group(1)) > LAST_TRAIN_SET;
                })
                .toList();
        if (extras.isEmpty()) {
            return false;
        }

        List<Long> extraIds = extras.stream().map(VehicleEntity::getId).toList();
        Set<Long> keep = dispatchRepository.findAllByVehicleIdIn(extraIds).stream()
                .filter(d -> d.getMoveType() == null || d.getRemark() == null
                        || !d.getRemark().contains(SEED_DISPATCH_REMARK))
                .map(d -> d.getVehicle().getId())
                .collect(Collectors.toSet());

        var seedDispatches = dispatchRepository.findAllByMoveTypeIsNotNullAndRemarkContaining(SEED_DISPATCH_REMARK);
        dispatchRepository.deleteAll(seedDispatches);
        dispatchRepository.flush();

        List<VehicleEntity> removable = extras.stream().filter(v -> !keep.contains(v.getId())).toList();
        Set<Long> removableIds = removable.stream().map(VehicleEntity::getId).collect(Collectors.toSet());
        operationRepository.deleteAll(operationRepository.findAll().stream()
                .filter(op -> removableIds.contains(op.getVehicle().getId()))
                .toList());
        vehicleRepository.deleteAll(removable);
        vehicleRepository.flush();
        removable.forEach(v -> vehicles.remove(v.getVehicleNo()));

        log.info("[seed] 차량 편성 정리: {}편성 삭제, 초기 시간표 배차 {}건 삭제 후 다시 생성 (실제 1호선 {}편성 기준)",
                removable.size(), seedDispatches.size(), TRAIN_SET_COUNT);
        if (!keep.isEmpty()) {
            log.warn("[seed] 직접 등록한 배차가 있어 남겨 둔 편성 {}개가 있습니다. 필요하면 화면에서 정리해 주세요.", keep.size());
        }
        return true;
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

    // 입·출고 시간표로 배차를 만든다.
    //  - 시간표 배차가 하나도 없을 때만 한다. (이미 있으면 사용자가 고친 배차를 건드리지 않는다)
    //  - 예전 예시 배차 10건(시간표 도입 전 초기 데이터)은 지우고 바꾼다.
    private void seedTimetableDispatches(boolean regenerate) {
        if (!regenerate && dispatchRepository.existsByMoveTypeIsNotNull()) {
            return;
        }
        var oldSamples = dispatchRepository.findAllByMoveTypeIsNullAndDriverNameIn(OLD_SAMPLE_DRIVERS).stream()
                .filter(d -> OLD_SAMPLE_REMARKS.contains(d.getRemark()))
                .toList();
        if (!oldSamples.isEmpty()) {
            dispatchRepository.deleteAll(oldSamples);
            dispatchRepository.flush();
            log.info("[seed] 예전 예시 배차 {}건 삭제 (시간표 배차로 교체)", oldSamples.size());
        }

        // 배차할 수 있는 차량(운행중/대기)이 없으면 만들지 않는다. (예외로 앱 시작이 막히지 않게 미리 확인)
        boolean hasVehicle = vehicleRepository.countByStatus(VehicleStatus.RUNNING)
                + vehicleRepository.countByStatus(VehicleStatus.STANDBY) > 0;
        if (!hasVehicle) {
            log.warn("[seed] 배차할 수 있는 차량이 없어 시간표 배차를 만들지 않았습니다.");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDate today = ServiceDay.of(now);
        int count = 0;
        for (int i = -DISPATCH_DAYS; i <= DISPATCH_DAYS; i++) {
            LocalDate date = today.plusDays(i);
            if (dispatchRepository.existsByDispatchDateAndMoveTypeIsNotNull(date)) {
                continue;
            }
            count += dispatchRepository.saveAll(dispatchTimetableGenerator.build(date, now)).size();
        }
        log.info("[seed] 입·출고 시간표 배차 {}건 등록 ({} ~ {})", count,
                today.minusDays(DISPATCH_DAYS), today.plusDays(DISPATCH_DAYS));
    }
}
