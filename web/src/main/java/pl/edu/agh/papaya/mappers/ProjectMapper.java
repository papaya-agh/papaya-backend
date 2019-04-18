package pl.edu.agh.papaya.mappers;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import pl.edu.agh.papaya.api.model.ProjectDto;
import pl.edu.agh.papaya.model.Project;

@Component
public class ProjectMapper {

    public ProjectDto mapToApi(Project modelProject) {
        return new ProjectDto()
                .id(modelProject.getId())
                .description(modelProject.getDescription())
                .initialCoefficient(modelProject.getInitialCoefficient())
                .name(modelProject.getName());
    }

    public List<ProjectDto> mapToApi(List<Project> projects) {
        return projects.stream()
                .map(this::mapToApi)
                .collect(Collectors.toList());
    }
}
