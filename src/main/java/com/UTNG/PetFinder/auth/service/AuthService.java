package com.UTNG.PetFinder.auth.service;

import com.UTNG.PetFinder.auth.dto.AuthResponseDTO;
import com.UTNG.PetFinder.auth.dto.LoginRequestDTO;
import com.UTNG.PetFinder.user.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponseDTO login(LoginRequestDTO request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getCorreo(), request.getPassword())
        );
        
        var usuario = usuarioRepository.findByCorreoIgnoreCase(request.getCorreo()).orElseThrow();
        var jwt = jwtService.generateToken(usuario);
        
        return AuthResponseDTO.builder().token(jwt).build();
    }
}