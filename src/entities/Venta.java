


import java.time.LocalDate;


public class Venta {

    private String cliente;

    private int idVenta;

    private LocalDate date;

    private double totalamount;

    private String sucursal;

    private String vendedor;

    private String lote;
    public Venta(String cliente, LocalDate date, double totalamount, String sucursal, String vendedor, String lote,int idVenta) {
        this.cliente = cliente;
        this.date = date;
        this.totalamount = totalamount;
        this.sucursal = sucursal;
        this.vendedor = vendedor;
        this.lote = lote;
        this.idVenta = idVenta;

    }

    public Venta(String cliente, LocalDate date, double totalamount, String sucursal, String vendedor, String lote) {
        this.cliente = cliente;
        this.date = date;
        this.totalamount = totalamount;
        this.sucursal = sucursal;
        this.vendedor = vendedor;
        this.lote = lote;

    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public double getTotalamount() {
        return totalamount;
    }

    public void setTotalamount(double totalamount) {
        this.totalamount = totalamount;
    }

    public String getSucursal() {
        return sucursal;
    }

    public void setSucursal(String sucursal) {
        this.sucursal = sucursal;
    }

    public String getVendedor() {
        return vendedor;
    }

    public void setVendedor(String vendedor) {
        this.vendedor = vendedor;
    }

    public String getLote() {
        return lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }

    public int getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
    }




}
