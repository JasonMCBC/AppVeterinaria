/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package veterinara;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

/**
 *
 * @author agent
 */
public class ConsultaVeterinaria {
    private String peso, temperatura, anamnesis, sintomatologia, 
            pruebasDiagnosticas, diagnostico, tratamiento, vacunas, 
            desparasitacion, alimentacionRecomendada, presionArterial, nCliente;
    private Date fecha;
    private long id;
    private Mascota mascota;
    
    //Constructores
    public ConsultaVeterinaria() {
    }

    public ConsultaVeterinaria(String peso, String temperatura, String anamnesis, String sintomatologia, String pruebasDiagnosticas, 
            String diagnostico, String tratamiento, String vacunas, String desparasitacion, String alimentacionRecomendada,
            String presionArterial, Date fecha, String cli, Mascota mas) {
        this.peso = peso;
        this.temperatura = temperatura;
        this.anamnesis = anamnesis;
        this.sintomatologia = sintomatologia;
        this.pruebasDiagnosticas = pruebasDiagnosticas;
        this.diagnostico = diagnostico;
        this.tratamiento = tratamiento;
        this.vacunas = vacunas;
        this.desparasitacion = desparasitacion;
        this.alimentacionRecomendada = alimentacionRecomendada;
        this.presionArterial = presionArterial;
        this.fecha = fecha;
        this.nCliente = cli;
        this.mascota = mas;
    }

    public Mascota getMascota() {
        return mascota;
    }

    public void setMascota(Mascota mascota) {
        this.mascota = mascota;
    }

    public String getnCliente() {
        return nCliente;
    }

    public void setnCliente(String nCliente) {
        this.nCliente = nCliente;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPeso() {
        return peso;
    }

    public void setPeso(String peso) {
        this.peso = peso;
    }

    public String getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(String temperatura) {
        this.temperatura = temperatura;
    }

    public String getAnamnesis() {
        return anamnesis;
    }

    public void setAnamnesis(String anamnesis) {
        this.anamnesis = anamnesis;
    }

    public String getSintomatologia() {
        return sintomatologia;
    }

    public void setSintomatologia(String sintomatología) {
        this.sintomatologia = sintomatología;
    }

    public String getPruebasDiagnosticas() {
        return pruebasDiagnosticas;
    }

    public void setPruebasDiagnosticas(String pruebasDiagnosticas) {
        this.pruebasDiagnosticas = pruebasDiagnosticas;
    }

    public String getDiagnostico() {
        return diagnostico;
    }

    public void setDiagnostico(String diagnostico) {
        this.diagnostico = diagnostico;
    }

    public String getTratamiento() {
        return tratamiento;
    }

    public void setTratamiento(String tratamiento) {
        this.tratamiento = tratamiento;
    }

    public String getVacunas() {
        return vacunas;
    }

    public void setVacunas(String vacunas) {
        this.vacunas = vacunas;
    }

    public String getDesparasitacion() {
        return desparasitacion;
    }

    public void setDesparasitacion(String desparasitacion) {
        this.desparasitacion = desparasitacion;
    }

    public String getAlimentacionRecomendada() {
        return alimentacionRecomendada;
    }

    public void setAlimentacionRecomendada(String alimentacionRecomendada) {
        this.alimentacionRecomendada = alimentacionRecomendada;
    }

    public String getPresionArterial() {
        return presionArterial;
    }

    public void setPresionArterial(String presionArterial) {
        this.presionArterial = presionArterial;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

}
