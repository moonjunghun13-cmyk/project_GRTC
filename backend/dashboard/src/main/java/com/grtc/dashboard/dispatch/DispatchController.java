package com.grtc.dashboard.dispatch;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

// 배차관리 API (관리자 전용: /api/admin/** 은 SecurityConfig 에서 ROLE_ADMIN 만 허용)
@RestController
@RequestMapping("/api/admin/dispatches")
@RequiredArgsConstructor
public class DispatchController {

    private final DispatchService dispatchService;

    // 요약 카드 + 목록 (검색어, 배차일자(yyyy-MM-dd), 차량, 운전자, 상태 필터 / page 는 1부터)
    @GetMapping
    public DispatchDto.ListResponse list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long vehicleId,
            @RequestParam(required = false) String driver,
            @RequestParam(required = false) DispatchStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return dispatchService.list(keyword, date, vehicleId, driver, status, page, size);
    }

    // 필터/등록 폼의 차량·운전자 선택 목록
    @GetMapping("/options")
    public DispatchDto.Options options() {
        return dispatchService.options();
    }

    // 상세보기
    @GetMapping("/{id}")
    public DispatchDto.Response get(@PathVariable Long id) {
        return dispatchService.get(id);
    }

    // 배차 등록
    @PostMapping
    public ResponseEntity<DispatchDto.Response> create(@Valid @RequestBody DispatchDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dispatchService.create(request));
    }

    // 배차 수정
    @PutMapping("/{id}")
    public DispatchDto.Response update(@PathVariable Long id,
                                       @Valid @RequestBody DispatchDto.Request request) {
        return dispatchService.update(id, request);
    }

    // 배차 취소
    @PatchMapping("/{id}/cancel")
    public DispatchDto.Response cancel(@PathVariable Long id) {
        return dispatchService.cancel(id);
    }
}
