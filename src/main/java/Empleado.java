package main.java;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public class Empleado implements Serializable {
    private String cedula;
    private String nombre;
    private String tipoContrato;
    private String areaTrabajo;
    private double sueldo;

    public Empleado(String cedula, String nombre, String tipoContrato, String areaTrabajo, double sueldo) {
        setCedula(cedula);
        setNombre(nombre);
        setTipoContrato(tipoContrato);
        setAreaTrabajo(areaTrabajo);
        setSueldo(sueldo);
    }

    public Empleado() {
    }

    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        // Validar que la cédula no esté vacía o sea nula
        if (cedula == null || cedula.trim().isEmpty()) {
            throw new IllegalArgumentException("La cédula no puede estar vacía o ser nula");
        }
        this.cedula = cedula;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        // Validar que el nombre no esté vacío o sea nulo
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío o ser nulo");
        }
        this.nombre = nombre;
    }

    public String getTipoContrato() {
        return tipoContrato;
    }

    public void setTipoContrato(String tipoContrato) {
        // Validar que el tipo de contrato sea uno de los valores permitidos
        if (!tipoContrato.equals("4 horas") && !tipoContrato.equals("6 horas") && !tipoContrato.equals("8 horas")) {
            throw new IllegalArgumentException("Tipo de contrato no válido");
        }
        this.tipoContrato = tipoContrato;
    }

    public String getAreaTrabajo() {
        return areaTrabajo;
    }

    public void setAreaTrabajo(String areaTrabajo) {
        // Validar que el área de trabajo no esté vacía o sea nula
        if (areaTrabajo == null || areaTrabajo.trim().isEmpty()) {
            throw new IllegalArgumentException("El área de trabajo no puede estar vacío o ser nulo");
        }
        this.areaTrabajo = areaTrabajo;
    }

    public double getSueldo() {
        return sueldo;
    }

    public void setSueldo(double sueldo) {
        // Validar que el sueldo sea mayor o igual a cero
        if (sueldo < 0) {
            throw new IllegalArgumentException("El sueldo no puede ser negativo");
        }
        this.sueldo = sueldo;
    }

    public void guardarEnBaseDatos() {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "INSERT INTO empleados (cedula, nombre, tipo_contrato, area_trabajo, sueldo) VALUES (?, ?, ?, ?, ?)")) {

            preparedStatement.setString(1, getCedula());
            preparedStatement.setString(2, getNombre());
            preparedStatement.setString(3, getTipoContrato());
            preparedStatement.setString(4, getAreaTrabajo());
            preparedStatement.setDouble(5, getSueldo());

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Empleado> consultarEmpleados() {
        List<Empleado> empleados = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM empleados")) {

            while (resultSet.next()) {
                Empleado empleado = new Empleado();
                empleado.setCedula(resultSet.getString("cedula"));
                empleado.setNombre(resultSet.getString("nombre"));
                empleado.setTipoContrato(resultSet.getString("tipo_contrato"));
                empleado.setAreaTrabajo(resultSet.getString("area_trabajo"));
                empleado.setSueldo(resultSet.getDouble("sueldo"));

                empleados.add(empleado);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return empleados;
    }

    @Override
    public String toString() {
        return "main.java.Empleado{" +
                "cedula='" + cedula + '\'' +
                ", nombre='" + nombre + '\'' +
                ", tipoContrato='" + tipoContrato + '\'' +
                ", areaTrabajo='" + areaTrabajo + '\'' +
                ", sueldo=" + sueldo +
                '}';
    }
}
