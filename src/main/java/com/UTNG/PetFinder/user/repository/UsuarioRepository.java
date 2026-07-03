package com.UTNG.PetFinder.user.repository;

import com.UTNG.PetFinder.user.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    // Método para buscar un usuario por su correo (muy útil para el login después)
    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    // Método rápido para validar si un correo ya está registrado
    boolean existsByCorreoIgnoreCase(String correo);
}