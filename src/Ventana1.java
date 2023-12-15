import main.java.Administrador;
import main.java.Empleado;
import main.java.RegistroEntradaSalida;
import main.java.SueldoRegistro;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class Ventana1 {
    private JTabbedPane tabbedPane1;
    private JPanel panel1;
    private JTextField textField1;
    private JButton marcarEntradaButton;
    private JButton marcarSalidaButton;
    private JButton verInformeDiarioButton;
    private JList list1;
    private JLabel imagenLabel;
    private JTabbedPane tabbedPane2;
    private JButton agregarButton;
    private JTextField textField2;
    private JTextField textField3;
    private JTextField textField4;
    private JTextField textField5;
    private JComboBox comboBox1;
    private JTextField textField6;
    private JButton eliminarButton;
    private JTextField textField7;
    private JTextField textField8;
    private JButton agregarMultaButton;
    private JTextField textField9;
    private JButton agregarBonoButton;
    private JTextField textField10;
    private JButton verHistorialDeAsistenciaButton;
    private JTable table1;
    private RegistroEntradaSalida system;
    private Administrador admin;
    private DefaultTableModel tableModel;

    public Ventana1() {
        system = new RegistroEntradaSalida();
        admin = new Administrador();

        // Configurar el modelo de la lista
        DefaultListModel<String> modeloLista = new DefaultListModel<>();
        list1.setModel(modeloLista);

        // Configuración del modelo de la tabla
        String[] columnNames = {"Cédula Empleado", "Nombre Empleado", "Fecha", "Hora Entrada", "Hora Salida", "Horas Trabajadas"};
        tableModel = new DefaultTableModel(columnNames, 0 );
        table1.setModel(tableModel);

        // Cargar la imagen JPEG
        String rutaImagen = "kobe2.jpeg";
        ImageIcon icono = new ImageIcon(rutaImagen);
        Image imagen = icono.getImage().getScaledInstance(300, 300, Image.SCALE_DEFAULT);
        icono = new ImageIcon(imagen);
        imagenLabel.setIcon(icono);
        marcarEntradaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                marcarEntrada();
            }
        });

        marcarSalidaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                marcarSalida();
            }
        });
        verInformeDiarioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                verInformeDiario();
            }
        });
        agregarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Obtener los valores de los campos
                String cedula = validarCedula(textField2.getText());
                String nombre = textField3.getText();
                String cargo = comboBox1.getSelectedItem().toString();
                String direccion = textField4.getText();
                String salarioTexto = textField5.getText();

                // Validar que ningún campo esté vacío
                if (cedula == null || nombre.isEmpty() || direccion.isEmpty() || salarioTexto.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Todos los campos son obligatorios.");
                    return;
                }
                // Validar el nombre
                if (!validarCampoTxt(nombre)) {
                    return;
                }
                // Validar el Área de trabajo
                if (!validarCampoTxt(direccion)) {
                    return;
                }

                // Validar que el salario sea un número válido
                try {
                    double salario = Double.parseDouble(salarioTexto);
                    // Agregar el empleado a la base de datos
                    Empleado empleado = new Empleado(cedula, nombre, cargo, direccion, salario);
                    String mensaje = admin.guardarEnBaseDatos(empleado);
                    JOptionPane.showMessageDialog(null, mensaje);
                    // Limpiar los campos después de agregar
                    textField2.setText("");
                    textField3.setText("");
                    comboBox1.setSelectedIndex(0);
                    textField4.setText("");
                    textField5.setText("");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Ingrese un salario válido (número).");
                }
            }
        });
        eliminarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String cedula = validarCedula(textField6.getText());

                // Verificar que la cédula tenga 10 dígitos antes de intentar eliminar al empleado
                if (cedula != null) {
                    // Intentar eliminar al empleado y capturar posibles excepciones
                    try {
                        String mensaje = admin.eliminarEmpleado(cedula);
                        JOptionPane.showMessageDialog(null, mensaje);
                        textField1.setText("");
                    } catch (SQLException ex) {
                        // Manejar la excepción específica (No se encontró ningún empleado con la cédula proporcionada)
                        if (ex.getMessage().contains("No se encontró ningún empleado")) {
                            JOptionPane.showMessageDialog(null, "No se encontró ningún empleado con la cédula proporcionada.");
                        } else {
                            // Manejar otras excepciones SQLException
                            JOptionPane.showMessageDialog(null, "Error al eliminar empleado y registros relacionados.");
                        }
                    }
                }
                textField6.setText("");
            }
        });
        agregarMultaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String cedulaEmpleado = validarCedula(textField7.getText());
                String montoT = textField8.getText();

                    if (cedulaEmpleado == null || montoT.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "Ingrese la cédula y el monto de la multa.");
                        return;
                    }
                    try {
                        int multa = Integer.parseInt(montoT);
                        if (multa <= 0) {
                            throw new NumberFormatException();
                        }
                        LocalDate fecha = LocalDate.now();
                        String mensaje = admin.agregarMultas(cedulaEmpleado, fecha, multa);
                        JOptionPane.showMessageDialog(null, mensaje);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(null, "Ingrese un monto válido.");
                    }

                textField7.setText("");
                textField8.setText("");
            }
        });
        agregarBonoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Obtener la información del formulario
                String cedulaEmpleado = validarCedula(textField9.getText());
                String montoBonoTexto = textField10.getText();

                if (cedulaEmpleado == null || montoBonoTexto.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Ingrese la cédula y el monto del bono.");
                    return;
                }

                try {
                    // Convertir el monto del bono a entero
                    int montoBono = Integer.parseInt(montoBonoTexto);

                    // Se llama al método agregarBonos del Administrador
                    String mensaje = admin.agregarBonos(cedulaEmpleado, LocalDate.now(), montoBono);
                    JOptionPane.showMessageDialog(null, mensaje);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Ingrese un monto válido (número entero).");
                }
                textField9.setText("");
                textField10.setText("");
            }
        });
        verHistorialDeAsistenciaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RegistroEntradaSalida.actualizarHorasTrabajadasParaTodos();
                historial();
            }
        });
    }
    private void marcarEntrada() {
        String cedula = validarCedula(textField1.getText());
        String mensaje = RegistroEntradaSalida.marcarEntrada(cedula);
        JOptionPane.showMessageDialog(null, mensaje);
        textField1.setText("");
        DefaultListModel<String> modeloVacio = new DefaultListModel<>();
        list1.setModel(modeloVacio);
    }

    private void marcarSalida() {
        String cedula = validarCedula(textField1.getText());
        String mensaje = RegistroEntradaSalida.marcarSalida(cedula);
        JOptionPane.showMessageDialog(null, mensaje);
        textField1.setText("");
        DefaultListModel<String> modeloVacio = new DefaultListModel<>();
        list1.setModel(modeloVacio);
    }
    private void verInformeDiario() {
        String cedula = validarCedula(textField1.getText());
        List<SueldoRegistro> sueldos = RegistroEntradaSalida.obtenerSueldosPorCedula(cedula);

        // Verificar si la lista de sueldos está vacía
        if (sueldos.isEmpty()) {
            // Si está vacía, mostrar un mensaje predeterminado
            DefaultListModel<String> model = new DefaultListModel<>();
            model.addElement("Sin informe disponible");
            list1.setModel(model);
        } else {
            // Si hay elementos en la lista, crear un modelo normal
            DefaultListModel<SueldoRegistro> model = new DefaultListModel<>();
            for (SueldoRegistro sueldo : sueldos) {
                model.addElement(sueldo);
            }
            list1.setModel(model);
        }
    }
    private void historial() {
        // Obtener listado de asistencia en formato de tabla
        List<Object[]> sueldosEnTabla = RegistroEntradaSalida.obtenerSueldosEnFormatoTablaOrdenado();
        actualizarTabla(sueldosEnTabla);
    }

    private String validarCedula(String cedula) {
        if (!cedula.matches("\\d{10}")) {
            // Muestra un mensaje indicando que la cédula no es válida
            JOptionPane.showMessageDialog(null, "Ingrese un número de cédula válido (10 dígitos).");
            return null;
        }
        return cedula;
    }
    private boolean validarCampoTxt(String nombre) {
        if (!nombre.matches("^[A-Za-z ]+$")) {
            // Muestra un mensaje indicando que la validación falló
            JOptionPane.showMessageDialog(null, "Ingrese datos válidos.");
            return false;
        }
        return true;
    }

    public void actualizarTabla(List<Object[]> data) {
        // Limpiar el modelo existente
        tableModel.setRowCount(0);
        for (Object[] row : data) {
            tableModel.addRow(row);
        }
        // Notificar a la tabla que el modelo ha cambiado
        tableModel.fireTableDataChanged();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Ventana1");
        frame.setContentPane(new Ventana1().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setSize(900, 700);
        frame.setVisible(true);
    }
}
