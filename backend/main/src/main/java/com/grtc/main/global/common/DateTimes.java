package com.grtc.main.global.common;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

// 명세서 3-1 날짜 형식(ISO-8601, 2026-10-15T05:30:00+09:00)으로 내려주기 위한 유틸
//   DB 에는 LocalDateTime 으로 저장되어 있으므로, 응답으로 내보낼 때 시간대(+09:00)를 붙인다.
public final class DateTimes {

    private DateTimes() {
    }

    public static OffsetDateTime toOffset(LocalDateTime value) {
        if (value == null) {
            return null;
        }
        return value.truncatedTo(ChronoUnit.SECONDS)
                .atZone(ZoneId.systemDefault())
                .toOffsetDateTime();
    }
}
