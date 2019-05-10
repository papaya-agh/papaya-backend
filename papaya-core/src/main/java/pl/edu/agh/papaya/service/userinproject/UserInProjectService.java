package pl.edu.agh.papaya.service.userinproject;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.papaya.model.Project;
import pl.edu.agh.papaya.model.UserInProject;
import pl.edu.agh.papaya.repository.UserInProjectRepository;

@Service
@RequiredArgsConstructor
@SuppressWarnings({"PMD.BeanMembersShouldSerialize"})
public class UserInProjectService {

    private final UserInProjectRepository userInProjectRepository;

    public Optional<UserInProject> getByProjectIdAndUserId(Long projectId, Long userId) {
        return userInProjectRepository.findByProjectIdAndUserId(projectId, userId);
    }

    public List<UserInProject> getActiveUsersInProject(Project project) {
        return userInProjectRepository.findActiveByProject(project);
    }

    public List<UserInProject> getActiveUsersInProject(List<Project> projects) {
        return projects.stream()
                .flatMap(projectId -> userInProjectRepository.findActiveByProject(projectId).stream())
                .collect(Collectors.toList());
    }
}
