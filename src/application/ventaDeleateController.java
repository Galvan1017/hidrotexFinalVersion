
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class ventaDeleateController {

    @FXML
    protected TextField vendedorField;
    @FXML
    protected TextField sucursalField;
    @FXML
    protected TextField loteField;
    @FXML
    protected Text textEdit;
    @FXML
    private TableView<String> tableviewStock;
    @FXML
    private TableColumn<String,String> tableStock;
    @FXML
    private TableView<Double> tableviewStock1;
    @FXML
    private TableColumn<Double,Double> tableStock1;
    @FXML
    private TableColumn<String,String> tableNoStock;

    @FXML
    private TableView<String> tableviewNoStock;
    @FXML
    private TableColumn<Double,Double> tableNoStock1;

    @FXML
    private TableView<Double> tableviewNoStock1;

    @FXML
    protected Text saleText;

    @FXML
    private Button delButton;

    List<String> stockChanged = new ArrayList<>();
    List<Double> stockchanged1 = new ArrayList<>();
    private ObservableList<String> stockList = FXCollections.observableArrayList();
    private ObservableList<Double> stockList1 = FXCollections.observableArrayList();
    private ObservableList<String> noStockList = FXCollections.observableArrayList();
    private ObservableList<Double> noStockList1 = FXCollections.observableArrayList();
    List<String> stockUnchanged = new ArrayList<>();
    List<Double> stockUnchanged1 = new ArrayList<>();

    @FXML
    private void editSale() {
        String query = "UPDATE hidrotex.venta SET Vendedor = ?, Sucursal = ?, Lote = ? WHERE idVenta = ?";
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/hidrotex", "Galvan",
                "#Chuy2001"); PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, vendedorField.getText());
            pstmt.setString(2, sucursalField.getText());
            pstmt.setString(3, loteField.getText());
            pstmt.setInt(4, Integer.parseInt(saleText.getText()));
            pstmt.executeUpdate();
            textEdit.setText("Venta Actualizada Con Exito!");
            textEdit.setFill(Color.GREEN);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @FXML
    private void eliminarVenta() {
        Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
        confirmAlert.setTitle("Eliminar Venta");
        confirmAlert.setHeaderText("Estas seguro que quieres eliminar la venta");
        confirmAlert.setContentText("Esta accion no se puede revertir,recuerda descargar la venta antes de eliminarla");

        // Mostrar y manejar la respuesta del usuario
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                deleateVenta();
            } else {
                return;
            }
        });
    }

    private void deleateVenta() {
        int saleID = Integer.parseInt(saleText.getText());
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/hidrotex", "Galvan",
                "#Chuy2001")) {
            delSale(saleID, connection);
            textEdit.setText("Venta Eliminada, recuerda agregar los productos no permitidos al inventario ");
            textEdit.setFill(Color.ORANGERED);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void delSale(int id, Connection con) {
        String query = "UPDATE hidrotex.venta SET Activo = false WHERE idVenta =?";
        try {
            PreparedStatement pstmt = con.prepareStatement(query);
            pstmt.setInt(1, id);
            pstmt.execute();
            returnProducts(id, con);
            review();


        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void returnProducts(int saleID, Connection con) {
        String query = "UPDATE hidrotex.producto_has_venta SET Activo = false WHERE Venta_idVenta = ? ";
        String elementSearch = "SELECT * FROM hidrotex.producto_has_venta WHERE Venta_idVenta = ?";

        try {
            PreparedStatement pstmt = con.prepareStatement(query);
            PreparedStatement query2 = con.prepareStatement(elementSearch);
            pstmt.setInt(1, saleID);
            pstmt.execute();
            query2.setInt(1,saleID);
            ResultSet rs = query2.executeQuery();
            while(rs.next()){
                int productID = rs.getInt("Producto_idProducto");

                if(stockValue(con,productID).equals(saleValue(con,productID,saleID))){
                    returnProductsToStock(con,productID,saleID);
                    stockChanged.add(getProductName(con,productID));
                    stockchanged1.add(getKGValueSale(con,productID,saleID)+getUnitsValueSale(con,productID,saleID));
                }else{
                    stockUnchanged.add(getProductName(con,productID));
                    stockUnchanged1.add(getKGValueSale(con,productID,saleID)+getUnitsValueSale(con,productID,saleID));
                }

            }





        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    private String stockValue(Connection con, int productId) {
        String query = "SELECT Tipo FROM hidrotex.producto WHERE idProducto = ?";

        try (PreparedStatement pstmt = con.prepareStatement(query)) {
            pstmt.setInt(1, productId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String tipo = rs.getString(1);

                    if (tipo.trim().contains("U&%T")) {
                        return "U%&T";
                    } else {
                        return "K%&G";
                    }
                } else {
                    System.out.println("No result found for idProducto: " + productId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "";
    }
    private String saleValue(Connection con, int productId, int saleID) {
        String query = "SELECT " +
                "    CASE " +
                "        WHEN `Cantidad KG` > 0 THEN 'Kg' " +
                "        WHEN `Cantidad Unitaria` > 0 THEN 'Unit' " +
                "        ELSE 'None' " +
                "    END AS Type " +
                "FROM hidrotex.producto_has_venta " +
                "WHERE Producto_idProducto = ? AND Venta_idVenta = ?";
        String tipo = "";

        try (PreparedStatement pstmt = con.prepareStatement(query)) {
            // Set parameters
            pstmt.setInt(1, productId);
            pstmt.setInt(2, saleID);

            // Execute query
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String resultType = rs.getString(1).trim(); // Trim to remove extra spaces


                    // Check the result and set 'tipo'
                    if (resultType.equalsIgnoreCase("Unit")) {
                        tipo = "U%&T";
                    } else if (resultType.equalsIgnoreCase("Kg")) {
                        tipo = "K%&G";
                    } else {
                        tipo = "None";
                    }
                } else {
                    System.out.println("No rows found for productId = " + productId + " and saleID = " + saleID);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tipo;
    }

    private void returnProductsToStock(Connection connection,int idProduct,int saleId){
        double saleKG= 0;
        int saleUnits = 0;
        int units = 0;
        double kg = 0;
        String query = "UPDATE hidrotex.producto SET KG = ? WHERE idProducto = ?";
        if(stockValue(connection,idProduct).contains("U%&T")){
            query = "UPDATE hidrotex.producto SET Unidades = ? WHERE idProducto = ?";
            units = getUnitsValue(connection,idProduct);
            saleUnits = getSaleUnits(connection,idProduct,saleId);

        }else {
            kg = getKGValue(connection,idProduct);
            saleKG =getSaleKG(connection,idProduct,saleId);

        }


        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
           if(kg != 0){
               pstmt.setDouble(1,(saleKG+kg));
           }else{
               pstmt.setInt(1,(saleUnits+units));
           }
           pstmt.setInt(2,idProduct);

           pstmt.executeUpdate();




        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }



    }

    private double getKGValue(Connection connection, int idProduct ){
        String query = "SELECT KG FROM hidrotex.producto WHERE idProducto = ? ";
        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, idProduct);

            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){

               return rs.getDouble(1);
            }

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return 0;
    }
    private double getKGValueSale(Connection connection, int idProduct,int idVenta){
        String query = "SELECT `Cantidad KG` FROM hidrotex.producto_has_venta WHERE Producto_idProducto = ? AND Venta_idVenta = ? ";
        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, idProduct);
            pstmt.setInt(2,idVenta);

            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){

                return rs.getDouble(1);
            }

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return 0;
    }
    private int getUnitsValueSale(Connection connection, int idProduct,int idVenta){
        String query = "SELECT `Cantidad Unitaria` FROM hidrotex.producto_has_venta WHERE Producto_idProducto = ? AND Venta_idVenta = ? ";
        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, idProduct);
            pstmt.setInt(2,idVenta);

            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){

                return rs.getInt(1);
            }

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return 0;
    }

    private int getUnitsValue(Connection connection, int idProduct ){
        String query = "SELECT Unidades FROM hidrotex.producto WHERE idProducto = ? ";
        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, idProduct);

            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){

                return rs.getInt(1);
            }

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return 0;
    }

    private int getSaleUnits(Connection connection, int idProduct,int saleId){
        String query = "SELECT `Cantidad Unitaria` FROM hidrotex.producto_has_venta WHERE Producto_idProducto = ?  AND Venta_idVenta = ?";
        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, idProduct);
            pstmt.setInt(2,saleId);

            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){

                return rs.getInt(1);
            }

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return 0;
    }

    private double getSaleKG(Connection connection, int idProduct,int saleId){
        String query = "SELECT `Cantidad KG` FROM hidrotex.producto_has_venta WHERE Producto_idProducto = ?  AND Venta_idVenta = ?";
        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, idProduct);
            pstmt.setInt(2,saleId);

            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){

                return rs.getDouble(1);
            }

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return 0;
    }
    private String getProductName(Connection connection, int idProduct ){
        String query = "SELECT `Nombre del Producto` FROM hidrotex.producto WHERE idProducto = ? ";
        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, idProduct);

            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){

                return rs.getString(1);
            }

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "";
    }

    private void review(){
        tableStock.setCellValueFactory(data ->new SimpleStringProperty(data.getValue()));
        tableNoStock.setCellValueFactory(data ->new SimpleStringProperty(data.getValue()));
        tableStock1.setCellValueFactory(data ->new SimpleDoubleProperty(data.getValue()).asObject());
        tableNoStock1.setCellValueFactory(data ->new SimpleDoubleProperty(data.getValue()).asObject());

        if(!stockChanged.isEmpty()){
            tableviewStock.setVisible(true);
            tableviewStock1.setVisible(true);
        }


        if(!stockUnchanged.isEmpty()){

            tableviewNoStock.setVisible(true);
            tableviewNoStock1.setVisible(true);
        }
        stockList.setAll(stockChanged);
        noStockList.setAll(stockUnchanged);
        stockList1.setAll(stockchanged1);
        noStockList1.setAll(stockUnchanged1);
        tableviewStock.setItems(stockList);
        tableviewStock1.setItems(stockList1);
        tableviewNoStock1.setItems(noStockList1);
        tableviewNoStock.setItems(noStockList);
        delButton.setVisible(false);


    }


}
