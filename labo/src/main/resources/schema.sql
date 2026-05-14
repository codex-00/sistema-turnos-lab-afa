CREATE TABLE pacientes (
    id_paciente INT AUTO_INCREMENT PRIMARY KEY,
    dni VARCHAR(20),
    nombre VARCHAR(50),
    apellido VARCHAR(50),
    email VARCHAR(100),
    fecha_registro TIMESTAMP,
    password VARCHAR(100),
    direccion VARCHAR(255),
    telefono VARCHAR(20)
);


CREATE TABLE medicos (
    id_medico INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100),
    apellido VARCHAR(100),
    dni VARCHAR(20),
    email VARCHAR(100),
    password VARCHAR(255),
    direccion VARCHAR(255),
    telefono VARCHAR(20),
    fecha_registro TIMESTAMP,
    especialidad VARCHAR(100)
);

CREATE TABLE disponibilidad (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    medico_id INT,
    dia VARCHAR(20),
    hora_inicio TIME,
    hora_fin TIME,
    duracion_turno INT,

    CONSTRAINT fk_disponibilidad_medico FOREIGN KEY (medico_id) REFERENCES medicos(id_medico)
);

CREATE TABLE agenda (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    medico_id INT,
    paciente_id INT,
    fecha DATE,
    hora TIME,
    disponible BOOLEAN,

    CONSTRAINT fk_agenda_medico FOREIGN KEY (medico_id) REFERENCES medicos(id_medico),
    CONSTRAINT fk_agenda_paciente FOREIGN KEY (paciente_id) REFERENCES pacientes(id_paciente)
);

CREATE TABLE turnos (
    id_turno INT AUTO_INCREMENT PRIMARY KEY,
    id_paciente INT,
    id_medico INT,
    establecimiento VARCHAR(255),
    fecha_creacion TIMESTAMP,
    fecha_de_turno TIMESTAMP,
    
    CONSTRAINT fk_turno_paciente FOREIGN KEY (id_paciente) REFERENCES pacientes(id_paciente),
    CONSTRAINT fk_turno_medico FOREIGN KEY (id_medico) REFERENCES medicos(id_medico)
);

CREATE TABLE estudios (
    id_estudio INT AUTO_INCREMENT PRIMARY KEY,
    id_turno INT,
    -- paciente VARCHAR(100),
    id_paciente INT,
    nombre VARCHAR(100),
    descripcion TEXT,
    fecha_creacion TIMESTAMP,
    fecha_de_turno TIMESTAMP,
    
    CONSTRAINT fk_estudio_turno FOREIGN KEY (id_turno) REFERENCES turnos(id_turno)
);

CREATE TABLE tests (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100)
);
