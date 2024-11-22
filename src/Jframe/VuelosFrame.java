package Jframe;

import Conection.Conexion;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VuelosFrame {
    private JPanel panel1;
    private JList<Vuelo> list1;
    private JButton Continua;
    private JButton Salir2;
    private DefaultListModel<Vuelo> modeloVuelos;
    private Connection connection;
    private JFrame frame;
    private JFrame parentFrame;

    public static class Vuelo {
        int idVuelo;
        String identificador;
        String ciudadOrigen;
        String ciudadDestino;
        double precio;
        int numMaxPasajeros;

        public Vuelo(int idVuelo, String identificador, String ciudadOrigen,
                     String ciudadDestino, double precio, int numMaxPasajeros) {
            this.idVuelo = idVuelo;
            this.identificador = identificador;
            this.ciudadOrigen = ciudadOrigen;
            this.ciudadDestino = ciudadDestino;
            this.precio = precio;
            this.numMaxPasajeros = numMaxPasajeros;
        }

        @Override
        public String toString() {
            return String.format("Vuelo %s: %s → %s | Precio: $%.2f | Capacidad: %d pasajeros",
                    identificador, ciudadOrigen, ciudadDestino, precio, numMaxPasajeros);
        }
    }


    public VuelosFrame(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        initialize();
    }

    private void initialize() {
        frame = new JFrame("Vuelos Disponibles");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                volverAVentanaAnterior();
            }
        });

        setupUI();
        connectToDatabase();
        cargarVuelosAsync();
    }

    private void setupUI() {
        panel1 = new JPanel(new BorderLayout());
        panel1.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titulo = new JLabel("Lista de Vuelos", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 16));
        titulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        modeloVuelos = new DefaultListModel<>();
        list1 = new JList<>(modeloVuelos);
        list1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list1.setFont(new Font("Arial", Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(list1);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER));

        Salir2 = new JButton("Volver");
        Salir2.setPreferredSize(new Dimension(100, 30));
        Salir2.addActionListener(e -> volverAVentanaAnterior());

        Continua = new JButton("Continuar");
        Continua.setPreferredSize(new Dimension(100, 30));
        Continua.addActionListener(e -> continuarSiguienteFrame());

        panelBotones.add(Salir2);
        panelBotones.add(Continua);

        panel1.add(titulo, BorderLayout.NORTH);
        panel1.add(scrollPane, BorderLayout.CENTER);
        panel1.add(panelBotones, BorderLayout.SOUTH);

        frame.setContentPane(panel1);
        frame.setSize(600, 500);
        frame.setLocationRelativeTo(parentFrame);
    }

    private void cargarVuelosAsync() {
        JDialog loadingDialog = createLoadingDialog();

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                cargarVuelos();
                return null;
            }

            @Override
            protected void done() {
                loadingDialog.dispose();
            }
        };

        worker.execute();
        loadingDialog.setVisible(true);
    }

    private JDialog createLoadingDialog() {
        JDialog loadingDialog = new JDialog(frame, "Cargando vuelos...", true);
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        loadingDialog.add(progressBar);
        loadingDialog.setSize(200, 60);
        loadingDialog.setLocationRelativeTo(frame);
        return loadingDialog;
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

    private void cargarVuelos() {
        String query = "SELECT idVuelo, identificador, ciudadOrigen, ciudadDestino, precio, numMaxPasajeros " +
                "FROM u984447967_op2024b.vuelos";
        try (PreparedStatement pstmt = connection.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            SwingUtilities.invokeLater(() -> modeloVuelos.clear());

            while (rs.next()) {
                final Vuelo vuelo = new Vuelo(
                        rs.getInt("idVuelo"),
                        rs.getString("identificador"),
                        rs.getString("ciudadOrigen"),
                        rs.getString("ciudadDestino"),
                        rs.getDouble("precio"),
                        rs.getInt("numMaxPasajeros")
                );
                SwingUtilities.invokeLater(() -> modeloVuelos.addElement(vuelo));
            }

        } catch (SQLException e) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(frame,
                        "Error al cargar los vuelos: " + e.getMessage(),
                        "Error de datos",
                        JOptionPane.ERROR_MESSAGE);
            });
            e.printStackTrace();
        }
    }

    private void volverAVentanaAnterior() {
        frame.setVisible(false);
        parentFrame.setVisible(true);
        frame.dispose();
    }

    private void continuarSiguienteFrame() {
        Vuelo vueloSeleccionado = list1.getSelectedValue();
        if (vueloSeleccionado == null) {
            JOptionPane.showMessageDialog(frame,
                    "Por favor, seleccione un vuelo para continuar",
                    "Selección requerida",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        FacturaFrame facturaFrame = new FacturaFrame(frame, vueloSeleccionado,
                AirportJframe.getAeropuertoSeleccionado(),
                CompaniaFrame.getCompaniaSeleccionada());
        frame.setVisible(false);
        facturaFrame.mostrar();
    }


    public void mostrar() {
        frame.setVisible(true);
    }

    public void cerrarConexion() {
        Conexion.cerrarConexion();
    }
}