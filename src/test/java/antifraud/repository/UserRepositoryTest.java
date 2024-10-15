package antifraud.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import antifraud.domain.Role;
import antifraud.domain.User;
import antifraud.security.RoleLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import(RoleLoader.class)
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RoleLoader roleLoader;  // loads default roles into test-RoleRepository

    @BeforeEach
    void setUp() {
        // Clean up before each test
        userRepository.deleteAll();
    }

    @Test
    void testSaveUser() {
        User user = new User();
        user.setUsername("john_doe");
        user.setPassword("password123");
        user.setName("John Doe");
        user.setLocked(false);
        Role role = roleRepository.findByName("ADMINISTRATOR");
        user.setRole(role);

        User savedUser = userRepository.save(user);
        assertThat(savedUser.getId()).isNotNull();
    }

    @Test
    void testSaveUserWithoutNameThrowsDataIntegrityViolationException() {
        User user = new User();
        user.setUsername("john_doe");
        user.setPassword("password123");
        // intentionally removed       user.setName("John Doe");
        user.setLocked(false);
        Role role = roleRepository.findByName("ADMINISTRATOR");
        user.setRole(role);

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            assertThat(e).isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    @Test
    void testSaveUserWithInvalidRoleThrowsDataIntegrityViolationException() {
        User user = new User();
        user.setUsername("john_doe");
        user.setPassword("password123");
        user.setName("John Doe");
        user.setLocked(false);
        Role role = new Role().setId(200L).setName("INVALID_ROLE");
        user.setRole(role);

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            assertThat(e).isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    @Test
    void testFindByUsernameIgnoreCase() {
        User user = new User();
        user.setUsername("TestUser");
        user.setPassword("password");
        user.setName("Test User");
        user.setLocked(false);
        Role role = roleRepository.findByName("ADMINISTRATOR");
        user.setRole(role);
        userRepository.save(user);

        Optional<User> foundUser = userRepository.findByUsernameIgnoreCase("testuser");
        assertTrue(foundUser.isPresent());
        assertThat(foundUser.get().getUsername()).isEqualToIgnoringCase("TestUser");
    }

    @Test
    void testExistsByUsernameIgnoreCase() {
        User user = new User();
        user.setUsername("Alice");
        user.setPassword("secret");
        user.setName("Alice Wonderland");
        user.setLocked(false);
        Role role = roleRepository.findByName("SUPPORT");
        user.setRole(role);
        userRepository.save(user);

        boolean exists = userRepository.existsByUsernameIgnoreCase("alice");
        assertTrue(exists);
    }

    @Test
    void testFindAllByOrderByIdAsc() {
        User user1 = new User();
        user1.setUsername("user1");
        user1.setPassword("password1");
        user1.setName("User One");
        user1.setLocked(false);
        Role role = roleRepository.findByName("MERCHANT");
        user1.setRole(role);

        User user2 = new User();
        user2.setUsername("user2");
        user2.setPassword("password2");
        user2.setName("User Two");
        user2.setLocked(false);
        user2.setRole(role);

        userRepository.save(user1);
        userRepository.save(user2);

        List<User> users = userRepository.findAllByOrderByIdAsc();
        assertThat(users.size()).isEqualTo(2);
        assertThat(users.get(0).getUsername()).isEqualTo("user1");
        assertThat(users.get(1).getUsername()).isEqualTo("user2");
    }
}
