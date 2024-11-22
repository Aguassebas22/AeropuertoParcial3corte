package Conection;


import javax.swing.*;
import java.sql.*;
import java.util.Timer;
import java.util.TimerTask;

public class Conexion {
    static String url = "jdbc:mysql://82.197.82.62:3306/u984447967_op2024b?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    static String user = "u984447967_unipaz";
    static String pass = "estudiantesPoo2024B.*";
    private static Connection connection;
    private static Timer connectionTimer;


    public static Connection conectar() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(url, user, pass);
            startConnectionKeepAlive();
        }
        return connection;
    }

    private static void startConnectionKeepAlive() {

        if (connectionTimer != null) {
            connectionTimer.cancel();
        }


        connectionTimer = new Timer(true);
        connectionTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (connection != null && !connection.isClosed()) {

                        connection.createStatement().execute("SELECT 1");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    try {
                        // Attempt to reconnect if connection is lost
                        connection = DriverManager.getConnection(url, user, pass);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }, 0, 300000); // Run every 5 minutes (300000 milliseconds)
    }

    public static void cerrarConexion() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
