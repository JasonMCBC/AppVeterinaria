/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package veterinara;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author agent
 */
public class Mascota {
    private String nombre, raza, microchip, pelo, capa, especie;
    private Date fNacimiento;
    private Cliente cliente;
    private List<ConsultaVeterinaria> consult;
    private boolean asegurado, desparasitado, vacunado;
    private Date fechaDesp, fechaVac;
    private long id;
    
    public void agregarConsVet(ConsultaVeterinaria consulta) {
        if (consulta != null) {
            this.consult.add(consulta);
        }
    }
    
    public Mascota() {
        this.consult = new ArrayList<>();
    }

    public Mascota(String nombre, String especie, String raza, String microchip, String pelo, String capa, Date fNacimiento) {
        this.nombre = nombre;
        this.especie = especie;
        this.raza = raza;
        this.microchip = microchip;
        this.pelo = pelo;
        this.capa = capa;
        this.fNacimiento = fNacimiento;
    }
    @Override
    public String toString() {
        return nombre;
    }

    public String getEspecie() {
        return especie;
    }

    public void setEspecie(String especie) {
        this.especie = especie;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getRaza() {
        return raza;
    }

    public void setRaza(String raza) {
        this.raza = raza;
    }

    public String getMicrochip() {
        return microchip;
    }

    public void setMicrochip(String microchip) {
        this.microchip = microchip;
    }

    public String getPelo() {
        return pelo;
    }

    public void setPelo(String pelo) {
        this.pelo = pelo;
    }

    public String getCapa() {
        return capa;
    }

    public void setCapa(String capa) {
        this.capa = capa;
    }

    public Date getfNacimiento() {
        return fNacimiento;
    }

    public void setfNacimiento(Date fNacimiento) {
        this.fNacimiento = fNacimiento;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente dueño) {
        this.cliente = dueño;
    }

    public List<ConsultaVeterinaria> getConsult() {
        return consult;
    }

    public void setConsult(List<ConsultaVeterinaria> consult) {
        this.consult = consult;
    }

    public boolean isAsegurado() {
        return asegurado;
    }

    public void setAsegurado(boolean asegurado) {
        this.asegurado = asegurado;
    }

    public boolean isDesparasitado() {
        return desparasitado;
    }

    public void setDesparasitado(boolean desparasitado) {
        this.desparasitado = desparasitado;
    }

    public boolean isVacunado() {
        return vacunado;
    }

    public void setVacunado(boolean vacunado) {
        this.vacunado = vacunado;
    }

    public Date getFechaDesp() {
        return fechaDesp;
    }

    public void setFechaDesp(Date fechaDesp) {
        this.fechaDesp = fechaDesp;
    }

    public Date getFechaVac() {
        return fechaVac;
    }

    public void setFechaVac(Date fechaVac) {
        this.fechaVac = fechaVac;
    }
    
}
