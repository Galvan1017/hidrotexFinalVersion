

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

public class ventaSeeController {

    @FXML
    private TableColumn<Void, Integer> idVenta;
    @FXML
    private TableColumn<Venta, String> cliente;

    @FXML
    private TableColumn<Venta, Date> fecha;

    @FXML
    private TableColumn<Venta, String> sucursal;
    @FXML
    private TableColumn<Venta, String> vendedor;
    @FXML
    private TableColumn<Venta, String> lote;
    @FXML
    private TableColumn<Venta, Void> ver;
    @FXML
    private TableColumn<Venta, Void> eliminar;

    @FXML
    private TableView<Venta> tableview;
    @FXML
    private TableColumn<Venta, Double> cantidad;

    @FXML
    private TextField filterField;

    @FXML
    private Pane detailsPane;

    @FXML
    private Button closeButton;
    private ObservableList<Venta> ventaList = FXCollections.observableArrayList();

    @FXML
    private DatePicker start;

    @FXML
    private DatePicker end;

    @FXML
    private Text descargaMess;

    @FXML
    private Button dwButton;


    @FXML
    private void initialize() {

        cliente.setCellValueFactory(new PropertyValueFactory<>("cliente"));
        fecha.setCellValueFactory(new PropertyValueFactory<>("date"));
        sucursal.setCellValueFactory(new PropertyValueFactory<>("sucursal"));
        vendedor.setCellValueFactory(new PropertyValueFactory<>("vendedor"));
        lote.setCellValueFactory(new PropertyValueFactory<>("lote"));
        cantidad.setCellValueFactory(new PropertyValueFactory<>("totalamount"));
        idVenta.setCellValueFactory(new PropertyValueFactory<>("idVenta"));

        addDeleteBT();
        addSeeBT();

        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
//
            searchSale();
        });
        loadSalesData();
    }

    public void searchSale() {
        String venta = filterField.getText();

        List<Venta> filteredSales = getSalesByName(venta);
        ventaList.setAll(filteredSales);
        tableview.setItems(ventaList);
    }

    private List<Venta> getSalesByName(String sale) {
        List<Venta> sales = new ArrayList<Venta>();
        String filterQuery = "SELECT * FROM hidrotex.venta WHERE `Cliente_idCliente` IN ("
                + "SELECT `idCliente`FROM hidrotex.cliente "
                + "WHERE `Nombre del Cliente` LIKE ?) AND Activo = true ";

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/hidrotex", "Galvan",
                "#Chuy2001"); PreparedStatement pstmt = connection.prepareStatement(filterQuery)) {

            pstmt.setString(1, "%" + sale + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Venta s = new Venta(getClientName(connection, rs.getInt("Cliente_idCliente")),
                        rs.getDate("Fecha").toLocalDate(), rs.getDouble("total"), rs.getString("Sucursal"),
                        rs.getString("Vendedor"), rs.getString("Lote"), rs.getInt("idVenta"));

                sales.add(s);

            }

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return sales;

    }

    private void loadSalesData() {
        String query = "SELECT `idVenta`,`Cliente_idCliente`,`Fecha`,`Sucursal`,`Vendedor`,`Lote`,`total` FROM hidrotex.venta WHERE Activo = true";
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/hidrotex", "Galvan",
                "#Chuy2001"); Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Venta venta = new Venta(getClientName(connection, rs.getInt("Cliente_idCliente")),
                        rs.getDate("Fecha").toLocalDate(), rs.getDouble("total"), rs.getString("Sucursal"),
                        rs.getString("Vendedor"), rs.getString("Lote"), rs.getInt("idVenta"));

                ventaList.add(venta);

            }

            tableview.setItems(ventaList);

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private String getClientName(Connection con, int id) {
        String searchQuery = "SELECT `Nombre del Cliente` FROM hidrotex.cliente WHERE `idCliente` = ?";
        try {
            PreparedStatement st = con.prepareStatement(searchQuery);
            st.setInt(1, id);
            st.execute();

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Nombre del Cliente");
                }
            }

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    private void addDeleteBT() {
        Callback<TableColumn<Venta, Void>, TableCell<Venta, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Venta, Void> call(final TableColumn<Venta, Void> param) {
                return new TableCell<>() {
                    private final Button btn = new Button("Editar");

                    {
                        btn.setOnAction(event -> {
                            Venta venta = getTableView().getItems().get(getIndex());
                            try {
                                saleDeleate(venta);
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

        eliminar.setCellFactory(cellFactory);
    }

    private void addSeeBT() {
        Callback<TableColumn<Venta, Void>, TableCell<Venta, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Venta, Void> call(final TableColumn<Venta, Void> param) {
                return new TableCell<>() {
                    private final Button btn = new Button("Ver");

                    {
                        btn.setOnAction(event -> {
                            Venta venta = getTableView().getItems().get(getIndex());
                            try {
                                saleDetails(venta);
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

        ver.setCellFactory(cellFactory);
    }


    private void saleDeleate(Venta venta) throws IOException{
        detailsPane.getChildren().clear();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("saleDeleatePane.fxml"));
        Pane newContent = loader.load();
        closeButton.setVisible(true);
        detailsPane.setVisible(true);
        detailsPane.getChildren().clear();
        detailsPane.getChildren().add(newContent);
        detailsPane.getChildren().add(new Label ("."));

        ventaDeleateController dr = loader.getController();
        dr.vendedorField.setText(venta.getVendedor());
        dr.sucursalField.setText(venta.getSucursal());
        dr.loteField.setText(venta.getLote());
        dr.saleText.setText(""+venta.getIdVenta());
    }



    private void saleDetails(Venta venta) throws IOException  {
        detailsPane.getChildren().clear();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("saleEditPane.fxml"));
        Pane newContent = loader.load();
        closeButton.setVisible(true);
        detailsPane.setVisible(true);
        detailsPane.getChildren().clear();
        detailsPane.getChildren().add(newContent);
        detailsPane.getChildren().add(new Label ("."));

        saleEditController sr = loader.getController();
        sr.clientText.setText(venta.getCliente());
        sr.fechaText.setText(venta.getDate().toString());
        sr.sucursalText.setText(venta.getSucursal());
        sr.loteText.setText(venta.getLote());
        sr.totalText.setText(""+venta.getTotalamount());
        sr.vendedorText.setText(venta.getVendedor());
        sr.id.setText(""+venta.getIdVenta());

        sr.getSaleId = venta.getIdVenta();
        sr.initialize();
    }
    @FXML
    private void closeExtraPane() {
        detailsPane.setVisible(false);
        closeButton.setVisible(false);
        detailsPane.getChildren().clear();
        ventaList.clear();
        loadSalesData();

    }
    @FXML
    private void generateCSVfile() {
        String formattedStartDate = "";
        String formattedEndDate = "";

        String query = "";
        if(start.getValue() == null && end.getValue() == null){ //PASSED
            query = "SELECT idVenta,`Nombre del Cliente`,`Fecha`," +
                    "`Sucursal`,`Vendedor`,`Lote`,`total`" +
                    "FROM hidrotex.venta " +
                    "JOIN hidrotex.cliente ON `Cliente_idCliente` = `idCliente`" +
                    "WHERE hidrotex.venta.Activo = true";

        }else if (start.getValue() != null &&  start.getValue().isAfter(LocalDate.now())){ //PASSED
            descargaMess.setText("Fecha Inicial Invalida");
            descargaMess.setFill(Color.RED);
            return;
        }else if(start.getValue() == null && end.getValue() != null){ //PASSED
            descargaMess.setText("Fecha Inicial vacia");
            descargaMess.setFill(Color.RED);
            return;
        }  else if(start.getValue() != null && end.getValue() == null){
            formattedStartDate = start.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            formattedEndDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            query = "SELECT idVenta,`Nombre del Cliente`,`Fecha`," +
                    "`Sucursal`,`Vendedor`,`Lote`,`total`" +
                    "FROM hidrotex.venta " +
                    "JOIN hidrotex.cliente ON `Cliente_idCliente` = `idCliente`" +
                    " WHERE Fecha BETWEEN '"+formattedStartDate+ "' AND '"+formattedEndDate+"?'  AND hidrotex.venta.Activo  = true";
        } else if (!start.getValue().isBefore(end.getValue()) && !start.getValue().isEqual(end.getValue())) { //PASSED
            descargaMess.setText("Rango de Fechas incorrecto");
            descargaMess.setFill(Color.RED);
            return;
        }else{
            formattedStartDate = start.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            formattedEndDate = end.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            query = "SELECT idVenta,`Nombre del Cliente`,`Fecha`," +
                    "`Sucursal`,`Vendedor`,`Lote`,`total`" +
                    "FROM hidrotex.venta " +
                    "JOIN hidrotex.cliente ON `Cliente_idCliente` = `idCliente`" +
                    " WHERE Fecha BETWEEN '"+formattedStartDate+ "' AND '"+formattedEndDate+"?'  AND hidrotex.venta.Activo  = true";

        }


        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("ventas");

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivo CSV (*.csv)", "*.csv")
        );
        LocalDate today =LocalDate.now();
        fileChooser.setInitialFileName("ventas"+today.getDayOfMonth() +"_" +today.getMonth() +"_" + today.getYear()+ ".csv");
        Stage stage = (Stage) dwButton.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            writeSalesToCSV(file,query);
        }
    }


    private void writeSalesToCSV(File file,String query) {



        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(query);
             BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {



            ResultSet rs = stmt.executeQuery(query);
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            List<String> columnNames = new ArrayList<>();
            if(!rs.isBeforeFirst()){
                descargaMess.setText("NINGUNA VENTA REGISTRADA EN ESE PERIODO");
                descargaMess.setFill(Color.RED);
                return ;
            }

            for (int i = 1; i <= columnCount; i++) {
                columnNames.add(metaData.getColumnName(i));
            }
            columnNames.add("IVA");

            writer.write(String.join(",", columnNames));
            writer.newLine();

            int id = 0;
            while (rs.next()) {
                List<String> row = new ArrayList<>();
                for (int i = 1; i <= columnCount; i++) {
                    String value = rs.getString(i);
                    if(i == 1){
                        id = rs.getInt(i);
                    }
                    row.add(value == null ? "NULL" : value.replace(",", ""));
                }
                String hasIva = ventaSum(id) == productVentaSum(id) ? "SIN IVA" :"CON IVA";
                row.add(hasIva);
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



    private double ventaSum(int id){
        String query = "SELECT total FROM hidrotex.venta WHERE idVenta = ?";
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(query);){
            stmt.setInt(1,id);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }
    private double productVentaSum(int id){
        String query = "SELECT SUM(\n" +
                "    CASE \n" +
                "\t\tWHEN `Cantidad KG`> 0 THEN PrecioUnitario *`Cantidad KG`\n" +
                "        WHEN `Cantidad Unitaria` > 0 THEN PrecioUnitario *`Cantidad Unitaria`\n" +
                "        ELSE 0\n" +
                "\tEND) AS \"Suma Total\"\n" +
                "  FROM hidrotex.producto_has_venta WHERE Venta_idVenta = ?";

        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(query);){
            stmt.setInt(1,id);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

}


