# Cambios realizados en el sistema de turnos

Este documento resume los cambios realizados para completar y mejorar el flujo solicitado por el profesor: que el paciente pueda pedir un turno, elegir una fecha desde un calendario y seleccionar uno de los horarios disponibles.

## Objetivo del cambio

El objetivo fue dejar funcionando el circuito completo de reserva de turnos:

1. El paciente inicia sesión.
2. Entra a la pantalla "Mis turnos".
3. Hace clic en "Nuevo Turno".
4. Selecciona especialidad.
5. Selecciona medico.
6. Elige una fecha desde un calendario.
7. El sistema muestra los horarios disponibles para ese medico y esa fecha.
8. El paciente selecciona un horario y reserva el turno.
9. El turno queda guardado y aparece en la lista del paciente.

## Cambios en el frontend

### Login demo

Archivo modificado:

- `labo_frontend/src/views/pages/auth/Login.vue`
- `labo_frontend/src/stores/auth.js`

Se mejoro el login de demostracion para que guarde correctamente:

- el email del usuario,
- el rol seleccionado,
- el identificador del usuario,
- los datos basicos del usuario logueado.

Antes el login tenia datos hardcodeados y el paciente no quedaba bien identificado. Ahora se puede ingresar como paciente o como medico usando credenciales demo.

Credenciales disponibles:

- Paciente: `ana@gmail.com` / `clave3`
- Paciente: `lucas@hotmail.com` / `clave4`
- Medico: `laura@clinica.com` / `clave1`
- Medico: `carlos@salud.com` / `clave2`

### Paciente logueado en la reserva

Archivo modificado:

- `labo_frontend/src/views/paciente/Turnos.vue`

Se reemplazo el paciente fijo `pacienteId = 1` por el paciente que esta logueado en el store de autenticacion.

Esto permite que el turno se reserve para el paciente correcto y no siempre para el mismo paciente.

Tambien se agrego una restriccion al calendario para evitar seleccionar fechas anteriores al dia actual.

### Calendario y horarios disponibles

Archivo principal:

- `labo_frontend/src/views/paciente/Turnos.vue`

La pantalla del paciente ya cuenta con el flujo de solicitud:

- seleccion de especialidad,
- carga de medicos por especialidad,
- seleccion de medico,
- seleccion de fecha con `DatePicker`,
- consulta de agenda disponible,
- seleccion del horario,
- reserva del turno.

Cuando el paciente elige una fecha, el frontend consulta al backend usando el endpoint de agenda y muestra los horarios libres como botones.

### Menu y rutas

Archivos modificados:

- `labo_frontend/src/router/index.js`
- `labo_frontend/src/layout/AppMenu.vue`

Se agrego la ruta faltante:

- `/admin/turnos`

Tambien se agrego al menu del medico el acceso a:

- `Disponibilidad`

Esto permite que desde la interfaz se pueda llegar a la pantalla donde se configura la disponibilidad medica.

Ademas se corrigio una ruta mal escrita de estudios, que apuntaba a `/admin/estdios`.

## Cambios en el backend

### Disponibilidad medica

Archivo modificado:

- `labo/src/main/java/proyecto_laboS/labo/model/Disponibilidad.java`

Se agrego la anotacion:

```java
@Enumerated(EnumType.STRING)
```

Esto hace que el dia de disponibilidad se guarde como texto, por ejemplo:

- `MONDAY`
- `TUESDAY`
- `WEDNESDAY`

Este cambio es importante porque la tabla `disponibilidad` guarda el campo `dia` como `VARCHAR`. Asi queda alineado el modelo Java con la base de datos.

### Datos iniciales de disponibilidad

Archivo modificado:

- `labo/src/main/resources/data.sql`

Se agregaron datos iniciales de disponibilidad para los dos medicos de prueba.

Disponibilidad agregada:

- Medico 1: lunes a viernes de 08:00 a 12:00, turnos cada 30 minutos.
- Medico 2: lunes a viernes de 14:00 a 18:00, turnos cada 30 minutos.

Esto permite que al iniciar el sistema ya existan horarios disponibles para probar la reserva sin tener que cargarlos manualmente.

## Funcionamiento tecnico del flujo

El sistema usa tres conceptos principales:

### Disponibilidad

Define en que dias y horarios atiende un medico.

Ejemplo:

```text
Medico 1 - Viernes - 08:00 a 12:00 - turnos de 30 minutos
```

### Agenda

Cuando el paciente selecciona médico y fecha, el backend genera automáticamente los horarios posibles para ese día según la disponibilidad del médico.

Por ejemplo, si el medico atiende de 08:00 a 12:00 con turnos de 30 minutos, se generan horarios como:

- 08:00
- 08:30
- 09:00
- 09:30
- 10:00
- 10:30
- 11:00
- 11:30

### Turno

Cuando el paciente confirma un horario, el sistema:

1. marca ese horario de agenda como no disponible,
2. asocia el horario al paciente,
3. crea un registro en la tabla `turnos`,
4. muestra el nuevo turno en la lista del paciente.

## Endpoints usados

### Consultar turnos del paciente

```http
GET http://localhost:8081/turnos?paciente=1
```

Devuelve los turnos asociados a un paciente.

### Consultar horarios disponibles

```http
GET http://localhost:8081/agenda?medicoId=1&fecha=2026-05-15
```

Devuelve los horarios disponibles de un medico para una fecha especifica.

### Reservar turno

```http
POST http://localhost:8081/agenda/reservar?agendaId=1&pacienteId=1
```

Reserva un horario de agenda y crea el turno correspondiente.

## Verificaciones realizadas

Se comprobaron los cambios con:

```powershell
.\mvnw.cmd clean test
```

Resultado: backend compila y los tests pasan correctamente.

También se verificó el frontend con:

```powershell
npm run build
```

Resultado: el frontend compila correctamente.

Además se probó manualmente el endpoint de agenda. Para el médico 1 y la fecha `2026-05-15`, el sistema devolvió 8 horarios disponibles:

```text
08:00
08:30
09:00
09:30
10:00
10:30
11:00
11:30
```

## Como probarlo

1. Iniciar el backend:

```powershell
cd labo
.\mvnw.cmd spring-boot:run
```

2. Iniciar el frontend:

```powershell
cd labo_frontend
npm run dev
```

3. Abrir el navegador en:

```text
http://localhost:5173
```

4. Iniciar sesión como paciente:

```text
Email: ana@gmail.com
Contraseña: clave3
Rol: Paciente
```

5. Ir a "Mis turnos".

6. Hacer clic en "Nuevo Turno".

7. Seleccionar especialidad, medico, fecha y horario.

8. Confirmar con "Reservar".

## Resumen para explicar al profesor

Se completó el flujo principal de turnos del paciente. Ahora el paciente puede iniciar sesión, pedir un nuevo turno, elegir una fecha desde un calendario y seleccionar un horario disponible generado automáticamente según la disponibilidad del médico.

También se conectó la reserva con el paciente logueado, se agregaron datos iniciales para poder probar el sistema y se corrigieron rutas del menú para acceder correctamente a turnos y disponibilidad.
