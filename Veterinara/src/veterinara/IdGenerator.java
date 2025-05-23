/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package veterinara;

/**
 *
 * @author agent
 */
//Clase para controlar los id de los clientes
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class IdGenerator implements Serializable {
    private Map<String, Long> contadores;

    public IdGenerator() {
        this.contadores = new HashMap<>();
    }

    /**
     * Devuelve el próximo ID para el tipo indicado, y lo incrementa.
     * @param tipo Ejemplo: "Cliente", "Mascota", etc.
     * @return nuevo ID para ese tipo
     */
    public long getNextId(String tipo) {
        long idActual = contadores.getOrDefault(tipo, 0L);
        long siguiente = idActual + 1;
        contadores.put(tipo, siguiente);
        return siguiente;
    }

    /**
     * Devuelve el último ID utilizado para un tipo (sin incrementarlo)
     */
    public long getCurrentId(String tipo) {
        return contadores.getOrDefault(tipo, 0L);
    }

    /**
     * Establece manualmente el ID actual para un tipo.
     */
    public void setCurrentId(String tipo, long nuevoId) {
        contadores.put(tipo, nuevoId);
    }
}