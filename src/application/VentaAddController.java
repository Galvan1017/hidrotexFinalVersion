


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.paint.Color;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.util.Callback;

public class VentaAddController {

    @FXML
    private ComboBox<String> clientComboBox;
    @FXML
    private Text clientErrMess;

    @FXML
    private TextField vendedorField;

    @FXML
    private TextField sucursalField;

    @FXML
    private TextField loteField;
    @FXML
    private ComboBox<String> productComboBox;

    @FXML
    private TextField cantidadField;

    @FXML
    private Text productAddMess;

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

    private List<String> clientNames = new ArrayList<String>();
    private List<String> productNames = new ArrayList<String>();

    @FXML
    private CheckBox iva;
    private double total = 0;
    @FXML
    private Text totalText;
    private boolean hasIva = false;


    @FXML
    private Text finalText;

    private boolean isUpdating = false;

    @FXML
    void initialize() {
        clientComboBox.setEditable(true);
        productComboBox.setEditable(true);
        loadClients("");
        clientComboBox.hide();

        loadProducts("");
        productComboBox.hide();
        clientComboBox.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (isUpdating)
                return; // Evita conflictos
            isUpdating = true;
            loadClients(newValue);
            isUpdating = false; // Filter based on user input
        });
        productComboBox.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (isUpdating)
                return; // Evita conflictos
            isUpdating = true;
            loadProducts(newValue);
            isUpdating = false; // Filter based on user input
        });

        TextFormatter<String> textFormatter = new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("\\d*(\\.\\d{0,2})?")) {
                return change; // Accept the change
            }
            return null; // Reject the change
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
        });
    }

    private void ventaConIva(double multiplier) {
        BigDecimal value = new BigDecimal(total*multiplier);
        BigDecimal roundedValue = value.setScale(2, RoundingMode.HALF_UP);
        totalText.setText("$"+roundedValue);


    }

    private void loadClients(String filter) {
        String query = "SELECT `Nombre del Cliente` FROM hidrotex.cliente WHERE `Nombre del Cliente` LIKE ? AND Activo = true";


        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/hidrotex", "Galvan",
                "#Chuy2001"); PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setString(1, "%" + filter + "%"); // Use the filter for a partial match
            ResultSet rs = pstmt.executeQuery();

            ObservableList<String> clientList = FXCollections.observableArrayList();
            while (rs.next()) {
                clientList.add(rs.getString(1));
                clientNames.add(rs.getString(1));
            }

            clientComboBox.setItems(clientList);

            clientComboBox.getEditor().setText(filter);

            if (!clientList.isEmpty()) {
                clientComboBox.show();
            }

        } catch (SQLException e) {
            clientErrMess.setText("Establece un valor valido");
            clientErrMess.setFill(Color.RED);
        }
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

    @FXML
    private void addProduct() {


        finalText.setText("");
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
        boolean add = false;

        if (productValue.contains("U&%T")) {
            units = getUnitsValue(selectedProduct);
            if(Integer.parseInt(cantidadField.getText())<1) {
                productAddMess.setText("La cantidad no puede ser menor a 1");
                return ;
            }
            add = canAdd(units);
        } else if (productValue.contains("K&%G")) {
            KG = getKGValue(selectedProduct);
            if(Double.parseDouble(cantidadField.getText())<0) {
                productAddMess.setText("La cantidad no puede ser 0");
                return ;
            }
            add = canAdd(KG);
        }

        if (!add) {
            productAddMess.setText("La cantidad máxima es: " + (productValue.contains("U&%T") ? units : KG));
            productAddMess.setFill(Color.RED);
            return;
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

    private <T> T executeQuery(String query, String parameter, SQLFunction<ResultSet, T> resultExtractor) {
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

    private boolean canAdd(double kg) {
        double value = Double.parseDouble(cantidadField.getText());
        if (kg - value >= 0) {

            return true;
        }
        return false;
    }

    private boolean canAdd(int units) {
        int value = (int) Double.parseDouble(cantidadField.getText());
        if (units - value >= 0) {

            return true;
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

    private int getIDClient(String value) {
        String query = "SELECT `idCliente` FROM hidrotex.cliente WHERE `Nombre del Cliente` = ?";
        Integer result = executeQuery(query, value, rs -> rs.getInt(1));
        return result != null ? result : 0;
    }
    @FXML
    private void generarVenta() {
        //Verificar esta parte
        if(!clientNames.contains(clientComboBox.getValue())) {
            finalText.setFill(Color.RED);
            finalText.setText("Nombre incorrecto");
            return;
        }

        if (productList.isEmpty()) {
            finalText.setFill(Color.RED);
            finalText.setText("No hay ningun producto en la lista ");
            return;
        }

        Venta venta = new Venta(clientComboBox.getValue(), LocalDate.now(), total, sucursalField.getText(),
                vendedorField.getText(), loteField.getText());

        String finalQuery = "INSERT INTO hidrotex.venta (`Cliente_idCliente`, `Fecha`, `Sucursal`, `Vendedor`, `Lote`,`total`) \r\n"
                + "VALUES (?, ?, ?, ?, ?,?)";

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/hidrotex", "Galvan", "#Chuy2001")) {
            connection.setAutoCommit(false);

            try (PreparedStatement insertStmt = connection.prepareStatement(finalQuery, Statement.RETURN_GENERATED_KEYS)) {

                insertStmt.setInt(1, getIDClient(venta.getCliente()));
                insertStmt.setObject(2, venta.getDate());
                insertStmt.setString(3, venta.getSucursal());
                insertStmt.setString(4, venta.getVendedor());
                insertStmt.setString(5, venta.getLote());
                double val = hasIva ? 1.16:1;
                insertStmt.setDouble(6, total*val);
                insertStmt.executeUpdate();

                try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int ventaId = generatedKeys.getInt(1);

                        for (ListItem product : productList) {
                            updateProducts(product, ventaId, connection);
                        }
                    } else {
                        throw new SQLException("Error al generar la venta. No se pudo obtener el ID.");
                    }
                }

                connection.commit();
                finalText.setText("Venta realizada con exito!");
                finalText.setFill(Color.GREEN);

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        loteField.setText("");
        clientComboBox.setValue("");
        vendedorField.setText("");
        productList.setAll();
        cantidadField.setText("");
        sucursalField.setText("");
        totalText.setText("$0.0");

        tableview.setItems(FXCollections.observableArrayList(productList));
    }

    private void updateProducts(ListItem item, int ventaId, Connection connection) {
        String secondFinalQuery = "INSERT INTO hidrotex.producto_has_venta (`Producto_idProducto`, `Venta_idVenta`, `Cantidad KG`, `Cantidad Unitaria`, `PrecioUnitario`) \r\n"
                + "VALUES (?, ?, ?, ?, ?)";

        int productId = getIDproduct(item.getName());
        String typeOfProduct = getType(item.getName());

        Cantidad cantidad = item.getCantidad();
        Cantidad productAmount = new Cantidad(
                typeOfProduct.contains("U&%T") ? getUnitsValue(item.getName()) : getKGValue(item.getName()));

        updateStock(productId, typeOfProduct, cantidad, productAmount, connection);

        try (PreparedStatement insertStmt = connection.prepareStatement(secondFinalQuery)) {
            insertStmt.setInt(1, productId);
            insertStmt.setInt(2, ventaId);
            insertStmt.setDouble(3, typeOfProduct.contains("U&%T") ? 0 : cantidad.asDouble());
            insertStmt.setInt(4, typeOfProduct.contains("U&%T") ? cantidad.asInteger() : 0);
            insertStmt.setDouble(5, item.getPrecioUnitario());
            insertStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getIDproduct(String productName) {
        String query = "SELECT `idProducto` FROM hidrotex.producto WHERE `Nombre del producto` = ?";
        Integer result = executeQuery(query, productName, rs -> rs.getInt(1));
        return result != null ? result : 0;
    }

    private void updateStock(int productId, String type, Cantidad cant, Cantidad prevAmount, Connection connection) {
        String unitsQuery = "UPDATE hidrotex.producto SET `Unidades` = ? WHERE `idProducto` = ?";
        String kgQuery = "UPDATE hidrotex.producto SET `KG` = ? WHERE `idProducto` = ?";

        try (PreparedStatement updateStmt = connection.prepareStatement(type.contains("U&%T") ? unitsQuery : kgQuery)) {
            if (type.contains("U&%T")) {
                updateStmt.setInt(1,  (prevAmount.asInteger()-cant.asInteger() ));
            } else {
                updateStmt.setDouble(1, (prevAmount.asDouble()-cant.asDouble()));
            }
            updateStmt.setInt(2, productId);
            updateStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Continuar


}
