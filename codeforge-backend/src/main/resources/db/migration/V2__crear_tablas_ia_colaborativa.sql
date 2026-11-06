-- =========================================================
-- V2: TABLAS PARA IA DESARROLLADORA COLABORATIVA
-- =========================================================

CREATE TABLE IF NOT EXISTS ia_conversacion (
    id_conversacion BIGSERIAL PRIMARY KEY,
    id_proyecto BIGINT NOT NULL UNIQUE REFERENCES proyecto(id_proyecto) ON DELETE CASCADE,
    titulo VARCHAR(150),
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ia_propuesta (
    id_propuesta BIGSERIAL PRIMARY KEY,
    id_proyecto BIGINT NOT NULL REFERENCES proyecto(id_proyecto) ON DELETE CASCADE,
    id_modelo BIGINT NOT NULL REFERENCES modelo(id_modelo) ON DELETE CASCADE,
    id_conversacion BIGINT NOT NULL REFERENCES ia_conversacion(id_conversacion) ON DELETE CASCADE,
    version_origen VARCHAR(30) NOT NULL,
    titulo VARCHAR(200) NOT NULL,
    descripcion_problema TEXT,
    solucion_propuesta TEXT,
    riesgos_efectos TEXT,
    operaciones_json TEXT NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    id_solicitante BIGINT NOT NULL REFERENCES usuario(id_usuario),
    id_aprobador BIGINT REFERENCES usuario(id_usuario),
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_decision TIMESTAMP,
    fecha_aplicacion TIMESTAMP,
    version_resultante VARCHAR(30),
    resultado_aplicacion TEXT
);

CREATE TABLE IF NOT EXISTS ia_mensaje (
    id_mensaje BIGSERIAL PRIMARY KEY,
    id_conversacion BIGINT NOT NULL REFERENCES ia_conversacion(id_conversacion) ON DELETE CASCADE,
    id_usuario BIGINT REFERENCES usuario(id_usuario) ON DELETE SET NULL,
    nombre_emisor VARCHAR(120) NOT NULL,
    tipo_emisor VARCHAR(30) NOT NULL,
    modo_ia VARCHAR(40),
    contenido TEXT NOT NULL,
    metadatos_json TEXT,
    id_propuesta_asociada BIGINT REFERENCES ia_propuesta(id_propuesta) ON DELETE SET NULL,
    fecha_envio TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ia_metrica (
    id_metrica BIGSERIAL PRIMARY KEY,
    id_proyecto BIGINT REFERENCES proyecto(id_proyecto) ON DELETE CASCADE,
    id_usuario BIGINT REFERENCES usuario(id_usuario) ON DELETE SET NULL,
    modelo_ia VARCHAR(80),
    modo_ia VARCHAR(50),
    prompt_tokens INT,
    completion_tokens INT,
    total_tokens INT,
    duracion_ms BIGINT,
    exitoso BOOLEAN NOT NULL DEFAULT TRUE,
    mensaje_error TEXT,
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_ia_mensaje_conversacion ON ia_mensaje(id_conversacion);
CREATE INDEX IF NOT EXISTS idx_ia_propuesta_proyecto ON ia_propuesta(id_proyecto);
CREATE INDEX IF NOT EXISTS idx_ia_propuesta_estado ON ia_propuesta(estado);
