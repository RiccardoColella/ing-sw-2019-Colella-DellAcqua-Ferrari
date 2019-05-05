package it.polimi.ingsw.client.ui.gui;

import it.polimi.ingsw.client.io.Connector;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.*;
import java.util.Optional;

/**
 * This class represents the graphical interface of the game
 *
 * @author Adriana Ferrari, Carlo Dell'Acqua
 */
public class GUI extends Application {

    public GUI() {
    }

    @Override
    public void start(Stage stage) {


        LoginController controller = new LoginController();
        controller.showAsModal();
        Optional<Connector> connector = controller.getConnector();
        if (connector.isPresent()) {
            // TODO: pass the connector
            // TODO: call the intitialization modal
            new GameController().showAsModal();
        } else {
            Platform.exit();
        }
    }

    public void start() {
        launch();
    }
}
