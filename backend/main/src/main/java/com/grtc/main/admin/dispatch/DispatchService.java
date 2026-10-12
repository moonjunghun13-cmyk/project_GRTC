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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DispatchService {

    public static final Sort DEFAULT_SORT = Sort.by(Sort.Order.desc("dispatchDate"), Sort.Order.asc("dispatchNo"));
    public static final Set<String> SORTABLE = Set.of(
            "id", "dispatchNo", "dispatchDate", "driverName", "departureTime", "arrivalTime", "status", "createdAt");

    private final DispatchRepository dispatchRepository;
    private final DispatchNoGenerator dispatchNoGenerator;
    private final VehicleRepository vehicleRepository;

    public PageResponse<DispatchDto.Response> list(String keyword, LocalDate date, Long vehicleId, String driver,
                                                   DispatchStatus status, Pageable pageable) {
        Page<DispatchEntity> result = dispatchRepository.findAll(
                spec(keyword, date, vehicleId, driver, status), pageable);
        return PageResponse.from(result, DispatchDto.Response::from);
    }

    public DispatchDto.Summary summary(String keyword, LocalDate date, Long vehicleId, String driver) {
        return new DispatchDto.Summary(
                dispatchRepository.count(spec(keyword, date, vehicleId, driver, null)),
                dispatchRepository.count(spec(keyword, date, vehicleId, driver, DispatchStatus.COMPLETED)),
                dispatchRepository.count(spec(keyword, date, vehicleId, driver, DispatchStatus.WAITING)),
                dispatchRepository.count(spec(keyword, date, vehicleId, driver, DispatchStatus.CHANGED)),
                dispatchRepository.count(spec(keyword, date, vehicleId, driver, DispatchStatus.CANCELLED))
        );
    }

    public DispatchDto.Response get(Long id) {
        return DispatchDto.Response.from(find(id));
    }

    @Transactional
    public DispatchDto.Response create(DispatchDto.Request request) {
        validateTime(request.departureTime(), request.arrivalTime());
        VehicleEntity vehicle = findVehicle(request.vehicleId());
        checkAvailable(vehicle);
        checkOverlap(vehicle.getId(), request.dispatchDate(),
                request.departureTime(), request.arrivalTime(), -1L);

        String dispatchNo = dispatchNoGenerator.next(request.dispatchDate());

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

    @Transactional
    public DispatchDto.Response update(Long id, DispatchDto.UpdateRequest request) {
        DispatchEntity dispatch = find(id);
        if (dispatch.getStatus() == DispatchStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.DISPATCH_CANCELLED);
        }

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

    @Transactional
    public DispatchDto.Response cancel(Long id) {
        DispatchEntity dispatch = find(id);
        dispatch.cancel();
        log.info("[dispatch] 취소 id={}", id);
        return DispatchDto.Response.from(dispatch);
    }

    @Transactional
    public void delete(Long id) {
        DispatchEntity dispatch = find(id);
        dispatchRepository.delete(dispatch);
        log.info("[dispatch] 삭제 id={} dispatchNo={}", id, dispatch.getDispatchNo());
    }

    public DispatchDto.Options options() {
        List<DispatchDto.VehicleOption> vehicles = vehicleRepository.findAll(Sort.by("vehicleNo")).stream()
                .map(v -> new DispatchDto.VehicleOption(v.getId(), v.getVehicleNo()))
                .toList();
        return new DispatchDto.Options(vehicles, dispatchRepository.findDistinctDriverNames());
    }

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

    private void validateTime(LocalTime departure, LocalTime arrival) {
        if (!arrival.isAfter(departure)) {
            throw new BusinessException(ErrorCode.INVALID_DISPATCH_TIME);
        }
    }

    private void checkAvailable(VehicleEntity vehicle) {
        if (vehicle.getStatus() == VehicleStatus.MAINTENANCE || vehicle.getStatus() == VehicleStatus.STOPPED) {
            throw new BusinessException(ErrorCode.VEHICLE_NOT_AVAILABLE);
        }
    }

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
