
public class ListItem {

    private String name;

    private double precioUnitario;

    private Cantidad cantidad;

    double total;

    public ListItem(String name, double precioUnitario, Cantidad cantidad,boolean isUnit) {
        this.name = name;
        this.precioUnitario = precioUnitario;
        this.cantidad = cantidad;
        this.total = precioUnitario*cantidad.asInteger();
    }

    public ListItem(String name, double precioUnitario, Cantidad cantidad) {
        this.name = name;
        this.precioUnitario = precioUnitario;
        this.cantidad = cantidad;
        this.total = precioUnitario*cantidad.asDouble();
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setCantidad(Cantidad cantidad) {
        this.cantidad = cantidad;
    }

    public String getName() {
        return name;
    }

    public Cantidad getCantidad() {
        return cantidad;
    }



}
