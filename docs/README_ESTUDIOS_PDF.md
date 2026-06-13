# Gestion de estudios medicos en PDF

## Objetivo

El módulo existente de `Estudio` permite que un médico suba estudios médicos en PDF para un paciente asignado. H2 guarda solo metadata; el archivo PDF se almacena físicamente en disco local para evitar BLOBs y respetar el límite práctico de 100 MB.

## Configuracion

Propiedades principales en `labo/src/main/resources/application.properties`:

```properties
labo.estudios.storage-directory=uploads/estudios
labo.estudios.max-file-size=100MB
labo.estudios.allowed-mime-types=application/pdf
spring.servlet.multipart.max-file-size=100MB
spring.servlet.multipart.max-request-size=100MB
```

El directorio se crea automaticamente al iniciar la aplicacion si no existe.

## Endpoints

Todos los endpoints protegidos usan los headers ya alineados con el login actual del frontend:

```http
X-Usuario-Id: 1
X-Usuario-Rol: medico
```

### Subir estudio

```http
POST /estudios
Content-Type: multipart/form-data
X-Usuario-Id: {idMedico}
X-Usuario-Rol: medico
```

Campos:

- `pacienteId`
- `nombre`
- `descripcion`
- `archivo` con MIME `application/pdf` y extension `.pdf`

### Listar estudios del medico

```http
GET /estudios/medico/{medicoId}
X-Usuario-Id: {idMedico}
X-Usuario-Rol: medico
```

### Listar estudios del paciente

```http
GET /estudios/paciente/{pacienteId}
X-Usuario-Id: {idPaciente}
X-Usuario-Rol: paciente
```

### Visualizar PDF inline

```http
GET /estudios/{id}/ver
X-Usuario-Id: {idMedico|idPaciente}
X-Usuario-Rol: medico|paciente
```

Responde `application/pdf` con `Content-Disposition: inline`.

### Descargar PDF

```http
GET /estudios/{id}/descargar
X-Usuario-Id: {idMedico|idPaciente}
X-Usuario-Rol: medico|paciente
```

Responde `application/pdf` con `Content-Disposition: attachment`.

## Seguridad y permisos

Solo pueden acceder al estudio:

- El medico que lo subio.
- El paciente asignado.

Respuestas esperadas:

- `403` si el usuario no tiene permiso.
- `404` si el estudio o el archivo fisico no existe.
- `400` si el archivo no cumple validaciones.

## Almacenamiento

La entidad `Estudio` conserva:

- `nombreArchivo`
- `nombreArchivoInterno`
- `rutaArchivo`
- `tipoArchivo`
- `tamanoArchivo`
- relacion con `Medico`
- relacion con `Paciente`

No se guarda el PDF como BLOB. El nombre interno se genera con UUID y extension `.pdf`; el nombre original se sanitiza para evitar path traversal.

La ruta fisica y el nombre interno no se exponen en las respuestas JSON.

## Decisiones tecnicas

- Se reutilizo la entidad, repositorio, servicio y controlador de `Estudio`.
- Se agrego `EstudioFileStorageService` solo como componente de infraestructura del mismo modulo.
- Se valida MIME type, extension y tamano antes de persistir.
- Se eliminan archivos fisicos al borrar el estudio.
- El frontend envia los headers desde el estado de autenticacion existente.
