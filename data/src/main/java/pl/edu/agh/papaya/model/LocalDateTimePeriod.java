package pl.edu.agh.papaya.model;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@Embeddable
public class LocalDateTimePeriod {

    @Column(nullable = false)
    private LocalDateTime start;

    @Column(nullable = false)
    private LocalDateTime end;

    public LocalDateTimePeriod() {
        this.start = LocalDateTime.now();
        this.end = this.start;
    }

    public void setStart(LocalDateTime start) {
        if (start.isAfter(this.end)) {
            throw new DateTimeException("Start date cannot be after the end date.");
        }
        this.start = start;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public void setEnd(LocalDateTime end) {
        if (end.isBefore(this.start)) {
            throw new DateTimeException("Start date cannot be after the end date.");
        }
        this.end = end;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public void set(LocalDateTime start, LocalDateTime end) {
        if (end.isBefore(start)) {
            throw new DateTimeException("Start date cannot be after the end date.");
        }
        this.start = start;
        this.end = end;
    }

    public void extendToContain(LocalDateTime date) {
        if (date.isBefore(this.start)) {
            this.start = date;
        } else if (date.isAfter(this.end)) {
            this.end = date;
        }
    }

    public void extendBy(Duration duration) {
        this.end = this.end.plus(duration);
    }

    public Duration getDuration() {
        return Duration.between(this.start, this.end);
    }
}
