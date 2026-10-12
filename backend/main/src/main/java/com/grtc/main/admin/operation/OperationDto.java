package com.grtc.main.admin.operation;

import java.util.List;

// 운행관리(노선도) / 대시보드 노선운영현황에서 쓰는 응답 모음
public final class OperationDto {

    private OperationDto() {
    }

    // 노선 요약 (대시보드 노선운영현황 카드)
    public record RouteInfo(
            Long id,
            String name,
            RouteStatus status,
            String statusLabel,
            int stationCount,
            int dailyRunCount,
            String firstStation,
            String lastStation
    ) {
    }

    // 노선도에 그릴 역 (seq 순서대로)
    public record StationInfo(Long id, int seq, String name) {
    }

    public record TrainInfo(
            Long operationId,
            String vehicleNo,
            TrainDirection direction,
            String directionLabel,
            Long currentStationId,
            String currentStation,
            String nextStation,
            String heading
    ) {
    }

    // 운행관리 화면 전체: 노선 + 역 목록 + 운행 중인 열차
    public record MapResponse(
            RouteInfo route,
            List<StationInfo> stations,
            List<TrainInfo> trains
    ) {
    }
}
