package main.java;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DatabaseManager {
    private static final String PROPERTIES_FILE = "config.properties";
    private static final String DB_URL_PROPERTY = "db.url";
    private static final String DB_USERNAME_PROPERTY = "db.username";
    private static final String DB_PASSWORD_PROPERTY = "db.password";

    static {
        loadDatabaseDriver();
    }

    private static void loadDatabaseDriver() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("No se pudo cargar el controlador de SQL Server.", e);
        }
    }


    public static void crearTablaEmpleados() {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {

            String createTableSQL = "CREATE TABLE IF NOT EXISTS empleados (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "cedula NVARCHAR(255) NOT NULL," +
                    "nombre NVARCHAR(255) NOT NULL," +
                    "tipo_contrato NVARCHAR(255) NOT NULL," +
                    "area_trabajo NVARCHAR(255) NOT NULL," +
                    "sueldo FLOAT NOT NULL)";

            statement.executeUpdate(createTableSQL);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static Connection getConnection() throws SQLException {
        Properties properties = new Properties();
        try (InputStream inputStream = DatabaseManager.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String dbUrl = properties.getProperty(DB_URL_PROPERTY);
        String dbUsername = properties.getProperty(DB_USERNAME_PROPERTY);
        String dbPassword = properties.getProperty(DB_PASSWORD_PROPERTY);

        return DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
    }
}
