package antifraud.security;

import antifraud.repository.UserRepository;
import antifraud.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLoadUserByUsernameWhenUserExistsReturnsUserDetails() {
        String username = "testuser";
        User user = new User();
        user.setUsername(username);
        user.setPassword("password");
        when(userRepository.findByUsernameIgnoreCase(username)).thenReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        verify(userRepository, times(1)).findByUsernameIgnoreCase(username);
    }

    @Test
    void testLoadUserByUsernameWhenUserDoesNotExistThrowsUsernameNotFoundException() {
        String username = "nonexistentuser";
        when(userRepository.findByUsernameIgnoreCase(username)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername(username));
        verify(userRepository, times(1)).findByUsernameIgnoreCase(username);
    }
}
