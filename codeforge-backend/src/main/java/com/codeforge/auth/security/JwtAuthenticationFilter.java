package com.codeforge.auth.security;

import java.io.IOException;
import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.codeforge.usuario.entity.Usuario;
import com.codeforge.usuario.repository.UsuarioRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private final JwtService jwtService;

    private final UsuarioRepository usuarioRepository;


    public JwtAuthenticationFilter(
            JwtService jwtService,
            UsuarioRepository usuarioRepository
    ) {

        this.jwtService =
                jwtService;

        this.usuarioRepository =
                usuarioRepository;
    }


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {


        String method =
                request.getMethod();

        String uri =
                request.getRequestURI();


        System.out.println(
                "=================================================="
        );

        System.out.println(
                "JWT FILTER"
        );

        System.out.println(
                "METHOD: " + method
        );

        System.out.println(
                "URI: " + uri
        );


        String authorizationHeader =
                request.getHeader(
                        "Authorization"
                );


        // =====================================================
        // COMPROBAR HEADER
        // =====================================================

        if (
                authorizationHeader == null
                ||
                authorizationHeader.isBlank()
        ) {

            System.out.println(
                    "JWT FILTER: NO HAY HEADER AUTHORIZATION"
            );

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }


        System.out.println(
                "JWT FILTER: HEADER AUTHORIZATION RECIBIDO"
        );


        if (
                !authorizationHeader.startsWith(
                        "Bearer "
                )
        ) {

            System.out.println(
                    "JWT FILTER: EL HEADER NO USA BEARER"
            );

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }


        String token =
                authorizationHeader.substring(
                        7
                ).trim();


        if (token.isBlank()) {

            System.out.println(
                    "JWT FILTER: TOKEN VACIO"
            );

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }


        try {

            // =================================================
            // VALIDAR TOKEN
            // =================================================

            boolean valido =
                    jwtService.esTokenValido(
                            token
                    );


            System.out.println(
                    "JWT FILTER: TOKEN VALIDO = "
                    + valido
            );


            if (!valido) {

                SecurityContextHolder
                        .clearContext();

                filterChain.doFilter(
                        request,
                        response
                );

                return;
            }


            // =================================================
            // OBTENER USERNAME
            // =================================================

            String username =
                    jwtService.obtenerUsername(
                            token
                    );


            System.out.println(
                    "JWT FILTER: USERNAME = "
                    + username
            );


            // =================================================
            // EVITAR DUPLICAR AUTENTICACION
            // =================================================

            if (
                    SecurityContextHolder
                            .getContext()
                            .getAuthentication()
                            == null
            ) {


                Usuario usuario =
                        usuarioRepository
                                .findByUsername(
                                        username
                                )
                                .orElse(null);


                // =============================================
                // USUARIO NO ENCONTRADO
                // =============================================

                if (usuario == null) {

                    System.out.println(
                            "JWT FILTER: USUARIO NO ENCONTRADO"
                    );

                    filterChain.doFilter(
                            request,
                            response
                    );

                    return;
                }


                // =============================================
                // USUARIO INACTIVO
                // =============================================

                if (
                        !"ACTIVO".equals(
                                usuario.getEstado()
                        )
                ) {

                    System.out.println(
                            "JWT FILTER: USUARIO INACTIVO"
                    );

                    filterChain.doFilter(
                            request,
                            response
                    );

                    return;
                }


                // =============================================
                // CREAR AUTENTICACION
                // =============================================

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                usuario.getUsername(),
                                null,
                                Collections.emptyList()
                        );


                SecurityContextHolder
                        .getContext()
                        .setAuthentication(
                                authentication
                        );


                System.out.println(
                        "JWT FILTER: AUTENTICACION CREADA"
                );

                System.out.println(
                        "JWT FILTER: USUARIO AUTENTICADO = "
                        + usuario.getUsername()
                );

            } else {

                System.out.println(
                        "JWT FILTER: YA EXISTIA AUTENTICACION"
                );

            }


        } catch (Exception e) {

            System.err.println(
                    "=================================================="
            );

            System.err.println(
                    "JWT FILTER: ERROR"
            );

            System.err.println(
                    "TIPO: "
                    + e.getClass().getName()
            );

            System.err.println(
                    "MENSAJE: "
                    + e.getMessage()
            );

            e.printStackTrace();

            SecurityContextHolder
                    .clearContext();
        }


        System.out.println(
                "JWT FILTER: CONTINUANDO CADENA"
        );

        System.out.println(
                "=================================================="
        );


        filterChain.doFilter(
                request,
                response
        );
    }
}