# DER - Sistema de Laboratorio

Este diagrama esta basado en el modelo actual del sistema: entidades JPA del backend y tablas declaradas en `schema.sql`.

```mermaid
erDiagram
    PACIENTES ||--o{ TURNOS : "solicita"
    MEDICOS ||--o{ TURNOS : "atiende"
    TURNOS ||--o{ ESTUDIOS : "genera"
    MEDICOS ||--o{ DISPONIBILIDAD : "define"
    MEDICOS ||--o{ AGENDA : "tiene"
    PACIENTES ||--o{ AGENDA : "reserva"

    PACIENTES {
        int id_paciente PK
        varchar dni
        varchar nombre
        varchar apellido
        varchar email UK
        timestamp fecha_registro
        varchar password
        varchar direccion
        varchar telefono
    }

    MEDICOS {
        int id_medico PK
        varchar nombre
        varchar apellido
        varchar dni
        varchar email
        varchar password
        varchar direccion
        varchar telefono
        timestamp fecha_registro
        varchar especialidad
    }

    TURNOS {
        int id_turno PK
        int id_paciente FK
        int id_medico FK
        varchar establecimiento
        timestamp fecha_creacion
        timestamp fecha_de_turno
    }

    ESTUDIOS {
        int id_estudio PK
        int id_turno FK
        int id_paciente FK
        varchar nombre
        text descripcion
        timestamp fecha_creacion
        timestamp fecha_de_turno
    }

    TESTS {
        int id PK
        varchar nombre
    }

    DISPONIBILIDAD {
        bigint id PK
        bigint medico_id FK
        enum dia
        time hora_inicio
        time hora_fin
        int duracion_turno
    }

    AGENDA {
        bigint id PK
        bigint medico_id FK
        bigint paciente_id FK
        date fecha
        time hora
        boolean disponible
    }
```

## Relaciones

| Relacion | Cardinalidad | Descripcion |
|---|---:|---|
| `PACIENTES` -> `TURNOS` | 1:N | Un paciente puede tener muchos turnos. |
| `MEDICOS` -> `TURNOS` | 1:N | Un medico puede atender muchos turnos. |
| `TURNOS` -> `ESTUDIOS` | 1:N | Un turno puede generar uno o mas estudios. |
| `MEDICOS` -> `DISPONIBILIDAD` | 1:N | Un medico puede definir varios bloques de disponibilidad. |
| `MEDICOS` -> `AGENDA` | 1:N | Un medico puede tener muchas entradas de agenda. |
| `PACIENTES` -> `AGENDA` | 1:N | Un paciente puede reservar varias entradas de agenda. |

## Notas

- `Usuario` no aparece como tabla porque en Java esta definido como `@MappedSuperclass`; sus campos se heredan en `Medico` y `Paciente`.
- `TESTS` queda como entidad independiente porque no tiene claves foraneas.
- `Agenda` y `Disponibilidad` existen como entidades Java. Si se usa `schema.sql` con `spring.jpa.hibernate.ddl-auto=none`, conviene agregar sus `CREATE TABLE` para que queden persistidas en H2.
