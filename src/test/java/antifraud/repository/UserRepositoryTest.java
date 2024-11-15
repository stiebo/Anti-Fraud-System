package antifraud.repository;

import static org.assertj.core.api.Assertions.assertThat;

import static antifraud.TestDataUtil.*;
import static org.junit.jupiter.api.Assertions.*;

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
        userRepository.deleteAll();
    }

    @Test
    void testSaveCorrectUserSavesUser() {
        User savedUser = userRepository.save(testCorrectUser);
        assertThat(savedUser.getId()).isNotNull();
    }

    @Test
    void testSaveUserWithoutNameThrowsDataIntegrityViolationException() {
        assertThrows(DataIntegrityViolationException.class, () -> userRepository.save(testUserWithoutUsername));
    }

    @Test
    void testSaveUserWithInvalidRoleThrowsDataIntegrityViolationException() {
        assertThrows(DataIntegrityViolationException.class, () -> userRepository.save(testUserWithUnknownRole));
    }

    @Test
    void testFindByUsernameIgnoreCaseFindsCorrectUser() {
        userRepository.save(testCorrectUser);

        Optional<User> foundUser = userRepository.findByUsernameIgnoreCase(testCorrectUser.getUsername());
        assertTrue(foundUser.isPresent());
        assertThat(foundUser.get().getUsername()).isEqualToIgnoringCase(testCorrectUser.getUsername());
    }

    @Test
    void testExistsByUsernameIgnoreCaseConfirmsUserExists() {
        userRepository.save(testCorrectUser);
        boolean exists = userRepository.existsByUsernameIgnoreCase(testCorrectUser.getUsername());
        assertTrue(exists);
    }

    @Test
    void testFindAllByOrderByIdAscFindsAllUsersInCorrectOrder() {
        userRepository.save(testCorrectUser);
        userRepository.save(testCorrectUser2);

        List<User> users = userRepository.findAllByOrderByIdAsc();
        assertThat(users.size()).isEqualTo(2);
        assertEquals(testCorrectUser, users.get(0));
        assertEquals(testCorrectUser2, users.get(1));
    }
}
