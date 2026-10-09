package com.grtc.main.admin.timetable;

import java.time.LocalDate;
import java.util.Set;

// 휴일 시간표를 쓰는 공휴일 목록 (일요일은 DayType 에서 따로 처리)
//  - 음력 공휴일·대체공휴일이 해마다 달라서 날짜로 적어 둔다. 해가 바뀌면 다음 해 날짜를 추가한다.
public final class KoreanHolidays {

    private KoreanHolidays() {
    }

    private static final Set<LocalDate> HOLIDAYS = Set.of(
            // 2026년
            LocalDate.of(2026, 1, 1),                                                  // 신정
            LocalDate.of(2026, 2, 16), LocalDate.of(2026, 2, 17), LocalDate.of(2026, 2, 18), // 설날 연휴
            LocalDate.of(2026, 3, 2),                                                  // 삼일절 대체공휴일
            LocalDate.of(2026, 5, 5),                                                  // 어린이날
            LocalDate.of(2026, 5, 25),                                                 // 부처님오신날 대체공휴일
            LocalDate.of(2026, 6, 3),                                                  // 전국동시지방선거
            LocalDate.of(2026, 8, 17),                                                 // 광복절 대체공휴일
            LocalDate.of(2026, 9, 24), LocalDate.of(2026, 9, 25), LocalDate.of(2026, 9, 26), // 추석 연휴
            LocalDate.of(2026, 10, 3), LocalDate.of(2026, 10, 5),                      // 개천절, 대체공휴일
            LocalDate.of(2026, 10, 9),                                                 // 한글날
            LocalDate.of(2026, 12, 25)                                                 // 성탄절
    );

    public static boolean isHoliday(LocalDate date) {
        return HOLIDAYS.contains(date);
    }
}
