package com.grtc.main.admin.dispatch;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.grtc.main.admin.timetable.DayType;
import com.grtc.main.admin.timetable.MoveType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

// 배차관리 화면에서 쓰는 요청·응답 모음
//   날짜/시간은 ISO-8601 (예: dispatchDate 2026-09-30, departureTime 06:30:00)
public final class DispatchDto {

    private DispatchDto() {
    }

    // 배차 등록 요청 (상태는 항상 '배차 대기'로 저장되므로 status 는 보내도 무시한다)
    //  - 일반 배차: 출발시간 + 도착시간 (도착 > 출발)
    //  - 입·출고 배차: moveType(DEPART/RETURN) + trainNo(열번) + departureTime(출고/입고 시각). arrivalTime 은 보내지 않아도 된다.
    public record Request(
            @NotNull(message = "필수 입력란입니다.")
            LocalDate dispatchDate,

            @NotNull(message = "필수 선택 항목입니다.")
            Long vehicleId,

            @NotBlank(message = "필수 입력란입니다.")
            @Size(max = 30, message = "운전자명은 최대 30자까지 입력 가능합니다.")
            String driverName,

            @NotNull(message = "필수 입력란입니다.")
            LocalTime departureTime,

            LocalTime arrivalTime,

            @Size(max = 200, message = "비고는 최대 200자까지 입력 가능합니다.")
            String remark,

            DispatchStatus status,

            MoveType moveType,

            @Size(max = 10, message = "열번은 최대 10자까지 입력 가능합니다.")
            @Pattern(regexp = "^$|^[0-9A-Za-z-]+$", message = "열번은 숫자·영문으로 입력해 주세요.")
            String trainNo
    ) {
        // 일반 배차 등록용 (기존 호출 호환)
        public Request(LocalDate dispatchDate, Long vehicleId, String driverName, LocalTime departureTime,
                       LocalTime arrivalTime, String remark, DispatchStatus status) {
            this(dispatchDate, vehicleId, driverName, departureTime, arrivalTime, remark, status, null, null);
        }
    }

    // 배차 수정 요청 (PATCH: 보낸 항목만 바뀐다. 보내지 않은 항목(null)은 그대로 둔다)
    //  - status 를 보내지 않았는데 차량/날짜/시간이 바뀌면 '배차 변경'으로 자동 처리한다.
    //  - remark 는 빈 문자열("")로 보내면 지워진다.
    public record UpdateRequest(
            LocalDate dispatchDate,

            Long vehicleId,

            @Pattern(regexp = "(?s).*\\S.*", message = "필수 입력란입니다.")
            @Size(max = 30, message = "운전자명은 최대 30자까지 입력 가능합니다.")
            String driverName,

            LocalTime departureTime,

            LocalTime arrivalTime,

            @Size(max = 200, message = "비고는 최대 200자까지 입력 가능합니다.")
            String remark,

            DispatchStatus status
    ) {
    }

    // 배차 한 건 (목록 한 줄 / 상세보기 공통)
    //  - 입·출고 배차는 moveType/trainNo/dayType 이 있고, departureTime 이 출고(또는 입고) 시각이다.
    public record Response(
            Long id,
            String dispatchNo,
            LocalDate dispatchDate,
            Long vehicleId,
            String vehicleNo,
            String driverName,
            LocalTime departureTime,
            LocalTime arrivalTime,
            DispatchStatus status,
            String statusLabel,
            String remark,
            MoveType moveType,
            String moveTypeLabel,
            String trainNo,
            DayType dayType,
            String dayTypeLabel
    ) {
        public static Response from(DispatchEntity d) {
            return new Response(
                    d.getId(),
                    d.getDispatchNo(),
                    d.getDispatchDate(),
                    d.getVehicle().getId(),
                    d.getVehicle().getVehicleNo(),
                    d.getDriverName(),
                    d.getDepartureTime(),
                    d.getArrivalTime(),
                    d.getStatus(),
                    d.getStatus().getLabel(),
                    d.getRemark(),
                    d.getMoveType(),
                    d.getMoveType() == null ? null : d.getMoveType().getLabel(),
                    d.getTrainNo(),
                    d.getDayType(),
                    d.getDayType() == null ? null : d.getDayType().getLabel()
            );
        }
    }

    // 상단 요약 카드 (상태 필터를 뺀 검색 조건 기준)
    public record Summary(
            long total,       // 전체 배차
            long completed,   // 배차 완료
            long waiting,     // 배차 대기
            long changed,     // 배차 변경
            long cancelled,   // 배차 취소
            long departs,     // 출고 (취소 제외)
            long returns      // 입고 (취소 제외)
    ) {
    }

    // 검색 필터/등록 폼의 차량 선택 항목
    public record VehicleOption(Long id, String vehicleNo) {
    }

    // 검색 필터/등록 폼의 선택 목록 (차량, 운전자)
    public record Options(List<VehicleOption> vehicles, List<String> drivers) {
    }

    // 시간표로 배차 만들기 결과
    public record GenerateResult(
            LocalDate date,
            DayType dayType,
            String dayTypeLabel,
            int created        // 만든 배차 수 (출고 + 입고)
    ) {
    }
}
