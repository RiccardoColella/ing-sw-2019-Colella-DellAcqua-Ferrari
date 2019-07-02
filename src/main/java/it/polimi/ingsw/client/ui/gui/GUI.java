package it.polimi.ingsw.client.ui.gui;

import it.polimi.ingsw.client.ClientConfig;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * This class represents the graphical interface of the game
 *
 * @author Adriana Ferrari, Carlo Dell'Acqua
 */
public class GUI extends Application {

    /**
     * Client configuration information
     */
    private static ClientConfig config;

    /**
     * Empty constructor
     */
    public GUI() {

    }

    /**
     * This method is automatically called when launching a new GUI.
     * It creates the LoginController and then shows the GameController window
     *
     * @param stage a Stage
     */
    @Override
    public void start(Stage stage) {

        Platform.setImplicitExit(true);

        LoginController loginController = new LoginController(config);
        loginController.showAsModal();
        Optional<GameController> gameController = loginController.getGameController();
        gameController.ifPresent(WindowController::showAsModal);
    }

    /**
     * Initializes the GUI
     *
     * @param config client configuration information
     */
    public static void initialize(ClientConfig config) {
        GUI.config = config;
    }

    /**
     * This method launches a new GUI
     */
    public void start() {
        launch();
    }
}