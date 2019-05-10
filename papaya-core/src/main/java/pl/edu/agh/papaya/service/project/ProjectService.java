package pl.edu.agh.papaya.service.project;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.agh.papaya.model.Project;
import pl.edu.agh.papaya.model.User;
import pl.edu.agh.papaya.model.UserInProject;
import pl.edu.agh.papaya.model.UserRole;
import pl.edu.agh.papaya.repository.ProjectRepository;
import pl.edu.agh.papaya.repository.UserInProjectRepository;
import pl.edu.agh.papaya.security.UserContext;
import pl.edu.agh.papaya.security.UserNotAuthorizedException;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserInProjectRepository userInProjectRepository;

    @Autowired
    private UserContext userContext;

    public Optional<Project> getProjectById(Long id) {
        return projectRepository.findById(id);
    }

    /**
     * <em>Note: this method doesn't validate the given ID, when no user with this ID exists,
     * the resulting collection is empty.</em>
     *
     * @param userId id of the user
     *
     * @return projects which the user is a part of
     */
    public List<Project> getUserProjects(String userId) {
        long parsedUserId;
        try {
            parsedUserId = Long.parseLong(userId);
        } catch (NumberFormatException e) {
            return Collections.emptyList();
        }

        return projectRepository.findActiveByUserId(parsedUserId);
    }

    public Optional<UserInProject> getUserInProject(Project project, User user) {
        return userInProjectRepository.findByProjectIdAndUserId(project.getId(), user.getId());
    }

    public boolean isUserInProject(Project project, String userId) {
        return userInProjectRepository.findByProject(project)
                .stream()
                .filter(up -> up.getUserRole() != UserRole.INACTIVE)
                .map(UserInProject::getUser)
                .map(User::getId)
                .map(Object::toString)
                .anyMatch(userId::equals);
    }

    public ProjectCreationWizard newProject() {
        return new ProjectCreationWizard(this);
    }

    Project createProject(Project project) {
        UserInProject userInProject = new UserInProject(project, userContext.getUser(), UserRole.ADMIN);
        project.setUsersInProject(Collections.singletonList(userInProject));

        projectRepository.save(project);
        userInProjectRepository.save(userInProject);
        return project;
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void setUserRole(Project project, User user, UserRole role) {
        validateCurrentUserAdmin(project);

        Optional<UserInProject> existing =
                userInProjectRepository.findByProjectIdAndUserId(project.getId(), user.getId());

        if (existing.isPresent()) {
            if (existing.get().getUserRole() != role) {
                userInProjectRepository.updateUserRole(project, user, role);
            }
        } else {
            UserInProject userInProject = new UserInProject(project, user, role);
            userInProjectRepository.save(userInProject);
        }
    }

    private void validateCurrentUserAdmin(Project project) {
        if (!project.isAdmin(userContext.getUser())) {
            throw new UserNotAuthorizedException();
        }
    }

    public List<UserInProject> getUsersInProject(Project project) {
        return userInProjectRepository.findByProject(project).stream()
                .filter(UserInProject::isUserActive)
                .collect(Collectors.toList());
    }

    public void removeUser(Project project, User user) {
        validateCurrentUserAdmin(project);

        userInProjectRepository.updateUserRole(project, user, UserRole.INACTIVE);
    }
}
