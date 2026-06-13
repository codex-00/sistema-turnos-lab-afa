package proyecto_laboS.labo.model;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "medicos")
public class Medico extends Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_medico")
    private Long idMedico;
    @Column(name = "dni", nullable = false, unique = true, length = 20)
    private String dni;
    @Column(name = "especialidad")
    private String especialidad;

    @Column(name = "estado")
    private String estado = "ACTIVO";

    @Transient
    private List<Disponibilidad> disponibilidades;
    

    public Medico(){}

    public Medico(String nombre, String apellido, String email, String especialidad) {
        super();
        this.idMedico = null; // Asumiendo que el ID se genera automáticamente
        this.especialidad = especialidad;
        this.fechaRegistro = LocalDateTime.now(); // Asignar la fecha de registro actual
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public List<String> getEspecialidades() {
        if (especialidad == null || especialidad.isBlank()) {
            return List.of();
        }
        return Arrays.stream(especialidad.split(","))
                .map(String::trim)
                .filter(valor -> !valor.isBlank())
                .distinct()
                .toList();
    }

    public void setEspecialidades(List<String> especialidades) {
        if (especialidades == null) {
            this.especialidad = null;
            return;
        }
        this.especialidad = especialidades.stream()
                .map(especialidad -> especialidad == null ? "" : especialidad.trim())
                .filter(valor -> !valor.isBlank())
                .distinct()
                .reduce((a, b) -> a + ", " + b)
                .orElse(null);
    }

    public Long getIdMedico() {
        return idMedico;
    }

    public void setIdMedico(Long idMedico) {
        this.idMedico = idMedico;
    }

    public List<Disponibilidad> getDisponibilidades() {
        return disponibilidades;
    }

    public void setDisponibilidades(List<Disponibilidad> disponibilidades) {
        this.disponibilidades = disponibilidades;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "Medico{" +
                "dni='" + dni + '\'' +
                ", especialidad='" + especialidad + '\'' +
                "} " + super.toString();
    }   
    
}
