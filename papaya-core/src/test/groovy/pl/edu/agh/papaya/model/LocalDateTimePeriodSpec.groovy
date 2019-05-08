package pl.edu.agh.papaya.model

import java.time.DateTimeException
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import org.springframework.context.annotation.ComponentScan
import pl.edu.agh.papaya.TestUtils
import spock.lang.Specification
import spock.lang.Unroll

@ComponentScan('pl.edu.agh.papaya')
class LocalDateTimePeriodSpec extends Specification {

    @Unroll
    def "correctly calculates the duration between the start (#startSeconds) and the end (#endSeconds)"(
            int startSeconds, int endSeconds) {
        given: 'local date time period was created'
        LocalDateTimePeriod localDateTimePeriod = new LocalDateTimePeriod()

        when: 'start and end are set'
        LocalDateTime start = TestUtils.createUtcLocalDateTimeFromEpochSeconds(startSeconds)
        LocalDateTime end = TestUtils.createUtcLocalDateTimeFromEpochSeconds(endSeconds)
        localDateTimePeriod.set(start, end)

        then: 'the local date time period duration will be equal to the difference between start and end'
        localDateTimePeriod.duration.seconds == endSeconds - startSeconds

        where:
        startSeconds | endSeconds
        0            | 0
        0            | 100
        100          | 100
    }

    def "throws exception when setting end before start"() {
        given: 'local date time period was created'
        final LocalDateTimePeriod localDateTimePeriod = new LocalDateTimePeriod()

        when: 'start is set to 100 s'
        localDateTimePeriod.start = TestUtils.createUtcLocalDateTimeFromEpochSeconds(100)
        and: 'end is set to 0 s'
        localDateTimePeriod.end = TestUtils.createUtcLocalDateTimeFromEpochSeconds(0)

        then: 'a date time exception will be thrown'
        thrown DateTimeException
    }

    def "throws exception when setting start after end"() {
        given: 'local date time period was created'
        LocalDateTimePeriod localDateTimePeriod = new LocalDateTimePeriod()

        when: 'end is set to 0 s'
        localDateTimePeriod.end = TestUtils.createUtcLocalDateTimeFromEpochSeconds(0)
        and: 'start is set to 100 s'
        localDateTimePeriod.start = TestUtils.createUtcLocalDateTimeFromEpochSeconds(100)

        then: 'a date time exception will be thrown'
        thrown DateTimeException
    }

    @Unroll
    def "correctly extends the time period (#startSeconds to #endSeconds) to include another timestamp \
        (#extensionSeconds)"(int startSeconds, int endSeconds, int extensionSeconds) {
        given: 'local date time period was created'
        LocalDateTimePeriod localDateTimePeriod = new LocalDateTimePeriod()
        and: 'start and end were set'
        LocalDateTime start = TestUtils.createUtcLocalDateTimeFromEpochSeconds(startSeconds)
        LocalDateTime end = TestUtils.createUtcLocalDateTimeFromEpochSeconds(endSeconds)
        localDateTimePeriod.set(start, end)

        when: 'the time period is extended to contain a new date'
        LocalDateTime extension = TestUtils.createUtcLocalDateTimeFromEpochSeconds(extensionSeconds)
        localDateTimePeriod.extendToContain(extension)

        then: 'the local date time will be extended to include the new date'
        localDateTimePeriod.start.toEpochSecond(ZoneOffset.UTC) == Math.min(startSeconds, extensionSeconds)
        localDateTimePeriod.end.toEpochSecond(ZoneOffset.UTC) == Math.max(endSeconds, extensionSeconds)

        where:
        startSeconds | endSeconds | extensionSeconds
        0            | 100        | 0
        0            | 100        | 50
        0            | 100        | 100
        0            | 100        | 200
        0            | 0          | 100
        0            | 0          | 0
        100          | 100        | 0
    }

    @Unroll
    def "correctly extends the time period (#startSeconds to #endSeconds) by duration (#extensionSeconds)"(
            int startSeconds, int endSeconds, int extensionSeconds) {
        given: 'local date time period was created'
        LocalDateTimePeriod localDateTimePeriod = new LocalDateTimePeriod()
        and: 'start and end were set'
        LocalDateTime start = TestUtils.createUtcLocalDateTimeFromEpochSeconds(startSeconds)
        LocalDateTime end = TestUtils.createUtcLocalDateTimeFromEpochSeconds(endSeconds)
        localDateTimePeriod.set(start, end)

        when: 'the time period is extended'
        Duration duration = Duration.ofSeconds(extensionSeconds)
        localDateTimePeriod.extendBy(duration)

        then: 'the local date time will be correctly extended'
        localDateTimePeriod.end.toEpochSecond(ZoneOffset.UTC) == endSeconds + extensionSeconds

        where:
        startSeconds | endSeconds | extensionSeconds
        0            | 100        | 0
        0            | 100        | 100
        0            | 0          | 100
        0            | 0          | 0
        100          | 100        | 0
        100          | 100        | 100
    }
}
