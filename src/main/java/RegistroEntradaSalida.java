package main.java;

import java.sql.*;
import java.time.Duration;
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

                        // Calcular las horas trabajadas y convertirlas a horas decimales
                        Time horaEntrada = obtenerHoraEntrada(cedulaEmpleado);
                        long horasTrabajadas = calcularHorasTrabajadas(horaEntrada, horaSalida);

                        preparedStatement.setLong(2, horasTrabajadas);
                        preparedStatement.setString(3, cedulaEmpleado);
                        preparedStatement.setDate(4, java.sql.Date.valueOf(LocalDate.now()));

                        preparedStatement.executeUpdate();

                        // Después de marcarSalida
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
    public static boolean existeEmpleado(String cedulaEmpleado) {
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
    public static long calcularHorasTrabajadas(Time horaEntrada, Time horaSalida) {
        // Calcular la diferencia entre la hora de entrada y la hora de salida
        long diferenciaMilisegundos = horaSalida.getTime() - horaEntrada.getTime();

        // Crear una instancia de Duration con la diferencia en milisegundos
        Duration duration = Duration.ofMillis(diferenciaMilisegundos);

        // Obtener la cantidad total de horas, considerando las horas y minutos
        long horasTrabajadas = duration.toHoursPart() + (duration.toMinutesPart() > 0 ? 0 : 0);

        return horasTrabajadas;
    }
    private static List<String> obtenerListaEmpleados() {
        List<String> listaEmpleados = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT cedula FROM empleados")) {

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String cedulaEmpleado = resultSet.getString("cedula");
                listaEmpleados.add(cedulaEmpleado);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return listaEmpleados;
    }
    public static void actualizarHorasTrabajadasParaTodos() {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "UPDATE registro_entradas_salidas SET horas_trabajadas = ? WHERE cedula_empleado = ? AND fecha = ?")) {

            // Obtener la lista de empleados
            List<String> listaEmpleados = obtenerListaEmpleados();

            // Iterar sobre los empleados
            for (String cedulaEmpleado : listaEmpleados) {
                // Obtener la hora de entrada y salida más reciente para cada empleado y fecha
                Time horaEntrada = obtenerHoraEntrada(cedulaEmpleado);
                Time horaSalida = obtenerHoraSalida(cedulaEmpleado);

                // Verificar que tanto horaEntrada como horaSalida no sean nulos
                if (horaEntrada != null && horaSalida != null) {
                    long horasTrabajadas = calcularHorasTrabajadas(horaEntrada, horaSalida);
                    preparedStatement.setLong(1, horasTrabajadas);
                } else {
                    // Si alguna de las horas es nula
                    preparedStatement.setLong(1, 0);
                }

                preparedStatement.setString(2, cedulaEmpleado);
                preparedStatement.setDate(3, java.sql.Date.valueOf(LocalDate.now()));

                preparedStatement.addBatch();  // Agregar la operación al lote para ejecución eficiente
            }

            // Ejecutar actualizaciones
            preparedStatement.executeBatch();

        } catch (SQLException e) {
            e.printStackTrace();
        }
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
        // Una multa de $15 por cada hora faltante
        return (int) (horasFaltantes * 15);
    }

    private static int calcularBonoPorHorasExtras(long horasExtras) {
        // Un bono de $8 por cada hora extra
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

    public static void calcularSueldo(String cedulaEmpleado, long horasTrabajadas) {
        try (Connection connection = DatabaseManager.getConnection()) {
            // Verificar si ya existe un registro para la fecha actual
            boolean registroExistente = existeRegistroSueldoParaFechaActual(cedulaEmpleado);

            String query;
            if (registroExistente) {
                // Si ya existe un registro, actualizarlo
                query = "UPDATE sueldos SET horas_trabajadas = ?, multa = ?, bono = ?, sueldo = ? " +
                        "WHERE cedula_empleado = ? AND fecha = ?";
            } else {
                // Si no existe un registro, insertarlo
                query = "INSERT INTO sueldos (cedula_empleado, fecha, horas_trabajadas, multa, bono, sueldo) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
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
                preparedStatement.setInt(3, (int) horasTrabajadas);
                preparedStatement.setInt(4, multa);
                preparedStatement.setInt(5, bono);
                preparedStatement.setDouble(6, sueldo);

                if (registroExistente) {
                    // Si ya existe un registro, establecer los parámetros adicionales para la actualización
                    preparedStatement.setString(7, cedulaEmpleado);
                    preparedStatement.setDate(8, java.sql.Date.valueOf(fechaActual));
                }

                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static boolean existeRegistroSueldoParaFechaActual(String cedulaEmpleado) {
        // Verificar si ya existe un registro en la tabla sueldos para la fecha actual
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "SELECT COUNT(*) FROM sueldos WHERE cedula_empleado = ? AND fecha = ?")) {

            preparedStatement.setString(1, cedulaEmpleado);
            preparedStatement.setDate(2, java.sql.Date.valueOf(LocalDate.now()));

            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            int count = resultSet.getInt(1);

            return count > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
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
                        resultSet.getInt("horas_trabajadas"),
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
    public static void actualizarSueldo(String cedulaEmpleado, LocalDate fecha) {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "UPDATE sueldos SET sueldo = (horas_trabajadas * ?) - multa + bono WHERE cedula_empleado = ? AND fecha = ?")) {

            // Obtener la tarifa por hora

            preparedStatement.setDouble(1, tarifaPorHora);
            preparedStatement.setString(2, cedulaEmpleado);
            preparedStatement.setDate(3, java.sql.Date.valueOf(fecha));

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static List<Object[]> obtenerSueldosEnFormatoTabla() {
        List<Object[]> sueldos = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "SELECT e.cedula, e.nombre, r.fecha, r.hora_entrada, r.hora_salida, r.horas_trabajadas " +
                             "FROM registro_entradas_salidas r " +
                             "JOIN empleados e ON r.cedula_empleado = e.cedula")) {

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String cedulaEmpleado = resultSet.getString("cedula");
                String nombreEmpleado = resultSet.getString("nombre");
                String fecha = resultSet.getString("fecha");
                String horaEntrada = resultSet.getString("hora_entrada");
                String horaSalida = resultSet.getString("hora_salida");
                double horasTrabajadas = resultSet.getInt("horas_trabajadas");

                Object[] fila = {cedulaEmpleado, nombreEmpleado, fecha, horaEntrada, horaSalida, horasTrabajadas};
                sueldos.add(fila);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return sueldos;
    }
    /*public static List<Object[]> obtenerSueldosEnFormatoTablaOrdenado() {
        List<Object[]> sueldos = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "SELECT e.cedula, e.nombre, r.fecha, r.hora_entrada, r.hora_salida, r.horas_trabajadas " +
                             "FROM registro_entradas_salidas r " +
                             "JOIN empleados e ON r.cedula_empleado = e.cedula " +
                             "ORDER BY r.fecha ASC, r.hora_entrada ASC, e.nombre ASC")) {

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String cedulaEmpleado = resultSet.getString("cedula");
                String nombreEmpleado = resultSet.getString("nombre");
                String fecha = resultSet.getString("fecha");
                String horaEntrada = resultSet.getString("hora_entrada");
                String horaSalida = resultSet.getString("hora_salida");
                double horasTrabajadas = resultSet.getDouble("horas_trabajadas");

                Object[] fila = {cedulaEmpleado, nombreEmpleado, fecha, horaEntrada, horaSalida, horasTrabajadas};
                sueldos.add(fila);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return sueldos;
    }*/
    public static List<Object[]> obtenerSueldosEnFormatoTablaOrdenado() {
        List<Object[]> sueldos = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "SELECT e.cedula, e.nombre, r.fecha, r.hora_entrada, r.hora_salida, r.horas_trabajadas " +
                             "FROM registro_entradas_salidas r " +
                             "JOIN empleados e ON r.cedula_empleado = e.cedula " +
                             "ORDER BY r.fecha ASC, r.hora_entrada ASC, e.nombre ASC")) {

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String cedulaEmpleado = resultSet.getString("cedula");
                String nombreEmpleado = resultSet.getString("nombre");
                String fecha = resultSet.getString("fecha");
                String horaEntrada = resultSet.getString("hora_entrada");
                String horaSalida = resultSet.getString("hora_salida");
                int horasTrabajadas = resultSet.getInt("horas_trabajadas");

                Object[] fila = {cedulaEmpleado, nombreEmpleado, fecha, horaEntrada, horaSalida, horasTrabajadas};
                sueldos.add(fila);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Aplicar ordenamiento de burbuja
        int n = sueldos.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                // Comparar fechas, horas de entrada y nombres
                if (compararF(sueldos.get(j), sueldos.get(j + 1)) > 0) {
                    // Intercambio
                    Object[] temp = sueldos.get(j);
                    sueldos.set(j, sueldos.get(j + 1));
                    sueldos.set(j + 1, temp);
                }
            }
        }

        return sueldos;
    }

    // Método para comparar dos filas (fechas, horas de entrada y nombres)
    private static int compararF(Object[] row1, Object[] row2) {
        /*Implementar la lógica de comparación según las columnas relevantes
          Devolver un valor negativo si fila1 es menor que fila2, cero si son iguales, y positivo si fila1 es mayor que fila2.*/

        // Comparación de fechas
        String fecha1 = (String) row1[2];
        String fecha2 = (String) row2[2];
        int fechaComparada = fecha1.compareTo(fecha2);
        if (fechaComparada != 0) {
            return fechaComparada;
        }

        // Comparación de horas de entrada
        String horaEntrada1 = (String) row1[3];
        String horaEntrada2 = (String) row2[3];
        int horaEntradaComparada = horaEntrada1.compareTo(horaEntrada2);
        if (horaEntradaComparada != 0) {
            return horaEntradaComparada;
        }

        // Comparación de nombres
        String nombre1 = (String) row1[1];
        String nombre2 = (String) row2[1];
        return nombre1.compareTo(nombre2);
    }


}
