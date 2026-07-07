package com.UTNG.PetFinder.auth.service;

import com.UTNG.PetFinder.auth.dto.AuthResponseDTO;
import com.UTNG.PetFinder.auth.dto.LoginRequestDTO;
import com.UTNG.PetFinder.user.repository.UsuarioRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponseDTO login(
            LoginRequestDTO request,
            HttpServletResponse response
    ) {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getCorreo(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException("Correo o contraseña incorrectos");
        }

        var usuario = usuarioRepository
                .findByCorreoIgnoreCase(request.getCorreo())
                .orElseThrow();

        String accessToken =
                jwtService.generateAccessToken(usuario);

        String refreshToken =
                jwtService.generateRefreshToken(usuario);

        Cookie refreshCookie = new Cookie(
                "refreshToken",
                refreshToken
        );

        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(
                60 * 60 * 24 * 7
        );

        response.addCookie(refreshCookie);

        return AuthResponseDTO.builder()
                .accessToken(accessToken)
                .build();
    }

    public AuthResponseDTO refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        String refreshToken = jwtService.extractRefreshToken(request);

        if (refreshToken == null) {
            throw new RuntimeException(
                    "Refresh token no encontrado"
            );
        }

        String correo =
                jwtService.extractUsername(refreshToken);

        var usuario = usuarioRepository
                .findByCorreoIgnoreCase(correo)
                .orElseThrow();

        if (!jwtService.isRefreshTokenValid(
                refreshToken,
                usuario
        )) {

            throw new RuntimeException(
                    "Refresh token inválido"
            );
        }

        String newAccessToken =
                jwtService.generateAccessToken(usuario);

        String newRefreshToken =
                jwtService.generateRefreshToken(usuario);

        Cookie refreshCookie = new Cookie(
                "refreshToken",
                newRefreshToken
        );

        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(
                60 * 60 * 24 * 7
        );

        response.addCookie(refreshCookie);

        return AuthResponseDTO.builder()
                .accessToken(newAccessToken)
                .build();
    }
}