package pl.edu.agh.papaya.rest.projects.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.validation.Valid;
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

    public ResponseEntity<SprintDto> addSprint(@Valid SprintDto sprintDto, Long projectId) {
        Project project = projectsRestService.getValidProject(projectId);

        if (!project.isAdmin(userContext.getUser())) {
            throw new ForbiddenAccessException();
        }

        LocalDateTimePeriod enrollmentPeriod = localDateTimePeriodMapper.mapFromApi(sprintDto.getEnrollmentPeriod());
        LocalDateTimePeriod durationPeriod = localDateTimePeriodMapper.mapFromApi(sprintDto.getDurationPeriod());

        if (isInOverlapWithLastSprint(project, durationPeriod)) {
            throw new IllegalStateException("Cannot create sprint starting before the last one ended");
        }

        @SuppressWarnings({"MultipleStringLiterals"})
        Long timePlanned = AssertionUtil.requireNonNegative("timePlanned", sprintDto.getTimePlanned());

        Sprint created = sprintService.newSprint()
                .withProject(project)
                .withEnrollmentPeriod(enrollmentPeriod)
                .withDurationPeriod(durationPeriod)
                .withTimePlanned(Duration.ofMinutes(timePlanned))
                .create();
        return ResponseEntity.ok(sprintMapper.mapToApi(created));
    }

    private ResponseEntity<SprintDto> closeSprint(Long sprintId, Duration timeBurned, Duration timePlanned) {
        Sprint sprint = getValidSprint(sprintId);

        if (sprint.getDateClosed() != null) {
            throw new IllegalStateException("Sprint already closed");
        }

        LocalDateTime timeNow = LocalDateTime.now();

        if (sprint.getSprintState(timeNow) != SprintState.FINISHED) {
            throw new IllegalStateException("Sprint cannot be closed before it has ended");
        }

        Sprint changedSprint = sprintService.closeSprint(sprint, timeBurned, timePlanned, timeNow);
        return ResponseEntity.ok(sprintMapper.mapToApi(changedSprint));
    }

    public ResponseEntity<SprintSummaryDto> getSprintSummary(Long projectId, Long sprintId) {
        Sprint sprint = getValidSprint(sprintId);
        Project project = projectsRestService.getValidProject(projectId);

        if (project.isAdmin(userContext.getUser())) {
            throw new ForbiddenAccessException();
        }

        List<Availability> availabilities = sprint.getAvailabilities();
        List<UserAvailabilityDto> userAvailabilityDtos = userAvailabilityMapper.mapToApi(availabilities);
        Duration totalAvailableTime = availabilities.stream()
                .map(Availability::getTimeAvailable)
                .reduce(Duration.ZERO, Duration::plus);

        //to be expanded in issue IO2019TEAM-100
        double coefficient = sprint.getTimePlanned().dividedBy(sprint.getTimeBurned());

        return ResponseEntity.ok(new SprintSummaryDto().membersAvailability(userAvailabilityDtos)
                .totalAvailableTime(totalAvailableTime.toMinutes())
                .sprintCoefficient(coefficient)
        );
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

            @SuppressWarnings({"MultipleStringLiterals"})
            Duration timePlanned = Duration.ofMinutes(
                    AssertionUtil.requireNonNegative("timePlanned", sprintDto.getTimePlanned())
            );

            return closeSprint(sprintId, timeBurned, timePlanned);
        }

        throw new BadRequestException("Can not modify unclosed sprint");
    }

    private Sprint getValidSprint(Long sprintId) {
        return sprintService.getById(sprintId)
                .orElseThrow(ResourceNotFoundException::new);
    }

    public boolean isInOverlapWithLastSprint(Project project, LocalDateTimePeriod durationPeriod) {
        Optional<Sprint> lastSprintOpt = sprintService.getLastInProject(project.getId());
        return lastSprintOpt.isEmpty() || durationPeriod.isAfter(lastSprintOpt.get().getDurationPeriod());
    }
}
