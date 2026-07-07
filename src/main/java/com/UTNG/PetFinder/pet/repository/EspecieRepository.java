package com.UTNG.PetFinder.pet.repository;

import com.UTNG.PetFinder.pet.entity.Especie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EspecieRepository extends JpaRepository<Especie, Short> {  // Short
}