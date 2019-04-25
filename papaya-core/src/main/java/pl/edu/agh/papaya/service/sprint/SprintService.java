package pl.edu.agh.papaya.service.sprint;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
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

    @Autowired
    public SprintService(SprintRepository sprintRepository) {
        sprintStateQueries.put(SprintState.UPCOMING, sprintRepository::findUpcoming);
        sprintStateQueries.put(SprintState.DECLARABLE, sprintRepository::findDeclarable);
        sprintStateQueries.put(SprintState.PADDING, sprintRepository::findPadding);
        sprintStateQueries.put(SprintState.IN_PROGRESS, sprintRepository::findInProgress);
        sprintStateQueries.put(SprintState.FINISHED, sprintRepository::findFinished);
        sprintStateQueries.put(SprintState.CLOSED, sprintRepository::findClosed);
    }

    public List<Sprint> findByState(SprintState sprintState) {
        LocalDateTime currentTime = LocalDateTime.now();
        return findByState(sprintState, currentTime);
    }

    public List<Sprint> findByState(SprintState sprintState, LocalDateTime evaluationTime) {
        return sprintStateQueries.get(sprintState).querySprints(evaluationTime);
    }

    public List<Sprint> findByStateInProject(SprintState sprintState, Long projectId) {
        LocalDateTime currentTime = LocalDateTime.now();
        return findByStateInProject(sprintState, projectId, currentTime);
    }

    public List<Sprint> findByStateInProject(SprintState sprintState, Long projectId, LocalDateTime evaluationTime) {
        return findByState(sprintState, evaluationTime)
                .stream()
                .filter(sprint -> sprint.getProject().getId().equals(projectId))
                .collect(Collectors.toList());
    }

    public List<Sprint> findByStates(List<SprintState> sprintStates) {
        LocalDateTime currentTime = LocalDateTime.now();
        return findByStates(sprintStates, currentTime);
    }

    public List<Sprint> findByStates(List<SprintState> sprintStates, LocalDateTime evaluationTime) {
        return sprintStates.stream()
                .flatMap(sprintState -> findByState(sprintState, evaluationTime).stream())
                .collect(Collectors.toList());
    }

    public List<Sprint> findByStatesInProject(List<SprintState> sprintStates, Long projectId) {
        LocalDateTime currentTime = LocalDateTime.now();
        return findByStatesInProject(sprintStates, projectId, currentTime);
    }

    public List<Sprint> findByStatesInProject(List<SprintState> sprintStates, Long projectId,
            LocalDateTime evaluationTime) {
        return findByStates(sprintStates, evaluationTime)
                .stream()
                .filter(sprint -> sprint.getProject().getId().equals(projectId))
                .collect(Collectors.toList());
    }

    @FunctionalInterface
    private interface SprintStateQuery {

        List<Sprint> querySprints(LocalDateTime evaluationTime);
    }
}
