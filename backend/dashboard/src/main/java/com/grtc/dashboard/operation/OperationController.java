package com.grtc.dashboard.operation;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 운행관리 API (관리자 전용: /api/admin/** 은 SecurityConfig 에서 ROLE_ADMIN 만 허용)
@RestController
@RequestMapping("/api/admin/operations")
@RequiredArgsConstructor
public class OperationController {

    private final OperationService operationService;

    // 노선도(역 목록) + 운행 중인 열차 위치
    @GetMapping
    public OperationDto.MapResponse map() {
        return operationService.map();
    }
}
