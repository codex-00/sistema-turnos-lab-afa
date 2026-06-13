-- Medicos
INSERT INTO medicos (nombre, apellido, dni, direccion, telefono, email, especialidad, password, fecha_registro)
VALUES
('Laura', 'Suarez', '31122334', 'Av. Siempre Viva 123', '111111111', 'laura@salud.com', 'Pediatria', 'clave1', CURRENT_TIMESTAMP),
('Carlos', 'Gomez', '22334455', 'Calle Falsa 456', '222222222', 'carlos@salud.com', 'Traumatologia', 'clave2', CURRENT_TIMESTAMP),
('Sofia', 'Gomez', '33445566', 'Calle Falsa 458', '333333333', 'sofia.medica@salud.com', 'Traumatologia', 'clave4', CURRENT_TIMESTAMP);

-- Administradores
INSERT INTO administradores (nombre, apellido, email, password, direccion, telefono, fecha_registro)
VALUES
('Admin', 'Sistema', 'admin@clinica.com', 'admin123', 'Clinica', '000000000', CURRENT_TIMESTAMP);

-- Pacientes
INSERT INTO pacientes (nombre, apellido, direccion, telefono, email, dni, password, fecha_registro)
VALUES
('Ana', 'Martinez', 'Pasaje Azul 789', '333333333', 'ana@gmail.com', '12345678', 'clave3', CURRENT_TIMESTAMP),
('Lucas', 'Fernandez', 'Calle Verde 321', '444444444', 'lucas@hotmail.com', '87654321', 'clave4', CURRENT_TIMESTAMP),
('Sofia', 'Fernandez', 'Calle Verde 322', '555555555', 'sofia@hotmail.com', '23456789', 'clave5', CURRENT_TIMESTAMP);

-- Turnos
INSERT INTO turnos (id_paciente, id_medico, fecha_creacion, fecha_de_turno, estado)
VALUES
(1, 1, CURRENT_TIMESTAMP, '2026-06-11 09:00:00', 'PENDIENTE'),
(2, 2, CURRENT_TIMESTAMP, '2026-06-12 14:30:00', 'REALIZADO'),
(1, 2, CURRENT_TIMESTAMP, '2026-06-11 15:00:00', 'PENDIENTE'),
(2, 1, CURRENT_TIMESTAMP, '2026-06-12 11:30:00', 'CANCELADO'),
(1, 1, CURRENT_TIMESTAMP, '2026-06-15 08:00:00', 'REALIZADO'),
(2, 1, CURRENT_TIMESTAMP, '2026-06-15 08:30:00', 'PENDIENTE'),
(3, 1, CURRENT_TIMESTAMP, '2026-06-15 09:00:00', 'REALIZADO'),
(1, 1, CURRENT_TIMESTAMP, '2026-06-15 09:30:00', 'PENDIENTE'),
(2, 1, CURRENT_TIMESTAMP, '2026-06-15 10:00:00', 'REALIZADO'),
(3, 1, CURRENT_TIMESTAMP, '2026-06-15 10:30:00', 'PENDIENTE'),
(1, 1, CURRENT_TIMESTAMP, '2026-06-15 11:00:00', 'REALIZADO'),
(2, 1, CURRENT_TIMESTAMP, '2026-06-15 11:30:00', 'PENDIENTE'),
(1, 2, CURRENT_TIMESTAMP, '2026-06-16 14:00:00', 'REALIZADO'),
(2, 2, CURRENT_TIMESTAMP, '2026-06-16 14:30:00', 'PENDIENTE'),
(3, 2, CURRENT_TIMESTAMP, '2026-06-16 15:00:00', 'REALIZADO'),
(1, 2, CURRENT_TIMESTAMP, '2026-06-16 15:30:00', 'PENDIENTE'),
(2, 2, CURRENT_TIMESTAMP, '2026-06-16 16:00:00', 'REALIZADO'),
(3, 2, CURRENT_TIMESTAMP, '2026-06-16 16:30:00', 'PENDIENTE'),
(1, 2, CURRENT_TIMESTAMP, '2026-06-16 17:00:00', 'REALIZADO'),
(2, 2, CURRENT_TIMESTAMP, '2026-06-16 17:30:00', 'PENDIENTE');

-- Disponibilidad medica para que el paciente pueda elegir fecha y horarios.
INSERT INTO disponibilidad (medico_id, dia, hora_inicio, hora_fin, duracion_turno, especialidad)
VALUES
(1, 'MONDAY', '08:00:00', '12:00:00', 30, 'Pediatria'),
(1, 'TUESDAY', '08:00:00', '12:00:00', 30, 'Pediatria'),
(1, 'WEDNESDAY', '08:00:00', '12:00:00', 30, 'Pediatria'),
(1, 'THURSDAY', '08:00:00', '12:00:00', 30, 'Pediatria'),
(1, 'FRIDAY', '08:00:00', '12:00:00', 30, 'Pediatria'),
(2, 'MONDAY', '14:00:00', '18:00:00', 30, 'Traumatologia'),
(2, 'TUESDAY', '14:00:00', '18:00:00', 30, 'Traumatologia'),
(2, 'WEDNESDAY', '14:00:00', '18:00:00', 30, 'Traumatologia'),
(2, 'THURSDAY', '14:00:00', '18:00:00', 30, 'Traumatologia'),
(2, 'FRIDAY', '14:00:00', '18:00:00', 30, 'Traumatologia');

-- Estudios
INSERT INTO estudios (id_turno, id_paciente, id_medico, nombre, descripcion, fecha_creacion, fecha_de_turno, nombre_archivo, nombre_archivo_interno, tipo_archivo, ruta_archivo)
VALUES
(1, 1, 1, 'Estudio de Sangre', 'Analisis completo de sangre', CURRENT_TIMESTAMP, '2026-06-11 09:00:00', '22.pdf', '22.pdf', 'application/pdf', 'uploads/estudios/22.pdf'),
(2, 2, 2, 'Radiografia de Rodilla', 'Radiografia para evaluar lesion en la rodilla', CURRENT_TIMESTAMP, '2026-06-12 14:30:00', '77.pdf', '77.pdf', 'application/pdf', 'uploads/estudios/77.pdf');
