package antifraud.security;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import antifraud.domain.Role;
import antifraud.exception.UserExistsException;
import antifraud.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

public class RoleLoaderTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleLoader roleLoader;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        reset(roleRepository);
    }

    @Test
    public void testCreateRoles() {
        // Arrange is not needed as the method is invoked during the RoleLoader construction

        // Act - RoleLoader constructor will automatically call createRoles()
        new RoleLoader(roleRepository);

        // Assert - Verify that the roles were saved correctly
        verify(roleRepository, times(1)).save(argThat(role -> role.getName().equals("ADMINISTRATOR")));
        verify(roleRepository, times(1)).save(argThat(role -> role.getName().equals("MERCHANT")));
        verify(roleRepository, times(1)).save(argThat(role -> role.getName().equals("SUPPORT")));
    }

    @Test
    public void testCreateRolesNoExceptions() {
        // Arrange

        // Act - Again, the RoleLoader constructor calls createRoles()
        new RoleLoader(roleRepository);

        // Assert - Ensure that no role is saved more times than needed
        verify(roleRepository, times(1)).save(argThat(role -> role.getName().equals("ADMINISTRATOR")));
        verify(roleRepository, times(1)).save(argThat(role -> role.getName().equals("MERCHANT")));
        verify(roleRepository, times(1)).save(argThat(role -> role.getName().equals("SUPPORT")));

        // Assert - Verify that other roles are never saved
        verify(roleRepository, never()).save(argThat(role -> role.getName().equals("INVALID_ROLE")));
    }

    @Test
    public void testCreateRolesHandlesExceptionsWhenRolesAlreadyInRepository() {
        // Arrange - Mock an exception being thrown by the repository
        doThrow(new RuntimeException()).when(roleRepository).save(argThat(role -> role.getName().equals("ADMINISTRATOR")));
        doThrow(new RuntimeException()).when(roleRepository).save(argThat(role -> role.getName().equals("MERCHANT")));
        doThrow(new RuntimeException()).when(roleRepository).save(argThat(role -> role.getName().equals("SUPPORT")));
        // Act - Create the RoleLoader, which will attempt to save roles
        new RoleLoader(roleRepository);

        // Assert - Verify that the exception was handled and other roles were still attempted to be saved
        verify(roleRepository, times(1)).save(argThat(role -> role.getName().equals("ADMINISTRATOR")));
        verify(roleRepository, times(1)).save(argThat(role -> role.getName().equals("MERCHANT")));
        verify(roleRepository, times(1)).save(argThat(role -> role.getName().equals("SUPPORT")));
    }
}