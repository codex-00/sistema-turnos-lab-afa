package proyecto_laboS.labo.service;

import java.util.List;
import java.nio.file.Path;

import org.springframework.web.multipart.MultipartFile;

import proyecto_laboS.labo.model.Estudio;

public interface EstudioService {

    List<Estudio> listarEstudios();
    Estudio crearEstudio(Estudio estudio);
    Estudio subirEstudio(Long medicoId, Long pacienteId, String nombre, String descripcion, MultipartFile archivo);
    Estudio obtenerEstudioPorId(Long id);
    void cancelarEstudio(Long id);
    List<Estudio> listarEstudiosPorPaciente(Long idPaciente);
    List<Estudio> listarEstudiosPorMedico(Long idMedico);
    Estudio obtenerEstudioAutorizado(Long id, Long usuarioId, String rol);
    Path obtenerPdfAutorizado(Long id, Long usuarioId, String rol);
    void eliminarEstudioAutorizado(Long id, Long medicoId, String rol);
    
}
