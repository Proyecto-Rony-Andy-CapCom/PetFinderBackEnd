package com.UTNG.PetFinder.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

    http
        .csrf(csrf -> csrf.disable())

        .authorizeHttpRequests(auth -> auth

            // ==========================
            // RUTAS PUBLICAS
            // ==========================

            .requestMatchers(
                    "/api/auth/login",
                    "/api/auth/register",
                    "/api/auth/refresh"
            ).permitAll()


            // ==========================
            // RUTAS ADMIN
            // ==========================

            .requestMatchers("/api/admin/**")
                    .hasRole("ADMIN")


            // ==========================
            // RUTAS REFUGIOS
            // ==========================

            .requestMatchers("/api/refugios/**")
                    .hasAnyRole(
                            "ADMIN",
                            "REFUGIO"
                    )


            // ==========================
            // RUTAS USUARIOS
            // ==========================

            .requestMatchers(
                    "/api/usuarios/perfil",
                    "/api/usuarios/actualizar/**"
            )
                    .hasAnyRole(
                            "ADMIN",
                            "CIUDADANO"
                    )


            // Todo lo demás requiere JWT
            .anyRequest().authenticated()
        )


        .sessionManagement(session ->
                session.sessionCreationPolicy(
                        SessionCreationPolicy.STATELESS
                )
        )

        .authenticationProvider(authenticationProvider)

        .addFilterBefore(
                jwtAuthFilter,
                UsernamePasswordAuthenticationFilter.class
        );


    return http.build();
}
}