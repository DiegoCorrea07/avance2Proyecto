import main.java.RegistroEntradaSalida;
import main.java.SueldoRegistro;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class Ventana1 {
    private JTabbedPane tabbedPane1;
    private JPanel panel1;
    private JTextField textField1;
    private JButton marcarEntradaButton;
    private JButton marcarSalidaButton;
    private JButton verInformeDiarioButton;
    private JButton verInformeSemanalButton;
    private JList list1;
    private JLabel imagenLabel;
    private RegistroEntradaSalida system;
    public Ventana1() {
        system = new RegistroEntradaSalida();
        // Configurar el modelo de la lista
        DefaultListModel<String> modeloLista = new DefaultListModel<>();
        list1.setModel(modeloLista);

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
        verInformeSemanalButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
    }

    private void marcarEntrada() {
        String cedula = validarCedula(textField1.getText());
        String mensaje = RegistroEntradaSalida.marcarEntrada(cedula);
        JOptionPane.showMessageDialog(null, mensaje);
        textField1.setText("");
    }

    private void marcarSalida() {
        String cedula = validarCedula(textField1.getText());
        String mensaje = RegistroEntradaSalida.marcarSalida(cedula);
        JOptionPane.showMessageDialog(null, mensaje);
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
    private String validarCedula(String cedula) {
        if (!cedula.matches("\\d{10}")) {
            // Muestra un mensaje indicando que la cédula no es válida y retorna un valor indicativo
            JOptionPane.showMessageDialog(null, "Ingrese un número de cédula válido (10 dígitos).");
            return null; // O retorna un valor indicativo de cédula no válida
        }
        return cedula;
    }
    
    public static void main(String[] args) {
        JFrame frame = new JFrame("Ventana1");
        frame.setContentPane(new Ventana1().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setSize(800, 700);
        frame.setVisible(true);
    }
}
