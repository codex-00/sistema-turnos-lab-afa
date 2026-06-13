package proyecto_laboS.labo.service;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import proyecto_laboS.labo.model.Estudio;
import proyecto_laboS.labo.model.Medico;
import proyecto_laboS.labo.model.Paciente;
import proyecto_laboS.labo.repository.EstudioRepository;
import proyecto_laboS.labo.repository.MedicoRepository;
import proyecto_laboS.labo.repository.PacienteRepository;
import proyecto_laboS.labo.service.exception.EstudioAccessDeniedException;
import proyecto_laboS.labo.service.exception.EstudioNotFoundException;
import proyecto_laboS.labo.service.exception.EstudioValidationException;

@Service
public class EstudioServiceImpl implements EstudioService {

    private static final Logger log = LoggerFactory.getLogger(EstudioServiceImpl.class);

    private final EstudioRepository estudioRepository;
    private final MedicoRepository medicoRepository;
    private final PacienteRepository pacienteRepository;
    private final EstudioFileStorageService fileStorageService;

    public EstudioServiceImpl(
            EstudioRepository estudioRepository,
            MedicoRepository medicoRepository,
            PacienteRepository pacienteRepository,
            EstudioFileStorageService fileStorageService) {
        this.estudioRepository = estudioRepository;
        this.medicoRepository = medicoRepository;
        this.pacienteRepository = pacienteRepository;
        this.fileStorageService = fileStorageService;
    }

    @Override
    public Estudio crearEstudio(Estudio estudio) {
        return estudioRepository.save(estudio);
    }

    @Override
    public Estudio subirEstudio(Long medicoId, Long pacienteId, String nombre, String descripcion, MultipartFile archivo) {
        if (medicoId == null || pacienteId == null) {
            throw new EstudioValidationException("El medico y el paciente son obligatorios");
        }
        if (!StringUtils.hasText(nombre)) {
            throw new EstudioValidationException("El nombre del estudio es obligatorio");
        }

        Medico medico = medicoRepository.findById(medicoId)
                .orElseThrow(() -> new EstudioValidationException("El medico indicado no existe"));
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new EstudioValidationException("El paciente indicado no existe"));

        StoredEstudioFile storedFile = fileStorageService.store(archivo);

        Estudio estudio = new Estudio();
        estudio.setMedico(medico);
        estudio.setPaciente(paciente);
        estudio.setNombre(nombre.trim());
        estudio.setDescripcion(StringUtils.hasText(descripcion) ? descripcion.trim() : null);
        estudio.setFechaCreacion(LocalDateTime.now());
        estudio.setNombreArchivo(storedFile.originalFileName());
        estudio.setNombreArchivoInterno(storedFile.internalFileName());
        estudio.setTipoArchivo(storedFile.contentType());
        estudio.setTamanoArchivo(storedFile.size());
        estudio.setRutaArchivo(storedFile.path().toString());

        Estudio saved = estudioRepository.save(estudio);
        log.info("Estudio PDF {} subido por medico {} para paciente {}", saved.getIdEstudio(), medicoId, pacienteId);
        return saved;
    }

    @Override
    public Estudio obtenerEstudioPorId(Long id) {
        return estudioRepository.findById(id)
                .orElseThrow(() -> new EstudioNotFoundException("El estudio no existe"));
    }

    @Override
    public void cancelarEstudio(Long id) {
        Estudio estudio = estudioRepository.findById(id)
                .orElseThrow(() -> new EstudioNotFoundException("El estudio no existe"));
        fileStorageService.deleteQuietly(estudio.getRutaArchivo());
        estudioRepository.delete(estudio);
        log.info("Estudio {} eliminado", id);
    }

    @Override
    public List<Estudio> listarEstudios() {
        return estudioRepository.findAll();
    }

    @Override
    public List<Estudio> listarEstudiosPorPaciente(Long idPaciente) {
        return estudioRepository.findByPacienteIdPaciente(idPaciente);
    }

    @Override
    public List<Estudio> listarEstudiosPorMedico(Long idMedico) {
        return estudioRepository.findByMedicoIdMedico(idMedico);
    }

    @Override
    public Estudio obtenerEstudioAutorizado(Long id, Long usuarioId, String rol) {
        Estudio estudio = estudioRepository.findById(id)
                .orElseThrow(() -> new EstudioNotFoundException("El estudio no existe"));

        if (!puedeAcceder(estudio, usuarioId, rol)) {
            throw new EstudioAccessDeniedException("No tiene permisos para acceder a este estudio");
        }

        return estudio;
    }

    @Override
    public Path obtenerPdfAutorizado(Long id, Long usuarioId, String rol) {
        Estudio estudio = obtenerEstudioAutorizado(id, usuarioId, rol);
        Optional<Path> path = fileStorageService.resolveExisting(estudio.getRutaArchivo())
                .or(() -> fileStorageService.resolveExistingByFileName(estudio.getNombreArchivoInterno()))
                .or(() -> fileStorageService.resolveExistingByFileName(estudio.getNombreArchivo()));
        Path pdfPath = path.orElseThrow(() -> new EstudioNotFoundException("El archivo PDF del estudio no existe"));

        if (!StringUtils.hasText(estudio.getRutaArchivo())) {
            estudio.setRutaArchivo(pdfPath.toString());
            if (!StringUtils.hasText(estudio.getNombreArchivoInterno())) {
                estudio.setNombreArchivoInterno(pdfPath.getFileName().toString());
            }
            estudioRepository.save(estudio);
        }

        return pdfPath;
    }

    @Override
    public void eliminarEstudioAutorizado(Long id, Long medicoId, String rol) {
        Estudio estudio = obtenerEstudioAutorizado(id, medicoId, rol);
        if (!"medico".equalsIgnoreCase(rol) || estudio.getMedico() == null
                || !medicoId.equals(estudio.getMedico().getIdMedico())) {
            throw new EstudioAccessDeniedException("Solo el medico dueno puede eliminar el estudio");
        }

        cancelarEstudio(id);
    }

    private boolean puedeAcceder(Estudio estudio, Long usuarioId, String rol) {
        if (usuarioId == null || !StringUtils.hasText(rol)) {
            return false;
        }

        if ("medico".equalsIgnoreCase(rol)) {
            return estudio.getMedico() != null && usuarioId.equals(estudio.getMedico().getIdMedico());
        }

        if ("paciente".equalsIgnoreCase(rol)) {
            return estudio.getPaciente() != null && usuarioId.equals(estudio.getPaciente().getIdPaciente());
        }

        return false;
    }
}
