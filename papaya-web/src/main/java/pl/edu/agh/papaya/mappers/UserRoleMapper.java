package pl.edu.agh.papaya.mappers;

import org.springframework.stereotype.Component;
import pl.edu.agh.papaya.api.model.UserRoleDto;
import pl.edu.agh.papaya.model.UserRole;

@Component
public class UserRoleMapper {

    public UserRole mapFromApi(UserRoleDto role) {
        switch (role) {
            case ADMIN:
                return UserRole.ADMIN;

            case MEMBER:
                return UserRole.MEMBER;

            default:
                throw new IncompleteMappingException();
        }
    }

    public UserRoleDto mapToApi(UserRole role) {
        switch (role) {
            case ADMIN:
                return UserRoleDto.ADMIN;

            case MEMBER:
                return UserRoleDto.MEMBER;

            default:
                throw new IncompleteMappingException();
        }
    }
}
