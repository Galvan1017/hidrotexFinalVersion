

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class ProductAddController {

    @FXML
    private TextField nombreDelProducto;

    @FXML
    private TextField tipoDelProducto;

    @FXML
    private TextField colorDelProducto;

    @FXML
    private TextField precioDelProducto;

    @FXML
    private ComboBox<String> medicionDelProducto;

    @FXML
    private TextField stockDelProducto;

    @FXML
    private Text errorMess;
    @FXML
    private Text textval;

    @FXML
    public void initialize() {
        medicionDelProducto.getItems().addAll("KG", "Unidades");
        medicionDelProducto.setValue("Unidades");
        TextFormatter<String> textFormatter = new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("\\d*(\\.\\d{0,2})?")) {
                return change; // Accept the change
            }
            return null; // Reject the change \\d*(\\.\\d{0,2})?
        });
        TextFormatter<String> textFormatter2 = new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("\\d*(\\.\\d{0,2})?")) {
                return change; // Accept the change
            }
            return null; // Reject the change \\d*(\\.\\d{0,2})?
        });
        stockDelProducto.setTextFormatter(textFormatter);

        precioDelProducto.setTextFormatter(textFormatter2);
    }

    @FXML
    private Product saveProduct() {
        String name = nombreDelProducto.getText();
        String type = tipoDelProducto.getText();

        String color = colorDelProducto.getText();

        double precioUnitario = precioDelProducto.getText().isEmpty() ? 0
                : Double.parseDouble(precioDelProducto.getText());

        String value = medicionDelProducto.getValue();
        if (stockDelProducto == null) {
            stockDelProducto.setText("0");
        }
        if (name.isEmpty()) {
            errorMess.setText("NO INCLUISTE NOMBRE DEL PRODUCTO");
            errorMess.setFill(Color.RED);
        } else if (type.isEmpty()) {
            errorMess.setText("NO INCLUISTE TIPO DEL PRODUCTO");
            errorMess.setFill(Color.RED);
        } else if (type.toUpperCase().contains("K&%G") || type.toUpperCase().contains("U&%T")) {
            errorMess.setText("EVITA USAR CARACTERES INVALIDOS");
            errorMess.setFill(Color.RED);
        } else if (color.isEmpty()) {
            errorMess.setText("NO INCLUISTE COLOR DEL PRODUCTO");
            errorMess.setFill(Color.RED);
        } else if (precioUnitario == 0) {
            errorMess.setText("NO INCLUISTE PRECIO UNITARIO");
            errorMess.setFill(Color.RED);

        }

        else {

            String checkQuery = "SELECT COUNT(*) FROM hidrotex.producto WHERE `Nombre del producto` = ?";

            String insertQuery = "INSERT INTO hidrotex.producto (`Nombre del Producto`, `Tipo`, `Color`, `Precio Unitario`, `KG`, `Unidades`) \r\n"
                    + "VALUES (?, ?, ?, ?, ?, ?)";

            try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/hidrotex", "Galvan",
                    "#Chuy2001");
                 PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
                 PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {

                checkStmt.setString(1, name);
                ResultSet resultSet = checkStmt.executeQuery();
                // K&%G U&%T
                resultSet.next();
                int count = resultSet.getInt(1);

                if (count > 0) {
                    errorMess.setText("EL PRODUCTO YA EXISTE");
                    errorMess.setFill(Color.RED);
                } else {

                    insertStmt.setString(1, name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase());
                    insertStmt.setString(2, type.substring(0, 1).toUpperCase() + type.substring(1).toLowerCase()
                            + (value == "KG" ? " K&%G" : " U&%T"));
                    insertStmt.setString(3, color.substring(0, 1).toUpperCase() + color.substring(1).toLowerCase());
                    insertStmt.setDouble(4, precioUnitario); // Assuming it's a decimal value
                    insertStmt.setDouble(5, (value == "KG" ? Double.parseDouble(stockDelProducto.getText()) : 0));
                    insertStmt.setInt(6, (value == "Unidades" ? doubleToint(stockDelProducto, textval) : 0));
                    insertStmt.executeUpdate();

                    errorMess.setText("PRODCUTO AGREGADO CON EXITO");
                    errorMess.setFill(Color.GREEN);
                    nombreDelProducto.setText("");
                    tipoDelProducto.setText("");
                    colorDelProducto.setText("");
                    precioDelProducto.setText("");
                    stockDelProducto.setText("");
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private int doubleToint(TextField field, Text text) {
        double amount = Double.parseDouble(field.getText());

        int value = (int) amount;
        if (amount % 1 != 0) {
            text.setText("El valor " + amount + " fue susituido por " + value);
        }

        return value;

    }

}
