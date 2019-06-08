package pl.edu.agh.papaya.service.sprint;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.edu.agh.papaya.model.Availability;
import pl.edu.agh.papaya.model.Sprint;
import pl.edu.agh.papaya.model.SprintState;
import pl.edu.agh.papaya.repository.SprintRepository;
import pl.edu.agh.papaya.service.availability.AvailabilityService;

@Service
@SuppressWarnings({"PMD.BeanMembersShouldSerialize", "MethodCount"})
public class SprintService {

    private final EnumMap<SprintState, SprintStateQuery> sprintStateQueries = new EnumMap<>(SprintState.class);

    private final SprintRepository sprintRepository;

    private final AvailabilityService availabilityService;

    @Autowired
    public SprintService(SprintRepository sprintRepository, AvailabilityService availabilityService) {
        this.sprintRepository = sprintRepository;
        this.availabilityService = availabilityService;
        sprintStateQueries.put(SprintState.UPCOMING, sprintRepository::findUpcomingInProject);
        sprintStateQueries.put(SprintState.DECLARABLE, sprintRepository::findDeclarableInProject);
        sprintStateQueries.put(SprintState.PADDING, sprintRepository::findPaddingInProject);
        sprintStateQueries.put(SprintState.IN_PROGRESS, sprintRepository::findInProgressInProject);
        sprintStateQueries.put(SprintState.FINISHED, sprintRepository::findFinishedInProject);
        sprintStateQueries.put(SprintState.CLOSED, sprintRepository::findClosedInProject);
    }

    public List<Sprint> getAllDeclarable(LocalDateTime evaluationTime) {
        return sprintRepository.findAllDeclarable(evaluationTime);
    }

    public List<Sprint> getAllPadding(LocalDateTime evaluationTime) {
        return sprintRepository.findAllPadding(evaluationTime);
    }

    public List<Sprint> getByStatesInProject(List<SprintState> sprintStates, Long projectId,
            LocalDateTime evaluationTime) {
        return sprintStates.stream()
                .flatMap(sprintState -> getByStateInProject(sprintState, projectId, evaluationTime).stream())
                .sorted(Comparator.comparing(sprint -> sprint.getEnrollmentPeriod().getStart()))
                .collect(Collectors.toList());
    }

    public List<Sprint> getByStateInProject(SprintState sprintState, Long projectId, LocalDateTime evaluationTime) {
        return sprintStateQueries.get(sprintState).querySprints(evaluationTime, projectId, Pageable.unpaged());
    }

    public Optional<Sprint> getFirstByStatesInProject(List<SprintState> sprintStates, Long projectId,
            LocalDateTime evaluationTime) {
        return sprintStates.stream()
                .flatMap(sprintState -> getFirstByStateInProject(sprintState, projectId, evaluationTime).stream())
                .min(Comparator.comparing(sprint -> sprint.getEnrollmentPeriod().getStart()));
    }

    public Optional<Sprint> getFirstByStateInProject(SprintState sprintState, Long projectId,
            LocalDateTime evaluationTime) {
        return sprintStateQueries.get(sprintState)
                .querySprints(evaluationTime, projectId, PageRequest.of(0, 1))
                .stream()
                .findFirst();
    }

    public SprintCreationWizard newSprint() {
        return new SprintCreationWizard(this);
    }

    Sprint createSprint(Sprint sprint) {
        return sprintRepository.save(sprint);
    }

    public Optional<Sprint> getLastInProject(Long projectId) {
        return sprintRepository.findFirstByProjectIdOrderByDurationPeriodStartDesc(projectId);
    }

    public Sprint closeSprint(Sprint sprint, Duration timeBurned, Duration estimatedTimePlanned,
            Duration finalTimePlanned, LocalDateTime dateClosed) {
        if (sprint.getDateClosed() != null) {
            throw new IllegalStateException("Sprint already closed");
        }

        if (sprint.getSprintState(dateClosed) != SprintState.FINISHED) {
            throw new IllegalStateException("Sprint cannot be closed before it has ended");
        }

        sprint.setDateClosed(dateClosed);
        updateSprintStats(sprint, estimatedTimePlanned, finalTimePlanned, timeBurned);

        return sprintRepository.save(sprint);
    }

    public void updateSprintStats(Sprint sprint, Duration estimatedTimePlanned, Duration finalTimePlanned,
            Duration timeBurned) {
        sprint.setEstimatedTimePlanned(estimatedTimePlanned);
        sprint.setFinalTimePlanned(finalTimePlanned);
        sprint.setTimeBurned(timeBurned);

        Duration totalDeclaredTime = getTotalDeclaredTimeBySprint(sprint);
        sprint.updateCoefficient(totalDeclaredTime);

        sprint.setAverageCoefficientCache(computeAverageSprintCoefficient(sprint));

        sprintRepository.save(sprint);
    }

    private double computeAverageSprintCoefficient(Sprint sprint) {
        // dates before the end of this sprint because this one also has to be included
        return sprintRepository.findAverageSprintCoefficientInProjectUpToDate(
                sprint.getProject().getId(),
                sprint.getDurationPeriod().getEnd())
                .orElseGet(() -> getDefaultSprintCoefficient(sprint));
    }

    private double getDefaultSprintCoefficient(Sprint sprint) {
        return sprint.getProject().getInitialCoefficient();
    }

    public Duration getTotalDeclaredTimeBySprint(Sprint sprint) {
        return availabilityService.getBySprintId(sprint.getId())
                .stream()
                .map(Availability::getTimeAvailable)
                .reduce(Duration.ZERO, Duration::plus);
    }

    public Optional<Sprint> getPrecedingSprint(Sprint sprint, List<SprintState> sprintStates,
            LocalDateTime evaluationTime) {
        return getPrecedingSprints(sprint, sprintStates, evaluationTime).stream().findFirst();
    }

    public List<Sprint> getPrecedingSprints(Sprint sprint, List<SprintState> sprintStates,
            LocalDateTime evaluationTime) {
        return sprintRepository.findPrecedingSprints(sprint, Pageable.unpaged()).stream()
                .filter(precedingSprint -> sprintStates.contains(precedingSprint.getSprintState(evaluationTime)))
                .collect(Collectors.toList());
    }

    public Optional<Sprint> getFollowingSprint(Sprint sprint, List<SprintState> sprintStates,
            LocalDateTime evaluationTime) {
        return getFollowingSprints(sprint, sprintStates, evaluationTime).stream().findFirst();
    }

    public List<Sprint> getFollowingSprints(Sprint sprint, List<SprintState> sprintStates,
            LocalDateTime evaluationTime) {
        return sprintRepository.findFollowingSprints(sprint, Pageable.unpaged()).stream()
                .filter(followingSprint -> sprintStates.contains(followingSprint.getSprintState(evaluationTime)))
                .collect(Collectors.toList());
    }

    public double getPrevSprintAverageCoefficient(Long sprintId) {
        return getById(sprintId)
                .map(this::getPrevSprintAverageCoefficient)
                .orElseThrow(() -> new IllegalArgumentException("No such sprint was found"));
    }

    public double getPrevSprintAverageCoefficient(Sprint sprint) {
        return sprintRepository.findPrecedingSprint(sprint)
                .map(Sprint::getAverageCoefficientCache)
                .orElseGet(() -> getDefaultSprintCoefficient(sprint));
    }

    public Optional<Sprint> getById(Long id) {
        return sprintRepository.findById(id);
    }

    @FunctionalInterface
    private interface SprintStateQuery {

        List<Sprint> querySprints(LocalDateTime evaluationTime, Long projectId, Pageable pageable);
    }
}
