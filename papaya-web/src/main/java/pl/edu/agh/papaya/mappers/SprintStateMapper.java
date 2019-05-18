package pl.edu.agh.papaya.mappers;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import pl.edu.agh.papaya.api.model.SprintStateDto;
import pl.edu.agh.papaya.model.SprintState;

@Component
public class SprintStateMapper implements Mapper<SprintState, SprintStateDto> {

    private static final List<SprintStateDto> ALL_SPRINT_STATE_DTOS = List.of(SprintStateDto.values());

    @Override
    public SprintStateDto mapToApi(SprintState sprintState) {
        return SprintStateDto.valueOf(sprintState.name());
    }

    public List<SprintState> mapFromApiWithDefault(List<SprintStateDto> sprintStateDtos) {
        return mapFromApi(Optional.ofNullable(sprintStateDtos).orElse(ALL_SPRINT_STATE_DTOS));
    }

    public List<SprintState> mapFromApi(List<SprintStateDto> sprintStateDtos) {
        return sprintStateDtos.stream()
                .map(this::mapFromApi)
                .collect(Collectors.toList());
    }

    public SprintState mapFromApi(SprintStateDto sprintStateDto) {
        return SprintState.valueOf(sprintStateDto.name());
    }
}
