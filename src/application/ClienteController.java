

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

public class ClienteController {
    @FXML
    private TableView<Cliente> tableview;
    @FXML
    private TextField filterField;

    @FXML
    private TableColumn<Cliente, String> nombreDelCliente;
    @FXML
    private TableColumn<Cliente, String> telefonoSee;
    @FXML
    private TableColumn<Cliente, String> correoSee;
    @FXML
    private TableColumn<Cliente,String> rfc;

    @FXML
    private TableColumn<Cliente, Boolean> active;
    @FXML
    private TableColumn<Cliente, Void> editarSee;


    @FXML
    public Pane editPane;

    @FXML
    private Button closeEditButton;

    @FXML
    private Button dwButton;
    @FXML
    private Text descargaMess;


    private ObservableList<Cliente> clientList = FXCollections.observableArrayList();

    public ClienteController() {

    }

    @FXML
    public void initialize() {

        nombreDelCliente.setCellValueFactory(new PropertyValueFactory<>("name"));
        telefonoSee.setCellValueFactory(new PropertyValueFactory<>("phone"));
        correoSee.setCellValueFactory(new PropertyValueFactory<>("mail"));
        rfc.setCellValueFactory(new PropertyValueFactory<>("rfc"));
        active.setCellValueFactory(new PropertyValueFactory<>("activo"));
        addButtonToTable();

        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
//
            search_user();
        });

        loadClientData();
    }

    @FXML
    public void search_user() {
        String name = filterField.getText();
        List<Cliente> filteredClients = getClientsByName(name);

        clientList.setAll(filteredClients);
        tableview.setItems(clientList);
    }

    private List<Cliente> getClientsByName(String name) {
        List<Cliente> clients = new ArrayList<>();
        String query = "SELECT * FROM hidrotex.cliente WHERE `Nombre del Cliente` LIKE ?";

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/hidrotex", "Galvan",
                "#Chuy2001");
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setString(1, "%" + name + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Cliente client = new Cliente(rs.getString("Nombre del Cliente"), rs.getString("Telefono"),
                        rs.getString("Correo"),rs.getString("rfc"),rs.getBoolean("Activo"));
                clients.add(client);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return clients;
    }

    public void loadClientData() {
        String query = "SELECT `Nombre del Cliente`, Telefono, Correo,rfc,Activo FROM hidrotex.cliente";

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/hidrotex", "Galvan",
                "#Chuy2001");
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            clientList.clear();

            while (rs.next()) {
                Cliente client = new Cliente(
                        rs.getString("Nombre del Cliente"),
                        rs.getString("Telefono"),
                        rs.getString("Correo"),
                        rs.getString("rfc"),
                        rs.getBoolean("Activo"));

                clientList.add(client);
            }

            tableview.setItems(clientList);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addButtonToTable() {
        Callback<TableColumn<Cliente, Void>, TableCell<Cliente, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Cliente, Void> call(final TableColumn<Cliente, Void> param) {
                return new TableCell<>() {
                    private final Button btn = new Button("Editar");

                    {
                        btn.setOnAction(event -> {
                            Cliente client = getTableView().getItems().get(getIndex());
                            try {
                                openClientsEditPane(client);
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

        editarSee.setCellFactory(cellFactory);
    }


    private void openClientsEditPane(Cliente client) throws IOException {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("clientsEditPane.fxml"));
        Pane newContent = loader.load();
        editPane.setVisible(true);
        editPane.getChildren().clear();
        editPane.getChildren().add(newContent);
        editPane.getChildren().add(new Label ("."));

        ClienteEditController cr = loader.getController();
        cr.clientNameField.setText(client.getName());

        if(client.getMail() != null) {
            cr.clientMailField.setText(client.getPhone());
        }

        cr.clientPhoneField.setText(client.getMail());
        if(client.getRfc() != null ) {
            cr.clientRFCField.setText(client.getRfc());
        }

        cr.activeBox.setValue(client.isActivo());


        cr.locateClient();
        closeEditButton.setVisible(true);
        descargaMess.setText("");

    }
    @FXML
    private void closeEdit() {
        editPane.setVisible(false);
        closeEditButton.setVisible(false);
        editPane.getChildren().clear();
        loadClientData();
    }

    @FXML
    private void generateCSVfile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("clientes");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivo CSV (*.csv)", "*.csv")
        );
        LocalDate today =LocalDate.now();
        fileChooser.setInitialFileName("clientes"+today.getDayOfMonth() +"_" +today.getMonth() +"_" + today.getYear()+ ".csv");
        Stage stage = (Stage) dwButton.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            writeClientsToCsv(file);
        }
    }



    private void writeClientsToCsv(File file) {
        String query = "SELECT `Nombre del Cliente`, Telefono, Correo, rfc FROM hidrotex.cliente WHERE Activo = true";

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