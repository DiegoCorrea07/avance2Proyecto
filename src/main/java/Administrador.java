package main.java;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class Administrador {

    private String usuario;
    private String contraseña;
    private String cedula;

    public Administrador(String usuario, String contraseña, String cedula) {
        this.usuario = "admin";
        this.contraseña = "admin";
        this.cedula = cedula;
    }

    public Administrador() {

    }


    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getContraseña() {
        return contraseña;
    }

    public void setContraseña(String contraseña) {
        this.contraseña = contraseña;
    }

    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        this.cedula = cedula;
    }

    public String guardarEnBaseDatos(Empleado a) {
        // Verificar si la cédula ya existe en la base de datos
        if (existeCedulaEnBaseDatos(a.getCedula())) {
            return "El empleado ya se encuentra registrado.";
        }

        // Si la cédula no existe, proceder con la inserción
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "INSERT INTO empleados (cedula, nombre, tipo_contrato, area_trabajo, sueldo) VALUES (?, ?, ?, ?, ?)")) {

            preparedStatement.setString(1, a.getCedula());
            preparedStatement.setString(2, a.getNombre());
            preparedStatement.setString(3, a.getTipoContrato());
            preparedStatement.setString(4, a.getAreaTrabajo());
            preparedStatement.setDouble(5, a.getSueldo());

            preparedStatement.executeUpdate();

            return "Empleado agregado correctamente a la base de datos.";

        } catch (SQLException e) {
            e.printStackTrace();
            return "Error al agregar el empleado a la base de datos.";
        }
    }

    private boolean existeCedulaEnBaseDatos(String cedula) {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "SELECT COUNT(*) FROM empleados WHERE cedula = ?")) {

            preparedStatement.setString(1, cedula);

            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            int count = resultSet.getInt(1);

            return count > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public String eliminarEmpleado(String cedula) throws SQLException{
        try (Connection connection = DatabaseManager.getConnection()) {
            // Deshabilitar el modo de autocommit para iniciar una transacción
            connection.setAutoCommit(false);

            // Definir consultas SQL
            String deleteEmpleadoQuery = "DELETE FROM empleados WHERE cedula = ?";
            String deleteRegistroQuery = "DELETE FROM registro_entradas_salidas WHERE cedula_empleado = ?";
            String deleteSueldosQuery = "DELETE FROM sueldos WHERE cedula_empleado = ?";

            try (
                    // Preparar las consultas
                    PreparedStatement deleteEmpleadoStatement = connection.prepareStatement(deleteEmpleadoQuery);
                    PreparedStatement deleteRegistroStatement = connection.prepareStatement(deleteRegistroQuery);
                    PreparedStatement deleteSueldosStatement = connection.prepareStatement(deleteSueldosQuery)
            ) {
                // Eliminar empleado de la tabla 'empleados'
                deleteEmpleadoStatement.setString(1, cedula);
                int filasAfectadas1 = deleteEmpleadoStatement.executeUpdate();

                if (filasAfectadas1 == 0) {
                    // No se encontró ningún empleado con la cédula proporcionada
                    return "No se encontró ningún empleado con la cédula proporcionada";
                }

                // Eliminar registros relacionados con el registro de entradas y salidas
                deleteRegistroStatement.setString(1, cedula);
                deleteRegistroStatement.executeUpdate();

                // Eliminar registros relacionados con cálculo de sueldos
                deleteSueldosStatement.setString(1, cedula);
                deleteSueldosStatement.executeUpdate();

                // Confirmar la transacción
                connection.commit();

                return "Empleado y registros en el sistema eliminados correctamente";
            } catch (SQLException e) {
                // En caso de error, realizar, para deshacer la transacción
                connection.rollback();
                e.printStackTrace();
                return "Error al eliminar empleado y registros relacionados.";
            } finally {
                try {
                    // Restaurar el modo de autocommit solo si no hay excepciones pendientes
                    if (connection != null && !connection.isClosed()) {
                        connection.setAutoCommit(true);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error al conectar con la base de datos.";
        }
    }


    public String agregarMultas(String cedula, LocalDate fecha, int multa) {
        // Verificar si el empleado existe antes de agregar multas
        if (RegistroEntradaSalida.existeEmpleado(cedula)) {
            try (Connection connection = DatabaseManager.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(
                         "UPDATE sueldos SET multa = multa + ? WHERE cedula_empleado = ? AND fecha = ?")) {

                preparedStatement.setInt(1, multa);
                preparedStatement.setString(2, cedula);
                preparedStatement.setDate(3, java.sql.Date.valueOf(fecha));

                int filasAfectadas = preparedStatement.executeUpdate();

                if (filasAfectadas > 0) {
                    // Después de agregar multas, actualizar el sueldo
                    RegistroEntradaSalida.actualizarSueldo(cedula, fecha);

                    return "Multas agregadas correctamente";


                } else {
                    return "No se encontró ningún registro para la cédula y fecha proporcionadas";
                }

            } catch (SQLException e) {
                e.printStackTrace();
                return "Error al registrar empleado";
            }
        } else {
            return "El empleado no está registrado en la base de datos.";
        }
    }

    public String agregarBonos(String cedula, LocalDate fecha, int bono) {
        // Verificar si el empleado existe antes de agregar bonos
        if (RegistroEntradaSalida.existeEmpleado(cedula)) {
            try (Connection connection = DatabaseManager.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(
                         "UPDATE sueldos SET bono = bono + ? WHERE cedula_empleado = ? AND fecha = ?")) {

                preparedStatement.setInt(1, bono);
                preparedStatement.setString(2, cedula);
                preparedStatement.setDate(3, java.sql.Date.valueOf(fecha));

                int filasAfectadas = preparedStatement.executeUpdate();

                if (filasAfectadas > 0) {
                    // Después de agregar bonos, actualizar el sueldo
                    RegistroEntradaSalida.actualizarSueldo(cedula, fecha);
                    return "Bonos agregados correctamente";
                } else {
                    return "No se encontró ningún registro para la cédula y fecha proporcionadas";
                }

            } catch (SQLException e) {
                e.printStackTrace();
                return "Error al agregar bono";
            }
        } else {
            return "El empleado no está registrado en la base de datos.";
        }
    }

}