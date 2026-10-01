package com.grtc.dashboard.vehicle;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// 차량관리 API (관리자 전용: /api/admin/** 은 SecurityConfig 에서 ROLE_ADMIN 만 허용)
@RestController
@RequestMapping("/api/admin/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    // 요약 카드 + 목록 (차량번호 검색, 상태 필터, page 는 1부터)
    @GetMapping
    public VehicleDto.ListResponse list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) VehicleStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return vehicleService.list(keyword, status, page, size);
    }

    // 상세보기
    @GetMapping("/{id}")
    public VehicleDto.Response get(@PathVariable Long id) {
        return vehicleService.get(id);
    }

    // 차량 등록
    @PostMapping
    public ResponseEntity<VehicleDto.Response> create(@Valid @RequestBody VehicleDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicleService.create(request));
    }

    // 차량 수정
    @PutMapping("/{id}")
    public VehicleDto.Response update(@PathVariable Long id,
                                      @Valid @RequestBody VehicleDto.Request request) {
        return vehicleService.update(id, request);
    }

    // 차량 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        vehicleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
