package com.UTNG.PetFinder.auth.entity;

import java.time.OffsetDateTime;
import java.util.UUID;
import com.UTNG.PetFinder.user.entity.Usuario;
import jakarta.persistence.*;
import lombok.*;

/**
 * Tokens de sesión / refresh tokens JWT (control server-side de
 * invalidación/rotación).
 * ip_origen es INET en Postgres; se mapea como String (ver nota en el campo).
 */
@Entity
@Table(name = "sesiones", indexes = {
        @Index(name = "idx_sesiones_usuario", columnList = "usuario_id"),
        @Index(name = "idx_sesiones_expira", columnList = "expira_en")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sesion {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "refresh_token_hash", nullable = false, columnDefinition = "TEXT")
    private String refreshTokenHash;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    // INET nativo de Postgres: se mapea como String; si necesitas el tipo
    // exacto usa columnDefinition = "inet" + un @JdbcTypeCode personalizado.
    @Column(name = "ip_origen", columnDefinition = "inet")
    private String ipOrigen;

    @Column(name = "emitido_en", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime emitidoEn = OffsetDateTime.now();

    @Column(name = "expira_en", nullable = false)
    private OffsetDateTime expiraEn;

    @Column(name = "revocado", nullable = false)
    @Builder.Default
    private Boolean revocado = false;

    @Column(name = "revocado_en")
    private OffsetDateTime revocadoEn;

    @PrePersist
    protected void onCreate() {
        if (this.emitidoEn == null) {
            this.emitidoEn = OffsetDateTime.now();
        }
    }
}