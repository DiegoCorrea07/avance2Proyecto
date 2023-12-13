package main.java;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

public class RegistroEntradaSalida {

    private static double tarifaPorHora = 6;
    public static String marcarEntrada(String cedulaEmpleado) {
        // Verificar si el empleado está en la base de datos
        if (existeEmpleado(cedulaEmpleado)) {
            // Verificar si el empleado ya ha marcado la hora de entrada para el día actual
            if (!haMarcadoEntrada(cedulaEmpleado, LocalDate.now())) {
                // Obtener la conexión a la base de datos
                try (Connection connection = DatabaseManager.getConnection();
                     PreparedStatement preparedStatement = connection.prepareStatement(
                             "INSERT INTO registro_entradas_salidas (cedula_empleado, fecha, hora_entrada) VALUES (?, ?, ?)")) {

                    preparedStatement.setString(1, cedulaEmpleado);
                    preparedStatement.setDate(2, java.sql.Date.valueOf(LocalDate.now()));

                    // Obtener la hora actual
                    Time horaEntrada = new Time(System.currentTimeMillis());
                    preparedStatement.setTime(3, horaEntrada);

                    preparedStatement.executeUpdate();

                    return "Entrada marcada correctamente para la cédula: " + cedulaEmpleado;

                } catch (SQLException e) {
                    e.printStackTrace();
                    return "Error al marcar la entrada. Consulta los registros para más detalles.";
                }
            } else {
                return "Ya has marcado la hora de entrada hoy.";
            }
        } else {
            return "El empleado no está registrado en la base de datos.";
        }
    }

    public static String marcarSalida(String cedulaEmpleado) {
        // Verificar si el empleado está en la base de datos
        if (existeEmpleado(cedulaEmpleado)) {
            // Obtener la conexión a la base de datos
            try (Connection connection = DatabaseManager.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(
                         "UPDATE registro_entradas_salidas SET hora_salida = ?, horas_trabajadas = ? WHERE cedula_empleado = ? AND fecha = ?")) {

                // Verificar si el empleado ha marcado entrada antes de permitir marcar salida
                if (haMarcadoEntrada(cedulaEmpleado, LocalDate.now())) {
                    // Verificar si el empleado ya ha marcado la hora de salida hoy
                    if (!haMarcadoSalida(cedulaEmpleado, LocalDate.now())) {
                        // Obtener la hora actual
                        Time horaSalida = new Time(System.currentTimeMillis());

                        preparedStatement.setTime(1, horaSalida);

                        // Calcular las horas trabajadas (hora_salida - hora_entrada) y convertirlas a horas decimales
                        Time horaEntrada = obtenerHoraEntrada(cedulaEmpleado);
                        long horasTrabajadas = calcularHorasTrabajadas(horaEntrada, horaSalida);

                        preparedStatement.setLong(2, horasTrabajadas);
                        preparedStatement.setString(3, cedulaEmpleado);
                        preparedStatement.setDate(4, java.sql.Date.valueOf(LocalDate.now()));

                        preparedStatement.executeUpdate();

                        // Después de preparedStatement.executeUpdate() en marcarSalida
                        calcularSueldo(cedulaEmpleado, horasTrabajadas);

                        return "Salida marcada correctamente para la cédula: " + cedulaEmpleado;
                    } else {
                        return "Ya has marcado la hora de salida hoy.";
                    }
                } else {

                    return "No puedes marcar salida sin haber marcado entrada.";
                }

            } catch (SQLException e) {
                e.printStackTrace();
                return "Error al marcar la salida. Consulta los registros para más detalles.";
            }
        } else {

            return "El empleado no está registrado en la base de datos.";
        }
    }


    private static boolean existeEmpleado(String cedulaEmpleado) {
        // Verificar si el empleado existe en la base de datos
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "SELECT COUNT(*) FROM empleados WHERE cedula = ?")) {

            preparedStatement.setString(1, cedulaEmpleado);

            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            int count = resultSet.getInt(1);

            return count > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private static boolean haMarcadoEntrada(String cedulaEmpleado, LocalDate fecha) {
        // Verificar si el empleado ya ha marcado la hora de entrada para el día especificado
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "SELECT COUNT(*) FROM registro_entradas_salidas WHERE cedula_empleado = ? AND fecha = ?")) {

            preparedStatement.setString(1, cedulaEmpleado);
            preparedStatement.setDate(2, java.sql.Date.valueOf(fecha));

            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            int count = resultSet.getInt(1);

            return count > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private static boolean haMarcadoSalida(String cedulaEmpleado, LocalDate fecha) {
        // Verificar si el empleado ya ha marcado la hora de salida para el día especificado
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "SELECT COUNT(*) FROM registro_entradas_salidas WHERE cedula_empleado = ? AND fecha = ? AND hora_salida IS NOT NULL")) {

            preparedStatement.setString(1, cedulaEmpleado);
            preparedStatement.setDate(2, java.sql.Date.valueOf(fecha));

            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            int count = resultSet.getInt(1);

            return count > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }


    private static Time obtenerHoraEntrada(String cedulaEmpleado) {
        // Obtener la hora de entrada más reciente del empleado

        Time horaEntrada = null;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "SELECT MAX(hora_entrada) FROM registro_entradas_salidas WHERE cedula_empleado = ? AND fecha = ?")) {

            preparedStatement.setString(1, cedulaEmpleado);
            preparedStatement.setDate(2, java.sql.Date.valueOf(LocalDate.now()));

            // Ejecutar la consulta y obtener el resultado
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                horaEntrada = resultSet.getTime(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return horaEntrada;
    }
    private static long calcularHorasTrabajadas(Time horaEntrada, Time horaSalida) {
        // Calcular la diferencia en milisegundos
        long diferenciaMilisegundos = horaSalida.getTime() - horaEntrada.getTime();

        // Convertir milisegundos a horas en formato decimal
       long horasTrabajadas = diferenciaMilisegundos / (60 * 60 * 1000);

        // Redondear hacia abajo para obtener un valor entero
        return (horasTrabajadas/3600000);
    }

    private static int obtenerHorasContratoSegunTipo(String tipoContrato) {
        switch (tipoContrato) {
            case "4 horas":
                return 4;
            case "6 horas":
                return 6;
            case "8 horas":
                return 8;
            default:
                throw new IllegalArgumentException("Tipo de contrato no válido");
        }
    }

    private static int calcularMultaPorHorasFaltantes(long horasFaltantes) {
        // Se asume una multa de $15 por cada hora faltante
        return (int) (horasFaltantes * 15);
    }

    private static int calcularBonoPorHorasExtras(long horasExtras) {
        // Se asume un bono de $8 por cada hora extra
        return (int) (horasExtras * 8);
    }
    private static String obtenerTipoContratoEmpleado(String cedulaEmpleado) {
        // Obtener el tipo de contrato del empleado desde la base de datos
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "SELECT tipo_contrato FROM empleados WHERE cedula = ?")) {

            preparedStatement.setString(1, cedulaEmpleado);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("tipo_contrato");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        throw new IllegalStateException("No se pudo obtener el tipo de contrato del empleado");
    }
    private static Time obtenerHoraSalida(String cedulaEmpleado) {
        // Obtener la hora de salida más reciente del empleado
        Time horaSalida = null;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "SELECT MAX(hora_salida) FROM registro_entradas_salidas WHERE cedula_empleado = ? AND fecha = ?")) {

            preparedStatement.setString(1, cedulaEmpleado);
            preparedStatement.setDate(2, java.sql.Date.valueOf(LocalDate.now()));

            // Ejecutar la consulta y obtener el resultado
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                horaSalida = resultSet.getTime(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return horaSalida;
    }

    private static void calcularSueldo(String cedulaEmpleado, long horasTrabajadas) {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "INSERT INTO sueldos (cedula_empleado, fecha, horas_trabajadas, multa, bono, sueldo) VALUES (?, ?, ?, ?, ?, ?)")) {

            // Obtener la fecha actual
            LocalDate fechaActual = LocalDate.now();

            // Obtener el tipo de contrato del empleado
            String tipoContrato = obtenerTipoContratoEmpleado(cedulaEmpleado);
            int horasContrato = obtenerHorasContratoSegunTipo(tipoContrato);

            // Calcular las horas faltantes o extras
            long horasFaltantesOExtras = horasTrabajadas - horasContrato;

            // Calcular la multa por horas faltantes
            int multa = (horasFaltantesOExtras < 0) ? calcularMultaPorHorasFaltantes(-horasFaltantesOExtras) : 0;

            // Calcular el bono por horas extras
            int bono = (horasFaltantesOExtras > 0) ? calcularBonoPorHorasExtras(horasFaltantesOExtras) : 0;

            // Calcular el sueldo
            double sueldo = (horasTrabajadas * tarifaPorHora) - multa + bono;

            preparedStatement.setString(1, cedulaEmpleado);
            preparedStatement.setDate(2, java.sql.Date.valueOf(fechaActual));
            preparedStatement.setLong(3, horasTrabajadas);
            preparedStatement.setInt(4, multa);
            preparedStatement.setInt(5, bono);
            preparedStatement.setDouble(6, sueldo);

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<SueldoRegistro> obtenerSueldosPorCedula(String cedulaEmpleado) {
        List<SueldoRegistro> sueldos = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "SELECT * FROM sueldos WHERE cedula_empleado = ?")) {

            preparedStatement.setString(1, cedulaEmpleado);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                SueldoRegistro sueldo = new SueldoRegistro(
                        resultSet.getInt("id"),
                        resultSet.getString("cedula_empleado"),
                        resultSet.getDate("fecha"),
                        resultSet.getDouble("horas_trabajadas"),
                        resultSet.getInt("multa"),
                        resultSet.getInt("bono"),
                        resultSet.getDouble("sueldo")
                );
                sueldos.add(sueldo);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return sueldos;
    }
}
