package com.UTNG.PetFinder.pet.repository;

import com.UTNG.PetFinder.pet.entity.Raza;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RazaRepository extends JpaRepository<Raza, Integer> {  // Integer
    List<Raza> findByEspecieId(Short especieId);  // Cambia UUID a Short
}