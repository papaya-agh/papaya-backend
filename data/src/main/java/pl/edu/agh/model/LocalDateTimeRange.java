package pl.edu.agh.model;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDateTime;
import javax.persistence.Embeddable;

@Embeddable
public class LocalDateTimeRange {

    private LocalDateTime start;
    private LocalDateTime end;

    public LocalDateTimeRange() {
        this.start = LocalDateTime.now();
        this.end = this.start;
    }

    public LocalDateTimeRange(LocalDateTime start, LocalDateTime end) {
        this.start = start;
        this.end = end;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public void setStart(LocalDateTime start) {
        if (this.end != null && start.isAfter(this.end)) {
            throw new DateTimeException("Start date cannot be after the end date.");
        }
        this.start = start;
    }

    public void setEnd(LocalDateTime end) {
        if (this.start != null && end.isBefore(this.start)) {
            throw new DateTimeException("End date cannot be before the start date.");
        }
        this.end = end;
    }

    public Duration getDuration() {
        return Duration.between(this.start, this.end);
    }
}
