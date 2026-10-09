package com.grtc.main.admin.timetable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

// 운행일 계산 도우미
//  - 도시철도 운행은 새벽 5시대에 시작해 다음 날 0시대에 끝난다.
//  - 그래서 새벽 3시 전의 시각(예: 0:05 입고)은 '전날 운행'의 마지막 열차로 본다.
public final class ServiceDay {

    public static final LocalTime START = LocalTime.of(3, 0);

    private ServiceDay() {
    }

    // 운행일 기준 분(分): 새벽 0~2시는 24시 이후(1440분~)로 센다.
    public static int minute(LocalTime time) {
        int minute = time.getHour() * 60 + time.getMinute();
        return time.isBefore(START) ? minute + 24 * 60 : minute;
    }

    // 지금이 어느 운행일인지 (새벽 3시 전이면 전날)
    public static LocalDate of(LocalDateTime now) {
        return now.toLocalTime().isBefore(START) ? now.toLocalDate().minusDays(1) : now.toLocalDate();
    }

    // 운행일 + 시간표 시각 -> 실제 일시 (0~2시대는 다음 날)
    public static LocalDateTime at(LocalDate serviceDate, LocalTime time) {
        return serviceDate.atTime(time).plusDays(time.isBefore(START) ? 1 : 0);
    }
}
