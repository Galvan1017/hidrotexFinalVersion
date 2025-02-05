

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;


import java.util.Objects;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
            Parent root = loader.load();
            MainController controller = loader.getController();
            controller.verButtonPress();
            Scene scene = new Scene(root);

            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("style.css")).toExternalForm());
            primaryStage.setTitle("Hidrotex ORM");
            primaryStage.setScene(scene);

            primaryStage.show();



        } catch (Exception e) {
            e.printStackTrace();
        }
    }




}
