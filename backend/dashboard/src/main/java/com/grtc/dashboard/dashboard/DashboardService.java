package com.grtc.dashboard.dashboard;

import com.grtc.dashboard.complain.ComplainRepository;
import com.grtc.dashboard.complain.ComplainStatus;
import com.grtc.dashboard.complain.ComplainType;
import com.grtc.dashboard.operation.OperationDto;
import com.grtc.dashboard.operation.OperationService;
import com.grtc.dashboard.operation.RouteRepository;
import com.grtc.dashboard.vehicle.VehicleDto;
import com.grtc.dashboard.vehicle.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

// 대시보드 통계 조회 (노선운영현황, 차량운행현황, 가동률, 민원건수, 답변건수)
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final OperationService operationService;
    private final VehicleService vehicleService;
    private final ComplainRepository complainRepository;
    private final RouteRepository routeRepository;

    public DashboardDto.Response get(DashboardPeriod complainPeriod, DashboardPeriod answerPeriod) {
        VehicleDto.Summary v = vehicleService.summary();

        return new DashboardDto.Response(
                routeOrNull(),
                new DashboardDto.VehicleStatusCard(v.total(), v.running(), v.standby(), v.maintenance(), v.stopped()),
                new DashboardDto.OperationRateCard(v.operationRate(), v.total(), v.running()),
                complainCount(complainPeriod),
                answerCount(answerPeriod)
        );
    }

    public List<DashboardDto.PeriodOption> periods() {
        return Arrays.stream(DashboardPeriod.values())
                .map(p -> new DashboardDto.PeriodOption(p.name(), p.getLabel()))
                .toList();
    }

    // 민원건수: 기간 안에 접수된 민원의 유형별 건수
    private DashboardDto.ComplainCountCard complainCount(DashboardPeriod period) {
        LocalDateTime from = period.from(LocalDate.now());
        List<DashboardDto.CountItem> byType = Arrays.stream(ComplainType.values())
                .map(t -> new DashboardDto.CountItem(t.name(), t.getLabel(),
                        complainRepository.countByTypeAndCreatedAtGreaterThanEqual(t, from)))
                .toList();
        return new DashboardDto.ComplainCountCard(period.name(), period.getLabel(),
                complainRepository.countByCreatedAtGreaterThanEqual(from), byType);
    }

    // 답변건수: 기간 안에 접수된 민원의 처리상태별 건수
    private DashboardDto.AnswerCountCard answerCount(DashboardPeriod period) {
        LocalDateTime from = period.from(LocalDate.now());
        List<DashboardDto.CountItem> byStatus = Arrays.stream(ComplainStatus.values())
                .map(s -> new DashboardDto.CountItem(s.name(), s.getLabel(),
                        complainRepository.countByStatusAndCreatedAtGreaterThanEqual(s, from)))
                .toList();
        return new DashboardDto.AnswerCountCard(period.name(), period.getLabel(),
                complainRepository.countByCreatedAtGreaterThanEqual(from), byStatus);
    }

    // 노선 데이터가 아직 없어도 대시보드의 나머지 카드는 보여준다.
    private OperationDto.RouteInfo routeOrNull() {
        // (예외로 처리하면 트랜잭션이 rollback-only 로 표시되므로 먼저 존재 여부를 확인한다)
        return routeRepository.count() == 0 ? null : operationService.routeInfo();
    }
}
