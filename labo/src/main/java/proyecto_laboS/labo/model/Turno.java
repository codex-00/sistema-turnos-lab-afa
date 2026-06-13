package proyecto_laboS.labo.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;



@Entity
@Table(name = "turnos")
public class Turno {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_turno")
    private Long idTurno;

    @ManyToOne
    @JoinColumn(name = "id_paciente")
    private Paciente paciente;

    @ManyToOne
    @JoinColumn(name = "id_medico")
    private Medico medico;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_de_turno")
    private LocalDateTime fechaDeTurno;

    @Column(name = "estado")
    private String estado = "PENDIENTE";
    

    public Turno() {}

    public Long getIdTurno() {return idTurno;}
    public void setIdTurno(Long idTurno) { this.idTurno = idTurno;}

    public LocalDateTime getFechaCreacion() {return fechaCreacion;}
    public void setFechaCreacion(LocalDateTime fechaCreacion) {this.fechaCreacion = fechaCreacion;}

    public LocalDateTime getFechaDeTurno() {return fechaDeTurno;}
    public void setFechaDeTurno(LocalDateTime fechaDeTurno) {this.fechaDeTurno = fechaDeTurno;}

    public String getEstado() {return estado;}
    public void setEstado(String estado) {this.estado = estado;}

    public Medico getMedico() {return medico;}
    public void setMedico(Medico medico) {this.medico = medico;}

    public Paciente getPaciente() {return paciente;}
    public void setPaciente(Paciente paciente) {this.paciente = paciente;}


    
}
