/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package veterinara;

/**
 *
 * @author agent
 */
public class Usuario {
    private String nombre;
    private String email;
    private RolUsuario rol;
    
    public enum RolUsuario {VETERINARIO, AUXILIAR, RECEPCIONISTA, ADMIN}

    public Usuario(String nombre, String email, RolUsuario rol) {
        this.nombre = nombre;
        this.email = email;
        this.rol = rol;
    }

    // Getters y setters
    public RolUsuario getRol() {
        return rol;
    }

    public void setRol(RolUsuario rol) {
        this.rol = rol;
    }

    @Override
    public String toString() {
        return nombre + " (" + rol + ")";
    }
}
