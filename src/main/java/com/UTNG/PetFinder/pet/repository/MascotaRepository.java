package com.UTNG.PetFinder.pet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.UTNG.PetFinder.pet.entity.Mascota;
import com.UTNG.PetFinder.user.entity.Usuario;

import java.util.List;
import java.util.UUID;

@Repository
public interface MascotaRepository extends JpaRepository<Mascota, UUID> {
    List<Mascota> findAllByUsuario(Usuario usuario);
}