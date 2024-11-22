package Jframe;

import Conection.Conexion;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AirportJframe {
    private JPanel Principal;
    private JPanel Base;
    private JList<Aeropuerto> AeroPriv;
    private JList<Aeropuerto> AeroPubli;
    private JButton Continuar;
    private JButton Salir;
    private JLabel lblPrivados;
    private JLabel lblPublicos;
    private Connection connection;
    private DefaultListModel<Aeropuerto> modeloPrivados;
    private DefaultListModel<Aeropuerto> modeloPublicos;
    private static Aeropuerto aeropuertoSeleccionado;
    private JFrame frame;
    private JDialog loadingDialog;
    public static class Aeropuerto {
        int id;
        String nombre;
        String ciudad;
        String pais;
        public boolean privado;
        public boolean publico;
        double subvencion;

        public Aeropuerto(int id, String nombre, String ciudad, String pais,
                          boolean privado, boolean publico, double subvencion) {
            this.id = id;
            this.nombre = nombre;
            this.ciudad = ciudad;
            this.pais = pais;
            this.privado = privado;
            this.publico = publico;
            this.subvencion = subvencion;
        }

        @Override
        public String toString() {
            return nombre + " (" + ciudad + ", " + pais + ")";
        }
    }

    public static Aeropuerto getAeropuertoSeleccionado() {
        return aeropuertoSeleccionado;
    }

    public AirportJframe() {
        frame = new JFrame("Aeropuertos GC");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initializeComponents();
        setupLayout();
        connectToDatabase();
        setupLoadingDialog();
        cargarAeropuertos();

        frame.setContentPane(Principal);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
    }


    private void initializeComponents() {
        modeloPrivados = new DefaultListModel<>();
        modeloPublicos = new DefaultListModel<>();
        Principal = new JPanel();
        Base = new JPanel();
        AeroPriv = new JList<>(modeloPrivados);
        AeroPubli = new JList<>(modeloPublicos);
        Continuar = new JButton("Continuar");
        Salir = new JButton("Salir");
        lblPrivados = new JLabel("Aeropuertos Privados", SwingConstants.CENTER);
        lblPublicos = new JLabel("Aeropuertos Públicos", SwingConstants.CENTER);
    }

    private void setupLayout() {
        Principal.setLayout(new BorderLayout());
        Base.setLayout(new GridLayout(1, 2, 10, 10));
        Base.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));


        JPanel panelPrivados = new JPanel(new BorderLayout());
        JScrollPane scrollPriv = new JScrollPane(AeroPriv);
        panelPrivados.add(lblPrivados, BorderLayout.NORTH);
        panelPrivados.add(scrollPriv, BorderLayout.CENTER);


        JPanel panelPublicos = new JPanel(new BorderLayout());
        JScrollPane scrollPub = new JScrollPane(AeroPubli);
        panelPublicos.add(lblPublicos, BorderLayout.NORTH);
        panelPublicos.add(scrollPub, BorderLayout.CENTER);

        Base.add(panelPrivados);
        Base.add(panelPublicos);

        JPanel panelBotones = new JPanel();
        panelBotones.add(Continuar);
        panelBotones.add(Salir);

        Principal.add(Base, BorderLayout.CENTER);
        Principal.add(panelBotones, BorderLayout.SOUTH);

        configurarEventosLista(AeroPriv);
        configurarEventosLista(AeroPubli);
        setupButtons();
    }

    private void setupLoadingDialog() {
        loadingDialog = new JDialog(frame, "Cargando datos...", true);
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        JPanel dialogPanel = new JPanel(new BorderLayout(10, 10));
        dialogPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        dialogPanel.add(new JLabel("Cargando aeropuertos..."), BorderLayout.NORTH);
        dialogPanel.add(progressBar, BorderLayout.CENTER);
        loadingDialog.add(dialogPanel);
        loadingDialog.pack();
        loadingDialog.setLocationRelativeTo(frame);
    }

    private void setupButtons() {
        Continuar.addActionListener(e -> {
            if (aeropuertoSeleccionado != null) {
                abrirSiguienteFrame();
            } else {

            }
        });

        Salir.addActionListener(e -> {
            try {
                connection.close();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            System.exit(0);
        });
    }

    private void configurarEventosLista(JList<Aeropuerto> lista) {
        lista.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    aeropuertoSeleccionado = lista.getSelectedValue();
                    if (aeropuertoSeleccionado != null) {
                        AeroPriv.clearSelection();
                        AeroPubli.clearSelection();
                        lista.setSelectedValue(aeropuertoSeleccionado, true);

                        JOptionPane.showMessageDialog(frame,
                                "Aeropuerto seleccionado: " + aeropuertoSeleccionado.nombre,
                                "Selección",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        });
    }

    private void cargarAeropuertos() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                String query = "SELECT idAeropuerto, nombre, ciudad, pais, privado, publico, subvencion " +
                        "FROM u984447967_op2024b.aeropuertos";
                try (PreparedStatement pstmt = connection.prepareStatement(query);
                     ResultSet rs = pstmt.executeQuery()) {

                    while (rs.next()) {
                        Aeropuerto aeropuerto = new Aeropuerto(
                                rs.getInt("idAeropuerto"),
                                rs.getString("nombre"),
                                rs.getString("ciudad"),
                                rs.getString("pais"),
                                rs.getBoolean("privado"),
                                rs.getBoolean("publico"),
                                rs.getDouble("subvencion")
                        );

                        SwingUtilities.invokeLater(() -> {
                            if (aeropuerto.privado) {
                                modeloPrivados.addElement(aeropuerto);
                            }
                            if (aeropuerto.publico) {
                                modeloPublicos.addElement(aeropuerto);
                            }
                        });
                    }
                }
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

    private void connectToDatabase() {
        try {
            connection = Conexion.conectar();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Error al conectar con la base de datos: " + e.getMessage(),
                    "Error de conexión",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void abrirSiguienteFrame() {
        CompaniaFrame companiaFrame = new CompaniaFrame(frame);
        companiaFrame.mostrar();
        frame.setVisible(false);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AirportJframe());
    }


}