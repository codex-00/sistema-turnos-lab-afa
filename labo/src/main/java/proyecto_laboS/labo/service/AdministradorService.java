package proyecto_laboS.labo.service;

import java.util.List;
import java.util.Optional;

import proyecto_laboS.labo.model.Administrador;

public interface AdministradorService {
    List<Administrador> listarAdministradores();
    Optional<Administrador> obtenerAdministradorPorId(Long id);
    Administrador guardarAdministrador(Administrador administrador);
    Administrador actualizarAdministrador(Long id, Administrador administrador);
    void eliminarAdministrador(Long id);
    boolean esAdministradorPrincipal(Administrador administrador);
}
