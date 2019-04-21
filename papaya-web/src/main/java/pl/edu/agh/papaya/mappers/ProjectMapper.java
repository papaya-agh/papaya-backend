package pl.edu.agh.papaya.mappers;

import org.springframework.stereotype.Component;
import pl.edu.agh.papaya.api.model.ProjectDto;
import pl.edu.agh.papaya.model.Project;

@Component
public class ProjectMapper implements Mapper<Project, ProjectDto> {

    @Override
    public ProjectDto mapToApi(Project modelProject) {
        return new ProjectDto()
                .id(modelProject.getId())
                .description(modelProject.getDescription())
                .initialCoefficient(modelProject.getInitialCoefficient())
                .name(modelProject.getName());
    }
}
