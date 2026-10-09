package com.grtc.main.admin.dispatch;

import com.grtc.main.admin.timetable.DayType;
import com.grtc.main.admin.timetable.MoveType;
import com.grtc.main.admin.timetable.ServiceDay;
import com.grtc.main.admin.timetable.TimetableEntity;
import com.grtc.main.admin.timetable.TimetableRepository;
import com.grtc.main.admin.vehicle.VehicleEntity;
import com.grtc.main.admin.vehicle.VehicleRepository;
import com.grtc.main.admin.vehicle.VehicleStatus;
import com.grtc.main.global.exception.BusinessException;
import com.grtc.main.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;

// 입·출고 시간표로 하루치 배차(출고 1회 = 배차 1건, 입고 1회 = 배차 1건)를 만든다.
//  - 시간표에는 열번과 시각만 있으므로 차량·운전자는 아래 규칙으로 '예시 배정'한다.
//      * 출고: 차량기지에 가장 오래 대기한 차량(운행중/대기 상태만)을 내보낸다.
//      * 입고: 가장 먼저 나간 차량이 들어온다고 보고, 그 차량·운전자를 그대로 쓴다.
//  - 0~2시대 시각(예: 0:05)은 그날 운행의 마지막 열차이므로 다음 날 새벽으로 계산한다.
//  - 이미 지난 시각은 '배차 완료', 아직 오지 않은 시각은 '배차 대기'로 만든다.
//  - 저장은 부르는 쪽에서 한다. (배차번호 잠금 때문에 반드시 트랜잭션 안에서 부른다)
@Component
@RequiredArgsConstructor
public class DispatchTimetableGenerator {

    // 예시 운전자 (실제 운전자 정보가 생기면 그 목록으로 바꾼다)
    static final List<String> DRIVERS = List.of(
            "김민수", "이지훈", "박서연", "장하늘", "최유진", "한지민", "송재민", "전수빈", "정민호", "윤지우",
            "강도현", "조예린", "임태윤", "오세라", "신동욱", "문가은", "배준서", "황보영", "서지안", "노현우");

    private final TimetableRepository timetableRepository;
    private final VehicleRepository vehicleRepository;
    private final DispatchNoGenerator dispatchNoGenerator;

    private record Event(TimetableEntity row, int minute) {
    }

    private record Trip(VehicleEntity vehicle, String driver, String trainNo, LocalTime time) {
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public List<DispatchEntity> build(LocalDate date, LocalDateTime now) {
        DayType dayType = DayType.of(date);
        List<Event> events = timetableRepository.findAllByDayTypeOrderBySeqAsc(dayType).stream()
                .map(r -> new Event(r, ServiceDay.minute(r.getScheduledTime())))
                // 시각 순. 같은 시각이면 입고를 먼저 처리해 들어온 차량이 바로 다시 나갈 수 있게 한다.
                .sorted(Comparator.comparingInt(Event::minute)
                        .thenComparing(e -> e.row().getMoveType() == MoveType.DEPART)
                        .thenComparingInt(e -> e.row().getSeq()))
                .toList();
        if (events.isEmpty()) {
            throw new BusinessException(ErrorCode.TIMETABLE_NOT_FOUND);
        }

        List<VehicleEntity> vehicles = vehicleRepository.findAll(Sort.by("vehicleNo")).stream()
                .filter(v -> v.getStatus() == VehicleStatus.RUNNING || v.getStatus() == VehicleStatus.STANDBY)
                .toList();
        if (vehicles.isEmpty()) {
            throw new BusinessException(ErrorCode.VEHICLE_NOT_AVAILABLE);
        }

        Deque<VehicleEntity> depot = new ArrayDeque<>(vehicles);
        Deque<Trip> out = new ArrayDeque<>();
        int driverIndex = 0;

        // 배차번호: 첫 번호만 잠금을 걸고 받은 뒤 1씩 늘린다. (예: DISP261009-001)
        String first = dispatchNoGenerator.next(date);
        String prefix = first.substring(0, first.lastIndexOf('-') + 1);
        int sequence = Integer.parseInt(first.substring(first.lastIndexOf('-') + 1));

        List<DispatchEntity> result = new ArrayList<>();
        for (Event event : events) {
            TimetableEntity row = event.row();
            LocalTime time = row.getScheduledTime();
            VehicleEntity vehicle;
            String driver;
            String remark;

            if (row.getMoveType() == MoveType.DEPART) {
                vehicle = depot.isEmpty() ? vehicles.get(result.size() % vehicles.size()) : depot.pollFirst();
                driver = DRIVERS.get(driverIndex++ % DRIVERS.size());
                out.addLast(new Trip(vehicle, driver, row.getTrainNo(), time));
                remark = dayType.getLabel() + " 시간표 출고 (차량·운전자는 예시 배정)";
            } else {
                Trip trip = out.pollFirst();
                if (trip != null) {
                    vehicle = trip.vehicle();
                    driver = trip.driver();
                    remark = dayType.getLabel() + " 시간표 입고 (" + trip.trainNo() + "열차 "
                            + trip.time() + " 출고분, 예시 배정)";
                } else {
                    vehicle = depot.isEmpty() ? vehicles.get(0) : depot.pollFirst();
                    driver = DRIVERS.get(driverIndex++ % DRIVERS.size());
                    remark = dayType.getLabel() + " 시간표 입고 (차량·운전자는 예시 배정)";
                }
                depot.addLast(vehicle);
            }

            LocalDateTime at = ServiceDay.at(date, time);
            result.add(DispatchEntity.builder()
                    .dispatchNo(prefix + String.format("%03d", sequence++))
                    .dispatchDate(date)
                    .vehicle(vehicle)
                    .driverName(driver)
                    .departureTime(time)
                    .arrivalTime(time)
                    .moveType(row.getMoveType())
                    .trainNo(row.getTrainNo())
                    .dayType(dayType)
                    .status(at.isAfter(now) ? DispatchStatus.WAITING : DispatchStatus.COMPLETED)
                    .remark(remark)
                    .build());
        }
        return result;
    }
}
