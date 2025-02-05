

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

public class ProductSeeController {

    @FXML
    private TableView<Product> tableview;

    @FXML
    private TextField filterField;

    @FXML
    private TableColumn<Product, String> nombreDelProducto;
    @FXML
    private TableColumn<Product, String> type;
    @FXML
    private TableColumn<Product, String> color;

    @FXML
    private TableColumn<Product, Double> price;

    @FXML
    private TableColumn<Product, Double> kiloG;

    @FXML
    private TableColumn<Product, Integer> units;
    @FXML
    private TableColumn<Product, Void> edit;
    @FXML
    private TableColumn<Product, Void> add;


    @FXML
    private TableColumn<Product, Boolean> isActive;
    @FXML
    private Pane extraPane;

    @FXML
    private Button closeButton;

    private ObservableList<Product> productList = FXCollections.observableArrayList();

    @FXML
    private Text descargaMess;

    @FXML
    private Button dwButton;

    @FXML
    public void initialize() {

        nombreDelProducto.setCellValueFactory(new PropertyValueFactory<>("nombreDelProducto"));
        type.setCellValueFactory(new PropertyValueFactory<>("type"));
        color.setCellValueFactory(new PropertyValueFactory<>("color"));
        price.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        kiloG.setCellValueFactory(new PropertyValueFactory<>("kg"));
        units.setCellValueFactory(new PropertyValueFactory<>("units"));
        isActive.setCellValueFactory(new PropertyValueFactory<>("activo"));
        editButton();
        addButton();

        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchProduct();
        });

        loadProuctData();
    }

    @FXML
    public void searchProduct() {
        String product = filterField.getText();
        List<Product> filteredProducts = getProducts(product);

        productList.setAll(filteredProducts);
        tableview.setItems(productList);
    }

    private List<Product> getProducts(String productInfo) {
        List<Product> products = new ArrayList<>();
        String query = "SELECT * FROM hidrotex.producto WHERE `Nombre del producto` LIKE ? OR `Tipo` LIKE ? OR `Color` LIKE ?";

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/hidrotex", "Galvan",
                "#Chuy2001"); PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setString(1, "%" + productInfo + "%");
            pstmt.setString(2, "%" + productInfo + "%");
            pstmt.setString(3, "%" + productInfo + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Product product = new Product(rs.getString("Nombre del producto"), rs.getString("Tipo"),
                        rs.getString("Color"), rs.getDouble("Precio Unitario"), rs.getDouble("KG"),
                        rs.getInt("Unidades"),
                        rs.getBoolean("Activo"));
                products.add(product);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return products;
    }

    public void loadProuctData() {
        String query = "SELECT `Nombre del producto`, Tipo, Color, `Precio Unitario`,KG,Unidades,Activo FROM hidrotex.producto";

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/hidrotex", "Galvan",
                "#Chuy2001"); Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {

            productList.clear();

            while (rs.next()) {
                Product product = new Product(rs.getString("Nombre del producto"), rs.getString("Tipo"),
                        rs.getString("Color"), rs.getDouble("Precio Unitario"), rs.getDouble("KG"),
                        rs.getInt("Unidades"),rs.getBoolean("Activo"));
                productList.add(product);
            }

            tableview.setItems(productList);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addButton() {
        Callback<TableColumn<Product, Void>, TableCell<Product, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Product, Void> call(final TableColumn<Product, Void> param) {
                return new TableCell<>() {
                    private final Button btn = new Button("Agregar");

                    {
                        btn.setOnAction(event -> {
                            Product product = getTableView().getItems().get(getIndex());
                            try {

                                openProductAddPane(product);
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
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

        add.setCellFactory(cellFactory);
    }

    private void editButton() {
        Callback<TableColumn<Product, Void>, TableCell<Product, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Product, Void> call(final TableColumn<Product, Void> param) {
                return new TableCell<>() {
                    private  final Button btn = new Button("Editar");

                    {
                        btn.setOnAction(event -> {
                            Product product = getTableView().getItems().get(getIndex());
                            try {

                                openProductEditPane(product);
                            } catch (IOException e) {

                                e.printStackTrace();
                            }
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

        edit.setCellFactory(cellFactory);
    }
    private void openProductEditPane(Product product) throws IOException {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("productEditPane.fxml"));
        Pane newContent = loader.load();
        extraPane.setVisible(true);
        extraPane.getChildren().clear();
        extraPane.getChildren().add(newContent);
        extraPane.getChildren().add(new Label ("."));

        ProductEditController cr = loader.getController();
        cr.nombreDelProducto.setText(product.getNombreDelProducto());
        cr.tipoDelProducto.setText(product.getType().replace("K&%G", "").replace("U&%T", ""));
        cr.colorDelProducto.setText(product.getColor());
        cr.precioDelProducto.setText(""+product.getPrecioUnitario());

        String value = product.getType().contains("K&%G") ? "KG":"Unidades";
        cr.stockDelProducto.setText(""+ (value == "KG" ? product.getKg():product.getUnits()));
        cr.locateProduct();
        cr.medicionDelProducto.getItems().addAll("KG", "Unidades");
        cr.medicionDelProducto.setValue(value);
        cr.activeField.setValue(product.isActivo());
        closeButton.setVisible(true);

    }

    private void openProductAddPane(Product product) throws IOException {
        closeButton.setVisible(true);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("productManagePane.fxml"));
        Pane newContent = loader.load();
        extraPane.setVisible(true);
        extraPane.getChildren().clear();
        extraPane.getChildren().add(newContent);
        extraPane.getChildren().add(new Label ("."));

        productManageController cr = loader.getController();
        cr.name.setText(product.getNombreDelProducto());
        cr.valueText.setText(product.getType().contains("K&%G") ? "KG" : "Unidades" );

        cr.initialize();
    }

    @FXML
    private void close() {
        extraPane.setVisible(false);
        closeButton.setVisible(false);
        extraPane.getChildren().clear();
        loadProuctData();
    }
    @FXML
    private void generateCSVfile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("producto");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivo CSV (*.csv)", "*.csv")
        );
        LocalDate today =LocalDate.now();
        fileChooser.setInitialFileName("producto"+today.getDayOfMonth() +"_" +today.getMonth() +"_" + today.getYear()+ ".csv");
        Stage stage = (Stage) dwButton.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            writeProductToCsv(file);
        }
    }



    private void writeProductToCsv(File file) {
        String query = "SELECT `Nombre del producto`, Tipo, Color, `Precio Unitario`, KG, Unidades FROM hidrotex.producto WHERE Activo = true";

        try (Connection connection = getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query);
             BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {


            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            List<String> columnNames = new ArrayList<>();

            for (int i = 1; i <= columnCount; i++) {
                columnNames.add(metaData.getColumnName(i));
            }


            writer.write(String.join(",", columnNames));
            writer.newLine();


            while (rs.next()) {
                List<String> row = new ArrayList<>();
                for (int i = 1; i <= columnCount; i++) {
                    String value = rs.getString(i);

                    row.add(value == null ? "NULL" : value.replace(",", ""));
                }
                writer.write(String.join(",", row));
                writer.newLine();
            }


            //comment
            descargaMess.setText("Descarga realizada con exito");
            descargaMess.setFill(Color.GREEN);

        } catch (SQLException e) {
            System.err.println("Error de base de datos: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error al escribir el archivo: " + e.getMessage());
        }
    }

    private Connection getConnection() throws SQLException {

        String url = "jdbc:mysql://localhost:3306/hidrotex";
        String user = "Galvan";
        String password = "#Chuy2001";
        return DriverManager.getConnection(url, user, password);
    }



}
