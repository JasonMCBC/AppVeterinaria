package veterinara;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author agent
 */
public class Cliente {
    private String nombre, dni, direccion, codP, prov, ciudad, telefono, email;
    private List<Mascota> mascota;
    private long id;

    public Cliente() {
        this.mascota = new ArrayList<>();
    }

    public Cliente(String nombre, String dni, String direccion, String codP, String prov, String ciudad, String telefono, String email) {
        this.nombre = nombre;
        this.dni = dni;
        this.direccion = direccion;
        this.codP = codP;
        this.prov = prov;
        this.ciudad = ciudad;
        this.telefono = telefono;
        this.email = email;
    }

    @Override
    public String toString() {
        return nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCodP() {
        return codP;
    }

    public void setCodP(String codP) {
        this.codP = codP;
    }

    public String getProv() {
        return prov;
    }

    public void setProv(String prov) {
        this.prov = prov;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
    
    public void agregarMascota(Mascota mascota) {
        if (mascota != null) {
            this.mascota.add(mascota);
        }
    }

    public void setMascota(List<Mascota> mascota) {
        this.mascota = mascota;
    }
    
    public List<Mascota> getMascota() {
        return mascota;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    
}