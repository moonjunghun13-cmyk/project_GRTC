package com.grtc.main.admin.vehicle;

import com.grtc.main.global.common.ApiResponse;
import com.grtc.main.global.common.PageResponse;
import com.grtc.main.global.common.Pages;
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

@RestController
@RequestMapping("/api/v1/admin/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

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

    @GetMapping("/summary")
    public ApiResponse<VehicleDto.Summary> summary() {
        return ApiResponse.ok(vehicleService.summary());
    }

    @GetMapping("/{vehicleId}")
    public ApiResponse<VehicleDto.Response> get(@PathVariable Long vehicleId) {
        return ApiResponse.ok(vehicleService.get(vehicleId));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<VehicleDto.Response>> create(@Valid @RequestBody VehicleDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(vehicleService.create(request)));
    }

    @PatchMapping("/{vehicleId}")
    public ApiResponse<VehicleDto.Response> update(@PathVariable Long vehicleId,
                                                   @Valid @RequestBody VehicleDto.UpdateRequest request) {
        return ApiResponse.ok(vehicleService.update(vehicleId, request));
    }

    @DeleteMapping("/{vehicleId}")
    public ApiResponse<Void> delete(@PathVariable Long vehicleId) {
        vehicleService.delete(vehicleId);
        return ApiResponse.ok();
    }
}
