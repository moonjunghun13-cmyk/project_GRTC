package com.grtc.main.admin.operation;

import java.util.List;

// 운행관리(노선도) / 대시보드 노선운영현황에서 쓰는 응답 모음
public final class OperationDto {

    private OperationDto() {
    }

    // 노선 요약 (대시보드 노선운영현황 카드)
    public record RouteInfo(
            Long id,
            String name,           // 광주 도시철도 1호선
            RouteStatus status,
            String statusLabel,    // 정상 운행
            int stationCount,      // 운행 역수
            int dailyRunCount,     // 운행 횟수
            String firstStation,   // 첫 역 (평동)
            String lastStation     // 마지막 역 (녹동)
    ) {
    }

    // 노선도에 그릴 역 (seq 순서대로)
    public record StationInfo(Long id, int seq, String name) {
    }

    // 노선도 위의 열차 한 대 (말풍선: "101편성 · 평동행 / 다음 역 도산")
    public record TrainInfo(
            Long operationId,
            String vehicleNo,          // 101편성
            TrainDirection direction,
            String directionLabel,     // 평동행
            Long currentStationId,
            String currentStation,     // 광주송정역
            String nextStation,        // 도산 (종착역에 도착했으면 null)
            String heading             // FORWARD(역 번호 증가 방향) / BACKWARD(감소 방향) / ARRIVED(종착역)
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
