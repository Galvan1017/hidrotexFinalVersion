

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class saleEditController {

    @FXML
    protected TableColumn<ListItem, String> productName;
    @FXML
    protected TableColumn<ListItem, Double> unitPrice;
    @FXML
    protected TableColumn<ListItem, Cantidad> cantidad;
    @FXML
    protected TableColumn<ListItem, Double> total;

    @FXML
    protected Text clientText;
    @FXML
    protected Text sucursalText;
    @FXML
    protected Text vendedorText;
    @FXML
    protected Text fechaText;
    @FXML
    protected Text loteText;
    @FXML
    protected Text totalText;
    @FXML
    private Text dwText;

    @FXML
    protected Text id;

    @FXML
    private Button dwButton;

    protected int getSaleId;

    private ObservableList<ListItem> itemList = FXCollections.observableArrayList();

    @FXML
    private TableView<ListItem> tableview;




    protected void initialize() {
        productName.setCellValueFactory(new PropertyValueFactory<>("name"));
        unitPrice.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        cantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        total.setCellValueFactory(new PropertyValueFactory<>("total"));


        List<ListItem> li = locateValues();

        itemList.setAll(li);
        tableview.setItems(itemList);

    }

    private List<ListItem> locateValues() {
        List<ListItem> listItem = new ArrayList<>();
        String query = "SELECT * FROM hidrotex.producto_has_venta WHERE Venta_idVenta = ?";
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/hidrotex", "Galvan",
                "#Chuy2001"); PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setInt(1, getSaleId);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {

                Number x = getProductType(rs.getInt("Producto_idProducto"), connection).equals("U&%T") ? rs.getInt("Cantidad Unitaria") : rs.getDouble("Cantidad KG");
                Cantidad can = new Cantidad(x);
                ListItem item = new ListItem(getProductName(rs.getInt("Producto_idProducto"), connection), rs.getDouble("PrecioUnitario"), can,
                        getProductType(rs.getInt("Producto_idProducto"), connection).equals("U&%T") ? true : false);
                listItem.add(item);
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }
        return listItem;
    }

    private String getProductName(int productID, Connection con) {
        String query = "SELECT `Nombre del producto` FROM hidrotex.producto WHERE idProducto = ?";
        try {
            PreparedStatement pstmt = con.prepareStatement(query);
            pstmt.setInt(1, productID);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {


                return rs.getString(1);
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "";
    }

    private String getProductType(int productID, Connection con) {
        String query = "SELECT `Tipo` FROM hidrotex.producto WHERE idProducto = ?";
        try {
            PreparedStatement pstmt = con.prepareStatement(query);
            pstmt.setInt(1, productID);
            ResultSet rs = pstmt.executeQuery();
            String res = "";
            while (rs.next()) {
                res = rs.getString(1);

            }

            if (res.contains("U&%T")) {
                return "U&%T";
            } else {
                return "K&%G";
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "";
    }
    @FXML
    private void generateTXTFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("venta");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivo TXT (*.txt)", "*.txt")
        );

        fileChooser.setInitialFileName("venta_"+clientText.getText()+"_"+fechaText.getText()+ ".txt");
        Stage stage = (Stage) dwButton.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            handleDownloadButton(file);
        }
    }

    private void handleDownloadButton(File file ) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            double subtotal =0;
            writer.write("----------------------------------\n");
            writer.write("Venta numero : " + id.getText() + "\n");
            writer.write("Fecha: " + fechaText.getText() + "\n");
            writer.write("Nombre del cliente: " + clientText.getText() + "\n");
            if(!getRFC(clientText.getText()).isEmpty() && getRFC(clientText.getText()) !=null ){
                writer.write("RFC: " + getRFC(clientText.getText()) + "\n");
            }
            writer.write("Sucursal: " + sucursalText.getText() + "\n");
            writer.write("Lote: " + loteText.getText() + "\n");

            double total = Double.parseDouble(totalText.getText());
            double totalDesglose = calcularTotalDesglose();
            for (ListItem item : itemList) {
                subtotal += item.getTotal();
            }
            writer.write("Precio Subtotal: $" + String.format("%.2f", subtotal) + "\n");
            writer.newLine();
            writer.write("Precio Total: $" + String.format("%.2f", total) + "\n");


            if (total > totalDesglose) {
                writer.write("Venta con IVA.\n");
            }

            writer.write("Lista de Productos:\n");
            for (ListItem item : itemList) {
                writer.write(" - " + item.getName() +"  Precio Unitario: $"+ String.format("%.2f", item.getPrecioUnitario()) +" Cantidad Adquirida: " +item.getCantidad() + " Total: $" + String.format("%.2f", item.getTotal()) + "\n");
            }
            writer.write("----------------------------------\n");
            dwText.setText("Descarga Realizada con exito");
            dwText.setFill(Color.GREEN);

        } catch (IOException  e) {
            e.printStackTrace();
            System.out.println("Error al escribir el archivo.");
        }
    }

    private String getRFC(String clientName){
        String query = "SELECT rfc FROM hidrotex.Cliente WHERE `Nombre del Cliente` = ? ";
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/hidrotex", "Galvan",
                "#Chuy2001"); PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setString(1,clientName);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                return rs.getString(1);
            }
            return "";

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    private double calcularTotalDesglose() {
        double totalDesglose = 0.0;
        for (ListItem item : itemList) {
            totalDesglose += item.getTotal();
        }
        return totalDesglose;
    }




}
