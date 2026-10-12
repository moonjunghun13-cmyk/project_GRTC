package com.grtc.main.admin.dashboard;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

@Getter
@RequiredArgsConstructor
public enum DashboardPeriod {
    TODAY("오늘"),
    THIS_WEEK("이번 주"),
    THIS_MONTH("이번 달"),
    THIS_YEAR("올해"),
    ALL("전체");

    private final String label;

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
