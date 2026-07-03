package com.UTNG.PetFinder.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. Desactivamos CSRF (Crucial para APIs REST que usarán JWT después)
            .csrf(csrf -> csrf.disable())
            
            // 2. Desactivamos los formularios de login por defecto de Spring
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            
            // 3. Configuramos los permisos de las rutas
            .authorizeHttpRequests(auth -> auth
                // Permite el acceso público a todo lo que esté bajo /api/usuarios
                // Ajusta esta ruta según cómo nombres tu Controlador
                .requestMatchers("/api/usuarios/**").permitAll()
                
                // Cualquier otra petición requerirá estar autenticado
                .anyRequest().authenticated()
            );

        return http.build();
    }

    // Bean para encriptar las contraseñas al registrar ciudadanos/entidades
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}