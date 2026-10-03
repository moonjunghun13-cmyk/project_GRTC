package com.grtc.main.admin.dashboard;

import com.grtc.main.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// 대시보드 API (명세서 3-3 ⑥ 대시보드) - 권한: 관리자
//   Base URL: /api/v1
@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    // 대시보드 화면 전체 (민원건수 / 답변건수 카드의 기간은 각각 선택, 기본 이번 달)
    //   예) GET /api/v1/admin/dashboard?complainPeriod=THIS_MONTH&answerPeriod=THIS_YEAR
    @GetMapping
    public ApiResponse<DashboardDto.Response> get(
            @RequestParam(defaultValue = "THIS_MONTH") DashboardPeriod complainPeriod,
            @RequestParam(defaultValue = "THIS_MONTH") DashboardPeriod answerPeriod
    ) {
        return ApiResponse.ok(dashboardService.get(complainPeriod, answerPeriod));
    }

    // 기간 선택 목록 (이번 달 등)
    @GetMapping("/periods")
    public ApiResponse<List<DashboardDto.PeriodOption>> periods() {
        return ApiResponse.ok(dashboardService.periods());
    }
}
