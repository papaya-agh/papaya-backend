package pl.edu.agh.papaya.rest.availability.service;

import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import pl.edu.agh.papaya.api.model.AvailabilityDto;
import pl.edu.agh.papaya.mappers.AvailabilityMapper;
import pl.edu.agh.papaya.model.Availability;
import pl.edu.agh.papaya.model.Sprint;
import pl.edu.agh.papaya.model.SprintState;
import pl.edu.agh.papaya.rest.sprints.service.SprintsRestService;
import pl.edu.agh.papaya.security.UserContext;
import pl.edu.agh.papaya.service.availability.AvailabilityService;
import pl.edu.agh.papaya.util.ForbiddenAccessException;

@Service
@RequiredArgsConstructor
public class AvailabilityRestService {

    private final SprintsRestService sprintRestService;

    private final UserContext userContext;

    private final AvailabilityService availabilityService;

    private final AvailabilityMapper availabilityMapper;

    public ResponseEntity<AvailabilityDto> getUserAvailability(Long projectId, Long sprintId) {
        Sprint sprint = sprintRestService.getValidSprint(projectId, sprintId);

        AvailabilityDto availability = availabilityService
                .getBySprintIdAndUserId(sprint.getId(), userContext.getUserId())
                .map(availabilityMapper::mapToApi)
                .orElseGet(() -> new AvailabilityDto().timeAvailable(0L).timeRemaining(0L).notes(""));

        return ResponseEntity.ok(availability);
    }

    public ResponseEntity<AvailabilityDto> updateUserAvailability(AvailabilityDto availabilityDto,
            Long projectId, Long sprintId) {
        Sprint sprint = sprintRestService.getValidSprint(projectId, sprintId);

        if (!isAvailabilityDeclarationAllowed(sprint)) {
            throw new ForbiddenAccessException();
        }

        if (availabilityDto.getTimeAvailable() < 0 || availabilityDto.getTimeRemaining() < 0) {
            throw new ForbiddenAccessException();
        }

        Availability availability = availabilityService.getBySprintIdAndUserId(sprintId, userContext.getUserId())
                .map(persistedAvailability -> updatePersistedAvailability(persistedAvailability, availabilityDto))
                .orElseGet(() -> createAvailability(availabilityDto, sprint));

        return ResponseEntity.ok(availabilityMapper.mapToApi(availability));
    }

    private boolean isAvailabilityDeclarationAllowed(Sprint sprint) {
        return sprint.getSprintState() == SprintState.DECLARABLE;
    }

    private Availability createAvailability(AvailabilityDto availabilityDto, Sprint sprint) {
        return availabilityService.newAvailability()
                .withSprint(sprint)
                .withUser(userContext.getUser())
                .withTimeAvailable(Duration.ofMinutes(availabilityDto.getTimeAvailable()))
                .withPastSprintRemainingTime(Duration.ofMinutes(availabilityDto.getTimeRemaining()))
                .withNotes(Optional.ofNullable(availabilityDto.getNotes()).orElse(""))
                .create();
    }

    private Availability updatePersistedAvailability(Availability availability, AvailabilityDto availabilityDto) {
        if (availabilityDto.getNotes() != null) {
            availability.setNotes(availabilityDto.getNotes());
        }
        availability.setTimeAvailable(Duration.ofMinutes(availabilityDto.getTimeAvailable()));
        availability.setPastSprintRemainingTime(Duration.ofMinutes(availabilityDto.getTimeRemaining()));
        return availabilityService.save(availability);
    }
}
