package pl.edu.agh.papaya.mappers;

import java.time.Duration;
import java.util.Optional;
import org.springframework.stereotype.Component;
import pl.edu.agh.papaya.api.model.AvailabilityDto;
import pl.edu.agh.papaya.model.Availability;

@Component
public class AvailabilityMapper implements Mapper<Availability, AvailabilityDto> {

    @Override
    public AvailabilityDto mapToApi(Availability availability) {
        return new AvailabilityDto()
                .timeAvailable(Optional.ofNullable(availability.getTimeAvailable())
                        .map(Duration::toMinutes)
                        .orElse(0L))
                .timeRemaining(Optional.ofNullable(availability.getPastSprintRemainingTime())
                        .map(Duration::toMinutes)
                        .orElse(0L))
                .notes(Optional.ofNullable(availability.getNotes())
                        .orElse(""));
    }
}
