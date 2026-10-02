package com.grtc.dashboard.vehicle;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

// 차량관리 화면에서 쓰는 요청·응답 모음
//   날짜는 ISO-8601 (예: 2026-09-21)
public final class VehicleDto {

    private VehicleDto() {
    }

    // 차량 등록 요청
    public record Request(
            @NotBlank(message = "필수 입력란입니다.")
            @Size(max = 30, message = "차량번호는 최대 30자까지 입력 가능합니다.")
            String vehicleNo,

            @NotNull(message = "필수 선택 항목입니다.")
            VehicleStatus status,

            @PastOrPresent(message = "최근 점검일은 오늘 이후로 입력할 수 없습니다.")
            LocalDate lastInspectionDate
    ) {
    }

    // 차량 수정 요청 (PATCH: 보낸 항목만 바뀐다. 보내지 않은 항목(null)은 그대로 둔다)
    public record UpdateRequest(
            @Pattern(regexp = "(?s).*\\S.*", message = "필수 입력란입니다.")
            @Size(max = 30, message = "차량번호는 최대 30자까지 입력 가능합니다.")
            String vehicleNo,

            VehicleStatus status,

            @PastOrPresent(message = "최근 점검일은 오늘 이후로 입력할 수 없습니다.")
            LocalDate lastInspectionDate
    ) {
    }

    // 차량 한 건 (목록 한 줄 / 상세보기 공통)
    public record Response(
            Long id,
            String vehicleNo,
            VehicleStatus status,
            String statusLabel,
            LocalDate lastInspectionDate
    ) {
        public static Response from(VehicleEntity v) {
            return new Response(
                    v.getId(),
                    v.getVehicleNo(),
                    v.getStatus(),
                    v.getStatus().getLabel(),
                    v.getLastInspectionDate()
            );
        }
    }

    // 상단 요약 카드 + 대시보드 가동률
    public record Summary(
            long total,        // 전체 차량
            long running,      // 운행중
            long standby,      // 대기
            long maintenance,  // 정비
            long stopped,      // 운행정지
            int operationRate  // 가동률(%) = 운행중 / 전체
    ) {
    }
}
