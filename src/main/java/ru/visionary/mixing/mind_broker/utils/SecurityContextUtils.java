package ru.visionary.mixing.mind_broker.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.visionary.mixing.mind_broker.entity.User;

public class SecurityContextUtils {
    public static User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            return null;
        }
        return (auth.getPrincipal() instanceof User user) ? user : null;
    }
}
