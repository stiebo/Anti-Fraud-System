package antifraud.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class UserTest {

    private User user;

    @Mock
    private Role mockRole;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockRole.getName()).thenReturn("ADMINISTRATOR");

        user = new User()
                .setId(1L)
                .setName("John Doe")
                .setUsername("john.doe")
                .setPassword("password123")
                .setRole(mockRole)
                .setLocked(false);
    }

    @Test
    public void testGetAuthorities() {
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_ADMINISTRATOR")));
    }

    @Test
    public void testIsAccountNonExpired() {
        assertTrue(user.isAccountNonExpired());
    }

    @Test
    public void testIsAccountNonLockedWhenNotLocked() {
        user.setLocked(false);
        assertTrue(user.isAccountNonLocked());
    }

    @Test
    public void testIsAccountNonLockedWhenLocked() {
        user.setLocked(true);
        assertFalse(user.isAccountNonLocked());
    }

    @Test
    public void testIsCredentialsNonExpired() {
        assertTrue(user.isCredentialsNonExpired());
    }

    @Test
    public void testIsEnabled() {
        assertTrue(user.isEnabled());
    }

    @Test
    public void testUserDetailsAttributes() {
        assertEquals("john.doe", user.getUsername());
        assertEquals("password123", user.getPassword());
    }
}
