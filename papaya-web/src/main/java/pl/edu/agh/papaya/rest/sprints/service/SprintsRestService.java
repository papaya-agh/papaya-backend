package pl.edu.agh.papaya.rest.sprints.service;

import com.google.common.collect.Lists;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import pl.edu.agh.papaya.api.model.JiraSprintDto;
import pl.edu.agh.papaya.api.model.SortingDirection;
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
import pl.edu.agh.papaya.model.SprintStats;
import pl.edu.agh.papaya.model.UserInProject;
import pl.edu.agh.papaya.rest.jira.service.JiraRestService;
import pl.edu.agh.papaya.rest.projects.service.ProjectsRestService;
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

    private final SprintService sprintService;

    private final SprintStateMapper sprintStateMapper;

    private final SprintMapper sprintMapper;

    private final ProjectsRestService projectsRestService;

    private final UserContext userContext;

    private final LocalDateTimePeriodMapper localDateTimePeriodMapper;

    private final UserAvailabilityMapper userAvailabilityMapper;

    private final JiraRestService jiraRestService;

    public ResponseEntity<List<SprintDto>> getSprints(Long projectId, List<SprintStateDto> sprintStateDtos,
            SortingDirection sortingDirection, Long limit) {

        Project project = projectsRestService.getValidProject(projectId);

        final LocalDateTime currentTime = LocalDateTime.now();

        List<SprintState> sprintStates = sprintStateMapper.mapFromApiWithDefault(sprintStateDtos);

        List<Sprint> sprints = sprintService.getByStatesInProject(sprintStates, project.getId(), currentTime);

        if (SortingDirection.DESC.equals(sortingDirection)) {
            sprints = Lists.reverse(sprints);
        }

        long limitOrAll = limit != null ? limit : sprints.size();

        List<SprintDto> sprintDtos =
                sprintMapper.mapToApi(sprints.stream().limit(limitOrAll).collect(Collectors.toList()), currentTime);

        return ResponseEntity.ok(sprintDtos);
    }

    public ResponseEntity<SprintDto> getNextSprint(Long projectId, Long sprintId,
            List<SprintStateDto> sprintStateDtos) {
        Sprint sprint = getValidSprint(projectId, sprintId);

        List<SprintState> sprintStates = sprintStateMapper.mapFromApiWithDefault(sprintStateDtos);

        LocalDateTime currentTime = LocalDateTime.now();

        Sprint nextSprint = sprintService.getFollowingSprint(sprint, sprintStates, currentTime)
                .orElseThrow(ResourceNotFoundException::new);

        SprintDto nextSprintDto = sprintMapper.mapToApi(nextSprint, currentTime);

        return ResponseEntity.ok(nextSprintDto);
    }

    public Sprint getValidSprint(Long projectId, Long sprintId) {
        Project project = projectsRestService.getValidProject(projectId);
        Sprint sprint = sprintService.getById(sprintId).orElseThrow(ResourceNotFoundException::new);

        if (!project.isSprintInProject(sprint)) {
            throw new ResourceNotFoundException();
        }

        if (!sprint.getProject().isUserInProject(userContext.getUser())) {
            throw new ForbiddenAccessException();
        }

        return sprint;
    }

    public ResponseEntity<SprintDto> getPreviousSprint(Long projectId, Long sprintId,
            List<SprintStateDto> sprintStateDtos) {
        Sprint sprint = getValidSprint(projectId, sprintId);

        List<SprintState> sprintStates = sprintStateMapper.mapFromApiWithDefault(sprintStateDtos);

        LocalDateTime currentTime = LocalDateTime.now();

        Sprint previousSprint = sprintService.getPrecedingSprint(sprint, sprintStates, currentTime)
                .orElseThrow(ResourceNotFoundException::new);

        SprintDto previousSprintDto = sprintMapper.mapToApi(previousSprint, currentTime);

        return ResponseEntity.ok(previousSprintDto);
    }

    @SuppressWarnings({"MethodLength"})
    public ResponseEntity<SprintDto> addSprint(SprintDto sprintDto, Long projectId) {
        Project project = projectsRestService.getValidProject(projectId);

        if (!project.isAdmin(userContext.getUser())) {
            throw new ForbiddenAccessException();
        }

        LocalDateTimePeriod enrollmentPeriod = localDateTimePeriodMapper.mapFromApi(sprintDto.getEnrollmentPeriod());
        LocalDateTimePeriod durationPeriod = localDateTimePeriodMapper.mapFromApi(sprintDto.getDurationPeriod());

        String name = sprintDto.getName();
        String notificationMessage = sprintDto.getNotificationMessage();

        Sprint created = sprintService.newSprint()
                .withProject(project)
                .withName(name)
                .withNotificationMessage(notificationMessage)
                .withEnrollmentPeriod(enrollmentPeriod)
                .withDurationPeriod(durationPeriod)
                .create();
        return ResponseEntity.ok(sprintMapper.mapToApi(created));
    }

    public ResponseEntity<SprintSummaryDto> getSprintSummary(Long projectId, Long sprintId, Long jiraSprintId) {
        Sprint sprint = getValidSprint(projectId, sprintId);

        if (!sprint.getProject().isAdmin(userContext.getUser())) {
            throw new ForbiddenAccessException();
        }

        Project project = sprint.getProject();
        if (jiraSprintId != null) {
            Optional<SprintStats> sprintStats = jiraRestService.updateFromJira(project, jiraSprintId);
            sprintStats.ifPresent(stats -> sprintService.updateSprintStats(sprint, stats.getEstimatedTimePlanned(),
                    stats.getFinalTimePlanned(), stats.getTimeBurned()));
        }
        List<Availability> availabilities = sprint.getAvailabilities();
        List<UserAvailabilityDto> userAvailabilityDtos = getUserAvailabilityDtos(project, availabilities);

        Duration totalDeclaredTime = availabilities.stream()
                .map(Availability::getTimeAvailable)
                .reduce(Duration.ZERO, Duration::plus);

        return ResponseEntity.ok(createSprintSummaryDto(sprint, userAvailabilityDtos, totalDeclaredTime));
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
        List<String> userIds = project.getUsersInProject().stream()
                .map(UserInProject::getUserId)
                .collect(Collectors.toList());

        return userIds.stream()
                .filter(userId -> availabilities.stream()
                        .noneMatch(availability -> userId.equals(availability.getUserInProject().getUserId())))
                .map(userAvailabilityMapper::emptyAvailability).collect(Collectors.toList());
    }

    public ResponseEntity<SprintDto> modifySprint(SprintDto sprintDto, Long projectId, Long sprintId) {
        Sprint sprint = getValidSprint(projectId, sprintId);

        if (!sprint.getProject().isAdmin(userContext.getUser())) {
            throw new ForbiddenAccessException();
        }

        if (sprintDto.getSprintState() == SprintStateDto.CLOSED) {
            return closeSprint(sprint, sprintDto.getTimeBurned(), sprintDto.getEstimatedTimePlanned(),
                    sprintDto.getFinalTimePlanned());
        } else if (sprintDto.getSprintState() == SprintStateDto.IN_PROGRESS) {
            return openSprint(sprint);
        }

        throw new BadRequestException("The only available sprint modification is closing the sprint");
    }

    private ResponseEntity<SprintDto> closeSprint(Sprint sprint, Long timeBurnedInMinutes,
            Long estimatedTimePlannedInMinutes, Long finalTimePlannedInMinutes) {
        LocalDateTime timeNow = LocalDateTime.now();
        try {
            Duration timeBurned = Duration.ofMinutes(
                    AssertionUtil.requireNonNegative("timeBurned", timeBurnedInMinutes));
            Duration estimatedTimePlanned = Duration.ofMinutes(
                    AssertionUtil.requireNonNegative("estimatedTimePlanned", estimatedTimePlannedInMinutes));
            Duration finalTimePlanned = Duration.ofMinutes(
                    AssertionUtil.requireNonNegative("finalTimePlanned", finalTimePlannedInMinutes));

            Sprint changedSprint =
                    sprintService.closeSprint(sprint, timeBurned, estimatedTimePlanned, finalTimePlanned, timeNow);

            return ResponseEntity.ok(sprintMapper.mapToApi(changedSprint));
        } catch (IllegalStateException e) {
            throw new BadRequestException(e);
        }
    }

    private ResponseEntity<SprintDto> openSprint(Sprint sprint) {
        LocalDateTime timeNow = LocalDateTime.now();
        try {
            Sprint changedSprint =
                    sprintService.openSprint(sprint, timeNow);
            return ResponseEntity.ok(sprintMapper.mapToApi(changedSprint));
        } catch (IllegalStateException e) {
            throw new BadRequestException(e);
        }
    }

    public ResponseEntity<List<JiraSprintDto>> getAvailableJiraSprints(Long projectId, Long sprintId) {
        Sprint sprint = getValidSprint(projectId, sprintId);

        List<JiraSprintDto> jiraSprintDtos = jiraRestService.getAvailableJiraSprints(sprint);

        return ResponseEntity.ok(jiraSprintDtos);
    }

    public ResponseEntity<Void> removeSprint(Long projectId, Long sprintId) {
        Sprint sprint = getValidSprint(projectId, sprintId);

        if (!sprint.getProject().isAdmin(userContext.getUser())) {
            throw new ForbiddenAccessException();
        }
        sprintService.removeSprint(sprint);

        return ResponseEntity.noContent().build();
    }
}
