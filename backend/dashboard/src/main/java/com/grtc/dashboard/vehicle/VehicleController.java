package com.grtc.dashboard.vehicle;

import com.grtc.dashboard.global.common.ApiResponse;
import com.grtc.dashboard.global.common.PageResponse;
import com.grtc.dashboard.global.common.Pages;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

// 차량관리 API (명세서 3-3 ③ 차량) - 권한: 관리자
//   Base URL: /api/v1
@RestController
@RequestMapping("/api/v1/admin/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    // 차량 목록 (차량번호 검색, 상태 필터, 페이징: page 0부터)
    //   예) GET /api/v1/admin/vehicles?keyword=10&status=RUNNING&page=0&size=10&sort=vehicleNo,asc
    @GetMapping
    public ApiResponse<PageResponse<VehicleDto.Response>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) VehicleStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort
    ) {
        return ApiResponse.ok(vehicleService.list(keyword, status,
                Pages.request(page, size, sort, VehicleService.DEFAULT_SORT, VehicleService.SORTABLE)));
    }

    // 상단 요약 카드 (전체 / 운행중 / 대기 / 정비 / 운행정지 차량 수, 가동률)
    @GetMapping("/summary")
    public ApiResponse<VehicleDto.Summary> summary() {
        return ApiResponse.ok(vehicleService.summary());
    }

    // 차량 상세
    @GetMapping("/{vehicleId}")
    public ApiResponse<VehicleDto.Response> get(@PathVariable Long vehicleId) {
        return ApiResponse.ok(vehicleService.get(vehicleId));
    }

    // 차량 등록
    @PostMapping
    public ResponseEntity<ApiResponse<VehicleDto.Response>> create(@Valid @RequestBody VehicleDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(vehicleService.create(request)));
    }

    // 차량 정보 수정 - 보낸 항목만 변경
    @PatchMapping("/{vehicleId}")
    public ApiResponse<VehicleDto.Response> update(@PathVariable Long vehicleId,
                                                   @Valid @RequestBody VehicleDto.UpdateRequest request) {
        return ApiResponse.ok(vehicleService.update(vehicleId, request));
    }

    // 차량 삭제 (배차/운행 이력이 없을 때만)
    @DeleteMapping("/{vehicleId}")
    public ApiResponse<Void> delete(@PathVariable Long vehicleId) {
        vehicleService.delete(vehicleId);
        return ApiResponse.ok();
    }
}
