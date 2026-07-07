package com.UTNG.PetFinder.pet.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="razas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Raza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="especie_id", nullable = false)
    private Especie especie;

    @Column(nullable = false, length = 80)
    private String nombre;

}