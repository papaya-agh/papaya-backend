package pl.edu.agh.papaya.service.project;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.edu.agh.papaya.model.Project;
import pl.edu.agh.papaya.model.User;
import pl.edu.agh.papaya.model.UserInProject;
import pl.edu.agh.papaya.model.UserRole;
import pl.edu.agh.papaya.repository.ProjectRepository;
import pl.edu.agh.papaya.repository.UserInProjectRepository;
import pl.edu.agh.papaya.security.UserContext;

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
        try {
            return projectRepository.findByUserId(Long.parseLong(userId));
        } catch (NumberFormatException e) {
            return Collections.emptyList();
        }
    }

    public boolean isUserInProject(Project project, String userId) {
        return project.getUsersInProject()
                .stream()
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
}
