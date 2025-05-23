/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package veterinara;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author agent
 */
public class Contacto {
    private String telefonos, emails, nombre, iden;

    public Contacto() {
    }

    public Contacto(String telefonos, String emails, String nombre, String iden) {
        this.telefonos = telefonos;
        this.emails = emails;
        this.nombre = nombre;
        this.iden = iden;
    }

    //Getter y Setter

    public String getTelefonos() {
        return telefonos;
    }

    public void setTelefonos(String telefonos) {
        this.telefonos = telefonos;
    }

    public String getEmails() {
        return emails;
    }

    public void setEmails(String emails) {
        this.emails = emails;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getIden() {
        return iden;
    }

    public void setIden(String iden) {
        this.iden = iden;
    }
    
}
