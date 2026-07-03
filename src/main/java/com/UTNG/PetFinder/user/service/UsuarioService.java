package com.UTNG.PetFinder.user.service;

import com.UTNG.PetFinder.user.dto.UsuarioRegistroDTO;
import com.UTNG.PetFinder.user.dto.UsuarioResponseDTO;
import com.UTNG.PetFinder.user.entity.Usuario;
import com.UTNG.PetFinder.user.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.UTNG.PetFinder.user.dto.UsuarioActualizacionDTO;

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
                .tipoCuenta(usuarioGuardado.getTipoCuenta().name())
                .estado(usuarioGuardado.getEstado().name())
                .fechaRegistro(usuarioGuardado.getFechaRegistro())
                .build();
    }

    public UsuarioResponseDTO obtenerUsuarioPorId(java.util.UUID id) {
        // Buscamos el usuario. Si no existe, lanzamos un error que luego podemos manejar
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con el ID: " + id));

        // Reutilizamos el patrón Builder para devolver la información limpia
        return UsuarioResponseDTO.builder()
                .id(usuario.getId())
                .nombreCompleto(usuario.getNombreCompleto())
                .correo(usuario.getCorreo())
                .telefono(usuario.getTelefono())
                .tipoCuenta(usuario.getTipoCuenta().name())
                .estado(usuario.getEstado().name())
                .fechaRegistro(usuario.getFechaRegistro())
                .build();
    }

    @Transactional
    public UsuarioResponseDTO actualizarUsuario(java.util.UUID id, UsuarioActualizacionDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con el ID: " + id));

        // Actualizamos solo si el frontend nos envió un dato nuevo
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
                .tipoCuenta(usuario.getTipoCuenta().name())
                .estado(usuario.getEstado().name())
                .fechaRegistro(usuario.getFechaRegistro())
                .build();
    }

    @Transactional
    public void eliminarUsuario(java.util.UUID id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con el ID: " + id));

        // Aplicamos el borrado lógico (Soft Delete)
        usuario.setAnonimizado(true);
        usuario.setEliminadoEn(java.time.OffsetDateTime.now());
        // Asegúrate de importar tu enum EstadoCuenta si no lo tienes importado en este archivo
        usuario.setEstado(com.UTNG.PetFinder.auth.entity.EstadoCuenta.eliminada);

        // Al usar @Transactional, Hibernate hará el UPDATE de estos 3 campos automáticamente
    }
}