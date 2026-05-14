-- Médicos
---Agregar dni a médicos
INSERT INTO medicos (nombre, apellido, direccion, telefono, email, especialidad, password, fecha_registro)
VALUES
('Laura', 'Suárez', 'Av. Siempre Viva 123', '111111111', 'laura@clinica.com', 'Pediatria', 'clave1', CURRENT_TIMESTAMP),
('Carlos', 'Gómez', 'Calle Falsa 456', '222222222', 'carlos@salud.com', 'Traumatologia', 'clave2', CURRENT_TIMESTAMP);

-- Pacientes
INSERT INTO pacientes (nombre, apellido, direccion, telefono, email, dni, password, fecha_registro) 
VALUES
('Ana', 'Martínez', 'Pasaje Azul 789', '333333333', 'ana@gmail.com', '12345678', 'clave3', CURRENT_TIMESTAMP),
('Lucas', 'Fernández', 'Calle Verde 321', '444444444', 'lucas@hotmail.com', '87654321', 'clave4', CURRENT_TIMESTAMP);

-- Turnos
INSERT INTO turnos (id_paciente, id_medico, establecimiento, fecha_creacion, fecha_de_turno) 
VALUES
(1, 1, 'Hospital Central', CURRENT_TIMESTAMP, DATEADD('DAY', 1, CURRENT_TIMESTAMP)),
(2, 2, 'Clínica Sur', CURRENT_TIMESTAMP, DATEADD('DAY', 3, CURRENT_TIMESTAMP)),
(1, 2, 'Hospital Central', CURRENT_TIMESTAMP, DATEADD('DAY', 4, CURRENT_TIMESTAMP)),
(2, 1, 'Clínica Sur', CURRENT_TIMESTAMP, DATEADD('DAY', 5, CURRENT_TIMESTAMP));

-- Estudios
INSERT INTO estudios (id_turno, id_paciente, nombre, descripcion, fecha_creacion, fecha_de_turno)
VALUES
(1, 1, 'Radiografía', 'Radiografía de tórax para evaluación de pulmones.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 2, 'Análisis de sangre', 'Análisis completo de sangre para chequeo general.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Tests
INSERT INTO tests (id, nombre)
VALUES
(1, 'Radiografía'),
(2, 'Análisis de sangre');