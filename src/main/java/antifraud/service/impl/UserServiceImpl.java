package antifraud.service.impl;

import antifraud.domain.User;
import antifraud.dto.ChangeAccessDto;
import antifraud.dto.ChangeRoleDto;
import antifraud.dto.NewUserDto;
import antifraud.dto.UserDto;
import antifraud.exception.RoleAlreadyProvided;
import antifraud.exception.UnableToLockAdminException;
import antifraud.exception.UserExistsException;
import antifraud.exception.UserNotFoundException;
import antifraud.mapper.UserMapper;
import antifraud.repository.RoleRepository;
import antifraud.repository.UserRepository;
import antifraud.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;
    private final UserMapper mapper;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder encoder,
                           UserMapper mapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
        this.mapper = mapper;
    }

    @Override
    public UserDto createUser(NewUserDto newUserDto) throws UserExistsException {
        if (userRepository.existsByUsernameIgnoreCase(newUserDto.username())) {
            throw new UserExistsException();
        }
        User user = new User()
                .setName(newUserDto.name())
                .setUsername(newUserDto.username())
                .setPassword(encoder.encode(newUserDto.password()));
        if (userRepository.count() == 0) {
            user.setRole(roleRepository.findByName("ADMINISTRATOR"));
            user.setLocked(false);
        } else {
            user.setRole(roleRepository.findByName("MERCHANT"));
            user.setLocked(true);
        }
        User savedUser = userRepository.save(user);
        return mapper.toDto(savedUser);
    }

    @Override
    public List<UserDto> listUsers() {
        List<User> userList = userRepository.findAllByOrderByIdAsc();
        return userList.stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public void deleteUser(String username) throws UserNotFoundException {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(UserNotFoundException::new);
        userRepository.delete(user);
    }

    @Override
    public UserDto changeRole(ChangeRoleDto changeRoleDto)
            throws UserNotFoundException, RoleAlreadyProvided {
        User user = userRepository.findByUsernameIgnoreCase(changeRoleDto.username())
                .orElseThrow(UserNotFoundException::new);
        if (user.getRole().getName().equals(changeRoleDto.role())) {
            throw new RoleAlreadyProvided();
        }
        user.setRole(roleRepository.findByName(changeRoleDto.role()));
        User user1 = userRepository.save(user);
        return mapper.toDto(user1);
    }

    @Override
    public void changeAccess(ChangeAccessDto changeAccessDto)
            throws UserNotFoundException, UnableToLockAdminException {
        User user = userRepository.findByUsernameIgnoreCase(changeAccessDto.username())
                .orElseThrow(UserNotFoundException::new);
        if (user.getRole().getName().equals("ADMINISTRATOR")) {
            throw new UnableToLockAdminException();
        }
        user.setLocked(changeAccessDto.operation().equals("LOCK"));
        userRepository.save(user);
    }
}
