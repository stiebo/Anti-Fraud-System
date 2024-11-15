package antifraud.controller;

import antifraud.dto.*;
import antifraud.security.SecurityConfig;
import antifraud.security.RestAuthenticationEntryPoint;
import antifraud.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import antifraud.exception.RoleAlreadyProvidedException;
import antifraud.exception.UnableToLockAdminException;
import antifraud.exception.UserExistsException;
import antifraud.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @Autowired
    private ObjectMapper objectMapper;

    private final NewUserDto newUserDto = new NewUserDto("John Doe", "john_doe", "password");
    private final UserDto userDto = new UserDto(1L, "John Doe", "john_doe",
            "ADMINISTRATOR", "LOCKED");
    private final ChangeRoleDto changeRoleDto = new ChangeRoleDto("john_doe", "SUPPORT");
    private final ChangeAccessDto changeAccessDto = new ChangeAccessDto("john_doe", "LOCK");

    @Test
    public void createUser_ShouldReturnCreatedUser() throws Exception {
        Mockito.when(userService.createUser(any(NewUserDto.class))).thenReturn(userDto);

        mockMvc.perform(post("/api/auth/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(userDto.username()))
                .andExpect(jsonPath("$.name").value(userDto.name()))
                .andExpect(jsonPath("$.role").value(userDto.role()));
    }

    @Test
    public void createUser_ShouldReturnConflictIfUserExists() throws Exception {
        Mockito.when(userService.createUser(any(NewUserDto.class))).thenThrow(new UserExistsException());

        mockMvc.perform(post("/api/auth/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserDto)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    public void getUsers_ShouldReturnUserList() throws Exception {
        Mockito.when(userService.listUsers()).thenReturn(List.of(
                new UserDto(1L, "Stief", "stief1", "ADMINISTRATOR", "LOCKED"),
                new UserDto(2L, "Stief", "stief2", "MERCHANT", "UNLOCKED")
        ));

        mockMvc.perform(get("/api/auth/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("stief1"))
                .andExpect(jsonPath("$[0].name").value("Stief"))
                .andExpect(jsonPath("$[0].role").value("ADMINISTRATOR"))
                .andExpect(jsonPath("$[1].username").value("stief2"))
                .andExpect(jsonPath("$[1].name").value("Stief"))
                .andExpect(jsonPath("$[1].role").value("MERCHANT"));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    public void deleteUser_ShouldReturnSuccessMessage() throws Exception {
        String username = "john_doe";
        Mockito.doNothing().when(userService).deleteUser(username);

        mockMvc.perform(delete("/api/auth/user/{username}", username))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.status").value("Deleted successfully!"));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    public void deleteUser_ShouldReturnNotFoundIfUserDoesNotExist() throws Exception {
        String username = "non_existing_user";
        Mockito.doThrow(new UserNotFoundException()).when(userService).deleteUser(username);

        mockMvc.perform(delete("/api/auth/user/{username}", username))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    public void changeRole_ShouldReturnUpdatedUser() throws Exception {
        UserDto updatedUserDto = new UserDto(1L, "John Doe", "john_doe", "SUPPORT",
                "LOCKED");
        Mockito.when(userService.changeRole(any(ChangeRoleDto.class))).thenReturn(updatedUserDto);

        mockMvc.perform(put("/api/auth/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changeRoleDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(updatedUserDto.username()))
                .andExpect(jsonPath("$.name").value(updatedUserDto.name()))
                .andExpect(jsonPath("$.role").value(updatedUserDto.role()));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    public void changeRole_ShouldReturnConflictIfRoleAlreadyProvided() throws Exception {
        Mockito.when(userService.changeRole(any(ChangeRoleDto.class))).thenThrow(new RoleAlreadyProvidedException());

        mockMvc.perform(put("/api/auth/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changeRoleDto)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    public void changeAccess_ShouldReturnAccessStatus() throws Exception {
        Mockito.doNothing().when(userService).changeAccess(any(ChangeAccessDto.class));

        mockMvc.perform(put("/api/auth/access")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changeAccessDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("User john_doe locked!"));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    public void changeAccess_ShouldReturnBadRequestIfUnableToLockAdmin() throws Exception {
        Mockito.doThrow(new UnableToLockAdminException())
                .when(userService).changeAccess(any(ChangeAccessDto.class));

        mockMvc.perform(put("/api/auth/access")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changeAccessDto)))
                .andExpect(status().isBadRequest());
    }
}
