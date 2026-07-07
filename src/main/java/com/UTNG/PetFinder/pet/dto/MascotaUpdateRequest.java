package com.UTNG.PetFinder.pet.dto;

import com.UTNG.PetFinder.pet.entity.SexoMascota;
import com.UTNG.PetFinder.pet.entity.TamanoMascota;
import lombok.Data;

@Data
public class MascotaUpdateRequest {
    private String nombre;
    private SexoMascota sexo;
    private TamanoMascota tamano;
    private Short edadAproximada;
    private Boolean esterilizado;
    private String senasParticulares;

    private Short especieId;
    private Integer razaId;
    private Short colorId;
    private boolean colorEliminado = false; // opcional
}