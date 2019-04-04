package pl.edu.agh.model;

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
    private LocalDateTime begin;

    @Column(nullable = false)
    private LocalDateTime end;

    public LocalDateTimePeriod() {
        this.begin = LocalDateTime.now();
        this.end = this.begin;
    }

    public void setBegin(LocalDateTime begin) {
        if (begin.isAfter(this.end)) {
            throw new DateTimeException("Start date cannot be after the end date.");
        }
        this.begin = begin;
    }

    public void setEnd(LocalDateTime end) {
        if (end.isBefore(this.begin)) {
            throw new DateTimeException("Start date cannot be after the end date.");
        }
        this.end = end;
    }

    public void set(LocalDateTime start, LocalDateTime end) {
        if (start.isBefore(end)) {
            throw new DateTimeException("Start date cannot be after the end date.");
        }
        this.begin = start;
        this.end = end;
    }

    public void extendToContain(LocalDateTime date) {
        if (date.isBefore(this.begin)) {
            this.begin = date;
        } else if (date.isAfter(this.end)) {
            this.end = date;
        }
    }

    public void extendBy(Duration duration) {
        this.end = this.end.plus(duration);
    }

    public Duration getDuration() {
        return Duration.between(this.begin, this.end);
    }
}
