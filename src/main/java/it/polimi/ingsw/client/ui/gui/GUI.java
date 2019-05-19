package it.polimi.ingsw.client.ui.gui;

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

    public GUI() {
    }

    @Override
    public void start(Stage stage) {

        Platform.setImplicitExit(true);

        //debug();
        LoginController loginController = new LoginController();
        loginController.showAsModal();
        Optional<GameController> gameController = loginController.getGameController();
        if (gameController.isPresent()) {
            gameController.get().showAsModal();
        }
    }

    public void start() {
        launch();
    }
}