

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;


import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class EgresoAddController {

    @FXML
    private TextField conceptoField;

    @FXML
    private TextField cantidadField;

    @FXML
    private TextField TipoField;

    @FXML
    private Text errorMess;
    @FXML
    private CheckBox iva;

    @FXML
    private Text total;
    @FXML
    private Text totalSign;



    @FXML
    private void initialize() {
        TextFormatter<String> textFormatter = new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("\\d*(\\.\\d{0,2})?")) {
                return change; // Accept the change
            }
            double value = iva.isSelected() ? 1.16:1.00;
            cantidadConIva(value);// Reject the change
            return null;

        });
        cantidadField.setTextFormatter(textFormatter);

        cantidadField.textProperty().addListener((observable, oldValue, newValue)->{
            double value = iva.isSelected() ? 1.16:1.00;
            cantidadConIva(value);// Reject the change

        });
        iva.selectedProperty().addListener((observable, oldValue, newValue) -> {
            double multiplier = newValue ? 1.16 : 1.0; // Si está seleccionado 1.16, si no 1.0

            if(multiplier == 1.16){
                total.setVisible(true);
                totalSign.setVisible(true);
            }else{
                total.setVisible(false);
                totalSign.setVisible(false);
            }

            cantidadConIva(multiplier);
        });
    }

    @FXML
    private void addGasto() {
        //
        String concepto = conceptoField.getText();
        double cantidad = cantidadField.getText().isEmpty() ? 0 : Double.parseDouble(cantidadField.getText());
        String tipo = TipoField.getText();

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

            Egreso egreso = new Egreso(concepto, cantidad, LocalDate.now(), tipo);
            String insertQuery = "INSERT INTO hidrotex.egresos (`Concepto`, `Cantidad`, `Fecha`, `Tipo de Gasto`,IVA) \r\n"
                    + "VALUES (?, ?, ?, ?,?)";

            try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/hidrotex", "Galvan",
                    "#Chuy2001");

                 PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                insertStmt.setString(1, egreso.getConcepto());
                if(iva.isSelected()){
                    insertStmt.setDouble(2, Double.parseDouble(total.getText()));
                }else{
                    insertStmt.setDouble(2, egreso.getCantidad());
                }

                insertStmt.setObject(3, egreso.getDate());
                insertStmt.setString(4, egreso.getTipoDeGasto());
                insertStmt.setBoolean(5,iva.isSelected());
                int rowsAffected = insertStmt.executeUpdate();

                if(rowsAffected>0) {
                    errorMess.setText("Egreso Agregado Con Exito!");
                    errorMess.setFill(Color.GREEN);
                    conceptoField.setText("");
                    cantidadField.setText("");
                    TipoField.setText("");

                }else {
                    errorMess.setText("El Egreso No pudo ser agregado");
                    errorMess.setFill(Color.RED);
                }
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void cantidadConIva(double multiplier){

        if(!cantidadField.getText().isEmpty()){
            BigDecimal value = new BigDecimal(Double.parseDouble(cantidadField.getText())*multiplier);
            BigDecimal roundedValue = value.setScale(2, RoundingMode.HALF_UP);
            total.setText(String.valueOf(roundedValue));
        }


    }

}
