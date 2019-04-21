package pl.edu.agh.papaya.mappers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.edu.agh.papaya.api.model.SprintDto;
import pl.edu.agh.papaya.model.Sprint;

@Component
public class SprintMapper implements Mapper<Sprint, SprintDto> {

    @Autowired
    private LocalDateTimePeriodMapper localDateTimePeriodMapper;

    @Autowired
    private SprintStateMapper sprintStateMapper;

    @Override
    public List<SprintDto> mapToApi(List<Sprint> modelItems) {
        LocalDateTime currentTime = LocalDateTime.now();
        return mapToApi(modelItems, currentTime);
    }

    public List<SprintDto> mapToApi(List<Sprint> modelItems, LocalDateTime evaluationTime) {
        return modelItems.stream()
                .map(modelSprint -> mapToApi(modelSprint, evaluationTime))
                .collect(Collectors.toList());
    }

    public SprintDto mapToApi(Sprint modelSprint, LocalDateTime evaluationTime) {
        return new SprintDto()
                .id(modelSprint.getId())
                .enrollmentPeriod(localDateTimePeriodMapper.mapToApi(modelSprint.getEnrollmentPeriod()))
                .durationPeriod(localDateTimePeriodMapper.mapToApi(modelSprint.getDurationPeriod()))
                .dateClosed(modelSprint.getDateClosed())
                .timeBurned(Optional.ofNullable(modelSprint.getTimeBurned()).map(Duration::toMinutes).orElse(null))
                .timePlanned(Optional.ofNullable(modelSprint.getTimePlanned()).map(Duration::toMinutes).orElse(null))
                .sprintState(sprintStateMapper.mapToApi(modelSprint.getSprintState(evaluationTime)));
    }

    @Override
    public SprintDto mapToApi(Sprint modelSprint) {
        LocalDateTime currentTime = LocalDateTime.now();
        return mapToApi(modelSprint, currentTime);
    }
}
