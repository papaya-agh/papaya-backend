package pl.edu.agh.papaya.service.userinproject;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
}
