package com.UTNG.PetFinder.user.service;

import com.UTNG.PetFinder.user.dto.UsuarioRegistroDTO;
import com.UTNG.PetFinder.user.dto.UsuarioResponseDTO;
import com.UTNG.PetFinder.user.entity.Usuario;
import com.UTNG.PetFinder.user.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.UTNG.PetFinder.user.dto.UsuarioActualizacionDTO;
import com.UTNG.PetFinder.auth.entity.EstadoCuenta;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UsuarioResponseDTO registrarUsuario(UsuarioRegistroDTO dto) {
        // 1. Validar que el correo no esté duplicado
        if (usuarioRepository.existsByCorreoIgnoreCase(dto.getCorreo())) {
            throw new IllegalArgumentException("El correo ya está registrado en el sistema.");
        }

        // 2. Construir la entidad aplicando el cifrado unidireccional a la contraseña
        Usuario nuevoUsuario = Usuario.builder()
                .nombreCompleto(dto.getNombreCompleto())
                .correo(dto.getCorreo())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .telefono(dto.getTelefono())
                // tipoCuenta, estado, fechas y booleanos tomarán los defaults de la entidad
                .build();

        // 3. Persistir en la base de datos
        Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);

        // 4. Retornar el DTO limpio sin exponer el hash de la contraseña
        return UsuarioResponseDTO.builder()
                .id(usuarioGuardado.getId())
                .nombreCompleto(usuarioGuardado.getNombreCompleto())
                .correo(usuarioGuardado.getCorreo())
                .telefono(usuarioGuardado.getTelefono())
                .tipoCuenta(usuarioGuardado.getTipoCuenta())
                .estado(usuarioGuardado.getEstado().name())
                .fechaRegistro(usuarioGuardado.getFechaRegistro())
                .build();
    }

    public UsuarioResponseDTO obtenerMiPerfil(String correoAutenticado) {

        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(correoAutenticado)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return UsuarioResponseDTO.builder()
                .id(usuario.getId())
                .nombreCompleto(usuario.getNombreCompleto())
                .correo(usuario.getCorreo())
                .telefono(usuario.getTelefono())
                .tipoCuenta(usuario.getTipoCuenta())
                .estado(usuario.getEstado().name())
                .fechaRegistro(usuario.getFechaRegistro())
                .build();
    }

    @Transactional
    public UsuarioResponseDTO actualizarUsuario(
            UsuarioActualizacionDTO dto,
            String correoAutenticado) {

        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(correoAutenticado)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con el correo"));

        // Protección contra BOLA
        if (!usuario.getCorreo().equals(correoAutenticado)) {
            throw new AccessDeniedException(
                    "No tienes permisos para modificar este usuario");
        }

        if (dto.getNombreCompleto() != null && !dto.getNombreCompleto().isBlank()) {
            usuario.setNombreCompleto(dto.getNombreCompleto());
        }

        if (dto.getTelefono() != null) {
            usuario.setTelefono(dto.getTelefono());
        }

        return UsuarioResponseDTO.builder()
                .id(usuario.getId())
                .nombreCompleto(usuario.getNombreCompleto())
                .correo(usuario.getCorreo())
                .telefono(usuario.getTelefono())
                .tipoCuenta(usuario.getTipoCuenta())
                .estado(usuario.getEstado().name())
                .fechaRegistro(usuario.getFechaRegistro())
                .build();
    }

    @Transactional
    public void eliminarUsuario(String correoAutenticado) {
        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(correoAutenticado)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con el correo: " + correoAutenticado));

        // Aplicamos el borrado lógico (Soft Delete)
        usuario.setAnonimizado(true);
        usuario.setEliminadoEn(java.time.OffsetDateTime.now());
        usuario.setEstado(EstadoCuenta.eliminada);

        // Al usar @Transactional, Hibernate hará el UPDATE de estos 3 campos
        // automáticamente
    }
}