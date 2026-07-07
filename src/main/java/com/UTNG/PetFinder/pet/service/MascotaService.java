package com.UTNG.PetFinder.pet.service;

import com.UTNG.PetFinder.pet.dto.MascotaCreateRequest;
import com.UTNG.PetFinder.pet.dto.MascotaResponse;
import com.UTNG.PetFinder.pet.dto.MascotaUpdateRequest;
import com.UTNG.PetFinder.pet.entity.*;
import com.UTNG.PetFinder.pet.repository.*;
import com.UTNG.PetFinder.user.entity.Usuario;
import com.UTNG.PetFinder.user.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MascotaService {

    private final MascotaRepository mascotaRepository;
    private final UsuarioRepository usuarioRepository;
    private final EspecieRepository especieRepository;
    private final RazaRepository razaRepository;
    private final ColorRepository colorRepository;

    // ================== MÉTODOS PÚBLICOS ==================

    @Transactional
    public MascotaResponse crearMascota(MascotaCreateRequest dto, String correoAutenticado) {
        Usuario usuario = obtenerUsuarioAutenticado(correoAutenticado);
        Especie especie = obtenerEspecie(dto.getEspecieId());
        Raza raza = obtenerRazaSiPresente(dto.getRazaId(), especie);
        Color color = obtenerColorSiPresente(dto.getColorId());

        Mascota mascota = Mascota.builder()
                .usuario(usuario)
                .especie(especie)
                .raza(raza)
                .color(color)
                .nombre(dto.getNombre())
                .sexo(dto.getSexo())
                .tamano(dto.getTamano())
                .edadAproximada(dto.getEdadAproximada())
                .esterilizado(dto.getEsterilizado())
                .senasParticulares(dto.getSenasParticulares())
                .build();

        mascota = mascotaRepository.save(mascota);
        return convertirDTO(mascota);
    }

    public List<MascotaResponse> obtenerMisMascotas(String correoAutenticado) {
        Usuario usuario = obtenerUsuarioAutenticado(correoAutenticado);
        return mascotaRepository.findAllByUsuario(usuario)
                .stream()
                .map(this::convertirDTO)
                .toList();
    }

    public MascotaResponse obtenerMiMascota(UUID mascotaId, String correoAutenticado) {
        Usuario usuario = obtenerUsuarioAutenticado(correoAutenticado);
        Mascota mascota = obtenerMascotaDelUsuario(mascotaId, usuario);
        return convertirDTO(mascota);
    }

    @Transactional
    public MascotaResponse actualizarMascota(UUID mascotaId, MascotaUpdateRequest dto, String correoAutenticado) {
        Usuario usuario = obtenerUsuarioAutenticado(correoAutenticado);
        Mascota mascota = obtenerMascotaDelUsuario(mascotaId, usuario);

        if (dto.getNombre() != null) mascota.setNombre(dto.getNombre());
        if (dto.getSexo() != null) mascota.setSexo(dto.getSexo());
        if (dto.getTamano() != null) mascota.setTamano(dto.getTamano());
        if (dto.getEdadAproximada() != null) mascota.setEdadAproximada(dto.getEdadAproximada());
        if (dto.getEsterilizado() != null) mascota.setEsterilizado(dto.getEsterilizado());
        if (dto.getSenasParticulares() != null) mascota.setSenasParticulares(dto.getSenasParticulares());

        // Actualizar especie
        if (dto.getEspecieId() != null) {
            Especie nuevaEspecie = obtenerEspecie(dto.getEspecieId());
            mascota.setEspecie(nuevaEspecie);
            if (dto.getRazaId() != null) {
                mascota.setRaza(obtenerRazaSiPresente(dto.getRazaId(), nuevaEspecie));
            } else {
                mascota.setRaza(null);
            }
        } else if (dto.getRazaId() != null) {
            // Solo cambia raza, manteniendo especie actual
            mascota.setRaza(obtenerRazaSiPresente(dto.getRazaId(), mascota.getEspecie()));
        }

        // Actualizar color
        if (dto.getColorId() != null) {
            mascota.setColor(obtenerColorSiPresente(dto.getColorId()));
        } else if (dto.isColorEliminado()) {
            mascota.setColor(null);
        }

        mascota = mascotaRepository.save(mascota);
        return convertirDTO(mascota);
    }

    @Transactional
    public void eliminarMascota(UUID mascotaId, String correoAutenticado) {
        Usuario usuario = obtenerUsuarioAutenticado(correoAutenticado);
        Mascota mascota = obtenerMascotaDelUsuario(mascotaId, usuario);
        mascotaRepository.delete(mascota);
    }

    // ================== MÉTODOS PRIVADOS AUXILIARES ==================

    private Usuario obtenerUsuarioAutenticado(String correo) {
        return usuarioRepository.findByCorreoIgnoreCase(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    private Mascota obtenerMascotaDelUsuario(UUID mascotaId, Usuario usuario) {
        Mascota mascota = mascotaRepository.findById(mascotaId)
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada"));
        if (!mascota.getUsuario().getId().equals(usuario.getId())) {
            throw new AccessDeniedException("No tienes permisos para acceder a esta mascota");
        }
        return mascota;
    }

    private Especie obtenerEspecie(Short especieId) {
        return especieRepository.findById(especieId)
                .orElseThrow(() -> new RuntimeException("La especie no existe"));
    }

    private Raza obtenerRazaSiPresente(Integer razaId, Especie especie) {
        if (razaId == null) return null;
        Raza raza = razaRepository.findById(razaId)
                .orElseThrow(() -> new RuntimeException("La raza no existe"));
        if (!raza.getEspecie().getId().equals(especie.getId())) {
            throw new IllegalArgumentException("La raza no pertenece a la especie seleccionada");
        }
        return raza;
    }

    private Color obtenerColorSiPresente(Short colorId) {
        if (colorId == null) return null;
        return colorRepository.findById(colorId)
                .orElseThrow(() -> new RuntimeException("El color no existe"));
    }

    private MascotaResponse convertirDTO(Mascota mascota) {
        return MascotaResponse.builder()
                .id(mascota.getId())
                .nombre(mascota.getNombre())
                .sexo(mascota.getSexo())
                .tamano(mascota.getTamano())
                .edadAproximada(mascota.getEdadAproximada())
                .esterilizado(mascota.getEsterilizado())
                .senasParticulares(mascota.getSenasParticulares())
                .especieId(mascota.getEspecie().getId())
                .nombreEspecie(mascota.getEspecie().getNombre())
                .razaId(mascota.getRaza() != null ? mascota.getRaza().getId() : null)
                .nombreRaza(mascota.getRaza() != null ? mascota.getRaza().getNombre() : null)
                .colorId(mascota.getColor() != null ? mascota.getColor().getId() : null)
                .nombreColor(mascota.getColor() != null ? mascota.getColor().getNombre() : null)
                .build();
    }

    @Transactional(readOnly = true)
    public List<MascotaResponse> obtenerTodasLasMascotas() {
    return mascotaRepository.findAll()
            .stream()
            .map(this::convertirDTO)
            .toList();
}


}