/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package veterinara;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.neodatis.odb.ODB;
import org.neodatis.odb.ODBFactory;
import org.neodatis.odb.Objects;
import org.neodatis.odb.core.query.IQuery;
import org.neodatis.odb.core.query.criteria.Where;
import org.neodatis.odb.impl.core.query.criteria.CriteriaQuery;

/**
 *
 * @author agent
 */
public class BaseDatos {
    //Creamos unstring con la ruta
    String rutaDesk  = System.getProperty("user.home") + File.separator + "Documents";
    
    private ODB abrirConexion() {
        return ODBFactory.open(rutaDesk + File.separator + "DatosClinica");
    }
    private void cerrarConexion(ODB odb) {
        if (odb != null) {
            try {
                odb.close();
                System.out.println("BD cerrada");
            } catch (Exception e) {
                System.err.println("Error al cerrar la base de datos: " + e.getMessage());
            }
            odb = null;
        }
        else{
            System.out.println("BD nula, proseguimos");
        }
    }   

    private long CrearId(ODB odb, String tipo) {
        if (odb == null || tipo == null) {
            throw new IllegalArgumentException("Conexión o tipo nulo en CrearId");
        }

        try {
            Objects<IdGenerator> generadores = odb.getObjects(IdGenerator.class);
            IdGenerator idGen;

            if (generadores.isEmpty()) {
                idGen = new IdGenerator();
            } else {
                idGen = generadores.getFirst();
            }

            long newId = idGen.getNextId(tipo);
            odb.store(idGen);
            return newId;

        } catch (Exception e) {
            System.err.println("Error generando ID para tipo " + tipo + ": " + e.getMessage());
            return -1;
        }
    }
    public void CrearCliente(Cliente cli){
        if (cli == null) {
            System.err.println("Cliente nulo, no se puede guardar.");
            return;
        }
        
        ODB odb = abrirConexion();
        try{
            // Comprobar si ya existe un cliente con ese DNI
            IQuery consulta = new CriteriaQuery(Cliente.class, Where.equal("dni", cli.getDni()));
            Objects<Cliente> resultados = odb.getObjects(consulta);

            if (!resultados.isEmpty()) {
                System.out.println("Ya existe un cliente con DNI " + cli.getDni() + ", no se creará otro.");
                return;
            }
        
            cli.setId(CrearId(odb, "Cliente"));
            odb.store(cli);
            odb.commit();
            System.out.println("Cliente creado: " + cli.getNombre());
        }catch (Exception e) {
            System.err.println("Error al crear el cliente: " + e.getMessage());
            odb.rollback();
        } finally {
            cerrarConexion(odb);
        }
    }
    public void CrearMascota(Mascota mas, Cliente clie){
        ODB odb = abrirConexion();
        try{
            mas.setId(CrearId(odb, "Mascota"));
            
            IQuery qCli = new CriteriaQuery(Cliente.class, Where.equal("id", clie.getId()));
            Objects<Cliente> res = odb.getObjects(qCli);
            if (res.isEmpty()) {
                System.err.println("Cliente no encontrado con id: " + clie.getId());
                return;
            }
            Cliente clienteBD = res.getFirst();
            
            mas.setCliente(clienteBD); // aseguramos referencia
            odb.store(mas);
            odb.commit();
            System.out.println("Mascota creada y asignada correctamente");
        } catch (Exception e) {
            System.err.println("Error al crear el producto: " + e.getMessage());
        } finally {
            cerrarConexion(odb); 
        }
    }
    public void CrearConsulta(ConsultaVeterinaria cv, Mascota mas){
        ODB odb = abrirConexion();
        try{
            cv.setId(CrearId(odb, "Consulta"));

            // 1. Buscar la mascota real desde la BD
            IQuery qMas = new CriteriaQuery(Mascota.class, Where.equal("id", mas.getId()));
            Objects<Mascota> res = odb.getObjects(qMas);
            if (res.isEmpty()) {
                System.err.println("Mascota no encontrada con id: " + mas.getId());
                return;
            }

            Mascota mascotaBD = res.getFirst();

            // 2. Relacionar la consulta con la mascota persistida
            cv.setMascota(mascotaBD);

            // 3. Guardar la consulta
            odb.store(cv);

            // 4. Añadir la consulta a la mascota
            mascotaBD.agregarConsVet(cv);
            odb.store(mascotaBD);

            odb.commit();
            System.out.println("Consulta creada correctamente y asignada a la mascota.");

        } catch (Exception e) {
            System.err.println("Error al crear la consulta: " + e.getMessage());
            odb.rollback();
        } finally {
            cerrarConexion(odb);
        }
    }
    public void CrearProd(Producto prod){
        ODB odb = abrirConexion();
        try{
            prod.setId(CrearId(odb, "Producto"));
            odb.store(prod);
            System.out.println("Producto creado con éxito.");
        } catch (Exception e) {
            System.err.println("Error al crear el producto: " + e.getMessage());
        } finally {
            cerrarConexion(odb);
        }
    }
    public String CrearCita(Cita cita) {
        String mensaje = "";
        ODB odb = abrirConexion();
        try {
            if (existeCitaEnFecha(cita.getFechaHora(), odb)) {
                mensaje = "Ya existe una cita en ese horario.";
                System.err.println(mensaje);
                return mensaje;
            }

            // Recuperar cliente y mascota desde BD para evitar duplicados
            IQuery qMas = new CriteriaQuery(Mascota.class, Where.equal("id", cita.getMascota().getId()));
            IQuery qCli = new CriteriaQuery(Cliente.class, Where.equal("id", cita.getCliente().getId()));

            Objects<Mascota> resMas = odb.getObjects(qMas);
            Objects<Cliente> resCli = odb.getObjects(qCli);

            if (resMas.isEmpty() || resCli.isEmpty()) {
                mensaje = "No se encontró el cliente o la mascota en la base de datos.";
                System.err.println(mensaje);
                return mensaje;
            }

            Mascota mascotaBD = resMas.getFirst();
            Cliente clienteBD = resCli.getFirst();

            cita.setMascota(mascotaBD);
            cita.setCliente(clienteBD);

            cita.setId(CrearId(odb, "Cita"));
            odb.store(cita);
            odb.commit();

            mensaje = "Cita guardada correctamente.";
            System.out.println(mensaje);
        } catch (Exception e) {
            mensaje = "Error al guardar cita: " + e.getMessage();
            System.err.println(mensaje);
            if (odb != null) odb.rollback();
        } finally {
            cerrarConexion(odb);
        }

        return mensaje;
    }
    public boolean existeCitaEnFecha(Date fechaHora, ODB odb) {
        boolean existe = false;

        try {
            IQuery query = new CriteriaQuery(Cita.class, Where.equal("fechaHora", fechaHora));
            Objects<Cita> resultados = odb.getObjects(query);
            existe = !resultados.isEmpty();
        } catch (Exception e) {
            System.err.println("Error al comprobar cita duplicada: " + e.getMessage());
        }

        return existe;
    }
    public List<Producto> ConsultarTodosProd(){
        List<Producto> productos = new ArrayList<>();
        ODB odb = abrirConexion();
        try{
            if (odb == null) {
                System.err.println("No se pudo abrir la base de datos.");
                return null;
            }
            //Creamos la query de consulta para ver todos los Productos
            IQuery query = new CriteriaQuery(Producto.class);
            //Ahora pasamos la query para que nos devulvan los objetos correspondientes, en este caso todos
            Objects<Producto> resultados = odb.getObjects(query);
            while (resultados.hasNext()) {
                productos.add(resultados.next()); // Copias el contenido a una lista independiente
            }
            
        }catch (Exception e) {
            System.err.println("Error al crear el producto: " + e.getMessage());
        } finally {
            cerrarConexion(odb); 
        }
        
        return productos;
    }
    public List<Mascota> ConsultarTodasMascotas(){
        List<Mascota> mascotas = new ArrayList<>();
        ODB odb = abrirConexion();
        try{
            if (odb == null) {
                System.err.println("No se pudo abrir la base de datos.");
                return null;
            }
            //Creamos la query de consulta para ver todos los Productos
            IQuery query = new CriteriaQuery(Mascota.class);
            //Ahora pasamos la query para que nos devulvan los objetos correspondientes, en este caso todos
            Objects<Mascota> resultados = odb.getObjects(query);
            while (resultados.hasNext()) {
                mascotas.add(resultados.next()); // Copias el contenido a una lista independiente
            }
            
        }catch (Exception e) {
            System.err.println("Error al consultar las mascotas:" + e.getMessage());
        } finally {
            cerrarConexion(odb);
        }
        
        return mascotas;
    }
    public List<Mascota> ConsultarMascotasPorClie(Cliente c){
        ODB odb = abrirConexion();
        List<Mascota> lista = new ArrayList<>();
        try {
            IQuery q = new CriteriaQuery(Mascota.class, Where.equal("cliente.id", c.getId()));
            Objects<Mascota> res = odb.getObjects(q);
            while (res.hasNext()) {
                lista.add(res.next());
            }
        } finally {
            cerrarConexion(odb);
        }
        return lista;
    }
    public List<Cliente> ConsultarTodosClientes(){
        List<Cliente> clie = new ArrayList<>();
        ODB odb = abrirConexion();
        try{
            if (odb == null) {
                System.err.println("No se pudo abrir la base de datos.");
                return clie;
            }
            //Creamos la query de consulta para ver todos los Productos
            IQuery query = new CriteriaQuery(Cliente.class);
            //Ahora pasamos la query para que nos devulvan los objetos correspondientes, en este caso todos
            Objects<Cliente> resultados = odb.getObjects(query);
            while (resultados.hasNext()) {
                clie.add(resultados.next());
            }
            
        }catch (Exception e) {
            System.err.println("Error al consultar los Clientes:" + e.getMessage());
        } finally {
            cerrarConexion(odb);
        }
        
        return clie;
    }
    public List<Cita> ConsultarCitasMes(Date date){
        List<Cita> citasMes = new ArrayList<>();
        ODB odb = abrirConexion();
        try{
            if (odb == null) {
                System.err.println("No se pudo abrir la base de datos.");
                return null;
            }
            // Fecha de inicio: 1er día del mes
            Calendar calInicio = Calendar.getInstance();
            calInicio.setTime(date);
            calInicio.set(Calendar.DAY_OF_MONTH, 1);
            calInicio.set(Calendar.HOUR_OF_DAY, 0);
            calInicio.set(Calendar.MINUTE, 0);
            calInicio.set(Calendar.SECOND, 0);
            calInicio.set(Calendar.MILLISECOND, 0);
            Date fechaInicio = calInicio.getTime();
            
            // Fecha de fin: último día del mes
            Calendar calFin = (Calendar) calInicio.clone();
            calFin.set(Calendar.DAY_OF_MONTH, calFin.getActualMaximum(Calendar.DAY_OF_MONTH));
            calFin.set(Calendar.HOUR_OF_DAY, 23);
            calFin.set(Calendar.MINUTE, 59);
            calFin.set(Calendar.SECOND, 59);
            calFin.set(Calendar.MILLISECOND, 999);
            Date fechaFin = calFin.getTime();
            
            //Creamos la query de consulta para ver las citas del mes
            IQuery query = new CriteriaQuery(Cita.class, Where.and()
                    .add(Where.ge("fechaHora", fechaInicio))
                    .add(Where.le("fechaHora", fechaFin)));
        
            //Ahora pasamos la query para que nos devulvan los objetos correspondientes, en este caso todos
            Objects<Cita> resultados = odb.getObjects(query);
            while (resultados.hasNext()) {
                citasMes.add(resultados.next());
            }
            
        }catch (Exception e) {
            System.err.println("Error al consultar las citas:" + e.getMessage());
        } finally {
            cerrarConexion(odb);
        }
        return citasMes;
    }
    public List<Cita> ConsultarCitasDia(Date fecha) {
        List<Cita> citasDia = new ArrayList<>();
        ODB odb = abrirConexion();

        try {
            if (odb == null) {
                System.err.println("No se pudo abrir la base de datos.");
                return null;
            }

            Calendar calInicio = Calendar.getInstance();
            calInicio.setTime(fecha);
            calInicio.set(Calendar.HOUR_OF_DAY, 0);
            calInicio.set(Calendar.MINUTE, 0);
            calInicio.set(Calendar.SECOND, 0);
            calInicio.set(Calendar.MILLISECOND, 0);
            Date inicio = calInicio.getTime();

            Calendar calFin = (Calendar) calInicio.clone();
            calFin.set(Calendar.HOUR_OF_DAY, 23);
            calFin.set(Calendar.MINUTE, 59);
            calFin.set(Calendar.SECOND, 59);
            calFin.set(Calendar.MILLISECOND, 999);
            Date fin = calFin.getTime();

            IQuery query = new CriteriaQuery(Cita.class, Where.and()
                .add(Where.ge("fechaHora", inicio))
                .add(Where.le("fechaHora", fin))
            );

            Objects<Cita> resultados = odb.getObjects(query);
            while (resultados.hasNext()) {
                citasDia.add(resultados.next());
            }

        } catch (Exception e) {
            System.err.println("Error al consultar citas del día: " + e.getMessage());
        } finally {
            cerrarConexion(odb);
        }

        return citasDia;
    }
    public List<ConsultaVeterinaria> obtenerConsultasDeMascota(Mascota mas){
         ODB odb = abrirConexion();
        List<ConsultaVeterinaria> resultado = new ArrayList<>();

        try {
            IQuery q = new CriteriaQuery(ConsultaVeterinaria.class, Where.equal("mascota.id", mas.getId()));
            Objects<ConsultaVeterinaria> consultas = odb.getObjects(q);

            for (ConsultaVeterinaria c : consultas) {
                resultado.add(c);
            }
        } catch (Exception e) {
            System.err.println("Error al obtener historial de la mascota: " + e.getMessage());
        } finally {
            cerrarConexion(odb);
        }

        return resultado;
    }
    public Cliente ConsultarCliente(String nom){
        ODB odb = abrirConexion();
        try {
            if (odb == null) return null;

            IQuery query = new CriteriaQuery(Cliente.class, Where.equal("nombre", nom));
            Objects<Cliente> resultados = odb.getObjects(query);

            if (resultados.isEmpty()) return null;
            return resultados.getFirst();
        } catch (Exception e) {
            System.err.println("Error al consultar el cliente: " + e.getMessage());
            return null;
        } finally {
            cerrarConexion(odb);
        }
    }
    public boolean modificarCliente(Cliente cliente) {
        ODB odb = abrirConexion();
        if (odb == null || cliente == null) return false;

        try {
            IQuery q = new CriteriaQuery(Cliente.class, Where.equal("id", cliente.getId()));
            Objects<Cliente> resultado = odb.getObjects(q);
            if (!resultado.isEmpty()) {
            Cliente clienteReal = resultado.getFirst();

            // Actualizar sus campos
            clienteReal.setNombre(cliente.getNombre());
            clienteReal.setTelefono(cliente.getTelefono());
            clienteReal.setEmail(cliente.getEmail());
            clienteReal.setDireccion(cliente.getDireccion());
            clienteReal.setDni(cliente.getDni());
            clienteReal.setCiudad(cliente.getCiudad());
            clienteReal.setProv(cliente.getProv());
            clienteReal.setCodP(cliente.getCodP());

            odb.store(clienteReal);
            odb.commit();
            return true;
            }
        }catch (Exception e) {
            System.err.println("Error al modificar cliente: " + e.getMessage());
        } finally {
            cerrarConexion(odb);
        }
        return false;
    }
    public boolean modificarMascota(Mascota masc) {
        ODB odb = abrirConexion();
        if (odb == null || masc == null) return false;

        try {
            System.out.println("El id de la mascota a modificar es: " + masc.getId());
            IQuery q = new CriteriaQuery(Mascota.class, Where.equal("id", masc.getId()));
            Objects<Mascota> resultado = odb.getObjects(q);
            if (!resultado.isEmpty()) {
                Mascota mascReal = resultado.getFirst();

                mascReal.setNombre(masc.getNombre());
                mascReal.setRaza(masc.getRaza());
                mascReal.setEspecie(masc.getEspecie());
                mascReal.setMicrochip(masc.getMicrochip());
                mascReal.setCapa(masc.getCapa());
                mascReal.setPelo(masc.getPelo());
                mascReal.setfNacimiento(masc.getfNacimiento());

                odb.store(mascReal);
                odb.commit();
                System.out.println("Aqui deberia haber modificado la mascota");
                return true;
            }
            else{
                System.out.println("No lo encuentra en la bd");
            }

        } catch (Exception e) {
            System.err.println("Error al modificar mascota: " + e.getMessage());
        } finally {
            cerrarConexion(odb);
        }

        return false;
    }
    public boolean ModificarProductos(List<Producto> prodModificados){
        if (prodModificados == null || prodModificados.isEmpty()) {return false;}

        ODB odb = abrirConexion();              // 1. Abrir BDOO
        try {
            for (Producto prodMod : prodModificados) {

                // 2. Localizar en BD el objeto con el mismo id
                IQuery q = new CriteriaQuery(Producto.class, Where.equal("id", prodMod.getId()));
                Objects<Producto> res = odb.getObjects(q);
                if (res.isEmpty()) {
                    System.err.println("No existe producto con id " + prodMod.getId());
                    continue;                   // pasa al siguiente
                }
                Producto prodBD = res.getFirst();

                /* 3. Copiar CAMBIOS */
                prodBD.setNombre      (prodMod.getNombre());
                prodBD.setDescripcion (prodMod.getDescripcion());
                prodBD.setCant        (prodMod.getCant());
                prodBD.setpCompra     (prodMod.getpCompra());
                prodBD.setpPublico    (prodMod.getpPublico());
                prodBD.setIva         (prodMod.getIva());
                prodBD.setCatProd     (prodMod.getCatProd());

                /* 4. Guardar */
                odb.store(prodBD);
            }
            return true;
        } catch (Exception e) {
            odb.rollback();
            System.err.println("Error al modificar productos: " + e.getMessage());
            return false;
        } finally {
            cerrarConexion(odb);                // 5. Cerrar BDOO
        }
    }
    public boolean modificarProducto(Producto prod) {
        ODB odb = abrirConexion();
        if (odb == null || prod == null) return false;

        try {
            IQuery q = new CriteriaQuery(Producto.class, Where.equal("id", prod.getId()));
            Objects<Producto> res = odb.getObjects(q);
            if (!res.isEmpty()) {
                Producto pReal = res.getFirst();
                pReal.setNombre(prod.getNombre());
                pReal.setpCompra(prod.getpCompra());
                pReal.setpPublico(prod.getpPublico());
                pReal.setCant(prod.getCant());
                pReal.setCatProd(prod.getCatProd());

                odb.store(pReal);
                odb.commit();
                return true;
            }
        } catch (Exception e) {
            System.err.println("Error al modificar producto: " + e.getMessage());
        } finally {
            cerrarConexion(odb);
        }

        return false;
    }
    public boolean borrarCliente(Cliente cli) {
        ODB odb = abrirConexion();
        if (cli == null || odb == null){
            System.err.println("Cliente nulo o BD no disponible.");
            return false;
        }
        try {
            // Volvemos a cargar el cliente desde esta conexión
            IQuery qCli = new CriteriaQuery(Cliente.class, Where.equal("id", cli.getId()));
            Objects<Cliente> resultado = odb.getObjects(qCli);

            if (resultado.isEmpty()) {
                System.err.println("No se encontró el cliente en BD.");
                return false;
            }
            
            Cliente clienteReal = resultado.getFirst();
            
            //Primero borramos a las mascotas
            IQuery qMasc = new CriteriaQuery(Mascota.class, Where.equal("cliente", clienteReal));
            Objects<Mascota> mascs = odb.getObjects(qMasc);
            while (mascs.hasNext()) {
                odb.delete(mascs.next());
            }
            
            //Y ahora al cliente
            odb.delete(clienteReal);

            odb.commit();
            System.out.println("Cliente y mascotas eliminados.");
            return true;
        } catch (Exception ex) {
            odb.rollback(); 
            System.err.println("Error al borrar cliente: " + ex.getMessage());
            return false;
        } finally {
            cerrarConexion(odb);
            limpiezaAutomatica();
        }
    }
    public boolean borrarCita(Cita cita) {
        ODB odb = abrirConexion();
        if (odb == null || cita == null) return false;

        try {
            IQuery q = new CriteriaQuery(Cita.class, Where.equal("id", cita.getId()));
            Objects<Cita> res = odb.getObjects(q);
            if (!res.isEmpty()) {
                odb.delete(res.getFirst());
                odb.commit();
                return true;
            }
        } catch (Exception e) {
            System.err.println("Error al borrar cita: " + e.getMessage());
        } finally {
            cerrarConexion(odb);
            limpiezaAutomatica();
        }
        return false;
    }
    public boolean borrarMascota(Mascota masc) {
        ODB odb = abrirConexion();
        if (odb == null || masc == null) return false;

        try {
            IQuery q = new CriteriaQuery(Mascota.class, Where.equal("id", masc.getId()));
            Objects<Mascota> res = odb.getObjects(q);
            if (!res.isEmpty()) {
                odb.delete(res.getFirst());
                odb.commit();
                return true;
            }
        } catch (Exception e) {
            System.err.println("Error al borrar mascota: " + e.getMessage());
        } finally {
            cerrarConexion(odb);
            limpiezaAutomatica();
        }
        return false;
    }
    public boolean borrarProducto(Producto prod) {
        ODB odb = abrirConexion();
        if (odb == null || prod == null) return false;

        try {
            IQuery q = new CriteriaQuery(Producto.class, Where.equal("id", prod.getId()));
            Objects<Producto> res = odb.getObjects(q);
            if (!res.isEmpty()) {
                odb.delete(res.getFirst());
                odb.commit();
                return true;
            }
        } catch (Exception e) {
            odb.rollback();
            System.err.println("Error al borrar producto: " + e.getMessage());
        } finally {
            cerrarConexion(odb);
            limpiezaAutomatica();
        }
        return false;
    }
    public boolean añadirStock(Producto prod, int cantidad) {
        ODB odb = abrirConexion();
        if (odb == null || prod == null || cantidad <= 0) return false;

        try {
            IQuery q = new CriteriaQuery(Producto.class, Where.equal("id", prod.getId()));
            Objects<Producto> res = odb.getObjects(q);
            if (!res.isEmpty()) {
                Producto pReal = res.getFirst();
                pReal.setCant(pReal.getCant() + cantidad);
                odb.store(pReal);
                odb.commit();
                return true;
            }
        } catch (Exception e) {
            odb.rollback();
            System.err.println("Error al añadir stock: " + e.getMessage());
        } finally {
            cerrarConexion(odb);
        }

        return false;
    }
    public List<ConsultaVeterinaria> consultarTodasConsultas() {
        List<ConsultaVeterinaria> consultas = new ArrayList<>();
        ODB odb = abrirConexion();

        try {
            if (odb == null) {
                System.err.println("No se pudo abrir la base de datos.");
                return consultas;
            }

            Objects<ConsultaVeterinaria> resultados = odb.getObjects(ConsultaVeterinaria.class);

            while (resultados.hasNext()) {
                consultas.add(resultados.next());
            }

            System.out.println("Consultas recuperadas: " + consultas.size());

        } catch (Exception e) {
            System.err.println("Error al consultar las consultas veterinarias: " + e.getMessage());
        } finally {
            cerrarConexion(odb);
        }

        return consultas;
    }
    public void limpiezaAutomatica() {
        ODB odb = abrirConexion();
        if (odb == null) return;

        try {
            int countMascotas = 0;
            int countConsultas = 0;
            int countCitas = 0;

            // 1. Mascotas sin cliente
            IQuery qMascotas = new CriteriaQuery(Mascota.class, Where.isNull("cliente"));
            Objects<Mascota> mascotasSinCliente = odb.getObjects(qMascotas);
            while (mascotasSinCliente.hasNext()) {
                odb.delete(mascotasSinCliente.next());
                countMascotas++;
            }

            // 2. Consultas sin mascota
            IQuery qConsultas = new CriteriaQuery(ConsultaVeterinaria.class, Where.isNull("mascota"));
            Objects<ConsultaVeterinaria> consultasSinMascota = odb.getObjects(qConsultas);
            while (consultasSinMascota.hasNext()) {
                odb.delete(consultasSinMascota.next());
                countConsultas++;
            }

            // 3. Citas sin mascota
            IQuery qCitas = new CriteriaQuery(Cita.class, Where.isNull("mascota"));
            Objects<Cita> citasSinMascota = odb.getObjects(qCitas);
            while (citasSinMascota.hasNext()) {
                odb.delete(citasSinMascota.next());
                countCitas++;
            }

            odb.commit();
            System.out.println("Limpieza automática completada: "
                + countMascotas + " mascotas huérfanas, "
                + countConsultas + " consultas huérfanas, y "
                + countCitas + " citas huérfanas eliminadas.");

        } catch (Exception e) {
            odb.rollback();
            System.err.println("Error durante limpieza automática: " + e.getMessage());
        } finally {
            cerrarConexion(odb);
        }
    }
}