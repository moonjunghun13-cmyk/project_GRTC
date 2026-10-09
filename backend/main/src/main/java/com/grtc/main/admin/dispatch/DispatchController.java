package com.grtc.main.admin.dispatch;

import com.grtc.main.admin.timetable.DayType;
import com.grtc.main.admin.timetable.MoveType;
import com.grtc.main.admin.timetable.TimetableDto;
import com.grtc.main.admin.timetable.TimetableService;
import com.grtc.main.global.common.ApiResponse;
import com.grtc.main.global.common.PageResponse;
import com.grtc.main.global.common.Pages;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

// 배차관리 API (명세서 3-3 ④ 배차) - 권한: 관리자
//   Base URL: /api/v1
@RestController
@RequestMapping("/api/v1/admin/dispatches")
@RequiredArgsConstructor
public class DispatchController {

    private final DispatchService dispatchService;
    private final TimetableService timetableService;

    // 배차 목록 (검색어, 배차일자(yyyy-MM-dd), 차량, 운전자, 상태, 입·출고 구분(DEPART/RETURN) 필터, 페이징: page 0부터)
    //   예) GET /api/v1/admin/dispatches?date=2026-09-30&moveType=DEPART&page=0&size=10&sort=departureTime,asc
    @GetMapping
    public ApiResponse<PageResponse<DispatchDto.Response>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long vehicleId,
            @RequestParam(required = false) String driver,
            @RequestParam(required = false) DispatchStatus status,
            @RequestParam(required = false) MoveType moveType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort
    ) {
        return ApiResponse.ok(dispatchService.list(keyword, date, vehicleId, driver, status, moveType,
                Pages.request(page, size, sort, DispatchService.DEFAULT_SORT, DispatchService.SORTABLE)));
    }

    // 상단 요약 카드 (전체 / 완료 / 대기 / 변경 / 취소 건수)
    //   목록과 같은 검색 조건을 보내면 그 조건 기준으로 센다. (상태 필터는 제외)
    @GetMapping("/summary")
    public ApiResponse<DispatchDto.Summary> summary(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long vehicleId,
            @RequestParam(required = false) String driver,
            @RequestParam(required = false) MoveType moveType
    ) {
        return ApiResponse.ok(dispatchService.summary(keyword, date, vehicleId, driver, moveType));
    }

    // 입·출고 시간표 (원본: 광주 도시철도 입·출고 시간표)
    //   GET /api/v1/admin/dispatches/timetable?date=2026-10-09  -> 그 날짜의 평일/토요일/휴일 시간표
    //   GET /api/v1/admin/dispatches/timetable?dayType=SATURDAY -> 해당 구분 시간표
    //   둘 다 없으면 오늘 날짜 기준
    @GetMapping("/timetable")
    public ApiResponse<TimetableDto.Response> timetable(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) DayType dayType
    ) {
        if (dayType != null && date == null) {
            return ApiResponse.ok(timetableService.get(dayType));
        }
        return ApiResponse.ok(timetableService.get(date != null ? date : LocalDate.now()));
    }

    // 입·출고 시간표로 그 날짜의 배차 만들기 (출고 1회·입고 1회가 각각 배차 1건)
    //   POST /api/v1/admin/dispatches/generate?date=2026-10-10
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<DispatchDto.GenerateResult>> generate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(dispatchService.generate(date)));
    }

    // 필터/등록 폼의 차량·운전자 선택 목록
    @GetMapping("/options")
    public ApiResponse<DispatchDto.Options> options() {
        return ApiResponse.ok(dispatchService.options());
    }

    // 배차 상세
    @GetMapping("/{dispatchId}")
    public ApiResponse<DispatchDto.Response> get(@PathVariable Long dispatchId) {
        return ApiResponse.ok(dispatchService.get(dispatchId));
    }

    // 배차 등록 (차량 상태·시간 겹침 검증 후 저장)
    @PostMapping
    public ResponseEntity<ApiResponse<DispatchDto.Response>> create(@Valid @RequestBody DispatchDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(dispatchService.create(request)));
    }

    // 배차 수정 - 보낸 항목만 변경
    @PatchMapping("/{dispatchId}")
    public ApiResponse<DispatchDto.Response> update(@PathVariable Long dispatchId,
                                                    @Valid @RequestBody DispatchDto.UpdateRequest request) {
        return ApiResponse.ok(dispatchService.update(dispatchId, request));
    }

    // 배차 삭제 (기록을 실제로 삭제한다. 취소 상태 변경과 별도)
    @DeleteMapping("/{dispatchId}")
    public ApiResponse<Void> delete(@PathVariable Long dispatchId) {
        dispatchService.delete(dispatchId);
        return ApiResponse.ok();
    }

    // 배차 취소 (status -> CANCELLED)
    @PatchMapping("/{dispatchId}/cancel")
    public ApiResponse<DispatchDto.Response> cancel(@PathVariable Long dispatchId) {
        return ApiResponse.ok(dispatchService.cancel(dispatchId));
    }
}
