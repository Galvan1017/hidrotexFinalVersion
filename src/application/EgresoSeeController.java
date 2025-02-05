

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
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

public class EgresoSeeController {

    @FXML
    private TextField filterField;
    @FXML
    private TableView<Egreso> tableview;

    @FXML
    private TableColumn<Egreso,String> concepto;

    @FXML
    private TableColumn<Egreso,Double> cantidad;
    @FXML
    private TableColumn<Egreso,Date> date;
    @FXML
    private TableColumn<Egreso,String> tipoDeGasto;
    @FXML
    private TableColumn<Egreso,Void> editar;
    @FXML
    private TableColumn<Egreso,Void> eliminar;

    @FXML
    private TableColumn<Egreso,Boolean> iva;

    @FXML
    private Button dwButton;
    @FXML
    private Text descargaMess;

    @FXML
    private Pane editPane;
    @FXML
    private Button closeEditButton;

    @FXML
    private DatePicker start;

    @FXML
    private DatePicker end;


    private ObservableList<Egreso> li = FXCollections.observableArrayList();


    @FXML
    public void initialize() {
        concepto.setCellValueFactory(new PropertyValueFactory<>("concepto"));
        cantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        date.setCellValueFactory(new PropertyValueFactory<>("date"));
        tipoDeGasto.setCellValueFactory(new PropertyValueFactory<>("tipoDeGasto"));
        iva.setCellValueFactory(new PropertyValueFactory<>("iva"));
        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchEgreso();
        });
        addButton();
        deleateButton();
        loadData();

    }
    @FXML
    private void searchEgreso() {
        String egreso = filterField.getText();
        List<Egreso> filteredEgresos = getEgresos(egreso);
        li.setAll(filteredEgresos);
        tableview.setItems(li);

    }
    private List<Egreso> getEgresos(String info){
        List<Egreso> egresos = new ArrayList<>();
        String query = "SELECT * FROM hidrotex.egresos WHERE (`Concepto` LIKE ? OR `Tipo de Gasto` LIKE ?) AND Activo = true ";

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/hidrotex", "Galvan",
                "#Chuy2001"); PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, "%" + info + "%");
            pstmt.setString(2, "%" + info + "%");

            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                Egreso egreso = new Egreso(rs.getString("Concepto"),rs.getDouble("Cantidad"),rs.getDate("Fecha").toLocalDate(),rs.getString("Tipo de Gasto"),rs.getBoolean("IVA"));
                egresos.add(egreso);
            }

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }



        return egresos;
    }

    private void loadData() {
        String query = "SELECT Concepto,Cantidad, Fecha, `Tipo de Gasto`,IVA FROM hidrotex.egresos WHERE Activo = true";

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/hidrotex", "Galvan",
                "#Chuy2001"); Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            li.clear();
            while(rs.next()) {
                Egreso egreso = new Egreso(rs.getString("Concepto"),rs.getDouble("Cantidad"),rs.getDate("Fecha").toLocalDate(),rs.getString("Tipo de Gasto"),rs.getBoolean("IVA"));
                li.add(egreso);
            }
            tableview.setItems(li);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    private void addButton() {
        Callback<TableColumn<Egreso, Void>, TableCell<Egreso, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Egreso, Void> call(final TableColumn<Egreso, Void> param) {
                return new TableCell<>() {
                    private final Button btn = new Button("Editar");

                    {
                        btn.setOnAction(event -> {
                            Egreso egreso = getTableView().getItems().get(getIndex());
                            try {
                                openEgresosEditPane(egreso);
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

        editar.setCellFactory(cellFactory);

    }

    private void openEgresosEditPane(Egreso egreso)throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("egresoEditPane.fxml"));
        Pane newContent = loader.load();
        editPane.setVisible(true);
        editPane.getChildren().clear();
        editPane.getChildren().add(newContent);
        editPane.getChildren().add(new Label("."));

        EgresoEditController er = loader.getController();
        String val = egreso.getCantidad()+"";
        er.cantidadField.setText(val);
       if(egreso.getIva()){
            val = ""+egreso.getCantidad();
            er.ivaValue.setText(val);
            val = (egreso.getCantidad()/1.16)+"";
            er.cantidadField.setText(val);
            er.iva.setSelected(true);

        }
        er.conceptoField.setText(egreso.getConcepto());
        er.tipoField.setText(egreso.getTipoDeGasto());
        er.locateEgreso();
        closeEditButton.setVisible(true);
        descargaMess.setText("");

    }
    @FXML
    private void closeEdit() {
        editPane.setVisible(false);
        closeEditButton.setVisible(false);
        editPane.getChildren().clear();
        loadData();
    }
    @FXML
    private void generateCSVfile() {
        String formattedStartDate = "";
        String formattedEndDate = "";

        String query = "";
        if(start.getValue() == null && end.getValue() == null){ //PASSED
            query = "SELECT Concepto, Cantidad, Fecha, `Tipo de Gasto`,IVA FROM hidrotex.egresos WHERE Activo = true";
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
            query = "SELECT  Concepto, Cantidad, Fecha, `Tipo de Gasto` FROM hidrotex.egresos " +
                    " WHERE Fecha BETWEEN '"+formattedStartDate+ "' AND '"+formattedEndDate+"?'  AND Activo = true";

        } else if (!start.getValue().isBefore(end.getValue()) && !start.getValue().isEqual(end.getValue())) { //PASSED
            descargaMess.setText("Rango de Fechas incorrecto");
            descargaMess.setFill(Color.RED);
            return;
        }else{
            formattedStartDate = start.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            formattedEndDate = end.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            query = "SELECT  Concepto, Cantidad, Fecha, `Tipo de Gasto` FROM hidrotex.egresos " +
                    " WHERE Fecha BETWEEN '"+formattedStartDate+ "' AND '"+formattedEndDate+"?'  AND Activo = true";

        }


        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("egresos");

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivo CSV (*.csv)", "*.csv")
        );
        LocalDate today =LocalDate.now();
        fileChooser.setInitialFileName("egresos"+today.getDayOfMonth() +"_" +today.getMonth() +"_" + today.getYear()+ ".csv");
        Stage stage = (Stage) dwButton.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            writeEgresoToCSV(file,query);
        }
    }


    private void writeEgresoToCSV(File file,String query) {


        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(query);
             BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
            ResultSet rs = stmt.executeQuery(query);
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            List<String> columnNames = new ArrayList<>();
            if(!rs.isBeforeFirst()){
                descargaMess.setText("NINGUN EGRESO REGISTRADO EN ESE PERIODO");
                descargaMess.setFill(Color.RED);
                return ;
            }
            for (int i = 1; i <= columnCount; i++) {
                columnNames.add(metaData.getColumnName(i));
            }


            writer.write(String.join(",", columnNames));
            writer.newLine();
            while (rs.next()) {
                List<String> row = new ArrayList<>();
                for (int i = 1; i <= columnCount; i++) {
                    String value = rs.getString(i);
                    if(i==5){
                        row.add((value.contains("1") ? "CON IVA" : "SIN IVA"));
                    }else{
                        row.add(value == null ? "NULL" : value.replace(",", ""));
                    }

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


    private void deleateButton() {
        Callback<TableColumn<Egreso, Void>, TableCell<Egreso, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Egreso, Void> call(final TableColumn<Egreso, Void> param) {
                return new TableCell<>() {
                    private final Button btn = new Button("Eliminar");

                    {
                        btn.setOnAction(event -> {
                            Egreso egreso = getTableView().getItems().get(getIndex());
                            deleateEgreso(egreso);
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


    private void deleateEgreso(Egreso egreso){
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Eliminar Egreso");
        confirmAlert.setHeaderText("Estas seguro que quieres eliminar el Egreso");
        confirmAlert.setContentText("Esta accion no se puede revertir");


        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
               deleate(egreso);
               loadData();
            }
        });
    }

    private void deleate(Egreso egreso){
        String query = "UPDATE hidrotex.egresos SET Activo = false WHERE Concepto = ?";
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/hidrotex", "Galvan",
                "#Chuy2001"))
        {
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1,egreso.getConcepto());
            pstmt.execute();


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
