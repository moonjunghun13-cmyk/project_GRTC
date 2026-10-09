package com.grtc.main.admin.dashboard;

import com.grtc.main.admin.operation.OperationDto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

// 대시보드 화면 응답 모음
public final class DashboardDto {

    private DashboardDto() {
    }

    // 차트 막대/진행바 한 줄 (code 는 enum 이름, label 은 화면 표시용)
    public record CountItem(String code, String label, long count) {
    }

    // 차량운행현황 카드 (운행 중 N대 + 운행/대기/정비 막대)
    public record VehicleStatusCard(
            long total,
            long running,
            long standby,
            long maintenance,
            long stopped
    ) {
    }

    // 가동률 카드 (전체 N대 · 운행 N대, 원형 그래프 %)
    public record OperationRateCard(int rate, long total, long running) {
    }

    // 민원건수 카드 (총 N건 + 유형별 막대: 단순/건의/제보/불만)
    public record ComplainCountCard(
            String period,
            String periodLabel,
            long total,
            List<CountItem> byType
    ) {
    }

    // 답변건수 카드 (총 N건 + 처리상태별 진행바: 접수대기/답변중/답변완료/이관안내/철회)
    //  - 철회(WITHDRAWN) 항목의 count 가 기간 안에 접수됐다가 민원인이 철회한 건수다.
    public record AnswerCountCard(
            String period,
            String periodLabel,
            long total,
            List<CountItem> byStatus
    ) {
    }

    // 입·출고 한 건 (다음 출고 / 다음 입고 표시용)
    public record DepotMove(String trainNo, LocalTime time) {
    }

    // 시간대별 입·출고 횟수 (hour 5 = 05시대, 24 = 0시대(그날 운행의 마지막))
    public record HourCount(int hour, long departs, long returns) {
    }

    // 입·출고현황 카드 (오늘 운행일 기준, 입·출고 시간표)
    //  - source: DISPATCH = 배차관리의 입·출고 배차(취소 제외)로 계산, TIMETABLE = 그날 배차가 없어 시간표 그대로 계산
    public record DepotCard(
            LocalDate date,
            String dayType,
            String dayTypeLabel,
            String source,
            long departTotal,      // 오늘 출고 예정 전체
            long returnTotal,      // 오늘 입고 예정 전체
            long departDone,       // 지금까지 출고
            long returnDone,       // 지금까지 입고
            long outNow,           // 지금 본선에 나가 있는 열차 수 (출고 - 입고)
            DepotMove nextDepart,  // 다음 출고 (없으면 null)
            DepotMove nextReturn,  // 다음 입고 (없으면 null)
            List<HourCount> hourly
    ) {
    }

    // 대시보드 화면 전체
    public record Response(
            OperationDto.RouteInfo route,        // 노선운영현황 (노선 정보가 아직 없으면 null)
            VehicleStatusCard vehicles,          // 차량운행현황
            OperationRateCard operationRate,     // 가동률
            ComplainCountCard complaints,        // 민원건수
            AnswerCountCard answers,             // 답변건수
            DepotCard depot                      // 입·출고현황 (시간표가 없으면 null)
    ) {
    }

    // 기간 선택 목록 (이번 달 등)
    public record PeriodOption(String code, String label) {
    }
}
