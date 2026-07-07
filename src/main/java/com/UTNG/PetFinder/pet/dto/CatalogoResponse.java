package com.UTNG.PetFinder.pet.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogoResponse {

    private Number id;

    private String nombre;

}