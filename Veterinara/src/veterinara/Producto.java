/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package veterinara;

/**
 *
 * @author agent
 */
public class Producto {
    private String nombre, descripcion;
    private int cant;
    private double pCompra, pPublico;
    private double  iva;
    private long id;
    private CategoriaProducto catProd;
    
    public enum CategoriaProducto {
    TIENDA,
    VETERINARIA,
    PELUQUERIA
    }

    public Producto(String nombre, String descripcion, int cant, double pCompra, double pPublico, CategoriaProducto catProd, double iva) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.cant = cant;
        this.pCompra = pCompra;
        this.pPublico = pPublico;
        this.catProd = catProd;
        this.iva = iva;
    }

    public Producto() {
    }
    
    public double beneficio(){
        double resto = (pPublico - (pPublico*iva)) - pCompra;
        
        return resto;
    }

    public double getIva() {
        return iva;
    }

    public void setIva(double iva) {
        this.iva = iva;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
    
    public CategoriaProducto getCatProd() {
        return catProd;
    }

    public void setCatProd(CategoriaProducto catProd) {
        this.catProd = catProd;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getCant() {
        return cant;
    }

    public void setCant(int cant) {
        this.cant = cant;
    }

    public double getpCompra() {
        return pCompra;
    }

    public void setpCompra(double pCompra) {
        this.pCompra = pCompra;
    }

    public double getpPublico() {
        return pPublico;
    }

    public void setpPublico(double pPublico) {
        this.pPublico = pPublico;
    }
    
    
}
