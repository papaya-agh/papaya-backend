package pl.edu.agh.papaya.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.edu.agh.papaya.api.model.UserAvailabilityDto;
import pl.edu.agh.papaya.model.Availability;
import pl.edu.agh.papaya.model.User;

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
                        .getUser()
                        .getId());
    }

    public UserAvailabilityDto emptyAvailability(User user) {
        return new UserAvailabilityDto().userId(user.getId());
    }
}
