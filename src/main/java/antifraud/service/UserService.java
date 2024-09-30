package antifraud.service;

import antifraud.dto.ChangeAccessDto;
import antifraud.dto.ChangeRoleDto;
import antifraud.dto.NewUserDto;
import antifraud.dto.UserDto;

public interface UserService {
    UserDto createUser(NewUserDto newUserDto);

    UserDto[] listUsers();

    void deleteUser(String username);

    UserDto changeRole(ChangeRoleDto changeRoleDto);

    void changeAccess(ChangeAccessDto changeAccessDto);
}
