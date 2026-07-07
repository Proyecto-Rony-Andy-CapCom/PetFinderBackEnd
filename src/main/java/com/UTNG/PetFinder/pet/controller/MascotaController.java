package com.UTNG.PetFinder.pet.controller;

import com.UTNG.PetFinder.pet.dto.MascotaCreateRequest;
import com.UTNG.PetFinder.pet.dto.MascotaResponse;
import com.UTNG.PetFinder.pet.dto.MascotaUpdateRequest;
import com.UTNG.PetFinder.pet.service.MascotaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/mascotas")
@RequiredArgsConstructor
public class MascotaController {

    private final MascotaService mascotaService;

    /**
     * Crea una nueva mascota para el usuario autenticado.
     */
    @PostMapping
    public ResponseEntity<MascotaResponse> crearMascota(
            @Valid @RequestBody MascotaCreateRequest dto,
            Authentication authentication) {

        MascotaResponse nuevaMascota = mascotaService.crearMascota(
                dto,
                authentication.getName()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaMascota);
    }

    /**
     * Obtiene todas las mascotas del usuario autenticado (solo sus propias mascotas).
     */
    @GetMapping("/mis-mascotas")
    public ResponseEntity<List<MascotaResponse>> obtenerMisMascotas(
            Authentication authentication) {

        List<MascotaResponse> mascotas = mascotaService.obtenerMisMascotas(
                authentication.getName()
        );
        return ResponseEntity.ok(mascotas);
    }

    /**
     * Obtiene una mascota específica del usuario autenticado por su ID.
     * Protegido contra BOLA (Broken Object Level Authorization).
     */
    @GetMapping("/{mascotaId}")
    public ResponseEntity<MascotaResponse> obtenerMiMascota(
            @PathVariable UUID mascotaId,
            Authentication authentication) {

        MascotaResponse mascota = mascotaService.obtenerMiMascota(
                mascotaId,
                authentication.getName()
        );
        return ResponseEntity.ok(mascota);
    }

    /**
     * Actualiza una mascota existente del usuario autenticado.
     * Protegido contra BOLA.
     */
    @PutMapping("/{mascotaId}")
    public ResponseEntity<MascotaResponse> actualizarMascota(
            @PathVariable UUID mascotaId,
            @Valid @RequestBody MascotaUpdateRequest dto,
            Authentication authentication) {

        MascotaResponse mascotaActualizada = mascotaService.actualizarMascota(
                mascotaId,
                dto,
                authentication.getName()
        );
        return ResponseEntity.ok(mascotaActualizada);
    }

    /**
     * Elimina una mascota del usuario autenticado.
     * Protegido contra BOLA.
     */
    @DeleteMapping("/{mascotaId}")
    public ResponseEntity<Void> eliminarMascota(
            @PathVariable UUID mascotaId,
            Authentication authentication) {

        mascotaService.eliminarMascota(
                mascotaId,
                authentication.getName()
        );
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtiene todas las mascotas de todos los usuarios (público o para administradores).
     * Este endpoint NO necesita autenticación (o puede estar restringido según tu lógica).
     */
    @GetMapping("/todas")
    public ResponseEntity<List<MascotaResponse>> obtenerTodasLasMascotas() {
        List<MascotaResponse> todas = mascotaService.obtenerTodasLasMascotas();
        return ResponseEntity.ok(todas);
    }
}