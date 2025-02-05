

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class productManageController {


    @FXML
    protected Text valueText;
    @FXML
    protected TextField productField;
    @FXML
    protected Text name;
    @FXML
    protected Text errMess;

    @FXML
    public void initialize() {
        if((valueText.getText().equals("KG"))) {
            TextFormatter<String> textFormatter = new TextFormatter<>(change -> {
                if (change.getControlNewText().matches("\\d*(\\.\\d{0,2})?")) {
                    return change; // Accept the change
                }
                return null; // Reject the change ^\\d+$
            });
            productField.setTextFormatter(textFormatter);
        }
        else if(valueText.getText().equals("Unidades")) {
            TextFormatter<String> textFormatter = new TextFormatter<>(change -> {
                if (change.getControlNewText().matches("^\\d*$")) {
                    return change; // Accept the change
                }
                return null; // Reject the change
            });
            productField.setTextFormatter(textFormatter);
        }
    }
    @FXML
    private void saveResults() {
        if((valueText.getText().equals("KG"))) {
            addProductKG();
        }else {
            addProductUnits();
        }
    }

    public void addProductKG() {
        if(Double.parseDouble(productField.getText()) ==0.0) {
            return;
        }
        String insertQuery = "UPDATE hidrotex.producto \r\n"
                + "SET `KG` = ? \r\n"
                + "WHERE `Nombre del Producto` = ?";
        String oldValueQuery = "SELECT KG FROM hidrotex.producto WHERE `Nombre del producto` = ?";

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/hidrotex", "Galvan",
                "#Chuy2001");
             PreparedStatement oldStmt = connection.prepareStatement(oldValueQuery);
             PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {

            // Fetch the old value
            oldStmt.setString(1, name.getText());
            ResultSet resultSet = oldStmt.executeQuery();
            double finalOldValue = 0;
            if (resultSet.next()) {
                finalOldValue = resultSet.getDouble("KG");
            }

            // Update the value
            double newUnits = Double.parseDouble(productField.getText()) + finalOldValue;
            insertStmt.setDouble(1, newUnits);
            insertStmt.setString(2, name.getText());

            int rowsAffected = insertStmt.executeUpdate(); // Use executeUpdate for non-SELECT queries

            // Feedback to the user
            if (rowsAffected > 0) {
                errMess.setText("Valor pasó de " + finalOldValue + " a " + newUnits);
                errMess.setFill(Color.GREEN);
                productField.setText("");
            } else {
                errMess.setText("No se pudo actualizar el producto.");
                errMess.setFill(Color.RED);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            errMess.setText("Ocurrió un error en la base de datos.");
            errMess.setFill(Color.RED);
        }
    }


    public void addProductUnits() {
        if(Integer.parseInt(productField.getText())==0) {
            return ;
        }
        String insertQuery = "UPDATE hidrotex.producto \r\n"
                + "SET `Unidades` = ? \r\n"
                + "WHERE `Nombre del Producto` = ?";
        String oldValueQuery = "SELECT Unidades FROM hidrotex.producto WHERE `Nombre del producto` = ?";

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/hidrotex", "Galvan",
                "#Chuy2001");
             PreparedStatement oldStmt = connection.prepareStatement(oldValueQuery);
             PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {

            // Fetch the old value
            oldStmt.setString(1, name.getText());
            ResultSet resultSet = oldStmt.executeQuery();
            int finalOldValue = 0;
            if (resultSet.next()) {
                finalOldValue = resultSet.getInt("Unidades");
            }

            // Update the value
            int newUnits = Integer.parseInt(productField.getText()) + finalOldValue;
            insertStmt.setInt(1, newUnits);
            insertStmt.setString(2, name.getText());

            int rowsAffected = insertStmt.executeUpdate(); // Use executeUpdate for non-SELECT queries

            // Feedback to the user
            if (rowsAffected > 0) {
                errMess.setText("Valor pasó de " + finalOldValue + " a " + newUnits);
                errMess.setFill(Color.GREEN);
                productField.setText("");
            } else {
                errMess.setText("No se pudo actualizar el producto.");
                errMess.setFill(Color.RED);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            errMess.setText("Ocurrió un error en la base de datos.");
            errMess.setFill(Color.RED);
        }
    }



}
