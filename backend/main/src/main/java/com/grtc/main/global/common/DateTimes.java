package com.grtc.main.global.common;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

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
