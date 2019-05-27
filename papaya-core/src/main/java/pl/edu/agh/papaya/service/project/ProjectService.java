package pl.edu.agh.papaya.service.project;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.agh.papaya.model.Project;
import pl.edu.agh.papaya.model.UserInProject;
import pl.edu.agh.papaya.model.UserRole;
import pl.edu.agh.papaya.repository.ProjectRepository;
import pl.edu.agh.papaya.repository.UserInProjectRepository;
import pl.edu.agh.papaya.security.User;
import pl.edu.agh.papaya.security.UserContext;
import pl.edu.agh.papaya.security.UserNotAuthorizedException;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;

    private final UserInProjectRepository userInProjectRepository;

    private final UserContext userContext;

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
        return projectRepository.findActiveByUserId(userId);
    }

    public Optional<UserInProject> getUserInProject(Project project, User user) {
        return userInProjectRepository.findByProjectIdAndUserId(project.getId(), user.getId());
    }

    public boolean isUserInProject(Project project, String userId) {
        return userInProjectRepository.findActiveByProject(project)
                .stream()
                .map(UserInProject::getUserId)
                .map(Object::toString)
                .anyMatch(userId::equals);
    }

    public ProjectCreationWizard newProject() {
        return new ProjectCreationWizard(this);
    }

    Project createProject(Project project) {
        UserInProject userInProject = new UserInProject(project, userContext.getUserId(), UserRole.ADMIN);
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
                userInProjectRepository.updateUserRole(project, user.getId(), role);
            }
        } else {
            UserInProject userInProject = new UserInProject(project, user.getId(), role);
            userInProjectRepository.save(userInProject);
        }
    }

    private void validateCurrentUserAdmin(Project project) {
        if (!project.isAdmin(userContext.getUser())) {
            throw new UserNotAuthorizedException();
        }
    }

    public List<UserInProject> getUsersInProject(Project project) {
        return userInProjectRepository.findActiveByProject(project);
    }

    public void removeUser(Project project, User user) {
        validateCurrentUserAdmin(project);

        userInProjectRepository.updateUserRole(project, user.getId(), UserRole.INACTIVE);
    }

    public void updateRequestToken(Project project, String requestToken) {
        project.setRequestToken(requestToken);
        projectRepository.save(project);
    }

    public void updateAccessToken(Project project, String accessToken) {
        project.setAccessToken(accessToken);
        projectRepository.save(project);
    }

    public void updateJiraSecret(Project project, String jiraSecret) {
        project.setJiraSecret(jiraSecret);
        projectRepository.save(project);
    }

    public void updateJiraBoardId(Project project, Long jiraBoardId) {
        project.setJiraBoardId(jiraBoardId);
        projectRepository.save(project);
    }

    public void updateJiraProjectId(Project project, Long jiraProjectId) {
        project.setJiraProjectId(jiraProjectId);
        projectRepository.save(project);
    }

    public Optional<URL> getJiraUrl(Project project) {
        try {
            return Optional.of(new URL(project.getJiraUrl()));
        } catch (MalformedURLException e) {
            return Optional.empty();
        }
    }
}
