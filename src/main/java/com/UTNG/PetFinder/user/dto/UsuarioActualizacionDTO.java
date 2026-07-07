package com.UTNG.PetFinder.user.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioActualizacionDTO {

    @Size(max = 150, message = "El nombre no puede exceder los 150 caracteres")
    private String nombreCompleto;

    @Size(max = 20, message = "El teléfono no puede exceder los 20 caracteres")
    private String telefono;
}