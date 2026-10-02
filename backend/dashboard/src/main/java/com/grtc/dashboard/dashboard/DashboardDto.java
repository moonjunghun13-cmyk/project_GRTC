package com.grtc.dashboard.dashboard;

import com.grtc.dashboard.operation.OperationDto;

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

    // 답변건수 카드 (총 N건 + 처리상태별 진행바: 접수대기/답변중/답변완료/이관안내)
    public record AnswerCountCard(
            String period,
            String periodLabel,
            long total,
            List<CountItem> byStatus
    ) {
    }

    // 대시보드 화면 전체
    public record Response(
            OperationDto.RouteInfo route,        // 노선운영현황 (노선 정보가 아직 없으면 null)
            VehicleStatusCard vehicles,          // 차량운행현황
            OperationRateCard operationRate,     // 가동률
            ComplainCountCard complaints,        // 민원건수
            AnswerCountCard answers              // 답변건수
    ) {
    }

    // 기간 선택 목록 (이번 달 등)
    public record PeriodOption(String code, String label) {
    }
}
