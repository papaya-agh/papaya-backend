package pl.edu.agh.papaya.rest.projects.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import pl.edu.agh.papaya.api.model.SprintDto;
import pl.edu.agh.papaya.api.model.SprintStateDto;
import pl.edu.agh.papaya.api.model.SprintSummaryDto;
import pl.edu.agh.papaya.api.model.UserAvailabilityDto;
import pl.edu.agh.papaya.mappers.LocalDateTimePeriodMapper;
import pl.edu.agh.papaya.mappers.SprintMapper;
import pl.edu.agh.papaya.mappers.SprintStateMapper;
import pl.edu.agh.papaya.mappers.UserAvailabilityMapper;
import pl.edu.agh.papaya.model.Availability;
import pl.edu.agh.papaya.model.LocalDateTimePeriod;
import pl.edu.agh.papaya.model.Project;
import pl.edu.agh.papaya.model.Sprint;
import pl.edu.agh.papaya.model.SprintState;
import pl.edu.agh.papaya.model.User;
import pl.edu.agh.papaya.model.UserInProject;
import pl.edu.agh.papaya.security.UserContext;
import pl.edu.agh.papaya.service.sprint.SprintService;
import pl.edu.agh.papaya.util.AssertionUtil;
import pl.edu.agh.papaya.util.BadRequestException;
import pl.edu.agh.papaya.util.ForbiddenAccessException;
import pl.edu.agh.papaya.util.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
@SuppressWarnings({"ClassFanOutComplexity"})
public class SprintsRestService {

    private static final List<SprintStateDto> ALL_SPRINT_STATE_DTOS = List.of(SprintStateDto.values());

    private final SprintService sprintService;

    private final SprintStateMapper sprintStateMapper;

    private final SprintMapper sprintMapper;

    private final ProjectsRestService projectsRestService;

    private final UserContext userContext;

    private final LocalDateTimePeriodMapper localDateTimePeriodMapper;

    private final UserAvailabilityMapper userAvailabilityMapper;

    public ResponseEntity<List<SprintDto>> getSprints(Long projectId, List<SprintStateDto> sprintStateDtos) {
        final LocalDateTime currentTime = LocalDateTime.now();

        List<SprintState> sprintStates = sprintStateMapper.mapFromApi(
                Optional.ofNullable(sprintStateDtos).orElse(ALL_SPRINT_STATE_DTOS));

        List<SprintDto> sprints = sprintService.getByStatesInProject(sprintStates, projectId, currentTime)
                .stream()
                .sorted(Comparator.comparing(sprint -> sprint.getEnrollmentPeriod().getStart()))
                .map(sprint -> sprintMapper.mapToApi(sprint, currentTime))
                .collect(Collectors.toList());

        return ResponseEntity.ok(sprints);
    }

    public ResponseEntity<SprintDto> addSprint(SprintDto sprintDto, Long projectId) {
        Project project = projectsRestService.getValidProject(projectId);

        if (!project.isAdmin(userContext.getUser())) {
            throw new ForbiddenAccessException();
        }

        LocalDateTimePeriod enrollmentPeriod = localDateTimePeriodMapper.mapFromApi(sprintDto.getEnrollmentPeriod());
        LocalDateTimePeriod durationPeriod = localDateTimePeriodMapper.mapFromApi(sprintDto.getDurationPeriod());

        if (isInOverlapWithLastSprint(project, durationPeriod)) {
            throw new IllegalStateException("Cannot create sprint starting before the last one ended");
        }

        Sprint created = sprintService.newSprint()
                .withProject(project)
                .withEnrollmentPeriod(enrollmentPeriod)
                .withDurationPeriod(durationPeriod)
                .create();
        return ResponseEntity.ok(sprintMapper.mapToApi(created));
    }

    private boolean isInOverlapWithLastSprint(Project project, LocalDateTimePeriod durationPeriod) {
        Optional<Sprint> lastSprintOpt = sprintService.getLastInProject(project.getId());
        return lastSprintOpt.isPresent() && durationPeriod.isBefore(lastSprintOpt.get().getDurationPeriod());
    }

    public ResponseEntity<SprintSummaryDto> getSprintSummary(Long projectId, Long sprintId) {
        Project project = projectsRestService.getValidProject(projectId);

        if (!project.isAdmin(userContext.getUser())) {
            throw new ForbiddenAccessException();
        }

        Sprint currentSprint = getValidSprint(sprintId);
        List<Availability> availabilities = currentSprint.getAvailabilities();
        List<UserAvailabilityDto> userAvailabilityDtos = getUserAvailabilityDtos(project, availabilities);

        Duration totalDeclaredTime = availabilities.stream()
                .map(Availability::getTimeAvailable)
                .reduce(Duration.ZERO, Duration::plus);

        return ResponseEntity.ok(createSprintSummaryDto(
                currentSprint,
                userAvailabilityDtos,
                totalDeclaredTime));
    }

    private SprintSummaryDto createSprintSummaryDto(Sprint currentSprint,
            List<UserAvailabilityDto> userAvailabilityDtos, Duration totalDeclaredTime) {

        double prevAverageSprintCoefficient = sprintService.getPrevSprintAverageCoefficient(currentSprint);
        double currentAverageSprintCoefficient = currentSprint.getAverageCoefficientCache();

        long timeToAssign = (long) (totalDeclaredTime.toMinutes() / prevAverageSprintCoefficient);

        Long totalNeededTime = null;
        Double coefficient = currentSprint.getCoefficient();
        Duration finalTimePlanned = currentSprint.getFinalTimePlanned();
        if (coefficient != null && finalTimePlanned != null) {
            totalNeededTime = (long) (finalTimePlanned.toMinutes() * coefficient);
        }

        return new SprintSummaryDto()
                .sprint(sprintMapper.mapToApi(currentSprint))
                .membersAvailability(userAvailabilityDtos)
                .prevAverageSprintCoefficient(prevAverageSprintCoefficient)
                .currentAverageSprintCoefficient(currentAverageSprintCoefficient)
                .timeToAssign(timeToAssign)
                .totalDeclaredTime(totalDeclaredTime.toMinutes())
                .totalNeededTime(totalNeededTime);
    }

    private List<UserAvailabilityDto> getUserAvailabilityDtos(Project project, List<Availability> availabilities) {
        List<UserAvailabilityDto> usersWithNoAvailabilities = getUsersWithNoAvailabilities(project, availabilities);
        List<UserAvailabilityDto> usersWithAvailabilities = userAvailabilityMapper.mapToApi(availabilities);
        return Stream.concat(usersWithAvailabilities.stream(), usersWithNoAvailabilities.stream())
                .collect(Collectors.toList());
    }

    private List<UserAvailabilityDto> getUsersWithNoAvailabilities(Project project, List<Availability> availabilities) {
        List<User> users = project.getUsersInProject().stream()
                .map(UserInProject::getUser)
                .collect(Collectors.toList());

        return users.stream()
                .filter(user -> availabilities.stream()
                        .noneMatch(availability -> user.equals(availability.getUserInProject().getUser())))
                .map(userAvailabilityMapper::emptyAvailability).collect(Collectors.toList());
    }

    private Sprint getValidSprint(Long sprintId) {
        return sprintService.getById(sprintId)
                .orElseThrow(ResourceNotFoundException::new);
    }

    public ResponseEntity<SprintDto> modifySprint(SprintDto sprintDto, Long projectId, Long sprintId) {
        Project project = projectsRestService.getValidProject(projectId);

        if (!project.isAdmin(userContext.getUser())) {
            throw new ForbiddenAccessException();
        }

        if (sprintDto.getSprintState() == SprintStateDto.CLOSED) {
            Duration timeBurned = Duration.ofMinutes(
                    AssertionUtil.requireNonNegative("timeBurned", sprintDto.getTimeBurned())
            );

            Duration finalTimePlanned = Duration.ofMinutes(
                    AssertionUtil.requireNonNegative("finalTimePlanned", sprintDto.getFinalTimePlanned())
            );

            return closeSprint(sprintId, timeBurned, finalTimePlanned);
        }

        throw new BadRequestException("Can not modify unclosed sprint");
    }

    private ResponseEntity<SprintDto> closeSprint(Long sprintId, Duration timeBurned, Duration finalTimePlanned) {
        Sprint sprint = getValidSprint(sprintId);

        if (sprint.getDateClosed() != null) {
            throw new IllegalStateException("Sprint already closed");
        }

        LocalDateTime timeNow = LocalDateTime.now();

        if (sprint.getSprintState(timeNow) != SprintState.FINISHED) {
            throw new IllegalStateException("Sprint cannot be closed before it has ended");
        }

        Sprint changedSprint = sprintService.closeSprint(sprint, timeBurned, null, finalTimePlanned, timeNow);
        return ResponseEntity.ok(sprintMapper.mapToApi(changedSprint));
    }
}
