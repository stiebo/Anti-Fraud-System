package antifraud.mapper;

import antifraud.domain.User;
import antifraud.dto.UserDto;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserDto toDto (User user) {
        return new UserDto(user.getId(), user.getName(),
                user.getUsername(), user.getRole().getName(),
                user.getLocked() ? "LOCKED" : "UNLOCKED");
    }
}
