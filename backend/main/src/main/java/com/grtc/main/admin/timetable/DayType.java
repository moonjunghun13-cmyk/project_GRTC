package com.grtc.main.admin.timetable;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;

// 입·출고 시간표의 요일 구분 (광주 도시철도 입·출고 시간표: 평일 / 토요일 / 휴일)
@Getter
@RequiredArgsConstructor
public enum DayType {
    WEEKDAY("평일"),
    SATURDAY("토요일"),
    HOLIDAY("휴일");   // 일요일 + 공휴일

    private final String label;

    // 날짜에 맞는 시간표 구분. 공휴일은 토요일이어도 휴일 시간표를 쓴다.
    public static DayType of(LocalDate date) {
        if (date.getDayOfWeek() == DayOfWeek.SUNDAY || KoreanHolidays.isHoliday(date)) {
            return HOLIDAY;
        }
        return date.getDayOfWeek() == DayOfWeek.SATURDAY ? SATURDAY : WEEKDAY;
    }
}
