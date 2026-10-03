package com.grtc.main.admin.vehicle;

import com.grtc.main.admin.dispatch.DispatchRepository;
import com.grtc.main.global.common.PageResponse;
import com.grtc.main.global.common.SearchUtil;
import com.grtc.main.global.exception.BusinessException;
import com.grtc.main.global.exception.ErrorCode;
import com.grtc.main.admin.operation.OperationRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

// 차량관리(조회/검색/등록/수정/삭제) 비즈니스 로직
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VehicleService {

    // 목록 정렬: sort 를 보내지 않으면 화면과 같이 차량번호 순
    public static final Sort DEFAULT_SORT = Sort.by("vehicleNo");
    public static final Set<String> SORTABLE = Set.of("id", "vehicleNo", "status", "lastInspectionDate");

    private final VehicleRepository vehicleRepository;
    private final DispatchRepository dispatchRepository;
    private final OperationRepository operationRepository;

    // 차량 목록: 차량번호 검색 + 상태 필터 + 페이징
    public PageResponse<VehicleDto.Response> list(String keyword, VehicleStatus status, Pageable pageable) {
        Specification<VehicleEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (SearchUtil.hasText(keyword)) {
                predicates.add(cb.like(cb.lower(root.<String>get("vehicleNo")),
                        SearchUtil.contains(keyword), SearchUtil.ESCAPE));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<VehicleEntity> result = vehicleRepository.findAll(spec, pageable);
        return PageResponse.from(result, VehicleDto.Response::from);
    }

    // 상태별 차량 수 (차량관리 요약 카드, 대시보드 차량운행현황/가동률에서 같이 사용)
    public VehicleDto.Summary summary() {
        long total = vehicleRepository.count();
        long running = vehicleRepository.countByStatus(VehicleStatus.RUNNING);
        long standby = vehicleRepository.countByStatus(VehicleStatus.STANDBY);
        long maintenance = vehicleRepository.countByStatus(VehicleStatus.MAINTENANCE);
        long stopped = vehicleRepository.countByStatus(VehicleStatus.STOPPED);
        int rate = total == 0 ? 0 : (int) Math.round(running * 100.0 / total);
        return new VehicleDto.Summary(total, running, standby, maintenance, stopped, rate);
    }

    // 차량 상세보기
    public VehicleDto.Response get(Long id) {
        return VehicleDto.Response.from(find(id));
    }

    // 차량 등록
    @Transactional
    public VehicleDto.Response create(VehicleDto.Request request) {
        String vehicleNo = request.vehicleNo().trim();
        if (vehicleRepository.existsByVehicleNo(vehicleNo)) {
            throw new BusinessException(ErrorCode.DUPLICATE_VEHICLE_NO);
        }
        VehicleEntity saved = vehicleRepository.save(VehicleEntity.builder()
                .vehicleNo(vehicleNo)
                .status(request.status())
                .lastInspectionDate(request.lastInspectionDate())
                .build());
        log.info("[vehicle] 등록 id={} vehicleNo={}", saved.getId(), vehicleNo);
        return VehicleDto.Response.from(saved);
    }

    // 차량 수정 (PATCH: 요청에 들어 있는 항목만 바꾼다)
    @Transactional
    public VehicleDto.Response update(Long id, VehicleDto.UpdateRequest request) {
        VehicleEntity vehicle = find(id);

        String vehicleNo = request.vehicleNo() != null ? request.vehicleNo().trim() : vehicle.getVehicleNo();
        if (vehicleRepository.existsByVehicleNoAndIdNot(vehicleNo, id)) {
            throw new BusinessException(ErrorCode.DUPLICATE_VEHICLE_NO);
        }
        VehicleStatus status = request.status() != null ? request.status() : vehicle.getStatus();
        LocalDate lastInspectionDate = request.lastInspectionDate() != null
                ? request.lastInspectionDate()
                : vehicle.getLastInspectionDate();

        vehicle.update(vehicleNo, status, lastInspectionDate);
        log.info("[vehicle] 수정 id={}", id);
        return VehicleDto.Response.from(vehicle);
    }

    // 차량 삭제 (배차/운행 이력이 있으면 막는다)
    @Transactional
    public void delete(Long id) {
        VehicleEntity vehicle = find(id);
        if (dispatchRepository.existsByVehicleId(id) || operationRepository.existsByVehicleId(id)) {
            throw new BusinessException(ErrorCode.VEHICLE_IN_USE);
        }
        vehicleRepository.delete(vehicle);
        log.info("[vehicle] 삭제 id={}", id);
    }

    private VehicleEntity find(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.VEHICLE_NOT_FOUND));
    }
}
