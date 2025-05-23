/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package veterinara;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import org.neodatis.odb.Objects;
import veterinara.Cita.TipoCita;
import veterinara.Producto.CategoriaProducto;

/**
 *
 * @author agent
 */
public class Main extends javax.swing.JFrame {

    /**
     * Creates new form Main
     */
    
    private BaseDatos bd = new BaseDatos();
    private DefaultTableModel modeloTablaInv, modeloTablaCli, modeloTablaAgenda, modeloTablaCobrar, modeloTablaFacturacion, modeloTablaHistorial;
    private JTextField editor;
    private List<Cliente> listaClientes;
    
    public Main() {
        bd.limpiezaAutomatica();
        initComponents();
        initListeners();
        cargarKeyReleased();
        initCalendar();
        initTablas();
        cargarAgendaInicio();
        cbTipoCita.setModel(new DefaultComboBoxModel<>(TipoCita.values()));
        btInventario.setOpaque(false);
        btInventario.setBorderPainted(false); // Opcional: quita el borde
        agrSubPanel();
        CardLayout cl = (CardLayout)(pContenido.getLayout());
        cl.show(pContenido, "pInicio");
    }
    
    private void initListeners(){
        
        monthView.addActionListener(e -> {
            Date diaSeleccionado = monthView.getSelectionDate();
            System.out.println("dia seleccionado: " + diaSeleccionado);
            cargarAgendaDelDia(diaSeleccionado);
        });
        
        tHistorialConsultas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int fila = tHistorialConsultas.getSelectedRow();
                if (fila != -1) {
                    ConsultaVeterinaria consulta = (ConsultaVeterinaria) tHistorialConsultas.getValueAt(fila, 2); // Columna oculta
                    mostrarDetallesConsulta(consulta);
                }
            }
        });
    }
    private void initCalendar(){
        Date date = new Date();
        List<Cita> citas = bd.ConsultarCitasMes(date);
        if(citas == null){ return;}
        Set<Date> operaciones = new HashSet<>();
        Set<Date> consultas = new HashSet<>();
        Set<Date> peluqueria = new HashSet<>();
        Set<Date> vacunaciones = new HashSet<>();

        for (Cita c : citas) {
            switch (c.getTipoCita()) {
                case CITA -> operaciones.add(truncarFecha(c.getFechaHora()));
                case OPERACION -> consultas.add(truncarFecha(c.getFechaHora()));
                case PELUQUERIA -> peluqueria.add(truncarFecha(c.getFechaHora()));
                case VACUNACION -> vacunaciones.add(truncarFecha(c.getFechaHora()));
            }
        }

        // Puedes usar colores con `setFlaggedDates`, pero para colores diferentes, usas HighlightPainter
        monthView.setFlaggedDates(operaciones.toArray(new Date[0])); // se verán marcadas

        // Para colorear distinto, necesitas aplicar `Highlighters` (te lo paso si quieres)
    }
    public void cargarAgendaDelDia(Date fecha) {
        System.out.println(new SimpleDateFormat("dd/MM/yyyy").format(fecha));
        this.lbFecha.setText(new SimpleDateFormat("dd/MM/yyyy").format(fecha));

        List<Cita> citas = bd.ConsultarCitasDia(fecha);
        modeloTablaAgenda.setRowCount(0); // Limpia

        for (Cita c : citas) {
            Mascota m = c.getMascota();
            Cliente cl = c.getCliente();

            String nombreMascota = (m != null) ? m.getNombre() : "(sin mascota)";
            String nombreCliente = (cl != null) ? cl.getNombre() : "(sin cliente)";

            modeloTablaAgenda.addRow(new Object[]{
                new SimpleDateFormat("HH:mm").format(c.getFechaHora()),
                nombreMascota,
                nombreCliente,
                c.getMotivo(),
                c.getTipoCita().toString(),
                c
            });
        }
        
        jdAgenda.setModal(true);
        jdAgenda.setLocationRelativeTo(this);
        jdAgenda.pack();
        jdAgenda.setVisible(true);
    }
    public void cargarAgendaInicio() {
        Date fecha = new Date();
        System.out.println(new SimpleDateFormat("dd/MM/yyyy").format(fecha));
        this.lbFechaInicio.setText(new SimpleDateFormat("dd/MM/yyyy").format(fecha));
        List<Cita> citas = bd.ConsultarCitasDia(fecha);
        modeloTablaAgenda.setRowCount(0); // Limpia

        for (Cita c : citas) {
            modeloTablaAgenda.addRow(new Object[]{
            new SimpleDateFormat("HH:mm").format(c.getFechaHora()),
            c.getMascota().getNombre(),
            c.getCliente().getNombre(),
            c.getMotivo(),
            c.getTipoCita().toString(),
            c
            });
        }
        
    }
    private void initTablas(){
        // Preparamos las columnas que quieres en la tabla
        String[] columnasInv = {"Id", "Nombre", "Precio", "Stock", "Familia", "ProductoObj"};
        String[] columnasCli = {"Id", "Nombre Dueño", "Mascota", "Número", "Dueño", "MascotaObj"};
        String[] columnasAge = {"Hora", "Mascota", "Cliente", "Motivo", "Tipo", "CitaObj"};
        String[] columnasCobrar = {"Producto", "Cant", "Precio", "Acción", "ProductoObj"};
        String[] columnasFact = {"Producto", "Stock", "Precio", "Acción", "ProductoObj"};
        String[] columnasHist = {"Fecha", "Motivo", "HistObj"};
        
        // Creamos el modelo con 0 filas inicialmente
        modeloTablaInv = new DefaultTableModel(columnasInv, 0){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        }; 
        modeloTablaCli = new DefaultTableModel(columnasCli, 0){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        modeloTablaAgenda = new DefaultTableModel(columnasAge, 0){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        modeloTablaCobrar = new DefaultTableModel(columnasCobrar, 0){
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3;
            }
        };
        modeloTablaFacturacion = new DefaultTableModel(columnasFact, 0){
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3;
            }
        };
        modeloTablaHistorial = new DefaultTableModel(columnasHist, 0){
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3;
            }
        };
        
        //Asignamos el modelo a una tabla
        tInventario.setModel(modeloTablaInv);
        tClientes.setModel(modeloTablaCli);
        tAgenda.setModel(modeloTablaAgenda);
        tAgendaInicio.setModel(modeloTablaAgenda);
        tInventarioFacturacion.setModel(modeloTablaFacturacion);
        tFactura.setModel(modeloTablaCobrar);
        tHistorialConsultas.setModel(modeloTablaHistorial);
        
        // Oculta la última columna ("Producto")
        tClientes.getColumnModel().getColumn(4).setMinWidth(0);
        tClientes.getColumnModel().getColumn(4).setMaxWidth(0);
        tClientes.getColumnModel().getColumn(4).setWidth(0);
        tClientes.getColumnModel().getColumn(5).setMinWidth(0);
        tClientes.getColumnModel().getColumn(5).setMaxWidth(0);
        tClientes.getColumnModel().getColumn(5).setWidth(0);
        tInventarioFacturacion.getColumnModel().getColumn(4).setMinWidth(0);
        tInventarioFacturacion.getColumnModel().getColumn(4).setMaxWidth(0);
        tInventarioFacturacion.getColumnModel().getColumn(4).setWidth(0);
        tFactura.getColumnModel().getColumn(4).setMinWidth(0);
        tFactura.getColumnModel().getColumn(4).setMaxWidth(0);
        tFactura.getColumnModel().getColumn(4).setWidth(0);
        tAgendaInicio.getColumnModel().getColumn(5).setMinWidth(0);
        tAgendaInicio.getColumnModel().getColumn(5).setMaxWidth(0);
        tAgendaInicio.getColumnModel().getColumn(5).setWidth(0);
        tInventario.getColumnModel().getColumn(5).setMinWidth(0);
        tInventario.getColumnModel().getColumn(5).setMaxWidth(0);
        tInventario.getColumnModel().getColumn(5).setWidth(0);
        tAgenda.getColumnModel().getColumn(5).setMinWidth(0);
        tAgenda.getColumnModel().getColumn(5).setMaxWidth(0);
        tAgenda.getColumnModel().getColumn(5).setWidth(0);
        tHistorialConsultas.getColumnModel().getColumn(2).setMinWidth(0);
        tHistorialConsultas.getColumnModel().getColumn(2).setMaxWidth(0);
        tHistorialConsultas.getColumnModel().getColumn(2).setWidth(0);
        
        
        tInventarioFacturacion.getColumn("Acción").setCellRenderer(new ButtonRenderer());
        tInventarioFacturacion.getColumn("Acción").setCellEditor(new ButtonEditor(new JCheckBox(), tInventarioFacturacion));
        tInventarioFacturacion.setRowHeight(28);
        tFactura.getColumn("Acción").setCellRenderer(new ButtonRendererElim());
        tFactura.getColumn("Acción").setCellEditor(new ButtonEditorElim(new JCheckBox(), tFactura));
        tFactura.setRowHeight(28);
        tFactura.getColumn("Cant").setMaxWidth(50);
        tFactura.getColumn("Producto").setMaxWidth(80);
        tFactura.getColumn("Precio").setMaxWidth(50);
    }
    private void cargarProdFact(){
        //Creamos una lista de objetos para recoger los objetos de la bd
        List<Producto> listaEntera = bd.ConsultarTodosProd();
        //Reseteamos la tabla
        modeloTablaFacturacion.setRowCount(0);
        //Mientras queden objetos en la lista los vamos añadiendo a la tabla
        for(Producto prod : listaEntera){
            modeloTablaFacturacion.addRow(new Object[]{
            prod.getNombre(),
            prod.getCant(),
            prod.getpPublico(),
            "Añadir",
            prod
            });
        }
    }
    private void añadirProductoAFactura(Producto prod){
        DefaultTableModel modelo = (DefaultTableModel) tFactura.getModel();

        for (int fila = 0; fila < modelo.getRowCount(); fila++) {
            Producto prodTabla = (Producto) modelo.getValueAt(fila, 4); // columna oculta o ID

            if (prodTabla.getId() == prod.getId()) {
                // Producto ya existe en la tabla, solo incrementamos cantidad
                int cantActual = (int) modelo.getValueAt(fila, 1);
                cantActual++;
                modelo.setValueAt(cantActual, fila, 1);

                double nuevoPrecio = cantActual * prod.getpPublico();
                modelo.setValueAt(nuevoPrecio, fila, 2); // columna precio total
                actualizarTotalFactura();
                return;
            }
        }
        
        // Si no está aún en la tabla, lo añadimos
        int cant = 1;
        double precio = prod.getpPublico() * cant;

        modelo.addRow(new Object[]{
            prod.getNombre(), // Col 0
            cant,             // Col 1: cantidad
            precio,            // Col 2: subtotal
            "Eliminar",         //Col 3: Acción
            prod              // Col 3: producto oculto
        });

        actualizarTotalFactura();
    }
    private void actualizarTotalFactura() {
        double total = 0;

        for (int i = 0; i < tFactura.getRowCount(); i++) {
            Object val = tFactura.getValueAt(i, 2); // Col 2: Precio total
            if (val instanceof Number) {
                total += ((Number) val).doubleValue();
            }
        }

        lbTotal.setText(String.format("%.2f €", total));
    }
    private void busquedaEnTabla(String busq){
        //Creamos una lista de objetos para recoger los objetos de la bd
        List<Producto> listaEntera = bd.ConsultarTodosProd();
        //Reseteamos la tabla
        modeloTablaFacturacion.setRowCount(0);
        //Mientras queden objetos en la lista los vamos añadiendo a la tabla
        for(Producto prod : listaEntera){
            if(prod.getNombre().toLowerCase().contains(busq)){
                modeloTablaFacturacion.addRow(new Object[]{
                prod.getNombre(),
                prod.getCant(),
                prod.getpPublico(),
                "Añadir",
                prod
                });
            }
        }
    }
    private void cargarProdInv(){
        //Creamos una lista de objetos para recoger los objetos de la bd
        List<Producto> listaEntera = bd.ConsultarTodosProd();
        //Reseteamos la tabla
        modeloTablaInv.setRowCount(0);
        //Mientras queden objetos en la lista los vamos añadiendo a la tabla
        for(Producto prod : listaEntera){
            modeloTablaInv.addRow(new Object[]{
            prod.getId(),
            prod.getNombre(),
            prod.getpPublico(),
            prod.getCant(),
            prod.getCatProd(),
            prod
            });
        }
    }
    private void cargarMascotasInv(){
        //Creamos una lista de objetos para recoger los objetos de la bd
        List<Mascota> listaEntera = bd.ConsultarTodasMascotas();
        System.out.println("Nos devuelve la lista de mascotas");
        //Reseteamos la tabla
        modeloTablaCli.setRowCount(0);
        //Mientras queden objetos en la lista los vamos añadiendo a la tabla
        for(Mascota masc : listaEntera){
            Cliente cli = masc.getCliente();
            String nombreCli = (cli != null) ? cli.getNombre() : "(sin cliente)";
            String telCli = (cli != null) ? cli.getTelefono() : "(desconocido)";
        
            modeloTablaCli.addRow(new Object[]{
                masc.getId(),
                nombreCli,
                masc.getNombre(),
                telCli,
                cli,
                masc
            });
        }
    }
    private void agrSubPanel(){
        pContenido.add(pConsultas, "pConsultas");
        pContenido.add(pInventario, "pInventario");
        pContenido.add(pClientes, "pClientes");
        pContenido.add(pCitas, "pCitas");
        pContenido.add(pInicio, "pInicio");
        pContenido.add(pFacturacion, "pFacturacion");
    }
    private void cargarCBClientes(javax.swing.JComboBox cb) {
        listaClientes = bd.ConsultarTodosClientes();
        cb.removeAllItems(); // Limpia antes de rellenar
        
        for (Cliente c : listaClientes) {
            cb.addItem(c);
        }
    }
    private void cargarClientesConsultas() {
        System.out.println("Cargando clientes...");
        listaClientes = bd.ConsultarTodosClientes();
        cbClientesConsul.removeAllItems(); // Limpia antes de rellenar
        
        for (Cliente c : listaClientes) {
            System.out.println("Cliente añadido: " + c.getId() + " - " + c.getNombre());
            cbClientesConsul.addItem(c);
        }
    }
    private void cargarKeyReleased(){ //Carga en el comboBox de citas los clientes coincidentes con las letras que se escriben
        cbCitas.setEditable(true);
        editor = (JTextField) cbCitas.getEditor().getEditorComponent();
        editor.addKeyListener(new KeyAdapter() {//Añadí de esta forma el keylistener por que de forma gráfica no hacia ninguna accion al apretar una key
            @Override
            public void keyReleased(KeyEvent evt) {
                String texto = editor.getText();
                if (texto == null) texto = "";

                // Limpia el combo y vuelve a llenarlo con coincidencias
                DefaultComboBoxModel<Cliente> modelo = new DefaultComboBoxModel<>();

                Set<Long> idsAñadidos = new HashSet<>();

                for (Cliente c : listaClientes) {
                    if (c.getNombre().toLowerCase().contains(texto.toLowerCase()) && !idsAñadidos.contains(c.getId())) {
                        modelo.addElement(c);
                        idsAñadidos.add(c.getId());
                    }
                }
                cbCitas.setModel(modelo);
                cbCitas.setSelectedItem(texto); // mantiene el texto que el usuario ha escrito
                cbCitas.showPopup(); // muestra el desplegable con resultados
            }
        });
    }
    private Date truncarFecha(Date fecha) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(fecha);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
    private void mostrarHistorialConsultas(Mascota mascota) {
        List<ConsultaVeterinaria> historial = bd.obtenerConsultasDeMascota(mascota);
        DefaultTableModel modeloHistorial = (DefaultTableModel) tHistorialConsultas.getModel();
        modeloHistorial.setRowCount(0); // Limpiar
        
        if (historial.isEmpty()) {
            System.out.println("No hay consultas registradas para esta mascota.");
        } else {
            System.out.println("Primera fecha registrada: " + 
                new SimpleDateFormat("dd/MM/yyyy").format(historial.get(0).getFecha()));
        }

        for (ConsultaVeterinaria c : historial) {
            modeloHistorial.addRow(new Object[]{
                new SimpleDateFormat("dd/MM/yyyy").format(c.getFecha()),
                c.getAnamnesis(),
                c              // objeto completo en columna oculta
            });
        }

        // Mostrar ventana o panel
        jdHistorial.setLocationRelativeTo(this);
        jdHistorial.setVisible(true);
    }
    private void mostrarDetallesConsulta(ConsultaVeterinaria consulta) {
        //terminar el codigo para que se muestre la consulta
        this.lbFechaHist.setText(new SimpleDateFormat("dd/MM/yyyy").format(consulta.getFecha()));
        this.lbClienteHist.setText(consulta.getnCliente());
        this.lbMascotaHist.setText(consulta.getMascota().getNombre());
        this.tfPesoConsul1.setText(consulta.getPeso());
        this.tfTempConsul1.setText(consulta.getTemperatura());
        this.taAnamnesis1.setText(consulta.getAnamnesis());
        this.taSintomatologia1.setText(consulta.getSintomatologia());
        this.taDiagnostico1.setText(consulta.getDiagnostico());
        this.taPruebDiagn1.setText(consulta.getPruebasDiagnosticas());
        this.taTratamiento1.setText(consulta.getTratamiento());
        this.tfVacunas1.setText(consulta.getVacunas()); 
        this.tfDespara1.setText(consulta.getDesparasitacion());
        this.tfAlimentacion1.setText(consulta.getAlimentacionRecomendada());
        this.tfPresion1.setText(consulta.getPresionArterial());
    }
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
        setText("Añadir");
        setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }
    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private boolean clicked;
        private JTable table;

        public ButtonEditor(JCheckBox checkBox, JTable table) {
            super(checkBox);
            this.table = table;
            button = new JButton("Añadir");

            button.addActionListener(e -> {
                clicked = true;
                fireEditingStopped();
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (clicked) {
                int row = table.getSelectedRow();
                Producto prod = (Producto) table.getValueAt(row, 4);
                añadirProductoAFactura(prod);
            }
            clicked = false;
            return "Añadir";
        }

        @Override
        public boolean stopCellEditing() {
            clicked = false;
            return super.stopCellEditing();
        }
    }
    class ButtonRendererElim extends JButton implements TableCellRenderer {
        public ButtonRendererElim() {
        setText("Eliminar");
        setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }
    class ButtonEditorElim extends DefaultCellEditor {
        private JButton button;
        private boolean clicked;
        private JTable table;

        public ButtonEditorElim(JCheckBox checkBox, JTable table) {
            super(checkBox);
            this.table = table;
            button = new JButton("Eliminar️");

            button.addActionListener(e -> {
                // guarda la fila antes de que Swing finalice la edición
                int fila = table.getSelectedRow();

                // 1. forzamos que termine la edición
                fireEditingStopped();

                // 2. eliminamos la fila después
                SwingUtilities.invokeLater(() -> {
                    DefaultTableModel modelo = (DefaultTableModel) table.getModel();

                    if (fila >= 0 && fila < modelo.getRowCount()) {
                        int cantActual = (int) modelo.getValueAt(fila, 1); // columna Cant

                        if (cantActual > 1) {
                            cantActual--;
                            modelo.setValueAt(cantActual, fila, 1);

                            Producto prod = (Producto) modelo.getValueAt(fila, 4); // columna ProductoObj
                            double nuevoPrecio = cantActual * prod.getpPublico();
                            modelo.setValueAt(nuevoPrecio, fila, 2);
                        } else {
                            modelo.removeRow(fila); // elimina si cantidad llega a 0
                        }

                        actualizarTotalFactura();
                    }
                });
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            return button;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jdVentanaAñadirProd = new javax.swing.JDialog();
        tfInputIVA = new javax.swing.JTextField();
        tfInputPVP = new javax.swing.JTextField();
        tfInputPrecioC = new javax.swing.JTextField();
        tfInputNombre = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        taDescripcion = new javax.swing.JTextArea();
        tfInputCant = new javax.swing.JTextField();
        lb1 = new javax.swing.JLabel();
        lb3 = new javax.swing.JLabel();
        lb4 = new javax.swing.JLabel();
        btCrearProd = new javax.swing.JButton();
        lbCat = new javax.swing.JLabel();
        cbCategory = new javax.swing.JComboBox<>();
        btModifProd = new javax.swing.JButton();
        jdVentanaAñadirCliente = new javax.swing.JDialog();
        tfInputPostal = new javax.swing.JTextField();
        tfInputProvincia = new javax.swing.JTextField();
        tfInputDNI = new javax.swing.JTextField();
        tfInputNombreClie = new javax.swing.JTextField();
        lbPestañaCliente = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        btCrearClie = new javax.swing.JButton();
        jLabel16 = new javax.swing.JLabel();
        tfInputTelefono = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        tfInputEmail = new javax.swing.JTextField();
        jLabel28 = new javax.swing.JLabel();
        tfInputDireccion = new javax.swing.JTextField();
        jLabel29 = new javax.swing.JLabel();
        tfInputCiudad = new javax.swing.JTextField();
        btModificarClie = new javax.swing.JButton();
        jdVentanaAñadirMascota = new javax.swing.JDialog();
        tfInputMicrochip = new javax.swing.JTextField();
        tfInputPelo = new javax.swing.JTextField();
        tfInputRaza = new javax.swing.JTextField();
        tfInputNombreMascota = new javax.swing.JTextField();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        tfInputCapa = new javax.swing.JTextField();
        btCrearMascota = new javax.swing.JButton();
        lbCat2 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        cbClientes = new javax.swing.JComboBox<>();
        jLabel30 = new javax.swing.JLabel();
        tfInputEspecie = new javax.swing.JTextField();
        btModifMascota = new javax.swing.JButton();
        dcFechaNacimiento = new com.toedter.calendar.JDateChooser();
        jdAgenda = new javax.swing.JDialog();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tAgenda = new javax.swing.JTable();
        jLabel34 = new javax.swing.JLabel();
        btBorrarCita = new javax.swing.JButton();
        lbFecha = new javax.swing.JLabel();
        jdVentanaModificarCliente = new javax.swing.JDialog();
        tfInputPostal1 = new javax.swing.JTextField();
        tfInputProvincia1 = new javax.swing.JTextField();
        tfInputDNI1 = new javax.swing.JTextField();
        tfInputNombreClie1 = new javax.swing.JTextField();
        lbPestañaCliente1 = new javax.swing.JLabel();
        jLabel42 = new javax.swing.JLabel();
        jLabel43 = new javax.swing.JLabel();
        jLabel44 = new javax.swing.JLabel();
        jLabel45 = new javax.swing.JLabel();
        btModClie = new javax.swing.JButton();
        jLabel46 = new javax.swing.JLabel();
        tfInputTelefono1 = new javax.swing.JTextField();
        jLabel47 = new javax.swing.JLabel();
        tfInputEmail1 = new javax.swing.JTextField();
        jLabel48 = new javax.swing.JLabel();
        tfInputDireccion1 = new javax.swing.JTextField();
        jLabel49 = new javax.swing.JLabel();
        tfInputCiudad1 = new javax.swing.JTextField();
        jdHistorial = new javax.swing.JDialog();
        jScrollPane15 = new javax.swing.JScrollPane();
        tHistorialConsultas = new javax.swing.JTable();
        jScrollPane16 = new javax.swing.JScrollPane();
        jPanel3 = new javax.swing.JPanel();
        jLabel50 = new javax.swing.JLabel();
        lbFechaHist = new javax.swing.JLabel();
        jLabel66 = new javax.swing.JLabel();
        jLabel67 = new javax.swing.JLabel();
        jLabel68 = new javax.swing.JLabel();
        jScrollPane17 = new javax.swing.JScrollPane();
        taSintomatologia1 = new javax.swing.JTextArea();
        jLabel69 = new javax.swing.JLabel();
        jScrollPane18 = new javax.swing.JScrollPane();
        taAnamnesis1 = new javax.swing.JTextArea();
        jLabel70 = new javax.swing.JLabel();
        jScrollPane19 = new javax.swing.JScrollPane();
        taDiagnostico1 = new javax.swing.JTextArea();
        tfPesoConsul1 = new javax.swing.JTextField();
        jLabel71 = new javax.swing.JLabel();
        jScrollPane20 = new javax.swing.JScrollPane();
        taTratamiento1 = new javax.swing.JTextArea();
        jLabel72 = new javax.swing.JLabel();
        tfVacunas1 = new javax.swing.JTextField();
        tfDespara1 = new javax.swing.JTextField();
        jLabel73 = new javax.swing.JLabel();
        tfAlimentacion1 = new javax.swing.JTextField();
        jLabel74 = new javax.swing.JLabel();
        tfPresion1 = new javax.swing.JTextField();
        jLabel75 = new javax.swing.JLabel();
        tfTempConsul1 = new javax.swing.JTextField();
        jLabel76 = new javax.swing.JLabel();
        jLabel77 = new javax.swing.JLabel();
        jLabel78 = new javax.swing.JLabel();
        jLabel79 = new javax.swing.JLabel();
        jScrollPane21 = new javax.swing.JScrollPane();
        taPruebDiagn1 = new javax.swing.JTextArea();
        jLabel80 = new javax.swing.JLabel();
        lbClienteHist = new javax.swing.JLabel();
        lbMascotaHist = new javax.swing.JLabel();
        Background = new javax.swing.JPanel();
        Header = new javax.swing.JPanel();
        Menu = new javax.swing.JPanel();
        btInventario = new javax.swing.JButton();
        btClientes = new javax.swing.JButton();
        btCitas = new javax.swing.JButton();
        btFacturacion = new javax.swing.JButton();
        btInicio = new javax.swing.JButton();
        btConsultas = new javax.swing.JButton();
        pContenido = new javax.swing.JPanel();
        pFacturacion = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        tInventarioFacturacion = new javax.swing.JTable();
        jScrollPane7 = new javax.swing.JScrollPane();
        tFactura = new javax.swing.JTable();
        jLabel40 = new javax.swing.JLabel();
        tfBuscarProd = new javax.swing.JTextField();
        btFacturar = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel41 = new javax.swing.JLabel();
        lbTotal = new javax.swing.JLabel();
        btBuscarFact = new javax.swing.JButton();
        pCitas = new javax.swing.JPanel();
        cbCitas = new javax.swing.JComboBox<>();
        cbHoraCitas = new javax.swing.JComboBox<>();
        cbMinCitas = new javax.swing.JComboBox<>();
        jLabel31 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        btCrearCita = new javax.swing.JButton();
        jLabel33 = new javax.swing.JLabel();
        monthView = new org.jdesktop.swingx.JXMonthView();
        lbComprobanteCita = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        jLabel37 = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        jLabel39 = new javax.swing.JLabel();
        cbMasCita = new javax.swing.JComboBox<>();
        cbTipoCita = new javax.swing.JComboBox<>();
        jScrollPane5 = new javax.swing.JScrollPane();
        taMotivoCita = new javax.swing.JTextArea();
        dcCitas = new com.toedter.calendar.JDateChooser();
        pInventario = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        btDelProd = new javax.swing.JButton();
        btNewProd = new javax.swing.JButton();
        btSumStack = new javax.swing.JButton();
        btVentanaModProd = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tInventario = new javax.swing.JTable();
        pClientes = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        btBorrarCliente = new javax.swing.JButton();
        btPestañaCliente = new javax.swing.JButton();
        btVentanaModCliente = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        tClientes = new javax.swing.JTable();
        btNuevaMasc = new javax.swing.JButton();
        btVentanaModMascota = new javax.swing.JButton();
        btBorrarMascota = new javax.swing.JButton();
        pConsultas = new javax.swing.JPanel();
        jScrollPane8 = new javax.swing.JScrollPane();
        jPanel2 = new javax.swing.JPanel();
        cbClientesConsul = new javax.swing.JComboBox<>();
        cbMascotasConsul = new javax.swing.JComboBox<>();
        jLabel14 = new javax.swing.JLabel();
        lbFechaConsul = new javax.swing.JLabel();
        jLabel51 = new javax.swing.JLabel();
        jLabel52 = new javax.swing.JLabel();
        jLabel53 = new javax.swing.JLabel();
        jScrollPane9 = new javax.swing.JScrollPane();
        taSintomatologia = new javax.swing.JTextArea();
        jLabel54 = new javax.swing.JLabel();
        jScrollPane10 = new javax.swing.JScrollPane();
        taAnamnesis = new javax.swing.JTextArea();
        jLabel55 = new javax.swing.JLabel();
        jScrollPane12 = new javax.swing.JScrollPane();
        taDiagnostico = new javax.swing.JTextArea();
        tfPesoConsul = new javax.swing.JTextField();
        jLabel56 = new javax.swing.JLabel();
        jScrollPane13 = new javax.swing.JScrollPane();
        taTratamiento = new javax.swing.JTextArea();
        jLabel57 = new javax.swing.JLabel();
        tfVacunas = new javax.swing.JTextField();
        tfDespara = new javax.swing.JTextField();
        jLabel58 = new javax.swing.JLabel();
        tfAlimentacion = new javax.swing.JTextField();
        jLabel59 = new javax.swing.JLabel();
        tfPresion = new javax.swing.JTextField();
        jLabel60 = new javax.swing.JLabel();
        tfTempConsul = new javax.swing.JTextField();
        jLabel61 = new javax.swing.JLabel();
        jLabel62 = new javax.swing.JLabel();
        jLabel63 = new javax.swing.JLabel();
        jLabel64 = new javax.swing.JLabel();
        btGuardarConsul = new javax.swing.JButton();
        jScrollPane14 = new javax.swing.JScrollPane();
        taPruebDiagn = new javax.swing.JTextArea();
        jLabel65 = new javax.swing.JLabel();
        pInicio = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane11 = new javax.swing.JScrollPane();
        tAgendaInicio = new javax.swing.JTable();
        jLabel81 = new javax.swing.JLabel();
        btBorrarCita1 = new javax.swing.JButton();
        lbFechaInicio = new javax.swing.JLabel();

        jdVentanaAñadirProd.setTitle("Añadir Producto");
        jdVentanaAñadirProd.setAlwaysOnTop(true);
        jdVentanaAñadirProd.setModal(true);
        jdVentanaAñadirProd.setName("Añadir Producto"); // NOI18N
        jdVentanaAñadirProd.setSize(new java.awt.Dimension(639, 471));
        jdVentanaAñadirProd.setLocationRelativeTo(this);

        tfInputIVA.setPreferredSize(new java.awt.Dimension(100, 22));

        tfInputPVP.setPreferredSize(new java.awt.Dimension(100, 22));

        tfInputPrecioC.setPreferredSize(new java.awt.Dimension(100, 22));

        tfInputNombre.setPreferredSize(new java.awt.Dimension(100, 22));

        jLabel4.setFont(new java.awt.Font("Segoe UI Semibold", 0, 36)); // NOI18N
        jLabel4.setText("Añadir Nuevo Producto");

        jLabel5.setText("Nombre: ");

        jLabel6.setText("Descripción:");

        jLabel7.setText("Precio de Compra:");

        jLabel8.setText("IVA Soportado: ");

        jLabel9.setText("PVP: ");

        jLabel10.setText("Cantidad");

        taDescripcion.setColumns(20);
        taDescripcion.setRows(5);
        jScrollPane2.setViewportView(taDescripcion);

        tfInputCant.setPreferredSize(new java.awt.Dimension(100, 22));

        lb1.setText("€");

        lb3.setText("€");

        lb4.setText("%");

        btCrearProd.setText("Crear Producto");
        btCrearProd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btCrearProdActionPerformed(evt);
            }
        });

        lbCat.setText("Categoria");

        cbCategory.setModel(new DefaultComboBoxModel<Producto.CategoriaProducto>(Producto.CategoriaProducto.values()));

        btModifProd.setText("Modificar Producto");
        btModifProd.setEnabled(false);
        btModifProd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btModifProdActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jdVentanaAñadirProdLayout = new javax.swing.GroupLayout(jdVentanaAñadirProd.getContentPane());
        jdVentanaAñadirProd.getContentPane().setLayout(jdVentanaAñadirProdLayout);
        jdVentanaAñadirProdLayout.setHorizontalGroup(
            jdVentanaAñadirProdLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jdVentanaAñadirProdLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(jdVentanaAñadirProdLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jdVentanaAñadirProdLayout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jdVentanaAñadirProdLayout.createSequentialGroup()
                        .addGroup(jdVentanaAñadirProdLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jdVentanaAñadirProdLayout.createSequentialGroup()
                                .addGroup(jdVentanaAñadirProdLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lbCat)
                                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 442, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel4)
                                    .addComponent(jLabel8))
                                .addGap(18, 18, 18)
                                .addGroup(jdVentanaAñadirProdLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(btCrearProd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(btModifProd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addGroup(jdVentanaAñadirProdLayout.createSequentialGroup()
                                .addGroup(jdVentanaAñadirProdLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jdVentanaAñadirProdLayout.createSequentialGroup()
                                        .addComponent(jLabel9)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(tfInputPVP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jdVentanaAñadirProdLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(tfInputIVA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(jdVentanaAñadirProdLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addGroup(jdVentanaAñadirProdLayout.createSequentialGroup()
                                                .addComponent(jLabel7)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(tfInputPrecioC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(jdVentanaAñadirProdLayout.createSequentialGroup()
                                                .addComponent(jLabel5)
                                                .addGap(95, 95, 95)
                                                .addComponent(tfInputNombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                    .addGroup(jdVentanaAñadirProdLayout.createSequentialGroup()
                                        .addComponent(jLabel10)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(jdVentanaAñadirProdLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(cbCategory, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(tfInputCant, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jdVentanaAñadirProdLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lb3, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lb1, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lb4, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(21, 21, 21))))
        );
        jdVentanaAñadirProdLayout.setVerticalGroup(
            jdVentanaAñadirProdLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jdVentanaAñadirProdLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4)
                .addGap(18, 18, 18)
                .addGroup(jdVentanaAñadirProdLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(tfInputNombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jdVentanaAñadirProdLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(tfInputPrecioC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lb3, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jdVentanaAñadirProdLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(tfInputIVA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lb4, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jdVentanaAñadirProdLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(tfInputPVP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lb1, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jdVentanaAñadirProdLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(tfInputCant, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jdVentanaAñadirProdLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbCat)
                    .addComponent(cbCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(21, 21, 21)
                .addComponent(jLabel6)
                .addGap(18, 18, 18)
                .addGroup(jdVentanaAñadirProdLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jdVentanaAñadirProdLayout.createSequentialGroup()
                        .addComponent(btCrearProd, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btModifProd, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jdVentanaAñadirProd.getAccessibleContext().setAccessibleParent(Background);

        jdVentanaAñadirCliente.setTitle("Añadir Producto");
        jdVentanaAñadirCliente.setAlwaysOnTop(true);
        jdVentanaAñadirCliente.setModal(true);
        jdVentanaAñadirCliente.setName("Añadir Producto"); // NOI18N
        jdVentanaAñadirCliente.setSize(new java.awt.Dimension(900, 500));
        jdVentanaAñadirProd.setLocationRelativeTo(this);

        tfInputPostal.setPreferredSize(new java.awt.Dimension(100, 22));

        tfInputProvincia.setPreferredSize(new java.awt.Dimension(100, 22));

        tfInputDNI.setPreferredSize(new java.awt.Dimension(100, 22));

        tfInputNombreClie.setPreferredSize(new java.awt.Dimension(100, 22));

        lbPestañaCliente.setFont(new java.awt.Font("Segoe UI Semibold", 0, 36)); // NOI18N
        lbPestañaCliente.setText("Añadir Nuevo Cliente");

        jLabel15.setText("Nombre: ");

        jLabel17.setText("DNI/NIE");

        jLabel18.setText("cod. Postal: ");

        jLabel19.setText("Provincia:");

        btCrearClie.setText("Crear Cliente");
        btCrearClie.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btCrearClieActionPerformed(evt);
            }
        });

        jLabel16.setText("Teléfono:");

        jLabel20.setText("email:");

        jLabel28.setText("Dirección:");

        tfInputDireccion.setPreferredSize(new java.awt.Dimension(100, 22));

        jLabel29.setText("Ciudad");

        tfInputCiudad.setPreferredSize(new java.awt.Dimension(100, 22));

        btModificarClie.setText("Modificar Cliente");
        btModificarClie.setEnabled(false);
        btModificarClie.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btModificarClieActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jdVentanaAñadirClienteLayout = new javax.swing.GroupLayout(jdVentanaAñadirCliente.getContentPane());
        jdVentanaAñadirCliente.getContentPane().setLayout(jdVentanaAñadirClienteLayout);
        jdVentanaAñadirClienteLayout.setHorizontalGroup(
            jdVentanaAñadirClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jdVentanaAñadirClienteLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(jdVentanaAñadirClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jdVentanaAñadirClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jdVentanaAñadirClienteLayout.createSequentialGroup()
                            .addComponent(jLabel20)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(tfInputEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jdVentanaAñadirClienteLayout.createSequentialGroup()
                            .addGroup(jdVentanaAñadirClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel17)
                                .addComponent(jLabel15)
                                .addComponent(jLabel28))
                            .addGap(36, 36, 36)
                            .addGroup(jdVentanaAñadirClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(tfInputDNI, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(tfInputNombreClie, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(tfInputDireccion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(jdVentanaAñadirClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(tfInputPostal, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(tfInputProvincia, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(tfInputCiudad, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(tfInputTelefono, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addComponent(jLabel16)
                    .addComponent(jLabel29)
                    .addComponent(jLabel18)
                    .addComponent(jLabel19)
                    .addComponent(lbPestañaCliente))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jdVentanaAñadirClienteLayout.createSequentialGroup()
                .addGap(102, 102, 102)
                .addComponent(btModificarClie, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btCrearClie, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jdVentanaAñadirClienteLayout.setVerticalGroup(
            jdVentanaAñadirClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jdVentanaAñadirClienteLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lbPestañaCliente)
                .addGroup(jdVentanaAñadirClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jdVentanaAñadirClienteLayout.createSequentialGroup()
                        .addGroup(jdVentanaAñadirClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tfInputNombreClie, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel15))
                        .addGap(18, 18, 18)
                        .addComponent(tfInputDNI, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jdVentanaAñadirClienteLayout.createSequentialGroup()
                        .addGap(46, 46, 46)
                        .addComponent(jLabel17)))
                .addGap(18, 18, 18)
                .addGroup(jdVentanaAñadirClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel28)
                    .addComponent(tfInputDireccion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jdVentanaAñadirClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfInputPostal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel18))
                .addGap(18, 18, 18)
                .addGroup(jdVentanaAñadirClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfInputProvincia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel19))
                .addGap(18, 18, 18)
                .addGroup(jdVentanaAñadirClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel29)
                    .addComponent(tfInputCiudad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jdVentanaAñadirClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(tfInputTelefono, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jdVentanaAñadirClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel20)
                    .addComponent(tfInputEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jdVentanaAñadirClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btCrearClie, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btModificarClie, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(24, Short.MAX_VALUE))
        );

        jdVentanaAñadirMascota.setTitle("Añadir Producto");
        jdVentanaAñadirMascota.setAlwaysOnTop(true);
        jdVentanaAñadirMascota.setMinimumSize(new java.awt.Dimension(403, 533));
        jdVentanaAñadirMascota.setModal(true);
        jdVentanaAñadirMascota.setName("Añadir Producto"); // NOI18N
        jdVentanaAñadirMascota.setSize(new java.awt.Dimension(403, 533));
        jdVentanaAñadirProd.setLocationRelativeTo(this);

        tfInputMicrochip.setPreferredSize(new java.awt.Dimension(100, 22));

        tfInputPelo.setPreferredSize(new java.awt.Dimension(100, 22));

        tfInputRaza.setPreferredSize(new java.awt.Dimension(100, 22));

        tfInputNombreMascota.setPreferredSize(new java.awt.Dimension(100, 22));

        jLabel21.setFont(new java.awt.Font("Segoe UI Semibold", 0, 36)); // NOI18N
        jLabel21.setText("Añadir Nueva Mascota");

        jLabel22.setText("Nombre: ");

        jLabel24.setText("Raza:");

        jLabel25.setText("Microchip:");

        jLabel26.setText("Pelo: ");

        jLabel27.setText("Capa:");

        tfInputCapa.setPreferredSize(new java.awt.Dimension(100, 22));

        btCrearMascota.setText("Crear Mascota");
        btCrearMascota.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btCrearMascotaActionPerformed(evt);
            }
        });

        lbCat2.setText("Fecha de Nacimiento");

        jLabel23.setText("Dueño:");

        jLabel30.setText("Especie:");

        tfInputEspecie.setPreferredSize(new java.awt.Dimension(100, 22));

        btModifMascota.setText("Modificar Mascota");
        btModifMascota.setToolTipText("");
        btModifMascota.setEnabled(false);
        btModifMascota.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btModifMascotaActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jdVentanaAñadirMascotaLayout = new javax.swing.GroupLayout(jdVentanaAñadirMascota.getContentPane());
        jdVentanaAñadirMascota.getContentPane().setLayout(jdVentanaAñadirMascotaLayout);
        jdVentanaAñadirMascotaLayout.setHorizontalGroup(
            jdVentanaAñadirMascotaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jdVentanaAñadirMascotaLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(jdVentanaAñadirMascotaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jdVentanaAñadirMascotaLayout.createSequentialGroup()
                        .addGroup(jdVentanaAñadirMascotaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jdVentanaAñadirMascotaLayout.createSequentialGroup()
                                .addComponent(jLabel27)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(tfInputCapa, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jdVentanaAñadirMascotaLayout.createSequentialGroup()
                                .addComponent(jLabel26)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(tfInputPelo, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jdVentanaAñadirMascotaLayout.createSequentialGroup()
                                .addGroup(jdVentanaAñadirMascotaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jdVentanaAñadirMascotaLayout.createSequentialGroup()
                                        .addComponent(jLabel25)
                                        .addGap(63, 63, 63))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jdVentanaAñadirMascotaLayout.createSequentialGroup()
                                        .addComponent(jLabel22)
                                        .addGap(70, 70, 70))
                                    .addGroup(jdVentanaAñadirMascotaLayout.createSequentialGroup()
                                        .addGroup(jdVentanaAñadirMascotaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel24)
                                            .addComponent(jLabel30))
                                        .addGap(78, 78, 78)))
                                .addGroup(jdVentanaAñadirMascotaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jdVentanaAñadirMascotaLayout.createSequentialGroup()
                                        .addGap(1, 1, 1)
                                        .addComponent(tfInputMicrochip, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(tfInputRaza, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(tfInputEspecie, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(tfInputNombreMascota, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jdVentanaAñadirMascotaLayout.createSequentialGroup()
                        .addGroup(jdVentanaAñadirMascotaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jdVentanaAñadirMascotaLayout.createSequentialGroup()
                                .addComponent(btModifMascota, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(95, 95, 95)
                                .addComponent(btCrearMascota, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(jdVentanaAñadirMascotaLayout.createSequentialGroup()
                                .addGroup(jdVentanaAñadirMascotaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel23)
                                    .addComponent(jLabel21)
                                    .addGroup(jdVentanaAñadirMascotaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jdVentanaAñadirMascotaLayout.createSequentialGroup()
                                            .addComponent(lbCat2)
                                            .addGap(26, 26, 26)
                                            .addComponent(dcFechaNacimiento, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addComponent(cbClientes, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 267, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        jdVentanaAñadirMascotaLayout.setVerticalGroup(
            jdVentanaAñadirMascotaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jdVentanaAñadirMascotaLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel21)
                .addGap(18, 18, 18)
                .addGroup(jdVentanaAñadirMascotaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel22)
                    .addComponent(tfInputNombreMascota, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jdVentanaAñadirMascotaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel30)
                    .addComponent(tfInputEspecie, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jdVentanaAñadirMascotaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel24)
                    .addComponent(tfInputRaza, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jdVentanaAñadirMascotaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel25)
                    .addComponent(tfInputMicrochip, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jdVentanaAñadirMascotaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel26)
                    .addComponent(tfInputPelo, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jdVentanaAñadirMascotaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel27)
                    .addComponent(tfInputCapa, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jdVentanaAñadirMascotaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lbCat2)
                    .addComponent(dcFechaNacimiento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel23)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(cbClientes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jdVentanaAñadirMascotaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btModifMascota, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btCrearMascota, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(32, 32, 32))
        );

        jPanel4.setPreferredSize(new java.awt.Dimension(772, 473));

        tAgenda.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tAgenda.setShowHorizontalLines(true);
        jScrollPane4.setViewportView(tAgenda);

        jLabel34.setFont(new java.awt.Font("Segoe UI", 0, 36)); // NOI18N
        jLabel34.setText("Agenda del día:");

        btBorrarCita.setText("Eliminar");
        btBorrarCita.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btBorrarCitaActionPerformed(evt);
            }
        });

        lbFecha.setFont(new java.awt.Font("Segoe UI", 0, 36)); // NOI18N
        lbFecha.setText("xx/xx/xxxx");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(51, Short.MAX_VALUE)
                .addComponent(jLabel34)
                .addGap(34, 34, 34)
                .addComponent(lbFecha)
                .addGap(131, 131, 131)
                .addComponent(btBorrarCita, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(34, 34, 34))
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane4)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(13, 13, 13)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btBorrarCita, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbFecha, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel34, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 361, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jdAgendaLayout = new javax.swing.GroupLayout(jdAgenda.getContentPane());
        jdAgenda.getContentPane().setLayout(jdAgendaLayout);
        jdAgendaLayout.setHorizontalGroup(
            jdAgendaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jdAgendaLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jdAgendaLayout.setVerticalGroup(
            jdAgendaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jdAgendaLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jdVentanaModificarCliente.setTitle("Añadir Producto");
        jdVentanaModificarCliente.setAlwaysOnTop(true);
        jdVentanaModificarCliente.setModal(true);
        jdVentanaModificarCliente.setName("Añadir Producto"); // NOI18N
        jdVentanaModificarCliente.setSize(new java.awt.Dimension(900, 500));
        jdVentanaAñadirProd.setLocationRelativeTo(this);

        tfInputPostal1.setPreferredSize(new java.awt.Dimension(100, 22));

        tfInputProvincia1.setPreferredSize(new java.awt.Dimension(100, 22));

        tfInputDNI1.setPreferredSize(new java.awt.Dimension(100, 22));

        tfInputNombreClie1.setPreferredSize(new java.awt.Dimension(100, 22));

        lbPestañaCliente1.setFont(new java.awt.Font("Segoe UI Semibold", 0, 36)); // NOI18N
        lbPestañaCliente1.setText("Modificar Cliente");

        jLabel42.setText("Nombre: ");

        jLabel43.setText("DNI/NIE");

        jLabel44.setText("cod. Postal: ");

        jLabel45.setText("Provincia:");

        btModClie.setText("Modificar Cliente");
        btModClie.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btModClieActionPerformed(evt);
            }
        });

        jLabel46.setText("Teléfono:");

        jLabel47.setText("email:");

        jLabel48.setText("Dirección:");

        tfInputDireccion1.setPreferredSize(new java.awt.Dimension(100, 22));

        jLabel49.setText("Ciudad");

        tfInputCiudad1.setPreferredSize(new java.awt.Dimension(100, 22));

        javax.swing.GroupLayout jdVentanaModificarClienteLayout = new javax.swing.GroupLayout(jdVentanaModificarCliente.getContentPane());
        jdVentanaModificarCliente.getContentPane().setLayout(jdVentanaModificarClienteLayout);
        jdVentanaModificarClienteLayout.setHorizontalGroup(
            jdVentanaModificarClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jdVentanaModificarClienteLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(jdVentanaModificarClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel46)
                    .addComponent(jLabel49)
                    .addGroup(jdVentanaModificarClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jLabel44, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel45, javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jdVentanaModificarClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lbPestañaCliente1)
                            .addGroup(jdVentanaModificarClienteLayout.createSequentialGroup()
                                .addGroup(jdVentanaModificarClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jdVentanaModificarClienteLayout.createSequentialGroup()
                                        .addComponent(jLabel47)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(tfInputEmail1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jdVentanaModificarClienteLayout.createSequentialGroup()
                                        .addGroup(jdVentanaModificarClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel43)
                                            .addComponent(jLabel42)
                                            .addComponent(jLabel48))
                                        .addGap(36, 36, 36)
                                        .addGroup(jdVentanaModificarClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(tfInputDNI1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(tfInputNombreClie1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(tfInputDireccion1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGroup(jdVentanaModificarClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                .addComponent(tfInputPostal1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(tfInputProvincia1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(tfInputCiudad1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(tfInputTelefono1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btModClie, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jdVentanaModificarClienteLayout.setVerticalGroup(
            jdVentanaModificarClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jdVentanaModificarClienteLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lbPestañaCliente1)
                .addGroup(jdVentanaModificarClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jdVentanaModificarClienteLayout.createSequentialGroup()
                        .addGroup(jdVentanaModificarClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tfInputNombreClie1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel42))
                        .addGap(18, 18, 18)
                        .addComponent(tfInputDNI1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jdVentanaModificarClienteLayout.createSequentialGroup()
                        .addGap(46, 46, 46)
                        .addComponent(jLabel43)))
                .addGap(18, 18, 18)
                .addGroup(jdVentanaModificarClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel48)
                    .addComponent(tfInputDireccion1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jdVentanaModificarClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfInputPostal1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel44))
                .addGap(18, 18, 18)
                .addGroup(jdVentanaModificarClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfInputProvincia1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel45))
                .addGap(18, 18, 18)
                .addGroup(jdVentanaModificarClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel49)
                    .addComponent(tfInputCiudad1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jdVentanaModificarClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel46)
                    .addComponent(tfInputTelefono1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jdVentanaModificarClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel47)
                    .addComponent(tfInputEmail1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 36, Short.MAX_VALUE)
                .addComponent(btModClie, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jdHistorial.setName("Historial Consultas"); // NOI18N
        jdHistorial.setSize(new java.awt.Dimension(1250, 465));

        tHistorialConsultas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Title 1", "Title 2"
            }
        ));
        jScrollPane15.setViewportView(tHistorialConsultas);

        jLabel50.setText("Fecha de consulta:");

        lbFechaHist.setText("01/02/2000");

        jLabel66.setText("Peso:");

        jLabel67.setText("Temperatura:");

        jLabel68.setText("Anamnesis:");

        taSintomatologia1.setColumns(20);
        taSintomatologia1.setRows(5);
        jScrollPane17.setViewportView(taSintomatologia1);

        jLabel69.setText("Sintomatología:");

        taAnamnesis1.setColumns(20);
        taAnamnesis1.setRows(5);
        jScrollPane18.setViewportView(taAnamnesis1);

        jLabel70.setText("Diagnóstico:");

        taDiagnostico1.setColumns(20);
        taDiagnostico1.setRows(5);
        jScrollPane19.setViewportView(taDiagnostico1);

        jLabel71.setText("Tratamiento:");

        taTratamiento1.setColumns(20);
        taTratamiento1.setRows(5);
        jScrollPane20.setViewportView(taTratamiento1);

        jLabel72.setText("Vacunas:");

        jLabel73.setText("Desparasitación:");

        jLabel74.setText("Alimentacion Recomendada:");

        jLabel75.setText("Presión Arterial:");

        jLabel76.setText("kg");

        jLabel77.setText("ºC");

        jLabel78.setText("Cliente:");

        jLabel79.setText("Mascota:");

        taPruebDiagn1.setColumns(20);
        taPruebDiagn1.setRows(5);
        jScrollPane21.setViewportView(taPruebDiagn1);

        jLabel80.setText("Pruebas Diagnósticas:");

        lbClienteHist.setText("jLabel81");

        lbMascotaHist.setText("jLabel82");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel50, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(lbFechaHist))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jScrollPane21, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel3Layout.createSequentialGroup()
                                    .addGap(8, 8, 8)
                                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel69)
                                        .addComponent(jLabel68)
                                        .addGroup(jPanel3Layout.createSequentialGroup()
                                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(jPanel3Layout.createSequentialGroup()
                                                    .addComponent(jLabel67)
                                                    .addGap(42, 42, 42)
                                                    .addComponent(tfTempConsul1, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGroup(jPanel3Layout.createSequentialGroup()
                                                    .addComponent(jLabel66, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addGap(58, 58, 58)
                                                    .addComponent(tfPesoConsul1, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(jLabel76)
                                                .addComponent(jLabel77)))
                                        .addGroup(jPanel3Layout.createSequentialGroup()
                                            .addComponent(jLabel78)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(lbClienteHist, javax.swing.GroupLayout.PREFERRED_SIZE, 226, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(jLabel79)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(lbMascotaHist, javax.swing.GroupLayout.PREFERRED_SIZE, 310, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addComponent(jLabel70)
                                .addComponent(jLabel71)
                                .addGroup(jPanel3Layout.createSequentialGroup()
                                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel74)
                                        .addComponent(jLabel73)
                                        .addComponent(jLabel75))
                                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel3Layout.createSequentialGroup()
                                            .addGap(18, 18, 18)
                                            .addComponent(tfPresion1, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(jPanel3Layout.createSequentialGroup()
                                            .addGap(18, 18, 18)
                                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addComponent(tfAlimentacion1, javax.swing.GroupLayout.DEFAULT_SIZE, 750, Short.MAX_VALUE)
                                                .addComponent(tfDespara1)))))
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                                        .addComponent(jLabel72)
                                        .addGap(124, 124, 124)
                                        .addComponent(tfVacunas1, javax.swing.GroupLayout.DEFAULT_SIZE, 750, Short.MAX_VALUE))
                                    .addComponent(jScrollPane20, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane19, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane17, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane18, javax.swing.GroupLayout.Alignment.LEADING))
                                .addComponent(jLabel80)))
                        .addGap(0, 28, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbFechaHist, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel50, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(28, 28, 28)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel78)
                    .addComponent(jLabel79)
                    .addComponent(lbClienteHist)
                    .addComponent(lbMascotaHist))
                .addGap(28, 28, 28)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel66, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(tfPesoConsul1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel76)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel67)
                            .addComponent(tfTempConsul1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jLabel77))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel68)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane18, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel69)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane17, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(jLabel80)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane21, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel70)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane19, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel71)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane20, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel72)
                    .addComponent(tfVacunas1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfDespara1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel73))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel74)
                    .addComponent(tfAlimentacion1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel75)
                    .addComponent(tfPresion1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jScrollPane16.setViewportView(jPanel3);

        javax.swing.GroupLayout jdHistorialLayout = new javax.swing.GroupLayout(jdHistorial.getContentPane());
        jdHistorial.getContentPane().setLayout(jdHistorialLayout);
        jdHistorialLayout.setHorizontalGroup(
            jdHistorialLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jdHistorialLayout.createSequentialGroup()
                .addComponent(jScrollPane15, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane16, javax.swing.GroupLayout.DEFAULT_SIZE, 971, Short.MAX_VALUE))
        );
        jdHistorialLayout.setVerticalGroup(
            jdHistorialLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane15, javax.swing.GroupLayout.DEFAULT_SIZE, 465, Short.MAX_VALUE)
            .addComponent(jScrollPane16, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        Background.setBackground(new java.awt.Color(255, 255, 255));

        Header.setBackground(new java.awt.Color(0, 153, 153));
        Header.setPreferredSize(new java.awt.Dimension(675, 100));

        javax.swing.GroupLayout HeaderLayout = new javax.swing.GroupLayout(Header);
        Header.setLayout(HeaderLayout);
        HeaderLayout.setHorizontalGroup(
            HeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        HeaderLayout.setVerticalGroup(
            HeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        Menu.setBackground(new java.awt.Color(0, 102, 102));

        btInventario.setBackground(new java.awt.Color(0, 102, 102));
        btInventario.setText("Inventario");
        btInventario.setBorder(null);
        btInventario.setBorderPainted(false);
        btInventario.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btInventarioActionPerformed(evt);
            }
        });

        btClientes.setBackground(new java.awt.Color(0, 102, 102));
        btClientes.setText("Clientes");
        btClientes.setBorder(null);
        btClientes.setBorderPainted(false);
        btClientes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btClientesActionPerformed(evt);
            }
        });

        btCitas.setBackground(new java.awt.Color(0, 102, 102));
        btCitas.setText("Citas");
        btCitas.setBorder(null);
        btCitas.setBorderPainted(false);
        btCitas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btCitasActionPerformed(evt);
            }
        });

        btFacturacion.setBackground(new java.awt.Color(0, 102, 102));
        btFacturacion.setText("Facturación");
        btFacturacion.setBorder(null);
        btFacturacion.setBorderPainted(false);
        btFacturacion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btFacturacionActionPerformed(evt);
            }
        });

        btInicio.setBackground(new java.awt.Color(0, 102, 102));
        btInicio.setText("Inicio");
        btInicio.setBorder(null);
        btInicio.setBorderPainted(false);
        btInicio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btInicioActionPerformed(evt);
            }
        });

        btConsultas.setBackground(new java.awt.Color(0, 102, 102));
        btConsultas.setText("Consulta");
        btConsultas.setBorder(null);
        btConsultas.setBorderPainted(false);
        btConsultas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btConsultasActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout MenuLayout = new javax.swing.GroupLayout(Menu);
        Menu.setLayout(MenuLayout);
        MenuLayout.setHorizontalGroup(
            MenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btInventario, javax.swing.GroupLayout.DEFAULT_SIZE, 254, Short.MAX_VALUE)
            .addComponent(btClientes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(btCitas, javax.swing.GroupLayout.DEFAULT_SIZE, 254, Short.MAX_VALUE)
            .addComponent(btFacturacion, javax.swing.GroupLayout.DEFAULT_SIZE, 254, Short.MAX_VALUE)
            .addComponent(btInicio, javax.swing.GroupLayout.DEFAULT_SIZE, 254, Short.MAX_VALUE)
            .addComponent(btConsultas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        MenuLayout.setVerticalGroup(
            MenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(MenuLayout.createSequentialGroup()
                .addComponent(btInicio, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(btConsultas, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(btClientes, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(btInventario, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(btCitas, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(btFacturacion, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pContenido.setBackground(new java.awt.Color(51, 51, 51));
        pContenido.setLayout(new java.awt.CardLayout());

        pFacturacion.setBackground(new java.awt.Color(204, 204, 204));
        pFacturacion.setPreferredSize(new java.awt.Dimension(975, 560));

        tInventarioFacturacion.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Producto", "Precio", "Stock", "Acción"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane6.setViewportView(tInventarioFacturacion);

        tFactura.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Producto", "Cant", "Precio", "Acción"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Integer.class, java.lang.Long.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane7.setViewportView(tFactura);

        jLabel40.setFont(new java.awt.Font("Segoe UI", 0, 36)); // NOI18N
        jLabel40.setText("Productos del inventario:");

        tfBuscarProd.setToolTipText("");

        btFacturar.setText("Facturar");
        btFacturar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btFacturarActionPerformed(evt);
            }
        });

        jLabel41.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel41.setText("Total:");

        lbTotal.setText("00.00 €");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel41, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 127, Short.MAX_VALUE)
                .addComponent(lbTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel41, javax.swing.GroupLayout.DEFAULT_SIZE, 37, Short.MAX_VALUE)
            .addComponent(lbTotal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        btBuscarFact.setText("Buscar");
        btBuscarFact.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btBuscarFactActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pFacturacionLayout = new javax.swing.GroupLayout(pFacturacion);
        pFacturacion.setLayout(pFacturacionLayout);
        pFacturacionLayout.setHorizontalGroup(
            pFacturacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pFacturacionLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pFacturacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pFacturacionLayout.createSequentialGroup()
                        .addGroup(pFacturacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel40, javax.swing.GroupLayout.PREFERRED_SIZE, 452, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(pFacturacionLayout.createSequentialGroup()
                                .addComponent(tfBuscarProd, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btBuscarFact, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 211, Short.MAX_VALUE))
                    .addGroup(pFacturacionLayout.createSequentialGroup()
                        .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 651, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGroup(pFacturacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(pFacturacionLayout.createSequentialGroup()
                        .addGap(120, 120, 120)
                        .addComponent(btFacturar, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pFacturacionLayout.setVerticalGroup(
            pFacturacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pFacturacionLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 436, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27)
                .addComponent(btFacturar, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pFacturacionLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel40, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pFacturacionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfBuscarProd, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btBuscarFact, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 399, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pContenido.add(pFacturacion, "card2");

        pCitas.setBackground(new java.awt.Color(204, 204, 204));
        pCitas.setPreferredSize(new java.awt.Dimension(908, 560));

        cbCitas.setEditable(true);

        cbHoraCitas.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24" }));
        cbHoraCitas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbHoraCitasActionPerformed(evt);
            }
        });

        cbMinCitas.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "00", "05", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55" }));

        jLabel31.setText("H :");

        jLabel32.setText("Min");

        btCrearCita.setText("Crear Cita");
        btCrearCita.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btCrearCitaActionPerformed(evt);
            }
        });

        jLabel33.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jLabel33.setText("Citas del Mes");

        monthView.setFocusable(false);
        monthView.setFont(new java.awt.Font("Segoe UI", 0, 30)); // NOI18N
        monthView.setTraversable(true);
        monthView.setZoomable(true);

        jLabel35.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel35.setText("Cliente:");

        jLabel36.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel36.setText("Fecha:");

        jLabel37.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel37.setText("Mascota:");

        jLabel38.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel38.setText("Motivo:");

        jLabel39.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel39.setText("Tipo:");

        cbMasCita.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                cbMasCitaPopupMenuWillBecomeVisible(evt);
            }
        });

        taMotivoCita.setColumns(20);
        taMotivoCita.setRows(5);
        jScrollPane5.setViewportView(taMotivoCita);

        javax.swing.GroupLayout pCitasLayout = new javax.swing.GroupLayout(pCitas);
        pCitas.setLayout(pCitasLayout);
        pCitasLayout.setHorizontalGroup(
            pCitasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pCitasLayout.createSequentialGroup()
                .addGroup(pCitasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pCitasLayout.createSequentialGroup()
                        .addGroup(pCitasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(monthView, javax.swing.GroupLayout.PREFERRED_SIZE, 487, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(pCitasLayout.createSequentialGroup()
                                .addGap(179, 179, 179)
                                .addComponent(jLabel33)))
                        .addGap(18, 18, 18)
                        .addGroup(pCitasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel35)
                            .addComponent(jLabel36)
                            .addComponent(jLabel37)
                            .addComponent(jLabel38)
                            .addComponent(jLabel39)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pCitasLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(lbComprobanteCita, javax.swing.GroupLayout.PREFERRED_SIZE, 241, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(19, 19, 19)))
                .addGroup(pCitasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(cbMasCita, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE)
                    .addComponent(btCrearCita, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cbTipoCita, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cbCitas, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pCitasLayout.createSequentialGroup()
                        .addComponent(dcCitas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(cbHoraCitas, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel31)
                        .addGap(25, 25, 25)
                        .addComponent(cbMinCitas, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel32)))
                .addGap(18, 18, 18))
        );
        pCitasLayout.setVerticalGroup(
            pCitasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pCitasLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel33, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(pCitasLayout.createSequentialGroup()
                .addGap(39, 39, 39)
                .addGroup(pCitasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbCitas, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel35))
                .addGroup(pCitasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pCitasLayout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addGroup(pCitasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel37)
                            .addComponent(cbMasCita, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(111, 111, 111)
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(82, 82, 82)
                        .addComponent(btCrearCita, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pCitasLayout.createSequentialGroup()
                        .addGroup(pCitasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pCitasLayout.createSequentialGroup()
                                .addGap(24, 24, 24)
                                .addComponent(monthView, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(pCitasLayout.createSequentialGroup()
                                .addGap(121, 121, 121)
                                .addGroup(pCitasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(pCitasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(cbMinCitas)
                                        .addComponent(cbHoraCitas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel32)
                                        .addComponent(jLabel31))
                                    .addGroup(pCitasLayout.createSequentialGroup()
                                        .addGroup(pCitasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(dcCitas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel36))
                                        .addGap(62, 62, 62)
                                        .addComponent(jLabel38)
                                        .addGap(80, 80, 80)
                                        .addGroup(pCitasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(jLabel39)
                                            .addComponent(cbTipoCita, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 75, Short.MAX_VALUE)
                        .addComponent(lbComprobanteCita, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(24, 24, 24))
        );

        pContenido.add(pCitas, "card2");

        pInventario.setBackground(new java.awt.Color(204, 204, 204));

        jLabel3.setFont(new java.awt.Font("Segoe UI Semibold", 0, 48)); // NOI18N
        jLabel3.setText("Inventario");

        btDelProd.setText("Borrar Producto");
        btDelProd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btDelProdActionPerformed(evt);
            }
        });

        btNewProd.setText("Nuevo Producto");
        btNewProd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btNewProdActionPerformed(evt);
            }
        });

        btSumStack.setText("Añadir Stock");
        btSumStack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btSumStackActionPerformed(evt);
            }
        });

        btVentanaModProd.setText("Modificar Producto");
        btVentanaModProd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btVentanaModProdActionPerformed(evt);
            }
        });

        tInventario.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4", "Title 5", "Title 6"
            }
        ));
        jScrollPane1.setViewportView(tInventario);

        javax.swing.GroupLayout pInventarioLayout = new javax.swing.GroupLayout(pInventario);
        pInventario.setLayout(pInventarioLayout);
        pInventarioLayout.setHorizontalGroup(
            pInventarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pInventarioLayout.createSequentialGroup()
                .addGroup(pInventarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pInventarioLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 764, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(26, 26, 26)
                        .addGroup(pInventarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btDelProd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btNewProd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btSumStack, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btVentanaModProd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(pInventarioLayout.createSequentialGroup()
                        .addGap(360, 360, 360)
                        .addComponent(jLabel3)))
                .addGap(12, 12, 12))
        );
        pInventarioLayout.setVerticalGroup(
            pInventarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pInventarioLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(pInventarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pInventarioLayout.createSequentialGroup()
                        .addGap(80, 80, 80)
                        .addComponent(btNewProd, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btDelProd, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btSumStack, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btVentanaModProd, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pInventarioLayout.createSequentialGroup()
                        .addGap(68, 68, 68)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 373, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(50, Short.MAX_VALUE))
        );

        pContenido.add(pInventario, "card2");

        pClientes.setBackground(new java.awt.Color(204, 204, 204));
        pClientes.setPreferredSize(new java.awt.Dimension(969, 560));

        jLabel13.setFont(new java.awt.Font("Segoe UI Semibold", 0, 48)); // NOI18N
        jLabel13.setText("Clientes");

        btBorrarCliente.setText("Borrar Cliente");
        btBorrarCliente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btBorrarClienteActionPerformed(evt);
            }
        });

        btPestañaCliente.setText("Nuevo Cliente");
        btPestañaCliente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btPestañaClienteActionPerformed(evt);
            }
        });

        btVentanaModCliente.setText("Modificar Cliente");
        btVentanaModCliente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btVentanaModClienteActionPerformed(evt);
            }
        });

        tClientes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4", "Title 5", "Title 6"
            }
        ));
        tClientes.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tClientesMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(tClientes);

        btNuevaMasc.setText("Nueva Mascota");
        btNuevaMasc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btNuevaMascActionPerformed(evt);
            }
        });

        btVentanaModMascota.setText("Modificar Mascota");
        btVentanaModMascota.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btVentanaModMascotaActionPerformed(evt);
            }
        });

        btBorrarMascota.setText("Borrar Mascota");
        btBorrarMascota.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btBorrarMascotaActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pClientesLayout = new javax.swing.GroupLayout(pClientes);
        pClientes.setLayout(pClientesLayout);
        pClientesLayout.setHorizontalGroup(
            pClientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pClientesLayout.createSequentialGroup()
                .addGroup(pClientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pClientesLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 764, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(26, 26, 26)
                        .addGroup(pClientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btBorrarCliente, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btPestañaCliente, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btVentanaModCliente, javax.swing.GroupLayout.DEFAULT_SIZE, 155, Short.MAX_VALUE)
                            .addComponent(btBorrarMascota, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btVentanaModMascota, javax.swing.GroupLayout.DEFAULT_SIZE, 155, Short.MAX_VALUE)
                            .addComponent(btNuevaMasc, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(pClientesLayout.createSequentialGroup()
                        .addGap(360, 360, 360)
                        .addComponent(jLabel13)))
                .addGap(12, 12, 12))
        );
        pClientesLayout.setVerticalGroup(
            pClientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pClientesLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(68, 68, 68)
                .addGroup(pClientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(pClientesLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(btPestañaCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btBorrarCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btVentanaModCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btNuevaMasc, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(btBorrarMascota, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btVentanaModMascota, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap(33, Short.MAX_VALUE))
        );

        pContenido.add(pClientes, "card2");

        pConsultas.setBackground(new java.awt.Color(204, 204, 204));

        jScrollPane8.setPreferredSize(new java.awt.Dimension(1100, 931));

        cbMascotasConsul.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                cbMascotasConsulPopupMenuWillBecomeVisible(evt);
            }
        });

        jLabel14.setText("Fecha de consulta:");

        lbFechaConsul.setText("01/02/2000");

        jLabel51.setText("Peso:");

        jLabel52.setText("Temperatura:");

        jLabel53.setText("Anamnesis:");

        taSintomatologia.setColumns(20);
        taSintomatologia.setRows(5);
        jScrollPane9.setViewportView(taSintomatologia);

        jLabel54.setText("Sintomatología:");

        taAnamnesis.setColumns(20);
        taAnamnesis.setRows(5);
        jScrollPane10.setViewportView(taAnamnesis);

        jLabel55.setText("Diagnóstico:");

        taDiagnostico.setColumns(20);
        taDiagnostico.setRows(5);
        jScrollPane12.setViewportView(taDiagnostico);

        jLabel56.setText("Tratamiento:");

        taTratamiento.setColumns(20);
        taTratamiento.setRows(5);
        jScrollPane13.setViewportView(taTratamiento);

        jLabel57.setText("Vacunas:");

        jLabel58.setText("Desparasitación:");

        jLabel59.setText("Alimentacion Recomendada:");

        jLabel60.setText("Presión Arterial:");

        jLabel61.setText("kg");

        jLabel62.setText("ºC");

        jLabel63.setText("Clientes:");

        jLabel64.setText("Mascotas:");

        btGuardarConsul.setText("Guardar");
        btGuardarConsul.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btGuardarConsulActionPerformed(evt);
            }
        });

        taPruebDiagn.setColumns(20);
        taPruebDiagn.setRows(5);
        jScrollPane14.setViewportView(taPruebDiagn);

        jLabel65.setText("Pruebas Diagnósticas:");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btGuardarConsul, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(lbFechaConsul))))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jScrollPane14, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel2Layout.createSequentialGroup()
                                    .addGap(8, 8, 8)
                                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel54)
                                        .addComponent(jLabel53)
                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(jPanel2Layout.createSequentialGroup()
                                                    .addComponent(jLabel52)
                                                    .addGap(42, 42, 42)
                                                    .addComponent(tfTempConsul, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGroup(jPanel2Layout.createSequentialGroup()
                                                    .addComponent(jLabel51, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addGap(58, 58, 58)
                                                    .addComponent(tfPesoConsul, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(jLabel61)
                                                .addComponent(jLabel62)))
                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(cbClientesConsul, javax.swing.GroupLayout.PREFERRED_SIZE, 213, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(jLabel63))
                                            .addGap(26, 26, 26)
                                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(jLabel64)
                                                .addComponent(cbMascotasConsul, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                                .addComponent(jLabel55)
                                .addComponent(jLabel56)
                                .addGroup(jPanel2Layout.createSequentialGroup()
                                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel59)
                                        .addComponent(jLabel58)
                                        .addComponent(jLabel60))
                                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                            .addGap(18, 18, 18)
                                            .addComponent(tfPresion, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                            .addGap(18, 18, 18)
                                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addComponent(tfAlimentacion, javax.swing.GroupLayout.DEFAULT_SIZE, 750, Short.MAX_VALUE)
                                                .addComponent(tfDespara)))))
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                                        .addComponent(jLabel57)
                                        .addGap(124, 124, 124)
                                        .addComponent(tfVacunas, javax.swing.GroupLayout.DEFAULT_SIZE, 750, Short.MAX_VALUE))
                                    .addComponent(jScrollPane13, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane12, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane9, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane10, javax.swing.GroupLayout.Alignment.LEADING))
                                .addComponent(jLabel65)))
                        .addGap(0, 24, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbFechaConsul, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel63)
                    .addComponent(jLabel64))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbMascotasConsul, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cbClientesConsul, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel51, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(tfPesoConsul, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel61)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel52)
                            .addComponent(tfTempConsul, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jLabel62))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel53)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel54)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(jLabel65)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane14, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel55)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane12, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel56)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane13, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel57)
                    .addComponent(tfVacunas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfDespara, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel58))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel59)
                    .addComponent(tfAlimentacion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel60)
                    .addComponent(tfPresion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btGuardarConsul, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jScrollPane8.setViewportView(jPanel2);

        javax.swing.GroupLayout pConsultasLayout = new javax.swing.GroupLayout(pConsultas);
        pConsultas.setLayout(pConsultasLayout);
        pConsultasLayout.setHorizontalGroup(
            pConsultasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane8, javax.swing.GroupLayout.DEFAULT_SIZE, 969, Short.MAX_VALUE)
        );
        pConsultasLayout.setVerticalGroup(
            pConsultasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane8, javax.swing.GroupLayout.DEFAULT_SIZE, 560, Short.MAX_VALUE)
        );

        pContenido.add(pConsultas, "card2");

        pInicio.setBackground(new java.awt.Color(204, 204, 204));

        jPanel5.setPreferredSize(new java.awt.Dimension(772, 473));

        tAgendaInicio.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tAgendaInicio.setShowHorizontalLines(true);
        jScrollPane11.setViewportView(tAgendaInicio);

        jLabel81.setFont(new java.awt.Font("Segoe UI", 0, 36)); // NOI18N
        jLabel81.setText("Agenda del día:");

        btBorrarCita1.setText("Eliminar");
        btBorrarCita1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btBorrarCita1ActionPerformed(evt);
            }
        });

        lbFechaInicio.setFont(new java.awt.Font("Segoe UI", 0, 36)); // NOI18N
        lbFechaInicio.setText("xx/xx/xxxx");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addComponent(jLabel81)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lbFechaInicio)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 335, Short.MAX_VALUE)
                .addComponent(btBorrarCita1, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(68, 68, 68))
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane11)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel81, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbFechaInicio, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btBorrarCita1, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane11, javax.swing.GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout pInicioLayout = new javax.swing.GroupLayout(pInicio);
        pInicio.setLayout(pInicioLayout);
        pInicioLayout.setHorizontalGroup(
            pInicioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, 969, Short.MAX_VALUE)
        );
        pInicioLayout.setVerticalGroup(
            pInicioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, 560, Short.MAX_VALUE)
        );

        pContenido.add(pInicio, "card2");

        javax.swing.GroupLayout BackgroundLayout = new javax.swing.GroupLayout(Background);
        Background.setLayout(BackgroundLayout);
        BackgroundLayout.setHorizontalGroup(
            BackgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(BackgroundLayout.createSequentialGroup()
                .addComponent(Menu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(BackgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(Header, javax.swing.GroupLayout.DEFAULT_SIZE, 981, Short.MAX_VALUE)
                    .addGroup(BackgroundLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(pContenido, javax.swing.GroupLayout.DEFAULT_SIZE, 969, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        BackgroundLayout.setVerticalGroup(
            BackgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(BackgroundLayout.createSequentialGroup()
                .addGroup(BackgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(BackgroundLayout.createSequentialGroup()
                        .addComponent(Header, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pContenido, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(Menu, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(Background, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(Background, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btInventarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btInventarioActionPerformed
        // TODO add your handling code here:
        cargarProdInv();
        CardLayout cl = (CardLayout)(pContenido.getLayout());
        cl.show(pContenido, "pInventario");
    }//GEN-LAST:event_btInventarioActionPerformed

    private void btCitasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCitasActionPerformed
        // TODO add your handling code here:
        listaClientes = bd.ConsultarTodosClientes();
        CardLayout cl = (CardLayout)(pContenido.getLayout());
        cl.show(pContenido, "pCitas");
    }//GEN-LAST:event_btCitasActionPerformed

    private void btFacturacionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btFacturacionActionPerformed
        // TODO add your handling code here:
        CardLayout cl = (CardLayout)(pContenido.getLayout());
        cl.show(pContenido, "pFacturacion");
        cargarProdFact();
    }//GEN-LAST:event_btFacturacionActionPerformed

    private void btInicioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btInicioActionPerformed
        // TODO add your handling code here:
        cargarAgendaInicio();
        CardLayout cl = (CardLayout)(pContenido.getLayout());
        cl.show(pContenido, "pInicio");
    }//GEN-LAST:event_btInicioActionPerformed

    private void btClientesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btClientesActionPerformed
        // TODO add your handling code here:
        cargarMascotasInv();
        CardLayout cl = (CardLayout)(pContenido.getLayout());
        cl.show(pContenido, "pClientes");
    }//GEN-LAST:event_btClientesActionPerformed

    private void btConsultasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btConsultasActionPerformed
        // TODO add your handling code here:
        Date fecha = new Date();
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
        String texto = formato.format(fecha);
        this.lbFechaConsul.setText(texto);
        cargarClientesConsultas();
        CardLayout cl = (CardLayout)(pContenido.getLayout());
        cl.show(pContenido, "pConsultas");
    }//GEN-LAST:event_btConsultasActionPerformed
    //Botones del panel Inventario
    private void btNewProdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btNewProdActionPerformed
        // TODO add your handling code here:
        //Vaciamos las casillas
        tfInputNombre.setText("");
        tfInputPrecioC.setText("");
        tfInputPVP.setText("");
        tfInputCant.setText("");
        cbCategory.setSelectedItem(null);
        
        this.btCrearProd.setEnabled(true);
        this.btModifProd.setEnabled(false);
        jdVentanaAñadirProd.setVisible(true);
    }//GEN-LAST:event_btNewProdActionPerformed

    private void btDelProdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btDelProdActionPerformed
        // TODO add your handling code here:
        int fila = tInventario.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto para borrar.");
            return;
        }

        Producto prod = (Producto) tInventario.getValueAt(fila, 5); // ProductoObj
        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Seguro que quieres eliminar el producto «" + prod.getNombre() + "»?",
            "Confirmar borrado", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        if (bd.borrarProducto(prod)) {
            cargarProdInv(); // Refresca la tabla
            JOptionPane.showMessageDialog(this, "Producto eliminado correctamente.");
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo eliminar el producto.");
        }
    }//GEN-LAST:event_btDelProdActionPerformed

    private void btSumStackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btSumStackActionPerformed
        // TODO add your handling code here:
         int fila = tInventario.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto primero.");
            return;
        }

        Producto prod = (Producto) tInventario.getValueAt(fila, 5);

        String input = JOptionPane.showInputDialog(this,
            "¿Cuánto stock quieres añadir a «" + prod.getNombre() + "»?",
            "Añadir stock", JOptionPane.QUESTION_MESSAGE);

        if (input == null) return; // Cancelado

        try {
            int cantidad = Integer.parseInt(input);
            if (cantidad <= 0) throw new NumberFormatException();

            if (bd.añadirStock(prod, cantidad)) {
                cargarProdInv(); // Refresca tabla
                JOptionPane.showMessageDialog(this, "Stock actualizado correctamente.");
            } else {
                JOptionPane.showMessageDialog(this, "Error al actualizar stock.");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Introduce una cantidad válida.");
        }
    }//GEN-LAST:event_btSumStackActionPerformed

    private void btVentanaModProdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btVentanaModProdActionPerformed
        // TODO add your handling code here:
        int fila = tInventario.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto para modificar.");
            return;
        }

        Producto prod = (Producto) tInventario.getValueAt(fila, 5);

        // Cargar en formulario
        tfInputNombre.setText(prod.getNombre());
        tfInputPrecioC.setText(String.valueOf(prod.getpCompra()));
        tfInputPVP.setText(String.valueOf(prod.getpPublico()));
        tfInputCant.setText(String.valueOf(prod.getCant()));
        cbCategory.setSelectedItem(prod.getCatProd());
    
        this.btCrearProd.setEnabled(false);
        this.btModifProd.setEnabled(true);
        jdVentanaAñadirProd.setVisible(true);
    }//GEN-LAST:event_btVentanaModProdActionPerformed

    private void btCrearProdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCrearProdActionPerformed
        // TODO add your handling code here:
        try{
            Producto prod = new Producto(this.tfInputNombre.getText(), this.taDescripcion.getText(), Integer.parseInt(this.tfInputCant.getText()), 
                Double.parseDouble(this.tfInputPrecioC.getText()), Double.parseDouble(this.tfInputPVP.getText()), (CategoriaProducto) cbCategory.getSelectedItem(),
                        Double.parseDouble(this.tfInputIVA.getText()));
            bd.CrearProd(prod);
            jdVentanaAñadirProd.setVisible(false);
            cargarProdInv();
            JOptionPane.showMessageDialog(jdVentanaAñadirProd, "Producto creado con éxito.");
        }catch (Exception e) {
            JOptionPane.showMessageDialog(jdVentanaAñadirProd, "Error al introducir los datos del Cliente");
            System.out.println("error" + e);
        }
    }//GEN-LAST:event_btCrearProdActionPerformed

    private void btBorrarClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btBorrarClienteActionPerformed
        // TODO add your handling code here:
        int fila = tClientes.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this,
                "Selecciona un cliente primero.", "Aviso",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        Cliente cli = (Cliente) tClientes.getValueAt(fila, 4);

        int opc = JOptionPane.showConfirmDialog(
            this,
            "¿Seguro que quieres eliminar a «" + cli.getNombre() + "»?",
            "Confirmar borrado",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);

        if (opc != JOptionPane.YES_OPTION) return;

        // 1. Borrar en BD
        if (bd.borrarCliente(cli)) {
            // 2. Quitar de la tabla
            cargarMascotasInv();
            JOptionPane.showMessageDialog(this,
                "Cliente eliminado correctamente.",
                "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                "No se pudo eliminar el cliente.",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btBorrarClienteActionPerformed

    private void btPestañaClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btPestañaClienteActionPerformed
        // TODO add your handling code here:
        //Aqui borramos todos los campos
        tfInputNombreClie.setText("");
        tfInputTelefono.setText("");
        tfInputEmail.setText("");
        tfInputDireccion.setText("");
        tfInputDNI.setText("");
        tfInputCiudad.setText("");
        tfInputProvincia.setText("");
        tfInputPostal.setText("");
        
        this.btCrearClie.setEnabled(true);
        this.btModClie.setEnabled(false);
        this.cbClientes.setEnabled(true);
        cargarCBClientes(cbClientes);
        jdVentanaAñadirCliente.setVisible(true);
    }//GEN-LAST:event_btPestañaClienteActionPerformed

    private void btVentanaModClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btVentanaModClienteActionPerformed
        // TODO add your handling code here:
        int fila = tClientes.getSelectedRow();

        if (fila == -1) {
            JOptionPane.showMessageDialog(this,
                "Selecciona un cliente para modificar.",
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Cliente cliente = (Cliente) tClientes.getValueAt(fila, 4); // Columna 4 = Dueño
        
        // Cargamos los datos en los campos de jdVentanaAñadirCliente
        tfInputNombreClie.setText(cliente.getNombre());
        tfInputTelefono.setText(cliente.getTelefono());
        tfInputEmail.setText(cliente.getEmail());
        tfInputDireccion.setText(cliente.getDireccion());
        tfInputDNI.setText(cliente.getDni());
        tfInputCiudad.setText(cliente.getCiudad());
        tfInputProvincia.setText(cliente.getProv());
        tfInputPostal.setText(cliente.getCodP());
        
        this.btCrearClie.setEnabled(false);
        this.btModificarClie.setEnabled(true);
        jdVentanaAñadirCliente.setVisible(true);
    }//GEN-LAST:event_btVentanaModClienteActionPerformed

    private void btCrearClieActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCrearClieActionPerformed
        // TODO add your handling code here:
        try{
            Cliente clie = new Cliente(this.tfInputNombreClie.getText(), this.tfInputDNI.getText(), this.tfInputDireccion.getText(), 
                this.tfInputPostal.getText(), this.tfInputProvincia.getText(), this.tfInputCiudad.getText(), this.tfInputTelefono.getText(),
                this.tfInputEmail.getText());
            bd.CrearCliente(clie);
            cargarMascotasInv();
            jdVentanaAñadirCliente.setVisible(false);
            JOptionPane.showMessageDialog(jdVentanaAñadirMascota, "Cliente creado con Éxito");
            cargarCBClientes(cbClientes);
            jdVentanaAñadirMascota.setVisible(true);
        }catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al introducir los datos del Cliente");
            System.out.println("error" + e);
        }
        jdVentanaAñadirCliente.setVisible(false);
    }//GEN-LAST:event_btCrearClieActionPerformed

    private void btCrearMascotaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCrearMascotaActionPerformed
        // TODO add your handling code here:
        Date fechaNacimiento = this.dcFechaNacimiento.getDate();
        Cliente clienteSeleccionado = (Cliente) cbClientes.getSelectedItem();

        try{
            Mascota masc = new Mascota(this.tfInputNombreMascota.getText(), this.tfInputEspecie.getText(), this.tfInputRaza.getText(), 
                this.tfInputMicrochip.getText(), this.tfInputPelo.getText(), this.tfInputCapa.getText(), fechaNacimiento);
            bd.CrearMascota(masc, clienteSeleccionado);
            JOptionPane.showMessageDialog(jdVentanaAñadirMascota, "Mascota creada con Éxito");
        }catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al introducir los datos del Cliente");
            System.out.println("error" + e);
        }
        jdVentanaAñadirMascota.setVisible(false);
        cargarMascotasInv();
    }//GEN-LAST:event_btCrearMascotaActionPerformed

    private void btNuevaMascActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btNuevaMascActionPerformed
        // TODO add your handling code here:
        //Aqui vaciamos los campos
        tfInputNombreMascota.setText("");
        tfInputRaza.setText("");
        tfInputEspecie.setText("");
        tfInputMicrochip.setText("");
        tfInputCapa.setText("");
        tfInputPelo.setText("");
        dcFechaNacimiento.setDate(null);
        
        this.btCrearMascota.setEnabled(true);
        this.btModifMascota.setEnabled(false);
        this.cbClientes.setEnabled(true);
        cargarCBClientes(cbClientes);
        jdVentanaAñadirMascota.setVisible(true);
    }//GEN-LAST:event_btNuevaMascActionPerformed

    private void btCrearCitaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCrearCitaActionPerformed
        // TODO add your handling code here:
        Date fechaSinHora = dcCitas.getDate();
        int hora = Integer.parseInt(cbHoraCitas.getSelectedItem().toString());
        int minuto = Integer.parseInt(cbMinCitas.getSelectedItem().toString());

        Calendar cal = Calendar.getInstance();
        cal.setTime(fechaSinHora);
        cal.set(Calendar.HOUR_OF_DAY, hora);
        cal.set(Calendar.MINUTE, minuto);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        Date fechaCita = cal.getTime();
        Cita cita;
        cita = new Cita(
                fechaCita, 
                (Mascota) this.cbMasCita.getSelectedItem(), 
                (Cliente) this.cbCitas.getSelectedItem(), 
                this.taMotivoCita.getText(), 
                (TipoCita) this.cbTipoCita.getSelectedItem()
        );
        String text = bd.CrearCita(cita);
        this.lbComprobanteCita.setText(text);
        JOptionPane.showMessageDialog(this, text);
        
        dcCitas.setDate(null);
        cbHoraCitas.setSelectedItem(null);
        cbMinCitas.setSelectedItem(null);
        this.cbMasCita.setSelectedItem(null); 
        this.cbCitas.setSelectedItem(null);
        this.taMotivoCita.setText(""); 
        this.cbTipoCita.setSelectedItem(null);
        
        listaClientes = bd.ConsultarTodosClientes();
        CardLayout cl = (CardLayout)(pContenido.getLayout());
        cl.show(pContenido, "pCitas");
    }//GEN-LAST:event_btCrearCitaActionPerformed

    private void btBorrarCitaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btBorrarCitaActionPerformed
        // TODO add your handling code here:
        int fila = tAgenda.getSelectedRow();

        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona una cita para eliminar.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Estás seguro de que quieres eliminar esta cita?",
            "Confirmar eliminación",
            JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        // Obtener el objeto Cita desde la columna 5
        Cita cita = (Cita) tAgenda.getValueAt(fila, 5);

        boolean borrado = bd.borrarCita(cita);

        if (borrado) {
            ((DefaultTableModel) tAgenda.getModel()).removeRow(fila);
            cargarAgendaInicio();
            JOptionPane.showMessageDialog(this, "Cita eliminada correctamente.");
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo eliminar la cita.");
        }
    }//GEN-LAST:event_btBorrarCitaActionPerformed

    private void cbHoraCitasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbHoraCitasActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbHoraCitasActionPerformed

    private void btFacturarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btFacturarActionPerformed
        // TODO add your handling code here:
        JOptionPane.showMessageDialog(this, "Se ha facturado un importe de: " + this.lbTotal.getText());
        DefaultTableModel modelo = (DefaultTableModel) tFactura.getModel();
        List<Producto> productosActualizados = new ArrayList<>();

        // 1. Recorremos la tabla
        for (int i = 0; i < modelo.getRowCount(); i++) {
            int cantidadVendida = (int) modelo.getValueAt(i, 1);         // Col 1: Cant
            Producto prod = (Producto) modelo.getValueAt(i, 4);          // Col 4: ProductoObj

            // 2. Restamos stock
            int nuevoStock = prod.getCant() - cantidadVendida;
            prod.setCant(nuevoStock);
            productosActualizados.add(prod);
        }

        // 3. Guardamos en BD los productos actualizados
        if (!productosActualizados.isEmpty()) {
            bd.ModificarProductos(productosActualizados);
        }

        // 4. Limpiamos la tabla
        modelo.setRowCount(0);

        // 5. Reseteamos total y recargamos la tabla
        lbTotal.setText("0.00 €");
        cargarProdFact();

        // 6. Mensaje final
        JOptionPane.showMessageDialog(this, "Factura registrada correctamente.", "Factura", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_btFacturarActionPerformed

    private void btBuscarFactActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btBuscarFactActionPerformed
        // TODO add your handling code here:
        busquedaEnTabla(this.tfBuscarProd.getText());
    }//GEN-LAST:event_btBuscarFactActionPerformed

    private void cbMasCitaPopupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_cbMasCitaPopupMenuWillBecomeVisible
        // TODO add your handling code here:
        // 1. Obtener el cliente seleccionado
        Cliente cli = (Cliente) this.cbCitas.getSelectedItem();
        if(cli == null){
            this.cbMasCita.setModel(new DefaultComboBoxModel<>());
            return;
        }
        
        //2. Crear / rellenar modelo con las mascotas de ese cliente
        DefaultComboBoxModel<Mascota> modeloMasc = new DefaultComboBoxModel<>();
        List<Mascota> lista = bd.ConsultarMascotasPorClie(cli);
        for (Mascota m : lista) {   // o getMascotas()
            modeloMasc.addElement(m);
        }
        
        // 3. Asignarlo al combo de mascotas
        cbMasCita.setModel(modeloMasc);
    }//GEN-LAST:event_cbMasCitaPopupMenuWillBecomeVisible

    private void btModClieActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btModClieActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btModClieActionPerformed

    private void btVentanaModMascotaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btVentanaModMascotaActionPerformed
        // TODO add your handling code here:
        int fila = tClientes.getSelectedRow();

        if (fila == -1) {
            JOptionPane.showMessageDialog(this,
                "Selecciona una mascota para modificar.",
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Mascota masc = (Mascota) tClientes.getValueAt(fila, 5); // Columna 5 = MascotaObj

        // Cargar datos en los campos
        tfInputNombreMascota.setText(masc.getNombre());
        tfInputRaza.setText(masc.getRaza());
        tfInputEspecie.setText(masc.getEspecie());
        tfInputMicrochip.setText(masc.getMicrochip());
        tfInputCapa.setText(masc.getCapa());
        tfInputPelo.setText(masc.getPelo());
        dcFechaNacimiento.setDate(masc.getfNacimiento());

        jdVentanaAñadirMascota.setTitle("Modificar Mascota");
        this.btModifMascota.setEnabled(true);
        this.btCrearMascota.setEnabled(false);
        this.cbClientes.setEnabled(false);
        jdVentanaAñadirMascota.setVisible(true);
    }//GEN-LAST:event_btVentanaModMascotaActionPerformed

    private void btBorrarMascotaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btBorrarMascotaActionPerformed
        // TODO add your handling code here:
        int fila = tClientes.getSelectedRow();

        if (fila == -1) {
            JOptionPane.showMessageDialog(this,
                "Selecciona una mascota para borrar.",
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Mascota masc = (Mascota) tClientes.getValueAt(fila, 5); // Columna 5 = MascotaObj

        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Seguro que quieres borrar a la mascota «" + masc.getNombre() + "»?",
            "Confirmar borrado", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        boolean borrado = bd.borrarMascota(masc);

        if (borrado) {
            cargarMascotasInv(); // ← Refrescar tabla
            JOptionPane.showMessageDialog(this,
                "Mascota eliminada correctamente.",
                "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                "No se pudo eliminar la mascota.",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btBorrarMascotaActionPerformed

    private void btGuardarConsulActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btGuardarConsulActionPerformed
        // TODO add your handling code here:
        Mascota mas = (Mascota) this.cbMascotasConsul.getSelectedItem();
        
        if (mas == null) {
            JOptionPane.showMessageDialog(this, "Selecciona una mascota antes de guardar la consulta.");
            return;
        }
        
        Date fecha = new Date();
        
        ConsultaVeterinaria cv = new ConsultaVeterinaria(
                this.tfPesoConsul.getText(), 
                this.tfTempConsul.getText(), 
                this.taAnamnesis.getText(), 
                this.taSintomatologia.getText(), 
                this.taDiagnostico.getText(), 
                this.taPruebDiagn.getText(), 
                this.taTratamiento.getText(), 
                this.tfVacunas.getText(), 
                this.tfDespara.getText(), 
                this.tfAlimentacion.getText(), 
                this.tfPresion.getText(), 
                fecha,
                mas.getCliente().getNombre(),
                mas);
        
        bd.CrearConsulta(cv, mas);
        
        JOptionPane.showMessageDialog(this, "Consulta guardada con éxito.");
        
        this.tfPesoConsul.setText(""); 
        this.tfTempConsul.setText(""); 
        this.taAnamnesis.setText("");
        this.taSintomatologia.setText(""); 
        this.taDiagnostico.setText(""); 
        this.taPruebDiagn.setText("");
        this.taTratamiento.setText(""); 
        this.tfVacunas.setText("");
        this.tfDespara.setText("");
        this.tfAlimentacion.setText("");
        this.tfPresion.setText("");
        this.cbMascotasConsul.setSelectedItem(null);
    }//GEN-LAST:event_btGuardarConsulActionPerformed

    private void cbMascotasConsulPopupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_cbMascotasConsulPopupMenuWillBecomeVisible
        // TODO add your handling code here:
        Cliente cli = (Cliente) this.cbClientesConsul.getSelectedItem();
        if(cli == null){
            this.cbMascotasConsul.setModel(new DefaultComboBoxModel<>());
            return;
        }
        System.out.println("Cliente es: " + cli.getNombre());
        
        //2. Crear / rellenar modelo con las mascotas de ese cliente
        DefaultComboBoxModel<Mascota> modeloMasc = new DefaultComboBoxModel<>();
        List<Mascota> lista = bd.ConsultarMascotasPorClie(cli);
        for (Mascota m : lista) {   // o getMascotas()
            modeloMasc.addElement(m);
        }
        
        // 3. Asignarlo al combo de mascotas
        cbMascotasConsul.setModel(modeloMasc);
    }//GEN-LAST:event_cbMascotasConsulPopupMenuWillBecomeVisible

    private void tClientesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tClientesMouseClicked
        // TODO add your handling code here:
        if (evt.getClickCount() == 2 && tClientes.getSelectedRow() != -1) {
            int fila = tClientes.getSelectedRow();
            Mascota mascota = (Mascota) tClientes.getValueAt(fila, 5); // Ajusta el índice
            System.out.println("Abriendo el historial de: " + mascota.getNombre());
            mostrarHistorialConsultas(mascota);
        }
    }//GEN-LAST:event_tClientesMouseClicked

    private void btBorrarCita1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btBorrarCita1ActionPerformed
        // TODO add your handling code here:
        int fila = tAgendaInicio.getSelectedRow();

        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona una cita para eliminar.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Estás seguro de que quieres eliminar esta cita?",
            "Confirmar eliminación",
            JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        // Obtener el objeto Cita desde la columna 5
        Cita cita = (Cita) tAgenda.getValueAt(fila, 5);

        boolean borrado = bd.borrarCita(cita);

        if (borrado) {
            ((DefaultTableModel) tAgendaInicio.getModel()).removeRow(fila);
            cargarAgendaInicio();
            JOptionPane.showMessageDialog(this, "Cita eliminada correctamente.");
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo eliminar la cita.");
        }
    }//GEN-LAST:event_btBorrarCita1ActionPerformed

    private void btModificarClieActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btModificarClieActionPerformed
        // TODO add your handling code here:
        int fila = tClientes.getSelectedRow();
        Cliente cliente = (Cliente) tClientes.getValueAt(fila, 4);
        
        cliente.setNombre(tfInputNombreClie.getText());
        cliente.setTelefono(tfInputTelefono.getText());
        cliente.setEmail(tfInputEmail.getText());
        cliente.setDireccion(tfInputDireccion.getText());
        cliente.setDni(tfInputDNI.getText());
        cliente.setCiudad(tfInputCiudad.getText());
        cliente.setProv(tfInputProvincia.getText());
        cliente.setCodP(tfInputPostal.getText());
        
        if (bd.modificarCliente(cliente)) {
            JOptionPane.showMessageDialog(jdVentanaAñadirCliente, "Cliente modificado correctamente.");
            jdVentanaAñadirCliente.setVisible(false);
            cargarMascotasInv();
        } else {
            JOptionPane.showMessageDialog(jdVentanaAñadirCliente, "Error al modificar cliente.");
        }
    }//GEN-LAST:event_btModificarClieActionPerformed

    private void btModifMascotaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btModifMascotaActionPerformed
        // TODO add your handling code here:
        int fila = tClientes.getSelectedRow();
        Mascota masc = (Mascota) tClientes.getValueAt(fila, 5);
        
        masc.setNombre(tfInputNombreMascota.getText());
        masc.setRaza(tfInputRaza.getText());
        masc.setEspecie(tfInputEspecie.getText());
        masc.setMicrochip(tfInputMicrochip.getText());
        masc.setCapa(tfInputCapa.getText());
        masc.setPelo(tfInputPelo.getText());
        masc.setfNacimiento(dcFechaNacimiento.getDate());
        
        System.out.println("El id de la mascota que le pasamos a la funcion es: " + masc.getId());
        
        if (bd.modificarMascota(masc)) {
            JOptionPane.showMessageDialog(jdVentanaAñadirMascota, "Mascota modificada correctamente.");
            jdVentanaAñadirMascota.setVisible(false);
            cargarMascotasInv();
        } else {
            JOptionPane.showMessageDialog(jdVentanaAñadirMascota, "Error al modificar mascota.");
        }
    }//GEN-LAST:event_btModifMascotaActionPerformed

    private void btModifProdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btModifProdActionPerformed
        // TODO add your handling code here:
        int fila = tInventario.getSelectedRow();
        Producto prod = (Producto) tInventario.getValueAt(fila, 5);
        
        prod.setNombre(tfInputNombre.getText());
        prod.setpCompra(Double.parseDouble(tfInputPrecioC.getText()));
        prod.setpPublico(Double.parseDouble(tfInputPVP.getText()));
        prod.setCant(Integer.parseInt(tfInputCant.getText()));
        prod.setCatProd((CategoriaProducto) cbCategory.getSelectedItem());

        if (bd.modificarProducto(prod)) {
            JOptionPane.showMessageDialog(jdVentanaAñadirProd, "Producto modificado.");
            jdVentanaAñadirProd.setVisible(false);
            cargarProdInv();
        } else {
            JOptionPane.showMessageDialog(jdVentanaAñadirProd, "No se pudo modificar el producto.");
        }
    }//GEN-LAST:event_btModifProdActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Main().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel Background;
    private javax.swing.JPanel Header;
    private javax.swing.JPanel Menu;
    private javax.swing.JButton btBorrarCita;
    private javax.swing.JButton btBorrarCita1;
    private javax.swing.JButton btBorrarCliente;
    private javax.swing.JButton btBorrarMascota;
    private javax.swing.JButton btBuscarFact;
    private javax.swing.JButton btCitas;
    private javax.swing.JButton btClientes;
    private javax.swing.JButton btConsultas;
    private javax.swing.JButton btCrearCita;
    private javax.swing.JButton btCrearClie;
    private javax.swing.JButton btCrearMascota;
    private javax.swing.JButton btCrearProd;
    private javax.swing.JButton btDelProd;
    private javax.swing.JButton btFacturacion;
    private javax.swing.JButton btFacturar;
    private javax.swing.JButton btGuardarConsul;
    private javax.swing.JButton btInicio;
    private javax.swing.JButton btInventario;
    private javax.swing.JButton btModClie;
    private javax.swing.JButton btModifMascota;
    private javax.swing.JButton btModifProd;
    private javax.swing.JButton btModificarClie;
    private javax.swing.JButton btNewProd;
    private javax.swing.JButton btNuevaMasc;
    private javax.swing.JButton btPestañaCliente;
    private javax.swing.JButton btSumStack;
    private javax.swing.JButton btVentanaModCliente;
    private javax.swing.JButton btVentanaModMascota;
    private javax.swing.JButton btVentanaModProd;
    private javax.swing.JComboBox<Producto.CategoriaProducto> cbCategory;
    private javax.swing.JComboBox<Cliente> cbCitas;
    private javax.swing.JComboBox<Cliente> cbClientes;
    private javax.swing.JComboBox<Cliente> cbClientesConsul;
    private javax.swing.JComboBox<String> cbHoraCitas;
    private javax.swing.JComboBox<Mascota> cbMasCita;
    private javax.swing.JComboBox<Mascota> cbMascotasConsul;
    private javax.swing.JComboBox<String> cbMinCitas;
    private javax.swing.JComboBox<TipoCita> cbTipoCita;
    private com.toedter.calendar.JDateChooser dcCitas;
    private com.toedter.calendar.JDateChooser dcFechaNacimiento;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel jLabel49;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel50;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel52;
    private javax.swing.JLabel jLabel53;
    private javax.swing.JLabel jLabel54;
    private javax.swing.JLabel jLabel55;
    private javax.swing.JLabel jLabel56;
    private javax.swing.JLabel jLabel57;
    private javax.swing.JLabel jLabel58;
    private javax.swing.JLabel jLabel59;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel60;
    private javax.swing.JLabel jLabel61;
    private javax.swing.JLabel jLabel62;
    private javax.swing.JLabel jLabel63;
    private javax.swing.JLabel jLabel64;
    private javax.swing.JLabel jLabel65;
    private javax.swing.JLabel jLabel66;
    private javax.swing.JLabel jLabel67;
    private javax.swing.JLabel jLabel68;
    private javax.swing.JLabel jLabel69;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel70;
    private javax.swing.JLabel jLabel71;
    private javax.swing.JLabel jLabel72;
    private javax.swing.JLabel jLabel73;
    private javax.swing.JLabel jLabel74;
    private javax.swing.JLabel jLabel75;
    private javax.swing.JLabel jLabel76;
    private javax.swing.JLabel jLabel77;
    private javax.swing.JLabel jLabel78;
    private javax.swing.JLabel jLabel79;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel80;
    private javax.swing.JLabel jLabel81;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane12;
    private javax.swing.JScrollPane jScrollPane13;
    private javax.swing.JScrollPane jScrollPane14;
    private javax.swing.JScrollPane jScrollPane15;
    private javax.swing.JScrollPane jScrollPane16;
    private javax.swing.JScrollPane jScrollPane17;
    private javax.swing.JScrollPane jScrollPane18;
    private javax.swing.JScrollPane jScrollPane19;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane20;
    private javax.swing.JScrollPane jScrollPane21;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JDialog jdAgenda;
    private javax.swing.JDialog jdHistorial;
    private javax.swing.JDialog jdVentanaAñadirCliente;
    private javax.swing.JDialog jdVentanaAñadirMascota;
    private javax.swing.JDialog jdVentanaAñadirProd;
    private javax.swing.JDialog jdVentanaModificarCliente;
    private javax.swing.JLabel lb1;
    private javax.swing.JLabel lb3;
    private javax.swing.JLabel lb4;
    private javax.swing.JLabel lbCat;
    private javax.swing.JLabel lbCat2;
    private javax.swing.JLabel lbClienteHist;
    private javax.swing.JLabel lbComprobanteCita;
    private javax.swing.JLabel lbFecha;
    private javax.swing.JLabel lbFechaConsul;
    private javax.swing.JLabel lbFechaHist;
    private javax.swing.JLabel lbFechaInicio;
    private javax.swing.JLabel lbMascotaHist;
    private javax.swing.JLabel lbPestañaCliente;
    private javax.swing.JLabel lbPestañaCliente1;
    private javax.swing.JLabel lbTotal;
    private org.jdesktop.swingx.JXMonthView monthView;
    private javax.swing.JPanel pCitas;
    private javax.swing.JPanel pClientes;
    private javax.swing.JPanel pConsultas;
    private javax.swing.JPanel pContenido;
    private javax.swing.JPanel pFacturacion;
    private javax.swing.JPanel pInicio;
    private javax.swing.JPanel pInventario;
    private javax.swing.JTable tAgenda;
    private javax.swing.JTable tAgendaInicio;
    private javax.swing.JTable tClientes;
    private javax.swing.JTable tFactura;
    private javax.swing.JTable tHistorialConsultas;
    private javax.swing.JTable tInventario;
    private javax.swing.JTable tInventarioFacturacion;
    private javax.swing.JTextArea taAnamnesis;
    private javax.swing.JTextArea taAnamnesis1;
    private javax.swing.JTextArea taDescripcion;
    private javax.swing.JTextArea taDiagnostico;
    private javax.swing.JTextArea taDiagnostico1;
    private javax.swing.JTextArea taMotivoCita;
    private javax.swing.JTextArea taPruebDiagn;
    private javax.swing.JTextArea taPruebDiagn1;
    private javax.swing.JTextArea taSintomatologia;
    private javax.swing.JTextArea taSintomatologia1;
    private javax.swing.JTextArea taTratamiento;
    private javax.swing.JTextArea taTratamiento1;
    private javax.swing.JTextField tfAlimentacion;
    private javax.swing.JTextField tfAlimentacion1;
    private javax.swing.JTextField tfBuscarProd;
    private javax.swing.JTextField tfDespara;
    private javax.swing.JTextField tfDespara1;
    private javax.swing.JTextField tfInputCant;
    private javax.swing.JTextField tfInputCapa;
    private javax.swing.JTextField tfInputCiudad;
    private javax.swing.JTextField tfInputCiudad1;
    private javax.swing.JTextField tfInputDNI;
    private javax.swing.JTextField tfInputDNI1;
    private javax.swing.JTextField tfInputDireccion;
    private javax.swing.JTextField tfInputDireccion1;
    private javax.swing.JTextField tfInputEmail;
    private javax.swing.JTextField tfInputEmail1;
    private javax.swing.JTextField tfInputEspecie;
    private javax.swing.JTextField tfInputIVA;
    private javax.swing.JTextField tfInputMicrochip;
    private javax.swing.JTextField tfInputNombre;
    private javax.swing.JTextField tfInputNombreClie;
    private javax.swing.JTextField tfInputNombreClie1;
    private javax.swing.JTextField tfInputNombreMascota;
    private javax.swing.JTextField tfInputPVP;
    private javax.swing.JTextField tfInputPelo;
    private javax.swing.JTextField tfInputPostal;
    private javax.swing.JTextField tfInputPostal1;
    private javax.swing.JTextField tfInputPrecioC;
    private javax.swing.JTextField tfInputProvincia;
    private javax.swing.JTextField tfInputProvincia1;
    private javax.swing.JTextField tfInputRaza;
    private javax.swing.JTextField tfInputTelefono;
    private javax.swing.JTextField tfInputTelefono1;
    private javax.swing.JTextField tfPesoConsul;
    private javax.swing.JTextField tfPesoConsul1;
    private javax.swing.JTextField tfPresion;
    private javax.swing.JTextField tfPresion1;
    private javax.swing.JTextField tfTempConsul;
    private javax.swing.JTextField tfTempConsul1;
    private javax.swing.JTextField tfVacunas;
    private javax.swing.JTextField tfVacunas1;
    // End of variables declaration//GEN-END:variables
}
