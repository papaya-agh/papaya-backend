package pl.edu.agh.papaya.model;

import java.time.Duration;
import javax.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.edu.agh.papaya.util.DoubleUtil;

@Setter
@Getter
@NoArgsConstructor
@Embeddable
public class SprintStats {

    private static final double MIN_COEFFICIENT = 10e-6;
    private static final double MAX_COEFFICIENT = 10e6;

    private Duration timeBurned;

    private Duration estimatedTimePlanned;

    private Duration finalTimePlanned;

    private Double coefficient;

    private Double averageCoefficientCache = 0d;

    public void updateCoefficient(Duration totalDeclaredTime) {
        if (timeBurned != null && totalDeclaredTime != null && !totalDeclaredTime.isZero()) {
            this.coefficient = DoubleUtil.saturated(
                    (double) totalDeclaredTime.toMinutes() / (double) timeBurned.toMinutes(),
                    MIN_COEFFICIENT, MAX_COEFFICIENT
            );
        }
    }
}
