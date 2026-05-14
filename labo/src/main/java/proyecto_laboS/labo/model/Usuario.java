package proyecto_laboS.labo.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@MappedSuperclass
public abstract class Usuario {

    @Column(name = "nombre")
    protected String nombre;
    @Column(name = "apellido")
    protected String apellido;
    @Column(name = "email")
    protected String email;
    @Column(name = "fecha_registro")
    protected LocalDateTime fechaRegistro;
    @Column(name = "password")
    protected String password;
    @Column(name = "direccion")
    protected String direccion;
    @Column(name = "telefono")
    protected String telefono;

    // Constructor por defecto
    public Usuario() {
         }
    
    // Getters y Setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
}
