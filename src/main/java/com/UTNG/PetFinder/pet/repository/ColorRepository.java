package com.UTNG.PetFinder.pet.repository;

import com.UTNG.PetFinder.pet.entity.Color;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ColorRepository extends JpaRepository<Color, Short> {  // Short
}