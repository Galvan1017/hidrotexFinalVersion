

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class ClienteAddController {
    @FXML
    private TextField clientNameField;

    @FXML
    private TextField clientPhoneField;

    @FXML
    private TextField clientMailField;

    @FXML
    private TextField clientRFCField;

    @FXML
    private Button generateClient;


    @FXML
    private Text errorMess;

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


    @FXML
    private Cliente saveClient() {
        String name = clientNameField.getText();

        String phone = clientPhoneField.getText();

        String mail = clientMailField.getText();

        String rfc = clientRFCField.getText().toUpperCase();


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
        }

        //if(isClientValid(name, errorMess) && isPhoneValid(phone, errorMess) && isMailValid(mail, errorMess) && isRFCValid(rfc, errorMess))
        else{


            String checkQuery = "SELECT COUNT(*) FROM hidrotex.cliente WHERE `Nombre del Cliente` = ?";

            String insertQuery = "INSERT INTO hidrotex.cliente (`Nombre del Cliente`, Telefono, Correo, rfc) VALUES (?, ?, ?, ?)";

            try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/hidrotex", "Galvan", "#Chuy2001");
                 PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
                 PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {


                checkStmt.setString(1, name);
                ResultSet resultSet = checkStmt.executeQuery();

                resultSet.next();
                int count = resultSet.getInt(1);

                if (count > 0) {
                    errorMess.setText("EL CLIENTE YA EXISTE");
                    errorMess.setFill(Color.RED);
                } else {

                    insertStmt.setString(1, name);
                    insertStmt.setString(2, phone);
                    insertStmt.setString(3, mail);
                    insertStmt.setString(4, rfc);
                    insertStmt.executeUpdate();

                    errorMess.setText("CLIENTE AGREGADO CON EXITO");
                    errorMess.setFill(Color.GREEN);
                    clientNameField.setText("");
                    clientPhoneField.setText("");
                    clientMailField.setText("");
                    clientRFCField.setText("");
                    return new Cliente(name, mail, phone,rfc);
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

        }


        return null;
    }
    protected boolean isClientValid(String client,Text errMess) {
        if(client.isEmpty() || client==null) {
            errorMess.setText("NO INCLUISTE NOMBRE DEL CLIENTE");
            errorMess.setFill(Color.RED);
            return false;
        }
        return true;
    }
    protected boolean isPhoneValid(String phone,Text errMess) {
        if(phone.isEmpty()) {
            errorMess.setText("EL TELEFONO ESTA EN BLANCO");
            errorMess.setFill(Color.RED);
            return false;
        }else if(!phone.matches("\\d{10,}")){
            errorMess.setText("EL TELEFONO ES INCORRECTO");
            errorMess.setFill(Color.RED);
            return false;
        }
        return true;
    }

    protected boolean isMailValid(String mail,Text errMess) {
        if(!mail.isEmpty() && !mail.matches("^[\\w\\.-]+@[\\w\\.-]+\\.[a-zA-Z]{2,}$")) {
            errorMess.setText("EL MAIL INTRODUCIDO ES INCORECTO");
            errorMess.setFill(Color.RED);
            return false;
        }
        return true;
    }

    protected boolean isRFCValid(String rfc, Text errMess) {
        if(!rfc.isEmpty() && (rfc.length()>13 || rfc.length()<12)) {
            errorMess.setText("EL RFC INTRODUCIDO ES INCORECTO");
            errorMess.setFill(Color.RED);
            return false;
        }
        return false;
    }
}
