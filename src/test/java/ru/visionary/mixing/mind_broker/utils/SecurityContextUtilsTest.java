package ru.visionary.mixing.mind_broker.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.visionary.mixing.mind_broker.entity.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class SecurityContextUtilsTest {
    @Test
    void getAuthenticatedUser_WithUserPrincipal_ReturnsUser() {
        User user = User.builder().email("test@example.com").build();
        Authentication auth = new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        User result = SecurityContextUtils.getAuthenticatedUser();

        assertEquals(user, result);
    }

    @Test
    void getAuthenticatedUser_WithNonUserPrincipal_ReturnsNull() {
        Authentication auth = new UsernamePasswordAuthenticationToken("invalid", null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        User result = SecurityContextUtils.getAuthenticatedUser();

        assertNull(result);
    }
}
