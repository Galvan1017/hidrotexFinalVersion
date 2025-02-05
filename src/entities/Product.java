

public class Product {

    private String nombreDelProducto;

    private String type;

    private String color;

    private double precioUnitario;

    private double kg;

    private boolean activo;


    private int units;

    public Product(String nombreDelProducto, String type, String color, double precioUnitario, double kg, int units,boolean activo) {
        this.nombreDelProducto = nombreDelProducto;
        this.type = type;
        this.color = color;
        this.precioUnitario = precioUnitario;
        this.kg = kg;
        this.units = units;
        this.activo = activo;
    }

    public Product(String nombreDelProducto, String type, String color, double precioUnitario, double kg, int units) {
        this.nombreDelProducto = nombreDelProducto;
        this.type = type;
        this.color = color;
        this.precioUnitario = precioUnitario;
        this.kg = kg;
        this.units = units;
    }

    public Product(String nombreDelProducto, String type, String color, double precioUnitario, int units) {
        this(nombreDelProducto,type,color,precioUnitario,0,units);
    }

    public Product(String nombreDelProducto, String type, String color, double precioUnitario, double kg) {
        this(nombreDelProducto,type,color,precioUnitario,kg,0);

    }

    public String getNombreDelProducto() {
        return nombreDelProducto;
    }

    public String getType() {

        return type;
    }

    public String getColor() {
        return color;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public double getKg() {
        return kg;
    }

    public int getUnits() {
        return units;
    }
    public boolean isActivo() {
        return activo;
    }
    public void setActivo(boolean activo) {
        this.activo = activo;
    }












}
