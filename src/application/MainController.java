

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

public class MainController {

    @FXML
    protected Pane clientSubmenu;
    @FXML
    public Pane generalPane;
    @FXML
    public Button clientesVerBT;

    @FXML
    public Pane productSubmenu;

    @FXML
    private Pane egresosSubMenu;

    @FXML
    private Pane saleSubmenu;



    @FXML
    protected void verButtonPress(){
        productSubmenu.setVisible(false);
        saleSubmenu.setVisible(false);
        clientSubmenu.setVisible(false);
        egresosSubMenu.setVisible(false);
        loadFXMLIntoPane("verPane.fxml");
    }
    @FXML
    private void egresosButtonPress() {
        if (egresosSubMenu.isVisible()) {
            egresosSubMenu.setVisible(false);
        } else if (!egresosSubMenu.isVisible()) {
            egresosSubMenu.setVisible(true);
        }
        productSubmenu.setVisible(false);
        saleSubmenu.setVisible(false);
        clientSubmenu.setVisible(false);
    }
    @FXML
    private void egresosAddPane() {
        egresosSubMenu.setVisible(false);
        loadEgresosAddScreen();
    }

    private void loadEgresosAddScreen() {
        loadFXMLIntoPane("egresoAddPane.fxml");
    }

    @FXML
    private void ventaCotizarPane() {
        saleSubmenu.setVisible(false);
        loadCotizarScreen();
    }
    private void loadCotizarScreen(){
        loadFXMLIntoPane("cotizacionPane.fxml");
    }
    @FXML
    private void egresosSeePane() {
        egresosSubMenu.setVisible(false);
        loadEgresosSeeScreen();
    }

    private void loadEgresosSeeScreen() {
        loadFXMLIntoPane("egresoSeePane.fxml");
    }
    @FXML
    public void clientButtonPress() {

        if (clientSubmenu.isVisible()) {
            clientSubmenu.setVisible(false);
        } else if (!clientSubmenu.isVisible()) {
            clientSubmenu.setVisible(true);
        }
        productSubmenu.setVisible(false);
        saleSubmenu.setVisible(false);
        egresosSubMenu.setVisible(false);
    }

    @FXML
    public void clientsReadPane() {

        clientSubmenu.setVisible(false);
        loadClientSeeScreen();


    }
    @FXML
    public void clientsAddPane() {
        clientSubmenu.setVisible(false);
        loadClientAddScreen();
    }

    private void loadClientSeeScreen() {
        loadFXMLIntoPane("clientsSeePane.fxml");
    }
    private void loadClientAddScreen() {
        loadFXMLIntoPane("clientsAddPane.fxml");
    }
    @FXML
    private void productReadPane() {
        productSubmenu.setVisible(false);
        loadProductSeeScreen();
    }
    @FXML
    private void productAddPane() {
        productSubmenu.setVisible(false);
        loadProductAddScreen();
    }

    private void loadProductAddScreen() {
        loadFXMLIntoPane("productAddPane.fxml");
    }

    private void loadProductSeeScreen() {
        loadFXMLIntoPane("productSeePane.fxml");
    }
    @FXML
    public void productButtonPress() {
        if (productSubmenu.isVisible()) {
            productSubmenu.setVisible(false);
        } else if (!productSubmenu.isVisible()) {
            productSubmenu.setVisible(true);
        }
        clientSubmenu.setVisible(false);
        saleSubmenu.setVisible(false);
        egresosSubMenu.setVisible(false);

    }



    @FXML

    public void ventaButtonPress() {
        if (saleSubmenu.isVisible()) {
            saleSubmenu.setVisible(false);
        } else if (!saleSubmenu.isVisible()) {
            saleSubmenu.setVisible(true);
        }
        clientSubmenu.setVisible(false);
        productSubmenu.setVisible(false);
        egresosSubMenu.setVisible(false);
    }
    @FXML
    private void ventaAddPane() {
        saleSubmenu.setVisible(false);
        loadSaleAddScreen();
    }

    private void loadSaleAddScreen() {
        loadFXMLIntoPane("saleAddPane.fxml");
    }

    @FXML
    private void ventaSeePane() {
        saleSubmenu.setVisible(false);
        loadSaleSeeScreen();
    }
    private void loadSaleSeeScreen() {
        loadFXMLIntoPane("saleSeePane.fxml");
    }


    private void loadFXMLIntoPane(String fxmlFile) {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Pane newContent = loader.load();

            generalPane.getChildren().clear();
            generalPane.getChildren().add(newContent);
            generalPane.getChildren().add(new Label ("."));



        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
