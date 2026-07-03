package com.UTNG.PetFinder.user.entity;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.UTNG.PetFinder.auth.entity.EstadoCuenta;
import com.UTNG.PetFinder.auth.entity.TipoCuenta;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "usuarios")
@Data // <--- ESTA ES LA ANOTACIÓN QUE RESUELVE EL ERROR (Genera los Getters y Setters)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // tipo_cuenta es un ENUM nativo de Postgres -> requiere JdbcTypeCode NAMED_ENUM
    // (Hibernate 6+)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "tipo_cuenta", nullable = false, columnDefinition = "tipo_cuenta")
    @Builder.Default
    private TipoCuenta tipoCuenta = TipoCuenta.ciudadano;

    @Column(name = "correo", nullable = false, length = 255)
    private String correo;

    @Column(name = "password_hash", nullable = false, columnDefinition = "TEXT")
    private String passwordHash;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "nombre_completo", nullable = false, length = 150)
    private String nombreCompleto;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "estado", nullable = false, columnDefinition = "estado_cuenta")
    @Builder.Default
    private EstadoCuenta estado = EstadoCuenta.activa;

    @Column(name = "correo_verificado", nullable = false)
    @Builder.Default
    private Boolean correoVerificado = false;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime fechaRegistro = OffsetDateTime.now();

    @Column(name = "ultima_actividad")
    private OffsetDateTime ultimaActividad;

    // --- soft delete / anonimización (ARCO) ---
    @Column(name = "anonimizado", nullable = false)
    @Builder.Default
    private Boolean anonimizado = false;

    @Column(name = "eliminado_en")
    private OffsetDateTime eliminadoEn;

    @Column(name = "creado_en", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime creadoEn = OffsetDateTime.now();

    @Column(name = "actualizado_en", nullable = false)
    @Builder.Default
    private OffsetDateTime actualizadoEn = OffsetDateTime.now();

    @PrePersist
    protected void onCreate() {
        OffsetDateTime ahora = OffsetDateTime.now();
        this.creadoEn = ahora;
        this.actualizadoEn = ahora;
        if (this.fechaRegistro == null) {
            this.fechaRegistro = ahora;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.actualizadoEn = OffsetDateTime.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Por ahora, devolvemos el rol basado en el enum TipoCuenta
        return List.of(new SimpleGrantedAuthority("ROLE_" + tipoCuenta.name().toUpperCase()));
    }

    @Override
    public String getPassword() {
        return passwordHash; // Retornamos el campo de tu tabla
    }

    @Override
    public String getUsername() {
        return correo; // Usamos el correo como identificador único
    }

    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return true; }
}