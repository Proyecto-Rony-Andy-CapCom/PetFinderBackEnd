package com.UTNG.PetFinder.auth.controller;

import com.UTNG.PetFinder.auth.dto.AuthResponseDTO;
import com.UTNG.PetFinder.auth.dto.LoginRequestDTO;
import com.UTNG.PetFinder.auth.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

@PostMapping("/login")
public ResponseEntity<AuthResponseDTO> login(
        @RequestBody LoginRequestDTO request,
        HttpServletResponse response
){

    return ResponseEntity.ok(
            authService.login(request,response)
    );
}

 @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ){

        return ResponseEntity.ok(
                authService.refresh(
                        request,
                        response
                )
        );
    }
}