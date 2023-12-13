package main.java;

import java.sql.Date;

public class SueldoRegistro {
    private int id;
    private String cedulaEmpleado;
    private Date fecha;
    private double horasTrabajadas;
    private int multa;
    private int bono;
    private double sueldo;

    public SueldoRegistro(int id, String cedulaEmpleado, Date fecha, double horasTrabajadas, int multa, int bono, double sueldo) {
        this.id = id;
        this.cedulaEmpleado = cedulaEmpleado;
        this.fecha = fecha;
        this.horasTrabajadas = horasTrabajadas;
        this.multa = multa;
        this.bono = bono;
        this.sueldo = sueldo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCedulaEmpleado() {
        return cedulaEmpleado;
    }

    public void setCedulaEmpleado(String cedulaEmpleado) {
        this.cedulaEmpleado = cedulaEmpleado;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public double getHorasTrabajadas() {
        return horasTrabajadas;
    }

    public void setHorasTrabajadas(double horasTrabajadas) {
        this.horasTrabajadas = horasTrabajadas;
    }

    public int getMulta() {
        return multa;
    }

    public void setMulta(int multa) {
        this.multa = multa;
    }

    public int getBono() {
        return bono;
    }

    public void setBono(int bono) {
        this.bono = bono;
    }

    public double getSueldo() {
        return sueldo;
    }

    public void setSueldo(double sueldo) {
        this.sueldo = sueldo;
    }

    @Override
    public String toString() {
        return "ID: " + id +
                ", Cedula Empleado: " + cedulaEmpleado +
                ", Fecha: " + fecha +
                ", Horas Trabajadas: " + horasTrabajadas +
                ", Multa: " + multa +
                ", Bono: " + bono +
                ", Sueldo: " + sueldo;
    }

}
