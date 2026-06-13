# Correcciones realizadas en el sistema

Este documento resume los problemas detectados y las soluciones aplicadas hasta ahora.

## 1. Horarios de turnos duplicados

Problema:

- Al pedir un turno, la pantalla podia mostrar horarios repetidos, por ejemplo `09:00 09:00 09:30 09:30`.

Causa:

- El endpoint `/agenda` generaba horarios automáticamente en cada consulta.
- No había una restricción real en la base de datos que impidiera guardar dos filas con el mismo médico, fecha y hora.

Cambios:

- Se agregó una clave única en `agenda` para `medico_id`, `fecha` y `hora`.
- Se agregó la misma restricción en la entidad `Agenda`.
- Se ordenó la consulta de horarios disponibles por hora.
- Se ajustó la generación para tolerar solicitudes simultáneas sin crear duplicados.

Archivos modificados:

- `labo/src/main/resources/schema.sql`
- `labo/src/main/java/proyecto_laboS/labo/model/Agenda.java`
- `labo/src/main/java/proyecto_laboS/labo/repository/AgendaRepository.java`
- `labo/src/main/java/proyecto_laboS/labo/service/AgendaService.java`

## 2. Login sin mensaje claro de error

Problema:

- Cuando el usuario ingresaba mal email, contraseña o rol, no siempre veía un mensaje claro.

Causa:

- El login se resolvia solo en el frontend con usuarios demo.
- La pantalla de login usaba toast, pero no tenia el componente `<Toast />` montado en esa vista.
- El frontend no distinguia credenciales invalidas de errores del servidor.

Cambios:

- Se agrego el endpoint `POST /auth/login`.
- El backend devuelve `401 Unauthorized` para credenciales invalidas.
- El frontend muestra el mensaje `Usuario o contraseña incorrectos`.
- Se agrego un mensaje visible dentro del formulario, ademas del toast.
- Se diferencia entre credenciales invalidas y error interno o backend no disponible.

Archivos modificados:

- `labo/src/main/java/proyecto_laboS/labo/controller/AuthController.java`
- `labo/src/main/java/proyecto_laboS/labo/repository/PacienteRepository.java`
- `labo_frontend/src/stores/auth.js`
- `labo_frontend/src/views/pages/auth/Login.vue`

## 3. Codificacion UTF-8 para acentos y caracteres especiales

Problema:

- Algunos textos con acentos podian verse rotos, por ejemplo el apellido `Suárez` aparecia con caracteres incorrectos.

Causa:

- Los datos iniciales estaban escritos correctamente en UTF-8, pero faltaba configurar explicitamente UTF-8 en todo el recorrido.
- Spring podia leer scripts SQL, requests o responses usando configuraciones por defecto.
- Los servicios del frontend no declaraban `charset=UTF-8` en los headers.
- No habia reglas del proyecto para ayudar a que los archivos nuevos se guarden como UTF-8.

Cambios:

- Se configuro Maven para compilar recursos y codigo con UTF-8.
- Se configuro Spring para leer scripts SQL con UTF-8.
- Se forzo UTF-8 en requests y responses HTTP.
- Se configuro el conversor JSON/String del backend para responder con UTF-8.
- Se agregaron headers UTF-8 a los servicios del frontend.
- Se ajusto la descarga de estudios para enviar nombres de archivo con UTF-8.
- Se agregaron reglas `.editorconfig` y `.gitattributes` para mantener los archivos del proyecto en UTF-8.
- Se agregaron pruebas de integracion para verificar acentos en APIs.

Archivos modificados:

- `labo/pom.xml`
- `labo/src/main/resources/application.properties`
- `labo/src/main/java/proyecto_laboS/labo/config/Utf8Config.java`
- `labo/src/main/java/proyecto_laboS/labo/controller/EstudioController.java`
- `labo_frontend/src/service/AgendaService.js`
- `labo_frontend/src/service/DisponibilidadService.js`
- `labo_frontend/src/service/EstudioService.js`
- `labo_frontend/src/service/MedicoService.js`
- `labo_frontend/src/service/PacienteService.js`
- `labo_frontend/src/service/TurnoService.js`
- `labo_frontend/src/stores/auth.js`
- `.editorconfig`
- `.gitattributes`
- `labo/src/test/java/proyecto_laboS/labo/EncodingIntegrationTests.java`

## Verificaciones agregadas

- `GET /medicos` debe devolver `Suárez` y `Gómez`.
- `GET /pacientes` debe devolver `Martínez` y `Fernández`.
- `GET /estudios` debe devolver `Radiografía` y `Análisis`.

## Notas

- La base H2 usada por el proyecto guarda texto Unicode, por lo que no necesita collation manual como MySQL.
- Si en el futuro se migra a MySQL o MariaDB, la base deberia crearse con `utf8mb4` y una collation compatible, por ejemplo `utf8mb4_unicode_ci`.
- No se encontro un flujo de exportacion PDF propio en el codigo actual. Las descargas de archivos de estudios conservan los bytes del archivo subido y ahora envian nombres de archivo con UTF-8.

## 4. Cancelacion y reprogramacion de turnos

Problema:

- Al cancelar un turno, el estado del turno cambiaba, pero el horario de `agenda` no siempre volvia a quedar disponible.
- Al reprogramar un turno, el horario anterior podia liberarse, pero el turno seguia figurando como `PENDIENTE`.
- La edicion directa de fecha/hora desde gestion de turnos podia cambiar el turno sin sincronizar la agenda.

Causa:

- `TurnoServiceImpl.cambiarEstado` solo actualizaba el campo `estado`.
- La agenda y el turno no estaban sincronizados desde un unico punto del backend.
- `AgendaService.reprogramarTurno` marcaba el turno como `PENDIENTE` despues de moverlo.

Cambios:

- Si un turno pasa a `CANCELADO`, se libera automaticamente el horario de agenda.
- Si un turno pasa a `RECHAZADO`, tambien se libera el horario para que otro paciente pueda tomarlo.
- Si se reprograma un turno, el horario anterior queda disponible y el nuevo queda ocupado.
- Los turnos reprogramados ahora quedan con estado `REPROGRAMADO`.
- La reserva y la reprogramacion bloquean la fila de agenda con lock pesimista para evitar que dos usuarios tomen el mismo horario al mismo tiempo.
- La edicion directa de un turno sincroniza la agenda anterior y la nueva.
- El frontend reconoce y muestra el estado `REPROGRAMADO` en paciente, medico y administracion.
- Se agregaron pruebas de integracion para validar cancelacion y reprogramacion desde los endpoints.

Archivos modificados:

- `labo/src/main/java/proyecto_laboS/labo/repository/AgendaRepository.java`
- `labo/src/main/java/proyecto_laboS/labo/service/AgendaService.java`
- `labo/src/main/java/proyecto_laboS/labo/service/TurnoServiceImpl.java`
- `labo_frontend/src/views/paciente/Turnos.vue`
- `labo_frontend/src/views/medico/Turnos.vue`
- `labo_frontend/src/views/admin/CrudTurnos.vue`
- `labo/src/test/java/proyecto_laboS/labo/TurnoAgendaConsistencyTests.java`

Verificaciones agregadas:

- Cancelar turno libera el horario y el endpoint `/agenda` vuelve a mostrarlo.
- Reprogramar turno libera el horario anterior, ocupa el nuevo y devuelve estado `REPROGRAMADO`.
