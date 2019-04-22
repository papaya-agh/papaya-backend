package pl.edu.agh.papaya.service.sprint;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
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

    public List<Sprint> findByState(SprintState sprintState) {
        return findByState(sprintState, LocalDateTime.now());
    }

    public List<Sprint> findByState(SprintState sprintState, LocalDateTime evaluationTime) {
        return sprintStateQueries.get(sprintState).querySprints(evaluationTime);
    }

    public List<Sprint> findNotClosed() {
        return findNotClosed(LocalDateTime.now());
    }

    public List<Sprint> findNotClosed(LocalDateTime evaluationTime) {
        return sprintRepository.findNotClosed(evaluationTime);
    }

    @FunctionalInterface
    private interface SprintStateQuery {

        List<Sprint> querySprints(LocalDateTime evaluationTime);
    }
}
