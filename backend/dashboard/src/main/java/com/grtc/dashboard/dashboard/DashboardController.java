package com.grtc.dashboard.dashboard;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// 대시보드 API (관리자 전용: /api/admin/** 은 ROLE_ADMIN 만 허용)
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    // 대시보드 화면 전체 (민원건수 / 답변건수 카드의 기간은 각각 선택, 기본 이번 달)
    //   예) GET /api/admin/dashboard?complainPeriod=THIS_MONTH&answerPeriod=THIS_YEAR
    @GetMapping
    public DashboardDto.Response get(
            @RequestParam(defaultValue = "THIS_MONTH") DashboardPeriod complainPeriod,
            @RequestParam(defaultValue = "THIS_MONTH") DashboardPeriod answerPeriod
    ) {
        return dashboardService.get(complainPeriod, answerPeriod);
    }

    // 기간 선택 목록 (이번 달 등)
    @GetMapping("/periods")
    public List<DashboardDto.PeriodOption> periods() {
        return dashboardService.periods();
    }
}
