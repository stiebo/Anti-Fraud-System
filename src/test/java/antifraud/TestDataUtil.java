package antifraud;

import antifraud.domain.Role;
import antifraud.domain.User;

public class TestDataUtil {
    private static final String USERNAME = "john_doe";
    private static final String PASSWORD = "password123";
    private static final String NAME = "John Doe";
    private static final boolean UNLOCKED = false;
    private static final Role ADMIN_ROLE = createRole("ADMINISTRATOR", 1L);
    private static final Role UNKNOWN_ROLE = createRole("UNKNOWN", 200L);

    public static final User testCorrectUser = createUser(USERNAME, PASSWORD, NAME, UNLOCKED, ADMIN_ROLE);
    public static final User testCorrectUser2 = createUser(USERNAME + "2", PASSWORD, NAME, UNLOCKED, ADMIN_ROLE);
    public static final User testUserWithoutUsername = createUser(null, PASSWORD, NAME, UNLOCKED, ADMIN_ROLE);
    public static final User testUserWithUnknownRole = createUser(USERNAME, PASSWORD, NAME, UNLOCKED, UNKNOWN_ROLE);

    private static Role createRole(String name, Long id) {
        return new Role().setName(name).setId(id);
    }

    private static User createUser(String username, String password, String name, boolean locked, Role role) {
        return new User()
                .setUsername(username)
                .setPassword(password)
                .setName(name)
                .setLocked(locked)
                .setRole(role);
    }
}
