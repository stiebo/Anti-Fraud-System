package antifraud.controller;

import antifraud.dto.*;
import antifraud.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class UserController {
    private final UserService service;

    @Autowired
    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping("/user")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@Valid @RequestBody NewUserDto newUserDto) {
        return service.createUser(newUserDto);
    }

    @GetMapping("/list")
    public UserDto[] getUsers () {
        return service.listUsers();
    }

    @DeleteMapping("/user/{username}")
    public DeleteUserDto deleteUser(@PathVariable("username") String username) {
        service.deleteUser(username);
        return new DeleteUserDto(username, "Deleted successfully!");
    }

    @PutMapping("/role")
    public UserDto changeRole(@Valid @RequestBody ChangeRoleDto changeRoleDto) {
        return service.changeRole(changeRoleDto);
    }

    @PutMapping("/access")
    public Map<String,String> changeAccess(@Valid @RequestBody ChangeAccessDto changeAccessDto) {
        service.changeAccess(changeAccessDto);
        return Collections.singletonMap("status",
                String.format("User %s %s!", changeAccessDto.username(),
                        changeAccessDto.operation().equals("LOCK") ?
                        "locked" : "unlocked"));
    }
}
