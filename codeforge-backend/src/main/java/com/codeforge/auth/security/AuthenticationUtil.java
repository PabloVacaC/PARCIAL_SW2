package com.codeforge.auth.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class AuthenticationUtil {

    private AuthenticationUtil() {
    }

    public static String obtenerUsernameAutenticado() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null) {
            throw new RuntimeException(
                    "No existe un usuario autenticado");
        }

        if (!authentication.isAuthenticated()) {
            throw new RuntimeException(
                    "El usuario no está autenticado");
        }

        if (authentication instanceof AnonymousAuthenticationToken) {
            throw new RuntimeException(
                    "El usuario no está autenticado");
        }

        return authentication.getName();
    }
}