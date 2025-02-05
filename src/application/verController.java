import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;

import java.awt.*;
import java.net.URI;
import java.sql.*;

public class verController {
    @FXML
    private Text ventaDiariaText;
    @FXML
    private Text ventaMensualText;

    @FXML
    private Text ingresoDiarioText;

    @FXML
    private Text ingresoMensualText;

    @FXML
    private Text egresoDiarioText;

    @FXML
    private Text egresoMensualText;

    @FXML
    private Text promedioPorVentaDiaria;

    @FXML
    private Text getPromedioPorVentaMensual;
    @FXML
    private TableColumn<Venta, String> cliente;
    @FXML
    private TableView<Venta> tableviewSales;
    @FXML
    private TableColumn<Venta, Date> fecha;
    @FXML
    private TableColumn<Venta, Double> cantidad;
    private ObservableList<Venta> ventaList = FXCollections.observableArrayList();

    @FXML
    private Hyperlink link;




    @FXML
    private void initialize(){
        cliente.setCellValueFactory(new PropertyValueFactory<>("cliente"));
        fecha.setCellValueFactory(new PropertyValueFactory<>("date"));
        cantidad.setCellValueFactory(new PropertyValueFactory<>("totalamount"));
        loadSalesData();
        ventaDiariaText.setText(results("SELECT COUNT(*) FROM hidrotex.venta\n" +
                "WHERE DAY(Fecha) = DAY(current_date())\n" +
                "AND MONTH(Fecha) = MONTH(current_date())\n" +
                "AND YEAR(Fecha) = YEAR(current_date()) AND Activo= true;",false));

        ventaMensualText.setText(results("SELECT COUNT(*) FROM hidrotex.venta\n" +
                "WHERE MONTH(Fecha) = MONTH(current_date())\n" +
                "AND YEAR(Fecha) = YEAR(current_date()) AND Activo = true;",false));

        ingresoDiarioText.setText(results("SELECT ROUND(SUM(total),2) FROM hidrotex.venta\n" +
                "WHERE DAY(Fecha) = DAY(current_date())\n" +
                "AND MONTH(Fecha) = MONTH(current_date())\n" +
                "AND YEAR(Fecha) = YEAR(current_date()) AND Activo = true;",true));

        ingresoMensualText.setText(results("SELECT ROUND(SUM(total),2) FROM hidrotex.venta\n" +
                "WHERE MONTH(Fecha) = MONTH(current_date())\n" +
                "AND YEAR(Fecha) = YEAR(current_date()) AND Activo = true;",true));

        egresoDiarioText.setText(results("SELECT ROUND(SUM(Cantidad),2) FROM hidrotex.egresos\n" +
                "WHERE DAY(Fecha) = DAY(current_date())\n" +
                "AND MONTH(Fecha) = MONTH(current_date())\n" +
                "AND YEAR(Fecha) = YEAR(current_date()) AND Activo = true;",true));
        egresoMensualText.setText(results("SELECT ROUND(SUM(Cantidad),2) FROM hidrotex.egresos\n" +
                "WHERE MONTH(Fecha) = MONTH(current_date())\n" +
                "AND YEAR(Fecha) = YEAR(current_date()) AND Activo = true;",true));
        promedioPorVentaDiaria.setText(results("SELECT ROUND(AVG(total),2) FROM hidrotex.venta\n" +
                "WHERE DAY(Fecha) = DAY(current_date())\n" +
                "AND MONTH(Fecha) = MONTH(current_date())\n" +
                "AND YEAR(Fecha) = YEAR(current_date()) AND Activo = true;",true));
        getPromedioPorVentaMensual.setText(results("SELECT ROUND(AVG(total),2) FROM hidrotex.venta\n" +
                "WHERE MONTH(Fecha) = MONTH(current_date())\n" +
                "AND YEAR(Fecha) = YEAR(current_date()) AND Activo = true;",true));
    }

    private String results(String query,boolean isMoney){
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/hidrotex", "Galvan",
                "#Chuy2001");
             PreparedStatement pstmt = connection.prepareStatement(query)) {
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String res = rs.getObject(1) == null ?  "0" : ""+rs.getObject(1);
                if(isMoney){
                    return "$"+res;
                }
                return res; // Extract the desired value
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return "";
    }
    private void loadSalesData() {
        String query = "SELECT `Cliente_idCliente`,`Fecha`,`total` FROM hidrotex.venta  WHERE Activo = true ORDER BY Fecha DESC LIMIT 10";
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/hidrotex", "Galvan",
                "#Chuy2001"); Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Venta venta = new Venta(getClientName(connection, rs.getInt("Cliente_idCliente")),
                        rs.getDate("Fecha").toLocalDate(), rs.getDouble("total"), null,
                       null,null, 0);

                ventaList.add(venta);

            }

            tableviewSales.setItems(ventaList);

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
    private String getClientName(Connection con, int id) {
        String searchQuery = "SELECT `Nombre del Cliente` FROM hidrotex.cliente WHERE `idCliente` = ?";
        try {
            PreparedStatement st = con.prepareStatement(searchQuery);
            st.setInt(1, id);
            st.execute();

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Nombre del Cliente");
                }
            }

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    @FXML
    private void openLink(ActionEvent event) {
        openWebpage("https://drive.google.com/drive/u/0/folders/1FJ1rozsBgG6xXzMTytMBdFG3oFIUSFq2");
    }

    private void openWebpage(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
