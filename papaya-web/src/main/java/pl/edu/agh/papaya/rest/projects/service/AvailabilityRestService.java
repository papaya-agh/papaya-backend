package pl.edu.agh.papaya.rest.projects.service;

import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import pl.edu.agh.papaya.api.model.AvailabilityDto;
import pl.edu.agh.papaya.mappers.AvailabilityMapper;
import pl.edu.agh.papaya.model.Availability;
import pl.edu.agh.papaya.model.Project;
import pl.edu.agh.papaya.model.Sprint;
import pl.edu.agh.papaya.model.SprintState;
import pl.edu.agh.papaya.security.UserContext;
import pl.edu.agh.papaya.service.availability.AvailabilityService;
import pl.edu.agh.papaya.service.sprint.SprintService;
import pl.edu.agh.papaya.util.ForbiddenAccessException;
import pl.edu.agh.papaya.util.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class AvailabilityRestService {

    private final SprintService sprintService;

    private final UserContext userContext;

    private final AvailabilityService availabilityService;

    private final AvailabilityMapper availabilityMapper;

    private final ProjectsRestService projectsRestService;

    public ResponseEntity<AvailabilityDto> getUserAvailability(Long projectId, Long sprintId) {
        Project project = projectsRestService.getValidProject(projectId);
        Sprint sprint = getValidSprint(sprintId);

        if (!isSprintInProject(sprint, project)) {
            throw new ForbiddenAccessException();
        }

        AvailabilityDto availability = availabilityService
                .getBySprintIdAndUserId(sprintId, userContext.getUserId())
                .map(availabilityMapper::mapToApi)
                .orElseGet(() -> new AvailabilityDto().timeAvailable(0L).timeRemaining(0L).notes(""));

        return ResponseEntity.ok(availability);
    }

    private Sprint getValidSprint(Long sprintId) {
        return sprintService.getById(sprintId)
                .orElseThrow(ResourceNotFoundException::new);
    }

    private boolean isSprintInProject(Sprint sprint, Project project) {
        return sprint.getProject().getId().equals(project.getId());
    }

    public ResponseEntity<AvailabilityDto> updateUserAvailability(AvailabilityDto availabilityDto,
            Long projectId, Long sprintId) {
        Project project = projectsRestService.getValidProject(projectId);
        Sprint sprint = getValidSprint(sprintId);
        if (!isSprintInProject(sprint, project) || isAvailabilityDeclarationNotAllowed(sprint)) {
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

    private boolean isAvailabilityDeclarationNotAllowed(Sprint sprint) {
        return sprint.getSprintState() != SprintState.DECLARABLE;
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
