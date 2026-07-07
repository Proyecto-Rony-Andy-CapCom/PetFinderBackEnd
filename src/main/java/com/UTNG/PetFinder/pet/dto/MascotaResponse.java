package com.UTNG.PetFinder.pet.dto;

import com.UTNG.PetFinder.pet.entity.SexoMascota;
import com.UTNG.PetFinder.pet.entity.TamanoMascota;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class MascotaResponse {
    private UUID id;
    private String nombre;
    private SexoMascota sexo;
    private TamanoMascota tamano;
    private Short edadAproximada;
    private Boolean esterilizado;
    private String senasParticulares;

    private Short especieId;
    private String nombreEspecie;
    private Integer razaId;
    private String nombreRaza;
    private Short colorId;
    private String nombreColor;
}