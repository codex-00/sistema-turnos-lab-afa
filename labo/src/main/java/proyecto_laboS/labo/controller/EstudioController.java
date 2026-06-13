package proyecto_laboS.labo.controller;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import proyecto_laboS.labo.model.Estudio;
import proyecto_laboS.labo.controller.dto.ApiDtos;
import proyecto_laboS.labo.controller.dto.ApiDtos.EstudioDto;
import proyecto_laboS.labo.service.EstudioService;
import proyecto_laboS.labo.service.exception.EstudioAccessDeniedException;

@RestController
@RequestMapping("/estudios")
public class EstudioController {

    private static final String HEADER_USER_ID = "X-Usuario-Id";
    private static final String HEADER_USER_ROLE = "X-Usuario-Rol";

    private final EstudioService estudioService;

    public EstudioController(EstudioService estudioService) {
        this.estudioService = estudioService;
    }

    @GetMapping
    public List<EstudioDto> listarEstudiosCompat(
            @RequestParam(required = false) Long paciente,
            @RequestHeader(value = HEADER_USER_ID, required = false) Long usuarioId,
            @RequestHeader(value = HEADER_USER_ROLE, required = false) String rol) {
        if (paciente != null) {
            validarPaciente(usuarioId, rol, paciente);
            return estudioService.listarEstudiosPorPaciente(paciente).stream().map(ApiDtos::toDto).toList();
        }

        throw new EstudioAccessDeniedException("Debe indicar el listado de medico o paciente autorizado");
    }

    @GetMapping("/medico/{medicoId}")
    public List<EstudioDto> listarEstudiosDelMedico(
            @PathVariable Long medicoId,
            @RequestHeader(value = HEADER_USER_ID, required = false) Long usuarioId,
            @RequestHeader(value = HEADER_USER_ROLE, required = false) String rol) {
        validarMedico(usuarioId, rol, medicoId);
        return estudioService.listarEstudiosPorMedico(medicoId).stream().map(ApiDtos::toDto).toList();
    }

    @GetMapping("/paciente/{pacienteId}")
    public List<EstudioDto> listarEstudiosDelPaciente(
            @PathVariable Long pacienteId,
            @RequestHeader(value = HEADER_USER_ID, required = false) Long usuarioId,
            @RequestHeader(value = HEADER_USER_ROLE, required = false) String rol) {
        validarPaciente(usuarioId, rol, pacienteId);
        return estudioService.listarEstudiosPorPaciente(pacienteId).stream().map(ApiDtos::toDto).toList();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public EstudioDto subirEstudio(
            @RequestParam Long pacienteId,
            @RequestParam String nombre,
            @RequestParam(required = false) String descripcion,
            @RequestParam MultipartFile archivo,
            @RequestHeader(value = HEADER_USER_ID, required = false) Long usuarioId,
            @RequestHeader(value = HEADER_USER_ROLE, required = false) String rol) {
        validarRolMedico(usuarioId, rol);
        return ApiDtos.toDto(estudioService.subirEstudio(usuarioId, pacienteId, nombre, descripcion, archivo));
    }

    @GetMapping("/{id}/ver")
    public ResponseEntity<Resource> verEstudio(
            @PathVariable Long id,
            @RequestHeader(value = HEADER_USER_ID, required = false) Long usuarioId,
            @RequestHeader(value = HEADER_USER_ROLE, required = false) String rol) {
        Estudio estudio = estudioService.obtenerEstudioAutorizado(id, usuarioId, rol);
        Path path = estudioService.obtenerPdfAutorizado(id, usuarioId, rol);
        return buildPdfResponse(estudio, path, false);
    }

    @GetMapping("/{id}/descargar")
    public ResponseEntity<Resource> descargarEstudio(
            @PathVariable Long id,
            @RequestHeader(value = HEADER_USER_ID, required = false) Long usuarioId,
            @RequestHeader(value = HEADER_USER_ROLE, required = false) String rol) {
        Estudio estudio = estudioService.obtenerEstudioAutorizado(id, usuarioId, rol);
        Path path = estudioService.obtenerPdfAutorizado(id, usuarioId, rol);
        return buildPdfResponse(estudio, path, true);
    }

    @DeleteMapping("/{id}")
    public void eliminarEstudio(
            @PathVariable Long id,
            @RequestHeader(value = HEADER_USER_ID, required = false) Long usuarioId,
            @RequestHeader(value = HEADER_USER_ROLE, required = false) String rol) {
        estudioService.eliminarEstudioAutorizado(id, usuarioId, rol);
    }

    private ResponseEntity<Resource> buildPdfResponse(Estudio estudio, Path path, boolean download) {
        String fileName = StringUtils.hasText(estudio.getNombreArchivo())
                ? estudio.getNombreArchivo()
                : "estudio-" + estudio.getIdEstudio() + ".pdf";
        ContentDisposition disposition = download
                ? ContentDisposition.attachment().filename(fileName, StandardCharsets.UTF_8).build()
                : ContentDisposition.inline().filename(fileName, StandardCharsets.UTF_8).build();

        FileSystemResource resource = new FileSystemResource(path);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(resource.getFile().length())
                .body(resource);
    }

    private void validarRolMedico(Long usuarioId, String rol) {
        if (usuarioId == null || !"medico".equalsIgnoreCase(rol)) {
            throw new EstudioAccessDeniedException("Solo un medico autenticado puede subir estudios");
        }
    }

    private void validarMedico(Long usuarioId, String rol, Long medicoId) {
        validarRolMedico(usuarioId, rol);
        if (!usuarioId.equals(medicoId)) {
            throw new EstudioAccessDeniedException("No tiene permisos para listar estos estudios");
        }
    }

    private void validarPaciente(Long usuarioId, String rol, Long pacienteId) {
        if (usuarioId == null || !"paciente".equalsIgnoreCase(rol) || !usuarioId.equals(pacienteId)) {
            throw new EstudioAccessDeniedException("No tiene permisos para listar estos estudios");
        }
    }
}
