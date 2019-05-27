package pl.edu.agh.papaya.service.userinproject;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.papaya.model.Project;
import pl.edu.agh.papaya.model.UserInProject;
import pl.edu.agh.papaya.repository.UserInProjectRepository;
import pl.edu.agh.papaya.security.User;
import pl.edu.agh.papaya.security.UserService;

@Service
@RequiredArgsConstructor
@SuppressWarnings({"PMD.BeanMembersShouldSerialize"})
public class UserInProjectService {

    private final UserService userService;

    private final UserInProjectRepository userInProjectRepository;

    public Optional<UserInProject> getByProjectIdAndUserId(Long projectId, String userId) {
        return userInProjectRepository.findByProjectIdAndUserId(projectId, userId);
    }

    public List<User> getActiveUsersInProject(Project project) {
        return getUsers(userInProjectRepository.findActiveByProject(project));
    }

    private List<User> getUsers(List<UserInProject> usersInProject) {
        return usersInProject.stream()
                .map(UserInProject::getUserId)
                .map(userService::getUserById)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }
}
