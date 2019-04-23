package pl.edu.agh.papaya

import java.time.LocalDateTime
import java.time.ZoneOffset
import pl.edu.agh.papaya.model.LocalDateTimePeriod

final class TestUtils {

    static LocalDateTimePeriod createUtcLocalDateTimePeriodFromEpochSeconds(int start, int end) {
        new LocalDateTimePeriod(createUtcLocalDateTimeFromEpochSeconds(start),
                createUtcLocalDateTimeFromEpochSeconds(end))
    }

    static LocalDateTime createUtcLocalDateTimeFromEpochSeconds(int seconds) {
        LocalDateTime.ofEpochSecond(seconds, 0, ZoneOffset.UTC)
    }
}
