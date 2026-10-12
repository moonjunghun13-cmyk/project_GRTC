package com.grtc.main.admin.dashboard;

import com.grtc.main.qna.complain.ComplainRepository;
import com.grtc.main.qna.complain.ComplainStatus;
import com.grtc.main.qna.complain.ComplainType;
import com.grtc.main.admin.operation.OperationDto;
import com.grtc.main.admin.operation.OperationService;
import com.grtc.main.admin.operation.RouteRepository;
import com.grtc.main.admin.vehicle.VehicleDto;
import com.grtc.main.admin.vehicle.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

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

    private DashboardDto.ComplainCountCard complainCount(DashboardPeriod period) {
        LocalDateTime from = period.from(LocalDate.now());
        List<DashboardDto.CountItem> byType = Arrays.stream(ComplainType.values())
                .map(t -> new DashboardDto.CountItem(t.name(), t.getLabel(),
                        complainRepository.countByTypeAndCreatedAtGreaterThanEqual(t, from)))
                .toList();
        return new DashboardDto.ComplainCountCard(period.name(), period.getLabel(),
                complainRepository.countByCreatedAtGreaterThanEqual(from), byType);
    }

    private DashboardDto.AnswerCountCard answerCount(DashboardPeriod period) {
        LocalDateTime from = period.from(LocalDate.now());
        List<DashboardDto.CountItem> byStatus = Arrays.stream(ComplainStatus.values())
                .map(s -> new DashboardDto.CountItem(s.name(), s.getLabel(),
                        complainRepository.countByStatusAndCreatedAtGreaterThanEqual(s, from)))
                .toList();
        return new DashboardDto.AnswerCountCard(period.name(), period.getLabel(),
                complainRepository.countByCreatedAtGreaterThanEqual(from), byStatus);
    }

    private OperationDto.RouteInfo routeOrNull() {
        return routeRepository.count() == 0 ? null : operationService.routeInfo();
    }
}
