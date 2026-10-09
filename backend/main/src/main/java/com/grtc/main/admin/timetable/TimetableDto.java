package com.grtc.main.admin.timetable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

// 입·출고 시간표 응답
public final class TimetableDto {

    private TimetableDto() {
    }

    // 시간표 한 줄
    public record Row(int seq, String trainNo, LocalTime time) {
        public static Row from(TimetableEntity e) {
            return new Row(e.getSeq(), e.getTrainNo(), e.getScheduledTime());
        }
    }

    // 요일 구분 하나의 시간표 (출고 목록 / 입고 목록, 원본 표 순서)
    public record Response(
            DayType dayType,
            String dayTypeLabel,
            LocalDate date,        // 날짜로 조회했을 때만 값이 있다
            List<Row> departs,
            List<Row> returns
    ) {
    }
}
