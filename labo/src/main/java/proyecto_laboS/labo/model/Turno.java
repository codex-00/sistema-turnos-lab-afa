package proyecto_laboS.labo.model;

import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;



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

    @Column(name = "establecimiento")
    private String establecimiento;
    
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_de_turno")
    private LocalDateTime fechaDeTurno;
    

    public Turno() {}

    public Long getIdTurno() {return idTurno;}
    public void setIdTurno(Long idTurno) { this.idTurno = idTurno;}

    public LocalDateTime getFechaCreacion() {return fechaCreacion;}
    public void setFechaCreacion(LocalDateTime fechaCreacion) {this.fechaCreacion = fechaCreacion;}

    public LocalDateTime getFechaDeTurno() {return fechaDeTurno;}
    public void setFechaDeTurno(LocalDateTime fechaDeTurno) {this.fechaDeTurno = fechaDeTurno;}

    public Medico getMedico() {return medico;}
    public void setMedico(Medico medico) {this.medico = medico;}

    public Paciente getPaciente() {return paciente;}
    public void setPaciente(Paciente paciente) {this.paciente = paciente;}

    // @Override
    // public String toString() {
    //     return "Turno{" +
    //             "idTurno=" + idTurno +
    //             ", fechaHora=" + fechaCreacion +
    //             ", medico=" + medico +
    //             ", paciente=" + paciente +
    //             '}';
    // }

    
}
