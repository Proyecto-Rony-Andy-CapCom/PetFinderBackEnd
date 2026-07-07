package com.UTNG.PetFinder.pet.dto;

import com.UTNG.PetFinder.pet.entity.SexoMascota;
import com.UTNG.PetFinder.pet.entity.TamanoMascota;
import lombok.Data;

@Data
public class MascotaCreateRequest {
    private String nombre;
    private SexoMascota sexo;
    private TamanoMascota tamano;
    private Short edadAproximada;
    private Boolean esterilizado;
    private String senasParticulares;

    private Short especieId;   // Cambiado a Short
    private Integer razaId;    // Cambiado a Integer
    private Short colorId;     // Cambiado a Short
}