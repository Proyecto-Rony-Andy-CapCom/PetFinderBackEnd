package com.UTNG.PetFinder.pet.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="colores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Color {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;

    @Column(nullable = false, unique = true, length = 50)
    private String nombre;

}