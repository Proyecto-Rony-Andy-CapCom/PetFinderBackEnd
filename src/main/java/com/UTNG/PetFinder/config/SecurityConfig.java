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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

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

    // ── 3. DEFINIMOS QUÉ ORÍGENES Y MÉTODOS TIENEN PERMISO ─────────────
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Le damos permiso explícito a tu frontend de Next.js
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000")); 
        // Permitimos todos los verbos HTTP necesarios
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // Permitimos las cabeceras para que puedas enviar el token JWT después
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}