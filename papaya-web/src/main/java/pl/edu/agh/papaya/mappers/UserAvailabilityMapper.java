package pl.edu.agh.papaya.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.edu.agh.papaya.api.model.UserAvailabilityDto;
import pl.edu.agh.papaya.model.Availability;

@Component
@RequiredArgsConstructor
public class UserAvailabilityMapper implements Mapper<Availability, UserAvailabilityDto> {

    private final AvailabilityMapper availabilityMapper;

    @Override
    public UserAvailabilityDto mapToApi(Availability availability) {
        return new UserAvailabilityDto()
                .availability(availabilityMapper.mapToApi(availability))
                .userId(availability
                        .getUserInProject()
                        .getUserId());
    }

    public UserAvailabilityDto emptyAvailability(String userId) {
        return new UserAvailabilityDto().userId(userId);
    }
}
