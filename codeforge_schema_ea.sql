-- =====================================================================
-- SCRIPT DDL PARA IMPORTACIÓN AUTOMÁTICA EN ENTERPRISE ARCHITECT
-- PROYECTO: CodeForge (INF 422 - UAGRM)
-- COMPATIBILIDAD: MySQL 8.0 / PostgreSQL / Estándar DDL
-- INSTRUCCIONES EN ENTERPRISE ARCHITECT:
-- 1. En Enterprise Architect ir a: Develop -> Data Modeling -> Import DB Schema from Script
--    (o Ribbon: Code / Tools -> Database Engineering -> Import DDL)
-- 2. Seleccionar este archivo .sql y presionar "Import".
-- 3. Arrastrar las tablas a tu diagrama y tendrás todo con sus tipos y relaciones exactas.
-- =====================================================================

DROP TABLE IF EXISTS ia_metrica;
DROP TABLE IF EXISTS ia_propuesta;
DROP TABLE IF EXISTS ia_mensaje;
DROP TABLE IF EXISTS ia_conversacion;
DROP TABLE IF EXISTS modelo;
DROP TABLE IF EXISTS invitacion;
DROP TABLE IF EXISTS proyecto_colaborador;
DROP TABLE IF EXISTS proyecto;
DROP TABLE IF EXISTS usuario_rol;
DROP TABLE IF EXISTS rol;
DROP TABLE IF EXISTS usuario;

-- ---------------------------------------------------------------------
-- TABLA: usuario
-- ---------------------------------------------------------------------
CREATE TABLE usuario (
    id_usuario BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    correo VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ultimo_acceso TIMESTAMP NULL,
    PRIMARY KEY (id_usuario)
);

-- ---------------------------------------------------------------------
-- TABLA: rol
-- ---------------------------------------------------------------------
CREATE TABLE rol (
    id_rol BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(255) NULL,
    PRIMARY KEY (id_rol)
);

-- ---------------------------------------------------------------------
-- TABLA ASOCIATIVA: usuario_rol
-- ---------------------------------------------------------------------
CREATE TABLE usuario_rol (
    id_usuario BIGINT NOT NULL,
    id_rol BIGINT NOT NULL,
    PRIMARY KEY (id_usuario, id_rol),
    CONSTRAINT fk_usuariorol_usuario FOREIGN KEY (id_usuario) 
        REFERENCES usuario (id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_usuariorol_rol FOREIGN KEY (id_rol) 
        REFERENCES rol (id_rol) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- TABLA: proyecto
-- ---------------------------------------------------------------------
CREATE TABLE proyecto (
    id_proyecto BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(150) NOT NULL,
    descripcion VARCHAR(500) NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    id_propietario BIGINT NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NULL,
    PRIMARY KEY (id_proyecto),
    CONSTRAINT fk_proyecto_propietario FOREIGN KEY (id_propietario) 
        REFERENCES usuario (id_usuario) ON DELETE RESTRICT
);

-- ---------------------------------------------------------------------
-- TABLA: proyecto_colaborador
-- ---------------------------------------------------------------------
CREATE TABLE proyecto_colaborador (
    id_proyecto_colaborador BIGINT NOT NULL AUTO_INCREMENT,
    id_proyecto BIGINT NOT NULL,
    id_usuario BIGINT NOT NULL,
    rol_proyecto VARCHAR(30) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    fecha_ingreso TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_proyecto_colaborador),
    CONSTRAINT fk_colab_proyecto FOREIGN KEY (id_proyecto) 
        REFERENCES proyecto (id_proyecto) ON DELETE CASCADE,
    CONSTRAINT fk_colab_usuario FOREIGN KEY (id_usuario) 
        REFERENCES usuario (id_usuario) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- TABLA: invitacion
-- ---------------------------------------------------------------------
CREATE TABLE invitacion (
    id_invitacion BIGINT NOT NULL AUTO_INCREMENT,
    id_proyecto BIGINT NOT NULL,
    id_emisor BIGINT NOT NULL,
    id_receptor BIGINT NOT NULL,
    rol_propuesto VARCHAR(30) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    fecha_envio TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_respuesta TIMESTAMP NULL,
    PRIMARY KEY (id_invitacion),
    CONSTRAINT fk_invitacion_proyecto FOREIGN KEY (id_proyecto) 
        REFERENCES proyecto (id_proyecto) ON DELETE CASCADE,
    CONSTRAINT fk_invitacion_emisor FOREIGN KEY (id_emisor) 
        REFERENCES usuario (id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_invitacion_receptor FOREIGN KEY (id_receptor) 
        REFERENCES usuario (id_usuario) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- TABLA: modelo (Diagramas UML 2.5)
-- ---------------------------------------------------------------------
CREATE TABLE modelo (
    id_modelo BIGINT NOT NULL AUTO_INCREMENT,
    id_proyecto BIGINT NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    descripcion VARCHAR(500) NULL,
    version VARCHAR(30) NOT NULL DEFAULT '1.0',
    contenido_json TEXT NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NULL,
    PRIMARY KEY (id_modelo),
    CONSTRAINT fk_modelo_proyecto FOREIGN KEY (id_proyecto) 
        REFERENCES proyecto (id_proyecto) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- TABLA: ia_conversacion
-- ---------------------------------------------------------------------
CREATE TABLE ia_conversacion (
    id_conversacion BIGINT NOT NULL AUTO_INCREMENT,
    id_proyecto BIGINT NOT NULL UNIQUE,
    titulo VARCHAR(150) NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_conversacion),
    CONSTRAINT fk_conversacion_proyecto FOREIGN KEY (id_proyecto) 
        REFERENCES proyecto (id_proyecto) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- TABLA: ia_mensaje
-- ---------------------------------------------------------------------
CREATE TABLE ia_mensaje (
    id_mensaje BIGINT NOT NULL AUTO_INCREMENT,
    id_conversacion BIGINT NOT NULL,
    id_usuario BIGINT NULL,
    nombre_emisor VARCHAR(120) NOT NULL,
    tipo_emisor VARCHAR(30) NOT NULL,
    modo_ia VARCHAR(40) NULL,
    contenido TEXT NOT NULL,
    metadatos_json TEXT NULL,
    id_propuesta_asociada BIGINT NULL,
    fecha_envio TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_mensaje),
    CONSTRAINT fk_mensaje_conversacion FOREIGN KEY (id_conversacion) 
        REFERENCES ia_conversacion (id_conversacion) ON DELETE CASCADE,
    CONSTRAINT fk_mensaje_usuario FOREIGN KEY (id_usuario) 
        REFERENCES usuario (id_usuario) ON DELETE SET NULL
);

-- ---------------------------------------------------------------------
-- TABLA: ia_propuesta (Con soporte de Rollback transaccional)
-- ---------------------------------------------------------------------
CREATE TABLE ia_propuesta (
    id_propuesta BIGINT NOT NULL AUTO_INCREMENT,
    id_proyecto BIGINT NOT NULL,
    id_modelo BIGINT NOT NULL,
    id_conversacion BIGINT NOT NULL,
    id_solicitante BIGINT NOT NULL,
    id_aprobador BIGINT NULL,
    version_origen VARCHAR(30) NOT NULL,
    titulo VARCHAR(200) NOT NULL,
    descripcion_problema TEXT NULL,
    solucion_propuesta TEXT NULL,
    riesgos_efectos TEXT NULL,
    operaciones_json TEXT NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_decision TIMESTAMP NULL,
    fecha_aplicacion TIMESTAMP NULL,
    version_resultante VARCHAR(30) NULL,
    resultado_aplicacion TEXT NULL,
    snapshot_previo_json TEXT NULL,
    PRIMARY KEY (id_propuesta),
    CONSTRAINT fk_propuesta_proyecto FOREIGN KEY (id_proyecto) 
        REFERENCES proyecto (id_proyecto) ON DELETE CASCADE,
    CONSTRAINT fk_propuesta_modelo FOREIGN KEY (id_modelo) 
        REFERENCES modelo (id_modelo) ON DELETE CASCADE,
    CONSTRAINT fk_propuesta_conversacion FOREIGN KEY (id_conversacion) 
        REFERENCES ia_conversacion (id_conversacion) ON DELETE CASCADE,
    CONSTRAINT fk_propuesta_solicitante FOREIGN KEY (id_solicitante) 
        REFERENCES usuario (id_usuario) ON DELETE RESTRICT,
    CONSTRAINT fk_propuesta_aprobador FOREIGN KEY (id_aprobador) 
        REFERENCES usuario (id_usuario) ON DELETE SET NULL
);

-- ---------------------------------------------------------------------
-- TABLA: ia_metrica (Auditoría y tokens de inferencia)
-- ---------------------------------------------------------------------
CREATE TABLE ia_metrica (
    id_metrica BIGINT NOT NULL AUTO_INCREMENT,
    id_proyecto BIGINT NULL,
    id_usuario BIGINT NULL,
    modelo_ia VARCHAR(80) NULL,
    modo_ia VARCHAR(50) NULL,
    prompt_tokens INT NULL,
    completion_tokens INT NULL,
    total_tokens INT NULL,
    duracion_ms BIGINT NULL,
    exitoso BOOLEAN NOT NULL DEFAULT TRUE,
    mensaje_error TEXT NULL,
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_metrica),
    CONSTRAINT fk_metrica_proyecto FOREIGN KEY (id_proyecto) 
        REFERENCES proyecto (id_proyecto) ON DELETE CASCADE,
    CONSTRAINT fk_metrica_usuario FOREIGN KEY (id_usuario) 
        REFERENCES usuario (id_usuario) ON DELETE SET NULL
);

