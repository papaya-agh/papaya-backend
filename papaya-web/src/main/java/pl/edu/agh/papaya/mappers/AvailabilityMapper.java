package pl.edu.agh.papaya.mappers;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.edu.agh.papaya.api.model.AvailabilityDto;
import pl.edu.agh.papaya.model.Availability;
import pl.edu.agh.papaya.model.Sprint;
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

    @Override
    public List<AvailabilityDto> mapToApi(List<Availability> availabilities) {
        return availabilities.stream()
                .map(Availability::getSprint)
                .map(Sprint::getId)
                .distinct()
                .flatMap(sprintId ->
                        mapToApi(availabilities.stream()
                                        .filter(availability -> sprintId.equals(availability.getSprint().getId()))
                                        .collect(Collectors.toList()),
                                sprintService.getPrevSprintAverageCoefficient(sprintId)).stream())
                .collect(Collectors.toList());
    }

    private List<AvailabilityDto> mapToApi(List<Availability> availabilities, double coefficient) {
        return availabilities.stream()
                .map(availability -> mapToApi(availability, coefficient))
                .collect(Collectors.toList());
    }
}
