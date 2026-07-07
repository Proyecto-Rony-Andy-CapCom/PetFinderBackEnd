package com.UTNG.PetFinder.user.controller;

import com.UTNG.PetFinder.user.dto.UsuarioActualizacionDTO;
import com.UTNG.PetFinder.user.dto.UsuarioRegistroDTO;
import com.UTNG.PetFinder.user.dto.UsuarioResponseDTO;
import com.UTNG.PetFinder.user.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * Registro de nuevos usuarios.
     * Endpoint público.
     */
    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponseDTO> registrarUsuario(
            @Valid @RequestBody UsuarioRegistroDTO dto) {

        UsuarioResponseDTO nuevoUsuario = usuarioService.registrarUsuario(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoUsuario);
    }

    /**
     * Obtiene el perfil del usuario autenticado.
     */
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> obtenerMiPerfil(
            Authentication authentication) {

        UsuarioResponseDTO usuario =
                usuarioService.obtenerMiPerfil(authentication.getName());

        return ResponseEntity.ok(usuario);
    }

    /**
     * Actualiza únicamente el perfil del usuario autenticado.
     */
    @PutMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> actualizarMiPerfil(
            @Valid @RequestBody UsuarioActualizacionDTO dto,
            Authentication authentication) {

        UsuarioResponseDTO usuarioActualizado =
                usuarioService.actualizarUsuario(
                        dto,
                        authentication.getName());

        return ResponseEntity.ok(usuarioActualizado);
    }

    /**
     * Elimina (Soft Delete) únicamente la cuenta del usuario autenticado.
     */
    @DeleteMapping("/me")
    public ResponseEntity<Void> eliminarMiCuenta(
            Authentication authentication) {

        usuarioService.eliminarUsuario(authentication.getName());

        return ResponseEntity.noContent().build();
    }
}