package com.grtc.main.admin.dashboard;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

// 대시보드 민원건수/답변건수 카드의 기간 선택 (기본: 이번 달)
@Getter
@RequiredArgsConstructor
public enum DashboardPeriod {
    TODAY("오늘"),
    THIS_WEEK("이번 주"),
    THIS_MONTH("이번 달"),
    THIS_YEAR("올해"),
    ALL("전체");

    private final String label;

    // 이 기간의 시작 시각 (이 시각 이후에 접수된 민원을 센다)
    public LocalDateTime from(LocalDate today) {
        return switch (this) {
            case TODAY -> today.atStartOfDay();
            case THIS_WEEK -> today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
            case THIS_MONTH -> today.withDayOfMonth(1).atStartOfDay();
            case THIS_YEAR -> today.withDayOfYear(1).atStartOfDay();
            case ALL -> LocalDateTime.of(1970, 1, 1, 0, 0);
        };
    }
}
