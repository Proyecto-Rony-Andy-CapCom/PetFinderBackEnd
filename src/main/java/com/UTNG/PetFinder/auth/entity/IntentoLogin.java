package com.UTNG.PetFinder.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * Registro de intentos de login para rate limiting (RNF).
 * No tiene FK a usuarios porque el login puede fallar antes de identificar
 * un usuario existente (se guarda el correo tal cual se intentó).
 */
@Entity
@Table(name = "intentos_login", indexes = {
        @Index(name = "idx_intentos_login_correo_fecha", columnList = "correo, creado_en"),
        @Index(name = "idx_intentos_login_ip_fecha", columnList = "ip_origen, creado_en")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntentoLogin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // BIGSERIAL
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "correo", nullable = false, length = 255)
    private String correo;

    // INET nativo de Postgres: mapeado como String (ver nota en Sesion.java)
    @Column(name = "ip_origen", nullable = false, columnDefinition = "inet")
    private String ipOrigen;

    @Column(name = "exitoso", nullable = false)
    private Boolean exitoso;

    @Column(name = "creado_en", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime creadoEn = OffsetDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (this.creadoEn == null) {
            this.creadoEn = OffsetDateTime.now();
        }
    }
}