package pl.edu.agh.papaya.mappers;

import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.edu.agh.papaya.api.model.AvailabilityDto;
import pl.edu.agh.papaya.model.Availability;
import pl.edu.agh.papaya.service.sprint.SprintService;

@Component
@RequiredArgsConstructor
public class AvailabilityMapper implements Mapper<Availability, AvailabilityDto> {

    private final SprintService sprintService;

    @Override
    public AvailabilityDto mapToApi(Availability availability) {
        double coefficient =
                sprintService.getPrevSprintAverageCoefficient(availability.getSprint());
        return mapToApi(availability, coefficient);
    }

    private AvailabilityDto mapToApi(Availability availability, double coefficient) {

        long timeAvailable = Optional.ofNullable(availability.getTimeAvailable())
                .map(Duration::toMinutes)
                .orElse(0L);

        long effectiveTimeAvailable = (long) (timeAvailable / coefficient);

        return new AvailabilityDto()
                .timeAvailable(timeAvailable)
                .effectiveTimeAvailable(effectiveTimeAvailable)
                .timeRemaining(Optional.ofNullable(availability.getPastSprintRemainingTime())
                        .map(Duration::toMinutes)
                        .orElse(0L))
                .notes(Optional.ofNullable(availability.getNotes())
                        .orElse(""));
    }
}
