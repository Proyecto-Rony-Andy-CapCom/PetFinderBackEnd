package com.UTNG.PetFinder.config;

import com.UTNG.PetFinder.auth.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {


    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;


    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {


        /*
         * Espera:
         *
         * Authorization: Bearer ACCESS_TOKEN
         *
         */
        final String authHeader = request.getHeader("Authorization");


        // Si no existe header Authorization continuamos
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }


        // Quitamos "Bearer "
        final String jwt = authHeader.substring(7);


        // Extraemos el correo del usuario desde el JWT
        final String userEmail = jwtService.extractUsername(jwt);


        /*
         * Si existe usuario y todavía no hay autenticación
         * en el contexto de Spring Security
         */
        if (
                userEmail != null &&
                SecurityContextHolder.getContext().getAuthentication() == null
        ) {


            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(userEmail);


            /*
             * IMPORTANTE:
             *
             * Aquí solamente aceptamos ACCESS TOKEN
             *
             * Un REFRESH TOKEN será rechazado
             */
            if (jwtService.isAccessTokenValid(jwt, userDetails)) {


                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );


                authToken.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );


                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authToken);
            }
        }


        filterChain.doFilter(request, response);
    }
}