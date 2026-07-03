-- ============================================================================
-- PLATAFORMA DE MASCOTAS EXTRAVIADAS / ENCONTRADAS / ADOPCIÓN / CELO
-- Esquema relacional PostgreSQL
-- ============================================================================
-- Cubre:
--   RF: Gestión de reportes, búsqueda/filtrado, geolocalización, contacto,
--       cuentas/trazabilidad, refugios/clínicas, auth JWT, registro,
--       Derechos ARCO, Aviso de Privacidad/consentimiento, ciclo de vida de datos
--   RNF: cifrado en reposo (pgcrypto), BOLA (FKs + owner_id en cada tabla),
--        auditoría (audit_logs), rate limiting (login_attempts)
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. TIPOS ENUMERADOS
-- NOTA: este esquema no usa extensiones (sin CREATE EXTENSION). gen_random_uuid()
-- es función nativa del core desde PostgreSQL 13, por lo que no requiere pgcrypto.
-- ----------------------------------------------------------------------------
CREATE TYPE tipo_cuenta        AS ENUM ('ciudadano', 'refugio', 'clinica', 'admin');
CREATE TYPE estado_cuenta      AS ENUM ('activa', 'suspendida', 'bloqueada_arco', 'eliminada');

CREATE TYPE tipo_reporte       AS ENUM ('extraviada', 'encontrada', 'adopcion', 'celo');
CREATE TYPE estado_reporte     AS ENUM ('activo', 'en_proceso', 'resuelto', 'cancelado', 'expirado');

CREATE TYPE sexo_mascota       AS ENUM ('macho', 'hembra', 'desconocido');
CREATE TYPE tamano_mascota     AS ENUM ('pequeno', 'mediano', 'grande');

CREATE TYPE estado_conversacion AS ENUM ('abierta', 'cerrada', 'archivada');

CREATE TYPE tipo_solicitud_arco AS ENUM ('acceso', 'rectificacion', 'cancelacion', 'oposicion');
CREATE TYPE estado_solicitud_arco AS ENUM ('recibida', 'en_revision', 'aprobada', 'rechazada', 'aplicada');

CREATE TYPE accion_auditoria   AS ENUM ('crear', 'leer', 'actualizar', 'eliminar', 'login', 'logout', 'login_fallido');

-- ----------------------------------------------------------------------------
-- 2. MÓDULO DE AUTENTICACIÓN Y CUENTAS
--    (Autenticación / Registro / Administración de cuentas y trazabilidad)
-- ----------------------------------------------------------------------------

CREATE TABLE usuarios (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tipo_cuenta         tipo_cuenta NOT NULL DEFAULT 'ciudadano',
    correo              VARCHAR(255) NOT NULL,
    password_hash       TEXT NOT NULL,                 -- bcrypt/argon2, cifrado unidireccional
    telefono            VARCHAR(20),
    nombre_completo      VARCHAR(150) NOT NULL,
    estado              estado_cuenta NOT NULL DEFAULT 'activa',
    correo_verificado   BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_registro      TIMESTAMPTZ NOT NULL DEFAULT now(),
    ultima_actividad    TIMESTAMPTZ,
    -- soft delete / anonimización para cumplir ARCO sin romper integridad referencial
    anonimizado         BOOLEAN NOT NULL DEFAULT FALSE,
    eliminado_en        TIMESTAMPTZ,
    creado_en           TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- índice único case-insensitive sobre el correo (equivalente a CITEXT sin requerir extensión)
CREATE UNIQUE INDEX idx_usuarios_correo_unico ON usuarios (lower(correo));

CREATE TABLE perfiles_entidad (
    -- datos adicionales para refugios y clínicas (ecosistema institucional)
    usuario_id          UUID PRIMARY KEY REFERENCES usuarios(id) ON DELETE CASCADE,
    razon_social         VARCHAR(200),
    rfc                  VARCHAR(20),
    sitio_web            VARCHAR(255),
    descripcion          TEXT,
    logo_url             TEXT,
    verificado           BOOLEAN NOT NULL DEFAULT FALSE,
    verificado_en        TIMESTAMPTZ
);

-- Tokens de sesión / refresh tokens JWT (control server-side de invalidación/rotación)
CREATE TABLE sesiones (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id          UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    refresh_token_hash  TEXT NOT NULL,          -- nunca se guarda el token en claro
    user_agent          TEXT,
    ip_origen           INET,
    emitido_en          TIMESTAMPTZ NOT NULL DEFAULT now(),
    expira_en           TIMESTAMPTZ NOT NULL,
    revocado            BOOLEAN NOT NULL DEFAULT FALSE,
    revocado_en         TIMESTAMPTZ
);
CREATE INDEX idx_sesiones_usuario ON sesiones(usuario_id);
CREATE INDEX idx_sesiones_expira ON sesiones(expira_en);

-- Rate limiting de login (RNF)
CREATE TABLE intentos_login (
    id                  BIGSERIAL PRIMARY KEY,
    correo              VARCHAR(255) NOT NULL,
    ip_origen           INET NOT NULL,
    exitoso             BOOLEAN NOT NULL,
    creado_en           TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_intentos_login_correo_fecha ON intentos_login(correo, creado_en);
CREATE INDEX idx_intentos_login_ip_fecha ON intentos_login(ip_origen, creado_en);

-- ----------------------------------------------------------------------------
-- 3. CATÁLOGOS (para el motor de búsqueda y filtrado avanzado)
-- ----------------------------------------------------------------------------

CREATE TABLE especies (
    id      SMALLSERIAL PRIMARY KEY,
    nombre  VARCHAR(50) NOT NULL UNIQUE          -- perro, gato, ave, etc.
);

CREATE TABLE razas (
    id          SERIAL PRIMARY KEY,
    especie_id  SMALLINT NOT NULL REFERENCES especies(id),
    nombre      VARCHAR(80) NOT NULL,
    UNIQUE (especie_id, nombre)
);

CREATE TABLE colores (
    id      SMALLSERIAL PRIMARY KEY,
    nombre  VARCHAR(50) NOT NULL UNIQUE
);

-- ----------------------------------------------------------------------------
-- 4. MASCOTAS Y REPORTES (Gestión integral de reportes)
-- ----------------------------------------------------------------------------

CREATE TABLE mascotas (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    especie_id      SMALLINT NOT NULL REFERENCES especies(id),
    raza_id         INTEGER REFERENCES razas(id),
    color_id        SMALLINT REFERENCES colores(id),
    sexo            sexo_mascota NOT NULL DEFAULT 'desconocido',
    tamano          tamano_mascota,
    nombre          VARCHAR(100),
    señas_particulares TEXT,
    edad_aproximada  SMALLINT,
    esterilizado    BOOLEAN,
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE reportes (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mascota_id      UUID NOT NULL REFERENCES mascotas(id) ON DELETE CASCADE,
    propietario_id  UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,   -- dueño legítimo -> control BOLA
    tipo            tipo_reporte NOT NULL,
    estado          estado_reporte NOT NULL DEFAULT 'activo',
    titulo          VARCHAR(150) NOT NULL,
    descripcion     TEXT,
    -- geolocalización interactiva sin PostGIS: coordenadas nativas + índice compuesto
    latitud         DOUBLE PRECISION NOT NULL CHECK (latitud BETWEEN -90 AND 90),
    longitud        DOUBLE PRECISION NOT NULL CHECK (longitud BETWEEN -180 AND 180),
    direccion_referencia VARCHAR(255),
    fecha_evento    DATE,                       -- fecha en que se perdió/encontró
    fecha_publicacion TIMESTAMPTZ NOT NULL DEFAULT now(),
    fecha_expiracion  TIMESTAMPTZ,               -- soporta ciclo de vida / auto-expiración
    fecha_resolucion  TIMESTAMPTZ,
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Índice compuesto para acotar por rango de coordenadas (bounding box) antes de
-- calcular distancia exacta en la aplicación (fórmula de Haversine)
CREATE INDEX idx_reportes_lat_lon      ON reportes (latitud, longitud);
CREATE INDEX idx_reportes_tipo_estado ON reportes (tipo, estado);
CREATE INDEX idx_reportes_propietario ON reportes (propietario_id);
CREATE INDEX idx_reportes_fecha_pub   ON reportes (fecha_publicacion DESC);
-- motor de búsqueda por texto (título/descripción) con full-text search nativo (sin pg_trgm)
ALTER TABLE reportes ADD COLUMN busqueda_texto tsvector
    GENERATED ALWAYS AS (
        setweight(to_tsvector('spanish', coalesce(titulo, '')), 'A') ||
        setweight(to_tsvector('spanish', coalesce(descripcion, '')), 'B')
    ) STORED;
CREATE INDEX idx_reportes_busqueda_texto ON reportes USING GIN (busqueda_texto);

CREATE TABLE reporte_fotos (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reporte_id  UUID NOT NULL REFERENCES reportes(id) ON DELETE CASCADE,
    url         TEXT NOT NULL,
    orden       SMALLINT NOT NULL DEFAULT 0,
    creado_en   TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_reporte_fotos_reporte ON reporte_fotos(reporte_id);

-- Avistamientos: otros usuarios pueden reportar haber visto a la mascota
-- (alimenta el mapa de geolocalización en tiempo real)
CREATE TABLE avistamientos (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reporte_id  UUID NOT NULL REFERENCES reportes(id) ON DELETE CASCADE,
    usuario_id  UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    latitud     DOUBLE PRECISION NOT NULL CHECK (latitud BETWEEN -90 AND 90),
    longitud    DOUBLE PRECISION NOT NULL CHECK (longitud BETWEEN -180 AND 180),
    comentario  TEXT,
    creado_en   TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_avistamientos_lat_lon ON avistamientos (latitud, longitud);
CREATE INDEX idx_avistamientos_reporte   ON avistamientos(reporte_id);

-- Publicaciones guardadas / seguimiento por parte del usuario (dashboard)
CREATE TABLE reportes_seguidos (
    usuario_id  UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    reporte_id  UUID NOT NULL REFERENCES reportes(id) ON DELETE CASCADE,
    creado_en   TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (usuario_id, reporte_id)
);

-- Búsquedas guardadas con criterios del motor de filtrado avanzado
CREATE TABLE busquedas_guardadas (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id  UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    nombre      VARCHAR(100),
    filtros     JSONB NOT NULL,     -- {especie_id, raza_id, color_id, tipo, radio_km, lat, lon, ...}
    notificar   BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en   TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_busquedas_filtros ON busquedas_guardadas USING GIN (filtros);

-- ----------------------------------------------------------------------------
-- 5. SISTEMA DE VINCULACIÓN Y CONTACTO
-- ----------------------------------------------------------------------------

CREATE TABLE conversaciones (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reporte_id  UUID NOT NULL REFERENCES reportes(id) ON DELETE CASCADE,
    iniciador_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    receptor_id  UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    estado      estado_conversacion NOT NULL DEFAULT 'abierta',
    creado_en   TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (reporte_id, iniciador_id, receptor_id)
);
CREATE INDEX idx_conversaciones_iniciador ON conversaciones(iniciador_id);
CREATE INDEX idx_conversaciones_receptor  ON conversaciones(receptor_id);

CREATE TABLE mensajes (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversacion_id UUID NOT NULL REFERENCES conversaciones(id) ON DELETE CASCADE,
    remitente_id    UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    contenido       TEXT NOT NULL,
    leido           BOOLEAN NOT NULL DEFAULT FALSE,
    enviado_en      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_mensajes_conversacion ON mensajes(conversacion_id, enviado_en);

-- ----------------------------------------------------------------------------
-- 6. ECOSISTEMA INSTITUCIONAL: REFUGIOS Y CLÍNICAS
-- ----------------------------------------------------------------------------

-- Catálogo de adopciones publicado por refugios (distinto de "reportes" ciudadanos,
-- aunque puede enlazarse a un reporte tipo 'adopcion')
CREATE TABLE catalogo_adopcion (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    refugio_id  UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    mascota_id  UUID NOT NULL REFERENCES mascotas(id) ON DELETE CASCADE,
    reporte_id  UUID REFERENCES reportes(id) ON DELETE SET NULL,
    disponible  BOOLEAN NOT NULL DEFAULT TRUE,
    costo_adopcion NUMERIC(10,2),
    creado_en   TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_catalogo_refugio ON catalogo_adopcion(refugio_id);

CREATE TABLE servicios_clinica (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    clinica_id  UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    nombre      VARCHAR(150) NOT NULL,
    descripcion TEXT,
    precio      NUMERIC(10,2),
    activo      BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en   TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_servicios_clinica ON servicios_clinica(clinica_id);

-- ----------------------------------------------------------------------------
-- 7. PRIVACIDAD: AVISO DE PRIVACIDAD Y CONSENTIMIENTO
-- ----------------------------------------------------------------------------

CREATE TABLE avisos_privacidad (
    id          SERIAL PRIMARY KEY,
    version     VARCHAR(20) NOT NULL UNIQUE,
    tipo        VARCHAR(20) NOT NULL CHECK (tipo IN ('integral', 'simplificado')),
    contenido   TEXT NOT NULL,
    vigente_desde TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE consentimientos (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id          UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    aviso_privacidad_id INTEGER NOT NULL REFERENCES avisos_privacidad(id),
    aceptado            BOOLEAN NOT NULL,          -- registra el estado del checkbox no premarcado
    ip_origen           INET,
    aceptado_en         TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_consentimientos_usuario ON consentimientos(usuario_id);

-- ----------------------------------------------------------------------------
-- 8. MÓDULO DE DERECHOS ARCO
-- ----------------------------------------------------------------------------

CREATE TABLE solicitudes_arco (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id      UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    tipo            tipo_solicitud_arco NOT NULL,
    estado          estado_solicitud_arco NOT NULL DEFAULT 'recibida',
    detalle         TEXT,                          -- ej. qué dato se solicita rectificar
    resolucion      TEXT,
    solicitado_en   TIMESTAMPTZ NOT NULL DEFAULT now(),
    resuelto_en     TIMESTAMPTZ,
    resuelto_por    UUID REFERENCES usuarios(id)     -- admin que gestionó la solicitud
);
CREATE INDEX idx_arco_usuario ON solicitudes_arco(usuario_id);
CREATE INDEX idx_arco_estado  ON solicitudes_arco(estado);

-- ----------------------------------------------------------------------------
-- 9. GESTIÓN AUTOMATIZADA DEL CICLO DE VIDA DE DATOS
-- ----------------------------------------------------------------------------

CREATE TABLE politicas_retencion (
    id                  SERIAL PRIMARY KEY,
    entidad             VARCHAR(50) NOT NULL,        -- ej. 'reportes', 'usuarios', 'mensajes'
    dias_retencion      INTEGER NOT NULL,
    accion              VARCHAR(20) NOT NULL CHECK (accion IN ('anonimizar', 'eliminar')),
    activo              BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en           TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Bitácora de ejecución del job automático de borrado/anonimización
CREATE TABLE ejecuciones_retencion (
    id                  BIGSERIAL PRIMARY KEY,
    politica_id         INTEGER NOT NULL REFERENCES politicas_retencion(id),
    registros_afectados INTEGER NOT NULL DEFAULT 0,
    ejecutado_en        TIMESTAMPTZ NOT NULL DEFAULT now(),
    detalle             TEXT
);

-- ----------------------------------------------------------------------------
-- 10. AUDITORÍA (Logs y bitácoras)
-- ----------------------------------------------------------------------------

CREATE TABLE audit_logs (
    id          BIGSERIAL PRIMARY KEY,
    usuario_id  UUID REFERENCES usuarios(id) ON DELETE SET NULL,
    accion      accion_auditoria NOT NULL,
    entidad     VARCHAR(50) NOT NULL,       -- tabla/recurso afectado
    entidad_id  TEXT,                       -- id del registro afectado
    detalle     JSONB,                      -- payload/diferencias relevantes
    ip_origen   INET,
    creado_en   TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_audit_usuario ON audit_logs(usuario_id);
CREATE INDEX idx_audit_fecha   ON audit_logs(creado_en);
CREATE INDEX idx_audit_entidad ON audit_logs(entidad, entidad_id);

-- ----------------------------------------------------------------------------
-- 11. FUNCIONES DE APOYO: DISTANCIA GEOGRÁFICA SIN POSTGIS (fórmula de Haversine)
-- ----------------------------------------------------------------------------
-- Devuelve la distancia en metros entre dos coordenadas. Se usa en el motor de
-- búsqueda para geolocalización interactiva: WHERE fn_distancia_metros(...) <= radio
CREATE OR REPLACE FUNCTION fn_distancia_metros(
    lat1 DOUBLE PRECISION, lon1 DOUBLE PRECISION,
    lat2 DOUBLE PRECISION, lon2 DOUBLE PRECISION
) RETURNS DOUBLE PRECISION AS $$
DECLARE
    radio_tierra CONSTANT DOUBLE PRECISION := 6371000; -- metros
    d_lat DOUBLE PRECISION;
    d_lon DOUBLE PRECISION;
    a DOUBLE PRECISION;
BEGIN
    d_lat := radians(lat2 - lat1);
    d_lon := radians(lon2 - lon1);
    a := sin(d_lat/2)^2 + cos(radians(lat1)) * cos(radians(lat2)) * sin(d_lon/2)^2;
    RETURN radio_tierra * 2 * atan2(sqrt(a), sqrt(1-a));
END;
$$ LANGUAGE plpgsql IMMUTABLE;

-- Ejemplo de uso para "reportes a 5 km a la redonda":
-- SELECT * FROM reportes
-- WHERE latitud BETWEEN :lat - 0.05 AND :lat + 0.05          -- bounding box (usa idx_reportes_lat_lon)
--   AND longitud BETWEEN :lon - 0.05 AND :lon + 0.05
--   AND fn_distancia_metros(latitud, longitud, :lat, :lon) <= 5000;

-- ----------------------------------------------------------------------------
-- 12. TRIGGERS DE APOYO
-- ----------------------------------------------------------------------------

-- 11.1 actualizado_en automático
CREATE OR REPLACE FUNCTION fn_actualizar_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.actualizado_en = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_usuarios_actualizado
    BEFORE UPDATE ON usuarios
    FOR EACH ROW EXECUTE FUNCTION fn_actualizar_timestamp();

CREATE TRIGGER trg_reportes_actualizado
    BEFORE UPDATE ON reportes
    FOR EACH ROW EXECUTE FUNCTION fn_actualizar_timestamp();

-- 11.2 Auditoría automática de cambios en reportes (ejemplo extensible a otras tablas)
CREATE OR REPLACE FUNCTION fn_auditar_reportes()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO audit_logs (usuario_id, accion, entidad, entidad_id, detalle)
    VALUES (
        COALESCE(NEW.propietario_id, OLD.propietario_id),
        CASE TG_OP WHEN 'INSERT' THEN 'crear'::accion_auditoria
                    WHEN 'UPDATE' THEN 'actualizar'::accion_auditoria
                    WHEN 'DELETE' THEN 'eliminar'::accion_auditoria END,
        'reportes',
        COALESCE(NEW.id, OLD.id)::TEXT,
        jsonb_build_object('operacion', TG_OP)
    );
    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_auditar_reportes
    AFTER INSERT OR UPDATE OR DELETE ON reportes
    FOR EACH ROW EXECUTE FUNCTION fn_auditar_reportes();

-- ----------------------------------------------------------------------------
-- 13. VISTA DE APOYO PARA EL DASHBOARD (Administración de cuentas y trazabilidad)
-- ----------------------------------------------------------------------------

CREATE VIEW vw_reportes_por_usuario AS
SELECT
    r.propietario_id AS usuario_id,
    r.id              AS reporte_id,
    r.tipo,
    r.estado,
    r.titulo,
    r.fecha_publicacion,
    m.especie_id,
    r.latitud,
    r.longitud
FROM reportes r
JOIN mascotas m ON m.id = r.mascota_id;

-- ============================================================================
-- FIN DEL ESQUEMA
-- ============================================================================
