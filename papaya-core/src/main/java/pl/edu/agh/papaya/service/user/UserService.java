package pl.edu.agh.papaya.service.user;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.edu.agh.papaya.model.User;
import pl.edu.agh.papaya.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    public Optional<User> getUserByEmail(String userEmail) {
        return userRepository.findByEmail(userEmail);
    }
}
