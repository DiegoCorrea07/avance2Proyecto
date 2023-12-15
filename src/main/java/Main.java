package main.java;
import java.sql.Time;
public class Main {


        /*public static void main(String[] args) {
            Time horaEntrada = Time.valueOf("08:30:10");
            Time horaSalida = Time.valueOf("17:30:50");

            long horasTrabajadas = calcularHorasTrabajadas(horaEntrada, horaSalida);
            System.out.println("Horas trabajadas: " + horasTrabajadas);
        }

        private static long calcularHorasTrabajadas(Time horaEntrada, Time horaSalida) {
            // Calcular la diferencia en milisegundos
            long diferenciaMilisegundos = horaSalida.getTime() - horaEntrada.getTime();

            // Convertir milisegundos a horas
            long horasTrabajadas = diferenciaMilisegundos / (60 * 60 * 1000);

            return Math.max(0, horasTrabajadas);  // Asegurarse de que no sea negativo
        }*/

    public static void main(String[] args) {
        // Crear la tabla de empleados (si no existe)
        DatabaseManager.crearTablaEmpleados();

        // Guardar un empleado en la base de datos
        Empleado empleado1 = new Empleado("1323674312", "Juan Perez", "6 horas", "Caja", 300.0);
        empleado1.guardarEnBaseDatos();

        // Guardar otro empleado
        Empleado empleado2 = new Empleado("1187654431", "Maria Rodriguez", "8 horas", "Bar", 450.0);
        empleado2.guardarEnBaseDatos();

        // Guardar un empleado en la base de datos
        Empleado empleado3 = new Empleado("1467341272", "Mateo Herrera", "4 horas", "Mesero", 250.0);
        empleado3.guardarEnBaseDatos();

        // Guardar otro empleado
        Empleado empleado4 = new Empleado("1021654432", "Abby Sanchez", "6 horas", "Bar", 380.0);
        empleado4.guardarEnBaseDatos();

        // Guardar otro empleado
        Empleado empleado5 = new Empleado("2187654472", "Luis Diaz", "4 horas", "Mesero", 250.0);
        empleado5.guardarEnBaseDatos();

        // Consultar la base de datos para verificar si los empleados se guardaron correctamente

        // Ejemplo de consulta para obtener todos los empleados
        System.out.println("Empleados en la base de datos:");
        Empleado.consultarEmpleados().forEach(System.out::println);
    }
}
