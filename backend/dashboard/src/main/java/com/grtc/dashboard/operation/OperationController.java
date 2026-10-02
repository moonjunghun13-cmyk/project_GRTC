package com.grtc.dashboard.operation;

import com.grtc.dashboard.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 운행관리 API (명세서 3-3 ⑤ 운행) - 권한: 관리자
//   Base URL: /api/v1
@RestController
@RequestMapping("/api/v1/admin/operations")
@RequiredArgsConstructor
public class OperationController {

    private final OperationService operationService;

    // 노선도(역 목록) + 운행 중인 열차 위치
    @GetMapping
    public ApiResponse<OperationDto.MapResponse> map() {
        return ApiResponse.ok(operationService.map());
    }
}
