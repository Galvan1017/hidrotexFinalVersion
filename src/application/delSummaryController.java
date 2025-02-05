import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.text.Text;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class delSummaryController {

    @FXML
    private Text mainText;

    @FXML
    private TableView<ListItem> tableview;


    @FXML
    private TableColumn<ListItem, String> productVenta;


    @FXML
    private TableColumn<ListItem, Cantidad> precioCVenta;

    List<String> stockChanged = new ArrayList<>();

    List<String> stockUnchanged = new ArrayList<>();

    private ObservableList<ListItem> stockList = FXCollections.observableArrayList();

    int saleID = 0;
    @FXML
    private void initialize() {
        stockList = (ObservableList<ListItem>) itemList(stockChanged);
        tableview.setItems(stockList);

    }


    protected void start(ArrayList<String> stockChanged,ArrayList<String> stockUnChanged,int saleID){
        this.stockChanged = stockChanged;
        this.stockUnchanged = stockUnChanged;
        this.saleID = saleID;

    }
    private List<ListItem> itemList (List<String> stocks){
        List<ListItem> list = new ArrayList<>();

        for (String stock : stocks) {
            ListItem li = new ListItem(stock, 0, getCantidad(stock));
            list.add(li);
        }
        return list;
    }

    private Cantidad getCantidad(String productName){
        String query = "SELECT\n" +
                "\tCASE\n" +
                "\t\tWHEN `Cantidad KG` > 0 THEN `Cantidad KG`\n" +
                "        ELSE `Cantidad Unitaria`\n" +
                "        END AS \"Value\"\n" +
                "FROM hidrotex.producto_has_venta WHERE Venta_idVenta = ? AND Producto_idProducto = (\n" +
                "\tSELECT idProducto FROM hidrotex.producto WHERE `Nombre del Producto` = ?" +
                ") ;" ;

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/hidrotex", "Galvan",
                "#Chuy2001")) {

            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1,saleID);
            pstmt.setString(2,productName);
            ResultSet rs = pstmt.executeQuery();

            if(rs.next()){
                Cantidad c = new Cantidad((Number) rs.getObject(1));
                return c;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }



}
