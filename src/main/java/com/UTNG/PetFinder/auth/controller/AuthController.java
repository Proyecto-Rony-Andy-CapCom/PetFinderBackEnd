package com.UTNG.PetFinder.auth.controller;

import com.UTNG.PetFinder.auth.dto.AuthResponseDTO;
import com.UTNG.PetFinder.auth.dto.LoginRequestDTO;
import com.UTNG.PetFinder.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }
}