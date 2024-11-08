package antifraud.service.impl;

import antifraud.domain.Role;
import antifraud.domain.SuspiciousIp;
import antifraud.domain.User;
import antifraud.dto.ChangeAccessDto;
import antifraud.dto.NewUserDto;
import antifraud.dto.UserDto;
import antifraud.dto.ChangeRoleDto;
import antifraud.exception.RoleAlreadyProvided;
import antifraud.exception.UnableToLockAdminException;
import antifraud.exception.UserExistsException;
import antifraud.exception.UserNotFoundException;
import antifraud.mapper.UserMapper;
import antifraud.repository.RoleRepository;
import antifraud.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder encoder;

    private UserServiceImpl userService;

    private NewUserDto newUserDto;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserServiceImpl(userRepository, roleRepository, encoder, new UserMapper());

        newUserDto = new NewUserDto("John", "john_doe", "password123");
        user = new User()
                .setId(1L)
                .setName("John")
                .setUsername("john_doe")
                .setPassword("encoded_password");
    }

    // Test for createUser()
    @Test
    void testCreateUserThrowUserExistsExceptionWhenUserAlreadyExists() {
        when(userRepository.existsByUsernameIgnoreCase(newUserDto.username())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(newUserDto))
                .isInstanceOf(UserExistsException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void testCreateUserCreateAdministratorWhenFirstUser() {
        Role role = new Role().setId(1L).setName("ADMINISTRATOR");
        user.setRole(role);
        user.setLocked(false);
        when(userRepository.existsByUsernameIgnoreCase(newUserDto.username())).thenReturn(false);
        when(userRepository.count()).thenReturn(0L);
        when(encoder.encode(newUserDto.password())).thenReturn("encoded_password");
        when(roleRepository.findByName("ADMINISTRATOR")).thenReturn(role);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto result = userService.createUser(newUserDto);

        assertEquals(user.getId(), result.id());
        assertEquals(user.getName(), result.name());
        assertEquals(user.getUsername(), result.username());
        assertEquals(user.getRole().getName(), result.role());

        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userArgumentCaptor.capture());
        User savedUser = userArgumentCaptor.getValue();
        assertEquals(user.getName(), savedUser.getName());
        assertEquals(user.getUsername(), savedUser.getUsername());
        assertEquals(user.getPassword(), savedUser.getPassword());
        assertEquals(user.getRole().getName(), savedUser.getRole().getName());
        assertEquals(user.getLocked(), savedUser.getLocked());
    }

    @Test
    void shouldCreateMerchantWhenNotFirstUser() {
        Role role = new Role().setId(2L).setName("MERCHANT");
        user.setRole(role);
        user.setLocked(true);

        when(userRepository.existsByUsernameIgnoreCase(newUserDto.username())).thenReturn(false);
        when(userRepository.count()).thenReturn(1L);
        when(encoder.encode(newUserDto.password())).thenReturn("encoded_password");
        when(roleRepository.findByName("MERCHANT")).thenReturn(role);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto result = userService.createUser(newUserDto);

        assertEquals(user.getId(), result.id());
        assertEquals(user.getName(), result.name());
        assertEquals(user.getUsername(), result.username());
        assertEquals(user.getRole().getName(), result.role());
        assertEquals(user.getLocked(), result.status().equals("LOCKED"));

        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userArgumentCaptor.capture());
        User savedUser = userArgumentCaptor.getValue();
        assertEquals(user.getName(), savedUser.getName());
        assertEquals(user.getUsername(), savedUser.getUsername());
        assertEquals(user.getPassword(), savedUser.getPassword());
        assertEquals(user.getRole().getName(), savedUser.getRole().getName());
        assertEquals(user.getLocked(), savedUser.getLocked());
    }

    // Test for listUsers()
    @Test
    void shouldReturnListOfUsers() {
        Role role = new Role().setId(1L).setName("MERCHANT");
        List<User> users = Arrays.asList(
                new User().setId(1L).setName("John").setUsername("john_doe").setRole(role).setLocked(true),
                new User().setId(2L).setName("Jane").setUsername("jane_doe").setRole(role).setLocked(false)
        );

        UserDto userDto1 = new UserDto(1L, "John", "john_doe", "MERCHANT", "LOCKED");
        UserDto userDto2 = new UserDto(2L, "Jane", "jane_doe", "MERCHANT", "UNLOCKED");

        when(userRepository.findAllByOrderByIdAsc()).thenReturn(users);

        List<UserDto> result = userService.listUsers();

        assertThat(result).containsExactly(userDto1, userDto2);
    }

    // Test for deleteUser()
    @Test
    void shouldDeleteUser() {
        User user = new User().setUsername("john_doe");

        when(userRepository.findByUsernameIgnoreCase("john_doe")).thenReturn(Optional.of(user));

        userService.deleteUser("john_doe");

        verify(userRepository).delete(user);
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenUserToDeleteNotFound() {
        when(userRepository.findByUsernameIgnoreCase("unknown_user")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser("unknown_user"))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).delete(any(User.class));
    }

    // Test for changeRole()
    @Test
    void shouldChangeUserRole() {
        ChangeRoleDto changeRoleDto = new ChangeRoleDto("john_doe", "SUPPORT");
        User userMerchant = new User().setId(1L).setName("John").setUsername("john_doe")
                .setRole(new Role().setName("MERCHANT")).setLocked(true);
        User userSupport = new User().setId(1L).setName("John").setUsername("john_doe")
                .setRole(new Role().setName("SUPPORT")).setLocked(true);

        UserDto userDto = new UserDto(1L, "John", "john_doe", "SUPPORT", "LOCKED");

        when(userRepository.findByUsernameIgnoreCase("john_doe")).thenReturn(Optional.of(userMerchant));
        when(roleRepository.findByName("SUPPORT")).thenReturn(new Role().setName("SUPPORT"));
        when(userRepository.save(any(User.class))).thenReturn(userSupport);

        UserDto result = userService.changeRole(changeRoleDto);

        assertThat(result).isEqualTo(userDto);
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userArgumentCaptor.capture());
        User savedUser = userArgumentCaptor.getValue();
        assertEquals(changeRoleDto.role(), savedUser.getRole().getName());
    }

    @Test
    void shouldThrowRoleAlreadyProvidedException() {
        ChangeRoleDto changeRoleDto = new ChangeRoleDto("john_doe", "SUPPORT");
        user.setRole(new Role().setName("SUPPORT"));

        when(userRepository.findByUsernameIgnoreCase("john_doe")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.changeRole(changeRoleDto))
                .isInstanceOf(RoleAlreadyProvided.class);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenUserToChangeRoleNotFound() {
        ChangeRoleDto changeRoleDto = new ChangeRoleDto("unknown_user", "SUPPORT");

        when(userRepository.findByUsernameIgnoreCase("unknown_user")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.changeRole(changeRoleDto))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).save(any(User.class));
    }

    // Test for changeAccess()
    @Test
    void shouldLockUserWhenRoleIsNotAdmin() {
        ChangeAccessDto changeAccessDto = new ChangeAccessDto("john_doe", "LOCK");
        user.setRole(new Role().setName("MERCHANT"));

        when(userRepository.findByUsernameIgnoreCase("john_doe")).thenReturn(Optional.of(user));

        userService.changeAccess(changeAccessDto);

        assertThat(user.isAccountNonLocked()).isFalse();
        verify(userRepository).save(user);
    }

    @Test
    void shouldThrowUnableToLockAdminException() {
        ChangeAccessDto changeAccessDto = new ChangeAccessDto("admin_user", "LOCK");
        user.setRole(new Role().setName("ADMINISTRATOR"));

        when(userRepository.findByUsernameIgnoreCase("admin_user")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.changeAccess(changeAccessDto))
                .isInstanceOf(UnableToLockAdminException.class);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenUserNotFound() {
        ChangeAccessDto changeAccessDto = new ChangeAccessDto("unknown_user", "LOCK");

        when(userRepository.findByUsernameIgnoreCase("unknown_user")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.changeAccess(changeAccessDto))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).save(any(User.class));
    }
}
