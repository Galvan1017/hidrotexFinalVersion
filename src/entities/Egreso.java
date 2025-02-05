

import java.time.LocalDate;

public class Egreso {

    private String concepto;


    private double cantidad;

    private LocalDate date;

    private String tipoDeGasto;

    private boolean iva;

    public boolean getIva(){
        return iva;
    }


    public String getConcepto() {
        return concepto;
    }

    public double getCantidad() {
        return cantidad;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getTipoDeGasto() {
        return tipoDeGasto;
    }

    public Egreso(String concepto, double cantidad, LocalDate date, String tipoDeGasto) {
        this.concepto = concepto;
        this.cantidad = cantidad;
        this.date = date;
        this.tipoDeGasto = tipoDeGasto;
        iva = false;

    }

    public Egreso(String concepto, double cantidad, LocalDate date, String tipoDeGasto,boolean iva) {
        this.concepto = concepto;
        this.cantidad = cantidad;
        this.date = date;
        this.tipoDeGasto = tipoDeGasto;
        this.iva = iva;
    }

}
