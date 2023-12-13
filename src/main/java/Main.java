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
        Empleado empleado1 = new Empleado("13456789", "Juan Perez", 100, "6 horas", "Caja", 1000.0);
        empleado1.guardarEnBaseDatos();

        // Guardar otro empleado
        Empleado empleado2 = new Empleado("876544", "Maria Rodriguez", 2, "8 horas", "Bar", 1200.0);
        empleado2.guardarEnBaseDatos();

        // Puedes seguir agregando más empleados y realizando otras acciones...

        // Consultar la base de datos para verificar si los empleados se guardaron correctamente
        // (Puedes imprimir los resultados o utilizar algún método de consulta)

        // Ejemplo de consulta para obtener todos los empleados
        System.out.println("Empleados en la base de datos:");
        Empleado.consultarEmpleados().forEach(System.out::println);
    }
}
