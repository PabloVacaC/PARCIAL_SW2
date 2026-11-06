package com.codeforge.auth.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.codeforge.auth.security.JwtAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of("http://localhost:4200")
        );

        configuration.setAllowedMethods(
                List.of(
                        HttpMethod.GET.name(),
                        HttpMethod.POST.name(),
                        HttpMethod.PUT.name(),
                        HttpMethod.DELETE.name(),
                        HttpMethod.OPTIONS.name()
                )
        );

        configuration.setAllowedHeaders(
                List.of("*")
        );

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }


    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http

            .cors(Customizer.withDefaults())

            .csrf(csrf ->
                    csrf.disable()
            )

            .sessionManagement(session ->
                    session.sessionCreationPolicy(
                            SessionCreationPolicy.STATELESS
                    )
            )

            .exceptionHandling(exception -> exception

                /*
                 * 401
                 * No autenticado.
                 */
                .authenticationEntryPoint(
                    (request, response, authException) -> {

                        response.setStatus(
                                HttpServletResponse.SC_UNAUTHORIZED
                        );

                        response.setContentType(
                                "application/json"
                        );

                        response.setCharacterEncoding(
                                "UTF-8"
                        );

                        response.getWriter().write(
                                """
                                {
                                  "status": 401,
                                  "error": "No autenticado",
                                  "mensaje": "Debe iniciar sesión o proporcionar un JWT válido"
                                }
                                """
                        );
                    }
                )

                /*
                 * 403
                 * Autenticado pero sin permisos.
                 */
                .accessDeniedHandler(
                    (request, response, accessDeniedException) -> {

                        response.setStatus(
                                HttpServletResponse.SC_FORBIDDEN
                        );

                        response.setContentType(
                                "application/json"
                        );

                        response.setCharacterEncoding(
                                "UTF-8"
                        );

                        response.getWriter().write(
                                """
                                {
                                  "status": 403,
                                  "error": "Acceso denegado",
                                  "mensaje": "No tiene permisos para realizar esta acción"
                                }
                                """
                        );
                    }
                )
            )

            .authorizeHttpRequests(auth -> auth

                /*
                 * Login y registro son públicos.
                 */
                .requestMatchers(
                        "/api/auth/**",
                        "/error"
                ).permitAll()

                /*
                 * WebSocket para colaboración en tiempo real.
                 */
                .requestMatchers(
                        "/ws-colaboracion/**"
                ).permitAll()

                /*
                 * Permitir preflight CORS.
                 */
                .requestMatchers(
                        HttpMethod.OPTIONS,
                        "/**"
                ).permitAll()

                /*
                 * Todo lo demás requiere autenticación.
                 */
                .anyRequest().authenticated()
            )

            .addFilterBefore(
                    jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}