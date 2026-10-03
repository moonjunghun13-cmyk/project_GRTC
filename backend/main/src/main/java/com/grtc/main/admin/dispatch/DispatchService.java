package com.grtc.main.admin.dispatch;

import com.grtc.main.global.common.PageResponse;
import com.grtc.main.global.common.SearchUtil;
import com.grtc.main.global.exception.BusinessException;
import com.grtc.main.global.exception.ErrorCode;
import com.grtc.main.admin.vehicle.VehicleEntity;
import com.grtc.main.admin.vehicle.VehicleRepository;
import com.grtc.main.admin.vehicle.VehicleStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

// 배차관리(조회/검색/등록/수정/취소) 비즈니스 로직
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DispatchService {

    // 목록 정렬: sort 를 보내지 않으면 화면과 같이 최근 배차일 먼저, 같은 날은 배차번호 순
    public static final Sort DEFAULT_SORT = Sort.by(Sort.Order.desc("dispatchDate"), Sort.Order.asc("dispatchNo"));
    public static final Set<String> SORTABLE = Set.of(
            "id", "dispatchNo", "dispatchDate", "driverName", "departureTime", "arrivalTime", "status", "createdAt");

    private static final DateTimeFormatter NO_DATE = DateTimeFormatter.ofPattern("yyMMdd");

    private final DispatchRepository dispatchRepository;
    private final VehicleRepository vehicleRepository;

    // 배차 목록: 검색어(배차번호/차량번호/운전자명) + 배차일자/차량/운전자/상태 필터 + 페이징
    public PageResponse<DispatchDto.Response> list(String keyword, LocalDate date, Long vehicleId, String driver,
                                                   DispatchStatus status, Pageable pageable) {
        Page<DispatchEntity> result = dispatchRepository.findAll(
                spec(keyword, date, vehicleId, driver, status), pageable);
        return PageResponse.from(result, DispatchDto.Response::from);
    }

    // 상단 요약 카드: 상태 필터를 제외한 검색 조건(검색어, 배차일자, 차량, 운전자) 기준으로 센다.
    public DispatchDto.Summary summary(String keyword, LocalDate date, Long vehicleId, String driver) {
        return new DispatchDto.Summary(
                dispatchRepository.count(spec(keyword, date, vehicleId, driver, null)),
                dispatchRepository.count(spec(keyword, date, vehicleId, driver, DispatchStatus.COMPLETED)),
                dispatchRepository.count(spec(keyword, date, vehicleId, driver, DispatchStatus.WAITING)),
                dispatchRepository.count(spec(keyword, date, vehicleId, driver, DispatchStatus.CHANGED)),
                dispatchRepository.count(spec(keyword, date, vehicleId, driver, DispatchStatus.CANCELLED))
        );
    }

    // 배차 상세보기
    public DispatchDto.Response get(Long id) {
        return DispatchDto.Response.from(find(id));
    }

    // 배차 등록: 시간 순서/차량 상태/차량 시간 겹침을 검사하고, 배차번호를 만들어 '배차 대기'로 저장
    @Transactional
    public DispatchDto.Response create(DispatchDto.Request request) {
        validateTime(request.departureTime(), request.arrivalTime());
        VehicleEntity vehicle = findVehicle(request.vehicleId());
        checkAvailable(vehicle);
        checkOverlap(vehicle.getId(), request.dispatchDate(),
                request.departureTime(), request.arrivalTime(), -1L);

        // 배차번호: DISP + 배차일(yyMMdd) + - + 그날의 일련번호(3자리)
        long sequence = dispatchRepository.countByDispatchDate(request.dispatchDate()) + 1;
        String dispatchNo = "DISP" + request.dispatchDate().format(NO_DATE) + "-" + String.format("%03d", sequence);

        DispatchEntity saved = dispatchRepository.save(DispatchEntity.builder()
                .dispatchNo(dispatchNo)
                .dispatchDate(request.dispatchDate())
                .vehicle(vehicle)
                .driverName(request.driverName().trim())
                .departureTime(request.departureTime())
                .arrivalTime(request.arrivalTime())
                .remark(blankToNull(request.remark()))
                .status(DispatchStatus.WAITING)
                .build());
        log.info("[dispatch] 등록 id={} dispatchNo={}", saved.getId(), dispatchNo);
        return DispatchDto.Response.from(saved);
    }

    // 배차 수정 (PATCH: 요청에 들어 있는 항목만 바꾼다)
    //  - 취소된 배차는 수정 불가
    //  - 상태를 직접 주지 않았는데 차량/날짜/시간이 바뀌면 '배차 변경'으로 처리
    @Transactional
    public DispatchDto.Response update(Long id, DispatchDto.UpdateRequest request) {
        DispatchEntity dispatch = find(id);
        if (dispatch.getStatus() == DispatchStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.DISPATCH_CANCELLED);
        }

        // 보내지 않은 항목은 기존 값을 그대로 쓴다.
        LocalDate dispatchDate = request.dispatchDate() != null ? request.dispatchDate() : dispatch.getDispatchDate();
        LocalTime departureTime = request.departureTime() != null ? request.departureTime() : dispatch.getDepartureTime();
        LocalTime arrivalTime = request.arrivalTime() != null ? request.arrivalTime() : dispatch.getArrivalTime();
        String driverName = request.driverName() != null ? request.driverName().trim() : dispatch.getDriverName();
        String remark = request.remark() != null ? blankToNull(request.remark()) : dispatch.getRemark();

        boolean vehicleChanged = request.vehicleId() != null
                && !request.vehicleId().equals(dispatch.getVehicle().getId());
        VehicleEntity vehicle = vehicleChanged ? findVehicle(request.vehicleId()) : dispatch.getVehicle();

        validateTime(departureTime, arrivalTime);
        if (vehicleChanged) {
            checkAvailable(vehicle);
        }
        checkOverlap(vehicle.getId(), dispatchDate, departureTime, arrivalTime, id);

        boolean changed = vehicleChanged
                || !dispatch.getDispatchDate().equals(dispatchDate)
                || !dispatch.getDepartureTime().equals(departureTime)
                || !dispatch.getArrivalTime().equals(arrivalTime);

        DispatchStatus status = request.status() != null
                ? request.status()
                : (changed ? DispatchStatus.CHANGED : dispatch.getStatus());

        dispatch.update(vehicle, dispatchDate, driverName, departureTime, arrivalTime, remark, status);
        log.info("[dispatch] 수정 id={} status={}", id, status);
        return DispatchDto.Response.from(dispatch);
    }

    // 배차 취소 (기록은 남기고 상태만 '배차 취소'로 바꾼다)
    @Transactional
    public DispatchDto.Response cancel(Long id) {
        DispatchEntity dispatch = find(id);
        dispatch.cancel();
        log.info("[dispatch] 취소 id={}", id);
        return DispatchDto.Response.from(dispatch);
    }

    // 검색 필터/등록 폼의 차량·운전자 선택 목록
    public DispatchDto.Options options() {
        List<DispatchDto.VehicleOption> vehicles = vehicleRepository.findAll(Sort.by("vehicleNo")).stream()
                .map(v -> new DispatchDto.VehicleOption(v.getId(), v.getVehicleNo()))
                .toList();
        return new DispatchDto.Options(vehicles, dispatchRepository.findDistinctDriverNames());
    }

    // ---------- 내부 도우미 ----------

    // 검색 조건 조립: 검색어(배차번호/차량번호/운전자명), 배차일자, 차량, 운전자, 상태
    private Specification<DispatchEntity> spec(String keyword, LocalDate date, Long vehicleId,
                                               String driver, DispatchStatus status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<DispatchEntity, VehicleEntity> vehicle = root.join("vehicle");

            if (SearchUtil.hasText(keyword)) {
                String like = SearchUtil.contains(keyword);
                predicates.add(cb.or(
                        cb.like(cb.lower(root.<String>get("dispatchNo")), like, SearchUtil.ESCAPE),
                        cb.like(cb.lower(vehicle.<String>get("vehicleNo")), like, SearchUtil.ESCAPE),
                        cb.like(cb.lower(root.<String>get("driverName")), like, SearchUtil.ESCAPE)
                ));
            }
            if (date != null) {
                predicates.add(cb.equal(root.get("dispatchDate"), date));
            }
            if (vehicleId != null) {
                predicates.add(cb.equal(vehicle.get("id"), vehicleId));
            }
            if (SearchUtil.hasText(driver)) {
                predicates.add(cb.equal(root.get("driverName"), driver.trim()));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    // 도착시간은 출발시간보다 늦어야 한다 (자정을 넘기는 배차는 지원하지 않음)
    private void validateTime(LocalTime departure, LocalTime arrival) {
        if (!arrival.isAfter(departure)) {
            throw new BusinessException(ErrorCode.INVALID_DISPATCH_TIME);
        }
    }

    // 정비 중이거나 운행정지된 차량은 배차할 수 없다 (명세서 3-2 TRAINSET_NOT_AVAILABLE)
    private void checkAvailable(VehicleEntity vehicle) {
        if (vehicle.getStatus() == VehicleStatus.MAINTENANCE || vehicle.getStatus() == VehicleStatus.STOPPED) {
            throw new BusinessException(ErrorCode.VEHICLE_NOT_AVAILABLE);
        }
    }

    // 같은 차량이 같은 날 겹치는 시간에 두 번 배차되지 않도록 검사 (명세서 3-2 SCHEDULE_CONFLICT)
    private void checkOverlap(Long vehicleId, LocalDate date, LocalTime departure, LocalTime arrival,
                              Long excludeId) {
        long overlaps = dispatchRepository.countOverlap(
                vehicleId, date, departure, arrival, DispatchStatus.CANCELLED, excludeId);
        if (overlaps > 0) {
            throw new BusinessException(ErrorCode.DISPATCH_TIME_CONFLICT);
        }
    }

    private DispatchEntity find(Long id) {
        return dispatchRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.DISPATCH_NOT_FOUND));
    }

    private VehicleEntity findVehicle(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.VEHICLE_NOT_FOUND));
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
