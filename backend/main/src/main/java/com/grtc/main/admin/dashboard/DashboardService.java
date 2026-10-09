package com.grtc.main.admin.dashboard;

import com.grtc.main.qna.complain.ComplainRepository;
import com.grtc.main.qna.complain.ComplainStatus;
import com.grtc.main.qna.complain.ComplainType;
import com.grtc.main.admin.dispatch.DispatchRepository;
import com.grtc.main.admin.dispatch.DispatchStatus;
import com.grtc.main.admin.operation.OperationDto;
import com.grtc.main.admin.operation.OperationService;
import com.grtc.main.admin.operation.RouteRepository;
import com.grtc.main.admin.timetable.DayType;
import com.grtc.main.admin.timetable.MoveType;
import com.grtc.main.admin.timetable.ServiceDay;
import com.grtc.main.admin.timetable.TimetableRepository;
import com.grtc.main.admin.vehicle.VehicleDto;
import com.grtc.main.admin.vehicle.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// 대시보드 통계 조회 (노선운영현황, 차량운행현황, 가동률, 민원건수, 답변건수, 입·출고현황)
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final OperationService operationService;
    private final VehicleService vehicleService;
    private final ComplainRepository complainRepository;
    private final RouteRepository routeRepository;
    private final DispatchRepository dispatchRepository;
    private final TimetableRepository timetableRepository;

    public DashboardDto.Response get(DashboardPeriod complainPeriod, DashboardPeriod answerPeriod) {
        VehicleDto.Summary v = vehicleService.summary();

        return new DashboardDto.Response(
                routeOrNull(),
                new DashboardDto.VehicleStatusCard(v.total(), v.running(), v.standby(), v.maintenance(), v.stopped()),
                new DashboardDto.OperationRateCard(v.operationRate(), v.total(), v.running()),
                complainCount(complainPeriod),
                answerCount(answerPeriod),
                depot(LocalDateTime.now())
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

    // ---------- 입·출고현황 ----------

    // 입·출고 한 건 (계산용)
    private record Move(MoveType type, String trainNo, LocalTime time, int minute) {
    }

    // 오늘 운행일(새벽 3시 기준)의 입·출고 현황
    //  - 배차관리에 그날 입·출고 배차가 있으면 그것(취소 제외)으로, 없으면 입·출고 시간표로 계산한다.
    DashboardDto.DepotCard depot(LocalDateTime now) {
        LocalDate date = ServiceDay.of(now);
        DayType dayType = DayType.of(date);

        String source = "DISPATCH";
        List<Move> moves = dispatchRepository
                .findAllByDispatchDateAndMoveTypeIsNotNullAndStatusNotOrderByDepartureTimeAsc(date, DispatchStatus.CANCELLED)
                .stream()
                .map(d -> new Move(d.getMoveType(), d.getTrainNo(), d.getDepartureTime(),
                        ServiceDay.minute(d.getDepartureTime())))
                .toList();
        if (moves.isEmpty()) {
            source = "TIMETABLE";
            moves = timetableRepository.findAllByDayTypeOrderBySeqAsc(dayType).stream()
                    .map(t -> new Move(t.getMoveType(), t.getTrainNo(), t.getScheduledTime(),
                            ServiceDay.minute(t.getScheduledTime())))
                    .toList();
        }
        if (moves.isEmpty()) {
            return null; // 시간표가 아직 없음
        }
        moves = moves.stream().sorted((a, b) -> Integer.compare(a.minute(), b.minute())).toList();

        // 지금 시각(운행일 기준 분). 운행일이 어제로 잡혔으면(새벽 0~2시) 24시 이후로 센다.
        int nowMinute = date.equals(now.toLocalDate())
                ? now.getHour() * 60 + now.getMinute()
                : now.getHour() * 60 + now.getMinute() + 24 * 60;

        long departTotal = 0, returnTotal = 0, departDone = 0, returnDone = 0;
        DashboardDto.DepotMove nextDepart = null, nextReturn = null;
        long[] departsByHour = new long[25];
        long[] returnsByHour = new long[25];
        for (Move m : moves) {
            boolean done = m.minute() <= nowMinute;
            int hour = Math.min(24, m.minute() / 60);
            if (m.type() == MoveType.DEPART) {
                departTotal++;
                departsByHour[hour]++;
                if (done) {
                    departDone++;
                } else if (nextDepart == null) {
                    nextDepart = new DashboardDto.DepotMove(m.trainNo(), m.time());
                }
            } else {
                returnTotal++;
                returnsByHour[hour]++;
                if (done) {
                    returnDone++;
                } else if (nextReturn == null) {
                    nextReturn = new DashboardDto.DepotMove(m.trainNo(), m.time());
                }
            }
        }

        // 05시대 ~ 0시대(24)
        List<DashboardDto.HourCount> hourly = new ArrayList<>();
        for (int h = 5; h <= 24; h++) {
            hourly.add(new DashboardDto.HourCount(h, departsByHour[h], returnsByHour[h]));
        }

        return new DashboardDto.DepotCard(date, dayType.name(), dayType.getLabel(), source,
                departTotal, returnTotal, departDone, returnDone, Math.max(0, departDone - returnDone),
                nextDepart, nextReturn, hourly);
    }
}
