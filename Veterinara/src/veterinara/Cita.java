/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package veterinara;

import java.util.Date;

/**
 *
 * @author agent
 */
public class Cita {
    private long id;
    private Date fechaHora;
    private Mascota mascota;
    private Cliente cliente;
    private String motivo;
    private TipoCita tCita;
    
    public enum TipoCita {
    CITA,
    OPERACION,
    VACUNACION, 
    PELUQUERIA
    }

    // Constructor, getters y setters
    public Cita() {
    }

    public Cita(Date fechaHora, Mascota mascota, Cliente cliente, String motivo, TipoCita tCita) {
        this.fechaHora = fechaHora;
        this.mascota = mascota;
        this.cliente = cliente;
        this.motivo = motivo;
        this.tCita = tCita;
    }

    public TipoCita getTipoCita() {
        return tCita;
    }

    public void setTipoCita(TipoCita tCita) {
        this.tCita = tCita;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(Date fechaHora) {
        this.fechaHora = fechaHora;
    }

    public Mascota getMascota() {
        return mascota;
    }

    public void setMascota(Mascota mascota) {
        this.mascota = mascota;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
    
}