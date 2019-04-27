package pl.edu.agh.papaya.service.sprint;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.edu.agh.papaya.model.Sprint;
import pl.edu.agh.papaya.model.SprintState;
import pl.edu.agh.papaya.repository.SprintRepository;

@Service
@SuppressWarnings({"PMD.BeanMembersShouldSerialize"})
public class SprintService {

    private final EnumMap<SprintState, SprintStateQuery> sprintStateQueries = new EnumMap<>(SprintState.class);

    private final SprintRepository sprintRepository;

    @Autowired
    public SprintService(SprintRepository sprintRepository) {
        this.sprintRepository = sprintRepository;
        sprintStateQueries.put(SprintState.UPCOMING, sprintRepository::findUpcoming);
        sprintStateQueries.put(SprintState.DECLARABLE, sprintRepository::findDeclarable);
        sprintStateQueries.put(SprintState.PADDING, sprintRepository::findPadding);
        sprintStateQueries.put(SprintState.IN_PROGRESS, sprintRepository::findInProgress);
        sprintStateQueries.put(SprintState.FINISHED, sprintRepository::findFinished);
        sprintStateQueries.put(SprintState.CLOSED, sprintRepository::findClosed);
    }

    public Optional<Sprint> getById(Long id) {
        return sprintRepository.findById(id);
    }

    public List<Sprint> getByState(SprintState sprintState) {
        LocalDateTime currentTime = LocalDateTime.now();
        return getByState(sprintState, currentTime);
    }

    public List<Sprint> getByState(SprintState sprintState, LocalDateTime evaluationTime) {
        return sprintStateQueries.get(sprintState).querySprints(evaluationTime);
    }

    public List<Sprint> getByStateInProject(SprintState sprintState, Long projectId) {
        LocalDateTime currentTime = LocalDateTime.now();
        return getByStateInProject(sprintState, projectId, currentTime);
    }

    public List<Sprint> getByStateInProject(SprintState sprintState, Long projectId, LocalDateTime evaluationTime) {
        return getByState(sprintState, evaluationTime)
                .stream()
                .filter(sprint -> sprint.getProject().getId().equals(projectId))
                .collect(Collectors.toList());
    }

    public List<Sprint> getByStates(List<SprintState> sprintStates) {
        LocalDateTime currentTime = LocalDateTime.now();
        return getByStates(sprintStates, currentTime);
    }

    public List<Sprint> getByStates(List<SprintState> sprintStates, LocalDateTime evaluationTime) {
        return sprintStates.stream()
                .flatMap(sprintState -> getByState(sprintState, evaluationTime).stream())
                .collect(Collectors.toList());
    }

    public List<Sprint> getByStatesInProject(List<SprintState> sprintStates, Long projectId) {
        LocalDateTime currentTime = LocalDateTime.now();
        return getByStatesInProject(sprintStates, projectId, currentTime);
    }

    public List<Sprint> getByStatesInProject(List<SprintState> sprintStates, Long projectId,
            LocalDateTime evaluationTime) {
        return getByStates(sprintStates, evaluationTime)
                .stream()
                .filter(sprint -> sprint.getProject().getId().equals(projectId))
                .collect(Collectors.toList());
    }

    public SprintCreationWizard newSprint() {
        return new SprintCreationWizard(this);
    }

    Sprint createSprint(Sprint sprint) {
        sprintRepository.save(sprint);
        return sprint;
    }

    public Optional<Sprint> findLastInProject(Long projectId) {
        return sprintRepository.findLastInProject(projectId);
    }

    @FunctionalInterface
    private interface SprintStateQuery {

        List<Sprint> querySprints(LocalDateTime evaluationTime);
    }
}
