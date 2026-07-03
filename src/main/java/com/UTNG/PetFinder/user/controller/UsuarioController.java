package com.UTNG.PetFinder.user.controller;

import com.UTNG.PetFinder.user.dto.UsuarioRegistroDTO;
import com.UTNG.PetFinder.user.dto.UsuarioResponseDTO;
import com.UTNG.PetFinder.user.service.UsuarioService;
import com.UTNG.PetFinder.user.dto.UsuarioActualizacionDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.DeleteMapping;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponseDTO> registrarUsuario(@Valid @RequestBody UsuarioRegistroDTO dto) {
        UsuarioResponseDTO nuevoUsuario = usuarioService.registrarUsuario(dto);
        return new ResponseEntity<>(nuevoUsuario, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> obtenerUsuario(@PathVariable java.util.UUID id) {
        UsuarioResponseDTO usuario = usuarioService.obtenerUsuarioPorId(id);
        return ResponseEntity.ok(usuario); // Devuelve un 200 OK con el JSON del perfil
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> actualizarUsuario(
            @PathVariable java.util.UUID id, 
            @Valid @RequestBody UsuarioActualizacionDTO dto) {
        
        UsuarioResponseDTO usuarioActualizado = usuarioService.actualizarUsuario(id, dto);
        return ResponseEntity.ok(usuarioActualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable java.util.UUID id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build(); // Retorna código 204
    }
}