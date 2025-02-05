

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class ClienteEditController {
    @FXML
    protected TextField clientNameField;
    @FXML
    protected TextField clientPhoneField;
    @FXML
    protected TextField clientMailField;

    @FXML
    protected TextField clientRFCField;

    @FXML
    private Text errorMess;
    String nameQuery;

    int clientId = 0;
    @FXML
    protected ComboBox<Boolean> activeBox;

    @FXML
    private void initialize() {


        TextFormatter<String> textFormatter = new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("^[a-zA-Z0-9.@ ]*$")) {
                return change; // Accept the change
            }
            return null; // Reject the change
        });
        clientMailField.setTextFormatter(textFormatter);
        TextFormatter<String> textFormatter2 = new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("^\\d+$")) {
                return change; // Accept the change
            }
            return null; // Reject the change
        });
        clientPhoneField.setTextFormatter(textFormatter2);
        TextFormatter<String> textFormatter3 = new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("^[a-zA-Z0-9 ]*$")) {
                return change; // Accept the change
            }
            return null; // Reject the change
        });
        clientRFCField.setTextFormatter(textFormatter3);
    }



    protected void locateClient() {
        nameQuery = clientNameField.getText();
        activeBox.getItems().addAll(true, false);


        String query = "SELECT idCliente FROM hidrotex.cliente WHERE `Nombre del Cliente` = ?";

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/hidrotex", "Galvan",
                "#Chuy2001"); PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setString(1,nameQuery );
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                clientId = rs.getInt("idCliente"); // Retrieve idCliente as an int

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void saveClient() {
        String name = clientNameField.getText();

        String phone = clientPhoneField.getText();

        String mail = clientMailField.getText();

        String rfc = clientRFCField.getText().isBlank() ? "":clientRFCField.getText().toUpperCase();
        boolean isActive =activeBox.getValue();
        if(name.isEmpty()) {
            errorMess.setText("NO INCLUISTE NOMBRE DEL CLIENTE");
            errorMess.setFill(Color.RED);
        }
        else if(!phone.matches("\\d{10,}")) {
            if(phone.isEmpty()) {
                errorMess.setText("EL TELEFONO ESTA EN BLANCO");
                errorMess.setFill(Color.RED);
            }else {
                errorMess.setText("EL TELEFONO ES INCORRECTO");
                errorMess.setFill(Color.RED);
            }

        }else if(!mail.isEmpty() && !mail.matches("^[\\w\\.-]+@[\\w\\.-]+\\.[a-zA-Z]{2,}$")) {
            errorMess.setText("EL MAIL INTRODUCIDO ES INCORECTO");
            errorMess.setFill(Color.RED);
        }else if(!rfc.isEmpty() && (rfc.length()>13 || rfc.length()<12)) {
            errorMess.setText("EL RFC INTRODUCIDO ES INCORECTO");
            errorMess.setFill(Color.RED);

        }else {

            String checkQuery = "SELECT idCliente FROM hidrotex.cliente WHERE `Nombre del Cliente` = ?";
            String updateQuery = "UPDATE hidrotex.cliente SET `Nombre del Cliente` = ?, Telefono = ?, Correo = ?, rfc = ?,Activo = ? WHERE idCliente = ?";

            try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/hidrotex", "Galvan",
                    "#Chuy2001");
                 PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
                 PreparedStatement pstmt = connection.prepareStatement(updateQuery)) {

                checkStmt.setString(1, name);
                ResultSet resultSet = checkStmt.executeQuery();


                if (resultSet.next() && resultSet.getInt("idCliente") != clientId) {
                    errorMess.setText("EL CLIENTE YA EXISTE");
                    errorMess.setFill(Color.RED);
                } else {

                    pstmt.setString(1, name);
                    pstmt.setString(2, phone);
                    pstmt.setString(3, mail);
                    pstmt.setString(4, rfc);
                    pstmt.setBoolean(5, isActive);
                    pstmt.setInt(6, clientId);
                    int rowsAffected = pstmt.executeUpdate();

                    if (rowsAffected > 0) {
                        errorMess.setText("CLIENTE ACTUALIZADO CON EXITO");
                        errorMess.setFill(Color.GREEN);


                    } else {
                        errorMess.setText("CLIENTE NO PUDO SER ACTUALIZADO");
                        errorMess.setFill(Color.RED);

                    }

                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

        }


    }
}
