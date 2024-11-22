package Jframe;

import Conection.Conexion;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CompaniaFrame {
    private JPanel Compania;
    private JPanel PanelCompa;
    private JList<Compania> list1;
    private JButton Continuar;
    private JButton btnVolver;
    private DefaultListModel<Compania> modeloCompanias;
    private Connection connection;
    private JFrame frame;
    private JFrame parentFrame;
    private static Compania companiaSeleccionada;

    public static class Compania {
        int idCompania;
        String nombre;

        public Compania(int idCompania, String nombre) {
            this.idCompania = idCompania;
            this.nombre = nombre;
        }

        @Override
        public String toString() {
            return nombre;
        }
    }

    public static Compania getCompaniaSeleccionada() {
        return companiaSeleccionada;
    }

    public CompaniaFrame(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        initialize();
        Continuar.addActionListener(e -> continuarSiguienteFrame());
    }

    private void initialize() {
        frame = new JFrame("Compañías Aéreas");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                volverAVentanaAnterior();
            }
        });

        setupUI();
        connectToDatabase();
        cargarCompaniasAsync();
    }

    private void setupUI() {
        Compania = new JPanel();
        Compania.setLayout(new BorderLayout());

        PanelCompa = new JPanel(new BorderLayout());
        PanelCompa.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titulo = new JLabel("Compañías Disponibles", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 16));
        titulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        modeloCompanias = new DefaultListModel<>();
        list1 = new JList<>(modeloCompanias);
        list1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list1.setFont(new Font("Arial", Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(list1);
        PanelCompa.add(titulo, BorderLayout.NORTH);
        PanelCompa.add(scrollPane, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnVolver = new JButton("Volver");
        Continuar = new JButton("Continuar");

        btnVolver.setPreferredSize(new Dimension(100, 30));
        Continuar.setPreferredSize(new Dimension(100, 30));

        btnVolver.addActionListener(e -> volverAVentanaAnterior());

        panelBotones.add(btnVolver);
        panelBotones.add(Continuar);

        PanelCompa.add(panelBotones, BorderLayout.SOUTH);
        Compania.add(PanelCompa, BorderLayout.CENTER);

        frame.setContentPane(Compania);
        frame.setSize(400, 500);
        frame.setLocationRelativeTo(parentFrame);
    }

    private void cargarCompaniasAsync() {
        JDialog loadingDialog = createLoadingDialog();

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                cargarCompanias();
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
        JDialog loadingDialog = new JDialog(frame, "Cargando compañías...", true);
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        loadingDialog.add(progressBar);
        loadingDialog.setSize(200, 60);
        loadingDialog.setLocationRelativeTo(frame);
        return loadingDialog;
    }

    private void continuarSiguienteFrame() {
        companiaSeleccionada = list1.getSelectedValue();
        if (companiaSeleccionada == null) {
            JOptionPane.showMessageDialog(frame,
                    "Por favor, seleccione una compañía para continuar",
                    "Selección requerida",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        VuelosFrame vuelosFrame = new VuelosFrame(frame);
        frame.setVisible(false);
        vuelosFrame.mostrar();
    }

    private void volverAVentanaAnterior() {
        frame.setVisible(false);
        parentFrame.setVisible(true);
        frame.dispose();
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



    private void cargarCompanias() {
        String query = "SELECT idCompania, nombre FROM u984447967_op2024b.companias";
        try (PreparedStatement pstmt = connection.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            SwingUtilities.invokeLater(() -> modeloCompanias.clear());

            while (rs.next()) {
                final Compania compania = new Compania(
                        rs.getInt("idCompania"),
                        rs.getString("nombre")
                );
                SwingUtilities.invokeLater(() -> modeloCompanias.addElement(compania));
            }

        } catch (SQLException e) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(frame,
                        "Error al cargar las compañías: " + e.getMessage(),
                        "Error de datos",
                        JOptionPane.ERROR_MESSAGE);
            });
            e.printStackTrace();
        }
    }

    public void mostrar() {
        frame.setVisible(true);
    }

    public void cerrarConexion() {
        Conexion.cerrarConexion();
    }
}