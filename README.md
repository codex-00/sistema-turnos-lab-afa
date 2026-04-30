# Backend `labo`

## Datos de presentacion

- Materia: `Trabajo de Campo`
- Integrantes: `Alejo Escurra`, `Ailen Villar`, `Fabian Sotomayor`
- Backend: `Spring Boot + PostgreSQL`

Este modulo contiene el backend del proyecto. A partir de la correccion docente, la persistencia principal fue adaptada para trabajar con PostgreSQL en lugar de H2, y se agrego soporte para consultar turnos disponibles desde la API.

## Tecnologias usadas

- Java 17
- Spring Boot 3.4.6
- Spring Web
- Spring Data JPA
- PostgreSQL
- Maven
- H2 solo para tests automatizados

## Estructura del backend

- `docs/`: documentacion tecnica del backend
- `docs/diagrams/DER.md`: diagrama entidad relacion
- `infra/docker/`: infraestructura local para contenedores
- `scripts/`: scripts de arranque y soporte
- `src/main/java/proyecto_laboS/labo/controller`: endpoints REST
- `src/main/java/proyecto_laboS/labo/dto`: objetos de transferencia para respuestas
- `src/main/java/proyecto_laboS/labo/service`: logica de negocio
- `src/main/java/proyecto_laboS/labo/repository`: acceso a datos
- `src/main/java/proyecto_laboS/labo/model`: entidades del sistema
- `src/main/resources/db/migration`: scripts SQL versionados

La estructura detallada tambien se documenta en:

- `docs/PROJECT_STRUCTURE.md`

## Base de datos principal

La aplicacion ahora usa PostgreSQL como base de datos principal.

Configuracion en `src/main/resources/application.properties`:

- `DB_URL` por defecto: `jdbc:postgresql://localhost:5432/labo_db`
- `DB_USERNAME` por defecto: `postgres`
- `DB_PASSWORD` por defecto: `postgres`
- `spring.jpa.hibernate.ddl-auto=none`
- `spring.sql.init.mode=always`

Esto permite que el esquema y los datos iniciales se carguen desde:

- `src/main/resources/db/migration/V1__schema.sql`
- `src/main/resources/db/migration/V2__data.sql`

## Levantar PostgreSQL con Docker

Se agrego `infra/docker/docker-compose.yml` para levantar una instancia local de PostgreSQL:

```powershell
docker compose -f .\infra\docker\docker-compose.yml up -d
```

La base queda disponible en:

- Host: `localhost`
- Puerto: `5432`
- Base: `labo_db`
- Usuario: `postgres`
- Password: `postgres`

## Variables de entorno

Se agrego el archivo `.env.example` como referencia para la configuracion local:

```env
DB_URL=jdbc:postgresql://localhost:5432/labo_db
DB_USERNAME=postgres
DB_PASSWORD=postgres
```

En PowerShell, antes de ejecutar el backend, se puede configurar asi:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/labo_db"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="postgres"
```

Si PostgreSQL corre en WSL y el puerto `5432` ya esta ocupado en Windows, se puede usar por ejemplo:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5433/labo_db"
```

Para evitar cargar estas variables manualmente en cada ejecucion, se puede usar el script:

```powershell
.\scripts\run-backend-wsl-postgres.ps1
```

## Puesta en marcha en una maquina con PostgreSQL

Si Docker no esta disponible, el backend tambien puede correr con una instalacion local de PostgreSQL:

1. Crear una base llamada `labo_db`.
2. Verificar que exista un usuario con acceso, por ejemplo `postgres`.
3. Configurar `DB_URL`, `DB_USERNAME` y `DB_PASSWORD`.
4. Ejecutar `.\mvnw.cmd spring-boot:run`.

Al iniciar, la aplicacion utiliza los scripts SQL versionados ubicados en `src/main/resources/db/migration`.

## Ejecutar el backend

Desde la carpeta `labo`:

```powershell
.\mvnw.cmd spring-boot:run
```

La API arranca en:

`http://localhost:8081`

## Modelo de datos

### Paciente

- `idPaciente`
- `dni`
- `nombre`
- `apellido`
- `email`
- `fechaRegistro`
- `password`
- `direccion`
- `telefono`

### Medico

- `idMedico`
- `nombre`
- `apellido`
- `dni`
- `email`
- `password`
- `direccion`
- `telefono`
- `fechaRegistro`
- `especialidad`

### Turno

- `idTurno`
- `paciente`
- `medico`
- `establecimiento`
- `fechaCreacion`
- `fechaDeTurno`
- `disponible`

### Estudio

- `idEstudio`
- `turno`
- `paciente`
- `nombre`
- `descripcion`
- `fechaCreacion`
- `fechaDeTurno`

## DER

El Diagrama Entidad Relacion del backend se encuentra en:

- `docs/diagrams/DER.md`

Ese archivo representa las entidades principales y las relaciones entre pacientes, medicos, turnos y estudios.

## Endpoints disponibles

### Inicio

#### `GET /`

Devuelve un mensaje simple de bienvenida.

### Medicos

Base path: `/medicos`

#### `GET /medicos`

Lista todos los medicos.

#### `POST /medicos`

Crea un medico.

#### `PUT /medicos/{id}`

Actualiza un medico existente.

#### `GET /medicos/especialidades?especialidad={valor}`

Filtra medicos por especialidad.

Ejemplo:

```text
GET /medicos/especialidades?especialidad=Traumatologia
```

### Pacientes

Base path: `/pacientes`

#### `GET /pacientes`

Lista todos los pacientes.

#### `POST /pacientes`

Crea un paciente.

### Turnos

Base path: `/turnos`

#### `GET /turnos`

Lista todos los turnos.

#### `GET /turnos?medico={id}`

Lista turnos de un medico.

#### `GET /turnos?paciente={id}`

Lista turnos de un paciente.

#### `GET /turnos/disponibles`

Lista todos los turnos disponibles ordenados por fecha.

#### `GET /turnos/disponibles?medico={id}`

Lista los turnos disponibles de un medico.

Este endpoint responde al requerimiento de visualizar turnos disponibles.

#### `POST /turnos`

Crea un turno.

Regla aplicada en backend:

- Si el turno se crea con paciente, se interpreta como turno reservado y `disponible` queda en `false`.
- Si el turno se crea sin paciente, se interpreta como turno disponible y `disponible` queda en `true`.

Ejemplo de turno reservado:

```json
{
  "paciente": {
    "idPaciente": 1
  },
  "medico": {
    "idMedico": 2
  },
  "establecimiento": "Hospital Central",
  "fechaDeTurno": "2026-04-22T14:30:00"
}
```

Ejemplo de turno disponible:

```json
{
  "medico": {
    "idMedico": 1
  },
  "establecimiento": "Clinica Sur",
  "fechaDeTurno": "2026-04-23T10:00:00"
}
```

### Estudios

Base path: `/estudios`

#### `GET /estudios`

Lista todos los estudios.

### Tests

Base path: `/tests`

#### `GET /tests`

Devuelve un mensaje simple.

#### `POST /tests`

Guarda un registro de prueba.

## Datos iniciales

Los scripts cargan datos de ejemplo para:

- medicos
- pacientes
- turnos reservados
- turnos disponibles
- estudios
- tests

## Pruebas

Para no depender de PostgreSQL durante los tests automatizados, se dejo H2 solo en el entorno de prueba:

- `src/test/resources/application.properties`

Esto sigue la recomendacion habitual: H2 solo para testing, no como base principal del backend.

Ejecutar tests:

```powershell
.\mvnw.cmd test
```

