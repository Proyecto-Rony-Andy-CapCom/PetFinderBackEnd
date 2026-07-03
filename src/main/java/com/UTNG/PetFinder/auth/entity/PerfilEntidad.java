package com.UTNG.PetFinder.auth.entity;

import java.time.OffsetDateTime;
import java.util.UUID;
import com.UTNG.PetFinder.user.entity.Usuario;
import jakarta.persistence.*;
import lombok.*;

/**
 * Datos adicionales para refugios y clínicas (ecosistema institucional).
 * usuario_id es a la vez PK y FK -> se modela con @MapsId sobre una
 * relación @OneToOne (clave primaria compartida).
 */
@Entity
@Table(name = "perfiles_entidad")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerfilEntidad {

    @Id
    @Column(name = "usuario_id")
    private UUID usuarioId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "razon_social", length = 200)
    private String razonSocial;

    @Column(name = "rfc", length = 20)
    private String rfc;

    @Column(name = "sitio_web", length = 255)
    private String sitioWeb;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "logo_url", columnDefinition = "TEXT")
    private String logoUrl;

    @Column(name = "verificado", nullable = false)
    @Builder.Default
    private Boolean verificado = false;

    @Column(name = "verificado_en")
    private OffsetDateTime verificadoEn;
}