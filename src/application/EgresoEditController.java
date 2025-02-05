

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


import javafx.fxml.FXML;

import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class EgresoEditController {
    @FXML
    protected TextField conceptoField;
    @FXML
    protected TextField cantidadField;

    @FXML
    protected TextField tipoField;

    @FXML
    private Text errorMess;

    int egresoId = 0;

    String egresoQuery;


    @FXML
    protected CheckBox iva;

    @FXML
    protected Text ivaText;

    @FXML
    protected Text ivaValue;







    @FXML
    private void initialize() {
        if(!iva.isSelected()){
            ivaText.setVisible(false);
            ivaValue.setVisible(false);
        }


        TextFormatter<String> textFormatter = new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("\\d*(\\.\\d{0,2})?")) {
                return change; // Accept the change
            }
            double value = iva.isSelected() ? 1.16:1.00;
            cantidadConIva(value);
            return null; // Reject the change
        });
        cantidadField.setTextFormatter(textFormatter);

        cantidadField.textProperty().addListener((observable, oldValue, newValue)->{
            double value = iva.isSelected() ? 1.16:1.00;
            cantidadConIva(value);// Reject the change

        });

        iva.selectedProperty().addListener((observable, oldValue, newValue) -> {
            double multiplier = newValue ? 1.16 : 1.0; // Si está seleccionado 1.16, si no 1.0

            if(multiplier == 1.16){
                ivaValue.setVisible(true);
                ivaText.setVisible(true);
            }else{
                ivaValue.setVisible(false);
                ivaText.setVisible(false);
            }

            cantidadConIva(multiplier);
        });


    }
    private void cantidadConIva(double multiplier){

        if(!cantidadField.getText().isEmpty()){
            BigDecimal value = new BigDecimal(Double.parseDouble(cantidadField.getText())*multiplier);
            BigDecimal roundedValue = value.setScale(2, RoundingMode.HALF_UP);
            ivaValue.setText(String.valueOf(roundedValue));
        }


    }

    protected void locateEgreso() {
        egresoQuery = conceptoField.getText();

        String query = "SELECT idEgresos FROM hidrotex.egresos WHERE Concepto = ?";

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/hidrotex", "Galvan",
                "#Chuy2001"); PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setString(1, egresoQuery);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                egresoId = rs.getInt("idEgresos"); // Retrieve idCliente as an int

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void updateEgreso() {
        String concepto = conceptoField.getText();
        double cantidad = cantidadField.getText().isEmpty() ? 0 : Double.parseDouble(cantidadField.getText());
        String tipo = tipoField.getText();

        if (concepto.isEmpty()) {
            errorMess.setText("El concepto esta vacio!");
            errorMess.setFill(Color.RED);

            return;
        } else if (cantidad == 0) {
            errorMess.setText("No se introdujo una cantidad");
            errorMess.setFill(Color.RED);
        } else if (tipo.isEmpty()) {
            errorMess.setText("El Tipo de Gasto esta vacio esta vacio!");
            errorMess.setFill(Color.RED);
            return;
        } else {
            String checkQuery = "SELECT idEgresos FROM hidrotex.egresos WHERE Concepto = ?";
            String updateQuery = "UPDATE hidrotex.egresos SET Concepto = ?, Cantidad = ?, `Tipo de Gasto` = ?, iva = ? WHERE idEgresos = ? ";
            try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/hidrotex", "Galvan",
                    "#Chuy2001");
                 PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
                 PreparedStatement pstmt = connection.prepareStatement(updateQuery)) {

                checkStmt.setString(1, concepto);
                ResultSet resultSet = checkStmt.executeQuery();

                if (resultSet.next() && resultSet.getInt("idEgresos") != egresoId) {
                    errorMess.setText("EL EGRESO YA EXISTE");
                    errorMess.setFill(Color.RED);
                } else {
                    pstmt.setString(1, concepto);
                    if(iva.isSelected()){
                        pstmt.setDouble(2, Double.parseDouble(ivaValue.getText()));
                    }else{
                        pstmt.setDouble(2, cantidad);
                    }

                    pstmt.setString(3, tipo);
                    pstmt.setBoolean(4,iva.isSelected());
                    pstmt.setInt(5,egresoId);

                    int rowsAffected = pstmt.executeUpdate();

                    if (rowsAffected > 0) {
                        errorMess.setText("EGRESO ACTUALIZADO CON EXITO");
                        errorMess.setFill(Color.GREEN);

                    } else {
                        errorMess.setText("EGRESO NO PUDO SER ACTUALIZADO");
                        errorMess.setFill(Color.RED);

                    }
                }

            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

    }

}
