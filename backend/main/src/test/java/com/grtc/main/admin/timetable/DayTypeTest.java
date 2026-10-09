package com.grtc.main.admin.timetable;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

// 입·출고 시간표의 요일 구분 / 운행일 계산 (DB 없이 도는 단위 테스트)
class DayTypeTest {

    @Test
    void 평일_토요일_일요일과_공휴일은_휴일() {
        assertThat(DayType.of(LocalDate.of(2026, 10, 12))).isEqualTo(DayType.WEEKDAY);   // 월
        assertThat(DayType.of(LocalDate.of(2026, 10, 10))).isEqualTo(DayType.SATURDAY);  // 토
        assertThat(DayType.of(LocalDate.of(2026, 10, 11))).isEqualTo(DayType.HOLIDAY);   // 일
        assertThat(DayType.of(LocalDate.of(2026, 10, 9))).isEqualTo(DayType.HOLIDAY);    // 한글날(금)
        assertThat(DayType.of(LocalDate.of(2026, 10, 3))).isEqualTo(DayType.HOLIDAY);    // 개천절(토)
    }

    @Test
    void 새벽_0시대는_전날_운행의_마지막_열차() {
        assertThat(ServiceDay.minute(LocalTime.of(0, 5))).isGreaterThan(ServiceDay.minute(LocalTime.of(23, 50)));
        assertThat(ServiceDay.of(LocalDateTime.of(2026, 10, 10, 0, 30))).isEqualTo(LocalDate.of(2026, 10, 9));
        assertThat(ServiceDay.of(LocalDateTime.of(2026, 10, 10, 5, 0))).isEqualTo(LocalDate.of(2026, 10, 10));
        assertThat(ServiceDay.at(LocalDate.of(2026, 10, 9), LocalTime.of(0, 5)))
                .isEqualTo(LocalDateTime.of(2026, 10, 10, 0, 5));
    }
}
