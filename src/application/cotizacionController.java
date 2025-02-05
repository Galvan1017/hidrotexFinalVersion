import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class cotizacionController {

    @FXML
    private TextField clienteField;

    @FXML
    private TextField sucursalField;

    @FXML
    private DatePicker endDate;

    @FXML
    private TextArea comments;

    @FXML
    private ComboBox<String> productComboBox;

    @FXML
    private TextField cantidadField;

    @FXML
    private TableView<ListItem> tableview;

    @FXML
    private TableColumn<ListItem, String> productVenta;

    @FXML
    private TableColumn<ListItem, Double> precioUVenta;

    @FXML
    private TableColumn<ListItem, Cantidad> precioCVenta;

    @FXML
    private TableColumn<ListItem, Double> totalVenta;

    @FXML
    private TableColumn<ListItem, Void> eliminarItem;

    private ObservableList<ListItem> productList = FXCollections.observableArrayList();
    private List<String> productNames = new ArrayList<String>();

    //Ver que HACE ITEMLIT
    @FXML
    private Text productAddMess;
    @FXML
    private CheckBox iva;
    private double total = 0;
    @FXML
    private Text totalText;
    private boolean hasIva = false;
    private boolean isUpdating = false;
    @FXML
    private Button dwButton;

    @FXML
    private Text finalText;

    @FXML
    void initialize(){
        productComboBox.setEditable(true);
        loadProducts("");
        productComboBox.hide();
        productComboBox.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (isUpdating)
                return;
            isUpdating = true;
            loadProducts(newValue);
            isUpdating = false;
        });
        TextFormatter<String> textFormatter = new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("\\d*(\\.\\d{0,2})?")) {
                return change;
            }
            return null;
        });
        cantidadField.setTextFormatter(textFormatter);
        productVenta.setCellValueFactory(new PropertyValueFactory<>("name"));
        precioUVenta.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        precioCVenta.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        totalVenta.setCellValueFactory(new PropertyValueFactory<>("total"));
        deleteButton();

        iva.selectedProperty().addListener((observable, oldValue, newValue) -> {
            double multiplier = newValue ? 1.16 : 1.0; // Si está seleccionado 1.16, si no 1.0
            if(multiplier == 1.16) {
                hasIva = true;
            }else {
                hasIva = false;
            }
            ventaConIva(multiplier);
        });// Filter based on user input

    }

    private void loadProducts(String filter) {
        String query = "SELECT `Nombre del producto` FROM hidrotex.producto WHERE `Nombre del producto` LIKE ? AND Activo = true";

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/hidrotex", "Galvan",
                "#Chuy2001"); PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setString(1, "%" + filter + "%"); // Use the filter for a partial match
            ResultSet rs = pstmt.executeQuery();

            ObservableList<String> productList = FXCollections.observableArrayList();
            while (rs.next()) {
                productList.add(rs.getString(1)); // Add each matching client to the list
                productNames.add(rs.getString(1));
            }

            productComboBox.setItems(productList);

            // Keep the user's input in the editor
            productComboBox.getEditor().setText(filter);

            if (!productList.isEmpty()) {
                productComboBox.show();
            }

        } catch (SQLException e) {

        }
    }
    private void deleteButton() {

        Callback<TableColumn<ListItem, Void>, TableCell<ListItem, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<ListItem, Void> call(final TableColumn<ListItem, Void> param) {
                return new TableCell<>() {

                    private final Button btn = new Button("Eliminar");

                    {

                        btn.setOnAction(event -> {

                            ListItem listItem = getTableView().getItems().get(getIndex());
                            total = total - listItem.getTotal();
                            totalText.setText("$" + total);

                            productList.remove(listItem);

                            tableview.refresh();
                            tableview.setItems(FXCollections.observableArrayList(productList));
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btn);
                        }
                    }
                };
            }
        };

        eliminarItem.setCellFactory(cellFactory);

    }
    private void ventaConIva(double multiplier) {

        totalText.setText("$"+total*multiplier);


    }
    @FXML
    private void addProduct(){
        String selectedProduct = productComboBox.getEditor().getText().trim();

        if (selectedProduct.isEmpty()) {
            productAddMess.setText("Selecciona un producto válido");
            productAddMess.setFill(Color.RED);
            return;
        }
        if(!productNames.contains(selectedProduct)) {
            productAddMess.setText("Selecciona un producto válido");
            productAddMess.setFill(Color.RED);
            return;
        }
        String productValue = getType(selectedProduct);
        int units = 0;
        double KG = 0;

        if(cantidadField.getText().isEmpty()){
            productAddMess.setText("Cantidad Vacia");
            productAddMess.setFill(Color.RED);
            return ;
        }
        if (productValue.contains("U&%T")) {
            units = getUnitsValue(selectedProduct);
            if(Integer.parseInt(cantidadField.getText())<1) {
                productAddMess.setText("La cantidad no puede ser menor a 1");
                return ;
            }

        } else if (productValue.contains("K&%G")) {
            KG = getKGValue(selectedProduct);
            if(Double.parseDouble(cantidadField.getText())<0) {
                productAddMess.setText("La cantidad no puede ser 0");
                return ;
            }

        }
        if (cantidadField.getText().isEmpty() || !cantidadField.getText().matches("\\d+(\\.\\d+)?")) {
            productAddMess.setText("Introduce una cantidad válida");
            productAddMess.setFill(Color.RED);
            return;
        }

        if (checkItem(selectedProduct)) {
            productAddMess.setText("El producto ya está en la lista");
            productAddMess.setFill(Color.RED);
            return;
        }
        Cantidad cantidad = new Cantidad(
                productValue.contains("U&%T") ? doubletoInt(Double.parseDouble(cantidadField.getText()), productAddMess)
                        : Double.parseDouble(cantidadField.getText())); // KG

        ListItem item = new ListItem(selectedProduct, getPriceValue(selectedProduct), cantidad);

        productList.add(item);
        tableview.setItems(FXCollections.observableArrayList(productList));
        total = total
                + (Double.parseDouble(cantidadField.getText()) * getPriceValue(productComboBox.getEditor().getText()));
        productComboBox.getEditor().clear();
        cantidadField.clear();
        productAddMess.setText("");

        totalText.setText("$ " + total);
        if(hasIva) {
            ventaConIva(1.16);
        }
    }
    @FunctionalInterface
    interface SQLFunction<T, R> {
        R apply(T t) throws SQLException;
    }

    private <T> T executeQuery(String query, String parameter, VentaAddController.SQLFunction<ResultSet, T> resultExtractor) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/hidrotex", "Galvan",
                "#Chuy2001"); PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setString(1, parameter);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return resultExtractor.apply(rs); // Extract the desired value
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Default value for failure cases
    }
    private String getType(String value) {
        String query = "SELECT `Tipo` FROM hidrotex.producto WHERE `Nombre del producto` = ?";
        String result = executeQuery(query, value, rs -> rs.getString(1));
        if (result != null) {
            if (result.contains("K&%G")) {
                return "K&%G";
            } else if (result.contains("U&%T")) {
                return "U&%T";
            }
        }
        return null;
    }
    private int getUnitsValue(String value) {
        String query = "SELECT `Unidades` FROM hidrotex.producto WHERE `Nombre del producto` = ?";
        Integer result = executeQuery(query, value, rs -> rs.getInt(1));
        return result != null ? result : 0;
    }

    private double getKGValue(String value) {
        String query = "SELECT `KG` FROM hidrotex.producto WHERE `Nombre del producto` = ?";
        Double result = executeQuery(query, value, rs -> rs.getDouble(1));
        return result != null ? result : 0.0;
    }

    private double getPriceValue(String value) {
        String query = "SELECT `Precio Unitario` FROM hidrotex.producto WHERE `Nombre del producto` = ?";
        Double result = executeQuery(query, value, rs -> rs.getDouble(1));
        return result != null ? result : 0.0;
    }

    private boolean checkItem(String itemName) {
        for (ListItem listItem : tableview.getItems()) {
            if (listItem.getName().equalsIgnoreCase(itemName)) {
                return true;
            }
        }
        return false;
    }
    private int doubletoInt(double value, Text info) {
        int finalValue = (int) value;
        if (value % 1 != 0) {
            info.setText("El valor: " + value + "Fue sustituido por: " + finalValue);
        }

        return finalValue;
    }

    @FXML
    private void generateCSVfile(){
        if(endDate.getValue() != null && !endDate.getValue().isAfter(LocalDate.now())){
            finalText.setText("Fecha Incorrecta");
            finalText.setFill(Color.RED);
            return ;
        }
        if(productList.isEmpty()){
            finalText.setText("Lista Vacia");
            finalText.setFill(Color.RED);
            return ;
        }
        if(clienteField.getText().isEmpty()){
            finalText.setText("Nombre vacio");
            finalText.setFill(Color.RED);
            return ;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("cotizacion");

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivo TXT (*.txt)", "*.txt")
        );
        LocalDate today =LocalDate.now();
        fileChooser.setInitialFileName(clienteField.getText()+"Cotizacion"+today.getDayOfMonth() +
                "_" +today.getMonth() +"_" + today.getYear()+ ".txt");
        Stage stage = (Stage) dwButton.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            writeEgresoToCSV(file);
        }
    }
    private void writeEgresoToCSV(File file) {
        double subtotal = 0;
       try( BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))){
           writer.write("-------------------------------");
           writer.newLine();
           writer.write(clienteField.getText());
           writer.newLine();
           if(!sucursalField.getText().isEmpty()){
               writer.write(sucursalField.getText());
               writer.newLine();
           }
           if(endDate.getValue() != null){
               writer.write(String.valueOf(endDate.getValue()));
               writer.newLine();
           }if(hasIva){
               writer.write("Cotización Con Iva");
               writer.newLine();
           }
           if(!comments.getText().isEmpty()){
               writer.write(comments.getText());
               writer.newLine();
           }
           for (ListItem item : productList) {
               subtotal+= item.getTotal();
           }
           writer.write("$" + subtotal);
           writer.newLine();
           writer.write(totalText.getText());
           writer.newLine();
           for (ListItem item : productList) {
               writer.write(" - " + item.getName() +"  Precio Unitario: $"+ String.format("%.2f", item.getPrecioUnitario()) +" Cantidad Adquirida: " +item.getCantidad() + " Total: $" + String.format("%.2f", item.getTotal()) + "\n");
           }

           writer.write("-------------------------------");



       }catch (IOException e) {
           System.err.println("Error al escribir el archivo: " + e.getMessage());
       }
       finalText.setText("Descarga realizada Con Exito");
       finalText.setFill(Color.GREEN);

       clienteField.setText("");
       sucursalField.setText("");

       comments.setText("");
       totalText.setText("$0.0");
       productList.setAll();
       productList.removeAll();
       tableview.setItems(FXCollections.observableArrayList(productList));



    }
}
