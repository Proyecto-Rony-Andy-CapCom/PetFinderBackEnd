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
            // Rutas públicas
            .requestMatchers(
                "/api/auth/**",
                "/api/usuarios/**"
            ).permitAll()

            // Solo ADMIN
            .requestMatchers("/api/admin/**").hasRole("administrador")


            // ADMIN o REFUGIO
            .requestMatchers("/api/refugios/**")
                .hasAnyRole("ADMIN", "REFUGIO")

            // ADMIN o CIUDADANO
            .requestMatchers("/api/usuarios/**")
                .hasAnyRole("ADMIN", "CIUDADANO")

            // Cualquier otro endpoint requiere autenticación
            .anyRequest().authenticated()
        )
        .sessionManagement(session ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .authenticationProvider(authenticationProvider)
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
}
}