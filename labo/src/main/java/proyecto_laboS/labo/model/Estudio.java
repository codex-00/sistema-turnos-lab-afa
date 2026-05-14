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
@Table(name = "estudios")
public class Estudio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estudio")
    private Long idEstudio;

    @ManyToOne
    @JoinColumn(name = "id_paciente")
    private Paciente paciente;
    
    @Column(name = "nombre")
    private String nombre;  
    
    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_de_turno")
    private LocalDateTime fechaDeTurno;
    

    public Estudio() {}

    public Long getIdEstudio() {return idEstudio;}
    public void setIdEstudio(Long idEstudio) {this.idEstudio = idEstudio;}
    public Paciente getPaciente() {return paciente;}
    public void setPaciente(Long idPaciente) {this.paciente = paciente;}
    public String getNombre() {return nombre;}
    public void setNombre(String nombre) {this.nombre = nombre;}
    public String getDescripcion() {return descripcion;}
    public void setDescripcion(String descripcion) {this.descripcion = descripcion;}
     public LocalDateTime getFechaCreacion() {return fechaCreacion;}
    public void setFechaCreacion(LocalDateTime fechaCreacion) {this.fechaCreacion = fechaCreacion;}
    public LocalDateTime getFechaDeTurno() {return fechaDeTurno;}
    public void setFechaDeTurno(LocalDateTime fechaDeTurno) {this.fechaDeTurno = fechaDeTurno;}

    //public Estudio(Long idEstudio, Paciente paciente, String nombre, String descripcion) {
    //    this.idEstudio = idEstudio;
    //    this.paciente = paciente;
    //    this.nombre = nombre;
    //    this.descripcion = descripcion;
    //}
    
    
}
