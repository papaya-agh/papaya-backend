package pl.edu.agh.papaya.rest.sprints.controller;

import com.google.common.collect.Lists;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.agh.papaya.api.model.SprintDto;
import pl.edu.agh.papaya.api.model.SprintStateDto;
import pl.edu.agh.papaya.mappers.LocalDateTimePeriodMapper;
import pl.edu.agh.papaya.mappers.SprintMapper;
import pl.edu.agh.papaya.mappers.SprintStateMapper;
import pl.edu.agh.papaya.model.LocalDateTimePeriod;
import pl.edu.agh.papaya.model.Project;
import pl.edu.agh.papaya.model.Sprint;
import pl.edu.agh.papaya.model.SprintState;
import pl.edu.agh.papaya.service.sprint.SprintService;

@SuppressWarnings({"PMD.BeanMembersShouldSerialize"})
@RestController
public class SprintsController {

    private static final List<SprintStateDto> ALL_SPRINT_STATE_DTOS = Lists.newArrayList(SprintStateDto.values());

    @Autowired
    private SprintService sprintService;

    @Autowired
    private SprintStateMapper sprintStateMapper;

    @Autowired
    private SprintMapper sprintMapper;

    public ResponseEntity<List<SprintDto>> getSprints(Project project, @Valid List<SprintStateDto> sprintStateDtos) {
        final LocalDateTime currentTime = LocalDateTime.now();

        List<SprintState> sprintStates = sprintStateMapper.mapFromApi(
                Optional.ofNullable(sprintStateDtos).orElse(ALL_SPRINT_STATE_DTOS));

        List<SprintDto> sprints = sprintService.getByStatesInProject(sprintStates, project.getId(), currentTime)
                .stream()
                .sorted(Comparator.comparing(sprint -> sprint.getEnrollmentPeriod().getStart()))
                .map(sprint -> sprintMapper.mapToApi(sprint, currentTime))
                .collect(Collectors.toList());

        return ResponseEntity.ok(sprints);
    }

    public ResponseEntity<SprintDto> addSprint(Project project, @Valid SprintDto request) {
        var periodMapper = new LocalDateTimePeriodMapper();
        LocalDateTimePeriod enrollmentPeriod = periodMapper.mapFromApi(request.getEnrollmentPeriod());
        LocalDateTimePeriod durationPeriod = periodMapper.mapFromApi(request.getDurationPeriod());

        Optional<Sprint> lastSprintOpt = sprintService.findLastInProject(project.getId());
        lastSprintOpt.ifPresent(lastSprint -> {
            if (!enrollmentPeriod.isAfter(lastSprint.getDurationPeriod())) {
                throw new IllegalStateException("cannot create sprint starting before the last one ended");
            }
        });

        Sprint created = sprintService.newSprint()
                .withProject(project)
                .withEnrollmentPeriod(enrollmentPeriod)
                .withDurationPeriod(durationPeriod)
                .withTimePlanned(Duration.ofMinutes(request.getTimePlanned()))
                .create();
        return ResponseEntity.ok(sprintMapper.mapToApi(created));
    }
}
