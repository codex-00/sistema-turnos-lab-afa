# Proyecto de Laboratorio AFA

Proyecto de la materia "Trabajo de Campo" de la carrera "Licenciatura en Gestión de Tecnologías de la Información" de la Universidad Nacional de José C. Paz.

El proyecto es un sistema de gestión de turnos de un laboratorio llamado AFA.

Integrantes:

- Ailén Villar
- Alejo Escurra
- Fabián Sotomayor

Este repositorio contiene dos partes principales:

- `labo/`: backend en Spring Boot (Java 17)
- `labo_frontend/`: frontend en Vue 3 + Vite

## Requisitos

- Java 17
- Maven (opcional si usas el wrapper `mvnw.cmd`)
- Node.js y npm

## Backend

Ruta: `labo/`

### Ejecutar

Abre una terminal en `labo/` y ejecuta:

```powershell
.\mvnw.cmd spring-boot:run
```

Si tienes Maven instalado globalmente, también funciona:

```powershell
mvn spring-boot:run
```

### Descripción

El backend usa Spring Boot con las dependencias:

- `spring-boot-starter-web`
- `spring-boot-starter-data-jpa`
- `h2` (base de datos en memoria)
- `spring-boot-devtools`

## Frontend

Ruta: `labo_frontend/`

### Instalación

Abre una terminal en `labo_frontend/` y ejecuta:

```powershell
npm install
```

### Ejecutar

```powershell
npm run dev
```

### Descripción

El frontend usa Vue 3, Vite y PrimeVue. Los scripts disponibles son:

- `npm run dev`: inicia el servidor de desarrollo
- `npm run build`: crea una versión de producción
- `npm run preview`: prueba la versión de producción localmente
- `npm run lint`: corrige y valida el código con ESLint

## Flujo de trabajo típico

1. Inicia el backend en `labo/`
2. Inicia el frontend en `labo_frontend/`
3. Abre el navegador en la URL que muestre Vite (por defecto `http://localhost:5173`)

## Notas

- Si el backend no arranca, revisa la versión de Java y que no haya otra aplicación usando el mismo puerto.
- El frontend requiere `npm install` solo la primera vez o cuando cambian las dependencias.
