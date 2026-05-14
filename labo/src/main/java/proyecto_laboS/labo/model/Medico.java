package proyecto_laboS.labo.model;
import java.time.LocalDateTime;
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
    @Column(name = "especialidad")
    private String especialidad;

    @Transient
    private List<Disponibilidad> disponibilidades;
    

    public Medico(){}

    public Medico(String nombre, String apellido, String email, String especialidad) {
        super();
        this.idMedico = null; // Asumiendo que el ID se genera automáticamente
        this.especialidad = especialidad;
        this.fechaRegistro = LocalDateTime.now(); // Asignar la fecha de registro actual
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
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

    @Override
    public String toString() {
        return "Medico{" +
                "especialidad='" + especialidad + '\'' +
                "} " + super.toString();
    }   
    
}
