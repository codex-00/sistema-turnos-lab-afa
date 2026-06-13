package proyecto_laboS.labo.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

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

    @ManyToOne
    @JoinColumn(name = "id_medico")
    private Medico medico;
    
    @Column(name = "nombre")
    private String nombre;  
    
    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_de_turno")
    private LocalDateTime fechaDeTurno;

    @Column(name = "nombre_archivo")
    private String nombreArchivo;

    @Column(name = "tipo_archivo")
    private String tipoArchivo;

    @Column(name = "ruta_archivo", length = 1024)
    @JsonIgnore
    private String rutaArchivo;

    @Column(name = "nombre_archivo_interno")
    @JsonIgnore
    private String nombreArchivoInterno;

    @Column(name = "tamano_archivo")
    private Long tamanoArchivo;
    

    public Estudio() {}

    public Long getIdEstudio() {return idEstudio;}
    public void setIdEstudio(Long idEstudio) {this.idEstudio = idEstudio;}
    public Paciente getPaciente() {return paciente;}
    public void setPaciente(Paciente paciente) {this.paciente = paciente;}
    public Medico getMedico() {return medico;}
    public void setMedico(Medico medico) {this.medico = medico;}
    public String getNombre() {return nombre;}
    public void setNombre(String nombre) {this.nombre = nombre;}
    public String getDescripcion() {return descripcion;}
    public void setDescripcion(String descripcion) {this.descripcion = descripcion;}
     public LocalDateTime getFechaCreacion() {return fechaCreacion;}
    public void setFechaCreacion(LocalDateTime fechaCreacion) {this.fechaCreacion = fechaCreacion;}
    public LocalDateTime getFechaDeTurno() {return fechaDeTurno;}
    public void setFechaDeTurno(LocalDateTime fechaDeTurno) {this.fechaDeTurno = fechaDeTurno;}
    public String getNombreArchivo() {return nombreArchivo;}
    public void setNombreArchivo(String nombreArchivo) {this.nombreArchivo = nombreArchivo;}
    public String getTipoArchivo() {return tipoArchivo;}
    public void setTipoArchivo(String tipoArchivo) {this.tipoArchivo = tipoArchivo;}
    public String getRutaArchivo() {return rutaArchivo;}
    public void setRutaArchivo(String rutaArchivo) {this.rutaArchivo = rutaArchivo;}
    public String getNombreArchivoInterno() {return nombreArchivoInterno;}
    public void setNombreArchivoInterno(String nombreArchivoInterno) {this.nombreArchivoInterno = nombreArchivoInterno;}
    public Long getTamanoArchivo() {return tamanoArchivo;}
    public void setTamanoArchivo(Long tamanoArchivo) {this.tamanoArchivo = tamanoArchivo;}

}
