CREATE TABLE pacientes (
    id_paciente INT AUTO_INCREMENT PRIMARY KEY,
    dni VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(50),
    apellido VARCHAR(50),
    email VARCHAR(100) UNIQUE,
    fecha_registro TIMESTAMP,
    password VARCHAR(255),
    direccion VARCHAR(255),
    telefono VARCHAR(20),
    estado VARCHAR(20) DEFAULT 'ACTIVO'
);


CREATE TABLE medicos (
    id_medico INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100),
    apellido VARCHAR(100),
    dni VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(100) UNIQUE,
    password VARCHAR(255),
    direccion VARCHAR(255),
    telefono VARCHAR(20),
    fecha_registro TIMESTAMP,
    especialidad VARCHAR(100),
    estado VARCHAR(20) DEFAULT 'ACTIVO'
);

CREATE TABLE administradores (
    id_administrador INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100),
    apellido VARCHAR(100),
    email VARCHAR(100) UNIQUE,
    password VARCHAR(255),
    direccion VARCHAR(255),
    telefono VARCHAR(20),
    fecha_registro TIMESTAMP,
    estado VARCHAR(20) DEFAULT 'ACTIVO'
);

CREATE TABLE password_reset_tokens (
    id_reset_token INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100),
    rol VARCHAR(30),
    token_hash VARCHAR(128),
    fecha_creacion TIMESTAMP,
    fecha_expiracion TIMESTAMP,
    usado BOOLEAN DEFAULT FALSE
);

CREATE TABLE disponibilidad (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    medico_id INT,
    dia VARCHAR(20),
    hora_inicio TIME,
    hora_fin TIME,
    duracion_turno INT,
    especialidad VARCHAR(100),

    CONSTRAINT fk_disponibilidad_medico FOREIGN KEY (medico_id) REFERENCES medicos(id_medico)
);

CREATE TABLE agenda (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    medico_id INT,
    paciente_id INT,
    fecha DATE,
    hora TIME,
    disponible BOOLEAN,
    descripcion VARCHAR(255),
    estado VARCHAR(50),

    CONSTRAINT fk_agenda_medico FOREIGN KEY (medico_id) REFERENCES medicos(id_medico),
    CONSTRAINT fk_agenda_paciente FOREIGN KEY (paciente_id) REFERENCES pacientes(id_paciente),
    CONSTRAINT uq_agenda_medico_fecha_hora UNIQUE (medico_id, fecha, hora)
);

CREATE TABLE turnos (
    id_turno INT AUTO_INCREMENT PRIMARY KEY,
    id_paciente INT,
    id_medico INT,
    fecha_creacion TIMESTAMP,
    fecha_de_turno TIMESTAMP,
    estado VARCHAR(30) DEFAULT 'PENDIENTE',
    
    CONSTRAINT fk_turno_paciente FOREIGN KEY (id_paciente) REFERENCES pacientes(id_paciente),
    CONSTRAINT fk_turno_medico FOREIGN KEY (id_medico) REFERENCES medicos(id_medico)
);

CREATE TABLE estudios (
    id_estudio INT AUTO_INCREMENT PRIMARY KEY,
    id_turno INT,
    -- paciente VARCHAR(100),
    id_paciente INT,
    id_medico INT,
    nombre VARCHAR(100),
    descripcion TEXT,
    fecha_creacion TIMESTAMP,
    fecha_de_turno TIMESTAMP,
    nombre_archivo VARCHAR(255),
    nombre_archivo_interno VARCHAR(255),
    tipo_archivo VARCHAR(100),
    ruta_archivo VARCHAR(1024),
    tamano_archivo BIGINT,
    
    CONSTRAINT fk_estudio_turno FOREIGN KEY (id_turno) REFERENCES turnos(id_turno),
    CONSTRAINT fk_estudio_paciente FOREIGN KEY (id_paciente) REFERENCES pacientes(id_paciente),
    CONSTRAINT fk_estudio_medico FOREIGN KEY (id_medico) REFERENCES medicos(id_medico)
);
