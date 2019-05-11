package pl.edu.agh.papaya.service.availability;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.papaya.model.Availability;
import pl.edu.agh.papaya.repository.AvailabilityRepository;
import pl.edu.agh.papaya.service.userinproject.UserInProjectService;

@Service
@RequiredArgsConstructor
@SuppressWarnings({"PMD.BeanMembersShouldSerialize"})
public class AvailabilityService {

    private final AvailabilityRepository availabilityRepository;

    private final UserInProjectService userInProjectService;

    public Optional<Availability> getBySprintIdAndUserId(Long sprintId, Long userId) {
        return availabilityRepository.findBySprintIdAndUserInProjectUserId(sprintId, userId);
    }

    public List<Availability> getBySprintId(Long sprintId) {
        return availabilityRepository.findBySprintId(sprintId);
    }

    public AvailabilityCreationWizard newAvailability() {
        return new AvailabilityCreationWizard(this, userInProjectService);
    }

    public Availability save(Availability availability) {
        return availabilityRepository.save(availability);
    }
}
