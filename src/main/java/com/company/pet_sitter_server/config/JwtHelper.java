package com.company.pet_sitter_server.config;

import com.company.pet_sitter_server.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Convenience helper to extract userId / role from the SecurityContext.
 * The JwtAuthenticationFilter already validated the token and loaded the user —
 * controllers just call getCurrentUserId() or getCurrentUserDetails().
 */
@Component
public class JwtHelper {

    public Long getCurrentUserId() {
        return getCurrentUserDetails().getId();
    }

    public String getCurrentUserRole() {
        return getCurrentUserDetails().getRole();
    }

    public CustomUserDetails getCurrentUserDetails() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof CustomUserDetails)) {
            throw new UnauthorizedException("Not authenticated");
        }
        return (CustomUserDetails) auth.getPrincipal();
    }
}
