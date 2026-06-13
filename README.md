# Laboratorio AFA - Sistema de turnos

Trabajo práctico de la materia Trabajo de Campo para la Licenciatura en Gestión de Tecnologías de la Información de la Universidad Nacional de José C. Paz.

El proyecto implementa un sistema de gestión de turnos para el Laboratorio AFA, con roles diferenciados para administradores, médicos y pacientes.

## Integrantes

- Ailén Villar
- Alejo Escurra
- Fabián Sotomayor

## Qué incluye el proyecto

- `labo/`: backend Java 17 con Spring Boot, Spring Web, Spring Data JPA y H2.
- `labo_frontend/`: frontend Vue 3, Vite y PrimeVue.
- `docs/`: documentación complementaria del proyecto.

El sistema usa una base de datos H2 en memoria. Esto significa que no hace falta instalar MySQL, PostgreSQL ni ningún motor externo: al iniciar el backend se crean las tablas y se cargan datos de prueba automáticamente desde `schema.sql` y `data.sql`.

## Requisitos previos

Antes de ejecutar el sistema, instalá:

1. Java 17
   Descargar desde: https://adoptium.net/

2. Node.js LTS
   Descargar desde: https://nodejs.org/

3. Git, opcional pero recomendado
   Descargar desde: https://git-scm.com/

4. PowerShell o una terminal similar.

Para verificar que Java y Node quedaron instalados, abrí PowerShell y ejecutá:

```powershell
java -version
node -v
npm -v
```

Si alguno de esos comandos no responde, cerrá y volvé a abrir PowerShell. Si sigue sin funcionar, revisá la instalación correspondiente.

## Descargar el proyecto

### Opción A: descargar ZIP desde GitHub

1. Entrá al repositorio en GitHub.
2. Presioná el botón verde `Code`.
3. Elegí `Download ZIP`.
4. Descomprimí el archivo en una carpeta simple, por ejemplo:

```text
C:\Users\TuUsuario\Desktop\sistema-turnos-lab-afa
```

### Opción B: clonar con Git

```powershell
cd Desktop
git clone https://github.com/codex-00/sistema-turnos-lab-afa.git
cd sistema-turnos-lab-afa
```

## Cómo levantar el sistema

El sistema necesita dos terminales abiertas al mismo tiempo:

- una para el backend;
- otra para el frontend.

No cierres la terminal del backend mientras uses el sistema.

## Paso 1: iniciar el backend

Abrí una terminal en la carpeta principal del proyecto y ejecutá:

```powershell
cd labo
.\mvnw.cmd spring-boot:run
```

La primera vez puede tardar porque Maven descarga dependencias.

Cuando termine de iniciar, el backend queda disponible en:

```text
http://localhost:8081
```

También podés abrir la consola de la base H2 en:

```text
http://localhost:8081/h2-console
```

Datos para entrar a H2:

- JDBC URL: `jdbc:h2:mem:LABORABD`
- User Name: `sa`
- Password: dejar vacío

## Paso 2: iniciar el frontend

Abrí una segunda terminal en la carpeta principal del proyecto y ejecutá:

```powershell
cd labo_frontend
npm install
npm run dev
```

La primera vez `npm install` puede tardar varios minutos.

Cuando Vite termine de iniciar, mostrará una URL similar a:

```text
http://localhost:5173
```

Abrí esa URL en el navegador.

## Paso 3: iniciar sesión

En la pantalla de login, elegí el rol correspondiente y usá alguno de estos usuarios.

### Administrador

- Email: `admin@clinica.com`
- Contraseña: `admin123`
- Rol: `admin`

### Médicos

- Email: `laura@salud.com`
- Contraseña: `clave1`
- Rol: `medico`

- Email: `carlos@salud.com`
- Contraseña: `clave2`
- Rol: `medico`

### Pacientes

- Email: `ana@gmail.com`
- Contraseña: `clave3`
- Rol: `paciente`

- Email: `lucas@hotmail.com`
- Contraseña: `clave4`
- Rol: `paciente`

## Uso básico

1. Iniciá el backend.
2. Iniciá el frontend.
3. Entrá al navegador en `http://localhost:5173`.
4. Iniciá sesión con un usuario de prueba.
5. Probá las funciones según el rol:
   - administrador: gestión de médicos, pacientes, usuarios y turnos;
   - médico: agenda, disponibilidad, turnos y estudios;
   - paciente: reserva, cancelación y reprogramación de turnos, perfil y estudios.

## Funcionalidades principales

- Login por rol.
- Paneles separados para administrador, médico y paciente.
- Gestión de pacientes, médicos y usuarios/permisos.
- Gestión de disponibilidad médica.
- Reserva, aprobación, cancelación y reprogramación de turnos.
- Agenda médica con horarios disponibles y ocupados.
- Carga, listado, visualización y descarga de estudios PDF.
- Recuperación de contraseña.
- Estados de turnos con colores consistentes.

## Ver correos de restablecimiento de contraseña

El sistema usa Mailtrap para probar los correos de recuperación de contraseña. Mailtrap funciona como una bandeja de entrada de prueba: los mails no llegan a una casilla real del usuario, sino que quedan guardados en Mailtrap para poder revisarlos durante el desarrollo.

Para comprobar que funciona la opción **Restablecer contraseña**:

1. Iniciá el backend y el frontend.
2. Entrá al sistema desde `http://localhost:5173`.
3. En la pantalla de login, usá la opción para restablecer la contraseña.
4. Escribí el correo del usuario al que querés enviarle el enlace de recuperación.
5. Abrí Mailtrap en esta dirección:

```text
https://mailtrap.io/sandboxes/4707727/messages/5538073584
```

6. Iniciá sesión con estas credenciales:

```text
Usuario: clinicaafa2026@gmail.com
Contraseña: Unpaz.affa
```

7. Dentro de Mailtrap, revisá la bandeja del sandbox. Ahí se visualizan los correos que el sistema enviaría para cambiar la contraseña.
8. Abrí el mensaje más reciente y buscá el enlace o botón de recuperación. Ese enlace es el que usaría el usuario para definir una nueva contraseña.

Si no aparece ningún correo nuevo, verificá que el backend esté iniciado, que el frontend esté conectado a `http://localhost:8081` y que el correo ingresado exista en los datos del sistema.

## Cambiar la URL del backend

Por defecto, el frontend intenta conectarse a:

```text
http://localhost:8081
```

Si necesitás usar otra URL, creá un archivo llamado `.env.local` dentro de `labo_frontend/` con este contenido:

```env
VITE_API_URL=http://localhost:8081
```

Después reiniciá el frontend con:

```powershell
npm run dev
```

## Verificar que todo funcione

### Backend

Desde la carpeta `labo/`:

```powershell
.\mvnw.cmd test
```

### Frontend

Desde la carpeta `labo_frontend/`:

```powershell
npm run build
```

Si ambos comandos terminan sin errores, el proyecto compila correctamente.

## Notas importantes

- La base H2 es en memoria: al reiniciar el backend, se vuelven a cargar los datos iniciales.
- Los PDFs de estudios se guardan en disco dentro de `labo/uploads/estudios`.
- Las contraseñas iniciales de `data.sql` están preparadas para facilitar la prueba del sistema.
- Las nuevas contraseñas se guardan con BCrypt.
- El campo `token` del frontend representa el ID del usuario autenticado por compatibilidad histórica. La respuesta de login también incluye `usuarioId`.
