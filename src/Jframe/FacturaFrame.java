package Jframe;

import Conection.Conexion;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class FacturaFrame {
    private JPanel panel1;
    private JList<String> facturaList;
    private JButton salirButton;
    private JButton comprarButton;
    private JTextField nombreField;
    private JTextField pasaporteField;
    private JTextField nacionalidadField;
    private DefaultListModel<String> modeloFactura;
    private Connection connection;
    private JFrame frame;
    private JFrame parentFrame;

    private VuelosFrame.Vuelo vueloSeleccionado;
    private AirportJframe.Aeropuerto aeropuertoSeleccionado;
    private CompaniaFrame.Compania companiaSeleccionada;

    public FacturaFrame(JFrame parentFrame, VuelosFrame.Vuelo vuelo,
                        AirportJframe.Aeropuerto aeropuerto,
                        CompaniaFrame.Compania compania) {
        this.parentFrame = parentFrame;
        this.vueloSeleccionado = vuelo;
        this.aeropuertoSeleccionado = aeropuerto;
        this.companiaSeleccionada = compania;
        initialize();
    }

    private void initialize() {
        frame = new JFrame("Facturación");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                volverAVentanaAnterior();
            }
        });

        setupUI();
        connectToDatabase();
        cargarResumenCompra();
    }

    private void setupUI() {
        panel1 = new JPanel(new BorderLayout(10, 10));
        panel1.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));


        JPanel datosPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        datosPanel.setBorder(BorderFactory.createTitledBorder("Datos del Pasajero"));

        datosPanel.add(new JLabel("Nombre:"));
        nombreField = new JTextField();
        datosPanel.add(nombreField);

        datosPanel.add(new JLabel("Pasaporte:"));
        pasaporteField = new JTextField();
        datosPanel.add(pasaporteField);

        datosPanel.add(new JLabel("Nacionalidad:"));
        nacionalidadField = new JTextField();
        datosPanel.add(nacionalidadField);


        modeloFactura = new DefaultListModel<>();
        facturaList = new JList<>(modeloFactura);
        JScrollPane scrollPane = new JScrollPane(facturaList);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Resumen de Compra"));

        JPanel botonesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        salirButton = new JButton("Volver");
        comprarButton = new JButton("Confirmar Compra");

        salirButton.addActionListener(e -> volverAVentanaAnterior());
        comprarButton.addActionListener(e -> procesarCompra());

        botonesPanel.add(salirButton);
        botonesPanel.add(comprarButton);


        panel1.add(datosPanel, BorderLayout.NORTH);
        panel1.add(scrollPane, BorderLayout.CENTER);
        panel1.add(botonesPanel, BorderLayout.SOUTH);

        frame.setContentPane(panel1);
        frame.setSize(500, 600);
        frame.setLocationRelativeTo(parentFrame);
    }

    private void connectToDatabase() {
        try {
            connection = Conexion.conectar();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame,
                    "Error al conectar con la base de datos: " + e.getMessage(),
                    "Error de conexión",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void cargarResumenCompra() {
        modeloFactura.clear();
        modeloFactura.addElement("=== RESUMEN DE COMPRA ===");
        modeloFactura.addElement("");
        modeloFactura.addElement("Aeropuerto: " + aeropuertoSeleccionado.toString());
        modeloFactura.addElement("Compañía: " + companiaSeleccionada.toString());
        modeloFactura.addElement("");
        modeloFactura.addElement("=== DETALLES DEL VUELO ===");
        modeloFactura.addElement("Identificador: " + vueloSeleccionado.identificador);
        modeloFactura.addElement("Origen: " + vueloSeleccionado.ciudadOrigen);
        modeloFactura.addElement("Destino: " + vueloSeleccionado.ciudadDestino);
        modeloFactura.addElement("Precio: $" + String.format("%.2f", vueloSeleccionado.precio));
    }

    private void procesarCompra() {
        if (!validarCampos()) {
            return;
        }

        try {
            guardarPasajero();
            JOptionPane.showMessageDialog(frame,
                    "¡Compra realizada con éxito!",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
            cerrarVentana();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame,
                    "Error al procesar la compra: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private boolean validarCampos() {
        if (nombreField.getText().trim().isEmpty() ||
                pasaporteField.getText().trim().isEmpty() ||
                nacionalidadField.getText().trim().isEmpty()) {

            JOptionPane.showMessageDialog(frame,
                    "Por favor, complete todos los campos",
                    "Campos incompletos",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void guardarPasajero() throws SQLException {
        String query = "INSERT INTO u984447967_op2024b.pasajeros (nombre, pasaporte, nacionalidad) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, nombreField.getText().trim());
            pstmt.setString(2, pasaporteField.getText().trim());
            pstmt.setString(3, nacionalidadField.getText().trim());
            pstmt.executeUpdate();
        }
    }

    private void volverAVentanaAnterior() {
        frame.setVisible(false);
        parentFrame.setVisible(true);
        frame.dispose();
    }

    private void cerrarVentana() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        frame.dispose();
        System.exit(0);
    }

    private void cerrarConexion() {
        Conexion.cerrarConexion();
    }

    public void mostrar() {
        frame.setVisible(true);
    }
}