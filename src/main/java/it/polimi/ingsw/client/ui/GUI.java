package it.polimi.ingsw.client.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.*;

/**
 * This class represents the graphical interface of the game
 *
 * @author Adriana Ferrari, Carlo Dell'Acqua
 */
public class GUI extends Application {

    public GUI() {
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Pane root = loader.load();
        LoginController controller = loader.getController();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/login.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/css/global.css").toExternalForm());
        stage.setTitle("Login");
        stage.setScene(scene);
        stage.setMinWidth(root.getMinWidth());
        stage.setMinHeight(root.getMinHeight());
        stage.widthProperty().addListener((obs, obj, newVal) -> controller.onResize());
        stage.heightProperty().addListener((obs, obj, newVal) -> controller.onResize());
        stage.show();
    }

    public void start() {
        launch();
    }
}
