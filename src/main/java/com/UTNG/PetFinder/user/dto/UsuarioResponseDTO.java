package com.UTNG.PetFinder.user.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.UTNG.PetFinder.auth.entity.TipoCuenta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponseDTO {
    
    private UUID id;
    private String nombreCompleto;
    private String correo;
    private String telefono;
    private TipoCuenta tipoCuenta;
    private String estado;
    private OffsetDateTime fechaRegistro;
    
}